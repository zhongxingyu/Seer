 package org.ita.neutrino.astparser;
 
import org.eclipse.jdt.core.dom.ASTNode;
 import org.ita.neutrino.codeparser.Expression;
 import org.ita.neutrino.codeparser.Type;
 import org.ita.neutrino.codeparser.VariableDeclarationStatement;
 
public class ASTVariableDeclarationStatement extends ASTAbstractStatement<ASTNode> implements VariableDeclarationStatement {
 
 	private Type variableType;
 	private String variableName;
 	private Expression initializationExpression;
 
 	@Override
 	public Type getVariableType() {
 		return variableType;
 	}
 
 	protected void setVariableType(Type declarationType) {
 		this.variableType = declarationType;
 	}
 
 	@Override
 	public String getVariableName() {
 		return variableName;
 	}
 
 	protected void setVariableName(String variableName) {
 		this.variableName = variableName;
 	}
 
 	@Override
 	public Expression getInitialization() {
 		return initializationExpression;
 	}
 	
 	protected void setInitializationExpression(Expression initializationExpression) {
 		this.initializationExpression = initializationExpression;
 	}
 
 }
