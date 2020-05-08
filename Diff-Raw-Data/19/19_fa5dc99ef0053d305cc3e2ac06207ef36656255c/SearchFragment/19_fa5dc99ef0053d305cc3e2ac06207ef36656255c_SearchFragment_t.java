 package com.music.fmv.fragments;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import com.music.fmv.R;
 import com.music.fmv.adapters.FragmentAdapter;
 import com.music.fmv.core.BaseFragment;
 import com.music.fmv.core.ISearchFragment;
 import com.music.fmv.models.dbmodels.ModelType;
 import com.music.fmv.models.dbmodels.SearchQueryCache;
 import com.music.fmv.utils.ViewUtils;
 import com.music.fmv.views.GlowButton;
 import com.music.fmv.widgets.RefreshableViewPager;
 
 import java.util.ArrayList;
 
 /**
 * User: Vitalii Lebedynskyi
  * Date: 8/5/13
  * Time: 12:39 PM
  */
 public class SearchFragment extends BaseFragment implements ISearchFragment {
     public static final int ARTIST_TAB = 0;
     public static final int ALBUM_TAB = 1;
     public static final int SONG_TAB = 2;
 
     private RefreshableViewPager pager;
 
     private GlowButton artistBtn;
     private GlowButton albumButton;
     private GlowButton songsButton;
 
     @Override
     protected View createView(Bundle savedInstanceState) {
         View mainView = inflateView(R.layout.search_fragment);
         pager = (RefreshableViewPager) mainView.findViewById(R.id.search_pager);
         artistBtn = (GlowButton) mainView.findViewById(R.id.artist_tab);
         albumButton = (GlowButton) mainView.findViewById(R.id.album_tab);
         songsButton = (GlowButton) mainView.findViewById(R.id.song_tab);
 
         artistBtn.setOnClickListener(tabListener);
         albumButton.setOnClickListener(tabListener);
         songsButton.setOnClickListener(tabListener);
 
         ArrayList<BaseFragment> fragments = new ArrayList<BaseFragment>(3);
         fragments.add(ARTIST_TAB, new SearchArtistFragment());
         fragments.add(ALBUM_TAB, new SearchAlbumFragment());
         fragments.add(SONG_TAB, new SearchSongsFragment());

         pager.setAdapter(new FragmentAdapter(getActivity().getSupportFragmentManager(), fragments));
         pager.setOnPageChangeListener(pagerListener);
        pager.setOffscreenPageLimit(3);

         ViewUtils.selectButton(artistBtn, albumButton, songsButton);
         baseActivity.sendScreenStatistic("Artist tab");
         return mainView;
     }
 
 
     private View.OnClickListener tabListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             switch (v.getId()) {
                 case R.id.artist_tab:
                     pager.setCurrentItem(ARTIST_TAB);
                     artistTabClicked();
                     break;
                 case R.id.album_tab:
                     pager.setCurrentItem(ALBUM_TAB);
                     albumTabClicked();
                     break;
                 case R.id.song_tab:
                     pager.setCurrentItem(SONG_TAB);
                     songsTabClicked();
             }
         }
     };
 
     private void songsTabClicked() {
         baseActivity.sendScreenStatistic("Songs tab");
         ViewUtils.selectButton(songsButton, albumButton, artistBtn);
     }
 
     private void albumTabClicked() {
         baseActivity.sendScreenStatistic("Album tab");
         ViewUtils.selectButton(albumButton, artistBtn, songsButton);
     }
 
     private void artistTabClicked() {
         baseActivity.sendScreenStatistic("Artist tab");
         ViewUtils.selectButton(artistBtn, albumButton, songsButton);
     }
 
     public void search(SearchQueryCache model) {
         ModelType type = ModelType.valueOf(model.getQueryType());
         Fragment fr = null;
         switch (type){
             case ARTIST:
                 fr = pager.getFragment(ARTIST_TAB);
                pager.setCurrentItem(ARTIST_TAB);
                 artistTabClicked();
                 break;
             case ALBUM:
                 fr = pager.getFragment(ALBUM_TAB);
                pager.setCurrentItem(ALBUM_TAB);
                 albumTabClicked();
                 break;
             case SONG:
                 fr = pager.getFragment(SONG_TAB);
                pager.setCurrentItem(SONG_TAB);
                 songsTabClicked();
                 break;
         }
         if (fr != null && fr instanceof ISearchFragment) {
             ((ISearchFragment) fr).search(model);
         }
     }
 
     //Listener for viewpager. OnPageSelected() clicked when page was changed by swiping
     private ViewPager.OnPageChangeListener pagerListener = new RefreshableViewPager.BasePageChangeListener() {
         @Override
         public void onPageSelected(int i) {
             switch (i) {
                 case ARTIST_TAB:
                     artistTabClicked();
                     break;
                 case SONG_TAB:
                     songsTabClicked();
                     break;
                 case ALBUM_TAB:
                     albumTabClicked();
             }
         }
     };
 }
