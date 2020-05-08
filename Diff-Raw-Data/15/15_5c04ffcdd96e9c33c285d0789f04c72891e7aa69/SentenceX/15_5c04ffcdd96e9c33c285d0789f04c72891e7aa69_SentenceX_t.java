 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gengram;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author lin
  */
 public class SentenceX {
 
     public void setTheSentence(sentence theSentence) {
         this.theSentence = theSentence;
     }
     private sentence theSentence = null;
     public SentenceX(sentence c)
     {
         theSentence = c;
     }
     public sentence getSentence() {
         return theSentence;
     }
     public String getSentenceKey() {
         return theSentence.orig;
     }
     public String[] getWords() {
         return this.theSentence.words;
     }
     public String[][] getOkayWords() {
         return this.theSentence.okay;
     }
     public String toString() {
         return this.theSentence.orig;
     }
     public void generateokays (ArrayList <String> oks) {
         if(tabModvalues == null)
             theSentence.generateokays();
         oks.addAll(theSentence.alts);
     }
     public Object[] getSentenceOptions () {
         return theSentence.alts.toArray();
     }
     public static Object[][] transpose(Object [][] mat) {
         int rows = mat.length;
         int cols = 0;
         for(Object[] o : mat) cols = o != null ? Math.max(cols, o.length) : cols;
         Object [][] tpose = new Object[cols][rows];
         for(int i = 0; i < rows; i++) {
             for(int j = 0; j < cols; j++) {
                 if(mat[i] == null || mat[i].length <= j) tpose[j][i] = "";
                 else tpose[j][i] = mat[i][j];
             }
         }
         return tpose;
     }
     public Object[][] getWordOptions() {
         String[][] okwords = getOkayWords();
         return transpose(okwords); 
     }
     public void debug() {
         theSentence.Debug(" --- SentenceX ---");
         if(tabModvalues == null) {System.out.println("null"); return; }
         for(ArrayList cells : tabModvalues) {
             for(Object cell : cells) {
                 if(cells.toString().trim().length() > 0) System.out.println(cell);
             }
         }
     }
     public boolean isExcluded(String word) {
         return excludes.contains(word);
     }
     public void addExclusion(String s) {
         excludes.add(s);
     }
     public ArrayList<ArrayList<Object>> getTabModvalues() {
         return tabModvalues;
     }
 
     public void setTabModvalues(ArrayList<ArrayList<Object>> tabModvalues) {
         this.tabModvalues = tabModvalues;
     }
     
     private ArrayList<ArrayList<Object>> tabModvalues = null;
     private ArrayList<String> excludes = new ArrayList<String>();
 
     public String dump(int level) {
         StringBuffer sb = new StringBuffer();
         if(tabModvalues == null) theSentence.generateokays();
         Object[] alts = getSentenceOptions();
        sb.append('*').append(this.theSentence.orig).append('\n');
         for(Object alt : alts) {
            String sent = (String)alt;
            if(sent.equals(this.theSentence.orig)) continue;
             boolean sel = true;
             String pad = "";
             for(String ex : excludes) {
                if(sent.contains(" "+ ex.trim())) {
                     sel = false; break;
                 }
             }
             if(level > 1) {
                 String format = "%"+(level - 1)+"s";
                 pad = String.format(format, " ").replace(' ', '\t');
             }
            pad = "\t";
             if(sel) sb.append(pad).append(alt).append('\n');
         }
         return sb.toString();
     }
 
 }
