 package viz;
  
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 import Interpreter.Global;
  
 //TODO: highlighting doesn't always work.
 public class XAALConnector {
  
   private static LinkedList<String> scopeColors;
   private int currentSnapNum;
   
   private final String highlightColor = "red";
   
   private LinkedList<FutureAction> actions;
   
   static {
     scopeColors = new LinkedList<String>();
     scopeColors.add("blue");
     scopeColors.add("red");
     scopeColors.add("green");
   }
   
   private XAALScripter scripter;
   private HashMap<UUID, Variable> varToVar;
   private HashMap<String, Scope> scopes;
   private ArrayList<Question> questions;
   private Scope globalScope;
   
   private String[] psuedoCode;
   private String title;
   private PseudoSerializer pseudo;
   private CodePageContainer cpc;
   
   public XAALConnector(String[] psuedoCode, String title)
   {
     scripter = new XAALScripter();
     varToVar = new HashMap<UUID, Variable>();
     scopes = new HashMap<String, Scope>();
     questions = new ArrayList<Question>();
     currentSnapNum = 0;
     this.psuedoCode = psuedoCode;
     this.title = title;
     this.pseudo = new PseudoSerializer(psuedoCode, title);
     actions = new LinkedList<FutureAction>();
     this.cpc = new CodePageContainer();
   }
   
   public String addCodePage(String[] code)
   {
 	  return cpc.createCodePage(code);
   }
   
   public void moveArgs(String codePageId, int fromLineNum, int fromPos, String fromStr, 
 		  int toLineNum, int toPos)
   {
 	  CodePage cp = cpc.get(codePageId);
 	  
 	  cp.setCallLineNum(fromLineNum);
 	  
 	  cp.addCopy(fromPos, fromStr);
 	  
 	  actions.offer(new MoveArgCodePageAction(cp, currentSnapNum, 
 			  fromLineNum, fromPos, toLineNum, toPos));
 	  
   }
   
   /*
   public boolean addTextLine(String codePageId, String value, int lineNum)
   {
 	  cpc.get(codePageId).addLine(lineNum, value);
 	  
 	  return true;
   }
   
   public boolean addLinePart(String codePageId, String value, int lineNum)
   {
 	  cpc.get(codePageId).addLinePart(lineNum, value);
 	  return true;
   }*/
   
   /**
    * Allows you to add an important Part to a line
    * @param codePageId
    * @param value
    * @param lineNum
    * @return the id of that important Part
    */
   /*
   public String addImportantPart(String codePageId, String value, int lineNum)
   {
 	  return cpc.get(codePageId).addImpPart(lineNum, value);
   }*/
   
   public boolean showCodePage(String codePageId)
   {
 	  actions.offer(new ShowHideCodePageAction(true, cpc.get(codePageId), currentSnapNum));
 	  return true;
   }
   
   public boolean hideCodePage(String codePageId)
   {
 	  actions.offer(new ShowHideCodePageAction(false, cpc.get(codePageId), currentSnapNum));
 	  return true;
   }
   /*
   public boolean moveLinePart(String codePageId, String fromId, String...toIds)
   {
 	  CodePage cp = cpc.get(codePageId);
 	  //make copies for the movements
 	  for( int i = 0; i < toIds.length; i++)
 	  {
 		 cp.addCopy(fromId);
 	  }
 	  
 	  actions.offer(new MovePartCodePageAction(cp, currentSnapNum, fromId, toIds));
 	  return true;
   }
   */
   /**
    * Add a scope to the visualization. Also adds its parameters.
    * Assumes that the local symbol table has only the parameters, nothing else.
    * @param symbols
    * @param name
    * @param parent
    */
   public void addScope(Interpreter.SymbolTable symbols, String name, String parent)
   {
     boolean isGlobal = parent == null;
     
     Scope retScope = new Scope(name, scopeColors.pop(), isGlobal);
     
     scopes.put(name, retScope);
     
     if (isGlobal)
     {
       globalScope = retScope;
     }
     else
     {
       scopes.get(parent).addScope(retScope);
     }
     
     retScope.setHidden(true);
     
     if (!name.equals("Global")) // global's not in the function table so don't try to find it
     {
 	    Interpreter.ASTFunction func = Global.getFunction(name);
 	    
 	    ArrayList<String> params = func.getParameters();
 	    
 	    for (String p : params)
 	    {
 	    	Interpreter.Variable iv = symbols.getVariable(p);
 	    	Variable v = null;
 	    	if (iv instanceof Interpreter.ByCopyRestoreVariable)
 	    	{
 	    		v = new Variable(p, null, iv.getValue(), true);
 	    		v.setIsCopyRestore();
 	    	}
 	    	else if (iv instanceof Interpreter.ByRefVariable)
 	      	{
 	      		v = new Variable(p, -255, true);
 	      		v.setIsReference(true);
 	      	}
 	      	else
 	      	{	
 	        	v = new Variable(p, symbols.get(p), true);
 	        }
 	        retScope.addVariable(v);
 	       
 	        //add a copy of the original
 	        v.addCopy();
 	        
 	        varToVar.put(iv.getUUID(), v);
 	    }
     }
   }
   
   public void addVariable(Interpreter.Variable var, String varName, String scope)
   {
     Variable v;
     if (var.getIsArray())
     {
     	v = new Array(varName, var.getValues(), false);
     	Array vArray = (Array)v;
     	
     	for (int i = 0; i < var.getValues().size(); i++)
     	{
     		vArray.addCopy(i);
     	}
     }
     else
     {
     	v = new Variable(varName, var.getValue(), false);
     	
     	v.addCopy();
     }
     
     //addCopy of the original value
     
     //setVarValue(v, var.getValue());
  
     varToVar.put(var.getUUID(), v);
  
     for (String key : scopes.keySet())
     {
       System.out.println(key);
     }
     scopes.get(scope).addVariable(v);
   }
   
   public void addVariableReference(Interpreter.Variable src, Interpreter.Variable  dest)
   {
   	Variable v1 = varToVar.get(src.getUUID());
   	Variable v2 = varToVar.get(dest.getUUID());
   	
   	if (v1 == null || v2 == null)
   	{
   		System.out.println("Bad variable");
   		return;
   	}
   	
   	v1.setReference(v2);
   
   	
   }
   
   
   public void addVariableReference(Interpreter.Variable src, Interpreter.Variable dest, 
 		  int index)
   {
 	  Variable v1 = varToVar.get(src.getUUID());
 	  	Variable v2 = varToVar.get(dest.getUUID());
 	  	
 	  	if (v1 == null || v2 == null)
 	  	{
 	  		System.out.println("Bad variable");
 	  		return;
 	  	}
 	  	
 	  	v1.setReference(v2, index);
 	  	
 	  	
   }
   
   /**
    * TODO: check if you're actually on a slide
    */
   public void showScope(String s)
   {
     actions.offer(new ShowHideScopeAction(true, s, currentSnapNum));
   }
   
   /**
    * TODO: check if you're actually on a slide
    */
   public void hideScope(String s)
   {
     actions.offer(new ShowHideScopeAction(false, s, currentSnapNum));
   }
   
   /**
    * TODO: check if you're actually on a slide
    * @param var
    */
   public void showVar(Interpreter.Variable var)
   {
  
     Variable v = varToVar.get(var.getUUID());
     actions.offer(new ShowHideVarAction(true, v, currentSnapNum));
   }
   
   // TODO: check if you're actually on a slide
   public void hideVar(Interpreter.Variable var)
   {
     Variable v = varToVar.get(var.getUUID());
     actions.offer(new ShowHideVarAction(false, v, currentSnapNum));
   }
   
   
   public boolean startSnap(int lineNum)
   {
 	return startSnap(lineNum, null);  
   }
   
   public boolean startSnap(int lineNum, String[] pseudocode)
   {
     if (currentSnapNum > 0)
       return false;
       
     try {
       currentSnapNum = scripter.startSlide();
       
       if (pseudocode != null)
     	pseudo.setPseudocode(pseudocode);
     	  
       scripter.addPseudocodeUrl(pseudo.toPseudoPage(lineNum));
     } catch (SlideException e) {
       return false;
     }
     
     return true;
   }
   
   public boolean endSnap()
   {
     if (currentSnapNum < 0)
       return false;
     
     try {
       scripter.endSlide();
     } catch (SlideException e) {
       return false;
     }
     currentSnapNum = -1;
     return true;
   }
   
   public boolean startPar()
   {
     if (currentSnapNum < 0)
       return false;
     
     try {
       scripter.startPar();
     } catch (Exception e)
     {
       return false;
     }
     
     return true;
   }
   
   public boolean endPar()
   {
     if (currentSnapNum < 0)
       return false;
     
     try
     {
       scripter.endPar();
     }
     catch (Exception e)
     {
       return false;
     }
       
     return true;  
   }
   
   
   public boolean addQuestion(Question q)
   {
     if (currentSnapNum < 0)
       return false;
     
     q.setSlideId(currentSnapNum);
     
     questions.add(q);
     
     return true;
   }
   
   
   public boolean moveValue (Interpreter.Variable from, Interpreter.Variable to)
   {
     if (currentSnapNum < 0)
       return false;
     
     Variable fromVar = varToVar.get(from.getUUID());
     Variable toVar = varToVar.get(to.getUUID());
     
     //add a copy of the currentValue to fromVar
     fromVar.addCopy();
     actions.offer(new MoveVarAction(fromVar, toVar, currentSnapNum));
     
     toVar.setValue(fromVar.getValue());
     
     return true;
   }
   
 
  
 	public boolean moveValue(Interpreter.Variable from, int fromIndex, Interpreter.Variable to)
 	{
 		 if (currentSnapNum < 0)
 		      return false;
 		    
 		 Variable fromVar = varToVar.get(from.getUUID());
 		 Variable toVar = varToVar.get(to.getUUID());
 		 
 		 Array fromArray = (Array)fromVar;
 		//add a copy of the currentValue to fromVar[fromIndex
 		 fromArray.addCopy(fromIndex);
 		 actions.offer(new MoveVarIndexAction(fromVar, fromIndex, toVar, currentSnapNum));
 		 
 		 toVar.setValue(fromArray.getValue(fromIndex));
 		 
 		 return true;
 	}
 	
 	public boolean moveValue(Interpreter.Variable from, Interpreter.Variable to, 
 			int toIndex)
 	{
 		 if (currentSnapNum < 0)
 		      return false;
 
 		Variable fromVar = varToVar.get(from.getUUID());
 		Variable toVar = varToVar.get(to.getUUID());
 		
 		
 		fromVar.addCopy();
 		
 		actions.offer(new MoveVarToIndexAction(fromVar, toVar, toIndex, currentSnapNum));
 		
 		Array toArray = (Array)toVar;
 		toArray.setElem(toIndex, fromVar.getValue());
 		
 		return true;
 	}
   
   public boolean modifyVar(Interpreter.Variable iv, int newValue)
   {
     if (currentSnapNum < 0)
       return false;
     
     Variable v = varToVar.get(iv.getUUID());
     v.setValue(newValue);
     
     v.addCopy();
     
     actions.offer(new ModifyVarAction(newValue, v, currentSnapNum));
     return true;
     
   }
   
 	public boolean modifyVar(Interpreter.Variable iv, int index, int newValue)
 	{
 		 if (currentSnapNum < 0)
 		      return false;
 		    
 		 Variable v = varToVar.get(iv.getUUID());
 		 Array vArray = (Array)v;
 		 
 		 vArray.setElem(index, newValue);
 		 
 		 vArray.addCopy(index);
 		 
 		 actions.offer(new ModifyVarIndexAction(newValue, v, index, currentSnapNum));
 		 
 		 return true;
 	}
   
   
  
   
   /**
    * where the magic happens
    * @param filename
    */
   public void draw(String filename)
   {
   
     //first calls draw on the global scope which then draws all of the children
     globalScope.draw(scripter);
     System.out.println("Drew global scope");
     //System.out.println(scripter.toString());
     //perform and write future actions to the scripter
     FutureAction action = null;
     do
     {
       action = actions.poll();
       if (action == null)
         break;
       
       if (action instanceof VarAction) // its a variable
       {
         if(action instanceof ShowHideVarAction)// its a show or hide action
         {
           if (((ShowHideVarAction)action).isShow()) // its a show action
           {
             writeVarShow((ShowHideVarAction)action);
           }
           else // its a hide action
           {
             writeVarHide((ShowHideVarAction)action);
           }
         }
         else if(action instanceof MoveVarAction) // this is a movement from one var to another
         {
         	if (action instanceof MoveVarIndexAction)
         	{
         		writeIndexMove((MoveVarIndexAction)action);
         	}
         	else if(action instanceof MoveVarToIndexAction)
         	{
         		writeIndexToMove((MoveVarToIndexAction)action);
         	}
         	else
         	{
         		writeMove((MoveVarAction)action);
         	}
         }
         else // a variable is being set by a constant
         {
         	if (action instanceof ModifyVarIndexAction)
         	{
         		writeIndexModify((ModifyVarIndexAction)action);
         	}
         	else
         	{
         		writeVarModify((ModifyVarAction)action);
         	}
         }
       }
       else if (action instanceof ScopeAction)// its a scope
       {
         if (action instanceof ShowHideScopeAction) // its a show or hide action
         {
           if (((ShowHideScopeAction)action).isShow())// its a show action
           {
             writeScopeShow((ShowHideScopeAction)action);
           }
           else// its a hide action
           {
             writeScopeHide((ShowHideScopeAction)action);
           }
         }
       }
       
       else // its a CodePageAction
       {
     	  if (action instanceof MoveArgCodePageAction)
     	  {
    		 // writeMovePartCodePage((MoveArgCodePageAction)action);
     	  }
     	  else if (action instanceof ShowHideCodePageAction)
     	  {
     		  if (((ShowHideCodePageAction)action).isShow()) // its a show action  
     		  {
     			  writeCodePageShow((ShowHideCodePageAction)action);
     		  }
     		  else // its a hide action
     		  {
     			  writeCodePageHide((ShowHideCodePageAction)action);
     		  }
     	  }
       }
       
     } while (true);
     
     //write out all the questions
 
     for (Question q : questions)
     {
       q.draw(scripter);
     }
     
     
     //write to the file
     FileWriter writer;
     try {
       writer = new FileWriter(filename);
     
       writer.write(scripter.toString());
     
       writer.close();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     
   }
   
  
   /**
    * a move consists of:
    * 1. reopening the slide
    * 1.5 reopen par
    * 2. getting a copy1 from the first variable.
    * 2.5 get a newCopy from the first Variable.
    * 3. performing a show on newCopy.
    * 3.25 change color of newCopy
    * 3.5 give ownership of newCopy back to the first variable.
    * 4. getting a copy from the second variable.
    * 5. hiding the copy from the second variable.
    * 6. perform the move
    * 7. give ownership to second variable.
    * 8. setting the value of the second variable to the new value.
    * 9. reclose par
    * 9.5 reclose slide
    * 10. reopen next slide
    * 10.5 reopen next par
    * 11. turn off highlighting of copy1
    * 12. reclose par
    * 12.5 reclose slide
    * @param action
    */
   private void writeMove(MoveVarAction action)
   {
     try {
     // reopen a slide
     scripter.reopenSlide(action.getSnapNum());
     
     // reopen par
     scripter.reopenPar();
     
     Variable from = action.getFrom();
     Variable to = action.getTo();
     
     //get copy for the first variable
     String copy1 = from.popCopyId();
     
     // get a new copy from the first variable.
     String newCopy = from.popCopyId();
     
     //show newCopy
     scripter.addShow(newCopy);
     
     //color newCopy
     scripter.addChangeStyle(highlightColor, copy1);
     from.receiveCopyOwnership(newCopy);
     
     // get copy from second variable
     String copy2 = to.popCopyId();
     
     //hide copy2
     scripter.addHide(copy2);
     scripter.reclosePar();
     //perform the move!!!
     
     scripter.startPar();
     int startX = from.getXPos(); 
     int startY = from.getYPos();
     
     if (from.getIsCopyRestore()) //you've gotta take it from right box
     {
     	startX = from.getRightXPosCR();
     }
     
    
     int endX = to.getXPos();
     int endY = to.getYPos();
     
     if (to.getIsCopyRestore()) //you've gotta take it from right box
     {
     	endX = to.getRightXPosCR();
     }
     
     int moveX = startX - endX;
     int moveY = startY - endY;
     
     scripter.addTranslate(-moveX, -moveY, copy1);
     
     // give ownership of copy1 to second variable.
     to.receiveCopyOwnership(copy1);
     
     // set the value of 'to' to from's value
     to.setValue(from.getValue());
     
     //reclose the par
     scripter.endPar();
     //reclose the slide
     scripter.recloseSlide();
     // turn off highlighting on next slide
     scripter.reopenSlide(action.getSnapNum() + 1);
     scripter.reopenPar();
     
     scripter.addChangeStyle("black", copy1);
     
     scripter.reclosePar();
     scripter.recloseSlide();
     }
     catch(Exception e)
     {
       
     
     }
   
     
   } // after this method completes every variable's value must equal the head of
   //its copiesOwned queue
   
   /**
    * a move consists of:
    * 1. reopening the slide
    * 1.5 reopen par
    * 2. getting a copy1 from the first variable at index.
    * 2.5 get a newCopy from the first Variable at index.
    * 3. performing a show on newCopy.
    * 3.25 highlight copy1
    * 3.5 give ownership of newCopy back to the first variable at index.
    * 4. getting a copy from the second variable.
    * 5. hiding the copy from the second variable.
    * 6. perform the move
    * 7. give ownership to second variable.
    * 8. setting the value of the second variable to the new value.
    * 9. reclose par
    * 9.5 reclose slide
    * 10. reopen next slide
    * 10.5 reopen next par
    * 11. unhighlight copy1
    * 12. reclose par
    * 12.5 reclose slide.
    * @param action
    */
   private void writeIndexMove(MoveVarIndexAction action)
   {
 	  try
 	  {
 		  scripter.reopenSlide(action.getSnapNum());
 	  	    
 	    // reopen par
 	    scripter.reopenPar();
 	    
 	    Array from = (Array)action.getFrom();
 	    Variable to = action.getTo();
 	    int fromIndex = action.getIndex();
 	    
 	    //get copy for the first variable
 	    String copy1 = from.popCopyId(fromIndex);
 	    
 	    // get a new copy from the first variable.
 	    String newCopy = from.popCopyId(fromIndex);
 	    
 	    //show newCopy
 	    scripter.addShow(newCopy);
 	    
 	    scripter.addChangeStyle(highlightColor, copy1);
 	    from.receiveCopyOwnership(newCopy, fromIndex);
 	    
 	    // get copy from second variable
 	    String copy2 = to.popCopyId();
 	    
 	    //hide copy2
 	    scripter.addHide(copy2);
 	    scripter.reclosePar();
 	    //perform the move!!!
 	    
 	    boolean parExists = false;
 	    parExists = scripter.reopenPar(1);
 	    
 	    if (!parExists)
 	    	scripter.startPar();
 	    
 	    int startX = from.getXPos(fromIndex);
 	    int startY = from.getYPos();
 	    
 	    int endX = to.getXPos();
 	    int endY = to.getYPos();
 	    
 	    if (to.getIsCopyRestore()) //you've gotta take it from right box
 	    {
 	    	endX = to.getRightXPosCR();
 	    }
 	    
 	    int moveX = startX - endX;
 	    int moveY = startY - endY;
 	    
 	    scripter.addTranslate(-moveX, -moveY, copy1);
 	    
 	    // give ownership of copy1 to second variable.
 	    to.receiveCopyOwnership(copy1);
 	    
 	    // set the value of 'to' to from's value
 	    to.setValue(from.getValue(fromIndex));
 	    
 	  //reclose the par
 	    if (parExists)
 	    	scripter.reclosePar();
 	    else
 	    	scripter.endPar();
 	    //reclose the slide
 	    scripter.recloseSlide();
 	    // turn off highlighting on next slide
 	    scripter.reopenSlide(action.getSnapNum() + 1);
 	    scripter.reopenPar();
 	    
 	    scripter.addChangeStyle("black", copy1);
 	    
 	    scripter.reclosePar();
 	    scripter.recloseSlide();
 	    
 	  }
 	  catch (Exception e)
 	  {
 		  System.out.println("this is bad");
 	  }
   }
   
   /**
    * this is a write back out to an index in an array
    * @param action
    */
   private void writeIndexToMove(MoveVarToIndexAction action)
   {
 	  try
 	  {
 		  scripter.reopenSlide(action.getSnapNum());
 	  	    
 	    // reopen par
 	    scripter.reopenPar();
 	    
 	    Variable from = action.getFrom();
 	    Array to = (Array)action.getTo();
 	    int toIndex = action.getIndex();
 	    
 	  //get copy for the first variable
 	    String copy1 = from.popCopyId();
 	    
 	 // get a new copy from the first variable.
 	    String newCopy = from.peekCopyId();
 	    
 	  //show newCopy
 	    scripter.addShow(newCopy);
 	    scripter.addChangeStyle(highlightColor, copy1);
 	    
 	 // get copy from second variable
 	    String copy2 = to.popCopyId(toIndex);
 	  
 	 //hide copy2
 	    scripter.addHide(copy2);
 	    scripter.reclosePar();
 	    
 	    boolean parExists = false;
 	    parExists = scripter.reopenPar(1);
 	    
 	    if (!parExists)
 	    	scripter.startPar();
 	    int startX = from.getXPos();
 	    int startY = from.getYPos();
 	    
 	    if (from.getIsCopyRestore()) //you've gotta take it from right box
 	    {
 	    	startX = from.getRightXPosCR();
 	    }
 	    
 	    int endX = to.getXPos(toIndex);
 	    int endY = to.getYPos();
 	    
 	    int moveX = startX - endX;
 	    int moveY = startY - endY;
 	    
 	    scripter.addTranslate(-moveX, -moveY, copy1);
 	    
 	    // give ownership of copy1 to second variable.
 	    to.receiveCopyOwnership(copy1, toIndex);
 	    
 	    // set the value of 'to' to from's value
 	    to.setElem(toIndex, from.getValue());
 	    
 	    
 	  //reclose the par
 	    if (parExists)
 	    	scripter.reclosePar();
 	    else
 	    	scripter.endPar();
 	    //reclose the slide
 	    scripter.recloseSlide();
 	    // turn off highlighting on next slide
 	    scripter.reopenSlide(action.getSnapNum() + 1);
 	    scripter.reopenPar();
 	    
 	    scripter.addChangeStyle("black", copy1);
 	    
 	    scripter.reclosePar();
 	    scripter.recloseSlide();
 	  }
 	  catch (Exception e)
 	  {
 		  System.out.println("bad");
 	  }
   }
   
   /**
    * a modify consists of:
    * 1. reopening the slide
    * 1.5 reopen par
    * 2. pop the copy of the currentValue
    * 3. hide this copy
    * 4. pop the copy of the newValue
    * 5. show the new copy
    * 6. give ownership of this copy BACK to the variable (its a hack)
    * 7. set the value of the variable to its new value
    * 8. reclose the par
    * 8.5 reclose the slide
    */
   private void writeVarModify(ModifyVarAction action)
   {
     try {
       // reopen a slide
       scripter.reopenSlide(action.getSnapNum());
       
       // reopen par
       scripter.reopenPar();
       
       Variable v = action.getTo();
       
       // pop copy of current value
       String oldCopy = v.popCopyId();
       
       //hide oldCopy
       scripter.addHide(oldCopy);
       
       // pop copy of new value
       String newCopy = v.popCopyId();
       
       //show new copy
       scripter.addShow(newCopy);
       
       //highlight the change
       scripter.addChangeStyle(highlightColor, newCopy);
       
       //give ownership of newCopy back to variable
       v.receiveCopyOwnership(newCopy);
       
       //set the value of variable to its new value
       v.setValue(action.getNewValue());
       
       //reclose the par
       scripter.reclosePar();
       //reclose the slide
       scripter.recloseSlide();
       
    // turn off highlighting on next slide
       
       scripter.reopenSlide(action.getSnapNum() + 1);
       scripter.reopenPar();
       
       scripter.addChangeStyle("black", newCopy);
       
       scripter.reclosePar();
       scripter.recloseSlide();
     }
     catch (Exception e)
     {
       //we're in trouble
     }
     
   }
   
   /**
    * a modify consists of:
    * 1. reopening the slide
    * 1.5 reopen par
    * 2. pop the copy of the currentValue at index
    * 3. hide this copy
    * 4. pop the copy of the newValue at index
    * 5. show the new copy
    * 6. give ownership of this copy BACK to the variable (its a hack) at index
    * 7. set the value of the variable at index to its new value
    * 8. reclose the par
    * 8.5 reclose the slide
    */
   private void writeIndexModify(ModifyVarIndexAction action)
   {
   	System.out.println("Writing index modify");
 	  try {
 	      // reopen a slide
 	      scripter.reopenSlide(action.getSnapNum());
 	      
 	      // reopen par
 	      scripter.reopenPar();
 	      
 	      int toIndex = action.getIndex();
 	      System.out.println("Modifying index " + toIndex);
 	      Array v = (Array)action.getTo();
 	      
 	      // pop copy of current value
 	      String oldCopy = v.popCopyId(toIndex);
 	      System.out.println("Current value = " + oldCopy);
 	      //hide oldCopy
 	      scripter.addHide(oldCopy);
 	      
 	      // pop copy of new value
 	      String newCopy = v.getCopyId(toIndex);
 
 	      System.out.println("New value " + newCopy);
 	      //show new copy
 	      scripter.addShow(newCopy);
 	      
 	      //highlight the change
 	      scripter.addChangeStyle(highlightColor, newCopy);
 	      
 	      //give ownership of newCopy back to variable
 	      //v.receiveCopyOwnership(newCopy);
 	      
 	      //set the value of variable to its new value
 	      v.setElem(toIndex, action.getNewValue());
 	      
 	      //reclose the par
 	      scripter.reclosePar();
 	      //reclose the slide
 	      scripter.recloseSlide();
 	      
 	   // turn off highlighting on next slide
 	      
 	      scripter.reopenSlide(action.getSnapNum() + 1);
 	      scripter.reopenPar();
 	      
 	      scripter.addChangeStyle("black", newCopy);
 	      
 	      scripter.reclosePar();
 	      scripter.recloseSlide();
 	    }
 	    catch (Exception e)
 	    {
 	      //we're in trouble
 	    }
   }
   
   /**
    * 1. reopening the slide
    * 1.5 reopen par
    * ... show all the ids
    * 2. pop the copy of current value from the variable
    * 3. show the value.
    * 4. give ownership of this copy BACK to the variable (its a HACK)
    * 5. reclose par
    * 5.5 reclose slide
    * @param action
    */
   private void writeVarShow(ShowHideVarAction action)
   {
   	System.out.println("Showing a var");
     try {
       // reopen a slide
       scripter.reopenSlide(action.getSnapNum());
       
       // reopen par
       scripter.reopenPar();
       
       Variable v = action.getTo();
       
       //show all the ids
       ArrayList<String> ids = v.getIds();
       for (String id : ids)
       {
       	System.out.println("Showing id: " + id);
         try
         {
           scripter.addShow(id);
         }
         catch (Exception e)
         {
           System.out.println(e);
         }
       }
       if (v instanceof Array)
       {
     	  Array vArray = (Array)v;
     	  for (int i = 0; i < vArray.getValues().size(); i++)
     	  {
     		  String copy = vArray.popCopyId(i);
     		  System.out.println("Showing: " + copy + " on slide " + action.getSnapNum());
     		  scripter.addShow(copy);
     		  
     		  vArray.receiveCopyOwnership(copy, i);
     		  
     		
     	  }
     	  
     	  scripter.reclosePar();
 	      //reclose the slide
 	      scripter.recloseSlide();
       }
       else
       {
 	      // pop copy of current value
 	      String copy = v.popCopyId();
 	      
 	      //show copy
 	      scripter.addShow(copy);
 	      
 	      scripter.addChangeStyle(highlightColor, copy);
 	      
 	      // give ownership of the copy back
 	      v.receiveCopyOwnership(copy);
 	      
 	      //reclose the par
 	      scripter.reclosePar();
 	      //reclose the slide
 	      scripter.recloseSlide();
 	      
 	      //turn off highlighting on the next slide.
 	      scripter.reopenSlide(action.getSnapNum()+1);
 	      scripter.reclosePar();
 	      
 	      scripter.addChangeStyle("black", copy);
 	      
 	      scripter.reopenPar();
 	      scripter.recloseSlide();
       }
     }
     catch (Exception e)
     {
       //we're in trouble
     }
   }
   
   /**
    * 1. reopening the slide
    * 1.5 reopen par
    * ... hide all the ids
    * 2. pop the copy of current value from the variable
    * 3. hide the value.
    * 4. give ownership of this copy BACK to the variable (its a HACK)
    * 5. reclose par
    * 5.5 reclose slide
    * @param action
    */
   private void writeVarHide(ShowHideVarAction action)
   {
     try {
       // reopen a slide
       scripter.reopenSlide(action.getSnapNum());
       
       // reopen par
       scripter.reopenPar();
       
       Variable v = action.getTo();
       
       //hide all the ids
       ArrayList<String> ids = v.getIds();
       for (String id : ids)
       {
         try
         {
           scripter.addHide(id);
         }
         catch (Exception e)
         {
           System.out.println(e);
         }
       }
       
       // pop copy of current value
       String copy = v.popCopyId();
       
       //hide copy
       scripter.addHide(copy);
       
       // give ownership of the copy back
       v.receiveCopyOwnership(copy);
       
       //reclose the par
       scripter.reclosePar();
       //reclose the slide
       scripter.recloseSlide();
     }
     catch (Exception e)
     {
       //we're in trouble
     }
   }
   
   /**
    * TODO: make sure that all the params and values are shown correctly,
    * its possible they might not be
    *
    * 1. reopen the slide
    * 1.5. reopen the par
    * 2. show all the ids
    * 3. loop through the params as follows:
    *     4. show all of the params ids
    *     5. pop a copy of the params value
    *     6. show the copy
    *     7. give ownership of the copy back to the param HACK
    * 8. reclose the par
    * 8.5 reclose the slide
    * @param action
    */
   private void writeScopeShow(ShowHideScopeAction action)
   {
     try {
       // reopen a slide
       scripter.reopenSlide(action.getSnapNum());
       
       // reopen par
       scripter.reopenPar();
       
       Scope scope = scopes.get(action.getScope());
       
       ArrayList<String> scopeIds = scope.getIds();
       //show all the ids
       for (String id : scopeIds)
       {
         scripter.addShow(id);
       }
       
       ArrayList<Variable> params = scope.getParams();
       
       //loop through the params
       for (Variable param : params)
       {
     	ArrayList<String> ids = param.getIds();
         // show all param's ids
         for (String id : ids)
         {
           scripter.addShow(id);
         }
         
         if (!param.getIsReference())
         {
 	        //pop a copy of param's value
 	        String copy = param.popCopyId();
 	        
 	        //show the copy
 	        scripter.addShow(copy);
 	        
 	        //give ownership of copy back to param
 	        param.receiveCopyOwnership(copy);
         }
       }
       
       //reclose par
       scripter.reclosePar();
       
       //reclose slide
       scripter.recloseSlide();
     }
     catch (Exception e)
     {
       
     }
   }
   
   /**
    * TODO: make sure that all the params and values are shown correctly,
    * its possible they might not be
    *
    * 1. reopen the slide
    * 1.5. reopen the par
    * 2. hide all the ids
    * 3. loop through the params as follows:
    *     4. hide all of the params ids
    *     5. pop a copy of the params value
    *     6. hide the copy
    *     7. give ownership of the copy back to the param HACK
    * 8. reclose the par
    * 8.5 reclose the slide
    * @param action
    */
   private void writeScopeHide(ShowHideScopeAction action)
   {
     try {
       // reopen a slide
       scripter.reopenSlide(action.getSnapNum());
       
       // reopen par
       scripter.reopenPar();
       
       Scope scope = scopes.get(action.getScope());
       ArrayList<String> scopeIds = scope.getIds();
       //show all the ids
       for (String id : scopeIds)
       {
         scripter.addHide(id);
       }
       
       ArrayList<Variable> params = scope.getParams();
       
       //loop through the params
       for (Variable param : params)
       {
     	  ArrayList<String> ids = param.getIds();
         // hide all param's ids
         for (String id : ids)
         {
           scripter.addHide(id);
         }
         if (!param.getIsReference())
         {
 	        //pop a copy of param's value
 	        String copy = param.popCopyId();
 	        
 	        //hide the copy
 	        scripter.addHide(copy);
 	        
 	        //give ownership of copy back to param
 	        param.receiveCopyOwnership(copy);
         }
       }
       
       ArrayList<Variable> locals = scope.getLocalVariables();
       
       //loop through the locals
       for (Variable local : locals)
       {
     	  for (String id : local.getIds())
     	  {
     		  scripter.addHide(id);
     	  }
     	  
     	  if (local instanceof Array)
     	  {
     		  Array a = (Array)local;
     		  int arrayLength = a.getValues().size();
     		  for( int i = 0; i < arrayLength; i++)
     		  {
     			  scripter.addHide(a.peekCopyId(i));
     		  }
     	  }
     	  else
     	  {
     		  scripter.addHide(local.peekCopyId());
     	  }
       }
       
       
       //reclose par
       scripter.reclosePar();
       
       //reclose slide
       scripter.recloseSlide();
     }
     catch (Exception e)
     {
       
     }
   }
   /**
   * 1. reopen the slide
   * 1.5. reopen the par
   */
   private void writeCodePageShow(ShowHideCodePageAction action)
   {
 	  try
 	  {
 		  scripter.reopenSlide(action.getSnapNum());
 		  scripter.reopenPar(0);
 		  CodePage p = action.getCP();
 		  
 		  for(String id : p.getIds())
 		  {
 			  scripter.addShow(id);
 		  }
 		  
 		  scripter.reclosePar();
 		  scripter.recloseSlide();
 	  }
 	  catch(Exception e)
 	  {
 	  
 	  }
   }
   
   private void writeCodePageHide(ShowHideCodePageAction action)
   {
 	  try
 	  {
 		  
 		  scripter.reopenSlide(action.getSnapNum());
 		  scripter.reopenPar(0);
 		  CodePage p = action.getCP();
 		  
 		  for(String id : p.getIds())
 		  {
 			  scripter.addHide(id);
 		  }
 		  
 		  scripter.reclosePar();
 		  scripter.recloseSlide();
 	  }
 	  catch(Exception e)
 	  {
 	  
 	  }
   }
   
   
   private void writeMoveArgCodePage(MoveArgCodePageAction action)
   {
 	  try
 	  {
 		  scripter.reopenSlide(action.getSnapNum());
 		  scripter.reopenPar();
 		  CodePage cp = cpc.get(action.getCP());
 		  
 		  
 		  //show a copy
 		  String id = cp.popCopy(action.getFromPos());
 		  scripter.addShow(id);
 		  scripter.reclosePar();
 		  
 		  //do the move!!!
 		  boolean parExists = false;
 		  parExists = scripter.reopenPar(1);
 		  
 		  if(!parExists)
 			  scripter.startPar();
 			int startX = cp.fromPosX[action.getFromPos()];
 		    int startY = cp.y + (cp.getLineHeight() * action.getFromLine()); 
 		
 		    int endX = cp.toPosX[action.getToPos()];
 		    int endY = cp.y + (cp.getLineHeight() * action.getToLine());
 		    		    
 		    int moveX = startX - endX;
 		    int moveY = startY - endY;
 		    
 		    scripter.addTranslate(-moveX, -moveY, id);
 		  
 		  cp.receiveCopyOwnership(id);
 		  
 		//reclose the par
 		    if (parExists)
 		    	scripter.reclosePar();
 		    else
 		    	scripter.endPar();
 		    //reclose the slide
 		    scripter.recloseSlide();
 	  }
 	  catch (Exception e)
 	  {
 	  }
   }
 }
