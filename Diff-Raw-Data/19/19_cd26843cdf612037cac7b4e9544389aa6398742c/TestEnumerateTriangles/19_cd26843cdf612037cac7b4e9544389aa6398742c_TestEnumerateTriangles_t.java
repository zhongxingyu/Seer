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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Vector;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.mapreduce.TaskAttemptID;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;
 import org.apache.mahout.common.MahoutTestCase;
 import org.apache.mahout.graph.model.GenericGraphElement;
 import org.apache.mahout.graph.model.Membership;
 import org.apache.mahout.graph.model.Parser;
 import org.apache.mahout.graph.model.RepresentativeEdge;
 import org.apache.mahout.graph.model.SimpleParser;
 import org.apache.mahout.graph.model.Triangle;
 import org.apache.mahout.graph.model.Vertex;
 import org.junit.Test;
 
 import com.google.common.io.Resources;
 
 public class TestEnumerateTriangles extends MahoutTestCase {
 
   @Test
   public void testEnumerateTrianglesJob() throws Exception {
 
     // run simplification first
     File inputFile = new File(Resources.getResource("trianglestest.csv").toURI());
     assertTrue(inputFile.canRead());
     File outputDir = getTestTempDir("simplifytest-out");
     File tempDir = getTestTempDir("simplifytest-tmp");
     outputDir.delete();
     tempDir.delete();
     Configuration conf = new Configuration();
     SimplifyGraphJob simplifyGraphJob = new SimplifyGraphJob();
     simplifyGraphJob.setConf(conf);
     simplifyGraphJob.run(new String[] { "--input", inputFile.getAbsolutePath(),
         "--output", outputDir.getAbsolutePath(), "--tempDir",
         tempDir.getAbsolutePath() });
 
     FileSystem sys = FileSystem.get(conf);
 
     File intermedediateFile = new File(outputDir, "part-r-00000");
 
     // augment the simplified graph
     AugmentGraphWithDegreesJob augmentJob = new AugmentGraphWithDegreesJob();
     augmentJob.setConf(conf);
     assertTrue(intermedediateFile.canRead());
 
     outputDir = getTestTempDir("augmenttest-out");
     tempDir = getTestTempDir("augmenttest-tmp");
     outputDir.delete();
     tempDir.delete();
 
     augmentJob.run(new String[] { "--input",
         intermedediateFile.getAbsolutePath(), "--output",
         outputDir.getAbsolutePath(), "--tempDir", tempDir.getAbsolutePath() });
 
     intermedediateFile = new File(outputDir, "part-r-00000");
 
     // enumerate the triangles
     EnumerateTrianglesJob enumerateJob = new EnumerateTrianglesJob();
     enumerateJob.setConf(conf);
     assertTrue(intermedediateFile.canRead());
 
     outputDir = getTestTempDir("trianglestest-out");
     tempDir = getTestTempDir("trianglestest-tmp");
     outputDir.delete();
     tempDir.delete();
 
     enumerateJob.run(new String[] { "--input",
         intermedediateFile.getAbsolutePath(), "--output",
         outputDir.getAbsolutePath(), "--tempDir", tempDir.getAbsolutePath() });
 
     Path output = new Path(
         new File(outputDir, "part-r-00000").getAbsolutePath());
 
     FileStatus outputStat = sys.getFileStatus(output);
 
     HashMap<Membership, Triangle> triangles = getTestFileContents(
         inputFile, sys, conf);
     FileSplit s = new FileSplit(output, 0L, outputStat.getLen(), new String[0]);
     SequenceFileRecordReader<Membership, GenericGraphElement> r = new SequenceFileRecordReader<Membership, GenericGraphElement>();
     r.initialize(s, new TaskAttemptContext(conf, new TaskAttemptID()));
 
     while (r.nextKeyValue()) {
       Membership m = r.getCurrentKey();
       Triangle t = (Triangle) r.getCurrentValue().getValue();
       
       System.out.println(String.format(
           "Job returned %s binned under membership %s. Testing map...", t, m));
       Triangle test = triangles.remove(m);
       assertEquals(test, t);
 
     }
     assertTrue(String.format("%s should have been empty.", triangles), triangles.isEmpty());
   }
 
   private HashMap<Membership, Triangle> getTestFileContents(File file, FileSystem sys,
       Configuration conf) throws IOException {
     Path path = new Path(file.getAbsolutePath());
     FileStatus stat = sys.getFileStatus(path);
     FileSplit s = new FileSplit(path, 0L, stat.getLen(), new String[0]);
     Parser parser = new SimpleParser();
    HashMap<Vertex, Membership> vertices = new HashMap<Vertex, Membership>();
     LineRecordReader l = new LineRecordReader();
     l.initialize(s, new TaskAttemptContext(conf, new TaskAttemptID()));
     while (l.nextKeyValue()) {
       Text t = l.getCurrentValue();
       Vector<Vertex> members = parser.parse(t);
       if (members != null && members.size() > 1) {
         for (Vertex vertex : members) {
          Membership neighbours = vertices.containsKey(vertex) ? vertices
               .get(vertex) : new Membership();
           for (Vertex v : members)
             if (!v.equals(vertex))
               neighbours.addMember(v);
          vertices.put(vertex, neighbours);
         }
       }
     }
     HashMap<Membership, Triangle> triangles = new HashMap<Membership, Triangle>();
     HashSet<Vertex> visited = new HashSet<Vertex>();
    for (Vertex v1 : vertices.keySet()) {
      for (Vertex v2 : vertices.get(v1).getMembers()) {
         if (visited.contains(v2))
           continue;
        for (Vertex v3 : vertices.get(v2).getMembers()) {
           if (visited.contains(v2))
             continue;
          if (vertices.get(v3).getMembers().contains(v1)) {
             Triangle t = new Triangle();
             t.addEdge(new RepresentativeEdge(v1, v2));
             t.addEdge(new RepresentativeEdge(v1, v3));
             t.addEdge(new RepresentativeEdge(v2, v3));
             Membership key = new Membership().addMember(v1).addMember(v2).addMember(v3);
             triangles.put(key, t);
           }
         }
       }
       visited.add(v1);
     }
     return triangles;
   }
 }
