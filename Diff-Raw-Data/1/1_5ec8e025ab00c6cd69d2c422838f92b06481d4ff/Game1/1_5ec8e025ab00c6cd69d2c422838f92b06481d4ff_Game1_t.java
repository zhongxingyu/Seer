 /*
  * This class holds all data during the game to have a samll amount of I/O 
  */
 package test;
 
 import java.awt.Color;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedList;
 import javax.swing.JOptionPane;
 
 /**
  * @author Dawid Rusin
  * @author Jannik
  * @author Jörg Woditschka
 * @author Kira Schomber
  */
 public class Game1 {
 
     public static final int _startCredits = 100;
     public int credits;
     public int overallCredits;
     public int points;
     public int round;
     public boolean gameover = false;
     public Date startTimePlayed;
     public Long timePlayed;
     public double averageKnowledge;
     public double avarageMotivation;
     public double averageTiredness;
     public int professor; //added by Julia
     public String professorIcon;
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
     public int quietingCounter = 0;
     public int lecturer_counter;
     public int remainingStudents;
     public int failedStudents = 0;
     public boolean teamwork = false;
     public boolean shortBreak = false;
     public boolean windowClosed = true;
     public boolean windowChangesNoise = true; //flag that sets whether noise is changed when closing the window
     private Boolean[] cheated = new Boolean[5];
     /**
      * Items are public to have an easy access from every class
      */
     public Item duplo;
     public Item redBull;
     public Item cheatSheet;
     public Item omniSenseAudio;
 
     public int getSemester() {
         return ((round - 1) / 3 + 1);
     }
 
     /**
      * Check if the cheat sheet was used in a semester
      *
      * @param semester the semester to check for
      * @return true if it was used in the specified semester, false otherwise
      */
     public boolean getCheated(int semester) {
         int i = semester - 2;
         if (i < 0 | i > 5) {
             return false;
         }
         return cheated[i];
     }
 
     /**
      * Set the cheat sheet flag on the given semester
      *
      * @param semester the semester to set
      * @param flag true or false to set or remove the flag
      */
     public void setCheated(int semester, boolean flag) {
         int i = semester - 2;
         if (i < 0 | i > 5) {
         } else {
             cheated[i] = flag;
         }
     }
 
     /**
      * Set the cheat sheet flag in the specified semester
      *
      * @param semester the semester to set
      */
     public void setCheated(int semester) {
         setCheated(semester, true);
     }
 
     /**
      * Initializes the student attributes motivation and tiredness
      */
     public void initAttr() {
         for (int i = 0; i < studentArray.length; i++) {
             studentArray[i].initMotivation();
             studentArray[i].initTiredness();
         }
     }
 
     /**
      * Initializes the array of students
      *
      * @param students list with students (taken from the save-file)
      */
     public final void initArray(LinkedList<LinkedList> students) {
         studentArray = new Student[students.size()];
         int i = 0;
         while (!students.isEmpty()) {
             studentArray[i] = new Student(students.pop());
             if (studentArray[i].present) {
                 this.remainingStudents++;
             }
             i++;
         }
     }
 
     /**
      * Updates the values of the students in the student array
      *
      * @param add1 used to determine the changing of the students motivation in
      * relation to the outward conditions (laptop open, teamwork, etc.).
      * Regularly a negative value.
      * @param add2 used to determine the changing of the students motivation in
      * relation to the outward conditions (laptop open, teamwork, etc.).
      * Regularly a positive value.
      */
     public void updateArray(double add1, double add2, double add3) {
         int counter = 0;
         int endCounter = 5;
         for (int j = 0; j < 6; j++) {
             for (int i = counter; i < endCounter; i++) {
                 if (quietingCounter > 0 && add1 >= 0) {
                     add1 -= 0.2;
                 } else if (quietingCounter > 0) {
                     add1 -= 0.1;
                 }
                 studentArray[i].changeMotivation(add1);
                 studentArray[i].changeTiredness(add2);
                 studentArray[i].updateKnowledge(add3, rowIntelligence[j]);   //changed by Kira: Added another variable and changed updateKnowledge in Student.java
             }
             counter = counter + 5;
             endCounter = endCounter + 5;
         }
     }
 
     public void calculateRowIntelligence() {
         rowIntelligence = new double[6];
         int rowStart = 0;
         int rowEnd = 5;
         int cnt = 0;
         double row = 0;
         for (int i = 0; i < 6; i++) {
             for (int j = rowStart; j < rowEnd; j++) {
                 if (studentArray[i].present) {
                     row = row + studentArray[j].getIntelligence();
                     cnt++;
                 }
             }
             row = row / cnt;
             rowIntelligence[i] = row;
             row = 0;
             rowStart = rowStart + 5;
             rowEnd = rowEnd + 5;
             cnt = 0;
         }
     }
 
     public void examTime(double examvalue) {
         for (int i = 0; i < 30; i++) {
             if (studentArray[i].present) {
                 double knowledge = studentArray[i].getKnowledge();
                 if (knowledge < examvalue && !studentArray[i].getCheatAvailable()) {
                     studentArray[i].present = false;
                     remainingStudents--;
                     failedStudents++;
                 } else if (studentArray[i].getCheatAvailable()) {
                     studentArray[i].setCheatAvailable(false);
                 }
             }
         }
     }
 
     /**
      * initializes the attributes of the room in general
      */
     public void initRoom() {
         this.airQuality = 100;
         this.noise = Math.round(Math.random() * 30);
     }
 
     /**
      * @param value The value of the airQuality
      *
      * Setter used to avoid values x<0 or x>100
      */
     public void setAirQuality(double value) {
         if (!windowClosed) {
             value = value + 5;
         }
         if (value < 0) {
             this.airQuality = 0;
         } else if (value > 100) {
             this.airQuality = 100;
         } else {
             this.airQuality = value;
         }
     }
 
     /**
      * @param value The value of the noise
      *
      * Setter used to avoid values x<0 or x>100
      */
     public void setNoise(double value) {
         if (!windowClosed) {
             value = value + 0.5;
         }
         if (value < 0) {
             this.noise = 0;
         } else if (value > 100) {
             this.noise = 100;
         } else {
             this.noise = value;
         }
     }
 
     /**
      * @param factor1 describes how much value AirQuality changes. Regularly a
      * negative value.
      * @param factor2 describes how much value Noise changes. Regularly a
      * positive value.
      *
      * This function updates the attributes of the room.
      */
     public void updateRoom(double factor1, double factor2) {
         setAirQuality(this.airQuality + factor1);
         setNoise(this.noise + factor2);
     }
 
     /**
      * Receives data (Item Objects) from Item.java and stores them in the
      * fitting Item Objects in this class. That's how each item can be called by
      * name.
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
 
     /**
      * This method retruns the aray of students
      *
      * @return array of students
      */
     public Student[] getArray() {
         return studentArray;
     }
 
     /**
      * Loads the game, creates _maingame in Sims_1
      *
      * @param savegame list with the content of the save file
      * @param inventory list with the content of the inventory
      */
     public Game1(LinkedList<LinkedList> savegame, LinkedList<LinkedList> inventory) {
         LinkedList<String> help;
 //        savegame.pop();
         help = savegame.pop();
         this.remainingStudents = 0;
         this.credits = new Integer(help.pop());
         this.points = new Integer(help.pop());
         this.round = new Integer(help.pop());
         this.professor = new Integer(help.pop());
         this.professorIconNum = new Integer(help.pop());
         this.professorIcon = professorIconPath[this.professorIconNum];
         this.lecturer_counter = new Integer(help.pop());
 //        this.timePlayed = new Long(help.pop());
         help = savegame.pop();
         for (int i = 0; i < 5; i++) {
             if (help.pop().equals("0")) {
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
      * this function takes an array of 30 buttons and paints them to visualize
      * the students attributes. It takes the number of the actually clicked bar
      * of this class to paint the students by taking the right attribute.
      *
      * @param studButtons array of 30 buttons which should be painted
      */
     public void barClicked(javax.swing.JButton[] studButtons) {
         if (this.barNum == 0) {
             for (int i = 0; i < 30; i++) {
                 if (this.studentArray[i].present) {
                     Color color = new Color(220, 220, 220);
                     studButtons[i].setBackground(color);
                     studButtons[i].setOpaque(true);
                 }
             }
         } else if (this.barNum == 1) {
             int factor;
             int sub;
             int smstr = getSemester();
             switch (smstr) {
                 case 1:
                     factor = 10;
                     sub = 0;
                     break;
                 case 2:
                     factor = 10;
                     sub = 10;
                     break;
                 case 3:
                     factor = 100 / 15;
                     sub = 20;
                     break;
                 case 4:
                     factor = 100 / 15;
                     sub = 35;
                     break;
                 case 5:
                     factor = 5;
                     sub = 50;
                     break;
                 case 6:
                     factor = 5;
                     sub = 70;
                     break;
                 default:
                     factor = 1;
                     sub = 0;
                     break;
             }
             for (int i = 0; i < 30; i++) {
                 if (this.studentArray[i].present) {
                     int knowledge = (int) ((this.studentArray[i].getKnowledge() - sub) * 2.55) * factor;
                     if (knowledge > 255) {
                         knowledge = 255;
                     }
                     Color color = new Color(255-knowledge, knowledge, 0);
                     studButtons[i].setBackground(color);
                     studButtons[i].setOpaque(true);
                 }
             }
         } else if (this.barNum == 2) {
             for (int i = 0; i < 30; i++) {
                 if (this.studentArray[i].present) {
                     Color color = new Color(255 - (int) (this.studentArray[i].getMotivation() * 2.55), (int) (this.studentArray[i].getMotivation() * 2.55), 0);
                     studButtons[i].setBackground(color);
                     studButtons[i].setOpaque(true);
                 }
             }
         } else if (this.barNum == 3) {
             for (int i = 0; i < 30; i++) {
                 if (this.studentArray[i].present) {
                     Color color = new Color((int) (this.studentArray[i].getTiredness() * 2.55), 255 - (int) (this.studentArray[i].getTiredness() * 2.55), 0);
                     studButtons[i].setBackground(color);
                     studButtons[i].setOpaque(true);
                 }
             }
         }
     }
 
     /**
      * Creates a new save file to use for a new game with random values for
      * intelligence and initial values for the rest
      */
     public static void initNewSavefile() {
         //build the list for game.txt
         LinkedList<LinkedList> mainlist = new LinkedList();
         LinkedList<String> sublist = new LinkedList();
 //        sublist.add("1");
 //        mainlist.add(new LinkedList(sublist));
 //        sublist = new LinkedList();
         sublist.add(Integer.toString(_startCredits));   //credits
         sublist.add("0");   //points
         sublist.add("1");   //round
         sublist.add(Integer.toString((int) Math.round(Math.random() * 100 + 1)));   //professor
         sublist.add("0");   //professorIconNumber
         sublist.add("3");   //lecturer_counter
         mainlist.add(new LinkedList(sublist));
         sublist = new LinkedList();
         for (int i = 0; i < 5; i++) {
             sublist.add("0");   //cheat-flags
         }
         mainlist.add(new LinkedList(sublist));  //student-array
         for (int i = 0; i < 30; i++) {
             sublist = new LinkedList();
             sublist.add("0");   //knowledge
             sublist.add(Double.toString(Math.round((1.3 + Math.random() % 0.7) * 100) / 100d));   //intelligence
             sublist.add(Integer.toString((int) (Math.random() * 5)));   //picture index
             sublist.add("0");   //cheat sheet available
             sublist.add("1");   //omni sense available
             mainlist.add(new LinkedList(sublist));
         }
         //build the string for inventory.txt
         String inventory = "1,0\n1,0\n0,2\n0,4";
         try {
             //write the save file and the inentory file
             CSVHandling.writeCSV(mainlist, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._gameFileName);
             CSVHandling.writeFile(inventory, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._inventoryFileName);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Loads the game for the current user
      */
     public static void loadGame() {
 
         LinkedList savegame = null;
         LinkedList inventory = null;
         try {
             savegame = CSVHandling.readCSV(Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._gameFileName);
             inventory = CSVHandling.readCSV(Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._inventoryFileName);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         if (savegame != null & inventory != null) {
             Sims_1._maingame = new Game1(savegame, inventory);
         } else {
             System.err.println("Something, somewhere went horribly wrong while loading the game!");
         }
     }
 
     /**
      * saves the current game, same scheme as initNewSavefile()
      */
     public static void saveGame() {
         if (!Sims_1._maingame.gameover) {
             LinkedList<LinkedList> mainlist = new LinkedList();
             LinkedList<String> sublist = new LinkedList();
 //        sublist.add("1");
 //        mainlist.add(new LinkedList(sublist));
 //        sublist = new LinkedList();
             sublist.add(Integer.toString(Sims_1._maingame.credits));
             sublist.add(Integer.toString(Sims_1._maingame.points));
             sublist.add(Integer.toString(Sims_1._maingame.round));
             sublist.add(Integer.toString(Sims_1._maingame.professor));
             sublist.add(Integer.toString(Sims_1._maingame.professorIconNum));
             sublist.add(Integer.toString(Sims_1._maingame.lecturer_counter));
 //        sublist.add(Long.toString(Sims_1._maingame.timePlayed));
             mainlist.add(new LinkedList(sublist));
             sublist = new LinkedList();
             for (int i = 0; i < 5; i++) {
                 if (Sims_1._maingame.cheated[i] == false) {
                     sublist.add("0");
                 } else {
                     sublist.add("1");
                 }
             }
             mainlist.add(new LinkedList(sublist));
             for (int i = 0; i < 30; i++) {
                 sublist = new LinkedList();
                 sublist.add(Double.toString(Sims_1._maingame.getArray()[i].getKnowledge()));
                 sublist.add(Double.toString(Sims_1._maingame.getArray()[i].getIntelligence()));
                 sublist.add(Integer.toString(Sims_1._maingame.getArray()[i].iconNum));
                 if (Sims_1._maingame.getArray()[i].getCheatAvailable() == false) {
                     sublist.add("0");
                 } else {
                     sublist.add("1");
                 }
                 if (Sims_1._maingame.getArray()[i].present == false) {
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
             try {
                 CSVHandling.writeCSV(mainlist, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._gameFileName);
                 CSVHandling.writeFile(inventory, Sims_1._dataFolderName + "/" + Sims_1._mainuser.getAccountname() + "/" + Sims_1._inventoryFileName);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
         /**
          * This method adds bonus credits on the users credits on base of the knowledge of the students at the end of a round.
          */
     public void gainBonusCredits(){
             int smstr = getSemester();
             int sub;
             switch (smstr) {
                 case 1:
                     sub = 0;
                     break;
                 case 2:
                     sub = 10;
                     break;
                 case 3:
                     sub = 20;
                     break;
                 case 4:
                     sub = 35;
                     break;
                 case 5:
                     sub = 50;
                     break;
                 case 6:
                     sub = 70;
                     break;
                 default:
                     sub = 0;
                     break;
 }
         double result=0;
         for(int i=0; i<30; i++){
             if(studentArray[i].present){
                 result+=studentArray[i].getKnowledge()-sub;
             }
         }
         credits+=(int)result;
         //System.out.println("Credits gained: "+(int)result);
     }
 }
