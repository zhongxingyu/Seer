 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.xins.util.collections.FastStack;
 
 /**
  * Response validator that just performs some common checks. The following
  * checks are performed:
  *
  * <ul>
  *    <li>No duplicate parameter names
  *    <li>No duplicate attribute names
  * </ul>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.50
  */
 public class BasicResponseValidator
 extends Object
 implements ResponseValidator {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Singleton instance.
     */
    public static final BasicResponseValidator SINGLETON = new BasicResponseValidator();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>BasicResponseValidator</code>.
     */
    protected BasicResponseValidator() {
       _threadLocals = new ThreadLocal();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Thread-local variables.
     *
     * <p>Per thread this {@link ThreadLocal} contains a
     * {@link Object}<code>[3]</code>, if it is already initialized. If so,
     * then the first element is the parameter {@link Map}, the second is the
     * attribute {@link Map} and the third is the element {@link FastStack}.
     * All are initially <code>null</code>.
     */
    private final ThreadLocal _threadLocals;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the 3-size <code>Object[]</code> array for this thread. If there is
     * no array yet, then it is created and stored in {@link #_threadLocals}.
     *
     * @return
     *    the {@link Object}<code>[]</code> array for this thread, never
     *    <code>null</code>.
     */
    private final Object[] getThreadLocals() {
       Object o = _threadLocals.get();
       Object[] arr;
       if (o == null) {
          arr = new Object[3];
          _threadLocals.set(arr);
       } else {
          arr = (Object[]) o;
       }
       return arr;
    }
 
    /**
     * Cleans up the current response.
     */
    private final void reset() {
       Object o = _threadLocals.get();
       if (o != null) {
          Object[] arr = (Object[]) o;
 
          // Clean the parameter map, if any
          o = arr[0];
          if (o != null) {
             ((Map) o).clear();
          }
 
          // Clean the attribute map, if any
          o = arr[1];
          if (o != null) {
             ((Map) o).clear();
          }
 
          // Clean the stack, if any
          o = arr[2];
          if (o != null) {
             ((FastStack) o).clear();
          }
       }
    }
 
    private final void resetAttributes() {
       Object o = _threadLocals.get();
       if (o != null) {
          Object[] arr = (Object[]) o;
          o = arr[1];
          if (o != null) {
             ((Map) o).clear();
          }
       }
    }
 
    /**
     * Gets the parameter <code>Map</code> for the current response. If there
     * is none, then one will be created and stored.
     *
     * @return
     *    the parameter {@link Map}, never <code>null</code>.
     */
    protected final Map getParameters() {
 
       // Get the 3-size Map array
       Object[] arr = getThreadLocals();
 
       // Get the parameter Map
       Object o = arr[0];
       Map parameters;
       if (o == null) {
          parameters = new HashMap();
          arr[0] = parameters;
       } else {
          parameters = (Map) o;
       }
 
       return parameters;
    }
 
    /**
     * Gets the attributes <code>Map</code> for the current element. If there
     * is none, then one will be created and stored.
     *
     * @return
     *    the attributes {@link Map}, never <code>null</code>.
     */
    protected final Map getAttributes() {
 
       // Get the 3-size Map array
       Object[] arr = getThreadLocals();
 
       // Get the attributes Map
       Object o = arr[1];
       Map attributes;
       if (o == null) {
          attributes = new HashMap();
          arr[1] = attributes;
       } else {
          attributes = (Map) o;
       }
 
       return attributes;
    }
 
    /**
     * Gets the element stack for the current response. If there is none, then
     * one will be created and stored.
     *
     * @return
     *    the {@link FastStack} with the element names, never
     *    <code>null</code>.
     */
    protected final FastStack getElements() {
 
       // Get the 3-size Map array
       Object[] arr = getThreadLocals();
 
       // Get the stack of elements
       Object o = arr[2];
       FastStack elements;
       if (o == null) {
          elements = new FastStack(3);
          arr[2] = elements;
       } else {
          elements = (FastStack) arr[2];
       }
 
       return elements;
    }
 
    /**
     * Fails with an <code>InvalidResponseException</code> after cleaning up.
     * Subclasses should use this method if they find that the response is
     * invalid.
     *
     * @param message
     *    the message, can be <code>null</code>.
     *
     * @throws InvalidResponseException
     *    always thrown, right after cleanup is performed.
     */
    protected final void fail(String message)
    throws InvalidResponseException {
       reset();
       throw new InvalidResponseException(message);
    }
 
    public final void startResponse(boolean success, String code)
    throws InvalidResponseException {
 
       // Reset in case endResponse() or cancelResponse() were not called
       reset();
 
       boolean succeeded = false;
       try {
          startResponseImpl(success, code);
          succeeded = true;
       } finally {
          // If an exception is thrown, then reset, just in case the subclass
          // threw something other than an InvalidResponseException
          if (succeeded == false) {
             reset();
          }
       }
    }
 
    protected void startResponseImpl(boolean success, String code)
    throws InvalidResponseException {
       // empty
    }
 
    public final void param(String name, String value)
    throws InvalidResponseException {
       Map parameters = getParameters();
       Object o = parameters.get(name);
       if (o != null) {
          fail("Duplicate parameter named \"" + name + "\".");
       }
       boolean succeeded = false;
       try {
          paramImpl(name, value);
          succeeded = true;
       } finally {
          // If an exception is thrown, then reset, just in case the subclass
          // threw something other than an InvalidResponseException
          if (succeeded == false) {
             reset();
          }
       }
       parameters.put(name, value);
    }
 
    protected void paramImpl(String name, String value)
    throws InvalidResponseException {
       // empty
    }
 
    public final void startTag(String name)
    throws InvalidResponseException {
       resetAttributes();
       boolean succeeded = false;
       try {
          startTagImpl(name);
          succeeded = true;
       } finally {
          // If an exception is thrown, then reset, just in case the subclass
          // threw something other than an InvalidResponseException
          if (succeeded == false) {
             reset();
          }
       }
       getElements().push(name);
    }
 
    protected void startTagImpl(String name)
    throws InvalidResponseException {
       // empty
    }
 
    public final void attribute(String name, String value)
    throws InvalidResponseException {
       Map attributes = getAttributes();
       if (attributes.containsKey(name)) {
          String element = (String) getElements().peek();
          fail("Duplicate attribute named \"" + name + "\" for element named \"" + element + "\".");
       }
       attributes.put(name, value);
    }
 
    public final void pcdata(String text)
    throws InvalidResponseException {
       // empty
    }
 
    public final void endTag()
    throws InvalidResponseException {
       boolean succeeded = false;
       try {
         endTagImpl(name);
          succeeded = true;
       } finally {
          // If an exception is thrown, then reset, just in case the subclass
          // threw something other than an InvalidResponseException
          if (succeeded == false) {
             reset();
          }
       }
       getElements().pop();
    }
 
    protected void endTagImpl()
    throws InvalidResponseException {
       // empty
    }
 
    public final void endResponse()
    throws InvalidResponseException {
       try {
          endResponseImpl();
       } finally {
          reset();
       }
    }
 
    protected void endResponseImpl()
    throws InvalidResponseException {
       // empty
    }
 
    public final void cancelResponse() {
       try {
          cancelResponseImpl();
       } finally {
          reset();
       }
    }
 
    protected void cancelResponseImpl() {
       // empty
    }
 }
