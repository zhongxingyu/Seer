 package ted.ui.addshowdialog;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.w3c.dom.Element;
 
 import ted.BrowserLauncher;
 import ted.Lang;
 import ted.TedDailySerie;
 import ted.TedIO;
 import ted.TedLog;
 import ted.TedMainDialog;
 import ted.TedSerie;
 import ted.TedSystemInfo;
 import ted.TedXMLParser;
 import ted.datastructures.DailyDate;
 import ted.datastructures.SeasonEpisode;
 import ted.datastructures.SimpleTedSerie;
 import ted.datastructures.StandardStructure;
 import ted.interfaces.EpisodeChooserListener;
 import ted.ui.TableRenderer;
 import ted.ui.editshowdialog.EditShowDialog;
 import ted.ui.editshowdialog.FeedPopupItem;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class AddShowDialog extends JDialog implements ActionListener, MouseListener, 
 													  EpisodeChooserListener, KeyListener
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1006862655927988046L;
 	private JTable showsTable;
 	private JButton cancelButton;
 	private JButton okButton;
 	private JScrollPane showsScrollPane;
 	private ShowsTableModel showsTableModel;
 	private TedSerie selectedSerie;
 	private TedMainDialog tedMain;
 	private JTextPane showInfoPane;
 	private JButton switchButton;
 	private JTextField jSearchField;
 	private JLabel showNameLabel;
 	private JLabel selectShowLabel;
 	private JLabel selectEpisodeLabel;
 	private JButton jHelpButton;
 	private JScrollPane showInfoScrollPane;
 	private JLabel buyDVDLabel;
 	private JButton buttonAddEmptyShow;
 	private Vector<SimpleTedSerie> allShows;
 	private SimpleTedSerie selectedShow;
 
 	private EpisodeChooserPanel   episodeChooserPanel   = new EpisodeChooserPanel(this);
 	private SubscribeOptionsPanel subscribeOptionsPanel = new SubscribeOptionsPanel(this);
 	
 	public AddShowDialog()
 	{
 		this.initGUI();
 	}
 	
 	public AddShowDialog(TedMainDialog main)
 	{
 		this.setModal(true);
 		this.tedMain = main;
 		this.initGUI();
 	}
 
 	private void initGUI() {
 		try 
 		{
 			this.episodeChooserPanel.setActivityStatus(false);
 			FormLayout thisLayout = new FormLayout(
 					"max(p;5dlu), 68dlu:grow, max(p;68dlu), 10dlu, 5dlu:grow, max(p;15dlu), 5dlu, 85dlu, max(p;5dlu)", 
					"max(p;5dlu), max(p;15dlu), 5dlu, 50dlu:grow, 5dlu, max(p;15dlu), 5dlu, bottom:110dlu, 5dlu, max(p;15dlu), 5dlu, max(p;15dlu), max(p;5dlu)");
 			getContentPane().setLayout(thisLayout);
 
 			episodeChooserPanel.setVisible(false);			
 			subscribeOptionsPanel.setVisible(true);
 			
 			showsTableModel = new ShowsTableModel();
 			showsTable = new JTable();
 			//getContentPane().add(showsTable, new CellConstraints("4, 3, 1, 1, default, default"));
 			getShowsScrollPane().setViewportView(showsTable);
 			getContentPane().add(getShowsScrollPane(), new CellConstraints("2, 4, 2, 5, fill, fill"));
 			getContentPane().add(episodeChooserPanel, new CellConstraints("5, 4, 4, 1, fill, fill"));
 			getContentPane().add(subscribeOptionsPanel, new CellConstraints("5, 8, 4, 1, fill, fill"));
 			getContentPane().add(getOkButton(), new CellConstraints("8, 12, 1, 1, default, default"));
 			getContentPane().add(getCancelButton(), new CellConstraints("6, 12, 1, 1, default, default"));
 			getContentPane().add(getShowInfoScrollPane(), new CellConstraints("5, 4, 4, 1, fill, fill"));
 			getContentPane().add(getJHelpButton(), new CellConstraints("2, 12, 1, 1, left, default"));
 			getContentPane().add(getSelectShowLabel(), new CellConstraints("2, 2, 2, 1, left, fill"));
 			getContentPane().add(getSelectEpisodeLabel(), new CellConstraints("5, 6, 4, 1, left, bottom"));
 			getContentPane().add(getShowNameLabel(), new CellConstraints("5, 2, 4, 1, left, fill"));
 			getContentPane().add(getButtonAddEmptyShow(), new CellConstraints("2, 10, 2, 1, left, default"));
 			getContentPane().add(getBuyDVDLabel(), new CellConstraints("5, 10, 4, 1, left, default"));
 			getContentPane().add(getJSearchField(), new CellConstraints("3, 2, 1, 1, default, fill"));
 			showsTable.setModel(showsTableModel);
 			showsTableModel.setSeries(this.readShowNames());
 			
 			showsTable.setAutoCreateColumnsFromModel(true);
 			showsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 			showsTable.setEditingRow(0);
 			showsTable.setFont(new java.awt.Font("Dialog",0,15));
 		    showsTable.setRowHeight(showsTable.getRowHeight()+10);
 		    TableRenderer tr = new TableRenderer();
 		    showsTable.setDefaultRenderer(Object.class, tr);
 			
 			//	disable horizontal lines in table
 			showsTable.setShowHorizontalLines(false);
 			showsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			
 			showsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 				public void valueChanged(ListSelectionEvent arg0) {
 					showsTableSelectionChanged();
 					
 				}});		
 			
 			jSearchField.addKeyListener(this);
 			
 			// Get the screen size
 		    Toolkit toolkit = Toolkit.getDefaultToolkit();
 		    Dimension screenSize = toolkit.getScreenSize();
 		    
 		    this.setSize((int)(screenSize.width*0.75), (int)(screenSize.height*0.75));
 
 		    //Calculate the frame location
 		    int x = (screenSize.width - this.getWidth()) / 2;
 		    int y = (screenSize.height - this.getHeight()) / 2;
 
 		    //Set the new frame location
 		    this.setLocation(x, y);
 		    this.setVisible(true);
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Read the shownames from the xml file
 	 * @return Vector with names
 	 */
 	private Vector readShowNames()
 	{
 		Vector<SimpleTedSerie> names = new Vector<SimpleTedSerie>();		
 		
 		TedXMLParser parser = new TedXMLParser();
 		Element shows = parser.readXMLFromFile(TedIO.XML_SHOWS_FILE); //$NON-NLS-1$
 		
 		if(shows!=null)
 		{
 			names = parser.getNames(shows);
 			allShows = names;
 		}
 		else
 			TedLog.error(Lang.getString("TedEpisodeDialog.LogXmlNotFound")); //$NON-NLS-1$
 		
 		return names;
 	}
 	
 	private JScrollPane getShowsScrollPane() {
 		if (showsScrollPane == null) {
 			showsScrollPane = new JScrollPane();
 			
 		}
 		return showsScrollPane;
 	}
 	
 	/**
 	 * Called whenever the selection of a show is changed in the dialog
 	 */
 	private void showsTableSelectionChanged()
 	{
 		// disable ok button
 		this.okButton.setEnabled(false);
 		
 		// get the selected show
 		int selectedRow = showsTable.getSelectedRow();
 		
 		if (selectedRow >= 0)
 		{			
 			// get the simple info of the show
 			SimpleTedSerie selectedShow = this.showsTableModel.getSerieAt(selectedRow);
 			
 			if (this.selectedShow == null || !(this.selectedShow.getName().equals(selectedShow.getName())))
 			{
 				this.selectedShow = selectedShow;
 						
 				this.showNameLabel.setText(selectedShow.getName());
 				
 				this.episodeChooserPanel.setVisible(false);
 				
 				// get the details of the show
 				TedXMLParser parser = new TedXMLParser();
 				Element series = parser.readXMLFromFile(TedIO.XML_SHOWS_FILE); //$NON-NLS-1$
 				
 				TedSerie selectedSerie = parser.getSerie(series, selectedShow.getName());
 				
 				buyDVDLabel.setText("<html><u>"+ Lang.getString("TedAddShowDialog.LabelSupportTed1")+ " " + selectedSerie.getName() +" " + Lang.getString("TedAddShowDialog.LabelSupportTed2") +"</u></html>");
 				
 				// create a new infoPane to (correctly) show the information
 				showInfoPane = null;
 				showInfoScrollPane.setViewportView(this.getShowInfoPane());
 				
 				// add auto-generated search based feeds to the show
 				Vector<FeedPopupItem> items = new Vector<FeedPopupItem>();
 				items = parser.getAutoFeedLocations(series);
 				selectedSerie.generateFeedLocations(items);
 				
 				// retrieve the show info and the episodes from the web
 				ShowInfoThread sit = new ShowInfoThread(this.getShowInfoPane(), selectedSerie);
 				//sit.setPriority( Thread.NORM_PRIORITY + 1 ); 
 				EpisodeParserThread ept = new EpisodeParserThread(this.episodeChooserPanel, selectedSerie, this.subscribeOptionsPanel);
 				//ept.setPriority( Thread.NORM_PRIORITY - 1 ); 
 				
 				sit.start();
 				ept.start();	
 				
 				// set the selected show
 				this.setSelectedSerie(selectedSerie);
 			}
 		}
 	}
 	
 	private JButton getOkButton() {
 		if (okButton == null) {
 			okButton = new JButton();
 			okButton.setText(Lang.getString("TedGeneral.ButtonAdd"));
 			okButton.setActionCommand("OK");
 			okButton.addActionListener(this);
 			this.getRootPane().setDefaultButton(okButton);
 			this.okButton.setEnabled(false);
 		}
 		return okButton;
 	}
 	
 	private JButton getCancelButton() {
 		if (cancelButton == null) {
 			cancelButton = new JButton();
 			cancelButton.setText(Lang.getString("TedGeneral.ButtonCancel"));
 			cancelButton.setActionCommand("Cancel");
 			cancelButton.addActionListener(this);
 		}
 		return cancelButton;
 	}
 
 	public void actionPerformed(ActionEvent arg0)
 	{
 		String command = arg0.getActionCommand();
 		
 		if (command.equals("OK"))
 		{
 			this.addShow();
 		}
 		else if (command.equals("Cancel"))
 		{
 			this.close();		
 		}
 		else if (command.equals("Help"))
 		{
 			try 
 			{
 				// open the help page of ted
 				BrowserLauncher.openURL("http://www.ted.nu/wiki/index.php/Add_show"); //$NON-NLS-1$
 			} 
 			catch (Exception err)
 			{
 				
 			}
 		}
 		else if (command.equals("addempty"))
 		{
 			// create an edit show dialog with an empty show and hide add show dialog		
 			TedSerie temp = new TedSerie();
 			this.close();
 			new EditShowDialog(tedMain, temp, true);
 		}
 		else if (command.equals("search"))
 		{
 			this.searchShows(jSearchField.getText());
 		}
 		else if (command.equals("switch"))
 		{
 			//this.setSubscribeOption();			
 			episodeChooserPanel.setVisible(!episodeChooserPanel.isVisible());
 			//subscribeOptionsPanel.setVisible(!subscribeOptionsPanel.isVisible());
 		}
 	}
 
 	/**
 	 * Add the selected show with the selected season/episode to teds show list
 	 */
 	private void addShow() 
 	{
 		// add show
 		if (selectedSerie != null)
 		{
 			StandardStructure selectedEpisode = this.subscribeOptionsPanel.getSelectedEpisode();
 			selectedSerie.setCurrentEpisode(selectedEpisode);
 			selectedSerie.updateShowStatus();
 			
 			// add the serie
 			tedMain.addSerie(selectedSerie);
 			
 			this.close();
 		}	
 	}
 
 	private void close() 
 	{
 		this.showsTableModel.removeSeries();
 		this.episodeChooserPanel.clear();
 		// close the dialog
 		this.setVisible(false);
 		this.dispose();
 		// call garbage collector to cleanup dirt
 		Runtime.getRuntime().gc(); 	
 	}
 
 	public void setSelectedSerie(TedSerie selectedSerie2)
 	{
 		this.selectedSerie = selectedSerie2;
 		
 	}
 	
 	private JScrollPane getShowInfoScrollPane() {
 		if (showInfoScrollPane == null) {
 			showInfoScrollPane = new JScrollPane();
 			showInfoScrollPane.setViewportView(getShowInfoPane());
 		}
 		return showInfoScrollPane;
 	}
 	
 	private JTextPane getShowInfoPane() {
 		if (showInfoPane == null) {
 			showInfoPane = new JTextPane();
 			showInfoPane.setContentType( "text/html" );
 			showInfoPane.setEditable( false );
 			//showInfoPane.setPreferredSize(new java.awt.Dimension(325, 128));
 			//showInfoPane.setText("jTextPane1");
 			
 			//	Set up the JEditorPane to handle clicks on hyperlinks
 		    showInfoPane.addHyperlinkListener(new HyperlinkListener() {
 		      public void hyperlinkUpdate(HyperlinkEvent e) {
 			// Handle clicks; ignore mouseovers and other link-related events
 			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 			  // Get the HREF of the link and display it.
 				try {
 					BrowserLauncher.openURL(e.getDescription());
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 				}
 			}
 		      }
 		    });
 			
 		}
 		return showInfoPane;
 	}
 	
 	private JButton getJHelpButton() {
 		if (jHelpButton == null) {
 			jHelpButton = new JButton();
 			jHelpButton.setActionCommand("Help");
 			if (!TedSystemInfo.osIsMacLeopardOrBetter())
 			{
 				jHelpButton.setIcon(new ImageIcon(getClass().getClassLoader()
 					.getResource("icons/help.png")));
 			}
 			jHelpButton.setBounds(11, 380, 28, 28);
 			jHelpButton.addActionListener(this);
 			jHelpButton.putClientProperty("JButton.buttonType", "help");
 			jHelpButton.setToolTipText(Lang.getString("TedGeneral.ButtonHelpToolTip"));
 		}
 		return jHelpButton;
 	}
 	
 	private JLabel getSelectShowLabel() {
 		if (selectShowLabel == null) {
 			selectShowLabel = new JLabel();
 			selectShowLabel.setText(Lang.getString("TedAddShowDialog.LabelSelectShow"));
 		}
 		return selectShowLabel;
 	}
 	
 	private JLabel getSelectEpisodeLabel() {
 		if (selectEpisodeLabel == null) {
 			selectEpisodeLabel = new JLabel();
 			selectEpisodeLabel
 				.setText(Lang.getString("TedAddShowDialog.LabelSelectEpisode"));
 		}
 		return selectEpisodeLabel;
 	}
 	
 	private JLabel getShowNameLabel() {
 		if (showNameLabel == null) {
 			showNameLabel = new JLabel();
 			showNameLabel.setFont(new java.awt.Font("Dialog",1,18));
 
 		}
 		return showNameLabel;
 	}
 	
 	private JButton getButtonAddEmptyShow() {
 		if (buttonAddEmptyShow == null) {
 			buttonAddEmptyShow = new JButton();
 			buttonAddEmptyShow.setText(Lang.getString("TedAddShowDialog.ButtonAddCustomShow"));
 			buttonAddEmptyShow.addActionListener(this);
 			buttonAddEmptyShow.setActionCommand("addempty");
 		}
 		return buttonAddEmptyShow;
 	}
 	
 	private JLabel getBuyDVDLabel() {
 		if (buyDVDLabel == null) {
 			buyDVDLabel = new JLabel();
 			buyDVDLabel.setText("");
 			buyDVDLabel.setForeground(Color.BLUE);
 			buyDVDLabel.setFont(new java.awt.Font("Dialog",1,12));
 			buyDVDLabel.addMouseListener(this);
 			buyDVDLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		}
 		return buyDVDLabel;
 	}
 
 	public void mouseClicked(MouseEvent arg0) 
 	{
 		// clicked on label to buy dvd
 		this.tedMain.openBuyLink(this.selectedSerie.getName());
 		
 	}
 
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void episodeSelectionChanged() 
 	{
 		StandardStructure selectedStructure = episodeChooserPanel.getSelectedStructure();
 		this.subscribeOptionsPanel.setCustomEpisode(selectedStructure);
 	}
 
 	/* (non-Javadoc)
 	 * @see ted.interfaces.EpisodeChooserListener#doubleClickOnEpisodeList()
 	 */
 	public void doubleClickOnEpisodeList() 
 	{
 		// add show
 		this.addShow();
 	}
 	
 	private JTextField getJSearchField() {
 		if(jSearchField == null) {
 			jSearchField = new SearchTextField();
 		}
 		return jSearchField;
 	}
 
 	private void searchShows(String searchString)
 	{
 		// Only search if we've entered a search term
 		if (!searchString.equals("<SEARCH>"))
 		{
 			Vector<SimpleTedSerie> tempShows = new Vector<SimpleTedSerie>();
 			
 			// If we've entered a search term filter the list, otherwise
 			// display all shows
 			if (!searchString.equals(""))
 			{
 				// Do the filtering
 				for (int show = 0; show < allShows.size(); ++show)
 				{
 					SimpleTedSerie serie = allShows.get(show);
 					
 					if (serie.getName().toLowerCase().contains(searchString.toLowerCase()))
 					{
 						tempShows.add(serie);
 					}
 				}
 				// Update the table
 				showsTableModel.setSeries(tempShows);	
 			}
 			else
 			{
 				showsTableModel.setSeries(allShows);
 			}
 			
 			// Let the table know that there's new information
 			showsTableModel.fireTableDataChanged();
 		}
 	}
 
 	public void keyPressed (KeyEvent arg0) { }
 	public void keyReleased(KeyEvent arg0) { searchShows(jSearchField.getText()); }
 	public void keyTyped   (KeyEvent arg0) { }
 	
 	public void subscribeOptionChanged() 
 	{
 		// called when episode selection is changed.		
 		// check if episode and show selected
 		if (selectedSerie != null && this.subscribeOptionsPanel.getSelectedEpisode() != null)
 		{
 			// enable add button
 			this.okButton.setEnabled(true);
 		}
 		else
 		{
 			this.okButton.setEnabled(false);
 		}		
 	}
 
 	public void setEpisodeChooserVisible(boolean b) 
 	{
 		this.episodeChooserPanel.setVisible(b);
 		
 	}
 }
