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
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.ViewPart;
 import org.mihalis.opal.checkBoxGroup.CheckBoxGroup;
 
 public class SpectrumDatasetView extends ViewPart {
 
 	private CheckboxTableViewer viewer;
 	private ISpectrumFile currentFile;
 	private List<ISpectrumFile> otherFiles;
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		parent.setLayout(new GridLayout(1, true));
 		
 		final CheckBoxGroup group = new CheckBoxGroup(parent, SWT.NONE);
 		group.setLayout(new GridLayout(1, false));
 		group.setText("Use X-Axis");
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		final Composite content = group.getContent();
 		final CCombo combo = new CCombo(content, SWT.READ_ONLY|SWT.BORDER);
 		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		group.setEnabled(false);
 		combo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (currentFile == null) return;
 				String name = combo.getText();
 				if (currentFile.contains(name)) {
 					currentFile.setxDatasetName(name);
 					for (ISpectrumFile file : otherFiles) {
 						if (file.contains(name)) file.setxDatasetName(name);
 					}
 				}
 				updateViewer();
 			}
 		});
 		
 		group.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (currentFile == null) return;
 				if (!group.isActivated()) {
 					currentFile.setUseAxis(false);
 					for (ISpectrumFile file : otherFiles) {
 						file.setUseAxis(false);
 					}
 				} else {
 					currentFile.setUseAxis(true);
 					for (ISpectrumFile file : otherFiles) {
 						file.setUseAxis(true);
 					}
 				}
 				
 				updateViewer();
 			}
 
 		});
 		
 		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		getSite().getPage().addSelectionListener("org.dawnsci.spectrum.ui.views.SpectrumView", new ISelectionListener() {
 
 			@Override
 			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 
 				otherFiles = SpectrumUtils.getSpectrumFilesList((IStructuredSelection)selection);
 
 				if (otherFiles.isEmpty()) {
 					if (viewer == null || viewer.getTable().isDisposed()) return;
 					viewer.setInput(new ArrayList<String>());
 					combo.removeAll();
 					currentFile = null;
 					group.deactivate();
 					group.setEnabled(false);
 					return;
 				} else {
 					currentFile = otherFiles.get(0);
 					otherFiles.remove(0);
 					List<String> names = currentFile.getPossibleAxisNames();
 					combo.setItems(names.toArray(new String[names.size()]));
 					int i = 0;
 					for(String name : currentFile.getPossibleAxisNames()) {
 						if (name.equals(currentFile.getxDatasetName())) {
 							combo.select(i);
 							break;
 						}
 						i++;
 					}
 
 					updateViewer();
 
 					group.setEnabled(true);
 					if (currentFile.isUsingAxis()) {
 						group.activate();
 					} else {
 						group.deactivate();
 					}
 				}
 			}
 		});
 		
 		viewer.setLabelProvider(new LabelProvider());
 		viewer.setContentProvider(new ViewContentProvider());
 		
 		viewer.addCheckStateListener(new ICheckStateListener() {
 			
 			@Override
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				String name = event.getElement().toString();
 				if (event.getChecked()) {
 					if (currentFile.contains(name)) {
 						currentFile.addyDatasetName(name);
 						for (ISpectrumFile file : otherFiles) {
 							if (file.contains(name)) file.addyDatasetName(name);
 						}
 					}
 				} else {
 					if (currentFile.contains(name)) {
 						currentFile.removeyDatasetName(name);
 						for (ISpectrumFile file : otherFiles) {
 							if (file.contains(name)) file.removeyDatasetName(name);
 						}
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 
 	}
 	
 	private void updateViewer() {
 		if (currentFile.getxDataset() != null && currentFile.isUsingAxis()) {
 
 			int[] size = currentFile.getxDataset().getShape();
 			int max = 0;
 
 			for (int j : size)
 				if (j > max)
 					max = j;
 
 			viewer.setInput(currentFile.getMatchingDatasets(max));
 		} else {
 			viewer.setInput(currentFile.getDataNames());
 		}
 		viewer.setCheckedElements(currentFile.getyDatasetNames().toArray());
 		viewer.refresh();
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
