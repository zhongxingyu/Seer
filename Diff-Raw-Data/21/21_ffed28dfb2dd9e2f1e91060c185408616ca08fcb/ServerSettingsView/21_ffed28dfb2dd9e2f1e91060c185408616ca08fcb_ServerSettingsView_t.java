 package ch.cern.atlas.apvs.client.ui;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.ServerSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.settings.ServerSettings;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.client.widget.VerticalFlowPanel;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class ServerSettingsView extends VerticalFlowPanel implements Module {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private CellTable<String> table = new CellTable<String>();
 
 	private ServerSettings settings = new ServerSettings();
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	public ServerSettingsView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory, Arguments args) {
 
 		final RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 
 		add(table);
 
 		// name column
 		Column<String, Object> name = new Column<String, Object>(
 				new EditableCell(ServerSettings.Entry.getNameClasses(), 30)) {
 			@Override
 			public Object getValue(String object) {
 				return object;
 			}
 
 			@Override
 			public void render(Context context, String object,
 					SafeHtmlBuilder sb) {
 				String value = (String) getValue(object);
 				getCell().render(context,
 						SafeHtmlUtils.fromSafeConstant(value), sb);
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		table.addColumn(name, "Server Setting");
 
 		Column<String, Object> column = new Column<String, Object>(
 				new EditableCell(ServerSettings.Entry.getCellClasses(), 50)) {
 			@Override
 			public Object getValue(String name) {
				return hidePwd(settings.get(name));
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				Object s = getValue(name);
 				getCell().render(context, s, sb);
 			}
 
 			@Override
 			public void onBrowserEvent(Context context, Element elem,
 					String name, NativeEvent event) {
 				super.onBrowserEvent(context, elem, name, event);
 			}
 		};
 
 		column.setFieldUpdater(new FieldUpdater<String, Object>() {
 
 			@Override
 			public void update(int index, String name, Object value) {
 				log.info("Updated " + index + " " + name + " " + value + " "
 						+ value.getClass());
 				settings.put(name, value.toString());
 				((RemoteEventBus) eventBus)
 						.fireEvent(new ServerSettingsChangedRemoteEvent(settings));
 			}
 		});
 
 		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		table.addColumn(column, "Value");
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(ServerSettings.Entry.getKeys());
 
 		ServerSettingsChangedRemoteEvent.subscribe(eventBus,
 				new ServerSettingsChangedRemoteEvent.Handler() {
 					@Override
 					public void onServerSettingsChanged(
 							ServerSettingsChangedRemoteEvent event) {
 
 						settings = event.getServerSettings();
 
 						log.info(settings.toString());
 
 						scheduler.update();
 					}
 				});
 
 		return true;
 	}
 
 	@Override
 	public boolean update() {
 		table.redraw();
 		return false;
 	}
	
	private String hidePwd(String s) {
		if (s == null) {
			return s;
		}
		
		String[] part = s.split("\\@\\/\\/", 2);
		if (part.length <= 1) {
			return s;
		}
		
		String[] userPwd = part[0].split("\\/", 2);
		if (userPwd.length <= 1) {
			return s;
		}
		
		return userPwd[0]+"/@//"+part[1];
	}
 
 }
