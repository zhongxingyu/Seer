 package com.mogoweb.browser;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.RelativeLayout;
 
 import com.mogoweb.browser.Intention.Type;
 import com.mogoweb.browser.Tab.Embodiment;
 import com.mogoweb.browser.TabManager.TabData;
 import com.mogoweb.browser.utils.Logger;
 import com.mogoweb.browser.views.DesignShared;
 import com.mogoweb.browser.views.PageIndicator;
 import com.mogoweb.browser.views.TileView;
 
 public class TabScreen extends RelativeLayout implements TabManager.Listener {
 
     private final Context mContext;
     private final TabManager mTabManager;
     private ViewPager mTabsPager;
     private ViewPagerAdapter mTabsAdapter;
 
     public TabScreen(Context context, AttributeSet attrs) {
         super(context, attrs);
         mContext = context;
         mTabManager = TabManager.getInstance();
     }
 
     @Override
     protected void onFinishInflate () {
         mTabManager.addListener(this);
 
         View.OnClickListener onClickListener = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 switch (v.getId()) {
                     case R.id.home_tabs_button:
                         mTabManager.setTabIntention(new Intention(Type.I_Welcome));
                         break;
                     case R.id.tabs_return_button:
                         setVisibility(View.GONE);
                         break;
                     default:
                         break;
                 }
             }
         };
 
         int ids[] = {R.id.home_tabs_button, R.id.tabs_return_button};
         for (int id: ids) {
             //set the same listener
             findViewById(id).setOnClickListener(onClickListener);
         }
 
         mTabsPager = (ViewPager) findViewById(R.id.tabs_list);
         mTabsAdapter = new ViewPagerAdapter();
         mTabsPager.setAdapter(mTabsAdapter);
         mTabsPager.setOnPageChangeListener( new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 PageIndicator indicator = (PageIndicator) findViewById(R.id.tabs_page_indicator);
                 indicator.setActiveDot(position);
             }
         });
     }
 
     private void updateTabCounter() {
         // Update the tab count
         PageIndicator indicator = (PageIndicator) findViewById(R.id.tabs_page_indicator);
         indicator.setDotCount(mTabManager.getTabsCount());
         indicator.setActiveDot(mTabManager.getActiveTabIndex());
     }
 
     @Override
     public void onTabAdded(TabData td, int idx) {
         TabTileView tv = new TabTileView(mContext, td);
 
         tv.setTag(td);
         mTabsAdapter.addTabView(tv);
 
         updateTabCounter();
     }
 
     @Override
     public void onTabRemoved(TabData td, int idx) {
         TileView tv = mTabsAdapter.getTabTile(td);
         if (tv != null)
             mTabsAdapter.removeTabView(tv);
 
         updateTabCounter();
     }
 
     @Override
     public void onTabSelected(TabData td, boolean bActive) {
         TileView tv = mTabsAdapter.getTabTile(td);
         if (tv != null)
             tv.setIsActive(bActive);
     }
 
     @Override
     public void onTabShow(TabData tab) {
     }
 
     private class TabTileView extends TileView {
         private TabData mTabData;
 
         public TabTileView(Context context, final TabData td) {
             super(context, null, R.attr.tileViewStyle);
             setHasShadow(true);
             setIsActive(td == TabManager.getInstance().getActiveTabData());
             setCloseable(true);
 
             mTabData = td;
 
             setListener( new TileView.Listener () {
                 @Override
                 public void onTileClicked(TileView tile) {
                     TabManager.getInstance().showTab(td);
                 }
 
                 @Override
                 public boolean onTileLongClicked(TileView tile) {
                     TabManager.getInstance().selectTab(td);
                     return true;
                 }
 
                 @Override
                 public void onTileRequestClose(TileView tile) {
                     // remove us from the tab manager
                     TabManager.getInstance().closeTab(td);
                 }
             }
             );
         }
 
         @Override
         protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
             super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 
             // create the tile image
             Bitmap tileBitmap = null;
             int width = getMeasuredWidth();
             int height = getMeasuredHeight();
 
             if (mTabData.tab == null || mTabData.tab.getEmbodiment() == Embodiment.E_Welcome) {
                 // use the default browser logo for a non-web tab
                 tileBitmap = DesignShared.getEmptyTabBitmap(getResources());
             } else {
                 // FIXME : remove once SurfaceTexture is replaced by TextureView
                 // and then use TextureView.getBitmap()
                 tileBitmap = mTabData.tab.getSnapshot(width - 2 * TileView.SHADOW_MARGIN_PX, height - 2 * TileView.SHADOW_MARGIN_PX);
             }
 
             if (tileBitmap != null)
                 setImageBitmap(tileBitmap);
 
         }
     }
 
     public void updateTabTiles() {
 
         Logger.debug("in updateTabTiles");
 
         mTabsAdapter.removeAllTabViews();
 
         int tabsCount = mTabManager.getTabsCount();
         for (int i = 0; i < tabsCount; i++) {
             TabData td = mTabManager.getTabData(i);
             TabTileView tv = new TabTileView(mContext, td);
             tv.setTag(td);
 
             mTabsAdapter.addTabView(tv);
         }
     }
 
     public class ViewPagerAdapter extends PagerAdapter {
         private List<View> views = new ArrayList<View>();
 
         public ViewPagerAdapter(){
         }
 
         @Override
         public void destroyItem(View arg0, int arg1, Object arg2) {
             ((ViewPager) arg0).removeView(views.get(arg1));
         }
 
         @Override
         public void finishUpdate(View arg0) {
             // TODO Auto-generated method stub
         }
 
         @Override
         public int getCount() {
             if (views != null) {
                 return views.size();
             }
 
             return 0;
         }
 
         @Override
         public Object instantiateItem(View arg0, int arg1) {
 
             ((ViewPager) arg0).addView(views.get(arg1), 0);
 
             return views.get(arg1);
         }
 
         @Override
         public boolean isViewFromObject(View arg0, Object arg1) {
             return (arg0 == arg1);
         }
 
         @Override
         public void restoreState(Parcelable arg0, ClassLoader arg1) {
             // TODO Auto-generated method stub
         }
 
         @Override
         public Parcelable saveState() {
             // TODO Auto-generated method stub
             return null;
         }
 
         @Override
         public void startUpdate(View arg0) {
             // TODO Auto-generated method stub
         }
 
         public void addTabView(View tabView) {
             views.add(tabView);
            mTabsPager.setCurrentItem(views.size() - 1);
         }
 
         public void removeTabView(View tabView) {
             mTabsPager.setAdapter(null);
             views.remove(tabView);
             mTabsPager.setAdapter(this);
         }
 
         public void removeAllTabViews() {
             views.clear();
         }
 
         public TileView getTabTile(TabData td) {
             for (Iterator<View> it = views.iterator(); it.hasNext();) {
                 View v = it.next();
                 if (v.getTag() == td) {
                     return (TileView)v;
                 }
             }
             return null;
         }
     }
 }
