 package net.bioclipse.brunn.ui.editors.plateEditor;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.plate.IPlateManager;
 import net.bioclipse.brunn.pojos.ILISObject;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.PlateFunction;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.ui.Activator;
 import net.bioclipse.brunn.ui.editors.plateEditor.MarkersContentProvider;
 import net.bioclipse.brunn.ui.editors.plateEditor.MarkersLabelProvider;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.MarkersTableRow;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.PlateTableModel;
 import net.bioclipse.brunn.ui.explorer.model.ITreeModelListener;
 import net.bioclipse.brunn.ui.explorer.model.ITreeObject;
 import net.bioclipse.brunn.ui.explorer.model.TreeEvent;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 
 import de.kupzog.ktable.KTable;
 import de.kupzog.ktable.KTableCellSelectionListener;
 import de.kupzog.ktable.SWTX;
 
 public class PlateEditor extends EditorPart implements OutlierChangedListener {
 
 	private Text barcodeText;
 	static class Sorter extends ViewerSorter {
 		public int compare(Viewer viewer, Object e1, Object e2) {
 			MarkersTableRow item1 = (MarkersTableRow)e1;
 			MarkersTableRow item2 = (MarkersTableRow)e2;
 			int M1 = Integer.parseInt( item1.getMarker().substring(1) );
 			int M2 = Integer.parseInt( item2.getMarker().substring(1) );
 			return M1 - M2;
 		}
 	}
 	private Combo resultsVersionCombo;
 	private Combo markPlateCombo;
 	private Combo wellFunctionCombo;
 	
 	private net.bioclipse.brunn.ui.explorer.model.nonFolders.Plate plate;
 	private Plate referencePlate;
 	private KTable plateTable;
 	private Plate toBeSaved;
 	private List<String> wellFunctionNames;
 	private static final String[] MARKER_TABLE_COLUMN_NAMES         = {"Marker", "Compound", "Structure"};
 	private static final String[] PLATEFUNCTIONS_TABLE_COLUMN_NAMES = {"PlateFunction", "Value", "Within values"}; 
 	private TableViewer markerTableViewer;
 	private TableViewer plateFunctionsTableViewer;
 	private PlateResults plateResults;
 	private IPlateManager pm = (IPlateManager) Springcontact.getBean("plateManager");
 	private boolean outlierSelected;
 	
 	private final Clipboard cb = new Clipboard(
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay() );
 	
 	public final static String ID = "net.bioclipse.brunn.ui.editors.plateEditor.PlateEditor";
 	private Button markAsOutlierButton;
 	private PlateMultiPageEditor plateMultiPageEditor; 
 	
 	public PlateEditor(PlateResults plateResults, PlateMultiPageEditor plateMultiPageEditor, Plate toBeSaved) {
 		super();
 		this.plateResults = plateResults;
 		this.plateMultiPageEditor = plateMultiPageEditor;
 		plateMultiPageEditor.addListener(this);
 		this.toBeSaved = toBeSaved;
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		
 		pm.edit( Activator.getDefault().getCurrentUser(), toBeSaved);
 		referencePlate = toBeSaved.deepCopy();
 		firePropertyChange(PROP_DIRTY);
 		plate.getParent().fireUpdate();
 	}
 	
 	@Override
 	public void doSaveAs() {
 		System.out.println("PlateEditor.doSaveAs()");
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);
 		
 		plate = (net.bioclipse.brunn.ui.explorer.model.nonFolders.Plate)input;
 		referencePlate = toBeSaved.deepCopy();
 //		pm.evictfromLazyLoading(toBeSaved);
 		ITreeObject parent = plate.getParent();
 		parent.fireUpdate();
 	}
 
 	@Override
 	public boolean isDirty() {
 		return !toBeSaved.equals(referencePlate);
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FormLayout());
 		final Composite top = new Composite(parent, SWT.NONE);
 		top.setLayout(new FormLayout());
 		final FormData formData = new FormData();
 		formData.bottom = new FormAttachment(0, 45);
 		formData.right = new FormAttachment(100, -5);
 		formData.top = new FormAttachment(0, 5);
 		formData.left = new FormAttachment(0, 5);
 		top.setLayoutData(formData);
 
 		final Button numbersButton = new Button(top, SWT.RADIO);
 		final FormData fd_numbersButton = new FormData();
 		fd_numbersButton.bottom = new FormAttachment(0, 29);
 		fd_numbersButton.top = new FormAttachment(0, 7);
 		fd_numbersButton.right = new FormAttachment(0, 88);
 		fd_numbersButton.left = new FormAttachment(0, 5);
 		numbersButton.setLayoutData(fd_numbersButton);
 		numbersButton.setEnabled(false);
 		numbersButton.setText("Numbers");
 
 		final Button colorsButton = new Button(top, SWT.RADIO);
 		final FormData fd_colorsButton = new FormData();
 		fd_colorsButton.bottom = new FormAttachment(0, 29);
 		fd_colorsButton.top = new FormAttachment(0, 7);
 		fd_colorsButton.right = new FormAttachment(0, 157);
 		fd_colorsButton.left = new FormAttachment(0, 93);
 		colorsButton.setLayoutData(fd_colorsButton);
 		colorsButton.setEnabled(false);
 		colorsButton.setText("Colors");
 
 		final Label resultsVersionLabel = new Label(top, SWT.NONE);
 		final FormData fd_resultsVersionLabel = new FormData();
 		fd_resultsVersionLabel.bottom = new FormAttachment(colorsButton, 17, SWT.TOP);
 		fd_resultsVersionLabel.top = new FormAttachment(colorsButton, 0, SWT.TOP);
 		fd_resultsVersionLabel.left = new FormAttachment(0, 210);
 		resultsVersionLabel.setLayoutData(fd_resultsVersionLabel);
 		resultsVersionLabel.setText("Results version:");
 
 		resultsVersionCombo = new Combo(top, SWT.NONE|SWT.READ_ONLY);
 		fd_resultsVersionLabel.right = new FormAttachment(resultsVersionCombo, 0, SWT.LEFT);
 		final FormData fd_resultsVersionCombo = new FormData();
 		fd_resultsVersionCombo.right = new FormAttachment(0, 425);
 		fd_resultsVersionCombo.bottom = new FormAttachment(colorsButton, 27, SWT.TOP);
 		fd_resultsVersionCombo.top = new FormAttachment(colorsButton, 0, SWT.TOP);
 		fd_resultsVersionCombo.left = new FormAttachment(0, 315);
 		resultsVersionCombo.setLayoutData(fd_resultsVersionCombo);
 		resultsVersionCombo.setEnabled(false);
 
 		barcodeText = new Text(top, SWT.BORDER);
 		final FormData fd_barcodeText = new FormData();
 		fd_barcodeText.right = new FormAttachment(100, -5);
 		fd_barcodeText.top = new FormAttachment(0, 5);
 		barcodeText.setLayoutData(fd_barcodeText);
 		barcodeText.addFocusListener( new FocusListener() {
 			public void focusGained(FocusEvent e) {
 			}
 			public void focusLost(FocusEvent e) {
 				toBeSaved.setBarcode(barcodeText.getText());
 				firePropertyChange(PROP_DIRTY);
 			}
 		});
 
 		final Label barcodeLabel = new Label(top, SWT.NONE);
 		final FormData fd_barcodeLabel = new FormData();
 		fd_barcodeLabel.right = new FormAttachment(barcodeText, -9, SWT.LEFT);
 		fd_barcodeLabel.bottom = new FormAttachment(0, 32);
 		fd_barcodeLabel.top = new FormAttachment(0, 15);
 		barcodeLabel.setLayoutData(fd_barcodeLabel);
 		barcodeLabel.setText("Barcode:");
 		Composite center = new Composite(parent, SWT.NULL);
 		center.setLayout(new FillLayout());
 		final FormData formData_1 = new FormData();
 		formData_1.right = new FormAttachment(top, 0, SWT.RIGHT);
 		formData_1.top = new FormAttachment(top, 0, SWT.BOTTOM);
 		formData_1.left = new FormAttachment(0, 5);
 		center.setLayoutData(formData_1);
 
 		final SashForm sashForm = new SashForm(center, SWT.NONE);
 		
 		/*
 		 * CREATE PLATE TABLE
 		 */
 		plateTable = new KTable( sashForm, SWT.V_SCROLL            |
 				                           SWT.H_SCROLL            |
 				                           SWTX.FILL_WITH_LASTCOL  | 
 				                           SWTX.MARK_FOCUS_HEADERS |
 				                           SWT.MULTI );
 		
 		IPlateManager pm = (IPlateManager) Springcontact.getBean("plateManager");
 		
 		
 		plateTable.setModel(new PlateTableModel(toBeSaved, plateTable, this, "raw", plateResults));
 		plateTable.setSelection(2, 2, false);
 		/*
 		 * PLATE CELL SELECTION CHANGED
 		 */
 		plateTable.addCellSelectionListener( new KTableCellSelectionListener() {
 
 			public void cellSelected(int col, int row, int statemask) {
 				refreshMarkerTableSelection();
 				outlierSelected = true;
 				markAsOutlierButton.setText("Unmark as outlier");
 				for( Point p : plateTable.getCellSelection() ) {
 					if ( !toBeSaved.getWell(p).isOutlier() ) {
 						markAsOutlierButton.setText("Mark as outlier");
 						outlierSelected = false;
 					}
 					
 				}
 				
 			}
 
 			public void fixedCellSelected(int col, int row, int statemask) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 				
 		final ScrolledComposite functionScrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		functionScrolledComposite.setExpandVertical(true);
 		functionScrolledComposite.setExpandHorizontal(true);
 
 		final Composite functionComposite = new Composite(functionScrolledComposite, SWT.NONE);
 		functionComposite.setLayout(new FillLayout());
 		functionComposite.setLocation(0, 0);
 
 		/*
 		 * CREATE MARKER TABLE 
 		 */
 
 		final SashForm sashForm_1 = new SashForm(functionComposite, SWT.NONE);
 		sashForm_1.setOrientation(SWT.VERTICAL);
 		markerTableViewer = new TableViewer(sashForm_1, SWT.BORDER|SWT.MULTI);
 		markerTableViewer.setSorter(new Sorter());
 
 		markerTableViewer.setContentProvider(new MarkersContentProvider());
 		markerTableViewer.setLabelProvider(new MarkersLabelProvider());
 		
 		markerTableViewer.addSelectionChangedListener( new ISelectionChangedListener() {
 
 			/*
 			 * MARKERTABLE SELECTION CHANGED
 			 */
 			public void selectionChanged(SelectionChangedEvent event) {
 
 				refreshPlateTableSelection();
 			}
 		});
 
 		/*
 		 * TableColumns
 		 */
 		TableColumn column1 = new TableColumn(markerTableViewer.getTable(), SWT.LEFT, 0);
 		column1.setText(MARKER_TABLE_COLUMN_NAMES [0]);
 		column1.setWidth(100);
 		TableColumn column2 = new TableColumn(markerTableViewer.getTable(), SWT.LEFT, 1);
 		TableColumn column3 = new TableColumn(markerTableViewer.getTable(), SWT.LEFT, 2);
 		markerTableViewer.getTable().setLinesVisible(true);
 		markerTableViewer.getTable().setHeaderVisible(true);
 		
 		markerTableViewer.setInput(plateResults.getPlate());
 	
 		column2.setText(MARKER_TABLE_COLUMN_NAMES[1]);
 		column2.setWidth(100);
 		column3.setText(MARKER_TABLE_COLUMN_NAMES[2]);
 		column3.setWidth(100);
 		
 		functionComposite.setSize(655, 274);
 		functionScrolledComposite.setContent(functionComposite);
 
 		Composite bottom;
 		bottom = new Composite(parent, SWT.NONE);
 		bottom.setLayout(new FormLayout());
 		formData_1.bottom = new FormAttachment(bottom, -2, SWT.DEFAULT);
 		final FormData formData_2 = new FormData();
 		formData_2.right = new FormAttachment(center, 0, SWT.RIGHT);
 		formData_2.top = new FormAttachment(100, -43);
 		formData_2.bottom = new FormAttachment(100, -5);
 		formData_2.left = new FormAttachment(center, 0, SWT.LEFT);
 		bottom.setLayoutData(formData_2);
 
 		final Label viewWellFunctionLabel = new Label(bottom, SWT.NONE);
 		final FormData formData_3 = new FormData();
 		formData_3.left = new FormAttachment(0, 5);
 		formData_3.right = new FormAttachment(0, 120);
 		viewWellFunctionLabel.setLayoutData(formData_3);
 		viewWellFunctionLabel.setText("View well function");
 
 		/*
 		 * CREATE PLATE FUNCTIONS TABLE
 		 */
 		plateFunctionsTableViewer = new TableViewer(sashForm_1, SWT.BORDER);
 		plateFunctionsTableViewer.setContentProvider(new PlateFunctionsContentProvider());
 		plateFunctionsTableViewer.setLabelProvider(new PlateFunctionsTableLabelProvider());
 		sashForm_1.setWeights(new int[] {1, 1 });
 		
 		plateFunctionsTableViewer.setSorter( new ViewerSorter() {
 			@Override
 			public int compare(Viewer viewer, Object e1, Object e2) {
 			
 				if( e1 instanceof String[]      && 
 					e2 instanceof String[]      &&
 					( (String[])e1 ).length > 0 &&
 					( (String[])e2 ).length > 0 ) {
 					
 					return ( (String[])e1 )[0].compareTo(
 						   ( (String[])e2 )[0] );
 				}
 				
 				return super.compare(viewer, e1, e2);
 			}
 		});
 		
 		for(String columnName : PLATEFUNCTIONS_TABLE_COLUMN_NAMES) {
 			TableColumn column = new TableColumn( plateFunctionsTableViewer.getTable(), SWT.LEFT);
 			column.setText(columnName);
 			column.setWidth(100);	
 		}
 		plateFunctionsTableViewer.getTable().setLinesVisible(true);
 		plateFunctionsTableViewer.getTable().setHeaderVisible(true);
 		plateFunctionsTableViewer.setInput(plateResults);
 		
 		wellFunctionCombo = new Combo(bottom, SWT.NONE|SWT.READ_ONLY);
 		final FormData formData_4 = new FormData();
 		formData_4.bottom = new FormAttachment(viewWellFunctionLabel, 0, SWT.BOTTOM);
 		formData_4.left = new FormAttachment(0, 120);
 		formData_4.right = new FormAttachment(0, 235);
 		wellFunctionCombo.setLayoutData(formData_4);
 
 		/*
 		 * POPULATE WELLFUNCTIONCOMBO
 		 */
 		wellFunctionNames = new ArrayList<String>( toBeSaved.getWellFunctionNames() );
 		
 		wellFunctionCombo.setItems( wellFunctionNames.toArray(new String[0]) );
 		wellFunctionCombo.select( wellFunctionNames.indexOf("raw") );
 		
 		final PlateEditor editor = this;
 		wellFunctionCombo.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				plateTable.setModel(new PlateTableModel(toBeSaved, plateTable, editor, wellFunctionCombo.getText(), plateResults));
 			}
 		});
 		
 		markAsOutlierButton = new Button(bottom, SWT.NONE);
 		markAsOutlierButton.addSelectionListener(new SelectionAdapter() {
 			/*
 			 * MARK AS OUTLIER
 			 */
 			public void widgetSelected(final SelectionEvent e) {
 				for( Point p : plateTable.getCellSelection() ) {
 					toBeSaved.getWell(p).setOutlier(!outlierSelected);
 					plateResults.setOutlier( toBeSaved.getWell(p).getName(), 
 							                 !outlierSelected );
 				}
 				plateMultiPageEditor.fireOutliersChanged();
 				firePropertyChange(PROP_DIRTY);
 			}
 		});
 		final FormData formData_5 = new FormData();
 		formData_5.bottom = new FormAttachment(100, -5);
 		formData_5.right = new FormAttachment(wellFunctionCombo, 115, SWT.RIGHT);
 		formData_5.left = new FormAttachment(wellFunctionCombo, 5, SWT.RIGHT);
 		markAsOutlierButton.setLayoutData(formData_5);
 		markAsOutlierButton.setText("Mark as outlier");
 
 		Label markPlateAsLabel;
 		markPlateAsLabel = new Label(bottom, SWT.NONE);
 		formData_3.bottom = new FormAttachment(markPlateAsLabel, 20, SWT.TOP);
 		final FormData formData_6 = new FormData();
 		markPlateAsLabel.setLayoutData(formData_6);
 		markPlateAsLabel.setText("Mark plate as");
 
 		markPlateCombo = new Combo(bottom, SWT.NONE|SWT.READ_ONLY);
 		markPlateCombo.setEnabled(false);
 		formData_6.bottom = new FormAttachment(markPlateCombo, 0, SWT.BOTTOM);
 		formData_6.left = new FormAttachment(markPlateCombo, -90, SWT.LEFT);
 		formData_6.right = new FormAttachment(markPlateCombo, 0, SWT.LEFT);
 		final FormData formData_7 = new FormData();
 		formData_7.bottom = new FormAttachment(markAsOutlierButton, 0, SWT.BOTTOM);
 		formData_7.left = new FormAttachment(100, -90);
 		formData_7.right = new FormAttachment(100, -5);
 		markPlateCombo.setLayoutData(formData_7);
 
 		final Button copyDataToButton = new Button(bottom, SWT.NONE);
 		copyDataToButton.addSelectionListener(new SelectionAdapter() {
 			/*
 			 * Copy data to Clipboard
 			 */
 			public void widgetSelected(final SelectionEvent e) {
 				PlateTableModel model = (PlateTableModel)plateTable.getModel();
 				StringBuilder stringBuilder = new StringBuilder();
 				for( int row = 1 ; row < model.getRowCount() ; row++) {
 					for( int col = 1 ; col < model.getColumnCount() ; col++) {
 						stringBuilder.append( 
 								model.getValueAt(col, row).equals("-1.0") ? ""
 								                                        : model.getValueAt(col, row) ); 
 						stringBuilder.append('\t');
 					}
 					stringBuilder.append('\n');
 				}
 				
 				TextTransfer textTransfer = TextTransfer.getInstance();
 				Transfer[] types = new Transfer[] {textTransfer};
 				cb.setContents( new Object[]{ stringBuilder.toString() }, types );
 			}
 		});
 		final FormData fd_copyDataToButton = new FormData();
 		fd_copyDataToButton.top = new FormAttachment(markAsOutlierButton, 0, SWT.TOP);
 		fd_copyDataToButton.left = new FormAttachment(markAsOutlierButton, 5, SWT.RIGHT);
 		copyDataToButton.setLayoutData(fd_copyDataToButton);
 		copyDataToButton.setText("Copy data to Clipboard");
 		
 		sashForm.setWeights(new int[] {4, 1});
 		
 		barcodeText.setText(toBeSaved.getBarcode());
 	}
 		
 	private void refreshPlateTableSelection() {
 		
 		HashSet<String> markers = new HashSet<String>();
 		for( Object o : ((IStructuredSelection)markerTableViewer.getSelection()).toArray() ) {
 			MarkersTableRow row = (MarkersTableRow)o;
 			markers.add( row.getMarker() );
 		}
 		ArrayList<Point> selections = new ArrayList<Point>();
 		for( String m : markers ) {
 			selections.addAll( ( (PlateTableModel)plateTable.getModel() ).getPointsWithMarker(m) );
 		}
 		plateTable.clearSelection();
 		plateTable.setSelection(selections.toArray(new Point[0]), true);
 		refreshMarkerTableSelection();  //KTable is broken only fires cellselectionchanged for first selected cell so we need to do an extra call here
 	}
 	
 	private void refreshMarkerTableSelection() {
 	
 		HashSet<String> markers = new HashSet<String>();
 		Point[] selectedCells = plateTable.getCellSelection();
 		for(Point selection : selectedCells) {
 			markers.addAll( ( (PlateTableModel)plateTable.getModel() ).getMarkerNames(selection) );
 		}
 		markerTableViewer.getTable().deselectAll();
 		int[] selections = new int[markers.size()];
 		int i = 0;
 		for(String marker : markers) {
 			if( !"".equals(marker.substring(1)) ) {
 				selections[i++] = Integer.parseInt( marker.substring(1) ) - 1;
 			}
 		}
 		markerTableViewer.getTable().select(selections);
 	}	
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	static class PlateFunctionsTableLabelProvider extends LabelProvider implements ITableLabelProvider {
 		public String getColumnText(Object element, int columnIndex) {
 			return ((String[]) element)[columnIndex];
 		}
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 	}
 	
 	class PlateFunctionsContentProvider implements IStructuredContentProvider {
 		public Object[] getElements(Object inputElement) {
 			PlateResults plateResults = (PlateResults)inputElement;
 			ArrayList<String[]> rows = new ArrayList<String[]>();
 			for( PlateFunction pf : toBeSaved.getPlateFunctions() ) {
 				String[] row = new String[3];
 				row[0] = pf.getName();
 				try {
					row[1] = String.format( "%01.3f", plateResults.getValue( pf.getName() ) );
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				row[2] = "";
 				rows.add(row);
 			}
 			return rows.toArray();
 		}
 		public void dispose() {
 		}
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 	}
 
 	public void onOutLierChange() {
 		plateTable.setModel( new PlateTableModel( toBeSaved, 
                                                   plateTable, 
                                                   this, 
                                                   wellFunctionCombo.getText(), 
                                                   plateResults ) );
 	}
 }
