 package mepk.kernel.internal;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 
 import mepk.kernel.Expression;
 import mepk.kernel.ProofStep;
 import mepk.kernel.Statement;
 
 /**
  * A substitution proof step takes a statement, and constructs a structurally
 * similar statement by substituting one variable for a (typed) expression.
  */
 public class Substitute implements ProofStep.Internal {
 
 	private final Statement statement;
 	private final Statement result;
 
 	/**
 	 * Create an instance.
 	 * 
 	 * @param statement
 	 *            the grounding statement
 	 * @param varName
 	 *            the variable name
 	 * @param replacement
 	 *            the replacement expression
 	 * @param typesOfNewVars
 	 *            the additional type expressions
 	 */
 	public Substitute(Statement statement, String varName, Expression replacement, Map<String, Expression> typesOfNewVars) {
 		this.statement = statement;
 		// TODO: Check arguments
 		result = statement.substitute(varName, replacement, typesOfNewVars);
 	}
 
 	@Override
 	public Set<Statement> getGrounding() {
 		return Collections.singleton(statement);
 	}
 
 	@Override
 	public Statement getGrounded1() {
 		return result;
 	}
 }
