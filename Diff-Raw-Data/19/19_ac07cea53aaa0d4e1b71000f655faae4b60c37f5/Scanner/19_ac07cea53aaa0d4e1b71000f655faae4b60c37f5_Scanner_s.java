 package eleven;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Scanner implements IScanner {
   
   private String originalString;
   private String concatenation = "";
   private ITokenCollection tokenCollection;
   private ArrayList<Command> commandCollection;
   private final Set<String> wordMoveTokens = new HashSet<String>(Arrays.asList("move", "mv", "moov"));
   private final Set<String> wordRedoTokens = new HashSet<String>(Arrays.asList("redo", "rd", "reedoo"));
   private final Set<String> wordGiveTokens = new HashSet<String>(Arrays.asList("give", "gv"));
   private final Set<String> wordUndoTokens = new HashSet<String>(Arrays.asList("undo", "ud", "undoo"));
   private final Set<String> wordAddHouseTokens = new HashSet<String>(Arrays.asList("addhouse", "ah", "add"));
   private final Set<String> wordRemoveHouseTokens = new HashSet<String>(Arrays.asList("removehouse", "rm", "remove"));
   private final Set<String> wordAnimateTokens = new HashSet<String>(Arrays.asList("animate", "am"));
   private final Set<String> wordTakeTokens = new HashSet<String>(Arrays.asList("take", "tk"));
    
   public Scanner(ITokenCollection tC, ArrayList<Command> cC) {
     setTokenCollection(tC);
     setCommandCollection(cC);
   }
   
   protected void tokensInArray() {
     boolean inAToken = false;
     boolean inACommand = false;
     int startMarker = 0;
     String tokenType = new String();
     for ( int index = 0; index < originalString.length(); index++ ) {
       char tokenChar = originalString.charAt(index);
 //    check first char
       if (Character.isDigit(tokenChar) || tokenChar == "+".charAt(0) || tokenChar == "-".charAt(0)) {
         if (!inAToken) {
           startMarker = index;
           tokenType = "Number";
           inAToken = true;
           inACommand = true;
         }
       } else if (Character.isLetter(tokenChar)) {
         if (!inAToken) { 
           startMarker = index;
           tokenType = "Word";
           inAToken = true;          
           inACommand = true;
         }
       } else { //blank 
         if (inAToken) { //and end of a token 
           if (tokenType == "Word") {
             String substr = originalString.substring(startMarker, index).toLowerCase();
             if (wordMoveTokens.contains(substr) ) {
               inACommand = true;
               if (inACommand) {                 
                 tokenCollection.addToken(new MoveWordToken(originalString.substring(startMarker, index)));
                 inAToken = false;
 //                add for other number tokens
                 int sM = index+1;
                 int numberOfNewChars = 0;
                 int followingNumberTokens = 0;
                 int j = sM;
                 while (followingNumberTokens < 2 && j < originalString.length()) {
                  char newerChar = originalString.charAt(j);
                   if (Character.isDigit(newerChar) || newerChar == "+".charAt(0) || newerChar == "-".charAt(0)) {
                     if (!inAToken) {
                       sM = j;
                       tokenType = "Number";
                       inAToken = true;
                       followingNumberTokens ++;
                       j++;
                       numberOfNewChars ++;
                     } else { //end of token
                       tokenCollection.addToken(new NumberToken(originalString.substring(sM, j)));            
                       inAToken = false;
                       tokenType = "";
                       concatenation += tokenCollection.getToken(tokenCollection.getTC().size()-1).getDescription() + ": " + tokenCollection.getToken(tokenCollection.getTC().size()-1).getStringValue() + " ";            
                       followingNumberTokens ++;
                       numberOfNewChars ++;
                       j++;
                     }
                   }
                 }
                 followingNumberTokens = 0;
                 inACommand = false;
                 commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-3), tokenCollection.getToken(tokenCollection.size()-2), tokenCollection.getToken(tokenCollection.size()-1)));
                 index = index + numberOfNewChars;
               }
               this.addConcatenation();
             } else if (wordRemoveHouseTokens.contains(substr) ) {
               tokenCollection.addToken(new RemoveHouseWordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             } else if (wordRedoTokens.contains(substr) ) {
               tokenCollection.addToken(new RedoWordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             } else if (wordGiveTokens.contains(substr) ) {
               tokenCollection.addToken(new GiveWordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             } else if (wordUndoTokens.contains(substr) ) {
               tokenCollection.addToken(new UndoWordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             } else if (wordAddHouseTokens.contains(substr) ) {
               tokenCollection.addToken(new AddHouseWordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             } else if (wordAnimateTokens.contains(substr) ) {
               tokenCollection.addToken(new AnimateWordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             } else if (wordTakeTokens.contains(substr) ) {
               tokenCollection.addToken(new TakeWordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             } else {
               tokenCollection.addToken(new WordToken(originalString.substring(startMarker, index)));
               commandCollection.add(new Command(tokenCollection.getToken(tokenCollection.size()-1)));
               this.addConcatenation();
             }
             startMarker = index;
             inAToken = false;
             tokenType = "";
           }
 //            else { //Number
 //              tokenCollection.addToken(new NumberToken(originalString.substring(startMarker, index)));            
 //              concatenation += tokenCollection.getToken(tokenCollection.getTC().size()-1).getDescription() + ": " + tokenCollection.getToken(tokenCollection.getTC().size()-1).getStringValue() + " ";            
 //              inAToken = false;
 //              tokenType = "";
 //              startMarker = index;
 //            }
         }
       }   
     }
   }
   public void processCommands(ITokenCollection tokenCollection){
       boolean inACommand = false;
       int startMarker = 0;
       if (!inACommand) {
         for ( int index = startMarker; index < tokenCollection.size(); index++ ) {
           wordOrNumberRD(tokenCollection, index);
         }
       }
   }
   public void wordOrNumberRD(ITokenCollection tC, int index) {
     if (tC.getToken(index) instanceof WordToken) {
       wordParamCountRD(tC, index);
     } else if (tC.getToken(index) instanceof NumberToken) {
       System.out.println("Number Token");
     } else {
       System.out.println("Not a Word or Number Token");
     }
   }
   public void wordParamCountRD(ITokenCollection tC, int index) {
     System.out.println(index);
     if (index+2 < tC.size() && tC.getToken(index+1) instanceof NumberToken && tC.getToken(index+2) instanceof NumberToken) {
       wordTwoParamRD(tC, index);
     } else if (index+1 < tC.size() && tC.getToken(index+1) instanceof NumberToken) {
       wordOneParamRD();
     } else {
       wordZeroParamRD(tC, index);
     }
   }
   public void wordZeroParamRD(ITokenCollection tC, int index) {
     if (tC.getToken(index) instanceof AddHouseWordToken) {
       System.out.println("Scanner(RD[0]): added a house");
     } else if (tC.getToken(index) instanceof RemoveHouseWordToken) {
       System.out.println("Scanner(RD[0]): removed a house");      
     } else if (tC.getToken(index) instanceof AnimateWordToken) {
       System.out.println("Scanner(RD[0]): toggel animation");     
     }
   }
   public void wordOneParamRD(){
   //    dont have one yet
   }
   public void wordTwoParamRD(ITokenCollection tC, int index) {
     if (tC.getToken(index) instanceof MoveWordToken) {
 //      getNeighborhood().moveChildBy(((NumberToken) tC.getToken(index+1)).toInt(), ((NumberToken) tC.getToken(index+2)).toInt());
       System.out.println("Scanner(RD[2]): moved Child by: "+ tC.getToken(index+1) + " by " + tC.getToken(index+2) );
     }
   }
   public ITokenCollection setAndProcess(String input) {
     this.originalString = input;
     this.tokensInArray();
     return this.tokenCollection;
   } 
   public String getConcatenation() {
     return this.concatenation;
   }
   public void addConcatenation() {
       this.concatenation += this.tokenCollection.getLastTokenStringConcatenation();
   }
   public ITokenCollection getTokenCollection() {
     return this.tokenCollection;
   }
   public void setTokenCollection(ITokenCollection tC) {
     this.tokenCollection = tC;
   }
   public ArrayList<Command> getCommandCollection() {
     return commandCollection;
   }
   public void setCommandCollection(ArrayList<Command> commandCollection) {
     this.commandCollection = commandCollection;
   }
 }
