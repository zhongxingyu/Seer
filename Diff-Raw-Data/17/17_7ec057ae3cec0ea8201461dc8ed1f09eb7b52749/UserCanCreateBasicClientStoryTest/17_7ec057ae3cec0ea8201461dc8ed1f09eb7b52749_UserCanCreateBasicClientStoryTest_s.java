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
 
 package acceptance.client;
 
 import java.io.StringReader;
 import java.sql.Connection;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.dbunit.Assertion;
 import org.dbunit.DataSourceDatabaseTester;
 import org.dbunit.IDatabaseTester;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.joda.time.format.DateTimeFormat;
 import org.mifos.test.framework.util.DatabaseTestUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DataSourceUtils;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.test.context.ContextConfiguration;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.pageobjects.ClientsAndAccountsPage;
 import framework.pageobjects.CreateClientPage;
 import framework.pageobjects.CreateClientSuccessPage;
 import framework.pageobjects.HomePage;
 import framework.pageobjects.LoginPage;
 import framework.test.UiTestCaseBase;
 
 /*
  * Corresponds to story 683 in mingle
  * http://mingle.mifos.org:7070/projects/cheetah/cards/683
  */
 @ContextConfiguration(locations={"classpath:ui-test-context.xml"})
 @Test(sequential=true, groups={"userCanCreateBasicClientStoryTest","acceptance","ui"})
 public class UserCanCreateBasicClientStoryTest extends UiTestCaseBase{
 
    private static final Log LOG = LogFactory.getLog(UserCanCreateBasicClientStoryTest.class);
 	private LoginPage loginPage;
 	private DriverManagerDataSource dataSource;
     private DatabaseTestUtils databaseTestUtils;
 
 	@BeforeMethod
 	public void setUp() throws Exception {
 		super.setUp();
 		loginPage = new LoginPage(selenium);
         this.databaseTestUtils.deleteDataFromTable("clients", this.getDataSource());
 	}
 
 	@AfterMethod
 	public void logOut() {
 		loginPage.logout();
 	}
 
 	public void createValidClientTest() throws Exception {
 		createAndVerifyValidClient("01/19/1971");
 	}
 
 	public void createValidClientEdgeTest() throws Exception {
 		createAndVerifyValidClient("11/19/1883");
 	}
 
 	public void createInvalidClientTest() {
 		createAndVerifyInvalidClient("01/01/1753");		
 	}
 
 	public void createInvalidClientTestEdge() {
 		createAndVerifyInvalidClient("12/31/1879");		
 	}
 
     public void createValidClientFrenchLocaleTest() throws Exception {
         createAndVerifyValidClientWithLocale("19/01/1971", "dd/MM/yyyy", "fr_Fr");
     }
 
     public void createValidClientPhilipinesLocaleTest() throws Exception {
         createAndVerifyValidClientWithLocale("1/19/1971", "M/d/yyyy", "en_PH");
     }
 
 	private void createAndVerifyValidClient(String dateOfBirth) throws Exception {
 	    createAndVerifyValidClientWithLocale(dateOfBirth, "M/d/yyyy", null);
 	}
 
     private void createAndVerifyValidClientWithLocale(String dateOfBirth, String datePattern, String locale) throws Exception {
         String firstName = "John";
         String lastName = "Ya-ya";
         CreateClientPage createClientPage = loginAndNavigateToCreateClientPage();
         if (locale != null) {
           createClientPage = createClientPage.changeLocale(locale);
         }
         createClientPage.verifyDatePatternMessage(datePattern);
         CreateClientSuccessPage createClientSuccessPage = createClientPage.createValidClient(firstName, lastName, dateOfBirth);
         createClientSuccessPage.verifyPage();
         verifyClientCreation(firstName, lastName, dateOfBirth, datePattern);
     }
 
     private void createAndVerifyInvalidClient(String dateOfBirth) {
 		String firstName = "John";
 		String lastName = "Ready To Fly";
 		CreateClientPage createClientPage = loginAndNavigateToCreateClientPage();
 		CreateClientPage createClientPage2 = createClientPage.createClientExpectingError(firstName, lastName, dateOfBirth);
 		createClientPage2.verifyPage();
 		createClientPage2.verifyErrorExists("Please enter a valid date of birth.");
 	}
 
 	private CreateClientPage loginAndNavigateToCreateClientPage() {
 		HomePage homePage = loginPage.loginAs("mifos", "testmifos");
 		ClientsAndAccountsPage clientsAndAccountsPage = homePage.navigateToClientsAndAccountsPage();
 		CreateClientPage createClientPage = clientsAndAccountsPage.navigateToCreateClientPage();
         createClientPage.verifyPage();
         createClientPage = createClientPage.changeLocale("en_US");
 		return createClientPage;
 	}
 	
 	private void verifyClientCreation(String firstName, String lastName, String dateOfBirth, String datePattern) throws Exception {
 	    String expectedTableDatasetXml = 
 	        "<dataset>\n" +
                 "<clients id=\"1\" " +
                 "firstName=\"" + firstName + "\" \n" +
                 "         lastname=\"" +lastName + "\" \n" +
                 "         dateOfBirth=\"" + getIso8601Date(dateOfBirth, datePattern) + "\" /> \n" +
 	        "</dataset>\n";
         Connection jdbcConnection = null;
         StringReader dataSetXmlStream = new StringReader(expectedTableDatasetXml);
         try {
             jdbcConnection = DataSourceUtils.getConnection(dataSource);
             IDatabaseTester databaseTester = new DataSourceDatabaseTester(this.dataSource);
             IDatabaseConnection databaseConnection = databaseTester.getConnection();
             IDataSet databaseDataSet = databaseConnection.createDataSet();
             ITable actualTable = databaseDataSet.getTable("clients");
             IDataSet expectedDataSet = new FlatXmlDataSet(dataSetXmlStream);
             ITable expectedTable = expectedDataSet.getTable("clients");
             Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[] { "id" });   
         }
         finally {
             jdbcConnection.close();
             DataSourceUtils.releaseConnection(jdbcConnection, dataSource);
         }
  	    
 	    
 	}
 	
 	private String getIso8601Date(String date, String datePattern) {
         String iso8601Date = DateTimeFormat.forPattern(datePattern).parseDateTime(date).toDateMidnight().toLocalDate().toString();
        LOG.info("**** date: " + date + " datePattern: " + datePattern + " iso date: " + iso8601Date);
         return iso8601Date;
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
 
