 package org.bh.gui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import org.apache.log4j.Logger;
 import org.bh.platform.PlatformEvent;
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
  * @version 0.1.1, 2009/12/16
  * 
  */
 public class BHMainFrame extends JFrame {
 
 	/**
 	 * main panel.
 	 */
 	public static JPanel desktop;
 
 	/**
 	 * ToolBar for desktop.
 	 */
 	public BHToolBar toolBar;
 
 	/**
 	 * Tree for File contents.
 	 */
 	public static BHTree treeBar;
 
 	/**
 	 * Status Bar.
 	 */
 	public BHStatusBar statusBar;
 
 	public static BHContent content;
 
 	/**
 	 * Horizontal Split pane.
 	 */
 	static JSplitPane paneH;
 
 	/**
 	 * Vertical Split pane.
 	 */
 	JSplitPane paneV;
 
 	JLabel test;
 
 	/**
 	 * Open / Save dialog
 	 */
 	private BHFileChooser chooser;
 
 	/**
 	 * Standard Bar height.
 	 */
 	int standardBarHeight = 40;
 
 	/**
 	 * Tree Bar height.
 	 */
 	static int treeBarWidth = 200;
 
 	/**
 	 * Standard constructor for <code>BHMainFrame</code>.
 	 * 
 	 * @param title
 	 *            title to be set for the <code>BHMainFrame</code>.
 	 */
 
 	public BHMainFrame(String title) {
 		super(title);
 		this.setProperties();
 
 		// create main frame
 		setExtendedState(MAXIMIZED_BOTH);
 
 		// build GUI components
 		desktop = new JPanel();
 		desktop.setLayout(new BorderLayout());
 
 		toolBar = new BHToolBar(getWidth(), standardBarHeight);
 		// toolBar.setBounds(0, 0, screenSize.width, standardBarHeight);
 
 		treeBar = new BHTree();
 		// treeBar.setBounds(0, standardBarHeight, treeBarWidth,
 		// screenSize.height-standardBarHeight);
 		// treeBar.setBounds(0,200,200,400);
 
 		statusBar = Services.getBHstatusBar();
 		content = new BHContent();
 
 		// Create the horizontal split pane and put the treeBar and the content
 		// in it.
 		paneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeBar, content);
 		paneH.setOneTouchExpandable(true);
 		paneH.setDividerLocation(treeBarWidth);
 
 		// stop moving the divider
 		// pane.setEnabled(false);
 
 		desktop.add(toolBar, BorderLayout.PAGE_START);
 		desktop.add(paneH, BorderLayout.CENTER);
 		desktop.add(statusBar, BorderLayout.PAGE_END);
 		// desktop.add(content, BorderLayout.CENTER);
 
 		this.setContentPane(desktop);

		chooser = new BHFileChooser();

 		setVisible(true);
 
 		// work around a Java-or-whatever bug which causes the main window to
 		// hide behind Eclipse
 		this.setAlwaysOnTop(true);
 		this.setAlwaysOnTop(false);
 
 		Services.firePlatformEvent(new PlatformEvent(this,
 				Type.PLATFORM_LOADING_COMPLETED));
 	}
 
 	/**
 	 * Sets initial properties on <code>BHMainFrame</code>.
 	 */
 	private void setProperties() {
 		// Mac properties
 		// System.setProperty("apple.laf.useScreenMenuBar", "true");
 		// System.setProperty("com.apple.mrj.application.apple.menu.about.name",
 		// Services.getTranslator().translate("title"));
 
 		this.setNimbusLookAndFeel();
 		// EXIT is like app suicide
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		this.setJMenuBar(new BHMenuBar());
 	}
 
 	public static void addContentForms(Component content) {
 		paneH.setRightComponent(content);
 
 	}
 
 	public static void addContentFormsAndChart(Component forms, Component chart) {
 		JSplitPane paneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forms,
 				chart);
 		paneV.setOneTouchExpandable(true);
 
 		paneH.setRightComponent(paneV);
 	}
 
 	/**
 	 * Sets Nimbus from Sun Inc. as default Look & Feel. Java 6 Update 10
 	 * required. Don't change complex looking implementation of invokation,
 	 * there are valid reasons for it.<br />
 	 * 
 	 * <b>Remark</b> <br />
 	 * For further information on Nimbus see <a href=
 	 * "http://developers.sun.com/learning/javaoneonline/2008/pdf/TS-6096.pdf"
 	 * >JavaOne Slides</a>
 	 */
 	private void setNimbusLookAndFeel() {
 		// set Nimbus if available
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e) {
 			Logger
 					.getLogger(BHMainFrame.class)
 					.debug(
 							"Nimbus Look&Feel not found, fall back to default Look&Feel");
 		}
 	}
 }
