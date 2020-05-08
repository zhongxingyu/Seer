 package org.ita.testrefactoring.astparser;
 
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.ita.testrefactoring.metacode.Annotation;
 import org.ita.testrefactoring.metacode.Argument;
 import org.ita.testrefactoring.metacode.Block;
 import org.ita.testrefactoring.metacode.CheckedExceptionClass;
 import org.ita.testrefactoring.metacode.InnerElementAccessModifier;
 import org.ita.testrefactoring.metacode.Method;
 import org.ita.testrefactoring.metacode.MethodDeclarationNonAccessModifier;
 import org.ita.testrefactoring.metacode.Type;
 
 public class ASTMethod implements Method, ASTWrapper<MethodDeclaration> {
 
 	private InnerElementAccessModifier accessModifier = new InnerElementAccessModifier();
 	private String name;
 	private Type parent;
 	private MethodDeclarationNonAccessModifier nonAccessModifier = new MethodDeclarationNonAccessModifier();
 	private MethodDeclaration astObject;
	private Block body = new ASTBlock();
 	
 	@Override
 	public InnerElementAccessModifier getAccessModifier() {
 		return accessModifier;
 	}
 
 	void setName(String name) {
 		this.name = name;
 	}
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	void setParentType(Type parent) {
 		this.parent = parent;
 	}
 	
 	@Override
 	public Type getParentType() {
 		return parent;
 	}
 
 	@Override
 	public List<Annotation> getAnnotations() {
 		return null;
 	}
 
 	@Override
 	public MethodDeclarationNonAccessModifier getNonAccessModifier() {
 		return nonAccessModifier ;
 	}
 
 	@Override
 	public Type getReturnType() {
 		return null;
 	}
 
 	@Override
 	public List<Argument> getArgumentList() {
 		return null;
 	}
 
 	@Override
 	public List<CheckedExceptionClass> getThrownExceptions() {
 		return null;
 	}
 
 	@Override
 	public Block getBody() {
 		return body;
 	}
 
 	@Override
 	public void setASTObject(MethodDeclaration astObject) {
 		this.astObject = astObject; 
 	}
 
 	@Override
 	public MethodDeclaration getASTObject() {
 		return astObject;
 	}
 	
 	ASTMethodBlock createBlock() {
 		ASTMethodBlock methodBlock = new ASTMethodBlock();
 		
 		methodBlock.setParentMethod(this);
 		
 		return methodBlock;
 	}
 
 }
