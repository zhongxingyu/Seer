 package fedora.server.storage.translation;
 
 import fedora.common.Constants;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.RepositoryConfigurationException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.ValidationException;
 import fedora.server.storage.types.AuditRecord;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamManagedContent;
 import fedora.server.storage.types.DatastreamReferencedContent;
 import fedora.server.storage.types.DatastreamXMLMetadata;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DSBindingMap;
 import fedora.server.storage.types.DSBinding;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.StreamUtility;
 import fedora.server.validation.ValidationUtility;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.axis.encoding.Base64;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  *
  * <p><b>Title:</b> FOXMLDODeserializer.java</p>
  * <p><b>Description:</b> 
  *       Deserializes XML digital object encoded in accordance with 
  * 		 the Fedora Object XML (FOXML) schema defined at: 
  * 		 http://www.fedora.info/definitions/1/0/foxml1-0.xsd.
  * 
  *       The FOXML XML is parsed using SAX and is instantiated into a Fedora
  *       digital object in memory (see fedora.server.types.DigitalObject). </p>
  *
  * @author payette@cs.cornell.edu
  * @version $Id$
  */
 public class FOXMLDODeserializer
         extends DefaultHandler
         implements DODeserializer,
                    Constants {
 
     /** The namespace for FOXML */
     private final static String F="info:fedora/fedora-system:def/foxml#";
 	
 	/** The deserializer context. See definitions of contexts in 
 	 *  fedora.server.storage.translation.DOTranslationUtility.
 	 */
 	private int m_transContext; 
     
 	/** The object to deserialize to. */
 	private DigitalObject m_obj;
     
 	/** SAX parser */
     private SAXParser m_parser;
 
     // Namespace prefix-to-URI mapping info from SAX2 startPrefixMapping events.
     private HashMap m_prefixMap;
     private HashMap m_localPrefixMap;
     private ArrayList m_prefixList;
 
 	// temporary variables and state variables
 	private int m_queryBehavior;
 	private String m_characterEncoding;
     private boolean m_rootElementFound;
 	private String m_objPropertyName;
 	private boolean m_readingBinaryContent; // indicates reading base64-encoded content
 	private File m_binaryContentTempFile;
 	private boolean m_inXMLMetadata;	
 	// Indicator for FOXML within FOXML (inline XML datastream contains FOXML)
 	private int m_xmlDataLevel;
 	
 	// temporary variables for datastream processing
     private String m_dsId;
 	private boolean m_dsVersionable;
     private String m_dsVersId;
     private Date m_dsCreateDate;
     private String m_dsState;
     private String[] m_dsAltIds;
     private String m_dsFormatURI;
     private String m_dsLabel;
     private long m_dsSize;
     private String m_dsLocationType;
     private URL m_dsLocationURL;
     private String m_dsLocation;
     private String m_dsMimeType;
     private String m_dsControlGrp;	
 	private String m_dsInfoType; // for METS backward compatibility
 	private String m_dsOtherInfoType; // for METS backward compatibility
 	private int m_dsMDClass; // for METS backward compatibility
 	private Pattern metsPattern=Pattern.compile("info:fedora/fedora-system:format/xml.mets.");
 	private HashMap m_dsAdmIds; // key=dsId, value=List of datastream ids (strings)
 	private String[] m_dsDmdIds; // key=dsId, value=List of datastream ids (strings)
 
 	// temporary variables for processing disseminators
 	private Disseminator m_diss;
 	private String m_dissID;
 	private String m_bDefID;
 	private String m_dissState;
 	private boolean m_dissVersionable;
 	private DSBindingMap m_dsBindMap;
 	private ArrayList m_dsBindings;
 	
 	// temporary variables for processing audit records
 	private AuditRecord m_auditRec;	
 	private boolean m_gotAudit=false;
 	//private String m_auditRecordID;
 	private String m_auditComponentID;
 	private String m_auditProcessType;
 	private String m_auditAction;
 	private String m_auditResponsibility;
 	private String m_auditDate;
 	private String m_auditJustification;
 	
 	// buffers for reading content
 	private StringBuffer m_elementContent; // single element
     private StringBuffer m_dsXMLBuffer; // chunks of inline XML metadata
     
     /**
      * Never query web server for content size and MIME type
      */
     public static int QUERY_NEVER=0;
 
     /**
      * Query web server for content size and MIME type if either are undefined.
      */
     public static int QUERY_IF_UNDEFINED=1;
 
     /**
      * Always query web server for content size and MIME type.
      */
     public static int QUERY_ALWAYS=2;
 
 
     public FOXMLDODeserializer()
             throws FactoryConfigurationError, ParserConfigurationException,
             SAXException, UnsupportedEncodingException {
         this("UTF-8", false, QUERY_NEVER);
     }
 
     /**
      * Initializes by setting up a parser that doesn't validate and never
      * queries the server for values of DSSize and DSMIME.
      */
     public FOXMLDODeserializer(String characterEncoding)
             throws FactoryConfigurationError, ParserConfigurationException,
             SAXException, UnsupportedEncodingException {
         this(characterEncoding, false, QUERY_NEVER);
     }
 
     /**
      * Initializes by setting up a parser that validates only if validate=true.
      * <p></p>
      * The character encoding of the XML is auto-determined by sax, but
      * we need it for when we set the byte[] in DatastreamXMLMetadata, so
      * we effectively, we need to also specify the encoding of the datastreams.
      * this could be different than how the digital object xml was encoded,
      * and this class won't care.  However, the caller should keep track
      * of the byte[] encoding if it plans on doing any translation of
      * that to characters (such as in xml serialization)
      */
     public FOXMLDODeserializer(String characterEncoding, boolean validate,
             int queryBehavior)
             throws FactoryConfigurationError, ParserConfigurationException,
             SAXException, UnsupportedEncodingException {
         m_queryBehavior=queryBehavior;
         m_characterEncoding=characterEncoding;
         StringBuffer buf=new StringBuffer();
         buf.append("test");
         byte[] temp=buf.toString().getBytes(m_characterEncoding);
         // then init sax
         SAXParserFactory spf = SAXParserFactory.newInstance();
         spf.setValidating(validate);
         spf.setNamespaceAware(true);
         m_parser=spf.newSAXParser();
     }
 
     public DODeserializer getInstance()
             throws RepositoryConfigurationException {
         try {
             return (DODeserializer) new FOXMLDODeserializer("UTF-8", false, QUERY_NEVER);
         } catch (Exception e) {
             throw new RepositoryConfigurationException("Error trying to get a "
                     + "new FOXMLDODeserializer instance: " + e.getClass().getName()
                     + ": " + e.getMessage());
         }
     }
 
     public void deserialize(InputStream in, DigitalObject obj, String encoding, int transContext)
             throws ObjectIntegrityException, StreamIOException, UnsupportedEncodingException {
             	
         if (fedora.server.Debug.DEBUG) System.out.println("Deserializing FOXML for transContext: " + transContext);
         m_obj=obj;
         m_transContext=transContext;
         initialize();
         try {
             m_parser.parse(in, this);
         } catch (IOException ioe) {
             throw new StreamIOException("low-level stream io problem occurred "
                     + "while sax was parsing this object.");
         } catch (SAXException se) {
             throw new ObjectIntegrityException("FOXML IO stream was bad : " + se.getMessage());
         }
         if (fedora.server.Debug.DEBUG) System.out.println("Just finished parse.");
 
         if (!m_rootElementFound) {
             throw new ObjectIntegrityException("FOXMLDODeserializer: Input stream is not valid FOXML." +
             	" The digitalObject root element was not detected.");
         }       
         obj.setNamespaceMapping(new HashMap());
     }
 
     public void startPrefixMapping(String prefix, String uri) {
         m_prefixMap.put(prefix, uri);
         if (m_inXMLMetadata) {
             m_localPrefixMap.put(prefix, uri);
             m_prefixList.add(prefix);
         }
     }
 
     public void endPrefixMapping(String prefix) {
         m_prefixMap.remove(prefix);
         if (m_inXMLMetadata) {
             m_localPrefixMap.remove(prefix);
         }
     }
 
     public void startElement(String uri, String localName, String qName,
             Attributes a) throws SAXException {
 
 		// Initialize string buffer to hold content of the new element.
 		// This will start a fresh buffer for every element encountered.            	
 		m_elementContent=new StringBuffer();
 
         if (uri.equals(F) && !m_inXMLMetadata) {
             // WE ARE NOT INSIDE A BLOCK OF INLINE XML...
             if (localName.equals("digitalObject")) {
                 m_rootElementFound=true;
                 //======================
                 // OBJECT IDENTIFIERS...
                 //======================
 				m_obj.setPid(grab(a, F, "PID"));
 			//=====================
 			// OBJECT PROPERTIES...
 			//=====================
             } else if (localName.equals("property") || localName.equals("extproperty")) {
 				m_objPropertyName = grab(a, F, "NAME");
 				if (m_objPropertyName.equals(MODEL.STATE.uri)){
                     String stateString = grab(a, F, "VALUE");
                     String stateCode = null;
                     if (MODEL.DELETED.looselyMatches(stateString, true)) {
                         stateCode = "D";
                     } else if (MODEL.INACTIVE.looselyMatches(stateString, true)) {
                         stateCode = "I";
                     } else if (MODEL.ACTIVE.looselyMatches(stateString, true)) {
                         stateCode = "A";
                     }
                     m_obj.setState(stateCode);
 				} else if (m_objPropertyName.equals(MODEL.CONTENT_MODEL.uri)){
 					m_obj.setContentModelId(grab(a, F, "VALUE"));
 				} else if (m_objPropertyName.equals(MODEL.LABEL.uri)){
 					m_obj.setLabel(grab(a, F, "VALUE"));
 				} else if (m_objPropertyName.equals(MODEL.CREATED_DATE.uri)){
 					m_obj.setCreateDate(DateUtility.convertStringToDate(grab(a, F, "VALUE")));
 				} else if (m_objPropertyName.equals(VIEW.LAST_MODIFIED_DATE.uri)){
 					m_obj.setLastModDate(DateUtility.convertStringToDate(grab(a, F, "VALUE")));
 				} else if (m_objPropertyName.equals(RDF.TYPE.uri)) {
 					String oType = grab(a, F, "VALUE");
 					if (oType==null || oType.equals("")) { oType=MODEL.DATA_OBJECT.localName; }
                     if (MODEL.BDEF_OBJECT.looselyMatches(oType, false)) {
 						m_obj.setFedoraObjectType(DigitalObject.FEDORA_BDEF_OBJECT);
 					} else if (MODEL.BMECH_OBJECT.looselyMatches(oType, false)) {
 						m_obj.setFedoraObjectType(DigitalObject.FEDORA_BMECH_OBJECT);
 					} else {
 						m_obj.setFedoraObjectType(DigitalObject.FEDORA_OBJECT);
 					}
 				} else {
 					// add an extensible property in the property map
 					m_obj.setExtProperty(m_objPropertyName, grab(a, F, "VALUE"));
 				}
 			//===============
 			// DATASTREAMS...
 			//===============
 			} else if (localName.equals("datastream")) {
 				// get datastream container-level attributes...
 				// These are common for all versions of the datastream. 
 				m_dsId=grab(a, F, "ID");
 				m_dsState=grab(a, F, "STATE");
 				m_dsControlGrp=grab(a, F, "CONTROL_GROUP");
 				String versionable =grab(a, F, "VERSIONABLE");
 				// If dsVersionable is null or missing, default to true.
 				if (versionable==null || versionable.equals("")) {
 					m_dsVersionable=true;
 				} else {
 					m_dsVersionable=new Boolean(versionable).booleanValue();
 				}
 				// Never allow the AUDIT datastream to be versioned
 				// since it naturally represents a system-controlled
 				// view of changes over time.
				checkMETSFormat(m_dsFormatURI);
 				if (m_dsId.equals("AUDIT")) {
 					m_dsVersionable=false;
 				}
 			} else if (localName.equals("datastreamVersion")) {
 				// get datastream version-level attributes...
 				m_dsVersId=grab(a, F, "ID");
 				m_dsLabel=grab(a, F, "LABEL");
 				m_dsCreateDate=DateUtility.convertStringToDate(grab(a, F, "CREATED"));
 				String altIDsString = grab(a, F, "ALT_IDS");
 				if (altIDsString.length() == 0) {
 					m_dsAltIds = new String[0];
 				} else {
 					m_dsAltIds = altIDsString.split(" ");
 				}
 				m_dsFormatURI=grab(a, F, "FORMAT_URI");
 				if (m_dsFormatURI.length() == 0) {
 					m_dsFormatURI = null;
 				}
 				m_dsMimeType=grab(a, F, "MIMETYPE");
 				String sizeString=grab(a, F, "SIZE");
 				if (sizeString!=null && !sizeString.equals("")) {
 					try {
 						m_dsSize=Long.parseLong(sizeString);
 					} catch (NumberFormatException nfe) {
 						throw new SAXException("If specified, a datastream's "
 								+ "SIZE attribute must be an xsd:long.");
 					}
 				} else {
 					m_dsSize=-1;
 				}
 				if (m_dsVersId.equals("AUDIT.0")) {
 					m_gotAudit=true;
 				}
 			//======================
 			// DATASTREAM CONTENT...
 			//======================
 			// inside a datastreamVersion element, it's either going to be
 			// xmlContent (inline xml), contentLocation (a reference) or binaryContent
 			} else if (localName.equals("xmlContent")) {
 				m_dsXMLBuffer=new StringBuffer();
 				m_xmlDataLevel=0;
 				m_inXMLMetadata=true;
             } else if (localName.equals("contentLocation")) {
                 String dsLocation=grab(a,F,"REF");
                 if (dsLocation==null || dsLocation.equals("")) {
                     throw new SAXException("REF attribute must be specified in contentLocation element");
                 }
                 // check if datastream is ExternalReferenced
                 if (m_dsControlGrp.equalsIgnoreCase("E") ||
                     m_dsControlGrp.equalsIgnoreCase("R") ) {
                     	
 				  // URL FORMAT VALIDATION for dsLocation:
 				  // make sure we have a properly formed URL
                   try {
                       ValidationUtility.validateURL(dsLocation, false);
                   } catch (ValidationException ve) {
                       throw new SAXException(ve.getMessage());
                   }
                   // system will set dsLocationType for E and R datastreams...
                   m_dsLocationType="URL";
                   m_dsLocation=dsLocation;
                   instantiateDatastream(new DatastreamReferencedContent());
 				  // check if datastream is ManagedContent
                 } else if (m_dsControlGrp.equalsIgnoreCase("M")) {
 				  // URL FORMAT VALIDATION for dsLocation:
 				  // For Managed Content the URL is only checked when we are parsing a
 				  // a NEW ingest file because the URL is replaced with an internal identifier
 				  // once the repository has sucked in the content for storage.
                   if (m_obj.isNew()) {
                     try {
                         ValidationUtility.validateURL(dsLocation, false);
                     } catch (ValidationException ve) {
                         throw new SAXException(ve.getMessage());
                     }
                   }
                   m_dsLocationType="INTERNAL_ID";
 				  m_dsLocation=dsLocation;
 				  instantiateDatastream(new DatastreamManagedContent());
                 }
             } else if (localName.equals("binaryContent")) {
 				// FIXME: implement support for this in Fedora 1.2
             	if (m_dsControlGrp.equalsIgnoreCase("M")) 
                 {
             		m_readingBinaryContent=true;
 					m_binaryContentTempFile = null;
 					try { 
 						m_binaryContentTempFile = File.createTempFile("binary-datastream", null);
 					}
 					catch (IOException ioe)
 					{
 						throw new SAXException(new StreamIOException("Unable to create temporary file for binary content"));
 					}
                 }
 				
 			//==================
 			// DISSEMINATORS...
 			//==================
             } else if (localName.equals("disseminator")) {
 				m_dissID=grab(a, F,"ID");
 				m_bDefID=grab(a, F, "BDEF_CONTRACT_PID");
 				m_dissState=grab(a, F,"STATE");
 				String versionable =grab(a, F, "VERSIONABLE");
 				// disseminator versioning is defaulted to true
 				if (versionable==null || versionable.equals("")) {
 					m_dissVersionable=true;
 				} else {
 					m_dissVersionable=new Boolean(versionable).booleanValue();
 				}
             } else if (localName.equals("disseminatorVersion")) {
 				m_diss = new Disseminator();
 				m_diss.dissID=m_dissID;
 				m_diss.bDefID=m_bDefID;
 				m_diss.dissState=m_dissState;
 				String versionable =grab(a, F, "VERSIONABLE");
 				// disseminator versioning is defaulted to true
 				if (versionable==null || versionable.equals("")) {
 					m_dissVersionable=true;
 				} else {
 					m_dissVersionable=new Boolean(versionable).booleanValue();
 				}
 				m_diss.dissVersionID=grab(a, F,"ID");
 				m_diss.dissLabel=grab(a, F, "LABEL");
 				m_diss.bMechID=grab(a, F, "BMECH_SERVICE_PID");
 				m_diss.dissCreateDT=DateUtility.convertStringToDate(grab(a, F, "CREATED"));
 			} else if (localName.equals("serviceInputMap")) {
 				m_diss.dsBindMap=new DSBindingMap();
 				m_dsBindings = new ArrayList();
 				// Note that the dsBindMapID is not really necessary from the
 				// FOXML standpoint, but it was necessary in METS since the structMap
 				// was outside the disseminator. (Look at how it's used in the sql db.)
 				// Also, the rest of the attributes on the DSBindingMap are not 
 				// really necessary since they are inherited from the disseminator.
 				// I just use the values picked up from disseminatorVersion.
 				m_diss.dsBindMapID=m_diss.dissVersionID + "b";
 				m_diss.dsBindMap.dsBindMapID=m_diss.dsBindMapID;
 				m_diss.dsBindMap.dsBindMechanismPID = m_diss.bMechID;
 				m_diss.dsBindMap.dsBindMapLabel = "";  // does not exist in FOXML
 				m_diss.dsBindMap.state = m_diss.dissState;
 			} else if (localName.equals("datastreamBinding")) {
 				DSBinding dsb = new DSBinding();
 				dsb.bindKeyName = grab(a, F,"KEY");
 				dsb.bindLabel = grab(a, F,"LABEL"); 
 				dsb.datastreamID = grab(a, F,"DATASTREAM_ID");
 				dsb.seqNo = grab(a, F,"ORDER");
 				m_dsBindings.add(dsb);
 			}
         } else {
         	//===============
         	// INLINE XML...
         	//===============
             if (m_inXMLMetadata) {
                 // we are inside an xmlContent element.
                 // just output it, remembering the number of foxml:xmlContent elements we see,
                 appendElementStart(uri, localName, qName, a, m_dsXMLBuffer);
                 
                 // FOXML INSIDE FOXML! we have an inline XML datastream 
                 // that is itself FOXML.  We do not want to parse this!
                 if (uri.equals(F) && localName.equals("xmlContent")) {
                     m_xmlDataLevel++;
                 }
                 
 				// if AUDIT datastream, initialize new audit record object
 				if (m_gotAudit) {
 					if (localName.equals("record")) {
 						m_auditRec=new AuditRecord();
 						m_auditRec.id=grab(a, uri, "ID");
 					} else if (localName.equals("process")) {
 						m_auditProcessType=grab(a, uri, "type");
 					}
 				}
             } else {
                 // ignore all else
             }
         }
     }
 
     private void appendElementStart(String uri,
                                     String localName,
                                     String qName,
                                     Attributes a,
                                     StringBuffer out) {
         out.append("<" + qName);
         // add the current qName's namespace to m_localPrefixMap
         // and m_prefixList if it's not already in m_localPrefixMap
         // This ensures that all namespaces used in inline XML are declared within,
         // since it's supposed to be a standalone chunk.
         String[] parts = qName.split(":");
         if (parts.length == 2) {
             String nsuri = (String) m_localPrefixMap.get(parts[0]);
             if (nsuri == null) {
                 m_localPrefixMap.put(parts[0], parts[1]);
                 m_prefixList.add(parts[0]);
             }
         }
         // do we have any newly-mapped namespaces?
         while (m_prefixList.size() > 0) {
             String prefix = (String) m_prefixList.remove(0);
             out.append(" xmlns");
             if (prefix.length() > 0) {
                 out.append(":");
             }
             out.append(prefix + "=\"" + StreamUtility.enc((String) m_prefixMap.get(prefix)) + "\"");
         }
         for (int i = 0; i < a.getLength(); i++) {
             out.append(" " + a.getQName(i) + "=\"" + StreamUtility.enc(a.getValue(i)) + "\"");
         }
         out.append(">");
     }
 
     public void characters(char[] ch, int start, int length) {
 		// read normal element content into a string buffer
 		if (m_elementContent !=null){
 			m_elementContent.append(ch, start, length);
 		}
 		// read entire inline XML metadata chunks into a buffer
         if (m_inXMLMetadata) {
 			// since this data is encoded straight back to xml,
 			// we need to make sure special characters &, <, >, ", and '
 			// are re-converted to the xml-acceptable equivalents.
 			StreamUtility.enc(ch, start, length, m_dsXMLBuffer);
         } else if (m_readingBinaryContent) {
             // append it to something...
             // FIXME: IMPLEMENT HERE IN POST v2.0
         }
     }
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
 		//==================
 		// INLINE XML...
 		//==================
         if (m_inXMLMetadata) {
 			//=====================
 			// AUDIT DATASTREAM... 
 			//=====================
 			if (m_gotAudit) {
 				// Pick up audit records from the current ds version
 				// and instantiate audit records array in digital object.
 				if (localName.equals("action")) {
 					m_auditAction=m_elementContent.toString();
 				//} else if (localName.equals("recordID")) {
 				//	m_auditRecordID=m_elementContent.toString();
 				} else if (localName.equals("componentID")) {
 					m_auditComponentID=m_elementContent.toString();
 				} else if (localName.equals("responsibility")) {
 					m_auditResponsibility=m_elementContent.toString();
 				} else if (localName.equals("date")) {
 					m_auditDate=m_elementContent.toString();
 				} else if (localName.equals("justification")) {
 					m_auditJustification=m_elementContent.toString();
 				} else if (localName.equals("record")) {
 					//m_auditRec.id=m_auditRecordID;
 					m_auditRec.processType=m_auditProcessType;
 					m_auditRec.action=m_auditAction;
 					m_auditRec.componentID=m_auditComponentID;
 					m_auditRec.responsibility=m_auditResponsibility;
 					m_auditRec.date=DateUtility.convertStringToDate(m_auditDate);
 					m_auditRec.justification=m_auditJustification;
 					// add the audit records to the digital object
 					m_obj.getAuditRecords().add(m_auditRec);
 					// reinit variables for next audit record
 					m_auditProcessType="";
 					m_auditAction="";
 					m_auditComponentID="";
 					m_auditResponsibility="";
 					m_auditDate="";
 					m_auditJustification="";
 				} else if (localName.equals("auditTrail")) {
 					m_gotAudit=false;
 				}
 			// process end of xmlContent ONLY if it is NOT embedded within inline XML!
 			} else if (uri.equals(F) && localName.equals("xmlContent") && m_xmlDataLevel==0) {
 				//=====================
 				// AUDIT DATASTREAM...
 				//=====================
 				if (m_dsId.equals("AUDIT")) {
 					// if we are in the inline XML of the AUDIT datastream just 
 					// end processing and move on.  Audit datastream handled elsewhere.
 					m_inXMLMetadata=false; // other stuff is re-initted upon
 										   // startElement for next xml metadata
 										   // element
 				//========================
 				// ALL OTHER INLINE XML...
 				//========================
 				} else {					
                     // Create the right kind of datastream and add to the object
 					DatastreamXMLMetadata ds=new DatastreamXMLMetadata();
 					instantiateXMLDatastream(ds);
 					m_inXMLMetadata=false;
                     m_localPrefixMap.clear();
 				}
             } else {
                 // finished an element within inline xml metadata
                 m_dsXMLBuffer.append("</" + qName + ">");
                 // make sure we know when to pay attention to FOXML again
                 if (uri.equals(F) && localName.equals("xmlContent")) {
                     m_xmlDataLevel--;
                 }					
             }
         //========================================
         // ALL OTHER ELEMENTS (NOT INLINE XML)...
         //========================================
         } else if (uri.equals(F) && localName.equals("binaryContent")) {
 			// FIXME: Implement functionality for inline base64 datastreams
 			// in a future version (post 2.0)
         	if (m_binaryContentTempFile != null)
             {
 				try {
 	            	FileOutputStream os = new FileOutputStream(m_binaryContentTempFile);
 	            	// remove all spaces and newlines, this might not be necessary.
 	            	String elementStr = m_elementContent.toString().replaceAll("\\s", "");
 	        		byte elementBytes[] =  StreamUtility.decodeBase64(elementStr);
 	        		os.write(elementBytes);
 	        		os.close();
 	            	m_dsLocationType="INTERNAL_ID";
 					m_dsLocation="temp://"+m_binaryContentTempFile.getAbsolutePath();
 					instantiateDatastream(new DatastreamManagedContent());
 				}
 				catch (FileNotFoundException fnfe)
 				{
 					throw new SAXException(new StreamIOException("Unable to open temporary file created for binary content"));
 				}
 				catch (IOException fnfe)
 				{
 					throw new SAXException(new StreamIOException("Error writing to temporary file created for binary content"));				 
 				}
             }
         	m_binaryContentTempFile = null;
 			m_readingBinaryContent=false;
 		} else if (uri.equals(F) && localName.equals("datastreamVersion")) {
 			// reinitialize datastream version-level attributes...
 			m_dsVersId="";
 			m_dsLabel="";
 			m_dsCreateDate=null;
 			m_dsAltIds=new String[0];
 			m_dsFormatURI="";
 			m_dsMimeType="";
 			m_dsSize=-1;
 			//m_dsAdmIds=new HashMap();
 			//m_dsDmdIds=null;
         } else if (uri.equals(F) && localName.equals("datastream")) {
 			// reinitialize datastream attributes ...
 			m_dsId="";
 			m_dsVersionable=true;
 			m_dsState="";
 			m_dsInfoType="";
 			m_dsOtherInfoType="";
 			m_dsMDClass=0;
 		} else if (localName.equals("serviceInputMap")) {
 			m_diss.dsBindMap.dsBindings=(DSBinding[])m_dsBindings.toArray(new DSBinding[0]);
 			m_dsBindings=null;
 		} else if (uri.equals(F) && localName.equals("disseminatorVersion")) {
 			m_obj.disseminators(m_diss.dissID).add(m_diss);
 			m_diss=null;
         } else if (uri.equals(F) && localName.equals("disseminator")) {
 			m_dissID="";
 			m_bDefID="";
 			m_dissState="";
 			m_dissVersionable=true;
         }
 
     }
 
     private static String grab(Attributes a, String namespace,
             String elementName) {
         String ret=a.getValue(namespace, elementName);
         if (ret==null) {
             ret=a.getValue(elementName);
         }
         // set null attribute value to empty string since it's
         // generally helpful in the code to avoid null pointer exception
         // when operations are performed on attributes values.
         if (ret==null) {
         	ret="";
         }
         return ret;
     }
     
     private void instantiateDatastream(Datastream ds) throws SAXException {
 
 		// set datastream variables with values grabbed from the SAX parse    	  	
 		ds.DatastreamID=m_dsId;
 		ds.DatastreamAltIDs=m_dsAltIds;
 		ds.DSVersionable=m_dsVersionable;
 		ds.DSFormatURI=m_dsFormatURI;
 		ds.DSVersionID=m_dsVersId;
 		ds.DSLabel=m_dsLabel;
 		ds.DSCreateDT=m_dsCreateDate;
 		ds.DSMIME=m_dsMimeType;
 		ds.DSControlGrp=m_dsControlGrp;
 		ds.DSState=m_dsState;
 		ds.DSLocation=m_dsLocation;
 		ds.DSLocationType=m_dsLocationType;
 		ds.DSInfoType=""; // METS legacy
 		
 		// Normalize the dsLocation for the deserialization context
 		ds.DSLocation=
 			(DOTranslationUtility.normalizeDSLocationURLs(
 				m_obj.getPid(), ds, m_transContext)).DSLocation;
 				
 		// LOOK! if the query behavior this deserializer instance was activated 
 		// in the constructor,  then we will obtain the datastreams's
 		// content stream to obtain its size and set the ds size attribute
 		// (done within the ds.getContentStream method implementation).
 		if (m_queryBehavior!=QUERY_NEVER) {
 			if ((m_queryBehavior==QUERY_ALWAYS)	
 				|| (m_dsMimeType==null)
 				|| (m_dsMimeType.equals(""))
 				|| (m_dsSize==-1)) {
 				try {
 					InputStream in=ds.getContentStream();
 				} catch (StreamIOException e) {
 					throw new SAXException("Error getting datastream content"
 						+ " for setting ds size (ds.getContentStream)"
 						+ " during SAX parse.");
 				}
 		  }
 		}		
 		// SDP: this is METS specific stuff.  What to do?
 		/*
 		if (m_dsDmdIds!=null) {
 		  for (int idi=0; idi<m_dsDmdIds.length; idi++) {
 			drc.metadataIdList().add(m_dsDmdIds[idi]);
 		  }
 		}
 		*/
 		
 		// FINALLLY! add the datastream to the digital object instantiation
 		m_obj.datastreams(m_dsId).add(ds);
  	
     }
     
 	private void instantiateXMLDatastream(DatastreamXMLMetadata ds) {
 		
 		// set the attrs common to all datastream versions
 		ds.DatastreamID=m_dsId;
 		ds.DatastreamAltIDs=m_dsAltIds;
 		ds.DSVersionable=m_dsVersionable;
 		ds.DSFormatURI=m_dsFormatURI;
 		ds.DSVersionID=m_dsVersId;
 		ds.DSLabel=m_dsLabel;
 		ds.DSCreateDT=m_dsCreateDate;
 		if (m_dsMimeType==null || m_dsMimeType.equals("")) {
 			ds.DSMIME="text/xml";
 		} else {
 			ds.DSMIME=m_dsMimeType;
 		}
 		// set the attrs specific to datastream version
 		ds.DSControlGrp="X";
 		ds.DSState=m_dsState;
 		ds.DSLocation=m_obj.getPid() + "+" + m_dsId + "+" + m_dsVersId;
 		ds.DSLocationType=m_dsLocationType;
 		ds.DSInfoType=m_dsInfoType; // METS legacy
 		ds.DSMDClass=m_dsMDClass;   // METS legacy
 		
 		// now set the xml content stream itself...
 		try {
 			String xmlString = m_dsXMLBuffer.toString();
 		
 			// Relative Repository URL processing... 
 			// For selected inline XML datastreams look for relative repository URLs
 			// and make them absolute.
 			if ( m_obj.getFedoraObjectType()==DigitalObject.FEDORA_BMECH_OBJECT &&
 				 (m_dsId.equals("SERVICE-PROFILE") || m_dsId.equals("WSDL")) ) {
 					ds.xmlContent=
 						(DOTranslationUtility.normalizeInlineXML(
 							xmlString, m_transContext))
 							.getBytes(m_characterEncoding);
 			} else {
 				ds.xmlContent = xmlString.getBytes(m_characterEncoding);
 			}
 			//LOOK! this sets bytes, not characters.  Do we want to set this?
 			ds.DSSize=ds.xmlContent.length;
 		} catch (Exception uee) {
 			System.out.println("Error processing inline xml content in SAX parse: " 
 				+ uee.getMessage());
 		}				
 		// FINALLY! add the xml datastream to the digitalObject
 		m_obj.datastreams(m_dsId).add(ds);  	
 	}	
 	
 	private void checkMETSFormat(String formatURI) {
 		if (formatURI!=null && !formatURI.equals("")) {
 			Matcher m = metsPattern.matcher(formatURI);
 			//Matcher m = metsURI.matcher(formatURI);
 			if (m.lookingAt()) {
 				int index = m.end();
 				StringTokenizer st = 
 					new StringTokenizer(formatURI.substring(index), ".");
 				String mdClass = st.nextToken();
 				if (st.hasMoreTokens()){
 					m_dsInfoType = st.nextToken();
 				}
 				if (st.hasMoreTokens()){
 					m_dsOtherInfoType = st.nextToken();
 				}
 				if (mdClass.equals("techMD")) { m_dsMDClass = 1;
 				} else if (mdClass.equals("sourceMD")) { m_dsMDClass = 2;
 				} else if (mdClass.equals("rightsMD")) { m_dsMDClass = 3;
 				} else if (mdClass.equals("digiprovMD")) { m_dsMDClass = 4;
 				} else if (mdClass.equals("descMD")) { m_dsMDClass = 5; }
 				if (m_dsInfoType.equals("OTHER")) {
 					m_dsInfoType = m_dsOtherInfoType;
 				}			
 			}
 		}		
 	}
 	
 	private Datastream getCurrentDS(List allVersions){
 		if (allVersions.size()==0) {
 			return null;
 		}
 		Iterator dsIter=allVersions.iterator();
 		Datastream mostRecentDS=null;
 		long mostRecentDSTime=-1;
 		while (dsIter.hasNext()) {
 			Datastream ds=(Datastream) dsIter.next();
 			long dsTime = ds.DSCreateDT.getTime();
 			if (dsTime > mostRecentDSTime) {
 				mostRecentDSTime=dsTime;
 				mostRecentDS=ds;
 			}
 		}
 		return mostRecentDS;
 	}
 
 	private void initialize(){
 
 		// temporary variables and state variables
 		m_rootElementFound=false;
 		m_objPropertyName="";
 		m_readingBinaryContent=false; // indicates reading base64-encoded content
 		m_inXMLMetadata=false;
         m_prefixMap = new HashMap();
         m_localPrefixMap = new HashMap();
         m_prefixList = new ArrayList();
 
 		// temporary variables for processing datastreams		
 		m_dsId="";
 		m_dsVersionable=true;
 		m_dsVersId="";
 		m_dsCreateDate=null;
 		m_dsState="";
 		m_dsFormatURI="";
 		m_dsSize=-1;
 		m_dsLocationType="";
 		m_dsLocationURL=null;
 		m_dsLocation="";
 		m_dsMimeType="";
 		m_dsControlGrp="";
 		m_dsInfoType="";
 		m_dsOtherInfoType="";
 		m_dsMDClass=0;
 		m_dsLabel="";
 		m_dsXMLBuffer=null;
 		
 		// temporary variables for processing disseminators
 		m_diss=null;
 		m_dissID="";
 		m_bDefID="";
 		m_dissState="";
 		m_dissVersionable=true;
 		m_dsBindMap=null;
 		m_dsBindings=null;
 		
 		// temporary variables for processing audit records
 		m_auditRec=null;	
 		m_gotAudit=false;
 		//m_auditRecordID=null;
 		m_auditComponentID="";
 		m_auditProcessType="";
 		m_auditAction="";
 		m_auditResponsibility="";
 		m_auditDate="";
 		m_auditJustification="";
 	}
   }
