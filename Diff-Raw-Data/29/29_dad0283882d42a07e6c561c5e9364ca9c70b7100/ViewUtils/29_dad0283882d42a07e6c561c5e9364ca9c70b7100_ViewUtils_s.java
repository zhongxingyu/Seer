 package il.ac.huji.chores;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ViewUtils {
     private ViewUtils() {
     }
 
     /**
      * Hides the loadingView and reveals the view with the given ID
      * @param loadingView the view that is still loading and should be hidden
      * @param activity  activity context
      * @param placeholderId android identifier for the view that should be displayed instead
      * @return
      */
     public static View hideLoadingView(View loadingView, Activity activity, int placeholderId) {
         loadingView.setVisibility(View.GONE);
         View view = activity.findViewById(placeholderId);
         view.setVisibility(View.VISIBLE);
         return view;
     }
     
     public static View hideAndKeepLoadingView(View loadingView, Activity activity, int placeholderId){
         loadingView.setVisibility(View.INVISIBLE);
         View view = activity.findViewById(placeholderId);
         view.setVisibility(View.VISIBLE);
         return view;
     }
     
 
     @Deprecated
     public static View hideLoadingView(View loadingView, Context context) {
         try {
             ViewGroup container = (ViewGroup) loadingView.getParent();
             ProgressBar progressBar = new ProgressBar(context);
             container.addView(progressBar);
             loadingView.setVisibility(View.INVISIBLE);
             return progressBar;
         } catch (ClassCastException e) {
             Log.d("ViewUtils$hideLoadingView", "Parent of view is not a ViewGroup", e);
         } catch (Exception e) {
             Log.d("ViewUtils$hideLoadingView", "Unexpected exception", e);
         }
         return null;
     }
 
     public static void replacePlaceholder(View view, View placeholder) {
         placeholder.setVisibility(View.GONE);
         view.setVisibility(View.VISIBLE);
     }
 
     public static View replaceWithMessage(View view, Context context, String message) {
         // TODO
         try {
             ViewGroup container = (ViewGroup) view.getParent();
             TextView textBox = new TextView(context);
             container.addView(textBox);
             view.setVisibility(View.INVISIBLE);
             return textBox;
         } catch (ClassCastException e) {
             Log.d("ViewUtils$hideLoadingView", "Parent of view is not a ViewGroup", e);
         }
         return null;
     }
 }
