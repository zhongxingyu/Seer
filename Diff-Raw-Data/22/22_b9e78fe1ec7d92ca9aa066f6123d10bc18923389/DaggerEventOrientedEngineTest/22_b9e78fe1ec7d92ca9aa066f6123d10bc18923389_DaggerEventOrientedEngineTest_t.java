 package ru.rulex.conclusion.dagger;
 
 import javax.inject.Named;
 
 
 import org.junit.Test;
 import ru.rulex.conclusion.Model;
 import ru.rulex.conclusion.PhraseBuildersFacade.DaggerEventPhrasesBuilder;
 import ru.rulex.conclusion.PhraseBuildersFacade.DaggerMutableEventPhraseBuilder;
 
 import static dagger.ObjectGraph.create;
import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 import static junit.framework.TestCase.assertTrue;
 import static org.fest.assertions.api.Assertions.assertThat;
 import static ru.rulex.conclusion.PhraseBuildersFacade.environment;
 import static ru.rulex.conclusion.PhraseBuildersFacade.var;
 import static ru.rulex.conclusion.dagger.DaggerImmutableDependencyAnalyzerModule.*;
 import static ru.rulex.conclusion.dagger.DaggerMutableDependencyAnalyzerModule.*;
 import static ru.rulex.conclusion.delegate.ProxyUtils.callOn;
 
 public class DaggerEventOrientedEngineTest
 {
   @Test
   public void testSimpleDaggerBuilder()
   {
     final DaggerEventPhrasesBuilder builder = create(
       $expression(
         $less( 19, callOn( Model.class ).getInteger() ),
         $less( 19, callOn( Model.class ).getOtherInteger() ),
         $more( 56.78f, callOn( Model.class ).getFloat() ) ))
       .get( DaggerEventPhrasesBuilder.class );
     
     assertThat( builder.sync( Model.values( 20, 78 ) ) ).isTrue();
   }
 
   @Test
   public void testMutableDaggerBuilder()
   {
     final String val1 = "x1";
     final String val2 = "x2";
 
     final DaggerMutableEventPhraseBuilder mutableBuilder = create(
       $mutableExpression(
         $less0( 12, val1 ),
         $less0( 13, val2 )
       )
     ).get( DaggerMutableEventPhraseBuilder.class );
 
     final boolean result = mutableBuilder.populateFrom(
       environment(
         var( val1, callOn( Model.class ).getInteger() ),
         var( val2, callOn( Model.class ).getInteger() )
       )
     ).sync( Model.values( 20, 78 ) );
 
     assertTrue( result );
   }
 
  @Test
   public void testMutableDaggerBuilderWithMissedValue()
   {
     final DaggerMutableEventPhraseBuilder mutableBuilder = create(
       $mutableExpression(
         $less0( 64, "a" ),
         $less0( 678, "b" ),
         $less0( 6755656, "c" )
       )
     ).get( DaggerMutableEventPhraseBuilder.class );
 
    try {
      mutableBuilder.populateFrom(
        environment(
          var( "a", callOn( Model.class ).getInteger() )
        )
      ).sync( Model.values( 20, 78 ) );
    } catch (IllegalStateException ex) {
      assertEquals( ex.getMessage(), "Undefined variables was found: b,c");
    } catch (Exception e) {
      fail();
    }
   }
 
   /*
   TODO: implement like this API , with AnnotationProcessor
   public void testAnnotationApi() {
     //@Module(addsTo = RootModule.class, injects = { C.class, D.class })
     @Expression( onClass = Model.class,
         conditions = { callOn( Model.class ).getFloat(), callOn( Model.class ).getFloat() } )
 
   }
   */
 
   interface Expression
   {
     @Less( "{value}" )
     void someValue1( @Named( "value" ) Integer value );
 
     @More( "{value}" )
     void someValue2( @Named( "value" ) Integer value0 );
   }
 }
