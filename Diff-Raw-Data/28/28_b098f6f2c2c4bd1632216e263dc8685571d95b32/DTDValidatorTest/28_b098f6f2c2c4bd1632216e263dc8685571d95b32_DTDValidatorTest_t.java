 /*******************************************************************************
  * Copyright (c) 2004, 2008 John Krasnay and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     John Krasnay - initial API and implementation
  *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
  *     Florian Thienel - bug 299999 - completed implementation of validation
  *******************************************************************************/
 package org.eclipse.wst.xml.vex.core.internal.dom;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.wst.xml.vex.core.internal.provisional.dom.I.VEXDocument;
 import org.eclipse.wst.xml.vex.core.internal.provisional.dom.I.Validator;
 import org.eclipse.wst.xml.vex.core.internal.validator.AttributeDefinition;
 import org.eclipse.wst.xml.vex.core.internal.validator.WTPVEXValidator;
 
 @SuppressWarnings("restriction")
 public class DTDValidatorTest extends TestCase {
 
 	private Validator validator = null;
 
 	@Override
 	protected void setUp() {
 		try {
 			final URL url = DTDValidatorTest.class.getResource("test1.dtd");
 			validator = WTPVEXValidator.create(url);
 		} catch (final Exception ex) {
 			fail("Failed to load test1.dtd");
 		}
 	}
 
 	public void testAttributeDefinition() throws Exception {
 		final AttributeDefinition.Type adType = validator.getAttributeDefinitions("section").get(0).getType();
 
 		// Test serialization while we're at it
 		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		final ObjectOutputStream oos = new ObjectOutputStream(baos);
 		oos.writeObject(validator);
 
 		final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
 		final ObjectInputStream ois = new ObjectInputStream(bais);
 		validator = (Validator) ois.readObject();
 
 		final AttributeDefinition.Type adType2 = validator.getAttributeDefinitions("section").get(0).getType();
 
 		assertSame(adType, adType2);
 
 	}
 
 	// public void testEmptyDTD() throws Exception {
 	// VEXDocument doc;
 	// Set expected;
 	//
 	// doc = new Document(new RootElement("empty"));
 	// doc.setValidator(validator);
 	// assertEquals(Collections.EMPTY_SET, getValidItemsAt(doc, 1));
 	// }
 
 	// public void testAnyDTD() throws Exception {
 	// VEXDocument doc;
 	// Set expected;
 	//
 	// doc = new Document(new RootElement("any"));
 	// doc.setValidator(validator);
 	// Set anySet = new HashSet();
 	// anySet.add(Validator.PCDATA);
 	// anySet.add("any");
 	// anySet.add("empty");
 	// anySet.add("section");
 	// anySet.add("title");
 	// anySet.add("para");
 	// anySet.add("emphasis");
 	// assertEquals(anySet, getValidItemsAt(doc, 1));
 	//		
 	// }
 
 	public void testSectionElement() {
 		// <section> <title> a b </title> <para> </para> </section>
 		// 1 2 3 4 5 6 7
 		final VEXDocument doc = new Document(new RootElement("section"));
 		doc.setValidator(validator);
 		doc.insertElement(1, new Element("title"));
 		doc.insertText(2, "ab");
 		doc.insertElement(5, new Element("para"));
 
 		assertValidItemsAt(doc, 1, "title", "para");
 		assertValidItemsAt(doc, 2);
 		assertValidItemsAt(doc, 3);
 		assertValidItemsAt(doc, 4);
 		assertValidItemsAt(doc, 5, "title", "para");
 		assertValidItemsAt(doc, 6, "emphasis");
 		assertValidItemsAt(doc, 7, "title", "para");
 	}
 
 	public void testOneKindOfChild() {
 		final VEXDocument doc = new Document(new RootElement("one-kind-of-child"));
 		doc.setValidator(validator);
 		assertValidItemsAt(doc, 1, "section");
 	}
 
 	private static void assertValidItemsAt(final VEXDocument doc, final int offset, final String... expectedItems) {
 		final Set<String> expected = new HashSet<String>(expectedItems.length);
 		for (final String expectedItem : expectedItems)
 			expected.add(expectedItem);
 
 		final String elementName = doc.getElementAt(offset).getName();
 		final Set<String> validItems = doc.getValidator().getValidItems(elementName);
 		assertEquals(expected, validItems);
 	}
 
 	public void testSequences() {
 		assertFullyValidSequence("title", "#PCDATA");
 		assertFullyValidSequence("para", "#PCDATA");
 
 		assertInvalidSequence("empty", "#PCDATA");
 
 		assertFullyValidSequence("index", "para");
 
 		assertPartiallyValidSequence("section", "title"); // partially valid, para is still missing
 		assertFullyValidSequence("section", "title", "para");
 		assertFullyValidSequence("section", "para");
 		assertFullyValidSequence("section", "para", "para");
 		assertInvalidSequence("section", "para", "title");
 		assertInvalidSequence("section", "title", "title");
 		assertInvalidSequence("section", "title", "title", "para");
 		assertInvalidSequence("section", "title", "#PCDATA");
 
 		assertFullyValidSequence("document", "preface", "section", "index");
 		assertFullyValidSequence("document", "title", "preface", "section", "index");
		assertFullyValidSequence("document", "title", "preface", "section", "section", "section", "index");
 		assertPartiallyValidSequence("document", "title", "preface");
 		assertPartiallyValidSequence("document", "preface", "section");
		assertPartiallyValidSequence("document", "preface", "section", "section");
 
 		assertInvalidSequence("document", "title", "index");
		assertInvalidSequence("document", "title", "preface", "index");
		assertInvalidSequence("document", "preface", "index");
 	}
 
 	private void assertFullyValidSequence(final String element, final String... sequence) {

		// fully includes partially
 		assertValidSequence(true, element, true, true, sequence);

 	}
 
 	private void assertPartiallyValidSequence(final String element, final String... sequence) {

		// as partial sequence valid...
 		assertValidSequence(true, element, false, true, sequence);

		// ... but as full sequence invalid
		assertValidSequence(false, element, true, false, sequence);
 	}
 
 	private void assertInvalidSequence(final String element, final String... sequence) {

		// partially _and_ fully
 		assertValidSequence(false, element, true, true, sequence);

 	}
 
 	private void assertValidSequence(final boolean expected, final String element, final boolean validateFully, final boolean validatePartially,
 			final String... sequence) {
 		for (int i = 0; i < sequence.length; i++) {
 			final EList<String> prefix = createPrefix(i, sequence);
 
 			final EList<String> toInsert = new BasicEList<String>(1);
 			toInsert.add(sequence[i]);
 
 			final EList<String> suffix = createSuffix(i, sequence);
 
 			if (validateFully)
 				assertEquals(expected, validator.isValidSequence(element, prefix, toInsert, suffix, false));
 			if (validatePartially)
 				assertEquals(expected, validator.isValidSequence(element, prefix, toInsert, suffix, true));
 		}
 	}
 
 	private static EList<String> createPrefix(final int index, final String... sequence) {
 		final EList<String> prefix = new BasicEList<String>();
 		for (int i = 0; i < index; i++)
 			prefix.add(sequence[i]);
 		return prefix;
 	}
 
 	private static EList<String> createSuffix(final int index, final String... sequence) {
 		final EList<String> suffix = new BasicEList<String>();
 		for (int i = index + 1; i < sequence.length; i++)
 			suffix.add(sequence[i]);
 		return suffix;
 	}
 }
