 import models.Transaction;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.vfs.VirtualFile;
 import utils.ModelHelper;
 
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 @OnApplicationStart
 public class Import extends Job {
 
     private static final Logger logger = Logger.getLogger(
             Import.class.getName());
     private final SimpleDateFormat dateFormat = new SimpleDateFormat(
             "dd.MM.yyyy");
 
     public void doJob() {
         if (Transaction.count() == 0) {
             File f = VirtualFile.fromRelativePath(
                    "/conf/fixture-transactions-full.csv").getRealFile();
             BufferedReader reader = null;
             try {
                 reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                 String line;
                 while ((line = reader.readLine()) != null) {
                     addTransaction(line);
                 }
             } catch (IOException e) {
                 logger.log(Level.ERROR, e);
             } finally {
                 IOUtils.closeQuietly(reader);
             }
         }
     }
 
     private void addTransaction(String line) {
        String[] s = line.split("_");
         try {
             Date accountingDate = dateFormat.parse(s[0]);
             String text = s[4];
             Double out = Double.parseDouble(s[5]);
             Double in = Double.parseDouble(s[6]);
             Transaction t = new Transaction();
             t.accountingDate = accountingDate;
             t.type = ModelHelper.getOrAddType(s[3]);
             t.text = text;
             t.amountOut = out;
             t.amountIn = in;
            if (s.length > 7) {
                t.tag = ModelHelper.getOrSaveTag(s[7]);
            }
             t.internal = false;
             t.dirty = false;
             t.timestamp = t.accountingDate.getTime();
             t.save();
         } catch (ParseException e) {
             logger.log(Level.ERROR, e);
         }
     }
 }
