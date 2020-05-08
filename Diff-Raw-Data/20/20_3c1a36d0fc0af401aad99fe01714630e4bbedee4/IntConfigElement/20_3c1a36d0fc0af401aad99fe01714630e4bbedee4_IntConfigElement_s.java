 package evaluation.simulator.gui.customElements.configElements;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Observable;
 import java.util.Observer;
 
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
 public class IntConfigElement extends JPanel implements ChangeListener, ActionListener, ItemListener, Observer {
 
 	IntProp property;
 	JCheckBox auto;
 	JCheckBox unlimited;
 	JSpinner spinner;
 	JSlider slider;
 	Component component;
 	JLabel valueLabel;
 	
 	SimPropRegistry simPropRegistry;
 	
 	public IntConfigElement(IntProp property) {
 		
 		simPropRegistry = SimPropRegistry.getInstance();
 		
 		this.property = property;
 		simPropRegistry.registerGuiElement(this, property.getPropertyID());
 		
 		// this.property.register(this);
 
 		this.valueLabel = new JLabel();
 		
 		MigLayout migLayout = new MigLayout("","[grow]","");
 		this.setLayout(migLayout);
 		
 		if ( property.getGuiElement().equals("slider") ) {
 			this.slider = new JSlider(this.property.getMinValue(), 
 					this.property.getMaxValue(), 
 					(int) this.property.getValue());
 			this.slider.addChangeListener( this );
 			this.valueLabel.setText( "" + this.property.getValue() + " (ms)" );
 			this.add( this.valueLabel, "growx, push, wrap" );
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
 		
 		this.auto.setSelected(property.getAuto());
 		this.unlimited.setSelected(property.getUnlimited());
 		
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
 			System.err.println("CHANGED EVENT");
 			simPropRegistry.setValue(this.property.getPropertyID(), this.spinner.getValue());
 		}
 		if ( event.getSource() == this.slider ){
 			this.valueLabel.setText( "" + this.property.getValue() + " (ms)");
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
 			this.simPropRegistry.setAuto(this.property.getPropertyID(), true, Integer.class);
 		}else if ( this.unlimited.isSelected() ){
 			this.auto.setEnabled(false);
 			this.component.setEnabled(false);
 			this.simPropRegistry.setUnlimited(this.property.getPropertyID(), true, Integer.class);
 		}else{
 			this.unlimited.setEnabled(true);
 			this.auto.setEnabled(true);
 			this.component.setEnabled(true);
 			this.simPropRegistry.setAuto(this.property.getPropertyID(), false, Integer.class);
 			this.simPropRegistry.setUnlimited(this.property.getPropertyID(), false, Integer.class);
 		}
 	}
 
 	// Called when simporp has changed
 	@Override
 	public void update(Observable observable, Object o) {
 		
 		this.auto.setSelected(property.getAuto());
 		this.unlimited.setSelected(property.getUnlimited());
 		
 		if ( (boolean)o ){
 			this.component.setEnabled(true);
 			this.unlimited.setEnabled(true);
 			this.auto.setEnabled(true);
 		} else {
 			this.component.setEnabled(false);
 			this.unlimited.setEnabled(false);
 			this.auto.setEnabled(false);
 		}
 		
		if ( property.getGuiElement().equals("slider") ) {
			this.slider.setValue((int) simPropRegistry.getValue( property.getPropertyID()).getValue());
 		}else{
			this.spinner.setValue((int) simPropRegistry.getValue( property.getPropertyID()).getValue());
 		}
 		
 		updateUI();
 	}
 }
