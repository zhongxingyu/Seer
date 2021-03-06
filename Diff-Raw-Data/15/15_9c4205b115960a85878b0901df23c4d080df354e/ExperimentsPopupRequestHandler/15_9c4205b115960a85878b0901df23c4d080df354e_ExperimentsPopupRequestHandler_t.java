 /*
  * Copyright 2008-2010 Microarray Informatics Team, EMBL-European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * For further details of the Gene Expression Atlas project, including source code,
  * downloads and documentation, please see:
  *
  * http://gxa.github.com/gxa
  */
 
 package uk.ac.ebi.gxa.requesthandlers.query;
 
 import ae3.dao.AtlasSolrDAO;
 import ae3.model.AtlasExperiment;
 import ae3.model.AtlasGene;
 import ae3.service.AtlasStatisticsQueryService;
 import ae3.service.structuredquery.Constants;
 import uk.ac.ebi.gxa.efo.Efo;
 import uk.ac.ebi.gxa.efo.EfoTerm;
 import uk.ac.ebi.gxa.properties.AtlasProperties;
 import uk.ac.ebi.gxa.requesthandlers.base.AbstractRestRequestHandler;
 import uk.ac.ebi.gxa.statistics.Attribute;
 import uk.ac.ebi.gxa.statistics.Experiment;
 import uk.ac.ebi.gxa.statistics.StatisticsQueryUtils;
 import uk.ac.ebi.gxa.statistics.StatisticsType;
 import uk.ac.ebi.gxa.utils.EscapeUtil;
 import uk.ac.ebi.microarray.atlas.model.ExpressionAnalysis;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.*;
 
 /**
  * @author pashky
  */
 public class ExperimentsPopupRequestHandler extends AbstractRestRequestHandler {
 
     private AtlasSolrDAO atlasSolrDAO;
     private Efo efo;
     private AtlasProperties atlasProperties;
     private AtlasStatisticsQueryService atlasStatisticsQueryService;
 
     public void setDao(AtlasSolrDAO atlasSolrDAO) {
         this.atlasSolrDAO = atlasSolrDAO;
     }
 
     public void setEfo(Efo efo) {
         this.efo = efo;
     }
 
     public void setAtlasProperties(AtlasProperties atlasProperties) {
         this.atlasProperties = atlasProperties;
     }
 
     public void setAtlasStatisticsQueryService(AtlasStatisticsQueryService atlasStatisticsQueryService) {
         this.atlasStatisticsQueryService = atlasStatisticsQueryService;
     }
 
     public Object process(HttpServletRequest request) {
         Map<String, Object> jsResult = new HashMap<String, Object>();
 
         String geneIdKey = request.getParameter("gene");
         String factor = request.getParameter("ef");
         String factorValue = request.getParameter("efv");
 
         if (geneIdKey != null && factor != null && factorValue != null) {
             boolean isEfo = Constants.EFO_FACTOR_NAME.equals(factor);
 
             jsResult.put("ef", factor);
             jsResult.put("eftext", atlasProperties.getCuratedEf(factor));
             jsResult.put("efv", factorValue);
 
             if (isEfo) {
                 EfoTerm term = efo.getTermById(factorValue);
                 if (term != null) {
                     jsResult.put("efv", term.getTerm());
                 }
             }
 
             AtlasSolrDAO.AtlasGeneResult result = atlasSolrDAO.getGeneById(geneIdKey);
             if (!result.isFound()) {
                 throw new IllegalArgumentException("Atlas gene " + geneIdKey + " not found");
             }
 
             AtlasGene gene = result.getGene();
 
             Map<String, Object> jsGene = new HashMap<String, Object>();
 
             jsGene.put("id", geneIdKey);
             jsGene.put("identifier", gene.getGeneIdentifier());
             jsGene.put("name", gene.getGeneName());
             jsResult.put("gene", jsGene);
 
             Long geneId = Long.parseLong(gene.getGeneId());
 
            List<Experiment> experiments = new ArrayList<Experiment>();
             if (isEfo) {
                experiments.addAll(atlasStatisticsQueryService.getExperimentsSortedByPvalueTRank(
                        Long.parseLong(gene.getGeneId()), StatisticsType.UP_DOWN, factor, factorValue, StatisticsQueryUtils.EFO, -1, -1));
             } else {
                 List<Attribute> scoringEfvsForGene = atlasStatisticsQueryService.getScoringEfvsForGene(geneId, StatisticsType.UP_DOWN);
 
                 for (Attribute attr : scoringEfvsForGene) {
                     if (!factor.equals(attr.getEf()))
                         continue;
                    experiments.addAll(atlasStatisticsQueryService.getExperimentsSortedByPvalueTRank(
                            Long.parseLong(gene.getGeneId()), StatisticsType.UP_DOWN, attr.getEf(), attr.getEfv(), !StatisticsQueryUtils.EFO, -1, -1));
                 }
             }
 
             Map<Long, Map<String, List<Experiment>>> exmap = new HashMap<Long, Map<String, List<Experiment>>>();
            for (Experiment experiment : experiments) {
                 Long expId = Long.parseLong(experiment.getExperimentId());
                 Map<String, List<Experiment>> efmap = exmap.get(expId);
                 if (efmap == null) {
                     exmap.put(expId, efmap = new HashMap<String, List<Experiment>>());
                 }
                 List<Experiment> list = efmap.get(experiment.getHighestRankAttribute().getEf());
                 if (list == null) {
                     efmap.put(experiment.getHighestRankAttribute().getEf(), list = new ArrayList<Experiment>());
                 }
 
                 list.add(experiment);
             }
 
 
             for (Map<String, List<Experiment>> ef : exmap.values()) {
                 for (List<Experiment> e : ef.values()) {
                     Collections.sort(e, new Comparator<Experiment>() {
                         public int compare(Experiment o1, Experiment o2) {
                             return o1.getpValTStatRank().compareTo(o2.getpValTStatRank());
                         }
                     });
                 }
             }
 
             @SuppressWarnings("unchecked")
 
             List<Map.Entry<Long, Map<String, List<Experiment>>>> exps =
                     new ArrayList<Map.Entry<Long, Map<String, List<Experiment>>>>(exmap.entrySet());
             Collections.sort(exps, new Comparator<Map.Entry<Long, Map<String, List<Experiment>>>>() {
                 public int compare(Map.Entry<Long, Map<String, List<Experiment>>> o1,
                                    Map.Entry<Long, Map<String, List<Experiment>>> o2) {
                     double minp1 = 1;
                     for (Map.Entry<String, List<Experiment>> ef : o1.getValue().entrySet()) {
                         minp1 = Math.min(minp1, ef.getValue().get(0).getpValTStatRank().getPValue());
                     }
                     double minp2 = 1;
                     for (Map.Entry<String, List<Experiment>> ef : o2.getValue().entrySet()) {
                         minp2 = Math.min(minp2, ef.getValue().get(0).getpValTStatRank().getPValue());
                     }
                     return minp1 < minp2 ? -1 : 1;
                 }
             });
 
             int numUp = 0, numDn = 0, numNo = 0;
 
             List<Map> jsExps = new ArrayList<Map>();
             for (Map.Entry<Long, Map<String, List<Experiment>>> e : exps) {
                 AtlasExperiment aexp = atlasSolrDAO.getExperimentById(e.getKey());
                 if (aexp != null) {
                     Map<String, Object> jsExp = new HashMap<String, Object>();
                     jsExp.put("accession", aexp.getAccession());
                     jsExp.put("name", aexp.getDescription());
                     jsExp.put("id", e.getKey());
 
                     boolean wasup = false;
                     boolean wasdn = false;
                     boolean wasno = false;
                     List<Map> jsEfs = new ArrayList<Map>();
                     for (Map.Entry<String, List<Experiment>> ef : e.getValue().entrySet()) {
                         Map<String, Object> jsEf = new HashMap<String, Object>();
                         jsEf.put("ef", ef.getKey());
                         jsEf.put("eftext", atlasProperties.getCuratedEf(ef.getKey()));
 
                         List<Map> jsEfvs = new ArrayList<Map>();
                         for (Experiment exp : ef.getValue()) {
                             Map<String, Object> jsEfv = new HashMap<String, Object>();
                             boolean isNo = ExpressionAnalysis.isNo(exp.getpValTStatRank().getPValue(), exp.getpValTStatRank().getTStatRank());
                             boolean isUp = ExpressionAnalysis.isUp(exp.getpValTStatRank().getPValue(), exp.getpValTStatRank().getTStatRank());
                             jsEfv.put("efv", exp.getHighestRankAttribute().getEfv());
                             jsEfv.put("isexp", isNo ? "no" : (isUp ? "up" : "dn"));
                             jsEfv.put("pvalue", exp.getpValTStatRank().getPValue());
                             jsEfvs.add(jsEfv);
 
                             if(isNo)
                                 wasno = true;
                             else {
                                 if (isUp) {
                                     wasup = true;
                                 }
                                 else {
                                     wasdn = true;
                                 }
                             }
                         }
                         jsEf.put("efvs", jsEfvs);
                         if(!jsEfvs.isEmpty())
                             jsEfs.add(jsEf);
                     }
                     jsExp.put("efs", jsEfs);
 
                     if (wasup) {
                         ++numUp;
                     }
                     if (wasdn) {
                         ++numDn;
                     }
                     if (wasno) {
                         ++numNo;
                     }
                     jsExps.add(jsExp);
                 }
             }
 
             jsResult.put("experiments", jsExps);
 
             String efv;
             if (isEfo) {
                efv = factorValue;
             } else {
                efv = EscapeUtil.encode(factor, factorValue);
             }
             long start = System.currentTimeMillis();
             numNo = atlasStatisticsQueryService.getExperimentCountsForGene(efv, StatisticsType.NON_D_E, isEfo == StatisticsQueryUtils.EFO, Long.parseLong(geneIdKey));
             log.debug("Obtained non-de counts for gene: " + geneIdKey + " and efv: " + efv + " in: " + (System.currentTimeMillis() - start) + " ms");
 
             jsResult.put("numUp", numUp);
             jsResult.put("numDn", numDn);
             jsResult.put("numNo", numNo);
 
         }
 
         return jsResult;
     }
 
 }
