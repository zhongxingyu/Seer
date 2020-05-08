 package com.netxforge.netxstudio.screens.f4;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.IEMFValueProperty;
 import org.eclipse.emf.databinding.edit.EMFEditProperties;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
 import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
 import org.eclipse.nebula.widgets.grid.Grid;
 import org.eclipse.nebula.widgets.grid.GridColumn;
 import org.eclipse.nebula.widgets.grid.GridItem;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 import com.netxforge.netxstudio.common.Tuple;
 import com.netxforge.netxstudio.metrics.MappingColumn;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.screens.editing.selector.IDataScreenInjection;
 import com.netxforge.netxstudio.screens.editing.selector.ScreenUtil;
 import com.netxforge.netxstudio.screens.f4.support.ColumnMappingMenu;
 import com.netxforge.netxstudio.screens.f4.support.ColumnMappingMenu.MappingMenuListener;
 import com.netxforge.netxstudio.screens.ide.WorkspaceUtil;
 
 public class NewEditMappingXLS extends AbstractFileBasedMapping implements
 		IDataScreenInjection {
 
 	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
 	private Text txtSheetNumber;
 	// private Text txtDataRow;
 	private Text txtSelectedXLSPath;
 	// private MetricSource metricSource;
 	private GridTableViewer gridTableViewer;
 	private Menu gridMenu;
 	private MappingMenuListener mmListener;
 
 	/**
 	 * Create the composite.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public NewEditMappingXLS(Composite parent, int style) {
 		super(parent, style);
 		addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				toolkit.dispose();
 			}
 		});
 		toolkit.adapt(this);
 		toolkit.paintBordersFor(this);
 		// buildUI();
 	}
 
 	protected void buildUI() {
 
 		buildSashComposites();
 		buildXLSSpecificSection(leftComposite);
 		buildGeneralSection(leftComposite);
 		buildHeaderMappingSection(leftComposite);
 		buildDataMappingSection(leftComposite);
 		buildInteractiveSection(rightComposite);
 
 	}
 
 	protected void buildInteractiveSection(Composite parentComposite) {
 		Section sctnXLSInteractive = toolkit.createSection(parentComposite,
 				Section.EXPANDED | Section.TITLE_BAR);
 		// fd_sctnSummary.top = new FormAttachment(sctnXLSInteractive, 0,
 		// SWT.TOP);
 		// fd_sctnSummary.right = new FormAttachment(0, 355);
 		// FormData fd_sctnXLSInteractive = new FormData();
 		// fd_sctnXLSInteractive.top = new FormAttachment(0, 10);
 		// fd_sctnXLSInteractive.bottom = new FormAttachment(100, -12);
 		// fd_sctnXLSInteractive.left = new FormAttachment(0, 370);
 		// fd_sctnXLSInteractive.right = new FormAttachment(100, -12);
 		// sctnXLSInteractive.setLayoutData(fd_sctnXLSInteractive);
 		toolkit.paintBordersFor(sctnXLSInteractive);
 
 		Composite composite_2 = toolkit.createComposite(sctnXLSInteractive,
 				SWT.NONE);
 		toolkit.paintBordersFor(composite_2);
 		sctnXLSInteractive.setClient(composite_2);
 		composite_2.setLayout(new GridLayout(2, false));
 
 		Button btnSelectXls = toolkit.createButton(composite_2, "Select XLS",
 				SWT.NONE);
 		btnSelectXls.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				IPath[] paths = WorkspaceUtil.INSTANCE
 						.browseWorkspace(NewEditMappingXLS.this.getShell());
 				if (paths == null || paths.length == 0)
 					return;
 				// We only process the first selection.
 				final IFile f = WorkspaceUtil.INSTANCE
 						.createFileHandle(paths[0]);
 				txtSelectedXLSPath.setText(f.getName());
 				loadXLSSampleFile(metricSource, f);
 			}
 
 		});
 
 		txtSelectedXLSPath = toolkit.createText(composite_2, "New Text",
 				SWT.READ_ONLY);
 		txtSelectedXLSPath.setText("<....>");
 		txtSelectedXLSPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
 				true, false, 1, 1));
 
 		CTabFolder tabFolder = new CTabFolder(composite_2, SWT.BORDER);
 		tabFolder.setTabHeight(20);
 		tabFolder.setTabPosition(SWT.BOTTOM);
 		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,
 				2));
 		toolkit.adapt(tabFolder);
 		toolkit.paintBordersFor(tabFolder);
 		tabFolder.setSelectionBackground(SWTResourceManager.getColor(240, 255,
 				255));
 
 		CTabItem tbtmSheet1 = new CTabItem(tabFolder, SWT.NONE);
 		tbtmSheet1.setText("Sheet 0");
 
 		tabFolder.setSelection(tbtmSheet1);
 
 		Composite composite = new Composite(tabFolder, SWT.BORDER);
 		tbtmSheet1.setControl(composite);
 		toolkit.paintBordersFor(composite);
 		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		gridTableViewer = new GridTableViewer(composite, SWT.H_SCROLL
 				| SWT.V_SCROLL | SWT.BORDER);
 		Grid grid = gridTableViewer.getGrid();
 		grid.setCellSelectionEnabled(true);
 		grid.setHeaderVisible(true);
 		grid.setRowHeaderVisible(true);
 		toolkit.paintBordersFor(grid);
 		grid.addSelectionListener(gridSelector);
 		buildFixedColumns(gridTableViewer);
 
 		createGridMenu(grid);
 	}
 
 	private void createGridMenu(Grid grid) {
 		gridMenu = new Menu(grid);
 		grid.setMenu(gridMenu);
 		// Delegate to a singleton holding the MappingMenuListener class.
 		mmListener = ColumnMappingMenu.getINSTANCE().new MappingMenuListener(
 				gridMenu, mapping, screenService, super.txtFirstHeaderRow,
 				super.txtFirstDataRow);
 		mmListener.setMetricSource(metricSource);
 
 		gridMenu.addMenuListener(mmListener);
 	}
 
 	protected void buildXLSSpecificSection(Composite parentComposite) {
 
 		Section sctnSummary = toolkit.createSection(parentComposite,
 				Section.EXPANDED | Section.TITLE_BAR);
 		toolkit.paintBordersFor(sctnSummary);
 		sctnSummary.setText("Basic");
 		sctnSummary.setExpanded(true);
 
 		Composite composite_1 = toolkit.createComposite(sctnSummary, SWT.NONE);
 		toolkit.paintBordersFor(composite_1);
 		sctnSummary.setClient(composite_1);
 		composite_1.setLayout(new GridLayout(2, false));
 
 		Label lblSheetName = toolkit.createLabel(composite_1, "Sheet Index:",
 				SWT.NONE);
 		lblSheetName.setAlignment(SWT.RIGHT);
 		GridData gd_lblSheetName = new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_lblSheetName.widthHint = 80;
 		lblSheetName.setLayoutData(gd_lblSheetName);
 
 		txtSheetNumber = toolkit.createText(composite_1, "New Text", SWT.NONE);
 		txtSheetNumber.setText("");
 		GridData gd_txtSheetName = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_txtSheetName.widthHint = 20;
 		txtSheetNumber.setLayoutData(gd_txtSheetName);
 
 	}
 
 	/**
 	 * Aggregates the selection in the grid, used by column and the grid.
 	 * 
 	 */
 	GridSelectionListener gridSelector = new GridSelectionListener();
 
 	class GridSelectionListener extends SelectionAdapter {
 		public void widgetSelected(SelectionEvent e) {
 			System.out.println(e);
 			updateSelection(e);
 		}
 	}
 
 	/**
 	 * Updates the row and column index to be used, by the screen further on.
 	 * 
 	 * @param e
 	 */
 	private void updateSelection(SelectionEvent e) {
 		Point[] p = gridTableViewer.getGrid().getCellSelection();
 		if (p.length >= 1) {
 			mmListener.setCurrentColumnIndex(p[0].x);
 			mmListener.setCurrentRowIndex(p[0].y);
 		}
 	}
 
 	private void buildFixedColumns(GridTableViewer v) {
 		int i = 0;
 		for (; i < 100; i++) {
 			GridViewerColumn gvc = new GridViewerColumn(gridTableViewer,
 					SWT.NONE);
 			GridColumn newGridColumn = gvc.getColumn();
 			newGridColumn.setAlignment(SWT.CENTER);
 			newGridColumn.setWidth(80);
 			newGridColumn.addSelectionListener(gridSelector);
 			newGridColumn.setText(new Integer(i).toString());
 		}
 	}
 
 	// FIXME, Algorithm for column header is not correct yet.
 	// private void buildFixedColumns(GridTableViewer v) {
 	// char[] alphabet = this.getAlphabet();
 	// int i = 0;
 	// int primaryIndex = 0;
 	// for (; i < 70; i++) {
 	// GridViewerColumn gvc = new GridViewerColumn(gridTableViewer,
 	// SWT.NONE);
 	// GridColumn newGridColumn = gvc.getColumn();
 	// newGridColumn.setAlignment(SWT.CENTER);
 	// newGridColumn.setWidth(80);
 	// newGridColumn.addSelectionListener(gridSelector);
 	// StringBuilder sb = new StringBuilder();
 	//
 	// int aIndex = i % alphabet.length;
 	// if (i >= alphabet.length) {
 	// sb.append(Character.toUpperCase(alphabet[primaryIndex]));
 	// sb.append(Character.toUpperCase(alphabet[aIndex]));
 	// if (aIndex == 0) {
 	// primaryIndex = primaryIndex % (alphabet.length - 1);
 	// primaryIndex++;
 	// }
 	// } else {
 	// sb.append(Character.toUpperCase(alphabet[aIndex]));
 	// }
 	// newGridColumn.setText(sb.toString());
 	// }
 	// }
 
 	public char[] getAlphabet() {
 		char[] alphabet = new char[26];
 		int index, ordinalVal = 97;
 		char element;
 
 		for (index = 0; index < alphabet.length; ++index, ++ordinalVal) {
 			element = (char) ordinalVal;
 			alphabet[index] = element;
 		}
 		return alphabet;
 	}
 
 	public void fillGrid(List<Map<Integer, Tuple>> records) {
 		this.gridTableViewer.setInput(records);
 	}
 
 	public void initXLSSpecificDataBindings(EMFDataBindingContext context) {
 		IObservableValue sheetNumberObservableValue = SWTObservables
 				.observeText(txtSheetNumber, SWT.Modify);
 		IEMFValueProperty sheetNumberProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				MetricsPackage.Literals.MAPPING_XLS__SHEET_NUMBER);
 		context.bindValue(sheetNumberObservableValue,
 				sheetNumberProperty.observe(mapping), null, null);
 	}
 
 	private void initInteractiveBinding() {
 		this.gridTableViewer.setContentProvider(new XLSGridContentProvider());
 		gridTableViewer.setLabelProvider(new XLSGridLabelProvider());
 
 		// Make sure our row headers, show starting with 0.
 		gridTableViewer.setRowHeaderLabelProvider(new CellLabelProvider() {
 			@Override
 			public void update(ViewerCell cell) {
 				GridItem gi = (GridItem) cell.getItem();
 				int index = gridTableViewer.getGrid().indexOf(gi);
 				cell.setText(new Integer(index).toString());
 			}
 
 		});
 	}
 
 	class XLSGridLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof Map<?, ?>) {
 				Map<?, ?> row = (Map<?, ?>) element;
				if (columnIndex < row.size()) {
					Tuple cell = (Tuple) row.get(columnIndex);
					if (cell != null) {
						Object v = cell.getValue();
						return v.toString();
					}
 				}
 			}
 			return "";
 		}
 
 	}
 
 	class XLSGridContentProvider implements IStructuredContentProvider {
 
 		public void dispose() {
 			// ?
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// Not applicable.
 		}
 
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof List<?>) {
 				List<?> input = (List<?>) inputElement;
 				Object[] elements = input.toArray();
 				return elements;
 			}
 			return null;
 		}
 	}
 
 	// ImmutableMap<?, String> dataKindMap = ImmutableMap.of(
 	// IdentifierDataKindImpl.class, "Identifier",
 	// ValueDataKindImpl.class, "Value");
 	//
 	// private class ColumnObservableMapLabelProvider extends
 	// ObservableMapLabelProvider {
 	//
 	// public ColumnObservableMapLabelProvider(IObservableMap attributeMap) {
 	// super(attributeMap);
 	// }
 	//
 	// public ColumnObservableMapLabelProvider(IObservableMap[] observeMaps) {
 	// super(observeMaps);
 	// }
 	//
 	// @Override
 	// public String getColumnText(Object element, int columnIndex) {
 	// MappingColumn c = (MappingColumn) element;
 	// if (columnIndex == 0) {
 	// // This is the type Column.
 	// DataKind k = c.getDataType();
 	// if (k != null) {
 	// return dataKindMap.get(k.getClass());
 	// }
 	// }
 	// if (columnIndex == 2) {
 	// if (c.getDataType() instanceof ValueDataKind) {
 	// ValueKindType vkt = ((ValueDataKind) c.getDataType())
 	// .getValueKind();
 	// return vkt.getName();
 	// }
 	// if (c.getDataType() instanceof IdentifierDataKind) {
 	// IdentifierDataKind idk = (IdentifierDataKind) c
 	// .getDataType();
 	// ObjectKindType okt = idk.getObjectKind();
 	// return okt.getName();
 	// }
 	//
 	// }
 	//
 	// return super.getColumnText(element, columnIndex);
 	// }
 	// }
 
 	public void injectData(Object owner, Object object) {
 		super.injectData(owner, object);
 
 		boolean edit = ScreenUtil.isEditOperation(getOperation());
 		String actionText = edit ? "Edit: " : "New: ";
 		frmMappings.setText(actionText + " XLS Mapping: "
 				+ metricSource.getName());
 		
 		IFile file = this.getMetricSourceSampleFile(metricSource);
 		if (file != null) {
 			this.txtSelectedXLSPath.setText(file.getName());
 			this.loadXLSSampleFile(metricSource, file);
 		}
 	}
 
 	public void addData() {
 		if (ScreenUtil.isNewOperation(getOperation()) && metricSource != null) {
 			// If new, we have been operating on an object not added yet.
 			Command c = new SetCommand(editingService.getEditingDomain(),
 					metricSource,
 					MetricsPackage.Literals.METRIC_SOURCE__METRIC_MAPPING,
 					mapping);
 			editingService.getEditingDomain().getCommandStack().execute(c);
 		} else if (ScreenUtil.isEditOperation(getOperation())) {
 			// If edit, we have been operating on a copy of the object, so we
 			// have to replace. However if our original object is invalid, this
 			// will
 			// cause invalidity, so the action will not occure in case the
 			// original is
 			// invalid, and we should cancel the action and warn the user.
 			if (mapping.cdoInvalid()) {
 				MessageDialog
 						.openWarning(Display.getDefault().getActiveShell(),
 								"Conflict",
 								"There is a conflict with another user. Your changes can't be saved.");
 				return;
 			}
 			System.out.println(mapping.cdoID() + "" + mapping.cdoState());
 
 		}
 		// After our edit, we shall be dirty
 		if (editingService.isDirty()) {
 			editingService.doSave(new NullProgressMonitor());
 		}
 
 	}
 
 	public Viewer getViewer() {
 		return null;
 	}
 
 	@Override
 	public boolean isValid() {
 		return true;
 	}
 
 	public void disposeData() {
 		// N/A
 
 	}
 
 	@Override
 	public void fillCSVGrid(String[][] records) {
 		// N/A
 	}
 
 	public String getScreenName() {
 		return "Mapping XLS";
 	}
 
 	@Override
 	protected void setGridSelection(MappingColumn mc, int row) {
 		int column = mc.getColumn();
 		if (this.gridTableViewer.getInput() != null) {
 			gridTableViewer.getGrid().setCellSelection(new Point(column, row));
 		}
 	}
 
 	public EMFDataBindingContext initDataBindings_() {
 		EMFDataBindingContext context = new EMFDataBindingContext();
 		// TODO, Validations and strategies.
 		initGeneralBinding(context);
 		initXLSSpecificDataBindings(context);
 		initHeaderMappingBinding(context);
 		initDataMappingBinding(context);
 		initInteractiveBinding();
 		return context;
 	}
 }
