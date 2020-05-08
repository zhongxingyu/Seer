 /*
  * Program Mode class.
  * This is started when the user hits "p" for Program mode.
  * This executes the program that is written in the text file
  * detailed by the user.
  */
 
 
 package karel;
 
 import java.util.ArrayList;
 
 
 /**
  *
  * @author Gonzo
  */
 public class ProgrammerMode {
     private final String TURNLEFT = "LEFT";
     private final String TURNRIGHT = "RIGHT";
     private final String FORWARD = "GO";
     private final String PICKUP = "GET";
     private final String PLACE = "PUT";
     private final String IF = "IF";
     private final String REPEAT = "REPEAT";
     private final String WHILE = "WHILE";
     private final String ELSE = "ELSE";
     private final String NOT = "NOT";
     private final String HOME = "HOME";
     private final String NORTH = "NORTH";
     private final String GEM = "GEM";
     private final String WALL = "WALL";
     private final String EMPTY = "EMPTY";
     private final String[] reserved= new String [10];
     private final String[] sensors = new String[5];
     private ArrayList <String> instruction = new ArrayList();
     private String UsersProgram;
     private ArrayList <Character> keyInstructions = new ArrayList();
     private Board programBoard;
     private String ErrorStatements = "There was a error on line: ";
    
     public ProgrammerMode(String uCode, Board tempBoard){  
         programBoard = new Board(tempBoard.getFileName(), 0);
         UsersProgram = uCode;
         SetReservedWord();
         setSensors();
         ConvertToArrayList();
         
     }
     
     /**
      * Set reserved words in a string array.
      */
     private void SetReservedWord() {
         
         reserved[0]="LEFT";
         reserved[1]="RIGHT";
         reserved[2]="GO";
         reserved[3]="GET";
         reserved[4]="PUT";
         reserved[5]="REPEAT";
         reserved[6]="IF";
         reserved[7]="NOT";
         reserved[8]="WHILE";
         reserved[9]="ELSE";
         
     }
     
     /**
      * Set sensor words in a string array.
      */
     private void setSensors(){
         sensors[0]="WALL";
         sensors[1]="GEM";
         sensors[2]="EMPTY";
         sensors[3]="HOME";
         sensors[4]="NORTH";
     }
     
     /**
      * Converts user's program into array list
      */
     private void ConvertToArrayList(){
         String[] temp = UsersProgram.split("\n");
         
         for(int i=0; i<temp.length; i++){
             temp[i] = temp[i].toUpperCase();
             instruction.add(temp[i]);
         }
     }
     
     
     /**
      * Checks for errors in the instruction of the file.
      * @return True if an error is found
      */
     public boolean ErrorChecking(){
         
         int line = 1;
         String temp;
         String[] tempStrArray;
         
         boolean Error = true;
         
         for(int i=0;i<instruction.size();i++){
             Error=true;
             temp=instruction.get(i);
             tempStrArray = temp.split(" ");
             
             //loop through reserved words to find a match
             for(int j=0;j<reserved.length;j++){
                if(tempStrArray[0].endsWith(reserved[j]))
                 {
                    if(tempStrArray[0].endsWith(IF)) {  
                         Error = errorCheckIfStatement(tempStrArray, line);
                     }
                     
                    else if(tempStrArray[0].endsWith(REPEAT)){   
                         Error = errorCheckRepeatStatement(tempStrArray, line);
                     }
                     
                    else if(tempStrArray[0].endsWith(WHILE)){
                         Error = errorCheckWhileStatement(tempStrArray, line);
                     }
                     
                    else if(tempStrArray[0].endsWith(ELSE)){
                         Error = errorCheckElseStatement(tempStrArray, line);
                     }
                     
                     else{
                         Error = false;
                     }
                     
                     break;     
                 }
                 
             }//end for
             
             if(Error==false)
             {
                 line++;
                 continue;
             }
             
             else{
                 ErrorStatements = ErrorStatements.concat(line + "\n");
                 ErrorStatements = ErrorStatements.concat("Invalid Syntax");
                 break;
             }
          }
         
        if(Error==true)
        {
         return true;
        }
        else{
            return false;
        }
     }
     
     
 private boolean errorCheckIfStatement(String[] Statement, int line){
         boolean Error = true;
         
         //if line has three statements the middle statement should be a not statement
         if (Statement.length == 3 ){
             if(Statement[1].equals(NOT)){
                 for(int z=0; z<sensors.length; z++){
                     if(Statement[2].equals(sensors[z])){
                         Error=false;
                         break; 
                     }
                  }//end for
              }
         }
 
         else if(Statement.length == 2){
             for(int z=0; z<sensors.length; z++){
                 if(Statement[1].equals(sensors[z])){
                     Error=false;
                     break; 
                 }
              }
         }
         
         else if(Statement.length < 2){
             ErrorStatements = ErrorStatements.concat(line + "\n");
             ErrorStatements = ErrorStatements.concat("Too few argument");
         }
         
         else if(Statement.length > 3){
             ErrorStatements = ErrorStatements.concat(line + "\n");
             ErrorStatements = ErrorStatements.concat("Too many argument");
         }
        
         return Error;                   
     }
     
     private boolean errorCheckRepeatStatement(String[] Statement, int line){
         boolean Error = true;
         if (Statement.length == 2){
             try{
                 int count = Integer.parseInt(Statement[1]);
                 Error=false;
 
             }
             catch(NumberFormatException e){
                 ErrorStatements = ErrorStatements.concat(line + "\n");
                 ErrorStatements = ErrorStatements.concat("Expected a numeric value after repeat");
                 return true;
             }
         }
         
         else if(Statement.length <2){
             ErrorStatements = ErrorStatements.concat(line + "\n");
             ErrorStatements = ErrorStatements.concat("Expected a numeric value after repeat");
         } 
         
         else if(Statement.length >2){
             ErrorStatements = ErrorStatements.concat(line + "\n");
             ErrorStatements = ErrorStatements.concat("Too many arguments for repeat");
         } 
         return Error;
     }
     
     
     private boolean errorCheckWhileStatement(String[] Statement, int line){
         boolean Error = true;
         
         if (Statement.length == 3 ){
             if(Statement[1].equals(NOT)){
                 for(int z=0; z<sensors.length; z++){
                     if(Statement[2].equals(sensors[z])){
                         Error=false;
                         break; 
                     }
                  }
              }
         }
          
         else if(Statement.length == 2){
             for(int z=0; z<sensors.length; z++){
                 if(Statement[1].equals(sensors[z])){
                     Error=false;
                     break; 
                 }
              }
           }
         
         else if(Statement.length < 2){
             ErrorStatements = ErrorStatements.concat(line + "\n");
             ErrorStatements = ErrorStatements.concat("Too few argument");
         }
         
         else if(Statement.length > 3){
             ErrorStatements = ErrorStatements.concat(line + "\n");
             ErrorStatements = ErrorStatements.concat("Too many argument");
         }
         
          return Error;
     }
     
     private boolean errorCheckElseStatement(String[] Statement, int line){
         boolean Error = true;
         if (Statement.length == 1 ){
             Error = false;
         }
         else{
             ErrorStatements = ErrorStatements.concat(line + "\n");
             ErrorStatements = ErrorStatements.concat("Too many argument");
         }
         return Error;
     }
     
      /**
      * Processes the correct instructions read in.
      */ 
     public void Execution(ArrayList<String> instr, int tCount){
         
       int i = 0;
       int tabCount = tCount;
       boolean usedIF = false;
       boolean encounteredIF = false;
       while(i <instr.size() && !programBoard.ReturnCrashState()){
         String instruct=instr.get(i);
         String [] tempStrArray = instruct.split(" ");
         if(tempStrArray[0].endsWith(TURNLEFT)){
             keyInstructions.add('a');
             programBoard.keyPressed('a');
             i++;
         }
         
         else if(tempStrArray[0].endsWith(TURNRIGHT)){
              keyInstructions.add('d');
              programBoard.keyPressed('d');
              i++;
         }
         
         else if(tempStrArray[0].endsWith(FORWARD)){
              keyInstructions.add('w');
              programBoard.keyPressed('w');
              i++;
         }
         
         else if(tempStrArray[0].endsWith(PICKUP)){
              keyInstructions.add('e');
              programBoard.keyPressed('e');
              i++;
         }
         
         else if(tempStrArray[0].endsWith(PLACE)){
              keyInstructions.add('s');
              programBoard.keyPressed('s');
              i++;
         }
         
         else if(tempStrArray[0].endsWith(IF)){
             tabCount++;
             encounteredIF = true;
             ArrayList<String> ifInstructions = new ArrayList();
             int x = i+1;
             String temp = instr.get(x);
             String tab = new String();
             
             for(int r=0; r<tabCount; r++){
                  tab = tab.concat("\t");
                 
             }
             while( temp.startsWith(tab) ){
                 ifInstructions.add(temp);
                 x++;
                 if (x >= instr.size()){
                     break;
                 }
                 temp = instr.get(x); 
                     
             }
             
             if(tempStrArray[1].equals(NOT)){
                 
                 if(tempStrArray[2].equals(WALL)){
                     if(!programBoard.wallSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
 
                 }
 
                 else if(tempStrArray[2].equals(GEM)){
                     if(!programBoard.gemSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
                 }
 
                 else if(tempStrArray[2].equals(EMPTY)){
                     if(!programBoard.emptySensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
                 }
                 
                 else if(tempStrArray[2].equals(HOME)){
                     if(!programBoard.homeSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
                 }
                 
                 else if(tempStrArray[2].equals(NORTH)){
                     if(!programBoard.northSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
                 }
             }
             else{
                 if(tempStrArray[1].equals(WALL)){
                 
                     if(programBoard.wallSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
 
                 }
 
                 else if(tempStrArray[1].equals(GEM)){
                     if(programBoard.gemSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
                 }
 
                 else if(tempStrArray[1].equals(EMPTY)){
                     if(programBoard.emptySensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     
                     tabCount--;
                 }
                 
                 else if(tempStrArray[1].equals(HOME)){
                     if(programBoard.homeSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
                 }
                 
                 else if(tempStrArray[1].equals(NORTH)){
                     if(programBoard.northSensor()){
                         Execution(ifInstructions, tabCount);
                         usedIF = true;
                     }
                     tabCount--;
                 }
             }
             i=x;
         }
         
         
         else if( (tempStrArray[0].endsWith(ELSE)) ){
             tabCount++;
             ArrayList<String> elseInstructions = new ArrayList();
             int x = i+1;
             String temp = instr.get(x);
             String tab = new String();
             for(int r=0; r<tabCount; r++){
                 tab = tab.concat("\t");
                 
             }
             while( temp.startsWith(tab) ){
                 elseInstructions.add(temp);
                 x++;
                 if (x >= instr.size()){
                     break;
                 }
                 temp = instr.get(x);       
             }
             
             //only run else if "if" was not run
             if((usedIF == false) && (encounteredIF == true)){
                 Execution(elseInstructions, tabCount);
                 usedIF = false;
                 encounteredIF = false;
             }
             
             
             
             i = x;
             tabCount--;
             
         }
         else if(tempStrArray[0].endsWith(REPEAT)){
             
             tabCount++;
             ArrayList<String> rInstructions = new ArrayList();
             int x = i+1;
             String temp = instr.get(x);
             String tab = new String();
             
             for(int r=0; r<tabCount; r++){
                 tab = tab.concat("\t"); 
             }
             while( (temp.startsWith(tab)) ){
                 
                 rInstructions.add(temp);
                 x++;
                 if (x >= instr.size()){
                     break;
                 }
                 temp = instr.get(x);
             }
             int counter = Integer.parseInt(tempStrArray[1]);
             for( int h=0; h<counter ; h++){
                 Execution(rInstructions, tabCount);
             }
             i = x;
             tabCount--; 
         }
         
         else if(tempStrArray[0].endsWith(WHILE)){ //not complete. only runs one time
             tabCount++;
             ArrayList<String> ifInstructions = new ArrayList();
             int x = i+1;
             String temp = instr.get(x);
             String tab = new String();
             for(int r=0; r<tabCount; r++){
                 tab = tab.concat("\t");
                 
             }
             while( temp.startsWith(tab) ){
                 ifInstructions.add(temp);
                 x++;
                 if (x >= instr.size()){
                     break;
                 }
                 temp = instr.get(x); 
                 }
             
             if(tempStrArray[1].equals(NOT)){
                 
                 if(tempStrArray[2].equals(WALL)){
                     while(!programBoard.wallSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
 
                 }
 
                 else if(tempStrArray[2].equals(GEM)){
                     while(!programBoard.gemSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
                 }
 
                 else if(tempStrArray[2].equals(EMPTY)){
                     while(!programBoard.emptySensor()){
                         Execution(ifInstructions, tabCount);  
                     }
                     
                     tabCount--;
                 }
                 
                 else if(tempStrArray[2].equals(HOME)){
                     while(!programBoard.homeSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
                 }
                 
                 else if(tempStrArray[2].equals(NORTH)){
                     while(!programBoard.northSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
                 }
             }
             else{
                 if(tempStrArray[1].equals(WALL)){
                 
                     while(programBoard.wallSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
 
                 }
 
                 else if(tempStrArray[1].equals(GEM)){
                     
                     while(programBoard.gemSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
                 }
 
                 else if(tempStrArray[1].equals(EMPTY)){
                     while(programBoard.emptySensor()){
                         Execution(ifInstructions, tabCount);
                     } 
                     tabCount--;
                 }
                 
                 else if(tempStrArray[1].equals(HOME)){
                     
                     while(programBoard.homeSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
                 }
                 
                 else if(tempStrArray[1].equals(NORTH)){
                     while(programBoard.northSensor()){
                         Execution(ifInstructions, tabCount);
                     }
                     tabCount--;
                 }
             }
             i=x;
         }
         
         }
 
     }
     
      public ArrayList<Character> getKeyInstructions(){
         return keyInstructions;
     }
     
     public String getErrorStatements(){
         return ErrorStatements;
     }
     
         /**
      * Start Programmer mode.
      */
     public void Start(){
         if(!ErrorChecking())
         {
             if(!instruction.isEmpty()){
                 Execution(instruction, 0);
             }
             
         }
 
      }
 }
