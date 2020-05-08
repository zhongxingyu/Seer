 package Interpreter;
 import viz.*;
 import java.util.*;
 
 public class ByNeedInterpretVisitor implements VizParserVisitor, VizParserTreeConstants, UpdateReasons
 {
 
 	public boolean JustCalling = false;
 	private QuestionFactory questionFactory;
 
 
 	private Question assignmentQuestion;
 	private Question callQuestion;
 	private Question startQuestion;
 	
 	private ASTProgram program;
 	
 	private XAALConnector connector;
 		private static final int QUESTION_FREQUENCY = 65;
 	public static final int LINE_NUMBER_END = -1;
 	
 	private boolean byMacroFlag = false;
 
 	public void setQuestionFactory(QuestionFactory questionFactory)
 	{
 		this.questionFactory = questionFactory;
 	}
 	
 	public void setXAALConnector(XAALConnector xc)
 	{
 		this.connector = xc;
 	}
 	
 	public void setByMacroFlag()
 	{
 		byMacroFlag = true;
 	}
 
 	//FIXME use this?
 	public void update(int lineNumber, int reason)
 	{
 if (XAALScripter.debug) {		System.out.println("Update on " + lineNumber);
 }if (XAALScripter.debug) {		System.out.println(Global.getCurrentSymbolTable().toString());
 }		//questionFactory.addAnswers(lineNumber, reason);
 	}
 	
 	public void setAssignmentQuestionAnswer(int value)
 	{
 		if (assignmentQuestion instanceof FIBQuestion)
 		{
 			((FIBQuestion)assignmentQuestion).addAnswer(value + "");
 		}
 	}
 	
 	public Object visit(SimpleNode node, Object data)
 	{
 		int id = node.getId();
 		Object retVal = null;
 
 		switch (id)
 		{
 			case JJTPROGRAM:
 				handleProgram((ASTProgram)node);
 				break;
 			case JJTDECLARATIONLIST:
 				handleDeclarationList((ASTDeclarationList)node);
 				break;
 			case JJTDECLARATION:
 				retVal = handleDeclaration((ASTDeclaration)node);
 				break;
 			case JJTVARDECL:
 				handleVarDecl((ASTVarDecl)node);
 				break;
 			case JJTARRAYDECLARATION:
 				retVal = handleArrayDeclaration((ASTArrayDeclaration)node);
 				break;
 			case JJTSTATEMENTLIST:
 				handleStatementList((ASTStatementList)node);
 				break;
 			case JJTSTATEMENT:
 				handleStatement((ASTStatement)node);
 				break;
 			case JJTCALL:
 				retVal = handleCall((ASTCall)node);
 				break;
 			case JJTVAR:
 				retVal = handleVar((ASTVar)node);
 				break;
 			case JJTASSIGNMENT:
 				handleAssignment((ASTAssignment)node);
 				break;
 			case JJTEXPRESSION:
 				retVal = handleExpression((ASTExpression)node);
 				break;
 			case JJTARGS:
 				retVal = handleArgs((ASTArgs)node);
 				break;
 			case JJTOP:
 				retVal = handleOp((ASTOp)node);
 				break;
 			case JJTNUM:
 				retVal = handleNum((ASTNum)node);
 				break;
 			case JJTFUNCTION:
 				handleFunction((ASTFunction)node);
 				break;
 			default:
 if (XAALScripter.debug) {				System.out.println("Unimplemented");
 }		}
 		return retVal;
 	}
 	
 	public void handleProgram(ASTProgram node)
 	{
 		program = node;
 		//System.out.println("visiting program");
 		Global.setCurrentSymbolTable(Global.getSymbolTable()); 
 		update(1, UPDATE_REASON_BEGIN);
 		startQuestion = questionFactory.getStartQuestion();
 		//Drawing Stuff
 		connector.addScope(Global.getSymbolTable(), "Global", null);
 		connector.startPar();						//STARTPAR
 			connector.showScope("Global");
 		connector.endPar();						//ENDPAR
 		//connector.endSnap();
 		
 		
 		node.jjtGetChild(0).jjtAccept(this, null);
 		update(LINE_NUMBER_END, UPDATE_REASON_END);
 		
 		
 		int value = 0;
 		
 		try
 		{
 			value = Global.getSymbolTable().get(startQuestion.getVariable());
 			System.out.println(startQuestion.getVariable() + " is " + value);
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 		if (startQuestion instanceof FIBQuestion)
 		{
 		
 			((FIBQuestion)startQuestion).addAnswer(value+"");
 		}
 		else if (startQuestion instanceof TFQuestion)
 		{
 			Random r = new Random();
 			int prob = r.nextInt(10);
 			int qa = value;
 			if (prob >= 3 && value != startQuestion.getValue())
 			{
 				qa = startQuestion.getValue();
 				((TFQuestion)startQuestion).setAnswer(false);
 			}
 			else
 			{
 				((TFQuestion)startQuestion).setAnswer(true);
 				
 			}
 			startQuestion.setText(startQuestion.getText() + qa + ".");
 		}
 		
 		
 		//TODO Write the last snap nicely
 	
 		connector.startSnap(node.getPseudocode().length);
 			connector.startPar();					//STARTPAR
 				// we can't hide foo in by macro cuz it doesn't exist
 				if (!byMacroFlag)
 					connector.hideScope("foo");
 			connector.endPar();					//ENDPAR
 		connector.endSnap();
 		
 		
 	}
 	
 	public void handleDeclarationList(ASTDeclarationList node)
 	{
 		//connector.startSnap(Global.getFunction("main").getLineNumber());
 		connector.startPar();	
 
 		int numDecls = node.jjtGetNumChildren();
 		for (int i = 0; i < numDecls; i++)
 		{
 			//A Declaration returned false which means we ran.
 			if(((Boolean)node.jjtGetChild(i).jjtAccept(this, null)) == false)
 			{
 				return;
 			}
 		}
 
 	}
 	
 	//returning false means we're done executing now.
 	public Boolean handleDeclaration(ASTDeclaration node)
 	{
 	
 		//System.out.println("Visiting decl");
 		SimpleNode child = (SimpleNode) node.jjtGetChild(0);
 		if (child.getId() == JJTFUNCTION)
 		{		
 			System.out.println("Found a function");
 
 			startQuestion = questionFactory.getStartQuestion();
 			connector.endPar();					//ENDPAR
 			connector.endSnap();
 			ASTFunction main = Global.getFunction("main");
 			connector.addScope(main.getSymbolTable(), "main", "Global");
 			
 			main.jjtAccept(this, null);
 			
 			
 			return false;
 		}
 		else
 		{
 			node.jjtGetChild(0).jjtAccept(this, null);
 			//update();
 			return true;
 		}	
 		
 	}
 	
 	public void handleVarDecl(ASTVarDecl node)
 	{
 		//System.out.println("Visiting var decl");
 		String name = node.getName();
 		node.setLineNumber(((SimpleNode)node.jjtGetParent()).getLineNumber());
 		SymbolTable s = Global.getCurrentSymbolTable();
 		ArrayList<Integer> values;
 
 		if (node.getIsArray())
 		{
 			ByValVariable v = (ByValVariable) s.getVariable(name);
 			v.setArray();
 			values = (ArrayList<Integer>)handleArrayDeclaration((ASTArrayDeclaration)node.jjtGetChild(0));
 			v.setValues(values);
 		}
 		else
 		{
 			Integer value =(Integer) node.jjtGetChild(0).jjtAccept(this, null);
 			s.setValue(name, value);
 		}
 
 			//Drawing Stuff
 			connector.addVariable(s.getVariable(name), name, s.getName());
 			
 			//This is a snapshot
 			connector.showVar(Global.getCurrentSymbolTable().getVariable(name));
 	
 	}
 	
 	public void handleFunction(ASTFunction node)
 	{	
 		//Get the function's symbol table, set it's previous to the
 		// calling function's, and then set it to current.
 		connector.startSnap(node.getLineNumber());
 		if (node.getName().equals("main"))
 		{
 			connector.addQuestion(startQuestion);
 			connector.showScope("main");
 
 		}
 		else
 		{
 		}
 		connector.endSnap();
 		if (!node.getUsed())
 		{
 			return;
 		}
 		SymbolTable currentSymbolTable = node.getSymbolTable();
 		for (String p : node.getParameters())
 		{
 			ByNameVariable v = new ByNameVariable();
 			v.setParam();
 			currentSymbolTable.put(p, v);
 		}
 		Global.setCurrentSymbolTable(currentSymbolTable);
 
 		
 		node.jjtGetChild(0).jjtAccept(this, null);
 		leaveScope();
 	}
 	
 	
 	public ArrayList<Integer> handleArrayDeclaration(ASTArrayDeclaration node)
 	{
 		ArrayList<Integer> values = new ArrayList<Integer>();
 		for (int i = 0; i < node.jjtGetNumChildren(); i++)
 		{
 			Integer value = (Integer)node.jjtGetChild(i).jjtAccept(this, null);
 			values.add(value);
 		}
 		return values;
 	}
 	
 	public void handleStatementList(ASTStatementList node)
 	{
 		//connector.endPar();						//ENDPAR
 
 		int numStatements = node.jjtGetNumChildren();
 		for (int i = 0; i < numStatements; i++)
 		{
 		node.jjtGetChild(i).jjtAccept(this, null);
 		}
 		if (!node.getIsFunction())
 		{
 			Global.setCurrentSymbolTable(Global.getCurrentSymbolTable().getPrevious());
 		}
 	}
 	
 	public void handleStatement(ASTStatement node)
 	{
 
 		//System.out.println(node.getCode());
 		
 		//Drawing
 		connector.startSnap(node.getLineNumber());
 
 		
 		//FIXME we'll see how this works	
 		
 		//Nested scope for by macro
 		SimpleNode s = (SimpleNode) node.jjtGetChild(0);
 		
 		if (s instanceof ASTStatementList)
 		{
 			System.out.println("This'll never happen");
 			SymbolTable st = new SymbolTable(Global.getCurrentSymbolTable());
 			st.setName("nested");
 			Global.setCurrentSymbolTable(st);
 			s.jjtAccept(this, null);
 			Global.setCurrentSymbolTable(st.getPrevious());
 		}
 		else
 		{
 		
 			node.jjtGetChild(0).jjtAccept(this, null);
 			if (((SimpleNode)node.jjtGetChild(0)).getId() == JJTCALL)
 			{
 				((ASTCall)(node.jjtGetChild(0))).setLineNumber(node.getLineNumber());
 			}
 			
 
 			update(node.getLineNumber(), UPDATE_REASON_STATEMENT);
 		}
 		//System.out.println("endStatement");
 		connector.endSnap();
 	}
 	
 	public Integer handleCall(ASTCall node)
 	{
 		boolean gotAQuestion = true; //FIXME HACK
 		//Get the correct function head node
 		ASTFunction fun = Global.getFunction(node.getName());
 		System.out.println("Calling: " + fun.getName());
 		//Get the parameters and put the correct values in the symbolTable
 		SymbolTable st = fun.getSymbolTable();
 		String name = fun.getName();
 		ArrayList<String> parameters = fun.getParameters();
 		JustCalling = true;		
 		ArrayList<Integer> args = (ArrayList<Integer>) node.jjtGetChild(0).jjtAccept(this, null);
 		JustCalling = false;
 		ArrayList<ASTVar> argNames = ((ASTArgs)node.jjtGetChild(0)).getArgs();
 		for (int i = 0; i < args.size(); i++)
 		{
 			ByNameVariable v = (ByNameVariable) st.getVariable(parameters.get(i));
 			v.setRef(argNames.get(i));
 
 			ByNameVariable argVar = (ByNameVariable) st.getVariable(argNames.get(i).getName()+"_");
 
 		}
 		HashMap<String, String> pa = new HashMap<String, String>(); //Maps args to params
 		for (int i = 0; i < parameters.size(); i++)
 		{
 			pa.put(parameters.get(i), argNames.get(i).getName());
 		}
 		Global.setCurrentParamToArg(pa);
 
 	//QUESTION!!!
 		callQuestion = questionFactory.getCallQuestion(name, pa);
 		if (callQuestion == null)
 		{
 			System.out.println("No question");
 			gotAQuestion = false;
 		}
 		//Drawing Stuff
 		connector.addScope(new SymbolTable(null), fun.getName(), "Global", true);
 			connector.startPar();					//STARTPAR
 				connector.showScope(node.getName());
 		if (gotAQuestion) 
 		{
 			System.out.println("Adding the call question");
 			connector.addQuestion(callQuestion);
 		}
 			connector.endPar();					//ENDPAR
 		
 			connector.endSnap();
 		fun.jjtAccept(this, null);//and we gogogo
 			
 		if(gotAQuestion)
 		{
 
 			int answer = 0;
 			try
 			{
 				answer = Global.getFunction("main").getSymbolTable().get(callQuestion.getVariable());
 			}
 			catch (Exception e)
 			{
 				System.out.println(e);
 			}
 			System.out.println(callQuestion.getVariable() + " is " + answer);
 
 			if (callQuestion instanceof FIBQuestion)
 			{
 				((FIBQuestion)callQuestion).addAnswer(answer+"");
 			}
 			else if (callQuestion instanceof TFQuestion)
 			{
 				int qa = answer;
 				//Getting the value of the var at the end of the function
 				String paramName = Global.getCurrentParamToArg().get(callQuestion.getVariable());
 				int prevVal = 0;
 				try
 				{
 					Global.getFunction("foo").getSymbolTable().get(paramName);
 				}
 				catch (Exception e)
 				{
 					System.out.println(e);
 				}
 			
 				Random r = new Random();
 				int choose = r.nextInt(3);
 				switch (choose)
 				{
 					case 0:
 						qa = callQuestion.getValue();
 						System.out.println(qa + "getValue");
 						((TFQuestion)callQuestion).setAnswer(false);
 						if (qa == answer) // Value is the same anyway
 						{
 							((TFQuestion)callQuestion).setAnswer(true);
 						}
 						break;
 					case 1:
 					case 2:
 						System.out.println(qa + "value");
 						((TFQuestion)callQuestion).setAnswer(true);
 						break;
 				}
 					
 		
 				callQuestion.setText(callQuestion.getText() + qa);
 			}	
 			else
 			{
 			}
 		}
 		connector.startSnap(Global.getFunction("main").getLineNumber());
 		
 		System.out.println("leaving call");
 		return 0;
 	}
 	
 	public Integer handleVar(ASTVar node)
 	{
 		System.out.println(node.jjtGetParent());
 		Integer value = -256;
 		int index = -10000;
 		String name = node.getName();
 		if (node.isArg())
 		{
 			name+="_";
 			
 		}
 		
 		if (JustCalling)
 		{
 			return value;
 		}
 		Variable v = Global.getCurrentSymbolTable().getVariable(name);	
 		if (v instanceof ByNameVariable)
 		{
 			v = Global.getCurrentSymbolTable().getCacheVariable(name);
 			System.out.println(v + " should be null the first time");
 			if (v != null)
 			{
 				int ret = 0;
 				try
 				{
 					ret = v.getValue();
 				}
 				catch (VizIndexOutOfBoundsException e)
 				{
 					System.out.println(e);
 				}
 				return ret;
 			}
 		}
 		else
 		{
 			v = Global.getCurrentSymbolTable().getVariable(name);
 		}
 		
 		boolean cached = false;
 		if (v == null)
 		{
 
 			v = Global.getCurrentSymbolTable().getVariable(name);
 			System.out.println(v);
 			int val = 0;
 			try
 			{
 				val = v.getValue();
 
 				Global.getCurrentSymbolTable().addCacheVariable(name, val);
 				
 			}
 			catch (VizIndexOutOfBoundsException e)
 			{
 				System.out.println(e);
 			}
 			if (node.getIsArray())
 			{
 				name += "[" + ((SimpleNode)node.jjtGetChild(0)).getCode() + "]";
 			}
 			connector.addVariableToCache(name, val, "foo");
 		}
 		else
 		{
 			cached = true;
 		}
 		
 		if (v instanceof ByNameVariable)
 		{	
 			try
 			{
 				value = v.getValue();
 			}
 			catch (VizIndexOutOfBoundsException e)
 			{
 				System.out.println(e);
 			}
 		}
 		else
 		{
 			if (node.getIsArray())
 			{
 				index = (Integer) node.jjtGetChild(0).jjtAccept(this, null);
 				node.setIndex(index);
 				try
 				{
 					value = v.getValue(index);
 
 				}
 				catch (VizIndexOutOfBoundsException e)
 				{
 					System.out.println(e);
 					ASTExpression exp = (ASTExpression)node.jjtGetChild(0);
 					ASTNum num = new ASTNum(JJTNUM);
 					Random r= new Random();
 					num.setValue(r.nextInt(6));
 					exp.jjtAddChild(num, 0);
 					try
 					{
 						index = (Integer) exp.jjtAccept(this, null);
 						value = v.getValue(index);
 					}
 					catch (VizIndexOutOfBoundsException f)
 					{
 						System.out.println("oops...");
 					}
 					program.codeBuilt = false;
 					Global.lineNumber = 1;
 					program.buildCode();
 
 				}
 			}
 			else
 			{
 				try
 				{
 					value = v.getValue();
 				}	
 				catch (VizIndexOutOfBoundsException f)
 				{
 					System.out.println(f);
 				}	
 			}
 			System.out.println("Fucking " + value + v + cached);
 		}
 		if (v instanceof ByNameVariable)
 		{
 			if (cached)
 			{
 				connector.highlightVarByName(v);
 			}
 			else
 			{
 
 				connector.greyScope("foo");
 			
 				connector.highlightScopeByName("main");
 				if (((ByNameVariable)v).getVariable().getIsArray())
 				{
 					connector.highlightVarByName(((ByNameVariable)v).getVariable(), ((ByNameVariable)v).getIndex());
 				}
 				else
 				{
 					connector.highlightVarByName(((ByNameVariable)v).getVariable());
 				}
 			}	
 		}
 		else
 		{
 
 			if (node.getIsArray())
 			{
 				//connector.highlightVarByName(v, index);
 			}
 			else
 			{
 				//connector.highlightVarByName(v);
 			}
 
 		}
 		return value;
 	}
 	
 	public void handleAssignment(ASTAssignment node)
 	{
 		connector.startPar();						//STARTPAR
 		Random r = new Random();
 		int q = r.nextInt(100);
 		
 		boolean gotAQuestion = q < QUESTION_FREQUENCY;//q < QUESTION_FREQUENCY;//HACK FOR NOW FIXME
 		String name = node.getName();
 		if (((ASTVar)node.jjtGetChild(0)).isArg())
 		{
 			name+="_";
 		}
 		Integer value = (Integer)node.jjtGetChild(1).jjtAccept(this, null);
 		int index = 0;
 		
 		Variable v = Global.getCurrentSymbolTable().getVariable(name);
 		if (v instanceof ByNameVariable)
 		{
 			if (gotAQuestion)
 			{
 				if (v.getIsArray())
 				{
 					gotAQuestion = false;
 				}
 				else
 				{
 					assignmentQuestion = questionFactory.getByNameQuestion(node.getLineNumber(), name);
 				}
 			}
 			v.setValue(value);
			index = ((ByNameVariable)v).getIndex();
 		}
 		else if (v.getIsArray())
 		{
 			index = (Integer) node.jjtGetChild(0).jjtGetChild(0).jjtAccept(this, null);
 
 			try
 			{
 				v.setValue(value, index);
 			}
 			catch (VizIndexOutOfBoundsException e)
 			{
 			
 				System.out.println(e);
 				
 			}
 			if (gotAQuestion)
 			{
 				assignmentQuestion = questionFactory.getAssignmentQuestion(node.getLineNumber(), name, index);
 			}
 		}
 		else
 		{
 			if (gotAQuestion)
 			{
 				assignmentQuestion = questionFactory.getAssignmentQuestion(node.getLineNumber(), name);
 			}
 			try
 			{
 				v.setValue(value);
 			}
 			catch (Exception e)
 			{
 				System.out.println(e);
 			}
 		}
 		System.out.println(assignmentQuestion);
 		if (gotAQuestion)
 		{
 			int i = -257;
 			if (assignmentQuestion.getIndex() != -1)
 			{
 				if (assignmentQuestion.aboutArg)
 				{
 					try
 					{
 						i = Global.getFunction("main").getSymbolTable().get(assignmentQuestion.getVariable(), assignmentQuestion.getIndex());
 					}
 					catch (Exception e)
 					{
 						System.out.println(e);
 					}
 				}
 				else
 				{
 					try
 					{
 						i = Global.getCurrentSymbolTable().get(name, assignmentQuestion.getIndex());
 					}
 					catch (Exception e)
 					{
 						System.out.println(e);
 					}
 				}
 			}
 			else
 			{
 				if (assignmentQuestion.aboutArg || v instanceof ByNameVariable)
 				{
 					System.out.println("Getting " + name);
 					try
 					{
 						i = Global.getFunction("main").getSymbolTable().get(assignmentQuestion.getVariable());
 					}
 					catch (Exception e)
 					{
 						System.out.println(e);
 					}
 				}
 				{
 					try
 					{
 						i = Global.getCurrentSymbolTable().get(name);
 					}
 					catch (Exception e)
 					{
 						System.out.println(e);
 					}
 				}
 			}
 			if (gotAQuestion)
 			{
 				setAssignmentQuestionAnswer(i);
 				connector.addQuestion(assignmentQuestion);
 			}
 		}
 			
 		if (v instanceof ByNameVariable)
 		{
 	
 			connector.greyScope("foo");
 			System.out.println("Greying scope");
 			connector.highlightScopeByName("main");
 			
 			if (v.getIsArray())
 			{			
 				connector.highlightVarByName(((ByNameVariable)v).getVariable(), index);
 				connector.modifyVarByName(((ByNameVariable)v).getVariable(), index, value);
 			}
 			else
 			{
 				connector.highlightVarByName(((ByNameVariable)v).getVariable());
 				connector.modifyVarByName(((ByNameVariable)v).getVariable(), value);
 			}
 		}
 		else
 		{
 			if (v.getIsArray())
 			{
 				connector.modifyVarByName(v, index, value);
 			}
 			else
 			{
 				connector.modifyVarByName(v, value);
 			}
 		}
 		connector.endPar();						//ENDPAR
 		update(node.getLineNumber(), UPDATE_REASON_ASSIGNMENT);
 		
 	}
 	
 	public Integer handleExpression(ASTExpression node)
 	{
  		Integer value = (Integer) node.jjtGetChild(0).jjtAccept(this, null);
  		return value;
  	}
  	
  	public ArrayList<Integer> handleArgs(ASTArgs node)
  	{
  		ArrayList<Integer> args = new ArrayList<Integer>();
  		int numArgs = node.jjtGetNumChildren();
  		for (int i = 0; i < numArgs; i++)
  		{
  			args.add((Integer)node.jjtGetChild(i).jjtAccept(this, null));
  		}
  		node.gatherArgs();
  		return args;
  	}
 	
 	public Integer handleOp(ASTOp node)
 	{
 		if (node.getOp().equals("+"))
 		{
 			return (Integer)node.jjtGetChild(0).jjtAccept(this, null) +
 				(Integer)node.jjtGetChild(1).jjtAccept(this, null);
 		}
 		else if (node.getOp().equals("-"))
 		{
 			return (Integer)node.jjtGetChild(0).jjtAccept(this, null) -
 				(Integer)node.jjtGetChild(1).jjtAccept(this, null);
 		}
 		return 0;
 	}
 	
 	public Integer handleNum(ASTNum node)
 	{
 		return node.getValue();
 	}
 	public Object visit(ASTProgram node, Object data)
 	{
 		handleProgram(node);
 		return null;
 	}
 	
 	public Object visit(ASTDeclarationList node, Object data)
 	{
 		handleDeclarationList(node);
 		return null;
 	}
 	
 	public Object visit(ASTDeclaration node, Object data)
 	{
 		handleDeclaration(node);
 		return null;
 	}
 	
 	public Object visit(ASTVarDecl node, Object data)
 	{
 		handleVarDecl(node);
 		return null;
 	}
 	
 	//FIXME
 	public Object visit(ASTArrayDeclaration node, Object data)
 	{
 		handleArrayDeclaration(node);
 		return null;
 	}
 	
 	public Object visit(ASTFunction node, Object data)
 	{	
 		handleFunction(node);
 		return null;
 	}
 	
 	public Object visit(ASTStatementList node, Object data)
 	{
 		handleStatementList(node);
 		return null;
 	}
   	public Object visit(ASTStatement node, Object data)
   	{
   		handleStatement(node);
   		return null;
   	}
   	public Object visit(ASTCall node, Object data)
   	{
   		return handleCall(node);
   	}
   	public Object visit(ASTVar node, Object data)
   	{
   		return handleVar(node);
   	}
   	public Object visit(ASTAssignment node, Object data)
   	{
   		handleAssignment(node);
   		return null;
   	}
  	public Object visit(ASTExpression node, Object data)
  	{
 		return handleExpression(node);
  	}
   	public Object visit(ASTArgs node, Object data)
   	{
   		return handleArgs(node);
   	}
   	public Object visit(ASTOp node, Object data)
   	{
   		return handleOp(node);
   	}
   	public Object visit(ASTNum node, Object data)
   	{
   		return handleNum(node);
   	}
   	
   	public void leaveScope()
   	{
 if (XAALScripter.debug) {  		System.out.println("Leaving scope " + Global.getCurrentSymbolTable().getName());
 }
   		update(-1, UPDATE_REASON_LEAVEFUN);
   	}
 }
