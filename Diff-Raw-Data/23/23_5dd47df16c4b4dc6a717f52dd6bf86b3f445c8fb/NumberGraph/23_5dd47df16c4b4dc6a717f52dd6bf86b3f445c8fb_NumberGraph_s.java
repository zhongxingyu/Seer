 /*
   Copyright (C) 2012-2013 the original author or authors.
 
   See the LICENSE.txt file distributed with this work for additional
   information regarding copyright ownership.
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 
 package jcommon.graph;
 
 /**
  * Factory and implementation of a dependency graph that can topologically sort vertices that are {@link Number}s.
  *
  * @param <TNumber> The type of number this graph operates on.
  *
  * @see DirectedAcyclicGraph
  */
 public class NumberGraph<TNumber extends Number> extends ObjectGraph<TNumber, TNumber> {
   /**
    * @see ObjectGraph#ObjectGraph()
    */
   protected NumberGraph() {
     super();
   }
 
   /**
    * @see ObjectGraph#copy()
    */
   public NumberGraph<TNumber> copyAsNumberGraph() {
     return copyGraph(this);
   }
 
   /**
    * @see ObjectGraph#build(IVertex[])
    */
   public static <TNumber extends Number> NumberGraph<TNumber> buildFromNumbers(final TNumber...values) {
     final NumberGraph<TNumber> g = new NumberGraph<TNumber>();
     for(TNumber d : values)
       g.addVertex(d);
     return g;
   }
 
   /**
    * @see ObjectGraph#create()
    */
   @SuppressWarnings("unchecked")
   public static <TNumber extends Number> NumberGraph<TNumber> createForNumbers() {
     return buildFromNumbers();
   }
 
   /**
    * @see ObjectGraph#addVertex(IVertex)
    */
  public NumberGraph<TNumber> addVertex(final TNumber vertex) {
    super.addVertex(ObjectVertex.from(vertex));
     return this;
   }
 
   /**
    * @see ObjectGraph#removeVertex(IVertex)
    */
  public NumberGraph<TNumber> removeVertex(final TNumber vertex) {
    super.removeVertex(ObjectVertex.from(vertex));
     return this;
   }
 
   /**
    * @see ObjectGraph#addEdge(IVertex, IVertex)
    */
   public NumberGraph<TNumber> addEdge(final TNumber from, final TNumber to) {
     super.addEdge(ObjectVertex.from(from), ObjectVertex.from(to));
     return this;
   }
 
   /**
    * @see ObjectGraph#removeEdge(IVertex, IVertex)
    */
   public NumberGraph<TNumber> removeEdge(final TNumber from, final TNumber to) {
     super.removeEdge(ObjectVertex.from(from), ObjectVertex.from(to));
     return this;
   }
 }
