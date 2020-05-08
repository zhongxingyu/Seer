 package djproject.gui.dj.gui;
 
 import java.awt.Component;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

 import djproject.comments.Comment;
import djproject.gui.dj.utils.DateStringComparator;
 import djproject.gui.dj.utils.RESTHandler;
 import net.miginfocom.swing.MigLayout;
 
 public class CommentPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5607180063105514228L;
 	
 	MigLayout layout = new MigLayout("insets 0 0 0 0");
 	MigLayout layout2 = new MigLayout();
 	
 	DefaultTableModel tableModelComments = new DefaultTableModel(){
 		private static final long serialVersionUID = 1L;
 		@Override
 	    public boolean isCellEditable(int row, int column) {
 	       //all cells false
 	       return false;
 	    }
 	};
 	JTable table_comments = new JTable(tableModelComments);
	TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(tableModelComments);
 
 	private int id;
 	
 	public CommentPanel(ListenerHandler_main listener) {
 		
 		setLayout(layout);
 		
 		table_comments.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		table_comments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 				
 		table_comments.addMouseListener(listener);
 		
 		table_comments.getTableHeader().setReorderingAllowed(false);
 		
 		tableModelComments.addColumn("Rating");
 		tableModelComments.addColumn("Author");
 		tableModelComments.addColumn("Content");
 		tableModelComments.addColumn("Time");
 		tableModelComments.addColumn("ID");
 		
 		//table_comments.setRowSorter(sorter);
 		table_comments.setAutoCreateRowSorter(true);
		sorter.setComparator(3, new DateStringComparator());
 		
 		updateCommentList();
 		
 		this.add(new JScrollPane(table_comments), "width 530!, height 180!");
 
 	}
 	
 	public void updateCommentList() {
 		table_comments.clearSelection();
		//table_comments.setRowSorter(null);		
 		tableModelComments.getDataVector().removeAllElements();
 		for(Comment c: RESTHandler.getComments().getComment()) {
 			Vector<Object> v = new Vector<Object>();
 			v.removeAllElements();
 			id = c.getId();
 			v.add(String.valueOf(c.getRating()));
 			v.add(c.getAuthor());
 			v.add(c.getContent());
 			Format formatter = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
 			v.add(String.valueOf(formatter.format((c.getTime().toGregorianCalendar().getTime()))));
 			//v.add(c.getTime().toGregorianCalendar().getTime());
 			v.add(String.valueOf(id));
 			tableModelComments.addRow(v);
 		}
 		
 		tableModelComments.fireTableDataChanged();
		
 		//table_comments.setRowSorter(sorter);
 		autoFitTable();
 		//sortByColumn(3);
 
 	}
 
 	public void autoFitTable() {
 		for(int j=0; j < table_comments.getColumnModel().getColumnCount(); j++) {
 
 			//Set the width to be the header width of this column
 			TableCellRenderer headerRenderer = table_comments.getTableHeader().getDefaultRenderer();
 			TableColumn column = table_comments.getColumnModel().getColumn(j);
 			Component comp = headerRenderer.getTableCellRendererComponent(table_comments, column.getHeaderValue(),	false, false, 0, 0);
 			int width = comp.getPreferredSize().width;
 	
 			//Set the width to be the larger of the header or a cell width in this column
 			for (int i=0; i< table_comments.getRowCount(); i++)
 			{
 				
 				try {
 						TableCellRenderer renderer = table_comments.getCellRenderer(i, j);
 						Component c = renderer.getTableCellRendererComponent(table_comments, table_comments.getValueAt(i, j), false, false, i, j);
 						width = Math.max(width, c.getPreferredSize().width);
 				} catch(ArrayIndexOutOfBoundsException e) {
 					
 				}
 				
 			}
 	
 			//Usethe width of the largest component (header or cell) plus a margin on other side.
 			column.setMinWidth(width + 14);
 			column.setMaxWidth(width + 14);
 			column.setPreferredWidth(width + 14);
 		}
 	}
 	
 	public int getId() {
 		return this.id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 //	public void replaceColumn(int index, String newname) {
 //
 //		JTableHeader th = table_comments.getTableHeader();
 //		TableColumnModel tcm = th.getColumnModel();
 //		TableColumn tc = tcm.getColumn(index);
 //		tc.setHeaderValue(newname);
 //		
 //		autoFitTable();
 //	
 //	}
 //	
 //	public void sortByColumn(int index) {
 //		//RowSorter<? extends TableModel> sorter = table_comments.getRowSorter();
 //    	ArrayList<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>();
 //    	list.add( new RowSorter.SortKey(index, SortOrder.DESCENDING) );
 //    	sorter.setSortKeys(list);
 //	}
 
 }
 
 
