 package org.padacore.ui.views.extvar;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.part.ViewPart;
 import org.padacore.core.gnat.GnatAdaProject;
 import org.padacore.core.gnat.ScenarioItem;
 import org.padacore.core.project.PropertiesManager;
 import org.padacore.ui.Activator;
 
 public class View extends ViewPart implements Observer {
 
 	private TableViewer viewer;
 
 	@Override
 	public void createPartControl(Composite parent) {
 
 		Activator.getDefault().getProjectSelectionListener().addObserver(this);
 
 		// Create the composite
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		// Add TableColumnLayout
 		TableColumnLayout layout = new TableColumnLayout();
 		composite.setLayout(layout);
 
		this.viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		this.viewer.getTable().setLinesVisible(true);
 		this.viewer.getTable().setHeaderVisible(true);
 
 		this.viewer.setContentProvider(new ContentProvider(
 				org.padacore.core.Activator.getDefault().getScenario()));
 
 		final TableViewerColumn nameViewerColumn = new TableViewerColumn(this.viewer, SWT.LEFT);
 		final TableColumn nameColumn = nameViewerColumn.getColumn();
 		nameColumn.setText("Name");
 		nameColumn.setWidth(200);
 		nameColumn.setResizable(true);
 		nameViewerColumn.setLabelProvider(new NameLabelProvider());
 		layout.setColumnData(nameColumn, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH,
 				true));
 
 		final TableViewerColumn valueViewerColumn = new TableViewerColumn(this.viewer, SWT.LEFT);
 		final TableColumn valueColumn = valueViewerColumn.getColumn();
 		valueColumn.setText("Value");
 		valueColumn.setWidth(200);
 		valueColumn.setResizable(true);
 		valueViewerColumn.setEditingSupport(new ValueEditingSupport(this.viewer,
 				org.padacore.core.Activator.getDefault().getScenario()));
 		valueViewerColumn.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				ScenarioItem item = (ScenarioItem) element;
 				return item.getValue();
 			}
 		});
 
 		layout.setColumnData(valueColumn, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH,
 				true));
 	}
 
 	@Override
 	public void setFocus() {
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		Assert.isLegal(arg instanceof IProject);
 
 		PropertiesManager propertyManager = new PropertiesManager((IProject) arg);
 		GnatAdaProject selectedProject = (GnatAdaProject) propertyManager.getAdaProject();
 
 		this.viewer.setInput(selectedProject);
 	}
 }
