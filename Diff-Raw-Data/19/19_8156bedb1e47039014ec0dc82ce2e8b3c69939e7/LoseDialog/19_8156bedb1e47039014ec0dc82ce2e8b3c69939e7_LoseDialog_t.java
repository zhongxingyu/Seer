 package org.ivan.simple.game.dialogs;
 
 import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
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
        styleText(android.R.id.title, context.app().getFontProvider().bold(), Color.DKGRAY);
         context.prepare(findViewById(R.id.replay));
        styleText(R.id.replay_caption, context.app().getFontProvider().regular(), Color.DKGRAY);
         context.prepare(findViewById(R.id.back));
        styleText(R.id.back_caption, context.app().getFontProvider().regular(), Color.DKGRAY);
     }
 
     public void setReplayOnClickListener(View.OnClickListener l) {
         findViewById(R.id.replay).setOnClickListener(l);
     }
 
     public void setBackOnClickListener(View.OnClickListener l) {
         findViewById(R.id.back).setOnClickListener(l);
     }

    private void styleText(int id, Typeface font, int color) {
        TextView textView = (TextView) findViewById(id);
        textView.setTypeface(font);
        textView.setTextColor(color);
    }
 }
