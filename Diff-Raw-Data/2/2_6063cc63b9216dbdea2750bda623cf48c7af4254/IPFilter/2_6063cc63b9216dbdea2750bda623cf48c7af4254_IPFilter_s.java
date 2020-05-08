 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.List;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.ParseException;
 
 /**
  * Authorization filter for IP addresses. An <code>IPFilter</code> object is
  * created and used as follows:
  *
  * <blockquote><code>IPFilter filter = IPFilter.parseFilter("10.0.0.0/24");
  * <br>if (filter.isAuthorized("10.0.0.1")) {
  * <br>&nbsp;&nbsp;&nbsp;// IP is granted access
  * <br>} else {
  * <br>&nbsp;&nbsp;&nbsp;// IP is denied access
 * <br>}</code><blockquote>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class IPFilter
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Creates an <code>IPFilter</code> object for the specified filter
     * expression. The expression consists of a base IP address and a bit
     * count. The bit count indicates how many bits in an IP address must match
     * the bits in the base IP address. 
     *
     * @param expression
     *    the filter expression, cannot be <code>null</code> and must match the
     *    form:
     *    <code><em>a</em>.<em>a</em>.<em>a</em>.<em>a</em>/<em>n</em></code>,
     *    where <em>a</em> is a number between 0 and 255, with no leading
     *    zeroes, and <em>n</em> is a number between <em>0</em> and
     *    <em>32</em>, no leading zeroes.
     *
     * @throws IllegalArgumentException
     *    if <code>expression == null</code>.
     *
     * @throws ParseException
     *    if <code>expression</code> does not match the specified format.
     */
    public static final IPFilter parseFilter(String expression)
    throws IllegalArgumentException, ParseException {
 
       return null; // TODO
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    // TODO: Constructor
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the filter expression.
     *
     * @return
     *    the original filter expression, never <code>null</code>.
     */
    public final String getExpression() {
       return null; // TODO
    }
 
    /**
     * Determines if the specified IP address is authorized.
     *
     * @param ip
     *    the IP address of which must be determined if it is authorized,
     *    cannot be <code>null</code> and must match the form:
     *    <code><em>a</em>.<em>a</em>.<em>a</em>.<em>a</em>/<em>n</em></code>,
     *    where <em>a</em> is a number between 0 and 255, with no leading
     *    zeroes.
     *
     * @return
     *    <code>true</code> if the IP address is authorized to access the
     *    protected resource, otherwise <code>false</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null</code>.
     *
     * @throws ParseException
     *    if <code>ip</code> does not match the specified format.
     */
    public final boolean isAuthorized(String ip)
    throws IllegalArgumentException, ParseException {
       return false; // TODO
    }
 
    /**
     * Returns a textual representation of this filter. The implementation of
     * this method returns the filter expression passed.
     *
     * @return
     *    a textual presentation, never <code>null</code>.
     */
    public final String toString() {
       return getExpression();
    }
 }
