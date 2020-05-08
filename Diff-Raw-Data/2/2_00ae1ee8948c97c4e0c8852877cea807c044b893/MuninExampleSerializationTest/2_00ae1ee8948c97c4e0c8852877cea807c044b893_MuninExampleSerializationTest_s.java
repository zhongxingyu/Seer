 package uk.co.benjiweber.puppetsafe.serializer;
 
import static junit.framework.Assert.assertEquals;
 
 import org.junit.Test;
 
 import uk.co.benjiweber.puppetsafe.examples.Munin;
 
 public class MuninExampleSerializationTest {
 
     ClassSerializer serializer = new ClassSerializer();
 
     @Test
     public void ensureExampleSerializedAsExpected() {
          assertEquals("class Munin {\n" +
                  "\n" +
                  "\tpackage { 'munin':\n" +
                  "\t\tensure => 'installed',\n" +
                  "\t}\n" +
                  "\n" +
                  "\tpackage { 'munin-common':\n" +
                  "\t\tensure => 'latest',\n" +
                  "\t\trequire => Package['munin'],\n" +
                  "\t}\n" +
                  "\n" +
          "}", serializer.serialize(Munin.class));
     }
 	
 }
