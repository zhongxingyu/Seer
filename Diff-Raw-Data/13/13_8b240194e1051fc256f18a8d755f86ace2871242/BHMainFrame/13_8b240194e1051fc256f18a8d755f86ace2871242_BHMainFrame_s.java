 package org.bh.gui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Image;
import java.util.List;
 import java.util.ArrayList;
 import java.util.prefs.BackingStoreException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 
 import org.apache.log4j.Logger;
 import org.bh.SVN;
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformController;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.ProjectRepositoryManager;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 
 /**
  * Main Frame for Business Horizon Application.
  * 
  * <p>
  * This <code>JFrame</code> provides the main frame for Business Horizon
  * Application.
  * 
  * @author Tietze.Patrick
  * @author Thiele.Klaus
  * @author Schmalzhaf.Alexander
  * @author Loeckelt.Michael
  * @version 0.1.1, 2009/12/16
  * @version 0.2, 2009/12/22
  * @version 0.3, 2009/12/31
  * @version 0.4, 2010/01/07
  * 
  */
 @SuppressWarnings("serial")
 public class BHMainFrame extends JFrame implements IPlatformListener {
 
 	/**
 	 * logger
 	 */
 	private static Logger log = Logger.getLogger(BHMainFrame.class);
 	
 	/**
 	 * Standard Bar height.
 	 */
 	public static final int STANDARDBARHEIGHT = 40;
 
 	/**
 	 * main panel.
 	 */
 	private JPanel desktop;
 
 	/**
 	 * Menu Bar for application.
 	 */
 	private BHMenuBar menuBar;
 
 	/**
 	 * ToolBar for desktop.
 	 */
 	private BHToolBar toolBar;
 
 	/**
 	 * Tree for File contents (placed on a ScrollPane).
 	 */
 	private JScrollPane bhTreeScroller;
 	
 	/**
 	 * Tree displaying the file contents.
 	 */
 	private BHTree bhTree;
 
 	/**
 	 * Status Bar.
 	 */
 	private BHStatusBar statusBar;
 
 	/**
 	 * TODO: Javadoc
 	 */
 	private BHContent content;
 
 	/**
 	 * Horizontal Split pane.
 	 */
 	private JSplitPane paneH;
 
 	/**
 	 * Open / Save dialog.
 	 */
 	private BHFileChooser chooser;
 	
 	private JScrollPane contentForm;
 	private Component chartsPanel;
 
 	/**
 	 * Standard constructor for <code>BHMainFrame</code>.
 	 * 
 	 * @param title
 	 *            title to be set for the <code>BHMainFrame</code>.
 	 */
 	public BHMainFrame() {
 		super();
 		this.setProperties();
 
 		// Build MenuBar
 		menuBar = new BHMenuBar();
 		this.setJMenuBar(menuBar);
 		
 		// build GUI components
 		desktop = new JPanel();
 		desktop.setLayout(new BorderLayout());
 
 		toolBar = new BHToolBar(getWidth(), STANDARDBARHEIGHT);
 		// toolBar.setBounds(0, 0, screenSize.width, standardBarHeight);
 
 		bhTree = new BHTree();
 		bhTreeScroller = new JScrollPane(bhTree);
 	
 		// treeBar.setBounds(0, standardBarHeight, treeBarWidth,
 		// screenSize.height-standardBarHeight);
 		// treeBar.setBounds(0,200,200,400);
 		
 		statusBar = Services.getBHstatusBar();
 		content = new BHContent();
 
 		// Create the horizontal split pane and put the treeBar and the content
 		// in it.
 		paneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bhTreeScroller,
 				content);
 		paneH.setOneTouchExpandable(true);
 		
 		bhTreeScroller.setMinimumSize(new Dimension(UIManager.getInt("BHTree.minimumWidth"), bhTreeScroller.getMinimumSize().height));
 		// stop moving the divider
 		// pane.setEnabled(false);
 
 		desktop.add(toolBar, BorderLayout.PAGE_START);
 		desktop.add(paneH, BorderLayout.CENTER);
 		desktop.add(statusBar, BorderLayout.PAGE_END);
 		// desktop.add(content, BorderLayout.CENTER);
 
 		this.setContentPane(desktop);
 
 		chooser = new BHFileChooser();
 	}
 
 	/**
 	 * Sets initial properties on <code>BHMainFrame</code>.
 	 */
 	private synchronized void setProperties() {
 		this.resetTitle();
 		this.setExtendedState(MAXIMIZED_BOTH);
 		this.setLocationRelativeTo(null);
 		this.setSize(1000, 700);
 		// EXIT is like app suicide
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		Services.addPlatformListener(this);
 		
 		//this.setIconImage(new ImageIcon("/org/bh/images/bh-logo.jpg").getImage()); //TODO Test on windows	
 		try {
 			List<Image> icons = new ArrayList<Image>();
 			icons.add(ImageIO.read(getClass().getResourceAsStream("/org/bh/images/BH-Logo-16px.png")));
 			icons.add(ImageIO.read(getClass().getResourceAsStream("/org/bh/images/BH-Logo-32px.png")));
 			icons.add(ImageIO.read(getClass().getResourceAsStream("/org/bh/images/BH-Logo-48px.png")));
 			this.setIconImages(icons);
 		} catch (Exception e) {
 			log.error("Failed to load IconImage", e);
 		}
 	}
 	
 	/**
 	 * resets the title of the <code>BHMainFrame</code>.
 	 */
 	public void resetTitle() {
 		String title =  Services.getTranslator().translate("title");
 		String path = PlatformController.preferences.get("path", null);
 		if (path != null) {
 			title += " - " + path;
 		}
 		
 		if (ProjectRepositoryManager.isChanged()) {
 			String changedSuffix = " (" + Services.getTranslator().translate("changed") + ")";
 			title += changedSuffix;
 		}
 		
 		if (SVN.isRevisionSet()) {
 			title += " (Revision " + SVN.getRevision() + ")";
 		}
 		
 		setTitle(title);
 	}
 	
 	/**
 	 * Disposes the Frame.
 	 */
 	@Override
 	public void dispose() {
 		// TODO Michael Löckelt: Kommentieren, Save Dialog
 		// TODO Dispose evtl noch auslagern
 		if (ProjectRepositoryManager.isChanged()) {
 			
			int i = JOptionPane.showConfirmDialog(this, Services.getTranslator().translate("PsaveEnd_long"), Services.getTranslator().translate("PsaveEnd"),JOptionPane.YES_NO_OPTION);
 			if (i == JOptionPane.YES_OPTION) {
 			
 				/**
 				 * Try to save all preferences
 				 * @author Marcus Katzor
 				 */
 				try {
 					Logger.getLogger(getClass()).debug("Save application preferences");
 					PlatformController.preferences.flush();
 					Services.firePlatformEvent(new PlatformEvent(BHMainFrame.class, PlatformEvent.Type.SAVE));
 				} catch (BackingStoreException e) {
 					Logger.getLogger(getClass()).error("Error while saving application preferences", e);
 				}
 				
 				super.dispose();
 				
 			} else if (i == JOptionPane.NO_OPTION) {
 				Logger.getLogger(getClass()).debug("Existing changes but no save wish - exiting app");
 				super.dispose();
 			}
 			
 		} else { 
 			Logger.getLogger(getClass()).debug("No changes - exiting app");
 			super.dispose();
 		}
 		
 	}
 
 	public void setContentForm(Component content) {
 		contentForm = new JScrollPane(content);
 		paneH.setRightComponent(contentForm);
 
 	}
 	
 	public JScrollPane getContentForm() {
 		return contentForm;
 	}
 
 	public JSplitPane createContentResultForm(Component chart) {
 		JSplitPane paneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getContentForm(), new JScrollPane(chart));
 		
 		paneV.setOneTouchExpandable(true);
 
 		paneH.setRightComponent(paneV);
 		
 		return paneV;
 	}
 	
 	public void setContentResultForm(JSplitPane contentResultForm){
 		paneH.setRightComponent(contentResultForm);
 	}
 
 	/**
 	 * Returns the <code>FileChooser</code> of the <code>BHMainFrame</code>.
 	 * @return the current <code>BHFileChooser</code>.
 	 */
 	public BHFileChooser getChooser() {
 		return this.chooser;
 	}
 	
 	/**
 	 * Returns the BHTree.
 	 * @return the BHTree.
 	 */
 	public BHTree getBHTree() {
 		return bhTree;
 	}
 
 	/**
 	 * Brings <code>BHMainFrame</code> to front when 
 	 * <code>PLATFORM_LOADING_COMPLETED</code>.
 	 */
 	@Override
 	public void platformEvent(PlatformEvent e) {
 
 		// Platform loading completed. show.
 		if (e.getEventType() == Type.PLATFORM_LOADING_COMPLETED) {
 			this.setVisible(true);
 			this.toFront();
 			this.requestFocus();
 		}
 		
 		// Data changed. Add changed suffix to title.
 		if (e.getEventType() == Type.DATA_CHANGED) {
 			String changedSuffix = " (" + Services.getTranslator().translate("changed") + ")";
 			if (! this.getTitle().endsWith(changedSuffix) && ProjectRepositoryManager.isChanged()) 
 				this.setTitle(this.getTitle() + changedSuffix);
 		}
 		
 		// Locale changed
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			this.resetTitle();
 			this.chooser = new BHFileChooser();
 		}
 	}
 	
 	
 }
