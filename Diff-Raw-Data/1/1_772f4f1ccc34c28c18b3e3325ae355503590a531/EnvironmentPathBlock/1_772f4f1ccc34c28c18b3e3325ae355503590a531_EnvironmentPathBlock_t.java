 package org.eclipse.dltk.ui.environment;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.ui.util.PixelConverter;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 
 public class EnvironmentPathBlock {
 	// private Text path;
 	private Table pathTable;
 	private TableViewer pathViewer;
 
 	/**
 	 * Environment to path association.
 	 */
 	private Map paths = new HashMap();
 
 	public EnvironmentPathBlock() {
 	}
 
 	private class PathLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof IEnvironment) {
 				switch (columnIndex) {
 				case 0:
 					return ((IEnvironment) element).getName();
 				case 1:
 					Object path = paths.get(element);
 					if (path != null) {
 						return (String) path;
 					}
 					return "";
 				default:
 					break;
 				}
 			}
 			return null;
 		}
 	}
 	public void createControl(Composite parent) {
 		createControl(parent, 1);
 	}
 	public void createControl(Composite parent, int columns ) {
 		PixelConverter conv = new PixelConverter(parent);
 
 		pathTable = new Table(parent, SWT.SINGLE | SWT.BORDER
 				| SWT.FULL_SELECTION);
 		pathTable.setHeaderVisible(true);
 		pathTable.setLinesVisible(true);
 		//GridData tableData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
 		//tableData.heightHint = conv.convertHeightInCharsToPixels(4);
 		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		tableData.heightHint = conv.convertHeightInCharsToPixels(4);
 		tableData.horizontalSpan = columns;
 		pathTable.setLayoutData(tableData);
 
 		pathViewer = new TableViewer(pathTable);
 
 		TableViewerColumn environmentsColumn = new TableViewerColumn(
 				pathViewer, SWT.NULL);
 		environmentsColumn.getColumn().setText(Messages.EnvironmentPathBlock_environment);
 		environmentsColumn.getColumn().setWidth(
 				conv.convertWidthInCharsToPixels(30));
 		TableViewerColumn pathColumn = new TableViewerColumn(pathViewer,
 				SWT.NULL);
 		pathColumn.getColumn().setText(Messages.EnvironmentPathBlock_path);
 		pathColumn.getColumn().setWidth(conv.convertWidthInCharsToPixels(40));
 		pathColumn.setEditingSupport(new EditingSupport(pathViewer) {
 			protected boolean canEdit(Object element) {
 				return true;
 			}
 
 			protected CellEditor getCellEditor(Object element) {
 				return new TextCellEditor(pathTable) {
 					private Button browse;
 					private Composite composite;
 
 					protected Control createControl(Composite compositeParent) {
 						composite = new Composite(compositeParent, SWT.NONE);
 						composite.setBackground(compositeParent.getBackground());
 						GridLayout layout = new GridLayout(2, false);
 						layout.marginLeft = -4;
 						layout.marginTop = -4;
 						layout.marginBottom = -4;
 						layout.marginRight = -4;
 						composite.setLayout(layout);
 						super.createControl(composite);
 						text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
 								true, false));
 						browse = new Button(composite, SWT.PUSH);
 						browse.setText("..."); //$NON-NLS-1$
 						Font font = new Font(compositeParent.getDisplay(), "arial", 6, 0); //$NON-NLS-1$
 						browse.setFont(font);
 						browse.setLayoutData(new GridData(SWT.DEFAULT,
 								SWT.FILL, false, true));
 						browse.addSelectionListener(new SelectionAdapter() {
 							public void widgetSelected(SelectionEvent e) {
 								editPath();
 								doFocusLost();
 							}
 						});
 						FocusAdapter listener = new FocusAdapter() {
 				            public void focusLost(FocusEvent e) {
 				            	Control cursorControl = composite.getDisplay().getCursorControl();
 				            	if( cursorControl != null ) {
 				            		if(cursorControl.equals( browse )) {
 				            			return;
 				            		}
 				            	}
 				                doFocusLost();
 				            }
 				        };
 				        browse.addFocusListener(listener);
 						text.addFocusListener(listener);
 						return composite;
 					}
 					
 					public void doFocusLost() {
 						super.focusLost();
 					}
 					
 					protected void focusLost() {
 					}
 				};
 			}
 
 			protected Object getValue(Object element) {
 				return paths.get(element);
 			}
 
 			protected void setValue(Object element, Object value) {
 				paths.put(element, value);
 				pathViewer.refresh();
 			}
 		});
 
 		pathViewer.setLabelProvider(new PathLabelProvider());
 		pathViewer.setContentProvider(new IStructuredContentProvider() {
 			public Object[] getElements(Object inputElement) {
 				if (inputElement instanceof IEnvironment[]) {
 					return (Object[]) inputElement;
 				}
 				return new Object[0];
 			}
 
 			public void dispose() {
 			}
 
 			public void inputChanged(Viewer viewer, Object oldInput,
 					Object newInput) {
 			}
 		});
 		pathViewer.setInput(EnvironmentManager.getEnvironments());
 	}
 
 	public void addSelectionListener(ISelectionChangedListener listener) {
 		pathViewer.addSelectionChangedListener(listener);
 	}
 
 	public void removeSelectionListener(ISelectionChangedListener listener) {
 		pathViewer.removeSelectionChangedListener(listener);
 	}
 
 	protected void editPath() {
 		ISelection selection = pathViewer.getSelection();
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection sel = (IStructuredSelection) selection;
 			IEnvironment environment = (IEnvironment) sel.getFirstElement();
 			IEnvironmentUI ui = (IEnvironmentUI) environment
 					.getAdapter(IEnvironmentUI.class);
 			String file = ui.selectFile(this.pathTable.getShell(),
 					IEnvironmentUI.EXECUTABLE);
 			if (file != null) {
 				this.paths.put(environment, file);
 				this.pathViewer.refresh();
 			}
 		}
 	}
 
 	public IStructuredSelection getSelection() {
 		return (IStructuredSelection) pathViewer.getSelection();
 	}
 
 	public void setPaths(Map paths) {
 		this.paths = paths;
 		pathTable.getDisplay().asyncExec(new Runnable(){
 			public void run() {
 				pathViewer.refresh();
 			}			
 		});
 	}
 
 	public Map getPaths() {
 		return this.paths;
 	}
 }
