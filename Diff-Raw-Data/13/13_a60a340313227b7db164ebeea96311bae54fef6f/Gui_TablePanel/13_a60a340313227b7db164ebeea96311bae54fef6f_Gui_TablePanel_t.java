 package gui;
 /* This program is licensed under the terms of the GPL V3 or newer*/
 /* Written by Johannes Putzke*/
 /* eMail: die_eule@gmx.net*/ 
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 import misc.Stream;
 
 import thread.AudioPlayer;
 
 import control.Control_Stream;
 import control.SRSOutput;
 
 public class Gui_TablePanel extends JPanel
 {
 	private static final long serialVersionUID = 1L;
 	
 	
 
 	private Object[] tableheader ={"Rec","Stream","Aktual Title"};
 	private String[][] Daten = {};
 	private DefaultTableModel model = new DefaultTableModel(Daten,tableheader)
 		{private static final long serialVersionUID = 1L;
 		public boolean isCellEditable(int rowIndex, int columnIndex){return false;}
 		@Override
 		public Class<?> getColumnClass(int col) {
 			return col == 0 ? ImageIcon.class : Object.class;
 		}};
 	private Control_Stream controlStreams;
 	private Gui_JTTable table = new Gui_JTTable(model);
 	
 	private ImageIcon recordSmallIcon = new ImageIcon((URL)getClass().getResource("/Icons/record_middle.png"));
 	private JLabel columnLabel0 = new JLabel(recordSmallIcon,JLabel.CENTER);
 	private JLabel columnLabel1 = new JLabel(tableheader[1].toString(),JLabel.CENTER);
 	private JLabel columnLabel2 = new JLabel(tableheader[2].toString(),JLabel.CENTER);
 	private TableCellRenderer renderer = new JComponentTableCellRenderer();
 	private TableColumnModel columnModel = table.getColumnModel();
 	private TableColumn column0 = columnModel.getColumn(0);
 	private TableColumn column1 = columnModel.getColumn(1);
 	private TableColumn column2 = columnModel.getColumn(2);
 	
 	private Gui_StreamRipStar mainGui = null;
 	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
 	
 	private ImageIcon recordIcon = new ImageIcon((URL)getClass().getResource("/Icons/record_middle.png"));
 	
 	private JPopupMenu popup;
 	private AudioPlayer player;
 	
 	public Gui_TablePanel(Control_Stream controlStreams,Gui_StreamRipStar mainGui) {
 		this.controlStreams = controlStreams;
 		this.mainGui = mainGui;
 
 		setLayout(new BorderLayout());
     
         JScrollPane scrollPane = new JScrollPane(table);
         add(scrollPane, BorderLayout.CENTER);
         setSize(new Dimension(650,400));
         
        
         columnLabel0.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         columnLabel1.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         columnLabel2.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         
         column0.setHeaderRenderer(renderer);
         column0.setHeaderValue(columnLabel0);
         column1.setHeaderRenderer(renderer);
         column1.setHeaderValue(columnLabel1);
         column2.setHeaderRenderer(renderer);
         column2.setHeaderValue(columnLabel2);
         
 		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		table.changeSelection(0,0,true,false );
 		table.setAutoCreateRowSorter(true);
 		table.addMouseListener(new CellMouseListener());
 		
 		table.getTableHeader().setReorderingAllowed(false);
 			
 		//set a default witdh for the first column
 		column0.setMaxWidth(recordIcon.getIconWidth());
 		column0.setPreferredWidth(recordIcon.getIconWidth());
 		
 		setLanguage(); //Must be at last, because the the identifier changes
 		
 		DropTargetListener dropTargetListener = new DropTargetListener()
 		{
 			public void dragEnter(DropTargetDragEvent e) {}
 			public void dragExit(DropTargetEvent e) {}
 			public void dragOver(DropTargetDragEvent e) {}
 			public void drop(DropTargetDropEvent e) {
 				try {
 					Transferable tr = e.getTransferable();
 					DataFlavor[] flavors = tr.getTransferDataFlavors();
 					for (int i = 0; i < flavors.length; i++) {
 						
 						//for Windows: tested with Windows XP 
 						if(flavors[i].isFlavorJavaFileListType()) {
 							e.acceptDrop (DnDConstants.ACTION_COPY);
 							List<?> files = (List<?>) tr.getTransferData(flavors[i]);
 							String url = files.get(0).toString();
 							
 							if(url.endsWith(".pls") || url.endsWith(".m3u")) {
 								SRSOutput.getInstance().log("Right extension");
 								new Import_Streams(getMainGui(),getControlStreams(), url);
 							} else {
 								SRSOutput.getInstance().log("Wrong extension");
 							}
 							e.dropComplete(true);
 							return;
 						}
 						
 						//for Linux: tested with KDE 3 +4
 						if(flavors[i].isFlavorTextType()) {
 							String tmp ="";
 							e.acceptDrop (DnDConstants.ACTION_COPY);
 							Vector<InputStreamReader> x = new Vector<InputStreamReader>(0,1);
 							x.add(((InputStreamReader)tr.getTransferData(flavors[i])));
 							BufferedReader br = new BufferedReader(x.get(0));
 							while(( tmp = br.readLine()) != null) {
 								URI fileURI = new URI(tmp);
 								String url = URLDecoder.decode(fileURI.getPath(),"UTF-8");
 								if(url.endsWith(".pls") || url.endsWith(".m3u")) {
 									SRSOutput.getInstance().log("Right extension");
 									new Import_Streams(getMainGui(),getControlStreams() ,url);
 								} else {
 									SRSOutput.getInstance().log("Wrong extension");
 								}
 
 							}
 							br.close(); 
 							e.dropComplete(true);
 							return;
 						}
 					}
 				}
 				catch (Throwable t) { t.printStackTrace(); }
 				// Ein Problem ist aufgetreten
 				e.rejectDrop();
 			}
 			public void dropActionChanged( DropTargetDragEvent e) {}
 		};
 		
 		new DropTarget(table,dropTargetListener);
 		new DropTarget(this,dropTargetListener);
 		table.getRowSorter().toggleSortOrder(1);
 	}
 	
 	/**
 	 * Translate the table headers
 	 */
 	private void setLanguage() {
 		try  {
 			columnLabel1.setText(trans.getString("streamname"));
 			columnLabel2.setText(trans.getString("curTitle"));
 		} catch ( MissingResourceException e ) { 
 			SRSOutput.getInstance().logE( e.getMessage()); 
 		}
 	}
 
 	//gets the width of the "name" and "title" column
 	public int[] getColumnWidths(){
 		int[] widths = new int[3];
 		widths[0] = column0.getWidth();
 		widths[1] = column1.getWidth();
 		widths[2] = column2.getWidth();
 		
 		return widths;
 	}
 
 	//set the width of "name" and "title" column
 	public void setColumWidths(int[] widths)
 	{
 		column0.setPreferredWidth(widths[0]);
 		column1.setPreferredWidth(widths[1]);
 		column2.setPreferredWidth(widths[2]);
 	}
 	
 	/**
 	 * removes the selected row from the table
 	 */
 	public void removeStreamfromTable() {
 		
 		int[] rows = table.getSelectedRows();
 		
 		//remove all selected rows
 		for(int i= rows.length-1; i >= 0; i--) {
 			model.removeRow(table.convertRowIndexToModel(rows[i]));
 		}
 	}
 	
 	
 	public void fillTableWithStreams() {
 		Vector<Stream> tmp = controlStreams.getStreamVector();
 		for(int i=0; i < tmp.capacity() ;i++) {
 			model.addRow(tmp.get(i).getBase());
 		}
 	}
 	
 	/**
 	 * Looks, if a stream is selected in the table
 	 * @return true, if a stream is selected, else false; 
 	 */
 	public boolean isStreamSelected() {
 		if(table.getSelectedRow() == -1) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	/**
 	 * Select the next stream. If no stream is selected, try to select the first
 	 * stream. If the last stream is selected, select the first stream.
 	 */
 	public void selectNextStream() {
 		int selectedRow = table.getSelectedRow();
 		int numberOfStreams = controlStreams.getStreamVector().size();
 		
 		//no stream is selected
 		if(selectedRow == -1 && numberOfStreams > 0) {
 			selectedRow = 0;
 		} else if (selectedRow >= (numberOfStreams-1)) {
 			selectedRow = 0;
 		} else {
 			selectedRow++;
 		}
 		
 		try {
 			table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
 		} catch (IllegalArgumentException e) {
 			SRSOutput.getInstance().logE("Gui_TablePanel: Unable to select an row");
 		}
 	}
 	
 	/**
 	 * Select the next stream. If no stream is selected, try to select the first
 	 * stream. If the last stream is selected, select the first stream.
 	 */
 	public void selectPreviousStream() {
 		int selectedRow = table.getSelectedRow();
 		int numberOfStreams = controlStreams.getStreamVector().size();
 		
 		//no stream is selected
 		if(selectedRow == -1 && numberOfStreams > 0) {
 			selectedRow = numberOfStreams-1;
 		} else if (selectedRow >= (numberOfStreams-1)) {
 			selectedRow = numberOfStreams-1;
 		} else {
 			selectedRow--;
 		}
 		
 		try {
 			table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
 		} catch (IllegalArgumentException e) {
 			SRSOutput.getInstance().logE("Gui_TablePanel: Unable to select an row");
 		}
 	}
 	
 	public void addLastStreamFromVector()
 	{
 		Vector<Stream> tmp = controlStreams.getStreamVector();
 		int length = tmp.capacity();
 		model.addRow(tmp.get(length-1).getBase());
 	}
 	
 	/**
 	 * This method adds Data to the table
 	 * @param data
 	 */
 	public void addData(Object[] data){
 		model.addRow(data);
 	}
 	
 	/**
 	 * Get the id in xml-file of the selected stream.
 	 * @return Integer of index in xml-file
 	 */
 	public int getSelectedStreamID() {
 		int id = 0;
 		id = table.convertRowIndexToModel(table.getSelectedRow());
 		id = controlStreams.getStreamVector().get(id).id;
 		return id;
 	}
 
 	
 	/**
 	 * look for the id of the selected stream and
 	 * search with this id in all streams for the right one.
 	 * @return: the selected stream in table. If no stream is selected
 	 * or no stream is in the table the return value is null
 	 */
 	public Stream[] getSelectedStream() {
 		
 		try 
 		{
 			int[] ids = table.getSelectedRows();
 		
 			Stream[] streams = new Stream[ids.length];
 			
 			for(int i=0 ; i < ids.length ; i++) {
 				int id = table.convertRowIndexToModel(ids[i]);
 				streams[i] = controlStreams.getStreamVector().get(id);
 			}
 			
 			return streams;
 		}
 		catch (ArrayIndexOutOfBoundsException e)
 		{
 			SRSOutput.getInstance().logE("No Stream in the table -> ArrayIndexOutOfBoundException");
 		}
 		return null;
 		
 	}
 	
 	/**
 	 * Look in der StreamVector, witch collects all streams
 	 * for an stream with the given ID
 	 * @param id: the ID for the stream in xml-file
 	 * @return The Stream with the given ID
 	 */
 	public Stream getStreamByID(int id) {
 		Vector<Stream> x = controlStreams.getStreamVector();
 		for(int i=0; i< x.capacity(); i++) {
 			if(x.get(i).id == id) {
 				return x.get(i);
 			}
 		}
 		return null;
 	}
 	
 	public String getSelectedName()
 	{
 		if( table.getSelectedRow() < 0 )
 			return null;
 		Object content = table.getValueAt(table.getSelectedRow(),1);
 		if(content != null)
 		{
 			return content.toString();
 		}
 		else
 			return null;
 	}
 	
 	/**
 	 * Looks, if something in the table is selected
 	 * @return: true, if something is selected
 	 */
 	public boolean isTHSelected() {
 		if( table.getSelectedRow() < 0 ) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	
 	/**
 	 * 
 	 * @param track	- will be displayed als title (including error
 	 * @param row - the appropriate row
 	 * @param rec - if true, set status "Rec."; else ""
 	 */
 	public void setSelectedCurrentNameCellAndTitle(String track, int row,boolean rec)	{
 		table.setValueAt(track,row ,2);
 		if(rec==true)
 			table.setValueAt(recordIcon,row ,0);
 		else
 			table.setValueAt(null,row ,0);
 	}
 
 	public void setNameValue(String newName,int row){
 		table.setValueAt(newName, row, 1);
 	}
 	
 	public void setNameValueWithConvert(String newName,int row){
 		table.setValueAt(newName, table.convertRowIndexToView(row), 1);
 	}
 	
 	public int getSelectedRow(){
 		return table.getSelectedRow();
 	}
 	
 	public String getNameFromRow(int row){
 		return (table.getValueAt(row, 1).toString());
 	}
 	
 	public String getNameFromRowForUpdate(int row){
 		return (model.getValueAt(row, 1).toString());
 	}
 	
 	public DefaultTableModel getModel(){
 		return model;
 	}
 	
 //	search for the line with the name name
 	public int getNewRowForName(String vName){
 		for(int i=0;i< model.getRowCount();i++){
 			if(model.getValueAt(i, 1).toString().equals(vName))
 				return i;
 		}
 		return 0;
 	}
 	
 	public int getNewRowForNameForUpdate(String vName)
 	{
 		for(int i=0;i< model.getRowCount();i++)
 		{
 			if(table.getValueAt(i, 1).toString().equals(vName))
 				return i;
 		}
 		return 0;
 	}
 	
 	/**
 	 * Initialize the first instance of the internal audio player. This must
 	 * be done, because gstreamer hangs for a while on the first start and we
 	 * loose control over all audio player, but the last one. -> all audio player
 	 * will play and can't be stopped anymore
 	 */
 	public void loadFirstAudioPlayer()
 	{
		//only load the player, if we want to use the internal audio player
		if(mainGui.useInternalAudioPlayer())
		{
			player = new AudioPlayer(mainGui);
		}
 	}
 	
 	/**
 	 * Looks for the selected stream in the stream table and
 	 * start playing it with the correct player. This can either 
 	 * be the internal or the external, which is defined in the 
 	 * settings.
 	 */
 	public synchronized void startMusicPlayerWithSelectedStream() {
 		//get the selected Stream
 		Stream[] stream = getSelectedStream();
 
 		if (stream != null)
 		{
 			//Test if a relay stream is running and connect to them
 			//else will connect to stream address directly 
 			if(stream[0].getStatus() && stream[0].connectToRelayCB)
 			{
 				if(mainGui.useInternalAudioPlayer())
 				{
 					stopInternalAudioPlayer();
 					player = new AudioPlayer(stream[0], mainGui);
 					player.start();
 				} else {
 					controlStreams.startMp3Player("http://127.0.0.1:"+stream[0].relayServerPortTF);
 				}
 				
 			} else if(stream[0].address != null  && !stream[0].address.equals(""))
 			{
 				if(mainGui.useInternalAudioPlayer())
 				{
 					stopInternalAudioPlayer();
 					player = new AudioPlayer(stream[0], mainGui);
 					player.start();
 				} else {
 					controlStreams.startMp3Player(stream[0].address);
 				}
 				
 			} else {
 				SRSOutput.getInstance().logE("error while fetching adress");
 			}
 		}
 		
 		else 
 		{
 			SRSOutput.getInstance().log("Could not find a stream to play. Nullpointer");
 		}
 	}
 	
 	
 	/**
 	 * Set the Thread with the audio player a new volume for the 
 	 * audio
 	 * @param percentage: a value in a range of 0 up to 100
 	 */
 	public synchronized void setAudioVolume(int percentage) {
 		if(player != null  && percentage >= 0 && percentage <= 100) {
 			
 			double val = 0;
 			
 			//the human ears has a logarithmn frequency characteristic
 			if(percentage !=  0)
 			{
 				 val = 100*(Math.pow(2.0,Double.valueOf(percentage)/100.0)-1);
 			} 
 			player.setAudioVolum(new Double(val).intValue());
 			SRSOutput.getInstance().logD("Lautstärke ist nun: "+val);
 		}
 	}
 	
 	/**
 	 * Start the music player with an given url. This function looks
 	 * to use the correct player. This can either be the internal or
 	 *  the external, which is defined in the settings.
 	 *  
 	 * @param url: The url to the stream
 	 * @param name: The name of the stream: the name is shown in front of the current title
 	 */
 	public synchronized  void startMusicPlayerWithUrl(String url, String name) {
 		//get the selected Stream
 		Stream stream = new Stream(name,0);
 		stream.address = url;
 		
 		//Test if a relay stream is running and connect to them
 		//else will connect to stream address directly 
 		if(stream.getStatus() && stream.connectToRelayCB) {
 			if(mainGui.useInternalAudioPlayer()) {
 				stopInternalAudioPlayer();
 				player = new AudioPlayer(stream, mainGui);
 				player.start();
 			} else {
 				controlStreams.startMp3Player("http://127.0.0.1:"+stream.relayServerPortTF);
 			}
 			
 		} else if(stream.address != null  && !stream.address.equals("")) {
 			if(mainGui.useInternalAudioPlayer()) {
 				stopInternalAudioPlayer();
 				player = new AudioPlayer(stream, mainGui);
 				player.start();
 			} else {
 				controlStreams.startMp3Player(stream.address);
 			}
 			
 		} else {
 			SRSOutput.getInstance().logE("error while fetching adress");
 		}		
 	}
 
 	
 	/**
 	 * Stops the Thread with the internal audioplayer, if there is one
 	 */
 	public synchronized void stopInternalAudioPlayer() {
 		if(player != null) {
 			player.stopPlaying();
 		}
 	}
 	
 	public Gui_StreamRipStar getMainGui() {
 		return mainGui;
 	}
 	
 	public Control_Stream getControlStreams() {
 		return controlStreams;
 	}
 	
 	/**
 	 * the method looks in the settings what to do
 	 * when a cell was double clicked. It provides
 	 * all thinks to start
 	 */
 	public void startAction() {
 		int action = 0;
 		int selCol = table.getSelectedColumn();
 		
 		//get and set action for the first Column
 		//witch shows the state of a stream
 		
 		//Column: status
 		if(selCol==0) {
 			action = mainGui.getAction(0);
 		}
 		
 		//Column: name
 		if(selCol==1) {
 			action = mainGui.getAction(1);
 		}
 		
 		//column: current track
 		if(selCol==2) {
 			action = mainGui.getAction(2);
 		}
 		
 		//0 is doing nothing
 		if (action != 0 ) {
 			Stream[] stream = getSelectedStream();
 			if (stream[0] != null) {	
 				//action = 1: Open Webbrowser
 				if(action == 1 ) {
 					if(stream[0].website != null  && !stream[0].website.equals("")) {
 						controlStreams.startWebBrowser(stream[0].website);
 					} else
 						JOptionPane.showMessageDialog(mainGui,
 								trans.getString("setWebsite"));
 				}
 				
 				//action = 2: edit Stream
 				if(action == 2) {
 					mainGui.editStream();
 				}
 				
 				//action = 3: switching between start record and stop record
 				if (action == 3) {
 					//if ripping -> stop it
 					if(stream[0].getStatus())
 						mainGui.stopRippingSelected();
 					//else -> start ripping
 					else
 						mainGui.startRippingSelected();
 				}
 				
 				//action = 5: play stream in media player
 				if (action == 4) {
 					startMusicPlayerWithSelectedStream();
 				}
 			}
 			else
 				SRSOutput.getInstance().logE("can't find stream in vector");
 		}
 	}
 
 	
 //	Listener
 	public class CellMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			if(e.getClickCount()==2) {
 				startAction();
 			}
 		}
 		
 		public void mousePressed(MouseEvent e){
 			if(e.getButton() == MouseEvent.BUTTON3) {
 				int row = table.rowAtPoint(e.getPoint());
 				
 				//only selection was a row
 				if(row >= 0) {
 					table.setRowSelectionInterval(row,row);
 					popup.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
 				}
 			}
 		}
 	}
 	
 	public void setTTablePopup(JPopupMenu popup) {
 		this.popup = popup;
 	}
 
 	/**
 	 * To add icon support for the header
 	 *
 	 */
 	class JComponentTableCellRenderer implements TableCellRenderer {
 		@Override
 		public Component getTableCellRendererComponent(JTable arg0,
 				Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
 			 return (JComponent) arg1;
 		}
 	}
 }
 
