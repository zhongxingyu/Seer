 package edu.arizona.sirls.server.bioportal;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import edu.arizona.sirls.server.db.ToOntologiesDAO;
 import edu.arizona.sirls.shared.beans.to_ontologies.OntologySubmission;
 
 import bioportal.beans.Filter;
 import bioportal.beans.ProvisionalTerm;
 import bioportal.beans.response.Entry;
 import bioportal.beans.response.Relations;
 import bioportal.beans.response.Success;
 import bioportal.client.BioPortalClient;
 
 public class TermsToOntologiesClient {
 
 	private BioPortalClient bioPortalClient;
 	private String bioportalUserID;
 
 	public TermsToOntologiesClient(String bioportalUserId,
 			String bioportalAPIKey) throws IOException {
 		this.bioportalUserID = bioportalUserId;
 		ClassLoader loader = Thread.currentThread().getContextClassLoader();
 		Properties properties = new Properties();
 		properties.load(loader.getResourceAsStream("config.properties"));
 		String url = properties.getProperty("bioportalUrl");
 		bioPortalClient = new BioPortalClient(url, bioportalUserId,
 				bioportalAPIKey);
 	}
 
 	public int getPermanentIDs() throws SQLException, JAXBException,
 			ClassNotFoundException, IOException {
 		int count = 0;
 		try {
 			List<OntologySubmission> pendingSubmissions = ToOntologiesDAO
 					.getInstance().getPendingOntologySubmissions();
 			for (OntologySubmission submission : pendingSubmissions) {
 				String permanentId = null;
 				Success success = bioPortalClient.getProvisionalTerm(submission
 						.getTmpID());
 				List<Object> fullIdOrIdOrLabels = success.getData()
 						.getClassBean().getFullIdOrIdOrLabel();
 				for (Object fullIdOrIdOrLabel : fullIdOrIdOrLabels) {
 					if (fullIdOrIdOrLabel instanceof Relations) {
 						Relations relations = (Relations) fullIdOrIdOrLabel;
 						List<Entry> entries = relations.getEntry();
 						for (Entry entry : entries) {
 							List<Object> objects = entry.getStringOrList();
 							if (objects.size() >= 2
 									&& objects.get(0).equals(
 											"provisionalPermanentId")) {
 								permanentId = (String) objects.get(1);
 							}
 						}
 					}
 				}
 
 				if (permanentId == null)
 					continue;
 				else {
 					submission.setPermanentID(permanentId);
 					ToOntologiesDAO.getInstance()
 							.updatePermanentIDOfSubmission(
 									Integer.parseInt(submission
 											.getSubmissionID()));
 					count++;
 				}
 			}
 		} catch (Exception e) {
 			System.out.println("Error in checking adopted submissions: " + e);
 			return -1;
 		}
 
 		return count;
 	}
 
 	/**
 	 * Refresh all the awaiting submissions: try to get permanent id for them
 	 * 
 	 * @return Map<Temporary ID, Permanent ID> of newly discovered adoptions
 	 * @throws Exception
 	 */
 	public Map<String, String> checkTermAdoptions() throws Exception {
 		Map<String, String> result = new HashMap<String, String>();
 		List<OntologySubmission> pendingSubmissions = ToOntologiesDAO
 				.getInstance().getPendingOntologySubmissions();
 		for (OntologySubmission provisionalTerm : pendingSubmissions) {
 			String permanentId = null;
 			Success success = bioPortalClient
 					.getProvisionalTerm(provisionalTerm.getTmpID());
 			List<Object> fullIdOrIdOrLabels = success.getData().getClassBean()
 					.getFullIdOrIdOrLabel();
 			for (Object fullIdOrIdOrLabel : fullIdOrIdOrLabels) {
 				if (fullIdOrIdOrLabel instanceof Relations) {
 					Relations relations = (Relations) fullIdOrIdOrLabel;
 					List<Entry> entries = relations.getEntry();
 					for (Entry entry : entries) {
 						List<Object> objects = entry.getStringOrList();
 						if (objects.size() >= 2
 								&& objects.get(0).equals(
 										"provisionalPermanentId")) {
 							permanentId = (String) objects.get(1);
 						}
 					}
 				}
 			}
 
 			if (permanentId == null)
 				continue;
 			else {
 				provisionalTerm.setPermanentID(permanentId);
 				ToOntologiesDAO.getInstance().updatePermanentIDOfSubmission(
 						Integer.parseInt(provisionalTerm.getSubmissionID()));
 				result.put(provisionalTerm.getTmpID(), permanentId);
 			}
 		}
 		return result;
 	}
 
 	private String getIdFromSuccessfulCreate(Success createSuccess,
 			String idName) {
 		List<Object> fullIdOrIdOrLabel = createSuccess.getData().getClassBean()
 				.getFullIdOrIdOrLabel();
 		for (Object object : fullIdOrIdOrLabel) {
 			if (object instanceof JAXBElement) {
 				JAXBElement<String> possibleIdElement = (JAXBElement<String>) object;
 				if (possibleIdElement.getName().toString().equals(idName)) {
 					return possibleIdElement.getValue();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * submit a term to bioportal
 	 * 
 	 * @param provisionalTerm
 	 * @return temporary id given to the provided provisionalTerm
 	 * @throws Exception
 	 */
 	public String submitTerm(OntologySubmission submission) throws Exception {
 		String definitionToSubmit = submission.getDefinition() + " "
 				+ "[this term has been used in sentence '"
 				+ submission.getSampleSentence() + "' in source '"
 				+ submission.getSource() + "']";
 		ProvisionalTerm termForSubmit = new ProvisionalTerm(
 				submission.getTerm(), definitionToSubmit,
 				submission.getSuperClass(), submission.getSynonyms(),
 				submission.getOntologyID(), bioportalUserID,
 				submission.getTmpID(), submission.getPermanentID());
 
 		// interact with the server
 		Success success = bioPortalClient.createProvisionalTerm(termForSubmit);
 		String temporaryId = getIdFromSuccessfulCreate(success, "id");
 
 		// modify local database
 		submission.setTmpID(temporaryId);
 		return temporaryId;
 	}
 
 	public void updateTerm(OntologySubmission submission) throws JAXBException,
 			SQLException, ClassNotFoundException, IOException {
 		String definitionToSubmit = submission.getDefinition() + " "
 				+ "[this term has been used in sentence '"
 				+ submission.getSampleSentence() + "' in source '"
 				+ submission.getSource() + "']";
 		ProvisionalTerm termForSubmit = new ProvisionalTerm(
 				submission.getTerm(), definitionToSubmit,
 				submission.getSuperClass(), submission.getSynonyms(),
 				submission.getOntologyID(), bioportalUserID,
 				submission.getTmpID(), submission.getPermanentID());
 		bioPortalClient.updateProvisionalTerm(submission.getTmpID(),
 				termForSubmit);
 	}
 
 	public void deleteTerm(OntologySubmission submission) throws Exception {
 		bioPortalClient.deleteProvisionalTerm(submission.getTmpID());		
 	}
 
 	public static void main(String[] args) throws IOException, JAXBException,
 			SAXException, ParserConfigurationException {
 		ClassLoader loader = Thread.currentThread().getContextClassLoader();
 		Properties properties = new Properties();
 		properties.load(loader.getResourceAsStream("config.properties"));
 		String url = properties.getProperty("bioportalUrl");
 		String userId = properties.getProperty("bioportalUserId");
 		String apiKey = properties.getProperty("bioportalApiKey");
 		BioPortalClient bioPortalClient = new BioPortalClient(url, userId,
 				apiKey);
 
 		Filter filter = new Filter();
 		filter.setSubmittedBy(userId);
 		String resultXML = bioPortalClient
 				.getProvisionalTermsReturnString(filter);
 		System.out.println(resultXML);
 
 		boolean deleteAll = true;
 		if (deleteAll) {
 			// parse xml to get all the temporaryID
 			DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			InputSource is = new InputSource();
 			is.setCharacterStream(new StringReader(resultXML));
 			Document doc = builder.parse(is);
 			doc.getDocumentElement().normalize();
 
 			// parse reviewed terms
 			NodeList idNodes = doc.getElementsByTagName("id");
 			if (idNodes.getLength() > 0) {
 				for (int i = 0; i < idNodes.getLength(); i++) {
 					Element e = (Element) idNodes.item(i);
 					if (e != null) {
 						String tmpID = e.getFirstChild().getNodeValue();
 						bioPortalClient.deleteProvisionalTerm(tmpID);
 						System.out.println("Deleted " + tmpID);
 					}
 				}
 			}
 		}
 
 	}
 }
