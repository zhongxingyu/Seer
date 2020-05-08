 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.validation;
 
 import org.eclipse.core.runtime.IStatus;
 
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.NonUITestCase;
import org.eclipse.riena.ui.ridgets.validation.IValidationRuleStatus;
import org.eclipse.riena.ui.ridgets.validation.ValidExpression;
import org.eclipse.riena.ui.ridgets.validation.ValidationFailure;
 
 /**
  * 
  */
 @NonUITestCase
 public class ValidExpressionTest extends RienaTestCase {
 
 	public void testInvalidArgs() throws Exception {
 		try {
 			final ValidExpression rule = new ValidExpression(ValidExpression.GERMAN_ZIP);
 			rule.validate(new Object());
 			fail("expected a thrown " + ValidationFailure.class.getName());
 		} catch (ValidationFailure e) {
 			ok("passed test");
 		} catch (RuntimeException e) {
 			fail("expected a thrown " + ValidationFailure.class.getName());
 		}
 
 		try {
 			new ValidExpression(null);
 			fail("Null expression: expected some kind of " + RuntimeException.class.getName());
 		} catch (RuntimeException e) {
 			ok("passed test");
 		}
 
 		try {
 			new ValidExpression("");
 			fail("Empty expression: expected some kind of " + RuntimeException.class.getName());
 		} catch (RuntimeException e) {
 			ok("passed test");
 		}
 	}
 
 	/**
 	 * Checks for validity of german PLZ ( only an example ) *
 	 * 
 	 * @throws Exception
 	 *             Handled by JUnit
 	 */
 	public void testSimpleExpression() throws Exception {
 		final ValidExpression rule = new ValidExpression(ValidExpression.GERMAN_ZIP);
 		assertFalse(rule.validate(null).isOK());
 		assertFalse(rule.validate("").isOK());
 
 		assertTrue(rule.validate("64372").isOK());
 		assertTrue(rule.validate("04372").isOK());
 
 		assertFalse(rule.validate("4372").isOK());
 		assertFalse(rule.validate("123456").isOK());
 		assertFalse(rule.validate("A6038").isOK());
 
 	}
 
 	/**
 	 * Checks for a SWIFT Bank identifier code
 	 * 
 	 * @throws Exception
 	 *             Handled by JUnit
 	 */
 	public void testAdvancedExpression() throws Exception {
 
 		final ValidExpression rule = new ValidExpression(ValidExpression.SWIFT_BIC);
 		assertFalse(rule.validate(null).isOK());
 		assertFalse(rule.validate("").isOK());
 
 		assertTrue("Natwest Offshore Bank Guernsey BIC", rule.validate("RBOSGGSX").isOK());
 		assertTrue("Raiffeisenbank Kitzbuehel BIC", rule.validate("RZTIAT22263").isOK());
 		assertTrue("Banque et Caisse d'Epargne de l'Etat BIC", rule.validate("BCEELULL").isOK());
 		assertTrue("Deutsche Bundesbank Frankfurt am Main BIC", rule.validate("MARKDEFF").isOK());
 		assertTrue("Deutsche Bundesbank Frankfurt am Main BIC (optional postfix)", rule.validate("MARKDEFFXXX").isOK());
 		assertTrue("Schweizer Bank UBS AG BIC", rule.validate("UBSWCHZH80A").isOK());
 		assertTrue("Rhn-Rennsteig-Sparkasse BIC", rule.validate("HELADEF1RRS").isOK());
 		assertTrue("Rhn-Rennsteig-Sparkasse BIC, transactions from USA", rule.validate("HELADEFF").isOK());
 
 		assertFalse("Alnum in first 6 characters", rule.validate("R2OSGGSX").isOK());
 		assertFalse("postfix too long", rule.validate("MARKDEFFXXXX").isOK());
 
 	}
 
 	/**
 	 * Checks for case insensititvity-feature *
 	 * 
 	 * @throws Exception
 	 *             Handled by JUnit
 	 */
 	public void testCaseInsensititvityOption() throws Exception {
 
 		final ValidExpression sensitiveRule = new ValidExpression("^test$");
 		assertFalse(sensitiveRule.validate(null).isOK());
 		assertFalse(sensitiveRule.validate("").isOK());
 
 		assertTrue(sensitiveRule.validate("test").isOK());
 
 		assertFalse(sensitiveRule.validate("TEST").isOK());
 		assertFalse(sensitiveRule.validate("Test").isOK());
 		assertFalse(sensitiveRule.validate("1Test").isOK());
 		assertFalse(sensitiveRule.validate("Test1").isOK());
 		assertFalse(sensitiveRule.validate("Te1st").isOK());
 
 		final ValidExpression insensitiveRule = new ValidExpression("^test$", ValidExpression.Option.CASE_INSENSITIVE);
 		assertFalse(insensitiveRule.validate(null).isOK());
 		assertFalse(insensitiveRule.validate("").isOK());
 
 		assertTrue(insensitiveRule.validate("test").isOK());
 		assertTrue(insensitiveRule.validate("TEST").isOK());
 		assertTrue(insensitiveRule.validate("Test").isOK());
 
 		assertFalse(insensitiveRule.validate("1Test").isOK());
 		assertFalse(insensitiveRule.validate("Test1").isOK());
 		assertFalse(insensitiveRule.validate("Te1st").isOK());
 	}
 
 	public void testDoesNotBlockInputWhenFailing() {
 
 		final ValidExpression rule = new ValidExpression(ValidExpression.GERMAN_ZIP);
 		IStatus result = rule.validate("XX123");
 
 		assertFalse(result.isOK());
		assertEquals(IValidationRuleStatus.ERROR_ALLOW_WITH_MESSAGE, result.getCode());
 	}
 
 	/**
 	 * Tests the method {@code setInitializationData}.
 	 * 
 	 * @throws Exception
 	 *             - Handled by JUnit.
 	 */
 	public void testSetInitializationData() throws Exception {
 
 		ValidExpression validator = new ValidExpression();
 		validator.setInitializationData(null, null, "^test$");
 		assertTrue(validator.validate("test").isOK());
 		assertFalse(validator.validate("TEST").isOK());
 
 		validator = new ValidExpression();
 		validator.setInitializationData(null, null, "^test$,DUMM*");
 		assertTrue(validator.validate("test").isOK());
 		assertFalse(validator.validate("TEST").isOK());
 
 	}
 
 }
