 package company;
 
 import java.io.*;
 import java.util.*;
 
 
 public class ChiefScientistAssistant implements Runnable{
 
     private ArrayList<RunnableExperiment> experiments;
     private int completedExperiments;
     private ChiefScientist chief;
 
     // Constructor the singelton 
 
     private static ChiefScientistAssistant assistant = null;
 
     public static synchronized ChiefScientistAssistant 
         getInstance(ArrayList<Experiment> experimentsToRun,ChiefScientist chief) {
 
             if (assistant == null) {
 
                 assistant = new ChiefScientistAssistant(experimentsToRun,chief);
                 return assistant;
 
             } else {
                 return assistant;
             }
         }
 
     private void ChiefScientistAssistant(ArrayList<Experiment> experimentsToRun,
             ChiefScientist chief) {
 
         Iterator<Experiment> it = experimentsToRun.iterator();
 
         while (it.hasNext()) {
             experiments.add(new RunnableExperiment(it.next(), chief));
         }
         // will be indicator for finishing scaning experiments.
         this.completeExperiments = 0;
         this.chief = chief;
     }
 
 
 
 
     public void run() {
 
         synchronized (this) {
             // Program Cycle
             while (this.completedExperiments != this.experiments.size()) {
 
                 // Scaning experiments and find experiments with 
                 // no preExperiments required..
 
                 Iterator<RunnableExperiment> it = this.experiments.iterator();
 
                 while(it.hasNext()) {
 
                     // Copy of experiment to work with - can't work with Iterator
                     Experiment experimentItr = it.next();
 
                     if (experimentItr.getExperiment()
                             .getExperimentPreRequirementsExperiments().size() == 0) {
 
                         // check status
                         if (experimentItr.getExperiment()
                                 .getExperimentStatus()
                                 .equals("INCOMPLETE") == true) {
                             // Look for laboratory and add Experiment
                             // later: claculate effiency and buy new scientists
                             Iterator<HeadOfLaboratory> labIt = 
                                 this.chief.getLaboratories().iterator();
 
                             boolean found = false; // Prevent from assigning to 2 labs the same experiment
                             while (labIt.hasNext() && !found) {
                                 // Copy of experiment to work with.
                                 HeadOfLaboratory laboratoryIt = labIt.next();
 
                                 // Find laboratory with same specialization.
                                 if (laboratoryIt.getSpecialization()
                                         .equals(experimentItr
                                             .getExperimentSpecialization()) == true) {
 
                                     if (found != true) {
                                         prepareExperimentToExecute(laboratoryIt, experimentItr);
                                         found = true;  
                                     }
                                             }
                             }
                             // Indicates that no lab found and lab need to be purchased and exe experiment.
                             if (found == false) {
                                 buyLaboratory(experimentItr.getExperimentSpecialization(),
                                         experimentItr); 
                             }
                                 } // Experiment is Complete or InProgress
                             }  // Pre experiments required and experiment can't execute.
                 }
 
                 // wrap it with try and catch and need to be sync??? have to i think
                 try {
                     this.wait();
                } catch (InterruptedException e) {}
 
             }
             // Need to ShutDown everything?!!!!!
             this.chief.shutdownAllLabs();
 
             // Printint all data in statistics!!!
             this.chief.getStatistics().toString();
         }
     }
     
     // preparing experiment to be execute
     // checks for equipment in repo if missing buying
     // changing status and execute.
     public void prepareExperimentToExecute(HeadOfLaboratory lab,
                                            RunnableExperiment experiment) {
  
         // Check for equipment in repo - if HashMap is empty 
         // (meaning no equipment need to be purchased!)
         HashMap<String,Integer> equipmentsToPurchase = checkEquipmentAvailability(experiment.
                     getExperimentRequiredEquipments());
 
         if (equipmentsToPurchase.size() == 0) {
             // change status of experiment to InProgress.
             experiment.setExperimentStatus("INPROGRESS");
 
             // Send to execute 
             lab.addExperimentToExecute(experiment);
         } else {
             
             // Go and purchase items in HashMap
             this.chief.getStore().purchaseEquipmentPackage(
                                         this.chief.getStatistics(),
                                         equipmentsToPurchase);
             // Change status of experiment to InProgress.
             experiment.setExperimentStatus("INPROGRESS");
             // Send to execute 
             lab.addExperimentToExecute(experiment);
         }
     }
     
     // Checks in repo for equipments, and if mising, fill hash map with
     // equipments to buy.
     public HashMap<String,Integer> checkEquipmentAvailability(HashMap<String,Integer> equipments){
 
         HashMap<String,Integer> repository =
             this.chief.getRepository(); 
 
         HashMap<String,Integer> equipmentsToPurchase =
             new HashMap<String,Integer>(); 
 
         // iterate all keys in hashmap
         Iterator it = equipments.entrySet().iterator();
         while (it.hasNext()) {
 
             String itemName = (String)it.next(); // maybe will cause problem! 
 
             if (repository.containsKey(itemName) == true) {
 
                 // checks if there is enough items in repo
                 if (repository.get(itemName).intValue() >= 
                         equipments.get(itemName).intValue()) {
 
                 } else { // not enough item in repo need to add to map to purchased
 
                     equipmentsToPurchase.put(new String(itemName),
                             new Integer(equipments.get(itemName).intValue() -
                                 repository.get(itemName).intValue()));
                 } 
             } else {
 
                 equipmentsToPurchase.put(new String(itemName),
                         new Integer(equipments.get(itemName).intValue()));
             }
         }
         return equipmentsToPurchase;
     } 
 
     // buy+add to arraylist + look for the new lab + send to prepareExperimentToExecute
     public void buyLaboratory(String specialization,
             RunnableExperiment experiment) {
 
         if (this.chief.getStore().purchaseLaboratory(this.chief.getStatistics(),
                     specialization)) {
 
             Iterator<HeadOfLaboratory> it = this.chief.getLaboratories().iterator();
 
             boolean found = false; 
             //iterate all labs with new lab that just purchased.
             while(it.hasNext() && !found) {
 
                 HeadOfLaboratory laboratoryIt = this.chief.getLaboratories().next();
                 // Find laboratory with same specialization.
                 if (laboratoryIt.getSpecialization()
                         .equals(experiment
                             .getExperimentSpecialization()) == true) {
 
                     if (found != true) {
                         prepareExperimentToExecute(laboratoryIt, experiment);
                         found = true;  
                     }
                             }
             } else { 
                 System.out.prinln("ERROR : Could not buy laboratory of type: " + specialization);
             }
         }
     }
 
     // Increase Number Of finishedExperiment by 1;
     public void increaseNumberOfFinishedExperiments() {
         this.completedExperiments += 1;
     }
 
     public String toString(){
 
         StringBuilder result = new StringBuilder();
         String NEW_LINE = System.getProperty("line.separator");
         Iterator<RunnableExperiment> it = experiments.iterator();
 
         result.append("______________________________________" + NEW_LINE);
         result.append("           ---Chief Scientist Assistant---: " + NEW_LINE);
         while (it.hasNext()) {
         result.append(it.next().toString() + NEW_LINE);
         }
 
         return result.toString();
     }
 
 }
