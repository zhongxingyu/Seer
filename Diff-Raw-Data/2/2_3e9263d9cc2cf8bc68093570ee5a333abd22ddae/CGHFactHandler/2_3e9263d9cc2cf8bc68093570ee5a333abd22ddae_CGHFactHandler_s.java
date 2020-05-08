 package gov.nih.nci.rembrandt.queryservice.queryprocessing.cgh;
 
 import gov.nih.nci.rembrandt.dbbean.ArrayGenoAbnFact;
 import gov.nih.nci.rembrandt.dbbean.GeneLlAccSnp;
 import gov.nih.nci.rembrandt.dto.query.ComparativeGenomicQuery;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.AllGenesCritValidator;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.CommonFactHandler;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.DBEvent;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ThreadController;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.cgh.CopyNumber.SNPAnnotation;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultSet;
 import gov.nih.nci.rembrandt.util.ThreadPool;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 import org.apache.ojb.broker.query.Criteria;
 import org.apache.ojb.broker.query.Query;
 import org.apache.ojb.broker.query.QueryFactory;
 import org.apache.ojb.broker.query.ReportQueryByCriteria;
 
 /**
  * @author BhattarR
  */
 abstract public class CGHFactHandler {
     private static Logger logger = Logger.getLogger(CGHFactHandler.class);
     Map cghObjects = Collections.synchronizedMap(new HashMap());
     Map annotations = Collections.synchronizedMap(new HashMap());
     private final static int VALUES_PER_THREAD = 200;
     List factEventList = Collections.synchronizedList(new ArrayList());
     List annotationEventList = Collections.synchronizedList(new ArrayList());
     abstract void addToResults(Collection results);
     abstract ResultSet[] executeSampleQuery(final Collection allSNPProbeIDs, final ComparativeGenomicQuery cghQuery)
     throws Exception;
     abstract ResultSet[] executeSampleQueryForAllGenes(final ComparativeGenomicQuery cghQuery)
     throws Exception;
 
     private static void addCopyNumbFactCriteria(ComparativeGenomicQuery cghQuery, final Class targetFactClass, PersistenceBroker _BROKER, final Criteria sampleCrit) throws Exception {
             CommonFactHandler.addDiseaseCriteria(cghQuery, targetFactClass, _BROKER, sampleCrit);
             CopyNumberCriteriaHandler.addCopyNumberCriteriaForAllGenes(cghQuery, targetFactClass, _BROKER, sampleCrit);
             CommonFactHandler.addSampleIDCriteria(cghQuery, targetFactClass, sampleCrit);
             CommonFactHandler.addAccessCriteria(cghQuery, targetFactClass, sampleCrit);
     }
 
     protected void executeQuery(final String snpOrCGHAttr, Collection cghOrSNPIDs, final Class targetFactClass, ComparativeGenomicQuery cghQuery) throws Exception {
             ArrayList arrayIDs = new ArrayList(cghOrSNPIDs);
             for (int i = 0; i < arrayIDs.size();) {
                 Collection values = new ArrayList();
                 int begIndex = i;
                 i += VALUES_PER_THREAD ;
                 int endIndex = (i < arrayIDs.size()) ? endIndex = i : (arrayIDs.size());
                 values.addAll(arrayIDs.subList(begIndex,  endIndex));
                 final Criteria IDs = new Criteria();
                 IDs.addIn(snpOrCGHAttr, values);
                 final String threadID = "CopyNumberChangeCriteriaHandler.ThreadID:" + snpOrCGHAttr + ":" +i;
 
                 final DBEvent.FactRetrieveEvent dbEvent = new DBEvent.FactRetrieveEvent(threadID);
                 factEventList.add(dbEvent);
                 PersistenceBroker _BROKER = PersistenceBrokerFactory.defaultPersistenceBroker();
                 _BROKER.clearCache();
 
                 final Criteria sampleCrit = new Criteria();
                 CommonFactHandler.addDiseaseCriteria(cghQuery, targetFactClass, _BROKER, sampleCrit);
                 CopyNumberCriteriaHandler.addCopyNumberCriteria(cghQuery, targetFactClass, _BROKER, sampleCrit);
                 CommonFactHandler.addSampleIDCriteria(cghQuery, targetFactClass, sampleCrit);
                 CommonFactHandler.addAccessCriteria(cghQuery, targetFactClass, sampleCrit);
 
 
                 _BROKER.close();
 
                 new Thread(
                    new Runnable() {
                       public void run() {
                           logger.debug("New Thread Started: " + threadID );
                           final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                           pb.clearCache();
                           sampleCrit.addAndCriteria(IDs);
                           logger.debug("Criteria To be exucuted for targetClass:" + targetFactClass.getName() + " " +
                                   sampleCrit.toString());
                           Query sampleQuery =
                          QueryFactory.newQuery(targetFactClass,sampleCrit, true);
                           assert(sampleQuery != null);
                           Collection exprObjects =  pb.getCollectionByQuery(sampleQuery );
                           addToResults(exprObjects);
                           pb.close();
                           dbEvent.setCompleted(true);
                       }
                    }
                ).start();
             }
     }
 
      protected void executeGeneAnnotationQuery(Collection cghOrSNPIDs) throws Exception {
             ArrayList arrayIDs = new ArrayList(cghOrSNPIDs);
             for (int i = 0; i < arrayIDs.size();) {
                 Collection values = new ArrayList();
                 int begIndex = i;
                 i += VALUES_PER_THREAD ;
                 int endIndex = (i < arrayIDs.size()) ? endIndex = i : (arrayIDs.size());
                 values.addAll(arrayIDs.subList(begIndex,  endIndex));
                 final Criteria annotCrit = new Criteria();
                 annotCrit.addIn(GeneLlAccSnp.SNP_PROBESET_ID, values);
                 long time = System.currentTimeMillis();
                 String threadID = "CGHHandler.ThreadID:" + time;
                 final DBEvent.AnnotationRetrieveEvent dbEvent = new DBEvent.AnnotationRetrieveEvent(threadID);
                 annotationEventList.add(dbEvent);
 
                 ThreadPool.AppThread t = ThreadPool.newAppThread(
                            new ThreadPool.MyRunnable() {
                               public void codeToRun() {
                                   final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                           ReportQueryByCriteria annotQuery =
                           QueryFactory.newReportQuery(GeneLlAccSnp.class, annotCrit, true);
                           annotQuery.setAttributes(new String[] {GeneLlAccSnp.SNP_PROBESET_ID, GeneLlAccSnp.GENE_SYMBOL, GeneLlAccSnp.LOCUS_LINK_ID, GeneLlAccSnp.ACCESSION});
                           assert(annotQuery != null);
                           Iterator iter =  pb.getReportQueryIteratorByQuery(annotQuery);
                           while (iter.hasNext()) {
                                Object[] attrs = (Object[]) iter.next();
                                Long snpProbID = new Long(((BigDecimal)attrs[0]).longValue());
                               if (snpProbID != null) { 
                                 CopyNumber.SNPAnnotation a = (CopyNumber.SNPAnnotation)annotations.get(snpProbID );
                                  if (a == null) {
                                    a = new CopyNumber.SNPAnnotation(snpProbID, new HashSet(), new HashSet(), new HashSet());
                                    annotations.put(snpProbID, a);
                                  }
                                  a.getGeneSymbols().add(attrs[1]);
                                  a.getLocusLinkIDs().add(attrs[2]);
                                  a.getAccessionNumbers().add(attrs[3]);
                               }
                           }
                           pb.close();
                           dbEvent.setCompleted(true);
                       }
                    }
                );
                 t.start();
             }
     }
 
     final static class SingleCGHFactHandler extends CGHFactHandler {
         ResultSet[] executeSampleQueryForAllGenes(final ComparativeGenomicQuery cghQuery)
         throws Exception {
             //logger.debug("Total Number Of SNP_PROBES:" + allSNPProbeIDs.size());
             //executeQuery(ArrayGenoAbnFact.SNP_PROBESET_ID, allSNPProbeIDs, ArrayGenoAbnFact.class, cghQuery);
             AllGenesCritValidator.validateSampleIDCrit(cghQuery);
 
             PersistenceBroker _BROKER = PersistenceBrokerFactory.defaultPersistenceBroker();
             final Criteria sampleCrit = new Criteria();
             addCopyNumbFactCriteria(cghQuery, ArrayGenoAbnFact.class, _BROKER, sampleCrit);
             org.apache.ojb.broker.query.Query sampleQuery =
                 QueryFactory.newQuery(ArrayGenoAbnFact.class, sampleCrit, false);
 
 
 
             Collection exprObjects =  _BROKER.getCollectionByQuery(sampleQuery );
             addToResults(exprObjects);
             logger.debug("Length: " + exprObjects.size());
             _BROKER.close();
 
             Collection allSNPProbeIDs = new ArrayList();
             Collection col = cghObjects.values();
             for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                 CopyNumber o =  (CopyNumber)iterator.next();
                 allSNPProbeIDs.add(o.getSnpProbesetId());
             }
 
             executeGeneAnnotationQuery(allSNPProbeIDs);
             ThreadController.sleepOnEvents(annotationEventList);
 
             // by now CopyNumberObjects and annotations would have populated
             Collection c = cghObjects.values();
             for (Iterator iterator = c.iterator(); iterator.hasNext();) {
                 CopyNumber obj =  (CopyNumber)iterator.next();
                 if (obj.getSnpProbesetId() != null) {
                     obj.setAnnotations((CopyNumber.SNPAnnotation)annotations.get(obj.getSnpProbesetId()));
                 }
             }
 
             logger.debug("Annotations Retrieved");
             Object[]objs = (cghObjects.values().toArray());
             CopyNumber[] results = new CopyNumber[objs.length];
             for (int j = 0; j < objs.length; j++) {
                 results[j] = (CopyNumber) objs[j];
             }
             return results;
 
         }
 
 
         ResultSet[] executeSampleQuery( final Collection allSNPProbeIDs, final ComparativeGenomicQuery cghQuery)
         throws Exception {
             logger.debug("Total Number Of SNP_PROBES:" + allSNPProbeIDs.size());
             System.out.println("Total Number Of SNP_PROBES:" + allSNPProbeIDs.size());
             executeQuery(ArrayGenoAbnFact.SNP_PROBESET_ID, allSNPProbeIDs, ArrayGenoAbnFact.class, cghQuery);
 
             ThreadController.sleepOnEvents(factEventList);
             executeGeneAnnotationQuery(allSNPProbeIDs);
             ThreadController.sleepOnEvents(annotationEventList);
 
             // by now CopyNumberObjects and annotations would have populated
             Object[]objs = (cghObjects.values().toArray());
             CopyNumber[] results = new CopyNumber[objs.length];
             for (int i = 0; i < objs.length; i++) {
                 CopyNumber obj = (CopyNumber) objs[i];
                 if (obj.getSnpProbesetId() != null) {
                     obj.setAnnotations((CopyNumber.SNPAnnotation)annotations.get(obj.getSnpProbesetId()));
                 }
                 results[i] = obj;
             }
             return results;
         }
 
         void addToResults(Collection factObjects) {
            for (Iterator iterator = factObjects.iterator(); iterator.hasNext();) {
                 ArrayGenoAbnFact factObj = (ArrayGenoAbnFact) iterator.next();
                 CopyNumber resObj = new CopyNumber();
                 copyTo(resObj, factObj);
                 cghObjects.put(resObj.getAgaID(), resObj);
                 resObj = null;
             }
         }
         private void copyTo(CopyNumber resultObj, ArrayGenoAbnFact factObj) {
             resultObj.setAgaID(factObj.getAgaId());
             resultObj.setAgeGroup(factObj.getAgeGroup());
             resultObj.setBiospecimenId(factObj.getBiospecimenId());
             resultObj.setChannelRatio(factObj.getChannelRatio());
             resultObj.setCopyNumber(factObj.getCopyNumber());
             resultObj.setCytoband(factObj.getCytoband());
             resultObj.setDiseaseType(factObj.getDiseaseType());
             resultObj.setGenderCode(factObj.getGenderCode());
             resultObj.setLoh(factObj.getLoh());
             resultObj.setLossGain(factObj.getLossGain());
             resultObj.setSampleId(factObj.getSampleId());
             resultObj.setSnpProbesetId(factObj.getSnpProbesetId());
             resultObj.setSnpProbesetName(factObj.getSnpProbesetName());
             resultObj.setSurvivalLengthRange(factObj.getSurvivalLengthRange());
             resultObj.setTimecourseId(factObj.getTimecourseId());
             resultObj.setPhysicalPosition(factObj.getPhysicalPosition());
             resultObj.setChromosome(factObj.getChromosome());
         }
     }
 }
 
 
