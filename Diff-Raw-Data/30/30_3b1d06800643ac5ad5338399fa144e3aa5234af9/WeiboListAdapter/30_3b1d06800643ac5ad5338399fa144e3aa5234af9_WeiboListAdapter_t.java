 package me.aiqi.A7weibo.weibo;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import me.aiqi.A7weibo.MainActivity;
 import me.aiqi.A7weibo.MyApplication;
 import me.aiqi.A7weibo.R;
 import me.aiqi.A7weibo.R.id;
 import me.aiqi.A7weibo.R.layout;
 import me.aiqi.A7weibo.R.string;
 import me.aiqi.A7weibo.downloader.WeiboDownloader;
 import me.aiqi.A7weibo.downloader.WeiboDownloader.Params;
 import me.aiqi.A7weibo.entity.AccessToken;
 import me.aiqi.A7weibo.entity.Consts;
 import me.aiqi.A7weibo.entity.WeiboItem;
 import me.aiqi.A7weibo.entity.WeiboUser;
 import me.aiqi.A7weibo.network.NetworkCondition;
 import me.aiqi.A7weibo.util.WbUtil;
 import android.content.Context;
 import android.opengl.Visibility;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.text.Html;
 import android.text.TextUtils;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WeiboListAdapter extends BaseAdapter {
 	public static final String TAG = "WeiboListAdapter";
 
 	/** append new weibo items to the end of the inner list */
 	public static final int UPDATE_MODE_LOAD_MORE = 0;
 	/** insert new weibo items to the head of the inner List */
 	public static final int UPDATE_MODE_REFRESH = 1;
 
 	private Context mContext;
 	private List<WeiboItem> mWeiboItems;
 	private List<String> mAvatarUrlList;
 	private List<WeiboItem> mWeiboItemsOld;
 	private AsyncTask<Params, Void, ArrayList<WeiboItem>> mTask; // weiboitems downloader task
 
 	public WeiboListAdapter(Context context) {
 		mContext = context;
 		mWeiboItems = readWeiboItemsFromCache();
 		updateAvatarUrl(); // init mAvatarUrlList
 		mWeiboItemsOld = null;
 		mTask = null;
 	}
 
 	/**
 	 * TODO: currently return new empty ArrayList, may read from SQlite in the
 	 * future
 	 * 
 	 * @return
 	 */
 	private ArrayList<WeiboItem> readWeiboItemsFromCache() {
 		return new ArrayList<WeiboItem>();
 	}
 
 	private void updateAvatarUrl() {
 		if (mWeiboItems == null) {
 			mAvatarUrlList = null;
 			return;
 		}
 		ArrayList<String> urls = new ArrayList<String>();
 		for (WeiboItem item : mWeiboItems) {
 			urls.add(item.getUser() == null ? null : item.getUser().getProfile_image_url());
 		}
 		synchronized (this) {
 			mAvatarUrlList = urls;
 		}
 	}
 
 	public void onStop() {
 		if (mTask != null) {
 			mTask.cancel(true);
 			mTask = null;
 		}
 		writeWeiboItemsToCache();
 	}
 
 	private void writeWeiboItemsToCache() {
 
 	}
 
 	@Override
 	public int getCount() {
 		return mWeiboItems.size();
 	}
 
 	@Override
 	public WeiboItem getItem(int position) {
 		return mWeiboItems.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return mWeiboItems.get(position).getId();
 	}
 
 	/**
 	 * reuse widgets to reduce call to View.findViewById(), speed up ListView
 	 * 
 	 * @author starfish
 	 * 
 	 */
 	private static class ViewHolder {
 		public ImageView iv_avatar;
 		public TextView tv_nickname;
 		public TextView tv_source;
 		public TextView tv_weibo_content;
 		public Button btn_comment;
 		public Button btn_forawrd;
 		public Button btn_like;
 
 		// the following view may not always visible
 		public ImageView iv_image;
 		public ImageView iv_orig_image;
 		public TextView tv_orig_weibo_content;
 		public LinearLayout ll_orig_weibo;
 		public FrameLayout fl_additional_info;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		final ViewHolder viewHolder;
 
 		if (convertView == null) {
 			convertView = LayoutInflater.from(mContext).inflate(R.layout.frag_weibo_list_item, parent, false);
 			viewHolder = new ViewHolder();
 			viewHolder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
 			viewHolder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
 			viewHolder.tv_source = (TextView) convertView.findViewById(R.id.tv_time_and_source);
 			viewHolder.tv_weibo_content = (TextView) convertView.findViewById(R.id.tv_weibo_content);
 			viewHolder.btn_comment = (Button) convertView.findViewById(R.id.btn_comment);
 			viewHolder.btn_forawrd = (Button) convertView.findViewById(R.id.btn_forawrd);
 			viewHolder.btn_like = (Button) convertView.findViewById(R.id.btn_like);
 
 			viewHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
 			viewHolder.iv_orig_image = (ImageView) convertView.findViewById(R.id.iv_orig_image);
 			viewHolder.tv_orig_weibo_content = (TextView) convertView.findViewById(R.id.tv_orig_weibo_content);
 			viewHolder.ll_orig_weibo = (LinearLayout) convertView.findViewById(R.id.ll_orig_weibo);
 			viewHolder.fl_additional_info = (FrameLayout) convertView.findViewById(R.id.fl_additional_info);
 			convertView.setTag(viewHolder);
 		} else {
 			viewHolder = (ViewHolder) convertView.getTag();
 		}
 
 		// get weibo content
 
 		WeiboItem weiboItem = getItem(position);

 		if (weiboItem == null) {
 			// make weibo content green to indicate no weibo info
 			viewHolder.tv_weibo_content.setText(Html.fromHtml("<font color='#00FF00'> 好像出错了=_=<br />没有微博信息</font>"));
 			viewHolder.fl_additional_info.setVisibility(View.GONE);
 			return convertView;
 		}
		Log.v(TAG, weiboItem.toString());
 
 		// set user name and avatar
 
 		WeiboUser user = weiboItem.getUser();
 		if (user != null) {
 			viewHolder.tv_nickname.setText(user.getScreen_name());
 			MyApplication.AVATAR_CACHE.get(user.getProfile_image_url(), mAvatarUrlList, viewHolder.iv_avatar);
 		} else {
 			// make username to green to indicate no user info found
 
 			viewHolder.tv_nickname.setText(Html.fromHtml("<font color='#00FF00'>好像出错了=_=<br />没有微博信息</font>"));
 			viewHolder.tv_weibo_content.setText("");
 			viewHolder.fl_additional_info.setVisibility(View.GONE);
 			return convertView;
 		}
 
 		// set weibo created_at time
 
 		String createTimeString = WbUtil.getTimeString(weiboItem.getCreated_at());
 		if (createTimeString == null) {
 			createTimeString = weiboItem.getCreated_at();
 			Log.d(TAG, "can't parse created_at time:" + createTimeString);
 		}
 		Log.v(TAG, createTimeString);
 
 		// handle image, forwareded (retweeted) weibo
 
 		if (!TextUtils.isEmpty(weiboItem.getThumbnail_pic())) {
 			// weibo have image, load thumb image
 
 			viewHolder.fl_additional_info.setVisibility(View.VISIBLE);
 			viewHolder.iv_image.setVisibility(View.VISIBLE);
 			viewHolder.ll_orig_weibo.setVisibility(View.GONE);
 			viewHolder.iv_orig_image.setImageBitmap(Consts.PLACE_HOLDER_IMAGE_1x1);
 
 			viewHolder.iv_image.setImageResource(R.drawable.image_loading);
 			MyApplication.LARGE_IMAGE_CACHE.get(weiboItem.getThumbnail_pic(), viewHolder.iv_image);
 
 		} else if (weiboItem.getRetweeted_status() != null) {
 			// load original weibo
 
 			viewHolder.fl_additional_info.setVisibility(View.VISIBLE);
 			viewHolder.ll_orig_weibo.setVisibility(View.VISIBLE);
			viewHolder.iv_image.setVisibility(View.GONE);
			viewHolder.iv_image.setImageBitmap(Consts.PLACE_HOLDER_IMAGE_1x1);
 
 			WeiboItem originalWeibo = weiboItem.getRetweeted_status();
 
			// set original weibo content
 			viewHolder.tv_orig_weibo_content.setText(
 					WeiboRichText.getRichWeiboText(mContext,
 							"@" + originalWeibo.getUser().getName() + ":" + originalWeibo.getText()));
 
 			if (!TextUtils.isEmpty(originalWeibo.getThumbnail_pic())) {
 				// original weibo have image
 				viewHolder.iv_orig_image.setVisibility(View.VISIBLE);
				viewHolder.iv_orig_image.setImageResource(R.drawable.image_loading);
				MyApplication.LARGE_IMAGE_CACHE.get(originalWeibo.getThumbnail_pic(), viewHolder.iv_orig_image);
 			} else {
 				// original don't have image
				viewHolder.iv_orig_image.setVisibility(View.GONE);
 				viewHolder.iv_orig_image.setImageBitmap(Consts.PLACE_HOLDER_IMAGE_1x1);
 			}
 
 		} else {
 			// a plain weibo, not forwarded, no image
 			viewHolder.fl_additional_info.setVisibility(View.GONE);
 			viewHolder.iv_image.setImageBitmap(Consts.PLACE_HOLDER_IMAGE_1x1);
 			viewHolder.iv_orig_image.setImageBitmap(Consts.PLACE_HOLDER_IMAGE_1x1);
 		}
 
 		String sourceAndTimeHtmlString = new StringBuilder()
 				.append("<font color='#FFCC00'>")
 				.append(createTimeString)
 				.append("</font> 来自")
 				.append(weiboItem.getSource()).toString();
 		viewHolder.tv_source.setText(Html.fromHtml(sourceAndTimeHtmlString));
 
 		viewHolder.tv_weibo_content.setText(WeiboRichText.getRichWeiboText(mContext, weiboItem.getText()));
 
 		// with next statement, click on a link will not open it in browser
 		viewHolder.tv_weibo_content.setMovementMethod(LinkMovementMethod.getInstance());
 		viewHolder.btn_comment.setText(weiboItem.getComments_count() == 0 ? "评论" : String.valueOf(weiboItem
 				.getComments_count()));
 		viewHolder.btn_forawrd.setText(weiboItem.getReposts_count() == 0 ? "转发" : String.valueOf(weiboItem
 				.getReposts_count()));
 		viewHolder.btn_like.setText(weiboItem.getAttitudes_count() == 0 ? "赞" : String.valueOf(weiboItem
 				.getAttitudes_count()));
 		return convertView;
 	}
 
 	public void updateWeibolist(List<WeiboItem> weiboItems, int mode) {
 		String msg = null;
 		synchronized (this) {
 			switch (mode) {
 			case UPDATE_MODE_REFRESH:
 				Log.v(TAG, "mode: refresh");
 				if (mWeiboItems.size() > 0) {
 					if (weiboItems.size() < Consts.WeiboDownloader.COUNT_PER_PAGE) {
 						// no time gap, cause we get less items than we request
 						mWeiboItems.addAll(0, weiboItems);
 						mWeiboItemsOld = null;
 					} else {
 						// some time gap between current time period and old time period appeared
 						mWeiboItemsOld = mWeiboItems;
 						mWeiboItems = weiboItems;
 					}
 				} else {
 					// load weibo items for the first time
 					mWeiboItems = weiboItems;
 				}
 				break;
 
 			case UPDATE_MODE_LOAD_MORE:
 				Log.v(TAG, "mode: load more");
 				if (mWeiboItemsOld == null) {
 					mWeiboItems.addAll(weiboItems);
 				} else if (weiboItems.size() < Consts.WeiboDownloader.COUNT_PER_PAGE) {
 					// old time and current time has meet each other
 					mWeiboItems.addAll(weiboItems);
 					mWeiboItems.addAll(mWeiboItemsOld);
 					mWeiboItemsOld = null;
 				} else {
 					// time gap (may) continues
 					mWeiboItems.addAll(weiboItems);
 				}
 				break;
 			}
 			Log.v(TAG, "refreshed, size: " + mWeiboItems.size() + ", oldweibo: " + (mWeiboItemsOld == null ? "null"
 					: mWeiboItemsOld.size()));
 			Log.d(TAG, "refresh UI now");
 			// refresh UI even got zero new weibo to refresh created_at time, make user know weibo has indeed refreshed
 			notifyDataSetChanged();
 			updateAvatarUrl();
 
 			final int size = weiboItems.size();
 			if (size > 0) {
 				msg = mContext.getString(R.string.got) + weiboItems.size() + mContext.getString(R.string.new_weibo);
 			} else {
 				if (mode == UPDATE_MODE_REFRESH) {
 					msg = mContext.getString(R.string.new_weibo_not_found);
 				} else {
 					msg = mContext.getString(R.string.no_more_old_weibo_try_to_refresh);
 				}
 			}
 		}
 		Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
 	}
 
 	/** It's the caller's duty to check the validity of AccessToken and reauth */
 	public void loadMore(AccessToken accessToken) {
 		Log.v(TAG, "start loadMore");
 		downloadWeiboItems(accessToken, UPDATE_MODE_LOAD_MORE);
 	}
 
 	/** It's the caller's duty to check the validity of AccessToken and reauth */
 	public void refresh(AccessToken accessToken) {
 		Log.v(TAG, "start refresh");
 		downloadWeiboItems(accessToken, UPDATE_MODE_REFRESH);
 	}
 
 	/** It's the caller's duty to check the validity of AccessToken and reauth */
 	public void downloadWeiboItems(AccessToken accessToken, int refreshMode) {
 		if (accessToken.isExpired()) {
 			// the caller should handle the validity
 			Log.v(TAG, "access token expired");
 			onDownloadCompleted();
 			return;
 		}
 		if (mTask != null) {
 			Status status = mTask.getStatus();
 			// another download task running or pending
 			if (status == Status.RUNNING || status == Status.PENDING) {
 				Log.v(TAG, "another task running or pending");
 				return;
 			}
 		}
 		if (checkDownloadCondition()) {
 			WeiboDownloader.Params params = new WeiboDownloader.Params();
 			params.put(WeiboDownloader.Params.ACCESS_TOKEN, accessToken.getAccessTokenString());
 			params.put(WeiboDownloader.Params.REFRESH_MODE, refreshMode);
 			params.put(WeiboDownloader.Params.COUNT, Consts.WeiboDownloader.COUNT_PER_PAGE);
 			switch (refreshMode) {
 			case UPDATE_MODE_LOAD_MORE:
 				params.put(WeiboDownloader.Params.MAX_ID, getMaxId(mWeiboItems));
 				if (mWeiboItemsOld != null) {
 					params.put(WeiboDownloader.Params.SINCE_ID, getSinceId(mWeiboItemsOld));
 				}
 				break;
 			case UPDATE_MODE_REFRESH:
 				params.put(WeiboDownloader.Params.SINCE_ID, getSinceId(mWeiboItems));
 			}
 			mTask = new WeiboDownloader(this, mContext).execute(params);
 		} else {
 			Toast.makeText(MyApplication.getContext(), "请检查网络连接", Toast.LENGTH_SHORT).show();
 			onDownloadCompleted();
 		}
 	}
 
 	/**
 	 * check for network conditions, etc. but do not check for AccessToken
 	 * validity, the caller should check it in Activity or Fragment
 	 * 
 	 * @return true if can perform download task (currently means network is
 	 *         connected, {@code false} otherwise
 	 */
 	private boolean checkDownloadCondition() {
 		return NetworkCondition.isOnline();
 	}
 
 	/** @return id of most recent weibo item, 0 otherwise */
 	private long getSinceId(List<WeiboItem> items) {
 		long sinceID = 0;
 		if (items != null && items.size() > 0) {
 			sinceID = items.get(0).getId();
 		}
 		return sinceID;
 	}
 
 	/**
 	 * used in load more method to return weibos whose id <= MaxId
 	 * 
 	 * @return id of weibo item in the end of the WeiboList minus one or 0
 	 *         otherwise
 	 */
 	private long getMaxId(List<WeiboItem> items) {
 		long id = 0;
 		if (items != null && items.size() > 0) {
 			id = items.get(items.size() - 1).getId() - 1;
 		}
 		return id;
 	}
 
 	protected void onDownloadCompleted() {
 		((MainActivity) mContext).getPullToRefreshAttacher().setRefreshComplete();
 	}
 }
