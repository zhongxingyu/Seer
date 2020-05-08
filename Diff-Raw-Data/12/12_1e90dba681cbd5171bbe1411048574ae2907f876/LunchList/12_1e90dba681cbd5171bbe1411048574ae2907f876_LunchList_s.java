 package csci498.thevoiceless.lunchlist;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class LunchList extends FragmentActivity implements LunchFragment.onRestaurantListener
 {
 	public final static String RESTAURANT_ID_KEY = "csci498.thevoiceless.RESTAURANT_ID";
 	private final static int LONG_PRESS_ACTIONS = 1;
 	private ListView list;
 	private long longPressedRestaurant;
 	
 	private LunchFragment lunch;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_lunch_list);
 		
 		setDataMembers();
 		setListeners();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		new MenuInflater(this).inflate(R.menu.option, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		if (item.getItemId() == R.id.add)
 		{
 			startActivity(new Intent(LunchList.this, DetailForm.class));
 			return true;
 		}
 		else if (item.getItemId() == R.id.prefs)
 		{
 			startActivity(new Intent(this, EditPreferences.class));
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onRestaurantSelected(long id)
 	{
 		// Check if using two-pane layout
 		if (findViewById(R.id.detailsPane) == null)
 		{
 			Intent i = new Intent(LunchList.this, DetailForm.class);
 			i.putExtra(RESTAURANT_ID_KEY, String.valueOf(id));
 			startActivity(i);
 		}
 		else
 		{
 			FragmentManager fragManager = getSupportFragmentManager();
 			DetailFragment details = (DetailFragment) fragManager.findFragmentById(R.id.detailsPane);
 			
 			if (details == null)
 			{
 				details = DetailFragment.newInstance(id);
 				FragmentTransaction transaction = fragManager.beginTransaction();
 				transaction.add(R.id.detailsPane, details)
 						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
 						.addToBackStack(null)
 						.commit();
 			}
 			else
 			{
 				details.loadRestaurantById(String.valueOf(id));
 			}
 		}
 	}
 		
 	private void setDataMembers()
	{
 		lunch = (LunchFragment) getSupportFragmentManager().findFragmentById(R.id.lunchFragment);
 		lunch.setOnRestaurantListener(this);
 		list = lunch.getListView();
 	}
 	
 	private void setListeners()
 	{
 		// Display dialog after long-pressing on a list item
 		list.setOnItemLongClickListener(new OnItemLongClickListener()
 		{
 			@Override
 			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
 			{
 				longPressedRestaurant = id;
 				// showDialog automatically calls onCreateDialog
 				showDialog(LONG_PRESS_ACTIONS);
 				return true;
 			}
 		});
 	}
 	
 	@Override
 	public Dialog onCreateDialog(int id)
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(LunchList.this);
 		
 		if (id == LONG_PRESS_ACTIONS)
 		{
 			builder.setItems(R.array.longpress_actions,
 					new DialogInterface.OnClickListener()
 					{
 						public void onClick(DialogInterface dialog, int whichIndexWasPressed)
 						{
 							switch (whichIndexWasPressed)
 							{
 								// Edit
 								case 0:
 									onRestaurantSelected(longPressedRestaurant);
 									break;
 								// Delete
 								case 1:
 									if (lunch.getDatabaseHelper().delete(String.valueOf(longPressedRestaurant)))
 									{
 										Toast.makeText(LunchList.this, R.string.delete_success, Toast.LENGTH_LONG).show();
 										lunch.getCursor().requery();
 									}
 									else
 									{
 										Toast.makeText(LunchList.this, R.string.delete_failure, Toast.LENGTH_LONG).show();
 									}
 									break;
 							}
 						}
 					});
 		}
 		
 		return builder.create();
 	}
 }
