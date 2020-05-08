 /*
  * Copyright 2013 Simon Brakhane
  *
  * This file is part of AnJaRoot.
  *
  * AnJaRoot is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * AnJaRoot is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * AnJaRoot. If not, see http://www.gnu.org/licenses/.
  */
 package org.failedprojects.anjaroot;
 
 import java.util.List;
 
 import org.failedprojects.anjaroot.GrantedStorage.OnChangeHandler;
 
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class PackagesFragment extends ListFragment implements OnChangeHandler {
 	private static final String LOGTAG = "AnjaRoot";
 	private PackageManager pm;
 	private GrantedStorage storage;
 
 	class PackagesAdapter extends ArrayAdapter<String> {
 		private class ViewHolder {
 			TextView name;
 			TextView pkgname;
 			ImageView icon;
 		}
 
 		public PackagesAdapter(Context context, List<String> objects) {
 			super(context, R.layout.packages_list_item, objects);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater inflater = (LayoutInflater) getActivity()
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = inflater
 						.inflate(R.layout.packages_list_item, parent, false);
 
 				ViewHolder holder = new ViewHolder();
 				holder.name = (TextView) v.findViewById(R.id.name);
 				holder.pkgname = (TextView) v.findViewById(R.id.package_name);
 				holder.icon = (ImageView) v.findViewById(R.id.icon);
 				v.setTag(holder);
 			}
 
 			String pkgname = getItem(position);
 			try {
 				PackageInfo pi = pm.getPackageInfo(pkgname, 0);
 
 				ViewHolder holder = (ViewHolder) v.getTag();
 				holder.name.setText(pi.applicationInfo.loadLabel(pm));
 				holder.pkgname.setText(pkgname);
 				holder.icon.setImageDrawable(pi.applicationInfo.loadIcon(pm));
 			} catch (NameNotFoundException e) {
 				Log.e(LOGTAG, "Failed to get PackageInfo", e);
 			}
 
 			return v;
 		}
 
 	}
 
 	public static PackagesFragment newInstance() {
 		return new PackagesFragment();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 
 		storage = new GrantedStorage(getActivity());
 		pm = getActivity().getPackageManager();
 
 		setListAdapter(new PackagesAdapter(getActivity(), storage.getPackages()));
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		PackagesAdapter pa = (PackagesAdapter) getListAdapter();
 		String pkg = pa.getItem(position);
 
 		// this will trigger the OnChangeHandler
 		storage.removePackage(pkg);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		PackagesAdapter pa = (PackagesAdapter) getListAdapter();
 		pa.clear();
 		pa.addAll(storage.getPackages());
 		storage.startObserving(this);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		storage.stopObserving();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.packages_list, null);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.packages_list_menu, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		default:
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onReload(boolean success) {
 		getActivity().runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				PackagesAdapter pa = (PackagesAdapter) getListAdapter();
 				pa.clear();
 				pa.addAll(storage.getPackages());
 			}
 		});
 	}
 }
