 package maven;
 
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mrumpf
  *
  */
 public class VersionTest {
 
     @Test
     public void testEmptyVersion() {
         try {
             new Version("");
         } catch (IllegalArgumentException ex) {
             // expected
         }
     }
 
     @Test
     public void testBlankVersion() {
         try {
             new Version("   ");
         } catch (IllegalArgumentException ex) {
             // expected
         }
     }
 
     @Test
     public void testNullVersion() {
         try {
             new Version(null);
         } catch (IllegalArgumentException ex) {
             // expected
         }
     }
 
     @Test
     public void testMajorAsLetter() {
         Version v = new Version("a");
         checkVersion(v, "0", "0", "0", "0", "a");
     }
 
     @Test
     public void testLongVersion() {
         Version v = new Version("1.2.3.4.5.6.7.8.9.0");
         checkVersion(v, "1", "2", "3", "4");
     }
 
     @Test
     public void testMajorOnly() {
         Version v = new Version("1");
         checkVersion(v, "1", "0", "0", "0");
     }
 
     @Test
     public void testMinorAsLetter() {
         Version v = new Version("0.b");
         checkVersion(v, "0", "0", "0", "0", "b");
     }
 
     @Test
     public void testMinorOnly() {
         Version v = new Version("1.2");
         checkVersion(v, "1", "2", "0", "0");
     }
 
     @Test
     public void testBugfixAsLetter() {
         Version v = new Version("0.1.c");
         checkVersion(v, "0", "1", "0", "0", "c");
     }
 
     @Test
     public void testBugfixOnly() {
         Version v = new Version("1.2.3");
         checkVersion(v, "1", "2", "3", "0");
     }
 
     @Test
     public void testHotfixAsLetter() {
         Version v = new Version("0.1.2.d");
         checkVersion(v, "0", "1", "2", "0", "d");
     }
 
     @Test
     public void testHotfixOnly() {
         Version v = new Version("1.2.3.4");
         checkVersion(v, "1", "2", "3", "4");
     }
 
     @Test
     public void testMajorWithBlanks() {
         Version v = new Version("  1  ");
         checkVersion(v, "1", "0", "0", "0");
     }
 
     @Test
     public void testTrailingDot() {
         Version v = new Version("1.2.3.");
         checkVersion(v, "1", "2", "3", "0", null);
     }
 
     @Test
     public void testBlanksInside() {
         Version v = new Version(" 1. 2. 3. 4#qualifier");
         checkVersion(v, "1", "2", "3", "4", "qualifier");
     }
 
     @Test
     public void testQualifierDash() {
         Version v = new Version("1.2.3.4-qualifier");
         checkVersion(v, "1", "2", "3", "4", "qualifier");
     }
     @Test
     public void testQualifierDashNumeric() {
         Version v = new Version("1.2.3-4");
         checkVersion(v, "1", "2", "3","0", "4");
     }
 
     @Test
     public void testQualifierHash() {
         Version v = new Version("1.2.3.4#qualifier");
         checkVersion(v, "1", "2", "3", "4", "qualifier");
     }
 
     @Test
     public void testQualifierAmpersand() {
         Version v = new Version("1.2.3.4&qualifier");
         checkVersion(v, "1", "2", "3", "4", "qualifier");
     }
 
     @Test
     public void testQualifierWithoutSeparator() {
         Version v = new Version("1.2.3.4qualifier");
         checkVersion(v, "1", "2", "3", "4", "ualifier");
     }
 
     @Test
     public void testDotQualifier() {
         Version v = new Version("1.2.3.4.yyy.xxx.qualifier");
         checkVersion(v, "1", "2", "3", "4", "yyy.xxx.qualifier");
     }
     @Test
     public void testCompareHotfix() {
         Version v1 = new Version("1.2.3.0.yyy.xxx.qualifier");
         Version v2 = new Version("1.2.3.4.yyy.xxx.qualifier");
         assertTrue(v1.compareTo(v2) == -1);
     }
 
     @Test
     public void testCompareBugfix() {
         Version v1 = new Version("1.2.0.0.yyy.xxx.qualifier");
         Version v2 = new Version("1.2.3.4.yyy.xxx.qualifier");
         assertTrue(v1.compareTo(v2) == -1);
     }
 
     @Test
     public void testCompareMinor() {
         Version v1 = new Version("1.0.0.0.yyy.xxx.qualifier");
         Version v2 = new Version("1.2.3.4.yyy.xxx.qualifier");
         assertTrue(v1.compareTo(v2) == -1);
     }
 
     @Test
     public void testCompareMajor() {
         Version v1 = new Version("0.0.0.0.yyy.xxx.qualifier");
         Version v2 = new Version("1.2.3.4.yyy.xxx.qualifier");
         assertTrue(v1.compareTo(v2) == -1);
     }
 
     @Test
     public void testCompareLess() {
         Version v1 = new Version("0");
         Version v2 = new Version("1");
         assertTrue(v1.compareTo(v2) == -1);
     }
     @Test
     public void testCompareQualifier() {
         Version v1 = new Version("1.1.2-r2");
         Version v2 = new Version("1.1.2-r3");
         assertTrue(v1.compareTo(v2) == -1);
     }
     @Test
     public void testCompareQualifierNumeric() {
         Version v1 = new Version("1.1.2-r9");
         Version v2 = new Version("1.1.2-r10");
         assertTrue(v1.compareTo(v2) == -1);
     }
 
     @Test
     public void testCompareGreater() {
         Version v1 = new Version("2");
         Version v2 = new Version("1");
         assertTrue(v1.compareTo(v2) == 1);
     }
 
     @Test
     public void testCompareSame() {
         Version v1 = new Version("1.2.3.4.yyy.xxx.qualifier");
         Version v2 = new Version("1.2.3.4.yyy.xxx.qualifier");
         assertTrue("version compare should return 0", v1.compareTo(v2) == 0);
     }
 
     @Test
     public void testEqual() {
         Version v = new Version("1.2.3.4.yyy.xxx.qualifier");
         assertEquals("Versions are not equal", v, v);
     }
 
     @Test
     public void testNotEqual() {
         Version v1 = new Version("1.2.3.4.yyy.xxx.qualifier");
         Version v2 = new Version("1.2.3.4.yyy.xxx.qualifie");
         assertNotSame("Versions are not equal", v1, v2);
     }
 
     @Test
    public void IamNotNewerThanMyself(){
        Version v1 = new Version("1.2");
        Version v2 = new Version("1.2");
        assertTrue(v1.notNewerThan(v2));
    }

    @Test
     public void testSort() {
         List<Version> versions = new ArrayList<Version>();
         versions.add(new Version("0.quali"));
         versions.add(new Version("0.qualifier"));
         versions.add(new Version("1.1.qual"));
         versions.add(new Version("1.2.qual"));
         versions.add(new Version("1.2.3.4.yyy.xxx.qualifie"));
         versions.add(new Version("1.2.3.4.yyy.xxx.qualifier"));
         versions.add(new Version("2.2.0.99.96"));
         versions.add(new Version("2.2.0.99.104"));
         versions.add(new Version("2.2.0.99.110"));
         List<Version> versionsSorted = new ArrayList<Version>();
         versionsSorted.addAll(versions);
         Collections.sort(versionsSorted);
         for (int i = 0; i < versions.size(); i++) {
             assertEquals("sorted version is not the same as unsorted (sorted: "
                     + versionsSorted + ", versions: " + versions + ")",
                     versions.get(i), versionsSorted.get(i));
         }
     }
 
     private void checkVersion(Version v, String major, String minor,
                               String bugfix, String hotfix) {
         checkVersion(v, major, minor, bugfix, hotfix, null);
     }
 
     private void checkVersion(Version v, String major, String minor,
                               String bugfix, String hotfix, String qualifier) {
         assertEquals("Major version does not match", major, v.getMajor());
         assertEquals("Minor version does not match", minor, v.getMinor());
         assertEquals("Bugfix version does not match", bugfix, v.getBugfix());
         assertEquals("Hotfix version does not match", hotfix, v.getHotfix());
         assertEquals("Qualifier does not match", qualifier, v.getQualifier());
     }
 
     @Test
     public void testComparison(){
         assertThat(new Version("1.0").compareTo(new Version("2.0")), is(-1));
         assertThat(new Version("1.0").compareTo(new Version("0.9")), is(1));
         assertThat(new Version("0.9-abc").compareTo(new Version("0.9-abd")), is(-1));
         assertThat(new Version("0.9").compareTo(new Version("0.9-alpha")), is(1));
         assertThat(new Version("0.9-beta").compareTo(new Version("0.9")), is(-1));
         assertThat(new Version("0.9-beta").compareTo(new Version("0.9.0-beta")), is(0));
         assertThat(new Version("0.9-beta2").compareTo(new Version("0.9.0-beta3")), is(-1));
         assertThat(new Version("0.9-beta9").compareTo(new Version("0.9.0-beta10")), is(-1));
     }
 }
