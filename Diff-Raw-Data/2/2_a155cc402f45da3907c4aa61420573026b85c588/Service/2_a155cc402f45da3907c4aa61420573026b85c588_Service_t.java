 /*
  * $Id$
  */
 package org.xins.util.service;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Service accessor. This abstract class must be subclasses by specific kinds
  * of service accessors, for example for HTTP, FTP, JDBC, etc.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.115
  */
 public abstract class Service extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Service</code> object.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     */
    protected Service(Descriptor descriptor)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("descriptor", descriptor);
 
       // Set fields
       _descriptor = descriptor;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The descriptor for this service. Cannot be <code>null</code>.
     */
    private final Descriptor _descriptor;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the descriptor.
     *
     * @return
     *    the descriptor for this service, never <code>null</code>.
     */
    public final Descriptor getDescriptor() {
       return _descriptor;
    }
 
    /**
     * Performs a call using the specified subject. Target
     * {@link ServiceDescriptor service descriptors} will be picked and passed
     * to {@link #doCallImpl(ServiceDescriptor,Object)} until there is one that
     * succeeds. If one of the calls succeeds, then the result is returned. If
     * none succeeds, then a {@link CallFailedException} is thrown.
     *
     * <p>Each attempt consists of a call to
    * {@link #doCallImpl(ServiceDescriptor,Object)}.
     *
     * @param subject
     *    the subject passed, could possibly be <code>null</code>.
     *
     * @return
     *    a combination of the call result and a link to the
     *    {@link ServiceDescriptor target} that returned this result, if and
     *    only if one of the calls succeeded, could be <code>null</code>.
     *
     * @throws CallFailedException
     *    if all calls failed.
     */
    protected final CallResult doCall(Object subject)
    throws CallFailedException {
 
       ArrayList failedTargets = null;
       ArrayList exceptions    = null;
 
       // Iterate over all targets
       Iterator iterator = _descriptor.iterateServices();
       while (iterator.hasNext()) {
 
          // Determine the service descriptor target
          ServiceDescriptor target = (ServiceDescriptor) iterator.next();
 
          // Call using this target
          try {
             Object result = doCallImpl(target, subject);
             if (failedTargets != null) {
                failedTargets.trimToSize();
                exceptions.trimToSize();
             }
             return new CallResult(failedTargets, exceptions, target, result);
 
          // If it fails, store the exception and try the next
          } catch (Throwable exception) {
             if (failedTargets == null) {
                failedTargets = new ArrayList();
                exceptions    = new ArrayList();
             }
             failedTargets.add(target);
             exceptions.add(exception);
          }
       }
 
       // Loop ended, all calls failed
       throw new CallFailedException(subject, failedTargets, exceptions);
    }
 
    /**
     * Calls the specified target using the specified subject. This method must
     * be implemented by subclasses. It is called as soon as a target is
     * selected to be called. If the call fails, then an exception should be
     * thrown. If the call succeeds, then the call result should be returned
     * from this method.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @param subject
     *    the subject passed, could possibly be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, could be
     *    <code>null</code>.
     *
     * @throws Throwable
     *    if the call to the specified target failed.
     */
    protected abstract Object doCallImpl(ServiceDescriptor target,
                                         Object            subject)
    throws Throwable;
 }
