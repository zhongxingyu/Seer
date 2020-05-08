 package integration;
 
 import br.com.caelum.seleniumdsl.table.DefaultTable;
 import br.com.caelum.seleniumdsl.table.Row;
 import br.com.caelum.seleniumdsl.table.Table;
 import com.thoughtworks.selenium.Selenium;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import se.citerus.dddsample.domain.model.weather.Weather;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static integration.RowMatcher.matchesCargoInfo;
 import static java.lang.String.format;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 class ApplicationRunner {
     public static class CargoInfo {
         private final String id, origin, destination, routed;
 
         public CargoInfo(String id, String origin, String destination, String routed) {
             this.id = id;
             this.origin = origin;
             this.destination = destination;
             this.routed = routed;
         }
 
         @Override
         public boolean equals(Object obj) {
             return EqualsBuilder.reflectionEquals(this, obj);
         }
 
         @Override
         public int hashCode() {
             return HashCodeBuilder.reflectionHashCode(this);
         }
     }
 
     private static final Pattern TRACKING_ID_PATTERN = Pattern.compile("Details for cargo (\\w+)");
     private static final String DATE_BOOKING_FORMAT = "M/d/yyyy";
     private static final String DATE_DISPLAY_FORMAT = "yyyy-MM-dd";
     private static final String DEFAULT_TIMEOUT = "30000";
     private static final String WEATHER_FORMAT = "Current weather in %s: %s (%d°C) Humidity: %d%%";
     private static final String DATE_DESTINATION_FORMAT = "Estimated time of arrival in %s: %s";
     private static final String STATUS_FORMAT = "Cargo %s is now: %s";
 
     private final Selenium selenium;
 
     ApplicationRunner(Selenium selenium) {
         this.selenium = selenium;
     }
 
     // ----------------------------------------------------------------------
     // COMMANDS
     // -----------------------------------------------------------------------
 
     String bookNewCargo(String origin, String destination, Date arrivalDate){
 		selenium.open("/");
 		selenium.click("link=booking and routing");
         selenium.waitForPageToLoad(DEFAULT_TIMEOUT);
 		selenium.click("link=Book new cargo");
 		selenium.waitForPageToLoad(DEFAULT_TIMEOUT);
 		selenium.select("originUnlocode", "label=" + origin);
 		selenium.select("destinationUnlocode", "label=" + destination);
 		selenium.type("arrivalDeadline", dateToType(arrivalDate));
 		selenium.click("//input[@value='Book']");
 		selenium.waitForPageToLoad(DEFAULT_TIMEOUT);
         return cargoTrackingId("//div[@id='container']/table/caption");
     }
 
     void routeCargo(String identifier){
         selenium.open("/admin/show.html?trackingId=" + identifier);
         selenium.click("link=Route this cargo");
         selenium.waitForPageToLoad("30000");
         selenium.click("//input[@value='Assign cargo to this route']");
         selenium.waitForPageToLoad("30000");
     }
 
     void changeCargoDestination(String identifier, String destination){
         selenium.open("/admin/show.html?trackingId=" + identifier);
         selenium.click("link=Change destination");
         selenium.waitForPageToLoad("30000");
         selenium.select("unlocode", "label=" + destination);
         selenium.click("//input[@value='Change destination']");
         selenium.waitForPageToLoad("30000");
     }
 
     void listAllCargos(){
         selenium.open("/admin/list");
     }
 
     void showCargoByTrackingId(String identifier){
         selenium.open("/public/track");
         selenium.type("idInput", identifier);
         selenium.click("//input[@value='Track!']");
         selenium.waitForPageToLoad("30000");
     }
     
     // ----------------------------------------------------------------------
     // ASSERTIONS
     // -----------------------------------------------------------------------
 
     void hasShownCargoOrigin(String unlocode){
         String text = selenium.getText("//div[@id='container']/table/tbody/tr[1]/td[2]");
         assertThat(text, is(unlocode));
     }
 
     void hasShownCargoDestination(String unlocode){
         String text = selenium.getText("//div[@id='container']/table/tbody/tr[2]/td[2]");
         assertThat(text, is(unlocode));
     }
 
     void hasShownArrivalDate(Date date){
         String text = selenium.getText("//div[@id='container']/table/tbody/tr[4]/td[2]");
        assertThat(text, is(dateToDisplay(date)));
     }
 
     void hasShownThatCargoIsNotRouted(){
         String text = selenium.getText("//div[@id='container']/p[2]/strong");
         assertThat(text, is("Not routed"));
     }
 
     void hasShownThatCargoIsMisrouted(){
         String text = selenium.getText("/div[@id='container']/p[2]/em");
         assertThat(text, is("Cargo is misrouted"));
     }
 
     void hasShownCargosList(List<CargoInfo> infos){
         Table allCargos = new DefaultTable(selenium, "allcargos");
 
         for (int i = 1; i < allCargos.getRowCount(); i++) {
             Row row = allCargos.row(i);
             assertThat(row, matchesCargoInfo(infos.get(i-1)));
         }
     }
 
     void hasShownWeatherInDestination(Weather weather, String destination){
         String text = selenium.getText("id=weather");
         assertThat(text, is(weatherText(destination, weather)));
     }
 
     void hasShownArrivalDateInDestination(Date date, String destination){
         String text = selenium.getText("id=destination");
         assertThat(text, is(destinationText(destination, date)));
     }
 
     void hasShownStatusForCargo(String status, String identifier){
         String text = selenium.getText("id=destination");
         assertThat(text, is(statusText(identifier, status)));
     }
 
     void hasShownErrorMessage(String message){
         String text = selenium.getText("id=destination");
         assertThat(text, is(message));        
     }
 
     void hasShownThatNoRoutesWereFound(){
         String text = selenium.getText("//p[@id='noroutes']");
         assertThat(text, is("No routes found that satisfy the route specification. " +
                 "Try setting an arrival deadline futher into the future (a few weeks at least). "));
     }
 
     // -----------------------------------------------------------------------
     // UTILITIES
     // ----------------------------------------------------------------------
 
     private String cargoTrackingId(String locator){
         Matcher matcher = TRACKING_ID_PATTERN.matcher(selenium.getText(locator));
         if (!matcher.matches())
             fail("Tracking ID could not be found on the page.");
         return matcher.group(1);
     }
 
     private static String dateToType(Date date){
         return new SimpleDateFormat(DATE_BOOKING_FORMAT).format(date);
     }
 
     private static String dateToDisplay(Date date){
         return new SimpleDateFormat(DATE_DISPLAY_FORMAT).format(date);
     }
 
     private String weatherText(String destination, Weather weather){
         return format(WEATHER_FORMAT, destination, weather.getCondition(), weather.getTemperature(), weather.getHumidity());
     }
 
     private String destinationText(String destination, Date date){
         String dt = new SimpleDateFormat(DATE_DISPLAY_FORMAT).format(date);
         return format(DATE_DESTINATION_FORMAT, destination, dt);
     }
 
     private String statusText(String identifier, String status){
         return format(STATUS_FORMAT, identifier, status);        
     }
 }
