 package au.com.sensis.mobile.web.component.core.aop;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 import org.aspectj.lang.ProceedingJoinPoint;
 
 import au.com.sensis.wireless.web.common.exception.ApplicationRuntimeException;
 import au.com.sensis.wireless.web.common.validation.Validatable;
 import au.com.sensis.wireless.web.common.validation.ValidatableUtils;
 
 /**
  * Abstract base class intended for AOP Aspects that need to log the entry and
  * exit into and out of particular methods. Subclasses are required to take the
  * following steps when extending this class:
  * <ol>
  * <li>Annotate your class with {@link org.aspectj.lang.annotation.Aspect}
  * <li>Define a {@link org.aspectj.lang.annotation.Pointcut} annotated method.
  * See Spring documentation.</li>
  * <li>Annotate a method with {@link org.aspectj.lang.annotation.Around},
  * referencing the pointcut. The implementation should simple delegate to
  * {@link #doHandleLog4jNDC(ProceedingJoinPoint)}</li>
  * <li>Inject the {@link #setNdcMessage(String)} property at runtime.</li>
  * </ol>
  * <p>
  * It was originally intended for subclasses to only have to override an
  * abstract pointcut. However, this did not seem to work with Spring's AOP,
  * although native aspectj would support it. Hence, the above requirements for
  * subclasses.
  * </p>
  *
  * @author Adrian.Koh2@sensis.com.au
  *
  */
 public abstract class AbstractEntryAndExitLoggingAspect implements Validatable {
 
     /**
      * Message to be pushed onto the log4j {@link NDC} before the join point is
      * proceeded to and popped off the NDC afterwards.
      */
     private String ndcMessage;
 
     /**
      * See {@link AbstractEntryAndExitLoggingAspect}.
      *
      * @param proceedingJoinPoint
      *            {@link ProceedingJoinPoint} representing the join point being
      *            advised.
      * @return Object returned from the join point.
      * @throws Throwable
      *             Thrown if any error occurs.
      * @see AbstractEntryAndExitLoggingAspect
      */
     protected final Object doHandleLog4jNDC(
             final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
 
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Pushing NDC ...");
         }
 
         NDC.push(getNdcMessage());
 
         try {
             logJoinPointEntryIfInfoLoggingEnabled(proceedingJoinPoint);
 
            final Object proceedingJoinPointReturnValue = proceedingJoinPoint.proceed();
 
            logJoinPointExitIfInfoLoggingEnabled(proceedingJoinPoint);

            return proceedingJoinPointReturnValue;
         } finally {
 
 
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Popping NDC ...");
             }
 
             NDC.pop();
         }
     }
 
     /**
      * @param proceedingJoinPoint
      */
     private void logJoinPointEntryIfInfoLoggingEnabled(
             final ProceedingJoinPoint proceedingJoinPoint) {
         if (getLogger().isInfoEnabled()) {
 
             getLogger().info("Entering: "
                     + createDescription(proceedingJoinPoint));
 
         }
 
     }
 
     /**
      * @param proceedingJoinPoint
      */
     private void logJoinPointExitIfInfoLoggingEnabled(
             final ProceedingJoinPoint proceedingJoinPoint) {
         if (getLogger().isInfoEnabled()) {
             getLogger().info("Exiting: "
                     + createDescription(proceedingJoinPoint));
         }
     }
 
 
     private String createDescription(
             final ProceedingJoinPoint proceedingJoinPoint) {
         final StringBuilder description = createTypeAndMethodDescription(proceedingJoinPoint);
 
         description.append("(");
 
         if (proceedingJoinPoint.getArgs() != null) {
             for (int i = 0; i < proceedingJoinPoint.getArgs().length; i++) {
                 if (proceedingJoinPoint.getArgs()[i] != null) {
                     description.append(proceedingJoinPoint.getArgs()[i]
                             .getClass().getName());
                 } else {
                     description.append("null");
                 }
                 if (i < proceedingJoinPoint.getArgs().length - 1) {
                     description.append(", ");
                 }
             }
         }
         description.append(")");
 
         return description.toString();
     }
 
     private StringBuilder createTypeAndMethodDescription(
             final ProceedingJoinPoint proceedingJoinPoint) {
         final StringBuilder description;
         if (proceedingJoinPoint.getSignature() != null) {
             description =
                     new StringBuilder(proceedingJoinPoint.getSignature()
                             .getDeclaringTypeName()
                             + "."
                             + proceedingJoinPoint.getSignature().getName());
         } else {
             description = new StringBuilder("<unknown type>.<unknown method>");
 
             if (getLogger().isEnabledFor(Level.WARN)) {
                 getLogger().warn(
                     "JoinPoint Signature was null so type and method invoked is unknown. "
                         + "This shouldn't happen !!! Is Spring doing something wrong?");
             }
 
         }
 
         return description;
     }
 
     /**
      * Message to be pushed onto the log4j {@link NDC} before the join point is
      * proceeded to and popped off the NDC afterwards.
      *
      * @return Message to be pushed onto the log4j {@link NDC} before the join
      *         point is proceeded to and popped off the NDC afterwards.
      */
     public final String getNdcMessage() {
         return ndcMessage;
     }
 
     /**
      * Message to be pushed onto the log4j {@link NDC} before the join point is
      * proceeded to and popped off the NDC afterwards.
      *
      * @param ndcMessage
      *            Message to be pushed onto the log4j {@link NDC} before the
      *            join point is proceeded to and popped off the NDC afterwards.
      */
     public final void setNdcMessage(final String ndcMessage) {
         this.ndcMessage = ndcMessage;
     }
 
     /**
      * {@inheritDoc}
      */
     public void validateState() throws ApplicationRuntimeException {
         ValidatableUtils
                 .validateStringIsNotBlank(getNdcMessage(), "ndcMessage");
     }
 
     /**
      * @return the logger
      */
     protected abstract Logger getLogger();
 
 }
