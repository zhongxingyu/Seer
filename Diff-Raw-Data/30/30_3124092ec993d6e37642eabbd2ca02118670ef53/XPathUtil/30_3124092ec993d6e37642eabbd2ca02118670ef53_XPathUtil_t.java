 package gov.nih.nci.caxchange.servicemix.bean.util;
 
 
 import gov.nih.nci.caXchange.messaging.CaXchangeRequestMessageDocument;
 import gov.nih.nci.caXchange.messaging.CaXchangeResponseMessageDocument;
 import gov.nih.nci.caXchange.messaging.Credentials;
 import gov.nih.nci.caXchange.messaging.ErrorDetails;
 import gov.nih.nci.caXchange.messaging.MessagePayload;
 import gov.nih.nci.caXchange.messaging.MessageStatuses;
 import gov.nih.nci.caXchange.messaging.Metadata;
 import gov.nih.nci.caXchange.messaging.Operations;
 import gov.nih.nci.caXchange.messaging.Response;
 import gov.nih.nci.caXchange.messaging.ResponseMessage;
 import gov.nih.nci.caXchange.messaging.ResponseMetadata;
 import gov.nih.nci.caXchange.messaging.Statuses;
 import gov.nih.nci.caXchange.messaging.TargetResponseMessage;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 /**
  * This is a utility class for methods to return caXchange Request data and generate 
  * caXchange Response.
  * 
  * @author hmarwaha
  *
  */
 public class XPathUtil {
  
     public static final String CAXCHANGE_URI="http://caXchange.nci.nih.gov/messaging";
     static Logger logger=LogManager.getLogger(XPathUtil.class);
       
     private NormalizedMessage in;
     private Document document;
     
     /**
      * Default constructor
      */
     public XPathUtil() {
     }
     /**
      * Initialize and get input caxchnge request message document
 	 * @param 
 	 * @return 
 	 * @throws Exception
 	 */ 
     public void initialize() throws Exception{
     	document =
     		new SourceTransformer().toDOMDocument(in);
     }
     /**
      * Get user name 
 	 * @param 
 	 * @return userName
 	 * @throws Exception
 	 */ 
     public String getUserName() throws Exception{
     	NodeList elements = document.getElementsByTagNameNS(CAXCHANGE_URI, "userName");
     	String userName=null;
     	if (elements.getLength()==1) {
     		userName = elements.item(0).getTextContent();
     	}
         return userName;        
     }
     /**
      * Get delegated credential reference
 	 * @param 
 	 * @return delegatedCredentialReference
 	 * @throws Exception
 	 */ 
     public String getDelegatedCredentialReference() throws Exception{
     	NodeList elements = document.getElementsByTagNameNS(CAXCHANGE_URI, "delegatedCredentialReference");
     	String delegatedCredentialReference=null;
     	if (elements.getLength()==1) {
     		Node delegatedReference = elements.item(0);
     		delegatedCredentialReference = delegatedReference.getTextContent();
     	}
         return delegatedCredentialReference;  
         
     }
     /**
      * Gets caxchange identifier
 	 * @param 
 	 * @return requestDocument.getCaXchangeRequestMessage().getMetadata().getExternalIdentifier()
 	 * @throws Exception
 	 */ 
     public String getCaXchangeIdentifier() throws Exception {
     	NodeList elements = document.getElementsByTagNameNS(CAXCHANGE_URI, "caXchangeIdentifier");
     	String caXchangeIdentifier=null;
     	if (elements.getLength()==1) {
     		caXchangeIdentifier = elements.item(0).getTextContent();
     	}
         return caXchangeIdentifier;  
     }
     /**
      * Gets message type
 	 * @param 
 	 * @return requestDocument.getCaXchangeRequestMessage().getMetadata().getExternalIdentifier()
 	 * @throws Exception
 	 */  
     public String getExternalIdentifier() throws Exception {
     	NodeList elements = document.getElementsByTagNameNS(CAXCHANGE_URI, "externalIdentifier");
     	String externalIdentifier=null;
     	if (elements.getLength()==1) {
     		externalIdentifier = elements.item(0).getTextContent();
     	}
         return externalIdentifier;     
     }    
     /**
      * Gets message type
 	 * @param 
 	 * @return requestDocument.getCaXchangeRequestMessage().getMetadata().getMessageType().toString()
 	 * @throws Exception
 	 */  
     public String getMessageType() throws Exception {
     	NodeList elements = document.getElementsByTagNameNS(CAXCHANGE_URI, "messageType");
     	String messageType=null;
     	if (elements.getLength()==1) {
     		messageType = elements.item(0).getTextContent();
     	}
         return messageType; 
     }   
     
     /**
      * Gets business payload included in the caXchange request.
 	 * @param 
 	 * @return Business payload 
 	 * @throws Exception
 	 */  
     public Node getBusinessPayload() throws Exception {
     	NodeList elements = document.getElementsByTagNameNS(CAXCHANGE_URI, "businessMessagePayload");
     	if (elements.getLength()==1) {
     		Node businessMessagePayload = elements.item(0);
     		NodeList payloads = businessMessagePayload.getChildNodes();
     		for (int i=0;((payloads!=null)&&(i<payloads.getLength()));i++) {
     			Node payload = payloads.item(i);
     			if (payload instanceof Element){
     			   if ("xmlSchemaDefinition".equals(payload.getLocalName())) {
     				   continue;
     			   }else {
     				  return payload;
     			   }
     			}
     		}
     	}
     	//No payload found.
         return null; 
     }  
     
     /**
      * This method generates a caXchangeResponse with the given error details.
 	 * @param errCode
 	 * @param errMessage
 	 * @return new DOMSource(responseDocument.getDomNode())
 	 * @throws Exception
 	 */    
     public Source generateResponseFromErroredRequest(String errCode, String errMessage) throws Exception {
         CaXchangeResponseMessageDocument responseDocument = CaXchangeResponseMessageDocument.Factory.newInstance();
         ResponseMessage responseMessage = responseDocument.addNewCaXchangeResponseMessage();
         ResponseMetadata responseMetadata = responseMessage.addNewResponseMetadata();
         responseMetadata.setExternalIdentifier(getExternalIdentifier());
         responseMetadata.setCaXchangeIdentifier(getCaXchangeIdentifier());
         Response response = responseMessage.addNewResponse();
         response.setResponseStatus(Statuses.FAILURE);
         ErrorDetails errorDetails = response.addNewCaXchangeError();
         errorDetails.setErrorCode(errCode);
         errorDetails.setErrorDescription(errMessage);
         
         return new DOMSource(responseDocument.getDomNode());
         
     }
     /**
      * Generates caXchangeResponse for caXchange routing metadata.
      * @param metadata
      * @return
      * @throws Exception
      */
     public Source generateResponseForMetadata(String metadata) throws Exception {
         CaXchangeResponseMessageDocument responseDocument = CaXchangeResponseMessageDocument.Factory.newInstance();
         ResponseMessage responseMessage = responseDocument.addNewCaXchangeResponseMessage();
         ResponseMetadata responseMetadata = responseMessage.addNewResponseMetadata();
         responseMetadata.setExternalIdentifier(getExternalIdentifier());
         responseMetadata.setCaXchangeIdentifier(getCaXchangeIdentifier());
         Response response = responseMessage.addNewResponse();
         response.setResponseStatus(Statuses.SUCCESS);
         TargetResponseMessage targetResponse = response.addNewTargetResponse();
         MessagePayload messagePayload = targetResponse.addNewTargetBusinessMessage();
         Node payloadNode = messagePayload.getDomNode();
 
         return new DOMSource(responseDocument.getDomNode());
         
     }    
     /**
      * This method generates a CaXchangeResponseMessageDocument from the aggregated Response.
      * 
 	 * @param aggregatedResponse
 	 * @return CaXchangeResponseMessageDocument
 	 * @throws Exception
 	 */
     public CaXchangeResponseMessageDocument generateResponseFromAggregatedResponse(Source aggregatedResponse) throws Exception {
         CaXchangeResponseMessageDocument responseDocument = CaXchangeResponseMessageDocument.Factory.newInstance();
         ResponseMessage responseMessage = responseDocument.addNewCaXchangeResponseMessage();
         ResponseMetadata responseMetaData= responseMessage.addNewResponseMetadata();
         responseMetaData.setCaXchangeIdentifier(getCaXchangeIdentifier());
         responseMetaData.setExternalIdentifier(getExternalIdentifier());
         Response response = responseMessage.addNewResponse();
         response = buildResponse(aggregatedResponse, response);
           
         return responseDocument;
     }
     /**
      * This method builds a caXchangeResponse from  he aggregateResponse.
 	 * @param aggregatedResponse
 	 * @param response
 	 * @return response
 	 * @throws Exception
 	 */
     public Response buildResponse(Source aggregatedResponse, Response response) throws Exception{
         XPath xpath = XPathFactory.newInstance().newXPath();
         CaXchangeNamespaceContext cnc = new CaXchangeNamespaceContext();
         cnc.addNameSpace("caxchange","http://caXchange.nci.nih.gov/messaging");
         Node node = ((DOMSource)aggregatedResponse).getNode();
         XPathExpression expression = xpath.compile("/aggregateResponse/targetResponse");
         XPathExpression tsiExp = xpath.compile("targetServiceIdentifier");
         XPathExpression tsoExp = xpath.compile("targetServiceOperation");
         XPathExpression msExp = xpath.compile("targetMessageStatus");
         XPathExpression mpExp = xpath.compile("messagePayload");
         XPathExpression teExp = xpath.compile("targetError");
         XPathExpression schemaDefExp = xpath.compile("xmlSchemaDefinition");
         NodeList nodes = (NodeList)expression.evaluate(node,XPathConstants.NODESET);
         Statuses.Enum responseStatus = Statuses.SUCCESS;
         for(int i=0;i<nodes.getLength();i++) {
            Node brNode = nodes.item(i);
            TargetResponseMessage brm = response.addNewTargetResponse();
            brm.setTargetServiceIdentifier((String)tsiExp.evaluate(brNode, XPathConstants.STRING));
            brm.setTargetServiceOperation((String)tsoExp.evaluate(brNode, XPathConstants.STRING));
            String targetMessageStatus = (String)msExp.evaluate(brNode, XPathConstants.STRING);
            if (MessageStatuses.FAULT.toString().equals(targetMessageStatus)) {
                brm.setTargetMessageStatus(MessageStatuses.FAULT);
                responseStatus = Statuses.FAILURE;
            }else if (MessageStatuses.ERROR.toString().equals(targetMessageStatus)) {
                brm.setTargetMessageStatus(MessageStatuses.ERROR);
                responseStatus = Statuses.FAILURE;
            }
            else if (MessageStatuses.RESPONSE.toString().equals(targetMessageStatus)) {
                brm.setTargetMessageStatus(MessageStatuses.RESPONSE);
            }
            Node mpNode =  (Node)mpExp.evaluate(brNode, XPathConstants.NODE);
            if (mpNode != null) {
                MessagePayload payload= brm.addNewTargetBusinessMessage();
                payload.setXmlSchemaDefinition((String)schemaDefExp.evaluate(mpNode, XPathConstants.STRING));
                Node payloadNode = payload.getDomNode();
                Document doc = payloadNode.getOwnerDocument();
                NodeList mpNodes = mpNode.getChildNodes();
                for(int k=0;k<mpNodes.getLength();k++) {
                    Node mpcNode = mpNodes.item(k);
                    if (!("xmlSchemaDefinition".equals(mpcNode.getNodeName()))) {
                       Node importedNode= doc.importNode(mpcNode, true);
                       payloadNode.appendChild(importedNode);
                    }
                }
            }
            Node teNode = (Node)teExp.evaluate(brNode, XPathConstants.NODE);
            if (teNode!= null) {
               ErrorDetails targetError = ErrorDetails.Factory.newInstance();
               NodeList teNodes=teNode.getChildNodes();              
               for (int k=0;k<teNodes.getLength();k++) {
                   Node teChildNode = teNodes.item(k);
                   if ("errorCode".equals(teChildNode.getNodeName())) {
                       targetError.setErrorCode(teChildNode.getTextContent());
                   }
                   if ("errorDescription".equals(teChildNode.getNodeName())) {
                       targetError.setErrorDescription(teChildNode.getTextContent());
                   }                  
               }
               brm.setTargetError(targetError);
            }
         }
         response.setResponseStatus(responseStatus);
         return response;
     }
     /**
      * This method cheks if the rollback is required
 	 * @param aggregatedResponse
 	 * @return false
 	 * @throws Exception
 	 */
     /*No references*/
     public boolean isRollbackRequired(Source aggregatedResponse) throws Exception{
         XPath xpath = XPathFactory.newInstance().newXPath();
         Node node = ((DOMSource)aggregatedResponse).getNode();
         XPathExpression expression = xpath.compile("/aggregateResponse/businessResponse/messageStatus");
         NodeList nodes = (NodeList)expression.evaluate(node,XPathConstants.NODESET);
         for(int i=0;i<nodes.getLength();i++) {
             Node brNode =  nodes.item(i);
             String messageStatus = brNode.getTextContent();
             if ((MessageStatuses.FAULT.toString().equals(messageStatus))||
                  (MessageStatuses.FAULT.toString().equals(messageStatus))){
                      return true;
                  }
         }
         return false;
     }
     /**
      * This method gets the roll back message
 	 * @param in
 	 * @return the original document with the rollback operation
 	 * @throws Exception
 	 */
     public Source getRollbackMessage() throws Exception {
     	NodeList elements = document.getElementsByTagNameNS(CAXCHANGE_URI, "operation");
         if (elements.getLength()==1){
        	elements.item(0).setTextContent("ROLLBACK");
        }else {
        	throw new Exception("Error generating rollback message. No operation element found.");
         }
        return new DOMSource(document);
     }
     
     /**
      * This method sets the normalized message
 	 * @param in
 	 * @return
 	 * @throws 
 	 */
     public void setIn(NormalizedMessage in) {
         this.in = in;
     }
     /**
      * This method gets the normalized message
 	 * @param 
 	 * @return in
 	 * @throws 
 	 */
     /*No references*/
     public NormalizedMessage getIn() {
         return in;
     }
     
     public static void main(String [] args) {
         try  {
             XPathUtil util = new XPathUtil();
             DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             Document document= db.parse(new InputSource(new StringReader(util.myDocumentAgg)));
             DOMSource ds = new DOMSource(document);
             Response response = Response.Factory.newInstance();
             util.buildResponse(ds, response);
             System.out.println(response);
         } catch (Exception ex)  {
             ex.printStackTrace();
         } finally  {
         }
         
     }
     
     String myDocument = "<caXchangeRequestMessage xmlns=\"http://caXchange.nci.nih.gov/mes\n" + 
     "saging\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
     "  <metaData>\n" + 
     "    <messageType>STUDY_CREATION</messageType>\n" + 
     "    <externalIdentifier>myExternalIdentifier</externalIdentifier>\n" + 
     "    <caXchangeIdentifier>0da440a0-8ff5-11dc-babe-d4801775f7f7</caXchangeIdentifi\n" + 
     "er>\n" + 
     "    <credentials>\n" + 
     "      <groupName xsi:nil=\"true\"/>\n" + 
     "      <gridIdentifier xsi:nil=\"true\"/>\n" + 
     "    </credentials>\n" + 
     "  </metaData>\n" + 
     "  <request>\n" + 
     "    <businessMessagePayload>\n" + 
     "      <xmlSchemaDefinition>http://test</xmlSchemaDefinition>\n" + 
     "      <mytest1 xmlns=\"\">\n" + 
     "        <first1>harsh</first1>\n" + 
     "        <last1>marwaha</last1>\n" + 
     "      </mytest1>\n" + 
     "    </businessMessagePayload>\n" + 
     "  </request>\n" + 
     "</caXchangeRequestMessage>";
     
     String myDocumentAgg = "<aggregateResponse>"+
     " <targetResponse xmlns=\"http://caXchange.nci.nih.gov/messaging\">\n" + 
     "                <targetServiceIdentifier>targetServiceIdentifier0</targetServiceIdentifier>\n" + 
     "                <targetServiceOperation>targetServiceOperation0</targetServiceOperation>\n" + 
     "                <targetMessageStatus>RESPONSE</targetMessageStatus>\n" + 
     "                <messagePayload>\n" + 
     "                    <xmlSchemaDefinition>http://www.oxygenxml.com/</xmlSchemaDefinition>\n" + 
     "<mytest1>\n" + 
     "   <first>Harsh</first>\n" + 
     "   <last>Marwaha</last>\n" + 
     "</mytest1> "+
     "                </messagePayload>\n" + 
     "            </targetResponse>\n" + 
     "<targetResponse> " + 
     " <targetServiceIdentifier>PROCESS</targetServiceIdentifier> " + 
     " <targetServiceOperation>STUDY_CREATION</targetServiceOperation> " + 
     " <targetMessageStatus>ERROR</targetMessageStatus> " + 
     " <targetError><errorCode>NA</errorCode><errorDescription>Invalid element in gov.nih.nci.ccts.grid.Study - systemAssignedIdentifier</errorDescription></targetError> " + 
     " </targetResponse>"+
     "    </aggregateResponse>"; 
  
 }
