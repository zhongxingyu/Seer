 package gcapi.gui;
 
 import gcapi.utils.Logger;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.table.DefaultTableModel;
 
 import org.powerbot.core.script.ActiveScript;
 import org.powerbot.game.api.methods.Environment;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.bot.Context;
 
 public class Gui extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane = new JPanel();
 
 	private JLabel scriptLabel = new JLabel();
 	private JLabel scriptValueLabel = new JLabel();
 
 	private JLabel runtimeLabel = new JLabel();
 	private JLabel runtimeValueLabel = new JLabel();
 
 	private JLabel statusLabel = new JLabel();
 	private JLabel statusValueLabel = new JLabel();
 
 	private JTabbedPane tabArea = new JTabbedPane();
 	private JScrollPane tableScrollPane;
 	private DefaultTableModel model;
 	private JTable table;
 
 	private String[] columns = new String[] { "Data", "Value" };
 	private Object[][] tableData;
 
 	private long startTime;
 	public Timer runTime;
 
 	private int PADDING = 10;
 
 	private Logger logger;
 
 	public static ActiveScript parent;
 
 	@SuppressWarnings("serial")
 	public Gui(String scriptName, Logger logger, Object[][] data, ActiveScript parent) {
 
 		this.logger = logger;
		logger.log("Initialised stats GUI.");
 
 		this.parent = parent;
 
 		startTime = System.currentTimeMillis();
 
 		this.runTime = new Timer(0);
 
 		setTitle("GC GUI");
 		setPreferredSize(new Dimension(350, 300));
 		setResizable(false);
 		setLayout(new FlowLayout(FlowLayout.LEADING, PADDING, PADDING));
 		addWindowListener(new WindowEventHandler());
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
 		scriptLabel.setText("Script Name:");
 		scriptLabel.setLabelFor(scriptValueLabel);
 		scriptLabel.setPreferredSize(new Dimension(120, 20));
 		scriptValueLabel.setText(scriptName);
 		scriptValueLabel.setPreferredSize(new Dimension(120, 20));
 
 		runtimeLabel.setText("Run time:");
 		runtimeLabel.setLabelFor(runtimeValueLabel);
 		runtimeLabel.setPreferredSize(new Dimension(120, 20));
 		runtimeValueLabel.setPreferredSize(new Dimension(120, 20));
 		new Thread() {
 			public void run() {
 				while (true) { // Constantly updates run time for label
 					runtimeValueLabel.setText(getTimeRunning());
 					try {
 						Thread.sleep(1);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 			}
 		}.start();
 
 		scriptLabel.setText("Script Name:");
 		scriptLabel.setLabelFor(scriptValueLabel);
 		scriptLabel.setPreferredSize(new Dimension(120, 20));
 		scriptValueLabel.setText(scriptName);
 		scriptValueLabel.setPreferredSize(new Dimension(120, 20));
 
 		model = new DefaultTableModel(data, columns);
 
 		tableData = data;
 		table = new JTable(model) {
 			public boolean isCellEditable(int nRow, int nCol) {
 				return false;
 			}
 		};
 
 		tableScrollPane = new JScrollPane(table);
 		tabArea.addTab("Script Info", tableScrollPane);
 		tableScrollPane.setPreferredSize(new Dimension(300, 150));
 
 		setContentPane(contentPane);
 
 		// contentPane.setBorder(BorderFactory.createTitledBorder("Script Info"));
 		contentPane.add(scriptLabel);
 		contentPane.add(scriptValueLabel);
 
 		contentPane.add(runtimeLabel);
 		contentPane.add(runtimeValueLabel);
 
 		contentPane.add(statusLabel);
 		contentPane.add(statusValueLabel);
 
 		contentPane.add(tabArea);
 
 		pack();
 		setVisible(true);
 
 	}
 
 	public void updateRows(final Object[][] data) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				model.setDataVector(data, columns);
 				model.fireTableDataChanged();
 			}
 		});
 	}
 
 	public Object[][] getTableData() {
 		return this.tableData;
 
 	}
 
 	String getTimeRunning() {
 		return this.runTime.toElapsedString();
 	}
 
 }
 
 class WindowEventHandler extends WindowAdapter {
 	public void windowClosing(WindowEvent evt) {
 		if (Gui.parent != null) Gui.parent.shutdown();
 	}
 }
