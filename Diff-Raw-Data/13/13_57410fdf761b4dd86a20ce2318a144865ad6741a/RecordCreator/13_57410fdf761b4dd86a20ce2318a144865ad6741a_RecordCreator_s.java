 package dk.statsbiblioteket.doms.ingesters.radiotv;
 
 import dk.statsbiblioteket.doms.client.DomsWSClient;
 import dk.statsbiblioteket.doms.client.exceptions.NoObjectFound;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.exceptions.XMLParseException;
 import dk.statsbiblioteket.doms.client.relations.LiteralRelation;
 import dk.statsbiblioteket.doms.client.relations.Relation;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Code to create programs.
  */
 public class RecordCreator {
     private DomsWSClient domsClient;
     private DocumentBuilder documentBuilder;
 
     public RecordCreator(DomsWSClient domsClient) throws ParserConfigurationException {
         this.domsClient = domsClient;
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilder = documentBuilderFactory.newDocumentBuilder();
     }
 
     /**
      * Ingests or updates a program object
      *
      *
      *
      * @param radioTVMetadata Bibliographical metadata about the program.
      * @return PID of the newly created program object, created by the DOMS.
      *
      * @throws ServerOperationFailed    if creation or manipulation of the program object fails.
      * @throws XMLParseException        if any errors were encountered while processing the
      *                                  <code>radioTVMetadata</code> XML document.
      * @throws XPathExpressionException Should never happen. Means program is broken with faulty XPath.
      * @throws MalformedURLException if a file element contains an invalid URL.
      * @throws NoObjectFound         if a URL is referenced, which is not found in DOMS.
      */
     public String ingestProgram(Document radioTVMetadata)
             throws ServerOperationFailed, XMLParseException, MalformedURLException, NoObjectFound,
             XPathExpressionException {
         // Get pids of referenced files - do this first, to ensure fail-early in case of missing files.
         List<String> filePIDs = getFilePids(radioTVMetadata);
 
         // Find or create program object.
         List<String> oldIdentifiers = getOldIdentifiers(radioTVMetadata);
         String existingPid = alreadyExistsInRepo(oldIdentifiers);
         String programObjectPID;
         if (existingPid == null) {//not Exist
             // Create a program object in the DOMS and update the PBCore metadata
             // datastream with the PBCore metadata from the pre-ingest file.
             programObjectPID = domsClient.createObjectFromTemplate(Common.PROGRAM_TEMPLATE_PID, oldIdentifiers, Common.COMMENT);
         } else { //Exists
             domsClient.unpublishObjects(Common.COMMENT, existingPid);
             addOldPids(existingPid, oldIdentifiers);
             programObjectPID = existingPid;
         }
 
         // Get the program title from the PBCore metadata and use that as the
         // object label for this program object.
         Node titleNode = Common.XPATH_SELECTOR.selectNode(radioTVMetadata, Common.PBCORE_TITLE_ELEMENT);
         String programTitle = titleNode.getTextContent();
         domsClient.setObjectLabel(programObjectPID, programTitle, Common.COMMENT);
 
         // Add PBCore datastream
         Document pbCoreDataStreamDocument = createDocumentFromNode(radioTVMetadata, Common.PBCORE_DESCRIPTION_ELEMENT);
         domsClient.updateDataStream(programObjectPID, Common.PROGRAM_PBCORE_DS_ID, pbCoreDataStreamDocument, Common.COMMENT);
 
         // Add Ritzau datastream
         Document ritzauOriginalDocument = createDocumentFromNode(radioTVMetadata, Common.RITZAU_ORIGINALS_ELEMENT);
         domsClient.updateDataStream(programObjectPID, Common.RITZAU_ORIGINAL_DS_ID, ritzauOriginalDocument, Common.COMMENT);
 
         // Add the Gallup datastream
         Document gallupOriginalDocument = createDocumentFromNode(radioTVMetadata, Common.GALLUP_ORIGINALS_ELEMENT);
         domsClient.updateDataStream(programObjectPID, Common.GALLUP_ORIGINAL_DS_ID, gallupOriginalDocument, Common.COMMENT);
 
         // Add the program broadcast datastream
         Document programBroadcastDocument = createDocumentFromNode(radioTVMetadata, Common.PROGRAM_BROADCAST_ELEMENT);
         domsClient.updateDataStream(programObjectPID, Common.PROGRAM_BROADCAST_DS_ID, programBroadcastDocument, Common.COMMENT);
 
         // Update file relations
         List<Relation> relations = domsClient.listObjectRelations(programObjectPID, Common.HAS_FILE_RELATION_TYPE);
         HashSet<String> existingRels = new HashSet<String>();
         for (Relation relation : relations) {
             if (!filePIDs.contains(relation.getSubjectPid())) {
                 domsClient.removeObjectRelation((LiteralRelation) relation, Common.COMMENT);
             } else {
                 existingRels.add(relation.getSubjectPid());
             }
         }
         for (String filePID : filePIDs) {
             if (!existingRels.contains(filePID)) {
                 domsClient.addObjectRelation(programObjectPID, Common.HAS_FILE_RELATION_TYPE, filePID, Common.COMMENT);
 
             }
         }
         return programObjectPID;
     }
 
     private void addOldPids(String existingPid, List<String> oldIdentifiers) throws ServerOperationFailed {
         Document dcDataStream = domsClient.getDataStream(existingPid, Common.DC_DS_ID);
         NodeList existingIDNodes = Common.XPATH_SELECTOR.selectNodeList(dcDataStream, Common.DC_IDENTIFIER_ELEMENT);
         Set<String> idsToAdd = new HashSet<String>(oldIdentifiers);
         for (int i = 0; i < existingIDNodes.getLength(); i++) {
             idsToAdd.remove(existingIDNodes.item(i).getTextContent());
         }
         if (idsToAdd.isEmpty()) {
             return;
         }
         Node identifier = Common.XPATH_SELECTOR.selectNode(dcDataStream, Common.DC_IDENTIFIER_ELEMENT);
         for (String id : idsToAdd) {
             Element newIdentifier = dcDataStream.createElementNS(Common.DC_NAMESPACE, "identifier");
             dcDataStream.setTextContent(id);
             identifier.getParentNode().insertBefore(newIdentifier, identifier);
         }
         domsClient.updateDataStream(existingPid, Common.DC_DS_ID, dcDataStream, Common.COMMENT);
     }
 
     /**
      * Utility method to create a document to ingest from a node.
      *
      * @param radioTVMetadata Bibliographical metadata about the program.
      * @param xpath           XPath expression to select and ingest in node.
      * @return A document containing the metadata form the node selected by xpath.
      */
     private Document createDocumentFromNode(Document radioTVMetadata, String xpath) {
         final Node nodeToIngest = Common.XPATH_SELECTOR.selectNode(radioTVMetadata, xpath);
 
         // Build a data document for the data stream in the program object.
         final Document document = documentBuilder.newDocument();
         document.appendChild(document.importNode(nodeToIngest, true));
         return document;
     }
 
     /**
      * Lookup a program in DOMS.
      * If program exists, returns the PID of the program. Otherwise returns null.
      *
      * @return PID of program, if found. Null otherwise
      *
      * @throws ServerOperationFailed Could not communicate with DOMS.
      * @param oldIdentifiers List of old identifiers to look up.
      */
     private String alreadyExistsInRepo(List<String> oldIdentifiers)
            throws XPathExpressionException, ServerOperationFailed, NoObjectFound {
         for (String oldId : oldIdentifiers) {
            List<String> pids = domsClient.getPidFromOldIdentifier(oldId);
            if (!pids.isEmpty()) {
                return pids.get(0);
             }
         }
         return null;
     }
 
     /**
      * Find old identifiers in program metadata, and use them for looking up programs in DOMS.
      *
      * @param radioTVMetadata The document containing the program metadata.
      * @return Old indentifiers found.
      * @throws XPathExpressionException Should never happen. Means program is broken with faulty XPath.
      */
     private List<String> getOldIdentifiers(Document radioTVMetadata) throws XPathExpressionException {
         List<String> result = new ArrayList<String>();
         Node radioTVPBCoreElement = Common.XPATH_SELECTOR
                 .selectNode(radioTVMetadata, Common.PBCORE_DESCRIPTION_ELEMENT);
 
         Node oldRitzauPIDNode = Common.XPATH_SELECTOR
                 .selectNode(radioTVPBCoreElement, Common.PBCORE_RITZAU_IDENTIFIER_ELEMENT);
         if (oldRitzauPIDNode != null && !oldRitzauPIDNode.getTextContent().isEmpty()) {
             result.add(oldRitzauPIDNode.getTextContent());
         }
 
         Node oldGallupPIDNode = Common.XPATH_SELECTOR
                 .selectNode(radioTVPBCoreElement, Common.PBCORE_GALLUP_IDENTIFIER_ELEMENT);
         if (oldGallupPIDNode != null && !oldGallupPIDNode.getTextContent().isEmpty()) {
             result.add(oldGallupPIDNode.getTextContent());
         }
         return result;
     }
 
     /**
      * Git the PIDs for all the file URLs.
      *
      * @param radioTVMetadata Metadata XML document containing the file information.
      * @return A <code>List</code> of PIDs of the radio-tv file objects found in DOMS.
      *
      * @throws MalformedURLException if a file element contains an invalid URL.
      * @throws ServerOperationFailed if looking up file URL failed.
      * @throws NoObjectFound         if a URL is referenced, which is not found in DOMS.
      */
     private List<String> getFilePids(Document radioTVMetadata)
             throws MalformedURLException, ServerOperationFailed, NoObjectFound {
         // Get the recording files XML element and process the file information.
         NodeList recordingFileURLs = Common.XPATH_SELECTOR.selectNodeList(radioTVMetadata, Common.RECORDING_FILES_URLS);
 
         // Find the pids for all referenced file urls.
         List<String> fileObjectPIDs = new ArrayList<String>();
         for (int nodeIndex = 0; nodeIndex < recordingFileURLs.getLength(); nodeIndex++) {
             // Lookup file object.
             fileObjectPIDs.add(domsClient.getFileObjectPID(new URL(recordingFileURLs.item(nodeIndex).getTextContent())));
         }
         return fileObjectPIDs;
     }
 }
