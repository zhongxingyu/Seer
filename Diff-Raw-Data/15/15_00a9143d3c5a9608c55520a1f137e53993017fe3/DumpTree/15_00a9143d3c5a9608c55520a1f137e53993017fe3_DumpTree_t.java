 package com.sonyericsson.chkbugreport.util;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import com.sonyericsson.chkbugreport.Section;
 
 /**
  * DumpTree reads and parses indented dump/log lines, building a tree hierarchy,
  * based on the indentation information.
  */
 public class DumpTree {
 
     private Node mRoot;
 
     public DumpTree(Section sec, int startAt) {
         int count = sec.getLineCount();
         mRoot = new Node(null);
         Node cur = mRoot;
         for (int i = startAt; i < count; i++) {
             String line = sec.getLine(i);
            if (isEmpty(line)) {
                 // Empty lines are ignored for now
                 continue;
             }
             Node node = new Node(line);
 
             // Now we must find a correct place to add this node
             // The logic is simple:
             //  * If the indentation is bigger then the current one's, add as child
             //  * If the indentation is the same as the current one, add as sibling
             //  * Otherwise repeat check with parent
             while (true) {
                 if (node.getIndent() > cur.getIndent()) {
                     cur.add(node);
                     break;
                 } else if (node.getIndent() == cur.getIndent()) {
                     cur.getParent().add(node);
                     break;
                 } else {
                     cur = cur.getParent();
                 }
             }
             cur = node;
         }
     }
 
    private boolean isEmpty(String line) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            if (line.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

     public Node find(String string) {
         return mRoot.find(string);
     }
 
     public class Node implements Iterable<Node> {
         private int mIndent;
         private String mLine;
         private Vector<Node> mChildren = new Vector<DumpTree.Node>();
         private Node mParent;
 
         public Node(String line) {
             if (line != null) {
                 int indent = 0, len = line.length();
                 while (indent < len && line.charAt(indent) == ' ') {
                     indent++;
                 }
                 mIndent = indent;
                 mLine = line.substring(indent);
             } else {
                 mIndent = -1;
             }
         }
 
         public Node find(String string) {
             for (Node child : mChildren) {
                 if (child.getLine().equals(string)) {
                     return child;
                 }
             }
             return null;
         }
 
         public Node getParent() {
             return mParent;
         }
 
         public void add(Node node) {
             mChildren.add(node);
             node.mParent = this;
         }
 
         public int getIndent() {
             return mIndent;
         }
 
         public String getLine() {
             return mLine;
         }
 
         @Override
         public Iterator<Node> iterator() {
             return mChildren.iterator();
         }
 
         public int getChildCount() {
             return mChildren.size();
         }
 
         public Node getChild(int idx) {
             return mChildren.get(idx);
         }
     }
 
 }
