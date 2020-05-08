 package de.cjanz.vaadin.lqc_demo.frontend.view;
 
 import static de.cjanz.vaadin.lqc_demo.backend.domain.QTaskEntity.*;
 
 import javax.inject.Inject;
 
 import com.vaadin.cdi.CDIView;
 import com.vaadin.data.fieldgroup.FieldGroup;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.event.ShortcutListener;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 import de.cjanz.vaadin.lqc_demo.frontend.presenter.TaskListPresenter;
 
 @CDIView
 public class TaskListView extends CustomComponent implements View {
 
 	private TaskListPresenter presenter;
 	private Table table;
 
 	@Inject
 	public TaskListView(final TaskListPresenter presenter) {
 		this.presenter = presenter;
 
 		setSizeFull();
 
 		VerticalLayout layout = new VerticalLayout();
 		layout.setSizeFull();
 		setCompositionRoot(layout);
 
 		createFilter(presenter, layout);
 		createTable(presenter, layout);
 	}
 
 	private void createFilter(final TaskListPresenter presenter,
 			VerticalLayout layout) {
 		HorizontalLayout filterLayout = new HorizontalLayout();
 		filterLayout.setSpacing(true);
 		layout.addComponent(filterLayout);
 
 		FieldGroup fieldGroup = new FieldGroup();
 		fieldGroup.setItemDataSource(presenter.getFilterItem());
 		fieldGroup.setBuffered(false);
 
 		TextField titleSearch = new TextField();
 		titleSearch.setWidth("250px");
 		titleSearch.setInputPrompt("Title");
 		titleSearch.setNullRepresentation("");
 		titleSearch.setNullSettingAllowed(true);
 		filterLayout.addComponent(titleSearch);
 		fieldGroup.bind(titleSearch, "title");
 
 		ComboBox priorityFilter = new ComboBox();
 		priorityFilter.setInputPrompt("All Priorities");
 		priorityFilter.setNullSelectionAllowed(true);
 		priorityFilter.setContainerDataSource(presenter.getPriorityContainer());
 		filterLayout.addComponent(priorityFilter);
 		fieldGroup.bind(priorityFilter, "priority");
 
 		Button searchButton = new Button("Search");
 		searchButton.setClickShortcut(KeyCode.ENTER);
 		filterLayout.addComponent(searchButton);
 		searchButton.addClickListener(new Button.ClickListener() {
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				presenter.startSearch();
 			}
 		});
 	}
 
 	private void createTable(final TaskListPresenter presenter,
 			VerticalLayout layout) {
 		table = new Table();
 		table.setSelectable(true);
 		table.setImmediate(true);
 		table.setNullSelectionAllowed(false);
 		table.setSizeFull();
		table.addShortcutListener(new ShortcutListener("Open", KeyCode.ENTER,
 				null) {
 
 			@Override
 			public void handleAction(Object sender, Object target) {
 				presenter.openTask(table.getValue());
 			}
		});
		layout.addComponent(table);
		layout.setExpandRatio(table, 1f);
 
 		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
 
 			@Override
 			public void itemClick(ItemClickEvent event) {
 				if (event.isDoubleClick()) {
 					presenter.openTask(event.getItemId());
 				}
 			}
 		});
 	}
 
 	@Override
 	public void enter(ViewChangeEvent event) {
 
 		if (table.getVisibleColumns().length == 0) {
 			table.setContainerDataSource(presenter.getTasksContainer());
 			addContentCellGenerator();
 			addDoneCellGenerator();
 			table.setVisibleColumns(new Object[] { taskEntity.id,
 					taskEntity.title, taskEntity.priority, taskEntity.done,
 					taskEntity.content });
 			table.setSortContainerPropertyId(taskEntity.title);
 		} else {
 			presenter.startSearch();
 		}
 
 		table.focus();
 	}
 
 	private void addDoneCellGenerator() {
 		table.addGeneratedColumn(taskEntity.done, new Table.ColumnGenerator() {
 
 			@Override
 			public Object generateCell(Table source, Object itemId,
 					Object columnId) {
 				CheckBox checkBox = new CheckBox();
 				checkBox.setWidth("19px");
 				checkBox.setEnabled(false);
 				checkBox.setValue((Boolean) source.getItem(itemId)
 						.getItemProperty(columnId).getValue());
 				return checkBox;
 			}
 		});
 	}
 
 	private void addContentCellGenerator() {
 		table.addGeneratedColumn(taskEntity.content,
 				new Table.ColumnGenerator() {
 
 					@Override
 					public Object generateCell(Table source, Object itemId,
 							Object columnId) {
 						Label contentLabel = new Label();
 						String text = (String) source.getItem(itemId)
 								.getItemProperty(columnId).getValue();
 						contentLabel.setContentMode(ContentMode.HTML);
 						contentLabel.setValue(text);
 						return contentLabel;
 					}
 				});
 	}
 
 }
