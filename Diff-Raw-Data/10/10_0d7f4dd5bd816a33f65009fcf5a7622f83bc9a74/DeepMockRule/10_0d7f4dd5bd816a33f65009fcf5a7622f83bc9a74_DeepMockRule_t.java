 package com.deepmock;
 
 import org.junit.rules.MethodRule;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.Statement;
 
 /**
  * This rule will cause any Annotated Mock objects on your test class to be deeply injected into your annotated Subject.
  * The object graph of your subject will be traversed and any objects that match the type of one of your mocks will be
  * replaced with the mock.  At the end of each test method the object graph will be returned to it's original state.
  *
  * Note: The rule is applied before the Before annotation, so if you are setting up your subject within your Before
  * method this will most likely not work.  Your subject must be instantiated by the time the Rule runs.
  *
  * This was originally developed to work with Spring dependency injection but should work with any object graph.
  *
  * To apply this rule to your test class:
  * <code>
 *     &#64;Rule
  *     DeepMockRule deepMockRule = new DeepMockRule();
  *
 *     &#64;Subject
 *     &#64;Resource // For spring injection
  *     MyService subject;
  *
 *     &#64;Mock
  *     MyDao daoMock;
  * </code>
  * @see org.mockito.Mock
  * @see Subject
  */
 public final class DeepMockRule implements MethodRule {
 
     @Override
     public Statement apply(Statement base, FrameworkMethod method, Object target) {
         return new DeepMockStatement(base, method, target);
     }
 }
