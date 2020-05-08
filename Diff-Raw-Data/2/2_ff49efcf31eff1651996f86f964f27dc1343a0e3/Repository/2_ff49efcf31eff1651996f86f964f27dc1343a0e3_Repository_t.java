 package uk.ac.bristol.dundry.dao;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.util.ResourceUtils;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.*;
 import java.util.Map.Entry;
 import org.quartz.Job;
 import static org.quartz.JobBuilder.newJob;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.SchedulerException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import uk.ac.bristol.dundry.model.ResourceCollection;
 import uk.ac.bristol.dundry.tasks.JobBase;
 import uk.ac.bristol.dundry.tasks.MoveTask;
 import uk.ac.bristol.dundry.tasks.StateChanger;
 import uk.ac.bristol.dundry.vocabs.RepositoryVocab;
 
 /**
  *
  * @author Damian Steer <d.steer@bris.ac.uk>
  */
 public class Repository {
     
     static final Logger log = LoggerFactory.getLogger(Repository.class);
     
     public static enum State {
         Created, Depositing, Deposited, Publishing, Published, Deleted       
     }
     
     // play it safe. radix of 36 is ideal 
     static final int RADIX = Math.min(Character.MAX_RADIX, 36);
     
     @Autowired
     protected TaskManager taskManager;
     
     private final FileRepository fileRepo;
     private final MetadataStore mdStore;
     private final List<Class<? extends Job>> postDepositJobs;
     private final List<Class<? extends Job>> prePublishJobs;
     private final Properties jobProperties;
     private final String publishURLBase;
 
     /**
      * The heart of the repository
      *
      * @param publishURLBase The base url where publications will appear
      * @param fileRepo The
      * @param mdStore The metadata store
      * @param postDepositJobClasses A list of classes (implementing Job) which
      * will be run when content is added
      * @param prePublishJobClasses A list of classes which will be run
      * pre-publication
      * @param jobProperties Parameters that will be passed to the jobs
      * @param sensitiveProperties Parameters that will be added to the jobProperties
      */
     public Repository(String publishURLBase, FileRepository fileRepo, MetadataStore mdStore,
             List<String> postDepositJobClasses,
             List<String> prePublishJobClasses,
             Properties jobProperties,
             Properties sensitiveProperties) {
         this.fileRepo = fileRepo;
         this.mdStore = mdStore;
         // ensure we just need to append id to base
         this.publishURLBase = publishURLBase.endsWith("/") ? publishURLBase : publishURLBase + "/";
         this.postDepositJobs = getJobsFromClassNames(postDepositJobClasses);
         this.prePublishJobs = getJobsFromClassNames(prePublishJobClasses);
         this.jobProperties = jobProperties;
         for (Entry<Object, Object> secret: sensitiveProperties.entrySet()) {
             jobProperties.setProperty((String) secret.getKey(), (String) secret.getValue());
         }
     }
     
     /**
      * The heart of the repository (no sensitive properties)
      *
      * @param publishURLBase The base url where publications will appear
      * @param fileRepo The
      * @param mdStore The metadata store
      * @param postDepositJobClasses A list of classes (implementing Job) which
      * will be run when content is added
      * @param prePublishJobClasses A list of classes which will be run
      * pre-publication
      * @param jobProperties Parameters that will be passed to the jobs
      */
     public Repository(String publishURLBase, FileRepository fileRepo, MetadataStore mdStore,
             List<String> postDepositJobClasses,
             List<String> prePublishJobClasses,
             Properties jobProperties) {
         this(publishURLBase, fileRepo, mdStore, postDepositJobClasses, 
                 prePublishJobClasses, jobProperties, new Properties());
     }
     
     public ResourceCollection getIds() {
         Model resultModel = ModelFactory.createDefaultModel();
         ResultSet r = mdStore.query("select distinct ?g ?state ?title ?description ?source ?project"
                 + "{ graph ?g1 { "
                 + "   ?g <http://vocab.bris.ac.uk/data/repository#state> ?state "
                 + "   OPTIONAL { ?g <http://purl.org/dc/terms/source> ?source } "
                 + "  } "
                 + "  graph ?g2 { "
                 + "   ?g <http://purl.org/dc/terms/title> ?title ."
                 + "   OPTIONAL { ?g <http://purl.org/dc/terms/description> ?description } "
                 + "   OPTIONAL { ?g <http://vocab.bris.ac.uk/data/repository#project> ?project } "
                 + "  } "
                 + "  FILTER (?state != \"Deleted\") "
                 + "}");
         List<Resource> ids = new LinkedList<>();
         while (r.hasNext()) {
             QuerySolution nxt = r.next();
             // get item and copy to resultModel
             Resource item = nxt.getResource("g").inModel(resultModel);
             item.addProperty(RDFS.label, nxt.get("title"));
             item.addProperty(RepositoryVocab.state, nxt.get("state"));
             if (nxt.contains("source")) {
                 item.addProperty(DCTerms.source, nxt.get("source"));
             }
             if (nxt.contains("description")) {
                 item.addProperty(DCTerms.description, nxt.get("description"));
             }
             if (nxt.contains("project")) {
                 item.addProperty(RepositoryVocab.project, nxt.get("project"));
             }
             ids.add(item);
         }
         log.info("Ids is: {}", ids);
         return new ResourceCollection(ids);
     }
 
     public boolean hasId(String item) {
         return getProvenanceMetadata(item).hasProperty(null);
     }
 
     /**
      * Create a new deposit
      *
      * @param creator User id who made this deposit
      * @param subject Metadata to include about the created deposit. It will be
      * renamed once and id has been allocated.
      * @return
      * @throws IOException
      */
     public String create(String creator, Resource subject) throws IOException, SchedulerException {
         // Create a random id!
         UUID randId = UUID.randomUUID();
         String baseEncoded =
                 Long.toString(randId.getMostSignificantBits(), RADIX)
                 + Long.toString(randId.getLeastSignificantBits(), RADIX);
         String id = baseEncoded.replace("-", ""); // remove sign bits
 
         // Now we have an id rename the subject
         Resource renamed = ResourceUtils.renameResource(subject, toInternalId(id));
         
         // Get base dir. It is allowed to be missing if using absolute repo paths
         String base = (renamed.hasProperty(RepositoryVocab.base_directory)) ?
                 renamed.getProperty(RepositoryVocab.base_directory).getString() :
                 null;
         
         Path repoDir = fileRepo.create(id, base);
 
         Resource prov = ModelFactory.createDefaultModel().createResource(toInternalId(id));
         prov.addLiteral(DCTerms.dateSubmitted, Calendar.getInstance());
         prov.addProperty(RepositoryVocab.depositor, creator);
         if (base != null) prov.addProperty(RepositoryVocab.base_directory, base);
         prov.addProperty(RepositoryVocab.state, State.Created.name());
 
         // Create mutable and immutable graphs
         mdStore.create(toInternalId(id), renamed.getModel()); // often a noop
         mdStore.create(toInternalId(id) + "/prov", prov.getModel());
 
         return id;
     }
 
     /**
      * Put content into the repository
      *
      * @param depositTask
      * @param id The repository id
      * @param source An identifier for the source (will be recorded with
      * deposit)
      */
     public void makeDeposit(JobDetail depositTask, String id, String source) throws SchedulerException { 
         ensureState(id, EnumSet.of(State.Created));
         
         Resource prov = getProvenanceMetadata(id);
         prov.addProperty(DCTerms.source, source);
         prov.addLiteral(DCTerms.dateSubmitted, Calendar.getInstance());
         
         startProcess(id, prov,
                 State.Depositing, State.Deposited,
                 Collections.singletonList(depositTask), postDepositJobs, jobProperties);
     }
 
     public void publish(String id) throws SchedulerException {
         ensureState(id, EnumSet.of(State.Deposited));
         
         Resource prov = getProvenanceMetadata(id);
         prov.addLiteral(DCTerms.date, Calendar.getInstance());
         
         // Add the move task. This is the primary task.
         List<Class<? extends Job>> jobs = new ArrayList<>();
         jobs.add(MoveTask.class);
         jobs.addAll(prePublishJobs);
         
         // Include move task properties at the end
         startProcess(id, prov,
                 State.Publishing, State.Published, Collections.EMPTY_LIST,
                 jobs, jobProperties, 
                 MoveTask.FROM, getDepositPathForId(id).toAbsolutePath().toString(),
                 MoveTask.TO, getPublishPathForId(id).toAbsolutePath().toString());
     }
     
     /**
      * Soft-delete a record.
      * @param id The repository id
      */
     public void delete(String id) {
         ensureState(id, EnumSet.of(State.Created, State.Deposited));
         
         Resource prov = getProvenanceMetadata(id);
         prov.removeAll(RepositoryVocab.state);
         prov.addLiteral(RepositoryVocab.state, State.Deleted.name());
         updateProvenanceMetadata(id, prov);
     }
     
     /**
      * Start a process to move between states.
      * Less abstractly, kick off tasks that will result in deposit or publication
      * @param id ID of the deposit
      * @param prov Provenance metadata to store (a bit hacky?)
      * @param startState State during this process (e.g. Depositing)
      * @param endState State at end of this process (e.g. Deposited)
      * @param jobsToRun External jobs to run (will run first)
      * @param jobs Internal jobs
      * @param jobParams Externally provided parameters
      * @param otherParams Any other parameters (in pairs)
      */
     private void startProcess(String id, Resource prov,
             State startState, State endState,
             List<JobDetail> jobsToRun,
             List<Class<? extends Job>> jobs, Properties jobParams,
             String... otherParams) throws SchedulerException {
         
         JobDataMap jobData = new JobDataMap();
         
         jobData.put(JobBase.ID, id);
         jobData.put(JobBase.REPOSITORY, this); // Not keen on this (not storable)
         
         jobData.put(StateChanger.TO_STATE, endState.name());
         
         // Copy in job params
         for (Entry<Object, Object> e : jobParams.entrySet()) {
             jobData.put((String) e.getKey(), (String) e.getValue());
         }
         // And add other params
         for (int i = 0; i < otherParams.length - 1; i += 2) {
             jobData.put(otherParams[i], otherParams[i + 1]);
         }
 
         List<JobDetail> jobDetails = new ArrayList<>();
         jobDetails.addAll(jobsToRun);
         
         for (Class<? extends Job> c: jobs) {
             JobDetail jd = newJob(c)
                     .withIdentity(c.getCanonicalName(), id)
                     .usingJobData(jobData)
                     .build();
             jobDetails.add(jd);
         }
         
         // Add the state changer at the end
         jobDetails.add(newJob(StateChanger.class)
                 .withIdentity("state changer", id)
                 .usingJobData(jobData).build());
         
         // Go to start state, store provenance
         // Not keen on this going over a method boundary, but simplifies things
         prov.removeAll(RepositoryVocab.state);
         prov.addLiteral(RepositoryVocab.state, startState.name());
         updateProvenanceMetadata(id, prov);
         
         // Make it so
         taskManager.executeJobsInOrder(id, jobDetails);
     }
 
     public Resource getMetadata(String id) {
         String internalId = toInternalId(id);
 
         Model m = ModelFactory.createDefaultModel();
         m.add(mdStore.getData(internalId));
         m.add(mdStore.getData(internalId + "/prov"));
         return m.createResource(internalId);
     }
 
     public void updateMetadata(String id, Resource r) {
         String internalId = toInternalId(id);
 
         // Replace metadata with new information that's not in prov
         Model m = ModelFactory.createDefaultModel();
         m.add(r.getModel());
         // Remove provenance info
         m.remove(mdStore.getData(internalId + "/prov"));
         mdStore.replaceData(internalId, m);
     }
 
     public Resource getProvenanceMetadata(String id) {
         String internalId = toInternalId(id);
 
         return mdStore.getData(internalId + "/prov").createResource(internalId);
     }
 
     public void updateProvenanceMetadata(String id, Resource r) {
         String internalId = toInternalId(id);
 
         mdStore.replaceData(internalId + "/prov", r.getModel());
     }
 
     public Path getDepositPathForId(String id) {
         return fileRepo.depositPathForId(id, getBase(id));
     }
 
     public Path getPublishPathForId(String id) {
         String base = getProvenanceMetadata(id).getRequiredProperty(RepositoryVocab.base_directory).getString();
         return fileRepo.publishPathForId(id, getBase(id));
     }
 
     public String getPublishedURL(String id) {
        return publishURLBase + id + "/";
     }
     
     /**
      * Returns the status of a deposit
      * @param id
      * @return 
      */
     public State getState(String id) {
         Resource r = getProvenanceMetadata(id);
         return State.valueOf(r.getRequiredProperty(RepositoryVocab.state).getString());
     }
     
     public String getBase(String id) {
         Resource r = getProvenanceMetadata(id);
         Statement base = r.getProperty(RepositoryVocab.base_directory);
         return (base == null) ? null : base.getString();
     }
     
     /**
      * Takes an id and makes it suitable for external use by stripping off
      * leading 'repo:' if present
      *
      * @param uri
      * @return An un-repo'd string
      */
     public static String toExternalId(String uri) {
         if (uri.startsWith("repo:")) {
             return uri.substring(5);
         } else {
             return uri;
         }
     }
 
     /**
      * Make an internal uri from id
      *
      * @param id
      * @return
      */
     public static String toInternalId(String id) {
         if (id.startsWith("http://") || id.startsWith("file://")) {
             return id;
         } else {
             return "repo:" + id;
         }
     }
 
     // For initialising the jobs
     private List<Class<? extends Job>> getJobsFromClassNames(List<String> classNames) {
         // Load up job classes
         List<Class<? extends Job>> jobs = new ArrayList<>();
         for (String jobClassName : classNames) {
             // Try to load the class. Check it is a Job.
             try {
                 Class<?> job = Repository.class.getClassLoader().loadClass(jobClassName);
                 if (Job.class.isAssignableFrom(job)) {
                     jobs.add((Class<? extends Job>) job);
                 } else {
                     log.error("Class <{}> is not a Job. Ignoring.", jobClassName);
                 }
             } catch (ClassNotFoundException ex) {
                 log.error("Job class <{}> not found. Ignoring.", jobClassName);
             }
         }
 
         return jobs;
     }
     
     /**
      * Check whether the state of id is one of allowed
      * @param id
      * @param allowed 
      */
     private void ensureState(String id, EnumSet<State> allowed) {
         State state = this.getState(id);
         if (!allowed.contains(state)) 
             throw new IllegalArgumentException(String.format(
                     "Not permitted in current state <%s> (allowed %s)",
                     state,
                     allowed
                     ));
     }
 }
