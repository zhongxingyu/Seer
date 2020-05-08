 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.types.Type;
 import org.xins.types.TypeValueException;
 
 /**
  * Session.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.52
  */
 public final class Session
 extends Object {
 
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
     * Constructs a new <code>Session</code> with the specified ID.
     *
     * @param api
     *    the API this session is associated with, cannot be <code>null</code>.
     *
     * @param id
     *    the identifier for this session, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null || id == null</code>.
     */
    public Session(API api, Object id)
    throws IllegalArgumentException {
       MandatoryArgumentChecker.check("api", api, "id", id);
       _sessionIDType = api.getSessionIDType();
       _id  = id;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The session ID type associated with this session, never
     * <code>null</code>.
     */
    private final SessionIDType _sessionIDType;
 
    /**
     * The identifier for this session.
     */
    private final Object _id;
 
    /**
     * Attributes for this session. This map contains {@link String} keys and
     * {@link Object} values.
     *
     * <p>This field is lazily initialized, so it is initially
     * <code>null</code>.
     */
    private Map _attributes;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the session ID type for this session.
     *
     * @return
     *    the session ID type associated with this session, never
     *    <code>null</code>.
     */
    public SessionIDType getSessionIDType() {
       return _sessionIDType;
    }
 
    /**
     * Gets the identifier.
     *
     * @return
     *    the identifier, never <code>null</code>.
     */
    public Object getID() {
       return _id;
    }
 
    /**
     * Gets the identifier, converted to a string.
     *
     * @return
     *    the session ID, converted to a {@link String}, never
     *    <code>null</code>.
     */
    public String getIDString() {
       try {
          return getSessionIDType().toString(_id);
       } catch (TypeValueException exception) {
          String message = "Caught unexpected " + exception.getClass().getName() + '.';
          Library.RUNTIME_LOG.error(message, exception);
          throw new Error(message);
       }
    }
 
    /**
     * Sets or resets the specified attribute. If the specified value is
     * <code>null</code> then the attribute setting will be removed (if it
     * existed at all).
     *
     * @param key
     *    the attribute key, cannot be <code>null</code>.
     *
     * @param value
     *    the attribute value, or <code>null</code> if the attribute should be
     *    reset.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public void setAttribute(String key, Object value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       boolean debugEnabled = Library.RUNTIME_LOG.isDebugEnabled();
 
       // If necessary init the Map and then store the entry
       if (_attributes == null) {
          if (value != null) {
             _attributes = new HashMap(89);
             if (debugEnabled) {
                String s = value instanceof String
                         ? "\"" + value + '"'
                         : value.getClass().getName() + " (\"" + value + "\")";
                Library.RUNTIME_LOG.debug("Setting session attribute \"" + key + "\" to " + s + '.');
             }
             _attributes.put(key, value);
          }
 
       // If the value is null, then remove the entry
       } else if (value == null) {
          if (debugEnabled) {
             Library.RUNTIME_LOG.debug("Resetting session attribute \"" + key + "\".");
          }
          _attributes.remove(key);
          // XXX: Check if the map is now empty and set it to null?
 
       // Otherwise store a new entry
       } else {
          if (debugEnabled) {
             String s = value instanceof String
                      ? "\"" + value + '"'
                      : value.getClass().getName() + " (\"" + value + "\")";
             Library.RUNTIME_LOG.debug("Setting session attribute \"" + key + "\" to " + s + '.');
          }
          _attributes.put(key, value);
       }
    }
 
    /**
    * Gets all attributes and their values.
     *
     * @return
     *    the modifiable map of attributes, or <code>null</code> if there are
     *    no attributes.
     */
    public Map getAttributes() {
 
       // TODO: Return an unmodifiable Map ?
 
       return _attributes;
    }
 
    /**
     * Gets the value of the attribute with the specified key.
     *
     * @param key
     *    the attribute key, cannot be <code>null</code>.
     *
     * @return
     *    the attribute value, or <code>null</code> if the attribute is not
     *    set.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object getAttribute(String key)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       if (_attributes != null) {
          return _attributes.get(key);
       } else {
          return null;
       }
    }
 
    public String toString() {
       return _id.toString();
    }
 }
