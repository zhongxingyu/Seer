 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Token;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import storage.SymbolTable;
 import storage.TokenValuePairs;
 
 /**
  *
  * @author charles_yu102
  */
 public class LexicalAnalyzer {
     
     // Attributes
     private TokenValuePairs tvp;
     private String sourceCode;
     ArrayList<String> lexemes;     // Parsed from the source code
     ArrayList<Token> tokens;       // List of tokens to be given to the parser
     SymbolTable symbolTable;
        
     // Constructor
     public LexicalAnalyzer(String sourceCode, SymbolTable symbolTable){
        this.tvp = new TokenValuePairs();
        this.sourceCode = sourceCode;
        this.lexemes = new ArrayList<String>();
        this.tokens = new ArrayList<Token>();
        this.symbolTable = symbolTable;
     }
     
     // Methods
     public void getLexemes(){
         // Remove spaces from source code
         String spaceDelims = "[ \t\n]+";
         String[] codes = this.sourceCode.split(spaceDelims);
         
         // Parse source code
         String delims = "[(),;+-*/%&|!<>=;'\"]+{}";
         for(int i=0; i<codes.length; i++){
             StringTokenizer st = new StringTokenizer(codes[i], delims, true);
             while(st.hasMoreTokens()){
                 String word = st.nextToken();
                 if(!lexemes.isEmpty() && checkDoubleDelim(word)){
                     lexemes.set(lexemes.size()-1, lexemes.get(lexemes.size()-1) + word);
                 }else{
                     lexemes.add(word);
                 }                
             }
         }   
         //this.displayLexemes();
     }
     
     public Boolean checkDoubleDelim(String curDelim){
         Boolean check = false;
         String lastDelim = lexemes.get(lexemes.size()-1);
         if(curDelim.equals("=") && (lastDelim.equals("=") || lastDelim.equals("!") ||lastDelim.equals("<") ||lastDelim.equals(">") )){
             check = true;
         }
         return check;
     }
     
     public ArrayList<Token> getTokensFormSymbolTable(){
         String curScope = "";
         
         // Check every lexeme type
         for(int i=0; i<this.lexemes.size(); i++){
             String curLexeme = lexemes.get(i);
             // Update current scope
             if(curLexeme.equals("#main")){
                 curScope = "#main";
             } else if(i!=lexemes.size()-1 && curLexeme.equals("#func")){
                 curScope = lexemes.get(i+1);
             }
             Token container = new Token();
             String type = tvp.getType(curLexeme);
             if(type != null) {                                                // Keyword
                 container.setToken(curLexeme);      
             } else if(isNumeric(curLexeme)){                                  // Number
                 container.setToken("int");
                 container.setTokenInfo(curLexeme);
             } else if(curLexeme.length() == 1 && tokens.get(tokens.size()-1).getToken().equals("'")){    // Character
                 container.setToken("char");
                 container.setTokenInfo(curLexeme);
             } else if(tokens.size()>1 && tokens.get(tokens.size()-1).getToken().equals("\"")){   // String
                 String string = "";
                for(; !lexemes.get(i).equals("\"") ;i++){
                     string += " "+lexemes.get(i);
                 }
                 i--;
                 container.setToken("string");
                 container.setTokenInfo(string);
             } else{                                                           // Identifier
                 // Get datatype if declaration or function
                 if(i!=0 && lexemes.get(i-1).contains("#")){
                     String datatype = lexemes.get(i-1);
                     String scope = (datatype.equals("#func"))?"null":curScope;
                     symbolTable.insert("id", curLexeme, datatype,scope);
                 }
                 container.setToken("id");
                 container.setTokenInfo(curLexeme);
             }
             this.tokens.add(container);
         }
         return this.tokens;
     }
 
     private boolean isNumeric(String lexeme){
         try{
             Integer num = Integer.parseInt(lexeme);
         }catch(NumberFormatException e){
             return false;
         }
         return true;
     }
     
     // Display lexemes
     private void displayLexemes(){
         System.out.println("LEXEMES");
         for(int i=0; i<this.lexemes.size(); i++){
             System.out.println("Lexeme "+i+": "+this.lexemes.get(i));
         }
     }
 }
