 /**
  * 
  */
 package nl.naiaden.ahci.poetrist.gui.panel;
 
 import java.awt.Color;
 
 import javax.swing.JButton;
 
 /**
  * This class is a wrapper around the normal {@link JButton}. However, because
  * the normal {@link JButton} background colour is working on a Mac out of the
  * box, we use this button class to automatically solve the problem.
  * 
  * @author louis
  * 
  */
 public class ColorSelectionButton extends JButton
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8194198147219801600L;
 
 	private Color color = null;
 
 	/**
 	 * Creates a button with a fixed background colour.
 	 * 
 	 * @param color
 	 *            The background colour.
 	 * @param buttonListener
 	 *            The ButtonListener.
 	 */
 	public ColorSelectionButton(Color color)
 	{
 		this.color = color;
 
 		setOpaque(true);
 		setBackground(color);
		setBorderPainted(false);
 	}
 
 	/**
 	 * @return the color.
 	 */
 	public Color getColor()
 	{
 		return color;
 	}
 }
