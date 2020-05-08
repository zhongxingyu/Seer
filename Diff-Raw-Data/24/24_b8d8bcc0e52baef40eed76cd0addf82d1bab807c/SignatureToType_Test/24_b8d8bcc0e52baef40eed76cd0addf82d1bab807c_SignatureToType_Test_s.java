 package descent.tests.mangling;
 
 import descent.core.Signature;
 import descent.core.dom.AST;
 import descent.internal.compiler.parser.ASTNodeEncoder;
 import descent.internal.compiler.parser.Argument;
 import descent.internal.compiler.parser.IntegerExp;
 import descent.internal.compiler.parser.LINK;
 import descent.internal.compiler.parser.MinExp;
 import descent.internal.compiler.parser.Module;
 import descent.internal.compiler.parser.Objects;
 import descent.internal.compiler.parser.Parser;
 import descent.internal.compiler.parser.STC;
 import descent.internal.compiler.parser.TemplateInstance;
 import descent.internal.compiler.parser.TemplateInstanceWrapper;
 import descent.internal.compiler.parser.Type;
 import descent.internal.compiler.parser.TypeAArray;
 import descent.internal.compiler.parser.TypeBasic;
 import descent.internal.compiler.parser.TypeDArray;
 import descent.internal.compiler.parser.TypeDelegate;
 import descent.internal.compiler.parser.TypeFunction;
 import descent.internal.compiler.parser.TypeIdentifier;
 import descent.internal.compiler.parser.TypeInstance;
 import descent.internal.compiler.parser.TypePointer;
 import descent.internal.compiler.parser.TypeSArray;
 import descent.internal.compiler.parser.TypeSlice;
 import descent.internal.compiler.parser.TypeTypeof;
 import descent.internal.compiler.parser.VarDeclaration;
 import descent.internal.core.InternalSignature;
 
 public class SignatureToType_Test extends AbstractSignatureTest implements ISignatureTest {
 	
 	public void testPrimitive() {
 		Type actual = InternalSignature.toType(i, new ASTNodeEncoder(AST.D1));
 		assertSame(Type.tint32, actual);
 	}
 	
 	public void testPointer() {
 		TypePointer actual = (TypePointer) InternalSignature.toType(P(i), new ASTNodeEncoder(AST.D1));
 		assertSame(Type.tint32, actual.next);
 	}
 	
 	public void testStaticArray() {
 		TypeSArray actual = (TypeSArray) InternalSignature.toType(G(i, "3"), new ASTNodeEncoder(AST.D1));
 		assertSame(Type.tint32, actual.next);
 		assertSame(3, ((IntegerExp) actual.dim).value.intValue());
 	}
 	
 	public void testDynamicArray() {
 		TypeDArray actual = (TypeDArray) InternalSignature.toType(A(i), new ASTNodeEncoder(AST.D1));
 		assertSame(Type.tint32, actual.next);
 	}
 	
 	public void testAssociativeArray() {
 		TypeAArray actual = (TypeAArray) InternalSignature.toType(H(i, a), new ASTNodeEncoder(AST.D1));
 		assertSame(Type.tint32, actual.index);
 		assertSame(Type.tchar, actual.next);
 	}
 	
 	public void testConst() {
 		TypeBasic actual = (TypeBasic) InternalSignature.toType(Signature.C_CONST + i, new ASTNodeEncoder(AST.D1));
 		assertEquals(descent.internal.compiler.parser.TY.Tint32, actual.ty);
 		assertEquals(Type.MODconst, actual.mod);
 	}
 	
 	public void testInvariant() {
 		TypeBasic actual = (TypeBasic) InternalSignature.toType(Signature.C_INVARIANT + i, new ASTNodeEncoder(AST.D1));
 		assertEquals(descent.internal.compiler.parser.TY.Tint32, actual.ty);
 		assertEquals(Type.MODinvariant, actual.mod);
 	}
 	
 	public void testTypeof() {
 		TypeTypeof actual = (TypeTypeof) InternalSignature.toType(typeof("3"), new ASTNodeEncoder(AST.D1));
 		assertSame(3, ((IntegerExp) actual.exp).value.intValue());
 	}
 	
 	public void testSlice() {
 		TypeSlice actual = (TypeSlice) InternalSignature.toType(slice(i, "1", "3"), new ASTNodeEncoder(AST.D1));
 		assertSame(Type.tint32, actual.next);
 		assertSame(1, ((IntegerExp) actual.lwr).value.intValue());
 		assertSame(3, ((IntegerExp) actual.upr).value.intValue());
 	}
 	
 	public void testFunction() {
 		TypeFunction actual = (TypeFunction) InternalSignature.toType(F + i + Z + a, new ASTNodeEncoder(AST.D1));
 		assertEquals(LINK.LINKd, actual.linkage);
 		assertSame(Type.tchar, actual.next);
 		
 		assertEquals(1, actual.parameters.size());
 		
 		Argument arg = actual.parameters.get(0);
 		assertSame(Type.tint32, arg.type);
 		assertEquals(STC.STCin, arg.storageClass);
 	}
 	
 	public void testFunctionManyParameters() {
 		TypeFunction actual = (TypeFunction) InternalSignature.toType(F + i + a + Z + v, new ASTNodeEncoder(AST.D1));
 		assertEquals(LINK.LINKd, actual.linkage);
 		assertSame(Type.tvoid, actual.next);
 		
 		assertEquals(2, actual.parameters.size());
 		
 		Argument arg;
 		
 		arg = actual.parameters.get(0);
 		assertSame(Type.tint32, arg.type);
 		assertEquals(STC.STCin, arg.storageClass);
 		
 		arg = actual.parameters.get(1);
 		assertSame(Type.tchar, arg.type);
 		assertEquals(STC.STCin, arg.storageClass);
 	}
 	
 	public void testFunctionParameterStorageClass() {
 		TypeFunction actual = (TypeFunction) InternalSignature.toType(F + 'J' + i + Z + a, new ASTNodeEncoder(AST.D1));
 		assertEquals(LINK.LINKd, actual.linkage);
 		assertSame(Type.tchar, actual.next);
 		
 		assertEquals(1, actual.parameters.size());
 		
 		Argument arg = actual.parameters.get(0);
 		assertSame(Type.tint32, arg.type);
 		assertEquals(STC.STCout, arg.storageClass);
 	}
 	
 	public void testDelegate() {
 		TypeDelegate delegate = (TypeDelegate) InternalSignature.toType(D + F + i + Z + a, new ASTNodeEncoder(AST.D1));
 		
 		TypeFunction actual = (TypeFunction) delegate.next;
 		assertEquals(LINK.LINKd, actual.linkage);
 		assertSame(Type.tchar, actual.next);
 		
 		assertEquals(1, actual.parameters.size());
 		
 		Argument arg = actual.parameters.get(0);
 		assertSame(Type.tint32, arg.type);
 		assertEquals(STC.STCin, arg.storageClass);
 	}
 
 	public void testIdentifier() {
 		TypeIdentifier actual = (TypeIdentifier) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo", new ASTNodeEncoder(AST.D1));
 		
 		assertEquals("Foo", new String(actual.ident.ident));
 		assertEquals(0, actual.idents.size());
 	}
 	
 	public void testIdentifier2() {
 		TypeIdentifier actual = (TypeIdentifier) InternalSignature.toType(Signature.C_IDENTIFIER + "3Bar4Test3Foo", new ASTNodeEncoder(AST.D1));
 		
 		assertEquals("Bar", new String(actual.ident.ident));
 		assertEquals(2, actual.idents.size());
 		assertEquals("Test", new String(actual.idents.get(0).ident));
 		assertEquals("Foo", new String(actual.idents.get(1).ident));
 	}
 	
 	public void testInstance() {
 		TypeInstance typeInstance = (TypeInstance) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_PARAMETERS_BREAK, new ASTNodeEncoder(AST.D1));
 		TemplateInstance templInstance = typeInstance.tempinst;
 		
 		assertEquals("Foo", new String(templInstance.name.ident));
 		assertTrue(templInstance.tiargs == null || templInstance.tiargs.isEmpty());
 	}
 	
 	public void testInstanceType() {
 		TypeInstance typeInstance = (TypeInstance) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_INSTANCE_TYPE_PARAMETER + i + Signature.C_TEMPLATE_PARAMETERS_BREAK, new ASTNodeEncoder(AST.D1));
 		TemplateInstance templInstance = typeInstance.tempinst;
 		
 		assertEquals("Foo", new String(templInstance.name.ident));
 		assertEquals(1, templInstance.tiargs.size());
 		
 		assertSame(Type.tint32, templInstance.tiargs.get(0));
 	}
 	
 	public void testInstanceSymbol() {
 		TypeInstance typeInstance = (TypeInstance) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_INSTANCE_SYMBOL_PARAMETER + i + Signature.C_TEMPLATE_PARAMETERS_BREAK, new ASTNodeEncoder(AST.D1));
 		TemplateInstance templInstance = typeInstance.tempinst;
 		
 		assertEquals("Foo", new String(templInstance.name.ident));
 		assertEquals(1, templInstance.tiargs.size());
 		
 		assertSame(Type.tint32, templInstance.tiargs.get(0));
 	}
 	
 	public void testInstanceValue() {
 		TypeInstance typeInstance = (TypeInstance) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_INSTANCE_VALUE_PARAMETER + "1" + Signature.C_TEMPLATE_INSTANCE_VALUE_PARAMETER + "3" + Signature.C_TEMPLATE_PARAMETERS_BREAK, new ASTNodeEncoder(AST.D1));
 		TemplateInstance templInstance = typeInstance.tempinst;
 		
 		assertEquals("Foo", new String(templInstance.name.ident));
 		assertEquals(1, templInstance.tiargs.size());
 		
 		assertSame(3, ((IntegerExp) templInstance.tiargs.get(0)).value.intValue());
 	}
 	
 	public void testQualifiedInstance() {
 		TypeIdentifier typeIdent = (TypeIdentifier) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo" + "3Bar" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_PARAMETERS_BREAK, new ASTNodeEncoder(AST.D1));
 		assertEquals("Foo", new String(typeIdent.ident.ident));
 		
 		assertEquals(1, typeIdent.idents.size());
 		
 		TemplateInstanceWrapper wrap = (TemplateInstanceWrapper) typeIdent.idents.get(0);
 		TemplateInstance tempInstance = wrap.tempinst;
 		
 		assertEquals("Bar", new String(tempInstance.name.ident));
 		assertTrue(tempInstance.tiargs == null || tempInstance.tiargs.isEmpty());
 	}
 	
 	public void testNestedInstance() {
 		TypeInstance typeInstance = (TypeInstance) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_PARAMETERS_BREAK + Signature.C_IDENTIFIER + "3Bar" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_PARAMETERS_BREAK, new ASTNodeEncoder(AST.D1));
 		TemplateInstance tempInstance = typeInstance.tempinst;
 		
 		assertEquals("Foo", new String(tempInstance.name.ident));
 		assertTrue(tempInstance.tiargs == null || tempInstance.tiargs.isEmpty());
 		
 		assertEquals(1, typeInstance.idents.size());
 		
 		TemplateInstanceWrapper wrap = (TemplateInstanceWrapper) typeInstance.idents.get(0);
 		tempInstance = wrap.tempinst;
 		
 		assertEquals("Bar", new String(tempInstance.name.ident));
 		assertTrue(tempInstance.tiargs == null || tempInstance.tiargs.isEmpty());
 	}
 	
 	public void testNestedQualifiedInstance() {
 		TypeIdentifier typeIdent = (TypeIdentifier) InternalSignature.toType(Signature.C_IDENTIFIER + "3Foo3Bar" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_PARAMETERS_BREAK + Signature.C_IDENTIFIER + "3One3Two" + Signature.C_TEMPLATE_INSTANCE + Signature.C_TEMPLATE_PARAMETERS_BREAK, new ASTNodeEncoder(AST.D1));
 		assertEquals("Foo", new String(typeIdent.ident.ident));
 		
 		assertEquals(3, typeIdent.idents.size());
 		
 		TemplateInstanceWrapper wrap = (TemplateInstanceWrapper) typeIdent.idents.get(0);
 		TemplateInstance tempInstance = wrap.tempinst;
 		
 		assertEquals("Bar", new String(tempInstance.name.ident));
 		assertTrue(tempInstance.tiargs == null || tempInstance.tiargs.isEmpty());
 		
 		assertEquals("One", new String(typeIdent.idents.get(1).ident));
 		
 		wrap = (TemplateInstanceWrapper) typeIdent.idents.get(2);
 		tempInstance = wrap.tempinst;
 		
 		assertEquals("Two", new String(tempInstance.name.ident));
 		assertTrue(tempInstance.tiargs == null || tempInstance.tiargs.isEmpty());
 	}
 	
 	public void testTypeGetSignature1() {
 		assertEquals("?3Foo!^i'", getTypeSignature("Foo!(int)"));
 	}
 	
 	public void testTypeGetSignature2() {
 		assertEquals("?3Foo!^i'n?3Bar!^i'", getTypeSignature("Foo!(int).Bar!(int)"));
 	}
 	
 	public void testTypeGetSignature3() {
 		assertEquals("?3one3two3Foo!^i'", getTypeSignature("one.two.Foo!(int)"));
 	}
 	
 	public void testTypeGetSignature4() {
 		assertEquals("?3one3two3Foo!^i'?5three4four3Bar!^i\'", getTypeSignature("one.two.Foo!(int).three.four.Bar!(int)"));
 	}
 	
 	public void testInstanceDotIdentifier() {
 		TypeInstance typeInstance = (TypeInstance) InternalSignature.toType("?3Foo!^?3Bar!\'?3Baz\'", new ASTNodeEncoder(AST.D1));
 		TemplateInstance tempInst = typeInstance.tempinst;
 		assertEquals("Foo", tempInst.name.toString());
 		assertEquals(1, tempInst.tiargs.size());
 		TypeInstance typeInstance2 = (TypeInstance) tempInst.tiargs.get(0);
 		TemplateInstance tempInst2 = typeInstance2.tempinst;
 		assertEquals("Bar", tempInst2.name.toString());
 		assertEquals("Baz", typeInstance2.idents.get(0).toString());
 	}
 	
 	public void testInstanceBug_20090206() {
 		TypeInstance instance = (TypeInstance) InternalSignature.toType("?11dynArgTypes!^?1i^?13sliceOffTuple!^?10FuncParams^?8SkipType6length\'n?3res^?5Tuple!^.?9BoundArgs4type~1.18.__dollar\'-31-minParamsLeft - SkipType.length\'n?3res", new ASTNodeEncoder(AST.D1));
 		Objects args = instance.tempinst.tiargs;
 		assertEquals(4, args.size());
 		
 		assertTrue(args.get(0) instanceof TypeIdentifier);
 		TypeIdentifier id1 = (TypeIdentifier) args.get(0);
 		assertEquals("i", id1.ident.toString());
 		
 		assertTrue(args.get(3) instanceof MinExp);
 	}
 	
 	public void testInstanceBug_20090207() {
 		TypeInstance instance = (TypeInstance) InternalSignature.toType("?14getDynArgTypes!^?10FuncParams^?12AllBoundArgs^?11minFuncArgs'n?3resn?4type", new ASTNodeEncoder(AST.D1));
 		assertEquals(2, instance.idents.size());
 		assertEquals("res", instance.idents.get(0).toString());
 		assertEquals("type", instance.idents.get(1).toString());
 	}
 	
 	public void testBug_20090214_1() {
 		TypeFunction func = (TypeFunction) InternalSignature.toType("F?1a?1bZv", new ASTNodeEncoder(AST.D1));
 		assertEquals(2, func.parameters.size());
 		assertEquals("a", func.parameters.get(0).toString());
 		assertEquals("b", func.parameters.get(1).toString());
 		assertEquals(STC.STCin, func.parameters.get(0).storageClass);
 		assertEquals(STC.STCin, func.parameters.get(1).storageClass);
 	}
 	
 	public void testBug_20090214_2() {
 		TypeFunction func = (TypeFunction) InternalSignature.toType("FJ?1a?1bZv", new ASTNodeEncoder(AST.D1));
 		assertEquals(2, func.parameters.size());
 		assertEquals("a", func.parameters.get(0).toString());
 		assertEquals("b", func.parameters.get(1).toString());
 		assertEquals(STC.STCout, func.parameters.get(0).storageClass);
 		assertEquals(STC.STCin, func.parameters.get(1).storageClass);
 	}
 	
 	public void testBug_20090214_3() {
 		TypeFunction func = (TypeFunction) InternalSignature.toType("F?1aJ?1bZv", new ASTNodeEncoder(AST.D1));
 		assertEquals(2, func.parameters.size());
 		assertEquals("a", func.parameters.get(0).toString());
 		assertEquals("b", func.parameters.get(1).toString());
 		assertEquals(STC.STCin, func.parameters.get(0).storageClass);
 		assertEquals(STC.STCout, func.parameters.get(1).storageClass);
 	}
 	
 	public void testBug_20090221() {
 		TypeAArray asoc = (TypeAArray) InternalSignature.toType("H?4cols?6Column!^?3flt^?4rows'", new ASTNodeEncoder(AST.D1));
 		assertEquals("Column!(flt, rows)[cols]", asoc.toString());
 	}
 	
 	
 	private String getTypeSignature(String type) {
 		Parser parser = new Parser(AST.D1, type + "x;");
 		parser.nextToken();
 		Module module = parser.parseModuleObj();
 		VarDeclaration var = (VarDeclaration) module.members.get(0);
 		return var.type.getSignature();
 	}
 	
 }
