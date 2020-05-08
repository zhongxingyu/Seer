 /*
  * This class holds all data during the game to have a samll amount of I/O 
  */
 package test;
 
 import java.awt.Color;
 import java.util.Arrays;
 import java.util.LinkedList;
 import javax.swing.JOptionPane;
 
 
 /**
  *
  * @author Jannik
  */
 public class Game1 {
 
     public static final int _startCredits = 100;
     
     public int credits;
     public int points;
     public int round;
     public int professor; //added by Julia
     public String professorIcon; //="/pictures/prof1_transparent.png"; //added by Julia - now has a value just for test will be loaded from game file, initial value on the beginning: "/pictures/prof1_transparent.png"
     public final String professorIconPath[] = { 
                                                  "/pictures/prof1_transparent.png",
                                                  "/pictures/prof2_transparent.png",
                                                  "/pictures/prof3_transparent.png"      
                                                 };
     public int professorIconNum;    //needed for saving the game
     public int barNum = 0; //value defines attribute statusbar which is actually clicked: 0: none, 1: knowledge, 2: motivation, 3: tiredness
     public Student[] studentArray; //added by Jörg
     public double[] rowIntelligence;
     public double airQuality;
     public double noise;
     public int quietingCounter=0;
     public int lecturer_counter=3;
     public boolean teamwork = false;
     public boolean shortBreak = false;
     public boolean windowClosed = true;
     private Boolean[] cheated = new Boolean[5];
     
     /**
      * Items are public to have an easy access from every class
      */
     public Item duplo;
     public Item redBull;
     public Item cheatSheet;
     public Item omniSenseAudio;
 
 
     public int getSemester() {
         return ((round -1 ) / 3 + 1);
     }
 
     public boolean getCheated(int semester) {
         int i = semester - 2;
         if (i < 0 | i > 5) {
             return false;
         }
         return cheated[i];
     }
 
     public boolean setCheated(int semester, boolean flag) {
         int i = semester - 2;
         if (i < 0 | i > 5) {
             return false;
         }
         cheated[i] = flag;
         return true;
     }
 
     public boolean setCheated(int semester) {
         return setCheated(semester, true);
     }
     
     public void initAttr(){
         for(int i= 0; i< studentArray.length; i++){
             studentArray[i].initMotivation();
             studentArray[i].initTiredness();
         }
     }
 
     /**
      * initializes the array of students.
      */
     @Deprecated public final void initArray() {
         studentArray = new Student[30];
         for (int i = 0; i < 30; i++) {
             studentArray[i] = new Student(i);
         }
     }
     
     public final void initArray(LinkedList<LinkedList> students){
         studentArray = new Student[students.size()];
         int i = 0;
         while(!students.isEmpty()){
             studentArray[i] = new Student(students.pop());
             i++;
         }
     }
     /**
      * @param add1  used to determine the changing of the students motivation in relation to the outward conditions (laptop open, teamwork, etc.). Regularly a negative value.
      * @param add2  used to determine the changing of the students motivation in relation to the outward conditions (laptop open, teamwork, etc.). Regularly a positive value.
      */
      public void updateArray (double add1, double add2, double add3){
       int counter = 0;
       int endCounter=5;
       for (int j=0; j<6; j++){
             for (int i=counter; i<endCounter; i++){
                 if (quietingCounter>0&&add1>=0){
                     add1=-1;
                 }
                 else if(quietingCounter>0){
                     add1=add1-1;
                 }
                 studentArray[i].changeMotivation(add1);
                 studentArray[i].changeTiredness(add2);
                 studentArray[i].updateKnowledge(add3, rowIntelligence[j]);   //changed by Kira: Added another variable and changed updateKnowledge in Student.java
             }
             counter=counter+5;
             endCounter=endCounter+5;
        }
      }
 
      public void calculateRowIntelligence(){
         rowIntelligence = new double[6];
         int rowStart=0;
         int rowEnd=5;
        int helper=0;
         double row=0;
         for (int i=0; i<6; i++){
             for (int j=rowStart; j<rowEnd;j++){
                 row = row+studentArray[j].getIntelligence();
                helper++;
             }
             row=row/5;
             rowIntelligence[i]=row;
             row=0;
             rowStart=rowStart+5;
             rowEnd=rowEnd+5;
         }
      }
      
      public void examTime(double examvalue){
          for (int i = 0; i < 30; i++) {
              if (studentArray[i].present){
                  double knowledge=studentArray[i].getKnowledge();
                  if(knowledge<examvalue){
                    studentArray[i].present=false;
                  } 
              }
          }
      }
      /**
       * initializes the attributes of the room in general
       */
      public void initRoom(){
          this.airQuality=100;
          this.noise=Math.round(Math.random()*30);
      }
      
      /**
       * @param value    The value of the airQuality
       * 
       * Setter used to avoid values x<0 or x>100
       */
      
      public void setAirQuality(double value) {
         if (!windowClosed){
             value=value+5;
         }
         if(value<0){
             this.airQuality= 0;
         }else if(value>100){
             this.airQuality=100;
         }else{
             this.airQuality = value;
         }
     }
      
       /**
       * @param value    The value of the noise
       * 
       * Setter used to avoid values x<0 or x>100
       */
     public void setNoise(double value) {
         if (!windowClosed){
             value=value+0.5;
         }
         if(value<0){
             this.noise= 0;
         }else if(value>100){
             this.noise=100;
         }else{
             this.noise = value;
         }
     }
      /**
       * @param factor1  describes how much value AirQuality changes. Regularly a negative value.
       * @param factor2  describes how much value Noise changes. Regularly a positive value.
       * 
       * This function updates the attributes of the room.
       */
      public void updateRoom(double factor1, double factor2){
          setAirQuality(this.airQuality + factor1);
          setNoise(this.noise + factor2);
      }
      /**
      * Receives data (Item Objects) from Item.java and
      * stores them in the fitting Item Objects in this class.
      * That's how each item can be called by name.
      */
     
     public final void putItem(String name, int amount, int available) {
 
         if (name.equals(Item._duploName)) {
             this.duplo = new Item(name, amount, available);
         }
         if (name.equals(Item._redBullName)) {
             this.redBull = new Item(name, amount, available);
         }
         if (name.equals(Item._spickerName)) {
             this.cheatSheet = new Item(name, amount, available);
         }
         if (name.equals(Item._omniSenseName)) {
             this.omniSenseAudio = new Item(name, amount, available);
         }
     }
 
     public Student[] getArray() {
         return studentArray;
     }
 
     @Deprecated public Game1() {
         this.putItem(Item._duploName, 1, 0);
         this.putItem(Item._redBullName, 1, 0);
         this.putItem(Item._spickerName, 0, 2);
         this.putItem(Item._omniSenseName, 0, 4);
         this.round = 1;
         this.credits = 100;
         this.points = 0;
         initArray();
         for (int i = 0; i < cheated.length; i++) {
             cheated[i] = false;
         }
     }
     
     public Game1(LinkedList<LinkedList> savegame, LinkedList<LinkedList> inventory){
         LinkedList<String> help;
         savegame.pop();
         help = savegame.pop();
         this.credits = new Integer(help.pop());
         this.points = new Integer(help.pop());
         this.round = new Integer(help.pop());
         this.professor = new Integer(help.pop());
         this.professorIconNum = new Integer(help.pop());
         this.professorIcon = professorIconPath[this.professorIconNum];
         help = savegame.pop();
         for(int i = 0; i<5; i++){
             if(help.pop().equals("0")){
             this.cheated[i] = false;
             } else {
                 this.cheated[i] = true;
             }
         }
         initArray(savegame);
         help = inventory.pop();
         putItem(Item._duploName, new Integer(help.get(0)), new Integer(help.get(1)));
         help = inventory.pop();
         putItem(Item._redBullName, new Integer(help.get(0)), new Integer(help.get(1)));
         help = inventory.pop();
         putItem(Item._spickerName, new Integer(help.get(0)), new Integer(help.get(1)));
         help = inventory.pop();
         putItem(Item._omniSenseName, new Integer(help.get(0)), new Integer(help.get(1)));
         
         //this.round=1;
     }
     
 /**
  * this function takes an array of 30 buttons and paints them to visualize the students attributes.
  * It takes the number of the actually clicked bar of this class to paint the students by taking the right attribute.
  * @param studButtons array of 30 buttons which should be painted
  */
     public void barClicked(javax.swing.JButton[] studButtons){        
         if(this.barNum==0){
             for(int i=0; i<30; i++){
             Color color = new Color(220, 220, 220);
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         }else if(this.barNum==1){
             for(int i=0; i<30; i++){
             Color color = new Color((int)(this.studentArray[i].getKnowledge()*2.55), 0, 0);
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         } else if(this.barNum==2){
             for(int i=0; i<30; i++){
             Color color = new Color(0, (int)(this.studentArray[i].getMotivation()*2.55), 0);
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         } else if(this.barNum==3){
             for(int i=0; i<30; i++){
             Color color = new Color(0, 0, (int)(this.studentArray[i].getTiredness()*2.55));
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         }
     }
     
     public static void initNewSavefile(){
         LinkedList<LinkedList> mainlist = new LinkedList();
         LinkedList<String> sublist = new LinkedList();
         sublist.add("1");
         mainlist.add(new LinkedList(sublist));
         sublist = new LinkedList();
         sublist.add(Integer.toString(_startCredits));
         sublist.add("0");
         sublist.add("1");
         sublist.add(Integer.toString((int) Math.round(Math.random() * 100 + 1)));
         sublist.add("0");
         mainlist.add(new LinkedList(sublist));
         sublist = new LinkedList();
         for(int i = 0; i<5; i++){
             sublist.add("0");
         }
         mainlist.add(new LinkedList(sublist));
         for(int i = 0; i<30; i++){
             sublist = new LinkedList();
             sublist.add("0");
             sublist.add(Double.toString(Math.round((1.3 + Math.random() % 0.7) * 100) / 100d));     //WEHE EINER MACHT DAS "d" WEG! /Dawid
             sublist.add(Integer.toString((int)(Math.random()*5)));
             sublist.add("0");
             sublist.add("1");
             mainlist.add(new LinkedList(sublist));
         }
         String inventory = "1,0\n1,0\n0,2\n0,4";
         try{
             CSVHandling.writeCSV(mainlist, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._gameFileName);
             CSVHandling.writeFile(inventory, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._inventoryFileName);
         }catch (Exception e){
             e.printStackTrace();
         }        
     }
     
     public static void loadGame(){
         
         LinkedList savegame = null;
         LinkedList inventory = null;
         try{
             savegame = CSVHandling.readCSV(Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._gameFileName);
             inventory = CSVHandling.readCSV(Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._inventoryFileName);
         }catch (Exception e){
             e.printStackTrace();
         }
         
         if(savegame != null & inventory != null){
             Sims_1._maingame = new Game1(savegame, inventory);
         } else {
             System.err.println("Something, somewhere went horribly wrong while loading the game!");
         }
     }
     
     public static void saveGame(){
         LinkedList<LinkedList> mainlist = new LinkedList();
         LinkedList<String> sublist = new LinkedList();
         sublist.add("1");
         mainlist.add(new LinkedList(sublist));
         sublist = new LinkedList();
         sublist.add(Integer.toString(Sims_1._maingame.credits));
         sublist.add(Integer.toString(Sims_1._maingame.points));
         sublist.add(Integer.toString(Sims_1._maingame.round));
         sublist.add(Integer.toString(Sims_1._maingame.professor));
         sublist.add(Integer.toString(Sims_1._maingame.professorIconNum));
         mainlist.add(new LinkedList(sublist));
         sublist = new LinkedList();
         for(int i = 0; i<5; i++){
             if (Sims_1._maingame.cheated[i] == false){
                 sublist.add("0");
             } else {
                 sublist.add("1");
             }
         }
         mainlist.add(new LinkedList(sublist));
         for(int i = 0; i<30; i++){
             sublist = new LinkedList();
             sublist.add(Double.toString(Sims_1._maingame.getArray()[i].getKnowledge()));
             sublist.add(Double.toString(Sims_1._maingame.getArray()[i].getIntelligence()));
             sublist.add(Integer.toString(Sims_1._maingame.getArray()[i].iconNum));
             if(Sims_1._maingame.getArray()[i].getCheatAvailable() == false){
                 sublist.add("0");
             } else {
                 sublist.add("1");
             }
             if(Sims_1._maingame.getArray()[i].present == false){
                 sublist.add("0");
             } else {
                 sublist.add("1");
             }
             mainlist.add(new LinkedList(sublist));
         }
         String inventory = "";
         inventory += Sims_1._maingame.duplo.amount + "," + Sims_1._maingame.duplo.available + "\n";
         inventory += Sims_1._maingame.redBull.amount + "," + Sims_1._maingame.redBull.available + "\n";
         inventory += Sims_1._maingame.cheatSheet.amount + "," + Sims_1._maingame.cheatSheet.available + "\n";
         inventory += Sims_1._maingame.omniSenseAudio.amount + "," + Sims_1._maingame.omniSenseAudio.available + "\n";
         try{
             CSVHandling.writeCSV(mainlist, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._gameFileName);
             CSVHandling.writeFile(inventory, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._inventoryFileName);
         }catch (Exception e){
             e.printStackTrace();
         }   
     }
 }
