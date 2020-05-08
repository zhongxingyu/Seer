 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.IPageChangedListener;
 import org.eclipse.jface.dialogs.PageChangedEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 import uk.ac.diamond.scisoft.analysis.rcp.hdf5.HDF5TreeExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.diamond.scisoft.analysis.rcp.navigator.treemodel.TreeNode;
 import uk.ac.gda.common.rcp.util.EclipseUtils;
 
 public class HDF5TreeEditor extends EditorPart implements IPageChangedListener {
 
 	private HDF5TreeExplorer hdfxp;
 	private HDF5File hdf5Tree;
 	private String fileName;
 
 	private ISelectionListener selectionListener;
 
 	public HDF5TreeEditor() {
 	}
 
 	/**
 	 * 
 	 */
 	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.editors.NexusTreeEditor"; //$NON-NLS-1$
 
 	private static final Logger logger = LoggerFactory.getLogger(HDF5TreeEditor.class);
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);
 		setPartName(input.getName());
 	}
 
 	protected void createHDF5Tree() {
 		if (hdf5Tree != null)
 			return;
 		try {
 			hdf5Tree = new HDF5Loader(fileName).loadTree(null);
 			if (hdf5Tree != null && hdfxp != null) {
 				hdfxp.setFilename(fileName);
 				hdfxp.setHDF5Tree(hdf5Tree);
 			}
 		} catch (Exception e) {
 			logger.warn("Could not load NeXus file {}", fileName);
 		}
 		return;
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
 		registerSelectionListener();
 		hdfxp = new HDF5TreeExplorer(parent, SWT.NONE, getSite());
 
 		createHDF5Tree();
 	}
 
 	@Override
 	public void pageChanged(PageChangedEvent event) {
 		if (event.getSelectedPage() == this) { // Just selected this page
 			createHDF5Tree();
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// do nothing
 	}
 
 	@Override
 	public void doSaveAs() {
 		// do nothing
 	}
 
 	@Override
 	public void setFocus() {
 		hdfxp.setFocus();
 	}
 
 	@Override
 	public void setInput(final IEditorInput input) {
 
 		super.setInput(input);
 
 		File f = EclipseUtils.getFile(input);
 		if (!f.exists()) {
 			logger.warn("File does not exist: {}", input.getName());
 			return;
 		}
 		this.fileName = f.getAbsolutePath();
 		setPartName(f.getName());
 		hdf5Tree = null;
 
 	}
 
 	public void expandAll() {
 		hdfxp.expandAll();
 	}
 
 	@Override
 	public void dispose() {
 		unregisterSelectionListener();
 		if (hdfxp != null)
 			hdfxp.dispose();
 		super.dispose();
 	}
 
 	public HDF5TreeExplorer getHDF5TreeExplorer() {
 		return hdfxp;
 	}
 	
 	public HDF5File getHDF5Tree() {
 		return hdf5Tree;
 	}
 
 	/* Setting up of the SRSEditor as a Selection listener on the navigator selectionProvider */
 	private void registerSelectionListener() {
 
 		final ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
 		if (selectionService == null)
 			throw new IllegalStateException("Cannot acquire the selection service!");
 		selectionListener = new ISelectionListener() {
 
 			@Override
 			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
 				if (selection instanceof IStructuredSelection) {
 					if (!selection.isEmpty()) {
 						final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 						final Object element = structuredSelection.getFirstElement();
 						if (element instanceof TreeNode) {
 							TreeNode hdf5Data = (TreeNode) element;
 							String filename = hdf5Data.getFile().getName();
 							//update only the relevant hdf5editor
 							if(filename.equals(getSite().getPart().getTitle()))
 								update(part, hdf5Data, structuredSelection);
 						}
 					}
 
 				}
 			}
 		};
 		selectionService.addSelectionListener(selectionListener);
 	}
 
 	private void unregisterSelectionListener() {
 
 		final ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
 		if (selectionService == null)
 			throw new IllegalStateException("Cannot acquire the selection service!");
 
 		selectionService.removeSelectionListener(selectionListener);
 	}
 
 	public void update(final IWorkbenchPart original, final TreeNode treeData, IStructuredSelection structuredSelection) {
 
 		/**
 		 * TODO Instead of selecting the editor, firing the selection and then selecting the navigator again, better to
 		 * have one object type selected by both the editor and navigator which the plot view listens to using eclipse
 		 * selection events.
 		 */
 		if(!EclipseUtils.getActivePage().getActivePart().getTitle().equals(this.getPartName()))
 			EclipseUtils.getActivePage().activate(this);
 
 		//System.out.println(treeData.getData().toString());
 		//selection of hedf5 tree element no working
 		
 		
 		final Cursor cursor = hdfxp.getCursor();
 		Cursor tempCursor = hdfxp.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
 		if (tempCursor != null)
 			hdfxp.setCursor(tempCursor);
 
 		try {
 			
 			
 			
 			TreePath navTreePath1 = new TreePath(structuredSelection.toArray());
 			//System.out.println(navTreePath1.getFirstSegment().toString());			
 			hdfxp.expandToLevel(navTreePath1,2);
 			hdfxp.setSelection(structuredSelection);
 			//TreePath[] editorTreePaths=hdfxp.getExpandedTreePaths();
 
 			//hdfxp.setSelection(structuredSelection);
 			//HDF5TableTree tableTree=hdfxp.getTableTree();
 			//tableTree.setSelection(structuredSelection);
 			//HDF5TableTree tableTree=hdfxp.getTableTree();
 			//tableTree.setInput(link);
 			
 		//	hdfxp.expandToLevel(navTreePath1,0);
 //			for (int i = 0; i < hdfxp.getTabList().; i++) {
 //				String name = viewer.getTable().getItem(i).getText();
 //				if (name.equals(srsData.getName())) {
 //					viewer.getTable().setSelection(i);
 //					selectItemAction.run();
 //					break;
 //				}
 //			}
 			
 			//hdfxp.getTableTree().getViewer().setSelection(structuredSelection);
 			
 			HDF5NodeLink link = ((HDF5NodeLink) treeData.getData());
 			hdfxp.selectHDF5Node(link, InspectorType.LINE);
 		} catch (Exception e) {
 			logger.error("Error processing selection: {}", e.getMessage());
 		} finally {
 			if (tempCursor != null)
 				hdfxp.setCursor(cursor);
 		}
 
 		//if(!original.getTitle().equals(this.getPartName()))
		EclipseUtils.getActivePage().activate(original);
 
 	}
 }
