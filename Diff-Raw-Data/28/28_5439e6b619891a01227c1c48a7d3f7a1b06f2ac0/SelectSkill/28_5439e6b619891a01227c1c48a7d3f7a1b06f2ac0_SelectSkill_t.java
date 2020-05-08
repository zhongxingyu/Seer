 package com.pate.diablo;
 
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
 import com.pate.diablo.sectionlist.EntrySkill;
 import com.viewpagerindicator.PageIndicator;
 import com.viewpagerindicator.TitlePageIndicator;
 import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
 
 public class SelectSkill extends SherlockFragmentActivity
 {
     SkillFragmentAdapter mAdapter;
     ViewPager mPager;
     PageIndicator mIndicator;
     int index;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.select_skill);
         
        AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.loadAd(new AdRequest());
        
         Bundle b = getIntent().getExtras();
         
         String skillType = null;
         String selectedClass = null;
         
         int requiredLevel = 0;
         if (b.containsKey("SkillType"))
         {
             skillType = b.getString("SkillType");
         }
         
         if (b.containsKey("SelectedClass"))
         {
             selectedClass = b.getString("SelectedClass");
         }
         
         if (b.containsKey("RequiredLevel"))
         {
             requiredLevel = b.getInt("RequiredLevel");
         }
         
         if (b.containsKey("Index"))
         {
         	index = b.getInt("Index");
         }
         
         OnClickListener itemClickListener = new OnClickListener()
         {
 
 			@Override
 			public void onClick(View v) {
 				
 				Intent intent = getIntent();
 				intent.putExtra("Skill_UUID", v.getTag().toString());
 				intent.putExtra("Index", index);
 				setResult(RESULT_OK, intent);
 				finish();
 			}
         	
         };
 
         mAdapter = new SkillFragmentAdapter(getSupportFragmentManager(), SelectSkill.this, skillType, selectedClass, requiredLevel);
         mAdapter.setOnListItemClickListener(itemClickListener);
         
         mPager = (ViewPager) findViewById(R.id.pager);
         mPager.setAdapter(mAdapter);
         
         TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.select_skill_indicator);
         indicator.setViewPager(mPager);
         indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
         mIndicator = indicator;
 
         mPager.setCurrentItem(mAdapter.getItemPosition(skillType));
     }
 
 }
