 package testing;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import source.Expression;
 import source.IllegalLineException;
 import source.ProofTree;
 
 public class ProofTreeTest {
 	ProofTree t1;
 	ProofTree t2;
 	
 /*	@Test
 	public void testEquals() {
 		t1 = new ProofTree
 	}*/
 	
 	@Test
 	public void testIsEquivalent() throws IllegalLineException {
 		
 		Expression proof1 = new Expression("((x&y)=>x)");
 		Expression exp1 = new Expression("(((a|b)&~c)=>(a|b))");
 		
 		Expression proof2 = new Expression("((x&y)=>x)");
 		Expression exp2 = new Expression("(((a|b)&~c)=>a)");
 		
 		Expression proof3 = new Expression("((x&y)=>x)");
 		Expression exp3 = new Expression("((a&b)=>a)");
 		
 		Expression proof4 = new Expression("((x&y)=>x)");
 		Expression exp4 = new Expression("((a&b)=>b)");
 		
 		Expression proof5 = new Expression("(a&b)");
 		Expression exp5 = new Expression("(q=>v)");
 		
 		Expression proof6 = new Expression("(a=>b)");
 		Expression exp6 = new Expression("(q=>v)");
 		
 		assertTrue(exp1.getTree().isEquivalent(proof1.getTree()));
 		assertFalse(exp2.getTree().isEquivalent(proof2.getTree()));
 		assertTrue(exp3.getTree().isEquivalent(proof3.getTree()));
 		assertFalse(exp4.getTree().isEquivalent(proof4.getTree()));
 		assertFalse(exp5.getTree().isEquivalent(proof5.getTree()));
		assertFalse(exp6.getTree().isEquivalent(proof6.getTree()));
 
 	}
 
 	@Test
 	public void testEquals() throws IllegalLineException
 	{
 		Expression a = new Expression("((x&y)=>x)");
 		Expression b = new Expression("((x&y)=>x)");
 
 		assertTrue(a.getTree().equals(b.getTree()));
 		
 		a = new Expression("(((a|b)&~c)=>a)");
 		b = new Expression("(((a|b)&~c)=>a)");
 		
 		assertTrue(a.getTree().equals(b.getTree()));
 		
 		a = new Expression("((x&y)=>x)");
 		b = new Expression("(((a|b)&~c)=>a)");
 		
 		assertFalse(a.getTree().equals(b.getTree()));
 	}
 	
 	@Test
 	public void testEqualsOppositeSign() throws IllegalLineException
 	{
 		Expression a = new Expression("((x&y)=>x)");
 		Expression b = new Expression("~((x&y)=>x)");
 
 		assertTrue(a.getTree().equalsOpositeSign(b.getTree()));
 		
 		a = new Expression("(((a|b)&~c)=>a)");
 		b = new Expression("~(((a|b)&~c)=>a)");
 		
 		assertTrue(a.getTree().equalsOpositeSign(b.getTree()));
 		
 		a = new Expression("(((a|b)&~c)=>a)");
 		b = new Expression("(((a|b)&~c)=>a)");
 		
 		assertFalse(a.getTree().equalsOpositeSign(b.getTree()));
 		
 		a = new Expression("((x&y)=>x)");
 		b = new Expression("~(((a|b)&~c)=>a)");
 		
 		assertFalse(a.getTree().equalsOpositeSign(b.getTree()));
 	}
 	
 }
