 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Token;
 import files.WriteFile;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import storage.SymbolTable;
 
 /**
  *
  * @author charles_yu102
  */
 public class CodeGenerator {
     
     // Attributes
     String javaCode;
     SymbolTable symbolTable;
     ArrayList<Token> tokens;
     ArrayList<String> lexemes;
     String output = "";
     
     // Constructor
     public CodeGenerator(ArrayList<Token> tokens, SymbolTable symbolTbl){
         this.tokens = tokens;
         this.symbolTable = symbolTbl;
         this.lexemes = new ArrayList<String>();
         //this.displayLexemes();
     }
     
     // Methods
     //<editor-fold defaultstate="collapsed" desc="Debugging Methods">
     public void displayLexemes(){
         System.out.println("CODE GENERATOR ");
         for(int i=0; i < this.lexemes.size(); i++){
             System.out.println(lexemes.get(i));
         }
     }
     //</editor-fold>
     
     public void adjustLexemes(){
         for(int i=0; i<this.tokens.size(); i++){
             Token curToken = this.tokens.get(i);
             String curLexeme = curToken.getToken();
             // if lexeme is an int, charm string or id get token info
             if(curLexeme.equals("int") || curLexeme.equals("char") || curLexeme.equals("string") || curLexeme.equals("id")){           
                 this.lexemes.add(curToken.getTokenInfo());
             } else{ // keyword
                 this.lexemes.add(curLexeme);
             }
         }
     }
     
     public void translateToJava(){
         this.javaCode = "public class TARCCode{";
         for(int i=0; i<this.lexemes.size(); i++){
             String curLexeme = lexemes.get(i);
             // Convert to java
             if(curLexeme.equals("|")){
                 javaCode+=" || ";
             } else if(curLexeme.equals("&")){
                 javaCode+=" && ";
             } else if(curLexeme.equals("&")){
                 javaCode+=" && ";
             } else if(curLexeme.equals("end")){
                 javaCode+="\n} \n";
             } else if(curLexeme.equals("#func")){
                 javaCode+="\npublic static void ";
             } else if(curLexeme.equals("#main")){
                 javaCode += "\npublic static void main(String[] args)";
                 i += 2;
             } else if(curLexeme.equals("#int") || curLexeme.equals("#char") || curLexeme.equals("#boolean")){
                 javaCode += curLexeme.substring(1)+" ";
             } else if(curLexeme.equals("#puts")){
                javaCode += "System.out.println";
             } else if(curLexeme.equals("else")){
                 javaCode += "\n } else{ \n";
             } else if(curLexeme.equals(")")){
                 boolean check = false;
                 for(int j=i; check == false && j>0; j--){
                     if(lexemes.get(j).equals("(")){
                         check = true;
                         if(lexemes.get(j-1).equals("if") || lexemes.get(j-1).equals("while")){
                             javaCode += "){ \n";
                         } else{
                             javaCode += curLexeme+" ";
                         }
                     }
                 } 
             } else if(curLexeme.charAt(0) == '#'){
                 javaCode += curLexeme.substring(1);
             } else{
                 if(curLexeme.equals("=")){
                     javaCode += " " +curLexeme + " ";
                 }else{
                     javaCode += curLexeme;
                 }
             }
         }
         this.javaCode += "}";
     }
     
     public void writeCompileRun(){
         WriteFile file = new WriteFile(new File("TARCCode.java"), javaCode); 
         file.write();
         try{
             Process pro;
             pro = Runtime.getRuntime().exec("javac TARCCode.java");
             printLines(" stderr:", pro.getErrorStream());
             pro = Runtime.getRuntime().exec("java TARCCode");
             printLines(" Output:", pro.getInputStream());
             printLines(" stderr:", pro.getErrorStream());
             pro.waitFor();
             System.out.println(" exitValue() " + pro.exitValue());
         }catch (Exception e) {
         }
     }
     
     private void printLines(String name, InputStream ins) throws Exception {
         String line;
         BufferedReader in = new BufferedReader(new InputStreamReader(ins));
         while ((line = in.readLine()) != null) {
             System.out.println(name + " " + line);
            this.output += line+"\n";
         }
     }
     
     public String getOutput(){
         return this.output;
     }
     
 }
