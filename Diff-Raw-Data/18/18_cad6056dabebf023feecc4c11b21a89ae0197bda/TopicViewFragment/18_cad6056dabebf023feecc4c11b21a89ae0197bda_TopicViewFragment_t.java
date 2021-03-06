 package com.dozuki.ifixit;
 
 import java.net.URLEncoder;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class TopicViewFragment extends SherlockFragment
  implements ActionBar.TabListener {
    private static final int GUIDES_TAB = 0;
    private static final int ANSWERS_TAB = 1;
    private static final int MORE_INFO_TAB = 2;
    private static final String CURRENT_PAGE = "CURRENT_PAGE";
   private static final String CURRENT_TOPIC_LEAF = "CURRENT_TOPIC_LEAF";
 
    private TopicNode mTopicNode;
    private TopicLeaf mTopicLeaf;
    private ImageManager mImageManager;
    private ActionBar mActionBar;
    private int mSelectedTab = -1;
 
   public boolean isDisplayingTopic() {
      return mTopicLeaf != null;
   }

    public void setActionBar(ActionBar actionBar) {
       mActionBar = actionBar;
    }
 
    public TopicLeaf getTopicLeaf() {
       return mTopicLeaf;
    }
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
 
       if (mImageManager == null) {
          mImageManager = ((MainApplication)getActivity().getApplication()).
           getImageManager();
       }
 
       if (savedInstanceState != null) {
          mSelectedTab = savedInstanceState.getInt(CURRENT_PAGE);
         mTopicLeaf = (TopicLeaf)savedInstanceState.getSerializable(
          CURRENT_TOPIC_LEAF);
       }
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
     Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.topic_view_fragment, container,
        false);
 
       return view;
    }
 
    @Override
    public void onSaveInstanceState(Bundle outState) {
       super.onSaveInstanceState(outState);
 
       outState.putInt(CURRENT_PAGE, mActionBar.getSelectedNavigationIndex());
      outState.putSerializable(CURRENT_TOPIC_LEAF, mTopicLeaf);
    }
 
    public void setTopicNode(TopicNode topicNode) {
       mTopicNode = topicNode;
 
       getTopicLeaf(mTopicNode.getName());
    }
 
    public void setTopicLeaf(TopicLeaf topicLeaf) {
       mTopicLeaf = topicLeaf;
 
       if (mTopicLeaf == null) {
          mActionBar.removeAllTabs();
          FragmentTransaction ft = getActivity().getSupportFragmentManager().
           beginTransaction();
          ft.replace(R.id.topic_view_page_fragment, new LoadingFragment());
          ft.commit();
          return;
       }
 
       mActionBar.setTitle(mTopicLeaf.getName());
 
       mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
       ActionBar.Tab tab = mActionBar.newTab();
       tab.setText(getActivity().getString(R.string.guides));
       tab.setTabListener(this);
       mActionBar.addTab(tab);
 
       tab = mActionBar.newTab();
       tab.setText(getActivity().getString(R.string.answers));
       tab.setTabListener(this);
       mActionBar.addTab(tab);
 
       tab = mActionBar.newTab();
       tab.setText(getActivity().getString(R.string.moreInfo));
       tab.setTabListener(this);
       mActionBar.addTab(tab);
 
       if (mSelectedTab != -1) {
          mActionBar.setSelectedNavigationItem(mSelectedTab);
       } else if (mTopicLeaf.getGuides().size() == 0) {
          mActionBar.setSelectedNavigationItem(MORE_INFO_TAB);
       }
    }
 
    private void getTopicLeaf(final String topicName) {
       // remove current info
       setTopicLeaf(null);
       mSelectedTab = -1;
 
       APIHelper.getTopic(topicName, new APIHelper.APIResponder<TopicLeaf>() {
          public void setResult(TopicLeaf result) {
             /*
             if (result == null) {
                // Display error
             }
             */
 
             setTopicLeaf(result);
          }
       });
    }
 
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
       int position = tab.getPosition();
       Fragment selectedFragment;
       ft = getActivity().getSupportFragmentManager().beginTransaction();
 
       if (mTopicLeaf == null) {
          Log.w("iFixit", "Trying to get Fragment at bad position");
          return;
       }
 
       if (position == GUIDES_TAB) {
          selectedFragment = new TopicGuideListFragment(mImageManager, mTopicLeaf);
       } else if (position == ANSWERS_TAB) {
          WebViewFragment webView = new WebViewFragment();
 
          webView.loadUrl(mTopicLeaf.getSolutionsUrl());
 
          selectedFragment = webView;
       } else if (position == MORE_INFO_TAB) {
          WebViewFragment webView = new WebViewFragment();
 
          try {
             webView.loadUrl("http://www.ifixit.com/c/" +
              URLEncoder.encode(mTopicLeaf.getName(), "UTF-8"));
          } catch (Exception e) {
             Log.w("iFixit", "Encoding error: " + e.getMessage());
          }
 
          selectedFragment = webView;
       } else {
          Log.w("iFixit", "Too many tabs!");
          return;
       }
 
       ft.replace(R.id.topic_view_page_fragment, selectedFragment);
       ft.commit();
    }
 
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }
 
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
 }
