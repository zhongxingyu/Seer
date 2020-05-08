 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.FastStringBuffer;
 import org.xins.util.text.ParseException;
 
 /**
  * Access rule list.
  *
  * <h3>Descriptor format</h3>
  *
  * <p>An access rule list <em>descriptor</em>, a character string, can be
  * converted to produce an {@link AccessRuleList} object. A valid descriptor
  * consists of a list of access rule descriptors (see class
  * {@link AccessRule}), separated by semi-colon characters (<code>';'</code>).
  * Optionally, the rules can have any amount of whitespace (space-, tab-,
  * newline- and carriage return-characters), before and after them. The last
  * descriptor cannot end with a semi-colon.
  *
  * <h3>Descriptor examples</h3>
  *
  * <p>An example of an access rule list descriptor is:
  *
  * <blockquote><code>allow 194.134.168.213/32 *;
  * <br>deny  194.134.168.213/24 _*;
  * <br>allow 194.134.168.213/24 *;
  * <br>deny 0.0.0.0/0 *</code></blockquote>
  *
  * <p>The above access control list grants access to the IP address
  * 194.134.168.213 to access all functions. Then in the second rule it denies
  * access to all IP addresses in the range 194.134.168.0 to 194.134.168.255 to
  * all functions that start with an underscore (<code>'_'</code>). Then it
  * allows access for those IP addresses to all other functions, and finally
  * all other IP addresses are denied access to any of the functions.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class AccessRuleList
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Parses the specified character string to construct a new
     * <code>AccessRuleList</code> object.
     *
     * @param descriptor
     *    the access rule list descriptor, the character string to parse,
     *    cannot be <code>null</code>.
     *
     * @return
     *    an {@link AccessRuleList} instance, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @throws ParseException
     *    if there was a parsing error.
     */
    public static final AccessRuleList parseAccessRuleList(String descriptor)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("descriptor", descriptor);
 
       // Tokenize the descriptor
       StringTokenizer tokenizer = new StringTokenizer(descriptor, ";");
       int ruleCount = tokenizer.countTokens();
 
       // Parse all tokens
       AccessRule[] rules = new AccessRule[ruleCount];
       for (int i = 0; i < ruleCount; i++) {
 
          // Remove leading and trailing whitespace from the next token
          String token = tokenizer.nextToken().trim();
 
          // Parse and add the rule
          rules[i] = AccessRule.parseAccessRule(token);
       }
 
       return new AccessRuleList(rules);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>AccessRuleList</code> object. The passed
     * {@link AccessRule} array is assumed to be owned by the constructor.
     *
     * @param rules
     *    the list of rules, should not be <code>null</code> and should not
     *    contain any <code>null</code> elements; if these rules are violated,
     *    the behaviour is undefined.
     */
    public AccessRuleList(AccessRule[] rules) {
 
       // Store the rules
       _rules = rules;
 
       // Build string representation
       int ruleCount = rules.length;
       FastStringBuffer buffer = new FastStringBuffer(ruleCount * 40);
       if (ruleCount > 0) {
          buffer.append(rules[0].toString());
       }
       for (int i = 1; i < ruleCount; i++) {
          buffer.append(';');
          buffer.append(rules[i].toString());
       }
       _asString = buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The list of rules. Cannot be <code>null</code>.
     */
    private AccessRule[] _rules;
 
    /**
     * The string representation of this instance. Cannot be <code>null</code>.
     */
    private String _asString;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Counts the number of rules in this list.
     *
     * @return
     *    the number of rules, always &gt;= 0.
     */
    public int getRuleCount() {
       return _rules.length;
    }
 
    /**
     * Determines if the specified IP address is allowed to access the
     * specified function. This method finds the first matching rule and then
     * returns the <em>allow</em> property of that rule (see
     * {@link AccessRule#isAllowRule()}). If there is no matching rule, then
     * <code>false</code> is returned.
     *
     * @param ip
     *    the IP address, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if the request is allowed, <code>false</code> if
     *    the request is denied.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null || functionName == null</code>.
     *
     * @throws ParseException
     *    if the specified IP address is malformed.
     */
    public boolean allow(String ip, String functionName)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("ip", ip, "functionName", functionName);
 
       // TODO: Logging
 
       int ruleCount = _rules.length;
       for (int i = 0; i < ruleCount; i++) {
          AccessRule rule = _rules[i];
          if (rule.match(ip, functionName)) {
             return rule.isAllowRule();
          }
       }
 
       return false;
    }
 
    /**
     * Returns a character string representation of this object. The returned
     * string is in the form:
     *
     * <blockquote><em>type a.b.c.d/m pattern;type a.b.c.d/m pattern</em></blockquote>
     *
     * where <em>type</em> is either <code>"allow"</code> or
     * <code>"deny"</code>, <em>a.b.c.d</em> is the base IP address, <em>m</em>
     * is the mask, and <em>pattern</em> is the function name simple pattern.
     *
     * @return
     *    a character string representation of this access rule, never
     *    <code>null</code>.
     */
    public String toString() {
       return _asString;
    }
 }
