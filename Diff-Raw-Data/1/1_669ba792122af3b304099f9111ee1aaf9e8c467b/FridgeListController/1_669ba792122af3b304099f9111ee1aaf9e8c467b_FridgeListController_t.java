 package com.ecofreego.fridgehandler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.PopupMenu;
 
 import com.ecofreego.entrypoint.R;
 import com.ecofreego.fridgehandler.listener.FridgeOnClickListener;
 import com.ecofreego.fridgehandler.listener.FridgeOnLongClickListener;
 import com.ecofreego.itemhandler.ItemFormController;
 import com.ecofreego.itemhandler.ItemListController;
 import com.ecofreego.util.BaseController;
 import com.ecofreego.util.BaseOnMenuItemClickListener;
 
 import de.greenrobot.daoexample.Fridge;
 import de.greenrobot.daoexample.ItemDao.Properties;
 
 public class FridgeListController extends BaseController
 {
 	public static final String KEY_FRIDGE_ID  = "fridgeId";
 
 	public List<Fridge> fridgeList            = new ArrayList<Fridge>();
 
 	public ArrayAdapter<Fridge> fridgeAdapter;
 
 	public ListView list;
 
 	@Override
 	protected void init()
 	{
 		fridgeList    = getGeneralEntityHandler().findAll(Fridge.class);
 		fridgeAdapter = new FridgeArrayAdapter(FridgeListController.this, fridgeList);
 		list          = (ListView) findViewById(R.id.fridge_list);
 
 		list.setAdapter(fridgeAdapter);
 
 		list.setOnItemClickListener(new FridgeOnClickListener(this));
 		list.setOnItemLongClickListener(new FridgeOnLongClickListener(this));
 	}
 
 	public void redirectToFridgeItems(Fridge fridge)
 	{
 		long countOfItemsByFridgeId = 0;
 
 		countOfItemsByFridgeId = getGeneralEntityHandler().getDaoSession().getItemDao().queryBuilder().where(Properties.FridgeId.eq(fridge.getId())).count();
 
 		Class<?> otherController;

 		if (0 != countOfItemsByFridgeId){
 			otherController = ItemListController.class;
 		} else {
 			otherController = ItemFormController.class;
 		}
 
 		Bundle bundle = new Bundle();
 		bundle.putLong(KEY_FRIDGE_ID, fridge.getId());
 
 		redirect(bundle, FridgeListController.this, otherController);
 	}
 
 	@Override
 	protected int getOnCreateLayoutId()
 	{
 		return R.layout.fridge_list;
 	}
 
 	public void redirectToFridgeEditor(Fridge fridge)
 	{
 		Bundle b = new Bundle();
 
 		b.putLong(KEY_FRIDGE_ID, fridge.getId());
 
 		redirect(b, FridgeListController.this, FridgeFormController.class);
 	}
 
 	@Override
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 
 		if (keyCode == KeyEvent.KEYCODE_MENU) {
 			PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.fridge_list));
 			popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, "Új hűtő felvétele");
 
 			popupMenu.getMenu().getItem(0).setOnMenuItemClickListener(new BaseOnMenuItemClickListener(this)
 			{
 
 				@Override
 				public boolean onMenuItemClick(MenuItem menuItem)
 				{
 					controller.redirect(null, FridgeListController.this, FridgeFormController.class);
 
 					return false;
 				}
 			});
 
 			popupMenu.show();
 
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
