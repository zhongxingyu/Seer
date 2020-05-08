 package contest.lab.gala;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import contest.lab.gala.callback.GetDamagedCallback;
 import contest.lab.gala.callback.JoinCallback;
 import contest.lab.gala.callback.LoginCallback;
 import contest.lab.gala.callback.OnGameEndedCallback;
 import contest.lab.gala.callback.OnLogout;
 import contest.lab.gala.callback.OnMatchedCallback;
 import contest.lab.gala.callback.RequestFriendsCallback;
 import contest.lab.gala.data.SkillType;
 import contest.lab.gala.data.User;
import contest.lab.gala.util.CommonUtils;
 
 public class NetworkManager {
 	public static NetworkManager getInstance() {
 		if (_instance == null) {
 			_instance = new NetworkManager();
 			_instance.debug("make Instance");
 		}
 		return _instance;
 	}
 	
 	public NetworkManager() {
 		checkSocketAndStart();
 	}
 	
 	public void doLogin(final String id, final String password, final LoginCallback callback) {
 		this.loginCallback = callback;
 		
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(KEY_TYPE, TYPE_LOGIN);
 		map.put("id", id);
 		map.put("password", password);
 		sendJSONWithSocket(map);
 	}
 	
 	public void doJoin(final String id, final String password, final int selected_character, final JoinCallback callback) {
 		this.joinCallback = callback;
 		
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(KEY_TYPE, TYPE_JOIN);
 		map.put("id", id);
 		map.put("password", password);
 		map.put("character", Integer.toString(selected_character));
 		sendJSONWithSocket(map);
 	}
 	
 	public void doLogout(final OnLogout callback) {
		this.onLogout = callback;
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(KEY_TYPE, TYPE_LOGOUT);
		sendJSONWithSocket(map);
 	}
 	
 	public void requestRandomMatching(OnMatchedCallback callback) {
 		this.onMatchedCallback = callback;
 		
 		// make JSON data
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(KEY_TYPE, TYPE_REQUEST_MATCHING);
 		sendJSONWithSocket(map);
 	}
 	
 	public void requestMatchingWithFriend(String friend_id, OnMatchedCallback callback) {
 		this.onMatchedCallback = callback;
 		
 		// make JSON data
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(KEY_TYPE, TYPE_REQUEST_MATCHING);
 		map.put("friend_id", friend_id);
 		sendJSONWithSocket(map);
 	}
 	
 	public void sendAttack(SkillType skillType) {
 		debug("sendAttack started");
 		
 		// make JSON data
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(KEY_TYPE, TYPE_SEND_ATTACK);
 		map.put("skill_type", skillType.toString());
 		sendJSONWithSocket(map);
 		
 		debug("sendAttack ended");
 	}
 	
 	public void requestFriends(RequestFriendsCallback callback) {
 		this.requestFriendsCallback = callback;
 		
 		// make JSON data
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(KEY_TYPE, TYPE_REQUEST_FRIENDS);
 		sendJSONWithSocket(map);
 	}
 	
 	public void setGetDamagedCallback(GetDamagedCallback callback) {
 		this.getDamagedCallback = callback;
 	}
 	
 	public void setGameEndedCallback(OnGameEndedCallback callback) {
 		this.onGameEndedCallback = callback;
 	}
 	
 	public void setOnMatchedCallback(OnMatchedCallback callback) {
 		this.onMatchedCallback = callback;
 	}
 	
 	private void gameEndRequest() {
 		
 		// make JSON data
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(KEY_TYPE, TYPE_GAME_END_CHECK);
 		sendJSONWithSocket(map);
 	}
 	
 	// private -----------------------------------------------------------------
 	
 //	private static final String domainString = "http://soma2.vps.phps.kr:4000";
 //	private static final String loginPath = domainString + "/login";
 //	private static final String joinPath = domainString + "/join";
 //	private static final String getRankingPath = domainString + "/get_ranking";
 	private static final String socketIpString = "27.102.204.239";
 	private static final int socketPort = 1234;
 	
 	
 	private static final String KEY_TYPE = "type";
 	private static final String TYPE_LOGIN = "login";
 	private static final String TYPE_JOIN = "join";
 	private static final String TYPE_REQUEST_FRIENDS = "request_friends";
 	private static final String TYPE_REQUEST_MATCHING = "request_matching";
 	private static final String TYPE_GAME_END_CHECK = "game_end_check";
 	private static final String TYPE_GAME_END = "game_end";
 	private static final String TYPE_LOGOUT = "logout";
 	
 	// SEND
 	private static final String TYPE_SEND_ATTACK = "attack_skill";
 	
 	// RECEIVE
 	private static final String TYPE_GET_DAMAGED = "attack_skill";
 	
 	private OnMatchedCallback onMatchedCallback = null;
 	private JoinCallback joinCallback = null;
 	private LoginCallback loginCallback = null;
 	private GetDamagedCallback getDamagedCallback = null;
 	private OnGameEndedCallback onGameEndedCallback = null;
 	private RequestFriendsCallback requestFriendsCallback = null;
 	private OnLogout onLogout = null;
 	
 	private static NetworkManager _instance = null;
 	private Socket socket = null;
 	private BufferedWriter networkWriter = null;
 	private BufferedReader networkReader = null;
 	
 	private void makeSocketConnection() {
 		try {
 			socket = new Socket(socketIpString, socketPort);
 			
 			networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 			networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			
 			debug("makeSocketConnection completed");
 			
 			startReadingThread();
 		} catch (UnknownHostException e) {
 			debug("UnknownHostException");
 			debug(e.getStackTrace().toString());
 			e.printStackTrace();
 		} catch (IOException e) {
 			debug("IOException");
 			debug(e.getStackTrace().toString());
 			e.printStackTrace();
 		}
 	}
 	
 
 	private boolean checkIfSocketUnabled() {
		if (socket == null || socket.isClosed() || networkWriter == null) {
 			return false;
 		}
 		debug("socket.isInputShutdown() : " + socket.isInputShutdown());
 		debug("socket.isOutputShutdown() : " + socket.isOutputShutdown());
 		return true;
 	}
 	
 	private void checkSocketAndStart() {
 		// start socket
 		if (checkIfSocketUnabled()) {
 			startSocket(null);
 		}		
 	}
 	
 	Thread makeSocketConnectionThread = null;
 	Queue<String> messageQueue = new LinkedList<String>();
 	private void startSocket(String message) {
 		debug("startSocket");
 		
 		if (message != null) {
 			messageQueue.offer(message);
 		}
 		
 		if (makeSocketConnectionThread == null) {
 
 			makeSocketConnectionThread = new Thread(new Runnable() {  
 				@Override
 				public void run() {
 					makeSocketConnection();
 					
 					makeSocketConnectionThread = null;
 					
 					while (!messageQueue.isEmpty()) {
 						String message = messageQueue.poll();
 						if (message != null) {
 							sendStringWithSocketDirectly(message);
 						}	
 					}
 				}
 			});
 			
 			makeSocketConnectionThread.start();
 		}
 	}
 	
 	
 	Thread readingThread = null;
 	private void startReadingThread() {
 		readingThread = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				debug("startReadingThread");
 				
 				String line;
 //				String text = "";
                 while (true) {
                 	try {
                 		if (networkReader != null) {
                 			line = networkReader.readLine();
 //    						text += line;
     						
     						if (line != null && line.length() > 0) {
     							debug("line : " + line);
     							try {
     								JSONObject receivedData = new JSONObject(line);
     								
     								parseReceivedData(receivedData);
     							} catch (JSONException e) {
     								e.printStackTrace();
     							} 
     						}							
 						} else {
 							break;
 						}
 					} catch (IOException e) {
 						e.printStackTrace();
 						break;
 					}
                 }
 				
 			}
 		});
 		readingThread.start();
 	}
 	
 	private void parseReceivedData(JSONObject jsonObject) {
 		try {
 			String dataTypeString = jsonObject.getString("type");
 			
 			debug("dataTypeString : " + dataTypeString);
 			debug("jsonObject : " + jsonObject.toString());
 			
 			if (dataTypeString != null) {
 				if (dataTypeString.equalsIgnoreCase(TYPE_GET_DAMAGED)) {
 					String skillTypeString = jsonObject.getString("skill_type");
 					int skillType = Integer.parseInt(skillTypeString);
 					
 					if (this.getDamagedCallback != null) {
 						this.getDamagedCallback.didGetDamaged(SkillType.parseInt(skillType));						
 					}
 				} else if(dataTypeString.equalsIgnoreCase(TYPE_LOGIN)) {
 					if (this.loginCallback != null) {
 						if (jsonObject.getString("status").equalsIgnoreCase("success")) {
 							String userString = jsonObject.getString("user");
 							JSONObject userJsonObject = new JSONObject(userString);
 							String id = userJsonObject.getString("login_id");
 							int character = Integer.parseInt(userJsonObject.getString("character"));
 							int number_of_wins = userJsonObject.getInt("number_of_wins");
 							int total_wins = userJsonObject.getInt("total_wins");
 							int total_loses = userJsonObject.getInt("total_loses");
 							boolean is_logon = true;
 							
 							this.loginCallback.didSuccessLogin(new User(id, character, number_of_wins, is_logon, total_wins, total_loses));
 						} else {
 							this.loginCallback.didFailedLogin(jsonObject.getString("message"));
 						}		
 						this.loginCallback = null;
 					}
 				} else if(dataTypeString.equalsIgnoreCase(TYPE_JOIN)) {
 					if (this.joinCallback != null) {
 						if (jsonObject.getString("status").equalsIgnoreCase("success")) {
 							String userString = jsonObject.getString("user");
 							JSONObject userJsonObject = new JSONObject(userString);
 							String id = userJsonObject.getString("login_id");
 							int character = Integer.parseInt(userJsonObject.getString("character"));
 							int number_of_wins = userJsonObject.getInt("number_of_wins");
 							boolean is_logon = true;
 							int total_wins = userJsonObject.getInt("total_wins");
 							int total_loses = userJsonObject.getInt("total_loses");
 							
 							this.joinCallback.didSuccessJoin(new User(id, character, number_of_wins, is_logon, total_wins, total_loses));							
 						} else {
 							this.joinCallback.didFailedJoin(jsonObject.getString("message"));
 						}
 						this.joinCallback = null;
 					}
 				} else if(dataTypeString.equalsIgnoreCase(TYPE_REQUEST_FRIENDS)) {
 					if (this.requestFriendsCallback != null) {
 						JSONArray jsonFriends = jsonObject.getJSONArray("friends");
 						
 						ArrayList<User> friends = new ArrayList<User>();
 						for (int i = 0; i < jsonFriends.length(); i++) {
 							JSONObject friend = jsonFriends.getJSONObject(i);
 							friends.add(User.parseJson(friend));
 						}
 						
 						this.requestFriendsCallback.didGetFriends(friends);
 						
 						this.requestFriendsCallback = null;
 					}
 				} else if(dataTypeString.equalsIgnoreCase(TYPE_REQUEST_MATCHING)) {
 					if (this.onMatchedCallback != null) {
 						JSONObject enemy = jsonObject.getJSONObject("user_information");
 						this.onMatchedCallback.onMatched(User.parseJson(enemy));
 						this.onMatchedCallback = null;
 					}
 				} else if(dataTypeString.equalsIgnoreCase(TYPE_GAME_END_CHECK)) {
 					gameEndRequest();
 				} else if(dataTypeString.equalsIgnoreCase(TYPE_GAME_END)) {
 					if (this.onGameEndedCallback != null) {
 						boolean isWin = jsonObject.getString("status").equalsIgnoreCase("win");
 						this.onGameEndedCallback.onGameEnded(isWin, User.parseJson(jsonObject.getJSONObject("user_information")));
 						this.onGameEndedCallback = null;
 					}
 				} else if(dataTypeString.equalsIgnoreCase(TYPE_LOGOUT)) {
 					if (this.onLogout != null) {
 						if (jsonObject.getString("status").equalsIgnoreCase("success")) {
 							this.onLogout.onLogoutSuccess();
 							closeSocketConnection();
 						} else {
 							this.onLogout.onLogoutFailed();
 						}
 						this.onLogout = null;
 					}
 				}
 			}
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void sendJSONWithSocket(Map<String, String> map) {
 		// make JSON data
 		
 		JSONObject jsonData = new JSONObject();
 		try {
 			for (Map.Entry<String,String> entry : map.entrySet()) {
 			  String key = entry.getKey();
 			  String value = entry.getValue();
 			  
 			  jsonData.put(key, value);			  
 			}
 			sendStringWithSocket(jsonData.toString());
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void sendStringWithSocketDirectly (String message) {
 		if (networkWriter != null) {
 			PrintWriter out = new PrintWriter(networkWriter, true);
 			out.println(message);
 			out.flush();
 		}
 	}
 	
 	private void sendStringWithSocket (final String message) {
 		debug("message : " + message);
 		
 		if (checkIfSocketUnabled()) {
 			new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					sendStringWithSocketDirectly(message);					
 				}
 			}).start();
 		} else {
 			startSocket(message);
 		}
 		
 			
 	}
 	
	@SuppressWarnings("unused")
 	private void closeSocketConnection() {
 		try {
 			if (networkWriter != null) {
 				networkWriter.close();
 				networkWriter = null;
 			}
 			if (networkReader != null) {
 				networkReader.close();
 				networkReader = null;
 			}
 			if (socket != null) {
 				socket.close();
 				socket = null;
 			}
 				
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void debug(String string) {
 		Log.e("galalab", string);
 	}
 }
