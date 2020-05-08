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
 
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.ObservableGraph;
 import edu.uci.ics.jung.io.PajekNetReader;
 import edu.uci.ics.jung.io.PajekNetWriter;
 import model.factories.EdgeFactory;
 import model.factories.GraphFactory2;
 import model.factories.VertexFactory;
 
 import java.io.IOException;
import model.MyEdge;
import org.apache.commons.collections15.Transformer;
 
 /**
  * @author reseter
  */
 public class PajekParser {
 
     private PajekParser() {
         //only static methods
     }
 
     public static ObservableGraph load(String path) throws IOException {
         VertexFactory vf = Controller.getVertexFactory();
         vf.reset();
         EdgeFactory ef = Controller.getEdgeFactory();
         ef.reset();
         PajekNetReader reader = new PajekNetReader(vf, ef);
         return (ObservableGraph) reader.load(path, new GraphFactory2());
     }
 
     public static void save(String path, Graph g) throws IOException {
         PajekNetWriter writer = new PajekNetWriter();
        writer.save(g, path, new Transformer<Object, String>() {

            @Override
            public String transform(Object graphElement) {
                return graphElement.toString();  //no vertex labels
            }
        }, new Transformer<MyEdge, Number>() {

            @Override
            public Number transform(MyEdge o) {
                return o.getWeigth();
            }
        });
     }
 }
