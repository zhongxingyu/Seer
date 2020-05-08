 /*
  * $Id$
  *
  * Copyright 2003-2008 Online Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.xml;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.ChainedMap;
 import org.xins.common.collections.ProtectedList;
 import org.xins.common.text.ParseException;
 
 /**
  * XML Element.
  *
  * <p>Note that this class is not thread-safe. It should not be used from
  * different threads at the same time. This applies even to read operations.
  *
  * <p>Note that the namespace URIs and local names are not checked for
  * validity in this class.
  *
  * <p>Instances of this class cannot be created directly, using a constructor.
  * Instead, use {@link ElementBuilder} to build an XML element, or
  * {@link ElementParser} to parse an XML string.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.1.0
  */
 public class Element implements Cloneable {
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = Element.class.getName();
 
    /**
     * The secret key to use to add child elements.
     */
    private static final Object SECRET_KEY = new Object();
 
    /**
     * The namespace prefix. This field can be <code>null</code>, but it can never
     * be an empty string.
     */
    private String _namespacePrefix;
 
    /**
     * The namespace URI. This field can be <code>null</code>, but it can never
     * be an empty string.
     */
    private String _namespaceURI;
 
    /**
     * The local name. This field is never <code>null</code>.
     */
    private String _localName;
 
    /**
     * The child elements. This field is lazily initialized is initially
     * <code>null</code>.
     */
    private ArrayList _children;
 
    /**
     * The attributes. This field is lazily initialized and is initially
     * <code>null</code>.
     */
    private ChainedMap _attributes;
 
    /**
     * The character content for this element. Can be <code>null</code>.
     */
    private String _text;
 
    /**
     * Creates a new <code>Element</code> with no namespace.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.0.
     */
    public Element(String localName)
    throws IllegalArgumentException {
       this(null, null, localName);
    }
 
    /**
     * Creates a new <code>Element</code>.
     *
     * @param namespaceURI
     *    the namespace URI for the element, can be <code>null</code>; an empty
     *    string is equivalent to <code>null</code>.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.0.
     */
    public Element(String namespaceURI, String localName)
    throws IllegalArgumentException {
       this(null, namespaceURI, localName);
    }
 
    /**
     * Creates a new <code>Element</code>.
     *
     * @param namespacePrefix
     *    the namespace prefix for the element, can be <code>null</code>; an empty
     *    string is equivalent to <code>null</code>.
     *
     * @param namespaceURI
     *    the namespace URI for the element, can be <code>null</code>; an empty
     *    string is equivalent to <code>null</code>.
     *
     * @param localName
     *    the local name of the element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.1.
     */
    public Element(String namespacePrefix, String namespaceURI, String localName)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("localName", localName);
 
       // An empty namespace prefix is equivalent to null
       if (namespacePrefix != null && namespacePrefix.length() < 1) {
          _namespacePrefix = null;
       } else {
          _namespacePrefix = namespacePrefix;
       }
 
       // An empty namespace URI is equivalent to null
       if (namespaceURI != null && namespaceURI.length() < 1) {
          _namespaceURI = null;
       } else {
          _namespaceURI = namespaceURI;
       }
 
       _localName = localName;
    }
 
    /**
     * Sets the namespace prefix.
     *
     * @param namespacePrefix
     *    the namespace prefix of this element, can be <code>null</code>.
     *    If the value if <code>null</code> or an empty string, the element is
     *    consider to have no namespace prefix.
     *
    * @throws IllegalArgumentException
    *    if <code>namespacePrefix == null</code>.
    *
     * @since XINS 2.1.
     */
   public void setNamespacePrefix(String namespacePrefix) throws IllegalArgumentException {
       if (namespacePrefix != null && namespacePrefix.length() == 0) {
          _namespacePrefix = null;
       } else {
          _namespacePrefix = namespacePrefix;
       }
    }
 
    /**
     * Gets the namespace prefix.
     *
     * @return
     *    the namespace prefix for this element, or <code>null</code> if there is
     *    none, but never an empty string.
     *
     * @since XINS 2.1.
     */
    public String getNamespacePrefix() {
       return _namespacePrefix;
    }
 
    /**
     * Sets the namespace URI.
     *
     * @param namespaceURI
     *    the namespace URI of this element, can be <code>null</code>.
     *    If the value if <code>null</code> or an empty string, the element is
     *    consider to have no namespace URI.
     *
    * @throws IllegalArgumentException
    *    if <code>namespaceURI == null</code>.
    *
     * @since XINS 2.1.
     */
   public void setNamespaceURI(String namespaceURI) throws IllegalArgumentException {
       if (namespaceURI != null && namespaceURI.length() == 0) {
          _namespaceURI = null;
       } else {
          _namespaceURI = namespaceURI;
       }
    }
 
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
     * Sets the local name.
     *
     * @param localName
     *    the local name of this element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.0.
     */
    public void setLocalName(String localName) throws IllegalArgumentException {
       MandatoryArgumentChecker.check("localName", localName);
       _localName = localName;
    }
 
    /**
     * Sets the specified attribute. If the value for the specified
     * attribute is already set, then the previous value is replaced.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @param value
     *    the value for the attribute, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.0.
     */
    public void setAttribute(String localName, String value)
    throws IllegalArgumentException {
       setAttribute(null, null, localName, value);
    }
 
    /**
     * Sets the specified attribute. If the value for the specified
     * attribute is already set, then the previous value is replaced.
     *
     * @param namespaceURI
     *    the namespace URI for the attribute, can be <code>null</code>; an
     *    empty string is equivalent to <code>null</code>.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @param value
     *    the value for the attribute, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.0.
     */
    public void setAttribute(String namespaceURI, String localName, String value)
    throws IllegalArgumentException {
       setAttribute(null, namespaceURI, localName, value);
    }
 
    /**
     * Sets the specified attribute. If the value for the specified
     * attribute is already set, then the previous value is replaced.
     *
     * @param namespacePrefix
     *    the namespace prefix for the attribute, can be <code>null</code>; an
     *    empty string is equivalent to <code>null</code>.
     *
     * @param namespaceURI
     *    the namespace URI for the attribute, can be <code>null</code>; an
     *    empty string is equivalent to <code>null</code>.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @param value
     *    the value for the attribute, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.1.
     */
    public void setAttribute(String namespacePrefix, String namespaceURI, String localName, String value)
    throws IllegalArgumentException {
 
       // Construct a QualifiedName object. This will check the preconditions.
       QualifiedName qn = new QualifiedName(namespacePrefix, namespaceURI, localName);
 
       if (_attributes == null) {
          if (value == null) {
             return;
          }
 
          // Lazily initialize
          _attributes = new ChainedMap();
       }
 
       // Set or reset the attribute
       _attributes.put(qn, value);
    }
 
 
    /**
     * Removes the specified attribute. If no attribute with the specified name
     * exists, nothing happens.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.0.
     */
    public void removeAttribute(String localName)
    throws IllegalArgumentException {
       removeAttribute(null, localName);
    }
 
    /**
     * Removes the specified attribute. If no attribute with the specified name
     * exists, nothing happens.
     *
     * @param namespaceURI
     *    the namespace URI for the attribute, can be <code>null</code>; an
     *    empty string is equivalent to <code>null</code>.
     *
     * @param localName
     *    the local name for the attribute, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>localName == null</code>.
     *
     * @since XINS 2.0.
     */
    public void removeAttribute(String namespaceURI, String localName)
    throws IllegalArgumentException {
 
       // Construct a QualifiedName object. This will check the preconditions.
       QualifiedName qn = new QualifiedName(namespaceURI, localName);
 
       if (_attributes != null) {
          _attributes.remove(qn);
       }
 
    }
 
    /**
     * Gets the attributes of this element.
     *
     * @return
     *    a {@link Map} (never <code>null</code>) which contains the attributes;
     *    each key in the <code>Map</code> is a {@link QualifiedName} instance
     *    (not <code>null</code>) and each value in it is a <code>String</code>
     *    instance (not <code>null</code>).
     */
    public Map getAttributeMap() {
       if (_attributes == null) {
          _attributes = new ChainedMap();
       }
       return _attributes;
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
     *
     * @since XINS 2.0.
     */
    public void addChild(Element child) throws IllegalArgumentException {
 
       final String METHODNAME = "addChild(Element)";
 
       // Check preconditions
       MandatoryArgumentChecker.check("child", child);
       if (child == this) {
          String message = "child == this";
          Log.log_1050(CLASSNAME, METHODNAME, Utils.getCallingClass(), Utils.getCallingMethod(), message);
          throw new IllegalArgumentException(message);
       }
 
       // Lazily initialize
       if (_children == null) {
          _children = new ArrayList();
       }
 
       _children.add(child);
    }
 
 
    /**
     * Removes a child element. If the child is not found, nothing is removed.
     *
     * @param child
     *    the child to be removed to this element, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>child == null || child == <em>this</em></code>.
     *
     * @since XINS 2.0.
     */
    public void removeChild(Element child) throws IllegalArgumentException {
 
       final String METHODNAME = "removeChild(Element)";
 
       // Check preconditions
       MandatoryArgumentChecker.check("child", child);
       if (child == this) {
          String message = "child == this";
          Log.log_1050(CLASSNAME, METHODNAME, Utils.getCallingClass(), Utils.getCallingMethod(), message);
          throw new IllegalArgumentException(message);
       }
 
       // Lazily initialize
       if (_children == null) {
          return;
       }
 
       _children.remove(child);
    }
 
    /**
     * Gets the list of all child elements.
     *
     * @return
     *    the {@link List} containing all child elements; each
     *    element in the list is another <code>Element</code> instance;
     *    never <code>null</code>.
     */
    public List getChildElements() {
 
       if (_children == null) {
          _children = new ArrayList();
       }
 
       return _children;
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
     *    never <code>null</code>. The list cannot be modified.
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
     *
     * @since XINS 2.0.
     */
    public void setText(String text) {
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
     * Gets the unique child of this element with the specified name.
     *
     * @param elementName
     *    the name of the child element to get, or <code>null</code> if the
     *    element name is irrelevant.
     *
     * @return
     *    the sub-element of this element, never <code>null</code>.
     *
     * @throws ParseException
     *    if no child with the specified name was found,
     *    or if more than one child with the specified name was found.
     *
     * @since XINS 1.4.0
     */
    public Element getUniqueChildElement(String elementName)
    throws ParseException {
 
       List childList;
       if (elementName == null) {
          childList = getChildElements();
       } else {
          childList = getChildElements(elementName);
       }
 
       if (childList.size() == 0) {
          throw new ParseException("No \"" + elementName +
                "\" children found in the \"" + getLocalName() +
                "\" element.");
       } else if (childList.size() > 1) {
          throw new ParseException("More than one \"" + elementName +
                "\" children found in the \"" + getLocalName() +
                "\" element.");
       }
       return (Element) childList.get(0);
    }
 
    /**
     * Gets the unique child of this element.
     *
     * @return
     *    the sub-element of this element, never <code>null</code>.
     *
     * @throws ParseException
     *    if no child was found or more than one child was found.
     *
     * @since XINS 2.2
     */
    public Element getUniqueChildElement() throws ParseException {
       return getUniqueChildElement(null);
    }
 
    public int hashCode() {
       int hashCode = _localName.hashCode();
       if (_namespaceURI != null) {
          hashCode += _namespaceURI.hashCode();
       }
       if (_attributes != null) {
          hashCode += _attributes.hashCode();
       }
       if (_children != null) {
          hashCode += _children.hashCode();
       }
       if (_text != null) {
          hashCode += _text.hashCode();
       }
       return hashCode;
    }
 
    public boolean equals(Object obj) {
       if (obj == null || !(obj instanceof Element)) {
          return false;
       }
       Element other = (Element) obj;
       if (!_localName.equals(other.getLocalName()) ||
             (_namespaceURI != null && !_namespaceURI.equals(other._namespaceURI)) ||
             (_namespaceURI == null && other._namespaceURI != null)) {
          return false;
       }
       if ((_attributes != null && !_attributes.equals(other._attributes)) ||
             (_attributes == null && other._attributes != null)) {
          return false;
       }
       // XXX This should be changed as the order of the children should no matter
       if ((_children != null && !_children.equals(other._children)) ||
             (_children == null && other._children != null)) {
          return false;
       }
       if ((_text != null && !_text.equals(other._text)) ||
             (_text == null && other._text != null)) {
          return false;
       }
       return true;
    }
 
    /**
     * Clones this object. The clone will have the same namespace URI and local
     * name and equivalent attributes, children and character content.
     *
     * @return
     *    a new clone of this object, never <code>null</code>.
     */
    public Object clone() {
 
       // Construct a new Element, copy all field values (shallow copy)
       Element clone;
       try {
          clone = (Element) super.clone();
       } catch (CloneNotSupportedException exception) {
          throw Utils.logProgrammingError(exception);
       }
 
       // Deep copy the children
       if (_children != null) {
          clone._children = (ArrayList) _children.clone();
       }
 
       // Deep copy the attributes
       if (_attributes != null) {
          clone._attributes = (ChainedMap) _attributes.clone();
       }
 
       return clone;
    }
 
    /**
     * Overrides the {@link Object#toString()} method to return
     * the element as its XML representation.
     *
     * @return
     *    the XML representation of this element without the XML declaration,
     *    never <code>null</code>.
     */
    public String toString() {
       ElementSerializer serializer = new ElementSerializer();
       return serializer.serialize(this);
    }
 
    /**
     * Qualified name for an element or attribute. This is a combination of an
     * optional namespace URI and a mandatory local name.
     *
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     *
     * @since XINS 1.1.0
     */
    public static final class QualifiedName {
 
       /**
        * The hash code for this object.
        */
       private final int _hashCode;
 
       /**
        * The namespace prefix. Can be <code>null</code>.
        */
       private final String _namespacePrefix;
 
       /**
        * The namespace URI. Can be <code>null</code>.
        */
       private final String _namespaceURI;
 
       /**
        * The local name. Cannot be <code>null</code>.
        */
       private final String _localName;
 
       /**
        * Constructs a new <code>QualifiedName</code> with the specified
        * namespace and local name.
        *
        * @param namespaceURI
        *    the namespace URI for the element, can be <code>null</code>; an
        *    empty string is equivalent to <code>null</code>.
        *
        * @param localName
        *    the local name of the element, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>localName == null</code>.
        */
       public QualifiedName(String namespaceURI, String localName)
       throws IllegalArgumentException {
          this(null, namespaceURI, localName);
       }
 
       /**
        * Constructs a new <code>QualifiedName</code> with the specified
        * namespace and local name.
        *
        * @param namespacePrefix
        *    the namespace prefix for the element, can be <code>null</code>; an
        *    empty string is equivalent to <code>null</code>.
        *
        * @param namespaceURI
        *    the namespace URI for the element, can be <code>null</code>; an
        *    empty string is equivalent to <code>null</code>.
        *
        * @param localName
        *    the local name of the element, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>localName == null</code>.
        *
        * @since XINS 2.1.
        */
       public QualifiedName(String namespacePrefix, String namespaceURI, String localName)
       throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("localName", localName);
 
          // An empty namespace prefix is equivalent to null
          if (namespacePrefix != null && namespacePrefix.length() < 1) {
             _namespacePrefix = null;
          } else {
             _namespacePrefix = namespacePrefix;
          }
 
          // An empty namespace URI is equivalent to null
          if (namespaceURI != null && namespaceURI.length() < 1) {
             _namespaceURI = null;
          } else {
             _namespaceURI = namespaceURI;
          }
 
          // Initialize fields
          _hashCode     = localName.hashCode();
          _localName    = localName;
       }
 
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
          return ((_namespaceURI == null && qn._namespaceURI == null) ||
                (_namespaceURI != null && _namespaceURI.equals(qn._namespaceURI)))
             &&  _localName.equals(qn._localName);
       }
 
       /**
        * Gets the namespace prefix.
        *
        * @return
        *    the namespace prefix, can be <code>null</code>.
        *
        * @since XINS 2.1.
        */
       public String getNamespacePrefix() {
          return _namespacePrefix;
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
