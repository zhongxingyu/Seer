 import models.Transaction;
 import models.TransactionTag;
 import models.User;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 import play.vfs.VirtualFile;
 import utils.FmtUtil;
 import utils.ModelHelper;
 
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 @OnApplicationStart
 public class Import extends Job {
 
     private static final Logger logger = Logger.getLogger(Import.class.getName());
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
     private static final String FIELD_SEPARATOR = "_";
     private static final int FIELD_IDX_DATE = 0;
     private static final int FIELD_IDX_TEXT = 4;
     private static final int FIELD_IDX_AMOUNT = 5;
     private static final int FIELD_IDX_TAG = 7;
     private static final int FIELD_IDX_IMAGE_ID = 8;
 
     public void doJob() {
         loadUsersFromFixture();
         loadTransactionsFromCsv();
     }
 
     private void loadUsersFromFixture() {
         if (User.count() == 0) {
            Fixtures.load("fixtures-local.yml");
         }
     }
 
     private void loadTransactionsFromCsv() {
         if (Transaction.count() == 0) {
            File f = VirtualFile.fromRelativePath(
                    "/conf/fixture-transactions-full.csv").getRealFile();
             User user = User.all().first();
             BufferedReader reader = null;
             try {
                 reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                 String line;
                 while ((line = reader.readLine()) != null) {
                     Transaction t = saveTransaction(line);
                     if (t != null) {
                         t.user = user;
                         t.save();
                     }
                 }
             } catch (IOException e) {
                 logger.log(Level.ERROR, e);
             } finally {
                 IOUtils.closeQuietly(reader);
             }
         }
     }
 
     private Transaction saveTransaction(String line) {
         final String[] s = line.split(FIELD_SEPARATOR);
         final Transaction t = new Transaction();
        t.date = parseDate(s[FIELD_IDX_DATE]);
         t.text = FmtUtil.trimTransactionText(s[FIELD_IDX_TEXT]).trim();
         t.amount = Double.parseDouble(s[FIELD_IDX_AMOUNT]);
         if (!"null".equals(s[FIELD_IDX_TAG])) {
             TransactionTag tag = new TransactionTag();
             tag.name = s[FIELD_IDX_TAG];
             t.tag = ModelHelper.saveOrUpdate(tag);
             t.tag.imageId = Integer.parseInt(s[FIELD_IDX_IMAGE_ID], 16);
             t.tag.save();
         } else {
             t.tag = null;
         }
         t.internal = false;
         t.dirty = false;
        t.timestamp = t.date.getTime();
         return t;
     }
 
     private Date parseDate(String s) {
         try {
             return dateFormat.parse(s);
         } catch (ParseException e) {
             logger.log(Level.WARN, String.format("Failed to parse date: %s", s), e);
         }
         return null;
     }
 }
