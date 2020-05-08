 package de.tudresden.inf.rn.mobilis.mxa.services.collabedit;
 
 import jabberSrpc.JabberClient;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.AndFilter;
 import org.jivesoftware.smack.filter.IQTypeFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.IQ;
 import org.jivesoftware.smackx.ServiceDiscoveryManager;
 import org.jivesoftware.smackx.packet.DiscoverItems;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import de.hdm.cefx.CEFXController;
 import de.hdm.cefx.CEFXControllerImpl;
 import de.hdm.cefx.awareness.AwarenessControllerImpl;
 import de.hdm.cefx.awareness.AwarenessEvent;
 import de.hdm.cefx.awareness.CEFXtoMobilisHub;
 import de.hdm.cefx.client.net.NetworkController;
 import de.hdm.cefx.client.net.NetworkControllerImpl;
 import de.hdm.cefx.concurrency.AbstractConcurrencyControllerImpl;
 import de.hdm.cefx.concurrency.OrderingConcurrencyControllerImpl;
 import de.hdm.cefx.concurrency.operations.NodePosition;
 import de.hdm.cefx.dom.adapter.CEFXDOMAdapter;
 import de.hdm.cefx.dom.adapter.CEFXDOMAdapterImpl;
 import de.hdm.cefx.exceptions.NodeNotFoundException;
 import de.hdm.cefx.server.ServerObject;
 import de.hdm.cefx.util.CEFXUtil;
 import de.hdm.cefx.util.XMLHelper;
 import de.tudresden.inf.rn.mobilis.media.parcelables.ParcelableAwarenessEvent;
 import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
 import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
 import de.tudresden.inf.rn.mobilis.mxa.XMPPRemoteService;
 import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
 import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.ICollabEditingCallback;
 import de.tudresden.inf.rn.mobilis.xmpp.android.Parceller;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.collabedit.AwarenessEventBean;
 import de.tudresden.inf.rn.mobilis.xmpp.packet.MonitoringIQ;
 
 /**
  * This service handles all stuff related to Collaborative Editing sessions. In
  * addition to taking care of the connection between clients and the server it
  * handles the access to the local Collaborative Editing Framework.
  * 
  * @author Sven Bendel
  * @author Dirk Hering
  */
 public class CollabEditingService extends Service implements CEFXtoMobilisHub {
 	private static final String TAG = "CollabEditingService";
 
 	final static String server = "mobilis@sven-ubuntu-big/CollabEditing";
 
 	private boolean DEBUG = false;
 	private Document document;
 	
 	private CEFXDOMAdapterImpl da;
 	private CEFXControllerImpl cefx;
 	private AwarenessControllerImpl ac;
 	private DocumentFactory docFac;
 	private OrderingConcurrencyControllerImpl cc;
 	private boolean readyForEditing;
 	private Messenger mConnectMessenger;
 	private IXMPPService xmppService;
 	private XMPPRemoteService xmppRemoteService;
 	
 	private List<ICollabEditingCallback> collabEditingCallbacks = new ArrayList<ICollabEditingCallback>(); 
 	
 	boolean connected = false;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent i) {
 		return mBinder;
 	}
 	
 	@Deprecated
 	private Handler ackHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			String toast = "ack: ";
 			
 			switch (msg.what) {
 			case ConstMXA.MSG_SEND_MESSAGE:
 				toast += "message ";
 				break;
 			case ConstMXA.MSG_SEND_IQ:
 				toast += "iq ";
 				break;
 			case ConstMXA.MSG_SEND_PRESENCE:
 				toast += "presence ";
 				break;
 			case ConstMXA.MSG_SEND_FILE:
 				toast += "file ";
 				break;
 			}
 			switch (msg.arg1) {
 			case ConstMXA.MSG_STATUS_DELIVERED:
 				toast += "delivered";
 				break;
 			case ConstMXA.MSG_STATUS_ERROR:
 				toast += "error";
 				break;
 			case ConstMXA.MSG_STATUS_REQUEST:
 				toast += "request";
 				break;
 			case ConstMXA.MSG_STATUS_SUCCESS:
 				toast += "success";
 				break;
 			case ConstMXA.MSG_STATUS_IQ_ERROR:
 				toast += "iq_error";
 				break;
 			case ConstMXA.MSG_STATUS_IQ_RESULT:
 				toast += "iq_result";
 				break;
 			}
 			Log.i(TAG, "Ack received. " + toast);
 			
 		}
 	};
 	
 	public CollabEditingService(XMPPRemoteService xmppRemoteService) {
 		this.xmppRemoteService = xmppRemoteService;
 		xmppService = xmppRemoteService.getXMPPService();
 		readyForEditing = false;
 		da = new CEFXDOMAdapterImpl();
 		docFac = new DocumentFactory();
 		da.setDocumentFactory(docFac);
 		document = null;
 		// xmlDoc=da.createDocument(uri);
 		cefx = (CEFXControllerImpl) da.getCEFXController();
 		// cefx.getClient().setID(id);
 		ac = new AwarenessControllerImpl();
 		cefx.setAwarenessController(ac);
 		ac.setCollabEditingService(this);
 		NetworkControllerImpl nc = new NetworkControllerImpl();
 		cefx.setNetworkController(nc);
 		cc = new OrderingConcurrencyControllerImpl();
 		cefx.setConcurrencyController(cc);
 		// registerBeanPrototypes();
 		// connectToServiceAndServer();
 		connectToXMPPViaCEFX();
 	}
 	
 	/**
 	 * @return the currently used {@link CEFXDOMAdapter}
 	 */
 	public CEFXDOMAdapter getDOMAdapter() {
 		return da;
 	}
 	
 	/**
 	 * @return the currently used {@link CEFXController}
 	 */
 	public CEFXController getCEFXController() {
 		return cefx;
 	}
 	
 	/**
 	 * This method connects the client part of CEFX with
 	 * the CEFX server via XMPP. It reuses the XMPP connection
 	 * from MXA.
 	 * 
 	 * @return
 	 * 			<code>true</code> if the connection could be established
 	 * 			successfully, <code>false</code> otherwise
 	 */
 	private boolean connectToXMPPViaCEFX() {
 		// read bundled connection params
 		// Bundle connectionParams =
 		// XMPPController.getInstance().readXMPPPreferences();
 		Bundle connectionParams = null;
 		try {
 			connectionParams = xmppService.getXMPPConnectionParameters();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (connectionParams == null) {
 			return false;
 		}
 
 		String host = connectionParams.getString("xmpp_host");
 		int port = connectionParams.getInt("xmpp_port", 5222);
 		String serviceName = connectionParams.getString("xmpp_service");
 		String username = connectionParams.getString("xmpp_user");
 		String password = connectionParams.getString("xmpp_password");
 		String resource = connectionParams.getString("xmpp_resource");
 
 		cefx.setJabberHost(host);
 		cefx.setJabberPort(port);
 		cefx.setJabberServiceName(serviceName);
 		cefx.setJabberUserName(username);
 		cefx.setJabberPassword(password);
 		cefx.setJabberResource(resource);
 		cefx.setJabberResourceSuffix("");
 
 		// Log.i(TAG, "Connecting to XMPP with: " + username + "@" + host + ":"
 		// + port + "/" + resource + " service: " + serviceName);
 
 		cefx.init();
 		JabberClient.getInstance().setUsesProvidedConnection(true);
 		JabberClient.getInstance().setProvidedXMPPConnection(
 				xmppRemoteService.getXMPPConnection());
 		cefx.jabberConnect();
 
 		NetworkController nc = cefx.getNetworkController();
 		if (nc != null) {
 			cefx.getConcurrencyController().setSession(nc.getSession());
 		}
 
 		XMPPConnection xmpp = JabberClient.getInstance().getJabberConnection();
 		xmpp.addPacketListener(Monitoring.get().getMonitoringIQHandler(),
 				new AndFilter(new IQTypeFilter(IQ.Type.GET),
 						new PacketTypeFilter(MonitoringIQ.class)));
 
 		String server = "mobilis@sven-ubuntu-big/CollabEditing";
 		Log.i(TAG, "Establishing connection to CEFX-Server: " + server);			
 		cefx.connect(server);
 		
 		// TODO: add more sophisticated connected-indication
 		connected = true;
 		
 		return connected;
 	}
 	
 	/**
 	 * This is just a workaround method for AwarenessControllerImpl which resends an
 	 * incoming AwarenessEvent to us again so that the attached apps may receive an
 	 * proper AwarenessEventBean. This method may be removed when CEFX is fully converted
 	 * to the Mobilis Beans communication model.
 	 * @param event
 	 */
 	@Deprecated
 	public void sendCEFXAwarenessEventToMyself(AwarenessEvent event)	{
 		AwarenessEventBean b = new AwarenessEventBean();
 		
 		b.setEvent(event);
 //		b.setEventDescription(event.getDescription());
 //		b.setEventSource(event.getEventSource());
 //		b.setEventType(event.getType());
 		String userName = null;
 		try {
 			userName = xmppService.getUsername();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		b.setFrom(userName);
 		b.setTo(userName);
 		
 		try {
 			xmppService.sendIQ(new Messenger(new Handler(Looper.getMainLooper())), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(b, true));
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void onAwarenessEventReceived(AwarenessEvent event) {
 		for (ICollabEditingCallback callback : collabEditingCallbacks) {
 			try {
 				callback.onAwarenessEventReceived(new ParcelableAwarenessEvent(event));
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * @return a List of the XMPP IDs of all occupants in the currently used
 	 * MUC room
 	 * @throws XMPPException
 	 */
 	public List<String> getRoomOccupants() throws XMPPException {
 		List<String> occupants = new ArrayList<String>();
 	    ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(xmppRemoteService.getXMPPConnection());
 	    DiscoverItems items = discoManager.discoverItems(getMucRoomName());
 	    for (Iterator<DiscoverItems.Item> it = items.getItems(); it.hasNext();) {
 	    	DiscoverItems.Item item = (DiscoverItems.Item) it.next();
 	        String occupant = item.getEntityID();
 	        occupants.add(occupant);
 	    }
 	    return occupants;
 	}
 	
 	/**
 	 * @return our user name in the current MUC session (this is *not* our global XMPP ID!)
 	 */
 	public String getMucUserName() {
 		return getMucRoomName() + "/" + JabberClient.getInstance().getUserName() + "_" + cefx.getIdentifier(); 
 	}
 
 	/**
 	 * @return the name of the currently used MUC room
 	 */
 	private String getMucRoomName() {
 		return cefx.getNetworkController().getSession().getMucRoomName();
 	}
 	
 	private final ICollabEditingService.Stub mBinder = new ICollabEditingService.Stub() {
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#isReadyForEditing()
 		 */
 		public boolean isReadyForEditing() {
 			return readyForEditing;
 		}
 		
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#isConnected()
 		 */
 		public boolean isConnected() {
 			return connected;
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#insertText(java.lang.String, java.lang.String, int, java.lang.String, int)
 		 */
 		public void insertText(String parentNodeID, String fixNodeID, int before,
 				String text, int textPos) {
 			Element parentNode = getElementForId(parentNodeID);
 			Element fixNode = getElementForId(fixNodeID);
 			da.Element_TextInsert(parentNode, fixNode, before, text, textPos);
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#deleteText(java.lang.String, java.lang.String, int, int, int)
 		 */
 		public void deleteText(String parentNodeID, String fixNodeID, int before,
 				int pos, int len) {
 			if (len != 0) {
 				Element parentNode = getElementForId(parentNodeID);
 				Element fixNode = getElementForId(fixNodeID);
 				da.Element_TextDelete(parentNode, fixNode, before, pos, len);
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#setText(java.lang.String, java.lang.String, int, java.lang.String)
 		 */
 		public void setText(String parentNodeID, String fixNodeID, int before,
 				String text) {
 			Element parentNode = getElementForId(parentNodeID);
 			Element fixNode = getElementForId(fixNodeID);
 			da.Element_TextSet(parentNode, fixNode, before, text);
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#deleteNode(java.lang.String)
 		 */
 		public void deleteNode(String id) {
 			Element f;
 			try {
 				((AbstractConcurrencyControllerImpl) cefx
 						.getConcurrencyController()).refreshNodeMap();
 				f = (Element) (((AbstractConcurrencyControllerImpl) cefx
 						.getConcurrencyController()).getNodeForId(id));
 				da.Node_removeChild(f);
 			} catch (NodeNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#uploadDocument(java.lang.String)
 		 */
 		public boolean uploadDocument(String filePath) {
 			return cefx.uploadDocument(filePath);
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#getElementForId(java.lang.String)
 		 */
 		public Element getElementForId(String id) {
 			try {
 				return (Element) cc.getNodeForId(id);
 			} catch (NodeNotFoundException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#showDocument()
 		 */
 		public void showDocument() {
 			System.out.println("Current document:");
 			XMLHelper.showDocumentContent(da.getDocument());
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#showStateVector()
 		 */
 		public void showStateVector() {
 			Log.i(TAG, "Client " + cefx.getClient().getID() + " - State Vector: "
 					+ cefx.getConcurrencyController().getStateVector().toString());
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#getDocumentString()
 		 */
 		public String getDocumentString() {
 			return XMLHelper.getDocumentString(da.getDocument(), true);
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#loadDocumentFromServer(java.lang.String)
 		 */
 		public boolean loadDocumentFromServer(String uri) {
 			Vector<ServerObject> files = cefx.listFiles();
 			if (files != null) {
 				int docID = cefx.getDocumentID(uri, files);
 				cefx.openDocument(docID);
 				document = da.getDocument();
 				return true;
 			} else {
 				Log.e(TAG, uri + " could not be found on the server");
 				return false;
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#joinSession(java.lang.String)
 		 */
 		public boolean joinSession(String sessionName) {
 			if (cefx.joinSession(sessionName).booleanValue()) {
 				document = da.getDocument();
 				readyForEditing = true;
 				Log.i(TAG, "Successfully joined collaboration session "
 						+ sessionName);
 				return true;
 			} else {
 				Log.e(TAG, "Could not join session: " + sessionName);
 				return false;
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#createElement(java.lang.String)
 		 */
 		public String createElement(String name) {
 			// TODO: assign CEFX-UUID to this element by automatically creating and executing an operation
 			return document.createElement(name).getAttributeNodeNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID).getValue();
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#createElementNS(java.lang.String, java.lang.String)
 		 */
 		public String createElementNS(String namespaceURI, String name) {
 			// TODO: assign CEFX-UUID to this element by automatically creating and executing an operation
 			return document.createElementNS(namespaceURI, name).getAttributeNodeNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID).getValue();
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#createTextNode(java.lang.String)
 		 */
 		public String createTextNode(String textContent) {
 			// TODO: assign CEFX-UUID to this element by automatically creating and executing an operation
 			return document.createTextNode(textContent).getAttributes().getNamedItemNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID).getNodeValue();
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#insertNode(java.lang.String, org.w3c.dom.Node, java.lang.String, int)
 		 */
 		public void insertTextNode(String parentNodeID, String content, String fixNodeID,
 				int before) {
 			// TODO: is this method necessary or does insertText the same?
 			Element parentNode = getElementForId(parentNodeID);
 
 			// will throw warning if fixNodeID is null, but that's okay anyway
 			Element fixNode = getElementForId(fixNodeID);
 			Text newNode =  document.createTextNode(content);
 			// TODO necessary to check if the new node is an attribute / ...?
 			da.Node_insert(parentNode, newNode, fixNode, before);
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#replaceText(java.lang.String, java.lang.String, int, java.lang.String, int, int)
 		 */
 		public void replaceText(String parentNodeID, String fixNodeID, int before,
 				String text, int pos, int len) {
 			this.deleteText(parentNodeID, fixNodeID, NodePosition.INSERT_BEFORE, pos, len);
 			this.insertText(parentNodeID, fixNodeID, NodePosition.INSERT_BEFORE, text, pos);
 		}
 
 		/* (non-Javadoc)
 		 * @see de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService#leaveSession(java.lang.String)
 		 */
 		public boolean leaveSession(String sessionName) {
 			if (cefx.leaveSession(sessionName).booleanValue()) {
 				document = null;
 				readyForEditing = false;
 				return true;
 			} else {
 				Log.e(TAG, "Could not disconnect from session: " + sessionName);
 				return false;
 			}
 		}
 		
 		public String getCEFXIDForName(String wmpId) {
 			return getDOMAdapter().getDocument().getElementsByTagName(wmpId).item(0).getAttributes().getNamedItem(CEFXUtil.CEFXUID).getNodeValue();
 		}
 		
 		public String getUsername() throws RemoteException {
 			return xmppService.getUsername();
 		}
 		
 		public String getMucRoomName() {
 			return cefx.getNetworkController().getSession().getMucRoomName();
 		}
 		
 		public int getCEFXUserID() {
 			return getCEFXController().getIdentifier();
 		}
 		
 		public void fireAndForgetIQ(XMPPIQ iq) throws RemoteException {
 			sendIQ(new Messenger(new Handler(Looper.getMainLooper())), null, 1, iq);
 		}
 		
 		@Override
 		public void sendIQ(Messenger acknowledgement, Messenger result,
 				int requestCode, XMPPIQ iq) throws RemoteException {
 			xmppService.sendIQ(acknowledgement, result, requestCode, iq);
 		}
 		
 		public void fireAndForgetMUCIQ(XMPPIQ iq) throws RemoteException {
 			List<String> occupants = null;
 			try {
 				occupants = getRoomOccupants(); getCEFXController().getIdentifier();
 				String mucUserName = getMucUserName();
 				for (String occupant : occupants) {
 					if (!mucUserName.equals(occupant)){
 						iq.to = occupant;
 						Log.v(TAG, "member to send MUC-IQ to:" + iq.to);
 						fireAndForgetIQ(iq);
 					}
 				}
 			} catch (XMPPException e) {
 				e.printStackTrace();
 				// try again
 				fireAndForgetMUCIQ(iq);
 			}
 		}
 		
 		public void registerCollabEditingCallback(ICollabEditingCallback callback) throws RemoteException {
 			if (!collabEditingCallbacks.contains(callback)) {
 				collabEditingCallbacks.add(callback);
 			}
 		}
 
 		@Override
 		public void deregisterCollabEditingCallback(
 				ICollabEditingCallback callback) throws RemoteException {
			collabEditingCallbacks.remove(callback);
 		}
 		
 	};
 
 }
