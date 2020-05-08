 package net.vrallev.android.base.view;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.view.View;
 import android.view.ViewTreeObserver;
 import android.view.animation.AccelerateDecelerateInterpolator;
 
 /**
  * @author Ralf Wondratschek
  */
 @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
 public class ViewHelper {
 
     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     public static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
             view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
         } else {
             //noinspection deprecation
             view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
         }
     }
 
     public static void setVisibility(View view, int visibility) {
         setVisibility(view, visibility, 300l);
     }
 
     public static void setVisibility(final View view, final int visibility, long duration) {
         if (view.getVisibility() == visibility) {
             return;
         }
         if (view.getVisibility() != View.VISIBLE && visibility == View.VISIBLE) {
             view.setAlpha(0f);
             view.setVisibility(View.VISIBLE);
            view.animate().alpha(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(duration).start();
 
         } else if (view.getVisibility() == View.VISIBLE) {
             view.animate().alpha(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(duration).setListener(new AnimatorListenerAdapter() {
                 @Override
                 public void onAnimationEnd(Animator animation) {
                     view.setVisibility(visibility);
                     view.setAlpha(1f);
                 }
             }).start();
         }
     }
 }
