 package javaImportTool;
 
 import java.math.BigInteger;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.jdt.core.dom.*;
 
 public class ASTConverter {
 	
 	private Node root;
 	private String source;
 	private Stack<Node> containers = new Stack<Node>();
 	private List<ImportDeclaration> imports;
 	
 	ASTConverter(Node root, String source)
 	{
 		this.root = root;
 		this.source = source;
 	}
 	
 	private int emptyLinesBetweenNodes(ASTNode a, ASTNode b)
 	{
 		if (a == null || b == null) return 0;
 		int empty = -1;
 		for(int i = a.getStartPosition()+a.getLength(); i<b.getStartPosition(); ++i)
 		{
 			if (source.charAt(i) == '\n') ++empty;
 		}
 		
 		if (empty < 0) return 0;
 		else return empty;
 	}
 	
 	public void visit(CompilationUnit node) throws ConversionException
 	{
 		PackageDeclaration pd = node.getPackage();
 		if (pd == null) containers.push(root);
 		else containers.push(root.getModuleContext( pd.getName().getFullyQualifiedName() ) );
 
 		imports = node.imports();
 	    for (AbstractTypeDeclaration type : (List<AbstractTypeDeclaration>) node.types())
 	    	visit(type, true);
 	    
 	    containers.pop();
 	}
 	
 	public void visit(AbstractTypeDeclaration type, boolean topLevel) throws ConversionException
 	{
 		Node cl = containers.peek().addSymbolNodeInList("classes", "Class", type.getName().getIdentifier());
 		containers.push(cl);
 		
 		setModifiers(type);
 		//TODO: Handle JavaDoc
 		
 		if (topLevel)
 		{
 			// Only process imports if this is a declaration directly in the compilation unit
 			for(ImportDeclaration id : imports)
 			{
 				//TODO: Handle static and on demand imports
 				Node importNode = new Node(null, "NameImport", cl.child("subDeclarations").numChildren());
 				importNode.setChild("importedName", expression(id.getName(), "importedName"));
 				if (id.isOnDemand()) importNode.child("importAll").setLongValue(1);
 				cl.child("subDeclarations").add(importNode);
 			}
 		}
 		
     	if (type.getNodeType() == ASTNode.TYPE_DECLARATION ) visit((TypeDeclaration)type);
     	else if (type.getNodeType() == ASTNode.ENUM_DECLARATION) visit((EnumDeclaration)type);
     	else if (type.getNodeType() == ASTNode.ANNOTATION_TYPE_DECLARATION) visit((AnnotationTypeDeclaration)type);
     	else throw new UnknownFeatureException("CompilationUnit content" + Integer.toString(type.getNodeType()));
     	
     	containers.pop();
 	}
 
 	public void processAnnotations(Node declaration, List<IExtendedModifier> modifiersAndAnnotations)
 			throws ConversionException
 	{
 		Node annotations = declaration.child("annotations");
 		
 		for (IExtendedModifier em : modifiersAndAnnotations)
 		{
 			if (!em.isAnnotation()) continue;
 			
 			Node ann = getAnnotation((Annotation) em);
 			ann.setName(annotations.numChildren());
 			annotations.add(ann);
 		}
 	}
 	
 	public Node getAnnotation(Annotation annotation) throws ConversionException
 	{
 		Node statement = new Node(null, "ExpressionStatement");
 		Node call = statement.setChild("expression", new Node(null, "MethodCallExpression"));
 		call.setChild("callee", expression(annotation.getTypeName(), "callee"));
 		
 		if (annotation instanceof SingleMemberAnnotation)
 		{
 			call.child("arguments").add(expression( ((SingleMemberAnnotation)annotation).getValue(),
 					Integer.toString(call.child("arguments").numChildren())));
 		}
 		else if (annotation instanceof NormalAnnotation)
 		{
 			NormalAnnotation normal = (NormalAnnotation) annotation;
 			
 			for(MemberValuePair mvp : (List<MemberValuePair>)normal.values())
 			{
 				Node assignment = new Node(null, "AssignmentExpression", call.child("arguments").numChildren());
 				assignment.setChild("left", expression(mvp.getName(), "left"));
 				assignment.setChild("right", expression(mvp.getValue(), "right"));
 						
 				call.child("arguments").add(assignment);
 			}
 		}
 		
 		return statement;
 	}
 	
 	public void visit(TypeDeclaration node) throws ConversionException
 	{
 		Node cl = containers.peek();
 		cl.child("cKind").setLongValue(node.isInterface() ? 1 : 0); // Make this a class or an interface
 	
 		// Type parameters
 		processTypeParameters(node.typeParameters());
 
 		// Super classes/interfaces
 		int i = 0;
 		Type st = node.getSuperclassType();
 		if (st != null) cl.child("baseClasses").add(typeExpression(st, Integer.toString(i++)));
 			
 		for (Type t : (List<Type>)node.superInterfaceTypes())
 			cl.child("baseClasses").add(typeExpression(t, Integer.toString(i++)));
 		
 		// Body declarations
 		visitClassBody(node.bodyDeclarations());
 	}
 	
 	public void visit(AnnotationTypeDeclaration node) throws ConversionException
 	{
 		Node cl = containers.peek();
 		cl.child("cKind").setLongValue(5); // Make this an annotation
 		
 		// Body declarations
 		visitClassBody(node.bodyDeclarations());
 	}
 	
 	public void visit(EnumDeclaration node) throws ConversionException
 	{
 		Node cl = containers.peek();
 		cl.child("cKind").setLongValue(4); // Make this an Enum
 
 		// Super classes/interfaces
 		int i = 0;			
 		for (Type t : (List<Type>)node.superInterfaceTypes())
 			cl.child("baseClasses").add(typeExpression(t, Integer.toString(i++)));
 		
 		// Body declarations
 		visitClassBody(node.bodyDeclarations());
 	}
 	
 	public void visitClassBody(List<BodyDeclaration> declarations) throws ConversionException
 	{
 		// Methods and Fields
 		int fields = 0;
 		int methods = 0;
 		int annotations = 0;
 		
 		for(BodyDeclaration b : declarations)
 		{
 			if (b instanceof MethodDeclaration) visit((MethodDeclaration)b, methods++);
 			else if (b instanceof FieldDeclaration) visit((FieldDeclaration)b, fields++);
 			else if (b instanceof AbstractTypeDeclaration) visit((AbstractTypeDeclaration)b, false);
 			else if (b instanceof Initializer); // TODO: Handle this
 			else if (b instanceof EnumConstantDeclaration)
 			{
 				// TODO: Handle special enumeration types
 				containers.peek().addSymbolNodeInList("enumerators", "Enumerator",
 						((EnumConstantDeclaration)b).getName().getIdentifier());
 			}
 			else if (b instanceof AnnotationTypeMemberDeclaration)
 				visit((AnnotationTypeMemberDeclaration)b, annotations++);
 			else throw new UnknownFeatureException("Unknown body declaration: " + b.getClass().getSimpleName());
 		}
 	}
 	
 	public void visit(MethodDeclaration node, int name) throws ConversionException
 	{
 		Node me = containers.peek().addSymbolNodeInList("methods", "Method", node.getName().getIdentifier());
 		containers.push(me);
 		
 		me.child("mthKind").setLongValue(node.isConstructor() ? 1 : 0);
 		
 		setModifiers(node);
 		processTypeParameters(node.typeParameters());
 		processParameters(node.parameters());
 		
 		// TODO: Implement support for variable method arity
 		
 		if (node.getReturnType2() != null)
 		{
 			Node result = me.child("results").add(new Node(null, "FormalResult", 0));
 			result.setSymbol("");
 			Node type = typeExpression(node.getReturnType2(), "typeExpression");
 			result.setChild("typeExpression", addExtraDimensions(type, node.getExtraDimensions()));
 		}
 		
 		// TODO: Implement support for throws specifier
 		if (node.getBody() != null)
 			visitBody(node.getBody().statements(), "items");
 		
 		containers.pop();
 	}
 	
 	public void visit(FieldDeclaration node, int name) throws ConversionException
 	{
 		for(VariableDeclarationFragment vdf : (List<VariableDeclarationFragment>)node.fragments())
 		{
 			Node field = containers.peek().addSymbolNodeInList("fields", "Field", vdf.getName().getIdentifier());
 			containers.push(field);
 			
 			setModifiers(node);
 			Node type = typeExpression(node.getType(), "typeExpression");
 			field.setChild("typeExpression", addExtraDimensions(type, vdf.getExtraDimensions()));
 
 			if (vdf.getInitializer() != null)
 				field.add(expression(vdf.getInitializer(), "initialValue"));
 			
 			containers.pop();
 		}
 	}
 	
 	public void visit(AnnotationTypeMemberDeclaration node, int name) throws ConversionException
 	{
 		Node field = containers.peek().addSymbolNodeInList("fields", "Field", node.getName().getIdentifier());
 		containers.push(field);
 			
 		setModifiers(node);
 		field.setChild("typeExpression", typeExpression(node.getType(), "typeExpression"));
 
 		if (node.getDefault() != null)
 			field.add(expression(node.getDefault(), "initialValue"));
 			
 		containers.pop();
 	}
 	
 	public void visitBody(List<Statement> statements, String name) throws ConversionException
 	{
 		containers.push(containers.peek().child(name));
 		Statement prevStatement = null;
 		for(Statement s : statements)
 		{
 			// Insert empty lines
 			for(int i = 0; i<emptyLinesBetweenNodes(prevStatement, s); ++i)
 			{
 		    	Node empty = new Node(null, "ExpressionStatement", Integer.toString(containers.peek().numChildren()));
 		    	empty.setChild("expression", new Node(null, "EmptyExpression", "expression"));
 		    	containers.peek().add(empty);
 		    }
 		    prevStatement = s;
 			    
 		    List<Node> nodeList = statement(s, Integer.toString(containers.peek().numChildren()));
 		    for(Node n : nodeList)
 		    {
 		    	if (n != null)
 		    	{
 		    		n.setName(Integer.toString(containers.peek().numChildren()));
 		    		containers.peek().add(n);
 		    	}
 		    }
 		}
 		containers.pop();
 	}
 	
 	public void visitStatementBody(Statement body, Node enclosingStatement, String bodyName) throws ConversionException
 	{
 		containers.push(enclosingStatement);
     	if (body instanceof Block)
     		visitBody(((Block)body).statements(),bodyName);
     	else
     	{
     		List<Statement> bodyList = new LinkedList<Statement>();
     		bodyList.add(body);
     		visitBody(bodyList, bodyName);
     	}
     	containers.pop();
 	}
 	
 	/**
 	 * Returns (possibly multiple) Nodes that correspond to the provided statement.
 	 */
 	public List<Node> statement(Statement s, String name) throws ConversionException
 	{
 		Node node = null;
 		List<Node> multipleNodes = null;
 		
 		if ( s instanceof AssertStatement); // TODO: Implement this
 	    else if ( s instanceof Block)
 	    {
 	    	node = new Node(null, "Block", name);
 	    	containers.push(node);
 	    	visitBody(((Block)s).statements(),"items");
 	    	containers.pop();
 	    } else if ( s instanceof BreakStatement)
 	    {
 	    	//TODO:Implement labels
 	    	node = new Node(null, "BreakStatement", name);
 	    } else if ( s instanceof ConstructorInvocation)
 	    {
 	    	//TODO: Implement this
 	    } else if ( s instanceof ContinueStatement)
 	    {
 	    	//TODO:Implement labels
 	    	node = new Node(null, "ContinueStatement", name);
 	    } else if ( s instanceof DoStatement)
 	    {
 	    	node = new Node(null, "LoopStatement", name);
 	    	node.child("lpKind").setLongValue(1);
 	    	DoStatement fs = (DoStatement) s;
 	    	if (fs.getExpression() != null)
 	    		node.add(expression(fs.getExpression(),"condition"));
 	    	
 	    	visitStatementBody(fs.getBody(), node, "body");
 	    } else if ( s instanceof EmptyStatement)
 	    {
 	    	//TODO:Investigate if returning null here is OK?
 	    } else if ( s instanceof ExpressionStatement)
 	    {
 	    	node = new Node(null, "ExpressionStatement", name);
 	    	node.setChild("expression", expression(((ExpressionStatement)s).getExpression(), "expression"));
 	    } else if ( s instanceof ForStatement)
 	    {
 	    	node = new Node(null, "LoopStatement", name);
 	    	ForStatement fs = (ForStatement) s;
 	    	if (!fs.initializers().isEmpty())
 	    		node.add(combineExpressionsWithComma(fs.initializers(),"initStep"));
 	    	if (!fs.updaters().isEmpty())
 	    		node.add(combineExpressionsWithComma(fs.updaters(),"updateStep"));
 	    	if (fs.getExpression() != null)
 	    		node.add(expression(fs.getExpression(),"condition"));
 	    	
 	    	visitStatementBody(fs.getBody(), node, "body");
 	    } else if ( s instanceof EnhancedForStatement)
 	    {
 	    	node = new Node(null, "ForEachStatement", name);
 	    	EnhancedForStatement fs = (EnhancedForStatement) s;
 	    	
 	    	//TODO: implement support for modifiers
 	    	node.child("varName").setStringValue(fs.getParameter().getName().getIdentifier());
 	    	node.add(addExtraDimensions(
 	    			typeExpression(fs.getParameter().getType(), "varType"),fs.getParameter().getExtraDimensions()));
 	    	
 	    	node.setChild("collection", expression(fs.getExpression(),"collection"));
 	    	
 	    	visitStatementBody(fs.getBody(), node, "body");
 	    } else if ( s instanceof IfStatement)
 	    {
 	    	node = new Node(null, "IfStatement", name);
 	    	IfStatement is = (IfStatement) s;
 	    	
 	    	visitStatementBody(is.getThenStatement(), node, "thenBranch");
 	    	if (is.getElseStatement() != null) visitStatementBody(is.getElseStatement(), node, "elseBranch");
 	    	
 	    	node.setChild("condition", expression(is.getExpression(), "condition"));
 	    	
 	    } else if ( s instanceof LabeledStatement); // TODO: Implement this
 	    else if ( s instanceof ReturnStatement)
 	    {
 	    	node = new Node(null, "ReturnStatement", name);
 	    	ReturnStatement rs = (ReturnStatement) s;
 	    	if (rs.getExpression() != null)
 	    		node.child("values").add(expression(rs.getExpression(), "0"));
 	    	
 	    } else if ( s instanceof SuperConstructorInvocation); // TODO: Implement this
 	    else if ( s instanceof SwitchStatement)
 	    {
 	    	node = new Node(null, "SwitchStatement", name);
 	    	SwitchStatement ss = (SwitchStatement) s;
 	    	
 	    	node.setChild("switchExpression", expression(ss.getExpression(),"switchExpression"));
 	    	
 	    	Node lastStatementContainer = node;
 	    	for (Statement item : (List<Statement>) ss.statements())
 	    	{	    		
 	    		if (item instanceof SwitchCase)
 	    		{
 	    			SwitchCase switchCase = (SwitchCase) item;
 	    			lastStatementContainer = new Node(node, "CaseStatement", node.child("body").numChildren());
 	    			node.child("body").add(lastStatementContainer);
 	    			
 	    			if (switchCase.getExpression() != null)
 	    				lastStatementContainer.add(expression(switchCase.getExpression(),"caseExpression"));
 	    		}
 	    		else if (item instanceof Block) // Treat blocks into individual statements
 	    		{
 	    			Block block = (Block) item;
 	    			for(Statement blockStatement : (List<Statement>) block.statements())
 	    			{
 	    				for(Node innerStatement : statement(blockStatement,
 	    						Integer.toString(lastStatementContainer.child("body").numChildren())))
 	    				{
 	    					if (innerStatement != null)
 	    					{
 	    						innerStatement.setName(
 	    								Integer.toString(lastStatementContainer.child("body").numChildren()));
 	    						lastStatementContainer.child("body").add(innerStatement);
 	    					}
 	    				}	
 	    			}
 	    		}
 	    		else
 	    		{
 	    			for(Node innerStatement :
 	    				statement(item,Integer.toString(lastStatementContainer.child("body").numChildren())))
     				{
     					if (innerStatement != null)
     					{
     						innerStatement.setName(
     								Integer.toString(lastStatementContainer.child("body").numChildren()));
     						lastStatementContainer.child("body").add(innerStatement);
     					}
     				}
 	    		}
 	    	}
 	    } else if ( s instanceof SwitchCase); //Handled above
 	    else if ( s instanceof SynchronizedStatement); // TODO: Implement this
 	    else if ( s instanceof ThrowStatement)
 	    {
 	    	node = new Node(null, "ExpressionStatement", name);
 	    	Node throwExpression = new Node(null, "ThrowExpression", "expression");
 	    	node.setChild("expression", throwExpression);
 	    	throwExpression.setChild("expr", expression(((ThrowStatement)s).getExpression(), "expr"));
 	    } else if ( s instanceof TryStatement)
 	    {
 	    	node = new Node(null, "TryCatchFinallyStatement", name);
 	    	TryStatement ts = (TryStatement) s;
 	    	//TODO: Handle resources
 	    	
 	    	visitStatementBody(ts.getBody(),node,"tryBody");
 	    	
 	    	if (ts.getFinally() != null)
 	    		visitStatementBody(ts.getFinally(),node,"finallyBody");
 	    	
 	    	for(CatchClause cc : (List<CatchClause>)ts.catchClauses())
 	    	{
 	    		Node catchNode = new Node(null, "CatchClause", node.child("catchClauses").numChildren());
 	    		node.child("catchClauses").add(catchNode);
 	    		
 	    		visitStatementBody(cc.getBody(),catchNode,"body");
 	    		
 	    		Node varDeclExpression = catchNode.add(
 	    				new Node(null,"VariableDeclarationExpression", "exceptionToCatch"));
 	    		Node varDecl = varDeclExpression.child("decl");
 	    		//TODO: handle modifiers
 	    		varDecl.setChild("typeExpression", addExtraDimensions(typeExpression(cc.getException().getType(),
 	    				"typeExpression"), cc.getException().getExtraDimensions()) );
 	    		varDecl.setSymbol(cc.getException().getName().getIdentifier());
 	    		//TODO: handle varArgs (...)
 	    		if (cc.getException().getInitializer() != null)
 	    			varDecl.add(expression(cc.getException().getInitializer(),"initialValue"));
 	    	}
 	    } else if ( s instanceof TypeDeclarationStatement); // TODO: Implement this
 	    else if ( s instanceof VariableDeclarationStatement)
 	    {    	
 	    	VariableDeclarationStatement vds = (VariableDeclarationStatement) s;
 	    	
 	    	multipleNodes = new LinkedList<Node>();
 	    	for(Node varDecl: variableDeclarationExpressions(name, vds.getType(), vds.fragments()))
 	    	{
 	    		// It's ok to reuse the same name for all generated variables, since this will be fixed later.   
 	    		Node exprStat = new Node(null,"ExpressionStatement",name);
 	    		varDecl.setName("expression");
 	    		exprStat.setChild("expression", varDecl);
 	    		multipleNodes.add(exprStat);
 	    	}
 	    } else if ( s instanceof WhileStatement)
 	    {
 	    	node = new Node(null, "LoopStatement", name);
 	    	WhileStatement ws = (WhileStatement) s;
 	    	
 	    	if (ws.getExpression() != null)
 	    		node.add(expression(ws.getExpression(),"condition"));
 	    	
 	    	visitStatementBody(ws.getBody(), node, "body");
 	    } else
 	    {
 	    	throw new UnknownFeatureException("Unknown statement type: " + s.getClass().getSimpleName());
 	    }
 		
 		if (multipleNodes == null)
 		{
 			multipleNodes = new LinkedList<Node>();
 			multipleNodes.add(node);
 		}
 		return multipleNodes;
 	}
 	
 	public Node addExtraDimensions(Node type, int extraDimensions) throws ConversionException
 	{
 		String name = type.name();
 		for(int i = 0; i<extraDimensions; ++i)
 		{
 			Node extraDim = new Node(null, "ArrayTypeExpression", name);
 			extraDim.setChild("typeExpression", type);
 			type = extraDim;
 		}
 		return type;
 	}
 	
 	public void processTypeParameters(List<TypeParameter> params) throws ConversionException
 	{
 		int i = 0;
 		for(TypeParameter tp : params)
 		{
 			Node fta = containers.peek().child("typeArguments").add(new Node(null,"FormalTypeArgument", i++));
 			fta.setSymbol(tp.getName().getIdentifier());
 			// TODO: Implement support for more than one bound
 			if (!tp.typeBounds().isEmpty())
 			{
 				fta.add(typeExpression((Type)tp.typeBounds().get(0), "subTypeOfExpression"));
 			}
 		}
 	}
 	
 	public void processParameters(List<SingleVariableDeclaration> params) throws ConversionException
 	{
 		int i = 0;
 		for(SingleVariableDeclaration arg : params)
 		{
 			Node a = containers.peek().child("arguments").add(new Node(null,"FormalArgument", i++));
 			a.setSymbol(arg.getName().getIdentifier());
 			
 			Node type = typeExpression(arg.getType(), "typeExpression");
 			a.setChild("typeExpression", addExtraDimensions(type,arg.getExtraDimensions()));
 			
 			// TODO: Implement support for modifies and annotations
 			// TODO: Implement support for variable method arity
 			// TODO: Implement support for initializers
 		}
 	}
 
 	void setModifiers(BodyDeclaration body) throws ConversionException
 	{
 		Node n = containers.peek();	
 
 		int modifiers = body.getModifiers();
 		int modifiersToSet = 0;
 		
 		if ((modifiers & Modifier.PUBLIC) != 0) modifiersToSet |= 0x00000001;
 		if ((modifiers & Modifier.PRIVATE) != 0) modifiersToSet |= 0x00000002;
 		if ((modifiers & Modifier.PROTECTED) != 0)  modifiersToSet |= 0x00000004;
 		if ((modifiers & Modifier.STATIC) != 0)  modifiersToSet |= 0x00000008;
 
 		// TODO: Handle other modifiers
 		// abstract
 		// final
 		// native
 		// synchronized
 		// transient
 		// volatile
 		// strictfp
 		
 		n.child("modifiers").setLongValue(modifiersToSet);
 	}
 	
 	Node typeExpression(Type type, String name) throws ConversionException
 	{
 		Node t = null;
 		
 		if (type.isPrimitiveType())
 		{
 			t = new Node(null, "PrimitiveTypeExpression", name);
 			//TODO: Have more appropriate mappings for byte and short
 			PrimitiveType pt = (PrimitiveType) type;
 			if ( pt.getPrimitiveTypeCode() == PrimitiveType.BYTE) t.child("val").setLongValue(0);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.SHORT) t.child("val").setLongValue(0);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.CHAR) t.child("val").setLongValue(7);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.INT) t.child("val").setLongValue(0);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.LONG) t.child("val").setLongValue(1);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.FLOAT) t.child("val").setLongValue(4);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.DOUBLE) t.child("val").setLongValue(5);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) t.child("val").setLongValue(6);
 			else if ( pt.getPrimitiveTypeCode() == PrimitiveType.VOID) t.child("val").setLongValue(8);
 		}
 		else if (type.isSimpleType())
 		{
 			t = new Node(null, "ClassTypeExpression", name);
 			t.setChild("typeExpression", expression(((SimpleType)type).getName(), "typeExpression"));
 		}
 		else if (type.isArrayType())
 		{
 			t = new Node(null, "ArrayTypeExpression", name);
 			t.setChild("typeExpression", typeExpression(((ArrayType)type).getComponentType(), "typeExpression"));
 		} else if (type.isQualifiedType())
 		{
 			t = new Node(null, "ClassTypeExpression", name);
 			Node prefix = typeExpression(((QualifiedType)type).getQualifier(), "prefix");
 			
 			t.child("typeExpression").add(prefix);
 			t.child("typeExpression").setChild("ref", expression(((QualifiedType)type).getName(), "ref"));
 		} else if (type.isParameterizedType())
 		{
 			t = typeExpression(((ParameterizedType)type).getType(), name);
 			if (!t.tag().equals("ClassTypeExpression"))
 				throw new ConversionException("Invaid parametric base type" + t.tag());
 			
 			int i = 0;
 			for(Type arg : (List<Type>)((ParameterizedType)type).typeArguments())
 				t.child("typeExpression").child("typeArguments").add(typeExpression(arg, Integer.toString(i++)));
 		} else if (type.isWildcardType())
 		{
 			// TODO: Implement this
 			//  ? [ ( extends | super) Type ]
 			t = new Node(null, "EmptyExpression", name);
 		} else if (type.isUnionType())
 		{
 			// TODO: Implement this
 			//  Type | Type { | Type }
 			t = new Node(null, "EmptyExpression", name);
 		}
 		else
 		{
 			throw new UnknownFeatureException("Unknown type");
 		}
 		
 		return t;
 	}
 	
 	Node combineExpressionsWithComma(List<Expression> list, String name) throws ConversionException
 	{
 		if (list.size() == 0)
 			return new Node(null, "EmptyExpression", name);
 		
 		if (list.size() == 1)
 		{
 			return expression(list.get(0), name);
 		}
 		
 		Node comma = new Node(null, "CommaExpression", name);
 		comma.setChild("left", expression(list.get(0),"left"));
 		comma.setChild("right", expression(list.get(1),"right"));
 		Node previousComma = comma;
 		for(int i = 2; i<list.size(); ++i)
 		{
 			Node innerComma = new Node(null, "CommaExpression", "right");
 			
 			Node middle = previousComma.child("right");
 			previousComma.setChild("right", innerComma);
 			innerComma.setChild("left", middle);
 			innerComma.setChild("right", expression(list.get(i),"right"));
 			previousComma = innerComma;
 		}
 		
 		return comma;
 	}
 	
 	Node combineNodesWithComma(List<Node> list, String name) throws ConversionException
 	{
 		if (list.size() == 0)
 			return new Node(null, "EmptyExpression", name);
 		
 		if (list.size() == 1)
 		{
 			list.get(0).setName(name);
 			return list.get(0);
 		}
 		
 		Node comma = new Node(null, "CommaExpression", name);
 		comma.setChild("left", list.get(0));
 		comma.setChild("right", list.get(1));
 		Node previousComma = comma;
 		for(int i = 2; i<list.size(); ++i)
 		{
 			Node innerComma = new Node(null, "CommaExpression", "right");
 			Node previousRightChild = previousComma.child("right");
 			previousComma.setChild("right", innerComma);
 			innerComma.setChild("left", previousRightChild);
 			innerComma.setChild("right", list.get(i));
 			previousComma = innerComma;
 		}
 		
 		return comma;
 	}
 	
 	Node expression(Expression e, String name) throws ConversionException
 	{
 		Node node = null;
 		
 		if (e instanceof Annotation) node = new Node(null, "EmptyExpression", name); //TODO: Implement this
 		else if (e instanceof ArrayAccess)
 		{
 			ArrayAccess aa = (ArrayAccess) e;
 			node = new Node(null, "BinaryOperation", name);
 			node.child("opr").setLongValue(19);
 			node.setChild("left", expression(aa.getArray(), "left"));
 			node.setChild("right", expression(aa.getIndex(), "right"));
 		} else if (e instanceof ArrayCreation)
 		{
 			//TODO: implement multi-dimensional arrays. The array type is supported already but creation is not.
 			ArrayCreation ac = (ArrayCreation) e;
 			node = new Node(null, "NewExpression", name);
 			
 			node.setChild("newType", typeExpression(ac.getType().getComponentType(),"newType"));
 			
 			//TODO: Make sure that an array with an unspecified dimension is properly interpreted by Envision
 			if (ac.dimensions().isEmpty() || ac.dimensions().get(0) == null)
 				node.add(new Node(null, "EmptyExpression", "amount"));
 			else node.add(expression((Expression)ac.dimensions().get(0),"amount"));
 			
 			//TODO: Handle the initializer
 		} else if (e instanceof ArrayInitializer)
 		{
 			ArrayInitializer ai = (ArrayInitializer) e;
 			node = new Node(null, "ArrayInitializer", name);
 			
 			for(Expression aiExp : (List<Expression>) ai.expressions())
 			{
 				node.child("values").add(expression(aiExp, Integer.toString(node.child("values").numChildren())));
 			}
 		} else if (e instanceof Assignment)
 		{
 			Assignment a = (Assignment) e;
 			node = new Node(null, "AssignmentExpression", name);
 			node.setChild("left", expression(a.getLeftHandSide(), "left"));
 			node.setChild("right", expression(a.getRightHandSide(), "right"));
 			if(a.getOperator() == Assignment.Operator.ASSIGN)
 				node.child("opr").setLongValue(0);
 			else if(a.getOperator() == Assignment.Operator.BIT_AND_ASSIGN )
 				node.child("opr").setLongValue(5);
 			else if(a.getOperator() == Assignment.Operator.BIT_OR_ASSIGN )
 				node.child("opr").setLongValue(6);
 			else if(a.getOperator() == Assignment.Operator.BIT_XOR_ASSIGN )
 				node.child("opr").setLongValue(7);
 			else if(a.getOperator() == Assignment.Operator.DIVIDE_ASSIGN )
 				node.child("opr").setLongValue(4);
 			else if(a.getOperator() == Assignment.Operator.LEFT_SHIFT_ASSIGN )
 				node.child("opr").setLongValue(9);
 			else if(a.getOperator() == Assignment.Operator.MINUS_ASSIGN )
 				node.child("opr").setLongValue(2);
 			else if(a.getOperator() == Assignment.Operator.PLUS_ASSIGN )
 				node.child("opr").setLongValue(1);
 			else if(a.getOperator() == Assignment.Operator.REMAINDER_ASSIGN )
 				node.child("opr").setLongValue(8);
 			else if(a.getOperator() == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN )
 				node.child("opr").setLongValue(10);
 			else if(a.getOperator() == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN )
 				node.child("opr").setLongValue(11);
 			else if(a.getOperator() == Assignment.Operator.TIMES_ASSIGN )
 				node.child("opr").setLongValue(3);
 			else
 				throw new UnknownFeatureException("Unknown assignment operator: "
 							+ a.getOperator().getClass().getName());
 		} else if (e instanceof BooleanLiteral)
 		{
 			BooleanLiteral bl = (BooleanLiteral) e;
 			node = new Node(null, "BooleanLiteral", name);
 			node.child("value").setLongValue(bl.booleanValue()?1:0);
 		} else if (e instanceof CastExpression)
 		{
 			CastExpression ce = (CastExpression) e;
 			node = new Node(null, "CastExpression", name);
 			node.setChild("castType", typeExpression(ce.getType(),"castType"));
 			node.setChild("expr", expression(ce.getExpression(),"expr"));
 		} else if (e instanceof CharacterLiteral)
 		{
 			CharacterLiteral cl = (CharacterLiteral) e;
 			node = new Node(null, "CharacterLiteral", name);
 			node.child("value").setStringValue(Character.toString(cl.charValue()));
 		} else if (e instanceof ClassInstanceCreation)
 		{
 			ClassInstanceCreation cic = (ClassInstanceCreation) e;
 			node = new Node(null, "NewExpression", name);
 			//TODO: support qualified new ( .new)
 			//TODO: support anonymous class creation
 			
 			//TODO: support constructor arguments
 			node.setChild("newType", typeExpression(cic.getType(), "newType"));
 		} else if (e instanceof ConditionalExpression)
 		{
 			ConditionalExpression ce = (ConditionalExpression) e;
 			node = new Node(null, "ConditionalExpression", name);
 			
 			node.setChild("condition", expression(ce.getExpression(), "condition"));
 			node.setChild("trueExpression", expression(ce.getThenExpression(), "trueExpression"));
 			node.setChild("falseExpression", expression(ce.getElseExpression(), "falseExpression"));
 		} else if (e instanceof FieldAccess)
 		{
 			FieldAccess fa = (FieldAccess) e;
 			node = new Node(null, "ReferenceExpression", name);
 			if (fa.getExpression() != null)
 				node.add(expression(fa.getExpression(),"prefix"));
 			node.child("ref").setStringValue("____NULL____:" + fa.getName().getIdentifier());
 		} else if (e instanceof InfixExpression)
 		{
 			InfixExpression ie = (InfixExpression) e;
 			node = new Node(null, "BinaryOperation", name);
 			
 			int operator = 0;
 			if (ie.getOperator() == InfixExpression.Operator.AND ) operator = 15;
 			else if (ie.getOperator() == InfixExpression.Operator.CONDITIONAL_AND ) operator = 17;
 			else if (ie.getOperator() == InfixExpression.Operator.CONDITIONAL_OR ) operator = 18;
 			else if (ie.getOperator() == InfixExpression.Operator.DIVIDE ) operator = 1;
 			else if (ie.getOperator() == InfixExpression.Operator.EQUALS ) operator = 12;
 			else if (ie.getOperator() == InfixExpression.Operator.GREATER ) operator = 9;
 			else if (ie.getOperator() == InfixExpression.Operator.GREATER_EQUALS ) operator = 11;
 			else if (ie.getOperator() == InfixExpression.Operator.LEFT_SHIFT ) operator = 5;
 			else if (ie.getOperator() == InfixExpression.Operator.LESS  ) operator = 8;
 			else if (ie.getOperator() == InfixExpression.Operator.LESS_EQUALS ) operator = 10;
 			else if (ie.getOperator() == InfixExpression.Operator.MINUS ) operator = 4;
 			else if (ie.getOperator() == InfixExpression.Operator.NOT_EQUALS ) operator = 13;
 			else if (ie.getOperator() == InfixExpression.Operator.OR ) operator = 16;
 			else if (ie.getOperator() == InfixExpression.Operator.PLUS ) operator = 3;
 			else if (ie.getOperator() == InfixExpression.Operator.REMAINDER ) operator = 2;
 			else if (ie.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_SIGNED ) operator = 6;
 			else if (ie.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED ) operator = 7;
 			else if (ie.getOperator() == InfixExpression.Operator.TIMES ) operator = 0;
 			else if (ie.getOperator() == InfixExpression.Operator.XOR ) operator = 14;
 			else throw new UnknownFeatureException("Unknown infix operator: " + ie.getOperator().getClass().getName());
 			
 			node.setChild("left", expression(ie.getLeftOperand(),"left"));
 			node.child("opr").setLongValue(operator);
 			
 			Node activeNode = node;
 			Node lastOperand = expression(ie.getRightOperand(),"right");
 			for(Expression ext : (List<Expression>) ie.extendedOperands())
 			{
 				activeNode = activeNode.setChild("right", new Node(null, "BinaryOperation", "right"));
 				activeNode.child("opr").setLongValue(operator);
 				lastOperand.setName("left");
 				activeNode.setChild("left", lastOperand);
 				lastOperand = expression(ext,"right");
 			}
 			
 			activeNode.setChild("right", lastOperand);
 		} else if (e instanceof InstanceofExpression)
 			node = new Node(null, "EmptyExpression", name); //TODO: Implement this
 		else if (e instanceof MethodInvocation)
 		{
 			MethodInvocation ie = (MethodInvocation) e;
 			node = new Node(null, "MethodCallExpression", name);
 			Node refNode = new Node(null, "ReferenceExpression", "callee");
 			node.setChild("callee", refNode);
 			
 			if (ie.getExpression() != null)
 				refNode.add(expression(ie.getExpression(), "prefix"));
 			refNode.child("ref").setStringValue("____NULL____:" + ie.getName().getIdentifier());
 			
 			for (Type ta : (List<Type>) ie.typeArguments())
 				refNode.child("typeArguments").add(
 						typeExpression(ta, Integer.toString(refNode.child("typeArguments").numChildren())));
 			
 			for (Expression arg : (List<Expression>) ie.arguments())
 				node.child("arguments").add(expression(arg, Integer.toString(node.child("arguments").numChildren())));
 		} else if (e instanceof Name)
 		{
 			Node prefix = null;
 			for (String s : ((Name) e).getFullyQualifiedName().split("\\.") )
 			{
 				prefix = new Node(null, "ReferenceExpression", "prefix");
 				prefix.child("ref").setStringValue("____NULL____:" + s);
 				if (node == null) node = prefix;
 				else
 				{
 					prefix.add(node);
 					node = prefix;
 				}
 			}
 			node.setName( name );
 		} else if (e instanceof NullLiteral)
 		{
 			node = new Node(null, "NullLiteral", name);
 		} else if (e instanceof NumberLiteral)
 		{
 			String num = ((NumberLiteral) e).getToken();
 			
 			//TODO: Verify that the correct node type is always created
 			if (!num.contains("x") && !num.contains("X")
 					&& ((num.contains(".") || num.contains("e") || num.contains("E"))
 						|| num.endsWith("f") || num.endsWith("F") || num.endsWith("d") ))
 			{
 				if (num.endsWith("f") || num.endsWith("F") || num.endsWith("d") ) num = num.substring(0,num.length()-1);
 				node = new Node(null, "FloatLiteral", name);
 				node.child("value").setDoubleValue(Double.parseDouble(num));
 			}
 			else
 			{
 				long decoded = 0;
 				if (num.equals("0x8000000000000000L")) decoded = 0x8000000000000000L;
 				else
 				{
 					if (num.endsWith("L") || num.endsWith("l")) num = num.substring(0,num.length()-1);
 					
					boolean hex = false;
					if (num.startsWith("0x") || num.startsWith("0X"))
					{
						hex = true;
						num = num.substring(2,num.length());
					}
					
					decoded = new BigInteger(num, hex ? 16 : 10).longValue();
 				}
 				node = new Node(null, "IntegerLiteral", name);
 				node.child("value").setLongValue(decoded);
 			}
 		} else if (e instanceof ParenthesizedExpression)
 		{
 			ParenthesizedExpression pe = (ParenthesizedExpression) e;
 			node = new Node(null, "UnaryOperation", name);
 			node.child("opr").setLongValue(8);
 			node.setChild("operand", expression(pe.getExpression(),"operand"));
 		} else if (e instanceof PostfixExpression)
 		{
 			PostfixExpression pe = (PostfixExpression) e;
 			node = new Node(null, "UnaryOperation", name);
 			if (pe.getOperator() == PostfixExpression.Operator.INCREMENT)
 				node.child("opr").setLongValue(2);
 			else if (pe.getOperator() == PostfixExpression.Operator.DECREMENT)
 				node.child("opr").setLongValue(3);
 			else
 				throw new UnknownFeatureException("Unknown postfix operator: " + pe.getOperator().getClass().getName());
 			node.setChild("operand", expression(pe.getOperand(),"operand"));
 		} else if (e instanceof PrefixExpression)
 		{
 			PrefixExpression pe = (PrefixExpression) e;
 			node = new Node(null, "UnaryOperation", name);
 			if (pe.getOperator() == PrefixExpression.Operator.COMPLEMENT )
 				node.child("opr").setLongValue(7);
 			else if (pe.getOperator() == PrefixExpression.Operator.DECREMENT )
 				node.child("opr").setLongValue(1);
 			else if (pe.getOperator() == PrefixExpression.Operator.INCREMENT )
 				node.child("opr").setLongValue(0);
 			else if (pe.getOperator() == PrefixExpression.Operator.MINUS )
 				node.child("opr").setLongValue(5);
 			else if (pe.getOperator() == PrefixExpression.Operator.NOT )
 				node.child("opr").setLongValue(6);
 			else if (pe.getOperator() == PrefixExpression.Operator.PLUS )
 				node.child("opr").setLongValue(4);
 			else
 				throw new UnknownFeatureException("Unknown prefix operator: " + pe.getOperator().getClass().getName());
 			node.setChild("operand", expression(pe.getOperand(),"operand"));
 		} else if (e instanceof StringLiteral)
 		{
 			StringLiteral sl = (StringLiteral) e;
 			node = new Node(null, "StringLiteral", name);
 			node.child("value").setStringValue(sl.getLiteralValue());
 		} else if (e instanceof SuperFieldAccess)
 			node = new Node(null, "EmptyExpression", name); //TODO: Implement this
 		else if (e instanceof SuperMethodInvocation)
 			node = new Node(null, "EmptyExpression", name); //TODO: Implement this
 		else if (e instanceof ThisExpression)
 			node = new Node(null, "ThisExpression", name);
 		else if (e instanceof TypeLiteral)
 			node = new Node(null, "EmptyExpression", name); //TODO: Implement this
 		else if (e instanceof VariableDeclarationExpression)
 		{
 			VariableDeclarationExpression vde = (VariableDeclarationExpression) e;
 			List<Node> varDecls = variableDeclarationExpressions(name, vde.getType(), vde.fragments());
 			node = combineNodesWithComma(varDecls, name);
 			
 		} else throw new UnknownFeatureException("Unknown expression type: " + e.getClass().getSimpleName());
 		
 		return node;
 	}
 	
 	List<Node> variableDeclarationExpressions(String name, Type type, List<VariableDeclarationFragment> fragments)
 			throws ConversionException
 	{
 		List<Node> variableDeclarations =  new LinkedList<Node>();
     	for(VariableDeclarationFragment vdf : fragments)
 		{
     		// It's ok to reuse the same name for all generated variables, since this will be fixed later.    		
 			Node varDeclExpression = new Node(null, "VariableDeclarationExpression", name);
 			Node varDecl = varDeclExpression.child("decl");
 			
 			varDecl.setSymbol(vdf.getName().getIdentifier());
 			//TODO: handle modifiers
 			Node variableType = typeExpression(type, "typeExpression");
 			varDecl.setChild("typeExpression", addExtraDimensions(variableType, vdf.getExtraDimensions()));
 			
 			if (vdf.getInitializer() != null)
 				varDecl.add(expression(vdf.getInitializer(), "initialValue"));
 			
 			variableDeclarations.add(varDeclExpression);
 		}
     	
     	return variableDeclarations;
 	}
 }
