 package com.swtorserversstatus.ui;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.view.ViewPager;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.swtorserversstatus.*;
 import com.swtorserversstatus.model.Server;
 import com.swtorserversstatus.utils.ServerListLoader;
 import com.swtorserversstatus.utils.Utils;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Sergey Benner
  * Date: 18/08/12
  * Time: 16:50
  * Purpose:
  */
 public class StatusListFragment extends ListFragment
         implements LoaderManager.LoaderCallbacks<HashSet<Server>> {
 
     ImageAdapter imageAdapter;
     Context context;
     public static HashSet<Server> list;
     public static ListView listView;
     Bundle bundle;
     ViewPager    mViewPager;
     private int tag = 0;
 
 
 
 
     private int tab = 0;
 
 
     @Override
     public void setUserVisibleHint(boolean isVisibleToUser) {
     super.setUserVisibleHint(isVisibleToUser);
 
     if (isVisibleToUser == true) {
         System.out.println("getTag   "+ getTag());
         System.out.println("setUserVisibleHint  "+ tag);
         if(list!=null)System.out.println("list.size()  "+ list.size());
 
     }
     else if (isVisibleToUser == false) {  }
 

     }
 
 
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         //  menu.setHeaderTitle("Context Menu");
         menu.add(0, v.getId(), 0, "Delete Server");
 
     }
 
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        // setRetainInstance(true);
 
 
         System.out.println("onActivityCreated tag changed: " + tab);
                 this.bundle = getArguments();
                 tag = this.bundle.getInt("tab");
                 context = getActivity().getApplicationContext();
                 mViewPager = (ViewPager)getActivity().findViewById(R.id.pager);
 
     }
 
 
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         setRetainInstance(true);
 
         if (this.bundle != null) {
 
             getLoaderManager().initLoader(tag, this.bundle, this);
             setListAdapter(imageAdapter);
             setListShown(false);
             if (tag == 0) {
                 registerForContextMenu(getListView());
             }
             listView  = getListView();
         }
 
 
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 
         HashMap<String, Server> map = null;
         HashSet<Server> serverList = null;
         try {
             serverList = Utils.loadMyServerList(context, ServerListLoader.servers);
             map = Utils.makeServerMap(serverList);
 
         } catch (XmlPullParserException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (item.getTitle().equals("Delete Server")) {
             TextView tv = (TextView) ((RelativeLayout)
                     ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).targetView).getChildAt(0);
             try {
 
                 imageAdapter.remove( map.get(tv.getText()));
 
                 map.remove(tv.getText());
                 serverList.clear();
                 serverList = new HashSet<Server>(map.values());
 
                 Utils.writeMyServerListToXml(getActivity(), Utils.makeXml(serverList));
                 imageAdapter =new ImageAdapter(context,
                                                      android.R.layout.simple_list_item_2, serverList);
 
 
                 setListAdapter(imageAdapter);
                 imageAdapter.notifyDataSetChanged();
                 mViewPager.getAdapter().notifyDataSetChanged();
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             Utils.showToast(getActivity(), tv.getText() + " removed");
 
         }
 
         return true;
     }
 
     public Loader<HashSet<Server>> onCreateLoader(int i, Bundle bundle) {
 
         return new ServerListLoader(context, bundle);
     }
 
     public void onLoadFinished(Loader<HashSet<Server>> hashSetLoader, HashSet<Server> servers) {
 
         list = servers;
         imageAdapter = new ImageAdapter(context, android.R.layout.simple_list_item_2, servers);
         setListAdapter(imageAdapter);
 
         if (isResumed()) {
                     setListShown(true);
                 } else {
                     setListShownNoAnimation(true);
                 }
 
 
     }
 
     public void onLoaderReset(Loader<HashSet<Server>> hashSetLoader) {
         setListAdapter(imageAdapter);
     }
 
 
 
 }
