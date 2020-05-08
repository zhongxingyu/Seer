 package net.cyklotron.cms.docimport;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Map;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.relation.CoralRelationManager;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.structure.StructureException;
 
 public class DocumentImportCoordinatorServiceImpl
     implements DocumentImportCoordinatorService
 {
     private static final int DEFAULT_FETCH_MONTHS = 3;
 
     private static final String RELATION_NAME = "docimport.ImportTracing";
 
     private final DocumentImportService documentImportService;
 
     private final DocumentPostingService documentPostingService;
 
     private final CoralSessionFactory coralSessionFactory;
 
     private final Logger log;
 
     public DocumentImportCoordinatorServiceImpl(DocumentImportService documentImportService,
         DocumentPostingService documentPostingService, CoralSessionFactory coralSessionFactory,
         Logger log)
     {
         this.documentImportService = documentImportService;
         this.documentPostingService = documentPostingService;
         this.coralSessionFactory = coralSessionFactory;
         this.log = log;
     }
 
     @Override
     public void processImport(ImportResource importResource, CoralSession coralSession)
     {
         log.info("Starting import processing for " + importResource);
         Date currentTime = new Date();
         Date lastNewDocumentsCheck = importResource.isLastNewDocumentsCheckDefined() ? importResource
             .getLastNewDocumentsCheck() : getDefaultStartDate(currentTime);
 
         log.info("Fetching new documents created after " + lastNewDocumentsCheck);
         Collection<DocumentData> docs;
         try
         {
             docs = documentImportService.importDocuments(new PersistentImportSourceConfiguration(
                 importResource), lastNewDocumentsCheck, currentTime);
         }
         catch(IOException e)
         {
             log.error("failed to fetch documents", e);
             return;
         }
 
         Map<DocumentData, DocumentNodeResource> docMap;
         try
         {
             docMap = documentPostingService.postDocuments(new PersistentImportTargetConfiguration(
                 importResource), docs);
         }
         catch(StructureException e)
         {
             log.error("failed to to post documents", e);
             return;
         }
 
         try
         {
             traceImports(docMap, importResource, currentTime, coralSession);
         }
         catch(StructureException e)
         {
             log.error("failed to update import tracing information", e);
         }
 
         importResource.setLastNewDocumentsCheck(currentTime);
         importResource.update();
         log.info("Finished import processing for " + importResource);
     }
 
     private Date getDefaultStartDate(Date currentTime)
     {
         Calendar cal = new GregorianCalendar();
         cal.setTime(currentTime);
         cal.add(Calendar.MONTH, -DEFAULT_FETCH_MONTHS);
         return cal.getTime();
     }
 
     private void traceImports(Map<DocumentData, DocumentNodeResource> docMap,
         ImportResource importResource, Date currentTime, CoralSession coralSession)
         throws StructureException
     {
         try
         {
             RelationModification mod = new RelationModification();
             for(Map.Entry<DocumentData, DocumentNodeResource> entry : docMap.entrySet())
             {
                 ImportTraceResource trace = ImportTraceResourceImpl.createImportTraceResource(
                     coralSession, entry.getKey().getOriginalName(), importResource, entry.getKey()
                         .getOriginalURI().toASCIIString(), entry.getKey().getModificationDate(),
                     currentTime, entry.getValue());
                 mod.add(trace, entry.getValue());
             }
             final CoralRelationManager relationManager = coralSession.getRelationManager();
             Relation rel = relationManager.getRelation(RELATION_NAME);
             relationManager.updateRelation(rel, mod);
         }
         catch(Exception e)
         {
             throw new StructureException("failed to update import trace information", e);
         }
     }
 
     @Override
     public void processAllImports()
     {
         CoralSession coralSession = null;
         try
         {
             coralSession = coralSessionFactory.getRootSession();
             QueryResults results = coralSession.getQuery().executeQuery(
                "FIND FROM docimport.import");
             for(Resource res : results.getList(1))
             {
                 processImport((ImportResource)res, coralSession);
             }
         }
         catch(MalformedQueryException e)
         {
             throw new RuntimeException("internal error", e);
         }
         finally
         {
             coralSession.close();
         }
     }
 }
