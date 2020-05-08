 /* $Id$
 
 The State and University DOMS project.
 Copyright (C) 2006  The State and University Library
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
 package dk.statsbiblioteket.doms.repository;
 
 import commonj.sdo.DataObject;
 import commonj.sdo.Sequence;
 import commonj.sdo.helper.HelperContext;
 import commonj.sdo.helper.XMLDocument;
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import dk.statsbiblioteket.doms.client.exceptions.NotFoundException;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.objects.FedoraState;
 import dk.statsbiblioteket.doms.client.relations.ObjectRelation;
 import dk.statsbiblioteket.doms.exceptions.MyDeleteException;
 import dk.statsbiblioteket.doms.exceptions.MyXMLParseException;
 import dk.statsbiblioteket.doms.exceptions.MyXMLWriteException;
 import dk.statsbiblioteket.doms.guiclient.GuiClient;
 import dk.statsbiblioteket.doms.guiclient.GuiClientImpl;
 import dk.statsbiblioteket.doms.guiclient.SearchResult;
 import dk.statsbiblioteket.doms.guiobjectmanipulation.ObjectManipulation;
 import dk.statsbiblioteket.doms.guiobjectmanipulation.ObjectManipulationService;
 import dk.statsbiblioteket.doms.guiobjectmanipulation.ObjectManipulationServiceLocator;
 import dk.statsbiblioteket.doms.model.object.DOMSDataObject;
 import dk.statsbiblioteket.doms.model.owl.Relation;
 import dk.statsbiblioteket.doms.objectcreation.DomsObjectCreation;
 import dk.statsbiblioteket.doms.objectcreation.DomsObjectCreationService;
 import dk.statsbiblioteket.doms.objectcreation.DomsObjectCreationServiceLocator;
 import dk.statsbiblioteket.doms.relations.DigitalObject;
 import dk.statsbiblioteket.doms.relations.Triple;
 import dk.statsbiblioteket.doms.repository.management.Configuration;
 import dk.statsbiblioteket.doms.resourceindex.DomsResourceIndex;
 import dk.statsbiblioteket.doms.resourceindex.DomsResourceIndexService;
 import dk.statsbiblioteket.doms.resourceindex.DomsResourceIndexServiceLocator;
 import dk.statsbiblioteket.doms.util.Constants;
 import dk.statsbiblioteket.doms.util.Util;
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.axis.types.URI;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.tuscany.sdo.util.SDOUtil;
 import org.fcrepo.client.FedoraClient;
 import org.fcrepo.server.access.FedoraAPIA;
 import org.fcrepo.server.management.FedoraAPIM;
 import org.fcrepo.server.types.gen.*;
 import org.jboss.seam.Component;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.*;
 import org.jboss.seam.core.Events;
 import org.jboss.seam.log.Log;
 import org.jboss.seam.security.Identity;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Remove;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.rpc.ServiceException;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 
 
 /**
  * Stateless connector to a Fedora installation. This connector uses the
  * {@link FedoraClient} for communication.
  */
 @Name("repository")
 @Scope(ScopeType.SESSION)
 
 public class RepositoryBean {
 
     private static final String FOXML =     "foxml1.0";
 
     private static final String PROTOCOL =  "fedora_protocol";
 
     private static final String HOST =       "fedora_host";
     private static final String PORT =       "fedora_port";
     private static final String USER =       "fedora_user";
     private static final String PASS =       "fedora_pass";
 
     private static final String fedoraProperties = "FedoraIngestProperties";
 
 
     private static final String ENCODING = "UTF-8";
 
     protected String baseURL;
     protected FedoraClient fc;
     protected FedoraAPIA apiA;
     protected FedoraAPIM apiM;
 
     private boolean tested = false;
 
     @Logger
     private Log logger;
 
     @In(value="domsGuiConfiguration")
     Configuration configuration;
 
     @In
     Identity identity;
     private GuiClient domsClient;
 
     @Remove @Destroy
     public void destroy()
     {
         logger.info("RepositoryImpl is destroyed.");
         Identity.instance().logout();
         Events.instance().raiseEvent(Identity.EVENT_LOGGED_OUT) ;
     }
 
     // @Create
     @PostConstruct
     public void setup() throws RemoteException, Exception
     {
         if (configuration==null)
             configuration = (Configuration) Component.getInstance("domsGuiConfiguration");
         if (identity == null)
             identity = ((Identity) Component.getInstance(Identity.class));
         prepareConnection();
         
     }
 
     /**
      */
     public RepositoryBean() throws RemoteException, Exception
     {
     }
 
     /**
      * Sets up connection parameters based on properties. No actual connections
      * are established.
      * @throws MalformedURLException if URL for the connector was faulty.
      */
     private void prepareConnection() throws Exception {
 
         //configuration = (Configuration) Component.getInstance("domsGuiConfiguration");
 
         domsClient = new GuiClientImpl(new URL(configuration.getBaseURL()+"centralWebservice-service/central/?wsdl"), identity.getUsername(),configuration.getPassword());
         fc = null; //new FedoraClient(configuration.getBaseURL(),
                // identity.getUsername(), configuration.getPassword());
         apiA = null; // fc.getAPIA();
         apiM = null; // fc.getAPIM();
     }
 
     public List<SearchResult> search(String query) throws ServiceException, RemoteException, MalformedURLException, ServerOperationFailed {
         
         return searchSelection(query, 0, 50); // TODO offset and pageLength must be set as a config option
     }
 
     private List<SearchResult> searchSelection(String query, int offset, int pageLength) throws RemoteException, ServerOperationFailed {
         
         return domsClient.search(query, offset, pageLength);
     }
 
     private String extractRecordId(DataObject fieldRecordId) throws  RemoteException {
         
         String pid = null;
         Sequence seq = fieldRecordId.getSequence();
         if (seq!=null) {
             pid = (String)seq.getValue(0);
             pid = pid.trim();
         }
         return pid;
     }
 
     private String[] extractShortformat(HelperContext context, DataObject fieldShortformat) throws  RemoteException {
         
         String[] titleTypeSource = new String[3];
 
         Sequence seq = fieldShortformat.getSequence();
         if (seq!=null) {
             String shortformatDocStr = (String)seq.getValue(0);
             //TODO Hack, the current version of the search webservice returns results that are not valid xml. The dc prefix is not bound.
             shortformatDocStr = shortformatDocStr.replaceFirst("<shortrecord>", "<shortrecord xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
             XMLDocument xmlDoc = context.getXMLHelper().load(shortformatDocStr);
 
             DataObject rootDataObject = xmlDoc.getRootObject();
             if (rootDataObject!=null)
             {
                 //get title
                 DataObject titleDataObject = rootDataObject.getDataObject("RDF.0/Description.0/title.0");
                 if (titleDataObject!=null) {
                     Sequence seq2 = titleDataObject.getSequence();
                     if (seq2!=null) {
                         if(seq2.size() != 0) {
                             titleTypeSource[0] = (String)seq2.getValue(0);
                             titleTypeSource[0] = titleTypeSource[0].trim();
                         }
                     }
                 }
                 //get type
                 DataObject typeDataObject = rootDataObject.getDataObject("RDF.0/Description.0/type.0");
                 if (typeDataObject!=null) {
                     Sequence typeSeq = typeDataObject.getSequence();
                     if (typeSeq!=null) {
                         if(typeSeq.size() != 0) {
                             titleTypeSource[1] = (String)typeSeq.getValue(0);
                             titleTypeSource[1] = titleTypeSource[1].trim();
                         }
                     }
                 }
 
                 //get source
                 DataObject sourceDataObject = rootDataObject.getDataObject("RDF.0/Description.0/source.0");
                 if (sourceDataObject!=null) {
                     Sequence sourceSeq = sourceDataObject.getSequence();
                     if (sourceSeq!=null) {
                         if(sourceSeq.size() != 0) {
                             titleTypeSource[2] = (String)sourceSeq.getValue(0);
                             titleTypeSource[2] = titleTypeSource[2].trim();
                         }
                     }
                 }
 
             }
         }
         return titleTypeSource;
     }
 
     public RelationshipTuple[] getRelationships(String pid, String relationship) throws RemoteException {
         
         return apiM.getRelationships(pid, relationship);
     }
 
     /* (non-Javadoc)
     * @see dk.statsbiblioteket.doms.database.Repository#addRelation(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String)
     */
     public Boolean addRelationship(String subjectPid, String relationship, String objectPid, Boolean isLiteral, String datatype) throws RemoteException {
         
         Boolean result = false;
         result = apiM.addRelationship(subjectPid, relationship, Constants.INFO_FEDORA_URI_SCHEME+objectPid, isLiteral , datatype);
 
         return result;
     }
 
     public Boolean purgeRelationship(String subjectPid, String relationship, String objectPid, Boolean isLiteral, String datatype) throws RemoteException {
         
         return apiM.purgeRelationship(subjectPid, relationship, Constants.INFO_FEDORA_URI_SCHEME+objectPid, isLiteral, datatype);
     }
 
     public void modifyDataStreamByValue(String pid, String dsID, byte[] content, String logMsg) throws UnsupportedEncodingException, RemoteException {
         
         apiM.modifyDatastreamByValue
                 (pid,
                         dsID,
                         null,
                         null,
                         "text/xml",
                         null,
                         content,
                         "DISABLED",
                         null,
                         logMsg,
                         false);
 
     }
 
     /**
      * Modifys a reference datastream (controlgroup E)
      * @param pid the pid of the dataobject that has the datastream to be modified
      * @param dsID	the id of the datastream
      * @param dsLocation the location to point the reference at
      * @param logMsg log message
      * @throws UnsupportedEncodingException
      * @throws RemoteException
      */
     public void modifyDataStreamByReference(String pid, String dsID, String dsLocation, String logMsg) throws UnsupportedEncodingException, RemoteException {
         
         apiM.modifyDatastreamByReference(
                 pid,
                 dsID,
                 null, 		//altIDs
                 null, 		//dsLabel
                 "text/xml", //mimeType
                 null, 		//formatURI
                 dsLocation, //The location to point the reference at
                 "DISABLED", //checksumtype
                 null, 		//checksum
                 "logMsg", 	//logMsg
                 false);     //force
 
     }
 
     public String ingest(byte[] xmlDoc, String logMessage) throws RemoteException {
         
         return apiM.ingest(xmlDoc, org.fcrepo.common.Constants.FOXML1_1.uri, logMessage);
     }
 
     public String addImportedInlineXmlDatastream(String pid, String dsId, String filename) throws RemoteException, IOException {
         
         File file = new File(filename);
         String tempUrl = fc.uploadFile(file);
         dsId = apiM.addDatastream(pid, dsId,
                 new String[0], "DOMS Fedora File Object Content Datastream",
                 true, "text/xml", null, tempUrl, "X", "I", "DISABLED", null, "Added imported xml data");
 
         return dsId;
 
     }
 
     public String modifyObject(String pid, String state, String label, String ownerId, String logMessage) throws RemoteException {
         
         return apiM.modifyObject(pid,
                 state,
                 label,
                 ownerId,
                 logMessage);
     }
 
     public org.fcrepo.server.types.gen.Datastream getDataStream(String pid, String dsID) throws RemoteException {
         
         org.fcrepo.server.types.gen.Datastream ds = null;
         try {
             ds = apiM.getDatastream(pid, dsID, null);
         }
         catch (RemoteException ex)
         {
             if (ex instanceof org.apache.axis.AxisFault) {
                 /* We get a fedora.server.errors.DatastreamNotFoundException if the Content Model
                      * does not have a DataStream with the specified ID. Here this is ok so we just
                      * silently ignores it.
                      */
             }
             else {
                 throw new Error("Failed remote procedure call 'findObjects' in getDataStream.",ex);
             }
         }
 
         return ds;
     }
 
     public ObjectMethodsDef[] getExportMethods(String pid) throws RemoteException {
         
         return getMethods(pid,  Util.getInitParameter("exportPrefix"));
     }
 
     public ObjectMethodsDef[] getImportMethods(String pid) throws RemoteException {
         
         return getMethods(pid,  Util.getInitParameter("importPrefix"));
     }
 
     private ObjectMethodsDef[] getMethods(String pid, String methodPrefix) throws RemoteException {
         
         ObjectMethodsDef[] methods = apiA.listMethods(pid, null);
         ArrayList<ObjectMethodsDef> importMethods = new ArrayList<ObjectMethodsDef>();
 
         for (ObjectMethodsDef method : methods) {
             if (method.getMethodName().startsWith(methodPrefix)) {
                 importMethods.add(method);
             }
         }
 
         ObjectMethodsDef[] temp = new ObjectMethodsDef[importMethods.size()];
         importMethods.toArray(temp);
         return temp;
     }
 
     public String importDOMSDataObject(String templatePid, String serviceDefinitionPID, String methodName, Property[] methodParameters) throws RemoteException {
         
         MIMETypedStream typedStream = apiA.getDissemination(templatePid, serviceDefinitionPID, methodName, methodParameters, null);
 
         String importResponse = new String (typedStream.getStream());
 
         String pid = extractImportPid(importResponse);
         if(pid.startsWith(Constants.INFO_FEDORA_URI_SCHEME)) {
             pid = pid.substring(Constants.INFO_FEDORA_URI_SCHEME.length());
         }
         return pid;
     }
 
     public void exportDOMSDataObject(String templatePid, String serviceDefinitionPID, String methodName, Property[] methodParameters) throws IOException {
         
         MIMETypedStream typedStream = apiA.getDissemination(templatePid, serviceDefinitionPID, methodName, methodParameters, null);
         String exportResponse = new String (typedStream.getStream());
         HttpServletResponse response = (HttpServletResponse)Util.getExternalContext().getResponse();
         response.setContentType("text/xml");
         response.addHeader("Content-Disposition", "attachment; filename=" + "domsExport.xml" );
         response.setBufferSize(exportResponse.getBytes().length);
         response.getOutputStream().write(exportResponse.getBytes());
         response.getOutputStream().flush();
         response.getOutputStream().close();
         javax.faces.context.FacesContext.getCurrentInstance().responseComplete();
 
         //return exportResponse;
     }
 
     /*
     * Temporary function to extract the pid of the imported object. Currently the import dissemination
     * returns the pid wrapped in xml. We expect it to be returned without the xml.
     */
     private String extractImportPid(String importResponse) throws  RemoteException {
         
         String pid = null;
 
         HelperContext context = SDOUtil.createHelperContext(true);
         XMLDocument xmlDoc = context.getXMLHelper().load(importResponse);
 
         DataObject rootDataObject = xmlDoc.getRootObject();
         if (rootDataObject!=null)
         {
             Sequence seq = rootDataObject.getSequence();
             for (int i = 0; i < seq.size(); i++) {
                 commonj.sdo.Property p = seq.getProperty(i);
                 if (p != null) {
                     DataObject dobj = (DataObject)seq.getValue(i);
                     Sequence seq2 = dobj.getSequence();
                     pid = (String)seq2.getValue(0);
                 }
             }
         }
 
         return pid;
     }
 
     /**
      * Gets the content of a DataStream.
      * @param fedoraUrl the URI to the DataStream, e.g. "info:fedora/doms:ContentModel_AudioPreservationFile/PRONOMID_SCHEMA"
      * @return An InputStream with the content of the DataStream.
      * @throws IOException
      * @deprecated
      */
     public InputStream getDatastreamDissemination(String fedoraUrl) throws IOException {
         
         InputStream result = null;
         try {
             result = fc.get(fedoraUrl, true);
         }
         catch (RemoteException ex)
         {
             if (ex instanceof org.apache.axis.AxisFault) {
                 /* We get a fedora.server.errors.DatastreamNotFoundException if the Content Model
                      * does not have a DataStream with the specified ID. Here this is ok so we just
                      * silently ignores it.
                      */
             }
             else {
                 throw new Error("Failed remote procedure call 'findObjects' in getDatastreamDissemination.",ex);
             }
         }
         return result;
     }
 
     /**
      * Gets the content of a DataStream.
      * @param pid The PID of the object
      * @param dataStreamIDID The DataStream ID
      * @return An InputStream with the content of the DataStream.
      * @throws IOException
      */
     public InputStream getDatastreamDissemination(String pid, String dataStreamIDID) throws IOException, MethodFailedException, InvalidResourceException {
         
         InputStream result = null;
         try
         {
             String ds = null;//apiA.getDatastreamDissemination(pid, dataStreamIDID, null);
             ds = domsClient.getFactory().getDigitalObject(pid).getDatastream(dataStreamIDID).getContents();
             if (ds!=null)
             {
                 result = new ByteArrayInputStream(ds.getBytes());
             }
             /*   }
         catch (RemoteException ex)
         {
         if (ex instanceof org.apache.axis.AxisFault) {
          /* We get a fedora.server.errors.DatastreamNotFoundException if the Content Model
             * does not have a DataStream with the specified ID. Here this is ok so we just
             * silently ignores it.
             */
             /*     }
                   else {
                       throw new Error("Failed remote procedure call 'findObjects' in getDatastreamDissemination.",ex);
                   }
             */  } catch (ServerOperationFailed ex) {
             throw new Error("Failed remote procedure call 'findObjects' in getDatastreamDissemination.",ex);
         } catch (NotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return result;
     }
 
     public String getDigitalObjectDCTitle(String pid) throws IOException, RemoteException, ServerOperationFailed {
         
         return domsClient.getFactory().getDigitalObject(pid).getTitle();
     }
 
 
     /**
      * @return A list containing the pid and the title of the ContentModels that have a gui view datastream with mainobject="true"
      * @throws IOException
      */
     public List<ObjectFields> getMainContentModels(NonNegativeInteger maxNumberOfCMs) throws IOException, MethodFailedException, InvalidResourceException {
         
         FieldSearchQuery query = new FieldSearchQuery();
 
         query.setTerms(null);
 
         String[] fieldToGet = {"pid", "title"};
 
         Condition[] conditions = new Condition[1];
 
         conditions[0] = new Condition("pid", ComparisonOperator.has, "doms:ContentModel*");
 
         query.setConditions(conditions);
 
         FieldSearchResult result = null;
 
         try {
             result = apiA.findObjects(fieldToGet,maxNumberOfCMs,query);
         } catch (RemoteException e) {
             throw new Error("Failed remote procedure call 'findObjects' in getMainContentModels.",e);
         }
 
         ArrayList<ObjectFields> results = new ArrayList<ObjectFields>();
         List<ObjectFields> objects = extractSearchResults(result);
         HelperContext context = SDOUtil.createHelperContext(true);
         for (ObjectFields object : objects)
         {
             InputStream is = getDatastreamDissemination(object.getPid(), "VIEW");
 
             if (is!=null)
             {
                 XMLDocument xmlDoc = context.getXMLHelper().load(is);
                 is.close();
 
                 DataObject rootDataObject = xmlDoc.getRootObject();
                 if (rootDataObject!=null)
                 {
                     DataObject mainobject = rootDataObject.getDataObject("view[name='GUI']");
                     if (mainobject!=null)
                     {
                         Object value = mainobject.get("mainobject");
                         if (value!=null)
                         {
                             if (value.equals("true"))
                             {
                                 results.add(object);
                             }
                         }
                     }
                 }
             }
         }
 
         return results;
     }
 
     public List<String> listPIDs() throws  RemoteException {
         
         return listPIDs("*");
     }
 
     public List<String> listPIDs(String terms) throws  RemoteException {
         
 
         FieldSearchQuery query = new FieldSearchQuery();
 
         query.setTerms(terms);
         //query.setTerms("pid~doms:*");
 
 
         String[] fieldToGet = {"pid"};
 
         FieldSearchResult result = null;
         NonNegativeInteger block = new NonNegativeInteger("20");
 
         try {
             result = apiA.findObjects(fieldToGet,block,query);
         } catch (RemoteException e) {
             throw new Error("Failed remote procedure call 'findObjects'",e);
         }
 
         List<String> pids = searchOnPIDs(result);
 
         try
         {
             InputStream is = fc.get("info:fedora/doms:ContentModel_AudioPreservationFile/PRONOMID_SCHEMA", true);
             if (is!=null)
             {
                 int i = is.read();
                 char c;
                 while (i!=-1)
                 {
                     c = (char)i;
                     //System.out.print(c);
                     i = is.read();
                 }
                 is.close();
             }
 
             MIMETypedStream ds2 = apiA.getDatastreamDissemination("doms:ContentModel_ReelTape", "VIEW", null);
             is = new ByteArrayInputStream(ds2.getStream());
 
             if (is!=null)
             {
                 int i = is.read();
                 char c;
                 while (i!=-1)
                 {
                     c = (char)i;
                     i = is.read();
                 }
                 is.close();
             }
 
             //apiM.addDatastream("doms:test1", "QualifiedDC", new String[0], "Qualified Dublin Core Record", true, "text/xml",
             //		 null, "http://www.fedora.info/junit/datastream1.xml", "M", "A", null, null, "test");
 
             String xmlAsString = "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" >";
             xmlAsString += "<dc:title>Example Simple Tape-Side Hey Joe</dc:title>";
             xmlAsString += "<dc:identifier>doms:example_Simple_side</dc:identifier>";
             xmlAsString += "</oai_dc:dc>";
             byte[] normalarr=xmlAsString.getBytes("UTF-8");
 
             apiM.modifyDatastreamByValue
                     ("doms:example_Simple_side",
                             "DC",
                             null,
                             "Dublin Core",
                             "text/xml",
                             null,
                             normalarr,
                             "DISABLED",
                             null,
                             "Lennert tester",
                             false);
 
         }
         catch(Exception ex)
         {
             logger.warn("Exception: " + ex.getMessage());
         }
 
         return pids;
 
     }
 
 
 
     public String catObject(String pid) throws  RemoteException {
         
 
         try {
             byte[] object = apiM.getObjectXML(pid);
             return new String(object,"UTF-8");
         }
         catch(RemoteException e){
             faultHandler(e);
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         return "";
     }
 
 
 
     public void faultHandler(Exception e) throws  RemoteException {
         
         throw new Error("Error",e);
 
     }
 
     private List<ObjectFields> extractSearchResults(FieldSearchResult resultOfPreviousSearch) throws  RemoteException {
         
         List<ObjectFields> pids = new ArrayList<ObjectFields>();
 
         ObjectFields[] objects = resultOfPreviousSearch.getResultList();
         for (ObjectFields object : objects){
             pids.add(object);
         }
 
         ListSession session = resultOfPreviousSearch.getListSession();
         if (session != null){
             String token = session.getToken();
             FieldSearchResult newResult = null;
             try {
                 newResult = apiA.resumeFindObjects(token);
             } catch (RemoteException e) {
                 throw new Error("Failed remote procedure call 'resumeFindObjects'",e);
             }
             List<ObjectFields> morePids = extractSearchResults(newResult);
             pids.addAll(morePids);
         }
 
         return pids;
     }
 
     private List<String> searchOnPIDs(FieldSearchResult resultOfPreviousSearch) throws  RemoteException {
         
         List<String> pids = new ArrayList<String>();
 
         ObjectFields[] objects = resultOfPreviousSearch.getResultList();
         for (ObjectFields object : objects){
             pids.add(object.getPid());
         }
 
         ListSession session = resultOfPreviousSearch.getListSession();
         if (session != null){
             String token = session.getToken();
             FieldSearchResult newResult = null;
             try {
                 newResult = apiA.resumeFindObjects(token);
             } catch (RemoteException e) {
                 throw new Error("Failed remote procedure call 'resumeFindObjects'",e);
             }
             List<String> morePids = searchOnPIDs(newResult);
             pids.addAll(morePids);
         }
 
         return pids;
     }
 
 
     private static final String RISEARCH = "/risearch?type=tuples&lang=iTQL&format=CSV&query=";
     private static final String QUERY =
             "select $object "
                     + "from <#ri> "
                     + "where $object <http://doms.statsbiblioteket.dk/relations/default/0/1/#isMainForNamedView> 'GUI' "
                     + "and $object <http://doms.statsbiblioteket.dk/relations/default/0/1/#isPartOfCollection> <info:fedora/";
 
 
     public DigitalObject[] getAllCollections() throws ServiceException, RemoteException {
         
         DomsResourceIndexService riWSLocator;
         DomsResourceIndex riWS;
         DigitalObject[] objects = null;
 
         riWSLocator = new DomsResourceIndexServiceLocator();
         riWS = riWSLocator.getDomsResourceIndex();
         objects = riWS.getAllCollections(Configuration.fedoraAdminUser, Configuration.fedoraAdminPass);
         return objects;
     }
 
     public void uploadFile(File file, org.apache.axis.types.URI objectPID) throws ServiceException, IOException {
         
         ObjectManipulationService omWSLocator;
         ObjectManipulation omWS;
         omWSLocator = new ObjectManipulationServiceLocator();
         omWS = omWSLocator.getDomsGUIObjectManipulation();
 
         //String checksum = Util.getChecksum(file);
         //omWS.addFileToObject("fedoraAdmin", "fedoraAdminPass", file.getPath(), checksum, objectPID );
         logger.info("uploadFile - Uploading file: "+file.getPath()+ " to dataobject: " + objectPID);
         omWS.addFileToObjectNoMD5(Configuration.fedoraAdminUser, Configuration.fedoraAdminPass, file.getPath(), objectPID);
     }
 
 
     public DigitalObject[] getTargetsForRelationFromPid(String owlTargetsPid) throws ServiceException, RemoteException {
         
         DomsResourceIndexService riWSLocator;
         DomsResourceIndex riWS;
         DigitalObject[] objects = null;
 
         riWSLocator = new DomsResourceIndexServiceLocator();
         riWS = riWSLocator.getDomsResourceIndex();
 
         objects = riWS.allObjectsForContentModel(owlTargetsPid, Configuration.fedoraAdminUser, Configuration.fedoraAdminPass);
 
         return objects;
     }
 
     public ArrayList<DigitalObject> getTemplates(String collectionPid) throws RemoteException, ServiceException {
         
         return getTemplates(collectionPid, null);
     }
 
 
     public ArrayList<DigitalObject> getTemplates(String collectionPid, String dataObjectPid) throws RemoteException, ServiceException {
         
         String plainCollectionPid;
         String uriCollectionPid;
         if (collectionPid.indexOf(Constants.INFO_FEDORA_URI_SCHEME)==0) {
             uriCollectionPid = collectionPid;
             plainCollectionPid = collectionPid.substring(Constants.INFO_FEDORA_URI_SCHEME.length());
         } else {
             uriCollectionPid = Constants.INFO_FEDORA_URI_SCHEME + collectionPid;
             plainCollectionPid = collectionPid;
         }
 
         String[] contentModelPids;
         if (dataObjectPid==null)
         {
             //Get the Content Models for the collection
             String response = null;
             try {
                 response = fc.getResponseAsString(
                         RISEARCH + URLEncoder.encode(QUERY+plainCollectionPid+">", "UTF-8"),
                         true,
                         true).trim();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             String[] results = response.split("\n");
             contentModelPids = new String[results.length-1];
             for (int i = 0; i < results.length; i++)
             {
                 if (i>0) {
                     String result = results[i];
                     contentModelPids[i-1] = result;
                 }
             }
         }
         else
         {
             contentModelPids = new String[1];
             if (collectionPid.indexOf(Constants.INFO_FEDORA_URI_SCHEME)==0) {
                 contentModelPids[0] = dataObjectPid;
             } else {
                 contentModelPids[0] = Constants.INFO_FEDORA_URI_SCHEME + dataObjectPid;
             }
         }
 
         //Get the templates for each content model.
         DomsResourceIndexService riWSLocator;
         DomsResourceIndex riWS;
         DigitalObject[] objects = null;
 
         riWSLocator = new DomsResourceIndexServiceLocator();
         riWS = riWSLocator.getDomsResourceIndex();
 
         ArrayList<DigitalObject> allObjects = new ArrayList<DigitalObject>();
         for (String pid : contentModelPids) {
             objects = riWS.allTemplatesInCollectionForContentModel(uriCollectionPid, pid, Configuration.fedoraAdminUser, Configuration.fedoraAdminPass);
             if (objects!=null) {
                 for (DigitalObject obj : objects) {
                     allObjects.add(obj);
                 }
             }
         }
 
         return allObjects;
     }
 
     /**
      * Creates a new dataobject from the given template
      * @param templatePid the pid of the template object
      * @return return the pid of the newly created object
      * @throws RemoteException
      * @throws ServiceException
      * @throws MalformedURIException
      */
     public String createDataObjectFromTemplate(org.apache.axis.types.URI templatePid)
             throws RemoteException, ServiceException, MalformedURIException {
         
         DomsObjectCreationService domsObjectCreateWSLocator;
         DomsObjectCreation domsObjectCreateWS;
 
         domsObjectCreateWSLocator = new DomsObjectCreationServiceLocator();
         domsObjectCreateWS = domsObjectCreateWSLocator.getDomsObjectCreation();
         logger.info("createDataObjectFromTemplate - creating object from templatePid: " + templatePid);
         org.apache.axis.types.URI result = domsObjectCreateWS.createObjectFromTemplate(templatePid, Configuration.fedoraAdminUser, Configuration.fedoraAdminPass);
 
         return result.toString();
     }
 
     /**
      * Fetches all the objects referencing the given dataobject. Sorted by relationname
      * @param pid the pid of the dataobject
      * @return list of dataobjets referencing the given dataobject
      * @throws ServiceException
      * @throws RemoteException
      */
     public List<ObjectRelation> getAllRerencesToObjectSorted(String pid) throws ServiceException, RemoteException, ServerOperationFailed {
         dk.statsbiblioteket.doms.client.objects.DigitalObject profile;
         List<ObjectRelation> relations = domsClient.getFactory().getDigitalObject(pid).getInverseRelations();
         return relations;
     }
 
     public List<Triple> getAllRerencesToObjectSortedAsTriple(String pid) throws ServerOperationFailed, RemoteException, ServiceException, MalformedURIException {
         List<ObjectRelation> rels = getAllRerencesToObjectSorted(pid);
         List<Triple> triples = new ArrayList<Triple>();
         for (ObjectRelation rel : rels) {
             DigitalObject obj = new DigitalObject(new URI(rel.getObject().getPid()), rel.getObject().getTitle());
             DigitalObject sub = new DigitalObject(new URI(rel.getSubject().getPid()), rel.getSubject().getTitle());
             triples.add(new Triple(obj, rel.getPredicate(), sub));
         }
         return triples;
     }
 
 
     private class TripleComparator implements Comparator<Triple> {
 
         @Override
         public int compare(Triple t1, Triple t2) {
             return t1.getRelation().compareTo(t2.getRelation());
         }
 
     }
 
     /**
      * Deletes the 'deleteobject' and all of its children.
      * Operation will not succeed if any object, that is not contained in 'recordRoot'
      * child hierarchy, has a relation to the objects that is being deleted
      * @param recordRoot the root dataobject of the record, which we are deleting an element from
      * @param deleteObject the object to delete, including all of its children
      * @throws ServiceException
      * @throws MyDeleteException
      * @throws IOException
      * @throws MyXMLParseException
      */
     public void deleteDomsDataObject(DOMSDataObject recordRoot, DOMSDataObject deleteObject) throws ServiceException, MyDeleteException, IOException, MyXMLParseException, MyXMLWriteException, MethodFailedException, InvalidResourceException {
         
         List<String> deleteList = getAllPids(deleteObject);
         List<DOMSDataObject> approvedList = new ArrayList<DOMSDataObject>();
         for(String pid : deleteList) {
             approvedList.addAll(dataObjectsRelatingToPid(pid, recordRoot));
         }
 
         //First find the objects that have some relation to one of the objects given and is not deleted
         DomsResourceIndexService riWSLocator;
         DomsResourceIndex riWS;
 
         riWSLocator = new DomsResourceIndexServiceLocator();
         riWS = riWSLocator.getDomsResourceIndex();
 
         String[] records = new String[deleteList.size()];
         deleteList.toArray(records);
 
         DigitalObject[] objects = riWS.allReferencingTheseObjects(records, Configuration.fedoraAdminUser, Configuration.fedoraAdminPass);
         //Check that all referencing objects are in the approvedlist
         if (objects==null) {
             logger.warn("deleteDomsDataObject - referencing objects was null");
         }
         else {
             for (DigitalObject obj: objects) {
                 String objPid = obj.getPid().toString();
                 if(objPid.startsWith(Constants.INFO_FEDORA_URI_SCHEME)) {
                     objPid = objPid.substring(Constants.INFO_FEDORA_URI_SCHEME.length());
                 }
                 boolean objectApproved = false;
                 for(DOMSDataObject valObject : approvedList) {
                     if(valObject.getPid().equals(objPid)) {
                         objectApproved = true;
                         break;
                     }
                 }
 
                 if(!objectApproved) {
                     logger.info("Delete failed - Approvedlist did not contain pid: " + obj.getPid().toString());
                     throw new MyDeleteException("Cannot delete element. The selected element is being referenced by an element from another record: "
                             + obj.getPid().toString());
                 }
             }
         }
         //Remove all references to the objects being deleted
         for(DOMSDataObject valObject : approvedList) {
             for(String keyPid : records) {
                 for(Relation childRel : valObject.getChildRelationships()) {
                     if(childRel.getPid().equals(keyPid)) {
                         logger.info("purging child relationship to object: " + keyPid);
                         this.purgeRelationship(valObject.getPid(), childRel.getObjProp().getMappingId(),
                                 keyPid, false, null);
                         valObject.removeChild(keyPid);
                     }
                 }
                 for(Relation childRel : valObject.getOtherPids()) {
                     if(childRel.getPid().equals(keyPid)) {
                         logger.info("purging other relationship to object: " + keyPid);
                         this.purgeRelationship(valObject.getPid(), childRel.getTypePid(), keyPid, false, null);
                     }
                 }
             }
             valObject.refreshRelations();
         }
 
         //Now delete the objects
         for(String rpid : records) {
             setDataObjectState(rpid, FedoraState.Deleted);
         }
 
     }
 
 
     /**
      * Fetches all the pids of the subtree of root
      * @param root the root of the dataobject tree to start in
      * @return list of pids
      */
     private List<String> getAllPids(DOMSDataObject root) throws  RemoteException {
         
         List<String> pids = new ArrayList<String>();
         if(root != null) {
             for(DOMSDataObject o : root.getChildren()) {
                 pids.addAll(getAllPids(o));
             }
             pids.add(root.getPid());
         }
         return pids;
     }
 
     /**
      * Fetches all pids of dataobjects that has a relation to the given pid.
      * Recursively goes through the dataobject tree
      * @param pid pid of the dataobject that the relation should point to
      * @param root dataobject to begin search from
      * @return
      */
     private List<DOMSDataObject> dataObjectsRelatingToPid(String pid, DOMSDataObject root) throws  RemoteException {
         
         List<DOMSDataObject> objects = new ArrayList<DOMSDataObject>();
         boolean addedRoot = false;
         if(!addedRoot) {
             for(Relation childRelation : root.getChildRelationships()) {
                 if(childRelation.getPid() == pid) {
                     objects.add(root);
                     addedRoot = true;
                     break;
                 }
             }
         }
         if(!addedRoot) {
             for(Relation otherRelation : root.getOtherPids()) {
                 if(otherRelation.getPid() == pid) {
                     objects.add(root);
                     break;
                 }
             }
         }
 
         for(DOMSDataObject d : root.getChildren()){
             objects.addAll(dataObjectsRelatingToPid(pid, d));
         }
         return objects;
 
     }
 
 
 
     public void setDataObjectState(String pid, FedoraState state) throws RemoteException {
         try {
             domsClient.getFactory().getDigitalObject(pid).setState(state);
         } catch (ServerOperationFailed serverOperationFailed) {
             serverOperationFailed.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         apiM.modifyObject(pid, state.toString(), null, null, "");
     }
 
     /**
      * Given a pid, this method returns the state of the object <code>Active, Inactive</code> or <code>Deleted</code>
      * @param pid A string
      * @return FedoraState enum describing the state of the object
      * @throws ServerOperationFailed
      */
     public FedoraState getDataObjectState(String pid) throws RemoteException, ServerOperationFailed {
         
 
        // brug domsClient mockup
         FedoraState state = domsClient.getFactory().getDigitalObject(pid).getState();
         return state;
     }
 
 
 
 
 }
