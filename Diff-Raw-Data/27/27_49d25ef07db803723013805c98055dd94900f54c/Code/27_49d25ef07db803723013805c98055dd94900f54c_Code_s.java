 package no.hist.gruppe5.pvu.coderacer;
 
 import com.badlogic.gdx.Game;
 import java.util.ArrayList;
 import no.hist.gruppe5.pvu.PVU;
 
 public class Code {
 
     private ArrayList<String> codes;
     private int i = 0;
     private int charCounter = 0;
     private String correct;
     private String left;
 
     public Code() {
         this.codes = new ArrayList();
         codes.add("du er idioter");
         //codes.add("public static void main(String[] args) { ");
         //codes.add("Label label = new Label(\"Hei på deg\", new LabelStyle(Assets.primaryFont10px, Color.BLACK");
         correct = "";
         left = getCode();
     }
 
     public String getCode() {
         return codes.get(i);
     }
 
     public boolean equals(char input) {
         if (input == codes.get(i).charAt(charCounter)) {
             charCounter++;
             correct += input;
             left = getCode().substring(charCounter, getCode().length());
             if (codes.get(i).equals(correct)) {
                 i++;
                 charCounter = 0;
                 correct = "";
                 if(!isFinished()) left = getCode();
             }
             return true;
         }
 
         return false;
     }
     public boolean isFinished() {
         if(i >= codes.size()) return true;
         else return false;
     }
     public int getCharCounter() {
         return charCounter;
     }
 
     public String getCorrect() {
         return correct;
     }
 
     public String getLeft() {
         return left;
     }
     private int getMaxScore() {
         int counter = 0;
         for(int i = 0; i < codes.size(); i++) {
             for(int j = 0; j < codes.get(i).length(); j++) {
                 counter++;
             }
         }
         return counter;
     }
    public char getGrade(int score) {
        if(((double) score/getMaxScore()) >= 0.95) {
            return 'A';
        }
        else if(((double) score/getMaxScore()) < 0.98 && ((double) score/getMaxScore()) >= 0.85) {
            return 'B';
        }
        else if(((double) score/getMaxScore()) < 0.85 && ((double) score/getMaxScore()) >= 0.60) {
            return 'C';
        }
        else if(((double) score/getMaxScore()) < 0.60 && ((double) score/getMaxScore()) >= 0.48) {
            return 'D';
        }
        else if(((double) score/getMaxScore()) < 0.48 && ((double) score/getMaxScore()) >= 0.35) {
            return 'E';
        }
        else return 'F';
     }
 }
