 /*
  * $Id: Element.java,v 1.44 2007/12/17 14:22:56 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.xml;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.ChainedMap;
 import org.xins.common.collections.ProtectedList;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 
 /**
  * XML Element. It always has a name and it may have attributes and contained
  * elements and character data. Comments and parsing instructions are not
  * supported.
  *
  * <p>Note that this class is not thread-safe. It should not be used from
  * different threads at the same time. This applies even to read operations.
  *
  * <p>Note that the namespace URIs and local names are not checked for
  * validity in this class.
  *
  * <p>Tip: Use class {@link ElementParser} to parse an XML string.
  *
  * @version $Revision: 1.44 $ $Date: 2007/12/17 14:22:56 $
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
     * The children, both elements and text. This field is lazily initialized and is
     * initially <code>null</code>.
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
     * @since XINS 2.0
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
     * @since XINS 2.0
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
     * @since XINS 2.1
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
     *    the namespace prefix of this element;
     *    if it is <code>null</code> or an empty string, then the element is
     *    considered to have no namespace.
     *
     * @since XINS 2.1
     */
    public void setNamespacePrefix(String namespacePrefix) {
       _namespacePrefix = (namespacePrefix == null || namespacePrefix.length() == 0)
                        ? null
                        : namespacePrefix;
 
       // Keep the namespace URI consistent
       if (_namespacePrefix == null) {
          _namespaceURI = null;
       }
    }
 
    /**
     * Gets the namespace prefix.
     *
     * @return
     *    the namespace prefix for this element, or <code>null</code> if there is
     *    none, but never an empty string.
     *
     * @since XINS 2.1
     */
    public String getNamespacePrefix() {
       return _namespacePrefix;
    }
 
    /**
     * Sets the namespace URI.
     *
     * @param namespaceURI
     *    the namespace URI of this element;
     *    if it is <code>null</code> or an empty string, then the element is
     *    considered to have no namespace.
     *
     * @since XINS 2.1
     */
    public void setNamespaceURI(String namespaceURI) {
       _namespaceURI = (namespaceURI == null || namespaceURI.length() == 0)
                     ? null
                     : namespaceURI;
 
       // Keep the namespace URI consistent
       if (_namespaceURI == null) {
          _namespacePrefix = null;
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
     * @since XINS 2.0
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
     * @since XINS 2.0
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
     * @since XINS 2.0
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
     * @since XINS 2.1
     */
    public void setAttribute(String namespacePrefix, String namespaceURI, String localName, String value)
    throws IllegalArgumentException {
 
       // Construct a QualifiedName object; this will check the preconditions
       QualifiedName qn = new QualifiedName(namespacePrefix, namespaceURI, localName);
 
       // If there are no attributes and the attribute should become null, then
       // nothing needs to be done
       if (_attributes == null && value == null) {
          return;
 
       // Check if there are any attributes yet, since the collection is lazily
       // initialized
       } else if (_attributes == null) {
          _attributes = new ChainedMap();
       }
 
       // Reset or set the attribute
       if (value == null) {
          _attributes.remove(qn);
       } else {
          _attributes.put(qn, value);
       }
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
     * @since XINS 2.0
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
     * @since XINS 2.0
     */
    public void removeAttribute(String namespaceURI, String localName)
    throws IllegalArgumentException {
 
       // Construct a QualifiedName object; this will check the preconditions
       QualifiedName qn = new QualifiedName(namespaceURI, localName);
 
       // Remove the attribute
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
     * Returns the number of attributes set on this element.
     *
     * @return
     *    the number of attributes set, always &gt;= 0.
     *
     * @since XINS 3.0
     */
    public int getAttributeCount() {
       return (_attributes == null) ? 0 : _attributes.size();
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
     * @since XINS 2.0
     *
     * @deprecated
     *    Since XINS 3.0, use {@link #add(Element)} instead.
     */
    public void addChild(Element child) throws IllegalArgumentException {
       add(child);
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
     * @since XINS 3.0
     */
    public void add(Element child) throws IllegalArgumentException {
 
       final String METHODNAME = "add(Element)";
 
       // Check preconditions
       MandatoryArgumentChecker.check("child", child);
       if (child == this) {
          String message = "child == this";
          Log.log_1050(CLASSNAME, METHODNAME, Utils.getCallingClass(), Utils.getCallingMethod(), message);
          throw new IllegalArgumentException(message);
       }
 
       // TODO: Also check to make sure an ancestor is not added as a child
 
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
     * @since XINS 2.0
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
 
       // Actually remove the child
       _children.remove(child);
    }
 
    /**
     * Gets the list of all child elements.
     *
     * <p>The returned {@link List} is (since XINS 3.0) modifiable by the
     * caller.
     *
     * @return
     *    a non-<code>null</code> {@link List} containing all child elements
     *    (and no <code>null</code> elements).
     */
    public List<Element> getChildElements() {
       ArrayList<Element> filtered = new ArrayList<Element>();
       if (_children != null) {
          for (Object child : _children) {
             if (child instanceof Element) {
                Element e = (Element) child;
                filtered.add((Element) e.clone());
             }
          }
       }
 
       return filtered;
    }
 
    /**
     * Gets a list of all contained elements and text snippets. The returned
     * list is modifiable by the caller.
     *
     * @return
     *    a {@link List} containing all child elements and text snippets;
     *    each item in the list is either an <code>Element</code> instance
     *    or a {@link String} instance; never <code>null</code>.
     *
     * @since XINS 3.0
     */
    public List getChildren() {
       ArrayList result = new ArrayList();;
       if (_children != null) {
          for (Object child : _children) {
             if (child instanceof Element) {
                Element e = (Element) child;
                result.add(e.clone());
             } else {
                result.add(child);
             }
          }
       }
 
       return result;
    }
 
    /**
     * Counts the number of children (both elements and text snippets).
     *
     * @return
     *    the number of children, always &gt;= 0.
     *
     * @since XINS 3.0
     */
    public int getChildCount() {
       return _children == null ? 0 : _children.size();
    }
 
    /**
     * Produces a list of child elements that match the specified name.
     *
     * <p>The returned {@link List} is (since XINS 2.2) modifiable by the
     * caller.
     *
     * @param name
     *    the name for the child elements to match, cannot be
     *    <code>null</code>.
     *
     * @return
     *    a modifiable {@link List} containing each child {@link Element} that
     *    matches the specified local name; never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public List<Element> getChildElements(String name)
    throws IllegalArgumentException {
 
       // TODO: Support namespaces
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       ArrayList result = new ArrayList();
 
       // Add all matching elements to the result
       if (_children != null) {
          for (Object child : _children) {
             if (child instanceof Element) {
                Element e = (Element) child;
                if (name.equals(e.getLocalName())) {
                   result.add(e.clone());
                }
             }
          }
       }
 
       return result;
    }
 
    /**
     * Sets the character content. All existing character content, if any, is
     * replaced and the text is added at the end of all current elements.
     *
     * @param text
     *    the character content for this element, or <code>null</code>.
     *
     * @since XINS 2.0
     *
     * @deprecated
     *    Since XINS 3.0, use {@link #add(String)} instead.
     */
    @Deprecated
    public void setText(String text) {
 
       // Remove all existing text snippets, if any
       if (_text != null && _children != null) {
          Iterator iterator = _children.iterator();
          while (iterator.hasNext()) {
             Object child = iterator.next();
             if (child instanceof String) {
                iterator.remove();
             }
          }
       }
 
       // Reset the contained text
       _text = null;
 
       // Add a text snippet at the end
       if (text != null) {
          add(text);
       }
    }
 
    /**
     * Adds character content behind all currently contained character content
     * and elements.
     *
     * @param text
     *    the text to add, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>text == null</code>.
     *
     * @since XINS 2.2
     *
     * @deprecated
     *    Since XINS 3.0, use {@link #add(String)} instead.
     */
    public void addText(String text)
    throws IllegalArgumentException {
       add(text);
    }
 
    /**
     * Adds character content behind all currently contained character content
     * and elements.
     *
     * @param text
     *    the text to add, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>text == null</code>.
     *
     * @since XINS 3.0
     */
    public void add(String text)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("text", text);
 
       // Short-circuit if there is actually no text
       if (text.length() < 1) {
          return;
       }
 
       // Lazily initialize _children
       if (_children == null) {
          _children = new ArrayList();
       }
 
       // Add a String child to _children and update _text
       if (_children.size() > 0) {
          int lastChildIndex = _children.size() - 1;
          Object lastChild = _children.get(lastChildIndex);
          if (lastChild instanceof String) {
             String newLastChild = ((String) lastChild) + text;
             _children.set(lastChildIndex, newLastChild);
          } else {
             _children.add(text);
          }
       } else {
          _children.add(text);
       }
       _text = (_text == null) ? text : _text + text;
    }
 
    /**
     * Gets the (combined) character content, if any.
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
 
       Element child = getOptionalChildElement(elementName);
       if (child == null) {
          String message = (elementName == null)
                         ? "No elements found inside \"" + getLocalName() + "\" element."
                         : "No \"" + elementName + "\" elements found inside \"" + getLocalName() + "\" element.";
          throw new ParseException(message);
       }
 
       return child;
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
    public Element getUniqueChildElement()
    throws ParseException {
       return getUniqueChildElement(null);
    }
 
    /**
     * Gets an optional child of this element with the specified name.
     *
     * @param elementName
     *    the name of the child element to get,
     *    or <code>null</code> if the element name is irrelevant.
     *
     * @return
     *    the sub-element of this element,
     *    or <code>null</code> if there was no match.
     *
     * @throws ParseException
     *    if more than one child matching the specified name (if any) was found.
     *
     * @since XINS 3.0
     */
    public Element getOptionalChildElement(String elementName)
    throws ParseException {
 
       // Get all matching child elements
       List childList = (elementName == null)
                      ? getChildElements()
                      : getChildElements(elementName);
 
       // If there are no matches, then return 0
       if (childList.size() == 0) {
          return null;
 
       // If there are multiple matches, then throw an exception
       } else if (childList.size() > 1) {
          String message = (elementName == null)
                         ? "Multiple elements found inside \"" + getLocalName() + "\" element."
                         : "Multiple \"" + elementName + "\" elements found inside \"" + getLocalName() + "\" element.";
          throw new ParseException(message);
       }
 
       // There is exactly one child, return it
       return (Element) childList.get(0);
    }
 
    /**
     * Gets an optional child of this element.
     *
     * @return
     *    the sub-element of this element,
     *    or <code>null</code> if this element has no child elements.
     *
     * @throws ParseException
     *    if more than one child element was found.
     *
     * @since XINS 3.0
     */
    public Element getOptionalChildElement()
    throws ParseException {
       return getOptionalChildElement(null);
    }
 
    @Override
    public int hashCode() {
       int hashCode = _localName.hashCode();
       if (_namespaceURI != null) {
          hashCode ^= _namespaceURI.hashCode();
       }
       if (_attributes != null) {
          hashCode ^= _attributes.hashCode();
       }
       if (_children != null) {
          hashCode ^= _children.hashCode();
       }
       return hashCode;
    }
 
    @Override
    public boolean equals(Object obj) {
       return describeDiffs(obj) == null;
    }
 
    /**
     * Describes the differences between this object and the argument. This is
    * an equivalent to the {@link #equals()} method that returns a
     * human-readable description of the differences. If there are no
     * differences, then <code>null</code> is returned.
     *
     * @param obj
     *    the object to compare with, can be <code>null</code>.
     *
     * @return
     *    a human-readable description of the differences,
     *    or <code>null</code> if and only if there are no differences
     *    (hence: these objects should be considered equal).
     *
     * @since XINS 3.0
     */
    public String describeDiffs(Object obj) {
 
       // The argument cannot be null...
       if (obj == null) {
          return "obj == null";
 
       // ...it must also be an Element
       } else if (! (obj instanceof Element)) {
          return "obj is not an instance of class " + Element.class.getName() + " but an instance of class " + obj.getClass().getName();
       }
       Element that = (Element) obj;
 
       // Check the local name
       if (! _localName.equals(that._localName)) {
          return "this local name is " + TextUtils.quote(_localName) + "; other local name is " + TextUtils.quote(that._localName);
       }
 
       // Check the namespace
       if ((_namespaceURI != null && !_namespaceURI.equals(that._namespaceURI)) || (_namespaceURI == null && that._namespaceURI != null)) {
          return "this namespace URI is " + TextUtils.quote(_namespaceURI) + "; other namespace URI is " + TextUtils.quote(that._namespaceURI);
       }
 
       // Check the attributes
       if (getAttributeCount() != that.getAttributeCount()) {
          return "this element contains " + getAttributeCount() + " attributes (" + TextUtils.list(getAttributeMap().keySet(), ", ", " and ", true) + "); other element contains " + that.getAttributeCount() + " attributes (" + TextUtils.list(that.getAttributeMap().keySet(), ", ", " and ", true);
       } else if (getAttributeCount() > 0) {
          for (Map.Entry entry : (Set<Map.Entry>) _attributes.entrySet()) {
             Object       key = entry.getKey();
             Object thisValue = entry.getValue();
             Object thatValue = that._attributes.get(key);
             if (! thisValue.equals(thatValue)) {
                return "attribute " + TextUtils.quote(key) + " on this element is " + TextUtils.quote(thisValue) + "; on other element it is " + TextUtils.quote(thatValue);
             }
          }
       }
 
       // Check the child elements
       // NOTE: The order of the children matters!
       int childCount = getChildCount();
       if (childCount != that.getChildCount()) {
          return "this element has " + childCount + " child(ren); other element has " + that.getChildCount();
       }
       for (int i = 0; i < childCount; i++) {
          Object thisChild =      _children.get(i);
          Object thatChild = that._children.get(i);
 
          if (thisChild instanceof Element && thatChild instanceof Element) {
             String diff = ((Element) thisChild).describeDiffs(thatChild);
             if (diff != null) {
                return "child " + i + " in this element is different from the one in the other element: " + diff;
             }
          } else if (! thisChild.equals(thatChild)) {
             return "child " + i + " in this element is " + TextUtils.quote(thisChild) + " while in the other element it is " + TextUtils.quote(thatChild);
          }
       }
 
       return null;
    }
 
    /**
     * Clones this object. The clone will have the same namespace URI and local
     * name and equivalent attributes, children and character content.
     *
     * @return
     *    a new clone of this object, never <code>null</code>.
     */
    @Override
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
     * the element as its XML representation. Invoking this method on an object
     * <code>e</code> is equivalent to calling:
     *
     * <blockquote><pre>new {@linkplain ElementSerializer}.{@linkplain ElementSerializer#serialize(Element) serialize}(e)</pre></blockquote>
     *
     * @return
     *    the XML representation of this element without the XML declaration,
     *    never <code>null</code>.
     */
    @Override
    public String toString() {
       return new ElementSerializer().serialize(this);
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
        * @since XINS 2.1
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
          _localName = localName;
       }
 
       @Override
       public String toString() {
          return "Element.QualifiedName(namespacePrefix=" + TextUtils.quote(_namespacePrefix) + "; namespaceURI=" + TextUtils.quote(_namespaceURI) + "; localName=" + TextUtils.quote(_localName) + ')';
       }
 
       @Override
       public int hashCode() {
          return _localName.hashCode();
       }
 
       @Override
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
        * @since XINS 2.1
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
