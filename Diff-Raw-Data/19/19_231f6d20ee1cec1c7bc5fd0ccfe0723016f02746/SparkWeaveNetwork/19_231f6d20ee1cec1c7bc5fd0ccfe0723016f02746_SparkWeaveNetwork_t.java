 package at.sti2.spark.network;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import at.sti2.spark.core.stream.StreamedTriple;
 import at.sti2.spark.epsilon.network.ClassNode;
 import at.sti2.spark.epsilon.network.PropertyNode;
 import at.sti2.spark.epsilon.network.build.NetworkBuilder;
 import at.sti2.spark.epsilon.network.run.EpsilonNetwork;
 import at.sti2.spark.language.query.SparkPatternParser;
 import at.sti2.spark.network.gc.SparkWeaveGarbageCollector;
 import at.sti2.spark.rete.RETENetwork;
 import at.sti2.spark.rete.alpha.AlphaNode;
 import at.sti2.spark.rete.alpha.ValueTestAlphaNode;
 import at.sti2.spark.rete.alpha.WorkingMemory;
 import at.sti2.spark.rete.condition.TriplePatternGraph;
 
 public class SparkWeaveNetwork{
 	
 	static Logger logger = Logger.getLogger(SparkWeaveNetwork.class);
 	
 	private RETENetwork reteNetwork = null;
 	private EpsilonNetwork epsilonNetwork = null;
 	
 	//Input artifacts
 	private TriplePatternGraph triplePatternGraph = null;
 	private File epsilonOntology = null;
 	
 	//Values needed for garbage collection
 	private long lastTimestamp = 0l;
 	private long gcSessionDelay = 0l;
 	
 	public SparkWeaveNetwork(TriplePatternGraph triplePatternGraph, File epsilonOntology, long gcSessionDelay) {
 		
 		this.triplePatternGraph = triplePatternGraph;
 		this.epsilonOntology = epsilonOntology;
 		this.gcSessionDelay = gcSessionDelay;	
 	}
 	
 	public SparkWeaveNetwork(String patternFileName, String epsilonOntologyFileName, String gcSessionDelay) {
 		
 		//Build triple pattern representation
 		SparkPatternParser patternParser = new SparkPatternParser(patternFileName);
 		TriplePatternGraph triplePatternGraph = patternParser.parse();
 		
 		File ontologyFile = new File(epsilonOntologyFileName);
 		
 		SparkWeaveNetwork sparkWeaveNetwork = new SparkWeaveNetwork(triplePatternGraph, ontologyFile, Long.parseLong(gcSessionDelay));
 		sparkWeaveNetwork.buildNetwork();
 		
 		//Print Rete network structure
 		sparkWeaveNetwork.getReteNetwork().printNetworkStructure();
 		
 		//Start SparkWeaveNetworkServerInstance
 		(new SparkWeaveNetworkServer(sparkWeaveNetwork)).start();
 	}
 
 	public void buildNetwork(){
 		
 		logger.info("Building RETE network...");
 		
 		//Build RETE network
 		reteNetwork = new RETENetwork();
 		reteNetwork.addTriplePatternGraph(triplePatternGraph);
 		
 		logger.info("Building epsilon network...");
 		
 		//Build epsilon network
 		NetworkBuilder builder = new NetworkBuilder(epsilonOntology);
 		epsilonNetwork = builder.buildNetwork();
 		
 		logger.info("Binding epsilon and RETE network binding...");
 		
 		bindNetworks();
 		
 		logger.info("SparkWeave network completed...");
 		
 		SparkWeaveGarbageCollector sparkWeaveGC = new SparkWeaveGarbageCollector(this, gcSessionDelay);
 		sparkWeaveGC.start();
 		
 		logger.info("SparkWeave garbage collector started...");
 	}
 
 	public RETENetwork getReteNetwork() {
 		return reteNetwork;
 	}
 
 	public EpsilonNetwork getEpsilonNetwork() {
 		return epsilonNetwork;
 	}
 	
 	private void bindNetworks(){
 		
 		WorkingMemory workingMemory = reteNetwork.getWorkingMemory();
 		
 		/**
 		 * Connecting epsilon property nodes to alpha predicate nodes 
 		 */
 		
 		List<AlphaNode> predicateAlphaNodes = workingMemory.getPropertyAlphaNodesWithoutRDFType();
 		logger.debug("Found predicate alpha nodes:" + predicateAlphaNodes);
 		
 		for (AlphaNode predicateAlphaNode : predicateAlphaNodes){
 			logger.debug("Connecting predicate alpha node " + predicateAlphaNode + " to corresponding epsilon node.");
 			PropertyNode propertyNode = epsilonNetwork.getNetwork().getPropertyNodeByURI(((ValueTestAlphaNode)predicateAlphaNode).getTestValue());
 			//If the node exists in the epsilon network add binding
 			if (propertyNode != null)
 				propertyNode.addAlphaNode(predicateAlphaNode);
 		}
 		
 		/**
 		 * Connecting all epsilon property nodes to all first children alpha nodes which are not predicate nodes 
 		 */
 		List<AlphaNode> rootChildAlphaNodes = workingMemory.getRootChildrenAlphaNodesWithoutPropertyAN();
 		logger.debug("Found property alpha nodes:" + rootChildAlphaNodes);
 		if (!rootChildAlphaNodes.isEmpty())
 			for (PropertyNode propertyNode : epsilonNetwork.getNetwork().getPropertyNodes())
 				for (AlphaNode alphaNode : rootChildAlphaNodes)
 					propertyNode.addAlphaNode(alphaNode);
 						
 		/**
 		 * Connecting epsilon class nodes to alpha object nodes
 		 */
 		List<AlphaNode> objectAlphaNodes = workingMemory.getObjectAlphaNodesOfRDFTypePredicateAlphaNode();
 		logger.debug("Found object alpha nodes:" + objectAlphaNodes);
 		for (AlphaNode objectAlphaNode : objectAlphaNodes){
 			logger.debug("Connecting object alpha node " + objectAlphaNode + " to corresponding epsilon class node.");
 			ClassNode classNode = epsilonNetwork.getNetwork().getClassNodeByURI(((ValueTestAlphaNode)objectAlphaNode).getTestValue());
 			//If the node exists in the epsilon network add binding
 			if (classNode != null)
 				classNode.addAlphaNode(objectAlphaNode);
 		}
 		
 		/**
 		 * Connecting all epsilon class nodes to all first children of alpha rdf:type nodes which are not object nodes 
 		 */
 		List<AlphaNode> rdfTypeChildAlphaNodes = workingMemory.getRDFTypeChildrenAlphaNodesWithoutObjectAN();
 		logger.debug("Found subject alpha nodes:" + rdfTypeChildAlphaNodes);
 		if (!rdfTypeChildAlphaNodes.isEmpty())
 			for (ClassNode classNode : epsilonNetwork.getNetwork().getClassNodes())
 				for (AlphaNode alphaNode : rdfTypeChildAlphaNodes)
 					classNode.addAlphaNode(alphaNode);
 						
 		/**
 		 * Connecting all epsilon class nodes to all first children alpha nodes which are not predicate nodes 
 		 */
 		rootChildAlphaNodes = workingMemory.getRootChildrenAlphaNodesWithoutPropertyAN();
 		logger.debug("Found alpha nodes:" + rootChildAlphaNodes);
 		if (!rootChildAlphaNodes.isEmpty())
 			for (ClassNode classNode : epsilonNetwork.getNetwork().getClassNodes())
 				for (AlphaNode alphaNode : rootChildAlphaNodes)
 					classNode.addAlphaNode(alphaNode);
 						
 		/**
 		 * Connect the epsilon entry to RETE entry for triples that are not entering RETE through epsilon
 		 */
 		epsilonNetwork.setRootAlphaNode(reteNetwork.getWorkingMemory().getRootNode());
 	}
 
 	public long getLastTimestamp() {
 		return lastTimestamp;
 	}
 
 	public void setLastTimestamp(long lastTimestamp) {
 		this.lastTimestamp = lastTimestamp;
 	}
 	
 	public long getTimeWindowLength(){
 		return triplePatternGraph.getTimeWindowLength();
 	}
 	
 	public void activateNetwork(StreamedTriple streamedTriple){
 		lastTimestamp = streamedTriple.getTimestamp();
 		epsilonNetwork.activate(streamedTriple);
 	}
 
 	public long getGcSessionDelay() {
 		return gcSessionDelay;
 	}
 	
 	public static void main(String args[]){
 		
 		if (args.length != 3){
			System.out.println("SparkWeaveNetwork builds an instance of Sparkweave. It expects following 3 parameters:");
			System.out.println(" <pattern_file> - name of the file holding triple pattern definition.");
			System.out.println(" <epsilon_ontology_file> - name of the file holding ontology.");
			System.out.println(" <gc_session_delay> - the time interval between garbage collection sessions in [ms].");
 			System.exit(0);
 		}
 		
 		new SparkWeaveNetwork(args[0], args[1], args[2]);
 	}
 }
