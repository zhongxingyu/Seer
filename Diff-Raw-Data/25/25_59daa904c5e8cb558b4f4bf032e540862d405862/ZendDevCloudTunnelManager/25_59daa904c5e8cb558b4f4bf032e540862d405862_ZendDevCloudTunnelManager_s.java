 package org.zend.php.zendserver.deployment.debug.core.tunnel;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.zend.php.zendserver.deployment.debug.core.tunnel.ZendDevCloudTunnel.State;
 import org.zend.sdklib.internal.target.ZendDevCloud;
 import org.zend.sdklib.target.IZendTarget;
 
 public class ZendDevCloudTunnelManager {
 
 	public static final String DEVPASS_HOST = ZendDevCloud.DEVPASS_HOST;
 
 	private static ZendDevCloudTunnelManager manager;
 
 	private Map<IZendTarget, ZendDevCloudTunnel> targets;
 
 	private ZendDevCloudTunnelManager() {
 		this.targets = new HashMap<IZendTarget, ZendDevCloudTunnel>();
 	}
 
 	public static ZendDevCloudTunnelManager getManager() {
 		if (manager == null) {
 			manager = new ZendDevCloudTunnelManager();
 		}
 		return manager;
 	}
 
 	public State connect(IZendTarget target) throws IOException {
 		ZendDevCloudTunnel tunnel = getTunnel(target);
 		String user = getUsername(target);
 		String privateKey = getSSHPrivateKey(target);
 		if (user == null || user.length() == 0 || privateKey == null || privateKey.length() == 0) {
 			return State.NOT_SUPPORTED;
 		}
 		if (tunnel == null) {
 			tunnel = new ZendDevCloudTunnel(user, privateKey);
 			targets.put(target, tunnel);
 		}
		return connect(tunnel);
 	}
 
 	public void disconnect(IZendTarget target) {
 		ZendDevCloudTunnel tunnel = targets.get(target);
 		if (tunnel != null) {
 			tunnel.disconnect();
 		}
 	}
 
 	public void disconnectAll() {
 		Set<IZendTarget> targetsSet = targets.keySet();
 		for (IZendTarget target : targetsSet) {
 			disconnect(target);
 		}
 	}
 
 	private ZendDevCloudTunnel getTunnel(IZendTarget target) {
 		Set<IZendTarget> targetsSet = targets.keySet();
 		for (IZendTarget t : targetsSet) {
 			if (target.getHost().getHost().equals(t.getHost().getHost())) {
 				return targets.get(t);
 			}
 		}
 		return null;
 	}
 
 	private String getSSHPrivateKey(IZendTarget target) {
 		String host = target.getHost().getHost();
 		if (host.contains(ZendDevCloud.DEVPASS_HOST)) {
 			return target.getProperty(ZendDevCloud.SSH_PRIVATE_KEY_PATH);
 		}
 		return null;
 	}
 
 	private String getUsername(IZendTarget target) {
 		String host = target.getHost().getHost();
 		if (host.contains(ZendDevCloud.DEVPASS_HOST)) {
 			return host.substring(0, host.indexOf('.'));
 		}
 		return null;
 	}
 
	private State connect(ZendDevCloudTunnel tunnel) throws IOException {
		if (!tunnel.isConnected()) {
 			return tunnel.connect();
 		}
 		return State.CONNECTED;
 	}
 
 }
