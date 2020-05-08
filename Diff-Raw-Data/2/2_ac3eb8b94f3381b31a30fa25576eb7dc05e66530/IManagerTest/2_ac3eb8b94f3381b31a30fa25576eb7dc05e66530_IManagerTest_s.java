 package com.mymed.tests;
 
 import java.util.Calendar;
 
 /**
  * General interface to hold static string values valid for all the manager
  * tests
  * 
  * @author Milo Casagrande
  */
 public interface IManagerTest {
	String CONF_FILE = "/local/mymed/backend/conf/config-new.xml";
 	String NAME = "username";
 	String FIRST_NAME = "First Name";
 	String LAST_NAME = "Last Name";
 	String TABLE_NAME = "User";
 	String WRONG_TABLE_NAME = "Users";
 	String KEY = "user1";
 	String WRONG_KEY = "1user";
 	String COLUMN_NAME = "name";
 	String COLUMN_FIRSTNAME = "firstName";
 	String COLUMN_LASTNAME = "lastName";
 	String COLUMN_BIRTHDATE = "birthday";
 	String WRONG_COLUMN_NAME = "name1";
 
 	String LOGIN = "usertest1";
 	String EMAIL = "testUser@example.net";
 	String LINK = "http://www.example.net";
 	String HOMETOWN = "123456789.123454";
 	String GENDER = "female";
 	String BUDDY_LST_ID = "buddylist1";
 	String SUBSCRIPTION_LST_ID = "subscription1";
 	String REPUTATION_ID = "reputation1";
 	String SESSION_ID = "session1";
 	String INTERACTION_LST_ID = "interaction1";
 
 	String INTERACTION_ID = "interaction1";
 	String APPLICATION_ID = "application1";
 	String PRODUCER_ID = "producerKey";
 	String CONSUMER_ID = "consumerKey";
 
 	Calendar CAL_INSTANCE = Calendar.getInstance();
 }
