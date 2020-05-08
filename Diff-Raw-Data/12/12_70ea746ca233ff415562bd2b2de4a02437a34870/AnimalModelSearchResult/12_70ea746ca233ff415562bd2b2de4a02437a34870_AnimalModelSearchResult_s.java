 /**
  * @author dgeorge
  * 
 * $Id: AnimalModelSearchResult.java,v 1.2 2005-10-12 18:09:29 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2005/10/07 16:27:50  georgeda
  * Implemented paganation
  *
  */
 package gov.nih.nci.camod.domain;
 
 import gov.nih.nci.camod.service.impl.AnimalModelManagerSingleton;
 
 import java.util.List;
 
 /**
  * Used as wrapper around animal model for speedy display during paganation.
  */
 public class AnimalModelSearchResult {
 
     private String myAnimalModelId;
     private String myTumorSites = null;
     private String myMetastatisSites = null;
     private String mySpecies = null;
     private String myModelDescriptor = null;
 
     private AnimalModel myAnimalModel = null;
 
     /**
      * Create the wraper object
      * 
      * @param inAnimalModel
      *            the animal model we will be wrapping. Saves only the id.
      */
     public AnimalModelSearchResult(AnimalModel inAnimalModel) {
         myAnimalModelId = inAnimalModel.getId().toString();
     }
 
     /**
      * Return the id for the associated model
      * 
      * @return the id for the model
      */
     public String getId() {
 
         return myAnimalModelId;
     }
 
     /**
      * Return the model descriptor. It will fetch the animal model from the DB
      * if it hasn't already happened.
      * 
      * @return the model descriptor for the associated model
      * 
      * @throws Exception
      */
     public String getModelDescriptor() throws Exception {
 
         if (myModelDescriptor == null) {
             fetchAnimalModel();
             myModelDescriptor = myAnimalModel.getModelDescriptor();
         }
         return myModelDescriptor;
     }
 
     /**
      * Return the species. It will fetch the animal model from the DB if it
      * hasn't already happened.
      * 
      * @return the species for the associated model
      * @throws Exception
      */
     public String getSpecies() throws Exception {
         if (mySpecies == null) {
             fetchAnimalModel();
             mySpecies = myAnimalModel.getSpecies().getScientificName();
         }
         return mySpecies;
     }
 
     /**
      * Return the list of tumor sites. It will fetch the animal model from the
      * DB if it hasn't already happened.
      * 
      * @return the list of tumor sites for the associated model
      * @throws Exception
      */
     public String getTumorSites() throws Exception {
 
         if (myTumorSites == null) {
             fetchAnimalModel();
 
             myTumorSites = "";
             List theOrgans = myAnimalModel.getDistinctOrgansFromHistopathologyCollection();
 
             for (int i = 0, j = theOrgans.size(); i < j; i++) {
                 String theOrgan = (String) theOrgans.get(i);
                 myTumorSites += "<b>" + theOrgan + "</b><br>";
             }
         }
 
         return myTumorSites;
     }
 
     /**
      * Return the list of metastatis sites. It will fetch the animal model from
      * the DB if it hasn't already happened.
      * 
      * @return the list of metastatis sites for the associated model
      * @throws Exception
      */
     public String getMetastatisSites() throws Exception {
 
         if (myMetastatisSites == null) {
             fetchAnimalModel();
 
             myMetastatisSites = "";
             List theOrgans = myAnimalModel.getDistinctMetastatisOrgansFromHistopathologyCollection();
 
             for (int i = 0, j = theOrgans.size(); i < j; i++) {
                 String theOrgan = (String) theOrgans.get(i);
                myTumorSites += theOrgan + " (Metastatis)<br>";
             }
         }
        return myTumorSites;
     }
 
     // Fetch the animal model from the DB
     private void fetchAnimalModel() throws Exception {
         if (myAnimalModel == null) {
             myAnimalModel = AnimalModelManagerSingleton.instance().get(myAnimalModelId);
         }
     }
 }
