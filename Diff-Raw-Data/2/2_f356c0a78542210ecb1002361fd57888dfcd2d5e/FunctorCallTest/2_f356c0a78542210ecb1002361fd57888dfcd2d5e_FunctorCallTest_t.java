 package gazap.site.web.mvc.wrime;
 
 import gazap.site.web.mvc.wrime.functor.I18Nizer;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 public class FunctorCallTest {
     private TestResource resources = new TestResource(FunctorCallTest.class);
 
     private void check(String resource) throws WrimeException {
         WrimeCompiler compiler = parse(resources.load(resource + ".txt"));
         resources.verify(resource + ".code", compiler.getClassCode());
     }
 
     private void checkError(String resource, String message) {
         boolean caught = false;
         try {
             check(resource);
         } catch (WrimeException e) {
             caught = true;
             assertEquals(message, e.getMessage());
         }
         if (!caught) {
             fail("Exception expected");
         }
     }
 
     private WrimeCompiler parse(ScriptResource resource) throws WrimeException {
         return new WrimeEngine()
                 .addFunctor("i18n", new I18Nizer())
                 .setOption(WrimeEngine.Scanner.EAT_SPACE, true)
                 .parse(resource);
     }
 
     @Test
     public void callContextFunction() throws WrimeException {
         check("002");
     }
 
     @Test
     public void callUnknownFunctor() throws WrimeException {
        checkError("001", "Expression analyser reports an error: unknown tag, variable or functor 'no_module' (FunctorCallTest/001.txt:1, column 3)");
     }
 }
