 package org.eclipse.dltk.core.tools.internal.ui;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.dltk.core.RuntimePerformanceMonitor;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.DataEntry;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.ui.part.ViewPart;
 
 public class DLTKPerformanceViewPart extends ViewPart {
 
 	private static final int UPDATE_INTERVAL = 500;
 
 	public class PerformanceLabelProvide extends LabelProvider implements
 			ITableLabelProvider {
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			switch (columnIndex) {
 			case 0:
 				if (element instanceof Map.Entry) {
 					return (String) ((Map.Entry) element).getKey();
 				}
 				break;
 			case 1:
 				if (element instanceof Map.Entry) {
 					Map.Entry entry = (Entry) element;
 					if (entry.getValue() instanceof DataEntry) {
 						DataEntry e = (DataEntry) entry.getValue();
 						return Long.toString(e.getTime());
 					}
 				}
 				break;
 			case 2:
 				if (element instanceof Map.Entry) {
 					Map.Entry entry = (Entry) element;
 					if (entry.getValue() instanceof DataEntry) {
 						DataEntry e = (DataEntry) entry.getValue();
 						return Long.toString(e.getTotal());
 					}
 				}
 				break;
 			case 3:
 				if (element instanceof Map.Entry) {
 					Map.Entry entry = (Entry) element;
 					if (entry.getValue() instanceof DataEntry) {
 						DataEntry e = (DataEntry) entry.getValue();
 						return Long.toString(e.getCount());
 					}
 				}
 				break;
 			case 4:
 				if (element instanceof Map.Entry) {
 					Map.Entry entry = (Entry) element;
 					if (entry.getValue() instanceof DataEntry) {
 						DataEntry e = (DataEntry) entry.getValue();
 						if (e.getCount() != 0) {
 							return Double.toString(e.getTime() / e.getCount());
 						}
 					}
 				}
 				break;
 			case 5:
 				if (element instanceof Map.Entry) {
 					Map.Entry entry = (Entry) element;
 					if (entry.getValue() instanceof DataEntry) {
 						DataEntry e = (DataEntry) entry.getValue();
 						if (e.getCount() != 0) {
 							return Double.toString(e.getTotal() / e.getCount());
 						}
 					}
 				}
 				break;
 			case 6:
 				if (element instanceof Map.Entry) {
 					Map.Entry entry = (Entry) element;
 					if (entry.getValue() instanceof DataEntry) {
 						DataEntry e = (DataEntry) entry.getValue();
 						if (e.getTime() != 0) {
 							return Double.toString(e.getTotal() / e.getTime());
 						}
 					}
 				}
 				break;
 			}
 			return "";
 		}
 	}
 
 	private static final class PerformanceContentProvider implements
 			ITreeContentProvider {
 		private static final Object[] NONE_OBJECTS = new Object[0];
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		public void dispose() {
 		}
 
 		public Object[] getElements(Object inputElement) {
 			Map<String, Map<String, DataEntry>> map = RuntimePerformanceMonitor
 					.getAllEntries();
 			return map.entrySet().toArray();
 		}
 
 		public boolean hasChildren(Object element) {
 			if (element instanceof Map) {
 				return !((Map) element).isEmpty();
 			} else if (element instanceof Map.Entry) {
 				Map.Entry e = (Entry) element;
 				Object value = e.getValue();
 				if (value instanceof Map) {
 					Map mval = (Map) value;
 					return !mval.isEmpty();
 				}
 			}
 			return false;
 		}
 
 		public Object getParent(Object element) {
 			return null;
 		}
 
 		public Object[] getChildren(Object parentElement) {
 			if (parentElement instanceof Map) {
 				return ((Map) parentElement).entrySet().toArray();
 			} else if (parentElement instanceof Map.Entry) {
 				Map.Entry e = (Entry) parentElement;
 				if (e.getValue() instanceof Map) {
 					return ((Map) e.getValue()).entrySet().toArray();
 				}
 			}
 			return NONE_OBJECTS;
 		}
 	}
 
 	private TreeViewer viewer;
 
 	public DLTKPerformanceViewPart() {
 	}
 
 	Runnable update = new Runnable() {
 		public void run() {
 			if (!viewer.getTree().isDisposed()) {
 				viewer.refresh();
 				viewer.getTree().getDisplay()
 						.timerExec(UPDATE_INTERVAL, update);
 				viewer.expandAll();
 			}
 		}
 	};
 
 	@Override
 	public void createPartControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout());
 		viewer = new TreeViewer(composite);
 		viewer.getTree().setHeaderVisible(true);
 		viewer.getTree().setLayoutData(
 				new GridData(SWT.FILL, SWT.FILL, true, true));
 		TreeColumn col = new TreeColumn(viewer.getTree(), SWT.NONE);
 		col.setText("ID");
 		col.setWidth(250);
 
 		TreeColumn time = new TreeColumn(viewer.getTree(), SWT.NONE);
 		time.setText("Time");
 		time.setWidth(80);
 
 		TreeColumn total = new TreeColumn(viewer.getTree(), SWT.NONE);
 		total.setText("Total");
 		total.setWidth(80);
 
 		TreeColumn count = new TreeColumn(viewer.getTree(), SWT.NONE);
 		count.setText("Count");
 		count.setWidth(80);
 
 		TreeColumn avrTime = new TreeColumn(viewer.getTree(), SWT.NONE);
 		avrTime.setText("Avr.Time");
 		avrTime.setWidth(80);
 
 		TreeColumn average = new TreeColumn(viewer.getTree(), SWT.NONE);
 		average.setText("Avr.Count");
 		average.setWidth(80);
 
 		TreeColumn speed = new TreeColumn(viewer.getTree(), SWT.NONE);
 		speed.setText("Speed");
 		speed.setWidth(80);
 
 		viewer.setContentProvider(new PerformanceContentProvider());
 		viewer.setLabelProvider(new PerformanceLabelProvide());
 		viewer.setInput(new Object());
 		parent.getDisplay().timerExec(500, update);
 		Button clear = new Button(composite, SWT.PUSH);
 		clear.setText("Clear");
 		clear.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				RuntimePerformanceMonitor.clear();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 	}
 
 	@Override
 	public void setFocus() {
 	}
 
 }
