 package net.sourceforge.pmd.rules.design;
 
 import net.sourceforge.pmd.AbstractRule;
 import net.sourceforge.pmd.RuleContext;
 import net.sourceforge.pmd.RuleViolation;
 
 import net.sourceforge.pmd.ast.ASTCompilationUnit;
 import net.sourceforge.pmd.ast.ASTMethodDeclaration;
 
 public class UseSingletonRule
     extends AbstractRule
 {
     public boolean isOK = false;
 
     public UseSingletonRule() { }
 
     public String getDescription() { 
 	return "All methods are static.  Consider using Singleton instead.";
     }
 
     public Object visit( ASTMethodDeclaration decl, Object data ) {
	System.err.println("Visiting Method Declaration.");
 	if (isOK) return data;
 	
 	if (!decl.isStatic()) {
 	    isOK = true;
 	    return data;
 	}
 	return data;
     }
 
     public Object visit( ASTCompilationUnit cu, Object data ) {
	System.err.println("Visiting Compilation Unit.");
 
	Object RC = cu.childrenAccept( this, data );

	System.err.println("Finished visiting CU.");

	if (!isOK) {
	    System.err.println("All methods are static.");
 	    (((RuleContext) data).getReport()).
 		addRuleViolation( new RuleViolation( this, cu.getBeginLine() ));
	}

	return RC;
     }
 }
