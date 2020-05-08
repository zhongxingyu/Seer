 package org.jmlspecs.eclipse.jdt.core.tests.boogie;
 
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.eclipse.jdt.internal.compiler.ast.ASTNode;
 import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
 import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
 import org.eclipse.jdt.internal.compiler.ast.Expression;
 import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
 import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
 import org.jmlspecs.jml4.boogie.BoogieSource;
 import org.jmlspecs.jml4.boogie.BoogieSourcePoint;
 import org.jmlspecs.jml4.ast.*;
 
 import junit.framework.TestCase;
 
 public class BoogieSourceTests extends TestCase{
 	
 	public void testPrepend(){
 		BoogieSource b = new BoogieSource();
 		assertTrue(b.getResults().equals(BoogieSource.getHeaders()));
 		String prepend = "This will now be prepended";
 		b.preprend(prepend);
		System.out.println(b.getResults());
		assertTrue(b.getResults().equals(BoogieSource.getHeaders()+prepend+"\n"));
 		
 		String toBeAppended = "true";
 		b.append(toBeAppended, new TrueLiteral(0, 4));
 		assertTrue(toBeAppended.equals(b.getTermAtPoint(new BoogieSourcePoint(4, 1)).toString()));
 		
 	}
 	
 	
 
 }
