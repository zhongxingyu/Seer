 
 package edu.psu.iam.cpr.core.tests;
 
 /**
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.Test;
 import org.testng.annotations.BeforeMethod;
 import org.testng.AssertJUnit;
 import java.util.Date;
 
 import edu.psu.iam.cpr.core.service.returns.MessageLogReturn;
 import edu.psu.iam.cpr.core.database.beans.MessageLog;
 import edu.psu.iam.cpr.core.database.tables.MessageLogTable;
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.SessionFactoryUtil;
 
 /**
  * The class <code>MessageLogTableTest</code> contains tests for the class <code>{@link MessageLogTable}</code>.
  *
  * @generatedBy CodePro at 1/5/12 3:46 PM
  * @author cpruser
  * @version $Revision: 5340 $
  */
 
 public class MessageLogTableTest {
 	
 	private static Database db = new Database();
 	public static void openDbConnection() throws Exception {
 		db.openSession(SessionFactoryUtil.getSessionFactory());
 	}
 	
 	/**
 	 * Run the MessageLogTable() constructor test.
 	 * Test method for {@link edu.psu.iam.cpr.core.database.tables.MessageLogTable#MessageLogTable()}.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testMessageLogTable_1()
 		throws Exception {
 
 		MessageLogTable result = new MessageLogTable();
 
 		// add additional test code here
 		AssertJUnit.assertNotNull(result);
 		AssertJUnit.assertEquals(null, result.getMessageLogBean());
 	}
 
 	/**
 	 * Run the MessageLogTable(long,long,String,String) constructor test.
 	 * Test method for {@link edu.psu.iam.cpr.core.database.tables.MessageLogTable#MessageLogTable(long,long,String,String)}.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testMessageLogTable_2()
 		throws Exception {
 
 		long webServiceId = 1L;
 		long serviceProviderId = 1L;
 		String messageSent = "";
 		String requestUserid = "";
 
 		MessageLogTable result = new MessageLogTable(1L, webServiceId, serviceProviderId, messageSent, requestUserid);
 
 		// add additional test code here
 		AssertJUnit.assertNotNull(result);
 		AssertJUnit.assertNotNull(result.getMessageLogBean());
 	}
 
 	/**
 	 * Run the void addMessageLog(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test(expectedExceptions = Exception.class)
 	public final void testAddMessageLog_1()
 		throws Exception {
 		MessageLogTable fixture = new MessageLogTable();
 		fixture.setMessageLogBean(new MessageLog());
 
 		fixture.addMessageLog(db);
 		// add additional test code here
 
 	}
 	
 	/**
 	 * Run the void addMessageLog(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testAddMessageLog_2()
 		throws Exception {
 		openDbConnection();
 		MessageLogTable fixture = new MessageLogTable();
 		MessageLog bean = new MessageLog();
 		bean.setMessageSent("Test");
 		bean.setRequestUserid("cpruser");
		bean.setMessageConsumerKey(1L);
		bean.setServiceKey(1L);
 		bean.setWebServiceKey(100126L);
 		bean.setNumberOfTries(0L);
 		bean.setSuccessFlag("N");
 		Date d = new Date();
 		bean.setCreatedOn(d);
 		bean.setLastUpdateOn(d);
 		fixture.setMessageLogBean(bean);
 		fixture.addMessageLog(db);
 		db.closeSession();
 		// add additional test code here
 
 	}
 
 	/**
 	 * Run the void addMessageLog(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testAddMessageLog_3()
 		throws Exception {
 		openDbConnection();
		MessageLogTable fixture = new MessageLogTable(100126L, 1L, 1L, "Test Add", "cpruser");
 		fixture.addMessageLog(db);
 		db.closeSession();
 		// add additional test code here
 
 	}
 	
 	/**
 	 * Run the MessageLogReturn[] getMessageLog(Database,long) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test(expectedExceptions = Exception.class)
 	public final void testGetMessageLog_1()
 		throws Exception {
 		MessageLogTable fixture = new MessageLogTable();
 		fixture.getMessageLog(db, 1000);
 
 		// add additional test code here
 
 	}
 
 	/**
 	 * Run the MessageLogReturn[] getMessageLog(Database,long) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testGetMessageLog_2()
 		throws Exception {
 		openDbConnection();
		MessageLogTable fixture = new MessageLogTable(100126L, 1L, 1L, "Test Get", "cpruser");
 		fixture.addMessageLog(db);
 		MessageLogReturn[] result = fixture.getMessageLog(db, fixture.getMessageLogBean().getMessageLogKey());
 		// add additional test code here
 		AssertJUnit.assertNotNull(result);
 		if (result[0] != null) {
 			AssertJUnit.assertEquals(result[0].getMessageLogKey().longValue(), fixture.getMessageLogBean().getMessageLogKey().longValue());
 		}
 		db.closeSession();
 	}
 
 	/**
 	 * Run the MessageLog getMessageLogBean() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testGetMessageLogBean_1()
 		throws Exception {
 		MessageLogTable fixture = new MessageLogTable();
 		MessageLog bean = new MessageLog();
 		fixture.setMessageLogBean(bean);
 
 		MessageLog result = fixture.getMessageLogBean();
 
 		// add additional test code here
 		AssertJUnit.assertNotNull(result);
 		AssertJUnit.assertEquals(result, bean);
 		AssertJUnit.assertEquals(null, result.getSuccessFlag());
 		AssertJUnit.assertEquals(null, result.getLastUpdateOn());
 		AssertJUnit.assertEquals(null, result.getMessageConsumerKey());
 		AssertJUnit.assertEquals(null, result.getCreatedOn());
 		AssertJUnit.assertEquals(null, result.getRequestUserid());
 		AssertJUnit.assertEquals(null, result.getWebServiceKey());
 		AssertJUnit.assertEquals(null, result.getNumberOfTries());
 		AssertJUnit.assertEquals(null, result.getMessageSent());
 		AssertJUnit.assertEquals(null, result.getMessageLogKey());
 	}
 
 	/**
 	 * Run the void setMessageLogBean(MessageLog) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testSetMessageLogBean_1()
 		throws Exception {
 		MessageLogTable fixture = new MessageLogTable();
 		MessageLog messageLogBean = new MessageLog();
 
 		fixture.setMessageLogBean(messageLogBean);
 
 		// add additional test code here
 		AssertJUnit.assertEquals(fixture.getMessageLogBean(), messageLogBean);
 	}
 
 	/**
 	 * Run the void updateMessageLog(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test(expectedExceptions = Exception.class)
 	public final void testUpdateMessageLog_1()
 		throws Exception {
 		MessageLogTable fixture = new MessageLogTable(100126L, 100002L, 1L, "Test update", "cpruser");
 		fixture.updateMessageLog(db,"Y",1L);
 
 		// add additional test code here
 	}
 	
 	/**
 	 * Run the void updateMessageLog(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@Test
 	public final void testUpdateMessageLog_2()
 		throws Exception {
 		openDbConnection();
		MessageLogTable fixture = new MessageLogTable(100126L, 1L, 1L, "Test update", "cpruser");
 		fixture.addMessageLog(db);
 		fixture.updateMessageLog(db,"Y",1L);
 		db.closeSession();
 
 	}
 
 	/**
 	 * Perform pre-test initialization.
 	 *
 	 * @throws Exception
 	 *         if the initialization fails for some reason
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@BeforeMethod
 	public final void setUp()
 		throws Exception {
 		// add additional set up code here
 	}
 
 	/**
 	 * Perform post-test clean-up.
 	 *
 	 * @throws Exception
 	 *         if the clean-up fails for some reason
 	 *
 	 * @generatedBy CodePro at 1/5/12 3:46 PM
 	 */
 	@AfterMethod
 	public final void tearDown()
 		throws Exception {
 		// Add additional tear down code here
 	}
 
 }
