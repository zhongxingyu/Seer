 package spaceinvaders;
 
 import java.util.HashMap;
 
 /**
  *
  * @author Edmund
  */
 public abstract class ScriptableClass implements Scriptable {
     
     //Which script are we running, and what line are we on?
     private int scriptID;
     private int currentLineNumber;
     
     //Whether the Scriptable is in the middle of the indicated command
     private boolean inProgress;
     
     //Basically, a catch all variable usable by the current method 
     private Parameter progressTemp;
     
     //Holds the variables declared by the Scriptable in the scripting engine
     private HashMap<String, Parameter> memoryBox;
     
     public ScriptableClass(int newScriptID) 
     {
         this.scriptID = newScriptID;
         currentLineNumber = 0;
         inProgress = false;
        
        memoryBox = new HashMap<String, Parameter>();
     }
     
     
     public ScriptableClass() 
     {
         this.scriptID = -1;
         currentLineNumber = 0;
         inProgress = false;
        
        memoryBox = new HashMap<String, Parameter>();
     }
     
     public void beginWait(double millisecondsToWait)
     {
         progressTemp = new Parameter(millisecondsToWait);
         inProgress = true;
     }
     
     public boolean continueWait(double delta)
     {
         //Update the temporary value with the delta time
         progressTemp.setDoubleValue(
                 progressTemp.getDoubleValue() - delta);
         
         if (progressTemp.getDoubleValue() < 0)
         {
             //Oh, so we're done waiting. Great.
             System.out.println("Finished waiting!");
             inProgress = false;
             return false;
         }
         
         //Alright, we still have milliseconds left to wait. Keep going
         return true;
     }
     
     //Accessors and mutators for the line number on the script
     public void setLineNumber(int newLineNumber)
     {
         currentLineNumber = newLineNumber;
     }
    
     public int getLineNumber()
     {
         return currentLineNumber;
     }
     
     //Accessors and mutators for ScriptID
     protected void setScriptID(int newScriptID)
     {
         scriptID = newScriptID;
     }
     
     public int getScriptID()
     {
         return scriptID;
     }
     
     //Running state, or basically, is the script in the middle of executing
     //some command
     public boolean isRunning()
     {
         return inProgress;
     }
     
     protected void setRunningState(boolean progress)
     {
         inProgress = progress;
     }
     
     //Accessors and mutators for the temporary variable
     protected void setTemporaryParameter(Parameter newParameter) 
     {
         progressTemp = newParameter;
     }
     
     protected Parameter getTemporaryParameter() 
     {
         return progressTemp;
     }
     
     //Memory/variable magic
     public void setVariable(String identifier, Parameter value) 
     {
         memoryBox.put(identifier, value);
     }
     
     public void newVariable(String identifier) 
     {
         //Scripter, it's YOUR FAULT if it crashes because you fail to
         //initialize the variable! LOL
         memoryBox.put(identifier, null);
     }
     
     public Parameter getVariable(String identifier) 
     {
         return memoryBox.get(identifier);
     }
     
     public void deleteVariable(String identifier)
     {
         memoryBox.remove(identifier);
     }
 }
