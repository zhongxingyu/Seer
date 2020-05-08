 /*
  * $Id$
  */
 package org.xins.logdoc;
 
 /**
  * Abstract base class for <code>LogdocSerializable</code> implementations,
  * with support for lazy initialization.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.201
  */
 public abstract class AbstractLogdocSerializable
 extends Object
 implements LogdocSerializable {
 
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
     * Constructs a new <code>AbstractLogdocSerializable</code> instance. This
     * constructor is only available for subclasses.
     */
    protected AbstractLogdocSerializable() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The serialized version of this object. The value is <code>null</code> as
     * long as it has not been initialized yet.
     */
    private String _asString;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Serializes this object for logging to the specified string buffer
     * (wrapper method). This method first checks if this object is already
    * initialized. If it is, then {@link #serialize(LogdocStringBuffer)}
     * is immediately called, otherwise {@link #initialize()} is called first,
     * to initialize this object.
     *
     * @param buffer
     *    the {@link LogdocStringBuffer} to serialize to, cannot be
     *    <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>buffer == null</code> (this is checked only <em>after</em>
     *    the optional initialization is performed).
     */
    public final void serialize(LogdocStringBuffer buffer)
    throws NullPointerException {
 
       // Initialize this object if that is not done yet
       if (_asString == null) {
          _asString = initialize();
          if (_asString == null) {
             _asString = "";
          }
       }
 
       buffer.append(_asString);
    }
 
    /**
     * Initializes this object. This method will be called the first time
     * {@link #serialize(LogdocStringBuffer)} is called. It should return the
     * serialized form of this object which will from then on be returned from
     * {@link #serialize(LogdocStringBuffer)}.
     *
     * @return
     *    the serialized form of this object which will from then on be
     *    returned from {@link #serialize(LogdocStringBuffer)}, a
     *    <code>null</code> will be interpreted as an empty string.
     *
     * @since XINS 0.203
     */
    protected abstract String initialize();
 }
