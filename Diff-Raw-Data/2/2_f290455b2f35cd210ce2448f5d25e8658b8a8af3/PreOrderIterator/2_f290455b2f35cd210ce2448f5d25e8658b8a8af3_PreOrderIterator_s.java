 /*
  * Copyright (c) 2012-2013 Ray A. Conner
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.github.rconner.anansi;
 
 import com.github.rconner.util.PersistentList;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.TreeTraverser;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Package implementation of the Iterator returned by {@link Traversers#preOrder(Object, TreeTraverser)}.
  *
  * @param <T> the vertex type
  *
  * @author rconner
  */
 final class PreOrderIterator<T> implements PruningIterator<T> {
     private final TreeTraverser<T> adjacency;
     private PersistentList<Iterator<T>> stack;
     private boolean canMutate = false;
 
     PreOrderIterator( final T root, final TreeTraverser<T> adjacency ) {
         this.adjacency = adjacency;
         stack = PersistentList.of( TraversalMove.rootIterator( root ) );
     }
 
     @Override
     public boolean hasNext() {
         for( final Iterator<T> iterator : stack ) {
             if( iterator.hasNext() ) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public T next() {
         while( !stack.isEmpty() && !stack.first().hasNext() ) {
             stack = stack.rest();
         }
         if( stack.isEmpty() ) {
             canMutate = false;
             throw new NoSuchElementException();
         }
         final T vertex = stack.first().next();
         stack = stack.add( adjacency.children( vertex ).iterator() );
         canMutate = true;
         return vertex;
     }
 
     @Override
     public void remove() {
         Preconditions.checkState( canMutate );
         // The operations make more sense like this:
         //   stack = stack.rest();
         //   stack.first().iterator.remove();
         // But that doesn't fail atomically.
         stack.rest().first().remove();
         stack = stack.rest();
         canMutate = false;
     }
 
     @Override
     public void prune() {
         Preconditions.checkState( canMutate );
         stack = stack.rest();
         canMutate = false;
     }
 }
 
 /*
 
 This is a step-by-step example of iterating with this Iterator. The example graph is below, child order is alphabetic.
 
           A
          / \
         B   C
        / \ / \
       E   D   F
 
 The iteration order will be:
   A, B, D, E, C, D, F
 
 The state of the pre-order iterator is the state of its stack, which will be written as:
 
               iterator
  bottom      [ A ]
               [ B, * C ]
               [ D, E * ]
   top         [ ]
 
 Or as just "empty" if there are no iterators in the stack. The "*" in the iterator precedes the next vertex to be returned.
 
 In the step-by-step below, colloquial language will be used (push, pop, top) rather than the actual method names.
 
 "advance top and push next" means:
   vertex = top.next()
   push [ children(vertex).iterator() ]
 So, advance the top iterator and push a move for its children.
 
 
 @init
                                [ * A ]
 
 next()
   pop exhausted iterators      no change
 
   advance top and push next    [ A * ]
                                [ * B, C ]
 
   return advanced top = A
 
 next()
   pop exhausted iterators      no change
 
   advance top and push next    [ A * ]
                                [ B, * C ]
                                [ * D, E ]
 
   return advanced top = B
 
 next()
   pop exhausted iterators      no change
 
   advance top and push next    [ A * ]
                                [ B, * C ]
                                [ D, * E ]
                                [ ]
 
   return advanced top = D
 
 next()
   pop exhausted iterators      [ A * ]
                                [ B, * C ]
                                [ D, * E ]
 
   advance top and push next    [ A * ]
                                [ B, * C ]
                                [ D, E * ]
                                [ ]
 
   return advanced top = E
 
 next()
   pop exhausted iterators      [ A * ]
                                [ B, * C ]
 
   advance top and push next    [ A * ]
                                [ B, C * ]
                                [ * D, F ]
 
   return advanced top = C
 
 next()
   pop exhausted iterators      no change
 
   advance top and push next    [ A * ]
                                [ B, C * ]
                                [ D, * F ]
                                [ ]
 
   return advanced top = D
 
 next()
   pop exhausted iterators      [ A * ]
                                [ B, C * ]
                                [ D, * F ]
 
   advance top and push next    [ A * ]
                                [ B, C * ]
                                [ D, F * ]
                                [ ]
 
   return advanced top = F
 
 next()
   pop exhausted iterators      empty
 
   throw NoSuchElementException
 
 */
