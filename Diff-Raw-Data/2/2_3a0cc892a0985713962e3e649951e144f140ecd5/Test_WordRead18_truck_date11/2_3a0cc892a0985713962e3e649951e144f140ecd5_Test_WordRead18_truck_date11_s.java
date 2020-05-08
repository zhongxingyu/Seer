 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package test_word;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  *
  * @author ppatel
  */
 
 public class Test_WordRead18_truck_date11 {
 
     public Test_WordRead18_truck_date11() throws IOException {
             Properties props = new Properties();
             String path = System.getProperty("user.dir")+"/test.properties";
             System.out.println("path is "+path);
             props.load(new FileInputStream(path));
             PreparedStatement stmt = null;
             
             userid = props.getProperty("userId").trim();
             password = props.getProperty("password").trim();
             filename = props.getProperty("fileName1").trim();
             fields = props.getProperty("fields1").trim();
             
             
             System.out.println("user id is "+userid);
             System.out.println("password  is "+password);
             System.out.println("filename   is "+fields);
         
     }
         
     
     ArrayList<String> list = new ArrayList<String>();
     String element= null;
     
     public String userid = null;
     public String password = null;
     public String filename = null;
     public String fields = null;
     
     private int count_batch;
     private Date date;
     private Timestamp timeStampDate1;
     
     DateFormat toformat = new SimpleDateFormat("MM/dd/yy");
     DateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
     
     
     Connection conn = null;
     String sql = null;
     PreparedStatement stmt = null;
   public static void main(String[] args) throws SQLException, IOException {
       
       Test_WordRead18_truck_date11 FirstConvert = new Test_WordRead18_truck_date11();
 
       FirstConvert.CovertFromText();
       
   }
   
   public Connection databaseconnection(String userid, String password) throws SQLException{
         try {
              String DRIVER = "com.ibm.as400.access.AS400JDBCDriver";       
              String URL = "jdbc:as400://72.14.164.60/;naming=system;libraries=OS61LXDTA:OS61LXCUST:LXLIB;transaction\n" +
                          "isolation=none";
              Class.forName(DRIVER);
              conn = DriverManager.getConnection(URL,userid.trim(),password.trim());
              
             
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         }
         return conn;
       
   }
    
     
    
       public void CovertFromText() throws SQLException
       {
       
       
       Scanner sc = null;
       
              
       
       try{
             String fields = "SESSION_DATE,ROUTE_ID,ROUTE_DESCRIPTION,ROUTE_START_DATE,ROUTE_START_TIME,DEPOT_ID,DEPOT_TYPE,STOP_NUMBER,STOP_LOCATION,LOCATION_DESCRIPTION,ADDRESS_LINE1,ADDRESS_LINE2,CITY,PHONE_NUMBER,ZIP_CODE,STATE,LATITUDE,LONGITUDE,OPEN_CLOSE_TIME,TIME_WINDOWS,TRAVEL_TIME,DISTANCE,ARRIVAL_DATE,ARRIVAL_TIME,SERVICE_TIME,TOTAL _OF _SIZE1,TOTAL_ OF_ SIZE2,TOTAL_ OF_ SIZE3,DRIVER1_ID,PREVIOUS_LOCATION,DRIVER1_FIRST_NAME,DRIVER1_MIDDLE_NAME,DRIVER1_LAST_NAME,DRIVER2_ID,DRIVER2_FIRST_NAME,DRIVER2_MIDDLE_NAME,DRIVER2_LAST_NAME,ROUTE_EQUIPMENT_ID,ROUTE_EQUIPMENT_TYPE,ROUTE_EQUIPMENT_OWNER,TRIP_NUMBER,FIXED_SERVIC_ TIME,VARIABLE_SERVICE_TIME,PRE-ROUTE_TIME,STOP_TYPE,POST-ROUTE_TIME,ROUTE_DEPARTURE_TIME,ROUTE_ARRIVAL_TIME,ROUTE_COMPLETE_TIME,INTERNAL_ID";
             Connection conn = null;
             String sql = null;
 //            PreparedStatement stmt = null;
             
             System.out.println("Enter in Try block");
             Properties props = new Properties();
             String path = System.getProperty("user.dir")+"/test.properties";
             System.out.println("path is "+path);
             props.load(new FileInputStream(path));  
             
  
             
             
             
  
             //Connect to iSeries 
             conn = databaseconnection(userid,password);
            
             
             
            
             
             
             String[] dbName = {"zero","LOCATION_ID" ,"ORDER_TYPE","CURRENT_SIZE1","ROUTE_ID","STOP_NUMBER","DC","LW","ORDER_NUMBER","CURRENT_SIZE2","ROUTE_START_DATE","TOTAL_OF_SIZE3","STOP_ARRIVAL_TIME","STOP_ARRIVAL_DATE","STOP_DEPARTURE_TIME" ,"STOP_DEPARTURE_DATE","ROUTE_NAME","ROUTE_COMPLETE_TIME" ,"DRIVER1_ID","DRIVER2_ID","EQUIPMENT1_ID" ,"EQUIPMENT1_OWNER_ID","EQUIPMENT1_DESCRIPTION","DEPOT_ID","DEPOT_TYPE","PREVIOUS_DISTANCE","PREVIOUS_TRAVEL_TIME","STOP_SERVICE_TIME","TOTAL_DISTANCE","TOTAL_TRAVEL_TIME","TOTAL_SERVICE_TIME","OPEN_TIME ","CLOSE_TIME","LOAD_ROUTE_PRIORITY","TIME_WINDOW_OPEN","TIME_WINDOW_CLOSE","SESSION_DATE","UPLOAD_SELECTOR","USER_FIELD1","USER_FIELD2 ","USER_FIELD3","DESCRIPTION ","ADDRESS_LINE1","ADDRESS_LINE2","CITY","STATE","LATITUDE","LONGITUDE","GEOCODE_QUALITY ","ZIP_CODE","PHONE_NUMBER","DRIVER1_FIRST_NAME","DRIVER1_MIDDLE_NAME","DRIVER1_LAST_NAME ","DRIVER2_FIRST_NAME","DRIVER2_MIDDLE_NAME","DRIVER2_LAST_NAME","TRIP_NUMBER","FIXED_SERVICE_TIME","VARIABLE_SERVICE_TIME","ROUTE_DEPARTURE_TIME","ROUTE_ARRIVAL_TIME  ","INTERNAL_ID","CURRENT_URGENCY","ROUTE_START_TIME","EQUIPMENT1_TYPE","DUE_DATE"};
               
             sql = "SELECT " + props.getProperty("fields1").trim() + " from " + props.getProperty("fileName1").trim();
 //            sql = "SELECT * from " + filename.trim();
             System.out.println("sql statement is "+sql);
             stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
  
             ResultSetMetaData metaData = rs.getMetaData();
             int colCount = metaData.getColumnCount();
             System.out.println("column count is "+colCount);
  
             String q = "";
             for (int c = 0; c < colCount; c++) {
                 if(q.equalsIgnoreCase("")){
                     q = "?";
                 }
                 else{
                     q = q + ",?";
                 }
             }
 
             System.out.println("value of q is "+q);
             sql = "INSERT into " +
             props.getProperty("fileName1").trim() +
             " (" + props.getProperty("fields1").trim() + ") VALUES(" + q + ")";
 //            sql = "INSERT into FDELIVERY_MAIN1 (SESSION_DATE ,ROUTE_ID,ROUTE_DESCRIPTION,ROUTE_START_DATE ,ROUTE_START_TIME,DEPOT_ID ,DEPOT_TYPE,STOP_NUMBER ,STOP_LOCATION,ADDRESS_LINE1 ,ADDRESS_LINE2 ,LOCATION_DESCRIPTION ,CITY ,PHONE_NUMBER,  ZIP_CODE ,STATE ,LATITUDE,LONGITUDE ,OPEN_CLOSE_TIME,TIME_WINDOWS ,TRAVEL_TIME ,DISTANCE ,ARRIVAL_DATE,ARRIVAL_TIME,SERVICE_TIME,TOTAL_OF_SIZE1 ,TOTAL_OF_SIZE2,TOTAL_OF_SIZE3,DRIVER1_ID ,PREVIOUS_LOCATION,DRIVER1_FIRST_NAME,DRIVER1_MIDDLE_NAME,DRIVER1_LAST_NAME,DRIVER2_ID,DRIVER2_FIRST_NAME,DRIVER2_MIDDLE_NAME,DRIVER2_LAST_NAME,ROUTE_EQUIPMENT_ID,ROUTE_EQUIPMENT_TYPE,ROUTE_EQUIPMENT_OWNER ,TRIP_NUMBER,FIXED_SERVICE_TIME,VARIABLE_SERVICE_TIME,PRE_ROUTE_TIME,STOP_TYPE ,POST_ROUTE_TIME,ROUTE_DEPARTURE_TIME,ROUTE_ARRIVAL_TIME ,ROUTE_COMPLETE_TIME,INTERNAL_ID) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
             System.out.println(sql);
 //            stmt = conn.prepareStatement(sql);
           
           
           
           
           
                fasdkfjsaklfja;lfkjsal;
           String directory = "C:\\All_file_New\\testfolder\\";
           
           File folder = new File(directory);
           File[] listOfFiles = folder.listFiles();
           
           for(File file : listOfFiles){
               if(file.isFile()){
               String accessPath = directory + file.getName();
               System.out.println(accessPath);
               
           stmt = conn.prepareStatement(sql);
           sc = new Scanner(new BufferedReader(new FileReader(accessPath)));
           int count = 0 ;
           while(sc.hasNextLine()){
             list.clear();
             int columIndex = 0;
             int main_column_index = 0;
                 
             
             String line = sc.nextLine();
             System.out.println(line);
             //line = line.replace('"', ' ');
             Scanner scline = new Scanner(line);
             scline.useDelimiter(" *\\| *");
             count++;
             
             
             while(scline.hasNext())
             {      
 //               scline.next();
                 boolean enter = true; 
                 main_column_index++;
                 
                 if(main_column_index==9 || main_column_index==10 || main_column_index==11 || main_column_index==12 || main_column_index==13 || main_column_index==14 ||  main_column_index==8){
 //                    System.out.println("value of column count is "+main_column_index);
                     scline.next();
                     //enter = false;
                }
                 else if(main_column_index == 17){
                     System.out.println("column count is >>>>>>>>>>>>>>>>"+main_column_index);
                     String value = null;
                     value = scline.next();
                     System.out.println("'"+value.trim()+"'");
                     if (!value.trim().equalsIgnoreCase("stop")){
                     System.out.println("WM layout skipping lines");
                     scline.next();
                     scline.next();
                     scline.next();
                     scline.next();
                     scline.next();
                     scline.next();
                     scline.next();
                     scline.next();
                     scline.next();
                 }
                 }
                else {
                 System.out.println("column count is >>>>>>>>>>>>>>>>"+main_column_index);
                 
                 
                  
 
 //                 System.out.println("Enter value is  "+enter);
                 
 //                if(enter==true){
 //                System.out.println("enter in loop");
                 boolean check_Value = true;
                 String value = null;
                 value = scline.next();
                 
                 if(value.trim().contains("\""))
                 {
                    value =  value.replace('"', '\0');
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+value.trim());
                 }
 //                System.out.print(value+",");
                 columIndex+=1;
                
                 
                 if(value.trim() == null || value.trim().equals("")){
                     
                         check_Value= false;
                 }
 
                 
                 if(main_column_index == 37){
                     System.out.println("index 37" + dbName[columIndex]);
                     System.out.println(metaData.getColumnType(columIndex));
                      
                 }
                 if(main_column_index == 1){
                    if ("\"".equals(value.trim().substring(0, 1))){
                        value = value.trim().substring(1, value.trim().length());
                        System.out.println("value is >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+value);
                    }
                     
                 }
 //                System.out.println("column index value is "+columIndex);
 
 //                System.out.println("COLUME TYPE is "+metaData.getColumnType(columIndex));
                 switch (metaData.getColumnType(columIndex)) {
  
                     case 1: // Char Datatype
 
                             element = null;
                             stmt.setString(columIndex,value.trim());
                             element = "'"+value.trim()+"'";
                             list.add(element);
 //                            System.out.println("char value set");
                             System.out.println("CHAR prepare statement"+columIndex+" " +dbName[columIndex]+ "," +value.trim());
                          
                          break;
  
                     case 4: // integer datatype
                                 element = null;
                                 if(check_Value==false)
                                     value = String.valueOf(0);
                                     
                                 
                             
                                 double temp1 = Double.parseDouble(value.trim());
                                 stmt.setInt(columIndex,((int)temp1));
                                 if(main_column_index==1){
                                 element = "'"+(String.valueOf((int)temp1/100))+"'";
                                 list.add(element);
                                 }
                                 else{
                                     element = "'"+(String.valueOf((int)temp1))+"'";
                                 list.add(element);
                                 }
                                 
 //                                System.out.println("integer value set");
                                 System.out.println("INT prepare statement"+columIndex+" " +dbName[columIndex]+ ","+value.trim());
                        
                         break;
                    
                         
                     case 3:// Decimal dataype
                                 element = null;
                                 if(check_Value==false)
                                     value = String.valueOf(0.0);
                                 BigDecimal d = null;
                                 
 //                               System.out.println("decimal value "+value);
                                     try
                                     {
                                         d = new BigDecimal(value.trim());
                                     }
                                     catch (NumberFormatException e)
                                     {
                                         String makeD = value.trim() + ".0000";
                                         System.out.println(makeD);
                                         d = new BigDecimal(makeD);
                                     }
                                stmt.setBigDecimal(columIndex, d);
                                element = "'"+d+"'";
                                list.add(String.valueOf(d));
                                
 //                               System.out.println("flot value set");
                                System.out.println("DECIMAL prepare statement"+columIndex+" " +dbName[columIndex]+ ","+d);
                                
                         break;
                     
                         
                       
                     case 91: // Date dataype
                              
                                 element = null;
                                 DateFormat fromUser = new SimpleDateFormat("MM-dd-yyyy");
                                 DateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
                                 String reformattedStr = null;
                                 
 //                         if(main_column_index==19){  //
 //                             
 //                             stmt.setNull(columIndex, java.sql.Types.DATE);
 //                         }
 //                         else{
                                 if(main_column_index==15){
                                     String temp =  value.substring(0, 2)+"-"+value.substring(2, 4)+"-"+value.substring(4, 8);
                                     
                                     reformattedStr =  myFormat.format((Date)fromUser.parse(temp.trim()));
                                     element = "'"+reformattedStr+"'";
                                     list.add(element);
 
                                 }
                                 else if (value.trim().isEmpty()){
                                     stmt.setNull(columIndex, java.sql.Types.DATE);
                                     System.out.println("DATE prepare statement"+columIndex+" " +dbName[columIndex]+ ","+value.trim());
                                     element = "'"+value.trim()+"'";
                                     list.add(element);
                                      break;
                                 }
                                 else{
                                     System.out.println(value.trim());
                                     reformattedStr =  myFormat.format((Date)myFormat.parse(value.trim()));
                                     element = "'"+reformattedStr+"'";
                                     list.add(element);
                             }
                         
 
                                 try {
                                     Date final_Date = myFormat.parse(reformattedStr);
 //                                    System.out.println("date datatype value ^^^"+final_Date);
                                     java.sql.Date sqlDate = new java.sql.Date(final_Date.getTime());
 //                                    System.out.println("SQL  date datatype value ^^^ "+sqlDate);
 //                                    System.out.println("DATE prepare statement DATEEEEEEEEEEEEEEEEEEEEE<<<<<<<<<<"+columIndex+","+sqlDate);
                                     stmt.setDate(columIndex, sqlDate);
 //                                    element = "'"+sqlDate+"'";
 //                                    list.add(element);
 //                                    System.out.println("Date value set");
                                     
                                      
                                 } catch (ParseException e) {
                                     System.out.println("DATE EXCEPTION*********************************");
                                     e.printStackTrace();
                                 }
 //                         } 
                                 System.out.println("DATE prepare statement"+columIndex+" " +dbName[columIndex]+ ","+value.trim());
                         break;
                         
                     case 92:  // Time Data type
 
                         String srt_time = null;
 //                        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"+value.trim());
 //                        if(value.trim().length()<=1)
 //                            stmt.setNull(columIndex, java.sql.Types.TIME);
 //                        else{
                         System.out.println("time length : " + value.trim().length());
 
                         if(value.trim().length()>5 && main_column_index!=37 && main_column_index!=39){
                             System.out.println(value.trim().substring(2,3));
                                 if(!value.trim().substring(2,3).equalsIgnoreCase(":")){
                             System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! inside");
 //                            for(int i=1;i<=8;i++){
 //                                  System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! pointer chnage ");
 //                                System.out.println("value: "+scline.next()+"index: "+i);
 //                            }
 //                                value = scline.next();
                                 stmt.setNull(columIndex, java.sql.Types.TIME);
 //                               columIndex++;
                                element = "''";
                                 list.add(element);
                         }
                         }    
                         
                         else if(main_column_index==37 || main_column_index==39)
                         {
                            
                             if(value.trim().length()<1){
 //                                System.out.println("if value is blacnk #########################");// if Time filed is null.
                                  stmt.setTimestamp(columIndex, (Timestamp) convertdate("00:00"));
                                  columIndex++;
                                  stmt.setTimestamp(columIndex, (Timestamp) convertdate("00:00"));
                                 
                             }
                             
                             
                             else if(value.trim().length()==5)  // if only open or close time exist in Time field.
                             {
                                  System.out.println("if open or close time #########################");
                                 
 //                                    if(value.substring(0,5).trim().length() ==5 ){
 //                                        stmt.setTimestamp(columIndex, (Timestamp) convertdate(value.trim().substring(0, 5)));
 //                                    columIndex++;
 //                       
 //                                    }
 //                                    else{
 //                                    stmt.setTimestamp(columIndex, (Timestamp) convertdate("00:00"));
 //                                         columIndex++;
 //                                    }
 //                                    if(value.substring(5,10).trim().length() ==5 ){
 //                                        stmt.setTimestamp(columIndex, (Timestamp) convertdate(value.trim().substring(5, 10)));
 //                                    }
 //                                    else{
 //                                    stmt.setTimestamp(columIndex, (Timestamp) convertdate("00:00"));
 //                                   }
                                  stmt.setTimestamp(columIndex, (Timestamp) convertdate("00:00"));
                                  columIndex++;
                                  stmt.setTimestamp(columIndex, (Timestamp) convertdate(value.trim()));
                             }
                             
                             else{                              // Both open and close time exist.
                             
         //                            System.out.println("###################"+value.trim().substring(0, 5));
         //                             System.out.println("###################"+value.trim().substring(0, 10));
 
                                         stmt.setTimestamp(columIndex, (Timestamp) convertdate(value.trim().substring(0, 5)));
                                     columIndex++;
 
                                         stmt.setTimestamp(columIndex, (Timestamp) convertdate(value.trim().substring(5, 10)));
 
                             }
                         }
                         else if (value.trim().isEmpty()){
                                     stmt.setNull(columIndex, java.sql.Types.DATE);
                                     System.out.println("TIME prepare statement"+columIndex+" " +dbName[columIndex]+ ","+value.trim());
                                     element = "'"+value.trim()+"'";
                                     list.add(element);
                                      break;
                                 }
                         else{                             // simple time filed like i.e (11:05)
                             System.out.println(value.trim());
                             
                             stmt.setTimestamp(columIndex, (Timestamp) convertdate(value.trim()));
                         }
 
                         System.out.println("TIME prepare statement"+columIndex+" " +dbName[columIndex]+ ","+value.trim());
 //                        } 
                          break;    
                         
                     case 12:  // Varchar Data type
 //                            System.out.println("VARCHAR prepare statement"+columIndex+","+value.trim());
                              if(columIndex!=6){
                              element = null;
                              stmt.setString(columIndex,value.trim());
                              element = "'"+value.trim()+"'";
                              list.add(element);
 //                             System.out.println("varchar");
                              }
                              
                              else if(main_column_index==55){
                                  stmt.setNull(columIndex, java.sql.Types.VARCHAR);
                                  element = "''";
                                  list.add(element);
                                  columIndex++;
                                  stmt.setNull(columIndex, java.sql.Types.VARCHAR);
                                  element = "''";
                                 list.add(element);
                              }
                           
                              else{
                                  element = null;
                                  System.out.println("VARCHAR prepare statement<<<<<<<<<<<"+columIndex+" " +dbName[columIndex]+ ","+value.trim().substring(0, 2));
                                  stmt.setString(columIndex,value.trim().substring(0, 2));
                                     element = "'"+value.trim().substring(0, 2)+"'";
                                     list.add(element);
                                  
                                  columIndex++;
                                  
                                  
                                  element = null;
                                  System.out.println("VARCHAR prepare statement<<<<<<<<<<<"+columIndex+" " +dbName[columIndex]+ ","+value.trim().substring(2, 4));
                                  stmt.setString(columIndex,value.trim().substring(2, 4));
                                      element = "'"+value.trim().substring(2, 4)+"'";
                                      list.add(element);
                                  
                                  columIndex++;
                                  
                                  
                                  element = null;
                                  System.out.println("VARCHAR prepare statement<<<<<<<<<<<"+columIndex+" " +dbName[columIndex]+ ","+value.trim().substring(4, 12));
                                  stmt.setString(columIndex,value.trim().substring(4, 12));
                                      element = "'"+value.trim().substring(4, 12)+"'";
                                      list.add(element);
                              }
                            
                              if (main_column_index==69){
                                                                   
 //                                 System.out.println("#####################$$$$$$$$$$$$$$$$%%%%%%%%%%%%%%%%%%%%%%%%########################################################");
                                  
                              columIndex++;
                                
                             String duedate = null;   
                             
                                 
                             if(!list.get(37).trim().equalsIgnoreCase("'c.c'")){
                                 duedate = getsameday().trim();
                                
                            if(duedate.trim().length()>=4){
                                
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################");
                                System.out.println("#############################################################"+duedate);
                                
                                                                                                        
                                                                                                        
                            setdate(duedate,columIndex);
                                    
                                }
                                
                                  
                            else{      
                                      duedate  = getduedate();   // do calculation of due_date 
 
                                   if(duedate.trim().length()>2){
 //                                    System.out.println("Final Arrival Date is######################### "+duedate);
                                     setdate(duedate,columIndex);
                                     
                                     }
 
                                     else{
                                         System.out.println("date is nullllllllllllllllllllllllllllllll");
                                         stmt.setNull(columIndex, java.sql.Types.DATE);
                                     }
 
                                }}
                               else{
 
                                     int date_length = list.get(9).trim().length();
                                     Date  final_date = ((Date)myFormat1.parse(list.get(9).trim().substring(1, (date_length-1))));
 //                                    System.out.println("date datatype value ^^^"+final_date);
                                     java.sql.Date sqlDate = new java.sql.Date(final_date.getTime());
 //                                    System.out.println("SQL  date datatype value ^^^ "+sqlDate);
 
                                     stmt.setDate(columIndex, sqlDate);
 
                               }
                             
                             
                              
                              }
                                
                         System.out.println("VARCHAR prepare statement"+columIndex+" " +dbName[columIndex]+ ","+value.trim());
                             break;
                     
                     
                    
                     default:
                  System.out.println("COULD NOT  prepare statement"+columIndex+" " +dbName[columIndex]+ ","+value.trim());
                     }
 
             
             }
                       
             }                    
                        for(int i = 0; i<list.size();i++)
                        {
                            
                            System.out.println("Indix: "+i+" value: "+list.get(i));
                        }
                           stmt.executeUpdate();
 //                        System.out.println("DONEEEEEEEEEEEE !!!!!!!!!!!!!!!!!!!!!!!!!! >> !!!!!");
 
                     System.out.println("ROW********************************************************************>>"+count);
           }
           
       
       if(stmt!=null)
           
           stmt.close();
       if(rs!=null)
           rs.close();
       }
           file.delete();}
       } 
       
       
       
       catch (FileNotFoundException e) {
             e.printStackTrace();
                  e.getMessage();
         } catch (IOException e) {
             e.printStackTrace();
                  e.getMessage();
         } catch (SQLException e) {
             e.printStackTrace();
                  e.getMessage();
         }
       catch (Exception e) {
             e.printStackTrace();
                  e.getMessage();
         }
      finally{
           if(sc!=null)
               sc.close();
          
       }
   }
 
     public Timestamp convertdate(String value) throws ParseException,SQLException
       {    
           element = null;
           DateFormat formatter = new SimpleDateFormat("HH:mm");
                              String str_time = null;
                              str_time=value.trim();
 //                             System.out.println("before formatting Date"+str_time);
                              
                              element = "'"+str_time+":00"+"'";
                              list.add(element);
 //                             System.out.println(str_time.trim());
                              date = formatter.parse(str_time.trim()); 
                              timeStampDate1 = new Timestamp(date.getTime());
 //                             System.out.println("Time stamp object is "+timeStampDate1);
 
                              
           return timeStampDate1;
       
       }
     
     private void setdate(String duedate, int columIndex) throws ParseException {
        
         String  reformattedStr=null;
         reformattedStr = myFormat1.format((Date)toformat.parse(duedate.trim()));
                                     element = "'"+reformattedStr+"'";
                                     list.add(reformattedStr);
                                      try {
                                         Date final_date = myFormat1.parse(reformattedStr);
 //                                        System.out.println("date datatype value ^^^"+final_date);
                                         java.sql.Date sqlDate = new java.sql.Date(final_date.getTime());
 
 //                                        System.out.println("SQL  date datatype value ^^^ "+sqlDate);
 
                                         stmt.setDate(columIndex, sqlDate);
 
 //                                        System.out.println("Date value set");
 //                                        System.out.println("DATE prepare statement DATEEEEEEEEEEEEEEEEEEEEE >>>>>>"+columIndex+","+sqlDate);
 
                                     } catch (SQLException e) {
                                         System.out.println("DATE EXCEPTION*********************************");
                                         e.printStackTrace();
                                     }
     
     
     
     
     }
 
     private String getsameday() throws SQLException{
         
     ResultSet rs = null;
     Connection conn = null;
     PreparedStatement stmt = null;    
         
      conn = databaseconnection(userid, password);
 
         
             System.out.println("date "+list.get(9));
        System.out.println("store no "+list.get(0));
        System.out.println("open time "+list.get(33));
        System.out.println("close "+list.get(34));
 
                  System.out.println("select del_date,store_no\n" +
                                     "from lxlib.fstore_schedule \n" +
                                     "join lxlib.commodity_def \n" +
                                     "on commodity_def.dc = " + list.get(5) + " \n" +
                                     "and commodity_def.lw = " + list.get(6) + " \n" +
                                     "where truck_date = "+list.get(9)+"\n" +
                                     "\n" +
                                     "and  store_no = "+list.get(0)+"\n" +
                                     "and  fstore_schedule.commodity = commodity_def.commodity ");
  
         
                 stmt = conn.prepareStatement("select del_date,store_no\n" +
                                     "from lxlib.fstore_schedule \n" +
                                     "join lxlib.commodity_def \n" +
                                     "on commodity_def.dc = " + list.get(5) + " \n" +
                                     "and commodity_def.lw = " + list.get(6) + " \n" +
                                     "where truck_date = "+list.get(9)+"\n" +
                                     "\n" +
                                     "and  store_no = "+list.get(0)+"\n" +
                                     "and  fstore_schedule.commodity = commodity_def.commodity ");
 
     
       rs = stmt.executeQuery();
       String date = null;
                                                 
                                                 
         if(rs.next()){
         System.out.println("data is "+rs.getString("del_date"));
         System.out.println("data is "+rs.getString("store_no"));
         date = rs.getString(1);
         }
         else
             date = "no";
         conn.close();
         return date;
     }
     
  
     private String getduedate() {
             ResultSet rs = null;
             ResultSet rs1 = null;
             Connection conn = null;
             PreparedStatement stmt = null;   
             String value = null;
             String value1 = null;
         try {
             DateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
             
             
             conn = databaseconnection(userid, password);
            
             stmt = conn.prepareStatement("select \n" +
                                         "date("+list.get(9)+") + (case when truckdweek.weekdayno > deldweek.weekdayno\n" +
                                         "then \n" +
                                         "	(case when sched.del_week_name = 'WEEK1'\n" +
                                         "	then (7 - (int(truckdweek.weekdayno)) + int(deldweek.weekdayno))\n" +
                                         "	when sched.del_week_name = 'WEEK2'\n" +
                                         "	then (7 - (int(truckdweek.weekdayno))+ int(deldweek.weekdayno) +7)\n" +
                                         "	when sched.del_week_name = 'WEEK3'\n" +
                                         "	then (7 - (int(truckdweek.weekdayno))+ int(deldweek.weekdayno)+14) end)\n" +
                                         "\n" +
                                         "when truckdweek.weekdayno <= deldweek.weekdayno\n" +
                                         "	then (case when sched.del_week_name = 'WEEK1'\n" +
                                         "	then (int(deldweek.weekdayno)   - int(truckdweek.weekdayno))\n" +
                                         "	when sched.del_week_name = 'WEEK2'\n" +
                                         "	then (int(deldweek.weekdayno)   +7 - int(truckdweek.weekdayno))\n" +
                                         "	when sched.del_week_name = 'WEEK3'\n" +
                                         "	then (int(deldweek.weekdayno)   +14 - int(truckdweek.weekdayno)) end)end) days as final_arrive_date \n" +
                                         "\n" +
                                         "from fstore_schedule1 as sched, commodity_def as commo, week_day as truckdweek, week_day as deldweek\n" +
                                         "\n" +
                                         "where sched.store_no = "+list.get(0)+"\n" +
                                         "and substr(upper(dayname("+list.get(9)+")),1,3) = sched.truck_day\n" +
                                         "and  sched.del_time_from = "+list.get(33)+"\n" +
                                         "and  sched.del_time_to = "+list.get(34)+"\n" +
                                         "and commo.dc =  "+list.get(5)+"\n" +
                                         "and commo.lw = "+list.get(6)+"\n" +
                                         "and commo.commodity = sched.commodity\n" +
                                         "and sched.truck_day =  truckdweek.weekday\n" +
                                         "and sched.del_day = deldweek.weekday, count_batch, count_batch",ResultSet.TYPE_SCROLL_INSENSITIVE,
                   ResultSet.CONCUR_UPDATABLE);
 
 //            System.out.println("Driver load succussfully for getduedate method");
 //            
 //            Connection conn2 = DriverManager.getConnection("jdbc:as400://72.14.164.60/;"
 //                                                             + "naming=system;libraries=OS61LXDTA:OS61LXCUST:LXLIB;transaction\n" +
 //                                                              "isolation=none","ppatel","PPATEL1");
 //            
 //            Statement statement = conn2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 //                  ResultSet.CONCUR_UPDATABLE);
             
             
        System.out.println("date "+list.get(9));
        System.out.println("store no "+list.get(0));
        System.out.println("open time "+list.get(33));
        System.out.println("close "+list.get(34));
        System.out.println("dccccc "+list.get(5));
        System.out.println("lw "+list.get(6));
        for(int i = 0; i<list.size();i++)
        {
            System.out.println("Indix: "+i+" value: "+list.get(i));
        }
        
 
         rs = stmt.executeQuery();
          
         
 
              if (rs.first()) {
                   
 //                  System.out.println("inside record in while loops ");
                   value = rs.getString("final_arrive_date");
 
              }
              else{
                  
                   stmt = conn.prepareStatement("select \n" +
                                                 "date("+list.get(9)+") + (case when truckdweek.weekdayno > deldweek.weekdayno\n" +
                                                 "then \n" +
                                                 "	(case when sched.del_week_name = 'WEEK1'\n" +
                                                 "	then (7 - (int(truckdweek.weekdayno)) + int(deldweek.weekdayno))\n" +
                                                 "	when sched.del_week_name = 'WEEK2'\n" +
                                                 "	then (7 - (int(truckdweek.weekdayno))+ int(deldweek.weekdayno) +7)\n" +
                                                 "	when sched.del_week_name = 'WEEK3'\n" +
                                                 "	then (7 - (int(truckdweek.weekdayno))+ int(deldweek.weekdayno)+14) end)\n" +
                                                 "\n" +
                                                 "when truckdweek.weekdayno <= deldweek.weekdayno\n" +
                                                 "	then (case when sched.del_week_name = 'WEEK1'\n" +
                                                 "	then (int(deldweek.weekdayno)   - int(truckdweek.weekdayno))\n" +
                                                 "	when sched.del_week_name = 'WEEK2'\n" +
                                                 "	then (int(deldweek.weekdayno)   +7 - int(truckdweek.weekdayno))\n" +
                                                 "	when sched.del_week_name = 'WEEK3'\n" +
                                                 "	then (int(deldweek.weekdayno)   +14 - int(truckdweek.weekdayno)) end)end) days as final_arrive_date \n" +
                                                 "\n" +
                                                 "from fstore_schedule1 as sched, commodity_def as commo, week_day as truckdweek, week_day as deldweek\n" +
                                                 "\n" +
                                                 "where sched.store_no = "+list.get(0)+"\n" +
                                                 "and substr(upper(dayname("+list.get(9)+")),1,3) = sched.truck_day\n" +
                                                 "and  sched.del_time_from = "+list.get(33)+"\n" +
                                                 "and  sched.del_time_to = "+list.get(34)+"\n" +
                                                 "and commo.dc =  "+list.get(5)+"\n" +
                                                 "and commo.lw = "+list.get(6)+"\n" +
                                                 "and commo.commodity = sched.commodity\n" +
                                                 "and sched.truck_day =  truckdweek.weekday\n" +
                                                 "and sched.del_day = deldweek.weekday, count_batch, count_batch",ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                 ResultSet.CONCUR_UPDATABLE);
                  rs1 = null; 
 //                 System.out.println("inside else loop");
                  rs1 = stmt.executeQuery();
                  
                  
 //                 System.out.println("select \n" +
 //                                        "date("+list.get(10)+") + (case when truckdweek.weekdayno > deldweek.weekdayno\n" +
 //                                        "then \n" +
 //                                        "	(case when sched.del_week_name = 'WEEK1'\n" +
 //                                        "	then (7 - (int(truckdweek.weekdayno)) + int(deldweek.weekdayno))\n" +
 //                                        "	when sched.del_week_name = 'WEEK2'\n" +
 //                                        "	then (7 - (int(truckdweek.weekdayno))+ int(deldweek.weekdayno) +7)\n" +
 //                                        "	when sched.del_week_name = 'WEEK3'\n" +
 //                                        "	then (7 - (int(truckdweek.weekdayno))+ int(deldweek.weekdayno)+14) end)\n" +
 //                                        "\n" +
 //                                        "when truckdweek.weekdayno <= deldweek.weekdayno\n" +
 //                                        "	then (case when sched.del_week_name = 'WEEK1'\n" +
 //                                        "	then (int(deldweek.weekdayno)   - int(truckdweek.weekdayno))\n" +
 //                                        "	when sched.del_week_name = 'WEEK2'\n" +
 //                                        "	then (int(deldweek.weekdayno)   +7 - int(truckdweek.weekdayno))\n" +
 //                                        "	when sched.del_week_name = 'WEEK3'\n" +
 //                                        "	then (int(deldweek.weekdayno)   +14 - int(truckdweek.weekdayno)) end)end) days as final_arrive_date \n" +
 //                                        "\n" +
 //                                        "from fstore_schedule1 as sched, commodity_def as commo, week_day as truckdweek, week_day as deldweek\n" +
 //                                        "\n" +
 //                                        "where sched.store_no = "+list.get(0)+"\n" +
 //                                        "and substr(upper(dayname("+list.get(10)+")),1,3) = sched.truck_day\n" +
 //                                       "and commo.dc =  "+list.get(5)+"\n" +
 //                                        "and commo.lw = "+list.get(6)+"\n" +
 //                                        "and commo.commodity = sched.commodity\n" +
 //                                        "and sched.truck_day =  truckdweek.weekday\n" +
 //                                        "and sched.del_day = deldweek.weekday");
 //                                            
                  
                                                
                                                 
                                                 if(rs1.first()){
 //                                                    System.out.println("insdie second loop");
 //                                                    System.out.println("value is @@@@@@@@@@@"+rs1.getString("final_arrive_date"));
                                                      value = rs1.getString("final_arrive_date");
 //                                                     System.out.println(value);
                                                  }
                                                 else{
 //                                                    System.out.println("no value ");
                                                     value = "";
                                                 }
 //                                                if(rs1.wasNull())
 //                                                    System.out.println("result set is null ");
                                            
 //                                          System.out.println("value is $$$$$$$$$$$$$$$$"+value.trim());     
       
                                             
              }              
 //                                                 if (rs!=null){
 //                                                  System.out.println("inside record in second while loops ");
                                                
                                               
                    
               
                                              
                   }
 
                  
          catch (Exception ex) {
             ex.printStackTrace();
         }
         
             if(!value.trim().equalsIgnoreCase("") || value.trim()!=null)
                 return value.trim(); 
             else
                 return value1.trim();
             
             
       }
 
     
 }
 
     
 
