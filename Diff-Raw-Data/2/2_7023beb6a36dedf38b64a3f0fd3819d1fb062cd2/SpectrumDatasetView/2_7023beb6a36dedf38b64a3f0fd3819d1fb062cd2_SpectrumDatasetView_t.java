 package org.dawnsci.spectrum.ui.views;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawnsci.spectrum.ui.file.ISpectrumFile;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.ViewPart;
 
 public class SpectrumDatasetView extends ViewPart {
 
 	private CheckboxTableViewer viewer;
 	private List<ISpectrumFile> currentFiles;
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		parent.setLayout(new GridLayout(1, true));
 		
 		final CCombo combo = new CCombo(parent, SWT.None);
 		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		getSite().getPage().addSelectionListener("org.dawnsci.spectrum.ui.views.SpectrumView", new ISelectionListener() {
 			
 			@Override
 			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 				
 				List<ISpectrumFile> files = SpectrumUtils.getSpectrumFilesList((IStructuredSelection)selection);
 				
 				if (files.isEmpty()) {
					if (viewer == null || viewer.getTable().isDisposed()) return;
 					viewer.setInput(new ArrayList<String>());
 					combo.removeAll();
 					return;
 				}
 				
 				if (files.size() == 1) {
 					currentFiles = new ArrayList<ISpectrumFile>(1);
 					currentFiles.add((ISpectrumFile)((IStructuredSelection)selection).getFirstElement());
 					combo.setItems(currentFiles.get(0).getDataNames().toArray(new String[currentFiles.get(0).getDataNames().size()]));
 					int i = 0;
 					for(String name : currentFiles.get(0).getDataNames()) {
 						if (name.equals(currentFiles.get(0).getxDatasetName())) {
 							combo.select(i);
 						}
 						i++;
 					}
 					
 					viewer.setInput(currentFiles.get(0).getDataNames());
 					viewer.setCheckedElements(currentFiles.get(0).getyDatasetNames().toArray());
 					viewer.refresh();
 				}
 				
 				
 //				if (((IStructuredSelection)selection).getFirstElement() instanceof SpectrumFile && ((IStructuredSelection)selection).size() == 1) {
 //					currentFile = (SpectrumFile)((IStructuredSelection)selection).getFirstElement();
 //					
 //					combo.setItems(currentFile.getDataNames().toArray(new String[currentFile.getDataNames().size()]));
 //					
 //					int i = 0;
 //					for(String name : currentFile.getDataNames()) {
 //						if (name.equals(currentFile.getxDatasetName())) {
 //							combo.select(i);
 //						}
 //						i++;
 //					}
 //					
 //					viewer.setInput(currentFile);
 //					viewer.setCheckedElements(currentFile.getyDatasetNames().toArray());
 //					viewer.refresh();
 //				} else if (((IStructuredSelection)selection).getFirstElement() instanceof SpectrumFile){
 //					
 //					Iterator<?> seIt = ((IStructuredSelection)selection).iterator();
 //					
 //					while (seIt.hasNext()){
 //						
 //						
 //						
 //					}
 //					
 //				}
 				
 				
 			}
 		});
 		
 		viewer.setLabelProvider(new LabelProvider());
 		viewer.setContentProvider(new ViewContentProvider());
 		
 		viewer.addCheckStateListener(new ICheckStateListener() {
 			
 			@Override
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				if (event.getChecked()) {
 					event.getElement().toString();
 					if (currentFiles.get(0).contains(event.getElement().toString())) {
 						currentFiles.get(0).addyDatasetName(event.getElement().toString());
 					}
 				} else {
 					if (currentFiles.get(0).contains(event.getElement().toString())) {
 						currentFiles.get(0).removeyDatasetName(event.getElement().toString());
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 
 	}
 	
 	class ViewContentProvider implements IStructuredContentProvider {
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 		public void dispose() {
 		}
 		public Object[] getElements(Object parent) {
 			
 			if (parent instanceof Collection<?>) {
 
 				return ((Collection<?>)parent).toArray();
 			}
 			
 			return null;
 		}
 	}
 
 }
