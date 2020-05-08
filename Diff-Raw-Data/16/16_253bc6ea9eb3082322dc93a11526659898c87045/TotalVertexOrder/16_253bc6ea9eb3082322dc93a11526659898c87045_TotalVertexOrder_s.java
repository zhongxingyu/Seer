 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.mahout.graph.common;
 
 import java.util.Comparator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.mahout.graph.model.RepresentativeEdge;
 import org.apache.mahout.graph.model.Vertex;
 import org.apache.mahout.graph.model.VertexWithDegree;
 
 /**
  * {@link Comparator} class to give a total ordering on augmented vertices. Use
  * the degree order and natural ordering of vertices as a tie-breaker. 
  */
 public class TotalVertexOrder implements Comparator<VertexWithDegree> {
 
   private static final TotalVertexOrder order = new TotalVertexOrder();
 
   private TotalVertexOrder() {
   }
 
   /**
    * Singleton usage proposed. Instances are stateless anyway.
    * 
   * @return
    */
   public static TotalVertexOrder instance() {
     return order;
   }
 
   /**
    * Orders {@link VertexWithDegree} according to their natural order and uses
    * the {@link Vertex} natural order as tie-breaker.
    */
   @Override
   public int compare(VertexWithDegree v, VertexWithDegree w) {
     int c = 0;
     if (v == null && w == null) {
       c = 0;
     } else if (v == null && w != null) {
       c = Integer.MIN_VALUE;
     } else if (v != null && w == null) {
       c = Integer.MAX_VALUE;
     } else {
       c = v.compareTo(w);
       if (c == 0) {
         c = v.getVertex().compareTo(w.getVertex());
       }
     }
     return c;
   }
 
   /**
    * Return the vertices of a {@link RepresentativeEdge} as an ordered set
    * according to the ordering of this class.
    * 
    * @param edge
    *          The edge to get the vertices of
    * @return An ordered set of vertices
    */
   public static Set<VertexWithDegree> getOrdered(RepresentativeEdge edge) {
     Set<VertexWithDegree> vertices = new TreeSet<VertexWithDegree>(instance());
     Vertex v0 = edge.getVertex0();
     vertices.add(new VertexWithDegree(v0, edge.getDegree(v0)));
     Vertex v1 = edge.getVertex1();
     vertices.add(new VertexWithDegree(v1, edge.getDegree(v1)));
     return vertices;
   }
 }
