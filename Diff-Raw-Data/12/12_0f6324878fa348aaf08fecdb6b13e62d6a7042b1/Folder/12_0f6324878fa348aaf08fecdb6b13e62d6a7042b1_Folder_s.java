 package pl.psnc.dl.wf4ever.model.RO;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.EvoBuilder;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.ORE.Aggregation;
 import pl.psnc.dl.wf4ever.model.ORE.Proxy;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 
 /**
  * ro:Folder.
  * 
  * @author piotrekhol
  * 
  */
 public class Folder extends Resource implements Aggregation {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(Folder.class);
 
     /** folder entries. */
     private Map<URI, FolderEntry> folderEntries;
 
     /** aggregated resources. */
     private Map<URI, AggregatedResource> aggregatedResources;
 
     /** Resource map (graph with folder description) URI. */
     private FolderResourceMap resourceMap;
 
     /** is the folder a root folder in the RO. */
     private boolean rootFolder;
 
     /** resource map URI. */
     private final URI resourceMapUri;
 
 
     /**
      * Constructor.
      * 
      * @param user
      *            user creating the instance
      * @param dataset
      *            custom dataset
      * @param useTransactions
      *            should transactions be used. Note that not using transactions on a dataset which already uses
      *            transactions may make it unreadable.
      * @param researchObject
      *            The RO it is aggregated by
      * @param uri
      *            resource URI
      * @param resourceMapUri2
      *            folder resource map URI, or null for default
      */
     public Folder(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject, URI uri,
             URI resourceMapUri2) {
         super(user, dataset, useTransactions, researchObject, uri);
         this.resourceMapUri = resourceMapUri2 != null ? resourceMapUri2 : FolderResourceMap
                 .generateResourceMapUri(this);
     }
 
 
     /**
      * Get the folder entries. May be loaded lazily.
      * 
      * @return a set of folder entries in this folder
      */
     public Map<URI, FolderEntry> getFolderEntries() {
         if (folderEntries == null) {
             folderEntries = getResourceMap().extractFolderEntries();
         }
         return folderEntries;
     }
 
 
     /**
      * Get resources aggregated in the folder.
      * 
      * @return resources aggregated in the folder
      */
     @Override
     public Map<URI, AggregatedResource> getAggregatedResources() {
         if (aggregatedResources == null) {
             aggregatedResources = new HashMap<>();
             for (FolderEntry entry : getFolderEntries().values()) {
                 if (entry.getProxyFor() != null) {
                     aggregatedResources.put(entry.getProxyFor().getUri(), entry.getProxyFor());
                 } else {
                     LOGGER.warn("Folder entry " + entry + " has no proxy for");
                 }
             }
         }
         return aggregatedResources;
     }
 
 
     public void setFolderEntries(Map<URI, FolderEntry> folderEntries) {
         this.folderEntries = folderEntries;
     }
 
 
     /**
      * Get the resource map.
      * 
      * @return a folder resource map
      */
     @Override
     public FolderResourceMap getResourceMap() {
         if (resourceMap == null) {
             resourceMap = builder.buildFolderResourceMap(resourceMapUri, this);
         }
         return resourceMap;
     }
 
 
     public boolean isRootFolder() {
         return rootFolder;
     }
 
 
     public void setRootFolder(boolean rootFolder) {
         this.rootFolder = rootFolder;
     }
 
 
     /**
      * Get an existing folder with that URI or null if not found.
      * 
      * @param builder
      *            model instance builder
      * @param uri
      *            folder URI
      * @return a folder instance or URI
      */
     public static Folder get(Builder builder, URI uri) {
         // default resource map URI
         Folder folder = builder.buildFolder(uri, null, null, null, null);
         if (!folder.getResourceMap().isNamedGraph()) {
             return null;
         }
         ResearchObject researchObject = folder.getResourceMap().extractResearchObject();
         return researchObject.getFolders().get(uri);
     }
 
 
     @Override
     protected void save() {
         super.save();
         getResourceMap().save();
         researchObject.getManifest().saveFolderData(this);
     }
 
 
     @Override
     public void delete() {
         //create another collection to avoid concurrent modification
         Set<FolderEntry> entriesToDelete = new HashSet<>(getFolderEntries().values());
         for (FolderEntry entry : entriesToDelete) {
             entry.delete();
         }
         getResourceMap().delete();
         getResearchObject().getFolders().remove(uri);
         super.delete();
     }
 
 
     /**
      * Create a folder instance out of an RDF/XML description.
      * 
      * @param builder
      *            builder for creating objects
      * @param researchObject
      *            research object used as RDF base
      * @param folderUri
      *            folder URI
      * @param content
      *            RDF/XML folder description
      * @return a folder instance
      * @throws BadRequestException
      *             the folder description is incorrect
      */
     private static Folder assemble(Builder builder, ResearchObject researchObject, URI folderUri, InputStream content)
             throws BadRequestException {
         Objects.requireNonNull(researchObject, "Research object cannot be null");
         Objects.requireNonNull(content, "Input stream object cannot be null");
         Folder folder = builder.buildFolder(folderUri, researchObject, builder.getUser(), DateTime.now(), null);
         folder.resourceMap = FolderResourceMap.create(builder, folder, folder.getResourceMapUri());
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         try {
             model.read(content, researchObject.getUri().toString());
         } catch (Exception e) {
             throw new BadRequestException("The folder description could not be parsed", e);
         }
         List<Individual> folders = model.listIndividuals(RO.Folder).toList();
         Map<URI, FolderEntry> entries = new HashMap<>();
         if (folders.size() == 1) {
             Individual folderInd = folders.get(0);
             List<RDFNode> aggregatedResources = folderInd.listPropertyValues(ORE.aggregates).toList();
             for (RDFNode aggregatedResource : aggregatedResources) {
                 if (aggregatedResource.isURIResource()) {
                     try {
                         URI resUri = new URI(aggregatedResource.asResource().getURI());
                         if (!researchObject.getAggregatedResources().containsKey(resUri)) {
                             throw new BadRequestException("Resource " + resUri
                                     + " is not aggregated by the research object");
                         }
                         URI entryUri = folder.getUri().resolve("entries/" + UUID.randomUUID());
                         FolderEntry entry = builder.buildFolderEntry(entryUri, researchObject.getAggregatedResources()
                                 .get(resUri), folder, null);
                         entries.put(resUri, entry);
                     } catch (URISyntaxException e) {
                         throw new BadRequestException("Aggregated resource has an incorrect URI", e);
                     }
                 } else {
                     throw new BadRequestException("Aggregated resources cannot be blank nodes.");
                 }
             }
         } else {
             throw new BadRequestException("The entity body must define exactly one ro:Folder.");
         }
         List<Individual> folderEntries = model.listIndividuals(RO.FolderEntry).toList();
         for (Individual folderEntryInd : folderEntries) {
             URI proxyFor;
             if (folderEntryInd.hasProperty(ORE.proxyFor)) {
                 RDFNode proxyForNode = folderEntryInd.getPropertyValue(ORE.proxyFor);
                 if (!proxyForNode.isURIResource()) {
                     throw new BadRequestException("Folder entry ore:proxyFor must be a URI resource.");
                 }
                 try {
                     proxyFor = new URI(proxyForNode.asResource().getURI());
                 } catch (URISyntaxException e) {
                     throw new BadRequestException("Folder entry proxy for URI is incorrect", e);
                 }
             } else {
                 throw new BadRequestException("Folder entries must have the ore:proxyFor property.");
             }
             if (!entries.containsKey(proxyFor)) {
                 throw new BadRequestException(
                         "Found a folder entry for a resource that is not aggregated by the folder");
             }
             FolderEntry folderEntry = entries.get(proxyFor);
             if (folderEntryInd.hasProperty(RO.entryName)) {
                 RDFNode nameNode = folderEntryInd.getPropertyValue(RO.entryName);
                 if (!nameNode.isLiteral()) {
                     throw new BadRequestException("Folder entry ro name must be a literal");
                 }
                 folderEntry.setEntryName(nameNode.asLiteral().toString());
             }
         }
         for (FolderEntry folderEntry : entries.values()) {
             if (folderEntry.getEntryName() == null) {
                 if (folderEntry.getProxyFor().getUri().getPath() != null) {
                     String[] segments = folderEntry.getProxyFor().getUri().getPath().split("/");
                     folderEntry.setEntryName(segments[segments.length - 1]);
                 } else {
                     folderEntry.setEntryName(folderEntry.getProxyFor().getUri().getHost());
                 }
             }
         }
         folder.setFolderEntries(entries);
         return folder;
     }
 
 
     /**
      * Create and save a new folder.
      * 
      * @param builder
      *            model instance builder
      * @param researchObject
      *            research object aggregating the folder
      * @param uri
      *            folder URI
      * @param content
      *            folder description
      * @return a folder instance
      * @throws BadRequestException
      *             if the description is not valid
      */
     public static Folder create(Builder builder, ResearchObject researchObject, URI uri, InputStream content)
             throws BadRequestException {
         if (researchObject.isUriUsed(uri)) {
             throw new ConflictException("Resource already exists: " + uri);
         }
         Folder folder = assemble(builder, researchObject, uri, content);
         folder.setCreated(DateTime.now());
         folder.setCreator(builder.getUser());
         folder.setProxy(researchObject.addProxy(folder));
         folder.save();
         for (FolderEntry entry : folder.getFolderEntries().values()) {
             folder.getResearchObject().getFolderEntries().put(entry.getUri(), entry);
             folder.getResearchObject().getFolderEntriesByResourceUri().put(entry.getProxyFor().getUri(), entry);
             entry.save();
         }
         folder.getResourceMap().serialize();
         folder.onCreated();
         return folder;
     }
 
 
     @Override
     protected void onCreated() {
         super.onCreated();
         researchObject.getResources().remove(this.getUri());
         researchObject.getFolders().put(this.getUri(), this);
     }
 
 
     /**
      * Copy the folder. The aggregated resources will be relativized against the original RO URI and resolved against
      * this RO URI.
      * 
      * @param builder
      *            model instance builder
      * @param evoBuilder
      *            builder of evolution properties
      * @param researchObject
      *            research object for which the folder will be created
      * @return the new folder
      */
     public Folder copy(Builder builder, EvoBuilder evoBuilder, ResearchObject researchObject) {
         URI folderUri = researchObject.getUri().resolve(getRawPath());
         if (researchObject.isUriUsed(folderUri)) {
             throw new ConflictException("Resource already exists: " + folderUri);
         }
         Folder folder2 = builder.buildFolder(folderUri, researchObject, getCreator(), getCreated(), null);
         folder2.setCopyDateTime(DateTime.now());
         folder2.setCopyAuthor(builder.getUser());
         folder2.setCopyOf(this);
         folder2.setProxy(researchObject.addProxy(folder2));
         folder2.save();
         for (FolderEntry entry : getFolderEntries().values()) {
             entry.copy(builder, folder2);
         }
         folder2.getResourceMap().serialize();
         folder2.onCreated();
         return folder2;
     }
 
 
     /**
      * Create and save a new folder entry, refresh the properties of this folder.
      * 
      * @param content
      *            folder entry description
      * @return a folder entry instance
      * @throws BadRequestException
      *             if the description is not valid
      */
     public FolderEntry createFolderEntry(InputStream content)
             throws BadRequestException {
         FolderEntry entry = FolderEntry.assemble(builder, this, content);
         return addFolderEntry(entry);
     }
 
 
     /**
      * Save folder entry, refresh the properties of this folder.
      * 
      * @param entry
      *            folder entry
      * @return a folder entry instance
      */
     public FolderEntry addFolderEntry(FolderEntry entry) {
         Objects.requireNonNull(entry, "Folder entry cannot be null");
         for (FolderEntry entry2 : getFolderEntries().values()) {
             if (entry2.getProxyFor().equals(entry.getProxyFor())) {
                 throw new ConflictException("Folder entry for this resource already exists");
             }
         }
         getFolderEntries().put(entry.getUri(), entry);
         getResearchObject().getFolderEntries().put(entry.getUri(), entry);
         getResearchObject().getFolderEntriesByResourceUri().put(entry.getProxyFor().getUri(), entry);
         entry.save();
         getResourceMap().serialize();
         return entry;
     }
 
 
     @Override
     public Map<URI, ? extends Proxy> getProxies() {
         return getFolderEntries();
     }
 
 
     public URI getResourceMapUri() {
         return resourceMapUri;
     }
 
 
     /**
      * Update the folder contents.
      * 
      * @param content
      *            the resource content
      * @param contentType
      *            the content MIME type
      * @throws BadRequestException
      *             if it is expected to be an RDF file and isn't
      */
     @Override
     public void update(InputStream content, String contentType)
             throws BadRequestException {
         Folder newFolder = assemble(builder, getResearchObject(), uri, content);
         for (FolderEntry entry : getFolderEntries().values()) {
             entry.delete();
         }
         for (FolderEntry entry : newFolder.getFolderEntries().values()) {
             addFolderEntry(entry);
         }
         getResourceMap().serialize();
     }
 }
