 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.editors;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.SRSExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.navigator.srs.SRSTreeData;
 import uk.ac.gda.common.rcp.util.EclipseUtils;
 
 public class SRSEditor extends EditorPart {
 
 	private SRSExplorer srsxp;
 	private String fileName;
 
 	public SRSEditor() {
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 
 	}
 
 	@Override
 	public void doSaveAs() {
 
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
         setSite(site);
 		fileName = EclipseUtils.getFilePath(input);
 		if (fileName == null) {
 			throw new PartInitException("Input is not a file or file does not exist");
 		}
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
 		IWorkbenchPartSite site = getSite();
 		srsxp = new SRSExplorer(parent, site, null);
 		site.setSelectionProvider(srsxp);
 
 		try {
 			srsxp.loadFileAndDisplay(fileName, null);
 		} catch (Exception e) {
 		}
 
 		// Register selection listener
 		registerSelectionListener();
 	}
 
 	@Override
 	public void setFocus() {
 	}
 
 	private ISelectionListener selectionListener;
 	
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
 						if (element instanceof SRSTreeData) {
 							SRSTreeData srsData = (SRSTreeData) element;
 							String filename = srsData.getFile().getLocation().toString();
							String[] temp = filename.split("/");
							filename = temp[temp.length-1];
 							//update only the relevant srseditor
 							String editorName= getSite().getPart().getTitle();
 							if(filename.equals(editorName))
 								update(part, srsData);			
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
 
 	@Override
 	public void dispose() {
 
 		// Unregister selection listener
 		unregisterSelectionListener();
 
 		super.dispose();
 	}
 
 	/**
 	 * This editor uses an SRS explorer
 	 * @return explorer class
 	 */
 	public static Class<? extends AbstractExplorer> getExplorerClass() {
 		return SRSExplorer.class;
 	}
 
 	public void update(final IWorkbenchPart original, final SRSTreeData srsData) {
 
 		/**
 		 * TODO Instead of selecting the editor, firing the selection and then selecting the naigator again, better to
 		 * have one object type selected by both the editor and navigator which the plot view listens to using eclipse
 		 * selection events.
 		 */
 
 		EclipseUtils.getActivePage().activate(this);
 
 		TableViewer viewer = srsxp.getViewer();
 		for (int i = 0; i < viewer.getTable().getItemCount(); i++) {
 			String name = viewer.getTable().getItem(i).getText();
 			if (name.equals(srsData.getName())) {
 				viewer.getTable().setSelection(i);
 				//selectItemAction.run();
 				srsxp.selectItemSelection();
 				break;
 			}
 		}
 
 		EclipseUtils.getActivePage().activate(original);
 
 	}
 
 }
