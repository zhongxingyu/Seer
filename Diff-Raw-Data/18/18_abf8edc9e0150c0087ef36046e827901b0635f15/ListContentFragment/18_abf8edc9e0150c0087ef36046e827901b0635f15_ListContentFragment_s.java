 package com.koushikdutta.widgets;
 
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.ViewSwitcher;
 
 public class ListContentFragment extends ActivityBaseFragment {
     ViewGroup mContent;
     ViewGroup mContainer;
     
     @Override
     protected int getListHeaderResource() {
         return R.layout.list_content_header;
     }
     
     private void setPadding() {
         float hor = getResources().getDimension(R.dimen.activity_horizontal_margin);
         float ver = getResources().getDimension(R.dimen.activity_vertical_margin);
         getListView().setPadding(0, 0, 0, 0);
         mContainer.setPadding((int)hor, (int)ver, (int)hor, (int)ver);
     }
     
     Fragment mCurrentContent;
     
     @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = super.onCreateView(inflater, container, savedInstanceState);

         mContent = (ViewGroup)ret.findViewById(R.id.content);
         mContainer = (ViewGroup)ret.findViewById(R.id.list_content_container);
 
         setPadding();
         getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         
        return ret;
     }
     
     public void setContent(Fragment content) {
         Fragment last = mCurrentContent;
         mCurrentContent = content;
         FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
         ft.add(R.id.content, mCurrentContent);
         if (last != null)
             ft.remove(last);
         if (mContainer instanceof ViewSwitcher) {
             ViewSwitcher switcher = (ViewSwitcher)mContainer;
             if (mContent != switcher.getCurrentView())
                 switcher.showNext();
         }
         ft.commit();
     }
 
     public boolean onBackPressed() {
         if (mCurrentContent == null)
             return false;
         if (mContainer instanceof ViewSwitcher) {
             ((ViewSwitcher)mContainer).showPrevious();
             FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
             ft.remove(mCurrentContent);
             ft.commit();
             mCurrentContent = null;
             return true;
         }
         return false;
     }
     
     @Override
     void onListItemClick(ListItem li) {
         super.onListItemClick(li);
 //        if (mContentAdapter == null)
 //            return;
 //        
 //        setContent(mContentAdapter.getFragment(li, mCurrentContent));
     }
 
     @Override
     protected int getListItemResource() {
         return R.layout.list_item_selectable;
     }
 
     @Override
     protected int getListFragmentResource() {
         return R.layout.list_content;
     }
     
     public ViewGroup getContent() {
         return mContent;
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         setPadding();
     }
 //    
 //    ListContentAdapter mContentAdapter;
 //    public ListContentAdapter getContentAdapter() {
 //        return mContentAdapter;
 //    }
 //    
 //    public void setContentAdapter(ListContentAdapter adapter) {
 //        mContentAdapter = adapter;
 //    }
 }
