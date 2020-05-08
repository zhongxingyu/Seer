 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.xml;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.ProtectedList;
 
 import org.xins.common.text.TextUtils;
 
 /**
  * Element in a XINS result data section.
  *
  * <p>Note that this class is not thread-safe. It should not be used from
  * different threads at the same time. This applies even to read operations.
  *
  * <p>Note that the namespace URIs and local names are not checked for
  * validity in this class.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public class Element implements Cloneable {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = Element.class.getName();
 
    /**
     * Fully-qualified name of the inner class <code>QualifiedName</code>.
     */
    private static final String QN_CLASSNAME = QualifiedName.class.getName();
 
    /**
     * The secret key to use to add child elements.
     */
    private static final Object SECRET_KEY = new Object();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>Element</code> with no namespace.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     */
    Element(String localName)
    throws IllegalArgumentException {
       this(null, localName);
    }
 
    /**
     * Creates a new <code>Element</code>.
     *
     * @param namespaceURI
     *    the namespace URI for the element, can be <code>null</code>; an empty
     *    string is equivalent to <code>null<code>.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     */
    Element(String namespaceURI, String localName)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("localName", localName);
 
       // An empty namespace URI is equivalent to null
       if (namespaceURI != null && namespaceURI.length() < 1) {
          namespaceURI = null;
       }
 
       // Store namespace URI and local name
       _namespaceURI = namespaceURI;
       _localName    = localName;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The namespace URI. This field can be <code>null</code>, but it can never
     * be an empty string.
     */
    private final String _namespaceURI;
 
    /**
     * The local name. This field is never <code>null</code>.
     */
    private final String _localName;
 
    /**
     * The child elements. This field is lazily initialized is initially
     * <code>null</code>.
     */
    private ProtectedList _children;
 
    /**
     * The attributes. This field is lazily initialized and is initially
     * <code>null</code>.
     */
    private HashMap _attributes;
 
    /**
     * The character content for this element. Can be <code>null</code>.
     */
    private String _text;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the namespace URI.
     *
     * @return
     *    the namespace URI for this element, or <code>null</code> if there is
     *    none, but never an empty string.
     */
    public String getNamespaceURI() {
       return _namespaceURI;
    }
 
    /**
     * Gets the local name.
     *
     * @return
     *    the local name of this element, cannot be <code>null</code>.
     */
    public String getLocalName() {
       return _localName;
    }
 
    /**
     * Sets the specified attribute. If the value for the specified
     * attribute is already set, then the previous value is replaced.
     *
     * @param namespaceURI
     *    the namespace URI for the attribute, can be <code>null</code>; an
     *    empty string is equivalent to <code>null<code>.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @param value
     *    the value for the attribute, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     */
    void setAttribute(String namespaceURI, String localName, String value)
    throws IllegalArgumentException {
 
       final String THIS_METHOD = "setAttribute(java.lang.String,java.lang.String,java.lang.String)";
 
       // Construct a QualifiedName object. This will check the preconditions.
       QualifiedName qn = new QualifiedName(namespaceURI, localName);
 
       if (_attributes == null) {
          if (value == null) {
             return;
          }
 
          // Lazily initialize
          _attributes = new HashMap();
       }
 
       // Set or reset the attribute
       _attributes.put(qn, value);
 
       // Check postconditions
       String getValue = getAttribute(namespaceURI, localName);
       if ((value != null && getValue == null)
        || (value == null && getValue != null)
        || (!value.equals(getValue))) {
          String message = "Postcondition failed"
            + "; namespaceURI="      + TextUtils.quote(namespaceURI)
            + "; qn.namespaceURI="   + TextUtils.quote(qn.getNamespaceURI())
            + "; localName="         + TextUtils.quote(localName)
            + "; qn.localName="      + TextUtils.quote(qn.getLocalName())
            + "; value="             + TextUtils.quote(value)
            + "; getAttribute(...)=" + TextUtils.quote(getValue)
            + '.';
          throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, CLASSNAME, THIS_METHOD, message);
          // TODO: Something like the following but with logging: throw ProgrammingError.forPostCondition(CLASSNAME, THIS_METHOD, message) -- message would be without "Postcondition failed"
       }
    }
 
    /**
     * Gets an unmodifiable view of all attributes.
     *
     * @return
     *    an unmodifiable {@link Map} (never <code>null</code>) which is a view
     *    on all the attributes; each key in the <code>Map</code> is a
     *    {@link QualifiedName} instance (not <code>null</code>) and each value
     *    in it is a <code>String</code> instance (not <code>null</code>).
     */
    public Map getAttributeMap() {
       if (_attributes == null) {
          return Collections.EMPTY_MAP;
       } else {
          return Collections.unmodifiableMap(_attributes);
       }
    }
 
    /**
     * Gets the value of the attribute with the qualified name. If the
     * qualified name does not specify a namespace, then only an attribute that
     * does not have a namespace will match.
     *
     * @param qn
     *    a combination of an optional namespace and a mandatory local name, or
     *    <code>null</code>.
     *
     * @return
     *    the value of the attribute that matches the specified namespace and
     *    local name, or <code>null</code> if such an attribute is either not
     *    set or set to <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>qn == null</code>.
     */
    public String getAttribute(QualifiedName qn)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("qn", qn);
 
       if (_attributes == null) {
          return null;
       } else {
          return (String) _attributes.get(qn);
       }
    }
 
    /**
     * Gets the value of the attribute with the specified namespace and local
     * name. The namespace is optional. If the namespace is not given, then only
     * an attribute that does not have a namespace will match.
     *
     * @param namespaceURI
     *    the namespace URI for the attribute, can be <code>null</code>; an
     *    empty string is equivalent to <code>null</code>; if specified this
     *    string must be a valid namespace URI.
     *
     * @param localName
     *    the local name of the attribute, cannot be <code>null</code>.
     *
     * @return
     *    the value of the attribute that matches the specified namespace and
     *    local name, or <code>null</code> if such an attribute is either not
     *    set or set to <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     */
    public String getAttribute(String namespaceURI, String localName)
    throws IllegalArgumentException {
       QualifiedName qn = new QualifiedName(namespaceURI, localName);
       return getAttribute(qn);
    }
 
    /**
     * Gets the value of an attribute that has no namespace.
     *
     * @param localName
     *    the local name of the attribute, cannot be <code>null</code>.
     *
     * @return
     *    the value of the attribute that matches the specified local name and
     *    has no namespace defined, or <code>null</code> if the attribute is
     *    either not set or set to <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     */
    public String getAttribute(String localName)
    throws IllegalArgumentException {
       return getAttribute(null, localName);
    }
 
    /**
     * Adds a new child element.
     *
     * @param child
     *    the new child to add to this element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>child == null || child == <em>this</em></code>.
     */
    void addChild(Element child) throws IllegalArgumentException {
 
       final String METHODNAME = "addChild(Element)";
 
       // Check preconditions
       MandatoryArgumentChecker.check("child", child);
       if (child == this) {
          String message = "child == this";
          Log.log_1050(CLASSNAME, METHODNAME, Utils.getCallingClass(), Utils.getCallingMethod(), message);
          // TODO: Log.log_1050 for every IllegalArgumentException
          throw new IllegalArgumentException(message);
       }
 
       // Lazily initialize
       if (_children == null) {
          _children = new ProtectedList(SECRET_KEY);
       }
 
       _children.add(SECRET_KEY, child);
    }
 
    /**
     * Gets the list of all child elements.
     *
     * @return
     *    an unmodifiable {@link List} containing all child elements; each
     *    element in the list is another <code>Element</code> instance;
     *    never <code>null</code>.
     */
    public List getChildElements() {
 
       // If there are no children, then return an immutable empty List
       if (_children == null || _children.size() == 0) {
          return Collections.EMPTY_LIST;
 
       // Otherwise return an immutable view of the list of children
       } else {
          return _children;
       }
    }
 
    /**
     * Gets the list of child elements that match the specified name.
     *
     * @param name
     *    the name for the child elements to match, cannot be
     *    <code>null</code>.
     *
     * @return
     *    a {@link List} containing each child element that matches the
     *    specified name as another <code>Element</code> instance;
     *    never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public List getChildElements(String name)
    throws IllegalArgumentException {
 
       // TODO: Support namespaces
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       // If there are no children, then return null
      if (_children == null || _children.size() == 0) {
          return Collections.EMPTY_LIST;
 
       // There are children, find all matching ones
       } else {
          ProtectedList matches = new ProtectedList(SECRET_KEY);
          Iterator it = _children.iterator();
          while (it.hasNext()) {
             Element child = (Element) it.next();
             if (name.equals(child.getLocalName())) {
                matches.add(SECRET_KEY, child);
             }
          }
 
          // If there are no matching children, then return null
          if (matches.size() == 0) {
             return Collections.EMPTY_LIST;
 
          // Otherwise return an immutable list with all matches
          } else {
             return matches;
          }
       }
    }
 
    /**
     * Sets the character content. The existing character content, if any, is
     * replaced
     *
     * @param text
     *    the character content for this element, or <code>null</code>.
     */
    void setText(String text) {
       _text = text;
    }
 
    /**
     * Gets the character content, if any.
     *
     * @return
     *    the character content of this element, or <code>null</code> if no
     *    text has been specified for this element.
     */
    public String getText() {
       return _text;
    }
 
    /**
     * Clones this object. The clone will have the same namespace URI and local
     * name and equivalent attributes, children and character content.
     *
     * @return
     *    a new clone of this object, never <code>null</code>.
     */
    public Object clone() {
 
       // Construct a new Element, copy the name
       Element clone = new Element(getNamespaceURI(), getLocalName());
 
       // Copy the children
       if (_children != null) {
          clone._children = (ProtectedList) _children.clone();
       }
 
       // Copy the attributes
       if (_attributes != null) {
          clone._attributes = (HashMap) _attributes.clone();
       }
 
       // Copy the character content
       clone._text = _text;
 
       return clone;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Qualified name for an element or attribute. This is a combination of an
     * optional namespace URI and a mandatory local name.
     *
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.1.0
     */
    public static final class QualifiedName extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>QualifiedName</code> with the specified
        * namespace and local name.
        *
        * @param namespaceURI
        *    the namespace URI for the element, can be <code>null</code>; an
        *    empty string is equivalent to <code>null<code>.
        *
        * @param localName
        *    the local name of the element, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>localName == null</code>.
        */
       public QualifiedName(String namespaceURI, String localName)
       throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("localName", localName);
 
          // An empty namespace URI is equivalent to null
          if (namespaceURI != null && namespaceURI.length() < 1) {
             namespaceURI = null;
          }
 
          // Initialize fields
          _hashCode     = localName.hashCode();
          _namespaceURI = namespaceURI;
          _localName    = localName;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The hash code for this object.
        */
       private final int _hashCode;
 
       /**
        * The namespace URI. Can be <code>null</code>.
        */
       private final String _namespaceURI;
 
       /**
        * The local name. Cannot be <code>null</code>.
        */
       private final String _localName;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the hash code value for this object.
        *
        * @return
        *    the hash code value.
        */
       public int hashCode() {
          return _hashCode;
       }
 
       /**
        * Compares this object with the specified object for equality.
        *
        * @param obj
        *    the object to compare with, or <code>null</code>.
        *
        * @return
        *    <code>true</code> if this object and the argument are considered
        *    equal, <code>false</code> otherwise.
        */
       public boolean equals(Object obj) {
 
          if (! (obj instanceof QualifiedName)) {
             return false;
          }
 
          QualifiedName qn = (QualifiedName) obj;
          return ((_namespaceURI == null && qn._namespaceURI == null) || (_namespaceURI.equals(qn._namespaceURI)))
             &&  _localName.equals(qn._localName);
       }
 
       /**
        * Gets the namespace URI.
        *
        * @return
        *    the namespace URI, can be <code>null</code>.
        */
       public String getNamespaceURI() {
          return _namespaceURI;
       }
 
       /**
        * Gets the local name.
        *
        * @return
        *    the local name, never <code>null</code>.
        */
       public String getLocalName() {
          return _localName;
       }
    }
 }
