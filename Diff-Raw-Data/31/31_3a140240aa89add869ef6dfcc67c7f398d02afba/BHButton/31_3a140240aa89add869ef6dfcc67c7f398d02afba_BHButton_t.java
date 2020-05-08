 package org.bh.gui.swing;
 
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.event.MouseInputAdapter;
 
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.PlatformKey;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  * BHButton to display buttons at screen.
  * 
  * <p>
  * This class extends the Swing <code>JButton</code> to display simple buttons
  * in Business Horizon.
  * 
  * @author Thiele.Klaus
  * @author Schmalzhaf.Alexander
  * @author Robert
  * 
  * @version 0.1, 2009/12/13
  * @version 0.1.1 2009/12/26
  * 
  */
 public class BHButton extends JButton implements IBHComponent, IBHAction {
 	private static List<IBHAction> platformItems = new ArrayList<IBHAction>();
 	private static ITranslator translator = Services.getTranslator();
 	
 	private String key;
 	private PlatformKey platformKey;
 	private String toolTip;
 
 	/**
 	 * Secondary constructor for platform buttons (are added to
 	 * platformButtons-list and uses ENUM Platform Key instead of String for
 	 * key)
 	 * 
 	 * @param key
 	 * @param isPlatformButton
 	 *            adds button to platformbutton-list
 	 */
 	public BHButton(PlatformKey platformKey, boolean isPlatformButton) {
 		super();
 		this.setProperties();
 		this.platformKey = platformKey;
 		this.key = platformKey.toString();
 		reloadText();
 
 		if (isPlatformButton)
 			platformItems.add(this);
 	}
 
 	/**
 	 * Standard constructor to create buttons NOT for platform
 	 * 
 	 * @param key
 	 */
 	public BHButton(String key) {
 		super();
 		this.setProperties();
 		this.key = key;
 		reloadText();
 		// TODO get INPUT-HINT out of properties-File (querstions? Ask Alex)
 	}
 
 	/**
 	 * Standard constructor to create buttons NOT for platform
 	 * 
 	 * @param key
 	 */
 	public BHButton(Object key) {
 		this(key.toString());
 	}
 
 	/**
 	 * return the key for value mapping
 	 * 
 	 * @return
 	 */
 	@Override
 	public String getKey() {
 		return key;
 	}
 
 	@Override
 	public PlatformKey getPlatformKey() {
 		return this.platformKey;
 	}
 
 	public boolean isTypeValid() {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	/**
 	 * set properties of instance.
 	 */
 	private void setProperties() {
 		Services.addPlatformListener(this);
 		this.putClientProperty("JComponent.sizeVariant", IBHComponent.MINI); // Minibutton
 	}
 
 	public static List<IBHAction> getPlatformItems() {
 		return platformItems;
 	}
 
 	@Override
 	public boolean isPlatformItem() {
 		return (this.platformKey != null);
 	}
 
 	@Override
 	public String getBHHint() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	/**
 	 * 
 	 * This MouseListener provides Buttons with the ability to show their texts
 	 * in StatusBar
 	 * 
 	 * @author Alexander Schmalzhaf
 	 * @version 1.0, 29.12.2009
 	 * 
 	 */
 	class BHToolTipListener extends MouseInputAdapter {
 
 		private String listenerToolTip;
 
 		public BHToolTipListener(String toolTip) {
 			this.listenerToolTip = toolTip;
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			Services.getBHstatusBar().setHint(
					new JLabel(listenerToolTip));
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			Services.getBHstatusBar().removeHint();
 		}
 
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			this.reloadText();
 		}
 	}
 
 	@Override
 	public void reloadText() {
 		this.setText(translator.translate(key));
 		// set ToolTip if available
 		this.toolTip = translator.translate(key, BHTranslator.LONG);
 		if (!toolTip.isEmpty()) {
 			this.addMouseListener(new BHToolTipListener(toolTip));
 		}
 	}
 }
