 package evaluation.simulator.gui.customElements;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import evaluation.simulator.annotations.property.BoolProp;
 import evaluation.simulator.annotations.property.DoubleProp;
 import evaluation.simulator.annotations.property.FloatProp;
 import evaluation.simulator.annotations.property.IntProp;
 import evaluation.simulator.annotations.property.SimProp;
 import evaluation.simulator.annotations.property.StringProp;
 import evaluation.simulator.gui.customElements.accordion.AccordionEntry;
 import evaluation.simulator.gui.customElements.configElements.BoolConfigElement;
 import evaluation.simulator.gui.customElements.configElements.DoubleConfigElement;
 import evaluation.simulator.gui.customElements.configElements.FloatConfigElement;
 import evaluation.simulator.gui.customElements.configElements.IntConfigElement;
 import evaluation.simulator.gui.customElements.configElements.StringConfigElement;
 import evaluation.simulator.gui.customElements.structure.HelpPropValues;
 import evaluation.simulator.gui.pluginRegistry.DependencyChecker;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 import net.miginfocom.swing.MigLayout;
 
 public class PropVaryElement extends JPanel{
 	
 	private static Logger logger = Logger.getLogger(PluginPanel.class);
 	
 	private JComboBox<String> cBox[];	
 	private JTextField propElement[];
 	private HelpPropValues value[];
 	private Class propType[];
 	private final int numOfPropsToVary = 2;
 		
 	Map<String, SimProp> propMap;
 	Map<JComboBox<String>, Integer> boxToIndexMap;
 	Map<JTextField, Integer> propToIndexMap;
 	
 	public PropVaryElement() {
 		 
 		this.setLayout(new MigLayout());
 		
 		loadContent();
 	}
 	
 	private void loadContent(){		
 				
 		this.propMap = SimPropRegistry.getInstance().getProperties();		
 		
 		String propertyStrings[] = propMap.keySet().toArray(new String[1]);
 		propertyStrings[0] = "---";
 		cBox = new JComboBox[numOfPropsToVary];
 		cBox[0] = new JComboBox<String>(propertyStrings);
 		cBox[1] = new JComboBox<String>(propertyStrings);		
 		addBoxListener(cBox[0]);
 		addBoxListener(cBox[1]);		
 		
 		propElement = new JTextField[numOfPropsToVary];
 		propElement[0] = new JTextField();		
 		propElement[1] = new JTextField();
 		addTextListener(propElement[0]);
 		addTextListener(propElement[1]);
 		
 		value = new HelpPropValues[numOfPropsToVary];
 		value[0] = null;
 		value[1] = null;
 		
 		propType = new Class[numOfPropsToVary];
 		
 		this.boxToIndexMap = new HashMap<>();
 		this.boxToIndexMap.put(cBox[0], 0);
 		this.boxToIndexMap.put(cBox[1], 1);
 		
 		this.propToIndexMap = new HashMap<>();
 		this.propToIndexMap.put(propElement[0], 0);
 		this.propToIndexMap.put(propElement[1], 1);
 
 					
 		this.add( cBox[0], "wrap");
 		this.add(propElement[0], "wrap, growx");		
 		this.add( cBox[1], "wrap");
 		this.add(propElement[1], "wrap, growx");
 		
 		comboboxChanged(cBox[0]);
 		comboboxChanged(cBox[1]);	
 		
 	}	
 
 	private void comboboxChanged(JComboBox<String> ComboBox){
 		
 		int index = this.boxToIndexMap.get(ComboBox);
 		JTextField currentElement = propElement[index];
 		
 		value[index]=null;
 		currentElement.setText("");
 		
 		String currentItem = (String) ComboBox.getSelectedItem();
 		if(currentItem == "---"){
 			propElement[index].setEnabled(false);
 			propType[index] = null;
 			if(index == 0){				
 				this.cBox[1].setEnabled(false);
 				this.propElement[1].setEnabled(false);
 			}			
 		}
 		else{			
 			propElement[index].setEnabled(true);
 			propType[index] = propMap.get(currentItem).getValueType();
 			logger.log(Level.DEBUG, "Proptype is set to" + propType[index].toString());
 		}
 		
 		if((ComboBox == cBox[0])&&(currentItem != "---")){
 			this.cBox[1].setEnabled(true);
 		}
 		
 		logger.log(Level.DEBUG, currentElement.toString());		
 		this.repaint();
 	}
 	
 	private void addBoxListener(final JComboBox<String> property) {
 		ItemListener il = new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 
 				if (e.getStateChange() == ItemEvent.SELECTED && property.isVisible()) {
 					PropVaryElement.this.comboboxChanged(property);
 				}
 			}
 		};
 		property.addItemListener(il);		
 	}
 	
 	private void addTextListener(final JTextField field) {
 		ActionListener al = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent a) {
 
 				if (a.getActionCommand() != null ) {
 					int i= PropVaryElement.this.propToIndexMap.get(field);
 					value[i] = new HelpPropValues(a.getActionCommand().toString(),propType[i]);
					logger.log(Level.DEBUG, "PropertyToVary set to: " + value[i].getType().toString());
 					logger.log(Level.DEBUG, "Validity is: " + value[i].isValid());
 				}
 			}
 			
 		};
 		field.addActionListener(al);
 	}
 	
 //	public int getMin (int i){
 //		return 0;
 //	}
 //	
 //	public int getMax (int i){
 //		return 0;
 //	}
 //	
 //	public List<Object> getValues (int i){
 //		return null;
 //	}
 	
 }
