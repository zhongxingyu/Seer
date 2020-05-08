 package be.quentinloos.manille.gui.activities;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.SpinnerAdapter;
 
 import be.quentinloos.manille.R;
 import be.quentinloos.manille.core.Manille;
 import be.quentinloos.manille.core.ManilleFree;
 import be.quentinloos.manille.core.ManilleScore;
 import be.quentinloos.manille.core.ManilleTurns;
 import be.quentinloos.manille.util.ManilleParcelable;
 import be.quentinloos.manille.gui.fragments.MainFragment;
 
 /**
  * Main Activity
  *
  * @author Quentin Loos <contact@quentinloos.be>
  */
 public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {
 
     private static final int RESULT_SETTINGS = 1;
 
    private Manille manille = new ManilleFree();
    private boolean recreate;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
        Log.i("Manille", "OnCreate");
        if (savedInstanceState != null) {
             manille = ((ManilleParcelable) savedInstanceState.getParcelable("Manille")).getManille();
            recreate = true;
        }
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
        setActionBarNavigationList();
     }
 
     private void setActionBarNavigationList() {
         String[] array = getResources().getStringArray(R.array.manille_array);
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         array[1] = String.format(array[1], Integer.parseInt(preferences.getString("score", getString(R.string.score_limit))));
         array[2] = String.format(array[2], Integer.parseInt(preferences.getString("turns", getString(R.string.turn_limit))));
 
         SpinnerAdapter mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putParcelable("Manille", new ManilleParcelable(manille));
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_settings:
                 this.startActivityForResult(new Intent(this, SettingsActivity.class), RESULT_SETTINGS);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         switch (requestCode) {
             case RESULT_SETTINGS:
                 refreshMainFragment();
                 break;
         }
     }
 
     public Manille getManille() {
         return manille;
     }
 
     public void newManille(int type) {
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         // The 'type' argument contains the index position
         // of the selected item
         // 0. ManilleFree
         // 1. ManilleScore
         // 2. ManilleTurns
         switch (type) {
             case 0:
                 manille = new ManilleFree();
                 break;
 
             case 1:
                 manille = new ManilleScore(Integer.parseInt(preferences.getString("score", getString(R.string.score_limit))));
                 break;
 
             case 2:
                 manille = new ManilleTurns(
                         Integer.parseInt(preferences.getString("turns", getString(R.string.turn_limit))),
                         Integer.parseInt(preferences.getString("no_trump", getString(R.string.no_trump)))
                 );
                 break;
         }
         refreshMainFragment();
     }
 
     public void refreshMainFragment() {
         ((MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main)).refresh();
     }
 
     @Override
     public boolean onNavigationItemSelected(int i, long l) {
        if (!recreate)
            newManille(i);
        recreate = false;
         return true;
     }
 }
