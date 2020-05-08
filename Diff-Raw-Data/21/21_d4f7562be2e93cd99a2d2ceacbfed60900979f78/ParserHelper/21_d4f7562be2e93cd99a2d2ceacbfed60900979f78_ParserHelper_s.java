 package com.magicmicky.habitrpglibrary.onlineapi.helper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.magicmicky.habitrpglibrary.habits.*;
 
 public class ParserHelper {
 	private final static String TAG_ERR="err";
 
 	private final static String TAG_AUTH_API_TOKEN="token";
 	private final static String TAG_AUTH_USER_TOKEN="id";
 	private final static String TAG_REG_API_TOKEN="apiToken";
 	private final static String TAG_REG_USER_TOKEN="id";
 
 	
 	private static final String TAG_habitData = "habitRPGData";
 	private static final String TAG_DAILIESID = "dailyIds";
 	private static final String TAG_HABITSID = "habitIds";
 	private static final String TAG_TODOIDS = "todoIds";
 	private static final String TAG_REWARDIDS = "rewardIds";
 	private static final String TAG_TAGS = "tags";
 		private static final String TAG_TAGS_ID = "id";
 		private static final String TAG_TAGS_NAME = "name";
 	private static final String TAG_STATS = "stats";
 		private static final String TAG_XP = "exp";
 		private static final String TAG_GP = "gp";
 		private static final String TAG_HP = "hp";
 		private static final String TAG_LVL="lvl";
 		private static final String TAG_XP_MAX="toNextLevel";
 		private static final String TAG_HP_MAX = "maxHealth";
 	private static final String TAG_TASKS = "tasks";
 		private static final String TAG_TASK_TYPE = "type";
 		private static final String TAG_TASK_ID = "id";
 		private static final String TAG_TASK_DATE = "date";
 		private static final String TAG_TASK_NOTES = "notes";
 		private static final String TAG_TASK_PRIORITY = "priority";
 		private static final String TAG_TASK_TEXT = "text";
 		private static final String TAG_TASK_VALUE = "value";
 		private static final String TAG_TASK_COMPLETED = "completed";
 		private static final String TAG_TASK_UP = "up";
 		private static final String TAG_TASK_DOWN = "down";
 		private static final String TAG_TASK_REPEAT = "repeat";
 		private static final String TAG_TASK_STREAK="streak";
 		private static final String TAG_TASK_TAGS = "tags";
 		private static final String TAG_TASK_HISTORY="history";
 			private static final String TAG_TASK_HISTORY_DATE = "date";
	private static final String TAG_AUTH = "auth";
		private static final String TAG_AUTH_LOCAL="local";
		private static final String TAG_AUTH_LOCAL_UNAME = "username";
		private static final String TAG_AUTH_FACEBOOK = "facebook";
		private static final String TAG_AUTH_FACEBOOK_DISPLAYNAME = "displayName";
 	private static final String TAG_PREFS = "preferences";
 		private static final String TAG_PREFS_GENDER = "gender";
 		private static final String TAG_PREFS_SKIN = "skin";
 		private static final String TAG_PREFS_HAIR = "hair";
 		private static final String TAG_PREFS_ARMORSET = "armorSet";
 		private static final String TAG_PREFS_SHOWHELM = "showHelm";
 		private static final String TAG_PREFS_DAYSTART = "dayStart";
 		private static final String TAG_PREFS_TIMEZONEOFFSET ="timezoneOffset";
 	private static final String TAG_ITEMS = "items";
 		private static final String TAG_ITEMS_ARMOR = "armor";
 		private static final String TAG_ITEMS_HEAD = "head";
 		private static final String TAG_ITEMS_SHIELD = "shield";
 		private static final String TAG_ITEMS_WEAPON = "weapon";
 
 		//TODO:private static final String TAG_ITEMS_PETS = "pets";
 		private static final String TAG_NOWXP = "exp";
 		private static final String TAG_NOWGP = "gp";
 		private static final String TAG_NOWHP = "hp";
 		private static final String TAG_DELTA = "delta";
 
 		private static final String TAG_TASK_DELETED = "task_deleted";
 		
 		//Hotfix
 		private static final String TAG_DAILIES="dailys";
 		private static final String TAG_HABITS="habits";
 		private static final String TAG_TODOS="todos";
 		private static final String TAG_REWARDS="rewards";
 	
 	public static User parseUser(JSONObject obj) throws ParseErrorException{
 		parseError(obj);
 		if(obj.has(TAG_habitData)) {
 			try {
 				obj = obj.getJSONObject(TAG_habitData);
 			} catch (JSONException e1) {
 				// Can't happen
 				e1.printStackTrace();
 			}
 		}
 		User user = new User();
 		/*
 		 * Parse tasks
 		 */
 			user.setItems(parseTasks(obj));
 		
 		/*
 		 * Parse User infos
 		 */
 		try {
 			parseUserInfos(obj, user);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			//exceptions.add(new ParseErrorException(ParseErrorException.JSON_USER_INFO_NOT_FOUND));
 		}
 		/*
 		 * Parse user look
 		 */
 		try  {
 			user.setLook(parseUserLook(obj));
 		} catch (ParseErrorException e) {
 			e.printStackTrace();
 			//TODO:
 			//exceptions.add(new ParseErrorException(ParseErrorException.JSON_USER_LOOK_NOT_FOUND));
 
 		}
 		/*
 		 * Parse user tags.
 		 */
 		try {
 			user.setTags(parseUserTags(obj.getJSONArray(TAG_TAGS)));
 		} catch (JSONException e) {
 			e.printStackTrace();
 			//exceptions.add(new ParseErrorException(ParseErrorException.JSON_USER_TAGS_NOT_FOUND));
 		}
 		return user;
 		
 	}
 	/**
 	 * Parses the tags from the user
 	 * @param obj the JSONArray containing the tags
 	 * @return a map of {@code <TagId, TagName>}
 	 */
 	private static Map<String, String> parseUserTags(JSONArray obj) {
 		Map<String,String> tags = new HashMap<String,String>();
 		try {
 			for(int i=0;i<obj.length();i++) {
 				JSONObject tag_obj = obj.getJSONObject(i);
 				tags.put(tag_obj.getString(TAG_TAGS_ID), tag_obj.getString(TAG_TAGS_NAME));
 			}
 		} catch(JSONException e) {
 			e.printStackTrace();
 		}
 		return tags;
 	}
 
 	public static UserLook parseUserLook(JSONObject obj) throws ParseErrorException {
 		UserLook look = new UserLook();
 		if(obj.has(TAG_PREFS)) {
 			try {
 				JSONObject prefs;
 				prefs = obj.getJSONObject(TAG_PREFS);
 	
 				if(prefs.has(TAG_PREFS_GENDER))
 					look.setGender(prefs.getString(TAG_PREFS_GENDER));
 				if(prefs.has(TAG_PREFS_SKIN))
 					look.setSkin(prefs.getString(TAG_PREFS_SKIN));
 				if(prefs.has(TAG_PREFS_HAIR))
 					look.setHair(prefs.getString(TAG_PREFS_HAIR));
 				if(prefs.has(TAG_PREFS_ARMORSET))
 					look.setArmorSet(prefs.getString(TAG_PREFS_ARMORSET));
 				if(prefs.has(TAG_PREFS_SHOWHELM))
 					look.setShowHelm(prefs.getBoolean(TAG_PREFS_SHOWHELM));
 			}catch(JSONException e) {
 				throw new ParseErrorException(ParseErrorException.JSON_USER_PREFS_ERROR);
 			}
 		}
 		try {
 			look.setItems(parseUserItems(obj.getJSONObject(TAG_ITEMS)));
 		} catch(JSONException e) {
 			throw new ParseErrorException(ParseErrorException.JSON_USER_HAS_NO_ITEMS);
 		}
 		return look;
 	}
 	public static UserLook.UserItems parseUserItems(JSONObject obj) throws ParseErrorException  {
 		parseError(obj);
 		int armor=0, head=0, shield=0, weapon=0;
 		try {
 			if(obj.has(TAG_ITEMS_ARMOR))
 				armor = (obj.getInt(TAG_ITEMS_ARMOR));
 			if(obj.has(TAG_ITEMS_HEAD))
 				head = (obj.getInt(TAG_ITEMS_HEAD));
 			if(obj.has(TAG_ITEMS_SHIELD))
 				shield = (obj.getInt(TAG_ITEMS_SHIELD));
 			if(obj.has(TAG_ITEMS_WEAPON))
 				weapon = (obj.getInt(TAG_ITEMS_WEAPON));
 		} catch (JSONException e){
 			e.printStackTrace();
 			//Should never happen
 		}
 		return new UserLook.UserItems(armor, head, shield, weapon);
 	}
 	private static void parseUserInfos(JSONObject obj, User user) throws JSONException {
 		if(obj.has(TAG_STATS)) {
 			JSONObject stats = obj.getJSONObject(TAG_STATS);
 			if(stats.has(TAG_LVL))
 				user.setLvl(stats.getInt(TAG_LVL));
 			if(stats.has(TAG_XP))
 				user.setXp(stats.getDouble(TAG_XP));
 			if(stats.has(TAG_XP_MAX))
 				user.setMaxXp(stats.getDouble(TAG_XP_MAX));
 			if(stats.has(TAG_HP))
 				user.setHp(stats.getDouble(TAG_HP));
 			if(stats.has(TAG_HP_MAX))
 				user.setMaxHp(stats.getDouble(TAG_HP_MAX));
 			if(stats.has(TAG_GP))
 				user.setGp(stats.getDouble(TAG_GP));
 		}
 		if(obj.has(TAG_PREFS)) {
 			JSONObject prefs = obj.getJSONObject(TAG_PREFS);
 			if(prefs.has(TAG_PREFS_DAYSTART))
 				user.setDayStart(prefs.getInt(TAG_PREFS_DAYSTART));
 			else
 				user.setDayStart(0);
 			if(prefs.has(TAG_PREFS_TIMEZONEOFFSET))
 				user.setTimeZoneOffset(prefs.getInt(TAG_PREFS_TIMEZONEOFFSET));
 		}
		if(obj.has(TAG_AUTH)) {
			JSONObject auth = obj.getJSONObject(TAG_AUTH);
			if(auth.has(TAG_AUTH_LOCAL)) {
				user.setName(auth.getJSONObject(TAG_AUTH_LOCAL).getString(TAG_AUTH_LOCAL_UNAME));
			} else if(auth.has(TAG_AUTH_FACEBOOK)) {
				user.setName(auth.getJSONObject(TAG_AUTH_FACEBOOK).getString(TAG_AUTH_FACEBOOK_DISPLAYNAME));
			}
 		}
 		
 
 	}
 	/**
 	 * Parse all the tasks of the user
 	 * @param obj the json object of the user
 	 * @return the list of all the tasks of the user
 	 * @throws ParseErrorException when something isn't found.
 	 */
 	public static List<HabitItem> parseTasks(JSONObject obj) throws ParseErrorException {
 		parseError(obj);
 		List<HabitItem> items = new ArrayList<HabitItem>();
 		//First, parse the TASKS tag to find all tasks.
 		JSONObject tasks;
 		//try {
 			try {
 				JSONArray h = obj.getJSONArray(TAG_HABITS);
 				for(int i=0;i<h.length();i++) {
 					items.add(parseHabit(h.getJSONObject(i)));
 				}
 				JSONArray d = obj.getJSONArray(TAG_DAILIES);
 	
 				for(int i=0;i<d.length();i++) {
 					items.add(parseDaily(d.getJSONObject(i)));
 				}
				JSONArray r = obj.getJSONArray(TAG_TODOS);
 				for(int i=0;i<r.length();i++) {
 					items.add(parseReward(r.getJSONObject(i)));
 				}
 				JSONArray t = obj.getJSONArray(TAG_TODOS);
 				for(int i=0;i<t.length();i++) {
 					items.add(parseTodo(t.getJSONObject(i)));
 				}
 				
 				
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}	
 
 			//tasks = obj.getJSONObject(TAG_TASKS);
 		
 		
 		/*
 		 * Then parse DAILISES tag to find dailies id
 		 * and loop through them to find it in the TASKS tags
 		 */
 		/*try {
 			JSONArray dailies = obj.getJSONArray(TAG_DAILIESID);
 			for(int i=0;i<dailies.length();i++) {
 				if(tasks.has(dailies.getString(i))) {
 
 					items.add(parseDaily(tasks.getJSONObject(dailies.getString(i))));
 					//TODO: it Throws exception when stuff not found, but we still want to continue if this happens.
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_USER_HAS_NO_DAILIES_TAGS);
 			throw(ex);
 		}
 
 		try {
 			JSONArray todos = obj.getJSONArray(TAG_TODOIDS);
 			for(int i=0;i<todos.length();i++) {
 				if(tasks.has(todos.getString(i))) {
 					items.add(parseTodo(tasks.getJSONObject(todos.getString(i))));
 				}
 			}	
 		} catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_USER_HAS_NO_TODOS_TAGS);
 			throw(ex);
 		}
 		
 		try {
 			JSONArray habits = obj.getJSONArray(TAG_HABITSID);
 			for(int i=0;i<habits.length();i++) {
 				if(tasks.has(habits.getString(i))) {
 					items.add(parseHabit(tasks.getJSONObject(habits.getString(i))));
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_USER_HAS_NO_HABITS_TAGS);
 			throw(ex);
 		}
 		
 		try {
 			JSONArray rewards = obj.getJSONArray(TAG_REWARDIDS);
 			for(int i=0;i<rewards.length();i++) {
 				if(tasks.has(rewards.getString(i))) {
 					items.add(parseReward(tasks.getJSONObject(rewards.getString(i))));
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_USER_HAS_NO_REWARDS_TAGS);
 			throw(ex);
 		}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			//tasks = obj.getJSONObject(key)
 			
 			try {
 				JSONArray h = obj.getJSONArray(TAG_HABITS);
 				for(int i=0;i<h.length();i++) {
 					items.add(parseHabit(h.getJSONObject(i)));
 				}
 				JSONArray d = obj.getJSONArray(TAG_DAILIES);
 	
 				for(int i=0;i<d.length();i++) {
 					items.add(parseDaily(d.getJSONObject(i)));
 				}
 				JSONArray r = obj.getJSONArray(TAG_TODOS);
 				for(int i=0;i<r.length();i++) {
 					items.add(parseReward(r.getJSONObject(i)));
 				}
 				JSONArray t = obj.getJSONArray(TAG_TODOS);
 				for(int i=0;i<t.length();i++) {
 					items.add(parseTodo(t.getJSONObject(i)));
 				}
 				
 				
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}	*/
 			
 		
 			//private static final String TAG_HABITS="habits";
 			//private static final String TAG_TODOS="todos";
 			//private static final String TAG_REWARDS="rewards";
 
 			//ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_USER_HAS_NO_TASKS_TAGS);
 			//throw(ex);
 		//}
 		return items;
 		
 	}
 
 
 	public static HabitItem parseHabitItem(JSONObject obj) throws ParseErrorException {
 		parseError(obj);
 		try {
 			if(obj.getString(TAG_TASK_TYPE).equals("reward")) {
 				return parseReward(obj);
 			}
 			else if(obj.getString(TAG_TASK_TYPE).equals("habit")) {
 				return parseHabit(obj);
 			} else if(obj.getString(TAG_TASK_TYPE).equals("daily")) {
 				return parseDaily(obj);
 			} else if(obj.getString(TAG_TASK_TYPE).equals("todo")) {
 				return parseTodo(obj);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return null;
 	
 	}
 
 	public static double[] parseDirectionAnswer(JSONObject obj) throws ParseErrorException {
 		try {
 			double xp = 0,hp = 0,gold = 0,lvl = 0,delta = 0;
 			 if(obj.has(TAG_NOWXP))
 				 xp = obj.getDouble(TAG_NOWXP);
 			 if(obj.has(TAG_NOWHP))
 				 hp = obj.getDouble(TAG_NOWHP);
 			 if(obj.has(TAG_NOWGP))
 				 gold = obj.getDouble(TAG_NOWGP);
 			 if(obj.has(TAG_LVL))
 				 lvl = obj.getDouble(TAG_LVL);
 			 if(obj.has(TAG_DELTA))
 				 delta = obj.getDouble(TAG_DELTA);
 			 double[] res = {xp,hp,gold,lvl,delta};
 			 return res;
 		} catch (JSONException e) {
 			 e.printStackTrace();
 			throw(new ParseErrorException(ParseErrorException.HABITRPG_DIRECTION_ANSWER_UNPARSABLE));
 		}
 
 	}
 
 	private static Habit parseHabit(JSONObject obj) throws ParseErrorException {
 		try {
 				Habit it = new Habit();
 				parseBase(it, obj);
 				it.setUp(obj.has(TAG_TASK_UP) ? obj.getBoolean(TAG_TASK_UP) : false);
 				it.setDown(obj.has(TAG_TASK_DOWN) ? obj.getBoolean(TAG_TASK_DOWN) : false);
 				return it;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_HABIT_UNPARSABLE);
 			throw(ex);
 		}
 	}
 	private static Daily parseDaily(JSONObject obj) throws ParseErrorException {
 		try {
 			Daily it;
 			String lastday="";
 			
 			if(obj.has(TAG_TASK_HISTORY)) {
 				JSONArray history = obj.getJSONArray(TAG_TASK_HISTORY);
 				lastday = history.getJSONObject(history.length()-1).getString(TAG_TASK_HISTORY_DATE);
 			} 
 			boolean[] repeats = {false,false,false,false,false,false,false};
 			if(obj.has(TAG_TASK_REPEAT)) {
 				JSONObject repeatTag = obj.getJSONObject(TAG_TASK_REPEAT);
 				for(int j=0;j<7;j++) {
 					if(repeatTag.has(whatDay(j)))
 						repeats[j] = repeatTag.getBoolean(whatDay(j));
 				}
 			}
 			
 			it = new Daily();
 			parseBase(it, obj);
 			it.setCompleted(obj.has(TAG_TASK_COMPLETED) ? obj.getBoolean(TAG_TASK_COMPLETED) : false);
 			it.setRepeat(repeats);
 			it.setStreak(obj.has(TAG_TASK_STREAK) ? obj.getInt(TAG_TASK_STREAK) : 0);
 			it.setLastCompleted(lastday);
 			return it;
 		}  catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_DAILY_UNPARSABLE);
 			throw(ex);
 		}
 
 	}
 	private static ToDo parseTodo(JSONObject obj) throws ParseErrorException {
 		try {
 			ToDo it= new ToDo();
 			parseBase(it, obj);
 			it.setCompleted(obj.has(TAG_TASK_COMPLETED) ? obj.getBoolean(TAG_TASK_COMPLETED) : false);
 			it.setDate(obj.has(TAG_TASK_DATE) ? obj.getString(TAG_TASK_DATE) : null);
 			return it;
 		}  catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_TODO_UNPARSABLE);
 			throw(ex);
 		}
 	}
 	private static Reward parseReward(JSONObject obj) throws ParseErrorException {
 		try  {
 			Reward it = new Reward();
 			parseBase(it, obj);
 			return it;
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			ParseErrorException ex = new ParseErrorException(ParseErrorException.JSON_REWARD_UNPARSABLE);
 			throw(ex);
 		}
 	}
 	
 	private static <T extends HabitItem> void parseBase(T it, JSONObject habit) throws JSONException {
 		if(habit.has(TAG_TASK_ID))
 			it.setId(habit.getString(TAG_TASK_ID));
 		if(habit.has(TAG_TASK_NOTES))
 			it.setNotes(habit.getString(TAG_TASK_NOTES));
 		if(habit.has(TAG_TASK_PRIORITY))
 			it.setPriority(habit.getString(TAG_TASK_PRIORITY));
 		if(habit.has(TAG_TASK_TEXT))
 			it.setText(habit.getString(TAG_TASK_TEXT));
 		if(habit.has(TAG_TASK_VALUE))
 			it.setValue(habit.getDouble(TAG_TASK_VALUE));
 		if(habit.has(TAG_TASK_TAGS) &&  !habit.isNull(TAG_TASK_TAGS)) {
 			try {
 				JSONObject tagsJSON = habit.getJSONObject(TAG_TASK_TAGS);
 				List<String> tags = parseTaskTags(tagsJSON);
 				it.setTagsId(tags);
 
 			} catch(JSONException error) {
 				error.printStackTrace(); 
 			}
 		}
 	}
 	
 	private static List<String> parseTaskTags(JSONObject tagsJSON) throws JSONException {
 		List<String> tagsIds = new ArrayList<String>();
 		@SuppressWarnings("unchecked")
 		Iterator<String> it = tagsJSON.keys();
 		while(it.hasNext()) {
 			String tag = it.next();
 			if(tagsJSON.getBoolean(tag))
 				tagsIds.add(tag);
 		}
 		
 		return tagsIds;
 	}
 	
 	
 	
 	public static void parseError(JSONObject obj) throws ParseErrorException{
 		if(obj.has(TAG_ERR)) {
 			try {
 				throw(new ParseErrorException(ParseErrorException.HABITRPG_REGISTRATION_ERROR, obj.getString(TAG_ERR)));
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 	}
 	public static String[] parseAuthenticationAnswer(JSONObject obj) throws ParseErrorException {
 		parseError(obj);
 		String api_t = null;
 		String user_t = null;
 		try {
 			api_t = obj.getString(TAG_AUTH_API_TOKEN);
 			user_t = obj.getString(TAG_AUTH_USER_TOKEN);
 			//callback.onUserConnected(api_t, user_t);
 		} catch (JSONException e) {
 			throw(new ParseErrorException(ParseErrorException.HABITRPG_PARSING_ERROR_AUTH));
 		}
 		String[] res = {api_t, user_t};
 		return res;
 	}
 	public static String[] parseRegistrationAnswer(JSONObject obj) throws ParseErrorException {
 		parseError(obj);
 		String api_t = null;
 		String user_t = null;
 		try {
 			api_t = obj.getString(TAG_REG_API_TOKEN);
 			user_t = obj.getString(TAG_REG_USER_TOKEN);
 			String[] res = {api_t, user_t};
 			return res;
 		} catch (JSONException e) {
 			throw(new ParseErrorException(ParseErrorException.HABITRPG_PARSING_ERROR_REGISTRATION));
 		}
 	}
 
 	/**
 	 * returns the day string depending on the number
 	 * @param j the number
 	 * @return the first/two first letter of the day
 	 */
 	public static String whatDay(int j) {
 		if(j==0)
 			return "m";
 		else if(j==1)
 			return "t";
 		else if(j==2)
 			return "w";
 		else if(j==3)
 			return "th";
 		else if(j==4)
 			return "f";
 		else if(j==5)
 			return "s";
 		else if(j==6)
 			return "su";
 		return "su";
 	}
 	public static boolean parseDeletedTask(JSONObject obj) throws ParseErrorException {
 		parseError(obj);
 		if(obj.has(TAG_TASK_DELETED)) {
  			return true;
 		} else {
 			throw new ParseErrorException(ParseErrorException.TASK_NOT_DELETED);
 		}
 		
 	}
 	
 	
 }
 
