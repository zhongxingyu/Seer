 package org.bitrepository.protocol;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.SettingsProvider;
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 public class ProtocolComponentFactoryTest {
     private Settings settings;
     
     @BeforeMethod(alwaysRun = true)
     public void beforeMethodSetup() throws Exception {
         setupSettings();
     }
     
     protected void setupSettings() throws Exception {
        SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader("settings/xml"));
         settings = settingsLoader.getSettings("bitrepository-devel");
     }
     
     @Test(groups = { "regressiontest" })    
     /**
      * Validates that only one message bus instance is created for each collection ID.
      */
     public void getMessageTest() throws Exception {
         MessageBus bus =
             ProtocolComponentFactory.getInstance().getMessageBus(settings);
     }
 }
