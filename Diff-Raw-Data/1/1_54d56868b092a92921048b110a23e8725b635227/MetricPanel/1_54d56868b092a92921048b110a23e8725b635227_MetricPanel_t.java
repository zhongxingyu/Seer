 package de.uni_leipzig.simba.saim.gui.widget.panel;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Stack;
 
 import lombok.Getter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vaadin.cytographer.Cytographer;
 import org.vaadin.cytographer.CytographerActionToolbar;
 import org.vaadin.cytographer.GraphProperties;
 
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.event.LayoutEvents.LayoutClickEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickListener;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Button.ClickEvent;
 
 import csplugins.layout.algorithms.force.ForceDirectedLayout;
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.view.CyNetworkView;
 import cytoscape.visual.VisualPropertyType;
 import de.konrad.commons.sparql.PrefixHelper;
 import de.uni_leipzig.simba.data.Mapping;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.core.metric.Measure;
 import de.uni_leipzig.simba.saim.core.metric.MetricParser;
 import de.uni_leipzig.simba.saim.core.metric.Node;
 import de.uni_leipzig.simba.saim.core.metric.Operator;
 import de.uni_leipzig.simba.saim.core.metric.Output;
 import de.uni_leipzig.simba.saim.core.metric.Property;
 import de.uni_leipzig.simba.saim.gui.widget.Listener.MetricPanelListeners;
 
 /** Contains instances of ClassMatchingForm and lays them out vertically.*/
 public class MetricPanel extends Panel{    
 
 	final static Logger logger = LoggerFactory.getLogger(MetricPanel.class);
 	@Getter private final Messages messages;
 	@Getter private Configuration config;
 	private static final long	serialVersionUID	= 6766679517868840795L;
 	VerticalLayout mainLayout = new VerticalLayout();
 	HorizontalLayout layout = new HorizontalLayout();
 	HorizontalLayout buttonLayout = new HorizontalLayout();
 	Set<String> sourceProps = new HashSet<String>();
 	Set<String> targetProps = new HashSet<String>();
 	
 	Button selfConfigButton;
 	Button learnButton;
 	Button startMapping;
 	Button setMetric;
 	
 	Cytographer cytographer;
 	CyNetworkView cyNetworkView;
 	
 	VerticalLayout sourceLayout =  new VerticalLayout();
 	VerticalLayout targetLayout =  new VerticalLayout();
 	VerticalLayout metricsLayout =  new VerticalLayout();
 	VerticalLayout operatorsLayout =  new VerticalLayout();
 
 	public MetricPanel(final Messages messages) {	this.messages = messages;}
 	@Override
 	public void attach() {
 		if((SAIMApplication)getApplication()!= null)
 			config = ((SAIMApplication)getApplication()).getConfig();
 		mainLayout.setSpacing(false);
 		mainLayout.setMargin(false);
 		final VerticalLayout accordionLayout = new VerticalLayout();
 		layout.addComponent(accordionLayout);
 		layout.setSpacing(false);
 		layout.setMargin(false);
 		setContent(mainLayout);
 		mainLayout.addComponent(layout);
 		final ProgressIndicator progress = new ProgressIndicator();
 		progress.setIndeterminate(false);
 		accordionLayout.addComponent(progress);
 		//buttons		
 		mainLayout.addComponent(getButtonLayout());
 		// accordion panel
 		Panel accordionPanel = new Panel();
 		accordionPanel.setHeight("100%"); //$NON-NLS-1$
 		accordionLayout.addComponent(accordionPanel);
 		accordionPanel.setWidth("25em"); //$NON-NLS-1$
 		final Accordion accordion = new Accordion();		
 		accordion.setHeight("100%"); //$NON-NLS-1$
 
 		accordionPanel.addComponent(accordion);
 		
 		accordion.addTab(sourceLayout,messages.getString("MetricPanel.sourceproperties")); //$NON-NLS-1$
 		accordion.addTab(targetLayout,messages.getString("MetricPanel.targetproperties")); //$NON-NLS-1$
 		accordion.addTab(metricsLayout,messages.getString("MetricPanel.metrics"));  //$NON-NLS-1$
 		accordion.addTab(operatorsLayout,messages.getString("MetricPanel.operators"));	 //$NON-NLS-1$
 		// add Cytographer
 		cytographer = getCytographer();
 		layout.addComponent(new CytographerActionToolbar(cytographer));
 		layout.addComponent(cytographer);
 		
 		new Thread(){			
 			@Override
 			public void run(){
 				//	performPropertyMapping();
 				getAllProps();
 
 				for(String s : sourceProps) {
 					final Label check = new Label(s);
 					sourceLayout.addComponent(check); 
 				}
 						
 				for(String t : targetProps) {
 					final Label check = new Label(t);
 					targetLayout.addComponent(check);
 				}
 				accordionLayout.removeComponent(progress);
 				progress.setEnabled(false);
 			}
 		}.start();
 //		metricsLayout.addComponent( new Label(messages.getString("MetricPanel.0"))); 
 //		operatorsLayout.addComponent( new Label(messages.getString("MetricPanel.8"))); 		
 		for(String label : Measure.identifiers)
 			metricsLayout.addComponent( new Label(label)); 
 		for(String label : Operator.identifiers)
 			operatorsLayout.addComponent( new Label(label)); 	
 		
 		sourceLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.SOURCE, config,messages));
 		targetLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.TARGET, config,messages));
 		metricsLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.METRIC, config,messages));
 		operatorsLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.OPERATOR, config,messages));
 		
 		this.checkButtons();
 	}
 	
 	private Cytographer getCytographer(){
 		
 		final int HEIGHT = 450;
 		final int WIDTH = 800;
 		final int NODESIZE = 100;
 		final double EDGE_LABEL_OPACITY = 0d;
 		
 		Cytoscape.createNewSession();	
 		Cytoscape.getVisualMappingManager().getVisualStyle().getGlobalAppearanceCalculator().setDefaultBackgroundColor(Color.WHITE);
 		Cytoscape.getVisualMappingManager().getVisualStyle().getEdgeAppearanceCalculator().getDefaultAppearance().set(VisualPropertyType.EDGE_COLOR,Color.BLACK);
 		Cytoscape.getVisualMappingManager().getVisualStyle().getEdgeAppearanceCalculator().getDefaultAppearance().set(VisualPropertyType.EDGE_LABEL_OPACITY,EDGE_LABEL_OPACITY);
 		Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator().getDefaultAppearance().set(VisualPropertyType.NODE_SIZE, NODESIZE);
 		
 		String name = "MyName";
 		CyNetwork cyNetwork = Cytoscape.createNetwork(name, false);		
 		cyNetworkView = Cytoscape.createNetworkView(cyNetwork);
 
 		Window window = SAIMApplication.getInstance().getMainWindow();
 		cytographer = new Cytographer(cyNetwork, cyNetworkView, name, WIDTH, HEIGHT,window,this);
 		cytographer.setImmediate(true);
 		cytographer.setWidth(WIDTH + "px"); //$NON-NLS-1$
 		cytographer.setHeight(HEIGHT + "px"); //$NON-NLS-1$
 		cytographer.setTextVisible(true);		
 		cytographer.setNodeSize(NODESIZE, true);	
 		
 		String metricExpression = config.getMetricExpression();
 		//metricExpression = "AND(levenshtein(x.rdfs:label,y.rdfs:label)|0.1,levenshtein(x.dbp:name,y.dbp:name)|1.0)";
 		if( metricExpression != null){
 		//	makeMetric( MetricParser.parse(metricExpression, "x"));
 			makeMetricRecursive(MetricParser.parse(metricExpression, config.getSource().var.replaceAll("\\?", "")), -1); //$NON-NLS-1$
 			cyNetworkView.applyLayout(new ForceDirectedLayout());		
 			cytographer.repaintGraph();
 		}else{
 			cytographer.addNode(new Output().id, WIDTH/2, HEIGHT/2, GraphProperties.Shape.OUTPUT);
 			cytographer.repaintGraph();
 		}
 		return cytographer;		
 	}
 
 	private Map<Integer,Node> blacklist = new HashMap<Integer,Node>();
 	/**
 	 * @param o the Output node
 	 * @deprecated use makeMetricRecursive() instead.
 	 */
 	private void makeMetric(Output output)
 	{
 		Stack<Node> cNodes = new Stack<Node>();
 		cNodes.push(output);
 		
 		while(!cNodes.isEmpty()){
 			Integer nID = null;
 			Node n = cNodes.pop();
 			if(!blacklist.containsValue(n)){
 				nID = addNode(n);
 				blacklist.put(nID, n);
 			}else{
 				for(Integer id : blacklist.keySet()){
 					if(blacklist.get(id).equals(n)){
 						nID=id;
 						break;						
 					}
 				}
 			}
 			
 			cNodes.addAll(n.getChilds());
 			for(Node c : n.getChilds()){
 				Integer cID = null; 
 				/* Need to support for adding multiple property nodes 
 				 * the second or part does that.
 				 * @TODO test test test!!!
 				 */
 				if(!blacklist.containsValue(c)||c.getMaxChilds()==0){
 					cID = addNode(c);
 					blacklist.put(cID, c);
 				}else{
 					for(Integer id : blacklist.keySet()){
 						if(blacklist.get(id).equals(c)){
 							cID=id;
 							break;						
 						}
 					}
 				}				
 				addEdge(nID,cID);			
 			}
 		}
 	}
 	/**
 	 * Recursive  function to create a graphical representation out of a output node.
 	 * @param n Call with the Output (root) node.
 	 * @param parentId On call just use an arbitrary value: 
 	 */
 	private void makeMetricRecursive(Node n, int parentId) {
 		if(n.getClass()==Output.class) {
 			parentId = addNode(n);
 		}
 		HashMap<Integer, Node> cList = new HashMap<Integer, Node>();
 		for(Node c : n.getChilds()) {
 				cList.put(addNode(c), c);
 		}
 		for(Entry<Integer, Node> c : cList.entrySet()) {
 			addEdge(parentId, c.getKey());
 		}
 		for(Entry<Integer, Node> c : cList.entrySet()) {
 			makeMetricRecursive(c.getValue(), c.getKey());
 		}
 	}
 	private int addNode(Node n){
 		Integer id = null;
 		// make node
 		if(n instanceof Output){
 			id= cytographer.addNode(new Output().id, 0, 0, GraphProperties.Shape.OUTPUT);
 			List<Object> l = new ArrayList<Object>();
 			l.add(((Output)n).param1);
 			l.add(((Output)n).param2);
			cytographer.setNodeMetadata(id+"", l);
 		}else if(n instanceof Operator){
 			id= cytographer.addNode(((Operator)n).id, 0, 0, GraphProperties.Shape.OPERATOR);
 			List<Object> l = new ArrayList<Object>();
 			l.add(((Operator)n).param1);
 			l.add(((Operator)n).param2);		
 			cytographer.setNodeMetadata(id+"", l);
 			
 		}else if(n instanceof Property){
 			if(((Property)n).getOrigin().equals(Property.Origin.TARGET)){
 				id=cytographer.addNode(((Property)n).id, 0, 0, GraphProperties.Shape.TARGET);
 			}else{
 				id=cytographer.addNode(((Property)n).id, 0, 0, GraphProperties.Shape.SOURCE);
 			}
 			
 		}else if(n instanceof Measure){
 			id=cytographer.addNode(((Measure)n).id, 0, 0, GraphProperties.Shape.METRIC);
 		}
 		return id;
 	}
 	private void addEdge(int nID,int cID){
 		cytographer.createAnEdge(nID, cID, new String(nID+"_to_"+cID));		
 	}
 	
 	private void getAllProps() {
 //		Configuration config = Configuration.getInstance();
 //		if(config.isLocal) {
 //			logger.info("Local data - using specified properties");
 			if(config != null)
 			if( config.getSource() != null && config.getSource().properties != null && config.getSource().properties.size()>0 &&
 					config.getTarget() != null && config.getTarget().properties != null && config.getTarget().properties.size()>0) {
 				for(String prop : config.getSource().properties) {
 					if(prop.trim().length()>0) {
 						String s_abr=PrefixHelper.abbreviate(prop);
 						sourceProps.add(s_abr);
 					}
 				}
 				
 				for(String prop : config.getTarget().properties) {
 					if(prop.trim().length()>0) {
 						String s_abr=PrefixHelper.abbreviate(prop);
 						targetProps.add(s_abr);
 					}
 				}
 				selfConfigButton.setEnabled(true);
 			}			
 			return;
 	}
 	
 	/**
 	 * Creates the button is the lower window.
 	 * @return
 	 */
 	public Layout getButtonLayout() {
 		this.setMetric = new Button(messages.getString("MetricPanel.setmetricbutton"));
 		setMetric.setEnabled(true);
 		setMetric.addListener(new ClickListener() {			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				setMetricFromGraph();
 			}
 		});
 		
 		selfConfigButton = new Button(messages.getString("MetricPanel.startselfconfigbutton")); //$NON-NLS-1$
 		selfConfigButton.setEnabled(false);
 		selfConfigButton.addListener(new MetricPanelListeners.SelfConfigClickListener(messages));
 		
 		this.learnButton = new Button(messages.getString("MetricPanel.learnmetricbutton")); //$NON-NLS-1$
 		learnButton.setEnabled(false);
 		learnButton.addListener(new MetricPanelListeners.LearnClickListener(messages));
 		
 		this.startMapping = new Button(messages.getString("MetricPanel.startmappingbutton")); //$NON-NLS-1$
 		startMapping.setEnabled(false);
 		startMapping.addListener(new MetricPanelListeners.StartMappingListener(messages));
 		
 		buttonLayout.addComponent(setMetric);
 		buttonLayout.addComponent(selfConfigButton);
 		buttonLayout.addComponent(learnButton);
 		buttonLayout.addComponent(startMapping);
 		return buttonLayout;		
 	}
 	
 	/**
 	 * Method to set Metric from the graph.
 	 */
 	protected void setMetricFromGraph() {
 		if(!cytographer.getMetric().isComplete()) {
 			getApplication().getMainWindow().showNotification(messages.getString("MetricPanel.settingnotablenotcomplete")); //$NON-NLS-1$
 		} else {
 			Node node =  cytographer.getMetric();
 			String expr = cytographer.getMetric().toString();
 			config.setMetricExpression(expr);
 			System.out.println(node.param1+" - "+node.param2); //$NON-NLS-1$
 			if(node.param1 != null && node.param2 != null) {
 				config.setAcceptanceThreshold(node.param1);
 				config.setVerificationThreshold(node.param2);
 				getApplication().getMainWindow().showNotification("Setting: "+expr+ "with thresholds "+node.param1+" / "+node.param2); //$NON-NLS-1$
 			}
 			else {
 				getApplication().getMainWindow().showNotification(messages.getString("MetricPanel.settingnotablenothreholds")); //$NON-NLS-1$
 			}
 		}		
 	}
 	/**
 	 * Checks whether the Buttons (selfconfig, learning and startMapping) could be activated.
 	 */
 	public void checkButtons() {
 		if((SAIMApplication)getApplication()!=null) {
 			Configuration config = ((SAIMApplication)getApplication()).getConfig();
 			if( config.getSource() != null && config.getSource().properties != null && config.getSource().properties.size()>0 &&
 					config.getTarget() != null && config.getTarget().properties != null && config.getTarget().properties.size()>0) {
 				selfConfigButton.setEnabled(true);
 				if(config.getMetricExpression() != null && config.getMetricExpression().length()>0) {
 					learnButton.setEnabled(true);
 					startMapping.setEnabled(true);
 				}
 			}		
 		}
 	}
 
 
 	/**Listener to react on clicks in the accordion panel.*/
 	class AccordionLayoutClickListener implements LayoutClickListener
 	{
 		private static final long serialVersionUID = -3498649095113131161L;
 		private Cytographer cytographer;
 		private CyNetworkView cyNetworkView;
 		private GraphProperties.Shape shape;
 		private Configuration config;
 		private final Messages messages;
 		
 		public AccordionLayoutClickListener(Cytographer cytographer, CyNetworkView cyNetworkView,GraphProperties.Shape shape, Configuration config,final Messages messages)
 		{
 			this.cytographer = cytographer;
 			this.cyNetworkView = cyNetworkView;
 			this.shape = shape;
 			this.config = config;
 			this.messages=messages;
 		}
 		
 		@Override
 		public void layoutClick(LayoutClickEvent event) {
 			// its left button
 			if(event.getButtonName().equalsIgnoreCase("left") && event.getClickedComponent() instanceof Label ){ //$NON-NLS-1$
 				String label = ((Label)event.getClickedComponent()).getValue().toString();
 				int x = (int)cytographer.getWidth()/2;
 				int y = (int)cytographer.getHeight()/2;
 				
 				switch(shape){
 				case SOURCE :{
 					String pref = config.getSource().var.replaceAll("\\?", ""); //$NON-NLS-1$
 					cytographer.addNode(pref+"."+label, x,y, shape); //$NON-NLS-1$
 					cytographer.addDefaultProperty(label, config.getSource());
 					break;
 				}
 				case TARGET : {
 					String pref = config.getTarget().var.replaceAll("\\?", ""); //$NON-NLS-1$
 					cytographer.addNode(pref+"."+label, x,y, shape); //$NON-NLS-1$
 					cytographer.addDefaultProperty(label, config.getTarget());
 					break;
 				}
 				default :
 						cytographer.addNode(label, x,y, shape);
 				}
 				// repaint
 				cytographer.repaintGraph();
 			}
 		}
 	} // end of class AccordionClickListener
 }// end of MetricPanel
