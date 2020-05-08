 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import whyq.WhyqApplication;
 import whyq.model.ActivityItem;
 import whyq.service.DataParser;
 import whyq.service.Service;
 import whyq.service.ServiceResponse;
 import whyq.utils.XMLParser;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.whyq.R;
 
 public class UserBoardActivity extends ImageWorkerActivity {
 
 	private static final int AVATAR_SIZE = WhyqApplication.sBaseViewHeight / 5 * 4;
 	private ActivitiesAdapter mAdapter;
 	private String mUserFirstName;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_user_board);
 
 		initCategory();
 
 		mUserFirstName = XMLParser.getValue(this, XMLParser.STORE_USER_NAME);
 		if (mUserFirstName != null) {
 			setTitle(mUserFirstName);
 		}
 		final String avatar = XMLParser.getValue(this,
 				XMLParser.STORE_USER_AVATAR);
 		if (avatar != null) {
 			ImageView imageView = (ImageView) findViewById(R.id.avatar);
 			LayoutParams LP = imageView.getLayoutParams();
 			LP.width = AVATAR_SIZE;
 			LP.height = AVATAR_SIZE;
 			mImageWorker.loadImage(avatar, imageView, AVATAR_SIZE, AVATAR_SIZE);
 		}
 
 		mAdapter = new ActivitiesAdapter(this);
 		((ListView) findViewById(R.id.listview)).setAdapter(mAdapter);
 
 		final Service service = getService();
 
 		setLoading(true);
 		final String userId = XMLParser.getUserId(this);
 		service.getUserActivities(getEncryptedToken(), userId);
 
 	}
 
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		super.onCompleted(service, result);
 		setLoading(false);
 		mAdapter.setItems(DataParser.parseActivities(String.valueOf(result
 				.getData())));
 	}
 
 	private void initCategory() {
 		final String totalCheckBill = XMLParser.getValue(this,
 				XMLParser.STORE_TOTAL_CHECK_BILL);
 		bindCategory(R.id.check_bill, R.drawable.icon_cat_coffee,
 				totalCheckBill + " places", "checked bills",
 				new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						startActivity(new Intent(UserBoardActivity.this,
 								CheckedBillActivity.class));
 					}
 				});
 		bindCategory(R.id.history, R.drawable.icon_cat_cutlery, "15",
 				"History", new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 
 					}
 				});
 
 		final String totalSaving = XMLParser.getValue(this,
 				XMLParser.STORE_TOTAL_SAVING);
 		bindCategory(R.id.saving, R.drawable.icon_cat_wine, "$" + totalSaving,
 				"Saving", new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 
 					}
 				});
 
 		final String totalComment = XMLParser.getValue(this,
 				XMLParser.STORE_TOTAL_COMMENT);
 		bindCategory(R.id.comment, R.drawable.icon_cat_wine,
 				totalComment == "" ? "0" : totalComment, "Comments",
 				new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						startActivity(new Intent(UserBoardActivity.this,
 								CommentActivity.class));
 					}
 				});
 	}
 
 	private void bindCategory(int id, int iconRes, String textTop,
 			String textBottom, OnClickListener listener) {
 		View categoryView = findViewById(id);
 		categoryView.getLayoutParams().height = WhyqApplication.sBaseViewHeight;
 		categoryView.setOnClickListener(listener);
 		ImageView icon = (ImageView) categoryView.findViewById(R.id.icon);
 		icon.getLayoutParams().height = WhyqApplication.sBaseViewHeight;
 		icon.setImageResource(iconRes);
 		TextView textviewTop = (TextView) categoryView
 				.findViewById(R.id.textviewTop);
 		textviewTop.setText(textTop);
 		TextView textviewBottom = (TextView) categoryView
 				.findViewById(R.id.textviewBottom);
 		textviewBottom.setText(textBottom);
 	}
 
 	@Override
 	protected View getTitleView() {
 		LinearLayout titleView = (LinearLayout) getLayoutInflater().inflate(
				R.layout.navigation_title_include_avatar, null);
 		View title = super.getTitleView();
 		LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(0,
 				LinearLayout.LayoutParams.WRAP_CONTENT, 1);
 		titleView.addView(title, 1, LP);
 		return titleView;
 	}
 
 	class ActivitiesAdapter extends BaseAdapter {
 
 		private Context mContext;
 		private List<ActivityItem> mItems;
 
 		public ActivitiesAdapter(Context context) {
 			this.mContext = context;
 			this.mItems = new ArrayList<ActivityItem>();
 		}
 
 		public void setItems(List<ActivityItem> items) {
 			if (items == null || items.size() == 0) {
 				mItems.clear();
 			} else {
 				mItems = items;
 			}
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			return mItems.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return mItems.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(mContext).inflate(
 						R.layout.activities_list_item, parent, false);
 			}
 
 			ViewHolder holder = getViewHolder(convertView);
 			ActivityItem item = mItems.get(position);
 
 			holder.activity.setText(Html.fromHtml(item.getMessage()));
 			holder.activity.setCompoundDrawablesWithIntrinsicBounds(
 					R.drawable.icon_quote, 0, 0, 0);
 
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
 			TextView activity;
 
 			public ViewHolder(View view) {
 				activity = (TextView) view.findViewById(R.id.activity);
 			}
 		}
 
 	}
 }
