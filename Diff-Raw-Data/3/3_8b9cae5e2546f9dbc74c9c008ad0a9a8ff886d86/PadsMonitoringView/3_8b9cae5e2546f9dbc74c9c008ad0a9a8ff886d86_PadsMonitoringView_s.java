 package org.eclipse.iee.editor.monitoring.views;
 
 
 import org.eclipse.iee.editor.IeeEditorPlugin;
 import org.eclipse.iee.editor.core.pad.IPadManagerListener;
import org.eclipse.iee.editor.core.pad.Pad;
 import org.eclipse.iee.editor.core.pad.PadManager;
 import org.eclipse.iee.editor.core.pad.PadManagerEvent;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.part.ViewPart;
 
 
 public class PadsMonitoringView extends ViewPart implements IPadManagerListener {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "org.eclipse.iee.editor.monitoring.views.PadsMonitoringView";
 	
 	private final PadManager fPadManager = IeeEditorPlugin.getDefault().getPadManager();
 	
 	private TableViewer fActivePadsTableViewer;
 	private TableViewer fSuspendedPadsTableViewer;
 	private TableViewer fTemporaryPadsTableViewer;
 	
 
 	class PadsContentProvider implements IStructuredContentProvider {
 		@Override
 		public Object[] getElements(Object parent) {
 			return (Object[]) parent;
 		}
 		@Override public void dispose() {}
 		@Override public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 	}
 	
 	class PadsLabelProvider extends LabelProvider implements ITableLabelProvider {
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
			Pad pad = (Pad) element;
 			switch (columnIndex) {
 			case 0:
 				return pad.getContainerID();
 
 			default:
 				return "unknown " + columnIndex;
 			}
 		}
 	}
 		
 
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout(SWT.VERTICAL));
 		
 		initActivePadsTableView(parent);		
 		initSuspendedPadsTableView(parent);
 		initTemporaryPadsTableView(parent);
 		
 		parent.pack();
 		
 		fPadManager.addPadManagerListener(this);
 	}
 	
 	public void dispose() {
 		fPadManager.removePadManagerListener(this);
 		super.dispose();
 	}
 	
 	@Override
 	public void padManagerUpdate(PadManagerEvent event) {
 		fActivePadsTableViewer.setInput(fPadManager.getActivePads());
 		fSuspendedPadsTableViewer.setInput(fPadManager.getSuspendedPads());
 		fTemporaryPadsTableViewer.setInput(fPadManager.getTemporaryPads());
 	}
 	
 
 	protected void initActivePadsTableView(Composite parent) {
 		fActivePadsTableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 
 		final Table table = fActivePadsTableViewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		String[] columnNames = new String[] { "Active Pads" };
 		int[] columnWidths = new int[] { 100 };
 		int[] columnAlignments = new int[] { SWT.LEFT };
 		
 		for (int i = 0; i < columnNames.length; i++) {
 			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
 			tableColumn.setText(columnNames[i]);
 			tableColumn.setWidth(columnWidths[i]);
 		}
 		
 		fActivePadsTableViewer.setContentProvider(new PadsContentProvider());
 		fActivePadsTableViewer.setLabelProvider(new PadsLabelProvider());
 		
 		fActivePadsTableViewer.setInput(fPadManager.getActivePads());
 	}
 	
 	protected void initSuspendedPadsTableView(Composite parent) {
 		fSuspendedPadsTableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 
 		final Table table = fSuspendedPadsTableViewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		String[] columnNames = new String[] { "Suspended pads" };
 		int[] columnWidths = new int[] { 100 };
 		int[] columnAlignments = new int[] { SWT.LEFT };
 		
 		for (int i = 0; i < columnNames.length; i++) {
 			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
 			tableColumn.setText(columnNames[i]);
 			tableColumn.setWidth(columnWidths[i]);
 		}
 		
 		fSuspendedPadsTableViewer.setContentProvider(new PadsContentProvider());
 		fSuspendedPadsTableViewer.setLabelProvider(new PadsLabelProvider());
 		
 		fSuspendedPadsTableViewer.setInput(fPadManager.getSuspendedPads());
 	}
 	
 	protected void initTemporaryPadsTableView(Composite parent) {
 		fTemporaryPadsTableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 
 		final Table table = fTemporaryPadsTableViewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		String[] columnNames = new String[] { "Temporary pads" };
 		int[] columnWidths = new int[] { 100 };
 		int[] columnAlignments = new int[] { SWT.LEFT };
 		
 		for (int i = 0; i < columnNames.length; i++) {
 			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
 			tableColumn.setText(columnNames[i]);
 			tableColumn.setWidth(columnWidths[i]);
 		}
 		
 		fTemporaryPadsTableViewer.setContentProvider(new PadsContentProvider());
 		fTemporaryPadsTableViewer.setLabelProvider(new PadsLabelProvider());
 		fTemporaryPadsTableViewer.setInput(fPadManager.getTemporaryPads());
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		fActivePadsTableViewer.getControl().setFocus();
 	}
 
 
 
 }
