 // Copyright © 2013 Solita Oy <www.solita.fi>
 // This software is released under the MIT License.
 // The license text is at http://opensource.org/licenses/MIT
 
 package fi.solita.datatree;
 
 import java.util.*;
 
 public final class Tree {
 
     private final String name;
     private final List<Meta> metae;
     private final List<Tree> children;
     private final String text;
 
     /**
      * Constructs a tree node. Tree nodes correspond to XML elements.
      *
      * @param name    name of this tree node. Should be a valid XML element name.
      * @param content {@link String}, {@link Tree} or {@link Meta} instances,
     *                or arrays or {@link Collection}s containing them.
      *                The following restrictions apply:
      *                <ul>
      *                <li>At most one {@code String} is allowed</li>
      *                <li>{@code String} and {@code Tree} cannot coexist</li>
      *                </ul>
      */
     public static Tree tree(String name, Object... content) {
         return new Tree(name, Util.flatten(content));
     }
 
     /**
      * Constructs a meta node. Meta nodes correspond to XML attributes.
      */
     public static Meta meta(String name, String value) {
         return new Meta(name, value);
     }
 
     private Tree(String name, Object[] content) {
         Objects.requireNonNull(name, "name must be non-null");
         this.name = name;
         this.metae = Util.filterMeta(content);
         this.children = Util.filterTree(content);
         this.text = Util.filterOneString(content);
         if (!text.isEmpty() && !children.isEmpty()) {
             throw new IllegalArgumentException("Cannot contain both text and trees; " +
                     "had text \"" + text + "\" and children " + children);
         }
     }
 
     public String name() {
         return name;
     }
 
     public List<Meta> metae() {
         return metae;
     }
 
     public String meta(String name) {
         for (Meta meta : metae) {
             if (meta.name().equals(name)) {
                 return meta.value();
             }
         }
         return "";
     }
 
     public List<Tree> children() {
         return children;
     }
 
     public String text() {
         return text;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (!(obj instanceof Tree)) {
             return false;
         }
         Tree that = (Tree) obj;
         return this.name.equals(that.name) &&
                 this.metae.equals(that.metae) &&
                 this.children.equals(that.children) &&
                 this.text.equals(that.text);
     }
 
     @Override
     public int hashCode() {
         int result = name.hashCode();
         result = 31 * result + metae.hashCode();
         result = 31 * result + children.hashCode();
         result = 31 * result + text.hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append('(');
         sb.append(name);
         for (Meta meta : metae) {
             sb.append(' ').append(meta.toString());
         }
         for (Tree child : children) {
             sb.append(' ').append(child.toString());
         }
         if (!text.isEmpty()) {
             sb.append(' ').append('"').append(text).append('"');
         }
         sb.append(')');
         return sb.toString();
     }
 }
