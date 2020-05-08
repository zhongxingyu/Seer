 package org.ita.testrefactoring.astparser;
 
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.PrefixExpression;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.ita.testrefactoring.codeparser.LiteralExpression;
 import org.ita.testrefactoring.codeparser.ParserException;
 import org.ita.testrefactoring.codeparser.Statement;
 import org.ita.testrefactoring.codeparser.Type;
 import org.ita.testrefactoring.codeparser.VariableDeclarationStatement;
 
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
 
 	public void setBlock(ASTBlock block) {
 		this.block = block;
 	}
 
 	public void parse() throws ParserException {
 		List<ASTNode> nodes = quickVisitor.quickVisit(block.getASTObject());
 
 		for (ASTNode node : nodes) {
 			parseStatement(node);
 		}
 	}
 
 	private void parseStatement(ASTNode node) throws ParserException {
 		// ConsoleVisitor.showNodes(node);
 
 		Statement statement = null;
 
 		if (node instanceof org.eclipse.jdt.core.dom.VariableDeclarationStatement) {
 			statement = parseVariableDeclaration((org.eclipse.jdt.core.dom.VariableDeclarationStatement) node);
 		} else if (node instanceof org.eclipse.jdt.core.dom.Statement) {
 			statement = parseGenericStatement((org.eclipse.jdt.core.dom.Statement) node);
 		} else {
 			throw new UnsupportedSintaxException();
 		}
 
 		block.getStatementList().add(statement);
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
 		
 		ASTEnvironment environment = getEnvironmentForBlock();
 
 		Type variableType = environment.getTypeCache().get(variableTypeNode.resolveBinding().getQualifiedName());
 
 		String variableName = variableNameNode.getIdentifier();
 		
 		ASTVariableDeclarationStatement variableDeclaration = block.createVariableDeclaration(variableName);
 
 		variableDeclaration.setVariableType(variableType);
 		
 		variableDeclaration.setASTObject(variableFragment);
 
 		
 		// Inicialização da variável
 		if (fragmentNodes.size() > 1) {
 			
 			// Expressão listeral
 			if (fragmentNodes.get(1) instanceof org.eclipse.jdt.core.dom.PrefixExpression) {
 				org.eclipse.jdt.core.dom.PrefixExpression astNode = (PrefixExpression) fragmentNodes.get(1);
 				
 				ASTLiteralExpression literalExpression = environment.createLiteralExpression(astNode.toString());
 				
 				literalExpression.setASTObject(astNode);
 				
 				variableDeclaration.setInitializationExpression(literalExpression);
 				
 			// Variável inicializada por método
 			} else if (fragmentNodes.get(1) instanceof org.eclipse.jdt.core.dom.MethodInvocation) {
 				org.eclipse.jdt.core.dom.MethodInvocation astNode = (org.eclipse.jdt.core.dom.MethodInvocation) fragmentNodes.get(1);
 				
 				String methodSignatureString = ASTEnvironment.getMethodSignature(astNode);
 
 				ASTMethodInvocationExpression mie = environment.createMethodInvocationExpression(methodSignatureString);
 				
 				mie.setASTObject(astNode);
 				
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
 				org.eclipse.jdt.core.dom.Expression astNode = (org.eclipse.jdt.core.dom.Expression) fragmentNodes.get(1);
 				
 				GenericExpression genericExpression = environment.createGenericExpression();
 				
 				genericExpression.setASTObject(astNode);
 				
 				variableDeclaration.setInitializationExpression(genericExpression);
 			} else {
 				throw new UnsupportedSintaxException();
 			}
 		}
 		
 		
 		return variableDeclaration;
 	}
 	
 	private ASTGenericStatement parseGenericStatement(org.eclipse.jdt.core.dom.Statement node) {
 		ASTGenericStatement genericStatement = block.createGenericStatement();
 
 		genericStatement.setASTObject(node);
 		
 		return genericStatement;
 	}
 
 	private ASTEnvironment getEnvironmentForBlock() {
		ASTMethod correspondingMethod = block.getParent();
 
 		return (ASTEnvironment) correspondingMethod.getParent().getParent().getParent().getParent();
 	}
 
 }
