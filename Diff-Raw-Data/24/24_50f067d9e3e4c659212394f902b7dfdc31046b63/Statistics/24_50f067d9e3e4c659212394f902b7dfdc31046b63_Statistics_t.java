 /** @author Eldar Damari, Ory Band */
 
 package company;
 
 import java.util.ArrayList;
 
 
 public class Statistics {
 
     private int budget;
     private int rewards;
     private int moneySpent;
 
     private ArrayList<Scientist> scientistsPurchased;
     private ArrayList<EquipmentPackage> equipmentPackagesPurchased;
     private ArrayList<Laboratory> laboratoriesPurchased;
     private ArrayList<Experiment> experiments;
 
     public Statistics(int budget) {
         this.budget = budget;
         this.rewards = 0 ;
         this.moneySpent = 0;
         this.scientistsPurchased = new ArrayList<Scientist>();
         this.equipmentPackagesPurchased = new ArrayList<EquipmentPackage>();
         this.laboratoriesPurchased = new ArrayList<Laboratory>();
         this.experiments = new ArrayList<Experiment>(); 
     }
 
     public Statistics() {
         this(0);
     }
 
     // Setters
     public void setBudget(int i) {
         this.budget = i;
     }
 
 
     public void increaseFinishedExperiment(Experiment experiment) {
         this.experiments.add(experiment);
     }
 
 
     public void addPurchasedEquipment(EquipmentPackage equipmentPackage) {
         this.budget -= equipmentPackage.getPrice();
         this.moneySpent += equipmentPackage.getPrice();
         this.equipmentPackagesPurchased.add(equipmentPackage);
     }
 
     public void addPurchasedScientist(Scientist scientist) {
         this.budget -= scientist.getPrice();
         this.moneySpent += scientist.getPrice();
         this.scientistsPurchased.add(scientist);
     }
 
     public void addPurchasedLaboratory(Laboratory laboratory) {
         this.budget -= laboratory.getPrice();
         this.moneySpent += laboratory.getPrice();
         this.laboratoriesPurchased.add(laboratory);
     }
 
 
     public void addReward(int reward) {
         this.budget += reward;
         this.rewards += reward;
     }
 
     public int getBudget() {
         return this.budget;
     }
 
     public String toString() {
 
         StringBuilder result = new StringBuilder();
         String N = System.getProperty("line.separator");
 
         result.append(N);
         result.append("Statistics:" + N + N);
 
         result.append("Finished Experiments: " + 
                 this.experiments.toString() + N + N);
 
         result.append("Equipment Packages Purchased : " + 
                 this.equipmentPackagesPurchased.toString() + N + N);
 
         result.append("Scientists Purchased: " + 
                 this.scientistsPurchased.toString() + N + N);
 
         result.append("Laboratories Purchased: " + 
                 this.laboratoriesPurchased.toString() + N + N);
 
        result.append("**Budget Summary** " + N);
        result.append("Reward: +" + this.rewards + "$ " + N);
        result.append("Money Spent: -" + this.moneySpent + "$ " + N);
        result.append("Total Budget: " + this.budget + N);
 
         return result.toString();
     }
 }
