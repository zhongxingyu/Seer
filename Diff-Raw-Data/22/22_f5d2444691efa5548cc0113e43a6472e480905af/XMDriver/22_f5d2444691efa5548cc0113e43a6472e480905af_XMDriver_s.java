 package org.haibara.autoxm;
 
import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.haibara.io.DataHandler;
 
 public class XMDriver {
 
 	protected static String root = "";
 	protected static boolean taskComplete = false;
 	protected final static String configDir = "/config/";
 	protected final static String logDir = configDir + "/log/";
 	protected final static String profileDir = configDir + "/profile/";
 	protected final static String userFilePath = configDir + "/user.txt";
 	protected final static String settingFilePath = configDir + "/setting.txt";
 	private static Map<String,String> methodMap = new HashMap<>();
 	static {
 		methodMap.put("pay_attention", "payAttention");
 		methodMap.put("judge_comment", "judgeComment");
 		methodMap.put("post_comment_to_song", "postCommentToSong");		
 	}
 	/*
 	 * 读取 config/user.txt，获取user数据
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	private static Map<String, List<Map<String, String>>> loadUser(String path) {
 		Map<String, List<Map<String, String>>> ret = new HashMap<String, List<Map<String, String>>>();
 		try {
 			String curMode = "null";
 			ret.put(curMode, new ArrayList<Map<String, String>>());
 			List<String> content = (List<String>) DataHandler.readFile(path,
 					"list");
 			for (String line : content) {
 				String piece = line.trim();
 				if (piece.isEmpty() || piece.startsWith("#")) {
 					continue;
 				}
 				if ("dj".equals(piece.toLowerCase())) {
 					curMode = "dj";
 					ret.put(curMode, new ArrayList<Map<String, String>>());
 					continue;
 				} else if ("audience".equals(piece.toLowerCase())) {
 					curMode = "audience";
 					ret.put(curMode, new ArrayList<Map<String, String>>());
 					continue;
 				}
 				String[] elements = piece.split(",");
 				if (elements.length != 2) {
 					System.err.println("invalid element format: " + piece);
 					continue;
 				}
 				Map<String, String> temp = new HashMap<String, String>();
 				for (String element : elements) {
 					String[] kv = element.trim().split("=");
 					if (kv.length != 2) {
 						System.err.println("invalid kv format: " + element);
 						continue;
 					}
 					temp.put(kv[0].trim(), kv[1].trim());
 				}
 				ret.get(curMode).add(temp);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/*
 	 * 读取setting.txt文件，#行为注释，跳过
 	 */
 	@SuppressWarnings("unchecked")
 	public static Map<String, String> loadSetting(String path) {
 		List<String> content = new ArrayList<String>();
 		try {
			content = (List<String>) DataHandler.readFile(path, "list");
 		} catch (Exception e) {
 			System.err.println("Load setting.txt error");
 			e.printStackTrace();
 		}
 		Map<String, String> ret = new HashMap<String, String>();
 		for (String line : content) {
 			String piece = line.trim();			
 			if (piece.isEmpty() || piece.startsWith("#")) {
 				continue;
 			}
 			String[] kv = piece.split("=");
 			if (kv.length != 2) {
 				System.err.println("invalid element format: " + piece);
 				continue;
 			}
 			ret.put(kv[0].trim(), kv[1].trim());
 		}
 		return ret;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static Map<String, String> loadProfile(String dir) {
 		Map<String, String> ret = new HashMap<String, String>();
 		for (String file : DataHandler.listFileName(dir)) {
 			try {
 				List<String> profile = (List<String>) DataHandler.readFile(root
 						+ profileDir + file, "list");
 				for (String line : profile) {
 					if (line.startsWith("account=")) {
 						ret.put(line.substring(line.indexOf("=") + 1).trim(), file.trim());
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return ret;
 	}
 
 	private static SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
 
 	@SuppressWarnings({ "unchecked" })
 	private static int loadLoopLog(String filename, int maxLoop) {
 		if (DataHandler.getFileSize(filename) <= 0) {
 			return -1;
 		}
 		try {
 			List<String> content = (List<String>) DataHandler.readFile(
 					filename, "list");
 			String lastDay = content.get(content.size() - 1).substring(0,
 					content.get(content.size() - 1).indexOf(" "));
 			String curDay = day.format(new Date());
 			if (curDay.equals(lastDay)) {
 				String lastLine = "";
 				int lastLoop = -1;
 				for (int i = content.size() - 1; i >= 0; i--) {
 					if (content.get(i).startsWith(curDay)) {
 						lastLine = content.get(i);
 						if (i == content.size() - 1) {
 							lastLoop = Integer.parseInt(lastLine
 									.substring(lastLine.indexOf("=") + 1));
 						}
 						if (i == 0) {
 							int startLoop = Integer.parseInt(lastLine
 									.substring(lastLine.indexOf("=") + 1));
 							if (lastLoop - startLoop >= maxLoop) {
 								return Integer.MAX_VALUE;
 							} else {
 								return startLoop;
 							}
 						}
 					} else {
 						int startLoop = Integer.parseInt(content.get(i + 1)
 								.substring(lastLine.indexOf("=") + 1));
 						if (lastLoop - startLoop >= maxLoop) {
 							return Integer.MAX_VALUE;
 						} else {
 							return startLoop;
 						}
 					}
 				}
 			} else {
 				return -1;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public void setAudienceChat(String message) {
 		for (XMRobot robot : audienceList) {
 			if (robot != null) {
 				robot.setChatMessage(message.trim());
 			}
 		}
 	}
 
 	public void setAudienceChatPeriod(int period) {
 		if (period <= 0) {
 			return;
 		}
 		for (XMRobot robot : audienceList) {
 			if (robot != null) {
 				robot.setChatPeriod(period * 1000);
 			}
 		}
 	}
 
 	public void setAudienceRatePeriod(int period) {
 		if (period <= 0) {
 			return;
 		}
 		for (XMRobot robot : audienceList) {
 			if (robot != null) {
 				robot.setRatePeriod(period * 1000);
 			}
 		}
 	}
 
 	public void setAudienceRate(int rate) {
 		for (XMAudience robot : audienceList) {
 			if (robot != null) {
 				robot.setRate(rate);
 			}
 		}
 	}
 
 	public void stopAudienceChat() {
 		for (XMAudience robot : audienceList) {
 			if (robot != null) {
 				robot.stopChat();
 			}
 		}
 	}
 
 	private int djCount = 0;
 	private int audienceCount = 0;
 	private int mode = -1;
 	private int maxLoop = 0;
 
 	private List<XMDJ> djList = new ArrayList<XMDJ>();
 	private List<XMAudience> audienceList = new ArrayList<XMAudience>();
 	private Map<String, String> djProperties = new HashMap<String, String>();
 	private Map<String, String> audienceProperties = new HashMap<String, String>();
 
 	public void start() throws InterruptedException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		Map<String, List<Map<String, String>>> users = XMDriver.loadUser(root
 				+ userFilePath);
 		Map<String,String> namePwMap = new HashMap<String, String>();
 		for (List<Map<String,String>> tmpUsers : users.values()) {
 			for (Map<String,String> user : tmpUsers) {
 				namePwMap.put(user.get("user"), user.get("password"));
 			}
 		}
 		Map<String, String> settings = XMDriver.loadSetting(root
 				+ settingFilePath);
 		Map<String, String> profiles = XMDriver.loadProfile(root + profileDir);
 		mode = Integer.parseInt(settings.get("mode"));
 		maxLoop = Integer.parseInt(settings.get("dj_max_loop"));
 		djProperties.put("show_log", settings.get("show_dj_log"));
 		djProperties.put("mode", settings.get("mode"));
 		djProperties.put("auto_next_period", settings.get("auto_next_period"));
 		djProperties.put("rate", settings.get("dj_rate"));
 		djProperties.put("room", settings.get("dj_room"));
 		djProperties.put("max_loop_val", settings.get("dj_max_loop"));
 		djProperties.put("chat_period", settings.get("dj_chat_period"));
 		djProperties.put("rate_period", settings.get("dj_rate_period"));
 
 		audienceProperties.put("show_log", settings.get("show_audience_log"));
 		audienceProperties.put("mode", settings.get("mode"));
 		audienceProperties.put("rate", settings.get("audience_rate"));
 		audienceProperties.put("room", settings.get("audience_room"));
 		audienceProperties.put("chat_period",
 				settings.get("audience_chat_period"));
 		audienceProperties.put("rate_period",
 				settings.get("audience_rate_period"));
 
 		if (mode == 0) {
 			djCount = 0;
 			audienceCount = 0;
 		} else if (mode == 1) {
 			djCount = Integer.parseInt(settings.get("dj_count")) > users.get(
 					"dj").size() ? users.get("dj").size() : Integer
 					.parseInt(settings.get("dj_count"));
 		} else if (mode == 2) {
 			audienceCount = Integer.parseInt(settings.get("audience_count")) > users
 					.get("audience").size() ? users.get("audience").size()
 					: Integer.parseInt(settings.get("audience_count"));
 		} else if (mode == 3) {
 			djCount = Integer.parseInt(settings.get("dj_count")) > users.get(
 					"dj").size() ? users.get("dj").size() : Integer
 					.parseInt(settings.get("dj_count"));
 			audienceCount = Integer.parseInt(settings.get("audience_count")) > users
 					.get("audience").size() ? users.get("audience").size()
 					: Integer.parseInt(settings.get("audience_count"));
 		} else {
 			System.err.println("Invalid mode:" + mode);
 		}
 		// process mode 0 situation
 		if (mode == 0) {		
 			int actionUserCount = Integer.parseInt(settings.get("action_user_count"));
 			System.out.println("action user count:" + actionUserCount);
 			String[] todoList = settings.get("action").split("\\|");
 			for (int i = 0; i < todoList.length; i++) {
 				todoList[i] = todoList[i].trim();
 			}
 			int counter = 0;			
 			List<Map<String,String>> userList = new ArrayList<Map<String,String>>();
 			for (Map<String, String> user : users.get("dj")) {
 				counter++;
 				if (counter > actionUserCount) {
 					break;
 				}
 				userList.add(user);
 			}
 			counter = userList.size();
 			for (Map<String, String> user : users.get("audience")) {
 				counter++;
 				if (counter > actionUserCount) {
 					break;
 				}
 				userList.add(user);				
 			}
 			List<Method> todoMethodList = new ArrayList<Method>();
 			Map<Method,List<XMRobot>> methodInvokerMap = new HashMap<Method, List<XMRobot>>();
 			Map<Method,String[]> methodParamsMap = new HashMap<Method, String[]>();
 			Pattern quotaPattern = Pattern.compile("\"(.*?)\"");
 			for(String todo : todoList) {
				String todoConfigStr = settings.get(todo);
 				Matcher m = quotaPattern.matcher(todoConfigStr);
 				List<String> tmpConf = new ArrayList<String>();
 				while (m.find()) {
 					tmpConf.add(m.group(1));
 					todoConfigStr = todoConfigStr.replace(m.group(0), "[param]");
 				}
 				String[] todoConfig = todoConfigStr.split(":");
 				int tmpConfCounter = 0;
 				for (int i = 0; i < todoConfig.length; i++) {
 					if ("[param]".equals(todoConfig[i])) {
 						todoConfig[i] = tmpConf.get(tmpConfCounter);
 						tmpConfCounter++;
 					}
 				}
 				String robotName = todoConfig[0].trim();
 				String[] methodParams = new String[todoConfig.length-1];
 				Class[] paramTypes = new Class[methodParams.length];
 				for (int i = 1; i < todoConfig.length; i++){
 					methodParams[i-1] = todoConfig[i].trim();
 					paramTypes[i-1] = String.class;
 				}
 				Method todoMethod = XMRobot.class.getMethod(methodMap.get(todo),paramTypes);				
 				List<XMRobot> robotList = new ArrayList<XMRobot>();
 				if (!"[user]".equals(robotName)){
 					robotList.add(new XMRobot(robotName, namePwMap.get(robotName), null));
 				} else {
 					for (Map<String,String> user : userList) {
 						robotList.add(new XMRobot(user.get("user"), user.get("password"), null));
 					}
 				}
 				todoMethodList.add(todoMethod);
 				methodInvokerMap.put(todoMethod, robotList);
 				methodParamsMap.put(todoMethod, methodParams);
 			}
 			(new Thread(new XMTaskThread(todoMethodList,methodInvokerMap,methodParamsMap))).start();
 			return;
 		}
 		
 		System.out.println("dj count:" + djCount);
 		System.out.println("audience count:" + audienceCount);
 		// start dj		
 		Set<String> djSet = new HashSet<String>();		
 		for (Map<String, String> user : users.get("dj")) {
 			if (djSet.size() >= djCount) {
 				break;
 			}
 			djSet.add(user.get("user"));
 			int startLoopVal = loadLoopLog(
 					root + logDir + "." + profiles.get(user.get("user")),
 					maxLoop);
 			if (startLoopVal == Integer.MAX_VALUE) {
 				System.out.println(profiles.get(user.get("user"))
 						+ " task completed");
 				continue;
 			}
 			djProperties.put("start_loop_val", startLoopVal + "");
 			XMDJ robot = new XMDJ(user.get("user"), user.get("password"),
 					djProperties);
 			djList.add(robot);
 		}
 		for (int i = 0; i < djList.size() - 1; i++) {
 			djList.get(i).addCascadeDJ(djList.get(i + 1));
 		}
 		if (djList.size() > 0) {
 			djList.get(djList.size() - 1).setXMDriver(this);
 			new Thread(djList.get(0)).start();
 		} else if (mode == 3 || mode == 1) {
 			taskComplete = true;
 			this.notifyExit();
 			return;
 		}
 		// start audience
 		Set<String> audienceSet = new HashSet<String>();
 		for (Map<String, String> user : users.get("audience")) {
 			if (audienceSet.size() >= audienceCount) {
 				break;
 			}
 			audienceSet.add(user.get("user"));
 			if (djSet.contains(user.get("user"))) {
 				continue;
 			}
 			XMAudience robot = new XMAudience(user.get("user"),
 					user.get("password"), audienceProperties);
 			audienceList.add(robot);
 			new Thread(robot).start();
 			Thread.sleep(Integer.parseInt(settings.get("login_gap")));
 		}
 		if (audienceList.size() == 0 && mode == 2) {
 			taskComplete = true;
 			this.notifyExit();
 			return;
 		}
 	}
 
 	private MainFrame notifier = null;
 
 	public void setNotifier(MainFrame frame) {
 		this.notifier = frame;
 	}
 
 	protected void notifyExit() {
 		if (this.notifier != null) {
 			this.notifier.exit();
 		}
 	}
 
 	public XMDJ getCurrentDJ() {
 		for (XMDJ dj : djList) {
 			if (dj != null && dj.atRoom) {
 				return dj;
 			}
 		}
 		return null;
 	}
 
 	protected void exit() {
 		this.stopALLAudience();
 		this.notifyExit();
 	}
 
 	public void stopALLAudience() {
 		for (XMAudience robot : audienceList) {
 			if (robot != null) {
 				robot.stopWork();
 				robot.stopConnect();
 			}
 		}
 	}
 }
