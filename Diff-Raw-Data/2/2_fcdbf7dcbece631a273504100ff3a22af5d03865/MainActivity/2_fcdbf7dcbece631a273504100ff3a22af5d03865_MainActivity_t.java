 package net.anzix.callcost;
 
 import android.app.AlertDialog;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.anzix.callcost.api.World;
 import net.anzix.callcost.api.Country;
 import net.anzix.callcost.api.Plan;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.SimpleAdapter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import net.anzix.callcost.custom.CustomLoader;
 
 public class MainActivity extends ListActivity {
 
     List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 
     private static final int MENU_SETTINGS = 1;
 
     private static final int MENU_RELOAD = 2;
 
     private static final int MENU_REFRESH = 3;
 
     private static final int MENU_ABOUT = 4;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, MENU_REFRESH, 0, "Refresh");
         menu.add(0, MENU_RELOAD, 1, "Reload ext.");
         menu.add(0, MENU_SETTINGS, 2, "Preferences");
         menu.add(0, MENU_ABOUT, 3, "About");
         return true;
 
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_SETTINGS:
                 Intent intent = new Intent(this, MyPreferencesActivity.class);
                 startActivity(intent);
                 return true;
             case MENU_REFRESH:
                 refresh();
                 return true;
             case MENU_RELOAD:
                 reload(World.instance());
                 return true;
             case MENU_ABOUT:
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle("Call costs");
                 builder.setMessage("For bugs, issues or feedback go to http://wiki.github.com/elek/callcosts/\n\n"
                         + "You can use custom files to define plans from your country, see the documentation at the linke above. "
                         + "Feel free to contant me if you have a problem or if you would like to include your country in the main application releases." +
                         "\n\n Buttons help:\nReload ext. -- reload external definition from sdcard\nRefresh -- recalculate suggestions").setCancelable(true);
                 AlertDialog alert = builder.create();
                 alert.show();
                 return true;
 
         }
         return false;
     }
 
     public void refresh() {
         result.clear();
         Cursor c = AndroidUtils.getCursor(this);
         startManagingCursor(c);
 
         Country country = World.instance().getCurrentCountry();
         Log.i("callcost", "" + country.getProviders().size());
         Log.i("callcost", "" + country.getNumberParser());
 
         Collection<Plan> plans = country.getAllPlans();
 
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
         int requiredNet = Integer.parseInt(sp.getString("netusage", "0"));
 
         result = AndroidUtils.calculateplans(plans, c, country.getNumberParser(), requiredNet);
 
         //calculate net usage
 
         Collections.sort(result, new Comparator<Map<String, Object>>() {
 
             public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                 Integer i1 = (Integer) o1.get("costint");
                 Integer i2 = (Integer) o2.get("costint");
                 return i1.compareTo(i2);
             }
         });
         SimpleAdapter adapter = new SimpleAdapter(this, result, R.layout.cost, new String[]{"provider", "callplan", "cost"}, new int[]{R.id.providert, R.id.plan, R.id.cost});
         setListAdapter(adapter);
     }
 
     public void reload(World instance) {
        File f = new File(Environment.getExternalStorageDirectory() + "/callcosts.def");
         if (f.exists()) {
             FileInputStream fis = null;
             try {
                 CustomLoader loader = new CustomLoader();
                 fis = new FileInputStream(f);
                 loader.read(instance, fis);
                 fis.close();
             } catch (Exception ex) {
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage("Error on loading external file " + f.getAbsolutePath());
                 builder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
 
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                     }
                 }).setNegativeButton("No", new DialogInterface.OnClickListener() {
 
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                     }
                 });
                 AlertDialog alert = builder.create();
                 alert.show();
                 Log.e("callcost", "Error on loading external file", ex);
             } finally {
                 try {
                     fis.close();
                 } catch (IOException ex) {
                     Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         refresh();
     }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         try {
             SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
             String countryCode = sp.getString("country", "hu");
             World instance = World.instance();
             CustomLoader loader = new CustomLoader();
             InputStream st = getResources().openRawResource(R.raw.hungary);
             loader.read(instance, st);
             st.close();
 
             reload(instance);
 
             try {
                 instance.changeCountry(countryCode);
             } catch (IllegalArgumentException ex) {
                 //if deleted the country
                 if (World.instance().getCountries().size() > 0) {
                     Country c = World.instance().getCountries().iterator().next();
                     World.instance().changeCountry(c.getId());
                 }
             }
             super.onCreate(savedInstanceState);
             setContentView(R.layout.plans);
         } catch (IOException ex) {
             Log.e("callcost", "Error on loading coutries", ex);
         }
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, final int position, long id) {
         super.onListItemClick(l, v, position, id);
         Intent intent = new Intent(this, PlanActivity.class);
         intent.putExtra("planid", (String) result.get(position).get("planid"));
         startActivity(intent);
 
     }
 
     private static class Result {
 
         public Result(int cost, String plan) {
             this.cost = cost;
             this.plan = plan;
         }
 
         public Result() {
         }
         int cost;
 
         String plan;
 
     }
 }
