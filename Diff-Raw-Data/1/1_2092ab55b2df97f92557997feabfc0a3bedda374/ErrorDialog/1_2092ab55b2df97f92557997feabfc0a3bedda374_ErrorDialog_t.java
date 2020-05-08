 package pleocmd.itfc.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 
 import pleocmd.Log;
 import pleocmd.cfg.ConfigCollection;
 import pleocmd.cfg.Configuration;
 import pleocmd.cfg.ConfigurationInterface;
 import pleocmd.cfg.Group;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.itfc.gui.Layouter.Button;
 
 public final class ErrorDialog extends JDialog implements
 		ConfigurationInterface {
 
 	private static final long serialVersionUID = -8104196615240425295L;
 
 	private static ErrorDialog errorDialog;
 
 	private final Layouter layErrorPanel;
 
 	private final JScrollPane spErrorPanel;
 
 	private boolean canDisposeIfHidden;
 
 	private int errorCount;
 
 	private final Map<AbstractButton, String> map = new HashMap<AbstractButton, String>();
 
 	private boolean ignoreChange;
 
 	private final ConfigCollection<String> cfgSuppressed = new ConfigCollection<String>(
 			"Suppressed", ConfigCollection.Type.Set) {
 		@Override
 		protected String createItem(final String itemAsString)
 				throws ConfigurationException {
 			return itemAsString;
 		}
 	};
 
 	private ErrorDialog() {
 		errorDialog = this;
 
 		setTitle("Error");
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(final WindowEvent e) {
 				close();
 			}
 		});
 
 		// Add components
 		final Layouter lay = new Layouter(this);
 
 		final JPanel panel = new JPanel();
 		layErrorPanel = new Layouter(panel);
 		layErrorPanel.nextComponentsAlwaysOnLastLine();
 		layErrorPanel.addVerticalSpacer();
 		spErrorPanel = new JScrollPane(panel,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		lay.addWholeLine(spErrorPanel, true);
 		spErrorPanel.setBorder(null);
 
 		lay.addButton("Reset", "",
 				"Clears the list of suppressed error messages", new Runnable() {
 					@Override
 					public void run() {
 						reset();
 					}
 				});
 		lay.addSpacer();
 		final JButton btn = lay.addButton(Button.Ok, new Runnable() {
 			@Override
 			public void run() {
 				close();
 			}
 		});
 		getRootPane().setDefaultButton(btn);
 		btn.requestFocusInWindow();
 
 		setAlwaysOnTop(true);
 		setModal(true);
		pack();
 
 		try {
 			Configuration.the().registerConfigurableObject(this,
 					getClass().getSimpleName());
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 	}
 
 	public static ErrorDialog the() {
 		if (errorDialog == null) new ErrorDialog();
 		return errorDialog;
 	}
 
 	public static void show(final Log log) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				the().showLog(log);
 			}
 		});
 	}
 
 	protected void showLog(final Log log) {
 		final String caller = log.getCaller().toString();
 		if (cfgSuppressed.contains(caller)) return;
 
 		final JLabel lblT, lblC;
 		final JTextArea lblM;
 		final JCheckBox cbS;
 		if (errorCount > 0)
 			layErrorPanel.addWholeLine(new JSeparator(), false);
 		layErrorPanel.add(lblT = new JLabel(log.getFormattedTime()), false);
 		layErrorPanel.add(lblC = new JLabel(log.getFormattedCaller()), false);
 		layErrorPanel.addSpacer();
 		layErrorPanel.add(cbS = new JCheckBox("Suppress"), false);
 		layErrorPanel.newLine();
 		layErrorPanel.addWholeLine(lblM = new JTextArea(log.getMsg()), false);
 
 		lblM.setLineWrap(true);
 		lblM.setWrapStyleWord(true);
 		lblM.setEditable(false);
 		lblM.setOpaque(false);
 		lblM.setForeground(Color.RED);
 
 		map.put(cbS, caller);
 		cbS.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				final boolean sel = cbS.isSelected();
 				lblT.setForeground(sel ? Color.GRAY : cbS.getForeground());
 				lblC.setForeground(sel ? Color.GRAY : cbS.getForeground());
 				lblM.setForeground(sel ? Color.GRAY : Color.RED);
 				try {
 					changeSuppress(caller, sel);
 				} catch (final ConfigurationException exc) {
 					Log.error(exc);
 				}
 			}
 		});
 
 		++errorCount;
 		final Dimension pref = getPreferredSize();
 		if (errorCount > 5) {
 			pref.height = getHeight();
 			pref.width += spErrorPanel.getVerticalScrollBar().getWidth();
 		}
 		setSize(pref);
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	protected void changeSuppress(final String caller, final boolean add)
 			throws ConfigurationException {
 		if (ignoreChange) return;
 		if (add)
 			cfgSuppressed.addContent(caller);
 		else
 			cfgSuppressed.removeContent(caller);
 		ignoreChange = true;
 		try {
 			final Component[] comps = layErrorPanel.getContainer()
 					.getComponents();
 			for (final Component comp : comps)
 				if (comp instanceof AbstractButton
 						&& map.get(comp).equals(caller))
 					if (((AbstractButton) comp).isSelected() ^ add)
 						((AbstractButton) comp).doClick();
 		} finally {
 			ignoreChange = false;
 		}
 	}
 
 	protected void reset() {
 		cfgSuppressed.clearContent();
 		ignoreChange = true;
 		try {
 			final Component[] comps = layErrorPanel.getContainer()
 					.getComponents();
 			for (final Component comp : comps)
 				if (comp instanceof AbstractButton)
 					if (((AbstractButton) comp).isSelected())
 						((AbstractButton) comp).doClick();
 		} finally {
 			ignoreChange = false;
 		}
 	}
 
 	protected void close() {
 		errorCount = 0;
 		map.clear();
 		layErrorPanel.clear();
 		layErrorPanel.nextComponentsAlwaysOnLastLine();
 		layErrorPanel.addVerticalSpacer();
 		pack();
 		if (canDisposeIfHidden)
 			dispose();
 		else
 			setVisible(false);
 	}
 
 	public static void canDisposeIfHidden() {
 		if (errorDialog != null) {
 			errorDialog.canDisposeIfHidden = true;
 			if (!errorDialog.isVisible()) errorDialog.dispose();
 		}
 	}
 
 	public Group getSkeleton(final String groupName) {
 		return new Group(groupName).add(cfgSuppressed);
 	}
 
 	public void configurationAboutToBeChanged() {
 		// nothing to do
 	}
 
 	public void configurationChanged(final Group group) {
 		// nothing to do
 	}
 
 	public List<Group> configurationWriteback() {
 		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
 	}
 
 }
