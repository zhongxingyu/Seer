 package gov.nih.nci.caintegrator.studyQueryService.germline;
 
 import gov.nih.nci.caintegrator.domain.finding.bean.Finding;
 import gov.nih.nci.caintegrator.domain.finding.variation.snpFrequency.bean.SNPFrequencyFinding;
 import gov.nih.nci.caintegrator.domain.study.bean.Population;
 import gov.nih.nci.caintegrator.domain.annotation.gene.bean.GeneBiomarker;
 import gov.nih.nci.caintegrator.domain.annotation.snp.bean.SNPAnnotation;
 import gov.nih.nci.caintegrator.studyQueryService.dto.FindingCriteriaDTO;
 import gov.nih.nci.caintegrator.studyQueryService.dto.germline.SNPFrequencyFindingCriteriaDTO;
 import gov.nih.nci.caintegrator.util.HQLHelper;
 import gov.nih.nci.caintegrator.util.ArithematicOperator;
 
 import java.util.*;
 import java.text.MessageFormat;
 
 import org.hibernate.*;
 import org.hibernate.criterion.Restrictions;
 
 /**
  * Author: Ram Bhattaru
  * Date:   Jul 17, 2006
  * Time:   5:52:11 PM
  */
 public class SNPFrequencyFindingHandler extends FindingsHandler {
     protected Collection<? extends Finding> getMyFindings(FindingCriteriaDTO critDTO, Set<String> snpAnnotationIDs, Session session) {
 
         List<SNPFrequencyFinding>  frequencyFindings = Collections.synchronizedList(
                                                    new ArrayList<SNPFrequencyFinding>());
         int fromIndex = 0;
         int toIndex = fromIndex + BATCH_OBJECT_INCREMENT;
         Collection batchFindings = new ArrayList<SNPFrequencyFinding>();
         do {
             batchFindings =
                     getMyFindings(critDTO, snpAnnotationIDs, session, fromIndex, toIndex);
             frequencyFindings.addAll(batchFindings);
             fromIndex =  toIndex;
             toIndex = fromIndex + BATCH_OBJECT_INCREMENT;;
             System.out.println("Start Index:"+ fromIndex + " End Index:" + toIndex +
                     " Findings Retrieved: " + frequencyFindings.size());
         }  while(batchFindings.size() > BATCH_OBJECT_INCREMENT);
 
         System.out.println("TOTAL frequencyFindings retrieved: " + frequencyFindings.size());
        return frequencyFindings;  
     }
 
     protected Collection<? extends Finding> getMyFindings(FindingCriteriaDTO critDTO, Set<String> snpAnnotationIDs, Session session, int startIndex, int endIndex) {
 
         List<SNPFrequencyFinding>  snpFrequencyFindings =
                 Collections.synchronizedList(new ArrayList<SNPFrequencyFinding>());
 
         /* if AnnotationCriteria results in no SNPs then return no findings */
         if (snpAnnotationIDs != null && snpAnnotationIDs.size() == 0)
             return snpFrequencyFindings;
 
         final StringBuffer targetHQL = new StringBuffer(
                         " FROM SNPFrequencyFinding "+ TARGET_FINDING_ALIAS +
                         " {0} {1} WHERE {2} {3} ");
 
         /*  if AnnotationCriteria resulted in some SNPs:  */
         if (snpAnnotationIDs != null && snpAnnotationIDs.size() > 0) {
             ArrayList arrayIDs = new ArrayList(snpAnnotationIDs);
             for (int i = 0; i < arrayIDs.size();) {
                 StringBuffer hql = new StringBuffer("").append(targetHQL);
                 Collection values = new ArrayList();
                 int begIndex = i;
                 i += IN_PARAMETERS ;
                 int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                 values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                 Collection<SNPFrequencyFinding> batchFindings = executeTargetFindingQuery(
                         critDTO, values, session, hql, startIndex, endIndex);
                 snpFrequencyFindings .addAll(batchFindings);
                 if (snpFrequencyFindings.size() > (BATCH_OBJECT_INCREMENT + 1))
                     return snpFrequencyFindings.subList(0, BATCH_OBJECT_INCREMENT + 1);
             }
         }
         else { /* means no AnnotationCriteria was specified in the FindingCriteriaDTO  */
             Collection<SNPFrequencyFinding> findings = executeTargetFindingQuery(
                     critDTO, null, session, targetHQL, startIndex, endIndex);
             snpFrequencyFindings.addAll(findings);
         }
 
         return snpFrequencyFindings ;
     }
 
     protected Collection<SNPFrequencyFinding> executeTargetFindingQuery(FindingCriteriaDTO critDTO, Collection<String> snpAnnotationIDs, Session session, StringBuffer targetHQL, int start, int end) {
          final HashMap params = new HashMap();
          SNPFrequencyFindingCriteriaDTO findingCritDTO = (SNPFrequencyFindingCriteriaDTO) critDTO;
 
         /* 1. Include Annotation Criteria in TargetFinding query   */
          StringBuffer snpAnnotJoin = new StringBuffer("");
          StringBuffer snpAnnotCond = new StringBuffer("");
          if (snpAnnotationIDs != null) {
             appendAnnotationCriteriaHQL(snpAnnotationIDs, snpAnnotJoin, snpAnnotCond, params);
          }
 
          /*  3. Handle population & Study criteria  */
          String populationJoin = "";
          String populationCond = "";
          List<Population> populationList = handlePopulationCriteria(findingCritDTO, session);
          if (populationList.size() > 0) {
             populationJoin = " LEFT JOIN FETCH " + TARGET_FINDING_ALIAS + ".population ";
             populationCond =  TARGET_FINDING_ALIAS + ".population IN (:populationList) AND ";
             params.put("populationList", populationList);
          }
 
          String hql  = MessageFormat.format(targetHQL.toString(), new Object[] {
                             snpAnnotJoin.toString(), populationJoin, snpAnnotCond.toString(), populationCond });
 
 //   /*      /* we need to add AND before adding SNPFrequencyFindingAttriuteCrit.  But prior ot that make
 //            sure there was no trailing AND already */
 //         String tempHQL = HQLHelper.removeTrailingToken(new StringBuffer(hql), "AND");
 //
 //*/
           StringBuffer formattedTargetHQL = new StringBuffer(hql);
          /*  2. Handle SNPFrequencyFinding Attributes Criteria itself  and populate targetHQL/params */
          addSNPFrequencyFindingAttriuteCrit(findingCritDTO, formattedTargetHQL, params);
 
          String andRemovedHQL = HQLHelper.removeTrailingToken(new StringBuffer(formattedTargetHQL), "AND");
          String finalHQL = HQLHelper.removeTrailingToken(new StringBuffer(andRemovedHQL), "WHERE");
          Query q = session.createQuery(finalHQL);
          HQLHelper.setParamsOnQuery(params, q);
          q.setFirstResult(start);
          q.setMaxResults(end - start);
          List<SNPFrequencyFinding> findings = q.list();
          return findings;
        }
     private List<Population> handlePopulationCriteria(SNPFrequencyFindingCriteriaDTO findingCritDTO, Session session) {
         String[] populationNames = findingCritDTO.getPopulationNames();
         String studyName = findingCritDTO.getStudyName();
         String sponsorIdentifier = findingCritDTO.getSponsorStudyIdentifier();
 
         if ((populationNames == null || populationNames.length < 1) &&
                 (studyName == null || studyName.length() < 1) &&
                 (sponsorIdentifier == null || sponsorIdentifier.length() < 1) ) {
             return new ArrayList<Population>();
         }
 
         Criteria popCrit = session.createCriteria(Population.class);
         if (populationNames != null && populationNames.length > 0) {
             popCrit.add(Restrictions.in("name", populationNames));
         }
         boolean appendStudy = isAddStudyCriteria(findingCritDTO);
         if (appendStudy) {
             Criteria studyCrit = popCrit.createCriteria("studyCollection");
             addStudyCriteria(findingCritDTO, studyCrit, session);
         }
         List<Population> populationList = popCrit.list();
         return populationList;
     }
 
     private void addStudyCriteria( SNPFrequencyFindingCriteriaDTO findingCritDTO, Criteria studyCrit, Session session) {
         String studyName = findingCritDTO.getStudyName();
         String sponsorIdentifier = findingCritDTO.getSponsorStudyIdentifier();
         if (studyName != null && studyName.length() > 0) {
             studyCrit.add(Restrictions.eq("name", studyName));
         }
         if (sponsorIdentifier != null && sponsorIdentifier.length() > 0) {
             studyCrit.add(Restrictions.eq("sponsorStudyIdentifier", sponsorIdentifier));
         }
     }
     private boolean isAddStudyCriteria( SNPFrequencyFindingCriteriaDTO findingCritDTO) {
             String studyName = findingCritDTO.getStudyName();
             String sponsorIdentifier = findingCritDTO.getSponsorStudyIdentifier();
             if ((studyName != null && studyName.length() > 0) ||
                     (sponsorIdentifier != null && sponsorIdentifier.length() > 0) )
                return true;
 
             return false;
     }
     private void addSNPFrequencyFindingAttriuteCrit(SNPFrequencyFindingCriteriaDTO crit, StringBuffer hql, HashMap params) {
 
         Float hardyWeinbergPValue  = crit.getHardyWeinbergPValue();
         ArithematicOperator hardyWeinbergPValueOperator = crit.getHardyWeinbergPValueOperator();
 
         Float minorAlleleFreq = crit.getMinorAlleleFrequency();
         ArithematicOperator minorAlleleOperator = crit.getMinorAlleleOperator();
 
         Double completionRate = crit.getCompletionRate();
         ArithematicOperator completeRateOperator = crit.getCompleteRateOperator();
 
         Integer heterCount = crit.getHeterozygoteCount();
         Integer missingAlleleCnt = crit.getMissingAlleleCount();
         Integer missingGenotypeCnt = crit.getMissingGenotypeCount();
         String otherAllele = crit.getOtherAllele();
         Integer otherAllelCnt = crit.getOtherAlleleCount();
         Integer otherHomogyoteCnt = crit.getOtherHomogygoteCount();
         String referenceAllele = crit.getReferenceAllele();
         Integer referenceAlleleCnt = crit.getReferenceAlleleCount();
         Integer referenceHomogzoteCnt = crit.getReferenceHomogyzoteCount();
 
 
         if (completionRate != null) {
             if (completeRateOperator == null) completeRateOperator = ArithematicOperator.EQ; // default
             String clause =  TARGET_FINDING_ALIAS + ".completionRate {0} :completionRate AND ";
             String condition = HQLHelper.prepareCondition(completeRateOperator);
             String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
             hql.append(formattedClause);
             params.put("completionRate", completionRate);
         }
 
         if (hardyWeinbergPValue != null) {
             if (hardyWeinbergPValueOperator == null) hardyWeinbergPValueOperator = ArithematicOperator.EQ; // default
             String clause =  TARGET_FINDING_ALIAS + ".hardyWeinbergPValue {0} :hardyWeinbergPValue AND ";
             String condition = HQLHelper.prepareCondition(hardyWeinbergPValueOperator);
             String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
             hql.append(formattedClause);
             params.put("hardyWeinbergPValue", hardyWeinbergPValue);
         }
 
         if (minorAlleleFreq != null) {
             if (minorAlleleOperator == null) minorAlleleOperator = ArithematicOperator.EQ; // default
             String clause =  TARGET_FINDING_ALIAS + ".minorAlleleFrequency  {0} :minorAlleleFrequency  AND ";
             String condition = HQLHelper.prepareCondition(minorAlleleOperator);
             String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
             hql.append(formattedClause);
             params.put("minorAlleleFrequency", minorAlleleFreq);
         }
 
         if (heterCount != null) {
            hql.append( TARGET_FINDING_ALIAS + ".heterozygoteCount = :heterCount AND ");
            params.put("heterCount", heterCount);
         }
 
         if (missingAlleleCnt != null) {
             hql.append( TARGET_FINDING_ALIAS + ".missingAlleleCount = :missingAlleleCnt AND ");
             params.put("missingAlleleCnt", missingAlleleCnt);
         }
         if (missingGenotypeCnt != null) {
             hql.append( TARGET_FINDING_ALIAS + ".missingAlleleCount = :missingGenotypeCnt AND ");
             params.put("missingGenotypeCount", missingGenotypeCnt);
         }
         if (otherAllele != null) {
             hql.append( TARGET_FINDING_ALIAS + ".missingAlleleCount = :otherAllele AND ");
             params.put("otherAllele", otherAllele);
         }
         if (otherAllelCnt != null) {
             hql.append( TARGET_FINDING_ALIAS + ".otherAlleleCount = :otherAllelCnt AND ");
             params.put("otherAllelCnt", otherAllelCnt);
         }
         if (otherHomogyoteCnt != null) {
             hql.append( TARGET_FINDING_ALIAS + ".otherHomogygoteCount = :otherHomogyoteCnt AND ");
             params.put("otherHomogyoteCnt", otherHomogyoteCnt);
         }
         if (referenceAllele != null) {
             hql.append( TARGET_FINDING_ALIAS + ".referenceAllele = :referenceAllele AND ");
             params.put("referenceAllele", referenceAllele);
         }
         if (referenceAlleleCnt != null) {
             hql.append( TARGET_FINDING_ALIAS + ".referenceAlleleCount = :referenceAlleleCnt AND ");
             params.put("referenceAlleleCnt", referenceAlleleCnt);
         }
         if (referenceHomogzoteCnt != null) {
             hql.append( TARGET_FINDING_ALIAS + ".referenceHomogyzoteCount = :referenceHomogzoteCnt AND ");
             params.put("referenceHomogzoteCnt", referenceHomogzoteCnt);
         }
 
     }
     protected void initializeProxies(Collection<? extends Finding> findings, Session session) {
 
         /* first initialize SNPAnnotations */
         Collection<String> snpAnnotsIDs = new ArrayList<String>();
         Collection<Long> populationIDs = new ArrayList<Long>();
         for (Iterator<? extends Finding> iterator = findings.iterator(); iterator.hasNext();) {
                 SNPFrequencyFinding finding =  (SNPFrequencyFinding) iterator.next();
                 snpAnnotsIDs.add(finding.getSnpAnnotation().getId());
                 populationIDs.add(finding.getPopulation().getId());
         }
         if(snpAnnotsIDs.size() >0) {
             Criteria snpAnnotcrit = session.createCriteria(SNPAnnotation.class).
             setFetchMode("geneBiomarkerCollection", FetchMode.EAGER).
             add(Restrictions.in("id", snpAnnotsIDs));
             snpAnnotcrit.list();
         }
 
         /* Second initialize Population */
         if (populationIDs.size() > 0) {
             Criteria populationCrit = session.createCriteria(Population.class).
                                 add(Restrictions.in("id", populationIDs));
             populationCrit.list();
         }
     }
 }
