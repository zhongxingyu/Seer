 package com.brotherlogic.booser.process;
 
 import org.junit.Before;
 
 import com.brotherlogic.booser.storage.web.Downloader;
 import com.brotherlogic.untappdpicker.TestBase;
 
 public class ProcessTests extends TestBase
 {
    @Before
    public void prepareForWeb()
    {
      // Make sure we're in test mode
      Downloader.setTestMode(true);
    }
 }
