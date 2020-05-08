 /**
  * 
  */
 package org.purl.wf4ever.wf2ro;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 import org.purl.wf4ever.rosrs.client.common.ROSRSException;
 import org.purl.wf4ever.wfdesc.scufl2.ROEvoSerializer;
 
 import uk.org.taverna.scufl2.api.annotation.Annotation;
 import uk.org.taverna.scufl2.api.common.NamedSet;
 import uk.org.taverna.scufl2.api.common.URITools;
 import uk.org.taverna.scufl2.api.container.WorkflowBundle;
 import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
 import uk.org.taverna.scufl2.api.io.WriterException;
 import uk.org.taverna.scufl2.rdfxml.RDFXMLReader;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 /**
  * This class defines the main logic of workflow-RO conversion. It is abstract because it leaves the resource upload
  * details to its subclasses.
  * 
  * @author piotrekhol
  * 
  */
 public abstract class Wf2ROConverter {
 
     /** text/turtle. */
     private static final String TEXT_TURTLE = "text/turtle";
 
     /** Wfdesc mime type. */
     private static final String TEXT_VND_WF4EVER_WFDESC_TURTLE = "text/vnd.wf4ever.wfdesc+turtle";
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(Wf2ROConverter.class);
 
     /** Workflow bundle serializer/deserializer. */
     private static WorkflowBundleIO bundleIO = new WorkflowBundleIO();
 
     /** Scufl2 URI helpers. */
     private static URITools uriTools = new URITools();
 
     /** Resources added so far. */
     private List<URI> resourcesAdded = Collections.synchronizedList(new ArrayList<URI>());
 
     /** Workflow bundle. */
     private WorkflowBundle wfbundle;
 
     /** Used to guarantee that the conversion is run only once. */
     private Boolean running = false;
 
     /** Filename with folder configuration. */
     private String foldersPropertiesFilename;
 
     /** Folder in which the workflow bundle will be stored. */
     private URI workflowBundleFolder;
 
     /** The original workflow URI. */
     protected URI originalWfUri;
 
 
     /**
      * The constructor.
      * 
      * @param wfbundle
      *            The t2flow/scufl2 workflow that needs to be converted to an RO.
      * @param wfUri
      *            workflow URI
      * @param foldersPropertiesFilename
      *            filename with folder configuration
      */
     public Wf2ROConverter(WorkflowBundle wfbundle, URI wfUri, String foldersPropertiesFilename) {
         this.wfbundle = wfbundle;
         this.originalWfUri = wfUri;
         this.foldersPropertiesFilename = foldersPropertiesFilename;
     }
 
 
     /**
      * The conversion method. Note that there are no ROSRS parameters, since all ROSRS handling is delegated to abstract
      * methods of this class.
      * 
      * This method can be called only once per class instance. Subsequent calls will result in IllegalStateException
      * being thrown.
      */
     public void convert() {
         synchronized (running) {
             if (running) {
                 throw new IllegalStateException(
                         "This instance can only be run once. Create another instance for another conversion.");
             }
             running = true;
         }
         UUID wfUUID = getWorkflowBundleUUID(wfbundle);
         String wfname = wfbundle.getMainWorkflow().getName() + ".wfbundle";
         URI roURI = null;
         try {
             roURI = createResearchObject(wfUUID);
         } catch (ROSRSException e) {
             LOG.error("Can't create RO", e);
             return;
         }
         try {
             createFolders(roURI, resourcesAdded, foldersPropertiesFilename);
         } catch (IOException | ROSRSException | ConfigurationException e) {
             LOG.error("Can't create folders", e);
         }
         URI wfbundleUri;
         try {
             String wfpath = roURI.relativize(workflowBundleFolder.resolve(wfname)).getPath();
             wfbundleUri = addWorkflowBundle(roURI, wfbundle, wfpath);
             if (workflowBundleFolder != null) {
                 addFolderEntry(workflowBundleFolder, wfbundleUri, wfname);
             }
             resourcesAdded.add(wfbundleUri);
         } catch (IOException | ROSRSException | WriterException e) {
             LOG.error("Can't upload workflow bundle", e);
             return;
         }
         try {
             extractAnnotations(roURI, wfbundleUri, wfbundle, resourcesAdded);
         } catch (IOException | ROSRSException e) {
             LOG.error("Can't extract annotations from workflow", e);
         }
 
         URI wfdescURI = null;
         try {
             wfdescURI = addWfDescAnnotation(roURI, wfbundle, wfbundleUri);
             resourcesAdded.add(wfdescURI);
         } catch (IOException | ROSRSException e) {
             LOG.error("Can't upload workflow desc", e);
         }
         try {
             resourcesAdded.add(addRoEvoAnnotation(roURI, wfbundle, wfbundleUri));
         } catch (IOException | ROSRSException e) {
             LOG.error("Can't upload RO evolution desc", e);
         }
         try {
             URI internalWorkflowURI = uriTools.uriForBean(wfbundle.getMainWorkflow());
             resourcesAdded.add(addLinkAnnotation(roURI, originalWfUri, wfbundleUri, internalWorkflowURI));
         } catch (ROSRSException e) {
             LOG.error("Can't upload the link annotation", e);
         }
 
     }
 
 
     /**
      * Read a folder structure from the properties file and create it in the RO.
      * 
      * @param roURI
      *            RO URI
      * @param resourcesAdded2
      *            list to which add created folders
      * @param foldersPropertiesFilename
      *            filename of properties file
      * @throws ConfigurationException
      *             could not read the properties file
      * @throws IOException
      *             ?
      * @throws ROSRSException
      *             error communicating with ROSRS
      */
     private void createFolders(URI roURI, List<URI> resourcesAdded2, String foldersPropertiesFilename)
             throws ConfigurationException, IOException, ROSRSException {
         PropertiesConfiguration props = new PropertiesConfiguration(foldersPropertiesFilename);
         List<Object> folders = props.getList("folder");
         if (folders == null) {
             return;
         }
         Map<String, URI> folderURIs = new HashMap<>();
         for (Object o : folders) {
             String path = o.toString();
             URI folder = createFolder(roURI, path);
             resourcesAdded2.add(folder);
             folderURIs.put(path, folder);
         }
         for (Entry<String, URI> e : folderURIs.entrySet()) {
             String folderPath = e.getKey();
             URI folderURI = e.getValue();
             List<Object> entries = props.getList("entries." + folderPath);
             if (entries == null) {
                 continue;
             }
             for (Object o : entries) {
                 String proxyForPath = o.toString();
                 URI proxyFor;
                 try {
                     proxyFor = roURI.resolve(proxyForPath);
                 } catch (IllegalArgumentException ex) {
                     LOG.debug(proxyForPath + " is not a valid URI");
                     proxyFor = UriBuilder.fromUri(roURI).path(proxyForPath).build();
                 }
                 String name = props.getString("name." + proxyForPath);
                 addFolderEntry(folderURI, proxyFor, name);
             }
         }
         String wfbundleFolder = props.getString("folder.wfbundle");
         if (wfbundleFolder == null || !folderURIs.containsKey(wfbundleFolder)) {
             throw new ConfigurationException("Incorrect workflow bundle folder: " + wfbundleFolder);
         } else {
             workflowBundleFolder = folderURIs.get(wfbundleFolder);
         }
     }
 
 
     /**
      * Create a folder in the RO.
      * 
      * @param roURI
      *            RO URI
      * @param path
      *            folder path
      * @return folder URI
      * @throws IOException
      *             ?
      * @throws ROSRSException
      *             incorrect response from ROSRS
      */
     protected abstract URI createFolder(URI roURI, String path)
             throws IOException, ROSRSException;
 
 
     /**
      * Add a resource to the folder.
      * 
      * @param folder
      *            folder
      * @param proxyFor
      *            resource
      * @param name
      *            name of the resource in the folder, optional
      * @return folder entry URI
      * @throws IOException
      *             ?
      * @throws ROSRSException
      *             incorrect response from ROSRS
      */
     protected abstract URI addFolderEntry(URI folder, URI proxyFor, String name)
             throws IOException, ROSRSException;
 
 
     /**
      * Extract workflow UUID.
      * 
      * @param wfbundle
      *            workflow bundle
      * @return workflow UUID
      */
     private UUID getWorkflowBundleUUID(WorkflowBundle wfbundle) {
         URI wfbundleURI = wfbundle.getGlobalBaseURI();
         return UUID.fromString(wfbundleURI.resolve("..").relativize(wfbundleURI).toString().split("/")[0]);
     }
 
 
     /**
      * Upload the workflow bundle and the resources it contains to RODL.
      * 
      * @param roURI
      *            research object URI
      * @param wfbundle
      *            the workflow bundle
      * @param wfPath
      *            workflow bundle path
      * @return the workflow bundle URI as in the manifest or null if uploading failed
      * @throws IOException
      *             when there was a problem with getting/uploading the RO resources
      * @throws ROSRSException
      *             ROSR service error
      * @throws WriterException
      *             workflow bundle error
      */
     protected URI addWorkflowBundle(URI roURI, final WorkflowBundle wfbundle, String wfPath)
             throws IOException, ROSRSException, WriterException {
         //save the scufl2
         final PipedInputStream in = new PipedInputStream();
         final PipedOutputStream out = new PipedOutputStream(in);
         new Thread(new Runnable() {
 
             public void run() {
                 Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                 try {
                     bundleIO.writeBundle(wfbundle, out, RDFXMLReader.APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE);
                 } catch (WriterException | IOException e) {
                     LOG.error("Can't download workflow bundle", e);
                 } finally {
                     try {
                         out.close();
                     } catch (IOException e) {
                         LOG.warn("Exception when closing the workflow bundle output stream", e);
                     }
                 }
             }
         }).start();
         uploadAggregatedResource(roURI, wfPath, in, RDFXMLReader.APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE);
 
         return roURI.resolve(wfPath);
     }
 
 
     /**
      * Extract annotations from workflow bundle and upload them as RO annotations. Annotation bodies are copied as RO
      * annotation bodies, annotation targets are referenced.
      * 
      * Update: only annotations of the workflow bundle or the main workflow are uploaded, with the workflow bundle as
      * their target. Others are skipped because they point to resources that are not aggregated.
      * 
      * @param roURI
      *            RO URI
      * @param wfURI
      *            workflow bundle ROSRS URI
      * @param wfbundle
      *            workflow bundle
      * @param resourcesAdded2
      *            list of resources to which the annotations URIs will be added
      * @throws IOException
      *             cannot read the annotation body
      * @throws ROSRSException
      *             ROSR service error
      */
     private void extractAnnotations(URI roURI, URI wfURI, final WorkflowBundle wfbundle, List<URI> resourcesAdded2)
             throws IOException, ROSRSException {
         //search for annotations
         URITools tools = new URITools();
         NamedSet<Annotation> annotations = wfbundle.getAnnotations();
         for (Annotation annotation : annotations) {
            InputStream annBody = wfbundle.getResources()
                    .getResourceAsInputStream(annotation.getBody().toASCIIString());
             if (annotation.getTarget().equals(wfbundle) || annotation.getTarget().equals(wfbundle.getMainWorkflow())) {
                 URI target = wfURI;
                 LOG.debug(String.format("Uploading annotation for %s taken from %s", target, annotation.getBody()));
                resourcesAdded2
                        .add(uploadAnnotation(roURI, "wf", Arrays.asList(target), annBody, "application/rdf+xml"));
             } else {
                 URI target = tools.uriForBean(annotation.getTarget());
                 LOG.debug(String.format("Skipping annotation for %s taken from %s", target, annotation.getBody()));
             }
         }
     }
 
 
     /**
      * Generates and adds a workflow bundle history annotation using the roevo ontology.
      * 
      * @param roURI
      *            research object URI
      * @param wfbundle
      *            the workflow bundle
      * @param rodlWfURI
      *            the workflow bundle URI used in the research object
      * @return the annotation body URI
      * @throws IOException
      *             when there was a problem with getting/uploading the RO resources
      * @throws ROSRSException
      *             ROSR service error
      */
     protected URI addRoEvoAnnotation(URI roURI, final WorkflowBundle wfbundle, URI rodlWfURI)
             throws IOException, ROSRSException {
         final PipedInputStream in = new PipedInputStream();
         final PipedOutputStream out = new PipedOutputStream(in);
         new Thread(new Runnable() {
 
             public void run() {
                 ROEvoSerializer roEvo = new ROEvoSerializer();
                 try {
                     roEvo.workflowHistory(wfbundle.getMainWorkflow(), out);
                 } catch (WriterException e) {
                     LOG.error("Can't download workflow desc", e);
                 } finally {
                     try {
                         out.close();
                     } catch (IOException e) {
                         LOG.warn("Exception when closing the annotation body output stream", e);
                     }
                 }
             }
         }).start();
         return uploadAnnotation(roURI, "roevo", Arrays.asList(rodlWfURI), in, TEXT_TURTLE);
     }
 
 
     /**
      * Generates and adds a workflow bundle description annotation using the wfdesc ontology.
      * 
      * @param roURI
      *            research object URI
      * @param wfbundle
      *            the workflow bundle
      * @param rodlWfURI
      *            the workflow bundle URI used in the research object
      * @return the annotation body URI
      * @throws IOException
      *             when there was a problem with getting/uploading the RO resources
      * @throws ROSRSException
      *             when there was a problem with getting/uploading the RO resources
      */
     protected URI addWfDescAnnotation(URI roURI, final WorkflowBundle wfbundle, URI rodlWfURI)
             throws IOException, ROSRSException {
         final PipedInputStream in = new PipedInputStream();
         final PipedOutputStream out = new PipedOutputStream(in);
         new Thread(new Runnable() {
 
             public void run() {
                 try {
                     bundleIO.writeBundle(wfbundle, out, TEXT_VND_WF4EVER_WFDESC_TURTLE);
                 } catch (WriterException | IOException e) {
                     LOG.error("Can't download workflow desc", e);
                 } finally {
                     try {
                         out.close();
                     } catch (IOException e) {
                         LOG.warn("Exception when closing the annotation body output stream", e);
                     }
                 }
             }
         }).start();
         return uploadAnnotation(roURI, "wfdesc", Arrays.asList(rodlWfURI), in, TEXT_TURTLE);
     }
 
 
     /**
      * Generates and adds an annotation linking the aggregated workflow bundle (ROSRS URI) with its main workflow
      * (internal URI).
      * 
      * @param roURI
      *            research object URI
      * @param originalWfUri
      *            original workflow Uri
      * @param wfUri
      *            the workflow bundle URI
      * @param workflowIdentifier
      *            the workflow internal URI
      * @return the annotation body URI
      * @throws ROSRSException
      *             when there was a problem with getting/uploading the RO resources
      */
     protected URI addLinkAnnotation(URI roURI, URI originalWfUri, URI wfUri, URI workflowIdentifier)
             throws ROSRSException {
         Model model = ModelFactory.createDefaultModel();
         Resource originalR = model.createResource(originalWfUri.toString());
         Resource wfbundleR = model.createResource(wfUri.toString());
         Resource mainwfR = model.createResource(workflowIdentifier.toString());
         //FIXME move that property to rodl-common
         Property link = model.createProperty("http://purl.org/wf4ever/wfdesc#hasWorkflowDefinition");
         model.add(mainwfR, link, wfbundleR);
         Property link2 = model.createProperty("http://purl.org/pav/importedFrom");
         model.add(wfbundleR, link2, originalR);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         model.write(out, "TURTLE");
         ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
 
         return uploadAnnotation(roURI, "link", Arrays.asList(wfUri), in, RDFFormat.TURTLE.getDefaultMIMEType());
     }
 
 
     /**
      * Search for external resources in the wfdesc description and aggregate them in the RO.
      * 
      * @param researchObject
      *            research object URI
      * @param wfdescURI
      *            workflow description URI
      * @throws ROSRSException
      *             when there was a problem with uploading the RO resources
      */
     @SuppressWarnings("unused")
     private void aggregateWorkflowDependencies(URI researchObject, URI wfdescURI)
             throws ROSRSException {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         readModelFromUri(model, wfdescURI);
         Property wsdlUri = model.createProperty("http://purl.org/wf4ever/wf4ever#wsdlURI");
         List<RDFNode> wsdls = model.listObjectsOfProperty(wsdlUri).toList();
         for (RDFNode wsdl : wsdls) {
             if (wsdl.isLiteral()) {
                 if ("http://www.w3.org/2001/XMLSchema#anyURI".equals(wsdl.asLiteral().getDatatypeURI())) {
                     URI wsdlURI = URI.create(wsdl.asLiteral().getString());
                     aggregateResource(researchObject, wsdlURI);
                     resourcesAdded.add(wsdlURI);
                 } else {
                     LOG.error("The WSDL URI was not a Literal, skipping: " + wsdl.toString());
                 }
             } else {
                 LOG.error("The WSDL URI was not a Literal, skipping: " + wsdl.toString());
             }
         }
     }
 
 
     /**
      * Read a model given a URI. RDF/XML is assumed.
      * 
      * @param model
      *            Ont model
      * @param wfdescURI
      *            RDF/XML source URI
      */
     public abstract void readModelFromUri(OntModel model, URI wfdescURI);
 
 
     /**
      * Create a new research object or, if it exists, prepare it for uploading workflows, i.e. preserve the previous
      * workflow versions.
      * 
      * @param wfUUID
      *            UUID of the workflow bundle that will be uploaded later.
      * 
      * @return the research object URI
      * @throws ROSRSException
      *             ROSR service error
      */
     protected abstract URI createResearchObject(UUID wfUUID)
             throws ROSRSException;
 
 
     /**
      * Saves an aggregated resource to RODL.
      * 
      * @param researchObject
      *            research object URI
      * @param path
      *            the resource path
      * @param contentType
      *            resource content type to be sent as in HTTP request
      * @param in
      *            resource input stream
      * @throws IOException
      *             when there are problems with uploading the resource
      * @throws ROSRSException
      *             ROSR service error
      */
     protected abstract void uploadAggregatedResource(URI researchObject, String path, InputStream in, String contentType)
             throws IOException, ROSRSException;
 
 
     /**
      * Saves an URI as an aggregated resource of an RO in RODL.
      * 
      * @param researchObject
      *            research object URI
      * @param resource
      *            resource URI
      * @throws ROSRSException
      *             when there are problems with uploading the resource
      * @throws ROSRSException
      */
     protected abstract void aggregateResource(URI researchObject, URI resource)
             throws ROSRSException;
 
 
     /**
      * Saves a resource in RODL as an annotation body of another resource.
      * 
      * @param researchObject
      *            research object URI
      * @param name
      *            annotation name to use in filename
      * @param targets
      *            list of URIs of resources that are annotated
      * @param contentType
      *            content type
      * @param in
      *            resource input stream
      * @return annotation body URI
      * @throws ROSRSException
      *             when there are problems with uploading the resource
      * @throws IOException
      */
     protected abstract URI uploadAnnotation(URI researchObject, String name, List<URI> targets, InputStream in,
             String contentType)
             throws ROSRSException;
 
 
     /**
      * Create Jena model of the manifest.
      * 
      * @param roURI
      *            research object URI
      * @return the Jena model of the manifest
      */
     protected abstract OntModel createManifestModel(URI roURI);
 
 
     /**
      * Return the list of resources that have already been added as a result of the conversion.
      * 
      * @return the list of resource URIs
      */
     public List<URI> getResourcesAdded() {
         return resourcesAdded;
     }
 
 
     public String getFoldersPropertiesFilename() {
         return foldersPropertiesFilename;
     }
 
 
     public void setFoldersPropertiesFilename(String foldersPropertiesFilename) {
         this.foldersPropertiesFilename = foldersPropertiesFilename;
     }
 
 }
