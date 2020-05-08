 /**
  * Copyright (c) 2012, Cloudera, Inc. All Rights Reserved.
  *
  * Cloudera, Inc. licenses this file to you under the Apache License,
  * Version 2.0 (the "License"). You may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package com.cloudera.science.matching.graph;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Map;
 
 import org.apache.giraph.utils.InternalVertexRunner;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.Test;
 
 import com.cloudera.science.matching.VertexData;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 
 public class BipartiteMatchingTest {
 
   ObjectMapper mapper = new ObjectMapper();
   
   private Map<String, VertexData> run(String[] data) throws Exception {
     Iterable<String> res = InternalVertexRunner.run(BipartiteMatchingVertex.class,
         BipartiteMatchingVertexInputFormat.class,
         BipartiteMatchingVertexOutputFormat.class,
         Maps.<String, String>newHashMap(), data);
     
     Map<String, VertexData> out = Maps.newHashMap();
     for (String line : res) {
       VertexData d = mapper.readValue(line, VertexData.class);
       out.put(d.getVertexId(), d);
     }
     return out;
   }
   
   @Test
   public void testSimpleMatching() throws Exception {
     VertexData d1 = new VertexData("1", true, ImmutableMap.of("3", 2, "4", 1)); 
     VertexData d2 = new VertexData("2", true, ImmutableMap.of("3", 1, "4", 2));
     VertexData d3 = new VertexData("3", false, ImmutableMap.of("1", -1, "2", -1));
     VertexData d4 = new VertexData("4", false, ImmutableMap.of("1", -1, "2", -1));
     
     String[] data = new String[] { mapper.writeValueAsString(d1),
         mapper.writeValueAsString(d2),
         mapper.writeValueAsString(d3),
         mapper.writeValueAsString(d4),
     };
     Map<String, VertexData> out = run(data);
     assertEquals("3", out.get("1").getMatchId());
     assertEquals("4", out.get("2").getMatchId());
     assertEquals("1", out.get("3").getMatchId());
     assertEquals("2", out.get("4").getMatchId());
   }
   
   @Test
   public void testIndifferentVertex() throws Exception {
     VertexData d1 = new VertexData("1", true, ImmutableMap.of("3", 2, "4", 2)); 
     VertexData d2 = new VertexData("2", true, ImmutableMap.of("3", 1, "4", 3));
     VertexData d3 = new VertexData("3", false, ImmutableMap.of("1", -1, "2", -1));
     VertexData d4 = new VertexData("4", false, ImmutableMap.of("1", -1, "2", -1));
     
     String[] data = new String[] { mapper.writeValueAsString(d1),
         mapper.writeValueAsString(d2),
         mapper.writeValueAsString(d3),
         mapper.writeValueAsString(d4),
     };
     Map<String, VertexData> out = run(data);
     assertEquals("3", out.get("1").getMatchId());
     assertEquals("4", out.get("2").getMatchId());
     assertEquals("1", out.get("3").getMatchId());
     assertEquals("2", out.get("4").getMatchId());
   }
   
   @Test
   public void testBothIndifferent() throws Exception {
     VertexData d1 = new VertexData("1", true, ImmutableMap.of("3", 2, "4", 2)); 
     VertexData d2 = new VertexData("2", true, ImmutableMap.of("3", 2, "4", 2));
     VertexData d3 = new VertexData("3", false, ImmutableMap.of("1", -1, "2", -1));
     VertexData d4 = new VertexData("4", false, ImmutableMap.of("1", -1, "2", -1));
     
     String[] data = new String[] { mapper.writeValueAsString(d1),
         mapper.writeValueAsString(d2),
         mapper.writeValueAsString(d3),
         mapper.writeValueAsString(d4),
     };
     Map<String, VertexData> out = run(data);
     assertEquals("3", out.get("1").getMatchId());
     assertEquals("4", out.get("2").getMatchId());
     assertEquals("1", out.get("3").getMatchId());
     assertEquals("2", out.get("4").getMatchId());
   }
   
   @Test
   public void testSameDirectionalPrefs() throws Exception {
     VertexData d1 = new VertexData("1", true, ImmutableMap.of("3", 1, "4", 2)); 
     VertexData d2 = new VertexData("2", true, ImmutableMap.of("3", 1, "4", 3));
     VertexData d3 = new VertexData("3", false, ImmutableMap.of("1", -1, "2", -1));
     VertexData d4 = new VertexData("4", false, ImmutableMap.of("1", -1, "2", -1));
     
     String[] data = new String[] { mapper.writeValueAsString(d1),
         mapper.writeValueAsString(d2),
         mapper.writeValueAsString(d3),
         mapper.writeValueAsString(d4),
     };
     Map<String, VertexData> out = run(data);
     assertEquals("3", out.get("1").getMatchId());
     assertEquals("4", out.get("2").getMatchId());
     assertEquals("1", out.get("3").getMatchId());
     assertEquals("2", out.get("4").getMatchId());
   }
   
   @Test
   public void testSameDirectionalIdenticalPrefs() throws Exception {
     VertexData d1 = new VertexData("1", true, ImmutableMap.of("3", 1, "4", 3)); 
     VertexData d2 = new VertexData("2", true, ImmutableMap.of("3", 1, "4", 3));
     VertexData d3 = new VertexData("3", false, ImmutableMap.of("1", -1, "2", -1));
     VertexData d4 = new VertexData("4", false, ImmutableMap.of("1", -1, "2", -1));
     
     String[] data = new String[] { mapper.writeValueAsString(d1),
         mapper.writeValueAsString(d2),
         mapper.writeValueAsString(d3),
         mapper.writeValueAsString(d4),
     };
     Map<String, VertexData> out = run(data);
     assertEquals("4", out.get("1").getMatchId());
     assertEquals("3", out.get("2").getMatchId());
     assertEquals("2", out.get("3").getMatchId());
     assertEquals("1", out.get("4").getMatchId());
   }
   
   @Test
   public void testUnassignedObject() throws Exception {
     VertexData d1 = new VertexData("1", true, ImmutableMap.of("3", 1, "4", 3, "5", 2)); 
     VertexData d2 = new VertexData("2", true, ImmutableMap.of("3", 3, "4", 1, "5", 2));
     VertexData d3 = new VertexData("3", false, ImmutableMap.of("1", -1, "2", -1));
     VertexData d4 = new VertexData("4", false, ImmutableMap.of("1", -1, "2", -1));
     VertexData d5 = new VertexData("5", false, ImmutableMap.of("1", -1, "2", -1));
     
     String[] data = new String[] { mapper.writeValueAsString(d1),
         mapper.writeValueAsString(d2),
         mapper.writeValueAsString(d3),
         mapper.writeValueAsString(d4),
         mapper.writeValueAsString(d5),
     };
     Map<String, VertexData> out = run(data);
     
     assertEquals("4", out.get("1").getMatchId());
     assertEquals("3", out.get("2").getMatchId());
     assertEquals("2", out.get("3").getMatchId());
     assertEquals("1", out.get("4").getMatchId());
     assertEquals("", out.get("5").getMatchId());
   }
  
  @Test
  public void testSingleBidder() throws Exception {
    VertexData d1 = new VertexData("1", true, ImmutableMap.of("4", 3)); 
    VertexData d2 = new VertexData("2", true, ImmutableMap.of("3", 1, "4", 3));
    VertexData d3 = new VertexData("3", false, ImmutableMap.of("2", -1));
    VertexData d4 = new VertexData("4", false, ImmutableMap.of("1", -1, "2", -1));
    
    String[] data = new String[] { mapper.writeValueAsString(d1),
        mapper.writeValueAsString(d2),
        mapper.writeValueAsString(d3),
        mapper.writeValueAsString(d4),
    };
    Map<String, VertexData> out = run(data);
    assertEquals("4", out.get("1").getMatchId());
    assertEquals("3", out.get("2").getMatchId());
    assertEquals("2", out.get("3").getMatchId());
    assertEquals("1", out.get("4").getMatchId());
  }
 }
