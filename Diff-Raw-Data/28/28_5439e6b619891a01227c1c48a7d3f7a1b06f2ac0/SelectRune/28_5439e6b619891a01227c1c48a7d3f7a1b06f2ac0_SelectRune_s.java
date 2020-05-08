 package com.pate.diablo;
 
 import java.util.UUID;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.viewpagerindicator.PageIndicator;
 import com.viewpagerindicator.TitlePageIndicator;
 import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
 
 public class SelectRune extends SherlockFragmentActivity {
 	RuneFragmentAdapter mAdapter;
 	ViewPager mPager;
 	PageIndicator mIndicator;
 	int index;
 	UUID skillUUID;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.select_skill);
 
 		Bundle b = getIntent().getExtras();
 
 		String skillName = null;
 		String selectedClass = null;
 
 		int requiredLevel = 0;
 
 		if (b.containsKey("SkillName")) {
 			skillName = b.getString("SkillName");
 		}
 
 		if (b.containsKey("SelectedClass")) {
 			selectedClass = b.getString("SelectedClass");
 		}
 
 		if (b.containsKey("RequiredLevel")) {
 			requiredLevel = b.getInt("RequiredLevel");
 		}
 
 		if (b.containsKey("SkillUUID")) {
 			skillUUID = (UUID) b.getSerializable("SkillUUID");
 		}
 
 		if (b.containsKey("Index")) {
 			index = b.getInt("Index");
 		}
 
 		OnClickListener itemClickListener = new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				Intent intent = getIntent();
 				intent.putExtra("Rune_UUID", v.getTag().toString());
 				intent.putExtra("Skill_UUID", skillUUID.toString());
 				intent.putExtra("Index", index);
 				setResult(RESULT_OK, intent);
 				finish();
 			}
 
 		};
 
 		mAdapter = new RuneFragmentAdapter(getSupportFragmentManager(),
 				SelectRune.this, skillName, skillUUID, selectedClass,
 				requiredLevel);
 		mAdapter.setOnListItemClickListener(itemClickListener);
 
 		mPager = (ViewPager) findViewById(R.id.pager);
 		mPager.setAdapter(mAdapter);
 
 		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.select_skill_indicator);
 		indicator.setViewPager(mPager);
 		indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
 		mIndicator = indicator;
 
 		mPager.setCurrentItem(mAdapter.getItemPosition(skillName));
 	}
 
 }
