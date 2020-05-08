 package com.tuit.ar.models;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 
 import com.tuit.ar.api.Twitter;
 import com.tuit.ar.api.TwitterAccount;
 import com.tuit.ar.api.TwitterAccountRequestsObserver;
 import com.tuit.ar.api.TwitterRequest;
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.databases.Model;
import com.tuit.ar.models.timeline.Favorites;
 
 public class Status extends ListElement implements TwitterAccountRequestsObserver {
 	private static final String[] columns = new String[]{
 			"date", "favorited", "id", "in_reply_to_screen_name", "in_reply_to_status_id", "in_reply_to_user_id", "message", "source", "user_id", "is_home", "is_reply", "belongs_to_user"
 		};
 	private JSONObject dataSourceJSON;
 	private Date createDate;
 	private long dateMillis = 0;
 	private boolean favorited = false;
 	private long id = 0;
 	private String in_reply_to_screen_name;
 	private long in_reply_to_status_id = 0;
 	private long in_reply_to_user_id = 0;
 	private String message;
 	private String source;
 	private long user_id = 0;
 	private User user;
 	private boolean is_home;
 	private boolean is_reply;
 	private long belongs_to_user;
 
 	public Status(Cursor query) {
 		super();
 		setDateMillis(query.getLong(0) * 1000);
 		setFavorited(query.getInt(1) == 1);
 		setId(query.getLong(2));
 		setInReplyToScreenName(query.getString(3));
 		setInReplyToStatusId(query.getLong(4));
 		setInReplyToUserId(query.getLong(5));
 		setMessage(query.getString(6));
 		setSource(query.getString(7));
 		setUserId(query.getLong(8));
 		setHome(query.getInt(9) == 1);
 		setReply(query.getInt(10) == 1);
 		setBelongsToUser(query.getLong(11));
 	}
 
 	public Status(JSONObject object) {
 		super();
 		this.dataSourceJSON = object;
 	}
 
 	public User getUser() {
 		if (user != null) return user;
 		if (dataSourceJSON != null) { 
 			try {
 				return user = new User(dataSourceJSON.getJSONObject("user"));
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		ArrayList<User> searchUser = User.select("id = ?", new String[] { String.valueOf(getUserId()) }, null, null, null, "1");
 		if (searchUser.size() > 0) return user = searchUser.get(0);
 		return null;
 	}
 
 	public boolean isFavorited() {
 		if (favorited != false) return favorited;
 		try {
 			return favorited = dataSourceJSON.getBoolean("favorited");
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public void setFavorited(boolean favorited) {
 		this.favorited = favorited;
 	}
 
 	public String getInReplyToScreenName() {
 		if (in_reply_to_screen_name != null) return in_reply_to_screen_name;
 		try {
 			return in_reply_to_screen_name = dataSourceJSON.isNull("in_reply_to_screen_name") ? null : dataSourceJSON.getString("in_reply_to_screen_name");
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public void setInReplyToScreenName(String inReplyToScreenName) {
 		in_reply_to_screen_name = inReplyToScreenName;
 	}
 
 	public long getInReplyToStatusId() {
 		if (in_reply_to_status_id != 0) return in_reply_to_status_id;
 		try {
 			return in_reply_to_status_id = dataSourceJSON.getLong("in_reply_to_status_id");
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	public void setInReplyToStatusId(long inReplyToStatusId) {
 		in_reply_to_status_id = inReplyToStatusId;
 	}
 
 	public long getInReplyToUserId() {
 		if (in_reply_to_user_id != 0) return in_reply_to_user_id;
 		try {
 			return in_reply_to_user_id = dataSourceJSON.getLong("in_reply_to_user_id");
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	public void setInReplyToUserId(long inReplyToUserId) {
 		in_reply_to_user_id = inReplyToUserId;
 	}
 
 	public String getSource() {
 		if (source != null) return source;
 		try {
 			return source = dataSourceJSON.isNull("source") ? null : dataSourceJSON.getString("source");
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	public long getUserId() {
 		if (user_id != 0) return user_id;
 		try {
 			return user_id = dataSourceJSON.getJSONObject("user").getLong("id");
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	public void setUserId(long userId) {
 		user_id = userId;
 	}
 
 	public long getId() {
 		if (id != 0) return id;
 		try {
 			return id = dataSourceJSON.getLong("id");
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getMessage() {
 		if (message != null) return message;
 		try {
 			setMessage(dataSourceJSON.isNull("text") ? null : dataSourceJSON.getString("text"));
 			return message;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public void setMessage(String message) {
 		this.message = sanitize(message);
 	}
 
 	public boolean isHome() {
 		return is_home;
 	}
 
 	public void setHome(boolean isHome) {
 		is_home = isHome;
 	}
 
 	public boolean isReply() {
 		return is_reply;
 	}
 
 	public void setReply(boolean isReply) {
 		is_reply = isReply;
 	}
 
 	public long getBelongsToUser() {
 		return belongs_to_user;
 	}
 
 	public void setBelongsToUser(long belongsToUser) {
 		belongs_to_user = belongsToUser;
 	}
 
 	@SuppressWarnings("unchecked")
 	static public ArrayList<Status> select(String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
 		return (ArrayList<Status>)Model.select(Status.class, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
 	}
 
 	public String getUsername() {
 		try {
 			return getUser().getScreenName();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Date getDate() {
 		try {
 			if (createDate != null) return createDate;
 			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d hh:mm:ss Z yyyy");
 			return createDate = sdf.parse(dataSourceJSON.getString("created_at"));
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public void setDateMillis(long dateMillis) {
 		this.dateMillis = dateMillis;
 	}
 
 	public long getDateMillis() {
 		if (dateMillis == 0) {
 			Date date = this.getDate();
 			if (date == null) return (long)0;
 			dateMillis = date.getTime();
 		}
 		return dateMillis;
 	}
 
 	@Override
 	public long replace() {
 		try {
 			User user = getUser();
 			user.setBelongsToUser(getBelongsToUser());
 			user.replace();
 		} catch (SQLiteException e) {}
 
 		// TODO: some kind of factory to avoid having twice the same instance?
 		try {
 			Status status = Status.select("id = ?", new String [] { String.valueOf(getId()) }, null, null, null, "1").get(0);
 			boolean reply = status.isReply() || this.isReply();
 			status.setReply(reply);
 			this.setReply(reply);
 
 			boolean home = status.isHome() || this.isHome();
 			status.setHome(home);
 			this.setHome(home);
 		} catch (Exception e) {
 		}
 		return super.replace();
 	}
 
 	@Override
 	protected ContentValues getValues() {
 		ContentValues fields = new ContentValues();
 		fields.put(columns[0], getDateMillis() / 1000);
 		fields.put(columns[1], isFavorited() ? 1 : 0);
 		fields.put(columns[2], getId());
 		fields.put(columns[3], getInReplyToScreenName());
 		fields.put(columns[4], getInReplyToStatusId());
 		fields.put(columns[5], getInReplyToUserId());
 		fields.put(columns[6], getMessage());
 		fields.put(columns[7], getSource());
 		fields.put(columns[8], getUserId());
 		fields.put(columns[9], isHome() ? 1 : 0);
 		fields.put(columns[10], isReply() ? 1 : 0);
 		fields.put(columns[11], getBelongsToUser());
 		return fields;
 	}
 
 	static public int deleteBelongsToUser(String user) {
 		return Model.delete(Status.class, "belongs_to_user = ?", new String[]{user});
 	}
 
 	@Override
 	public String getText() {
 		return getMessage();
 	}
 
 	@Override
 	public String getDisplayDate() {
 		return calculateElapsed(getDateMillis());
 	}
 
 	public void addToFavorites() {
 		ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair> ();
 		nvps.add(new BasicNameValuePair("id", String.valueOf(getId())));
 		Options option = Options.ADD_TO_FAVORITES;
 		option.setParameters(nvps);
 
 		TwitterAccount account = Twitter.getInstance().getDefaultAccount();
 		account.addRequestObserver(this);
 		if (this.isHome()) com.tuit.ar.models.timeline.Friends.getInstance(account).startedUpdate();
 		if (this.isReply()) com.tuit.ar.models.timeline.Replies.getInstance(account).startedUpdate();
 		
 		try {
 			account.requestUrl(option, nvps, TwitterRequest.Method.POST);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeFromFavorites() {
 		ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair> ();
 		nvps.add(new BasicNameValuePair("id", String.valueOf(getId())));
 		Options option = Options.REMOVE_FROM_FAVORITES;
 		option.setParameters(nvps);
 
 		TwitterAccount account = Twitter.getInstance().getDefaultAccount();
 		account.addRequestObserver(this);
 		if (this.isHome()) com.tuit.ar.models.timeline.Friends.getInstance(account).startedUpdate();
 		if (this.isReply()) com.tuit.ar.models.timeline.Replies.getInstance(account).startedUpdate();
 		com.tuit.ar.models.timeline.Favorites.getInstance(account).startedUpdate();
 
 		try {
 			account.requestUrl(option, nvps, TwitterRequest.Method.POST);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void requestHasFinished(TwitterRequest request) {
 		if (request.getUrl().equals(Options.ADD_TO_FAVORITES) || request.getUrl().equals(Options.REMOVE_FROM_FAVORITES)) {
 			TwitterAccount account = Twitter.getInstance().getDefaultAccount();
 			com.tuit.ar.models.timeline.Favorites f = (com.tuit.ar.models.timeline.Favorites) com.tuit.ar.models.timeline.Favorites.getInstance(account);
 			if (request.getUrl().equals(Options.ADD_TO_FAVORITES)) {
 				this.setFavorited(true);
 				f.getTweets().add(this);
 			} else {
 				this.setFavorited(false);
 				f.getTweets().remove(this);
 			}
 			this.replace();
 			account.removeRequestObserver(this);
 			if (this.isHome()) com.tuit.ar.models.timeline.Friends.getInstance(account).finishedUpdate();
 			if (this.isReply()) com.tuit.ar.models.timeline.Replies.getInstance(account).finishedUpdate();
 			com.tuit.ar.models.timeline.Favorites.getInstance(account).finishedUpdate();
 		}
 	}
 
 	public void requestHasStarted(TwitterRequest request) {
 	}
 }
