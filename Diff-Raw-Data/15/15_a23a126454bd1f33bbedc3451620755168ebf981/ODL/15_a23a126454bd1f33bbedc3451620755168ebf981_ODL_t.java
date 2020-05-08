 import java.sql.ResultSet;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class ODL {
 
     public static final Scanner input = new Scanner(System.in);
     public static MyConnection myConn = null;
 
     public static void main(String[] args) {
         try {
 
             Class.forName("oracle.jdbc.driver.OracleDriver");
 
             ResultSet rs = null;
             boolean shouldContinue = true;
             char choice;
 
             try {
                 myConn = new MyConnection();
                 while (shouldContinue) {
                     System.out.println("1. Register a Patient");
                     System.out.println("2. Login as a Patient");
                     System.out.println("3. Exit System");
                     choice = input.nextLine().charAt(0);
 
                     switch (choice) {
                         case '1':
                             registerPatient();
                             break;
 
                         case '2':
                             loginAsPatient();
                             break;
 
                         case '3':
                             System.out.println("Thanks for using our system.. Have a nice day..");
                             System.out.println("Bye!!");
                             shouldContinue = false;
                             break;
 
                         default:
                             System.out.println("Please Select An Option From The Allowed Values");
                     }
                 }
 
             } finally {
                 close(rs);
                 if (myConn != null) {
                     myConn.closeStatement();
                     myConn.closeConnection();
                 }
             }
         } catch (Throwable oops) {
             if (oops instanceof MyException)
                 System.out.println(((MyException) oops).message);
             oops.printStackTrace();
         }
     }
 
     private static void loginAsPatient() throws MyException {
         System.out.println("Enter patient id: ");
         String patientId = input.nextLine();
         System.out.println("Enter password: ");
         String password = input.nextLine();
 
         int pid = Integer.parseInt(patientId);
         Patient patient = Patient.getById(pid, myConn);
         if (patient != null && patient.password.equals(password)) {
             boolean logout = false;
             char choice;
 
             while (!logout) {
                 System.out.println("1. Enter Data");
                 System.out.println("2. View Data");
                 System.out.println("3. Clear Alerts");
                 System.out.println("4. Connection");
                 System.out.println("5. Logout");
                 choice = input.nextLine().charAt(0);
 
                 switch (choice) {
 
                     case '1':
                         enterData(pid);
                         break;
                     case '2':
                         viewData(pid);
                         break;
                     case '3':
                         deleteAlerts(pid);
                         break;
                     case '4':
                         connections(pid);
                         break;
                     case '5':
                         logout = true;
                         System.out.println("You have been successfully logged out");
                         break;
 
                     default:
                         System.out.println("Please Select An Option From The Allowed Values");
 
                 }
             }
         } else
             System.out.println("Invalid Patient Id/Password pair. Please make sure you enter correct credentials");
     }
 
     private static void connections(int pid) throws MyException {
         boolean shouldContinue = true;
         while (shouldContinue) {
             System.out.println("1. Find a new Health Friend");
             System.out.println("2. Find a Health Friend at Risk");
             System.out.println("3. Go back to patient's home page");
             char choice = input.nextLine().charAt(0);
             switch(choice) {
                 case '1':
                     findNewHealthFriend(pid);
                     break;
                 case '3':
                     shouldContinue = false;
                     break;
                 default:
                     System.out.println("Please Select a valid option");
             }
         }
 
     }
 
     private static void findNewHealthFriend(int pid) throws MyException {
         System.out.println("Enter the serial number of the person you would like to be friends with: ");
         Patient patient = Patient.getById(pid, myConn);
         List<Patient> prospectiveFriends = patient.findHealthFriends(myConn);
         for(int i = 1; i<= prospectiveFriends.size(); i++)
            System.out.println(i + ". " + prospectiveFriends.get(i-1).name);
         int fid = Integer.parseInt(input.nextLine());
         HealthFriend.insert(pid, fid, myConn);
     }
 
     private static void deleteAlerts(int pid) throws MyException {
         Alert.deleteViewedAlerts(pid, myConn);
     }
 
     private static void viewData(int patientId) throws MyException {
         char choice;
         boolean shouldContinue = true;
         while (shouldContinue) {
             System.out.println("1. View Observations");
             System.out.println("2. View MyAlert");
             System.out.println("3. Go to patient login homepage");
             choice = input.nextLine().charAt(0);
 
             switch (choice) {
                 case '1':
                     viewObservations(patientId);
                     break;
                 case '2':
                     viewAlerts(patientId);
                     break;
                 case '3':
                     shouldContinue = false;
                     break;
                 default:
                     System.out.println("Please Select An Option From The Allowed Values");
             }
         }
     }
 
     private static void viewAlerts(int patientId) throws MyException {
         List<Alert> alertList = Alert.getByPId(patientId, myConn);
         for (Alert alert : alertList)
             System.out.println(alert.text + " " + alert.timestamp);
     }
 
     private static void enterData(int patientId) throws MyException {
         char choice;
         boolean shouldContinue = true;
         while (shouldContinue) {
             System.out.println("1. Enter new observation data");
             System.out.println("2. Add a new observation type");
             System.out.println("3. Go to patient login homepage");
             choice = input.nextLine().charAt(0);
 
             switch (choice) {
                 case '1':
                     enterObservations(patientId);
                     break;
                 case '2':
                     enterNewObservationType();
                     break;
                 case '3':
                     shouldContinue = false;
                     break;
                 default:
                     System.out.println("Please Select An Option From The Allowed Values");
             }
         }
     }
 
     private static void enterNewObservationType() throws MyException {
         System.out.println("Enter the observation type name :");
         String name = input.nextLine();
         ObservationType.insertForCategory(name, "General", myConn);
         while (true) {
             System.out.println("Enter additional information question for the new type:");
             String question = input.nextLine();
             ObservationQuestion.insertByTypeName(name, question, myConn);
             System.out.println("Would you like to get any more additional information? (y/n)");
             char choice = input.nextLine().toLowerCase().charAt(0);
 
             if (choice == 'n')
                 return;
         }
     }
 
     private static void enterObservations(int patientId) throws MyException {
         List<Integer> availableTypes = getObservationTypesForPatient(patientId);
 
         System.out.println("Please enter the observation type no that you would like to enter: ");
         for (int i = 0; i < availableTypes.size(); i++)
             System.out.println((i + 1) + ". " + ObservationType.getById(availableTypes.get(i), myConn).name);
 
         int typeNo = Integer.parseInt(input.nextLine());
         int otid = availableTypes.get(typeNo - 1);
         List<ObservationQuestion> questions = ObservationQuestion.getByObservationType(otid, myConn);
         for (ObservationQuestion question : questions) {
             System.out.println(question.text);
             String answer = input.nextLine();
             Date obsDate = getObservationDate();
             Date recordDate = new Date();
             Observation.insert(patientId, otid, obsDate, recordDate, question.qid, answer, myConn);
         }
 
     }
 
     private static void viewObservations(int patientId) throws MyException {
         List<Integer> availableTypes = getObservationTypesForPatient(patientId);
 
         System.out.println("Please select the observation type no: ");
         for (int i = 0; i < availableTypes.size(); i++)
             System.out.println((i + 1) + ". " + ObservationType.getById(availableTypes.get(i), myConn).name);
         int typeNo = Integer.parseInt(input.nextLine());
         System.out.println("Please enter begin date for filter in mm/dd/yyyy format eg. 03/08/1988:");
         String beginDate = input.nextLine();
         System.out.println("Please enter end date for filter in mm/dd/yyyy format eg. 03/08/1988:");
         String endDate = input.nextLine();
         try {
             List<Observation> observations = Observation.filter(patientId, availableTypes.get(typeNo - 1),
                     getDateFromString(beginDate + " 0:0:1", "MM/dd/yyyy HH:mm:ss"),
                     getDateFromString(endDate + " 23:59:59", "MM/dd/yyyy HH:mm:ss"), myConn);
             System.out.println("after filter");
             for (Observation o : observations) {
                 System.out.println(o.pid + " " + o.otid + " " + o.obvTimestamp + " " + o.recTimestamp + " " +
                         o.qid + " " + o.answer);
             }
         } catch (ParseException e) {
             e.printStackTrace();
             System.out.println("Invalid input.");
         }
     }
 
     private static List<Integer> getObservationTypesForPatient(int patientId) throws MyException {
         Set<Integer> availableTypesSet = new HashSet<Integer>();
         List<Integer> cids = PatientClassRelationship.getClassesForPatient(patientId, myConn);
 
         for (Integer cid : cids)
             availableTypesSet.addAll(PatientClassObservationTypeMapper.getByClass(cid, myConn));
 
         return new ArrayList<Integer>(availableTypesSet);
     }
 
     private static Date getObservationDate() {
         while (true) {
             try {
                 System.out.println("Enter date and time of the observation (in MM/dd/yyyy hh:mm AM/PM ex: 10/04/2013 10:15 AM): ");
                 String date = input.nextLine();
                 return getDateFromString(date, "MM/dd/yyyy hh:mm a");
             } catch (ParseException e) {
                 System.out.println("Please enter date and time in proper format ex: 10/04/2013 10:15 AM");
             }
         }
 
     }
 
     private static void registerPatient() throws ParseException, MyException {
         System.out.println("Please enter patients name:");
         String name = input.nextLine();
         System.out.println("Please enter patients address:");
         String address = input.nextLine();
         System.out.println("Please enter patients date of birth in mm/dd/yyyy format eg. 03/08/1988:");
         String dob = input.nextLine();
         System.out.println("Please enter patients sex(m/f):");
         String sex = input.nextLine();
         System.out.println("Do you want your profile to be public? (y/n):");
         String publicStatus = input.nextLine();
         System.out.println("Please enter the password (minimum 6 character long):");
         String password = input.nextLine();
 
         if (checkArgumentsForPatient(dob, sex, publicStatus, password)) {
             int patientId = Patient.insert(getDateFromString(dob, "MM/dd/yyyy"), name, address, sex.toLowerCase(),
                     publicStatus.toLowerCase(), password.trim(), myConn);
             if (patientId > 0) {
                 System.out.println("A patient has been created with id " + patientId + ".");
                 System.out.println("Please remember this id as this will be used to login next time");
                 PatientClassRelationship.insertAsGeneral(patientId, myConn);
 
             }
         }
     }
 
     private static Date getDateFromString(String dob, String format) throws ParseException {
         SimpleDateFormat formatter = new SimpleDateFormat(format);
         formatter.setLenient(false);
         return formatter.parse(dob);
     }
 
     private static boolean checkArgumentsForPatient(String dob, String sex, String publicStatus, String password) {
         try {
             getDateFromString(dob, "MM/dd/yyyy");
         } catch (Exception e) {
             System.out.println("Please Enter date in MM dd yyyy format example 03 08 1988");
             return false;
         }
         if (sex.length() != 1 || !(sex.toLowerCase().equals("m") || sex.toLowerCase().equals("f"))) {
             System.out.println("Please Enter Sex properly. The only available options are m or f");
             return false;
         }
         if (publicStatus.length() != 1 || !(publicStatus.toLowerCase().equals("y") || publicStatus.toLowerCase().equals("n"))) {
             System.out.println("Please Enter Public Status properly. The only available options are y or n");
             return false;
         }
         if (password.trim().length() < 6) {
             System.out.println("Please make sure that password is at-least 6 character long");
             return false;
         }
 
         return true;
     }
 
     static void close(ResultSet rs) {
         if (rs != null) {
             try {
                 rs.close();
             } catch (Throwable whatever) {
                 whatever.printStackTrace();
             }
         }
     }
 }
