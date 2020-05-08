 package whyq.activity;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import whyq.WhyqApplication;
 import whyq.adapter.AmazingAdapter;
 import whyq.interfaces.FriendFacebookController;
 import whyq.model.FriendFacebook;
 import whyq.model.ResponseData;
 import whyq.model.SearchFriendCriteria;
 import whyq.service.DataParser;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.Constants;
 import whyq.utils.ImageViewHelper;
 import whyq.utils.SpannableUtils;
 import whyq.utils.Util;
 import whyq.utils.WhyqUtils;
 import whyq.utils.facebook.sdk.DialogError;
 import whyq.utils.facebook.sdk.Facebook;
 import whyq.utils.facebook.sdk.Facebook.DialogListener;
 import whyq.utils.facebook.sdk.FacebookError;
 import whyq.view.AmazingListView;
 import whyq.view.SearchField;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.whyq.BuildConfig;
 import com.whyq.R;
 
 public class WhyqFriendsFacebookActivity extends ImageWorkerActivity {
 
 	private FriendsFacebookAdapter mFriendFacebookAdapter = null;
 	private String mAccessToken;
 	private AmazingListView mListview;
 	private TextView mInviteMessage;
 	private Button mInviteButton;
 	private View mInviteContainer;
 	private boolean isFacebook = true;
 	private static final Map<String, String> INVITED_LIST = new HashMap<String, String>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_facebook_friends);
 
 		isFacebook = getIntent().getBooleanExtra("is_facebook", true);
 		showHeaderSearchField(true);
 		SearchField searchField = getSearchField();
 		EditText tvSearch = searchField.getEditTextView();
 
 		tvSearch.setHint(R.string.find_a_friend);
 		tvSearch.setTextColor(getResources().getColor(R.color.white));
 		tvSearch.setBackgroundResource(R.drawable.textfield_search_default_holo_dark);
 
 		mListview = (AmazingListView) findViewById(R.id.listview);
 		mListview.setPinnedHeaderView(findViewById(R.id.header));
 		mListview.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				final Object item = arg0.getItemAtPosition(arg2);
 				FriendFacebook facebook = (FriendFacebook) item;
 				if (facebook.getIs_join()) {
 					startUserProfileActivity(facebook.getId(),
 							facebook.getFirstName(), facebook.getAvatar());
 				}
 			}
 		});
 
 		mFriendFacebookAdapter = new FriendsFacebookAdapter(this, mImageWorker);
 		mAccessToken = getAccessToken();
 		if (BuildConfig.DEBUG) {
 			Log.d("WhyqFriendsFacebookActivity", "access token: "
 					+ mAccessToken);
 		}
 
 		setTitle(R.string.friend_from_facebook);
 		getFriends();
 
 		mInviteContainer = findViewById(R.id.inviteContainer);
 		mInviteMessage = (TextView) findViewById(R.id.tvMessage);
 		mInviteButton = (Button) findViewById(R.id.invite);
 		mInviteButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (INVITED_LIST.size() == 0) {
 					return;
 				}
 				inviteFriends();
 			}
 		});
 
 	}
 
 	private void startUserProfileActivity(String userId, String userName,
 			String avatar) {
 		Intent i = new Intent(this, WhyqUserProfileActivity.class);
 		i.putExtra(WhyqUserProfileActivity.ARG_USER_ID, userId);
 		i.putExtra("is_friend", true);
 		startActivity(i);
 	}
 
 	private String getAccessToken() {
 		final WhyqUtils mPermutils = new WhyqUtils();
 		return mPermutils.getFacebookToken(this);
 	}
 
 	@Override
 	public void onQuery(String queryString) {
 		if (queryString != null && queryString.length() > 0) {
 			searchFriends(queryString);
 		} else {
 			getFriends();
 		}
 	}
 
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		super.onCompleted(service, result);
 		setLoading(false);
 		if (result != null) {
 			if (result.getAction() == ServiceAction.ActionInviteFriendsFacebook) {
 				mInviteContainer.setVisibility(View.GONE);
 				INVITED_LIST.clear();
 				getFriends();
 			} else {
 				if (result.getAction() == ServiceAction.ActionGetFriendsFacebook
 						|| result.getAction() == ServiceAction.ActionSearchFriendsFacebook) {
 					DataParser parser = new DataParser();
 					ResponseData data = (ResponseData) parser
 							.parseFriendFacebook(String.valueOf(result
 									.getData()));
 					if (data.getStatus().equals("401")) {
 						Util.loginAgain(this, data.getMessage());
 					} else {
 						mFriendFacebookAdapter.setController((FriendFacebookController) data.getData());
 					}
 				}
 			}
 		}
 	}
 
 	private void searchFriends(String queryString) {
 		if (mFriendFacebookAdapter == null) {
 			mFriendFacebookAdapter = new FriendsFacebookAdapter(this,
 					mImageWorker);
 		}
 		mListview.setAdapter(mFriendFacebookAdapter);
 
 		Service service = getService();
 		setLoading(true);
 		if (mAccessToken != null && mAccessToken.length() > 0) {
 			service.searchFriends(SearchFriendCriteria.facebook,
 					getEncryptedToken(), queryString, mAccessToken, null, null);
 		}
 	}
 
 	private void getFriends() {
 		Service service = getService();
 		setLoading(true);
 		if(isFacebook){
 			if (mAccessToken != null && mAccessToken.length() > 0) {
 				mListview.setAdapter(mFriendFacebookAdapter);
 				service.getFriendsFacebook(getEncryptedToken(), mAccessToken);
 			}
 		}else{
 			
 		}
 	}
 
 	void addInviteFriend(FriendFacebook friend) {
 		INVITED_LIST.put(friend.getId(),
 				friend.getFirstName() + " " + friend.getLast_name());
 		mInviteContainer.setVisibility(View.VISIBLE);
 		displayInviteMessage(friend.getId());
 	}
 
 	void removeIntiveFriend(FriendFacebook friend) {
 		INVITED_LIST.remove(friend.getId());
 		if (INVITED_LIST.size() == 0) {
 			mInviteContainer.setVisibility(View.GONE);
 		} else {
 			displayInviteMessage(INVITED_LIST.keySet().toArray(new String[] {})[INVITED_LIST
 					.size() - 1]);
 		}
 	}
 
 	void displayInviteMessage(String userId) {
 		String key = INVITED_LIST.get(userId);
 		if (INVITED_LIST.size() > 1) {
 			String message = "Invite " + key + " and "
 					+ (INVITED_LIST.size() - 1) + " other to join WHYQ?";
 			Spannable messageSpannable = SpannableUtils.stylistTextBold(
 					message, key, R.color.orange);
 			mInviteMessage.setText(messageSpannable);
 		} else {
 			String message = "Invite " + key + " to join WHYQ?";
 			mInviteMessage.setText(SpannableUtils.stylistTextBold(message, key,
 					R.color.orange));
 		}
 	}
 
 	void inviteFriends() {
 		final String accessToken = getAccessToken();
 		Service service = getService();
 		setLoading(true);
 		StringBuilder userIds = new StringBuilder();
 		for (String id : INVITED_LIST.keySet()) {
 			userIds.append(id + ",");
 		}
 		service.inviteFriendsFacebook(getEncryptedToken(), userIds.toString(),
 				accessToken);
 	}
 
 	static class FriendsFacebookAdapter extends AmazingAdapter {
 
 		private static final int AVATAR_SIZE = WhyqApplication.sBaseViewHeight / 5 * 4;
 		private static final String SECTION_WHYQ = "joined whyq";
 		private static final String SECTION_NOT_JOIND_WHYQ = "not joined whyq";
 		private WhyqFriendsFacebookActivity mActivity;
 		private List<FriendFacebook> listWhyq;
 		private List<FriendFacebook> listNotJoinWhyq;
 		private ImageViewHelper mImageWorker;
 		private int countListNotJoinWhyq = 0;
 		private int countListWhyq = 0;
 		private Facebook facebookSdk = new Facebook(Constants.FACEBOOK_APP_ID);
 
 		public FriendsFacebookAdapter(WhyqFriendsFacebookActivity context,
 				ImageViewHelper imageWorker) {
 			this.mActivity = context;
 			this.mImageWorker = imageWorker;
 		}
 
 		public void setController(FriendFacebookController controller) {
 			if (controller != null) {
 				listWhyq = controller.getListWhyq();
 				listNotJoinWhyq = controller.getListNotJoinWhyq();
 			} else {
 				listWhyq = null;
 				listNotJoinWhyq = null;
 			}
 			if (listWhyq != null) {
 				countListWhyq = listWhyq.size();
 			} else {
 				countListWhyq = 0;
 			}
 			if (listNotJoinWhyq != null) {
 				countListNotJoinWhyq = listNotJoinWhyq.size();
 			} else {
 				countListNotJoinWhyq = 0;
 			}
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			return countListWhyq + countListNotJoinWhyq;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			if (position < 0 || position >= getCount()) {
 				return null;
 			}
 
 			if (position < countListWhyq) {
 				return listWhyq.get(position);
 			} else {
 				return listNotJoinWhyq.get(position - countListWhyq);
 			}
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		protected void onNextPageRequested(int page) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		protected void bindSectionHeader(View view, int position,
 				boolean displaySectionHeader) {
 			ViewHolder holder = getViewHolder(view);
 			if (displaySectionHeader) {
 				holder.header.setVisibility(View.VISIBLE);
 				bindHeader(holder,
 						getSections()[getSectionForPosition(position)]);
 			} else {
 				holder.header.setVisibility(View.GONE);
 			}
 
 		}
 		HashMap<String, View> viewList = new HashMap<String, View>();
 		@Override
 		public View getAmazingView(int position, View convertView,
 				ViewGroup parent) {
 			final FriendFacebook item = (FriendFacebook) getItem(position);
 			convertView = viewList.get(item.getFacebookId());
 			if (convertView == null) {
 				convertView = mActivity.getLayoutInflater().inflate(
 						R.layout.friend_list_item, parent, false);
 
 				Util.applyTypeface(convertView,
 						WhyqApplication.sTypefaceRegular);
 				final ViewHolder holder = getViewHolder(convertView);
 
 
 				holder.name.setText(item.getFirstName());
 				mImageWorker.downloadImage(item.getAvatar(), holder.avatar);
 				if (item.getIsFriend() == 0) {
 					holder.invite.setBackgroundResource(R.drawable.btn_accept);
 					holder.invite.setText("");
 				} else {
 					displayInviteButtn(holder, item);
 					holder.invite.setOnClickListener(new View.OnClickListener() {
 
 						@Override
 						public void onClick(View v) {
							if (!item.getIs_join()) {
 								if (INVITED_LIST.containsKey(item.getId())) {
 									mActivity.removeIntiveFriend(item);
 								} else {
 									mActivity.addInviteFriend(item);
 								}
 								displayInviteButtn(holder, item);
 							} else {
 								Bundle params = new Bundle();
								params.putString("title", "invite friends");
 								params.putString("to", item.getFacebookId());
 								facebookSdk.dialog(mActivity, "apprequests",
 										params, new DialogListener() {
 
 											@Override
 											public void onFacebookError(
 													FacebookError e) {
 												// TODO Auto-generated method stub
												Log.d("DialogListener", "onFacebookError"+e.getMessage());
 											}
 
 											@Override
 											public void onError(DialogError e) {
 												// TODO Auto-generated method stub
												Log.d("DialogListener", "onError"+e.getMessage());
 											}
 
 											@Override
 											public void onComplete(Bundle values) {
 												// TODO Auto-generated method stub
												Log.d("DialogListener", "onComplete");
 											}
 
 											@Override
 											public void onCancel() {
 												// TODO Auto-generated method stub
												Log.d("DialogListener", "onCancel");
 											}
 										});
 							}
 						}
 					});
 				}
 				viewList.put(item.getFacebookId(), convertView);
 			}else{
 				
 			}
 
 
 
 			return convertView;
 		}
 
 		private void displayInviteButtn(ViewHolder holder, FriendFacebook item) {
 			if (INVITED_LIST.containsKey(item.getId())) {
 				holder.invite.setBackgroundResource(R.drawable.btn_accept);
 				holder.invite.setText("");
 			} else {
 				holder.invite.setBackgroundResource(R.drawable.btn_base);
 				holder.invite.setText(R.string.invite);
 			}
 		}
 
 		@Override
 		public void configurePinnedHeader(View header, int position, int alpha) {
 			bindHeader(getViewHolder(header),
 					getSections()[getSectionForPosition(position)]);
 		}
 
 		@Override
 		public int getPositionForSection(int section) {
 			if (section == 0) {
 				return 0;
 			} else {
 				return countListWhyq;
 			}
 		}
 
 		@Override
 		public int getSectionForPosition(int position) {
 			if (position >= 0 && position < countListWhyq) {
 				return 0;
 			} else {
 				return 1;
 			}
 		}
 
 		@Override
 		public String[] getSections() {
 			return new String[] { SECTION_WHYQ, SECTION_NOT_JOIND_WHYQ };
 		}
 
 		private void bindHeader(ViewHolder holder, String section) {
 			if (section.equals(SECTION_WHYQ)) {
 				final String key = countListWhyq + " facebook friends";
 				final String result = "You have " + key + " had joined WHY Q.";
 				Spannable message = SpannableUtils.stylistTextBold(result, key,
 						mActivity.getResources().getColor(R.color.orange));
 				holder.headerMessage.setText(message);
 				holder.headerFriendAll.setVisibility(View.VISIBLE);
 			} else {
 				final String key = countListNotJoinWhyq + " facebook friends";
 				final String result = "And "
 						+ key
 						+ " haven't joined WHY Q. Invite your friend to join this app!";
 				final Spannable message = SpannableUtils.stylistTextBold(
 						result, key,
 						mActivity.getResources().getColor(R.color.orange));
 				holder.headerMessage.setText(message);
 				holder.headerFriendAll.setVisibility(View.GONE);
 			}
 		}
 
 		private ViewHolder getViewHolder(View view) {
 			ViewHolder holder = (ViewHolder) view.getTag();
 			if (holder == null) {
 				holder = new ViewHolder(view);
 				view.setTag(holder);
 			}
 			return holder;
 		}
 
 		private class ViewHolder {
 			ImageView avatar;
 			TextView name;
 			Button invite;
 			View header;
 			TextView headerMessage;
 			Button headerFriendAll;
 
 			public ViewHolder(View view) {
 				avatar = (ImageView) view.findViewById(R.id.avatar);
 				if (avatar != null) {
 					avatar.getLayoutParams().width = AVATAR_SIZE;
 					avatar.getLayoutParams().height = AVATAR_SIZE;
 				}
 				name = (TextView) view.findViewById(R.id.name);
 				invite = (Button) view.findViewById(R.id.invite);
 				header = view.findViewById(R.id.header);
 				headerMessage = (TextView) header.findViewById(R.id.message);
 				headerFriendAll = (Button) header.findViewById(R.id.friendAll);
 			}
 		}
 
 	}
 	public void onBackClicked(View v){
 		finish();
 	}
 }
