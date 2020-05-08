 package de.skuzzle.polly.core.parser.ast.declarations.types.unification;
 
 
 class UnionFindItem<E> {
     final E element;
     UnionFindItem<E> parent;
     
     
     
     public UnionFindItem(E element) {
         this.element = element;
         this.parent = this;
     }
     
     
     
     public boolean isRoot() {
         return this.parent == this;
     }
     
     
     
     public E getValue() {
         return this.element;
     }
     
     
     
     public UnionFindItem<E> newChild(E value) {
         final UnionFindItem<E> result = new UnionFindItem<E>(value);
         result.mergeInto(this);
         return result;
     }
     
     
     
    public UnionFindItem<E> compress() {
         UnionFindItem<E> root = this.parent;
         while (root != root.parent) root = root.parent;
         
         UnionFindItem<E> current = this;
         while (current != root) {
             final UnionFindItem<E> temp = current.parent;
             current.parent = root;
             current = temp;
         }
         return root;
     }
     
     
     
     public UnionFindItem<E> mergeInto(UnionFindItem<E> other) {
        final UnionFindItem<E> myRoot = this.compress();
        final UnionFindItem<E> otherRoot = other.compress();
         myRoot.parent = otherRoot;
         return otherRoot;
     }
 }
