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
  * Factory and implementation of a dependency graph that can topologically sort vertices that are {@link String}s.
  *
  * @see DirectedAcyclicGraph
  */
 public class StringGraph extends ObjectGraph<String, String> {
   /**
    * @see ObjectGraph#ObjectGraph()
    */
   protected StringGraph() {
     super();
   }
 
   /**
    * @see ObjectGraph#copy()
    */
   public StringGraph copyAsStringGraph() {
     return copyGraph(this);
   }
 
   /**
    * @see ObjectGraph#build(IVertex[])
    */
   public static StringGraph buildFromStrings(final String...vertices) {
     final StringGraph g = new StringGraph();
     for(String d : vertices) {
       g.addVertex(d);
     }
     return g;
   }
 
   /**
    * @see ObjectGraph#create()
    */
   public static StringGraph createForStrings() {
     return buildFromStrings();
   }
 
   /**
    * @see ObjectGraph#addVertex(IVertex)
    */
  public StringGraph addVertex(final String value) {
    super.addVertex(ObjectVertex.from(value));
     return this;
   }
 
   /**
    * @see ObjectGraph#removeVertex(IVertex)
    */
  public StringGraph removeVertex(final String value) {
    super.removeVertex(ObjectVertex.from(value));
     return this;
   }
 
   /**
    * @see ObjectGraph#addEdge(IVertex, IVertex)
    */
   public StringGraph addEdge(final String from, final String to) {
     super.addEdge(ObjectVertex.from(from), ObjectVertex.from(to));
     return this;
   }
 
   /**
    * @see ObjectGraph#removeEdge(IVertex, IVertex)
    */
   public StringGraph removeEdge(final String from, final String to) {
     super.removeEdge(ObjectVertex.from(from), ObjectVertex.from(to));
     return this;
   }
 }
