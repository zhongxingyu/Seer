 package org.nohope.test.runner;
 
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 import org.junit.runners.model.Statement;
 
 /**
  * Simple {@link org.junit.runner.Runner JUnit runner} which allows to skip
 * test if particular exception type(s) was thrown by test method.
  *
  * Usage:
  * <pre>
  *  &#064;RunWith({@link ExpectedExceptionSkippingRunner NameAwareRunner.class})
  *  public class MyDatabaseTest {
  *
  *      &#064;Test
  *      &#064;{@link SkipOnException SkipOnException}(ConnectionException.class)
  *      public void testDBConnection() {
  *          ...
  *      }
  *  }
  * </pre>
  *
  * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
  * @since 18/01/11 05:40 PM
  */
 public final class ExpectedExceptionSkippingRunner extends BlockJUnit4ClassRunner {
     /**
      * Runner constructor.
      *
      * @param clazz test class
      * @throws InitializationError on runner initialization error
      */
     public ExpectedExceptionSkippingRunner(final Class<?> clazz) throws InitializationError {
         super(clazz);
     }
 
     @Override
     protected Statement methodBlock(final FrameworkMethod method) {
          return new SkipStatement(super.methodBlock(method),
                  method.getAnnotation(SkipOnException.class));
     }
 }
