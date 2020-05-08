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
     
     
     
    public UnionFindItem<E> root() {
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
     
     
     
    /**
     * Merges to items by setting this item's root to the root of the provided
     * item.  
     * @param other The item to merge with.
     * @return The new root item (which is the root of <tt>other</tt>
     */
     public UnionFindItem<E> mergeInto(UnionFindItem<E> other) {
        final UnionFindItem<E> myRoot = this.root();
        final UnionFindItem<E> otherRoot = other.root();
         myRoot.parent = otherRoot;
         return otherRoot;
     }
 }
