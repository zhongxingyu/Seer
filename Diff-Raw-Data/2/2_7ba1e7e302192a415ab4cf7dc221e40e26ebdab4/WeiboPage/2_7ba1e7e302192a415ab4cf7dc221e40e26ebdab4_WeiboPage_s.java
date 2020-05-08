 package com.zrd.zr.letuwb;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import weibo4android.Comment;
 import weibo4android.Status;
 import weibo4android.User;
 import weibo4android.WeiboException;
 import weibo4android.org.json.JSONArray;
 import weibo4android.org.json.JSONException;
 import weibo4android.org.json.JSONObject;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.weibo.sdk.android.api.FriendshipsAPI;
 import com.weibo.sdk.android.api.StatusesAPI;
 import com.weibo.sdk.android.api.UsersAPI;
 import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
 import com.weibo.sdk.android.net.RequestListener;
 import com.weibo.sdk.android.custom.Responds;
 import com.weibo.sdk.android.custom.Status2;
 import com.weibo.sdk.android.custom.User2;
 
 import com.zrd.zr.pnj.ThreadPNJDealer;
 import com.zrd.zr.protos.WeibousersProtos.UCMappings;
 import com.zrd.zr.weiboes.Sina;
 import com.zrd.zr.weiboes.ThreadSinaDealer;
 
 public class WeiboPage {
 	private EntranceActivity parent;
 	private int mReferer = -1;
 	
 	protected static final int COUNT_PERPAGE_TIMELINE = 20;
 	protected static final int COUNT_PERPAGE_COMMENTS = 20;
 	private TextView mTextScreenName;
 	private ImageView mImageVerified;
 	private TextView mTextCreatedAt;
 	private TextView mTextLocation;
 	private ImageButton mBtnDescription;
 	private TextView mTextCounts;
 	private ListView mListStatus;
 	private ProgressBar mProgressStatusLoading;
 	private TextView mTextPossess;
 	private TextView mTextFriendship;
 	private TextView mTextAtSomeone;
 	private ImageView mBtnTinyProfileImage;
 	
 	private AlertDialog mDlgRepost;
 	private EditText mEditRepost;
 	private AlertDialog mDlgComment;
 	private EditText mEditComment;
 	private AlertDialog mDlgUpdateStatus;
 	private EditText mEditUpdateStatus;
 	private Button mBtnMoreTimelines;
 	private Button mBtnMoreComments;
 	private Dialog mDlgComments;
 	private Dialog mDlgMore;
 	
 	private Long mUid = null;
 	private Long mId = null;
 	private User2 mLastUser = null;
 	private List<Sina.XStatus> mLastUserTimeline = new ArrayList<Sina.XStatus>();
 	private int mIndexOfSelectedStatus = -1;
 	private List<Comment> mLastComments = new ArrayList<Comment>();
 	
 	//private AlphaAnimation mAnimFadein = new AlphaAnimation(0.1f, 1.0f);
 	//private AlphaAnimation mAnimFadeout = new AlphaAnimation(1.0f, 0.1f);
 	
 	/*
 	 * Handler for showing all kinds of SINA_weibo data from background thread.
 	 */
 	Handler mHandler = new Handler() {
 		@SuppressWarnings("unchecked")
 		public void handleMessage(Message msg) {
 			Sina sina = (Sina)msg.getData().getSerializable(ThreadSinaDealer.KEY_SINA);
 			if (sina == null) {
 				sina = mSina;
 			} else {
 				setSina(sina);
 			}
 			weibo4android.WeiboException wexp = (weibo4android.WeiboException)msg.getData().getSerializable(ThreadSinaDealer.KEY_WEIBO_ERR);
 			User user;
 			Status status;
 			Comment comment;
 			
 			Responds resp;
 			Bundle bundle = msg.getData();
 			
 			switch (msg.what) {
 			case ThreadPNJDealer.GET_POSSESSIONS:
 				UCMappings mappings = (UCMappings)msg.getData().getSerializable(ThreadPNJDealer.KEY_DATA);
 				if (mappings != null) {
 					if (mappings.getFlag() == 1) {
 						Toast.makeText(
 							parent,
 							R.string.tips_alreadypossessed,
 							Toast.LENGTH_LONG
 						).show();
 					} else if (mappings.getFlag() == 2) {
 						Toast.makeText(
 							parent,
 							R.string.tips_possessed,
 							Toast.LENGTH_LONG
 						).show();
 					} else {
 						Toast.makeText(
 							parent,
 							R.string.tips_failedtopossess,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				} else {
 					//deal with failing to possess
 				}
 				break;
 			case ThreadSinaDealer.SHOW_USER:
 				//mLastUser = (User)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 			case Responds.USERS_SHOW:
 				resp = (Responds) bundle.getSerializable(Responds.KEY_DATA);
 				switch (resp.getRespType()) {
 				case Responds.TYPE_COMPLETE:
 					try {
 						mLastUser = new User2(new JSONObject(resp.getRespOnComplete()));
 					} catch (WeiboException e) {
 						// TODO Auto-generated catch block
 						mLastUser = null;
 						e.printStackTrace();
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						mLastUser = null;
 						e.printStackTrace();
 					} catch (NullPointerException e) {
 						mLastUser = null;
 						e.printStackTrace();
 					}
 					break;
 				case Responds.TYPE_ERROR:
 				case Responds.TYPE_IO_ERROR:
 					mLastUser = null;
 					break;
 				}
 				if (mLastUser != null) {			
 					/*
 					 * show the profile-image
 					 */
 					AsyncImageLoader ail = new AsyncImageLoader(
 						parent,
 						R.id.btnTinyProfileImage,
 						R.drawable.person
 					);
 					ail.execute(mLastUser.getProfileImageURL());
 					
 					/*
 					 * show the screen name
 					 */
 					mTextScreenName.setText(mLastUser.getScreenName());
 					
 					/*
 					 * show "v" if verified
 					 */
 					if (mLastUser.isVerified()) {
 						mImageVerified.setVisibility(ImageView.VISIBLE);
 					} else {
 						mImageVerified.setVisibility(ImageView.GONE);
 					}
 					
 					/*
 					 * show when was the user created
 					 */
 					Date dtCreatedAt = mLastUser.getCreatedAt();
 					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 					mTextCreatedAt.setText(sdf.format(dtCreatedAt));
 					
 					/*
 					 * show the location and the description
 					 */
 					mTextLocation.setText(
 						" [" + mLastUser.getLocation() + "]"
 					);
 					String description = mLastUser.getDescription();
 					if (!description.equals("")) {
 						mBtnDescription.setVisibility(ImageButton.VISIBLE);
 						mBtnDescription.setTag(description);
 					} else {
 						mBtnDescription.setVisibility(ImageButton.GONE);
 						mBtnDescription.setTag(null);
 					}
 					
 					/*
 					 * show all kinds of the counts
 					 */
 					String sCounts = parent.getString(R.string.label_weibos) + ":" + mLastUser.getStatusesCount()
 						+ " " 
 						+ parent.getString(R.string.label_favorites) + ":" + mLastUser.getFavouritesCount()
 						+ " "
 						+ parent.getString(R.string.label_followers) + ":" + mLastUser.getFollowersCount()
 						+ " " 
 						+ parent.getString(R.string.label_friends) + ":" + mLastUser.getFriendsCount(); 
 					mTextCounts.setText(sCounts);
 					parent.getBrowPage().getTextCounts_brow().setText(sCounts);
 					
 					/*
 					 * alter the label of "@" button according to the gender of the user
 					 */
 					if (mLastUser.getGender().equals("f")) {
 						mTextAtSomeone.setText(parent.getString(R.string.label_atsomeone) + parent.getString(R.string.label_her));
 						parent.getBrowPage().getBtnAtSomeone().setText(parent.getString(R.string.label_atsomeone) + parent.getString(R.string.label_her));
 					} else if (mLastUser.getGender().equals("m")){
 						mTextAtSomeone.setText(parent.getString(R.string.label_atsomeone) + parent.getString(R.string.label_him));
 						parent.getBrowPage().getBtnAtSomeone().setText(parent.getString(R.string.label_atsomeone) + parent.getString(R.string.label_him));
 					} else {
 						mTextAtSomeone.setText(parent.getString(R.string.label_atsomeone) + parent.getString(R.string.label_her) + "/" + parent.getString(R.string.label_him));
 						parent.getBrowPage().getBtnAtSomeone().setText(parent.getString(R.string.label_atsomeone) + parent.getString(R.string.label_her) + "/" + parent.getString(R.string.label_him));
 					}
 					
 				} else {
 					mTextAtSomeone.setText(R.string.label_atsomeone);
 					parent.getBrowPage().getBtnAtSomeone().setText(R.string.label_atsomeone);
 					/*
 					 * clear all kinds of the counts
 					 */
 					String sCounts = parent.getString(R.string.label_weibos) + ":-"
 						+ " " 
 						+ parent.getString(R.string.label_favorites) + ":-"
 						+ " "
 						+ parent.getString(R.string.label_followers) + ":-"
 						+ " " 
 						+ parent.getString(R.string.label_friends) + ":-"; 
 					mTextCounts.setText(sCounts);
 					parent.getBrowPage().getTextCounts_brow().setText(sCounts);
 					
 					if (wexp != null) {
 						Toast.makeText(
 							parent,
 							//wexp.toString(),
 							R.string.tips_getweiboinfofailed,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 				break;
 			case ThreadSinaDealer.GET_USER_TIMELINE:
 				//ArrayList<Sina.XStatus> xstatuses = (ArrayList<Sina.XStatus>)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 			case Responds.STATUSES_USERTIMELINE:
 				final ArrayList<Sina.XStatus> xstatuses = new ArrayList<Sina.XStatus>();
 				resp = (Responds) bundle.getSerializable(Responds.KEY_DATA);
 				switch (resp.getRespType()) {
 				case Responds.TYPE_COMPLETE:
 					JSONObject json;
 					try {
 						json = new JSONObject(resp.getRespOnComplete());
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						json = null;
 						Toast.makeText(parent, "~~4debug~~00", Toast.LENGTH_LONG).show();
 						break;
 					}
 					JSONArray statuses;
 					ArrayList<String> ids = new ArrayList<String>();
 					try {
 						statuses = json.getJSONArray("statuses");
 						for (int i = 0; i < statuses.length(); i++) {
 							Sina.XStatus xstatus = mSina.getXStatus();
 							xstatus.setStatus(new Status2(statuses.getJSONObject(i)));
 							xstatuses.add(xstatus);
 							ids.add(statuses.getJSONObject(i).getString("id"));
 						}
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						statuses = null;
 						Toast.makeText(parent, "~~4debug~~11", Toast.LENGTH_LONG).show();
 						break;
 					}
 					//List<Status> tlist = mSina.getWeibo().getUserTimeline(mParams[0], paging);
 					catch (WeiboException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						statuses = null;
 						Toast.makeText(parent, "~~4debug~~22", Toast.LENGTH_LONG).show();
 						break;
 					} catch (NullPointerException e) {
 						e.printStackTrace();
 						statuses = null;
 						Toast.makeText(parent, "~~4debug~~33", Toast.LENGTH_LONG).show();
 						break;
 					}
 					
 					if (ids.size() != 0) {
 						StatusesAPI api = new StatusesAPI(parent.getAccessToken());
 						String[] _ids = new String[ids.size()];
 						for (int i = 0; i < ids.size(); i++) {
 							_ids[i] = ids.get(i);
 						}
 						api.count(_ids,
 							new RequestListener() {
 
 								@Override
 								public void onComplete(String response) {
 									// TODO Auto-generated method stub
 									JSONArray counts;
 									try {
 										counts = new JSONArray(response);
 									} catch (JSONException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 										counts = null;
 									}
 									for (int i = 0; counts != null && i < counts.length(); i++) {
 										for (int j = 0; j < xstatuses.size(); j++) {
 											try {
 												if (xstatuses.get(j).getStatus().getId()
 													== counts.getJSONObject(i).getLong("id")) {
 													xstatuses.get(j).setComments(counts.getJSONObject(i).getInt("comments"));
 													xstatuses.get(j).setReposts(counts.getJSONObject(i).getInt("reposts"));
 												}
 											} catch (JSONException e) {
 												// TODO Auto-generated catch block
 												e.printStackTrace();
 											}
 										}
 									}
 								}
 
 								@Override
 								public void onIOException(IOException e) {
 									// TODO Auto-generated method stub
 									
 								}
 
 								@Override
 								public void onError(
 										com.weibo.sdk.android.WeiboException e) {
 									// TODO Auto-generated method stub
 									
 								}
 								
 						});
 					}
 					break;
 				case Responds.TYPE_ERROR:
 				case Responds.TYPE_IO_ERROR:
 					break;
 				}
 				for (int i = 0; i < xstatuses.size(); i++) {
 					mLastUserTimeline.add(xstatuses.get(i));
 				}
 				/*
 				 * show the user time_line
 				 */
 				WeiboStatusListAdapter adapter = new WeiboStatusListAdapter(
 					parent,
 					//getStatusData(ThreadSinaDealer.GET_USER_TIMELINE)
 					getStatusData(Responds.STATUSES_USERTIMELINE)
 				);
 				mListStatus.setAdapter(adapter);
 				mListStatus.setSelection(
 					(getLastUserTimelineTotalPage() - 1) * COUNT_PERPAGE_TIMELINE
 				);
 				turnDealing(false);
 				break;
 			/*
 			case ThreadSinaDealer.CREATE_FRIENDSHIP:
 				user = (User)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 				if (user != null) {
 					if (!user.equals(mSina.getLoggedInUser())) {
 						Toast.makeText(
 							parent,
 							R.string.tips_friendsmade,
 							Toast.LENGTH_LONG
 						).show();
 					} else {
 						Toast.makeText(
 							parent,
 							R.string.tips_friendsalready,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				} else {
 					//deal with failing to make friends
 					if (wexp != null) {
 						Toast.makeText(
 							parent,
 							//wexp.toString(),
 							R.string.tips_getweiboinfofailed,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 				turnDealing(false);
 				break;
 			*/
 			case Responds.FRIENDSHIPS_CREATE:
 				int fsc = msg.getData().getInt(Responds.KEY_DATA);
 				switch (fsc) {
 				case 0:
 					Toast.makeText(
 						parent,
 						R.string.tips_friendsalready,
 						Toast.LENGTH_LONG
 					).show();
 					break;
 				case 1:
 					Toast.makeText(
 						parent,
 						R.string.tips_friendsmade,
 						Toast.LENGTH_LONG
 					).show();
 					break;
 				case -1:
 					Toast.makeText(
 						parent,
 						R.string.tips_getweiboinfofailed,
 						Toast.LENGTH_LONG
 					).show();
 				default:
 					Toast.makeText(
 						parent,
 						R.string.tips_getweiboinfofailed,
 						Toast.LENGTH_SHORT
 					).show();
 					break;
 				}
 				turnDealing(false);
 				break;
 			case ThreadSinaDealer.CREATE_FAVORITE:
 				status = (Status)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 				if (status != null) {
 					Toast.makeText(
 						parent,
 						R.string.tips_favoritemade,
 						Toast.LENGTH_LONG
 					).show();
 				} else {
 					//deal with failing to make favorite
 					if (wexp != null) {
 						Toast.makeText(
 							parent,
 							//wexp.toString(),
 							R.string.tips_getweiboinfofailed,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 				turnDealing(false);
 				break;
 			case ThreadSinaDealer.REPOST:
 				status = (Status)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 				if (status != null) {
 					Toast.makeText(
 						parent,
 						R.string.tips_reposted,
 						Toast.LENGTH_LONG
 					).show();
 				} else {
 					//deal with failing to make favorite
 					if (wexp != null) {
 						Toast.makeText(
 							parent,
 							//wexp.toString(),
 							R.string.tips_getweiboinfofailed,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 				turnDealing(false);
 				break;
 			case ThreadSinaDealer.GET_COMMENTS:
 				ArrayList<Comment> comments = (ArrayList<Comment>)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 				if (comments != null) {
 					if (comments.size() != 0) {
 						for (int i = 0; i < comments.size(); i++) {
 							mLastComments.add(comments.get(i));
 						}
 						if (mLastComments.size() != 0) {
 							ListView lv = (ListView)mDlgComments.findViewById(R.id.lvCustomList);
 							lv.removeFooterView(mBtnMoreComments);
 							lv.addFooterView(mBtnMoreComments);
 							ArrayList<String> contents = new ArrayList<String>();
 							WeiboStatusListAdapter _tmp = 
 								new WeiboStatusListAdapter(parent, null);
 							for (int i = 0; i < mLastComments.size(); i++) {
 								user = mLastComments.get(i).getUser();
 								contents.add(
 									"[" + (i + 1) + "] "
 									+ (user != null ? user.getScreenName() : "")
 									+ "(" + _tmp.getSpecialDateText(mLastComments.get(i).getCreatedAt(), 0) + "):\n"
 									+ mLastComments.get(i).getText()
 								);
 							}
 							lv.setAdapter(
 								new ArrayAdapter<String>(
 									parent,
 									R.layout.item_custom_dialog_list,
 									contents
 								)
 							);
 							lv.setSelection(
 								(getLastCommentsTotalPage() - 1) * COUNT_PERPAGE_COMMENTS
 							);
 						} else {
 							Toast.makeText(
 								parent,
 								R.string.tips_nocomments,
 								Toast.LENGTH_LONG
 							).show();
 						}
 					} else {
 						Toast.makeText(
 							parent,
 							R.string.tips_nomorecomments,
 							Toast.LENGTH_LONG
 						).show();
 						mBtnMoreComments.setTag(2);
 						mBtnMoreComments.setEnabled(false);
 					}
 				} else {
 					//deal with failing to get comments
 					Integer flag = (Integer)mBtnMoreComments.getTag();
 					if (flag != null && flag != 0) {
 						//if mBtnMoreComments was clicked, then do something here
 						mDlgComments.dismiss();
 					} else {
 						//if mBtnMoreComments was not clicked, do something here
 						
 					}
 					
 					if (wexp != null) {
 						Toast.makeText(
 							parent,
 							//wexp.toString(),
 							R.string.tips_getweiboinfofailed,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 				turnDealing(false);
 				break;
 			case ThreadSinaDealer.UPDATE_COMMENT:
 				comment = (Comment)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 				if (comment != null) {
 					Toast.makeText(
 						parent,
 						R.string.tips_commented,
 						Toast.LENGTH_LONG
 					).show();
 				} else {
 					//deal with failing to make favorite
 					if (wexp != null) {
 						Toast.makeText(
 							parent,
 							//wexp.toString(),
 							R.string.tips_getweiboinfofailed,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 				turnDealing(false);
 				break;
 			case ThreadSinaDealer.UPDATE_STATUS:
 				status = (Status)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 				if (status != null) {
 					Toast.makeText(
 						parent,
 						R.string.tips_statusupdated,
 						Toast.LENGTH_LONG
 					).show();
 				} else {
 					//deal with failing to make favorite
 					if (wexp != null) {
 						Toast.makeText(
 							parent,
 							//wexp.toString(),
 							R.string.tips_getweiboinfofailed,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 				turnDealing(false);
 				break;
 			case Sina.REFRESH_USERBAR:
 				ImageView iv = (ImageView)parent.findViewById(R.id.ivTitleIcon);
 				TextView tv = (TextView)parent.findViewById(R.id.tvTitleName);
 				if (iv != null && tv != null && getSina().getLoggedInUser() != null) {
 					AsyncImageLoader loader = new AsyncImageLoader(parent,
 						iv, R.drawable.person);
 					loader.execute(getSina().getLoggedInUser().getProfileImageURL());
 					tv.setText(getSina().getLoggedInUser().getScreenName());
 				}
 				break;
 			}
 		}
 
 	};
 	private Sina mSina = new Sina(mHandler);
 
 	
 	WeiboPage(EntranceActivity activity) {
 		this.parent = activity;
 		
 		mTextScreenName = (TextView)parent.findViewById(R.id.tvScreenName);
 		mImageVerified = (ImageView)parent.findViewById(R.id.ivVerified);
 		mTextCreatedAt = (TextView)parent.findViewById(R.id.tvCreatedAt);
 		mTextLocation = (TextView)parent.findViewById(R.id.tvLocation);
 		mBtnDescription = (ImageButton)parent.findViewById(R.id.btnDescription);
 		mTextCounts = (TextView)parent.findViewById(R.id.tvCounts);
 		mListStatus = (ListView)parent.findViewById(R.id.lvStatus);
 		mProgressStatusLoading = (ProgressBar)parent.findViewById(R.id.pbStatusLoading);
 		mTextPossess = (TextView)parent.findViewById(R.id.tvPossess);
 		mTextFriendship = (TextView)parent.findViewById(R.id.tvFriendship);
 		mTextAtSomeone = (TextView)parent.findViewById(R.id.tvAtSomeone);
 		mEditRepost  = new EditText(parent);
 		mEditComment = new EditText(parent);
 		mBtnTinyProfileImage = (ImageButton)parent.findViewById(R.id.btnTinyProfileImage);
 		mEditUpdateStatus = new EditText(parent);
 		
 		mImageVerified.setVisibility(ImageView.GONE);
 		mBtnDescription.setVisibility(ImageButton.GONE);
 			
 		mBtnMoreTimelines = new Button(parent);
 		mBtnMoreTimelines.setText(R.string.label_getmore);
 		mBtnMoreTimelines.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				/*
 				new Thread(
 					new ThreadSinaDealer(
 						mSina,
 						ThreadSinaDealer.GET_USER_TIMELINE,
 						new String[] {
 							getUid().toString(), 
 							"" + (getLastUserTimelineTotalPage() + 1), 
 							"" + COUNT_PERPAGE_TIMELINE
 						},
 						mHandler
 					)
 				).start();
 				*/
 				StatusesAPI api = new StatusesAPI(parent.getAccessToken());
 				api.userTimeline(getUid(), 0, 0, 
 					COUNT_PERPAGE_TIMELINE/*how many lines*/, 
 					(getLastUserTimelineTotalPage() + 1), 
 					false, FEATURE.ALL, false, 
 					new RequestListener() {
 
 						@Override
 						public void onComplete(String response) {
 							// TODO Auto-generated method stub
 							fireToUI(Responds.STATUSES_USERTIMELINE, response);
 						}
 
 						@Override
 						public void onIOException(IOException e) {
 							// TODO Auto-generated method stub
 							fireToUI(Responds.STATUSES_USERTIMELINE, e);
 						}
 
 						@Override
 						public void onError(
 								com.weibo.sdk.android.WeiboException e) {
 							// TODO Auto-generated method stub
 							fireToUI(Responds.STATUSES_USERTIMELINE, e);
 						}
 					
 				});
 				turnDealing(true);
 			}
 			
 		});
 		mListStatus.addFooterView(mBtnMoreTimelines);
 		
 		mBtnMoreComments = new Button(parent);
 		mBtnMoreComments.setText(R.string.label_getmore);
 		/*
 		 * 0 means not clicked
 		 * 1 means clicked
 		 * 2 means should not be clicked till next comments showing up
 		 */
 		mBtnMoreComments.setTag(0);
 		mBtnMoreComments.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				long sid;
 				if (mLastUserTimeline == null) {
 					sid = mLastUser.getStatus().getId();
 				} else {
 					sid = mLastUserTimeline.get(mIndexOfSelectedStatus).getStatus().getId();
 				}
 				new Thread(
 					new ThreadSinaDealer(
 						mSina,
 						ThreadSinaDealer.GET_COMMENTS,
 						new String[] {
 							"" + sid,
 							"" + (getLastCommentsTotalPage() + 1),
 							"" + COUNT_PERPAGE_COMMENTS
 						},
 						mHandler
 					)
 				).start();
 				turnDealing(true);
 				mBtnMoreComments.setTag(1);
 			}
 			
 		});
 		
 		mBtnTinyProfileImage.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				/*
 				Intent intent = new Intent();
 				intent.setClass(WeiboShowActivity.this, PicbrowActivity.class);
 				intent.putExtra("id", mId);
 				startActivity(intent);
 				*/
 				parent.getBrowPage().setReferer(R.layout.main);
 				//parent.switchPage(R.layout.brow, mId);
 				parent.switchPage(R.layout.brow);
 			}
 			
 		});
 		
 		mDlgRepost = new AlertDialog.Builder(parent)
 			.setTitle(R.string.title_repost)
 			.setIcon(android.R.drawable.ic_dialog_info)
 			.setView(mEditRepost)
 			.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
 	
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					// TODO Auto-generated method stub
 					/*
 					 * This is the place it handles reposting
 					 */
 					long sid;
 					if (mLastUserTimeline == null) {
 						sid = mLastUser.getStatus().getId();
 					} else {
 						sid = mLastUserTimeline.get(mIndexOfSelectedStatus).getStatus().getId();
 					}
 					new Thread(
 						new ThreadSinaDealer(
 							mSina,
 							ThreadSinaDealer.REPOST,
 							new String[] {"" + sid, mEditRepost.getText().toString()},
 							mHandler
 						)
 					).start();
 					turnDealing(true);
 				}
 				
 			})
 			.setNegativeButton(R.string.label_cancel, null)
 			.create();
 		
 		mDlgComment = new AlertDialog.Builder(parent)
 			.setTitle(R.string.title_comment)
 			.setIcon(android.R.drawable.ic_dialog_info)
 			.setView(mEditComment)
 			.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
 		
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					// TODO Auto-generated method stub
 					/*
 					 * This is the place it handles comment
 					 */
 					long sid;
 					if (mLastUserTimeline == null) {
 						sid = mLastUser.getStatus().getId();
 					} else {
 						sid = mLastUserTimeline.get(mIndexOfSelectedStatus).getStatus().getId();
 					}
 					new Thread(
 						new ThreadSinaDealer(
 							mSina,
 							ThreadSinaDealer.UPDATE_COMMENT,
 							new String[] {mEditComment.getText().toString(), "" + sid, null},
 							mHandler
 						)
 					).start();
 					turnDealing(true);
 				}
 				
 			})
 			.setNegativeButton(R.string.label_cancel, null)
 			.create();
 		
 		mDlgUpdateStatus = new AlertDialog.Builder(parent)
 			.setTitle(R.string.title_updatestatus)
 			.setIcon(android.R.drawable.ic_dialog_info)
 			.setView(mEditUpdateStatus)
 			.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
 		
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					// TODO Auto-generated method stub
 					/*
 					 * This is the place it handles comment
 					 */
 					Toast.makeText(
 						parent,
 						R.string.tips_statusupdating,
 						Toast.LENGTH_LONG
 					).show();
 					new Thread(
 						new ThreadSinaDealer(
 							mSina,
 							ThreadSinaDealer.UPDATE_STATUS,
 							new String[] {mEditUpdateStatus.getText().toString()},
 							mHandler
 						)
 					).start();
 					turnDealing(true);
 				}
 				
 			})
 			.setNegativeButton(R.string.label_cancel, null)
 			.create();
 		
 		mDlgComments = new Dialog(parent, R.style.Dialog_Clean);
 		mDlgComments.setContentView(R.layout.custom_dialog_list);
 		ListView lvComments = (ListView)mDlgComments.findViewById(R.id.lvCustomList);
 		lvComments.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				mDlgComments.dismiss();
 			}
 			
 		});
 		
 		/*
 		 * deal actions for components
 		 */
 		mListStatus.setTag((long)0);
 		mListStatus.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				
 				/*
 				 * make the selected item different with others
 				 */
 				int position = arg2;
 				/*
 				HeaderViewListAdapter ha = (HeaderViewListAdapter)parent.getAdapter();
 				WeiboStatusListAdapter adapter  = (WeiboStatusListAdapter)ha.getWrappedAdapter();
 				adapter.setSelectedItem(position);
 				adapter.notifyDataSetInvalidated();
 				*/
 				mIndexOfSelectedStatus = position;
 				
 				/*
 				 * pop up the "more" list
 				 */
 				mDlgMore.show();
 				
 				long lastClickTime;
 				/*
 				 * check if it's double click
 				 */
 				lastClickTime = (Long)mListStatus.getTag();
 				if (Math.abs(lastClickTime-System.currentTimeMillis()) < 2000) {
 					mListStatus.setTag((long)0);
 					//to do some double click stuff here
 					showComments();
 					turnDealing(true);
 				} else {
 					mListStatus.setTag(System.currentTimeMillis());
 				}
 			}
 			
 		});
 		
 		mListStatus.setOnScrollListener(new OnScrollListener() {
 
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				// TODO Auto-generated method stub
 				//when it's not scrolling
 				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
 					//if it's already to the bottom
 					if(view.getLastVisiblePosition()==(view.getCount()-1)){
 						Toast.makeText(
 							parent,
 							R.string.tips_alreadylastone,
 							Toast.LENGTH_LONG
 						).show();
 					}
 				}
 			}
 			
 		});
 
 		mBtnDescription.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				parent.popupDescription((String) mBtnDescription.getTag());
 			}
 			
 		});
 				
 		mTextPossess.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				new Thread(
 					new ThreadPNJDealer(
 						ThreadPNJDealer.GET_POSSESSIONS,
 						EntranceActivity.URL_SITE 
 							+ "updpzs.php?"
 							+ "clientkey=" + EntranceActivity.getClientKey()
 							+ "&channelid=0"
 							+ "&uid=" + getUid(),
 						mHandler
 					)
 				).start();
 			}
 			
 		});
 		
 		mTextFriendship.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (mSina != null && mSina.isLoggedIn()) {
 					/*
 					new Thread(
 						new ThreadSinaDealer(
 							mSina,
 							ThreadSinaDealer.CREATE_FRIENDSHIP,
 							new String[] {getUid().toString()},
 							mHandler
 						)
 					).start();
 					*/
 					turnDealing(true);
 					
 					FriendshipsAPI api = new FriendshipsAPI(parent.getAccessToken());
 					//step 1, judge if already friends
 					//step 2, if not yet, create it
 					User2 usr = getSina().getLoggedInUser();
 					api.show(usr.getId(), getUid(), new RequestListener() {
 
 						@Override
 						public void onComplete(String response) {
 							// TODO Auto-generated method stub
 							FriendshipsAPI api = new FriendshipsAPI(parent.getAccessToken());
 							try {
 								JSONObject json = new JSONObject(response);
 								JSONObject target = new JSONObject(json.getString("target"));
 								Message msg = new Message();
 								msg.what = Responds.FRIENDSHIPS_CREATE;
 								Bundle bundle = new Bundle();
 								if (target.getBoolean("followed_by")) {
 									/**
 									 * 0 means "already friends"
 									 * 1 means "friendships created"
 									 * -1 means "friendships failed to be created"
 									 * <-1 means other errors
 									 */
 									bundle.putInt(Responds.KEY_DATA , 0);
 									msg.setData(bundle);
 									mHandler.sendMessage(msg);
 								} else {
 									api.create(getUid(), null, new RequestListener() {
 
 										@Override
 										public void onComplete(String response) {
 											// TODO Auto-generated method stub
 											Message msg = new Message();
 											msg.what = Responds.FRIENDSHIPS_CREATE;
 											Bundle bundle = new Bundle();
 											try {
 												JSONObject json = new JSONObject(response);
 												if (json.has("id")) {
 													bundle.putInt(Responds.KEY_DATA, 1);
 												} else {
 													bundle.putInt(Responds.KEY_DATA, -1);
 												}
 											} catch (JSONException e) {
 												// TODO Auto-generated catch block
 												e.printStackTrace();
 												bundle.putInt(Responds.KEY_DATA, -2);
 											}
 											msg.setData(bundle);
 											mHandler.sendMessage(msg);
 										}
 
 										@Override
 										public void onIOException(IOException e) {
 											// TODO Auto-generated method stub
 											e.printStackTrace();
 											Message msg = new Message();
 											msg.what = Responds.FRIENDSHIPS_CREATE;
 											Bundle bundle = new Bundle();
 											bundle.putInt(Responds.KEY_DATA, -3);
 											msg.setData(bundle);
 											mHandler.sendMessage(msg);
 										}
 
 										@Override
 										public void onError(
 												com.weibo.sdk.android.WeiboException e) {
 											// TODO Auto-generated method stub
 											e.printStackTrace();
 											Message msg = new Message();
 											msg.what = Responds.FRIENDSHIPS_CREATE;
 											Bundle bundle = new Bundle();
 											bundle.putInt(Responds.KEY_DATA, -4);
 											msg.setData(bundle);
 											mHandler.sendMessage(msg);
 										}
 										
 									});
 								}
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 								Message msg = new Message();
 								msg.what = Responds.FRIENDSHIPS_CREATE;
 								Bundle bundle = new Bundle();
 								bundle.putInt(Responds.KEY_DATA, -5);
 								msg.setData(bundle);
 								mHandler.sendMessage(msg);
 							}
 						}
 
 						@Override
 						public void onIOException(IOException e) {
 							// TODO Auto-generated method stub
 							e.printStackTrace();
 							Message msg = new Message();
 							msg.what = Responds.FRIENDSHIPS_CREATE;
 							Bundle bundle = new Bundle();
 							bundle.putInt(Responds.KEY_DATA, -6);
 							msg.setData(bundle);
 							mHandler.sendMessage(msg);
 						}
 
 						@Override
 						public void onError(
 								com.weibo.sdk.android.WeiboException e) {
 							// TODO Auto-generated method stub
 							e.printStackTrace();
 							Message msg = new Message();
 							msg.what = Responds.FRIENDSHIPS_CREATE;
 							Bundle bundle = new Bundle();
 							bundle.putInt(Responds.KEY_DATA, -7);
 							msg.setData(bundle);
 							mHandler.sendMessage(msg);
 						}
 						
 					});
 					
 				} else {
 					RegLoginActivity.shallWeLogin(R.string.title_loginfirst, parent);
 				}
 			}
 			
 		});
 		
 		mTextAtSomeone.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (mLastUser == null) {
 					Toast.makeText(
 						parent,
 						R.string.tips_waitforgettinguser,
 						Toast.LENGTH_LONG
 					).show();
 				} else {
 					mEditUpdateStatus.setText(
 						"@" + mLastUser.getScreenName() + ":"
						+ parent.getString(R.string.tips_routinehello)
 						+ " \"" + parent.getString(R.string.app_name)
 						+ "\":" + EntranceActivity.URL_UPDATE + "letuwb.apk"
 					);
 					mDlgUpdateStatus.show();
 				}
 			}
 			
 		});
 		
 		mDlgMore = new Dialog(parent, R.style.Dialog_Clean);
 		mDlgMore.setContentView(R.layout.custom_dialog_list);
 		ListView lvMore = (ListView)mDlgMore.findViewById(R.id.lvCustomList);
 		ArrayList<String> mlist = new ArrayList<String>();
 		mlist.add(parent.getString(R.string.label_weibo_favorite));
 		mlist.add(parent.getString(R.string.label_comment));
 		mlist.add(parent.getString(R.string.label_weibo_repost));
 		mlist.add(parent.getString(R.string.label_comments));
 		mlist.add(parent.getString(R.string.label_seebiggerimage0));
 		mlist.add(parent.getString(R.string.label_seebiggerimage1));
 		mlist.add(parent.getString(R.string.label_reload));
 		lvMore.setAdapter(
 			new ArrayAdapter<String> (
 				parent,
 				R.layout.item_custom_dialog_list,
 				mlist
 			)
 		);
 		lvMore.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> av, View view, 
 					int position, long id) {
 				// TODO Auto-generated method stub
 				String originalPic;
 				switch (position) {
 				case 0:
 					if (mSina != null && mSina.isLoggedIn()) {
 						if (mIndexOfSelectedStatus != -1) {
 							/*
 							 * make it favorite
 							 */
 							if (mLastUserTimeline == null) {
 								new Thread(
 									new ThreadSinaDealer(
 										mSina,
 										ThreadSinaDealer.CREATE_FAVORITE,
 										new String[] {"" + mLastUser.getStatus().getId()},
 										mHandler
 									)
 								).start();
 							} else {
 								new Thread(
 									new ThreadSinaDealer(
 										mSina,
 										ThreadSinaDealer.CREATE_FAVORITE,
 										new String[] {"" + mLastUserTimeline.get(mIndexOfSelectedStatus).getStatus().getId()},
 										mHandler
 									)
 								).start();
 							}
 							turnDealing(true);
 						} else {
 							Toast.makeText(
 								parent,
 								R.string.tips_noitemselected,
 								Toast.LENGTH_LONG
 							).show();
 						}
 					} else {
 						RegLoginActivity.shallWeLogin(R.string.title_loginfirst, parent);
 					}
 					break;
 				case 1:
 					if (mSina != null && mSina.isLoggedIn()) {
 						if (mIndexOfSelectedStatus == -1) {
 							Toast.makeText(
 								parent,
 								R.string.tips_noitemselected,
 								Toast.LENGTH_LONG
 							).show();
 							return;
 						}
 						
 						mDlgComment.show();
 					} else {
 						RegLoginActivity.shallWeLogin(R.string.title_loginfirst, parent);
 					}
 					break;
 				case 2:
 					if (mSina != null && mSina.isLoggedIn()) {
 						if (mIndexOfSelectedStatus == -1) {
 							Toast.makeText(
 								parent,
 								R.string.tips_noitemselected,
 								Toast.LENGTH_LONG
 							).show();
 							return;
 						}
 						
 						mDlgRepost.show();
 					} else {
 						RegLoginActivity.shallWeLogin(R.string.title_loginfirst, parent);
 					}
 					break;
 				case 3:
 					if (mIndexOfSelectedStatus == -1) {
 						Toast.makeText(
 							parent,
 							R.string.tips_noitemselected,
 							Toast.LENGTH_LONG
 						).show();
 					} else {
 						showComments();
 						turnDealing(true);
 					}
 					break;
 				case 4:
 					if (mIndexOfSelectedStatus == -1) {
 						Toast.makeText(
 							parent,
 							R.string.tips_noitemselected,
 							Toast.LENGTH_LONG
 						).show();
 					} else {
 						originalPic = mLastUserTimeline
 							.get(mIndexOfSelectedStatus)
 							.getStatus()
 							.getOriginal_pic();
 						if (originalPic != null && !originalPic.trim().equals("")) {
 							showOriginalImage(originalPic);
 						} else {
 							Toast.makeText(
 								parent,
 								R.string.tips_nooriginalimage0,
 								Toast.LENGTH_LONG
 							).show();
 						}
 					}
 					break;
 				case 5:
 					if (mIndexOfSelectedStatus == -1) {
 						Toast.makeText(
 							parent,
 							R.string.tips_noitemselected,
 							Toast.LENGTH_LONG
 						).show();
 					} else {
 						Status2 retweeted = mLastUserTimeline
 							.get(mIndexOfSelectedStatus)
 							.getStatus()
 							.getRetweeted_status();
 						if (retweeted != null) {
 							originalPic = retweeted.getOriginal_pic();
 						} else {
 							originalPic = null;
 						}
 						if (originalPic != null && !originalPic.trim().equals("")) {
 							showOriginalImage(originalPic);
 						} else {
 							Toast.makeText(
 								parent,
 								R.string.tips_nooriginalimage1,
 								Toast.LENGTH_LONG
 							).show();
 						}
 					}
 					break;
 				case 6:
 					reloadAll();
 					turnDealing(true);
 					break;
 				}
 				mDlgMore.dismiss();
 			}
 			
 		});
 
 	}
 	
 	protected void showComments() {
 		// TODO Auto-generated method stub
 		long sid;
 		if (mLastUserTimeline == null) {
 			sid = mLastUser.getStatus().getId();
 		} else {
 			sid = mLastUserTimeline.get(mIndexOfSelectedStatus).getStatus().getId();
 		}
 		mLastComments.clear();
 		new Thread(
 			new ThreadSinaDealer(
 				mSina,
 				ThreadSinaDealer.GET_COMMENTS,
 				new String[] {
 					"" + sid,
 					"" + getLastCommentsTotalPage(),
 					"" + COUNT_PERPAGE_COMMENTS
 				},
 				mHandler
 			)
 		).start();
 		ListView lvComments = (ListView)mDlgComments.findViewById(R.id.lvCustomList);
 		ArrayList<String> lstWaiting = new ArrayList<String>();
 		lstWaiting.add(parent.getString(R.string.tips_waitasecond));
 		lvComments.removeFooterView(mBtnMoreComments);
 		mBtnMoreComments.setTag(0);
 		mBtnMoreComments.setEnabled(true);
 		lvComments.setAdapter(
 			new ArrayAdapter<String>(
 				parent,
 				R.layout.item_custom_dialog_list,
 				lstWaiting
 			)
 		);
 		mDlgComments.show();
 	}
 
 	public void reloadLastUser(Long uid) {
 		mLastUser = null;
 		mIndexOfSelectedStatus = -1;
 		/*
 		new Thread(
 			new ThreadSinaDealer(
 				mSina, 
 				ThreadSinaDealer.SHOW_USER, 
 				new String[] {uid.toString()}, 
 				mHandler
 			)
 		).start();
 		*/
 		
 		UsersAPI api = new UsersAPI(parent.getAccessToken());
 		api.show(uid, 
 			new RequestListener() {
 
 				@Override
 				public void onComplete(String response) {
 					// TODO Auto-generated method stub
 					fireToUI(Responds.USERS_SHOW, response);
 				}
 
 				@Override
 				public void onIOException(IOException e) {
 					// TODO Auto-generated method stub
 					fireToUI(Responds.USERS_SHOW, e);
 				}
 
 				@Override
 				public void onError(com.weibo.sdk.android.WeiboException e) {
 					// TODO Auto-generated method stub
 					fireToUI(Responds.USERS_SHOW, e);
 				}
 	        	
     	});
 	}
 	
 	protected void reloadAll() {
 		// TODO Auto-generated method stub
 		if (mLastUser == null) {
 			reloadLastUser(getUid());
 		}
 		
 		mLastUserTimeline.clear();
 		/*
 		new Thread(
 			new ThreadSinaDealer(
 				mSina,
 				ThreadSinaDealer.GET_USER_TIMELINE,
 				new String[] {getUid().toString(), "" + getLastUserTimelineTotalPage(), "" + COUNT_PERPAGE_TIMELINE},
 				mHandler
 			)
 		).start();
 		*/
 		StatusesAPI api = new StatusesAPI(parent.getAccessToken());
 		api.userTimeline(getUid(), 0, 0, 
 			COUNT_PERPAGE_TIMELINE/*how many lines*/, 
 			(getLastUserTimelineTotalPage() + 1), 
 			false, FEATURE.ALL, false, 
 			new RequestListener() {
 
 				@Override
 				public void onComplete(String response) {
 					// TODO Auto-generated method stub
 					fireToUI(Responds.STATUSES_USERTIMELINE, response);
 				}
 
 				@Override
 				public void onIOException(IOException e) {
 					// TODO Auto-generated method stub
 					fireToUI(Responds.STATUSES_USERTIMELINE, e);
 				}
 
 				@Override
 				public void onError(
 						com.weibo.sdk.android.WeiboException e) {
 					// TODO Auto-generated method stub
 					fireToUI(Responds.STATUSES_USERTIMELINE, e);
 				}
 			
 		});
 	}
 
 	protected int getLastUserTimelineTotalPage() {
 		// TODO Auto-generated method stub
 		if (mLastUserTimeline == null) return 1;
 		int size = mLastUserTimeline.size();
 		if (size == 0) return 1;
 		if (size % COUNT_PERPAGE_TIMELINE != 0) return size / COUNT_PERPAGE_TIMELINE + 1;
 		else return size / COUNT_PERPAGE_TIMELINE;
 		
 	}
 	
 	protected int getLastCommentsTotalPage() {
 		if (mLastComments == null) return 1;
 		int size = mLastComments.size();
 		if (size == 0) return 1;
 		if (size % COUNT_PERPAGE_COMMENTS != 0) return size / COUNT_PERPAGE_COMMENTS + 1;
 		else return size / COUNT_PERPAGE_COMMENTS;
 	}
 
 	public Sina getSina() {
 		return mSina;
 	}
 	
 	public void setSina(Sina sina) {
 		mSina = sina;
 	}
 	
 	public int getPrivilege() {
 		return mSina.getLoggedInUser() == null ? 1 : 0;
 	}
 	
 	private List<Map<String, Object>> getStatusData(int type) {
 		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 		Map<String, Object> map;
 		Sina.XStatus xstatus;
 		switch (type) {
 		case ThreadSinaDealer.SHOW_USER:
 			if (mLastUser != null) {
 				xstatus = mSina.getXStatus();
 				xstatus.setStatus(mLastUser.getStatus());
 				if (xstatus != null) {
 					map = new HashMap<String, Object>();
 					map.put("xstatus", xstatus);
 					list.add(map);
 				}	
 			}
 			break;
 		case ThreadSinaDealer.GET_USER_TIMELINE:
 		case Responds.STATUSES_USERTIMELINE:
 			if (mLastUserTimeline != null) {
 				for (int i = 0; i < mLastUserTimeline.size(); i++) {
 					xstatus = mLastUserTimeline.get(i);
 					map = new HashMap<String, Object>();
 					map.put("xstatus", xstatus);
 					list.add(map);
 				}
 			}
 			break;
 		}
 		return list;
 	}
 	
 	/*
 	 * change the status for the components when dealing with SINA_weibo data
 	 */
 	public void turnDealing(boolean on) {
 		if (on == true) {
 			mTextPossess.setEnabled(false);
 			mBtnMoreTimelines.setEnabled(false);
 			mBtnMoreComments.setEnabled(false);
 			mProgressStatusLoading.setVisibility(ProgressBar.VISIBLE);
 		} else {
 			mTextPossess.setEnabled(true);
 			mBtnMoreTimelines.setEnabled(true);
 			Integer flag = (Integer)mBtnMoreComments.getTag();
 			if (flag != null && flag == 2) {
 				
 			} else {
 				mBtnMoreComments.setEnabled(true);
 			}
 			mProgressStatusLoading.setVisibility(ProgressBar.GONE);
 		}
 	}
 	
 	/*
 	 * try to show pictures from weibo in a dialog
 	 * with given uri
 	 */
 	protected void showOriginalImage(String sUrl) {
 		Intent intent = new Intent();
 		intent.putExtra("url", sUrl);
 		intent.setClass(parent, ImageActivity.class);
 		parent.startActivity(intent);
 	}
 	
 	private void fireToUI(int act, String arg) {
     	if (mHandler == null) return;
     	Message msg = new Message();
 		msg.what = act;
 		Responds resp = new Responds(msg.what);
 		resp.setRespOnComplete(arg);
 		Bundle bundle = new Bundle();
 		bundle.putSerializable(Responds.KEY_DATA, resp);
 		msg.setData(bundle);
 		mHandler.sendMessage(msg);
     }
     
     private void fireToUI(int act, com.weibo.sdk.android.WeiboException arg) {
     	if (mHandler == null) return;
     	Message msg = new Message();
 		msg.what = act;
 		Responds resp = new Responds(msg.what);
 		resp.setRespOnError(arg);
 		Bundle bundle = new Bundle();
 		bundle.putSerializable(Responds.KEY_DATA, resp);
 		msg.setData(bundle);
 		mHandler.sendMessage(msg);
     }
     
     private void fireToUI(int act, IOException arg) {
     	if (mHandler == null) return;
     	Message msg = new Message();
 		msg.what = act;
 		Responds resp = new Responds(msg.what);
 		resp.setRespOnIOError(arg);
 		Bundle bundle = new Bundle();
 		bundle.putSerializable(Responds.KEY_DATA, resp);
 		msg.setData(bundle);
 		mHandler.sendMessage(msg);
     }
 
 	public Long getUid() {
 		return mUid;
 	}
 
 	public void setUid(Long mUid) {
 		this.mUid = mUid;
 	}
 
 	public int getReferer() {
 		return mReferer;
 	}
 
 	public void setReferer(int mReferer) {
 		this.mReferer = mReferer;
 	}
 
 	public Long getId() {
 		return mId;
 	}
 
 	public void setId(Long mId) {
 		this.mId = mId;
 	}
 	
 	public User2 getLastUser() {
 		return this.mLastUser;
 	}
 	
 	public TextView getTextAtSomeone() {
 		return this.mTextAtSomeone;
 	}
 	
 	public void friendshipClick() {
 		mTextFriendship.performClick();
 	}
 }
