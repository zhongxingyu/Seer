 /*
  * Copyright (c) 2005-2008 Grameen Foundation USA
  * All rights reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package org.mifos.client.repository;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dbunit.DatabaseUnitException;
 import org.dbunit.dataset.DataSetException;
 import org.joda.time.LocalDate;
 import org.joda.time.contrib.hibernate.PersistentLocalDate;
 import org.mifos.core.MifosException;
 import org.mifos.test.framework.util.DatabaseTestUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations={"classpath:integrationTestContext.xml"})
 @TransactionConfiguration(transactionManager="transactionManager")
 @Test(groups = { "integration" })
 public class PersistentLocalDateTest extends AbstractTransactionalTestNGSpringContextTests {
 
    private static final Log LOG = LogFactory.getLog(PersistentLocalDateTest.class);

 	@Autowired
 	private ClientDao clientDao;
 	private DriverManagerDataSource dataSource;
     private DatabaseTestUtils databaseTestUtils;
 
 	@BeforeMethod
 	public void setUp() throws DataSetException, IOException, SQLException, DatabaseUnitException {
 	    this.databaseTestUtils.deleteDataFromTable("clients", this.getDataSource());
 	}
 	
     public void testPersistentLocalDateBoundaries() throws MifosException, SQLException {
         verifyPersistentLocalDate("1884-01-01");
         verifyPersistentLocalDate("1883-12-31");
         verifyPersistentLocalDate("1883-12-21");
         verifyPersistentLocalDate("1883-12-11");
         verifyPersistentLocalDate("1883-12-01");
         verifyPersistentLocalDate("1883-11-30");
         verifyPersistentLocalDate("1883-11-21");
         verifyPersistentLocalDate("1883-11-20");
         verifyPersistentLocalDate("1883-11-19");
 // all dates before this one are one day off - introduction of daylight savings time?
 //        verifyPersistentLocalDate("1883-11-18");
 //        verifyPersistentLocalDate("1883-01-01");
 //        verifyPersistentLocalDate("1880-01-01");
     }
 
     private void verifyPersistentLocalDate(String expectedDate) throws MifosException, SQLException {
         LocalDate localDate = new LocalDate(expectedDate);
         PersistentLocalDate persistentLocalDate = new PersistentLocalDate();
         Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("insert into clients (dateOfBirth, firstName, lastName) values (?, ?, ?)");
         persistentLocalDate.nullSafeSet(preparedStatement, localDate, 1);
        LOG.info("**** statement: " + preparedStatement.toString());
         String[] statementParts = preparedStatement.toString().split(" ");
         String actualDate = statementParts[8];
         actualDate = actualDate.replaceAll("\\(", "");
         actualDate = actualDate.replaceAll("'", "");
         actualDate = actualDate.replaceAll(",", "");
        LOG.info("**** statement parts: " + actualDate);
         Assert.assertEquals(actualDate, expectedDate);
     }
     
     @Test(enabled=false)
     public DriverManagerDataSource getDataSource() {
         return dataSource;
     }
 
     @Autowired
     @Test(enabled=false)
     public void setDataSource(DriverManagerDataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     @Autowired
     @Test(enabled=false)
     public void setDatabaseTestUtils(DatabaseTestUtils databaseTestUtils) {
         this.databaseTestUtils = databaseTestUtils;
     }
 
 }
