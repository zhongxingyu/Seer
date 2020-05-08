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
 
 package org.apache.mahout.graph.components;
 
 import java.util.Map;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.mahout.common.AbstractJob;
 import org.apache.mahout.graph.common.AugmentGraphWithDegrees.JoinDegrees;
 import org.apache.mahout.graph.common.AugmentGraphWithDegrees.ScatterEdges;
 import org.apache.mahout.graph.common.AugmentGraphWithDegrees.SumDegrees;
 import org.apache.mahout.graph.common.EnumerateTriangles.BuildOpenTriads;
 import org.apache.mahout.graph.common.EnumerateTriangles.BuildTriangles;
 import org.apache.mahout.graph.common.EnumerateTriangles.ScatterEdgesToLowerDegreeVertex;
 import org.apache.mahout.graph.common.SimplifyGraph.SimplifyGraphMapper;
 import org.apache.mahout.graph.common.SimplifyGraph.SimplifyGraphReducer;
 import org.apache.mahout.graph.components.FindKTrusses.DropUnsupportedEdges;
 import org.apache.mahout.graph.components.FindKTrusses.SplitTrianglesToEdges;
 import org.apache.mahout.graph.model.GenericGraphElement;
 import org.apache.mahout.graph.model.Membership;
 import org.apache.mahout.graph.model.Parser;
 
 /**
  * Find <code>k</code>-trusses in a graph. This algorithm works as follows:
  * <p>
  * First simplify the graph. See
  * {@link org.apache.mahout.graph.common.SimplifyGraphJob}.
  * <p>
  * At least once and as long as the last step drops any edges:
  * <ol>
  * <li>Augment the graph with degrees. See
  * {@link org.apache.mahout.graph.common.AugmentGraphWithDegreesJob}</li>
  * <li>Enumerate the triangles of the graph. See
  * {@link org.apache.mahout.graph.common.EnumerateTrianglesJob}</li>
  * <li>For each edge record the number of triangles containing that edge and
  * keep only edges with sufficient support using classes in {@link FindKTrusses}
  * .</li>
  * </ol>
  * 
  * <p>
  * Find the components of the remaining graph, each one is a truss
  * {@link FindComponentsJob}.
  * 
  * <p>
  * The input file format is a {@link TextInputFormat} which can be parsed via an
  * implementation of {@link Parser}.
  * 
  * <p>
  * This job accepts the following input arguments:
  * <dl>
  * <dt>input</dt>
  * <dd>The path of the input file or directory</dd>
  * <dt>output</dt>
  * <dd>The path of output directory</dd>
  * <dt>org.apache.mahout.graph.model.Parser</dt>
  * <dd>An optional class implementing
  * {@link org.apache.mahout.graph.model.Parser} that can be used to parse a
  * variety of graphs</dd>
  * <dt>k</dt>
  * <dd>The <code>k</code> parameter of the k-trusses to find
  * </dl>
  * 
  * The output is a {@link SequenceFile} containing a {@link Membership} as key
 * and a {@link ZoneAssignment} as value (contained in a
  * {@link GenericGraphElement}).
  */
 public class FindKTrussesJob extends AbstractJob {
 
   public static void main(String[] args) throws Exception {
     ToolRunner.run(new FindKTrussesJob(), args);
   }
 
   @Override
   public int run(String[] args) throws Exception {
     addInputOption();
     addOutputOption();
     addOption(
         Parser.class.getCanonicalName(),
         Parser.class.getCanonicalName(),
         "A class implementing the Parser interface that should be used to parse the graph file.");
 
     addOption("k", "k", "The k parameter of the k-trusses to find.");
 
     Map<String, String> parsedArgs = parseArguments(args);
     if (parsedArgs == null) {
       return -1;
     }
 
     Path inputPath = getInputPath();
     Path outputPath = getOutputPath();
 
     Path tempDirPath = new Path(parsedArgs.get("--tempDir"));
     int k = Integer.parseInt(parsedArgs.get("--k")); // extract parameter
 
     /*
      * Simplify the graph first
      */
     Path simplifyInputPath = inputPath;
     Path simplifyOutputPath = new Path(tempDirPath, String.valueOf(System.currentTimeMillis()));
     Job simplify = prepareJob(simplifyInputPath, simplifyOutputPath,
         TextInputFormat.class, SimplifyGraphMapper.class, Membership.class,
         GenericGraphElement.class, SimplifyGraphReducer.class,
         Membership.class, GenericGraphElement.class,
         SequenceFileOutputFormat.class);
     String parserImplementationClass = parsedArgs.get("--"
         + Parser.class.getCanonicalName()); // extract parameter
     if (parserImplementationClass != null) { // pass parser parameter to the job
                                              // if set
       simplify.getConfiguration().set(Parser.class.getCanonicalName(),
           parserImplementationClass);
     }
     simplify.waitForCompletion(true);
 
     boolean isDroppedSupportedEdges = true;
     Path currentTrussesDirPath = simplifyOutputPath;
     while (isDroppedSupportedEdges) {
 
       /*
        * Augment the simplified graph with degrees
        */
       // scatter the edges to each of the vertices and count degree
       Path augmentInputPath = currentTrussesDirPath;
       Path augmentTempDirPath = new Path(tempDirPath, String.valueOf(System.currentTimeMillis()));
       Job scatter = prepareJob(augmentInputPath, augmentTempDirPath,
           SequenceFileInputFormat.class, ScatterEdges.class, Membership.class,
           GenericGraphElement.class, SumDegrees.class, Membership.class,
           GenericGraphElement.class, SequenceFileOutputFormat.class);
       scatter.waitForCompletion(true);
       // join augmented edges with partial degree information to to complete
       // records
       Path augmentOutputPath = new Path(tempDirPath, String.valueOf(System.currentTimeMillis()));
       Job join = prepareJob(augmentTempDirPath, augmentOutputPath,
           SequenceFileInputFormat.class, Mapper.class, Membership.class,
           GenericGraphElement.class, JoinDegrees.class, Membership.class,
           GenericGraphElement.class, SequenceFileOutputFormat.class);
       join.waitForCompletion(true);
 
       /*
        * Enumerate triangles in the graph
        */
       Path enumerateInputPath = augmentOutputPath;
       // scatter the edges to lower degree vertex and build open triads
       Path enumerateTempDirPath = new Path(tempDirPath, String.valueOf(System.currentTimeMillis()));
       Job scatterToLowerDegreeVertex = prepareJob(enumerateInputPath,
           enumerateTempDirPath, SequenceFileInputFormat.class,
           ScatterEdgesToLowerDegreeVertex.class, Membership.class,
           GenericGraphElement.class, BuildOpenTriads.class, Membership.class,
           GenericGraphElement.class, SequenceFileOutputFormat.class);
       scatterToLowerDegreeVertex.waitForCompletion(true);
       // join triads and edges pairwise to get all triangles
       Path enumerateOutputPath = new Path(tempDirPath, String.valueOf(System.currentTimeMillis()));
       Job joinTriadsAndEdges = prepareJob(new Path(enumerateTempDirPath + ","
           + enumerateInputPath), enumerateOutputPath,
           SequenceFileInputFormat.class, Mapper.class, Membership.class,
           GenericGraphElement.class, BuildTriangles.class, Membership.class,
           GenericGraphElement.class, SequenceFileOutputFormat.class);
       joinTriadsAndEdges.waitForCompletion(true);
 
       /*
        * Drop edges with insufficient support
        */
       Path checkSupportInputPath = enumerateOutputPath;
 //      Path checkSupportOutputPath = new Path(tempDirPath, String.valueOf(System.currentTimeMillis()));
       Path checkSupportOutputPath = outputPath; // FIXME remove this when job is complete
       Job checkTrianglesForSupport = prepareJob(checkSupportInputPath,
           checkSupportOutputPath, SequenceFileInputFormat.class,
           SplitTrianglesToEdges.class, Membership.class,
           GenericGraphElement.class, DropUnsupportedEdges.class,
           Membership.class, GenericGraphElement.class,
           SequenceFileOutputFormat.class);
       checkTrianglesForSupport.getConfiguration().setInt(FindKTrusses.K, k);
       checkTrianglesForSupport.waitForCompletion(true);
 
       // FIXME check for dropped edges and if no more dropped break
       currentTrussesDirPath = checkSupportOutputPath;
       if (isDroppedSupportedEdges == false || true) {
         break;
       }
 
     }
 
     /*
      * Find the components of the remaining graph
      */
     Path componentsInputPath = currentTrussesDirPath;
     // FIXME implement find components
 
     return 0;
 
   }
 }
