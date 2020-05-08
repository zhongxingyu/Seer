 
 
 package org.ben.socialgraph;
 
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.concurrent.TimeUnit;
 
 import org.gephi.data.attributes.api.AttributeColumn;
 import org.gephi.data.attributes.api.AttributeController;
 import org.gephi.data.attributes.api.AttributeModel;
 import org.gephi.graph.api.DirectedGraph;
 import org.gephi.graph.api.Edge;
 import org.gephi.graph.api.GraphController;
 import org.gephi.graph.api.GraphModel;
 import org.gephi.graph.api.Node;
 import org.gephi.io.importer.api.Container;
 import org.gephi.io.importer.api.EdgeDefault;
 import org.gephi.io.importer.api.ImportController;
 import org.gephi.io.processor.plugin.DefaultProcessor;
 import org.gephi.layout.plugin.AutoLayout;
 import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
 import org.gephi.partition.api.Partition;
 import org.gephi.partition.api.PartitionController;
 import org.gephi.partition.plugin.NodeColorTransformer;
 import org.gephi.preview.api.PreviewController;
 import org.gephi.preview.api.PreviewModel;
 import org.gephi.preview.api.PreviewProperty;
 import org.gephi.preview.api.ProcessingTarget;
 import org.gephi.preview.api.RenderTarget;
 import org.gephi.preview.types.DependantOriginalColor;
 import org.gephi.project.api.ProjectController;
 import org.gephi.project.api.Workspace;
 import org.gephi.ranking.api.Ranking;
 import org.gephi.ranking.api.RankingController;
 import org.gephi.ranking.api.Transformer;
 import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
 import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
 import org.gephi.statistics.plugin.GraphDistance;
 import org.gephi.statistics.plugin.Modularity;
 import org.openide.util.Lookup;
 
 import processing.core.PApplet;
 
 
 /**
  * RenderGraph
  * @author Ben Sunderland
  *
  */
 public class RenderGraph  {
 
 	public TibbrUser[] users = new TibbrUser[200];
 	public Node[] arrNodes = new Node[200];
 	public Edge[] arrEdges = new Edge[1000];
 	public String filename = "graph.csv";
 	public String userCentric = "";
 	public String tibbr_url = "https://tibbrdemo.tibbr.com"; // default in case not set in UI.
 	public String tibbr_usr = "tibbradmin";
 	public String tibbr_pwd = "Tibbr2013";
 	private HashMap<String, Object> prevProps; 
 
 	/**
 	 * @param url
 	 * @param usr
 	 * @param pwd
 	 * @param usrCentric
 	 */
 	public RenderGraph(String url, String usr, String pwd, String usrCentric, HashMap<String, Object> map){
 
 		
 		this.tibbr_url = url;
 		this.tibbr_usr = usr;
 		this.tibbr_pwd = pwd;
 		userCentric = usrCentric;
 		prevProps = new HashMap<String, Object>(map);   // copy map from parameter into member
 		
 		System.out.println("Getting graph data for tibbr server: " + url);
 		
 		if (userCentric != "" ){
 			System.out.println("User-centric graph for user: " + userCentric);
 		}
 		
 		String[] a = this.tibbr_url.split("//");
 		String[] url_server = a[1].split(".t"); // assumes the URL is *.tibbr.*
 
 		//String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
 		//filename = b[0]+ "_graph_" +date+ ".csv";
 		filename = url_server[0]+ "_graph.csv";
 
 	}
 	
 	public RenderGraph(){
 
 		
 		String[] a = this.tibbr_url.split("//");
 		String[] url = a[1].split(".t"); // assumes the URL is *.tibbr.*
 
 		//String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
 		//filename = b[0]+ "_graph_" +date+ ".csv";
 		filename = url[0]+ "_graph.csv";
 
 	}
 
 	public ProcessingTarget buildGraph(){
 
 
 		// checks if a data file exists, otherwise calls the class to gather raw data from tibbr	
 		getTibbrGraph();
 
 		//Init a project - and therefore a workspace
 		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
 		pc.newProject();
 		Workspace workspace = pc.getCurrentWorkspace();
 
 		//Get a graph model - it exists because we have a workspace
 		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
 		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
 		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
 
 		//Import file 
 		System.out.println("Loading raw tibbr data into graph");
 		Container container;
 		try {
 			// if the user centric user exists, lets create a new CSV, with just that users relations.
			if (!userCentric.equals("")){
 				filename = CreateUserCentricCSV();
 			}
 			
 			File f = new File(filename);
 			container = importController.importFile(f);
 			container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
 		    container.setAllowAutoNode(false);  //Don't create missing nodes
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		}
 
 		//Append imported data to GraphAPI
 		importController.process(container, new DefaultProcessor(), workspace);
 
 // -----------------------------  GRAPH LAYOUT ---------------------------------------
 
 		DirectedGraph graph = graphModel.getDirectedGraph();
 		 
 	//  1) LAYOUT
 		
 		// Force-Atlas - linked node attract - non-linked repell.
 		 
 		AutoLayout autoLayout = new AutoLayout(7, TimeUnit.SECONDS);
 		autoLayout.setGraphModel(graphModel);
 		//YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
 		
 		
 		ForceAtlasLayout faLayout = new ForceAtlasLayout(null);
 		
 		//tinker with some of the layout properties
 		AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
 		AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(10000.), 0f);//500 for the complete period
 		//autoLayout.addLayout( firstLayout, 0.5f );
 		
 		autoLayout.addLayout(faLayout, 1f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
 		System.out.println("Applying graph layout...");
 		autoLayout.execute();
 		
 
 	// 2) RANKING 
 		
 		//Rank color of nodes by Degree (which mean most connected node have highest ranking)
 		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
 		Ranking<?> degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
 		AbstractColorTransformer<?> colorTransformer = (AbstractColorTransformer<?>) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
 		float[] positions = {0f,0.33f,0.66f,1f};
 		colorTransformer.setColorPositions(positions);
 		Color[] colors = new Color[]{new Color(0x0000FF), new Color(0xFFFFFF),new Color(0x00FF00),new Color(0xFF0000)};
 		colorTransformer.setColors(colors);
 		rankingController.transform(degreeRanking,colorTransformer);
 
 	//Get graph distance metrics (average path length) - used to get centrality 
 		GraphDistance distance = new GraphDistance();
 		distance.setDirected(true);
 		distance.execute(graphModel, attributeModel);
 
 	//Rank size of node by "Betweeness Centrality" - a measure of how often a node appears on the shortest path between nodes.
 		// in otherwords, the more often a node is in the path between all nodes, the higher its BC score. A measure of its influence.
 		AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
 		
 		Ranking<?> centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
 		AbstractSizeTransformer<?> sizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
 		sizeTransformer.setMinSize(3);
 		sizeTransformer.setMaxSize(24);
 		rankingController.transform(centralityRanking,sizeTransformer);
 
 	//Rank label size - set a multiplier size
 		Ranking<?> centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
 		AbstractSizeTransformer<?> labelSizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
 		labelSizeTransformer.setMinSize(1);
 		labelSizeTransformer.setMaxSize(10);
 		rankingController.transform(centralityRanking2,labelSizeTransformer);
 		
 		
 	// 3) Partitioning
 		
 	//Run modularity algorithm - community detection
 		Modularity modularity = new Modularity();
 		modularity.execute(graphModel, attributeModel);
 		 
 		PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
 		
 		//Partition with 'modularity_class', just created by Modularity algorithm
 		AttributeColumn modColumn = attributeModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
 		@SuppressWarnings("rawtypes")
 		Partition p2 = partitionController.buildPartition(modColumn, graph);
 		System.out.println(p2.getPartsCount() + " partitions found");
 		NodeColorTransformer nodeColorTransformer2 = new NodeColorTransformer();
 		nodeColorTransformer2.randomizeColors(p2);
 		partitionController.transform(p2, nodeColorTransformer2);
 		
 		
 		
 //------------------------------ DISPLAY ----------------------------------------
 		//Preview configuration
 		System.out.println("Building preview...");
 		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
 		PreviewModel previewModel = previewController.getModel();
 		
 		
 		// edges curved is coming from UI
 		prevProps.put(PreviewProperty.DIRECTED, Boolean.TRUE);
 		prevProps.put(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
 		prevProps.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
 		prevProps.put(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
 		prevProps.put(PreviewProperty.ARROW_SIZE,5f);
 		prevProps.put(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE,Boolean.TRUE);
 		
 		
 		
 		Iterator<String> keySetIterator = prevProps.keySet().iterator();
 		
 		while (keySetIterator.hasNext()){
 			
 			String key = keySetIterator.next();
 			previewModel.getProperties().putValue(key, prevProps.get(key));
 			
 		}
 		
 		
 		
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 100);
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 1f);
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS,0.2f);
 
 
 		previewController.refreshPreview();
 
 		//----------------------------
 
 		//New Processing target, get the PApplet
 		ProcessingTarget target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
 		
 		PApplet applet = target.getApplet();
 		applet.init();
 
 		// Add .1 second delay to fix stability issue - per Gephi forums
             try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
         
 		
 		//Refresh the preview and reset the zoom
 		previewController.render(target);
 		target.refresh();
 		target.resetZoom();
 		target.zoomMinus();
 		System.out.println("Graph redered...");
 		return target;
 		
 
 	}
 
 	private String CreateUserCentricCSV() throws IOException {
 		// reads existing file of users, and extracts to a new file rows that contain our specified user
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(filename));  // assumes that by now we have the full list of users
 
 				
 				// first get users followed by pricinciple.
 			    StringBuilder sb = new StringBuilder();
 			    String line = br.readLine();
 
 			    while (line != null) {
 			    	
 			    	if (line.contains(this.userCentric)){
 			    		sb.append(line);
 			    		sb.append('\n');
 			    	}
 			        line = br.readLine();
 			    }
 			    String everything = sb.toString();
 			    br.close();
 			    
 			    BufferedWriter writer = null;
 			    
 			    String newFilename = filename.substring(0, (filename.length() - 4)) + "_" + this.userCentric + ".csv";
 				writer = new BufferedWriter(new FileWriter(newFilename));
 				writer.write(everything);
 				writer.close();
 				return newFilename;
 				
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	        
 
 	       
 
 		
 	
 	}
 
 	// login to tibbr REST API and get user followers
 	public void getTibbrGraph(){
 
 		if (!fDataFileExists()) 
 		{
 			System.out.println("No cached graph data for this server - contacting tibbr server...");
 			GetGraphDataFromTibbr tibbr = new GetGraphDataFromTibbr(this.tibbr_url, this.tibbr_usr, this.tibbr_pwd);
 			try {
 				tibbr.loginUser();	
 				tibbr.getTibbrUserData();  // just stores each user and who they follow in an array
 				this.users = tibbr.myUsers;
 
 				dumpUsersToFile();  // write all user data from array to csv, ready for Gephi toolkit.
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else
 			System.out.println("Cached user data found for this server...");
 	}
 
 
 	public void dumpUsersToFile(){
 
 		try {
 
 			BufferedWriter writer = null;
 			writer = new BufferedWriter(new FileWriter(filename));
 			for ( int i = 0; i < this.users.length; i++)
 			{   
 				if( users[i].login == "") break;
 
 				for ( int j = 0; j < this.users[i].idols.length; j++)
 				{   
 					if (users[i].idols[j] == "")
 						break;
 					writer.write(users[i].login + "," + users[i].idols[j]);
 					writer.newLine();
 				}
 
 
 			}
 			writer.close();
 		} catch(IOException ex) {
 			ex.printStackTrace();
 		}
 
 
 	}
 
 	public boolean fDataFileExists(){
 		Boolean retVal = false;
 
 		File f = new File(this.filename);
 		if(f.exists()) { retVal = true;}
 
 
 		return retVal;
 
 	}	
 
 	public void getGraphFromArray(GraphModel graphModel){
 
 
 
 		//Create All Nodes first
 		for (int i=0; i<this.users.length; i++) {
 			if (users[i].login != "") {
 				arrNodes[i] = graphModel.factory().newNode(users[i].login);
 				arrNodes[i].getNodeData().setLabel(users[i].login);
 			}	
 		}
 
 		int eIdx = 0;
 		// Create All Edges
 		for (int i=0; i<this.users.length; i++) {
 			Node currUser = arrNodes[i];
 			//loop through each idol
 			for (int j=0; j < this.users[i].idols.length; j++) {	
 				if (this.users[i].idols[j] != "") {
 					Node idol = getNodeFromLogin(this.users[i].idols[j]);
 					if (idol != null){
 						arrEdges[eIdx] = graphModel.factory().newEdge( currUser, idol, 2f, true);
 						eIdx++;
 					}
 				}
 			}
 		}
 
 		//Append as a Directed Graph
 		DirectedGraph directedGraph = graphModel.getDirectedGraph();
 		for (int i=0; i < arrNodes.length;i++) {
 			if (users[i].login == ""){
 				break;
 			}
 			if (users[i].login != ""){
 				directedGraph.addNode(arrNodes[i]);
 			}
 		}
 		for (int i=0; i < eIdx;i++) {
 			directedGraph.addEdge(arrEdges[i]);
 		}
 
 	}
 	public Node getNodeFromLogin(String login){
 
 		Node retval = null;
 
 		for (int i = 0; i < users.length; i++){
 			if (users[i].login == ""){
 				break;
 			}	  
 
 
 			if (users[i].login.equals(login)){
 				retval = arrNodes[i];
 				break;
 			}	  
 		}
 		return retval;
 
 	}
 
 
 
 
 }
