 package com.myapps.articlesactivity;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 
 import com.myapps.articlesactivity.model.ArticlesManager;
 
 public class MainActivity extends FragmentActivity implements ArticlesHeadlineFragment.OnArticleHeadlineSelectedListener {
 
     public static final String EXTRA_ARTICLE_ID = "com.myapps.articlesactivity.EXTRA_ARTICLE_ID";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         if (savedInstanceState == null) {
            showFragment(new ArticlesHeadlineFragment());
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public void onArticleHeadlineSelected(int position) {
         int headlineId = ArticlesManager.get().getHeadLines().get(position).getId();
 
         ArticlesFragment articlesFragment = new ArticlesFragment();
         Bundle bundle = new Bundle();
         bundle.putInt(EXTRA_ARTICLE_ID, headlineId);
         articlesFragment.setArguments(bundle);
 
        showFragment(articlesFragment);
     }
 
    private void showFragment(Fragment fragment) {
         if (fragment != null && findViewById(R.id.fragments_container) != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container,
                    fragment).addToBackStack(null).commit();
         }
     }
 }
