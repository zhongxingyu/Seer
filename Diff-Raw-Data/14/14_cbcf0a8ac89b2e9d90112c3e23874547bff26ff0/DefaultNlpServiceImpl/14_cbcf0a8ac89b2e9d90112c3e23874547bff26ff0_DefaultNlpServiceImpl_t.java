 /**
  * Copyright (C) 2011-
  *
  * All rights reserved.
  */
 package gov.va.vinci.v3nlp;
 
 import gov.va.vinci.cm.Corpus;
 import gov.va.vinci.cm.DocumentInterface;
 import gov.va.vinci.cm.service.SerializationService;
 import gov.va.vinci.v3nlp.model.BatchJobStatus;
 import gov.va.vinci.v3nlp.model.CorpusSummary;
 import gov.va.vinci.v3nlp.model.ServicePipeLine;
 import gov.va.vinci.v3nlp.model.ServicePipeLineComponent;
 import gov.va.vinci.v3nlp.registry.RegistryService;
 import gov.va.vinci.v3nlp.services.ServicePipeLineProcessor;
 import gov.va.vinci.v3nlp.services.database.DatabaseRepositoryService;
 import gov.va.vinci.v3nlp.services.database.V3nlpDBRepository;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import java.io.*;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Default NlpService implementation that V3NLP Keywords.
  *
  */
 @Transactional
 public class DefaultNlpServiceImpl implements NlpService {
 
     private String directoryToStoreResults;
 
     private SerializationService serializationService;
 
     private ServicePipeLineProcessor servicePipeLineProcessor;
 
     private ServicePipeLineProcessor uimaServicePipeLineProcessor;
 
     private DatabaseRepositoryService databaseRepositoryService;
 
     private RegistryService registryService;
 
     private EntityManager entityManager;
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this. entityManager = entityManager;
     }
 
     public void setDirectoryToStoreResults(String directoryToStoreResults) {
         this.directoryToStoreResults = directoryToStoreResults;
     }
 
     public void setSerializationService(SerializationService serializationService) {
         this.serializationService = serializationService;
     }
 
     public void init() {
         if (!new File(directoryToStoreResults).exists()
                 || !new File(directoryToStoreResults).isDirectory()) {
             throw new RuntimeException(directoryToStoreResults
                     + ": Is not a valid directory to store results.");
         }
     }
 
     @Override
     public CorpusSummary getPipeLineResults(String pipeLineId, String userToken) {
         String pathOfResults = directoryToStoreResults + Utilities.getUsernameAsDirectory(userToken);
 
         if (new File(pathOfResults + pipeLineId + ".results")
                 .exists()) {
             CorpusSummary c = (CorpusSummary) this.deSerialize(pathOfResults
                     + pipeLineId + ".results");
             return c;
         } else if (new File(pathOfResults + pipeLineId + ".err")
                 .exists()) {
             Exception e = (Exception) this
                     .deSerialize(pathOfResults + pipeLineId
                             + ".err");
             throw new RuntimeException(e);
         } else {
             throw new RuntimeException("Results not found.");
         }
     }
 
     @Override
     public String getPipeLineStatus(String pipeLineId, String userToken) {
         String pathOfResults = directoryToStoreResults + Utilities.getUsernameAsDirectory(userToken);
         if (new File(pathOfResults + pipeLineId + ".lck").exists()) {
             return "PROCESSING";
         }
         if (new File(pathOfResults + pipeLineId + ".err").exists()) {
             return "ERROR";
         }
         if (new File(pathOfResults + pipeLineId + ".results").exists()) {
             return "COMPLETE";
         }
 
         return null;
     }
 
     @Override
     public String submitPipeLine(ServicePipeLine pipeLine, Corpus corpus, List<V3nlpDBRepository> dataServiceSourceList)
             throws SQLException {
 
 
         for (V3nlpDBRepository ds : dataServiceSourceList) {
             List<DocumentInterface> docs = this.databaseRepositoryService.getDocuments(ds,  Utilities.getUsername(pipeLine.getUserToken().trim()));
 
             if (docs==null || docs.size() == 0) {
                 throw new RuntimeException("Data service returned no documents.");
             }
 
             for (DocumentInterface di : docs) {
                 corpus.addDocument(di);
             }
         }
         return this.submitPipeLine(pipeLine, corpus);
     }
 
     @Override
     @Transactional(readOnly = false)
     public String submitPipeLine(ServicePipeLine pipeLine, Corpus corpus) {
         String pathOfResults = directoryToStoreResults + Utilities.getUsernameAsDirectory(pipeLine.getUserToken().trim());
        System.out.println("Path:" + pathOfResults);
         BatchJobStatus jobStatus = new BatchJobStatus();
         jobStatus.setJobName(pipeLine.getJobName());
         jobStatus.setDescription(pipeLine.getJobDescription());
         jobStatus.setRunDate(new java.util.Date());
         jobStatus.setStatus("RUNNING");
         jobStatus.setUsername(Utilities.getUsername(pipeLine.getUserToken().trim()));
         jobStatus.setPipelineXml(pipeLine.getPipelineXml());
 
 
          // Make sure output path exists, or create it.
         if (!new File(pathOfResults).exists()) {
            new File(pathOfResults).mkdir();
         }
 
         try {
             String pipeLineId = new Date().getTime() + "-"
                     + pipeLine.hashCode();
             jobStatus.setPipeLineId(pipeLineId);
             entityManager.persist(jobStatus);
             entityManager.flush();
             new File(pathOfResults + pipeLineId + ".lck").createNewFile();
 
 
             // Determine the technology of this pipeline.
             String technology = null;
             for (ServicePipeLineComponent comp: pipeLine.getServices()) {
                  if (comp.getServiceUid() != null) {
                      technology = registryService.getNlpComponent(comp.getServiceUid()).getTechnology();
                      break;
                  }
             }
 
             if ("Gate".equals(technology)) {
                 servicePipeLineProcessor.processPipeLine(pipeLineId, pipeLine, corpus, jobStatus);
             } else if ("UIMA".equals(technology)) {
                 uimaServicePipeLineProcessor.processPipeLine(pipeLineId, pipeLine, corpus, jobStatus);
             }
 
             return pipeLineId;
 
         } catch (Exception e) {
            System.out.println("Exception:" + e);
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public List<BatchJobStatus> jobsForUserToken(String userToken) {
         String pathOfResults = directoryToStoreResults + Utilities.getUsernameAsDirectory(userToken);
 
         String username= Utilities.getUsername(userToken);
         Query query = entityManager.createQuery("SELECT job FROM gov.va.vinci.v3nlp.model.BatchJobStatus job where username = ?1 order by runDate desc");
         query.setParameter(1, username);
         return (List<BatchJobStatus> ) query.getResultList();
 
     }
 
     public String testDataService(V3nlpDBRepository ds, String loggedInUser) {
         return databaseRepositoryService.test(ds, Utilities.getUsername(loggedInUser.trim()));
     }
 
     /**
      * Given a pipeline id and user token, load the result and return it as a serialized string.
      * @param pipeLineId
      * @param userToken
      * @return the corpus in serialized form.
      */
     public String getSerializedResults(String pipeLineId, String userToken){
         CorpusSummary summary = this.getPipeLineResults(pipeLineId, userToken);
         return this.serializeCorpus(summary.getCorpus());
     }
 
     public String serializeCorpus(Corpus c) {
         return serializationService.serialize(c);
     }
 
     public Corpus deSerializeCorpus(String content) {
         return serializationService.deserialize(content, Corpus.class);
     }
 
     public CorpusSummary deSerializeCorpusToCorpusSummary(String content) {
         CorpusSummary cs = new CorpusSummary(deSerializeCorpus(content));
         return cs;
     }
 
     public List<V3nlpDBRepository> getRepositories() {
         return this.databaseRepositoryService.getRepositories();
     }
     
     /************************************************************************************************************
      * Setters Methods Below.
      ***********************************************************************************************************/
     public void setServicePipeLineProcessor(ServicePipeLineProcessor servicePipeLineProcessor) {
         this.servicePipeLineProcessor = servicePipeLineProcessor;
     }
 
     public void setDatabaseRepositoryService(DatabaseRepositoryService databaseRepositoryService) {
         this.databaseRepositoryService = databaseRepositoryService;
     }
 
     public void setUimaServicePipeLineProcessor(ServicePipeLineProcessor uimaServicePipeLineProcessor) {
         this.uimaServicePipeLineProcessor = uimaServicePipeLineProcessor;
     }
 
     public void setRegistryService(RegistryService r) {
         this.registryService = r;
     }
 
 
     /************************************************************************************************************
      * Private Methods Below.
      ***********************************************************************************************************/
 
     private Object deSerialize(String path) {
         try {
             InputStream is = new FileInputStream(new File(path));
             ObjectInput oi = new ObjectInputStream(is);
             Object newObj = oi.readObject();
             oi.close();
             return newObj;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
 }
