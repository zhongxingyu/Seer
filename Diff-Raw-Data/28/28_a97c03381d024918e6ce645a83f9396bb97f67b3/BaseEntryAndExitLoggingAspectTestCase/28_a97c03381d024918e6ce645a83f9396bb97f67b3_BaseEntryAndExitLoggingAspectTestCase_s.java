 package au.com.sensis.mobile.web.component.core.aop;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.Signature;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.aspectj.lang.reflect.SourceLocation;
 import org.aspectj.runtime.internal.AroundClosure;
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.com.sensis.wireless.test.AbstractJUnit4TestCase;
 import au.com.sensis.wireless.web.common.validation.ValidatableTestUtils;
 
 /**
  * Unit test {@link AbstractEntryAndExitLoggingAspect} and also serve as a base
  * test case class for all classes that test
  * {@link AbstractEntryAndExitLoggingAspect} subclasses. Subclasses should do
  * the following:
  * <ol>
  * <li>Override {@link #getObjectUnderTest()} and
  * {@link #createObjectUnderTest()}</li>
  * <li>Implement a similar tet to {@link #testDoHandleLog4jNDC()}.</li>
  * </ol>
  * We cannot have this base class implement a template method pattern due to the
  * limitations of the {@link AbstractEntryAndExitLoggingAspect} implementation.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class BaseEntryAndExitLoggingAspectTestCase extends
         AbstractJUnit4TestCase {
 
     /**
      * Expected value that the proceed call should return.
      */
     protected static final String EXPECTED_PROCEED_RETURN_VALUE =
             "expected proceed() return value";
 
     private static final Integer EXPECTED_JOIN_POINT_ARG3 = new Integer(1);
 
     private static final String EXPECTED_JOIN_POINT_ARG1 = "arg1";
 
     private static final String EXPECTED_NDC_MESSAGE = "my ndc message";
 
     private static final String EXPECTED_DECLARING_TYPE_NAME = "some.package.SomeClass";
 
     private static final String EXPECTED_NAME = "myMethod";
 
     private static final Object EXPECTED_JOIN_POINT_ARG2 = null;
 
     private AbstractEntryAndExitLoggingAspect objectUnderTest;
 
     private ProceedingJoinPointStub proceedingJoinPointStub;
     private Signature mockSignature;
 
     /**
      * @return the objectUnderTest
      */
     protected AbstractEntryAndExitLoggingAspect getObjectUnderTest() {
         return objectUnderTest;
     }
 
     protected AbstractEntryAndExitLoggingAspect createObjectUnderTest() {
         return new EntryAndExitLoggingAspectStub();
     }
 
     /**
      * Setup test data.
      *
      * @throws Exception
      *             Thrown if any error occurs.
      */
     @Before
     public void setUp() throws Exception {
         objectUnderTest = createObjectUnderTest();
         objectUnderTest.setNdcMessage(EXPECTED_NDC_MESSAGE);
         setProceedingJoinPointStub(
                 new ProceedingJoinPointStub(getMockSignature(), new Object[] {
                         EXPECTED_JOIN_POINT_ARG1, EXPECTED_JOIN_POINT_ARG2,
                         EXPECTED_JOIN_POINT_ARG3 },
                         EXPECTED_PROCEED_RETURN_VALUE));
 
         swapOutRealLoggerForMock(getObjectUnderTest().getClass());
     }
 
     @Test
     public final void testValidateStateWhenValid() throws Throwable {
         getObjectUnderTest().validateState();
     }
 
     @Test
     public final void testValidateState() throws Throwable {
         final String[] testValues = { null, StringUtils.EMPTY, " ", "  " };
         for (final String testValue : testValues) {
             getObjectUnderTest().setNdcMessage(testValue);
             ValidatableTestUtils.testValidateStateWithCustomFailureMessage(
                     "Falure for testValue: '" + testValue + "'",
                     getObjectUnderTest(), "ndcMessage");
         }
 
     }
 
     @Test
     public final void testDoHandleLog4jNDC() throws Throwable {
         if (!EntryAndExitLoggingAspectStub.class.equals(getObjectUnderTest().getClass())) {
             swapOutRealLoggerForMock(EntryAndExitLoggingAspectStub.class);
         }
         objectUnderTest = new EntryAndExitLoggingAspectStub();
         objectUnderTest.setNdcMessage(EXPECTED_NDC_MESSAGE);
 
         setupForDoHandleLog4jNDC();
 
         replay();
 
         getObjectUnderTest().doHandleLog4jNDC(getProceedingJoinPointStub());
 
         assertForDoHandleLog4jNDC();
 
     }
 
     @Test
     public final void testDoHandleLog4jNDCWhenSignatureNull() throws Throwable {
         if (!EntryAndExitLoggingAspectStub.class.equals(getObjectUnderTest().getClass())) {
             swapOutRealLoggerForMock(EntryAndExitLoggingAspectStub.class);
         }
 
         objectUnderTest = new EntryAndExitLoggingAspectStub();
         objectUnderTest.setNdcMessage(EXPECTED_NDC_MESSAGE);
 
         setProceedingJoinPointStub(
                 new ProceedingJoinPointStub(null, new Object[] {
                         EXPECTED_JOIN_POINT_ARG1, EXPECTED_JOIN_POINT_ARG2,
                         EXPECTED_JOIN_POINT_ARG3 },
                         EXPECTED_PROCEED_RETURN_VALUE));
 
         setupForDoHandleLog4jNDCWhenSignatureNull();
 
         replay();
 
         getObjectUnderTest().doHandleLog4jNDC(getProceedingJoinPointStub());
 
         assertForDoHandleLog4jNDC();
 
     }
 
     @Test
     public final void testDoHandleLog4jNDCWhenArgsNull() throws Throwable {
         if (!EntryAndExitLoggingAspectStub.class.equals(getObjectUnderTest().getClass())) {
             swapOutRealLoggerForMock(EntryAndExitLoggingAspectStub.class);
         }
 
         objectUnderTest = new EntryAndExitLoggingAspectStub();
         objectUnderTest.setNdcMessage(EXPECTED_NDC_MESSAGE);
 
         setProceedingJoinPointStub(
                 new ProceedingJoinPointStub(getMockSignature(), null,
                         EXPECTED_PROCEED_RETURN_VALUE));
 
         setupForDoHandleLog4jNDCWhenArgsNull();
 
         replay();
 
         getObjectUnderTest().doHandleLog4jNDC(getProceedingJoinPointStub());
 
         assertForDoHandleLog4jNDC();
 
     }
 
     @Test
     public final void testDoHandleLog4jNDCWhenProceedThrowsException()
             throws Throwable {
         if (!EntryAndExitLoggingAspectStub.class.equals(getObjectUnderTest().getClass())) {
             swapOutRealLoggerForMock(EntryAndExitLoggingAspectStub.class);
         }
 
         objectUnderTest = new EntryAndExitLoggingAspectStub();
         objectUnderTest.setNdcMessage(EXPECTED_NDC_MESSAGE);
 
         final RuntimeException expectedException = new RuntimeException("test");
         setProceedingJoinPointStub(
                 new ExceptionThrowingProceedingJoinPointStub(
                         getMockSignature(), new Object[] {
                                 EXPECTED_JOIN_POINT_ARG1,
                                 EXPECTED_JOIN_POINT_ARG2,
                                 EXPECTED_JOIN_POINT_ARG3 },
                         EXPECTED_PROCEED_RETURN_VALUE, expectedException));
 
        setupForDoHandleLog4jNDC();
 
         replay();
 
         try {
             getObjectUnderTest().doHandleLog4jNDC(getProceedingJoinPointStub());
             Assert.fail("RuntimeException expected");
         } catch (final RuntimeException e) {
             Assert.assertSame("RuntimeException is wrong instance", expectedException, e);
             assertForDoHandleLog4jNDC();
         }
     }
 
     protected final Logger getMockLogger() {
         return getMockLogger(getObjectUnderTest().getClass());
     }
 
     protected final void setupForDoHandleLog4jNDC() throws Throwable {
         EasyMock.expect(getMockLogger().isInfoEnabled())
                 .andReturn(Boolean.TRUE);
         getMockLogger().info(
                 "Entering: " + expectedJoinPointDescription());
 
         recordGetSignatureDetails();
 
         EasyMock.expect(getMockLogger().isInfoEnabled())
                 .andReturn(Boolean.TRUE);
         getMockLogger().info(
                 "Exiting: " + expectedJoinPointDescription());
 
     }
 
     protected final void setupForDoHandleLog4jNDCWhenSignatureNull()
             throws Throwable {
         EasyMock.expect(getMockLogger().isInfoEnabled())
                 .andReturn(Boolean.TRUE);
         getMockLogger().info(
                 "Entering: "
                     + expectedJoinPointDescriptionWhenSignatureNull());
 
         EasyMock.expect(getMockLogger().isEnabledFor(Level.WARN)).andReturn(
                 Boolean.TRUE).times(2);
         getMockLogger().warn(
                 "JoinPoint Signature was null so type and method invoked is unknown. "
                         + "This shouldn't happen !!! Is Spring doing something wrong?");
         EasyMock.expectLastCall().times(2);
 
         EasyMock.expect(getMockLogger().isInfoEnabled())
                 .andReturn(Boolean.TRUE);
         getMockLogger().info(
                 "Exiting: "
                     + expectedJoinPointDescriptionWhenSignatureNull());
 
     }
 
     protected final void setupForDoHandleLog4jNDCWhenArgsNull() throws Throwable {
         EasyMock.expect(getMockLogger().isInfoEnabled())
                 .andReturn(Boolean.TRUE);
         getMockLogger().info(
                 "Entering: " + expectedJoinPointDescriptionWhenArgsNull());
 
         recordGetSignatureDetails();
 
         EasyMock.expect(getMockLogger().isInfoEnabled())
                 .andReturn(Boolean.TRUE);
         getMockLogger().info(
                 "Exiting: " + expectedJoinPointDescriptionWhenArgsNull());
 
     }
 
     protected final void assertForDoHandleLog4jNDC() throws Throwable {
         Assert.assertEquals("NDC has wrong value during join point proceed",
                 EXPECTED_NDC_MESSAGE, getProceedingJoinPointStub()
                         .getActualNDCValue());
     }
 
     private String expectedJoinPointDescription() {
         return EXPECTED_DECLARING_TYPE_NAME + "." + EXPECTED_NAME + "("
                 + EXPECTED_JOIN_POINT_ARG1.getClass().getName() + ", " + EXPECTED_JOIN_POINT_ARG2
                 + ", " + EXPECTED_JOIN_POINT_ARG3.getClass().getName() + ")";
     }
 
     private String expectedJoinPointDescriptionWhenSignatureNull() {
         return "<unknown type>.<unknown method>(" + EXPECTED_JOIN_POINT_ARG1.getClass().getName()
                 + ", " + EXPECTED_JOIN_POINT_ARG2 + ", "
                 + EXPECTED_JOIN_POINT_ARG3.getClass().getName() + ")";
     }
 
     private String expectedJoinPointDescriptionWhenArgsNull() {
         return EXPECTED_DECLARING_TYPE_NAME + "." + EXPECTED_NAME + "()";
     }
 
     private void recordGetSignatureDetails() {
         EasyMock.expect(getMockSignature().getDeclaringTypeName()).andReturn(
                 EXPECTED_DECLARING_TYPE_NAME).atLeastOnce();
         EasyMock.expect(getMockSignature().getName()).andReturn(EXPECTED_NAME)
                 .atLeastOnce();
     }
 
     /**
      * @return the mockSignature
      */
     public Signature getMockSignature() {
         return mockSignature;
     }
 
     /**
      * @param mockSignature
      *            the mockSignature to set
      */
     public void setMockSignature(final Signature mockSignature) {
         this.mockSignature = mockSignature;
     }
 
     @Aspect
     private static final class EntryAndExitLoggingAspectStub extends
             AbstractEntryAndExitLoggingAspect {
 
         private static Logger logger = Logger.getLogger(EntryAndExitLoggingAspectStub.class);
 
         /**
          * AOP Pointcut matching all delegates in the current component.
          */
         @Pointcut("bean(map.*Delegate)")
         protected void mapDelegate() {
         }
 
         /**
          * AOP Around advice that delegates to
          * {@link #doHandleLog4jNDC(ProceedingJoinPoint)}.
          *
          * @param proceedingJoinPoint
          *            {@link ProceedingJoinPoint} representing the join point
          *            being advised.
          * @return Object returned from the join point.
          * @throws Throwable
          *             Thrown if any error occurs.
          * @see {@link #doHandleLog4jNDC(ProceedingJoinPoint)}
          */
         @Around("mapDelegate()")
         public Object handleLog4jNDC(
                 final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
             return super.doHandleLog4jNDC(proceedingJoinPoint);
         }
 
         @Override
         protected Logger getLogger() {
             return logger;
         }
 
     }
 
     /**
      * Stub implementation of {@link ProceedingJoinPoint}.
      *
      * @author Adrian.Koh2@sensis.com.au
      */
     private static class ProceedingJoinPointStub implements
             ProceedingJoinPoint {
 
         private final Signature signature;
         private final Object[] args;
         private final Object proceedReturnValue;
         private String actualNDCValue;
 
         protected ProceedingJoinPointStub(final Signature signature,
                 final Object[] args, final Object proceedReturnValue) {
             super();
             this.signature = signature;
             this.args = args;
             this.proceedReturnValue = proceedReturnValue;
         }
 
         public Object proceed() throws Throwable {
             actualNDCValue = NDC.peek();
             return proceedReturnValue;
         }
 
         public Object proceed(final Object[] arg0) throws Throwable {
             // TODO Auto-generated method stub
             return null;
         }
 
         public void set$AroundClosure(final AroundClosure arg0) {
             // TODO Auto-generated method stub
 
         }
 
         public Object[] getArgs() {
             return args;
         }
 
         public String getKind() {
             // TODO Auto-generated method stub
             return null;
         }
 
         public Signature getSignature() {
             return signature;
         }
 
         public SourceLocation getSourceLocation() {
             // TODO Auto-generated method stub
             return null;
         }
 
         public StaticPart getStaticPart() {
             // TODO Auto-generated method stub
             return null;
         }
 
         public Object getTarget() {
             // TODO Auto-generated method stub
             return null;
         }
 
         public Object getThis() {
             // TODO Auto-generated method stub
             return null;
         }
 
         public String toLongString() {
             // TODO Auto-generated method stub
             return null;
         }
 
         public String toShortString() {
             // TODO Auto-generated method stub
             return null;
         }
 
         /**
          * @return the actualNDCValue
          */
         private String getActualNDCValue() {
             return actualNDCValue;
         }
 
     }
 
     private static final class ExceptionThrowingProceedingJoinPointStub extends
         ProceedingJoinPointStub {
 
         private final Throwable proceedThrownException;
 
         protected ExceptionThrowingProceedingJoinPointStub(
                 final Signature signature, final Object[] args,
                 final Object proceedReturnValue,
                 final Throwable proceedThrownException) {
             super(signature, args, proceedReturnValue);
             this.proceedThrownException = proceedThrownException;
         }
 
         @Override
         public Object proceed() throws Throwable {
             super.proceed();
             throw proceedThrownException;
         }
     }
 
 
     /**
      * @return the proceedingJoinPointStub
      */
     public ProceedingJoinPointStub getProceedingJoinPointStub() {
         return proceedingJoinPointStub;
     }
 
     /**
      * @param proceedingJoinPointStub
      *            the proceedingJoinPointStub to set
      */
     public void setProceedingJoinPointStub(
             final ProceedingJoinPointStub proceedingJoinPointStub) {
         this.proceedingJoinPointStub = proceedingJoinPointStub;
     }
 }
