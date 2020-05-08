 package com.b4e.plugin_manager.ui;
 
 import java.awt.Color;
 import java.awt.Dialog;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.LayoutStyle;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingWorker;
 import javax.swing.border.Border;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 
 import com.b4e.logger.api.Logger;
 import com.b4e.plugin_manager.i18n.T;
 
 public class UIProvider_ManageExisting {
 
     private final Logger LOG = new Logger(UIProvider_ManageExisting.class);
 
     private final String[] columnNames = { T.get(T.NAME), T.get(T.VERSION),
 	    T.get(T.STATE), "" };
     private final String systemPluginPrefix = new String("plugin-system");
 
     private final int PADDING = 5;
     final int COLUMN_NAME = 0;
     final int COLUMN_VERSION = 1;
     final int COLUMN_STATE = 2;
     final int COLUMN_ID = 3;
 
     private JButton btnStartStop;
     private JButton btnUninstall;
     private JButton btnRefresh;
     private JTable pluginsTable;
 
     public void show(final BundleContext context, JFrame aMainFrame) {
 	final JDialog dialog = new JDialog(aMainFrame,
 		T.get(T.INSTALLED_PLUGINS), Dialog.ModalityType.TOOLKIT_MODAL);
 
 	pluginsTable = createTable();
 	initTableWithData(context);
 	JScrollPane scrollPane = new JScrollPane(pluginsTable);
 
 	JButton btnInstallNew = createBtnInstallNew(context, dialog);
 
 	btnRefresh = new JButton(new AbstractAction(T.get(T.REFRESH)) {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		initTableWithData(context);
 	    }
 	});
 
 	btnStartStop = new JButton(new AbstractAction(T.get(T.START)) {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void actionPerformed(ActionEvent event) {
 		if (pluginsTable.getSelectedRow() < 0) {
 		    return;
 		}
 		boolean isStart = btnStartStop.getText().equals(T.get(T.START));
 		processStartStop(isStart, getSelectedBundle(context), dialog,
 			context);
 	    }
 	});
 	btnStartStop.setEnabled(false);
 
 	btnUninstall = new JButton(new AbstractAction(T.get(T.UNINSTALL)) {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		processUninstall(getSelectedBundle(context), dialog, context);
 	    }
 	});
 	btnUninstall.setEnabled(false);
 
 	JButton btnCancel = new JButton(new AbstractAction(T.get(T.CANCEL)) {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		dialog.dispose();
 	    }
 	});
 
 	JPanel contentPanel = new JPanel();
 	Border padding = BorderFactory.createEmptyBorder(PADDING, PADDING,
 		PADDING, PADDING);
 	contentPanel.setBorder(padding);
 	dialog.setContentPane(contentPanel);
 
 	GroupLayout layout = new GroupLayout(contentPanel);
 	contentPanel.setLayout(layout);
 	layout.setAutoCreateGaps(true);
 	layout.setAutoCreateContainerGaps(true);
 
 	layout.setHorizontalGroup(layout
 		.createParallelGroup(Alignment.CENTER)
 		.addComponent(scrollPane)
 		.addGroup(
 			layout.createSequentialGroup()
 				.addPreferredGap(
 					LayoutStyle.ComponentPlacement.RELATED,
 					GroupLayout.DEFAULT_SIZE,
 					Short.MAX_VALUE)
 				.addComponent(btnInstallNew))
 		.addGroup(
 			layout.createSequentialGroup()
 				.addComponent(btnRefresh)
 				.addContainerGap(50, 60)
 				.addPreferredGap(
 					LayoutStyle.ComponentPlacement.RELATED,
 					GroupLayout.DEFAULT_SIZE,
 					Short.MAX_VALUE)
 				.addComponent(btnStartStop)
 				.addComponent(btnUninstall)
 				.addComponent(btnCancel)));
 	layout.setVerticalGroup(layout
 		.createSequentialGroup()
 		.addComponent(scrollPane)
 		.addComponent(btnInstallNew)
 		.addGroup(
 			layout.createParallelGroup().addComponent(btnRefresh)
 				.addComponent(btnStartStop)
 				.addComponent(btnUninstall)
 				.addComponent(btnCancel)));
 
 	layout.linkSize(btnRefresh, btnStartStop, btnUninstall, btnCancel, btnInstallNew);
 
 	dialog.pack();
 	dialog.setLocationRelativeTo(aMainFrame);
 	dialog.setVisible(true);
     }
 
     private JButton createBtnInstallNew(final BundleContext context,
 	    final Dialog aMainFrame) {
 	JButton result = new JButton(new AbstractAction() {
 
 	    private static final long serialVersionUID = 1L;
 
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		UIProvider_InstallNew windowHelperInstallNew = new UIProvider_InstallNew();
 		windowHelperInstallNew.show(context, aMainFrame);
 	    }
 	});
 	result.setText(new String("<HTML><FONT color=\"#000099\"><U>")
 		+ T.get(T.INSTALL_NEW) + new String("</U></FONT></HTML>"));
 	result.setHorizontalAlignment(SwingConstants.RIGHT);
 	result.setBorderPainted(false);
 	result.setOpaque(false);
 	result.setBackground(Color.WHITE);
 
 	return result;
     }
 
     private Bundle getSelectedBundle(BundleContext context) {
 	DefaultTableModel model = (DefaultTableModel) pluginsTable.getModel();
 	String bundleID = model.getValueAt(pluginsTable.getSelectedRow(),
 		COLUMN_ID).toString();
 	return context.getBundle(Long.parseLong(bundleID));
     }
 
     private void processUninstall(final Bundle aBundle, final JDialog dialog,
 	    final BundleContext context) {
 	new SwingWorker<Void, Void>() {
 
 	    private final int FAILED_TO_UNINSTALL = 0;
 	    private final int SUCCEED_TO_UNINSTALL = 1;
 	    private int result;
 	    private final String bundleName = aBundle.getSymbolicName();
 
 	    @Override
 	    protected Void doInBackground() throws Exception {
 		try {
 		    aBundle.uninstall();
 		    result = SUCCEED_TO_UNINSTALL;
 		} catch (BundleException e) {
 		    result = FAILED_TO_UNINSTALL;
 		    LOG.error(new String("Failed to uninstall bundle "
 			    + bundleName), e);
 		}
 		return null;
 	    }
 
 	    @Override
 	    protected void done() {
 		if (result == FAILED_TO_UNINSTALL) {
 		    JOptionPane.showMessageDialog(dialog, String.format(
 			    T.get(T.BUNDLE_UNINSTALLING_FAILED), bundleName), T
 			    .get(T.BUNDLE_UNINSTALLING),
 			    JOptionPane.ERROR_MESSAGE);
 		} else {
 		    JOptionPane.showMessageDialog(dialog, String.format(
 			    T.get(T.BUNDLE_UNINSTALLED), bundleName), T
 			    .get(T.BUNDLE_UNINSTALLING),
 			    JOptionPane.PLAIN_MESSAGE);
 		}
 		initTableWithData(context);
 	    }
 
 	}.execute();
     }
 
     private void processStartStop(final boolean aIsStart, final Bundle aBundle,
 	    final JDialog dialog, final BundleContext context) {
 	new SwingWorker<Void, Void>() {
 
 	    private final String bundleName = aBundle.getSymbolicName();
 
 	    private final int FAILED_TO_START = 0;
 	    private final int FAILED_TO_STOP = 1;
 	    private final int SUCCEED_TO_START = 2;
 	    private final int SUCCEED_TO_STOP = 3;
 	    private int result;
 
 	    @Override
 	    protected Void doInBackground() throws Exception {
 		if (aIsStart) {
 		    try {
 			aBundle.start();
 			result = SUCCEED_TO_START;
 		    } catch (BundleException e) {
 			LOG.error(new String("Failed to start bundle "
 				+ bundleName), e);
 			result = FAILED_TO_START;
 		    }
 		} else {
 		    try {
 			aBundle.stop();
 			result = SUCCEED_TO_STOP;
 		    } catch (BundleException e) {
 			LOG.error(new String("Failed to stop bundle "
 				+ bundleName), e);
 			result = FAILED_TO_STOP;
 		    }
 		}
 		return null;
 	    }
 
 	    @Override
 	    protected void done() {
 		switch (result) {
 		case FAILED_TO_START:
 		    JOptionPane.showMessageDialog(dialog, String.format(
 			    T.get(T.BUNDLE_STARTING_FAILED), bundleName), T
 			    .get(T.BUNDLE_STARTING), JOptionPane.ERROR_MESSAGE);
 		    break;
 		case FAILED_TO_STOP:
 		    JOptionPane.showMessageDialog(dialog, String.format(
 			    T.get(T.BUNDLE_STOPPING_FAILED), bundleName), T
 			    .get(T.BUNDLE_STOPPING), JOptionPane.ERROR_MESSAGE);
 		    break;
 		case SUCCEED_TO_START:
 		    JOptionPane
 			    .showMessageDialog(dialog, T.get(String.format(
 				    T.BUNDLE_STARTED, bundleName)), T
 				    .get(T.BUNDLE_STARTING),
 				    JOptionPane.PLAIN_MESSAGE);
 		    break;
 		case SUCCEED_TO_STOP:
 		    JOptionPane
 			    .showMessageDialog(dialog, T.get(String.format(
 				    T.BUNDLE_STOPPED, bundleName)), T
 				    .get(T.BUNDLE_STOPPING),
 				    JOptionPane.PLAIN_MESSAGE);
 		    break;
 		}
 		initTableWithData(context);
 	    }
 	}.execute();
     }
 
     private void initTableWithData(final BundleContext context) {
 
 	new SwingWorker<Void, String>() {
 
 	    private String[][] strRowValues = new String[context.getBundles().length][columnNames.length];
 
 	    @Override
 	    protected Void doInBackground() throws Exception {
 		Bundle[] bundles = context.getBundles();
 		strRowValues = new String[bundles.length][columnNames.length];
 		for (int i = 0; i < bundles.length; i++) {
 		    strRowValues[i][COLUMN_NAME] = bundles[i].getSymbolicName();
 		    strRowValues[i][COLUMN_VERSION] = bundles[i].getVersion()
 			    .toString();
 		    strRowValues[i][COLUMN_STATE] = getBundleState(bundles[i]
 			    .getState());
 		    strRowValues[i][COLUMN_ID] = String.valueOf(bundles[i]
 			    .getBundleId());
 		}
 		return null;
 	    }
 
 	    @Override
 	    protected void done() {
 		setButtonsEnabled(false);
 		DefaultTableModel model = (DefaultTableModel) pluginsTable
 			.getModel();
 		model.setRowCount(0);
 		for (String[] row : strRowValues) {
 		    model.addRow(row);
 		}
 		model.fireTableDataChanged();
 	    }
 	}.execute();
     }
 
     private String getBundleState(int state) {
 	switch (state) {
 	case Bundle.ACTIVE:
 	    return T.get(T.ACTIVE);
 	case Bundle.INSTALLED:
 	    return T.get(T.INSTALLED);
 	case Bundle.RESOLVED:
 	    return T.get(T.RESOLVED);
 	case Bundle.STARTING:
 	    return T.get(T.STARTING);
 	case Bundle.STOPPING:
 	    return T.get(T.STOPPING);
 	case Bundle.UNINSTALLED:
 	    return T.get(T.UNINSTALLED);
 	}
 	return null;
     }
 
     private JTable createTable() {
 
 	DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
 	final JTable result = new JTable(tableModel);
 	result.removeColumn(result.getColumnModel().getColumn(COLUMN_ID));
 	result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 	result.getSelectionModel().addListSelectionListener(
 		new ListSelectionListener() {
 
 		    @Override
 		    public void valueChanged(ListSelectionEvent e) {
 			if (e.getValueIsAdjusting()) {
 			    int selectedRow = result.getSelectedRow();
 			    if (selectedRow < 0) {
 				return;
 			    }
 			    Object strPluginName = result.getModel()
 				    .getValueAt(selectedRow, COLUMN_NAME);
 			    Object strPluginID = result.getModel().getValueAt(
 				    selectedRow, COLUMN_ID);
 
 			    if (new String("0").equals(strPluginID)
 				    || strPluginName.toString().startsWith(
 					    systemPluginPrefix)) {
 				setButtonsEnabled(false);
 			    } else {
 				Object strPluginStatus = result.getModel()
 					.getValueAt(selectedRow, COLUMN_STATE);
 				if (T.get(T.ACTIVE).equals(strPluginStatus)) {
 				    btnStartStop.setText(T.get(T.STOP));
 				} else {
 				    btnStartStop.setText(T.get(T.START));
 				}
 				setButtonsEnabled(true);
 			    }
 			}
 		    }
 		});
 
 	return result;
     }
 
     private void setButtonsEnabled(boolean aIsEnabled) {
 	btnStartStop.setEnabled(aIsEnabled);
 	btnUninstall.setEnabled(aIsEnabled);
     }
 }
