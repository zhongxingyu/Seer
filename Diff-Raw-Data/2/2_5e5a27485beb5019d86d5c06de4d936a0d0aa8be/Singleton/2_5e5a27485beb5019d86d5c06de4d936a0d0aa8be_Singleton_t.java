 /*
  * $Id$
  */
 package org.xins.server;
 
 import org.xins.util.collections.PropertyReader;
 
 /**
  * Interface for singleton classes registered with an API implementation.
  * Implementations must have a public no-argument constructor. The
  * {@link #init(Properties)} method will be called during the initialization
 * of the XINS/Java Server Framework, while {@link #destroy()} will be called
  * during shutdown.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.55
  */
 public interface Singleton {
 
    /**
     * Initializes this instance.
     *
     * @param properties
     *    the initialization properties, can be <code>null</code>.
     *
     * @throws InitializationException
     *    if the initialization failed, for any reason.
     */
    void init(PropertyReader properties)
    throws InitializationException;
 
    /**
     * Deinitializes this instance.
     *
     * @throws Throwable
     *    if the deinitialization fails.
     */
    void destroy() throws Throwable;
 }
