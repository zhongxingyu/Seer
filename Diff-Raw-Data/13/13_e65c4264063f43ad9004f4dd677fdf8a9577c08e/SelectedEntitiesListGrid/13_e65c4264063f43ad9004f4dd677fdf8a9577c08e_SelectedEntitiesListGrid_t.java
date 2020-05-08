 package mayo.edu.cts2.editor.client.widgets.search;
 
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.types.ListGridFieldType;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
 import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
 import mayo.edu.cts2.editor.client.Cts2Editor;
 import mayo.edu.cts2.editor.client.datasource.SelectedEntitiesXmlDS;
 import mayo.edu.cts2.editor.client.events.AddEntityDeselectedEvent;
 import mayo.edu.cts2.editor.client.events.AddEntityDeselectedEventHandler;
 import mayo.edu.cts2.editor.client.events.AddEntitySelectedEvent;
 import mayo.edu.cts2.editor.client.events.AddEntitySelectedEventHandler;
 import mayo.edu.cts2.editor.client.events.SelectedEntityAddedEvent;
 import mayo.edu.cts2.editor.client.events.SelectedEntityRemovedEvent;
 
 public class SelectedEntitiesListGrid extends ListGrid {
 
 	private final SelectedEntitiesXmlDS selectedEntitiesXmlDS;
 
 	public SelectedEntitiesListGrid() {
 		super();
 		selectedEntitiesXmlDS = new SelectedEntitiesXmlDS();
 
 
 		setWidth100();
 		setHeight100();
 
 		setShowAllRecords(true);
 		setWrapCells(false);
 		setDataSource(selectedEntitiesXmlDS);
 
 		ListGridField idField = new ListGridField("uri", "URI");
 		idField.setHidden(true);
 
 		ListGridField deleteField = new ListGridField("delete", "");
 		deleteField.setType(ListGridFieldType.ICON);
 		deleteField.setIcon("delete.png");
 		deleteField.setShowHover(false);
 		deleteField.setCanSort(false);
 		deleteField.addRecordClickHandler(new RecordClickHandler() {
 			@Override
 			public void onRecordClick(RecordClickEvent recordClickEvent) {
 				removeRecord(getSelectedRecord().getAttribute("uri"));
 			}
 		});
 
 		ListGridField codeField = new ListGridField("name", "Code");
 		codeField.setWrap(false);
 
 		ListGridField descriptionField = new ListGridField("designation", "Description");
 		descriptionField.setWrap(false);
 
 		ListGridField codeSystemField = new ListGridField("namespace", "Code System");
 		codeSystemField.setWrap(false);
 
 		ListGridField codeSystemVersionField = new ListGridField("codeSystemVersion", "Code System Version");
 		codeSystemVersionField.setWrap(false);
 
 		setFields(deleteField, idField, codeField, descriptionField, codeSystemField, codeSystemVersionField);
 		setAutoFetchData(false);
 		setCanHover(true);
 		setShowHover(true);
 		setShowHoverComponents(true);
 
 		addEventHandlers();
 	}
 
 	public void clearData() {
 		selectedEntitiesXmlDS.setTestData(new Record[0]);
 		setData(new ListGridRecord[0]);
 		refresh();
 	}
 
 	private void addEventHandlers() {
 		Cts2Editor.EVENT_BUS.addHandler(AddEntitySelectedEvent.TYPE, new AddEntitySelectedEventHandler() {
 			@Override
 			public void onEntitySelected(AddEntitySelectedEvent event) {
 				ListGridRecord record = new ListGridRecord();
 				record.setAttribute("uri", event.getUri());
 				record.setAttribute("name", event.getName());
 				record.setAttribute("designation", event.getDesignation());
 				record.setAttribute("namespace", event.getNamespace());
 				record.setAttribute("codeSystemVersion", event.getCodeSystemVersion());
 				addData(record);
 				refresh();
 				Cts2Editor.EVENT_BUS.fireEvent(new SelectedEntityAddedEvent());
 			}
 		});
 
 		Cts2Editor.EVENT_BUS.addHandler(AddEntityDeselectedEvent.TYPE, new AddEntityDeselectedEventHandler() {
 			@Override
 			public void onEntityDeselected(AddEntityDeselectedEvent event) {
 				removeRecord(event.getHref());
 			}
 		});
 	}
 
 	private void removeRecord(String href) {
 		ListGridRecord record = new ListGridRecord();
 		record.setAttribute("uri", href);
 		removeData(record);
 		refresh();
 		Cts2Editor.EVENT_BUS.fireEvent(new SelectedEntityRemovedEvent(href));
 	}
 
 	private void refresh() {
 		fetchData();
 		redraw();
 	}
 
 	public boolean containsRecord(String href) {
 		boolean contains = false;
 		for (Record record : getRecords()) {
 			if (record.getAttribute("uri").equals(href)) {
 				contains = true;
 				break;
 			}
 		}
 		return contains;
 	}
 
 	public String getCodeSystemVersionSelected(String codeSystem) {
 		/* TODO: cache the code system and version rather then iterating over all the records. */
 		String version = null;
 		for (Record record : getRecords()) {
 			if (record.getAttribute("namespace").equals(codeSystem)) {
 				version = record.getAttribute("codeSystemVersion");
 				break;
 			}
 		}
 		return version;
 	}
 
 	public boolean hasSelectedEntities() {
		return getDataAsRecordList() != null && !getDataAsRecordList().isEmpty();
 	}
 
 	public void setSelectedEntities(Record[] entries) {
 		for (Record entry : entries) {
 			addData(entry);
 		}
 		refresh();
 		Cts2Editor.EVENT_BUS.fireEvent(new SelectedEntityAddedEvent());
 	}
 
 }
