 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.editors;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.progress.IProgressService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.diamond.scisoft.analysis.utils.ImageThumbnailLoader;
 import uk.ac.gda.common.rcp.util.EclipseUtils;
 import uk.ac.gda.monitor.ProgressMonitorWrapper;
 
 /**
  *
  */
public class PlotEditor extends EditorPart implements ISelectionProvider, IReusableEditor {
 
 	private static final Logger logger = LoggerFactory.getLogger(PlotEditor.class);
 	private TableViewer viewer;
 	private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();
 	private AbstractDataset image;
 	private Action doubleClickAction;
 	private DatasetSelection dSelection = null;
 	
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// Nothing to do
 	}
 
 	@Override
 	public void doSaveAs() {
 		// Nothing to do
 
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
		getSite().setSelectionProvider(this);
 		setInput(input);
 		setPartName(input.getName());
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.getTable().setHeaderVisible (true);
 		
 		TableColumn tc = new TableColumn(viewer.getTable(), SWT.LEFT);
 		tc.setText("Name");
 		tc.setWidth(200);
 		tc = new TableColumn(viewer.getTable(), SWT.LEFT);
 		tc.setText("min");
 		tc.setWidth(100);
 		tc = new TableColumn(viewer.getTable(), SWT.LEFT);
 		tc.setText("max");
 		tc.setWidth(100);
 		tc = new TableColumn(viewer.getTable(), SWT.LEFT);
 		tc.setText("Class");
 		tc.setWidth(100);
 		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setInput(getEditorSite());
 		makeActions();
 		hookDoubleClickAction();
 	}
 
 	private void makeActions() {
 
 		doubleClickAction = new Action() {
 			@Override
 			public void run() {
 				DatasetSelection datasetSelection = new DatasetSelection(InspectorType.IMAGE, null, getActiveData());
 				setSelection(datasetSelection);
 			}
 		};
 	}
 
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				doubleClickAction.run();
 			}
 		});
 	}
 	
 	private ILazyDataset getActiveData() {
 		ISelection selection = viewer.getSelection();
 		Object obj = ((IStructuredSelection)selection).getFirstElement();
 		return (ILazyDataset) obj;		
 	}
 	
 	
 	@Override
 	public void setInput(final IEditorInput input) {
 		
 		super.setInput(input);
 		
 		if (!(input instanceof IURIEditorInput)) {
 			logger.warn("Editor input cannot be used here");
 			return;
 		}
 		setPartName(input.getName());
 		
 		try {
 			loadFile(input);
 		} catch (Exception e) {
 			logger.error("Cannot load "+input.getName(), e);
 		}
		
		DatasetSelection datasetSelection = new DatasetSelection(InspectorType.IMAGE, null, image);
		setSelection(datasetSelection);

 	}
 
 	/**
 	 * Uses separate job to parse and open file so that large files work
 	 * @param input
 	 * @throws InterruptedException 
 	 * @throws InvocationTargetException 
 	 */
 	private void loadFile(final IEditorInput input) throws InvocationTargetException, InterruptedException {
 		
 		final IProgressService service = (IProgressService)getSite().getService(IProgressService.class);
 		service.run(true, true, new IRunnableWithProgress() {
 			
 			@Override
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				monitor.beginTask("Opening " + input.getName(), 100);
 				monitor.worked(1);
 
 				File f = EclipseUtils.getFile(input);
 				if (f == null || !f.exists()) {
 					logger.error("Could not load {}", input.getName());
 					monitor.done();
 					return;
 				}
 
 				final String fileName = f.getAbsolutePath();
 				// Had to use LoaderFactory to avoid out of memory errors.
 				try {
 					// This is the bit that can take a while.
 					final DataHolder dh = LoaderFactory.getData(fileName, new ProgressMonitorWrapper(monitor));
 
 					// Rest should be fast.
 					image = ImageThumbnailLoader.getSingle(fileName, false, dh);
 					if (image != null && image.getRank() != 2) {
 						image = null;
 						logger.warn("File is not an image, plot editor only supports images at the moment");
 					}
 				} catch (Exception ne) {
 					logger.error("Cannot load file " + fileName, ne);
 				}
 				monitor.done();
 			}
 		});
 		
 	}
 
 	@Override
 	public void dispose()
 	{
 		super.dispose();
 	}
 
 	@Override
 	public void setFocus() {
 		// Nothing to do
 		
 	}
 
 	@Override
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 	@Override
 	public ISelection getSelection() {
 		return dSelection;
 	}
 
 	@Override
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.remove(listener);
 		
 	}
 
 	@Override
 	public void setSelection(ISelection selection) {
 		logger.debug("Starting selection Update");
 		if (selection instanceof DatasetSelection)
 			dSelection = (DatasetSelection) selection;
 
 		SelectionChangedEvent e = new SelectionChangedEvent(this, dSelection);
 		for (ISelectionChangedListener listener : listeners) {
 			logger.debug("Updateing ", listener.toString());
 			listener.selectionChanged(e);	
 		}
 		logger.debug("Finished selection Update");
 	}
 
 	private class ViewContentProvider implements IStructuredContentProvider {
 		@Override
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 		
 		@Override
 		public void dispose() {
 		}
 		
 		@Override
 		public Object[] getElements(Object parent) {
 			if (image != null) {
 				return new Object[]{image};
 			} 
 			return new Object[]{};
 		}
 		
 	}
 	
 	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
 		@Override
 		public String getColumnText(Object obj, int index) {
 			IDataset dataset = (IDataset) obj;
 			if (index == 0) return dataset.getName();
 			if (index == 1) return dataset.min().toString();
 			if (index == 2) return dataset.max().toString();
 			if (index == 3) {
 				String[] parts = dataset.elementClass().toString().split("\\.");
 				return parts[parts.length-1];
 			}
 			return null;
 		}
 		
 		@Override
 		public Image getColumnImage(Object obj, int index) {
 			if (index == 0 ) return getImage(obj);
 			return null;
 		}
 		
 		@Override
 		public Image getImage(Object obj) {
 			return PlatformUI.getWorkbench().
 			getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		}
 	}	
 	
 }
