 package de.uni_leipzig.simba.saim.gui.widget;
 
 import java.awt.Color;
 import java.awt.Shape;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.vaadin.cytographer.Cytographer;
 import org.vaadin.cytographer.model.GraphProperties;
 
 import com.github.wolfie.refresher.Refresher;
 import com.github.wolfie.refresher.Refresher.RefreshListener;
 import com.vaadin.data.Validator;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.event.LayoutEvents.LayoutClickEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickListener;
 import com.vaadin.terminal.UserError;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import csplugins.layout.algorithms.force.ForceDirectedLayout;
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.view.CyNetworkView;
 
 import de.konrad.commons.sparql.PrefixHelper;
 import de.konrad.commons.sparql.SPARQLHelper;
 import de.uni_leipzig.simba.cache.HybridCache;
 import de.uni_leipzig.simba.data.Mapping;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.learning.query.PropertyMapper;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.selfconfig.MeshBasedSelfConfigurator;
 import de.uni_leipzig.simba.selfconfig.SimpleClassifier;
 
 /** Contains instances of ClassMatchingForm and lays them out vertically.*/
 public class MetricPanel extends Panel
 {
 	ManualMetricForm manualMetricForm;
 	private static final long	serialVersionUID	= 6766679517868840795L;
 	Mapping propMapping;
 	VerticalLayout mainLayout = new VerticalLayout();
 	HorizontalLayout layout = new HorizontalLayout();
 
 	Set<String> sourceProps = new HashSet<String>();
 	Set<String> targetProps = new HashSet<String>();
 
 	Button selfconfig;
 	Cytographer cytographer;
 	CyNetworkView cyNetworkView;
 	final VerticalLayout sourceLayout =  new VerticalLayout();
 	final VerticalLayout targetLayout =  new VerticalLayout();
 	final VerticalLayout metricsLayout =  new VerticalLayout();
 	final VerticalLayout operatorsLayout =  new VerticalLayout();
 
 	public MetricPanel()
 	{
 		mainLayout.setSpacing(false);
 		mainLayout.setMargin(false);
 		mainLayout.setWidth("100%");
 		layout.setSpacing(false);
 		layout.setMargin(false);
 		setContent(mainLayout);
 		layout.setWidth("100%");
 		mainLayout.addComponent(layout);
 //		mainLayout.setComponentAlignment(layout, Alignment.TOP_LEFT);
 		final VerticalLayout accordionLayout = new VerticalLayout();
 		layout.addComponent(accordionLayout);	
 //		layout.setComponentAlignment(accordionLayout, Alignment.TOP_LEFT);
 		final ProgressIndicator progress = new ProgressIndicator();
 		progress.setIndeterminate(false);
 		accordionLayout.addComponent(progress);
 		// self config
 		mainLayout.addComponent(manualMetricForm=new ManualMetricForm());
 		selfconfig = new Button("Start SelfConfiguration");
 		selfconfig.setEnabled(false);
 		selfconfig.addListener(new SelfConfigClickListener(layout));
 		mainLayout.addComponent(selfconfig);
 
 		// accordion panel
 		Panel accordionPanel = new Panel();
 		accordionLayout.addComponent(accordionPanel);
 		accordionPanel.setWidth("25em");
 
 		final Accordion accordion = new Accordion();		
 		accordionPanel.addComponent(accordion);
 		
 		accordion.addTab(sourceLayout,Messages.getString("MetricPanel.sourceproperties"));		
 		accordion.addTab(targetLayout,Messages.getString("MetricPanel.targetproperties"));
 		accordion.addTab(metricsLayout,Messages.getString("MetricPanel.metrics")); 
 		accordion.addTab(operatorsLayout,Messages.getString("MetricPanel.operators"));	
 		
 
 		
 		// add Cytographer
 		accordionLayout.addComponent(getCytographer());
 		
 		new Thread(){			
 			@Override
 			public void run(){
 				//	performPropertyMapping();
 				getAllProps();
 
 				for(String s : sourceProps) {
 					final Label check = new Label(s);
 
 					//	check.setCaption(s);
 					//						check.addListener(new Property.ValueChangeListener() {							
 					//							@Override
 					//							public void valueChange(ValueChangeEvent event) {
 					//								String prop = check.getCaption();
 					//								if(check.booleanValue() && prop != null && prop.length()>0) {
 					//									String s_abr=PrefixHelper.abbreviate(prop);
 					//									sourceProps.add(s_abr);
 					//									Configuration.getInstance().getSource().properties.add(s_abr);
 					//									Configuration.getInstance().getSource().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 					//									Configuration.getInstance().getSource().functions.put(s_abr, "lowercase");
 					//									System.out.println("Adding source property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 					//							}
 					//						});
 					sourceLayout.addComponent(check); 
 				}
 						
 				for(String t : targetProps) {
 					final Label check = new Label(t);
 
 					//						check.addListener(new Property.ValueChangeListener() {							
 					//							@Override
 					//							public void valueChange(ValueChangeEvent event) {
 					//								String prop = check.getCaption();
 					//								if(check.booleanValue() && prop != null && prop.length()>0) {
 					//									String s_abr=PrefixHelper.abbreviate(prop);
 					//									Configuration.getInstance().getTarget().properties.add(s_abr);
 					//									Configuration.getInstance().getTarget().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 					//									Configuration.getInstance().getTarget().functions.put(s_abr, "lowercase");
 					//									System.out.println("Adding target property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 					//							}
 					//						});
 					targetLayout.addComponent(check);
 				}
 				accordionLayout.removeComponent(progress);
 				progress.setEnabled(false);
 			}
 		}.start();
 		metricsLayout.addComponent( new Label("a metric"));
 		operatorsLayout.addComponent( new Label("an operator"));
 		
 		sourceLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.SOURCE));
 		targetLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.TARGET));
 		metricsLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.METRIC));
 		operatorsLayout.addListener(new AccordionLayoutClickListener(cytographer,cyNetworkView,GraphProperties.Shape.OPERATOR));	
 	}
 	private Cytographer getCytographer(){
 		
 		final int HEIGHT = 450;
 		final int WIDTH = 800;
 		final int NODESIZE = 100;
 
 		Cytoscape.createNewSession();	
 		Cytoscape.getVisualMappingManager().getVisualStyle().getGlobalAppearanceCalculator().setDefaultBackgroundColor(Color.WHITE);
 		String name = "MyName";
 		CyNetwork cyNetwork = Cytoscape.createNetwork(name, false);		
 		cyNetworkView = Cytoscape.createNetworkView(cyNetwork);
 
 		cytographer = new Cytographer(cyNetwork, cyNetworkView, name, WIDTH, HEIGHT,SAIMApplication.getInstance().getMainWindow());
 		cytographer.setImmediate(true);
 		cytographer.setWidth(WIDTH + "px");
 		cytographer.setHeight(HEIGHT + "px");
 		cytographer.setTextVisible(true);		
 		cytographer.setNodeSize(NODESIZE, true);	
 		
 		return cytographer;		
 	}
 //	private void performPropertyMapping() {
 //		Configuration config = Configuration.getInstance();
 //		config.getSource().properties.clear();
 //		config.getTarget().properties.clear();
 //		PropertyMapper propMapper = new PropertyMapper();
 //		String classSource = getClassOfEndpoint(config.getSource());
 //		String classTarget = getClassOfEndpoint(config.getTarget());
 //		if(classSource != null && classTarget != null) {
 //			showErrorMessage("Getting property mapping...");
 //			propMapping = propMapper.getPropertyMapping(config.getSource().endpoint,
 //					config.getTarget().endpoint, classSource, classTarget);
 //			for(String s : propMapping.map.keySet())
 //				for(Entry<String, Double> e : propMapping.map.get(s).entrySet()) {
 //					System.out.println(s + " - " + e.getKey());
 //					String s_abr=PrefixHelper.abbreviate(s);
 //					sourceProps.add(s_abr);
 //					config.getSource().properties.add(s_abr);
 //					config.getSource().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 //					System.out.println("Adding source property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 //					targetProps.add(PrefixHelper.abbreviate(e.getKey()));
 //					String t_abr=PrefixHelper.abbreviate(e.getKey());
 //					config.getTarget().properties.add(t_abr);
 //					config.getTarget().prefixes.put(PrefixHelper.getPrefixFromURI(t_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(t_abr)));
 //					System.out.println("Adding target property: "+t_abr+"::::"+PrefixHelper.getPrefixFromURI(t_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(t_abr)));
 //				}
 //		} else {
 //			showErrorMessage("Cannot perform automatic property mapping due to missing class specifications.");
 //		}		
 //	}
 
 	private void getAllProps() {
 		//for source
 		KBInfo info = Configuration.getInstance().getSource();
 		String className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
 		List<String> propList = SPARQLHelper.properties(info.endpoint, info.graph, className);
 		Logger.getLogger("SAIM").info("Got "+propList.size()+ " source props");
 		for(String prop : propList) {
			PrefixHelper.generatePrefix(prop);
 			String s_abr=PrefixHelper.abbreviate(prop);
 			sourceProps.add(s_abr);
 		}
 		//for target
 		info = Configuration.getInstance().getTarget();
 		className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
 		for(String prop : SPARQLHelper.properties(info.endpoint, info.graph, className)) {
 			String s_abr=PrefixHelper.abbreviate(prop);
 			targetProps.add(s_abr);
 		}	
 		//enable selfconfig
 		selfconfig.setEnabled(true);
 	}
 
 
 	/**
 	 * Little helper function to retrieve classes out of restrictions of the LIMES SPEC. Whereas, a
 	 * class is in a restriction of the format "?var rdf:type <class>".
 	 * @param ep
 	 * @return
 	 */
 	private String getClassOfEndpoint(KBInfo ep) {
 		for(String rest : ep.restrictions) {
 			if(rest.matches(".* rdf:type .*"))
 				return rest.substring(rest.indexOf("rdf:type")+8).replaceAll("<", "").replaceAll(">", "").trim();
 		}
 		return null;
 	}
 
 	private void showErrorMessage(String message) {
 		layout.setComponentError(new UserError(message));
 	}
 
 	/**
 	 * To decide whether we can proceed to the execution panel
 	 * @return
 	 */
 	public boolean isValid() {
 		try{
 			manualMetricForm.validate();
 		}catch(InvalidValueException e) {}
 		if(manualMetricForm.isValid()) {
 			Configuration.getInstance().setMetricExpression(manualMetricForm.metricTextField.getValue().toString());
 			Configuration.getInstance().setAcceptanceThreshold(Double.parseDouble(manualMetricForm.thresholdTextField.getValue().toString()));
 			return true;
 		} else {
 		//	manualMetricForm.setComponentError();
 		}
 		return false;
 	}
 
 	public class SelfConfigClickListener implements Button.ClickListener {
 		Layout l;
 		public SelfConfigClickListener(Layout l) {
 			this.l=l;
 		}
 		@Override
 		public void buttonClick(ClickEvent event) {
 			/* add all properties
 			for(String s : sourceProps) {
 				Configuration.getInstance().getSource().properties.add(s);
 				Configuration.getInstance().getSource().prefixes.put(PrefixHelper.getBase(s), PrefixHelper.getURI(PrefixHelper.getBase(s)));
 				Configuration.getInstance().getSource().functions.put(s, "");
 			}
 			for(String s : targetProps) {
 				Configuration.getInstance().getTarget().properties.add(s);
 				Configuration.getInstance().getTarget().prefixes.put(PrefixHelper.getBase(s), PrefixHelper.getURI(PrefixHelper.getBase(s)));
 				Configuration.getInstance().getTarget().functions.put(s, "");
 			}*/
 			// run selfconfig
 			l.removeAllComponents();
 			Refresher refresher = new Refresher();
 			SelfConfigRefreshListener listener = new SelfConfigRefreshListener();
 			refresher.addListener(listener);
 			addComponent(refresher);
 
 			final ProgressIndicator indicator = new ProgressIndicator();
 			indicator.setCaption("Progress");
 			l.addComponent(indicator);
 			indicator.setImmediate(true);
 
 			final Panel stepPanel = new Panel("Starting self configuration");
 			l.addComponent(stepPanel);
 
 			new Thread() {
 				public void run() {
 
 					float steps = 5f;
 					indicator.setValue(new Float(1f/steps));
 					indicator.requestRepaint();
 					stepPanel.setCaption("Getting source cache...");
 					HybridCache sourceCache = HybridCache.getData(Configuration.getInstance().getSource());
 					indicator.setValue(new Float(2f/steps));
 					indicator.requestRepaint();
 					stepPanel.setCaption("Getting target cache...");
 					HybridCache targetCache = HybridCache.getData(Configuration.getInstance().getTarget());
 					indicator.setValue(new Float(3f/steps));
 					stepPanel.setCaption("Performing self configuration...");
 					MeshBasedSelfConfigurator bsc = new MeshBasedSelfConfigurator(sourceCache, targetCache, 0.6, 0.5);
 					List<SimpleClassifier> cp = bsc.getBestInitialClassifiers();						
 					indicator.setValue(new Float(4f/steps));
 					stepPanel.setCaption("Performed self configuration:");
 					for(SimpleClassifier cl : cp) {
 						System.out.println(cl);
 					}
 
 				}
 			}.start();
 		}			
 	}
 
 /**Listener for the selfconfig button*/
 public class SelfConfigRefreshListener implements RefreshListener
 	{
 		boolean running = true; 
 		private static final long serialVersionUID = -8765221895426102605L;		    
 		@Override 
 		public void refresh(final Refresher source)	{
 			if(!running) {
 				removeComponent(source);
 				source.setEnabled(false);
 			}
 		}
 	}
 }
 
 class AccordionLayoutClickListener implements LayoutClickListener{
 
 	private static final long serialVersionUID = -3498649095113131161L;
 	private Cytographer cytographer;
 	private CyNetworkView cyNetworkView;
 	private GraphProperties.Shape shape;
 	
 	public AccordionLayoutClickListener(Cytographer cytographer, CyNetworkView cyNetworkView,GraphProperties.Shape shape){
 		this.cytographer = cytographer;
 		this.cyNetworkView = cyNetworkView;
 		this.shape = shape;
 	}
 	
 	@Override
 	public void layoutClick(LayoutClickEvent event) {
 		// its left button
 		if(event.getButtonName().equalsIgnoreCase("left")){
 			if(shape.equals(shape.SOURCE)) {
 				String pref = Configuration.getInstance().getSource().var.replaceAll("\\?", "");
 				cytographer.addNode(pref+"."+((Label)event.getClickedComponent()).getValue().toString(), 0, 0, shape);
 				addProperty(((Label)event.getClickedComponent()).getValue().toString(), Configuration.getInstance().getSource());
 			} else if(shape.equals(shape.TARGET)) {
 				String pref = Configuration.getInstance().getTarget().var.replaceAll("\\?", "");
 				cytographer.addNode(pref+"."+((Label)event.getClickedComponent()).getValue().toString(), 0, 0, shape);
 				addProperty(((Label)event.getClickedComponent()).getValue().toString(), Configuration.getInstance().getTarget());
 			} else {
 				cytographer.addNode(((Label)event.getClickedComponent()).getValue().toString(), 0, 0, shape);
 			}
 			cyNetworkView.applyLayout(new ForceDirectedLayout());		
 			cytographer.fitToView();
 			// repaint
 			cytographer.repaintGraph();
 		}
 	}
 	
 	private void addProperty(String s, KBInfo info) {
 		Logger logger = Logger.getLogger("SAIM");
		String prop;
		if(s.startsWith("http:")) {//do not have a prefix, so we generate one
			PrefixHelper.generatePrefix(s);
			prop = PrefixHelper.abbreviate(s);
		} else {// have the prefix already
			prop = s;
			s = PrefixHelper.expand(s);
		}
 		
		info.properties.add(prop);
		info.functions.put(prop, "");
				
 		String base = PrefixHelper.getBase(s);
 		info.prefixes.put(PrefixHelper.getPrefix(base), PrefixHelper.getURI(PrefixHelper.getPrefix(base)));
	
		logger.info(info.var+": adding property: "+prop+" with prefix "+PrefixHelper.getPrefix(base)+" - "+PrefixHelper.getURI(PrefixHelper.getPrefix(base)));
 	}
 }
