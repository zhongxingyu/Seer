 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.android.activity;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.mustard.android.Controller;
 import org.mustard.android.MessagingListener;
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.Preferences;
 import org.mustard.android.R;
 import org.mustard.android.provider.StatusNet;
 import org.mustard.android.view.ActionItem;
 import org.mustard.android.view.GimmeMoreListView;
 import org.mustard.android.view.MustardStatusTextView;
 import org.mustard.android.view.QuickAction;
 import org.mustard.android.view.RemoteImageView;
 import org.mustard.geonames.GeoName;
 import org.mustard.statusnet.Attachment;
 import org.mustard.statusnet.RowStatus;
 import org.mustard.util.DateUtils;
 import org.mustard.util.MustardException;
 import org.mustard.util.StatusNetUtils;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.AsyncTask.Status;
 import android.preference.PreferenceManager;
 import android.text.ClipboardManager;
 import android.text.Html;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TextView.BufferType;
 
 public abstract class MustardBaseActivity extends ListActivity implements GimmeMoreListView.OnNeedMoreListener {
 
 	protected String TAG="MustardBaseActivity";
 
 	protected static final String EXTRA_USER="mustard.user";
 	protected static final String EXTRA_ACCOUNT="mustard.account";
 
 	protected static final int DIALOG_FETCHING_ID=0;
 	protected static final int DIALOG_OPENING_ID=1;
 
 	private static final int ACTIVITY_CREATE = 0;
 	private static final int ACTIVITY_EDIT = 1;
 	private static final int ACCOUNT_ADD = 2;
 	private static final int ACCOUNT_DEL = 3;
 	//	private static final int ACTIVITY_MENTIONS = 4;
 	private static final int ACCOUNT_ADD_SWITCH = 5;
 	//	private static final int ACTIVITY_FRIENDS = 6;
 	//	private static final int ACTIVITY_FAVORITES = 7;
 	private static final int ACTIVITY_PUBLIC = 8;
 
 	private static final int INSERT_ID = 0;
 	protected static final int MENTIONS_ID = 4;
 	private static final int PUBLIC_ID = 5;
 	private static final int REFRESH_ID = 6;
 	private static final int LOGOUT_ID = 7;
 	private static final int DM_ID = 8;
 	private static final int SWITCH_ID = 9;
 	private static final int SEARCH_ID = 10;
 	private static final int ABOUT_ID = 11;
 	private static final int BACK_ID = 12;
 	protected static final int SUB_ID = 15;
 	protected static final int UNSUB_ID = 16;
 	protected static final int FAVORITES_ID = 17;
 	protected static final int FRIENDS_ID = 18;
 //	private static final int ACCOUNT_SETTINGS_ID = 19;
 	private static final int SETTINGS_ID = 20;
 	private static final int BOOKMARKS_ID = 21;
 	private static final int BOOKMARK_THIS_ID = 22;
 	protected static final int GROUP_JOIN_ID= 26;
 	protected static final int GROUP_LEAVE_ID= 27;
 	protected static final int M_SUB_ID = 28;
 	protected static final int M_UNSUB_ID = 29;
 
 	private QuickAction mQuickAction;
 	private Context mContext;
 
 	//	protected MustardDbAdapter mDbHelper;
 	private StatusesLoadMore mLoadMoreTask = null;
 	private StatusesFetcher mFetcherTask = null;
 	private boolean mIsRefresh=false;
 	protected boolean mNoMoreDents=false;
 	protected HashMap<Long, Boolean> mHMNoMoreDents= new HashMap<Long, Boolean>();
 	private String mErrorMessage="";
 
 
 
 	protected StatusNet mStatusNet = null;
 	protected boolean mFromSavedState = false;
 	protected boolean mForceOnlyBackMenu=false;
 	private boolean  mIsOnSaveInstanceState=false;
 
 	protected int DB_ROW_TYPE;
 	protected String DB_ROW_EXTRA;
 	protected String DB_ROW_ORDER = "DESC";
 	protected int R_ROW_ID=R.layout.timeline_list_item;
 	protected boolean isRefreshEnable=true;
 	protected boolean isBookmarkEnable=true;
 	protected boolean isConversationEnable=true;
 	protected boolean mFromService=false;
 	//	private Cursor mNoticesCursor;
 	NoticeListAdapter mNoticeCursorAdapter = null;
 	protected SharedPreferences mPreferences;
 
 	//private Timer mAutoRefreshTimer ;
 	protected boolean mAutoRefresh=false;
 
 	protected boolean mLayoutLegacy=false;
 	private boolean mLayoutNewButton=false;
 
 	private int mTextSizeNormal=14;
 	private int mTextSizeSmall=12;
 
 	//	protected static boolean isMainTimeline = false;
 
 
 	@Override
 	public boolean onSearchRequested() {
 		doSearch();
 		return false;
 	}
 
 	protected MustardDbAdapter getDbAdapter() {
 		MustardDbAdapter dbAdapter = new MustardDbAdapter(this);
 		dbAdapter.open();
 		return dbAdapter;
 	}
 
 	class NoticeListAdapter extends ArrayAdapter<RowStatus> {
 
 		private boolean mLoading = true;
 
 		private HashMap<Long, String> hmAccounts;
 
 		class ViewHolder {
 
 			RemoteImageView profile_image;
 			ImageButton noticeinfo;
 			ImageButton geolocation;
 			ImageButton conversation;
 			TextView screen_name;
 			TextView account_name;
 			MustardStatusTextView status;
 			TextView datetime;
 			TextView source;
 			MustardStatusTextView in_reply_to;
 			ImageButton attachment;
 			View bottomRow;
 		}
 
 		/**
 		 * @param context
 		 * @param layout
 		 * @param c
 		 * @param from
 		 * @param to
 		 */
 		public NoticeListAdapter(ArrayList<RowStatus> statuses) {
 			super(MustardBaseActivity.this, 0, statuses);
 		}
 
 		public void setLoading(boolean loading) {
 			mLoading = loading;
 		}
 
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			final RowStatus status = getItem(position);
 			View v;
 			if (convertView != null) {
 				v = convertView;
 			} else {
 				v = getLayoutInflater().inflate(R_ROW_ID,
 						parent, false);
 			}
 			//				Log.d(TAG, "newView");
 			ViewHolder vh =  (ViewHolder)v.getTag();
 			if(vh == null) {
 				vh = new ViewHolder();
 				try {
 					vh.profile_image = (RemoteImageView)v.findViewById(R.id.profile_image);
 				} catch (Exception e) {
 				}
 
 				vh.noticeinfo = (ImageButton)v.findViewById(R.id.noticeinfo);
 				vh.geolocation = (ImageButton)v.findViewById(R.id.geolocation);          
 				vh.screen_name = (TextView)v.findViewById(R.id.screen_name);
 				try {
 					vh.account_name = (TextView)v.findViewById(R.id.account_name);
 				} catch (Exception e) {
 
 				}
 				vh.status = (MustardStatusTextView)v.findViewById(R.id.status);
 				Typeface tf = Typeface.createFromAsset(getAssets(),MustardApplication.MUSTARD_FONT_NAME);
 				vh.status.setTypeface(tf);
 				vh.datetime = (TextView)v.findViewById(R.id.datetime);
 				vh.datetime.setTypeface(tf);
 				vh.source = (TextView)v.findViewById(R.id.source);
 				vh.source.setTypeface(tf);
 				try {
 					vh.bottomRow = (View)v.findViewById(R.id.bottom_row);
 				}catch (Exception e) {
 				} 
 				try {
 					vh.conversation = (ImageButton)v.findViewById(R.id.conversation);
 				}catch (Exception e) {
 				}
 				try {
 					vh.in_reply_to = (MustardStatusTextView)v.findViewById(R.id.in_reply_to);
 					vh.in_reply_to.setTypeface(tf);
 				} catch (Exception e) {
 				}
 				vh.attachment = (ImageButton)v.findViewById(R.id.attachment);
 				v.setTag(vh);
 			}
 
 
 			if (hmAccounts==null) {
 				hmAccounts = new HashMap<Long,String>();
 				if (MustardApplication.DEBUG)
 					Log.i(TAG, "############################## CREATO hmAccounts ##########");
 			}
 			final long id = status.getId();
 			//			final long statusId = status.getStatusId();
 
 			v.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					onShowNoticeMenu(v,id);
 				}
 			});
 			v.setOnLongClickListener(new View.OnLongClickListener() {
 				public boolean onLongClick(View v) {
 //					openContextMenu(v);
 					
 					onShowContextMenu(v,id);
 					return true;
 				}
 			});
 
 
 			long accountId = status.getAccountId();
 			if (vh.screen_name != null) {
 				vh.screen_name.setText(status.getScreenName());
 				vh.screen_name.setTextSize(mTextSizeSmall);
 			}
 			boolean isTwitterStatus=mStatusNet.isTwitterInstance();
 			if ( mMergedTimeline && vh.account_name != null) {
 
 				if(!hmAccounts.containsKey(accountId)) {
 					MustardDbAdapter mDbHelper = getDbAdapter();
 					Cursor c = mDbHelper.fetchAccount(accountId);
 					String account = "";
 					if (c.moveToNext()) {
 						account=c.getString(c.getColumnIndex(MustardDbAdapter.KEY_USER));
 						String instance = c.getString(c.getColumnIndex(MustardDbAdapter.KEY_INSTANCE));
 						try {
 							URL url = new URL(instance);
 							account +=  "@" + url.getHost() + url.getPath();
 							//	        					Log.i(TAG, "AccountID " + accountId + " => " + account + " " + host + " (" +instance +")");
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					} else {
 						Log.e(TAG,"NO ACCOUNT WITH ID: " + accountId);
 					}
 					try {
 						c.close();
 					} catch (Exception e) {
 
 					} finally {
 						mDbHelper.close();
 					}
 					hmAccounts.put(accountId, account);
 				}
 				vh.account_name.setText(hmAccounts.get(accountId));
 				isTwitterStatus=hmAccounts.get(accountId).endsWith("twitter.com");
 				vh.account_name.setVisibility(View.VISIBLE);
 				vh.account_name.setTextSize(mTextSizeSmall);
 			}
 			String source = status.getSource();
 			if (source != null && !"".equals(source)) {
 				source = Html.fromHtml("&nbsp;"+getString(R.string.from)+"&nbsp;" + source.replace("&lt;", "<").replace("&gt;", ">"))+" ";
 				if (source.trim().length()>15)
 					source = source.trim().substring(0, 15) +"..";
 				else
 					source=source.trim();
 			}
 			vh.source.setText(source, BufferType.SPANNABLE);
 			vh.source.setTextSize(mTextSizeSmall);
 
 			long inreplyto = status.getInReplyTo();
 			TextView vr = vh.in_reply_to;
 			if (vr != null) {
 				if (inreplyto > 0) {
 					vr.setText(Html.fromHtml("&nbsp;<a href='statusnet://conversation/"+id+"' >"+getString(R.string.show_conversation)+"</a>") , BufferType.SPANNABLE);
 					vr.setVisibility(View.VISIBLE);
 					vr.setTextSize(mTextSizeSmall);
 				} else {
 					vr.setVisibility(View.GONE);
 				}
 			}
 
 			if (vh.profile_image != null) {
 				String profileUrl = status.getProfileImage();
 				if (profileUrl != null && !"".equals(profileUrl)) {
 					vh.profile_image.setRemoteURI(profileUrl);
 					vh.profile_image.loadImage();
 				}
 				if(mLayoutNewButton) {
 					vh.profile_image.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							onShowContextMenu(v,id);
 						}
 					});
 				}
 				vh.profile_image.setFocusable(mLayoutNewButton);
 			}
 			Date d = new Date();
 			d.setTime(status.getDateTime());
 			vh.datetime.setText(DateUtils.getRelativeDate( mContext, d ));
 			vh.datetime.setTextSize(mTextSizeSmall);
 			if(mPreferences.getBoolean(Preferences.COMPACT_VIEW, true) ) {
 				if (vh.bottomRow!=null)
 					vh.bottomRow.setVisibility(View.GONE);
 				if(vh.in_reply_to !=null)
 					if(!mPreferences.getBoolean(Preferences.LAYOUT_SHOW_CONTEXT, false))
 						vh.in_reply_to.setVisibility(View.GONE);
 			} else {
 				if(vh.conversation != null) {
 					if(vh.in_reply_to != null) {
 						// In Grey bubble layout I have to hide the link
 						vh.in_reply_to.setVisibility(View.GONE);
 					}
 					if (inreplyto > 0) {
 						vh.conversation.setImageResource(R.drawable.conversation);
 						vh.conversation.setOnClickListener(new View.OnClickListener() {
 
 							public void onClick(View v) {
 								//									doOpenConversation(statusId);
 								doOpenConversation(id);
 							}
 
 						});
 
 					} else {
 						vh.conversation.setImageResource(R.drawable.conversation_disabled);
 					}
 					vh.conversation.setFocusable(mLayoutNewButton);
 				}
 
 				int geo = status.getGeolocation();
 				if (geo == 1) {
 					final String lon = status.getLon();
 					final String lat = status.getLat();
 					vh.geolocation.setOnClickListener(new View.OnClickListener() {
 
 						public void onClick(View v) {
 							doShowLocation(lon, lat);
 						}
 
 					});
 					vh.geolocation.setImageResource(R.drawable.pin_button);
 				} else {
 					vh.geolocation.setImageResource(R.drawable.pin_disabled);
 				}
 				vh.geolocation.setFocusable(mLayoutNewButton);
 
 				int attachment = status.getAttachment();
 				if (attachment == 1) {
 
 					vh.attachment.setOnClickListener(new View.OnClickListener() {
 
 						public void onClick(View v) {
 							onShowAttachemntList(id);
 						}
 
 					});
 					vh.attachment.setImageResource(R.drawable.attachment);
 				} else {
 					vh.attachment.setImageResource(R.drawable.attachment_disabled);
 				}
 				vh.attachment.setFocusable(mLayoutNewButton);
 
 			}
 			String sstatus = status.getStatus();
 			if (sstatus.indexOf("<")>=0)
 				sstatus=sstatus.replaceAll("<", "&lt;");
 			if(sstatus.indexOf(">")>=0)
 				sstatus=sstatus.replaceAll(">","&gt;");
 
 			TextView tv = vh.status;
 			tv.setText(Html.fromHtml(sstatus).toString(), BufferType.SPANNABLE);
 			Linkify.addLinks(tv, Linkify.WEB_URLS);
 			//				if (mMergedTimeline) {
 			StatusNetUtils.linkifyUsers(tv,accountId);
 			if(isTwitterStatus) {
 				StatusNetUtils.linkifyGroupsForTwitter(tv,accountId);
 				StatusNetUtils.linkifyTagsForTwitter(tv,accountId);
 			} else {
 				StatusNetUtils.linkifyGroups(tv,accountId);
 				StatusNetUtils.linkifyTags(tv,accountId);
 			}
 			//				} else {
 			//					StatusNetUtils.linkifyUsers(v);
 			//					if(isTwitterStatus) {
 			//						StatusNetUtils.linkifyGroupsForTwitter(v);
 			//						StatusNetUtils.linkifyTagsForTwitter(v);
 			//					} else {
 			//						StatusNetUtils.linkifyGroups(v);
 			//						StatusNetUtils.linkifyTags(v);
 			//					}
 			//				}
 			tv.setTextSize(mTextSizeNormal);
 			if (vh.noticeinfo !=null) {
 
 				if(mLayoutNewButton) {
 
 					vh.noticeinfo.setOnClickListener(new View.OnClickListener() {
 
 						public void onClick(View v) {
 							onShowNoticeAction(id);
 						}
 
 					});
 
 				} else {
 
 					vh.noticeinfo.setVisibility(View.GONE);
 				}
 				vh.noticeinfo.setFocusable(mLayoutNewButton);
 			}
 
 			return v;
 		}
 
 
 
 		public boolean isEmpty() {
 			if (mLoading) {
 				// We don't want the empty state to show when loading.
 				return false;
 			} else {
 				return super.isEmpty();
 			}
 		}
 
 
 	}
 
 
 	private void doReply(long id) {
 		MustardUpdate.actionReply(this,mHandler, id);
 		dismissQuickAction();
 	}
 	
 	private void doForward(long id) {
 		MustardUpdate.actionForward(this,mHandler, id);
 		dismissQuickAction();
 	}
 	
 	
 	private void doFavorite(long rowid, boolean favorited) {
 		dismissQuickAction();
 		if (favorited)
 			new StatusDisfavor().execute(rowid);
 		else
 			new StatusFavor().execute(rowid);
 	}
 	
 	private void onShowNoticeMenu(View v,final long rowid) {
 
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		Cursor c = mDbHelper.fetchStatus(rowid);
 		BitmapDrawable _icon = new BitmapDrawable(
 				MustardApplication.sImageManager.get(
 						c.getString(
 								c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_IMAGE)
 						)
 				)
 		);
 		String userName = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 		final boolean favorited = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FAVORITE)) == 1 ? true : false;
 		long in_reply_to = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_IN_REPLY_TO));
 		int geo = c.getInt(c.getColumnIndex(MustardDbAdapter.KEY_GEO));
 		int attachment = c.getInt(c.getColumnIndex(MustardDbAdapter.KEY_ATTACHMENT));
 		final String lon = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LON));
 		final String lat = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LAT));
 
 		try { c.close(); } catch (Exception e) {} finally { mDbHelper.close(); }
 		//		Log.v(TAG, "Username id: " + usernameId + " vs " + mStatusNet.getUsernameId());
 
 		View tv = findViewById(R.id.dent_info);
 		if (tv == null)
 			tv = v;
 		mQuickAction = new QuickAction(tv);
 		
 		ActionItem iconItem = new ActionItem();
 		iconItem.setTitle(userName);
 		iconItem.setIcon(_icon);
 		mQuickAction.addActionItem(iconItem);
 		
 		ActionItem replyItem = new ActionItem();
 		replyItem.setTitle(getString(R.string.menu_reply));
 		replyItem.setIcon(getResources().getDrawable(R.drawable.n_icon_reply));
 		replyItem.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				doReply(rowid);
 			}
 		});
 		mQuickAction.addActionItem(replyItem);
 		
 		
 		ActionItem forwardAction = new ActionItem();
 		forwardAction.setTitle(getString(R.string.menu_forward));
 		forwardAction.setIcon(getResources().getDrawable(R.drawable.n_icon_forward));
 		forwardAction.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				doForward(rowid);
 			}
 		});
 		mQuickAction.addActionItem(forwardAction);
 		
 		ActionItem repeatAction = new ActionItem();
 		repeatAction.setTitle(getString(R.string.menu_repeat));
 		repeatAction.setIcon(getResources().getDrawable(R.drawable.n_icon_repeat));
 		repeatAction.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				doRepeat(rowid);
 			}
 		});
 		mQuickAction.addActionItem(repeatAction);
 		
 		ActionItem favAction = new ActionItem();
 		if(favorited) {
 			favAction.setTitle(getString(R.string.menu_unfav));
 			favAction.setIcon(getResources().getDrawable(R.drawable.n_icon_favorite));
 			
 		} else {
 			favAction.setTitle(getString(R.string.menu_fav));
 			favAction.setIcon(getResources().getDrawable(R.drawable.n_icon_disfavorite));	
 		}
 		favAction.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				doFavorite(rowid,favorited);
 			}
 		});
 		mQuickAction.addActionItem(favAction);
 		
 		if (in_reply_to > 0 && isConversationEnable) {
 			
 			ActionItem conversationAction = new ActionItem();
 			conversationAction.setTitle(getString(R.string.menu_conversation));
 			conversationAction.setIcon(getResources().getDrawable(R.drawable.n_icon_conversation));
 			conversationAction.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doOpenConversation(rowid);
 				}
 			});
 			mQuickAction.addActionItem(conversationAction);
 		}
 		
 		if (attachment>0) {
 			
 			ActionItem attachmentAction = new ActionItem();
 			attachmentAction.setTitle(getString(R.string.menu_view_attachment));
 			attachmentAction.setIcon(getResources().getDrawable(R.drawable.n_icon_attachment));
 			attachmentAction.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					onShowAttachemntList(rowid);
 				}
 			});
 			mQuickAction.addActionItem(attachmentAction);
 			
 		}
 		
 		if (geo == 1) {
 			ActionItem deleteAction = new ActionItem();
 			deleteAction.setTitle(getString(R.string.menu_view_geo));
 			deleteAction.setIcon(getResources().getDrawable(R.drawable.n_icon_geo));
 			deleteAction.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doShowLocation(lon,lat);
 				}
 			});
 			mQuickAction.addActionItem(deleteAction);
 			
 		}
 
 		mQuickAction.show();
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		mContext = this;
 		if(savedInstanceState != null) {
 			mFromSavedState=true;
 		}
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 
 		String sFontSize=mPreferences.getString(Preferences.FONT_SIZE, "1");
 		//		Log.i(TAG,"Font Size: " + sFontSize);
 		int fontSize=1;
 		try {
 			fontSize=Integer.parseInt(sFontSize);
 		} catch (NumberFormatException e) {
 			// Not sure but got a cast exception..
 			if(sFontSize.equals(getString(R.string.small))) {
 				fontSize=0;
 			} else if (sFontSize.equals(getString(R.string.medium))) {
 				fontSize=1;
 			} else {
 				fontSize=2;
 			}
 		}
 
 
 		switch (fontSize) {
 		case 0:
 			mTextSizeNormal=12;
 			mTextSizeSmall=10;
 			break;
 		case 1:
 			mTextSizeNormal=14;
 			mTextSizeSmall=12;
 			break;
 		case 2:
 			mTextSizeNormal=16;
 			mTextSizeSmall=14;
 			break;
 		}
 
 		mLayoutLegacy = 
 			mPreferences.getString(Preferences.THEME, getString(R.string.theme_bw))
 			.equals(getString(R.string.theme_bw));
 		if(!mPreferences.getBoolean(Preferences.COMPACT_VIEW, true))
 			mLayoutNewButton= mPreferences.getBoolean(Preferences.LAYOUT_NEW_BUTTON, false);
 		if (MustardApplication.DEBUG) Log.i(TAG,"onCreate");
 
 		if(mLayoutLegacy) {
 			R_ROW_ID=R.layout.legacy_timeline_list_item;
 		} else {
 			R_ROW_ID=R.layout.timeline_list_item;
 		}
 		onSetListView();
 		ListView view = null;
 		try {
 			view = (GimmeMoreListView)getListView();
 			((GimmeMoreListView)view).setOnNeedMoreListener(this);
 		} catch(ClassCastException e){ 
 			Log.e(TAG," change view type!!");
 			view = getListView();
 		}
 
 //		registerForContextMenu(view);
 
 		onSetupTimeline();
 		onBeforeSetAccount();
 		getStatusNet();
 
 		if (mStatusNet == null) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "No account found. Starting Login activity");
 			showLogin();
 		} else {
 			if (MustardApplication.DEBUG) Log.i(TAG, "calling onBeforeFetch()");
 			onBeforeFetch();
 			changeTitle();
 			//			onStartScheduler();
 		}
 	}
 
 	protected abstract void onSetListView() ;
 
 	private void onShowNoticeAction(final long rowid) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		Cursor c = mDbHelper.fetchStatus(rowid);
 		String text =  c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS));
 		boolean fav =  c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FAVORITE)) == 1 ? true : false;
 		View view = LayoutInflater.from(this).inflate(R.layout.notice_action, null);
 		TextView tv = (TextView)view.findViewById(R.id.text_status);
 		tv.setText(text);
 		final Context context = this;
 
 		Button ibr = (Button)view.findViewById(R.id.button_reply);
 		ibr.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				MustardUpdate.actionReply(context,mHandler, rowid);
 				closeNoticeDialog();
 			}
 
 		});
 
 		Button ibf = (Button)view.findViewById(R.id.button_repeat);
 		ibf.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				boolean newRepeat = mPreferences.getBoolean(Preferences.NEW_REPEAT_ENABLES_KEY, false);
 				if(newRepeat) {
 					doRepeat(rowid);
 				} else {
 					MustardUpdate.actionForward(context,mHandler, rowid);
 				}
 				closeNoticeDialog();
 			}
 
 		});
 		
 		Button ib = (Button)view.findViewById(R.id.button_fav);
 		if(!fav) {
 			ib.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,  R.drawable.favorited);
 			ib.setText(R.string.menu_fav);
 			ib.setOnClickListener(new View.OnClickListener() {
 
 				public void onClick(View v) {
 					new StatusFavor().execute(rowid);
 					closeNoticeDialog();
 				}
 
 			});
 
 		} else {
 			ib.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.unfavorited);
 			ib.setText(R.string.menu_unfav);
 			ib.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					new StatusDisfavor().execute(rowid);
 					closeNoticeDialog();
 				}
 			});
 		}
 		BitmapDrawable _icon = new BitmapDrawable(
 				MustardApplication.sImageManager.get(
 						c.getString(
 								c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_IMAGE)
 						)
 				)
 		);
 		String userName = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 
 		try { c.close(); } catch (Exception e) { } finally { mDbHelper.close(); }
 		builder.setIcon(_icon);
 		builder.setView(view);
 		builder.setCancelable(true);
 		builder.setTitle(userName);
 		builder.setPositiveButton(R.string.close, null);
 		noticeDialog = builder.create();
 		noticeDialog.show();
 	}
 
 	private AlertDialog noticeDialog;
 
 	private void closeNoticeDialog() {
 		noticeDialog.dismiss();
 	}
 
 	private void onShowAttachemntList(long statusId) {
 		dismissQuickAction();
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		Cursor c = mDbHelper.fetchAttachment(statusId);
 		final CharSequence[] items = new CharSequence[c.getCount()];
 		final ArrayList<Attachment> attachments = new ArrayList<Attachment>();
 		int cc=0;
 		while(c.moveToNext()) {
 			Attachment a = new Attachment();
 			String mimeType = c.getString(c.getColumnIndex(MustardDbAdapter.KEY_MIMETYPE));
 			a.setMimeType(mimeType);
 			a.setUrl(c.getString(c.getColumnIndex(MustardDbAdapter.KEY_URL)));
 			attachments.add(a);
 			if (mimeType.startsWith("image")) {
 				items[cc]="Image";
 			} else if (mimeType.startsWith("text/html")) {
 				items[cc]="Html";
 			} else {
 				items[cc]="Unknown";
 			}
 			cc++;
 		}
 		try {
 			c.close();
 		} catch (Exception e) {} finally { mDbHelper.close(); }
 		if (attachments.size() > 1) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("View attachment");
 
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface xdialog, int item) {
 					Attachment a = attachments.get(item);
 					if (a.getMimeType().startsWith("image")) {
 						showAttachmentImage(a.getUrl(),true);
 					} else if (a.getMimeType().startsWith("text/html")){
 						showAttachmentText(a.getUrl());
 					}
 				}
 			});
 			builder.create();
 			builder.setPositiveButton(R.string.close, null);
 			builder.show();	
 		} else {
 			Attachment a = attachments.get(0);
 			if (a.getMimeType().startsWith("image")) {
 				showAttachmentImage(a.getUrl(),true);
 			} else if (a.getMimeType().startsWith("text/html")){
 				showAttachmentText(a.getUrl());
 			}
 		}
 	}
 
 	void showAttachmentImage(String url, boolean extraLink) {
 
 		View view = LayoutInflater.from(this).inflate(R.layout.html, null);
 		WebView html = (WebView)view.findViewById(R.id.html);
 		String summary = "<html><body>" +
 		"<center>" +
 		"<img width=\"100%\" src=\""+url+"\"/>";
 		if (extraLink)
 			summary += "<br/><a href=\""+url+"\">Open with Browser</a></center>";
 
 		summary += "</body></html>";
 		html.loadDataWithBaseURL("fake://this/is/not/real",summary, "text/html", "utf-8","");
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setIcon(R.drawable.attachment);
 		builder.setView(view);
 		builder.setCancelable(true);
 		builder.setTitle("View Image");
 		builder.setPositiveButton(R.string.close, null);
 		builder.create().show();
 
 	}
 
 	void showAttachmentText(String url) {
 
 		View view = LayoutInflater.from(this).inflate(R.layout.html, null);
 
 		WebView html = (WebView)view.findViewById(R.id.html);
 		html.loadUrl(url);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(view);
 		builder.setIcon(R.drawable.attachment);
 		builder.setCancelable(true);
 		builder.setTitle("View Text");
 		builder.setPositiveButton(R.string.close, null);
 		builder.create().show();
 
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onResume()");
 		mIsOnSaveInstanceState=false;
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onRestart()");
 		mIsOnSaveInstanceState=false;
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		super.onRestoreInstanceState(state);
 		if (MustardApplication.DEBUG) Log.i(TAG, "onRestoreInstanceState()");
 		mIsOnSaveInstanceState=false;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onPause()");
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onStart()");
 		mIsOnSaveInstanceState=false;
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onStop()");
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		if (MustardApplication.DEBUG) Log.i(TAG, "onConfigurationChanged()");
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		Log.i(TAG,"onSaveInstanceState()");
 		mIsOnSaveInstanceState=true;
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public void onDestroy() {
 
 		super.onDestroy();
 //		if (MustardApplication.DEBUG) 
 			Log.i(TAG,"onDestroy()");
 
 		
 		if(!mIsOnSaveInstanceState) {
 			MustardDbAdapter mDbHelper = getDbAdapter();
 //			if (MustardApplication.DEBUG) 
 				Log.i(TAG,"deleting dents");
 
 			try {
 				if(isTaskRoot()) {
 					if(mMergedTimeline)
 						mDbHelper.deleteOlderMergedStatuses(DB_ROW_TYPE,DB_ROW_EXTRA);
 					else
 						mDbHelper.deleteOlderStatuses(DB_ROW_TYPE,DB_ROW_EXTRA);
 				} else {
 					mDbHelper.deleteStatuses(DB_ROW_TYPE,DB_ROW_EXTRA);
 				}
 			} catch (Exception e) {
 				if (MustardApplication.DEBUG) e.printStackTrace();
 			} finally {
 				mDbHelper.close();
 			}
 		} else {
 			Log.i(TAG,"mIsOnSaveInstanceState == true");
 		}
 	}
 
 	protected void onPreCreateOptionsMenu(Menu menu) {
 	}
 
 	protected void onPostCreateOptionsMenu(Menu menu) {
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		onPreCreateOptionsMenu(menu);
 
 		if(mForceOnlyBackMenu) {
 			menu.add(0, BACK_ID, 0, R.string.menu_back)
 			.setIcon(android.R.drawable.ic_menu_revert);
 			return true;
 		}
 		if(isTaskRoot()) {
 			menu.add(0, INSERT_ID, 0, R.string.menu_insert)
 			.setIcon(android.R.drawable.ic_menu_add);
 		}
 
 		if(isRefreshEnable)
 			menu.add(0, REFRESH_ID, 0, R.string.menu_refresh)
 			.setIcon(android.R.drawable.ic_menu_rotate);
 
 		if(isTaskRoot()) {
 			menu.add(0, MENTIONS_ID, 0, R.string.menu_mentions)
 			.setIcon(android.R.drawable.ic_menu_mylocation);
 			menu.add(0, SEARCH_ID, 0, R.string.menu_search)
 			.setIcon(android.R.drawable.ic_menu_search);
 			menu.add(0, BOOKMARKS_ID, 0, R.string.menu_bookmarks)
 			.setIcon(android.R.drawable.ic_menu_compass);
 			menu.add(0, DM_ID, 0, R.string.menu_dm)
 			.setIcon(android.R.drawable.ic_menu_day);
 			menu.add(0, FAVORITES_ID, 0, R.string.menu_favorites)
 			.setIcon(android.R.drawable.ic_menu_recent_history);
 			menu.add(0, PUBLIC_ID, 0, R.string.menu_public)
 			.setIcon(android.R.drawable.ic_menu_myplaces);
 			menu.add(0, SWITCH_ID, 0, R.string.menu_switch)
 			.setIcon(android.R.drawable.ic_menu_directions);
 //			menu.add(0, ACCOUNT_SETTINGS_ID, 0, R.string.menu_account_settings)
 //			.setIcon(android.R.drawable.ic_menu_gallery);
 			menu.add(0, SETTINGS_ID, 0, R.string.menu_settings)
 			.setIcon(android.R.drawable.ic_menu_preferences);
 			menu.add(0, LOGOUT_ID, 0, R.string.menu_logout)
 			.setIcon(android.R.drawable.ic_menu_delete);
 			menu.add(0, ABOUT_ID, 0, R.string.menu_about)
 			.setIcon(android.R.drawable.ic_menu_info_details);
 		} else {
 			if(isBookmarkEnable) {
 				menu.add(0, BOOKMARK_THIS_ID, 0, R.string.menu_bookmark_this_page)
 				.setIcon(android.R.drawable.btn_star);
 			}
 			menu.add(0, BACK_ID, 0, R.string.menu_back)
 			.setIcon(android.R.drawable.ic_menu_revert);
 		}
 		onPostCreateOptionsMenu(menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case INSERT_ID:
 			doCompose();
 			return true;
 		case REFRESH_ID:
 			doRefresh();
 			return true;
 		case MENTIONS_ID:
 			getMentions();
 			return true;
 		case PUBLIC_ID:
 			getPublic();
 			return true;
 		case DM_ID:
 			getDM();
 			return true;
 		case LOGOUT_ID:
 			logout();
 			return true;
 		case SWITCH_ID:
 			switchUser();
 			return true;
 		case SEARCH_ID:
 			doSearch();
 			return true;
 		case BOOKMARKS_ID:
 			doBookmark();
 			return true;
 		case ABOUT_ID:
 			AboutDialog.show(this);
 			return true;
 		case BACK_ID:
 			setResult(RESULT_OK);
 			finish();
 			return true;
 		case BOOKMARK_THIS_ID:
 			bookmarkThis();
 			return true;
 		case FAVORITES_ID:
 			getFavorites();
 			return true;
 		case FRIENDS_ID:
 			getFriends();
 			return true;
 //		case ACCOUNT_SETTINGS_ID:
 //			AccountSettings.actionAccountSettings(this);
 //			return true;
 		case SETTINGS_ID:
 			settings();
 			return true;
 		case GROUP_LEAVE_ID:
 			doLeaveGroup();
 			return true;
 		case GROUP_JOIN_ID:
 			doJoinGroup();
 			return true;
 		case SUB_ID:
 			doSubscribe();
 			return true;
 		case UNSUB_ID:
 			doUnsubscribe();
 			return true;			
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	protected abstract void onBeforeFetch() ;
 
 	protected void onBeforeSetAccount() {
 		Intent intent = getIntent();
 		Uri data = intent.getData();
 		if (intent.hasExtra(EXTRA_ACCOUNT)) {
 			mStatusNetAccountId=intent.getLongExtra(EXTRA_ACCOUNT,-1);
 			Log.d(TAG, "Got an EXTRA_ACCOUNT: " + mStatusNetAccountId);
 		} else {
 			if (data != null) {
 				Log.d(TAG, data.toString());
 				List<String> segs = data.getPathSegments();
 				if (segs.size()>1) {
 					try {
 						mStatusNetAccountId=Long.valueOf(segs.get(0));
 						Log.d(TAG, "Got an EXTRA_ACCOUNT: " + mStatusNetAccountId);
 					} catch (NumberFormatException e) {
 
 					}
 				}
 			}
 		}
 	}
 
 	protected void onSetupTimeline() {
 	}
 
 	public void needMore() {
 		if(MustardApplication.DEBUG) Log.d(TAG,"Asked for more!");
 		doLoadMore();
 	}
 
 	protected void doRefresh() {
 		mIsRefresh=true;
 		getStatuses();
 	}
 
 	private void switchUser() {
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		Cursor c = mDbHelper.fetchAllNonDefaultAccounts();
 		final CharSequence[] items = new CharSequence[c.getCount()+1];
 		final long[] rowIds = new long[c.getCount()];
 		int cc=0;
 		while(c.moveToNext()) {
 			items[cc]=c.getString(c.getColumnIndex(MustardDbAdapter.KEY_INSTANCE)) +
 			"/" + c.getString(c.getColumnIndex(MustardDbAdapter.KEY_USER));
 			rowIds[cc]=c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 			cc++;
 		}
 		items[cc]=getString(R.string.menu_add_new);
 		c.close();
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.menu_choose_account));
 
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int item) {
 				MustardDbAdapter mDbHelper = getDbAdapter();
 				mDbHelper.resetDefaultAccounts();
 				mDbHelper.deleteStatuses(MustardDbAdapter.ROWTYPE_ALL, "");
 				mDbHelper.deleteStatuses(DB_ROW_TYPE,DB_ROW_EXTRA);
 				if(mFetcherTask!=null) {
 					mFetcherTask.cancel(true);
 				}
 				mFetcherTask = null;
 				if (items[item].equals(getString(R.string.menu_add_new))) {
 					if (mNoticeCursorAdapter!=null) {
 						mNoticeCursorAdapter.notifyDataSetInvalidated();
 					}
 					showLogin();
 				} else {
 					mDbHelper.setDefaultAccount(rowIds[item]);
 					startMainTimeline();
 				}
 				mDbHelper.close();
 			}
 		});
 		builder.create();
 		builder.show();
 		mDbHelper.close();
 	}
 
 	private void startMainTimeline() {
 		MustardMain.actionHandleTimeline(this);
 		finish();
 	}
 
 	private void changeTitle() {
 		if (mStatusNet != null) {
 			String hostname = mStatusNet.getURL().getHost();
 			setTitle(getString(R.string.app_name)  + " - " + mStatusNet.getMUsername() + "@" + ( hostname.endsWith("twitter.com") ? "twitter" : hostname) );
 		}
 	}
 
 	private void showLogin() {
 		Login.actionHandleLogin(this);
 		finish();
 	}
 
 	private void logout() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.msg_logout))
 		.setCancelable(false)
 		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				MustardDbAdapter mDbHelper = getDbAdapter();
 				mDbHelper.deleteAccount(mStatusNet.getUserId());
 				mDbHelper.deleteBookmarks(mStatusNet.getUserId());
 				mDbHelper.deleteStatuses(MustardDbAdapter.ROWTYPE_ALL, "");
 				mDbHelper.close();
 				finish();
 			}
 		})
 		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				xdialog.cancel();
 			}
 		});
 		builder.create();
 		builder.show();
 	}
 
 	private void doRepeat(final long rowid) {
 		dismissQuickAction();
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.msg_confirm_repeat))
 		.setCancelable(false)
 		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				new StatusRepeat().execute(rowid);
 			}
 		})
 		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				xdialog.cancel();
 			}
 		});
 		
 		builder.setIcon(getResources().getDrawable(R.drawable.n_icon_repeat));
 		
 		builder.create();
 		builder.show();
 		
 	}
 
 	private void doBlock(final long rowid, final boolean block) {
 		dismissQuickAction();
 		final Context context = this;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString( block ? R.string.msg_confirm_block : R.string.msg_confirm_unblock))
 		.setCancelable(false)
 		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				if (block) {
 					new StatusBlock().execute(rowid);
 					MustardDbAdapter dbHelper = getDbAdapter();
 					RowStatus rs = getRowStatus(rowid, dbHelper);
 					StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 					if (mPreferences.getBoolean(Preferences.SPAMREPORT_ON_BLOCK, false) && 
 							sn.getAccount().getInstance().endsWith("identi.ca")) {
						MustardUpdate.actionSpamReport(context,mHandler, rs.getScreenName(), rs.getAccountId());
 					}
 					dbHelper.close();
 				} else {
 					new StatusUnblock().execute(rowid);
 				}
 			}
 		})
 		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				xdialog.cancel();
 			}
 		});
 		builder.create();
 		builder.show();
 	}
 
 	
 	private void getFriends() {
 		MustardFriend.actionHandleTimeline(this, DB_ROW_EXTRA);
 	}
 
 	protected void getMentions() {
 		if(mMergedTimeline)
 			MustardMention.actionHandleTimeline(this, "-1");
 		else
 			MustardMention.actionHandleTimeline(this, mStatusNet.getMUsername());
 	}
 
 	private void getFavorites() {
 		MustardFavorite.actionHandleTimeline(this,DB_ROW_EXTRA);
 	}
 
 	private void getPublic() {
 		Intent i = new Intent("android.intent.action.VIEW",Uri.parse("statusnet://public/"));
 		startActivityForResult(i, ACTIVITY_PUBLIC);
 	}
 
 	private void getDM() {
 		Intent i = new Intent(this, DirectMessageTab.class);
 		startActivity(i);
 	}
 	
 	protected void doCompose() {
 		MustardUpdate.actionCompose(this,mHandler);
 	}
 
 	private void doSearch() {
 		Intent i = new Intent(this, Search.class);
 		startActivity(i);
 	}
 
 	private void doBookmark() {
 		Intent i = new Intent(this, Bookmark.class);
 		startActivity(i);
 	}
 
 	private void bookmarkThis() {
 		try {
 			MustardDbAdapter mDbHelper = getDbAdapter();
 			mDbHelper.createBookmark(mStatusNet.getUserId(), DB_ROW_TYPE, DB_ROW_EXTRA);
 			mDbHelper.close();
 		} catch (MustardException e) {
 			if(MustardApplication.DEBUG) Log.e(TAG, e.getMessage());
 		}
 	}
 
 	private void settings() {
 		Intent i = new Intent(this, Settings.class);
 		startActivity(i);
 	}
 
 	protected void showToastMessage(CharSequence message) {
 		showToastMessage(message,false);
 	}
 
 	protected void showToastMessage(CharSequence message,boolean longView) {
 		int popTime = longView ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
 		Toast.makeText(this,
 				message,
 				popTime).show();
 	}
 
 	protected void showAlertMessage(String errorTitle,String errorMessage) {
 		new AlertDialog.Builder(this)
 		.setTitle(errorTitle)
 		.setMessage(errorMessage)
 		.setNeutralButton(getString(R.string.close), null).show();
 	}
 
 	protected void showAlertMessageAndFinish(String errorTitle,String errorMessage) {
 		new AlertDialog.Builder(this)
 		.setTitle(errorTitle)
 		.setMessage(errorMessage)
 		.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				finish();
 			}
 		}).show();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		ProgressDialog dialog;
 		switch(id) {
 		case DIALOG_FETCHING_ID:
 			// do the work to define the pause Dialog
 			dialog = new ProgressDialog(this);
 			dialog.setIndeterminate(true);
 			dialog.setCancelable(true);
 			dialog.setMessage(getString(R.string.please_wait_fetching_dents));
 			break;
 
 		case DIALOG_OPENING_ID:
 			dialog = new ProgressDialog(this);
 			dialog.setIndeterminate(true);
 			dialog.setCancelable(true);
 			dialog.setMessage(getString(R.string.please_wait_opening));
 			break;
 
 		default:
 			if(MustardApplication.DEBUG) Log.d(TAG,"onCreateDialog null....");
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	protected long mStatusNetAccountId = -1;
 
 	private void getStatusNet() {
 		MustardApplication _ma = (MustardApplication) getApplication();
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		if (mStatusNetAccountId>=0) {
 			mStatusNet = _ma.checkAccount(mDbHelper,mStatusNetAccountId);
 		} else {
 			mStatusNet = _ma.checkAccount(mDbHelper);
 		}
 		mDbHelper.close();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (MustardApplication.DEBUG) Log.i(TAG,"onActivityResult");
 		super.onActivityResult(requestCode, resultCode, intent);
 		if (requestCode == ACCOUNT_ADD || requestCode == ACCOUNT_ADD_SWITCH) {
 			if (resultCode == RESULT_OK) {
 				// ... have to recheck
 				mStatusNet = null;
 				if (MustardApplication.DEBUG) 
 					Log.d(TAG,"Back OK from ActivityResult " + requestCode);
 				getStatusNet();
 				onBeforeFetch();
 				changeTitle();
 				getStatuses();
 			} else {
 				//				Log.i(TAG, "Finshed..." );
 				finish();
 			}
 		} else if (requestCode == ACCOUNT_DEL) {
 			if (!isTaskRoot()) {
 				setResult(ACCOUNT_DEL);
 			}
 			finish();
 		} else if (requestCode == ACTIVITY_EDIT || requestCode == ACTIVITY_CREATE) {
 			if(mPreferences.getBoolean(Preferences.REFRESH_ON_POST_ENABLES_KEY, false) && 
 					resultCode == RESULT_OK) {
 				if(MustardApplication.DEBUG) Log.d(TAG, "Refresh");
 				doRefresh();
 			}
 		}
 	}
 
 //	@Override
 //	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 //		super.onCreateContextMenu(menu, v, menuInfo);
 	
 	public void onShowContextMenu(View v,final long rowid) {
 		
 		
 		//		Log.d(TAG,"Set mCurrentRowid " + mCurrentRowid);
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		Cursor c = mDbHelper.fetchStatus(rowid);
 		
 		BitmapDrawable _icon = new BitmapDrawable(
 				MustardApplication.sImageManager.get(
 						c.getString(
 								c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_IMAGE)
 						)
 				)
 		);
 		
 		final long statusId = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID));
 		final String userName = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 		final long usernameId = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_ID));
 		final long accountId = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_ACCOUNT_ID));
 //		final String lon = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LON));
 //		final String lat = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LAT));
 		
 		
 		ActionItem share = new ActionItem();
 		ActionItem copy2clip = new ActionItem();
 		
 		View tv = findViewById(R.id.dent_info);
 		if (tv == null)
 			tv = v;
 		mQuickAction = new QuickAction(tv);
 
 		ActionItem iconItem = new ActionItem();
 		iconItem.setTitle(userName);
 		iconItem.setIcon(_icon);
 		mQuickAction.addActionItem(iconItem);
 		
 		
 		if(!mLayoutNewButton) {
 
 			final String text = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS));
 			
 			share.setTitle(getString(R.string.menu_share));
 			share.setIcon(getResources().getDrawable(R.drawable.n_icon_share));
 			share.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doShare(text);
 				}
 			});
 			mQuickAction.addActionItem(share);
 
 			copy2clip.setTitle(getString(R.string.menu_copy2clipboard));
 			copy2clip.setIcon(getResources().getDrawable(R.drawable.n_icon_clipboard));
 			copy2clip.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doCopy2Clipboard(rowid, statusId, userName);
 				}
 			});
 			mQuickAction.addActionItem(copy2clip);
 			
 			ActionItem userTimeline = new ActionItem();
 			userTimeline.setTitle(getString(R.string.menu_timeline));
 			userTimeline.setIcon(getResources().getDrawable(R.drawable.n_icon_usertimeline));
 			userTimeline.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doOpenUsertimeline(accountId, userName);
 				}
 			});
 			
 			mQuickAction.addActionItem(userTimeline);
 
 			if (usernameId != mStatusNet.getUsernameId()) {
 //
 				boolean following = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FOLLOWING)) == 1 ? true : false;
 				
 				ActionItem followAction = new ActionItem();
 				
 				if (following) {
 					
 					ActionItem dmAction = new ActionItem();
 					dmAction.setIcon(getResources().getDrawable(R.drawable.n_icon_dm));
 					dmAction.setTitle(getString(R.string.menu_dm));
 					dmAction.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							DirectMessageNew.actionCompose(mContext, userName);
 							dismissQuickAction();
 						}
 					});
 					mQuickAction.addActionItem(dmAction);
 
 //					menu.add(0, M_UNSUB_ID,0, R.string.menu_unsub);
 					followAction.setIcon(getResources().getDrawable(R.drawable.n_icon_unsubscribe));
 					followAction.setTitle(getString(R.string.menu_unsub));
 					
 					followAction.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							doManageSub(false,rowid);
 						}
 					});
 
 				} else {
 					followAction.setIcon(getResources().getDrawable(R.drawable.n_icon_subscribe));
 //					menuadd(0, M_SUB_ID,0, R.string.menu_sub);
 					followAction.setTitle(getString(R.string.menu_sub));
 					followAction.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							doManageSub(true,rowid);
 						}
 					});
 				}
 				mQuickAction.addActionItem(followAction);
 				
 				ActionItem blockAction = new ActionItem();
 				
 				
 				final boolean blocking = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_BLOCKING)) == 1 ? true : false;
 				if (blocking) {
 					blockAction.setIcon(getResources().getDrawable(R.drawable.n_icon_unblock));
 					blockAction.setTitle(getString(R.string.menu_unblock));
 				} else {
 					blockAction.setIcon(getResources().getDrawable(R.drawable.n_icon_block));
 					blockAction.setTitle(getString(R.string.menu_block));
 				}
 				blockAction.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						doBlock(rowid, !blocking);
 					}
 				});
 				mQuickAction.addActionItem(blockAction);
 			}
 //
 		} else {
 			ActionItem userTimeline = new ActionItem();
 			userTimeline.setTitle(getString(R.string.menu_timeline));
 			userTimeline.setIcon(getResources().getDrawable(R.drawable.n_icon_usertimeline));
 			userTimeline.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doOpenUsertimeline(accountId, userName);
 				}
 			});
 			mQuickAction.addActionItem(userTimeline);
 			
 //			menu.add(0, USER_TL_ID, 0, R.string.menu_timeline);
 //
 //			if (usernameId != mStatusNet.getUsernameId()) {
 
 			boolean following = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FOLLOWING)) == 1 ? true : false;
 			
 			ActionItem followAction = new ActionItem();
 			
 			if (following) {
 //				menu.add(0, M_UNSUB_ID,0, R.string.menu_unsub);
 				followAction.setIcon(getResources().getDrawable(R.drawable.n_icon_unsubscribe));
 				followAction.setTitle(getString(R.string.menu_unsub));
 				
 				followAction.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						doManageSub(false,rowid);
 					}
 				});
 
 			} else {
 //				menuadd(0, M_SUB_ID,0, R.string.menu_sub);
 				followAction.setIcon(getResources().getDrawable(R.drawable.n_icon_subscribe));
 				followAction.setTitle(getString(R.string.menu_sub));
 				followAction.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						doManageSub(true,rowid);
 					}
 				});
 			}
 			mQuickAction.addActionItem(followAction);
 			
 			ActionItem blockAction = new ActionItem();
 
 			
 			final boolean blocking = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_BLOCKING)) == 1 ? true : false;
 			if (blocking) {
 				blockAction.setIcon(getResources().getDrawable(R.drawable.n_icon_unblock));
 				blockAction.setTitle(getString(R.string.menu_unblock));
 			} else {
 				blockAction.setIcon(getResources().getDrawable(R.drawable.n_icon_block));
 				blockAction.setTitle(getString(R.string.menu_block));
 			}
 			blockAction.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doBlock(rowid,!blocking);
 				}
 			});
 			mQuickAction.addActionItem(blockAction);
 		}
 		if (usernameId == mStatusNet.getUsernameId()) {
 			ActionItem deleteAction = new ActionItem();
 			deleteAction.setTitle(getString(R.string.menu_delete));
 			deleteAction.setIcon(getResources().getDrawable(R.drawable.n_icon_delete));
 			deleteAction.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					doDelete(rowid,statusId);
 				}
 			});
 			mQuickAction.addActionItem(deleteAction);
 	
 //			menu.add(0, DELETE_ID, 0, R.string.menu_delete).setIcon(android.R.drawable.ic_delete);
 		}
 
 		
 		mQuickAction.show();
 		try {
 			c.close();
 		} catch(Exception e) {
 		} finally {
 			mDbHelper.close();
 		}
 	}
 
 	private void doOpenConversation(long rowid) {
 		dismissQuickAction();
 		MustardConversation.actionHandleTimeline(this,rowid);
 	}
 
 	private void doManageSub(boolean sub, long rowid) {
 		if(sub) {
 			new StatusSubscribe().execute(rowid);
 		} else {
 			new StatusUnsubscribe().execute(rowid);
 		}
 		dismissQuickAction();
 	}
 	
 	
 	private void doShowLocation(final String lon,final String lat) {
 		dismissQuickAction();
 		new Thread() {
 			public void run() {
 				Controller.getInstance(getApplication())
 				.loadGeoNames(getApplication(), lon, lat, mListener);
 			}
 		}.start();
 	}
 
 	private void doShowGeolocation(GeoName gn) {
 		Builder b = StatusNetUtils.getGeoInfo(this, gn);
 		b.show();
 	}
 
 	private void doShare(String text) {
 		Intent i = new Intent(Intent.ACTION_SEND);
 		i.setType("text/plain");
 		i.putExtra(Intent.EXTRA_TEXT, text);
 		startActivity(i);
 		dismissQuickAction();
 	}
 
 	private void dismissQuickAction() {
 		if(mQuickAction != null)
 			mQuickAction.dismiss();
 	}
 	
 	private void doOpenUsertimeline(long accountId,String screenname) {
 		MustardUser.actionHandleTimeline(this, accountId,screenname);
 		dismissQuickAction();
 	}
 	
 	private void doDelete(long rowid, long statusId) {
 		StatusNet sn = getStatusNetFromRowid(rowid);
 		if(sn.delete(Long.toString(statusId))) {
 			MustardDbAdapter mDbHelper = getDbAdapter();
 			try {
 				mDbHelper.deleteStatus(rowid);
 				Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
 				fillData();
 			} finally {
 				mDbHelper.close();
 			}
 		} else {
 			Toast.makeText(this, "Can't Delete", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	private void doCopy2Clipboard(long rowid, long statusId, String screenname) {
 		
 		StatusNet sn = getStatusNetFromRowid(rowid);
 		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 
 		if(sn.isTwitterInstance()) {
 			// http://twitter.com/#!/{user}/status/{id}
 			String url = "http://twitter.com/#!/"+screenname+"/status/"+statusId;
 			clipboard.setText(url);
 		} else {
 			// http://identi.ca/notice/{id}
 			String url = sn.getURL().toExternalForm();
 			if (url.endsWith("/api"))
 				url = url.substring(0, -4);
 			clipboard.setText(url+"/notice/"+statusId);
 		}
 		Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show();
 		dismissQuickAction();
 	}
 
 	private void doJoinGroup() {
 		new StatusGroupJoin().execute(DB_ROW_EXTRA);
 	}
 
 	protected void doSubscribe() {
 	}
 
 	protected void doUnsubscribe() {
 	}
 
 	private void doLeaveGroup() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.warning_leave_group,DB_ROW_EXTRA))
 		.setCancelable(false)
 		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				new StatusGroupLeave().execute(DB_ROW_EXTRA);
 			}
 		})
 		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				xdialog.cancel();
 			}
 		});
 		builder.create();
 		builder.show();
 	}
 
 	private void showIntederminateProgressBar(boolean show) {
 		setProgressBarIndeterminateVisibility(show);
 	}
 
 	protected abstract void onAfterFetch() ;
 
 	protected void doLoadMore() {
 		if (MustardApplication.DEBUG) Log.d(TAG, "Attempting load more.");
 
 		if (mLoadMoreTask != null
 				&& mLoadMoreTask.getStatus() == Status.RUNNING 
 		) {
 			if(MustardApplication.DEBUG) Log.w(TAG, "Already loading more.");
 		} else {
 			if (mMergedTimeline) {
 				mLoadMoreTask = new MergedStatusesLoadMore();
 				mLoadMoreTask.execute();
 			} else {
 				if (mNoMoreDents) {
 					if(MustardApplication.DEBUG) Log.w(TAG, "Reached NoMoreDent!");
 				} else {
 					mLoadMoreTask = new StatusesLoadMore();
 					mLoadMoreTask.execute();
 				}
 			}
 
 		}
 	}
 
 	protected void doSilentRefresh() {
 		if(MustardApplication.DEBUG) Log.d(TAG, "Silent Refresh.");
 
 		if(mFetcherTask != null
 				&& mFetcherTask.getStatus()==Status.RUNNING) {
 			if(MustardApplication.DEBUG) 
 				if(MustardApplication.DEBUG) Log.w(TAG, "Already fetching statuses");
 		} else {
 			if(MustardApplication.DEBUG) Log.i(TAG, "Fetching statuses silently");
 			if (mMergedTimeline)
 				mFetcherTask = new MultiStatusesFetcher();
 			else
 				mFetcherTask = new StatusesFetcher();
 			mFetcherTask.setSilent(true);
 			mFetcherTask.execute();
 		}
 	}
 
 	protected boolean mMergedTimeline=false;
 
 	protected void getStatuses() {
 		getStatuses(mMergedTimeline);
 	}
 
 	protected void getStatuses(boolean multiple) {
 		if(mFromSavedState) {
 			mFromSavedState=false;
 			fillData();
 			return;
 		}
 		if (MustardApplication.DEBUG) Log.d(TAG,"Attempting fetching statuses");
 		if(mFetcherTask != null
 				&& mFetcherTask.getStatus()==Status.RUNNING) {
 			if(MustardApplication.DEBUG) 
 				if(MustardApplication.DEBUG) Log.w(TAG, "Already fetching statuses");
 		} else {
 			if(MustardApplication.DEBUG) Log.i(TAG, "Fetching statuses");
 			if(multiple) {
 				mMergedTimeline=true;
 				//				Log.i(TAG, "MULTIPLE STATUS FETCHER!!!");
 				mFetcherTask = new MultiStatusesFetcher();
 				mFetcherTask.execute();
 			} else {
 				mFetcherTask = new StatusesFetcher();
 				mFetcherTask.execute();
 			}
 		}
 	}
 
 	protected void fillData() {
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		Cursor cursor = null;
 		ArrayList<RowStatus> statuses = new ArrayList<RowStatus>();
 		NoticeListAdapter noticeListAdapter = null;
 		try {
 
 			cursor = mDbHelper.fetchAllStatuses(DB_ROW_TYPE, DB_ROW_EXTRA,DB_ROW_ORDER);
 
 			if (cursor == null) {
 				Log.e(TAG, "Cursor is null.. ");
 				mDbHelper.close();
 				return;
 			}
 
 			noticeListAdapter = (NoticeListAdapter)getListView().getAdapter();
 			if(noticeListAdapter == null) {
 				noticeListAdapter = new NoticeListAdapter(statuses);
 				getListView().setAdapter(noticeListAdapter);
 			}
 			noticeListAdapter.clear();
 			int mIdIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_ROWID);
 			int mIdAccountIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_ACCOUNT_ID);
 			int mGeolocationIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_GEO);
 			int mScreenNameIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_SCREEN_NAME);
 			int mStatusIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_STATUS);
 			int mStatusIdIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_STATUS_ID);
 			int mDatetimeIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_INSERT_AT);
 			int mSourceIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_SOURCE);
 			int mInReplyToIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_IN_REPLY_TO);
 			int mProfileImageIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_USER_IMAGE);
 			int mLonIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_LON);
 			int mLatIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_LAT);
 			int mAttachmentIdx =  cursor.getColumnIndex(MustardDbAdapter.KEY_ATTACHMENT);
 			while (cursor.moveToNext()) {
 				RowStatus rs = new RowStatus();
 				rs.setId(cursor.getLong(mIdIdx));
 				rs.setStatusId(cursor.getLong(mStatusIdIdx));
 
 				rs.setAccountId(cursor.getLong(mIdAccountIdx));
 				rs.setScreenName(cursor.getString(mScreenNameIdx));
 				rs.setSource(cursor.getString(mSourceIdx)) ;
 				rs.setInReplyTo(cursor.getLong(mInReplyToIdx));
 				rs.setProfileImage(cursor.getString(mProfileImageIdx));
 				rs.setDateTime(cursor.getLong(mDatetimeIdx));
 				rs.setGeolocation(cursor.getInt(mGeolocationIdx));
 				rs.setLon(cursor.getString(mLonIdx));
 				rs.setLat(cursor.getString(mLatIdx));
 				rs.setAttachment(cursor.getInt(mAttachmentIdx));
 				rs.setStatus(cursor.getString(mStatusIdx));
 				noticeListAdapter.add(rs);
 			}
 		} finally {
 			try {
 				cursor.close();
 			} catch (Exception e) {
 			}
 			mDbHelper.close();
 		}
 
 		
 	}
 
 	public class StatusesFetcher extends AsyncTask<Void, Integer, Integer> {
 
 		private final String TAG = "StatusesFetcher";
 		protected boolean mGetdents=false;
 		protected boolean mSilent = false;
 
 		public void setSilent(boolean silent) {
 			mSilent=silent;
 		}
 
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) 
 				Log.i(TAG, "background task - start");
 
 			ArrayList<org.mustard.statusnet.Status> al = null;
 			MustardDbAdapter mDbHelper = getDbAdapter();
 			try {
 				if (mStatusNet==null) {
 					Log.e(TAG, "Statusnet is null!");
 					return 0;
 				}
 				long maxId = mDbHelper.fetchMaxStatusesId(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA);
 				al=mStatusNet.get(DB_ROW_TYPE,DB_ROW_EXTRA,maxId-1,true);
 				if(al==null || al.size()< 1) {
 					return 0;
 				} else {
 					// Ok BIG BUG!!!
 					// IF the array lowest id > maxId there's a hole and it will be never filled! So I delete all old dents
 					long lowestid = al.get(al.size()-1).getNotice().getId();
 //					Log.d(TAG,"Got " + lowestid +  " I have " + maxId );
 					if(maxId > 0 && lowestid > maxId) {
 						mDbHelper.deleteStatuses(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA,maxId);
 					}
 					mGetdents=mDbHelper.createStatuses(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA,al);
 				}
 			} catch(Exception e) {
 				if (MustardApplication.DEBUG) 
 					e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return -1;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 				mDbHelper.close();
 			}
 			return 1;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			if (MustardApplication.DEBUG) Log.i(TAG, "onPreExecute");
 			try {
 				if(mSilent)
 					showIntederminateProgressBar(true);
 				else
 					showDialog(MustardBaseActivity.DIALOG_FETCHING_ID);
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		protected void onPostExecute(Integer result) {
 			if(mSilent)
 				showIntederminateProgressBar(false);
 			else {
 				try { dismissDialog(MustardBaseActivity.DIALOG_FETCHING_ID); } catch (IllegalArgumentException e) {}
 			}
 			try {
 				if (result==-1) {
 					showToastMessage(getText(R.string.error_fetch_dents)+"\n"+mErrorMessage);
 				} else if (result == -10) {
 					showToastMessage("Merged timeline active but no account selected!");
 				} else {
 					if(mGetdents) {
 						onAfterFetch();
 					}
 					fillData();
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {
 				if(mIsRefresh)
 					mIsRefresh=false;
 			}
 		}
 
 	}
 
 	public class MultiStatusesFetcher extends StatusesFetcher {
 
 		private final String TAG = "MultiStatusesFetcher";
 
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) 
 				Log.i(TAG, "background task - start");
 
 			MustardDbAdapter mDbHelper = getDbAdapter();
 			try {
 				//				if (mStatusNet==null) {
 				//					Log.e(TAG, "Statusnet is null!");
 				//					return 0;
 				//				}
 
 				MustardApplication _ma = (MustardApplication) getApplication();
 				StatusNet _sn = null;
 				boolean haveAtLeastOneAccount=false;
 				boolean haveAtLeastOneStatus=false;
 				Cursor c = mDbHelper.fetchAllAccountsToMerge();
 				while(c.moveToNext()) {
 					ArrayList<org.mustard.statusnet.Status> al = null;
 					haveAtLeastOneAccount=true;
 					long _aid = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 					_sn = _ma.checkAccount(mDbHelper,false,_aid);
 					//					Log.i(TAG, "Fetching " + _sn.getMUsername() + "@" + _sn.getURL().getHost());
 					long maxId = mDbHelper.fetchMaxStatusesId(_aid, DB_ROW_TYPE,DB_ROW_EXTRA);
 					
 					al=_sn.get(DB_ROW_TYPE,DB_ROW_EXTRA,maxId-1,true);
 					if(al==null || al.size()< 1) {
 						continue;
 					} else {
 						haveAtLeastOneStatus=true;
 						// Ok BIG BUG!!!
 						// IF the array lowest id > maxId there's a hole and it will be never filled! So I delete all old dents
 						long lowestid = al.get(al.size()-1).getNotice().getId();
 						//Log.d(TAG,"Got " + lowestid +  " I have " + maxId );
 						if(maxId > 0 && lowestid > maxId) {
 							mDbHelper.deleteStatuses(_aid,DB_ROW_TYPE,DB_ROW_EXTRA,maxId);
 						}
 						mGetdents=mDbHelper.createStatuses(_aid,DB_ROW_TYPE,DB_ROW_EXTRA,al);
 					}
 				}
 				c.close();
 				if(!haveAtLeastOneAccount) {
 					return -10;
 				}
 				if(!haveAtLeastOneStatus) {
 					return 0;
 				}
 
 			} catch(Exception e) {
 				if (MustardApplication.DEBUG) 
 					e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return -1;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 				mDbHelper.close();
 			}
 			return 1;
 		}
 
 	}
 
 
 	public class StatusRepeat extends AsyncTask<Long, Integer, Integer> {
 
 		private final String TAG = "StatusRepeat";
 
 		@Override
 		protected Integer doInBackground(Long... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			MustardDbAdapter dbHelper = getDbAdapter();
 			RowStatus rs = getRowStatus(s[0], dbHelper);
 			StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 			try {
 				sn.doRepeat(Long.toString(rs.getStatusId()));
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				dbHelper.close();
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_repeat));
 				} else {
 					showToastMessage(getText(R.string.error_repeat)+"\n"+mErrorMessage,true);
 					mPreferences.edit().putBoolean(Preferences.NEW_REPEAT_ENABLES_KEY, false).commit();
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusFavor extends AsyncTask<Long, Integer, Integer> {
 
 		private final String TAG = "StatusFavor";
 
 		@Override
 		protected Integer doInBackground(Long... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			MustardDbAdapter dbHelper = getDbAdapter();
 			RowStatus rs = getRowStatus(s[0], dbHelper);
 			StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 			try {
 				sn.doFavour(Long.toString(rs.getStatusId()));
 				dbHelper.updateStatusFavor(s[0], true);
 			} catch(Exception e) {
 				e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 				dbHelper.close();
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_fav));
 				} else {
 					showToastMessage(getText(R.string.error_fav)+"\n"+mErrorMessage,true);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusDisfavor extends AsyncTask<Long, Integer, Integer> {
 
 		private final String TAG = "StatusDisfavor";
 
 		@Override
 		protected Integer doInBackground(Long... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			MustardDbAdapter dbHelper = getDbAdapter();
 			RowStatus rs = getRowStatus(s[0], dbHelper);
 			StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 			try {
 				sn.doDisfavour(Long.toString(rs.getStatusId()));
 				dbHelper.updateStatusFavor(s[0], false);
 			} catch(Exception e) {
 				e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 				dbHelper.close();
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_unfav));
 				} else {
 					showToastMessage(getText(R.string.error_unfav)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusBlock extends AsyncTask<Long, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(Long... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			MustardDbAdapter dbHelper = getDbAdapter();
 			RowStatus rs = getRowStatus(s[0], dbHelper);
 			StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 			try {
 				sn.doBlock(Long.toString(rs.getUserId()));
 				dbHelper.updateStatusBlocking(Long.toString(rs.getUserId()), true);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 				dbHelper.close();
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_block));
 				} else {
 					showToastMessage(getString(R.string.error_block,mErrorMessage));
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusUnblock extends AsyncTask<Long, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(Long... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			MustardDbAdapter dbHelper = getDbAdapter();
 			RowStatus rs = getRowStatus(s[0], dbHelper);
 			StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 			try {
 				sn.doUnblock(Long.toString(rs.getUserId()));
 				dbHelper.updateStatusBlocking(Long.toString(rs.getUserId()), false);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 				dbHelper.close();
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_unblock));
 				} else {
 					showToastMessage(getString(R.string.error_unblock,mErrorMessage));
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusGroupJoin extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 
 			try {
 				String group = s[0];
 				mStatusNet.doJoinGroup(group);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_join));
 				} else {
 					showToastMessage(getText(R.string.error_join)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusGroupLeave extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 
 			try {
 				String group = s[0];
 				mStatusNet.doLeaveGroup(group);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_leave));
 				} else {
 					showToastMessage(getText(R.string.error_leave)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusSubscribe extends AsyncTask<Long, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(Long... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			MustardDbAdapter dbHelper = getDbAdapter();
 			RowStatus rs = getRowStatus(s[0], dbHelper);
 			StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 			try {
 				if(sn.doSubscribe(rs.getScreenName())) {
 					dbHelper.updateStatusFollowing(Long.toString(rs.getUserId()), true);
 				} else {
 					mErrorMessage=getString(R.string.error_sub);
 					return 0;
 				}
 			} catch(MustardException e) {
 				mErrorMessage=e.getMessage();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 				dbHelper.close();
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_sub));
 				} else {
 					showToastMessage(getText(R.string.error_sub)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusUnsubscribe extends AsyncTask<Long, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(Long... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			MustardDbAdapter dbHelper = getDbAdapter();
 			RowStatus rs = getRowStatus(s[0], dbHelper);
 			StatusNet sn = getStatusNetFromRowStatus(rs, dbHelper);
 			try {
 				if(sn.doUnsubscribe(rs.getScreenName())) {
 					dbHelper.updateStatusFollowing(Long.toString(rs.getUserId()), false);
 				} else {
 					return 0;
 				}
 			} catch(MustardException e) {
 				mErrorMessage=e.getMessage();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 				dbHelper.close();
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_unsub));
 				} else {
 					showToastMessage(getText(R.string.error_unsub)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 
 	public class StatusesLoadMore extends AsyncTask<Void, Integer, Integer> {
 
 		private final String TAG = "StatusesLoadMore";
 		protected boolean mGetdents=false;
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			long maxId = mDbHelper.fetchMinStatusesId(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA);
 			Log.v(TAG,"Search " + (maxId-1));
 			if (maxId-1 < 1) {
 				return -1;
 			}
 			ArrayList<org.mustard.statusnet.Status> al = null;
 			try {
 				al=mStatusNet.get(DB_ROW_TYPE,DB_ROW_EXTRA,maxId-1,false);
 
 				if(al==null) {
 					return -1;
 				} else if (al.size()< 1) {
 					return -1;
 				} else {
 					Log.v(TAG,"Found X " + al.size());
 					mGetdents=mDbHelper.createStatuses(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA,al);
 
 				}
 			} catch(Exception e) {
 				mNoMoreDents=true;
 				//				if (MustardApplication.DEBUG) e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return -1;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 				mDbHelper.close();
 			}
 			return 1;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			setProgressBarIndeterminateVisibility(true);
 		}
 
 		protected void onPostExecute(Integer result) {
 			setProgressBarIndeterminateVisibility(false);
 
 			if (result<0) {
 				mNoMoreDents=true;
 			} else {
 				if(mGetdents) {
 					fillData();
 				} else {
 					mNoMoreDents=true;
 					showToastMessage(getText(R.string.error_fetch_more_dents)+"\n"+mErrorMessage);
 				}
 			}
 
 		}
 	}
 
 	public class MergedStatusesLoadMore extends StatusesLoadMore {
 
 		private final String TAG = "MergedStatusesLoadMore";
 
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 
 			MustardApplication _ma = (MustardApplication) getApplication();
 			StatusNet _sn = null;
 			boolean haveAtLeastOneStatus=false;
 			Cursor c = mDbHelper.fetchAllAccountsToMerge();
 			while(c.moveToNext()) {
 				long _aid = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 				if(mHMNoMoreDents==null) {
 					break;
 				}
 				if(mHMNoMoreDents.containsKey(_aid)) {
 					continue;
 				}
 
 				ArrayList<org.mustard.statusnet.Status> al = null;
 				try {
 					_sn = _ma.checkAccount(mDbHelper,false,_aid);
 					Log.i(TAG, "Fetching " + _sn.getMUsername() + "@" + _sn.getURL().getHost());
 					long maxId = mDbHelper.fetchMinStatusesId(_aid,DB_ROW_TYPE,DB_ROW_EXTRA);
 					Log.v(TAG,"Search " + (maxId-1));
 					if (maxId-1 < 1) {
 						return -1;
 					}
 					al=_sn.get(DB_ROW_TYPE,_sn.getMUsername(),maxId-1,false);
 
 					if(al==null || al.size()< 1) {
 						continue;
 					} else {
 						Log.v(TAG,"Found  " + al.size());
 						mGetdents=mDbHelper.createStatuses(_aid,DB_ROW_TYPE,DB_ROW_EXTRA,al);
 						haveAtLeastOneStatus=true;
 					}
 				} catch(Exception e) {
 					mHMNoMoreDents.put(_aid, true);
 					//				if (MustardApplication.DEBUG) e.printStackTrace();
 					mErrorMessage=e.toString();
 					if (MustardApplication.DEBUG)
 						e.printStackTrace();
 					Log.e(TAG,e.toString());
 					continue;
 				} finally {
 					if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 				}
 
 			}
 			c.close();
 
 			return haveAtLeastOneStatus ? 1 : 0;
 		}
 
 	}
 
 	protected TimelineHandler mHandler = new TimelineHandler();
 
 	class TimelineHandler extends Handler {
 
 		private static final int MSG_PROGRESS = 2;
 		private static final int MSG_GEOLOCATION_OK = 3;
 		private static final int MSG_GEOLOCATION_KO = 4;
 		private static final int MSG_REFRESH = 5;
 
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_PROGRESS:
 				setProgressBarIndeterminateVisibility(msg.arg1 != 0);
 				break;
 			case MSG_GEOLOCATION_OK:
 				GeoName gn = (GeoName)msg.obj;
 				doShowGeolocation(gn);
 				break;
 			case MSG_GEOLOCATION_KO:
 				showErrorMessage((String)msg.obj);
 				break;
 
 			case MSG_REFRESH:
 				doSilentRefresh();
 				break;
 			}
 		}
 
 		public void progress(boolean progress) {
 			Message msg = new Message();
 			msg.what = MSG_PROGRESS;
 			msg.arg1 = progress ? 1 : 0;
 			sendMessage(msg);
 		}
 
 		public void showGeolocation(GeoName geoname) {
 			Message msg = new Message();
 			msg.what = MSG_GEOLOCATION_OK;
 			msg.obj = geoname;
 			sendMessage(msg);			
 		}
 
 		public void errorGeolocation(String error) {
 			Message msg = new Message();
 			msg.what = MSG_GEOLOCATION_KO;
 			msg.obj = error;
 			sendMessage(msg);			
 		}
 
 	}
 
 	private void showErrorMessage(String reason) {
 		Toast.makeText(this, getString(R.string.error_generic_detail,reason), Toast.LENGTH_LONG).show();
 	}
 
 	private MessagingListener mListener = new MessagingListener() {
 
 		public void loadGeonameStarted(Context context) {
 			mHandler.progress(true);
 		}
 
 		public void loadGeonameFinished(Context context, GeoName geoname ) {
 			mHandler.progress(false);
 			mHandler.showGeolocation(geoname);
 		}
 
 		public void loadGeonameFailed(Context context, String reason) {
 			mHandler.progress(false);
 			mHandler.errorGeolocation(reason);
 		}
 	};
 
 	protected StatusNet getStatusNetFromRowid(long rowid) {
 		StatusNet sn = null;
 		MustardDbAdapter dbHelper = getDbAdapter();
 		sn = getStatusNetFromRowid(rowid, dbHelper);
 		dbHelper.close();
 		return sn;
 	}
 
 	protected StatusNet getStatusNetFromRowid(long rowid, MustardDbAdapter dbHelper) {
 		StatusNet sn = null;
 		RowStatus rs = getRowStatus(rowid);
 		long aid = rs.getAccountId();
 		if (aid > 0) {
 			sn = ((MustardApplication)getApplication()).checkAccount(dbHelper,false,aid);
 		}
 		return sn;
 	}
 	
 	protected StatusNet getStatusNetFromRowStatus(RowStatus rs, MustardDbAdapter dbHelper) {
 		StatusNet sn = null;
 		long aid = rs.getAccountId();
 		if (aid > 0) {
 			sn = ((MustardApplication)getApplication()).checkAccount(dbHelper,false,aid);
 		}
 		return sn;
 	}
 
 	protected RowStatus getRowStatus(long rowid) {
 		MustardDbAdapter dbHelper = getDbAdapter();
 		RowStatus rs = getRowStatus(rowid, dbHelper);
 		dbHelper.close();
 		return rs;
 	}
 
 	protected RowStatus getRowStatus(long rowid, MustardDbAdapter dbHelper) {
 		RowStatus rs = new RowStatus();
 		Cursor c = dbHelper.fetchStatus(rowid);
 		int mIdIdx = c.getColumnIndex(MustardDbAdapter.KEY_ROWID);
 		int mIdAccountIdx = c.getColumnIndex(MustardDbAdapter.KEY_ACCOUNT_ID);
 		int mGeolocationIdx = c.getColumnIndex(MustardDbAdapter.KEY_GEO);
 		int mScreenNameIdx = c.getColumnIndex(MustardDbAdapter.KEY_SCREEN_NAME);
 		int mIdUserIdx = c.getColumnIndex(MustardDbAdapter.KEY_USER_ID);
 		int mStatusIdx = c.getColumnIndex(MustardDbAdapter.KEY_STATUS);
 		int mStatusIdIdx = c.getColumnIndex(MustardDbAdapter.KEY_STATUS_ID);
 		int mDatetimeIdx = c.getColumnIndex(MustardDbAdapter.KEY_INSERT_AT);
 		int mSourceIdx = c.getColumnIndex(MustardDbAdapter.KEY_SOURCE);
 		int mInReplyToIdx = c.getColumnIndex(MustardDbAdapter.KEY_IN_REPLY_TO);
 		int mProfileImageIdx = c.getColumnIndex(MustardDbAdapter.KEY_USER_IMAGE);
 		int mLonIdx = c.getColumnIndex(MustardDbAdapter.KEY_LON);
 		int mLatIdx = c.getColumnIndex(MustardDbAdapter.KEY_LAT);
 		int mAttachmentIdx =  c.getColumnIndex(MustardDbAdapter.KEY_ATTACHMENT);
 		rs.setId(c.getLong(mIdIdx));
 		rs.setStatusId(c.getLong(mStatusIdIdx));
 		rs.setAccountId(c.getLong(mIdAccountIdx));
 		rs.setScreenName(c.getString(mScreenNameIdx));
 		rs.setUserId(c.getLong(mIdUserIdx));
 		rs.setSource(c.getString(mSourceIdx)) ;
 		rs.setInReplyTo(c.getLong(mInReplyToIdx));
 		rs.setProfileImage(c.getString(mProfileImageIdx));
 		rs.setDateTime(c.getLong(mDatetimeIdx));
 		rs.setGeolocation(c.getInt(mGeolocationIdx));
 		rs.setLon(c.getString(mLonIdx));
 		rs.setLat(c.getString(mLatIdx));
 		rs.setAttachment(c.getInt(mAttachmentIdx));
 		rs.setStatus(c.getString(mStatusIdx));
 		c.close();
 		return rs;
 	}
 }
