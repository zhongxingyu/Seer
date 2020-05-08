 package whyq.activity;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import whyq.WhyqApplication;
 import whyq.adapter.AmazingAdapter;
 import whyq.interfaces.FriendFacebookController;
 import whyq.model.FriendFacebook;
 import whyq.model.FriendWhyq;
 import whyq.model.SearchFriendCriteria;
 import whyq.model.StatusWithFriend;
 import whyq.service.DataParser;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.ImageViewHelper;
 import whyq.utils.SpannableUtils;
 import whyq.utils.Util;
 import whyq.utils.WhyqUtils;
 import whyq.view.AmazingListView;
 import whyq.view.SearchField;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.whyq.R;
 
 public class WhyqFriendsFacebookActivity extends ImageWorkerActivity {
 
 	private FriendsFacebookAdapter mFriendFacebookAdapter = null;
 	private String mAccessToken;
 	private AmazingListView mListview;
 	private TextView mInviteMessage;
 	private FrameLayout mInviteButton;
 	private View mInviteContainer;
 	private static final Map<String, FriendFacebook> INVITED_LIST = new HashMap<String, FriendFacebook>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_facebook_friends);
 
 		showHeaderSearchField(true);
 		SearchField searchField = getSearchField();
 		EditText tvSearch = searchField.getEditTextView();
 
 		tvSearch.setHint(R.string.find_a_friend);
 		tvSearch.setTextColor(getResources().getColor(R.color.white));
 		tvSearch.setBackgroundResource(R.drawable.textfield_search_default_holo_dark);
 
 		mListview = (AmazingListView) findViewById(R.id.listview);
		mListview.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.friend_list_item_header, mListview, false));
 		// TODO: handle on item click
 		// mListview.setOnItemClickListener(new OnItemClickListener() {
 		//
 		// @Override
 		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 		// long arg3) {
 		// final Object item = arg0.getItemAtPosition(arg2);
 		// if (item instanceof FriendFacebook) {
 		// FriendFacebook facebook = (FriendFacebook) item;
 		// startUserProfileActivity(facebook.getId(),
 		// facebook.getFirstName(), facebook.getAvatar());
 		// } else if (item instanceof FriendWhyq) {
 		// FriendWhyq whyq = (FriendWhyq) item;
 		// startUserProfileActivity(whyq.getId(),
 		// whyq.getFirst_name(), whyq.getAvatar());
 		// }
 		// }
 		// });
 
 		mFriendFacebookAdapter = new FriendsFacebookAdapter(this, mImageWorker);
 		mAccessToken = getAccessToken();
 
 		setTitle(R.string.friend_from_facebook);
 		getFriends();
 
 //		mInviteContainer = findViewById(R.id.inviteContainer);
 //		mInviteMessage = (TextView) findViewById(R.id.inviteMessage);
 //		mInviteButton = (FrameLayout) findViewById(R.id.inviteButton);
 //		mInviteButton.setOnClickListener(new View.OnClickListener() {
 //
 //			@Override
 //			public void onClick(View v) {
 //				if (INVITED_LIST.size() == 0) {
 //					return;
 //				}
 //				final StringBuilder userIds = new StringBuilder();
 //				for (String key : INVITED_LIST.keySet()) {
 //					userIds.append(key + ",");
 //				}
 //				inviteFriends(userIds.substring(0, userIds.length() - 1));
 //			}
 //		});
 
 	}
 
 	private void startUserProfileActivity(String userId, String userName,
 			String avatar) {
 		Intent i = new Intent(this, WhyqUserProfileActivity.class);
 		i.putExtra(WhyqUserProfileActivity.ARG_USER_ID, userId);
 		i.putExtra(WhyqUserProfileActivity.ARG_USER_NAME, userName);
 		i.putExtra(WhyqUserProfileActivity.ARG_AVATAR, avatar);
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
 
 			} else {
 				if (result.getAction() == ServiceAction.ActionGetFriendsFacebook
 						|| result.getAction() == ServiceAction.ActionSearchFriendsFacebook) {
 					FriendFacebookController handler = DataParser
 							.parseFriendFacebook(String.valueOf(result
 									.getData()));
 					mFriendFacebookAdapter.setController(handler);
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
 		if (mAccessToken != null && mAccessToken.length() > 0) {
 			mListview.setAdapter(mFriendFacebookAdapter);
 			service.getFriendsFacebook(getEncryptedToken(), mAccessToken);
 		}
 	}
 
 	void addInviteFriend(FriendFacebook friend) {
 		INVITED_LIST.put(friend.getId(), friend);
 		mInviteContainer.setVisibility(View.VISIBLE);
 		if (INVITED_LIST.size() > 1) {
 		} else {
 			String key = friend.getFirstName();
 			String message = "Invite " + key + " to join WHYQ?";
 			mInviteMessage.setText(SpannableUtils.stylistText(message, key,
 					R.color.orange));
 		}
 	}
 
 	void inviteFriends(String userId) {
 		final String accessToken = getAccessToken();
 		Service service = getService();
 		setLoading(true);
 		service.inviteFriendsFacebook(getEncryptedToken(), userId, accessToken);
 	}
 
 	static class FriendsFacebookAdapter extends AmazingAdapter {
 
 		private static final int AVATAR_SIZE = WhyqApplication.sBaseViewHeight / 5 * 4;
 		private static final String SECTION_WHYQ = "joined whyq";
 		private static final String SECTION_NOT_JOIND_WHYQ = "not joined whyq";
 		private WhyqFriendsFacebookActivity mActivity;
 		private List<FriendFacebook> listWhyq;
 		private List<FriendFacebook> listNotJoinWhyq;
 		private ImageViewHelper mImageWorker;
 		private static int countListNotJoinWhyq = 0;
 		private static int countListWhyq = 0;
 
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
 				bindHeader(holder, getSections()[getSectionForPosition(position)]);
 			} else {
 				holder.header.setVisibility(View.GONE);
 			}
 
 		}
 
 		@Override
 		public View getAmazingView(int position, View convertView,
 				ViewGroup parent) {
 			if (convertView == null) {
 				convertView = mActivity.getLayoutInflater().inflate(
 						R.layout.friend_list_item, parent, false);
 
 				Util.applyTypeface(convertView,
 						WhyqApplication.sTypefaceRegular);
 			}
 
 			final ViewHolder holder = getViewHolder(convertView);
 			final FriendFacebook item = (FriendFacebook) getItem(position);
 
 			holder.name.setText(item.getFirstName());
 			mImageWorker.downloadImage(item.getAvatar(), holder.avatar);
 
 			return convertView;
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
			if (position >=0 && position < countListWhyq) {
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
 
 }
