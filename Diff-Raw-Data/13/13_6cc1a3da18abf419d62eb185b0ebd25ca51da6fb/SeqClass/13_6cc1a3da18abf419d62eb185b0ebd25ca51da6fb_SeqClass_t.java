 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package makedecoyfasta;
 
 import java.util.ArrayList;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author dfermin
  */
 class SeqClass {
     
     private String id;
     private String defline;
     private String seqStr;
     private String revStr;
     private String revPepStr;
     private int seqLen;
     private int minLen; // min. peptide length
     private int maxLen; // max. peptide length
     private ArrayList<Integer> sitesAry;
     private ArrayList<Character> seqAry;
     private SortedMap<Integer, Peptide> pepMap;
     private SortedMap<Integer, Peptide> revProtMap;
     
     public String getId() { return id; }
     public String getDefline() { return defline; }
     public String getSeqStr() { return seqStr; }
     public String getRevPepStr() { return revPepStr; }
     public String getRevProtStr() { return revStr; }
     public int getLen() { return seqLen; }
     
     
     public SeqClass() {
         id = "";
         seqStr = "";
         defline = "";
         revStr = "";
         revPepStr = "";
         seqLen = 0;
         
         // Hard code peptide length requirements
         minLen = 7; 
         maxLen = 30;
         
         sitesAry = new ArrayList<Integer>();
         pepMap = new TreeMap<Integer, Peptide>();
         revProtMap = new TreeMap<Integer, Peptide>();
         
     }
     
     
     public void append(String x) { 
         seqLen = seqStr.length();
         int pos = seqLen;
         
         for(int i = 0; i < x.length(); i++) {
             char c = x.charAt(i);
             
             if( !Character.isLetter(c) ) continue;
             
             pos += i;
             
             if( (c == 'K') || (c == 'R') ) sitesAry.add( pos );
             
             seqStr += c;
         }
     }
     
     
     public void parseFastaHdr(String line) {
         Pattern pat = Pattern.compile(">(sp|tr)\\|(.+)\\|\\w+ (.+)");
         Matcher mat = pat.matcher(line);
         
         if(mat.matches()) {
             id = mat.group(2);
             defline = mat.group(3);
             return;
         }
         
         pat = Pattern.compile(">(\\S+) (.+)");
         mat = pat.matcher(line);
         
         if(mat.matches()) {
             id = mat.group(1);
             defline = mat.group(2);
             return;
         }
         
     }
 
 
     
 /********************
  * This version of the digest function follows the trypsin rule:
  * Cut at K or R but not if they are followed by P.
  * 
  * @param whichSeq 
  *********************/
 public void digest(int whichSeq) {
         seqLen = seqStr.length();
         String curPep = null;
         int pepStart = 0;
         Peptide P = null;
         
         String curSeq = null;
         
         // Determine which sequence we will be working with
         if(whichSeq == 0) curSeq = seqStr; // forward sequence
         else curSeq = revStr;              // reverse sequence
         
         seqAry = new ArrayList<Character>();
         for(int i = 0; i < seqLen; i++) {
             seqAry.add( curSeq.charAt(i) );
         }
         
         
         for(int j = 1; j <= (seqLen - 1); j++) {
             int i = j - 1;
             if(i == 0) curPep = ""; // initialized for first iteration
             
             
             char c = seqAry.get(i);
             char d = seqAry.get(j);
             
        
             curPep += c; // add this residue to the growing string
             
             
             if( ((c == 'K') || (c == 'R')) && ( d != 'P') ) { //end of peptide
                P = new Peptide(curPep, pepStart);
                
                if(whichSeq == 0) pepMap.put(pepStart, P);
                else revProtMap.put(pepStart, P);
                
                pepStart = i + 1;
                curPep = "";
                P = null;
             }
         }
         
         if(!curPep.isEmpty()) {
             curPep += seqAry.get( (seqLen -1) );
             P = new Peptide(curPep, pepStart);
             if(whichSeq == 0) pepMap.put(pepStart, P);
             else revProtMap.put(pepStart, P);
             curPep = "";
         }
     }
 
 
     
     public void printPeps() {
         for(int i : pepMap.keySet()) {
             Peptide P = pepMap.get(i);
             P.printPeptide();
         }
     }
     
     
     public void buildReversePeptideSeq() {
         
         // reversed peptides
         revPepStr = "";
         for(int i : pepMap.keySet()) {
             Peptide P = pepMap.get(i);
             revPepStr += P.decoySeq;
         }
     }
 
     
     public void reverseSeq() {
         revStr = "";
         seqLen = seqStr.length();
         StringBuilder sb = new StringBuilder(seqStr);
         revStr = sb.reverse().toString();
     }
     
     
     // Returns sequence as FASTA formatted string
     public String getFasta(int i) {
         String ret = "";
         StringBuilder sb = new StringBuilder();
         int FASTA = 50;
         
         String srcStr = "";
         
         
         switch(i) {
             case 0: // forward sequence
                 srcStr = seqStr;
                 break;
             
             case 1: // reverse peptides
                 srcStr = revPepStr;
                 break;
             
             case 2: // reverse protein sequence
                 srcStr = revStr;
                 break;
                 
             default:
                 break;
         }
         
       
		seqLen = srcStr.length();
         for(int p = 0; p < seqLen; p++) {
             sb.append( srcStr.charAt(p));
             if( (p > 0) && ((p % FASTA) == 0) ) sb.append('\n');
         }
         ret = sb.toString() + "\n";
         
         return ret;
     }
 
     
     public String getDigestTable(int d) {
         String ret = "";
         String line = "";
     
         //"protid\tisDecoy\tpepStart\tpepEnd\tpepLen\tpepSeq\tpepMas\n";
         
         if(d == 0) { // forwards
             for(Peptide P : pepMap.values()) {
                 
                 if( (P.pepLen >= minLen) && (P.pepLen <= maxLen) ) {
                     line += id + "\t0\t" + P.pepStart + "\t" + P.pepEnd + "\t" + P.pepLen 
                          + "\t" + P.seq + "\t" + P.MHplus + "\n";
                 }
             }
         }
         
         if(d == 1) { // reversed peptides
             for(Peptide P : pepMap.values()) {
                 
                 if( (P.pepLen >= minLen) && (P.pepLen <= maxLen) ) {
                     line += "revPep_" + id + "\t1\t" + P.pepStart + "\t" + P.pepEnd + "\t" + P.pepLen 
                          + "\t" + P.decoySeq + "\t" + P.MHplus + "\n";
                 }
             }
         }
         
         if(d == 2) { // reversed protein
             for(Peptide P : revProtMap.values()) {
                 
                 if( (P.pepLen >= minLen) && (P.pepLen <= maxLen) ) {
                     line += "revProt_" + id + "\t1\t" + P.pepStart + "\t" + P.pepEnd + "\t" + P.pepLen 
                          + "\t" + P.seq + "\t" + P.MHplus + "\n";
                 }
             }
         }
         
         ret = line;
         return ret;
     }
     
     
 }
