 package util;
 
 import gui.renderer.MyCellRenderer;
 import gui.renderer.MyTableModel;
 import gui.renderer.RowHighlightRenderer;
 
 import java.awt.Font;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableRowSorter;
 
 @SuppressWarnings({ "rawtypes", "serial" })
 public class SBTable extends JTable implements MouseListener {
 
 	final RowHighlightRenderer renderer = new RowHighlightRenderer();
 	GradientTableHeader gradientTableHeader = new GradientTableHeader();
 	private TableRowSorter<MyTableModel> sorter;
 	private MyTableModel model;
 	private List objects;
 	private String tableName;
 	private Object[][] data;
 	private String[] header;
 
 	public SBTable(Object[][] data, String[] header, List objects, String tableName) {
 		// TODO Auto-generated constructor stub
 
 		this.data = data;
 		this.header = header;
 		this.objects = objects;
 		this.tableName = tableName;
 		// gradientTableHeader.setColumnModel(this.getColumnModel());
 		// setTableHeader(gradientTableHeader);
 
 		init();
 		addMouseListener(this);
 	}
 
 	public SBTable(Object[][] data, String[] header) {
 		// TODO Auto-generated constructor stub
 
 		this.data = data;
 		this.header = header;
 		
 		tableName = "HISTORY";
 
 		init();
 	}
 
 	private void init() {
 		// TODO Auto-generated method stub
 		getTableHeader().setDefaultRenderer(new MyCellRenderer());
 
 		model = new MyTableModel(data, header);
 
 		setModel(model);
 		setName(tableName);
 
 		getTableHeader().setReorderingAllowed(false);
 		setFocusable(false);
 		setShowGrid(false);
 		setOpaque(false);
 		setFont(new Font("Lucida Sans", Font.PLAIN, 12));
 
 		sorter = new TableRowSorter<MyTableModel>(model);
 		setRowSorter(sorter);
 
 		setDefaultRenderer(Object.class, renderer);
 	}
 
 	public List getObjects() {
 		return objects;
 	}
 
 	public TableRowSorter<MyTableModel> getSorter() {
 		return sorter;
 	}
 
 	class GradientTableHeader extends JTableHeader {
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		if (arg0.getClickCount() == 2 && !Values.tableUtilPanel.getLabel().equals(Tables.LOGS)) {
 
 			/*
 			 * if (!(Manager.loggedInAccount != null &&
 			 * !Manager.loggedInAccount.getAccountType().getName()
 			 * .equals(AccountType.manager) &&
 			 * Values.tableUtilPanel.getLabel().equals("ACCOUNTS"))){
 			 */
 			JTable target = (JTable) arg0.getSource();
 			int row = target.getSelectedRow();
 			row = target.convertRowIndexToModel(row);
 
 			// do some action
 
 			// System.out.println(getValueAt(row, column));
 			// new EditItemPopup().setVisible(true);
 			Values.editPanel.setHide(false);
 			Values.editPanel.startAnimation();
 			Values.editPanel.showComponent(objects.get(row));
 			// Values.editPanel.showComponent(objects.get(row));
 			// Values.editPanel.showComponent(null);
 			// }
 		}
 
 		if (arg0.getClickCount() == 1 && (Values.tableUtilPanel.getLabel().equals(Tables.LOGS))) {
 			JTable target = (JTable) arg0.getSource();
 			int row = target.getSelectedRow();
 
 			// setToolTipText(getValueAt(convertRowIndexToModel(row),
 			// column).toString());
 
			JOptionPane.showMessageDialog(null, getValueAt(convertRowIndexToModel(row), 1).toString(), getValueAt(convertRowIndexToModel(row), 0).toString(), JOptionPane.INFORMATION_MESSAGE);
 
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 }
