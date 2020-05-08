 /**
  * Copyright (C) 2011 / cube-team <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.freedesktop;
 
 import java.io.IOException;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.freedesktop.DBus.Properties;
 import org.freedesktop.dbus.DBusConnection;
 import org.freedesktop.dbus.DBusSigHandler;
 import org.freedesktop.dbus.DBusSignal;
 import org.freedesktop.dbus.Path;
 import org.freedesktop.dbus.UInt32;
 import org.freedesktop.dbus.exceptions.DBusException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 import ch.admin.vbs.cube.common.shell.ScriptUtil;
 import ch.admin.vbs.cube.common.shell.ShellUtil;
 import ch.admin.vbs.cube.core.CubeClientCoreProperties;
 import ch.admin.vbs.cube.core.network.impl.DBusExplorer;
 
 /**
  * @see http://projects.gnome.org/NetworkManager//developers/api/08/spec-08.html
  */
 public class NMApplet {
 	private static final String NM_DBUS_OBJECT = "/org/freedesktop/NetworkManager";
 	private static final String NM_DBUS_BUSNAME = "org.freedesktop.NetworkManager";
 	private static final String NM_DBUS_NMIFACE = "org.freedesktop.NetworkManager";
 	private static final Logger LOG = LoggerFactory.getLogger(NMApplet.class);
 
 	public enum NmState {
 		NM_STATE_UNKNOWN, NM_STATE_ASLEEP, NM_STATE_CONNECTING, NM_STATE_CONNECTED, NM_STATE_DISCONNECTED
 	}
 
 	public enum ActiveConnectionState {
 		NM_ACTIVE_CONNECTION_STATE_UNKNOWN, NM_ACTIVE_CONNECTION_STATE_ACTIVATING, NM_ACTIVE_CONNECTION_STATE_ACTIVATED, NM_ACTIVE_CONNECTION_STATE_DEACTIVATING
 	}
 
 	public enum VpnConnectionState {
 		CUBEVPN_CONNECTION_STATE_PREPARE, CUBEVPN_CONNECTION_STATE_CONNECT, CUBEVPN_CONNECTION_STATE_ACTIVATED, CUBEVPN_CONNECTION_STATE_FAILED, CUBEVPN_CONNECTION_STATE_DISCONNECTED
 	}
 
 	public enum CubeVpnConnectionReason {
 		NM_VPN_CONNECTION_STATE_REASON_UNKNOWN, NM_VPN_CONNECTION_STATE_REASON_NONE, NM_VPN_CONNECTION_STATE_REASON_USER_DISCONNECTED, NM_VPN_CONNECTION_STATE_REASON_DEVICE_DISCONNECTED, NM_VPN_CONNECTION_STATE_REASON_SERVICE_STOPPED, NM_VPN_CONNECTION_STATE_REASON_IP_CONFIG_INVALID, NM_VPN_CONNECTION_STATE_REASON_CONNECT_TIMEOUT, NM_VPN_CONNECTION_STATE_REASON_SERVICE_START_TIMEOUT, NM_VPN_CONNECTION_STATE_REASON_SERVICE_START_FAILED, NM_VPN_CONNECTION_STATE_REASON_NO_SECRETS, NM_VPN_CONNECTION_STATE_REASON_LOGIN_FAILED, NM_VPN_CONNECTION_STATE_REASON_CONNECTION_REMOVED
 	}
 
 	public enum DeviceState {
 		NM_DEVICE_STATE_STATE_UNKNOWN, NM_DEVICE_STATE_UNMNAGED, NM_DEVICE_STATE_UNAVAILABLE, NM_DEVICE_STATE_DISCONNECTED, NM_DEVICE_STATE_PREPARE, NM_DEVICE_STATE_CONFIG, NM_DEVICE_STATE_NEED_AUTH, NM_DEVICE_STATE_IP_CONFIG, NM_DEVICE_STATE_ACTIVATED, NM_DEVICE_STATE_FAILED
 	}
 
 	private DBusConnection sysConn; // system dbus
 	private DBusConnection sesConn; // session dbus
 	private DBusExplorer explo;
 	private Executor exec = Executors.newCachedThreadPool();
 	private ArrayList<VpnStateListener> listeners = new ArrayList<NMApplet.VpnStateListener>();
 
 	public NMApplet() {
 	}
 
 	/**
 	 * Connect DBUS
 	 * 
 	 * @throws DBusException
 	 */
 	public void connect() throws DBusException {
 		sysConn = DBusConnection.getConnection(DBusConnection.SYSTEM);
 		sesConn = DBusConnection.getConnection(DBusConnection.SESSION);
 		explo = new DBusExplorer();
 	}
 
 	public <T extends DBusSignal> void addSignalHanlder(int scope, Class<T> type, DBusSigHandler<T> h) throws DBusException {
 		switch (scope) {
 		case DBusConnection.SESSION:
 			sesConn.addSigHandler(type, h);
 			break;
 		case DBusConnection.SYSTEM:
 		default:
 			sysConn.addSigHandler(type, h);
 			break;
 		}
 	}
 
 	public <E> E getEnumConstant(int stateId, Class<E> x) {
 		if (stateId < 0 || stateId >= x.getEnumConstants().length) {
 			return null;
 		} else {
 			return x.getEnumConstants()[stateId];
 		}
 	}
 
 	
 	public boolean isIpReachable(String ip) {
 		// convert ip to int
 		int ipToCheck = ipToInt(ip);
 		// find all active IP for system connections
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				for (InterfaceAddress nic : intf.getInterfaceAddresses()) {
 					byte[] arr = nic.getAddress().getAddress();
 					// only handle IPV4 since cube is still IPV4
 					if (arr.length == 4 && nic.getNetworkPrefixLength()>0) {
 						LOG.debug("check IP address [{}]",nic.getAddress());
 						int nicIp = arr[0] << 24 | arr[1]<<16 | arr[2]<<8 | arr[3];
 						int mask = ((1 << nic.getNetworkPrefixLength()) - 1) << (32 - nic.getNetworkPrefixLength());
 						if ((nicIp & mask) == (ipToCheck & mask)) {
 							return true;
 						}
 					}
 				}
 			}
 		} catch (SocketException e) {
 			LOG.error("Failed to list network interfaces", e);
 		}
 		return false;
 	}
 
 	/**
 	 * Revert uint32 since NetworkManager deliver the result inverted
 	 */
 	public static final int uint32ToInt(UInt32 uint32) {
 		int uint = uint32.intValue();
 		return (uint & 0xff) << 24 | //
 				(uint & 0xff00) << 8 | //
 				(uint & 0xff0000) >> 8 | //
 				(uint & 0xff000000) >> 24;
 	}
 
 	/**
 	 * convert IP to int. (10.11.1.2 --> 0x0a0b0102)
 	 */
 	public static final int ipToInt(String ip) {
 		String[] split = ip.split("\\.");
 		return ((Integer.parseInt(split[0]) & 0xFF) << 24) | //
 				((Integer.parseInt(split[1]) & 0xFF) << 16) | //
 				((Integer.parseInt(split[2]) & 0xFF) << 8) //
 				| (Integer.parseInt(split[3]) & 0xFF);
 	}
 
 	/*
 	 * do not use network manager to start VPN anymore. system network-manager
 	 * is unable to start it (seems to be a bug) and user network manager need
 	 * to run network manager applet in background (will display unwanted status
 	 * pop-ups over cube UI)
 	 */
 	public void startVpn() {
 		exec.execute(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					LOG.debug("Open Cube VPN");
 					fireVpnConnectionState(VpnConnectionState.CUBEVPN_CONNECTION_STATE_PREPARE);
 					ScriptUtil script = new ScriptUtil();
 					ShellUtil su = script.execute("sudo", "./vpn-open.pl", //
 							"--tap", CubeClientCoreProperties.getProperty("INetworkManager.vpnTap"),//
 							"--hostname", CubeClientCoreProperties.getProperty("INetworkManager.vpnServer"),//
 							"--port", CubeClientCoreProperties.getProperty("INetworkManager.vpnPort"),//
 							"--ca", CubeClientCoreProperties.getProperty("INetworkManager.vpnCa"),//
 							"--cert", CubeClientCoreProperties.getProperty("INetworkManager.vpnCrt"),//
 							"--key", CubeClientCoreProperties.getProperty("INetworkManager.vpnKey"), //
 							"--no-bridge" //
 					);
 					if (su.getExitValue() == 0) {
 						fireVpnConnectionState(VpnConnectionState.CUBEVPN_CONNECTION_STATE_ACTIVATED);
 					} else {
 						fireVpnConnectionState(VpnConnectionState.CUBEVPN_CONNECTION_STATE_FAILED);
 					}
 				} catch (Exception e) {
 					LOG.error("Failed to start Cube VPN",e);
 					fireVpnConnectionState(VpnConnectionState.CUBEVPN_CONNECTION_STATE_FAILED);
 				}
 			}
 		});
 	}
 	public void closeVpn() {
 		try {
 			LOG.debug("Close Cube VPN");
 			ScriptUtil script = new ScriptUtil();
 			ShellUtil su = script.execute("sudo", "./vpn-close.pl", //
 					"--tap", CubeClientCoreProperties.getProperty("INetworkManager.vpnTap"),//
 					"--no-bridge" //
 			);
 			LOG.debug(su.getStandardOutput().toString());
 			LOG.debug(su.getStandardError().toString());
 			fireVpnConnectionState(VpnConnectionState.CUBEVPN_CONNECTION_STATE_DISCONNECTED);
 		} catch (Exception e) {
 			LOG.error("Failed to start OpenVpn",e);
 			fireVpnConnectionState(VpnConnectionState.CUBEVPN_CONNECTION_STATE_FAILED);
 		}
 	}
 
 	public Path getBaseConnection() throws DBusException, SAXException, IOException, ParserConfigurationException {
 		Vector<Path> activeConnections = explo.getProperty(sysConn, //
 				NM_DBUS_BUSNAME, //
 				NM_DBUS_OBJECT, //
 				NM_DBUS_NMIFACE, //
 				"ActiveConnections");
 		// filter VPNs (do not use CubeVPN to open CubeVPN....)
 		ArrayList<Path> noVpn = new ArrayList<Path>();
 		for (Path p : activeConnections) {
 			Properties properties = explo.getProperties(sysConn, NM_DBUS_BUSNAME, p.getPath());
 			ActiveConnectionState pState = getEnumConstant(((UInt32) properties.Get("org.freedesktop.NetworkManager.Connection.Active", "State")).intValue(),
 					ActiveConnectionState.class);
 			boolean pVpn = (Boolean) properties.Get("org.freedesktop.NetworkManager.Connection.Active", "Vpn");
 			if (!pVpn && pState == ActiveConnectionState.NM_ACTIVE_CONNECTION_STATE_ACTIVATED) {
 				noVpn.add(p);
 			} else {
 				LOG.debug("Exclude connection [{}]", p.getPath());
 			}
 		}
 		// return a valid connection
 		if (noVpn.size() == 1) {
 			return noVpn.get(0);
 		} else if (noVpn.size() < 1) {
 			LOG.debug("No active connection to connect a VPN");
 			return null;
 		} else {
 			LOG.debug("More than 1 active connection to connect a VPN. Use the first one.");
 			return noVpn.get(0);
 		}
 	}
 
 	public void enable(boolean b) throws DBusException {
 		NetworkManager nm = sysConn.getRemoteObject(NM_DBUS_BUSNAME, NM_DBUS_OBJECT, org.freedesktop.NetworkManager.class);
 		nm.Enable(b);
 	}
 
 	public void addListener(VpnStateListener l) {
 		listeners.add(l);
 	}
 
 	public void removeListener(VpnStateListener l) {
 		listeners.remove(l);
 	}
 
 	private void fireVpnConnectionState(VpnConnectionState state) {
 		for (VpnStateListener l : listeners) {
 			l.handle(state);
 		}
 	}
 
 	public static interface VpnStateListener {
 		public void handle(VpnConnectionState sig);
 	}
 
 
 }
