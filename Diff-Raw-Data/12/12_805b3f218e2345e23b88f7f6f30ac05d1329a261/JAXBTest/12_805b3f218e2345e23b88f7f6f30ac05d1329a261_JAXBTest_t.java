 package org.opennms.netmgt.model.ncs;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.UnsupportedEncodingException;
import java.net.URL;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.junit.Test;
 import org.opennms.netmgt.model.ncs.NCSBuilder;
 import org.opennms.netmgt.model.ncs.NCSComponent;
 import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
 import org.xml.sax.SAXException;
 
 public class JAXBTest {
 	
 
 
 	@Test
 	public void testMarshall() throws JAXBException, UnsupportedEncodingException, SAXException {
 		
 		final String expectedXML = "" +
 				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
 				"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"Service\" foreignId=\"123\" foreignSource=\"NA-Service\">\n" + 
 				"    <name>CokeP2P</name>\n" + 
 				"    <component type=\"ServiceElement\" foreignId=\"8765:1234\" foreignSource=\"NA-ServiceElement\">\n" + 
 				"        <name>PE1:ge-1/0/2</name>\n" + 
 				"        <component type=\"ServiceElementComponent\" foreignId=\"8765:ge-1/0/2.50\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"            <name>ge-1/0/2.50</name>\n" + 
 				"            <component type=\"PhysicalInterface\" foreignId=\"8765:ifIndex-1\" foreignSource=\"NA-PhysIfs\">\n" + 
 				"                <name>ge-1/0/2</name>\n" + 
 				"            </component>\n" + 
 				"        </component>\n" + 
 				"        <component type=\"ServiceElementComponent\" foreignId=\"8765:vcid(50)\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"            <name>PE1:vcid(50)</name>\n" + 
 				"            <dependenciesRequired>ANY</dependenciesRequired>\n" + 
 				"            <attributes>\n" + 
 				"                <attribute>\n" + 
 				"                    <key>jnxVpnPwVpnType</key>\n" + 
 				"                    <value>5</value>\n" + 
 				"                </attribute>\n" + 
 				"                <attribute>\n" + 
 				"                    <key>jnxVpnPwVpnName</key>\n" + 
 				"                    <value>ge-1/0/2.2</value>\n" + 
 				"                </attribute>\n" + 
 				"            </attributes>\n" + 
 				"            <component type=\"ServiceElementComponent\" foreignId=\"8765:LSP-1234\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"                <name>lspA-PE1-PE2</name>\n" + 
 				"            </component>\n" + 
 				"            <component type=\"ServiceElementComponent\" foreignId=\"8765:LSP-4321\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"                <name>lspB-PE1-PE2</name>\n" + 
 				"            </component>\n" + 
 				"        </component>\n" + 
 				"    </component>\n" + 
 				"    <component type=\"ServiceElement\" foreignId=\"9876:4321\" foreignSource=\"NA-ServiceElement\">\n" + 
 				"        <name>PE2:ge-3/1/4</name>\n" + 
 				"        <component type=\"ServiceElementComponent\" foreignId=\"9876:ge-3/1/4.50\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"            <name>ge-3/1/4.50</name>\n" + 
 				"            <component type=\"PhysicalInterface\" foreignId=\"9876:ifIndex-3\" foreignSource=\"NA-PhysIfs\">\n" + 
 				"                <name>ge-3/1/4</name>\n" + 
 				"            </component>\n" + 
 				"        </component>\n" + 
 				"        <component type=\"ServiceElementComponent\" foreignId=\"9876:vcid(50)\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"            <name>PE2:vcid(50)</name>\n" + 
 				"            <dependenciesRequired>ANY</dependenciesRequired>\n" + 
 				"            <attributes>\n" + 
 				"                <attribute>\n" + 
 				"                    <key>jnxVpnPwVpnType</key>\n" + 
 				"                    <value>5</value>\n" + 
 				"                </attribute>\n" + 
 				"                <attribute>\n" + 
 				"                    <key>jnxVpnPwVpnName</key>\n" + 
 				"                    <value>ge-3/1/4.2</value>\n" + 
 				"                </attribute>\n" + 
 				"            </attributes>\n" + 
 				"            <component type=\"ServiceElementComponent\" foreignId=\"9876:LSP-1234\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"                <name>lspA-PE2-PE1</name>\n" + 
 				"            </component>\n" + 
 				"            <component type=\"ServiceElementComponent\" foreignId=\"9876:LSP-4321\" foreignSource=\"NA-SvcElemComp\">\n" + 
 				"                <name>lspB-PE2-PE1</name>\n" + 
 				"            </component>\n" + 
 				"        </component>\n" + 
 				"    </component>\n" + 
 				"</component>\n" + 
 				"";
 		
 		NCSComponent svc = new NCSBuilder("Service", "NA-Service", "123")
 		.setName("CokeP2P")
 		.pushComponent("ServiceElement", "NA-ServiceElement", "8765:1234")
 			.setName("PE1:ge-1/0/2")
 			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:ge-1/0/2.50")
 				.setName("ge-1/0/2.50")
 				.pushComponent("PhysicalInterface", "NA-PhysIfs", "8765:ifIndex-1")
 					.setName("ge-1/0/2")
 				.popComponent()
 			.popComponent()
 			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:vcid(50)")
 				.setName("PE1:vcid(50)")
 				.setAttribute("jnxVpnPwVpnType", "5")
 				.setAttribute("jnxVpnPwVpnName", "ge-1/0/2.2")
 				.setDependenciesRequired(DependencyRequirements.ANY)
 				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:LSP-1234")
 					.setName("lspA-PE1-PE2")
 				.popComponent()
 				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:LSP-4321")
 					.setName("lspB-PE1-PE2")
 				.popComponent()
 			.popComponent()
 		.popComponent()
 		.pushComponent("ServiceElement", "NA-ServiceElement", "9876:4321")
 			.setName("PE2:ge-3/1/4")
 			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:ge-3/1/4.50")
 				.setName("ge-3/1/4.50")
 				.pushComponent("PhysicalInterface", "NA-PhysIfs", "9876:ifIndex-3")
 					.setName("ge-3/1/4")
 				.popComponent()
 			.popComponent()
 			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:vcid(50)")
 				.setName("PE2:vcid(50)")
 				.setAttribute("jnxVpnPwVpnType", "5")
 				.setAttribute("jnxVpnPwVpnName", "ge-3/1/4.2")
 				.setDependenciesRequired(DependencyRequirements.ANY)
 				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:LSP-1234")
 					.setName("lspA-PE2-PE1")
 				.popComponent()
 				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:LSP-4321")
 					.setName("lspB-PE2-PE1")
 				.popComponent()
 			.popComponent()
 		.popComponent()
 		.get();
 		
 		// Create a Marshaller
 		JAXBContext context = JAXBContext.newInstance(NCSComponent.class);
 		Marshaller marshaller = context.createMarshaller();
 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 		
 		// save the output in a byte array
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 		// marshall the output
 		marshaller.marshal(svc, out);
 
 		// verify its matches the expected results
 		byte[] utf8 = out.toByteArray();
 
 		String result = new String(utf8, "UTF-8");
 		assertEquals(expectedXML, result);
 		
 		System.err.println(result);
 		
 		// unmarshall the generated XML
		
		URL xsd = getClass().getResource("/ncs-model.xsd");
		
		assertNotNull(xsd);
 		
 		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 		Schema schema = schemaFactory.newSchema(xsd);
 		
 		Unmarshaller unmarshaller = context.createUnmarshaller();
 		unmarshaller.setSchema(schema);
 		Source source = new StreamSource(new ByteArrayInputStream(utf8));
 		NCSComponent read = unmarshaller.unmarshal(source, NCSComponent.class).getValue();
 		
 		assertNotNull(read);
 		
 		// round trip back to XML and make sure we get the same thing
 		ByteArrayOutputStream reout = new ByteArrayOutputStream();
 		marshaller.marshal(read, reout);
 		
 		String roundTrip = new String(reout.toByteArray(), "UTF-8");
 		
 		assertEquals(expectedXML, roundTrip);
 	}
 
 }
