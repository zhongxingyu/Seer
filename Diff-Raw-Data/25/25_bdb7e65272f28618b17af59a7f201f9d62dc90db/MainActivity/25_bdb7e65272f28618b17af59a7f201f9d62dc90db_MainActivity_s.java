 package net.nologin.meep.ca;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import net.nologin.meep.ca.util.Utils;
 import net.nologin.meep.ca.view.WolframCAView;
 
 public class MainActivity extends SherlockActivity {
 
 
     private WolframCAView caView;
 
     private int ruleDialog_rule;
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //Used to put dark icons on light action bar
         //boolean isLight = SampleList.THEME == R.style.Theme_Sherlock_Light;
 
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.actionbar_itemlist, menu);
         return true;
 
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.actionbar_changerule:
 
                 ruleDialog_rule = caView.getCurrentRule();
 
                 View layout = getLayoutInflater().inflate(R.layout.rule_select, null);
 
                 // set the summary of the selected rule, eg "Rule 110"
                 final TextView ruleSummaryTxt = (TextView) layout.findViewById(R.id.ruleChangeDialog_ruleSummaryTxt);
                 ruleSummaryTxt.setText(getResources().getString(R.string.ruledialog_ruleSummary, ruleDialog_rule));
 
                 final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.ruleChangeDialog_slider);
                 seekBar.setMax(255);
                 seekBar.setProgress(ruleDialog_rule);
                 seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
 
                     @Override
                     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                         ruleDialog_rule = progress;
                         ruleSummaryTxt.setText(getResources().getString(R.string.ruledialog_ruleSummary, ruleDialog_rule));
                     }
 
                     @Override
                     public void onStartTrackingTouch(SeekBar seekBar) {}
 
                     @Override
                     public void onStopTrackingTouch(SeekBar seekBar) {}
                 });
 
 
                 layout.findViewById(R.id.ruleChangeDialog_butPrev).setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         ruleDialog_rule = ruleDialog_rule <= 0 ? 255: ruleDialog_rule-1;
                         seekBar.setProgress(ruleDialog_rule); // seekbar listener will update edittext value
                     }
                 });
 
                 layout.findViewById(R.id.ruleChangeDialog_butNext).setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         ruleDialog_rule = ruleDialog_rule >= 255 ? 0: ruleDialog_rule+1;
                         seekBar.setProgress(ruleDialog_rule); // seekbar listener will update edittext value
                     }
                 });
 
                 AlertDialog.Builder builder = new AlertDialog.Builder(this)
                         .setTitle(getResources().getString(R.string.ruledialog_title))
                         .setCancelable(true)
                         .setView(layout)
                         .setPositiveButton("Select Rule", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 selectRule(ruleDialog_rule);
                             }
                         })
                         .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                             }
                         });
 
                 // create alert dialog
                 AlertDialog alertDialog = builder.create();
 
                 // show it
                 alertDialog.show();
 
                 return true;
 
             case R.id.actionbar_backtostart:
                caView.jumpToOriginTile();
                 return true;
 
             case R.id.actionbar_settings:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
     public void selectRule(int rule){
 
         getSherlock().getActionBar().setTitle(getResources().getString(R.string.actionbar_ruleSummary,rule));
         caView.setupForRule(rule);
 
 
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         caView = (WolframCAView) findViewById(R.id.caView);
 
         // TODO: fix
         selectRule(110);
 
 
     }
 
 
     @Override
     protected void onResume() {
         super.onResume();
 
         caView.setDebugEnabled(Utils.Prefs.getPrefDebugEnabled(this));
 
 
     }
 }
 
