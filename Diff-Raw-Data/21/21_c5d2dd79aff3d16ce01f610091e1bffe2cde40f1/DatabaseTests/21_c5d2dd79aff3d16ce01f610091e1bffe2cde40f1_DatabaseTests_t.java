 package com.alexrnl.commons.database;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
import com.alexrnl.commons.database.dao.DAOTests;
 import com.alexrnl.commons.database.sql.SQLTests;
import com.alexrnl.commons.database.structure.StructureTests;
 
 /**
  * Test suite for the database package.
  * @author Alex
  */
 @RunWith(Suite.class)
@SuiteClasses({ DAOTests.class, SQLTests.class, StructureTests.class })
 public class DatabaseTests {
 }
