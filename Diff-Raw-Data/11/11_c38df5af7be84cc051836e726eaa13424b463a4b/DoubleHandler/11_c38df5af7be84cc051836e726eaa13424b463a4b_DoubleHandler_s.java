 package org.cytoscape.work.internal.tunables;
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.lang.reflect.*;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.util.Properties;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ListSelectionEvent;
 
 import org.cytoscape.work.Tunable;
 import org.cytoscape.work.swing.AbstractGUITunableHandler;
 
 
 /**
  * Handler for the type <i>Double</i> of <code>Tunable</code>
  *
  * @author pasteur
  */
 public class DoubleHandler extends AbstractGUITunableHandler implements ActionListener {
 	private JFormattedTextField textField;
 	private String newline = System.getProperty("line.separator");
 
 	/**
 	 * Constructs the <code>GUIHandler</code> for the <code>Double</code> type
 	 *
 	 * It creates the Swing component for this Object (JTextField) that contains the initial value of the Double Object annotated as <code>Tunable</code>, its description, and displays it in a proper way
 	 *
 	 *
 	 * @param f field that has been annotated
 	 * @param o object contained in <code>f</code>
 	 * @param t tunable associated to <code>f</code>
 	 */
 	public DoubleHandler(Field f, Object o, Tunable t) {
 		super(f,o,t);
 		init();
 	}
 
 	public DoubleHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
 		super(getter, setter, instance, tunable);
 		init();
 	}
 
 	private void init() {
 		Double d;
 		try {
 			d = (Double)getValue();
 		} catch(final Exception e) {
 			e.printStackTrace();
 			d = Double.valueOf(0.0);
 		}
 
 		//set Gui
 		
 		textField = new JFormattedTextField(new DecimalFormat());
 		textField.setValue(d);
 		panel = new JPanel(new BorderLayout());
 		JLabel label = new JLabel(getDescription());
 		label.setFont(new Font(null, Font.PLAIN,12));
 		textField.setHorizontalAlignment(JTextField.RIGHT);
 		textField.addActionListener(this);
 
 		if (horizontal) {
 			panel.add(label, BorderLayout.NORTH);
 			panel.add(textField, BorderLayout.SOUTH);
 		} else {
 			panel.add(label, BorderLayout.WEST);
 			panel.add(textField, BorderLayout.EAST);
 		}
 	}
 
 	public void update(){
 		Double d;
 		try {
 			d = (Double)getValue();
 			textField.setValue(d);
 
 		} catch(final Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Catches the value inserted in the JTextField, parses it to a <code>Double</code> value, and tries to set it to the initial object. If it can't, throws an exception that displays the source error to the user
 	 */
 	public void handle() {
 		textField.setBackground(Color.white);

 		Double d;
 		try{
			d = Double.parseDouble(textField.getText());
 			try {
 				setValue(d);
				
 			} catch (final Exception e) {
 				textField.setBackground(Color.red);
 				JOptionPane.showMessageDialog(null, "The value entered cannot be set!", "Error", JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 				textField.setBackground(Color.white);
 				return;
 			}
			textField.setValue(d);
 		} catch(NumberFormatException nfe) {
 			textField.setBackground(Color.red);
 			try {
 				d = (Double)getValue();
 			} catch(final Exception e){
 				e.printStackTrace();
 				d = Double.valueOf(0.0);
 			}
 			JOptionPane.showMessageDialog(null,"A double was expected. Value will be set to default: " + d, "Error", JOptionPane.ERROR_MESSAGE);
 			try {
 				textField.setValue(getValue().toString());
 				textField.setBackground(Color.white);
 			} catch(final Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * To get the item that is currently selected
 	 */
 	public String getState() {
 		if ( textField == null )
 			return "";
 
		Double d = Double.parseDouble( textField.getText());
 		final String text = d.toString();
 		if ( text == null )
 			return "";
 
 		try {
 			 //d = Double.parseDouble(text);
 			return text;
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}
 		
 	}
 	
 	/**
 	 *  Action listener event handler.
 	 *
 	 *  @param ae specifics of the event (ignored!)
 	 */
 	public void actionPerformed(ActionEvent ae) {
 		handle();
 	}
 
 }
 
 
 
