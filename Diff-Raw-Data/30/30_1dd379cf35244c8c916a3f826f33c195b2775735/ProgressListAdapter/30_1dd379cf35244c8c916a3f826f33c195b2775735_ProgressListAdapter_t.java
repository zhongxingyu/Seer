 package com.masenf.core.progress;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.masenf.core.DrawingItemList;
 import com.masenf.core.adapters.ItemDrawingListAdapter;
 import com.masenf.core.async.callbacks.BaseCallback;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 
 public class ProgressListAdapter extends ItemDrawingListAdapter<DrawingItemList<ProgressItem>> {
 
 	private static final String TAG = "ProgressListAdapter";
 	private HashMap<String,ProgressItem> ptags = new HashMap<String,ProgressItem>();
 	private BaseCallback completeCallback = null;
 	private ProgressItem freshItem = new ProgressItem();
 	public ProgressListAdapter(Context ctx) {
 		super(ctx);
 		items = new DrawingItemList<ProgressItem>();
 		completeCallback = new BaseCallback() {
 			@Override
 			public void notifyComplete(boolean success, String tag) {
 				Log.v(TAG,"Progress " + tag + " has completed, removing it from list");
 				expireProgress(tag);
 			}
 		};
 		items.add(freshItem);
 	}
 	public ProgressCallback getCallbackByTag(String tag) {
 		if (ptags.containsKey(tag)) {
 			return ptags.get(tag);
 		}
 		return null;
 	}
 	public ProgressCallback newProgress(String tag) {
 		return addProgressItem(tag, null);
 	}
 	public void expireProgress(String tag) {
 		// wipe out a progress tag, remove all refs
 		if (ptags.containsKey(tag)) {
 			ProgressItem p = ptags.get(tag);
 			items.remove(p);
 			ptags.remove(tag);
 			p = null;
 			notifyDataSetChanged();
 		}
 	}
 	@Override
 	public Bundle saveAdapterState() {
		// TODO: consider removing save/restore functions
 		Bundle s = new Bundle();
 		if (this.getCount() > 0) {
 			ArrayList<Bundle> itemState = new ArrayList<Bundle>(getCount());
 			for (ProgressItem pi : items) {
 				if (pi.isComplete() == false)			// only persist in progress items
 					itemState.add(pi.saveItemState());
 			}
 			s.putSerializable("item_state", itemState);
 		}
 		return s;
 	}
 	@Override
 	public void restoreAdapterState(Bundle s) {
 		if (s != null && s.containsKey("item_state")) {
 			ArrayList<Bundle> itemState = (ArrayList<Bundle>) s.getSerializable("item_state");
 			for (Bundle b : itemState) {
 				addProgressItem(null, b);
 			}
 		}
 	}
 	private ProgressItem addProgressItem(String tag, Bundle state) {
 		Log.d(TAG,"addProgressItem()");
 		ProgressItem p = items.get(items.size() - 1);
 		p.setCallback(completeCallback);
 		if (tag != null)
 			p.setTag(tag);
 		else
 			tag = p.getTag();
 		ptags.put(tag, p);
 		
 		items.add(new ProgressItem());
 		notifyDataSetChanged();
 		Log.d(TAG,"addProgressItem() notifiedDataSetChanged");
 		return p;
 	}
 
 }
