 package hms.views;
 
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.text.*;
 
 import java.util.ArrayList;
 
 public abstract class AbstractInfoPanel extends JPanel {
 	protected static String REQUIRED_SIGNIFIER = "* ";
 	protected ArrayList<JComponent> requiredComponents = new ArrayList<JComponent>();
 	protected ArrayList<JComponent> editableComponents = new ArrayList<JComponent>();
 	
 	/**
	 * Initializes the user interface components.
 	 */
 	abstract void initUI();
 	
 	/**
 	 * Should reset the form inputs to their standard state.
 	 */
 	abstract void reset();
 	
 	/**
 	 * Must be overridden to load information from an object into the form. The Object
 	 * will typically be cast into something more useful (like a Patient or Nurse) so
 	 * that information can be loaded.
 	 * @param objToLoad The object to load information into the form from.
 	 */
 	abstract void loadInformation(Object objToLoad);
 	
 	/**
 	 * Must be overridden to validate all of the information on the form. Returns a boolean
 	 * value of true if all the inputs are valid and false otherwise. Should signify on the
 	 * form which fields are invalid.
 	 * @return true if the fields are all valid; false otherwise
 	 */
 	abstract boolean validateInformation();
 	
 	/**
 	 * Sets all text component borders to the same style so we have a more unified look
 	 * across JTextFields and JTextAreas.
 	 */
 	protected void setTextComponentBorders() {
 		for (Component comp : getComponents()) {
 			if (comp instanceof JTextComponent) {
 				JTextComponent textComp = (JTextComponent)comp;
 				textComp.setBorder(BorderFactory.createEtchedBorder());
 			}
 		}
 	}
 	
 	/**
 	 * Sets whether the editable components on this panel should be editable.
 	 * @param editable A boolean value to set whether the text components should be editable.
 	 */
 	public void setEditable(boolean editable) {
 		for (JComponent comp : editableComponents) {
 			comp.setEnabled(editable);
 		}
 	}
 	
 	/**
 	 * Toggles the signifiers for required fields on the panel.
 	 * @param showRequiredFields Boolean to determine whether the required fields should be signified
 	 */
 	public void signifyRequiredFields(boolean showRequiredFields) {
 		for (JComponent component : requiredComponents) {
 			if (component instanceof JTextComponent) {
 				if (showRequiredFields) {
 					JTextComponent textComp = (JTextComponent)component;
 					textComp.setText(REQUIRED_SIGNIFIER + textComp.getText());
 				} else {
 					JTextComponent textComp = (JTextComponent)component;
 					textComp.setText(textComp.getText().replaceFirst(REQUIRED_SIGNIFIER, ""));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Creates a form separator.
 	 * @param label The label to use on the separator.
 	 */
 	protected void addSeparator(String message) {
 		JLabel label = new JLabel(message);
 		Font font = label.getFont();
 		label.setFont(font.deriveFont(font.getStyle() ^ Font.BOLD));
 		this.add(label, "split, span, gapbottom 10");
 		this.add(new JSeparator(), "growx, wrap, gapbottom 10");
 	}
 }
