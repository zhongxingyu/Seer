 package djproject.gui.dj;
 
 import java.awt.Component;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 import djproject.rest.RESTHandler;
 import djproject.songs.Song;
 import djproject.wishes.Wish;
 
 import net.miginfocom.swing.MigLayout;
 
 public class WishPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5607180063105514228L;
 	
 	MigLayout layout = new MigLayout("insets 0 0 0 0");
 	MigLayout layout2 = new MigLayout();
 	
 	DefaultTableModel tableModelSongs = new DefaultTableModel(){
 		private static final long serialVersionUID = 1L;
 		@Override
 	    public boolean isCellEditable(int row, int column) {
 	       //all cells false
 	       return false;
 	    }
 	};
 	JTable table_songs = new JTable(tableModelSongs);
 
 	private int id;
 	
 	public WishPanel(ListenerHandler_main listener, boolean dragable) {
 		
 		setLayout(layout);
 		
 		table_songs.setAutoCreateRowSorter(true);
 		table_songs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		table_songs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 				
 		if(dragable) {
 			table_songs.setDragEnabled(true);
 			table_songs.setTransferHandler(new TableTransferHandler());
 		} else {
 			table_songs.addMouseListener(listener);
 		}
 		
 		table_songs.getTableHeader().setReorderingAllowed(false);
 		
 		tableModelSongs.addColumn("Count");
 		tableModelSongs.addColumn("Artist");
 		tableModelSongs.addColumn("Title");
 		tableModelSongs.addColumn("Album");
 		tableModelSongs.addColumn("Genre");
 		tableModelSongs.addColumn("Length");
 		tableModelSongs.addColumn("ID");
 		
 		updateSongList();
 		
 		this.add(new JScrollPane(table_songs), "width 530!, height 180!");
 
 	}
 	
 	public void updateSongList() {
		table_songs.setRowSorter(null);
 		tableModelSongs.getDataVector().removeAllElements();
 		for(Wish w: RESTHandler.getWishes().getWish()) {
 			Vector<String> v = new Vector<String>();
 			v.removeAllElements();
 			id = w.getId();
 			v.add(String.valueOf(w.getCount()));
			Song s = RESTHandler.getSongById(w.getSongId());
 			v.add(s.getArtist());
 			v.add(s.getTitle());
 			v.add(s.getAlbum());
 			v.add(s.getGenre());
 			Format formatter = new SimpleDateFormat( "mm:ss" );
 			v.add(String.valueOf(formatter.format((s.getLength()*1000))));
 			v.add(String.valueOf(w.getId()));
			System.out.println("" + tableModelSongs.getColumnCount());
			System.out.println("" + v.size());
 			tableModelSongs.addRow(v);
 		}
 		
 		autoFitTable();
 		tableModelSongs.fireTableDataChanged();
		table_songs.setAutoCreateRowSorter(true);
 
 	}
 
 	public void autoFitTable() {
 		for(int j=0; j < table_songs.getColumnModel().getColumnCount(); j++) {
 
 			//Set the width to be the header width of this column
 			TableCellRenderer headerRenderer = table_songs.getTableHeader().getDefaultRenderer();
 			TableColumn column = table_songs.getColumnModel().getColumn(j);
 			Component comp = headerRenderer.getTableCellRendererComponent(table_songs, column.getHeaderValue(),	false, false, 0, 0);
 			int width = comp.getPreferredSize().width;
 	
 			//Set the width to be the larger of the header or a cell width in this column
 			for (int i=0; i< table_songs.getRowCount(); i++)
 			{
 				TableCellRenderer renderer = table_songs.getCellRenderer(i, j);
 				Component c = renderer.getTableCellRendererComponent(table_songs, table_songs.getValueAt(i, j), false, false, i, j);
 				width = Math.max(width, c.getPreferredSize().width);
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
 	
 	public void replaceColumn(int index, String newname) {
 
 		JTableHeader th = table_songs.getTableHeader();
 		TableColumnModel tcm = th.getColumnModel();
 		TableColumn tc = tcm.getColumn(index);
 		tc.setHeaderValue(newname);
 		
 		autoFitTable();
 	
 	}
 	
 	public void sortByColumn(int index) {
 		RowSorter<? extends TableModel> sorter = table_songs.getRowSorter();
     	ArrayList<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>();
     	list.add( new RowSorter.SortKey(index, SortOrder.DESCENDING) );
     	sorter.setSortKeys(list);
 	}
 
 }
 
