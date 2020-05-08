 /*
 	
 	Copyright 2012 VU Medical Center Amsterdam
 	
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 	
 	    http://www.apache.org/licenses/LICENSE-2.0
 	
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
 	
 */
 
 package nl.vumc.trait.oc.mirth;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.parsers.ParserConfigurationException;
 
 import nl.vumc.trait.oc.connect.ConnectInfo;
 import nl.vumc.trait.oc.connect.OCConnectorException;
 import nl.vumc.trait.oc.connect.OCWebServices;
 import nl.vumc.trait.oc.odm.ClinicalODMResolver;
 import nl.vumc.trait.oc.odm.ODMException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * Class for automated data uploads. To be used by Mirth.
  * @author Arjan van der Velde (a.vandervelde (at) xs4all.nl)
  */
 public class ImportODM extends Main {
 
 	private final static int DEFAULT_EXPIRE = 60;
 
 	private class ResolverCache {
 		public long timestamp;
 		public ClinicalODMResolver resolver;
 		public ResolverCache(ClinicalODMResolver resolver, int expire) {
			updateTimeStamp();
 			this.resolver = resolver;
 		}
 		public boolean isExpired() {
 			return (System.currentTimeMillis() - timestamp) > (DEFAULT_EXPIRE * 1000);
 		}
		public void updateTimeStamp() {
			timestamp = System.currentTimeMillis();
		}
 	}
 
 	/** Per batch clinical resolver */
 	private HashMap<String, ResolverCache> resolvers; // <batch, resolver>
 
 	/**
 	 * Initiliaze importer
 	 * @throws ParserConfigurationException
 	 */
 	public ImportODM() throws ParserConfigurationException {
 		super();
 		resolvers = new HashMap<String, ResolverCache>();
 		logger.debug("ImportODM Instantiated...");
 	}
 	
 	/**
 	 * Clear all batches (resolvers)
 	 */
 	public void clearCache() {
 		resolvers = new HashMap<String, ResolverCache>();
 	}
 
 	/**
 	 * Initialize a batch -- setup resolver using connect info supplied.
 	 * @param batch batch name
 	 * @param baseURL OpenClinica web services URL
 	 * @param user OpenClinica user name
 	 * @param password OpenClinca password
 	 * @throws OCConnectorException
 	 */
 	public void setupBatch(String batch, String baseURL, String user, String password) throws OCConnectorException {
 		setupBatch(batch, baseURL, user, password, DEFAULT_EXPIRE);
 	}
 	
 	/**
 	 * Initialize a batch -- setup resolver using connect info supplied.
 	 * @param batch batch name
 	 * @param baseURL OpenClinica web services URL
 	 * @param user OpenClinica user name
 	 * @param password OpenClinca password
 	 * @param expire time to expire cached resolver
 	 * @throws OCConnectorException
 	 */
 	public void setupBatch(String batch, String baseURL, String user, String password, int expire) throws OCConnectorException {
 		logger.debug("setupBatch(): batch: " + batch);
 		ConnectInfo connectInfo = new ConnectInfo(baseURL, user);
 		connectInfo.setPassword(password);
		if (!resolvers.containsKey(batch) || resolvers.get(batch).isExpired()) {
 			try {
 				OCWebServices connector = OCWebServices.getInstance(connectInfo, debug, false);
 				resolvers.put(batch, new ResolverCache(new ClinicalODMResolver(connector), expire));
 			} catch (Exception e) {
 				throw new OCConnectorException("Cannot setup ImportODM", e);
 			}
 		} else {
			resolvers.get(batch).updateTimeStamp();
 			resolvers.get(batch).resolver.getConnector().setCredentials(connectInfo);
 		}
 	}
 
 	/**
 	 * Process an ODM message (string) for a given batch -- parse, clean ODM, create subjects, load data, etc.
 	 * @param batch batch name
 	 * @param odmInput ODM XML string
 	 * @return processed ODM
 	 * @throws ODMException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws OCConnectorException
 	 * @throws DatatypeConfigurationException
 	 */
 	public String process(String batch, String odmInput) throws ODMException, SAXException, IOException,
 			OCConnectorException, DatatypeConfigurationException {
 		logger.debug("BLA (batch): " + batch);
 		logger.debug("BLA (odm): " + odmInput);
 		InputSource reader = new InputSource(new StringReader(odmInput));
 		ClinicalODMResolver resolver;
 		logger.debug("ImportODM.process(): batch: " + batch);
 		if (resolvers.containsKey(batch)) {
 			resolver = resolvers.get(batch).resolver;
 		} else {
 			logger.debug("ImportODM.process(): batches: " + resolvers.keySet());
 			throw new OCConnectorException("No resolver for batch '" + batch + "'!");
 		}
 		resolver.setOdm(documentBuilder.parse(reader));
 		resolver.resolveOdmDocument();
 		String resolvedODM = resolver.toString();
 		Document odmDoc = resolver.getOdm();
 		Node odmNode = resolver.getOdm().getFirstChild();
 		// bulk load -- chop up into ClinicaDatas...
 		NodeList clinicalDatas = resolver.xPath(odmNode, "//ClinicalData");
 		for (int i = 0; i < clinicalDatas.getLength(); ++i) {
 			odmNode.removeChild(clinicalDatas.item(i));
 		}
 		for (int i = 0; i < clinicalDatas.getLength(); ++i) {
 			odmNode.appendChild(clinicalDatas.item(i));
 			logger.debug("============================ setOdm start =================");
 			resolver.setOdm(odmDoc); // important!
 			logger.debug("Current ODM:\n" + resolver.extraClean().toString());
 			logger.debug("============================ setOdm end ===================");
 			logger.debug("connector.import..... start");
 			logger.debug("connector.import..... connector: " + resolver.getConnector());
 			resolver.getConnector().importODM(resolver.extraClean().toString());
 			logger.debug("connector.import..... end");
 			odmNode.removeChild(clinicalDatas.item(i));
 		}
 		return resolvedODM;
 	}
 
 }
