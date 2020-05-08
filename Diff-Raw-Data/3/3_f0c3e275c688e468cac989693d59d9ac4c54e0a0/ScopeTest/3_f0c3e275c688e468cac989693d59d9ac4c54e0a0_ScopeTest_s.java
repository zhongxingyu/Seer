 package titocc.compiler;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import titocc.compiler.types.CType;
 
 public class ScopeTest
 {
 	private Scope globalScope;
 
 	private Symbol sym1, sym2;
 
 	@Before
 	public void setUp()
 	{
 		globalScope = new Scope(null, "prefix1_");
 		sym1 = new Symbol("aa", CType.INT, StorageClass.Extern, false);
 		sym2 = new Symbol("bb", CType.INT, StorageClass.Extern, false);
 	}
 
 	@Test
 	public void scopeWithNoParentIsGlobal()
 	{
 		assertNull(globalScope.getParent());
 		assertTrue(globalScope.isGlobal());
 	}
 
 	@Test
 	public void scopeWithParentIsNotGlobal()
 	{
 		Scope subScope = new Scope(globalScope, "");
 		assertSame(globalScope, subScope.getParent());
 		assertFalse(subScope.isGlobal());
 	}
 
 	@Test
 	public void newScopeHasNoSymbols()
 	{
 		assertEquals(0, globalScope.getSymbols().size());
 	}
 
 	@Test
 	public void getSymbolsReturnsAddedSymbols()
 	{
 		globalScope.add(sym1);
 		globalScope.add(sym2);
 		assertEquals(2, globalScope.getSymbols().size());
 		assertTrue(globalScope.getSymbols().contains(sym1));
 		assertTrue(globalScope.getSymbols().contains(sym2));
 	}
 
 	@Test
 	public void findsAddedSymbols()
 	{
 		globalScope.add(sym1);
 		globalScope.add(sym2);
 		assertSame(sym1, globalScope.find(sym1.getName()));
 		assertSame(sym2, globalScope.find(sym2.getName()));
 	}
 
 	@Test
 	public void addReturnsEalierSymbolIfExistsAlready()
 	{
 		globalScope.add(sym1);
 		globalScope.add(sym2);
 		Symbol sym3 = new Symbol(sym1.getName(), CType.INT, StorageClass.Extern, false);
 		DeclarationResult ret = globalScope.add(sym3);
 		assertSame(sym1, ret.symbol);
 		assertEquals(2, globalScope.getSymbols().size());
 	}
 
 	@Test
 	public void addReturnsNullIfEarlierSymbolHasDifferentType()
 	{
 		globalScope.add(sym1);
 		globalScope.add(sym2);
 		Symbol sym3 = new Symbol(sym1.getName(), CType.UINT, StorageClass.Auto, false);
 		assertNull(globalScope.add(sym3).symbol);
 	}
 
 	@Test
 	public void findReturnsNullIfSymbolDoesntExist()
 	{
 		globalScope.add(sym1);
 		globalScope.add(sym2);
 		assertNull(globalScope.find("araweraw"));
 	}
 
 	@Test
 	public void findsSymbolsInParentScope()
 	{
 		globalScope.add(sym1);
 		globalScope.add(sym2);
 		Scope subScope = new Scope(globalScope, "");
 		assertSame(sym1, subScope.find(sym1.getName()));
 		assertSame(sym2, subScope.find(sym2.getName()));
 	}
 
 	@Test
 	public void symbolsAreHiddenBySubscopeSymbolsWithSameName()
 	{
 		globalScope.add(sym1);
 		Scope subScope = new Scope(globalScope, "");
 		Symbol sym3 = new Symbol(sym1.getName(), CType.INT, StorageClass.Auto, false);
 		subScope.add(sym3);
 		assertSame(sym3, subScope.find(sym1.getName()));
 		assertSame(sym1, globalScope.find(sym1.getName()));
 	}
 
 	@Test
 	public void findsDoesntSearchSubscopesOrSiblingScopes()
 	{
 		Scope myScope = new Scope(globalScope, "");
 		globalScope.addSubScope(myScope);
 
 		Scope siblingScope = new Scope(globalScope, "");
 		globalScope.addSubScope(siblingScope);
 
 		Scope subScope = new Scope(myScope, "");
 		myScope.addSubScope(subScope);
 
 		siblingScope.add(sym1);
 		Symbol sym3 = new Symbol(sym1.getName(), CType.INT, StorageClass.Auto, false);
 		subScope.add(sym3);
 
 		assertNull(myScope.find(sym1.getName()));
 	}
 
 	@Test
 	public void newScopeHasNoSubscopes()
 	{
 		assertEquals(0, globalScope.getSubScopes().size());
 	}
 
 	@Test
 	public void getSubscopesReturnsAddedSubscopes()
 	{
 		Scope subScope1 = new Scope(globalScope, "");
 		Scope subScope2 = new Scope(globalScope, "");
 		globalScope.addSubScope(subScope1);
 		globalScope.addSubScope(subScope2);
 		assertEquals(2, globalScope.getSubScopes().size());
 		assertTrue(globalScope.getSubScopes().contains(subScope1));
 		assertTrue(globalScope.getSubScopes().contains(subScope2));
 	}
 
 	@Test
 	public void globalNameHasCorrectPrefixes()
 	{
 		Scope subScope = new Scope(globalScope, "prefix2_");
 		assertEquals("prefix1_prefix2_name", subScope.makeGloballyUniqueName("name"));
 	}
 
 	@Test
 	public void globalNameHasNumberSuffixIfNameExistsAlready()
 	{
 		Scope subScope = new Scope(globalScope, "prefix2_");
 		subScope.makeGloballyUniqueName("name");
 		assertEquals("prefix1_prefix2_name2", subScope.makeGloballyUniqueName("name"));
 		assertEquals("prefix1_prefix2_name3", subScope.makeGloballyUniqueName("name"));
 	}
 
 	@Test
 	public void symbolsInDifferentScopesCantHaveSameGlobalName()
 	{
 		Scope subScope = new Scope(globalScope, "prefix2_");
 		globalScope.makeGloballyUniqueName("prefix2_name");
 		assertEquals("prefix1_prefix2_name2", subScope.makeGloballyUniqueName("name"));
 	}
 
 	@Test
 	public void addingToScopeSetsGlobalName()
 	{
 		Scope subScope = new Scope(globalScope, "prefix2_");
 		subScope.add(sym1);
 		assertEquals("prefix1_prefix2_aa", sym1.getGlobalName());
 	}
 
 	@Test
 	public void noDoubleUnderscoreInGlobalName()
 	{
 		Scope subScope = new Scope(globalScope, "prefix2_");
		Symbol sym = new Symbol("__abc", null, Symbol.Category.LocalVariable, StorageClass.Auto,
				false);
 		subScope.add(sym);
 		assertEquals("prefix1_prefix2_abc", sym.getGlobalName());
 	}
 }
