 package edu.rit.smartFridge;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.rit.smartFridge.model.ShoppingList;
 import edu.rit.smartFridge.util.Connector;
 import edu.rit.smartFridge.util.DataConnect;
 
 public class ShoppingListActivity extends ListActivity
 {
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		final Context context = this;
 
 		// get the connecter
 		DataConnect connecter = Connector.getInstance();
 
 		// get the shopping lists
 		final List<ShoppingList> lists = connecter.getLists();
 
 		// copy the list names into another list for display
 		List<String> listNames = new ArrayList<String>();
 		String label;
 		if (lists != null && lists.size() != 0)
 		{
 			for (ShoppingList l : lists)
 			{
 				if (l.isAutoGen())
 				{
 					label = "[A]\t" + l.getName();
 				}
 				else
 				{
 					label = "\t\t" + l.getName();
 				}
 
 				listNames.add(label);
 			}
 		} 
 
 		if (listNames.size() == 0)
 		{
 			// no lists to display
 			listNames.add("No shopping lists to display.");
 		}
 
 		// display the list
 		String[] a = new String[listNames.size()];
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
 				listNames.toArray(a)));
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 
 		// make a listener
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id)
 			{
 				if (lists != null)
 				{
 					// populate the shopping list's items
 					Intent i = new Intent().setClass(context,
 							ItemListActivity.class).putExtra(
 									getString(R.string.current_list),
 									lists.get(position).getID());
 					context.startActivity(i);
 				}
 			}
 		});
 		lv.setLongClickable(true);
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View v,
 					int position, long id)
 			{
 				Toast.makeText(getApplicationContext(),
 						((TextView) v).getText(), Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 
 		switch (item.getItemId())
 		{
 			case R.id.refresh:
 				refresh();
 		}
 
 		Intent i = new Intent().setClass(getBaseContext(), SmartFridgeActivity.class);
 		i.addFlags(268435456); //FLAG_ACTIVITY_NEW_TASK
 		i.getExtras().putInt(getString(R.string.curr_tab), 1); // set the shopping list activity active
 		getBaseContext().startActivity(i);
 
 		return true;
 	}
 
 	public void refresh()
 	{
 		DataConnect connecter = Connector.getInstance();
 		connecter.refreshLists();
 	}
 }
