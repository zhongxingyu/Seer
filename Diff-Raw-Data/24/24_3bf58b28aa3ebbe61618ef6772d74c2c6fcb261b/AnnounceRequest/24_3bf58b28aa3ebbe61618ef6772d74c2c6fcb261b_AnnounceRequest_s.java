 package tracker.announcer;
 
 import play.mvc.Http;
 import play.mvc.Scope;
 import tracker.Config;
import tracker.util.NetUtils;
 import tracker.util.Utils;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 public class AnnounceRequest {
 	private Scope.Params params;
 
 	private Map<String, String[]> binarySafeParams;
 
 	public AnnounceRequest(Scope.Params params) {
 		this.params = params;
 		this.binarySafeParams = Utils.parseUrlEncodedString(Http.Request.current().querystring, Charset.forName(Config.getString("tracker.bytesCharset")), true);
 	}
 
 	public byte[] getInfoHash() throws UnsupportedEncodingException {
 		if (!this.binarySafeParams.containsKey("info_hash")) {
 			Response.error("Invalid info_hash");
 		}
 
 		String infoHashStr = this.binarySafeParams.get("info_hash")[0];
 
 		byte[] infoHash = infoHashStr.getBytes(Config.getString("tracker.bytesCharset"));
 
 		if (infoHash == null || infoHash.length != 20) {
 			Response.error("Invalid info_hash");
 		}
 
 		return infoHash;
 	}
 
 	public String getInfoHashHexString() throws UnsupportedEncodingException {
 		return Utils.getHexString(this.getInfoHash());
 	}
 
 	public String getPeerIdString()  {
 		if (!this.binarySafeParams.containsKey("peer_id")) {
 			return null;
 		}
 
 		return this.binarySafeParams.get("peer_id")[0];
 	}
 
 	public byte[] getPeerId() throws UnsupportedEncodingException {
 		if (!this.binarySafeParams.containsKey("peer_id")) {
 			return null;
 		}
 
 		String peerIdStr = this.binarySafeParams.get("peer_id")[0];
 		return peerIdStr.getBytes(Config.getString("tracker.bytesCharset"));
 	}
 
 	public Integer getPort() {
 		if (!params._contains("port")) {
 			return null;
 		}
 
 		return params.get("port", Integer.class);
 	}
 
 	public Long getDownloaded() {
 		if (!params._contains("downloaded")) {
 			return null;
 		}
 
 		return params.get("downloaded", Long.class);
 	}
 
 	public Long getUploaded() {
 		if (!params._contains("uploaded")) {
 			return null;
 		}
 
 		return params.get("uploaded", Long.class);
 	}
 
 	public Long getLeft() {
 		if (!params._contains("left")) {
 			return null;
 		}
 
 		return Long.parseLong(params.get("left"));
 	}
 
 	public Boolean isCompact() {
 		return params._contains("compact") && Byte.parseByte(params.get("compact")) > 0;
 	}
 
 	/**
 	 * Получить состояние no_peer_id.
 	 * Указывает на то, что можно не включать peer_id в список пиров, отдаваемых клиенту.
 	 * Эта опция игнорируется, если активен флаг Compact.
 	 *
 	 * @return no_peer_id
 	 */
 	public boolean isNoNeedPeerId() {
 		return params._contains("no_peer_id") && Byte.parseByte(params.get("no_peer_id")) > 0;
 	}
 
 	public Event getEvent() {
 		if (!params._contains("event")) {
 			return Event.EMPTY;
 		}
 
 		Event event = Event.byKey(params.get("event"));
 
 		if (event == null) {
 			Response.error("Wrong event!");
 		}
 
 		return event;
 	}
 
 	public String getIp() {
 		if (!params._contains("ip")) {
			return NetUtils.getIp();
 		}
 
 		return params.get("ip");
 	}
 
 	public Long getIpValue() {
 		final String[] addressBytes = this.getIp().split("\\.");
 
 		long ip = 0;
 		for (int i = 0; i < 4; i++) {
 			ip <<= 8;
 			ip |= Long.parseLong(addressBytes[i]);
 		}
 
 		return ip;
 	}
 
 	public String getIpv6() throws Exception {
 		throw new Exception("Not implemented");
 	}
 
 	public Integer getIpv6Value() throws Exception {
 		throw new Exception("Not implemented");
 	}
 
 	public Integer getNumWant() {
 		String[] numwantParamNames = { "numwant", "num_want", "num want" };
 		for (String numwantParamName : numwantParamNames) {
 			if (params._contains(numwantParamName)) {
 				int numwant;
 
 				try {
 					numwant = Integer.parseInt(params.get(numwantParamName));
 				} catch (NumberFormatException numberFormatException) {
 					continue;
 				}
 
 				if (numwant > Config.getInt("tracker.announce.numwant.maximum")) {
 					return Config.getInt("tracker.announce.numwant.maximum");
 				} else {
 					return numwant;
 				}
 			}
 		}
 
 		return Config.getInt("tracker.announce.numwant.default");
 	}
 
 	public String getPassKey() {
 		if (!params._contains("passkey")) {
 			return null;
 		}
 
 		return params.get("passkey");
 	}
 
 	public Integer getTrackerId() throws Exception {
 		throw new Exception("Not implemented");
 	}
 
 //	private volatile static AnnounceRequest instance = null;
 //
 //	public static AnnounceRequest get() {
 //		if (instance == null) {
 //			synchronized (AnnounceRequest.class) {
 //				if (instance == null) {
 //					instance = new AnnounceRequest();
 //				}
 //			}
 //		}
 //
 //		return instance;
 //	}
 }
