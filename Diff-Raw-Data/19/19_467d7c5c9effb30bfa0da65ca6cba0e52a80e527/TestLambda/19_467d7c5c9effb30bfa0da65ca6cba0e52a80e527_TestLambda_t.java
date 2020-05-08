 package org.wixpress.hoopoe.lambda;
 
 import org.junit.Test;
 
 import java.util.*;
 
 import static org.wixpress.hoopoe.lambda.Lambdas.Lambda;
 import static org.wixpress.hoopoe.lambda.Lambdas.val;
 import static org.wixpress.hoopoe.lambda.Lambdas.var;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 /**
  * @author Yoav
  * @since 10/3/11
  */
 
 public class TestLambda {
 
     @Test
     public void testLambda() {
         Function2<Integer, Integer, Integer> f = Lambda(Integer.class, var(Integer.class, "x"), var(Integer.class, "y")).build("x+y");
         assertThat(f.apply(1, 2), is(3));
     }
 
     @Test
    public void testLambdaWithString() {
        Function1<String, String> f = Lambda(String.class, var(String.class, "y")).build("y + \"abc\"");
        assertThat(f.apply("xxx"), is("xxxabc"));
    }

    @Test
    public void testLambdaReturningBoolean() {
        Function1<Boolean, String> f = Lambda(Boolean.class, var(String.class, "y")).build("y.equals(\"abc\")");
        assertThat(f.apply("abc"), is(true));
    }

    @Test
     public void testLambdaWithBind() {
         Function2<Integer, Integer, Integer> f = Lambda(Integer.class, var(Integer.class, "x"), var(Integer.class, "y")).build("x+y+z", val("z", 12));
         assertThat(f.apply(1, 2), is(15));
     }
 
     @Test
     public void testLambdaDefNames() {
         Function2<Integer, Integer, Integer> f = Lambda(Integer.class, var(Integer.class), var(Integer.class)).build("a+b+c", val(12));
         assertThat(f.apply(1, 2), is(15));
     }
 
     @Test
     public void testLambdaReuseNames() {
 
         LambdaSignature<Function2<Integer, Integer, Integer>> lambdaSignature = Lambda(Integer.class, var(Integer.class), var(Integer.class));
 
         Function2<Integer, Integer, Integer> f = lambdaSignature.build("a+b+r", val("r", 12));
         assertThat(f.apply(1, 2), is(15));
         Function2<Integer, Integer, Integer> f2 = lambdaSignature.build("a+b+c", val(15));
         assertThat(f2.apply(1, 2), is(18));
         Function2<Integer, Integer, Integer> f3 = lambdaSignature.build("a+b");
         assertThat(f3.apply(1, 2), is(3));
 
     }
 
 
 }
