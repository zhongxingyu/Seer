 package company;
 
 import stat.*;
 import utilities.*;
 
 import java.io.*;
 import java.util.*;
 
 public class ChiefScientist implements Observer{
 
     private ArrayList<HeadOfLaboratory> laboratories;
     private ArrayList<Experiment> experiments;
     private Statistics statistics;
     private ScienceStore store;
     private Repository repository;
     private ChiefScientistAssistant chiefAssistant;
     private Object _lockScanAndUpdate;
 
 
     public ChiefScientist(ArrayList<HeadOfLaboratory> laboratories,
                           ArrayList<Experiment> experiments,
                           Statistics statistics,
                           ScienceStore store,
                           Repository repository,
                           ChiefScientistAssistant chiefAssistant) {
         // TODO check if need to do deep copy!!!
         this.laboratories = laboratories;
         this.experiments = experiments;
         this.statistics = statistics;
         this.store = store;
         this.repository = repository;
         this.chiefAssistant = chiefAssistant.getInstance(this.experiments,this);
     }
     // an experiments inform the chief that he is done.
     // chief need to update the database for rerun the ChiefAssistant by notify 
     // him (wake him up from wait())
     public synchronized void update(Observable o, Object arg){
 
             if (arg instanceof String) {
                 String finishedExperiment = (String) arg;
                 
                 // Removing the experiment from all preRequiredExperiments
                 // of all experiments in chief
                 deletePreExperiments(finishedExperiment);
 
                 // Changing status to COMPLETE!
                 changeStatusToComplete(finishedExperiment);
 
                 // Updating statistics with finishedExperiment
                 updateStatisticsFinishedExperiment(finishedExperiment); 
 
                 // Updating number of complete experiments in assistant
                 this.chiefAssistant.increaseNumberOfFinishedExperiments();
 
             } else {
                 System.out.println("ERROR: Problem with casting in Chief:update()");
             }
             synchronized(this.chiefAssistant) {
                 this.chiefAssistant.notifyAll();
             }
     }
     //Getters
     public Repository getRepository() {
         return this.repository;
     } 
     public Statistics getStatistics() {
         return this.statistics;
     }
     public ArrayList<HeadOfLaboratory> getLaboratories() {
         return this.laboratories;
     }
     public ScienceStore getStore() {
         return this.store;
     }
     public Object getLockObject() {
         return this._lockScanAndUpdate;
     }
 
     // Delete pre required experiments from experiment list by the name of
     // the specific experiment that finised!!!
     public void deletePreExperiments(String expId) {
 
         Iterator<Experiment> experimentItr = this.experiments.iterator();
         ListIterator preRequiredExpItr = null; 
 
         // Iterate all Experiments
         while (experimentItr.hasNext()) {
 
             Experiment experimentIt = experimentItr.next();
 
             if (experimentIt.getExperimentId().equals(expId) == false) { 
 
                 ArrayList<Integer> preRequiredExperiments = experimentIt
                     .getExperimentRequiredEquipments();
 
                 // get listIterator to iterate backwards
                 preRequiredExpItr = preRequiredExperiments.
                     listIterator(preRequiredExperiments.size());
 
                 //Iterate in each experiment the pre required experiment list
                 while (preRequiredExpItr.hasPrevious()) {
 
                     Integer expInt = preRequiredExpItr.previous();
                     if (expInt.compareTo(Integer.getInteger(expId)) == true ) {
 
                         // delete from pre required experiments
                         preRequiredExpItr.remove(); // hopefully with no special problems
                     }
                 }
             } else {} // we reached the same experiment that sent here!
         }
     }
 
     // Changing status of experiment to COMPLETE!
     public void changeStatusToComplete(finishedExperiment) {
 
         Iterator<Experiment> it = this.experiments.iterator();
 
         boolean found = false;
         while (it.hasNext() && !found) {
             Experiment experiment = it.next();
             if (experiment.getExperimentId().
                     equals(finishedExperiment) == true) {
 
                 experiment.setExperimentStatus(finishedExperiment);
                 found = true;
              }
         }
     }
     // Updating statistics with the finished experiment!
     public void updateStatisticsFinishedExperiment(String experimentId) {
 
         Iterator<Experiment> it = this.experiments.iterator();
 
         boolean found = false;
         while(it.hasNext() && !found) {
             Experiment expIt = it.next();
 
             if (expIt.getExperimentId().equals(experimentId) == true) {
 
                 this.statistics.increaseFinishedExperiment(expIt);
                 found = true;
             }
         }
     }
 
     // Shuting Down all Labs Gracefully my lord!
     public void shutdownAllLabs() {
         
         Iterator<HeadOfLaboratory> it = this.laboratories;
         while (it.hasNext()) {
             it.next().shutdownLab();
         }
     }
 
     public String toString() {
 
         StringBuilder result = new StringBuilder();
         String NEW_LINE = System.getProperty("line.separator");
 
         result.append("______________________________________" + NEW_LINE);
         result.append("           ---ChiefScientist---: " + NEW_LINE);
         result.append("Laboratories: " + this.laboratories.toString() + NEW_LINE);
         result.append("Experiments: " + this.experiments.toString() + NEW_LINE);
         result.append("Statistics: " + this.statistics.toString() + NEW_LINE);
         result.append("Store: " + this.store.toString() + NEW_LINE);
         result.append("Repository: " + this.repository.toString() + NEW_LINE);
         result.append("ChiefAssistant " + this.chiefAssistant.toString() + NEW_LINE);
         return result.toString();
     }
 
 
 }
