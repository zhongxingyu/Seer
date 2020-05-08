 package org.ivan.simple.game.dialogs;
 
 import android.app.Dialog;
import android.content.Context;
 import android.view.View;
 import android.widget.TextView;
 
 import org.ivan.simple.PandaBaseActivity;
 import org.ivan.simple.R;
 
 /**
  * Created by ivan on 17.06.2014.
  */
 public class LoseDialog extends Dialog {
 
     public static final String TITLE = "Oops...";
 
     public LoseDialog(PandaBaseActivity context) {
         super(context);
         init(context);
     }
 
     private void init(PandaBaseActivity context) {
         setTitle(TITLE);
         setContentView(R.layout.lose_dialog);
         getWindow().setBackgroundDrawableResource(R.drawable.settings_border);
        ((TextView) findViewById(android.R.id.title)).setTypeface(context.app().getFontProvider().bold());
         context.prepare(findViewById(R.id.replay));
        ((TextView) findViewById(R.id.replay_caption)).setTypeface(context.app().getFontProvider().regular());
         context.prepare(findViewById(R.id.back));
        ((TextView) findViewById(R.id.back_caption)).setTypeface(context.app().getFontProvider().regular());
     }
 
     public void setReplayOnClickListener(View.OnClickListener l) {
         findViewById(R.id.replay).setOnClickListener(l);
     }
 
     public void setBackOnClickListener(View.OnClickListener l) {
         findViewById(R.id.back).setOnClickListener(l);
     }
 }
