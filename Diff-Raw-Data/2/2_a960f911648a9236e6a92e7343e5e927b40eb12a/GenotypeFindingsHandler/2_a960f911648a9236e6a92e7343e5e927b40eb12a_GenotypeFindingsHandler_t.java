 package gov.nih.nci.caintegrator.studyQueryService.germline;
 
 import gov.nih.nci.caintegrator.domain.finding.bean.Finding;
 import gov.nih.nci.caintegrator.domain.finding.variation.germline.bean.GenotypeFinding;
 import gov.nih.nci.caintegrator.domain.study.bean.Specimen;
 import gov.nih.nci.caintegrator.domain.annotation.snp.bean.SNPAnnotation;
 import gov.nih.nci.caintegrator.studyQueryService.dto.FindingCriteriaDTO;
 import gov.nih.nci.caintegrator.studyQueryService.dto.germline.GenotypeFindingCriteriaDTO;
 import gov.nih.nci.caintegrator.studyQueryService.dto.study.StudyParticipantCriteria;
 import gov.nih.nci.caintegrator.util.ArithematicOperator;
 import gov.nih.nci.caintegrator.util.HQLHelper;
 
 import java.util.*;
 import java.text.MessageFormat;
 
 import org.hibernate.*;
 import org.hibernate.criterion.Restrictions;
 
 
 /**
  * Author: Ram Bhattaru
  * Date:   Jul 5, 2006
  * Time:   3:08:00 PM
  */
 public class GenotypeFindingsHandler extends FindingsHandler {
     protected Collection<? extends Finding> getMyFindings(FindingCriteriaDTO critDTO, Set<String> snpAnnotationIDs,
                                                           final Session session) {
         List<GenotypeFinding>  genotypeFindings = Collections.synchronizedList(
                                                    new ArrayList<GenotypeFinding>());
         int fromIndex = 0;
         int toIndex = fromIndex + BATCH_OBJECT_INCREMENT;
         Collection batchFindings = new ArrayList<GenotypeFinding>();
         do {
             batchFindings =
                     getMyFindings(critDTO, snpAnnotationIDs, session, fromIndex, toIndex);
             genotypeFindings.addAll(batchFindings);
             fromIndex =  toIndex;
             toIndex = fromIndex + BATCH_OBJECT_INCREMENT;;
             System.out.println("Start Index:"+ fromIndex + " End Index:" + toIndex +
                     " Findings Retrieved: " + genotypeFindings.size());
         }  while(batchFindings.size() > BATCH_OBJECT_INCREMENT);
 
         return genotypeFindings;
     }
     protected Collection<? extends Finding> getMyFindings(FindingCriteriaDTO critDTO, Set<String> snpAnnotationIDs,
                                                           final Session session, final int startIndex, final int endIndex) {
 
         List<GenotypeFinding>  genotypeFindings = Collections.synchronizedList(
                                                    new ArrayList<GenotypeFinding>());
         /* if AnnotationCriteria results in no SNPs then return no findings */
         if (snpAnnotationIDs != null && snpAnnotationIDs.size() == 0)
             return genotypeFindings;
 
         StringBuffer targetHQL = new StringBuffer(
                 " FROM GenotypeFinding " + TARGET_FINDING_ALIAS + " {0} WHERE {1} ") ;
 
        /* 2. Add hql to handle AnnotationCriteria (using the SNPAnnotatiosIDs passed in) */
         if (snpAnnotationIDs != null && snpAnnotationIDs.size() > 0) {
               ArrayList arrayIDs = new ArrayList(snpAnnotationIDs);
               for (int i = 0; i < arrayIDs.size();) {
                   StringBuffer hql = new StringBuffer("").append(targetHQL);
                   Collection values = new ArrayList();
                   int begIndex = i;
                   i += IN_PARAMETERS ;
                   int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                   values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                   Collection<GenotypeFinding> batchFindings = executeTargetFindingQuery(
                           critDTO, values, session, hql, startIndex, endIndex);
                   genotypeFindings.addAll(batchFindings);
                   if (genotypeFindings.size() > BATCH_OBJECT_INCREMENT + 1)
                       return genotypeFindings.subList(startIndex, BATCH_OBJECT_INCREMENT + 1);
               }
           }  else { /* means no AnnotationCriteria was specified in the FindingCriteriaDTO  */
              Collection<GenotypeFinding> findings = executeTargetFindingQuery(
                     critDTO, null, session, targetHQL, startIndex, endIndex);
               genotypeFindings.addAll(findings);
         }
 
         return genotypeFindings;
     }
 
     protected Collection<GenotypeFinding> executeTargetFindingQuery(FindingCriteriaDTO critDTO, Collection<String> snpAnnotationIDs, Session session, StringBuffer targetHQL, int start, int end) {
         GenotypeFindingCriteriaDTO findingCritDTO = (GenotypeFindingCriteriaDTO) critDTO;
         HashMap params = new HashMap();
 
         List<GenotypeFinding>  genotypeFindings = Collections.synchronizedList(
                                                    new ArrayList<GenotypeFinding>());
         /* 1. Include Annotation Criteria in TargetFinding query   */
         StringBuffer snpAnnotJoin = new StringBuffer("");
         StringBuffer snpAnnotCond = new StringBuffer("");
         if (snpAnnotationIDs != null) {
             appendAnnotationCriteriaHQL(snpAnnotationIDs, snpAnnotJoin, snpAnnotCond, params);
         }
 
         /* 2. Handle GenoType Attributes Criteria itself  and populate targetHQL/params */
         addGenoTypeAttributeCriteria(findingCritDTO, targetHQL, params);
 
         /* 3. Add hql to handle StudyParticipantCriteria */
         StudyParticipantCriteria spCrit = findingCritDTO.getStudyParticipantCriteria();
         if (spCrit != null) {
             /* Convert all StudyParticipantCriteria in to Specimens and then include in Finding criteria */
             List<String> specimenIDs = StudyParticipantCriteriaHandler.retrieveSpecimens(spCrit, session);
             if (specimenIDs != null && specimenIDs.size() == 0) {
                 /* means StudyParticipantCriteria did not select and Specimens  Hence return
                    no GenotypeFindings */
                 session.close();
                 return new ArrayList<GenotypeFinding>();
             }
             else if (specimenIDs != null && specimenIDs.size() > 0) {
                 /*  means  specimens.size() > 0.  So include this clause in findings criteria */
                   targetHQL.append(TARGET_FINDING_ALIAS + ".specimen.id IN  (:specimenIDs)  AND ");
                   ArrayList arrayIDs = new ArrayList(specimenIDs);
                   for (int i = 0; i < arrayIDs.size();) {
                      StringBuffer hql = new StringBuffer("").append(targetHQL);
                      Collection values = new ArrayList();
                      int begIndex = i;
                      i += IN_PARAMETERS ;
                      int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                      values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                      params.put("specimenIDs", values );
                      Collection<GenotypeFinding> findings = executeSplittedFindingQuery(session, hql, params, snpAnnotJoin, snpAnnotCond, start, end);
                      genotypeFindings.addAll(findings);
                      if (genotypeFindings.size() > (BATCH_OBJECT_INCREMENT + 1))
                       return genotypeFindings.subList(start, BATCH_OBJECT_INCREMENT);
                   }
 
             } else {  /* specimenIDs == null.  Meaning that no StudyParticipantCriteria attributes
                  are mentioned. So ignore  StudyParticipantCriteria and do not include Specimen clause
                  to Finding Criteria */
                  Collection<GenotypeFinding> findings  =
                          executeSplittedFindingQuery(session, targetHQL, params,
                                             snpAnnotJoin, snpAnnotCond, start, end);
                  genotypeFindings.addAll(findings);
             }
 
             return genotypeFindings;
        }
 
        Collection<GenotypeFinding> findings = executeSplittedFindingQuery(session, targetHQL, params,
                                                         snpAnnotJoin, snpAnnotCond, start, end);
        return findings;
     }
 
     private Collection<GenotypeFinding> executeSplittedFindingQuery (Session session, StringBuffer hql, HashMap params,
                     StringBuffer snpAnnotJoin, StringBuffer snpAnnotCond, int start, int end ) {
 
          String formatedhql  = MessageFormat.format(hql.toString(), new Object[] {
                         snpAnnotJoin.toString(), snpAnnotCond.toString()} );
          String tempHQL = HQLHelper.removeTrailingToken(new StringBuffer(formatedhql), "AND");
          String finalHQL = HQLHelper.removeTrailingToken(new StringBuffer(tempHQL), "WHERE");
          Query q = session.createQuery(finalHQL);
          HQLHelper.setParamsOnQuery(params, q);
          q.setFirstResult(start);
          q.setMaxResults(end - start);
          List<GenotypeFinding> findings = q.list();
          return findings;
     }
 
     private void addGenoTypeAttributeCriteria(GenotypeFindingCriteriaDTO crit, StringBuffer hql, HashMap params) {
         Float score = crit.getQualityScore();
         String status = crit.getQCStatus();
         ArithematicOperator oper =
            (crit.getOperatorType() == null) ? ArithematicOperator.EQ : crit.getOperatorType();
 
         if (status != null) {
           hql.append(TARGET_FINDING_ALIAS + ".status = :status AND ");
            params.put("status", status);
         }
 
         if (score != null) {
             String clause =  TARGET_FINDING_ALIAS + ".qualityScore {0} :score AND ";
             String condition = HQLHelper.prepareCondition(oper);
             String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
             hql.append(formattedClause);
             params.put("score", score);
         }
     }
 
     protected void initializeProxies(Collection<? extends Finding> findings, Session session) {
 
         /* 1. initialize SNPAnnotations */
         Collection<String> snpAnnotsIDs = new HashSet<String>();
         for (Iterator<? extends Finding> iterator = findings.iterator(); iterator.hasNext();) {
                 GenotypeFinding finding =  (GenotypeFinding) iterator.next();
                 snpAnnotsIDs.add(finding.getSnpAnnotation().getId());
         }
         if (snpAnnotsIDs.size() > 0) {
               ArrayList arrayIDs = new ArrayList(snpAnnotsIDs );
               for (int i = 0; i < arrayIDs.size();) {
                   Collection values = new ArrayList();
                   int begIndex = i;
                   i += IN_PARAMETERS ;
                   int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                   values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                   Criteria crit = session.createCriteria(SNPAnnotation.class).add(Restrictions.in("id", values));
                   crit.list();
               }
         }
 
         /* 2. initialize Specimens along with associated StudyParticipants */
         Collection<String> specimenIDs = new HashSet<String>();
         for (Iterator<? extends Finding> iterator = findings.iterator(); iterator.hasNext();) {
             GenotypeFinding finding =  (GenotypeFinding) iterator.next();
             specimenIDs.add(finding.getSpecimen().getId());
         }
         if (specimenIDs.size() > 0)  {
             ArrayList arrayIDs = new ArrayList(specimenIDs );
             for (int i = 0; i < arrayIDs.size();) {
                 Collection values = new ArrayList();
                 int begIndex = i;
                 i += IN_PARAMETERS ;
                 int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                 values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                 Criteria specimenCrit = session.createCriteria(Specimen.class).setFetchMode("studyParticipant", FetchMode.EAGER)
                                     .add(Restrictions.in("id", values));
                 specimenCrit .list();
             }
         }
     }
 }
