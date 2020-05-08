 package hoplugins;
 
 import hoplugins.commons.utils.PluginProperty;
 import hoplugins.youthclub.ctrl.Retriever;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JWindow;
 
 import plugins.IHOMiniModel;
 import plugins.IOfficialPlugin;
 import plugins.IPlugin;
 import plugins.IRefreshable;
 
 /**
  * HO! plugin to retrieve and visualize data from the famous HT youth academy
  * site "HT-YouthClub" (www.hattrick-youthclub.org).
  */
 public class YouthClub implements IPlugin, IRefreshable, IOfficialPlugin {
 	// plugin name
 	public static final String PLUGIN_NAME = "YouthClub";
 	private static final String PLUGIN_PACKAGE = "YouthClub";
 	private static IHOMiniModel miniModel;
 
 	// plugin version constant
 	public static final double PLUGIN_VERSION = 0.1;
 	// plugin id
	private static final int PLUGIN_ID = 45;
 	// debug mode
 	public static boolean DEBUG = false;
 
 	public final String getName() {
 		return getPluginName() + " " + getVersion();
 	}
 
 	public final int getPluginID() {
 		return PLUGIN_ID;
 	}
 
 	public final String getPluginName() {
 		return PLUGIN_NAME;
 	}
 
 	/**
 	 * Get the plugin related files, which should not get deleted (e.g. config files).
 	 */
 	public final File[] getUnquenchableFiles() {
 		return null;
 	}
 
 	public final double getVersion() {
 		return PLUGIN_VERSION;
 	}
 
 	/**
 	 * This method is called when new data is available
 	 * (i.e. after a download, after setting options, after sub skill recalculation...).
 	 */
 	public final void refresh() {
 		miniModel = Commons.getModel();
 		final JWindow waitWindow = miniModel.getGUI().createWaitDialog(miniModel.getGUI().getOwner4Dialog());
 		waitWindow.setVisible(true);
 		// TODO: call refresh logic here
 		waitWindow.setVisible(false);
 		waitWindow.dispose();
 	}
 
 
 	/**
 	 * This method is called on HO startup.
 	 */
 	public final void start(IHOMiniModel hoMiniModel) {
 		YouthClub.miniModel = hoMiniModel;
 		PluginProperty.loadPluginProperties(PLUGIN_PACKAGE);
 		//hoMiniModel.getGUI().addOptionPanel(getPluginName(), new OptionPanel()); // add plugin specific panel to HO settings
 		hoMiniModel.getGUI().addMenu(createMenu()); // add plugin specific menu
 		//hoMiniModel.getGUI().registerRefreshable(this);
 	}
 
 	/**
 	 * Get the HO model.
 	 */
 	public static IHOMiniModel getMiniModel() {
 		return miniModel;
 	}
 	
 	/**
 	 * Create a new menu item.
 	 * TODO
 	 */
 	public static JMenu createMenu() {
 		JMenu menu = new JMenu(PLUGIN_NAME);
 		JMenuItem test1 = new JMenuItem("Test 1");
 		test1.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent ev) {
         		try {
         			String ycCode = ""; // TODO
         			Retriever.loadPackage1(ycCode);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
         	}
         });
     	JMenuItem about = new JMenuItem("About");
         about.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent ev) {
         		JPanel panel = new JPanel();
         		panel.add(new JLabel(PLUGIN_NAME + " " + PLUGIN_VERSION));
         		JOptionPane.showMessageDialog(miniModel.getGUI().getOwner4Dialog(), panel,
         				"About", JOptionPane.PLAIN_MESSAGE);
         	}
         });
         if (miniModel.getBasics().getTeamId() != 0) {
         	menu.add(test1);
         }
         menu.add(about);
         return menu;
 	}
 }
