 package de.uni_leipzig.simba.saim.gui.widget;
 
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.terminal.UserError;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.TabSheet.Tab;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 import de.konrad.commons.sparql.PrefixHelper;
 import de.konrad.commons.sparql.SPARQLHelper;
 import de.uni_leipzig.simba.data.Mapping;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.learning.query.PropertyMapper;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.core.Configuration;
 /** Contains instances of ClassMatchingForm and lays them out vertically.*/
 public class MetricPanel extends Panel
 {	
 	
 	ManualMetricForm manualMetricForm;
 	private static final long	serialVersionUID	= 6766679517868840795L;
 //	TextField metricTextField = new TextField("Insert metric here");
 	Mapping propMapping;
 	HorizontalLayout layout = new HorizontalLayout();
 	Set<String> sourceProps = new HashSet<String>();
 	Set<String> targetProps = new HashSet<String>();
 	//	protected void setupContextHelp()
 	//	{
 	//		ContextHelp contextHelp = new ContextHelp();
 	//		getContent().addComponent(contextHelp);
 	//		//contextHelp.addHelpForComponent(proposals, "Source properties from the knowledgebase <insert id here> ....");
 	//	}
 
 	public MetricPanel()
 	{
 		layout.setSpacing(false);
 		layout.setMargin(false);
 		setContent(layout);
 		layout.addComponent(manualMetricForm=new ManualMetricForm());
 		final VerticalLayout accordionLayout = new VerticalLayout();
 		addComponent(accordionLayout);		
 		final ProgressIndicator progress = new ProgressIndicator();
 		progress.setIndeterminate(false);
 		accordionLayout.addComponent(progress);
 		Panel accordionPanel = new Panel();
 		accordionLayout.addComponent(accordionPanel);
 		//accordionPanel.setVisible(false);
 		accordionPanel.setWidth("40em"); //$NON-NLS-1$
 		
 		Panel graphPanel = new Panel();		
 		addComponent(graphPanel);
 		final Accordion accordion = new Accordion();		
 		accordionPanel.addComponent(accordion);
 
 		final VerticalLayout sourceLayout = new VerticalLayout();
 		Tab sourceTab = accordion.addTab(sourceLayout,Messages.getString("MetricPanel.sourceproperties")); //$NON-NLS-1$
 
 		final VerticalLayout targetLayout = new VerticalLayout();
 		{Tab targetTab = accordion.addTab(targetLayout,Messages.getString("MetricPanel.targetproperties"));} //$NON-NLS-1$}
 
 		{
 			VerticalLayout functionLayout = new VerticalLayout();
 			Tab functionTab = accordion.addTab(functionLayout,Messages.getString("MetricPanel.functions")); //$NON-NLS-1$
 		}
 		{
 			VerticalLayout metricLayout = new VerticalLayout();
 			Tab metricTab = accordion.addTab(metricLayout,Messages.getString("MetricPanel.metrics")); //$NON-NLS-1$
 		}
 		{
 			VerticalLayout operatorLayout = new VerticalLayout();
 			Tab operatorTab = accordion.addTab(operatorLayout,Messages.getString("MetricPanel.operators"));			 //$NON-NLS-1$
 		}
 
 		new Thread()
 		{			
 			@Override
 			public void run()
 			{
 			//	performPropertyMapping();
 				getAllProps();
 				{
 					for(String s : sourceProps) {
 						System.out.println("Addinge check for "+s);
 						final CheckBox check = new CheckBox(s, false);
 					//	check.setCaption(s);
 						check.addListener(new Property.ValueChangeListener() {							
 							@Override
 							public void valueChange(ValueChangeEvent event) {
 								String prop = check.getCaption();
 								if(check.booleanValue() && prop != null && prop.length()>0) {
 									String s_abr=PrefixHelper.abbreviate(prop);
 									sourceProps.add(s_abr);
 									Configuration.getInstance().getSource().properties.add(s_abr);
 									Configuration.getInstance().getSource().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 									Configuration.getInstance().getSource().functions.put(s_abr, "lowercase");
 									System.out.println("Adding source property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 								} else {
 									System.out.println("Tried to add a source false property");
 								}
 							}
 						});
 						sourceLayout.addComponent(check); //$NON-NLS-1$
 					}
 
 					for(String t : targetProps) {
 						System.out.println("Addinge check for "+t);
 						final CheckBox check = new CheckBox(t, false);
 						check.setEnabled(true);
 						check.addListener(new Property.ValueChangeListener() {							
 							@Override
 							public void valueChange(ValueChangeEvent event) {
 								String prop = check.getCaption();
 								if(check.booleanValue() && prop != null && prop.length()>0) {
 									String s_abr=PrefixHelper.abbreviate(prop);
 									Configuration.getInstance().getTarget().properties.add(s_abr);
 									Configuration.getInstance().getTarget().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 									Configuration.getInstance().getTarget().functions.put(s_abr, "lowercase");
 									System.out.println("Adding target property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 								} else {
 									System.out.println("Tried to add a target false property");
 								}
 							}
 						});
 						targetLayout.addComponent(check);//$NON-NLS-1$
 					} 
 				}
 				accordionLayout.removeComponent(progress);
 				progress.setEnabled(false);
 			}
 		}.start();
 
 		//		{
 		//			//		setupContextHelp();
 		//		}
 		}
 
 		private void performPropertyMapping() {
 			Configuration config = Configuration.getInstance();
 			config.getSource().properties.clear();
 			config.getTarget().properties.clear();
 			PropertyMapper propMapper = new PropertyMapper();
 			String classSource = getClassOfEndpoint(config.getSource());
 			String classTarget = getClassOfEndpoint(config.getTarget());
 			if(classSource != null && classTarget != null) {
 				showErrorMessage("Getting property mapping...");
 				propMapping = propMapper.getPropertyMapping(config.getSource().endpoint,
 						config.getTarget().endpoint, classSource, classTarget);
 				for(String s : propMapping.map.keySet())
 					for(Entry<String, Double> e : propMapping.map.get(s).entrySet()) {
 						System.out.println(s + " - " + e.getKey());
 						String s_abr=PrefixHelper.abbreviate(s);
 						sourceProps.add(s_abr);
 						config.getSource().properties.add(s_abr);
 						config.getSource().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 						System.out.println("Adding source property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 						targetProps.add(PrefixHelper.abbreviate(e.getKey()));
 						String t_abr=PrefixHelper.abbreviate(e.getKey());
 						config.getTarget().properties.add(t_abr);
 						config.getTarget().prefixes.put(PrefixHelper.getPrefixFromURI(t_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(t_abr)));
 						System.out.println("Adding target property: "+t_abr+"::::"+PrefixHelper.getPrefixFromURI(t_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(t_abr)));
 					}
 			} else {
 				showErrorMessage("Cannot perform automatic property mapping due to missing class specifications.");
 			}		
 		}
 		
 		private void getAllProps() {
 			//for source
 			KBInfo info = Configuration.getInstance().getSource();
 			String className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
 			for(String prop : SPARQLHelper.properties(info.endpoint, info.graph, className)) {
 				String s_abr=PrefixHelper.abbreviate(prop);
 				sourceProps.add(s_abr);
 //				Configuration.getInstance().getSource().properties.add(s_abr);
 //				 Configuration.getInstance().getSource().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 //				System.out.println("Adding source property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 			}
 			//for target
 			info = Configuration.getInstance().getTarget();
 			className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
 			for(String prop : SPARQLHelper.properties(info.endpoint, info.graph, className)) {
 				String s_abr=PrefixHelper.abbreviate(prop);
 				targetProps.add(s_abr);
 //				Configuration.getInstance().getTarget().properties.add(s_abr);
 //				Configuration.getInstance().getTarget().prefixes.put(PrefixHelper.getPrefixFromURI(s_abr), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 //				System.out.println("Adding source property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 			}	
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
 
 		public boolean isValid() {
 			manualMetricForm.validate();
 			if(manualMetricForm.isValid()) {
 				Configuration.getInstance().setMetricExpression(manualMetricForm.metricTextField.getValue().toString());
 				Configuration.getInstance().setAcceptanceThreshold(Double.parseDouble(manualMetricForm.thresholdTextField.getValue().toString()));
 				return true;
 			}
 			return false;
 		}
 	}
