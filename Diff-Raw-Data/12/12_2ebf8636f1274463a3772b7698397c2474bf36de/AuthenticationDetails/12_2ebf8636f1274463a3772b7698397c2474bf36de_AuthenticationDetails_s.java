 /*
  * $Id$
  */
 package org.xins.util.service.ldap;
 
 /**
  * LDAP authentication details. Combines authentication method, principal
  * and credentials.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.115
  */
 public final class AuthenticationDetails
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
     * Constructs a new <code>AuthenticationDetails</code> object.
     *
     * @param method
     *    the authentication method, for example
     *    {@link AuthenticationMethod#NONE}
     *    or {@link AuthenticationMethod#SIMPLE}, cannot be <code>null</code>.
     *
     * @param principal
     *    the principal, cannot be <code>null</code> unless
     *    <code>method == </code>{@link AuthenticationMethod#NONE}.
     *
     * @param credentials
     *    the credentials, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>method == null
     *    || (method != {@link AuthenticationMethod#NONE} &amp;&amp; principal == null)
     *    || (method == {@link AuthenticationMethod#NONE} &amp;&amp; principal != null)
     *    || (method == {@link AuthenticationMethod#NONE} &amp;&amp; credentials != null)</code>.
     */
    public AuthenticationDetails(AuthenticationMethod method,
 				String               principal,
				String               credentials) {
       // Check preconditions
       if (method == null) {
 	 throw new IllegalArgumentException("method == null");
       } else if (method != AuthenticationMethod.NONE && principal == null) {
 	 throw new IllegalArgumentException("method (" + method + ") != AuthenticationMethod.NONE && principal == null");
       } else if (method == AuthenticationMethod.NONE && principal != null) {
 	 throw new IllegalArgumentException("method == AuthenticationMethod.NONE && principal != null");
       } else if (method == AuthenticationMethod.NONE && credentials != null) {
 	 throw new IllegalArgumentException("method == AuthenticationMethod.NONE && credentials != null");
       }
 
       // Set fields
       _method      = method;
       _principal   = principal;
       _credentials = credentials;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The authentication method. Cannot be <code>null</code>.
     */
    private final AuthenticationMethod _method;
 
    /**
     * The principal. Is <code>null</code> if and only if
     * {@link #_method}<code> == </code>{@link AuthenticationMethod#NONE}.
     */
    private final String _principal;
 
    /**
     * The credentials. Can be <code>null</code>. This field is always
     * <code>null</code> if
     * {@link #_method}<code> == </code>{@link AuthenticationMethod#NONE}.
     */
    private final String _credentials;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the authentication method.
     *
     * @return
     *    the authentication method, not <code>null</code>.
     */
    public final AuthenticationMethod getMethod() {
       return _method;
    }
 
    /**
     * Returns the principal. Is <code>null</code> if and only if
     * {@link #getMethod()}<code> == </code>{@link AuthenticationMethod#NONE}.
     *
     * @return
     *    the principal, possibly <code>null</code>.
     */
    public final String getPrincipal() {
       return _principal;
    }
 
    /**
     * The credentials. Can be <code>null</code>. This field is always
     * <code>null</code> if
     * {@link #getMethod()}<code> == </code>{@link AuthenticationMethod#NONE}.
     *
     * @return
     *    the credentials, possibly <code>null</code>.
     */
    public final String getCredentials() {
       return _credentials;
    }
 }
 
