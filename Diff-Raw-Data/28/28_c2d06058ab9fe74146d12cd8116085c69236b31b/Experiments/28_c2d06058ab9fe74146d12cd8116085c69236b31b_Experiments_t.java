 package uk.ac.ebi.ae15;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.apache.commons.logging.Log;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.sql.DataSource;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import java.io.*;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.sql.*;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.xml.serialize.*;
 
 public class Experiments {
 
     // logging macinery
     private static final Log log = org.apache.commons.logging.LogFactory.getLog(Experiments.class);
 
     // sql to get a list of experiments from the database
     private static String getExperimentsSql = "select distinct e.id, i.identifier as accession, case when v.user_id = 1 then 1 else 0 end as \"public\"" +
             " from tt_experiment e left outer join tt_identifiable i on i.id = e.id" +
             "  left outer join tt_extendable ext on ext.id = e.id" +
             "  left outer join pl_visibility v on v.label_id = ext.label_id" +
 //          " where i.identifier like 'E-GEOD-20%'" +
             " order by" +
             "  i.identifier asc";
 
     private static int numOfParallelConnections = 25;
     private static int initialXmlStringBufferSize = 20000000;  // 20 Mb
 
     private final static String XML_DOCUMENT_VERSION = "1.0.080604.2";
 
     public Experiments()
     {
         experimentsXmlCacheLocation = getCachedXmlLocation();
     }
 
     public Document getExperiments()
     {
         if ( null == experimentsDoc ) {
             if ( !loadExperimentsFromCache() ) {
                 log.warn("Experiments XML Cache was NOT loaded, possible fresh start up?");
                 experimentsDoc = createExperimentsDocument();
             }
             //TODO: temp
             saveSpeciesAndArraysToCache();
             experimentSearch.buildText(experimentsDoc);
 
         }
         return experimentsDoc;
     }
 
     //TODO: refactor!
     public String getArrays()
     {
         if ( null == arraysString ) {
             arraysString = loadStringFromCache("ae-arrays.xml");
         }
         return arraysString;
     }
 
     private static String arraysString = null;
 
     public String getSpecies()
     {
         if ( null == speciesString ) {
             speciesString = loadStringFromCache("ae-species.xml");
         }
         return speciesString;
     }
 
     private static String speciesString = null;
 
     private static String loadStringFromCache( String name )
     {
         String result = "";
         try {
             BufferedReader r = new BufferedReader( new InputStreamReader( new FileInputStream( System.getProperty("java.io.tmpdir") + File.separator + name )));
             result = r.readLine();
         } catch ( Throwable x ) {
             log.debug( "Caught an exception:", x );
             //TODO:error loggging
         }
         return result;
     }
 
     public ExperimentSearch Search()
     {
         return experimentSearch;
     }
 
     public void reloadExperiments( String dsName, boolean onlyPublic )
     {
         if ( loadExperimentsFromDataSource( dsName, onlyPublic ) ) {
             saveExperimentsToCache();
             saveSpeciesAndArraysToCache();
         } else {
             log.error("Unable to reload experiments from [" + dsName + "], check log for messages" );
             experimentsDoc = createExperimentsDocument();            
         }
         experimentSearch.buildText(experimentsDoc);
     }
 
     //  returns the location of xml cache ({java.io.tmpdir}/{ae.experiments.cache.location})
     private URL getCachedXmlLocation()
     {
         URL url = null;
 
         String tmpDir = System.getProperty("java.io.tmpdir");
 
         if ( null != tmpDir ) {
             String relCacheLocation = Application.Preferences().getProperty("ae.experiments.cache.location");
             if ( null != relCacheLocation ) {
                 try {
                     url = new File( tmpDir + File.separator + relCacheLocation ).toURL();
                 } catch ( MalformedURLException x ) {
                     if (log.isDebugEnabled()) {
                         log.debug( "Caught an exception:", x );
                     } else {
                         log.error( "Caught an exception while attempting to form a URL for XML cache. tmpDir: " + tmpDir + ", relCacheLocation: " + relCacheLocation + ", message: " + x.getMessage() );
                     }
                 }
             }
         }
 
         if ( null == url ) {
             log.error("Experiments XML cache location was not determined, expect problems down the road");
         }
 
         return url;
     }
 
     private void saveExperimentsToCache()
     {
         try {
             OutputFormat format = new OutputFormat();
             format.setLineSeparator(LineSeparator.Web);
             format.setIndenting(true);
             format.setLineWidth(0);
             format.setPreserveSpace(true);
             FileWriter fw = new FileWriter(experimentsXmlCacheLocation.getFile());
             XMLSerializer serializer = new XMLSerializer(fw, format);
             serializer.asDOMSerializer();
             serializer.serialize(experimentsDoc);
             fw.flush();
             fw.close();
         } catch ( Throwable x ) {
             log.debug( "Caught an exception:", x );
         }
     }
 
     private void saveSpeciesAndArraysToCache()
     {
         XsltHelper.transformDocumentToFile( experimentsDoc, "preprocess-species-html.xsl", null, new File(System.getProperty("java.io.tmpdir") + File.separator + "ae-species.xml") );
         XsltHelper.transformDocumentToFile( experimentsDoc, "preprocess-arrays-html.xsl", null, new File(System.getProperty("java.io.tmpdir") + File.separator + "ae-arrays.xml") );
     }
 
     private boolean loadExperimentsFromCache()
     {
         boolean isLoaded = false;
 
         if ( null != experimentsXmlCacheLocation ) {
             Document doc;
 
             try {
                 //parse using builder to get DOM representation of the XML file
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                doc = docBuilder.parse( experimentsXmlCacheLocation.getFile() );
 
                 if ( null != doc ) {
                     String docVer = doc.getDocumentElement().getAttribute("version");
                     if ( !XML_DOCUMENT_VERSION.equals(docVer) ) {
                         log.error( "Cache XML document version mismatch: loaded [" + docVer + "], expected [" + XML_DOCUMENT_VERSION + "]" );
                     } else {
                         log.info("Successfuly loaded experiments from XML cache, version " + docVer );
                         experimentsDoc = doc;
                         isLoaded = true;
                     }
                 }
             } catch ( Throwable x ) {
                 log.debug( "Caught an exception:", x );
             }
         }
 
         return isLoaded;
     }
 
     private boolean loadExperimentsFromString( String xmlString )
     {
         Document dom = XsltHelper.transformStringToDocument( xmlString, "preprocess-experiments-xml.xsl", null );
         if ( null == dom ) {
             log.error("Pre-processing returned an error, will have an empty document");
             return false;
         } else {
             experimentsDoc = dom;
             return true;
         }
     }
 
 
     public boolean loadExperimentsFromDataSource( String dataSourceName, boolean onlyPublic )
     {
         log.info("ArrayExpress Repository XML reload started.");
 
         boolean result = false;
 
         StringBuilder xmlBuf = new StringBuilder(initialXmlStringBufferSize);
         xmlBuf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><experiments version=\"" + XML_DOCUMENT_VERSION + "\">");
 
         DataSource ds = getJdbcDataSource( dataSourceName );
         if ( null != ds ) {
 
             try {
                 // get a list of experiments populated into expList
                 List<ExperimentListEntry> expList = new ArrayList<ExperimentListEntry>();
 
                 Connection conn = ds.getConnection();
 
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(getExperimentsSql);
 
                 while ( rs.next() ) {
                     ExperimentListEntry entry = new ExperimentListEntry(
                             rs.getInt("id")
                             , rs.getString("accession")
                             , rs.getBoolean("public")
                     );
                     expList.add(entry);
                 }
 
                 rs.close();
                 conn.close();
 
                 //
                 int poolSize = Math.min( expList.size(), numOfParallelConnections );
 
                 // so we create a pool of threads :)
                 List<GetExperimentXmlThread> threadPool = new ArrayList<GetExperimentXmlThread>();
                 for ( int i = 0; i < poolSize; ++i ) {
                     GetExperimentXmlThread th = new GetExperimentXmlThread( ds, i );
                     th.start();
                     threadPool.add(th);
                 }
 
                 int expListIndex = 0;
                 int threadPoolIndex = 0;
                 int expCount = 0;
 
                 result = true;
 
                 while ( expListIndex < expList.size() && result ) {
                     if ( onlyPublic && !expList.get(expListIndex).isPublic ) {
                         // skipping this private experiment
                         expListIndex++;
                     } else {
                         GetExperimentXmlThread th = threadPool.get(threadPoolIndex);
                         if (th.isNew()) { // this thread is new - load it with work, fast!
                             th.setId(expList.get(expListIndex).id);
                             expListIndex++;
                         } else if (th.isFaulty()) { // this thread is faulty, discard it
                             threadPool.remove(threadPoolIndex);
                             th.retire();
                             result = false;
                         } else if (th.isDone()) { // this thread has done its job, reload it with more work :)
                             xmlBuf.append(th.getXml());
                             expCount++; // increment number of resulting experiments
                             th.setId(expList.get(expListIndex).id);
                             expListIndex++;
                         }
                         threadPoolIndex++;
                         if (threadPoolIndex >= threadPool.size())
                             threadPoolIndex = 0;
 
                         Thread.yield();
                     }
                 }
 
                 threadPoolIndex = 0;
                 while ( threadPool.size() > 0 ) {
                     GetExperimentXmlThread th = threadPool.get(threadPoolIndex);
                     if (th.isDone()) {
                         xmlBuf.append(th.getXml());
                         expCount++; // increment number of resulting experiments
                         threadPool.remove(threadPoolIndex);
                         th.retire();
                     }
                     threadPoolIndex++;
                     if (threadPoolIndex >= threadPool.size())
                         threadPoolIndex = 0;
 
                     Thread.yield();
                 }
                 if ( !result ) {
                     log.warn("There are indications that at least one thread has failed getting experiments from the database, expect problems down the road");
                 }
                 log.info("ArrayExpress Repository XML reload completed (" + expCount + "/" + expList.size() + " experiments loaded).");
 
             } catch ( Throwable x ) {
                 if ( log.isDebugEnabled() ) {
                     log.debug( "Caught an exception:", x );
                 } else {
                     log.error( "loadExperimentsFromDataSource Exception: " + x.getMessage() );
                 }
                 result = false;
             }
 
         } else {
             log.error("Unable to obtain data source object, no experiments retrieved.");
         }
 
         xmlBuf.append("</experiments>");
 
         if ( result ) {
             String xml = xmlBuf.toString().replaceAll("[^\\p{Print}]"," ");    // replace is a nasty hack to get rid of non-printable characters altogether
             result = loadExperimentsFromString(xml);
         } else  {
             log.error("Experiments Document WAS NOT loaded from database, expect problems down the road");
         }
         
         return result;
     }
 
     private Document createExperimentsDocument()
     {
         Document doc = null;
 
             try {
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 doc = docBuilder.newDocument();
 
                 Element expElement = doc.createElement("experiments");
                 expElement.setAttribute( "total", "0" );
                 expElement.setAttribute( "version", XML_DOCUMENT_VERSION );
 
                 doc.appendChild(expElement);
 
             } catch ( Throwable e ) {
                 log.debug( "Caught an exception:", e );
             }
 
         if ( null == doc ) {
             log.error("Experiments Document WAS NOT created, expect problems down the road");
         }
         
         return doc;
     }
 
     private DataSource getJdbcDataSource( String dataSourceName )
     {
         DataSource dataSource = null;
 
         try {
             Context initContext = new InitialContext();
             Context envContext = (Context)initContext.lookup( "java:/comp/env" );
 
             dataSource = (DataSource)envContext.lookup( "jdbc/" + dataSourceName.toLowerCase() );
         } catch ( NamingException x ) {
             if (log.isDebugEnabled()) {
                 log.debug( "Caught an exception:", x );
             } else {
                 log.error( "getJdbcDataSource NamingException: " + x.getMessage() );
             }
         }
 
         return dataSource;
     }
 
     // url that represents the location of xml file (for persistence)
     private URL experimentsXmlCacheLocation = null;
 
     // the document that contains the basis for all experiments
     private Document experimentsDoc = null;
 
     private ExperimentSearch experimentSearch = new ExperimentSearch();
 }
 
 class ExperimentListEntry {
 
     int id;
     String accession;
     boolean isPublic;
 
     public ExperimentListEntry( int _id, String _accession, boolean _public )
     {
         id = _id;
         accession = _accession;
         isPublic = _public;
     }
 
 }
 
 class GetExperimentXmlThread extends Thread implements Runnable {
 
     private final static Log log = org.apache.commons.logging.LogFactory.getLog(GetExperimentXmlThread.class);
 
     private boolean isNew = true;       // is thread new to the business?
     private boolean isReady = false;    // is thread ready to execute a statement?
     private boolean isDone = false;     // is thread done with a statement result?
     private boolean isRetired = false;  // is thread to be retired?
     private boolean isFaulty = false;   // has thready thrown any exception in the past - we abort
 
     private DataSource dataSource;
     private int experimentId;
     private String result;
 
     private static String getExperimentXmlSql = "select XmlElement( \"experiment\"" +
         " , XmlAttributes( e.id as \"id\", i.identifier as \"accession\", nvt_name.value as \"name\", nvt_releasedate.value as \"releasedate\", nvt_miamegold.value as \"miamegold\" )" +
         " , ( select XmlAgg( XmlElement( \"user\", v.user_id ) ) from tt_extendable ext left outer join pl_visibility v on v.label_id = ext.label_id where ext.id = e.id )" +
         " , ( select XmlAgg( XmlElement( \"secondaryaccession\", sa.value ) ) from tt_namevaluetype sa where sa.t_extendable_id = e.id and sa.name = 'SecondaryAccession' )" +
         " , ( select XmlAgg( XmlElement( \"sampleattribute\", XmlAttributes( i4samattr.category as \"category\", i4samattr.value as \"value\") ) ) from ( select  /*+ LEADING(b) INDEX(o) INDEX(c) INDEX(b)*/ distinct b.experiments_id as id, o.category, o.value from tt_ontologyentry o, tt_characteris_t_biomateri c, tt_biomaterials_experiments b where b.biomaterials_id = c.t_biomaterial_id and c.characteristics_id = o.id ) i4samattr where i4samattr.id = e.id group by i4samattr.id )" +
         " , ( select XmlAgg( XmlElement( \"experimentalfactor\", XmlAttributes( i4efvs.name as \"name\", i4efvs.value as \"value\") ) ) from (select /*+ leading(d) index(d) index(doe) index(tl) index(f) index(fi) index(fv) index(voe) index(m) */ distinct d.t_experiment_id as id, fi.name as name, ( case when voe.value is not null then voe.value else m.value end ) as value from tt_experimentdesign d, tt_ontologyentry doe, tt_types_t_experimentdesign tl, tt_experimentalfactor f, tt_identifiable fi, tt_factorvalue fv, tt_ontologyentry voe, tt_measurement m where doe.id = tl.types_id and tl.t_experimentdesign_id = d.id and f.t_experimentdesign_id (+) = d.id and fv.experimentalfactor_id (+) = f.id and voe.id (+) = fv.value_id and fi.id (+) = f.id and m.id (+) = fv.measurement_id) i4efvs where i4efvs.id = e.id group by i4efvs.id )" +
         " , ( select XmlElement( \"miamescore\", XmlAgg( XmlElement( \"score\", XmlAttributes( nvt_miamescores.name as \"name\", nvt_miamescores.value as \"value\" ) ) ) ) from tt_namevaluetype nvt_miamescores, tt_namevaluetype nvt_miame where nvt_miame.id = nvt_miamescores.t_namevaluetype_id and nvt_miame.t_extendable_id = e.id and nvt_miame.name = 'AEMIAMESCORE' group by nvt_miame.value )" +
         " , ( select /*+ index(pba) */ XmlAgg( XmlElement( \"arraydesign\", XmlAttributes( a.arraydesign_id as \"id\", i4array.identifier as \"accession\", nvt_array.value as \"name\" , count(a.arraydesign_id) as \"count\" ) ) ) from tt_bioassays_t_experiment ea inner join tt_physicalbioassay pba on pba.id = ea.bioassays_id inner join tt_bioassaycreation h on h.id = pba.bioassaycreation_id inner join tt_array a on a.id = h.array_id inner join tt_identifiable i4array on i4array.id = a.arraydesign_id inner join tt_namevaluetype nvt_array on nvt_array.t_extendable_id = a.arraydesign_id and nvt_array.name = 'AEArrayDisplayName' where ea.t_experiment_id = e.id group by a.arraydesign_id, i4array.identifier, nvt_array.value )" +
         " , ( select /*+ leading(i7) index(bad)*/ XmlAgg( XmlElement( \"bioassaydatagroup\", XmlAttributes( badg.id as \"id\", i8.identifier as \"name\", count(badg.id) as \"bioassaydatacubes\", ( select substr( i10.identifier, 3, 4 ) from tt_arraydesign_bioassaydat abad, tt_identifiable i10 where abad.bioassaydatagroups_id = badg.id and i10.id = abad.arraydesigns_id and rownum = 1 ) as \"arraydesignprovider\", ( select d.dataformat from tt_bioassays_t_bioassayd b, tt_bioassaydata c, tt_biodatacube d, tt_bioassaydat_bioassaydat badbad where b.t_bioassaydimension_id = c.bioassaydimension_id and c.biodatavalues_id = d.id and badbad.bioassaydatas_id = c.id and badbad.bioassaydatagroups_id = badg.id and rownum = 1) as \"dataformat\", ( select count(bbb.bioassays_id) from tt_bioassays_bioassaydat bbb where bbb.bioassaydatagroups_id = badg.id ) as \"bioassays\", ( select count(badg.id) from tt_derivedbioassaydata dbad, tt_bioassaydat_bioassaydat bb where bb.bioassaydatagroups_id = badg.id and dbad.id = bb.bioassaydatas_id and rownum = 1 ) as \"isderived\" ) ) ) from  tt_bioassaydatagroup badg, tt_bioassaydat_bioassaydat bb, tt_bioassaydata bad, tt_identifiable i8 where badg.experiment_id = e.id and bb.bioassaydatagroups_id = badg.id and bad.id = bb.bioassaydatas_id and i8.id = bad.designelementdimension_id group by i8.identifier, badg.id )" +
         " , ( select XmlAgg( XmlElement( \"bibliography\", XmlAttributes( trim(db.accession) as \"accession\", trim(b.publication) AS \"publication\", trim(b.authors) AS \"authors\", trim(b.title) as \"title\", trim(b.year) as \"year\", trim(b.volume) as \"volume\", trim(b.issue) as \"issue\", trim(b.pages) as \"pages\", trim(b.uri) as \"uri\" ) ) ) from tt_bibliographicreference b, tt_description dd, tt_accessions_t_bibliogra ab, tt_databaseentry db where b.t_description_id = dd.id and dd.t_describable_id = e.id and ab.t_bibliographicreference_id(+) = b.id and db.id (+)= ab.accessions_id )" +
         " , ( select XmlAgg( XmlElement( \"provider\", XmlAttributes( pp.firstname || ' ' || pp.lastname AS \"contact\", c.email AS \"email\", value AS \"role\" ) ) ) from tt_identifiable ii, tt_ontologyentry o, tt_providers_t_experiment p, tt_roles_t_contact r, tt_person pp, tt_contact c where c.id = r.t_contact_id and ii.id = r.t_contact_id and r.roles_id = o.id and pp.id = ii.id and ii.id = p.providers_id and p.t_experiment_id = e.id )" +
         " , ( select XmlAgg( XmlElement( \"experimentdesign\", expdesign ) ) from ( select  /*+ index(ed) */ distinct ed.t_experiment_id as id, translate(replace(oe.value,'_design',''),'_',' ') as expdesign from tt_experimentdesign ed, tt_types_t_experimentdesign tte, tt_ontologyentry oe where tte.t_experimentdesign_id = ed.id and oe.id = tte.types_id and oe.category = 'ExperimentDesignType' ) t where t.id = e.id )" +
         " , XmlAgg( XmlElement( \"description\", XmlAttributes( d.id as \"id\" ), d.text ) ) " +
         " ).getClobVal() as xml" +
         " from tt_experiment e" +
         "  left outer join tt_description d on d.t_describable_id = e.id" +
         "  left outer join tt_identifiable i on i.id = e.id" +
         "  left outer join tt_namevaluetype nvt_releasedate on ( nvt_releasedate.t_extendable_id = e.id and nvt_releasedate.name = 'ArrayExpressLoadDate' )" +
         "  left outer join tt_namevaluetype nvt_name on ( nvt_name.t_extendable_id = e.id and nvt_name.name = 'AEExperimentDisplayName' )" +
         "  left outer join tt_namevaluetype nvt_miamegold on ( nvt_miamegold.t_extendable_id=e.id and nvt_miamegold.name='AEMIAMEGOLD' )" +
         " where" +
         "  e.id = ?" +
         " group by" +
         "  e.id" +
         "  , i.identifier" +
         "  , nvt_name.value" +
         "  , nvt_releasedate.value" +
         "  , nvt_miamegold.value";
 
     public GetExperimentXmlThread( DataSource ds, int index )
     {
         super("GetExperimentXmlThread[" + Integer.toString(index) + "]");
         dataSource = ds;
     }
 
     public static String ClobToString(Clob cl) throws IOException, SQLException
     {
         if (cl == null)
             return null;
 
         StringBuilder strOut = new StringBuilder();
         String aux;
 
 	    // We access to stream, as this way we don't have to use the CLOB.length() which is slower...
     	BufferedReader br = new BufferedReader(cl.getCharacterStream());
 
         while ((aux=br.readLine())!=null)
             strOut.append(aux);
 
         return strOut.toString();
     }
 
     public void run()
     {
         // initializing the thread's internals
         Connection conn = null;
         try {
             conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getExperimentXmlSql);
 
             // loop the thread till we want it retired
             while (!isRetired) {
 
                 // wait till id is set
                 while (!isReady && !isRetired) {
                     Thread.sleep(250);
                 }
 
                 if (!isRetired) {
                     try {
                         setReady(false);
                         stmt.setInt( 1, experimentId );
                         ResultSet rs = stmt.executeQuery();
 
                         if ( rs.next() ) {
                             Clob xmlClob = rs.getClob(1);
                             result = ClobToString(xmlClob);
                         }
                         rs.close();
                     } catch ( SQLException x) {
                         log.error( "GetExperimentXmlThread.run SQLException: " + x.getMessage() );
                         result = null;
                         isFaulty = true;
                     }
                     setDone(true);
                 }
             }
         }
         catch(Exception x) {
             log.error( "GetExperimentXmlThread.run Exception: " + x.getMessage() );
             isFaulty = true;
         }
 
         if ( null != conn ) {
             try {
                 conn.close();
             } catch (SQLException x) {
                 log.error( "GetExperimentXmlThread.run (conn.close) SQLException: " + x.getMessage() );
                 isFaulty = true;
             }
         }
     }
 
     public void setId( int id )
     {
         log.debug( "setId: " + Integer.toString(id) );
         experimentId = id;
         isNew = false;
         setDone(false);
         setReady(true);
     }
 
     public void retire()
     {
         isRetired = true;
     }
 
     public boolean isNew()
     {
         return isNew;
     }
 
     public boolean isDone()
     {
         return isDone;
     }
 
     public boolean isFaulty()
     {
         return isFaulty;
     }
     public String getXml()
     {
         if ( null != result ) {
             log.debug( "getXml: " + result.substring(0, 55) + "..." );
         } else {
             log.debug( "getXml: result is null, expect problems down the road." );
         }
         return result;
     }
 
     private synchronized void setReady( boolean ready )
     {
         isReady = ready;
     }
 
     private synchronized void setDone( boolean done )
     {
         isDone = done;
     }
 }
