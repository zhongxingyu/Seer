 package viz;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 public class XAALConnector {
 
 	private static LinkedList<String> scopeColors;
 	private int currentSnapNum;
 	
 	private LinkedList<FutureAction> actions;
 	
 	static {
 		scopeColors = new LinkedList<String>();
 		scopeColors.add("blue");
 		scopeColors.add("red");
 		scopeColors.add("green");
 	}
 	
 	XAALScripter scripter;
 	HashMap<UUID, Variable> varToVar;
 	HashMap<String, Scope> scopes;
 	ArrayList<Question> questions;
 	Scope globalScope;
 	
 	String[] psuedoCode;
 	String title;
 	PsuedoSerializer psuedo;
 	
 	public XAALConnector(String[] psuedoCode, String title)
 	{
 		scripter = new XAALScripter();
 		varToVar = new HashMap<UUID, Variable>();
 		scopes = new HashMap<String, Scope>();
 		questions = new ArrayList<Question>();
 		currentSnapNum = 0;
 		this.psuedoCode = psuedoCode;
 		this.title = title;
 		this.psuedo = new PsuedoSerializer(psuedoCode, title);
 		actions = new LinkedList<FutureAction>();
 	}
 	
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
 		//Global never starts hidden
 		retScope.setHidden(!isGlobal);
 		
 		String[] symbolNames = new String[symbols.getLocalVarNames().size()];
 		symbolNames = symbols.getLocalVarNames().toArray(symbolNames);
 
 		for(String s : symbolNames)
 		{
			Variable v = new Variable(s, symbols.get(s), true);
 			retScope.addVariable(v);
 			Interpreter.Variable iv = symbols.getVariable(s);
 			
 			varToVar.put(iv.getUUID(), v);
 
 			//if (iv instanceof ByVarVariable)	
 				//do nothing
 			//else (iv instanceof ByRefVariable)
 				//set reference
 		}
 	}
 	
 	public void addVariable(Interpreter.Variable var, String varName, String scope)
 	{
 		System.out.println(var.getValue());
 		Variable v = new Variable(varName, var.getValue(), false);
 
 		setVarValue(v, var.getValue());
 		System.out.println("Past setVarValue");
 
 		varToVar.put(var.getUUID(), v);
 		System.out.println("Getting scope: " + scope);
 		for (String key : scopes.keySet())
 		{
 			System.out.println(key);
 		}
 		scopes.get(scope).addVariable(v);
 	}
 	
 	/**
 	 * TODO: check if you're actually on a slide
 	 */
 	public void showScope(String s)
 	{
 		actions.offer(new FutureAction(true, s, currentSnapNum));
 	}
 	
 	/**
 	 * TODO: check if you're actually on a slide
 	 */
 	public void hideScope(String s)
 	{
 		actions.offer(new FutureAction(false, s, currentSnapNum));
 	}
 	
 	/**
 	 * TODO: check if you're actually on a slide
 	 * @param var
 	 */
 	public void showVar(Interpreter.Variable var)
 	{
 
 		Variable v = varToVar.get(var.getUUID());
 		actions.offer(new FutureAction(true, v, currentSnapNum));
 	}
 	
 	// TODO: check if you're actually on a slide
 	public void hideVar(Interpreter.Variable var)
 	{
 		Variable v = varToVar.get(var.getUUID());
 		actions.offer(new FutureAction(false, v, currentSnapNum));
 	}
 	
 	public boolean startSnap(int lineNum)
 	{
 		if (currentSnapNum >= 0)
 			return false;
 			
 		try {
 			currentSnapNum = scripter.startSlide();
 			scripter.addPseudocodeUrl(psuedo.toPsuedoPage(lineNum));
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
 		actions.offer(new FutureAction(fromVar, toVar, currentSnapNum));
 		
 		toVar.setValue(fromVar.getValue());
 		
 		return true;
 	}
 	
 	public boolean modifyVar(Interpreter.Variable iv, int newValue)
 	{
 		if (currentSnapNum < 0)
 			return false;
 		
 		Variable v = varToVar.get(iv.getUUID());
 		v.setValue(newValue);
 		
 		v.addCopy();
 		
 		actions.offer(new FutureAction(newValue, v, currentSnapNum));
 		return true;
 		
 	}
 	
 	/**
 	 * by default add a copy to the list
 	 * @param var
 	 * @param value
 	 */
 	public void setVarValue(Variable var, int value)
 	{
 		setVarValue(var, value, true);
 	}
 	
 	//TODO: I don't know why I'm adding the copy twice
 	private void setVarValue(Variable var, int value, boolean addCopy)
 	{
 		var.setValue(value);
 		
 		if (addCopy)
 			var.addCopy();
 		
 		var.addCopy();
 		System.out.println("About to add to action");
 		actions.offer(new FutureAction(value, var, currentSnapNum));
 		
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
 		
 		//perform and write future actions to the scripter
 		FutureAction action = null;
 		do
 		{
 			action = actions.poll();
 			if (action == null)
 				break;
 			
 			if (action.getScope() == null) // its a variable
 			{
 				if(action.isShowOrHide())// its a show or hide action
 				{
 					if (action.isShow()) // its a show action
 					{
 						writeVarShow(action);
 					}
 					else // its a hide action
 					{
 						writeVarHide(action);
 					}
 				}
 				else if(action.getNewValue() == -1) // this is a movement from one var to another
 				{
 					writeMove(action);
 				}
 				else // a variable is being set by a constant
 				{
 					writeVarModify(action);
 				}
 			}
 			else // its a scope
 			{
 				if (action.isShowOrHide()) // its a show or hide action
 				{
 					if (action.isShow())// its a show action
 					{
 						writeScopeShow(action);
 					}
 					else// its a hide action
 					{
 						writeScopeHide(action);
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
 	 * 2. getting a copy from the first variable.
 	 * 3. performing a show on that copy.
 	 * 4. getting a copy from the second variable.
 	 * 5. hiding the copy from the second variable.
 	 * 6. perform the move
 	 * 7. give ownership to second variable.
 	 * 8. setting the value of the second variable to the new value.
 	 * 9. reclose par
 	 * 9.5 reclose slide
 	 * @param action
 	 */
 	private void writeMove(FutureAction action)
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
 		
 		//show copy1
 		scripter.addShow(copy1);
 		
 		// get copy from second variable
 		String copy2 = to.popCopyId(); 
 		
 		//hide copy2
 		scripter.addHide(copy2);
 		
 		//perform the move!!!
 		
 		int startX = from.getXPos();
 		int startY = from.getYPos();
 		int endX = to.getXPos();
 		int endY = to.getYPos();
 		
 		int moveX = startX - endX;
 		int moveY = startY - endY;
 		
 		scripter.addTranslate(-moveX, -moveY, copy1);
 		
 		// give ownership of copy1 to second variable.
 		to.receiveCopyOwnership(copy1);
 		
 		// set the value of 'to' to from's value
 		to.setValue(from.getValue());
 		
 		//reclose the par
 		scripter.reclosePar();
 		//reclose the slide
 		scripter.recloseSlide();
 		}
 		catch(Exception e)
 		{
 			
 		
 		}
 	
 		
 	} // after this method completes every variable's value must equal the head of 
 	//its copiesOwned queue
 	
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
 	private void writeVarModify(FutureAction action) 
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
 			
 			//give ownership of newCopy back to variable
 			v.receiveCopyOwnership(newCopy);
 			
 			//set the value of variable to its new value
 			v.setValue(action.getNewValue());
 			
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
 	private void writeVarShow(FutureAction action)
 	{
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
 				try
 				{
 					scripter.addShow(id);
 				}
 				catch (Exception e)
 				{
 					System.out.println(e);
 				}
 			}
 			
 			
 			// pop copy of current value
 			String copy = v.popCopyId();
 			
 			//show copy
 			scripter.addShow(copy);
 			
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
 	private void writeVarHide(FutureAction action)
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
 	 * 		4. show all of the params ids
 	 * 		5. pop a copy of the params value
 	 * 		6. show the copy
 	 * 		7. give ownership of the copy back to the param HACK
 	 * 8. reclose the par
 	 * 8.5 reclose the slide
 	 * @param action
 	 */
 	private void writeScopeShow(FutureAction action)
 	{
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 			
 			// reopen par
 			scripter.reopenPar();
 			
 			Scope scope = scopes.get(action.getScope());
 			
 			//show all the ids
 			for (String id : scope.getIds())
 			{
 				scripter.addShow(id);
 			}
 			
 			ArrayList<Variable> params = scope.getParams();
 			
 			//loop through the params
 			for (Variable param : params)
 			{
 				// show all param's ids
 				for (String id : param.getIds())
 				{
 					scripter.addShow(id);
 				}
 				
 				//pop a copy of param's value
 				String copy = param.popCopyId();
 				
 				//show the copy
 				scripter.addShow(copy);
 				
 				//give ownership of copy back to param
 				param.receiveCopyOwnership(copy);
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
 	 * 		4. hide all of the params ids
 	 * 		5. pop a copy of the params value
 	 * 		6. hide the copy
 	 * 		7. give ownership of the copy back to the param HACK
 	 * 8. reclose the par
 	 * 8.5 reclose the slide
 	 * @param action
 	 */
 	private void writeScopeHide(FutureAction action)
 	{
 		try {
 			// reopen a slide
 			scripter.reopenSlide(action.getSnapNum());
 			
 			// reopen par
 			scripter.reopenPar();
 			
 			Scope scope = scopes.get(action.getScope());
 			
 			//show all the ids
 			for (String id : scope.getIds())
 			{
 				scripter.addHide(id);
 			}
 			
 			ArrayList<Variable> params = scope.getParams();
 			
 			//loop through the params
 			for (Variable param : params)
 			{
 				// show all param's ids
 				for (String id : param.getIds())
 				{
 					scripter.addHide(id);
 				}
 				
 				//pop a copy of param's value
 				String copy = param.popCopyId();
 				
 				//show the copy
 				scripter.addHide(copy);
 				
 				//give ownership of copy back to param
 				param.receiveCopyOwnership(copy);
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
 }
