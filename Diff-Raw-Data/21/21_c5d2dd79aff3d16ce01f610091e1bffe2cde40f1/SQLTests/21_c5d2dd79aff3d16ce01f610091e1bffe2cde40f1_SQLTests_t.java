 package com.alexrnl.commons.database.sql;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
  * Test suite for the database package.
  * @author Alex
  */
 @RunWith(Suite.class)
@SuiteClasses({ SQLDAOTest.class, QueryGeneratorTest.class })
 public class SQLTests {
 }
