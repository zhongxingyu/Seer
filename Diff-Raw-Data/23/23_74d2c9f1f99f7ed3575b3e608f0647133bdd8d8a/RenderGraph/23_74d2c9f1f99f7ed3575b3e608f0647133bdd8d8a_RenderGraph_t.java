 package org.ben.socialgraph;
 
 //import java.awt.BorderLayout;
 import java.awt.Color;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 //import java.text.SimpleDateFormat;
 //import java.util.Date;
 import java.util.concurrent.TimeUnit;
 //import javax.swing.JFrame;
 
 
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
 import org.gephi.preview.api.*;
 import org.gephi.preview.types.DependantOriginalColor;
 import org.gephi.project.api.ProjectController;
 import org.gephi.project.api.Workspace;
 import org.gephi.ranking.api.Transformer;
 import org.gephi.ranking.api.Ranking;
 import org.gephi.ranking.api.RankingController;
 import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
 import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
 import org.gephi.statistics.plugin.GraphDistance;
 import org.openide.util.Lookup;
 
 import processing.core.PApplet;
 
 
 
 
 
 public class RenderGraph  {
 
 	public TibbrUser[] users = new TibbrUser[200];
 	public Node[] arrNodes = new Node[200];
 	public Edge[] arrEdges = new Edge[1000];
 	public String filename = "graph.csv";
 
 	public String tibbr_url = "https://malaysiaair.tibbr.com";
 	public String tibbr_usr = "tibbradmin";
 	public String tibbr_pwd = "Tibbr2013";
 
 	//public MainFrame frame;
 	
 	// Entrypoint for the app
 	
 	// constructor
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
 		Container container;
 		try {
 			File f = new File(this.filename);
 			container = importController.importFile(f);
 			container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
 		    container.setAllowAutoNode(false);  //Don't create missing nodes
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		}
 
 		//Append imported data to GraphAPI
 		importController.process(container, new DefaultProcessor(), workspace);
 
 		//See if graph is well imported
 		//DirectedGraph graph = graphModel.getDirectedGraph();
 		//---------------------------
 
 		//Layout for 1 minute
 		AutoLayout autoLayout = new AutoLayout(5, TimeUnit.SECONDS);
 		autoLayout.setGraphModel(graphModel);
 		//YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
 		ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
 		AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
 		AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(10000.), 0f);//500 for the complete period
 		//autoLayout.addLayout( firstLayout, 0.5f );
 		autoLayout.addLayout(secondLayout, 1f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
 		autoLayout.execute();
 
 
 
 
 
 		//Rank color by Degree
 		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		Ranking<?> degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		AbstractColorTransformer<?> colorTransformer = (AbstractColorTransformer<?>) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
 
 		colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
 		rankingController.transform(degreeRanking,colorTransformer);
 
 		//Get Centrality
 		GraphDistance distance = new GraphDistance();
 		distance.setDirected(true);
 		distance.execute(graphModel, attributeModel);
 
 		//Rank size by centrality
 		AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Ranking<?> centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		AbstractSizeTransformer<?> sizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
 		sizeTransformer.setMinSize(3);
 		sizeTransformer.setMaxSize(20);
 		rankingController.transform(centralityRanking,sizeTransformer);
 
 		//Rank label size - set a multiplier size
		Ranking<?> centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		AbstractSizeTransformer<?> labelSizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
 		labelSizeTransformer.setMinSize(1);
 		labelSizeTransformer.setMaxSize(3);
 		rankingController.transform(centralityRanking2,labelSizeTransformer);
 
 		float[] positions = {0f,0.33f,0.66f,1f};
 		colorTransformer.setColorPositions(positions);
 		Color[] colors = new Color[]{new Color(0x0000FF), new Color(0xFFFFFF),new Color(0x00FF00),new Color(0xFF0000)};
 		colorTransformer.setColors(colors);
 
 		
 		//---------------------------------
 		//Preview configuration
 		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
 		PreviewModel previewModel = previewController.getModel();
 		previewModel.getProperties().putValue(PreviewProperty.DIRECTED, Boolean.TRUE);
 		previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
 		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
 		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.YELLOW));
 		
 		
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 100);
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 1f);
 		//previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS,0.2f);
 		//previewModel.getProperties().putValue(PreviewProperty.ARROW_SIZE,0.2f);
 
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
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         
 		
 		//Refresh the preview and reset the zoom
 		previewController.render(target);
 		target.refresh();
 		target.resetZoom();
 		target.zoomMinus();
 		target.zoomMinus();
 		
 		return target;
 		
 
 	}
 
 	// login to tibbr REST API and get user followers
 	public void getTibbrGraph(){
 
 		if (!fDataFileExists()) 
 		{
 			GetGraphDataFromTibbr tibbr = new GetGraphDataFromTibbr(this.tibbr_url, this.tibbr_usr, this.tibbr_pwd);
 			try {
 				tibbr.loginUser();	
 				tibbr.getTibbrUserData();
 				this.users = tibbr.myUsers;
 				dumpUsersToFile(this.filename);
 
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	public void dumpUsersToFile(String fileName){
 
 		try {
 
 			BufferedWriter writer = null;
 			writer = new BufferedWriter(new FileWriter(fileName));
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
