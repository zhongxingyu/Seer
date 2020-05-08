 package com.xmlmachines.resources;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.bind.JAXBException;
 
 import org.apache.log4j.Logger;
 
 import com.marklogic.xcc.Request;
 import com.marklogic.xcc.ResultItem;
 import com.marklogic.xcc.ResultSequence;
 import com.marklogic.xcc.exceptions.RequestException;
 import com.marklogic.xcc.types.XSString;
 import com.marklogic.xcc.types.XdmItem;
 import com.sun.jersey.api.core.InjectParam;
 import com.xmlmachines.beans.jaxb.ExampleBean;
 
 @Path("/jaxb")
 @Produces({ "application/xml", "application/json" })
 public class JaxbResource extends BaseResource {
 
 	private static final Logger LOG = Logger.getLogger(JaxbResource.class
 			.getName());
 
 	@InjectParam
 	ExampleBean bean;
 
 	@GET
 	public ExampleBean getDefaultText() {
 		bean.setText("default text");
 		return bean;
 	}
 
 	@GET
 	@Path("{userInput}")
 	public ExampleBean getUserInput(@PathParam("userInput") String userInput) {
 		LOG.debug(MessageFormat.format("Returning a bean containing {0}",
 				userInput));
 		bean.setText(userInput);
 		return bean;
 	}
 
 	@GET
 	@Path("{userInput}/test")
 	@Produces({ "text/plain" })
 	public Response testXccConnection(@PathParam("userInput") String userInput) {
 
 		LOG.debug("About to connect to MarkLogic");
 		// //////////
 		Request request = session.newAdhocQuery(MessageFormat.format("\"{0}\"",
 				userInput));
 		LOG.debug(MessageFormat.format("sending {0} to ML", userInput));
 		ResultSequence rs;
 		try {
 			rs = session.submitRequest(request);
 			while (rs.hasNext()) {
 
 				ResultItem rsItem = rs.next();
 				XdmItem item = rsItem.getItem();
 
 				if (item instanceof XSString) {
 					return Response.ok(item.asString())
 							.header("Transfer-Encoding", "binary").build();
 				}
 			}
 			// ///////////
 		} catch (RequestException e) {
 			LOG.debug("Inside a request exception");
 			LOG.error(e);
 			return Response.noContent().build();
 		} finally {
 			LOG.info("closing session");
 			session.close();
 		}
 
 		LOG.error("Please check your XCC connection settings; something's not right");
 		return Response.status(500).build();
 
 	}
 
 	@POST
 	public Response persistBean() {
 
 		String guid = generateGuid();
 		LOG.debug(guid);
 		bean.setText(guid);
 		// TODO - InputStream instead?
 		String xml = marshallBeanToXmlString(bean);
        LOG.info("** Marshalled ** " + xml);
 		createDocumentInMarkLogic(guid, xml);
 
 		return Response.ok(
 				MessageFormat.format("{0} has been stored",
 						generateXmlDocumentUri(guid))).build();
 		// .header("Transfer-Encoding", "binary")
 	}
 
 	/**
 	 * Used in the unit tests - allows a specific Id to be used when saving so
 	 * we can verify easily
 	 * 
 	 * @param userInput
 	 * @return
 	 */
 	@POST
 	@Path("save/{userInput}")
 	public Response persistBean(@PathParam("userInput") String userInput) {
 		LOG.info("user input is: " + userInput);
 		bean.setText(userInput);
 		// TODO - InputStream instead?
 		String xml = marshallBeanToXmlString(bean);
 		LOG.info("XML is " + xml);
 		LOG.info("about to store doc");
 		createDocumentInMarkLogic(userInput, xml);
 		LOG.info("doc should now be stored");
 		return Response.ok(
 				generateXmlDocumentUri(userInput) + " has been stored").build();
 		// .header("Transfer-Encoding", "binary")
 	}
 
 	/**
 	 * Admittedly this is totally pointless - you wouldn't be using a webservice
 	 * to get Java Objects back only to output them as XML - but it will prove
 	 * whether the code works
 	 * 
 	 * @param docUri
 	 * @return
 	 */
 	@GET
 	@Path("{docUri}/testml")
 	public ExampleBean getUnmarshalledStuffBackFromMarkLogicAndDisplayAsXml(
 			@PathParam("docUri") String docUri) {
 		try {
 			return unmarshallXmlStringToExampleBeanObject(readDocumentFromMarkLogicAsInputStream(docUri));
 		} catch (JAXBException e) {
 			Response.status(500).build();
 			LOG.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Currently used for unit tests
 	 * 
 	 * @return
 	 */
 	@GET
 	@Path("/context/json")
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	public List<ExampleBean> getExampleBeanFromList() {
 		List<ExampleBean> ebs = new ArrayList<ExampleBean>();
 		ExampleBean e = new ExampleBean();
 		e.setText("test json or xml");
 		ebs.add(e);
 		return ebs;
 	}
 
 	@GET
 	@Path("/context/json/multiples")
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	public List<ExampleBean> getExampleBeansFromList() {
 		List<ExampleBean> ebs = new ArrayList<ExampleBean>();
 		ExampleBean e = new ExampleBean();
 		e.setText("first child in list");
 		ebs.add(e);
 		ExampleBean e2 = new ExampleBean();
 		e2.setText("second child in list");
 		ebs.add(e2);
 		return ebs;
 	}
 
 	// Service ONLY returns JSON - should 406 if you set an accept header for
 	// "text/plain" or similar
 	@GET
 	@Path("/context/jsononly")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public List<ExampleBean> getExampleBeansAsJsonOnly() {
 		List<ExampleBean> ebs = new ArrayList<ExampleBean>();
 		ExampleBean e = new ExampleBean();
 		e.setText("test json only");
 		ebs.add(e);
 		return ebs;
 	}
 }
 
 /*
  * TODO - provider output
  * 
  * @Context Providers ps; MessageBodyWriter uw =
  * ps.getMessageBodyWriter(UserInfoImpl.class, UserInfoImpl.class, new
  * Annotation[0], MediaType.APPLICATION_JSON_TYPE);
  * 
  * uw.writeTo(....)
  */
