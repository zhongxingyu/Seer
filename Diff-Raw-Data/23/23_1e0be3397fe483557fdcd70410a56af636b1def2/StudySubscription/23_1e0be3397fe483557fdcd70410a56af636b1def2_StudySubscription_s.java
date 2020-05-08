 package gov.nih.nci.caintegrator2.domain.application;
 
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * 
  */
 public class StudySubscription extends AbstractCaIntegrator2StudyObject {
 
     private static final long serialVersionUID = 1L;
 
     private static final String GLOBAL_GENE_LIST_PREFIX = "[Global]-";
     
     private Study study;
     private Set<Query> queryCollection = new HashSet<Query>();
     private Set<AbstractPersistedAnalysisJob> analysisJobCollection = new HashSet<AbstractPersistedAnalysisJob>();
     private UserWorkspace userWorkspace;
     
     /**
      * @return the study
      */
     public Study getStudy() {
         return study;
     }
     
     /**
      * @param study the study to set
      */
     public void setStudy(Study study) {
         this.study = study;
     }
     
     /**
      * @return the queryCollection
      */
     public Set<Query> getQueryCollection() {
         return queryCollection;
     }
     
     /**
      * @param queryCollection the queryCollection to set
      */
     @SuppressWarnings("unused") // For hibernate.
     private void setQueryCollection(Set<Query> queryCollection) {
         this.queryCollection = queryCollection;
     }
 
     /**
      * @return the userWorkspace
      */
     public UserWorkspace getUserWorkspace() {
         return userWorkspace;
     }
 
     /**
      * @param userWorkspace the userWorkspace to set
      */
     public void setUserWorkspace(UserWorkspace userWorkspace) {
         this.userWorkspace = userWorkspace;
     }
 
     /**
      * @return the analysisJobCollection
      */
     public Set<AbstractPersistedAnalysisJob> getAnalysisJobCollection() {
         return analysisJobCollection;
     }
 
     /**
      * @param analysisJobCollection the analysisJobCollection to set
      */
     @SuppressWarnings("unused") // For hibernate.
     private void setAnalysisJobCollection(Set<AbstractPersistedAnalysisJob> analysisJobCollection) {
         this.analysisJobCollection = analysisJobCollection;
     }
     
     /**
      * @return a list of all gene lists including global lists
      */
     public List<GeneList> getAllGeneLists() {
         List<GeneList> resultList = new ArrayList<GeneList>();
         resultList.addAll(getGeneLists());
         resultList.addAll(getStudy().getStudyConfiguration().getGeneLists());
         return resultList;
     }
     
     /**
      * @return a list of all gene list names including global lists
      */
     public List<String> getAllGeneListNames() {
         List<String> resultList = new ArrayList<String>();
         resultList.addAll(getGeneListNames());
         for (String name : getStudy().getStudyConfiguration().getGeneListNames()) {
             resultList.add(GLOBAL_GENE_LIST_PREFIX + name);
         }
         return resultList;
     }
     
     /**
      * Parse the gene list name from the given name including prefix then retrieve the gene list.
      * @param name then gene list name with prefix
      * @return The gene list
      */
     public GeneList getSelectedGeneList(String name) {
         if (name != null) {
            String[] nameDetails = name.split("-");
            if (GLOBAL_GENE_LIST_PREFIX.equals(nameDetails[0] + "-")) {
                return getStudy().getStudyConfiguration().getGeneList(nameDetails[1]);
             }
             return getGeneList(name);
         }
         return null;
     }
 }
