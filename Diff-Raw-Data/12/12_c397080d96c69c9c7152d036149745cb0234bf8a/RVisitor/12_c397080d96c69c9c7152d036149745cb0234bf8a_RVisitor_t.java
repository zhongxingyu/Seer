 package parser;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import models.CallGraph;
 import models.Clazz;
 import models.Mapping;
 import models.Method;
 
import org.eclipse.jdt.core.dom.Assignment;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.BooleanLiteral;
 import org.eclipse.jdt.core.dom.CastExpression;
 import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.InfixExpression;
 import org.eclipse.jdt.core.dom.InstanceofExpression;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.Name;
 import org.eclipse.jdt.core.dom.NullLiteral;
 import org.eclipse.jdt.core.dom.NumberLiteral;
 import org.eclipse.jdt.core.dom.ParenthesizedExpression;
 import org.eclipse.jdt.core.dom.PostfixExpression;
 import org.eclipse.jdt.core.dom.PrefixExpression;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jdt.core.dom.SuperFieldAccess;
 import org.eclipse.jdt.core.dom.SuperMethodInvocation;
 import org.eclipse.jdt.core.dom.ThisExpression;
 import org.eclipse.jdt.core.dom.TypeLiteral;
 import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 
 import callgraphanalyzer.Mappings;
 
 public class RVisitor extends ASTVisitor {
 	
 	private Resolver 	resolver;
 	private CallGraph 	callGraph;
 	private Clazz 		clazz;
 	private Method		method;
 	private Mappings 	mappings = new Mappings();
 	
 	public RVisitor(Resolver resolver, CallGraph callGraph, Clazz clazz, Method method) {
 		this.resolver = resolver;
 		this.callGraph = callGraph;
 		this.clazz = clazz;
 		this.method = method;
 	}
 	
 	/**
 	 * This is the main item we are looking for 
 	 * to resolve and will be our entry point.
 	 */
 	@Override
 	public boolean visit(MethodInvocation node) {
 		// Get the type
 		String type = null;
 		if(node.getExpression() != null)
 			type = resolveExpression(node.getExpression());
 		else
 			type = clazz.getName();
 		
 		// This means we were unable to resolve the method invocation
 		if(type == null)
 			return super.visit(node);
 		
 		// Get the method call
 		String methodName = node.getName().getFullyQualifiedName();
 		
 		List<String> parameters = resolveParameters(node.arguments());
 		
 		String methodToResolve = methodNameBuilder(type, methodName, parameters);
 		
 		System.out.println("Need to resolve the method: " + methodToResolve);
 		
 		Method resolved = lookupClassMethod(methodToResolve.substring(0, methodToResolve.lastIndexOf(".")), 
 				methodToResolve.substring(methodToResolve.lastIndexOf(".")));
 		
 		// The resolving has failed
 		if(resolved == null) {
 			return super.visit(node);
 		}
 		
 		method.addMethodCall(resolved);
 		resolved.addCalledBy(method);
 		
 		return super.visit(node);
 	}
 	
 	/**
 	 * This will be the main point for resolving expressions.
 	 * We need to cover all the cases here of the Expression
 	 * types we want to handle.
 	 * @param expression
 	 * @return
 	 */
 	private String resolveExpression(Expression expression) {
 		// Handle method invocation
 		if(expression instanceof MethodInvocation) {
 			return resolveMethodInvocation((MethodInvocation)expression);
 		}
 		// Handle variable
 		else if(expression instanceof SimpleName) {
 			return resolveSimpleName((SimpleName)expression);
 		}
 		// Handle boolean literal
 		else if(expression instanceof BooleanLiteral) {
 			return "boolean";
 		}
 		// Handle character literal
 		else if(expression instanceof CharacterLiteral) {
 			return "char";
 		}
 		// Handle null literal
 		else if(expression instanceof NullLiteral) {
 			return "null";
 		}
 		// Handle number literal
 		else if(expression instanceof NumberLiteral) {
 			return resolveNumberLiteral((NumberLiteral)expression);
 		}
 		// Handle String literal
 		else if(expression instanceof StringLiteral) {
 			return "String";
 		}
 		// Handle Type literal
 		else if(expression instanceof TypeLiteral) {
 			return ((TypeLiteral)expression).getType().toString();
 		}
 		// Handle cast expression
 		else if(expression instanceof CastExpression) {
 			return ((CastExpression)expression).getType().toString();
 		}
 		// Handle field access NOTE: Even though it says QualifiedName, it
 		// still behaves as a field access
 		else if(expression instanceof QualifiedName) {
 			return resolveQualifiedName((QualifiedName)expression);
 		}
 		// Handle explicit super field access
 		else if(expression instanceof SuperFieldAccess) {
 			return resolveSuperFieldAccess((SuperFieldAccess)expression);
 		}
 		// Handle explicit super method invocation
 		else if(expression instanceof SuperMethodInvocation) {
 			return resolveSuperMethodInvocation((SuperMethodInvocation)expression);
 		}
 		// Handle explicit use of "this"
 		else if(expression instanceof ThisExpression) {
 			return resolveThisExpression((ThisExpression)expression);
 		}
 		// Handle parathesized expression 
 		else if(expression instanceof ParenthesizedExpression) {
 			return resolveExpression(((ParenthesizedExpression)expression).getExpression());
 		}
 		// Handle postfix operator
 		else if(expression instanceof PostfixExpression) {
 			return resolveExpression(((PostfixExpression)expression).getOperand());
 		}
 		// Handle prefix operator
 		else if(expression instanceof PrefixExpression) {
 			return resolveExpression(((PrefixExpression)expression).getOperand());
 		}
 		// Handle instanceof operator
 		// Defaults to bool for now.
 		else if(expression instanceof InstanceofExpression) {
 			return "boolean";
 		}
 		// Handle Infix operator - assuming each side of the operator must be the same type
 		// This could be a risky assumption and may need tweaking later.
 		else if(expression instanceof InfixExpression) {
 			// Really we need to evaluate each expression and then take the 
 			// Higher precedence
 			return resolveExpression(((InfixExpression)expression).getLeftOperand());
 		}
		// Handle class instance creation
		else if(expression instanceof ClassInstanceCreation) {
			return ((ClassInstanceCreation)expression).getType().toString();
		}
		// Handle assignment
		else if(expression instanceof Assignment) {
			return resolveExpression(((Assignment)expression).getLeftHandSide());
		}
 		
 		return null;
 	}
 	
 	private String resolveMethodInvocation(MethodInvocation methodInvocation) {
 		// Get the type
 		String type = null;
 		if(methodInvocation.getExpression() != null)
 			type = resolveExpression(methodInvocation.getExpression());
 		else
 			type = clazz.getName();
 		
 		// This means that the type was unresolvable and we have failed at resolving.
 		if(type == null)
 			return null;
 
 		// Get the method call
 		String methodName = methodInvocation.getName().getFullyQualifiedName();
 
 		List<String> parameters = resolveParameters(methodInvocation.arguments());
 
 		String methodToResolve = methodNameBuilder(type, methodName, parameters);
 		
 		System.out.println("Need to look up the type of: " + methodToResolve);
 		
 		Method resolved = lookupClassMethod(methodToResolve.substring(0, methodToResolve.lastIndexOf(".")), 
 				methodToResolve.substring(methodToResolve.lastIndexOf(".")));
 		
 		if(resolved != null) {
 			System.out.println("                             " + "Return type: " + 
 					resolved.getReturnType());
 			return resolved.getReturnType();
 		}
 		else {
 			System.out.println("                             " + "Return type: unknown");
 			return null;
 		}
 	}
 	
 	private String resolveSimpleName(SimpleName name) {
 		String type = mappings.lookupType(name.toString());
 		if(type == null)
 			return clazz.lookupField(name.toString());
 		return type;
 	}
 	
 	private String resolveQualifiedName(QualifiedName qualifiedName) {
 		String type = null;
 		if(qualifiedName.getQualifier() instanceof SimpleName)
 			type = resolveSimpleName((SimpleName)qualifiedName.getQualifier());
 		else if(qualifiedName.getQualifier() instanceof QualifiedName)
 			type = resolveQualifiedName((QualifiedName)qualifiedName.getQualifier());
 		
 		return lookupClassField(type, qualifiedName.getName().toString());
 	}
 	
 	private String resolveNumberLiteral(NumberLiteral numberLiteral) {
 		if(numberLiteral.getToken().contains("F") || numberLiteral.getToken().contains("f"))
 			return "float";
 		if(numberLiteral.getToken().contains("D") || numberLiteral.getToken().contains("d") || 
 				numberLiteral.getToken().contains("E") || numberLiteral.getToken().contains("e"))
 			return "double";
 		if(numberLiteral.getToken().contains("L") || numberLiteral.getToken().contains("l"))
 			return "long";
 		if(!numberLiteral.getToken().contains("."))
 			return "int";
 		else
 			return "double";
 	}
 	
 	private String resolveSuperFieldAccess(SuperFieldAccess access) {
 		Clazz superClazz = null;
 		String className = null;
 		if(access.getQualifier() == null)
 			superClazz = clazz.getSuperClazz();
 		else {
 			className = resolveExpression(access.getQualifier());
 			Clazz lookupClazz = lookupClassName(className);
 			if(lookupClazz != null)
 				superClazz = lookupClazz.getSuperClazz();
 		}
 		
 		if(superClazz == null)
 			return null;
 		else
 			return superClazz.getName();
 	}
 	
 	private String resolveSuperMethodInvocation(SuperMethodInvocation invocation) {
 		Clazz superClazz = null;
 		String className = null;
 		if(invocation.getQualifier() == null)
 			superClazz = clazz.getSuperClazz();
 		else {
 			className = resolveExpression(invocation.getQualifier());
 			Clazz lookupClazz = lookupClassName(className);
 			if(lookupClazz != null)
 				superClazz = lookupClazz.getSuperClazz();
 		}
 		
 		if(superClazz == null)
 			return null;
 		else
 			return superClazz.getName();
 	}
 	
 	private String resolveThisExpression(ThisExpression thisExpression) {
 		if(thisExpression.getQualifier() != null)
 			return resolveExpression(thisExpression.getQualifier());
 		else
 			return clazz.getName();
 	}
 	
 	private List<String> resolveParameters(List<Expression> parameters) {
 		List<String> types = new ArrayList<String>();
 		
 		for(Expression expression: parameters)
 			types.add(resolveExpression(expression));
 		
 		return types;
 	}
 	
 	private String lookupClassField(String className, String field) {
 		if(className == null || field == null)
 			return null;
 		
 		// Look through the package for the class name and field
 		for (Clazz packageClazz : getClazzesInPackage(clazz.getFile().getFilePackage())) {
 			if(packageClazz.hasUnqualifiedName(className))
 				return packageClazz.lookupField(field);
 		}
 		
 		// Look through the imports for the class name and field
 		List<Clazz> imports = getClazzesInImports(clazz.getFile().getFileImports());
 		for (Clazz s : imports) {
 			if(s.hasUnqualifiedName(className))
 				return s.lookupField(field);
 		}
 		
 		// Check the current class for the field
 		return clazz.lookupField(field);
 	}
 	
 	private Method lookupClassMethod(String className, String method) {
 		// Look through the package for the class name and field
 		for (Clazz packageClazz : getClazzesInPackage(clazz.getFile().getFilePackage())) {
 			if(packageClazz.hasUnqualifiedName(className))
 				return packageClazz.hasUnqualifiedMethod(method);
 		}
 
 		// Look through the imports for the class name and field
 		List<Clazz> imports = getClazzesInImports(clazz.getFile().getFileImports());
 		for (Clazz s : imports) {
 			if(s.hasUnqualifiedName(className))
 				return s.hasUnqualifiedMethod(method);
 		}
 
 		// Check the current class for the field
 		return clazz.hasUnqualifiedMethod(method);
 	}
 	
 	private Clazz lookupClassName(String className) {
 		// Look through the package for the class name
 		for (Clazz packageClazz : getClazzesInPackage(clazz.getFile().getFilePackage())) {
 			if(packageClazz.hasUnqualifiedName(className))
 				return packageClazz;
 		}
 		
 		// Look through the imports for the class name
 		List<Clazz> imports = getClazzesInImports(clazz.getFile().getFileImports());
 		for (Clazz s : imports) {
 			if(s.hasUnqualifiedName(className))
 				return s;
 		}
 		
 		// Check the current class for the name
 		if(clazz.hasUnqualifiedName(className))
 			return clazz;
 		
 		return null;
 	}
 	
 	/**
 	 * This returns a list of all clazzes that are contained
 	 * inside of the given package.
 	 * @param pkg
 	 * @return
 	 */
 	public List<Clazz> getClazzesInPackage(String pkg) {
 		List<Clazz> clazzes = new ArrayList<Clazz>();
 		
 		for(Clazz clazz: callGraph.getAllClazzes()) {
 			if(clazz.getFile().getFilePackage().equals(pkg))
 				clazzes.add(clazz);
 		}
 		
 		return clazzes;
 	}
 	
 	/**
 	 * This will return a list of clazzes that are inside the
 	 * imported packages.
 	 * @param imports
 	 * @return
 	 */
 	public List<Clazz> getClazzesInImports(List<String> imports) {
 		Clazz tc = null;
 		try {
 			List<Clazz> clazzes = new ArrayList<Clazz>();
 			
 			for(Clazz clazz: callGraph.getAllClazzes()) {
 				tc = clazz;
 				for(String imp: imports) {
 					tc = clazz;
 					if(clazz.getName().equals(imp) || clazz.getFile().getFilePackage().equals(imp))
 						clazzes.add(clazz);
 				}
 			}
 			
 			return clazzes;
 		}
 		catch (NullPointerException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private String methodNameBuilder(String type, String methodName, List<String> parameterTypes) {
 		String builder = "";
 		
 		if(type != null && !type.equals(""))
 			builder += type + ".";
 		
 		builder += methodName + "(";
 		
 		for(String param: parameterTypes)
 			builder += param + ", ";
 		
 		if(parameterTypes.size() != 0)
 			builder = builder.substring(0, builder.length()-2);
 		
 		builder += ")";
 		
 		return builder;
 	}
 	
 	/***********************************************************************************
 	 * This divider is here to break up this class into its two main components.
 	 * The top half is dedicated to resolving method invocations.
 	 * The bottom half is dedicated to creating the variable mapping so that we
 	 * can look up the types of declared variables when we need to.
 	 **********************************************************************************/
 	
 	@Override
 	public boolean visit(VariableDeclarationExpression node)
 	{
 		SimpleName varName = null;
 		for (Iterator<VariableDeclarationFragment> i = node.fragments().iterator();i.hasNext();)
 		{
 			VariableDeclarationFragment frag = (VariableDeclarationFragment)i.next();
 			varName = frag.getName();
 		}
 		Mapping m = new Mapping(node.getType().toString(), varName.getFullyQualifiedName());
 		mappings.addMapping(node.fragments().get(0).toString(), m);
 		
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(VariableDeclarationStatement node)
 	{
 		SimpleName varName = null;
 		for (Iterator<VariableDeclarationFragment> i = node.fragments().iterator();i.hasNext();)
 		{
 			VariableDeclarationFragment frag = (VariableDeclarationFragment)i.next();
 			varName = frag.getName();
 		}
 		Mapping m = new Mapping(node.getType().toString(), varName.getFullyQualifiedName());
 		mappings.addMapping(varName.getFullyQualifiedName(), m);
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(Block node)
 	{
 		mappings.newMap();
 		return super.visit(node);
 	}
 	
 	@Override 
 	public void endVisit(Block node)
 	{
 		mappings.removeMap();
 	}
 
 }
