 
 /**
  * Write a description of class Controller here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 
 import java.io.*;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Queue;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 public class Controller
 {
     // instance variables
     public View view;
     private Model model;
     private int points;
     private int ptsDed;
     private int[] hitCount;
     private int[][] conditionCount;
     public int dvrcMostPreferred, dvrcLeastPreferred, dvrcLeft, dvrcRight;
     public int dvrMostPreferred, dvrLeastPreferred, dvrLeft, dvrRight;
     public int dcMostPreferred, dcLeastPreferred, dcLeft, dcRight;
     public int icMostPreferred, icLeastPreferred;
     private ArrayList<Condition> conditionArray;
     Condition dvrc, dvr, dc, ic, lastCondition = null;
     private boolean sound;
     Queue<String> manualProgramList;
     private boolean instructionSheetShown = false;
     static BufferedWriter writer;
     static int caret;
     // constructor
     public Controller()
     {
         Setup.set();
         System.out.println("DEBUG: Controller constructor Setup.getRD():  " + Setup.getRD());
         points = Setup.getPoints();
         ptsDed = Setup.getPtLost();
         view = new View(this);
         model = new Model();
         hitCount  = new int[14];
         conditionCount = new int[4][4];
         conditionArray = new ArrayList<Condition>();
         dvrc = new Condition();
         dvr = new Condition();
         dc = new Condition();
         ic = new Condition();
         sound = false;
         conditionArray.add(dvrc);
         conditionArray.add(dvr);
         conditionArray.add(dc);
         conditionArray.add(ic);
         manualProgramList = Setup.getManualProgram();
         String filename = "" + (System.currentTimeMillis()/10000);
         filename += ".csv";
         try
         {
             writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
         } catch(IOException ex) {
             System.out.println("DEBUG - caught exception making a writer, exiting.");
             System.exit(1);
         }
         
     }
     
     public void writePreferences()
     {
         writeToCSV("Group,Most Preferred, Left, Right, Least Preferred");
         writeToCSV("DVRC," + dvrcMostPreferred + "," + dvrcLeft + "," + dvrcRight + "," + dvrcLeastPreferred);
         writeToCSV("DVR," + dvrMostPreferred + "," + dvrLeft + "," + dvrRight + "," + dvrLeastPreferred);
         writeToCSV("DC," + dcMostPreferred + "," + dcLeft + "," + dcRight + "," + dcLeastPreferred);
         writeToCSV("IC," + icMostPreferred + ",,," + dvrcLeastPreferred);
     }
     
     public void calculatePointLoss(int group, int index)
     {
         switch(group)
         {
             case Model.DVRC_ENUM: if(index == dvrcMostPreferred) {
                 points -= ptsDed;
                 sound = false;
                 }
             else{
                 sound = true;
             }
             break;
             case Model.DVR_ENUM: if(index == dvrMostPreferred) {
                 points -= ptsDed;
                 sound = false;
             }
             else{
                 sound = true;
             }break;
             case Model.DC_ENUM: if(index == dcMostPreferred) {
                 points -= ptsDed;
                 sound = false;
             }
             else{
                 sound = true;
             }break;
             case Model.IC_ENUM: if(index == icMostPreferred) {
                 points -= ptsDed;
                 sound = false;
             }
            else{
                sound = true;
             }break;
         }
     }
     
     public char getCharCubeChar(int group, int index)
     {
         return model.getCharCubeChar(group, index);
     }
     
     public void updatePoints()
     {
         playSound();
         view.setPoints(points);
     }
 
     // methods
     public void showInstructionSheet()
     {
         System.out.println("DEBUG: showInstructionSheet() - Setting title to Instruction Sheet.");
         System.out.println("DEBUG: showInstructionSheet() - Setting the points to initial value of 100.");
         view.setPoints(points);
         System.out.println("DEBUG: showInstructionSheet() - envoke showInstructionSheet from view.");
         view.showInstructionSheet();
         writeToCSV("GROUP,SELECTED,NOT SELECTED");
     }
     
     public void showBaseline(int group, int left, int right)
     {
         System.out.println("DEBUG: Controller.showBaseline() - manually showing the baseline.");
         Pair thisPair = new Pair(left, right, group);
         System.out.println("DEBUG: Controller.showBaseline() - invoke view.establishPreference(" + model.getLeftChar(thisPair) + "," +  model.getRightChar(thisPair) + "," +  thisPair.getGroup() + "," + thisPair.getLeftIndex() + "," + thisPair.getRightIndex() + ")");    
         view.establishPreference(model.getLeftChar(thisPair), model.getRightChar(thisPair), thisPair.getGroup(), thisPair.getLeftIndex(), thisPair.getRightIndex());
     }
 
     public void getBaselineCondition()
     {
         System.out.println("DEBUG: getBaselineCondition() - Setting title to Baseline Condition.");
 
         System.out.println("DEBUG: getBaselineCondition() - Get Random Pair.");
         Pair thisPair = model.getRandomPair();
 
         System.out.println("DEBUG: getBaselineCondition() - invoke view.establishPreference(" + model.getLeftChar(thisPair) + "," +  model.getRightChar(thisPair) + "," +  thisPair.getGroup() + "," + thisPair.getLeftIndex() + "," + thisPair.getRightIndex() + ")");    
         view.establishPreference(model.getLeftChar(thisPair), model.getRightChar(thisPair), thisPair.getGroup(), thisPair.getLeftIndex(), thisPair.getRightIndex());
     }
 
     public void continueBaselineCondition()
     {
         if(model.isBaselineEstablished() == false) //  false
         {
             System.out.println("DEBUG - continueBaselineCondition() - isBaseLineEstablished is false");
             Pair thisPair = model.getRandomPair();
             System.out.println("DEBUG - continueBaselineCondition() - the following pair was selected:" + thisPair);
             System.out.println("DEBUG: continueBaselineCondition() - invoke view.establishPreference(" + model.getLeftChar(thisPair) + "," +  model.getRightChar(thisPair) + "," +  thisPair.getGroup() + "," + thisPair.getLeftIndex() + "," + thisPair.getRightIndex() + ")");    
             view.establishPreference(model.getLeftChar(thisPair), model.getRightChar(thisPair), thisPair.getGroup(), thisPair.getLeftIndex(), thisPair.getRightIndex());
         } else {
             System.out.println("DEBUG - continueBaselineCondition() - The baseline has been established. Show instructionSheet() for now.");
             view.showInstructionSheet();
             System.out.println("DEBUG - continueBaselineCondition() - print the baseline stats");
             printBaselineStats();
             System.out.println("DEBUG - continueBaselineCondition() - Set the preference.");
             setPreference();
             presentCondition();
         }
     }
     
    
     public void presentCondition()
     {
         System.out.println("DEBUG: presentCondition() - Setting title to Delayed Consequence.");
         Condition thisCondition = null;
         Random rand = new Random();
         do
         {
             System.out.println("DEBUG: presentCondition() - Randomly Selectiong condition");
             if(conditionArray.size() == 1)
             {
                 thisCondition = conditionArray.get(0);
                 System.out.println("DEBUG: presentCondition() - DVRC shown " + dvrc.getTimesShown() + " times.");
                 System.out.println("DEBUG: presentCondition() - DVR shown " + dvr.getTimesShown() + " times.");
                 System.out.println("DEBUG: presentCondition() - DC shown " + dc.getTimesShown() + " times.");
                 System.out.println("DEBUG: presentCondition() - IC shown " + ic.getTimesShown() + " times.");
                 break;
             } else if(conditionArray.size() > 1) {
                 thisCondition= conditionArray.get(rand.nextInt(conditionArray.size()));
             }
         } while(lastCondition == thisCondition);
         
         if(thisCondition == null)
         {
             printStatsToFile();
             view.gameOver(points);
             return;
         }
             lastCondition = thisCondition;
             thisCondition.incrementTimesShown();
             System.out.println("DEBUG: presentCondition() - Condition chosen: " + thisCondition + " shown " + thisCondition.getTimesShown() + " times.");
             
             if(thisCondition.getTimesShown() > 6)
             {
                 System.out.println("DEBUG: presentCondition() - Condition shown more than 6 times, removing form conditionArray.");
                 conditionArray.remove(thisCondition);
             }
             
             if(thisCondition == dvrc)
             {
                 view.dvrc(model.getCharCubeChar(Model.DVRC_ENUM, dvrcMostPreferred), model.getCharCubeChar(Model.DVRC_ENUM, dvrcLeastPreferred), Model.DVRC_ENUM, dvrcMostPreferred, dvrcLeastPreferred);
             }
             
             if(thisCondition == dvr)
             {
                 view.dvr(model.getCharCubeChar(Model.DVR_ENUM, dvrMostPreferred), model.getCharCubeChar(Model.DVR_ENUM, dvrLeastPreferred), Model.DVR_ENUM, dvrMostPreferred, dvrLeastPreferred);
             }
             
             if(thisCondition == dc)
             {
                 view.dc(model.getCharCubeChar(Model.DC_ENUM, dcMostPreferred), model.getCharCubeChar(Model.DC_ENUM, dcLeastPreferred), Model.DC_ENUM, dcMostPreferred, dcLeastPreferred);
             }
             
             if(thisCondition == ic)
             {
                 view.ic(model.getCharCubeChar(Model.IC_ENUM, icMostPreferred), model.getCharCubeChar(Model.IC_ENUM, icLeastPreferred), Model.IC_ENUM, icMostPreferred, icLeastPreferred);
             }
     }
     
     public void printStatsToFile()
     {
         System.out.println("Stats printed.");
     }
 
     public void setPreference()
     {
         // DVRC
         int max, min;
 
         max = 0;
         min = 0;
         boolean middleSet = false;
         for(int i = 0; i < 4; i++)
         {
             if(hitCount[i] > hitCount[max]) 
             {
                 // This index is higher than what is at index max. 
                 max = i;
             } 
 
             if(hitCount[i] < hitCount[min])
             {
                 min = i;
             }
         } 
         for(int j = 0; j < 4; j++)
         {
             if(middleSet) 
             {
                 if(j != max && j!= min)
                 {
                     System.out.println("DEBUG: - setPreference - dvrcLeft = " + j);
                     dvrcLeft = j;
                 }
                 System.out.println("DEBUG: - setPreference - j matches most or least\tmax: " + max + "\tmin: " + min + "\tj: " + j);
             } else {
                  if(j != max && j!= min)
                     {
                         System.out.println("DEBUG: - setPreference - dvrcRight = " + j);
                         dvrcRight = j;
                         middleSet = true;
                         System.out.println("DEBUG: - setPreference - j matches most or least\tmax: " + max + "\tmin: " + min + "\tj: " + j);
                     }
                 
             }
         }
         
         dvrcMostPreferred = max;
         dvrcLeastPreferred = min;
 
        
 
         max = 4;
         min = 4;
         for(int i = 4; i < 8; i++)
         {
             if(hitCount[i] > hitCount[max]) 
             {
                 // This index is higher than what is at index max. 
                 max = i;
             } 
 
             if(hitCount[i] < hitCount[min])
             {
                 min = i;
             }
         }
         middleSet = false;
          for(int j = 4; j < 8; j++)
         {
             if(middleSet) 
             {
                 if(j != max && j!= min)
                 {
                     System.out.println("DEBUG: - setPreference - dvrLeft = " + j);
                     dvrLeft = j-4;
                 }
                 System.out.println("DEBUG: - setPreference - j matches most or least\tmax: " + max + "\tmin: " + min + "\tj: " + j);
             } else {
                  if(j != max && j!= min)
                     {
                         System.out.println("DEBUG: - setPreference - dvrRight = " + j);
                         dvrRight = j-4;
                         middleSet = true;
                         System.out.println("DEBUG: - setPreference - j matches most or least\tmax: " + max + "\tmin: " + min + "\tj: " + j);
                     }
                 
             }
         }
         dvrMostPreferred = max-4;
         dvrLeastPreferred = min-4;
 
         
 
         max = 8;
         min = 8;
         for(int i = 8; i < 12; i++)
         {
             if(hitCount[i] > hitCount[max]) 
             {
                 // This index is higher than what is at index max. 
                 max = i;
             } 
 
             if(hitCount[i] < hitCount[min])
             {
                 min = i;
             }
         }
         
         middleSet = false;
          for(int j = 8; j < 12; j++)
         {
             if(middleSet) 
             {
                 if(j != max && j!= min)
                 {
                     System.out.println("DEBUG: - setPreference - dcLeft = " + j);
                     dcLeft = j-8;
                 }
                 System.out.println("DEBUG: - setPreference - j matches most or least\tmax: " + max + "\tmin: " + min + "\tj: " + j);
             } else {
                  if(j != max && j!= min)
                     {
                         System.out.println("DEBUG: - setPreference - dcRight = " + j);
                         dcRight = j-8;
                         middleSet = true;
                         System.out.println("DEBUG: - setPreference - j matches most or least\tmax: " + max + "\tmin: " + min + "\tj: " + j);
                     }
                 
             }
         }
         
         dcMostPreferred = max-8;
         dcLeastPreferred = min-8;
 
         
 
         max = 12;
         min = 12;
         for(int i = 12; i < 14; i++)
         {
             if(hitCount[i] > hitCount[max]) 
             {
                 // This index is higher than what is at index max. 
                 max = i;
             } 
         }
         
        if(max-12 == 0)
        {
            icMostPreferred = max-12;
            icLeastPreferred = 1;
        } else {
            icMostPreferred = 1;
            icLeastPreferred = 0;
        }
 
         System.out.println("DEBUG - setPreference() - dvrcMostPreferred = " + dvrcMostPreferred);
         System.out.println("DEBUG - setPreference() - dvrcLeastPreferred = " + dvrcLeastPreferred);
         System.out.println("DEBUG - setPreference() - dvrcLeft = " + dvrcLeft);
         System.out.println("DEBUG - setPreference() - dvrcRight = " + dvrcRight);
         System.out.println("DEBUG - setPreference() - dvrMostPreferred = " + dvrMostPreferred);
         System.out.println("DEBUG - setPreference() - dvrLeastPreferred = " + dvrLeastPreferred);
         System.out.println("DEBUG - setPreference() - dvrLeft = " + dvrLeft);
         System.out.println("DEBUG - setPreference() - dvrRight = " + dvrRight);
         System.out.println("DEBUG - setPreference() - dcMostPreferred = " + dcMostPreferred);
         System.out.println("DEBUG - setPreference() - dcLeastPreferred = " + dcLeastPreferred);
         System.out.println("DEBUG - setPreference() - dcLeft = " + dcLeft);
         System.out.println("DEBUG - setPreference() - dcRight = " + dvrcRight);
         System.out.println("DEBUG - setPreference() - icMostPreferred = " + icMostPreferred);
         System.out.println("DEBUG - setPreference() - icLeastPreferred = " + icLeastPreferred);
     }
     
     public int getDVRCleft()
     {
         return dvrcLeft;
     }
     
     public int getDVRCright()
     {
         return dvrcRight;
     }
     
     public int getDVRleft()
     {
         return dvrLeft;
     }
     
     public int getDVRright()
     {
         return dvrRight;
     }
     
     public int getDCleft()
     {
         return dcLeft;
     }
     
     public int getDCright()
     {
         return dcRight;
     }
 
     public void incrementHitCount(int group, int index)
     {
         int hitCountIndex = group*4+index;
         System.out.println("DEBUG - incrementHitCount() - incrementing hitCount[" + hitCountIndex + "]");
         hitCount[hitCountIndex]++;
     }
     
     public void incrementConditionCount(int group, int index)
     {
         System.out.println("DEBUG - incrementConditionCount() - incrementing conditionCount[" + group + "][" + index + "]");
         conditionCount[group][index]++;
     }
     
 
     public void printBaselineStats()
     {
         for(int i = 0; i < hitCount.length; i++)
         {
             if(i < 4)
             {
                 System.out.println("DVRC index[" + i + "]: " + hitCount[i]);
             } else if(i < 8) {
                 System.out.println("DVR index[" + (i-4) + "]: " + hitCount[(i-4)]);
             } else if(i < 12) {
                 System.out.println("DC index[" + (i-8) + "]: " + hitCount[(i-8)]);
             } else {
                 System.out.println("IC index[" + (i-12) + "]: " + hitCount[(i-12)]);
             }
         }
     }
     
     public void printConditionStats()
     {
         for(int i = 0; i < conditionCount.length; i++)
         {
             for(int j = 0; j < conditionCount[i].length; j++)
             {
                 if(i == 3 && j == 2)
                 {
                     break;
                 }
                 
                 switch(i)
                 {
                     case 0: System.out.print("\tDVRC index[" + j + "]:" + conditionCount[i][j]); break;
                     case 1: System.out.print("\tDVR index[" + j + "]:" + conditionCount[i][j]); break;
                     case 2: System.out.print("\tDC index[" + j + "]:" + conditionCount[i][j]); break;
                     case 3: System.out.print("\tIC index[" + j + "]:" + conditionCount[i][j]); break;
                 }
             }
             System.out.println();
         }
     }
     
     public void playSound()
     {
         if(sound == false)
         {
             View.playLossSound();
         }
         else
         {
             View.playWinSound();
         }
     }
     
     
     public void beginDvrcSequence()
     {
         System.out.println("DEBUG - beginDvrcSequence()");
         view.dvrc(model.getCharCubeChar(Model.DVRC_ENUM, dvrcMostPreferred), model.getCharCubeChar(Model.DVRC_ENUM, dvrcLeastPreferred), Model.DVRC_ENUM, dvrcMostPreferred, dvrcLeastPreferred);
     }
     
     public void beginDvrSequence()
     {
         view.dvr(model.getCharCubeChar(Model.DVR_ENUM, dvrMostPreferred), model.getCharCubeChar(Model.DVR_ENUM, dvrLeastPreferred), Model.DVR_ENUM, dvrMostPreferred, dvrLeastPreferred);
     }
     
     public void beginDcSequence()
     {
         view.dc(model.getCharCubeChar(Model.DC_ENUM, dcMostPreferred), model.getCharCubeChar(Model.DC_ENUM, dcLeastPreferred), Model.DC_ENUM, dcMostPreferred, dcLeastPreferred);
     }
     
     public void beginIcSequence()
     {
         view.ic(model.getCharCubeChar(Model.IC_ENUM, icMostPreferred), model.getCharCubeChar(Model.IC_ENUM, icLeastPreferred), Model.IC_ENUM, icMostPreferred, icLeastPreferred);
     }
     
     public void gameOver()
     {
         view.gameOver(points);
     }
     
     
     public void manualProgram()
     {
 //         
 //         while(manualProgramList.peek() != null)
 //         {
 //             String line = manualProgramList.remove();
 //             System.out.println(line);
 //             String[] lineArray = line.split(",");
 //             for(int i = 0; i < lineArray.length; i++)
 //             {
 //                 System.out.println("\t linArray[" + i + "]: " + lineArray[i]);
 //             }
 //         }
         if(instructionSheetShown)
         {
             
         if(manualProgramList.peek() != null)
         {
             String line = manualProgramList.remove();
             String[] lineArray = line.split(",");
             System.out.println("DEBUG - Parsing line: " + line);
             if(lineArray[0].equals("BASELINE_DVRC"))
             {
                 view.setBeginPanelColorDefault();
                 showBaseline(Model.DVRC_ENUM, Integer.parseInt(lineArray[1].trim()), Integer.parseInt(lineArray[2].trim()));
             } else if(lineArray[0].equals("START_BASELINE_DVRC") || lineArray[0].equals("START_BASELINE_DVR") || 
                         lineArray[0].equals("START_BASELINE_DC") || lineArray[0].equals("START_BASELINE_IC")) {
                 writeToCSV("GROUP,SELECTED,NOT SELECTED");
             }else if(lineArray[0].equals("BASELINE_DVR")) {
                  view.setBeginPanelColorDefault();
                 showBaseline(Model.DVR_ENUM, Integer.parseInt(lineArray[1].trim()), Integer.parseInt(lineArray[2].trim()));
             } else if(lineArray[0].equals("BASELINE_DC")) {
                  view.setBeginPanelColorDefault();
                 showBaseline(Model.DC_ENUM, Integer.parseInt(lineArray[1].trim()), Integer.parseInt(lineArray[2].trim()));
             } else if(lineArray[0].equals("BASELINE_IC")) { 
                  view.setBeginPanelColorDefault();
                 showBaseline(Model.IC_ENUM, Integer.parseInt(lineArray[1].trim()), Integer.parseInt(lineArray[2].trim()));
             } else if(lineArray[0].equals("DVRC_SEQUENCE")) { 
                 setPreference(); beginDvrcSequence(); 
             } else if(lineArray[0].equals("DVR_SEQUENCE")) { 
                 setPreference(); beginDvrSequence(); 
             } else if(lineArray[0].equals("DC_SEQUENCE")) { 
                 setPreference(); beginDcSequence(); 
             } else if(lineArray[0].equals("IC_SEQUENCE")) { 
                 setPreference(); beginIcSequence(); 
             } else { 
                 manualProgram();
             }
         } else {
             gameOver();
         }
     } else {
         instructionSheetShown = true;
         view.setPoints(Setup.getPoints());
         view.showInstructionSheet();
         
     }
     }
     
     public static void setCaret(int a)
     {
     	caret = a;
     }
     
     public static int getCaret()
     {
     	return caret;
     }
     
     
     public static void writeToCSV(String s)
     {
         try
         {
             writer.write(s + "\n");
         } catch(IOException e) {
             System.out.println("DEBUG - writeToCSV()");
             System.exit(1);
         }
         
     }
     
     public static void closeCSV()
     {
         try {writer.close();} catch (Exception ex) {}
     }
 }
