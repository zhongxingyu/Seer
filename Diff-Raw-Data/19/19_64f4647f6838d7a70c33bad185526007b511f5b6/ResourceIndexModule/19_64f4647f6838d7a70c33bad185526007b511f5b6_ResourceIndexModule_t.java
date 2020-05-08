 package fedora.server.resourceIndex;
 
 import java.io.*;
 import java.util.*;
 
 import org.trippi.*;
 import org.trippi.impl.multi.*;
 import org.jrdf.graph.*;
 
 import fedora.server.*;
 import fedora.server.errors.*;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.ConnectionPoolManager;
 import fedora.server.storage.types.*;
 
 public class ResourceIndexModule extends Module 
                                 implements ResourceIndex {
 
     private int m_level;
     private TriplestoreConnector m_conn;
     private ResourceIndex m_resourceIndex;
 
 	public ResourceIndexModule(Map moduleParameters, Server server, String role) 
 	throws ModuleInitializationException {
 		super(moduleParameters, server, role);
 	}
 
 	public void postInitModule() throws ModuleInitializationException {
 		logConfig("ResourceIndexModule: loading...");
 		// Parameter validation
 		if (getParameter("level")==null) {
 			throw new ModuleInitializationException(
                     "level parameter must be specified.", getRole());
         } else {
         	try {
                 m_level = Integer.parseInt(getParameter("level"));
                 if (m_level < 0 || m_level > 2) {
                 	throw new NumberFormatException();
                 }
     		} catch (NumberFormatException nfe) {
     			throw new ModuleInitializationException(
                         "level parameter must have value 0, 1, or 2", getRole());
     		}
             // If level == 0, we don't want to proceed further.
             if (m_level == 0) {
                 return;
             }
         }
         
         //
         // get connectionPool from ConnectionPoolManager
         //
         ConnectionPoolManager cpm=(ConnectionPoolManager) getServer().
                 getModule("fedora.server.storage.ConnectionPoolManager");
         if (cpm==null) {
             throw new ModuleInitializationException(
                 "ConnectionPoolManager module was required, but apparently has "
                 + "not been loaded.", getRole());
         }
         String cPoolName=getParameter("connectionPool");
         ConnectionPool cPool=null;
         try {
             if (cPoolName==null) {
                 logConfig("connectionPool unspecified; using default from "
                         + "ConnectionPoolManager.");
                 cPool=cpm.getPool();
             } else {
                 logConfig("connectionPool specified: " + cPoolName);
                 cPool=cpm.getPool(cPoolName);
             }
         } catch (ConnectionPoolNotFoundException cpnfe) {
             throw new ModuleInitializationException("Could not find requested "
                     + "connectionPool.", getRole());
         }
 
         // Get anything starting with alias: and put the following name
         // and its value in the alias map.
         //
         HashMap aliasMap = new HashMap();
         Iterator iter = parameterNames();
         while (iter.hasNext()) {
             String pName = (String) iter.next();
             String[] parts = pName.split(":");
             if ((parts.length == 2) && (parts[0].equals("alias"))) {
                 aliasMap.put(parts[1], getParameter(pName));
             }
         }
         
         String datastore = getParameter("datastore");
         if (datastore == null || datastore.equals("")) {
             throw new ModuleInitializationException(
                       "datastore parameter must be specified.", getRole());
         }
         Parameterized conf = getServer().getDatastoreConfig(datastore);
         if (conf == null) {
             throw new ModuleInitializationException(
                       "No such datastore: " + datastore, getRole());
         }
         Map map = conf.getParameters();
         String connectorClassName = (String) map.get("connectorClassName");
         if (connectorClassName == null || connectorClassName.equals("")) {
             throw new ModuleInitializationException(
                       "Datastore \"" + datastore + "\" must specify a "
                       + "connectorClassName", getRole());
         }
         // params ok, let's init the triplestore
         try {
             m_conn = TriplestoreConnector.init(connectorClassName,
                                                map);
 
             // Make a MultiConnector if any mirrors are specified
             String mirrors = getParameter("mirrors");
             if (mirrors != null) {
                 mirrors = mirrors.trim();
                 if (mirrors.length() > 0) {
                     String[] mirrorList = mirrors.replaceAll(" +", ",").split(",");
                     // make sure they exist first
                     for (int i = 0; i < mirrorList.length; i++) {
                         Parameterized mConf = getServer().getDatastoreConfig(mirrorList[i]);
                         if (mConf == null) {
                             throw new ModuleInitializationException("No such datastore: " + mirrorList[i], getRole());
                         }
                     }
                     // then put them into the array
                     TriplestoreConnector[] connectors = new TriplestoreConnector[mirrors.length() + 1];
                     connectors[0] = m_conn;
                     for (int i = 0; i < mirrorList.length; i++) {
                         Map mMap = getServer().getDatastoreConfig(mirrorList[i]).getParameters();
                         String mClass = (String) mMap.get("connectorClassName");
                         connectors[i+1] = TriplestoreConnector.init(mClass, mMap);
                     }
                     m_conn = new MultiConnector(connectors);
                 }
             }
             try {
                 m_resourceIndex = new ResourceIndexImpl(m_level, m_conn, cPool, aliasMap, this);
             } catch (ResourceIndexException e) {
                 throw new ModuleInitializationException("Error initializing "
                        + "connection pool.", getRole(), e);
             } 
         } catch (ClassNotFoundException e) {
             throw new ModuleInitializationException("Connector class \"" 
                     + connectorClassName + "\" not in classpath.", getRole(), e);
        } catch (Exception e) {
            throw new ModuleInitializationException("Error initializing ResourceIndexModule", 
                                                    getRole(), e);
         }
     }
 
     public void shutdownModule() throws ModuleShutdownException {
         try {
             if (m_conn != null) m_conn.close();
         } catch (TrippiException e) {
             throw new ModuleShutdownException("Error closing triplestore "
                     + "connector", getRole(), e);
         }
     }
     
     /* from ResourceIndex interface */
     public int getIndexLevel() {
         // if m_level is 0, we never instantiated the ResourceIndex in the first place
         if (m_level == 0) {
             return m_level;
         } else {
             return m_resourceIndex.getIndexLevel();
         }
     }
     
     /* (non-Javadoc)
      * @see fedora.server.resourceIndex.ResourceIndex#addDigitalObject(fedora.server.storage.types.DigitalObject)
      */
     public void addDigitalObject(DigitalObject digitalObject) throws ResourceIndexException {
         m_resourceIndex.addDigitalObject(digitalObject);
     }
 
     /* (non-Javadoc)
      * @see fedora.server.resourceIndex.ResourceIndex#modifyDigitalObject(fedora.server.storage.types.DigitalObject)
      */
     public void modifyDigitalObject(DigitalObject digitalObject) throws ResourceIndexException {
         m_resourceIndex.modifyDigitalObject(digitalObject);
     }
 
     /* (non-Javadoc)
      * @see fedora.server.resourceIndex.ResourceIndex#deleteDigitalObject(java.lang.String)
      */
     public void deleteDigitalObject(DigitalObject digitalObject) throws ResourceIndexException {
         m_resourceIndex.deleteDigitalObject(digitalObject);
     }
     
     public void commit() throws ResourceIndexException {
         m_resourceIndex.commit();
     }
     
     public void export(OutputStream out, RDFFormat format) throws ResourceIndexException {
         m_resourceIndex.export(out, format);
     }
 
     /* from TriplestoreReader interface */
     public void setAliasMap(Map aliasToPrefix) throws TrippiException {
         m_resourceIndex.setAliasMap(aliasToPrefix);
     }
 
     public Map getAliasMap() throws TrippiException {
         return m_resourceIndex.getAliasMap();
     }
 
     public TupleIterator findTuples(String queryLang,
                                     String tupleQuery,
                                     int limit,
                                     boolean distinct) throws TrippiException {
         return m_resourceIndex.findTuples(queryLang, tupleQuery, limit, distinct);
     }
 
     public int countTuples(String queryLang,
                            String tupleQuery,
                            int limit,
                            boolean distinct) throws TrippiException {
         return m_resourceIndex.countTuples(queryLang, tupleQuery, limit, distinct);
     }
 
     public TripleIterator findTriples(String queryLang,
                                       String tupleQuery,
                                       int limit,
                                       boolean distinct) throws TrippiException {
         return m_resourceIndex.findTriples(queryLang, tupleQuery, limit, distinct);
     }
 
     public int countTriples(String queryLang,
                             String tupleQuery,
                             int limit,
                             boolean distinct) throws TrippiException {
         return m_resourceIndex.countTriples(queryLang, tupleQuery, limit, distinct);
     }
 
     public TripleIterator findTriples(SubjectNode subject,
                                       PredicateNode predicate,
                                       ObjectNode object,
                                       int limit) throws TrippiException {
         return m_resourceIndex.findTriples(subject, predicate, object, limit);
     }
 
     public int countTriples(SubjectNode subject,
                             PredicateNode predicate,
                             ObjectNode object,
                             int limit) throws TrippiException {
         return m_resourceIndex.countTriples(subject, predicate, object, limit);
     }
 
     public TripleIterator findTriples(String queryLang,
                                       String tupleQuery,
                                       String tripleTemplate,
                                       int limit,
                                       boolean distinct) throws TrippiException {
         return m_resourceIndex.findTriples(queryLang, tupleQuery, tripleTemplate, limit, distinct);
     }
 
     public int countTriples(String queryLang,
                             String tupleQuery,
                             String tripleTemplate,
                             int limit,
                             boolean distinct) throws TrippiException {
         return m_resourceIndex.countTriples(queryLang, tupleQuery, tripleTemplate, limit, distinct);
     }
 
     public String[] listTupleLanguages() {
         return m_resourceIndex.listTupleLanguages();
     }
 
     public String[] listTripleLanguages() {
         return m_resourceIndex.listTripleLanguages();
     }
 
     public void close() throws TrippiException {
         // nope
     }
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#add(java.util.List, boolean)
 	 */
 	public void add(List triples, boolean flush) throws IOException, TrippiException {
 		m_resourceIndex.add(triples, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#add(org.trippi.TripleIterator, boolean)
 	 */
 	public void add(TripleIterator iter, boolean flush) throws IOException, TrippiException {
 		m_resourceIndex.add(iter, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#add(org.jrdf.graph.Triple, boolean)
 	 */
 	public void add(Triple triple, boolean flush) throws IOException, TrippiException {
 		m_resourceIndex.add(triple, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#delete(java.util.List, boolean)
 	 */
 	public void delete(List triples, boolean flush) throws IOException, TrippiException {
 		m_resourceIndex.delete(triples, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#delete(org.trippi.TripleIterator, boolean)
 	 */
 	public void delete(TripleIterator iter, boolean flush) throws IOException, TrippiException {
 		m_resourceIndex.delete(iter, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#delete(org.jrdf.graph.Triple, boolean)
 	 */
 	public void delete(Triple triple, boolean flush) throws IOException, TrippiException {
 		m_resourceIndex.delete(triple, flush);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#flushBuffer()
 	 */
 	public void flushBuffer() throws IOException, TrippiException {
 		m_resourceIndex.flushBuffer();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#setFlushErrorHandler(org.trippi.FlushErrorHandler)
 	 */
 	public void setFlushErrorHandler(FlushErrorHandler h) {
 		m_resourceIndex.setFlushErrorHandler(h);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.trippi.TriplestoreWriter#getBufferSize()
 	 */
 	public int getBufferSize() {
 		return m_resourceIndex.getBufferSize();
 	}
 }
