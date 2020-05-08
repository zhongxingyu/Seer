 package cz.muni.fi.pa165.pujcovnaStroju.restclient.test;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineTypeEnumDTO;
 import cz.muni.fi.pa1685.pujcovnaStroju.restclient.util.MessageResolver;
 
 public class MessageResolverTest extends TestCase {
 
 	@Test
 	public void testWrongMessage1() {
 		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://localhost:8080/pa165/xmlt/schema.xsd\" status=\"success\"><machines2 numFound=\"0\"/></response>";
 		try {
 			MessageResolver resolver = new MessageResolver(message);
 
 		} catch (SAXException e) {
 			return;
 		} catch (IOException | ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		fail();
 	}
 
 	@Test
 	public void testWrongMessage2() {
 		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<response xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 				+ " xsi:noNamespaceSchemaLocation=\"http://localhost:8080/pa165/xmlt/schema.xsd\" status=\"success\">"
 				+ "<machines numFound=\"1\">" + "<machine2>" + "<id>1</id>"
 				+ "<label>karel</label>" + "<description>aaa</description>"
 				+ "<type>BULDOZER</type>" + "</machine2>" + "</machines>"
 				+ "</response>";
 
 		try {
 			MessageResolver resolver = new MessageResolver(message);
 		} catch (SAXException e) {
 			return;
 		} catch (IOException | ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		fail();
 	}
 
 	@Test
 	public void testMachineMarshalling1() {
 		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<response xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 				+ " xsi:noNamespaceSchemaLocation=\"http://localhost:8080/pa165/xmlt/schema.xsd\" status=\"success\">"
 				+ "<machines numFound=\"1\">" + "<machine>" + "<id>1</id>"
 				+ "<label>karel</label>" + "<description>aaa</description>"
 				+ "<type>BULDOZER</type>" + "</machine>" + "</machines>"
 				+ "</response>";
 
 		try {
 			MessageResolver resolver = new MessageResolver(message);
 			MachineDTO machine = new MachineDTO();
 			machine.setId(1L);
 			machine.setLabel("karel");
 			machine.setDescription("aaa");
 			MachineTypeEnumDTO type = new MachineTypeEnumDTO();
 			type.setTypeLabel("BULDOZER");
 			machine.setType(type);
 
 			List<? extends Object> response = resolver.getResponse();
 			assertEquals(1, response.size());
 			Object recivedMachine = response.get(0);
 
 			if (recivedMachine instanceof MachineDTO) {
 				assertEquals(machine, (MachineDTO) response.get(0));
 			} else {
 				fail();
 			}
 
		} catch (Exception e) {
			fail();
 		}
 	}
 
 	@Test
 	public void testMachineMarshalling2() {
 		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<response xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 				+ " xsi:noNamespaceSchemaLocation=\"http://localhost:8080/pa165/xmlt/schema.xsd\" status=\"success\">"
 				+ "<machine>" + "<id>1</id>" + "<label>karel</label>"
 				+ "<description>aaa</description>" + "<type>BULDOZER</type>"
 				+ "</machine>" + "</response>";
 
 		try {
 			MessageResolver resolver = new MessageResolver(message);
 			MachineDTO machine = new MachineDTO();
 			machine.setId(1L);
 			machine.setLabel("karel");
 			machine.setDescription("aaa");
 			MachineTypeEnumDTO type = new MachineTypeEnumDTO();
 			type.setTypeLabel("BULDOZER");
 			machine.setType(type);
 
 			List<? extends Object> response = resolver.getResponse();
 			assertEquals(1, response.size());
 			Object recivedMachine = response.get(0);
 
 			if (recivedMachine instanceof MachineDTO) {
 				assertEquals(machine, (MachineDTO) response.get(0));
 			} else {
 				fail();
 			}
 
		} catch (Exception e) {
			fail();
 		}
 	}
 
 }
