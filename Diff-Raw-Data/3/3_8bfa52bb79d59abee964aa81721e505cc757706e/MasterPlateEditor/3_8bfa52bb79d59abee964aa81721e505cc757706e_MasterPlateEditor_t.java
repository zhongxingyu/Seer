 package net.bioclipse.brunn.ui.editors.masterPlateEditor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.plate.IPlateManager;
 import net.bioclipse.brunn.business.sample.ISampleManager;
 import net.bioclipse.brunn.pojos.AbstractSample;
 import net.bioclipse.brunn.pojos.AuditType;
 import net.bioclipse.brunn.pojos.ConcUnit;
 import net.bioclipse.brunn.pojos.DrugOrigin;
 import net.bioclipse.brunn.pojos.DrugSample;
 import net.bioclipse.brunn.pojos.ILISObject;
 import net.bioclipse.brunn.pojos.MasterPlate;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.SampleContainer;
 import net.bioclipse.brunn.pojos.SampleMarker;
 import net.bioclipse.brunn.pojos.Well;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.ui.Activator;
 import net.bioclipse.brunn.ui.dialogs.AddDrugToMasterPlate;
 import net.bioclipse.brunn.ui.transferTypes.BrunnTransfer;
 import net.bioclipse.brunn.ui.editors.masterPlateEditor.model.MarkersModel;
 import net.bioclipse.brunn.ui.editors.plateEditor.PlateMultiPageEditor;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.MarkersTableRow;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ICellModifier;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 
 import de.kupzog.ktable.KTable;
 import de.kupzog.ktable.KTableModel;
 import de.kupzog.ktable.SWTX;
 
 public class MasterPlateEditor extends EditorPart {
 
 	public final static String ID = "net.bioclipse.brunn.ui.editors.masterPlateEditor.MasterPlateEditor"; 
 	
 	public final static double UNDEFINED_CONCENTRATION = -1; 
 	
 	private KTable markersTable;
 	
 	public static final String[] COLUMN_NAMES = {"Marker", "Compound"};
 	
 	private volatile MasterPlate referenceMasterPlate;
 	private volatile MasterPlate toBeSaved;
 	private net.bioclipse.brunn.ui.explorer.model.nonFolders.MasterPlate masterPlate;
 
 	private Table compoundsTable;
 
 	private TableViewer tableViewer;
 	
 	public MasterPlateEditor(MasterPlate toBeSaved) {
 		super();
 		this.toBeSaved = toBeSaved;
 	}
 	
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		IPlateManager pm = (IPlateManager) Springcontact.getBean("plateManager");
 		pm.edit( Activator.getDefault().getCurrentUser(), toBeSaved );
 		pm.evictfromLazyLoading(toBeSaved);
 		
 		referenceMasterPlate = toBeSaved.deepCopy();
 		firePropertyChange(PROP_DIRTY);
 		masterPlate.getParent().fireUpdate();
 		monitor.done();
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);
 
 		masterPlate = (net.bioclipse.brunn.ui.explorer.model.nonFolders.MasterPlate)input;
 		setPartName(masterPlate.getName());
 		
 		MasterPlate masterPlate = ((MasterPlate) ((net.bioclipse.brunn.ui.explorer.model.nonFolders.MasterPlate) input)
 				            .getPOJO()); 
		toBeSaved = masterPlate.deepCopy();
 		referenceMasterPlate = toBeSaved.deepCopy();
 	}
 
 	@Override
 	public boolean isDirty() {
 		return !referenceMasterPlate.equals(toBeSaved);
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout());
 		Composite center = new Composite(parent, SWT.NULL);
 		center.setLayout(new FillLayout());
 
 		final SashForm sashForm = new SashForm(center, SWT.NONE);
 		sashForm.setOrientation(SWT.VERTICAL);
 
 		/*
 		 * Create markerstable
 		 */
 		markersTable = new KTable(sashForm, SWT.V_SCROLL           |
                 							SWT.H_SCROLL           |
                                             SWTX.MARK_FOCUS_HEADERS|
                                             SWTX.FILL_WITH_LASTCOL );
 		markersTable.setModel(new MarkersModel(toBeSaved, markersTable, this));
 		
 		compoundsTable = new Table(sashForm, SWT.MULTI         | 
                                              SWT.BORDER         | 
                                              SWT.H_SCROLL       | 
                                              SWT.V_SCROLL       | 
 			                                 SWT.FULL_SELECTION | 
 			                                 SWT.HIDE_SELECTION );
 		TableColumn column1 = new TableColumn(compoundsTable, SWT.CENTER, 0);
 		column1.setText(COLUMN_NAMES[0]);
 		column1.setWidth(139);
 		TableColumn column2 = new TableColumn(compoundsTable, SWT.LEFT, 1);
 		column2.setText(COLUMN_NAMES[1]);
 		column2.setWidth(400);
 		
 		compoundsTable.setLinesVisible(true);
 		compoundsTable.setHeaderVisible(true);
 		
 		tableViewer = new TableViewer(compoundsTable);
 		tableViewer.setUseHashlookup(true);
 		tableViewer.setColumnProperties(COLUMN_NAMES);
 		
 		CellEditor[] editors = new CellEditor[COLUMN_NAMES.length];
 		editors[1] = new TextCellEditor();
 		tableViewer.setCellEditors(editors);
 		tableViewer.setContentProvider( new CompoundsTableContentProvider() );
 		tableViewer.setLabelProvider(   new CompoundsTableLabelProvider()   );
 		tableViewer.setCellModifier(    new CompoundsCellModifier()         );
 		tableViewer.setInput(toBeSaved);
 		
 		Transfer[] transfers = new Transfer[] { BrunnTransfer.getInstance() };
 		tableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers, new CompoundDropAdapter(tableViewer));
 		
 		createCompoundsTableMenu();
 	}
 
 	private void createCompoundsTableMenu() {
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager mgr) {
 				TableItem[] items = compoundsTable.getSelection();
 				final List<String> markerNamesToClear = new ArrayList<String>();
 				for( TableItem item : items) {
 					markerNamesToClear.add( ((SampleMarker) item.getData()).getName() );
 				}
 				mgr.add(new Action("Clear Marker" + (items.length == 1 ? "" : "s")) {
 					public void run() {
 						for( String name : markerNamesToClear ) {
 							for( Well well : toBeSaved.getWells() ) {
 								for( SampleMarker sampleMarker : well.getSampleMarkers() ) {
 									if( sampleMarker.getName().equals(name) ) {
 										AbstractSample sample = sampleMarker.getSample();
 										if( sample == null ) {
 											continue;
 										}
 										sampleMarker.setSample(null);
 										sampleMarker.getWell().getSampleContainer().getSamples().remove(sample);
 										sample.setSampleMarker(null);
 										sample.setSampleContainer(null);										
 									}
 								}
 							}
 						}
 						tableViewer.refresh();
 						refresh();
 					}
 				});
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(compoundsTable);
 		compoundsTable.setMenu(menu);
 		//getSubstanceNames();
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public String[][] getSubstanceNames() {
 		TableItem[] items = compoundsTable.getItems();
 		String[][] substances = new String[items.length][2];
 		int count = 0;
 		boolean found = false;
 		for(TableItem item : items) {
 			substances[count][0] = ((SampleMarker)item.getData()).getName();
 			substances[count][1] = "";
 			for( Well well : toBeSaved.getWells() ) {
 				for( SampleMarker sampleMarker : well.getSampleMarkers() ) {
 					if(sampleMarker.getName().equals( ((SampleMarker)item.getData()).getName()) ) {
 						AbstractSample sample = sampleMarker.getSample();
 						if( sampleMarker.getSample() == null ) {
 							continue;
 						}
 						//substances[count][0] = sampleMarker.getName(); //already done in the beginning
 						substances[count][1] = sample.getName();
 						count++;
 						found = true;
 						break;
 					}
 				}
 				if(found) {
 					found = false;
 					break;
 				}
 			}
 		}
 		//filters out markers with no attached compound
 		int numCompounds = 0;
 		for(int i=0; i<substances.length; i++) {
 			if(substances[i][1] != "") {
 				numCompounds++;
 			}
 		}
 		String[][] compounds = new String[numCompounds][2]; 
 		for(int i=0, j=0; i<substances.length; i++) {
 			if(substances[i][1] != "") {
 				compounds[j][0] = substances[i][0];
 				compounds[j][1] = substances[i][1];
 				j++;
 			}
 		}
 		return compounds;
 	}
 	
 	public String[][] getMasterPlateLayout() {
 		KTableModel tableModel = markersTable.getModel();
 		String[][] results = new String[tableModel.getRowCount()][tableModel.getColumnCount()];
 		for(int i=0; i<tableModel.getRowCount(); i++) {
 			for(int j=0; j<tableModel.getColumnCount(); j++) {
 				results[i][j] = tableModel.getContentAt(j,i).toString();
 			}
 		}
 		return results;
 	}
 	
 	public String getMasterPlateName() {
 		return referenceMasterPlate.getName();
 	}
 	
 	class CompoundsTableContentProvider implements IStructuredContentProvider {
 
 		public Object[] getElements(Object inputElement) {
 			
 			MasterPlate masterPlate = (MasterPlate)inputElement;
 			
 			List<SampleMarker> list = new LinkedList<SampleMarker>(masterPlate.getDrugMarkers());
 			Comparator<SampleMarker> c = new Comparator<SampleMarker>() {
 	          	public int compare(SampleMarker arg0, SampleMarker arg1) {
 	          		try {
 	          			int M1 = Integer.parseInt( arg0.getName().substring(1) );
 		    			int M2 = Integer.parseInt( arg1.getName().substring(1) );
 		    			return M1 - M2;
 	          		}
 	          		catch (NumberFormatException e) {
 	          			return arg0.getName().compareTo( arg1.getName() );
 	          		}
 	          	}
 	        };
 	        Collections.sort(list, c);
 	        
 			return list.toArray();
 		}
 
 		public void dispose() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 	
 	class CompoundsCellModifier implements ICellModifier {
 
 		public boolean canModify(Object element, String property) {
 			// TODO Auto-generated method stub
 			System.out.println("canModify()");
 			return false;
 		}
 
 		public Object getValue(Object element, String property) {
 			
 			int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
 			SampleMarker marker = (SampleMarker)element;
 			String result = "";
 			switch (columnIndex) {
 			case 0:
 				result = marker.getName();
 				break;
 				
 			case 1:
 				result = marker.getSample().getName();
 				break;
 			default:
 				break;
 			}
 			return result;
 		}
 
 		public void modify(Object element, String property, Object value) {
 			// TODO Auto-generated method stub
 		}
 
 	}
 
 	class CompoundsTableLabelProvider implements ITableLabelProvider {
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 
 			SampleMarker marker = (SampleMarker)element;
 			String result = "";
 			switch (columnIndex) {
 			case 0:
 				result = marker.getName();
 				break;
 				
 			case 1:
 				if(marker.getSample() != null) {
 					result = marker.getSample().getName();
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("unknown columnindex");
 			}
 			return result;
 		}
 
 		public void addListener(ILabelProviderListener listener) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void dispose() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public boolean isLabelProperty(Object element, String property) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void removeListener(ILabelProviderListener listener) {
 			// TODO Auto-generated method stub
 			
 		}
 
 	}
 	
 	public class CompoundDropAdapter extends ViewerDropAdapter {
 
 		protected CompoundDropAdapter(Viewer viewer) {
 			super(viewer);
 		}
 
 		@Override
 		public boolean performDrop(Object data) {
 
 			SampleMarker sampleMarker = (SampleMarker)getCurrentTarget();
 			
 			if( sampleMarker.getSample() != null ) {
 				MessageDialog.openInformation( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
 						                       "Can not drop Compound", 
 						                       "There is already a compound set for this marker");
 				return false;
 			}
 			
 			AddDrugToMasterPlate dialog = new AddDrugToMasterPlate( PlatformUI
 					                                                .getWorkbench()
 					                                                .getActiveWorkbenchWindow()
 					                                                .getShell() );
 			if( dialog.open() == dialog.OK ) {
 				
 				if(sampleMarker == null) {
 					sampleMarker = (SampleMarker)getViewer().getInput();
 				}
 				ILISObject[] toDrop = (ILISObject[])data; 
 				createSample( sampleMarker, 
 						      toDrop, 
 						      dialog.getConcentration(), 
 						      dialog.getDilutionFactor(),
 						      dialog.getConcUnit(),
 						      dialog.isDoingHorizontalDilutionSeries() );
 				
 				getViewer().refresh();
 				refresh();
 				
 				return true;
 			}
 			return false;
 		}
 
 		private void createSample( SampleMarker sampleMarker, 
 				                   ILISObject[] toDrop, 
 				                   double startConcentration, 
 				                   double dilutionfactor,
 				                   ConcUnit concUnit, 
 				                   boolean doingHorizonalDilution ) {
 			
 			Map< Integer, List<Well> > wellsByDilutionSerie = new HashMap< Integer, List<Well> >();  
 			
 			for(Well well : toBeSaved.getWells() ) {
 				int identifier = doingHorizonalDilution ? well.getCol() : well.getRow();
 				for(SampleMarker marker : well.getSampleMarkers()) {
 					if(marker.getName().equals(sampleMarker.getName())) {
 						
 						List<Well> wellsInDilutionSerie = wellsByDilutionSerie.get(identifier);
 						
 						if( wellsInDilutionSerie == null ) {
 							wellsInDilutionSerie = new ArrayList<Well>();
 							wellsByDilutionSerie.put(identifier, wellsInDilutionSerie);
 						}
 						
 						wellsInDilutionSerie.add(well);
 					}
 				}
 			}
 			List<Integer> dilutionSerie = new ArrayList<Integer>(wellsByDilutionSerie.keySet());
 			Collections.sort(dilutionSerie);
 			//To get the vertical series going in the right direction.
 			if (!doingHorizonalDilution) {
 				Collections.reverse(dilutionSerie);
 			}
 			int i = 0;
 			for( Integer identifier : dilutionSerie ) {
 				double concentration = startConcentration * Math.pow(dilutionfactor, -i++);
 				for( Well well : wellsByDilutionSerie.get(identifier) ) {
 					for(SampleMarker marker : well.getSampleMarkers()) {
 						if(marker.getName().equals(sampleMarker.getName())) {
 							DrugSample drugSample = new DrugSample( Activator.getDefault().getCurrentUser(),
 		                                		                    toDrop[0].getName(),
 		                                		                    concentration,
 		                                		                    (DrugOrigin)toDrop[0], 
 		                                		                    well.getSampleContainer(), 
 		                                		                    concUnit );
 							drugSample.setSampleMarker(marker);
 							marker.setSample(drugSample);
 						}
 					}
 				}
 			}
 		}
 
 		@Override
 		public boolean validateDrop(Object target, int operation, TransferData transferType) {
 			return BrunnTransfer.getInstance().isSupportedType(transferType);
 		}
 	}
 	
 	public void refresh() {
 		((MarkersModel)markersTable.getModel()).refresh();
 		markersTable.redraw();
 		firePropertyChange(PROP_DIRTY);
 	}
 	
 	static class MarkerSorter extends ViewerSorter {
 		public int compare(Viewer viewer, Object e1, Object e2) {
 			MarkersTableRow item1 = (MarkersTableRow)e1;
 			MarkersTableRow item2 = (MarkersTableRow)e2;
 			int M1 = Integer.parseInt( item1.getMarker().substring(1) );
 			int M2 = Integer.parseInt( item2.getMarker().substring(1) );
 			return M1 - M2;
 		}
 	}
 }
