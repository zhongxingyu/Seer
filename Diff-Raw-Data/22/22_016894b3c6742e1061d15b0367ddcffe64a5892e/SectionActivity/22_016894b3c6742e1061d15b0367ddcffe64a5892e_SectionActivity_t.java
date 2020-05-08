 package com.hoffenkloffen.lonewolf;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.*;
 import android.webkit.WebView;
 import android.widget.EditText;
 import com.hoffenkloffen.lonewolf.controllers.GameContext;
 import com.hoffenkloffen.lonewolf.controllers.events.DebugEventHandler;
 import com.hoffenkloffen.lonewolf.controllers.events.SectionEventHandler;
 import com.hoffenkloffen.lonewolf.controllers.interfaces.JavascriptInterface;
 import com.hoffenkloffen.lonewolf.controllers.interfaces.SectionJavascriptInterface;
 import com.hoffenkloffen.lonewolf.controllers.section.Section;
 import com.hoffenkloffen.lonewolf.models.KaiDiscipline;
 import com.hoffenkloffen.lonewolf.models.LoneWolf;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SectionActivity extends BaseBrowserActivity implements SectionEventHandler, DebugEventHandler {
 
     private static final String TAG = SectionActivity.class.getSimpleName();
 
     // Controllers
     protected GameContext context;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.browser);
 
         browser = (WebView) findViewById(R.id.browser);
 
         init();
         display();
 
         Log.d(TAG, "Done");
     }
 
     @Override
     protected void init() {
         super.init();
 
         context = GameContext.getInstance();
         context.getSectionManager().setResourceHandler(new DebugSectionResourceHandler(this));
         context.getSectionManager().setRenderer(this);
     }
 
     protected Iterable<JavascriptInterface> getJavascriptInterfaces() {
         List<JavascriptInterface> result = new ArrayList<JavascriptInterface>();
         result.add(new SectionJavascriptInterface(this));
 
         return result;
     }
 
     //<editor-fold desc="Menu">
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.section_meny, menu);
 
         menu.setGroupVisible(R.id.menu_group_debug, context.getPreferences().getDebugMode());
 
         return true;
     }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        if (id == R.id.menu_debug_go_to_previous) debugGoToPrevious(item);
        else if (id == R.id.menu_debug_go_to_next) debugGoToNext(item);
        else if (id == R.id.menu_debug_go_to) debugGoTo(item);
        else if (id == R.id.menu_debug_noob_wolf) debugNoobWolf(item);
        else if (id == R.id.menu_debug_leet_wolf) debugLeetWolf(item);
        else if (id == R.id.menu_debug_log) debugLog(item);
        else super.onOptionsItemSelected(item);

        return true;
    }

     public void debugGoTo(MenuItem item) {
         final EditText input = new EditText(this);
         input.setInputType(InputType.TYPE_CLASS_NUMBER);
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Go to section:")
                 .setView(input)
                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         Log.d(TAG, "begin");
                         String section = input.getText().toString();
                         goTo(section);
                         Log.d(TAG, "end");
                     }
                 });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void debugGoToPrevious(MenuItem item) {
         goToPrevious();
     }
 
     public void debugGoToNext(MenuItem item) {
         goToNext();
     }
 
     public void debugNoobWolf(MenuItem item) {
         noobWolf();
     }
 
     public void debugLeetWolf(MenuItem item) {
         leetWolf();
     }
 
     public void debugLog(MenuItem item) {
         Log.d(TAG, "Debug log");
 
         startActivity(new Intent(getBaseContext(), LogActivity.class));
     }
 
     //</editor-fold>
 
     private void display() {
         Log.d(TAG, "Display");
 
         context.getSectionManager().enter(context.getSectionManager().getCurrent().getNumber());
     }
 
     //<editor-fold desc="SectionEventHandler">
 
     @Override
     public void turnTo(final String section) {
         runOnUiThread(new Runnable() {
             public void run() {
                 Log.d(TAG, "Turn to section: " + section);
 
                 context.getSectionManager().enter(section);
             }
         });
     }
 
     @Override
     public void roll() {
         runOnUiThread(new Runnable() {
             public void run() {
                 Log.d(TAG, "Roll a random number");
 
                 context.getRandomNumberManager().roll();
                 display();
             }
         });
     }
 
     @Override
     public void roll(final String index) {
         runOnUiThread(new Runnable() {
             public void run() {
                 Log.d(TAG, "Roll a random number: " + index);
 
                 context.getRandomNumberManager().roll(index);
                 display();
             }
         });
     }
 
     @Override
     public void fight() {
         runOnUiThread(new Runnable() {
             public void run() {
                 Log.d(TAG, "Fight enemy");
 
                 context.getCombatManager().fight();
                 display();
             }
         });
     }
 
     @Override
     public void fight(final String index) {
         runOnUiThread(new Runnable() {
             public void run() {
                 Log.d(TAG, "Fight enemy: " + index);
 
                 context.getCombatManager().fight(index);
                 display();
             }
         });
     }
 
     @Override
     public void inventory() {
         runOnUiThread(new Runnable() {
             public void run() {
                 Log.d(TAG, "Display action chart");
 
                 startActivity(new Intent(getBaseContext(), ActionChartActivity.class));
             }
         });
     }
 
     //</editor-fold>
 
     //<editor-fold desc="DebugEventHandler">
 
     @Override
     public void goToPrevious() {
         Log.d(TAG, "goToPrevious");
 
         Section section = context.getSectionManager().getCurrent();
         int number = Integer.parseInt(section.getNumber());
 
         if (number > 1) context.getSectionManager().enter(Integer.toString(--number));
     }
 
     @Override
     public void goToNext() {
         Log.d(TAG, "goToNext");
 
         Section section = context.getSectionManager().getCurrent();
         int number = Integer.parseInt(section.getNumber());
 
         if (number < 350) context.getSectionManager().enter(Integer.toString(++number));
     }
 
     @Override
     public void goTo(String section) {
         Log.d(TAG, "goTo: " + section);
 
        int number = Integer.parseInt(section);

        if (number < 1 || number > 350) return;

         context.getSectionManager().enter(section);
     }
 
     public void noobWolf() {
         Log.d(TAG, "noobWolf");
 
         context.setCharacter(getNoobWolf());
     }
 
     public void leetWolf() {
         Log.d(TAG, "leetWolf");
 
         context.setCharacter(getLeetWolf());
     }
 
     private LoneWolf getNoobWolf() {
         LoneWolf character = new LoneWolf();
         character.setCombatSkill(10);
         character.setEndurance(10);
         character.getInventory().getGoldCrowns().setQuantity(1);
 
         return character;
     }
 
     private LoneWolf getLeetWolf() {
         LoneWolf character = new LoneWolf();
         character.setCombatSkill(40);
         character.setEndurance(40);
         character.add(KaiDiscipline.Camouflage);
         character.add(KaiDiscipline.Hunting);
         character.add(KaiDiscipline.SixthSense);
         character.add(KaiDiscipline.Tracking);
         character.add(KaiDiscipline.Healing);
         character.add(KaiDiscipline.Weaponskill);
         character.add(KaiDiscipline.Mindshield);
         character.add(KaiDiscipline.Mindblast);
         character.add(KaiDiscipline.AnimalKinship);
         character.add(KaiDiscipline.MindOverMatter);
         character.getInventory().getGoldCrowns().setQuantity(40);
 
         return character;
     }
 
     //</editor-fold>
 }
