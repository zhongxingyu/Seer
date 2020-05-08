 package evaluation.simulator.gui.customElements.accordion;
 
 import java.awt.Component;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import evaluation.simulator.annotations.simulationProperty.BoolProp;
 import evaluation.simulator.annotations.simulationProperty.DoubleProp;
 import evaluation.simulator.annotations.simulationProperty.FloatProp;
 import evaluation.simulator.annotations.simulationProperty.IntProp;
 import evaluation.simulator.annotations.simulationProperty.SimProp;
 import evaluation.simulator.annotations.simulationProperty.StringProp;
 import evaluation.simulator.gui.customElements.configElements.BoolConfigElement;
 import evaluation.simulator.gui.customElements.configElements.DoubleConfigElement;
 import evaluation.simulator.gui.customElements.configElements.FloatConfigElement;
 import evaluation.simulator.gui.customElements.configElements.IntConfigElement;
 import evaluation.simulator.gui.customElements.configElements.StringConfigElement;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 @SuppressWarnings("serial")
 public class PropertyPanel extends JPanel {
 
 	String localName;
 	
 	public PropertyPanel( String name ) {
 		super();
 		this.localName = name;
 		
 		MigLayout migLayout = new MigLayout("","[grow]","[grow]");
 		this.setLayout(migLayout);
 		
 		loadContent();
 	}
 	
 	private void loadContent(){
 		
 		// Select all global SimProps for the given plugin layer
 		SimPropRegistry simPropRegistry = SimPropRegistry.getInstance();
 		String pluginLayer = this.localName;
 		List<SimProp> tmpListOfAllVisibleSimProperties = simPropRegistry.getGlobalSimPropertiesByPluginLayer(pluginLayer);
 		
 		// add content
 		for (SimProp simProp : tmpListOfAllVisibleSimProperties ){
 			if ( simProp.getValueType() == Integer.class ){
 				this.add( new IntConfigElement((IntProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == Float.class ){
 				this.add( new FloatConfigElement((FloatProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == Double.class ){
 				this.add( new DoubleConfigElement((DoubleProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == Boolean.class ){
 				this.add( new BoolConfigElement((BoolProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == String.class ){
 				this.add( new StringConfigElement((StringProp) simProp), "growx, wrap");
 			}
 		}
 	}
 	
 	public void realoadContent(String pluginName){
 		for ( Component c : this.getComponents()){
 			this.remove(c);
 		}
 		
 		SimPropRegistry simPropRegistry = SimPropRegistry.getInstance();
 		List<SimProp> tmpListOfAllVisibleSimProperties = simPropRegistry.getSimPropertiesByPluginOrPluginLayer(pluginName, this.localName);
 		
 		for (SimProp simProp : tmpListOfAllVisibleSimProperties ){
 			if ( simProp.getValueType() == Integer.class ){
 				this.add( new IntConfigElement((IntProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == Float.class ){
 				this.add( new FloatConfigElement((FloatProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == Double.class ){
 				this.add( new DoubleConfigElement((DoubleProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == Boolean.class ){
 				this.add( new BoolConfigElement((BoolProp) simProp), "growx, wrap");
 			}
 			if ( simProp.getValueType() == String.class ){
 				this.add( new StringConfigElement((StringProp) simProp), "growx, wrap");
 			}
 		}
 		
 //		loadContent();
 	}
 
 }
