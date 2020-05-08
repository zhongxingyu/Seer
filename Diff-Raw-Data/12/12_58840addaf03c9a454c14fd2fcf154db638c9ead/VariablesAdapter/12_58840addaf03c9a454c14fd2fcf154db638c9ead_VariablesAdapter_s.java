 package de.vilandgr.cssc;
 
 import java.util.Hashtable;
 
 import de.vilandgr.cssc.analysis.*;
 import de.vilandgr.cssc.node.ADeclaration;
 import de.vilandgr.cssc.node.AExpr;
 import de.vilandgr.cssc.node.AExprAp;
 import de.vilandgr.cssc.node.AFunctionDeclaration;
 import de.vilandgr.cssc.node.ASpecialElementName;
 import de.vilandgr.cssc.node.Node;
 
 public class VariablesAdapter extends DepthFirstAdapter {
 	private static final int NO_INDEND = -1;
 
 	private static final String VAR_FUNC = "var(";
 
 	public static final String VARIABLES = "@variables";
 
 	private int indend = 0;
 	
 	private int variablesIndendLevel = NO_INDEND;
 	
 	private Hashtable<String, Node> variables;
 	
 	private boolean debug = false;
 	
 	public VariablesAdapter() {
 		super();
 		this.variables = new Hashtable<String, Node>();
 	}
 	
 	@Override
 	public void defaultIn(Node node) {
 		if (debug) System.out.println(tabs() + "->" + node.getClass().getSimpleName() + "[" + node + "]");
 		
 		// check for variables section
 		if(node instanceof ASpecialElementName) {
 			ASpecialElementName special = (ASpecialElementName)node;
 			if (special.getSpecialSym().getText().equals(VARIABLES)) {
 				variablesIndendLevel = indend - 3; // go up 3 levels to the root of variables
 			}
 		}
 		
 		// add variables
 		if(variablesIndendLevel != NO_INDEND && node instanceof ADeclaration) {
 			ADeclaration decl = (ADeclaration)node;
 			variables.put(decl.getProperty().toString().trim(), decl.getExpr());
 		}
 		
 		// check for functions
 		if(node instanceof AFunctionDeclaration) {
 			AFunctionDeclaration func = (AFunctionDeclaration)node;
 			String funcName = func.getFunction().getText();
 			String exprText = func.getExpr().toString().trim();
 			if (funcName.equals(VAR_FUNC)) {
 				if (variables.get(exprText) != null) {
 					Node toRepelace = func.parent().parent();
 					
 					if (toRepelace instanceof AExprAp) {
 						AExpr ex = (AExpr)variables.get(exprText);
 						AExprAp expr = new AExprAp();
						expr.setTerm(ex.getTerm());
 						toRepelace.replaceBy(expr);
 					} else {
 						toRepelace.replaceBy((Node)variables.get(exprText).clone());
 					}
 				} else {
 					throw new RuntimeException("undefiend variable: '" + exprText + "'");
 				}
 			}
 		}
 		
 		indend++;
 	}
 
 	private String tabs() {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < indend; i++) sb.append("  ");
 		return sb.toString();
 	}
 
 	@Override
 	public void defaultOut(Node node) {
 		if (debug) System.out.println(tabs() + "<-" + node.getClass().getSimpleName());
 		indend--;
 		
 		// leave variables section
 		if (indend == variablesIndendLevel) {
 			variablesIndendLevel = NO_INDEND;
 		}
 	}
 }
