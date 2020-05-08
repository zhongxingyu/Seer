 package uk.co.ataulmunim.android.widget;
 
 import android.app.Activity;
 import android.view.View;
 import com.nicedistractions.shortstacks.R;
 import de.keyboardsurfer.android.widget.crouton.Configuration;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 
 /**
  * Provides extension of Crouton by Ben Weiss.
  * 
  * @author ataulm
  */
 public class CroutonEx {
     private static final String TAG = CroutonEx.class.getSimpleName();
 
     /** Default style for alerting the user. */
     public static final Style ALERT;
     /** Default style for confirming an action. */
     public static final Style CONFIRM;
     /** Default style for warning the user. */
     public static final Style WARN;
     /** Default style for general information. */
     public static final Style INFO;
 
     public static final Configuration SHORT_CONFIG;
     
     static {
         ALERT = new Style.Builder()
                .setBackgroundColor(R.color.crouton_error)
                 .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
         CONFIRM = new Style.Builder()
                .setBackgroundColor(R.color.crouton_confirm)
                 .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
         WARN = new Style.Builder()
                .setBackgroundColor(R.color.crouton_warning)
                 .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
         INFO = new Style.Builder()
                .setBackgroundColor(R.color.crouton_info)
                 .setHeightDimensionResId(R.dimen.actionbar_height)
                 .build();
 

         SHORT_CONFIG = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT).build();
     }
 
     public static Crouton makeText(Activity activity, int textResource, Style style) {
         return makeText(activity, textResource, style, SHORT_CONFIG, true);
     }
 
     public static Crouton makeText(Activity activity, CharSequence text, Style style) {
         return makeText(activity, text, style, SHORT_CONFIG, true);
     }
 
     /**
      * Returns a {@link Crouton} specifying text, style and configuration.
      * 
      * @param activity
      * @param text
      * @param style
      * @param configuration
      * @param dismissOnClick
      * @return
      */
     public static Crouton makeText(Activity activity, CharSequence text, Style style, Configuration configuration,
             boolean dismissOnClick) {
 
         Crouton crouton = Crouton.makeText(activity, text, style);
         crouton.setConfiguration(configuration);
         if (dismissOnClick) makeDismissOnClick(crouton);
 
         return crouton;
     }
 
     public static Crouton makeText(Activity activity, int textResource, Style style, Configuration configuration,
             boolean dismissOnClick) {
         Crouton crouton = Crouton.makeText(activity, textResource, style);
         crouton.setConfiguration(configuration);        
         if (dismissOnClick) makeDismissOnClick(crouton);
         
         return crouton;
     }
 
     /**
      * Adds an onClickListener to dismiss the {@link Crouton} immediately.
      * 
      * @param crouton
      * @return
      */
     public static Crouton makeDismissOnClick(final Crouton crouton) {
         return crouton.setOnClickListener(new View.OnClickListener() {            
             @Override
             public void onClick(View v) {
                 Crouton.hide(crouton);                
             }
         });
     }
 }
