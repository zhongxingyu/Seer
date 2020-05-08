 /*
  * Copyright (c) 2009, Miroslav Batchkarov, University of Sussex
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  *  * Neither the name of the University of Sussex nor the names of its
  *    contributors may be used to endorse or promote products  derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import edu.uci.ics.jung.graph.MyGraph;
 import edu.uci.ics.jung.graph.ObservableGraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import model.MyEdge;
 import model.MyVertex;
 import model.Strings;
 import org.apache.commons.collections15.Factory;
 
 import java.util.Random;
 
 /**
  * Generates random graphs, where each two vertices are equally likely to be
  * connected with an edge
  * Allows parallel edges, but not self-links
  * @author reseter
  */
 public class RandomGenerator {
 
     private int numV;
     private int numE;
     private Factory<MyEdge> ef;
     private Factory<MyVertex> vf;
     private Factory<MyGraph> gf;
 
     public RandomGenerator(Factory gf,
             Factory vf,
             Factory ef,
             int vertices, int edges) {
 
         this.gf = gf;
         this.vf = vf;
         this.ef = ef;
         numV = vertices;
         numE = edges;
     }
 
     public ObservableGraph create() {
         ObservableGraph g = gf.create();
         for (int i = 0; i < numV; i++) {
             MyVertex v = vf.create();
 //            v.setUserDatum(Strings.state, EpiState.SUSCEPTIBLE);
             g.addVertex(v);
         }
 
         int i = 0;
         Random r = new Random();
         Object[] v = g.getVertices().toArray();
         numE = Math.min(numE, (numV * (numV - 1) / 2));
         while (i < numE) {
             int first = r.nextInt(numV);
             int second = r.nextInt(numV);
             if (first != second && !g.isNeighbor((MyVertex) v[first], (MyVertex) v[second])) {
                 MyEdge e = ef.create();
                e.setWeigth(1.0);
                 e.setUserDatum(Strings.infected, false);
                 g.addEdge(e, (MyVertex) v[first], (MyVertex) v[second], EdgeType.UNDIRECTED);
                 i++;
             }
         }
 
         return g;
     }
 }
