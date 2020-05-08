 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package searchtree;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  *
  * @author Fedor Uvarychev
  */
 public class SearchTree {
 
     public SearchTree() {
         this.top = new TreeElement(0);
     }
 
     /**
      *
      * @return number of elements of the tree.
      */
     public int sizeOfTree() {
         return this.count;
     }
 
     public SearchTreeIterator iterator() {
         return new SearchTreeIterator();
     }
 
     /**
      * Inserts a given value into the tree.
      *
      * @param value
      */
     public void add(int value) {
         if (0 == this.count) {
             this.top.value = value;
             count++;
         } else {
             this.top.insert(value);
         }
     }
 
     /**
      * Learns, if the given number belongs to search tree.
      *
      * @param value
      * @return 'true', if element was found, and 'false' otherwise.
      */
     public boolean find(int value) {
         return (null != this.findPosition(value));
     }
 
     /**
      * Tries to establish a position of given value, if it's indeed in the tree.
      *
      * @param value
      * @return a position in tree, or in case of fail -- null.
      */
     private TreeElement findPosition(int value) {
         if (0 != this.count) {
             TreeElement temp = this.top;
             while (null != temp && value != temp.value) {
                 if (value < temp.value) {
                     temp = temp.leftSon;
                 } else {
                     temp = temp.rightSon;
                 }
             }
             return temp;
         } else {
             return null;
         }
     }
 
     /**
      * Tries to delete a given value from the tree.
      *
      * @param value
      */
     public void delete(int value) {
         TreeElement victim = this.findPosition(value);
         if (null == victim) {
             // do nothing.
         } else {
             if (null != victim.leftSon) {
                 TreeElement temp = this.theClosestLeftSon(victim);
                 victim.value = temp.value;
                 temp.delete();
             } else if (null != victim.rightSon) {
                 TreeElement temp = this.theClosestRightSon(victim);
                 victim.value = temp.value;
                 temp.delete();
             } else {
                 victim.delete();
             }
         }
     }
 
     /**
      * Function, returning the closest descendant of given node from its left
      * branch.
      *
      * @param element
      * @return null if there's no branch, and the true descendant otherwise.
      */
     private TreeElement theClosestLeftSon(TreeElement element) {
         TreeElement temp = element.leftSon;
         if (temp == null) {
             return null;
         }
         while (temp.rightSon != null) {
             temp = temp.rightSon;
         }
         return temp;
     }
 
     /**
      * Function, returning the closest descendant of given node from its right
      * branch.
      *
      * @param element
      * @return null if there's no branch, and the true descendant otherwise.
      */
     private TreeElement theClosestRightSon(TreeElement element) {
         TreeElement temp = element.rightSon;
         if (temp == null) {
             return null;
         }
         while (temp.leftSon != null) {
             temp = temp.leftSon;
         }
         return temp;
     }
 
     public class SearchTreeIterator implements Iterator<Integer> {
 
         private SearchTreeIterator() {
             this.stack = new LinkedList<>();
             this.stack.add(top);
         }
 
         /**
          * Implementation of method from Iterator interface.
          * 
          * @return 
          */
         @Override
         public boolean hasNext() {
             return (stack.size() > 0 && 0 != count);
         }
 
         /**
          * Implementation of method from Iterator interface.
          * @return an integer value.
          */
         @Override
         public Integer next() {
            TreeElement temp = (TreeElement) stack.removeLast();
             if (null != temp.rightSon) {
                 stack.add(temp.rightSon);
             }
             if (null != temp.leftSon) {
                 stack.add(temp.leftSon);
             }
 
             return temp.value;
         }
 
         /**
          * This method is not supported in this version yet.
          */
         @Override
         public void remove() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
         private LinkedList<TreeElement> stack;
     }
 
     /**
      * Class which corresponds to element of the tree.
      */
     private class TreeElement {
 
         private TreeElement(int value) {
             this.value = value;
         }
 
         /**
          * Function recursively finding the place where the insertion is
          * possible.
          *
          * @param value
          */
         private void insert(int value) {
             if (value < this.value) {
                 if (this.leftSon == null) {
                     this.leftSon = new TreeElement(value);
                     this.leftSon.batya = this;
                     count++;
                 } else {
                     this.leftSon.insert(value);
                 }
             } else if (value > this.value) {
                 if (this.rightSon == null) {
                     this.rightSon = new TreeElement(value);
                     this.rightSon.batya = this;
                     count++;
                 } else {
                     this.rightSon.insert(value);
                 }
             } else {
             }
         }
 
         /**
          * Deletes a node if the tree, must be applied to leaves only.
          */
         private void delete() {
             if (top == this) {
                 top.value = 0;
             } else {
                 if (this == this.batya.leftSon) {
                     this.batya.leftSon = null;
                 } else {
                     this.batya.rightSon = null;
                 }
             }
             count--;
         }
         private int value;
         private TreeElement batya;
         private TreeElement leftSon;
         private TreeElement rightSon;
     }
     private int count;
     private TreeElement top;
 }
