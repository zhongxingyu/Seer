 package cx.ath.mancel01.utils;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import org.junit.Assert;
 import org.junit.Test;
 
 import static cx.ath.mancel01.utils.F.*;
 import static cx.ath.mancel01.utils.C.*;
 import static cx.ath.mancel01.utils.M.*;
 
 public class UtilsTest {
 
     @Test
     public void testCollections() {
         Collection<String> values = Arrays.asList(new String[]
             {"Hello", "dude", ",", "how", "are", "you", "today", "!"});
         Collection<String> expected = Arrays.asList(new String[]
             {"HELLO", "DUDE", ",", "HOW", "ARE", "YOU", "TODAY", "!"});
         Collection<String> result = 
             forEach(values)
                 .apply(new Transformation<String, String>() {
                        @Override
                        public String apply(String t) {
                            return t.toUpperCase();
                        }
                    }
                ).get();
         Assert.assertEquals(expected, result);
     }
 
     @Test
     public void testJoin() {
         Collection<String> values = Arrays.asList(new String[] {"Hello", "dude", "!"});
         String expected1 = "Hello | dude | !";
         String expected2 = "HELLO | DUDE | !";
         String expected3 = "before => HELLO | DUDE | ! <= after";
         String join1 = join(values).with(" | ");
         Assert.assertEquals(expected1, join1);
         String join2 = join(values).labelized(new Transformation<String, String>() {
             @Override
             public String apply(String t) {
                 return t.toUpperCase();
             }
         }).with(" | ");
         Assert.assertEquals(expected2, join2);
         String join3 = join(values).labelized(new Transformation<String, String>() {
             @Override
             public String apply(String t) {
                 return t.toUpperCase();
             }
         }).before("before => ").after(" <= after").with(" | ");        
         Assert.assertEquals(expected3, join3);
     }
 
     @Test
     public void testTransformation() {
         Collection<Person> values = Arrays.asList(new Person[]
             {new Person("John", "Doe"), new Person("John", "Adams")});
         Collection<String> expected = Arrays.asList(new String[] {"John", "John"}); 
         String expected1 = "John | John";
         Collection<String> transformed = forEach(values).apply(new Transformation<Person, String>() {
             @Override
             public String apply(Person t) {
                 return t.getName();
             }
         }).get();
         Assert.assertEquals(expected, transformed);
         String join1 = join(values).labelized(new Transformation<Person, String>() {
             @Override
             public String apply(Person t) {
                 return t.getName();
             }
         }).with(" | ");
         Assert.assertEquals(expected1, join1);
     }
 
     @Test
     public void testCollections2() {
         Collection<Integer> values = Arrays.asList(new Integer[]
             {1, 2, 3, 4, 5, 6, -8, -2, -1, -28});
         int expectedTotal = 21;
         int expectedCount = 6;
         Sum sum = new Sum();
         forEach(values)
             .filteredBy(greaterThan(0))
                 .execute(sum);
         Assert.assertEquals(expectedTotal, sum.sum());
         Assert.assertEquals(expectedCount, sum.count());
         sum = new Sum();
         forEach(values)
             .filteredBy(greaterThan(0))
             .filteredBy(lesserThan(6))
                 .execute(sum);
         Assert.assertEquals(15, sum.sum());
         Assert.assertEquals(5, sum.count());
     }
 
     @Test
     public void testPatternMatching() {
         String value = "foobar";
         Option<String> matched =
             match(value)
                 .andExpect(String.class)
                     .with(
                         caseEquals("one")
                             .then(new OneFunction()),
                         caseEquals("two")
                             .then(new TwoFunction()),
                         caseEquals("three")
                             .then(new ThreeFunction()),
                         otherCases()
                             .then(new OtherFunction())
                     );
         Assert.assertEquals(matched.get(), "-1");
         
         value = "one";
         matched =
             match(value)
                 .andExpect(String.class)
                     .with(
                         caseEquals("one")
                             .then(new OneFunction()),
                         caseEquals("two")
                             .then(new TwoFunction()),
                         caseEquals("three")
                             .then(new ThreeFunction()),
                         otherCases()
                             .then(new OtherFunction())
                     );
         Assert.assertEquals(matched.get(), "It's a one");
         
         value = "two";
         matched =
             match(value)
                 .andExpect(String.class)
                     .with(
                         caseEquals("one")
                             .then(new OneFunction()),
                         caseEquals("two")
                             .then(new TwoFunction()),
                         caseEquals("three")
                             .then(new ThreeFunction()),
                         otherCases()
                             .then(new OtherFunction())
                     );
         Assert.assertEquals(matched.get(), "It's a two");
         
         value = "three";
         matched =
             match(value)
                 .andExpect(String.class)
                     .with(
                         caseEquals("one")
                             .then(new OneFunction()),
                         caseEquals("two")
                             .then(new TwoFunction()),
                         caseEquals("three")
                             .then(new ThreeFunction()),
                         otherCases()
                             .then(new OtherFunction())
                     );
         Assert.assertEquals(matched.get(), "It's a three");
 
         
     }
 
     @Test
     public void testPatternMatching2() {
         String value = "one";
         String ret = "-1";
         for (String s : caseEquals("one").match(value)) {
             ret = "It's a one";
         }
         for (String s : caseEquals("two").match(value)) {
             ret = "It's a two";
         }
         for (String s : caseEquals("three").match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "It's a one");
 
         value = "two";
         ret = "-1";
         for (String s : caseEquals("one").match(value)) {
             ret = "It's a one";
         }
         for (String s : caseEquals("two").match(value)) {
             ret = "It's a two";
         }
         for (String s : caseEquals("three").match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "It's a two");
 
         value = "three";
         ret = "-1";
         for (String s : caseEquals("one").match(value)) {
             ret = "It's a one";
         }
         for (String s : caseEquals("two").match(value)) {
             ret = "It's a two";
         }
         for (String s : caseEquals("three").match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "It's a three");
 
         value = "foobar";
         ret = "-1";
         for (String s : caseEquals("one").match(value)) {
             ret = "It's a one";
         }
         for (String s : caseEquals("two").match(value)) {
             ret = "It's a two";
         }
         for (String s : caseEquals("three").match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "-1");
     }
 
     @Test
     public void testPatternMatching3() {
         String value = "one";
         String ret = "-1";
         for (String s : with(caseEquals("one"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a one";
         }
         for (String s : with(caseEquals("two"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a two";
         }
         for (String s : with(caseEquals("three"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "It's a one");
 
         value = "two";
         ret = "-1";
         for (String s : with(caseEquals("one"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a one";
         }
         for (String s : with(caseEquals("two"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a two";
         }
         for (String s : with(caseEquals("three"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "It's a two");
 
         value = "three";
         ret = "-1";
         for (String s : with(caseEquals("one"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a one";
         }
         for (String s : with(caseEquals("two"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a two";
         }
         for (String s : with(caseEquals("three"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "It's a three");
 
         value = "foobar";
         ret = "-1";
         for (String s : with(caseEquals("one"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a one";
         }
         for (String s : with(caseEquals("two"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a two";
         }
         for (String s : with(caseEquals("three"))
                             .and(caseLengthGreater(2))
                                 .match(value)) {
             ret = "It's a three";
         }
         Assert.assertEquals(ret, "-1");
     }
 
     @Test
     public void curryTest() {
         Option<Method> m = method(UtilsTest.class, "concat",
                     String.class, String.class, String.class);
         Option<Method> m2 = method(UtilsTest.class, "concat",
                     Integer.class, String.class, Long.class);
         if (m.isDefined()) {
             String value = curry(m, this, String.class)
                     ._("A")._("B")._("C")
                     .apply();
 
             String expected = "ABC";
             Assert.assertEquals(expected, value);
 
             CurryFunction<String> function =
                 curry(m, this, String.class)
                     ._("A")._("B");
             String value2 =
                 function
                     ._("C")
                     .apply();
             String value22 =
                 function
                     ._("D")
                     .apply();
             String value222 =
                 function
                     ._("E")
                     .apply();
 
             Assert.assertEquals(expected, value2);
             Assert.assertEquals("ABD", value22);
             Assert.assertEquals("ABE", value222);
 
             String value3 =
                 new MyCurryFunction()
                     ._("A")._("B")._("C")
                     .apply();
             
             Assert.assertEquals(expected, value3);
 
             CurryFunction<String> function2 =
                 new MyCurryFunction()
                     ._("A");
             String value4 =
                 function2
                     ._("B")._("C")
                     .apply();
 
             Assert.assertEquals(expected, value4);
         } else {
             Assert.fail("Method not defined");
         }
         if (m2.isDefined()) {
             CurryFunction<String> function =
                 curry(m2, this, String.class)
                     ._(new Long(3))._("2");
             String value5 =
                 function
                     ._(1)
                     .apply();
             
             String expected = "123";
             Assert.assertEquals(expected, value5);
         } else {
             Assert.fail("Method not defined");
         }
     }
 
     public class MyCurryFunction extends AbstractCurryFunction<String> {
 
         @Override
         public CurryFunction<String> create(List<Object> args) {
             MyCurryFunction f = new MyCurryFunction();
             f.init(args.toArray(new Object[args.size()]));
             return f;
         }
 
         @Override
         public String apply() {
             return join(args).with("");
         }
     }
 
     public String concat(String s1, String s2, String s3) {
         return s1.concat(s2).concat(s3);
     }
 
     public String concat(Integer s1, String s2, Long s3) {
         return ("" + s1).concat(s2).concat("" + s3);
     }
 
     public class OneFunction implements MatchCaseFunction<String, String> {
 
         @Override
         public Option<String> apply(String value) {
             return F.some("It's a one");
         }
     }
 
     public class TwoFunction implements MatchCaseFunction<String, String> {
 
         @Override
         public Option<String> apply(String value) {
             return F.some("It's a two");
         }
     }
 
     public class ThreeFunction implements MatchCaseFunction<String, String> {
 
         @Override
         public Option<String> apply(String value) {
             return F.some("It's a three");
         }
     }
 
     public class OtherFunction implements MatchCaseFunction<String, String> {
 
         @Override
         public Option<String> apply(String value) {
             return F.some("-1");
         }
     }
 
     private class Sum implements Function<Integer> {
         
         private int value = 0;
 
         private int items = 0;
 
         public void add(int i) {
             value += i;
             items++;
         }
 
         public int sum() {
             return value;
         }
 
         public int count() {
             return items;
         }
 
         @Override
         public void apply(Integer t) {
             add(t);
         }
     }
 
     private class Person {
         
         private String name;
 
         private String surname;
 
         public Person(String name, String surname) {
             this.name = name;
             this.surname = surname;
         }
 
         public String getName() {
             return name;
         }
 
         public String getSurname() {
             return surname;
         }
     }
 }
