 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cars;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.*;
 import oracle.jdbc.driver.*;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Timer;
 import javax.swing.*;
 
 /**
  *
  * @author Greg Mathews
  */
 public class Cars {
     static String dbConnectString = "jdbc:oracle:thin:@localhost:1521:orcl2";
     static String dbUserName = "scott";
     static String dbPassword = "tiger";
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
         
         String[] makes = {"Select a Make","BUICK","CADILLAC","CHEVROLET","CHRYSLER","FORD","CHEVROLET & GMC TRUCK & VAN","INTERNATIONAL TRUCK (I.H.C.)","MAZDA","OLDSMOBILE","PORSCHE","RENAULT","SAAB","SUBARU","TOYOTA","UPS","VOLKSWAGEN"};
 
                 Class.forName("oracle.jdbc.driver.OracleDriver");
              // Load the Oracle JDBC driver
              DriverManager.registerDriver
                      (new oracle.jdbc.driver.OracleDriver());
         
              // connect through driver
              Connection conn = DriverManager.getConnection
                      //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                      //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                      (dbConnectString, dbUserName, dbPassword);
              // Create Oracle DatabaseMetaData object
              DatabaseMetaData meta = conn.getMetaData();
         
              // gets driver info:
             System.out.println("JDBC driver version is " + meta.getDriverVersion());
         
              // Create a statement
              Statement stmt = conn.createStatement();
         
              // Do the SQL "Hello World" thing
              final ResultSet rset = stmt.executeQuery("SELECT TABLE_NAME FROM USER_TABLES");
         
         
         
         
         
         // TODO code application logic here
         final CarUI test = new CarUI();
         final JComboBox box1 = test.getComboBox1();
         final JComboBox boxMake = test.getComboBoxMake();
         final JTextArea text = test.getTextArea();
         final JTextArea text2 = test.getTextArea2();
         box1.removeAllItems();
         boxMake.removeAllItems();
         final JComboBox box2 = test.getComboBox2();
         final JComboBox box3 = test.getComboBox3();
         final JComboBox box4 = test.getComboBox4();
         final JTextField textField1 = test.getJText1();
         final JTextField textField2 = test.getJText2();
         final JTextField textField3 = test.getJText3();
         final JTextField textField4 = test.getJText4();
         final JTextField textField5 = test.getJText5();
         final JTextField textField6 = test.getJText6();
         final JTextField textField7 = test.getJText7();
         final JTextField textField8 = test.getJText8();
         final JTextField textField9 = test.getJText9();
         final JTextField textField10 = test.getJText10();
         final JTextField textField11 = test.getJText11();
         final JTextField textField12 = test.getJText12();
         final JTextField textField13 = test.getJText13();
         final JTextField textField14 = test.getJText14();
         final JTextField textField15 = test.getJText15();
         final JTextField textField16 = test.getJText16();
         final JTextField textField17 = test.getJText17();
         
         final JButton newCar = test.getJButton1();
         final JButton newPart = test.getJButton2();
         final JButton deleteCar = test.getJButton3();
         final JButton deletePart = test.getJButton4();
         
         // delete the selected car
         deleteCar.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 String makeName = (String)box1.getSelectedItem();
                 String makeAbbr = test.selectedMake(makeName);
                 
                 try{
                     Connection conn = DriverManager.getConnection
                      //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                      //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                     (dbConnectString, dbUserName, dbPassword);
                     Statement stmt = conn.createStatement();
                     System.out.println("Delete from APL"+makeAbbr+" where model ='"+box2.getSelectedItem()+"' and year="+box3.getSelectedItem()+" and rlink = "+box4.getSelectedItem());
                     ResultSet testSet = stmt.executeQuery("Delete from APL"+makeAbbr+" where model = '"+box2.getSelectedItem()+"' and year = "+box3.getSelectedItem()+" and rlink = "+box4.getSelectedItem());
                     
                     testSet.close();
                     stmt.close();
                     conn.close();
                     box1.setSelectedItem("Select a Make");
                     box2.removeAllItems();
                     box3.removeAllItems();
                     box4.removeAllItems();
                 }
                 catch(SQLException exep){
                    exep.printStackTrace();
                 }
             }
         });
         // delete the selected part
         deletePart.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 String makeName = (String)box1.getSelectedItem();
                 String makeAbbr = test.selectedMake(makeName);
                 
                 try{
                     Connection conn = DriverManager.getConnection
                      //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                      //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                     (dbConnectString, dbUserName, dbPassword);
                     Statement stmt = conn.createStatement();
                     String rlink = (String)box4.getSelectedItem();
                     System.out.println("delete from rdimmod where P_Number in (select MOD4 from radcrx where rlink=" + rlink+ ")");
                     ResultSet testSet = stmt.executeQuery("delete from rdimmod where P_Number in (select MOD4 from radcrx where rlink=" + rlink+ ")");
                     
                     testSet.close();
                     stmt.close();
                     conn.close();
                     text2.setText("");
                 }
                 catch(SQLException exep){
                    exep.printStackTrace();
                 }
             }
         });
         
         // code to create a new part
         newPart.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 String rlink = (String)box4.getSelectedItem();
                 String pnum, core, inhead, outhead, incon, oucon, tmount, oilcool, price, amount;
                 if(textField8.getText().equals("")){
                     pnum = "NA";
                 }
                 else{
                     pnum = textField8.getText();
                 }
                 if(textField9.getText().equals("")){
                     core = "NA";
                 }
                 else{
                     core = textField9.getText();
                 }
                 if(textField10.getText().equals("")){
                     inhead = "NA";
                 }
                 else{
                     inhead = textField10.getText();
                 }
                 if(textField11.getText().equals("")){
                     outhead = "NA";
                 }
                 else{
                     outhead = textField11.getText();
                 }
                 if(textField12.getText().equals("")){
                    incon = "NA";
                 }
                 else{
                     incon = textField12.getText();
                 }
                 if(textField13.getText().equals("")){
                     oucon = "NA";
                 }
                 else{
                     oucon = textField13.getText();
                 }
                 if(textField14.getText().equals("")){
                     tmount = "NA";
                 }
                 else{
                     tmount = textField14.getText();
                 }
                 if(textField15.getText().equals("")){
                     oilcool = "NA";
                 }
                 else{
                     oilcool = textField15.getText();
                 }
                 if(textField16.getText().equals("")){
                     price = "0";
                 }
                 else{
                     price = textField16.getText();
                 }
                 if(textField17.getText().equals("")){
                     amount = "0";
                 }
                 else{
                     amount = textField17.getText();
                 }
                 try{
                     Connection conn = DriverManager.getConnection
                      //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                      //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                     (dbConnectString, dbUserName, dbPassword);
                     Statement stmt = conn.createStatement();
                     System.out.println("INSERT INTO rdimmod VALUES ('"+pnum+"','"+core+"','"+inhead+"','"+outhead+"','"+incon+"','"+oucon+"','"+tmount+"','"+oilcool+"',"+price+", "+amount+")");
                     ResultSet testSet = stmt.executeQuery("INSERT INTO rdimmod VALUES ('"+pnum+"','"+core+"','"+inhead+"','"+outhead+"','"+incon+"','"+oucon+"','"+tmount+"','"+oilcool+"',"+price+", "+amount+")");
                     testSet.close();
                     stmt.close();
                     conn.close();
                     newPart.setText("Inserted");
                     textField8.setText("");
                     textField9.setText("");
                     textField10.setText("");
                     textField11.setText("");
                     textField12.setText("");
                     textField13.setText("");
                     textField14.setText("");
                     textField15.setText("");
                     textField16.setText("");
                     textField17.setText("");
                     String all_Data = "PNum: " + pnum + 
                                     " \nCore: " + core+
                                     " \nINHEAD: "+inhead+
                                     " \nOUTHEAD: "+outhead+
                                     " \nINCON: "+incon+
                                     " \nOUCON: "+oucon+
                                     " \nTMOUNT: "+tmount+
                                     " \nOILCOOL: "+oilcool+
                                     " \nPRICE: "+price+
                                     " \nAMOUNT: "+amount+"\n";
                     text2.setText(all_Data);
                     Timer time = new Timer();
                     (new Thread(new Runnable(){
                         public void run(){
                             newPart.setEnabled(false);
                             try{
                                 Thread.sleep(2000);
                             }
                             catch(InterruptedException exep){
                                 exep.printStackTrace();
                             }
                             newPart.setText("Add Part");
                             newPart.setEnabled(true);
                         }
                     })).start();
                 }
                 catch(SQLException exep){
                    exep.printStackTrace();
                 }
                 
                 
             }
         });
         
         
         // code to create a new car
         newCar.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 String model;
                 String description;
                 String year;
                 String liter;
                 String rlink;
                 String engine;
                 String cubic;
                 String makeName = (String)boxMake.getSelectedItem();
                 String makeAbbr = test.selectedMake(makeName);
                 if(textField1.getText().equals("")){
                     model = "NA";
                 }
                 else{
                     model = textField2.getText();
                 }
                 if(textField1.getText().equals("")){
                     description = "NA";
                 }
                 else{
                     description = textField1.getText();
                 }
                 if(textField3.getText().equals("")){
                     year = "12";
                 }
                 else{
                     year = textField3.getText();
                 }
                 if(textField5.getText().equals("")){
                     liter = "NA";
                 }
                 else{
                     liter = textField5.getText();
                 }
                 if(textField4.getText().equals("")){
                    rlink = "NA";
                 }
                 else{
                     rlink = textField4.getText();
                 }
                 if(textField6.getText().equals("")){
                     engine = "NA";
                 }
                 else{
                     engine = textField6.getText();
                 }
                 if(textField7.getText().equals("")){
                     cubic = "0";
                 }
                 else{
                     cubic = textField7.getText();
                 }
 
                 try{
                     Connection conn = DriverManager.getConnection
                     //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                     //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                     (dbConnectString, dbUserName, dbPassword);
                     Statement stmt = conn.createStatement();
                     System.out.println("INSERT INTO APL"+makeAbbr+" VALUES ('"+model+"', "+year+", '"+description+"', '"+liter+"', '"+engine+"', "+cubic+", "+rlink+")");
                     ResultSet testSet = stmt.executeQuery("INSERT INTO APL"+makeAbbr+" VALUES ('"+model+"', "+year+", '"+description+"', '"+liter+"', '"+engine+"', "+cubic+", "+rlink+")");
                     testSet.close();
                     stmt.close();
                     conn.close();
                     newCar.setText("Inserted");
                     textField1.setText("");
                     textField2.setText("");
                     textField3.setText("");
                     textField4.setText("");
                     textField5.setText("");
                     textField6.setText("");
                     textField7.setText("");
                     Timer time = new Timer();
                     (new Thread(new Runnable(){
                         public void run(){
                             newCar.setEnabled(false);
                             try{
                                 Thread.sleep(2000);
                             }
                             catch(InterruptedException exep){
                                 exep.printStackTrace();
                             }
                             newCar.setText("Create New Car");
                             newCar.setEnabled(true);
                         }
                     })).start();
                     
                 }
                 catch(SQLException exep){
                    exep.printStackTrace();
                 }
             }
         });
         
         // Add all the makes to the combobox
         int i = 0;
         while(i < makes.length){
             box1.addItem(makes[i]);
             boxMake.addItem(makes[i]);
             i++;
         }
         // Listen for a change on this combo box
         box1.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 JComboBox cb = (JComboBox)e.getSource();
                 String makeName = (String)cb.getSelectedItem();
                 String makeAbbr = test.selectedMake(makeName);
                 
                 try{
                     Connection conn = DriverManager.getConnection
                      //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                      //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                     (dbConnectString, dbUserName, dbPassword);
                     Statement stmt = conn.createStatement();
                     ResultSet testSet = stmt.executeQuery("SELECT DISTINCT MODEL FROM APL"+makeAbbr);
                     box2.removeAllItems();
                     while (testSet.next())
                         box2.addItem(testSet.getString(1));
                     testSet.close();
                     stmt.close();
                     conn.close();
                 }
                 catch(SQLException exep){
                    exep.printStackTrace();
                 }
                 
                 
             }
         });
         box2.removeAllItems();
         box2.addItem("Select a Model".toString());
         box2.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 String modelName = (String)box2.getSelectedItem();
                 String makeName = (String)box1.getSelectedItem();
                 String makeAbbr = test.selectedMake(makeName);
                 System.out.println(modelName);
                 if(modelName != null){
                     System.out.println("SELECT YEAR FROM APL"+makeAbbr+" WHERE model = '"+modelName+"'");
                     try{
                         Connection conn = DriverManager.getConnection
                         //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                         //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                        (dbConnectString, dbUserName, dbPassword);
                         Statement stmt = conn.createStatement();
                         System.out.println(makeName);
                         ResultSet testSet = stmt.executeQuery("SELECT DISTINCT YEAR FROM APL"+makeAbbr+" WHERE model = '"+modelName+"'");
                         box3.removeAllItems();
                         while (testSet.next())
                             box3.addItem(testSet.getString(1));
                         testSet.close();
                         stmt.close();
                         conn.close();
                     }
                     catch(SQLException exep){
                        exep.printStackTrace();
                     }
                 }
             }
         });
         box3.removeAllItems();
         box3.addItem("Select a Year".toString());
         box3.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 String modelName = (String)box2.getSelectedItem();
                 String makeName = (String)box1.getSelectedItem();
                 String makeAbbr = test.selectedMake(makeName);
                 String modelYear = (String)box3.getSelectedItem();
                 System.out.println(modelName);
                 
                 if(modelYear != null){
                     System.out.println("SELECT * FROM APL"+makeAbbr+" WHERE model = '"+modelName+"' AND year = "+modelYear);
                     try{
                         Connection conn = DriverManager.getConnection
                         //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                         //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                         (dbConnectString, dbUserName, dbPassword);
                         Statement stmt = conn.createStatement();
                         System.out.println(makeName);
                         ResultSet testSet = stmt.executeQuery("SELECT * FROM APL"+makeAbbr+" WHERE model = '"+modelName+"' AND year = "+modelYear);
                         String all_Data="";
                         box4.removeAllItems();
                         while (testSet.next()){
                             all_Data = all_Data + "Model: " + testSet.getString(1) + 
                                     " \nYear: " + testSet.getString(2)+
                                     " \nDescription: "+testSet.getString(3)+
                                     " \nLITER: "+testSet.getString(4)+
                                     " \nENG: "+testSet.getString(5)+
                                     " \nCUBIC: "+testSet.getString(6)+
                                     " \nRLINK: "+testSet.getString(7)+"\n";
                                    box4.addItem(testSet.getString(7));
                         }
                         text.setText(all_Data);
                         testSet.close();
                         stmt.close();
                         conn.close();
                     }
                     catch(SQLException exep){
                        exep.printStackTrace();
                     }
                 }
             }
         });
         
         box4.removeAllItems();
         box4.addItem("Select a RLINK".toString());
         box4.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){              
                
                 String rlink = (String)box4.getSelectedItem();
                   
                     try{
                         Connection conn = DriverManager.getConnection
                          //("jdbc:oracle:thin:@localhost:1521:ORCL","system","admin");
                          //("jdbc:oracle:thin:@localhost:1521:orcl", "scott", "tiger");
                         (dbConnectString, dbUserName, dbPassword);
                         Statement stmt = conn.createStatement();
                         ResultSet testSet = stmt.executeQuery("select * from rdimmod where P_Number in (select MOD4 from radcrx where rlink=" + rlink+ ")");
                         String all_Data="";
                         while (testSet.next()){
                             all_Data = all_Data + "PNum: " + testSet.getString(1) + 
                                     " \nCore: " + testSet.getString(2)+
                                     " \nINHEAD: "+testSet.getString(3)+
                                     " \nOUTHEAD: "+testSet.getString(4)+
                                     " \nINCON: "+testSet.getString(5)+
                                     " \nOUCON: "+testSet.getString(6)+
                                     " \nTMOUNT: "+testSet.getString(7)+
                                     " \nOILCOOL: "+testSet.getString(8)+
                                     " \nPRICE: "+testSet.getString(9)+
                                     " \nAMOUNT: "+testSet.getString(10)+"\n";
                                             
                         }
                         
                         text2.setText(all_Data);
                         testSet.close();
                         stmt.close();
                         conn.close();
                     }
                     catch(SQLException exep){
                        exep.printStackTrace();
                     }
                 }
             
         });
         
         test.setVisible(true);
 
         
 //             while (rset.next())
 //                System.out.println(rset.getString(1));
         
              // close the result set, the statement and disconnect
              rset.close();
              stmt.close();
              conn.close();
         
         
         
     }
     public void actionPerformed(ActionEvent e){
             JComboBox cb = (JComboBox)e.getSource();
             String makeName = (String)cb.getSelectedItem();
             System.out.println("pressed");
         }
 }
