 package net.sparktank.morrigan.editors;
 
 import net.sparktank.morrigan.model.media.MediaList;
 import net.sparktank.morrigan.model.media.MediaTrack;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 public class MediaListEditor extends EditorPart {
 	public static final String ID = "net.sparktank.morrigan.editors.MediaListEditor";
 	
 	private MediaList editedMediaList;
 	private TableViewer editTable;
 	
 	public MediaListEditor() {
 	}
 	
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO save changes.
 		
 //		person.getAddress().setCountry(text2.getText());
 	}
 	
 	@Override
 	public void doSaveAs() {
 	}
 	
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);
 		editedMediaList = ((MediaListEditorInput) input).getEditedMediaList();
 		setPartName(editedMediaList.getListName());
 	}
 	
 	@Override
 	public boolean isDirty() {
 		
		// TODO
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		editTable = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		
 		// add columns.
 		String[] titles = { "file", "size" };
 		int[] bounds = { 400, 100 };
 		
 		for (int i = 0; i < titles.length; i++) {
 			TableViewerColumn column = new TableViewerColumn(editTable, SWT.NONE);
 			column.getColumn().setText(titles[i]);
 			column.getColumn().setWidth(bounds[i]);
 			column.getColumn().setResizable(true);
 			column.getColumn().setMoveable(true);
 		}
 		Table table = editTable.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		// where the data is coming from.
 		editTable.setContentProvider(new IStructuredContentProvider() {
 			@Override
 			public Object[] getElements(Object inputElement) {
 				return editedMediaList.getMediaTracks().toArray();
 			}
 			@Override
 			public void dispose() {
 			}
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			}
 		});
 		
 		// how the data will appear.
 		editTable.setLabelProvider(new ITableLabelProvider() {
 			@Override
 			public Image getColumnImage(Object element, int columnIndex) {
 				return null;
 			}
 			@Override
 			public String getColumnText(Object element, int columnIndex) {
 				MediaTrack elm = (MediaTrack) element;
 				switch (columnIndex) {
 				case 0:
 					return elm.getFilepath();
 				case 1:
 					return "0";
 				default:
 					throw new RuntimeException("Invalid column; '" + columnIndex + "'.");
 				}
 			}
 			@Override
 			public void addListener(ILabelProviderListener listener) {
 				// not needed.
 			}
 			@Override
 			public void dispose() {
 				// not needed.
 			}
 			@Override
 			public boolean isLabelProperty(Object element, String property) {
 				return false;
 			}
 			@Override
 			public void removeListener(ILabelProviderListener listener) {
 				// not needed.
 			}
 		});
 		
 		// finishing off.
 		editTable.setInput(getEditorSite());
 		
 	}
 	
 	@Override
 	public void setFocus() {
 	}
 	
 }
