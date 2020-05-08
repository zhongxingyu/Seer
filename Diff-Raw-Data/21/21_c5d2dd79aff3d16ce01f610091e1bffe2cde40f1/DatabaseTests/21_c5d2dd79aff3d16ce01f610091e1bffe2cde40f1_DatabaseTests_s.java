 package com.alexrnl.commons.database;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
import com.alexrnl.commons.database.dao.AbstractDAOFactoryTest;
import com.alexrnl.commons.database.dao.DAOInstantiationErrorTest;
import com.alexrnl.commons.database.dao.DataSourceConfigurationTest;
import com.alexrnl.commons.database.sql.QueryGeneratorTest;
 import com.alexrnl.commons.database.sql.SQLTests;
import com.alexrnl.commons.database.structure.ColumnTest;
import com.alexrnl.commons.database.structure.NoIdErrorTest;
 
 /**
  * Test suite for the database package.
  * @author Alex
  */
 @RunWith(Suite.class)
@SuiteClasses({ AbstractDAOFactoryTest.class, ColumnTest.class, DataSourceConfigurationTest.class,
		NoIdErrorTest.class, DAOInstantiationErrorTest.class, QueryGeneratorTest.class,
		SQLTests.class })
 public class DatabaseTests {
 }
