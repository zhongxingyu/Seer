 package lib;
 import java.sql.*;
 import java.util.Scanner;
 import java.util.Arrays;
 
 public class DBAPI {
 
     static final String jdbcURL
     = "jdbc:oracle:thin:@ora.csc.ncsu.edu:1521:orcl";
     static final String[] ILLNESS_LIST = {"HIV", "Obesity", "High Risk Pregnancy", "COPD"};
 
     Connection conn;
     Statement stmt = null;
     ResultSet rs = null;
     ResultSet count = null;
     ResultSet check= null;
 
     public DBAPI() {
     }
 
 
     public boolean authDB(String user, String passwd) {
         try {
             // Load the driver. This creates an instance of the driver
             // and calls the registerDriver method to make Oracle Thin
             // driver available to clients.
 
             Class.forName("oracle.jdbc.driver.OracleDriver");
 
             // Get a connection from the first driver in the
             // DriverManager list that recognizes the URL jdbcURL
             conn = DriverManager.getConnection(jdbcURL, user, passwd);
             stmt = conn.createStatement();
         }
         catch(Throwable err) {
             conn = null;
         }
         return conn != null;
     }
 
     public boolean dropTables() {
         System.out.println("Dropping all tables.");
         try
         {
           stmt.executeUpdate("drop table Makes_Observation");
           stmt.executeUpdate("drop table Has_Illness");
           stmt.executeUpdate("drop table Observations");
           stmt.executeUpdate("drop table Alerts");
           stmt.executeUpdate("drop table HAS_HF");
           stmt.executeUpdate("drop table patient_info");
           stmt.executeUpdate("drop table type_assoc_ill");
           stmt.executeUpdate("drop table Observation_Type");
           stmt.executeUpdate("drop table HP_INFO");
         }
         catch(Throwable err) {
             conn = null;
             System.out.println("Not dropped table" + err);
         }
         //insert SQL to drop all related tables
         return true;
     }
 
     public boolean initTables() {
         System.out.println("Initializing tables with sample data.");
         try
         {
             // stmt = conn.createStatement();
             stmt.executeUpdate("CREATE TABLE Patient_Info " +
                 "(Patient_NAME VARCHAR(32), Address varchar (100), Age integer,Sex varchar(6),Patient_Id varchar (10)," +
                 " Password varchar(16), PublicStatus char(2), primary key (Patient_Id))");
             //System.out.println("inserted table Patient");
             stmt.executeUpdate("INSERT INTO Patient_Info " +
                 "VALUES ('Gary George', '2806 Conifer Drive, Raleigh, NC 27606', 25 , 'Male','ggeorge','geo123','Y')");
             stmt.executeUpdate("INSERT INTO Patient_Info " +
                 "VALUES ('Sheldon Cooper', '2808 Avent Ferry Road, Raleigh, NC 27616', 33 , 'Female','scooper','cooper123','Y')");
             //System.out.println("inserted value Patient");
 
 
             stmt.executeUpdate("create table HP_INFO" +
                 "(name VARCHAR(32), id VARCHAR(32), password VARCHAR(16), clinic VARCHAR(32), PRIMARY KEY(id))");
 
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Altaf Hussain', 'ahussain', 'hussain123', 'Dayview')");
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Manu Joseph', 'mjoseph', 'joseph123', 'Dayview')");
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Shane Lee', 'slee', 'lee123', 'Huntington')");
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Shyam Prasad', 'sprasad', 'prasad123', 'Huntington')");
 
             stmt.executeUpdate("create table Observation_Type"+
                 "(Type varchar (40), Category varchar(40), AdditionalInfo varchar (200), primary key (Type))");
             //System.out.println("created table oBS");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                 "VALUES ('Temperature','Physiological','Amount in Fahrenheit')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                "VALUES ('Diet','Behavioral','What was consumed, Amount in servings')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                 "VALUES ('Weight','Physiological','Amount in Pounds')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                 "VALUES ('Oxygen Saturation','Physiological','the fraction of hemoglobin that is saturated by oxygen, e.g. 95% ?')");
             //System.out.println("inserted table oBS");
 
             stmt.executeUpdate("create table type_assoc_ill" +
                 "(Illness varchar (40), Type varchar(40), primary key (Illness, Type), foreign key (Type) references Observation_Type(Type))");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('HIV', 'Temperature')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('General', 'Diet')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('General', 'Weight')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('COPD', 'Oxygen Saturation')");
 
 
             stmt.executeUpdate("create table Has_Illness"+
                 "(Patient_Id varchar(10), Illness varchar(20), Type varchar (40),  foreign key(Patient_Id) references Patient_Info(Patient_Id), " +
                 "foreign key (Illness, Type) references type_assoc_ill(Illness,Type))");
             //System.out.println("created has illness");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness,Type)" +
                 "VALUES ('ggeorge', 'HIV','Temperature')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness,Type)" +
                 "VALUES ('scooper', 'HIV','Temperature')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness,Type)" +
                 "VALUES ('scooper', 'COPD','Oxygen Saturation')");
             //System.out.println(" inserted has illness");
 
             stmt.executeUpdate("create table Alerts" +
                 "(Type varchar(40), Threshhold varchar(50), Message varchar(100), primary key (Type))");
             //System.out.println("created alerts");
             stmt.executeUpdate("Insert into Alerts (Type,Threshhold,Message)"+
                 "values ('Diet',null,null)");
             //System.out.println(" inserted alerts");
 
             stmt.executeUpdate("create table Observations"+
                 "(OId varchar(5), Type varchar(40), Date_of_observation varchar(20), time_of_observation varchar(20), " +
                 "AdditionalInfo varchar(100), primary key (OId),foreign key (Type) references Alerts(Type))");
             //System.out.println("not inserted table observations new");
             //stmt.executeUpdate("Insert into Observations (OId, Type, Date_of_observtion,time_of_observation,AdditionalInfo)"+
             //"values ('O1', 'Diet', to_date('01-01-2011','dd-mm-yyyy'),to_date('18:12:02', 'HH24:MI:SS'),'What was consumed and amount in servings: egg 1, orange ½, toast 1, margarine 1')");
             stmt.executeUpdate("Insert into Observations (OId, Type, Date_of_observation,time_of_observation,AdditionalInfo)"+
                 "values ('O1', 'Diet','01-01-2011','18:12:02','What was consumed, amount that was consumed')");
 
             stmt.executeUpdate("create table Makes_Observation" +
             "(pId varchar(10), OId varchar(5), Date_of_record date, time_of_record varchar(20), foreign key (pId) references Patient_Info(Patient_Id), " +
                 "foreign key(OId) references Observations)");
             //System.out.println("created table makesobs");
             stmt.executeUpdate("Insert into Makes_Observation (pId, OId, Date_of_record ,time_of_record)"+
                 "values ('ggeorge', 'O1',sysdate,'18:12:02')");
 
             stmt.executeUpdate("create table HAS_HF"+
                 "(hf_id varchar(20), patient_id varchar(10), on_date DATE, PRIMARY KEY(hf_id,patient_id), FOREIGN KEY(patient_id) REFERENCES PATIENT_INFO)");
         }
 
         catch(Throwable err) {
             conn = null;
             System.out.println("Error occurred while intializing tables.\n" + err);
         }
         return true;
     }
     
     public void observationMenu(String patientId)
     {
         int i=1;
                 Scanner sc= new Scanner(System.in);
 
         try {
             rs = stmt.executeQuery("select distinct type from Observation_Type where Illness ='General' or Illness in " +
                 "( SELECT Illness FROM Has_Illness where Patient_Id= '"+ patientId +"')");
             //System.out.println(patientId);
             //String typeObv1 = rs.getString("type");
             //System.out.println(i+". " + typeObv1);
             while (rs.next()) {
                 String typeObv = rs.getString("type");
                 System.out.println(i+". " + typeObv);
                 i++;
             }
         }
         catch(Throwable err) {
             conn = null;
             System.out.println("querry nt executed" + err);
         }
     }
     public void enterObservation( String patientId,String Obs_Type,String obsDate,String obsTime)
     {
         //	System.out.println("hello");
         int j=0,k=0,counter=0;
         String Obs_Data=null;
         Scanner sc= new Scanner(System.in);
         try
         {
             rs = stmt.executeQuery("select AdditionalInfo from Observation_Type where Type ='"+Obs_Type+"'");
             //rs = stmt.executeQuery("select AdditionalInfo from Observation_Type where Type ='Diet' and Illness ='General' or Illness in ( SELECT Illness FROM Has_Illness where Patient_Id= 'ggeorge')");
             while (rs.next()) {
                 String AdditionalInfo = rs.getString("AdditionalInfo");
                 String [] data= AdditionalInfo.split(",");
 
                 while(j!=data.length)
                 {
                     System.out.println("Enter "+ data[j]);
                     Obs_Data+= ""+ sc.next();
                     j++;
                 }
             }
             System.out.println("Before : "+counter);
             count = stmt.executeQuery("select count(*) as noOfRows from Observations");
             System.out.println("Before parsing : "+counter);
             while (count.next()) {
                 counter= Integer.parseInt(count.getString("noOfRows"));
             }
             counter+=1;
             //System.out.println(counter);
             stmt.executeUpdate("Insert into Observations "+
                 "values ('O"+counter+"','"+Obs_Type+"','"+ obsDate +"','"+ obsTime + "','"+ Obs_Data +"')");
             System.out.println(patientId);
             //do we need a trigger instead of insert i makes here ??
 
             //If no trigger how to enter corresponding oid..that would be an issue 
 
         stmt.executeUpdate("Insert into Makes_Observation (pId, OId, Date_of_record ,time_of_record)"+
             "values ('"+patientId+"','O"+counter+"', sysdate,'18:12:02')"); // here date and time are supposed to be system date and time as i trigger below but for the time being we have this
         System.out.println("inserted n makes");
         /*
         create or replace Trigger MakesObservationInsert
         After Inser on Observations
         Referencing new row as NewRow
         insert into Makes_Observaton (pId, OId, Date_of_record ,time_of_record)
         values (patient_Id, NewRow.OId, TO_CHAR(SYSDATE,'dd-Mon-yyy'),TO_CHAR(SYSDATE,' hh:mi:ss PM'))
         */
         }
         catch(Throwable err) {
             conn = null;
             System.out.println("querry nt executed" + err);
         }
     }
 
     public void displayObservations(String patientId) {
         int i=0;
         try
         {
             rs = stmt.executeQuery("select COUNT(*) from Observations o,makes_observation m where o.oid=m.oid and m.pid='"+patientId+"'");  
 
             while (rs.next()) {
                 System.out.println(i+". " + rs.getString("type"));
                 System.out.println(rs.getString("Oid"));
                 System.out.println(rs.getString("date_of_observation"));
                 System.out.println(rs.getString("time_of_observation"));
                 System.out.println(rs.getString("AdditionalInfo"));
                 i++;
             }
             System.out.println("End of Observation List.");
         }
         catch(Throwable err) {
             conn = null;
             System.out.println("Query not executed.\n" + err);
             }
     }
 
     public boolean addAssoc(String type, String illness) {
         //invalid illness type specified
         if (!(Arrays.asList(ILLNESS_LIST).contains(illness) || illness.equals("General"))) {
             return false;
         }
         ResultSet rs;
         try {
             rs = stmt.executeQuery("SELECT COUNT(*) FROM type_assoc_ill AI WHERE AI.type = '" + type +
                 "' AND AI.illness = '" + illness + "'");
             rs.next();
             //association is already present
             if (rs.getInt("COUNT(*)") > 0)
                 return true;
             rs = stmt.executeQuery("SELECT * FROM type_assoc_ill AI WHERE AI.type = '" + type + "'");
             if (!rs.next())
                 return false; //obs type not defined
 
             if(illness.equals("General")) {
                 stmt.executeQuery("DELETE FROM type_assoc_ill WHERE type = '" + type + "'");
                 stmt.executeQuery("INSERT INTO type_assoc_ill VALUES ('" + illness + "','" + type + "')");
                 System.out.println("after");
                 //must delete multiple duplicate resulting general illness assoc's (going from specific to general)
             }
             else {
                 rs = stmt.executeQuery("SELECT * FROM type_assoc_ill AI WHERE AI.type = '" + type + "' AND AI.illness = 'General'");
                 //if obs type is associated with general patient class
                 if(rs.next()) {
                     stmt.executeQuery("UPDATE type_assoc_ill AI SET AI.illness = '" + illness +
                         "' WHERE AI.type = '" + type + "' AND AI.illness = 'General'");
                 }
                 else {
                     stmt.executeQuery("INSERT INTO type_assoc_ill VALUES ('" + illness + "', '" + type + "')");
                 }
             }
         }
         catch (Throwable err) {
             conn = null;
             return false;
         }
         return true;
     }
 
     public boolean addNewType(String type,String category,String addtionalInformation, String illness)
     {
         boolean flag=true; //a more descriptive name would be good here
         try
         {   //To check if the type already exists
             check = stmt.executeQuery("select type from type_assoc_ill where illness ='" + illness + "'");
             while (check.next()) {
                 String typeExist = check.getString("type"); 
                 if(typeExist.equals(type))
                     flag =false;
             }
             //here first check if that particular type already exists
             if(flag)
             {
                 rs = stmt.executeQuery("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('" + type + "','" + category + "','" + addtionalInformation + "')" );
                 rs = stmt.executeQuery("INSERT INTO type_assoc_ill VALUES ('" + illness + "','" + type + "')");
             }
         }
         catch(Throwable err) {
             conn = null;
             flag = false;
         }
         return flag;
     }
     public boolean close(Connection conn) {
         if(conn != null) {
             try {
                 conn.close();
             }
             catch(Throwable whatever) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean close(Statement st) {
         if(st != null) {
             try {
                 st.close();
             }
             catch(Throwable whatever) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean close(ResultSet rs) {
         if(rs != null) {
             try {
                 rs.close();
             }
             catch(Throwable whatever) {
                 return false;
             }
         }
         return true;
     }
     public String authLogin(String uname, String password) throws SQLException
     {
         String type="";
         String query_patient="SELECT PATIENT_ID from PATIENT_INFO WHERE PATIENT_ID='"+uname+"' AND PASSWORD='"+password+"'";
         String query_hp="SELECT id from HP_INFO WHERE id='"+uname+"' AND PASSWORD='"+password+"'";
 
         Statement stmt = conn.createStatement();
         try {
              ResultSet rs_p = stmt.executeQuery(query_patient);
              if (!rs_p.next()) 
              {
                  ResultSet rs_hp = stmt.executeQuery(query_hp);
                 if(!rs_hp.next())
                     type="Enter valid username and password.";
                 else
                     type="HP";
              }
              else
                  type="patient";
              } catch (SQLException e ) 
              {}
         return type;
     }
     public boolean viewHF(String uname) throws SQLException
     {
         String query_hf="SELECT DISTINCT patient_name, p.patient_id " +
                         "FROM PATIENT_INFO p, HAS_HF h " +
                         "WHERE h.patient_id='"+uname+"' AND h.hf_id=p.patient_id";
         boolean hasHF=true;
         Statement stmt = conn.createStatement();
 
         try {
             ResultSet rs_hf = stmt.executeQuery(query_hf);
 
             if(!rs_hf.next())
             {
                 System.out.println("\n\nYOU HAVE NO HEALTH FRIENDS\n\n");
                 hasHF=false;
             }
 
             else {
                 rs_hf = stmt.executeQuery(query_hf);
                 int count=1;
                 System.out.println("\n\n\nYOUR HEALTHFRIENDS\n");
                  while (rs_hf.next())
                  {
                      String name = rs_hf.getString("patient_name");
                      String id = rs_hf.getString("patient_id");
                      System.out.println("\n"+count+". "+id+"\t\t"+name);
                      count++;
                 }
             }
         } catch (SQLException e ) 
         {}
         return hasHF;
     }
     public boolean findNewHF(String uname) throws SQLException
     {
         boolean existnewfriend=true;
         String query_hf="select p.patient_name, h1.patient_id "
             + "from patient_info p, has_illness h1, has_illness h2 "
             + "where p.patient_id=h1.patient_id "
             + "and h1.illness=h2.illness "
             + "and h1.patient_id<>h2.patient_id "
             + "and h1.patient_id<>'"+uname+"' "
             + "MINUS"
             + "(select p.patient_name, p.patient_id "
             + "from patient_info p,has_hf h "
             + "where p.patient_id=h.hf_id "
             + "and h.patient_id='"+uname+"')";
 
         Statement stmt = conn.createStatement();
 
         try {
             ResultSet rs_hf = stmt.executeQuery(query_hf);
             if(!rs_hf.next())
             {
                 System.out.println("No new friends to add\n\n");
                 existnewfriend=false;
             }
             else {
                 rs_hf = stmt.executeQuery(query_hf);
                 int count=1;
                 System.out.println("\n\n\nADD HEALTHFRIENDS\n\nPATIENT ID \tPATIENT NAME");
                 while (rs_hf.next()) {
                     String id = rs_hf.getString("patient_id");
                     String name=rs_hf.getString("patient_name");
                     System.out.println("\n"+count+". "+id+"\t"+name);
                     count++;
                 }
             }
         } catch (SQLException e ) {
         }
         return existnewfriend;
     }
     public void addNewHF(String uname, String addFriend) throws SQLException
     {
         Statement stmt = conn.createStatement();
 
         //check if the entered id is valid         
         String query_check="select patient_name "
             + "from patient_info "
             + "where patient_id='"+addFriend+"'";
         ResultSet rs_check = stmt.executeQuery(query_check);
         if(!rs_check.next())
             System.out.println("\nEnter a valid Patient ID.");
         else {
             String friendName=rs_check.getString("patient_name");
             //insert into table if id entered exists in the patient table
             String query_hf="insert into HAS_HF values('"+addFriend+"', '"+uname+"', sysdate)";
             try{
                 stmt.executeUpdate(query_hf);
                 System.out.println(friendName+ " and you are now friends.\n\n");
             }
             catch(SQLException e)
             {}
         }
     }
     public boolean viewRiskHF(String uname) throws SQLException
     {
         boolean atRisk=true;
         Statement stmt = conn.createStatement();
         String query_riskHF="select patient_name, p.patient_id "
             + "from patient_info p, alerts a "
             + "where p.patient_id=a.patient_id and a.patient_id<>'"+uname+"' "
             + "group by p.patient_name, p.patient_id "
             + "having count(a_id)>2";
 
         ResultSet rs_riskHF = stmt.executeQuery(query_riskHF);
         if(!rs_riskHF.next())
         {
              System.out.println("\n\nNo health friends at risk currently.\n");
              atRisk=false;
              rs_riskHF.close();
         }
         else {
             rs_riskHF = stmt.executeQuery(query_riskHF);
             int count=1;
             System.out.println("\n\n\nLIST OF HEALTH FRIENDS AT RISK\n\n");
             while (rs_riskHF.next()) {
                 String name=rs_riskHF.getString("patient_name");
                 String id=rs_riskHF.getString("patient_id");
                 System.out.println("\n"+count+". "+id+"\t"+name);
                 count++;
             }
         }
         return atRisk;
     }
     public void msgRiskHF(String uname, String riskFriend)
     {
         //create message table to store msg
         //store msg_id,from (uname), to (riskFriend)
         System.out.println("Msg "+ riskFriend);
     }
     public void viewHFAlerts(String uname) throws SQLException
     {
         //query for displaying friend's alerts
         String query_hf="SELECT a_id, description"
             + " FROM alerts "
             + "WHERE patient_id='"+uname+"'";
 
         Statement stmt = conn.createStatement();
 
         try {
             ResultSet rs_hf = stmt.executeQuery(query_hf);
             int count=1;
             System.out.println("\n\n\nHealth Friend's Alerts\n");
             while (rs_hf.next()) 
             {
                  String a_id = rs_hf.getString("a_id");
                  String desc = rs_hf.getString("description");
                  System.out.println("\n"+count+". "+a_id+"\t"+desc);
                  count++;
             }
         } catch (SQLException e )
         {}
     }
 
     public void viewHFobs(String uname)
     {
         //Show Health friend's Observations from observation table 
          System.out.println("No observations for now..have to check observations table");
     }
 
     //maybe not needed, left in from merge
     public void viewObs(String uname)
     {
         //query for displaying observations based on the pid(uname)
     }
 
     public void viewMyAlerts(String uname)
     {
         //query for displaying alerts based on the pid(uname)
     }
 }
