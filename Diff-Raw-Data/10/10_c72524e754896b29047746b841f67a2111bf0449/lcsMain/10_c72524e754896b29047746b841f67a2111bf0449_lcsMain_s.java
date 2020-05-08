 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package lcs;
 
 import lcs.ui.main.*;
 import lcs.model.*;
 import java.io.*;
 import javax.swing.*;
 import java.util.*;
 import javax.swing.table.*;
 /**
  *
  * @author Timothy
  */
 public class lcsMain {
 
     public lcsMain(){
         dictionary = new lcsDictionary();
         sounds = new lcsSounds();
         syllable = new lcsSyllableStructure();
         writingSystem = new lcsWritingSystem();
         posRules = new lcsPosRules();
     }
 
     public void setDictionary(lcsDictionary dict){
         dictionary = dict;
     }
 
     public void setSounds(lcsSounds sounds){
         this.sounds = sounds;
     }
 
     public void setSyllable(lcsSyllableStructure syllable){
         this.syllable = syllable;
     }
 
     public void setWritingSystem(lcsWritingSystem system){
         writingSystem = system;
     }
 
     public void setPosRules(lcsPosRules rules){
         posRules = rules;
     }
 
     public lcsDictionary getDictionary(){
         return dictionary;
     }
 
     public lcsSounds getSounds(){
         return sounds;
     }
 
     public lcsSyllableStructure getSyllableStructure(){
         return syllable;
     }
 
     public lcsWritingSystem getWritingSystem(){
         return writingSystem;
     }
 
     public lcsPosRules getPosRules(){
         return posRules;
     }
 
     public void loadDictionary(){
         JFileChooser jfc = new JFileChooser(".");
         int chosenOption = jfc.showOpenDialog(this.mainFrame);
         if(chosenOption != jfc.CANCEL_OPTION){
             File definitions =jfc.getSelectedFile();
             definitionsName = definitions.getName();
             if (definitionsName.endsWith(".csv")){
                 definitionsName = definitionsName.substring(0,
                         definitionsName.length()-4);
             }
             words.clear();
             dictionary.clear();
             wordCount = 0;            
             try{
                 BufferedReader in = new BufferedReader(new FileReader(definitions));
                 String defLine = " ";
                while(!defLine.equals("")){
                     System.out.println(">"+defLine+"<");
                     String splitter = ",";
                    defLine = in.readLine();
                     defLine = defLine.replace("\"","");
                     String[] stuff = {"","","",""};
                     String[] stff = defLine.split(splitter);
                     stuff[0] = strip(stff[0]).trim();
                     stuff[1] = strip(stff[1]).trim();
                    //System.out.println(stuff[1]);
                     if(stff.length<3){
                         stuff[2]=" ";
                         stuff[3]="0";
                     }else{
                         stuff[2]=strip(stff[2]).trim();
                     }
                     if(stff.length<4){
                         stuff[3] = "2";
                     } else {
                         stuff[3] = stff[3];
                     }
                     lcsDictionaryEntry rde = new lcsDictionaryEntry(stuff);
                     if(sounds.getVowels().size()>0 && sounds.getConsonants().size()>0){
                         do{
                             rde.setMeaning(rde.generateMeaning(syllable, 
                                     sounds.getVowels(), sounds.getConsonants(),
                                     rde.getPos(), posRules,
                                     rde.getDefinition(),maximumSyllables));
                         }while(words.contains(rde.getMeaning()));
                     }
                     //System.out.println(rde.getDefinition()+":"+rde.getMeaning());
                     dictionary.addEntry(rde);
                     words.addElement(rde.getMeaning());
                     wordCount++;
                     //updateTable();
                }//while(!defLine.equals("***"));
                 in.close();
             }
             catch (IOException ex){
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
             }
             catch(NullPointerException ex){
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
             }
             updateTable();
         }
     }
 
     public void updateTable(){
         DefaultTableModel dtm = (DefaultTableModel)mainFrame.getMainPanel().getMainTable().getModel();
         lcs.ui.table.lcsCellRenderer tcr = new lcs.ui.table.lcsCellRenderer();
         dtm.setRowCount(0);
         dtm.setRowCount(wordCount);
         tcr.setSystemFont(writingSystem.getInfo().get("Font")+"");
         for(int a = 0; a < wordCount; a++){
             lcsDictionaryEntry rwe = dictionary.getEntryAt(a);
             dtm.setValueAt(a + "", a, 0);
             dtm.setValueAt(rwe.getPos(), a, 1);
             dtm.setValueAt(rwe.getDefinition(), a, 2);
             dtm.setValueAt(rwe.getMeaning(), a, 3);
             if(writingSystem != null){
                 if(writingSystem.getInfo().get("System") == "Alphabet"){
                     HashMap phonemes = (HashMap)writingSystem.getLetterMap();
                     Object[] keys = phonemes.keySet().toArray();
                     java.util.Arrays.sort(keys, new lcs.util.LengthReverseComparator());
                     HashMap glyphUseMap = (HashMap)writingSystem.getGlyphUseMap();
                     int glyphsPerLetter = Integer.parseInt(writingSystem.getInfo().get("GlyphsPerLetter")+"");
                     int ucaseVal = 0;
                     int lcaseVal = 0;
                     String capRule = (String)writingSystem.getInfo().get("CapRule");
                     if (glyphUseMap.containsValue("Upper")){
                         for(int q=0; q<glyphsPerLetter; q++){
                             if (glyphUseMap.get("Glyph_"+(q+1)).equals("Upper")){
                                 ucaseVal=q;
                             }
                             if (glyphUseMap.get("Glyph_"+(q+1)).equals("Lower")){
                                 lcaseVal=q;
                             }
                         }
                     }
                     int initVal = 0;
                     int medVal = 0;
                     int finVal = 0;
                     int isoVal = 0;
                     if (glyphUseMap.containsValue("Initial")){
                         for (int q=0; q<glyphsPerLetter; q++){
                             if (glyphUseMap.get("Glyph_"+(q+1)).equals("Initial")){
                                 initVal = q;
                             }
                             if (glyphUseMap.get("Glyph_"+(q+1)).equals("Medial")){
                                 medVal = q;
                             }
                             if (glyphUseMap.get("Glyph_"+(q+1)).equals("Final")){
                                 finVal = q;
                             }
                             if (glyphsPerLetter > 3 &&
                                     glyphUseMap.get("Glyph_"+(q+1)).equals("Isolated")){
                                 isoVal = q;
                             }
                         }
                     }
                     int pos = 0;
                     String m = rwe.getMeaning();
                     String ws="";
                     while(pos < m.length()){
                         for (int q = 0; q < keys.length; q++){
                             String key = (String)keys[q];
                             if(pos + key.length() > m.length())
                                 continue;
                             if(m.substring(pos,(pos + key.length())).equals(key)){
                                 pos += key.length();
                                 String[] glyphs = (phonemes.get(key)+"").split(" ");
                                 if (glyphUseMap.containsValue("Upper")){
                                     if(capRule.equals("Word")){
                                         if(ws.length() == 0){
                                             ws += glyphs[ucaseVal];
                                         } else {
                                             ws += glyphs[lcaseVal];
                                         }
                                     }else if (capRule.equals("Nouns")){
                                         if(ws.length() == 0 && rwe.getPos().equals("Noun")){
                                             ws += glyphs[ucaseVal];
                                         } else {
                                             ws += glyphs[lcaseVal];
                                         }
                                     }else{
                                         ws += glyphs[lcaseVal];
                                     }
                                 } else if (glyphUseMap.containsValue("Lower")){
                                     ws += glyphs[lcaseVal];
                                 }
                                 if (glyphUseMap.containsValue("Initial")){
                                     if(ws.length() == 0){
                                         ws += glyphs[initVal];
                                     }else if(pos == (m.length())){
                                         ws += glyphs[finVal];
                                     } else {
                                         ws += glyphs[medVal];
                                     }
                                 }
                                 break;
                             }
                         }
                         //System.out.println();
                     }
                     dtm.setValueAt(ws, a, 4);
                 }
                 if(writingSystem.getInfo().get("System") == "Abjad"){
                     System.out.println("Using Abjadic System");
                 }
                 if(writingSystem.getInfo().get("System") == "Abugida"){
                     System.out.println("Using Abugidic System");
                 }
                 if(writingSystem.getInfo().get("System") == "Syllabary"){
                     HashMap syllables = (HashMap)writingSystem.getLetterMap();
                     Object[] keys = syllables.keySet().toArray();
                     java.util.Arrays.sort(keys, new lcs.util.LengthReverseComparator());
                     int pos = 0;
                     String m = rwe.getMeaning();
                     String ws="";
                     while(pos < m.length()){
                         for (int q = 0; q < keys.length; q++){
                             String key = (String)keys[q];
                             if(pos + key.length() > m.length())
                                 continue;
                             if(m.substring(pos,(pos + key.length())).equals(key)){
                                 pos += key.length();
                                 String glyph = syllables.get(key)+"";
                                 ws += glyph;
                             }
                         }
                     }
                     dtm.setValueAt(ws, a, 4);
                 }
             }
         }
     }
 
 
     public static void main(String[] args){
         MainFrame mf = new MainFrame();
         mf.setVisible(true);
         lcsMain lm = new lcsMain();
         lm.mainFrame = mf;
         mf.getMainPanel().setMain(lm);
     }
 
     public static String strip(String raw){
         String stripped = raw;
         if(stripped.startsWith("\"")){
             stripped = stripped.substring(1);
         }
         if (stripped.endsWith("\"")){
             stripped = stripped.substring(0, stripped.length()-1);
         }
         return stripped;
     }
 
     public void setMaximumSyllables(int max){
         maximumSyllables = max;
     }
 
     private lcsDictionary dictionary;
     private lcsSounds sounds;
     private lcsSyllableStructure syllable;
     private lcsWritingSystem writingSystem;
     private lcsPosRules posRules =new lcsPosRules();
     private MainFrame mainFrame;
     private String definitionsName;
     private Vector words = new Vector();
     private int wordCount = 0;
     private int maximumSyllables = 3;
 }
