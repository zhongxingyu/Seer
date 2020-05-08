 
 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.ti;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.internal.javascript.validation.JavaScriptValidations;
 import org.eclipse.dltk.javascript.ast.Argument;
 import org.eclipse.dltk.javascript.ast.ArrayInitializer;
 import org.eclipse.dltk.javascript.ast.AsteriskExpression;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.BooleanLiteral;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.CaseClause;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.CommaExpression;
 import org.eclipse.dltk.javascript.ast.ConditionalOperator;
 import org.eclipse.dltk.javascript.ast.ConstStatement;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.EmptyStatement;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.ForEachInStatement;
 import org.eclipse.dltk.javascript.ast.ForInStatement;
 import org.eclipse.dltk.javascript.ast.ForStatement;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetAllChildrenExpression;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.GetLocalNameExpression;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.Keyword;
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ObjectInitializerPart;
 import org.eclipse.dltk.javascript.ast.ParenthesizedExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.RegExpLiteral;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.SimpleType;
 import org.eclipse.dltk.javascript.ast.Statement;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.SwitchComponent;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.TypeOfExpression;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.VoidOperator;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlExpressionFragment;
 import org.eclipse.dltk.javascript.ast.XmlFragment;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.XmlTextFragment;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.PropertyExpressionUtils;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IParameter;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.ReferenceSource;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.emf.common.util.EList;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class TypeInferencerVisitor extends TypeInferencerVisitorBase {
 
 	public TypeInferencerVisitor(ITypeInferenceContext context) {
 		super(context);
 	}
 
 	private final Stack<Branching> branchings = new Stack<Branching>();
 
 	private class Branching {
 		public void end() {
 			branchings.remove(this);
 		}
 	}
 
 	protected Branching branching() {
 		final Branching branching = new Branching();
 		branchings.add(branching);
 		return branching;
 	}
 
 	private ReferenceSource getSource() {
 		final ReferenceSource source = context.getSource();
 		return source != null ? source : ReferenceSource.UNKNOWN;
 	}
 
 	protected void assign(IValueReference dest, IValueReference src) {
 		if (branchings.isEmpty()) {
 			dest.setValue(src);
 		} else {
 			dest.addValue(src, false);
 		}
 	}
 
 	@Override
 	public IValueReference visitArrayInitializer(ArrayInitializer node) {
 		return context.getFactory().createArray(peekContext());
 	}
 
 	@Override
 	public IValueReference visitAsteriskExpression(AsteriskExpression node) {
 		return context.getFactory().createXML(peekContext());
 	}
 
 	@Override
 	public IValueReference visitBinaryOperation(BinaryOperation node) {
 		final IValueReference left = visit(node.getLeftExpression());
 		final IValueReference right = visit(node.getRightExpression());
 		if (JSParser.ASSIGN == node.getOperation()) {
 			return visitAssign(left, right);
 		} else if (left == null && right instanceof ConstantValue) {
 			return right;
 		} else {
 			// TODO handle other operations
 			return null;
 		}
 	}
 
 	protected IValueReference visitAssign(IValueReference left,
 			IValueReference right) {
 		if (left != null)
 			assign(left, right);
 		return right;
 	}
 
 	@Override
 	public IValueReference visitBooleanLiteral(BooleanLiteral node) {
 		return context.getFactory().createBoolean(peekContext());
 	}
 
 	@Override
 	public IValueReference visitBreakStatement(BreakStatement node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitCallExpression(CallExpression node) {
 		final IValueReference reference = visit(node.getExpression());
 		for (ASTNode argument : node.getArguments()) {
 			visit(argument);
 		}
 		if (reference != null) {
 			return reference.getChild(IValueReference.FUNCTION_OP);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public IValueReference visitCommaExpression(CommaExpression node) {
 		return visit(node.getItems());
 	}
 
 	@Override
 	public IValueReference visitConditionalOperator(ConditionalOperator node) {
 		visit(node.getCondition());
 		return merge(visit(node.getTrueValue()), visit(node.getFalseValue()));
 	}
 
 	@Override
 	public IValueReference visitConstDeclaration(ConstStatement node) {
 		final IValueCollection context = peekContext();
 		for (VariableDeclaration declaration : node.getVariables()) {
 			createVariable(context, declaration);
 		}
 		return null;
 	}
 
 	protected IValueReference createVariable(IValueCollection context,
 			VariableDeclaration declaration) {
 		final Identifier identifier = declaration.getIdentifier();
 		final String varName = identifier.getName();
 		final IValueReference reference = context.createChild(varName);
 		final org.eclipse.dltk.javascript.ast.Type varType = declaration
 				.getType();
 		if (varType != null) {
 			reference.setDeclaredType(resolveType(varType));
 		}
 		reference.setKind(ReferenceKind.LOCAL);
 		reference.setLocation(ReferenceLocation.create(getSource(),
 				declaration.sourceStart(), declaration.sourceEnd(),
 				identifier.sourceStart(), identifier.sourceEnd()));
 		if (declaration.getInitializer() != null) {
 			assign(reference, visit(declaration.getInitializer()));
 		}
 		return reference;
 	}
 
 	@Override
 	public IValueReference visitContinueStatement(ContinueStatement node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitDecimalLiteral(DecimalLiteral node) {
 		return context.getFactory().createNumber(peekContext());
 	}
 
 	@Override
 	public IValueReference visitDefaultXmlNamespace(
 			DefaultXmlNamespaceStatement node) {
 		visit(node.getValue());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitDeleteStatement(DeleteStatement node) {
 		IValueReference value = visit(node.getExpression());
 		if (value != null) {
 			value.delete();
 		}
 		return context.getFactory().createBoolean(peekContext());
 	}
 
 	@Override
 	public IValueReference visitDoWhileStatement(DoWhileStatement node) {
 		visit(node.getCondition());
 		visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitEmptyExpression(EmptyExpression node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitEmptyStatement(EmptyStatement node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitForEachInStatement(ForEachInStatement node) {
 		visit(node.getItem());
 		visit(node.getIterator());
 		visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitForInStatement(ForInStatement node) {
 		final IValueReference item = visit(node.getItem());
 		if (item != null) {
 			assign(item, context.getFactory().createString(peekContext()));
 		}
 		visit(node.getIterator());
 		visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitForStatement(ForStatement node) {
 		if (node.getInitial() != null)
 			visit(node.getInitial());
 		if (node.getCondition() != null)
 			visit(node.getCondition());
 		if (node.getStep() != null)
 			visit(node.getStep());
 		if (node.getBody() != null)
 			visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitFunctionStatement(FunctionStatement node) {
 		final JSMethod method = new JSMethod();
 		final Identifier methodName = node.getName();
 		if (methodName != null) {
 			method.setName(methodName.getName());
 		}
 		org.eclipse.dltk.javascript.ast.Type funcType = node.getReturnType();
 		if (funcType != null) {
 			method.setType(resolveType(funcType));
 		}
 		for (Argument argument : node.getArguments()) {
 			final IParameter parameter = method.createParameter();
 			parameter.setName(argument.getIdentifier().getName());
 			org.eclipse.dltk.javascript.ast.Type paramType = argument.getType();
 			if (paramType != null) {
 				parameter.setType(resolveType(paramType));
 				parameter.setLocation(ReferenceLocation.create(getSource(),
 						argument.sourceStart(), paramType.sourceEnd(),
 						argument.sourceStart(), argument.sourceEnd()));
 			} else {
 				parameter.setLocation(ReferenceLocation.create(getSource(),
 						argument.sourceStart(), argument.sourceEnd()));
 			}
 			method.getParameters().add(parameter);
 		}
 		if (methodName != null) {
 			for (IModelBuilder extension : TypeInfoManager.getModelBuilders()) {
 				extension.processMethod(context, node, method);
 			}
 		}
 		final IValueCollection function = new FunctionValueCollection(
 				peekContext(), method.getName());
 		for (IParameter parameter : method.getParameters()) {
 			final IValueReference refArg = function.createChild(parameter
 					.getName());
 			refArg.setKind(ReferenceKind.ARGUMENT);
 			refArg.setDeclaredType(parameter.getType());
 			refArg.setLocation(parameter.getLocation());
 		}
 		enterContext(function);
 		try {
 			visitFunctionBody(node);
 		} finally {
 			leaveContext();
 		}
 		final IValueReference result;
 		if (methodName != null) {
 			result = peekContext().createChild(method.getName());
 			result.setLocation(ReferenceLocation.create(getSource(),
 					node.sourceStart(), node.sourceEnd(),
 					methodName.sourceStart(), methodName.sourceEnd()));
 		} else {
 			result = new AnonymousValue();
 			final Keyword kw = node.getFunctionKeyword();
 			result.setLocation(ReferenceLocation.create(getSource(),
 					node.sourceStart(), node.sourceEnd(), kw.sourceStart(),
 					kw.sourceEnd()));
 		}
 		result.setKind(ReferenceKind.FUNCTION);
 		result.setDeclaredType(context.getKnownType(ITypeNames.FUNCTION));
 		result.setAttribute(IReferenceAttributes.PARAMETERS, method);
 		result.setAttribute(IReferenceAttributes.FUNCTION_SCOPE, function);
 		final IValueReference returnValue = result
 				.getChild(IValueReference.FUNCTION_OP);
 		returnValue.setDeclaredType(method.getType());
 		returnValue.setValue(function.getReturnValue());
 		return result;
 	}
 
 	protected void visitFunctionBody(FunctionStatement node) {
 		visit(node.getBody());
 	}
 
 	protected Type resolveType(org.eclipse.dltk.javascript.ast.Type type) {
 		return context.getType(type.getName());
 	}
 
 	@Override
 	public IValueReference visitGetAllChildrenExpression(
 			GetAllChildrenExpression node) {
 		return context.getFactory().createXML(peekContext());
 	}
 
 	@Override
 	public IValueReference visitGetArrayItemExpression(
 			GetArrayItemExpression node) {
 		final IValueReference array = visit(node.getArray());
 		visit(node.getIndex());
 		if (array != null) {
 			// always just create the ARRAY_OP child (for code completion)
 			IValueReference child = array.getChild(IValueReference.ARRAY_OP);
 			String arrayType = null;
 			if (array.getDeclaredType() != null) {
 				arrayType = (String) array.getDeclaredType().getAttribute(
 						ITypeInferenceContext.GENERIC_ARRAY_TYPE);
 			}
 			else
 			{
 				Set<Type> types = array.getTypes();
 				if (types.size() > 0)
 					arrayType = (String) types.iterator().next()
 							.getAttribute(
 									ITypeInferenceContext.GENERIC_ARRAY_TYPE);
 			}
 			if (arrayType != null && child.getDeclaredType() == null) {
 				child.setDeclaredType(context.getType(arrayType));
 			}
 			if (node.getIndex() instanceof StringLiteral) {
 				child = extractNamedChild(array, node.getIndex());
 				if (arrayType != null && child.getDeclaredType() == null) {
 					child.setDeclaredType(context.getType(arrayType));
 				}
 			}
 			return child;
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitGetLocalNameExpression(
 			GetLocalNameExpression node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitIdentifier(Identifier node) {
 		return peekContext().getChild(node.getName());
 
 	}
 
 	private Boolean evaluateCondition(Expression condition) {
 		if (condition instanceof BooleanLiteral) {
 			return Boolean.valueOf(((BooleanLiteral) condition).getText());
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public IValueReference visitIfStatement(IfStatement node) {
 		visit(node.getCondition());
 		final List<Statement> statements = new ArrayList<Statement>(2);
 		Statement onlyBranch = null;
 		final Boolean condition = evaluateCondition(node.getCondition());
 		if ((condition == null || condition.booleanValue())
 				&& node.getThenStatement() != null) {
 			statements.add(node.getThenStatement());
 			if (condition != null && condition.booleanValue()) {
 				onlyBranch = node.getThenStatement();
 			}
 		}
 		if ((condition == null || !condition.booleanValue())
 				&& node.getElseStatement() != null) {
 			statements.add(node.getElseStatement());
 			if (condition != null && !condition.booleanValue()) {
 				onlyBranch = node.getElseStatement();
 			}
 		}
 		if (!statements.isEmpty()) {
 			if (statements.size() == 1) {
 				if (statements.get(0) == onlyBranch) {
 					visit(statements.get(0));
 				} else {
 					final Branching branching = branching();
 					visit(statements.get(0));
 					branching.end();
 				}
 			} else {
 				final Branching branching = branching();
 				final List<NestedValueCollection> collections = new ArrayList<NestedValueCollection>(
 						statements.size());
 				for (Statement statement : statements) {
 					final NestedValueCollection nestedCollection = new NestedValueCollection(
 							peekContext());
 					enterContext(nestedCollection);
 					visit(statement);
 					leaveContext();
 					collections.add(nestedCollection);
 				}
 				NestedValueCollection.mergeTo(peekContext(), collections);
 				branching.end();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitLabelledStatement(LabelledStatement node) {
 		if (node.getStatement() != null)
 			visit(node.getStatement());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitNewExpression(NewExpression node) {
 		final IValueReference result = new AnonymousValue() {
 			public IValueReference getChild(String name) {
 				if (name.equals(IValueReference.FUNCTION_OP))
 					return this;
 				return super.getChild(name);
 			}
 		};
 		final Expression objectClass = node.getObjectClass();
 		visit(objectClass);
 		final String className = PropertyExpressionUtils.getPath(objectClass);
 
 		if (className != null) {
 			final Type type = resolveJavaScriptType(context, className,
 					peekContext());
 			if (type.getKind() != TypeKind.UNKNOWN) {
 				result.setValue(context.getFactory()
 						.create(peekContext(),
 						type));
 			} else {
 				result.setValue(new LazyReference(context, className,
 						peekContext()));
 			}
 			return result;
 		}
 		result.setValue(context.getFactory().createObject(peekContext()));
 		return result;
 	}
 
 	public static Type resolveJavaScriptType(ITypeInferenceContext context,
 			String className, IValueCollection collection) {
 		if (className == null)
 			return null;
 
 		Type type = context.getType(className);
 		if (type.getKind() == TypeKind.UNKNOWN) {
 			String[] scopes = className.split("\\.");
 			IValueReference functionType = null;
 			for (String scope : scopes) {
 				if (functionType == null) {
 					functionType = collection.getChild(scope);
 				} else {
 					functionType = functionType.getChild(scope);
 				}
 			}
 			if (functionType != null && functionType.exists()) {
 				// test first if it could be a Java type (Packages.xx support)
 				Type javaType = (Type) functionType.getAttribute(
 						IReferenceAttributes.JAVA_OBJECT_TYPE, true);
 				if (javaType != null) {
 					return javaType;
 				}
 				type.setKind(TypeKind.JAVASCRIPT);
 				EList<Member> members = type.getMembers();
 				FunctionValueCollection functionCollection = (FunctionValueCollection) functionType
 						.getAttribute(IReferenceAttributes.FUNCTION_SCOPE, true);
 				IValueReference functionCallChild = null;
 				if (functionCollection != null) {
 					functionCallChild = functionCollection.getThis();
 				} else {
 					functionCallChild = functionType
 							.getChild(IValueReference.FUNCTION_OP);
 				}
 
 				Set<String> functionFields = functionCallChild
 						.getDirectChildren();
 				for (String fieldName : functionFields) {
 					if (fieldName.equals(IValueReference.FUNCTION_OP))
 						continue;
 					IValueReference child = functionCallChild
 							.getChild(fieldName);
 					// test if it is a function.
 					if (child.hasChild(IValueReference.FUNCTION_OP)) {
 						Method method = TypeInfoModelFactory.eINSTANCE
 								.createMethod();
						method.setAttribute(IReferenceAttributes.LOCATION,
								child.getLocation());
 						method.setName(fieldName);
						method.setType(JavaScriptValidations.typeOf(child));
 
 						JSMethod jsmethod = (JSMethod) child.getAttribute(
 								IReferenceAttributes.PARAMETERS, true);
 						if (jsmethod != null
 								&& jsmethod.getParameterCount() > 0) {
 							EList<Parameter> parameters = method
 									.getParameters();
 							List<IParameter> jsParameters = jsmethod
 									.getParameters();
 							for (IParameter jsParameter : jsParameters) {
 								Parameter parameter = TypeInfoModelFactory.eINSTANCE
 										.createParameter();
 								parameter.setKind(ParameterKind.OPTIONAL);
 								parameter.setType(jsParameter.getType());
 								parameter.setName(jsParameter.getName());
 								parameters.add(parameter);
 							}
 						}
 						members.add(method);
 					} else {
 						Property property = TypeInfoModelFactory.eINSTANCE
 								.createProperty();
 						property.setAttribute(IReferenceAttributes.LOCATION,
 								child.getLocation());
 						property.setName(fieldName);
 						property.setType(JavaScriptValidations.typeOf(child));
 						members.add(property);
 					}
 				}
 				return type;
 			}
 		}
 		return type;
 	}
 
 	@Override
 	public IValueReference visitNullExpression(NullExpression node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitObjectInitializer(ObjectInitializer node) {
 		final IValueReference result = new AnonymousValue();
 		for (ObjectInitializerPart part : node.getInitializers()) {
 			if (part instanceof PropertyInitializer) {
 				final PropertyInitializer pi = (PropertyInitializer) part;
 				final IValueReference child = extractNamedChild(result,
 						pi.getName());
 				final IValueReference value = visit(pi.getValue());
 				if (child != null) {
 					child.setValue(value);
 					child.setLocation(ReferenceLocation.create(getSource(), pi
 							.getName().sourceStart(), pi.getName().sourceEnd()));
 					if (child.getKind() == ReferenceKind.UNKNOWN) {
 						child.setKind(ReferenceKind.LOCAL);
 					}
 				}
 			} else {
 				// TODO handle get/set methods
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public IValueReference visitParenthesizedExpression(
 			ParenthesizedExpression node) {
 		return visit(node.getExpression());
 	}
 
 	@Override
 	public IValueReference visitPropertyExpression(PropertyExpression node) {
 		final IValueReference object = visit(node.getObject());
 		Expression property = node.getProperty();
 		IValueReference child = extractNamedChild(object, property);
 		if (child != null && node.getObject() instanceof ThisExpression) {
 			// TODO check is this also a local reference kind or should this be
 			// a special one?
 			child.setKind(ReferenceKind.LOCAL);
 			child.setLocation(ReferenceLocation.create(getSource(),
 					node.sourceStart(), node.sourceEnd(),
 					property.sourceStart(), property.sourceEnd()));
 		}
 		return child;
 	}
 
 	protected IValueReference extractNamedChild(IValueReference parent,
 			Expression name) {
 		if (parent != null) {
 			final String nameStr;
 			if (name instanceof Identifier) {
 				nameStr = ((Identifier) name).getName();
 			} else if (name instanceof StringLiteral) {
 				nameStr = ((StringLiteral) name).getValue();
 			} else if (name instanceof XmlAttributeIdentifier) {
 				nameStr = ((XmlAttributeIdentifier) name).getAttributeName();
 			} else {
 				return null;
 			}
 			return parent.getChild(nameStr);
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitRegExpLiteral(RegExpLiteral node) {
 		return context.getFactory().createRegExp(peekContext());
 	}
 
 	@Override
 	public IValueReference visitReturnStatement(ReturnStatement node) {
 		if (node.getValue() != null) {
 			final IValueReference value = visit(node.getValue());
 			if (value != null) {
 				final IValueReference returnValue = peekContext()
 						.getReturnValue();
 				if (returnValue != null) {
 					returnValue.addValue(value, true);
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitScript(Script node) {
 		return visit(node.getStatements());
 	}
 
 	@Override
 	public IValueReference visitSimpleType(SimpleType node) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IValueReference visitStatementBlock(StatementBlock node) {
 		for (Statement statement : node.getStatements()) {
 			visit(statement);
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitStringLiteral(StringLiteral node) {
 		return context.getFactory().createString(peekContext());
 	}
 
 	@Override
 	public IValueReference visitSwitchStatement(SwitchStatement node) {
 		if (node.getCondition() != null)
 			visit(node.getCondition());
 		for (SwitchComponent component : node.getCaseClauses()) {
 			if (component instanceof CaseClause) {
 				visit(((CaseClause) component).getCondition());
 			}
 			visit(component.getStatements());
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitThisExpression(ThisExpression node) {
 		return peekContext().getThis();
 	}
 
 	@Override
 	public IValueReference visitThrowStatement(ThrowStatement node) {
 		visit(node.getException());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitTryStatement(TryStatement node) {
 		visit(node.getBody());
 		for (CatchClause catchClause : node.getCatches()) {
 			final NestedValueCollection collection = new NestedValueCollection(
 					peekContext());
 			collection.createChild(catchClause.getException().getName());
 			enterContext(collection);
 			visit(catchClause.getStatement());
 			leaveContext();
 		}
 		if (node.getFinally() != null) {
 			visit(node.getFinally().getStatement());
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitTypeOfExpression(TypeOfExpression node) {
 		return context.getFactory().createString(peekContext());
 	}
 
 	@Override
 	public IValueReference visitUnaryOperation(UnaryOperation node) {
 		return visit(node.getExpression());
 	}
 
 	@Override
 	public IValueReference visitVariableStatment(VariableStatement node) {
 		final IValueCollection context = peekContext();
 		IValueReference result = null;
 		for (VariableDeclaration declaration : node.getVariables()) {
 			result = createVariable(context, declaration);
 		}
 		return result;
 	}
 
 	@Override
 	public IValueReference visitVoidExpression(VoidExpression node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitVoidOperator(VoidOperator node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitWhileStatement(WhileStatement node) {
 		if (node.getCondition() != null)
 			visit(node.getCondition());
 		if (node.getBody() != null)
 			visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitWithStatement(WithStatement node) {
 		final IValueReference with = visit(node.getExpression());
 		if (with != null) {
 			final WithValueCollection withCollection = new WithValueCollection(
 					peekContext(), with);
 			enterContext(withCollection);
 			visit(node.getStatement());
 			leaveContext();
 		} else {
 			visit(node.getStatement());
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitXmlLiteral(XmlLiteral node) {
 		IValueReference xmlValueReference = context.getFactory().createXML(
 				peekContext());
 
 		if (xmlValueReference instanceof IValueProvider) {
 			Type xmlType = context.getKnownType(ITypeNames.XML);
 			IValue xmlValue = ((IValueProvider) xmlValueReference).getValue();
 			List<XmlFragment> fragments = node.getFragments();
 			StringBuilder xml = new StringBuilder();
 			for (XmlFragment xmlFragment : fragments) {
 				if (xmlFragment instanceof XmlTextFragment
 						&& !((XmlTextFragment) xmlFragment).getXml().equals(
 								"<></>")) {
 					xml.append(((XmlTextFragment) xmlFragment).getXml());
 				} else if (xmlFragment instanceof XmlExpressionFragment) {
 					xml.append("\"\"");
 				}
 			}
 
 			if (xml.length() > 0) {
 				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
 						.newInstance();
 				try {
 					DocumentBuilder docBuilder = docBuilderFactory
 							.newDocumentBuilder();
 					Document doc = docBuilder.parse(new InputSource(
 							new StringReader(xml.toString())));
 					NodeList nl = doc.getChildNodes();
 					if (nl.getLength() == 1) {
 						Node item = nl.item(0);
 						NamedNodeMap attributes = item.getAttributes();
 						for (int a = 0; a < attributes.getLength(); a++) {
 							Node attribute = attributes.item(a);
 							xmlValue.createChild("@" + attribute.getNodeName());
 						}
 						createXmlChilds(xmlType, xmlValue, item.getChildNodes());
 					} else {
 						System.err.println("root should be 1 child?? " + xml);
 					}
 				} catch (Exception e) {
 				}
 			}
 		}
 		return xmlValueReference;
 	}
 
 	/**
 	 * @param xmlType
 	 * @param xmlValue
 	 * @param nl
 	 */
 	private void createXmlChilds(Type xmlType, IValue xmlValue, NodeList nl) {
 		for (int i = 0; i < nl.getLength(); i++) {
 			Node item = nl.item(i);
 			if (item.getNodeType() == Node.TEXT_NODE) {
 				String value = item.getNodeValue();
 				if (value == null || "".equals(value.trim())) {
 					continue;
 				}
 			}
 			IValue nodeValue = xmlValue.createChild(item.getNodeName());
 			nodeValue.setDeclaredType(xmlType);
 			NamedNodeMap attributes = item.getAttributes();
 			for (int a = 0; a < attributes.getLength(); a++) {
 				Node attribute = attributes.item(a);
 				nodeValue.createChild("@" + attribute.getNodeName());
 			}
 			createXmlChilds(xmlType, nodeValue, item.getChildNodes());
 		}
 	}
 
 	@Override
 	public IValueReference visitXmlPropertyIdentifier(
 			XmlAttributeIdentifier node) {
 		return context.getFactory().createXML(peekContext());
 	}
 
 	@Override
 	public IValueReference visitYieldOperator(YieldOperator node) {
 		final IValueReference value = visit(node.getExpression());
 		if (value != null) {
 			final IValueReference reference = peekContext().getReturnValue();
 			if (reference != null) {
 				reference.addValue(value, true);
 			}
 		}
 		return null;
 	}
 
 }
