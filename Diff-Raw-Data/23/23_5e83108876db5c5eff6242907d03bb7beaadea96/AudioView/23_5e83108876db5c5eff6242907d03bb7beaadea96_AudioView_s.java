 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.AudioSupervisorSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.AudioUsersSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.client.event.MeetMeRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.service.AudioServiceAsync;
 import ch.cern.atlas.apvs.client.settings.AudioSettings;
 import ch.cern.atlas.apvs.client.settings.ConferenceRooms;
 import ch.cern.atlas.apvs.client.settings.VoipAccount;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.client.widget.GenericColumn;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class AudioView extends GlassPanel implements Module {
 
 	private CellTable<String> table = new CellTable<String>();
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private AudioSettings voipAccounts = new AudioSettings();
 	private ConferenceRooms conferenceRooms = new ConferenceRooms();
 	private String ptuId = new String("PTUWeb");
 	private static final String SUPERVISOR_ACCOUNT = "SIP/2001";
 	private static final String SUPERVISOR_NUMBER = "2001";
 	private VoipAccount supervisorAccount = new VoipAccount();
 
 	private EventBus cmdBus;
 
 	private List<String> fieldName = new ArrayList<String>(Arrays.asList(new String[] { "Status", "Private Call", "Group Call" }));
 	private List<Class<?>> classField = new ArrayList<Class<?>>(Arrays.asList(new Class<?>[] { TextCell.class, ButtonCell.class,ButtonCell.class }));
 
 	public AudioView() {
 	}
 
 	@Override
 	public boolean configure(Element element, final ClientFactory clientFactory,
 			Arguments args) {
 		
 		final RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 
 		/* 
 		 * TABLE SETUP
 		 * 
 		 	(TextCell)  	 (EditableCell=customizable)	
 		 ________________________________________________________________________________________
 		 |  Field Name  | Status/Action Field               | Status/Action Field               |
 		 |______________|___________________________________|___________________________________|
 		 | Status		|  		Online / Offline		    |  		Online / Offline		    | (textCell field)
 		 | Private Call |    Call/Hangup 'username'         |    Call/Hangup 'username'         | (buttonCell field) 
 		 | Group Call	| Join/Leave/Create 'IMPACT ACTION' | Join/Leave/Create 'IMPACT ACTION' | (buttonCell field)
 		 ****************************************************************************************
 		 * FOLLOWING ROWS ARE DYNAMICALLY ADDED													*
 		 * Mode = On conference																	*
 		 ****************************************************************************************
  		 | Conference   | Close Conference (btn)			| Mute all users(btn)	 			|
  		 | 'User 1'     | Kick (btn)  						| Mute 'User 1'(btn)   				|
  		 | 'User 2'     | Kick(btn)			                | Mute 'User 2'(btn)				|
  		 | 'User n'     | Kick(btn)  						| Mute 'User n'(btn)				|
  		 |______________|___________________________________|___________________________________|
  
 		 */
 		
 		add(table, CENTER);
 		
 		// Status/Action Field column
 		EditableCell fieldActionCell = new EditableCell(classField);
 		GenericColumn<String> fieldActionCol = new GenericColumn<String>(fieldActionCell) {
 			@Override
 			public Object getValue(String fieldName) {
 				if (fieldName.equals("Status"))
 						return ("Status: " + (voipAccounts.getStatus(ptuId)?"Online":"Offline"));
 				else if (fieldName.equals("Private Call"))
 					return ((voipAccounts.getOnCall(ptuId) ? "Hangup '": "Call '") + voipAccounts.getUsername(ptuId) + "'");
 				else if (fieldName.equals("Group Call"))
 					return ((!conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId)) ? ("Make "): (voipAccounts.getOnConference(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT)) ? "Leave ": "Join "))+ "Impact conference");
 				else if(fieldName.equals("Conference"))
 					return "Close '" + voipAccounts.getActivity(ptuId)+ "' conference";
 				else if(fieldName.equals("Mute/Unmute"))
 					return (voipAccounts.getMute(ptuId) ? ("Unmute '") + voipAccounts.getUsername(ptuId) + "'" : ("Mute '") + voipAccounts.getUsername(ptuId) + "'");
 				else if(fieldName.equals("Kick/Add"))
 					return ( voipAccounts.getOnConference(ptuId)? ( (voipAccounts.getRoom(ptuId) == conferenceRooms.roomOfActivity(voipAccounts.getActivity(ptuId))) ? ("Kick '" + voipAccounts.getActivity(ptuId) + "'") : ("Hangup from '" + voipAccounts.getActivity(ptuId) + "' conference")) : ("Add to '" + voipAccounts.getActivity(ptuId) + "'"));
 				else
 					return null;
 			}
 		};
 		
 		fieldActionCol.setHorizontalAlignment(ALIGN_CENTER);
 		fieldActionCol.setEnabled(clientFactory.isSupervisor());
 		table.addColumn(fieldActionCol);
 		
 		fieldActionCol.setFieldUpdater(new FieldUpdater<String, Object>() {
 
 			@Override
 			public void update(int index, String fieldName, Object value) {
 				if (!clientFactory.isSupervisor()) {
 					return;
 				}
 
 				AsyncCallback<Void> callbackConference = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL CONFERENCE ESTABLISHMENT: "
 								+ caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Conference established...");
 					}
 				};
 
 				AsyncCallback<Void> callbackCall = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err
 								.println("FAIL CALL ESTABLISHMENT: " + caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Call Established...");
 					}
 				};
 
 				AsyncCallback<Void> callbackHangup = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL HANGUP ESTABLISHMENT: " + caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Call Hangup...");
 					}
 				};
 				
 				AsyncCallback<Void> callbackMute = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL MUTE: " + caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Mute/Unmute Success...");
 					}	
 				};
 
 				if (fieldName.equals("Private Call")) {
 					if (!voipAccounts.getOnCall(ptuId)) {
 						List<String> channels = new ArrayList<String>();
 						channels.add(supervisorAccount.getChannel());//voipAccounts.getChannel(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT)));
 						channels.add(voipAccounts.getChannel(ptuId));
 						// Hangup Supervisor and PTU User from active calls
 						AudioServiceAsync.Util.getInstance().hangupMultiple(channels, callbackHangup);
 						AudioServiceAsync.Util.getInstance().call(voipAccounts.getNumber(ptuId),SUPERVISOR_NUMBER, callbackCall);
 
 					} else {
 						AudioServiceAsync.Util.getInstance().hangup(supervisorAccount.getChannel(),callbackHangup);//voipAccounts.getChannel(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT)),callbackHangup);
 						if (conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId))) {
 							AudioServiceAsync.Util.getInstance().addToConference(voipAccounts.getNumber(ptuId),conferenceRooms.roomOfActivity(voipAccounts.getActivity(ptuId)),callbackConference);
 						}
 					}
 					
 				} else if (fieldName.equals("Group Call")){
 					if (!voipAccounts.getOnConference(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT))) {
 						if (!conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId))) {
 							// Hangup Impact Activity Users from active calls
 							AudioServiceAsync.Util.getInstance().hangupMultiple(voipAccounts.getActiveChannelsActivity(voipAccounts.getActivity(ptuId)),callbackHangup);
 							AudioServiceAsync.Util.getInstance().newConference(voipAccounts.getNumbersActivity(voipAccounts.getActivity(ptuId)),callbackConference);
 						} else{
 							// Hangup Supervisor from active calls
 							AudioServiceAsync.Util.getInstance().hangup(voipAccounts.getChannel(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT)),callbackHangup);
 							AudioServiceAsync.Util.getInstance().addToConference(SUPERVISOR_ACCOUNT,voipAccounts.getRoom(ptuId),callbackConference);
 						}
 					} else {
 						AudioServiceAsync.Util.getInstance().hangup(voipAccounts.getChannel(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT)),callbackHangup);
 					}
 					
 				} else if (fieldName.equals("Conference")){
 					AudioServiceAsync.Util.getInstance().hangupMultiple(voipAccounts.getActiveChannelsActivity(voipAccounts.getActivity(ptuId)),callbackHangup);
 					if( voipAccounts.getRoom(ptuId).equals(voipAccounts.getRoom(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT))))
 						AudioServiceAsync.Util.getInstance().hangup(voipAccounts.getChannel(voipAccounts.getPtuId(SUPERVISOR_ACCOUNT)),callbackHangup);
 					
 				} else if (fieldName.equals("Mute/Unmute")){
 					System.out.println(voipAccounts.getMute(ptuId));
 					if(voipAccounts.getMute(ptuId)){
 						AudioServiceAsync.Util.getInstance().unMuteUser(voipAccounts.getRoom(ptuId),voipAccounts.getChannel(ptuId), ptuId, callbackMute);
 					}else{
 						AudioServiceAsync.Util.getInstance().muteUser(voipAccounts.getRoom(ptuId),voipAccounts.getChannel(ptuId), ptuId, callbackMute);
 					}
 					
 				} else if (fieldName.equals("Kick/Add")){
 					if(voipAccounts.getOnConference(ptuId))
 						AudioServiceAsync.Util.getInstance().hangup(voipAccounts.getChannel(ptuId),callbackHangup);
 					else
 						AudioServiceAsync.Util.getInstance().addToConference(voipAccounts.getNumber(ptuId),conferenceRooms.roomOfActivity(voipAccounts.getActivity(ptuId)),callbackConference);
 					
 				} else
 					return;
 			}
 		});
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(new ArrayList<String>());
 
 		if (cmdBus != null) {
 			SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 				@Override
 				public void onPtuSelected(SelectPtuEvent event) {
 					ptuId = event.getPtuId();
 					System.err.println("NEW PTU " + ptuId);
 					table.redraw();
 				}
 			});
 		}
 
 		ConnectionStatusChangedRemoteEvent.subscribe(eventBus,
 				new ConnectionStatusChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onConnectionStatusChanged(
 							ConnectionStatusChangedRemoteEvent event) {
 						if (event.getConnection() == ConnectionType.audio) {
 							showGlass(!event.getStatus().isTrue());
 						}
 					}
 				});
 
 		AudioUsersSettingsChangedRemoteEvent.subscribe(eventBus,
 				new AudioUsersSettingsChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onAudioUsersSettingsChanged(
 							AudioUsersSettingsChangedRemoteEvent event) {
 						voipAccounts = event.getAudioSettings();
 						System.out.println("Just got the audio settings");
 						dataProvider.getList().clear();
 						dataProvider.getList().addAll(fieldName);
 					}
 				});
 
 		AudioSupervisorSettingsChangedRemoteEvent.subscribe(eventBus, new AudioSupervisorSettingsChangedRemoteEvent.Handler() {
 			
 			@Override
 			public void onAudioSupervisorSettingsChanged(
 					AudioSupervisorSettingsChangedRemoteEvent event) {
 						supervisorAccount = event.getSupervisorSettings();
 			}
 		});
 		
 		MeetMeRemoteEvent.subscribe(eventBus, new MeetMeRemoteEvent.Handler() {
 
 			@Override
 			public void onMeetMeEvent(MeetMeRemoteEvent event) {
 				if (fieldName.size() > 3) {
 					while (fieldName.size() > 3) {
 						fieldName.remove(3);
 						classField.remove(3);
 					}
 				}
 				
 				if (conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId))) {
 					fieldName.add("Conference");
 					classField.add(ButtonCell.class);
 					fieldName.add("Mute/Unmute");
 					classField.add(ButtonCell.class);
 					fieldName.add("Kick/Add");
 					classField.add(ButtonCell.class);
 				}
 				dataProvider.getList().clear();
 				dataProvider.getList().addAll(fieldName);
 			}
 		});
 		
 		return true;
 	}
 
 	@Override
 	public boolean update() {
 		return false;
 	}
 
 }
