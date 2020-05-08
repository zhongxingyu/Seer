 package ch.cern.atlas.apvs.client;
 
 import java.util.Date;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.service.ServerService.User;
 import ch.cern.atlas.apvs.client.settings.LocalStorage;
 import ch.cern.atlas.apvs.client.settings.Proxy;
 import ch.cern.atlas.apvs.client.settings.SettingsPersister;
 import ch.cern.atlas.apvs.client.ui.AlarmView;
 import ch.cern.atlas.apvs.client.ui.Arguments;
 import ch.cern.atlas.apvs.client.ui.AudioSummary;
 import ch.cern.atlas.apvs.client.ui.AudioSupervisorSettingsView;
 import ch.cern.atlas.apvs.client.ui.AudioView;
 import ch.cern.atlas.apvs.client.ui.CameraTable;
 import ch.cern.atlas.apvs.client.ui.CameraView;
 import ch.cern.atlas.apvs.client.ui.EventView;
 import ch.cern.atlas.apvs.client.ui.GeneralInfoView;
 import ch.cern.atlas.apvs.client.ui.InterventionView;
 import ch.cern.atlas.apvs.client.ui.MeasurementConfigurationView;
 import ch.cern.atlas.apvs.client.ui.MeasurementTable;
 import ch.cern.atlas.apvs.client.ui.MeasurementView;
 import ch.cern.atlas.apvs.client.ui.Module;
 import ch.cern.atlas.apvs.client.ui.PlaceView;
 import ch.cern.atlas.apvs.client.ui.ProcedureControls;
 import ch.cern.atlas.apvs.client.ui.ProcedureView;
 import ch.cern.atlas.apvs.client.ui.PtuSettingsView;
 import ch.cern.atlas.apvs.client.ui.PtuTabSelector;
 import ch.cern.atlas.apvs.client.ui.PtuView;
 import ch.cern.atlas.apvs.client.ui.ServerSettingsView;
 import ch.cern.atlas.apvs.client.ui.Tab;
 import ch.cern.atlas.apvs.client.ui.TimeView;
 import ch.cern.atlas.apvs.client.widget.DialogResultEvent;
 import ch.cern.atlas.apvs.client.widget.DialogResultHandler;
 import ch.cern.atlas.apvs.client.widget.PasswordDialog;
 import ch.cern.atlas.apvs.domain.Device;
 import ch.cern.atlas.apvs.domain.InterventionMap;
 import ch.cern.atlas.apvs.domain.Ternary;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * @author Mark Donszelmann
  */
 public class APVS implements EntryPoint {
 
 //	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	@SuppressWarnings("unused")
 	private Window screen;
 
 	private RemoteEventBus remoteEventBus;
 	@SuppressWarnings("unused")
 	private PlaceController placeController;
 	@SuppressWarnings("unused")
 	private SettingsPersister settingsPersister;
 
 	private Device defaultPtu;
 	
 	private ClientFactory clientFactory;
 	
 	private Ternary alive = Ternary.Unknown;
 	private String aliveCause = "Not yet checked";
 
 	@Override
 	public void onModuleLoad() {
 		GWT.setUncaughtExceptionHandler(new APVSUncaughtExceptionHandler());
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				boot();
			}
		});
	}
		
	private void boot() {
 		Build build = GWT.create(Build.class);
 //		log.info("Starting APVS Version: " + build.version() + " - "
 //				+ build.build());
 
 		clientFactory = GWT.create(ClientFactory.class);
 		
 		clientFactory.getServerService().isReady(new AsyncCallback<Boolean>() {
 			
 			@Override
 			public void onSuccess(Boolean result) {
 				getProxy();
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert("Server not ready. reload webpage "
 						+ caught);
 			}
 		});
 	}
 
 	private void getProxy() {
 		clientFactory.getServerService().getProxy(new AsyncCallback<Proxy>() {
 			
 			@Override
 			public void onSuccess(Proxy proxy) {
 				clientFactory.setSecure(proxy.isActive());
 
 				clientFactory.setProxy(proxy);
 
 				if (proxy.isActive()) {
 					login(null);
 				} else {
 					// not secure try with plain password
 					String pwd = LocalStorage.getInstance()
 							.get(LocalStorage.SUPERVISOR_PWD);
 					if (pwd != null) {
 						login(pwd);
 					} else {
 						prompt();
 					}					
 				}
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				// possibly secure, but do not check if supervisor
 				clientFactory.setSecure(false);
 				clientFactory.setProxy(new Proxy(false, ""));
 				try {
 					start();
 				} catch (SerializationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	private void login(final String pwd) {
 		clientFactory.getServerService().login(pwd,
 				new AsyncCallback<User>() {
 
 					@Override
 					public void onSuccess(User user) {
 						clientFactory.setUser(user);
 //						log.info("Server ready, user is "
 //								+ (user.isSupervisor() ? "SUPERVISOR" : "OBSERVER"));
 						LocalStorage.getInstance().put(LocalStorage.SUPERVISOR_PWD, user.isSupervisor() ? pwd : null);
 						try {
 							start();
 						} catch (SerializationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						// ignore problem, start as an observer
 						clientFactory.setUser(new User("Unknown", "", false));
 						try {
 							start();
 						} catch (SerializationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				});
 	}
 
 	private void prompt() {
 		final PasswordDialog pwdDialog = new PasswordDialog();
 		pwdDialog.addDialogResultHandler(new DialogResultHandler() {
 
 			@Override
 			public void onDialogResult(DialogResultEvent event) {
 				login(event.getResult());
 			}
 		});
 		pwdDialog.setModal(true);
 		pwdDialog.setGlassEnabled(true);
 		pwdDialog.setPopupPositionAndShow(new PositionCallback() {
 
 			@Override
 			public void setPosition(int offsetWidth, int offsetHeight) {
 				// center
 				pwdDialog.setPopupPosition(
 						(Window.getClientWidth() - offsetWidth) / 3,
 						(Window.getClientHeight() - offsetHeight) / 3);
 			}
 		});
 	}
 	
 	private void start() throws SerializationException {
 		
 		remoteEventBus = clientFactory.getRemoteEventBus();
 		placeController = clientFactory.getPlaceController();
 
 		settingsPersister = new SettingsPersister(remoteEventBus);
 
 		// get first div element
 		NodeList<Element> divs = Document.get().getElementsByTagName("div");
 		if (divs.getLength() == 0) {
 			Window.alert("Please define a <div> element with the class set to your view you want to show.");
 			return;
 		}
 
 		boolean layoutOnlyMode = Window.Location.getQueryString().indexOf(
 				"layout=true") >= 0;
 		if (layoutOnlyMode) {
 //			log.info("Running in layoutOnly mode");
 			return;
 		}
 		
 		for (int i = 0; i < divs.getLength(); i++) {
 			Element element = divs.getItem(i);
 			String id = element.getId();
 
 			if (id.equals("footer")) {
 				Label supervisor = new Label(
 						clientFactory.getFullName()+" : "+
 						(clientFactory.isSupervisor() ? "Supervisor"
 								: "Observer"));
 				supervisor.addStyleName("footer-left");
 				RootPanel.get(id).insert(supervisor, 0);
 				continue;
 			}
 
 			String[] parts = id.split("\\(", 2);
 			if (parts.length == 2) {
 				String className = parts[0];
 				if ((parts[1].length() > 0) && !parts[1].endsWith(")")) {
 //					log.warn("Missing closing parenthesis on '" + id + "'");
 					parts[1] += ")";
 				}
 				Arguments args = new Arguments(
 						parts[1].length() > 0 ? parts[1].substring(0,
 								parts[1].length() - 1) : null);
 
 //				log.info("Creating " + className + " with args (" + args + ")");
 
 				Module module = null;
 				// FIXME handle generically
 				if (id.startsWith("MeasurementView")) {
 					module = new MeasurementView();
 				} else if (id.startsWith("MeasurementTable")) {
 					module = new MeasurementTable();
 				} else if (id.startsWith("AlarmView")) {
 					module = new AlarmView();
 				} else if (id.startsWith("AudioSummary")) {
 					module = new AudioSummary();
 				} else if (id.startsWith("AudioView")) {
 					module = new AudioView();
 				} else if (id.startsWith("AudioSupervisorSettingsView")) {
 					module = new AudioSupervisorSettingsView();
 				} else if (id.startsWith("CameraTable")) {
 					module = new CameraTable();
 				} else if (id.startsWith("CameraView")) {
 					module = new CameraView();
 				} else if (id.startsWith("EventView")) {
 					module = new EventView();
 				} else if (id.startsWith("GeneralInfoView")) {
 					module = new GeneralInfoView();
 				} else if (id.startsWith("MeasurementConfigurationView")) {
 					module = new MeasurementConfigurationView();
 				} else if (id.startsWith("InterventionView")) {
 					module = new InterventionView();
 				} else if (id.startsWith("PlaceView")) {
 					module = new PlaceView();
 				} else if (id.startsWith("ProcedureControls")) {
 					module = new ProcedureControls();
 				} else if (id.startsWith("ProcedureView")) {
 					module = new ProcedureView();
 				} else if (id.startsWith("PtuSettingsView")) {
 					module = new PtuSettingsView();
 				} else if (id.startsWith("PtuTabSelector")) {
 					module = new PtuTabSelector();
 				} else if (id.startsWith("PtuView")) {
 					module = new PtuView();
 				} else if (id.startsWith("ServerSettingsView")) {
 					module = new ServerSettingsView();
 				} else if (id.startsWith("Tab")) {
 					module = new Tab();
 				} else if (id.startsWith("TimeView")) {
 					module = new TimeView();
 				}
 
 				if (module != null) {
 					boolean add = module
 							.configure(element, clientFactory, args);
 					if (add && module instanceof IsWidget) {
 						RootPanel.get(id).add((IsWidget) module);
 					}
 				}
 			}
 			
 			// subscribe to keep track and set default PTU
 			InterventionMapChangedRemoteEvent.subscribe(remoteEventBus, new InterventionMapChangedRemoteEvent.Handler() {
 				
 				@Override
 				public void onInterventionMapChanged(InterventionMapChangedRemoteEvent event) {
 					InterventionMap interventionMap = event.getInterventionMap();
 									
 					if ((defaultPtu == null) || (interventionMap.get(defaultPtu).equals(null))) {
 						List<Device> ptus = interventionMap.getPtus();
 						if (ptus.size() > 0) {
 							defaultPtu = ptus.get(0);
 						} else {
 							defaultPtu = null;
 						}
 						
 						SelectPtuEvent.fire(clientFactory.getEventBus("ptu"), defaultPtu);
 					}
 				}
 			});
 		}
 		
 		// Server ALIVE status
 		RequestRemoteEvent.register(remoteEventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(ConnectionStatusChangedRemoteEvent.class
 						.getName())) {
 					ConnectionStatusChangedRemoteEvent.fire(remoteEventBus,
 							ConnectionType.server, alive, aliveCause);
 				}
 			}
 		});
 
 		Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
 			
 			@Override
 			public boolean execute() {
 				RequestBuilder request = PingServiceAsync.Util.getInstance().ping(new AsyncCallback<Void>() {
 					
 					@Override
 					public void onSuccess(Void result) {
 						if (!alive.isTrue()) {
 							alive = Ternary.True;
 							aliveCause = "Last connect at: "+new Date();
 							ConnectionStatusChangedRemoteEvent.fire(remoteEventBus, ConnectionType.server, alive, aliveCause);
 						}
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						if (!alive.isFalse()) {
 							alive = Ternary.False;
 							ConnectionStatusChangedRemoteEvent.fire(remoteEventBus, ConnectionType.server, alive, aliveCause);							
 						}
 					}
 				});
 				
 				request.setTimeoutMillis(10000);
 				try {
 					request.send();
 				} catch (RequestException e) {
 				}
 				
 				return true;
 			}
 		}, 20000);
 		
 		return;
 	}
 }
