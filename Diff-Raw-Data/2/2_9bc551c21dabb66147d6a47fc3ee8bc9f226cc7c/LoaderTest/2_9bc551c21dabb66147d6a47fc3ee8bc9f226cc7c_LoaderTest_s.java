package gx.realtime.acceptance;
 
 import gx.realtime.RealtimeLoader;
 import gx.realtime.RealtimeOptions;
 import org.junit.Test;
 
 /**
  *
  */
 public class LoaderTest {
     @Test
     public void testLoader() {
         RealtimeOptions options = new RealtimeOptions();
         options.setAppId("foo");
         options.setClientId("bar");
 
         options.setOnFileLoaded((document) -> {
             //...
         });
 
         RealtimeLoader rtLoader = new RealtimeLoader(options);
         rtLoader.start();
     }
 }
