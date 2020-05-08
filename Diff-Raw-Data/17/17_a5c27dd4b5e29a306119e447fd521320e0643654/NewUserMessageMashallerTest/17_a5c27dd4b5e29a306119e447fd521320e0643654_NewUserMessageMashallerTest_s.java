 package ar.uba.dc.seginf.sims.marshallers;
 
 import static org.junit.Assert.*;
 
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Test;
 
 import ar.uba.dc.seginf.sims.messages.NewUserMessage;
 import ar.uba.dc.seginf.sims.util.ISODateUtils;
 
 /**
  * Test para {@link NewUserMessageMarshaller}.
  * 
  * 
  * @author Juan Edi
  * @since May 28, 2012
  */
 public class NewUserMessageMashallerTest {
 
     private MessageMarshaller<NewUserMessage> marshaller = new NewUserMessageMarshaller();
     
     @Test
     public final void emptyRolesMarshallTest() throws ParseException {
         NewUserMessage msg = new NewUserMessage("juanedi", "Juan", "Edi", "MD5", "123", 
         										ISODateUtils.parse("2010-01-01"), Collections.<String>emptyList());
         String str = marshaller.marshall(msg);
         assertEquals("juanedi,Juan,Edi,MD5,123,2010-01-01,[]", str);
     }
     
     @Test
     public final void singleRoleMarshallTest() throws ParseException {
         NewUserMessage msg = new NewUserMessage("juanedi", "Juan", "Edi", "MD5", "123", 
 												ISODateUtils.parse("2010-01-01"), Arrays.asList("ROL1"));
         String str = marshaller.marshall(msg);
         assertEquals("juanedi,Juan,Edi,MD5,123,2010-01-01,[ROL1]", str);
     }
     
     @Test
     public final void multipleRolesMarshallTest() throws ParseException {
         NewUserMessage msg = new NewUserMessage("juanedi", "Juan", "Edi", "MD5", "123", 
 												ISODateUtils.parse("2010-01-01"), Arrays.asList("ROL1", "ROL2"));
         String str = marshaller.marshall(msg);
         assertEquals("juanedi,Juan,Edi,MD5,123,2010-01-01,[ROL1,ROL2]", str);
     }
     
     /** mensaje invlido */
     @Test(expected = IllegalArgumentException.class)
     public final void failedUnMarshallTest() {
         marshaller.unMarshall("juanedi,MD5,123");
     }
 
     @Test
     public final void emptyRolesUnMarshallTest() throws ParseException {
         NewUserMessage msg = marshaller.unMarshall("juanedi,Juan,Edi,MD5,123,2010-01-01,[]");
         assertEquals("juanedi", msg.getUsername());
         assertEquals("Juan", msg.getFirstName());
         assertEquals("Edi", msg.getLastName());
         assertEquals("MD5", msg.getHashType());
         assertEquals("123", msg.getPassword());
         assertEquals(ISODateUtils.parse("2010-01-01"), msg.getPasswordExpiration());
         assertEquals(0, msg.getRoles().size());
     }
     
     @Test
     public final void singleRoleUnMarshallTest() throws ParseException {
         NewUserMessage msg = marshaller.unMarshall("juanedi,Juan,Edi,MD5,123,2010-01-01,[ROL1]");
         assertEquals("juanedi", msg.getUsername());
         assertEquals("Juan", msg.getFirstName());
         assertEquals("Edi", msg.getLastName());
         assertEquals("MD5", msg.getHashType());
         assertEquals("123", msg.getPassword());
         assertEquals(1, msg.getRoles().size());
         assertEquals(ISODateUtils.parse("2010-01-01"), msg.getPasswordExpiration());
         assertEquals("ROL1", msg.getRoles().get(0));
     }
     
     @Test
     public final void multipleRolesUnMarshallTest() throws ParseException {
         NewUserMessage msg = marshaller.unMarshall("juanedi,Juan,Edi,MD5,123,2010-01-01,[ROL1,ROL2,ROL3]");
         assertEquals("juanedi", msg.getUsername());
         assertEquals("Juan", msg.getFirstName());
         assertEquals("Edi", msg.getLastName());
         assertEquals("MD5", msg.getHashType());
         assertEquals("123", msg.getPassword());
         assertEquals(3, msg.getRoles().size());
         assertEquals(ISODateUtils.parse("2010-01-01"), msg.getPasswordExpiration());
         assertEquals("ROL1", msg.getRoles().get(0));
         assertEquals("ROL2", msg.getRoles().get(1));
         assertEquals("ROL3", msg.getRoles().get(2));
     }
 }
