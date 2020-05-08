 
 package com.exam.slieer.activities;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 
 import com.exam.slieer.ui.fragment.DummyItemDetailFragment;
 import com.exam.slieer.ui.fragment.DummyItemListFragment;
import com.example.helloworld.R;
 
 /**
  * 程序入口, FragmentActivity 在内部的某个ViewGroup内动态添加或替代一个Fragment
  */
 public class DummyItemListActivity extends FragmentActivity implements
         DummyItemListFragment.Callbacks {
     private final static String TAG = "ItemListActivity";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         /*
          * 左侧带导航模式
          */
         setContentView(R.layout.activity_item_twopane);
         // In two-pane mode, list items should be given the
         // 'activated' state when touched.
         FragmentManager fragmentManager = getSupportFragmentManager();
         DummyItemListFragment itemListFragment = (DummyItemListFragment)fragmentManager
                 .findFragmentById(R.id.item_list);
         itemListFragment.setActivateOnItemClick(true);
     }
 
     /**
      * Callback method from {@link DummyItemListFragment.Callbacks} indicating
      * that the item with the given ID was selected.
      */
     @Override
     public void onItemSelected(String id) {
         // In two-pane mode, show the detail view in this activity by
         // adding or replacing the detail fragment using a
         // fragment transaction.
         Bundle arguments = new Bundle();
         arguments.putString(DummyItemDetailFragment.ARG_ITEM_ID, id);
         DummyItemDetailFragment fragment = new DummyItemDetailFragment();
 
         /* 在FragmentActivity内，切换Fragment. */
         fragment.setArguments(arguments);
         getSupportFragmentManager().beginTransaction()
                 .replace(R.id.item_detail_container, fragment).commit();
 
     }
 }
