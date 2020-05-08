 package lib;
 import java.sql.*;
 import java.util.*;
 
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
             conn.setAutoCommit(false);
             stmt.executeUpdate("drop table HAS_HF");
             stmt.executeUpdate("drop table Makes_Observation");
             stmt.executeUpdate("drop table Observations");
             stmt.executeUpdate("drop table Messages");
             stmt.executeUpdate("drop table Threshold_check");
             stmt.executeUpdate("drop table Has_Illness");
             stmt.executeUpdate("drop table type_assoc_ill");
             stmt.executeUpdate("drop table Illness");
             stmt.executeUpdate("drop table Observation_Type");
             stmt.executeUpdate("drop table HP_INFO");
             stmt.executeUpdate("drop table patient_info");
             stmt.executeUpdate("drop table address");
             conn.commit();
         }
         catch(Throwable err) {
        
                 
                 System.err.print("Transaction is being rolled back");
                 try {
 					conn.rollback();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
            // conn = null;
             System.out.println("Not dropped table" + err);
             return false;
         } finally {
             try {conn.setAutoCommit(true);} catch(SQLException e) {}
         }
         return true;
     }  
         //insert SQL to drop all related tables
         
     
     public boolean initTables() {
         System.out.println("Initializing tables with sample data.");
         try
         {
         	stmt.executeUpdate("CREATE TABLE Patient_Info " +
                     "(Patient_NAME VARCHAR(32), Age integer,Sex varchar(6),Patient_Id varchar (10)," +
                     " Password varchar(16), PublicStatus char(2), primary key (Patient_Id))");
                 stmt.executeUpdate("INSERT INTO Patient_Info " +
                    "VALUES ('Gary George', 25 , 'Male','ggeorge','geo123','Y')");
                 stmt.executeUpdate("INSERT INTO Patient_Info " +
                     "VALUES ('Sheldon Cooper', 33 , 'Female','scooper','cooper123','Y')");
                 stmt.executeUpdate("INSERT INTO Patient_Info " +
                         "VALUES ('Adnan Kazi', 31 , 'Female','akazi','kazi123','Y')");
                 stmt.executeUpdate("INSERT INTO Patient_Info " +
                         "VALUES ('Neha Shetty', 40 , 'Female','nshetty','shetty123','Y')");
                 stmt.executeUpdate("INSERT INTO Patient_Info " +
                         "VALUES ('Michael Watson', 47 , 'Male','mwatson','watson123','Y')");
                 stmt.executeUpdate("INSERT INTO Patient_Info " +
                         "VALUES ('Tom Kerr', 40 , 'Male','tkerr','tkerr123','Y')");
                 stmt.executeUpdate("INSERT INTO Patient_Info " +
                         "VALUES ('Maya Tran', 37 , 'Female','mtran','tran123','Y')");
                         
                 stmt.executeUpdate("CREATE TABLE Address " +
                         "(Patient_id VARCHAR(32), apt_no varchar (100), street_name varchar(100), city varchar(20), State varchar(32), zipcode varchar(10),"
                         + " primary key (Patient_Id))");
                 stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
                 		+ "values('ggeorge','2806','Avent Ferry Road', 'Raleigh', 'NC', '27607')");
                 stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
                 		+ "values('scooper','2808', 'Avent Ferry Road', 'Raleigh', 'NC', '27616')");
                 stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
                 		+ "values('akazi','1234', 'Capability Drive', 'Raleigh', 'NC', '27655')");
                 stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
                 		+ "values('nshetty','440', 'Sullivan Drive', 'Chapel Hill', 'NC', '27517')");
                 stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
                 		+ "values('mwatson','2222', 'Gorman Street', 'Raleigh', 'NC', '27678')");
                 stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
                 		+ "values('tkerr','1430', 'Collegeview Ave','Durham', 'NC', '27701')");
                 stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
                 		+ "values('mtran','100', 'Brown Circle', 'Chapel Hill', 'NC', '27516')");
                     
 
 
             stmt.executeUpdate("create table HP_INFO" +
                 "(name VARCHAR(32), id VARCHAR(32), password VARCHAR(16), clinic VARCHAR(32), PRIMARY KEY(id))");
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Altaf Hussain', 'ahussain', 'hussain123', 'Dayview')");
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Manu Joseph', 'mjoseph', 'joseph123', 'Dayview')");
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Shane Lee', 'slee', 'lee123', 'Huntington')");
             stmt.executeUpdate("INSERT INTO HP_Info VALUES ('Shyam Prasad', 'sprasad', 'prasad123', 'Huntington')");
 
             stmt.executeUpdate("create table Observation_Type"+
                 "(Type varchar (40), Category varchar(40), AdditionalInfo varchar (200), primary key (Type))");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                 "VALUES ('Temperature','Physiological','Amount in Fahrenheit')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                 "VALUES ('Diet','Behavioral','What was consumed, Amount in servings')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                 "VALUES ('Weight','Physiological','Amount in Pounds')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                 "VALUES ('Oxygen Saturation','Physiological','the fraction of hemoglobin that is saturated by oxygen e.g. 95% ?')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('Exercise','Behavioral','What kind: walking/cycling/jogging, Duration: number of minutes ')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('Mood','Psychological','One of the values: happy/sad/neutral')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('Exercise Tolerance','Physiological','Number of steps before exhaustion')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('Pain','Physiological','Scale [1 - 10]')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('Contraction','Physiological','Frequency - # per hour')");
             stmt.executeUpdate("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('Blood Pressure','Physiological','Systolic,Diastolic')");
                
 
             stmt.executeUpdate("CREATE TABLE Illness(Illness varchar (40), primary key(Illness))");
             stmt.executeUpdate("INSERT INTO Illness VALUES ('COPD')");
             stmt.executeUpdate("INSERT INTO Illness VALUES ('HIV')");
             stmt.executeUpdate("INSERT INTO Illness VALUES ('General')");
             stmt.executeUpdate("INSERT INTO Illness VALUES ('High Risk Pregnancy')");
             stmt.executeUpdate("INSERT INTO Illness VALUES ('Obesity')");
 
             stmt.executeUpdate("create table type_assoc_ill" +
                 "(Illness varchar (40), Type varchar(40), primary key (Illness, Type), foreign key (Type) references Observation_Type(Type)," +
                 " foreign key (Illness) references Illness)");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('General', 'Diet')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('General', 'Weight')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('General', 'Mood')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('General', 'Exercise')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('COPD', 'Oxygen Saturation')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('COPD', 'Exercise Tolerance')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('HIV', 'Temperature')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('High Risk Pregnancy', 'Pain')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('High Risk Pregnancy', 'Contraction')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('High Risk Pregnancy', 'Blood Pressure')");
             stmt.executeUpdate("INSERT INTO type_assoc_ill VALUES ('Obesity', 'Blood Pressure')");
             
             
 
 
             stmt.executeUpdate("create table Has_Illness"+
                 "(Patient_Id varchar(10), Illness varchar(20), foreign key(Patient_Id) references Patient_Info(Patient_Id), " +
                 "foreign key (Illness) references Illness, primary key (Patient_Id, Illness))");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                 "VALUES ('ggeorge', 'HIV')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                 "VALUES ('scooper', 'HIV')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                 "VALUES ('scooper', 'COPD')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('akazi', 'Obesity')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('akazi', 'High Risk Pregnancy')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('nshetty', 'Obesity')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('nshetty', 'High Risk Pregnancy')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('mwatson', 'COPD')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('tkerr', 'COPD')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('tkerr', 'Obesity')");
             stmt.executeUpdate("INSERT INTO Has_Illness (Patient_Id, Illness)" +
                     "VALUES ('mtran', 'High Risk Pregnancy')");
             
             
 
             stmt.executeUpdate("create table Threshold_check" +
                     "(Type varchar(40), AdditionalInfo varchar(100),Threshold varchar(50),"
                     + "primary key (Type,AdditionalInfo),foreign key(Type) references Observation_type(type))");
             stmt.executeUpdate("INSERT INTO Threshold_check (Type, AdditionalInfo, Threshold) "
             + "VALUES ('Temperature', 'Amount in Fahrenheit', '>102')");
             stmt.executeUpdate("INSERT INTO Threshold_check (Type, AdditionalInfo, Threshold) "
             + "VALUES ('Oxygen Saturation', 'the fraction of hemoglobin that is saturated by oxygen e.g. 95%', '<88')");
             stmt.executeUpdate("INSERT INTO Threshold_check (Type, AdditionalInfo, Threshold) "
             + "VALUES ('Pain', 'Scale [1 - 10]', '>7')");
             stmt.executeUpdate("INSERT INTO Threshold_check (Type, AdditionalInfo, Threshold) "
             + "VALUES ('Contraction', 'Frequency - # per hour', '>4')");
             stmt.executeUpdate("INSERT INTO Threshold_check (Type, AdditionalInfo, Threshold) "
             + "VALUES ('Blood Pressure', 'Systolic', '>140')");
             stmt.executeUpdate("INSERT INTO Threshold_check (Type, AdditionalInfo, Threshold) "
             + "VALUES ('Blood Pressure', 'Diastolic', '>90')");
 
 
             stmt.executeUpdate("CREATE TABLE messages " +
                  "(from_pId varchar (20), to_friend varchar(20), on_date DATE, text varchar(100)," +
                  " foreign key (from_pID) references Patient_Info, foreign key (to_friend) references Patient_Info, " +
                 "primary key (from_pId, to_friend))" );
             
 
 
             stmt.executeUpdate("create table Observations" +
                 "(OId varchar(5), Type varchar(40), Date_of_observation varchar(20), time_of_observation varchar(20)," +
                 " AdditionalInfo varchar(100),threshValue varchar(100),isactive char(5) default 'FALSE' check (isactive in ('TRUE','FALSE') )," + 
                 " primary key (OId), foreign key (Type) references Observation_Type(Type))");
             
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O1','Diet','4/5/2013','08:40:00','What was consumed','Eggs','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O2','Diet','4/5/2013','08:40:00','Amount in Servings','1','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O3','Weight','4/5/2013','08:05:00','Amount','100','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O4','Exercise','4/5/2013','07:35:00','Type','walking','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O5','Exercise','4/5/2013','07:35:00','Duration','30','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O6','Mood','4/5/2013','09:00:00','Value','Neutral','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O7','Temperature','4/5/2013','6:00:00','Amount','98.2','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O8','Exercise Tolerance','4/5/2013','11:00:00','Amount','20','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O9','Oxygen Saturation','4/5/2013','10:00:00','Amount','78','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O10','Weight','4/6/2013','08:00:00','Amount','102','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O11','Weight','4/5/2013','07:50:00','Amount','150','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O12','Weight','4/6/2013','08:00:00','Amount','156','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O13','Blood Pressure','4/6/2013','07:50:00','Systolic','150','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O14','Blood Pressure','4/6/2013','07:50:00','Diastolic','150','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O15','Blood Pressure','4/8/2013','08:00:00','Systolic','170','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O16','Blood Pressure','4/8/2013','08:00:00','Diastolic','90','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O17','Blood Pressure','4/6/2013','07:50:00','Systolic','162','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O18','Blood Pressure','4/6/2013','07:50:00','Diastolic','110','FALSE')");
             stmt.executeUpdate("INSERT INTO Observations (OId, Type,Date_of_observation,time_of_observation,AdditionalInfo,ThreshValue,isactive) "
                     + " VALUES ('O19','Pain','4/6/2013','13:0:00','Amount','8','FALSE')");
             
             
             stmt.executeUpdate("create table Makes_Observation" +
                     "(pId varchar(10), OId varchar(5), datetime_of_record timestamp, foreign key (pId) references Patient_Info(Patient_Id), " +
                         "foreign key(OId) references Observations, primary key(Pid,OId))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O1',TO_DATE('2013/05/04 08:40:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O2',TO_DATE('2013/05/04 08:40:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O3',TO_DATE('2013/05/04 08:05:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O4',TO_DATE('2013/05/04 07:35:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O5',TO_DATE('2013/05/04 07:35:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O6',TO_DATE('2013/05/04 21:00:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O7',TO_DATE('2013/05/04 06:10:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O8',TO_DATE('2013/05/04 21:00:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O9',TO_DATE('2013/05/04 10:10:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
             		+ "values('scooper','O10',TO_DATE('2013/05/04 08:06:44', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('ggeorge','O11',TO_DATE('2013/05/04 08:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('ggeorge','O12',TO_DATE('2013/06/04 08:05:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('akazi','O13',TO_DATE('2013/06/04 08:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('akazi','O14',TO_DATE('2013/06/04 08:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('akazi','O15',TO_DATE('2013/06/04 08:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('akazi','O16',TO_DATE('2013/06/04 08:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('nshetty','O17',TO_DATE('2013/06/04 08:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('nshetty','O18',TO_DATE('2013/06/04 08:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             stmt.executeUpdate("insert into makes_observation(pId,OId,datetime_of_record) "
                     + "values('mtran','O19',TO_DATE('2013/06/04 18:00:00', 'yyyy/mm/dd hh24:mi:ss'))");
             
     
 
 
             stmt.executeUpdate("create table HAS_HF"+
                 "(hf_id varchar(20), patient_id varchar(10), on_date DATE, PRIMARY KEY(hf_id,patient_id), FOREIGN KEY(patient_id) REFERENCES PATIENT_INFO)");
             stmt.executeUpdate("Insert into HAS_HF(hf_id, patient_id, on_date) "
             + "Values('tkerr', 'akazi', TO_DATE('04-01-2013', 'DD.MM.YYYY'))");
             stmt.executeUpdate("Insert into HAS_HF(hf_id, patient_id, on_date) "
             + "Values('tkerr', 'mwatson', TO_DATE('03-04-2011', 'DD.MM.YYYY'))");
             stmt.executeUpdate("Insert into HAS_HF(hf_id, patient_id, on_date) "
             + "Values('scooper', 'ggeorge', TO_DATE('10-12-2012', 'DD.MM.YYYY'))");
             stmt.executeUpdate("Insert into HAS_HF(hf_id, patient_id, on_date) "
             + "Values('scooper', 'mwatson', TO_DATE('01-02-2013', 'DD.MM.YYYY'))");
             stmt.executeUpdate("Insert into HAS_HF(hf_id, patient_id, on_date) "
             + "Values('scooper', 'tkerr', TO_DATE('05-04-2011', 'DD.MM.YYYY'))");
         }
 
         catch(Throwable err) {
            // conn = null;
             System.out.println("Error occurred while intializing tables.\n" + err);
         }
         return true;
     }
 
     public void addAddress(String patient_id, String apt_no, String street_name, String city, String state, String zipcode) throws SQLException
     {
     	stmt.executeUpdate("INSERT INTO address(patient_id,apt_no, street_name, city, State , zipcode) "
          		+ "values('"+patient_id+"','"+apt_no+"','"+street_name+"','"+city+"','"+state+"','"+zipcode+"')");
          
     }
 
     public void observationMenu(String patientId)
     {
         int i=1;
                 Scanner sc= new Scanner(System.in);
 
         try {
             rs = stmt.executeQuery("select distinct type from type_assoc_ill where Illness ='General' or Illness in " +
                 "( SELECT Illness FROM Has_Illness where Patient_Id= '"+ patientId +"')");
         
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
   		boolean flag=false;
   		Scanner sc= new Scanner(System.in);
     try  
    {
     	rs = stmt.executeQuery("select distinct type from type_assoc_ill where Illness ='General' or Illness in ( SELECT Illness FROM Has_Illness where Patient_Id= '"+ patientId +"')");
     	while(rs.next()){
     		if((rs.getString("type").equals(Obs_Type)))
     			flag=true;
     	}
     if(flag==false){
     
     	System.out.println("Type does not exist. Please select the other option or enter the new Type");
     	//api.recordObservation(patientId);
     	return;
     }
     
    // System.out.println("Hello");
     stmt.executeQuery("create or replace TRIGGER MakesObservationInsert\n"+    //This trigger working now finally
 			  " After Insert on Observations\n"+
 			" REFERENCING NEW AS newrow\n"+
 			  "for each row\n"+
 			  " insert into Makes_Observation (pId, OId, datetime_of_record)\n"+
 			 " values ('"+patientId+"',:newrow.OId, CURRENT_TIMESTAMP)\n");
     
     count = stmt.executeQuery("select count(*) as noOfRows from Observations");
 	//  System.out.println("Before parsing : "+counter);
 	  while (count.next()) {
 	   counter= Integer.parseInt(count.getString("noOfRows"));
 	  }
 	  counter+=1;
     	  
 	rs = stmt.executeQuery("select distinct AdditionalInfo from Observation_Type where Type ='"+Obs_Type+"'");
     	 // rs = stmt.executeQuery("select AdditionalInfo from Observation_Type where Type ='Diet' and Illness ='General' or Illness in ( SELECT Illness FROM Has_Illness where Patient_Id= 'ggeorge')");
 	while (rs.next()) {
 	    String AdditionalInfo = rs.getString("AdditionalInfo");
 	  String [] data= AdditionalInfo.split(",");
 	  
 	  while(j!=data.length)
 	  {
 		  System.out.println("Enter "+ data[j]);
 		  Obs_Data = sc.next();
 		  String oid= "O"+counter;
 		  stmt.executeUpdate("Insert into Observations "+
 				  "values ('O"+counter+"','"+Obs_Type+"','"+ obsDate +"','"+ obsTime + "','"+data[j]+"','"+ Obs_Data +"','FALSE')");
 		  rs= stmt.executeQuery("select threshold from threshold_check where type='"+Obs_Type+"' and AdditionalInfo= '"+data[j]+"'");
 		  while(rs.next())
 			  {
 				  if(!(rs.getString("threshold").equals("null")))
 			  {
 				 createTrigger(Obs_Type,data[j],Obs_Data,oid);
 			  }  				 
 			  }
 		  j++;	
 		  counter++;
 	  }
 	}
 	
 	  
 	    // do we need a trigger instead of insert i makes here ??
 	  
 	 //If no trigger how to enter corresponding oid..that would be an issue 
 	  
 	
 	}
 	  catch(Throwable err) {
           conn = null;
           System.out.println("querry nt executed" + err);
           
           }
 	 }
 
 	 public void displayObservations(String patientId) {
 	        int i=0;String typeOfObservation;
 	        Scanner sc= new Scanner(System.in);
 	        try
 	        {
 	        	rs = stmt.executeQuery("select distinct type from observations o,makes_observation m where o.OId=m.OId and m.pId= '"+ patientId +"'");
 	        	 while (rs.next()) {
 	        		 String typeObv = rs.getString("type");
 	                System.out.println(typeObv);
 	                 i++;
 	             }  
 	        	 
 	        	 System.out.println("Enter the Type for which you want to view the observations");
 	        	 typeOfObservation= sc.next();
 	        	 
 	        	rs = stmt.executeQuery("select * from Observations o,makes_observation m where o.oid=m.oid and o.type='"+typeOfObservation+"' and m.pid='"+patientId+"'");  
 
 	            while (rs.next()) {
 	                System.out.println("Selected Type:  " + rs.getString("type"));
 	               // System.out.println(rs.getString("Oid"));
 	                System.out.println(rs.getString("date_of_observation"));
 	                System.out.println(rs.getString("time_of_observation"));
 	                System.out.println(rs.getString("AdditionalInfo"));
 	                System.out.println(rs.getString("threshValue"));
 	                
 	                i++;
 	            }
 	            System.out.println("End of Observation List.");
 	        }
 	        catch(Throwable err) {
 	            conn = null;
 	            System.out.println("Query not executed.\n" + err);
 	            }
 	    }
 
     public String getObservationsByType(String id) {
         String result = "";

         try {
            Statement loc_stmt = conn.createStatement();
             rs = stmt.executeQuery("select distinct type from observations o,makes_observation m where o.OId=m.OId and m.pId= '"+ id +"'");
             while (rs.next()) {
                 String type = rs.getString("type");
                 result += "\n" + "---" + type + "---\n";
                ResultSet rs_inner = loc_stmt.executeQuery("select * from Observations o,makes_observation m where o.oid=m.oid and o.type='"+type+"' and m.pid='"+id+"'" +
                     " ORDER BY o.Date_of_observation, o.time_of_observation");
                 while (rs_inner.next()) {
                     result += "Date of Observation: " + rs_inner.getString("date_of_observation") + "\n";
                     result += "Time of Observation: " + rs_inner.getString("time_of_observation") + "\n";
                     result += rs_inner.getString("AdditionalInfo") + ": " + rs_inner.getString("ThreshValue") + "\n\n";
                 }
             }
            close(loc_stmt);
             System.out.println("End of Observation List.");
         }
         catch (SQLException e) {
         }
         return result;
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
 
     public boolean addNewType(String type,String category,String additionalInformation, String illness)
     {
     	int j=0;
     	Scanner sc=new Scanner(System.in);
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
             	 // System.out.println("length =");
                 rs = stmt.executeQuery("INSERT INTO Observation_Type (Type, Category, AdditionalInfo)" +
                     "VALUES ('" + type + "','" + category + "','" + additionalInformation + "')" );
               rs = stmt.executeQuery("INSERT INTO type_assoc_ill VALUES ('" + illness + "','" + type + "')");
                 String [] data= additionalInformation.split(",");
                // System.out.println("additional info"+additionalInformation);
               //  System.out.println("length ="+data.length);
   			  while(j!=data.length)
   			  {
   			   // System.out.println("Enter Threshold for :"+ data[j]);
   			//  String thresholdValue = sc.next();
   				  stmt.executeQuery("INSERT INTO Threshold_check (type,AdditionalInfo,Threshold)"+
   			        		"values ('"+type+"','"+data[j]+"',null)");   //This insertion is for patients , in case of physician you will have to modify Threshold according to the input
   				 //call the function which creates trigger for each type
   				// System.out.println("inserted in threshold");
   				
   				 // rs= stmt.executeQuery("select threshold from threshold_check where type='"+type+"' and AdditionalInfo= '"+data[j]+"'");
   				  j++;			  
   			  }
             }
         }
         catch(Throwable err) {
             conn = null;
             System .out.println("Error  "+ err);
             flag = false;
         }
         return flag;
     }
     
     public void createTrigger(String type,String additionalInfo,String val,String oid) throws SQLException
     {
     	 System.out.println("entered here");
     	String value="";
     	 rs= stmt.executeQuery("select threshold from threshold_check where type='"+type+"' and additionalInfo='"+additionalInfo+"'");
     	 while(rs.next())
     	 {
     		 value=rs.getString("threshold");
     	 }
     	
     	 System.out.println(value);
     	 
 		try {
 			  rs= stmt.executeQuery("select oid from observations o where type='"+type+"' and additionalInfo='"+additionalInfo+"' and threshValue"+value+" and o.OId='"+oid+"'");
 			if(rs.next())
 			{
 				stmt.executeUpdate("update observations o set o.isactive ='TRUE'where o.OId ='"+rs.getString("oid")+"'");
 				System.out.println("Updated");
 			}
 			/*
 			ResultSet rs1 = stmt.executeQuery("CREATE OR REPLACE TRIGGER test_trigger123 "+
 					 "BEFORE INSERT ON Observations "+
 					 "REFERENCING NEW AS NEW "+
 					 "FOR EACH ROW "+
 					 "BEGIN "+
 					 "select t.threshold into thresh from threshold_check t, observations o where :NEW.additionalInfo=t.additionalInfo; "+
 					 "END; "+
 					 ""+
 					"/");
 			*/
 			
 			// System.out.println("enterede");
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			System.out.println("error "+ e);
 		}
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
     	String query_hf="SELECT DISTINCT patient_name, p.patient_id FROM PATIENT_INFO p, HAS_HF h "
     			+ "WHERE h.hf_id=p.patient_id AND h.patient_id='"+uname+"'"
     			+ "union "
     			+ "SELECT DISTINCT patient_name, p.patient_id FROM PATIENT_INFO p, HAS_HF h "
     			+ "WHERE h.hf_id='"+uname+"' AND h.patient_id=p.patient_id"; 
     	boolean hasHF=true;
     	Statement stmt = conn.createStatement();
     	
     	try {	
     		ResultSet rs_hf = stmt.executeQuery(query_hf);
     		
     		if(!rs_hf.next())
     			hasHF=false;
     		
     		
     		else{
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
     
   //modify query to find alerts for only health friend
     public boolean viewRiskHF(String uname) throws SQLException
     {        
             boolean atRisk=true;
             Statement stmt = conn.createStatement();
             String query_riskHF="select p.patient_name from "
             		+ "observations o, makes_observation m, patient_info p"
             		+ " where o.oid=m.oid and p.patient_id=m.pid "
             		+ "and o.ISACTIVE ='TRUE' AND (sysdate-date_of_record)>7";
         
             ResultSet rs_riskHF = stmt.executeQuery(query_riskHF);
             if(!rs_riskHF.next())
             {
                     System.out.println("\n\nNo health friends at risk currently.\n");
                     atRisk=false;
                     rs_riskHF.close();
             }        
                 
             else{
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
     
  
     public boolean msgRiskHF(String uname, String riskFriend, String text)
     {
    	 		boolean validID=true;
    	 		try{
         //check if the entered id is valid         
    	 		String query_check="select patient_name "
                                        + "from patient_info "
                                        + "where patient_id='"+riskFriend+"'";
    	 		ResultSet rs_check = stmt.executeQuery(query_check);
    	 		if(!rs_check.next())
    	 		{
    	 				System.out.println("Enter a valid ID. Try again.");
    	 				validID=false;
    	 		}
    	 
    	 		else{
    	 				rs=stmt.executeQuery("insert into messages values('"+uname+"','"+riskFriend+"',sysdate,'"+text+"')");
    	 				System.out.println("Messaged sent to "+ riskFriend);
    	 			}
    	 		}
             
             catch(Throwable err) {
                 	conn = null;
                 	System.out.println("querry nt executed" + err);
                 
                 }
    	 return validID;
     }
     
     public void viewAlerts(String id) //* added clause for empty set
  	{
   		int i=1;
          try
          {
         	 rs = stmt.executeQuery("select type from Observations o,makes_observation m where o.oid=m.oid and m.pId='"+id+"' and isactive = 'TRUE'");  
         	/* if(!rs.next())
         		 System.out.println("No Alerts.\n");
    	
         	 else{
         		 rs = stmt.executeQuery("select type from Observations o,makes_observation m where o.oid=m.oid and m.pId='"+id+"' and isactive = 'True'");  */
         
         		 while (rs.next()) {
 		    
         			 System.out.println(i+". Your " + rs.getString("type")+"is in the range of Risk");
         			 i++;
         		 	} 
         		 stmt.executeQuery("update Observations o set o.isactive='False' where  o.isactive = 'True'and exists (select pid from makes_observation m where m.pid='"+id+"' and  o.oid=m.oid)");
         	 	
  
          }
    	  	catch(Throwable err) {
              conn = null;
              System.out.println("querry nt executed" + err);
              }
 
  	}
     
     
     public void viewMessages(String id)
   	{
    		int i=1;
           try
           {
         	  rs = stmt.executeQuery("select p.patient_name, m.text, m.on_date from messages m, patient_info p"
     			+ " where m.from_pId=p.patient_id and m.to_friend='"+id+"'");  
         	//  System.out.println("\tFROM\t\t MESSAGE\t\tDATE");
         	  while (rs.next()) {
         		  System.out.println(i+". " + rs.getString("patient_name")+"\t\t"+rs.getString("text")+"\t\t"+rs.getString("on_date"));
         		  i++;
         	  		}  
           }
     	  catch(Throwable err) {
               conn = null;
               System.out.println("Querry not executed" + err);
               }
     }
     
 
     public void createPatient(String name, String patientID, String password, String address, int age, String sex,String publicStatus) throws SQLException
     {
    	 	String query_hp="INSERT into PATIENT_INFO(PATIENT_NAME, PATIENT_ID, PASSWORD, ADDRESS, AGE, SEX, PUBLICSTATUS) "
    	 		+ "values('"+name+"','"+patientID+"','"+password+"','"+address+"',"+age+",'"+sex+"','"+publicStatus+"')";
 
    	 Statement stmt = conn.createStatement();
 
    	 try {        
    		 stmt.executeQuery(query_hp);
    		 System.out.println("You are now a registered. Login to continue..");
    	 	} catch (SQLException e ) 
    	 		{
    	 			System.out.println("You could not be registered. Please try again..");
    	 		}
    	 }
     
     
     public void createHP(String name, String hpID, String password, String clinic,String description) throws SQLException
     {
    	 String query_hp="INSERT into HP_INFO(NAME, HP_ID, PASSWORD, CLINIC, ROLE) "
     	 		+ "values('"+name+"','"+hpID+"','"+password+"','"+clinic+"','"+description+"')";
 
    	 Statement stmt = conn.createStatement();
 
    	 try {        
    		 stmt.executeQuery(query_hp);
    		 System.out.println("You are now a registered. Login to continue..");
    	 	 } catch (SQLException e ) 
    	 	 	{
    	 		 	System.out.println("You could not be registered. Please try again..");
    	 	 	}
    	 }
     
     
     public boolean checkValidID(String selectedHF) throws SQLException
     {
    	 boolean isValid=true;
    	 String query_check="select patient_name "
                 + "from patient_info "
                 + "where patient_id='"+selectedHF+"'";
    	 ResultSet rs_check = stmt.executeQuery(query_check);
    	 if(!rs_check.next())
    	 {	
    		 isValid=false;
    	 }
    	return isValid;
     }
 
     public String getID(String name)
     {
         String id = "";
         try {
             ResultSet rs = stmt.executeQuery("SELECT Patient_Id FROM Patient_Info WHERE Patient_Name = '" + name + "'");
             if(rs.next())
                 id = rs.getString("Patient_Id");
         }
         catch (SQLException e) {
         }
         return id;
     }
 
     public boolean assignPatientIllness(String name, String illness) {
         try {
             ResultSet rs = stmt.executeQuery("SELECT Patient_Id FROM Patient_Info WHERE Patient_Name = '" + name + "'");
             rs.next();
             String id = rs.getString("Patient_Id");
             rs = stmt.executeQuery("SELECT * FROM Has_Illness WHERE Patient_Id = '" + id + "' AND illness = '" + illness + "'");
             if(rs.next())
                 return true;
             else
                 stmt.executeUpdate("INSERT INTO Has_Illness VALUES ('" + id + "','" + illness + "')");
         }
         catch (SQLException e) {
             return false;
         }
         return true;
     }
 
     public ArrayList<String> getObsTypes() {
         ArrayList<String> types = new ArrayList<String>();
         try {
             ResultSet rs_Types = stmt.executeQuery("SELECT Type FROM Observation_type");
             while(rs_Types.next())
                 types.add(rs_Types.getString("Type"));
         }
         catch (SQLException e) {
         }
         return types;
     }
 
     public ArrayList<String> getPatientsByObsType(String type) {
         ArrayList<String> names = new ArrayList<String>();
         try {
             ResultSet rs = stmt.executeQuery("SELECT Illness FROM type_assoc_ill WHERE Illness = 'General' AND type = '" + type + "'");
             //check if obs type is associated with the general category
             if (rs.next())
                 names = getPNames();
             else {
                 rs = stmt.executeQuery("SELECT P.Patient_name FROM Patient_Info P WHERE P.Patient_Id IN (" +
                     "SELECT DISTINCT H.Patient_Id FROM Has_Illness H WHERE H.Illness IN (" + 
                         "SELECT T.Illness FROM type_assoc_ill T WHERE T.Type = '" + type + "'))");
                 while(rs.next())
                     names.add(rs.getString("Patient_name"));
             }
         }
         catch (SQLException e) {
         }
         return names;
     }
 
     /**
      * Returns patient Info + observations
      */
     public ArrayList<String> getPatientsByName(String name) {
         
         return null;
     }
 
     public ArrayList<String> getPNames() {
         ArrayList<String> names = new ArrayList<String>();
         try {
             ResultSet rs_names = stmt.executeQuery("SELECT Patient_name FROM Patient_Info");
             while(rs_names.next())
                 names.add(rs_names.getString("Patient_name"));
         }
         catch (SQLException e) {
         }
         return names;
     }
 
     public ArrayList<String> getIllNames() {
         ArrayList<String> names = new ArrayList<String>();
         try {
             ResultSet rs_names = stmt.executeQuery("SELECT Illness FROM Illness");
             while(rs_names.next())
                 names.add(rs_names.getString("Illness"));
         }
         catch (SQLException e) {
         }
         return names;
     }
 
     public ArrayList<String> getAdditionalInfoFields(String type) {
         ArrayList<String> infos = new ArrayList<String>();
         try {
             ResultSet rs_infos = stmt.executeQuery("SELECT AdditionalInfo FROM Threshold_Check WHERE Type = '" + type + "'");
             while(rs_infos.next())
                 infos.add(rs_infos.getString("AdditionalInfo"));
         }
         catch (SQLException e) {
         }
         return infos;
     }
 
     public boolean insertThreshold(String type, String info, String threshold) {
         boolean insert = false;
         try {
             stmt.executeQuery("UPDATE Threshold_Check SET Threshold = '" + threshold + "' WHERE Type = '" + 
                 type + "' AND AdditionalInfo = '" + info + "'");
             insert = true;
         }
         catch (SQLException e) {
         }
         return insert;
     }
 }
