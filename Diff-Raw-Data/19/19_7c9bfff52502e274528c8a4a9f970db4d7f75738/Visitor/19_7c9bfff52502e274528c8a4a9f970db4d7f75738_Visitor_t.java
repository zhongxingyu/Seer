 package parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 import models.CallGraph;
 import models.Clazz;
 import models.File;
 import models.Mapping;
 import models.Method;
 
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.BooleanLiteral;
 import org.eclipse.jdt.core.dom.CastExpression;
 import org.eclipse.jdt.core.dom.CharacterLiteral;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.FieldAccess;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.IVariableBinding;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.Name;
 import org.eclipse.jdt.core.dom.NullLiteral;
 import org.eclipse.jdt.core.dom.NumberLiteral;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.TypeParameter;
 import org.eclipse.jdt.core.dom.VariableDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 
 import callgraphanalyzer.Mappings;
 
 public class Visitor extends ASTVisitor {
 	
 	private CallGraph callGraph;
 	
 	private File file;
 	private Stack<Clazz> clazzStack;
 	private Clazz currentClazz;
 	private Method currentMethod;
 	
	public Visitor(CallGraph callGraph, String fileName, int fileLength) {
 		this.callGraph = callGraph;
 		clazzStack = new Stack<Clazz>();
 		
 		file = new File();
 		file.setFileName(fileName);
		file.setStartChar(0);
		file.setEndChar(fileLength);
 	}
 	
 	/**
 	 * This function overrides what to do when the AST visitor 
 	 * encounters a package declaration
 	 */
 	@Override
 	public boolean visit(PackageDeclaration node) {
 		if (!file.getFilePackage().equals(""))
 			System.err.println("ERROR: StructuredFile.java there is more than one package, using first visited");
 		else 
 			file.setFilePackage(node.getName().getFullyQualifiedName()); 
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when the AST visitor
 	 * encounters an import declaration
 	 */
 	@Override
 	public boolean visit(ImportDeclaration node) {
 		String imp= node.getName().getFullyQualifiedName();
 		file.addFileImport(imp);
 
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when the AST visitor
 	 * encounters a type declaration (This is mostly when 
 	 * you define a class in the Java file)
 	 */
 	@Override
 	public boolean visit(TypeDeclaration node) {
 		Clazz clazz = new Clazz();
 		currentClazz = clazz;
 		clazzStack.push(clazz);
 		
 		currentClazz.setName(file.getFilePackage() + "." + node.getName().getIdentifier());
 		currentClazz.setInterface(node.isInterface());
 		
 		Type t = node.getSuperclassType();
 		if(t != null)
 			currentClazz.addUnresolvedSuperClazz(t.toString());
 		
 		List<Type> interfaces = node.superInterfaceTypes();
 		for(Type i: interfaces)
 			currentClazz.addUnresolvedInterface(i.toString());
 		
 		// Add generic parameters if needed
 		List<TypeParameter> types = node.typeParameters();
 		for(TypeParameter type: types)
 			currentClazz.addGenericType(type.toString());
		
		// Add start and end
		currentClazz.setStartChar(node.getStartPosition());
		currentClazz.setEndChar(node.getStartPosition() + node.getLength());
 	
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when we reach
 	 * the end of visiting a class. (Add the class to 
 	 * the call graph as we have finished parsing it)
 	 */
 	@Override
 	public void endVisit(TypeDeclaration node) {
 		// Add it to the current file
 		clazzStack.peek().setFile(file);
 		// Add to call graph
 		callGraph.addClazz(clazzStack.peek());
 		// Add to file
 		// Add to file - interfaces if needed
 		if(clazzStack.peek().isInterface())
 			file.addInterface(clazzStack.peek());
 		file.addClazz(clazzStack.pop());
 	}
 	
 	/**
 	 * This function overrides what to do when we reach
 	 * a method declaration inside a class.
 	 */
 	@Override
 	public boolean visit(MethodDeclaration node) {
 		// Get the method's parameter
 		List<SingleVariableDeclaration> parametersList =  node.parameters();
 		List<String> parameters = new ArrayList<String>();
 		for(SingleVariableDeclaration v: parametersList) {
 			parameters.add(v.getType().toString());
 		}
 		
 		// Get the unique name
 		String uniqueMethod = currentClazz.getName() + "." + node.getName().getIdentifier()+ "(";
 		for(String s: parameters)
 			uniqueMethod += s + ", ";
 		if(!parameters.isEmpty())
 			uniqueMethod = uniqueMethod.substring(0, uniqueMethod.length()-2);
 		uniqueMethod += ")";
 		
 		currentMethod = callGraph.containsMethod(uniqueMethod);
 		if(currentMethod == null) {
 			Method m = new Method();
 			currentMethod = m;
 		}
 		currentMethod.setName(uniqueMethod);
 		currentMethod.setClazz(currentClazz);
 		currentMethod.setstartChar(node.getStartPosition());
 		currentMethod.setendChar(node.getLength() + currentMethod.getstartChar());
 		if(node.getReturnType2() != null)
 			currentMethod.setReturnType(node.getReturnType2().toString());
 		else //constructor type
 			currentMethod.setReturnType(node.getName().getIdentifier());
 		
 		currentMethod.setNode(node);
 
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when we reach
 	 * a method invocation statement. The only thing we need to worry
 	 * about here is if the invocation is outside a method aka in 
 	 * the class variables;
 	 */
 	@Override
 	public boolean visit(MethodInvocation node) {
 		if(currentMethod == null)
 			clazzStack.peek().getInvocations().add(node);
 		
 		return super.visit(node);
 	}
 	
 	/**
 	 * This method overrides what to do when we reach
 	 * the end of a method declaration
 	 */
 	@Override
 	public void endVisit(MethodDeclaration node) {
 		
 		// TODO @bradens @jordanell handle inline class declarations
 		if (currentMethod == null) 
 			return;
 		// Add method to the call graph
 		callGraph.addMethod(currentMethod);
 		// Add method to the current class
 		clazzStack.peek().addMethod(currentMethod);
 		// Must set to null incase we have variable declarations in between
 		currentMethod = null;
 	}
 	
 	@Override
 	public boolean visit(FieldDeclaration node)
 	{
 		SimpleName varName = null;
 		for (Iterator<VariableDeclarationFragment> i = node.fragments().iterator();i.hasNext();)
 		{
 			VariableDeclarationFragment frag = (VariableDeclarationFragment)i.next();
 			varName = frag.getName();
 		}
 		// Add the field declaration to the current clazz
 		if(clazzStack.peek() != null)
 			clazzStack.peek().getVariables().add(new Mapping(node.getType().toString(), varName.getFullyQualifiedName()));
 		return super.visit(node);
 	}
 	
 	public void commitFile() {
 		callGraph.addFile(file);
 	}
 }
