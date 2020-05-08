 /*
  * Software License Agreement (BSD License)
  * 
  * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
  * All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  * 
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  * 
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  * 
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute 
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package es.csic.iiia.dcop.cli;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import es.csic.iiia.dcop.CostFunction;
 import es.csic.iiia.dcop.CostFunctionFactory;
 import es.csic.iiia.dcop.FactorGraph;
 import es.csic.iiia.dcop.Variable;
 import es.csic.iiia.dcop.VariableAssignment;
 import es.csic.iiia.dcop.algo.JunctionTreeAlgo;
 import es.csic.iiia.dcop.algo.MaxSum;
 import es.csic.iiia.dcop.algo.RandomNoiseAdder;
 import es.csic.iiia.dcop.bb.UBGraph;
 import es.csic.iiia.dcop.bb.UBResults;
 import es.csic.iiia.dcop.up.UPResults;
 import es.csic.iiia.dcop.dfs.DFS;
 import es.csic.iiia.dcop.dsa.DSA;
 import es.csic.iiia.dcop.dsa.DSAResults;
 import es.csic.iiia.dcop.gdl.GdlFactory;
 import es.csic.iiia.dcop.gdlf.GdlFFactory;
 import es.csic.iiia.dcop.gdlf.GdlFGraph;
 import es.csic.iiia.dcop.gdlf.strategies.AbstractMixedStrategy;
 import es.csic.iiia.dcop.gdlf.strategies.GdlFStrategy;
 import es.csic.iiia.dcop.io.CliqueTreeSerializer;
 import es.csic.iiia.dcop.io.DatasetReader;
 import es.csic.iiia.dcop.io.TreeReader;
 import es.csic.iiia.dcop.jt.JTResults;
 import es.csic.iiia.dcop.jt.JunctionTree;
 import es.csic.iiia.dcop.mp.AbstractNode.Modes;
 import es.csic.iiia.dcop.up.UPFactory;
 import es.csic.iiia.dcop.up.UPGraph;
 import es.csic.iiia.dcop.util.Compressor;
 import es.csic.iiia.dcop.util.ConstantFactorExtractor;
 import es.csic.iiia.dcop.util.MemoryTracker;
 import es.csic.iiia.dcop.util.UnaryVariableFilterer;
 import es.csic.iiia.dcop.vp.VPGraph;
 import es.csic.iiia.dcop.vp.VPResults;
 import es.csic.iiia.dcop.vp.strategy.VPStrategy;
 import es.csic.iiia.dcop.vp.strategy.expansion.StochasticalExpansion;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Command Line Interface application logic handler.
  *
  * 
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public class CliApp {
 
     private static Logger log = LoggerFactory.getLogger(Cli.class);
 
     private void printInformation() {
         log.info("[Info] Algorithm: " + algorithm.toString());
         log.info("[Info] Summarize: " + summarizeOperation.toString());
         log.info("[Info] Combine: " + combineOperation.toString());
         log.info("[Info] Normalize: " + normalization.toString());
         if (algorithm == Algorithm.GDLF) {
             log.info("[Info] Approximation: " + approximationStrategy.toString());
             if (approximationStrategy == ApproximationStrategies.MIXED_NOSLICE
                     || approximationStrategy == ApproximationStrategies.MIXED_SLICE
                     || approximationStrategy == ApproximationStrategies.MIXED_USLICE) {
                 log.info("[Info] Delta: " + delta);
             }
             log.info("[Info] Number-of-solutions: " + VPStrategy.numberOfSolutions);
             log.info("[Info] Solution-expansion: " + expansionStrategy.toString());
             if (expansionStrategy == SolutionExpansionStrategies.STOCHASTIC) {
                 log.info("[Info] Expansion-probability: " + StochasticalExpansion.p);
             }
             log.info("[Info] Solution-exploration: " + solvingStrategy.toString());
         }
     }
 
     /**
      * Output formatting choser
      */
     private OutputFormat outputFormat = OutputFormat.CUSTOM;
 
     private Algorithm algorithm = Algorithm.GDL;
     private CostFunction.Summarize summarizeOperation = CostFunction.Summarize.MAX;
     private CostFunction.Combine combineOperation = CostFunction.Combine.SUM;
     private CostFunction.Normalize normalization = CostFunction.Normalize.NONE;
     private int maxCliqueVariables = 14;
     private double randomVariance = 0;
 
     /**
      * Junction Tree building parameters
      */
     private JTBuildingHeuristic heuristic = JTBuildingHeuristic.RANDOM;
     private int maxJunctionTreeTries = 30;
 
     /**
      * Tree/Graph import/export options
      */
     private boolean createCliqueTree = false;
     private boolean createCliqueGraph = false;
     private boolean createFactorGraph = false;
     private InputStream treeFile = null;
     private String cliqueGraphFile = "cgraph.dot";
     private String factorGraphFile = "fgraph.dot";
     private String cliqueTreeFile  = "problem.tree";
 
 
     /**
      * Algorithm tracing options
      */
     private boolean createTraceFile = false;
     private String traceFile = "trace.txt";
 
 
     /**
      * Strategies to use for GDL with filtering
      */
     private ApproximationStrategies approximationStrategy = ApproximationStrategies.DCR_BOTTOM_UP;
     private SolutionExpansionStrategies expansionStrategy = SolutionExpansionStrategies.GREEDY;
     private SolutionSolvingStrategies solvingStrategy = SolutionSolvingStrategies.OPTIMAL;
 
     private int delta = 0;
     private int IGdlR = 2;
     private InputStream input = System.in;
 
 
     private void outputVariableStatistics(List<CostFunction> factors) {
         // Collect all variables
         HashSet<Variable> varSet = new HashSet<Variable>();
         for (CostFunction f : factors) {
             varSet.addAll(f.getVariableSet());
         }
 
         // Compute min/max/avg
         int min = Integer.MAX_VALUE;
         int max = Integer.MIN_VALUE;
         double avg = 0;
         for (Variable v : varSet) {
             int domain = v.getDomain();
             if (domain < min) min = domain;
             if (domain > max) max = domain;
             avg += domain;
         }
         avg /= (double)varSet.size();
 
         // Output stats
         log.info("VARIABLES " + varSet.size());
         log.info("MIN_DOMAIN " + min);
         log.info("AVG_DOMAIN " + avg);
         log.info("MAX_DOMAIN " + max);
     }
 
     void run() {
         // Setup log handling
         setupLogHandling();
 
         // Parameter combination checks
         if (algorithm == Algorithm.MAX_SUM && normalization == CostFunction.Normalize.NONE) {
             System.err.println("Warning: maxsum doesn't converge without normalization, using sum0.");
             normalization = CostFunction.Normalize.SUM0;
         }
         if (algorithm == Algorithm.GDLF && summarizeOperation == CostFunction.Summarize.SUM) {
             System.err.println("Error: figdl can not work with sum summarization.");
             System.exit(1);
         }
 
         // Read the input file into factors
         DatasetReader r = new DatasetReader();
         CostFunctionFactory factory = new CostFunctionFactory();
         factory.setCombineOperation(combineOperation);
         factory.setNormalizationType(normalization);
         factory.setSummarizeOperation(summarizeOperation);
         List<CostFunction> factors = r.read(input, factory);
 
         printInformation();
 
         // Filter out unary variables
         VariableAssignment unaries = UnaryVariableFilterer.filterVariables(factors);
         if (unaries.size() > 0) {
             log.info("[Info] " + unaries.size() + " unary variables found.");
         }
 
         // Output factor graph
         FactorGraph fg = new FactorGraph(factors);
         createFactorGraphFile(fg);
         // Output total number of variables and min/avg/max domain
         outputVariableStatistics(factors);
 
        // DSA Can solve from here
         VariableAssignment map = null;
        CostFunction constant = factory.buildCostFunction(new Variable[0]);
 
         if (algorithm == Algorithm.DSA) {
 
             DSA dsa = new DSA(fg);
             DSAResults res = dsa.run(10000);
             map = res.getGlobalAssignment();
             log.info("ITERATIONS " + res.getIterations());
             
         } else {
             
             // Positivize if figdl
             boolean inverse = false;
             if (algorithm == Algorithm.GDLF) {
                 // Invert the problem (max -> min)
                 if (summarizeOperation == CostFunction.Summarize.MAX) {
                     factory.setSummarizeOperation(CostFunction.Summarize.MIN);
                     ConstantFactorExtractor.invert(factors);
                     constant = constant.invert();
                     inverse = true;
                 }
                 constant = ConstantFactorExtractor.positivize(factors, constant);
             }
             // Remove constant factors
             constant = ConstantFactorExtractor.extract(factors, constant);
 
             // Create the clique graph, using the specified algorithm
             UPGraph cg = createCliqueGraph(factors, constant);
 
             // Setup the solution propagation strategy
             VPStrategy sStrategy = new VPStrategy(
                     expansionStrategy.getInstance(),
                     solvingStrategy.getInstance()
             );
             if (algorithm == Algorithm.GDLF && cg instanceof GdlFGraph) {
                 GdlFGraph.setSolutionStrategy(sStrategy);
             }
 
             // Add noise if requested
             if (randomVariance != 0) {
                 RandomNoiseAdder rna = new RandomNoiseAdder(randomVariance);
                 rna.addNoise(cg);
             }
 
             // Run the solving algorithm
             cg.setFactory(factory);
             UPResults results = cg.run(1000);
             UBResults ubres = null;
 
             if (algorithm == Algorithm.GDLF && cg instanceof GdlFGraph) {
                 ubres = ((GdlFGraph)cg).getUBResults();
             } else {
                 VPGraph st = new VPGraph(cg, sStrategy);
                 VPResults res = st.run(10000);
                 results.mergeResults(res);
                 UBGraph ub = new UBGraph(st);
                 ubres = ub.run(1000);
                 results.mergeResults(ubres);
             }
 
             map = ubres.getMap();
             log.info("ITERATIONS " + results.getIterations());
             log.info("CBR " + results.getCBR(communicationCost));
             log.info("TOTAL_CCS " + results.getTotalCcc());
             log.info("CYCLE_CCS " + results.getMaximalCcc());
             log.info("TOTAL_BYTES " + results.getTotalBytesc());
             log.info("CYBLE_BYTES " + results.getMaximalBytesc());
             log.info("MAX_NODE_MEMORY " + 
                     MemoryTracker.toString(results.getMaximalMemoryc()) + " Mb");
             log.info("LOAD_FACTOR " + results.getLoadFactor());
             final double bound = ubres.getBound() + constant.getValue(0);
             log.info("BOUND " + (inverse ? -bound : bound));
         }
 
         map.putAll(unaries);
         SortedMap<Variable, Integer> foo = new TreeMap<Variable, Integer>(map);
         StringBuilder buf = new StringBuilder();
         if (outputFormat == outputFormat.UAI) {
             buf.append("MPE\n").append(foo.size());
         } else {
             buf.append("SOLUTION ");
         }
         for(Variable v : foo.keySet()) {
             buf.append(" ").append(map.get(v));
         }
         System.out.println(buf.toString());
 
         // Evaluate solution
         double cost = 0;
         factors.add(constant);
         if (algorithm == Algorithm.GDLF && summarizeOperation == CostFunction.Summarize.MAX) {
             ConstantFactorExtractor.invert(factors);
         }
         for (CostFunction f : factors) {
             cost = combineOperation.eval(cost, f.getValue(map));
         }
         log.info("COST " + cost);
         //log.info("MAX_NODE_MEMORY " + MemoryTracker.asString() + " Mb");
     }
 
     void setCreateCliqueGraph(boolean create) {
         createCliqueGraph = create;
     }
 
     void setCreateFactorGraph(boolean create) {
         createFactorGraph = create;
     }
 
     void setHeuristic(JTBuildingHeuristic heuristic) {
         this.heuristic = heuristic;
     }
 
     private void createCliqueGraphFile(UPGraph cg) {
         if (!createCliqueGraph) {
             return;
         }
 
         FileWriter fw;
         try {
             fw = new FileWriter(new File(cliqueGraphFile), false);
             fw.write(cg.toString());
             fw.close();
         } catch (IOException ex) {
             System.err.println("Error: " + ex.getLocalizedMessage());
             System.exit(1);
         }
     }
 
     private void createFactorGraphFile(FactorGraph fg) {
         if (!createFactorGraph) {
             return;
         }
 
         FileWriter fw;
         try {
             fw = new FileWriter(new File(factorGraphFile), false);
             fw.write(fg.toString());
             fw.close();
         } catch (IOException ex) {
             System.err.println("Error: " + ex.getLocalizedMessage());
             System.exit(1);
         }
     }
 
     private void createCliqueTreeFile(UPGraph cg) {
         if (!createCliqueTree) {
             return;
         }
 
         FileWriter fw;
         try {
             fw = new FileWriter(new File(cliqueTreeFile), false);
             CliqueTreeSerializer serializer = new CliqueTreeSerializer();
             fw.write(serializer.serializeTreeStructure(cg));
             fw.close();
         } catch (IOException ex) {
             System.err.println("Error: " + ex.getLocalizedMessage());
             System.exit(1);
         }
     }
 
     private UPGraph createCliqueGraph(List<CostFunction> factors, CostFunction constant) {
         
         UPGraph cg = null;
         switch(algorithm) {
 
             case GDL:
             case GDLF:
                 UPFactory factory = null;
                 if (algorithm == Algorithm.GDL) {
                     factory = new GdlFactory();
                     ((GdlFactory)factory).setMode(Modes.TREE_UP);
                 } else {
                     GdlFStrategy pStrategy = approximationStrategy.getInstance(this.getIGdlR());
                     if (pStrategy instanceof AbstractMixedStrategy) {
                         ((AbstractMixedStrategy)pStrategy).setDelta(delta);
                     }
                     factory = new GdlFFactory(constant, 
                             summarizeOperation == CostFunction.Summarize.MAX,
                             pStrategy);
                 }
                 int variables = 0;
                 JTResults results = null;
 
                 if (treeFile != null) {
                     TreeReader treeReader = new TreeReader();
                     treeReader.read(treeFile, factors);
                     cg = JunctionTreeAlgo.buildGraph(factory, treeReader.getFactorDistribution(), treeReader.getAdjacency());
                     cg.setRoot(treeReader.getRoot());
                     JunctionTree jt = new JunctionTree(cg);
                     results = jt.run(1000);
                     variables = results.getMaxVariables();
                     if (algorithm == Algorithm.GDLF) {
                         int newRoot = jt.getLowestDecisionRoot();
                         cg.setRoot(newRoot);
                     }
                 } else {
                     int minVariables = Integer.MAX_VALUE;
                     for(int i=0; i < maxJunctionTreeTries; i++) {
                         DFS dfs = heuristic.getInstance();
                         dfs.build(factors);
                         UPGraph candidateCg = null;
                         candidateCg = JunctionTreeAlgo.buildGraph(factory, dfs.getFactorDistribution(), dfs.getAdjacency());
                         candidateCg.setRoot(dfs.getRoot());
                         JunctionTree jt = new JunctionTree(candidateCg);
                         JTResults candidateResults = jt.run(10000);
                         variables = candidateResults.getMaxVariables();
                         log.warn("Generated junction tree (tw=" + (variables-1) + ")");
                         
                         if (variables < minVariables) {
                             minVariables = variables;
                             cg = candidateCg;
                             results = candidateResults;
                         }
                     }
                 }
                 
                 log.info("MAX_CLIQUE_VARIABLES " + results.getMaxVariables());
                 log.info("MAX_CLIQUE_SIZE " + results.getMaxSize());
                 log.info("MAX_EDGE_VARIABLES " + results.getMaxEdgeVariables());
 
                 JunctionTree jt = new JunctionTree(cg);
                 results = jt.run(1000);
                 variables = results.getMaxVariables();
                 if (algorithm == Algorithm.GDLF) {
                     int newRoot = jt.getLowestDecisionRoot();
                     cg.setRoot(newRoot);
                 }
                 log.info("[Info] Maximum-decision-variables: " + jt.getNumberOfDecisionVariables(cg.getRoot()));
                 //System.out.println(jt.getTreeOfDecisionVariables(newRoot));
 
                 createCliqueGraphFile(cg);
                 createCliqueTreeFile(cg);
                 if (results.getMaxVariables() >= maxCliqueVariables) {
                     System.err.println("Error: minimum clique variables found is greater than the specified max.");
                     System.exit(1);
                 }
                 break;
 
             case MAX_SUM:
                 cg = MaxSum.buildGraph(factors);
                 createCliqueGraphFile(cg);
                 break;
 
         }
         
         return cg;
     }
 
     void setAlgorithm(Algorithm algo) {
         algorithm = algo;
     }
 
     void setInputFile(File file) throws FileNotFoundException {
         input = new FileInputStream(file);
     }
 
     void setOutputFile(File file) throws FileNotFoundException {
         System.setOut(new PrintStream(file));
     }
 
     void setFactorGraphFile(String fileName) {
         cliqueGraphFile = fileName;
     }
 
     void setCliqueGraphFile(String fileName) {
         cliqueGraphFile = fileName;
     }
 
     void setTreeFile(File fileName) throws FileNotFoundException {
         treeFile = new FileInputStream(fileName);
     }
 
     /**
      * @return the traceFile
      */
     public String getTraceFile() {
         return traceFile;
     }
 
     /**
      * @param traceFile the traceFile to set
      */
     public void setTraceFile(String traceFile) {
         
         this.traceFile = traceFile;
     }
 
     /**
      * @return the createTraceFile
      */
     public boolean isCreateTraceFile() {
         return createTraceFile;
     }
 
     /**
      * @param createTraceFile the createTraceFile to set
      */
     public void setCreateTraceFile(boolean createTraceFile) {
         this.createTraceFile = createTraceFile;
     }
 
     /**
      * @return the IGdlR
      */
     public int getIGdlR() {
         return IGdlR;
     }
 
     /**
      * @param IGdlR the IGdlR to set
      */
     public void setIGdlR(int IGdlR) {
         this.IGdlR = IGdlR;
     }
 
     private void setupLogHandling() {
         LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
         try {
             JoranConfigurator configurator = new JoranConfigurator();
             configurator.setContext(lc);
             lc.reset();
             if (outputFormat == OutputFormat.UAI) {
                 lc.putProperty("log-root-level", "OFF");
             }
             URL cURL;
             if (this.createTraceFile) {
                 lc.putProperty("file.name", traceFile);
                 cURL = getClass().getClassLoader().getResource("logback-trace.xml");
             } else {
                 cURL = getClass().getClassLoader().getResource("logback.xml");
             }
             configurator.doConfigure(cURL);
         } catch (JoranException je) {
             je.printStackTrace();
         }
     }
 
     void setPartitionStrategy(ApproximationStrategies partitionStrategy) {
         this.approximationStrategy = partitionStrategy;
     }
 
     void setCompressionMethod(CompressionMethod method) {
         Compressor.METHOD = method;
     }
 
     public OutputFormat getOutputFormat() {
         return outputFormat;
     }
 
     public void setOutputFormat(OutputFormat outputFormat) {
         this.outputFormat = outputFormat;
     }
 
     void setSolutionExpansion(SolutionExpansionStrategies solutionExpansionStrategy) {
         this.expansionStrategy = solutionExpansionStrategy;
     }
     void setSolutionSolving(SolutionSolvingStrategies solutionSolvingStrategy) {
         this.solvingStrategy = solutionSolvingStrategy;
     }
 
     public String getCliqueTreeFile() {
         return cliqueTreeFile;
     }
 
     public void setCliqueTreeFile(String cliqueTreeFile) {
         this.cliqueTreeFile = cliqueTreeFile;
     }
 
     public boolean isCreateCliqueTree() {
         return createCliqueTree;
     }
 
     public void setCreateCliqueTree(boolean createCliqueTree) {
         this.createCliqueTree = createCliqueTree;
     }
 
     /**
      * Get the maximum number of junction tree's built trying to minimize the
      * maximal clique size.
      *
      * @return maximum number of junction tree building tries.
      */
     public int getMaxJunctionTreeTries() {
         return maxJunctionTreeTries;
     }
 
     /**
      * Set the maximum number of junction tree's built trying to minimize the
      * maximal clique size.
      *
      * @param maxJunctionTreeTries number of junction tree building tries.
      */
     public void setMaxJunctionTreeTries(int maxJunctionTreeTries) {
         this.maxJunctionTreeTries = maxJunctionTreeTries;
     }
 
     /**
      * Get the variance of the random gaussian noise adder.
      *
      * @return random noise variance.
      */
     public double getRandomVariance() {
         return randomVariance;
     }
 
     /**
      * Set the variance of the random gaussian noise adder.
      *
      * @param randomVariance random noise variance.
      */
     public void setRandomVariance(double randomVariance) {
         this.randomVariance = randomVariance;
     }
 
     /**
      * Get the hard maximum of variables in one clique.
      *
      * If no junction tree with less than this maximum is found in the
      *
      * @return
      */
     public int getMaxCliqueVariables() {
         return maxCliqueVariables;
     }
 
     /**
      *
      * @param maxCliqueVariables
      */
     public void setMaxCliqueVariables(int maxCliqueVariables) {
         this.maxCliqueVariables = maxCliqueVariables;
     }
 
     /**
      *
      * @return
      */
     public int getCommunicationCost() {
         return communicationCost;
     }
 
     /**
      *
      * @param communicationCost
      */
     public void setCommunicationCost(int communicationCost) {
         this.communicationCost = communicationCost;
     }
 
     /**
      *
      * @return
      */
     public CostFunction.Normalize getNormalization() {
         return normalization;
     }
 
     /**
      *
      * @param normalization
      */
     public void setNormalization(CostFunction.Normalize normalization) {
         this.normalization = normalization;
     }
     private int communicationCost = 0;
 
     /**
      *
      * @return
      */
     public CostFunction.Combine getCombineOperation() {
         return combineOperation;
     }
 
     /**
      *
      * @param combineOperation
      */
     public void setCombineOperation(CostFunction.Combine combineOperation) {
         this.combineOperation = combineOperation;
     }
 
     /**
      *
      * @return
      */
     public CostFunction.Summarize getSummarizeOperation() {
         return summarizeOperation;
     }
 
     /**
      *
      * @param summarizeOperation
      */
     public void setSummarizeOperation(CostFunction.Summarize summarizeOperation) {
         this.summarizeOperation = summarizeOperation;
     }
     
     public void setDelta(int delta) {
         this.delta = delta;
     }
     
 }
