 package com.vitalyobukhov.obliquestrategies;
 
 import android.content.*;
 import android.app.*;
 import android.content.pm.PackageManager;
 import android.content.res.Resources;
 import android.view.*;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.widget.TextView;
 import java.util.Random;
 
 
 public class Main extends Activity {
 
 
     private static final String SEED_STATE_KEY = "seed";
     private static final String STRATEGY_STATE_KEY = "strategy";
     private static final String STRATEGIES_STATE_KEY = "strategies";
 
     /* restart is required after preferences activity */
     private boolean restartRequired;
 
     /* shared data */
     private SharedPreferences preferences;
     private Resources resources;
 
     /* main TextView with strategy text */
     private TextView main_strategy;
 
     /* random seed and instance */
     private long seed;
     private Random random;
 
     /* current strategy and strategies array from resources */
     private Integer strategy;
     private String[] strategies;
 
 
     /* handlers */
     @Override
     protected void onCreate(Bundle activityBundle) {
         super.onCreate(activityBundle);
 
         restartRequired = false;
         preferences =  PreferenceManager.getDefaultSharedPreferences(this);
         resources = getResources();
 
         /* should be initialized before content view */
         setupTheme();
         setupTitleVisibility();
         setupFullscreenMode();
 
         setContentView(R.layout.main);
 
         /* should be initialized after content view */
         main_strategy = (TextView)this.findViewById(R.id.main_strategy);
         setupTextSize();
 
         /* data initialization on first start */
         Bundle intentBundle = getIntent().getExtras();
         if (intentBundle != null) {
             seed = intentBundle.getLong(SEED_STATE_KEY);
             random = new Random(seed);
             strategy = intentBundle.getInt(STRATEGY_STATE_KEY);
             strategies = intentBundle.getStringArray(STRATEGIES_STATE_KEY);
 
             showStrategy();
         } else if (activityBundle == null) {
             seed = System.currentTimeMillis();
             random = new Random(seed);
             strategy = null;
             strategies = resources.getStringArray(R.array.strategies);
 
             showRandomStrategy();
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         if (restartRequired) {
 
             /* activity was started after preferences activity */
             /* restart required to rebuild activity styles */
             restartRequired = false;
 
             Intent intent = getIntent();
             intent.putExtra(SEED_STATE_KEY, seed);
             intent.putExtra(STRATEGY_STATE_KEY, strategy);
             intent.putExtra(STRATEGIES_STATE_KEY, strategies);
 
             finish();
             startActivity(intent);
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         /* save state data */
         outState.putLong(SEED_STATE_KEY, seed);
         outState.putInt(STRATEGY_STATE_KEY, strategy);
         outState.putStringArray(STRATEGIES_STATE_KEY, strategies);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
 
         /* restore state data */
         seed = savedInstanceState.getLong(SEED_STATE_KEY);
         random = new Random(seed);
         strategy = savedInstanceState.getInt(STRATEGY_STATE_KEY);
         strategies = savedInstanceState.getStringArray(STRATEGIES_STATE_KEY);
 
         showStrategy();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         /* show menu */
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         boolean result = true;
 
         switch (item.getItemId()) {
 
             /* get next strategy */
             case R.id.main_menu_next:
                 showRandomStrategy();
                 break;
 
             /* show preferences */
             case R.id.main_menu_preferences:
 
                 /* required to reapply themes */
                 restartRequired = true;
                 startActivity(new Intent(this, Preferences.class));
                 break;
 
             /* show about dialog */
             case R.id.main_menu_about:
                 String version;
                 try {
                     version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                 }
                 catch (PackageManager.NameNotFoundException ignored) {
                     version = "";
                 }
                 String message = String.format(resources.getString(R.string.about_dialog_message), version);
 
                 new AlertDialog.Builder(this)
                         .setTitle(R.string.about_dialog_title)
                         .setMessage(message)
                         .setIcon(android.R.drawable.ic_dialog_info)
                         .setPositiveButton(R.string.about_dialog_button_close,
                                 new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) { }
                         }).show();
                 break;
 
             /* exit */
             case R.id.main_menu_exit:
                 finish();
                 break;
 
             default:
                 result = super.onOptionsItemSelected(item);
                 break;
         }
 
         return result;
     }
 
    public void onMainStrategyClick(View v) {
        showRandomStrategy();
    }

     /* strategies routines */
     private String getRandomStrategy() {
         int newStrategy;
         do {
             newStrategy = random.nextInt(strategies.length);
         } while (strategy != null && newStrategy == strategy);
         strategy = newStrategy;
         return strategies[strategy];
     }
 
     private void showRandomStrategy() {
         main_strategy.setText(formatStrategyText(getRandomStrategy()));
     }
 
     private void showStrategy() {
         main_strategy.setText(formatStrategyText(strategies[strategy]));
     }
 
     private String formatStrategyText(String text) {
         final String format = "#%d\n%s";
         return String.format(format, strategy + 1, text);
     }
 
     /* preferences setup routines */
     private void setupTheme() {
         PreferenceTheme theme = PreferenceTheme.parse(
                 preferences.getString(resources.getString(R.string.preferences_keys_theme),
                         resources.getString(R.string.preferences_defaults_theme)));
 
         switch (theme) {
 
             case BLACK:
                setTheme(android.R.style.Theme_Black);
                 break;
 
             case WHITE:
                setTheme(android.R.style.Theme_Light);
                 break;
 
             default:
                 setTheme(android.R.style.Theme);
                 break;
         }
     }
 
     private void setupTextSize() {
         PreferenceTextSize textSize = PreferenceTextSize.parse(
                 preferences.getString(resources.getString(R.string.preferences_keys_text_size),
                         resources.getString(R.string.preferences_defaults_text_size)));
 
         switch (textSize) {
 
             case SMALL:
                 main_strategy.setTextAppearance(this, android.R.style.TextAppearance_Small);
                 break;
 
             case MEDIUM:
                 main_strategy.setTextAppearance(this, android.R.style.TextAppearance_Medium);
                 break;
 
             case LARGE:
                 main_strategy.setTextAppearance(this, android.R.style.TextAppearance_Large);
                 break;
 
             default:
                 main_strategy.setTextAppearance(this, android.R.style.TextAppearance);
                 break;
         }
     }
 
     private void setupTitleVisibility() {
         boolean showTitle = preferences.getBoolean(
                 resources.getString(R.string.preferences_keys_is_title_visible),
                         resources.getBoolean(R.bool.preferences_defaults_is_title_visible));
 
         if (!showTitle) {
             requestWindowFeature(Window.FEATURE_NO_TITLE);
         }
     }
 
     private void setupFullscreenMode() {
         boolean fullscreen = preferences.getBoolean(
                 resources.getString(R.string.preferences_keys_is_fullscreen),
                         resources.getBoolean(R.bool.preferences_defaults_is_fullscreen));
 
         if (fullscreen) {
             getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                     WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
     }
 }
