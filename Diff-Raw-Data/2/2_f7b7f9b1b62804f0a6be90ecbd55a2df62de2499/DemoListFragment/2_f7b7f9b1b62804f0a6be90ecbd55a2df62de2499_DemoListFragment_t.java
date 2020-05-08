 package com.example.myfirstapp;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.ListFragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class DemoListFragment extends ListFragment
 {
     private boolean mDualPane;
     
     private int currentDemo;
     
     private Map<String, Fragment> demoMap = new HashMap<String, Fragment>();
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         Log.i("info", this.getClass().getName() + " onCreate");
         
         //TODO: Expose this
         addDemo("MessageLogDemo", new MessageLogDemo());
         addDemo("MessageLogDemo2", new MessageLogDemo());
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState) 
     {
         super.onActivityCreated(savedInstanceState);
         Log.i("info", this.getClass().getName() + " onActivityCreated");
         
         setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, getDemoTitles()));
         
         View detailsFrame = getActivity().findViewById(R.id.demoview);
         mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
         
         Log.i("info", this.getClass().getName() + " mDualPane = " + mDualPane);
         
         if(savedInstanceState != null) 
         {
             currentDemo = savedInstanceState.getInt("curChoice");
         }
         
         if(mDualPane)
         {
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             showDetails(currentDemo);
         }
     }
     
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         outState.putInt("curChoice", currentDemo);
     }
     
     @Override
     public void onListItemClick(ListView l, View v, int position, long id)
     {
         Log.i("info", "Demo selected: id: " + id + " pos: " + position);
         Log.i("info", "Item at position: " + l.getItemAtPosition(position));
         
        if(!mDualPane || currentDemo != position)
         {
             showDetails(position);
         }
     }
     
     private void showDetails(int index)
     {
         String demoName = (String) getListView().getItemAtPosition(index);
         
         Log.i("info", "Showing demo: " + demoName);
         
         currentDemo = index;
         
         if(mDualPane)
         {
             getListView().setItemChecked(index, true);
             //Fragment demoFragment = getFragmentManager().findFragmentById(R.id.demoview);
             //if(demoFragment == null) 
             {
                 //TODO: Get correct demo fragment to start
                 Fragment demoFragment = new MessageLogDemo();
                 
                 FragmentTransaction ft = getFragmentManager().beginTransaction();
                 ft.setBreadCrumbTitle("Demo:" + index);
                 ft.setBreadCrumbShortTitle("Demo:" + index);
                 ft.replace(R.id.demoview, demoFragment);
                 ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                 ft.commit();
             }
         }
         else
         {
             Intent intent = new Intent();
             intent.setClass(getActivity(), DemoViewerActivity.class);
             startActivity(intent);
         }
     }
     
     private void addDemo(String title, Fragment demo)
     {
         demoMap.put(title, demo);
     }
     
     private String[] getDemoTitles()
     {
         return demoMap.keySet().toArray(new String[demoMap.keySet().size()]);
     }
 }
