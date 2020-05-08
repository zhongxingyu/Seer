 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kagglefacebook;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.neo4j.graphalgo.GraphAlgoFactory;
 import org.neo4j.graphalgo.PathFinder;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.graphdb.index.IndexHits;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.kernel.Traversal;
 import org.neo4j.kernel.Uniqueness;
 import org.neo4j.kernel.impl.util.FileUtils;
 import org.neo4j.unsafe.batchinsert.BatchInserter;
 import org.neo4j.unsafe.batchinsert.BatchInserters;
 
 
 /**
  *
  * @author rafael
  */
 public class Graph {
     
     private String DB_PATH;
     String greeting;
     private GraphDatabaseService graphDb;
     private Index<Node> nameIndex;
     private String NAME_KEY = "name";
     private String DIR;
     private boolean newDB;
     
     private final int MAX_THREADS = 7;
     private final long TIME_LIMIT = 10000;
     
     private final TraversalDescription PREDICTION_TRAVERSAL = 
             Traversal.description()
             .breadthFirst()
             .relationships(facebookRelationshipTypes.relation)
             .uniqueness(Uniqueness.NODE_GLOBAL);
     private String NL = System.getProperty("line.separator");
     private Random random = new Random();
     private Map<String, String> config = new HashMap<String, String>();
     
       
    
     
     
     public Graph(String dir, String db_path, boolean newdb){
         DIR = dir;
         DB_PATH = db_path;
         newDB = newdb;
         if(newDB){
             clearDb();
         }
        config.put("use_memory_mapped_buffers","false");
        config.put("neostore.propertystore.db.index.keys.mapped_memory","5M");
        config.put("neostore.propertystore.db.strings.mapped_memory","100M");
        config.put("neostore.propertystore.db.arrays.mapped_memory","107M");
        config.put("neostore.relationshipstore.db.mapped_memory","2000M");
        config.put("neostore.propertystore.db.index.mapped_memory","5M");
        config.put("neostore.propertystore.db.mapped_memory","1000M");
        config.put("dump_configuration","true");
        config.put("cache_type","none");
        config.put("neostore.nodestore.db.mapped_memory","500M");
         startDB();
     }
     
     private void startDB(){
         // START SNIPPET: startDb
         //graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
         graphDb = new EmbeddedGraphDatabase( DB_PATH , config);
         nameIndex = graphDb.index().forNodes(NAME_KEY);
         registerShutdownHook( graphDb );
         // END SNIPPET: startDb
     }
     
     public void loadFromCSV(String file){
         String[] lines = Helper.readFile(DIR+file).split(NL);
         System.out.println("Loading "+DIR+file);
         int batchSize = (lines.length-1)/MAX_THREADS;
         List<Thread> threads = new ArrayList<Thread>();
         
         for(int i=1; i<lines.length;){
             List<String> batch = new ArrayList<String>();
             for(int j=0; j<batchSize && i<lines.length; j++){
                 batch.add(lines[i]);
                 i++;
             }            
             threads.add(new Thread(new LoadBatch(batch)));            
             threads.get(threads.size()-1).start();
         }
         
         for (Thread thread: threads){
             try {
                 thread.join();
             } catch (InterruptedException ex) {
                 Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         System.out.println("Finished loading "+DIR+file);
     }
     
    
    public void batchInsertFromCSV(String file){
        graphDb.shutdown();
        
        String[] lines = Helper.readFile(DIR+file).split(NL);
        System.out.println("batchInserterFromCSV "+DIR+file);
        
        BatchInserter inserter = BatchInserters.inserter(DB_PATH, config);       
        
        for (int i=1; i< lines.length; i++){
            Map<String,Object> properties = new HashMap<String, Object>();
            String[] names = lines[i].split(",");
            long node1 = Long.valueOf(names[0]);
            long node2 = Long.valueOf(names[1]);           
            if(!inserter.nodeExists(node1)){
                properties.put(NAME_KEY, node1);
                inserter.createNode(node1,properties);
            }                          
            if(!inserter.nodeExists(node2)){
                properties.put(NAME_KEY, node2);
                inserter.createNode(node2,properties);
            } 
            inserter.createRelationship(node1, node2, facebookRelationshipTypes.relation, null);       
        }
        inserter.shutdown();      
        startDB();
    }
     
    private enum facebookRelationshipTypes implements RelationshipType{
        relation
    }
    
      
     
    private void clearDb(){
         try
         {
             FileUtils.deleteRecursively( new File( DB_PATH ) );
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
     
    
    void shutDown(){
         System.out.println();
         System.out.println( "Shutting down database ..." );
         // START SNIPPET: shutdownServer
         graphDb.shutdown();
         // END SNIPPET: shutdownServer
     }
     
    // START SNIPPET: shutdownHook
    private static void registerShutdownHook( final GraphDatabaseService graphDb ){
         // Registers a shutdown hook for the Neo4j instance so that it
         // shuts down nicely when the VM exits (even if you "Ctrl-C" the
         // running example before it's completed)
         Runtime.getRuntime().addShutdownHook( new Thread()
         {
             @Override
             public void run()
             {
                 graphDb.shutdown();
             }
         } );
     }
     // END SNIPPET: shutdownHook
    
    public void makePredictions(String file){     
        String[] lines = Helper.readFile(DIR+file).split(NL);
        System.out.println("makePredictions "+DIR+file);
        int batchSize = (lines.length-1)/MAX_THREADS;
        List<Thread> threads = new ArrayList<Thread>(); 
        List<String> batchFiles = new ArrayList<String>(); 
        int batchCount=0;
        for(int i=1; i<lines.length;){
            List<Long> batch = new ArrayList<Long>();
            for(int j=0; j<batchSize && i<lines.length; j++){
                batch.add(Long.valueOf(lines[i]));
                i++;           
            }      
            String batchFile = DIR+"batch_"+batchCount;
            batchFiles.add(batchFile);
            batchCount++;
            threads.add(new Thread(new PredictBatch(batch, batchFile)));            
            threads.get(threads.size()-1).start();           
        }
         
        for (Thread thread: threads){
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        String resultFile = DIR+"result.csv";
        Helper.writeToFile(resultFile, "source_node,destination_nodes"+NL, false);
        for (String batchFile:batchFiles){
            lines = Helper.readFile(batchFile).split(NL);
            for (String line: lines){
                Helper.writeToFile(resultFile, line+NL, false);
            }
        }
        
        System.out.println("Finished predicting "+resultFile);             
    }
        
        
    /*       
    private Map<Long,Double> predictRelatedNodes(Node origin){
        Map<Long,Double> predictedNodes = new HashMap<Long,Double>();
        ValueComparator bvc = new ValueComparator(predictedNodes);
        TreeMap<Long,Double> sortedMap = new TreeMap(bvc);
        int iterationsWithoutImprovement = 0;
        Double prevAvgWeight = 0.0;
        int depth = 0;
        int maxDepth = 2;       
        long elapsedTime = 0;
        long startTime = System.currentTimeMillis();
        for(Path position: PREDICTION_TRAVERSAL.traverse(origin)){           
            depth = position.length();
            if(depth<2)
                continue;
            if(elapsedTime > TIME_LIMIT){
                 if(predictedNodes.size()>=10)
                     break;
            }
            if (iterationsWithoutImprovement>50 || depth > maxDepth)
                if(predictedNodes.size()<10){
                    maxDepth++;
                    iterationsWithoutImprovement = 0;
                } else {
                    break;
                }
 
            Double weight = getRelationshipWeight(origin,position.endNode(), depth+1);
            if (weight>0)
                predictedNodes.put(position.endNode().getId(), weight);
            if (predictedNodes.size()>10){
                sortedMap.clear();
                sortedMap.putAll(predictedNodes);
                predictedNodes.clear();
                int i=0;
                Double newAvgWeight = 0.0;
                for(Map.Entry<Long, Double> entry:  sortedMap.entrySet()){
                    predictedNodes.put(entry.getKey(), entry.getValue());
                    newAvgWeight+=entry.getValue();
                    i++;
                    if(i>=10)
                        break;
                }                    
                newAvgWeight /=10.0;
                if(newAvgWeight <= prevAvgWeight){
                    iterationsWithoutImprovement++;
                } else {
                    iterationsWithoutImprovement = 0;
                }
                prevAvgWeight = newAvgWeight;               
            }           
            elapsedTime = System.currentTimeMillis()-startTime;
            System.out.println(depth+":"+iterationsWithoutImprovement+":"+elapsedTime+":"+sortedMap.toString());
        }
        return predictedNodes;
    }
    
    
    private Double getRelationshipWeight(Node from, Node to, int maxDepth){
        PathFinder<Path> finder = GraphAlgoFactory.allSimplePaths(Traversal.expanderForAllTypes(),maxDepth);       
        Iterable<Path> paths = finder.findAllPaths(from, to);                   
        Double weight = 0.0;
        Double pathCount = 0.0;
        Double avgPathLength = 0.0;
        for(Path path:paths){
            Double pathLength = Double.valueOf(path.length());
            if(pathLength>1){
                weight += 1.0/pathLength;
                avgPathLength += pathLength;
                pathCount++;
            } 
            else
                return 0.0;
        }
        avgPathLength /= pathCount;
        //weight /= pathCount;
        Double rand = random.nextDouble();
        if (rand<0.5){
            rand *= -1.0;
        }
        rand *= 0.000001;
        //System.out.println(weight+":"+avgPathLength+":"+pathCount);
        return weight+rand;
    }
    
    */
  
    
     public class LoadBatch implements Runnable{
         
         private List<String> lines;
         
         public LoadBatch (List<String> lines){
             this.lines = lines;
         }
 
         @Override
         public void run() {
             String sourceName = "";
             Node sourceNode = null;        
             for (String line : lines){           
                 String [] names = line.split(",");
                 if(!names[0].equals(sourceName)){
                     sourceName = names[0];
                     sourceNode = getOrCreateAndIndexNode(sourceName);
                 }
                 createRelationship(sourceName, names[1], sourceNode);
             }
         }
         
          public void createRelationship(String fromName, String toName, Node fromNode) {
             if (fromNode == null)
                 fromNode = getOrCreateAndIndexNode(fromName);
             Node toNode = getOrCreateAndIndexNode(toName);
             Transaction tx = graphDb.beginTx();
             try{                        
                 fromNode.createRelationshipTo(toNode, facebookRelationshipTypes.relation);
                 tx.success();
                 //System.out.println(fromNode.getProperty(NAME_KEY) +":"+toNode.getProperty(NAME_KEY));
             } catch (Exception ex){
                  //System.out.println("Problem with graph relationship:"+ex);
                 ex.getMessage();
             }
             finally {
                 tx.finish();
             }
         }
         
         private Node getOrCreateAndIndexNode(String name){
             Node node = null;
             try{
                 IndexHits<Node> hits = nameIndex.get(NAME_KEY, name);
                 if(hits.hasNext()){
                     node = hits.next();
                 }
                 hits.close();
             } catch (Exception e){
                 System.out.println(e+":"+name);
             }
             if (node == null){
                 Transaction tx = graphDb.beginTx();
                 try{
                     node = graphDb.createNode();
                     node.setProperty(NAME_KEY, name);        
                     nameIndex.add(node, NAME_KEY, name);
                     tx.success();
                 }
                 finally {
                     tx.finish();
                 }
             }
             return node;        
         }  
         
     }
      
     public class PredictBatch implements Runnable{       
         private List<Long> nodes;
         private String outFile;
         
         public PredictBatch(List<Long> nodes, String outfile){
             this.nodes = nodes;            
             this.outFile = outfile;
         }
 
         @Override
         public void run() {
             for(Long nodeId: nodes){
                 Node node = graphDb.getNodeById(nodeId);
                 Map<Long,Double> bestNodes = predictRelatedNodes(node);
                 String nodesString = "";
                 for(Long n:bestNodes.keySet()){
                     nodesString = nodesString + n + " ";
                 }
                 if(nodesString.length()>0)
                     nodesString = nodesString.substring(0, nodesString.length()-1);
                 Helper.writeToFile(outFile, nodeId+","+nodesString+NL, false);
                //System.out.println(nodeId+","+nodesString);
             }
         }
         
         private Map<Long,Double> predictRelatedNodes(Node origin){
             Map<Long,Double> predictedNodes = new HashMap<Long,Double>();
             ValueComparator bvc = new ValueComparator(predictedNodes);
             TreeMap<Long,Double> sortedMap = new TreeMap(bvc);
             int iterationsWithoutImprovement = 0;
             Double prevAvgWeight = 0.0;
             int depth = 0;
             int maxDepth = 2;       
             long elapsedTime = 0;
             long startTime = System.currentTimeMillis();
             for(Path position: PREDICTION_TRAVERSAL.traverse(origin)){           
                 depth = position.length();
                 if(depth<2)
                     continue;
                 if(elapsedTime > TIME_LIMIT){
                     if(predictedNodes.size()>=10)
                         break;
                 }
                 if (iterationsWithoutImprovement>50 || depth > maxDepth)
                     if(predictedNodes.size()<10){
                         maxDepth++;
                         iterationsWithoutImprovement = 0;
                     } else {
                         break;
                     }
 
                 Double weight = getRelationshipWeight(origin,position.endNode(), depth+1);
                 if (weight>0)
                     predictedNodes.put(position.endNode().getId(), weight);
                 if (predictedNodes.size()>10){
                     sortedMap.clear();
                     sortedMap.putAll(predictedNodes);
                     predictedNodes.clear();
                     int i=0;
                     Double newAvgWeight = 0.0;
                     for(Map.Entry<Long, Double> entry:  sortedMap.entrySet()){
                         predictedNodes.put(entry.getKey(), entry.getValue());
                         newAvgWeight+=entry.getValue();
                         i++;
                         if(i>=10)
                             break;
                     }                    
                     newAvgWeight /=10.0;
                     if(newAvgWeight <= prevAvgWeight){
                         iterationsWithoutImprovement++;
                     } else {
                         iterationsWithoutImprovement = 0;
                     }
                     prevAvgWeight = newAvgWeight;               
                 }           
                 elapsedTime = System.currentTimeMillis()-startTime;
                //System.out.println(depth+":"+iterationsWithoutImprovement+":"+elapsedTime+":"+sortedMap.toString());
             }
            //System.out.println(depth+":"+iterationsWithoutImprovement+":"+elapsedTime+":"+sortedMap.toString());
             return predictedNodes;
         }
    
    
         private Double getRelationshipWeight(Node from, Node to, int maxDepth){
             PathFinder<Path> finder = GraphAlgoFactory.allSimplePaths(Traversal.expanderForAllTypes(),maxDepth);       
             Iterable<Path> paths = finder.findAllPaths(from, to);                   
             Double weight = 0.0;
             Double pathCount = 0.0;
             Double avgPathLength = 0.0;
             for(Path path:paths){
                 Double pathLength = Double.valueOf(path.length());
                 if(pathLength>1){
                     weight += 1.0/pathLength;
                     avgPathLength += pathLength;
                     pathCount++;
                 } 
                 else
                     return 0.0;
             }
             avgPathLength /= pathCount;
             //weight /= pathCount;
             Double rand = random.nextDouble();
             if (rand<0.5){
                 rand *= -1.0;
             }
             rand *= 0.000001;
             //System.out.println(weight+":"+avgPathLength+":"+pathCount);
             return weight+rand;
         }
         
     }
 }
