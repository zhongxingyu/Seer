 /*
  * Last modification information:
 * $Revision: 1.1 $
 * $Date: 2007-09-26 00:00:18 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2007 The Concord Consortium 
 */
 package org.concord.data.state.ui;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.concord.data.state.OTUnitValue;
 
 
 /**
  * UnitEditPanel
  * 
  * This panel allows the user to change units for a value unit object
  * It can either show a list of possible units and let user choose,
  * or, if the user already selected a unit, just ask for the conversion 
  * rate and calculate the new value
  *
  * Date created: Sep 25, 2007
  *
  * @author Ingrid Moncada<p>
  *
  */
 public class UnitEditPanel extends JPanel 
 	implements ActionListener
 {
 	/**
 	 * Comment for <code>serialVersionUID</code>
 	 */
 	private static final long serialVersionUID = -3832240416484085290L;
 
 	protected final String[] messages = {
 			"Current Value: ", 
 			"Change units to: ", 
 			"Conversion rate:", 
			"How many $unit2$ on 1 $unit1$? ", 
 			"New Value: "};
 	
 	protected OTUnitValue value;
 	protected JComboBox unitSelection;
 	protected JTextField conversionValueText;
 	protected JLabel newValueLabel;
 	
 	/** If the user already selected a unit, this panel doesn't give the option to choose */
 	protected String forceNewUnit;
 	
 	protected OTUnitValue newValue;
 	protected String errorMessage;
 	
 	/**
 	 * Creates a panel that allows the user to change the unit of a value
 	 * It shows a list of possible units to choose from and it asks for a conversion rate
 	 * 
 	 * @param value
 	 */
 	public UnitEditPanel(OTUnitValue value)
 	{
 		this(value, null);
 	}
 	
 	/**
 	 * Creates a panel that asks for details to convert the unit of a value
 	 * If the new unit passed if not null, it means that the user already selected
 	 * a unit and this panel won't show the list of possible units to choose from.
 	 * It will only ask for the conversion value
 	 * 
 	 * @param value
 	 * @param newUnit
 	 */
 	public UnitEditPanel(OTUnitValue value, String newUnit)
 	{
 		super();
 		
 		this.forceNewUnit = newUnit;
 		this.value = value;
 		
 		try{
 			newValue = (OTUnitValue)value.getOTObjectService().copyObject(value, 0);
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		//Show the whole dialog if the user hasn't already selected a unit
 		setupView(forceNewUnit == null);
 	}
 	
 	protected void setupView(boolean showFullDialog)
 	{		
 		String strMessage;
 		
 		if (showFullDialog){
 			setLayout(new GridLayout(4,2));
 			
 			//Old value
 			strMessage = messages[0];
 			add(new JLabel(strMessage));
 			
 			add(new JLabel(value.getValue() + " " + value.getUnit()));
 			
 			//New units
 			strMessage = messages[1];
 			add(new JLabel(strMessage));
 			setupUnitSelection(value);
 			add(unitSelection);
 		}
 		
 		//Conversion value
 		if (showFullDialog){
 			strMessage = messages[2];
 		}
 		else{
 			strMessage = messages[3];
 			strMessage = strMessage.replaceAll("\\$unit1\\$", value.getUnit());
 			strMessage = strMessage.replaceAll("\\$unit2\\$", forceNewUnit);
 		}
 		add(new JLabel(strMessage));
 		conversionValueText = new JTextField(4);
 		conversionValueText.addActionListener(this);
 		add(conversionValueText);
 		
 		if (showFullDialog){
 			//New value
 			strMessage = messages[4];
 			add(new JLabel(strMessage));
 			newValueLabel = new JLabel();
 			add(newValueLabel);
 		}		
 	}	
 	
 	protected void resetNewValue()
 	{
 		newValue.setUnit(value.getUnit());
 		newValue.setValue(value.getValue());
 	}
 	
 	protected void calculateNewValue()
 	{
 		float val = value.getValue();
 		try{
 			val = val * Float.parseFloat(conversionValueText.getText());
 		}
 		catch (Exception ex) {
 			//ex.printStackTrace();
 			errorMessage = "Value "+conversionValueText.getText()+" not valid.";
 			resetNewValue();
 			return;
 		}
 		
 		if (forceNewUnit != null){
 			newValue.setUnit(forceNewUnit);
 		}
 		else{
 			newValue.setUnit((String)unitSelection.getSelectedItem());
 		}
 		newValue.setValue(val);
 		
 		if (newValueLabel != null){
 			newValueLabel.setText(newValue.getValue() + " " + newValue.getUnit());
 		}
 	}
 	
 	protected void setupUnitSelection(OTUnitValue value)
 	{
 		unitSelection = new JComboBox();
 		unitSelection.setOpaque(false);
 		unitSelection.addItem(value.getUnit());
 		unitSelection.addItem("m");
 		unitSelection.addItem("kg");
 		unitSelection.addItem("g");
 		
 		unitSelection.addActionListener(this);
 	}
 
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		calculateNewValue();
 	}
 	
 	public OTUnitValue getNewValue()
 	{
 		calculateNewValue();
 		return newValue;
 	}
 	
 	public String getErrorMessage()
 	{
 		return errorMessage;
 	}
 }
