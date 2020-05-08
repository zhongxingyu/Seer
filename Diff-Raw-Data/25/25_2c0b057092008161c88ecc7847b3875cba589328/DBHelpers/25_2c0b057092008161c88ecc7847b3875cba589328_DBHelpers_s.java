 package edu.cs408.vormund;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.security.NoSuchAlgorithmException;
 
 public class DBHelpers {
 	//Where the user key will be stored upon successful login to the system. Used by functions to pass key to decryption algorithm
 	private String key;
 	private Database dbObj;
 	private int user_id = 0; //Setting this manually for now, but will have to be accessed from somewhere in the future
 
 	public DBHelpers()
 	{
 		dbObj = new Database();
 		if(!dbObj.hasConnection() || !dbObj.hasStatement())
 			System.exit(1);
 	}
 
 	//Creates new user for the system and returns the generated userID. Returns -1 if user already in system
	public int newUser(String userName, String password) throws SQLException {
 		//Check to see if userName is already taken
 		ResultSet userNameCheck = dbObj.query("SELECT * FROM user_data WHERE user_name='" + userName + "'");
 		//If the result set is not empty
		if(userNameCheck.first())
 		{
 			return -1;
 		}
 
 		//Perform the insert
 
		user_id = dbObj.insertQuery("INSERT INTO user_data (user_name, password) VALUES ('" + userName + "', '" + password + "')");
 
 
 		return user_id;
 	}
 
 	//Creates new entry for bank information. Will return -1 if account number already exists or upon other error, and 0 if successful
 	public int newBank(String name, String accountNumber, String routingNumber, String bankAddress, String accountType) throws SQLException {
 		//Check to see if accountNumber is already taken
 		boolean accountExists = false;
 
 		ResultSet bankEntries = dbObj.query("SELECT data_id FROM encryped_data WHERE category LIKE 'Bank Account' AND user_id=" + user_id);
 		if(bankEntries.first())
 		{
 			//I think that this will work as a way to loop through
 			while(!bankEntries.isAfterLast())
 			{
 				int data_id = bankEntries.getInt(bankEntries.findColumn("data_id"));
 				BankInfo tmpBank = getBank(data_id);
 				if(tmpBank.getAccountNumber().equals(accountNumber))
 				{
 					accountExists = true;
 					break;
 				}
 				bankEntries.next();
 			}
 		}
 
 		if(accountExists)
 			return -1;
 
 		//Have now shown accountNumber to not already exist in database and can proceed with the insert
 		String dataString = accountNumber + ";" + routingNumber + ";" + bankAddress + ";" + accountType;
 		int insertStatus = dbObj.insertBLOB(user_id, "Bank Account", name, dataString, key);
 
 		if(insertStatus == -1)
 			return -1;
 
 		return 0;
 	}
 
 	//Creates new entry for web account. Returns -1 if name and email pair is already taken or upon other error, and will return 0 otherwise
 	public int newWeb(String name, String url, String email, String userName, String password, String[][] securityQAPairs) throws SQLException {
 		//Check to see if name email pair is already taken
 		boolean accountExists = false;
 
 		ResultSet webEntries = dbObj.query("SELECT data_id, note FROM encryped_data WHERE category LIKE 'Web Account' AND user_id='" + user_id + "'");
 		if(webEntries.first())
 		{
 			while(!webEntries.isAfterLast())
 			{
 				int data_id = webEntries.getInt(webEntries.findColumn("data_id"));
 				WebInfo tmpWeb = getWeb(data_id);
 				if(tmpWeb.getEmail().equals(email) && name.equals(webEntries.getString(2)))
 				{
 					accountExists = true;
 					break;
 				}
 				webEntries.next();
 			}
 		}
 
 		if(accountExists)
 			return -1;
 
 		//Need to build out and insert new web blob. Start by building the csv style delimited data string
 		String dataString = url + ";" + email + ";" + userName + ";" + password;
 
 		//Dynamically add the security QA pairs into the dataString for encryption
 		for(int i = 0; i < securityQAPairs.length; i++)
 		{
 			dataString += ";";
 			dataString += securityQAPairs[i][0];
 			dataString += ";";
 			dataString += securityQAPairs[i][1];
 		}
 
 		int insertStatus = dbObj.insertBLOB(user_id, "Web Account", name, dataString, key);
 
 		if(insertStatus == -1)
 			return -1;
 
 		return 0;
 	}
 
 	//Creates new note
 	public int newNote(String name, String text) {
 		//Notes should be able to occur more than once with the same name (i.e. grocery list), so jump right into the insert
 
 		int insertStatus = dbObj.insertBLOB(user_id, "Note", name, text, key);
 
 		if(insertStatus == -1)
 			return -1;
 
 		return 0;
 	}
 
 	//Creates new entry for SSN and returns ID of data table entry. Is the user allowed to store more than one SSN for themselves? I guess they can... hmmm
 	public int newSocial(String name, String ssn) throws SQLException {
 		//Check to see if SSN is already taken
 		boolean ssnExists = false;
 
 		ResultSet ssnEntries = dbObj.query("SELECT data_id FROM encryped_data WHERE category LIKE 'SSN' AND user_id='" + user_id + "'");
 		if(ssnEntries.first())
 		{
 			while(!ssnEntries.isAfterLast())
 			{
 				int data_id = ssnEntries.getInt(ssnEntries.findColumn("data_id"));
 				String tmpSSN = getSocial(data_id).getSSN();
 				if(tmpSSN.equals(ssn))
 				{
 					ssnExists = true;
 					break;
 				}
 				ssnEntries.next();
 			}
 		}
 
 		if(ssnExists)
 			return -1;
 
 		//Have now shown ssn to not already exist in database and can proceed with the insert
 		int insertStatus = dbObj.insertBLOB(user_id, "SSN", name, ssn, key);
 
 		if(insertStatus == -1)
 			return -1;
 
 		return 0;
 	}
 
 	//Returns the userID if valid login, false otherwise
 	public boolean checkLogin(String userName, String password) {
 		ResultSet entries = null;
 		try {
 		String encrypt = Encryption.encryptHashString(password);
 		entries = dbObj.query("SELECT * FROM user_data WHERE user_name='" + userName + "' AND password='" + encrypt + "'");
 		} catch (NoSuchAlgorithmException e) {
 			System.err.println("No algorithm found: " + e);
 		}
 		return entries != null && resultsCount(entries) != 0;
 	}
 
     //Return a listing of all data entries of type bank including their name/label and ID
     public ArrayList<BankInfo> getBanks() {
         //get the type_id of Bank Account
         ArrayList<BankInfo> banks = new ArrayList<BankInfo>();
 
         try {
             ResultSet entries = dbObj.query("SELECT encryped_data FROM encryped_data WHERE type_id='" + getRecordType("Bank Account") + "' AND user_id='" + user_id + "'");
             if(entries.first())
             {
                 while(!entries.isAfterLast())
                 {
                     String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
                     banks.add(BankInfo.serializeCSVDump(decrypt));
                     entries.next();
                 }
             }
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return banks;
     }
 
     //Used to retrieve the encrypted data of a bank entry
     public BankInfo getBank(int bankEntryID) {
         BankInfo bank = null;
         try {
             ResultSet entries = dbObj.query("SELECT * FROM encrypted_data WHERE data_id='" + bankEntryID + "' AND user_id='" + user_id + "'");
             entries.first();
             String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
             bank = BankInfo.serializeCSVDump(decrypt);
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return bank;
     }
 
     //Returns a listing of all data entries of type web account including their name/label and ID
     public ArrayList<WebInfo> getWebs() {
         ArrayList<WebInfo> webs = new ArrayList<WebInfo>();
 
         try {
             ResultSet entries = dbObj.query("SELECT encryped_data FROM encryped_data WHERE type_id='" + getRecordType("Web Account") + "' AND user_id='" + user_id + "'");
             if(entries.first())
             {
                 while(!entries.isAfterLast())
                 {
                     String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
                     webs.add(WebInfo.serializeCSVDump(decrypt));
                     entries.next();
                 }
             }
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return webs;
     }
 
     //Used to retrieve the encryped data of a web account entry
     public WebInfo getWeb(int webID) {
         WebInfo web = null;
         try {
             ResultSet entries = dbObj.query("SELECT * FROM encrypted_data WHERE data_id='" + webID + "' AND user_id='" + user_id + "'");
             entries.first();
             String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
             web = WebInfo.serializeCSVDump(decrypt);
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return web;
     }
 
     //Returns a listing of all notes stored by the user including their name and ID
     public ArrayList<NoteInfo> getNotes() {
         ArrayList<NoteInfo> notes = new ArrayList<NoteInfo>();
 
         try {
             ResultSet entries = dbObj.query("SELECT encryped_data FROM encryped_data WHERE type_id='" + getRecordType("Note") + "' AND user_id='" + user_id + "'");
             if(entries.first())
             {
                 while(!entries.isAfterLast())
                 {
                     String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
                     notes.add(NoteInfo.serializeCSVDump(decrypt));
                     entries.next();
                 }
             }
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return notes;
     }
 
     //Used to retrieve the contents of a note
     public NoteInfo getNote(int noteID) {
         NoteInfo note = null;
         try {
             ResultSet entries = dbObj.query("SELECT * FROM encrypted_data WHERE data_id='" + noteID + "' AND user_id='" + user_id + "'");
             entries.first();
             String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
             note = NoteInfo.serializeCSVDump(decrypt);
  
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return note;
     }
 
     //Used to retrieve a listing of SSNs stored including their ID and the name (person they are associated with)
     public ArrayList<SSNInfo> getSocials() {
         ArrayList<SSNInfo> ssns = new ArrayList<SSNInfo>();
 
         try {
             ResultSet entries = dbObj.query("SELECT encryped_data FROM encrypted_data WHERE type_id='" + getRecordType("SSN") + "' AND user_id='" + user_id + "'");
             if(entries.first())
             {
                 while(!entries.isAfterLast())
                 {
                     String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
                     ssns.add(SSNInfo.serializeCSVDump(decrypt));
                     entries.next();
                 }
             }
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return ssns;
     }
 
     //Used to retrieve a specific SSN
     public SSNInfo getSocial(int socialID) {
         SSNInfo ssn = null;
         try {
             ResultSet entries = dbObj.query("SELECT * FROM encrypted_data WHERE data_id='" + socialID + "' AND user_id='" + user_id + "'");
             entries.first();
             String decrypt = dbObj.readFromBLOB(entries, "encrypted_data");
             ssn = SSNInfo.serializeCSVDump(decrypt);
         } catch (SQLException e) {
             //TODO: replace with error logging
             System.err.println("Database error: " + e);
         }
 
         return ssn;
     }
 
 	//Will overwrite data previously written for entry with given userID
 	public void updateUser(int userID, String userName, String password, String name) {
 		String query = "UPDATE user_data SET user_name='" + userName + "', password='" + password + "', name='" + name + "' WHERE user_id='" + userID + "'";
 		dbObj.updateQuery(query);
 	}
 
 	//Will overwrite data previously written for entry with given bankID
 	public void updateBank(int bankID, String name, String accountNumber, String routingNumber, String bankAddress, String accountType) {
 		String dataString = accountNumber + ";" + routingNumber + ";" + bankAddress + ";" + accountType;
 		dbObj.updateBLOB(bankID, name, dataString, key);
 	}
 
 	//Will overwrite data previously written for entry with given webID
 	public void updateWeb(int webID, String name, String url, String email, String userName, String password, String[][] securityQAPairs) {
 		String dataString = url + ";" + email + ";" + userName + ";" + password;
 
 		//Dynamically add the security QA pairs into the dataString for encryption
 		for(int i = 0; i < securityQAPairs.length; i++)
 		{
 			dataString += ";";
 			dataString += securityQAPairs[i][0];
 			dataString += ";";
 			dataString += securityQAPairs[i][1];
 		}
 
 		dbObj.updateBLOB(webID, name, dataString, key);
 	}
 
 	//Will overwrite data previously written for entry with given noteID
 	public void updateNote(int noteID, String name, String text) {
 		dbObj.updateBLOB(noteID, name, text, key);
 	}
 
 	//Will overwrite data previously written for entry with given socialID
 	public void updateSocial(int socialID, String name, String ssn) {
 		dbObj.updateBLOB(socialID, name, ssn, key);
 	}
 
 	//Will remove entry with given ID
 	public void delete(int ID) {
 		String query = "DELETE FROM encrypted_data WHERE data_id='" + ID + "'";
 		dbObj.updateQuery(query);
 	}
 
 	/*********************
 	*	Helper Methods   *
 	*********************/
 
 	private int getRecordType(String type) {
 		int numType = -1;
 		try {
 			ResultSet dataTypeQuery = dbObj.query("SELECT type_id FROM data_type WHERE type_name='" + type + "'");
 			dataTypeQuery.first();
 			numType = dataTypeQuery.getInt(1);
 		} catch (SQLException e) {
 			System.err.println("Database error: " + e);
 		}
 		return numType;
 	}
 
     private int resultsCount(ResultSet r) {
       int numRows = 0;
       try {
           r.last();
           numRows = r.getRow();
           r.beforeFirst();
       } catch (SQLException e) {
           System.err.println("Database error: " + e);
       }
       return numRows;
   }
 
 	public void close() {
 		dbObj.close();
 	}
 }
