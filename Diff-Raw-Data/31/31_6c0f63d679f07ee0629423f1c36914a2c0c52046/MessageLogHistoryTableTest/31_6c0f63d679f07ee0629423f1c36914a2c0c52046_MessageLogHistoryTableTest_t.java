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
 
 
 import static org.testng.AssertJUnit.assertEquals;
 import static org.testng.AssertJUnit.assertNotNull;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.Test;
 import org.testng.annotations.BeforeMethod;
 import java.util.Date;
 
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.SessionFactoryUtil;
 import edu.psu.iam.cpr.core.database.beans.MessageLogHistory;
 import edu.psu.iam.cpr.core.database.beans.MessageLog;
 import edu.psu.iam.cpr.core.database.tables.MessageLogHistoryTable;
 import edu.psu.iam.cpr.core.database.tables.MessageLogTable;
 import edu.psu.iam.cpr.core.service.returns.MessageLogHistoryReturn;
 
 /**
  * The class <code>MessageLogHistoryTableTest</code> contains tests for the class <code>{@link MessageLogHistoryTable}</code>.
  *
  * @generatedBy CodePro at 1/16/12 9:51 AM
  * @author cpruser
  * @version $Revision: 1.0 $
  */
 public class MessageLogHistoryTableTest {
 	
 	private static Database db = new Database();
 	public static void openDbConnection() throws Exception {
 		db.openSession(SessionFactoryUtil.getSessionFactory());
 	}
 	
 	/**
 	 * Run the MessageLogHistoryTable() constructor test.
 	 * Test method for {@link edu.psu.iam.cpr.core.database.tables.MessageLogHistoryTable#MessageHsitoryLogTable()}.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test
 	public void testMessageLogHistoryTable_1()
 		throws Exception {
 
 		MessageLogHistoryTable result = new MessageLogHistoryTable();
 
 		// add additional test code here
 		assertNotNull(result);
 		assertEquals(null, result.getMessageLogHistoryBean());
 	}
 
 	/**
 	 * Run the MessageLogHistoryTable(MessageLog,String) constructor test.
 	 * Test method for {@link edu.psu.iam.cpr.core.database.tables.MessageLogHistoryTable#MessageHsitoryLogTable(MessageLog, String)}.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test(expectedExceptions = Exception.class)
 	public void testMessageLogHistoryTable_2()
 		throws Exception {
 		MessageLog messageLogBean = new MessageLog();
 
 		new MessageLogHistoryTable(messageLogBean);
 
 		// add additional test code here
 
 	}
 	
 	/**
 	 * Run the MessageLogHistoryTable(MessageLog,String) constructor test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test
 	public void testMessageLogHistoryTable_3()
 		throws Exception {
 		openDbConnection();
		MessageLogTable fixture = new MessageLogTable(100126L, 1L, 1L, "Log History Test", "cpruser");
 		fixture.addMessageLog(db);
 
 		MessageLogHistoryTable result = new MessageLogHistoryTable(fixture.getMessageLogBean());
 
 		// add additional test code here
 		assertNotNull(result);
 		db.closeSession();
 	}
 
 	/**
 	 * Run the void addMessageLogHistory(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test(expectedExceptions = Exception.class)
 	public void testAddMessageLogHistory_1()
 		throws Exception {
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable();
 		fixture.setMessageLogHistoryBean(new MessageLogHistory());
 
 		fixture.addMessageLogHistory(db);
 
 		// add additional test code here
 	}
 
 	/**
 	 * Run the void addMessageLogHistory(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test
 	public void testAddMessageLogHistory_2()
 		throws Exception {
 		openDbConnection();
		MessageLogTable msgLog = new MessageLogTable(100126L, 1L, 1L, "Log History Add Test", "cpruser");
 		msgLog.addMessageLog(db);
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable(msgLog.getMessageLogBean());
 		fixture.addMessageLogHistory(db);
 		db.closeSession();
 		// add additional test code here
 	}
 	
 	/**
 	 * Run the MessageLogHistoryReturn[] getMessageLogHistory(Database,long) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test(expectedExceptions = Exception.class)
 	public void testGetMessageLogHistory_1()
 		throws Exception {
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable();
 		fixture.setMessageLogHistoryBean(new MessageLogHistory());
 		String messageId = "LogHistoryGetTest1";
 
 		MessageLogHistoryReturn[] result = fixture.getMessageLogHistory(db, messageId);
 
 		// add additional test code here
 		assertNotNull(result);
 	}
 
 	/**
 	 * Run the MessageLogHistoryReturn[] getMessageLogHistory(Database,long) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test
 	public void testGetMessageLogHistory_2()
 		throws Exception {
 		openDbConnection();
		MessageLogTable msgLog = new MessageLogTable(100126L, 1L, 1L, "Log History Get Test", "cpruser");
 		msgLog.addMessageLog(db);
 		String messageId = "LogHistoryGetTest2A";
 		MessageLogHistoryTable msgLogHistory = new MessageLogHistoryTable(msgLog.getMessageLogBean());
 		msgLogHistory.getMessageLogHistoryBean().setMessageId(messageId);
 		msgLogHistory.addMessageLogHistory(db);
 
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable();
 		fixture.setMessageLogHistoryBean(new MessageLogHistory());
 		MessageLogHistoryReturn[] result = fixture.getMessageLogHistory(db, messageId);
 		db.closeSession();
 
 		// add additional test code here
 		assertNotNull(result);
 		if (result[0] != null) {
 			assertEquals(result[0].getMessageId(), messageId);
 		}
 	}
 
 	/**
 	 * Run the MessageLogHistory getMessageLogHistoryBean() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test
 	public void testGetMessageLogHistoryBean_1()
 		throws Exception {
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable();
 		fixture.setMessageLogHistoryBean(new MessageLogHistory());
 
 		MessageLogHistory result = fixture.getMessageLogHistoryBean();
 
 		// add additional test code here
 		assertNotNull(result);
 		assertEquals(null, result.getErrorCode());
 		assertEquals(null, result.getErrorMessage());
 		assertEquals(null, result.getMessageId());
 		assertEquals(null, result.getMessageLogKey());
 		assertEquals(null, result.getCreatedOn());
 		assertEquals(null, result.getTryNumber());
 		assertEquals(null, result.getMessageSentTimestamp());
 		assertEquals(null, result.getMessageReceived());
 		assertEquals(null, result.getMessageReceivedTimestamp());
 		assertEquals(null, result.getMessageLogHistoryKey());
 	}
 
 	/**
 	 * Run the void setMessageLogHistoryBean(MessageLogHistory) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test
 	public void testSetMessageLogHistoryBean_1()
 		throws Exception {
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable();
 		fixture.setMessageLogHistoryBean(new MessageLogHistory());
 		MessageLogHistory messageHistoryLogBean = new MessageLogHistory();
 
 		fixture.setMessageLogHistoryBean(messageHistoryLogBean);
 
 		// add additional test code here
 		assertEquals(fixture.getMessageLogHistoryBean(), messageHistoryLogBean);
 	}
 	
 	/**
 	 * Run the void updateMessageLogHistory(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test(expectedExceptions = Exception.class)
 	public final void testUpdateMessageLogHistory_1()
 		throws Exception {
		MessageLogTable msgLog = new MessageLogTable(100126L, 1L, 1L, "Log History Update Test", "cpruser");
 		msgLog.addMessageLog(db);
 		String messageId = "LogHistoryUpdateTest1";
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable(msgLog.getMessageLogBean());
 		fixture.addMessageLogHistory(db);
 
 		fixture.getMessageLogHistoryBean().setMessageReceived("Test message id " + messageId + " received.");
 		fixture.getMessageLogHistoryBean().setMessageReceivedTimestamp(new Date());
 		fixture.updateMessageLogHistory(db, messageId);
 
 		// add additional test code here
 
 	}
 	
 	/**
 	 * Run the void updateMessageLogHistory(Database) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@Test
 	public final void testUpdateMessageLogHistory_2()
 		throws Exception {
 		Date d = new Date();
 		openDbConnection();
		MessageLogTable msgLog = new MessageLogTable(100126L, 1L, 1L, "Log History Update Test", "cpruser");
 		msgLog.addMessageLog(db);
 		String messageId = "LogHistoryUpdateTest2";
 		MessageLogHistoryTable fixture = new MessageLogHistoryTable(msgLog.getMessageLogBean());
 		fixture.addMessageLogHistory(db);
 
 		fixture.getMessageLogHistoryBean().setMessageReceived("Test message id " + messageId + " received.");
 		fixture.getMessageLogHistoryBean().setMessageReceivedTimestamp(d);
 		fixture.updateMessageLogHistory(db,messageId);
 		// add additional test code here
 		assertEquals(fixture.getMessageLogHistoryBean().getMessageReceivedTimestamp(), d);
 		db.closeSession();
 
 	}
 
 	/**
 	 * Perform pre-test initialization.
 	 *
 	 * @throws Exception
 	 *         if the initialization fails for some reason
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@BeforeMethod
 	public void setUp()
 		throws Exception {
 		// add additional set up code here
 	}
 
 	/**
 	 * Perform post-test clean-up.
 	 *
 	 * @throws Exception
 	 *         if the clean-up fails for some reason
 	 *
 	 * @generatedBy CodePro at 1/16/12 9:51 AM
 	 */
 	@AfterMethod
 	public void tearDown()
 		throws Exception {
 		// Add additional tear down code here
 	}
 
 }
