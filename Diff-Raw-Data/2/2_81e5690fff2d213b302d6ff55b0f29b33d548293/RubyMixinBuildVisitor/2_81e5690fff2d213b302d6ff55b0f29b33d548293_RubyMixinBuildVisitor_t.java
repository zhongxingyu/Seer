 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTListNode;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.core.mixin.IMixinRequestor.ElementInfo;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.ruby.ast.RubyAliasExpression;
 import org.eclipse.dltk.ruby.ast.RubyAssignment;
 import org.eclipse.dltk.ruby.ast.RubyCallArgument;
 import org.eclipse.dltk.ruby.ast.RubyClassDeclaration;
 import org.eclipse.dltk.ruby.ast.RubyColonExpression;
 import org.eclipse.dltk.ruby.ast.RubyConstantDeclaration;
 import org.eclipse.dltk.ruby.ast.RubyMethodArgument;
 import org.eclipse.dltk.ruby.ast.RubySelfReference;
 import org.eclipse.dltk.ruby.ast.RubySingletonClassDeclaration;
 import org.eclipse.dltk.ruby.ast.RubySingletonMethodDeclaration;
 import org.eclipse.dltk.ruby.core.IRubyConstants;
 import org.eclipse.dltk.ruby.core.model.FakeField;
 import org.eclipse.dltk.ruby.core.model.FakeMethod;
 import org.eclipse.dltk.ruby.internal.parser.visitors.RubyAttributeHandler;
 
 public class RubyMixinBuildVisitor extends ASTVisitor {
 
 	private static final String INSTANCE_SUFFIX = RubyMixin.INSTANCE_SUFFIX;
 	private static final String VIRTUAL_SUFFIX = RubyMixin.VIRTUAL_SUFFIX;
 	private static final String SEPARATOR = MixinModel.SEPARATOR;
 
 	private final ISourceModule sourceModule;
 	private final boolean moduleAvailable;
 	private final IMixinRequestor requestor;
 	private final HashSet allReportedKeys = new HashSet();
 
 	private abstract class Scope {
 		private final ASTNode node;
 
 		public Scope(ASTNode node) {
 			super();
 			this.node = node;
 		}
 
 		public ASTNode getNode() {
 			return node;
 		}
 
 		public abstract String reportMethod(String name, IMethod object);
 
 		public abstract String reportVariable(String name, IField object);
 
 		public abstract String reportType(String name, IType object,
 				boolean module);
 
 		public abstract String reportInclude(String object);
 
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
 
 		public String reportMethod(String name, IMethod object) {
 			return report(name, RubyMixinElementInfo.createMethod(object));
 		}
 
 		public String reportType(String name, IType object, boolean module) {
 			RubyMixinElementInfo obj = null;
 			if (module)
 				obj = RubyMixinElementInfo.createModule(object);
 			else
 				obj = RubyMixinElementInfo.createClass(object);
 			report(name + INSTANCE_SUFFIX, obj);
 			return report(name, obj);
 		}
 
 		public String reportVariable(String name, IField object) {
 			RubyMixinElementInfo info = (name.endsWith(VIRTUAL_SUFFIX)) ? RubyMixinElementInfo
 					.createVirtualClass()
 					: RubyMixinElementInfo.createVariable(object);
 			if (name.startsWith("$"))
 				return report(name, info);
 			if (name.startsWith("@") || Character.isUpperCase(name.charAt(0)))
 				return report("Object" + SEPARATOR + name, info);
 			else {
 				if (info.getKind() == RubyMixinElementInfo.K_VIRTUAL)
 					return report(name, info);
 				return null; // no top-level vars
 			}
 		}
 
 		public String getKey() {
 			return "Object";
 		}
 
 		public String reportInclude(String object) {
 			return null;
 		}
 
 	}
 
 	private class ClassScope extends Scope {
 
 		private final String classKey;
 
 		public ClassScope(ASTNode node, String classKey) {
 			super(node);
 			this.classKey = classKey;
 		}
 
 		public String reportMethod(String name, IMethod object) {
 			String key = classKey + INSTANCE_SUFFIX + SEPARATOR + name;
 			return report(key, RubyMixinElementInfo.createMethod(object));
 		}
 
 		public String reportType(String name, IType obj, boolean module) {
 			RubyMixinElementInfo object = null;
 			if (module)
 				object = RubyMixinElementInfo.createModule(obj);
 			else
 				object = RubyMixinElementInfo.createClass(obj);
 			String key = classKey + SEPARATOR + name;
 			report(key + INSTANCE_SUFFIX, object);
 			return report(key, object);
 		}
 
 		public String reportVariable(String name, IField object) {
 			RubyMixinElementInfo info = (name.endsWith(VIRTUAL_SUFFIX)) ? RubyMixinElementInfo
 					.createVirtualClass()
 					: RubyMixinElementInfo.createVariable(object);
 			if (name.startsWith("$"))
 				return report(name, info);
 			RubyMixinElementInfo obj = info;
 			String key = null;
 			if (name.startsWith("@@")) {
 				key = classKey + SEPARATOR + name;
 				report(classKey + INSTANCE_SUFFIX + SEPARATOR + name, obj);
 				return report(key, obj);
 			} else if (name.startsWith("@")) {
 				key = classKey + SEPARATOR + name;
 				return report(key, obj);
 			} else {
 				key = classKey + SEPARATOR + name;
 				return report(key, obj);
 			}
 		}
 
 		public String getClassKey() {
 			return classKey;
 		}
 
 		public String getKey() {
 			return classKey;
 		}
 
 		public String reportInclude(String object) {
 			return report(classKey + INSTANCE_SUFFIX, new RubyMixinElementInfo(
 					RubyMixinElementInfo.K_INCLUDE, object));
 		}
 
 	}
 
 	private class MetaClassScope extends Scope {
 
 		private final String classKey;
 
 		public MetaClassScope(ASTNode node, String classKey) {
 			super(node);
 			this.classKey = classKey;
 		}
 
 		public String reportMethod(String name, IMethod object) {
 			return report(classKey + SEPARATOR + name, RubyMixinElementInfo
 					.createMethod(object));
 		}
 
 		public String reportType(String name, IType object, boolean module) {
 			RubyMixinElementInfo obj = null;
 			if (module)
 				obj = RubyMixinElementInfo.createModule(object);
 			else
 				obj = RubyMixinElementInfo.createClass(object);
 			report(classKey + SEPARATOR + name + INSTANCE_SUFFIX, obj);
 			return report(classKey + SEPARATOR + name, obj);
 		}
 
 		public String reportVariable(String name, IField object) {
 			RubyMixinElementInfo info = (name.endsWith(VIRTUAL_SUFFIX)) ? RubyMixinElementInfo
 					.createVirtualClass()
 					: RubyMixinElementInfo.createVariable(object);
 			if (name.startsWith("$"))
 				return report(name, info);
 			RubyMixinElementInfo obj = info;
 			if (name.startsWith("@@")) {
 				report(classKey + INSTANCE_SUFFIX + SEPARATOR + name, obj);
 				return report(classKey + SEPARATOR + name, obj);
 			} else {
 				return report(classKey + SEPARATOR + name, obj);
 			}
 		}
 
 		public String getClassKey() {
 			return classKey;
 		}
 
 		public String getKey() {
 			return classKey;
 		}
 
 		public String reportInclude(String object) {
 			return report(classKey, new RubyMixinElementInfo(
 					RubyMixinElementInfo.K_INCLUDE, object));
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
 
 		public String reportMethod(String name, IMethod object) {
 			return classScope.reportMethod(name, object);
 		}
 
 		public String reportType(String name, IType obj, boolean module) {
 			// throw new RuntimeException();
 			return null;
 		}
 
 		public String reportVariable(String name, IField obj) {
 			RubyMixinElementInfo info = (name.endsWith(VIRTUAL_SUFFIX)) ? RubyMixinElementInfo
 					.createVirtualClass()
 					: RubyMixinElementInfo.createVariable(obj);
 			if (name.startsWith("$"))
 				return report(name, info);
 			RubyMixinElementInfo object = info;
 			if (name.startsWith("@@")) {
 				String key = classScope.getKey() + SEPARATOR + name;
 				report(
 						classScope.getKey() + INSTANCE_SUFFIX + SEPARATOR
 								+ name, object);
 				return report(key, object);
 			}
 			if (name.startsWith("@")) {
 				String key;
 				if (classScope instanceof ClassScope) {
 					key = classScope.getKey() + INSTANCE_SUFFIX + SEPARATOR
 							+ name;
 				} else {
 					key = classScope.getKey() + SEPARATOR + name;
 				}
 				return report(key, object);
 			} else {
 				return report(methodKey + SEPARATOR + name, object);
 			}
 		}
 
 		public String getClassKey() {
 			return classScope.getClassKey();
 		}
 
 		public String getKey() {
 			return methodKey;
 		}
 
 		public String reportInclude(String object) {
 			return classScope.reportInclude(object);
 		}
 
 	}
 
 	private Stack scopes = new Stack();
 	private final ModuleDeclaration module;
 
 	public RubyMixinBuildVisitor(ModuleDeclaration module,
 			ISourceModule sourceModule, boolean moduleAvailable,
 			IMixinRequestor requestor) {
 		super();
 		this.module = module;
 		this.sourceModule = sourceModule;
 		this.moduleAvailable = moduleAvailable;
 		this.requestor = requestor;
 	}
 
 	private Scope peekScope() {
 		return (Scope) scopes.peek();
 	}
 
 	public boolean visit(ModuleDeclaration s) throws Exception {
 		this.scopes.add(new SourceModuleScope(s));
 		return true;
 	}
 
 	public boolean visit(MethodDeclaration decl) throws Exception {
 		IMethod obj = null;
 		String name = decl.getName();
 		if (moduleAvailable) {
 			IModelElement element = findModelElementFor(decl);
 			obj = (IMethod) element;
 		}
 		if (decl instanceof RubySingletonMethodDeclaration) {
 			RubySingletonMethodDeclaration singl = (RubySingletonMethodDeclaration) decl;
 			ASTNode receiver = singl.getReceiver();
 			if (receiver instanceof RubySelfReference) {
 				Scope scope = peekScope();
 				MetaClassScope metaScope = new MetaClassScope(scope.getNode(),
 						scope.getClassKey());
 				String method = metaScope.reportMethod(name, obj);
 				scopes.push(new MethodScope(decl, metaScope, method));
 			} else if (receiver instanceof ConstantReference
 					|| receiver instanceof RubyColonExpression) {
 				String evaluatedClassKey = evaluateClassKey(receiver);
 				if (evaluatedClassKey != null) {
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
 
 	private IModelElement findModelElementFor(ASTNode decl)
 			throws ModelException {
 // return null;
 		return sourceModule.getElementAt(decl.sourceStart() + 1);
 	}
 
 	public boolean visitGeneral(ASTNode s) throws Exception {
 		if (s instanceof RubyMethodArgument) {
 			RubyMethodArgument argument = (RubyMethodArgument) s;
 			String name = argument.getName();
 			Scope scope = peekScope();
 			IField obj = null;
 			if (sourceModule != null) {
 				obj = new FakeField((ModelElement) sourceModule, name, s
 						.sourceStart(), s.sourceEnd() - s.sourceStart());
 			}
 			scope.reportVariable(name, obj);
 		} else if (s instanceof RubyAssignment) {
 			RubyAssignment assignment = (RubyAssignment) s;
 			ASTNode left = assignment.getLeft();
 			if (left instanceof VariableReference) {
 				VariableReference ref = (VariableReference) left;
 				String name = ref.getName();
 				Scope scope = peekScope();
 				IField obj = null;
 				if (sourceModule != null)
 					obj = new FakeField((ModelElement) sourceModule, name, ref
 							.sourceStart(), ref.sourceEnd() - ref.sourceStart());
 				scope.reportVariable(name, obj);
 			}
 		} else if (s instanceof CallExpression) {
 			visit((CallExpression) s);
 		} else if (s instanceof RubyConstantDeclaration) {
 			RubyConstantDeclaration constantDeclaration = (RubyConstantDeclaration) s;
 			SimpleReference name2 = constantDeclaration.getName();
 			String name = name2.getName();
 			boolean closeScope = false;
 			if (constantDeclaration.getPath() instanceof RubyColonExpression) {
 				RubyColonExpression colon = (RubyColonExpression) constantDeclaration
 						.getPath();
 				String classKey = evaluateClassKey(colon.getLeft());
 				if (classKey != null) {
 					this.scopes.add(new ClassScope(colon, classKey));
 					closeScope = true;
 				}
 			}
 			Scope scope = peekScope();
 			IField obj = null;
 			if (sourceModule != null)
 				obj = new FakeField((ModelElement) sourceModule, name, name2
 						.sourceStart(), name2.sourceEnd() - name2.sourceStart());
 			scope.reportVariable(name, obj);
 			if (closeScope)
 				this.scopes.pop();
 		} else if (s instanceof RubyAliasExpression) {
 			RubyAliasExpression alias = (RubyAliasExpression) s;
 			String oldValue = alias.getOldValue();
 			if (!oldValue.startsWith("$")) {
 				String newValue = alias.getNewValue();
 				String nkey = peekScope().reportMethod(newValue, null);
 				report(nkey, new RubyMixinElementInfo(
 						RubyMixinElementInfo.K_ALIAS, alias));
 			}
 		}
 		return true;
 	}
 
 	public boolean visit(CallExpression call) throws Exception {
		if (call.getReceiver() == null && call.getName().equals("include") && call.getArgs().getChilds().size() > 0) {
 			ASTNode expr = (ASTNode) call.getArgs().getChilds().get(0);
 			if (expr instanceof RubyCallArgument)
 				expr = ((RubyCallArgument) expr).getValue();
 			Scope scope = peekScope();
 			String incl = evaluateClassKey(expr);
 			if (incl != null)
 				scope.reportInclude(incl);
 			return false;
 		} else if (RubyAttributeHandler.isAttributeCreationCall(call)
 				&& sourceModule != null) {
 			Scope scope = peekScope();
 			RubyAttributeHandler info = new RubyAttributeHandler(call);
 			List readers = info.getReaders();
 			for (Iterator iterator = readers.iterator(); iterator.hasNext();) {
 				ASTNode n = (ASTNode) iterator.next();
 				String attr = RubyAttributeHandler.getText(n);
 				if (attr == null)
 					continue;
 				FakeMethod fakeMethod = new FakeMethod(
 						(ModelElement) sourceModule, attr, n.sourceStart(),
 						attr.length(), n.sourceStart(), attr.length());
 				scope.reportMethod(attr, fakeMethod);
 			}
 			List writers = info.getWriters();
 			for (Iterator iterator = writers.iterator(); iterator.hasNext();) {
 				ASTNode n = (ASTNode) iterator.next();
 				String attr = RubyAttributeHandler.getText(n);
 				if (attr == null)
 					continue;
 				FakeMethod fakeMethod = new FakeMethod(
 						(ModelElement) sourceModule, attr + "=", n
 								.sourceStart(), attr.length(), n.sourceStart(),
 						attr.length());
 				scope.reportMethod(attr + "=", fakeMethod);
 			}
 			return false;
 		}
 		return true;
 	}
 
 	public boolean visit(TypeDeclaration decl) throws Exception {
 		IType obj = null;
 		if (moduleAvailable) {
 			IModelElement elementFor = findModelElementFor(decl);
 			obj = (IType) elementFor;
 		}
 		boolean module = (decl.getModifiers() & Modifiers.AccModule) != 0;
 		if (decl instanceof RubySingletonClassDeclaration) {
 			RubySingletonClassDeclaration declaration = (RubySingletonClassDeclaration) decl;
 			ASTNode receiver = declaration.getReceiver();
 			if (receiver instanceof RubySelfReference) {
 				Scope scope = peekScope();
 				scopes.push(new MetaClassScope(decl, scope.getClassKey()));
 				return true;
 			} else if (receiver instanceof ConstantReference
 					|| receiver instanceof RubyColonExpression) {
 				String evaluatedClassKey = evaluateClassKey(receiver);
 				if (evaluatedClassKey != null) {
 					MetaClassScope metaScope = new MetaClassScope(decl,
 							evaluatedClassKey);
 					scopes.push(metaScope);
 					return true;
 				}
 			} else if (receiver instanceof VariableReference) {
 				VariableReference ref = (VariableReference) receiver;
 				Scope scope = peekScope();
 				String key = scope.reportVariable(ref.getName()
 						+ VIRTUAL_SUFFIX, null);
 				scopes.push(new MetaClassScope(decl, key));
 				return true;
 			} else {
 				// TODO: add common method for singletons resolving
 			}
 		} else if (decl instanceof RubyClassDeclaration) {
 			RubyClassDeclaration declaration = (RubyClassDeclaration) decl;
 			ASTNode className = declaration.getClassName();
 			if (className instanceof ConstantReference) {
 				String name = ((ConstantReference) className).getName();
 				Scope scope = peekScope();
 				String newKey = scope.reportType(name, obj, module);
 				scopes.push(new ClassScope(decl, newKey));
 			} else {
 				String name = evaluateClassKey(className);
 				if (name != null) {
 					report(name, RubyMixinElementInfo.createClass(obj));
 					scopes.push(new ClassScope(decl, name));
 				}
 			}
 			ASTListNode superClasses = declaration.getSuperClasses();
 			if (superClasses != null && superClasses.getChilds().size() == 1) {
 				ASTNode s = (ASTNode) superClasses.getChilds().get(0);
 				if (this.sourceModule != null) {
 					SuperclassReferenceInfo ref = new SuperclassReferenceInfo(
 							s, this.module, sourceModule);
 					Scope scope = peekScope();
 					report(scope.getKey() + INSTANCE_SUFFIX,
 							new RubyMixinElementInfo(
 									RubyMixinElementInfo.K_SUPER, ref));
 				}
 			}
 			return true;
 		} else {
 			String name = decl.getName();
 			Scope scope = peekScope();
 			String newKey = scope.reportType(name, obj, module);
 			scopes.push(new ClassScope(decl, newKey));
 			return true;
 		}
 		return false;
 	}
 
 	private String report(String key, RubyMixinElementInfo object) {
 		RubyMixinModel.getRawInstance().clearKeysCashe(key);
 		ElementInfo info = new IMixinRequestor.ElementInfo();
 		info.key = key;
 		info.object = object;
 		if (requestor != null) {
 			requestor.reportElement(info);
 // System.out.println("Mixin reported: " + key);
 		}
 		allReportedKeys.add(key);
 		return key;
 	}
 
 	private String evaluateClassKey(ASTNode expr) {
 		if (expr instanceof RubyColonExpression) {
 			RubyColonExpression colonExpression = (RubyColonExpression) expr;
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
 			// simple heuristic
 			int size = this.scopes.size();
 			for (int i = size - 1; i >= 0; i--) {
 				String possibleKey = "";
 				if (i > 0) {
 					Scope s = (Scope) this.scopes.get(i);
 					possibleKey = s.getKey() + SEPARATOR
 							+ constantReference.getName();
 				} else
 					possibleKey = constantReference.getName();
 				if (this.allReportedKeys.contains(possibleKey))
 					return possibleKey;
 			}
 
 			return constantReference.getName();
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
 					visitor.visit((TypeDeclaration) nodes[i]);
 				} else if (nodes[i] instanceof MethodDeclaration) {
 					visitor.visit((MethodDeclaration) nodes[i]);
 				} else {
 					visitor.visit(nodes[i]);
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
