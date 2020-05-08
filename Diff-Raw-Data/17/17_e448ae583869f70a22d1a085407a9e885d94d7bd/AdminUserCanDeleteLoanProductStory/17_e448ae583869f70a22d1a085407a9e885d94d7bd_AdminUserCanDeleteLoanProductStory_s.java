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
 
 package acceptance.loan;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import org.dbunit.DatabaseUnitException;
 import org.dbunit.dataset.DataSetException;
 import org.mifos.test.framework.util.DatabaseTestUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.test.context.ContextConfiguration;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.pageobjects.DeleteLoanProductPage;
 import framework.pageobjects.LoginPage;
 import framework.pageobjects.ViewLoanProductDetailsPage;
 import framework.test.UiTestCaseBase;
 
 /*
  * Corresponds to story 681 in Mingle
  * {@link http://mingle.mifos.org:7070/projects/cheetah/cards/681 }
  */
 @ContextConfiguration(locations={"classpath:ui-test-context.xml"})
 @Test(groups={"DeleteLoanProductStory","acceptance","ui", "workInProgress"})
 public class AdminUserCanDeleteLoanProductStory extends UiTestCaseBase {
 
     private LoginPage loginPage;
 
     @Autowired
     private DriverManagerDataSource dataSource;
         
     private static final DatabaseTestUtils dbTestUtils = new DatabaseTestUtils();
     
 
     private static final String loanProductWithNoLoansDataSetXml = 
         "<dataset>\n" + 
         "  <loanproducts id=\"1\" longName=\"long1\" maxInterestRate=\"2.0\" minInterestRate=\"1.0\" shortName=\"short1\" status=\"0\"/>\n" + 
         "  <loans/>\n" + 
         "</dataset>\n";
 
     private static final String loanProductWithLoansDataSetXml = 
             "<dataset>\n" + 
             "  <loanproducts id=\"1\" longName=\"long1\" maxInterestRate=\"2.0\" minInterestRate=\"1.0\" shortName=\"short1\" status=\"0\"/>\n" + 
             "  <loans id=\"1\" amount=\"10.1\" clientId=\"1\" interestRate=\"10\" loanProductId=\"1\" disbursalDate=\"2007-01-01\" />\n " +
             "</dataset>\n";
 
     @BeforeMethod
     @SuppressWarnings("PMD.SignatureDeclareThrowsException") // signature of superclass method
     public void setUp() throws Exception {
         super.setUp();
         loginPage = new LoginPage(selenium);
     }
 
     @AfterMethod
     public void logOut() {
         loginPage.logout();
     }            
 
     public void testDeleteLoanProductWithoutLoans() throws DataSetException, IOException, SQLException, DatabaseUnitException {
         insertDataSetAndDeleteProduct(loanProductWithNoLoansDataSetXml);
         Assert.assertEquals(selenium.getText("id=deleteLoanProduct.successMessage"), "Successfully deleted loan product 'long1'.");
     }
     
     public void testDeleteLoanProductWithLoans() throws DataSetException, IOException, SQLException, DatabaseUnitException {
         insertDataSetAndDeleteProduct(loanProductWithLoansDataSetXml);
         Assert.assertEquals(selenium.getText("id=deleteLoanProduct.errorMessage"), "Could not delete loan product 'long1' because there are loans that use this product.");
     }
 
     private void insertDataSetAndDeleteProduct(String dataSetXml) throws IOException,
             DataSetException, SQLException, DatabaseUnitException {
         dbTestUtils.cleanAndInsertDataSet(dataSetXml, dataSource); 
         ViewLoanProductDetailsPage viewLoanProductDetailsPage = navigateToViewLoanProductDetailsPage("short1");
         DeleteLoanProductPage deleteLoanProductPage = viewLoanProductDetailsPage.navigateToDeleteLoanProductPage();
         deleteLoanProductPage.verifyPage();
         deleteLoanProductPage.deleteLoanProduct();
     }
     
     private ViewLoanProductDetailsPage navigateToViewLoanProductDetailsPage (String linkName){
         return
             loginPage
                 .loginAs("mifos", "testmifos")
                 .navigateToAdminPage()
                 .navigateToViewLoanProductsPage()
                 .navigateToViewLoanProductDetailsPage(linkName);
     }
 
    

 }
