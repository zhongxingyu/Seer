 package evaluation.simulator.gui.customElements.configElements;
 
 import java.awt.Component;
 import java.awt.Dimension;
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
 
 import net.miginfocom.swing.MigLayout;
 import evaluation.simulator.annotations.simulationProperty.DoubleProp;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 @SuppressWarnings("serial")
 public class DoubleConfigElement extends JPanel implements ChangeListener, ActionListener, ItemListener, Observer {
 
 	DoubleProp property;
 	JCheckBox auto;
 	JCheckBox unlimited;
 	JSpinner spinner;
 	Component component;
 	
 	SimPropRegistry simPropRegistry;
 	
 	public DoubleConfigElement(DoubleProp property) {
 		
 		simPropRegistry = SimPropRegistry.getInstance();
 		
 		this.property = property;
 		simPropRegistry.registerGuiElement(this, property.getPropertyID());
 		
 		MigLayout migLayout = new MigLayout("","[grow]","");
 		this.setLayout(migLayout);
 		
 		this.spinner = new JSpinner();
 		this.spinner.setModel( new SpinnerNumberModel( (double) property.getValue(),
 				property.getMinValue(),
 				property.getMaxValue(),
 				property.getStepSize()) );
 		this.spinner.addChangeListener( this );
 		this.spinner.setToolTipText(property.getTooltip());
 		this.spinner.setPreferredSize( new Dimension(1,1));
 		((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setColumns(20);
 		this.add( this.spinner, "growx, push, wrap" );
 		this.component = this.spinner;
 				
 		this.setBorder(BorderFactory.createTitledBorder(property.getName()));
 		
 		this.auto = new JCheckBox("AUTO");
 		this.auto.addItemListener( this );
 		this.auto.setToolTipText("Overwrite with AUTO");
 		
 		this.unlimited = new JCheckBox("UNLIMITED");
 		this.unlimited.addItemListener( this );
 		this.unlimited.setToolTipText("Overwrite with AUTO");
 		
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
 			simPropRegistry.setValue(this.property.getPropertyID(), this.spinner.getValue());
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
 			this.simPropRegistry.setAuto(this.property.getPropertyID(), true, Double.class);
 		}else if ( this.unlimited.isSelected() ){
 			this.auto.setEnabled(false);
 			this.component.setEnabled(false);
 			this.simPropRegistry.setUnlimited(this.property.getPropertyID(), true, Double.class);
 		}else{
 			this.unlimited.setEnabled(true);
 			this.auto.setEnabled(true);
 			this.component.setEnabled(true);
 			this.simPropRegistry.setAuto(this.property.getPropertyID(), false, Double.class);
 			this.simPropRegistry.setUnlimited(this.property.getPropertyID(), false, Double.class);
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
 		
 		this.spinner.setValue((Double) simPropRegistry.getValue( property.getPropertyID()).getValue());
		this.spinner.setValue((int) simPropRegistry.getValue(property.getPropertyID()).getValue());
 		
 		updateUI();
 	}
 }
