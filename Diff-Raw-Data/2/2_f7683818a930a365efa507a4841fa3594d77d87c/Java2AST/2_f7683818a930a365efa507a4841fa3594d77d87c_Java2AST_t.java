 package superintents.util;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.eclipse.jdt.core.dom.*;
 import org.eclipse.jdt.core.dom.InfixExpression.Operator;
 import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
 
 import superintents.util.ASTNodeWrapper.NodeType;
 import intentmodel.impl.*;
 
 public class Java2AST {
 	
 	private static String intentName;
 	private static String requestCodeName;
 	private static int requestCodeValue;
 
 	public static SuperIntentImpl createTestSI() {
 		SuperIntentImpl bigRedButtonIntent = new SuperIntentImpl();
 		bigRedButtonIntent.setDescription("THIS IS AN INTENT DESCRIPTION");
 
 		DataImpl bigRedButtonOutput = new DataImpl();
 		bigRedButtonOutput.setMIMEType("THIS IS A MIME TYPE");
 		bigRedButtonOutput.setValue("THISISAVALUE");
 		
 		bigRedButtonIntent.setOutput(bigRedButtonOutput);
 		
 		IntentImpl newIntent = new IntentImpl();
 		newIntent.setAction("THIS IS AN ACTION");
 		
 		newIntent.getCategories().add("CATEGORY1");
 		newIntent.getCategories().add("CATEGORY2");
 		
 		newIntent.setComponent("String");
 		
 		DataImpl data = new DataImpl();
 		data.setValue("THISISDATA");
 		data.setMIMEType(".mp3");
 		newIntent.setData(data);
 		
 		bigRedButtonIntent.setIntent(newIntent);
 		
 		bigRedButtonIntent.getIntent().getExtras().put("SECOND URL", "YOUR TEXT HERE");
 		bigRedButtonIntent.getIntent().getExtras().put("THIRD URL", "YOUR TEXT HERE");
 		
 		return bigRedButtonIntent;
 	}
 	
 	
 	public static ArrayList<ASTNodeWrapper> transformSuperIntent(SuperIntentImpl si)
 	{
 		CompilationUnit cu = JDTHelper.getASTTupleHelper().compilationUnit;
 		
 		//Get at valid intent name
 		intentName = "i";
 		int nameCounter = 1;
 		while (doesVariableNameExist(cu, intentName)) {
 			intentName = intentName + nameCounter;
 			nameCounter++;
 		}	
 		
 		//result list
 		ArrayList<ASTNodeWrapper> resultList = new ArrayList<ASTNodeWrapper>();
 		
 		//AST for generating nodes
 		AST ast = AST.newAST(AST.JLS4);
 		
 		//Add imports
 		if(!doesImportExist(cu,"android.content.Intent"))
 			resultList.add(new ASTNodeWrapper(generateImports(si, ast),NodeType.IMPORT));
 		
 		//Insert Input and OutPut Comments
 		if(si.getDescription() != null)
 			resultList.add(newCommentInsideMethod("Description: \n// " + si.getDescription()));
 		if(si.getOutput() != null)
 			resultList.add(newCommentInsideMethod("Output: \n// " + si.getOutput()));
 		
 		//Initialize the Intent
 		resultList.add(new ASTNodeWrapper(initializeIntent(si, ast)));
 		
 		//Set the data type
		if(si.getIntent().getData() != null && si.getIntent().getData().getMIMEType() != null) {
 			resultList.add(new ASTNodeWrapper(setType(si, ast)));
 		}
 		
 		//Set Categories
 		for (String category : si.getIntent().getCategories()) {
 			resultList.add(new ASTNodeWrapper(setCategory(category, ast)));
 		}
 		
 		//Set Extras
 		for (String extra : si.getIntent().getExtras().keySet()) {
 			resultList.add(new ASTNodeWrapper(setExtra(extra, si.getIntent().getExtras().get(extra), ast)));
 		}
 		
 		//Add callback method
 		if((si.getOutput() != null) )
 		{
 			Random random = new Random();
 			requestCodeValue = random.nextInt(10000000);
 			requestCodeName = "REQUEST_CODE_" + intentName.toUpperCase();
 			//check for existing duplicate variables
 			int requestNameCounter = 1;
 			while (doesVariableNameExist(cu, requestCodeName)) {
 				requestCodeName = requestCodeName + requestNameCounter;
 				requestNameCounter++;
 			}		
 			
 			resultList.add(new ASTNodeWrapper(generateRequestCode(ast),NodeType.FIELD));
 			resultList.add(new ASTNodeWrapper(callStartActivity(ast)));
 			
 			MethodDeclaration md = doesMethodExist(cu, "OnActivityResult");
 			if (md == null)
 				resultList.add(new ASTNodeWrapper(generateCallbackMethod(ast),NodeType.CALLBACK_METHOD));
 			else 
 				resultList.add(new ASTNodeWrapper(generateCallbackMethodBody(ast), NodeType.CALLBACK_METHOD, md));
 		}
 			
 		return resultList;
 	}
 
 	private static ASTNode generateImports(SuperIntentImpl si, AST ast) {
 		
 		//insert the import android.content.Intent;
 		ImportDeclaration i = ast.newImportDeclaration();
 		
 		QualifiedName q1 = ast.newQualifiedName(ast.newSimpleName("android"), ast.newSimpleName("content"));
 		QualifiedName q2 = ast.newQualifiedName(q1, ast.newSimpleName("Intent"));
 		
 		i.setName(q2);
 		
 		return i;
 	}
 
 	private static ASTNode initializeIntent(SuperIntentImpl si, AST ast)
 	{
 		//Intent(String action, Uri uri, Context packageContext, Class<?> cls)
 		if(si.getIntent().getAction() != null && si.getIntent().getData() != null && si.getIntent().getComponent() != null)
 			return InitializeConstructor1(si, ast);
 		//Intent(Context packageContext, Class<?> cls)
 		else if(si.getIntent().getComponent() != null) 
 			return InitializeConstructor2(si, ast);
 		//Intent(String action, Uri uri)
 		else if(si.getIntent().getAction() != null && si.getIntent().getData() != null)
 			return InitializeConstructor3(si, ast);
 		//Intent(String action)
 		else if(si.getIntent().getAction() != null)
 			return InitializeConstructor4(si, ast);
 		//Intent(Intent o) is not supported
 		//Intent()
 		else 
 			InitializeConstructor5(si, ast);
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor1(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		StringLiteral arg1 = ast.newStringLiteral();
 		arg1.setLiteralValue(si.getIntent().getAction());
 		cic.arguments().add(arg1);
 		
 		StringLiteral arg2 = ast.newStringLiteral();
 		arg2.setLiteralValue(si.getIntent().getData().getValue());
 		cic.arguments().add(arg2);
 		
 		TypeLiteral arg3 = ast.newTypeLiteral();
 		arg3.setType(ast.newSimpleType(ast.newSimpleName(si.getIntent().getComponent())));
 		cic.arguments().add(arg3);
 		
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor2(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		TypeLiteral arg1 = ast.newTypeLiteral();
 		arg1.setType(ast.newSimpleType(ast.newSimpleName(si.getIntent().getComponent())));
 		cic.arguments().add(arg1);
 		
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor3(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		StringLiteral arg1 = ast.newStringLiteral();
 		arg1.setLiteralValue(si.getIntent().getAction());
 		cic.arguments().add(arg1);
 
 		StringLiteral arg2 = ast.newStringLiteral();
 		arg2.setLiteralValue(si.getIntent().getData().getValue());
 		cic.arguments().add(arg2);
 
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode InitializeConstructor4(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		//set arguments
 		StringLiteral arg1 = ast.newStringLiteral();
 		arg1.setLiteralValue(si.getIntent().getAction());
 		cic.arguments().add(arg1);
 
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	private static ASTNode InitializeConstructor5(SuperIntentImpl si, AST ast)
 	{
 		//set the name of the variable
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
 		vdf.setName(ast.newSimpleName(intentName));
 		
 		//set the class of the instance 
 		ClassInstanceCreation cic = ast.newClassInstanceCreation();
 		cic.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 
 		vdf.setInitializer(cic);
 		
 		//set the type of the variable
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		f.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode setType(SuperIntentImpl si, AST ast)
 	{
 		//set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(intentName));
 		mi.setName(ast.newSimpleName("setType"));
 		
 		//set argument
 		StringLiteral sl = ast.newStringLiteral();
 		sl.setLiteralValue(si.getIntent().getData().getMIMEType());
 		mi.arguments().add(sl);
 		
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 		
 		return es;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode setCategory(String category, AST ast) {
 		// set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(intentName));
 		mi.setName(ast.newSimpleName("setCategory"));
 
 		// set argument
 		StringLiteral sl = ast.newStringLiteral();
 		sl.setLiteralValue(category);
 		mi.arguments().add(sl);
 
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 
 		return es;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode setExtra(String extra, String value, AST ast) {
 		// set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setExpression(ast.newSimpleName(intentName));
 		mi.setName(ast.newSimpleName("putExtra"));
 
 		// set argument
 		StringLiteral sl1 = ast.newStringLiteral();
 		sl1.setLiteralValue(extra);
 		mi.arguments().add(sl1);
 		
 		StringLiteral sl2 = ast.newStringLiteral();
 		sl2.setLiteralValue(value);
 		mi.arguments().add(sl2);
 
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 
 		return es;
 	}
 	
 	private static ASTNodeWrapper newCommentInsideMethod(String comment) {
 		return new ASTNodeWrapper(comment);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode callStartActivity(AST ast) {
 		// set invocation method name
 		MethodInvocation mi = ast.newMethodInvocation();
 		mi.setName(ast.newSimpleName("startActivityForResult"));
 
 		// set argument 1
 		StringLiteral sl1 = ast.newStringLiteral();
 		sl1.setLiteralValue(intentName);
 		mi.arguments().add(sl1);
 		
 		// set argument 2
 		SimpleName sl2 = ast.newSimpleName(requestCodeName);
 		mi.arguments().add(sl2);
 
 		ExpressionStatement es = ast.newExpressionStatement(mi);
 
 		return es;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static ASTNode generateRequestCode(AST ast) {
 		VariableDeclarationFragment vdf =  ast.newVariableDeclarationFragment();
 		//set var name
 		vdf.setName(ast.newSimpleName(requestCodeName));
 	
 		//set value
 		vdf.setInitializer(ast.newNumberLiteral(requestCodeValue + ""));
 		
 		FieldDeclaration f = ast.newFieldDeclaration(vdf);
 		
 		//set final modifer
 		Modifier mo = ast.newModifier(ModifierKeyword.FINAL_KEYWORD);
 		f.modifiers().add(mo);
 		
 		//set int type
 		f.setType(ast.newPrimitiveType(PrimitiveType.INT));
 
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ASTNode generateCallbackMethod(AST ast) {
 		//method declaration
 		MethodDeclaration m = ast.newMethodDeclaration();
 		m.setName(ast.newSimpleName("OnActivityResult"));
 		
 		//add the @Override annotation
 		MarkerAnnotation ma = ast.newMarkerAnnotation();
 		ma.setTypeName(ast.newSimpleName("Override"));
 		m.modifiers().add(ma);
 		
 		//add the "public" keyword
 		Modifier mo = ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD);
 		m.modifiers().add(mo);
 		
 		//parameters
 		SingleVariableDeclaration svd1 = ast.newSingleVariableDeclaration();
 		svd1.setType(ast.newPrimitiveType(PrimitiveType.INT));
 		svd1.setName(ast.newSimpleName("requestCode"));
 		m.parameters().add(svd1);
 		
 		SingleVariableDeclaration svd2 = ast.newSingleVariableDeclaration();
 		svd2.setType(ast.newPrimitiveType(PrimitiveType.INT));
 		svd2.setName(ast.newSimpleName("resultCode"));
 		m.parameters().add(svd2);
 		
 		SingleVariableDeclaration svd3 = ast.newSingleVariableDeclaration();
 		svd3.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		svd3.setName(ast.newSimpleName("data"));
 		m.parameters().add(svd3);
 		
 		//Generate the internal if statement
 		m.setBody(ast.newBlock());
 		m.getBody().statements().add(generateCallbackMethodBody(ast));
 		
 		return m;
 	}
 	
 	private static ASTNode generateCallbackMethodBody(AST ast)
 	{	
 		IfStatement is = ast.newIfStatement();
 		InfixExpression ie1 = ast.newInfixExpression();
 		
 		InfixExpression leftIe = ast.newInfixExpression();
 		leftIe.setLeftOperand(ast.newSimpleName("resultCode"));
 		leftIe.setOperator(Operator.EQUALS);
 		leftIe.setRightOperand(ast.newSimpleName("RESULT_OK"));
 		ie1.setLeftOperand(leftIe);
 		
 		InfixExpression RightIe = ast.newInfixExpression();
 		RightIe.setLeftOperand(ast.newSimpleName("requestCode"));
 		RightIe.setOperator(Operator.EQUALS);
 		RightIe.setRightOperand(ast.newSimpleName(requestCodeName));
 		ie1.setRightOperand(RightIe);
 		
 		ie1.setOperator(Operator.CONDITIONAL_AND);
 		
 		is.setExpression(ie1);
 		
 		return is;
 	}
 	
 	private static boolean doesImportExist(CompilationUnit cu, String name)
 	{
 		ImportASTVisitor astv = new ImportASTVisitor(name);
 		
 		cu.accept(astv);
 		
 		return astv.getExists();
 	}
 	
 	//returns null if no method is found
 	public static MethodDeclaration doesMethodExist(CompilationUnit cu, String name)
 	{
 		MethodASTVisitor astv = new MethodASTVisitor(name);
 		
 		cu.accept(astv);
 		
 		return astv.getMethod();
 	}
 	
 	//returns null if no method is found
 	private static boolean doesVariableNameExist(CompilationUnit cu, String name)
 	{
 		VariableNameASTVisitor astv = new VariableNameASTVisitor(name);
 		
 		cu.accept(astv);
 		
 		return astv.getExists();
 	}
 }
 	
