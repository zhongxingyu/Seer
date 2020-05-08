 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import java.util.Stack;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Assignment;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.core.mixin.IMixinRequestor.ElementInfo;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.ruby.ast.ColonExpression;
 import org.eclipse.dltk.ruby.ast.RubyMethodArgument;
 import org.eclipse.dltk.ruby.ast.RubySingletonClassDeclaration;
 import org.eclipse.dltk.ruby.ast.RubySingletonMethodDeclaration;
 import org.eclipse.dltk.ruby.ast.SelfReference;
 import org.eclipse.dltk.ruby.core.model.FakeField;
 import org.eclipse.dltk.ruby.internal.core.RubyClassDeclaration;
 import org.eclipse.dltk.ruby.typeinference.RubyTypeInferencingUtils;
 import org.eclipse.dltk.ti.DLTKTypeInferenceEngine;
 import org.eclipse.dltk.ti.ITypeInferencer;
 
 public class RubyMixinBuildVisitor extends ASTVisitor {
 
 	private static final String INSTANCE_SUFFIX = RubyMixin.INSTANCE_SUFFIX;
 	private static final String VIRTUAL_SUFFIX = RubyMixin.VIRTUAL_SUFFIX;
 	private static final String SEPARATOR = MixinModel.SEPARATOR;
 
 	private final ISourceModule sourceModule;
 	private final boolean moduleAvailable;
 	private final ModuleDeclaration module;
 	private final IMixinRequestor requestor;
 
 	private abstract class Scope {
 		private final ASTNode node;
 
 		public Scope(ASTNode node) {
 			super();
 			this.node = node;
 		}
 
 		public ASTNode getNode() {
 			return node;
 		}
 
 		public abstract String reportMethod(String name, Object object);
 
 		public abstract String reportVariable(String name, Object object);
 
 		public abstract String reportType(String name, Object object);
 
 		public abstract String getClassKey();
 
 		public abstract String getKey();
 	}
 
 	private class SourceModuleScope extends Scope {
 
 		public SourceModuleScope(ModuleDeclaration node) {
 			super(node);
 		}
 
 		public String getClassKey() {
 			return "Object";
 		}
 
 		public String reportMethod(String name, Object object) {
 			report ("Object", null);
 			return report("Object" + SEPARATOR + name, object);
 		}
 
 		public String reportType(String name, Object object) {
 			report(name + INSTANCE_SUFFIX, object); 
 			return report(name, object);
 		}
 
 		public String reportVariable(String name, Object object) {
 			report ("Object", null);
 			if (name.startsWith("@"))
 				return report("Object" + SEPARATOR + name, object);
 			else
 				return null; //no top-level vars
 		}
 
 		public String getKey() {
 			return "Object";
 		}
 
 	}
 
 	private class ClassScope extends Scope {
 
 		private final String classKey;
 
 		public ClassScope(ASTNode node, String classKey) {
 			super(node);
 			this.classKey = classKey;
 		}
 
 		public String reportMethod(String name, Object object) {
 			String key = classKey + INSTANCE_SUFFIX + SEPARATOR + name;			
 			return report(key, object);
 		}
 
 		public String reportType(String name, Object obj) {
 			String key = classKey + SEPARATOR + name;
 			report(key + INSTANCE_SUFFIX, obj);
 			return report(key, obj);
 		}
 
 		public String reportVariable(String name, Object object) {
 			String key = null;
 			if (name.startsWith("@@")) {				
 				key = classKey + SEPARATOR + name;
 				report(classKey + INSTANCE_SUFFIX + SEPARATOR + name, object);
 				return report(key, object);
 			} else if (name.startsWith("@")) {
 				key = classKey + SEPARATOR + name;
 				return report(key, object);
 			} else {
 				key = classKey + SEPARATOR + name;
 				return report(key, object);
 			}
 		}
 
 		public String getClassKey() {
 			return classKey;
 		}
 
 		public String getKey() {
 			return classKey;
 		}
 
 	}
 
 	private class MetaClassScope extends Scope {
 
 		private final String classKey;
 
 		public MetaClassScope(ASTNode node, String classKey) {
 			super(node);
 			this.classKey = classKey;
 		}
 
 		public String reportMethod(String name, Object object) {
 			return report(classKey + SEPARATOR + name, object);
 		}
 
 		public String reportType(String name, Object object) {
 			report(classKey + SEPARATOR + name + INSTANCE_SUFFIX, object);
 			return report(classKey + SEPARATOR + name, object);
 		}
 
 		public String reportVariable(String name, Object object) {
 			if (name.startsWith("@@")) {
 				report(classKey + INSTANCE_SUFFIX + SEPARATOR + name, object);
 				return report(classKey + SEPARATOR + name, object);
 			} else {
 				return report(classKey + SEPARATOR + name, object);
 			}
 		}
 
 		public String getClassKey() {
 			return classKey;
 		}
 
 		public String getKey() {
 			return classKey;
 		}
 	}
 
 	private class MethodScope extends Scope {
 
 		private final Scope classScope;
 		private final String methodKey;
 
 		public MethodScope(ASTNode node, Scope classScope, String methodKey) {
 			super(node);
 			this.classScope = classScope;
 			this.methodKey = methodKey;
 		}
 
 		public String reportMethod(String name, Object object) {
 			return classScope.reportMethod(name, object);
 		}
 
 		public String reportType(String name, Object obj) {
 			throw new RuntimeException();
 		}
 
 		public String reportVariable(String name, Object obj) {
 			if (name.startsWith("@@")) {
 				String key = classScope.getKey() + SEPARATOR + name;
 				report(classScope.getKey() + INSTANCE_SUFFIX + SEPARATOR + name, obj);
 				return report(key, obj);
 			} if (name.startsWith("@")) {
 				String key;
 				if (classScope instanceof ClassScope) {
 					key = classScope.getKey() + INSTANCE_SUFFIX + SEPARATOR + name;
 				} else {
 					key = classScope.getKey() + SEPARATOR + name;
 				}
 				return report(key, obj);
 			} else {
 				return report(methodKey + SEPARATOR + name, obj);
 			}
 		}
 
 		public String getClassKey() {
 			return classScope.getClassKey();
 		}
 
 		public String getKey() {
 			return methodKey;
 		}
 
 	}
 
 	private Stack scopes = new Stack();
 	private ITypeInferencer inferencer;
 
 	public RubyMixinBuildVisitor(ModuleDeclaration module,
 			ISourceModule sourceModule, boolean moduleAvailable,
 			IMixinRequestor requestor) {
 		super();
 		this.module = module;
 		this.sourceModule = sourceModule;
 		this.moduleAvailable = moduleAvailable;
 		this.requestor = requestor;
 	}
 
 	private ITypeInferencer getInferencer() {
 		if (inferencer == null)
 			inferencer = new DLTKTypeInferenceEngine();
 		return inferencer;
 	}
 
 	private Scope peekScope() {
 		return (Scope) scopes.peek();
 	}
 
 	public boolean visit(ModuleDeclaration s) throws Exception {
 		this.scopes.add(new SourceModuleScope(s));
 //		report("Object", null);
 		return true;
 	}
 
 	private String evaluateSingletonMethodReceiver(Expression receiver) {
 		String parentKey = null;
 		if (receiver instanceof SelfReference) { // simple optimization
 			parentKey = "";
 		} else {
 			if (moduleAvailable) {
 				// BIG TODO
 				// new need to evaluate object(!) that results from receiver
 			} else
 				return null;
 		}
 		return parentKey;
 	}
 
 	public boolean visit(MethodDeclaration decl) throws Exception {
 		Object obj = null;
 		String name = decl.getName();
 		if (moduleAvailable)
 			obj = sourceModule.getElementAt(decl.sourceStart() + 1);
 		if (decl instanceof RubySingletonMethodDeclaration) {
 			RubySingletonMethodDeclaration singl = (RubySingletonMethodDeclaration) decl;
 			Expression receiver = singl.getReceiver();
 			if (receiver instanceof SelfReference) {
 				Scope scope = peekScope();
 				MetaClassScope metaScope = new MetaClassScope(scope.getNode(),
 						scope.getClassKey());
 				String method = metaScope.reportMethod(name, obj);
 				scopes.push(new MethodScope(decl, metaScope, method));
 			} else if (receiver instanceof ConstantReference
 					|| receiver instanceof ColonExpression) {
 				String evaluatedClassKey = evaluateClassKey(receiver);
 				if (evaluatedClassKey != null) {
 					Scope scope = peekScope();
 					MetaClassScope metaScope = new MetaClassScope(decl,
 							evaluatedClassKey);
 					String method = metaScope.reportMethod(name, obj);
 					scopes.push(new MethodScope(decl, metaScope, method));
 				} 
 			} else {
 				// TODO
 			}
 		} else {
 			Scope scope = peekScope();
 			String method = scope.reportMethod(name, obj);
 			scopes.push(new MethodScope(decl, scope, method));
 		}
 		return true;
 	}
 
 	
 	
 	public boolean visit(Expression s) throws Exception {
 		return this.visit((Statement)s);
 	}
 
 	public boolean visit(Statement s) throws Exception {
 		if (s instanceof RubyMethodArgument) {
 			RubyMethodArgument argument = (RubyMethodArgument) s;
 			String name = argument.getName();
 			Scope scope = peekScope();
 			Object obj = null;
 			if (sourceModule != null) {
 				obj = new FakeField((ModelElement) sourceModule, name, s.sourceStart(), s.sourceEnd());
 			}
 			scope.reportVariable(name, obj);
 		}
 		if (s instanceof Assignment) {
 			Assignment assignment = (Assignment) s;
 			Expression left = assignment.getLeft();
 			if (left instanceof VariableReference) {
 				VariableReference ref = (VariableReference) left;
 				String name = ref.getName();
 				Scope scope = peekScope();
 				Object obj = null;
 				if (sourceModule != null)
 					obj = new FakeField((ModelElement) sourceModule, name, ref.sourceStart(), ref.sourceEnd());
 				scope.reportVariable(name, obj);
 			}			
 		}
 		return super.visit(s);
 	}
 
 	public boolean visit(TypeDeclaration decl) throws Exception {
 		Object obj = null;
 		if (moduleAvailable)
 			obj = sourceModule.getElementAt(decl.sourceStart() + 1);
 		if (decl instanceof RubySingletonClassDeclaration) {
 			RubySingletonClassDeclaration declaration = (RubySingletonClassDeclaration) decl;
 			Expression receiver = declaration.getReceiver();
 			if (receiver instanceof SelfReference) {
 				Scope scope = peekScope();
 				scopes.push(new MetaClassScope(decl, scope.getClassKey()));
 				return true;
 			} else {
 				// TODO
 			}
 		} else if (decl instanceof RubyClassDeclaration) {
 			RubyClassDeclaration declaration = (RubyClassDeclaration) decl;
 			Statement className = declaration.getClassName();
 			if (className instanceof ConstantReference) {
 				String name = ((ConstantReference) className).getName();
 				Scope scope = peekScope();
 				String newKey = scope.reportType(name, obj);
 				scopes.push(new ClassScope(decl, newKey));
 			} else {
 				String name = evaluateClassKey(className);
 				if (name != null) {
 					report(name, obj);
 					scopes.push(new ClassScope(decl, name));
 				}
 			}
 			return true;
 		} else {
 			String name = decl.getName();
 			Scope scope = peekScope();
 			String newKey = scope.reportType(name, obj);
 			scopes.push(new ClassScope(decl, newKey));
 			return true;
 		}
 		return false;
 	}
 
 	private String report(String key, Object object) {
 		ElementInfo info = new IMixinRequestor.ElementInfo();
 		info.key = key;
 		info.object = object;
 		if (requestor != null) {
 			requestor.reportElement(info);
 //			if (DLTKCore.DEBUG_INDEX) {
				System.out.println("Mixin reported: " + key);
 //			}
 		}
 		return key;
 	}
 
 	private String evaluateClassKey(Statement expr) {
 		if (expr instanceof ColonExpression) {
 			ColonExpression colonExpression = (ColonExpression) expr;
 			if (colonExpression.isFull()) {
 				return colonExpression.getName();
 			} else {
 				String key = evaluateClassKey(colonExpression.getLeft());
 				if (key != null) {
 					return key + SEPARATOR + colonExpression.getName();
 				}
 			}
 		} else if (expr instanceof ConstantReference) {
 			ConstantReference constantReference = (ConstantReference) expr;
 			if (moduleAvailable) {
 				String elementKey = RubyTypeInferencingUtils
 						.searchConstantElement(module, expr.sourceStart() - 1,
 								constantReference.getName());
 				if (elementKey != null) {
 					return elementKey;
 				}
 			}
 		}
 		return null;
 	}
 
 	public void endvisitGeneral(ASTNode node) throws Exception {
 		Scope scope = (Scope) scopes.peek();
 		if (scope.getNode() == node) {
 			scopes.pop();
 		}
 		super.endvisitGeneral(node);
 	}
 
 	public static String[] restoreScopesByNodes(ASTNode[] nodes) {
 		Assert.isLegal(nodes != null);
 		Assert.isLegal(nodes.length > 0);
 		String[] keys = new String[nodes.length];
 		RubyMixinBuildVisitor visitor = new RubyMixinBuildVisitor(
 				(ModuleDeclaration) nodes[0], null, false, null);
 		for (int i = 0; i < nodes.length; i++) {
 			try {
 				if (nodes[i] instanceof ModuleDeclaration) {
 					visitor.visit((ModuleDeclaration) nodes[i]);
 				} else if (nodes[i] instanceof TypeDeclaration) {
 					visitor.visit((TypeDeclaration)nodes[i]);
 				} else if (nodes[i] instanceof MethodDeclaration) {
 					visitor.visit((MethodDeclaration)nodes[i]);
 				} else {
 					Statement s = (Statement)nodes[i];
 					visitor.visit(s);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			}
 			Scope scope = visitor.peekScope();
 			keys[i] = scope.getKey();
 		}
 		return keys;
 	}
 
 }
