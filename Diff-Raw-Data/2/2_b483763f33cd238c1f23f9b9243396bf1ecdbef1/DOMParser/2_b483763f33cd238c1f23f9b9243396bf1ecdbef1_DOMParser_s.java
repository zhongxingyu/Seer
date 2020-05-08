 package at.yawk.yxml.dom;
 
 import java.io.EOFException;
 import java.io.IOException;
 import at.yawk.yxml.Lexer;
 import at.yawk.yxml.Node;
 import at.yawk.yxml.TagNode;
 import at.yawk.yxml.TagNode.TagType;
 
 public class DOMParser {
     private final Lexer lexer;
     private DOMNode tree = new DOMNode(null);
     
     public DOMParser(Lexer lexer) {
         this.lexer = lexer;
     }
     
     public DOMNode parse() throws IOException {
         try {
             while (tree != null) {
                 walk();
             }
         } catch (EOFException e) {}
         return tree;
     }
     
     private void walk() throws IOException {
         final Node node = lexer.next();
         if (!(node instanceof TagNode) || ((TagNode) node).getType() == TagType.START_END) {
             tree.appendChild(node);
         } else {
             TagType type = ((TagNode) node).getType();
             if (type == TagType.START) {
                 tree = tree.appendChild(new DOMNode(tree, node));
             } else if (type == TagType.END) {
                 final DOMNode sparent = findParent(tree, ((TagNode) node).getTagName());
                 if (sparent == null) {
                     // invalid end tag, add as regular node
                     tree.appendChild(node);
                 } else {
                     tree = sparent.getParent();
                 }
             } else {
                 throw new IllegalStateException();
             }
         }
     }
     
     private static DOMNode findParent(DOMNode tree, String tagType) {
        if (tree == null) {
             return null;
         } else if (((TagNode) tree.getElement()).getTagName().equals(tagType)) {
             return tree;
         } else {
             return findParent(tree.getParent(), tagType);
         }
     }
 }
