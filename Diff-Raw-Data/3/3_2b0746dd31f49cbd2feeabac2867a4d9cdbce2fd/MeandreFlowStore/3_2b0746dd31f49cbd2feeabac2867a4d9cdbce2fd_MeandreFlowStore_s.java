 package org.imirsel.nema.flowservice;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 
 import net.jcip.annotations.GuardedBy;
 
 import org.imirsel.meandre.client.MeandreClient;
 import org.imirsel.meandre.client.TransmissionException;
 import org.imirsel.nema.annotations.parser.beans.DataTypeBean;
 import org.imirsel.nema.annotations.parser.beans.StringDataTypeBean;
 import org.imirsel.nema.client.beans.converters.MeandreConverter;
 import org.imirsel.nema.client.beans.repository.WBFlowDescription;
 import org.imirsel.nema.model.Component;
 import org.imirsel.nema.model.NemaDataset;
 import org.imirsel.nema.model.Property;
 import org.imirsel.nema.renderers.CollectionRenderer;
 import org.imirsel.nema.repository.RepositoryClientConnectionPool;
 import org.imirsel.nema.repositoryservice.RepositoryClientInterface;
 import org.meandre.core.repository.ExecutableComponentDescription;
 import org.meandre.core.repository.ExecutableComponentInstanceDescription;
 import org.meandre.core.repository.FlowDescription;
 import org.meandre.core.repository.PropertiesDescriptionDefinition;
 import org.meandre.core.repository.QueryableRepository;
 import org.meandre.core.repository.RepositoryImpl;
 import org.meandre.core.store.Store;
 import org.springframework.core.io.DefaultResourceLoader;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DC;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import com.hp.hpl.jena.vocabulary.XSD;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 
 /**
  * Models the flow repository for a Meandre server.
  * 
  * @author shirk
  * @since 0.6.0
  */
 public class MeandreFlowStore {
 
    private static final Logger logger = Logger
    .getLogger(MeandreFlowStore.class.getName());
    
    private static final String DATATYPE_KEY = "DATATYPE";
    private final XStream xstream = new XStream(new JettisonMappedXmlDriver());
 
    private RepositoryClientConnectionPool repositoryClientConnectionPool = null;
    
    private MeandreClient meandreClient = null;
    
    /** Locally cached version of the remote flow repository */
    @GuardedBy("repositoryLock")
    private QueryableRepository cachedRemoteRepository;
    private final Lock repositoryLock = new ReentrantLock(true);
    
    private String repositoryLocation;
 
    @PostConstruct
    public void init() {
       DefaultResourceLoader drl = new DefaultResourceLoader();
       org.springframework.core.io.Resource resource = 
          drl.getResource("flowrepository.properties");
       if (resource != null) {
          Properties properties = new Properties();
          try {
             properties.load(resource.getInputStream());
             repositoryLocation = properties.getProperty(
                   "flowresource.location", "repository");
          } catch (IOException e) {
             logger
                   .warning("Property file 'flowrepository.properties' " +
                   		"was not found. Using the default properties...");
             repositoryLocation = "repository";
          }
       } else {
          logger
          .warning("Property file 'flowrepository.properties' " +
                "was not found. Using the default properties...");
          repositoryLocation = "repository";
       }
       File file = new File(repositoryLocation);
       if (!file.exists()) {
          boolean success = file.mkdirs();
          if (!success) {
             throw new RuntimeException(
                   "Could not create flow repository at location: "
                         + file.getAbsolutePath());
          }
       }
       
       xstream.setMode(XStream.NO_REFERENCES);
       
       try {
          cachedRemoteRepository = meandreClient.retrieveRepository();
       } catch (TransmissionException e) {
          throw new RuntimeException (
                "Could not initialize MeandreFlowStore...", e);
       }
    }
 
    /**
     * 
     * @return Map<String,FlowDescription> The Map of String and FlowDescription
     */
    public Map<String, FlowDescription> getAvailableFlowDescriptionsMap() {
       repositoryLock.lock();
       Map<String, FlowDescription> map = null;
       try {
          map = cachedRemoteRepository.getAvailableFlowDescriptionsMap();
       } catch (Exception ex) {
       } finally {
          repositoryLock.unlock();
       }
       return map;
    }
 
    /**
     * Flushes the cached repository.
     * 
     * @return success true/false
     * @throws MeandreServerException
     * 
     */
    protected boolean flushRepository() throws MeandreServerException {
       boolean success = false;
       cachedRemoteRepository = null;
       repositoryLock.lock();
       try {
          if (meandreClient == null) {
             return false;
          }
          cachedRemoteRepository = meandreClient.retrieveRepository();
          success = true;
       } catch (Exception e) {
          success = false;
          throw new MeandreServerException(e);
       } finally {
          repositoryLock.unlock();
       }
       return success;
    }
 
    /**
     * Returns the flows that reside at the server, as {@link Resource}s.
     * 
     * @return Set<Resource> Set of {@link Resource}s that are flows.
     */
    public Set<Resource> getAvailableFlows() {
       Set<Resource> resources = null;
       repositoryLock.lock();
       try {
          resources = cachedRemoteRepository.getAvailableFlows();
       } catch (Exception ex) {
       } finally {
          repositoryLock.unlock();
       }
       return resources;
    }
 
    public ExecutableComponentDescription getComponentDescription(
          Resource flowResource) {
       ExecutableComponentDescription ecd = null;
       repositoryLock.lock();
       try {
          ecd = cachedRemoteRepository.getExecutableComponentDescription(flowResource);
       } catch (Exception ex) {
       } finally {
          repositoryLock.unlock();
       }
       return ecd;
    }
 
    public Set<URI> getFlowUris() throws MeandreServerException {
       Set<URI> set = null;
       repositoryLock.lock();
       try {
          set = meandreClient.retrieveFlowUris();
       } catch (TransmissionException e) {
          throw new MeandreServerException(e);
       } finally {
          repositoryLock.unlock();
       }
 
       return set;
    }
 
    public ExecutableComponentDescription getComponentDescription(
          String componentUri) throws MeandreServerException {
 
       ExecutableComponentDescription componentDesc = null;
       repositoryLock.lock();
       try {
          componentDesc = meandreClient.retrieveComponentDescriptor(componentUri);
       } catch (TransmissionException e) {
          throw new MeandreServerException(e);
       } finally {
          repositoryLock.unlock();
       }
       return componentDesc;
    }
 
    public FlowDescription getFlowDescription(String flowUri)
          throws MeandreServerException {
       FlowDescription flowDesc = null;
       repositoryLock.lock();
       try {
          flowDesc = meandreClient.retrieveFlowDescriptor(flowUri);
       } catch (TransmissionException e) {
          throw new MeandreServerException(e);
       } finally {
          repositoryLock.unlock();
       }
       return flowDesc;
    }
 
    /**
     * Return the URIs of all the components in the Meandre server repository.
     * 
     * @return URIs of all the components in the Meandre server repository.
     * 
     * @throws MeandreServerException if a problem occurs while communicating
     *            with the remote Meandre server.
     */
    public Set<URI> getComponentUrisInRepository() throws MeandreServerException {
       Set<URI> componentUriSet = null;
       repositoryLock.lock();
       try {
          componentUriSet = meandreClient.retrieveComponentUris();
       } catch (TransmissionException e) {
          throw new MeandreServerException(e);
       } finally {
          repositoryLock.unlock();
       }
       return componentUriSet;
    }
 
    /**
     * For the given flow URI, return the list of {@link Component}s that make up
     * the flow.
     * 
     * @param flowUri URI of the flow to get the components for.
     * @return List of {@link Component}s that make up the flow.
     * @throws MeandreServerException if a problem occurs while attempting to get
     *            the flows from the remote Meandre server.
     */
    public List<Component> getComponents(String flowUri)
          throws MeandreServerException {
       Map<String, FlowDescription> map = null;
       map = getAvailableFlowDescriptionsMap();
       // Used later in case something goes wrong communicating with the server.
       String errMsg = "Could not find components for the flow with URI: "
             + flowUri;
       if (map == null) {
          logger.severe(errMsg);
          throw new MeandreServerException(errMsg);
       }
       FlowDescription flowDescription = map.get(flowUri);
       if (flowDescription == null) {
          logger.severe(errMsg);
          throw new MeandreServerException(errMsg);
       }
       Set<ExecutableComponentInstanceDescription> componentDescriptions = flowDescription
             .getExecutableComponentInstances();
       ArrayList<Component> list = new ArrayList<Component>(
             componentDescriptions.size());
       Iterator<ExecutableComponentInstanceDescription> descriptionsIterator = componentDescriptions
             .iterator();
       while (descriptionsIterator.hasNext()) {
          ExecutableComponentInstanceDescription description = descriptionsIterator
                .next();
          String uri = description.getExecutableComponent().getURI();
          String instanceUri = description.getExecutableComponentInstance()
                .getURI();
          Component component = new Component();
          component.setUri(uri);
          component.setInstanceUri(instanceUri);
          component.setName(description.getName());
          component.setDescription(description.getDescription());
          component.setHidden(isHiddenComponentForFlow(flowUri, instanceUri));
          list.add(component);
       }
       return list;
    }
 
    private boolean isHiddenComponentForFlow(String flowUri,
          String componentInstanceUri) {
       if (componentInstanceUri.indexOf("fork") != -1
             || componentInstanceUri.indexOf("printobject") != -1
             || componentInstanceUri.indexOf("runbinary") != -1
             || componentInstanceUri.indexOf("StructuralSegmentationEvaluator"
                   .toLowerCase()) != -1
             || componentInstanceUri
                   .indexOf("TrainTestGateKeeper".toLowerCase()) != -1
             || componentInstanceUri.indexOf("ClassificationEvaluator"
                   .toLowerCase()) != -1
             || componentInstanceUri
                   .indexOf("ClassificationEvaluationsAggregator".toLowerCase()) != -1) {
          return true;
       }
       return false;
    }
 
    /**
     * Create a new flow and save it in the local repository. The the supplied
     * parameter map containing custom properties that a user has set based on a
     * template flow and creates a new flow.
     * 
     * @returns URI of the new flow.
     */
    public synchronized String createFlow(HashMap<String, String> paramMap,
          String flowUri, long userId) throws MeandreServerException {
       WBFlowDescription flowDesc = MeandreConverter.FlowDescriptionConverter
             .convert(getFlowDescription(flowUri));
       String name = flowDesc.getName();
       name = name + System.currentTimeMillis();
       flowDesc.setName(name);
       flowDesc.setDescription("Derived from " + flowUri);
       flowDesc.setRights("owned by user");
       flowDesc.setCreationDate(new Date());
       flowDesc.updateParameters(flowUri, paramMap);
       String fileLocation = saveFlow(flowDesc, userId);
       logger.info("Saved new flow to the following location:  " + fileLocation);
       return fileLocation;
    }
 
    /**
     * This method removes a flow from meandre
     * 
     * @throws MeandreServerException
     * 
     * @returns success
     */
    public boolean removeFlow(String uri) throws MeandreServerException {
       boolean success = false;
       try {
          success = meandreClient.removeResource(uri);
       } catch (TransmissionException e) {
          throw new MeandreServerException("Could not remove" + uri + "--"
                + e.getMessage());
       }
       flushRepository();
       return success;
    }
 
    public Map<String, Property> getComponentPropertyDataType(
          Component component, String flowUri) throws 
        MeandreServerException {
 
       Model model = getEmptyModel();
       ExecutableComponentDescription ecd = getComponentDescription(model
             .createResource(component.getUri()));
       FlowDescription fd = getFlowDescription(flowUri);
 
       if (ecd == null) {
          logger.severe("component: " + component.getUri()
                + " could not be found.");
 
          throw new MeandreServerException("component: " + component.getUri()
                + " could not be found.");
       }
       Model m = this.getEmptyModel();
       ExecutableComponentInstanceDescription ecid = fd
             .getExecutableComponentInstanceDescription(m
                   .createResource(component.getInstanceUri()));
       if (ecid == null) {
          logger.severe("component instance: " + component.getInstanceUri()
                + " could not be found.");
 
          throw new MeandreServerException("component instance : "
                + component.getInstanceUri() + " could not be found.");
       }
       PropertiesDescriptionDefinition propertiesDefn = ecd.getProperties();
       Set<String> propertiesSet = propertiesDefn.getKeys();
       Iterator<String> it = propertiesSet.iterator();
       Map<String, Property> dataTypeMap = new HashMap<String, Property>();
       boolean foundDataType = Boolean.FALSE;
       while (it.hasNext()) {
          String propertyName = it.next();
          Property property = new Property();
          property.setName(propertyName);
 
          Map<String, String> otherPropertyMap = propertiesDefn
                .getOtherProperties(propertyName);
          String description = propertiesDefn.getDescription(propertyName);
          String defaultValue = propertiesDefn.getValue(propertyName);
          String value = ecid.getProperties().getValue(propertyName);
          if (value != null) {
             property.setValue(value);
          } else {
             property.setValue(defaultValue);
          }
          property.setDefaultValue(defaultValue);
          property.setDescription(description);
          List<DataTypeBean> dataTypes = null;
          if (!otherPropertyMap.isEmpty()) {
             Iterator<Entry<String, String>> it1 = otherPropertyMap.entrySet()
                   .iterator();
             while (it1.hasNext()) {
                Entry<String, String> tmp = it1.next();
                String key = tmp.getKey();
                String value1 = tmp.getValue();
                if (key.endsWith(propertyName + DATATYPE_KEY)) {
                   dataTypes = getDataTypeBeanFromJson(value1);
                   updatePropertyWithCollectionMetadata(property, dataTypes);
                   property.setDataTypeBeanList(dataTypes);
                   foundDataType = true;
                }
             }
             // add the default data type
             if (!foundDataType) {
                dataTypes = getDefaultDataTypeBean();
                property.setDataTypeBeanList(dataTypes);
             }
          } else {
             if (!foundDataType) {
                dataTypes = getDefaultDataTypeBean();
                property.setDataTypeBeanList(dataTypes);
             }
          }
 
          dataTypeMap.put(propertyName, property);
          // reset to false for the next property
          foundDataType = Boolean.FALSE;
       }
       return dataTypeMap;
    }
 
    private void updatePropertyWithCollectionMetadata(Property property,
          List<DataTypeBean> dataTypes) {
       for (DataTypeBean dtb : dataTypes) {
          if (dtb.getRenderer().equals(CollectionRenderer.class.getName())) {
             // this is a collection
             ArrayList<String> labelList = new ArrayList<String>();
             ArrayList<Object> valueList = new ArrayList<Object>();
             RepositoryClientInterface rpi = repositoryClientConnectionPool
                   .getFromPool();
             try {
                List<NemaDataset> ltb = rpi.getDatasets();
                for (NemaDataset dataset : ltb) {
                   String label = dataset.getName();
                   int value = dataset.getId();
                   labelList.add(label);
                   valueList.add(value);
                }
             } catch (SQLException e) {
                e.printStackTrace();
             } finally {
                repositoryClientConnectionPool.returnToPool(rpi);
             }
 
             if (labelList.size() > 0) {
                property.setEnumeratedValueList(labelList);
                property.setEnumneratedLabelList(valueList);
             }
          }
       }
    }
 
    private Model getEmptyModel() {
       Model model = ModelFactory.createDefaultModel();
       model.setNsPrefix("meandre", Store.MEANDRE_ONTOLOGY_BASE_URL);
       model.setNsPrefix("xsd", XSD.getURI());
       model.setNsPrefix("rdf", RDF.getURI());
       model.setNsPrefix("rdfs", RDFS.getURI());
       model.setNsPrefix("dc", DC.getURI());
       return model;
    }
 
    private List<DataTypeBean> getDefaultDataTypeBean() {
       List<DataTypeBean> list = new ArrayList<DataTypeBean>();
       list.add(new StringDataTypeBean());
       return list;
    }
 
    @SuppressWarnings("unchecked")
    private List<DataTypeBean> getDataTypeBeanFromJson(String value) {
       List<DataTypeBean> list = (List<DataTypeBean>) xstream.fromXML(value);
       return list;
    }
 
    /**
     * Returns the file path to the new flow
     * 
     * @param wbFlow
     * @param userId
     * @return File Path to the new flow
     * @throws MeandreServerException
     * @throws CorruptedFlowException
     */
    public String saveFlow(WBFlowDescription wbFlow, long userId)
          throws MeandreServerException {
       FlowDescription flow = MeandreConverter.WBFlowDescriptionConverter
             .convert(wbFlow);
       String flowUri = flow.getFlowComponent().getURI();
       logger.info("Saving flow " + flowUri);
 
       String execStepMsg = "";
       String fileName = null;
       FileOutputStream ttlStream = null, ntStream = null, rdfStream = null;
       try {
          Model flowModel = flow.getModel();
 
          String fName = flowUri.replaceAll(":|/", "_");
          String userRepoDir = repositoryLocation + File.separator
                + "x" + userId;
          File file = new File(userRepoDir);
          if (!file.exists()) {
             boolean success = file.mkdir();
             if (!success) {
                throw new RuntimeException(
                      "Error could not create user directory "
                            + file.getAbsolutePath());
             }
          }
 
         fileName = userRepoDir + System.getProperty("file.separator") + fName
               + ".nt";
          logger.info(fileName);
          ntStream = new FileOutputStream(userRepoDir + fName + ".nt");
          flowModel.write(ntStream, "N-TRIPLE");
          ttlStream = new FileOutputStream(userRepoDir + fName + ".ttl");
          flowModel.write(ttlStream, "TTL");
          rdfStream = new FileOutputStream(userRepoDir + fName + ".rdf");
          flowModel.write(rdfStream, "RDF/XML-ABBREV");
 
          execStepMsg = "STEP1: Creating RepositoryImpl from flow model";
          RepositoryImpl repository = new RepositoryImpl(flowModel);
          execStepMsg = "STEP2: Retrieving available flows";
          Set<FlowDescription> flows = repository.getAvailableFlowDescriptions();
          execStepMsg = "STEP3: Getting flow";
          flow = flows.iterator().next();
          if (flow == null) {
             throw new MeandreServerException("The flow obtained is null!");
          }
       } catch (Exception e) {
          MeandreServerException mException = (execStepMsg != null) ? new MeandreServerException(
                execStepMsg, e)
                : (MeandreServerException) e;
          throw mException;
       } finally {
 
          try {
             if (ttlStream != null)
                ttlStream.close();
          } catch (IOException e) {
             e.printStackTrace();
          }
          try {
             if (ntStream != null)
                ntStream.close();
          } catch (IOException e) {
             e.printStackTrace();
          }
          try {
             if (rdfStream != null)
                rdfStream.close();
          } catch (IOException e) {
             e.printStackTrace();
          }
       }
       return fileName;
    }
    
    public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
       return repositoryClientConnectionPool;
    }
 
 
    public void setRepositoryClientConnectionPool(
          RepositoryClientConnectionPool repositoryClientConnectionPool) {
       this.repositoryClientConnectionPool = repositoryClientConnectionPool;
    }
    
    public MeandreClient getMeandreClient() {
       return meandreClient;
    }
 
 
    /**
     * Set the Meandre client reference to use to communicate with the remote
     * Meandre server. Must be a pre-initialized reference to a Meandre server.
     * @param meandreClient
     */
    public void setMeandreClient(MeandreClient meandreClient) {
       this.meandreClient = meandreClient;
    }
 }
