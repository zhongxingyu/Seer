 /*******************************************************************************
  * Copyright (C) 2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package ru.neverdark.phototools.fragments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.neverdark.phototools.R;
 import ru.neverdark.phototools.utils.MainMenuItem;
 import ru.neverdark.phototools.utils.PluginAdapter;
 import ru.neverdark.phototools.utils.PluginManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TabHost;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class PluginsFragment extends SherlockFragment {
 
     private class AvailableItemClickListener implements OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position,
                 long id) {
             MainMenuItem item = mAvailablePlugins.getItem(position);
             Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(Uri.parse("market://details?id=".concat(item
                     .getPluginPackage())));
             startActivity(marketIntent);
         }
     }
 
     private View mView;
     private ListView mAvailableListView;
     private ListView mInstalledListView;
     private Context mContext;
     private PluginAdapter mAvailablePlugins;
     private static final String ACTIVE_TAB = "activeTabs";
     private TabHost mTabHost;
     /*
      * (non-Javadoc)
      * 
      * @see
      * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
      * android.view.ViewGroup, android.os.Bundle)
      */
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         mView = inflater.inflate(R.layout.activity_plugins, container, false);
         mContext = getSherlockActivity();
 
         bindObjectsToResources();
 
         int activeTab;
         if (savedInstanceState != null) {
             activeTab = savedInstanceState.getInt(ACTIVE_TAB);
         } else {
             activeTab = 0;
         }
         
         buildTabs(activeTab);
 
         mAvailableListView
                 .setOnItemClickListener(new AvailableItemClickListener());
         return mView;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         loadDataToLists();
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         savedInstanceState.putInt(ACTIVE_TAB, mTabHost.getCurrentTab());
     }
     
     /**
      * Builds tabs
      */
     private void buildTabs(int activeTab) {
         mTabHost = (TabHost) mView.findViewById(android.R.id.tabhost);
 
         mTabHost.setup();
 
         TabHost.TabSpec spec = mTabHost.newTabSpec("tag1");
 
         spec.setContent(R.id.plugins_tab_availabe);
         spec.setIndicator(getString(R.string.plugins_available));
         mTabHost.addTab(spec);
 
         spec = mTabHost.newTabSpec("tag2");
         spec.setContent(R.id.plugins_tab_installed);
         spec.setIndicator(getString(R.string.plugins_installed));
         mTabHost.addTab(spec);
         
         mTabHost.setCurrentTab(activeTab);
     }
 
     /**
      * Binds classes objects to resources
      */
     private void bindObjectsToResources() {
         mAvailableListView = (ListView) mView
                 .findViewById(R.id.plugins_listView_available);
         mInstalledListView = (ListView) mView
                 .findViewById(R.id.plugins_listView_installed);
     }
 
     /**
      * Loads data to lists
      */
     private void loadDataToLists() {
         List<MainMenuItem> installed = PluginManager.getInstance(mContext)
                 .scan().getMenuItems();
         PluginAdapter installedPlugins = new PluginAdapter(mContext,
                 R.layout.plugin_installed_item, installed, true);
         mInstalledListView.setAdapter(installedPlugins);
 
         mAvailablePlugins = new PluginAdapter(mContext,
                 R.layout.plugin_available_item, buildAvailableList(), false);
         mAvailableListView.setAdapter(mAvailablePlugins);
     }
 
     /**
      * Builds list of available plug-ins
      * 
      * @return list of available plug-ins
      */
     private List<MainMenuItem> buildAvailableList() {
         List<MainMenuItem> list = new ArrayList<MainMenuItem>();
 
         list.add(createAvailableItem("ru.neverdark.phototools.azimuth",
                 "Azimuth plugin",
                 getResources().getDrawable(R.drawable.ic_plugin_azimuth)));
 
         list.add(createAvailableItem("ru.neverdark.phototools.azimuthfree",
                 "Azimuth plugin (Free)",
                 getResources().getDrawable(R.drawable.ic_plugin_azimuth_free)));
 
         return list;
     }
 
     private MainMenuItem createAvailableItem(String packageName,
             String pluginName, Drawable pluginIcon) {
         MainMenuItem item = new MainMenuItem();
         item.setPluginPackage(packageName);
         item.setAppName(pluginName);
         item.setIcon(pluginIcon);
         return item;
     }
 }
