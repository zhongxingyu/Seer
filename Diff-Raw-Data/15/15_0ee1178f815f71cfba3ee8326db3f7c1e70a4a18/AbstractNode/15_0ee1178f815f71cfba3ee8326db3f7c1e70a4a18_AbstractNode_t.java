 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich(at)weltraumschaf(dot)de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.ast;
 
 import de.weltraumschaf.jebnf.ast.visitor.Visitor;
 import com.google.common.collect.Maps;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Abstract representation of AST nodes which are not the {@link nodes.Syntax "root node"}
  * but any other kind of {@link Node}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public abstract class AbstractNode implements Node {
 
     /**
      * The direct ancestor in the AST tree.
      */
     private final Node parent;
 
     /**
      * Type to differentiate homogenous nodes.
      */
     private final NodeType type;
 
     /**
      * Map of node attributes.
      */
     private final Map<String, String> attributes = Maps.newHashMap();
 
     /**
      * Initializes the node with it's parent. This is immutable.
      *
      * May the node itself change, it is not possible to set the
      * reference to the parent node.
      *
      * @param parent Ancestor node.
      * @param type Type of node.
      */
     protected AbstractNode(final Node parent, final NodeType type) {
         this.parent = parent;
         this.type   = type;
     }
 
     @Override
     public boolean hasParent() {
         return parent != null && parent.getType() != NodeType.NULL;
     }
 
     @Override
     public Node getParent() {
         return parent;
     }
 
     @Override
     public NodeType getType() {
         return type;
     }
 
     @Override
     public String getNodeName() {
         return getType().toString();
     }
 
     @Override
     public void accept(final Visitor visitor) {
         visitor.beforeVisit(this);
         visitor.visit(this);
         visitor.afterVisit(this);
     }
 
     @Override
     public boolean hasAttributes() {
         return !attributes.isEmpty();
     }
 
     @Override
     public Map<String, String> getAttributes() {
         return Maps.newHashMap(attributes);
     }
 
     @Override
     public boolean hasAttribute(final String name) {
         return attributes.containsKey(name);
     }
 
     @Override
     public String getAttribute(final String name) {
         if (!hasAttribute(name)) {
             throw new IllegalArgumentException(String.format("Does not have attribute with name '%s'!", name));
         }
 
         return attributes.get(name);
     }
 
     @Override
     public void setAttribute(final String name, final String value) {
         attributes.put(name, value);
     }
 
     @Override
     public String toString() {
         final StringBuilder str = new StringBuilder(String.format("<%s", getNodeName().toUpperCase()));
 
         if (hasAttributes()) {
             final Iterator<Map.Entry<String, String>> iterator = attributes.entrySet().iterator();
 
             while (iterator.hasNext()) {
                 final Map.Entry<String, String> pairs = iterator.next();
                 str.append(String.format(" %s=%s", pairs.getKey(), pairs.getValue()));
             }
         }
 
         return str.append('>').toString();
     }
 
     @Override
     public boolean isType(final NodeType checked) {
        return type == checked; // NOPMD Enums may be treated like integers.
     }
 
 }
