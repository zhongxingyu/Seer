 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import whyq.WhyqApplication;
 import whyq.model.FriendFacebook;
 import whyq.model.FriendWhyq;
 import whyq.model.ResponseData;
 import whyq.model.SearchFriendCriteria;
 import whyq.service.DataParser;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.ImageViewHelper;
 import whyq.utils.Util;
 import whyq.utils.XMLParser;
 import whyq.view.SearchField;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.whyq.R;
 
 import dialog.AlertFindFriendDialog;
 import dialog.AlertFindFriendDialog.OnDialogButtonClickListener;
 
 public class WhyqFriendsActivity extends ImageWorkerActivity implements
 		OnDialogButtonClickListener {
 
 	private FriendsWhyqAdapter mFriendWhyqAdapter = null;
 	private ListView mListview;
 	private boolean isSearchByName;
 	private boolean isFriend;
 	private String friendId;
 	private RelativeLayout rlContent;
 	private Context context;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_friends);
 		context = this;
 		rlContent = (RelativeLayout)findViewById(R.id.rlContent);
 		isSearchByName = getIntent().getBooleanExtra("is_search_by_name", false);
 		isFriend = getIntent().getBooleanExtra("is_friend", false);
 		friendId = getIntent().getStringExtra("friend_id");
 		SearchField searchField = (SearchField) findViewById(R.id.searchField);
 		searchField.setQueryCallback(this);
 		searchField.getEditTextView().setHint(R.string.find_a_friend);
 
 		mListview = (ListView) findViewById(R.id.listview);
 		mListview.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				final Object item = arg0.getItemAtPosition(arg2);
 				if (item instanceof FriendFacebook) {
 					FriendFacebook facebook = (FriendFacebook) item;
 					startUserProfileActivity(facebook.getId(),
 							facebook.getFirstName(), facebook.getAvatar());
 				} else if (item instanceof FriendWhyq) {
 					FriendWhyq whyq = (FriendWhyq) item;
 					startUserProfileActivity(whyq.getId(),
 							whyq.getFirst_name()+" "+whyq.getLast_name(), whyq.getAvatar());
 				}
 			}
 		});
 
 		mFriendWhyqAdapter = new FriendsWhyqAdapter(this, mImageWorker);
 		mFriendWhyqAdapter.notifyDataSetChanged();
 		mListview.setAdapter(mFriendWhyqAdapter);
 
 		setTitle(R.string.friends);
 
 		// Set extra button.
 
 		if(isFriend){
 			setTitle("Friends");
 			setBackButtonIcon(R.drawable.icon_back);
 			rlContent.setBackgroundColor(getResources().getColor(R.color.white));
 		}else if(!isSearchByName){
 			ImageView imageView = new ImageView(this);
 			imageView.setImageResource(R.drawable.icon_add_friend);
 			setExtraView(imageView);
 			setBackButtonIcon(R.drawable.icon_friend_invite);
 		}else {
 			setTitle("Search by name");
 			rlContent.setBackgroundColor(getResources().getColor(R.color.white));
 		}
 
 //		getFriends();
 
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		getFriends();
 	}
 	
 	public void onInviteClicked(View v){
 		final FriendWhyq item = (FriendWhyq)v.getTag();		
 		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
 				context);
 		builder.setTitle(context.getString(R.string.app_name_title));
 		builder.setMessage("Are you sure to unfriend "+item.getFirst_name()+" "+item.getLast_name()+" on WHY Q?");
 		final android.app.AlertDialog alertError = builder.create();
 		alertError.setButton("Ok", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 
 				setLoading(true);
 				Service service = new Service(WhyqFriendsActivity.this);
 				service.unFriend(WhyqApplication.Instance().getRSAToken(),item.getId());
 			}
 		});
 		alertError.setButton2("Cancel", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				alertError.dismiss();
 			}
 		});
 		alertError.show();
 	}
 	
 	private void startUserProfileActivity(String userId, String userName,
 			String avatar) {
 		Intent i = new Intent(this, WhyqUserProfileActivity.class);
 		i.putExtra(WhyqUserProfileActivity.ARG_USER_ID, userId);
 		i.putExtra("is_friend", true);
 		i.putExtra("is_friended", true);
 		startActivity(i);
 	}
 
 	@Override
 	public void onQuery(String queryString) {
 		if (queryString != null && queryString.length() > 0) {
 			searchFriends(queryString);
 		} else {
 			getFriends();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		super.onCompleted(service, result);
 		setLoading(false);
 		if (result != null
 				&& result.getAction() == ServiceAction.ActionGetFriends) {
 			DataParser parser = new DataParser();
 
 			try {
 				ResponseData data =  (ResponseData) parser.parseFriendWhyq(String
 						.valueOf(result.getData()));
 				
 				if (data.getStatus().equals("401")) {
 					Util.loginAgain(this, data.getMessage());
 				} else {
 					List<FriendWhyq> friends = (List<FriendWhyq>) data.getData();;
 					if (friends == null || friends.size() == 0) {
 						if(!isSearchByName&& !isFriend){
 							AlertFindFriendDialog dialog = new AlertFindFriendDialog();
 							dialog.setOnDialogButtonClickListern(this);
 							dialog.show(getSupportFragmentManager(), "Dialog");
 
 						}
 					} else {
 						mFriendWhyqAdapter.setItems(friends);
 					}
 				}
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}else if(result.isSuccess()&& (result != null
 				&& result.getAction() == ServiceAction.ActionSearchFriendsFacebook)){
 
 			DataParser parser = new DataParser();
 			ResponseData data =  (ResponseData) parser.parseFriendWhyq(String
 					.valueOf(result.getData()));
 			
 			if (data.getStatus().equals("401")) {
 				Util.loginAgain(this, data.getMessage());
 			} else {
 				List<FriendWhyq> friends = (List<FriendWhyq>) data.getData();;
 				if (friends == null || friends.size() == 0) {
 					mFriendWhyqAdapter.setItems(friends);
 				} else {
 					mFriendWhyqAdapter.setItems(friends);
 				}
 			}
 		
 		}else if(result.isSuccess()
 				&& result.getAction() == ServiceAction.ActionSearchOnlyFriend){
 
 			DataParser parser = new DataParser();
 			ResponseData data =  (ResponseData) parser.parseFriendWhyq(String
 					.valueOf(result.getData()));
 			
 			if (data.getStatus().equals("401")) {
 				Util.loginAgain(this, data.getMessage());
 			} else {
 				List<FriendWhyq> friends = (List<FriendWhyq>) data.getData();;
 				if (friends == null || friends.size() == 0) {
 					mFriendWhyqAdapter.setItems(friends);
 				} else {
 					mFriendWhyqAdapter.setItems(friends);
 				}
 			}
 		
 		} else if (result.isSuccess()
 				&& result.getAction() == ServiceAction.ActionUnFriend) {
 			setLoading(false);
 			ResponseData data = (ResponseData) result.getData();
 			if (data.getStatus().equals("200")) {
 //				showToast(data.getMessage());
 				Util.showDialog(context, data.getMessage());
 				getFriends();
 			} else if (data.getStatus().equals("401")) {
 				Util.loginAgain(getParent(), data.getMessage());
 			} else if (data.getStatus().equals("204")) {
 
 			} else {
 			}
 		}
 	}
 	
 	@Override
 	protected void onExtraButtonPressed(View v) {
 		super.onExtraButtonPressed(v);
 		startActivity(new Intent(this, WhyqFindMenuActivity.class));
 	}
 
 	@Override
 	public void onPositiveButtonClick() {
 		startActivity(new Intent(this, WhyqFindMenuActivity.class));
 	}
 
 	@Override
 	public void onNegativeButtonClick() {
 
 	}
 
 	private void searchFriends(String queryString) {
 		Log.d("searchFriends","key: "+queryString);
 		Service service = getService();
 		setLoading(true);
 		if(!isSearchByName){
 			service.searchFriends(SearchFriendCriteria.whyq, getEncryptedToken(),
 					queryString, null, null, null);
 		}else{
 			service.searchOnlyFriend(SearchFriendCriteria.whyq, getEncryptedToken(),
 					queryString, null, null, null);
 		}
 
 	}
 
 	private void getFriends() {
 
 		if(isFriend){
 			Service service = getService();
 			setLoading(true);
 			service.getFriends(getEncryptedToken(),
					XMLParser.getValue(this, friendId));
 		}else{
 			Service service = getService();
 			setLoading(true);
 			service.getFriends(getEncryptedToken(),
 					XMLParser.getValue(this, XMLParser.STORE_USER_ID));
 		}
 	}
 
 	static class FriendsWhyqAdapter extends BaseAdapter {
 
 		private static final int AVATAR_SIZE = WhyqApplication.sBaseViewHeight / 5 * 4;
 		private WhyqFriendsActivity mActivity;
 		private List<FriendWhyq> listWhyq;
 		private ImageViewHelper mImageWorker;
 
 		public FriendsWhyqAdapter(WhyqFriendsActivity context,
 				ImageViewHelper imageWorker) {
 			this.mActivity = context;
 			this.mImageWorker = imageWorker;
 			this.listWhyq = new ArrayList<FriendWhyq>();
 		}
 
 		public void setItems(List<FriendWhyq> items) {
 			if (items == null || items.size() == 0) {
 				listWhyq.clear();
 			} else {
 				listWhyq = items;
 			}
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			return listWhyq.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return listWhyq.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = mActivity.getLayoutInflater().inflate(
 						R.layout.friend_list_item, parent, false);
 				Util.applyTypeface(convertView,
 						WhyqApplication.sTypefaceRegular);
 			}
 
 			final ViewHolder holder = getViewHolder(convertView);
 
 			final FriendWhyq item = listWhyq.get(position);
 			holder.name.setText(item.getFirst_name()+" "+item.getLast_name());
 			if(item.getStatus_user().getIs_friend()==1){
 				holder.invite.setBackgroundResource(R.drawable.icon_friended);
 				holder.invite.setVisibility(View.VISIBLE);
 				holder.invite.setText("");
 				holder.invite.setTag(item);
 			}else{
 				holder.invite.setVisibility(View.GONE);
 			}
 			mImageWorker.downloadImage(item.getAvatar(), holder.avatar);
 			return convertView;
 		}
 
 		private ViewHolder getViewHolder(View view) {
 			ViewHolder holder = (ViewHolder) view.getTag();
 			if (holder == null) {
 				holder = new ViewHolder(view);
 				view.setTag(holder);
 			}
 			return holder;
 		}
 
 		class ViewHolder {
 			ImageView avatar;
 			TextView name;
 			Button invite;
 
 			public ViewHolder(View view) {
 				view.getLayoutParams().height = WhyqApplication.sBaseViewHeight;
 				avatar = (ImageView) view.findViewById(R.id.avatar);
 				if (avatar != null) {
 					avatar.getLayoutParams().width = AVATAR_SIZE;
 					avatar.getLayoutParams().height = AVATAR_SIZE;
 				}
 				name = (TextView) view.findViewById(R.id.name);
 				invite = (Button) view.findViewById(R.id.invite);
 //				invite.setVisibility(View.GONE);
 			}
 		}
 
 	}
 	public void onBackClicked(View v){
 //		finish();
 	}
 }
