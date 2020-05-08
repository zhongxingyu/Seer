 package de.inselhome.beermat.persistence;
 
 import android.content.Context;
 import android.util.Log;
 import com.google.gson.Gson;
 import de.inselhome.beermat.domain.Bill;
 import de.inselhome.beermat.exception.BillPersistenceException;
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BillFileRepository implements BillRepository {
 
     private static final String LOGTAG = "[beermat] BillFileRepository";
 
     private static BillFileRepository INSTANCE;
     private Context context;
 
     private BillFileRepository(Context context) {
         this.context = context;
     }
 
     public static BillFileRepository getInstance(Context context) {
         if (INSTANCE == null) {
             INSTANCE = new BillFileRepository(context);
         }
 
         return INSTANCE;
     }
 
     public Bill save(Bill bill) throws BillPersistenceException {
         long id = determineId(bill);
         File persistTo = buildBillFile(FileUtils.getBillDataDirectory(context), id);
 
         writeToFile(bill, persistTo);
 
         bill.setId(id);
         return bill;
     }
 
     public List<Bill> getAll() throws BillPersistenceException {
         List<Long> ids = determineBillIds();
         List<Bill> bills = new ArrayList<Bill>(ids.size());
 
         for (Long id: ids) {
             Bill bill = get(id);
             if (bill != null) {
                 bills.add(bill);
             }
         }
 
         return bills;
     }
 
     private Bill get(File directory, long id) throws BillPersistenceException {
         File source = buildBillFile(directory, id);
         return readFromFile(source);
     }
 
     public Bill get(long id) throws BillPersistenceException {
         File directory = FileUtils.getBillDataDirectory(context);
         return get(directory, id);
     }
 
     public Bill getProfile(long id) throws BillPersistenceException {
         File directory = FileUtils.getProfileDataDirectory(context);
         return get(directory, id);
     }
 
     public void delete(Bill bill) throws BillPersistenceException {
         if (bill.getId() > 0) {
             File toDelete = FileUtils.getBillFile(context, bill.getId());
             if (toDelete.exists() && toDelete.delete()) {
                 Log.i(LOGTAG, "Successfully deleted bill " + bill.getName());
             }
         }
     }
 
     @Override
     public Bill saveAsProfile(Bill bill) throws BillPersistenceException {
         Bill profile = (Bill) bill.clone();
 
         File profileDirectory = FileUtils.getProfileDataDirectory(context);
         long nextId = FileUtils.getNextId(profileDirectory);
 
         File target = buildProfileFile(nextId);
         FileWriter writer = null;
 
         try {
             Gson gson = new Gson();
             String json = gson.toJson(profile);
 
             writer = new FileWriter(target);
             writer.write(json);
 
             return profile;
         } catch (IOException e) {
             Log.e(LOGTAG, "Unable to write profile to disk", e);
         }
         finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {}
             }
         }
 
         return null;
     }
 
     @Override
     public List<Bill> getAllProfiles() throws BillPersistenceException {
         List<Long> ids = determineProfileIds();
         List<Bill> bills = new ArrayList<Bill>(ids.size());
 
         for (Long id: ids) {
             Bill bill = getProfile(id);
             if (bill != null) {
                 bills.add(bill);
             }
         }
 
         return bills;
     }
 
     @Override
     public void deleteProfile(Bill profile) {
         if (profile.getId() > 0) {
             File toDelete = FileUtils.getProfileFile(context, profile.getId());
             if (toDelete.exists() && toDelete.delete()) {
                 Log.i(LOGTAG, "Successfully deleted bill " + profile.getName());
             }
         }
     }
 
     private long determineId(Bill bill) throws BillPersistenceException {
         if (bill.getId() > 0) {
             return bill.getId();
         }
 
         return FileUtils.getNextId(FileUtils.getBillDataDirectory(context));
     }
 
     private List<Long> determineBillIds() {
         File billData = FileUtils.getBillDataDirectory(context);
         return FileUtils.getIds(billData);
     }
 
     private List<Long> determineProfileIds() {
         File profileData = FileUtils.getProfileDataDirectory(context);
         return FileUtils.getIds(profileData);
     }
 
     private File buildBillFile(File dir, long id) {
         return new File(dir, String.valueOf(id) + FileUtils.DATA_FILE_ENDING);
     }
 
     private File buildProfileFile(long id) {
         File profileData = FileUtils.getProfileDataDirectory(context);
         return new File(profileData, String.valueOf(id) + FileUtils.DATA_FILE_ENDING);
     }
 
     private void writeToFile(Bill bill, File destination) throws BillPersistenceException {
         FileWriter writer = null;
 
         try {
             Gson gson = new Gson();
             String s = gson.toJson(bill);
 
             writer = new FileWriter(destination);
             writer.write(s);
             writer.flush();
         } catch (IOException e) {
             throw new BillPersistenceException("Unable to persist bill to file.", e);
         }
         finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) { }
             }
         }
     }
 
     private Bill readFromFile(File source) throws BillPersistenceException {
         InputStream inputStream = null;
 
         try {
             inputStream = new FileInputStream(source);
             String json = IOUtils.toString(inputStream);
 
             return new Gson().fromJson(json, Bill.class);
         } catch (FileNotFoundException e) {
             throw new BillPersistenceException("Unable to read file: " + source.toString(), e);
         } catch (IOException e) {
             throw new BillPersistenceException("Unable to read file: " + source.toString(), e);
         }
     }
 }
