 package org.ita.neutrino.astparser;
 
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.eclipse.jdt.core.dom.PrefixExpression;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.ita.neutrino.codeparser.Invokable;
 import org.ita.neutrino.codeparser.LiteralExpression;
 import org.ita.neutrino.codeparser.Method;
 import org.ita.neutrino.codeparser.Package;
 import org.ita.neutrino.codeparser.ParserException;
 import org.ita.neutrino.codeparser.SourceFile;
 import org.ita.neutrino.codeparser.Statement;
 import org.ita.neutrino.codeparser.Type;
 import org.ita.neutrino.codeparser.VariableDeclarationStatement;
 
 class BlockParser {
 
 	private class UnsupportedSintaxException extends ParserException {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 3227126996063055111L;
 
 		public UnsupportedSintaxException() {
 			super("Sintax não suportada.");
 		}
 	}
 
 	private ASTBlock block;
 	QuickVisitor quickVisitor = new QuickVisitor();
 	private ASTEnvironment environment;
 	private boolean wrappAsGenericStatement;
 
 	public void setBlock(ASTBlock block) {
 		this.block = block;
 		environment = getEnvironment();
 	}
 
 	public void parse() throws ParserException {
 		List<ASTNode> nodes = quickVisitor.quickVisit(block.getASTObject());
 
 		for (ASTNode node : nodes) {
 			parseStatement(node);
 		}
 	}
 
 	private void parseStatement(ASTNode node) throws ParserException {
 //		Debug purposes
//		ConsoleVisitor.showNodes(node);
 
 		Statement statement = null;
 
 		wrappAsGenericStatement = false;
 
 		if (node instanceof org.eclipse.jdt.core.dom.VariableDeclarationStatement) {
 			statement = parseVariableDeclaration((org.eclipse.jdt.core.dom.VariableDeclarationStatement) node);
 		} else if (node instanceof org.eclipse.jdt.core.dom.ExpressionStatement) {
 			statement = parseExpression((org.eclipse.jdt.core.dom.ExpressionStatement) node);
 		} else if (node instanceof org.eclipse.jdt.core.dom.Statement) {
 			wrappAsGenericStatement = true;
 		} else {
 			throw new UnsupportedSintaxException();
 		}
 
 		if (wrappAsGenericStatement) {
 			statement = parseGenericStatement((org.eclipse.jdt.core.dom.Statement) node);
 		}
 		block.getStatementList().add(statement);
 
 		ASTSelection selection = environment.getSelection();
 
 		if (selection.isOverNode(node)) {
 			selection.setSelectedElement(statement);
 		}
 	}
 
 	private Statement parseExpression(ExpressionStatement node) {
 		Expression astExpression = node.getExpression();
 
 		if (astExpression instanceof org.eclipse.jdt.core.dom.MethodInvocation) {
 			org.eclipse.jdt.core.dom.MethodInvocation astMethodInvocation = (org.eclipse.jdt.core.dom.MethodInvocation) astExpression;
 
 			ASTMethodInvocationStatement methodInvocation = block.createMethodInvocationStatement();
 
 			methodInvocation.setASTObject(node);
 
 			populateParameterList(methodInvocation.getParameterList(), astMethodInvocation.arguments());
 
 			String methodTypeQualifiedName = astMethodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName();
 			
 			String methodName = astMethodInvocation.getName().getIdentifier();
 
 			Type type = environment.getTypeCache().get(methodTypeQualifiedName);
 
 			Method method = type.getOrCreateMethod(methodName);
 
 			methodInvocation.setCalledMethod(method);
 
 			return methodInvocation;
 		} else {
 			wrappAsGenericStatement = true;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Faz o parsing de uma declaração de variável.
 	 * 
 	 * @param node
 	 * @return
 	 * @throws ParserException
 	 */
 	private VariableDeclarationStatement parseVariableDeclaration(org.eclipse.jdt.core.dom.VariableDeclarationStatement node) throws ParserException {
 		List<ASTNode> nodes = quickVisitor.quickVisit(node);
 
 		if (!(nodes.get(0) instanceof org.eclipse.jdt.core.dom.Type)) {
 			throw new UnsupportedSintaxException();
 		}
 
 		org.eclipse.jdt.core.dom.Type variableTypeNode = (org.eclipse.jdt.core.dom.Type) nodes.get(0);
 
 		if (!(nodes.get(1) instanceof VariableDeclarationFragment)) {
 			throw new UnsupportedSintaxException();
 		}
 
 		VariableDeclarationFragment variableFragment = (VariableDeclarationFragment) nodes.get(1);
 
 		List<ASTNode> fragmentNodes = quickVisitor.quickVisit(variableFragment);
 
 		if (!(fragmentNodes.get(0) instanceof SimpleName)) {
 			throw new UnsupportedSintaxException();
 		}
 
 		SimpleName variableNameNode = (SimpleName) fragmentNodes.get(0);
 
 		Type variableType = environment.getTypeCache().get(variableTypeNode.resolveBinding().getQualifiedName());
 
 		String variableName = variableNameNode.getIdentifier();
 
 		ASTVariableDeclarationStatement variableDeclaration = block.createVariableDeclaration(variableName);
 
 		variableDeclaration.setVariableType(variableType);
 
		variableDeclaration.setASTObject(variableFragment);
 
 		// Inicialização da variável
 		if (fragmentNodes.size() > 1) {
 
 			// Expressão literal
 			if (fragmentNodes.get(1) instanceof org.eclipse.jdt.core.dom.PrefixExpression) {
 				org.eclipse.jdt.core.dom.PrefixExpression astNode = (PrefixExpression) fragmentNodes.get(1);
 
 				Type nodeType = environment.getTypeCache().get(astNode.resolveTypeBinding().getQualifiedName());
 
 				ASTLiteralExpression literalExpression = environment.getExpressionFactory().createLiteralExpression(nodeType, astNode.toString());
 
 				literalExpression.setASTObject(astNode);
 
 				variableDeclaration.setInitializationExpression(literalExpression);
 
 				// Variável inicializada por método
 			} else if (fragmentNodes.get(1) instanceof org.eclipse.jdt.core.dom.MethodInvocation) {
 				org.eclipse.jdt.core.dom.MethodInvocation astNode = (org.eclipse.jdt.core.dom.MethodInvocation) fragmentNodes.get(1);
 
 				String methodSignatureString = ASTEnvironment.getMethodSignature(astNode);
 
 				ASTMethodInvocationExpression mie = environment.createMethodInvocationExpression(methodSignatureString);
 
 				mie.setASTObject(node);
 
 				populateParameterList(mie.getParameterList(), astNode.arguments());
 
 				variableDeclaration.setInitializationExpression(mie);
 
 				// Variável inicializada com null
 			} else if (fragmentNodes.get(1) instanceof org.eclipse.jdt.core.dom.NullLiteral) {
 				variableDeclaration.setInitializationExpression(LiteralExpression.NULL_EXPRESSION);
 
 			} else if (fragmentNodes.get(1) instanceof org.eclipse.jdt.core.dom.ClassInstanceCreation) {
 				org.eclipse.jdt.core.dom.ClassInstanceCreation astNode = (org.eclipse.jdt.core.dom.ClassInstanceCreation) fragmentNodes.get(1);
 
 				String constructorSignatureString = ASTEnvironment.getConstructorSignature(astNode);
 
 				ASTConstructorInvocationExpression cie = environment.createConstructorInvocationExpression(constructorSignatureString);
 
 				cie.setASTObject(astNode);
 
 				variableDeclaration.setInitializationExpression(cie);
 			} else if (fragmentNodes.get(1) instanceof org.eclipse.jdt.core.dom.Expression) {
 				org.ita.neutrino.codeparser.Expression expression = parseExpression((org.eclipse.jdt.core.dom.Expression) fragmentNodes.get(1));
 
 				variableDeclaration.setInitializationExpression(expression);
 			} else {
 				throw new UnsupportedSintaxException();
 			}
 		}
 
 		return variableDeclaration;
 	}
 
 	private org.ita.neutrino.codeparser.Expression parseExpression(org.eclipse.jdt.core.dom.Expression astNode) {
 		org.ita.neutrino.codeparser.Expression expression;
 
 		if (astNode instanceof org.eclipse.jdt.core.dom.StringLiteral) {
 			org.eclipse.jdt.core.dom.StringLiteral astStringLiteralExpression = (org.eclipse.jdt.core.dom.StringLiteral) astNode;
 
 			Type stringType = environment.getTypeCache().get("java.lang.String");
 			
 			org.ita.neutrino.astparser.ASTLiteralExpression stringLiteralExpression = environment.getExpressionFactory().createLiteralExpression(stringType, astStringLiteralExpression.getLiteralValue());
 			
 			stringLiteralExpression.setASTObject(astNode);
 
 			expression = stringLiteralExpression;
 		} else {
 			GenericExpression genericExpression = environment.createGenericExpression();
 
 			genericExpression.setASTObject(astNode);
 
 			String typeQualifiedName = astNode.resolveTypeBinding().getQualifiedName();
 
 			Type type = environment.getTypeCache().get(typeQualifiedName);
 
 			genericExpression.setType(type);
 
 			expression = genericExpression;
 		}
 
 		return expression;
 	}
 
 	private void populateParameterList(List<org.ita.neutrino.codeparser.Expression> parameterList, @SuppressWarnings("rawtypes") List arguments) {
 		for (Object argument : arguments) {
 			if (argument instanceof org.eclipse.jdt.core.dom.Expression) {
 				org.eclipse.jdt.core.dom.Expression astExpression = (org.eclipse.jdt.core.dom.Expression) argument;
 
 				org.ita.neutrino.codeparser.Expression expression = parseExpression(astExpression);
 
 				parameterList.add(expression);
 			}
 		}
 
 	}
 
 	private ASTGenericStatement parseGenericStatement(org.eclipse.jdt.core.dom.Statement node) {
 		ASTGenericStatement genericStatement = block.createGenericStatement();
 
 		genericStatement.setASTObject(node);
 
 		return genericStatement;
 	}
 
 	private ASTEnvironment getEnvironment() {
 		Invokable invokable = block.getParentInvokable();
 		Type type = invokable.getParent();
 		SourceFile sourceFile = type.getParent();
 		Package pack = sourceFile.getParent();
 
 		return (ASTEnvironment) pack.getParent();
 	}
 
 }
