 /*******************************************************************************
  * Copyright (C) 2010 Technische Universität Dresden
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 	http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Dresden, University of Technology, Faculty of Computer Science
  * Computer Networks Group: http://www.rn.inf.tu-dresden.de
  * mobilis project: http://mobilisplatform.sourceforge.net
  ******************************************************************************/
 package de.tudresden.inf.rn.mobilis.friendfinder.proxy;
 
 import java.io.StringReader;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.FriendFinderProxy;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.IFriendFinderOutgoing;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.IXMPPCallback;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.IsTrackAvailableRequest;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.IsTrackAvailableResponse;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.JoinServiceRequest;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.JoinServiceResponse;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.LeaveServiceRequest;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.LeaveServiceResponse;
 import de.tudresden.inf.rn.mobilis.friendfinder.service.BackgroundService;
 import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
 import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.CreateNewServiceInstanceBean;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.SendNewServiceInstanceBean;
 
 import android.os.RemoteException;
 import android.util.Log;
 
 /**
  * The Class IQProxy is a wrapper to simpify the IQ handling.
  */
 public class IQProxy {
 
 	/** The Constant TAG for logging. */
 	public static final String TAG = "IQProxy";
 
 	/** The MXAProxy for XMPP communication. */
 	private MXAProxy mMXAProxy;
 
 	/** The jid of the current MobilisXHunt service (game on server side). */
 	private String mGameServiceJid = "mruntime1@philipp-pc";
 
 	/** The descriptive name of the MobilisXHunt service. */
 	private String mGameName;
 
 	/** The jid of the Coordinator of the Mobilis Server. */
 	private String mServerCoordinatorJid = "mruntime1@philipp-pc/Coordinator";
 
 	/** The local XHuntService used as application controller. */
 	private BackgroundService mService;
 
 	/**
 	 * The bean prototypes contains templates for each IQ used in this
 	 * application.
 	 * 
 	 * @author Benjamin Söllner
 	 */
 	private Map<String, Map<String, XMPPBean>> beanPrototypes = Collections
 			.synchronizedMap(new HashMap<String, Map<String, XMPPBean>>());
 
 	private FriendFinderProxy _proxy;
 
 	private int _usedServiceVersion = 1;
 
 	private Map<String, IXMPPCallback<? extends XMPPBean>> _waitingCallbacks = new HashMap<String, IXMPPCallback<? extends XMPPBean>>();
 
 	/**
 	 * Instantiates a new IQProxy.
 	 * 
 	 * @param mxaProxy
 	 *            the MXAProxy for XMPP communication
 	 * @param xhuntService
 	 *            the XHuntService as application controller
 	 */
 	public IQProxy(MXAProxy mxaProxy, BackgroundService service) {
 		this.mMXAProxy = mxaProxy;
 		mService = service;
 		_proxy = new FriendFinderProxy(_proxyOutgoingMapper);
 
 		registerBeanPrototypes();
 	}
 
 	private IFriendFinderOutgoing _proxyOutgoingMapper = new IFriendFinderOutgoing() {
 
 		@Override
 		public void sendXMPPBean(XMPPBean out) {
 			mMXAProxy.sendIQ(beanToIQ(out, true));
 		}
 
 		@Override
 		public void sendXMPPBean(XMPPBean out,
 				IXMPPCallback<? extends XMPPBean> callback) {
 			_waitingCallbacks.put(out.getId(), callback);
 			mMXAProxy.sendIQ(beanToIQ(out, true));
 		}
 	};
 
 	/**
 	 * Convert an XMPPBean to an XMPPIQ to send it via the MXAProxy/MXA.
 	 * 
 	 * @param bean
 	 *            the bean to convert
 	 * @param mergePayload
 	 *            true if the playload should be merged
 	 * @return the XMPPIQ
 	 */
 	public XMPPIQ beanToIQ(XMPPBean bean, boolean mergePayload) {
 		// default XMPP IQ type
 		int type = XMPPIQ.TYPE_GET;
 
 		switch (bean.getType()) {
 		case XMPPBean.TYPE_GET:
 			type = XMPPIQ.TYPE_GET;
 			break;
 		case XMPPBean.TYPE_SET:
 			type = XMPPIQ.TYPE_SET;
 			break;
 		case XMPPBean.TYPE_RESULT:
 			type = XMPPIQ.TYPE_RESULT;
 			break;
 		case XMPPBean.TYPE_ERROR:
 			type = XMPPIQ.TYPE_ERROR;
 			break;
 		}
 
 		XMPPIQ iq;
 
 		if (mergePayload)
 			iq = new XMPPIQ(bean.getFrom(), bean.getTo(), type, null, null,
 					bean.toXML());
 		else
 			iq = new XMPPIQ(bean.getFrom(), bean.getTo(), type,
 					bean.getChildElement(), bean.getNamespace(),
 					bean.payloadToXML());
 
 		iq.packetID = bean.getId();
 
 		return iq;
 	}
 
 	/**
 	 * Formats a Bean to a string.
 	 * 
 	 * @param bean
 	 *            the bean
 	 * @return the formatted string of the Bean
 	 */
 	public String beanToString(XMPPBean bean) {
 		String str = "XMPPBean: [NS=" + bean.getNamespace() + " id="
 				+ bean.getId() + " from=" + bean.getFrom() + " to="
 				+ bean.getTo() + " type=" + bean.getType() + " payload="
 				+ bean.payloadToXML();
 
 		if (bean.errorCondition != null)
 			str += " errorCondition=" + bean.errorCondition;
 		if (bean.errorText != null)
 			str += " errorText=" + bean.errorText;
 		if (bean.errorType != null)
 			str += " errorType=" + bean.errorType;
 
 		str += "]";
 
 		return str;
 	}
 
 	/**
 	 * Convert XMPPIQ to XMPPBean to simplify the handling of the IQ using the
 	 * beanPrototypes.
 	 * 
 	 * @param iq
 	 *            the XMPPIQ
 	 * @return the related XMPPBean or null if something goes wrong
 	 */
 	public XMPPBean convertXMPPIQToBean(XMPPIQ iq) {
 
 		try {
 			String childElement = iq.element;
 			String namespace = iq.namespace;
 			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 			XmlPullParser parser = factory.newPullParser();
 			parser.setInput(new StringReader(iq.payload));
 			XMPPBean bean = null;
 
 			Log.v(TAG, "prototypes contains ns: " + namespace + "? "
 					+ this.beanPrototypes.containsKey(namespace));
 			if (this.beanPrototypes.containsKey(namespace))
 				Log.v(TAG,
 						"prototypes contains ce: "
 								+ childElement
 								+ "? "
 								+ this.beanPrototypes.get(namespace)
 										.containsKey(childElement));
 
 			synchronized (this.beanPrototypes) {
 				if (namespace != null
 						&& this.beanPrototypes.containsKey(namespace)
 						&& this.beanPrototypes.get(namespace).containsKey(
 								childElement)) {
 
 					bean = (this.beanPrototypes.get(namespace)
 							.get(childElement)).clone();
 					bean.fromXML(parser);
 
 					bean.setId(iq.packetID);
 					bean.setFrom(iq.from);
 					bean.setTo(iq.to);
 
 					switch (iq.type) {
 					case XMPPIQ.TYPE_GET:
 						bean.setType(XMPPBean.TYPE_GET);
 						break;
 					case XMPPIQ.TYPE_SET:
 						bean.setType(XMPPBean.TYPE_SET);
 						break;
 					case XMPPIQ.TYPE_RESULT:
 						bean.setType(XMPPBean.TYPE_RESULT);
 						break;
 					case XMPPIQ.TYPE_ERROR:
 						bean.setType(XMPPBean.TYPE_ERROR);
 						break;
 					}
 
 					return bean;
 				}
 			}
 		} catch (Exception e) {
 			Log.e(TAG,
 					"ERROR while parsing XMPPIQ to XMPPBean: " + e.getMessage());
 		}
 
 		return null;
 	}
 
 	/**
 	 * Convert XMPPIQ to a formatted string.
 	 * 
 	 * @param iq
 	 *            the XMPPIQ
 	 * @return the formatted string of the XMPPIQ
 	 */
 	public String convertXMPPIQToString(XMPPIQ iq) {
 		return "id=" + iq.packetID + " ns=" + iq.namespace + " type=" + iq.type
 				+ " from=" + iq.from + " to=" + iq.to + " payload="
 				+ iq.payload;
 	}
 
 	public FriendFinderProxy getProxy() {
 		return _proxy;
 	}
 
 	/**
 	 * Get a registered prototype XMPPBean by namespace and element tags.
 	 * 
 	 * @param namespace
 	 *            the namespace of the XMPPBean
 	 * @param element
 	 *            the element of hte XMPPBEan
 	 * @return the registered XMPPBean or null if nothing was matched
 	 */
 	public XMPPBean getRegisteredBean(String namespace, String element) {
 		try {
 			return this.beanPrototypes.get(namespace).get(element);
 		} catch (NullPointerException e) {
 			Log.e(TAG, "Cannot find namespace '" + namespace
 					+ "' in list of bean prototypes!");
 
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the jid of the Mobilis server Coordinator.
 	 * 
 	 * @return the coordinators jid
 	 */
 	public String getServerCoordinatorJid() {
 		return mServerCoordinatorJid;
 	}
 
 	/**
 	 * Gets the jid of the MobilisXHunt service (game service).
 	 * 
 	 * @return the jid of the game service
 	 */
 	public String getGameServiceJid() {
 		return mGameServiceJid;
 	}
 
 	/**
 	 * Gets the descriptive name of the game service.
 	 * 
 	 * @return the name of the game service
 	 */
 	public String getGameName() {
 		return mGameName;
 	}
 
 	/**
 	 * Sets the descriptive name of the game service.
 	 * 
 	 * @param gameName
 	 *            the name of the game service
 	 */
 	public void setGameName(String gameName) {
 		this.mGameName = gameName;
 	}
 
 	/**
 	 * Register XMPPBean prototypes used in this application to communicate with
 	 * the MobilisXHunt service.
 	 */
 	public void registerBeanPrototypes() {
 		// todo: register self-defined Beans
 
 		registerXMPPBean(new CreateNewServiceInstanceBean());
 		registerXMPPBean(new MobilisServiceDiscoveryBean());
 		registerXMPPBean(new SendNewServiceInstanceBean());
 		
 		registerXMPPBean(new JoinServiceRequest());
 		registerXMPPBean(new JoinServiceResponse());
 		
 		registerXMPPBean(new LeaveServiceRequest());
 		registerXMPPBean(new LeaveServiceResponse());
 		
 		registerXMPPBean(new IsTrackAvailableRequest());
 		registerXMPPBean(new IsTrackAvailableResponse());
 	}
 
 	/**
 	 * Register a global callback(AbstractCallback) which will be notified if an
 	 * IQ related to the registered XMPPBeans is incoming in MXAProxy/MXA.
 	 */
 	public void registerCallbacks() {
 		if (mMXAProxy.isConnected()) {
 			try {
 
 				for (Map.Entry<String, Map<String, XMPPBean>> entity : this.beanPrototypes
 						.entrySet()) {
 					for (Map.Entry<String, XMPPBean> subEntity : entity
 							.getValue().entrySet()) {
 						registerXMPPExtension(AbstractCallback, subEntity
 								.getValue().getNamespace(), subEntity
 								.getValue().getChildElement());
 					}
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Register a prototype of an XMPPBean.
 	 * 
 	 * @param prototype
 	 *            the prototype XMPPBean
 	 */
 	public void registerXMPPBean(XMPPBean prototype) {
 		String namespace = prototype.getNamespace();
 		String childElement = prototype.getChildElement();
 
 		synchronized (this.beanPrototypes) {
 			if (!this.beanPrototypes.keySet().contains(namespace))
 				this.beanPrototypes.put(namespace, Collections
 						.synchronizedMap(new HashMap<String, XMPPBean>()));
 
 			this.beanPrototypes.get(namespace).put(childElement, prototype);
 		}
 	}
 
 	/**
 	 * Register an callback extension which is used by the MXAProxy/MXA for
 	 * listening for this kind of IQ.
 	 * 
 	 * @param callback
 	 *            the callback which should be notified if IQ is incoming
 	 * @param beanNamespace
 	 *            the namespace of the IQ
 	 * @param beanElement
 	 *            the root element tag of the IQ
 	 * @return true, if callback was registered successful
 	 * @throws RemoteException
 	 *             the remote exception if something goes wrong
 	 */
 	public boolean registerXMPPExtension(IXMPPIQCallback callback,
 			String beanNamespace, String beanElement) throws RemoteException {
 		boolean isRegistered = false;
 
 		if (mMXAProxy.isConnected()) {
 			XMPPBean bean = getRegisteredBean(beanNamespace, beanElement);
 
 			// if there is no related prototype of this XMPPBean it can not be
 			// registered a callback
 			if (bean != null) {
 				mMXAProxy.getXMPPService().registerIQCallback(callback,
 						bean.getChildElement(), bean.getNamespace());
 
 				Log.v(TAG,
 						"child: " + bean.getChildElement() + " ns: "
 								+ bean.getNamespace());
 
 				isRegistered = true;
 			}
 		}
 
 		return isRegistered;
 	}
 
 	/**
 	 * Sends a CreateNewServiceInstanceBean to the current Mobilis Coordinator
 	 * service.
 	 * 
 	 * IQ parameters: Type: SET From: players jid To: Mobilis Coordinator
 	 * service
 	 * 
 	 * @param serviceNamespace
 	 *            the service namespace for to create
 	 * @param serviceName
 	 *            the service name
 	 * @param servicePassword
 	 *            the service password
 	 */
 	public void sendCreateNewServiceInstanceIQ(String serviceNamespace,
 			String serviceName, String servicePassword) {
 		CreateNewServiceInstanceBean bean = new CreateNewServiceInstanceBean(
 				serviceNamespace, servicePassword);
 		bean.setServiceName(serviceName);
 
 		bean.setType(XMPPBean.TYPE_SET);
 		bean.setFrom(mMXAProxy.getXmppJid());
 		bean.setTo(mServerCoordinatorJid);
 		mMXAProxy.sendIQ(beanToIQ(bean, true));
 
 		Log.v("IQProxy", "CreateNewServiceInstanceBean send");
 	}
 	
	public void AnswerSendNewServiceInstance(SendNewServiceInstanceBean inBean){
 		SendNewServiceInstanceBean bean = new SendNewServiceInstanceBean();
 		bean.setType(XMPPBean.TYPE_RESULT);
		bean.setId(inBean.getId());
 		bean.setFrom(mMXAProxy.getXmppJid());
 		bean.setTo(mServerCoordinatorJid);
 		mMXAProxy.sendIQ(beanToIQ(bean, true));
 
 		Log.v("IQProxy", "SendNewServiceInstanceBean send");
 	}
 
 	/**
 	 * Sends a MobilisServiceDiscoveryBean to the an MobilisXHunt game service.
 	 * 
 	 * IQ parameters: Type: GET From: players jid To: Mobilis Coordinator
 	 * service
 	 * 
 	 * @param serviceNamespace
 	 *            the namespace of a service to discover amount of instances or
 	 *            null to discover all services running on the Mobilis server
 	 */
 	public void sendServiceDiscoveryIQ(String serviceNamespace) {
 		MobilisServiceDiscoveryBean bean = new MobilisServiceDiscoveryBean(
 				serviceNamespace, Integer.MIN_VALUE, false);
 
 		bean.setType(XMPPBean.TYPE_GET);
 		bean.setFrom(mMXAProxy.getXmppJid());
 		bean.setTo(mServerCoordinatorJid);
 		mMXAProxy.sendIQ(beanToIQ(bean, true));
 
 		Log.v("IQProxy", "MobilisServiceDiscoveryBean send");
 	}
 
 	/**
 	 * Sets the jid of the Mobilis XHunt service (game service).
 	 * 
 	 * @param mServerJid
 	 *            the new jid of the XHUnt service
 	 */
 	public void setGameServiceJid(String mServerJid) {
 		this.mGameServiceJid = mServerJid;
 	}
 
 	public void setServiceVersion(int version) {
 		this._usedServiceVersion = version;
 	}
 
 	public int getServiceVersion() {
 		return _usedServiceVersion;
 	}
 
 	/**
 	 * Unregister an XMPPBean from prototypes.
 	 * 
 	 * @param prototype
 	 *            the prototype to remove
 	 */
 	public void unregisterXMPPBean(XMPPBean prototype) {
 		String namespace = prototype.getNamespace();
 		String childElement = prototype.getChildElement();
 
 		synchronized (this.beanPrototypes) {
 			if (this.beanPrototypes.containsKey(namespace)) {
 				this.beanPrototypes.get(namespace).remove(childElement);
 
 				if (this.beanPrototypes.get(namespace).size() > 0)
 					this.beanPrototypes.remove(namespace);
 			}
 		}
 	}
 
 	/**
 	 * Send an XMPPBean of type ERROR.
 	 * 
 	 * TODO: Option for custom ERROR tag and text is missing.
 	 * 
 	 * @param inBean
 	 *            the XMPPBean to reply with an ERROR. The playload will be
 	 *            copied.
 	 */
 	public void sendXMPPBeanError(XMPPBean inBean) {
 		XMPPBean resultBean = inBean.clone();
 		resultBean.setTo(inBean.getFrom());
 		resultBean.setFrom(mMXAProxy.getXmppJid());
 		resultBean.setType(XMPPBean.TYPE_ERROR);
 
 		_proxy.getBindingStub().sendXMPPBean(resultBean);
 	}
 
 	/**
 	 * Unregister the global callback of all registered XMPPBeans in MXA. Now,
 	 * MXA will no more refer the IQs to this application and answers with an
 	 * 'Not supported' to the requester.
 	 */
 	public void unregisterCallbacks() {
 		if (mMXAProxy.isConnected()) {
 			try {
 				unregisterXMPPExtension(AbstractCallback,
 						MobilisServiceDiscoveryBean.NAMESPACE,
 						MobilisServiceDiscoveryBean.CHILD_ELEMENT);
 
 				unregisterXMPPExtension(AbstractCallback,
 						CreateNewServiceInstanceBean.NAMESPACE,
 						CreateNewServiceInstanceBean.CHILD_ELEMENT);
 				
 				unregisterXMPPExtension(AbstractCallback,
 						SendNewServiceInstanceBean.NAMESPACE,
 						SendNewServiceInstanceBean.CHILD_ELEMENT);
 
 				for (Map.Entry<String, Map<String, XMPPBean>> entity : this.beanPrototypes
 						.entrySet()) {
 					for (Map.Entry<String, XMPPBean> subEntity : entity
 							.getValue().entrySet()) {
 						unregisterXMPPExtension(AbstractCallback, subEntity
 								.getValue().getNamespace(), subEntity
 								.getValue().getChildElement());
 					}
 				}
 			} catch (RemoteException e) {
 				Log.e(TAG, e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Unregister a single XMPPBean in MXA.
 	 * 
 	 * @param callback
 	 *            the callback to be unregistered
 	 * @param beanNamespace
 	 *            the namespace of the IQ
 	 * @param beanElement
 	 *            the root element of the IQs payload
 	 * @throws RemoteException
 	 *             the remote exception if something goes wrong
 	 */
 	public void unregisterXMPPExtension(IXMPPIQCallback callback,
 			String beanNamespace, String beanElement) throws RemoteException {
 		if (mMXAProxy.isConnected()) {
 			XMPPBean bean = getRegisteredBean(beanNamespace, beanElement);
 
 			if (bean != null)
 				mMXAProxy.getXMPPService().unregisterIQCallback(callback,
 						bean.getChildElement(), bean.getNamespace());
 		}
 	}
 
 	/**
 	 * Updates the server jid from shared preferences (can be modified by user
 	 * in SettingsActivity).
 	 */
 	/*
 	 * public void updateServerJid(){ String serverJid =
 	 * mService.getSharedPrefHelper().getValue(mService.getResources()
 	 * .getString(R.string.bundle_key_settings_serverjid));
 	 * 
 	 * if(serverJid == null) serverJid =
 	 * mService.getResources().getString(R.string.default_jid_server);
 	 * 
 	 * if(!serverJid.equals(mServerCoordinatorJid)) { Log.v(TAG,
 	 * "ServerJID changed from " + mServerCoordinatorJid + " to " + serverJid);
 	 * mServerCoordinatorJid = serverJid; } }
 	 */
 
 	/**
 	 * The Abstract callback to receive each IQ registered by an XMPPBean
 	 * prototype. the incoming IQ will be converted to an XMPPBean and referred
 	 * to the current running GameState inside the specific Activity.
 	 */
 	private IXMPPIQCallback AbstractCallback = new IXMPPIQCallback.Stub() {
 
 		@SuppressWarnings({ "rawtypes", "unchecked" })
 		@Override
 		public void processIQ(XMPPIQ iq) throws RemoteException {
 
 //			if (!(mGameServiceJid.equals(iq.from.substring(0, mGameServiceJid.length())) || iq.from
 //					.equals(mServerCoordinatorJid))) {
 //				String msg = "Discarded IQ from unknown JID " + iq.from
 //						+ " to prevent GameService zombies from interfering"
 //						+ " - see IQProxy.AbstractCallback.processIQ()";
 //				Log.w(TAG, msg);
 //				return;
 //			}
 
 			Log.v(TAG, "AbstractCallback: ID: " + iq.packetID + " type: "
 					+ iq.type + " ns: " + iq.namespace + " payload: "
 					+ iq.payload);
 
 			XMPPBean inBean = convertXMPPIQToBean(iq);
 
 			Log.v(TAG, "Converted Abstract Bean: " + beanToString(inBean));
 
 			if (_waitingCallbacks.containsKey(inBean.getId())) {
 				IXMPPCallback callback = _waitingCallbacks.get(inBean.getId());
 
 				if (null != callback) {
 					try {
 						callback.invoke(inBean);
 					} catch (ClassCastException e) {
 						e.printStackTrace();
 					}
 				}
 			} else {
 				mService.getCallbackClass().processPacket(inBean);
 			}
 		}
 	};
 
 }
