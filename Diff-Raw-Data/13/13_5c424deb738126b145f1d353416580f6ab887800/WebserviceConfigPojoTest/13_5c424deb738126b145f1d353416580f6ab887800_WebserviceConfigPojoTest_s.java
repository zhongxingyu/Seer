 package eu.dm2e.ws.api;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.OmnomUnitTest;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
 
 public class WebserviceConfigPojoTest  extends OmnomUnitTest {
 	
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	
 	
 	@Test 
 	public void testGetParameterAssignmentForParam() throws Exception {
 		WebservicePojo wsDesc = new WebservicePojo();
 		wsDesc.setId("http://quux.bork/ws1/");
 		ParameterPojo fooParam = wsDesc.addInputParameter("foo");
 		fooParam.setIsRequired(true);
 		
 		WebserviceConfigPojo wsConf = new WebserviceConfigPojo();
 //		wsConf.setId("http://quux.bork/config/1");
 		wsConf.setWebservice(wsDesc);
 		wsConf.addParameterAssignment("foo", "bar");
 		wsConf.validate();
 	}
 
 //	@Ignore
 	@Test
 	public void testSerialization() {
         log.info("TEST: Config serislization");
         WebserviceConfigPojo config = new WebserviceConfigPojo();
         WebservicePojo ws = new WebservicePojo();
         ws.setId("http://example.org/webservice1");
         ws.addInputParameter("testparam1");
         ws.addInputParameter("testparam2");
         ws.addOutputParameter("testparam3");
         config.setWebservice(ws);
         config.addParameterAssignment("testparam1", "1");
         config.addParameterAssignment("testparam2", "2");
         config.setId("http://example.org/config1");
         Grafeo g = new GrafeoImpl();
         g.getObjectMapper().addObject(config);
         log.trace("Serialized config: " + g.getTurtle());
         WebserviceConfigPojo config2 = g.getObjectMapper().getObject(WebserviceConfigPojo.class, "http://example.org/config1");
         assert (config.getId().equals(config2.getId()));
         assert (config.getWebservice().getId().equals(config2.getWebservice().getId()));
         assert (config.getParameterValueByName("testparam1").equals("1"));
         assert (config.getParameterValueByName("testparam2").equals("2"));
         assert (config2.getParameterValueByName("testparam1").equals("1"));
         assert (config2.getParameterValueByName("testparam2").equals("2"));
     }
 	
 	@Test
 	public void testDeserializationRDF() {
 		final String confUri = "http://foo/conf";
 		final String wsUri = "http://foo/service";
 		{
 			GrafeoImpl gIn = new GrafeoImpl();
 			gIn.addTriple(confUri, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
 //			gIn.addTriple(wsUri, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE);
 			gIn.addTriple(confUri, NS.OMNOM.PROP_WEBSERVICE, wsUri);
 
 			WebservicePojo ws = new WebservicePojo();
 			ws.setId(wsUri);
 			WebserviceConfigPojo config = new WebserviceConfigPojo();
 			config.setId(confUri);
 			config.setWebservice(ws);
 			GrafeoAssert.graphContainsGraph(config.getGrafeo(), gIn);
 //			GrafeoAssert.graphsAreEquivalent(config.getGrafeo(), gIn);
 		}
 		{
 			GrafeoImpl gIn = new GrafeoImpl();
 			gIn.addTriple(confUri, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
 			gIn.addTriple(wsUri, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE);
 			gIn.addTriple(confUri, NS.OMNOM.PROP_WEBSERVICE, wsUri);
 
 			WebservicePojo ws = new WebservicePojo();
 			ws.setId(wsUri);
 			WebserviceConfigPojo config = new WebserviceConfigPojo();
 			config.setId(confUri);
 			config.setWebservice(ws);
 			GrafeoAssert.graphsAreEquivalent(config.getGrafeo(), gIn);
 		}
 	}
 	
 	@Test
 	public void testDeserializationJSON() {
 		final String confUri = "http://foo/conf";
 		final String wsUri = "http://foo/service";
 		{
 			StringBuilder sb = new StringBuilder();
 			sb.append("{")
 					.append("'id':'").append(confUri).append("'")
 					.append(",")
					.append("'webservice':").append("{")
 						.append("'id':'").append(wsUri).append("'")
 						.append("}")
 			.append("}");
 			log.debug(sb.toString());
 			
 			String jsonStr = sb.toString();
			WebserviceConfigPojo pojoParsed = jsonSerializer.deserializeFromJSON(jsonStr, WebserviceConfigPojo.class);
 			log.debug(pojoParsed.getTerseTurtle());
 
 			WebservicePojo ws = new WebservicePojo();
 			ws.setId(wsUri);
 			WebserviceConfigPojo config = new WebserviceConfigPojo();
 			config.setId(confUri);
 			config.setWebservice(ws);
 			GrafeoAssert.graphsAreEquivalent(config.getGrafeo(), pojoParsed.getGrafeo());
 		}
 	}
 
 }
