 package com.fs.domain.page.admin;
 
 
 import com.fs.domain.common.page.PageObject;
 import com.fs.domain.page.HumanResourcesHome;
 import com.fs.domain.page.admin.dialog.AddEmployeeDialog;
 import com.fs.domain.page.admin.dialog.FindEmployeeDialogForDelete;
 import com.fs.domain.page.admin.dialog.FindEmployeeDialogForEdit;
 import com.fs.domain.page.browse.BrowseEmployeesPage;
 import com.fs.domain.page.search.EmployeeSearchPage;
 import org.apache.log4j.Logger;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 public class AdminPage extends PageObject {
 
     Logger log = Logger.getLogger(AdminPage.class);
 
     private static final String FORM = "adminTool";
     private static final String CONTENT_PANEL_ID = "contentPanel";
    private static final String ADMIN_INFO_PANEL = "adminInfoPanel";
 
     public AdminPage(WebDriver driver) {
         super(driver);
     }
 
     public void assertPageIsPresent() {
         findFormElementById(FORM, CONTENT_PANEL_ID);
     }
 
     private WebElement assertEmployeeAdminMenuDisplayed() {
         return assertMenuDisplayed("Employee Admin");
     }
 
     public void openEmployeeAdminMenu() {
         moveToElement(assertEmployeeAdminMenuDisplayed());
         assertEmployeeAdminMenuDisplayed().click();
     }
 
    public WebElement assertAdminInfoPanelDisplayed() {
        return findFormElementById(FORM, ADMIN_INFO_PANEL);
     }
 
     public WebElement assertBrowseMenuDisplayed() {
         return assertMenuDisplayed("Browse");
     }
 
 
     public WebElement assertEditEmployeeMenuItemDisplayed() {
         return findFormElementById(FORM,"editEmployee");
     }
 
     public WebElement assertAddEmployeeMenuItemDisplayed() {
         return findFormElementById(FORM,"addEmployee");
     }
 
     public WebElement assertDeleteEmployeeMenuItemDisplayed() {
         return findFormElementById(FORM,"deleteEmployee");
     }
 
     public WebElement assertBrowseEmployeeMenuItemDisplayed() {
         return findFormElementById(FORM,"browseEmployees");
     }
 
     public WebElement assertSearchBtnDisplayed() {
         return findFormElementById(FORM,"searchBtn");
     }
 
     public WebElement assertSearchInputDisplayed() {
         return findFormElementById(FORM,"searchTerm");
     }
 
     private WebElement assertMenuDisplayed(String labelText) {
         return findLinkByText(labelText);
     }
 
     public WebElement assertLogoutBtnDisplayed() {
         return findFormElementById(FORM,"logoutBtn");
     }
 
     public AddEmployeeDialog clickAddEmployeeMenuItem() {
         moveToAddEmployeeMenuItem();
         assertAddEmployeeMenuItemDisplayed().click();
         return new AddEmployeeDialog(getDriver());
     }
 
     public FindEmployeeDialogForEdit clickEditEmployeeMenuItem() {
         moveToEditEmployeeMenuItem();
         assertEditEmployeeMenuItemDisplayed().click();
         return new FindEmployeeDialogForEdit(getDriver());
     }
 
     public BrowseEmployeesPage clickBrowseEmployeesMenuItem() {
         moveToBrowseEmployeeMenuItem();
         assertBrowseEmployeeMenuItemDisplayed().click();
         return new BrowseEmployeesPage(getDriver());
     }
 
     private void moveToEditEmployeeMenuItem() {
         moveToElement(assertEditEmployeeMenuItemDisplayed());
     }
 
     private void moveToDeleteEmployeeMenuItem() {
         moveToElement(assertDeleteEmployeeMenuItemDisplayed());
     }
 
     private void moveToAddEmployeeMenuItem() {
         moveToElement(assertDeleteEmployeeMenuItemDisplayed());
     }
 
     private void moveToBrowseEmployeeMenuItem() {
         moveToElement(assertBrowseEmployeeMenuItemDisplayed());
     }
 
     public HumanResourcesHome clickLogoutBtn() {
         assertLogoutBtnDisplayed().click();
         return new HumanResourcesHome(getDriver());
     }
 
     public FindEmployeeDialogForDelete clickDeleteEmployeeMenuItem() {
         moveToDeleteEmployeeMenuItem();
         assertDeleteEmployeeMenuItemDisplayed().click();
         return new FindEmployeeDialogForDelete(getDriver());
     }
 
     public EmployeeSearchPage clickSearchButton() {
         assertSearchBtnDisplayed().click();
         return new EmployeeSearchPage(getDriver());
     }
 
     public void enterSearchTerm(String searchTerm) {
         setInputTextOnElement(assertSearchInputDisplayed(),searchTerm);
     }
 }
