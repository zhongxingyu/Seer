package com.protech.ascension.gui.chapter1;
 
 import android.animation.*;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.*;
 import com.protech.ascension.*;
 import com.protech.ascension.gui.CaptionTextFragment;
 import com.protech.ascension.gui.MyPageStateAdapter;
 import com.protech.ascension.gui.TouchImageView2;
 import com.protech.ascension.gui.TouchViewPager;
 import com.protech.ascension.util.AsyncDrawable;
 import com.protech.ascension.util.BitmapWorkerTask;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BNengel
  * Date: 5/10/12
  * Time: 10:56 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Chapter1FragmentActivity extends FragmentActivity {
     private Animator mCurrentTextAnimator;
     private MyPageStateAdapter myPageStateAdapter;
 
 //    private ViewPager mViewPager;
     private TouchViewPager mViewPager;
     private TouchImageView2 mImageView;
     private CaptionTextFragment captionTextFragment;
 
     private String[] mToggleText = {"Show Text", "Hide Text"};
 
     private boolean mTextAreaHidden = false;
 
     public final static Integer[] PAGE_LIST = new Integer[] {
             R.drawable.image8,
             R.drawable.image5,
             R.drawable.image1
     };
     private final static Integer[] CAPTION_LIST = new Integer[] {
             R.array.ch1_pg1_caption_list,
             R.array.ch1_pg2_caption_list
     };
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (savedInstanceState != null) {
             mTextAreaHidden = savedInstanceState.getBoolean("textAreaHidden");
         }
         setContentView(R.layout.chapter_1_layout);
 
         captionTextFragment = (CaptionTextFragment) getSupportFragmentManager().
                         findFragmentById(R.id.caption_text_frag);
 
         // Create an adapter that when requested, will return a fragment representing an object in
         // the collection.
         //
         // ViewPager and its adapters use support library fragments, so we must use
         // getSupportFragmentManager.
         String[] pageList = getResources().getStringArray(R.array.ch1_page_list);
         myPageStateAdapter = new MyPageStateAdapter(getSupportFragmentManager(), 1,  PAGE_LIST.length);
 
         // Set up action bar.
 //        final ActionBar actionBar = getActionBar();
 
         // Specify that the Home button should show an "Up" caret, indicating that touching the
         // button will take the user one step up in the application's hierarchy.
 //        actionBar.setDisplayHomeAsUpEnabled(true);
 
         // Set up the ViewPager, attaching the adapter.
         mViewPager = (TouchViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(myPageStateAdapter);
         mViewPager.setCaptionTextFragment(captionTextFragment);
 
         if (mTextAreaHidden) {
 //            getSupportFragmentManager().beginTransaction().hide(
 //                    getSupportFragmentManager().findFragmentById(R.id.caption_text_frag));
         } else {
 //            captionTextFragment = new CaptionTextFragment();
 //
 //            getSupportFragmentManager().beginTransaction().add(
 //                    android.R.id.content, captionTextFragment
 //            ).commit();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.findItem(R.id.menu_toggleTextCloud).setTitle(mToggleText[mTextAreaHidden ? 0 : 1]);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_toggleTextCloud:
                 toggleVisibleTextArea();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public void toggleVisibleTextArea() {
         // Use these for custom animations.
         final android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
         final Fragment f = fm.findFragmentById(R.id.caption_text_frag);
 //        final Fragment f = captionTextFragment;
         final View textAreaView = f.getView();
 
         // Determine if we're in portrait, and whether we're showing or hiding the titles
         // with this toggle.
         final boolean isPortrait = getResources().getConfiguration().orientation ==
                 Configuration.ORIENTATION_PORTRAIT;
 
         final boolean shouldShow = f.isHidden() || mCurrentTextAnimator != null;
 
         // Cancel the current titles animation if there is one.
         if (mCurrentTextAnimator != null)
             mCurrentTextAnimator.cancel();
 
         // Begin setting up the object animator. We'll animate the bottom or right edge of the
         // titles view, as well as its alpha for a fade effect.
         // todo - Original Approach
         ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                 textAreaView,
                 PropertyValuesHolder.ofFloat("alpha", shouldShow ? 1 : 0)
         );
         if (shouldShow) {
             fm.beginTransaction().show(f).commit();
             objectAnimator.addListener(new AnimatorListenerAdapter() {
                 @Override
                 public void onAnimationEnd(Animator animator) {
                     mCurrentTextAnimator = null;
                     mTextAreaHidden = false;
                     invalidateOptionsMenu();
                 }
             });
 
         } else {
             objectAnimator.addListener(new AnimatorListenerAdapter() {
                 boolean canceled;
 
                 @Override
                 public void onAnimationCancel(Animator animation) {
                     canceled = true;
                     super.onAnimationCancel(animation);
                 }
 
                 @Override
                 public void onAnimationEnd(Animator animator) {
                     if (canceled)
                         return;
                     mCurrentTextAnimator = null;
                     fm.beginTransaction().hide(f).commit();
                     mTextAreaHidden = true;
                     invalidateOptionsMenu();
                 }
             });
         }
 
         // Start the animation.
         objectAnimator.start();
         mCurrentTextAnimator = objectAnimator;
 
         // Manually trigger onNewIntent to check for ACTION_DIALOG.
         onNewIntent(getIntent());
     }
 
     public void loadBitmap(int resId, TouchImageView2 imageView) {
         if (BitmapWorkerTask.cancelPotentialWork(resId, imageView)) {
             final BitmapWorkerTask task = new BitmapWorkerTask(getResources(), imageView);
             final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
             imageView.setImageDrawable(asyncDrawable);
             task.execute(resId);
         }
 //        mImageView.setImageResource(R.drawable.image_placeholder);
 //        mImageView.setImageBitmap();
         BitmapWorkerTask task = new BitmapWorkerTask(getResources(), mImageView);
         task.execute(resId);
     }
 
 //    public static boolean cancelPotentialWork(int data, TouchImageView2 imageView) {
 //        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
 //
 //        if (bitmapWorkerTask != null) {
 //            final int bitmapData = bitmapWorkerTask.data;
 //            if (bitmapData != data) {
 //                // Cancel previous task
 //                bitmapWorkerTask.cancel(true);
 //            } else {
 //                // The same work is already in progress
 //                return false;
 //            }
 //        }
 //        // No task associated with the ImageView, or an existing task was cancelled
 //        return true;
 //    }
 //
 //    private static BitmapWorkerTask getBitmapWorkerTask(TouchImageView2 imageView) {
 //       if (imageView != null) {
 //           final Drawable drawable = imageView.getDrawable();
 //           if (drawable instanceof AsyncDrawable) {
 //               final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
 //               return asyncDrawable.getBitmapWorkerTask();
 //           }
 //        }
 //        return null;
 //    }
 }
