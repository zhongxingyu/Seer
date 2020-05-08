 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.security.SecureRandom;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLException;
 import javax.net.ssl.TrustManager;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.protocol.DSMException;
 import com.bigpupdev.synodroid.protocol.DSMHandlerFactory;
 import com.bigpupdev.synodroid.protocol.MultipartBuilder;
 import com.bigpupdev.synodroid.protocol.ResponseHandler;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllHostNameVerifier;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllTrustManager;
 import com.bigpupdev.synodroid.R;
 import org.json.JSONObject;
 
 import com.bigpupdev.synodroid.action.AddTaskAction;
 import com.bigpupdev.synodroid.action.SynoAction;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.data.SynoProtocol;
 
 import android.net.Uri;
 import android.os.Message;
import android.support.v4.app.Fragment;
 import android.util.Log;
 
 /**
  * This class represents a Synology server. It manages the connection and also the automatic refresh to retrieve the torrent list.
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class SynoServer {
 
 	// The nickname of the server
 	private String nickname = "";
 	// The version of DSM
 	private DSMVersion dsmVersion = DSMVersion.VERSION2_2;
 	// Local connection through WIFI
 	private SynoServerConnection localConnection;
 	// Public connection
 	private SynoServerConnection publicConnection;
 	// The current connection
 	private SynoServerConnection currentConn;
 
 	// The user
 	private String user;
 	// The password
 	private String password;
 	// The sort atttribut
 	private String sortAttribute;
 	// Is the sort ascending
 	private boolean ascending;
 
 	// The recurrent action to execute
 	private SynoAction recurrentAction = null;
 
 	// Are we connected with the server: login+passwd?
 	private boolean connected;
 	// Flag to stop the server's collector
 	private boolean stop;
 	// Flag to pause the thread until it is interrupted
 	private boolean pause;
 	// The DSM protocol handler
 	private DSMHandlerFactory dsmFactory;
 	// The data's collector thread
 	private Thread collector;
 	// Cookies
 	private List<String> cookies;
 
 	// Flag to know is the server has been interrupted while sleeping
 	private boolean interrupted;
 
 	// Binded DownloadActivity
 	private ResponseHandler handler;
 
 	private String lasterror;
 	private boolean DEBUG;
 
 	HashMap<String, String> map = new HashMap<String, String>();
 
 	private void initMap() {
 		map.put("commfail", "Connection failed. Please check your network settings.");
 		map.put("err_creating_volume", "Processing volume settings. Please try again later when it's finished.");
 		map.put("err_pass", "The password is invalid.");
 		map.put("error_baddns", "The value of the DNS server is invalid.");
 		map.put("error_bademail", "Invalid email format.");
 		map.put("error_badgate", "The value of the default gateway is invalid.");
 		map.put("error_badhost", "The server name you entered is invalid, please refer to Help for more information.");
 		map.put("error_badhostname", "Invalid hostname format.");
 		map.put("error_badip", "The IP address you entered is invalid.");
 		map.put("error_badmask", "The subnet mask you entered is invalid.");
 		map.put("error_badport", "The port number should be from 1 to 65535.");
 		map.put("error_badserver", "Failed to apply the network time server setting, the possible reasons are:<br>Cannot find the network time server. Please check if you have assigned a correct DNS server and default gateway, or try to enter the IP instead of a network time server name.<br>The network time server does not exist or is temporarily unavailable.<br>The server you have entered is not a NTP (network time protocol) server.<br>The network connection to the network time server is unstable or in low quality.<br>Please try again after checking the possible reasons above.");
 		map.put("error_emptyhost", "You have not entered the server name.");
 		map.put("error_emptyip", "You have not entered the IP address.");
 		map.put("error_emptymask", "You have not entered the subnet mask.");
 		map.put("error_lock", "The system is updating security information. Please try again later.");
 		map.put("error_no_enough_space", "Operation failed because the available volume size is insufficient.");
 		map.put("error_nogate", "You have not entered the default gateway yet. Are you sure you want to apply these settings?");
 		map.put("error_noiporhostname", "Invalid IP or hostname.");
 		map.put("error_notmatch", "The IP address and the default gateway belong to two different subnets.");
 		map.put("error_system", "The operation failed.");
 		map.put("loadres_fail", "Failed to load the required resources.");
 		map.put("loadsetting_fail", "Failed to load system settings.");
 		map.put("proc_askwait", "It is not proper to execute the specified command now. Please try again later.");
 		map.put("proc_conflict", "The specified command is prohibited now. Please try again later.");
 		map.put("proc_lock", "The server will not perform your request currently because the system resource is being used. Please try again later.");
 		map.put("warn_abort", "The action was aborted.");
 		map.put("download_msg_reach_limit", "Files in Download Queue have exceeded the maximum limit.");
 		map.put("error_task_add_dup_file", "The file you are trying to download already exists in the destination folder.");
 		map.put("error_task_add_exceed_max", "You can add up to 20 links at a time.");
 		map.put("error_task_add_new_dl_failed", "Failed to add new task.");
 		map.put("error_task_not_found", "The task does not exist.");
 		map.put("download_dl_not_enabled", "Download Service is disabled.");
 		map.put("download_empty_input_file", "Please open the file to create.");
 		map.put("download_empty_input_url", "Please enter the URL.");
 		map.put("download_emule_not_enabled", "eMule is not enabled.");
 		map.put("download_end_desc", "This feature is mainly designed for those tasks which cannot be completed or encountered unknown errors.");
 		map.put("download_end_note_finished", "You cannot end this task because the download is complete.");
 		map.put("download_end_note_no_file", "You cannot end this task because the download has not yet started.");
 		map.put("download_end_warning", "Files in this task will be stopped and moved to the selected shared folder. You cannot resume/restart this task after it's being moved.");
 		map.put("download_err_network", "Failed to establish network connection.");
 		map.put("download_err_no_file_to_end", "File(s) not found.");
 		map.put("download_err_no_privilege", "You have no right to see current task.");
 		map.put("download_err_no_such_task", "Invalid task ID or task has been deleted.");
 		map.put("download_err_read_nzb_fail", "Failed to read NZB file.");
 		map.put("download_err_read_torrent_fail", "Failed to read torrent.");
 		map.put("download_err_select_num", "You can only select one task.");
 		map.put("download_err_tmploc_not_exist", "Temporary download location no longer exists or is crashed.");
 		map.put("download_error_bad_urls_found", "One or more invalid links have been found and will be omitted.");
 		map.put("download_error_dl_rss", "Failed to download RSS file. Please make sure the download link and your network settings are correct.");
 		map.put("download_error_exceed_dest_fs_max_size_short", "File cannot exceed 4GB at destination file system.");
 		map.put("download_error_exceed_fs_max_size", "Download service or destination folder is under a FAT file system and cannot store files over 4GB.");
 		map.put("download_error_exceed_fs_max_size_short", "File cannot exceed 4GB.");
 		map.put("download_error_exceed_temp_fs_max_size_short", "File cannot exceed 4GB at location file system.");
 		map.put("download_error_invalid_rss", "The RSS file is empty or invalid.");
 		map.put("download_error_ln_exceed_fs_max_size", "One or more files cannot be moved to the destination folder, because its file system (FAT) does not allow single file size over 4 GB.");
 		map.put("download_error_not_in_dl_schedule", "This service is only available during the time specified in the download schedule.");
 		map.put("download_error_rss_no_selection", "Please select at least one task to download.");
 		map.put("download_error_rss_not_dl", "RSS file not found.");
 		map.put("download_error_server_error", "Unexpected error occurred!");
 		map.put("download_error_share_not_found", "No writeable shared folder found.");
 		map.put("download_error_user_removed", "Your user does not exist or has been removed.");
 		map.put("download_error_wrong_file_extension", "The file extension is incorrect.");
 		map.put("download_error_wrong_file_format", "Incorrect file format.");
 		map.put("download_error_wrong_files_format", "The format of your file(s) is invalid.");
 		map.put("download_error_wrong_rss_url", "The URL should start with http://.");
 		map.put("download_error_wrong_url", "The URL should start with http://, https://, or ftp://.");
 		map.put("download_msg_action_failed", "Failed to finish the requested operation.");
 		map.put("download_msg_ask_help2", "Please consult the system administrator to solve this problem.");
 		map.put("download_msg_end_done_del_err", "Successfully ended the selected task. However, system failed to remove the download task.  Please remove the task manually.");
 		map.put("download_msg_invalid_user", "Invalid user.");
 		map.put("download_msg_reach_limit", "Number of download tasks has reached the maximum limit.");
 		map.put("download_need_auth", "Authentication required");
 		map.put("download_redirect_confirm", "Download Station is not enabled. Do you want to configure the Download Station settings?");
 		map.put("download_task_broken_link", "Broken Link");
 		map.put("download_task_dest_deny", "Shared folder access denied.");
 		map.put("download_task_dest_not_exist", "Shared folder not found.");
 		map.put("download_task_disk_full", "Disk is full");
 		map.put("download_task_downloading", "Downloading");
 		map.put("download_task_error", "Error");
 		map.put("download_task_finished", "Completed");
 		map.put("download_task_finishing", "Finishing");
 		map.put("download_task_hash_checking", "Checking");
 		map.put("download_task_paused", "Paused");
 		map.put("download_task_preseeding", "Prepare seeding");
 		map.put("download_task_quota_reached", "Quota reached.");
 		map.put("download_task_seeding", "Seeding");
 		map.put("download_task_timeout", "Connection Timeout");
 		map.put("download_task_waiting", "Waiting");
 		map.put("download_upload_erro_files", "The content of file(s) is invalid.");
 		map.put("download_upload_exceed_maximum_filesize", "The size of following file(s) exceeds the maximum file size.");
 		map.put("download_upload_zerobyte_filesize", "The following files will not be uploaded because the file size is either 0 KB or over the maximum file size, or unknown error occurs when the files are added to the upload list.");
 		map.put("error_auth", "Authorization Required");
 		map.put("error_bad_field", "Field value is invalid.");
 		map.put("error_encryption_long_path", "The name of the encrypted file or folder should be within 143 characters.");
 		map.put("error_hint", "Click on [_BACK_] to return to the last page. ");
 		map.put("error_long_path", "File/folder name should be within 255 characters.");
 		map.put("error_nochoosefile", "Please choose a file.");
 		map.put("error_noshare", "The selected shared folder does not exist.");
 		map.put("error_page", "The page is not found");
 		map.put("error_page_desc", "Sorry, the page you are looking for is not found.");
 		map.put("error_port_conflict", "One of the port numbers is used by another service. Please choose another port range.");
 		map.put("error_repswd", "Inconsistent password. Please enter it again.");
 		map.put("error_rmvnone", "You have not selected any item to remove.");
 		map.put("error_select_one", "You cannot apply settings to multiple items at the same time.");
 		map.put("error_service_datamove_failed", "Failed to process the operation. Please check system log.");
 		map.put("error_service_start_failed", "Failed to start service. Please check system log.");
 		map.put("error_service_stop_failed", "Failed to stop service. Please check system log.");
 		map.put("error_subject", "Data can not be applied.");
 		map.put("error_system_abnormal_steps", "System partition has crashed. Please back up data and replace the damaged disks as soon as possible. Then reinstall the system with Synology Assistant.");
 		map.put("error_system_abnormal_warning", "Warning");
 		map.put("error_timeout", "Connection Expired.");
 		map.put("error_unknown", "Unknown error occurred.");
 		map.put("error_unknown_desc", "The system failed to perform the requested operation, there is an unknown error.");
 		map.put("error_volume_readonly", "The volume is read only.");
 		map.put("error_cantlogin", "The account or password is invalid. Please try again.");
 		map.put("error_expired", "Your account has been disabled. Please contact the administrator.");
 		map.put("error_guest", "Cannot login as guest. Please use another account to login.");
 		map.put("error_interrupt", "You have signed in on another computer. Please log in again.");
 		map.put("error_noprivilege", "You are not authorized to use this service.");
 		map.put("error_systemfull", "You cannot login to the system because the disk space is full currently. Please restart the system and try again.");
 		map.put("error_timeout", "Connection expired. Please login again.");
 		map.put("error_kad_not_connected", "You are not connected to the Kad network.");
 		map.put("error_search_key_empty", "Please enter a keyword to search for.");
 		map.put("error_search_key_too_short", "The search keyword is too short. Please enter more than two characters.");
 		map.put("error_server_not_connected", "Please connect to a server.");
 		map.put("error_emule_not_running", "eMule service is activating. Please try again later.");
 		map.put("error_invalid_link_found", "One or more invalid ED2K links have been found and will be ignored.");
 		map.put("error_server_add_duplicate", "The server is already on the server list.");
 		map.put("error_server_add_new_server_failed", "Failed to add the new server.");
 		map.put("error_server_empty_ip", "Please enter the IP address.");
 		map.put("error_server_empty_link", "Please enter the ED2K link.");
 		map.put("error_server_empty_port", "Please enter the port number.");
 		map.put("error_server_invalid_link", "Please enter a valid ED2K link.");
 		map.put("error_server_invalid_met_url", "Invalid server.met URL");
 		map.put("error_server_not_found", "Server not found.");
 		map.put("error_server_remove_failed", "Failed to remove the server.");
 		map.put("error_server_update_met_failed", "Failed to update server list.");
 		map.put("error_dl_port_in_used", "This port number is reserved for system use only. Please use other numbers.");
 		map.put("error_dl_same_time", "Start time and stop time should be different.");
 		map.put("error_emule_port_conflict", "TCP port conflicts with the TCP port in BitTorrent.");
 		map.put("error_emule_udp_conflict", "UDP port conflicts with the DHT UDP port in BitTorrent.");
 	}
 
 	/**
 	 * Static intialization of the SSL factory to accept each certificate, even if a certificate is self signed
 	 */
 	static {
 		SSLContext sc;
 		try {
 			sc = SSLContext.getInstance("TLS");
 			sc.init(null, new TrustManager[] { new AcceptAllTrustManager() }, new SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 			HttpsURLConnection.setDefaultHostnameVerifier(new AcceptAllHostNameVerifier());
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * A new SynoServer with no informations
 	 */
 	/*public SynoServer() {
 		initMap();
 		connected = false;
 		stop = false;
 		pause = false;
 		interrupted = false;
 		ascending = true;
 		sortAttribute = "task_id";
 		DEBUG = false;
 	}*/
 
 	/**
 	 * Constructor which set all server's informations. No connection are made when calling the constructor.
 	 */
 	public SynoServer(String nicknameP, SynoServerConnection localConP, SynoServerConnection publicConnP, String userP, String passwordP, boolean debug) {
 		nickname = nicknameP;
 		localConnection = localConP;
 		publicConnection = publicConnP;
 		user = userP;
 		password = passwordP;
 		DEBUG = debug;
 		// Create the appropriated factory
 		dsmFactory = DSMHandlerFactory.getFactory(dsmVersion, this, DEBUG);
 		initMap();
 	}
 
 	/**
 	 * Set a new recurrent action. The collector thread is interrupted to executed the new recurrent action immediatly
 	 * 
 	 * @param handlerP
 	 *            The handler which will receive the response
 	 * @param recurrentActionP
 	 *            the recurrentAction to set
 	 */
 	public void setRecurrentAction(ResponseHandler handlerP, SynoAction recurrentActionP) {
 		bindResponseHandler(handlerP);
 		recurrentAction = recurrentActionP;
 	}
 
 	/**
 	 * Connect to the server. It is a requirement to connect to the NAS server before any attempt to call a method of this class.
 	 * 
 	 * @return
 	 * @throws DSMException
 	 */
 	public void connect(final ResponseHandler handlerP, final List<SynoAction> actionQueueP, boolean publicP) {
 		// Set the connection according to the public or local parameter
 		if (publicP) {
 			currentConn = publicConnection;
 		} else {
 			currentConn = localConnection;
 		}
 
 		bindResponseHandler(handlerP);
 		// If we are not already connected
 		if (!connected) {
 			// Everything is OK, so launch the thread
 			Runnable runnable = new Runnable() {
 				public void run() {
 					try {
 						doConnection(false);
 						// If the action's queue is not empty
 						if (actionQueueP != null) {
 							if (DEBUG) Log.d(Synodroid.DS_TAG, "There are items to execute in the queue...");
 							for (SynoAction taskAction : actionQueueP) {
 								executeAsynchronousAction(handler, taskAction, false);
 							}
 						}
 						// If everything is fine then start to collect informations
 						boolean silentMode = false;
 						while (connected && !stop) {
 							try {
 								// Update the progressbar
 								fireMessage(SynoServer.this.handler, ResponseHandler.MSG_OPERATION_PENDING);
 								// Execute the recurrent action
 								SynoAction toDo = null;
 								synchronized (this) {
 									if (recurrentAction != null) {
 										toDo = recurrentAction;
 									}
 								}
 								if (toDo != null) {
 									toDo.execute(SynoServer.this.handler, SynoServer.this);
 								}
 								// In case we are disconnected before the response is
 								// received
 								if (connected) {
 									// If auto refresh
 									setInterrupted(false);
 									synchronized (this) {
 										if (currentConn.autoRefresh) {
 											// Sleep
 											wait(currentConn.refreshInterval * 1000);
 										} else {
 											wait();
 										}
 									}
 									// If the thread is paused
 									synchronized (this) {
 										if (pause) {
 											silentMode = true;
 											wait();
 										}
 									}
 								}
 							}
 							// Nothing to do. It may be a force refresh after an action!
 							catch (InterruptedException iex) {
 								if (DEBUG) Log.d(Synodroid.DS_TAG, "Been interrupted while sleeping...");
 								setInterrupted(true);
 							}
 							// All others exceptions
 							catch (Exception ex) {
 								// If not in Silent mode and throws it again
 								if (silentMode) {
 									doConnection(silentMode);
 									silentMode = false;
 								} else {
 									throw ex;
 								}
 							}
 						}
 					}
 					// Connection error
 					catch (DSMException e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "DSMException occured", e);
 						try{
 							fireMessage(SynoServer.this.handler, ResponseHandler.MSG_ERROR, translateError(SynoServer.this.handler, e));
 						}catch (Exception err){}
 					}
 					// Programmation exception
 					catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception occured", e);
 						// This is most likely a connection timeout
 						DSMException ex = new DSMException(e);
 						try{
 							fireMessage(SynoServer.this.handler, ResponseHandler.MSG_ERROR, translateError(SynoServer.this.handler, ex));
 						}catch (Exception err){}
 					}
 					// Set the connection to null to force connection next time
 					finally {
 						synchronized (this){
 							connected = false;
 						}
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Server forced to reconnect.");
 					}
 				}
 			};
 			collector = new Thread(runnable, "Synodroid DS collector");
 			collector.start();
 		}
 	}
 
 	/**
 	 * Fo the connection
 	 * 
 	 * @throws Exception
 	 */
 	private void doConnection(boolean silentModeP) throws Exception {
 		// Send a connecting message
 		if (!silentModeP) {
 			fireMessage(handler, ResponseHandler.MSG_CONNECTING);
 		}
 		// Connect: try to...
 		boolean need_shared = !dsmFactory.connect();
 		// Send a connected message
 		if (!silentModeP) {
 			fireMessage(SynoServer.this.handler, ResponseHandler.MSG_CONNECTED);
 		}
 		if (need_shared){
 			fireMessage(SynoServer.this.handler, ResponseHandler.MSG_SHARED_NOT_SET);
 		}
 	}
 
 	/**
 	 * Bind an activity with this current server
 	 * 
 	 * @param activityP
 	 */
 	public void bindResponseHandler(ResponseHandler handlerP) {
 		handler = handlerP;
 	}
 
 	/**
 	 * Disconnect from the server
 	 */
 	synchronized public void disconnect() {
 		connected = false;
 		stop = true;
 		collector.interrupt();
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Server disconnected.");
 	}
 
 	/**
 	 * Saves the last error for future retrieval
 	 */
 	synchronized public void setLastError(String error) {
 		lasterror = error;
 	}
 
 	/**
 	 * Disconnect from the server
 	 */
 	public String getLastError() {
 		return lasterror;
 	}
 
 	/**
 	 * Send a message
 	 */
 	public void fireMessage(ResponseHandler handlerP, int msgP) {
 		fireMessage(handlerP, msgP, null);
 	}
 
 	/**
 	 * Send a message
 	 */
 	public void fireMessage(ResponseHandler handlerP, int msgP, Object objP) {
 		// Send the connecting message
 		Message msg = new Message();
 		msg.what = msgP;
 		msg.obj = objP;
 		handlerP.handleReponse(msg);
 
 	}
 
 	/**
 	 * Translate an error (JSON or technical exception) to a end-user message
 	 * 
 	 * @param Log
 	 */
 	private String translateError(ResponseHandler handlerP, DSMException dsmExP) {
 		String msg = "Can't display error";
 		msg = handlerP.getString(R.string.unknow_reason);
 		// Get the reason
 		String jsoReason = dsmExP.getJsonReason();
 		// If no JSON reason, try to find the reason in the root DSMException
 		if (jsoReason == null && dsmExP.getRootException() != null && dsmExP.getRootException() instanceof DSMException) {
 			jsoReason = ((DSMException) dsmExP.getRootException()).getJsonReason();
 		}
 		
 		// If there's is a wellknown reason
 		if (jsoReason != null) {
 			// Wrong user or password
 			if (jsoReason.equals("error_cantlogin")) {
 				msg = handlerP.getString(R.string.connect_wrong_userpassword);
 			} else if (jsoReason.equals("error_interrupt")) {
 				msg = handlerP.getString(R.string.connect_already_connected);
 			} else if (jsoReason.equals("error_noprivilege")) {
 				msg = handlerP.getString(R.string.connect_cant);
 			} else {
 				String mapMessage = map.get(jsoReason);
 				if (mapMessage == null) {
 					msg += ": " + jsoReason;
 					if (DEBUG) Log.d(Synodroid.DS_TAG, "JSON's error not trapped: " + jsoReason);
 				} else {
 					msg = "DSM Error: " + mapMessage;
 				}
 			}
 		}
 		// Or if there's a wellknown exception
 		else if (dsmExP.getRootException() != null) {
 			if (dsmExP.getRootException() instanceof SocketException) {
 				msg = handlerP.getString(R.string.connect_nohost);
 			} else if (dsmExP.getRootException() instanceof SSLException) {
 				try {
 					msg = MessageFormat.format(handlerP.getString(R.string.connect_ssl_error), new Object[] { dsmExP.getCause().getMessage() });
 				} catch (Exception e) {
 					msg = handlerP.getString(R.string.port_mismatch);
 				}
 			} else if (dsmExP.getRootException() instanceof SocketTimeoutException) {
 				msg = handlerP.getString(R.string.connect_nohost);
 			} else {
 				String m = dsmExP.getRootException().getMessage();
 				if (m != null) {
 					msg = m;
 				}
 			}
 		}
 		else if (jsoReason == null && dsmExP.getRootException() == null && dsmExP.isIDException) {
 			msg = handlerP.getString(dsmExP.getExceptionID());
 		}
 		
 		// Return the message
 		return msg;
 	}
 
 	/**
 	 * Return the string representation of a Synology server
 	 */
 	@Override
 	public String toString() {
 		return (currentConn.protocol.name() + "://" + currentConn.host + ":" + currentConn.port).toLowerCase();
 	}
 
 	/**
 	 * Return the handler factory
 	 * 
 	 * @return
 	 */
 	public DSMHandlerFactory getDSMHandlerFactory() {
 		return dsmFactory;
 	}
 
 	/**
 	 * @return the user
 	 */
 	public String getUser() {
 		return user;
 	}
 
 	/**
 	 * @return the password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return (currentConn.protocol.name() + "://" + currentConn.host + ":" + currentConn.port).toLowerCase();
 	}
 
 	/**
 	 * @return the nickname
 	 */
 	public String getNickname() {
 		return nickname;
 	}
 
 	/**
 	 * @return the protocol
 	 */
 	public SynoProtocol getProtocol() {
 		return currentConn.protocol;
 	}
 
 	/**
 	 * @return the host
 	 */
 	public String getHost() {
 		return currentConn.host;
 	}
 
 	/**
 	 * @return the port
 	 */
 	public Integer getPort() {
 		return currentConn.port;
 	}
 
 	/**
 	 * @return the refreshInterval
 	 */
 	public int getRefreshInterval() {
 		return currentConn.refreshInterval;
 	}
 
 	/**
 	 * @return the dsmVersion
 	 */
 	public DSMVersion getDsmVersion() {
 		return dsmVersion;
 	}
 
 	/**
 	 * @param nickname
 	 *            the nickname to set
 	 */
 	synchronized public void setNickname(String nickname) {
 		this.nickname = nickname;
 		connected = false;
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Server nickname updated.");
 	}
 
 	/**
 	 * @param user
 	 *            the user to set
 	 */
 	synchronized public void setUser(String user) {
 		this.user = user;
 		connected = false;
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Username updated on server.");
 	}
 
 	/**
 	 * @param password
 	 *            the password to set
 	 */
 	synchronized public void setPassword(String password) {
 		this.password = password;
 		connected = false;
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Password updated on server.");
 	}
 
 	/**
 	 * @param dsmVersion
 	 *            the dsmVersion to set
 	 */
 	synchronized public void setDsmVersion(DSMVersion dsmVersion) {
 		this.dsmVersion = dsmVersion;
 		connected = false;
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "DSM Handler updated.");
 		// Create the appropriated factory
 		dsmFactory = DSMHandlerFactory.getFactory(dsmVersion, this, DEBUG);
 	}
 
 	/**
 	 * @param sortAttribute
 	 *            the sortAttribute to set
 	 */
 	synchronized public void setSortAttribute(String sortAttribute) {
 		this.sortAttribute = sortAttribute;
 	}
 
 	/**
 	 * @param ascending
 	 *            the ascending to set
 	 */
 	synchronized public void setAscending(boolean ascending) {
 		this.ascending = ascending;
 	}
 
 	/**
 	 * @return the sortAttribute
 	 */
 	public String getSortAttribute() {
 		return sortAttribute;
 	}
 
 	/**
 	 * @return the ascending
 	 */
 	public boolean isAscending() {
 		return ascending;
 	}
 
 	/**
 	 * Execute an asynchronous action on this server
 	 * 
 	 * @param actionP
 	 *            The action to execute
 	 * @param forceRefreshP
 	 *            Flag to set if a refresh is needed after the completion of the action
 	 */
 	public void executeAsynchronousAction(final ResponseHandler handlerP, final SynoAction actionP, final boolean forceRefreshP) {
 		executeAsynchronousAction(handlerP, actionP, forceRefreshP, true);
 	}
 
 	/**
 	 * Execute an asynchronous action on this server
 	 * 
 	 * @param actionP
 	 *            The action to execute
 	 * @param forceRefreshP
 	 *            Flag to set if a refresh is needed after the completion of the action
 	 */
 	public void executeAsynchronousAction(final ResponseHandler handlerP, final SynoAction actionP, final boolean forceRefreshP, final boolean showToast) {
 		Runnable runnable = new Runnable() {
 			public void run() {
 				// An operation is pending
 				fireMessage(handlerP, ResponseHandler.MSG_OPERATION_PENDING);
 				if (DEBUG) Log.d(Synodroid.DS_TAG, "Executing action: " + actionP.getName());
 				
 				//Kill toast if it is a safe addTaskAction...
 				try{
 					((AddTaskAction) actionP).checkToast(SynoServer.this);
 				} catch (Exception e){}
 				
 				try {
 					// If a Toast must be shown
 					if (actionP.isToastable() && showToast) {
 						int resId = actionP.getToastId();
 						String fileName = (actionP.getTask() != null ? actionP.getTask().fileName : "");
 						String text = handlerP.getString(resId, new Object[] { fileName });
 						fireMessage(handlerP, ResponseHandler.MSG_TOAST, text);
 					}
 					actionP.execute(handlerP, SynoServer.this);
 				} catch (DSMException ex) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected DSM error", ex);
 					try{
 						fireMessage(handlerP, ResponseHandler.MSG_ERROR, SynoServer.this.translateError(SynoServer.this.handler, ex));
 					}catch (Exception err){}
 				} catch (Exception e) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected error", e);
 					DSMException ex = new DSMException(e);
 					try {
 						fireMessage(handlerP, ResponseHandler.MSG_ERROR, SynoServer.this.translateError(SynoServer.this.handler, ex));
 					}catch (Exception err){}
 				} finally {
 					fireMessage(handlerP, ResponseHandler.MSG_OPERATION_DONE);
 					// Interrup the collector's thread so it will refresh
 					// immediatelty
 					if (forceRefreshP) {
 						collector.interrupt();
 					}
 				}
 			}
 		};
		((Fragment)handlerP).getActivity().runOnUiThread(runnable);
		//new Thread(runnable, "Synodroid DS action").start();
 	}
 
 	/**
 	 * Create a connection and add all required cookies information
 	 * 
 	 * @param uriP
 	 * @param requestP
 	 * @param methodP
 	 * @return
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	private HttpURLConnection createConnection(String uriP, String requestP, String methodP) throws MalformedURLException, IOException {
 		// Prepare the connection
 		HttpURLConnection con = (HttpURLConnection) new URL(getUrl() + Uri.encode(uriP, "/")).openConnection();
 
 		// Add cookies if exist
 		if (cookies != null) {
 			for (String cookie : cookies) {
 				con.addRequestProperty("Cookie", cookie);
 				if (DEBUG) Log.d(Synodroid.DS_TAG, "Added cookie: " + cookie);
 			}
 		}
 		con.setDoOutput(true);
 		con.setDoInput(true);
 		con.setUseCaches(false);
 		con.setRequestMethod(methodP);
 		con.setConnectTimeout(20000);
 		if (DEBUG) Log.d(Synodroid.DS_TAG, methodP + ": " + uriP + "?" + requestP);
 		return con;
 	}
 
 	/**
 	 * Send a request to the server.
 	 * 
 	 * @param uriP
 	 *            The part of the URI ie: /webman/doit.cgi
 	 * @param requestP
 	 *            The query in the form 'param1=foo&param2=yes'
 	 * @param methodP
 	 *            The method to send this request
 	 * @return A JSONObject containing the response of the server
 	 * @throws DSMException
 	 */
 	public JSONObject sendJSONRequest(String uriP, String requestP, String methodP) throws Exception {
 		HttpURLConnection con = null;
 		OutputStreamWriter wr = null;
 		BufferedReader br = null;
 		StringBuffer sb = null;
 		try {
 
 			// For some reason in Gingerbread I often get a response code of -1.
 			// Here I retry for a maximum of MAX_RETRY to send the request and it usually succeed at the second try...
 			int retry = 0;
 			int MAX_RETRY = 2;
 			while (retry <= MAX_RETRY) {
 				try{
 					// Create the connection
 					con = createConnection(uriP, requestP, methodP);
 					// Add the parameters
 					wr = new OutputStreamWriter(con.getOutputStream());
 					wr.write(requestP);
 					// Send the request
 					wr.flush();
 					wr.close();
 	
 					// Try to retrieve the session cookie
 					Map<String, List<String>> headers = con.getHeaderFields();
 					List<String> newCookie = headers.get("set-cookie");
 					if (newCookie != null) {
 						synchronized (this){
 							cookies = newCookie;
 						}
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Retreived cookies: " + cookies);
 					}
 					
 					// Now read the reponse and build a string with it
 					br = new BufferedReader(new InputStreamReader(con.getInputStream()));
 					sb = new StringBuffer();
 					String line;
 					while ((line = br.readLine()) != null) {
 						sb.append(line);
 					}
 					br.close();
 					// Verify is response if not -1, otherwise take reason from the header
 					if (con.getResponseCode() == -1) {
 						retry++;
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response code is -1 (retry: " + retry + ")");
 					} else {
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response is: " + sb.toString());
 						JSONObject respJSO = new JSONObject(sb.toString());
 						return respJSO;
 					}
 				}catch (Exception e){
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught exception while contacting the server, retying...", e);
 					retry ++;
 				}
 				
 			}
 			throw new Exception("Failed to read response from server. Please reconnect!");
 		}
 		// Special for SSL Handshake failure
 		catch (IOException ioex) {
 			if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected error", ioex);
 			String msg = ioex.getMessage();
 			if (msg != null && msg.indexOf("SSL handshake failure") != -1) {
 				// Don't need to translate: the opposite message (HTTP on a SSL port) is in english and come from the server
 				throw new Exception("SSL handshake failure.\n\nVerify if you don't speak HTTPS to a standard server port.\n");
 			} else {
 				throw ioex;
 			}
 		}
 		// Unexpected exception
 		catch (Exception ex) {
 			if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected error", ex);
 			throw ex;
 		}
 		// Finally close everything
 		finally {
 			if (con != null) {
 				con.disconnect();
 			}
 			wr = null;
 			br = null;
 			sb = null;
 			con = null;
 		}
 	}
 
 	/**
 	 * Upload a file which is located on the mobile
 	 */
 	public JSONObject sendMultiPart(String uriP, MultipartBuilder multiPartP) throws Exception {
 		HttpURLConnection conn = null;
 		JSONObject respJSO = null;
 		int retry = 0;
 		int MAX_RETRY = 2;
 		try {
 			while (retry <= MAX_RETRY) {
 				try {
 					// Create the connection
 					conn = createConnection(uriP, "", "POST");
 					conn.setRequestProperty("Connection", "keep-alive");
 					conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + multiPartP.getBoundary());
 		
 					// Write the multipart
 					multiPartP.writeData(conn.getOutputStream());
 		
 					// Now read the reponse and build a string with it
 					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 					StringBuffer sb = new StringBuffer();
 					String line;
 					while ((line = br.readLine()) != null) {
 						sb.append(line);
 					}
 					br.close();
 		
 					if (conn.getResponseCode() == -1) {
 						retry++;
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response code is -1 (retry: " + retry + ")");
 					} else {
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response is: " + sb.toString());
 						respJSO = new JSONObject(sb.toString());
 						return respJSO;
 					}
 				} catch (Exception e) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught exception while contacting the server, retying...", e);
 					retry ++;
 				}
 			}
 		}
 		finally {
 			if (conn != null) {
 				conn.disconnect();
 			}
 			conn = null;
 		}
 		throw new Exception("Failed to read response from server. Please reconnect!");
 	}
 
 	public StringBuffer download(String uriP, String requestP) throws Exception {
 		HttpURLConnection con = null;
 		try {
 			// Create the connection
 			con = createConnection(uriP, requestP, "GET");
 			// Add the parameters
 			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
 			wr.write(requestP);
 			// Send the request
 			wr.flush();
 
 			// Try to retrieve the session cookie
 			Map<String, List<String>> headers = con.getHeaderFields();
 			List<String> newCookie = headers.get("set-cookie");
 			if (newCookie != null) {
 				synchronized (this){
 					cookies = newCookie;
 				}
 				if (DEBUG) Log.d(Synodroid.DS_TAG, "Retreived cookies: " + cookies);
 			}
 			// Now read the reponse and build a string with it
 			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			try {
 				while ((line = br.readLine()) != null) {
 					sb.append(line);
 				}
 			} catch (OutOfMemoryError e) {
 				sb = null;
 			}
 
 			br.close();
 			return sb;
 		}
 		// Unexpected exception
 		catch (Exception ex) {
 			if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected error", ex);
 			throw ex;
 		}
 		// Finally close everything
 		finally {
 			if (con != null) {
 				con.disconnect();
 			}
 		}
 	}
 
 	/**
 	 * @return the connected
 	 */
 	public boolean isConnected() {
 		return connected;
 	}
 	
 	/**
 	 * Set server connection flag
 	 */
 	synchronized public void setConnected(boolean status) {
 		connected = status;
 	}
 
 	/**
 	 * Force a refresh by interrupting the sleep
 	 */
 	synchronized public void forceRefresh() {
 		if (collector != null) {
 			collector.interrupt();
 		}
 	}
 
 	/**
 	 * Pause the server's thread
 	 */
 	synchronized public void pause() {
 		pause = true;
 	}
 
 	/**
 	 * Resume the server's thread
 	 */
 	synchronized public void resume() {
 		pause = false;
 		collector.interrupt();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SynoServer other = (SynoServer) obj;
 		if (nickname == null) {
 			if (other.nickname != null)
 				return false;
 		} else if (!nickname.equals(other.nickname))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Does the collector thread is alive
 	 * 
 	 * @return
 	 */
 	public boolean isAlive() {
 		if (collector != null) {
 			return collector.isAlive() && !stop;
 		}
 		return false;
 	}
 
 	/**
 	 * @return the interrupted
 	 */
 	public boolean isInterrupted() {
 		return interrupted;
 	}
 
 	/**
 	 * @param interruptedP
 	 *            the interrupted to set
 	 */
 	synchronized public void setInterrupted(boolean interruptedP) {
 		interrupted = interruptedP;
 	}
 
 	/**
 	 * Return the current connection or null if there's no connection currentlty
 	 * 
 	 * @return
 	 */
 	public SynoServerConnection getConnection() {
 		return currentConn;
 	}
 
 	/**
 	 * @return the localConnection
 	 */
 	public SynoServerConnection getLocalConnection() {
 		return localConnection;
 	}
 
 	/**
 	 * @param localConnectionP
 	 *            the localConnection to set
 	 */
 	synchronized public void setLocalConnection(SynoServerConnection localConnectionP) {
 		localConnection = localConnectionP;
 	}
 
 	/**
 	 * @return the publicConnection
 	 */
 	public SynoServerConnection getPublicConnection() {
 		return publicConnection;
 	}
 
 	/**
 	 * @param publicConnectionP
 	 *            the publicConnection to set
 	 */
 	synchronized public void setPublicConnection(SynoServerConnection publicConnectionP) {
 		publicConnection = publicConnectionP;
 	}
 
 	public ResponseHandler getResponseHandler(){
 		return handler;
 	}
 	
 	public void setDebugLvl(boolean debug) {
 		DEBUG = debug;
 	}
 
 }
