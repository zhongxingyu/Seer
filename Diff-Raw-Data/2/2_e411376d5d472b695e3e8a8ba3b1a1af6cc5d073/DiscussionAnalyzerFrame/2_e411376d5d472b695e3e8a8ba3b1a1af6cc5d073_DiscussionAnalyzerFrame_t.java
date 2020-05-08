 package org.computer.knauss.reqtDiscussion.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableRowSorter;
 
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 import org.computer.knauss.reqtDiscussion.model.VisualizationConfiguration;
 import org.computer.knauss.reqtDiscussion.model.metric.AbstractDiscussionMetric;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.HighlightRelatedDiscussions;
 import org.computer.knauss.reqtDiscussion.ui.uiModel.DiscussionTableModel;
 import org.computer.knauss.reqtDiscussion.ui.visualization.ZoomPanel;
 import org.computer.knauss.reqtDiscussion.ui.visualization.sna.NetworkFrame;
 
 public class DiscussionAnalyzerFrame extends JFrame implements
 		TableModelListener, ListSelectionListener {
 	public static final int DATA_MENU = 0;
 	public static final int EDIT_MENU = 1;
 	public static final int ACTION_MENU = 2;
 	public static final int STATISTICS_MENU = 3;
 	public static final int HELP_MENU = 4;
 
 	private static final long serialVersionUID = 1L;
 	private DiscussionTableModel tableModel;
 	private JTable table;
 	private JMenuBar menu;
 	private JMenu dataMenu;
 	private DiscussionVisualizationPanel visualizer;
 	private EditClassificationFrame editClassificationFrame;
 	private VisualizationConfigurationPanel configureVisualizationPanel;
 	private JMenu editMenu;
 	private JMenu statisticMenu;
 	private JMenu helpMenu;
 	private JMenu actionMenu;
 	private AbstractAction detailsAction = new AbstractAction(
 			"Edit discussion details") {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			Discussion[] selectedDiscussion = tableModel
 					.getSelectedDiscussions();
 			if (selectedDiscussion == null)
 				return;
 			getEditClassificationFrame().setDiscussions(selectedDiscussion);
 			getEditClassificationFrame().setVisible(true);
 		}
 	};
 	private AbstractAction socialNetworkAnalysis = new AbstractAction(
 			"Show social network") {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			networkAnalysisFrame.setWorkitems(tableModel
 					.getSelectedDiscussions());
 			networkAnalysisFrame.setVisible(true);
 		}
 
 	};
 	private JLabel infoLabel;
 	private NetworkFrame networkAnalysisFrame;
 	private HighlightRelatedDiscussions highlightRelated;
 
 	// private SocialNetwork sn = new PartitionedSocialNetwork();
 
 	public DiscussionAnalyzerFrame(VisualizationConfiguration configuration) {
 		super("V:issue:lizer");
 
 		setLayout(new BorderLayout());
 
 		this.networkAnalysisFrame = new NetworkFrame(configuration);
 
 		this.table = new JTable();
 		add(new JScrollPane(this.table), BorderLayout.WEST);
 
 		this.menu = new JMenuBar();
 		this.dataMenu = new JMenu("Data");
 		this.menu.add(this.dataMenu);
 		add(this.menu, BorderLayout.NORTH);
 
 		this.configureVisualizationPanel = new VisualizationConfigurationPanel(
 				configuration);
 		JPanel flowPanel = new JPanel();
 		flowPanel.add(this.configureVisualizationPanel);
 		add(flowPanel, BorderLayout.EAST);
 
 		// add the visualization panel
 		this.visualizer = new DiscussionVisualizationPanel(
 				this.configureVisualizationPanel);
 
 		// add zooming to the visualization panel
 		JScrollPane scroller = new JScrollPane(this.visualizer);
 		JPanel centerPane = new JPanel(new BorderLayout());
 		centerPane.add(scroller, BorderLayout.CENTER);
 		ZoomPanel zoomPanel = new ZoomPanel();
 		zoomPanel.setZoomable(this.visualizer);
 		zoomPanel.setZoomableParent(scroller);
 		centerPane.add(zoomPanel, BorderLayout.SOUTH);
 		scroller.setPreferredSize(this.visualizer.getPreferredSize());
 		scroller.setBackground(Color.WHITE);
 
 		add(centerPane, BorderLayout.CENTER);
 
 		this.table.getSelectionModel()
 				.addListSelectionListener(this.visualizer);
 		this.table.getSelectionModel().addListSelectionListener(this);
 		this.infoLabel = new JLabel();
 		add(this.infoLabel, BorderLayout.SOUTH);
 
 		this.table.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getComponent().isEnabled()
 						&& e.getButton() == MouseEvent.BUTTON1
 						&& e.getClickCount() == 2) {
 					// Point p = e.getPoint();
 					// int row = table.rowAtPoint(p);
 					// int col = table.columnAtPoint(p);
 					// Use table.convertRowIndexToModel /
 					// table.convertColumnIndexToModle to convert to view
 					// indices
 					// row = table.convertRowIndexToModel(row);
 					// col = table.convertColumnIndexToModel(col);
 					// table.setRowSelectionInterval(row, row);
 					detailsAction.actionPerformed(null);
 					// ...
 				}
 			}
 		});
 
 		addAction(EDIT_MENU, detailsAction);
 		addAction(EDIT_MENU, socialNetworkAnalysis);
 	}
 
 	public EditClassificationFrame getEditClassificationFrame() {
 		if (this.editClassificationFrame == null) {
 			this.editClassificationFrame = new EditClassificationFrame();
 		}
 		return this.editClassificationFrame;
 	}
 
 	public void setTableModel(DiscussionTableModel tableModel) {
 		if (this.tableModel != null) {
 			this.tableModel.removeTableModelListener(this);
 		}
 		this.tableModel = tableModel;
 		this.tableModel.addTableModelListener(this);
 		this.table.setModel(this.tableModel);
 		TableRowSorter<DiscussionTableModel> sorter = new TableRowSorter<DiscussionTableModel>(
 				tableModel);
 		this.table.setRowSorter(sorter);
 		this.tableModel.setTable(this.table);
 		this.visualizer.setTableModel(this.tableModel);
 	}
 
 	public void addAction(int menuType, AbstractAction a) {
 		if (DATA_MENU == menuType) {
 			this.dataMenu.add(a);
 			return;
 		}
 		if (EDIT_MENU == menuType) {
 			getEditMenu().add(a);
 			return;
 		}
 		if (STATISTICS_MENU == menuType) {
 			getStatisticsMenu().add(a);
 			return;
 		}
 		if (HELP_MENU == menuType) {
 			getHelpMenu().add(a);
 			return;
 		}
 		this.getActionMenu().add(a);
 	}
 
 	public void addSeperator(int menuType, String seperatorName) {
 		JSeparator sep = new JSeparator();
 		sep.setName(seperatorName);
 		if (DATA_MENU == menuType) {
 			this.dataMenu.add(sep);
 			return;
 		}
 		if (EDIT_MENU == menuType) {
 			getEditMenu().add(sep);
 			return;
 		}
 		if (STATISTICS_MENU == menuType) {
 			getStatisticsMenu().add(sep);
 			return;
 		}
 		if (HELP_MENU == menuType) {
 			getHelpMenu().add(sep);
 			return;
 		}
 		this.getActionMenu().add(sep);
 	}
 
 	private JMenu getEditMenu() {
 		if (this.editMenu == null) {
 			this.editMenu = new JMenu("Edit");
 			this.menu.add(this.editMenu);
 		}
 		return this.editMenu;
 	}
 
 	private JMenu getStatisticsMenu() {
 		if (this.statisticMenu == null) {
 			this.statisticMenu = new JMenu("Statistics");
 			this.menu.add(this.statisticMenu);
 		}
 		return this.statisticMenu;
 	}
 
 	private JMenu getHelpMenu() {
 		if (this.helpMenu == null) {
 			this.helpMenu = new JMenu("Help");
 			this.menu.add(this.helpMenu);
 		}
 		return this.helpMenu;
 	}
 
 	private JMenu getActionMenu() {
 		if (this.actionMenu == null) {
 			this.actionMenu = new JMenu("Actions");
 			this.menu.add(this.actionMenu);
 		}
 		return this.actionMenu;
 	}
 
 	public void removeAction(AbstractAction a) {
 
 	}
 
 	@Override
 	public void tableChanged(TableModelEvent event) {
 		Discussion[] selected = this.tableModel.getSelectedDiscussions();
 		// get the standard metrics
 		StringBuffer metrics = new StringBuffer();
 		for (AbstractDiscussionMetric m : AbstractDiscussionMetric.STANDARD_METRICS) {
 			metrics.append(" | ");
 			metrics.append(m.getName());
 			metrics.append(" = ");
 			metrics.append(m.getDecimalFormat().format(
 					m.considerDiscussions(selected)));
 		}
 
 		int x = 0;
 		int y = 0;
 		for (Discussion d : selected) {
 			for (DiscussionEvent de : d.getDiscussionEvents()) {
				if (de.getReferenceClassification().startsWith("clari"))
 					x++;
 				else
 					y++;
 			}
 		}
 		this.infoLabel.setText(this.tableModel.getRowCount()
 				+ " discussions | " + selected.length + " selected | " + x
 				+ " clarification | " + y + " other" + metrics.toString());
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent arg0) {
 		if (configureVisualizationPanel.isShowRelatedDiscussions()) {
 			try {
 				if (this.highlightRelated == null)
 					this.highlightRelated = new HighlightRelatedDiscussions();
 
 				this.highlightRelated.highlightRelatedDiscussions(tableModel);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		configureVisualizationPanel.getDiscussionPartition().setTimeInterval(
 				tableModel.getSelectedDiscussions());
 		networkAnalysisFrame.setWorkitems(tableModel.getSelectedDiscussions());
 		tableChanged(null);
 	}
 
 	public DiscussionVisualizationPanel getDiscussionVisualizationPanel() {
 		return this.visualizer;
 	}
 }
