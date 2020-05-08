 package ws;
 
 import java.rmi.RemoteException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 
 
 public class GeoTrackerMenu 
 {
 	private Display _display = Display.getCurrent();
 	
 	private Combo _idCombo = null;
 	private Spinner _maxResponseLabel = null;
 	private Text _minDate = null;
 	private Text _maxDate = null;
 	
 	private String[] _ids = null;
 	private String _selId = null;
 	private String _selMinDate = null;
 	private String _selMaxDate = null;
 	private int _selMaxResponse = 0;
 	
 	public GeoTrackerMenu(Composite parent) throws RemoteException 
 	{
 		initLayout(parent);
 		
 		LocGetIds myIds = new LocGetIds();	
 		_ids = myIds.show();
 	}
 	
 	public void initLayout(Composite parent) throws RemoteException
 	{
 		parent.setLayout(new GridLayout(1, false));
 		
 		// Ligne 1: ID et Points
 		Composite compIdAndPoints = newLine(parent, 2);
 		
 		// Init ID
 		Composite compId = new Composite(compIdAndPoints, SWT.NONE);
 		_idCombo = initId(compId);
 		
 		// Init Points
 		Composite compPoints = new Composite(compIdAndPoints, SWT.NONE);
 		_maxResponseLabel = initPoints(compPoints);
 		
 		// Ligne 2: Date debut et fin
 		Composite compDate = newLine(parent, 4);
 		_minDate = initMinDate(compDate);
 		_maxDate = initMaxDate(compDate);
 		
 		// Line 3: Valid, selecteur de date, geoloc
 		Composite compValid = newLine(parent, 3);
 		initValidButton(compValid);
 		
 		// Line 4: Tableau
 		Composite compTab = newLine(parent, 1);
 		initTab(compTab);
 	}
 	
 	/**
 	 * Nouvelle ligne d'interface
 	 * @param parent
 	 * @param numCol
 	 * @return
 	 */
 	protected Composite newLine(Composite parent, int numCol) {
 		Composite newLine = new Composite(parent, SWT.BORDER);
 		newLine.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		newLine.setLayout(new GridLayout(numCol, true));
 		return newLine;
 	}
 	
 	/**
 	 * Init ID
 	 * @param parent
 	 * @throws RemoteException 
 	 */
 	protected Combo initId(Composite parent) throws RemoteException {
 		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		parent.setLayout(new GridLayout(2, true));
 		Label idLabel = new Label(parent, SWT.NONE);
 		idLabel.setText("ID: ");
 		Combo idCombo = new Combo(parent, SWT.NONE);
 		idCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		// Plug to GUI
 		if (_ids != null)
 		{
 			idCombo.setItems(_ids);
 		}
 		
 		return idCombo;
 	}
 	
 	/**
 	 * Init points max
 	 * @param parent
 	 */
 	protected Spinner initPoints(Composite parent) {
 		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		parent.setLayout(new GridLayout(2, true));
 		Label pointsLabel = new Label(parent, SWT.NONE);
 		pointsLabel.setText("Nb max points: ");
 		Spinner pointsSpinner = new Spinner(parent, SWT.BORDER);
 		// Exemple d'utilisation
 		pointsSpinner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		pointsSpinner.setValues(1000, 1, 100000, 0, 1, 100);
 		
 		return pointsSpinner;
 	}
 	
 	/**
 	 * Init choix date début et fin
 	 * @param parent
 	 */
 	protected Text initMinDate(Composite parent) {
 		
 		// Init date debut
 		Label minDateLabel = new Label(parent, SWT.NONE);
 		minDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
 		minDateLabel.setText("Date initiale: ");
 		
 		Text minDate = new Text(parent,SWT.NONE);
 		minDate.setText("28/03/2008:00:00:00");
 		
 		return minDate;
 	}
 	
 	protected Text initMaxDate(Composite parent)
 	{
 		Label maxDateLabel = new Label(parent, SWT.NONE);
 		maxDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
 		maxDateLabel.setText("Date finale: ");
 
 		Text maxDate = new Text(parent,SWT.NONE);
 		maxDate.setText("30/03/2008:23:59:59");
 		
 		return maxDate;
 	}
 	
 	protected void initValidButton(Composite parent) {
 		Button validButton = new Button(parent, SWT.PUSH);
 		validButton.setText("OK");
 		
 		validButton.addListener(SWT.Selection, new Listener()
 		{
 			public void handleEvent(Event event)
 			{
 				_selId = _idCombo.getText();
 				_selMinDate = _minDate.getText();
 				_selMaxDate = _maxDate.getText();
 				_selMaxResponse = _maxResponseLabel.getSelection();
 				
 				try
 				{
 					LocGetPositions myPositions = new LocGetPositions(_selId, _selMinDate, _selMaxDate, _selMaxResponse);
 				}
 				catch (RemoteException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	private void initTab(Composite parent) {
 		Table table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		table.setLinesVisible (true);
 		table.setHeaderVisible (true);
 		
 		String[] titles = {"No", "Latitude", "Longitude", "Speed", "Heading", "Date"};
 		for (int i=0; i < titles.length; i++) {
 			TableColumn column = new TableColumn (table, SWT.NONE);
 			column.setText (titles [i]);
 		}
 		
 		// Add DATA HERE !!!
 		if (_ids != null && _ids.length > 0) {
 			for (int i = 0; i < _ids.length; i++) {
 				TableItem tabItem = new TableItem(table, SWT.BORDER);
 				// No
 				tabItem.setText(0, String.valueOf(i));
 				// Latitude
 				tabItem.setText(1, "PUT_DATAS_HERE");
 				// Longitude
 				tabItem.setText(2, "PUT_DATAS_HERE");
 				//Speed
 				tabItem.setText(3, "PUT_DATAS_HERE");
 				// Heading
 				tabItem.setText(4, "PUT_DATAS_HERE");
 				// Heading
 				tabItem.setText(5, "PUT_DATAS_HERE");
 				// Date
 				tabItem.setText(6, "PUT_DATAS_HERE");
 			}
 		}
 		
 		for (int i=0; i<titles.length; i++) {
 			table.getColumn(i).pack();
 		}
 	}
 }
