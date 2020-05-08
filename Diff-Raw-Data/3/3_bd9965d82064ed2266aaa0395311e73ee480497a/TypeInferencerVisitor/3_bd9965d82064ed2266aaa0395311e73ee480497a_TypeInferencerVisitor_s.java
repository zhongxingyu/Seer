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
 
 import static org.eclipse.dltk.javascript.typeinfo.ITypeNames.NUMBER;
 import static org.eclipse.dltk.javascript.typeinfo.ITypeNames.OBJECT;
 import static org.eclipse.dltk.javascript.typeinfo.ITypeNames.STRING;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.internal.javascript.validation.JavaScriptValidations;
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
 import org.eclipse.dltk.javascript.typeinfo.IMemberEvaluator;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IParameter;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IVariable;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.ReferenceSource;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.ArrayType;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.MapType;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeRef;
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
 
 	public ReferenceSource getSource() {
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
 
 	private static final int K_NUMBER = 1;
 	private static final int K_STRING = 2;
 	private static final int K_OTHER = 4;
 
 	@Override
 	public IValueReference visitArrayInitializer(ArrayInitializer node) {
 		int kind = 0;
 		for (ASTNode astNode : node.getItems()) {
 			if (astNode instanceof StringLiteral) {
 				kind |= K_STRING;
 			} else if (astNode instanceof DecimalLiteral) {
 				kind |= K_NUMBER;
 			} else if (astNode instanceof NullExpression) {
 				// ignore
 			} else {
 				kind |= K_OTHER;
 				visit(astNode);
 			}
 		}
 		if (kind == K_STRING) {
 			return context.getFactory().create(peekContext(),
 					TypeUtil.arrayOf(context.getTypeRef(STRING)));
 		} else if (kind == K_NUMBER) {
 			return context.getFactory().create(peekContext(),
 					TypeUtil.arrayOf(context.getTypeRef(NUMBER)));
 		} else {
 			return context.getFactory().createArray(peekContext());
 		}
 	}
 
 	@Override
 	public IValueReference visitAsteriskExpression(AsteriskExpression node) {
 		return context.getFactory().createXMLList(peekContext());
 	}
 
 	@Override
 	public IValueReference visitBinaryOperation(BinaryOperation node) {
 		final IValueReference left = visit(node.getLeftExpression());
 		final IValueReference right = visit(node.getRightExpression());
 		final int op = node.getOperation();
 		if (JSParser.ASSIGN == op) {
 			return visitAssign(left, right, node);
 		} else if (left == null && right instanceof ConstantValue) {
 			return right;
 		} else if (op == JSParser.LAND) {
 			return coalesce(right, left);
 		} else if (op == JSParser.GT || op == JSParser.GTE || op == JSParser.LT
 				|| op == JSParser.LTE || op == JSParser.NSAME
 				|| op == JSParser.SAME || op == JSParser.NEQ
 				|| op == JSParser.EQ) {
 			return context.getFactory().createBoolean(peekContext());
 		} else if (isNumber(left) && isNumber(right)) {
 			return context.getFactory().createNumber(peekContext());
 		} else if (op == JSParser.ADD) {
 			if (isString(left) || isString(right)) {
 				return context.getFactory().createString(peekContext());
 			}
 			return left;
 		} else if (JSParser.INSTANCEOF == op) {
 			return context.getFactory().createBoolean(peekContext());
 		} else if (JSParser.LOR == op) {
 			final JSTypeSet typeSet = JSTypeSet.create();
 			if (left != null) {
 				typeSet.addAll(left.getDeclaredTypes());
 				typeSet.addAll(left.getTypes());
 			}
 			if (right != null) {
 				typeSet.addAll(right.getDeclaredTypes());
 				typeSet.addAll(right.getTypes());
 			}
 			return new ConstantValue(typeSet);
 		} else {
 			// TODO handle other operations
 			return null;
 		}
 	}
 
 	private static IValueReference coalesce(IValueReference v1,
 			IValueReference v2) {
 		return v1 != null ? v1 : v2;
 	}
 
 	private boolean isNumber(IValueReference ref) {
 		if (ref != null) {
 			final Type numType = context.getType(NUMBER);
 			if (ref.getTypes().contains(numType))
 				return true;
 			if (numType.equals(ref.getDeclaredType()))
 				return true;
 		}
 		return false;
 	}
 
 	private boolean isString(IValueReference ref) {
 		if (ref != null) {
 			final Type strType = context.getType(STRING);
 			if (ref.getTypes().contains(strType))
 				return true;
 			if (strType.equals(ref.getDeclaredType()))
 				return true;
 		}
 		return false;
 	}
 
 	protected IValueReference visitAssign(IValueReference left,
 			IValueReference right, BinaryOperation node) {
 		if (left != null) {
 			if (IValueReference.ARRAY_OP.equals(left.getName())
 					&& node.getLeftExpression() instanceof GetArrayItemExpression) {
 				GetArrayItemExpression arrayItemExpression = (GetArrayItemExpression) node
 						.getLeftExpression();
 				IValueReference namedChild = extractNamedChild(
 						left.getParent(), arrayItemExpression.getIndex());
 				if (namedChild != null) {
 					assign(namedChild, right);
 				} else {
 					assign(left, right);
 				}
 			} else {
				assign(left, right);
 			}
 		}
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
 			IValueReference constant = createVariable(context, declaration);
 			if (constant != null)
 				constant.setAttribute(IReferenceAttributes.CONSTANT,
 						Boolean.TRUE);
 		}
 		return null;
 	}
 
 	protected IValueReference createVariable(IValueCollection context,
 			VariableDeclaration declaration) {
 		final Identifier identifier = declaration.getIdentifier();
 		final String varName = identifier.getName();
 		final IValueReference reference = context.createChild(varName);
 		final JSVariable variable = new JSVariable();
 		variable.setName(declaration.getVariableName());
 		final org.eclipse.dltk.javascript.ast.Type varType = declaration
 				.getType();
 		if (varType != null) {
 			variable.setType(resolveType(varType));
 		}
 		if (declaration.getParent() instanceof VariableStatement) {
 			final VariableStatement statement = (VariableStatement) declaration
 					.getParent();
 			for (IModelBuilder extension : this.context.getModelBuilders()) {
 				extension.processVariable(statement, variable, reporter,
 						getJSDocTypeChecker());
 			}
 		}
 		if (varType == null)
 			setTypeImpl(reference, variable.getType());
 		else
 			setType(identifier, reference, variable.getType(), true);
 		reference.setAttribute(IReferenceAttributes.VARIABLE, variable);
 
 		reference.setKind(inFunction() ? ReferenceKind.LOCAL
 				: ReferenceKind.GLOBAL);
 		reference.setLocation(ReferenceLocation.create(getSource(),
 				declaration.sourceStart(), declaration.sourceEnd(),
 				identifier.sourceStart(), identifier.sourceEnd()));
 		initializeVariable(reference, declaration, variable);
 		return reference;
 	}
 
 	protected void initializeVariable(final IValueReference reference,
 			VariableDeclaration declaration, IVariable variable) {
 		if (declaration.getInitializer() != null) {
 			IValueReference assignment = visit(declaration.getInitializer());
 			if (assignment != null) {
 				assign(reference, assignment);
 				if (assignment.getKind() == ReferenceKind.FUNCTION
 						&& reference
 								.getAttribute(IReferenceAttributes.PARAMETERS) != null)
 					reference.setKind(ReferenceKind.FUNCTION);
 			}
 		}
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
 		IValueReference itemReference = visit(node.getItem());
 		IValueReference iteratorReference = visit(node.getIterator());
 		JSType type = JavaScriptValidations.typeOf(iteratorReference);
 		if (type != null) {
 			if (type instanceof ArrayType
 				&& JavaScriptValidations.typeOf(itemReference) == null) {
 				final JSType itemType = ((ArrayType) type).getItemType();
 				itemReference.setDeclaredType(itemType);
 			} else if (type instanceof MapType
 					&& JavaScriptValidations.typeOf(itemReference) == null) {
 				final JSType itemType = ((MapType) type).getValueType();
 				itemReference.setDeclaredType(itemType);
 			} else if (type.getName().endsWith(ITypeNames.XMLLIST)) {
 				itemReference.setDeclaredType(type);
 			}
 		}
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
 		final JSMethod method = generateJSMethod(node);
 		final IValueCollection function = new FunctionValueCollection(
 				peekContext(), method.getName());
 
 		for (IParameter parameter : method.getParameters()) {
 			final IValueReference refArg = function.createChild(parameter
 					.getName());
 			refArg.setKind(ReferenceKind.ARGUMENT);
 			if (parameter.getPropertiesType() != null) {
 				refArg.setDeclaredType(TypeUtil.ref(parameter
 						.getPropertiesType()));
 			} else {
 				// call directly the impl else unknown type is reported twice if
 				// used in jsdoc,
 				// but when it is declared in code itself it will now fail..
 				setTypeImpl(refArg, parameter.getType());
 			}
 			refArg.setLocation(parameter.getLocation());
 		}
 		final Identifier methodName = node.getName();
 		final IValueReference result;
 		if (isChildFunction(node)) {
 			result = peekContext().createChild(method.getName());
 		} else {
 			result = new AnonymousValue();
 		}
 		result.setLocation(method.getLocation());
 		result.setKind(ReferenceKind.FUNCTION);
 		result.setDeclaredType(context.getTypeRef(ITypeNames.FUNCTION));
 		result.setAttribute(IReferenceAttributes.PARAMETERS, method);
 		result.setAttribute(IReferenceAttributes.FUNCTION_SCOPE, function);
 		enterContext(function);
 		try {
 			visitFunctionBody(node);
 		} finally {
 			leaveContext();
 		}
 		final IValueReference returnValue = result
 				.getChild(IValueReference.FUNCTION_OP);
 		returnValue.addValue(function.getReturnValue(), true);
 		if (node.getReturnType() == null)
 			setTypeImpl(returnValue, method.getType());
 		else
 			setType(methodName != null ? methodName : node.getFunctionKeyword(),
 					returnValue, method.getType(), true);
 		return result;
 	}
 
 	/**
 	 * @param node
 	 * @param methodName
 	 * @return
 	 */
 	protected static boolean isChildFunction(FunctionStatement node) {
 		return node.getName() != null
 				&& !(node.getParent() instanceof BinaryOperation)
 				&& !(node.getParent() instanceof VariableDeclaration)
 				&& !(node.getParent() instanceof PropertyInitializer)
 				&& !(node.getParent() instanceof NewExpression);
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	protected JSMethod generateJSMethod(FunctionStatement node) {
 		final JSMethod method = new JSMethod(node, getSource());
 		for (IModelBuilder extension : context.getModelBuilders()) {
 			extension.processMethod(node, method, reporter,
 					getJSDocTypeChecker());
 		}
 		return method;
 	}
 
 	public void visitFunctionBody(FunctionStatement node) {
 		visit(node.getBody());
 	}
 
 	protected JSType resolveType(org.eclipse.dltk.javascript.ast.Type type) {
 		return context.getTypeRef(type.getName());
 	}
 
 	public void setType(ASTNode node, IValueReference value, JSType type,
 			boolean lazy) {
 		setTypeImpl(value, type);
 	}
 
 	private void setTypeImpl(IValueReference value, JSType type) {
 		if (type != null) {
 			type = context.resolveTypeRef(type);
 			Assert.isTrue(type.getKind() != TypeKind.UNRESOLVED);
 			if (type.getKind() != TypeKind.UNKNOWN) {
 				value.setDeclaredType(type);
 				if (type instanceof TypeRef && value instanceof IValueProvider) {
 					for (IMemberEvaluator evaluator : TypeInfoManager
 							.getMemberEvaluators()) {
 						final IValueCollection collection = evaluator.valueOf(
 								context, ((TypeRef) type).getTarget());
 						if (collection != null) {
 							if (collection instanceof IValueProvider) {
 								((IValueProvider)value).getValue().addValue(((IValueProvider) collection)
 										.getValue());
 							}
 						}
 					}
 				}
 			} else if (type instanceof TypeRef) {
 				value.addValue(new LazyTypeReference(context, type.getName(),
 						peekContext()), false);
 			}
 		}
 	}
 
 	@Override
 	public IValueReference visitGetAllChildrenExpression(
 			GetAllChildrenExpression node) {
 		return context.getFactory().createXMLList(peekContext());
 	}
 
 	@Override
 	public IValueReference visitGetArrayItemExpression(
 			GetArrayItemExpression node) {
 		final IValueReference array = visit(node.getArray());
 		visit(node.getIndex());
 		if (array != null) {
 			// always just create the ARRAY_OP child (for code completion)
 			IValueReference child = array.getChild(IValueReference.ARRAY_OP);
 			JSType arrayType = null;
 			if (array.getDeclaredType() != null) {
 				arrayType = TypeUtil.extractArrayItemType(array
 						.getDeclaredType());
 			} else {
 				JSTypeSet types = array.getTypes();
 				if (types.size() > 0)
 					arrayType = TypeUtil.extractArrayItemType(types.getFirst());
 			}
 			if (arrayType != null && child.getDeclaredType() == null) {
 				child.setDeclaredType(arrayType);
 			}
 			if (node.getIndex() instanceof StringLiteral) {
 				IValueReference namedChild = extractNamedChild(array,
 						node.getIndex());
 				if (namedChild.exists()) {
 					child = namedChild;
 					if (arrayType != null
 							&& namedChild.getDeclaredType() == null) {
 						namedChild.setDeclaredType(arrayType);
 					}
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
 
 	protected static class AnonymousNewValue extends AnonymousValue {
 		@Override
 		public IValueReference getChild(String name) {
 			if (name.equals(IValueReference.FUNCTION_OP))
 				return this;
 			return super.getChild(name);
 		}
 	}
 
 	@Override
 	public IValueReference visitNewExpression(NewExpression node) {
 		Expression objectClass = node.getObjectClass();
 		if (objectClass instanceof CallExpression) {
 			final CallExpression call = (CallExpression) objectClass;
 			for (ASTNode argument : call.getArguments()) {
 				visit(argument);
 			}
 			objectClass = call.getExpression();
 		}
 		IValueReference visit = visit(objectClass);
 
 		IValueReference result = null;
 		if (visit != null) {
 			if (visit.getKind() == ReferenceKind.FUNCTION) {
 				Object fs = visit
 						.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 				if (fs instanceof IValueCollection
 						&& ((IValueCollection) fs).getThis() != null) {
 					result = new AnonymousNewValue();
 					result.setValue(((IValueCollection) fs).getThis());
 					result.setKind(ReferenceKind.TYPE);
 					String className = PropertyExpressionUtils
 							.getPath(objectClass);
 					if (className != null) {
 						Type type = TypeInfoModelFactory.eINSTANCE.createType();
 						type.setSuperType(context.getKnownType(OBJECT));
 						type.setKind(TypeKind.JAVASCRIPT);
 						type.setName(className);
 						result.setDeclaredType(TypeUtil.ref(type));
 					} else {
 						result.setDeclaredType(context.getTypeRef(OBJECT));
 					}
 				}
 			} else if (visit.exists()) {
 				for (JSType type : visit.getDeclaredTypes()) {
 					if (type instanceof TypeRef && ((TypeRef) type).isStatic()) {
 						result = new AnonymousNewValue();
 						result.setKind(ReferenceKind.TYPE);
 						result.setDeclaredType(TypeUtil.ref(((TypeRef) type)
 								.getTarget()));
 						return result;
 					}
 				}
 				for (JSType type : visit.getTypes()) {
 					if (type instanceof TypeRef && ((TypeRef) type).isStatic()) {
 						result = new AnonymousNewValue();
 						result.setKind(ReferenceKind.TYPE);
 						result.setDeclaredType(TypeUtil.ref(((TypeRef) type)
 								.getTarget()));
 						return result;
 					}
 				}
 			}
 		}
 		if (result == null) {
 			final String className = PropertyExpressionUtils
 					.getPath(objectClass);
 			IValueCollection contextValueCollection = peekContext();
 			if (className != null) {
 				Type knownType = context.getKnownType(className);
 				if (knownType != null) {
 					result = new AnonymousNewValue();
 					result.setValue(context.getFactory().create(
 							contextValueCollection, TypeUtil.ref(knownType)));
 					result.setKind(ReferenceKind.TYPE);
 				} else {
 					result = new LazyTypeReference(context, className,
 							contextValueCollection);
 				}
 			} else {
 				result = new AnonymousNewValue();
 				result.setValue(context.getFactory().createObject(
 						contextValueCollection));
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public IValueReference visitNullExpression(NullExpression node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitObjectInitializer(ObjectInitializer node) {
 		final IValueReference result = new AnonymousValue();
 		result.setDeclaredType(TypeUtil.ref(context.getKnownType(OBJECT)));
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
 						child.setKind(ReferenceKind.FIELD);
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
 		if (child != null && node.getObject() instanceof ThisExpression
 				&& !child.exists()) {
 			if (isFunctionDeclaration(node))
 				child.setKind(ReferenceKind.FUNCTION);
 			else
 				child.setKind(ReferenceKind.FIELD);
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
 				if (((XmlAttributeIdentifier) name).getExpression() instanceof AsteriskExpression) {
 					return visitAsteriskExpression((AsteriskExpression) ((XmlAttributeIdentifier) name)
 							.getExpression());
 				} else {
 					nameStr = ((XmlAttributeIdentifier) name)
 							.getAttributeName();
 				}
 			} else if (name instanceof AsteriskExpression) {
 				return visitAsteriskExpression((AsteriskExpression) name);
 			}else {
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
 					returnValue.addValue(value,
 							!(value instanceof LazyTypeReference));
 				}
 			}
 			return value;
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
 		if (node.getException() != null)
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
 			if (catchClause.getStatement() != null) {
 				visit(catchClause.getStatement());
 			}
 			leaveContext();
 		}
 		if (node.getFinally() != null) {
 			visit(node.getFinally().getStatement());
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitTypeOfExpression(TypeOfExpression node) {
 		visit(node.getExpression());
 		return context.getFactory().createString(peekContext());
 	}
 
 	@Override
 	public IValueReference visitUnaryOperation(UnaryOperation node) {
 		return visit(node.getExpression());
 	}
 
 	@Override
 	public IValueReference visitVariableStatement(VariableStatement node) {
 		final IValueCollection collection = peekContext();
 		IValueReference result = null;
 		for (VariableDeclaration declaration : node.getVariables()) {
 			result = createVariable(collection, declaration);
 			final JSVariable variable = new JSVariable();
 			variable.setName(declaration.getVariableName());
 			if (result.getDeclaredType() != null)
 				variable.setType(result.getDeclaredType());
 			for (IModelBuilder extension : context.getModelBuilders()) {
 				extension.processVariable(node, variable, reporter,
 						getJSDocTypeChecker());
 			}
 			if (result.getDeclaredType() == null && variable.getType() != null) {
 				result.setDeclaredType(variable.getType());
 			}
 			result.setAttribute(IReferenceAttributes.VARIABLE, variable);
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
 
 	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
 			.newInstance();
 	private DocumentBuilder docBuilder;
 
 	/**
 	 * @return
 	 * @throws ParserConfigurationException
 	 */
 	private DocumentBuilder getDocumentBuilder()
 			throws ParserConfigurationException {
 		if (docBuilder == null)
 			docBuilder = docBuilderFactory.newDocumentBuilder();
 		return docBuilder;
 	}
 
 	@Override
 	public IValueReference visitXmlLiteral(XmlLiteral node) {
 		IValueReference xmlValueReference = context.getFactory().createXML(
 				peekContext());
 
 		if (xmlValueReference instanceof IValueProvider) {
 			JSType xmlType = TypeUtil.ref(context.getKnownType(ITypeNames.XML));
 			IValue xmlValue = ((IValueProvider) xmlValueReference).getValue();
 			List<XmlFragment> fragments = node.getFragments();
 			StringBuilder xml = new StringBuilder();
 			for (XmlFragment xmlFragment : fragments) {
 				if (xmlFragment instanceof XmlTextFragment) {
 					String xmlText = ((XmlTextFragment) xmlFragment).getXml();
 					if (xmlText.equals("<></>"))
 						continue;
 					if (xmlText.startsWith("<>") && xmlText.endsWith("</>")) {
 						xmlText = "<xml>"
 								+ xmlText.substring(2, xmlText.length() - 3)
 								+ "</xml>";
 					}
 					xml.append(xmlText);
 				} else if (xmlFragment instanceof XmlExpressionFragment) {
 					Expression expression = ((XmlExpressionFragment) xmlFragment)
 							.getExpression();
 					visit(expression);
 					if (xml.charAt(xml.length() - 1) == '<'
 							|| xml.subSequence(xml.length() - 2, xml.length())
 									.equals("</")) {
 						if (expression instanceof Identifier) {
 							xml.append(((Identifier) expression).getName());
 						} else {
 							xml.setLength(0);
 							break;
 						}
 					} else
 						xml.append("\"\"");
 				}
 			}
 
 			if (xml.length() > 0) {
 				try {
 					DocumentBuilder docBuilder = getDocumentBuilder();
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
 	private void createXmlChilds(JSType xmlType, IValue xmlValue, NodeList nl) {
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
 			if (attributes != null) {
 				for (int a = 0; a < attributes.getLength(); a++) {
 					Node attribute = attributes.item(a);
 					nodeValue.createChild("@" + attribute.getNodeName());
 				}
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
 
 	public static boolean isFunctionDeclaration(Expression expression) {
 		PropertyExpression pe = null;
 		if (expression instanceof PropertyExpression)
 			pe = (PropertyExpression) expression;
 		else if (expression.getParent() instanceof PropertyExpression)
 			pe = (PropertyExpression) expression.getParent();
 		if (pe != null && pe.getObject() instanceof ThisExpression
 				&& pe.getParent() instanceof BinaryOperation) {
 			return ((BinaryOperation) pe.getParent()).getRightExpression() instanceof FunctionStatement;
 		}
 		return false;
 	}
 
 }
