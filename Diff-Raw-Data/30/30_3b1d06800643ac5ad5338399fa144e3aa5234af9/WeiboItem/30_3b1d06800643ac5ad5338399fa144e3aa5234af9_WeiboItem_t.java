 package me.aiqi.A7weibo.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.R.string;
 import android.text.TextUtils;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * 代表一条微博：见：http://open.weibo.com/wiki/2/statuses/friends_timeline
  * 
  * @author starfish
  * 
  */
 public class WeiboItem {
 	private static final String TAG = WeiboItem.class.getSimpleName();
 
 	private String created_at = ""; // 微博创建时间
 	private long id = 0; // 微博ID. int64
 	private long mid = 0; // 微博MID
 	private String idstr = ""; // 字符串型的微博ID
 	private String text = "";// 微博信息内容
 	private String source = ""; // 微博来源
 	private boolean favorited = false; // 是否已收藏，true：是，false：否
 	private boolean truncated = false; // 是否被截断，true：是，false：否
 	private String in_reply_to_status_id = ""; // （暂未支持）回复ID
 	private String in_reply_to_user_id = ""; // （暂未支持）回复人UID
 	private String in_reply_to_screen_name = ""; // （暂未支持）回复人昵称
 	private String thumbnail_pic = "";// 缩略图片地址，没有时不返回此字段
 	private String bmiddle_pic = "";// 中等尺寸图片地址，没有时不返回此字段
 	private String original_pic = "";// 原始图片地址，没有时不返回此字段
 	private WeiboGeo geo = null; // 地理信息字段 详细 类型：object
 	private WeiboUser user = null; // 微博作者的用户信息字段 详细 类型：object
 	private WeiboItem retweeted_status = null; // 被转发的原微博信息字段，当该微博为转发微博时返回 详细
 												// 类型：object
 	private int reposts_count = 0; // 转发数
 	private int comments_count = 0; // 评论数
 	private int attitudes_count = 0; // 表态数
 	private int mlevel = 0;// 暂未支持
 	private WeiboVisiblity visible = new WeiboVisiblity(WeiboVisiblity.NORMAL);// 微博的可见性及指定可见分组信息。该object中type取值，0：普通微博，1：私密微博，3：指定分组微博，4：密友微博；list_id为分组的组号
 	// 类型：object
 	private List<String> pic_urls = null;// 微博配图地址。多图时返回多图链接。无配图返回“[]” 类型：object
 
 	// private Object[] ad;// 微博流内的推广微博ID 类型： object array [这个估计用不到
 
 	public WeiboItem() {
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("idstr:" + this.idstr + ", created_at: " + this.created_at + ", source: " + this.source);
 		sb.append("text: " + this.text);
 		sb.append("评(" + this.comments_count + ")，转(" + this.reposts_count + ")，赞(" + this.attitudes_count + ")");
 		sb.append("可见性：" + this.visible.toString());
		if (this.retweeted_status != null) {
			sb.append("原微博：" + this.retweeted_status.toString());
		}
 		return sb.toString();
 	}
 
 	public String getCreated_at() {
 		return created_at;
 	}
 
 	public void setCreated_at(String created_at) {
 		this.created_at = created_at;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public long getMid() {
 		return mid;
 	}
 
 	public void setMid(long mid) {
 		this.mid = mid;
 	}
 
 	public String getIdstr() {
 		return idstr;
 	}
 
 	public void setIdstr(String idstr) {
 		this.idstr = idstr;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public String getSource() {
 		return source;
 	}
 
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	public boolean isFavorited() {
 		return favorited;
 	}
 
 	public void setFavorited(boolean favorited) {
 		this.favorited = favorited;
 	}
 
 	public boolean isTruncated() {
 		return truncated;
 	}
 
 	public void setTruncated(boolean truncated) {
 		this.truncated = truncated;
 	}
 
 	public String getIn_reply_to_status_id() {
 		return in_reply_to_status_id;
 	}
 
 	public void setIn_reply_to_status_id(String in_reply_to_status_id) {
 		this.in_reply_to_status_id = in_reply_to_status_id;
 	}
 
 	public String getIn_reply_to_user_id() {
 		return in_reply_to_user_id;
 	}
 
 	public void setIn_reply_to_user_id(String in_reply_to_user_id) {
 		this.in_reply_to_user_id = in_reply_to_user_id;
 	}
 
 	public String getIn_reply_to_screen_name() {
 		return in_reply_to_screen_name;
 	}
 
 	public void setIn_reply_to_screen_name(String in_reply_to_screen_name) {
 		this.in_reply_to_screen_name = in_reply_to_screen_name;
 	}
 
 	public String getThumbnail_pic() {
 		return thumbnail_pic;
 	}
 
 	public void setThumbnail_pic(String thumbnail_pic) {
 		this.thumbnail_pic = thumbnail_pic;
 	}
 
 	public String getBmiddle_pic() {
 		return bmiddle_pic;
 	}
 
 	public void setBmiddle_pic(String bmiddle_pic) {
 		this.bmiddle_pic = bmiddle_pic;
 	}
 
 	public String getOriginal_pic() {
 		return original_pic;
 	}
 
 	public void setOriginal_pic(String original_pic) {
 		this.original_pic = original_pic;
 	}
 
 	public WeiboGeo getGeo() {
 		return geo;
 	}
 
 	public void setGeo(WeiboGeo geo) {
 		this.geo = geo;
 	}
 
 	public WeiboUser getUser() {
 		return user;
 	}
 
 	public void setUser(WeiboUser user) {
 		this.user = user;
 	}
 
 	public WeiboItem getRetweeted_status() {
 		return retweeted_status;
 	}
 
 	public void setRetweeted_status(WeiboItem retweeted_status) {
 		this.retweeted_status = retweeted_status;
 	}
 
 	public int getReposts_count() {
 		return reposts_count;
 	}
 
 	public void setReposts_count(int reposts_count) {
 		this.reposts_count = reposts_count;
 	}
 
 	public int getComments_count() {
 		return comments_count;
 	}
 
 	public void setComments_count(int comments_count) {
 		this.comments_count = comments_count;
 	}
 
 	public int getAttitudes_count() {
 		return attitudes_count;
 	}
 
 	public void setAttitudes_count(int attitudes_count) {
 		this.attitudes_count = attitudes_count;
 	}
 
 	public int getMlevel() {
 		return mlevel;
 	}
 
 	public void setMlevel(int mlevel) {
 		this.mlevel = mlevel;
 	}
 
 	public WeiboVisiblity getVisible() {
 		return visible;
 	}
 
 	public void setVisible(WeiboVisiblity visible) {
 		this.visible = visible;
 	}
 
 	public List<String> getPic_urls() {
 		return pic_urls;
 	}
 
 	public void setPic_urls(List<String> pic_urls) {
 		this.pic_urls = pic_urls;
 	}
 
 	/**
 	 * parse json string and encapsulate Weibo items into ArrayList<WeiboItem>,
 	 * return null if parse error
 	 * 
 	 * @param json
 	 * @return ArrayList<WeiboItem>
 	 */
 	public static ArrayList<WeiboItem> parseJson(String json) {
 		if (TextUtils.isEmpty(json)) {
 			Log.d(TAG, "json is empty or null");
 			return null;
 		}
 		Log.v(TAG, "json: length: " + json.length());
 		try {
 			final ArrayList<WeiboItem> list = new ArrayList<WeiboItem>();
 			JSONArray array = null;
 			JSONObject object = new JSONObject(json);
 			array = object.optJSONArray("statuses");
 			if (array == null) {
 				return null;
 			}
 			Log.v(TAG, "begin build Weiboitem ArrayList");
 
 			for (int i = 0; i < array.length(); i++) {
 				list.add(parseSingleWeiboItem((JSONObject) array.get(i)));
 			}
 			return list;
 		} catch (JSONException e) {
 			Log.d(TAG, "parsing weiboitem json error");
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static WeiboItem parseSingleWeiboItem(JSONObject object) {
 		if (object == null) {
 			return null;
 		}
 
 		WeiboItem weiboItem = new WeiboItem();
 
 		// common properties
 		weiboItem.setAttitudes_count(object.optInt("attitudes_count")); // fallback:0
 		weiboItem.setComments_count(object.optInt("comments_count")); // fallback:0
 		weiboItem.setCreated_at(object.optString("created_at")); // fallback:""
 		weiboItem.setFavorited(object.optBoolean("favorited")); // fallback:false
 		// weiboItem.setGeo(); // fallback:null TODO: 暂时不实现
 		weiboItem.setId(object.optLong("id")); // fallback:0
 		weiboItem.setIdstr(object.optString("idstr")); // fallback:""
 		weiboItem.setReposts_count(object.optInt("reposts_count"));
 		weiboItem.setSource(object.optString("source"));
 		weiboItem.setText(object.optString("text"));
 		weiboItem.setTruncated(object.optBoolean("truncated"));
 		weiboItem.setUser(WeiboUser.parseUserFromJsonObject(object.optJSONObject("user")));
 		WeiboVisiblity visiblity = new WeiboVisiblity(object.optInt("visible"));
 		if (visiblity.getType() == WeiboVisiblity.SELECTED_GROUP) {
 			visiblity.setList_id(object.optInt("list_id"));
 		}
 		weiboItem.setVisible(visiblity);
 
 		// Picture urls
 		JSONArray pic_urlsArray = object.optJSONArray("pic_urls"); // fallback:null
 		if (pic_urlsArray != null) {
 			ArrayList<String> pic_urls = new ArrayList<String>();
 			try {
 				for (int j = 0; j < pic_urlsArray.length(); j++) {
 					pic_urls.add(((JSONObject) pic_urlsArray.get(j)).optString("thumbnail_pic"));
 				}
 			} catch (JSONException e) {
 				if (pic_urls.size() == 0) {
 					pic_urls = null;
 				}
 				e.printStackTrace();
 			}
 			weiboItem.setPic_urls(pic_urls);
 		}
 		weiboItem.setThumbnail_pic(object.optString("thumbnail_pic"));
 		weiboItem.setBmiddle_pic(object.optString("bmiddle_pic"));
 		weiboItem.setOriginal_pic(object.optString("original_pic"));
 
 		// Original weibo, may be null
 		weiboItem.setRetweeted_status(parseSingleWeiboItem(object.optJSONObject("retweeted_status")));
 
 		//				Log.v(TAG, weiboItem.toString());
 		return weiboItem;
 	}
 
 }
