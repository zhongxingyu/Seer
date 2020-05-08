 import org.junit.Test;
 
 import java.util.Objects;
 import java.util.function.Predicate;
 import java.util.function.Predicates;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 
 
 public class PredicateTests {
 
     @Test
     public void youCanMakeYourOwnPredicates(){
         Predicate<String> isFoo = (x)-> Objects.equals(x,"foo");
 
         assertTrue(isFoo.test("foo"));
         assertFalse(isFoo.test("bar"));
     }
 
     @Test
     public void canChainPredicates(){
         Predicate<Integer> gt6 = (x)-> x > 6;
         Predicate<Integer> lt9 = (x)-> x < 9;
 
         assertTrue(gt6.and(lt9).test(7));
         assertFalse(gt6.and(lt9).test(5));
         assertFalse(gt6.and(lt9).test(10));
     }
 
     @Test
     public void canProduceANegatingPredicate(){
         Predicate<String> isFoo = (x)-> Objects.equals(x,"foo");
 
         assertFalse(isFoo.negate().test("foo"));
         assertTrue(isFoo.negate().test("bar"));
     }
 
     @Test
     public void canProduceOrPredicates(){
         Predicate<Integer> is6 = (x)->x.equals(6);
         Predicate<Integer> is9 = (x)->x.equals(9);
 
 
         assertTrue(is6.or(is9).test(6));
         assertTrue(is6.or(is9).test(9));
         assertFalse(is6.or(is9).test(7));
     }
 
     @Test
     public void canDoAXOr(){
        Predicate<String> containsF = (x) -> x.contains("F");
        Predicate<String> startsWithp = (x) -> x.startsWith("p");
 
        Predicate<String> xor = containsF.xor(startsWithp);
 
        assertFalse(xor.test("pAndFoo"));
        assertTrue(xor.test("patty"));
        assertTrue(xor.test("Barney Fife"));
        assertFalse(xor.test("Neither is true here"));
     }
 
     @Test
     public void findNulls(){
         Predicate<String> p = Predicates.isNull();
 
         assertFalse(p.test("foo"));
         assertTrue(p.test(null));
     }
 
     @Test
     public void findNotNulls(){
         Predicate<String> p = Predicates.nonNull();
 
         assertFalse(p.test(null));
         assertTrue(p.test("foo"));
     }
 
     @Test
     public void alwaysFalseIsAlwaysFalse(){
         Predicate<String> p = Predicates.alwaysFalse();
 
         assertFalse(p.test(null));
         assertFalse(p.test("foo"));
     }
 
     @Test
     public void alwaysTrueIsAlwaysTrue(){
         Predicate<String> p = Predicates.alwaysTrue();
 
         assertTrue(p.test(null));
         assertTrue(p.test("foo"));
     }
 
     @Test
     public void canTellIfItsAnInstanceOf(){
         Predicate<Foo> p = Predicates.instanceOf(FooBar.class);
 
         assertFalse(p.test(new Foo()));
         assertTrue(p.test(new FooBar()));
     }
 
     static class FooBar extends Foo {}
 
     @Test
     public void cantellIfSame(){
         Foo foo = new Foo();
         Predicate<Foo> p = Predicates.isSame(foo);
 
         assertTrue(p.test(foo));
         assertFalse(p.test(new Foo()));
     }
 
     @Test
     public void canTellIfEquals(){
         Predicate<String> p = Predicates.isEqual("foo");
 
         assertTrue(p.test("foo"));
         assertFalse(p.test("bar"));
     }
 }
