 package com.anddevbg.andlib.adapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.anddevbg.andlib.log.LogWrapper;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Looper;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseAdapter;
 
 /**
  * 
  * Abstract adapter with useful methods for list views. You can register this
  * adapter for scroll state change in list view. This class is thread safe.
  * 
  * @author anddevbg@gmail.com
  * 
  */
 public abstract class BaseAdapterWrapper<T> extends BaseAdapter implements OnScrollListener {
 
 	private final Object mLock = new Object();
 
 	private int mResId;
 	private LayoutInflater mInflater;
 
 	private Context mContext;
 	private Handler mHandler;
 
 	private boolean mNotifyOnSetChanged;
 
 	private List<T> mItems;
 
 	private boolean mIsScrolling;
 
 	public BaseAdapterWrapper(Context context, int resId) {
 		mContext = context;
 
 		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		mResId = resId;
 
 		mItems = new ArrayList<T>();
 
 		mNotifyOnSetChanged = true;
 
 		mIsScrolling = false;
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		mIsScrolling = !(scrollState == OnScrollListener.SCROLL_STATE_IDLE);
 	}
 
 	/**
 	 * This will notify listeners for set changed if is set to true.
 	 * 
 	 * @param value
 	 */
 	public void notifyOnSetChanged(boolean value) {
 		mNotifyOnSetChanged = value;
 	}
 
 	/**
 	 * Add single item in adapter. Notify for set changed if
 	 * {@link #notifyOnSetChanged(boolean)} is set to true
 	 * 
 	 * @param item
 	 */
 	public void add(T item) {
 		synchronized (mLock) {
 			mItems.add(item);
 		}
 
 		if (mNotifyOnSetChanged) {
 			notifyDataSetChanged();
 		}
 	}
 
 	/**
 	 * Remove single item in adapter. Notify for set changed if
 	 * {@link #notifyOnSetChanged(boolean)} is set to true
 	 * 
 	 * @param item
 	 */
 	public void remove(T item) {
 		synchronized (mLock) {
 			mItems.remove(item);
 		}
 
 		if (mNotifyOnSetChanged) {
 			notifyDataSetChanged();
 		}
 	}
 
 	/**
	 * Convenience method to add list without notifying for each element.
 	 * 
 	 * @param items
 	 */
 	public void addAll(List<T> items) {
 		synchronized (mLock) {
 			mItems.addAll(items);
 		}
 
 		if (mNotifyOnSetChanged) {
 			notifyDataSetChanged();
 		}
 	}
 
 	/**
	 * Convenience method to remove list without notifying for each element.
 	 * 
 	 * @param items
 	 */
 	public void removeAll(List<T> items) {
 		synchronized (mLock) {
 			mItems.removeAll(items);
 		}
 
 		if (mNotifyOnSetChanged) {
 			notifyDataSetChanged();
 		}
 	}
 
 	/**
 	 * Returns count of elements inside adapter.
 	 */
 	@Override
 	public int getCount() {
 		synchronized (mLock) {
 			return mItems.size();
 		}
 	}
 
 	@Override
 	public T getItem(int position) {
 		synchronized (mLock) {
 			return mItems.get(position);
 		}
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = ensureView(position, convertView, parent);
 		bindView(position, row, getItem(position));
 		return row;
 	}
 
 	private View ensureView(int position, View convertView, ViewGroup parent) {
 		View v;
 		if (convertView == null) {
 			LogWrapper.d(this, "Creating view for position " + position);
 			v = newView(position, parent);
 		} else {
 			LogWrapper.d(this, "Reusing view for position " + position);
 			v = convertView;
 		}
 
 		return v;
 	}
 
 	private View newView(int position, ViewGroup parent) {
 		return mInflater.inflate(mResId, parent, false);
 	}
 
 	/**
 	 * Inside this method you are responsible to bind the view.
 	 * 
 	 * @param position
 	 * @param view
 	 *            to bind
 	 * @param data
 	 */
 	protected abstract void bindView(int position, View view, T data);
 
 	/**
 	 * @return false if adapter is not registered as scroll listener, correct
 	 *         state otherwise.
 	 */
 	protected boolean isScrolling() {
 		return mIsScrolling;
 	}
 
 	protected Context getContext() {
 		return mContext;
 	}
 
 	protected Handler getUIHandler() {
 		prepareUIHandler();
 		return mHandler;
 	}
 
 	protected void runOnUiThread(Runnable runnable) {
 		prepareUIHandler();
 		mHandler.post(runnable);
 	}
 
 	private void prepareUIHandler() {
 		if (mHandler == null) {
 			mHandler = new Handler(Looper.getMainLooper());
 		}
 	}
 }
