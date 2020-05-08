 package company;
 
 import stat.*;
 import utilities.*;
 import java.io.*;
 import java.util.*;
 
 
 public class Driver{
 
 
 public static void main(String args[]){
 
         // utilities instance - reads files.
         Util u = new Util();  
         
         Statistics statistics = new Statistics();
         Repository repository = new Repository();
         ArrayList<HeadOfLaboratory> headsOfLaboratory = 
             new ArrayList<HeadOfLaboratory>();
         // for ExperimentsList
         ArrayList<Experiment> experiments = new ArrayList<Experiment>();
         // for ScienceStore
         HashMap<String, ArrayList<EquipmentPackage>> equipmentForSale = 
             new HashMap<String, ArrayList<EquipmentPackage>>();
         HashMap<String, ArrayList<Laboratory>> laboratoryForSale= 
             new HashMap<String, ArrayList<Laboratory>>();
         HashMap<String, ArrayList<Scientist>> scientistsForSale= 
             new HashMap<String, ArrayList<Scientist>>();
         // for ChiedScientist
         ChiefScientistAssistant chiefAssistant = new ChiefScientistAssistant();
         
         
         // Read data from: InitialData.txt
         u.getDataFromInizialData("InitialData.txt",
                                  statistics,
                                  repository,
                                  headsOfLaboratory);
 
         // Print Tests
         /*System.out.println("Printing InitialData : " ); 
 
         System.out.println(statistics.toString());
         System.out.println(repository.toString());
         
         Iterator<HeadOfLaboratory> itr = headsOfLaboratory.iterator();
 
         while(itr.hasNext()) {
        //print to string 
         System.out.println(itr.next().toString());
         }*/
 
 
         // Read data from file: ExperimentsList.txt
         experiments = u.getDataFromExperimentsList("ExperimentsList.txt");
 
         //Print Tests
         /*System.out.println("all good , need to print" ); 
         Iterator<Experiment> it = experiments.iterator();
         while(it.hasNext()) {
        //print to string 
         System.out.println(it.next().toString());
         }*/
 
 
         // Read Data From: EquipmentForSale.txt
         equipmentForSale = u.getDataFromEquipmentForSale("EquipmentForSale.txt");
         // Print Tests
         //System.out.println(equipmentForSale.values());*/
         
         
         // Read Data From: LaboratoryForSale.txt
         laboratoryForSale = u.getDataFromLaboratoriesForSale("LaboratoriesForSale.txt");
         // Print Tests
         //System.out.println(laboratoryForSale.values());*/
         
         
         // Read Data From: ScientistsForSale.txt
         scientistsForSale = u.getDataFromScientistsForSale("ScientistsForPurchase.txt");
         // Print Tests
         //System.out.println(scientistsForSale.values());*/
 
 
 
         ChiefScientist chief = new ChiedScientist(headsOfLaboratory,
                                                   experiments, 
                                                   statistics,
                                                   new ScienceStore(
                                                       equipmentForSale,
                                                       scientistsForSale,
                                                       laboratoryForSale),
                                                   repository,
                                                   chiefAssistant);
         // Print Tets
         System.out.println(chief.toString());
     }
 
    chief.simulate();
 }
