 package eu.kratochvil.util;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Jiri Kratochvil (jiri.kratochvil@jetminds.com)
  * @version $Revision:$
  */
 public class StringUtilsTest {
 
     public final String XML_EXAMPLE1 = "<?xml version='1.0' encoding='UTF-8'?>" +
             "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
             "<S:Body><ns2:sayHelloResponse xmlns:ns2=\"http://vinodsingh.com\">" +
             "<return>Hello Test!</return>" +
             "</ns2:sayHelloResponse></S:Body></S:Envelope>";
     public final String XML_EXAMPLE2 = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
             "<S:Body><ns2:sayHelloResponse xmlns:ns2=\"http://vinodsingh.com\">" +
             "<return>Hello Test!</return>" +
             "</ns2:sayHelloResponse></S:Body></S:Envelope>";
     public final String NON_XML_EXAMPLE1 = "<?xml version='1.0' encoding='UTF-8'?>" +
             "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
             "<S:Body><ns2:sayHelloResponse xmlns:ns2=\"http://vinodsingh.com\">" +
             "<return>Hello Test!</return>" +
             "</ns2:sayHelloResponse></S:Body></S:EnvelopeX>";
     public final String NON_XML_EXAMPLE2 = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
             "<S:Body><ns2:sayHelloResponse xmlns:ns2=\"http://vinodsingh.com\">" +
             "<return>Hello Test!</return>" +
             "</ns2:sayHelloResponse></S:Body></S:EnvelopeX>";
     public final String NON_XML_EXAMPLE3 = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">";
 
     @Test
     public void testIsXml() throws Exception {
         assertTrue(StringUtils.isXmlLike(XML_EXAMPLE1));
         assertTrue(StringUtils.isXmlLike(XML_EXAMPLE2));
     }
 
     @Test
     public void testIsNotXml() throws Exception {
         assertFalse(StringUtils.isXmlLike(NON_XML_EXAMPLE1));
         assertFalse(StringUtils.isXmlLike(NON_XML_EXAMPLE2));
         assertFalse(StringUtils.isXmlLike(NON_XML_EXAMPLE3));
     }
 
     @Test
     public void testNotBlankValue() throws Exception {
         assertEquals("TEST", StringUtils.notBlankValue("TEST", "ANOTHER"));
         assertEquals("ANOTHER", StringUtils.notBlankValue("", "ANOTHER"));
         assertEquals("ANOTHER", StringUtils.notBlankValue(null, "ANOTHER"));
     }
 
     @Test
     public void testPathTransformation() throws Exception {
         assertEquals("aaaa.bbbb.cccc", StringUtils.transformToPackageName("/aaaa/bbbb/cccc"));
         assertEquals("aaaa.bbbb.cccc", StringUtils.transformToPackageName("aaaa/bbbb/cccc"));
         assertEquals("aaaa.bbbb.cccc", StringUtils.transformToPackageName("\\aaaa\\bbbb\\cccc"));
         assertEquals("aaaa.bbbb.cccc.N_jak__nesmysl", StringUtils.transformToPackageName("/aaaa/bbbb/cccc/Nějaký-nesmysl"));
     }
 
     @Test
     public void testNullValue() throws Exception {
         assertEquals("abcd", StringUtils.nullValue("abcd", "efgh"));
         assertEquals("efgh", StringUtils.nullValue(null, "efgh"));
         assertEquals(null, StringUtils.nullValue(null, null));
     }

    @Test
    public void testWithArgs1() throws Exception {
        assertEquals("Hi there!", StringUtils.withArgs("Hi {}!", "there"));
    }

    @Test
    public void testWithArgs2() throws Exception {
        assertEquals("Hi Tom, hello James!", StringUtils.withArgs("Hi {}, hello {}!", "Tom", "James"));
    }

    @Test
    public void testWithArgs3() throws Exception {
        assertEquals("Hi Tom, hello James! I am Jiri...", StringUtils.withArgs("Hi {}, hello {}! I am {}...", new String[]{"Tom", "James", "Jiri"}));
    }
 }
