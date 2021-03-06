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
 
 package ae3.model;
 
 import ae3.dao.AtlasSolrDAO;
 import ae3.service.structuredquery.UpdownCounter;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.solr.common.SolrDocument;
 import uk.ac.ebi.gxa.index.GeneExpressionAnalyticsTable;
 import uk.ac.ebi.gxa.utils.*;
 import static uk.ac.ebi.gxa.utils.EscapeUtil.nullzero;
 import uk.ac.ebi.microarray.atlas.model.ExpressionAnalysis;
 
 import java.util.*;
 
 /**
  * View class for Atlas gene SOLR document
  */
 public class AtlasGene {
     private SolrDocument geneSolrDocument;
     private Map<String, List<String>> geneHighlights;
     private GeneExpressionAnalyticsTable expTable;
     private static final String PROPERTY_PREFIX = "property_";
 
     /**
      * Constructor
      * @param geneDoc SOLR document for the gene
      */
     public AtlasGene(SolrDocument geneDoc) {
         this.geneSolrDocument = geneDoc;
     }
 
     /**
      * Safe value getter correctly dealing with multiple values
      * @param name field name
      * @return string value or empty string if not found
      */
     private String getValue(String name)
     {
         Collection fval = geneSolrDocument.getFieldValues(name);
         if(fval != null)
             return StringUtils.join(fval, ", ");
         return "";
     }
 
     /**
      * Safely gets collection of field values
      * @param name field name
      * @return collection (maybe empty but never null)
      */
     @SuppressWarnings("unchecked")
     private Collection<String> getValues(String name)
     {
         Collection<Object> r = geneSolrDocument.getFieldValues(name);
         return r == null ? Collections.EMPTY_LIST : (Collection)r;
     }
 
     /**
      * Safely gets highlighted string value of the field
      * @param name
      * @return highlighted HTML value of the field or empty string if not found
      */
     private String getHilitValue(String name) {
         List<String> val = geneHighlights.get(name);
         if(val == null || val.size() == 0)
             return StringEscapeUtils.escapeHtml(getValue(name));
         return StringUtils.join(val, ", ");
     }
 
     /**
      * Set highlights map from SOLR QueryResponse to use for field value highlighting
      * @param geneHighlights highlighting map
      */
     public void setGeneHighlights(Map<String, List<String>> geneHighlights) {
         this.geneHighlights = geneHighlights;
     }
 
     /**
      * Returns document object
      * @return SOLR document object
      */
     public SolrDocument getGeneSolrDocument() {
         return geneSolrDocument;
     }
 
     /**
      * Returns map of gene property values
      * @return map of gene property values
      */
     public Map<String,Collection<String>> getGeneProperties() {
         return new LazyMap<String, Collection<String>>() {
             protected Collection<String> map(String key) {
                 return getValues(PROPERTY_PREFIX + key);
             }
 
             protected Iterator<String> keys() {
                 return getGenePropertiesIterator();
             }
         };
     }
 
     /**
      * Returns map of highlighted gene property values
      * @return map of highlighted gene property values
      */
     public Map<String,String> getHilitGeneProperties() {
         return new LazyMap<String, String>() {
             protected String map(String key) {
                 return getHilitValue(PROPERTY_PREFIX + key);
             }
 
             protected Iterator<String> keys() {
                 return getGenePropertiesIterator();
             }
         };
     }
 
     /**
      * Returns iterable of all available gene properties
      * @return iterable property names
      */
     public Iterable<String> getGenePropertyNames() {
         return new Iterable<String>() {
             public Iterator<String> iterator() {
                 return getGenePropertiesIterator();
             }
         };
     }
 
     /**
      * Returns iterator of all available gene properties
      * @return iterator of all available gene properties
      */
     public Iterator<String> getGenePropertiesIterator() {
         return new FilterIterator<String, String>(geneSolrDocument.getFieldNames().iterator()) {
             public String map(String name) {
                 return name.startsWith(PROPERTY_PREFIX) ? name.substring(PROPERTY_PREFIX.length()) : null;
             }
         };
     }
 
     /**
      * Returns internal numeric gene ID
      * @return internal numeric gene ID (in string apparently)
      */
     public String getGeneId() {
         return getValue("id");
     }
 
     /**
      * Returns highlighted gene property value
      * @param property property name
      * @return highlighted gene property value
      */
     public String getHilitPropertyValue(String property) {
         return getHilitValue(PROPERTY_PREFIX + property);
     }
 
     /**
      * Returns string gene propery value
      * @param property property name
      * @return string property value
      */
     public String getPropertyValue(String property) {
         return getValue(PROPERTY_PREFIX + property);
     }
 
     /**
      * Returns collection of property values
      * @param property property name
      * @return collection of property values
      */
     public Collection<String> getPropertyValues(String property) {
         return getValues(PROPERTY_PREFIX + property);
     }
 
     /**
      * Returns gene name
      * @return gene name
      */
     public String getGeneName() {
         return getValue("name");
     }
 
     /**
      * Returns highlighted gene name
      * @return highlighted gene name
      */
     public String getHilitGeneName() {
         return getHilitValue("name");
     }
 
     /**
      * Returns primary gene identifier
      * @return gene identifier
      */
     public String getGeneIdentifier() {
         return getValue("identifier");
     }
 
     /**
      * Returns gene organism
      * @return organism
      */
     public String getGeneSpecies() {
         return StringUtil.upcaseFirst(getValue("species"));
     }
 
     /**
      * Returns collection of ortholog identifiers
      * @return collection of ortholog identifiers
      */
 	public Collection<String> getOrthologs() {
         return getValues("orthologs");
 	}
 
     /**
      * Returns number of experiments, where gene is UP expressed in EFO accession
      * @param efo accession
      * @return number
      */
     public int getCount_up(String efo) {
         return nullzero((Number)geneSolrDocument.getFieldValue("cnt_efo_" + EscapeUtil.encode(efo) + "_up"));
     }
 
     /**
      * Returns number of experiments, where gene is DOWN expressed in EFO accession
      * @param efo accession
      * @return number
      */
     public int getCount_dn(String efo) {
         return nullzero((Number)geneSolrDocument.getFieldValue("cnt_efo_" + EscapeUtil.encode(efo) + "_dn"));
     }
 
     /**
      * Returns analytics table for gene
      * @return analytics table reference
      */
     public GeneExpressionAnalyticsTable getExpressionAnalyticsTable() {
         if(expTable != null)
             return expTable;
 
         byte[] eadata = (byte[]) geneSolrDocument.getFieldValue("exp_info");
         if(eadata != null)
             expTable = GeneExpressionAnalyticsTable.deserialize((byte[])geneSolrDocument.getFieldValue("exp_info"));
         else
             expTable = new GeneExpressionAnalyticsTable();
 
         return expTable;
     }
 
     /**
      * Returns number of experiments gene studied in
      * @return number
      */
     public int getNumberOfExperiments() {
         return getExperimentIds().size();
     }
 
         /**
      * Returns number of experiments gene studied in
      * @return number
      */
     public Set<Long> getExperimentIds() {
         Set<Long> expIds = new HashSet<Long>();
         for(ExpressionAnalysis e : getExpressionAnalyticsTable().getAll())
             expIds.add(e.getExperimentID());
         return expIds;
     }
 
     /**
      * Returns expression heatmap for gene
      * @param omittedEfs factors to skip
      * @return EFV tree of up/down counters for gene
      */
     public EfvTree<UpdownCounter> getHeatMap(Collection<String> omittedEfs){
         return getHeatMap(null, omittedEfs);
     }
 
     //get heatmap for one factor only
     public EfvTree<UpdownCounter> getHeatMap(String efName, Collection<String> omittedEfs) {
         EfvTree<UpdownCounter> result = new EfvTree<UpdownCounter>();
 
         Maker<UpdownCounter> maker = new Maker<UpdownCounter>() {
             public UpdownCounter make() {
                 return new UpdownCounter();
             }
         };
         for(ExpressionAnalysis ea : getExpressionAnalyticsTable().getAll()) {
             if(omittedEfs.contains(ea.getEfName()))
                 continue;
 
             if(null!=efName)
                 if(!efName.equals(ea.getEfName()))
                     continue;
 
             UpdownCounter counter = result.getOrCreate(ea.getEfName(), ea.getEfvName(), maker);
             if(ea.isNo())
                 counter.addNo();
             else counter.add(ea.isUp(), ea.getPValAdjusted());
 
             counter.addExperiment(ea.getExperimentID());
         }
 
         return result;
     }
 
     /**
      * Returns list of top analytics for experiment
      * @param exp_id_key numerical internal experiment id
      * @return list of analytics
      */
     public List<ExpressionAnalysis> getTopFVs(long exp_id_key) {
         List<ExpressionAnalysis> result = new ArrayList<ExpressionAnalysis>();
         for(ExpressionAnalysis e : getExpressionAnalyticsTable().findByExperimentId(exp_id_key)) {
             result.add(e);
         }
         Collections.sort(result, new Comparator<ExpressionAnalysis>() {
             public int compare(ExpressionAnalysis o1, ExpressionAnalysis o2) {
                 return Float.valueOf(o1.getPValAdjusted()).compareTo(o2.getPValAdjusted());
             }
         });
         return result;
     }
 
     /**
      * Returns list of analytics for specified experiment
      * @param exp_id_key numerical internal experiment id
      * @return list of analytics
      */
     public List<ExpressionAnalysis> getAtlasResultsForExperiment(long exp_id_key){
         ArrayList<ExpressionAnalysis> result = new ArrayList<ExpressionAnalysis>();
         for(ExpressionAnalysis e : getExpressionAnalyticsTable().findByExperimentId(exp_id_key)){
             result.add(e);
         }
         return result;
     }
 
     /**
      * Return highest rank EF in experiment and associated pvalue
      * @param experimentId internal experiment id
      * @return pair of EF and pvalue
      */
     public Pair<String,Float> getHighestRankEF(long experimentId) {
         String ef = null;
         Float pvalue = null;
         for(ExpressionAnalysis e : getExpressionAnalyticsTable().findByExperimentId(experimentId))
             if(pvalue == null || pvalue > e.getPValAdjusted()) {
                 pvalue = e.getPValAdjusted();
                 ef = e.getEfName();
             }
         return new Pair<String,Float>(ef, pvalue);
     }
 
     public boolean getHasAnatomogram(){
         if(null==anatomogramEfoList)
            return true;
         for(String term : anatomogramEfoList){
             if(this.getCount_dn(term)>0||this.getCount_up(term)>0)
                 return true;
         }
         return false;
     }
     private List<String> anatomogramEfoList = null;
 
     public void setAnatomogramEfoList(List<String> queryTerms){
         anatomogramEfoList = queryTerms;
     }
 
     @Override
     public boolean equals(Object obj) {
         if(obj == this)
             return true;
         return obj != null && getGeneId().equals(((AtlasGene)obj).getGeneId());
     }
 
     @Override
     public int hashCode() {
         return geneSolrDocument != null ? geneSolrDocument.hashCode() : 0;
     }
 
     public List<ExperimentalFactor> getDifferentiallyExpressedFactors(Collection<String> omittedEfs, AtlasSolrDAO atlasSolrDAO, String ef){
         List<ExperimentalFactor> result = new ArrayList<ExperimentalFactor>();
         List<String> efs = new ArrayList<String>();
 
         for(EfvTree.EfEfv<UpdownCounter> i : this.getHeatMap(omittedEfs).getNameSortedList()){
             if((ef==null)||(ef.equals(i.getEf()))){
             if(!efs.contains(i.getEf()))
                efs.add(i.getEf()) ;
             }
         }
 
         for(String factorName : efs){
             ExperimentalFactor factor = new ExperimentalFactor(this,factorName,omittedEfs);
 
             List<Long> experimentIds = new ArrayList<Long>();
             for(ExpressionAnalysis ea : this.getExpressionAnalyticsTable().getAll()){
                 if(!ea.getEfName().equals(factorName))
                     continue;
 
                 if(!experimentIds.contains(ea.getExperimentID()))
                     experimentIds.add(ea.getExperimentID());
             }
 
             for(Long experimentID : experimentIds){
                 String accession = atlasSolrDAO.getExperimentById(experimentID).getAccession();
                 factor.addExperiment(experimentID,accession);
             }
 
             result.add(factor);
         }
 
         Collections.sort(result,new Comparator<ExperimentalFactor>(){
             private int SortOrder(String name){
                 if(name.equals("organismpart"))
                     return 0;
                 else if (name.equals("cellline"))
                     return 1;
                 else if (name.equals("celltype"))
                     return 2;
                 else if (name.equals("diseasestate"))
                     return 3;
                 else if (name.equals("compound"))
                     return 4;
                 else
                     return 999;
             }
             public int compare(ExperimentalFactor f1,ExperimentalFactor f2){
                 int i = SortOrder(f1.getName()) - SortOrder(f2.getName());
                 return ( i==0 ? f1.getName().compareTo(f2.getName()) : i);
             }
         });
 
         return result;
     }
 }
