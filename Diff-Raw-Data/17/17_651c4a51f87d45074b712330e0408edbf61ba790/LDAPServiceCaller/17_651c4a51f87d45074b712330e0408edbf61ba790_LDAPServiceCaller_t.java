 /*
  * $Id$
  */
 package org.xins.common.service.ldap;
 
 import java.util.Hashtable;
 import javax.naming.AuthenticationException;
 import javax.naming.Context;
import javax.naming.NameNotFoundException;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.service.CallFailedException;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.TargetDescriptor;
 
 /**
  * LDAP service caller.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.115
  */
 public final class LDAPServiceCaller extends ServiceCaller {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Authentication details to be used when none are specified.
     */
    private static final AuthenticationDetails FALLBACK_AUTHENTICATION_DETAILS = new AuthenticationDetails(AuthenticationMethod.NONE, null, null);
 
    /**
     * The initial context factory.
     */
    private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>LDAPServiceCaller</code> object.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     */
    public LDAPServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException {
       super(descriptor);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Authenticates using the specified details. If this succeeds with one of
     * the targets, then a {@link QueryResult} is returned. Otherwise, if
     * none of the targets could successfully be called, a
     * {@link CallFailedException} is thrown.
     *
     * @param method
     *    the authentication method, for example
     *    {@link AuthenticationMethod#NONE} or
     *    {@link AuthenticationMethod#SIMPLE}, cannot be <code>null</code>.
     *
     * @param principal
     *    the principal, cannot be <code>null</code> unless
     *    <code>method == </code>{@link AuthenticationMethod#NONE}.
     *
     * @param credentials
     *    the credentials, can be <code>null</code>.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>method == null
     *    || (method != {@link AuthenticationMethod#NONE} &amp;&amp; principal == null)
     *    || (method == {@link AuthenticationMethod#NONE} &amp;&amp; principal != null)
     *    || (method == {@link AuthenticationMethod#NONE} &amp;&amp; credentials != null)</code>.
     *
     * @throws CallFailedException
     *    if the call failed.
     */
    public QueryResult call(AuthenticationMethod method,
                       String               principal,
                       String               credentials)
    throws IllegalArgumentException, CallFailedException {
       return call(new AuthenticationDetails(method, principal, credentials),
                   null);
    }
 
    /**
     * Performs the specified LDAP query using with no authentication details.
     * If this succeeds with one of the targets, then a
     * {@link QueryResult} is returned. Otherwise, if none of the targets
     * could successfully be called, a {@link CallFailedException} is thrown.
     *
     * @param query
     *    the query to be executed, may be <code>null</code>.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>method == null
     *    || (method != {@link AuthenticationMethod#NONE} &amp;&amp; principal == null)
     *    || (method == {@link AuthenticationMethod#NONE} &amp;&amp; principal != null)
     *    || (method == {@link AuthenticationMethod#NONE} &amp;&amp; credentials != null)</code>.
     *
     * @throws CallFailedException
     *    if the call failed.
     */
    public QueryResult call(Query query)
    throws IllegalArgumentException, CallFailedException {
       return call(null, query);
    }
 
    /**
     * Performs the specified LDAP query using the specified authentication
     * details. If this succeeds with one of the targets, then a
     * {@link QueryResult} is returned. Otherwise, if none of the targets
     * could successfully be called, a {@link CallFailedException} is thrown.
     *
     * @param authenticationDetails
     *    the authentication details, or <code>null</code> if no authentication
     *    details should be sent down.
     *
     * @param query
     *    the query to execute, or <code>null</code> if only a connection
     *    should be made.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws CallFailedException
     *    if the call failed.
     */
    public QueryResult call(AuthenticationDetails authenticationDetails,
                                  Query                 query)
    throws CallFailedException {
 
       // Construct a Request object
       Request request = new Request(authenticationDetails, query);
 
       // Perform the call
       CallResult callResult = doCall(request);
 
       // Return the result
       return (QueryResult) callResult.getResult();
    }
 
    protected Object doCallImpl(TargetDescriptor target,
                                Object            subject)
    throws Throwable {
 
       // Convert subject to a Request object
       Request request = (Request) subject;
 
       // Authenticate and connect
       AuthenticationDetails authenticationDetails = request.getAuthenticationDetails();
       if (authenticationDetails == null) {
          authenticationDetails = FALLBACK_AUTHENTICATION_DETAILS;
       }
       InitialDirContext context;
       try {
          context = authenticate(target,  authenticationDetails);
      } catch (NameNotFoundException exception) {
         return new QueryResult(false, null);
       } catch (AuthenticationException exception) {
          return new QueryResult(false, null);
       }
 
       // Perform a query if applicable
       try {
          Query query = request.getQuery();
          if (query != null) {
             return query(target, context, query);
          } else {
             return new QueryResult(true, null);
          }
 
       // Always close the context
       } finally {
          context.close();
       }
    }
 
    protected final String reasonForImpl(Throwable exception) {
       if (exception instanceof NamingException) {
          NamingException ne = (NamingException) exception;
          Throwable rootCause = ne.getRootCause();
          if (rootCause != null) {
             return reasonFor(rootCause);
          }
       }
       return null;
    }
 
    /**
     * Attempts to authenticate with a target service, using the specified
     * authentication details.
     *
     * @param target
     *    descriptor of the target service to connect to, cannot be
     *    <code>null</code>.
     *
     * @param authenticationDetails
     *    the authentication details, cannot be <code>null</code>.
     *
     * @return
     *    the directory context, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || authenticationDetails == null</code>.
     *
     * @throws NamingException
     *    if the connection failed.
     */
    private InitialDirContext authenticate(TargetDescriptor      target,
                                           AuthenticationDetails authenticationDetails)
    throws IllegalArgumentException, NamingException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("target",                target,
                                      "authenticationDetails", authenticationDetails);
 
       // Determine what location to connect to
       String url = target.getURL();
 
       // Initialize connection settings
       Hashtable env = new Hashtable();
       env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY); // TODO: Allow configuration of initial context factory ?
       env.put(Context.PROVIDER_URL,            url);
       env.put(Context.SECURITY_AUTHENTICATION, authenticationDetails.getMethod().getName());
 
       // Only add principal if given
       String principal = authenticationDetails.getPrincipal();
       if (principal != null) {
          env.put(Context.SECURITY_PRINCIPAL, principal);
       }
 
       // Only add credentials if given
       String credentials = authenticationDetails.getCredentials();
       if (credentials != null) {
          env.put(Context.SECURITY_CREDENTIALS, credentials);
       }
 
       // Connect
       // TODO: Connection time-out
       InitialDirContext context = null;
       try {
          context = new InitialDirContext(env);
          return context;
       } finally {
          if (context != null) {
             if (principal == null) {
                Log.log_3305(url);
             } else {
                Log.log_3306(url, principal);
             }
          } else {
             if (principal == null) {
                Log.log_3307(url);
             } else {
                Log.log_3308(url, principal);
             }
          }
       }
    }
 
    /**
     * Performs the specified query.
     *
     * @param target
     *    the target service, cannot be <code>null</code>.
     *
     * @param context
     *    the directory context for the query, cannot be <code>null</code>.
     *
     * @param query
     *    the query to execute, cannot be <code>null</code>.
     *
     * @return
     *    the results of the query, or <code>null</code> if there are no results.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || context == null || query == null</code>.
     *
     * @throws NamingException
     *    if the search failed.
     */
    private QueryResult query(TargetDescriptor  target,
                              InitialDirContext context,
                              Query             query)
    throws IllegalArgumentException, NamingException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("target",  target,
                                      "context", context,
                                      "query",   query);
 
       // Get search base and filter
       String searchBase = query.getSearchBase();
       String filter     = query.getFilter();
 
       // Create SearchControls object
       // TODO: Allow configuration of maximum
       SearchControls searchControls = new SearchControls(
          SearchControls.SUBTREE_SCOPE, // scope
          0L,                           // return all entries that match, no maximum
          target.getTimeOut(),          // time-out (in ms) or 0 if unlimited
          query._attributes,            // IDs of attributres to return
          false,                        // do not return named objects
          false                         // do not dereference links
       );
 
       // Perform the search
       NamingEnumeration ne = null;
       boolean succeeded = false;
       try {
          ne = context.search(searchBase, filter, searchControls);
          succeeded = true;
      } catch (NameNotFoundException exception) {
         return new QueryResult(false, null);
       } finally {
          if (!succeeded) {
             Log.log_3309(searchBase, filter);
          }
       }
 
       if (ne == null) {
          Log.log_3310(searchBase, filter);
          return null;
       }
 
       // Convert the results
       succeeded = false;
       try {
          QueryResult result = new QueryResult(true, ne);
          succeeded = true;
          return result;
       } finally {
 
          // If no exception has been thrown yet, then still
          // NamingEnumeration.close() could throw one
          if (succeeded) {
             Log.log_3310(searchBase, filter);
             ne.close();
 
          // If an exception has been thrown, don't allow it to be hidden by an
          // exception thrown by NamingEnumeration.close()
          } else {
             Log.log_3309(searchBase, filter);
             try {
                ne.close();
             } catch (Throwable exception) {
                Log.log_3311(exception);
             }
          }
       }
    }
 }
