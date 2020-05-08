 package com.openseedbox.backend.transmission;
 
 import com.google.gson.*;
 import com.openseedbox.Config;
 import com.openseedbox.backend.IFile;
 import com.openseedbox.backend.IPeer;
 import com.openseedbox.backend.ISessionStatistics;
 import com.openseedbox.backend.ITorrent;
 import com.openseedbox.backend.ITorrentBackend;
 import com.openseedbox.backend.ITracker;
 import com.openseedbox.code.MessageException;
 import com.openseedbox.code.Util;
 import com.turn.ttorrent.bcodec.BDecoder;
 import com.turn.ttorrent.bcodec.BEValue;
 import com.turn.ttorrent.bcodec.BEncoder;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import play.Logger;
 import play.Play;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 
 public class TransmissionBackend implements ITorrentBackend {
 	
 	private void writeConfigFile() {
 		File configFile = new File(Config.getBackendBasePath(), "settings.json");
 		Map<String, Object> conf = Util.convertToMap(new Object[] {
 			"rpc-bind-address", "127.0.0.1",
 			"rpc-authentication-required", false,
 			"rpc-port", Integer.parseInt(getTransmissionPort()),
 			"download-dir", Config.getTorrentsCompletePath(),
 			"incomplete-dir-enabled", false,
 			"download-queue-enabled", false,
 			"lpd-enabled", true,
 			"blocklist-enabled", true,
 			"blocklist-url", Config.getPeerBlocklistUrl()
 		});
 		String contents = new Gson().toJson(conf);
 		try {
 			FileUtils.writeStringToFile(configFile, contents, "UTF-8");
 			if (this.isRunning()) {
 				String pid = FileUtils.readFileToString(new File(getDaemonPidFilePath()));
 				Util.executeCommand("kill -HUP " + pid);
 			}			
 		} catch (IOException ex) {
 			throw new MessageException(ex, "Unable to write transmission config file");
 		}
 	}
 
 	public void start()  {
 		if (!isRunning()) {
 			writeConfigFile();
 			String command = String.format(
 				"transmission-daemon --config-dir %s --pid-file %s 2>&1",
 				Config.getBackendBasePath(), getDaemonPidFilePath());
 			Logger.info("Starting transmission-daemon via: %s", command);
 			String output = Util.executeCommand(command);
 			if (!StringUtils.isEmpty(output.trim())) {
 				Logger.info("Oddities starting transmission: %s", output);
 			}
 			sleepWhile(false);
 		}
 	}
 
 	public void stop() {
 		if (isRunning()) {
 			try {
 				String pid = getDaemonPID(new File(getDaemonPidFilePath()));
 				Util.executeCommand("kill " + pid);
 				sleepWhile(true);			
 			} catch (IOException ex) {
 				Logger.info("Unable to read PID file: %s", ex);
 			}			
 		}
 		new File(getDaemonPidFilePath()).delete();
 	}
 	
 	public void restart() {
 		stop();
 		start();
 	}
 	
 	private void sleepWhile(boolean running) {
 		int count = 0;
 		int limit = 20; //prevent infinite loop
 		try {
 			if (running) {
 				while (isRunning()) {
 					if (count > limit) {
 						throw new RuntimeException("Process didnt start in 10 seconds, exiting to prevent infinite loop.");
 					}
 					Thread.sleep(500);
 					count++;
 				}
 			} else {
 				while (!isRunning()) {
 					if (count > limit) {
 						throw new RuntimeException("Process didnt die in 10 seconds, exiting to prevent infinite loop.");
 					}
 					Thread.sleep(500);
 					count++;
 				}
 			}
 		} catch (InterruptedException ex) {
 			//no state to clean up
 		}
 	}
 	
 	public boolean isRunning() {
 		File pidFile = new File(getDaemonPidFilePath());
 		if (pidFile.exists()) {
 			try {
 				String pid = getDaemonPID(pidFile);
 				return !StringUtils.isEmpty(Util.executeCommand("ps -A | grep " + pid).trim());
 			} catch (IOException ex) {
 				Logger.error("Unable to read daemon.pid file", ex);
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	private String getDaemonPID(File pidFile) throws IOException {
 		String pid = FileUtils.readFileToString(pidFile);
 		if (StringUtils.isEmpty(pid)) {
 			//for some reason, sometimes transmission doesnt write its PID file but its still running
 			pid = StringUtils.trim(Util.executeCommand("ps -A | grep transmission | awk '{print $1}'"));
 		}
 		return pid;
 	}
 		
 	public ISessionStatistics getSessionStatistics() throws MessageException {
 		RpcRequest req = new RpcRequest("session-stats");
 		RpcResponse res = req.getResponse();
 		if (res.success()) {
 			return new Gson().fromJson(res.getArguments(), TransmissionSessionStats.class);
 		} else {
 			throw new MessageException("Unable to get session stats: " + res.getResultMessage());
 		}
 	}
 	
 	public ITorrent addTorrent(File f) {
 		byte[] torrent;
 		try {
 			torrent = FileUtils.readFileToByteArray(f);
 		} catch (IOException ex) {
 			throw new MessageException("Unable to read torrent file!");
 		}
 		String contents = Base64.encodeBase64String(torrent);
 		return addTorrent(null, contents);
 	}
 	
 	public ITorrent addTorrent(String urlOrMagnet) {
 		return addTorrent(urlOrMagnet, null);
 	}	
 	
 	public void removeTorrent(String hash) {
 		List<String> l = new ArrayList<String>();
 		l.add(hash);
 		removeTorrent(l);
 	}
 	
 	public void removeTorrent(List<String> hashes) {
 		RpcRequest r = new RpcRequest("torrent-remove", hashes);
 		r.addArgument("delete-local-data", true);
 		r.getResponse().successOrExcept();		
 	}
 	
 	public void startTorrent(String torrentHash) {	
 		List<String> s = new ArrayList<String>();
 		s.add(torrentHash);
 		startTorrent(s);
 	}
 	
 	public void startTorrent(List<String> torrentHashes) {
 		RpcRequest r = new RpcRequest("torrent-start");
 		r.addArgument("ids", getTorrentIds(torrentHashes));
 		r.getResponse().successOrExcept();		
 	}
 	
 	public void stopTorrent(String torrentHash) {
 		List<String> s = new ArrayList<String>();
 		s.add(torrentHash);
 		stopTorrent(s);		
 	}
 	
 	public void stopTorrent(List<String> torrentHashes) {
 		RpcRequest r = new RpcRequest("torrent-stop", torrentHashes);
 		r.getResponse().successOrExcept();		
 	}
 	
 	public ITorrent getTorrentStatus(String hashString) {
 		return getTorrentStatus(Arrays.asList(new String[] { hashString })).get(0);
 	}
 	
 	public List<ITorrent> listTorrents() {
 		return getTorrentStatus((List<String>) null);
 	}
 	
 	public List<ITorrent> getTorrentStatus(List<String> hashes) {
 		return getTorrentInfo(hashes, true, false, false, false);
 	}
 
 	public boolean isInstalled() {
 		String output = Util.executeCommand("which transmission-daemon");
 		return !StringUtils.isEmpty(output);
 	}
 
 	public String getName() {
 		return "transmission";
 	}
 
 	public String getVersion() {
 		return Util.executeCommand("transmission-daemon -V 2>&1");
 	}
 
 	public List<IPeer> getTorrentPeers(String hash) {
 		return getTorrentPeers(Arrays.asList(new String[] { hash })).get(hash);
 	}
 
 	public Map<String, List<IPeer>> getTorrentPeers(List<String> hashes) {
 		List<ITorrent> torrents = getTorrentInfo(hashes, false, false, true, false);
 		Map<String, List<IPeer>> ret = new HashMap<String, List<IPeer>>();
 		for (ITorrent t : torrents) {
 			ret.put(t.getTorrentHash(), t.getPeers());
 		}
 		return ret;
 	}
 
 	public List<ITracker> getTorrentTrackers(String hash) {
 		return getTorrentTrackers(Arrays.asList(new String[] { hash })).get(hash);
 	}
 
 	public Map<String, List<ITracker>> getTorrentTrackers(List<String> hashes) {
 		List<ITorrent> torrents = getTorrentInfo(hashes, false, false, false, true);
 		Map<String, List<ITracker>> ret = new HashMap<String, List<ITracker>>();
 		for (ITorrent t : torrents) {
 			ret.put(t.getTorrentHash(), t.getTrackers());
 		}
 		return ret;
 	}
 
 	public List<IFile> getTorrentFiles(String hash) {
 		return getTorrentFiles(Arrays.asList(new String[] { hash })).get(hash);
 	}
 
 	public Map<String, List<IFile>> getTorrentFiles(List<String> hashes) {
 		List<ITorrent> torrents = getTorrentInfo(hashes, false, true, false, false);
 		Map<String, List<IFile>> ret = new HashMap<String, List<IFile>>();
 		for (ITorrent t : torrents) {
 			ret.put(t.getTorrentHash(), t.getFiles());
 		}
 		return ret;
 	}
 
 	public void modifyTorrent(String hash, double seedRatio, long uploadLimitBytes, long downloadLimitBytes) {
 		RpcRequest rpc = new RpcRequest("torrent-set", hash);
 		if (seedRatio > 0) {
 			rpc.addArgument("seedRatioLimit", seedRatio);
 		}
 		if (uploadLimitBytes > 0) {
 			rpc.addArgument("uploadLimit", uploadLimitBytes);
 		}
 		if (downloadLimitBytes > 0) {
 			rpc.addArgument("downloadLimit", downloadLimitBytes);
 		}		
 		rpc.getResponse().successOrExcept();		
 	}
 
 	public List<ITorrent> listRecentlyActiveTorrents() {
 		return getTorrentStatus(Arrays.asList(new String[] { "recently-active" }));
 	}
 
 	public void modifyTorrentFiles(String hash, List<IFile> files) {
 		RpcRequest rpc = new RpcRequest("torrent-set", hash);
 		List<String> filesUnwanted = new ArrayList<String>();
 		List<String> filesWanted = new ArrayList<String>();
 		List<String> highPriority = new ArrayList<String>();
 		List<String> normalPriority = new ArrayList<String>();
 		List<String> lowPriority = new ArrayList<String>();
 		for (IFile f : files) {
 			if (f.isWanted()) {
 				filesWanted.add(f.getId());
 			} else {
 				filesUnwanted.add(f.getId());
 			}
 			if (f.getPriority() == -1) {
 				lowPriority.add(f.getId());
 			} else if (f.getPriority() == 1) {
 				highPriority.add(f.getId());
 			} else {
 				normalPriority.add(f.getId());
 			}
 		}
 		if (filesUnwanted.size() > 0) {
 			rpc.addArgument("files-unwanted", getTorrentIds(filesUnwanted));
 		}
 		if (filesWanted.size() > 0) {
 			rpc.addArgument("files-wanted", getTorrentIds(filesWanted));		
 		}
 		if (highPriority.size() > 0) {
 			rpc.addArgument("priority-high", getTorrentIds(highPriority));
 		}
 		if (normalPriority.size() > 0) {
 			rpc.addArgument("priority-normal", getTorrentIds(normalPriority));
 		}
 		if (lowPriority.size() > 0) {
 			rpc.addArgument("priority-low", getTorrentIds(lowPriority));
 		}		
 		rpc.getResponse().successOrExcept();		
 	}
 	
 	private ITorrent addTorrent(String urlOrMagnet, String base64Contents) {
 		RpcRequest req = new RpcRequest("torrent-add");
 		String torrentHash = null;
 		if (!StringUtils.isEmpty(urlOrMagnet)) {
 			if (!urlOrMagnet.startsWith("magnet:")) {
 				//make sure file is only downloaded once (not twice, ie once by us and once by transmission)
 				//this is to avoid things like using 2 freeleech tokens in eg what.cd
 				try {
 					base64Contents = Base64.encodeBase64String(
 							  IOUtils.toByteArray(WS.url(urlOrMagnet.trim()).get().getStream()));					
 				} catch (Exception ex) {
 					throw new MessageException("Unable to read file at url, are you sure it's valid?");
 				}
 			} else {
 				req.addArgument("filename", urlOrMagnet);
 				torrentHash = getHashFromMagnet(urlOrMagnet);
 			}
 		}
 		if (base64Contents != null) {
 			req.addArgument("metainfo", base64Contents);
 			torrentHash = getHashFromBase64(base64Contents);
 		}
 		if (torrentHash == null) {
 			throw new MessageException("Invalid file! Could not determine info_hash.");
 		}
 		req.addArgument("paused", false);		
		req.addArgument("download-dir", new File(Config.getTorrentsCompletePath(), torrentHash.toLowerCase()).getAbsolutePath());
 		RpcResponse res = req.getResponse();
 		if (res.success()) {
 			//DONT USE Util.getGson() HERE! What happens is the JSON that transmission returns doesnt match up with
 			//the @SerializedAccessorName's on ITorrent (which TransmissionTorrent) implements so some properties
 			//(notably, the hashString) dont get passed through
 			return new Gson().fromJson(res.getArguments().get("torrent-added"), TransmissionTorrent.class);						
 		} else {
 			throw new MessageException("Unable to add torrent: " + res.getResultMessage());
 		}
 	}	
 	
 	private String getHashFromBase64(String b64) {
 		byte[] data = Base64.decodeBase64(b64);
 		ByteArrayInputStream bais = new ByteArrayInputStream(data);
 		return getHashFromInputStream(bais);
 	}
 	
 	private String getHashFromMagnet(String magnet) {		
 		Map<String, String> params = Util.getUrlParameters(magnet.replace("magnet:", ""));
 		if (params.containsKey("xt")) {
 			return params.get("xt").replace("urn:btih:", "");
 		}
 		throw new MessageException("Unable to get torrent hash from magnet link, are you sure its valid?");		
 	}
 	
 	private String getHashFromInputStream(InputStream is) {
 		try {
 			Map<String, BEValue> map = BDecoder.bdecode(is).getMap();	
 			if (map.containsKey("info")) {
 				BEValue info = map.get("info");
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				BEncoder.bencode(info, baos);
 				return DigestUtils.shaHex(baos.toByteArray());
 			}			
 		} catch (IOException ex) {
 			//fuck off java
 		}
 		throw new MessageException("Unable to obtain torrent info_hash! Is this a valid torrent file or magnet link?");
 	}
 	
 	private List<ITorrent> getTorrentInfo(List<String> hashes, boolean normal, boolean files, boolean peers, boolean trackers) {
 		List<String> fields = new ArrayList<String>();
 		fields.add("hashString"); //always need hashString or everything breaks
 		if (normal) {
 			fields.addAll(Arrays.asList(new String[] {
 				"id", "name", "percentDone", "rateDownload", "rateUpload", "errorString",
 				"totalSize", "downloadedEver", "uploadedEver", "status", "metadataPercentComplete"
 			}));
 		}
 		if (files) {
 			fields.addAll(Arrays.asList(new String[] {
 				"files", "wanted", "priorities"
 			}));
 		}		
 		if (peers) {
 			fields.addAll(Arrays.asList(new String[] {
 				"peers", "peersFrom"
 			}));
 		}
 		if (trackers) {
 			fields.add("trackerStats");
 		}				 	
 		RpcRequest req = (hashes != null) ? new RpcRequest("torrent-get", hashes) : new RpcRequest("torrent-get");
 		req.addArgument("fields", fields);
 		RpcResponse r = req.getResponse();
 		JsonArray torrents = r.getArguments().getAsJsonArray("torrents");
 		//Logger.info("Response: %s", torrents.toString());
 		List<ITorrent> ret = new ArrayList<ITorrent>();
 		Gson g = new Gson();
 		for (JsonElement torrent : torrents) {
 			ret.add(g.fromJson(torrent, TransmissionTorrent.class));
 		}
 		return ret;		
 	}
 	
 	private List<Object> getTorrentIds(List<String> ids) {
 		/* convert the List<String> into a List<Object>. Transmission-daemon wont
 		 * remove torrents by id if the submitted id is a string and not an int.
 		 * However, we might also want to remove torrents by hash in the same request.
 		 * Therefore, we need both strings and ints to be in the ids array */		
 		List<Object> actual_ids = new ArrayList<Object>();
 		for (String eyedee : ids) {
 			try {
 				actual_ids.add(Integer.parseInt(eyedee));
 			} catch (NumberFormatException ex) {
 				actual_ids.add(eyedee);
 			}
 		}	
 		return actual_ids;
 	}	
 
 	private String getTransmissionUrl() throws MessageException {
 		return String.format("http://127.0.0.1:%s/transmission/rpc", getTransmissionPort());
 	}	
 	
 	private String getTransmissionPort() {
 		return Play.configuration.getProperty("backend.transmission.port", "9091");
 	}
 	
 	private String getDaemonPidFilePath() {
 		return new File(Config.getBackendBasePath(), "daemon.pid").getAbsolutePath();
 	}	
 	
 	public class RpcRequest {
 		
 		public String method;	
 		public Map<String, Object> arguments = new HashMap<String, Object>();
 		
 		public RpcRequest() {}
 		
 		public RpcRequest(String method) {
 			this.method = method;
 		}
 		
 		public RpcRequest(String method, String torrentHash) {
 			this(method);
 			this.arguments.put("ids", getTorrentIds(Arrays.asList(new String[] { torrentHash })));
 		}
 		
 		public RpcRequest(String method, List<String> torrentHashes) {
 			this(method);
 			this.arguments.put("ids", getTorrentIds(torrentHashes));
 		}
 		
 		public void addArgument(String key, Object value) {
 			this.arguments.put(key, value);
 		}
 		
 		public RpcResponse getResponse() throws MessageException {
 			String url = getTransmissionUrl();
 			//Logger.info("Transmission RPC request, to url: %s", url);
 			WS.WSRequest req = WS.url(url);
 //			req.authenticate(_account.getPrimaryUser().emailAddress, _account.getTransmissionPassword());
 			String body = new Gson().toJson(this);
 			//Logger.info("Sending request: %s", body);
 			req.body(body);
 			WS.HttpResponse res = getResponse(req, null);
 			RpcResponse rpc = new RpcResponse(res);
 			return rpc;		
 		}
 		
 		private WS.HttpResponse getResponse(WS.WSRequest req, String transmissionId) throws MessageException {
 			if (!StringUtils.isEmpty(transmissionId)) {
 				req.headers.put("X-Transmission-Session-Id", transmissionId);
 			}
 			WS.HttpResponse res = null;
 			try {
 				res = req.post();
 			} catch (Exception ex) {
 				Logger.info("Error in RPC call: %s", ex);
 				if (ex.getMessage().contains("Connection refused")) {
 					throw new MessageException("Unable to connect to backend transmission-daemon!");
 				} else {
 					throw new MessageException(ex.getMessage());
 				}
 			}
 			if (res.getStatus() == 409) {
 				String tid = res.getHeader("X-Transmission-Session-Id");
 				return getResponse(req, tid);
 			} else if (res.getStatus() == 401) {
 				throw new MessageException("Invalid RPC credentials supplied!");
 			}
 			return res;
 		}
 	}
 	
 	public class RpcResponse {
 		private String jsonData;
 		
 		public RpcResponse(HttpResponse r) {
 			jsonData = r.getString();
 		}
 		
 		public String getJsonData() {
 			return jsonData;
 		}
 		
 		public boolean successOrExcept() throws MessageException {
 			boolean s = success();
 			if (!s) {
 				throw new MessageException(getResultMessage());
 			}
 			return s;
 		}
 		
 		public boolean success() {
 			return (getResultMessage().equals("success"));
 		}
 		
 		public JsonObject getWholeResponse() {
 			return new JsonParser().parse(jsonData).getAsJsonObject();
 		}
 		
 		public JsonObject getArguments() {
 			JsonObject wr = getWholeResponse();
 			return wr.getAsJsonObject("arguments");
 		}
 		
 		public String getResultMessage() {
 			JsonObject ob = getWholeResponse();
 			return ob.get("result").getAsString();
 		}
 		
 		
 		@Override
 		public String toString() {
 			if (success()) {
 				return "Successful RPC response: " + jsonData;
 			} else {
 				return "Failed RPC Response: " + getResultMessage();
 			}
 		}
 	}
 	
 }
