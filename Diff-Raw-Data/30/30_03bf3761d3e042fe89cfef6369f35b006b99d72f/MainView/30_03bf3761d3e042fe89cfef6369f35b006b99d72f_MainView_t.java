 package com.qvdev.apps.twitflick.View;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 
 import com.qvdev.apps.twitflick.Adapters.SectionsPagerAdapter;
import com.qvdev.apps.twitflick.DeveloperKey;
 import com.qvdev.apps.twitflick.Presenter.MainPresenter;
 import com.qvdev.apps.twitflick.R;
 import com.qvdev.apps.twitflick.listeners.onBuzzingListItemClicked;
import com.newrelic.agent.android.NewRelic;
 
 import java.util.Observer;
 
 public class MainView extends FragmentActivity implements onBuzzingListItemClicked {
 
     private ViewPager mViewPager;
     private MainPresenter mMainPresenter;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
        NewRelic.withApplicationToken(
                DeveloperKey.DEVELOPER_KEY_RELIC
        ).start(this.getApplication());

         SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
 
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
         mViewPager.setOffscreenPageLimit(2);
 
         mMainPresenter = new MainPresenter(this);
     }
 
     @Override
     public void onBuzzingItemSelected(float buzzingId) {
         mViewPager.setCurrentItem(1);
         mMainPresenter.getBuzzingDetails(buzzingId);
     }
 
     @Override
     public void onBackPressed() {
         if (mViewPager.getCurrentItem() == 0) {
             finish();
         } else {
             mViewPager.setCurrentItem(0);
         }
     }
 
     public void addBuzzingDetailsObserver(Observer detailView) {
         mMainPresenter.addBuzzingDetailsObserver(detailView);
     }
 }
