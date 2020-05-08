 package edu.stanford.cs224u.disentanglement.disentanglers;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.primitives.Doubles;
 import edu.stanford.cs224u.disentanglement.classifier.DataBuilder;
 import edu.stanford.cs224u.disentanglement.features.*;
 import edu.stanford.cs224u.disentanglement.structures.*;
 import edu.stanford.cs224u.disentanglement.util.Benchmarker;
 import edu.stanford.cs224u.disentanglement.util.LDAModel;
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.Graph;
 import org.jgrapht.graph.DefaultDirectedGraph;
 import org.jgrapht.alg.KruskalMinimumSpanningTree;
 import org.jgrapht.graph.DefaultDirectedWeightedGraph;
 import org.jgrapht.graph.DefaultEdge;
 import org.jgrapht.graph.DefaultWeightedEdge;
 import weka.classifiers.functions.SMO;
 import weka.core.Instance;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 enum MessagePairCategories {
     NOT_RELATED,
     RELATED,
 }
 
 public class SVMDisentangler implements Disentangler {
     private static final double FALL_BACK_TO_ROOT_PROBABILITY = 0.5;
 
     private SMO classifier;
     private DataBuilder dataBuilder;
     private final int numFalseExamples = 1;
     private Random random;
 
     public SVMDisentangler() {
         random = new Random(1); // fixed seed for now
     }
 
     @Override
     public void train(Iterable<MessageTree> trainingData) {
         Preconditions.checkArgument(new File("test_model").exists(), "test_model LDA file must exist!");
         Benchmarker.push("Create data builder");
 
         dataBuilder = new DataBuilder(MessagePairCategories.class, "SVMDisentangler",
             //new MinuteDifferenceFeatureFactory()
             new TFIDFFeatureFactory(),
             new AuthorMentionFeatureFactory(),
             new ReplyToSelfFeatureFactory(),
             new JacardNERFactory(),
             new LDAFeatureFactory(LDAModel.loadModel(new File("test_model")))
         );
         Benchmarker.pop();
 
         Benchmarker.push("Adding examples");
         for(MessageTree tree : trainingData) {
             List<Message> linearized = tree.linearize();
             DataBuilder.TreeData td = dataBuilder.createTreeData(linearized);
             for(MessagePair p : tree.extractEdges()) {
                 td.addExample(p, MessagePairCategories.RELATED);
                 int foundExamples = 0;
                 int iterations = 0;
                 while(iterations < 100 && foundExamples < numFalseExamples) {
                     iterations++;
                     Message example = linearized.get(random.nextInt(linearized.size()));
                     if(example.equals(p.getSecond()) || example.equals(p.getFirst())) {
                         continue;
                     }
                     MessagePair pReplace = new MessagePair(example, p.getSecond());
                     td.addExample(pReplace, MessagePairCategories.NOT_RELATED);
                     foundExamples++;
                 }
 
             }
         }
 
         classifier = new SMO();
         classifier.setBuildLogisticModels(true);
         classifier.setC(0.5);
         try {
             Benchmarker.push("Build classifier");
             classifier.buildClassifier(dataBuilder.getInstances());
             System.out.println(classifier);
         } catch (Exception e) {
             Benchmarker.popError();
             throw new RuntimeException(e);
         }
         Benchmarker.pop();
         System.out.println("Learned Classifier: " + classifier);
     }
 
     @Override
     public MessageTree predict(List<Message> test) {
         Preconditions.checkArgument(test.size() > 0, "Tried to predict empty tree!");
         return predictGreedy(test);
     }
 
     public MessageTree predictMST(List<Message> test) {
         DataBuilder.TreeData td = dataBuilder.createTreeData(test);
         DefaultDirectedWeightedGraph<Message, DefaultWeightedEdge> predictionGraph
                 = new DefaultDirectedWeightedGraph<Message, DefaultWeightedEdge>(DefaultWeightedEdge.class);
 
         Map<Message, MessageNode> nodeForMessage = Maps.newHashMap();
         MessageNode root = new MessageNode(test.get(0));
         nodeForMessage.put(test.get(0), root);
         predictionGraph.addVertex(test.get(0));
         for(int i = 1; i < test.size(); i++){
             predictionGraph.addVertex(test.get(i));
             MessageNode mn = new MessageNode(test.get(i));
             nodeForMessage.put(test.get(i), mn);
         }
 
         for(int i = 1; i < test.size(); i++) {
             Message m = test.get(i);
             boolean hasAttached = false;
             for(int p = 0; p < i; p++) {
                 Message parentCandidate = test.get(p);
                 MessagePair candidatePair = MessagePair.of(parentCandidate, m);
                 Instance instance = td.buildClassificationInstance(candidatePair);
                 instance.setDataset(dataBuilder.getInstances());
 
                 double []classProbs;
                 try {
                     classProbs = classifier.distributionForInstance(instance);
                 } catch(Exception e) {
                     throw new RuntimeException(e);
                 }
 
 
                 double prob = classProbs[1];
                 if(classProbs[1] > classProbs[0]) {
                     predictionGraph.addEdge(parentCandidate, m);
                     predictionGraph.setEdgeWeight(predictionGraph.getEdge(parentCandidate,m), -prob);
                     hasAttached = true;
                 }
             }
 
             if(!hasAttached) {
                 predictionGraph.addEdge(root.getMessage(), m);
                 predictionGraph.setEdgeWeight(predictionGraph.getEdge(root.getMessage(),m), -2.0);
             }
         }
 
         Benchmarker.push("Create maximum spanning tree");
        KruskalMinimumSpanningTree<Message, DefaultWeightedEdge> mst
                = new KruskalMinimumSpanningTree<Message, DefaultWeightedEdge>(predictionGraph);
         for(DefaultWeightedEdge edge : mst.getEdgeSet()) {
             MessageNode parent = nodeForMessage.get(predictionGraph.getEdgeSource(edge));
             MessageNode child = nodeForMessage.get(predictionGraph.getEdgeTarget(edge));
             parent.addChildren(child);
         }
         System.err.println("MST cost: " + mst.getSpanningTreeCost());
         Benchmarker.pop();
 
         return new MessageTree(root, "Predicted Tree");
     }
 
 
     public MessageTree predictGreedy(List<Message> test) {
         DataBuilder.TreeData td = dataBuilder.createTreeData(test);
 
         Map<Message, MessageNode> nodeForMessage = Maps.newHashMap();
         MessageNode root = new MessageNode(test.get(0));
         nodeForMessage.put(test.get(0), root);
 
         for(int i = 1; i < test.size(); i++) {
             Message m = test.get(i);
             // When in doubt, attach to root
             MessageNode maxParent = root;
             double maxProb = Double.NEGATIVE_INFINITY;
             for(int p = 0; p < i; p++) {
                 MessageNode parentCandidate = nodeForMessage.get(test.get(p));
                 MessagePair candidatePair = MessagePair.of(parentCandidate.getMessage(), m);
                 Instance instance = td.buildClassificationInstance(candidatePair);
                 instance.setDataset(dataBuilder.getInstances());
 
                 double []classProbs;
                 try {
                      classProbs = classifier.distributionForInstance(instance);
                 } catch(Exception e) {
                     throw new RuntimeException(e);
                 }
 
 
                 double prob = classProbs[1];
                 if(classProbs[1] > FALL_BACK_TO_ROOT_PROBABILITY && prob > maxProb) {
                     maxProb = prob;
                     maxParent = parentCandidate;
                 }
             }
             MessageNode mn = new MessageNode(m);
             maxParent.addChildren(mn);
             nodeForMessage.put(m, mn);
         }
 
         return new MessageTree(root, "Predicted Tree");
     }
 
 }
