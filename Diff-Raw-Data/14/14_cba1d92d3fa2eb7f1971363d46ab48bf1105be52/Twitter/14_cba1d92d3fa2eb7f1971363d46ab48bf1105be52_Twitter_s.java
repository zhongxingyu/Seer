 package org.fukata.android.mytw.twitter;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.fukata.android.exandroid.util.StringUtil;
 import org.fukata.android.mytw.TimelineItem;
 import org.fukata.android.mytw.twitter.rs.Status;
 import org.fukata.android.mytw.twitter.rs.User;
 import org.fukata.android.mytw.util.SettingUtil;
 
 import android.util.Log;
 
 import com.thoughtworks.xstream.XStream;
 
 
 public class Twitter {
 	static final String CHARSET = "UTF-8";
	XStream xStream;
 	
 	public Twitter() {
		xStream = new XStream();
 	}
 	
 	private String getApiPrefix() {
 		String url = SettingUtil.getApiServerUrl();
 		if (StringUtil.isNotBlank(url) && url.endsWith("/")) {
 			url = url.substring(0, url.length()-1);
 		}
 		return url;
 	}
 	
 	// ===============================================================
 	// statuses/home_timeline
 	// ===============================================================
 	
 	public List<TimelineItem> getHomeTimeline() {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		return loadHomeTimeline(params);
 	}
 	
 	public List<TimelineItem> getMoreHomeTimeline(String maxId) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("max_id", maxId));
 		return loadHomeTimeline(params);
 	}
 	
 	public List<TimelineItem> getNewHomeTimeline(String sinceId) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("since_id", sinceId));
 		return loadHomeTimeline(params);
 	}
 	
 	private List<TimelineItem> loadHomeTimeline(List<NameValuePair> parameters) {
 		parameters.add(new BasicNameValuePair("count", String.valueOf(SettingUtil.getTimelineCount())));
 		
 		HttpParams params = new BasicHttpParams();
 		List<TimelineItem> list = new ArrayList<TimelineItem>();
 		DefaultHttpClient client = new DefaultHttpClient(params);
 		StringBuilder url = new StringBuilder(getApiPrefix()+"/statuses/home_timeline/");
 		if (parameters!=null && parameters.size()>0) {
 			URLEncodedUtils.format(parameters, CHARSET);
 			for (int i=0; i<parameters.size(); i++) {
 				NameValuePair param = parameters.get(i);
 				if (i==0) {
 					url.append("?");
 				} else {
 					url.append("&");
 				}
 				url.append(param.getName()+"="+param.getValue());
 			}
 		}
 		HttpGet method = new HttpGet(url.toString());
 		String xml = null;
 		try {
 			HttpResponse response = client.execute(method);
 			xml = EntityUtils.toString(response.getEntity(), CHARSET);
 			xStream.alias("xml", ArrayList.class);
 			xStream.alias("item", Status.class);
 			xStream.alias("user", User.class);
 			@SuppressWarnings("unchecked")
 			List<Status> statuses = (List<Status>) xStream.fromXML(xml);
 			for (Status status : statuses) {
 				TimelineItem item = new TimelineItem();
 				item.setStatusId(status.getId());
 				item.setStatus(status.getText());
 				item.setUsername(status.getUser().getScreenname());
 				item.setUserId(status.getUser().getId());
 				item.setSource(status.getSource());
 				item.setInReplytoStatusId(status.getInreplytostatusid());
 				item.setCreatedAt( new Date(status.getCreatedat()) );
 				list.add(item);
 			}
 		} catch (Exception e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 		
 		return list;
 	} 
 	
 	public TimelineItem show(String statusId) {
 		List<TimelineItem> list = new ArrayList<TimelineItem>();
 		DefaultHttpClient client = new DefaultHttpClient();
 		String url = getApiPrefix()+"/statuses/show?id=" + statusId;
 		HttpGet method = new HttpGet(url);
 		String xml = null;
 		try {
 			HttpResponse response = client.execute(method);
 			xml = EntityUtils.toString(response.getEntity(), CHARSET);
 			Log.d(getClass().getSimpleName(), xml);
			xStream = new XStream(); //initialize.
 			xStream.alias("xml", Status.class);
 			xStream.alias("user", User.class);
 			Status status = (Status) xStream.fromXML(xml);
 			TimelineItem item = new TimelineItem();
 			item.setStatusId(status.getId());
 			item.setStatus(status.getText());
 			item.setUsername(status.getUser().getScreenname());
 			item.setUserId(status.getUser().getId());
 			item.setSource(status.getSource());
 			item.setInReplytoStatusId(status.getInreplytostatusid());
 			item.setCreatedAt( new Date(status.getCreatedat()) );
 			list.add(item);
 			return item;
 		} catch (Exception e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 		return null;
 	} 
 
 	// ===============================================================
 	// statuses/mentions
 	// ===============================================================
 
 	public List<TimelineItem> getMentions() {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		return loadMentions(params);
 	}
 	
 	public List<TimelineItem> getMoreMentions(String maxId) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("max_id", maxId));
 		return loadMentions(params);
 	}
 	
 	public List<TimelineItem> getNewMentions(String sinceId) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("since_id", sinceId));
 		return loadMentions(params);
 	}
 	
 	private List<TimelineItem> loadMentions(List<NameValuePair> parameters) {
 		parameters.add(new BasicNameValuePair("count", String.valueOf(SettingUtil.getTimelineCount())));
 		
 		HttpParams params = new BasicHttpParams();
 		List<TimelineItem> list = new ArrayList<TimelineItem>();
 		DefaultHttpClient client = new DefaultHttpClient(params);
 		StringBuilder url = new StringBuilder(getApiPrefix()+"/statuses/mentions/");
 		if (parameters!=null && parameters.size()>0) {
 			URLEncodedUtils.format(parameters, CHARSET);
 			for (int i=0; i<parameters.size(); i++) {
 				NameValuePair param = parameters.get(i);
 				if (i==0) {
 					url.append("?");
 				} else {
 					url.append("&");
 				}
 				url.append(param.getName()+"="+param.getValue());
 			}
 		}
 		HttpGet method = new HttpGet(url.toString());
 		String xml = null;
 		try {
 			HttpResponse response = client.execute(method);
 			xml = EntityUtils.toString(response.getEntity(), CHARSET);
 			xStream.alias("xml", ArrayList.class);
 			xStream.alias("item", Status.class);
 			xStream.alias("user", User.class);
 			@SuppressWarnings("unchecked")
 			List<Status> statuses = (List<Status>) xStream.fromXML(xml);
 			for (Status status : statuses) {
 				TimelineItem item = new TimelineItem();
 				item.setStatusId(status.getId());
 				item.setStatus(status.getText());
 				item.setUsername(status.getUser().getScreenname());
 				item.setUserId(status.getUser().getId());
 				item.setSource(status.getSource());
 				item.setCreatedAt( new Date(status.getCreatedat()) );
 				list.add(item);
 			}
 		} catch (Exception e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 		
 		return list;
 	}
 	
 	// ===============================================================
 	// direst_messages
 	// ===============================================================
 	
 	public List<TimelineItem> getDirectMessages() {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		return loadDirectMessages(params);
 	}
 	
 	public List<TimelineItem> getMoreDirectMessages(String maxId) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("max_id", maxId));
 		return loadDirectMessages(params);
 	}
 	
 	public List<TimelineItem> getNewDirectMessages(String sinceId) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("since_id", sinceId));
 		return loadDirectMessages(params);
 	}
 	
 	private List<TimelineItem> loadDirectMessages(List<NameValuePair> parameters) {
 		parameters.add(new BasicNameValuePair("count", String.valueOf(SettingUtil.getTimelineCount())));
 		HttpParams params = new BasicHttpParams();
 		List<TimelineItem> list = new ArrayList<TimelineItem>();
 		DefaultHttpClient client = new DefaultHttpClient(params);
 		StringBuilder url = new StringBuilder(getApiPrefix()+"/direct_messages/");
 		if (parameters!=null && parameters.size()>0) {
 			URLEncodedUtils.format(parameters, CHARSET);
 			for (int i=0; i<parameters.size(); i++) {
 				NameValuePair param = parameters.get(i);
 				if (i==0) {
 					url.append("?");
 				} else {
 					url.append("&");
 				}
 				url.append(param.getName()+"="+param.getValue());
 			}
 		}
 		HttpGet method = new HttpGet(url.toString());
 		String xml = null;
 		try {
 			HttpResponse response = client.execute(method);
 			xml = EntityUtils.toString(response.getEntity(), CHARSET);
 			xStream.alias("xml", ArrayList.class);
 			xStream.alias("item", Status.class);
 			xStream.alias("user", User.class);
 			@SuppressWarnings("unchecked")
 			List<Status> statuses = (List<Status>) xStream.fromXML(xml);
 			for (Status status : statuses) {
 				TimelineItem item = new TimelineItem();
 				item.setStatusId(status.getId());
 				item.setStatus(status.getText());
 				item.setUsername(status.getSenderscreenname());
 				item.setUserId(status.getSenderid());
 				item.setSource(status.getSource());
 				item.setCreatedAt( new Date(status.getCreatedat()) );
 				list.add(item);
 			}
 		} catch (Exception e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 		
 		return list;
 	}
 
 	private boolean updateBase(String apiPath, HashMap<String, String> map) {
 		HttpParams params = new BasicHttpParams();
 		DefaultHttpClient client = new DefaultHttpClient(params);
 		HttpPost method = new HttpPost(getApiPrefix() + apiPath);
 		try {
 			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 			for (String key : map.keySet()) {
 				parameters.add(new BasicNameValuePair(key, map.get(key)));				
 			}
 			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, CHARSET);
 			method.setEntity(entity);
 			HttpResponse response = client.execute(method);
 			return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
 		} catch (Exception e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 			return false;
 		}
 	}
 
 	public boolean updateStatus(String status) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("status", status);
 		return updateBase("/statuses/update/", map);
 	}
 	
 	public boolean postReTweet(String statusId) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("id", statusId);
 		return updateBase("/statuses/retweet/", map);
 	}
 	
 	public boolean deleteDirectMessage(String statusId) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("id", statusId);
 		return updateBase("/direct_messages/destroy/", map);
 	}
 
 	public boolean postFavorites(String statusId) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("id", statusId);
 		return updateBase("/favorites/create/", map);
 	}
 	
 	// ===============================================================
 	// users
 	// ===============================================================
 	public User getUser() {
 		HttpParams params = new BasicHttpParams();
 		DefaultHttpClient client = new DefaultHttpClient(params);
 		StringBuilder url = new StringBuilder(getApiPrefix()+"/users/show/");
 		HttpGet method = new HttpGet(url.toString());
 		String xml = null;
 		
 		User user = null;
 		try {
 			HttpResponse response = client.execute(method);
 			xml = EntityUtils.toString(response.getEntity(), CHARSET);
 			xStream.alias("xml", User.class);
 			user = (User) xStream.fromXML(xml);
 		} catch (Exception e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 		
 		return user;
 	}
 
 }
