 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.validation.tests;
 
 import junit.framework.TestCase;
 
import org.eclipse.core.runtime.IStatus;
import org.eclipse.riena.ui.ridgets.validation.IValidationRuleStatus;
 import org.eclipse.riena.ui.ridgets.validation.ValidEmailAddress;
 import org.eclipse.riena.ui.ridgets.validation.ValidationFailure;
 
 /**
  * Tests for the RequiredField rule.
  * 
  * @author Wanja Gayk
  */
 public class ValidEmailAddressTest extends TestCase {
 
 	/**
 	 * @throws Exception
 	 *             Handled by JUnit.
 	 */
 	public void testValidEmailAddress() throws Exception {
 		final ValidEmailAddress rule = new ValidEmailAddress();
 
 		// -- valid
 		assertTrue(rule.validate(null).isOK());
 		assertTrue(rule.validate("").isOK());
 		assertTrue(rule.validate("user@a.b.example").isOK());
 		assertTrue(rule.validate("user@domain.de").isOK());
 		assertTrue(rule.validate("user@xy.example.de").isOK());
 		assertTrue(rule.validate("user@compeople.com").isOK());
 		assertTrue(rule.validate("user@compeople.co.uk").isOK());
 		assertTrue(rule.validate("user@[192.168.2.100]").isOK());
 		assertTrue(rule.validate("user@#1234567890.example").isOK());
 		assertTrue(rule.validate("ha@domain.example").isOK());
 		// quoted string local part
 		assertTrue(rule.validate("\"basti\"@domain.example").isOK());
 		// quoted string local part with escaped character inside
 		assertTrue(rule.validate("\"ba\\,sti\"@domain.example").isOK());
 		// dot string local part
 		assertTrue(rule.validate("user.from.hell@domain.example").isOK());
 
 		// --- invalid
 
 		// local part empty
 		assertFalse(rule.validate("@domain.example").isOK());
 		// domain part with illegal character
 		assertFalse(rule.validate("user@dom\"ain.example").isOK());
 		// domain part with illegal character
 		assertFalse(rule.validate("user@dom ain.example").isOK());
 		// domain part with illegal character
 		assertFalse(rule.validate("user@dom,ain.example").isOK());
 		// local part with illegal character
 		assertFalse(rule.validate("u\nser@domain.example").isOK());
 		// local part with illegal character
 		assertFalse(rule.validate("u\nser@domain.example").isOK());
 		// local part with illegal character
 		assertFalse(rule.validate("u\"nser@domain.example").isOK());
 		// local part with illegal character
 		assertFalse(rule.validate("u;ser@domain.example").isOK());
 		// local part with illegal character
 		assertFalse(rule.validate("us er@domain.example").isOK());
 		// local part without domain
 		assertFalse(rule.validate("user@").isOK());
 		// illegal IP address
 		assertFalse(rule.validate("user@[xxx.xxx.xxx]").isOK());
 		// IP address without [], or domain element not starting with alphabetic
 		// letter
 		assertFalse(rule.validate("user@192.168.2.100").isOK());
 		// number without #, or domain element not starting with alphabetic
 		// letter
 		assertFalse(rule.validate("user@1234567").isOK());
 		// number with illegal character inside
 		assertFalse(rule.validate("user@#1a2346").isOK());
 
 	}
 
 	public void testException() throws Exception {
 		try {
 			new ValidEmailAddress().validate(new Object());
 			fail("expected a thrown ValidationFailure");
 		} catch (final ValidationFailure e) {
 			// passed
 		} catch (final RuntimeException e) {
 			fail("expected a " + ValidationFailure.class.getName());
 		}
 	}
 
 	public void testValidEmailAddressSpecial() throws Exception {
 
 		final ValidEmailAddress rule = new ValidEmailAddress();
 
 		// Mail to recipient in local domain (no @domain given)
 		// Illegal according to RFC 821, but accepted by some MTAs and MUAs:
 		assertFalse(rule.validate("user").isOK());
 
 		// Tthe following email addresses are uncommon but valid according
 		// to RFC 821.
 		// Still the rule won't validate them:
 
 		// single local domain with all digits name:
 		// assertTrue(rule.validate("user@#1234567890").isOK());
 
 		// mixed segments:
 		// assertTrue(rule.validate("user@[192.168.2.100].de").isOK());
		//assertTrue(rule.validate("user@#1234567890.[127.0.0.1].example").isOK(
		// ));
 
 		// local part with escaped character:
 		// assertTrue(rule.validate("us\\,er@domain.example").isOK());
 
 		// local part with escaped character:
 		// assertTrue(rule.validate("us\\\"er@domain.example").isOK());
 
 		// quoted string local part with escaped quote inside:
 		// assertTrue(rule.validate("\"basti\\\"\"@domain.example").isOK());
 
 		// org.apache.commons.validator.EmailValidator fails this one with an
 		// ArrayIndexOutOfBoundsException, because there are more than 10
 		// Segments in the domain part:
 		//
 		// assertTrue(rule
 		// .validate(
		// "user.who.has.an@extreme.unbelievalble.long.strange.email.address.of.doom.withLotsOf.Strange.UpperCaseletters.co.uk.example"
		// )
 		// .isOK());
	}

	public void testDoesNotBlockInputWhenFailing() {

		final ValidEmailAddress rule = new ValidEmailAddress();
		IStatus result = rule.validate("invalid");
 
		assertFalse(result.isOK());
		assertEquals(IValidationRuleStatus.ERROR_ALLOW_WITH_MESSAGE, result.getCode());
 	}
 
 }
