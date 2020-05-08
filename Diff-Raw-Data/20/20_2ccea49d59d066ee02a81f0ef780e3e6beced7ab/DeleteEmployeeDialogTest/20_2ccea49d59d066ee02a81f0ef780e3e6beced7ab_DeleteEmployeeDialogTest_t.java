 package com.fs.humanResources.admin.employee;
 
 import com.fs.domain.page.admin.AdminPage;
 import com.fs.domain.page.admin.dialog.DeleteEmployeeDialog;
 import com.fs.domain.page.admin.dialog.FindEmployeeDialogForDelete;
 import com.fs.domain.page.admin.dialog.FindEmployeeDialogForEdit;
 import com.fs.humanResources.common.BaseSeleniumTest;
 import com.fs.humanResources.model.address.entities.Address;
 import com.fs.humanResources.model.employee.entities.Employee;
 import org.junit.*;
 
 import java.text.ParseException;
 
 public class DeleteEmployeeDialogTest extends BaseSeleniumTest {
 
     AdminPage adminPage;
 
     DeleteEmployeeDialog deleteEmployeeDialog;
 
     Employee employee;
 
     Address address;
 
     private boolean deletedBySelenium = false;
 
     @Before
     public void setUp() throws ParseException {
         address = new Address();
         address.setHouseNumber("50");
         address.setAddressFirstLine(persitenceHelper.getUniqueString(8));
         address.setAddressSecondLine("Domain Court");
         address.setTownCity("Progammer City");
         address.setPostCode("AB1CDX");
         address.setPrimaryAddress(true);
 
         employee = new Employee();
         employee.setFirstName("James");
         employee.setLastName(persitenceHelper.getUniqueString(8));
 
         employee.setDateOfBirth(simpleDateFormat.parse("15/07/1976"));
 
         employee.addAddress(address);
 
         persitenceHelper.beginTransaction();
 
         persitenceHelper.persistNewEmployee(employee);
         persitenceHelper.addDeletionCandidate(employee);
         persitenceHelper.commitTransaction();
 
         humanResourcesHome.openHomePage();
         humanResourcesHome.assertPageIsPresent();
 
         adminPage = humanResourcesHome.clickLoginBtn();
         adminPage.assertPageIsPresent();
 
         adminPage.assertEmployeeAdminMenuDisplayed().click();
 
         FindEmployeeDialogForDelete findEmployeeDialogForDelete = adminPage.clickDeleteEmployeeMenuItem();
         findEmployeeDialogForDelete.assertDialogIsPresent();
 
         findEmployeeDialogForDelete.setEmployeeId(employee.getId() + "");
         deleteEmployeeDialog = findEmployeeDialogForDelete.clickFindEmployeeBtnForDelete();
 
         deleteEmployeeDialog.assertDialogIsPresent();
     }
 
     @After
     public void tearDown() {
         if(!deletedBySelenium) {
            //persitenceHelper.deleteCandidates();
         }
         super.tearDown();
     }
 
     @Test
     public void deleteEmployeeFormElements_displayedAsExpected() {
         Assert.assertEquals("First Name:", deleteEmployeeDialog.firstNameLabelDisplayed().getText());
         deleteEmployeeDialog.firstNameInputDisplayed();
 
         Assert.assertEquals("Last Name:", deleteEmployeeDialog.lastNameLabelDisplayed().getText());
         deleteEmployeeDialog.lastNameInputDisplayed();
 
         Assert.assertEquals("DOB (dd/mm/yyyy):", deleteEmployeeDialog.dateOfBirthLabelDisplayed().getText());
         deleteEmployeeDialog.dateOfBirthInputDisplayed();
 
         Assert.assertEquals("House No:", deleteEmployeeDialog.houseNumberLabelDisplayed().getText());
         deleteEmployeeDialog.houseNumberInputDisplayed();
 
         Assert.assertEquals("Address Line 1:", deleteEmployeeDialog.addressFirstLineLabelDisplayed().getText());
         deleteEmployeeDialog.addressFirstLineInputDisplayed();
 
         Assert.assertEquals("Address Line 2:", deleteEmployeeDialog.addressSecondLineLabelDisplayed().getText());
         deleteEmployeeDialog.addressSecondLineInputDisplayed();
 
         Assert.assertEquals("Town/City:", deleteEmployeeDialog.townCityLabelDisplayed().getText());
         deleteEmployeeDialog.townCityInputDisplayed();
 
         Assert.assertEquals("Postcode:", deleteEmployeeDialog.postCodeLabelDisplayed().getText());
         deleteEmployeeDialog.postCodeInputDisplayed();
     }
 
     @Test
     public void employeeDetails_displayedAsExpected() {
         Assert.assertEquals(employee.getId() + "", deleteEmployeeDialog.employeeIdInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getFirstName(), deleteEmployeeDialog.firstNameInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getLastName(), deleteEmployeeDialog.lastNameInputDisplayed().getAttribute("value"));
         Assert.assertEquals(formateDate(employee.getDateOfBirth()), deleteEmployeeDialog.dateOfBirthInputDisplayed().getAttribute("value"));
 
         Assert.assertEquals(employee.getAddressList().get(0).getHouseNumber(), deleteEmployeeDialog.houseNumberInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getAddressList().get(0).getAddressFirstLine(), deleteEmployeeDialog.addressFirstLineInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getAddressList().get(0).getAddressSecondLine(), deleteEmployeeDialog.addressSecondLineInputDisplayed().getAttribute("value"));
         Assert.assertEquals(employee.getAddressList().get(0).getTownCity(), deleteEmployeeDialog.townCityInputDisplayed().getAttribute("value"));
         Assert.assertEquals("AB1-CDX", deleteEmployeeDialog.postCodeInputDisplayed().getAttribute("value"));
     }
 
     @Test
     public void employeeDetails_deleteAsExpected() {
         deletedBySelenium = true;
 
         deleteEmployeeDialog.clickDeleteEmployeeBtn();
         deleteEmployeeDialog.assertConfirmDialogIsPresent();
         deleteEmployeeDialog.confirmDeletion();
 
         FindEmployeeDialogForDelete findEmployeeDialogForDelete = adminPage.clickDeleteEmployeeMenuItem();
         findEmployeeDialogForDelete.assertDialogIsPresent();
 
         findEmployeeDialogForDelete.setEmployeeId(employee.getId() + "");
         findEmployeeDialogForDelete.clickFindEmployeeBtnForDelete();
         findEmployeeDialogForDelete.assertGrowlMessageDisplayed(
                 "Employee Id ("+employee.getId()+") not found!");
     }
 
 }
