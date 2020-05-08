 package com.quizmania.activities;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.Button;
 
 import com.quizmania.entities.QuizElement;
 import com.quizmania.fruits.R;
 import com.quizmania.utils.QuizManiaActivity;
 import com.quizmania.utils.QuizPager;
 import com.quizmania.utils.StaticGlobalVariables;
 import com.quizmania.utils.ViewUtils;
 
public class QuizLevelPager extends FragmentActivity{
 	
 	FragmentManager fragmentManager;
 	QuizElement initialElement;
 	int initialSlide=0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_quiz_level);
 		//ViewUtils.inflateContentInTemplate(this, R.layout.activity_quiz_level);
 		initializeElementsFromIntent();		
 		calculateInitialSlide();
 		initializeLevelPager();		
 		
 	}
 
 
 	private void calculateInitialSlide() {
 		initialSlide = StaticGlobalVariables.getLevelElements().indexOf(initialElement);
 		
 	}
 	
 	private void initializeElementsFromIntent() {
 		initialElement = (QuizElement) getIntent().getExtras().get("clickedElement");
 	
 	}
 
 
 
 	private void initializeLevelPager() {
 		fragmentManager = getSupportFragmentManager();
 		QuizPager quizPager = new QuizPager(StaticGlobalVariables.getLevelElements(), fragmentManager);
 		ViewPager quizPagerView = (ViewPager) findViewById(R.id.quizElementPager);		
 		quizPagerView.setAdapter(quizPager);
 		quizPagerView.setCurrentItem(initialSlide);
 		
 		
 		
 	}
 		
 
 
 
 }
