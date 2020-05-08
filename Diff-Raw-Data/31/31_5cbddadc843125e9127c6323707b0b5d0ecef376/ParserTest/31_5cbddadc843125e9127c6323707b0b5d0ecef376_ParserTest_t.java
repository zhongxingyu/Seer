 /*******************************************************************************
  * Copyright (c) 2012 Jens von Pilgrim
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 
 package de.jevopi.mitra2;
 
 import static de.jevopi.ecore.utils.DynEcoreUtil.xeval;
 import static java.util.Collections.emptyMap;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import de.jevopi.ecore.utils.DynEcoreDump;
 import de.jevopi.mitra2.context.PathHandler.ResourceType;
 import de.jevopi.mitra2.context.ProjectContext;
 import de.jevopi.mitra2.message.IMessageAcceptor;
 import de.jevopi.mitra2.mitra.Module;
 import de.jevopi.mitra2.mitra.ReferenceType;
 import de.jevopi.utils.FileUtils;
 
 /**
  * @author Jens von Pilgrim (developer@jevopi.de)
  * @since Jan 16, 2012
  */
 public class ParserTest {
 
 	public final static String MITRA_PATH = "src/mitraParserTest/";
 
 	ProjectContext projectContext;
 
 	/**
 	 * Logger for this class
 	 */
 	private static final Logger log = Logger.getLogger(ParserTest.class
 		.getName());
 
 	public EObject load(String strURIFileName) throws IOException {
 
		URI uri =
 			projectContext.getPathHandler().resolve(ResourceType.MODULE,
 					strURIFileName);
 		Resource res = projectContext.getResourceSet().createResource(uri);
 
 		res.load(emptyMap());
 
 		for (EObject content : res.getContents()) {
 			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(content);
 			if (diagnostic.getSeverity() == Diagnostic.ERROR) {
 				throw new IOException("Error validating " + strURIFileName
 					+ ": " + diagnostic);
 			}
 		}
 
 		return res.getContents().get(0);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 
 		MitraStandaloneSetup.doSetup();
 
 		projectContext = new ProjectContext();
 
 		File f = new File(FileUtils.toOS(MITRA_PATH));
 		String strPath = f.getAbsolutePath();
 
 		projectContext.getPathHandler().addPath(ResourceType.METAMODEL,
 				URI.createFileURI(strPath));
 		projectContext.getPathHandler().addPath(ResourceType.MODEL,
 				URI.createFileURI(strPath));
 		projectContext.getPathHandler().addPath(ResourceType.MODULE,
 				URI.createFileURI(strPath));
 		
 		projectContext.setMessageAcceptor(IMessageAcceptor.JDK_LOGGING_ACCEPTOR);
 
 		// setup.addRegisterEcoreFile("../mitra.dsl/src-gen/mitra/mitra.ecore");
 		// mitraResourceFactory.register();
 
 	}
 
 	@Test
 	public void testResourceLoading() throws IOException {
		EObject m = load(MITRA_PATH + "EmptyModule.mitra");
 		assertNotNull(m);
 	}
 
 	// uncommented, path must be resolved
 	// @Test
 	// public void testManualLoading() throws IOException, RecognitionException
 	// {
 	//
 	// InputStream is;
 	// EObject m;
 	//
 	// is = ParserTest.class.getResourceAsStream("EmptyModule.mitra");
 	// DynMitraLoader loader = new DynMitraLoader();
 	// m = loader.loadModule(is);
 	//		
 	//		
 	// // Resource res = m.eResource();
 	// // assertNotNull(res);
 	//		
 	//		
 	// assertEquals(0, loader.getErrors().size());
 	// assertNotNull(m);
 	//		
 	// is = ParserTest.class.getResourceAsStream("OnlyMetamodels.mitra");
 	// m = loader.loadModule(is);
 	// assertEquals(0, loader.getErrors().size());
 	// assertNotNull(m);
 	//		
 	// }
 
 	@Test
 	public void testMetamodelDeclaration() throws IOException,
 			RecognitionException {
 		EObject m = load("OnlyMetamodels.mitra");
 		assertNotNull(m);
 		assertEquals("Module", m.eClass().getName());
 
 		// System.out.println(ModelToConcreteSyntax.model2Text(m));
 
 		assertEquals("OnlyMetamodels", xeval(m, "name"));
 
 		assertEquals("MM1", xeval(m, "metamodelDeclarations[0].name"));
 		assertEquals("ecoreFile",
 				xeval(m, "metamodelDeclarations[0].properties[0].name"));
 		assertEquals("simpleER.ecore",
 				xeval(m, "metamodelDeclarations[0].properties[0].value"));
 	}
 
 	@Test
 	public void testRuleDeclarations() throws IOException, RecognitionException {
 		EObject eobj = load("RuleDeclarations.mitra");
 		EcoreUtil.resolveAll(eobj);
 
 		assertNotNull(eobj);
 		assertEquals("Module", eobj.eClass().getName());
 
 		Module m = (Module) eobj;
 
 		assertEquals("RuleDeclarations", xeval(m, "name"));
 
 		// test metamodel declaration
 		assertEquals("MM3", xeval(m, "metamodelDeclarations[0].name"));
 		assertEquals("ecoreFile",
 				xeval(m, "metamodelDeclarations[0].properties[0].name"));
 		assertEquals("simpleER.ecore",
 				xeval(m, "metamodelDeclarations[0].properties[0].value"));
 		assertEquals("prop1",
 				xeval(m, "metamodelDeclarations[0].properties[1].name"));
 		assertEquals("value1",
 				xeval(m, "metamodelDeclarations[0].properties[1].value"));
 		assertEquals("OLD0", xeval(m, "metamodelDeclarations[0].replaces[0]"));
 		assertEquals("OLD1", xeval(m, "metamodelDeclarations[0].replaces[1]"));
 
 		// test very simple declaration:
 		// called rule0 ( ) MM3.r0
 		assertEquals("rule0", xeval(m, "ruleDeclarations[0].name"));
 		assertEquals("called", xeval(m, "ruleDeclarations[0].exec"));
 		// assertEquals("r0", xeval(m, "ruleDeclarations[0].returnType.name"));
 
 		// test formal parameter
 		// called multi rule2 ( MM3.t1 par1 ) void
 		assertEquals("rule1", xeval(m, "ruleDeclarations[1].name"));
 		assertEquals("called", xeval(m, "ruleDeclarations[1].exec"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[1].formalParameters[0].vardecl.name"));
 
 		ReferenceType type =
 			(ReferenceType) xeval(m,
 					"ruleDeclarations[1].formalParameters[0].vardecl.type");
 
 		assertNotNull("Probably link not resolved, use a resource set!",
 				type.getMetamodelDeclaration());
 
 		assertEquals(
 				"MM3",
 				xeval(m,
 						"ruleDeclarations[1].formalParameters[0].vardecl.type.metamodelDeclaration.name"));
 		assertEquals(
 				"Schema",
 				xeval(m,
 						"ruleDeclarations[1].formalParameters[0].vardecl.type.eClassifier.name"));
 
 		// test formal parameter list
 		// called multi rule2 ( MM3.t0 par0, MM3.t1 par1, MM3.t2 par2): (return MM3.r0)
 		assertEquals("rule2", xeval(m, "ruleDeclarations[2].name"));
 		assertEquals("called", xeval(m, "ruleDeclarations[2].exec"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[2].formalParameters[0].vardecl.name"));
 		assertEquals(
 				"par1",
 				xeval(m, "ruleDeclarations[2].formalParameters[1].vardecl.name"));
 		assertEquals(
 				"par2",
 				xeval(m, "ruleDeclarations[2].formalParameters[2].vardecl.name"));
 		assertEquals(
 				"ret0",
 				xeval(m, "ruleDeclarations[2].returnParameters[0].vardecl.name"));
 
 		// called multi rule3 ( MM3.t0 par0, MM3.t1 par1, MM3.t2 par2): 
 		//						(return MM3.r0 ret0, return MM3.r1 ret1)
 		assertEquals("rule3", xeval(m, "ruleDeclarations[3].name"));
 		assertEquals("called", xeval(m, "ruleDeclarations[3].exec"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[3].formalParameters[0].vardecl.name"));
 		assertEquals(
 				"par1",
 				xeval(m, "ruleDeclarations[3].formalParameters[1].vardecl.name"));
 		assertEquals(
 				"par2",
 				xeval(m, "ruleDeclarations[3].formalParameters[2].vardecl.name"));
 		assertEquals(
 				"ret0",
 				xeval(m, "ruleDeclarations[3].returnParameters[0].vardecl.name"));
 		assertEquals(
 				"ret1",
 				xeval(m, "ruleDeclarations[3].returnParameters[1].vardecl.name"));
 
 		// called rule4 ( MM3.t0 par0 ) implements rule1(MM3.t0), ruleX(MM3.t0)
 		assertEquals("rule4", xeval(m, "ruleDeclarations[4].name"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[4].formalParameters[0].vardecl.name"));
 		assertEquals(
 				"rule1",
 				xeval(m,
 						"ruleDeclarations[4].implementedRules[0].ruleDeclaration.name"));
 		assertEquals(
 				"Schema",
 				xeval(m,
 						"ruleDeclarations[4].implementedRules[0].parameterReferences[0].type.eClassifier.name"));
 		assertEquals(
 				"ruleX",
 				xeval(m,
 						"ruleDeclarations[4].implementedRules[1].ruleDeclaration.name"));
 
 		// alled rule5 ( MM3.t0 par0 ) implements rule1(MM3.t0)  overloads ruleX(MM3.t0), ruleY(MM3.t0) 
 		assertEquals("rule5", xeval(m, "ruleDeclarations[5].name"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[5].formalParameters[0].vardecl.name"));
 		assertEquals(
 				"rule1",
 				xeval(m,
 						"ruleDeclarations[5].implementedRules[0].ruleDeclaration.name"));
 		assertEquals(
 				"Schema",
 				xeval(m,
 						"ruleDeclarations[5].implementedRules[0].parameterReferences[0].type.eClassifier.name"));
 		assertEquals(
 				"ruleX",
 				xeval(m,
 						"ruleDeclarations[5].overriddenRule.ruleDeclaration.name"));
 		assertEquals(
 				"Schema",
 				xeval(m,
 						"ruleDeclarations[5].overriddenRule.parameterReferences[0].type.eClassifier.name"));
 
 		// TODO test trigger, mapped return parameters
 
 	}
 
 	/**
 	 * 
 	 module RuleDeclarationsAbstractNative { metamodel MM3(prop0="value0",
 	 * prop1="value1") replaces OLD0, OLD1; called rule0 ( MM3.t0 par0, MM3.t1
 	 * par1, MM3.t2 par2 ) MM3.r0 abstract; called rule1 ( MM3.t0 par0, MM3.t1
 	 * par1, MM3.t2 par2 ) MM3.r0 native ( class="my.test.Rule",
 	 * method="Simple"); }
 	 * 
 	 * @throws IOException
 	 * @throws RecognitionException
 	 */
 	@Ignore("Metamodels are to be real and no fake")
 	@Test
 	public void testNativeAndAbstractRuleDeclarations() throws IOException,
 			RecognitionException {
 
 		EObject m = load("RuleDeclarationsAbstractNative.mitra");
 		assertNotNull(m);
 
 		assertEquals("RuleDeclarationsAbstractNative", xeval(m, "name"));
 
 		assertEquals("rule0", xeval(m, "ruleDeclarations[0].name"));
 		assertEquals("abstract", xeval(m, "ruleDeclarations[0].exec"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[0].formalParameters[0].vardecl.name"));
 		assertEquals(
 				"par2",
 				xeval(m, "ruleDeclarations[0].formalParameters[2].vardecl.name"));
 		// assertEquals(true, xeval(m, "ruleDeclarations[0].abstract"));
 		// rev. 629: new execution modifier: abstract
 		assertNull(xeval(m, "ruleDeclarations[0].nativeSpec"));
 
 		assertEquals("rule1", xeval(m, "ruleDeclarations[1].name"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[1].formalParameters[0].vardecl.name"));
 		assertEquals(
 				"par2",
 				xeval(m, "ruleDeclarations[1].formalParameters[2].vardecl.name"));
 		// assertEquals(false, xeval(m, "ruleDeclarations[1].abstract"));
 		// rev. 629: new execution modifier: abstract
 		assertEquals("class",
 				xeval(m, "ruleDeclarations[1].nativeSpec.properties[0].name"));
 		assertEquals("my.test.Rule",
 				xeval(m, "ruleDeclarations[1].nativeSpec.properties[0].value"));
 		assertEquals("method",
 				xeval(m, "ruleDeclarations[1].nativeSpec.properties[1].name"));
 		assertEquals("Simple",
 				xeval(m, "ruleDeclarations[1].nativeSpec.properties[1].value"));
 	}
 
 	/**
 	 * 
 	 module SimpleBlocks { metamodel MM3(prop0="value0", prop1="value1")
 	 * replaces OLD0, OLD1; called rule0 ( MM3.t0 par0, MM3.t1 par1, MM3.t2 par2
 	 * ) MM3.r0 { var MM3.l0 lv0; lv0 = par0; } }
 	 * 
 	 * @throws IOException
 	 * @throws RecognitionException
 	 */
 	@Ignore("Metamodel types are to be real and no fake")
 	@Test
 	public void testSimpleBlocks() throws IOException, RecognitionException {
 
 		EObject m = load("SimpleBlocks.mitra");
 		assertNotNull(m);
 
 		assertEquals("SimpleBlocks", xeval(m, "name"));
 
 		assertEquals("rule0", xeval(m, "ruleDeclarations[0].name"));
 		assertEquals("called", xeval(m, "ruleDeclarations[0].exec"));
 		assertEquals(
 				"par0",
 				xeval(m, "ruleDeclarations[0].formalParameters[0].vardecl.name"));
 		assertEquals(
 				"par2",
 				xeval(m, "ruleDeclarations[0].formalParameters[2].vardecl.name"));
 		// assertEquals(false, xeval(m, "ruleDeclarations[0].abstract")); 
 		// rev. 629: new execution modifier: abstract
 		assertNull(xeval(m, "ruleDeclarations[0].nativeSpec"));
 
 		assertEquals(
 				"lv0",
 				xeval(m,
 						"ruleDeclarations[0].body.statements[0].localVariableDeclaration.vardecl.name"));
 		assertEquals(
 				"t0",
 				xeval(m,
 						"ruleDeclarations[0].body.statements[0].localVariableDeclaration.vardecl.type.eClassifier.name"));
 
 		assertEquals(
 				"lv0",
 				xeval(m,
 						"ruleDeclarations[0].body.statements[1].statementExpression.lhs[0].variable.name"));
 		assertEquals(
 				"par0",
 				xeval(m,
 						"ruleDeclarations[0].body.statements[1].statementExpression.expression.variable.name"));
 
 		assertEquals("main", xeval(m, "ruleDeclarations[1].name"));
 		String mainBody = "ruleDeclarations[1].body.";
 		assertEquals(
 				"schema",
 				xeval(m, mainBody
 					+ "statements[0].localVariableDeclaration.vardecl.name"));
 
 	}
 
 	@Ignore("Adaptate test proband.")
 	@Test
 	public void testFeatureAccess() throws IOException, RecognitionException {
 
 		EObject m = load( "FeatureAccess.mitra");
 		assertNotNull(m);
 
 		assertEquals("FeatureAccess", xeval(m, "name"));
 
 		assertEquals("main", xeval(m, "ruleDeclarations[0].name"));
 		assertEquals("entities", xeval(m,
 				"ruleDeclarations[0].body.statements[4]."
 					+ "statementExpression.lhs[0].features[0].name"));
 		assertEquals(1, xeval(m, "ruleDeclarations[0].body.statements[6]."
 			+ "statementExpression.features[1].arguments[0].intValue"));
 
 		// test feature access vs. rule invocation
 		assertEquals("foo", xeval(m, "ruleDeclarations[1].name"));
 		EObject body = (EObject) xeval(m, "ruleDeclarations[1].body");
 		assertEquals(
 				"eClass",
 				xeval(body,
 						"statements[1].statementExpression.features[0].name"));
 		assertEquals(
 				"array",
 				xeval(body,
 						"statements[2].statementExpression.ruleDeclaration.name"));
 	}
 
 	@Ignore("Adaptate test proband.")
 	@Test
 	public void testLiterals() throws IOException, RecognitionException {
 
 		EObject m = load("Literals.mitra");
 		assertNotNull(m);
 
 		if (log.isLoggable(Level.INFO)) {
 			log.info(DynEcoreDump.dumpNotNull(m)); //$NON-NLS-1$
 		}
 
 		assertEquals("Literals", xeval(m, "name"));
 
 		EObject b = (EObject) xeval(m, "ruleDeclarations[0].body");
 		assertNotNull(b);
 
 	}
 
 	@Ignore("Adaptate test proband.")
 	@Test
 	public void testExpressions() throws IOException, RecognitionException {
 
 		EObject m = load( "Expressions.mitra");
 		assertNotNull(m);
 
 		if (log.isLoggable(Level.INFO)) {
 			log.info(DynEcoreDump.dumpNotNull(m)); //$NON-NLS-1$
 		}
 
 		assertEquals("Expressions", xeval(m, "name"));
 
 		assertEquals("prepostfix", xeval(m, "ruleDeclarations[0].name"));
 		EObject b = (EObject) xeval(m, "ruleDeclarations[0].body");
 		assertEquals("schema",
 				xeval(b, "statements[1].statementExpression.variable.name"));
 		assertEquals("inc",
 				xeval(b, "statements[1].statementExpression.prefixOperator"));
 		assertEquals("schema",
 				xeval(b, "statements[2].statementExpression.variable.name"));
 		assertEquals("dec",
 				xeval(b, "statements[2].statementExpression.postfixOperator"));
 
 	}
 
 	@Ignore("Adaptate test proband.")
 	@Test
 	public void testAssignments() throws IOException, RecognitionException {
 
 		EObject m = load( "Assignments.mitra");
 		assertNotNull(m);
 		assertEquals("Assignments", xeval(m, "name"));
 
 		if (log.isLoggable(Level.INFO)) {
 			//			log.info(DynEcoreDump.dumpNotNull(m)); //$NON-NLS-1$
 		}
 
 		assertEquals(8,
 				((List<?>) xeval(m, "ruleDeclarations[0].body.statements"))
 					.size());
 
 		EObject body = (EObject) xeval(m, "ruleDeclarations[0].body");
 		// quick check
 		for (int i = 2; i <= 6; i++) {
 			assertEquals(
 					"Assignment",
 					((EObject) xeval(body, "statements[" + i
 						+ "].statementExpression")).eClass().getName());
 		}
 		// details
 		String toRhs = ".statementExpression.expression.";
 		// [2]
 		assertEquals("Schema",
 				xeval(body, "statements[2]" + toRhs + "type.eClassifier.name"));
 		// [3]
 		assertEquals("entity",
 				xeval(body, "statements[3]" + toRhs + "variable.name"));
 		// [4]
 		assertEquals("Hallo",
 				xeval(body, "statements[4]" + toRhs + "stringValue"));
 		// [5]
 		assertEquals("entity",
 				xeval(body, "statements[5]" + toRhs + "variable.name"));
 		assertEquals("add",
 				xeval(body, "statements[5].statementExpression.operator"));
 		// [6]
 		assertEquals("entity",
 				xeval(body, "statements[6]" + toRhs + "variable.name"));
 		assertEquals("eClass",
 				xeval(body, "statements[6]" + toRhs + "features[0].name"));
 		assertEquals("getName",
 				xeval(body, "statements[6]" + toRhs + "features[1].name"));
 		// [7]
 		assertEquals(
 				"rule",
 				xeval(body, "statements[7]" + toRhs
 					+ "expression.ruleDeclaration.name"));
 		assertEquals("Schema",
 				xeval(body, "statements[7]" + toRhs + "type.eClassifier.name"));
 	}
 
 	@Ignore("Adaptate test proband.")
 	@Test
 	public void testFacultyTest() throws IOException, RecognitionException {
 
 		EObject m = load( "FacultyTest.mitra");
 
 		if (log.isLoggable(Level.INFO)) {
 			log.info(DynEcoreDump.dumpNotNull(m)); //$NON-NLS-1$
 		}
 
 		assertNotNull(m);
 		assertEquals("FacultyTest", xeval(m, "name"));
 
 		EObject body = (EObject) xeval(m, "ruleDeclarations[0].body");
 		assertNotNull(body);
 		// TODO define tests
 
 	}
 
 	@Ignore("Adaptate test proband.")
 	@Test
 	public void testFacultySimpleTest() throws IOException,
 			RecognitionException {
 
 		EObject m = load("FacultySimpleTest.mitra");
 
 		if (log.isLoggable(Level.INFO)) {
 			log.info(DynEcoreDump.dumpNotNull(m)); //$NON-NLS-1$
 		}
 
 		assertNotNull(m);
 		assertEquals("FacultySimpleTest", xeval(m, "name"));
 
 		EObject body = (EObject) xeval(m, "ruleDeclarations[0].body");
 		assertNotNull(body);
 		// TODO define tests
 
 	}
 
 	@Test
 	public void testIncludes() throws IOException {
 		EObject m = load( "ModuleB.mitra");
 
 		assertNotNull(m);
 		assertEquals("ModuleB", xeval(m, "name"));
 
 		//	assertEquals("ModuleA", xeval(m, "imports[0]"));
 
 		// TODO acess to included rules
 
 	}
 
 
 	@Ignore("Adaptate test proband.")
 	@Test
 	public void testSimpleUML2ER() throws IOException, RecognitionException {
 
 		EObject m = load("simpleUML2ER.mitra");
 		assertNotNull(m);
 		// assertEquals("FacultyTest", xeval(m, "name"));
 
 		if (log.isLoggable(Level.INFO)) {
 			log.info(DynEcoreDump.dumpNotNull(m)); //$NON-NLS-1$
 		}
 
 		// package2schema
 		EObject body = (EObject) xeval(m, "ruleDeclarations[1].body");
 		assertNotNull(body);
 		assertEquals(5, ((List) xeval(body, "statements")).size());
 		// check line 33. explicit type cast
 		// class = (SimpleUML.Class) package.ownedElements->at(i);
 		assertEquals("SimpleUML", xeval(body,
 				"statements[4].statement.statements[0]."
 					+ "trueStatement.statements[0].statementExpression."
 					+ "expression.type.metamodelDeclaration.name"));
 		assertEquals("Class", xeval(body,
 				"statements[4].statement.statements[0]."
 					+ "trueStatement.statements[0].statementExpression."
 					+ "expression.type.eClassifier.name"));
 
 		// TODO define tests
 
 	}
 }
