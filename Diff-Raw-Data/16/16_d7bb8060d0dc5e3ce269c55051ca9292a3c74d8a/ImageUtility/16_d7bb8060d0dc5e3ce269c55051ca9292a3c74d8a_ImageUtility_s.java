 package org.lazydog.comic.image.utility;
 
 import java.util.List;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.UUID;
 import java.util.concurrent.Executor;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import org.lazydog.comic.ComicService;
 import org.lazydog.comic.model.Image;
 
 
 /**
  * File utility.
  * 
  * @author  Ron Rickard
  */
 public class ImageUtility {
 
     public static final String EXTENSION_SEPARATOR = ".";
     public static final String JPG_EXTENSION = "jpg";
     public static final String TIF_EXTENSION = "tif";
 
     private ComicService comicService;
 
     /**
      * Constructor.
      *
      * @throws  Exception  if unable to initialize ImageUtility.
      */
     private ImageUtility() throws Exception {
 
         // Declare.
         Context context;
 
         // Initialize the context.
         context = new InitialContext();
 
         // Initialize the service.
         this.comicService = (ComicService)context.lookup("ejb/ComicService");
     }
 
     /**
      * Get the base name of the file name.  A base name is a file name without
      * the trailing suffix.
      * 
      * @param  fileName  the file name.
      * @param  suffix    the suffix.
      * 
      * @return  the base name.
      */
     private static String getBaseName(String fileName, String suffix) {
         return (fileName.endsWith(suffix)) 
                 ? fileName.substring(0, fileName.lastIndexOf(suffix) - 1)
                 : fileName;
     }
     
     /**
      * Get the file.
      * 
      * @param  directoryPath  the directory path.
      * @param  baseName       the base name.
      * @param  extension      the extension.
      * 
      * @return  the file.
      */
     private static File getFile(String directoryPath, String baseName, String extension) {
         return new File(directoryPath, getFileName(baseName, extension));
     }
     
     /**
      * Get the file name.
      * 
      * @param  baseName   the base name.
      * @param  extension  the extension.
      * 
      * @return  the file name.
      */
     private static String getFileName(String baseName, String extension) {
         return new StringBuffer()
                 .append(baseName)
                 .append(EXTENSION_SEPARATOR)
                 .append(extension)
                 .toString();
     }
 
     /**
      * Get a unique base name.
      * 
      * @return  a unique base name.
      */
     private static String getUniqueBaseName() {
         return UUID.randomUUID().toString();
     }
 
     /**
      * Get a unique file.
      * 
      * @param  directoryPath  the directory path.
      * @param  extension      the extension.
      * 
      * @return  a unique file.
      */
     public static File getUniqueFile(String directoryPath, String extension) {
         return getFile(directoryPath, getUniqueBaseName(), extension);
     }
 
     /**
      * Close the process streams.
      *
      * @param  process  the process.
      */
     private static void closeStreams(Process process) {
 
         // Check if the process exists.
         if (process != null) {
 
             // Check if the process error stream exists.
             if (process.getErrorStream() != null) {
 
                 try {
 
                     // Close the process error stream.
                     process.getErrorStream().close();
                 }
                 catch(IOException e) {
                     // Ignore.
                 }
             }
 
             // Check if the process input stream exists.
             if (process.getInputStream() != null) {
                 try {
 
                     // Close the process input stream.
                     process.getInputStream().close();
                 }
                 catch(IOException e) {
                     // Ignore.
                 }
             }
 
             // Check if the process output stream exists.
             if (process.getOutputStream() != null) {
                 try {
 
                     // Close the process output stream.
                     process.getOutputStream().close();
                 }
                 catch(IOException e) {
                     // Ignore.
                 }
             }
         }
     }
 
     /**
      * Get the JPG file name from the TIF file name.
      * 
      * @param  tifFileName  the TIF file name.
      * 
      * @return  the JPG file name.
      */
     public static String getJpgFileName(String tifFileName) {
         return getFileName(getBaseName(tifFileName, TIF_EXTENSION), JPG_EXTENSION);
     }
 
     /**
      * Get the TIF file name from the JPG file.
      *
      * @param  jpgFileName  the JPG file name.
      *
      * @return  the TIF file name.
      */
     public static String getTifFileName(String jpgFileName) {
         return getFileName(getBaseName(jpgFileName, JPG_EXTENSION), TIF_EXTENSION);
     }
 
     /**
      * Create the JPG file from the TIF file.
      *
      * @param  tifFile            the TIF file.
      * @param  destDirectoryPath  the destination directory path.
      *
      * @return  the created JPG file.
      */
     public static File createJpgFromTif(File tifFile, String destDirectoryPath) {
 
         // Declare.
         File jpgFile;
         OutputStream outputStream;
         Process process1;
         Process process2;
         Process process3;
 
         // Initialize.
         jpgFile = new File(destDirectoryPath, getJpgFileName(tifFile.getName()));
         outputStream = null;
         process1 = null;
         process2 = null;
         process3 = null;
 
         try {
 
             // Declare.
             PipeExecutor pipeExecutor;
 
             // Initialize.
             pipeExecutor = PipeExecutor.newInstance();
 
             // Start the processes to convert a TIF file to a JPG file.
             process1 = new ProcessBuilder("tifftopnm", tifFile.getCanonicalPath()).start();
             process2 = new ProcessBuilder("pnmscale", "-width=400").start();
             process3 = new ProcessBuilder("pnmtojpeg", "-quality=75", "-optimize", "-density=72x72dpi").start();
 
             // Get the JPG file.
             outputStream = new FileOutputStream(jpgFile);
 
             // Pipe the processes.
             pipeExecutor.execute(Pipe.newInstance(process1.getInputStream(), process2.getOutputStream()));
             pipeExecutor.execute(Pipe.newInstance(process2.getInputStream(), process3.getOutputStream()));
             pipeExecutor.join().execute(Pipe.newInstance(process3.getInputStream(), outputStream));
         }
         catch(Exception e) {
             jpgFile = null;
         }
         finally {
             closeStreams(process1);
             closeStreams(process2);
             closeStreams(process3);
 
             if (outputStream != null) {
                 try {
                     outputStream.close();
                 }
                 catch(IOException e) {
                     // Ignore.
                 }
             }
         }
 
         return jpgFile;
     }
 
     public void process() throws Exception {
 
         // Declare.
         List<Image> images;
         int count;
 
         images = this.comicService.findList(Image.class);
 
         count = 0;
 
         for (Image image : images) {
 
             if ("Comic".equals(image.getType().getValue())) {
 
                 // Declare.
                 File newJpgFile;
                 File newTifFile;
                 File oldJpgFile;
                 File oldTifFile;
 
                 newJpgFile = this.getUniqueFile(image.getType().getDirectoryPath(), JPG_EXTENSION);
                 newTifFile = new File("/image/upload", this.getTifFileName(newJpgFile.getName()));
                 oldJpgFile = new File(image.getType().getDirectoryPath(), image.getFileName());
                 oldTifFile = new File("/image/tif/converted", this.getTifFileName(image.getFileName()));
 
                 if (oldJpgFile.exists() && oldTifFile.exists()) {
 
                     count++;
                     System.out.println("moving " + oldJpgFile.getCanonicalPath() + " to " + newJpgFile.getCanonicalPath());
                     System.out.println("moving " + oldTifFile.getCanonicalPath() + " to " + newTifFile.getCanonicalPath());
                 }
             }
         }
 
         System.out.println("moved " + count + " files");
     }
 
     /**
      * Main for image utility.
      *
      * @param  arguments  the command line arguments.
      */
     public static void main(String[] arguments) throws Exception {
 
         // Declare.
         ImageUtility imageUtility;
 
         // Initialize.
         imageUtility = new ImageUtility();
 
         imageUtility.process();
     }
 
     /**
      * Pipe.
      */
     private static class Pipe implements Runnable {
 
         private InputStream inputStream;
         private OutputStream outputStream;
 
         /**
          * Private constructor.
          *
          * @param  inputStream   the input stream.
          * @param  outputStream  the output stream.
          */
         private Pipe(InputStream inputStream, OutputStream outputStream) {
             this.inputStream = inputStream;
             this.outputStream = outputStream;
         }
 
         /**
          * Create a new instance of this class.
          *
          * @param  inputStream   the input stream.
          * @param  outputStream  the output stream.
          *
          * @return  a new instance of this class.
          */
         public static Pipe newInstance(InputStream inputStream, OutputStream outputStream) {
             return new Pipe(inputStream, outputStream);
         }
 
         /**
          * Run.
          */
         @Override
         public void run() {
 
             try {
 
                 // Declare.
                 byte[] bytes;
                 int bytesRead;
 
                 // Initialize.
                 bytes = new byte[1024];
                 bytesRead = 1;
 
                 // Loop as long as bytes are read.
                 while (bytesRead > -1) {
 
                     // Read bytes from the input stream.
                     bytesRead = this.inputStream.read(bytes, 0, bytes.length);
 
                     // Check if bytes are read.
                     if (bytesRead > -1) {
 
                         // Write bytes to the output stream.
                         this.outputStream.write(bytes, 0, bytesRead);
                     }
                 }
             }
             catch (Exception e) {
                 throw new RuntimeException("Broken pipe.", e);
             }
             finally {
                 try {
                     this.inputStream.close();
                 }
                 catch (Exception e) {
                     // Ignore.
                 }
                 try {
                     this.outputStream.close();
                 }
                 catch (Exception e) {
                     // Ignore.
                 }
             }
         }
     }
 
     /**
      * Pipe executor.
      */
     private static class PipeExecutor implements Executor {
 
         private boolean join;
 
         /**
          * Create a new instance of this class.
          *
          * @return  a new instance of this class.
          */
         public static PipeExecutor newInstance() {
             return new PipeExecutor();
         }
 
         /**
          * Wait for the runnable to finish.
          * 
          * @return  this instance.
          */
         public PipeExecutor join() {
             this.join = true;
             return this;
         }
 
         /**
          * Execute the runnable.
          *
          * @param  runnable  the runnable.
          */
         @Override
         public void execute(Runnable runnable) {
 
             try {
 
                 // Declare.
                 Thread thread;
 
                 // Get the thread.
                 thread = new Thread(runnable);
 
                 // Start the thread.
                 thread.start();
 
                 // Check if wait for runnable to finish.
                 if (join) {
 
                     // Wait for the thread to finish.
                     thread.join();
                 }
             }
             catch(Exception e) {
                 throw new RuntimeException("Execute interrupted.", e);
             }
             
         }
     }
 }
