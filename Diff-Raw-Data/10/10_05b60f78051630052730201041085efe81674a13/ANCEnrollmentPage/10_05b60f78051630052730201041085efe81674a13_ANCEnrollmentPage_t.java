 package org.motechproject.ghana.national.functional.pages.patient;
 
 import org.joda.time.format.DateTimeFormat;
 import org.motechproject.ghana.national.functional.data.TestANCEnrollment;
 import org.motechproject.ghana.national.functional.pages.home.HomePage;
 import org.joda.time.LocalDate;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.CacheLookup;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 import org.openqa.selenium.support.ui.Select;
 
 import static com.thoughtworks.selenium.SeleneseTestBase.assertEquals;
 
 public class ANCEnrollmentPage extends HomePage {
     @FindBy(id = "motechPatientId")
     @CacheLookup
     private WebElement motechPatientId;
 
     @FindBy(id = "staffId")
     @CacheLookup
     private WebElement staffId;
 
     @FindBy(id = "serialNumber")
     @CacheLookup
     private WebElement serialNumber;
 
     @FindBy(id = "registrationToday")
     @CacheLookup
     private WebElement registrationToday;
 
     @FindBy(id = "registrationDateHolder")
     @CacheLookup
     private WebElement registrationDateHolder;
 
     @FindBy(id = "registrationDate")
     @CacheLookup
     private WebElement registrationDate;
 
     @FindBy(id = "estimatedDateOfDeliveryHolder")
     @CacheLookup
     private WebElement estimatedDateOfDeliveryHolder;
 
     @FindBy(id = "estimatedDateOfDelivery")
     @CacheLookup
     private WebElement estimatedDateOfDelivery;
 
     @FindBy(id = "addHistory1")
     @CacheLookup
     private WebElement yes;
 
     @FindBy(id = "addHistory2")
     @CacheLookup
     private WebElement no;
 
     @FindBy(id = "countries")
     @CacheLookup
     private WebElement country;
 
     @FindBy(id = "facilities")
     @CacheLookup
     private WebElement facilities;
 
     @FindBy(id = "districts")
     @CacheLookup
     private WebElement district;
 
     @FindBy(id = "sub-districts")
     @CacheLookup
     private WebElement subDistrict;
 
     @FindBy(id = "regions")
     @CacheLookup
     private WebElement region;
 
     @FindBy(id = "submitANC")
     @CacheLookup
     private WebElement submit;
 
     @FindBy(id = "height")
     @CacheLookup
     private WebElement height;
 
     @FindBy(id = "gravida")
     @CacheLookup
     private WebElement gravida;
 
     @FindBy(id = "parity")
     @CacheLookup
     private WebElement parity;
 
     @FindBy(id = "deliveryDateConfirmed1")
     @CacheLookup
     private WebElement confirmed;
 
     @FindBy(id = "deliveryDateConfirmed2")
     @CacheLookup
     private WebElement notconfirmed;
 
     @FindBy(id = "careHistory2")
     @CacheLookup
     private WebElement iptCareHistory;
 
     @FindBy(id = "careHistory1")
     @CacheLookup
     private WebElement ttCareHistory;
 
     @FindBy(id = "lastTT1")
     @CacheLookup
     private WebElement lastTT1;
 
     @FindBy(id = "lastIPT1")
     @CacheLookup
     private WebElement lastIPT1;
 
     @FindBy(id = "lastIPTDate")
     @CacheLookup
     private WebElement lastIPTDate;
 
     @FindBy(id = "lastIPTDateHolder")
     @CacheLookup
     private WebElement lastIPTDateHolder;
 
     @FindBy(id = "lastTTDate")
     @CacheLookup
     private WebElement lastTTDate;
 
     @FindBy(id = "lastTTDateHolder")
     @CacheLookup
     private WebElement lastTTDateHolder;
 
     public ANCEnrollmentPage(WebDriver webDriver) {
         super(webDriver);
         elementPoller.waitForElementID("submitANC", driver);
         PageFactory.initElements(driver, this);
     }
 
     public ANCEnrollmentPage withParity(String parity) {
         this.parity.clear();
         this.parity.sendKeys(parity);
         return this;
     }
 
     public ANCEnrollmentPage withHeight(String height) {
         this.height.clear();
         this.height.sendKeys(height);
         return this;
     }
 
     public ANCEnrollmentPage withGravida(String gravida) {
         this.gravida.clear();
         this.gravida.sendKeys(gravida);
         return this;
     }
 
     public ANCEnrollmentPage withDeliveryDateConfirmed(Boolean deliveryDateConfirmed) {
         if (deliveryDateConfirmed)
             confirmed.click();
         else
             notconfirmed.click();
 
         return this;
     }
 
     public ANCEnrollmentPage withStaffId(String staffId) {
         this.staffId.clear();
         this.staffId.sendKeys(staffId);
         return this;
     }
 
     public ANCEnrollmentPage withSerialNumber(String serialNumber) {
         this.serialNumber.clear();
         this.serialNumber.sendKeys(serialNumber);
         return this;
     }
 
     public ANCEnrollmentPage withRegistrationToday(String registrationToday) {
         Select selectSubDist = new Select(this.registrationToday);
         selectSubDist.selectByValue(registrationToday);
         return this;
     }
 
     public String getRegsitrationToday() {
         return new Select(registrationToday).getFirstSelectedOption().getText();
     }
 
     public ANCEnrollmentPage withRegistrationDate(LocalDate registrationDate) {
         dateSelector.select(registrationDateHolder, registrationDate, driver);
         return this;
     }
 
     public ANCEnrollmentPage withFacility(String facility) {
         Select selectFacility = new Select(this.facilities);
         selectFacility.selectByVisibleText(facility);
         return this;
     }
 
     public ANCEnrollmentPage withCountry(String country) {
         Select selectCountry = new Select(this.country);
         selectCountry.selectByValue(country);
         return this;
     }
 
     public ANCEnrollmentPage withRegion(String region) {
         Select selectRegion = new Select(this.region);
         selectRegion.selectByValue(region);
         return this;
     }
 
     public ANCEnrollmentPage withDistrict(String district) {
         Select selectDistrict = new Select(this.district);
         selectDistrict.selectByValue(district);
         return this;
     }
 
     public ANCEnrollmentPage withSubDistrict(String subDistrict) {
         Select selectSubDistrict = new Select(this.subDistrict);
         selectSubDistrict.selectByValue(subDistrict);
         return this;
     }
 
     public ANCEnrollmentPage withAddHistory(Boolean history) {
         if (history)
             yes.click();
         else
             no.click();
         return this;
     }
 
     public ANCEnrollmentPage withEstimatedDateOfDelivery(LocalDate date) {
         dateSelector.select(estimatedDateOfDeliveryHolder, date, driver);
         return this;
     }
 
     public ANCEnrollmentPage withTT(Boolean ttValue) {
         boolean selected = ttCareHistory.isSelected();
         if (ttValue && !selected || !ttValue && selected)
             ttCareHistory.click();
         return this;
     }
 
     public ANCEnrollmentPage withLastTTValue1(Boolean lastTTValue1) {
         if (lastTTValue1)
             lastTT1.click();
         return this;
     }
 
     public ANCEnrollmentPage withLastIPTValue1(Boolean lastTPTValue1) {
         if (lastTPTValue1)
             lastIPT1.click();
         return this;
     }
 
     public ANCEnrollmentPage withIPT(Boolean iptValue) {
        waitFor(iptCareHistory);
         boolean selected = iptCareHistory.isSelected();
         if ((iptValue && !selected) || (!iptValue && selected)) {
             iptCareHistory.click();
         }
         return this;
     }
 
 
     public ANCEnrollmentPage withIPTDate(LocalDate date) {
         dateSelector.select(lastIPTDateHolder, date, driver);
         return this;
     }
 
     public ANCEnrollmentPage withTTDate(LocalDate date) {
         dateSelector.select(lastTTDateHolder, date, driver);
         return this;
     }
 
     public String getGravida() {
         return gravida.getAttribute("value");
     }
 
     public String getMotechPatientId() {
         return motechPatientId.getAttribute("value");
     }
 
     public String getStaffId() {
         return staffId.getAttribute("value");
     }
 
     public String getFacility() {
         return new Select(facilities).getFirstSelectedOption().getText();
     }
 
     public String getHeight() {
         return height.getAttribute("value");
     }
 
     public boolean getTtCareHistory() {
         return ttCareHistory.isSelected();
     }
 
     public String getLastTTDate() {
         return lastTTDate.getAttribute("value");
     }
 
     public String getLastTT1() {
         return lastTT1.getAttribute("value");
     }
 
     public String getLastIPT1() {
         return lastIPT1.getAttribute("value");
     }
 
     public String getLastIPTDate() {
         return lastIPTDate.getAttribute("value");
     }
 
     public String getSerialNumber() {
         return serialNumber.getAttribute("value");
     }
 
     public String getRegistrationDate() {
         return registrationDate.getAttribute("value");
     }
 
     public String getEstimatedDateOfDeliveryHolder() {
         return estimatedDateOfDeliveryHolder.getAttribute("value");
     }
 
     public void submit() {
         submit.click();
         waitForSuccessfulCompletion();
     }
 
     public String motechId() {
         return motechPatientId.getAttribute("value");
     }
 
 
     @Override
     public void waitForSuccessfulCompletion() {
         elementPoller.waitForElementClassName("success", driver);
     }
 
     public String getParity() {
         return parity.getAttribute("value");
     }
 
     public String getEstimatedDateOfDelivery() {
         return estimatedDateOfDelivery.getAttribute("value");
 
     }
 
     public void save(TestANCEnrollment ancEnrollment) {
         withStaffId(ancEnrollment.staffId())
                 .withRegistrationToday(ancEnrollment.registrationToday().name())
                 .withSerialNumber(ancEnrollment.serialNumber())
                 .withCountry(ancEnrollment.country())
                 .withRegion(ancEnrollment.region())
                 .withDistrict(ancEnrollment.district())
                 .withSubDistrict(ancEnrollment.subDistrict())
                 .withRegistrationDate(ancEnrollment.registrationDate())
                 .withAddHistory(ancEnrollment.addHistory())
                 .withFacility(ancEnrollment.facility())
                 .withHeight(ancEnrollment.height())
                 .withGravida(ancEnrollment.gravida())
                 .withParity(ancEnrollment.parity())
                 .withDeliveryDateConfirmed(ancEnrollment.deliveryDateConfirmed())
                 .withEstimatedDateOfDelivery(ancEnrollment.estimatedDateOfDelivery())
                 .withIPT(ancEnrollment.hasIPTHistory())
                 .withTT(ancEnrollment.hasTTHistory());
 
         if (ancEnrollment.hasIPTHistory()) {
             withLastIPTValue(ancEnrollment.lastIPT())
                 .withIPTDate(ancEnrollment.lastIPTDate());
         }
 
         if (ancEnrollment.hasTTHistory()) {
             withLastTTValue(ancEnrollment.lastTT())
                 .withTTDate(ancEnrollment.lastTTDate());
         }
         submit();
     }
 
     public void displaying(TestANCEnrollment ancEnrollment) {
         assertEquals(getStaffId(), ancEnrollment.staffId());
         assertEquals(getSerialNumber(), ancEnrollment.serialNumber());
         assertEquals(getRegion(), ancEnrollment.region());
         assertEquals(getDistrict(), ancEnrollment.district());
         assertEquals(getSubDistrict(), ancEnrollment.subDistrict());
         assertEquals(getRegistrationDate(), ancEnrollment.registrationDate().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
         assertEquals(getFacility(), ancEnrollment.facility());
         assertEquals(getHeight(), ancEnrollment.height());
         assertEquals(getGravida(), ancEnrollment.gravida());
         assertEquals(getParity(), ancEnrollment.parity());
         assertEquals(getEstimatedDateOfDelivery(), ancEnrollment.estimatedDateOfDelivery().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
         assertEquals(getLastTTDate(), ancEnrollment.lastTTDate().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
         assertEquals(getLastTT1(), ancEnrollment.lastTT());
         assertEquals(getLastIPTDate(), ancEnrollment.lastIPTDate().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
         assertEquals(getLastIPT1(), ancEnrollment.lastIPT());
 
 
     }
 
     private ANCEnrollmentPage withLastTTValue(String ttValue) {
         if ("1".equals(ttValue)) {
             withLastTTValue1(true);
         }
         return this;
     }
 
     private ANCEnrollmentPage withLastIPTValue(String iptValue) {
         if ("1".equals(iptValue)) {
             withLastIPTValue1(true);
         }
         return this;
     }
 
     public String getRegion() {
         return region.getAttribute("value");
     }
 
     public String getDistrict() {
         return district.getAttribute("value");
     }
 
     public String getSubDistrict() {
         return subDistrict.getAttribute("value");
     }
 }
