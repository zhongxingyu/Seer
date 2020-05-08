 /* CVS Header
    $Id$
    $Log$
 */
 
 package fedora.webservices.client.api.m.test;
 
 import fedora.webservices.client.api.RepositoryTest;
 import fedora.webservices.client.api.Utils;
 import fedora.webservices.client.api.m.FedoraAPIMServiceStub;
 import fedora.fedoraSystemDef.foxml.*;
 import org.junit.Test;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 import org.apache.axis2.transport.http.HTTPConstants;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 import info.fedora.definitions.x1.x0.types.IngestDocument;
 import info.fedora.definitions.x1.x0.types.IngestResponseDocument;
 
 import java.rmi.RemoteException;
 
 /**
  * Test for the API-M ingest web service method
  *
  * <a href="http://www.fedora.info/download/2.0/userdocs/digitalobjects/ingestExport.html">API-M documentation</a>
  * @author Alistair Young
  */
 public class Ingest extends RepositoryTest {
   @Test
   public void ingest() {
     // Build a new request document
     IngestDocument doc = IngestDocument.Factory.newInstance();
 
     IngestDocument.Ingest ingest = doc.addNewIngest();
     // java.lang.AssertionError: fedora.server.errors.ObjectValidityException: [DOValidatorImpl]: failed Schematron rules validation. null
     // if this is wrong
     ingest.setFormat(DIGITAL_OBJECT_FORMAT_FOXML);
     ingest.setLogMessage(repositoryProperties.getString(PROPS_KEY_DUMP_INGEST_LOG_MESSAGE));
 
     DigitalObjectDocument objectDoc = DigitalObjectDocument.Factory.newInstance();
     DigitalObjectDocument.DigitalObject object = objectDoc.addNewDigitalObject();
 
     object.setPID(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_PID));
 
     // /////////////////////////////////////////////////////////////////////////////////////////////////
     // Fedora object properties
     ObjectPropertiesType objectProperties = object.addNewObjectProperties();
 
     // <foxml:property NAME="http://www.w3.org/1999/02/22-rdf-syntax-ns#type" VALUE="FedoraObject"/>
     PropertyType typeProperty = objectProperties.addNewProperty();
     typeProperty.setNAME(PropertyType.NAME.HTTP_WWW_W_3_ORG_1999_02_22_RDF_SYNTAX_NS_TYPE);
     typeProperty.setVALUE("FedoraObject");
 
     // <foxml:property NAME="info:fedora/fedora-system:def/model#state" VALUE="A"/>
     PropertyType stateProperty = objectProperties.addNewProperty();
     stateProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE);
     stateProperty.setVALUE(StateType.A.toString());
 
     // <foxml:property NAME="info:fedora/fedora-system:def/model#label" VALUE="FOXML Reference Example"/>
     PropertyType labelProperty = objectProperties.addNewProperty();
     labelProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL);
     labelProperty.setVALUE(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_LABEL));
 
     // <foxml:property NAME="info:fedora/fedora-system:def/model#contentModel" VALUE="TEST_IMAGE"/>
     PropertyType contentModelProperty = objectProperties.addNewProperty();
     contentModelProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_CONTENT_MODEL);
     contentModelProperty.setVALUE(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_CONTENT_MODEL));
 
     // <foxml:extproperty NAME="http://www.openarchives.org/OAI/1.1/oai-identifier.xsd" VALUE="oai:cornell.edu:demo:999"/>
     // For use with <foxml:datastreamVersion ID="DC.0" MIMETYPE="text/xml" LABEL="Default Dublin Core Record">
     // <foxml:xmlContent>/<oai_dc:dc>
     ExtpropertyType extProperty = objectProperties.addNewExtproperty();
     extProperty.setNAME("http://www.openarchives.org/OAI/1.1/oai-identifier.xsd");
     extProperty.setVALUE("oai:cornell.edu:" + repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_PID));
     // /////////////////////////////////////////////////////////////////////////////////////////////////
 
     // /////////////////////////////////////////////////////////////////////////////////////////////////
     // Dublin Core record for the digital object
     DatastreamType dcDatastream = object.addNewDatastream();
     dcDatastream.setID("DC");
     dcDatastream.setSTATE(StateType.A);
     dcDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);
 
     DatastreamVersionType dcDatastreamVersion = dcDatastream.addNewDatastreamVersion();
     dcDatastreamVersion.setID("DC.0");
     dcDatastreamVersion.setMIMETYPE("text/xml");
     dcDatastreamVersion.setLABEL("Default Dublin Core Record");
 
     XmlContentType xmlContent = dcDatastreamVersion.addNewXmlContent();
 
     Element dc = xmlContent.getDomNode().getOwnerDocument().createElementNS("oai_dc", "dc");
     
     // http://www.fedora.info/bugzilla/show_bug.cgi?id=153
     dc.setAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
     dc.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
 
     Element dcTitle = xmlContent.getDomNode().getOwnerDocument().createElementNS("dc", "title");
     Text textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_TITLE));
     dcTitle.appendChild(textNode);
     dc.appendChild(dcTitle);
 
     Element dcCreator = xmlContent.getDomNode().getOwnerDocument().createElementNS("dc", "creator");
     textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_CREATOR));
     dcCreator.appendChild(textNode);
     dc.appendChild(dcCreator);
 
     Element dcSubject = xmlContent.getDomNode().getOwnerDocument().createElementNS("dc", "subject");
     textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_SUBJECT));
     dcSubject.appendChild(textNode);
     dc.appendChild(dcSubject);
 
     Element dcDescription = xmlContent.getDomNode().getOwnerDocument().createElementNS("dc", "description");
     textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_DESCRIPTION));
     dcDescription.appendChild(textNode);
     dc.appendChild(dcDescription);
 
     Element dcPublisher = xmlContent.getDomNode().getOwnerDocument().createElementNS("dc", "publisher");
     textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_PUBLISHER));
     dcPublisher.appendChild(textNode);
     dc.appendChild(dcPublisher);
 
     Element dcIdentifier = xmlContent.getDomNode().getOwnerDocument().createElementNS("dc", "identifier");
    textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_IDENTIFIER));
     dcIdentifier.appendChild(textNode);
     dc.appendChild(dcIdentifier);
 
     xmlContent.getDomNode().appendChild(dc);
     // /////////////////////////////////////////////////////////////////////////////////////////////////
 
     // /////////////////////////////////////////////////////////////////////////////////////////////////
     // Datastream for the object
     DatastreamType objDatastream = object.addNewDatastream();
     objDatastream.setID(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_DATASTREAM_ID));
     objDatastream.setSTATE(StateType.A);
     objDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.M);
 
     DatastreamVersionType objDatastreamVersion = objDatastream.addNewDatastreamVersion();
     objDatastreamVersion.setID(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_DATASTREAM_VERSION_ID));
     objDatastreamVersion.setMIMETYPE(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_MIME_TYPE));
     objDatastreamVersion.setLABEL(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE_LABEL));
 
     objDatastreamVersion.setBinaryContent(Utils.readFile(repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE)));
     // /////////////////////////////////////////////////////////////////////////////////////////////////
 
     ingest.setObjectXML(objectDoc.toString().getBytes());
 
    /*
     XmlCursor cursor = objectDoc.newCursor();
     if (cursor.toFirstChild())
       cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"),
                                         "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd");
                                        */
 
    if (repositoryProperties.getString(PROPS_KEY_TEST_INGEST_FILE).equalsIgnoreCase("yes"))
       Utils.dumpXML(objectDoc, repositoryProperties.getString(PROPS_KEY_DUMP_INGEST_XML_FILE));
 
     try {
       // Initiate the client connection to the API-A endpoint
       FedoraAPIMServiceStub stub = new FedoraAPIMServiceStub(repositoryProperties.getString(PROPS_KEY_API_M_ENDPOINT));
 
       // Add the auth creds to the client
       stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
 
       // Call the web service
       IngestResponseDocument outDoc = stub.ingest(doc);
       assertNotNull(outDoc);
     }
     catch(RemoteException re) {
       fail(re.getMessage());
     }
   }
 }
