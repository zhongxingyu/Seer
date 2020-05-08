 package com.hjfreyer.util;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.common.base.Join;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 public class Tree<T> {
   private final Object node;
 
   public static <T> Tree<T> leaf(T prim) {
     return new Tree<T>(prim);
   }
 
   public static <T> Tree<T> inode(List<Tree<T>> list) {
     return new Tree<T>(list);
   }
 
   public static <T> Tree<T> inode(Tree<T>... list) {
     return inode(Arrays.asList(list));
   }
 
   @SuppressWarnings("unchecked")
   public static <T> Tree<T> copyOf(Object obj) {
     if (obj instanceof List<?>) {
       List<Tree<T>> result = Lists.newLinkedList();
 
       for (Object child : (List<Object>) obj) {
         result.add(Tree.<T> copyOf(child));
       }
 
       return Tree.inode(ImmutableList.copyOf(result));
     }
 
     return Tree.leaf((T) obj);
   }
 
   private Tree(Object node) {
     this.node = node;
   }
 
   public boolean isLeaf() {
     return !(node instanceof List<?>);
   }
 
   @SuppressWarnings("unchecked")
   public List<Tree<T>> asList() {
     return (List<Tree<T>>) node;
   }
 
   @SuppressWarnings("unchecked")
   public T asLeaf() {
     return (T) node;
   }
 
   public Object getNode() {
     return node;
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((node == null) ? 0 : node.hashCode());
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj)
       return true;
     if (obj == null)
       return false;
     if (getClass() != obj.getClass())
       return false;
    Tree other = (Tree) obj;
     if (node == null) {
       if (other.node != null)
         return false;
     } else if (!node.equals(other.node))
       return false;
     return true;
   }
 
   @Override
   public String toString() {
     if (isLeaf()) {
       return "Tree.leaf(" + node + ")";
     }
 
     return "Tree.inode(" + Join.join(", ", asList()) + ")";
   }
 }
