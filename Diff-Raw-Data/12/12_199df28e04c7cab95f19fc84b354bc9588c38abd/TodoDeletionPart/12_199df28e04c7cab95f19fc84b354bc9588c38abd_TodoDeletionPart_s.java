 package com.example.e4.rcp.todo.parts;
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.core.services.events.IEventBroker;
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.e4.ui.di.UIEventTopic;
 import org.eclipse.e4.ui.services.IServiceConstants;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 
 import com.example.e4.rcp.todo.event.EventConstants;
 import com.example.e4.rcp.todo.facade.ModelFacade;
 import com.example.e4.rcp.todo.model.Todo;
 
 public class TodoDeletionPart {
 
 	@Inject
 	private ModelFacade model;
 	@Inject
 	private IEventBroker broker;
 	@Inject
 	private ESelectionService selectionService;
 	private ComboViewer viewer;
 
 	@PostConstruct
 	public void createControls(Composite parent) {
 		parent.setLayout(new GridLayout(2, false));
 		viewer = new ComboViewer(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
 		Combo combo = viewer.getCombo();
 		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
 				1));
 		viewer.setLabelProvider(new LabelProvider() {
 
 			@Override
 			public String getText(Object element) {
 				Todo todo = (Todo) element;
 				return todo.getSummary();
 			}
 
 		});
 		viewer.setContentProvider(ArrayContentProvider.getInstance());
 
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) viewer
 						.getSelection();
 				selectionService.setSelection(selection.getFirstElement());
 			}
 		});
 
 		Button button = new Button(parent, SWT.PUSH);
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				IStructuredSelection selection = (IStructuredSelection) viewer
 						.getSelection();
 				if (selection.size() > 0) {
 					Todo todo = (Todo) selection.getFirstElement();
 					model.deleteTodo(todo.getId());
 				}
 			}
 		});
 		button.setText("Delete");
 	}
 
 	@Inject
 	public void setTodo(
 			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Todo todo) {
 		if (viewer != null) {
 			ISelection selection;
 			if (todo != null) {
 				selection = new StructuredSelection(todo);
 			} else {
 				selection = StructuredSelection.EMPTY;
 			}
 			viewer.setSelection(selection);
 		}
 	}
 
 	@Inject
 	@Optional
 	public void onDataLoaded(
 			@UIEventTopic(EventConstants.TOPIC_TODO_DATA_LOADED) List<Todo> todos) {
 		updateViewer(todos);
 	}
 
 	@Inject
 	@Optional
 	public void onTodoDeleted(
 			@UIEventTopic(EventConstants.TOPIC_TODO_DATA_UPDATE_DELETE) Todo todo) {
 		viewer.remove(todo);
 	}
 
 	@Inject
 	@Optional
 	public void onTodoAdded(
 			@UIEventTopic(EventConstants.TOPIC_TODO_DATA_UPDATE_NEW) Todo todo) {
 		viewer.add(todo);
 		setTodo(todo);
 	}
 
 	@Inject
 	@Optional
 	public void onTodoUpdated(
 			@UIEventTopic(EventConstants.TOPIC_TODO_DATA_UPDATE_UPDATED) Todo todo) {
 		viewer.refresh(todo);
 	}
 
 	@Focus
 	public void onFocus() {
 		viewer.getCombo().setFocus();
 	}
 
 	private void updateViewer(List<Todo> todos) {
 		viewer.setInput(todos);
 		if (todos.size() > 0) {
 			viewer.setSelection(new StructuredSelection(todos.get(0)));
 		}
 	}
 
 }
