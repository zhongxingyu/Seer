 package jhekasoft.lotto;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
 
 /**
  *
  * @author jheka
  */
 public class AboutActivity extends Activity {
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
        setContentView(R.layout.about);
        
//        // Make link active
//        TextView textViewAbout = (TextView) findViewById(R.id.textViewAbout);
//        if (textViewAbout != null) {
//            textViewAbout.setMovementMethod(LinkMovementMethod.getInstance());
//        }
     }
 }
