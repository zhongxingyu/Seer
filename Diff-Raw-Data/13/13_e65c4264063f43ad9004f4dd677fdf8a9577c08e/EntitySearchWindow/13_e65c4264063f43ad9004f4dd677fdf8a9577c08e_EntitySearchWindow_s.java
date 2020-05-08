 package mayo.edu.cts2.editor.client.widgets.search;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.smartgwt.client.core.DataClass;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.data.RecordList;
 import com.smartgwt.client.widgets.form.fields.PickerIcon;
 import com.smartgwt.client.widgets.form.fields.SelectItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
 import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
 import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
 import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
 import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.layout.Layout;
 import com.smartgwt.client.widgets.layout.LayoutSpacer;
 import mayo.edu.cts2.editor.client.Cts2Editor;
 import mayo.edu.cts2.editor.client.Cts2EditorService;
 import mayo.edu.cts2.editor.client.Cts2EditorServiceAsync;
 import mayo.edu.cts2.editor.client.datasource.ValueSetItemSearchXmlDS;
 import mayo.edu.cts2.editor.client.datasource.ValueSetsSearchXmlDS;
 
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.widgets.Button;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.Window;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.events.CloseClickEvent;
 import com.smartgwt.client.widgets.events.CloseClickHandler;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.TextItem;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import mayo.edu.cts2.editor.client.events.AddSelectedEntitiesEvent;
 import mayo.edu.cts2.editor.client.events.SelectedEntityAddedEvent;
 import mayo.edu.cts2.editor.client.events.SelectedEntityAddedEventHandler;
 import mayo.edu.cts2.editor.client.events.SelectedEntityRemovedEvent;
 import mayo.edu.cts2.editor.client.events.SelectedEntityRemovedEventHandler;
 import mayo.edu.cts2.editor.client.events.ValueSetItemsReceivedEvent;
 import mayo.edu.cts2.editor.client.events.ValueSetItemsReceivedEventHandler;
 import mayo.edu.cts2.editor.client.events.ValueSetsReceivedEvent;
 import mayo.edu.cts2.editor.client.events.ValueSetsReceivedEventHandler;
 import mayo.edu.cts2.editor.shared.DefinitionEntry;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class EntitySearchWindow extends Window {
 
 	private static final String BACKGROUND_COLOR = "#ECECEC";
 	private static final String TITLE = "Edit";
 	private static final String ROWS_RETRIEVED_TITLE = "Rows Matching Criteria:";
 
 	private Label rowsRetrievedLabel;
 	private SelectItem codeSystemCb;
 	private SelectItem codeSystemVersionCb;
 	protected Button finishBtn;
 	protected Button cancelBtn;
 	private TextItem filterTi;
 	private PickerIcon searchPicker;
 	private SearchValueSetItemsListGrid searchListGrid;
 	private SelectedEntitiesListGrid selectedEntitiesListGrid;
 	private String entityUrl;
 
 	public EntitySearchWindow() {
 		this(new Record[0]);
 	}
 
 	public EntitySearchWindow(String entityUrl) {
 		this(new Record[0]);
 	}
 
 	public EntitySearchWindow(List<DefinitionEntry> entries) {
 		this(entries, null);
 	}
 
 	public EntitySearchWindow(List<DefinitionEntry> entries, String entityUrl) {
 		this(new Record[0], entityUrl);
 		this.setSelectedEntities(entries);
 	}
 
 	public EntitySearchWindow(Record[] entries) {
 		this(entries, null);
 	}
 
 	public EntitySearchWindow(Record[] entries, String entityUrl) {
 		super();
 
 		if (entityUrl != null && !entityUrl.trim().isEmpty()) {
 			this.entityUrl = entityUrl;
 		}
 
 		searchListGrid = new SearchValueSetItemsListGrid();
 
 		VLayout layout = new VLayout(5);
 
 		setWidth100();
 		setHeight100();
 		setMargin(20);
 
 		setTitle(TITLE);
 		setShowMinimizeButton(false);
 		setIsModal(true);
 		setShowModalMask(true);
 		setCanDragResize(true);
 		centerInPage();
 
 		addCloseClickHandler(new CloseClickHandler() {
 
 			@Override
 			public void onCloseClick(CloseClickEvent event) {
 				destroy();
 			}
 		});
 
 		layout.addMember(createDisplayLabel());
 		layout.addMember(createFilterPanel());
 		layout.addMember(createSearchPanel());
 		layout.addMember(createSelectedCodesPanel());
 		layout.addMember(getButtons());
 
 		addItem(layout);
 
 		addEventHandlers();
 		if (entries != null) {
 			selectedEntitiesListGrid.setSelectedEntities(entries);
 		}
 	}
 
 	public Button getCloseButton() {
 		return finishBtn;
 	}
 
 	public Button getCancelButton() {
 		return cancelBtn;
 	}
 
 	public SearchValueSetItemsListGrid getSearchListGrid() {
 		return searchListGrid;
 	}
 
 	public void setSelectedEntities(List<DefinitionEntry> entries) {
 		Record[] records = new Record[entries.size()];
 		int i = 0;
 		for (DefinitionEntry entry : entries) {
 			Record record = new Record();
 			record.setAttribute("uri", entry.getUri());
 			record.setAttribute("namespace", entry.getNamespace());
 			record.setAttribute("codeSystemVersion", entry.getCodeSystemVersion());
 			record.setAttribute("name", entry.getName());
 			record.setAttribute("designation", entry.getDescription());
 			records[i++] = record;
 		}
 		selectedEntitiesListGrid.setSelectedEntities(records);
 	}
 
 	public List<DefinitionEntry> getSelectedEntities() {
 		List<DefinitionEntry> entities = new ArrayList<DefinitionEntry>();
 		for (Record r : selectedEntitiesListGrid.getDataAsRecordList().toArray()) {
 			entities.add(new DefinitionEntry(
 			  r.getAttribute("uri"),
 			  "", //href
 			  r.getAttribute("namespace"),
 			  r.getAttribute("name"),
 			  r.getAttribute("designation"),
 			  r.getAttribute("codeSystemVersion")));
 		}
 		return entities;
 	}
 
 	public RecordList getSelectedEntitiesAsRecords() {
 		return selectedEntitiesListGrid.getDataAsRecordList();
 	}
 
 	private VLayout createDisplayLabel() {
 		String message = "Search for entities.  Select the entities by checking the checkbox.  Start by selecting the code system and version then search in the search box.";
 		Label label = new Label("<b>" + message + "<b>");
 		label.setWidth100();
 		label.setHeight(30);
 		label.setMargin(2);
 		label.setValign(VerticalAlignment.CENTER);
 		label.setBackgroundColor(BACKGROUND_COLOR);
 
 		final VLayout vLayoutLayoutSpacers = new VLayout();
 		vLayoutLayoutSpacers.setWidth100();
 		vLayoutLayoutSpacers.setHeight(30);
 		vLayoutLayoutSpacers.setBackgroundColor(BACKGROUND_COLOR);
 		vLayoutLayoutSpacers.setLayoutMargin(6);
 		vLayoutLayoutSpacers.setMembersMargin(6);
 
 		vLayoutLayoutSpacers.addMember(label);
 
 		return vLayoutLayoutSpacers;
 	}
 
 	private VLayout createSearchPanel() {
 
 		VLayout searchLayout = new VLayout();
 		searchLayout.setWidth100();
 		searchLayout.setHeight100();
 		searchLayout.setLayoutMargin(10);
 		searchLayout.setLayoutMargin(6);
 		searchLayout.setMembersMargin(15);
 		searchLayout.addMember(searchListGrid);
 		searchLayout.setShowResizeBar(true);
 		return searchLayout;
 	}
 
 	private HLayout getButtons() {
 
 		// Buttons on the bottom
 		HLayout buttonLayout = new HLayout();
 		buttonLayout.setWidth100();
 		buttonLayout.setHeight(40);
 		buttonLayout.setLayoutMargin(6);
 		buttonLayout.setMembersMargin(10);
 		buttonLayout.setAlign(Alignment.RIGHT);
 
 		finishBtn = new Button("Finish");
 
 		// initially disable
 		finishBtn.setDisabled(true);
 
 		finishBtn.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 
 				// let others know that records are to be added.
 				Cts2Editor.EVENT_BUS.fireEvent(new AddSelectedEntitiesEvent(selectedEntitiesListGrid.getRecords()));
 
 				// hide the window, but don't destroy
 				hide();
 			}
 		});
 
 		cancelBtn = new Button("Cancel");
 		cancelBtn.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				// close the window
 				destroy();
 			}
 		});
 
 		buttonLayout.addMember(finishBtn);
 		buttonLayout.addMember(cancelBtn);
 
 		return buttonLayout;
 	}
 
 	private void updateRowsRetrieved(DataClass[] dc) {
 		if (dc.length >= 1) {
 
 			String numEntries = dc[0].getAttribute("numEntries");
 			String complete = dc[0].getAttribute("complete");
 
 			if (complete != null && !complete.equals("COMPLETE")) {
 				rowsRetrievedLabel.setContents(ROWS_RETRIEVED_TITLE + "<b> " + numEntries + "</b>+");
 			} else {
 				rowsRetrievedLabel.setContents(ROWS_RETRIEVED_TITLE + " <b>" + numEntries + "</b>");
 			}
 
 			String searchText = filterTi.getValueAsString();
 			if (searchText == null || searchText.length() == 0) {
 				rowsRetrievedLabel.setContents("");
 			}
 		} else {
 			rowsRetrievedLabel.setContents(ROWS_RETRIEVED_TITLE + " <b>0</b>");
 		}
 	}
 
 	private Layout createFilterPanel() {
 
 		VLayout comboboxLayout = new VLayout();
 		DynamicForm codeSystemForm = new DynamicForm();
 		codeSystemCb = new SelectItem("codesystem");
 		codeSystemCb.setWrapTitle(false);
 		codeSystemCb.setTitle("Code System");
 		codeSystemCb.setAttribute("browserSpellCheck", false);
 
 		codeSystemVersionCb = new SelectItem("codesystemversion");
 		codeSystemVersionCb.setWrapTitle(false);
 		codeSystemVersionCb.setTitle("Code System Version");
 		codeSystemVersionCb.setAttribute("browserSpellCheck", false);
 
 		final Cts2EditorServiceAsync service = GWT.create(Cts2EditorService.class);
 		if (entityUrl != null) {
 			service.addServiceProperty("EntityUrl", entityUrl, new AsyncCallback<Void>() {
 				@Override
 				public void onFailure(Throwable caught) { }
 
 				@Override
 				public void onSuccess(Void result) { }
 			});
 		}
 
 		service.getCodeSystems(new AsyncCallback<String[]>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				codeSystemCb.setValueMap(new String[0]);
 			}
 
 			@Override
 			public void onSuccess(String[] result) {
 				codeSystemCb.setValueMap(result);
 				executeSearch();
 			}
 		});
 
 		codeSystemCb.addChangedHandler(new ChangedHandler() {
 			@Override
 			public void onChanged(ChangedEvent changedEvent) {
 				codeSystemVersionCb.setValueMap(new String[0]);
 				codeSystemVersionCb.clearValue();
 				diableSearch();
 				searchListGrid.setData(new ListGridRecord[0]);
 				String version = selectedEntitiesListGrid.getCodeSystemVersionSelected((String) changedEvent.getValue());
 				if (version == null) {
 					service.getCodeSystemVersions((String) changedEvent.getValue(), new AsyncCallback<String[]>() {
 						@Override
 						public void onFailure(Throwable caught) {
 							codeSystemVersionCb.setValueMap(new String[0]);
 						}
 
 						@Override
 						public void onSuccess(String[] result) {
 							codeSystemVersionCb.setValueMap(result);
 							if (result.length == 1) {
 								codeSystemVersionCb.setValue(result[0]);
 								diableSearch();
 							}
 							executeSearch();
 						}
 					});
 					codeSystemVersionCb.setDisabled(false);
 				} else {
 					codeSystemVersionCb.setValue(version);
 					codeSystemVersionCb.setDisabled(true);
 				}
 			}
 		});
 
 		codeSystemVersionCb.addChangedHandler(new ChangedHandler() {
 			@Override
 			public void onChanged(ChangedEvent changedEvent) {
 				diableSearch();
 				executeSearch();
 			}
 		});
 
 		codeSystemForm.setItems(codeSystemCb, codeSystemVersionCb);
 
 		comboboxLayout.addMember(codeSystemForm);
 
 		PickerIcon clearPicker = new PickerIcon(PickerIcon.CLEAR, new FormItemClickHandler() {
 			public void onFormItemClick(FormItemIconClickEvent event) {
 				filterTi.setValue("");
 				executeSearch();
 			}
 		});
 
 		searchPicker = new PickerIcon(PickerIcon.SEARCH, new FormItemClickHandler() {
 			public void onFormItemClick(FormItemIconClickEvent event) {
 				executeSearch();
 			}
 		});
 
 		VLayout filterLayout = new VLayout();
 		filterLayout.setWidth100();
 		filterLayout.setAlign(Alignment.RIGHT);
 		filterLayout.setMargin(2);
 		DynamicForm filterForm = new DynamicForm();
 		filterTi = new TextItem("filter");
 		filterTi.setWidth("100%");
 		filterTi.addKeyPressHandler(new KeyPressHandler() {
 			@Override
 			public void onKeyPress(KeyPressEvent keyPressEvent) {
 				if (keyPressEvent.getKeyName().equals("Enter")) {
 					executeSearch();
 				}
 			}
 		});
 		filterTi.setTitle("Search");
 		filterTi.setIcons(searchPicker, clearPicker);
 
 		filterForm.setItems(filterTi);
 		filterLayout.addMember(filterForm);
 
 		rowsRetrievedLabel = new Label();
 		rowsRetrievedLabel.setWrap(false);
 		rowsRetrievedLabel.setWidth100();
 		rowsRetrievedLabel.setHeight(23);
 		rowsRetrievedLabel.setAlign(Alignment.RIGHT);
 		filterLayout.addMember(rowsRetrievedLabel);
 
 		HLayout mainLayout = new HLayout();
 		mainLayout.setWidth100();
 		mainLayout.addMember(comboboxLayout);
 		LayoutSpacer spacer = new LayoutSpacer();
 		spacer.setWidth("40%");
 		mainLayout.addMember(spacer);
 		mainLayout.addMember(filterLayout);
 
 		diableSearch();
 		return mainLayout;
 	}
 
 	private void diableSearch() {
 //		filterTi.setDisabled(codeSystemVersionCb.getValueAsString() == null
 //		  || codeSystemVersionCb.getValueAsString().isEmpty());
 	}
 
 	private Layout createSelectedCodesPanel() {
 		VLayout searchLayout = new VLayout();
 		searchLayout.setWidth100();
 		searchLayout.setHeight100();
 		searchLayout.setLayoutMargin(10);
 		searchLayout.setLayoutMargin(6);
 		searchLayout.setMembersMargin(15);
 
 		Label selectedLabel = new Label("Selected Entities");
 		selectedLabel.setAlign(Alignment.LEFT);
 		selectedLabel.setAutoFit(true);
 		selectedLabel.setWrap(false);
 
 		selectedEntitiesListGrid = new SelectedEntitiesListGrid();
 		searchLayout.addMember(selectedLabel);
 		searchLayout.addMember(selectedEntitiesListGrid);
 
 		return searchLayout;
 	}
 
 	private void executeSearch() {
 		if (codeSystemCb.getValueAsString() != null && !codeSystemCb.getValueAsString().trim().isEmpty()
 		  && codeSystemVersionCb.getValueAsString() != null && !codeSystemVersionCb.getValueAsString().trim().isEmpty()
 		  && filterTi.getValueAsString() != null && !filterTi.getValueAsString().trim().isEmpty()) {
 			searchListGrid.getData(
 			  codeSystemCb.getValueAsString(),
 			  codeSystemVersionCb.getValueAsString(),
 			  filterTi.getValueAsString());
 			rowsRetrievedLabel.show();
 		} else {
 			rowsRetrievedLabel.hide();
 			searchListGrid.clearData();
 		}
 	}
 
 	private void addEventHandlers() {
 		Cts2Editor.EVENT_BUS.addHandler(ValueSetsReceivedEvent.TYPE, new ValueSetsReceivedEventHandler() {
 
 			@Override
 			public void onValueSetsReceived(ValueSetsReceivedEvent event) {
 				DataClass[] dc = ValueSetsSearchXmlDS.getInstance().getTestData();
 				updateRowsRetrieved(dc);
 			}
 		});
 
 		Cts2Editor.EVENT_BUS.addHandler(ValueSetItemsReceivedEvent.TYPE, new ValueSetItemsReceivedEventHandler() {
 
 			@Override
 			public void onValueSetItemsReceived(ValueSetItemsReceivedEvent event) {
 				DataClass[] dc = ValueSetItemSearchXmlDS.getInstance().getTestData();
 				updateRowsRetrieved(dc);
 				Record[] records = ValueSetItemSearchXmlDS.getInstance().getTestData();
 				for (int i = 0; i < records.length; i++) {
 					if (selectedEntitiesListGrid.containsRecord(records[i].getAttribute("uri"))) {
 						records[i].setAttribute("add", true);
 						searchListGrid.refreshRow(i);
 					}
 				}
 			}
 		});
 
 		Cts2Editor.EVENT_BUS.addHandler(SelectedEntityRemovedEvent.TYPE, new SelectedEntityRemovedEventHandler() {
 			@Override
 			public void onEntityRemoved(SelectedEntityRemovedEvent event) {
 				finishBtn.setDisabled(selectedEntitiesListGrid.getRecords().length <= 0);
 				String uri = event.getUri();
 				Record[] records = searchListGrid.getRecords();
 				for (int i = 0; i < records.length; i++) {
 					if (records[i].getAttribute("uri").equals(uri)) {
 						records[i].setAttribute("add", false);
 						searchListGrid.refreshRow(i);
 						break;
 					}
 				}
			}
		});

		Cts2Editor.EVENT_BUS.addHandler(SelectedEntityAddedEvent.TYPE, new SelectedEntityAddedEventHandler() {
			@Override
			public void onEntityAdded(SelectedEntityAddedEvent event) {
 				finishBtn.setDisabled(!selectedEntitiesListGrid.hasSelectedEntities());
 			}
 		});
 
 		Cts2Editor.EVENT_BUS.addHandler(SelectedEntityAddedEvent.TYPE, new SelectedEntityAddedEventHandler() {
 			@Override
 			public void onEntityAdded(SelectedEntityAddedEvent event) {
 				finishBtn.setDisabled(!selectedEntitiesListGrid.hasSelectedEntities());
 			}
 		});
 
 	}
 
 }
