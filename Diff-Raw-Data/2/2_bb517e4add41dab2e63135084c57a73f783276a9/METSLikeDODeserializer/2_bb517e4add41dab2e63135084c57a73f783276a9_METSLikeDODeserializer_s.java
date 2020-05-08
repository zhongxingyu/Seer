 /* The contents of this file are subject to the license and copyright terms
  * detailed in the license directory at the root of the source tree (also 
  * available online at http://www.fedora.info/license/).
  */
 
 package fedora.server.storage.translation;
 
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
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import org.apache.log4j.Logger;
 
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
 //import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DSBindingMap;
 import fedora.server.storage.types.DSBinding;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.StreamUtility;
 import fedora.server.validation.ValidationUtility;
 
 /**
  * Deserializes XML digital object encoded in accordance 
  * with the Fedora extension of the METS schema defined at: 
  * http://www.fedora.info/definitions/1/0/mets-fedora-ext.xsd.
  * 
  * The METS XML is parsed using SAX and is instantiated into a Fedora
  * digital object in memory (see fedora.server.types.DigitalObject).
  *
  * @author cwilper@cs.cornell.edu
  * @author payette@cs.cornell.edu
  * @version $Id$
  */
 public class METSLikeDODeserializer
         extends DefaultHandler
         implements DODeserializer {
 
     /** Logger for this class. */
     private static final Logger LOG = Logger.getLogger(
             METSLikeDODeserializer.class.getName());
 
     /** The namespace for METS */
     private final static String M="http://www.loc.gov/METS/";
 
     /** The namespace for XLINK */
     private final static String XLINK_NAMESPACE="http://www.w3.org/TR/xlink";
     // Mets says the above, but the spec at http://www.w3.org/TR/xlink/
     // says it's http://www.w3.org/1999/xlink
     
 	/** Namespace declarations for RDF */   
 	public static final String RDF_PREFIX="rdf";
 	public static final String RDF_NS="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
 	public static final String RDFS_PREFIX="rdfs";
 	public static final String RDFS_NS="http://www.w3.org/2000/01/rdf-schema#";
 	public static final String RELS_PREFIX="fedora";
 	public static final String RELS_NS="info:fedora/fedora-system:def/relations-internal#";
 	
 	/** Buffer to build RDF expression of ADMID and DMDID relationships **/
 	private StringBuffer m_relsBuffer;
 	private boolean hasRels=false;
 	
 	/** Hashtables to record DMDID references */
 	private HashMap m_dsDMDIDs; // key=dsVersionID, value=ArrayList of dsID
 	/** Hashtables to record ADMID references */
 	private HashMap m_dsADMIDs; // key=dsVersionID, value=ArrayList of dsID
 	/** Hashtables to correlate audit record ids to datastreams */
 	private HashMap m_AuditIdToComponentId;
 
     private SAXParser m_parser;
     private String m_characterEncoding;
 
     /** The object to deserialize to. */
     private DigitalObject m_obj;
 
     /** Namespace prefix-to-URI mapping info from SAX2 startPrefixMapping events. */
     private HashMap<String, String> m_prefixMap;
     private HashMap<String, String> m_localPrefixMap;
     private ArrayList<String> m_prefixList;
 
 	/** Variables to parse into */
     private boolean m_rootElementFound;
     private String m_agentRole;
     private String m_dsId;
     private String m_dsVersId;
     private Date m_dsCreateDate;
     private String m_dissemId;
     private String m_dissemState;
     private String m_dsState;
     private String m_dsInfoType;
 	private String m_dsOtherInfoType;
     private String m_dsLabel;
     private int m_dsMDClass;
     private long m_dsSize;
 	private URL m_dsLocationURL;
 	private String m_dsLocation;
 	private String m_dsLocationType;
     private String m_dsMimeType;
     private String m_dsControlGrp;
     private boolean m_dsVersionable;
     private String m_dsFormatURI;
     private String[] m_dsAltIDs;
     private String m_dsChecksum;
     private String m_dsChecksumType;
 
     private StringBuffer m_dsXMLBuffer;
     
     // are we reading binary in an FContent element? (base64-encoded)
 	private boolean m_readingContent; // indicates reading element content
 	private boolean m_readingBinaryContent; // indicates reading binary element content
 	private File m_binaryContentTempFile;
 	private StringBuffer m_elementContent; // single element
 
     /** While parsing, are we inside XML metadata? */
     private boolean m_inXMLMetadata;
 
     /**
      * Used to differentiate between a metadata section in this object
      * and a metadata section in an inline XML datastream that happens
      * to be a METS document.
      */
     private int m_xmlDataLevel;
 
     /** String buffer for audit element contents */
     private StringBuffer m_auditBuffer;
     private String m_auditProcessType;
     private String m_auditAction;
 	private String m_auditComponentID;
     private String m_auditResponsibility;
     private String m_auditDate;
     private String m_auditJustification;
 
 
     /** Hashmap for holding disseminators during parsing, keyed
      * by structMapId */
     private HashMap m_dissems;
 
 //    /**
 //     * Currently-being-initialized disseminator, during structmap parsing.
 //     */
 //    private Disseminator m_diss;
 
     /**
      * Whether, while in structmap, we've already seen a div
      */
     private boolean m_indiv;
 
     /** The structMapId of the dissem currently being parsed. */
     private String m_structId;
 
     /**
      * For non-inline datastreams, never query the server to get values 
      * for Content-length and Content-type
      */
     public static int QUERY_NEVER=0;
 
     /**
      * For non-inline datastreams, conditionally query the server to get values 
      * for Content-length and Content-type (if either are undefined).
      */
     public static int QUERY_IF_UNDEFINED=1;
 
     /**
      * For non-inline datastreams, always query the server to get values
      * for Content-length and Content-type.
      */
     public static int QUERY_ALWAYS=2;
 
     private int m_queryBehavior;
 
 	/** The translation context for deserialization */
     private int m_transContext;
 
     public METSLikeDODeserializer()
             throws FactoryConfigurationError, ParserConfigurationException,
             SAXException, UnsupportedEncodingException {
         this("UTF-8", false, QUERY_NEVER);
     }
 
     /**
      * Initializes by setting up a parser that doesn't validate and never
      * queries the server for values of DSSize and DSMIME.
      */
     public METSLikeDODeserializer(String characterEncoding)
             throws FactoryConfigurationError, ParserConfigurationException,
             SAXException, UnsupportedEncodingException {
         this(characterEncoding, false, QUERY_NEVER);
     }
 
     /**
      * Initializes by setting up a parser that validates only if validate=true.
      * <p></p>
      * The character encoding of the XML is auto-determined by SAX, but
      * we need it for when we set the byte[] in DatastreamXMLMetadata, so
      * we effectively, we need to also specify the encoding of the datastreams.
      * this could be different than how the digital object xml was encoded,
      * and this class won't care.  However, the caller should keep track
      * of the byte[] encoding if it plans on doing any translation of
      * that to characters (such as in xml serialization)
      */
     public METSLikeDODeserializer(String characterEncoding, boolean validate,
             int queryBehavior)
             throws FactoryConfigurationError, ParserConfigurationException,
             SAXException, UnsupportedEncodingException {
         m_queryBehavior=queryBehavior;
         // ensure the desired encoding is supported before starting
         // unsuppenc will be thrown if not
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
             return (DODeserializer) new METSLikeDODeserializer("UTF-8", false, QUERY_NEVER);
         } catch (Exception e) {
             throw new RepositoryConfigurationException("Error trying to get a "
                     + "new METSLikeDODeserializer instance: " + e.getClass().getName()
                     + ": " + e.getMessage());
         }
     }
 
     public void deserialize(InputStream in, DigitalObject obj, String encoding, int transContext)
             throws ObjectIntegrityException, StreamIOException, UnsupportedEncodingException {
 		LOG.debug("Deserializing METS (Fedora extension)...");
         m_obj=obj;
 		m_transContext=transContext;
 		initialize();       
         try {
             m_parser.parse(in, this);
         } catch (IOException ioe) {
             throw new StreamIOException("Low-level stream IO problem occurred "
                     + "while SAX parsing this object.");
         } catch (SAXException se) {
             throw new ObjectIntegrityException("METS stream was bad : " + se.getMessage());
         }
         if (!m_rootElementFound) {
             throw new ObjectIntegrityException("METS root element not found :"
             	+ " Must have 'mets' element in namespace " + M + " as root element.");
         }
         m_obj.setNamespaceMapping(new HashMap());
         
         // POST-PROCESSING...
         // convert audit records to contain component ids
 		convertAudits();
 		// preserve ADMID and DMDID relationships in a RELS-INT
 		// datastream, if one does not already exist.
 		createRelsInt();        
 		        		
 //        // DISSEMINATORS... put disseminators in the instantiated digital object
 //        Iterator dissemIter=m_dissems.values().iterator();
 //        while (dissemIter.hasNext()) {
 //            Disseminator diss=(Disseminator) dissemIter.next();
 //            m_obj.disseminators(diss.dissID).add(diss);
 //        }
     }
    
 	public void startPrefixMapping(String prefix, String uri) {
         // Keep the prefix map up-to-date throughout the entire parse,
         // and maintain a list of newly mapped prefixes on a per-element basis.
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
         if (uri.equals(M) && !m_inXMLMetadata) {
             // a new mets element is starting
             if (localName.equals("mets")) {
                 m_rootElementFound=true;
                 m_obj.setPid(grab(a, M, "OBJID"));
                 m_obj.setLabel(grab(a, M, "LABEL"));
                 m_obj.setContentModelId(grab(a, M, "PROFILE"));
                 String objType=grab(a, M, "TYPE");
                 if (objType==null || objType.equals("")) { objType="FedoraObject"; }
                 if (objType.indexOf("FedoraBDefObject")!= -1) 
                 {
                     m_obj.addFedoraObjectType(DigitalObject.FEDORA_BDEF_OBJECT);
                 } 
                 if (objType.equalsIgnoreCase("FedoraBMechObject")) 
                 {
                     m_obj.addFedoraObjectType(DigitalObject.FEDORA_BMECH_OBJECT);
                 } 
                 if (objType.equalsIgnoreCase("FedoraCModelObject")) 
                 {
                    m_obj.addFedoraObjectType(DigitalObject.FEDORA_BMECH_OBJECT);
                 } 
                 if (objType.equalsIgnoreCase("FedoraObject")) 
                 {
                     m_obj.addFedoraObjectType(DigitalObject.FEDORA_OBJECT);
                 }
             } else if (localName.equals("metsHdr")) {
                 m_obj.setCreateDate(DateUtility.convertStringToDate(
                         grab(a, M, "CREATEDATE")));
                 m_obj.setLastModDate(DateUtility.convertStringToDate(
                         grab(a, M, "LASTMODDATE")));
                 m_obj.setState(grab(a, M, "RECORDSTATUS"));
             } else if (localName.equals("agent")) {
                 m_agentRole = grab(a, M, "ROLE");
             } else if (localName.equals("name") && m_agentRole.equals("IPOWNER")) {
             	m_readingContent=true;
                 m_elementContent = new StringBuffer();
             } else if (localName.equals("amdSec")) {
                 m_dsId=grab(a, M, "ID");
                 m_dsState=grab(a, M, "STATUS");
                 String dsVersionable = grab(a, M, "VERSIONABLE");
                 if (dsVersionable!=null && !dsVersionable.equals("")) {
                 	m_dsVersionable=new Boolean(grab(a, M, "VERSIONABLE")).booleanValue();
                 } else {
                 	m_dsVersionable = true;
                 }                
             } else if (localName.equals("dmdSecFedora")) {
                 m_dsId=grab(a, M, "ID");
                 m_dsState=grab(a, M, "STATUS");
                 String dsVersionable = grab(a, M, "VERSIONABLE");
                 if (dsVersionable!=null && !dsVersionable.equals("")) {
                 	m_dsVersionable=new Boolean(grab(a, M, "VERSIONABLE")).booleanValue();
                 } else {
                 	m_dsVersionable = true;
                 }
             } else if (localName.equals("techMD") || localName.equals("descMD")
                     || localName.equals("sourceMD")
                     || localName.equals("rightsMD")
                     || localName.equals("digiprovMD")) {
                 m_dsVersId=grab(a, M, "ID");
                 if (localName.equals("techMD")) {
                     m_dsMDClass=DatastreamXMLMetadata.TECHNICAL;
                 }
                 if (localName.equals("sourceMD")) {
                     m_dsMDClass=DatastreamXMLMetadata.SOURCE;
                 }
                 if (localName.equals("rightsMD")) {
                     m_dsMDClass=DatastreamXMLMetadata.RIGHTS;
                 }
                 if (localName.equals("digiprovMD")) {
                     m_dsMDClass=DatastreamXMLMetadata.DIGIPROV;
                 }
                 if (localName.equals("descMD")) {
                     m_dsMDClass=DatastreamXMLMetadata.DESCRIPTIVE;
                 }
                 String dateString=grab(a, M, "CREATED");
                 if (dateString!=null && !dateString.equals("")){
 					m_dsCreateDate=
 						DateUtility.convertStringToDate(dateString);
                 }
             } else if (localName.equals("mdWrap")) {
                 m_dsInfoType=grab(a, M, "MDTYPE");
 				m_dsOtherInfoType=grab(a, M, "OTHERMDTYPE");
                 m_dsLabel=grab(a, M, "LABEL");
                 m_dsMimeType=grab(a, M, "MIMETYPE");
                 m_dsFormatURI=grab(a, M, "FORMAT_URI");
                 String altIDs= grab(a, M, "ALT_IDS");
 				if (altIDs.length() == 0) {
 					m_dsAltIDs = new String[0];
 				} else {
 					m_dsAltIDs = altIDs.split(" ");
 				}               
                 m_dsChecksum = grab(a, M, "CHECKSUM");
                 m_dsChecksumType = grab(a, M, "CHECKSUMTYPE");
             } else if (localName.equals("xmlData")) {
                 m_dsXMLBuffer=new StringBuffer();
                 m_xmlDataLevel=0;
                 m_inXMLMetadata=true;
             } else if (localName.equals("fileGrp")) {
                 m_dsId=grab(a, M, "ID");
                 String dsVersionable = grab(a, M, "VERSIONABLE");
                 if (dsVersionable!=null && !dsVersionable.equals("")) {
                 	m_dsVersionable=new Boolean(grab(a, M, "VERSIONABLE")).booleanValue();
                 } else {
                 	m_dsVersionable = true;
                 }                
                 // reset the values for the next file
                 m_dsVersId="";
                 m_dsCreateDate=null;
                 m_dsMimeType="";
                 m_dsControlGrp="";
                 m_dsFormatURI="";
                 m_dsAltIDs=new String[0];
                 m_dsState=grab(a,M,"STATUS");
                 m_dsSize=-1;
                 m_dsChecksum="";
                 m_dsChecksumType="";
             } else if (localName.equals("file")) {
                 m_dsVersId=grab(a, M, "ID");
 				String dateString=grab(a, M, "CREATED");
 				if (dateString!=null && !dateString.equals("")){
 					m_dsCreateDate=
 						DateUtility.convertStringToDate(dateString);
 				}
                 m_dsMimeType=grab(a,M,"MIMETYPE");
                 m_dsControlGrp=grab(a,M,"OWNERID");
                 String ADMID=grab(a,M,"ADMID");
                 if ((ADMID!=null) && (!"".equals(ADMID))) {
                     ArrayList al=new ArrayList();
                     if (ADMID.indexOf(" ")!=-1) {
                         String[] admIds=ADMID.split(" ");
                         for (int idi=0; idi<admIds.length; idi++) {
                             al.add(admIds[idi]);
                         }
                     } else {
                         al.add(ADMID);
                     }
                     m_dsADMIDs.put(m_dsVersId, al);
                 }
 				String DMDID=grab(a,M,"DMDID");
 				if ((DMDID!=null) && (!"".equals(DMDID))) {
 					ArrayList<String> al=new ArrayList<String>();
 					if (DMDID.indexOf(" ")!=-1) {
 						String[] dmdIds=DMDID.split(" ");
 						for (int idi=0; idi<dmdIds.length; idi++) {
 							al.add(dmdIds[idi]);
 						}
 					} else {
 						al.add(DMDID);
 					}
 					m_dsDMDIDs.put(m_dsVersId, al);
 				}
                 String sizeString=grab(a,M,"SIZE");
                 if (sizeString!=null && !sizeString.equals("")) {
                     try {
                         m_dsSize=Long.parseLong(sizeString);
                     } catch (NumberFormatException nfe) {
                         throw new SAXException("If specified, a datastream's "
                                 + "SIZE attribute must be an xsd:long.");
                     }
                 }
                 String formatURI=grab(a, M, "FORMAT_URI");
                 if (formatURI!=null && !formatURI.equals("")) {
                 	m_dsFormatURI=formatURI;
                 }
                 String altIDs=grab(a, M, "ALT_IDS");
 				if (altIDs.length() == 0) {
 					m_dsAltIDs = new String[0];
 				} else {
 					m_dsAltIDs = altIDs.split(" ");
 				}  
 				m_dsChecksum = grab(a, M, "CHECKSUM");
 				m_dsChecksumType = grab(a, M, "CHECKSUMTYPE");
                 // inside a "file" element, it's either going to be
                 // FLocat (a reference) or FContent (inline)
             } else if (localName.equals("FLocat")) {
                 m_dsLabel=grab(a,XLINK_NAMESPACE,"title");
                 String dsLocation=grab(a,XLINK_NAMESPACE,"href");
                 if (dsLocation==null || dsLocation.equals("")) {
                     throw new SAXException("xlink:href must be specified in FLocat element");
                 }
 
                 if (m_dsControlGrp.equalsIgnoreCase("E") ||
                     m_dsControlGrp.equalsIgnoreCase("R") ) {
                     	
 				  // URL FORMAT VALIDATION for dsLocation:
 				  // make sure we have a properly formed URL (must have protocol)
                   try {
                       ValidationUtility.validateURL(dsLocation, false);
                   } catch (ValidationException ve) {
                       throw new SAXException(ve.getMessage());
                   }
 				  // system will set dsLocationType for E and R datastreams...
 				  m_dsLocationType="URL";
 				  m_dsInfoType="DATA";
 				  m_dsLocation=dsLocation;
 				  instantiateDatastream(new DatastreamReferencedContent());
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
 				  m_dsInfoType="DATA";
 				  m_dsLocation=dsLocation;
 				  instantiateDatastream(new DatastreamManagedContent());
                 }
             } else if (localName.equals("FContent")) {
             	// In the version of METS that Fedora supports, the FContent element
             	// contains base64 encoded data.
                 m_readingContent=true;
 				m_elementContent = new StringBuffer();
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
 
             } 
 //            else if (localName.equals("structMap")) 
 //            {
 //                // this is a component of a disseminator.  here we assume the rest
 //                // of the disseminator's information will be seen later, so we
 //                // construct a new Disseminator object to hold the structMap...
 //                // and later, the other info
 //                //
 //                // Building up a global map of Disseminators, m_dissems,
 //                // keyed by bindingmap ID.
 //                //
 //                if (grab(a,M,"TYPE").equals("fedora:dsBindingMap")) 
 //                {
 //                    String bmId=grab(a,M,"ID");
 //                    if ( (bmId==null) || (bmId.equals("")) ) 
 //                    {
 //                        throw new SAXException("structMap with TYPE fedora:dsBindingMap must specify a non-empty ID attribute.");
 //                    } 
 //                    else 
 //                    {
 //                        Disseminator diss=new Disseminator();
 //                        diss.dsBindMapID=bmId;
 //                        m_dissems.put(bmId,diss);
 //                        m_diss=diss;
 //                        m_diss.dsBindMap=new DSBindingMap();
 //                        m_diss.dsBindMap.dsBindMapID=bmId;
 //                        m_indiv=false; // flag that we're not looking at inner part yet
 //                    }
 //                } else {
 //                    throw new SAXException("StructMap must have TYPE fedora:dsBindingMap");
 //                }
 //            } 
 //            else if (localName.equals("div")) 
 //            {
 //                if (m_indiv) {
 //                    // inner part of structmap
 //                    DSBinding binding=new DSBinding();
 //                    if (m_diss.dsBindMap.dsBindings==null) {
 //                        // none yet.. create array of size one
 //                        DSBinding[] bindings=new DSBinding[1];
 //                        m_diss.dsBindMap.dsBindings=bindings;
 //                        m_diss.dsBindMap.dsBindings[0]=binding;
 //                    } else {
 //                        // need to expand the array size by one,
 //                        // and do an array copy.
 //                        int curSize=m_diss.dsBindMap.dsBindings.length;
 //                        DSBinding[] oldArray=m_diss.dsBindMap.dsBindings;
 //                        DSBinding[] newArray=new DSBinding[curSize+1];
 //                        for (int i=0; i<curSize; i++) {
 //                            newArray[i]=oldArray[i];
 //                        }
 //                        newArray[curSize]=binding;
 //                        m_diss.dsBindMap.dsBindings=newArray;
 //                    }
 //                    // now populate 'binding' values...we'll have
 //                    // everything at this point except datastreamID...
 //                    // that comes as a child: <fptr FILEID="DS2"/>
 //                    binding.bindKeyName=grab(a,M,"TYPE");
 //                    binding.bindLabel=grab(a,M,"LABEL");
 //                    binding.seqNo=grab(a,M,"ORDER");
 //                } else {
 //                    m_indiv=true;
 //                    // first (outer div) part of structmap
 //                    m_diss.dsBindMap.dsBindMechanismPID=grab(a,M,"TYPE");
 //                    m_diss.dsBindMap.dsBindMapLabel=grab(a,M,"LABEL");
 //                }
 //            } 
 //            else if (localName.equals("fptr")) 
 //            {
 //                // assume we're inside the inner div... that's the
 //                // only place the fptr element is valid.
 //                DSBinding binding=m_diss.dsBindMap.dsBindings[
 //                        m_diss.dsBindMap.dsBindings.length-1];
 //                binding.datastreamID=grab(a,M,"FILEID");
 //            } 
             else if (localName.equals("behaviorSec")) 
             {
                 // looks like we're in a disseminator... it should be in the
                 // hash by now because we've already gone through structmaps
                 // ...keyed by structmap id... remember the id (group id)
                 // so we can put it in when parsing serviceBinding
                 m_dissemId=grab(a,M,"ID");
                 m_dissemState=grab(a,M,"STATUS");
             } 
 //            else if (localName.equals("serviceBinding")) 
 //            {
 //                // remember the structId so we can grab the right dissem
 //                // when parsing children
 //                m_structId=grab(a,M,"STRUCTID");
 //                // grab the disseminator associated with the provided structId
 //                Disseminator dissem=(Disseminator) m_dissems.get(m_structId);
 //                // plug known items in..
 //                dissem.dissID=m_dissemId;
 //                dissem.dissState=m_dissemState;
 //                // then grab the new stuff for the dissem for this element, and
 //                // put it in.
 //                dissem.dissVersionID=grab(a,M,"ID");
 //                dissem.bDefID=grab(a,M,"BTYPE");
 //                dissem.dissCreateDT=DateUtility.convertStringToDate(grab(a,M,"CREATED"));
 //                dissem.dissLabel=grab(a,M,"LABEL");
 //            } 
 //            else if (localName.equals("interfaceMD")) 
 //            {
 //                Disseminator dissem=(Disseminator) m_dissems.get(m_structId);
 //                // already have the id from containing element, just need label
 //                //dissem.bDefLabel=grab(a,M,"LABEL");
 //            } 
 //            else if (localName.equals("serviceBindMD")) 
 //            {
 //                Disseminator dissem=(Disseminator) m_dissems.get(m_structId);
 //                //dissem.bMechLabel=grab(a,M,"LABEL");
 //                dissem.bMechID=grab(a,XLINK_NAMESPACE,"href");
 //            }
         } else {
             if (m_inXMLMetadata) {
                 // must be in xmlData... just output it, remembering the number
                 // of METS:xmlData elements we see
                 appendElementStart(uri, localName, qName, a, m_dsXMLBuffer);
 
                 // METS INSIDE METS! we have an inline XML datastream 
                 // that is itself METS.  We do not want to parse this!
                 if (uri.equals(M) && localName.equals("xmlData")) {
                     m_xmlDataLevel++;
                 }
                 // remember this stuff... (we don't have to look at level
                 // because the audit schema doesn't allow for xml elements inside
                 // these, so they're never set incorrectly)
                 // signaling that we're interested in sending char data to
                 // the m_auditBuffer by making it non-null, and getting
                 // ready to accept data by allocating a new StringBuffer
                 if (m_dsId.equals("FEDORA-AUDITTRAIL") || m_dsId.equals("AUDIT")) {
 					if (localName.equals("process")) {
 						m_auditProcessType=grab(a, uri, "type");              	
                     } else if ( (localName.equals("action"))
 							|| (localName.equals("componentID"))
                             || (localName.equals("responsibility"))
                             || (localName.equals("date"))
                             || (localName.equals("justification")) ) {
                         m_auditBuffer=new StringBuffer();
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
         if (m_inXMLMetadata) {
             if (m_auditBuffer!=null) {
                 m_auditBuffer.append(ch, start, length);
             } else {
                 // since this data is encoded straight back to xml,
                 // we need to make sure special characters &, <, >, ", and '
                 // are re-converted to the xml-acceptable equivalents.
                 StreamUtility.enc(ch, start, length, m_dsXMLBuffer);
             }
         } else if (m_readingContent) {
     		// read normal element content into a string buffer
     		if (m_elementContent !=null)
     		{
     			m_elementContent.append(ch, start, length);
     		}
         }
     }
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
     	// first, deal with the situation when we are processing a block of inline XML
         if (m_inXMLMetadata) 
         {
             if (uri.equals(M) && localName.equals("xmlData")
                     && m_xmlDataLevel==0) {
                 // finished all xml metadata for this datastream
                 if (m_dsId.equals("FEDORA-AUDITTRAIL") || m_dsId.equals("AUDIT")) {
                     // we've been looking at an audit trail... set audit record
                     AuditRecord a=new AuditRecord();
                     // In METS each audit record is in its own <digiprovMD>
                     // element within an <amdSec>.  So, pick up the XML ID
                     // of the <digiprovMD> element for the audit record id.
                     // This amdSec is treated like a datastream, and each 
                     // digiprovMD is a version, so id was parsed into dsVersId.
                     a.id=m_dsVersId; 
                     a.processType=m_auditProcessType;
                     a.action=m_auditAction;
                     a.componentID=m_auditComponentID;
                     a.responsibility=m_auditResponsibility;
                     a.date=DateUtility.convertStringToDate(m_auditDate);
                     a.justification=m_auditJustification;
                     m_obj.getAuditRecords().add(a);
 					m_inXMLMetadata=false; // other stuff is re-initted upon
 										   // startElement for next xml metadata
 										   // element
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
                 // make sure we know when to pay attention to METS again
                 if (uri.equals(M) && localName.equals("xmlData")) {
                     m_xmlDataLevel--;
                 }
                 if (m_dsId.equals("FEDORA-AUDITTRAIL") || m_dsId.equals("AUDIT")) {
                     if (localName.equals("action")) {
                         m_auditAction=m_auditBuffer.toString();
                         m_auditBuffer=null;
 					} else if (localName.equals("componentID")) {
 						m_auditComponentID=m_auditBuffer.toString();
 						m_auditBuffer=null;
                     } else if (localName.equals("responsibility")) {
                         m_auditResponsibility=m_auditBuffer.toString();
                         m_auditBuffer=null;
                     } else if (localName.equals("date")) {
                         m_auditDate=m_auditBuffer.toString();
                         m_auditBuffer=null;
                     } else if (localName.equals("justification")) {
                         m_auditJustification=m_auditBuffer.toString();
                         m_auditBuffer=null;
                     }
                 }
             }
         // ALL OTHER ELEMENT CASES: we are NOT processing a block of inline XML metadata
         } else {
             if (m_readingBinaryContent)
             {
             	// In the version of METS Fedora uses, FContent assumes base64-encoded content
             	if (uri.equals(M) && localName.equals("FContent"))
             	{
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
             	}
             	m_binaryContentTempFile = null;
     			m_readingBinaryContent=false;
     			m_elementContent = null;  			
     		// all other cases...
             } else {
 	            if (m_readingContent) {
 	    	        // elements for which we were reading regular content
 	    	        if (uri.equals(M) && localName.equals("name") && m_agentRole.equals("IPOWNER")) {
 	    	        	m_obj.setOwnerId(m_elementContent.toString());
 	    	        } else if (uri.equals(M) && localName.equals("agent")) {
 	    	        	m_agentRole = null;       		
 	    	        } 
 		            m_readingContent=false;
 		        	m_elementContent = null;
 	            } else {
 	            	// no other processing requirements at this time
 	            }
     		}
         }
     }
     
 	private void instantiateDatastream(Datastream ds) throws SAXException {
 
 		// set datastream variables with values grabbed from the SAX parse     	  	
 		ds.DatastreamID=m_dsId;
 		ds.DSVersionable=m_dsVersionable;
 		ds.DSFormatURI=m_dsFormatURI;
 		ds.DatastreamAltIDs=m_dsAltIDs;
 		ds.DSVersionID=m_dsVersId;
 		ds.DSLabel=m_dsLabel;
 		ds.DSCreateDT=m_dsCreateDate;
 		ds.DSMIME=m_dsMimeType;
 		ds.DSControlGrp=m_dsControlGrp;
 		ds.DSState=m_dsState;
 		ds.DSLocation=m_dsLocation;
 		ds.DSLocationType=m_dsLocationType;
 		ds.DSInfoType=m_dsInfoType;
 
 		ds.DSChecksumType=m_dsChecksumType;
         LOG.debug("instantiate datastream: dsid = "+ m_dsId + 
                 "checksumType = "+ m_dsChecksumType +
                 "checksum = "+ m_dsChecksum);
         if (m_obj.isNew()) 
         {
             if (m_dsChecksum != null && !m_dsChecksum.equals("") && !m_dsChecksum.equals("none"))
             {
                 String tmpChecksum = ds.getChecksum();
                 LOG.debug("checksum = "+ tmpChecksum);
                 if (!m_dsChecksum.equals(tmpChecksum))
                 {
                     throw new SAXException(new ValidationException("Checksum Mismatch: " + tmpChecksum));
                 }
             }
             ds.DSChecksumType=ds.getChecksumType();
         }
         else
         {
             ds.DSChecksum = m_dsChecksum;
         }
 		
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
 		// FINALLY! add the datastream to the digital object instantiation
 		m_obj.addDatastreamVersion(ds, true);	
 	}
 
 	private void instantiateXMLDatastream(DatastreamXMLMetadata ds) throws SAXException 
     {
 		
 		// set the attrs common to all datastream versions
 		ds.DatastreamID=m_dsId;
 		ds.DSVersionable=m_dsVersionable;
 		ds.DSFormatURI=m_dsFormatURI;
 		ds.DatastreamAltIDs=m_dsAltIDs;
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
 		ds.DSInfoType=m_dsInfoType; // METS only
 		ds.DSMDClass=m_dsMDClass;   // METS only
 		ds.DSChecksumType=m_dsChecksumType;
 		
 		// now set the xml content stream itself...
 		try {
 			String xmlString = m_dsXMLBuffer.toString();
 		
 			// Relative Repository URL processing... 
 			// For selected inline XML datastreams look for relative repository URLs
 			// and make them absolute.
 			if ( m_obj.isFedoraObjectType(DigitalObject.FEDORA_BMECH_OBJECT) &&
 				 (m_dsId.equals("SERVICE-PROFILE") || m_dsId.equals("WSDL")) ) 
             {
 				ds.xmlContent=
 					(DOTranslationUtility.normalizeInlineXML(
 						xmlString, m_transContext))
 						.getBytes(m_characterEncoding);
 			} 
             else 
             {
 				ds.xmlContent = xmlString.getBytes(m_characterEncoding);
 			}
 			//LOOK! this sets bytes, not characters.  Do we want to set this?
 			ds.DSSize=ds.xmlContent.length;
 		} catch (Exception uee) {
 			LOG.debug("Error processing inline xml content in SAX parse: " 
 				+ uee.getMessage());
 		}				
         
         LOG.debug("instantiate datastream: dsid = "+ m_dsId + 
                 "checksumType = "+ m_dsChecksumType +
                 "checksum = "+ m_dsChecksum);
         if (m_obj.isNew()) 
         {
             if (m_dsChecksum != null && !m_dsChecksum.equals("") && !m_dsChecksum.equals("none"))
             {
                 String tmpChecksum = ds.getChecksum();
                 LOG.debug("checksum = "+ tmpChecksum);
                 if (!m_dsChecksum.equals(tmpChecksum))
                 {
                     throw new SAXException(new ValidationException("Checksum Mismatch: " + tmpChecksum));
                 }
             }
             ds.DSChecksumType=ds.getChecksumType();
         }
         else
         {
             ds.DSChecksum = m_dsChecksum;
         }
 		// FINALLY! add the xml datastream to the digitalObject
 		m_obj.addDatastreamVersion(ds, true);  	
 	}
 
 
 	/**
 	 *  convertAudits: In Fedora 2.0 and beyond, we want self-standing audit
 	 *  records.  Make sure audit records are converted to new format
 	 *  that contains a componentID to show what component in the object 
 	 *  the audit record is about. 
 	 */
 	private void convertAudits(){
 		// Only do this if ADMID values were found in the object.
 		if (m_dsADMIDs.size()>0){
 			// Look at datastreams to see if there are audit records for them.
 			// NOTE:  we do not look at disseminators because in pre-2.0
 			// the disseminators did not point to their audit records as
 			// did the datastreams.
 			Iterator dsIdIter=m_obj.datastreamIdIterator();
 			while (dsIdIter.hasNext()) {
 				List datastreams=m_obj.datastreams((String) dsIdIter.next());
 				// The list is a set of versions of the same datastream...
 				for (int i=0; i<datastreams.size(); i++) {
 					Datastream ds=(Datastream) datastreams.get(i);
 					// ADMID processing...
 					// get list of ADMIDs that go with a datastream version
 					List admIdList=(List)m_dsADMIDs.get(ds.DSVersionID);
 					List cleanAdmIdList = new ArrayList();
 					if (admIdList!=null) {
 						Iterator admIdIter=admIdList.iterator();
 						while (admIdIter.hasNext()) {
 						   String admId=(String) admIdIter.next();
 						   // Detect ADMIDs that reference audit records 
 						   // vs. regular admin metadata. Drop audits from
 						   // the list. We know we have an audit if the ADMID
 						   // is not a regular datatream in the object.
 						   List matchedDatastreams=m_obj.datastreams(admId);
 						   if (matchedDatastreams.size()<=0) {                      
 								// Keep track of audit metadata correlated with the 
 								// datastream version it's about (for later use).
 								m_AuditIdToComponentId.put(admId, ds.DSVersionID);
 						   } else {
 								// Keep track of non-audit metadata in a new list.
 								cleanAdmIdList.add(admId);
 						   }
 						}
 					}
 					if (cleanAdmIdList.size()<=0){
 						// we keep track of admin metadata references
 						// for each datastream, but we exclude the audit
 						// records from this list.  If there are no
 						// non-audit metadata references, remove the
 						// datastream entry from the master hashmap.
 						m_dsADMIDs.remove(ds.DSVersionID);
 					} else {
 						// otherwise, update the master hashmap with the 
 						// clean list of non-audit metadata 
 						m_dsADMIDs.put(ds.DSVersionID, cleanAdmIdList);
 					}
 				}
 			}
 			// Now, put component ids on audit records.  Pre-Fedora 2.0
 			// datastream versions pointed to their audit records.
 			Iterator iter=((ArrayList) m_obj.getAuditRecords()).iterator();
 			while (iter.hasNext()) {
 				AuditRecord au=(AuditRecord) iter.next();
 				if (au.componentID == null || au.componentID.equals("")) {
 					// Before Fedora 2.0 audit records were associated with 
 					// datastream version ids.  From now on, the datastream id
 					// will be posted as the component id in the audit record,
 					// and associations to particular datastream versions can
 					// be derived via the datastream version dates and the audit
 					// record dates.
 					String dsVersId = (String)m_AuditIdToComponentId.get(au.id);
 					if (dsVersId!=null && !dsVersId.equals(""))
 					{
 						au.componentID=dsVersId.substring(0, dsVersId.indexOf("."));
 					}
 
 				}
 			}
 		}
 	}
 	/**
 	 * 	addRelsInt: Build an RDF relationship datastream to preserve
 	 *  DMDID and ADMID references in the digital object when METS
 	 *  is converted to FOXML (or other formats in the future). 
 	 *  If there is no pre-existing RELS-INT, look for DMDID and ADMID
 	 *  attributes to create new RELS-INT datastream.
 	 */
 	private void createRelsInt(){
 		
 		// create a new RELS-INT datastream only if one does not already exist.
 		List metsrels=m_obj.datastreams("RELS-INT");
 		if (metsrels.size()<=0) {
 			m_relsBuffer=new StringBuffer();
 			appendRDFStart(m_relsBuffer);   
 			Iterator dsIds=m_obj.datastreamIdIterator();
 			while (dsIds.hasNext()) {
 				// initialize hash sets to keep a list of
 				// unique DMDIDs or ADMIDs at the datatream id level.
 				HashSet<String> uniqueDMDIDs = new HashSet<String>();
 				HashSet uniqueADMIDs = new HashSet();
 				// get list of datastream *versions*
 				List dsVersionList=m_obj.datastreams((String) dsIds.next());
 				for (int i=0; i<dsVersionList.size(); i++) {
 					Datastream dsVersion=(Datastream) dsVersionList.get(i);
 					// DMDID processing...
 					List dmdIdList=(List) m_dsDMDIDs.get(dsVersion.DSVersionID);
 					if (dmdIdList!=null) {
 						hasRels=true;
 						Iterator dmdIdIter=dmdIdList.iterator();
 						while (dmdIdIter.hasNext()) {
 						   String dmdId=(String) dmdIdIter.next();
 						   // APPEND TO RDF: record the DMDID relationship.
 						   // Relationships will now be recorded at the 
 						   // datastream level, not the datastream version level.
 						   // So, is the relationship existed on more than one
 						   // datastream version, only write it once to the RDF. 
 						   if (!uniqueDMDIDs.contains(dmdId)){
 								appendRDFRel(m_relsBuffer, m_obj.getPid(), dsVersion.DatastreamID, 
 								 "hasDescMetadata", dmdId);
 						   }
 						   uniqueDMDIDs.add(dmdId);
 						}
 					}
 					// ADMID processing (already cleansed of audit refs)...
 					List cleanAdmIdList=(List) m_dsADMIDs.get(dsVersion.DSVersionID);
 					if (cleanAdmIdList!=null) {
 						hasRels=true;
 						Iterator admIdIter=cleanAdmIdList.iterator();
 						while (admIdIter.hasNext()) {
 							String admId=(String) admIdIter.next();             
 							// APPEND TO RDF: record the ADMID relationship.
 							// Relationships will now be recorded at the 
 							// datastream level, not the datastream version level.
 							// So, is the relationship existed on more than one
 							// datastream version, only write it once to the RDF. 
 							if (!uniqueADMIDs.contains(admId)){
 								appendRDFRel(m_relsBuffer, m_obj.getPid(), dsVersion.DatastreamID, 
 									"hasAdminMetadata", admId);
 							}
 							uniqueADMIDs.add(admId);
 						}
 					}
 				}
 			}
 			// APPEND RDF: finish up and add RDF as a system-generated datastream
 			if (hasRels) {
 				appendRDFEnd(m_relsBuffer);
 				setRDFAsDatastream(m_relsBuffer);
 			} else {
 				m_relsBuffer=null;
 			}
 		}
 	}
 	// Create a system-generated datastream from the RDF expression of the
 	// DMDID and ADMID relationships found in the METS file.
 	private void setRDFAsDatastream(StringBuffer buf) {
 		
 		DatastreamXMLMetadata ds = new DatastreamXMLMetadata();
 		// set the attrs common to all datastream versions
 		ds.DatastreamID="RELS-INT";
 		ds.DSVersionable=false;
 		ds.DSFormatURI=m_dsFormatURI;
 		ds.DatastreamAltIDs=m_dsAltIDs;
 		ds.DSVersionID="RELS-INT.0";
 		ds.DSLabel="DO NOT EDIT: System-generated datastream to preserve METS DMDID/ADMID relationships.";
 		ds.DSCreateDT=new Date();
 		ds.DSMIME="text/xml";
 		// set the attrs specific to datastream version
 		ds.DSControlGrp="X";
 		ds.DSState="A";
 		ds.DSLocation=m_obj.getPid() + "+" + ds.DatastreamID + "+" + ds.DSVersionID;
 		ds.DSLocationType="INTERNAL_ID";
 		ds.DSInfoType="DATA";
 		ds.DSMDClass=DatastreamXMLMetadata.TECHNICAL;
 		
 		// now set the xml content stream itself...
 		try {
 			ds.xmlContent=buf.toString().getBytes(m_characterEncoding);
 			ds.DSSize=ds.xmlContent.length;
 		} catch (UnsupportedEncodingException uee) {
             LOG.error("Encoding error when creating RELS-INT datastream", uee);
 		}				
 		// FINALLY! add the RDF and an inline xml datastream in the digital object
 		m_obj.datastreams(ds.DatastreamID).add(ds);  	
 	}
 
 	private StringBuffer appendRDFStart(StringBuffer buf) {
 
 		buf.append("<" + RDF_PREFIX + ":RDF"
 			+ " xmlns:"	+ RDF_PREFIX + "=\"" + RDF_NS + "\""
 			+ " xmlns:"	+ RDFS_PREFIX + "=\"" + RDFS_NS + "\""
 			+ " xmlns:"	+ RELS_PREFIX + "=\"" + RELS_NS + "\">\n");
 		return buf;
 	}
 
 	private StringBuffer appendRDFRel(StringBuffer buf, String pid, 
 		String subjectNodeId, String relType, String objectNodeId) {
 
 		// RDF subject node
 		buf.append("    <" + RDF_PREFIX + ":Description "
 			+ RDF_PREFIX + ":about=\"" + "info:fedora/" + pid + "/" + subjectNodeId + "\">\n");
 		// RDF relationship property and object node
 		buf.append("        <" + RELS_PREFIX + ":" + relType + " "
 			+ RDF_PREFIX + ":resource=\"" + "info:fedora/" + pid + "/" + objectNodeId + "\"/>\n");
 		buf.append("    </" + RDF_PREFIX + ":Description" + ">\n");
 		return buf;
 	}
 	
 	private StringBuffer appendRDFEnd(StringBuffer buf) {
 
 		buf.append("</" + RDF_PREFIX + ":RDF>\n");
 		return buf;
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
 
 	private void initialize(){		
 		//NOTE: variables that are commented out exist in FOXML but not METS
 		
 		// temporary variables and state variables
 		m_rootElementFound=false;
 		//m_objPropertyName="";
 		//m_readingBinaryContent=false; // future
 		m_inXMLMetadata=false;
         m_prefixMap = new HashMap<String, String>();
         m_localPrefixMap = new HashMap();
         m_prefixList = new ArrayList();
 
 		// temporary variables for processing datastreams		
 		m_dsId="";
 		//m_dsURI="";  // FOXML only.
 		m_dsVersionable=true; // FOXML only.
 		m_dsVersId="";
 		m_dsCreateDate=null;
 		m_dsState="";
 		m_dsFormatURI="";
 		m_dsAltIDs=new String[0];
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
 		m_dsADMIDs=new HashMap();
 		m_dsDMDIDs=new HashMap();
 		m_dsChecksum="";
 		m_dsChecksumType="";
 		
 		// temporary variables for processing disseminators
 //		m_diss=null;
 		m_dissems=new HashMap();
 	
 		// temporary variables for processing audit records
 		m_auditBuffer=null;
 		m_auditComponentID="";
 		m_auditProcessType="";
 		m_auditAction="";
 		m_auditResponsibility="";
 		m_auditDate="";
 		m_auditJustification="";
 		
 		m_AuditIdToComponentId=new HashMap();
 		m_relsBuffer=null;
 	}
 }
