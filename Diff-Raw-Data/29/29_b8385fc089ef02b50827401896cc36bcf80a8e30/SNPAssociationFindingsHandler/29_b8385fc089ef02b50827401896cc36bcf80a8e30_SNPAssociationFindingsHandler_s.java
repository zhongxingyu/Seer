 package gov.nih.nci.caintegrator.studyQueryService.germline;
 
 import gov.nih.nci.caintegrator.domain.analysis.snp.bean.OddsRatio;
 import gov.nih.nci.caintegrator.domain.analysis.snp.bean.SNPAssociationAnalysis;
 import gov.nih.nci.caintegrator.domain.analysis.snp.bean.SNPAssociationFinding;
 import gov.nih.nci.caintegrator.domain.annotation.snp.bean.SNPAnnotation;
 import gov.nih.nci.caintegrator.domain.annotation.gene.bean.GeneBiomarker;
 import gov.nih.nci.caintegrator.domain.finding.bean.Finding;
 import gov.nih.nci.caintegrator.studyQueryService.dto.FindingCriteriaDTO;
 import gov.nih.nci.caintegrator.studyQueryService.dto.study.StudyCriteria;
 import gov.nih.nci.caintegrator.studyQueryService.dto.annotation.AnnotationCriteria;
 import gov.nih.nci.caintegrator.studyQueryService.dto.germline.*;
 import gov.nih.nci.caintegrator.util.ArithematicOperator;
 import gov.nih.nci.caintegrator.util.HQLHelper;
 
 import java.util.*;
 import java.text.MessageFormat;
 
 import org.hibernate.*;
 import org.hibernate.criterion.Restrictions;
 
 /**
  * Author: Ram Bhattaru
  * Date:   Jul 21, 2006
  * Time:   2:20:11 PM
  */
 
 public class SNPAssociationFindingsHandler extends FindingsHandler {
 
     protected Class getTargeFindingType() {
          return SNPAssociationFinding.class;
     }
 
    protected List<? extends Finding> getConcreteTypedFindingList() {
        return new ArrayList<SNPAssociationFinding>();
    }
 
    protected Set getConcreteTypedFindingSet() {
        return new HashSet<SNPAssociationFinding>();
    }
 
     protected StringBuffer getTargetFindingHQL() {
         StringBuffer targetHQL = new StringBuffer(
                                  " FROM SNPAssociationFinding " + TARGET_FINDING_ALIAS +
                                 // " JOIN "+ TARGET_FINDING_ALIAS + ".snpAnnotation snpAnnot " +
                                  " JOIN "+ TARGET_FINDING_ALIAS + ".snpAssociationAnalysis analysis " +
                                 " JOIN "+TARGET_FINDING_ALIAS + ".oddsRatioCollection oddsRatios " +
                                  " {0} {1} " + " WHERE {2} {3} {4} " );
 
 
         return targetHQL;
     }
 
     protected Collection<? extends Finding> executeFindingSetQuery(FindingCriteriaDTO critDTO, StringBuffer targetHQL,
                                                                Session session, int start, int end ) throws Exception {
 
        SNPAssociationFindingCriteriaDTO  findingCritDTO= (SNPAssociationFindingCriteriaDTO) critDTO;
        Collection<SNPAssociationFinding>  snpAssociationFindings = new ArrayList<SNPAssociationFinding>();
 
        AnnotationCriteria annotCrit = critDTO.getAnnotationCriteria();
        if ((annotCrit != null) && isOnlyPanelCriteria(annotCrit))
             return executePanelOnlySearch(critDTO, session, start, end);
 
        HashMap params = new HashMap();
        StringBuffer snpAnnotCond = new StringBuffer();
 
        /* 0. if AnnotationCrit is specified, then append required HQL (to snpAnnotCondition) for handling AnnotationCrit*/
        appendAnnotationCritHQL(critDTO, params, snpAnnotCond);
 
        /* 1.  Include SNPAssociationFinding attribute criteria in  TargetFinding query */
        StringBuffer myAttributeHql = new StringBuffer("");
        addSNPAssociationFindingAttriuteCrit(findingCritDTO, myAttributeHql, params);
 
        /* 2. Include HQL to handle Annotation Criteria to snpAnnotJoin & snpAnnotCond */
        StringBuffer snpAnnotJoin = new StringBuffer("");
 
        /* 3. Include SNPAnalysisGroup Criteria in TargetFinding query by converting & adding to AnalysisGroupCriteria */
        handleAnalysisGroupCriteria(findingCritDTO, session);
 
        /* 4. Include SNPAssociationAnalysisCriteria Criteria in TargetFinding query */
        HashMap h = applySnpassociationAnalysisCriteria(findingCritDTO, params);
        String analysisJoin = (String) h.get("ANALYSIS_JOIN");
        String analysisCondition = h.get("ANALYSIS_COND").toString();
 
        /* 5. Assemble the Query with all criteria conditions together */
        String tmpHQL = MessageFormat.format(targetHQL.toString(), new Object[] {snpAnnotJoin.toString(), analysisJoin,
                                                      myAttributeHql, snpAnnotCond.toString(), analysisCondition});
 
        String hql= HQLHelper.removeTrailingToken(new StringBuffer(tmpHQL), "AND");
        String noORHQL = HQLHelper.removeTrailingToken(new StringBuffer(hql), "OR");
        String finalHQL = HQLHelper.removeTrailingToken(new StringBuffer(noORHQL), "WHERE");
 
        /* 6. Now execute the final TargetFinding query */
        Query q = session.createQuery(finalHQL);
        HQLHelper.setParamsOnQuery(params, q);
        q.setFirstResult(start);
        q.setMaxResults(end - start);
        Iterator triplets = q.list().iterator();
        while(triplets.hasNext()) {
            Object[] triplet = (Object[]) triplets.next();
            SNPAssociationFinding finding = (SNPAssociationFinding) triplet[0];
            //SNPAnnotation snpAnnot = (SNPAnnotation) triplet[1];
            //finding.setSnpAnnotation(snpAnnot);
            SNPAssociationAnalysis analysis = (SNPAssociationAnalysis) triplet[1];
            finding.setSnpAssociationAnalysis(analysis);
            snpAssociationFindings.add(finding);
        }
 
        return snpAssociationFindings ;
     }
 
     private void addSNPAssociationFindingAttriuteCrit(SNPAssociationFindingCriteriaDTO findingCritDTO,
                                                                             StringBuffer myHql,HashMap params) {
         StudyCriteria studyCrit = findingCritDTO.getStudyCriteria();
         Float pValue = findingCritDTO.getpValue();
         if (pValue != null) {
             ArithematicOperator pValueOP = findingCritDTO.getpValueOperator();
             if (pValueOP == null) pValueOP = ArithematicOperator.EQ; // (default)
             StringBuffer hql = new StringBuffer("");
             String condition = HQLHelper.prepareCondition(pValueOP);
             String clause = TARGET_FINDING_ALIAS + ".pvalue {0} :pValue AND ";
             String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
             hql.append(formattedClause);
             params.put("pValue", pValue);
             myHql.append(hql);
         }
 
         Integer rank = findingCritDTO.getRank();
         if (rank != null) {
             ArithematicOperator rankOP = findingCritDTO.getRankOperator();
             if (rankOP == null) rankOP = ArithematicOperator.EQ; // (default)
             StringBuffer hql = new StringBuffer("");
             String condition = HQLHelper.prepareCondition(rankOP);
             String clause = TARGET_FINDING_ALIAS + ".rank {0} :rank AND ";
             String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
             hql.append(formattedClause);
             params.put("rank", rank);
             myHql.append(hql);
         }
         if (studyCrit != null) {
             if (studyCrit.getId() != null) {
                 myHql.append( TARGET_FINDING_ALIAS + ".study.id = :studyId AND ");
                 params.put("studyId", studyCrit.getId());
             }
         }
     }
 
     private void handleAnalysisGroupCriteria(SNPAssociationFindingCriteriaDTO findingCritDTO, Session session) {
         AnalysisGroupCriteria groupCrit = findingCritDTO.getAnalysisGroupCriteria();
         StudyCriteria studyCrit = findingCritDTO.getStudyCriteria();
         if (groupCrit != null) {
             String[] names = groupCrit.getNames();
             if (names != null && names.length > 0) {
                Criteria c = session.createCriteria(SNPAssociationAnalysis.class).
                                     createAlias("analysisGroupCollection", "analysisGroup").
                                     add(Restrictions.in("analysisGroup.name", names));
 
                List<SNPAssociationAnalysis> assocAnalysisObjs = c.list();
 
                /* 1. Convert these SNPAssociationAnalysis in to SNPAssociationAnalysisCriteria */
                Collection analysisCrits = new ArrayList<SNPAssociationAnalysisCriteria>();
                for (int i = 0; i < assocAnalysisObjs.size(); i++) {
                     SNPAssociationAnalysis assocAnalysisObj =  assocAnalysisObjs.get(i);
                     SNPAssociationAnalysisCriteria methodAndNameCrit =
                                                     new SNPAssociationAnalysisCriteria(studyCrit.getId());
                     methodAndNameCrit.setMethods(assocAnalysisObj.getMethods());
                     methodAndNameCrit.setName(assocAnalysisObj.getName());
                     analysisCrits.add(methodAndNameCrit);
                }
 
                /* 2. Now set these SNPAssociationAnalysisCriteria objects back to SNPAssociationFindingCriteriaDTO */
                Collection<SNPAssociationAnalysisCriteria> anaCrits =
                                                         findingCritDTO.getSnpAssociationAnalysisCriteriaCollection();
                if (anaCrits == null) {
                  /* means no SNPAssociationAnalysisCriteria was specified in the Query DTO
                     (SNPAssociationFindingCriteriaDTO) So intantiate one and add it to QueryDTO */
                   anaCrits = new ArrayList<SNPAssociationAnalysisCriteria>();
                   findingCritDTO.setSnpAssociationAnalysisCriteriaCollection(anaCrits);
                }
                anaCrits.addAll(analysisCrits);
             }
         }
     }
 
     /**
      * This method returns SNPAssociationAnalysis Condition & Join in a hashMap
      * @param findingCritDTO
      * @param params
      * @return Map containing Condition & Join
      */
     private HashMap applySnpassociationAnalysisCriteria(SNPAssociationFindingCriteriaDTO findingCritDTO,HashMap params){
         StringBuffer analysisCond = new StringBuffer("");
         Collection<SNPAssociationAnalysisCriteria> anaCrits =
                                                           findingCritDTO.getSnpAssociationAnalysisCriteriaCollection();
         HashMap h = new HashMap();
         if (anaCrits != null && anaCrits.size() > 0) {
             int suffix = 0;
             for (SNPAssociationAnalysisCriteria anaCritObj : anaCrits) {
                 String methods = anaCritObj.getMethods();
                 List<String> analysisCodes = anaCritObj.getAnalysisCodes();
                 if ((methods == null) && (analysisCodes == null)) continue;
                 StringBuffer methodTrailingAND = new StringBuffer("");
                 if (methods != null) {
                     String methodParam = "methods" + suffix;
                     StringBuffer tmpAnalysisCond = new StringBuffer("");
                     tmpAnalysisCond.append(TARGET_FINDING_ALIAS + ".snpAssociationAnalysis.methods = :{0} ");
                     analysisCond.append(MessageFormat.format(tmpAnalysisCond.toString(), new Object[]{methodParam}));
                     params.put(methodParam, methods);
                     methodTrailingAND.append(" AND ");
                 }
                 if (analysisCodes != null  && analysisCodes.size()> 0) {
                     analysisCond.append(methodTrailingAND);
                     String nameParam = "analysisCode" + suffix;
                     StringBuffer tmpAnalysisCond = new StringBuffer("");
                     tmpAnalysisCond.append(TARGET_FINDING_ALIAS + ".snpAssociationAnalysis.analysisCode IN (:{0}) ");
                     analysisCond.append(MessageFormat.format(tmpAnalysisCond.toString(), new Object[]{nameParam}));
                     params.put(nameParam, analysisCodes);
                 }
                 analysisCond.append(" OR ");
                 suffix++;
             }
             String analCondition = HQLHelper.removeTrailingToken(new StringBuffer(analysisCond), " OR");
             String andAppendedhql = (new StringBuffer(analCondition).append(" AND ")).toString();
             h.put("ANALYSIS_JOIN", new String(""));
             h.put("ANALYSIS_COND", andAppendedhql);
 
         } else  {
             h.put("ANALYSIS_JOIN", new String(""));
             h.put("ANALYSIS_COND", analysisCond);
         }
 
         return h;
     }
 
     protected void initializeProxies(Collection<? extends Finding> findings, Session session) {
         List<GeneBiomarker> gbObjs = new ArrayList<GeneBiomarker>();
        //List<OddsRatio> oddsRatioObjs = new ArrayList<OddsRatio>();
 
         /* initialize SNPAnnotations */
         Collection<Long> snpAnnotsIDs = new HashSet<Long>();
         Collection<Long> populationIDs = new HashSet<Long>();
         for (Iterator<? extends Finding> iterator = findings.iterator(); iterator.hasNext();) {
                 SNPAssociationFinding finding =  (SNPAssociationFinding) iterator.next();
                 snpAnnotsIDs.add(finding.getSnpAnnotation().getId());
                finding.getOddsRatioCollection().size();
         }
        //Hibernate.initialize(oddsRatioObjs);
        //oddsRatioObjs = null;
         if(snpAnnotsIDs.size() >0) {
               ArrayList arrayIDs = new ArrayList(snpAnnotsIDs);
               for (int i = 0; i < arrayIDs.size();) {
                   Collection values = new ArrayList();
                   int begIndex = i;
                   i += IN_PARAMETERS ;
                   int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                   values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                   Criteria snpAnnotcrit = session.createCriteria(SNPAnnotation.class).
                   setFetchMode("geneBiomarkerCollection", FetchMode.JOIN).
                   add(Restrictions.in("id", values));
                   snpAnnotcrit.list();
               }
         }
 
 //        Collection findingIDs = new HashSet();
 //        for (Iterator<? extends Finding> iterator = findings.iterator(); iterator.hasNext();) {
 //           SNPAssociationFinding finding = (SNPAssociationFinding) iterator.next();
 //           findingIDs.add(finding.getId());
 //           //gbObjs.addAll(finding.getSnpAnnotation().getGeneBiomarkerCollection());
 //        }
 //
 //        Criteria crit;
 //        ArrayList<String> arrayIDs = new ArrayList<String>(findingIDs);
 //        for (int i = 0; i < arrayIDs.size();) {
 //            List<String> values = new ArrayList<String>();
 //            int begIndex = i;
 //            i += 1000 ;
 //            int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
 //            values.addAll(arrayIDs.subList(begIndex,  lastIndex));
 //            crit = session.createCriteria(SNPAnnotation.class).
 //                                      createAlias("snpAssociationFindingCollection", "findings").
 //                                      setFetchMode("geneBiomarkerCollection", FetchMode.EAGER).
 //                                      add(Restrictions.in("findings.id", values));
 //            crit.list();
 //        }
 //        //Hibernate.initialize(gbObjs);
 //        //gbObjs = null;
     }
 
     protected StringBuffer addHQLForFindingAttributeCriteria(FindingCriteriaDTO crit, StringBuffer hql,
                                                              HashMap params, Session session)  {
 
         SNPAssociationFindingCriteriaDTO findingCritDTO = (SNPAssociationFindingCriteriaDTO)crit;
         /* 1. Include SNPAnalysisGroup crit in TargetFinding query by converting and adding to AnalysisGroupCriteria */
         handleAnalysisGroupCriteria(findingCritDTO, session);
         HashMap h = applySnpassociationAnalysisCriteria(findingCritDTO, params);
         String analysisJoin = " JOIN "+ TARGET_FINDING_ALIAS + ".snpAssociationAnalysis analysis ";
         String analysisCondition = h.get("ANALYSIS_COND").toString();
         addSNPAssociationFindingAttriuteCrit((SNPAssociationFindingCriteriaDTO) crit, hql, params);
         StringBuffer temp = new StringBuffer(MessageFormat.format( hql.toString(),
                                     new Object[] {"","",analysisCondition, analysisJoin}) );
         return temp;
      }
 
     protected Collection<? extends Finding> getFindingsFromResults(List results) {
         Collection<SNPAssociationFinding> snpAssociationFindings = new ArrayList<SNPAssociationFinding>(results.size());
         Iterator resultsIter = results.iterator();
         while(resultsIter.hasNext()) {
             Object[] triplet = (Object[]) resultsIter.next();
             SNPAssociationFinding finding = (SNPAssociationFinding) triplet[0];
             SNPAnnotation snpAnnot = (SNPAnnotation) triplet[3];
             finding.setSnpAnnotation(snpAnnot);
             SNPAssociationAnalysis analysis = (SNPAssociationAnalysis) triplet[1];
             finding.setSnpAssociationAnalysis(analysis);
             snpAssociationFindings.add(finding);
         }
         return snpAssociationFindings;
     }
 
     public Collection<? extends Finding> executePanelOnlySearch(FindingCriteriaDTO findingCritDTO, Session session, int start, int end) {
        AnnotationCriteria annotCrit = findingCritDTO.getAnnotationCriteria();
        Collection<? extends Finding> findings ;
        StringBuffer hql = new StringBuffer(" FROM SNPAssociationFinding {0}, SNPAssay s " +
                                          //   " JOIN " + TARGET_FINDING_ALIAS + ".snpAnnotation " +
                                              " {3} WHERE s.snpPanel.id = {1} AND " +
                                              TARGET_FINDING_ALIAS + ".snpAnnotation = s.snpAnnotation " +
                                              " AND {2} " );
 
        StringBuffer panelHQL = new StringBuffer ( MessageFormat.format( hql.toString(), new Object[] {
                                             TARGET_FINDING_ALIAS, annotCrit.getPanelCriteria().getSnpPanelID()}) );
 
        HashMap params = new HashMap();
        StringBuffer finalPanelHQL = addHQLForFindingAttributeCriteria(findingCritDTO, panelHQL, params,  session);
        StringBuffer finalHQLWithAND = new StringBuffer(
                                                 HQLHelper.removeTrailingToken(new StringBuffer(finalPanelHQL), "AND"));
        finalHQLWithAND.append(" AND ");
        String finalHQL = HQLHelper.removeTrailingToken(new StringBuffer(finalHQLWithAND), "AND");
 
        Query q = session.createQuery(finalHQL);
        HQLHelper.setParamsOnQuery(params, q);
        q.setFirstResult(start);
        q.setMaxResults(end - start); //RAM: 01/31/07
        List results = q.list();
        findings = getFindingsFromResultsForPanelSearch(results);
        return findings;
     }
 
     protected Collection<? extends Finding> getFindingsFromResultsForPanelSearch(List results) {
         Collection<SNPAssociationFinding> snpAssociationFindings = new ArrayList<SNPAssociationFinding>(results.size());
         Iterator resultsIter = results.iterator();
         while(resultsIter.hasNext()) {
             Object[] triplet = (Object[]) resultsIter.next();
             SNPAssociationFinding finding = (SNPAssociationFinding) triplet[0];
             //SNPAnnotation snpAnnot = (SNPAnnotation) triplet[1];
             //finding.setSnpAnnotation(snpAnnot);
             SNPAssociationAnalysis analysis = (SNPAssociationAnalysis) triplet[1];
             finding.setSnpAssociationAnalysis(analysis);
             snpAssociationFindings.add(finding);
         }
         return snpAssociationFindings;
     }
 }
