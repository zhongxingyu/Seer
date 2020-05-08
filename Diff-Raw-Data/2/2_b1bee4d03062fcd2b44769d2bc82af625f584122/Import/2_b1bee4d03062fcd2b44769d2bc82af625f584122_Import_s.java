 import models.Transaction;
 import models.TransactionTag;
 import models.User;
 import org.apache.commons.io.IOUtils;
 import play.Logger;
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
 
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
     private static final String FIXTURES = "fixtures-local.yml";
     private static final String FIXTURES_CSV = "/conf/fixture-transactions-full.csv";
     private static final String FIELD_SEPARATOR = "_";
     private static final int FIELD_IDX_DATE = 0;
     private static final int FIELD_IDX_TEXT = 4;
     private static final int FIELD_IDX_AMOUNT = 5;
     private static final int FIELD_IDX_TAG = 7;
 
     public void doJob() {
         loadUsersFromFixture();
         loadTransactionsFromCsv();
     }
 
     private void loadUsersFromFixture() {
         if (User.count() == 0) {
             Fixtures.load(FIXTURES);
         }
     }
 
     private void loadTransactionsFromCsv() {
         if (Transaction.count() == 0) {
             File f = VirtualFile.fromRelativePath(FIXTURES_CSV).getRealFile();
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
                 Logger.error(e, "IOException");
             } finally {
                 IOUtils.closeQuietly(reader);
             }
         }
     }
 
     private Transaction saveTransaction(String line) {
         final String[] s = line.split(FIELD_SEPARATOR);
         final double amount = Double.parseDouble(s[FIELD_IDX_AMOUNT]);
         if (amount == 0) { // Skip incoming transactions
             return null;
         }
         final Transaction t = new Transaction();
         t.date = parseDate(s[FIELD_IDX_DATE]);
         t.text = FmtUtil.trimTransactionText(s[FIELD_IDX_TEXT]).trim();
         t.amount = amount;
         if (!"null".equals(s[FIELD_IDX_TAG])) {
             final TransactionTag tag = new TransactionTag();
             tag.name = s[FIELD_IDX_TAG];
             t.tag = ModelHelper.saveOrUpdate(tag);
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
            Logger.warn(e, "Failed to parse date: %s", e);
         }
         return new Date();
     }
 }
