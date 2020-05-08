 package ru.rulex.matchers;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static ru.rulex.conclusion.delegate.ProxyUtils.callOn;
 import static ru.rulex.matchers.RulexObjectMatcher.selector;
 
 import org.fest.assertions.api.Assertions;
 import org.hamcrest.Matcher;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 
 import ru.rulex.conclusion.Model;
 import ru.rulex.matchers.RulexMatcher;
 import ru.rulex.matchers.RulexAnalyzer;
 import ru.rulex.matchers.RulexMatchersBuilder;
 import ru.rulex.matchers.AssertionAwareListener;
 import static ru.rulex.matchers.RulexObjectMatcher.projection;
 
 public class RulexMatchersTest
 {
   @Test
   public void testRulexMatcherChainCalls()
   {
     Model model = Model.values( 4, 56.7f );
 
     RulexMatcher<Model> matcher0 = selector( Model.class, callOn( Model.class ).getInteger() )
         .lessThan( 5 ).and(
             selector( Model.class, callOn( Model.class ).getInteger() ).lessThan( 6 ) );
 
     assertThat( matcher0.matches( model ) ).isTrue();
 
     RulexMatcher<Model> matcher = selector( Model.class, callOn( Model.class ).getInteger() )
         .lessThan( 5 ).and(
             selector( Model.class, callOn( Model.class ).getFloat() ).lessThan( 56.8f ) );
 
     assertThat( matcher.matches( model ) ).isTrue();
   }
 
   @Test
   public void shouldBeEqualsOnSameFields()
   {
     RulexMatcher<Model> matcher = selector( Model.class, callOn( Model.class ).getInteger() )
         .isEquals( selector( Model.class, callOn( Model.class ).getInteger() ) );
 
     Model model = Model.values( 4, 5 );
     assertThat( matcher.matches( model ) ).isTrue();
   }
 
   @Test
   public void oneIntegerShouldBeLessOtherInteger()
   {
     RulexMatcher<Model> matcher = modelSelector( callOn( Model.class ).getInteger() ).lessThan(
         modelSelector( callOn( Model.class ).getOtherInteger() ) );
 
     final Model model = Model.values( 1, 2 );
 
     // separate configuration from processing
     RulexAnalyzer analyzed = projection( Model.class ).assertThat( matcher )
         .on( model );
     analyzed.analyze( new AssertionAwareListener()
     {
       @Override
       public void passed( Object analysedObject, Matcher<?> matcher )
       {
         assertThat( analysedObject ).isSameAs( model );
       }
 
       @Override
       public void failed( Object analysedObject, Matcher<?> matcher )
       {
         Assertions.fail( "failed" );
       }
 
       @Override
       public void filtered( Object analysedObject, Matcher<?> matcher )
       {
         Assertions.fail( "failed" );
       }
 
       @Override
       public void unexpected( Object analysedObject, Exception exception )
       {
         Assertions.fail( "failed" );
       }
 
       @Override
       public void done()
       {
         assertThat( true ).isTrue();
       }
     } );
   }
 
   @Test
   public void oneShouldBePassedSecondShouldBeFiltered()
   {
     final Model first = Model.values( 121 );
     final Model second = Model.values( 49 );
 
     final ImmutableList<Model> list = ImmutableList.of( first, second );
 
     RulexMatcher<Model> filter = modelSelector( callOn( Model.class ).getInteger() ).lessThan( 122 );
 
     RulexMatcher<Model> matcher = modelSelector( callOn( Model.class ).getInteger() ).moreThan( 50 );
 
     projection( Model.class ).forEach( filter ).assertThat( matcher ).in( list )
         .analyze( new AssertionAwareListener()
         {
           @Override
           public void passed( Object analysedObject, Matcher<?> matcher )
           {
             assertThat( analysedObject ).isSameAs( first );
           }
 
           @Override
           public void failed( Object analysedObject, Matcher<?> matcher )
           {
             assertThat( analysedObject ).isSameAs( second );
           }
 
           @Override
           public void filtered( Object analysedObject, Matcher<?> matcher )
           {
             Assertions.fail( "unexpected" );
           }
 
           @Override
           public void unexpected( Object analysedObject, Exception exception )
           {
             Assertions.fail( "unexpected" );
           }
 
           @Override
           public void done()
           {
             assertThat( true ).isTrue();
           }
         } );
   }
 
   /**
    * Method for creation custom builder with specific type parameter
    * 
    * @param arg
    * @return
    */
   public static <E extends Comparable<? super E>> RulexMatchersBuilder<Model> modelSelector(
       final E arg )
   {
     return selector( Model.class, arg );
   }
 }
