 package com.uwusoft.timesheet.wizard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import com.uwusoft.timesheet.submission.model.SubmissionProject;
 import com.uwusoft.timesheet.submission.model.SubmissionTask;
 
 public class TaskListPage extends WizardPage {
 
 	private TableViewer viewer;
 	private SubmissionProject project;
 
 	protected TaskListPage(String system, SubmissionProject project) {
 		super("Submission system: " + system);
 		setTitle("Project: " + project.getName());
 		setDescription("System: " + system);
 		this.project = project;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
		// A WizardPage must create a new Composite that must use the Composite of the method parameter as parent
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		viewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
 		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
 		final TableColumn column = viewerColumn.getColumn();
 		column.setWidth(300);
 		column.setResizable(true);
 		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
             public String getText(Object element) {
 		        return ((SubmissionTask) element).getName();
 			}
 			public Image getImage(Object obj) {
 				return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
 			}
 		});
         viewer.setContentProvider(ArrayContentProvider.getInstance());
         viewer.setInput(project.getTasks());
 		// Required to avoid an error in the system
		setControl(container);
 		setPageComplete(true);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void addTasksToProjects(List<SubmissionProject> projects) {
 		project.setTasks(new ArrayList<SubmissionTask>(((StructuredSelection) viewer.getSelection()).toList()));
 		projects.add(project);
 	}
 }
