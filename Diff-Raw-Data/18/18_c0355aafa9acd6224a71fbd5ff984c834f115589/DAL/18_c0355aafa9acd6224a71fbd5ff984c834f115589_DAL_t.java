 package src;
 
 import java.util.Vector;
 import javax.microedition.rms.RecordEnumeration;
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreNotFoundException;
 
 /**
  * Data Access Layer. This class provides read/write access to a RMS storage
  * for storing wallets.
  * @author Christian Lampl
  * @version 2011.05.25
  */
 
 public class DAL
 {
     private RecordStore rs;
 
     /**
      * Create a new DAL object
      */
     public DAL()
     {
     }
 
     /**
      * Reads data from the RMS and constructs a wallet object from it
      * @param walletName the wallet-file
      * @return a Wallet object containing the information from the file
      * @throws Exception if an error occurs
      */
     public Wallet readWallet(String walletName) throws Exception
     {
         try
         {
             rs= RecordStore.openRecordStore(walletName, false);
             // Wallet wdetails are stored in the first record (means that it has ID=1)
             Wallet w= new Wallet(new String(rs.getRecord(1)));
             // add expenses to the wallet
             // expenses are stored in the form:
             // value,category,location,date (see Expense.toString())
 
             RecordEnumeration re= rs.enumerateRecords(null, null, true);
 
             int id= 0;
             while (re.hasNextElement())
             {
                 id= re.nextRecordId();
 
                 if (id != 1)
                 {
                     w.addExpense(new Expense(new String(rs.getRecord(id))));
                 }
             }
 
             rs.closeRecordStore();
             return w;
         } catch (Exception e)
         {
             rs.closeRecordStore();
             throw e;
         }
     }
 
     /**
      * Deletes the specified wallet from the RecordStore
      * @param walletName the name of the wallet
      * @throws Exception if an error occurs
      * @throws RecordStoreNotFoundException if no Wallet with @param walletName exists
      */
     public void deleteWallet(String walletName) throws RecordStoreNotFoundException, Exception
     {
         try
         {
             RecordStore.deleteRecordStore(walletName);
         } catch (RecordStoreNotFoundException e)
         {
             throw e;
         } catch (Exception e)
         {
             throw e;
         }
     }
 
     /**
      * Write a Wallet into the RMS.
      * @param w the Wallet object to be written
      * @throws Exception if an error occurs
      */
     public void writeWallet(Wallet w) throws Exception
     {
         try
         {
             rs= RecordStore.openRecordStore(Util.createStoreName(w), true);
             byte[] b= w.toString().getBytes();
             rs.addRecord(b, 0, b.length);
 
             Vector exp= w.getExpenses();
             for (int i=0; i<exp.size(); i++)
             {
                 b= exp.elementAt(i).toString().getBytes();
                 rs.addRecord(b, 0, b.length);
             }
 
             rs.closeRecordStore();
         }
         catch (Exception e)
         {
             rs.closeRecordStore();
             throw e;
         }
     }
 
     /**
      * Updates the data of a wallet which already exists in the RecordStore
      * @param w the Wallet object to be updated
      * @throws Exception if an error occurs
      */
     public void updateWallet(Wallet w) throws Exception
     {
         try
         {
             RecordStore.deleteRecordStore(Util.createStoreName(w));
             writeWallet(w);
         } catch (Exception e)
         {
             throw e;
         }
     }
 
     /**
      * Returns a list of all RecordStores, or in other words, wallets
      * @return a String-Array containing wallet names
      */
     public String[] getWalletList()
     {
         String[] list= RecordStore.listRecordStores();
 
         if (list == null)
             return null;
 
         String[] newList= new String[list.length-2];
 
         if (newList.length == 0)
             return newList;
 
         int n= 0;
 
         for (int i=0; i<list.length; i++)
         {
             // the categories and settings stores should be dropped
             if ((!list[i].equals("Category")) && (!list[i].equals("Settings")))
             {
                 newList[n]= list[i];
                 n++;
             }
         }
 
         return newList;
     }
 
     /**
      * Returns a list of the stored categories
      * @return a String array containing the categories
      * @throws Exception if an error occurs
      */
     public String[] getCategories() throws Exception
     {
         try
         {
             rs= RecordStore.openRecordStore("Category", true);
             String[] cats= new String[rs.getNumRecords()];
 
             int i=0;
             RecordEnumeration re= rs.enumerateRecords(null, null, true);
 
             while (re.hasNextElement())
             {
                 cats[i]= new String(re.nextRecord());
                 i++;
             }
 
             rs.closeRecordStore();
             return cats;
         }
         catch (Exception e)
         {
             rs.closeRecordStore();
             throw e;
         }
     }
 
     /**
      * Updates the category table
      * @param cats String-Array containing all categories
      * @throws Exception if anything doesn't work
      */
     public void updateCategories(String[] cats) throws Exception
     {
         try
         {
             // at first, delete existing categories
             if (existsRecordStore("Category"))
                 RecordStore.deleteRecordStore("Category");
             
             // re-create the store and write new categories
             rs= RecordStore.openRecordStore("Category", true);
 
             for (int i=0; i<cats.length; i++)
             {
                 byte[] b= cats[i].getBytes();
                 rs.addRecord(b, 0, b.length);
             }
 
             rs.closeRecordStore();
         }
         catch (Exception e)
         {
             rs.closeRecordStore();
             throw e;
         }
     }
 
     /**
      * Reads the settings from the RecordStore
      * @return a string array containing the settings
      * @throws Exception
      * @see Util#DEFAULT_SETTINGS
      */
     public String[] getSettings() throws Exception
     {
         try
         {
             rs= RecordStore.openRecordStore("Settings", true);
             String[] settings= new String[rs.getNumRecords()];
 
             RecordEnumeration re= rs.enumerateRecords(null, null, true);
 
            String s;
             while (re.hasNextElement())
             {
                s= new String(re.nextRecord());
                int index= (Integer.valueOf(s.substring(0, 1))).intValue();
                settings[index]= s.substring(2, s.length());
                //System.out.println("Index: "+s.substring(0, 1));
                //System.out.println("Value: "+s.substring(2, s.length()));
             }
 
             rs.closeRecordStore();
             return settings;
         }
         catch (Exception e)
         {
             rs.closeRecordStore();
             throw e;
         }
     }
 
     /**
     * Updates the settings table. Entries are stored with leading index
     * { 1_Active wallet, 2_currency, 3_expense output format }, because it seems
     * that different phones read/write RMS data in different order!
      * @param settings a String array containing the settings
      * @throws Exception if anything doesn't work
      */
     public void updateSettings(String[] settings) throws Exception
     {
         try
         {
             // at first, delete existing settings
             if (existsRecordStore("Settings"))
                 RecordStore.deleteRecordStore("Settings");
 
             // re-create the store and write new settings
             rs= RecordStore.openRecordStore("Settings", true);
 
             for (int i=0; i<settings.length; i++)
             {
                settings[i]= String.valueOf(i)+"_"+settings[i];
                 byte[] b= settings[i].getBytes();
                 rs.addRecord(b, 0, b.length);
             }
 
             rs.closeRecordStore();
         }
         catch (Exception e)
         {
             rs.closeRecordStore();
             throw e;
         }
     }
 
     /**
      * Resets the whole DAL: delete all RecordStores and write default values
      * @throws Exception if anything doesn't quite work
      * @see Util#DEFAULT_CATEGORIES
      * @see Util#DEFAULT_SETTINGS
      * @see Util#DEFAULT_WALLET
      */
     public void resetDataStore() throws Exception
     {
         try
         {
             // At first, delete all the record stores
             String[] list= RecordStore.listRecordStores();
             if (list != null) {
                 for (int i=0; i<list.length; i++)
                 {
                     RecordStore.deleteRecordStore(list[i]);
                 }
             }
 
             // Create default stores
             updateCategories(Util.DEFAULT_CATEGORIES);
             updateSettings(Util.DEFAULT_SETTINGS);
             writeWallet(Util.DEFAULT_WALLET);
         } catch (Exception e)
         {
             throw e;
         }
     }
 
     /**
      * Checks if a given record store exists
      * @param rsname the name of the record store to be checked for existence
      * @return whether it exists
      */
     private boolean existsRecordStore(String rsname)
     {
         String[] list= RecordStore.listRecordStores();
 
         if (list != null)
         {
             for (int i=0; i<list.length; i++)
             {
                 if (list[i].equals(rsname))
                     return true;
             }
             return false;
         } else
             return false;
     }
 
 }
