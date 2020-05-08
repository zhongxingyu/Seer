 package djproject.gui.dj;
 
 import java.awt.Component;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
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
 
 import net.miginfocom.swing.MigLayout;
 
 public class SongChoosePanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5607180063105514228L;
 	
 	MigLayout layout = new MigLayout("insets 0 0 0 0");
 	MigLayout layout2 = new MigLayout();
 	
 	JLabel label_filtertype = new JLabel();
 	JLabel label_filtertext = new JLabel();
 	
 	JButton btn_filter = new JButton("Filter");
 	JButton btn_filterreset = new JButton("Reset");
 	
 	JPanel panel_filter = new JPanel(layout2);
 
 	DefaultComboBoxModel<String> cboxModelFilter = new DefaultComboBoxModel<>();
 	JComboBox<String> cbox_filter = new JComboBox<String>(cboxModelFilter);
 	
 	JTextField txt_filter = new JTextField();
 	
 	DefaultTableModel tableModelSongs = new DefaultTableModel(){
 		private static final long serialVersionUID = 1L;
 		@Override
 	    public boolean isCellEditable(int row, int column) {
 	       //all cells false
 	       return false;
 	    }
 	};
 	JTable table_songs = new JTable(tableModelSongs);
 	
 	public SongChoosePanel(ListenerHandler_main listener, boolean dragable) {
 		
 		setLayout(layout);
 		
 		label_filtertype.setText("Filter by:");
 		label_filtertext.setText("Filter keyword:");
 		
 		cboxModelFilter.addElement("Artist");
 		cboxModelFilter.addElement("Title");
 		cboxModelFilter.addElement("Genre");
 		
 		JLabel dummy = new JLabel();
 		
 		panel_filter.add(label_filtertype, "width 150!, height 30!, span 3, wrap");
 		panel_filter.add(cbox_filter, "width 150!, height 30!, span 3, wrap");
 		panel_filter.add(label_filtertext, "width 150!, height 30!, span 3, wrap");
 		panel_filter.add(txt_filter, "width 150!, height 30!, span 3, wrap");
 		panel_filter.add(btn_filter, "width 65!, height 30!, span 1");
 		panel_filter.add(dummy, "width 10!, height 30!, span 1");
 		panel_filter.add(btn_filterreset, "width 65!, height 30!, span 3");
 		
 		btn_filter.addActionListener(listener);
 		btn_filterreset.addActionListener(listener);
 		txt_filter.addActionListener(listener);
 		
 		
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
 		
 		tableModelSongs.addColumn("ID");
 		tableModelSongs.addColumn("Artist");
 		tableModelSongs.addColumn("Title");
 		tableModelSongs.addColumn("Album");
 		tableModelSongs.addColumn("Album Artist");
 		tableModelSongs.addColumn("Number in Album");
 		tableModelSongs.addColumn("Genre");
 		tableModelSongs.addColumn("Length");
 		
 		updateSongList();
 		
 		this.add(new JScrollPane(table_songs), "width 350!, height 180!");
 		this.add(panel_filter, "width 200!, height 180!");
 
 	}
 	
 	public void updateSongList() {
 		tableModelSongs.getDataVector().removeAllElements();
 		for(Song s: RESTHandler.getSongs(String.valueOf(cbox_filter.getSelectedItem()), txt_filter.getText()).getSong()) {
 			Vector<String> v = new Vector<String>();
 			v.add(String.valueOf(s.getId()));
 			v.add(s.getArtist());
 			v.add(s.getTitle());
 			v.add(s.getAlbum());
 			v.add(s.getAlbumArtist());			
 			v.add((s.getNumberInAlbum() == null) ? "" : String.valueOf(s.getNumberInAlbum()));
 			v.add(s.getGenre());
 			Format formatter = new SimpleDateFormat( "mm:ss" );
 			v.add(String.valueOf(formatter.format((s.getLength()*1000))));
 			tableModelSongs.addRow(v);
 		}
 		
 		autoFitTable();
 		tableModelSongs.fireTableDataChanged();
 
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
