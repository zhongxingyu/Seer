 package com.intro.compintro.mainfragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.intro.compintro.R;
 import com.intro.compintro.activity.MainActivity;
 import com.intro.compintro.adapter.BehindMenuAdapter;
 import com.intro.compintro.datastruct.MenuItem;
 
 public class BehindContentFragment extends SherlockListFragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.behind_menu, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		String[] menuItemNamesZh = getResources().getStringArray(
 				R.array.menu_items_name_zh);
 		String[] menuItemNamesEn = getResources().getStringArray(
 				R.array.menu_items_name_en);
		int[] icon = getResources().getIntArray(R.array.menu_item_icon);
 		List<MenuItem> menuList = new ArrayList<MenuItem>();
 
		for (int i = 0; i < icon.length; i++) {
			menuList.add(new MenuItem(icon[i], menuItemNamesZh[i],
					menuItemNamesEn[i]));
 		}

 		ArrayAdapter<MenuItem> adapter = new BehindMenuAdapter(
 				getSherlockActivity(), menuList);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onListItemClick(ListView lv, View v, int position, long id) {
 		SherlockFragment newContent = null;
 		switch (position) {
 		case 0:
 			newContent = new BasicInfoFragment();
 			break;
 		case 1:
 			newContent = new MarketingFragment();
 			break;
 		case 2:
 			newContent = new MainProductFragment();
 			break;
 		case 3:
 			newContent = new MoreProductFragment();
 			break;
 		}
 		if (newContent != null)
 			switchFragment(newContent);
 	}
 
 	// the meat of switching the above fragment
 	private void switchFragment(SherlockFragment fragment) {
 		if (getSherlockActivity() == null)
 			return;
 
 		if (getSherlockActivity() instanceof MainActivity) {
 			MainActivity fca = (MainActivity) getActivity();
 			fca.switchContent(fragment);
 		}
 	}
 
 }
