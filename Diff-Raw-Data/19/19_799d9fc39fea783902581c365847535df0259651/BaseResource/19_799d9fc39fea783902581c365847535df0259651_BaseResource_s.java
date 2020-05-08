 package com.xmlmachines.resources;
 
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.text.MessageFormat;
 import java.util.UUID;
 
 import javax.ws.rs.core.Context;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.log4j.Logger;
 
 import com.marklogic.xcc.ContentCreateOptions;
 import com.marklogic.xcc.ContentFactory;
 import com.marklogic.xcc.Request;
 import com.marklogic.xcc.ResultItem;
 import com.marklogic.xcc.ResultSequence;
 import com.marklogic.xcc.Session;
 import com.marklogic.xcc.exceptions.RequestException;
 import com.marklogic.xcc.types.XdmItem;
 import com.xmlmachines.beans.jaxb.ExampleBean;
 
 public class BaseResource {
 
 	@Context
 	Session session;
 
 	private static final Logger LOG = Logger.getLogger(BaseResource.class
 			.getName());
 
 	public String generateGuid() {
 		return UUID.randomUUID().toString();
 	}
 
 	public String generateXmlDocumentUri(String baseUri) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(baseUri).append(".xml");
 		return sb.toString();
 	}
 
 	/**
 	 * Used for serialising the bean for saving to MarkLogic using xcc/j - less
 	 * efficient than it should be because the JAXBContext needs to be created
 	 * each time a doc gets saved
 	 * 
 	 * @param bean
 	 * @return
 	 */
 	public String marshallBeanToXmlString(Object bean) {
 		StringWriter sw = new StringWriter();
 		try {
 			JAXBContext jc = JAXBContext.newInstance(bean.getClass());
 			Marshaller m = jc.createMarshaller();
 			m.marshal(bean, sw);
 		} catch (JAXBException e) {
 			LOG.error(e);
 		}
 		LOG.info(sw.toString());
 		return sw.toString();
 	}
 
 	/**
 	 * TODO - how do we do this more effectively ?!?
 	 * 
 	 * @param xml
 	 * @return
 	 * @throws JAXBException
 	 */
 	public ExampleBean unmarshallXmlStringToExampleBeanObject(InputStream is)
 			throws JAXBException {
 		JAXBContext jc = JAXBContext.newInstance(ExampleBean.class);
 		Unmarshaller unmarshaller = jc.createUnmarshaller();
 		ExampleBean eb = (ExampleBean) unmarshaller.unmarshal(is);
 		return eb;
 	}
 
 	/**
 	 * Stores an XML Document in MarkLogic
 	 * 
 	 * @param baseDocumentUri
 	 * @param xmlDoc
 	 */
 	public void createDocumentInMarkLogic(String baseDocumentUri, String xmlDoc) {
 		ContentCreateOptions cco = new ContentCreateOptions();
 		try {
			LOG.debug(MessageFormat.format("attempting to store doc: {0}",
 					generateXmlDocumentUri(baseDocumentUri)));
 			session.insertContent(ContentFactory.newContent(
 					generateXmlDocumentUri(baseDocumentUri), xmlDoc, cco));
			session.commit();
 		} catch (RequestException e) {
 			LOG.error(e);
 		} finally {
 			session.close();
 		}
 	}
 
 	/**
 	 * Returns an InputStream from MarkLogic
 	 */
 	public InputStream readDocumentFromMarkLogicAsInputStream(
 			String baseDocumentUri) {
 
 		InputStream is = null;
 		StringBuilder simpleQuery = new StringBuilder();
 		simpleQuery.append("xquery version \"1.0-ml\";\n");
 		simpleQuery.append("fn:doc(\"")
 				.append(generateXmlDocumentUri(baseDocumentUri)).append("\")");
 		LOG.info("XQuery: " + simpleQuery.toString());
 
 		Request request = session.newAdhocQuery(MessageFormat.format("{0}",
 				simpleQuery.toString()));
 		LOG.debug(MessageFormat.format("requesting {0} from ML",
 				generateXmlDocumentUri(baseDocumentUri)));
 		ResultSequence rs;
 
 		try {
 			rs = session.submitRequest(request);
 
 			while (rs.hasNext()) {
 				ResultItem rsItem = rs.next();
 				XdmItem item = rsItem.getItem();
 				is = item.asInputStream();
 				LOG.debug(MessageFormat.format("XML: {0}", item.asString()));
 			}
 
 		} catch (RequestException e) {
 			LOG.error(e);
 		} finally {
 			session.close();
 		}
 
 		return is;
 	}
 
 }
