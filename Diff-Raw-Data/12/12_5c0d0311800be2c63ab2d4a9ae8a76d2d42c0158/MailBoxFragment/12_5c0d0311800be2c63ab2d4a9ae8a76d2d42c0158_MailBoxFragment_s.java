 package com.dbstar.app.settings.smartcard;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.dbstar.R;
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.app.base.FragmentObserver;
 import com.dbstar.model.EMailItem;
 import com.dbstar.service.GDDataProviderService;
 
 public class MailBoxFragment extends GDSmartcardFragment {
 
 	TextView mMailContentView;
 	ViewGroup mMailListContainer;
 	ListView mMailListView;
 	ListAdapter mAdapter;
 
 	ArrayList<EMailItem> mMailsList;
 	EMailItem[] mMails;
 
 	int mSelectedItemIndex = -1;
 	Drawable mItemLightBackground, mItemDarkBackground, mItemFocusedBackground;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.mail_view, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		initializeView();
 	}
 
 	public void serviceStart() {
 		if (mSmartcardEngine != null) {
 			mSmartcardEngine.getSmartcardInfo(this,
 					DbstarServiceApi.CMD_DRM_EMAILHEADS_READ);
 		}
 	}
 
 	public void updateData(FragmentObserver observer, int type, Object key,
 			Object data) {
 
 		if (observer != this || data == null)
 			return;
 
 		if (type == GDDataProviderService.REQUESTTYPE_GETSMARTCARDINFO) {
 			int requestType = (Integer) key;
 			if (requestType == DbstarServiceApi.CMD_DRM_EMAILHEADS_READ) {
 				EMailItem[] mails = (EMailItem[]) data;
 
 				if (mMailsList == null) {
 					mMailsList = new ArrayList<EMailItem>();
 				}
 
 				for (int i = 0; i < mails.length; i++) {
					mMailsList.add(mails[i]);
 				}
 
 				mMails = mMailsList.toArray(new EMailItem[mMailsList.size()]);
 				mAdapter.setDataSet(mMails);
 				mAdapter.notifyDataSetChanged();
 			}
 		} else if (type == GDDataProviderService.REQUESTTYPE_GETMAILCONTENT) {
 			String id = (String) key;
 			String content = (String) data;
 
 			int length = mMails.length;
 			for(int i=0; i<length; i++) {
 				EMailItem item = mMails[i];
 				if (item.ID.equals(id)) {
 					item.Content = content;
 					break;
 				}
 			}
 
 			if (mMailContentView.getVisibility() == View.VISIBLE) {
 				mMailContentView.setText(content);
 			}
 		}
 	}
 
 	public void notifyEvent(FragmentObserver observer, int type, Object event) {
 		if (observer != this)
 			return;
 	}
 
 	void displayMailContent(int index) {
 		mMailListContainer.setVisibility(View.GONE);
 		
 		mMailContentView.setText("");
 		mMailContentView.setVisibility(View.VISIBLE);
 
 		EMailItem mail = mMails[index];
 
 		mail.Flag = 1;
 		if (mail.Content == null) {
 			mSmartcardEngine.getMailContent(this, mail.ID);
 		} else {
 			mMailContentView.setText(mail.Content);
 		}
 	}
 
 	public boolean onBackkeyPress() {
 		if (mMailContentView.getVisibility() == View.VISIBLE) {
 			mMailContentView.setVisibility(View.GONE);
 			mMailListContainer.setVisibility(View.VISIBLE);
 			mAdapter.notifyDataSetChanged();
 
 			return true;
 		}
 
 		return false;
 	}
 
 	void initializeView() {
 
 		mItemLightBackground = getResources().getDrawable(
 				R.drawable.listitem_light_bg);
 		mItemDarkBackground = getResources().getDrawable(
 				R.drawable.listitem_dark_bg);
 		mItemFocusedBackground = getResources().getDrawable(
 				R.drawable.receive_item_focused_bg);
 
 		mMailListContainer = (ViewGroup) mActivity.findViewById(R.id.mail_list);
 		mMailContentView = (TextView) mActivity.findViewById(R.id.mail_content);
 		mMailListView = (ListView) mActivity.findViewById(R.id.list_view);
 
 		mAdapter = new ListAdapter(mActivity);
 		mMailListView.setAdapter(mAdapter);
 
 		mMailListView.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				if (mSelectedItemIndex >= 0) {
 					View oldSel = mMailListView.getChildAt(mSelectedItemIndex);
 					Drawable d = position % 2 == 0 ? mItemLightBackground
 							: mItemDarkBackground;
 					oldSel.setBackgroundDrawable(d);
 				}
 
 				mSelectedItemIndex = position;
 				view.setBackgroundDrawable(mItemFocusedBackground);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 
 			}
 
 		});
 
 		mMailListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				displayMailContent(position);
 			}
 
 		});
 	}
 
 	private class ListAdapter extends BaseAdapter {
 
 		public class ViewHolder {
 			TextView mailId;
 			TextView date;
 			TextView flag;
 			TextView title;
 		}
 
 		private EMailItem[] mDataSet = null;

 		public ListAdapter(Context context) {
 		}
 
 		public void setDataSet(EMailItem[] dataSet) {
 			mDataSet = dataSet;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			if (mDataSet != null) {
 				count = mDataSet.length;
 			}
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder = null;
 			if (null == convertView) {
 				LayoutInflater inflater = mActivity.getLayoutInflater();
 				convertView = inflater.inflate(R.layout.email_item, parent,
 						false);
 
 				holder = new ViewHolder();
 				holder.mailId = (TextView) convertView
 						.findViewById(R.id.email_id);
 				holder.date = (TextView) convertView
 						.findViewById(R.id.email_date);
 				holder.flag = (TextView) convertView
 						.findViewById(R.id.email_flag);
 				holder.title = (TextView) convertView
 						.findViewById(R.id.email_title);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			holder.mailId.setText(mDataSet[position].ID);
 			holder.date.setText(mDataSet[position].Date);
			holder.flag.setText(String.valueOf(mDataSet[position].Flag));
 			holder.title.setText(mDataSet[position].Title);
 
 			if (position == mMailListView.getSelectedItemPosition()) {
 				convertView.setBackgroundDrawable(mItemFocusedBackground);
 				holder.mailId.setTextColor(Color.WHITE);
 				holder.date.setTextColor(Color.WHITE);
 				holder.flag.setTextColor(Color.WHITE);
 				holder.title.setTextColor(Color.WHITE);
 			} else {
 				holder.mailId.setTextColor(Color.BLACK);
 				holder.date.setTextColor(Color.BLACK);
 				holder.flag.setTextColor(Color.BLACK);
 				holder.title.setTextColor(Color.BLACK);
 				if (position % 2 == 0) {
 					convertView.setBackgroundDrawable(mItemLightBackground);
 				} else {
 					convertView.setBackgroundDrawable(mItemDarkBackground);
 				}
 			}
 
 			return convertView;
 		}
 	}
 }
