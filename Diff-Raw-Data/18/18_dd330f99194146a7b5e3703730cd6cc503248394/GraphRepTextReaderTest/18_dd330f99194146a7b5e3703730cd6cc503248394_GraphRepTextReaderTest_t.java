 /*
  * Copyright 2012 ToureNPlaner
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package de.tourenplaner.graphrep;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  */
 public class GraphRepTextReaderTest {
 
    // TODO: Test that works with reordering the graph and/or better test graph
    //@Test
     public final void testCreateGraphRep() {
         GraphRep graphRep = (new TestGraphReader()).readTestGraph();
         assertEquals(14505, graphRep.getNodeCount());
         assertEquals(59600, graphRep.getEdgeCount());
         assertEquals(4.84878025E8, graphRep.lat[0], 0.01);
         assertEquals(4.84748005E8, graphRep.lat[100], 0.01);
         assertEquals(4.84010105E8, graphRep.lat[1000], 0.01);
         assertEquals(4.84724514E8, graphRep.lat[14504], 0.01);
 
         assertEquals(9.1901961E7, graphRep.lon[0], 0.01);
         assertEquals(9.1363799E7, graphRep.lon[100], 0.01);
         assertEquals(9.0469706E7, graphRep.lon[1000], 0.01);
         assertEquals(9.1529005E7, graphRep.lon[14504], 0.01);
 
         assertEquals(386, graphRep.height[0]);
         assertEquals(435, graphRep.height[100]);
         assertEquals(475, graphRep.height[1000]);
         assertEquals(430, graphRep.height[14504]);
 
         assertEquals(0, graphRep.src[0]);
         assertEquals(0, graphRep.src[1]);
         assertEquals(0, graphRep.src[2]);
         assertEquals(25, graphRep.src[100]);
         assertEquals(221, graphRep.src[1000]);
         assertEquals(3027, graphRep.src[14504]);
 
         assertEquals(1, graphRep.trgt[0]);
         assertEquals(1823, graphRep.trgt[1]);
         assertEquals(1824, graphRep.trgt[2]);
         assertEquals(1949, graphRep.trgt[100]);
         assertEquals(222, graphRep.trgt[1000]);
         assertEquals(3238, graphRep.trgt[14504]);
 
         assertEquals(22, graphRep.getEuclidianDist(0));
         assertEquals(99, graphRep.getEuclidianDist(1));
         assertEquals(66, graphRep.getEuclidianDist(2));
         assertEquals(115, graphRep.getEuclidianDist(100));
         assertEquals(36, graphRep.getEuclidianDist(1000));
         assertEquals(38, graphRep.getEuclidianDist(14504));
 
         assertEquals(22, graphRep.dist[0]);
         assertEquals(124, graphRep.dist[1]);
         assertEquals(66, graphRep.dist[2]);
         assertEquals(116, graphRep.dist[100]);
         assertEquals(36, graphRep.dist[1000]);
         assertEquals(38, graphRep.dist[14504]);
 
         assertEquals(0, graphRep.offsetOut[0]);
         assertEquals(5, graphRep.offsetOut[1]);
         assertEquals(444, graphRep.offsetOut[100]);
         assertEquals(5045, graphRep.offsetOut[1000]);
         assertEquals(59599, graphRep.offsetOut[14504]);
 
         assertEquals(0, graphRep.offsetIn[0]);
         assertEquals(5, graphRep.offsetIn[1]);
         assertEquals(444, graphRep.offsetIn[100]);
         assertEquals(4961, graphRep.offsetIn[1000]);
         assertEquals(59599, graphRep.offsetIn[14504]);
     }
 }
