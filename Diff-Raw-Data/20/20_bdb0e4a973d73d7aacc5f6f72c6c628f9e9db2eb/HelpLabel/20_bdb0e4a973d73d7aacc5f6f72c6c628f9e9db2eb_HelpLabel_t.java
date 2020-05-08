 package emcshop.gui;
 
 import static emcshop.util.GuiUtils.toolTipText;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import emcshop.gui.images.ImageManager;
import emcshop.util.GuiUtils;
 
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
		super(text);
		if (!GuiUtils.linux) {
			setIcon(helpIcon);
			setHorizontalAlignment(SwingConstants.LEFT);
			setToolTipText(toolTipText(tooltip));
		}
 	}
 }
