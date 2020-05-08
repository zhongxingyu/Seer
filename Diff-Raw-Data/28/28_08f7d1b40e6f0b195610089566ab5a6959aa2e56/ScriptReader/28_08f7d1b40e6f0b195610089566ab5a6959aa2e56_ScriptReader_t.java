 package spaceinvaders;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Edmund
  */
 public class ScriptReader
 {
     private ScriptManager scr;
     
     //These will get changed every time act(Scriptable s, double deltaTime)
     //is called. It makes it convenient since now the ScriptReader methods
     //do not have to be passed this every time.
     private Scriptable currentScriptable;
     private double currentDeltaTime;
     
     public ScriptReader(ScriptManager scriptManagerHandle) 
     {
         //Initialize ScriptManager! rather, change the constructor such that
         //it can receive the object reference to ScriptManager, which probably
         //has been initialized sometime during the initialization stage of the
         //program
         scr = scriptManagerHandle;
         
     }
     
     public void act(Scriptable s, double deltaTime)
     {
         this.currentDeltaTime = deltaTime;
         this.currentScriptable = s;
         
         //We need to know what line of what script the Scriptable is on
         int currentLineNumber = currentScriptable.getLineNumber();
         int currentScriptID = currentScriptable.getScriptID();
         
         //Now we call ScriptManager to have it return a reference to the
         //Script class which currentScriptID refers to
         Script currentScript = scr.getScriptAtID(currentScriptID);
         
         //Now, we get the current line
         Line currentLine = currentScript.getLine(currentLineNumber);
         
         //Now, our response depends on whether there is a command already in 
         //execution
         if (currentScriptable.isRunning()) 
         {
             //Will we continue running the same command?
             boolean doesContinue = continuesRunning(currentLine);
             
             //This only gets called when we JUST finished the current command
             if (!doesContinue) 
             {
                 //So since we're not staying on the same command, we now move on
                 //to the next line!
                 currentScriptable.setLineNumber(
                         currentScriptable.getLineNumber() + 1);
             }
         }
         
         //Now, if by now there is no command already in execution, then...
         if (!currentScriptable.isRunning()) 
         {
             
             //We will go from line to line
             boolean doWeContinue = true;
             while (doWeContinue) 
             {
                 //First, get the current Line of the current Script
                 //This is kind of a mouthful, so first it gets the ScriptID
                 //from the class implementing Scriptable, and then grabs from
                 //the ScriptManager, using that scriptID, the Script object
                 //itself, and then finally, from that Script object the program
                 //retrieves the line which is located at the line number
                 //specified in the class implementing Scriptable.
                 Line thisLine = scr.getScriptAtID(currentScriptable.getScriptID()).
                         getLine(currentScriptable.getLineNumber());
                 
                 //Now that we have the line, we execute it.
                 doWeContinue = executeCommand(thisLine);
                 
                 //Now, if we aren't halting, then after the line is over, 
                 //move on to the next line! UNLESS it was a goto statement!
                 //Of course, if it is a goto statement, it will remain a
                 //"Yes we continue!" but without switching lines
                 if (doWeContinue && (thisLine.getCommandID() != 1)) 
                 {
                     System.out.println("Next line!");
                     currentScriptable.setLineNumber(
                         currentScriptable.getLineNumber() + 1);
                 }
                 else 
                 {
                     //Well, we DID stop then, and so we DON'T want to move on
                     //to the next line...
                 }
                 
             } //End of the while loop which basically continues to execute the
             //next command until it reaches a command which takes time to do
           
         } //This is the end of the if-statement that executes commands. It will
         //only get called if there isn't a command in progress already for
         //the given script
         
     } //This is the end of the entire act(Scriptable s, double deltaTime) method
     
     
     //continuesRunning basically asks currentScriptable whether it has completed
     //its task. If it still has not, then continuesRunning returns a TRUE, as in
     //"yes, since the task isn't complete, DO CONTINUE TO RUN". If the class
     //instead indicates that it has completed its task, then continuesRunning
     //will return false.
     private boolean continuesRunning(Line currentLine)
     {
         boolean result = true;
         
         switch (currentLine.getCommandID())
         {
             case 0: //Waiting
                 result = currentScriptable.continueWait(currentDeltaTime);
                 break;
             case 51: //Moving... something only an Entity could do
                 result = ((Entity)currentScriptable).continueMove(currentDeltaTime);
                 break;
             
         }
         
         return result;
     }
     
     
     //Now, there are two types of commands - ones which occupy time
     //or ones which are executed immediately. When executeCommand goes
     //through and discovers what the command does, it will return a boolean
     //which indicates whether to continue to the next line. TRUE means 
     //"YES, we do continue" and FALSE means "FALSE, this is the last command!" 
     public boolean executeCommand(Line currentLine)
     {
         //By default it's true, unless the command is the type that makes it
         //false
         boolean continueExecuting = true;
         
         switch (currentLine.getCommandID())
         {
             //Remember, core functions are from 0-9. 
             case 0: //wait
                 double thisLong = currentLine.getDoubleParameter(0);
                 
                 System.out.println("Let's start waiting for " + thisLong + " milliseconds!!");
                 currentScriptable.beginWait(thisLong);
                 continueExecuting = false;
                 break;
             case 1: //GOTO, huh?
                 int newLineIndex = currentLine.getIntegerParameter(0);
                 currentScriptable.setLineNumber(newLineIndex);
                 break;
             case 2:
                 
                 //We'll leave this out for now...
                 break;
                 
             //The memory functions go here
             
             //createVariable variableType identifier (Optional value)
             case 10: //CreateVariable
                 //This isn't used because we can directly pull the Parameter
                 //object from the line, but it's still best to make it clear!
                 String variableType = currentLine.getStringParameter(0);
                //System.out.println(variableType);
                
                 
                 //The name of the variable.
                 String variableIdentifier = currentLine.getStringParameter(1);
                 
                //System.out.println(variableIdentifier);
                
                 //Check if the there is that third, optional value
                 //Obviously, if that number is equal to or greater than 3, we
                 //can access that third parameter
                 int lineParameterCount = currentLine.getParameterCount();
                
                //System.out.println(lineParameterCount);
                 if (lineParameterCount >= 3)
                 {
                     //So they decided to declare and initialize.
                     Parameter initParameter = currentLine.getParameter(2);
                     currentScriptable.setVariable(variableIdentifier,
                             initParameter);
                 }
                 else 
                 {
                     //They decided to declare a variable without initialization
                     currentScriptable.newVariable(variableIdentifier);
                 }
                 break;
                 
             //setVariable identifier newValue
             case 11:
                 
                 
                 
             //The manipulation of the locations of Displayables goes here    
             case 50:
                 //Facing a direction
                 System.out.println("Alright we don't have code for facing a direction yet.");
                 break;
             case 51:
                 //Moving
                 System.out.println("Starting to walk....");
                 double pixelsToWalk = currentLine.getDoubleParameter(0);
                 ((Entity)currentScriptable).beginMove(pixelsToWalk);
                 continueExecuting = false;
                 break;
                 
             
         }
         
         //Returns whether to continue loading more commands
         return continueExecuting;
     }
     
    
    
    /**************************************************************************
    ***************************************************************************
    ********************ALL THE INDIVIDUAL METHODS GO HERE*********************
    ***************************************************************************
    **************************************************************************/
    
    
 }
