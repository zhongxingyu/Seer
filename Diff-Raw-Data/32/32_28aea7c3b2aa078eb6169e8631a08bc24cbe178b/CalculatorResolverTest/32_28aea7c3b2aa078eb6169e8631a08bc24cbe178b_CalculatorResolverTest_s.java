 package net.ggtools.codestory;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * User: Christophe Labouisse
  * Date: 17/01/13
  * Time: 17:24
  */
 @RunWith(MockitoJUnitRunner.class)
 public class CalculatorResolverTest {
 
     @Mock
     private HttpServletRequest request;
 
     private CalculatorResolver resolver = new CalculatorResolver();
 
     @Test
     public void testSolve() throws Exception {
 //        ((1.1+2)+3.14+4+(5+6+7)+(8+9+10)*4267387833344334647677634)/2*553344300034334349999000;

         Mockito.when(request.getParameter(Matchers.eq("q"))).
                thenReturn("((1,1+2)+3,14+4+(5+6+7)+(8+9+10)*4267387833344334647677634)/2*553344300034334349999000");
 
         String answer = resolver.solve(request);
        System.out.println(answer);
     }
 }
