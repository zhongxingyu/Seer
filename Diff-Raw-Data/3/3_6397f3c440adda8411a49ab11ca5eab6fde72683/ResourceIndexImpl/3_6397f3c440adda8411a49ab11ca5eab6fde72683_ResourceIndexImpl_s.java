 package fedora.server.resourceIndex;
 
 import fedora.client.bmech.data.MethodParm;
 import fedora.common.PID;
 import fedora.server.Logging;
 import fedora.server.StdoutLogging;
 import fedora.server.errors.ResourceIndexException;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.service.ServiceMapper;
 import fedora.server.storage.types.*;
 import fedora.server.utilities.DCFields;
 
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.ExtensibilityElement;
 import javax.wsdl.extensions.mime.MIMEContent;
 import javax.wsdl.factory.*;
 import javax.wsdl.xml.*;
 import javax.wsdl.*;
 import javax.xml.namespace.QName;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.jrdf.graph.ObjectNode;
 import org.jrdf.graph.PredicateNode;
 import org.jrdf.graph.SubjectNode;
 import org.jrdf.graph.Triple;
 
 import org.trippi.FlushErrorHandler;
 import org.trippi.RDFFormat;
 import org.trippi.TripleMaker;
 import org.trippi.TripleIterator;
 import org.trippi.TriplestoreConnector;
 import org.trippi.TriplestoreReader;
 import org.trippi.TriplestoreWriter;
 import org.trippi.TrippiException;
 import org.trippi.TupleIterator;
 
 import org.xml.sax.InputSource;
 
 /**
  * Implementation of the ResourceIndex interface.
  *
  * @author Edwin Shin
  */
 public class ResourceIndexImpl extends StdoutLogging implements ResourceIndex {
     // TODO
     //		- latent indexing: "tagging" objects with current index level to 
     //        support querying what needs indexing later
     //		- subsequent insert/edits/deletes
     //		- changes in levels
     //      - clean up/separate out sql/database calls
 	
 	private static final Logger logger =
         Logger.getLogger(ResourceIndex.class.getName());
     
     private int m_indexLevel;
     
     // For the database
     private ConnectionPool m_cPool;
     
     // Triplestore (Trippi)
     private TriplestoreConnector m_connector;
     private TriplestoreReader m_reader;
     private TriplestoreWriter m_writer;
     
     // RDF Prefix and Namespaces
     private Map namespaces; 
     
     public ResourceIndexImpl(int indexLevel, 
                              TriplestoreConnector connector, 
                              ConnectionPool cPool, 
                              Map aliasMap,
                              Logging target) throws ResourceIndexException {
         super(target);  
         m_indexLevel = indexLevel;
         namespaces = aliasMap;
         if (namespaces == null) namespaces = new HashMap();
         
         namespaces.put("fedora", FEDORA.uri);
         namespaces.put("dc", DC.uri);
         namespaces.put("fedora-model", MODEL.uri);
         namespaces.put("fedora-rels-ext", RELS_EXT.uri);
         namespaces.put("fedora-view", VIEW.uri);
         namespaces.put("rdf", RDF.uri);
         namespaces.put("tucana", TUCANA.uri);
         namespaces.put("xml-schema", XSD.uri);
         m_connector = connector;
         m_reader = m_connector.getReader();
         m_writer = m_connector.getWriter();
         m_cPool = cPool;
         try {
             m_reader.setAliasMap(namespaces);
             m_writer.setAliasMap(namespaces);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see fedora.server.resourceIndex.ResourceIndex#getIndexLevel()
      * 
      */
     public int getIndexLevel() {
         return m_indexLevel;
     }
 
 	/* (non-Javadoc)
 	 * @see fedora.server.resourceIndex.ResourceIndex#addDigitalObject(fedora.server.storage.types.DigitalObject)
 	 */
 	public void addDigitalObject(DigitalObject digitalObject) throws ResourceIndexException {
 	    if (m_indexLevel == INDEX_LEVEL_OFF) {
 	        return;
         }
         
         // List (queue) of statements to add (poor man's transaction impl)
         RIQueue tripleQ = new RIQueue();
         
         String pid = digitalObject.getPid();
         logger.debug("+ adding " + pid);
 		String doIdentifier = getDOURI(digitalObject);
 		
 		// Insert basic system metadata
         tripleQ.queueCreatedDate(doIdentifier, digitalObject.getCreateDate());
         tripleQ.queueLabel(doIdentifier, digitalObject.getLabel());
         tripleQ.queueLastModifiedDate(doIdentifier, digitalObject.getLastModDate());
 		
 		if (digitalObject.getOwnerId() != null) {
 		    tripleQ.queueOwner(doIdentifier, digitalObject.getOwnerId());
 		}
 		tripleQ.queueContentModel(doIdentifier, digitalObject.getContentModelId());
 		tripleQ.queueState(doIdentifier, digitalObject.getState());
 		
         // Insert ExtProperties
         Map extProps = digitalObject.getExtProperties();
         Iterator epIt = extProps.keySet().iterator();
         String epKey;
         while (epIt.hasNext()) {
             epKey = (String)epIt.next();
             tripleQ.queueExternalProperty(doIdentifier, epKey, (String)extProps.get(epKey));
         }
 
 		// handle type specific duties
 		int fedoraObjectType = digitalObject.getFedoraObjectType();
 		switch (fedoraObjectType) {
 			case DigitalObject.FEDORA_BDEF_OBJECT: 
 				addBDef(tripleQ, digitalObject);
 				break;
 			case DigitalObject.FEDORA_BMECH_OBJECT: 
 				addBMech(tripleQ, digitalObject);
 				break;
 			case DigitalObject.FEDORA_OBJECT: 
 				addDataObject(tripleQ, digitalObject);
 				break;
 			default: throw new ResourceIndexException("Unknown DigitalObject type: " + fedoraObjectType);	
 		}
 		
 		// Add datastreams
 		Iterator it;
 	    it = digitalObject.datastreamIdIterator();
 		while (it.hasNext()) {
 		    addDatastream(tripleQ, digitalObject, (String)it.next());
 		}
 		
 		// Add disseminators
 		it = digitalObject.disseminatorIdIterator();
 		while (it.hasNext()) {
 		    addDisseminator(tripleQ, digitalObject, (String)it.next());		    
 		}
         
 		// No errors, so add the triples
         addQueue(tripleQ, false);
         logger.debug("+ added " + pid);
 	}
 	
     /**
      * Note: by design, we do not represent datastreams-as-datastreams in the 
      * Resource Index. We only represent datastreams as disseminations.
      * Similarly, we don't represent disseminators per se in the Resource Index.
      * 
      * Thus, datastream attributes such as createDate and alternate IDs are not
      * reflected in the Resource Index.
      * @param tripleQ
      * @param digitalObject
      * @param datastreamID
      * @throws ResourceIndexException
      */
 	private void addDatastream(RIQueue tripleQ, DigitalObject digitalObject, String datastreamID) throws ResourceIndexException {
 	    if (m_indexLevel == INDEX_LEVEL_OFF) {
             return;
         }
         
         Datastream ds = getLatestDatastream(digitalObject.datastreams(datastreamID));
 	    String doURI = getDOURI(digitalObject);
 		String datastreamURI = getDSURI(doURI, datastreamID);
 		
         // Managed and Inline XML datastreams are never volatile
         // Future dependency analysis may be able to discern if kinds of E
         // and R datastreams are also not volatile
         boolean isVolatile = !(ds.DSControlGrp.equals("M") || ds.DSControlGrp.equals("X"));
         tripleQ.queueIsVolatile(datastreamURI, isVolatile);
         
         tripleQ.queueHasDatastream(doURI, datastreamURI);
         tripleQ.queueDissemination(doURI, datastreamURI);
         tripleQ.queueDisseminationType(datastreamURI, getDisseminationType(datastreamID));
         tripleQ.queueLastModifiedDate(datastreamURI, ds.DSCreateDT);
         tripleQ.queueMimeType(datastreamURI, ds.DSMIME);
         tripleQ.queueState(datastreamURI, ds.DSState);
         
 		// handle special system datastreams: DC, METHODMAP, RELS-EXT
 		if (datastreamID.equalsIgnoreCase("DC")) {
 			addDublinCoreDatastream(tripleQ, digitalObject, ds);
         } else if (datastreamID.equalsIgnoreCase("DSINPUTSPEC")) {
             // do nothing
         } else if (datastreamID.equalsIgnoreCase("METHODMAP")) { 
             addMethodMapDatastream(digitalObject, ds);
         } else if (datastreamID.equalsIgnoreCase("RELS-EXT")) {
             addRelsDatastream(tripleQ, ds);
         } else if (datastreamID.equalsIgnoreCase("SERVICE-PROFILE")) { 
             // do nothing
 		} else if (datastreamID.equalsIgnoreCase("WSDL")) { 
 		    addWSDLDatastream(digitalObject, ds);
 		}
 	}
 
 	private void addDisseminator(RIQueue tripleQ, DigitalObject digitalObject, String disseminatorID) throws ResourceIndexException {
 	    if (m_indexLevel == INDEX_LEVEL_OFF) {
             return;
         }
         
         Disseminator diss = getLatestDisseminator(digitalObject.disseminators(disseminatorID));
 	    String doIdentifier = getDOURI(digitalObject);
         String bMechPID = diss.bMechID;
         
         tripleQ.queueUsesBMech(doIdentifier, getDOURI(bMechPID));
 	    String bDefPID = diss.bDefID;
         
         // Map of datastream binding keys to datastream URIs
         Map dsMap = new HashMap();
         DSBinding[] dsBindings = diss.dsBindMap.dsBindings;
         for (int i = 0; i < dsBindings.length; i++) {
             dsMap.put(dsBindings[i].bindKeyName, getDSURI(doIdentifier, dsBindings[i].datastreamID));
         }
         
 	    // Query for disseminations
 	    if (digitalObject.getFedoraObjectType() == DigitalObject.FEDORA_OBJECT) {
             String query = "SELECT riMethodPermutation.methodId, riMethodPermutation.permutation, riMethodMimeType.mimeType, riMethodImplBinding.dsBindKey " +
                            "FROM riMethodPermutation, riMethodMimeType, riMethodImpl, riMethodImplBinding " +
                            "WHERE riMethodPermutation.methodId = riMethodImpl.methodId " +
                            "AND riMethodImpl.methodImplId = riMethodMimeType.methodImplId " +
                            "AND riMethodImplBinding.methodImplId = riMethodImpl.methodImplId " +
                            "AND riMethodImpl.bMechPid = '" + bMechPID + "'";
             Connection conn = null;
             Statement select = null;
             ResultSet rs = null;
             
             Set methods = new HashSet();
             Set permutations = new HashSet();
             try {
             	conn = m_cPool.getConnection();
             	select = conn.createStatement();
             	rs = select.executeQuery(query);
             	String permutation, mimeType, method, rep, repType, dsBindKey, dsURI;
             	while (rs.next()) {
             		method = doIdentifier + "/" + rs.getString("methodId");
             		if (methods.add(method)) {
             			logger.debug("adding triples for " + method);
             			tripleQ.queueDissemination(doIdentifier, method);
             			mimeType = rs.getString("mimeType");
             			tripleQ.queueMimeType(method, mimeType);
             			tripleQ.queueLastModifiedDate(method, diss.dissCreateDT);
             			tripleQ.queueState(method, diss.dissState);
             		}
             		permutation = rs.getString("permutation");
             		if (permutations.add(permutation)) {
             			logger.debug("adding permutation: " + permutation);
             			tripleQ.queueDisseminationType(method, getDisseminationType(bDefPID, permutation));
             		}
             		
             		// Dependencies
             		dsBindKey = rs.getString("dsBindKey");
             		dsURI = (String)dsMap.get(dsBindKey);
             		tripleQ.queueDependsOn(method, dsURI);
             		tripleQ.updateLastModified(method, dsURI);
             		tripleQ.updateIsVolatile(method, dsURI);
             	}
             } catch (SQLException e) {
             	throw new ResourceIndexException(e.getMessage(), e);
             } finally {
             	try {
             		if (rs != null) {
             			rs.close();
             		}
             		if (select != null) {
             			select.close();
             		}
             		if (conn != null) {
             			m_cPool.free(conn);
             		}
             	} catch(SQLException e2) {
             		throw new ResourceIndexException(e2.getMessage(), e2);
             	} finally {
             		rs = null;
             		select = null;
             	}
             }
 	    }
     }
 
 	/* (non-Javadoc)
 	 * @see fedora.server.resourceIndex.ResourceIndex#modifyDigitalObject(fedora.server.storage.types.DigitalObject)
 	 */
 	public void modifyDigitalObject(DigitalObject digitalObject) throws ResourceIndexException {
         if (m_indexLevel == INDEX_LEVEL_OFF) {
             return;
         }
 		deleteDigitalObject(digitalObject);
         addDigitalObject(digitalObject);
 	}
 
 	/* (non-Javadoc)
 	 * @see fedora.server.resourceIndex.ResourceIndex#deleteDigitalObject(java.lang.String)
 	 */
 	public void deleteDigitalObject(DigitalObject digitalObject) throws ResourceIndexException {
         if (m_indexLevel == INDEX_LEVEL_OFF) {
             return;
         }
         
         // flush buffer before deletes, or findTriples may be incomplete
         commit();
         
         String pid = digitalObject.getPid();
         logger.debug("- deleting " + pid);
         Iterator it;
         
         // Delete disseminators
         it = digitalObject.disseminatorIdIterator();
         while (it.hasNext()) {
             deleteDisseminator(digitalObject, (String)it.next());          
         }
         
         // Delete datastreams
         it = digitalObject.datastreamIdIterator();
         while (it.hasNext()) {
             deleteDatastream(digitalObject, (String)it.next());
         }
         
         // Delete all statements where doURI is the subject
         String doURI = getDOURI(digitalObject);
         try {
         	deleteTriples(findTriples(TripleMaker.createResource(doURI), null, null, 0), false);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
         logger.debug("- deleted " + pid);
 	}
 
 	private void deleteDatastream(DigitalObject digitalObject, String datastreamID) throws ResourceIndexException {
 	    if (m_indexLevel == INDEX_LEVEL_OFF) {
             return;
         }
         
         Datastream ds = getLatestDatastream(digitalObject.datastreams(datastreamID));
         String doURI = getDOURI(digitalObject);
         String datastreamURI = getDSURI(doURI, datastreamID);
 
         // DELETE statements where datastreamURI is subject
         try {
             deleteTriples(findTriples(TripleMaker.createResource(datastreamURI), null, null, 0), false);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
         
         // handle special system datastreams, e.g.: DC, METHODMAP, RELS-EXT
         if (datastreamID.equalsIgnoreCase("DC")) {
             deleteDublinCoreDatastream(digitalObject, ds);
         } else if (datastreamID.equalsIgnoreCase("DSINPUTSPEC")) {
             // do nothing
         } else if (datastreamID.equalsIgnoreCase("METHODMAP")) { 
             deleteMethodMapDatastream(digitalObject, ds);
         } else if (datastreamID.equalsIgnoreCase("RELS-EXT")) {
             deleteRelsDatastream(ds);
         } else if (datastreamID.equalsIgnoreCase("SERVICE-PROFILE")) { 
             deleteServiceProfileDatastream(ds);
         } else if (datastreamID.equalsIgnoreCase("WSDL")) { 
             deleteWSDLDatastream(digitalObject, ds);
         }
 	}
 
 	private void deleteDisseminator(DigitalObject digitalObject, String disseminatorID) throws ResourceIndexException {
         if (m_indexLevel == INDEX_LEVEL_OFF) {
             return;
         }
         
         Disseminator diss = getLatestDisseminator(digitalObject.disseminators(disseminatorID));
         String doIdentifier = getDOURI(digitalObject);
         
         String bDefPID = diss.bDefID;
         String bMechPID = diss.bMechID;
         
         // delete bMech reference: 
         try {
             deleteTriples(findTriples(TripleMaker.createResource(doIdentifier), 
                                                  MODEL.USES_BMECH, 
                                                  TripleMaker.createResource(getDOURI(bMechPID)), 
                                                  0), 
                           false);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
         
         // delete statements where rep is the subject
         String query = "SELECT permutation " +
                        "FROM riMethodPermutation, riMethodImpl " +
                        "WHERE riMethodPermutation.methodId = riMethodImpl.methodId " +
                        "AND riMethodImpl.bMechPid = '" + bMechPID + "'";
         Connection conn = null;
         Statement select = null;
         Statement delete = null;
         ResultSet rs = null;
         
         try {
             conn = m_cPool.getConnection();
             select = conn.createStatement();
             rs = select.executeQuery(query);
             String permutation, rep;
             while (rs.next()) {
                 permutation = rs.getString("permutation");
                 rep = doIdentifier + "/" + bDefPID + "/" + permutation;
                 deleteTriples(findTriples(TripleMaker.createResource(rep), null, null, 0), false);
             }
             delete = conn.createStatement();
             if (digitalObject.getFedoraObjectType() == DigitalObject.FEDORA_BMECH_OBJECT) {
                 String deleteRIMIB = "DELETE FROM riMethodImplBinding " +
                                      "WHERE methodImplId LIKE '" + bMechPID + "/%'";
                 delete.execute(deleteRIMIB);
             }
         } catch (SQLException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (select != null) {
                     select.close();
                 }
                 if (conn != null) {
                     m_cPool.free(conn);
                 }
             } catch(SQLException e2) {
                 throw new ResourceIndexException(e2.getMessage(), e2);
             } finally {
                 rs = null;
                 select = null;
             }
         }
 	}
     
 	private void deleteTriples(TripleIterator ti, boolean flush) throws ResourceIndexException {
 		File tempFile = null;
 		try {
 			if (logger.isDebugEnabled()) {
 				tempFile = File.createTempFile("trippi-deltriples", "txt");
                 FileOutputStream fout = new FileOutputStream(tempFile);
                 ti.toStream(fout, RDFFormat.TURTLE);
                 fout.close();
                 ti.close();
                 ti = TripleIterator.fromStream(new FileInputStream(tempFile), RDFFormat.TURTLE);
 				OutputStream os = new ByteArrayOutputStream();
                 ti.toStream(os, RDFFormat.TURTLE);
                 logger.debug("delete:\n" + os.toString());
 				/*
                 while(ti.hasNext()) {
 					org.jrdf.graph.Triple t = ti.next();
 					logger.debug(t.getSubject().toString() + " " + 
 							     t.getPredicate().toString() + " " + 
 								 t.getObject().toString());
 				}
 				*/
 				ti.close();
 				ti = TripleIterator.fromStream(new FileInputStream(tempFile), RDFFormat.TURTLE);
 			}
 			m_writer.delete(ti, flush);
 		} catch (IOException e) {
 			throw new ResourceIndexException(e.getMessage(), e);
 		} catch (TrippiException e) {
 			throw new ResourceIndexException(e.getMessage(), e);
 		} finally {
             if (tempFile != null) tempFile.delete();
         }
 	}
 	
     public void commit() throws ResourceIndexException {
         try {
             flushBuffer();
         } catch (IOException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
     }
     
     /**
      * 
      * @param out
      * @param format
      * @throws ResourceIndexException
      */
     public void export(OutputStream out, RDFFormat format) throws ResourceIndexException {
         commit();
         try {
             TripleIterator it = findTriples(null, null, null, 0);
             it.setAliasMap(namespaces);
             it.toStream(out, format);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
     }
 
 	private void addBDef(RIQueue tripleQ, DigitalObject bDef) throws ResourceIndexException {
 		String doURI = getDOURI(bDef);
         tripleQ.queueRDFType(doURI, MODEL.BDEF_OBJECT.uri);
 		
 		Datastream ds = getLatestDatastream(bDef.datastreams("METHODMAP"));
 		MethodDef[] mdef = getMethodDefs(bDef.getPid(), ds);
 		for (int i = 0; i < mdef.length; i++) {
             tripleQ.queueDefinesMethod(doURI, mdef[i].methodName);
 	    }
 	}
     
 	private void addBMech(RIQueue tripleQ, DigitalObject bMech) throws ResourceIndexException {
 		String doURI = getDOURI(bMech);
         tripleQ.queueRDFType(doURI, MODEL.BMECH_OBJECT.uri);
 	
 		String bDefPid = getBDefPid(bMech);
 		tripleQ.queueImplements(doURI, getDOURI(bDefPid));	
 	}
 	
 	private void addDataObject(RIQueue tripleQ, DigitalObject digitalObject) throws ResourceIndexException {
 		String identifier = getDOURI(digitalObject);
         tripleQ.queueRDFType(identifier, MODEL.DATA_OBJECT.uri);	
 	}
 	
 	private void addDublinCoreDatastream(RIQueue tripleQ, DigitalObject digitalObject, Datastream ds) throws ResourceIndexException {
 	    String doURI = getDOURI(digitalObject);
 	    DatastreamXMLMetadata dc = (DatastreamXMLMetadata)ds;
 		DCFields dcf;
         
 		try {
 			dcf = new DCFields(dc.getContentStream());
 		} catch (Throwable t) {
 			throw new ResourceIndexException(t.getMessage());
 		}
 		Iterator it;
 		it = dcf.titles().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.TITLE.uri, (String)it.next());
 		}
 		it = dcf.creators().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.CREATOR.uri, (String)it.next());
 		}
 		it = dcf.subjects().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.SUBJECT.uri, (String)it.next());
 		}
 		it = dcf.descriptions().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.DESCRIPTION.uri, (String)it.next());
 		}
 		it = dcf.publishers().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.PUBLISHER.uri, (String)it.next());
 		}
 		it = dcf.contributors().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.CONTRIBUTOR.uri, (String)it.next());
 		}
 		it = dcf.dates().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.DATE.uri, (String)it.next());
 		}
 		it = dcf.types().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.TYPE.uri, (String)it.next());
 		}
 		it = dcf.formats().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.FORMAT.uri, (String)it.next());
 		}
 		it = dcf.identifiers().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.IDENTIFIER.uri, (String)it.next());
 		}
 		it = dcf.sources().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.SOURCE.uri, (String)it.next());
 		}
 		it = dcf.languages().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.LANGUAGE.uri, (String)it.next());
 		}
 		it = dcf.relations().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.RELATION.uri, (String)it.next());
 		}
 		it = dcf.coverages().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.COVERAGE.uri, (String)it.next());
 		}
 		it = dcf.rights().iterator();
 		while (it.hasNext()) {
             tripleQ.queueDC(doURI, DC.RIGHTS.uri, (String)it.next());
 		}
 	}
 	
     /**
      * MethodMap datastream is only required for identifying the various 
      * combinations (permutations) of method names, their parameters and values.
      * However, the MethodMap datastream is only available in the bDef that 
      * defines the methods or the bMech(s) that implement the bDef, but this 
      * information is needed at the ingest of the dataobjects.
      * So, these method permutations are stored in a relational database
      * for performance reasons.
      * 
      * Note that we only track unparameterized disseminations and disseminations
      * with fixed parameters
      * 
      * @param digitalObject
      * @param ds
      * @throws ResourceIndexException
      */
 	private void addMethodMapDatastream(DigitalObject digitalObject, Datastream ds) throws ResourceIndexException {
         if (digitalObject.getFedoraObjectType() == DigitalObject.FEDORA_BDEF_OBJECT) {
             addBDefMethodMapDatastream(digitalObject, ds);
         } else if (digitalObject.getFedoraObjectType() == DigitalObject.FEDORA_BMECH_OBJECT) {
             addBMechMethodMapDatastream(digitalObject, ds);
 	    } else {
 	        return;   
         }
         
 	}
     
     /**
      * MethodMap datastream is only required for identifying the various 
      * combinations (permutations) of method names, their parameters and values.
      * However, the MethodMap datastream is only available in the bDef that 
      * defines the methods or the bMech(s) that implement the bDef, but this 
      * information is needed at the ingest of the dataobjects.
      * So, these method permutations are stored in a relational database
      * for performance reasons.
      * 
      * Note that we only track unparameterized disseminations and disseminations
      * with fixed parameters
      * 
      * @param digitalObject
      * @param ds
      * @throws ResourceIndexException
      */
     private void addBDefMethodMapDatastream(DigitalObject digitalObject, Datastream ds) throws ResourceIndexException {
         if (digitalObject.getFedoraObjectType() != DigitalObject.FEDORA_BDEF_OBJECT) {
             return;
         }
         
         String doURI = getDOURI(digitalObject);
         String bDefPid = digitalObject.getPid();
         MethodDef[] mdef = getMethodDefs(bDefPid, ds);
         List permutations = new ArrayList();
         
         String methodName;
         boolean noRequiredParms;
         int optionalParms;
         Connection conn = null;
         PreparedStatement insertMethod = null, insertPermutation = null;
         try {
             conn = m_cPool.getConnection();
             insertMethod = conn.prepareStatement("INSERT INTO riMethod (methodId, bDefPid, methodName) VALUES (?, ?, ?)");
             insertPermutation = conn.prepareStatement("INSERT INTO riMethodPermutation (methodId, permutation) VALUES (?, ?)");
 
             for (int i = 0; i < mdef.length; i++) {
                 methodName = mdef[i].methodName;
                 MethodParmDef[] mparms = mdef[i].methodParms;
                 if (m_indexLevel % INDEX_LEVEL_PERMUTATIONS != 0 || mparms.length == 0) { // no method parameters
                     permutations.add(methodName);
                 } else {
                     noRequiredParms = true;
                     optionalParms = 0;
                     List parms = new ArrayList();
                     for (int j = 0; j < mparms.length; j++) {
                         if (noRequiredParms && mparms[j].parmRequired) {
                             noRequiredParms = false;
                         }
                         if (!mparms[j].parmRequired) {
                             optionalParms++;
                         }
                     }
                     if (noRequiredParms) {
                         permutations.add(methodName);
                     } else {
                         // add methods with their required, fixed parameters
                         parms.addAll(getMethodParameterCombinations(mparms, true));
                     }
                     if (optionalParms > 0) {
                         parms.addAll(getMethodParameterCombinations(mparms, false));
                     }
                     Iterator it = parms.iterator();
                     while (it.hasNext()) {
                         permutations.add(methodName + "?" + it.next());
                     }
                 }
                 
                 // build the batch of sql statements to execute
                 String riMethodPK = getRIMethodPrimaryKey(bDefPid, methodName);
                 insertMethod.setString(1, riMethodPK);
                 insertMethod.setString(2, bDefPid);
                 insertMethod.setString(3, methodName);
                 insertMethod.addBatch();
 
                 Iterator it = permutations.iterator();
                 while (it.hasNext()) {
                     insertPermutation.setString(1, riMethodPK);
                     insertPermutation.setString(2, (String)it.next());
                     insertPermutation.addBatch();
                 }
                 permutations.clear();
             }
 
             insertMethod.executeBatch();
             insertPermutation.executeBatch();
         } catch (SQLException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } finally {
             try {
                 if (insertMethod != null) {
                     insertMethod.close();
                 }
                 if (insertPermutation != null) {
                     insertPermutation.close();
                 }
                 if (conn != null) {
                     m_cPool.free(conn);
                 }
             } catch(SQLException e2) {
                 throw new ResourceIndexException(e2.getMessage(), e2);
             } finally {
                 insertMethod = null;
                 insertPermutation = null;
             }
         }
     }
     
     /**
      * It's in a bmech's methodmap that we see the DatastreamInputParm.parmName
      * which we need to match when we parse a dataobject's disseminator's 
      * dsBinding.bindKeyName.
      * 
      * We maintain this mapping of methodImplIds to datastream binding keys in 
      * a database table, e.g.
      * demo:DualResImageCollection/view => XSLT
      * demo:DualResImageCollection/view => LIST
      * 
      * @param digitalObject
      * @param ds
      * @throws ResourceIndexException
      */
     private void addBMechMethodMapDatastream(DigitalObject digitalObject, Datastream ds) throws ResourceIndexException {
         if (digitalObject.getFedoraObjectType() != DigitalObject.FEDORA_BMECH_OBJECT) {
             return;
         }
         
         String doURI = getDOURI(digitalObject);
         String bMechPid = digitalObject.getPid();
         MethodDef[] mdef = getMethodDefs(bMechPid, ds);
         String methodName, methodImplFK;
         
         Connection conn = null;
         PreparedStatement insertMethodImplBinding = null;
 
         try {
             conn = m_cPool.getConnection();
             insertMethodImplBinding = conn.prepareStatement("INSERT INTO riMethodImplBinding (methodImplId, dsBindKey) VALUES (?, ?)");
 
             for (int i = 0; i < mdef.length; i++) {
                 methodName = mdef[i].methodName;
                 methodImplFK = getRIMethodImplPrimaryKey(bMechPid, methodName);
                 MethodParmDef[] mparms = mdef[i].methodParms;
                 for (int j = 0; j < mparms.length; j++) {
                     if (mparms[j].parmType.equals(MethodParm.DATASTREAM_INPUT)) {
                         insertMethodImplBinding.setString(1, methodImplFK);
                         insertMethodImplBinding.setString(2, mparms[j].parmName);
                         insertMethodImplBinding.addBatch();
                     }
                 }
             }
             insertMethodImplBinding.executeBatch();
         } catch (SQLException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } finally {
             try {
                 if (insertMethodImplBinding != null) {
                     insertMethodImplBinding.close();
                 }
                 if (conn != null) {
                     m_cPool.free(conn);
                 }
             } catch(SQLException e2) {
                 throw new ResourceIndexException(e2.getMessage(), e2);
             } finally {
                 insertMethodImplBinding = null;
             }
         }
     }
 	
     private void addRelsDatastream(RIQueue tripleQ, Datastream ds) throws ResourceIndexException {
         DatastreamXMLMetadata rels = (DatastreamXMLMetadata)ds;
         try {
             TripleIterator it = TripleIterator.fromStream(rels.getContentStream(), 
                                                           RDFFormat.RDF_XML);
             while (it.hasNext()) {
                 tripleQ.addTriple(it.next());
             }
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
     }
     
     /**
      * The WSDL datastream is only parsed to obtain the mime types of
      * disseminations. As this is only available in bMech objects,
      * we cache this information in a relational database table for future
      * queries when data objects are ingested.
      * 
      * @param digitalObject
      * @param ds
      * @throws ResourceIndexException
      */
     private void addWSDLDatastream(DigitalObject digitalObject, Datastream ds) 
     throws ResourceIndexException {
         if (digitalObject.getFedoraObjectType() != DigitalObject.FEDORA_BMECH_OBJECT) {
             return;
         }
 
         String doURI = getDOURI(digitalObject);
         String bDefPid = getBDefPid(digitalObject);
         String bMechPid = digitalObject.getPid();
         DatastreamXMLMetadata wsdlDS = (DatastreamXMLMetadata)ds;
         Map bindings;
         Connection conn = null;
         PreparedStatement insertMethodImpl = null;
         PreparedStatement insertMethodMimeType = null;
         
         try {
             WSDLFactory wsdlFactory = WSDLFactory.newInstance();
             WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
             wsdlReader.setFeature("javax.wsdl.verbose",false);
             
             Definition definition = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(wsdlDS.xmlContent)));
             bindings = definition.getBindings();
             
             Set bindingKeys = bindings.keySet();
             Iterator it = bindingKeys.iterator();
             
             String methodName, mimeType;
             conn = m_cPool.getConnection();
             insertMethodImpl = conn.prepareStatement("INSERT INTO riMethodImpl (methodImplId, bMechPid, methodId) VALUES (?, ?, ?)");
             insertMethodMimeType = conn.prepareStatement("INSERT INTO riMethodMimeType (methodImplId, mimeType) VALUES (?, ?)");
             String riMethodImplPK, riMethodFK;
             QName mimeContentQName = new QName("http://schemas.xmlsoap.org/wsdl/mime/", "content");
             while (it.hasNext()) {
                 QName qname = (QName)it.next();
                 Binding binding = (Binding)bindings.get(qname);
 
                 List bops = binding.getBindingOperations();
                 Iterator bit = bops.iterator();
                 while (bit.hasNext()) {
                     BindingOperation bop = (BindingOperation)bit.next();
                     methodName = bop.getName();
                     riMethodFK = getRIMethodPrimaryKey(bDefPid, methodName);
                     riMethodImplPK = getRIMethodImplPrimaryKey(bMechPid, methodName);
                     insertMethodImpl.setString(1, riMethodImplPK);
                     insertMethodImpl.setString(2, bMechPid);
                     insertMethodImpl.setString(3, riMethodFK);
                     insertMethodImpl.addBatch();
                     BindingOutput bout = bop.getBindingOutput();
                     List extEls = bout.getExtensibilityElements();
                     Iterator eit = extEls.iterator();
                     while (eit.hasNext()) {
                         ExtensibilityElement extEl = (ExtensibilityElement)eit.next();
                         QName eType = extEl.getElementType();
                         if (eType.equals(mimeContentQName)) {
                             MIMEContent mc = (MIMEContent)extEl;
                             mimeType = mc.getType();
                             insertMethodMimeType.setString(1, riMethodImplPK);
                             insertMethodMimeType.setString(2, mimeType);
                             insertMethodMimeType.addBatch();
                         }
                     }
                 }
             }
             
             insertMethodImpl.executeBatch();
             insertMethodMimeType.executeBatch();
             
         } catch (WSDLException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } catch (SQLException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } finally {
             try {
                 if (insertMethodImpl != null) {
                     insertMethodImpl.close();
                 }
                 if (insertMethodMimeType != null) {
                     insertMethodMimeType.close();
                 }
                 if (conn != null) {
                     m_cPool.free(conn);
                 }
             } catch(SQLException e2) {
                 throw new ResourceIndexException(e2.getMessage(), e2);
             } finally {
                 insertMethodImpl = null;
                 insertMethodMimeType = null;
             }
         }
     }
     
     private void deleteDublinCoreDatastream(DigitalObject digitalObject, Datastream ds) {
         // So long as deletes are always initiated at the object level
         // (i.e., deleteDigitalObject(DigitalObject do), we don't actually
         // need to handle anything here.
     }
 
     private void deleteMethodMapDatastream(DigitalObject digitalObject, Datastream ds) throws ResourceIndexException {
         if (digitalObject.getFedoraObjectType() != DigitalObject.FEDORA_BDEF_OBJECT) {
             return;
         }
         
         String bDefPid = digitalObject.getPid();
         
         // Between earlier versions of MySQL lacking sub-select support,
         // other db's lacking MySQL's joined delete support,
         // McKoi's JDBC driver lacking updateable ResultSets,
         // and reluctant to issue a SELECT and then iteratively issue DELETEs,
         // I am going to count on an implementation detail of the methodId field:
         // namely that it is constructed bDefPid/methodName
         String deleteMP = "DELETE FROM riMethodPermutation " +
                           "WHERE methodId LIKE '" + bDefPid + "/%'";
         String deleteM = "DELETE FROM riMethod WHERE riMethod.bDefPid = '" + bDefPid + "'";
         
         Connection conn = null;
         Statement stmt = null;
         try {
             conn = m_cPool.getConnection();
             stmt = conn.createStatement();
             stmt.execute(deleteMP);
             stmt.execute(deleteM);
         } catch (SQLException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     m_cPool.free(conn);
                 }
             } catch(SQLException e2) {
                 throw new ResourceIndexException(e2.getMessage(), e2);
             } finally {
                 stmt = null;
             }
         }
     }
 
     private void deleteRelsDatastream(Datastream ds) throws ResourceIndexException {
         // So long as deletes are always initiated at the object level
         // (i.e., deleteDigitalObject(DigitalObject do), we don't actually
         // need to handle anything here.
         
 //        // NOTE this is not complete
 //        DatastreamXMLMetadata rels = (DatastreamXMLMetadata)ds;
 //        // Use the SAX2-compliant Xerces parser:
 //        System.setProperty(
 //                "org.xml.sax.driver",
 //                "org.apache.xerces.parsers.SAXParser");
 //        Parser parser = new RdfXmlParser();
 //        try {
 //            TripleIterator it = new RIOTripleIterator(rels.getContentStream(), parser, "http://www.example.org/");
 //            m_writer.delete(it, false);
 //        } catch (TrippiException e) {
 //            throw new ResourceIndexException(e.getMessage(), e);
 //        } catch (IOException e) {
 //            throw new ResourceIndexException(e.getMessage(), e);
 //        }
     }
 
     private void deleteServiceProfileDatastream(Datastream ds) {
         // placeholder
     }
 
     private void deleteWSDLDatastream(DigitalObject digitalObject, Datastream ds) throws ResourceIndexException {
         if (digitalObject.getFedoraObjectType() != DigitalObject.FEDORA_BMECH_OBJECT) {
             return;
         }
         
         String bMechPid = digitalObject.getPid();
         
         String deleteMMT = "DELETE FROM riMethodMimeType " +
                            "WHERE methodImplId LIKE '" + bMechPid + "/%'";
         String deleteMI = "DELETE FROM riMethodImpl " +
                           "WHERE riMethodImpl.bMechPid = '" + bMechPid + "'";
         
         Connection conn = null;
         Statement stmt = null;
         try {
             conn = m_cPool.getConnection();
             stmt = conn.createStatement();
             stmt.execute(deleteMMT);
             stmt.execute(deleteMI);
         } catch (SQLException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     m_cPool.free(conn);
                 }
             } catch(SQLException e2) {
                 throw new ResourceIndexException(e2.getMessage(), e2);
             } finally {
                 stmt = null;
             }
         }
     }
 
     private String getBDefPid(DigitalObject bMech) throws ResourceIndexException {
         if (bMech.getFedoraObjectType() != DigitalObject.FEDORA_BMECH_OBJECT) {
             throw new ResourceIndexException("Illegal argument: object is not a bMech");
         }
         Datastream ds;
         ds = getLatestDatastream(bMech.datastreams("DSINPUTSPEC"));
         BMechDSBindSpec dsBindSpec = getDSBindSpec(bMech.getPid(), ds);
         return dsBindSpec.bDefPID;
     }
     
     private String getDisseminationType(String bDefPID, String permutation) {
         return FEDORA.uri + "*" + "/" + bDefPID + "/" + permutation;
     }
 
     private String getDisseminationType(String dsID) {
         return FEDORA.uri + "*" + "/" + dsID;
     }
 
     private BMechDSBindSpec getDSBindSpec(String pid, Datastream ds) throws ResourceIndexException {
         DatastreamXMLMetadata dsInSpecDS = (DatastreamXMLMetadata)ds;
         ServiceMapper serviceMapper = new ServiceMapper(pid);
         BMechDSBindSpec dsBindSpec;
         try {
             return serviceMapper.getDSInputSpec(new InputSource(new ByteArrayInputStream(dsInSpecDS.xmlContent)));
         } catch (Throwable t) {
             throw new ResourceIndexException(t.getMessage());
         }
     }
 
     private String getDOURI(DigitalObject digitalObject) {
 		return getDOURI(digitalObject.getPid());
     }
 
     private String getDOURI(String pid) {
         return PID.toURI(pid);
     }
     
     private String getDSURI(String doURI, String datastreamID) {
         return doURI + "/" + datastreamID;
     }
 
     private Datastream getLatestDatastream(List datastreams) throws ResourceIndexException {
         Iterator it = datastreams.iterator();
         long latestDSCreateDT = -1;
         Datastream ds, latestDS = null;
         while (it.hasNext()) {
             ds = (Datastream)it.next();
             if (ds.DSCreateDT == null) {
                 throw new ResourceIndexException("Datastream, " + ds.DSVersionID + ", is missing create date");
             } else if (ds.DSCreateDT.getTime() > latestDSCreateDT) {
                 latestDS = ds;
             }
         }
         return latestDS;
     }
 
     private Disseminator getLatestDisseminator(List disseminators) throws ResourceIndexException {
         Iterator it = disseminators.iterator();
         long latestDISSCreateDT = -1;
         Disseminator diss, latestDISS = null;
         while (it.hasNext()) {
             diss = (Disseminator)it.next();
             if (diss.dissCreateDT == null) {
                 throw new ResourceIndexException("Disseminator, " + diss.dissVersionID + ", is missing create date");
             } else if (diss.dissCreateDT.getTime() > latestDISSCreateDT) {
                 latestDISS = diss;
             }
         }
         return latestDISS;
     }
 	
 	private MethodDef[] getMethodDefs(String pid, Datastream ds) throws ResourceIndexException {
 	    DatastreamXMLMetadata mmapDS = (DatastreamXMLMetadata)ds;
 	    ServiceMapper serviceMapper = new ServiceMapper(pid);
 	    try {
 	        return serviceMapper.getMethodDefs(new InputSource(new ByteArrayInputStream(mmapDS.xmlContent)));
 	    } catch (Throwable t) {
 	        throw new ResourceIndexException(t.getMessage());
 	    }
 	}
 
     /**
      * Returns a List of Strings, representing the cross product of possible
      * method parameters and their values, e.g. 
      * ( "arg1=val1&arg2=val2", "foo=bar&baz=quux" )
      * 
      */
     private List getMethodParameterCombinations(MethodParmDef[] mparms, boolean isRequired) {
         List combinations = new ArrayList();
         
         Arrays.sort(mparms, new MethodParmDefParmNameComparator());
         List parms = new ArrayList();
         for (int j = 0; j < mparms.length; j++) {
             List parm = new ArrayList();
             for (int k = 0; k < mparms[j].parmDomainValues.length; k++) {
                 if (isRequired) {
                     if (mparms[j].parmRequired) {
                         parm.add(mparms[j].parmName + "=" + mparms[j].parmDomainValues[k]);
                     }
                 } else {
                     parm.add(mparms[j].parmName + "=" + mparms[j].parmDomainValues[k]);
                 }
             }
         	parms.add(parm);
         }
         
         CrossProduct cp = new CrossProduct(parms);
         List results = cp.getCrossProduct();
         Iterator it = results.iterator();
         while (it.hasNext()) {
             List cpParms = (List)it.next();
             Iterator it2 = cpParms.iterator();
             StringBuffer sb = new StringBuffer();
             while (it2.hasNext()) {
                 sb.append(it2.next());
                 if (it2.hasNext()) {
                     sb.append("&");
                 }
             }
             combinations.add(sb.toString());
         }
         return combinations;
     }
 
     /**
      * If we could rely on support for JDBC 3.0's Statement.getGeneratedKeys(),
      * we could use an auto-increment field for the primary key.
      * 
      * A composite primary key isn't used (at the moment) because
      * of varying support for composite keys.
      * A post to the McKoi mailing list suggests that while McKoi
      * allows composite primary keys, it's not indexing them as a 
      * composite, and requiring a (full?) table scan.
      * 
      * @return bDefPid + "/" + methodName, e.g. demo:8/getImage
      */
     private String getRIMethodPrimaryKey(String bDefPid, String methodName) {
         return (bDefPid + "/" + methodName);
     }
 
     private String getRIMethodImplPrimaryKey(String bMechPid, String methodName) {
         return (bMechPid + "/" + methodName);
     }
     
 
    
     private void addQueue(RIQueue queue, boolean flush) throws ResourceIndexException {
         try {
         	if (logger.isDebugEnabled()) {
         		TripleIterator ti = queue.getTripleIterator();
         		OutputStream os = new ByteArrayOutputStream();
                 ti.toStream(os, RDFFormat.TURTLE);
                 logger.debug("add: \n" + os.toString());
         	}
             m_writer.add(queue.listTriples(), flush);
         } catch (IOException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         } finally {
             queue.clear();
         }
     }
 	
 	/* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#setAliasMap(java.util.Map)
      */
     public void setAliasMap(Map aliasToPrefix) throws TrippiException {
         m_reader.setAliasMap(aliasToPrefix);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#getAliasMap()
      */
     public Map getAliasMap() throws TrippiException {
         return m_reader.getAliasMap();
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#findTuples(java.lang.String, java.lang.String, int, boolean)
      */
     public TupleIterator findTuples(String queryLang,
                                     String tupleQuery,
                                     int limit,
                                     boolean distinct) throws TrippiException {
     	logger.debug("findTuples() \n" +
 			     "  queryLang: " + queryLang + "\n" +
 			     "  tupleQuery: " + tupleQuery + "\n" +
 				 "  limit: " + limit + "\n" +
 				 "  distinct: " + distinct + "\n");
         return m_reader.findTuples(queryLang, tupleQuery, limit, distinct);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#countTuples(java.lang.String, java.lang.String, int, boolean)
      */
     public int countTuples(String queryLang,
                            String tupleQuery,
                            int limit,
                            boolean distinct) throws TrippiException {
         return m_reader.countTuples(queryLang, tupleQuery, limit, distinct);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#findTriples(java.lang.String, java.lang.String, int, boolean)
      */
     public TripleIterator findTriples(String queryLang,
                                       String tripleQuery,
                                       int limit,
                                       boolean distinct) throws TrippiException {
     	logger.debug("findTriples() \n" +
     			     "  queryLang: " + queryLang + "\n" +
     			     "  tripleQuery: " + tripleQuery + "\n" +
 					 "  limit: " + limit + "\n" +
 					 "  distinct: " + distinct + "\n");
         return m_reader.findTriples(queryLang, tripleQuery, limit, distinct);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#countTriples(java.lang.String, java.lang.String, int, boolean)
      */
     public int countTriples(String queryLang,
                             String tripleQuery,
                             int limit,
                             boolean distinct) throws TrippiException {
         return m_reader.countTriples(queryLang, tripleQuery, limit, distinct);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#findTriples(org.jrdf.graph.SubjectNode, org.jrdf.graph.PredicateNode, org.jrdf.graph.ObjectNode, int)
      */
     public TripleIterator findTriples(SubjectNode subject,
                                       PredicateNode predicate,
                                       ObjectNode object,
                                       int limit) throws TrippiException {
     	logger.debug("findTriples() \n" +
 			     "  subject: " + subject + "\n" +
 			     "  predicate: " + predicate + "\n" +
 				 "  object: " + object + "\n" +
 				 "  limit: " + limit + "\n");
         return m_reader.findTriples(subject, predicate, object, limit);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#countTriples(org.jrdf.graph.SubjectNode, org.jrdf.graph.PredicateNode, org.jrdf.graph.ObjectNode, int)
      */
     public int countTriples(SubjectNode subject,
                             PredicateNode predicate,
                             ObjectNode object,
                             int limit) throws TrippiException {
         return m_reader.countTriples(subject, predicate, object, limit);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#findTriples(java.lang.String, java.lang.String, java.lang.String, int, boolean)
      */
     public TripleIterator findTriples(String queryLang,
                                       String tripleQuery,
                                       String tripleTemplate,
                                       int limit,
                                       boolean distinct) throws TrippiException {
     	logger.debug("findTriples() \n" +
 			     "  queryLang: " + queryLang + "\n" +
 			     "  tripleQuery: " + tripleQuery + "\n" +
 			     "  tripleTemplate: " + tripleTemplate + "\n" +
 				 "  limit: " + limit + "\n" +
 				 "  distinct: " + distinct + "\n");
         return m_reader.findTriples(queryLang, tripleQuery, tripleTemplate, limit, distinct);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#countTriples(java.lang.String, java.lang.String, java.lang.String, int, boolean)
      */
     public int countTriples(String queryLang,
                             String tripleQuery,
                             String tripleTemplate,
                             int limit,
                             boolean distinct) throws TrippiException {
         return m_reader.countTriples(queryLang, tripleQuery, tripleTemplate, limit, distinct);
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#listTupleLanguages()
      */
     public String[] listTupleLanguages() {
         return m_reader.listTupleLanguages();
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#listTripleLanguages()
      */
     public String[] listTripleLanguages() {
         return m_reader.listTripleLanguages();
     }
 
     /* (non-Javadoc)
      * @see org.trippi.TriplestoreReader#close()
      */
     public void close() throws TrippiException {
         m_reader.close();
     }
     
     /**
      * Case insensitive sort by parameter name
      */
     protected class MethodParmDefParmNameComparator implements Comparator {
         public int compare(Object o1, Object o2) {
             MethodParmDef p1 = (MethodParmDef)o1;
             MethodParmDef p2 = (MethodParmDef)o2;
             return p1.parmName.toUpperCase().compareTo(p2.parmName.toUpperCase());
         }
     }
     
     protected class CrossProduct {
         public List crossProduct;
         public List lol;
         
         public CrossProduct(List listOfLists) {
         	this.crossProduct = new ArrayList();
         	this.lol = new ArrayList();
         	Iterator it = listOfLists.iterator();
         	while (it.hasNext()) {
         		List list = (List)it.next();
         		if (!list.isEmpty()) {
         			lol.add(list);
         		}
         	}
         }
         
         public List getCrossProduct() {
             generateCrossProduct(new ArrayList());
             return crossProduct;
         }
         
         private void generateCrossProduct(List productList) {
             if (productList.size() == lol.size()) {
                 addCopy(productList);
             } else {
                 int idx = productList.size();
                 List elementList = (List)lol.get(idx);
                 Iterator it = elementList.iterator();
                 if (it.hasNext()) {
                     productList.add(it.next());
                     generateCrossProduct(productList);
                     while (it.hasNext()) {
                         productList.set(idx, it.next());
                         generateCrossProduct(productList);
                     }
                     productList.remove(idx);
                 }
             }
         }
         
         private void addCopy(List result) {
             List copy = new ArrayList();
             copy.addAll(result);
             crossProduct.add(copy);
         }
     }
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#add(java.util.List, boolean)
 	 */
 	public void add(List arg0, boolean arg1) throws IOException, TrippiException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#add(org.trippi.TripleIterator, boolean)
 	 */
 	public void add(TripleIterator triples, boolean flush) throws IOException, TrippiException {
 		m_writer.add(triples, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#add(org.jrdf.graph.Triple, boolean)
 	 */
 	public void add(Triple triple, boolean flush) throws IOException, TrippiException {
 		m_writer.add(triple, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#delete(java.util.List, boolean)
 	 */
 	public void delete(List triples, boolean flush) throws IOException, TrippiException {
 		m_writer.delete(triples, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#delete(org.trippi.TripleIterator, boolean)
 	 */
 	public void delete(TripleIterator iter, boolean flush) throws IOException, TrippiException {
 		m_writer.delete(iter, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#delete(org.jrdf.graph.Triple, boolean)
 	 */
 	public void delete(Triple triple, boolean flush) throws IOException, TrippiException {
 		m_writer.delete(triple, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#flushBuffer()
 	 */
 	public void flushBuffer() throws IOException, TrippiException {
 		m_writer.flushBuffer();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#setFlushErrorHandler(org.trippi.FlushErrorHandler)
 	 */
 	public void setFlushErrorHandler(FlushErrorHandler h) {
 		m_writer.setFlushErrorHandler(h);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#getBufferSize()
 	 */
 	public int getBufferSize() {
 		return m_writer.getBufferSize();
 	}
 
 
 }
