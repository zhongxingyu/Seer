 package com.kaist.crescendo.activity;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.kaist.crescendo.R;
 import com.kaist.crescendo.data.PlanData;
 import com.kaist.crescendo.data.PlanListAdapter;
 import com.kaist.crescendo.utils.MyPref;
 import com.kaist.crescendo.utils.MyStaticValue;
 
 public class PlanListActivity extends UpdateActivity {
 	
 	private static PlanListAdapter adapter;
 	private ListView listView;
 	private ArrayList<PlanData> planArrayList;
 	
 	private final int UPDATE_ID = Menu.FIRST;
 	private final int DELETE_ID = Menu.FIRST + 1;
 	private final int SETDEFAULT_ID = Menu.FIRST + 2;	
 	
 	public final static PlanListAdapter getAdapterInstance() 
 	{
 		return adapter;
 	}
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		menu.setHeaderTitle(R.string.str_plan_context_menu);
 		
 		menu.add(0, UPDATE_ID, 0, R.string.str_modify_plan2);
 		menu.add(0, DELETE_ID, 0, R.string.str_delete_plan);
 		menu.add(0, SETDEFAULT_ID, 0, R.string.str_set_default_plan);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo menuInfo;
 		menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		int index = 0;
 		boolean result = false;
 		
 		index = menuInfo.position;
 		PlanData plan = (PlanData) adapter.getItem(index);
 		
 		switch(item.getItemId())
 		{
 		case DELETE_ID:
 			
 			
 			
 			
 			if(adapter.getCount() > 1 && plan.isSelected == true)
 			{
 				Toast.makeText(this, R.string.str_err_defaultplan, Toast.LENGTH_LONG).show();
 				return true;
 			}
 				
 			result = deletePlan(((PlanData) adapter.getItem(index)).uId);
 			if(result == true)
 			{
 				//delete alarm info from alarmSerivce
 				try {
 					getServiceInterface().delAlarm(((PlanData)adapter.getItem(index)).uId);
 				} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				adapter.clearAllItems();
 				String ret = getPlanList(planArrayList);
 				if(ret.equals("good")) {
 					for(int i = 0 ; i < planArrayList.size() ; i++) {
 						adapter.addItem(planArrayList.get(i));
 					}
 					adapter.notifyDataSetChanged();
 					PlanData defaultP =  adapter.getDefaultPlan();
 					updateDefaultPlan(defaultP);
 				}
 			}
 			return true;
 		case UPDATE_ID:
 			index = menuInfo.position;
 			//result = updatePlan(((PlanData) adapter.getItem(index)).uId);
 			// ADD NEW PLAN
 			Intent intent = new Intent();
 			intent.putExtra(MyStaticValue.MODE, MyStaticValue.MODE_UPDATE);
 			intent.putExtra(MyStaticValue.NUMBER, index);
 			
 			startActivityForResult(intent.setClass(getApplicationContext(), PlanEditorActivity.class), MyStaticValue.REQUESTCODE_UPDATEPLAN);
 			return true;
 		case SETDEFAULT_ID:
 			adapter.clearSelectedPlan();
 			plan.isSelected = true;
 			
 			if(updatePlan(plan) == true) {
 				
 			}
 			
 			updateDefaultPlan(plan);
 			
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	private void updateDefaultPlan(PlanData plan)
 	{
 		/* save info. to preference */
 		
 		SharedPreferences prefs = getSharedPreferences(MyPref.myPref, MODE_MULTI_PROCESS);
 		SharedPreferences.Editor editor = prefs.edit();
 		
 		if(plan == null)
 			editor.putInt(MyPref.MY_AVATA_UID, 0);
 		else
 			editor.putInt(MyPref.MY_AVATA_UID, plan.uId);
 		editor.commit();
 		
 		if(plan == null)
 			editor.putInt(MyPref.MY_AVATA_TYPE, 0);
 		else
 			editor.putInt(MyPref.MY_AVATA_TYPE, plan.type);
 		editor.commit();
 		
 		if(plan == null)
 			editor.putString(MyPref.MY_AVATA_TITLE, "");
 		else
 			editor.putString(MyPref.MY_AVATA_TITLE, plan.title);
 		editor.commit();
 		
 		if(plan == null)
 			editor.putInt(MyPref.MY_AVATA_PROGRESS, 0);
 		else {
 			int progress;
 			
 			if(plan.hItem == null || plan.hItem.size() == 0)
 				progress = 0;
 			else
 				progress = (int) (100*(plan.targetValue - plan.hItem.get(plan.hItem.size()-1).value) / (plan.targetValue - plan.initValue));
			editor.putInt(MyPref.MY_AVATA_PROGRESS, progress );
 		}
 		editor.commit();
 		
 		
 		sendBroadCasetIntent();
 		adapter.notifyDataSetChanged();
 	}
 	
 	private void sendBroadCasetIntent()
 	{
 		Intent intent = new Intent(MyStaticValue.ACTION_UPDATEWALLPAPER);
 
 		sendBroadcast(intent);
 	}
 	
 	OnItemClickListener mListClickListener = new OnItemClickListener() {
 		
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			Intent intent = new Intent();
 			intent.putExtra(MyStaticValue.MODE, MyStaticValue.MODE_VIEW);
 			intent.putExtra(MyStaticValue.NUMBER, position);
 			
 			startActivityForResult(intent.setClass(getApplicationContext(), PlanViewActivity.class), MyStaticValue.REQUESTCODE_VIEWPLAN);
 			
 		}
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_planslist);
 		setTitle(R.string.str_managing_plans);
 		
 		
 		listView = (ListView) findViewById(R.id.plans_list);
 		listView.setOnItemClickListener(mListClickListener);
 		
 		planArrayList = new ArrayList<PlanData>();		
 		
 		OnClickListener mClickListener = new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// ADD NEW PLAN
 				Intent intent = new Intent();
 				intent.putExtra(MyStaticValue.MODE, MyStaticValue.MODE_NEW);
 				
 				startActivityForResult(intent.setClass(getApplicationContext(), PlanEditorActivity.class), MyStaticValue.REQUESTCODE_ADDNEWPLAN);
 			}
 		};
 		
 
 		
 		findViewById(R.id.button_add_new_plan).setOnClickListener(mClickListener);
 		//listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		listView.setItemsCanFocus(false);
 		listView.setClickable(false);
 		listView.setFocusable(false);
 		
 		//listView.setOnItemClickListener(new OnItemClickListener() {
 //			@Override
 //			public void onItemClick(AdapterView<?> adapt, View view, int position, long id) {
 //				PlanData plan = (PlanData) adapter.getItem(position);
 //				adapter.clearSelectedPlan();
 //				plan.isSelected = true;
 //				
 //				if(updatePlan(plan) == true) {
 //					
 //				}
 //				
 //				adapter.notifyDataSetChanged();
 //			}
 //		});
 		
 		
 		
 		/*
 		 *  TODO Get Plans List from server
 		 *  How to update my list?
 		 *  After get list, 
 		 */
 		adapter = new PlanListAdapter(this);
 		
 		String result = getPlanList(planArrayList);
 		
 		if(result.equals("good")) {
 			for(int i = 0; i < planArrayList.size(); i++) {
 				adapter.addItem(planArrayList.get(i));
 			}
 			PlanData defaultP =  adapter.getDefaultPlan();
 			updateDefaultPlan(defaultP); /* set widget */
 		}
 	
 		/* TODO ̰  ϸ  ٵ.. */
 		//adapter.setListItems(lit);
 		listView.setAdapter(adapter);
 		registerForContextMenu(listView);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		switch(requestCode){
 			case MyStaticValue.REQUESTCODE_ADDNEWPLAN: 
 				if(resultCode == RESULT_OK){ 
 					boolean result = data.getExtras().getBoolean("success");
 					if(result == true) /* user add new plan sucessfully */
 					{						
 						adapter.clearAllItems();
 						String ret = getPlanList(planArrayList);
 						if(ret.equals("good")) {
 							for(int i = 0 ; i < planArrayList.size() ; i++) {
 								adapter.addItem(planArrayList.get(i));
 							}
 							adapter.notifyDataSetChanged();
 							PlanData defaultP =  adapter.getDefaultPlan();
 							updateDefaultPlan(defaultP);
 						}
 					}
 				}
 				break;
 			case MyStaticValue.REQUESTCODE_UPDATEPLAN: 
 				if(resultCode == RESULT_OK){ 
 					boolean result = data.getExtras().getBoolean("success");
 					if(result == true) /* user add new plan sucessfully */
 					{
 						adapter.clearAllItems();
 						String ret = getPlanList(planArrayList);
 						if(ret.equals("good")) {
 							for(int i = 0 ; i < planArrayList.size() ; i++) {
 								adapter.addItem(planArrayList.get(i));
 							}
 						
 							adapter.notifyDataSetChanged();
 							PlanData defaultP =  adapter.getDefaultPlan();
 							if(defaultP != null)
 								updateDefaultPlan(defaultP);
 						}
 					}
 				}
 				break;
 			case MyStaticValue.REQUESTCODE_VIEWPLAN:
 				if(resultCode == RESULT_OK)
 				{ 
 					boolean result = data.getExtras().getBoolean("success");
 					if(result == true) /* user add new plan sucessfully */
 					{
 						if(data.getExtras().getInt(MyStaticValue.MODE)== MyStaticValue.MODE_DELETE)
 						{
 							int index = data.getExtras().getInt(MyStaticValue.NUMBER);
 							
 							if(adapter.getCount() > 1 && ((PlanData) adapter.getItem(index)).isSelected == true)
 							{
 								Toast.makeText(this, R.string.str_err_defaultplan, Toast.LENGTH_LONG).show();
 								return;
 							}
 							
 							result = deletePlan(((PlanData) adapter.getItem(index)).uId);
 							if(result == true)
 							{
 								//delete alarm info from alarmSerivce
 								try {
 									getServiceInterface().delAlarm(((PlanData)adapter.getItem(index)).uId);
 								} catch (RemoteException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 								adapter.clearAllItems();
 								String ret = getPlanList(planArrayList);
 								if(ret.equals("good")) {
 									for(int i = 0 ; i < planArrayList.size() ; i++) {
 										adapter.addItem(planArrayList.get(i));
 									}
 									adapter.notifyDataSetChanged();
 									PlanData defaultP =  adapter.getDefaultPlan();
 									updateDefaultPlan(defaultP);
 								}
 							}
 						}
 						else if(data.getExtras().getInt(MyStaticValue.MODE) == MyStaticValue.MODE_UPDATE) 
 						{
 							int index = data.getExtras().getInt(MyStaticValue.NUMBER);
 							//result = updatePlan(((PlanData) adapter.getItem(index)).uId);
 							// ADD NEW PLAN
 							Intent intent = new Intent();
 							intent.putExtra(MyStaticValue.MODE, MyStaticValue.MODE_UPDATE);
 							intent.putExtra(MyStaticValue.NUMBER, index);
 							
 							startActivityForResult(intent.setClass(getApplicationContext(), PlanEditorActivity.class), MyStaticValue.REQUESTCODE_UPDATEPLAN);
 						}
 
 					}
 				}
 				break;
 		}
 	}
 	
 }
