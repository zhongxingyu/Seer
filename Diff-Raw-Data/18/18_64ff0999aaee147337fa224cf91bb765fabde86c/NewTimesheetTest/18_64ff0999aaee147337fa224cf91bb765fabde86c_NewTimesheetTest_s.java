 package functional.com.thoughtworks.twu;
 
 
 import constants.TestData;
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.Select;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 
 
 public class NewTimesheetTest extends BaseTest {
 
 
     public NewTimesheetTest() throws UnknownHostException {
         super();
     }
 
     @Before
     public void setup() throws UnknownHostException {
         super.setUpAndroid();
         webDriver.get(dashboardUrl);
         super.submitCredentials(TestData.validPasswordString);
         webDriver.get(timeRecordUrl);
     }
 
     @Test
     public void shouldShowSelectCountryMessageOnPageLoad() throws UnknownHostException {
         Select selectBox = new Select(webDriver.findElement(By.id("country")));
         assertThat(selectBox.getFirstSelectedOption().getText(), is("Select a country"));
     }
 
     @Test
 
     public void checkIfCountryCanBeSelected() throws Exception {
         WebElement country = webDriver.findElement(By.id("country"));
         assertThat(country.isEnabled(), is(true));
     }
 
 
     @Test
 
     public void shouldDisplayCountryListWithBothCountryCodeAndName() throws UnknownHostException {
         List<String> obtainedCountryNames = getActualCountryList();
         List<String> expectedCountryNames = getExpectedCountryList();
         assertThat(obtainedCountryNames, is(expectedCountryNames));
     }
 
     @Test
     public void checkIfStateIsBlankInitially() throws Exception {
         WebElement state = webDriver.findElement(By.id("state"));
         Select dropDownState = new Select(state);
         assertEquals("", dropDownState.getFirstSelectedOption().getText());
     }
 
 
     @Test
     public void checkIfStateIsDisabledOnPageLoad() throws UnknownHostException {
         WebElement state = webDriver.findElement(By.id("state"));
         assertThat(state.isEnabled(), is(false));
     }
 
     @Test
     public void givenUSAIsSelectedStatesShouldBeEnabled() {
         selectCountry("USA - USA");
         WebElement state = webDriver.findElement(By.id("state"));
 
         assertThat(state.isEnabled(), is(true));
     }
 
     @Test
     public void changeCountryUSAToOtherStatesEmpty() {
         selectCountry("USA - USA");
         selectState("GA");
         selectCountry("IND - India");
         WebElement state = webDriver.findElement(By.id("state"));
         Select dropDownState = new Select(state);
         assertEquals("", dropDownState.getFirstSelectedOption().getText());
     }
 
     @Test
     public void checkIfStateListIsAvailable() throws Exception {
         selectCountry("USA - USA");
         List<String> obtainedStateNames = getActualStateList();
 
         List<String> expectedStateNames = getExpectedStateList();
         assertEquals(expectedStateNames, obtainedStateNames);
 
     }
 
     private void selectCountry(String countryName) {
         WebElement country = webDriver.findElement(By.id("country"));
         Select dropDownCountry = new Select(country);
         dropDownCountry.selectByValue(countryName);
     }
 
 
     private List<String> getExpectedCountryList() {
         List<String> expectedCountryNames = new ArrayList<String>();
 
         expectedCountryNames.add("Select a country");
         expectedCountryNames.add("AUS - Australia");
         expectedCountryNames.add("BRA - Brazil");
         expectedCountryNames.add("CAN - Canada");
         expectedCountryNames.add("CHN - China");
         expectedCountryNames.add("DEU - Germany");
         expectedCountryNames.add("GBR - UK");
         expectedCountryNames.add("IND - India");
         expectedCountryNames.add("SGP - Singapore");
         expectedCountryNames.add("UGA - Uganda");
         expectedCountryNames.add("USA - USA");
         expectedCountryNames.add("ZAF - Sth Africa");
 
         return expectedCountryNames;
     }
 
     private ArrayList<String> getActualCountryList() {
         WebElement country = webDriver.findElement(By.id("country"));
         Select dropDown = new Select(country);
         List<WebElement> options = dropDown.getOptions();
         ArrayList<String> obtainedCountryNames = new ArrayList<String>();
         for (WebElement pageCountry : options) {
             obtainedCountryNames.add(pageCountry.getText());
         }
         return obtainedCountryNames;
     }
 
     private List<String> getExpectedStateList() {
         List<String> stateList = new ArrayList<String>();
 
         stateList.add("Select a state");
         stateList.add("AK");
         stateList.add("AL");
         stateList.add("AR");
         stateList.add("AS");
         stateList.add("AZ");
         stateList.add("CA");
         stateList.add("CO");
         stateList.add("CT");
         stateList.add("DC");
         stateList.add("DE");
         stateList.add("FL");
         stateList.add("GA");
         stateList.add("GU");
         stateList.add("HI");
         stateList.add("IA");
         stateList.add("ID");
         stateList.add("IL");
         stateList.add("IN");
         stateList.add("KS");
         stateList.add("KY");
         stateList.add("LA");
         stateList.add("MA");
         stateList.add("MD");
         stateList.add("ME");
         stateList.add("MI");
         stateList.add("MN");
         stateList.add("MO");
         stateList.add("MS");
         stateList.add("MT");
         stateList.add("NC");
         stateList.add("ND");
         stateList.add("NE");
         stateList.add("NH");
         stateList.add("NJ");
         stateList.add("NM");
         stateList.add("NV");
         stateList.add("NY");
         stateList.add("OH");
         stateList.add("OK");
         stateList.add("OR");
         stateList.add("PA");
         stateList.add("PR");
         stateList.add("RI");
         stateList.add("SC");
         stateList.add("SD");
         stateList.add("TN");
         stateList.add("TX");
         stateList.add("UT");
         stateList.add("VA");
         stateList.add("VI");
         stateList.add("VT");
         stateList.add("WA");
         stateList.add("WI");
         stateList.add("WV");
         stateList.add("WY");
 
         return stateList;
     }
 
     private List<String> getActualStateList() {
         WebElement state = webDriver.findElement(By.id("state"));
         Select dropDownState = new Select(state);
         List<WebElement> states = dropDownState.getOptions();
 
 
         List<String> obtainedStateNames = new ArrayList<String>();
         for (WebElement pageState : states) {
             obtainedStateNames.add(pageState.getText());
         }
         return obtainedStateNames;
     }
 
 
     private void selectState(String stateName) {
         WebElement state = webDriver.findElement(By.id("state"));
         Select dropDownState = new Select(state);
         dropDownState.selectByValue(stateName);
     }
 
     @After
     public void tearDown() {
               webDriver.close();
     }
 
 
 }
 
