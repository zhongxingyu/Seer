 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.web.action.analysis.biodbnet;
 
 import gov.nih.nci.caintegrator.external.biodbnet.BioDbNetService;
 import gov.nih.nci.caintegrator.external.biodbnet.enums.SearchType;
 import gov.nih.nci.caintegrator.external.biodbnet.search.GeneResults;
 import gov.nih.nci.caintegrator.external.biodbnet.search.PathwayResults;
 import gov.nih.nci.caintegrator.external.biodbnet.search.SearchParameters;
 import gov.nih.nci.caintegrator.web.action.AbstractCaIntegrator2Action;
 
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import com.google.common.collect.Sets;
 
 /**
  * Actions for dealing with bioDbNet searches.
  *
  * @author Abraham J. Evans-EL <aevansel@5amsolutions.com>
  */
 public class BioDbNetSearchAction extends AbstractCaIntegrator2Action {
     private static final long serialVersionUID = 1L;
     private static final String GENE_RESULT = "genes";
     private static final String PATHWAY_RESULT = "pathways";
     private BioDbNetService bioDbNetService;
     private SearchParameters searchParameters;
     private Set<GeneResults> geneResults;
     private Set<PathwayResults> pathwayResults;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void prepare() {
         super.prepare();
         setSearchParameters(new SearchParameters());
         geneResults = Sets.newHashSet();
         pathwayResults = Sets.newHashSet();
     }
 
     /**
      * Default input method.
      * @return the struts result
      */
     @Override
     public String input() {
         return SUCCESS;
     }
 
     /**
      * Performs the bioDbNet search.
      * @return the struts results
      */
     public String search() {
         try {
             getSearchParameters().setStudy(getCurrentStudy());
             if (getSearchParameters().getSearchType() == SearchType.PATHWAY_BY_GENE) {
                 setPathwayResults(handlePathwaySearch());
                 if (CollectionUtils.isEmpty(getPathwayResults())) {
                     addActionError(getText("bioDbNet.noPathwayResultsFound"));
                 }
                 return PATHWAY_RESULT;
             } else {
                 setGeneResults(handleGeneSearch(getSearchParameters().getSearchType()));
                 if (CollectionUtils.isEmpty(getGeneResults())) {
                     addActionError(getText("bioDbNet.noGeneResultsFound"));
                 }
                 return GENE_RESULT;
             }
         } catch (Exception e) {
             addActionError(getText("bioDbNet.error"));
             LOG.error("Unabled to access the bioDbNet Web Service.", e);
             return INPUT;
         }
     }
 
     private Set<PathwayResults> handlePathwaySearch() {
         Set<String> newInputs = handleCaseSensitivity(getSearchParameters());
         return bioDbNetService.retrievePathwaysByGeneSymbols(generateNewParams(newInputs));
     }
 
     private Set<GeneResults> handleGeneSearch(SearchType searchType) {
         Set<GeneResults> results = Sets.newHashSet();
         Set<String> newInputs = handleCaseSensitivity(getSearchParameters());
         if (searchType == SearchType.GENE_ID) {
             results = bioDbNetService.retrieveGenesById(generateNewParams(newInputs));
         } else if (searchType == SearchType.GENE_ALIAS) {
             Set<String> geneIds = bioDbNetService.retrieveGeneIdsByAlias(generateNewParams(newInputs));
             results = bioDbNetService.retrieveGenesById(generateNewParams(geneIds));
         } else if (searchType == SearchType.GENE_SYMBOL) {
             Set<String> geneIds = bioDbNetService.retrieveGeneIds(generateNewParams(newInputs));
             results = bioDbNetService.retrieveGenesById(generateNewParams(geneIds));
         } else if (searchType == SearchType.PATHWAY) {
             results = bioDbNetService.retrieveGenesByPathway(generateNewParams(newInputs));
         }
         return results;
     }
 
     private SearchParameters generateNewParams(Set<String> input) {
         SearchParameters params = new SearchParameters();
         params.setFilterGenesOnStudy(getSearchParameters().isFilterGenesOnStudy());
         params.setTaxon(getSearchParameters().getTaxon());
         params.setInputValues(StringUtils.join(input, ','));
        params.setStudy(getCurrentStudy());
         return params;
     }
 
     /**
      * Generates the search inputs in the following manner if case insensitivity has been selected.
      *  - the original inputs
      *  - inputs are transformed to all upper case
      *  - inputs are transformed to all lower case
      *  - inputs are transformed to 1st letter upper case, all others lower case
      * @return the transformed input strings as comma separated values
      */
     private Set<String> handleCaseSensitivity(SearchParameters searchParams) {
         Set<String> inputs = Sets.newTreeSet();
         if (searchParams.isCaseSensitiveSearch() || searchParams.getSearchType() == SearchType.GENE_ID) {
             CollectionUtils.addAll(inputs, StringUtils.split(searchParams.getInputValues(), ','));
             return inputs;
         }
         String[] splitInputs = StringUtils.split(searchParams.getInputValues(), ',');
         for (String input : splitInputs) {
             inputs.add(input);
             inputs.add(StringUtils.upperCase(input));
             inputs.add(StringUtils.lowerCase(input));
             inputs.add(StringUtils.capitalize(input));
         }
         return inputs;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         super.validate();
         if (StringUtils.isEmpty(getSearchParameters().getInputValues())) {
             addActionError(getText("struts.messages.error.must.enter.keywords"));
         }
     }
 
     /**
      * @return the searchParameters
      */
     public SearchParameters getSearchParameters() {
         return searchParameters;
     }
 
     /**
      * @param searchParameters the searchParameters to set
      */
     public void setSearchParameters(SearchParameters searchParameters) {
         this.searchParameters = searchParameters;
     }
 
     /**
      * @return the geneResults
      */
     public Set<GeneResults> getGeneResults() {
         return geneResults;
     }
 
     /**
      * @param geneResults the geneResults to set
      */
     public void setGeneResults(Set<GeneResults> geneResults) {
         this.geneResults = geneResults;
     }
 
     /**
      * @return the pathwayResults
      */
     public Set<PathwayResults> getPathwayResults() {
         return pathwayResults;
     }
 
     /**
      * @param pathwayResults the pathwayResults to set
      */
     public void setPathwayResults(Set<PathwayResults> pathwayResults) {
         this.pathwayResults = pathwayResults;
     }
 
     /**
      * Sets the bioDbNet service.
      * @param svc the service to set
      */
     public void setBioDbNetService(BioDbNetService svc) {
         this.bioDbNetService = svc;
     }
 }
