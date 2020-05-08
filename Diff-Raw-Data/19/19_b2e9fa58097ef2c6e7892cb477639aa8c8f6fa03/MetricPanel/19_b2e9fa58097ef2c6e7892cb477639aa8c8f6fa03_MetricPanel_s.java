 package de.uni_leipzig.simba.saim.gui.widget.panel;
 
 import java.util.Set;
 import java.util.TreeSet;
 import lombok.Getter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import com.vaadin.event.LayoutEvents.LayoutClickEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickListener;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.VerticalLayout;
 import de.konrad.commons.sparql.PrefixHelper;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.SAIMCytoprocess;
 import de.uni_leipzig.simba.saim.SAIMCytoprocessProperties;
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.core.metric.Measure;
 import de.uni_leipzig.simba.saim.core.metric.Node;
 import de.uni_leipzig.simba.saim.core.metric.Operator;
 import de.uni_leipzig.simba.saim.cytoprocess.CytoprocessProperties;
 import de.uni_leipzig.simba.saim.gui.widget.Listener.LearnClickListener;
 import de.uni_leipzig.simba.saim.gui.widget.Listener.SelfConfigClickListener;
 import de.uni_leipzig.simba.saim.gui.widget.Listener.StartMappingListener;
 /** Contains instances of ClassMatchingForm and lays them out vertically.*/
 public class MetricPanel extends Panel{
 
 	final static Logger logger = LoggerFactory.getLogger(MetricPanel.class);
 	private static final long serialVersionUID = 6766679517868840795L;
 
 	@Getter private final Messages messages;
 	@Getter private Configuration config;
 
 	private VerticalLayout sourceLayout, targetLayout;
 	private HorizontalLayout buttonLayout;
 	private Set<String> sourceProps,targetProps;
 	private Button selfConfigButton, learnButton, startMapping;
 
 	@Getter private SAIMCytoprocess saimcytopro;
 
 	public MetricPanel(final Messages messages) {
 		this.messages = messages;
 	}
 
 	@Override
 	public void attach() {
 
 		if((SAIMApplication)getApplication()!= null)
 			config = ((SAIMApplication)getApplication()).getConfig();
 
 		VerticalLayout mainLayout = new VerticalLayout();
 		mainLayout.setSizeFull();
 		
 		mainLayout.setSpacing(false);
 		mainLayout.setMargin(false);
 
 		final VerticalLayout accordionLayout = new VerticalLayout();
 		HorizontalLayout layout = new HorizontalLayout();
 
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
 
 		VerticalLayout metricsLayout, operatorsLayout;
 		sourceLayout =  new VerticalLayout();
 		targetLayout =  new VerticalLayout();
 		metricsLayout =  new VerticalLayout();
 		operatorsLayout =  new VerticalLayout();
 
 		accordion.addTab(sourceLayout,messages.getString("MetricPanel.sourceproperties")); //$NON-NLS-1$
 		accordion.addTab(targetLayout,messages.getString("MetricPanel.targetproperties")); //$NON-NLS-1$
 		accordion.addTab(metricsLayout,messages.getString("MetricPanel.metrics"));  //$NON-NLS-1$
 		accordion.addTab(operatorsLayout,messages.getString("MetricPanel.operators"));	 //$NON-NLS-1$
 		// add Cytographer
 		saimcytopro = makeCytographer();
 		layout.addComponent(saimcytopro);
 
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
 
 //		metricsLayout.addComponent( new Label(messages.getString("MetricPanel.0")));
 //		operatorsLayout.addComponent( new Label(messages.getString("MetricPanel.8")));
 		Set<String> sorted = new TreeSet<String>();
 		sorted.addAll( Measure.identifiers);
		for(String label : sorted)
			metricsLayout.addComponent( new Label(label));
 
 		sorted.clear();
 		sorted.addAll(Operator.identifiers);
		for(String label : sorted)
			operatorsLayout.addComponent( new Label(label));
 
 		sourceLayout.addListener(   new AccordionLayoutClickListener(saimcytopro, SAIMCytoprocess.NODE_TYPE.SOURCE,   config));
 		targetLayout.addListener(   new AccordionLayoutClickListener(saimcytopro, SAIMCytoprocess.NODE_TYPE.TARGET,   config));
 		metricsLayout.addListener(  new AccordionLayoutClickListener(saimcytopro, SAIMCytoprocess.NODE_TYPE.MEASURE,  config));
 		operatorsLayout.addListener(new AccordionLayoutClickListener(saimcytopro, SAIMCytoprocess.NODE_TYPE.OPERATOR, config));
 
 		this.checkButtons();
 	}
 
 	private SAIMCytoprocess makeCytographer(){
 
 		int hadjust = 100;
 		int wadjust = 350;
 
 		int wm = Math.round(((SAIMApplication)getApplication()).getMainWindow().getWidth());
 		int hm = Math.round(((SAIMApplication)getApplication()).getMainWindow().getHeight());
 
 		final int HEIGHT = hm > hadjust ? hm - hadjust : 600;
 		final int WIDTH  = wm > wadjust ? wm - wadjust : 900;
 
 		saimcytopro  = new SAIMCytoprocess(WIDTH, HEIGHT, (SAIMApplication)getApplication());
 
 		CytoprocessProperties.defaults();
 
 		saimcytopro.loadMetricExpression();
 
 		return saimcytopro;
 	}
 
 	private void getAllProps() {
 		sourceProps = new TreeSet<String>();
 		targetProps = new TreeSet<String>();
 //		Configuration config = Configuration.getInstance();
 //		if(config.isLocal) {
 //			logger.info("Local data - using specified properties");
 			if(config != null)
 			if( config.getSource() != null && config.getSource().properties != null && config.getSource().properties.size()>0 &&
 					config.getTarget() != null && config.getTarget().properties != null && config.getTarget().properties.size()>0) {
 				for(String prop : config.getSource().properties) {
 					if(prop.trim().length()>0) {
 						String s_abr=PrefixHelper.abbreviate(prop);
 						if(!sourceProps.contains(s_abr))
 							sourceProps.add(s_abr);
 					}
 				}
 
 				for(String prop : config.getTarget().properties) {
 					if(prop.trim().length()>0) {
 						String s_abr=PrefixHelper.abbreviate(prop);
 						if(!targetProps.contains(s_abr))
 							targetProps.add(s_abr);
 					}
 				}
 				selfConfigButton.setEnabled(true);
 			}
 	}
 
 	/**
 	 * Creates the button is the lower window.
 	 * @return
 	 */
 	public Layout getButtonLayout() {
 
 //		setMetric = new Button(messages.getString("MetricPanel.setmetricbutton"));
 //		setMetric.setEnabled(true);
 //		setMetric.addListener(new ClickListener() {
 //			 /**
 //			 *
 //			 */
 //			private static final long serialVersionUID = 2349781868228639555L;
 //
 //			@Override
 //			public void buttonClick(ClickEvent event) {
 //				setMetricFromGraph();
 //			}
 //		});
 
 		selfConfigButton = new Button(messages.getString("MetricPanel.startselfconfigbutton")); //$NON-NLS-1$
 		selfConfigButton.setEnabled(false);
 		selfConfigButton.addListener(new SelfConfigClickListener((SAIMApplication) getApplication(), messages));
 
 		learnButton = new Button(messages.getString("MetricPanel.learnmetricbutton")); //$NON-NLS-1$
 		learnButton.setEnabled(true);
 		learnButton.addListener(new LearnClickListener((SAIMApplication) getApplication(), messages,this));
 
 		startMapping = new Button(messages.getString("MetricPanel.startmappingbutton")); //$NON-NLS-1$
 		startMapping.setEnabled(true);
 		startMapping.addListener(new StartMappingListener((SAIMApplication) getApplication(), messages,this));
 
 		buttonLayout = new HorizontalLayout();
 		//buttonLayout.addComponent(setMetric);
 		buttonLayout.addComponent(selfConfigButton);
 		buttonLayout.addComponent(learnButton);
 		buttonLayout.addComponent(startMapping);
 		return buttonLayout;
 	}
 
 	/**
 	 * Method to set Metric from the graph.
 	 */
 	public boolean setMetricFromGraph() {
 		if(!saimcytopro.getMetric().isComplete()) {
 			getApplication().getMainWindow().showNotification(messages.getString("MetricPanel.settingnotablenotcomplete")); //$NON-NLS-1$
 			return false;
 		} else {
 			Node node =  saimcytopro.getMetric();
 			String expr = saimcytopro.getMetric().toString();
 			config.setMetricExpression(expr);
 			System.out.println(node.param1+" - "+node.param2); //$NON-NLS-1$
 			if(node.param1 != null && node.param2 != null) {
 				config.setAcceptanceThreshold(node.param1);
 				config.setVerificationThreshold(node.param2);
 				getApplication().getMainWindow().showNotification("Setting: "+expr+ "with thresholds "+node.param1+" / "+node.param2); //$NON-NLS-1$
 				return checkButtons();
 			}
 			else {
 				getApplication().getMainWindow().showNotification(messages.getString("MetricPanel.settingnotablenothreholds")); //$NON-NLS-1$
 				return false;
 			}
 		}
 	}
 
 	/**
 	 * Checks whether the Buttons (selfconfig, learning and startMapping) could be activated.
 	 */
 	public boolean checkButtons() {
 		if((SAIMApplication)getApplication()!=null) {
 			Configuration config = ((SAIMApplication)getApplication()).getConfig();
 			if( config.getSource() != null && config.getSource().properties != null && config.getSource().properties.size()>0 &&
 					config.getTarget() != null && config.getTarget().properties != null && config.getTarget().properties.size()>0) {
 				selfConfigButton.setEnabled(true);
 				if(config.getMetricExpression() != null && config.getMetricExpression().length()>0) {
 					learnButton.setEnabled(true);
 					startMapping.setEnabled(true);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**Listener to react on clicks in the accordion panel.*/
 	class AccordionLayoutClickListener implements LayoutClickListener{
 		private static final long serialVersionUID = -3498649095113131161L;
 
 		private SAIMCytoprocess saimcytoprocess;
 		private SAIMCytoprocess.NODE_TYPE shape;
 		private Configuration config;
 
 		public AccordionLayoutClickListener(SAIMCytoprocess cytographer,SAIMCytoprocess.NODE_TYPE shape, Configuration config){
 			this.saimcytoprocess = cytographer;
 			this.shape = shape;
 			this.config = config;
 		}
 
 		@Override
 		public void layoutClick(LayoutClickEvent event) {
 			// its left button
 			if(event.getButtonName().equalsIgnoreCase("left") && event.getClickedComponent() instanceof Label ){ //$NON-NLS-1$
 				String labelValue = ((Label)event.getClickedComponent()).getValue().toString();
 
 				switch(shape){
 					case SOURCE: {
 						String pref = config.getSource().var.replaceAll("\\?", ""); //$NON-NLS-1$
 						saimcytoprocess.addNode(pref+"."+labelValue, SAIMCytoprocess.NODE_TYPE.SOURCE); //$NON-NLS-1$
 						break;
 					}
 					case TARGET: {
 						String pref = config.getTarget().var.replaceAll("\\?", ""); //$NON-NLS-1$
 						saimcytoprocess.addNode(pref+"."+labelValue, SAIMCytoprocess.NODE_TYPE.TARGET); //$NON-NLS-1$
 						break;
 					}
 					case OPERATOR:
 						double t1 = Double.parseDouble(SAIMCytoprocessProperties.getProperty(SAIMCytoprocessProperties.OPERATOR_DEFAULT_THRESHOLD_1));
 						double t2 = Double.parseDouble(SAIMCytoprocessProperties.getProperty(SAIMCytoprocessProperties.OPERATOR_DEFAULT_THRESHOLD_2));
 						saimcytoprocess.addNode(labelValue, SAIMCytoprocess.NODE_TYPE.OPERATOR, t1, t2);
 						break;
 
 					case MEASURE:
 						saimcytoprocess.addNode(labelValue, SAIMCytoprocess.NODE_TYPE.MEASURE);
 					}
 				// repaint
 				saimcytoprocess.repaintGraph();
 			}
 		}
 	} // end of class AccordionClickListener
 }// end of MetricPanel
