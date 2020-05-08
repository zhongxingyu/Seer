 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.app.test.manual.db;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.jbundle.app.test.test.db.TestTable;
 import org.jbundle.base.field.BaseField;
 import org.jbundle.base.field.DateTimeField;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.RecordOwner;
 import org.jbundle.base.thread.BaseProcess;
 import org.jbundle.base.util.Environment;
 import org.jbundle.base.util.MainApplication;
 import org.jbundle.model.DBException;
 import org.jbundle.model.Task;
 import org.jbundle.model.util.Util;
import org.jbundle.test.manual.TestAll;
 import org.jbundle.thin.base.thread.AutoTask;
 import org.jbundle.thin.base.util.Application;
 
 public class DbTest {
 
     public DbTest() {
 
     }
 
     static String[] defaultargs = { "databaseproduct=derby", "remote=Jdbc", "local=Jdbc", "table=Jdbc", "jmsserver=true" };
 
     public static final void main(String[] args) {
         DbTest pwd = new DbTest();
         if ((args == null) || (args.length == 0))
             args = defaultargs;
         pwd.run(args);
     }
 
     public void run(String[] args) {
        args = TestAll.fixArgs(args);
         Map<String, Object> properties = new Hashtable<String, Object>();
         Util.parseArgs(properties, args);
         Environment env = new Environment(properties);
         Application app = new MainApplication(env, properties, null);
         Task task = new AutoTask(app, null, null);
         RecordOwner recordOwner = new BaseProcess(task, null, null);
 
         while (true) {
             DataInputStream in = new DataInputStream(System.in);
             int count = 0;
             System.out.print("enter a repeat count: ");
             try {
                 count = Integer.parseInt(in.readLine());
             } catch (NumberFormatException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             if (count == 0)
                 break;
 
             long start = System.currentTimeMillis();
             System.out.print("running count: " + count);
             for (int i = 0; i < count; i++)
             {
                 TestTable testTable = new TestTable(recordOwner);
 
                 this.addTestTableRecords(testTable);
                 
                 testTable.free();
             }
             System.out.println("Time: " + (System.currentTimeMillis() - start));
             System.gc();
         }
 
         System.exit(0);
     }
     /**
      * Add the test table records.
      */
     public void readTestTableRecords(TestTable testTable)
     {
         try   {
             int iCount = 0;
             testTable.open();           // Open the table
  
             testTable.setKeyArea(DBConstants.MAIN_KEY_AREA);
             iCount = 0;
             testTable.close();
             while (testTable.hasNext())
             {
                 testTable.next();
                 iCount++;
             }
 //            System.out.println("Count: " + iCount);
         } catch (DBException e)   {
         }
     }
     Date date = null;
     /**
      * Add the test table records.
      */
     public void addTestTableRecords(TestTable testTable)
     {
         boolean bSuccess = false;
         int iCount = 0;
         try   {
             testTable.open();           // Open the table
         } catch (DBException e)   {
             String strError = e.getMessage();
         }
 
         try   {     
             testTable.setKeyArea(DBConstants.MAIN_KEY_AREA);
             iCount = 0;
             testTable.close();
             while (testTable.hasNext())
             {
                 testTable.next();
                 iCount++;
             }
         } catch (DBException e)   {
         }
             
         iCount = 0;
         try   {
             testTable.close();
             while (testTable.hasNext())
             {
                 testTable.move(+1);
                 testTable.remove();
                 iCount++;
             }
         } catch (DBException e)   {
         }
 
         try   {
             testTable.addNew();
 
             boolean bRefreshTest = true;
             if (bRefreshTest)
                 testTable.setOpenMode(DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY);  // Make sure keys are updated before sync
 
             testTable.getField(TestTable.kID).setString("1");
             testTable.getField(TestTable.TEST_NAME).setString("A - Excellent Agent");
             testTable.getField(TestTable.TEST_MEMO).setString("This is a very long line\nThis is the second line.");
             testTable.getField(TestTable.TEST_YES_NO).setState(true);
             testTable.getField(TestTable.TEST_LONG).setValue(15000000);
             testTable.getField(TestTable.TEST_SHORT).setValue(14000);
             date = new Date();
 //////////////////////////
             BaseField.gCalendar.setTime(date);
             BaseField.gCalendar.set(Calendar.MILLISECOND, 0);
             date = BaseField.gCalendar.getTime();
 //////////////////////////
 
             ((DateTimeField)testTable.getField(TestTable.TEST_DATE_TIME)).setDateTime(date, DBConstants.DISPLAY, DBConstants.SCREEN_MOVE);
             ((DateTimeField)testTable.getField(TestTable.TEST_DATE)).setDate(date, DBConstants.DISPLAY, DBConstants.SCREEN_MOVE);
             testTable.getField(TestTable.TEST_TIME).setString("5:15 PM");
             Calendar cal = ((DateTimeField)testTable.getField(TestTable.TEST_TIME)).getCalendar();
             if (cal.get(Calendar.HOUR_OF_DAY) != 17)
                 testTable.getField(TestTable.TEST_TIME).setString("17:15");
             testTable.getField(TestTable.TEST_FLOAT).setValue(1234.56);
             testTable.getField(TestTable.TEST_DOUBLE).setValue(1234567.8912);
             testTable.getField(TestTable.TEST_PERCENT).setValue(34.56);
             testTable.getField(TestTable.TEST_REAL).setValue(5432.432);
             testTable.getField(TestTable.TEST_CURRENCY).setValue(1234567.89);
             testTable.getField(TestTable.TEST_KEY).setString("A");
 
             testTable.add();
 
             testTable.addNew();
             testTable.getField(TestTable.kID).setString("2");
             testTable.getField(TestTable.TEST_NAME).setString("B - Good Agent");
             testTable.getField(TestTable.TEST_KEY).setString("B");
             testTable.add();
 
             testTable.addNew();
             testTable.getField(TestTable.kID).setString("3");
             testTable.getField(TestTable.TEST_NAME).setString("C - Average Agent");
             testTable.getField(TestTable.TEST_KEY).setString("C");
             testTable.add();
 
             testTable.addNew();
             testTable.getField(TestTable.kID).setString("4");
             testTable.getField(TestTable.TEST_NAME).setString("F - Fam Trip Agent");
             testTable.getField(TestTable.TEST_KEY).setString("B");
             testTable.add();
 
             testTable.addNew();
             testTable.getField(TestTable.kID).setString("5");
             testTable.getField(TestTable.TEST_NAME).setString("T - Tour Operator");
             testTable.getField(TestTable.TEST_KEY).setString("B");
             testTable.add();
 
             testTable.addNew();
             testTable.getField(TestTable.kID).setString("6");
             testTable.getField(TestTable.TEST_NAME).setString("Q - Q Agency");
             testTable.getField(TestTable.TEST_KEY).setString("Q");
             testTable.add();
         } catch (DBException e)   {
         }
 
         try   {
             testTable.setKeyArea(DBConstants.MAIN_KEY_AREA);
             iCount = 0;
             testTable.close();
             while (testTable.hasNext())   {
                 testTable.move(+1);
                 iCount++;
             }
         } catch (DBException e)   {
         }
     }
 }
