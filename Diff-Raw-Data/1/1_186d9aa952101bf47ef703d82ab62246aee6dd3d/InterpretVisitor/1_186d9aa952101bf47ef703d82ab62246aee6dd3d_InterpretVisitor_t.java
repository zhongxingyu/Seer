 package Interpreter;
 import viz.*;
 import java.util.*;
 
 public class InterpretVisitor implements VizParserVisitor, VizParserTreeConstants, UpdateReasons
 {
 	private QuestionFactory questionFactory;
 	private ArrayList<Question> startQuestions = new ArrayList<Question>();
 	private XAALConnector connector;
 	public static final int LINE_NUMBER_END = -1;
 
 	public void setQuestionFactory(QuestionFactory questionFactory)
 	{
 		this.questionFactory = questionFactory;
 	}
 	
 	public void setXAALConnector(XAALConnector xc)
 	{
 		this.connector = xc;
 	}
 
 	public void update(int lineNumber, int reason)
 	{
 		System.out.println("Update on " + lineNumber);
 		//System.out.println(Global.getCurrentSymbolTable().toString());
 		questionFactory.addAnswers(lineNumber, reason);
 	}
 	
 	private Question getStartQuestion()
 	{
 		//FIXME random
 		return startQuestions.get(0);
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
 				handleArrayDeclaration((ASTArrayDeclaration)node);
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
 				System.out.println("Unimplemented");
 		}
 		return retVal;
 	}
 	
 	public void handleProgram(ASTProgram node)
 	{
 		//System.out.println("visiting program");
 		Global.setCurrentSymbolTable(Global.getSymbolTable()); //set current symbol table to the global one
 		update(1, UPDATE_REASON_BEGIN);
 		
 		//Drawing Stuff
 		connector.addScope(Global.getSymbolTable(), "Global", null);
 		connector.startSnap(1);
 		connector.startPar();
 			connector.showScope("Global");
 		connector.endPar();
 		connector.endSnap();
 		
 		node.jjtGetChild(0).jjtAccept(this, null);
 		update(LINE_NUMBER_END, UPDATE_REASON_END);
 		
 		//TODO Write the last snap nicely
 		System.out.println("Done");
 	}
 	
 	public void handleDeclarationList(ASTDeclarationList node)
 	{
 		//System.out.println("Visiting declList");
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
 			ASTFunction main = Global.getFunction("main");
 			connector.addScope(main.getSymbolTable(), "main", "Global");
 			connector.startSnap(Global.getFunction("main").getLineNumber());
 			connector.startPar();
 				connector.addQuestion(questionFactory.addBeginQuestion());
 			connector.endPar();
 			connector.startPar();
 				connector.showScope("main");
 			connector.endPar();
 			connector.endSnap();
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
 		if (node.getIsArray())
 		{
 			//FIXME
 			System.out.println("Array declaration unimplemented");
 		}
 		else
 		{
 			Integer value =(Integer) node.jjtGetChild(0).jjtAccept(this, null);
 			SymbolTable s = Global.getCurrentSymbolTable();
 			s.setValue(name, value);
 			
 			//Drawing Stuff
 			connector.addVariable(s.getVariable(name), name, s.getName());
 			
 			//If we're not in Global, this should be a snapshot
 
 			System.out.println("Adding a varDecl not in global");
 			connector.startSnap(node.getLineNumber());
 				connector.startPar();
 					connector.showVar(Global.getCurrentSymbolTable().getVariable(name));
 				connector.endPar();
 			connector.endSnap();			
 		}	
 	}
 	
 	public void handleFunction(ASTFunction node)
 	{	
 		//Get the function's symbol table, set it's previous to the
 		// calling function's, and then set it to current.
 		
 		
 		SymbolTable currentSymbolTable = node.getSymbolTable();
 		for (String p : node.getParameters())
 		{
 			ByValVariable v = new ByValVariable(-255);
 			v.setParam();
 			currentSymbolTable.put(p, v);
 		}
 		Global.setCurrentSymbolTable(currentSymbolTable);
 
 		//Drawing Stuff:
 		//connector.addScope(currentSymbolTable, currentSymbolTable.getName(), "Global");
 		System.out.println("Added scope " + currentSymbolTable.getName());
 		//Drawing the actually running
 		connector.startSnap(node.getLineNumber());
 			connector.startPar();
 
 			connector.endPar();
 		connector.endSnap();
 			
 		
 		System.out.println("Executing function: " + node.getName());
 		update(node.getLineNumber(), UPDATE_REASON_FUNCTION);
 		node.jjtGetChild(0).jjtAccept(this, null);
 		leaveScope();
 	}
 	public void handleArrayDeclaration(ASTArrayDeclaration node)
 	{
 	
 	}
 	
 	public void handleStatementList(ASTStatementList node)
 	{
 		int numStatements = node.jjtGetNumChildren();
 		for (int i = 0; i < numStatements; i++)
 		{
 			node.jjtGetChild(i).jjtAccept(this, null);
 		}
 	}
 	
 	public void handleStatement(ASTStatement node)
 	{
 
 		//System.out.println(node.getCode());
 		
 		//Drawing
 		connector.startSnap(node.getLineNumber());
 			connector.startPar();
 		
 		//FIXME we'll see how this works	
 		node.jjtGetChild(0).jjtAccept(this, null);
 		if (((SimpleNode)node.jjtGetChild(0)).getId() == JJTCALL)
 		{
 			((ASTCall)(node.jjtGetChild(0))).setLineNumber(node.getLineNumber());
 		}
 			
 			connector.endPar();
 		connector.endSnap();
 		update(node.getLineNumber(), UPDATE_REASON_STATEMENT);
 	}
 	
 	public Integer handleCall(ASTCall node)
 	{
 		//Get the correct function head node
 		ASTFunction fun = Global.getFunction(node.getName());
 		System.out.println("Calling: " + fun.getName());
 		//Get the parameters and put the correct values in the symbolTable
 		SymbolTable st = fun.getSymbolTable();
 
 		ArrayList<String> parameters = fun.getParameters();		
 		ArrayList<Integer> args = (ArrayList<Integer>) node.jjtGetChild(0).jjtAccept(this, null);
 		for (int i = 0; i < args.size(); i++)
 		{
 			Variable v = new ByValVariable(args.get(i));
 			v.setParam();
 			st.put(parameters.get(i), v);
 		}
 		System.out.println("Added params");
 
 		ArrayList<String> argNames = ((ASTArgs)node.jjtGetChild(0)).getArgs();
 		System.out.println("params: " + parameters.size() + " args: " + argNames.size());
 		HashMap<String, String> pa = new HashMap<String, String>(); //Maps args to params
 		for (int i = 0; i < parameters.size(); i++)
 		{
 			pa.put(parameters.get(i), argNames.get(i));
 		}
 		Global.setCurrentParamToArg(pa);
 		
 		//Drawing Stuff
 		connector.addScope(fun.getSymbolTable(), fun.getName(), "Global");
 		connector.startSnap(node.getLineNumber());
 			connector.startPar();
 				connector.showScope(node.getName());
 				connector.addQuestion(questionFactory.addCallQuestion(Global.getCurrentparamToArg(), fun.getName()));
 			connector.endPar();
 			
 			connector.startPar();
 				for (int i = 0; i < parameters.size(); i++)
 				{
 					Variable v1 = Global.getCurrentSymbolTable().getVariable(argNames.get(i));
 					Variable v2 = st.getVariable(parameters.get(i));
					v2.setParam();
 
 					connector.moveValue(v1, v2);
 				}
 			connector.endPar();
 		connector.endSnap();
 				
 		fun.jjtAccept(this, null);
 		return 0;
 	}
 	
 	public Integer handleVar(ASTVar node)
 	{
 		if (node.getIsArray())
 		{
 			System.out.println(" Array not implemented");
 			return 99;
 		}
 		return Global.getCurrentSymbolTable().get(node.getName());
 	}
 	
 	public void handleAssignment(ASTAssignment node)
 	{
 		String name = node.getName();
 		Integer value = (Integer)node.jjtGetChild(1).jjtAccept(this, null);
 		System.out.println("!!1!!" + Global.getCurrentSymbolTable().getName());
 		System.out.println(Global.getCurrentSymbolTable().getPrevious().getName());
 		Global.getCurrentSymbolTable().setValue(name, value);
 		
 		//Drawing stuff. snap and par should be opened from enclosing statement
 		
 		connector.addQuestion(questionFactory.addAssignmentQuestion(Global.getCurrentParamToArg(), name));
 		connector.endPar();
 		connector.endSnap();
 		connector.startSnap(node.getLineNumber());
 		connector.startPar();
 		
 		connector.modifyVar(Global.getCurrentSymbolTable().getVariable(name), value);
 		
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
   		System.out.println("Leaving scope " + Global.getCurrentSymbolTable().getName());
 
   		update(-1, UPDATE_REASON_LEAVEFUN);
   	}
 }
