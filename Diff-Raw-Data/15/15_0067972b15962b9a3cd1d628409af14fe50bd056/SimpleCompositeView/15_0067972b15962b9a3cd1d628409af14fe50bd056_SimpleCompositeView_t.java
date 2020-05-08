 package andex.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import andex.Callback;
 import andex.Utils;
 import andex.view.SimpleListView.SimpleInfoListViewAdapter;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 /**
  * The base composite view. 
  * @author 
  *
  */
 public abstract class SimpleCompositeView {
 	
 	public static final int STATE_POSITIVE = 1;
 	public static final int STATE_NEGATIVE = 2;
 	
 	public static int translateState(int value, int posTarget, int negTarget) {
 		return (Integer)Utils.quaterLogic(value
 				, posTarget, SimpleCompositeView.STATE_POSITIVE
 				, negTarget, SimpleCompositeView.STATE_NEGATIVE);
 	}
 	
 	protected Context ctx;
 	protected AbsListView absListView;
 	protected String idkey = "k_id";
 	protected String statekey = "k_state";
 	
 	// keys to extract text line 1 and text line 2 for each item. 
 	protected String[] keys = new String[] { "k1", "k2", "k3"};
 	
 	// Data of rows with k1 and k2.
 	protected List<Map<String, ?>> data;
 	
 	// Label that displayed while no data for this View.
 	public String defaultLabel = "[N/A]";
 	
 	// Adapter for data of view.
 	private ListAdapter adapter;
 
 	public SimpleCompositeView(Context context, AbsListView alv) {
 		this.ctx = context;
 		this.absListView = alv;
 		data = new ArrayList();
 		adapter = getAdapter(context);
 	}
 	
 	public SimpleCompositeView(Context context, ListView lv, String defaultLabel) {
 		this(context, lv);
 		this.defaultLabel = defaultLabel;
 		adapter = getAdapter(context);
 	}
 	
 	/**
 	 * Inject adapter by overriding this method.
 	 * This method will be called while constructing the view.
 	 * @param context
 	 * @return
 	 */
 	protected abstract ListAdapter getAdapter(Context context);
 	
 	/**
 	 * 添加一个空（null）项
 	 * @param id
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object id) {
 		return addItem(id, 0, null);
 	}
 
 	/**
 	 * 添加一个列表项。
 	 * @param values 至少有两个值，分别表示title和description.
 	 *            
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object[] values) {
 		if (values == null || values.length != 2) {
 			throw new IllegalArgumentException();
 		}
 		return addItem(values[0], values[1]);
 	}
 
 	/**
 	 * Add new item 
 	 * @param id
 	 * @param values
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object id, Object[] values) {
 		return addItem(id, 0 , values);
 	}
 
 	/**
 	 * Add new list item, title and description.
 	 * 
 	 * @param
 	 * @param
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object title, Object desc) {
 		return addItem(null, 0, title, desc);
 	}
 	
 	/**
 	 * Add new item with id, title and description.
 	 * @param id
 	 * @param title
 	 * @param desc
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object id, Object title, Object desc) {
 		return addItem(id, 0, title, desc);
 	}
 	
 	/**
 	 * title和desc都是资源ID。
 	 * @param id
 	 * @param title
 	 * @param desc
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object id, int title, int desc) {
 		return addItem(id, 0, title, desc);
 	}
 	
 	/**
 	 * Add new item with title, description, state.
 	 * @param title
 	 * @param desc
 	 * @param state
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object title, Object desc, int state) {
 		return addItem(null, state, title, desc);
 	}
 	
 	/**
 	 * Add new item with id, title, description and state.
 	 * @param id
 	 * @param state
 	 * @param values
 	 * @return
 	 */
 	public SimpleCompositeView addItem(Object id, int state, Object... values) {
 		Map<String, Object> itemData = new HashMap<String, Object>();
 		if (id != null) {
 			itemData.put(idkey, id);
 		}
 		itemData.put(statekey, state);
 		if (values != null) {
 			if (values.length > 0)
 				itemData.put(keys[0], values[0]);
 			if (values.length > 1)
 				itemData.put(keys[1], values[1]);
 			if (values.length > 2)
 				itemData.put(keys[2], values[2]);
 		}
 		data.add(itemData);
 		return this;
 	}
 	
 	/**
 	 * Add all map entries, key as id, value as title and description.
 	 * @param m
 	 * @return
 	 */
 	public SimpleCompositeView addAllItems(Map m) {
 		Iterator it = m.keySet().iterator();
 		while (it.hasNext()) {
 			Object k = it.next();
 			Object v = m.get(k);
 			Log.v("", k + "=" + v);
 			if (v != null) {
 				addItem(k, v, v);
 			}
 		}
 		return this;
 	}
 	
 	/**
 	 * Add all Bundle entries, key as id and title, value as description.
 	 * @param bundle
 	 * @return
 	 */
 	public SimpleCompositeView addAllItems(Bundle bundle) {
 		Iterator it = bundle.keySet().iterator();
 		while (it.hasNext()) {
 			Object k = it.next();
 			Object v = bundle.get(k.toString());
 			Log.v("", k + "=" + v);
 			if (v != null) {
 				addItem(k, k, v);
 			}
 		}
 		return this;
 	}
 	
 	/**
 	 * 
 	 * @param data
 	 * @param k1
 	 * @param k2
 	 * @return
 	 */
 	public SimpleCompositeView addAllItems(List<Map> data, Object k1, Object k2) {
 		for (int i = 0; i < data.size(); i++) {
 			Map m = data.get(i);
 			addItem(m.get(k1), m.get(k2), null);
 		}
 		return this;
 	}
 
 
 	/**
 	 * Remove item by item ID.
 	 * @param id
 	 * @return
 	 */
 	public boolean removeItem(Object id) {
 		if (id == null) {
 			return false;
 		}
 		for (int i = 0; i < data.size(); i++) {
 			if (data.get(i).get(idkey) == id) {
 				data.remove(i);
 				this.render();
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public Map<String, ?> getItem(Object id) {
 		if (id == null) {
 			return null;
 		}
 		for (int i = 0; i < data.size(); i++) {
 			if (data.get(i).get(idkey) == id) {
 				return data.get(i);
 			}
 		}
 		return null;
 	}
 	
 //	public View getItemView() {
 //		this.absListView.getchi
 //	}
 	
 	public int indexOf(Object id) {
 		if (id == null) {
 			return -1;
 		}
 		for (int i = 0; i < data.size(); i++) {
 			if (data.get(i).get(idkey) == id) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Clear all data.
 	 */
 	public void clear() {
 		data.clear();
 		// render here will case wrong displaying.
 		// this.render();
 	}
 
 	/**
 	 * Render to display all data for this view.
 	 */
 	public void render() {
 		Log.v("andex", String.format("render() with %d rows", data == null ? 0 : data.size()));
 		if (this.absListView == null) {
 			throw new RuntimeException("The composite view was not init correctly, the abstract list view object is Null");
 		}
 		if (data == null || data.isEmpty()) {
 			Log.v("andex", "  No data for ListView, show info");
 			((AdapterView)this.absListView).setAdapter(new SimpleInfoListViewAdapter(ctx, defaultLabel));
 		}
 		else {
 			Log.v("andex", String.format("  Rnder %d items", adapter.getCount()));
 			((AdapterView)this.absListView).setAdapter(adapter);
 		}
 	}
 	
 	/**
 	 * Set handler for item clicked event.
 	 * @param handler Invoked with ID and displayed contents if ID exists.
 	 */
 	public void onItemClick(final Callback handler) {
 		this.absListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int pos, long itemid) {
 				Log.v("andex", "  Item at position " + pos + " was clicked");
 				view.setSelected(true); // TODO
 				handleClickEvent(pos, handler);
 			}
 		});
 	}
 	
 	/**
 	 * Set handler for item long clicked event.
 	 * @param handler Invoked with ID and displayed contents if ID exists.
 	 */
 	public void onItemLongClick(final Callback handler) {
 		this.absListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long itemid) {
 				Log.v("andex", "  Item at position " + pos + " was long clicked");
 				handleClickEvent(pos, handler);
 				return true;
 			}
 		});
 	}
 	
 	private void handleClickEvent(int pos, Callback handler) {
 		if(pos >= data.size()) {
 			return;
 		}
 		Map item = data.get(pos);
 		if (item == null || item.isEmpty()) {
 			return;
 		}
 		Object bizid = item.get(idkey);
 		if (bizid == null) {
 			Log.v("andex", "  No business ID there, callback without anything");
 			handler.invoke();
 		}
 		else {
			Log.v("andex", String.format("  Select business ID %s [%s]", bizid, bizid.getClass()));
 			handler.invoke(bizid);
 			handler.invoke(bizid, item.get(keys[0]), item.get(keys[1]));
 		}
 	}
 
 	/**
 	 * Get inner data as a list with Maps inside.
 	 * @return
 	 */
 	public List<Map<String, ?>> getData() {
 		return data;
 	}
 	
 }
