 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.RoundingMode;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 public class Main extends Thread {
 
   protected static Path organizedXmlDirectory;
   protected static Path inputDirectory;
   protected static Path tmpDirectory;
   protected static Path outputDirectory;
   protected static KeyFile keyFile;
   protected static boolean unzipFiles;
   protected static int keyNumber;
   protected int MAX_THREADS = 1;
   protected static int KEY_SIZE;
   protected static int CHUNK_SIZE;
   protected static AtomicLong count = new AtomicLong(0);
   private static final Logger LOG = LogManager.getLogger(Main.class);
   protected static String mode;
 
   protected static BlockingQueue<Runnable> workQueue = null;
   protected static ThreadPoolExecutor tpe = null;
 
   private static QueueCounter queueThread;
 
   public Main(QueueCounter queueThread) {
    Main.queueThread = queueThread;
   }
 
   public void run() {
     // Load Property File
     Properties prop = new Properties();
     try {
       prop.load(new FileInputStream("DecryptorUtility.properties"));
     } catch (Exception e) {
       try {
         prop.load(Main.class.getResourceAsStream("DecryptorUtility.properties"));
       } catch (Exception e2) {
         e2.printStackTrace();
         LOG.error(e2.getStackTrace());
       }
     }
 
     mode = prop.getProperty("mode").trim();
 
     if (mode.equalsIgnoreCase("OrganizedXmlDirectory")) {
       // Organized XML Directory
       organizedXmlDirectory = FileSystems.getDefault().getPath(prop.getProperty("organizedXmlDirectory").trim());
       try {
         Files.createDirectories(organizedXmlDirectory);
       } catch (IOException e1) {
         e1.printStackTrace();
         LOG.error(e1.getStackTrace());
       }
       System.out.println("Organized Xml Directory: " + organizedXmlDirectory.normalize().toAbsolutePath().toString());
     } else if (mode.equalsIgnoreCase("Folder")) {
       inputDirectory = FileSystems.getDefault().getPath(prop.getProperty("inputDirectory"));
       inputDirectory.toFile().mkdirs();
       tmpDirectory = FileSystems.getDefault().getPath(prop.getProperty("tmpDirectory"));
       tmpDirectory.toFile().mkdirs();
       outputDirectory = FileSystems.getDefault().getPath(prop.getProperty("outputDirectory"));
       outputDirectory.toFile().mkdirs();
     }
 
     // Get Password
     String password = prop.getProperty("password").trim();
 
     // Get keyNumber
     keyNumber = Integer.parseInt(prop.getProperty("keyNumber").trim());
 
     // Get zip file
     unzipFiles = Boolean.valueOf(prop.getProperty("unzipFiles").trim());
 
     switch (prop.getProperty("keyType").trim().toLowerCase()) {
       case "usbtoken":
         keyFile = new USBToken(password);
         break;
       case "file":
         keyFile = new CertificateFile(prop.getProperty("keyFilePath").trim(), password);
         break;
       default:
         // Panic
     }
 
     MAX_THREADS = Integer.parseInt(prop.getProperty("maxThreads"));
     KEY_SIZE = Integer.parseInt(prop.getProperty("keySize").trim());
     CHUNK_SIZE = Integer.parseInt(prop.getProperty("chunkSize").trim());
 
     Date now = new Date();
     NumberFormat df;
     df = DecimalFormat.getInstance();
     df.setMinimumFractionDigits(2);
     df.setMaximumFractionDigits(2);
     df.setRoundingMode(RoundingMode.DOWN);
     Calendar cal = Calendar.getInstance();
     cal.setTime(now);
     int year = cal.get(Calendar.YEAR);
     int month = cal.get(Calendar.MONTH) + 1;
     String strYear, strMonth;
     strYear = StringUtils.leftPad(year + "", 4, '0');
     strMonth = StringUtils.leftPad(month + "", 2, '0');
 
     workQueue = new LinkedBlockingQueue<Runnable>();
     tpe = new ThreadPoolExecutor(MAX_THREADS, Integer.MAX_VALUE, 30, TimeUnit.SECONDS, workQueue);
 
     if (mode.equalsIgnoreCase("OrganizedXmlDirectory"))
       inputDirectory = Main.organizedXmlDirectory.resolve(strYear + File.separator + strMonth);
 
     boolean found;
     while (true) {
       found = false;
       if (!found && Main.count.get() == 0 && tpe.getTaskCount() == tpe.getCompletedTaskCount() && workQueue.size() == 0) {
         if (mode.equalsIgnoreCase("OrganizedXmlDirectory"))
           inputDirectory = Main.organizedXmlDirectory.resolve(strYear + File.separator + strMonth);
         if (inputDirectory.toFile().exists()) {
           try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory)) {
             for (Path inFile : stream) {
               found = true;
               count.incrementAndGet();
               tpe.execute(new DecryptEIDUID(inFile));
             }
           } catch (Exception e) {
             e.printStackTrace();
             LOG.error(ExceptionUtils.getStackTrace(e));
           }
         } else {
           System.out.println("All folders processed.");
           break;
         }
       }
 
       try {
         LOG.debug("Going to sleep.");
         Thread.sleep(5000);
         LOG.debug("Waking up.");
         if (Main.count.get() == 0 && tpe.getTaskCount() == tpe.getCompletedTaskCount() && workQueue.size() == 0) {
           System.out.println(inputDirectory + " Processed.");
           if (Main.mode.equalsIgnoreCase("OrganizedXmlDirectory")) {
             month--;
             if (month == 0) {
               month = 12;
               year--;
             }
             strYear = StringUtils.leftPad(year + "", 4, '0');
             strMonth = StringUtils.leftPad(month + "", 2, '0');
             found = false;
           } else {
             break;
           }
         }
       } catch (InterruptedException e) {
         e.printStackTrace();
         LOG.error(ExceptionUtils.getStackTrace(e));
       }
       
     }
     tpe.shutdown();
     queueThread.stopThread();
     System.exit(0);
   }
 
   public static void main(String[] args) throws Exception {
     // System.out.println(Arrays.toString(Security.getProviders()));
     queueThread = new QueueCounter();
     queueThread.start();
     Main mainThread = new Main(queueThread);
     mainThread.start();
   }
 }
