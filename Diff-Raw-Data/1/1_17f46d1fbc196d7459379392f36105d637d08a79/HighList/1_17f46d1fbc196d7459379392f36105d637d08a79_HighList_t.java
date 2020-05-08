 import java.util.*;
 import java.io.*;
 
 public class HighList {
     private ArrayList<HighListItem> list;
    int min;
 
     /**
      * Construct list of high scores via input file.
      * @param saved input file.
      */
     HighList() {
         list = new ArrayList<HighListItem>();
         min = 1000000; // INIT with large magic num
         String name;
         int score;
         int difficulty;
         try {
             Scanner s = new Scanner(new FileReader("HighScores.txt"));
             while(s.hasNext()) {
                 name = s.next();
                 score = s.nextInt();
                 difficulty = s.nextInt();
                 HighListItem r = new HighListItem(name,score,difficulty);
                 // System.out.println(r.getRecord());
                 this.list.add(r);
                 if(score < min) min = score;
             }
             s.close();
         } catch( FileNotFoundException e) {
             e.printStackTrace();
         }
         this.enList();
     }
 
     /**
      * Construct list of high scores in program.
      * @param org program generated list.
      */
     HighList(ArrayList<HighListItem> org) {
         list = new ArrayList<HighListItem>();
         min = 1000000; // INIT with large magic num
         for(HighListItem I : org) {
             HighListItem N = I.clone();
             this.list.add(N);
             if(I.getScore() < min) min = I.getScore();
         }
         this.enList();
     }
 
     /**
      * Save high scores to file HighScores.
      */
     public void saveRecord() {
         try {
             FileWriter f = new FileWriter("HighScores.txt");
             f.write(this.getMsg());
             f.close();
         } catch( Exception e ){
             e.printStackTrace();
         }
     }
 
     /**
      * add new record to the high score list.
      */
     public void addToList(String name, int score, int difficulty) {
         HighListItem i = new HighListItem(name,score,difficulty);
         this.list.add(i);
         this.enList();
     }
 
     /**
      * @return sorted list information.
      */
     public String getMsg() {
         if(list.isEmpty()) {
             return "No High Scores";
         } else {
             String msg = "";
             // msg += "<html><body><ol>";
             //int i = 1;
             for(HighListItem I : list){
                 //msg += i;
                 //msg += ". ";
                 msg += I.getRecord();
                 //i++;
             }
             // msg += "</ol></body></html>";
             return msg;
         }
     }
 
     /**
      * sort and keep list within limit.
      */
     void enList() {
         // sort list
         Collections.sort(list,new HSLComp());
 
         // cut to 10 items
         int size = list.size();
         if(size>10) {
             while(size>10){
                 size--;
                 list.remove(size);
             }
         }
     }
 
 }
