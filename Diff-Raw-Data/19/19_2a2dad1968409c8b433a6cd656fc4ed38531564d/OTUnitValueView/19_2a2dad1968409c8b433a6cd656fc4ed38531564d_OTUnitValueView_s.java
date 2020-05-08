 /*
  * Last modification information:
 * $Revision: 1.14 $
 * $Date: 2007-09-26 00:00:18 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
  */
 package org.concord.data.state;
 
 import java.awt.BorderLayout;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.OTJComponentView;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewEntryAware;
 
 /**
  * OTUnitValueView
  * Class name and description
  *
  * Date created: March 6, 2007
  *
  * @author Shengyao Wang<p>
  *
  */
 public class OTUnitValueView 
 	implements OTViewEntryAware, OTJComponentView 
 {
 	protected OTUnitValue otObject;
 	protected OTUnitValueViewConfig viewConfig;
 	protected JComponent newComponent;
 	protected boolean editable;
 	
 	protected OTChangeListener otChangeListener; 
 
 	// This looks like it was used before but not anymore
 	// I'm keeping it here because it probably took quite a bit of twiddling to get right.
 	// private String unitValueFormat = "\\d+(\\.?\\d*){0,1}( +\\w*){0,1}";
 	protected DecimalFormat nf;
 
 	private String originalText;
 
 	public JComponent getComponent(OTObject otObject) 
 	{
 		this.otObject = (OTUnitValue)otObject;
 		this.editable = true;
 
 		newComponent = new JPanel();
 		newComponent.setLayout(new BorderLayout());
 
 		createOTListener();
 		this.otObject.addOTChangeListener(otChangeListener);
 
 		int precision = 2;
 		if(viewConfig != null) {
 			precision = viewConfig.getPrecision();
 			editable = viewConfig.getEditable();
 		}
 		nf = createFormatObject(precision);
 		
 		newComponent.add(getUnitValueView());
 		newComponent.setOpaque(false);
 
 		return newComponent;
 	}
 
 	protected JComponent getUnitValueView() 
 	{
 		final JComponent tf;
 
 		String strValue = getStringValue(otObject, nf);
 
 		if(editable) {
 			tf = new JTextField(strValue);
 			((JTextField)tf).setColumns(((JTextField)tf).getText().length());
 			((JTextField)tf).setHorizontalAlignment(JTextField.CENTER);
 			addListeners((JTextField)tf);
 		}
 		else{
 			tf = new JLabel(strValue);
 		}
 
 		if(tf instanceof JTextField) originalText = ((JTextField)tf).getText();
 
 		return tf;
 	}
 
 	private void addListeners(final JTextField tf) 
 	{
 		//mouselistener: listen to mouse click
 		tf.addMouseListener(new MouseAdapter() 
 		{
 			public void mouseClicked(MouseEvent e) {
 				tf.requestFocusInWindow();
 			}
 
 		});
 
 		//focus listener: listen to focus lost
 		tf.addFocusListener(new FocusAdapter() 
 		{
 			public void focusLost(FocusEvent e) {
 				updateFieldAndValue(tf);
 			}
 
 		});
 
 		//key listener: listen to Enter press
 		tf.addKeyListener(new KeyAdapter() 
 		{
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
 					updateFieldAndValue(tf);
 				}
 			}
 
 		});
 	}
 
 	private void updateFieldAndValue(Object obj) 
 	{
 		JTextField tf = (JTextField) obj;
 		String text = tf.getText().trim();
 
 		UnitValueEvaluator uve = new UnitValueEvaluator(text);
 		if(uve.isValidUnitValue()) {
 			otObject.setValue(uve.getValue());
 			otObject.setUnit(uve.getUnit());
 
 			// This shouldn't be necessary because just setting values in the otObject will through
 			// an event
 			// otObject.notifyOTChange(null, null, null);
 		} else {
 			tf.setText(originalText);
 		}
 	}
 
 	public void viewClosed() 
 	{
 		otObject.removeOTChangeListener(otChangeListener);
 	}
 
 	public void setViewEntry(OTViewEntry viewConfig) 
 	{
 		if (viewConfig instanceof OTUnitValueViewConfig){
 			this.viewConfig = (OTUnitValueViewConfig)viewConfig;
 		}
 	}
 
 	protected void createOTListener()
 	{
 		otChangeListener = new OTChangeListener() 
 		{
 			/**
 			 * Called when a property is changed in the OTUnitValue object
 			 */
 			public void stateChanged(OTChangeEvent e) 
 			{
 				if(e.getProperty() != null && (e.getProperty().equals("value")
 						|| e.getProperty().equals("unit"))) {
 					updateView();
 				}
 			}
 		};
 	}
 	
 	protected void updateView() 
 	{
 		newComponent.removeAll();
 		newComponent.add(getUnitValueView());
 		newComponent.validate();
 	}
 
 	protected static DecimalFormat createFormatObject(int precision)
 	{
 		DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance();
 		format.setMinimumFractionDigits(precision);
 		format.setMaximumFractionDigits(precision);	
 
 		return format;
 	}
 
 	public static String getStringValue(OTUnitValue otObject, DecimalFormat format)
 	{
 		return getStringValue(otObject, format, true);
 	}
 	
 	public static String getStringValue(OTUnitValue otObject, DecimalFormat format, boolean bIncludeUnit)
 	{
 		String strValue = "";
 		
 		if (format == null){
 			format = createFormatObject(2);
 		}
 
 		if(Float.isNaN(otObject.getValue())){
			return "???";
 		} 
 		else {
 			strValue = format.format(otObject.getValue());			
 			if (bIncludeUnit){
 				strValue = strValue + " " + otObject.getUnit();
 			}
 			return strValue;
 		}
 	}
 }
