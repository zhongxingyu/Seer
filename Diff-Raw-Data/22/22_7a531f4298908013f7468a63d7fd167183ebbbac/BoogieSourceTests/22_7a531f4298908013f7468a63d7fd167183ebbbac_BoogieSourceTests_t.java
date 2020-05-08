 package org.jmlspecs.eclipse.jdt.core.tests.boogie;
 
import junit.framework.TestCase;

 import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
 import org.jmlspecs.jml4.boogie.BoogieSource;
 import org.jmlspecs.jml4.boogie.BoogieSourcePoint;
 
 public class BoogieSourceTests extends TestCase{
 	
	public void testPrepend() {
 		BoogieSource b = new BoogieSource();
 		assertTrue(b.getResults().equals(BoogieSource.getHeaders()));
 		String prepend = "This will now be prepended";
 		b.preprend(prepend);
		assertTrue(b.getResults().equals(BoogieSource.getHeaders() + prepend + "\n"));
 		
 		String toBeAppended = "true";
 		b.append(toBeAppended, new TrueLiteral(0, 4));
 		assertTrue(toBeAppended.equals(b.getTermAtPoint(new BoogieSourcePoint(4, 1)).toString()));
 	}
 	
 	
 
 }
