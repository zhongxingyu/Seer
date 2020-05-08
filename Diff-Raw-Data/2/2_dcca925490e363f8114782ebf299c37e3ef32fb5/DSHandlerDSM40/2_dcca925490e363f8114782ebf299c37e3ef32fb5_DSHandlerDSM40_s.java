 /**
  * Copyright 2010 Steve Garon Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.protocol.v40;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.server.SimpleSynoServer;
 import com.bigpupdev.synodroid.utils.SearchResult;
 import com.bigpupdev.synodroid.utils.SortOrder;
 import com.bigpupdev.synodroid.utils.Utils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.bigpupdev.synodroid.data.Folder;
 import com.bigpupdev.synodroid.data.SearchEngine;
 import com.bigpupdev.synodroid.data.SharedDirectory;
 import com.bigpupdev.synodroid.data.Task;
 import com.bigpupdev.synodroid.data.TaskContainer;
 import com.bigpupdev.synodroid.data.TaskDetail;
 import com.bigpupdev.synodroid.data.TaskFile;
 import com.bigpupdev.synodroid.data.TaskFilesContainer;
 import com.bigpupdev.synodroid.data.TaskProperties;
 import com.bigpupdev.synodroid.data.TaskStatus;
 import com.bigpupdev.synodroid.protocol.DSHandler;
 import com.bigpupdev.synodroid.protocol.DSMException;
 import com.bigpupdev.synodroid.protocol.MultipartBuilder;
 import com.bigpupdev.synodroid.protocol.Part;
 import com.bigpupdev.synodroid.protocol.QueryBuilder;
 import com.bigpupdev.synodroid.protocol.StreamFactory;
 
 import android.net.Uri;
 import android.util.Log;
 
 /**
  * @author Stave Garon (steve.garon at gmail dot com)
  */
 class DSHandlerDSM40 implements DSHandler {
 
 	// DownloadManager constant declaration
 	private static final String DM_URI_NEW = "/webman/3rdparty/DownloadStation/dlm/downloadman.cgi";
 	private static final String TORRENT_INFO = "/webman/3rdparty/DownloadStation/dlm/torrent_info.cgi";
 	private static final String INITDATA_URI = "/webman/initdata.cgi";
 	private static final String USER_SETTINGS = "/webman/usersettings.cgi";
 	private static final String SEARCH_URI = "/webman/3rdparty/DownloadStation/dlm/btsearch.cgi";
 	private static final String FILE_URI = "/webman/modules/FileBrowser/file_share.cgi";
 	private static final String BOUNDARY = "-----------7dabb2d41348";
 	private static final int MAX_LOOP = 4;
 	
 	/* The Synology's server */
 	private SimpleSynoServer server;
 	private boolean DEBUG;
 
 	/**
 	 * The constructor
 	 * 
 	 * @param serverP
 	 */
 	public DSHandlerDSM40(SimpleSynoServer serverP, boolean debug) {
 		server = serverP;
 		DEBUG = debug;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.Protocol#getAllTask()
 	 */
 	public TaskContainer getAllTask(int startP, int limitP, String sortAttrP, boolean ascendingP) throws Exception {
 		ArrayList<Task> result = new ArrayList<Task>();
 		TaskContainer container = new TaskContainer(result);
 		// If we are logged on
 		if (server.isConnected()) {
 			QueryBuilder getAllRequest = new QueryBuilder().add("action", "getall");
 			getAllRequest.add("start", "" + startP);
 			getAllRequest.add("limit", "" + limitP);
 			getAllRequest.add("sort", sortAttrP.toLowerCase());
 			String asc = (ascendingP ? "ASC" : "DESC");
 			getAllRequest.add("dir", asc);
 			// Execute
 			JSONObject jso = null;
 			synchronized (server) {
 				jso = server.sendJSONRequest(DM_URI_NEW, getAllRequest.toString(), "GET");
 			}
 			boolean success = jso.getBoolean("success");
 			// If request succeded
 			if (success) {
 				// Get the totals rates
 				String totalUp = jso.getString("total_up");
 				String totalDown = jso.getString("total_down");
 				int totalTasks = jso.getInt("total");
 				container.setTotalUp(totalUp);
 				container.setTotalDown(totalDown);
 				container.setTotalTasks(totalTasks);
 				JSONArray items = jso.getJSONArray("items");
 				for (int iLoop = 0; iLoop < items.length(); iLoop++) {
 					JSONObject item = items.getJSONObject(iLoop);
 					// Create the task item
 					Task task = new Task();
 					task.fileName = item.getString("filename").trim();
 					task.taskId = item.getInt("task_id");
 					task.downloadRate = item.getString("current_rate").trim();
 					task.downloadSize = item.getString("current_size").trim();
 					String prog = item.getString("progress").trim();
 					int index = prog.indexOf("%");
 					// If a value could be found ('%' found)
 					if (index != -1) {
 						prog = prog.substring(0, index);
 						try {
 							task.downloadProgress = (int) Float.parseFloat(prog);
 						} catch (NumberFormatException ex) {
 							// Set to unknown
 							task.downloadProgress = -1;
 						}
 					}
 					// Set to unknown
 					else {
 						task.downloadProgress = -1;
 					}
 					task.status = item.getString("status");
 					task.eta = Utils.computeTimeLeft(item.getInt("timeleft"));
 					task.totalSize = item.getString("total_size").trim();
 					task.uploadRate = item.getString("upload_rate").trim();
 					task.creator = item.getString("username").trim();
 					if (task.creator.equals("")) {
 						task.creator = server.getUser();
 					}
 					task.server = server;
 					result.add(task);
 				}
 			}
 			// Try to do something
 			else {
 				String reason = jso.getJSONObject("errno").getString("key");
 				throw new DSMException(reason);
 			}
 		}
 		return container;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#delete(com.bigpupdev.synodroid .common.data.Task)
 	 */
 	public void delete(String taskids) throws Exception {
 		// If we are logged on
 		if (server.isConnected()) {
 			try {
 				QueryBuilder getAllRequest = new QueryBuilder().add("action", "delete").add("idList", taskids);
 				// Execute
 				synchronized (server) {
 					server.sendJSONRequest(DM_URI_NEW, getAllRequest.toString(), "GET");
 				}
 			} catch (Exception e) {
 				if (DEBUG) Log.e(Synodroid.DS_TAG, "Not expected exception occured while deleting id:" + taskids, e);
 				throw e;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#clearAll()
 	 */
 	public void clearAll() throws Exception {
 		// If we are logged on
 		if (server.isConnected()) {
 			try {
 				QueryBuilder getAllRequest = new QueryBuilder().add("action", "clear").add("idList", "");
 				// Execute
 				synchronized (server) {
 					server.sendJSONRequest(DM_URI_NEW, getAllRequest.toString(), "GET");
 				}
 			} catch (Exception e) {
 				if (DEBUG) Log.e(Synodroid.DS_TAG, "Not expected exception occured while clearing tasks", e);
 				throw e;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#resume(com.bigpupdev.synodroid .common.data.Task)
 	 */
 	public void resume(String taskids) throws Exception {
 		// If we are logged on
 		if (server.isConnected()) {
 			QueryBuilder getAllRequest = new QueryBuilder().add("action", "resume").add("idList", taskids);
 			// Execute
 			synchronized (server) {
 				server.sendJSONRequest(DM_URI_NEW, getAllRequest.toString(), "GET");
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#resumeAll(List<com.bigpupdev. synodroid. common.data.Task>)
 	 */
 	public void resumeAll(List<Task> taskP) throws Exception {
 		String taskids = "";
 		for (Task task : taskP) {
 			if (task.status.equals(TaskStatus.TASK_PAUSED.toString())) {
 				if (!taskids.equals("")){
 					taskids += ":";
 				}
 				taskids += ""+task.taskId;
 			}
 		}
 		resume(taskids);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#stop(com.bigpupdev.synodroid. common.data.Task)
 	 */
 	public void stop(String taskids) throws Exception {
 		// If we are logged on
 		if (server.isConnected()) {
 			QueryBuilder getAllRequest = new QueryBuilder().add("action", "stop").add("idList", taskids);
 			// Execute
 			synchronized (server) {
 				server.sendJSONRequest(DM_URI_NEW, getAllRequest.toString(), "GET");
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#stopAll(List<com.bigpupdev.synodroid . common.data.Task>)
 	 */
 	public void stopAll(List<Task> taskP) throws Exception {
 		String taskids = "";
 		for (Task task : taskP) {
 			if (task.status.equals(TaskStatus.TASK_DOWNLOADING.toString()) || task.status.equals(TaskStatus.TASK_PRE_SEEDING.toString()) || task.status.equals(TaskStatus.TASK_SEEDING.toString()) || task.status.equals(TaskStatus.TASK_HASH_CHECKING.toString()) || task.status.equals(TaskStatus.TASK_WAITING.toString())) {
 				if (!taskids.equals("")){
 					taskids += ":";
 				}
 				taskids += ""+task.taskId;
 			}
 		}
 		stop(taskids);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#getFiles(com.bigpupdev.synodroid .common.data.Task)
 	 */
 	public TaskFilesContainer getFiles(Task taskP, int start, int limit) throws Exception {
 		ArrayList<TaskFile> result = new ArrayList<TaskFile>();
 		TaskFilesContainer container = new TaskFilesContainer(result);
 		
 		// If we are logged on
 		if (server.isConnected()) {
 			QueryBuilder getAllRequest = new QueryBuilder().add("action", "list_file").add("task_id", "" + taskP.taskId).add("start", ""+ start).add("limit", ""+limit);
 			// Execute
 			JSONObject json = null;
 			synchronized (server) {
 				json = server.sendJSONRequest(TORRENT_INFO, getAllRequest.toString(), "GET");
 			}
 			boolean success = json.getBoolean("success");
 			// If request succeded
 			if (success) {
 				JSONArray array = json.getJSONArray("items");
 				int totalFiles = json.getInt("total");
 				container.setTotalFiles(totalFiles);
 				
 				for (int iLoop = 0; iLoop < array.length(); iLoop++) {
 					JSONObject obj = array.getJSONObject(iLoop);
 					// Create the file
 					TaskFile file = new TaskFile();
 					file.name = obj.getString("name");
 					file.filesize = obj.getString("size");
 					file.download = obj.getBoolean("wanted");
 					file.id = obj.getInt("index");
 					result.add(file);
 				}
 				array.length();
 			}
 			else {
 				String reason = "";
 				if (json.has("reason")) {
 					reason = json.getString("reason");
 				} else if (json.has("errno")) {
 					JSONObject err = json.getJSONObject("errno");
 					reason = err.getString("key");
 				}
 				throw new DSMException(reason);
 			}
 		}
 		return container;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#getDetails(com.bigpupdev.synodroid .common.data.Task)
 	 */
 	public TaskDetail getDetails(Task taskP) throws Exception {
 		TaskDetail result = new TaskDetail();
 		// If we are logged on
 		if (server.isConnected()) {
 			QueryBuilder getAllRequest = new QueryBuilder().add("action", "getone").add("taskid", "" + taskP.taskId).add("update", "1");
 			// Execute
 			JSONObject json = null;
 			synchronized (server) {
 				json = server.sendJSONRequest(DM_URI_NEW, getAllRequest.toString(), "GET");
 			}
 			boolean success = json.getBoolean("success");
 			// If successful then build details list
 			if (success) {
 				JSONObject data = json.getJSONObject("data");
 				if (data.has("stime"))
 					result.seedingDate = data.getString("stime");
 				if (data.has("totalpeer"))
 					result.peersTotal = Utils.toLong(data.getString("totalpeer"));
 				if (data.has("currpeer"))
 					result.peersCurrent = Utils.toLong(data.getString("currpeer"));
 				if (data.has("istorrent"))
 					result.isTorrent = data.getBoolean("istorrent");
 				if (data.has("speed")) {
 					Pattern p = Pattern.compile("(((\\d)*\\.(\\d)*) KB/s)");
 					Matcher m = p.matcher(data.getString("speed"));
 					if (m.find() && m.groupCount() >= 2) {
 						result.speedUpload = Utils.toDouble(m.group(2));
 					}
 					if (m.find() && m.groupCount() >= 2) {
 						result.speedDownload = Utils.toDouble(m.group(2));
 					} else {
 						result.speedDownload = result.speedUpload;
 					}
 				}
 				if (data.has("filename"))
 					result.fileName = data.getString("filename");
 				if (data.has("username"))
 					result.userName = data.getString("username");
 				if (data.has("totalpieces"))
 					result.piecesTotal = Utils.toLong(data.getString("totalpieces"));
 				if (data.has("transfered")) {
 					Pattern p = Pattern.compile("((\\d*\\.\\d*)\\s[KMGT]B)");
 					Matcher m = p.matcher(data.getString("transfered"));
 					if (m.find() && m.groupCount() >= 1) {
 						result.bytesUploaded = Utils.fileSizeToBytes(m.group(1));
 					}
 					// If you could find another matching group, it means downmload
 					// informations are present
 					if (m.find() && m.groupCount() >= 1) {
 						result.bytesDownloaded = Utils.fileSizeToBytes(m.group(1));
 					}
 					// Otherwise download informations were the first matching (no upload
 					// information)
 					else {
 						result.bytesDownloaded = result.bytesUploaded;
 						result.bytesUploaded = 0;
 					}
 					result.bytesRatio = 0;
 					if (result.bytesDownloaded != 0) {
 						result.bytesRatio = (int) (((float) result.bytesUploaded / (float) result.bytesDownloaded) * 100);
 					}
 				}
 				if (data.has("seedelapsed"))
 					result.seedingElapsed = data.getInt("seedelapsed");
 				if (data.has("isnzb"))
 					result.isNZB = data.getBoolean("isnzb");
 				if (data.has("destination"))
 					result.destination = data.getString("destination");
 				if (data.has(("url")))
 					result.url = data.getString("url");
 				if (data.has("ctime"))
 					result.creationDate = data.getString("ctime");
 				if (data.has("status"))
 					result.status = data.getString("status");
 				if (data.has("seeding_interval"))
 					result.seedingInterval = data.getInt("seeding_interval");
 				if (data.has("currpieces"))
 					result.piecesCurrent = Utils.toLong(data.getString("currpieces"));
 				if (data.has("id"))
 					result.taskId = data.getInt("id");
 				if (data.has("seeding_ratio"))
 					result.seedingRatio = data.getInt("seeding_ratio");
 				if (data.has("filesize"))
 					result.fileSize = Utils.fileSizeToBytes(data.getString("filesize"));
 				if (data.has("seeders_leechers")) {
 					Pattern p = Pattern.compile("(\\d+)(/)(\\d+)");
 					String v = data.getString("seeders_leechers");
 					Matcher m = p.matcher(v);
 					if (m.find()) {
 						if (m.groupCount() >= 1)
 							result.seeders = Utils.toLong(m.group(1));
 						if (m.groupCount() >= 3)
 							result.leechers = Utils.toLong(m.group(3));
 					}
 				}
 			}
 			// Otherwise throw a exception
 			else {
 				String reason = "";
 				if (json.has("reason")) {
 					reason = json.getString("reason");
 				} else if (json.has("errno")) {
 					JSONObject err = json.getJSONObject("errno");
 					reason = err.getString("key");
 				}
 				throw new DSMException(reason);
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#upload(android.net.Uri)
 	 */
 	public void upload(Uri uriP) throws Exception {
 		// If we are logged on
 		if (server.isConnected()) {
 			if (uriP.getPath() != null) {
 				// Create the multipart
 				MultipartBuilder builder = new MultipartBuilder(BOUNDARY, DEBUG);
 
 				// The upload_type's part
 				builder.addPart(new Part("upload_type").setContent("torrent".getBytes()));
 				// The upload_type's part
 				builder.addPart(new Part("desttext").setContent(getSharedDirectory().getBytes()));
 				// The direction's part
 				builder.addPart(new Part("direction").setContent("ASC".getBytes()));
 				// The field's part
 				builder.addPart(new Part("field").setContent("task_id".getBytes()));
 				
 				// The torrent's part
 				Part filePart = new Part("torrent");
 				filePart.addExtra("filename", uriP.getLastPathSegment());
 				if (uriP.getPath().toLowerCase().endsWith("nzb")){
 					filePart.setContentType("application/octet-stream");
 				}
 				else{
 					filePart.setContentType("application/x-bittorrent");
 				}
 				
 				// Get the stream according to the Uri
 				byte[] buffer = StreamFactory.getStream(uriP);
 
 				// Set the content
 				filePart.setContent(buffer);
 				builder.addPart(filePart);
 				// Execute
 				JSONObject json;
 				synchronized (server) {
 					json = server.sendMultiPart(DM_URI_NEW, builder);
 				}
 				if (json != null){
 					boolean success = json.getBoolean("success");
 					// If successful then build details list
 					if (!success) {
 						String reason = "";
 						if (json.has("reason")) {
 							reason = json.getString("reason");
 						} else if (json.has("errno")) {
 							JSONObject err = json.getJSONObject("errno");
 							reason = err.getString("key");
 						}
 						throw new DSMException(reason);
 					}
 				}
 			}
 		}
 	}
 
 	public void uploadUrl(Uri uriP) throws Exception {
 		// If we are logged on
 		if (server.isConnected()) {
 			if (uriP.toString() != null) {
 				String urls = "[\""+uriP.toString()+"\"]";
 				
 				// Create the builder
 				QueryBuilder builder = new QueryBuilder().add("urls", urls).add("action", "add_url_task");
 				
 				// Execute
 				JSONObject json = null;
 				synchronized (server) {
 					json = server.sendJSONRequest(DM_URI_NEW, builder.toString(), "POST");
 				}
 				boolean success = json.getBoolean("success");
 				if (!success){
 					String reason = "";
 					if (json.has("reason")) {
 						reason = json.getString("reason");
 					} else if (json.has("errno")) {
 						JSONObject err = json.getJSONObject("errno");
 						reason = err.getString("key");
 					}
 					throw new DSMException(reason);
 				}
 			}
 		}
 	}
 
 	public String getMultipartUri(){
 		return DM_URI_NEW;
 	}
 	
 	public String getBoundary(){
 		return BOUNDARY;
 	}
 	
 	public byte[] generateMultipart(Uri uriP, String shared) throws Exception {
 		if (uriP.getPath() != null) {
 			// Create the multipart
 			MultipartBuilder builder = new MultipartBuilder(BOUNDARY, DEBUG);
 
 			// The upload_type's part
 			builder.addPart(new Part("upload_type").setContent("torrent".getBytes()));
 			// The upload_type's part
 			builder.addPart(new Part("desttext").setContent(shared.getBytes()));
 			// The direction's part
 			builder.addPart(new Part("direction").setContent("ASC".getBytes()));
 			// The field's part
 			builder.addPart(new Part("field").setContent("task_id".getBytes()));
 			
 			// The torrent's part
 			Part filePart = new Part("torrent");
 			filePart.addExtra("filename", uriP.getLastPathSegment());
 			if (uriP.getPath().toLowerCase().endsWith("nzb")){
 				filePart.setContentType("application/octet-stream");
 			}
 			else{
 				filePart.setContentType("application/x-bittorrent");
 			}
 			
 			// Get the stream according to the Uri
 			byte[] buffer = StreamFactory.getStream(uriP);
 
 			// Set the content
 			filePart.setContent(buffer);
 			builder.addPart(filePart);
 			
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			builder.writeData(baos);
 			return baos.toByteArray();
 		}
 		return null;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#getTaskProperty(com.bigpupdev.synodroid .common.data.Task)
 	 */
 	public TaskProperties getTaskProperty(Task taskP) throws Exception {
 		// torrent_info.cgi?_dc=1299264441500&action=get_property&task_id=2001
 		QueryBuilder getPropReq = new QueryBuilder().add("action", "get_property").add("task_id", "" + taskP.taskId);
 
 		TaskProperties output = new TaskProperties();
 		output.id = "" + taskP.taskId;
 
 		JSONObject json = null;
 		synchronized (server) {
 			json = server.sendJSONRequest(TORRENT_INFO, getPropReq.toString(), "POST");
 		}
 		boolean success = json.getBoolean("success");
 		// If not successful then throw an exception
 		if (!success) {
 			String reason = "";
 			if (json.has("reason")) {
 				reason = json.getString("reason");
 			} else if (json.has("errno")) {
 				JSONObject err = json.getJSONObject("errno");
 				reason = err.getString("key");
 			}
 			throw new DSMException(reason);
 		} else {
 			try {
 				if (json.has("ul_rate")) {
 					output.ul_rate = json.getInt("ul_rate");
 				}
 				if (json.has("dl_rate")) {
 					output.dl_rate = json.getInt("dl_rate");
 				}
 				if (json.has("max_peers")) {
 					output.max_peers = json.getInt("max_peers");
 				}
 				if (json.has("priority")) {
 					output.priority = json.getInt("priority");
 				}
 				if (json.has("seeding_ratio")) {
 					output.seeding_ratio = json.getInt("seeding_ratio");
 				}
 				if (json.has("seeding_interval")) {
 					output.seeding_interval = json.getInt("seeding_interval");
 				}
 				if (json.has("destination")) {
 					output.destination = json.getString("destination");
 				}
 
 			} catch (JSONException e) {
 			}
 
 		}
 		return output;
 	}
 
 	public void setTaskProperty(final Task taskP, int ul_rate, int dl_rate, int priority, int max_peers, String destination, int seeding_ratio, int seeding_interval) throws Exception {
 		// action=set_property
 		// &task_ids=[2001]
 		// &settings={"ext-comp-1434":"","ul_rate":5,"dl_rate":5,"priority":-1,"max_peers":100,"destination":"upload","seeding_ratio":25,"seeding_interval":90}'
 
 		// Create the JSON request
 		QueryBuilder updateTaskRequest = new QueryBuilder().add("action", "set_property").add("task_ids", "[" + taskP.taskId + "]");
 		JSONObject data = new JSONObject();
 
 		data.put("ext-comp-1434", "");
 		data.put("ul_rate", ul_rate);
 		data.put("dl_rate", dl_rate);
 		data.put("priority", priority);
 		data.put("max_peers", max_peers);
 		data.put("destination", destination);
 		data.put("seeding_ratio", seeding_ratio);
 		data.put("seeding_interval", seeding_interval);
 
 		updateTaskRequest.add("settings", data.toString());
 
 		// Execute it to the server
 		JSONObject json = null;
 		synchronized (server) {
 			json = server.sendJSONRequest(TORRENT_INFO, updateTaskRequest.toString(), "POST");
 		}
 		boolean success = json.getBoolean("success");
 		// If not successful then throw an exception
 		if (!success) {
 			String reason = "";
 			if (json.has("reason")) {
 				reason = json.getString("reason");
 			} else if (json.has("errno")) {
 				JSONObject err = json.getJSONObject("errno");
 				reason = err.getString("key");
 			}
 			throw new DSMException(reason);
 		}
 
 	}
 
 	public void setFilePriority(final Task taskP, List<TaskFile> filesP) throws Exception {
 		// action=set_files_priority&task_id=2001&priority=skip&indexes=%5B1%5D
 		List<Integer> l_skip = new ArrayList<Integer>();
 		List<Integer> l_normal = new ArrayList<Integer>();
 
 		for (TaskFile taskFile : filesP) {
 			if (taskFile.download) {
 				l_normal.add(taskFile.id);
 			} else {
 				l_skip.add(taskFile.id);
 			}
 		}
 
 		if (l_skip.size() != 0) {
 			QueryBuilder updateSkipped = new QueryBuilder().add("action", "set_files_priority").add("task_id", "" + taskP.taskId).add("priority", "skip").add("indexes", l_skip.toString());
 
 			// Execute it to the server
 			JSONObject json = null;
 			synchronized (server) {
 				json = server.sendJSONRequest(TORRENT_INFO, updateSkipped.toString(), "POST");
 			}
 			boolean success = json.getBoolean("success");
 			// If not successful then throw an exception
 			if (!success) {
 				String reason = "";
 				if (json.has("reason")) {
 					reason = json.getString("reason");
 				} else if (json.has("errno")) {
 					JSONObject err = json.getJSONObject("errno");
 					reason = err.getString("key");
 				}
 				throw new DSMException(reason);
 			}
 		}
 
 		if (l_normal.size() != 0) {
 			QueryBuilder updateNormal = new QueryBuilder().add("action", "set_files_priority").add("task_id", "" + taskP.taskId).add("priority", "1").add("indexes", l_normal.toString());
 
 			// Execute it to the server
 			JSONObject json = null;
 			synchronized (server) {
 				json = server.sendJSONRequest(TORRENT_INFO, updateNormal.toString(), "POST");
 			}
 			boolean success = json.getBoolean("success");
 			// If not successful then throw an exception
 			if (!success) {
 				String reason = "";
 				if (json.has("reason")) {
 					reason = json.getString("reason");
 				} else if (json.has("errno")) {
 					JSONObject err = json.getJSONObject("errno");
 					reason = err.getString("key");
 				}
 				throw new DSMException(reason);
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#updateTask(com.bigpupdev.synodroid .common.data.Task, java.util.List, int, int)
 	 */
 	public void updateTask(Task taskP, List<TaskFile> filesP, int seedingRatioP, int seedingIntervalP) throws Exception {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#enumSharedDirectory()
 	 */
 	public List<SharedDirectory> enumSharedDirectory() throws Exception {
 		List<SharedDirectory> result = new ArrayList<SharedDirectory>();
 		// Create the JSON request
 		QueryBuilder updateTaskRequest = new QueryBuilder().add("action", "enumshares");
 		// Execute it to the server
 		JSONObject json = null;
 		synchronized (server) {
 			json = server.sendJSONRequest(DM_URI_NEW, updateTaskRequest.toString(), "GET");
 		}
 		boolean success = json.getBoolean("success");
 		// If request succeded
 		if (success) {
 			JSONArray array = json.getJSONArray("items");
 			for (int iLoop = 0; iLoop < array.length(); iLoop++) {
 				JSONObject obj = array.getJSONObject(iLoop);
 				// Create the file
 				SharedDirectory dir = new SharedDirectory(obj.getString("name"));
 				dir.description = obj.getString("description");
 				result.add(dir);
 			}
 		}
 		// If not successful then throw an exception
 		else {
 			String reason = "";
 			if (json.has("reason")) {
 				reason = json.getString("reason");
 			} else if (json.has("errno")) {
 				JSONObject err = json.getJSONObject("errno");
 				reason = err.getString("key");
 			}
 			throw new DSMException(reason);
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#setSharedDirectory(java.lang .String)
 	 */
 	public void setSharedDirectory(Task taskP, String directoryP) throws Exception {
 		// If we are logged on
 		if (server.isConnected()) {
 			if (taskP != null) {
 				QueryBuilder setShared = new QueryBuilder().add("action", "set_destination").add("task_ids", "[" + taskP.taskId + "]").add("destination", directoryP);
 				
 				// Execute
 				synchronized (server) {
 					server.sendJSONRequest(TORRENT_INFO, setShared.toString(), "POST");
 				}
 			}else{
 				QueryBuilder setShared = new QueryBuilder().add("action", "shareset").add("share", directoryP);
 				
 				// Execute
 				synchronized (server) {
 					server.sendJSONRequest(DM_URI_NEW, setShared.toString(), "POST");
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#getSharedDirectory()
 	 */
 	public String getSharedDirectory() throws Exception {
 		String result = null;
 		// If we are logged on
 		if (server.isConnected()) {
			QueryBuilder getShared = new QueryBuilder().add("action", "shareget");
 			// Execute
 			JSONObject json;
 			synchronized (server) {
 				json = server.sendJSONRequest(DM_URI_NEW, getShared.toString(), "GET");
 			}
 			boolean success = json.getBoolean("success");
 			// If request succeded
 			if (success) {
 				result = json.getString("shareName");
 			}
 			// If not successful then throw an exception
 			else {
 				String reason = "";
 				if (json.has("reason")) {
 					reason = json.getString("reason");
 				} else if (json.has("errno")) {
 					JSONObject err = json.getJSONObject("errno");
 					reason = err.getString("key");
 					// Means that no shared directory has been set currently: don't throw an exception
 					if (reason != null && reason.equals("download_error_user_removed")) {
 						return "";
 					}
 				} else if (json.has("disable_err")) {
 					reason = json.getString("disable_err");
 					if (reason.equals("none") || reason.equals("sharenotfound")){
 						return "";
 					}
 				}
 				throw new DSMException(reason);
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#getOriginalLink(com.bigpupdev .synodroid.common.data.Task)
 	 */
 	public StringBuffer getOriginalFile(Task taskP) throws Exception {
 		StringBuffer result = null;
 		// If we are logged on
 		if (server.isConnected()) {
 			QueryBuilder getOriginal = new QueryBuilder().add("action", "torrent").add("id", "" + taskP.taskId).add("_rn", "" + System.currentTimeMillis());
 			// Execute
 			synchronized (server) {
 				String uri = DM_URI_NEW + "/" + taskP.originalLink;
 				result = server.download(uri, getOriginal.toString());
 			}
 		}
 		return result;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.DSHandler#getOriginalLink(com.bigpupdev .synodroid.common.data.Task)
 	 */
 	public String buildOriginalFileString(int taskid) throws Exception {
 		QueryBuilder getOriginal = new QueryBuilder().add("action", "torrent").add("id", "" + taskid).add("_rn", "" + System.currentTimeMillis());
 		return getOriginal.toString();
 	}
 	
 	public List<SearchEngine> getSearchEngines() throws Exception {
 		List<SearchEngine> seList = new ArrayList<SearchEngine>();
 		// If we are logged on
 		if (server.isConnected()) {
 			// Execute
 			JSONObject json = null;
 			synchronized (server) {
 				json = server.sendJSONRequest(INITDATA_URI, "", "GET");
 			}
 			if (json != null){
 				JSONArray jsonArray = null;
 				try{
 					jsonArray = json.getJSONObject("UserSettings").getJSONObject("SYNO.SDS.DownloadStation.Application").getJSONArray("btsearchplugins");
 				}
 				catch (Exception e){
 					jsonArray = new JSONArray();
 				}
 				for (int i = 0; i < jsonArray.length(); i++){
 					SearchEngine se = new SearchEngine();
 					se.enabled = ((JSONObject) jsonArray.get(i)).getBoolean("enable");
 					se.name = ((JSONObject) jsonArray.get(i)).getString("name");
 					seList.add(se);
 				}
 			
 			}
 		}
 		return seList;
 	}
 
 	public void setSearchEngines(List<SearchEngine> seList) throws Exception {
 		JSONArray jArr = new JSONArray();
 		// If we are logged on
 		if (server.isConnected()) {
 			for ( SearchEngine se : seList ){
 				jArr.put(new JSONObject().put("enable", se.enabled).put("name", se.name));
 			}
 			
 			QueryBuilder qBuilder = new QueryBuilder().add("action", "apply").add("data", new JSONObject().put("SYNO.SDS.DownloadStation.Application", new JSONObject().put("btsearchplugins", jArr)).toString());
 			
 			// Execute
 			synchronized (server) {
 				server.sendJSONRequest(USER_SETTINGS, qBuilder.toString(), "POST");
 			}
 		}
 	}
 
 	public List<SearchResult> search(String query, SortOrder order, int start, int limit) throws Exception{
 		List<SearchResult> results = new ArrayList<SearchResult>();
 		List<SearchEngine> seList = getSearchEngines();
 		
 		String plugins = "";
 		for (SearchEngine se : seList){
 			if (se.enabled){
 				if (!plugins.equals("")){
 					plugins += ",";
 				}
 				plugins += se.name;
 			}
 		}
 		
 		QueryBuilder builder = new QueryBuilder().add("action", "search").add("query", query).add("plugins", plugins);
 		JSONObject json = null;
 		json = server.sendJSONRequest(SEARCH_URI, builder.toString(), "GET");
 		if (json != null){
 			if (server.DEBUG) Log.d(Synodroid.DS_TAG, "DSMSearch: Search query '"+query+"' queued for searching on the NAS.");
 			if (json.getBoolean("success") && json.getBoolean("running")){
 				String taskid = json.getString("taskid");
 				boolean stop = false;
 				QueryBuilder rBuilder = new QueryBuilder().add("start", String.valueOf(start)).add("limit", String.valueOf(limit)).add("action", "query").add("dir", "DESC").add("sort", order.equals(SortOrder.BySeeders)?"seeds":"date").add("taskid", taskid).add("categories", "1").add("category", "_allcat_");
 				int loop = 0;
 				while(!stop){
 					json = server.sendJSONRequest(SEARCH_URI, rBuilder.toString(), "GET");
 					if (json != null ){
 						if ( json.getBoolean("success") ){
 							JSONArray items = new JSONArray();
 							boolean running = false;
 							
 							try{ items = json.getJSONArray("items");}
 							catch (Exception e){/*Do Nothing*/}
 							
 							try{ running = json.getBoolean("running");}
 							catch (Exception e){/*Do Nothing*/}
 							
 							if ( !running || items.length() >= limit || loop == MAX_LOOP){
 								if (server.DEBUG) Log.d(Synodroid.DS_TAG, "DSMSearch: Found "+items.length()+" results from the search.");
 								for (int i = 0; i < items.length(); i++){
 									JSONObject item = items.getJSONObject(i);
 									SearchResult sr = new SearchResult(item.getInt("id"), item.getString("title"), item.getString("dlurl"), item.getString("page"), item.getString("size"), item.getString("date"), item.getInt("seeds"), item.getInt("leechs"));
 									results.add(sr);
 								}
 								stop = true;
 							}
 							else{
 								loop ++;
 								Thread.sleep(5000);
 								if (server.DEBUG) Log.d(Synodroid.DS_TAG, "DSMSearch: Still running, not enough results and didn't reach max loop. Waiting 5 seconds for more results...");
 							}
 						}
 						else{
 							loop ++;
 							Thread.sleep(5000);
 							if (server.DEBUG) Log.d(Synodroid.DS_TAG, "DSMSearch: Success == False; Waiting 5 seconds for more results...");
 						}
 					}
 					else{
 						stop = true;
 						if (server.DEBUG) Log.w(Synodroid.DS_TAG, "DSMSearch: Search failed !!");
 					}
 				}
 			}
 		}
 	
 		return results;
 	}
 
 	public List<Folder> getDirectoryListing(String srcPath) throws Exception {
 		List<Folder> result = new ArrayList<Folder>();
 		String backPath = null;
 		if (!srcPath.equals("fm_root")){
 			backPath = srcPath.substring(0, srcPath.lastIndexOf("/"));
 			if (backPath.equals("remote")){
 				backPath = "fm_root";
 			}
 		}	
 		// If we are logged on
 		if (server.isConnected()) {
 			QueryBuilder getDirectory = new QueryBuilder().add("action", "getshares").add("node", srcPath);
 			// Execute
 			JSONArray json;
 			synchronized (server) {
 				try{
 					json = server.sendJSONRequestArray(FILE_URI, getDirectory.toString(), "GET");	
 				}
 				catch (Exception e){
 					Log.e(Synodroid.DS_TAG, "Directory Listing: Cannot go further in the directory.", e);
 					json = new JSONArray();
 				}
 				
 			}
 			if (backPath != null)
 				result.add(new Folder(backPath, "..", backPath));
 			for (int i=0; i<json.length(); i++){
 				if (!json.getJSONObject(i).getString("right").equals("RO")){
 					result.add(new Folder(json.getJSONObject(i).getString("id"), json.getJSONObject(i).getString("text"), json.getJSONObject(i).getString("spath")));
 				}
 			}
 		}
 		return result;
 	}
 }
