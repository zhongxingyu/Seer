 package edu.x3m.kas.core.structures;
 
 
 /**
  *
  * @author Hans
  */
 public class GroupNode extends AbstractNode {
 
 
     protected AbstractNode left, right;
 
 
 
     public GroupNode (AbstractNode left, AbstractNode right) {
         this.left = left;
         this.right = right;
         this.count = (left == null ? 0 : left.count) + (right == null ? 0 : right.count);
         this.createTree ();
     }
 
 
 
     @Override
     public void append (char c) {
         if (left != null)
             left.append (c);
         if (right != null)
             right.append (c);
     }
 
 
 
     public final void createTree () {
         if (left != null)
             left.append ('0');
         if (right != null)
             right.append ('1');
     }
 
 
 
     @Override
     public String toString () {
         return String.format ("[G %d [L:%s][R:%s]]", count, left, right);
     }
 
 
 
     public void printTree () {
         printTree ("");
     }
 
 
 
     private void printTree (String s) {
         System.out.println (s + "[G]");
         if (left != null) {
             if (left instanceof GroupNode)
                 ((GroupNode) left).printTree (s + "  ");
             else
                 System.out.println (s + "  " + left);
         }
         if (right != null) {
             if (right instanceof GroupNode)
                 ((GroupNode) right).printTree (s + "  ");
             else
                 System.out.println (s + "  " + right);
         }
     }
 
 
 
     @Override
     public SimpleNode find (byte[] data, int from, int pos) {
         if (from + pos >= data.length) return null;
        
         if (data[from + pos] == '0') {
             return left == null ? null : left.find (data, from, pos + 1);
         } else {
             return right == null ? null : right.find (data, from, pos + 1);
         }
     }
 }
