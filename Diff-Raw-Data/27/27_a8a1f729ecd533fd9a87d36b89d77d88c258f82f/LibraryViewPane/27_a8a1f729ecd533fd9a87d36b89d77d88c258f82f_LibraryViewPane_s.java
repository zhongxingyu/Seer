 package house.neko.media.slave;
 
 import house.neko.util.TableSorter;
 import house.neko.media.common.Media;
 import house.neko.media.common.LibrarySearchResult;
 import house.neko.media.common.LibraryView;
 import house.neko.media.common.ConfigurationManager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 
 import java.io.IOException;
 
 import javax.swing.Action;
 import javax.swing.AbstractAction;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.event.TableModelListener;
 
 import org.apache.commons.logging.Log;
 
 import org.apache.commons.configuration.HierarchicalConfiguration;
 
 /**
  *
  * @author andy
  */
 public class LibraryViewPane extends JScrollPane implements TableModel, ListSelectionListener, java.util.Observer
 {
 	private static final long serialVersionUID = 6L;
 
 	private Slave slave;
 	private house.neko.media.common.LibraryView libraryView;
 	
 	private Log log = null;
 	private HierarchicalConfiguration config;
 
 	private LibrarySearchResult result;
 	private TableModelListener listeners[];
 	private boolean listnerLock = true;
 
 	private JTable table;
 	private String[] columnHeaders = { };
 	private Class[] columnClasses = { };
 	private TableColumn[] columns = { };
 	private TableSorter tableSorter;
 
 	/**
 	 *
 	 * @param mms
 	 */
 	public LibraryViewPane(Slave mms, HierarchicalConfiguration config, house.neko.media.common.LibraryView libraryView)
 	{
 		this(new JTable(), mms, config, libraryView);
 	}
 
 	private LibraryViewPane(JTable t, Slave mms, HierarchicalConfiguration config, house.neko.media.common.LibraryView libraryView)
 	{
 		super(t);
 		slave = mms;
 		this.config = config;
 		this.libraryView = libraryView;
 		table = t;
 		
 		this.log = ConfigurationManager.getLog(getClass());
 		
 		setViewportView(table);
 
 		tableSorter = new TableSorter(this);  // sets model
 		tableSorter.setTableHeader(table.getTableHeader());
 		
 		table.setModel(tableSorter);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		ListSelectionModel rowSM = table.getSelectionModel();
 		rowSM.addListSelectionListener(this);
 		
 		columnHeaders = libraryView.getColumnHeaders();
 		if(columnHeaders == null || columnHeaders.length < 1)
 		{
 			columnHeaders = new String[2];
 			columnHeaders[0] = "Title";
 			columnHeaders[1] = "Artist"; 
 		}
 		columnClasses = new Class[columnHeaders.length];
 		columns = new TableColumn[columnHeaders.length];
 		for(int i = 0; i < columns.length; i++)
 		{
 			columnClasses[i] = columnHeaders[i].getClass();
 			columns[i] = new TableColumn(i);
 			columns[i].setIdentifier(columnHeaders[i]);
 			table.addColumn(columns[i]);
 		}
 		result = libraryView.getVisibleMedia();
 		libraryView.addObserver(this);
 		
 		listeners = new TableModelListener[0];
 		
 		tableSorter.setTableHeader(table.getTableHeader());
 		
         /*String javaVersion = System.getProperty("java.version");
 		if(javaVersion != null && javaVersion.matches("^1\\.6.*$"))
 		{	// this is 1.6 only 
 			table.setAutoCreateRowSorter(true);
 		}*/
 		
 		
 		// assign mouse listener
 		addMouseListener(new LibraryMouseListener(this));
 		
 		if(log.isTraceEnabled())
         {	log.trace("Library view created"); }
 	}
 
 	// house.neko.media.common.LibraryView
 	/**
 	 *
 	 * @param r
 	 */
 	synchronized public void setResult(LibrarySearchResult r)
 	{
 		if(log.isTraceEnabled())
 		{	log.trace("Setting LibrarySearchResults: " + r); }
 		result = r;
 		table.tableChanged(new TableModelEvent(this, 0, result.results.length, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
 		if(log.isTraceEnabled())
 		{	log.trace("Updating results to " + r.results.length); }
 	}
 	
 	public LibraryView getView()
 	{	return libraryView; }
 
 	// TableModel
 	/**
 	 *
 	 * @param l
 	 */
 	synchronized public void addTableModelListener(TableModelListener l)
 	{
 		if(log.isTraceEnabled())
 		{	log.trace("Adding TableModelListener"); }
 		if(listeners == null || listeners.length < 1)
 		{
 			listeners = new TableModelListener[1];
 			listeners[0] = l;
 		} else {
 			TableModelListener[] t = new TableModelListener[listeners.length + 1];
 			System.arraycopy(listeners, 0, t, 0, listeners.length);
 			t[listeners.length] = l;
 			listeners = t;
 		}
 	}
 
 	// TableModel
 	/**
 	 *
 	 * @param columnIndex
 	 * @return
 	 */
 	public Class getColumnClass(int columnIndex)
 	{	return columnClasses[columnIndex]; }
 
 	// TableModel
 	/**
 	 *
 	 * @return
 	 */
 	public int getColumnCount()
 	{	return columnHeaders.length; }
 
 	// TableModel
 	/**
 	 *
 	 * @param columnIndex
 	 * @return
 	 */
 	public String getColumnName(int columnIndex)
 	{	return columnHeaders[columnIndex]; }
 
 	// TableModel
 	/**
 	 *
 	 * @return
 	 */
 	public int getRowCount()
 	{
 		return result.results.length;
 	}
 
 	// TableModel
 	/**
 	 *
 	 * @param rowIndex
 	 * @param columnIndex
 	 * @return
 	 */
 	public Object getValueAt(int rowIndex, int columnIndex)
 	{	return result.results[rowIndex][columnIndex]; }
 
 	// TableModel
 	/**
 	 *
 	 * @param rowIndex
 	 * @param columnIndex
 	 * @return
 	 */
 	public boolean isCellEditable(int rowIndex, int columnIndex)
 	{	return false; }
 
 	// TableModel
 	/**
 	 *
 	 * @param l
 	 */
 	public void removeTableModelListener(TableModelListener l)
 	{
 		if(log.isTraceEnabled())
 		{	log.trace("Removing TableModelListener"); }
 		synchronized(this)
 		{
 			int index = -1;
 			for(int i = 0; i < listeners.length; i++)
 			{	if(l == listeners[index]) {index = i; break; } }
 			if(index > -1)
 			{
 				if(listeners.length > 1)
 				{
 					TableModelListener[] t = new TableModelListener[listeners.length - 1];
 					System.arraycopy(listeners, 0, t, 0, index);
 					if(index < t.length)
 					{	System.arraycopy(listeners, index + 1, t, index, listeners.length - index); }
 					listeners = t;
 				} else {
 					listeners = new TableModelListener[0];
 				}
 			}
 		}
 	}
 
 	// TableModel
 	/**
 	 *
 	 * @param aValue
 	 * @param rowIndex
 	 * @param columnIndex
 	 */
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
 	{	result.results[rowIndex][columnIndex] = aValue; }
 
 	//ListSelectionListener
 	/**
 	 *
 	 * @param e
 	 */
 	public void valueChanged(ListSelectionEvent e)
 	{
 		//Ignore extra messages.
 		if(e.getValueIsAdjusting()) { return; }
 
 		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
 		if(lsm.isSelectionEmpty())
 		{
 			//no rows are selected
 			if(log.isTraceEnabled())
 			{	log.trace("Selection cleared"); }
 		} else {
 			//selectedRow is selected
 			int selectedRow = lsm.getMinSelectionIndex();
			Media m = (Media) result.results[tableSorter.modelIndex(selectedRow)][2];
 			if(log.isTraceEnabled())
 			{	log.trace("Selection changed to row " + selectedRow + ", is " + m); }
 			slave.play(m);
 		}
 	}
 	
 	public void openInfoForSelected()
 	{
 		int selectedRow = table.getSelectedRow();
 		if(selectedRow < 0)
 		{
 			if(log.isTraceEnabled())
 			{	log.trace("Skipping opening info for selected, nothing selected?!"); }
 			return; 
 		}
		Media m = (Media) result.results[tableSorter.modelIndex(selectedRow)][2];
 		if(log.isTraceEnabled())
 		{	log.trace("Opening info dialog for track " + m); }
 		MediaTrackInfoDialog d = new MediaTrackInfoDialog(slave, m);
 		d.setVisible(true);
 	}
 	
 	public Media[] getSelectedMedia()
 	{
 		int selectedRow = table.getSelectedRow();
 		if(selectedRow < 0)
 		{
 			if(log.isTraceEnabled())
 			{	log.trace("Skipping syncing for selected, nothing selected?!"); }
 			return null; 
 		}
 		Media[] m = new Media[1];
		m[0] = (Media) result.results[tableSorter.modelIndex(selectedRow)][2];
 		return m;
 	}
 	
 	public Action[] getActions()
 	{
 		int actionCount = 0;
 		Action[] allActions = new Action[actionCount];
 		return allActions;
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{
 		if(log.isDebugEnabled())
 		{	log.debug("Action " + e.getActionCommand()); }
 		
 	}
 	
 	public static class LibraryMouseListener extends MouseAdapter
 	{
 		private LibraryViewPane view;
 		
 		public LibraryMouseListener(LibraryViewPane view)
 		{	this.view = view; }
 		
 		public void mouseClicked(MouseEvent e)
 		{
 			if(e.getClickCount() > 1)
 			{
 				if(view.log.isDebugEnabled())
 				{	view.log.debug("was double-clicked!"); }
 				view.openInfoForSelected();
 			}
 		}
 	}
 	
 	public class ChangeColumnAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			
 		}
 	}
 	
 	public void update(java.util.Observable o, Object arg)
 	{
 		System.out.println("LibraryViewPane was updated : " + o + " -> " + arg);
 		setResult(libraryView.getVisibleMedia());
 		//table.tableChanged(new TableModelEvent(this, 0, result.results.length, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
 		if(log.isTraceEnabled())
 		{	log.trace("Forcing update results from " + o); }
 	}
 }
