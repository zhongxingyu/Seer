 /*
  * $Id$
  */
 package org.xins.common.service.ldap;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.SearchResult;
 
 /**
  * LDAP search result.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.115
  */
 public final class QueryResult extends Object {
 
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
     * Constructs a new <code>QueryResult</code> object.
     *
     * <p>This constructor accepts and reads from a {@link NamingEnumeration}
     * object. Note that it will not {@link NamingEnumeration#close() close()}
     * this object.
     *
     * @param authenticated
     *    flag that indicates if the authentication failed or not.
     *
     * @param namingEnumeration
    *    enumeration that returns the {@link SearchResult} objects, cannot be
    *    <code>null</code> and must return {@link SearchResult} objects.
     *
     * @throws IllegalArgumentException
     *    if <code>authenticated == false &amp;&amp; namingEnumeration != null</code>.
     *
     * @throws ClassCastException
     *    if <code>namingEnumeration.</code>{@link NamingEnumeration#next() next()}
     *    returns an object that is not an instance of class
     *    {@link SearchResult}.
     *
     * @throws NamingException
     *    if <code>namingEnumeration.</code>{@link NamingEnumeration#next() next()} or
     *    <code>namingEnumeration.</code>{@link NamingEnumeration#hasMore() hasMore()}
     *    throws this exception, or if
     *    <code>namingEnumeration.</code>{@link NamingEnumeration#next() next()}
     *    returned a <code>null</code> value.
     */
    QueryResult(boolean           authenticated,
                NamingEnumeration namingEnumeration)
    throws IllegalArgumentException, ClassCastException, NamingException {
 
       // Check preconditions
       if (!authenticated && namingEnumeration != null) {
          throw new IllegalArgumentException("authenticated == false && namingEnumeration != null");
       }
 
       // Initialize fields
       _searchResults = authenticated ? new ArrayList() : null;
 
       // Fill list of search results from the enumeration
       if (namingEnumeration != null) {
          while (namingEnumeration.hasMore()) {
             SearchResult sr = (SearchResult) namingEnumeration.next();
             if (sr == null) {
                throw new NamingException("NamingEnumeration.next() returned: null.");
             }
             _searchResults.add(sr);
          }
       }
    }
 
 
    //----------------------------------------------------------------------------
    // Fields
    //----------------------------------------------------------------------------
 
    /**
     * List of search results. If the authentication failed, then this field is
     * <code>null</code>, otherwise it cannot be <code>null</code> (although it
     * might be <em>empty</em>).
     */
    private final List _searchResults;
 
 
    //----------------------------------------------------------------------------
    // Methods
    //----------------------------------------------------------------------------
 
    /**
     * Determines if the authentication was successful. If it was, then
     * <code>true</code> is returned. If it was not, then <code>false</code> is
     * returned, and it is not allowed to call {@link #getSearchResultCount()}
     * nor {@link #getSearchResult(int)}. Both methods will throw an
     * {@link IllegalStateException} if called.
     *
     * @return
     *    <code>true</code> if the authentication succeeded, <code>false</code>
     *    if it failed.
     */
    public boolean isAuthenticated() {
       return _searchResults != null;
    }
 
    /**
     * Returns the number of available search results. This method should only
     * be called if the authentication succeeded, i.e. if
     * {@link #isAuthenticated()} returned <code>true</code>.
     *
     * @return
     *    the number of search results, always &gt;= 0.
     *
     * @throws IllegalStateException
     *    if {@link #isAuthenticated()}<code> == false</code>.
     */
    public int getSearchResultCount()
    throws IllegalStateException {
 
       // Check state
       if (_searchResults == null) {
          throw new IllegalStateException("Authentication failed.");
       }
 
       return _searchResults.size();
    }
 
    /**
     * Returns a specific search result by index. This method should only
     * be called if the authentication succeeded, i.e. if
     * {@link #isAuthenticated()} returned <code>true</code>.
     *
     * @param index
     *    the index, must be &gt;= 0 and &lt; {@link #getSearchResultCount()}.
     *
     * @return
     *    the search result at the specified index, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if {@link #isAuthenticated()}<code> == false</code>.
     *
     * @throws IndexOutOfBoundsException
     *    if <code>index &lt; 0 || index &gt;= </code>{@link #getSearchResultCount()}.
     */
    public SearchResult getSearchResult(int index)
    throws IllegalStateException, IndexOutOfBoundsException {
 
       // Check state
       if (_searchResults == null) {
          throw new IllegalStateException("Authentication failed.");
       }
 
       return (SearchResult) _searchResults.get(index);
    }
 }
