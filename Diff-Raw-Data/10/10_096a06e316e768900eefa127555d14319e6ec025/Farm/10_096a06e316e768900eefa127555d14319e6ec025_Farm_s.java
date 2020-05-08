 package com.biofuels.fof.kosomodel;
 
 import java.util.LinkedList;
 
 public class Farm {
 
   private String name;
   private int clientID;
 //  private Field[] fields;
   private LinkedList<Field> fields;
   private int capital=10000;
   private double envScore;
   private double energyScore;
   private double econScore;
   private int envRank;
   private int energyRank;
   private int econRank;
   private double overallScore;
   private int overallRank;
 
   private double soilSubscore;
   private double waterSubscore;
 
   private String currentUser;
   private boolean acceptCornContract;
   private boolean acceptSwitchgrassContract;
   private boolean ready;
   private double phosphorous;
   private double GBI; //Grassland Bird Index
   private Game game;
   private FarmHistory history;
 
 
   public Farm(String name, int capital, Game g) {
     this.name = name;
     this.capital = capital;
     this.game = g;
     history = new FarmHistory();
     fields = new LinkedList<Field>();
     //    fields[0].setCrop(Crop.GRASS);
   }
 
   public LinkedList<Field> getFields(){
     return fields;
   }
 
   public void recomputeScores(){
     this.econScore = calcEconScore();
     this.envScore = calcEnvScore();
     this.energyScore = calcEnergyScore();
   }
 
   public double getEnvScore() {
     return envScore;
   }
 
   public double calcEnvScore() {
     double avgSoc = 0;
     for(Field f:fields){
       avgSoc += f.getSOC();
     }
     avgSoc /= fields.size();
     this.soilSubscore = avgSoc / 190;
     this.waterSubscore = 1 - ((this.getPhosphorous() - 0.0363) / 0.1876);
 
     return soilSubscore * .5 + waterSubscore * .5;
   }
 
   public int getCapital() {
     return capital;
   }
 
   public double getEnergyScore() {
     return energyScore;
   }
 
   public double calcEnergyScore() {
     double EMAX = 210666.6;
 
     //Energy is Mj per Mg (?)
     double energyKernel = 18800.0;
     double energyStover = 17700.0;
     double energyGrass = 17700.0;
 
     double cornYield = this.calcTotalCornYield();
     double grassYield = this.calcTotalGrassYield();
 
     double cornEnergy = ((cornYield / 2) * energyKernel) + ((cornYield / 4) * energyStover);
     double grassEnergy = grassYield * energyGrass;
 
 
     return ((cornEnergy + grassEnergy) / fields.size()) / EMAX;
   }
 
   public double getEconScore() {
     return econScore;
   }
 
  public int calcEconScore() {
    return this.capital / game.getLargestEarnings();
   }
 
   public String getName() {
     return name;
   }
 
   public int getClientID() {
     return clientID;
   }
 
   public void setClientID(int clientID) {
     this.clientID = clientID;
   }
 
   public boolean isAcceptCornContract() {
     return acceptCornContract;
   }
 
   public void setAcceptCornContract(boolean acceptCornContract) {
     this.acceptCornContract = acceptCornContract;
   }
 
   public boolean isAcceptSwitchgrassContract() {
     return acceptSwitchgrassContract;
   }
 
   public void setAcceptSwitchgrassContract(boolean acceptSwitchgrassContract) {
     this.acceptSwitchgrassContract = acceptSwitchgrassContract;
   }
 
   public String getCurrentUser() {
     return currentUser;
   }
 
   public void setCurrentUser(String currentUser) {
     this.currentUser = currentUser;
   }
 
   public void setField(Integer fieldNum, String crop) {
 //    System.out.println("planting " + crop + " on field " + fieldNum);
     switch (crop){
     case "grass":
       fields.get(fieldNum).setCrop(Crop.GRASS);
       break;
     case "corn":
       fields.get(fieldNum).setCrop(Crop.CORN);
       break;
     case "cover":
       fields.get(fieldNum).setCrop(Crop.COVER);
       break;
     case "none":
       fields.get(fieldNum).setCrop(Crop.FALLOW);
       break;
     }
   }
 
   public void changeFieldManagement(int fieldnum, String technique, boolean value) {
     Field field = fields.get(fieldnum);
     switch (technique){
     case "fertilizer":
       field.setFertilize(value);
       break;
     case "pesticide":
       field.setPesticide(value);
       break;
     case "tillage":
       field.setTill(value);
       break;
 
     }
 
   }
 
   public void setCapital(int i) {
     capital = i;
   }
 
   public boolean isReady() {
     return ready;
   }
 
   public void setReady(boolean ready) {
     this.ready = ready;
   }
 
   public double getPhosphorous() {
     return phosphorous;
   }
 
   public void updatePhosphorous() {
     double cornCount =0;
     for(Field f:this.fields){
       if(f.getCrop() == Crop.CORN){
         cornCount ++;
       }
     }
     double cornRatio = cornCount / this.fields.size();
     this.phosphorous = Math.pow(10, (.79 * cornRatio) - 1.44);
 
   }
 
   public double getGBI() {
     return GBI;
   }
 
   public void updateGBI() {
     //this is going to take some spatial specificty, so save for later
     //Fields are about 65 meters on each side if they are each an acre, so for now maybe
   }
 
   public int getEnvRank() {
     return envRank;
   }
 
   public void setEnvRank(int envRank) {
     this.envRank = envRank;
   }
 
   public int getEnergyRank() {
     return energyRank;
   }
 
   public void setEnergyRank(int energyRank) {
     this.energyRank = energyRank;
   }
 
   public int getEconRank() {
     return econRank;
   }
 
   public void setEconRank(int econRank) {
     this.econRank = econRank;
   }
 
   public double getOverallScore() {
     return overallScore;
   }
 
   public void setOverallScore(double d) {
     this.overallScore = d;
   }
 
   public int getOverallRank() {
     return overallRank;
   }
 
   public void setOverallRank(int overallRank) {
     this.overallRank = overallRank;
   }
 
   public LinkedList<FarmHistory.HistoryYear> getHistory() {
     return history.getHistory();
   }
 
   public void addHistoryYear() {
     // TODO Add real prices
 
     double cornYield = this.calcTotalCornYield();
 
     double grassYield = calcTotalGrassYield();
     System.out.println("corn yield: " + cornYield);
     history.addYear(capital, soilSubscore, waterSubscore, this.getOverallScore(), getEconScore(), getEnvScore(),
     getEnergyScore(), getOverallRank(), getEconRank(), getEnvRank(), getEnergyRank(),
     cornYield * Game.CORNPRICE, grassYield * Game.GRASSPRICE, cornYield, grassYield);
   }
 
   private double calcTotalGrassYield() {
     double yield = 0;
     for(Field f:fields){
       if(f.getCrop() == Crop.GRASS){
         yield += f.calculateYield();
       }
     }
     return yield;
   }
 
   private double calcTotalCornYield() {
     double yield = 0;
 //    System.out.print("Fields: ");
     for(Field f:fields){
 //      System.out.print(f.getCrop() + ", ");
       if(f.getCrop() == Crop.CORN){
         yield += f.calculateYield();
 //        System.out.print(" yield("+f.calculateYield()+")");
       }
     }
 //    System.out.print("\n");
     return yield;
   }
 
   private double calcAvgGrassYield() {
     double yield = 0;
     double count = 0;
     for(Field f:fields){
       if(f.getCrop() == Crop.GRASS){
         yield += f.calculateYield();
         count ++;
       }
     }
     return yield/count;
   }
 
   private double calcAvgCornYield() {
     double yield = 0;
     double count = 0;
     for(Field f:fields){
       if(f.getCrop() == Crop.CORN){
         yield += f.calculateYield();
         count ++;
       }
     }
     return yield/count;
   }
 
  public void removeHarvestedCrops() {
    // TODO Auto-generated method stub

  }

 }
