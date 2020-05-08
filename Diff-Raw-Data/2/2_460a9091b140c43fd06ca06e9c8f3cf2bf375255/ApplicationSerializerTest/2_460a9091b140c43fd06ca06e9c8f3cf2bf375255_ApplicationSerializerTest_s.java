 package com.cedarsoft.serialization;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.app.Application;
 import org.jetbrains.annotations.NotNull;
 
 /**
  *
  */
 public class ApplicationSerializerTest extends AbstractXmlSerializerTest<Application> {
   @NotNull
   @Override
   protected ApplicationSerializer getSerializer() {
     return new ApplicationSerializer( new VersionSerializer() );
   }
 
   @NotNull
   @Override
   protected Application createObjectToSerialize() {
     return new Application( "gimp", new Version( 1, 2, 3 ) );
   }
 
   @NotNull
   @Override
   protected String getExpectedSerialized() {
     return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<application>\n" +
       "  <name>gimp</name>\n" +
       "  <version>1.2.3</version>\n" +
       "</application>";
   }
 }
