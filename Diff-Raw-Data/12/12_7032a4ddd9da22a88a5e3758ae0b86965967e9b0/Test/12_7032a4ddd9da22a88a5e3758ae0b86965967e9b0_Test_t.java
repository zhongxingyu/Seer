 package Test;
 
 import java.util.List;
 import java.io.ObjectInputStream.GetField;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 import Core_System.*;
 import Data_Access.*;
 import GUI.LogIn;
 
 import java.sql.Date;
 import java.sql.Time;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 
 import java.sql.*;
 
 public class Test {
 
 	public static void main(String[] args) throws SQLException, MbankException {
 		/** create singleton mbank object */
 		MBank b = MBank.getInstance();
 		System.out.println(" singleton MBank   = " + b);
 
 		/*********************************************/
 		/** 2. Login as Client */
 		/**Update Client throw client activity**/
 		
 		ClientActivity activity = b.login(56, "password");
 		//TODO the permeation mast  be only for client it self and not allowed to update any other client.
 		
 		
 		//activity.updateClientDetails(new Client(1, "Aycheh2", "12345", Data_Access.Type.PLATINUM, "New Yorek","Emoachy@gmail.com", "0574704549", " try again"));
 		
 		//activity.getClientDetails(9);
 		
 //		activity.viewAccountDetails(14);
 		//activity.viewAllDiposits();
 //		activity.getClientDetails(4444);
 		//activity.depositInToAccount(9, 10000);
 		//java.sql.Date sqlDate = new Date(ts);
 		
 //		Deposit deposit = new Deposit(0,9, 1970, Data_Access.Type.REGULAR, 2000,"2013-08-23","2014-09-30");
 //		Deposit deposit = new Deposit(0,9, 1000, null, 0,"2013-08-23","2014-09-30");
 //		Deposit deposit = new Deposit(int deposit_id,int client_id,double balance,Type type,long estimated_balance,String opneng_date, String closing_date);
 //		activity.createNewDeposit(deposit);
 //		System.out.println("deposit..............."+deposit);
 		//activity.viewAllDiposits();
 //		activity.viewAllClientDeposits(20);
		activity.withdraw(1, 3768);
 //		activity.getAccountBy_Id(14);
 //		activity.viewAllClientDeposits(9);
 		
 		
 		
 		
 //		ClientActivity clAc = b.login(9, "password");
 //		 long ts = System.currentTimeMillis();
 //		 java.sql.Date sqlDate = new Date(ts);
 //		 Deposit deposit = new Deposit(0,9, 1970, Data_Access.Type.REGULAR, 2000,sqlDate,"2013-09-30");
 //		 clAc.createNewDeposit(deposit,10)
 		
 		
 		
 		
 		
 		
 		
 		
 		/**withdraw*/
 //		ClientActivity clAc = b.login(9, "password");
 //		try {
 //			 Account ac = new Account(9, 13, 39, 10,"2013_07_19_3");
 //			 
 //			clAc.withdraw(ac,1600);
 //		} catch (MbankException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		/**********************************************/
 //		ClientActivity clAc = b.login(9, "password");
 //		try {
 //			 Account ac = new Account(9, 13, 3339, 10,"2013_07_19_3");
 //			 
 //			clAc.depositInToAccount(ac,20000);
 //		} catch (MbankException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		
 		/*********************************************/
 		/**createNewDeposit**/
 //		ClientActivity clAc = b.login(9, "password");
 //		 long ts = System.currentTimeMillis();
 //		 java.sql.Date sqlDate = new Date(ts);
 //		 Deposit deposit = new Deposit(0,9, 1970, Data_Access.Type.REGULAR, 2000,sqlDate,"2013-09-30");
 //		 clAc.createNewDeposit(deposit,10);
 		/*********************************************/
 		/**view account details**/
 //		ClientActivity clAc = b.login(9, "password");
 //			Account ac = new Account(9,9, 3339, 10,"2013_07_19_3");
 //		clAc.viewAccountDetails(ac);
 		/*********************************************/
 		
 
 		/**admin login*/
 	//	b.AdminLogin("system", "admin");
 		
 //		AdminActivity act =  b.AdminLogin("system", "admin");
 //		 Client cl = new Client(0, "Dabur", "qw23rt",
 //		 Data_Access.Type.REGULAR,
 //		 "New Yorek ", "EDabur@gmail.com", "0574704549",
 //		 "comment Dabur");
 //		 Properties p = new Properties(null, null, 20124, 0, 100000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
 //		 act.addNewClient(cl,100,p); 
 		/***********************************************/
 		/**admin create new client and account*/
 		
 //		AdminActivity act = b.AdminLogin("password", "admin");		 
 //		 final String PROP_KEY1 = "gold_deposit_rate";
 //		 final String PROP_VALUE = "100000";
 //		 Properties pr = new Properties(PROP_KEY1,PROP_VALUE);
 //		 Client cl = new Client(0, "Mshico7", "password", Data_Access.Type.REGULAR, "New Yorek","Emoachy@gmail.com", "0574704549", "  again");
 //		 Account ac = new Account(0, 2, 1, 1100, "Update test seccses");
 //		 act.addNewClient(cl, 10, pr,ac);
 		 /***********************************************/
 		 /**admin update client **/
 //		 AdminActivity act = b.AdminLogin("password", "admin");
 //		 Client cl = new Client(1, "Mshico9", "password", Data_Access.Type.REGULAR, "New Yorek","Emoachy@gmail.com", "0574704549", "  I am goood");
 //		 act.updateClientDetails(cl);
 		/**********************************************/
 		/**Delete/removeClient Client by Admin***/
 		
 //		AdminActivity act = b.AdminLogin("admin", "password");
 //		 act.removeClient(34);
 		 /*******************************************/
 		 /****viewClientDetails***/
 		
 //		 AdminActivity act = b.AdminLogin("admin", "password");
 //		 act.viewClientDetails(100);
 		/**************************************************/
 		/**Delete/removeClient Client by Admin***/
 //		AdminActivity act = b.AdminLogin("admin", "password");
 //		act.removeAccount(13);
 		/****************************************************/
 //		AccountsDBManager acm = AccountsDBManager.getInstance();
 //		ConnectionPoolManager con = new ConnectionPoolManager();
 //		//ConnectionPoolManager db1 = new ConnectionPoolManager("jdbc:mysql://localhost:3306/mbank", "root", "root");
 //		String str =  "jdbc:mysql://localhost:3306/mbank"; // "jdbc:mysql://localhost:3306/mbank", "root", "root";
 //		acm.getAllAccounts(con.getConnectionFromPool(), str);
 		
 		/******************************************************/
 		
 //		AccountsDBManager acm = AccountsDBManager.getInstance();
 //		ConnectionPoolManager con = new ConnectionPoolManager();
 //		List<Account> allAccounts = acm.getAllAccounts(con.getConnectionFromPool());
 //		
 //		for (Account a : allAccounts) {
 //			System.out.println(a);
 //		}
 
 		/********************************/
 		//AdminActivity act = b.AdminLogin( "password","admin");
 		//act.viewAllAccounts();
 		///for (Account a : accounts) {
 		///System.out.println(a);
 		///}
 		/************************************/
 		
 //		AdminActivity act = b.AdminLogin( "password","admin");
 //		act.viewAllClientsDetails();
 		/*****************************************/
 		
 		/**get Client Deposit using Deposit_id**/
 //		 DepositsDBManager dmgr = DepositsDBManager.getInstance();
 //		 ConnectionPoolManager conpm = new ConnectionPoolManager();
 //		 long ts = System.currentTimeMillis();
 //		 java.sql.Date sqlDate = new Date(ts);
 //		 Deposit dp = new Deposit(20,20,1000,Type.GOLD,10010,sqlDate,"2014-10-10");
 //		 dmgr.getDeposit(conpm.getConnectionFromPool(), dp);
 		/***************************************************/
 		/**get one deposit using deposit_id**/
 //	 AdminActivity act1 = b.AdminLogin( "password","admin");
 //		 act1.viewDeposit(7);
 		/*************************************/
 		/**get all deposits**/
 		//AdminActivity act = b.AdminLogin( "password","admin");
 		//for (Deposit d : act.viewAllDiposits()){
 		//	System.out.println(d);
 	//	}
 		//act.viewAllDiposits();
 	/*****************************************/
 //		 /**get client Activity by client_id**/
 //		 AdminActivity act2 = b.AdminLogin( "admin","password");	
 //		 for (Activity a : act2.viewAllActivities()){
 //			// System.out.println(a);;
 //		 }
 		 
 		// AdminActivity act3 = b.AdminLogin( "password","admin");
 		 
 		// act3.viewAllClientDeposits(1);
 			//act2.viewClientActivities(1);
 		/***************************************/
 		// TODO Fix This and get client activities using client_id !!!!!
 		/**Get client activities**/
 		// AdminActivity act2 = b.AdminLogin( "password","admin");
 		 //act2.ViewClientActivities(1);
 		
 		/********************************************/
 		/**update System Property**/
 		//AdminActivity act4 = b.AdminLogin( "password","admin");
 		//act4.updateSystemProperty(new Properties("gold_daily_interest", "0.019"));
 		/**************************************************************************/
 		//act4.viewSystemproperty();
 //		 for (Properties a : act4.viewSystemproperty()){
 //			System.out.println(a);;
 //			 }
 		
 		 
 		
 	
 
 //	public static void main(String[] args){
 //		JFrame.setDefaultLookAndFeelDecorated(true);
 //		JDialog.setDefaultLookAndFeelDecorated(true);
 //		try{
 //			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 //		}
 //		catch (Exception ex){
 //			System.out.println("Failed loading L&F: ");
 //			System.out.println(ex);
 //		}
 		//new LogIn();
 		//new LogIn();
 //	};
 
 	
 	}/** main end */
 
 }/** end of Class **/
