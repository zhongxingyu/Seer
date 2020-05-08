 package edu.channel4.mm.db.android.model.description;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.json.JSONException;
 
 public class EventDescriptionTest extends TestCase {
    
    private final String validTestInput = "{\"sAttrs\":[\"AndroidIDHash__c\",\"DataConnectionType__c\",\"DeviceSerialHash__c\",\"DeviceCountry__c\",\"DeviceManufacturer__c\",\"DeviceModel__c\",\"DeviceType__c\",\"EpochTime__c\",\"LanguageLocale__c\",\"LocaleCountryCode__c\",\"LocalyticsApiKey__c\",\"LocalyticsLibraryVersion__c\",\"NetworkCarrier__c\",\"NetworkCountryCode__c\",\"OSVersion__c\",\"PersistentStorageCreationTimeSeconds__c\",\"SDKCompatibility__c\",\"SessionLengthSeconds__c\",\"UUID__c\"],\"eDescs\":[{\"name\":\"button pressed\",\"eAttrs\":[\"button\",\"tr.button\"]},{\"name\":\"record name and age\",\"eAttrs\":[\"age\",\"name\"]}]}";
    private final String invalidTestString = "{\"sAttrs\":[\"AndroidIDHash__c\",\"DataConnectionType__c\",\"DeviceSerialHash__c\",\"DeviceCountry__c\",\"DeviceManufacturer__c\",\"DeviceModel__c\",\"DeviceType__c\",\"EpochTime__c\",\"LanguageLocale__c\",\"LocaleCountryCode__c\",\"LocalyticsApiKey__c\",\"LocalyticsLibraryVersion__c\",\"NetworkCarrier__c\",\"NetworkCountryCode__c\",\"OSVersion__c\",\"Persistent";
    private final String veryInvalidTestString = "HTTP 400 ERROR NOT VALID ABORT";
    
    private final String[] sessionAttributesOracle = {"AndoridIDHash__c", "DataConnectionType__c", "DeviceSerialHash__c", "DeviceCountry__c", "DeviceManufacturer__c", "DeviceModel__c", "DeviceType__c", "EpochTime__c", "LanguageLocale__c", "LocaleCountryCode__c", "LocalyticsApiKey__c", "LocalyticsLibraryVersion__c", "NetworkCarrier__c", "NetworkCountryCode__c","OSVersion__c","PersistentStorageCreationTimeSeconds__c","SDKCompatibility__c","SessionLengthSeconds__c","UUID__c"};  
    private final String[] buttonPressedAttributesOracle = {"button", "tr.button"};
    private final String[] recordNameAttributeOracle = {"age", "name"};
    
    public void setUp() throws Exception {
    }
 
    public void tearDown() throws Exception {
    }
 
    public void testParseValidJSON() {
       try {
          List<EventDescription> result = EventDescription.parseList(validTestInput);
          
          // CAUTION: assuming order of elements in list. Make it a @post of .parseList()
          
          // check session attributes
         checkEventDescription(result.get(0), "Session Attributes", sessionAttributesOracle);
                   
          // check "button pressed" event description
          checkEventDescription(result.get(1), "button pressed", buttonPressedAttributesOracle);
                   
          // check "record name and age" event description
          checkEventDescription(result.get(2), "record name and age", recordNameAttributeOracle);
       }
       catch (JSONException e) {
          fail(e.toString());
       }
    }
    
    private void checkEventDescription(EventDescription description, String eventName, String[] eventAttributeNames) {
       List<AttributeDescription> sessionAttributes = description.getAttributes();
       assertEquals(eventName, description.getName());
       assertEquals(eventAttributeNames.length, sessionAttributes.size());
       for (AttributeDescription attrib : sessionAttributes) {
          assertTrue(Arrays.asList(eventAttributeNames).contains(attrib.getName()));
       }
    }
    
    public void testParseInvalidJSON() {
       try {
          EventDescription.parseList(invalidTestString);
          fail();
       }
       catch (JSONException e) {
       }
    }
    
    public void testParseVeryInvalidJSON() {
       try {
          EventDescription.parseList(veryInvalidTestString);
          fail();
       }
       catch (JSONException e) {
       }
    }
 }
