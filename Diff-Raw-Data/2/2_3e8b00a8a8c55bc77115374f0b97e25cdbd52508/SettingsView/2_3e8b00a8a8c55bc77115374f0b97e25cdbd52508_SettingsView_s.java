 package ch.cern.atlas.apvs.client;
 
 import java.util.Arrays;
 
 import ch.cern.atlas.apvs.client.event.SettingsChangedEvent;
 import ch.cern.atlas.apvs.client.widget.HorizontalFlowPanel;
 import ch.cern.atlas.apvs.client.widget.VerticalFlowPanel;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.EditTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.SelectionCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class SettingsView extends VerticalFlowPanel {
 
 	private int id = 1;
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private CellTable<String> table = new CellTable<String>();
 
 	private Settings settings;
 	private RemoteEventBus eventBus;
 
 	private static final String[] settingNames = { "Name", "PTU Id",
 			"Dosimeter #", "URL Helmet Camera", "URL Hand Camera" };
 	@SuppressWarnings("rawtypes")
 	private static final Class[] cellClass = { EditTextCell.class,
 			SelectionCell.class, SelectionCell.class, EditTextCell.class,
 			EditTextCell.class };
 	
 	private HorizontalFlowPanel buttonPanel = new HorizontalFlowPanel();
 
 	public SettingsView(RemoteEventBus eventBus) {
 		this.eventBus = eventBus;
 
 		add(table);
 		add(buttonPanel);
 		
		Button add = new Button();
 		add.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				insertColumn(id++);
 			}
 		});
 		buttonPanel.add(add);
 		
 		SettingsChangedEvent.subscribe(eventBus, new SettingsChangedEvent.Handler() {
 			@Override
 			public void onSettingsChanged(SettingsChangedEvent event) {
 				settings = event.getSettings();
 				
 				update();
 			}
 		});
 
 		// name column
 		TextColumn<String> name = new TextColumn<String>() {
 			@Override
 			public String getValue(String object) {
 				return object;
 			}
 
 			@Override
 			public void render(Context context, String object,
 					SafeHtmlBuilder sb) {
 				((TextCell) getCell()).render(context,
 						SafeHtmlUtils.fromSafeConstant(getValue(object)), sb);
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		table.addColumn(name, "Setting");
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(Arrays.asList(settingNames));
 
 		update();
 	}
 
 	private void insertColumn(final int id) {
 		int columnIndex = id;
 		@SuppressWarnings("unchecked")
 		Column<String, String> column = new Column<String, String>(
 				new EditableCell(cellClass)) {
 			@Override
 			public String getValue(String name) {
 				String s = settings.get(id, name);
 				if (s != null) {
 					return s;
 				}
 				return "";
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				String s = getValue(name);
 				getCell().render(context, s, sb);
 			}
 		};
 
 		column.setFieldUpdater(new FieldUpdater<String, String>() {
 
 			@Override
 			public void update(int index, String name, String value) {
 				System.err.println("Updated " + index + " " + name + " "
 						+ value + " for " + id);
 				settings.put(id, name, value);
 
 				eventBus.fireEvent(new SettingsChangedEvent(settings));
 			}
 		});
 
 		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		table.insertColumn(columnIndex, column, Integer.toString(id));
 		
 		// FIXME add to settings and fire event
 		
 		update();
 	}
 
 	private void update() {
 		table.redraw();
 	}
 }
