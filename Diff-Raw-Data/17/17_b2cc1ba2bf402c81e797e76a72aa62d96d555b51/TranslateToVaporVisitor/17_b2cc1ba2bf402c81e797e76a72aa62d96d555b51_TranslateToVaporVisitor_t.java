 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import syntaxtree.AllocationExpression;
 import syntaxtree.AndExpression;
 import syntaxtree.ArrayAllocationExpression;
 import syntaxtree.ArrayAssignmentStatement;
 import syntaxtree.ArrayLength;
 import syntaxtree.ArrayLookup;
 import syntaxtree.ArrayType;
 import syntaxtree.AssignmentStatement;
 import syntaxtree.Block;
 import syntaxtree.BooleanType;
 import syntaxtree.BracketExpression;
 import syntaxtree.ClassDeclaration;
 import syntaxtree.ClassExtendsDeclaration;
 import syntaxtree.CompareExpression;
 import syntaxtree.Expression;
 import syntaxtree.ExpressionList;
 import syntaxtree.ExpressionRest;
 import syntaxtree.FalseLiteral;
 import syntaxtree.FormalParameter;
 import syntaxtree.FormalParameterList;
 import syntaxtree.FormalParameterRest;
 import syntaxtree.Goal;
 import syntaxtree.Identifier;
 import syntaxtree.IfStatement;
 import syntaxtree.IntegerLiteral;
 import syntaxtree.IntegerType;
 import syntaxtree.MainClass;
 import syntaxtree.MessageSend;
 import syntaxtree.MethodDeclaration;
 import syntaxtree.MinusExpression;
 import syntaxtree.Node;
 import syntaxtree.NodeList;
 import syntaxtree.NodeListOptional;
 import syntaxtree.NodeOptional;
 import syntaxtree.NodeSequence;
 import syntaxtree.NodeToken;
 import syntaxtree.NotExpression;
 import syntaxtree.PlusExpression;
 import syntaxtree.PrimaryExpression;
 import syntaxtree.PrintStatement;
 import syntaxtree.Statement;
 import syntaxtree.ThisExpression;
 import syntaxtree.TimesExpression;
 import syntaxtree.TrueLiteral;
 import syntaxtree.Type;
 import syntaxtree.TypeDeclaration;
 import syntaxtree.VarDeclaration;
 import syntaxtree.WhileStatement;
 import visitor.GJDepthFirst;
 
 
 public class TranslateToVaporVisitor extends GJDepthFirst<Object, Object> 
 {
 	Map<String, MJClass> classes;
 	
 	/**
 	 * @param classes
 	 */
 	public TranslateToVaporVisitor(Map<String, MJClass> classes) {
 		super();
 		this.classes = classes;
 	}
 
 	//
 	// Auto class visitors--probably don't need to be overridden.
 	//
 	public Object visit(NodeList n, Object argu) 
 	{
 		List<Object> _ret=new ArrayList<Object>();
 		int _count=0;
 		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) 
 		{
 			Object o = e.nextElement().accept(this,argu);
 			if (Input.class.isInstance(argu) && Output.class.isInstance(o))
             {
             	((Input) argu).nextVariableIndex = ((Output) o).nextVariableIndex;
             	if (ExpressionInput.class.isInstance(argu) && ExpressionOutput.class.isInstance(o))
             	{
             		((ExpressionInput) argu).variableTypes = ((ExpressionOutput) o).variableTypes;
             	}
             }
 			_ret.add(o);
 			_count++;
 		}
 		return _ret;
 	}
 
 	   public Object visit(NodeListOptional n, Object argu) {
 		   List<Object> _ret = new ArrayList<Object>();
 	      if ( n.present() ) {
 	         int _count=0;
 	         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
 	            Object o = e.nextElement().accept(this,argu);
 	            if (Input.class.isInstance(argu) && Output.class.isInstance(o))
 	            {
 	            	((Input) argu).nextVariableIndex = ((Output) o).nextVariableIndex;
 	            	if (ExpressionInput.class.isInstance(argu) && ExpressionOutput.class.isInstance(o))
 	            	{
 	            		((ExpressionInput) argu).variableTypes = ((ExpressionOutput) o).variableTypes;
 	            	}
 	            }
 	            _ret.add(o);
 	            _count++;
 	         }
 	      }
 	      
 	      return _ret;
 	   }
 
 	   public Object visit(NodeOptional n, Object argu) {
 	      if ( n.present() )
 	         return n.node.accept(this,argu);
 	      else
 	         return null;
 	   }
 
 	   public Object visit(NodeSequence n, Object argu) {
 		   List<Object> _ret = new ArrayList<Object>();
 	      int _count=0;
 	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
 	         Object o = e.nextElement().accept(this,argu);
 	         if (Input.class.isInstance(argu) && Output.class.isInstance(o))
 	            {
 	            	((Input) argu).nextVariableIndex = ((Output) o).nextVariableIndex;
 	            	if (ExpressionInput.class.isInstance(argu) && ExpressionOutput.class.isInstance(o))
 	            	{
 	            		((ExpressionInput) argu).variableTypes = ((ExpressionOutput) o).variableTypes;
 	            	}
 	            }
 	         _count++;
 	      }
 	      return _ret;
 	   }
 
 	   public Object visit(NodeToken n, Object argu) { return null; }
 
 	   //
 	   // User-generated visitor methods below
 	   //
 
 	   /**
 	    * f0 -> MainClass()
 	    * f1 -> ( TypeDeclaration() )*
 	    * f2 -> <EOF>
 	    */
 	   public Object visit(Goal n, Object argu) {
 		  ClassInput input = new ClassInput(0);
 	      MainOutput main = (MainOutput) n.f0.accept(this, input);
 	      
 	      input.nextVariableIndex = main.nextVariableIndex;
 	      List<ClassOutput> classOutputs = (List<ClassOutput>) n.f1.accept(this, input);
 	      
 	      StringBuilder codeBuilder = new StringBuilder(main.code);
 	      for (ClassOutput c_i : classOutputs)
 	      {
 	    	  codeBuilder.append(c_i.code);
 	      }
 	      
 	      String code = codeBuilder.toString();
 	      
 	      return code;
 	   }
 
 	   /**
 	    * f0 -> "class"
 	    * f1 -> Identifier()
 	    * f2 -> "{"
 	    * f3 -> "public"
 	    * f4 -> "static"
 	    * f5 -> "void"
 	    * f6 -> "main"
 	    * f7 -> "("
 	    * f8 -> "String"
 	    * f9 -> "["
 	    * f10 -> "]"
 	    * f11 -> Identifier()
 	    * f12 -> ")"
 	    * f13 -> "{"
 	    * f14 -> ( VarDeclaration() )*
 	    * f15 -> ( Statement() )*
 	    * f16 -> "}"
 	    * f17 -> "}"
 	    */
 	   public Object visit(MainClass n, Object argu) {
 		  int nextVariableIndex = 0;
 		  
 		  ExpressionInput input = new ExpressionInput(0, null, null, null);
 	      List<VariableDeclarationOutput> variables = (List<VariableDeclarationOutput>) n.f14.accept(this, input);
 	      Map<String, String> variableTypes = new HashMap<String, String>();
 	      for (VariableDeclarationOutput v_i : variables)
 	      {
 	    	  variableTypes.put(v_i.variableName, v_i.type);
 	      }
 	      
 	      input = new ExpressionInput(0, variableTypes, null, variableTypes.keySet());
 	      List<StatementOutput> statements = (List<StatementOutput>) n.f15.accept(this, input);
 	      
 	      StringBuilder mainBuilder = new StringBuilder("func Main()\n");
 	      for (StatementOutput s_i : statements)
 	      {
 	    	  mainBuilder.append(s_i.code);
 	    	  nextVariableIndex = s_i.nextVariableIndex;
 	      }
 	      
 	      mainBuilder.append("ret\n\n");
 	      
 	      String code = mainBuilder.toString();
 	      
 	      MainOutput _ret = new MainOutput(code, nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> ClassDeclaration()
 	    *       | ClassExtendsDeclaration()
 	    */
 	   public Object visit(TypeDeclaration n, Object argu) {
 	      ClassOutput _ret = (ClassOutput) n.f0.accept(this, argu);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "class"
 	    * f1 -> Identifier()
 	    * f2 -> "{"
 	    * f3 -> ( VarDeclaration() )*
 	    * f4 -> ( MethodDeclaration() )*
 	    * f5 -> "}"
 	    */
 	   public Object visit(ClassDeclaration n, Object argu) {
 		   ClassInput input = (ClassInput) argu;
 	      ClassOutput _ret = visitClass(n.f1, n.f3, n.f4, input);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "class"
 	    * f1 -> Identifier()
 	    * f2 -> "extends"
 	    * f3 -> Identifier()
 	    * f4 -> "{"
 	    * f5 -> ( VarDeclaration() )*
 	    * f6 -> ( MethodDeclaration() )*
 	    * f7 -> "}"
 	    */
 	   public Object visit(ClassExtendsDeclaration n, Object argu) {
 		  ClassInput input = (ClassInput) argu;
 	      ClassOutput _ret = visitClass(n.f1, n.f5, n.f6, input);
 	      return _ret;
 	   }
 	   
 	   private ClassOutput visitClass(Identifier classNameNode, NodeListOptional fieldsNode, NodeListOptional methodsNode, ClassInput input)
 	   {
 		   int nextVariableIndex = input.nextVariableIndex;
 			  
 			  String className = classNameNode.f0.tokenImage;
 			  MJClass mjclass = classes.get(className);
 			  
 			  // Build method table with correct order
 			  MJMethod[] methodArray = new MJMethod[mjclass.methods.size()]; 
 			  for (MJMethod method : mjclass.methods.values())
 			  {
 				  methodArray[method.methodTableIndex] = method;
 			  }
 			  String methodTableLabel = "const vmt_" + className + "\n";
 			  StringBuilder methodTableBuilder = new StringBuilder(methodTableLabel);
 			  for (MJMethod method : methodArray)
 			  {
 				  String funcLabel = ":" + className + "." + method.name + "\n";
 				  methodTableBuilder.append(funcLabel);
 			  }
 			  String methodTable = methodTableBuilder.toString();
 			  
 			  // Construct field map
 			  /*ExpressionInput exprInput = new ExpressionInput(input.nextVariableIndex, null, null, null);
 		      List<VariableDeclarationOutput> fields = (List<VariableDeclarationOutput>) fieldsNode.accept(this, exprInput);
 		      Map<String, String> fieldMap = new HashMap<String, String>();
 		      for (VariableDeclarationOutput v_i : fields)
 		      {
 		    	  fieldMap.put(v_i.variableName, v_i.type);
 		      }*/
 		      
 		      // TODO: change methodinput
 		      MethodInput methodInput = new MethodInput(input.nextVariableIndex, classes.get(className));
 		      List<MethodOutput> methods = (List<MethodOutput>) methodsNode.accept(this, methodInput);
 		      StringBuilder methodBuilder = new StringBuilder();
 		      for (MethodOutput m_i : methods)
 		      {
 		    	  methodBuilder.append(m_i.code);
 		    	  nextVariableIndex = m_i.nextVariableIndex;
 		      }
 		      String methodCode = methodBuilder.toString();
 		      
 		      String code = methodTable + "\n" + methodCode;
 		      
 		      ClassOutput _ret = new ClassOutput(code, nextVariableIndex);
 		      return _ret;
 	   }
 
 	   /**
 	    * f0 -> Type()
 	    * f1 -> Identifier()
 	    * f2 -> ";"
 	    */
 	   public Object visit(VarDeclaration n, Object argu) {
 	      String type = (String) n.f0.accept(this, argu);
 	      String variableName = n.f1.f0.tokenImage;
 	      
 	      VariableDeclarationOutput _ret = new VariableDeclarationOutput(type, variableName);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "public"
 	    * f1 -> Type()
 	    * f2 -> Identifier()
 	    * f3 -> "("
 	    * f4 -> ( FormalParameterList() )?
 	    * f5 -> ")"
 	    * f6 -> "{"
 	    * f7 -> ( VarDeclaration() )*
 	    * f8 -> ( Statement() )*
 	    * f9 -> "return"
 	    * f10 -> Expression()
 	    * f11 -> ";"
 	    * f12 -> "}"
 	    */
 	   public Object visit(MethodDeclaration n, Object argu) {
 		   MethodInput input = (MethodInput) argu;
 	      String methodName = n.f2.f0.tokenImage;
 	      
 	      ExpressionInput tempInput = new ExpressionInput(input.nextVariableIndex, null, null, null); 
 	      List<ParameterOutput> paramList = (List<ParameterOutput>) n.f4.accept(this, tempInput);
 	      List<VariableDeclarationOutput> variableList = (List<VariableDeclarationOutput>) n.f7.accept(this, tempInput);
 	      
 	      // Build map of variable name -> type, starting with class fields
 	      Map<String, String> variableTypes = new HashMap<String, String>();
 	      for (MJField mjfield : input.currentClass.fields.values())
 	      {
 	    	  variableTypes.put(mjfield.name, mjfield.type);
 	      }
 	      // Add the this type to map
 	      variableTypes.put("this", input.currentClass.name);
 	      
 	      // Keep track of local variables
 	      Set<String> localVariables = new HashSet<String>();
 	      localVariables.add("this");
 	      
 	      // Add method parameters to map and local variables
 	      String parameters = "";
 	      if (paramList != null)
 	      {
 		      StringBuilder paramBuilder = new StringBuilder();
 		      for (ParameterOutput p_i : paramList)
 		      {
 		    	  variableTypes.put(p_i.variableName, p_i.type);
 		    	  localVariables.add(p_i.variableName);
 		    	  paramBuilder.append(" ");
 		    	  paramBuilder.append(p_i.variableName);
 		      }
 		      parameters = paramBuilder.toString();
 	      }
 	      
 	      // Add local variables to map and set
 	      for (VariableDeclarationOutput v_i : variableList)
 	      {
 	    	  variableTypes.put(v_i.variableName, v_i.type);
 	    	  localVariables.add(v_i.variableName);
 	      }
 	      
 	      ExpressionInput exprInput = new ExpressionInput(input.nextVariableIndex, variableTypes, input.currentClass, localVariables);
 	      
 	      List<StatementOutput> statementList = (List<StatementOutput>) n.f8.accept(this, exprInput);
 	      StringBuilder statementsBuilder = new StringBuilder();
 	      for (StatementOutput s_i : statementList)
 	      {
 	    	  statementsBuilder.append(s_i.code);
 	    	  exprInput.nextVariableIndex = s_i.nextVariableIndex;
 	      }
 	      String statements = statementsBuilder.toString();
 	      
 	      ExpressionOutput returnExpr = (ExpressionOutput) n.f10.accept(this, exprInput);
 	      
 	      String funcDeclaration = "func " + input.currentClass.name + "." + methodName + "(this" + parameters + ")\n";
 	      String returnLine = "ret " + returnExpr.expressionVariable + "\n\n";
 	      
 	      String code = funcDeclaration + statements + returnExpr.code + returnLine;
 	      
 	      MethodOutput _ret = new MethodOutput(code, returnExpr.nextVariableIndex);
 	      
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> FormalParameter()
 	    * f1 -> ( FormalParameterRest() )*
 	    */
 	   public Object visit(FormalParameterList n, Object argu) {
 	      ParameterOutput p1 = (ParameterOutput) n.f0.accept(this, argu);
 	      List<ParameterOutput> _ret = (List<ParameterOutput>) n.f1.accept(this, argu);
 	      _ret.add(0, p1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> Type()
 	    * f1 -> Identifier()
 	    */
 	   public Object visit(FormalParameter n, Object argu) {
 	      String type = (String) n.f0.accept(this, argu);
 	      String variableName = n.f1.f0.tokenImage;
 	      
 	      ParameterOutput _ret = new ParameterOutput(type, variableName);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> ","
 	    * f1 -> FormalParameter()
 	    */
 	   public Object visit(FormalParameterRest n, Object argu) {
 	      ParameterOutput _ret = (ParameterOutput) n.f1.accept(this, argu);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> ArrayType()
 	    *       | BooleanType()
 	    *       | IntegerType()
 	    *       | Identifier()
 	    */
 	   public Object visit(Type n, Object argu) {
 	      Object o = n.f0.accept(this, argu);
 	      if (o.getClass() == String.class)
 	    	  return o;
 	      ExpressionOutput e = (ExpressionOutput) o;	    
 	      return e.expressionVariable;
 	   }
 
 	   /**
 	    * f0 -> "int"
 	    * f1 -> "["
 	    * f2 -> "]"
 	    */
 	   public Object visit(ArrayType n, Object argu) {
 	      String _ret = "int[]";
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "boolean"
 	    */
 	   public Object visit(BooleanType n, Object argu) {
 	      String _ret = "boolean";
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "int"
 	    */
 	   public Object visit(IntegerType n, Object argu) {
 		   String _ret = "int";
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> Block()
 	    *       | AssignmentStatement()
 	    *       | ArrayAssignmentStatement()
 	    *       | IfStatement()
 	    *       | WhileStatement()
 	    *       | PrintStatement()
 	    */
 	   public Object visit(Statement n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      StatementOutput _ret = (StatementOutput) n.f0.accept(this, input);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "{"
 	    * f1 -> ( Statement() )*
 	    * f2 -> "}"
 	    */
 	   public Object visit(Block n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      List<StatementOutput> statementList = (List<StatementOutput>) n.f1.accept(this, input);
 	      
 	      StringBuilder codeBuilder = new StringBuilder();
 	      int nextVariableIndex = input.nextVariableIndex;
 	      for (StatementOutput s_i : statementList)
 	      {
 	    	  codeBuilder.append(s_i.code);
 	    	  nextVariableIndex = s_i.nextVariableIndex;
 	      }
 	      String code = codeBuilder.toString();
 	      
 	      StatementOutput _ret = new StatementOutput(code, nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> Identifier()
 	    * f1 -> "="
 	    * f2 -> Expression()
 	    * f3 -> ";"
 	    */
 	   //TODO: check if identifier is local var or class field - DONE
 	   public Object visit(AssignmentStatement n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		  String variable = n.f0.f0.tokenImage;
 		  if (!input.localVariables.contains(variable))
 		  {
			  MJField field = input.currentClass.getFields().get(variable);
 			  variable = "[this+" + Integer.toString(field.index*4) + "]";
 		  }
 	      ExpressionOutput e = (ExpressionOutput) n.f2.accept(this, input);
 	      
 	      String line1 = variable + " = " + e.expressionVariable + "\n";
 	      String code = e.code + line1;
 	      
 	      StatementOutput _ret = new StatementOutput(code, e.nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> Identifier()
 	    * f1 -> "["
 	    * f2 -> Expression()
 	    * f3 -> "]"
 	    * f4 -> "="
 	    * f5 -> Expression()
 	    * f6 -> ";"
 	    */
 	 //TODO: check if identifier is local var or class field - DONE
 	   public Object visit(ArrayAssignmentStatement n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		  String arrayAddrVariable = n.f0.f0.tokenImage;
 		  if (!input.localVariables.contains(arrayAddrVariable))
 		  {
			  MJField field = input.currentClass.getFields().get(arrayAddrVariable);
 			  arrayAddrVariable = "[this+" + Integer.toString(field.index*4) + "]";
 		  }
 		  ExpressionOutput e1 = (ExpressionOutput) n.f2.accept(this, input);
 		  
 		  input.nextVariableIndex = e1.nextVariableIndex;
 		  input.variableTypes = e1.variableTypes;
 	      ExpressionOutput e2 = (ExpressionOutput) n.f5.accept(this, input);
 	      
 	      String indexVariable = e1.expressionVariable;
 	      String offsetVariable = "t." + Integer.toString(e2.nextVariableIndex);
 	      String actualAddrVariable = "t." + Integer.toString(e2.nextVariableIndex+1);
 	      
 	      // calculate the address
 	      String line1 = offsetVariable + " = MulS(" + indexVariable + " 4)\n"; 
 	      String line2 = actualAddrVariable + " = Add(" + arrayAddrVariable + " " + offsetVariable + ")\n";
 	      String expressionVariable = "[" + actualAddrVariable + "]";
 	      
 	      String assignmentLine = expressionVariable + " = " + e2.expressionVariable + "\n";
 	      
 	      String code = e1.code + e2.code + line1 + line2 + assignmentLine;
 	      
 	      StatementOutput _ret = new StatementOutput(code, e2.nextVariableIndex+2);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "if"
 	    * f1 -> "("
 	    * f2 -> Expression()
 	    * f3 -> ")"
 	    * f4 -> Statement()
 	    * f5 -> "else"
 	    * f6 -> Statement()
 	    */
 	   public Object visit(IfStatement n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e1 = (ExpressionOutput) n.f2.accept(this, input);
 	      
 	      input.nextVariableIndex = e1.nextVariableIndex;
 	      StatementOutput s1 = (StatementOutput) n.f4.accept(this, argu);
 	      
 	      input.nextVariableIndex = s1.nextVariableIndex;
 	      StatementOutput s2 = (StatementOutput) n.f6.accept(this, input);
 	      
 	      String ifNum = Integer.toString(s2.nextVariableIndex);
 	      String condCheck = "if0 " + e1.expressionVariable +" goto :if" + ifNum + "_else\n";
 	      String gotoEnd = "goto :if" + ifNum + "_end\n";
 	      String elseLabel = "if" + ifNum + "_else:\n";
 	      String endLabel = "if" + ifNum + "_end:\n";
 	      String code = e1.code + condCheck + s1.code + gotoEnd + elseLabel + s2.code + endLabel;
 	      
 	      StatementOutput _ret = new StatementOutput(code, s2.nextVariableIndex+1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "while"
 	    * f1 -> "("
 	    * f2 -> Expression()
 	    * f3 -> ")"
 	    * f4 -> Statement()
 	    */
 	   public Object visit(WhileStatement n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e1 = (ExpressionOutput) n.f2.accept(this, input);
 	      
 	      input.nextVariableIndex = e1.nextVariableIndex;
 	      StatementOutput s1 = (StatementOutput) n.f4.accept(this, input);
 	      
 	      String whileNum = Integer.toString(s1.nextVariableIndex);
 	      String whileTop = "while" + whileNum  + "_top:\n";
 	      String condCheck = "if0 " + e1.expressionVariable + " goto :while" + whileNum + "_end\n";
 	      String loopToTop = "goto :while" + whileNum + "_top\n";
 	      String whileEnd = "while" + whileNum + "_end:\n";
 	      String code = whileTop + e1.code + condCheck + s1.code + loopToTop + whileEnd;
 	      
 	      StatementOutput _ret = new StatementOutput(code, s1.nextVariableIndex+1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "System.out.println"
 	    * f1 -> "("
 	    * f2 -> Expression()
 	    * f3 -> ")"
 	    * f4 -> ";"
 	    */
 	   public Object visit(PrintStatement n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e = (ExpressionOutput) n.f2.accept(this, input);
 	      String line = "PrintIntS(" + e.expressionVariable + ")\n";
 	      String code = e.code + line;
 	      StatementOutput _ret = new StatementOutput(code, e.nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> AndExpression()
 	    *       | CompareExpression()
 	    *       | PlusExpression()
 	    *       | MinusExpression()
 	    *       | TimesExpression()
 	    *       | ArrayLookup()
 	    *       | ArrayLength()
 	    *       | MessageSend()
 	    *       | PrimaryExpression()
 	    */
 	   public Object visit(Expression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput _ret = (ExpressionOutput) n.f0.accept(this, input);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "&&"
 	    * f2 -> PrimaryExpression()
 	    */
 	   // nonzero is true, 0 is false. Multiply is same as &&
 	   public Object visit(AndExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 		      
 		      input.nextVariableIndex = e1.nextVariableIndex;
 		      input.variableTypes = e1.variableTypes;
 		      ExpressionOutput e2 = (ExpressionOutput) n.f2.accept(this, input);
 		      
 		      String expressionVariable = "t." + e2.nextVariableIndex;
 		      String line1 = expressionVariable + " = MulS(" + e1.expressionVariable + " " + e2.expressionVariable + ")\n";
 		      String code = e1.code + e2.code + line1;
 		      
 		      ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, e2.variableTypes, e2.nextVariableIndex+1);
 		      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "<"
 	    * f2 -> PrimaryExpression()
 	    */
 	   public Object visit(CompareExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 		      
 		      input.nextVariableIndex = e1.nextVariableIndex;
 		      input.variableTypes = e1.variableTypes;
 		      ExpressionOutput e2 = (ExpressionOutput) n.f2.accept(this, input);
 		      
 		      String expressionVariable = "t." + e2.nextVariableIndex;
 		      String line1 = expressionVariable + " = LtS(" + e1.expressionVariable + " " + e2.expressionVariable + ")\n";
 		      String code = e1.code + e2.code + line1;
 		      
 		      ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, e2.variableTypes, e2.nextVariableIndex+1);
 		      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "+"
 	    * f2 -> PrimaryExpression()
 	    */
 	   public Object visit(PlusExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 		      
 		      input.nextVariableIndex = e1.nextVariableIndex;
 		      input.variableTypes = e1.variableTypes;
 		      ExpressionOutput e2 = (ExpressionOutput) n.f2.accept(this, input);
 		      
 		      String expressionVariable = "t." + e2.nextVariableIndex;
 		      String line1 = expressionVariable + " = Add(" + e1.expressionVariable + " " + e2.expressionVariable + ")\n";
 		      String code = e1.code + e2.code + line1;
 		      
 		      ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, e2.variableTypes, e2.nextVariableIndex+1);
 		      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "-"
 	    * f2 -> PrimaryExpression()
 	    */
 	   public Object visit(MinusExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 		      
 		      input.nextVariableIndex = e1.nextVariableIndex;
 		      input.variableTypes = e1.variableTypes;
 		      ExpressionOutput e2 = (ExpressionOutput) n.f2.accept(this, input);
 		      
 		      String expressionVariable = "t." + e2.nextVariableIndex;
 		      String line1 = expressionVariable + " = Sub(" + e1.expressionVariable + " " + e2.expressionVariable + ")\n";
 		      String code = e1.code + e2.code + line1;
 		      
 		      ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, e2.variableTypes, e2.nextVariableIndex+1);
 		      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "*"
 	    * f2 -> PrimaryExpression()
 	    */
 	   public Object visit(TimesExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 	      
 	      input.nextVariableIndex = e1.nextVariableIndex;
 	      input.variableTypes = e1.variableTypes;
 	      ExpressionOutput e2 = (ExpressionOutput) n.f2.accept(this, input);
 	      
 	      String expressionVariable = "t." + e2.nextVariableIndex;
 	      String line1 = expressionVariable + " = MulS(" + e1.expressionVariable + " " + e2.expressionVariable + ")\n";
 	      String code = e1.code + e2.code + line1;
 	      
 	      ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, e2.variableTypes, e2.nextVariableIndex+1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "["
 	    * f2 -> PrimaryExpression()
 	    * f3 -> "]"
 	    */
 	   public Object visit(ArrayLookup n, Object argu) {
 		  ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 	      
 	      input.nextVariableIndex = e1.nextVariableIndex;
 	      input.variableTypes = e1.variableTypes;
 	      ExpressionOutput e2 = (ExpressionOutput) n.f2.accept(this, input);
 	      
 	      String arrayAddrVariable = e1.expressionVariable;
 	      String indexVariable = e2.expressionVariable;
 	      String boundsVar = "t." + Integer.toString(e2.nextVariableIndex);
 	      String offsetVariable = "t." + Integer.toString(e2.nextVariableIndex+1);
 	      String actualAddrVariable = "t." + Integer.toString(e2.nextVariableIndex+2);
 	      
 	      // Check bounds
 	      String assignArrayLength = boundsVar + " = [" + arrayAddrVariable + "-4]\n";
 	      String assignCheckValue = boundsVar + " = Sub(" + boundsVar + " 1)\n";
 	      String assignCheckBool = boundsVar + " = LtS(" + boundsVar + " " + indexVariable + ")\n";
 	      String ifStmt = "if0 " + boundsVar + " goto :bounds" + Integer.toString(e2.nextVariableIndex) + "\n";
 	      String error = "Error(\"array index out of bounds\")\n";
 	      String boundsLabel = "bounds" + Integer.toString(e2.nextVariableIndex) + ":\n";
 	      
 	      // calculate the address
 	      String assignOffset = offsetVariable + " = MulS(" + indexVariable + " 4)\n"; 
 	      String assignAddr = actualAddrVariable + " = Add(" + arrayAddrVariable + " " + offsetVariable + ")\n";
 	      String expressionVariable = "[" + actualAddrVariable + "]";
 	      
 	      String code = e1.code + e2.code + assignArrayLength + assignCheckValue + assignCheckBool + ifStmt + error + boundsLabel + assignOffset + assignAddr;
 	      
 	      ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, e2.variableTypes, e2.nextVariableIndex+3);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "."
 	    * f2 -> "length"
 	    */
 	   public Object visit(ArrayLength n, Object argu) {
 	      ExpressionOutput e = (ExpressionOutput) n.f0.accept(this, argu);
 	      String arrayAddrVariable = e.expressionVariable;
 	      String arrayLengthVariable = "t." + Integer.toString(e.nextVariableIndex);
 	      
 	      String code = arrayLengthVariable + " = [" + arrayAddrVariable + "-4]\n";
 	      
 	      ExpressionOutput _ret = new ExpressionOutput(arrayLengthVariable, code, e.variableTypes, e.nextVariableIndex+1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> PrimaryExpression()
 	    * f1 -> "."
 	    * f2 -> Identifier()
 	    * f3 -> "("
 	    * f4 -> ( ExpressionList() )?
 	    * f5 -> ")"
 	    */
 	   public Object visit(MessageSend n, Object argu) {
 	      ExpressionInput input = (ExpressionInput) argu;
 	      String methodName = n.f2.f0.tokenImage;
 	      
 	      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 	      String objectVariable = e1.expressionVariable;
 	      String className = e1.variableTypes.get(objectVariable);
 	      int methodIndex = classes.get(className).methods.get(methodName).methodTableIndex;
 	      String methodReturnType = classes.get(className).methods.get(methodName).returnType;
 	      
 	      input.nextVariableIndex = e1.nextVariableIndex;
 	      input.variableTypes = e1.variableTypes;
 	      List<ExpressionOutput> expressionList = (List<ExpressionOutput>) n.f4.accept(this, input);
 	      
 	      int nextVarIndex = e1.nextVariableIndex;
 	      Map<String, String> outputVariableTypes = e1.variableTypes;
 	      StringBuilder codeBuilder = new StringBuilder(e1.code);
 	      
 	      // Add code for parameters
 	      
 	      if (expressionList != null)
 	      {
 	    	  for (ExpressionOutput e_i : expressionList)
 		      {
 		    	  codeBuilder.append(e_i.code);
 		    	  nextVarIndex = e_i.nextVariableIndex;
 		    	  outputVariableTypes = e_i.variableTypes;
 		      }
 	      }
 	      
 	      // Assign method table addr to a variable
 	      String methodTableAddr = "t." + Integer.toString(nextVarIndex);
 	      String assignMethodTableAddr = methodTableAddr + " = [" + e1.expressionVariable + "]\n";
 	      
 	      // Assign method addr to a variable
 	      String methodAddr = methodTableAddr;
 	      String assignMethodAddr = methodAddr + " = [" + methodTableAddr + "+" + Integer.toString(methodIndex * 4) + "]\n";
 	      
 	      // Add call line
 	      String returnVar = "t." + Integer.toString(nextVarIndex+1);
 	      
 	      // Build parameter list for call
 	      StringBuilder paramBuilder = new StringBuilder(objectVariable);
 	      if (expressionList != null)
 	      {
 		      for (ExpressionOutput paramExpr : expressionList)
 		      {
 		    	  paramBuilder.append(" ");
 		    	  paramBuilder.append(paramExpr.expressionVariable);
 		      }
 	      }
 	      String parameters = paramBuilder.toString();
 	      
 	      String call = returnVar + " = call " + methodAddr + "(" + parameters + ")\n";
 	      
 	      codeBuilder.append(assignMethodTableAddr);
 	      codeBuilder.append(assignMethodAddr);
 	      codeBuilder.append(call);
 	      
 	      String code = codeBuilder.toString();
 	      outputVariableTypes.put(returnVar, methodReturnType);
 	      
 	      ExpressionOutput _ret = new ExpressionOutput(returnVar, code, outputVariableTypes, nextVarIndex+2);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> Expression()
 	    * f1 -> ( ExpressionRest() )*
 	    */
 	   public Object visit(ExpressionList n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e1 = (ExpressionOutput) n.f0.accept(this, input);
 	      
 	      input.nextVariableIndex = e1.nextVariableIndex;
 	      input.variableTypes = e1.variableTypes;
 	      List<ExpressionOutput> _ret = (List<ExpressionOutput>) n.f1.accept(this, input);
 	      _ret.add(0, e1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> ","
 	    * f1 -> Expression()
 	    */
 	   public Object visit(ExpressionRest n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput _ret = (ExpressionOutput) n.f1.accept(this, input);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> IntegerLiteral()
 	    *       | TrueLiteral()
 	    *       | FalseLiteral()
 	    *       | Identifier()
 	    *       | ThisExpression()
 	    *       | ArrayAllocationExpression()
 	    *       | AllocationExpression()
 	    *       | NotExpression()
 	    *       | BracketExpression()
 	    */
 	   //TODO: may want a special case on identifier to check if its a local var or a class field - DONE
 	   public Object visit(PrimaryExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		   if (Identifier.class.isInstance(n.f0.choice))
 		   {
 			   String variable = ((Identifier) n.f0.choice).f0.tokenImage;
 			   if (!input.localVariables.contains(variable))
 			   {
				   MJField field = input.currentClass.getFields().get(variable);
 				   variable = "[this+" + Integer.toString(field.index*4) + "]";
 				   String expressionVariable = "t." + Integer.toString(input.nextVariableIndex);
 				   String code = expressionVariable + " = " + variable + "\n";
				   input.variableTypes.put(expressionVariable, field.type);
 				   return new ExpressionOutput(expressionVariable, code, input.variableTypes, input.nextVariableIndex+1);
 			   }
 			   
 			   return new ExpressionOutput(variable, "", input.variableTypes, input.nextVariableIndex);
 		   }
 	      ExpressionOutput _ret = (ExpressionOutput) n.f0.accept(this, input);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> <INTEGER_LITERAL>
 	    */
 	   public Object visit(IntegerLiteral n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		  ExpressionOutput _ret= new ExpressionOutput(n.f0.tokenImage, "", input.variableTypes, input.nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "true"
 	    */
 	   public Object visit(TrueLiteral n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		  ExpressionOutput _ret= new ExpressionOutput("1", "", input.variableTypes, input.nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "false"
 	    */
 	   public Object visit(FalseLiteral n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		  ExpressionOutput _ret= new ExpressionOutput("0", "", input.variableTypes, input.nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> <IDENTIFIER>
 	    */
 	   public Object visit(Identifier n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 		  ExpressionOutput _ret= new ExpressionOutput(n.f0.tokenImage, "", input.variableTypes, input.nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "this"
 	    */
 	   public Object visit(ThisExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput _ret = new ExpressionOutput("this", "", input.variableTypes, input.nextVariableIndex);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "new"
 	    * f1 -> "int"
 	    * f2 -> "["
 	    * f3 -> Expression()
 	    * f4 -> "]"
 	    */
 	   public Object visit(ArrayAllocationExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e = (ExpressionOutput) n.f3.accept(this, input);
 	      
 	      String allocSizeVariable = "t." + Integer.toString(e.nextVariableIndex);
 	      String arrayLengthAddrVariable = "t." + Integer.toString(e.nextVariableIndex+1);
 	      String arrayAddrVariable = "t." + Integer.toString(e.nextVariableIndex+2);
 	      
 	      String assignAllocSize = allocSizeVariable + " = MulS(" + e.expressionVariable + " 4)\n"; 
 	      String assignFullAllocSize = allocSizeVariable + " = Add(" + allocSizeVariable + " 4)\n"; // add space for length
 	      String assignArrayLengthAddr = arrayLengthAddrVariable + " = HeapAllocZ(" + allocSizeVariable + ")\n";
 	      String nullCheck = "if " + arrayLengthAddrVariable + " goto :null" + input.nextVariableIndex + "\n";
 	      String error = "Error(\"null pointer\")\n";
 	      String endCheck = "null" + input.nextVariableIndex + ":\n";
 	      String storeLength = "[" + arrayLengthAddrVariable + "] = " + e.expressionVariable + "\n"; 
 	      String assignArrayAddr = arrayAddrVariable + " = Add(" + arrayLengthAddrVariable + " 4)\n";
 	      
 	      String code = e.code + assignAllocSize + assignFullAllocSize + assignArrayLengthAddr + nullCheck + error + endCheck + storeLength + assignArrayAddr;
 	      ExpressionOutput _ret = new ExpressionOutput(arrayAddrVariable, code, e.variableTypes, e.nextVariableIndex+3);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "new"
 	    * f1 -> Identifier()
 	    * f2 -> "("
 	    * f3 -> ")"
 	    */
 	   public Object visit(AllocationExpression n, Object argu) {
 		  ExpressionInput input = (ExpressionInput) argu;
 	      String objectAddrVariable = "t." + Integer.toString(input.nextVariableIndex);
 	      String className = n.f1.f0.tokenImage;
 	      MJClass mjclass = classes.get(className);
 	      int size = 4 * mjclass.getFields().size() + 4;
 	      String assignAllocation = objectAddrVariable + " = HeapAllocZ(" + size + ")\n";
 	      String nullCheck = "if " + objectAddrVariable + " goto :null" + input.nextVariableIndex + "\n";
 	      String error = "Error(\"null pointer\")\n";
 	      String endCheck = "null" + input.nextVariableIndex + ":\n";
 	      String storeMethodTable = "[" + objectAddrVariable + "] = :vmt_" + className + "\n";
 	      String code = assignAllocation + nullCheck + error + endCheck + storeMethodTable;
 	      
 	      Map<String, String> outputVariableTypes = input.variableTypes;
 	      outputVariableTypes.put(objectAddrVariable, className);
 	      ExpressionOutput _ret = new ExpressionOutput(objectAddrVariable, code, outputVariableTypes, input.nextVariableIndex + 1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "!"
 	    * f1 -> Expression()
 	    */
 	   public Object visit(NotExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e = (ExpressionOutput) n.f1.accept(this, input);
 	      String expressionVariable = "t." + Integer.toString(e.nextVariableIndex);
 	      String line1 = expressionVariable + " = Eq(" + e.expressionVariable + " 0)\n";
 	      String code = e.code + line1;
 	      
 	      ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, e.variableTypes, e.nextVariableIndex+1);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "("
 	    * f1 -> Expression()
 	    * f2 -> ")"
 	    */
 	   public Object visit(BracketExpression n, Object argu) {
 		   ExpressionInput input = (ExpressionInput) argu;
 	      ExpressionOutput e = (ExpressionOutput) n.f1.accept(this, input);
 	      String expressionVariable = "t." + Integer.toString(e.nextVariableIndex);
 	      String line1 = expressionVariable + " = " + e.expressionVariable + "\n";
 	      String code = e.code + line1;
 	      
 	      Map<String, String> outputVariableTypes = e.variableTypes;
 	      if (outputVariableTypes.containsKey(e.expressionVariable))
 	    	  outputVariableTypes.put(expressionVariable, e.variableTypes.get(e.expressionVariable));
 	    		  
 		ExpressionOutput _ret = new ExpressionOutput(expressionVariable, code, outputVariableTypes, e.nextVariableIndex+1);
 	      return _ret;
 	   }
 }
