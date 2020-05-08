 package org.jmlspecs.eclipse.jdt.core.tests.boogie;
 
 import junit.framework.TestCase;
 
 import org.eclipse.jdt.internal.compiler.ast.Block;
 import org.jmlspecs.jml4.boogie.BoogieSymbolTable;
 
 public class BoogieSymbolTableTests extends TestCase {
 	public void testSymbolAddition() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		assertEquals("a", tab.addSymbol("x"));
 		assertEquals("a", tab.lookup("x"));
 	}
 
 	public void testDuplicateSymbolAdditionShouldFail() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		tab.addSymbol("x");
 		assertEquals("a", tab.lookup("x"));
		try {
			tab.addSymbol("x");
			fail("Duplicate symbol addition should fail.");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Symbol x already exists", e.getMessage());
		}
 	}
 	
 	public void testEnterScope() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		tab.addSymbol("x");
 		assertEquals("a", tab.lookup("x"));
 		tab.enterScope(new Block(0));
 		tab.addSymbol("x");
 		assertEquals("b", tab.lookup("x"));
 	}
 	
 	public void testExitScope() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		tab.addSymbol("x");
 		assertEquals("a", tab.lookup("x"));
 		tab.enterScope(new Block(0));
 		tab.addSymbol("x");
 		assertEquals("b", tab.lookup("x"));
 		tab.exitScope();
 		assertEquals("a", tab.lookup("x"));
 	}
 	
 	public void testMissingSymbol() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		
 		assertEquals(null, tab.lookup("x"));
 
 		tab.enterScope(new Block(0));
 		tab.addSymbol("x");
 		tab.exitScope();
 		assertEquals(null, tab.lookup("x"));
 	}
 	
 	public void testSymbolInHigherScope() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		tab.enterScope(new Block(0));
 		tab.addSymbol("x");
 		tab.enterScope(new Block(0));
 		tab.enterScope(new Block(0));
 		assertEquals("a", tab.lookup("x"));
 	}
 
 	public void testSymbolGenerator() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		
 		for (int i = 0; i < 26; i++) tab.addSymbol("x" + i);
 		assertEquals("aa", tab.addSymbol("test"));
 		assertEquals("aa", tab.lookup("test"));
 		
 		for (int i = 0; i < 25; i++) tab.addSymbol("y" + i);
 		tab.addSymbol("test2");
 		assertEquals("ba", tab.lookup("test2"));
 		assertEquals("ba", tab.lookup("test2"));
 	}
 	
 	public void testScopeReentry() {
 		Block blk1 = new Block(0);
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		tab.enterScope(blk1);
 		tab.addSymbol("x");
 		tab.exitScope();
 		tab.enterScope(blk1);
 		assertEquals("a", tab.lookup("x"));
 	}
 
 	public void testScopeReentryWithScopeRemoval() {
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		tab.enterScope(new Block(0));
 		tab.addSymbol("x");
 		tab.exitScope(); 
 		tab.enterScope(new Block(0));
 		assertEquals(null, tab.lookup("x"));
 	}
 	
 	public void testLookupInDifferentBlock() {
 		Block blk1 = new Block(0);
 		Block blk2 = new Block(0);
 		BoogieSymbolTable tab = new BoogieSymbolTable();
 		tab.enterScope(blk1);
 		tab.enterScope(blk2);
 		tab.addSymbol("x");
 		tab.exitScope();
 		assertEquals("a", tab.lookup("x", blk2));
 		assertEquals(null, tab.lookup("x", blk1));
 	}
 }
