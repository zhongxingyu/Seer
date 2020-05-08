 package pleocmd.itfc.gui;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumnModel;
 
 import pleocmd.Log;
 import pleocmd.itfc.gui.Layouter.Button;
 
 public final class MainLogPanel extends JPanel {
 
 	private static final long serialVersionUID = -6921879308383765734L;
 
 	private final LogTableModel logModel;
 
 	private final JTable logTable;
 
 	private final JButton btnStart;
 
 	private final JButton btnAbort;
 
 	private final JButton btnSave;
 
 	private final JCheckBox cbShowDetail;
 
 	private final JButton btnClear;
 
 	private int minRowHeight;
 
 	public MainLogPanel() {
 		final Layouter lay = new Layouter(this);
 
 		logTable = new JTable() {
 
 			private static final long serialVersionUID = 1128237812769648620L;
 
 			@Override
 			public String getToolTipText(final MouseEvent event) {
 				return getBacktrace(rowAtPoint(event.getPoint()));
 			}
 
 		};
 		logModel = new LogTableModel(logTable);
 		logTable.setModel(logModel);
 		final TableColumnModel tcm = logTable.getTableHeader().getColumnModel();
 		tcm.getColumn(0).setHeaderValue("Time");
 		tcm.getColumn(0).setMinWidth(100);
 		tcm.getColumn(0).setMaxWidth(100);
 		tcm.getColumn(1).setHeaderValue("Type");
 		tcm.getColumn(1).setMinWidth(50);
 		tcm.getColumn(1).setMaxWidth(50);
 		tcm.getColumn(2).setHeaderValue("Source");
 		tcm.getColumn(2).setMinWidth(200);
 		tcm.getColumn(2).setMaxWidth(200);
 		tcm.getColumn(3).setHeaderValue("Message");
 		logTable.getTableHeader().setEnabled(false);
 		logTable.setShowGrid(false);
 		logTable.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 				if (e.getClickCount() == 1) markSelectedOccurrences();
 			}
 		});
 		lay.addWholeLine(new JScrollPane(logTable,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), true);
 
 		btnStart = lay.addButton("Start", "arrow-right",
 				"Starts the currently configured pipe", new Runnable() {
 					@Override
 					public void run() {
 						MainFrame.the().startPipeThread();
 					}
 				});
 		btnAbort = lay.addButton("Abort", "dialog-close",
 				"Forcefully aborts the currently running pipe", new Runnable() {
 					@Override
 					public void run() {
 						MainFrame.the().abortPipeThread();
 					}
 				});
 		lay.addSpacer();
 		cbShowDetail = new JCheckBox("Show detailed log", true);
 		cbShowDetail.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				refreshLogDetailState();
 			}
 		});
 		lay.add(cbShowDetail, false);
 		btnSave = lay.addButton(Button.SaveTo,
 				"Saves the whole log to a text file", new Runnable() {
 					@Override
 					public void run() {
 						writeLogToFile();
 					}
 				});
 		btnClear = lay.addButton(Button.Clear, "Empties the whole log list",
 				new Runnable() {
 					@Override
 					public void run() {
 						clearLog();
 					}
 				});
 	}
 
 	protected void markSelectedOccurrences() {
 		logModel.setMark(logTable.getSelectedRow());
 	}
 
 	protected String getBacktrace(final int index) {
 		final Throwable bt = logModel.getLogAt(index).getBacktrace();
 		if (bt == null) return null;
 		final StringWriter sw = new StringWriter();
 		final PrintWriter pw = new PrintWriter(sw);
 		bt.printStackTrace(pw); // CS_IGNORE
 		pw.flush();
 		return String.format("<html>%s</html>", sw.toString().replace("<",
 				"&lt;").replace("\n", "<br>"));
 	}
 
 	public void writeLogToFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileNameExtensionFilter("Ascii-Logfile",
 				"log"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) try {
			logModel.writeToFile(fc.getSelectedFile());
		} catch (final IOException exc) {
			Log.error(exc);
		}
 	}
 
 	public void clearLog() {
 		logModel.clear();
 		updateState();
 	}
 
 	public void refreshLogDetailState() {
 		Log.setLogDetailed(cbShowDetail.isSelected());
 		logModel.refresh();
 		updateState();
 	}
 
 	public void addLog(final Log log) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				addLog0(log);
 			}
 		});
 	}
 
 	protected void addLog0(final Log log) {
 		logModel.addLog(log);
 		final int row = logModel.getRowCount() - 1;
 		if (minRowHeight == 0)
 			minRowHeight = logTable.prepareRenderer(
 					logTable.getCellRenderer(0, 0), 0, 0).getPreferredSize().height;
 		logTable.setRowHeight(row, 2 + Math.max(minRowHeight, logTable
 				.prepareRenderer(logTable.getCellRenderer(row, 3), row, 3)
 				.getPreferredSize().height));
 		logTable.scrollRectToVisible(logTable.getCellRect(row, 0, true));
 		if (row == 1) updateState();
 	}
 
 	public void updateState() {
 		btnStart.setEnabled(!MainFrame.the().isPipeRunning());
 		btnAbort.setEnabled(MainFrame.the().isPipeRunning());
 		btnSave.setEnabled(logModel.getRowCount() > 0);
 		btnClear.setEnabled(logModel.getRowCount() > 0);
 	}
 
 	void updateRowHeights() {
 		if (logTable.getRowCount() == 0) return;
 		final TableCellRenderer tcr = logTable.getCellRenderer(0, 3);
 		minRowHeight = logTable.prepareRenderer(logTable.getCellRenderer(0, 0),
 				0, 0).getPreferredSize().height;
 		for (int i = logTable.getRowCount() - 1; i >= 0; --i)
 			logTable.setRowHeight(i, 2 + Math.max(minRowHeight, logTable
 					.prepareRenderer(tcr, i, 3).getPreferredSize().height));
 	}
 }
