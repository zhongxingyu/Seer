 package org.kvj.bravo7.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.kvj.bravo7.ApplicationContext;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class WidgetList extends ListFragment {
 
 	private static final String TAG = "Widget list";
 	ApplicationContext app = null;
 
 	public WidgetList(ApplicationContext app) {
 		this.app = app;
 	}
 
 	public interface ClickListener {
 		public void click(WidgetInfo info);
 	}
 
 	public class WidgetInfo {
 		public int id;
 		public String type;
 
 		@Override
 		public String toString() {
 			return app.getWidgetConfig(id).getString("name", "Widget " + id);
 		}
 	}
 
 	ClickListener clickListener = null;
 	List<WidgetInfo> list = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		reloadData();
 	}
 
 	public void reloadData() {
 		Map<Integer, String> data = app.getWidgetConfigs(null);
 		// Log.i(TAG, "Widgets: " + data.size());
 		list = new ArrayList<WidgetList.WidgetInfo>();
 		for (Integer id : data.keySet()) {
 			WidgetInfo info = new WidgetInfo();
 			info.id = id;
 			info.type = data.get(id);
 			list.add(info);
 		}
 		setListAdapter(new ArrayAdapter<WidgetInfo>(getActivity(),
				android.R.layout.simple_list_item_1, list));
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		reloadData();
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		if (null != clickListener) {
 			clickListener.click(list.get(position));
 		}
 
 	}
 
 	public void setClickListener(ClickListener clickListener) {
 		this.clickListener = clickListener;
 	}
 
 }
