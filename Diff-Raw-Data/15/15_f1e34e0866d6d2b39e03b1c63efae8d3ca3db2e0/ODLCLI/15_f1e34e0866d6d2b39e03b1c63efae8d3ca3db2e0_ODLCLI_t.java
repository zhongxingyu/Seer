 
 package ui;
 import java.util.*;
 import java.sql.SQLException;
 
 import lib.*;
 
 /**
  * Description here.
  */
 
 public class ODLCLI
 {
     static final String HP = "HP",
                         PATIENT = "patient";
     DBAPI api;
     String id="";
     Scanner in;
 
     public ODLCLI() 
     {
         in = new Scanner(System.in);
         preStart();
         startMenu();
     }
 
     public static void main(String [] args)
     {
         new ODLCLI();
     }
 
     public void preStart() {
         System.out.println("Please enter an oracle DB " + 
                             "username and password.\n");
 
         System.out.println("Username (ie \"jsmith\"): ");
         String user = in.nextLine().trim();
      // System.out.print("Password: ");
      // String passwd = in.nextLine().trim();
         //Use hidden password - comment out only for local testing
        String passwd = new String(System.console().readPassword("Password: "));
         
         api = new DBAPI();
         if (!api.authDB(user, passwd)) {
             System.out.println("\nError connecting to Oracle database. Please try again.\n");
             preStart();
         }
         else
             promptReinit();
     }
 
     public void promptReinit() {
         System.out.print("\nWould you like to delete any existing tables and " + 
                     "re-initialize tables with the sample data? (y/n)");
         String input = in.nextLine().trim();
         switch (input) {
             case "y":
                 api.dropTables();
                 api.initTables();
                 break;
             case "n":
                 break;
             default:
                 System.out.println("Please enter \'y\' or \'n\'.");
                 promptReinit();
         }
     }
 
     public void startMenu()
     {
         System.out.println("\n\n\n===== Observations of Dailing Living -- Start Menu =====\n");
         System.out.println("Available Options:");
         System.out.println(" 1.  Login");
         System.out.println(" 2.  Create User");
         System.out.println(" 3.  Exit");
         System.out.print("\nInput: ");
         String input = in.nextLine().trim();
         switch (input) {
             case "1":
                 login();
                 break;
             case "2":
                 createUser();
                 break;
             case "3":
                 terminate(0, "Exiting.");
             default:
                 System.out.println("Invalid input. Please Try again.");
                 startMenu();
         }
     }
 
     public void login()
     {
         String role="";
         //Take username and password from the user
         String uname="", password="";
         System.out.print("\nUsername: ");
         uname=in.nextLine().trim();
         id=uname;
      // System.out.print("Password: ");
         //Use hidden password - comment out only for local testing
        password = new String(System.console().readPassword("Password: "));
       // password=in.nextLine().trim();
         //call to DBAPI.authLogin for validation
         try {
             role=api.authLogin(uname,password);
         }
         catch(SQLException e )
         {
             System.out.println("ERROR");
         }
         switch (role) {
             case PATIENT:
                 patientMenu(id);
                 break;
             case HP:
                 healthProfMenu();
                 break;
             default:
                 invalidUserLogin();
         }
     }
     public void invalidUserLogin() {
         System.out.println("\nLogin incorrect. Would you like to try again?");
         System.out.print("1. Try again\n2. Create User\n3. Back\nInput: ");
         String choice = in.nextLine().trim();
         if (choice.equals("1"))
             login();
         else if (choice.equals("2"))
             createUser();
         else if (choice.equals("3"))
             startMenu();
         else {
             System.out.println("Invalid option.  Please try again.");
             login();
         }
     }
 
     public void createUser()
     {
         System.out.println("Create user screen not yet implemented, returning to start menu.");
         startMenu();
     }
     public void terminate(int status, String msg)
     {
         System.out.println(msg + "\n");
         System.exit(status);
     }
     
     public void patientMenu(String id)
     {
         while(true)
         {
             System.out.println("\n\n\n\n===== Observations of Dailing Living -- Observation Menu =====\n");
             System.out.println("Available Options:");
             System.out.println(" 1.  Enter Observations");
             System.out.println(" 2.  View Observations");
             System.out.println(" 3.  Add a new Observation Type");
             System.out.println(" 4.  View my Alerts");
             System.out.println(" 5. Manage HealthFriends");
             System.out.println(" 6.  Back (log out)");
             System.out.print("\nInput: ");
             String input = in.nextLine().trim();
             switch (input) {
                 case "1":
                     recordObservation(id);
                     break;
                 case "2":
                     viewObservations(id);
                     break;
                 case "3":
                     addObservationType(PATIENT);
                     break;
                 case "4":
                 	  viewAlerts(id);//write alert function name here
                       api.viewMessages(id);
                     break;
                 case "5":
                     healthFriendsMenu();
                     break;
                 case "6":
                     startMenu();
                     break;
                 default:
                     System.out.println("Invalid input. Please Try again.");
                     patientMenu(id);
             }
         }
     }
     public void recordObservation(String id)
     {
         String obsType,obsDate,obsTime;
         System.out.println("Enter observations for the following available types based on your Illness :");
         api.observationMenu(id);
         System.out.print("Enter your type of Observation : ");
         obsType= in.nextLine().trim();
        // System.out.println(obsType +":\nEnter :");
         System.out.println("Enter Date of Observation in mm/dd/yyyy format :");
         obsDate= in.nextLine().trim();
         System.out.println("Enter Time of Observation in HH:mm:ss format :");
         obsTime= in.nextLine().trim();
         api.enterObservation(id,obsType,obsDate,obsTime);
     }
 
     public void viewObservations(String patientId)
     {
         api.displayObservations(patientId);
     }
     
     public void viewAlerts(String patientId)
     {
     	api.viewAlerts(patientId);
     }
     
     public void addObservationType(String userType)
     {
         System.out.print("Enter your Type of Observation: ");
         String type= in.nextLine().trim();
         System.out.print("Enter your Category of Observation: ");
         String category= in.nextLine().trim();
         System.out.print("Enter your Additional Information about the Observation: ");
         String additionalInfo= in.nextLine().trim();
         if (userType.equals(PATIENT)) {
             if (api.addNewType(type, category, additionalInfo, "General"))
                 System.out.println("New General observation type successfully added!");
             else
                 System.out.println("Failed to add new observation type!");
         }
         else if (userType.equals(HP))
             addAssocTypeIll(type, category, additionalInfo);
     }
 
     /**
      *  For associating an observation type with an illness at the time of insertion.
      */
     public void addAssocTypeIll(String type, String category, String additionalInfo) {
         System.out.print("Enter a Patient/illness Class to associate the observation\n" +
             "    type \"" + type + "\" with (N/A for General): ");
         String illness = in.nextLine().trim();
         if (illness.equals("N/A"))
              illness = "General";
         String threshold = "";
         if(api.addNewType(type, category, additionalInfo, illness)) {
             System.out.println("Association between type \"" + type + "\" and patient class \"" +
                 illness + "\" successfully added (or already exists)!");
             if(!illness.equals("General"))
                 promptThreshold(type);
         }
         else
             System.out.println("Failed to add association.");
     }
 
     public void promptThreshold(String type) {
         ArrayList<String> infos = api.getAdditionalInfoFields(type);
         System.out.print("Would you like to enter threshold value(s) for the entered observation type? (y/n)");
         String input = in.nextLine().trim();
         if (input.equals("y")) {
             System.out.println("Enter values in the format '>n', '<n', or '=n' without quotes, no units, no extra text, etc.");
             for(String info : infos) {
                 System.out.print("Threshold for \"" + info + "\": ");
                 String threshold = in.nextLine().trim();
                 if(api.insertThreshold(type, info, threshold))
                     System.out.println("Threshold added for " + type + " - " + info + ".");
                 else
                     System.out.println("Threshold could not be added for " + type + " - " + info + ".");
             }
         }
     }
 
     /**
      *  For adding an association to a pre-existing type of observation.
      */
     public void addAssocTypeIll() {
         System.out.println("Add an association between observation type and illness: ");
         System.out.print("Observation type: ");
         String type = in.nextLine().trim();
         System.out.print("Patient Class/Illness (N/A for General): ");
         String illness = in.nextLine().trim();
         if (illness.equals("N/A"))
             illness = "General";
         if (api.addAssoc(type, illness)) { //associate illness with type, overwrite general.  Else, create new?
             System.out.println("Association between type \"" + type + "\" and patient class \"" +
                 illness + "\" successfully added (or already exists)!");
         }
         else
             System.out.println("Failed to add association.");
     }
 
     public void healthProfMenu()
     {
         while(true)
         {
             System.out.println("\n\n\n\n===== Observations of Dailing Living -- Health Professional Menu =====\n");
             System.out.println("Available Options:");
             System.out.println(" 1.  Add a New Observation Type");
             System.out.println(" 2.  Add an Association Between Observation Type and Illness");
             System.out.println(" 3.  View Patients");
             System.out.println(" 4.  Assign a Patient an illness");
             System.out.println(" 5.  Back (log out)");
             System.out.print("\nInput: ");
             String input = in.nextLine().trim();
             switch (input) {
                 case "1":
                     addObservationType(HP);
                     break;
                 case "2":
                     addAssocTypeIll();
                     break;
                 case "3":
                     viewPatients();
                     break;
                 case "4":
                     assignIllness();
                     break;
                 case "5":
                     startMenu();
                     break;
                 default:
                     System.out.println("Invalid input. Please try again.");
                     healthProfMenu();
             }
         }
     }
 
     public void assignIllness() {
         System.out.print("Enter Patient name: ");
         String name = in.nextLine().trim();
         System.out.print("Enter Illness to assign to patient: ");
         String illness = in.nextLine().trim();
         if(checkPName(name)) {
             if(checkIllName(illness)) {
                 if(api.assignPatientIllness(name, illness))
                     System.out.println("Illness assignment to patient successful (or already exists).");
                 else
                     System.out.println("Illness assignment to patient failed.");
             }
             else
             System.out.println("Invalid illness name");
         }
         else
             System.out.println("Invalid patient name");
     }
 
     public void viewPatients() {
         System.out.println("\nView Patients: ");
         System.out.println(" 1.  View by Observation Type");
         System.out.println(" 2.  View by Patient Name");
         System.out.println(" 3.  Back");
         System.out.print("\nInput: ");
         String input = in.nextLine().trim();
         switch (input) {
             case "1":
                 displayPatientsByObsType();
                 break;
             case "2":
                 displayPatientsByName();
                 break;
             case "3":
                 break;
             default:
                 System.out.println("Invalid input. Please try again.");
                 viewPatients();
         }
     }
 
     public void displayPatientsByObsType() {
         int type_count = 0;
         ArrayList<String> o_types = api.getObsTypes();
         System.out.println("View by Observation Type:");
         for(String type : o_types) {
             type_count++;
             System.out.println(" " + type_count + ".  " + type);
         }
         System.out.println(" " + (type_count+1) + ".  Back");
         System.out.println("\nInput: ");
         Integer i_in = tryParse(in.nextLine().trim());
         if (i_in != null && i_in <= type_count+1 && i_in > 0) {
             if (i_in != type_count+1) {
                 ArrayList<String> names = api.getPatientsByObsType(o_types.get(i_in-1));
                 System.out.println("\nList of patients which have been assigned the observation type \"" + o_types.get(i_in-1) + "\":");
                 for (String name : names)
                     System.out.println("  " + name);
                 System.out.println("\nEnd of patients list");
             }
             viewPatients();
         }
         else {
             System.out.println("Invalid input. Please try again.");
             displayPatientsByObsType();
         }
     }
 
     public Integer tryParse(String input) {
         try {
             return Integer.parseInt(input);
         }
         catch (NumberFormatException e) {
             return null;
         }
     }
 
     public boolean checkPName(String name) {
         return api.getPNames().contains(name);
     }
 
     public boolean checkIllName(String illness) {
         return api.getIllNames().contains(illness);
     }
 
     public void displayPatientsByName() {
         System.out.println("View by Patient Name:");
         System.out.println("  1.  Enter patient name");
         System.out.println("  2.  Back");
         Integer i_in = tryParse(in.nextLine().trim());
         if (i_in == 1) {
             System.out.print("Name: ");
             String name = in.nextLine().trim();
             String uid = api.getID(name);
             if(checkPName(name)) {
                 System.out.println("Patient Data/Observations by Observation Type for patient \"" + name + "\":");
                 System.out.println(api.getObservationsByType(uid));
                 //TODO.
             }
             else {
                 System.out.println("Invalid name");
                 viewPatients();
             }
         }
         else if (i_in == 2)
             viewPatients();
         else {
             System.out.println("Invalid input. Please try again.");
             displayPatientsByName();
         }
     }
 
     public  void healthFriendsMenu()
   	{
       String selectHF="";
       boolean hasHF=true;
       boolean isValid;
   	System.out.println("\n\nSelect an option");
   	System.out.println("1. View existing Health Friends");
   	System.out.println("2. Find a new Health Friend");
   	System.out.println("3. Find a Health Friend at risk");
   	System.out.println("4. Back");
   	String input = in.next();
   	
   	 switch (input) {
        case "1":
       	 
       	 try{
       		 hasHF=api.viewHF(id);
       	 }
       	 catch(SQLException e)
       	 {}
       	 
       	 if(hasHF){
       	 System.out.println("***End of Health Friends List***\n\n");
       	 System.out.println("Select a friend by Health Friend ID  or '0' to go back");
       	 selectHF=in.next();
       	 
       	 	if(selectHF.matches("0"))
       		 healthFriendsMenu();
       	 	
       	 	else 
       	 	{	
       	 		try{
       	 			isValid= api.checkValidID(selectHF);
       		
       	 			if(isValid)
       	 				existingHFMenu(selectHF);
       	 			else{
       	 				System.out.println("\nEnter a valid Patient ID.\n\n");
       	 				healthFriendsMenu();
       	 			}
       		 
       		 }
       		 catch(SQLException e)
       		 {}
       	 	}
       	 }
       	 
       	 else{
       		 System.out.println("\n\nYOU HAVE NO HEALTH FRIENDS\n\n");
       		 healthFriendsMenu(); 
       	 	}
            break;
            
            
            
        case "2":
            boolean existnewfriend=true;
       	 try{
       		 existnewfriend=api.findNewHF(id);
       	 }
       	 catch(SQLException e)
       	 {}
       	
       	 Scanner s=new Scanner(System.in);
   	     String option="y";
   	     String addFriend="";
   	     while(option.matches("y")&&existnewfriend)
   	     {
   	     System.out.println("\n\nAdd new HealthFriend? (y/n) ");
   	     option=s.next();
   	     	switch(option)
   	     	{
   	     	case "y":
   	     		System.out.println("Enter PATIENT ID to add him as your friend: ");
   	     		addFriend=s.next();
   	     		try{	     		
   	     			api.addNewHF(id,addFriend);
   	     			existnewfriend=api.findNewHF(id);
   	     			}
   	     		catch(SQLException e)
   	     		{}
   	     		break;
   	     		
   	     	case "n": 
   	     		healthFriendsMenu();
   	            break;
   	         
   	            
   	     	default:
   	     		System.out.println("Invalid input. Please Try again.");
   	     		healthFriendsMenu();
   	     	}
   	     
   	     }
   	     	healthFriendsMenu();
   	     	break;
            
        case "3":
     	   messageHFRisk();
       	   break;
       	   
        case "4":
       	 	patientMenu(id);
       	 	break;
       	 
        default:
            System.out.println("Invalid input. Please Try again.");
           
   	}
   	 
 
   }
     
     public void messageHFRisk()
     {
     	 Scanner sc=new Scanner(System.in);
       	 boolean atRisk=true, validID=true;
       	 try{
       	atRisk=api.viewRiskHF(id);
       	 	}
       	 catch(SQLException e)
     		{}
       	
   	    String option="";
   	    String riskFriend="", text="";
   	     
   	    if(atRisk)
   	    {
   	     System.out.println("\n\nSend message to health friend at risk ? (y/n) ");
   	     option=sc.next();
   	     	switch(option)
   	     	{
   	     	case "y":
   	     		System.out.println("Enter HealthFriend ID to message healthfriend: ");
   	     		riskFriend=sc.next();
   	     		System.out.println("Type a message for your friend (100 chars): ");
   	     		text=sc.next();
   	     			     		
   	     		validID=api.msgRiskHF(id,riskFriend,text);
   	     		if(!validID)
   	     		messageHFRisk();
   	     		else{
   	     			healthFriendsMenu();
   	     		}
   	     		break;
   	     		
   	     	case "n": 
   	     		healthFriendsMenu();
   	            break;
   	         
   	            
   	     	default:
   	     		System.out.println("Invalid input. Please Try again.");
   	     	 try{
   	         	atRisk=api.viewRiskHF(id);
   	         	 	}
   	         	 catch(SQLException e)
   	       		{}
   	     	}
   	    }
     }
 
     public void existingHFMenu(String selectedHF) 
     {
 
             System.out.println("\n\nSelect an option");
             Scanner in=new Scanner(System.in);
             
             System.out.println("1. View a list of the friend's active (unviewed) alerts");
             System.out.println("2. View observations of the friend");
             System.out.println("3. Back");
             String input = in.next();
             
              switch (input) {
          case "1":
                  api.viewAlerts(selectedHF);
                  existingHFMenu(selectedHF);
                  break;
                  
          case "2":
                  
                   api.displayObservations(selectedHF);
                   existingHFMenu(selectedHF);
                     
                  break;
                  
          case "3":
                  healthFriendsMenu();
                  
              }
     }
 }
