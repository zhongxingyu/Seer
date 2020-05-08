 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.AudioSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.service.AudioServiceAsync;
 import ch.cern.atlas.apvs.client.settings.AudioSettings;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.ListDataProvider;
 
 
 
 public class AudioView extends VerticalPanel implements Module{
 
 
 	private CellTable<String> table = new CellTable<String>();
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	
 	private List<String> activePtuIds = new ArrayList<String>();
 	private AudioSettings audioAccounts = new AudioSettings();
 	private static final Button btnCall = new Button("CALL");
 	
 	public AudioView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory, Arguments args) {
 		final RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 		
 		add(table);
 		activePtuIds.add("ALEX");
 		activePtuIds.add("MARK");
 		
 		
 		//PTU ID
 		Column<String, String> ptuId = new Column<String, String>(
 				new TextCell()) {
 			@Override
 			public String getValue(String object) {
 				return object;
 			}			
 		};
 		ptuId.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		table.addColumn(ptuId, "PTU ID");
 		
 		
 		//Username
 		Column<String, String> username = new Column<String, String>(
 				new TextCell()) {
 			@Override
 			public String getValue(String object) {
 				return audioAccounts.getUsername(object);
 			}			
 		};
 		username.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		table.addColumn(username, "Username");
 
 		
 		//Phonenumber
 		Column<String, String> number = new Column<String, String>(
 				new TextCell()) {
 			@Override
 			public String getValue(String object) {
 				return audioAccounts.getNumber(object);
 			}			
 		};
 		number.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		table.addColumn(number, "Phone Number");
 				
 		
 		//TODO Active Call
 		Column<String, String> activeCall = new Column<String, String>(
 				new TextCell()) {
 			@Override
 			public String getValue(String object) {
 				return audioAccounts.getChannel(object);
 			}			
 		};
 		activeCall.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		table.addColumn(activeCall, "Active Call");
 		
 		
 		//Status
 		Column<String, String> status = new Column<String, String>(
 				new TextCell()) {
 			@Override
 			public String getValue(String object) {
 				return audioAccounts.getStatus(object);
 			}			
 		};
 		status.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		table.addColumn(status, "Status");
 		
 		
 		//Action
 		Column<String, String> action = new Column<String, String>(new ButtonCell()) {
 			@Override
 			public String getValue(String object) {
 				return "Call";//audioAccounts.getOnCall(object).toString();
 			}		
 		};
 		action.setFieldUpdater(new FieldUpdater<String, String>() {
 			
 			@Override
 			public void update(int index, String object, String value) {
 				// TODO Implementation of the button Handler
 				AudioServiceAsync.Util.getInstance().call("SIP/1002","1000", new AsyncCallback<Void>() {
 					
 					@Override
 					public void onSuccess(Void result) {
 						System.err.println("Call Established...");
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("Fail to established the call " + caught);				
 					}
 				});
 			}
 		});
 		
 		action.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		table.addColumn(action);
 		
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(activePtuIds);
 							
 		AudioSettingsChangedEvent.subscribe(eventBus,new AudioSettingsChangedEvent.Handler() {
 			
 			@Override
 			public void onAudioSettingsChanged(AudioSettingsChangedEvent event) {
 				System.err.println("Audio Account Settings Changed");
 				audioAccounts = event.getAudioSettings();
 				//TODO update(); UPDATE TABLE
 				update();
 
 			}
 		});
 		
 		//TODO To be implemented.... Requires getting data from db
 				PtuIdsChangedEvent.subscribe(eventBus,
 				new PtuIdsChangedEvent.Handler() {
 
 					@Override
 					public void onPtuIdsChanged(PtuIdsChangedEvent event) {
 						System.err.println("PTU IDS changed");
 						activePtuIds = event.getPtuIds();
 						dataProvider.getList().clear();
 						dataProvider.getList().addAll(activePtuIds);
 					
 						//TODO update(); UPDATE TABLE
 					}
 		});
 		 	
 		//TODO update
 		
 		
 		return true;
 
 	}
 	
 	private void update(){
 		table.redraw();
 	}
 		
 }
 	
