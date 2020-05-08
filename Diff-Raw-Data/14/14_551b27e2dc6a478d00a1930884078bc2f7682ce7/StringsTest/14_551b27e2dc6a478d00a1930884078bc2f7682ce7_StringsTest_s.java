 package org.softwareFm.utilities.strings;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.softwareFm.utilities.functions.Functions;
 
 public class StringsTest extends TestCase {
 
 	public void testCamelCaseToPretty() {
 		assertEquals("G", Strings.camelCaseToPretty("G"));
 		assertEquals("", Strings.camelCaseToPretty(""));
 		assertEquals("G", Strings.camelCaseToPretty("g"));
 		assertEquals("G", Strings.camelCaseToPretty(" g"));
 		assertEquals("G", Strings.camelCaseToPretty(" G"));
 
 		assertEquals("Group", Strings.camelCaseToPretty("group"));
 		assertEquals("Group Id", Strings.camelCaseToPretty("groupId"));
 		assertEquals("Group Id Again", Strings.camelCaseToPretty("groupIdAgain"));
 		assertEquals("Group Id Again", Strings.camelCaseToPretty("GroupIdAgain"));
 		assertEquals("Group Id Again And Again", Strings.camelCaseToPretty("GroupIdAgain And Again"));
 		assertEquals("Group Id Again And Again", Strings.camelCaseToPretty("GroupIdAgain     And   Again"));
 	}
 
 	public void testVersions() {
 		checkVersions("\\a\\b\\c\\spring-code-1.0.0.jar", "spring-code", "1.0.0");
 		checkVersions("a/b/c/spring-code-1.0.0.jar", "spring-code", "1.0.0");
 		checkVersions("spring-code-1.0.0.jar", "spring-code", "1.0.0");
 		checkVersions("spring-code-1.A.0.jar", "n/a", "n/a");
 		checkVersions("spring-code-1.A.0.jar", "n/a", "n/a");
 		checkVersions(null, "n/a", "n/a");
 	}
 
 	private void checkVersions(String raw, String prefix, String version) {
 		File file = raw == null ? null : new File(raw);
 		assertEquals(version, Strings.versionPartOf(file, "n/a"));
 		assertEquals(prefix, Strings.withoutVersion(file, "n/a"));
 	}
 
 	public void testStringToUrlSegment() {
 		checkStringToUrlSegment("", "");
 		checkStringToUrlSegment("abc", "abc");
 		checkStringToUrlSegment("ab.c", "a/b.c");
 		checkStringToUrlSegment("aB.C", " a / b . c ");
 		checkStringToUrlSegment("someNewValue", " Some New Value ");
 		checkStringToUrlSegment("s.omeNewValue", " S.ome New Value ");
 		checkStringToUrlSegment("aBC", "a!\"$%^&*\'# b c");
 	}
 
 	public void testLowerAndUpperCaseFirstCharacter() {
 		checkLowerAndUpperCaseFirstCharacter("Abc", "abc", "Abc");
 		checkLowerAndUpperCaseFirstCharacter("Abc", "abc", "abc");
 		checkLowerAndUpperCaseFirstCharacter("ABc", "aBc", "aBc");
 		checkLowerAndUpperCaseFirstCharacter("A", "a", "a");
 		checkLowerAndUpperCaseFirstCharacter("", "", "");
 		checkLowerAndUpperCaseFirstCharacter(null, null, null);
 	}
 
 	private void checkLowerAndUpperCaseFirstCharacter(String expectedUpper, String expectedLower, String raw) {
 		assertEquals(expectedLower, Strings.lowerCaseFirstCharacter(raw));
 		assertEquals(expectedUpper, Strings.upperCaseFirstCharacter(raw));
 		assertEquals(expectedUpper, Functions.call(Strings.upperCaseFirstCharacterFn(), raw));
 
 	}
 
 	private void checkStringToUrlSegment(String expected, String raw) {
 		assertEquals(expected, Strings.stringToUrlSegment(raw));
 		assertEquals(expected, Functions.call(Strings.stringToUrlSegmentFn(), raw));
 	}
 
 	public void testVersionCompare() {
 		assertEquals(0, Strings.compareVersionNumbers("1.2.1", "1.2.1"));
 		assertEquals(1, Strings.compareVersionNumbers("1.2.2", "1.2.1"));
 		assertEquals(1, Strings.compareVersionNumbers("1.3.1", "1.2.1"));
 		assertEquals(1, Strings.compareVersionNumbers("2.1.1", "1.2.1"));
 
 		assertEquals(0, Strings.compareVersionNumbers("1.2.11", "1.2.11"));
 		assertEquals(-9, Strings.compareVersionNumbers("1.2.2", "1.2.11"));
 		assertEquals(-111, Strings.compareVersionNumbers("1.2.11", "1.113.1"));
 
 		assertEquals(0, Strings.compareVersionNumbers("1-2-11", "1.2.11"));
 		assertEquals(-9, Strings.compareVersionNumbers("1-2-2", "1.2.11"));
 		assertEquals(-111, Strings.compareVersionNumbers("1-2-11", "1.113.1"));
 
 		assertEquals(0, Strings.compareVersionNumbers("1-2-11", "1-2-11"));
 		assertEquals(-9, Strings.compareVersionNumbers("1-2-2", "1-2-11"));
 		assertEquals(-111, Strings.compareVersionNumbers("1-2-11", "1-113-1"));
 	}
 
 	public void testSplit() {
 		assertEquals(new PreAndPost("a", "b"), Strings.split("a.b", '.'));
 		assertEquals(new PreAndPost("a", "b"), Strings.split("a$b", '$'));
 		assertEquals(new PreAndPost("ab", null), Strings.split("ab", '$'));
 		assertEquals(new PreAndPost("ab", ""), Strings.split("ab$", '$'));
 		assertEquals(new PreAndPost("", null), Strings.split("", '$'));
 		assertEquals(new PreAndPost("", ""), Strings.split(".", '.'));
 	}
 
 	public void testSqlEscape() {
 		assertEquals("abc", Strings.sqlEscape("abc"));
 		assertEquals("a''bc", Strings.sqlEscape("a'bc"));
 		assertEquals("a''''bc", Strings.sqlEscape("a''bc"));
 		assertEquals("a''bc", Strings.sqlEscape("a\'bc"));
 		assertEquals("a\\''bc", Strings.sqlEscape("a\\'bc"));
 	}
 
 	public void testOneLineLowWhiteSpace() {
 		checkOneLineLowWhiteSpace("a b c", "   \n   a  \n\nb\n  c");
 	}
 
 	private void checkOneLineLowWhiteSpace(String expected, String input) {
 		assertEquals(expected, Strings.oneLineLowWhiteSpace(input));
 
 	}
 
 	public void testKeepOnly() {
 		assertEquals("1234", Strings.onlyKeep("raw12and3,4", "4231"));
 	}
 
 	public void testLastSegmentFn() throws Exception {
 		assertEquals("whole", Strings.lastSegmentFn("/").apply("whole"));
 		assertEquals("last", Strings.lastSegmentFn("/").apply("first/second/last"));
 	}
 
 }
