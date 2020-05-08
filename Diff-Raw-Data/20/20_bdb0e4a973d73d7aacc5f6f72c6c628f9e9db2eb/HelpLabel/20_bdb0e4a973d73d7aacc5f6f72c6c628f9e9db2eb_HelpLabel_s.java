 package emcshop.gui;
 
 import static emcshop.util.GuiUtils.toolTipText;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import emcshop.gui.images.ImageManager;
 
 /**
  * Represents a JLabel with a help icon.
  */
 @SuppressWarnings("serial")
 public class HelpLabel extends JLabel {
 	private static final ImageIcon helpIcon = ImageManager.getHelpIcon();
 
 	/**
 	 * @param text the label text
 	 * @param tooltip the tooltip text
 	 */
 	public HelpLabel(String text, String tooltip) {
		super(text, helpIcon, SwingConstants.LEFT);
		setToolTipText(toolTipText(tooltip));
 	}
 }
