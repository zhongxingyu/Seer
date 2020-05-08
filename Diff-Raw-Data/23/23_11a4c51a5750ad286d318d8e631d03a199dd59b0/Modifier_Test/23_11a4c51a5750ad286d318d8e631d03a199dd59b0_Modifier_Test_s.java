 package descent.tests.mars;
 
 import java.util.Arrays;
 import java.util.List;
 
 import descent.core.dom.AliasDeclaration;
 import descent.core.dom.Declaration;
 import descent.core.dom.Modifier;
 import descent.core.dom.ModifierDeclaration;
 import descent.core.dom.TypedefDeclaration;
 import descent.core.dom.VariableDeclaration;
 import descent.core.dom.Modifier.ModifierKeyword;
 
 public class Modifier_Test extends Parser_Test {
 	
 	public void testWithoutModifiers() {
 		String s = " alias int Bla;";
 		
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(0, aliasDeclaration.modifiers().size());
 	}
 	
 	public void testAllModifiersAsJava() {
 		Object[][] objs = {
 				{ "private", Modifier.ModifierKeyword.PRIVATE_KEYWORD },
 				{ "package", Modifier.ModifierKeyword.PACKAGE_KEYWORD },
 				{ "protected", Modifier.ModifierKeyword.PROTECTED_KEYWORD },
 				{ "public", Modifier.ModifierKeyword.PUBLIC_KEYWORD },
 				{ "export", Modifier.ModifierKeyword.EXPORT_KEYWORD },
 				{ "const", Modifier.ModifierKeyword.CONST_KEYWORD },
 				{ "final", Modifier.ModifierKeyword.FINAL_KEYWORD },
 				{ "auto", Modifier.ModifierKeyword.AUTO_KEYWORD },
 				{ "scope", Modifier.ModifierKeyword.SCOPE_KEYWORD },
 				{ "override", Modifier.ModifierKeyword.OVERRIDE_KEYWORD },
 				{ "abstract", Modifier.ModifierKeyword.ABSTRACT_KEYWORD },
 				{ "synchronized", Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD },
 				{ "deprecated", Modifier.ModifierKeyword.DEPRECATED_KEYWORD },
 			};
 		
 		for(Object[] pair : objs) {
 			String s = " " + pair[0] + " alias int Bla;";
 			AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 			assertPosition(aliasDeclaration, 1, s.length() - 1);
 			
 			try {
 				assertEquals(1, aliasDeclaration.modifiers().size());
 			} catch (Throwable t) {
 				fail(Arrays.toString(pair));
 			}
 			
 			Modifier modifier = aliasDeclaration.modifiers().get(0);
 			assertEquals(pair[1], modifier.getModifierKeyword());
 			assertPosition(modifier, 1, ((String) pair[0]).length());
 		}
 	}
 	
 	public void testManyProtectionModifiersAsJava() {
 		String s = " private public alias int Bla;";
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(2, aliasDeclaration.modifiers().size());
 		
 		Modifier modifier;
 		
 		modifier = aliasDeclaration.modifiers().get(0);
 		assertEquals(Modifier.ModifierKeyword.PRIVATE_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 1, "private".length());
 		
 		modifier = aliasDeclaration.modifiers().get(1);
 		assertEquals(Modifier.ModifierKeyword.PUBLIC_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 9, "public".length());
 	}
 	
 	public void testProtectionAndStaticModifiersAsJava() {
 		String s = " private static alias int Bla;";
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(2, aliasDeclaration.modifiers().size());
 		
 		Modifier modifier;
 		
 		modifier = aliasDeclaration.modifiers().get(0);
 		assertEquals(Modifier.ModifierKeyword.PRIVATE_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 1, "private".length());
 		
 		modifier = aliasDeclaration.modifiers().get(1);
 		assertEquals(Modifier.ModifierKeyword.STATIC_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 9, "static".length());
 	}
 	
 	public void testProtectionAndOtherModifiersAsJava() {
 		String s = " private const alias int Bla;";
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(2, aliasDeclaration.modifiers().size());
 		
 		Modifier modifier;
 		
 		modifier = aliasDeclaration.modifiers().get(0);
 		assertEquals(Modifier.ModifierKeyword.PRIVATE_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 1, "private".length());
 		
 		modifier = aliasDeclaration.modifiers().get(1);
 		assertEquals(Modifier.ModifierKeyword.CONST_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 9, "const".length());
 	}
 	
 	public void testStaticAndProtectionModifiersAsJava() {
 		String s = " static private alias int Bla;";
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(2, aliasDeclaration.modifiers().size());
 		
 		Modifier modifier;
 		
 		modifier = aliasDeclaration.modifiers().get(0);
 		assertEquals(Modifier.ModifierKeyword.STATIC_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 1, "static".length());
 		
 		modifier = aliasDeclaration.modifiers().get(1);
 		assertEquals(Modifier.ModifierKeyword.PRIVATE_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 8, "private".length());
 	}
 	
 	public void testOtherAndProtectionModifiersAsJava() {
 		String s = " const private alias int Bla;";
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(2, aliasDeclaration.modifiers().size());
 		
 		Modifier modifier;
 		
 		modifier = aliasDeclaration.modifiers().get(0);
 		assertEquals(Modifier.ModifierKeyword.CONST_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 1, "const".length());
 		
 		modifier = aliasDeclaration.modifiers().get(1);
 		assertEquals(Modifier.ModifierKeyword.PRIVATE_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 7, "private".length());
 	}
 	
 	public void testOtherAndStaticModifiersAsJava() {
 		String s = " const static alias int Bla;";
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(2, aliasDeclaration.modifiers().size());
 		
 		Modifier modifier;
 		
 		modifier = aliasDeclaration.modifiers().get(0);
 		assertEquals(Modifier.ModifierKeyword.CONST_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 1, "const".length());
 		
 		modifier = aliasDeclaration.modifiers().get(1);
 		assertEquals(Modifier.ModifierKeyword.STATIC_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 7, "static".length());
 	}
 	
 	public void testStaticAndOtherModifiersAsJava() {
 		String s = " static const alias int Bla;";
 		AliasDeclaration aliasDeclaration = (AliasDeclaration) getSingleDeclarationNoProblems(s);
 		assertPosition(aliasDeclaration, 1, s.length() - 1);
 		
 		assertEquals(2, aliasDeclaration.modifiers().size());
 		
 		Modifier modifier;
 		
 		modifier = aliasDeclaration.modifiers().get(0);
 		assertEquals(Modifier.ModifierKeyword.STATIC_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 1, "static".length());
 		
 		modifier = aliasDeclaration.modifiers().get(1);
 		assertEquals(Modifier.ModifierKeyword.CONST_KEYWORD, modifier.getModifierKeyword());
 		assertPosition(modifier, 8, "const".length());
 	}
 	
 	public void testModifiersWithCurlyBraces() {
 		Object[][] objs = {
 			{ "private", Modifier.ModifierKeyword.PRIVATE_KEYWORD },
 			{ "package", Modifier.ModifierKeyword.PACKAGE_KEYWORD },
 			{ "protected", Modifier.ModifierKeyword.PROTECTED_KEYWORD },
 			{ "public", Modifier.ModifierKeyword.PUBLIC_KEYWORD },
 			{ "export", Modifier.ModifierKeyword.EXPORT_KEYWORD },
 			{ "const", Modifier.ModifierKeyword.CONST_KEYWORD },
 			{ "final", Modifier.ModifierKeyword.FINAL_KEYWORD },
 			{ "auto", Modifier.ModifierKeyword.AUTO_KEYWORD },
 			{ "scope", Modifier.ModifierKeyword.SCOPE_KEYWORD },
 			{ "override", Modifier.ModifierKeyword.OVERRIDE_KEYWORD },
 			{ "abstract", Modifier.ModifierKeyword.ABSTRACT_KEYWORD },
 			{ "synchronized", Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD },
 			{ "deprecated", Modifier.ModifierKeyword.DEPRECATED_KEYWORD },
 		};
 		
 		for(Object[] pair : objs) {
 			String s = " " + pair[0] + " { class Clazz { } }";
 			ModifierDeclaration modifierDeclaration = (ModifierDeclaration) getSingleDeclarationNoProblems(s);
 			assertEquals(pair[1], modifierDeclaration.getModifier().getModifierKeyword());
 			assertPosition(modifierDeclaration.getModifier(), 1, ((String) pair[0]).length());
 			assertEquals(1, modifierDeclaration.declarations().size());
 			assertEquals(0, modifierDeclaration.modifiers().size());
 		}
 	}
 	
 	public void testModifiersWithColon() {
 		Object[][] objs = {
 			{ "private", Modifier.ModifierKeyword.PRIVATE_KEYWORD },
 			{ "package", Modifier.ModifierKeyword.PACKAGE_KEYWORD },
 			{ "protected", Modifier.ModifierKeyword.PROTECTED_KEYWORD },
 			{ "public", Modifier.ModifierKeyword.PUBLIC_KEYWORD },
 			{ "export", Modifier.ModifierKeyword.EXPORT_KEYWORD },
 			{ "const", Modifier.ModifierKeyword.CONST_KEYWORD },
 			{ "final", Modifier.ModifierKeyword.FINAL_KEYWORD },
 			{ "auto", Modifier.ModifierKeyword.AUTO_KEYWORD },
 			{ "scope", Modifier.ModifierKeyword.SCOPE_KEYWORD },
 			{ "override", Modifier.ModifierKeyword.OVERRIDE_KEYWORD },
 			{ "abstract", Modifier.ModifierKeyword.ABSTRACT_KEYWORD },
 			{ "synchronized", Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD },
 			{ "deprecated", Modifier.ModifierKeyword.DEPRECATED_KEYWORD },
 		};
 		
 		for(Object[] pair : objs) {
 			String s = " " + pair[0] + ": class Clazz { }";
 			ModifierDeclaration modifierDeclaration = (ModifierDeclaration) getSingleDeclarationNoProblems(s);
 			assertEquals(pair[1], modifierDeclaration.getModifier().getModifierKeyword());
 			assertPosition(modifierDeclaration.getModifier(), 1, ((String) pair[0]).length());
 			assertEquals(1, modifierDeclaration.declarations().size());
 			assertEquals(0, modifierDeclaration.modifiers().size());
 		}
 	}
 	
 	public void testModifiersWithVar() {
 		Object[][] objs = {
 			{ "const", Modifier.ModifierKeyword.CONST_KEYWORD },
 			{ "final", Modifier.ModifierKeyword.FINAL_KEYWORD },
 			{ "auto", Modifier.ModifierKeyword.AUTO_KEYWORD },
 			{ "scope", Modifier.ModifierKeyword.SCOPE_KEYWORD },
 			{ "override", Modifier.ModifierKeyword.OVERRIDE_KEYWORD },
 			{ "abstract", Modifier.ModifierKeyword.ABSTRACT_KEYWORD },
 			{ "synchronized", Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD },
 			{ "deprecated", Modifier.ModifierKeyword.DEPRECATED_KEYWORD },
 			{ "scope", Modifier.ModifierKeyword.SCOPE_KEYWORD },
 		};
 		
 		for(Object[] pair : objs) {
 			String s = " static " + pair[0] + "  x = 1;";
 			VariableDeclaration var = (VariableDeclaration) getSingleDeclarationNoProblems(s);
 			assertEquals(2, var.modifiers().size());
 			assertEquals(ModifierKeyword.STATIC_KEYWORD, var.modifiers().get(0).getModifierKeyword());
 			assertPosition(var.modifiers().get(0), 1, "static".length());
 			assertEquals(pair[1], var.modifiers().get(1).getModifierKeyword());
 			assertPosition(var.modifiers().get(1), 8, ((String) pair[0]).length());
 		}
 	}
 	
 	public void testPosition_bug() {
 		String s = " this() { } public int bla() { }";
 		List<Declaration> declDefs = getDeclarationsNoProblems(s);
 		assertEquals(2, declDefs.size());
 		
 		assertPosition(declDefs.get(0), 1, 10);
 	}
 	
 	public void testModifiersBug1_1a() {
 		String s = " public int x, y;";
 		VariableDeclaration decl = (VariableDeclaration) getDeclarationsNoProblems(s).get(0);
 		assertEquals(1, decl.modifiers().size());
 		assertPosition(decl, 1, s.length() - 1);
 	}
 	
 	public void testModifiersBug1_1b() {
 		String s = " final int x, y;";
 		VariableDeclaration decl = (VariableDeclaration) getDeclarationsNoProblems(s).get(0);
 		assertEquals(1, decl.modifiers().size());
 		assertPosition(decl, 1, s.length() - 1);
 	}
 	
 	public void testModifiersBug1_2a() {
 		String s = " public alias int x, y;";
 		AliasDeclaration decl = (AliasDeclaration) getDeclarationsNoProblems(s).get(0);
 		assertEquals(1, decl.modifiers().size());
 		assertPosition(decl, 1, s.length() - 1);
 	}
 	
 	public void testModifiersBug1_2b() {
 		String s = " final alias int x, y;";
 		AliasDeclaration decl = (AliasDeclaration) getDeclarationsNoProblems(s).get(0);
 		assertEquals(1, decl.modifiers().size());
 		assertPosition(decl, 1, s.length() - 1);
 	}
 	
 	public void testModifiersBug1_3a() {
 		String s = " public typedef int x, y;";
 		TypedefDeclaration decl = (TypedefDeclaration) getDeclarationsNoProblems(s).get(0);
 		assertEquals(1, decl.modifiers().size());
 		assertPosition(decl, 1, s.length() - 1);
 	}
 	
 	public void testModifiersBug1_3b() {
 		String s = " final typedef int x, y;";
 		TypedefDeclaration decl = (TypedefDeclaration) getDeclarationsNoProblems(s).get(0);
 		assertEquals(1, decl.modifiers().size());
 		assertPosition(decl, 1, s.length() - 1);
 	}
 
 }
