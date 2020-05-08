 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import whyq.WhyqApplication;
 import whyq.model.Comment;
 import whyq.model.Photo;
 import whyq.model.ResponseData;
 import whyq.service.DataParser;
 import whyq.service.ResultCode;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.ImageViewHelper;
 import whyq.utils.Util;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 
 import com.whyq.R;
 
 public class CommentActivity extends ImageWorkerActivity {
 
 	private CommentAdapter mAdapter;
 	private RadioGroup mFilterLayout;
 	private String mStoreId;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_comment);
 
 		setTitle(R.string.comment_title);
 
 		mAdapter = new CommentAdapter(this, mImageWorker);
 
 		ListView listview = (ListView) findViewById(R.id.listview);
 		listview.setAdapter(mAdapter);
 
 		Intent i = getIntent();
 		mStoreId = i.getStringExtra("store_id");
 		if (mStoreId == null) {
 			mStoreId = "";
 		}
 
 		mFilterLayout = (RadioGroup) findViewById(R.id.filterLayout);
 		mFilterLayout.getLayoutParams().width = WhyqApplication.sScreenWidth * 2 / 3;
 		mFilterLayout.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				if (checkedId == R.id.viewAll) {
 					getComments(false);
 				} else {
 					getComments(true);
 				}
 			}
 		});
 
 		View filter = getLayoutInflater().inflate(R.layout.filter, null);
 		setExtraView(filter);
 		getComments(false);
 	}
 
 	private void getComments(boolean onlyFriend) {
 		setLoading(true);
 		getService().getComments(getEncryptedToken(), mStoreId, 1, 20,
 				onlyFriend);
 	}
 
 	@Override
 	protected void onExtraButtonPressed(View v) {
 		super.onExtraButtonPressed(v);
 		if (mFilterLayout.getVisibility() == View.VISIBLE) {
 			mFilterLayout.setVisibility(View.GONE);
 			findViewById(R.id.triAngle).setVisibility(View.GONE);
 		} else {
 			mFilterLayout.setVisibility(View.VISIBLE);
 			findViewById(R.id.triAngle).setVisibility(View.VISIBLE);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		super.onCompleted(service, result);
 		setLoading(false);
 		if (result != null && result.getCode() == ResultCode.Success
 				&& result.getAction() == ServiceAction.ActionGetComment) {
 			DataParser paser = new DataParser();
 			ResponseData data = (ResponseData) paser.parseComments(String
 					.valueOf(result.getData()));
 			if (data == null) {
 				return;
 			}
 			
 			if (data.getStatus().equals("401")) {
 				Util.loginAgain(this, data.getStatus());
 			} else {
 				mAdapter.setItems((List<Comment>) data.getData());
 			}
 		}
 	}
 
 	static class CommentAdapter extends BaseAdapter {
 
 		private static final int AVATAR_SIZE = WhyqApplication.sBaseViewHeight / 5 * 4;
 		private static final int THUMB_HEIGHT = WhyqApplication.sBaseViewHeight * 5;
 		private Context mContext;
 		private List<Comment> mItems;
 		private ImageViewHelper mImageWorker;
 
 		public CommentAdapter(Context context, ImageViewHelper imageWorker) {
 			this.mContext = context;
 			this.mItems = new ArrayList<Comment>();
 			this.mImageWorker = imageWorker;
 		}
 
 		public void setItems(List<Comment> items) {
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
 						R.layout.comment_list_item, parent, false);
 				Util.applyTypeface(convertView,
 						WhyqApplication.sTypefaceRegular);
 			}
 
 			ViewHolder holder = getViewHolder(convertView);
 			Comment item = mItems.get(position);
 
 			holder.name.setText(item.getUser().getFirstName());
 			holder.comment.setText(item.getContent());
 			holder.like.setText("" + item.getCount_like());
 			holder.like.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 
 				}
 			});
 
 			mImageWorker.downloadImage(item.getUser().getUrlAvatar(),
 					holder.avatar);
 			Photo photo = item.getPhotos();
 			if (photo != null) {
 				mImageWorker.downloadImage(item.getPhotos().getThumb(),
 						holder.thumb);
 			}
 			if (item.getUser().getLike() > 0) {
 				holder.favorite.setImageResource(R.drawable.icon_fav_enable);
 			} else {
 				holder.favorite.setImageResource(R.drawable.icon_fav_disable);
 			}
 
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
 			ImageView favorite;
 			ImageView thumb;
 			TextView name;
 			TextView like;
 			TextView comment;
 
 			public ViewHolder(View view) {
 				view.findViewById(R.id.infor).getLayoutParams().height = WhyqApplication.sBaseViewHeight;
 				avatar = (ImageView) view.findViewById(R.id.avatar);
 				avatar.getLayoutParams().width = AVATAR_SIZE;
 				avatar.getLayoutParams().height = AVATAR_SIZE;
 				thumb = (ImageView) view.findViewById(R.id.imageThumb);
 				thumb.getLayoutParams().height = THUMB_HEIGHT;
 
 				favorite = (ImageView) view.findViewById(R.id.favorite);
 				name = (TextView) view.findViewById(R.id.name);
 				like = (TextView) view.findViewById(R.id.like);
 				comment = (TextView) view.findViewById(R.id.comment);
 			}
 		}
 
 	}

 }
