 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.parser.modelingunit.test;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitFormatter;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitParser;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitParserImpl;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.serializer.ModelingUnitSerializer;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.test.utils.FileToStringConverter;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.test.utils.ModelingUnitParsingTestConfigurator;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests the serialization of Modeling Units.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class TestModelingUnitSerialization {
 	/**
 	 * Parser used to parse modeling units.
 	 */
 	private static ModelingUnitParser modelingUnitParser;
 
 	private static ModelingUnitSerializer modelingUnitSerializer;
 
 	/**
 	 * Initialisation of the Test environnement : launching the StandaloneParsingManager.
 	 */
 	@Before
 	public void setUp() {
 		modelingUnitParser = new ModelingUnitParserImpl();
 		modelingUnitSerializer = ModelingUnitSerializer.getSerializer();
 	}
 
 	static void parseAndCompareSerializationToExpected(String fileToTest, boolean supposedToWork) {
 		// Step 1 : we parse the model and obtain the AST
 		ModelingUnit parsedAST;
 		try {
 			parsedAST = (ModelingUnit)ModelingUnitParsingTestConfigurator.parseFile(modelingUnitParser,
 					fileToTest);
 
 			// xtextRes.getContents().add(parsedAST);
 
 			// Step 2 : we serialize this AST
 			String actual = modelingUnitSerializer.serialize(parsedAST);
 			actual = ModelingUnitFormatter.indentAccordingToBrackets(modelingUnitSerializer, actual);
 
 			// Step 3 : we get the file as a String
 			File expectedFile = new File(ModelingUnitParsingTestConfigurator.getDatatestsFolder()
 					+ fileToTest + ModelingUnitParsingTestConfigurator.getFileExtensions());
 			String expected = FileToStringConverter.getFileAsString(expectedFile);
 
 			// Step 4 : we compare these to String
 			Assert.assertEquals(expected, actual);
 			Assert.assertEquals(supposedToWork, true);
 		} catch (ParseException e) {
 			System.err.println("----------------------------------------");
 			System.err.println("Parsing errors for file : " + fileToTest);
 			System.err.println(e.getMessage());
 			Assert.assertEquals(supposedToWork, false);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.assertEquals(supposedToWork, false);
 		}
 	}
 
 	@Test
 	public void testSimpleSerialization() {
 		parseAndCompareSerializationToExpected("simpleTests/SimpleModelingUnit", true);
 	}
 
 	@Test
 	public void testSpecialvalues() {
		// parseAndCompareSerializationToExpected("simpleTests/SpecialValues", true);
 	}
 
 	@Test
 	public void testCompleteSerialization() {
 		parseAndCompareSerializationToExpected("simpleTests/CompleteModelingUnit", true);
 		parseAndCompareSerializationToExpected("simpleTests/CompleteModelingUnit2", true);
		// parseAndCompareSerializationToExpected("simpleTests/CompleteModelingUnit3", true);
 	}
 
 	@Test
 	public void testLabelsSerialization() {
 		parseAndCompareSerializationToExpected("simpleTests/AllKindOfLabels", true);
 	}
 
 	@Test
 	public void testResourceDeclarationSerialization() {
 		parseAndCompareSerializationToExpected("resourcesRelatedTest/resourceDeclaration", true);
 	}
 
 	@Test
 	public void testResourceReferenceSerialization() {
 		parseAndCompareSerializationToExpected("resourcesRelatedTest/resourceReference", true);
 	}
 
 	@Test
 	public void testQualifiedNamesSerialization() {
 		parseAndCompareSerializationToExpected("qualifiedNames/qualifiedName", true);
 		parseAndCompareSerializationToExpected("qualifiedNames/qualifiedNameNewInstance", true);
 	}
 }
