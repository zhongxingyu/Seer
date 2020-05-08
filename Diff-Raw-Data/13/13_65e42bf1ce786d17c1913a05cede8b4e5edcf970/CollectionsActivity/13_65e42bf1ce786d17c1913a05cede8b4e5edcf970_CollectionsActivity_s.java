 package net.jsiq.marketing.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.jsiq.marketing.R;
 import net.jsiq.marketing.adapter.CollectionAdapter;
 import net.jsiq.marketing.db.CollectionDBHelper;
 import net.jsiq.marketing.model.CollectionItem;
 import net.jsiq.marketing.util.MessageToast;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CollectionsActivity extends RightMenuBaseActivity implements
 		OnItemClickListener {
 
 	private List<CollectionItem> collections;
 	private CollectionDBHelper DBHelper;
 	private CollectionAdapter adapter;
 	private ListView listview;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.collection_listview);
		DBHelper = new CollectionDBHelper(this);
		DBHelper.open();
 		collections = new ArrayList<CollectionItem>();
 		listview = (ListView) findViewById(android.R.id.list);
 		listview.setEmptyView(findViewById(android.R.id.empty));
 		adapter = new CollectionAdapter(this, collections);
 		listview.setAdapter(adapter);
 		listview.setOnItemClickListener(this);
 		registerForContextMenu(listview);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		adapter.clear();
 		adapter.addAll(DBHelper.queryAll());
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		menu.setHeaderTitle(R.string.options);
 		inflater.inflate(R.menu.context_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		boolean success = false;
 		int pos = (int) adapter.getItemId(menuInfo.position);
 		switch (item.getItemId()) {
 		case R.id.viewCollection:
 			startContentDisplayActivityByCollection(collections.get(pos));
 			break;
 		case R.id.delete:
 			success = DBHelper.delete(collections.get(pos).getContentId());
 			if (success) {
 				collections.remove(pos);
 				adapter.notifyDataSetChanged();
 				MessageToast.showText(this, R.string.operatSucceed);
 			} else {
 				MessageToast.showText(this, R.string.operatFailed);
 			}
 			break;
 		case R.id.deleteAll:
 			success = DBHelper.deleteAll();
 			if (success) {
 				collections.clear();
 				adapter.notifyDataSetChanged();
 				MessageToast.showText(this, R.string.operatSucceed);
 			} else {
 				MessageToast.showText(this, R.string.operatFailed);
 			}
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 	private void startContentDisplayActivityByCollection(
 			CollectionItem collection) {
 		Intent i = new Intent("android.intent.action.ContentDisplayActivity");
 		i.putExtra(ContentDisplayActivity.CONTENT_INFO, new String[] {
 				collection.getContentId() + "", collection.getContentTitle(),
 				collection.getContentSummary() });
 		startActivity(i);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		DBHelper.close();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		startContentDisplayActivityByCollection(collections.get(arg2));
 	}
 
 	@Override
 	protected void setTitle(TextView title) {
 		title.setText(R.string.collections);
 	}
 
 }
