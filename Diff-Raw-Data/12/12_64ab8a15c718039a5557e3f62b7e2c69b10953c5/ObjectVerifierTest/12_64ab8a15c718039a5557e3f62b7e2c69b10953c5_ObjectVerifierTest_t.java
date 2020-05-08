 package com.github.ver4j;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.github.ver4j.arg.exception.ArgumentTypeVerificationException;
 import com.github.ver4j.arg.exception.ArgumentVerificationException;
 import com.github.ver4j.arg.exception.NullPointerArgumentVerificationException;
 
 public class ObjectVerifierTest {
 	private final ObjectVerifier verifier = new ObjectVerifier(null,
 			new ExceptionMessageInfo("Test"),
 			ExceptionTypeInfo.of(ArgumentVerificationException.class),
 			ExceptionTypeInfo.of(ArgumentTypeVerificationException.class),
 			ExceptionTypeInfo
 					.of(NullPointerArgumentVerificationException.class));
 
 	// isTrue*
 
 	@Test
 	public void testIsTrueWithTrue() {
 		verifier.isTrue(true);
 		verifier.isTrue(true, "");
 		verifier.isTrue(true, "%s", "1");
 		verifier.isTrueCm(true, "", null);
 	}
 
 	@Test
 	public void testIsTrueWithFalseDisabled() {
 		verifier.setDisabled(true);
 		verifier.isTrue(false);
 		verifier.isTrue(false, "");
 		verifier.isTrue(false, "%s", "1");
		verifier.isTrueCm(false, "", null);
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsTrueWithFalse1() {
 		verifier.isTrue(false);
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsTrueWithFalse2() {
 		verifier.isTrue(false, "");
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsTrueWithFalse3() {
 		verifier.isTrue(false, "%s", "1");
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsTrueCmWithFalse() {
 		verifier.isTrueCm(false, "", null);
 	}
 
 	// isFalse*
 
 	@Test
 	public void testIsFalseWithFalse() {
 		verifier.isFalse(false);
 		verifier.isFalse(false, "");
 		verifier.isFalse(false, "%s", "1");
 		verifier.isFalseCm(false, "", null);
 	}
 
 	@Test
 	public void testIsFalseWithTrueDisabled() {
 		verifier.setDisabled(true);
 		verifier.isFalse(true);
 		verifier.isFalse(true, "");
 		verifier.isFalse(true, "%s", "1");
 		verifier.isFalseCm(true, "", null);
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsFalseWithTrue1() {
 		verifier.isFalse(true);
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsFalseWithTrue2() {
 		verifier.isFalse(true, "");
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsFalseWithTrue3() {
 		verifier.isFalse(true, "%s", "1");
 	}
 
 	@Test(expected = ArgumentVerificationException.class)
 	public void testIsFalseCmWithTrue() {
 		verifier.isFalseCm(true, "", null);
 	}
 
 	// isAssignableFrom*
 
 	@Test
 	public void testIsAssignableFromWithTrue() {
 		Assert.assertEquals(String.class,
 				verifier.assignableFrom(CharSequence.class, String.class));
 		Assert.assertEquals(String.class,
 				verifier.assignableFrom(CharSequence.class, String.class, ""));
 		Assert.assertEquals(String.class, verifier.assignableFrom(
 				CharSequence.class, String.class, "%s", "1"));
 		Assert.assertEquals(String.class, verifier.assignableFromCm(
 				CharSequence.class, String.class, "", null));
 	}
 
 	@Test
 	public void testIsAssignableFromWithFalseDisabled() {
 		verifier.setDisabled(true);
 		Assert.assertEquals(Integer.class,
 				verifier.assignableFrom(CharSequence.class, Integer.class));
 		Assert.assertEquals(Integer.class,
 				verifier.assignableFrom(CharSequence.class, Integer.class, ""));
 		Assert.assertEquals(Integer.class, verifier.assignableFrom(
 				CharSequence.class, Integer.class, "%s", "1"));
 		Assert.assertEquals(Integer.class, verifier.assignableFromCm(
 				CharSequence.class, Integer.class, "", null));
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsAssignableFromWithFalse1() {
 		verifier.assignableFrom(CharSequence.class, Integer.class);
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsAssignableFromWithFalse2() {
 		verifier.assignableFrom(CharSequence.class, Integer.class, "");
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsAssignableFromWithFalse3() {
 		verifier.assignableFrom(CharSequence.class, Integer.class, "%s", "1");
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsAssignableFromCmWithFalse() {
 		verifier.assignableFromCm(CharSequence.class, Integer.class, "", null);
 	}
 
 	// isInstanceOf*
 
 	@Test
 	public void testIsInstanceOfWithTrue() {
 		Assert.assertEquals("", verifier.instanceOf("", String.class));
 		Assert.assertEquals("", verifier.instanceOf("", String.class, ""));
 		Assert.assertEquals("",
 				verifier.instanceOf("", String.class, "%s", "1"));
 		Assert.assertEquals("",
 				verifier.instanceOfCm("", String.class, "", null));
 	}
 
 	@Test(expected = ClassCastException.class)
 	public void testIsInstanceOfWithFalseDisabled() {
 		verifier.setDisabled(true);
 		Assert.assertEquals(null, verifier.instanceOf(1, String.class));
 		Assert.assertEquals(null, verifier.instanceOf(1, String.class, ""));
 		Assert.assertEquals(null,
 				verifier.instanceOf(1, String.class, "%s", "1"));
 		Assert.assertEquals(null,
 				verifier.instanceOfCm(1, String.class, "", null));
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsInstanceOfWithFalse1() {
 		verifier.instanceOf(1, String.class, 1);
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsInstanceOfWithFalse2() {
 		verifier.instanceOf(1, String.class, "");
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsInstanceOfWithFalse3() {
 		verifier.instanceOf(1, String.class, "%s", "1");
 	}
 
 	@Test(expected = ArgumentTypeVerificationException.class)
 	public void testIsInstanceOfCmWithFalse() {
 		verifier.instanceOfCm(1, String.class, "", null);
 	}
 
 	// notNull*
 
 	@Test
 	public void testNotNullWithNotNull() {
 		Assert.assertEquals("", verifier.notNull(""));
 		Assert.assertEquals("", verifier.notNull("", ""));
 		Assert.assertEquals("", verifier.notNull("", "%s", "1"));
 		Assert.assertEquals("", verifier.notNullCm("", "", null));
 	}
 
 	@Test
 	public void testNotNullWithNullDisabled() {
 		verifier.setDisabled(true);
 		Assert.assertEquals(null, verifier.notNull(null));
 		Assert.assertEquals(null, verifier.notNull(null, ""));
 		Assert.assertEquals(null, verifier.notNull(null, "%s", "1"));
 		Assert.assertEquals(null, verifier.notNullCm(null, "", null));
 	}
 
 	@Test(expected = NullPointerArgumentVerificationException.class)
 	public void testNotNullWithNull1() {
 		verifier.notNull(null);
 	}
 
 	@Test(expected = NullPointerArgumentVerificationException.class)
 	public void testNotNullWithNull2() {
 		verifier.notNull(null, "");
 	}
 
 	@Test(expected = NullPointerArgumentVerificationException.class)
 	public void testNotNullWithNull3() {
 		verifier.notNull(null, "%s", "1");
 	}
 
 	@Test(expected = NullPointerArgumentVerificationException.class)
 	public void testNotNullCmWithNull() {
 		verifier.notNullCm(null, "", null);
 	}
 }
