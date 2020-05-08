 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.AudioSupervisorSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.AudioSupervisorStatusRemoteEvent;
 import ch.cern.atlas.apvs.client.settings.VoipAccount;
 import ch.cern.atlas.apvs.client.widget.DynamicSelectionCell;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.client.widget.StringList;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class AudioSupervisorSettingsView extends GlassPanel implements Module {
 
 	private CellTable<VoipAccount> table = new CellTable<VoipAccount>();
 	private ListDataProvider<VoipAccount> dataProvider = new ListDataProvider<VoipAccount>();
 
 	private VoipAccount supervisor = new VoipAccount(true);
 	private List<String> supervisorsList = new ArrayList<String>();
 	private List<VoipAccount> supervisorsAccounts = new ArrayList<VoipAccount>(); 
 	
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 		
 		final RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 		
 		clientFactory.getAudioService().usersList(new AsyncCallback<Void>() {
 			
 			@Override
 			public void onSuccess(Void result) {
 				System.err.println("Supervisor SIP accounts listed...");
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				System.err.println("Fail to list Supervisor SIP accounts " + caught);				
 			}
 		});
 		add(table, CENTER);
 		
 		setVisible(clientFactory.isSupervisor());
 		
 		// Supervisor Label
 		Column<VoipAccount, String> supervisorLabel = new Column<VoipAccount, String> (new TextCell()) {
 			
 			@Override
 			public String getValue(VoipAccount object) {
 				return "Supervisor";
 			}
 		};
 		table.addColumn(supervisorLabel, "Account Type");
 		
 		//  SIP Account
 		Column<VoipAccount, String> account = new Column<VoipAccount, String> (new DynamicSelectionCell(new StringList<String>(supervisorsList))){
 			
 			@Override
 			public String getValue(VoipAccount object) {					
 					return object.getAccount();
 			}
 		};
 		account.setFieldUpdater(new FieldUpdater<VoipAccount, String>() {
 			
 			@Override
 			public void update(int index, VoipAccount object, String value) {
 					for(int i=0; i<supervisorsAccounts.size(); i++){
 						if(supervisorsAccounts.get(i).getAccount().equals(value)){
 							supervisor=supervisorsAccounts.get(i);
 							eventBus.fireEvent(new AudioSupervisorSettingsChangedRemoteEvent(supervisor));
 							return;
 						}	
 					}
 			}
 		});
 		table.addColumn(account, "SIP Account");
 		
 		// Account status
 		Column<VoipAccount, String> status = new Column<VoipAccount, String> (new TextCell()) {
 			
 			@Override
 			public String getValue(VoipAccount account) {
 				for (int i=0; i<supervisorsAccounts.size(); i++){
					if(supervisorsAccounts.get(i).getStatus().equals(supervisor.getStatus()))
							return (supervisorsAccounts.get(i).getStatus() ?"Online":"Offline");
 				}
 				return "Not assigned";
 			}
 			
 			@Override
 			public void render(Context context, VoipAccount object,
 					SafeHtmlBuilder sb) {
 				String value = getValue(object);
 				
 				sb.append(SafeHtmlUtils.fromSafeConstant("<div class=\""
 						+ value.toLowerCase() + "\">"));
 				getCell().render(context, value, sb);
 				sb.append(SafeHtmlUtils.fromSafeConstant("</div>"));
 			}
 		};
 		table.addColumn(status, "Account Status");
		
 		dataProvider.addDataDisplay(table);
 		dataProvider.getList().add(new VoipAccount());		
 
 		AudioSupervisorSettingsChangedRemoteEvent.subscribe(eventBus, new AudioSupervisorSettingsChangedRemoteEvent.Handler()  {
 			
 			@Override
 			public void onAudioSupervisorSettingsChanged(AudioSupervisorSettingsChangedRemoteEvent event) {
 					supervisor = event.getSupervisorSettings();	
 					dataProvider.getList().clear();
 					dataProvider.getList().add(supervisor);
 			}
 		});
 		
 		AudioSupervisorStatusRemoteEvent.subscribe(eventBus, new AudioSupervisorStatusRemoteEvent.Handler()  {
 			
 			@Override
 			public void onAudioSupervisorStatusChanged(AudioSupervisorStatusRemoteEvent event) {
 				supervisorsAccounts = event.getSupervisorsList();
 				supervisorsList.clear();
 				for(int i=0; i<supervisorsAccounts.size();i++){
 					supervisorsList.add(supervisorsAccounts.get(i).getAccount());
 				}	
 				dataProvider.getList().clear();
 				dataProvider.getList().add(supervisor);
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public boolean update() {
 		return false;
 	}
 	
 }
