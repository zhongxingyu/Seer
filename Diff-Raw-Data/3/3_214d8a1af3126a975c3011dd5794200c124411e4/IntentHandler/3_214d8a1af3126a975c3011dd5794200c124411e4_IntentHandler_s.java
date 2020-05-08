 package itu.dk.aamj.intentdsl.classes;
 
 import intent.Callback;
 import intent.Extra;
 import intent.Intent;
 import intent.Model;
 import intent.Permissions;
 import itu.dk.aamj.intentdsl.IntentDslStandaloneSetup;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.CatchClause;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.IfStatement;
 import org.eclipse.jdt.core.dom.InfixExpression;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.PrimitiveType;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.Statement;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jdt.core.dom.TryStatement;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.osgi.framework.Bundle;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.inject.Injector;
 
 public class IntentHandler {
 	
 	private Model model = null;
 	
 	public Model getModel() {
 		
 		if(model != null)
 			return model;
 		
 		// http://wiki.eclipse.org/Xtext/FAQ#How_do_I_load_my_model_in_a_standalone_Java_application.C2.A0.3F
 		Injector injector = new IntentDslStandaloneSetup().createInjectorAndDoEMFRegistration();
 		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
 		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
 		
 		Bundle bundle = Platform.getBundle("itu.dk.aamj.intentdsl.ui");
 		URL fileURL = bundle.getEntry("gen/i.intentdsl");
 
 		Resource resource = resourceSet.getResource(
 				URI.createURI(fileURL.toString()), true);
 		
 		model = (Model) resource.getContents().get(0);
 		return model;
 		
 	}
 	
 	public ArrayList<String> getIntentNameArray() {
 		
 		ArrayList<String> obj = new ArrayList<String>();
 		
 		for(Intent i : model.getIntents()) {
 			
 			obj.add(i.getName());
 			
 		}
 		
 		return obj;
 	}
 	
 	public int InsertIntent(Intent intent, boolean handleExceptions) throws Exception {
 		
 		// Get the current editor window, and the input 
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IEditorPart editor = page.getActiveEditor();
 		IEditorInput input = editor.getEditorInput();
 		
 		ICompilationUnit compilationUnit = (ICompilationUnit) JavaUI.getEditorInputJavaElement(editor.getEditorInput());
 		
 		// Ensure the editor is open
 		if(!(compilationUnit instanceof ICompilationUnit)) {
 			return -1;
 		}
 		
 		ManifestHandler mh = new ManifestHandler();
 		boolean success = mh.process(intent, compilationUnit);
 		if(!success) {
 			return -2;
 		}
 
 		ITextEditor texteditor = (ITextEditor) editor;
 		IDocument document = texteditor.getDocumentProvider().getDocument(input);
 		ISelection selection = texteditor.getSelectionProvider().getSelection();
 
 		//Parse as AST
 		CompilationUnit astRoot = parse(compilationUnit);
 		astRoot.recordModifications();
 		AST ast = astRoot.getAST();
 		
 		//Get the cursor position
 		int cursorOffset = ((ITextSelection) selection).getOffset();
 
 		//Find the method we're in
 		MethodDeclaration method = findMethod(cursorOffset, astRoot);
 		if(method == null) {
 			//Not inside a method, return 0 and warn user
 			return 0;
 		}
 		
 		ArrayList<Statement> statementsList = new ArrayList<Statement>();
 
 		//Generate the variable name
 		StringBuilder sb = new StringBuilder();
 		for(String s : intent.getName().split(" ")) {
 			
 			sb.append(s.substring(0, 1).toLowerCase());
 			
 		}
 		String instanceNameString = sb.toString(); // Note that using a string here isntead of newSimpleName is important as it gets overridden for some reason
 
 		//Create the class call
 		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();  
 		vdf.setName(ast.newSimpleName(instanceNameString));  
 		ClassInstanceCreation cc = ast.newClassInstanceCreation();  
 		cc.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 		
 		// Add the action name as a parameters
 		StringLiteral actionStringName = ast.newStringLiteral();
 		actionStringName.setLiteralValue(intent.getAction());
 		cc.arguments().add(actionStringName);
 		
 		vdf.setInitializer(cc);
 		VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);  
 		vds.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 				
 		//Add this call
 		statementsList.add(vds);
 		
 		SimpleName instanceName = ast.newSimpleName(instanceNameString);
 		
 		// i.setType( value )
 		String intentType = intent.getType();
 		if(intentType != null) {
 
 			StringLiteral data = ast.newStringLiteral();
 			data.setLiteralValue(intentType);
 			
 			MethodInvocation methodInv = ast.newMethodInvocation();
 			methodInv.setExpression(instanceName);
 			methodInv.setName(ast.newSimpleName("setType"));
 			methodInv.arguments().add(data);
 			
 			statementsList.add(ast.newExpressionStatement(methodInv));
 			
 		}
 		
 		// i.setData(Uri.parse( value ));
 		String intentData = intent.getData();
 		if(intentData != null) {
 			
 			StringLiteral data = ast.newStringLiteral();
 			data.setLiteralValue(intentData);
 			
 			MethodInvocation uriInv = ast.newMethodInvocation();
 			uriInv.setExpression(ast.newName("Uri"));
 			uriInv.setName(ast.newSimpleName("parse"));
 			uriInv.arguments().add(data);
 			
 			MethodInvocation dataInv = ast.newMethodInvocation();
 			dataInv.setExpression(instanceName);
 			dataInv.setName(ast.newSimpleName("setData"));
 			dataInv.arguments().add(uriInv);
 			
 			statementsList.add(ast.newExpressionStatement(dataInv));
 			
 		}
 		
 		for(Extra extra : intent.getExtras()) {
 			
 //			SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
 //			variableDeclaration.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName(extra.getType()))));
 //			variableDeclaration.setName(ast.newSimpleName(extra.getName()));
 			
 			//TODO Why does this need to go here twice!?????
 			instanceName = ast.newSimpleName(instanceNameString);
 			
 			StringLiteral value = ast.newStringLiteral();
 			value.setLiteralValue(extra.getValue());
 			
 			StringLiteral data = ast.newStringLiteral();
 			data.setLiteralValue(extra.getName());
 			
 			MethodInvocation methodInv = ast.newMethodInvocation();
 			methodInv.setExpression(instanceName);
 			methodInv.setName(ast.newSimpleName("putExtra"));
 			methodInv.arguments().add(data);
 			methodInv.arguments().add(value);
 			
 			statementsList.add(ast.newExpressionStatement(methodInv));
 			
 		}
 		
 		//TODO Again WHY?! It won't work if you take this out
 		instanceName = ast.newSimpleName(instanceNameString);
 		
 		//Start the intent
 		//TODO We need a Broadcast intent that will use "sendBroadcast"
 		Callback callback = intent.getSucessCallback();
 		if(callback != null) {
 			
 			//TODO Again WHY?! It won't work if you take this out
 			MethodInvocation startAct = ast.newMethodInvocation();
 			startAct.setName(ast.newSimpleName("startActivityForResult"));
 			startAct.arguments().add(instanceName);
 			startAct.arguments().add(ast.newSimpleName(callback.getName()));
 			statementsList.add(ast.newExpressionStatement(startAct));
 			
 			//Find the method onActivityResult
 			MethodDeclaration methodDecl = null;
 			
 			MethodVisitor visitor = new MethodVisitor();
 			astRoot.accept(visitor);
 
 			for (MethodDeclaration methodDec : visitor.getMethods()) {
 				
 				if(methodDec.getName().toString().equalsIgnoreCase("onActivityResult"))
 					methodDecl = methodDec;
 				
 			}
 			
 			if(methodDecl == null) {
 				
 				//http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_api_manip.htm
 				//Method not found, create it
 				methodDecl = ast.newMethodDeclaration();
 				methodDecl.setConstructor(false);
 				List modifiers = methodDecl.modifiers();
 				modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD));
 				methodDecl.setName(ast.newSimpleName("onActivityResult"));
 				methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
 				
 				SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
 				variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
 				variableDeclaration.setName(ast.newSimpleName("requestCode"));
 				methodDecl.parameters().add(variableDeclaration);
 				
 				variableDeclaration = ast.newSingleVariableDeclaration();
 				variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
 				variableDeclaration.setName(ast.newSimpleName("resultCode"));
 				methodDecl.parameters().add(variableDeclaration);
 				
 				variableDeclaration = ast.newSingleVariableDeclaration();
 				variableDeclaration.setType(ast.newSimpleType(ast.newSimpleName("Intent")));
 				variableDeclaration.setName(ast.newSimpleName("data"));
 				methodDecl.parameters().add(variableDeclaration);
 				
 				((TypeDeclaration)astRoot.types().get(0)).bodyDeclarations().add(methodDecl);
 				
 			}
 			
 			InfixExpression equalsName = methodDecl.getAST().newInfixExpression();
 			equalsName.setOperator(InfixExpression.Operator.EQUALS);
 			equalsName.setLeftOperand(ast.newSimpleName("requestCode"));
 			equalsName.setRightOperand(ast.newSimpleName(callback.getName()));
 			
 			IfStatement ifStatement = methodDecl.getAST().newIfStatement();
 			ifStatement.setExpression(equalsName);
 			
 			InfixExpression equalsResultOK = ifStatement.getAST().newInfixExpression();
 			equalsResultOK.setOperator(InfixExpression.Operator.EQUALS);
 			equalsResultOK.setLeftOperand(ast.newSimpleName("resultCode"));
 			equalsResultOK.setRightOperand(ast.newSimpleName("RESULT_OK"));
 			
 			IfStatement secondIfStatement = methodDecl.getAST().newIfStatement();
 			secondIfStatement.setExpression(equalsResultOK);
 			ifStatement.setThenStatement(secondIfStatement);
 			
 //			TODO Not sure how to add this comment. The docs say that comments don't work well and are not intended for client use
 //			LineComment comment = secondIfStatement.getAST().newLineComment();
 //			StringLiteral comment = secondIfStatement.getAST().newStringLiteral();
 //			comment.setLiteralValue("// Success");
 //			secondIfStatement.setThenStatement(secondIfStatement.getAST().newEmptyStatement().set);
 
 			//Add to the method
 			if(methodDecl.getBody() == null) {
 				
 				Block block = methodDecl.getAST().newBlock();
 				block.statements().add(ifStatement);
 				methodDecl.setBody(block);
 				
 			} else
 				methodDecl.getBody().statements().add(ifStatement);
 			
 		}
 		else {
 			
 			//Exception handling
 			//http://stackoverflow.com/questions/10152252/android-emulator-intent-createchooser-says-no-application-can-perform-this-ac
 			//http://svn.svnkit.com/repos/svnkit/branches/1.7-script/src/org/tmatesoft/refactoring/split/core/SplitDelegateChanges.java
 			
 			MethodInvocation startAct = ast.newMethodInvocation();
 			startAct.setName(ast.newSimpleName(
 				intent.getIntentType().toString().equalsIgnoreCase("broadcast") ? "sendBroadcast" : "startActivity"
 			));
 			startAct.arguments().add(instanceName);
 			
 			if(handleExceptions) {
 				
 				TryStatement tryStatement = ast.newTryStatement();
 				tryStatement.getBody().statements().add(ast.newExpressionStatement(startAct));
 				
 				SingleVariableDeclaration exception = ast.newSingleVariableDeclaration();
 				exception.setType(ast.newSimpleType(ast.newSimpleName("ActivityNotFoundException")));
 				exception.setName(ast.newSimpleName("e"));
 				
 				List<CatchClause> catchClauses = tryStatement.catchClauses();
 				CatchClause catchClause = ast.newCatchClause();
 				catchClause.setException(exception);
 				tryStatement.catchClauses().add(catchClause);
 				
 				statementsList.add(tryStatement);
 				
 			}
 			else
 				statementsList.add(ast.newExpressionStatement(startAct));
 			
 		}
 			
 		//Add new block to method
 		List<Statement> methodStatements = method.getBody().statements();
 		if(methodStatements.size() > 0) {
 			
 			int index = findIndexInMethod(method, cursorOffset);
 			for(int i = 0; i < statementsList.size(); i++) {
 				
 				Statement statement = (Statement) statementsList.get(i);
 				methodStatements.add(index++, statement);
 				
 			}
 			
 		}
 		else {
 			
 			for(int i = 0; i < statementsList.size(); i++) {
 				
 				Statement statement = (Statement) statementsList.get(i);
 				methodStatements.add(statement);
 				
 			}
 			
 		}
 		
 
 		//Rewrite the AST
 		TextEdit edits = astRoot.rewrite(document, compilationUnit.getJavaProject().getOptions(true));
 		compilationUnit.applyTextEdit(edits, null);
 
 		compilationUnit.getBuffer().setContents(document.get());
 
 		// Success
 		return 1;
 
 	}
 	
 	public class MethodVisitor extends ASTVisitor {
 		List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
 
 		@Override
 		public boolean visit(MethodDeclaration node) {
 			methods.add(node);
 			return super.visit(node);
 		}
 
 		public List<MethodDeclaration> getMethods() {
 			return methods;
 		}
 	}
 	
 	private CompilationUnit parse(ICompilationUnit compilationUnit) {
 		
 		ASTParser parser = ASTParser.newParser(AST.JLS4);
 		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 		parser.setSource(compilationUnit);
 		parser.setResolveBindings(true);
 		return (CompilationUnit) parser.createAST(null);
 		
 	}
 	
 	/**
 	 * Find the method which the cursor is currently in
 	 * @param offset
 	 * @param astRoot
 	 * @return MethodDeclaration
 	 */
 	private MethodDeclaration findMethod(int offset, CompilationUnit astRoot) {
 		
 		MethodVisitor visitor = new MethodVisitor();
 		astRoot.accept(visitor);
 
 		for (MethodDeclaration methodDecl : visitor.getMethods()) {
 			
 			int startRange = methodDecl.getBody().getStartPosition();
 			int endRange = startRange + methodDecl.getBody().getLength();
 			
 			if(offset >= startRange && offset <= endRange)
 				return methodDecl;
 			
 		}
 		
 		return null;
 		
 	}
 
 	/**
 	 * Return the current statement inside a method, which the cursor currently has focus
 	 * @param method
 	 * @param offset
 	 * @return int
 	 */
 	private int findIndexInMethod(MethodDeclaration method, int offset) {
 
 		List<Statement> statements = method.getBody().statements();
 		
 		if(statements.size() == 0)
 			return 0;
 		
 		for(int i = 0; i < statements.size(); i++){
 			
 			Statement statement = (Statement) statements.get(i);
 			int startRange = statement.getStartPosition();
 			int endRange = startRange + statement.getLength();
 			
 			//Is cursor at the start of the method?
 			if(offset <= startRange && i == 0)
 				return 0;
 			
 			// Is cursor inside current statement
 			if(offset >= startRange && offset <= endRange)
 				return i+1;
 			
 		}
 
 		// Insert at end
 		return statements.size();
 	}
 	
 	private class ManifestHandler {
 		
 //		http://developer.android.com/guide/topics/manifest/manifest-intro.html
 		public boolean process(Intent intent, ICompilationUnit compilationUnit) {
 			
 			if(intent.getPermissions().size() == 0)
 				return true;
 			
 			IJavaProject project = compilationUnit.getJavaProject();
 //			project.open(null);
 			
 			IProject p = project.getProject();
 			IFile rm = p.getFile("AndroidManifest.xml");
 
 			if(!rm.exists())
 				return false;
 			
 			File am = null;
 			try {
 				am = new File(rm.getLocationURI());
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 			
 			
 			if(am == null)
 				return false;
 			
 			//Load the XML
 			Document doc = null;
 			try {
 				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder builder = factory.newDocumentBuilder();
 				doc = builder.parse(am);
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 			
 			if(doc == null)
 				return false;
 			
 			Node root = doc.getFirstChild();
 			Node application = doc.getElementsByTagName("application").item(0);
 			NodeList permissions = doc.getElementsByTagName("uses-permission");
 			
 			//Check for existing permissions
 			ArrayList<String> existing = new ArrayList<String>(permissions.getLength());
 			for(int i = 0; i < permissions.getLength(); i++) {
 				
 				existing.add(permissions.item(i).getAttributes().item(0).getNodeValue());
 				
 			}
 			
 			boolean changed = false;
 			
 			for(Permissions permission : intent.getPermissions()) {
 				
 				if(!existing.contains(permission.getName())) {
 					
 					Element newPerm = doc.createElement("uses-permission");
 					newPerm.setAttribute("android:name", permission.getName());
 					if(application != null)
 						root.insertBefore(newPerm, application);
 					else
 						root.appendChild(newPerm);
 					
 					
 					changed = true;
 					
 				}
 				
 			}
 			
 			// write the content into xml file
 			if(changed) {
 				
 				try {
 					TransformerFactory transformerFactory = TransformerFactory.newInstance();
 					Transformer transformer = transformerFactory.newTransformer();
 					DOMSource source = new DOMSource(doc);
 					StreamResult result = new StreamResult(new File(rm.getLocationURI()));
 					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 					transformer.transform(source, result);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					return false;
 				}
 				
 			}
 			
 			return true;
 		}
 		
 	}
 
 }
