 package de.skuzzle.polly.parsing.tree;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 
 import de.skuzzle.polly.parsing.ExecutionException;
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.declarations.Declaration;
 import de.skuzzle.polly.parsing.declarations.FunctionDeclaration;
 import de.skuzzle.polly.parsing.declarations.Namespace;
 import de.skuzzle.polly.parsing.declarations.VarDeclaration;
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 import de.skuzzle.polly.parsing.tree.literals.Literal;
 import de.skuzzle.polly.parsing.tree.literals.UserLiteral;
 
 public class VarOrCallExpression extends Expression {
 
 	private IdentifierLiteral id;
 	private Declaration resolvedDeclaration;
 	private Expression resolvedExpression;
 	private List<Expression> actualParameters;
 	private boolean hardcoded;
 
 	private static final long serialVersionUID = 1L;
 
 	public VarOrCallExpression(IdentifierLiteral id) {
 		super(id.getPosition());
 		this.id = id;
 		this.actualParameters = new ArrayList<Expression>();
 	}
 	
 	
 	
     public IdentifierLiteral getId() {
         return this.id;
     }
 	
 	
 	
 	public List<Expression> getActualParameters() {
         return this.actualParameters;
     }
 
 	
 	
 	@Override
 	public Expression contextCheck(Namespace context) throws ParseException {
 	    
 	    /*
 	     * This method is only called for var access' or function calls that are
 	     * not preceded by a namespace access. That means, the identifier can be resolved
 	     * within the current namespace
 	     */
 	    
 	    // Precede this call with a namespace access if this call does not
 	    // reference a local declaration.
 	    this.contextCheckForMember(context, context, false);
 	    if (!((VarDeclaration)this.resolvedDeclaration).isLocal()) {
     		Expression result = new NamespaceAccessExpression(new UserLiteral(
     				context.getRootNS()), this, this.getPosition());
     		
     		result = result.contextCheck(context);
     		return result;
 	    }
 	    return this;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param context This is the namespace in which the declaration for this var or call
 	 *             will be resolved.
 	 * @param current This is the namespace used for contextchecking.
 	 * @param root Determines whether the declaration is only resolved in the root 
 	 *             declaration level of the current namespace. This is the case when
 	 *             accessing a var of function from a different namespace
 	 * @throws ParseException
 	 */
 	public void contextCheckForMember(Namespace context, Namespace current, boolean root) 
 	            throws ParseException {
 	    
 	    
 	    this.resolvedDeclaration = context.resolve(this.id, root);
 	    
 	    if (this.resolvedDeclaration instanceof FunctionDeclaration) {
 	        FunctionDeclaration decl = (FunctionDeclaration) this.resolvedDeclaration;
 	        
 	        if (decl.getFormalParameters().size() != this.actualParameters.size()) {
 	            throw new ParseException("Falsche Parameteranzahl: " + 
 	                    this.actualParameters.size() + ". Erwartet: " + 
 	                    decl.getFormalParameters().size(), this.getPosition());
 	        }
 
 	        current.enter();
 	        try {
 	            for (int i = 0; i < this.actualParameters.size(); ++i) {
 	                Expression actual = this.actualParameters.get(i);
 	                VarDeclaration formal = decl.getFormalParameters().get(i);
 	                
 	                actual = actual.contextCheck(current);
 	                this.actualParameters.set(i, actual);
 	                
 	                if (!formal.getType().check(actual.getType())) {
 	                    Type.typeError(actual.getType(), formal.getType(), 
 	                        actual.getPosition());
 	                }
 	                
 	                // declare a new local var for each formal parameter which contains
 	                // the expression of the actual parameter
 	                VarDeclaration act = new VarDeclaration(
 	                            formal.getName(), actual, true);
 	                current.addNormal(act);
 	            }
 	            
 	            this.hardcoded = decl.isHardcoded();
 	            
 	            // For non hardcoded functions, this will cause all parameters in the
 	            // expression to be replaced by their actual expression.
 	            // hardcoded functions will do nothing
 	            this.resolvedExpression = decl.getExpression().contextCheck(current);
 	        } finally {
 	            // make sure to leave the declarations in a clean state
 	            current.leave();
 	        }
 	        
 	        this.setType(this.resolvedExpression.getType());
 	    } else if (this.resolvedDeclaration instanceof VarDeclaration) {
 	        if (this.actualParameters != null && !this.actualParameters.isEmpty()) {
 	            throw new ParseException(this.id + 
 	                " ist eine Variable und kann nicht mit Parametern aufgerufen werden", 
 	                this.getPosition());
 	        }
 	        this.resolvedExpression = ((VarDeclaration) this.resolvedDeclaration)
 	                .getExpression().contextCheck(context);
 	        this.setType(this.resolvedExpression.getType());
 	    } else {
	        assert false;
 	    }
 	}
 
 
 	
 	@Override
 	public void collapse(Stack<Literal> stack) throws ExecutionException {
 		if (this.resolvedDeclaration instanceof FunctionDeclaration) {
 		    // Hardcoded functions expect their params to be on the stack
 		    if (this.hardcoded) {
 		        for (Expression exp : this.actualParameters) {
 		            exp.collapse(stack);
 		        }
 		    }
 		}
 		
 		// this is the same for vars and functions
 	    this.resolvedExpression.collapse(stack);
 	}
 }
