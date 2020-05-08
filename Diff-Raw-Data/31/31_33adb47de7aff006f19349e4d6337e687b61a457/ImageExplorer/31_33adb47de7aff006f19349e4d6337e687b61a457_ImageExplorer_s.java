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
 
 package uk.ac.diamond.scisoft.analysis.rcp.explorers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.lang.ArrayUtils;
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
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PlatformUI;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisChoice;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.gda.monitor.IMonitor;
 
 public class ImageExplorer extends AbstractExplorer implements ISelectionProvider {
 
 	private TableViewer viewer;
 	private DataHolder data = null;
 	private ISelectionChangedListener listener;
 	private Display display = null;
 	private SelectionAdapter contextListener = null;
 
 	public ImageExplorer(Composite parent, IWorkbenchPartSite partSite, ISelectionChangedListener valueSelect) {
 		super(parent, partSite, valueSelect);
 
 		display = parent.getDisplay();
 		setLayout(new FillLayout());
 
 		viewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.getTable().setHeaderVisible(true);
 
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
 		// viewer.setInput(getEditorSite());
 
 		listener = new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent arg0) {
 				selectItemSelection();
 			}
 		};
 
 		viewer.addSelectionChangedListener(listener);
 
 		if (metaValueListener != null) {
 			final ImageExplorer provider = this;
 			contextListener = new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					int i = viewer.getTable().getMenu().indexOf((MenuItem) e.widget);
 					SelectionChangedEvent ce = new SelectionChangedEvent(provider, new MetadataSelection(metaNames.get(i)));
 					metaValueListener.selectionChanged(ce);
 				}
 			};
 		}
 	}
 
 	public TableViewer getViewer() {
 		return viewer;
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
 			return data.getList().toArray();
 		}
 	}
 
 	private class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
 		@Override
 		public String getColumnText(Object obj, int index) {
 			if (obj instanceof IDataset) {
 				IDataset dataset = (IDataset) obj;
 				if (index == 0)
 					return dataset.getName();
 				if (index == 1)
 					return dataset.min().toString();
 				if (index == 2)
 					return dataset.max().toString();
 				if (index == 3) {
 					String[] parts = dataset.elementClass().toString().split("\\.");
 					return parts[parts.length - 1];
 				}
 			}
 			if (obj instanceof ILazyDataset) {
 				ILazyDataset dataset = (ILazyDataset) obj;
 				if (index == 0)
 					return dataset.getName();
 				if (index == 1)
 					return "Not Available";
 				if (index == 2)
 					return "Not Available";
 				if (index == 3)
 					return "Not Available";
 			}
 
 			return null;
 		}
 
 		@Override
 		public Image getColumnImage(Object obj, int index) {
 			if (index == 0)
 				return getImage(obj);
 			return null;
 		}
 
 		@Override
 		public Image getImage(Object obj) {
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		}
 	}
 
 	private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();
 	private DatasetSelection dSelection = null;
 	private ArrayList<String> metaNames;
 	private String fileName;
 
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
 		if (selection instanceof DatasetSelection)
 			dSelection = (DatasetSelection) selection;
 		else return;
 
 		SelectionChangedEvent e = new SelectionChangedEvent(this, dSelection);
 		for (ISelectionChangedListener listener : listeners)
 			listener.selectionChanged(e);
 	}
 
 	@Override
 	public void dispose() {
 		viewer.removeSelectionChangedListener(listener);
 		data = null;
 		MenuItem[] items = viewer.getTable().getMenu().getItems();
 		for (MenuItem i : items) {
 			i.removeSelectionListener(contextListener);
 		}
 	}
 
 	@Override
 	public DataHolder loadFile(String fileName, IMonitor mon) throws Exception {
 		if (fileName == this.fileName)
 			return data;
 
 		return LoaderFactory.getData(fileName, mon);
 	}
 
 	@Override
 	public void loadFileAndDisplay(String fileName, IMonitor mon) throws Exception {
 		this.fileName = fileName;
 
 		data = LoaderFactory.getData(fileName, mon);
 		if (data != null) {
 			if (display != null) {
 				final IMetaData meta = data.getMetadata();
 
 				display.asyncExec(new Runnable() {
 					
 					@Override
 					public void run() {
 						viewer.setInput(data);
 						display.update();
 						if (metaValueListener != null) {
 							addMenu(meta);
 						}
 					}
 				});
 			}
 
 			selectItemSelection();
 		}
 	}
 
 	private ILazyDataset getActiveData() {
 		ISelection selection = viewer.getSelection();
 		Object obj = ((IStructuredSelection) selection).getFirstElement();
 		ILazyDataset d;
 		if (obj == null) {
 			d = data.getLazyDataset(0);
 		} else if (obj instanceof ILazyDataset) {
 			d = (ILazyDataset) obj;
 		} else
 			return null;
 	
 		if (d.getRank() > 2)
 			return null;
 	
 		if (d.getRank() == 1) {
 			d.setShape(1, d.getShape()[0]);
 		}
 		return d;
 	}
 
 	private List<AxisSelection> getAxes(ILazyDataset d) {
 		List<AxisSelection> axes = new ArrayList<AxisSelection>();
 	
 		int[] shape = d.getShape();
 	
 		for (int j = 0; j < shape.length; j++) {
 			final int len = shape[j];
 			AxisSelection axisSelection = new AxisSelection(len, j);
 			axes.add(axisSelection);
 	
 			AbstractDataset autoAxis = AbstractDataset.arange(len, AbstractDataset.INT32);
 			autoAxis.setName(AbstractExplorer.DIM_PREFIX + (j+1));
 			AxisChoice newChoice = new AxisChoice(autoAxis);
 			newChoice.setAxisNumber(j);
 			axisSelection.addChoice(newChoice, 0);
 	
 			for (int i = 0, imax = data.size(); i < imax; i++) {
 				ILazyDataset ldataset = data.getLazyDataset(i);
 				if (ldataset.equals(d))
 					continue;
 				if (ArrayUtils.contains(ldataset.getShape(), len)) {
 					newChoice = new AxisChoice(ldataset);
 					newChoice.setAxisNumber(j);
 					axisSelection.addChoice(newChoice, i + 1);
 				}
 			}
 		}
 	
 		return axes;
 	}
 
 	public void selectItemSelection() {
 		ILazyDataset d = getActiveData();
 		if (d == null)
 			return;
 
 		d = d.clone();
 		d.setName(new File(fileName).getName() + AbstractFileLoader.FILEPATH_DATASET_SEPARATOR + d.getName());
 		DatasetSelection datasetSelection = new DatasetSelection(InspectorType.IMAGE, getAxes(d), d);
 		setSelection(datasetSelection);
 	}
 
 	private void addMenu(IMetaData meta) {
 		// create context menu and handling
 		if (meta != null) {
 			Collection<String> names = null;
 			try {
 				names = meta.getMetaNames();
 			} catch (Exception e1) {
 				return;
 			}
 			if (names == null) {
 				return;
 			}
 			try {
 				Menu context = new Menu(viewer.getControl());
 				metaNames = new ArrayList<String>();
 				for (String n : names) {
 					try {
 						String v = meta.getMetaValue(n).toString();
 						Double.parseDouble(v);
 						metaNames.add(n);
 						MenuItem item = new MenuItem(context, SWT.PUSH);
 						item.addSelectionListener(contextListener);
 						item.setText(n + " = " + v);
 					} catch (NumberFormatException e) {
 						
 					}
 				}
 
 				viewer.getTable().setMenu(context);
 			} catch (Exception e) {
 			}
 		}
 	}
 }
