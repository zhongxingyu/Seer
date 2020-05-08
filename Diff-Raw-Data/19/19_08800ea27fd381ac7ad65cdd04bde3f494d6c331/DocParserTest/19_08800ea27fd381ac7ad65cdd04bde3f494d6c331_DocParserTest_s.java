 /*
  * Copyright (C) 2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.md for licensing information.
  */
 
 package de.ailis.jasdoc.doc;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.ailis.jasdoc.tree.TreeParser;
 import de.ailis.jasdoc.types.MapType;
 import de.ailis.jasdoc.types.NumberType;
 import de.ailis.jasdoc.types.StringType;
 
 /**
  * Tests the DocParser class.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public final class DocParserTest
 {
     /** The configuration. */
     private Configuration config;
 
     /** The parser to test. */
     private TreeParser treeParser;
 
     /** The doc parser. */
     private DocParser docParser;
 
     /**
      * Silence the logger because we are going to do nasty stuff in this test
      * which will trigger expected errors.
      */
     @BeforeClass
     public static void silenceLogger()
     {
         final Logger logger = Logger.getLogger("de.ailis.jasdoc");
         logger.setLevel(Level.SEVERE);
     }
 
     /**
      * Set up the test.
      */
     @Before
     public void setUp()
     {
         this.config = new Configuration();
         this.treeParser = new TreeParser();
         this.docParser = new DocParser(this.config);
     }
 
     /**
      * Parses the specified resource.
      *
      * @param filename
      *            The resource filename relative to this class.
      * @return The parsed global namespace.
      * @throws IOException
      *             If parsing fails.
      */
     private GlobalDoc parse(final String filename) throws IOException
     {
         final InputStream stream =
             this.getClass().getResourceAsStream(filename);
         if (stream == null) throw new FileNotFoundException(filename);
         try
         {
             this.treeParser.parseStream(stream);
             return this.docParser.parse(this.treeParser.getTree());
         }
         finally
         {
             stream.close();
         }
     }
 
     /**
      * Tests parsing a normal function.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testFunction() throws IOException
     {
         final GlobalDoc global = parse("function.js");
         assertEquals(1, global.getFunctions().size());
         final FunctionDoc func = global.getFunctions().iterator().next();
         assertEquals("funcName", func.getName());
         assertEquals("Short description. A longer description which is so "
             + "long it needs another line to tell you how long it is.",
             func.getDescription());
         assertEquals("Short description.", func.getShortDescription());
         assertEquals("index.html#funcName", func.getUrl());
         assertEquals("The return value. Again we have a long description "
             + "which fills multiple lines. This time it is even so long it "
             + "needs another line.", func.getResult().getDescription());
         assertEquals("The return value.", func.getResult()
             .getShortDescription());
         assertEquals("number|string",
             func.getResult().getType().toExpression());
         assertSame(global, func.getNamespace());
         assertEquals(2, func.getParameters().size());
         final Iterator<ParameterDoc> it = func.getParameters().iterator();
         final ParameterDoc param1 = it.next();
         final ParameterDoc param2 = it.next();
         assertEquals("param1", param1.getName());
         assertEquals("number", param1.getType().toExpression());
         assertEquals("The first parameter. Another long description which "
             + "needs more lines.", param1.getDescription());
         assertEquals("The first parameter.", param1.getShortDescription());
         assertEquals("param2", param2.getName());
         assertEquals("string", param2.getType().toExpression());
         assertEquals("The second parameter with a short description which "
             + "spans multiple lines. And some more stuff.",
             param2.getDescription());
         assertEquals("The second parameter with a short description which "
             + "spans multiple lines.",
             param2.getShortDescription());
     }
 
     /**
      * Tests parsing an assigned function.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testAssignedFunction() throws IOException
     {
         final GlobalDoc global = parse("assignedFunction.js");
         assertEquals(1, global.getFunctions().size());
         final FunctionDoc func = global.getFunctions().iterator().next();
         assertEquals("foo", func.getName());
         assertEquals("Foo.", func.getDescription());
         assertEquals(1, func.getParameters().size());
         assertSame(global, func.getNamespace());
     }
 
     /**
      * Tests parsing a var declared function.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testVarFunction() throws IOException
     {
         final GlobalDoc global = parse("varFunction.js");
         assertEquals(1, global.getFunctions().size());
         final FunctionDoc func = global.getFunctions().iterator().next();
         assertEquals("varFunc", func.getName());
         assertEquals("Var Func", func.getDescription());
         assertEquals(1, func.getParameters().size());
         assertSame(global, func.getNamespace());
     }
 
     /**
      * Tests parsing a bunch of differently var declared functions.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testVarFunctions() throws IOException
     {
         final GlobalDoc global = parse("varFunctions.js");
         assertEquals(4, global.getFunctions().size());
         final Iterator<FunctionDoc> functions =
             global.getFunctions().iterator();
 
         FunctionDoc func = functions.next();
         assertEquals("varFunc1", func.getName());
         assertEquals("Var Func 1", func.getDescription());
         assertSame(global, func.getNamespace());
 
         func = functions.next();
         assertEquals("varFunc2", func.getName());
         assertEquals("Var Func 2", func.getDescription());
         assertSame(global, func.getNamespace());
 
         func = functions.next();
         assertEquals("varFunc3", func.getName());
         assertEquals("Var Func 3", func.getDescription());
         assertSame(global, func.getNamespace());
 
         func = functions.next();
         assertEquals("varFunc4", func.getName());
         assertEquals("", func.getDescription());
         assertSame(global, func.getNamespace());
     }
 
     /**
      * Tests parsing a scoped function.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testScopedFunction() throws IOException
     {
         final GlobalDoc global = parse("scopedFunction.js");
         assertEquals(0, global.getFunctions().size());
         assertEquals(1, global.getNamespaces().size());
         final NamespaceDoc scope = global.getNamespaces().iterator().next();
         assertEquals(0, scope.getNamespaces().size());
         assertEquals(1, scope.getFunctions().size());
         assertEquals("someScope", scope.getName());
         assertSame(global, scope.getSuperNamespace());
         final FunctionDoc func = scope.getFunctions().iterator().next();
         assertEquals("bar", func.getName());
         assertEquals("Bar.", func.getDescription());
         assertEquals(2, func.getParameters().size());
         assertSame(scope, func.getNamespace());
     }
 
     /**
      * Tests parsing a deep scoped function.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testDeepScopedFunction() throws IOException
     {
         final GlobalDoc global = parse("deepScopedFunction.js");
         assertEquals(0, global.getFunctions().size());
         assertEquals(1, global.getNamespaces().size());
         final NamespaceDoc scope = global.getNamespaces().iterator().next();
         assertEquals("someScope", scope.getName());
         assertEquals(1, scope.getNamespaces().size());
         assertEquals(0, scope.getFunctions().size());
         final NamespaceDoc scope2 = scope.getNamespaces().iterator().next();
         assertEquals("deeper", scope2.getName());
         assertEquals(0, scope2.getNamespaces().size());
         assertEquals(1, scope2.getFunctions().size());
         assertSame(global, scope.getSuperNamespace());
         final FunctionDoc func = scope2.getFunctions().iterator().next();
         assertEquals("foobar", func.getName());
         assertEquals("Foo Bar.", func.getDescription());
         assertEquals(0, func.getParameters().size());
         assertSame(scope2, func.getNamespace());
     }
 
     /**
      * Tests parsing a namespace.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testNamespace() throws IOException
     {
         final GlobalDoc global = parse("namespace.js");
         assertEquals(1, global.getNamespaces().size());
         final Iterator<NamespaceDoc> namespaces =
             global.getNamespaces().iterator();
         final NamespaceDoc namespace = namespaces.next();
         assertEquals("namespace1", namespace.getName());
         assertEquals("Namespace Doc", namespace.getDescription());
     }
 
     /**
      * Tests parsing sub scopes.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testSubNamespaces() throws IOException
     {
         final GlobalDoc global = parse("subNamespaces.js");
         assertEquals(1, global.getNamespaces().size());
         Iterator<NamespaceDoc> scopes = global.getNamespaces().iterator();
         final NamespaceDoc scope = scopes.next();
         assertEquals("parent", scope.getName());
         assertEquals("Parent Scope", scope.getDescription());
         assertSame(global, scope.getSuperNamespace());
         assertEquals(2, scope.getVariables().size());
         assertEquals(2, scope.getNamespaces().size());
         scopes = scope.getNamespaces().iterator();
 
         NamespaceDoc sub = scopes.next();
         assertEquals("sub1", sub.getName());
         assertEquals("Sub scope 1.", sub.getDescription());
         assertSame(scope, sub.getSuperNamespace());
         assertEquals(1, sub.getNamespaces().size());
 
         final NamespaceDoc subSub = sub.getNamespaces().iterator().next();
         assertEquals("sub", subSub.getName());
         assertEquals("Sub sub scope", subSub.getDescription());
         assertSame(sub, subSub.getSuperNamespace());
         assertEquals(0, subSub.getNamespaces().size());
 
         sub = scopes.next();
         assertEquals("sub2", sub.getName());
         assertEquals("Sub scope 2.", sub.getDescription());
         assertSame(scope, sub.getSuperNamespace());
         assertEquals(0, sub.getNamespaces().size());
     }
 
     /**
      * Tests parsing a class.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testClass() throws IOException
     {
         final GlobalDoc global = parse("class.js");
         assertEquals(1, global.getFunctions().size());
         assertEquals(2, global.getClasses().size());
         final Iterator<ClassDoc> classes = global.getClasses().iterator();
         ClassDoc cls = classes.next();
         assertEquals("Class1", cls.getName());
         assertEquals("Class description.",
             cls.getDescription());
         assertEquals("Class1.html", cls.getUrl());
         final ConstructorDoc constructor = cls.getConstructor();
         assertEquals("Class1", constructor.getName());
         assertEquals("Constructor description.",
             constructor.getDescription());
         cls = classes.next();
         assertEquals("Class2", cls.getName());
     }
 
     /**
      * Tests parsing a method.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testMethod() throws IOException
     {
         final GlobalDoc global = parse("method.js");
         final ClassDoc cls = global.getClasses().iterator().next();
         assertEquals(1, cls.getMethods().size());
         final MethodDoc method = cls.getMethods().iterator().next();
         assertEquals("myMethod", method.getName());
         assertEquals("Method description.",
             method.getDescription());
     }
 
     /**
      * Tests a param tag without a description.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testNoDescParam() throws IOException
     {
         final GlobalDoc global = parse("noDescParam.js");
         final FunctionDoc func = global.getFunctions().iterator().next();
         final ParameterDoc param = func.getParameters().get(0);
         assertEquals("number", param.getType().toExpression());
         assertEquals("a", param.getName());
         assertEquals("", param.getDescription());
     }
 
     /**
      * Tests parsing a method.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testStaticMethod() throws IOException
     {
         final GlobalDoc global = parse("staticMethod.js");
         final ClassDoc cls = global.getClasses().iterator().next();
         final Iterator<MethodDoc> methods = cls.getMethods().iterator();
         assertFalse(methods.next().getModifier().isStatic());
         assertTrue(methods.next().getModifier().isStatic());
     }
 
     /**
      * Tests accessibility of classes, constructs and functions. Private symbols
      * are included.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testAccessibilityWithPrivateIncluded() throws IOException
     {
         this.config.setPrivateIncluded(true);
 
         final GlobalDoc global = parse("accessibility.js");
         assertEquals(3, global.getClasses().size());
         final Iterator<ClassDoc> classes = global.getClasses().iterator();
 
         ClassDoc cls = classes.next();
         ConstructorDoc constructor = cls.getConstructor();
         assertEquals("Class1", cls.getName());
         assertTrue(constructor.getModifier().isPrivate());
         assertFalse(constructor.getModifier().isProtected());
         assertFalse(constructor.getModifier().isPublic());
 
         cls = classes.next();
         constructor = cls.getConstructor();
         assertEquals("Class2", cls.getName());
         assertFalse(constructor.getModifier().isPrivate());
         assertTrue(constructor.getModifier().isProtected());
         assertFalse(constructor.getModifier().isPublic());
 
         cls = classes.next();
         constructor = cls.getConstructor();
         assertEquals("Class3", cls.getName());
         assertFalse(constructor.getModifier().isPrivate());
         assertFalse(constructor.getModifier().isProtected());
         assertTrue(constructor.getModifier().isPublic());
     }
 
     /**
      * Tests accessibility of classes, constructs and functions. Private symbols
      * are excluded.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testAccessibilityWithPrivateExcluded() throws IOException
     {
         this.config.setPrivateIncluded(false);
 
         final GlobalDoc global = parse("accessibility.js");
         assertEquals(3, global.getClasses().size());
         final Iterator<ClassDoc> classes = global.getClasses().iterator();
 
         ClassDoc cls = classes.next();
         ConstructorDoc constructor = cls.getConstructor();
         assertEquals("Class1", cls.getName());
         assertNull(constructor);
 
         cls = classes.next();
         constructor = cls.getConstructor();
         assertEquals("Class2", cls.getName());
         assertFalse(constructor.getModifier().isPrivate());
         assertTrue(constructor.getModifier().isProtected());
         assertFalse(constructor.getModifier().isPublic());
 
         cls = classes.next();
         constructor = cls.getConstructor();
         assertEquals("Class3", cls.getName());
         assertFalse(constructor.getModifier().isPrivate());
         assertFalse(constructor.getModifier().isProtected());
         assertTrue(constructor.getModifier().isPublic());
     }
 
     /**
      * Tests accessibility of classes, constructs and functions. Private and
      * protected symbols are excluded.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testAccessibilityWithProtectedExcluded() throws IOException
     {
         this.config.setPrivateIncluded(false);
         this.config.setProtectedIncluded(false);
 
         final GlobalDoc global = parse("accessibility.js");
         assertEquals(3, global.getClasses().size());
         final Iterator<ClassDoc> classes = global.getClasses().iterator();
 
         ClassDoc cls = classes.next();
         ConstructorDoc constructor = cls.getConstructor();
         assertEquals("Class1", cls.getName());
         assertNull(constructor);
 
         cls = classes.next();
         constructor = cls.getConstructor();
         assertEquals("Class2", cls.getName());
         assertNull(constructor);
 
         cls = classes.next();
         constructor = cls.getConstructor();
         assertEquals("Class3", cls.getName());
         assertFalse(constructor.getModifier().isPrivate());
         assertFalse(constructor.getModifier().isProtected());
         assertTrue(constructor.getModifier().isPublic());
     }
 
     /**
      * Tests parsing a var declared property.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testVarProperty() throws IOException
     {
         final GlobalDoc global = parse("varProperty.js");
         assertEquals(1, global.getVariables().size());
         final VariableDoc property = global.getVariables().iterator().next();
         assertEquals("varProp", property.getName());
         assertEquals("Property description.", property.getDescription());
         assertEquals("number", property.getType().toExpression());
         assertSame(global, property.getNamespace());
     }
 
     /**
      * Tests parsing a bunch of differently var declared properties.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testVarProperties() throws IOException
     {
         final GlobalDoc global = parse("varProperties.js");
         assertEquals(3, global.getVariables().size());
         final Iterator<VariableDoc> properties =
             global.getVariables().iterator();
 
         VariableDoc property = properties.next();
         assertEquals("prop1", property.getName());
         assertEquals("Prop 1", property.getDescription());
         assertEquals("*", property.getType().toExpression());
         assertSame(global, property.getNamespace());
 
         property = properties.next();
         assertEquals("prop2", property.getName());
         assertEquals("", property.getDescription());
         assertEquals("*", property.getType().toExpression());
         assertSame(global, property.getNamespace());
 
         property = properties.next();
         assertEquals("prop3", property.getName());
         assertEquals("Prop 3", property.getDescription());
         assertEquals("!string", property.getType().toExpression());
         assertSame(global, property.getNamespace());
     }
 
     /**
      * Tests parsing a normal property.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testProperty() throws IOException
     {
         final GlobalDoc global = parse("property.js");
         assertEquals(1, global.getVariables().size());
         final VariableDoc property = global.getVariables().iterator().next();
         assertEquals("testProperty", property.getName());
         assertEquals("index.html#testProperty", property.getUrl());
         assertSame(global, property.getNamespace());
         // For some reason (Rhino bug?) the JsDoc can not be read for this
         // kind of property
         // assertEquals("Short description. A longer description which is so " +
         // "long it needs another line to tell you how long it is.",
         // property.getDescription());
         // assertEquals("boolean", property.getType().getExpression());
     }
 
     /**
      * Tests parsing a property value.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testPropertyValue() throws IOException
     {
         final GlobalDoc global = parse("propertyValue.js");
         assertEquals(1, global.getVariables().size());
         final VariableDoc property = global.getVariables().iterator().next();
         assertEquals("testProperty", property.getName());
         assertEquals("index.html#testProperty", property.getUrl());
         assertSame(global, property.getNamespace());
         assertEquals("Short description. A longer description which is so "
             + "long it needs another line to tell you how long it is.",
             property.getDescription());
         assertEquals("boolean", property.getType().toExpression());
     }
 
     /**
      * Tests scoped property.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testScopedProperty() throws IOException
     {
         final GlobalDoc global = parse("scopedProperty.js");
         assertEquals(1, global.getNamespaces().size());
         final NamespaceDoc scope = global.getNamespaces().iterator().next();
         assertEquals(0, scope.getNamespaces().size());
         assertEquals(1, scope.getVariables().size());
         assertEquals("someScope", scope.getName());
         assertSame(global, scope.getSuperNamespace());
         final VariableDoc func = scope.getVariables().iterator().next();
         assertEquals("prop", func.getName());
         assertEquals("Scoped prop.", func.getDescription());
         assertSame(scope, func.getNamespace());
     }
 
     /**
      * Tests parsing a deep scoped property.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testDeepScopedProperty() throws IOException
     {
         final GlobalDoc global = parse("deepScopedProperty.js");
         assertEquals(0, global.getVariables().size());
         assertEquals(1, global.getNamespaces().size());
         final NamespaceDoc scope = global.getNamespaces().iterator().next();
         assertEquals("someScope", scope.getName());
         assertEquals(1, scope.getNamespaces().size());
         assertEquals(0, scope.getVariables().size());
         final NamespaceDoc scope2 = scope.getNamespaces().iterator().next();
         assertEquals("deeper", scope2.getName());
         assertEquals(0, scope2.getNamespaces().size());
         assertEquals(1, scope2.getVariables().size());
         assertSame(global, scope.getSuperNamespace());
         final VariableDoc prop = scope2.getVariables().iterator().next();
         assertEquals("foobar", prop.getName());
         assertEquals("Foo Bar.", prop.getDescription());
         assertSame(scope2, prop.getNamespace());
     }
 
     /**
      * Tests parsing sub scopes in a variable declaration.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testVarSubScopes() throws IOException
     {
         final GlobalDoc global = parse("varSubScopes.js");
         assertEquals(1, global.getNamespaces().size());
         Iterator<NamespaceDoc> scopes = global.getNamespaces().iterator();
         final NamespaceDoc scope = scopes.next();
         assertEquals("parent", scope.getName());
         assertEquals("Parent Scope", scope.getDescription());
         assertSame(global, scope.getSuperNamespace());
         assertEquals(2, scope.getNamespaces().size());
         scopes = scope.getNamespaces().iterator();
 
         NamespaceDoc sub = scopes.next();
         assertEquals("sub1", sub.getName());
         assertEquals("Sub scope 1.", sub.getDescription());
         assertSame(scope, sub.getSuperNamespace());
         assertEquals(1, sub.getNamespaces().size());
 
         final NamespaceDoc subSub = sub.getNamespaces().iterator().next();
         assertEquals("sub", subSub.getName());
         assertEquals("Sub sub scope", subSub.getDescription());
         assertSame(sub, subSub.getSuperNamespace());
         assertEquals(0, subSub.getNamespaces().size());
 
         sub = scopes.next();
         assertEquals("sub2", sub.getName());
         assertEquals("Sub scope 2.", sub.getDescription());
         assertSame(scope, sub.getSuperNamespace());
         assertEquals(0, sub.getNamespaces().size());
     }
 
     /**
      * Tests marging two scopes.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testScopeConflict() throws IOException
     {
         final GlobalDoc global = parse("scopeConflict.js");
         assertEquals(1, global.getNamespaces().size());
         final Iterator<NamespaceDoc> scopes = global.getNamespaces().iterator();
         final NamespaceDoc scope = scopes.next();
         assertEquals("scope", scope.getName());
         assertEquals("Scope 1 Doc Scope 2 Doc", scope.getDescription());
         assertSame(global, scope.getSuperNamespace());
         assertEquals(2, scope.getFunctions().size());
     }
 
     /**
      * Tests sub scoped functions.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testSubScopedFunctions() throws IOException
     {
         final GlobalDoc global = parse("subScopedFunctions.js");
         assertEquals(1, global.getNamespaces().size());
         final Iterator<NamespaceDoc> scopes = global.getNamespaces().iterator();
         final NamespaceDoc scope = scopes.next();
         assertEquals("someScope", scope.getName());
         assertEquals("Some scope.", scope.getDescription());
         assertSame(global, scope.getSuperNamespace());
         assertEquals(2, scope.getFunctions().size());
         final Iterator<FunctionDoc> functions = scope.getFunctions().iterator();
         FunctionDoc func = functions.next();
         assertEquals("bar", func.getName());
         assertEquals("Bar function.", func.getDescription());
         assertSame(scope, func.getNamespace());
         func = functions.next();
         assertEquals("foo", func.getName());
         assertEquals("Foo function.", func.getDescription());
         assertSame(scope, func.getNamespace());
     }
 
     /**
      * Tests enum.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testEnum() throws IOException
     {
         final GlobalDoc global = parse("enum.js");
         assertEquals(1, global.getEnums().size());
         final Iterator<EnumDoc> enums = global.getEnums().iterator();
         final EnumDoc e = enums.next();
         assertEquals("Enum1", e.getName());
         assertEquals("Enum description.",
             e.getDescription());
         assertEquals("Enum1.html", e.getUrl());
         assertSame(global, e.getNamespace());
         assertEquals(2, e.getConstants().size());
         final Iterator<EnumConstantDoc> props = e.getConstants().iterator();
         EnumConstantDoc prop = props.next();
         assertEquals("FIRST", prop.getName());
         assertEquals("number", prop.getType().toExpression());
         assertEquals("First constant.", prop.getDescription());
         assertTrue(prop.getModifier().isConst());
         prop = props.next();
         assertEquals("SECOND", prop.getName());
         assertEquals("number", prop.getType().toExpression());
         assertEquals("Second constant.", prop.getDescription());
         assertTrue(prop.getModifier().isConst());
     }
 
     /**
      * Tests parsing broken tags.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testBrokenTags() throws IOException
     {
         parse("brokenTags.js");
     }
 
     /**
      * Tests parsing a interface.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testInterface() throws IOException
     {
         final GlobalDoc global = parse("interface.js");
         assertEquals(1, global.getInterfaces().size());
         final Iterator<InterfaceDoc> interfaces =
             global.getInterfaces().iterator();
         final InterfaceDoc intf = interfaces.next();
         assertEquals("Interface1", intf.getName());
         assertEquals("Interface description.",
             intf.getDescription());
         assertEquals("Interface1.html", intf.getUrl());
         assertSame(global, intf.getNamespace());
         assertEquals(1, intf.getMethods().size());
         final MethodDoc method = intf.getMethods().iterator().next();
         assertEquals("intfMethod", method.getName());
         assertEquals("Interface method.", method.getDescription());
         // TODO? assertSame(intf, method.getNamespace().getNamespace());
     }
 
     /**
      * Tests HTML code in parameter description.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testHtmlInParam() throws IOException
     {
         final GlobalDoc global = parse("htmlInParam.js");
         final FunctionDoc func = global.getFunctions().iterator().next();
         final ParameterDoc paramDoc = func.getParameters().get(0);
        assertEquals("Bold Italic.", paramDoc.getShortDescription());
        assertEquals("<b>Bold</b> \n<i>Italic</i>. \n"
            + "<b>Broken <i>tag structure</i></b>.", paramDoc.getDescription());
         assertEquals("!Array.<string>", paramDoc.getType().toExpression());
     }
 
     /**
      * Tests HTML code in class description.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testHtmlInClass() throws IOException
     {
         final GlobalDoc global = parse("htmlInClass.js");
         final ClassDoc cls = global.getClasses().iterator().next();
        assertEquals("Bold Italic.", cls.getShortDescription());
         assertEquals("<b>Bold</b> \n<i>Italic</i>. \n"
             + "<b>Broken <i>tag structure</i></b>.", cls.getDescription());
     }
 
     /**
      * Tests HTML code in namespace description.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testHtmlInNamespace() throws IOException
     {
         final GlobalDoc global = parse("htmlInNamespace.js");
         final NamespaceDoc scope = global.getNamespaces().iterator().next();
        assertEquals("Bold Italic.", scope.getShortDescription());
         assertEquals("<b>Bold</b> \n<i>Italic</i>. \n"
             + "<b>Broken <i>tag structure</i></b>.", scope.getDescription());
     }
 
     /**
      * Tests HTML code in parameter description.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testHtmlInFunction() throws IOException
     {
         final GlobalDoc global = parse("htmlInFunction.js");
         final FunctionDoc func = global.getFunctions().iterator().next();
        assertEquals("Bold Italic.", func.getShortDescription());
        assertEquals("<b>Bold</b> \n<i>Italic</i>. \n"
             + "<b>Broken <i>tag structure</i></b>.", func.getDescription());
     }
 
     /**
      * Tests pre-formatted HTML code.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testPreHtml() throws IOException
     {
         final GlobalDoc global = parse("preHtml.js");
         final FunctionDoc func = global.getFunctions().iterator().next();
         assertEquals(
             "Some normal stuff without any important whitespace. \n"
                 + "<pre>\n"
                 + "and now\n"
                 + "  now is\n"
                 + "    important.\n"
                 + "</pre> Not important any more", func.getDescription());
     }
 
     /**
      * Tests parsing a parameter with a space character in the type expression.
      *
      * @throws IOException
      *             If JavaScript parsing fails.
      */
     @Test
     public void testSpaceInTypeExpression() throws IOException
     {
         final GlobalDoc global = parse("spaceInTypeExpression.js");
         assertEquals(1, global.getFunctions().size());
         final FunctionDoc func = global.getFunctions().iterator().next();
         final ParameterDoc param = func.getParameters().get(0);
         assertEquals("test", param.getName());
         assertEquals("Param doc.", param.getDescription());
         assertEquals(new MapType(new StringType(), new NumberType()),
             param.getType());
 
         final ResultDoc result = func.getResult();
         assertEquals("Return doc.", result.getDescription());
         assertEquals(new MapType(new NumberType(), new StringType()),
             result.getType());
     }
 }
