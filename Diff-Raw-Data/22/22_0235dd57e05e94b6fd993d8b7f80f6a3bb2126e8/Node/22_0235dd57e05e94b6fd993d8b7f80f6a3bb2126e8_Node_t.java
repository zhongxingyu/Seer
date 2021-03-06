 /*
  *  Java HTML Tidy - JTidy
  *  HTML parser and pretty printer
  *
  *  Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
  *  Institute of Technology, Institut National de Recherche en
  *  Informatique et en Automatique, Keio University). All Rights
  *  Reserved.
  *
  *  Contributing Author(s):
  *
  *     Dave Raggett <dsr@w3.org>
  *     Andy Quick <ac.quick@sympatico.ca> (translation to Java)
  *     Gary L Peskin <garyp@firstech.com> (Java development)
  *     Sami Lempinen <sami@lempinen.net> (release management)
  *     Fabrizio Giustina <fgiust at users.sourceforge.net>
  *
  *  The contributing author(s) would like to thank all those who
  *  helped with testing, bug fixes, and patience.  This wouldn't
  *  have been possible without all of you.
  *
  *  COPYRIGHT NOTICE:
  * 
  *  This software and documentation is provided "as is," and
  *  the copyright holders and contributing author(s) make no
  *  representations or warranties, express or implied, including
  *  but not limited to, warranties of merchantability or fitness
  *  for any particular purpose or that the use of the software or
  *  documentation will not infringe any third party patents,
  *  copyrights, trademarks or other rights. 
  *
  *  The copyright holders and contributing author(s) will not be
  *  liable for any direct, indirect, special or consequential damages
  *  arising out of any use of the software or documentation, even if
  *  advised of the possibility of such damage.
  *
  *  Permission is hereby granted to use, copy, modify, and distribute
  *  this source code, or portions hereof, documentation and executables,
  *  for any purpose, without fee, subject to the following restrictions:
  *
  *  1. The origin of this source code must not be misrepresented.
  *  2. Altered versions must be plainly marked as such and must
  *     not be misrepresented as being the original source.
  *  3. This Copyright notice may not be removed or altered from any
  *     source or altered source distribution.
  * 
  *  The copyright holders and contributing author(s) specifically
  *  permit, without fee, and encourage the use of this source code
  *  as a component for supporting the Hypertext Markup Language in
  *  commercial products. If you use this source code in a product,
  *  acknowledgment is not required but would be appreciated.
  *
  */
 package org.w3c.tidy;
 
 import org.w3c.tidy.Options.DupAttrModes;
 
 /**
  * Used for elements and text nodes element name is null for text nodes start and end are offsets into lexbuf which
  * contains the textual content of all elements in the parse tree. Parent and content allow traversal of the parse tree
  * in any direction. attributes are represented as a linked list of AttVal nodes which hold the strings for
  * attribute/value pairs.
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public class Node {
 	static enum NodeType {
 		RootNode, DocTypeTag, CommentTag, ProcInsTag, TextNode, StartTag, EndTag, StartEndTag, CDATATag, SectionTag,
 		AspTag, JsteTag, PhpTag, XmlDecl
 	}
 	
     /**
      * parent node.
      */
     protected Node parent;
 
     /**
      * previous node.
      */
     protected Node prev;
 
     /**
      * next node.
      */
     protected Node next;
 
     /**
      * last node.
      */
     protected Node last;
 
     /**
      * start of span onto text array.
      */
     protected int start;
 
     /**
      * end of span onto text array.
      */
     protected int end;
 
     /**
      * the text array.
      */
     protected byte[] textarray;
 
     /**
      * TextNode, StartTag, EndTag etc.
      */
     protected NodeType type;
     
     protected int line;
     protected int column;
 
     /**
      * true if closed by explicit end tag.
      */
     protected boolean closed;
 
     /**
      * true if inferred.
      */
     protected boolean implicit;
 
     /**
      * true if followed by a line break.
      */
     protected boolean linebreak;
 
     /**
      * old tag when it was changed.
      */
     protected Dict was;
 
     /**
      * tag's dictionary definition.
      */
     protected Dict tag;
 
     /**
      * Tag name.
      */
     protected String element;
 
     /**
      * Attribute/Value linked list.
      */
     protected AttVal attributes;
 
     /**
      * Contained node.
      */
     protected Node content;
 
     /**
      * DOM adapter.
      */
     protected org.w3c.dom.Node adapter;
 
     /**
      * Instantiates a new text node.
      */
     public Node()
     {
         this(NodeType.TextNode, null, 0, 0);
     }
 
     /**
      * Instantiates a new node.
      * @param type node type
      * @param textarray array of bytes contained in the Node
      * @param start start position
      * @param end end position
      */
     public Node(NodeType type, byte[] textarray, int start, int end)
     {
         this.parent = null;
         this.prev = null;
         this.next = null;
         this.last = null;
         this.start = start;
         this.end = end;
         this.textarray = textarray;
         this.type = type;
         this.closed = false;
         this.implicit = false;
         this.linebreak = false;
         this.was = null;
         this.tag = null;
         this.element = null;
         this.attributes = null;
         this.content = null;
     }
 
     /**
      * Instantiates a new node.
      * @param type node type
      * @param textarray array of bytes contained in the Node
      * @param start start position
      * @param end end position
      * @param element tag name
      * @param tt tag table instance
      */
     public Node(NodeType type, byte[] textarray, int start, int end, String element, TagTable tt)
     {
         this.parent = null;
         this.prev = null;
         this.next = null;
         this.last = null;
         this.start = start;
         this.end = end;
         this.textarray = textarray;
         this.type = type;
         this.closed = false;
         this.implicit = false;
         this.linebreak = false;
         this.was = null;
         this.tag = null;
         this.element = element;
         this.attributes = null;
         this.content = null;
         if (type == NodeType.StartTag || type == NodeType.StartEndTag || type == NodeType.EndTag)
         {
             tt.findTag(this);
         }
     }
 
     /**
      * Returns an attribute with the given name in the current node.
      * @param name attribute name.
      * @return AttVal instance or null if no attribute with the iven name is found
      */
     public AttVal getAttrByName(String name)
     {
         AttVal attr;
 
         for (attr = this.attributes; attr != null; attr = attr.next)
         {
             if (name != null && attr.attribute != null && attr.attribute.equals(name))
             {
                 break;
             }
         }
 
         return attr;
     }
     
     public AttVal getAttrById(final AttrId id) {
 		for (AttVal av = attributes; av != null; av = av.next) {
 			if (av.hasId(id)) {
 				return av;
 			}
 		}
 		return null;
 	}
 
     /**
      * Default method for checking an element's attributes.
      * @param lexer Lexer
      */
     public void checkAttributes(Lexer lexer)
     {
         AttVal attval;
 
         for (attval = this.attributes; attval != null; attval = attval.next)
         {
             attval.checkAttribute(lexer, this);
         }
     }
 
     /**
      * The same attribute name can't be used more than once in each element. Discard or join attributes according to
      * configuration.
      * @param lexer Lexer
      */
     public void repairDuplicateAttributes(Lexer lexer) {
         for (AttVal first = this.attributes; first != null;) {
         	if (!(first.asp == null && first.php == null)) {
         		first = first.next;
         		continue;
         	}
         	boolean firstRedefined = false;
         	
             for (AttVal second = first.next; second != null;) {
                 if (!(second.asp == null && second.php == null && first.attribute != null
                 		&& first.attribute.equalsIgnoreCase(second.attribute))) {
                 	second = second.next;
                 	continue;
                 }
                 
                 AttVal temp;
 
                 if ("class".equalsIgnoreCase(second.attribute) && lexer.configuration.isJoinClasses())
                 {
                     // concatenate classes
                     second.value = second.value + " " + first.value;
 
                     temp = first.next;
 
                     if (temp.next == null)
                     {
                         second = null;
                     }
                     else
                     {
                         second = second.next;
                     }
 
                     lexer.report.attrError(lexer, this, first, ErrorCode.JOINING_ATTRIBUTE);
 
                     removeAttribute(first);
                     first = temp;
                 }
                 else if ("style".equalsIgnoreCase(second.attribute) && lexer.configuration.isJoinStyles())
                 {
                     // concatenate styles
 
                     // this doesn't handle CSS comments and leading/trailing white-space very well see
                     // http://www.w3.org/TR/css-style-attr
 
                     int end = second.value.length() - 1;
 
                     if (second.value.charAt(end) == ';')
                     {
                         // attribute ends with declaration seperator
                         second.value = second.value + " " + first.value;
                     }
                     else if (second.value.charAt(end) == '}')
                     {
                         // attribute ends with rule set
                         second.value = second.value + " { " + first.value + " }";
                     }
                     else
                     {
                         // attribute ends with property value
                         second.value = second.value + "; " + first.value;
                     }
 
                     temp = first.next;
 
                     if (temp.next == null)
                     {
                         second = null;
                     }
                     else
                     {
                         second = second.next;
                     }
 
                     lexer.report.attrError(lexer, this, first, ErrorCode.JOINING_ATTRIBUTE);
 
                     removeAttribute(first);
                     first = temp;
 
                 }
                 else if (lexer.configuration.getDuplicateAttrs() == DupAttrModes.KeepLast)
                 {
                     temp = first.next;
                     lexer.report.attrError(lexer, this, first, ErrorCode.REPEATED_ATTRIBUTE);
                     removeAttribute(first);
                     firstRedefined = true;
                     first = temp;
                     second = second.next;
                 } else {
                     temp = second.next;
                     lexer.report.attrError(lexer, this, second, ErrorCode.REPEATED_ATTRIBUTE);
                     removeAttribute(second);
                     second = temp;
                 }
             }
             if (!firstRedefined) {
             	first = first.next;
             }
         }
     }
     
     AttVal repairAttrValue(final String name, final String value) {
         final AttVal old = getAttrByName(name);
         if (old != null) {
         	old.value = value;
             return old;
         } else {
             return addAttribute(name, value);
         }
     }
 
 	protected void insertAttributeAtEnd(final AttVal av) {
 		attributes = AttVal.addAttrToList(attributes, av);
 	}
 
     /**
      * Adds an attribute to the node.
      * @param name attribute name
      * @param value attribute value
      * @return 
      */
     public AttVal addAttribute(final String name, final String value) {
         final AttVal av = new AttVal(null, null, null, null, '"', name, value);
         av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(av);
         insertAttributeAtEnd(av);
         return av;
     }
 
     /**
      * Remove an attribute from node and then free it.
      * @param attr attribute to remove
      */
     public void removeAttribute(AttVal attr)
     {
         AttVal av;
         AttVal prev = null;
         AttVal next;
 
         for (av = this.attributes; av != null; av = next)
         {
             next = av.next;
 
             if (av == attr)
             {
                 if (prev != null)
                 {
                     prev.next = next;
                 }
                 else
                 {
                     this.attributes = next;
                 }
             }
             else
             {
                 prev = av;
             }
         }
     }
 
     /**
      * Find the doctype element.
      * @return doctype node or null if not found
      */
     public Node findDocType()
     {
         Node node = this.content;
 
         while (node != null && node.type != NodeType.DocTypeTag)
         {
             node = node.next;
         }
 
         return node;
     }
 
     /**
      * Discard the doctype node.
      */
     public void discardDocType()
     {
         Node node;
 
         node = findDocType();
         if (node != null)
         {
             if (node.prev != null)
             {
                 node.prev.next = node.next;
             }
             else
             {
                 node.parent.content = node.next;
             }
 
             if (node.next != null)
             {
                 node.next.prev = node.prev;
             }
 
             node.next = null;
         }
     }
 
     /**
      * Remove node from markup tree and discard it.
      * @param element discarded node
      * @return next node
      */
     public static Node discardElement(Node element)
     {
         Node next = null;
 
         if (element != null)
         {
             next = element.next;
             element.removeNode();
         }
 
         return next;
     }
 
     /**
      * Insert a node into markup tree.
      * @param node to insert
      */
     public void insertNodeAtStart(Node node)
     {
         node.parent = this;
 
         if (this.content == null)
         {
             this.last = node;
         }
         else
         {
             this.content.prev = node; // AQ added 13 Apr 2000
         }
 
         node.next = this.content;
         node.prev = null;
         this.content = node;
     }
 
     /**
      * Insert node into markup tree.
      * @param node Node to insert
      */
     public void insertNodeAtEnd(Node node)
     {
         node.parent = this;
         node.prev = this.last;
 
         if (this.last != null)
         {
             this.last.next = node;
         }
         else
         {
             this.content = node;
         }
 
         this.last = node;
     }
 
     /**
      * Insert node into markup tree in pace of element which is moved to become the child of the node.
      * @param element child node. Will be inserted as a child of element
      * @param node parent node
      */
     public static void insertNodeAsParent(Node element, Node node)
     {
         node.content = element;
         node.last = element;
         node.parent = element.parent;
         element.parent = node;
 
         if (node.parent.content == element)
         {
             node.parent.content = node;
         }
 
         if (node.parent.last == element)
         {
             node.parent.last = node;
         }
 
         node.prev = element.prev;
         element.prev = null;
 
         if (node.prev != null)
         {
             node.prev.next = node;
         }
 
         node.next = element.next;
         element.next = null;
 
         if (node.next != null)
         {
             node.next.prev = node;
         }
     }
 
     /**
      * Insert node into markup tree before element.
      * @param element child node. Will be insertedbefore element
      * @param node following node
      */
     public static void insertNodeBeforeElement(Node element, Node node)
     {
         Node parent;
 
         parent = element.parent;
         node.parent = parent;
         node.next = element;
         node.prev = element.prev;
         element.prev = node;
 
         if (node.prev != null)
         {
             node.prev.next = node;
         }
 
         if (parent != null && parent.content == element)
         {
             parent.content = node;
         }
     }
 
     /**
      * Insert node into markup tree after element.
      * @param node new node to insert
      */
     public void insertNodeAfterElement(Node node)
     {
         Node parent;
 
         parent = this.parent;
         node.parent = parent;
 
         // AQ - 13Jan2000 fix for parent == null
         if (parent != null && parent.last == this)
         {
             parent.last = node;
         }
         else
         {
             node.next = this.next;
             // AQ - 13Jan2000 fix for node.next == null
             if (node.next != null)
             {
                 node.next.prev = node;
             }
         }
 
         this.next = node;
         node.prev = this;
     }
 
     /**
      * Trim an empty element.
      * @param lexer Lexer
      * @param element empty node to be removed
      */
     public static Node trimEmptyElement(final Lexer lexer, final Node element) {
     	if (lexer.canPrune(element)) {
             if (element.type != NodeType.TextNode) {
                 lexer.report.warning(lexer, element, null, ErrorCode.TRIM_EMPTY_ELEMENT);
             }
             return discardElement(element);
         }
         return element.next;
     }
 
     /**
      * This maps <em> hello </em> <strong>world </strong> to <em> hello </em> <strong>world </strong>. If last child of
      * element is a text node then trim trailing white space character moving it to after element's end tag.
      * @param lexer Lexer
      * @param element node
      * @param last last child of element
      */
     public static void trimTrailingSpace(Lexer lexer, Node element, Node last)
     {
         byte c;
 
         if (last != null && last.type == NodeType.TextNode)
         {
             if (last.end > last.start)
 
             {
                 c = lexer.lexbuf[last.end - 1];
 
                 if (c == 160 || c == (byte) ' ')
                 {
                     // take care with <td> &nbsp; </td>
                     // fix for [435920]
                     if (c == 160 && (element.is(TagId.TD) || element.is(TagId.TH)))
                     {
                         if (last.end > last.start + 1)
                         {
                             last.end -= 1;
                         }
                     }
                     else
                     {
                         last.end -= 1;
 
                         if (TidyUtils.toBoolean(element.tag.model & Dict.CM_INLINE)
                             && !TidyUtils.toBoolean(element.tag.model & Dict.CM_FIELD))
                         {
                             lexer.insertspace = true;
                         }
                     }
                 }
             }
             // if empty string then delete from parse tree
             if (last.start == last.end) // COMMENT_NBSP_FIX: && tag != tag_td && tag != tag_th
             {
                 trimEmptyElement(lexer, last);
             }
         }
     }
 
     /**
      * Escapes the given tag.
      * @param lexer Lexer
      * @param element node to be escaped
      * @return escaped node
      */
     protected static Node escapeTag(Lexer lexer, Node element)
     {
         Node node = lexer.newNode();
         node.start = lexer.lexsize;
         node.textarray = element.textarray; // @todo check it
         lexer.addByte('<');
 
         if (element.type == NodeType.EndTag)
         {
             lexer.addByte('/');
         }
 
         if (element.element != null)
         {
             lexer.addStringLiteral(element.element);
         }
         else if (element.type == NodeType.DocTypeTag)
         {
             int i;
 
             lexer.addByte('!');
             lexer.addByte('D');
             lexer.addByte('O');
             lexer.addByte('C');
             lexer.addByte('T');
             lexer.addByte('Y');
             lexer.addByte('P');
             lexer.addByte('E');
             lexer.addByte(' ');
 
             for (i = element.start; i < element.end; ++i)
             {
                 lexer.addByte(lexer.lexbuf[i]);
             }
         }
 
         if (element.type == NodeType.StartEndTag)
         {
             lexer.addByte('/');
         }
 
         lexer.addByte('>');
         node.end = lexer.lexsize;
 
         return node;
     }
 
     /**
      * Is the node content empty or blank? Assumes node is a text node.
      * @param lexer Lexer
      * @return <code>true</code> if the node content empty or blank
      */
     public boolean isBlank(Lexer lexer)
     {
         if (this.type == NodeType.TextNode)
         {
             if (this.end == this.start)
             {
                 return true;
             }
             if (this.end == this.start + 1 && lexer.lexbuf[this.end - 1] == ' ')
             {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * This maps <code>&lt;p> hello &lt;em> world &lt;/em></code> to <code>&lt;p> hello &lt;em> world &lt;/em></code>.
      * Trims initial space, by moving it before the start tag, or if this element is the first in parent's content, then
      * by discarding the space.
      * @param lexer Lexer
      * @param element parent node
      * @param text text node
      */
     public static void trimInitialSpace(Lexer lexer, Node element, Node text)
     {
         Node prev, node;
 
         if (text.type == NodeType.TextNode && lexer.lexbuf[text.start] == (byte) ' ' && (text.start < text.end))
         {
             if (element.hasCM(Dict.CM_INLINE) && !element.hasCM(Dict.CM_FIELD))
             {
                 prev = element.prev;
 
                 if (prev != null && prev.type == NodeType.TextNode)
                 {
                     if (prev.textarray[prev.end - 1] != (byte) ' ')
                     {
                         prev.textarray[prev.end++] = (byte) ' ';
                     }
 
                     ++element.start;
                 }
                 else
                 {
                     // create new node
                     node = lexer.newNode();
                     node.start = element.start++;
                     node.end = element.start;
                     lexer.lexbuf[node.start] = (byte) ' ';
                     Node.insertNodeBeforeElement(element, node);
                 }
             }
 
             // discard the space in current node
             ++text.start;
         }
     }
 
     /**
      * Move initial and trailing space out. This routine maps: hello <em> world </em> to hello <em> world </em> and
      * <em> hello </em> <strong>world </strong> to <em> hello </em> <strong>world </strong>.
      * @param lexer Lexer
      * @param element Node
      */
     public static void trimSpaces(Lexer lexer, Node element)
     {
         Node text = element.content;
 
         if (text != null && text.type == NodeType.TextNode && !element.is(TagId.PRE))
         {
             trimInitialSpace(lexer, element, text);
         }
 
         text = element.last;
 
         if (text != null && text.type == NodeType.TextNode)
         {
             trimTrailingSpace(lexer, element, text);
         }
     }
 
     /**
      * Is this node contained in a given tag?
      */
     public boolean isDescendantOf(final TagId id) {
         for (Node parent = this.parent; parent != null; parent = parent.parent) {
             if (parent.is(id)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * The doctype has been found after other tags, and needs moving to before the html element.
      * @param lexer Lexer
      * @param element document
      * @param doctype doctype node to insert at the beginning of element
      */
    public static void insertDocType(final Lexer lexer, Node element, final Node doctype) {
    	Node existing = lexer.root.findDocType();
    	if (existing != null) {
    		lexer.report.warning(lexer, element, doctype, ErrorCode.DISCARDING_UNEXPECTED);
    	}
    	else {
    		lexer.report.warning(lexer, element, doctype, ErrorCode.DOCTYPE_AFTER_TAGS);
    		while (!element.is(TagId.HTML)) {
    			element = element.parent;
    		}
    		insertNodeBeforeElement(element, doctype);
    	}
     }
 
     /**
      * Find the body node.
      * @return body node
      */
     public Node findBody()
     {
         Node node;
 
         node = this.content;
 
         while (node != null && !node.is(TagId.HTML))
         {
             node = node.next;
         }
 
         if (node == null)
         {
             return null;
         }
 
         node = node.content;
 
         while (node != null && !node.is(TagId.BODY) && !node.is(TagId.FRAMESET))
         {
             node = node.next;
         }
 
         if (node != null && node.is(TagId.FRAMESET))
         {
             node = node.content;
 
             while (node != null && !node.is(TagId.NOFRAMES))
             {
                 node = node.next;
             }
 
             if (node != null)
             {
                 node = node.content;
                 while (node != null && !node.is(TagId.BODY))
                 {
                     node = node.next;
                 }
             }
         }
 
         return node;
     }
 
     /**
      * Is the node an element?
      * @return <code>true</code> if type is START_TAG | START_END_TAG
      */
     public boolean isElement() {
         return this.type == NodeType.StartTag || this.type == NodeType.StartEndTag;
     }
 
     /**
      * Unexpected content in table row is moved to just before the table in accordance with Netscape and IE. This code
      * assumes that node hasn't been inserted into the row.
      * @param row Row node
      * @param node Node which should be moved before the table
      */
     public static void moveBeforeTable(Node row, Node node)
     {
         Node table;
 
         /* first find the table element */
         for (table = row.parent; table != null; table = table.parent)
         {
             if (table.is(TagId.TABLE))
             {
                 if (table.parent.content == table)
                 {
                     table.parent.content = node;
                 }
 
                 node.prev = table.prev;
                 node.next = table;
                 table.prev = node;
                 node.parent = table.parent;
 
                 if (node.prev != null)
                 {
                     node.prev.next = node;
                 }
 
                 break;
             }
         }
     }
 
     /**
      * If a table row is empty then insert an empty cell.This practice is consistent with browser behavior and avoids
      * potential problems with row spanning cells.
      * @param lexer Lexer
      * @param row row node
      */
     public static void fixEmptyRow(Lexer lexer, Node row)
     {
         Node cell;
 
         if (row.content == null)
         {
             cell = lexer.inferredTag(TagId.TD);
             row.insertNodeAtEnd(cell);
             lexer.report.warning(lexer, row, cell, ErrorCode.MISSING_STARTTAG);
         }
     }
 
     /**
      * Coerce a node.
      * @param lexer Lexer
      * @param node Node
      * @param tag tag dictionary reference
      */
     public static void coerceNode(final Lexer lexer, final Node node, final TagId tid, final boolean obsolete,
     		final boolean unexpected) {
     	final Dict tag = lexer.configuration.tt.lookup(tid);
         Node tmp = lexer.inferredTag(tag.id);
         if (obsolete) {
         	lexer.report.warning(lexer, node, tmp, ErrorCode.OBSOLETE_ELEMENT);
         } else if (unexpected) {
         	lexer.report.warning(lexer, node, tmp, ErrorCode.REPLACING_UNEX_ELEMENT);
         } else {
         	lexer.report.warning(lexer, node, tmp, ErrorCode.REPLACING_ELEMENT);
         }
         node.was = node.tag;
         node.tag = tag;
         node.type = NodeType.StartTag;
         node.implicit = true;
         node.element = tag.name;
     }
 
     /**
      * Extract this node and its children from a markup tree.
      */
     public void removeNode()
     {
         if (this.prev != null)
         {
             this.prev.next = this.next;
         }
 
         if (this.next != null)
         {
             this.next.prev = this.prev;
         }
 
         if (this.parent != null)
         {
             if (this.parent.content == this)
             {
                 this.parent.content = this.next;
             }
 
             if (this.parent.last == this)
             {
                 this.parent.last = this.prev;
             }
         }
 
         this.parent = null;
         this.prev = null;
         this.next = null;
     }
 
     /**
      * Insert a node at the end.
      * @param element parent node
      * @param node will be inserted at the end of element
      * @return <code>true</code> if the node has been inserted
      */
     public static boolean insertMisc(Node element, Node node) {
         if (node.type == NodeType.CommentTag
 	            || node.type == NodeType.ProcInsTag
 	            || node.type == NodeType.CDATATag
 	            || node.type == NodeType.SectionTag
 	            || node.type == NodeType.AspTag
 	            || node.type == NodeType.JsteTag
 	            || node.type == NodeType.PhpTag) {
             element.insertNodeAtEnd(node);
             return true;
         }
         
         if (node.type == NodeType.XmlDecl) {
         	Node root = element;
             while (root != null && root.parent != null) {
                 root = root.parent;
             }
             if (root != null && !(root.content != null && root.content.type == NodeType.XmlDecl)) {
             	root.insertNodeAtStart(node);
             	return true;
             }
         }
         
         /* Declared empty tags seem to be slipping through
          ** the cracks.  This is an experiment to figure out
          ** a decent place to pick them up.
          */
         if (node.tag != null && node.isElement() && node.hasCM(Dict.CM_EMPTY) && node.is(TagId.UNKNOWN)
         		&& (node.tag.versions & Versions.VERS_PROPRIETARY) != 0) {
         	element.insertNodeAtEnd(node);
         	return true;
         }
 
         return false;
     }
 
     /**
      * Is this a new (user defined) node? Used to determine how attributes without values should be printed. This was
      * introduced to deal with user defined tags e.g. Cold Fusion.
      * @return <code>true</code> if this node represents a user-defined tag.
      */
     public boolean isNewNode()
     {
         if (this.tag != null)
         {
             return TidyUtils.toBoolean(this.tag.model & Dict.CM_NEW);
         }
 
         return true;
     }
 
     /**
      * Does the node have one (and only one) child?
      * @return <code>true</code> if the node has one child
      */
     public boolean hasOneChild()
     {
         return (this.content != null && this.content.next == null);
     }
 
     /**
      * Find the "html" element.
      * @return html node
      */
     public Node findHTML()
     {
         Node node;
 
         for (node = this.content; node != null && !node.is(TagId.HTML); node = node.next)
         {
             //
         }
 
         return node;
     }
 
     /**
      * Find the head tag.
      * @return head node
      */
     public Node findHEAD()
     {
         Node node;
 
         node = this.findHTML();
 
         if (node != null)
         {
             for (node = node.content; node != null && !node.is(TagId.HEAD); node = node.next)
             {
                 //
             }
         }
 
         return node;
     }
     
     public Node findTITLE() {
         Node node = findHEAD();
         if (node != null) {
             for (node = node.content; node != null && !node.is(TagId.TITLE); node = node.next) {
             	// do nothing
             }
         }
         return node;
     }
 
     /**
      * Checks for node integrity.
      * @return false if node is not consistent
      */
     public boolean checkNodeIntegrity()
     {
         Node child;
 
         if (this.prev != null)
         {
             if (this.prev.next != this)
             {
                 return false;
             }
         }
 
         if (this.next != null)
         {
             if (next == this || this.next.prev != this)
             {
                 return false;
             }
         }
 
         if (this.parent != null)
         {
             if (this.prev == null && this.parent.content != this)
             {
                 return false;
             }
 
             if (this.next == null && this.parent.last != this)
             {
                 return false;
             }
         }
 
         for (child = this.content; child != null; child = child.next)
         {
             if (child.parent != this || !child.checkNodeIntegrity())
             {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Add a css class to the node. If a class attribute already exists adds the value to the existing attribute.
      * @param classname css class name
      */
     public void addClass(String classname)
     {
         AttVal classattr = this.getAttrByName("class");
 
         // if there already is a class attribute then append class name after a space
         if (classattr != null)
         {
             classattr.value = classattr.value + " " + classname;
         }
         else
         {
             // create new class attribute
             this.addAttribute("class", classname);
         }
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @Override
 	public String toString()
     {
         String s = "";
         Node n = this;
 
         while (n != null)
         {
             s += "[Node type=";
             s += n.type;
             s += ",element=";
             if (n.element != null)
             {
                 s += n.element;
             }
             else
             {
                 s += "null";
             }
             if (n.type == NodeType.TextNode || n.type == NodeType.CommentTag || n.type == NodeType.ProcInsTag)
             {
                 s += ",text=";
                 if (n.textarray != null && n.start <= n.end)
                 {
                     s += "\"";
                     s += TidyUtils.getString(n.textarray, n.start, n.end - n.start);
                     s += "\"";
                 }
                 else
                 {
                     s += "null";
                 }
             }
             s += ",content=";
             if (n.content != null)
             {
                 s += n.content.toString();
             }
             else
             {
                 s += "null";
             }
             s += "]";
             if (n.next != null)
             {
                 s += ",";
             }
             n = n.next;
         }
         return s;
     }
 
     /**
      * Returns a DOM Node which wrap the current tidy Node.
      * @return org.w3c.dom.Node instance
      */
     protected org.w3c.dom.Node getAdapter()
     {
         if (adapter == null)
         {
             switch (this.type)
             {
                 case RootNode:
                     adapter = new DOMDocumentImpl(this);
                     break;
                 case StartTag:
                 case StartEndTag:
                     adapter = new DOMElementImpl(this);
                     break;
                 case DocTypeTag:
                     adapter = new DOMDocumentTypeImpl(this);
                     break;
                 case CommentTag:
                     adapter = new DOMCommentImpl(this);
                     break;
                 case TextNode:
                     adapter = new DOMTextImpl(this);
                     break;
                 case CDATATag:
                     adapter = new DOMCDATASectionImpl(this);
                     break;
                 case ProcInsTag:
                     adapter = new DOMProcessingInstructionImpl(this);
                     break;
                 default:
                     adapter = new DOMNodeImpl(this);
             }
         }
         return adapter;
     }
 
     /**
      * Clone this node.
      * @param deep if true deep clone the node (also clones all the contained nodes)
      * @return cloned node
      */
     protected Node cloneNode(boolean deep)
     {
     	Node node = new Node(type, textarray, start, end);
         node.parent = parent;
         node.closed = closed;
         node.implicit = implicit;
         node.tag = tag;
         node.element = element;
         node.line = line;
         node.column = column;
         if (attributes != null) {
         	node.attributes = (AttVal) attributes.clone();
         }
         if (deep)
         {
             Node child;
             Node newChild;
             for (child = this.content; child != null; child = child.next)
             {
                 newChild = child.cloneNode(deep);
                 node.insertNodeAtEnd(newChild);
             }
         }
         return node;
     }
 
     /**
      * Setter for node type.
      * @param newType a valid node type constant
      */
     protected void setType(NodeType newType)
     {
         this.type = newType;
     }
 
     /**
      * Used to check script node for script language.
      * @return <code>true</code> if the script node contains javascript
      */
     public boolean isJavaScript()
     {
         boolean result = false;
         AttVal attr;
 
         if (this.attributes == null)
         {
             return true;
         }
 
         for (attr = this.attributes; attr != null; attr = attr.next)
         {
             if (("language".equalsIgnoreCase(attr.attribute) || "type".equalsIgnoreCase(attr.attribute))
                 && attr.value != null
                 && attr.value.toLowerCase().contains("javascript"))
             {
                 result = true;
             }
         }
 
         return result;
     }
 
     /**
      * Does the node expect contents?
      * @return <code>false</code> if this node should be empty
      */
     public boolean expectsContent()
     {
         if (this.type != NodeType.StartTag)
         {
             return false;
         }
 
         // unknown element?
         if (this.tag == null)
         {
             return true;
         }
 
         if (TidyUtils.toBoolean(this.tag.model & Dict.CM_EMPTY))
         {
             return false;
         }
 
         return true;
     }
     
     public boolean is(final TagId id) {
     	return tag != null && tag.id == id;
     }
     
     public TagId getId() {
     	return tag == null ? TagId.UNKNOWN : tag.id;
     }
 
 	/** May id or name serve as anchor? */    
     public boolean isAnchorElement() {
 		final TagId tid = getId();
 		return tid == TagId.A || tid == TagId.APPLET || tid == TagId.FORM
 				|| tid == TagId.FRAME || tid == TagId.IFRAME
 				|| tid == TagId.IMG || tid == TagId.MAP;
 	}
     
     /** Checks for content model flags */
     public boolean hasCM(final int contentModel) {
     	return tag != null && (tag.model & contentModel) != 0;
     }
     
     public boolean isText() {
     	return type == NodeType.TextNode;
     }
     
     boolean hasMixedContent() {
         for (Node node = content; node != null; node = node.next) {
         	if (node.isText()) {
         		return true;
         	}
         }
         return false;
     }
 
     /** Finds parent container element */
     Node findContainer() {
     	Node node;
         for (node = parent;
               node != null && node.hasCM(Dict.CM_INLINE);
               node = node.parent )
             /**/;
         return node;
     }
     
     protected int getAttributeVersions(final AttVal attval) {
     	if (attval == null || attval.dict == null) {
             return Versions.VERS_UNKNOWN;
     	}
         if (tag == null || tag.attrvers == null) {
             return attval.dict.getVersions();
         }
         if (tag.attrvers.containsKey(attval.dict.id)) {
         	return tag.attrvers.get(attval.dict.id);
         }
         return (attval.dict.getVersions() & Versions.VERS_ALL) != 0
                  ? Versions.VERS_UNKNOWN
                  : attval.dict.getVersions();
     }
     
     /* return the version of the attribute "id" of element "node" */
     protected int getAttributeVersions(final AttrId id) {
         if (tag == null || tag.attrvers == null) {
             return Versions.VERS_UNKNOWN;
         }
         if (tag.attrvers.containsKey(id)) {
         	return tag.attrvers.get(id);
         }
         return Versions.VERS_UNKNOWN;
     }
     
     /* returns true if the element is a W3C defined element */
     /* but the element/attribute combination is not         */
     protected boolean attributeIsProprietary(final AttVal attval) {
         if (attval == null) {
             return false;
         }
         if (tag == null) {
             return false;
         }
         if ((tag.versions & Versions.VERS_ALL) == 0) {
             return false;
         }
         if ((getAttributeVersions(attval) & Versions.VERS_ALL) != 0) {
             return false;
         }
         return true;
     }
 }
