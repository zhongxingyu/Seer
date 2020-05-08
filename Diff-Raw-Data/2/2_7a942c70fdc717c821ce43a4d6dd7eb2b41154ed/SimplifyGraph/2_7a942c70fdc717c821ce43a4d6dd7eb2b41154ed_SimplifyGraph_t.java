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
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.mahout.graph.model.Membership;
 import org.apache.mahout.graph.model.Parser;
 import org.apache.mahout.graph.model.RepresentativeEdge;
 import org.apache.mahout.graph.model.SimpleParser;
 import org.apache.mahout.graph.model.Vertex;
 
 public class SimplifyGraph {
 
   public static class SimplifyGraphMapper extends
           Mapper<Object, Text, Membership, RepresentativeEdge> {
 
     Parser parser;
 
     @Override
     public void setup(Context ctx) {
       Configuration conf = ctx.getConfiguration();
       String classname = conf.get(Parser.class.getCanonicalName());
      if( classname!=null ) try {
         @SuppressWarnings("unchecked")
         Class<Parser> parserclass = (Class<Parser>) Class.forName(classname);
         parser = (Parser) parserclass.newInstance();
       } catch (ClassNotFoundException e) {
         e.printStackTrace();
         // TODO log this error
       } catch (InstantiationException e) {
         // TODO log this error
         e.printStackTrace();
       } catch (IllegalAccessException e) {
         // TODO log this error
         e.printStackTrace();
       }
       if (parser == null) {
         parser = new SimpleParser();
       }
 
     }
 
     @Override
     public void map(Object key, Text description, Context ctx)
             throws IOException, InterruptedException {
 
       Vector<Vertex> members = parser.parse(description);
       if (members.size() > 1) {
         Iterator<Vertex> i = members.iterator();
         Vertex node0 = i.next();
         Vertex node1 = i.next();
         RepresentativeEdge edge = new RepresentativeEdge(node0, node1);
         Membership mem = new Membership();
         mem.setMembers(members);
         ctx.write(mem, edge);
       }
 
     }
   }
 
   public static class SimplifyGraphReducer extends
           Reducer<Membership, RepresentativeEdge, Membership, RepresentativeEdge> {
 
     @Override
     public void reduce(Membership key, Iterable<RepresentativeEdge> values,
             Context ctx) throws InterruptedException, IOException {
 
       Map<RepresentativeEdge, RepresentativeEdge> edges =
               new HashMap<RepresentativeEdge, RepresentativeEdge>();
       for (RepresentativeEdge edge : values) {
         RepresentativeEdge prev = edges.get(edge);
         if (prev != null) {
           // TODO implement aggregation
         }
         edges.put(edge, edge);
       }
       for (RepresentativeEdge edge : edges.values()) {
         ctx.write(key, edge);
       }
     }
   }
 }
