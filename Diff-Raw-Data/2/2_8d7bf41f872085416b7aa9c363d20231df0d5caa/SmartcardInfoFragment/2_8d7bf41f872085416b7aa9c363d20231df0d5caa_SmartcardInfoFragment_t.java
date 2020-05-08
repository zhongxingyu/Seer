 package com.dbstar.app.settings.smartcard;
 
 import com.dbstar.R;
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.app.base.BaseFragment;
 import com.dbstar.app.base.FragmentObserver;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.ProductItem;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SmartcardInfoFragment extends GDSmartcardFragment {
 	private static final String TAG = "SmartcardInfoFragment";
 
 	TextView mSmartcardNumberView, mSmartcardStateView, mSmartcardVersionView;
	TextView[] mEignevalueIDView = new TextView[10];
 	ListView mAthorizationInfoView;
 	ListAdapter mAdapter;
 	String mSmartcardSN, mSmartcardStateStr, mSmartcardVersion;
 	String[] mIDValues;
 	ProductItem[] mProductItems;
 	int mSmartcardState = GDCommon.SMARTCARD_STATE_NONE;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.smartcard_info_view, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		initializeView();
 	}
 
 	// Request data at this point
 	public void serviceStart() {
 		Log.d(TAG, "=== service is started ===");
 		
 		mSmartcardState = mService.getSmartcardState();
 		updateSmartcardState();
 
 		if (mSmartcardState == GDCommon.SMARTCARD_STATE_INSERTED) {
 			getSmartcardData();
 		}
 	}
 
 	void getSmartcardData() {
 		mSmartcardEngine.getSmartcardInfo(this, DbstarServiceApi.CMD_DRM_SC_SN_READ);
 		mSmartcardEngine.getSmartcardInfo(this, DbstarServiceApi.CMD_DRMLIB_VER_READ);
 		mSmartcardEngine.getSmartcardInfo(this,
 				DbstarServiceApi.CMD_DRM_SC_EIGENVALUE_READ);
 		mSmartcardEngine.getSmartcardInfo(this,
 				DbstarServiceApi.CMD_DRM_ENTITLEINFO_READ);
 	}
 
 	// Receive data at this point
 	public void updateData(FragmentObserver observer, int type, Object key,
 			Object data) {
 
 		if (observer != this || data == null)
 			return;
 
 		int requestType = (Integer) key;
 		if (requestType == DbstarServiceApi.CMD_DRM_SC_SN_READ) {
 			mSmartcardSN = (String) data;
 
 			updateSmartcardSN();
 		} else if (requestType == DbstarServiceApi.CMD_DRMLIB_VER_READ) {
 			mSmartcardVersion = (String) data;
 			updateSmartcardVersion();
 		} else if (requestType == DbstarServiceApi.CMD_DRM_SC_EIGENVALUE_READ) {
 			String[] ids = (String[]) data;
 			if (ids.length > mEignevalueIDView.length) {
 				Log.e(TAG, "Fata error: smartcard eignevalue is wrong!");
 				return;
 			}
 
 			mIDValues = new String[ids.length];
 			for (int i = 0; i < ids.length; i++) {
 				mIDValues[i] = ids[i];
 			}
 
 			updateSmartcardIds();
 		} else if (requestType == DbstarServiceApi.CMD_DRM_ENTITLEINFO_READ) {
 			mProductItems = (ProductItem[]) data;
 
 			updateProducts();
 		}
 
 	}
 
 	// handle event at this point
 	public void notifyEvent(FragmentObserver observer, int type, Object event) {
 		if (observer != this)
 			return;
 
 		if (type == EventData.EVENT_SMARTCARD_STATUS) {
 			EventData.SmartcardStatus status = (EventData.SmartcardStatus) event;
 			mSmartcardState = status.State;
 
 			updateSmartcardState();
 		}
 	}
 
 	void updateSmartcardSN() {
 		mSmartcardNumberView.setText(mSmartcardSN);
 	}
 
 	void updateSmartcardVersion() {
 		mSmartcardVersionView.setText(mSmartcardVersion);
 	}
 
 	void updateSmartcardState() {
 		if (mSmartcardState == GDCommon.SMARTCARD_STATE_REMOVING) {
 			mSmartcardStateView.setText(R.string.smarcard_state_not_in);
 			clearSmartcardData();
 		} else if (mSmartcardState == GDCommon.SMARTCARD_STATE_INVALID) {
 			mSmartcardStateView.setText(R.string.smarcard_state_invalid);
 			clearSmartcardData();
 		} else if (mSmartcardState == GDCommon.SMARTCARD_STATE_INSERTED) {
 			mSmartcardStateView.setText(R.string.smarcard_state_normal);
 			getSmartcardData();
 		}
 
 	}
 
 	void updateSmartcardIds() {
 		if (mIDValues == null || mIDValues.length == 0)
 			return;
 
 		for (int i = 0; i < mIDValues.length; i++) {
 			mEignevalueIDView[i].setText(mIDValues[i]);
 		}
 	}
 
 	void updateProducts() {
 		if (mProductItems == null && mProductItems.length == 0)
 			return;
 
 		mAdapter.setDataSet(mProductItems);
 		mAdapter.notifyDataSetChanged();
 	}
 
 	void clearSmartcardData() {
 		mSmartcardNumberView.setText("");
 		mSmartcardVersionView.setText("");
 		if (mIDValues != null) {
 			for (int i = 0; i < mIDValues.length; i++) {
 				mEignevalueIDView[i].setText("");
 			}
 		}
 
 		mAdapter.setDataSet(null);
 		mAdapter.notifyDataSetChanged();
 	}
 
 	void initializeView() {
 		mSmartcardNumberView = (TextView) mActivity
 				.findViewById(R.id.smartcard_number);
 		mSmartcardVersionView = (TextView) mActivity
 				.findViewById(R.id.smartcard_version);
 		mSmartcardStateView = (TextView) mActivity
 				.findViewById(R.id.smartcard_state);
 
 		int[] ids = new int[] { R.id.eignevlaue_id1, R.id.eignevlaue_id2,
 				R.id.eignevlaue_id3, R.id.eignevlaue_id4, R.id.eignevlaue_id5,
 				R.id.eignevlaue_id6 };
 
 		for (int i = 0; i < ids.length; i++) {
 			mEignevalueIDView[i] = (TextView) mActivity.findViewById(ids[i]);
 		}
 
 		mAthorizationInfoView = (ListView) mActivity
 				.findViewById(R.id.athorization_info);
 		mAdapter = new ListAdapter(mActivity);
 		mAthorizationInfoView.setAdapter(mAdapter);
 	}
 
 	private class ListAdapter extends BaseAdapter {
 
 		public class ViewHolder {
 			TextView operatorId;
 			TextView productId;
 			TextView startTime;
 			TextView endTime;
 			TextView limitCount;
 		}
 
 		private ProductItem[] mDataSet = null;
 
 		public ListAdapter(Context context) {
 		}
 
 		public void setDataSet(ProductItem[] dataSet) {
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
 				convertView = inflater.inflate(R.layout.product_item, parent,
 						false);
 
 				holder = new ViewHolder();
 				holder.operatorId = (TextView) convertView
 						.findViewById(R.id.operator_id);
 				holder.productId = (TextView) convertView
 						.findViewById(R.id.product_id);
 
 				holder.startTime = (TextView) convertView
 						.findViewById(R.id.start_time);
 
 				holder.endTime = (TextView) convertView
 						.findViewById(R.id.end_time);
 
 				holder.limitCount = (TextView) convertView
 						.findViewById(R.id.limit_count);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			holder.operatorId.setText(mDataSet[position].OperatorID);
 			holder.productId.setText(mDataSet[position].ProductID);
 			holder.startTime.setText(mDataSet[position].StartTime);
 			holder.endTime.setText(mDataSet[position].EndTime);
 			holder.limitCount.setText(mDataSet[position].LimitCount);
 
 			return convertView;
 		}
 	}
 }
