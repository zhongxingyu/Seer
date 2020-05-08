 package org.acme.embedded;
 
 import static com.sun.jersey.api.client.ClientResponse.Status.*;
 import static javax.ws.rs.core.HttpHeaders.*;
 import static org.acme.TestRepository.*;
 import static org.acme.utils.TestUtils.*;
 import static org.dynamicvalues.Dynamic.*;
 import static org.junit.Assert.*;
 import static org.virtualrepository.service.Constants.*;
 import static org.virtualrepository.service.rest.VrsMediaType.*;
 import static org.virtualrepository.service.rest.resources.Assets.*;
 
 import java.io.StringReader;
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.core.EntityTag;
 import javax.xml.bind.Unmarshaller;
 
 import org.acme.utils.TestUtils;
 import org.dynamicvalues.DynamicIO;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.virtualrepository.csv.CsvCodelist;
 import org.virtualrepository.sdmx.SdmxCodelist;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 import flexjson.JSONDeserializer;
 
 
 @RunWith(Arquillian.class)
 public class AssetsTest {
 
 
 
 	@Deployment(testable = false)
 	public static WebArchive deploy() {
 		return TestUtils.war();
 	}
 
 	@Test
 	public void inJson(@ArquillianResource URL context) throws Exception {
 
 		String outcome = call().resource(at(context, path)).accept(JMOM.type()).get(String.class);
 
 		JSONDeserializer<List<?>> deserializer = new JSONDeserializer<List<?>>();
 
 		List<?> list = deserializer.deserialize(outcome);
 
 		assertEquals(csvAssets.size() + sdmxAssets.size(), list.size());
 
 	}
 	
 	@Test
 	public void usingTheDefaultType(@ArquillianResource URL context) throws Exception {
 
 		String outcome = call().resource(at(context, path)).get(String.class);
 
 		JSONDeserializer<List<?>> deserializer = new JSONDeserializer<List<?>>();
 
 		List<?> list = deserializer.deserialize(outcome);
 
 		assertEquals(csvAssets.size() + sdmxAssets.size(), list.size());
 
 	}
 	
 	@Test
 	public void usingUnsupportedType(@ArquillianResource URL context) throws Exception {
 
 		try {
 			call().resource(at(context, path)).accept("unsupported").get(String.class);
 			fail();
 		}
 		catch(UniformInterfaceException e) {
 			assertEquals(NOT_ACCEPTABLE,e.getResponse().getClientResponseStatus());
 		}
 
 
 	}
 
 
 	@Test
 	public void withSelectedTypesInJson(@ArquillianResource URL context) throws Exception {
 
 		String outcome = call().resource(at(context, path)).queryParam(typeParam, CsvCodelist.type.name())
 				.accept(JMOM.type()).get(String.class);
 
 		JSONDeserializer<List<?>> deserializer = new JSONDeserializer<List<?>>();
 
 		List<?> list = deserializer.deserialize(outcome);
 
 		assertEquals(csvAssets.size(), list.size());
 
 	}
 
 	@Test
 	public void selectingAllTypeIsSameAsNotSelectingThemAtAll_InJson(@ArquillianResource URL context) throws Exception {
 
 		String outcome = call().resource(at(context, path)).queryParam(typeParam, CsvCodelist.type.name())
 				.queryParam(typeParam, SdmxCodelist.type.name()).accept(JMOM.type()).get(String.class);
 
 		JSONDeserializer<List<?>> deserializer = new JSONDeserializer<List<?>>();
 
 		List<?> list = deserializer.deserialize(outcome);
 
 		assertEquals(csvAssets.size() + sdmxAssets.size(), list.size());
 
 	}
 
 	@Test
 	public void selectingUnknownTypesIsa400(@ArquillianResource URL context) throws Exception {
 
 		ClientResponse response = call().resource(at(context, path)).queryParam(typeParam, "bad")
 				.accept(JMOM.type()).get(ClientResponse.class);
 		
 		assertEquals(BAD_REQUEST, response.getClientResponseStatus());
 		
 
 	}
 
 	@Test
 	public void withRefreshInJson(@ArquillianResource URL context) throws Exception {
 
 		String outcome = call().resource(at(context, path)).accept(JMOM.type()).get(String.class);
 
 		JSONDeserializer<List<?>> deserializer = new JSONDeserializer<List<?>>();
 
 		List<?> list = deserializer.deserialize(outcome);
 
 		int current = list.size();
 
 		csvAssets.add(new CsvCodelist("idnew", "standardName", 0));
 
 		outcome = call().resource(at(context, path)).accept(JMOM.type()).method("POST", String.class);
 
 		list = deserializer.deserialize(outcome);
 
 		assertEquals(current + 1, list.size());
 
 	}
 	
 	
 	@Test
 	public void conditionallyWithLastModified(@ArquillianResource URL context) throws Exception {
 
 		WebResource resource = call().resource(at(context, path));
 
 		ClientResponse response = resource.accept(xobject).get(ClientResponse.class);
 
 		Date lm = response.getLastModified();
 		
 		response = resource.header(IF_MODIFIED_SINCE,lm).get(ClientResponse.class);
 
 		assertEquals(NOT_MODIFIED,response.getClientResponseStatus());
 		
 	}
 	
 	@Test
 	public void conditionallyWithEtag(@ArquillianResource URL context) throws Exception {
 
 		WebResource resource = call().resource(at(context, path));
 
 		ClientResponse response = resource.accept(JMOM.type()).get(ClientResponse.class);
 
 		EntityTag tag = response.getEntityTag();
 		
 		response = resource.header(IF_NONE_MATCH,tag).get(ClientResponse.class);
 
 		assertEquals(NOT_MODIFIED,response.getClientResponseStatus());
 		
 	}
 	
	
 	@Test
 	public void inXml(@ArquillianResource URL context) throws Exception {
 
 		String outcome = call().resource(at(context, path)).accept(XMOM.type()).get(String.class);
 
 		Unmarshaller um = DynamicIO.newInstance().createUnmarshaller();
 
 		// parse it as clean map
 		List<?> list = valueOf(um.unmarshal(new StringReader(outcome)));
 
 		assertEquals(csvAssets.size() + sdmxAssets.size(), list.size());
 	}
 
 	@Test
 	public void inVXml(@ArquillianResource URL context) throws Exception {
 
 		String outcome = call().resource(at(context, path)).accept(XOBJECT.type()).get(String.class);
 
 		List<?> list = (List<?>) new XStream(new StaxDriver()).fromXML(outcome);
 
 		assertEquals(csvAssets.size() + sdmxAssets.size(), list.size());
 
 	}
 	
 	
 	@Test
 	public void oneInJson(@ArquillianResource URL context) throws Exception {
 
 		call().resource(at(context, path+"/"+csv_id1)).accept(JMOM.type()).get(String.class);
 		
 	}
 	
 	
 	@Test
 	public void oneInXml(@ArquillianResource URL context) throws Exception {
 
 		call().resource(at(context, path+"/"+csv_id1)).accept(XMOM.type()).get(String.class);
 
 	}
 	
 	
 	@Test
 	public void oneInSdmxML(@ArquillianResource URL context) throws Exception {
 
 		call().resource(at(context,path)).path(sdmx_id1).accept(SDMX_ML.type()).get(String.class);
 
 
 	}
 
 	@Test
 	public void tableInJson(@ArquillianResource URL context) throws Exception {
 
 		call().resource(at(context,path)).path(csv_id1).accept(JTABLE.type()).get(String.class);
 
 
 	}
 	
 	@Test
 	public void tableInXml(@ArquillianResource URL context) throws Exception {
 
 		call().resource(at(context,path)).path(csv_id1).accept(XTABLE.type()).get(String.class);
 
 	}
 	
 	@Test
 	public void tableInVXml(@ArquillianResource URL context) throws Exception {
 
 		call().resource(at(context,path)).path(csv_id1).accept(VTABLE.type()).get(String.class);
 
 	}
 
 	@Test
 	public void oneUsingUnsupportedType(@ArquillianResource URL context) throws Exception {
 
 		try {
 			call().resource(at(context,path)).path(csv_id1).accept("unsupported").get(String.class);
 			fail();
 		}
 		catch(UniformInterfaceException e) {
 			assertEquals(NOT_ACCEPTABLE,e.getResponse().getClientResponseStatus());
 		}
 
 
 	}
 	@Test
 	public void badoneInJson(@ArquillianResource URL context) throws Exception {
 
 		try {
 			call().resource(at(context, path+"/bad")).accept(JMOM.type()).get(String.class);
 			fail();
 		}
 		catch(UniformInterfaceException e) {
 			assertEquals(NOT_FOUND,e.getResponse().getClientResponseStatus());
 		}
 
 		
 	}
 
 }
