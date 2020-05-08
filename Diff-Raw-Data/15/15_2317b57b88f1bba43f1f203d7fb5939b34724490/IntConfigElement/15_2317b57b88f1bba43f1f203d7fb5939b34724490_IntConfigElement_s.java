 package evaluation.simulator.gui.customElements.configElements;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import evaluation.simulator.annotations.simulationProperty.IntProp;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 import net.miginfocom.swing.MigLayout;
 
 @SuppressWarnings("serial")
 public class IntConfigElement extends JPanel implements ChangeListener, ActionListener, ItemListener {
 
 	IntProp property;
 	JCheckBox auto;
 	JCheckBox unlimited;
 	JSpinner spinner;
 	JSlider slider;
 	Component component;
 	
 	SimPropRegistry simPropRegistry;
 	
 	public IntConfigElement(IntProp property) {
 		
 		simPropRegistry = SimPropRegistry.getInstance();
 		
 		this.property = property;
 		
 		MigLayout migLayout = new MigLayout("","[grow]","");
 		this.setLayout(migLayout);
 		
 		if ( property.getGuiElement().equals("slider") ) {
 			this.slider = new JSlider(this.property.getMinValue(), 
 					this.property.getMaxValue(), 
 					(int) this.property.getValue());
 			this.slider.addChangeListener( this );
 			this.add( this.slider, "growx, push, wrap" );
 			this.component = this.slider;
 		}else{ 
 			this.spinner = new JSpinner();
 			this.spinner.setModel( new SpinnerNumberModel( (int) property.getValue(),
 					property.getMinValue(),
 					property.getMaxValue(),
 					property.getStepSize()) );
 			this.spinner.addChangeListener( this );
 			this.spinner.setToolTipText(property.getTooltip());
 			this.add( this.spinner, "growx, push, wrap" );
 			this.component = this.spinner;
 		}
 		
 		this.setBorder(BorderFactory.createTitledBorder(property.getName()));
 		
 		this.auto = new JCheckBox("AUTO");
 		this.auto.addItemListener( this );
 		this.auto.setToolTipText("Overwrite with AUTO");
 		
 		this.unlimited = new JCheckBox("UNLIMITED");
 		this.unlimited.addItemListener( this );
 		this.unlimited.setToolTipText("Overwrite with UNLIMITED");
 		
 		if (property.getEnableAuto()){
 			this.add(auto, "wrap");
 		}
 		
 		if (property.getEnableUnlimited()){
 			this.add(unlimited, "push");;
 		}
 		
 		if (!property.getInfo().equals("")){
 			JTextArea textarea = new JTextArea("Info: " + property.getInfo());
 			textarea.setEditable(false);
 			textarea.setLineWrap(true);
 			textarea.setWrapStyleWord(true);
 			this.add( textarea, "growx, growy" );
 		}
 		
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent event) {
 		if ( event.getSource() == this.spinner ){
 			simPropRegistry.setValue(this.property.getPropertyID(), this.spinner.getValue());
 		}
 		if ( event.getSource() == this.slider ){
 			simPropRegistry.setValue(this.property.getPropertyID(), this.slider.getValue());
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent event) {
 		if ( this.auto.isSelected() ){
 			this.unlimited.setEnabled(false);
 			this.component.setEnabled(false);
 		}else{
 			this.unlimited.setEnabled(true);
 			this.component.setEnabled(true);
 		}
 		
 //		if ( this.unlimited.isSelected() ){
 //			this.auto.setEnabled(false);
 //			this.component.setEnabled(false);
 //		}else{
 //			this.auto.setEnabled(true);
 //			this.component.setEnabled(true);
 //		}
 	}
 }
