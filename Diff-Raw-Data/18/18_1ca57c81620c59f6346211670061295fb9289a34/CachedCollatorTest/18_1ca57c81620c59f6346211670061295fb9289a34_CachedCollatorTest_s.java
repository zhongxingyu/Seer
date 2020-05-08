 /* $Id: CachedCollatorTest.java,v 1.6 2007/12/04 13:22:01 mke Exp $
  * $Revision: 1.6 $
  * $Date: 2007/12/04 13:22:01 $
  * $Author: mke $
  *
  * The SB Util Library.
  * Copyright (C) 2005-2007  The State and University Library of Denmark
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package dk.statsbiblioteket.util;
 
 import java.util.Locale;
 import java.util.Random;
 import java.util.Arrays;
 import java.text.Collator;
 import java.text.CollationKey;
 import java.io.StringWriter;
 import java.io.File;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import junit.framework.TestCase;
 
 @SuppressWarnings({"DuplicateStringLiteralInspection"})
 public class CachedCollatorTest extends TestCase {
     public CachedCollatorTest(String name) {
         super(name);
     }
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     public void tearDown() throws Exception {
         super.tearDown();
     }
 
     String summa = "eaoi 0ntr1s24cl93857hd6pgum.bfv:xwykj_z/-qASPCXIUø"
                    + "NEGæ$>é#Väåö&ü^áāLó~–íãT*@ıç%čâèBM|š—FYêDúàūžñRð"
                    + "·Oć−ôë,łβα°±HşīîJõKZQēśδ†ṣōïěğăńýřûė→ìþ×µμγ§ßο∼"
                    + "£ò▿ưκđσơλùειżτę­νπąρœ¤őηǩĸºφ≥ςĭωί³⋅≤иũňţθό∞ή™υź"
                    + "еаέ…²ªW€≈ψ¢нт•↑ľ¾ύχ₂ώр‰űάÿ¹о½ẽ‐ųζů;л'‡ξĩ√⁰¼ﬁĝȩ←"
                    + "вп的ŭɛс∈〉〈=дб″÷书ĉǧм∑ŕ‚₃↓⁺зуŏťя图♭⩾∫к∂ĕﬂϕď≃ч∇₁⩽ŝℓ∥馆₀ˉ∩≡≅ц∷ǀ˜≠∧ġ∆ф了 г⊥ņ⁻¬ĵ↔ḥ₄?ыхṉй∗";
     String dk = "abcdefghijklmnopqrstuvwxyzæøåABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ";
     String ascii = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
     String higher = "abcdefg٭";
 
     public void testSummaStatsCompare() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"), summa);
         genericComparisons(collator);
     }
 
     public void testDKCompare() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"), dk);
         genericComparisons(collator);
     }
 
     public void testExtendedCompare() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"), ascii);
         genericComparisons(collator);
     }
 
     public void testHigherCompare() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"), higher);
         genericComparisons(collator);
     }
 
     public void testEmptyStats() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"));
         genericComparisons(collator);
     }
 
     Collator plain = Collator.getInstance(new Locale("da"));
     public void genericComparisons(Collator collator) throws Exception {
         // Note: "aa" is "a" + "a" in this unit test.
         String[] checks = new String[]{
                 "abc", "abf", "abcd", "abf", "æbler", "bananer", "abc٭",
                 "abcd", "kirsebær", "øllebrød", "abc٭ü", "ab ", "ab  ",
                 "b", "bb", "b"};
         for (String check1 : checks) {
             for (String check2 : checks) {
                 int plainCompare = plain.compare(check1, check2);
                 int customCompare = collator.compare(check1, check2);
                 plainCompare =
                         plainCompare < 0 ? -1 : plainCompare > 0 ? 1 : 0;
                 customCompare =
                         customCompare < 0 ? -1 : customCompare > 0 ? 1 : 0;
                 assertEquals("The comparison should yield the same results for "
                              + "compareTo(" + check1 + ", " + check2 + ")",
                            plainCompare, customCompare);
             }
         }
     }
 
     Random random = new Random();
     protected String randomString(int minLength, int maxLength,
                                   String normal, String medium, String rare,
                                   double mediumChance, double rareChance) {
         int length = random.nextInt(maxLength - minLength) + minLength;
         StringWriter sw = new StringWriter(length);
         for (int i = 0 ; i < length ; i++) {
             double dice = random.nextDouble();
             String pool;
             if (dice <= rareChance) {
                 pool = rare;
             } else if (dice <= mediumChance) {
                 pool = medium;
             } else {
                 pool = normal;
             }
             sw.append(pool.charAt(random.nextInt(pool.length())));
         }
         return sw.toString();
     }
 
     public void dumpRandom() throws Exception {
         for (int i = 0 ; i < 10 ; i++) {
             System.out.println(
                     randomString(2, 5, "abcd", " -", "æøå", 0.3, 0.1));
         }
     }
 
     String NORMAL = "eaoi 0ntr1s24cl93857hd6pgum.bfv:xwykj_z/-qASPCXIUøNEGæ$>é"
                     + "#Väåö&ü^áāLó~–íãT*@ıç%čâèBM|š—FYêDúàūžñRð·Oć−ôë,łβα°±Hş"
                     + "īîJõKZQēśδ†ṣōïěğăńýřûė→ìþ×µμγ§ßο∼£ò▿ưκđσơλùειżτę­νπąρœ"
                     + "¤őηǩĸºφ≥ςĭωί³⋅≤иũňţθό∞ή™υźеаέ…²ªW€≈ψ¢нт•↑ľ¾ύχ₂ώр‰űάÿ¹о½"
                     + "ẽ‐ųζů;л'‡ξĩ√⁰¼ﬁĝȩ←вп的ŭɛс∈〉〈=дб″÷书ĉм∑ŕ‚₃↓⁺зуŏťя图♭⩾∫к∂ĕﬂ"
                     + "ϕď≃ч∇₁⩽ŝℓ∥馆₀ˉ∩≡≅ц∷ǀ˜≠∧ġ∆ф了 г⊥";
     String MEDIUM = "ņ⁻¬ĵ↔ḥ₄?ыхй∗";
     String RARE =   "∙目本⇒¥和⁴数⟩į作国}{ǒϵ∝据";
     @SuppressWarnings({"DuplicateStringLiteralInspection"})
     public void testCompareSpeed() throws Exception {
         int POOL_SIZE = 500;
 
         int MIN_LENGTH = 10;
         int MAX_LENGTH = 50;
         double MEDIUMPROBABILITY = 0.2;
         double RAREPROBABILITY = 0.00001; // Real world number from Summa
 
         System.out.println("Generating pool");
         String[] pool = new String[POOL_SIZE];
         for (int i = 0 ; i < POOL_SIZE ; i++) {
             pool[i] = randomString(MIN_LENGTH, MAX_LENGTH, NORMAL, MEDIUM, RARE,
                                    MEDIUMPROBABILITY, RAREPROBABILITY);
         }
 
         Collator cachedCollator = new CachedCollator(new Locale("da"),
                                                      NORMAL + MEDIUM);
         Collator compareCollator = new CompareToCollator();
 
         double comparebps =
                 testSpeedOfCollator("Compare", pool, compareCollator);
         double plainbps = testSpeedOfCollator("Plain", pool, plain);
         double cachedbps = testSpeedOfCollator("Cached", pool, cachedCollator);
         System.out.println("\nCached compare is "
                            + String.format("%f2", cachedbps / plainbps)
                            + " times faster than standard collator");
         System.out.println("Cached compare is "
                            + String.format("%f2", cachedbps / comparebps)
                            + " times faster than String.compareTo");
 
         long comparesort = testSpeedOfSort("Compare", pool, compareCollator);
         long plainsort = testSpeedOfSort("Plain", pool, plain);
         long cachedsort = testSpeedOfSort("Cached", pool, cachedCollator);
         System.out.println(
                 String.format("Array.sort speed: plain(%d ms) / cached(%d ms) "
                               + "= %f2 times faster for cached",
                               plainsort, cachedsort,
                               1.0 * plainsort / cachedsort));
         System.out.println(
                 String.format("Array.sort speed: compare(%d ms) / cached(%d ms) "
                               + "= %f2 times faster for cached",
                               comparesort, cachedsort,
                               1.0 * comparesort / cachedsort));
     }
 
     protected long testSpeedOfSort(String collatorName, String[] pool,
                                      Collator collator) {
         String[] poolCopy = pool.clone();
         System.gc();
         Profiler profiler = new Profiler();
         Arrays.sort(poolCopy, collator);
         return profiler.getSpendMilliseconds();
     }
 
     protected double testSpeedOfCollator(String collatorName,
                                          String[] pool, Collator collator) {
         int RUNS = 3;
         // Warmup
         for (int j = 0 ; j < 3 ; j++) {
             for (int i = 0 ; i < pool.length / 2 ; i++) {
                 collator.compare(pool[i], pool[pool.length-1-i]);
             }
         }
 
         System.gc();
         Profiler profiler = new Profiler();
         String dummy1 = "d";
         String dummy2 = "d";
         for (int run = 0 ; run < RUNS ; run++) {
             for (int i = 0 ; i < pool.length / 2 ; i++) {
                 dummy1 = pool[i];
                 dummy2 = pool[pool.length-1-i];
                 profiler.beat();
             }
         }
         if (dummy1 == null || dummy2 == null){
             System.out.println("To avoid optimization");
         }
         double dryms = profiler.getSpendMilliseconds();
 //        System.out.println("Dry run speed: "
 //                           + Math.round(profiler.getBps())
 //                           + " string requests/second at " + profiler.getBeats()
 //                           + " request");
 
         profiler = new Profiler();
         for (int run = 0 ; run < RUNS ; run++) {
             for (int i = 0 ; i < pool.length / 2 ; i++) {
                 collator.compare(pool[i], pool[pool.length-1-i]);
                 profiler.beat();
             }
         }
         double bps = profiler.getBps();
         double realms = profiler.getSpendMilliseconds();
         if (realms < dryms) {
             System.out.println("Warning: realms (" + realms 
                                + ") is smaller than dryms (" + dryms + ")");
         }
         double totalms = realms - dryms;
         double totalbps = 1.0 * profiler.getBeats() / totalms;
         System.out.println(collatorName + " compare speed minus dry-run: "
                            + Math.round(totalbps)
                            + " comparisons/second at " + profiler.getBeats()
                            + " comparisons");
         return totalbps;
     }
 
     public void testGetCollationKey() throws Exception {
         //TODO: Test goes here...
     }
 
     public static Test suite() {
         return new TestSuite(CachedCollatorTest.class);
     }
 
     public void dumpDefault() throws Exception {
         System.out.println(
                 new CachedCollator(new Locale("da")).getBasicChars());
     }
 
     protected class CompareToCollator extends Collator {
 
         public int compare(String source, String target) {
             return source.compareTo(target);
         }
 
         public CollationKey getCollationKey(String source) {
             return null;
         }
 
         public int hashCode() {
             return 0;
         }
     }
 
     public void testNull() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"));
         String[] strings = new String[]{null, "a"};
         Arrays.sort(strings, collator);
         assertEquals("The first element should be 'a'", "a", strings[0]);
         assertEquals("The second element should be null", null, strings[1]);
     }
 
 
     public void testCache() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"), "bca");
         assertEquals("The order shold be correct",
                      "abc", collator.getCachedChars());
     }
 
     public void testDuplicateReduction() throws Exception {
         CachedCollator collator = new CachedCollator(new Locale("da"), "aaab");
         assertEquals("The number of unique chars should be 2",
                      "ab", collator.getCachedChars());
     }
 
     // Bad unit test as it relies on Idea.
     public void noTestBasicLoad() throws Exception {
         String common = "abcdefg";
         File statsFile = new File("classes", CachedCollator.CHARSTATS);
         statsFile.deleteOnExit();
         Files.saveString(common, statsFile);
         System.out.println(statsFile.getAbsoluteFile());
         assertTrue("The file '" + statsFile + "' should exist",
                    statsFile.exists());
         CachedCollator collator = new CachedCollator(new Locale("da"));
         assertEquals("The chars should be loaded from the file system",
                      common, collator.getCachedChars());
     }
 
     Collator dkCollator = new CachedCollator(new Locale("da"), dk);
     public void testDKCollator() throws Exception {
         assertTrue("i and a should be sorted correctly with natural",
                      "Drillenisse".compareTo("Drabant") > 0);
         assertTrue("i and a should be sorted correctly with collator",
                      dkCollator.compare("Drillenisse", "Drabant") > 0);
 
         assertTrue("i and ø should be sorted correctly with natural",
                      "Drøbel".compareTo("Drillenisse") > 0);
         assertTrue("i and ø should be sorted correctly with collator",
                      dkCollator.compare("Drøbel", "Drillenisse") > 0);
     }
 
     public void testSpaceSort() throws Exception {
         assertTrue("Standard compareTo should sort space first",
                    "a b".compareTo("ab") < 0);
 
         Collator sansSpaceStandard = Collator.getInstance(new Locale("da"));
         assertTrue("Standard Collator should sort space last",
                    sansSpaceStandard.compare("a b", "ab") > 0);
 
         Collator sansSpace = new CachedCollator(new Locale("da"), "");
         assertTrue("None-space-modified CachedCollator should sort space last",
                    sansSpace.compare("a b", "ab") > 0);
 
        Collator space = new CachedCollator(new Locale("da"), dk, true);
         assertTrue("Space-modified Collator should sort space first",
                   space.compare("a b", "aa") < 0);
     }
 }
