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
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableListMultimap;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.Multimap;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import static com.github.rconner.anansi.WalkTest.assertWalkContains;
 import static com.github.rconner.util.IterableTest.assertIteratorEmpty;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public final class TraversersTest {
 
     // We'll be using Multimaps to define adjacency for test cases.
     // Lacking a key (vertex) means that there are no walks leaving that vertex. There is no way, using this
     // representation, to denote whether or not a vertex is present. Any vertex is present.
 
     private final Multimap<String, Walk<String, String>> empty = ImmutableMultimap.of();
 
     private final Multimap<String, Walk<String, String>> singleEdge = ImmutableMultimap.of(
             "A", Walk.single( "A", "B", "A->B" ) );
 
     // Warning! Do not perform a post-order traversal on this graph.
     private final Multimap<String, Walk<String, String>> loop = ImmutableMultimap.of(
             "A", Walk.single( "A", "A", "A->A" ) );
 
     // Warning! Do not perform a post-order traversal on this graph.
     private final Multimap<String, Walk<String, String>> cycle = ImmutableListMultimap.<String, Walk<String, String>>builder()
             .put( "A", Walk.single( "A", "B", "A->B" ) )
             .put( "B", Walk.single( "B", "C", "B->C" ) )
             .put( "C", Walk.single( "C", "A", "C->A" ) )
             .build();
 
     private final Multimap<String, Walk<String, String>> tree = ImmutableListMultimap.<String, Walk<String, String>>builder()
             .put( "A", Walk.single( "A", "B", "A->B" ) )
             .put( "A", Walk.single( "A", "C", "A->C" ) )
             .put( "B", Walk.single( "B", "D", "B->D" ) )
             .put( "B", Walk.single( "B", "E", "B->E" ) )
             .put( "C", Walk.single( "C", "F", "C->F" ) )
             .put( "C", Walk.single( "C", "G", "C->G" ) )
             .build();
 
     // Has two paths from A to D.
     private final Multimap<String, Walk<String, String>> dag = ImmutableListMultimap.<String, Walk<String, String>>builder()
             .put( "A", Walk.single( "A", "B", "A->B" ) )
             .put( "A", Walk.single( "A", "C", "A->C" ) )
             .put( "B", Walk.single( "B", "D", "B->D" ) )
             .put( "B", Walk.single( "B", "E", "B->E" ) )
             .put( "C", Walk.single( "C", "D", "C->D" ) )
             .put( "D", Walk.single( "D", "G", "D->G" ) )
             .build();
 
     private static Traverser<String, String> adjacencyFor( final Multimap<String, Walk<String, String>> graph ) {
         return new Traverser<String, String>() {
             @Override
             public Iterable<Walk<String, String>> apply( final String input ) {
                 return graph.get( input );
             }
         };
     }
 
     private static Traverser<String, String> mutableAdjacencyFor( final Multimap<String, Walk<String, String>> graph ) {
         return adjacencyFor( ArrayListMultimap.create( graph ) );
     }
 
 
     static <V, E> void assertNextWalkIs( final Iterator<Walk<V, E>> iterator, final Object... expectedWalk ) {
         assertThat( iterator.hasNext(), is( true ) );
         assertWalkContains( iterator.next(), expectedWalk );
     }
 
     static final Object[][] EMPTY_EXPECTED_WALKS = new Object[][] { };
 
     static <V, E> void assertNextWalksAre( final Iterator<Walk<V, E>> iterator, final Object[][] expectedWalks ) {
         for( final Object[] expectedWalk : expectedWalks ) {
             assertNextWalkIs( iterator, expectedWalk );
         }
     }
 
     static <V, E> void assertTraversalContains( final Iterable<Walk<V, E>> traversal, final Object[][] expectedWalks ) {
         final Iterator<Walk<V, E>> iterator = traversal.iterator();
         assertNextWalksAre( iterator, expectedWalks );
         assertThat( iterator.hasNext(), is( false ) );
         assertIteratorEmpty( iterator );
     }
 
     static <V, E> void assertTraversalBegins( final Iterable<Walk<V, E>> traversal, final Object[][] expectedWalks ) {
         final Iterator<Walk<V, E>> iterator = traversal.iterator();
         assertNextWalksAre( iterator, expectedWalks );
         assertThat( iterator.hasNext(), is( true ) );
     }
 
     // empty()
 
     @Test
     public void empty() {
         final Traverser<String, String> traverser = Traversers.empty();
         assertTraversalContains( traverser.apply( "A" ), EMPTY_EXPECTED_WALKS );
     }
 
     // preOrder( Traverser )
 
     @Test
     public void preOrderEmpty() {
         final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( empty ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
     }
 
     @Test
     public void preOrderSingleEdge() {
         final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( singleEdge ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" }, { "A", "A->B", "B" } } );
     }
 
     @Test
     public void preOrderLoop() {
         final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( loop ) );
         assertTraversalBegins( traverser.apply( "A" ),
                                new Object[][] { { "A" },
                                                 { "A", "A->A", "A" },
                                                 { "A", "A->A", "A", "A->A", "A" },
                                                 { "A", "A->A", "A", "A->A", "A", "A->A", "A" } } );
     }
 
     @Test
     public void preOrderCycle() {
         final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( cycle ) );
         assertTraversalBegins( traverser.apply( "A" ),
                                new Object[][] { { "A" },
                                                 { "A", "A->B", "B" },
                                                 { "A", "A->B", "B", "B->C", "C" },
                                                 { "A", "A->B", "B", "B->C", "C", "C->A", "A" },
                                                 { "A", "A->B", "B", "B->C", "C", "C->A", "A", "A->B", "B" } } );
     }
 
     @Test
     public void preOrderTree() {
         final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( tree ) );
         assertTraversalContains( traverser.apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->C", "C", "C->F", "F" },
                                                   { "A", "A->C", "C", "C->G", "G" } } );
     }
 
     private static void assertPreOrderFullDag( final Iterable<Walk<String, String>> traversal ) {
         assertTraversalContains( traversal,
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void preOrderDag() {
         final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( dag ) );
         assertPreOrderFullDag( traverser.apply( "A" ) );
     }
 
     @Test( expected = IllegalStateException.class )
     public void preOrderRemoveBeforeNext() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         iterator.remove();
     }
 
     @Test( expected = IllegalStateException.class )
     public void preOrderRemoveRoot() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalkIs( iterator, "A" );
         iterator.remove();
     }
 
     @Test
     public void preOrderRemoveB() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.preOrder( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void preOrderRemoveD() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" }, { "A", "A->B", "B" }, { "A", "A->B", "B", "B->D", "D" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.preOrder( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void preOrderRemoveE() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->E", "E" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.preOrder( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test( expected = IllegalStateException.class )
     public void preOrderRemoveTwice() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         iterator.remove();
         iterator.remove();
     }
 
     @Test( expected = IllegalStateException.class )
     public void preOrderPruneBeforeNext() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         ( (PruningIterator) iterator ).prune();
     }
 
     @Test
     public void preOrderPruneRoot() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalkIs( iterator, "A" );
         ( (PruningIterator) iterator ).prune();
         assertIteratorEmpty( iterator );
 
         // Check that data structure is unchanged
         assertPreOrderFullDag( Traversers.preOrder( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void preOrderPruneB() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertPreOrderFullDag( Traversers.preOrder( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void preOrderPruneD() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" }, { "A", "A->B", "B" }, { "A", "A->B", "B", "B->D", "D" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertPreOrderFullDag( Traversers.preOrder( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void preOrderPruneE() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->E", "E" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertPreOrderFullDag( Traversers.preOrder( adjacency ).apply( "A" ) );
     }
 
     @Test( expected = IllegalStateException.class )
     public void preOrderPruneTwice() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).prune();
         ( (PruningIterator) iterator ).prune();
     }
 
     @Test( expected = IllegalStateException.class )
     public void preOrderRemoveThenPrune() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).remove();
         ( (PruningIterator) iterator ).prune();
     }
 
     @Test( expected = IllegalStateException.class )
     public void preOrderPruneThenRemove() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.preOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).prune();
         ( (PruningIterator) iterator ).remove();
     }
 
     // postOrder( Traverser )
 
     @Test
     public void postOrderEmpty() {
         final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( empty ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
     }
 
     @Test
     public void postOrderSingleEdge() {
         final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( singleEdge ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A", "A->B", "B" }, { "A" } } );
     }
 
     @Test
     public void postOrderTree() {
         final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( tree ) );
         assertTraversalContains( traverser.apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C", "C->F", "F" },
                                                   { "A", "A->C", "C", "C->G", "G" },
                                                   { "A", "A->C", "C" },
                                                   { "A" } } );
     }
 
     @Test
     public void postOrderDag() {
         final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( dag ) );
         assertTraversalContains( traverser.apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C" },
                                                   { "A" } } );
     }
 
     @Test( expected = IllegalStateException.class )
     public void postOrderRemoveBeforeNext() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.postOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         iterator.remove();
     }
 
     @Test( expected = IllegalStateException.class )
     public void postOrderRemoveRoot() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.postOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C" },
                                              { "A" } } );
         iterator.remove();
     }
 
     @Test
     public void postOrderRemoveB() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.postOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->B", "B" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C" },
                                              { "A" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.postOrder( adjacency ).apply( "A" ),
                                  new Object[][] { { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C" },
                                                   { "A" } } );
     }
 
     @Test
     public void postOrderRemoveD() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.postOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->D", "D" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C" },
                                              { "A" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.postOrder( adjacency ).apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C" },
                                                   { "A" } } );
     }
 
     @Test
     public void postOrderRemoveE() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.postOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C" },
                                              { "A" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.postOrder( adjacency ).apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C" },
                                                   { "A" } } );
     }
 
     @Test( expected = IllegalStateException.class )
     public void postOrderRemoveTwice() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.postOrder( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->B", "B" } } );
         iterator.remove();
         iterator.remove();
     }
 
     // breadthFirst( Traverser )
 
     @Test
     public void breadthFirstEmpty() {
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( empty ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
     }
 
     @Test
     public void breadthFirstSingleEdge() {
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( singleEdge ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" }, { "A", "A->B", "B" } } );
     }
 
     @Test
     public void breadthFirstLoop() {
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( loop ) );
         assertTraversalBegins( traverser.apply( "A" ),
                                new Object[][] { { "A" },
                                                 { "A", "A->A", "A" },
                                                 { "A", "A->A", "A", "A->A", "A" },
                                                 { "A", "A->A", "A", "A->A", "A", "A->A", "A" } } );
     }
 
     @Test
     public void breadthFirstCycle() {
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( cycle ) );
         assertTraversalBegins( traverser.apply( "A" ),
                                new Object[][] { { "A" },
                                                 { "A", "A->B", "B" },
                                                 { "A", "A->B", "B", "B->C", "C" },
                                                 { "A", "A->B", "B", "B->C", "C", "C->A", "A" },
                                                 { "A", "A->B", "B", "B->C", "C", "C->A", "A", "A->B", "B" } } );
     }
 
     @Test
     public void breadthFirstTree() {
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( tree ) );
         assertTraversalContains( traverser.apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->F", "F" },
                                                   { "A", "A->C", "C", "C->G", "G" } } );
     }
 
     private static void assertBreadthFirstFullDag( final Iterable<Walk<String, String>> traversal ) {
         assertTraversalContains( traversal,
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void breadthFirstDag() {
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( dag ) );
         assertBreadthFirstFullDag( traverser.apply( "A" ) );
     }
 
     @Test( expected = IllegalStateException.class )
     public void breadthFirstRemoveBeforeNext() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         iterator.remove();
     }
 
     @Test( expected = IllegalStateException.class )
     public void breadthFirstRemoveRoot() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalkIs( iterator, "A" );
         iterator.remove();
     }
 
     @Test
     public void breadthFirstRemoveB() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.breadthFirst( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void breadthFirstRemoveC() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" }, { "A", "A->C", "C" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.breadthFirst( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void breadthFirstRemoveFirstD() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.breadthFirst( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void breadthFirstRemoveE() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.breadthFirst( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->C", "C", "C->D", "D" },
                                                   { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void breadthFirstRemoveSecondD() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" } } );
         iterator.remove();
         assertNextWalksAre( iterator, new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.breadthFirst( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
     }
 
     @Test
     public void breadthFirstRemoveFirstG() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
         iterator.remove();
         assertNextWalksAre( iterator, EMPTY_EXPECTED_WALKS );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.breadthFirst( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->D", "D" } } );
     }
 
     @Test
     public void breadthFirstRemoveSecondG() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
         iterator.remove();
         assertNextWalksAre( iterator, EMPTY_EXPECTED_WALKS );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.breadthFirst( adjacency ).apply( "A" ),
                                  new Object[][] { { "A" },
                                                   { "A", "A->B", "B" },
                                                   { "A", "A->C", "C" },
                                                   { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->D", "D" } } );
     }
 
     @Test( expected = IllegalStateException.class )
     public void breadthFirstRemoveTwice() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         iterator.remove();
         iterator.remove();
     }
 
     @Test( expected = IllegalStateException.class )
     public void breadthFirstPruneBeforeNext() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         ( (PruningIterator) iterator ).prune();
     }
 
     @Test
     public void breadthFirstPruneRoot() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalkIs( iterator, "A" );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator, EMPTY_EXPECTED_WALKS );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void breadthFirstPruneB() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void breadthFirstPruneC() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" }, { "A", "A->C", "C" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void breadthFirstPruneFirstD() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void breadthFirstPruneE() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void breadthFirstPruneSecondD() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator, new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void breadthFirstPruneFirstG() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator, EMPTY_EXPECTED_WALKS );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test
     public void breadthFirstPruneSecondG() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A" },
                                              { "A", "A->B", "B" },
                                              { "A", "A->C", "C" },
                                              { "A", "A->B", "B", "B->D", "D" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" },
                                              { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
         ( (PruningIterator) iterator ).prune();
         assertNextWalksAre( iterator, EMPTY_EXPECTED_WALKS );
 
         // Check that data structure is unchanged
         assertBreadthFirstFullDag( Traversers.breadthFirst( adjacency ).apply( "A" ) );
     }
 
     @Test( expected = IllegalStateException.class )
     public void breadthFirstPruneTwice() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Iterator<Walk<String, String>> iterator = Traversers.breadthFirst( adjacency ).apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).prune();
         ( (PruningIterator) iterator ).prune();
     }
 
     @Test( expected = IllegalStateException.class )
     public void breadthFirstRemoveThenPrune() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).remove();
         ( (PruningIterator) iterator ).prune();
     }
 
     @Test( expected = IllegalStateException.class )
     public void breadthFirstPruneThenRemove() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.breadthFirst( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator, new Object[][] { { "A" }, { "A", "A->B", "B" } } );
         ( (PruningIterator) iterator ).prune();
         ( (PruningIterator) iterator ).remove();
     }
 
     // leaves( Traverser )
 
     @Test
     public void leavesEmpty() {
         final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( empty ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
     }
 
     @Test
     public void leavesSingleEdge() {
         final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( singleEdge ) );
         assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A", "A->B", "B" } } );
     }
 
     @Test
     public void leavesTree() {
         final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( tree ) );
         assertTraversalContains( traverser.apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->F", "F" },
                                                   { "A", "A->C", "C", "C->G", "G" } } );
     }
 
     @Test
     public void leavesDag() {
         final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( dag ) );
         assertTraversalContains( traverser.apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test( expected = IllegalStateException.class )
     public void leavesRemoveBeforeNext() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.leaves( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         iterator.remove();
     }
 
     @Test
     public void leavesRemoveFirstG() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.leaves( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.leaves( adjacency ).apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->D", "D" } } );
     }
 
     @Test
     public void leavesRemoveSecondG() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.leaves( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
         iterator.remove();
         assertIteratorEmpty( iterator );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.leaves( adjacency ).apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D" },
                                                   { "A", "A->B", "B", "B->E", "E" },
                                                   { "A", "A->C", "C", "C->D", "D" } } );
     }
 
     @Test
     public void leavesRemoveE() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.leaves( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->E", "E" } } );
         iterator.remove();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
 
         // Check that data structure was actually changed
         assertTraversalContains( Traversers.leaves( adjacency ).apply( "A" ),
                                  new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                                   { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
     }
 
     @Test( expected = IllegalStateException.class )
     public void leavesRemoveTwice() {
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.leaves( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->E", "E" } } );
         iterator.remove();
         iterator.remove();
     }
 
     @Test( expected = IllegalStateException.class )
     public void leavesNextAfterLastThenRemove() {
         // This use case is slightly different, because it exercises different logic in the traverser.
         final Traverser<String, String> adjacency = mutableAdjacencyFor( dag );
         final Traverser<String, String> traverser = Traversers.leaves( adjacency );
         final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
         assertNextWalksAre( iterator,
                             new Object[][] { { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                                              { "A", "A->B", "B", "B->E", "E" },
                                              { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
         try {
             iterator.next();
         } catch( NoSuchElementException ignored ) {
             // expected
         }
         iterator.remove();
     }
 
     // elements()
 
     private static void assertPathWalksAre( final Iterable<Walk<Object, String>> traversal,
                                             final Object[][] expectedElements ) {
         final Iterator<Walk<Object, String>> iterator = traversal.iterator();
         for( final Object[] element : expectedElements ) {
             assertThat( iterator.hasNext(), is( true ) );
             final Walk<Object, String> walk = iterator.next();
             assertThat( Traversers.elementPath( walk ), is( element[ 0 ] ) );
             assertThat( walk.getTo(), is( element[ 1 ] ) );
         }
         assertIteratorEmpty( iterator );
     }
 
     @Test
     public void elementsNull() {
         final Object root = null;
         assertPathWalksAre( Traversers.elements().apply( root ), EMPTY_EXPECTED_WALKS );
     }
 
     @Test
     public void elementsString() {
         final Object root = "abc";
         assertPathWalksAre( Traversers.elements().apply( root ), EMPTY_EXPECTED_WALKS );
     }
 
     @Test
     public void elementsEmptyMap() {
         final Object root = Collections.emptyMap();
         assertPathWalksAre( Traversers.elements().apply( root ), EMPTY_EXPECTED_WALKS );
     }
 
     @Test
     public void elementsEmptyList() {
         final Object root = Collections.emptyList();
         assertPathWalksAre( Traversers.elements().apply( root ), EMPTY_EXPECTED_WALKS );
     }
 
     private static final int[] EMPTY_INT_ARRAY = new int[ 0 ];
 
     @Test
     public void elementsEmptyArray() {
         final Object root = EMPTY_INT_ARRAY;
         assertPathWalksAre( Traversers.elements().apply( root ), EMPTY_EXPECTED_WALKS );
     }
 
     @Test
     public void elementsSimpleMap() {
         final Object root = ImmutableMap.of( "name", "Alice", "age", 37, "deceased", false );
         assertPathWalksAre( Traversers.elements().apply( root ),
                             new Object[][] { { "name", "Alice" }, { "age", 37 }, { "deceased", false } } );
     }
 
     @Test
     public void elementsSimpleList() {
        final Object root = Arrays.asList( "Alice", 37, false );
         assertPathWalksAre( Traversers.elements().apply( root ),
                             new Object[][] { { "[0]", "Alice" }, { "[1]", 37 }, { "[2]", false } } );
     }
 
     @Test
     public void elementsSimpleArray() {
         final Object root = new int[] { 42, 99, 256 };
         assertPathWalksAre( Traversers.elements().apply( root ),
                             new Object[][] { { "[0]", 42 }, { "[1]", 99 }, { "[2]", 256 } } );
     }
 
     @Test
     public void elementsMap() {
        final Map root = ImmutableMap.of( "names", Arrays.asList( "Alice", "Becky", "Carol" ),
                                    "ages", Arrays.asList( 37, 42, 32 ) );
         assertPathWalksAre( Traversers.elements().apply( root ),
                             new Object[][] { { "names", root.get( "names" ) },
                                              { "ages", root.get( "ages" ) } } );
     }
 
     @SuppressWarnings( "unchecked" )
     @Test
     public void elementsList() {
        final List root = Arrays.asList( ImmutableMap.of( "name", "Alice", "age", 37 ),
                                   ImmutableMap.of( "name", "Becky", "age", 42 ) );
         assertPathWalksAre( Traversers.elements().apply( root ),
                             new Object[][] { { "[0]", root.get( 0 ) },
                                              { "[1]", root.get( 1 ) } } );
     }
 
     @Test
     public void elementsArray() {
         final Object[] root = new Object[] { ImmutableMap.of( "name", "Alice", "age", 37 ),
                                        ImmutableMap.of( "name", "Becky", "age", 42 ) };
         assertPathWalksAre( Traversers.elements().apply( root ),
                             new Object[][] { { "[0]", root[ 0 ] },
                                              { "[1]", root[ 1 ] } } );
     }
 
     // leafElements()
     // elementPath()
 
     // Note that an empty map/iterable/array *is* a leaf.
 
     @Test
     public void leafElementsNull() {
         final Object root = null;
         assertPathWalksAre( Traversers.leafElements().apply( root ), new Object[][] { { "", root } } );
     }
 
     @Test
     public void leafElementsString() {
         final Object root = "abc";
         assertPathWalksAre( Traversers.leafElements().apply( root ), new Object[][] { { "", root } } );
     }
 
     @Test
     public void leafElementsEmptyMap() {
         final Object root = Collections.emptyMap();
         assertPathWalksAre( Traversers.leafElements().apply( root ), new Object[][] { { "", root } } );
     }
 
     @Test
     public void leafElementsEmptyList() {
         final Object root = Collections.emptyList();
         assertPathWalksAre( Traversers.leafElements().apply( root ), new Object[][] { { "", root } } );
     }
 
     @Test
     public void leafElementsEmptyArray() {
         final Object root = EMPTY_INT_ARRAY;
         assertPathWalksAre( Traversers.leafElements().apply( root ), new Object[][] { { "", root } } );
     }
 
     @Test
     public void leafElementsSimpleMap() {
         final Object root = ImmutableMap.of( "name", "Alice", "age", 37, "deceased", false );
         assertPathWalksAre( Traversers.leafElements().apply( root ),
                             new Object[][] { { "name", "Alice" }, { "age", 37 }, { "deceased", false } } );
     }
 
     @Test
     public void leafElementsSimpleList() {
        final Object root = Arrays.asList( "Alice", 37, false );
         assertPathWalksAre( Traversers.leafElements().apply( root ),
                             new Object[][] { { "[0]", "Alice" }, { "[1]", 37 }, { "[2]", false } } );
     }
 
     @Test
     public void leafElementsSimpleArray() {
         final Object root = new int[] { 42, 99, 256 };
         assertPathWalksAre( Traversers.leafElements().apply( root ),
                             new Object[][] { { "[0]", 42 }, { "[1]", 99 }, { "[2]", 256 } } );
     }
 
     @Test
     public void leafElementsMap() {
        final Map root = ImmutableMap.of( "names", Arrays.asList( "Alice", "Becky", "Carol" ),
                                    "ages", Arrays.asList( 37, 42, 32 ) );
         assertPathWalksAre( Traversers.leafElements().apply( root ),
                             new Object[][] { { "names[0]", "Alice" },
                                              { "names[1]", "Becky" },
                                              { "names[2]", "Carol" },
                                              { "ages[0]", 37 },
                                              { "ages[1]", 42 },
                                              { "ages[2]", 32 } } );
     }
 
     @SuppressWarnings( "unchecked" )
     @Test
     public void leafElementsList() {
        final List root = Arrays.asList( ImmutableMap.of( "name", "Alice", "age", 37 ),
                                   ImmutableMap.of( "name", "Becky", "age", 42 ) );
         assertPathWalksAre( Traversers.leafElements().apply( root ),
                             new Object[][] { { "[0].name", "Alice" },
                                              { "[0].age", 37 },
                                              { "[1].name", "Becky" },
                                              { "[1].age", 42 } } );
     }
 
     @Test
     public void leafElementsArray() {
         final Object[] root = new Object[] { ImmutableMap.of( "name", "Alice", "age", 37 ),
                                        ImmutableMap.of( "name", "Becky", "age", 42 ) };
         assertPathWalksAre( Traversers.leafElements().apply( root ),
                             new Object[][] { { "[0].name", "Alice" },
                                              { "[0].age", 37 },
                                              { "[1].name", "Becky" },
                                              { "[1].age", 42 } } );
     }
 
     // Test a more complex graph
 
     @SuppressWarnings( "unchecked" )
     @Test
     public void leafElementsComplex() {
         final Map<?, ?> map = ImmutableMap.builder()
                 .put( "string", "A String" )
                 .put( "integer", 42 )
                 .put( "list", Arrays.asList( "zero", "one", "two", "three" ) )
                 .put( "array", new Object[] { "four", "five", "six" } )
                 .put( "boolean.array", new boolean[] { false, true, true, false, true } )
                 .put( "map",
                       ImmutableMap.builder()
                               .put( "foo[abc]bar", "Another String" )
                               .put( "people",
                                     Arrays.asList( ImmutableMap.of( "name", "Alice", "age", 37 ),
                                                    ImmutableMap.of( "name", "Bob", "age", 55 ),
                                                    ImmutableMap.of( "name", "Carol", "age", 23 ),
                                                    ImmutableMap.of( "name", "Dave", "age", 27 ) ) )
                               .put( "owner", ImmutableMap.of( "name", "Elise", "age", 43 ) )
                               .build() )
                 .build();
 
         assertPathWalksAre( Traversers.leafElements().apply( map ),
                             new Object[][] { { "string", "A String" },
                                              { "integer", 42 },
                                              { "list[0]", "zero" },
                                              { "list[1]", "one" },
                                              { "list[2]", "two" },
                                              { "list[3]", "three" },
                                              { "array[0]", "four" },
                                              { "array[1]", "five" },
                                              { "array[2]", "six" },
                                              { "boolean\\.array[0]", false },
                                              { "boolean\\.array[1]", true },
                                              { "boolean\\.array[2]", true },
                                              { "boolean\\.array[3]", false },
                                              { "boolean\\.array[4]", true },
                                              { "map.foo\\[abc\\]bar", "Another String" },
                                              { "map.people[0].name", "Alice" },
                                              { "map.people[0].age", 37 },
                                              { "map.people[1].name", "Bob" },
                                              { "map.people[1].age", 55 },
                                              { "map.people[2].name", "Carol" },
                                              { "map.people[2].age", 23 },
                                              { "map.people[3].name", "Dave" },
                                              { "map.people[3].age", 27 },
                                              { "map.owner.name", "Elise" },
                                              { "map.owner.age", 43 } } );
     }
 }
