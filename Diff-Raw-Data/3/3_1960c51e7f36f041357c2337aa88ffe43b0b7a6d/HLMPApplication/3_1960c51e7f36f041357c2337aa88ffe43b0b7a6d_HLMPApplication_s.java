 package android.HLMPConnect;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import android.os.Handler;
 import android.util.Log;
 
 import hlmp.CommLayer.Communication;
 import hlmp.CommLayer.Configuration;
 import hlmp.CommLayer.SubProtocolList;
 import hlmp.CommLayer.Messages.Message;
 import hlmp.CommLayer.Observers.ConnectEventObserverI;
 import hlmp.CommLayer.Observers.ConnectingEventObserverI;
 import hlmp.CommLayer.Observers.DisconnectEventObserverI;
 import hlmp.CommLayer.Observers.DisconnectingEventObserverI;
 import hlmp.CommLayer.Observers.ErrorMessageEventObserverI;
 import hlmp.CommLayer.Observers.ExceptionEventObserverI;
 import hlmp.CommLayer.Observers.NetInformationEventObserverI;
 import hlmp.NetLayer.Constants.IpState;
 import hlmp.NetLayer.Constants.WifiConnectionState;
 import hlmp.NetLayer.Interfaces.WifiHandler;
 import hlmp.SubProtocol.Chat.ChatProtocol;
 import hlmp.SubProtocol.Ping.PingProtocol;
 
 import android.adhoc.AdHocApp;
 import android.adhoc.AdHocService;
 import android.HLMPConnect.Managers.ChatManager;
 import android.HLMPConnect.Managers.UsersManager;
 import android.HLMPConnect.Managers.PingManager;
 
 
 public class HLMPApplication extends AdHocApp implements ErrorMessageEventObserverI, ExceptionEventObserverI, NetInformationEventObserverI, WifiHandler, ConnectEventObserverI, ConnectingEventObserverI, DisconnectEventObserverI, DisconnectingEventObserverI {
 	static final String MSG_TAG = "HLMPApplication";
 	static final int HLMP_STARTING_SHOW = 0;
 	static final int HLMP_STARTING_HIDE = 1;
 	static final int HLMP_STOPPING_SHOW = 2;
 	static final int HLMP_STOPPING_HIDE = 3;
 	
 	static HLMPApplication self;
 	
 	protected Communication communication;
 	protected ChatManager chatManager;
 	protected UsersManager usersManager;
 	protected PingManager pingManager;
 	protected Handler tabHostHandler;
 	
 	final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(android.os.Message msg) {
 			self.startAdHoc();
 		};
     };
 	
     final Handler hlmpDialogsHandler = new Handler() {
 		@Override
 		public void handleMessage(android.os.Message msg) {
 			if (msg.what == HLMP_STARTING_SHOW) {
 				self.adHocActivity.showDialog(ConnectionsActivity.DLG_HLMP_STARTING);
 			}
 			else if (msg.what == HLMP_STARTING_HIDE) {
 				try{
 					self.adHocActivity.dismissDialog(ConnectionsActivity.DLG_HLMP_STARTING);
 			    } catch(Exception e) {}
 			}
 			else if (msg.what == HLMP_STOPPING_SHOW) {
 				try{
 					self.adHocActivity.showDialog(ConnectionsActivity.DLG_HLMP_STOPPING);
 				} catch(Exception e) {}
 			}
 			else if (msg.what == HLMP_STOPPING_HIDE) {
 				try{
 					self.adHocActivity.dismissDialog(ConnectionsActivity.DLG_HLMP_STOPPING);
 			    } catch(Exception e) {}
 			}
 		};
     };
     
 	
     @Override
     public void onCreate() {
     	super.onCreate();
     	self = this;
     };
 	
 	@Override
     public void onTerminate() {
     	super.onTerminate();
     }
 	
 	
 	// HLMPConnect
 	
 	public ChatManager getChatManager() {
 		return this.chatManager;
 	}
 	
 	public UsersManager getUsersManager() {
 		return this.usersManager;
 	}
 
 	public void setTabHostHandler(Handler tabHostHandler) {
 		this.tabHostHandler = tabHostHandler;
 	}
 	
 	// AdHocApp Overrides
 	
 	@Override
 	public void adHocFailed(int error) {
 		super.adHocFailed(error);
 		if (error == AdHocApp.ERROR_ROOT) {
 			//communication.disconnect();
 		}
 	}
 	
 	
 	// HLMP Access
 	
 	public void startHLMP(String username) {
 		// Set HLMP Configurations
 		Configuration configuration = new Configuration();
 		if (this.usersManager == null) {
 			this.usersManager = new UsersManager();
 		}
 		if (this.chatManager == null) {
 			this.chatManager = new ChatManager();
 		}
 		if (this.pingManager == null) {
 			this.pingManager = new PingManager();
 		}
 		
 		// Set HLMP Subprotocols
 		SubProtocolList subProtocols = new SubProtocolList();
 		ChatProtocol chatProtocol = new ChatProtocol(this.chatManager);
 		subProtocols.add(hlmp.SubProtocol.Chat.Types.CHATPROTOCOL, chatProtocol);
 		this.chatManager.setChatProtocol(chatProtocol);
 		this.chatManager.setNetUser(configuration.getNetUser());
 		
 		PingProtocol pingProtocol = new PingProtocol(this.pingManager);
 		subProtocols.add(hlmp.SubProtocol.Ping.Types.PINGPROTOCOL, pingProtocol);
 		
 		// Set HLMP Communication
 		this.communication = new Communication(configuration, subProtocols, null, this);
 		
 		this.communication.subscribeAddUserEvent(this.usersManager);
 		this.communication.subscribeConnectEvent(this);
 		this.communication.subscribeConnectingEvent(this);
 		this.communication.subscribeDisconnectEvent(this);
 		this.communication.subscribeDisconnectingEvent(this);
 		this.communication.subscribeErrorMessageEvent(this);
 		this.communication.subscribeExceptionEvent(this);
 		this.communication.subscribeNetInformationEvent(this);
 		this.communication.subscribeRemoveUserEvent(this.usersManager);
 		this.communication.subscribeRefreshUserEvent(this.usersManager);
 		this.communication.subscribeRefreshLocalUserEvent(this.usersManager);
 		
 		configuration.getNetUser().setName(username);
 		this.communication.startEventConsumer();
 		this.communication.connect();
 	}
 
 	public void stopHLMP() {
 		if (communication != null) {
 			this.communication.disconnect();
 			this.communication.stopEventConsumer();
 		}
 	}
 
 	public Communication getCommunication() {
 		return this.communication;
 	}
 	
 	
 	// HLMP Events
 	
 	public void netInformationEventUpdate(String s) {
 		Log.i(MSG_TAG, s);
 	}
 	
 	public void exceptionEventUpdate(Exception e) {
 		Log.e(MSG_TAG, " EXCEPTION: " + e.toString());	
 	}
 	
 	public void errorMessageEventUpdate(Message m) {
 		Log.e(MSG_TAG, " ERROR: " + m.toString());
 	}
 
 	public void connectingEventUpdate() {
 		hlmpDialogsHandler.sendEmptyMessage(HLMP_STARTING_SHOW);
 	}
 	
 	public void connectEventUpdate() {
 		tabHostHandler.sendEmptyMessage(Tabs.ACTIVE);
 		hlmpDialogsHandler.sendEmptyMessage(HLMP_STARTING_HIDE);
 	}
 	
 	public void disconnectingEventUpdate() {
 		hlmpDialogsHandler.sendEmptyMessage(HLMP_STOPPING_SHOW);
 		
 	}
 
 	public void disconnectEventUpdate() {
 		hlmpDialogsHandler.sendEmptyMessage(HLMP_STOPPING_HIDE);
 	}
 	
 	
 	// HLMP WifiHandler Implement
 	
 	public void connect() {
 		// Necessary Hanlder for context
 		mHandler.sendEmptyMessage(0);
 	}
 
 	public void disconnect() {
 		this.stopAdHoc();
 	}
 
 	public int getConnectionState() {
 		int state = WifiConnectionState.STOP;
 		switch (this.getAdHocServiceState()) {
 			case AdHocService.STATE_FAILED : {
 				state = WifiConnectionState.FAILED;
 				break;
 			}
 			case AdHocService.STATE_STARTING : {
 				state = WifiConnectionState.WAITING;
 				break;
 			}
 			case AdHocService.STATE_RUNNING : {
 				state = WifiConnectionState.CONNECTED;
 				break;
 			}
 		}
 		return state;
 	}
 
 	public int getIpState() {
 		int state = IpState.NOTFOUND;
 		switch (this.getAdHocServiceState()) {
 			case AdHocService.STATE_FAILED : {
 				state = IpState.INVALID;
 				break;
 			}
 			case AdHocService.STATE_STARTING : {
 				state = IpState.NOTFOUND;
 				break;
 			}
 			case AdHocService.STATE_RUNNING : {
 				state = IpState.VALID;
 				break;
 			}
 		}
 		return state;
 	}
 
 	public InetAddress getInetAddress() {
 		InetAddress inetAddress = null;
 		try {
 			inetAddress = InetAddress.getByName(this.getIPAdress());
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		return inetAddress;
 	}
 
 
 }
