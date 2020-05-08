 package eu.pinnoo.garbagecalendar.view;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import static android.content.Context.ALARM_SERVICE;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.google.analytics.tracking.android.EasyTracker;
 import eu.pinnoo.garbagecalendar.R;
 import eu.pinnoo.garbagecalendar.models.DataModel;
 import eu.pinnoo.garbagecalendar.models.UserModel;
 import eu.pinnoo.garbagecalendar.receivers.NotificationRaiser;
 import eu.pinnoo.garbagecalendar.util.AreaType;
 import eu.pinnoo.garbagecalendar.util.GarbageCollection;
 import eu.pinnoo.garbagecalendar.util.GarbageType;
 import eu.pinnoo.garbagecalendar.util.LocalConstants;
 import eu.pinnoo.garbagecalendar.util.Sector;
 import eu.pinnoo.garbagecalendar.util.scrapers.ApartmentsScraper;
 import eu.pinnoo.garbagecalendar.util.scrapers.CalendarScraper;
 import eu.pinnoo.garbagecalendar.util.scrapers.StreetsScraper;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         initializeModels();
     }
     
     @Override
     public void onStart() {
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
     }
 
     private void toggleNotifications(boolean state) {
         AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
         int i = 0;
         for (GarbageCollection col : DataModel.getInstance().getCollections()) {
             Intent intent = new Intent(getBaseContext(), NotificationRaiser.class);
             intent.putExtra(LocalConstants.NOTIF_INTENT_COL, col);
             PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), i++, intent, 0);
 
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(col.getDate());
             calendar.add(Calendar.HOUR, -4);
 
             if (state) {
                 alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
             } else {
                 alarmManager.cancel(pendingIntent);
             }
         }
     }
 
     private void initializeModels() {
         UserModel.getInstance().setContainer(this);
         DataModel.getInstance().setContainer(this);
 
         Sector s = new Sector(UserModel.getInstance().getContainer().getSharedPreferences("PREFERENCE", Activity.MODE_PRIVATE).getString(LocalConstants.CacheName.USER_SECTOR.toString(), LocalConstants.DEFAULT_SECTOR));
         if (s.getType().equals(AreaType.NONE)) {
             promptUserLocation(false, false, true);
         } else {
             UserModel.getInstance().restoreFromCache();
             new DataScraper(false, false, false).execute();
         }
     }
 
     private void locationIsApartment(final boolean cancelable, final boolean force, final boolean forceStreetSearch) {
         Builder b = new AlertDialog.Builder(this);
         b.setTitle(getString(R.string.invalidLocation));
         b.setMessage(getString(R.string.noCalendarAvailable));
         b.setCancelable(false);
         b.setPositiveButton(getString(R.string.newLocation), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 promptUserLocation(cancelable, force, forceStreetSearch);
             }
         });
         b.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 finish();
             }
         });
         b.show();
     }
 
     /**
      *
      * @param force
      * @return 0 when scraping was successful 1 when the address is an apartment
      * 2 when loading the list of apartments was unsuccessful 3 when loading the
      * streetlist was unsuccessful 4 when loading the calendar was unsuccessful
      */
     private int scrapeData(boolean force, boolean forceStreetSearch) {
         int result = new ApartmentsScraper().loadData(force);
         if (result != 0) {
             return 2;
         }
         if (UserModel.getInstance().isApartment()) {
             return 1;
         }
 
         if (forceStreetSearch || UserModel.getInstance().getSector().toString().equals(LocalConstants.DEFAULT_SECTOR)) {
             result = new StreetsScraper().loadData(force);
             if (result != 0) {
                 return 3;
             }
         }
 
         result = new CalendarScraper().loadData(force);
         if (result != 0) {
             return 4;
         }
         return 0;
     }
 
     private InputStream getStream(String url) throws IOException {
         URLConnection urlConnection = new URL(url).openConnection();
         urlConnection.setConnectTimeout(1000);
         return urlConnection.getInputStream();
     }
 
     public void promptUserLocation(final boolean cancelable, final boolean force, final boolean forceStreetSearch) {
         final EditText input = new EditText(this);
         Builder b = new AlertDialog.Builder(this);
         b.setTitle(getString(R.string.yourLocation));
         b.setMessage(getString(R.string.enterAddress));
         b.setView(input);
         b.setCancelable(cancelable);
         b.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 String address = input.getText().toString();
                 new AddressParser(address, cancelable, force, forceStreetSearch).execute();
             }
         });
         if (cancelable) {
             b.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.dismiss();
                 }
             });
         }
         b.show();
     }
 
     private JSONArray filterOnGhent(JSONArray arr) throws JSONException {
         JSONArray newArr = new JSONArray();
         for (int i = 0; i < arr.length(); i++) {
             JSONObject obj = arr.getJSONObject(i);
             JSONArray objarr = obj.optJSONArray("address_components");
             for (int k = 0; k < objarr.length(); k++) {
                 JSONObject subobj = objarr.getJSONObject(k);
                 if (subobj.getJSONArray("types").getString(0).equals("locality")
                         && subobj.getString("long_name").equals("Gent")) {
                     newArr.put(obj);
                     break;
                 }
             }
         }
         return newArr;
     }
 
     private class AddressParser extends AsyncTask<Void, Void, Integer> {
 
         private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
         private String address;
         private JSONArray arr;
         private boolean cancelable;
         private boolean force;
         private boolean forceStreetSearch;
 
         public AddressParser(String address, boolean cancelable, boolean force, boolean forceStreetSearch) {
             this.address = address;
             this.cancelable = cancelable;
             this.force = force;
             this.forceStreetSearch = forceStreetSearch;
         }
 
         @Override
         protected void onPreExecute() {
             dialog.setMessage(getString(R.string.lookingUp));
             dialog.show();
             dialog.setCancelable(false);
         }
 
         @Override
         protected Integer doInBackground(Void... params) {
             try {
                 if (!networkAvailable()) {
                     return 3;
                 }
 
                 arr = parseAddress(address);
                 if (arr == null || arr.length() == 0) {
                     return 1;
                 } else if (arr.length() > 1) {
                     return 2;
                 } else {
                     return 0;
                 }
             } catch (IOException ex) {
                 Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
             }
             return 0;
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             dialog.dismiss();
             switch (result) {
                 case 0:
                     new DataScraper(cancelable, force, forceStreetSearch).execute();
                     break;
                 case 1:
                     addressNotFound(cancelable, force, forceStreetSearch);
                     break;
                 case 2:
                     multiplePossibilities(arr, cancelable, force, forceStreetSearch);
                     break;
                 case 3:
                     noInternetConnectionAvailable();
                     break;
             }
         }
     }
 
     private void noInternetConnectionAvailable() {
         Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.noInternetConnection));
         b.setMessage(getString(R.string.needConnection));
         b.setCancelable(false);
         b.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 finish();
             }
         });
         b.show();
     }
 
     private class DataScraper extends AsyncTask<Void, Void, Integer> {
 
         private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
         private boolean cancelable;
         private boolean force;
         private boolean forceStreetSearch;
 
         private DataScraper(boolean cancelable, boolean force, boolean forceStreetSearch) {
             this.cancelable = cancelable;
             this.force = force;
             this.forceStreetSearch = forceStreetSearch;
         }
 
         @Override
         protected void onPreExecute() {
             dialog.setMessage(getString(R.string.loadingCalendar));
             dialog.show();
             dialog.setCancelable(false);
         }
 
         @Override
         protected Integer doInBackground(Void... params) {
             return scrapeData(force, forceStreetSearch);
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             dialog.dismiss();
             switch (result) {
                 case 1:
                     locationIsApartment(cancelable, force, forceStreetSearch);
                     break;
                 case 2:
                 case 3:
                 case 4:
                     loadingUnsuccessful();
                     break;
             }
             Toast.makeText(getApplicationContext(), getString(R.string.locationSet) + " " + UserModel.getInstance().getFormattedAddress(), Toast.LENGTH_LONG).show();
             createGUI();
             toggleNotifications(false);
         }
     }
 
     private void loadingUnsuccessful() {
         Builder b = new AlertDialog.Builder(this);
         b.setTitle(getString(R.string.error));
         b.setMessage(getString(R.string.loadingUnsuccessful));
         b.setCancelable(false);
         b.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 finish();
             }
         });
         b.show();
     }
 
     private boolean networkAvailable() {
         ConnectivityManager connectivityManager = (ConnectivityManager) DataModel.getInstance().getContainer().getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
         return activeNetworkInfo != null;
     }
 
     public JSONArray parseAddress(String address) throws IOException {
         String url = LocalConstants.GOOGLE_MAPS_API + "?";
         List<NameValuePair> params = new LinkedList<NameValuePair>();
         params.add(new BasicNameValuePair("address", address));
         params.add(new BasicNameValuePair("sensor", "false"));
         url += URLEncodedUtils.format(params, "utf-8");
 
         InputStream inp = getStream(url);
         BufferedReader reader = new BufferedReader(new InputStreamReader(inp, LocalConstants.ENCODING), 8);
         StringBuilder builder = new StringBuilder();
         builder.append(reader.readLine()).append("\n");
 
         String line;
         while ((line = reader.readLine()) != null) {
             builder.append(line).append("\n");
         }
         inp.close();
         String result = builder.toString();
 
         JSONArray arr = null;
         try {
             if (!result.isEmpty() && !result.equals("null\n")) {
                 JSONObject obj = new JSONObject(result);
                 if (obj.has("status") && obj.getString("status").equals("OK")) {
                     arr = obj.getJSONArray("results");
                     arr = filterOnGhent(arr);
                 }
             }
         } catch (JSONException ex) {
             Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             if (arr != null && arr.length() == 1) {
                 try {
                     submitAddress(arr.getJSONObject(0));
                 } catch (JSONException ex) {
                     Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (NullPointerException e) {
                     Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, e);
                 }
             }
             return arr;
         }
     }
 
     private void submitAddress(JSONObject addressobj) throws JSONException {
         JSONArray addressArr = addressobj.getJSONArray("address_components");
         for (int i = 0; i < addressArr.length(); i++) {
             JSONObject obj = addressArr.getJSONObject(i);
             String type = obj.getJSONArray("types").getString(0);
             if (type.equals("street_number")) {
                 int receivedNr = Integer.parseInt(obj.getString("long_name"));
                 UserModel.getInstance().setNr(Math.max(receivedNr, 1));
             } else if (type.equals("route")) {
                 UserModel.getInstance().setStreetname(obj.getString("long_name"));
             } else if (type.equals("sublocality")) {
                 UserModel.getInstance().setCity(obj.getString("long_name"));
             } else if (type.equals("postal_code")) {
                 UserModel.getInstance().setZipcode(Integer.parseInt(obj.getString("long_name")));
             }
         }
     }
 
     private void addressNotFound(final boolean cancelable, final boolean force, final boolean forceStreetSearch) {
         Builder b = new AlertDialog.Builder(this);
         b.setTitle(getString(R.string.invalidLocation));
         b.setMessage(getString(R.string.invalidLocationLong));
         b.setCancelable(cancelable);
         b.setPositiveButton(getString(R.string.tryAgain), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 promptUserLocation(cancelable, force, forceStreetSearch);
             }
         });
         if (cancelable) {
             b.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.dismiss();
                 }
             });
         } else {
             b.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     finish();
                 }
             });
         }
         b.show();
     }
 
     private void multiplePossibilities(final JSONArray arr, final boolean cancelable, final boolean force, final boolean forceStreetSearch) {
         final CharSequence[] possibilities = new CharSequence[arr.length()];
         for (int i = 0; i < arr.length(); i++) {
             try {
                 JSONObject obj = arr.getJSONObject(i);
                 possibilities[i] = obj.getString("formatted_address");
             } catch (JSONException ex) {
                 Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this)
                 .setTitle(getString(R.string.selectAddress))
                 .setCancelable(false)
                 .setSingleChoiceItems(possibilities, 0, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface d, int choice) {
                 try {
                     submitAddress(arr.getJSONObject(choice));
                     new DataScraper(cancelable, force, forceStreetSearch).execute();
                 } catch (JSONException ex) {
                     Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                 } finally {
                     d.dismiss();
                 }
             }
         });
         builder.create().show();
     }
 
     private void createGUI() {
         TableLayout table = (TableLayout) findViewById(R.id.main_table);
         table.removeViews(0, table.getChildCount());
 
         List<GarbageCollection> collections = DataModel.getInstance().getCollections();
         Iterator<GarbageCollection> it = collections.iterator();
         int i = 0;
         while (it.hasNext()) {
             GarbageCollection col = it.next();
             if (col.getDate().before(Calendar.getInstance().getTime())) {
                 continue;
             }
 
             addTableRowDate(col, i);
             addTableRowTypes(col, i);
             i++;
         }
     }
 
     private String beautifyDate(Date date) {
         Date today = Calendar.getInstance().getTime();
         int daysBetween = (int) ((date.getTime() - today.getTime()) / (24 * 60 * 60 * 1000));
 
         if (daysBetween <= 1) {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date)
                     + " ("
                     + getString(R.string.tomorrow)
                     + ")";
         } else if (daysBetween < 7) {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date)
                     + " ("
                     + getString(R.string.thisweek)
                     + " "
                     + LocalConstants.getDateFormatter(LocalConstants.DateFormatType.WEEKDAY, this).format(date)
                     + ")";
         } else if (daysBetween < 14) {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date)
                     + " ("
                     + getString(R.string.nextweek)
                     + " "
                     + LocalConstants.getDateFormatter(LocalConstants.DateFormatType.WEEKDAY, this).format(date)
                     + ")";
         } else {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date);
         }
     }
 
     private void addTableRowTypes(GarbageCollection col, int rowNumber) {
         GarbageType[] types = col.getTypes();
         boolean rest, gft, pmd, pk, glas;
         rest = gft = pmd = pk = glas = false;
         for (GarbageType t : types) {
             switch (t) {
                 case REST:
                     rest = true;
                     break;
                 case GFT:
                     gft = true;
                     break;
                 case PMD:
                     pmd = true;
                     break;
                 case PK:
                     pk = true;
                     break;
                 case GLAS:
                     glas = true;
                     break;
             }
         }
 
         int backgroundColor = rowNumber % 2 == 0 ? LocalConstants.COLOR_TABLE_EVEN_ROW : LocalConstants.COLOR_TABLE_ODD_ROW;
 
         LayoutInflater inflater = getLayoutInflater();
         TableLayout tl = (TableLayout) findViewById(R.id.main_table);
         TableRow tr = (TableRow) inflater.inflate(R.layout.main_table_row_types, tl, false);
 
         TextView labelRest = (TextView) tr.findViewById(R.id.main_row_rest);
         labelRest.setText(rest ? GarbageType.REST.shortStrValue(this) : "");
         labelRest.setPadding(1, 5, 5, 5);
         labelRest.setBackgroundColor(rest ? GarbageType.REST.getColor(UserModel.getInstance().getSector().getType()) : backgroundColor);
 
         TextView labelGFT = (TextView) tr.findViewById(R.id.main_row_gft);
         labelGFT.setText(gft ? GarbageType.GFT.shortStrValue(this) : "");
         labelGFT.setPadding(1, 5, 5, 5);
         labelGFT.setBackgroundColor(gft ? GarbageType.GFT.getColor(UserModel.getInstance().getSector().getType()) : backgroundColor);
 
         TextView labelPMD = (TextView) tr.findViewById(R.id.main_row_pmd);
         labelPMD.setText(pmd ? GarbageType.PMD.shortStrValue(this) : "");
         labelPMD.setPadding(1, 5, 5, 5);
         labelPMD.setBackgroundColor(pmd ? GarbageType.PMD.getColor(UserModel.getInstance().getSector().getType()) : backgroundColor);
 
         TextView labelPK = (TextView) tr.findViewById(R.id.main_row_pk);
         labelPK.setText(pk ? GarbageType.PK.shortStrValue(this) : "");
         labelPK.setPadding(1, 5, 5, 5);
         labelPK.setBackgroundColor(pk ? GarbageType.PK.getColor(UserModel.getInstance().getSector().getType()) : backgroundColor);
 
         TextView labelGlas = (TextView) tr.findViewById(R.id.main_row_glas);
         labelGlas.setText(glas ? GarbageType.GLAS.shortStrValue(this) : "");
         labelGlas.setPadding(1, 5, 5, 5);
         labelGlas.setBackgroundColor(glas ? GarbageType.GLAS.getColor(UserModel.getInstance().getSector().getType()) : backgroundColor);
 
         tr.setBackgroundColor(backgroundColor);
         tr.setOnClickListener(new TableRowListener(col));
 
         tl.addView(tr);
     }
 
     private void addTableRowDate(GarbageCollection col, int rowNumber) {
         String date = beautifyDate(col.getDate());
 
         LayoutInflater inflater = getLayoutInflater();
         TableLayout tl = (TableLayout) findViewById(R.id.main_table);
         TableRow tr = (TableRow) inflater.inflate(R.layout.main_table_row_date, tl, false);
 
         TextView labelDate = (TextView) tr.findViewById(R.id.main_row_date);
         labelDate.setText(date);
         labelDate.setPadding(5, 5, 5, 5);
         labelDate.setTextColor(Color.BLACK);
 
         tr.setBackgroundColor(rowNumber % 2 == 0 ? LocalConstants.COLOR_TABLE_EVEN_ROW : LocalConstants.COLOR_TABLE_ODD_ROW);
         tr.setOnClickListener(new TableRowListener(col));
 
         tl.addView(tr);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.about:
                 final SpannableString msg = new SpannableString(getString(R.string.aboutMessage));
                 Linkify.addLinks(msg, Linkify.ALL);
 
                 Builder builder = new Builder(this);
                 builder.setIcon(R.drawable.about);
                 builder.setMessage(msg);
                 AlertDialog dialog = builder.create();
                 dialog.show();
 
                 ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                 return true;
             case R.id.newlocation:
                 promptUserLocation(true, false, true);
                 return true;
             case R.id.refresh:
                 new DataScraper(true, true, true).execute();
                 return true;
             case R.id.notif:
                 final CharSequence[] choices = new CharSequence[]{getString(R.string.on), getString(R.string.off)};
                 boolean initialValue = getSharedPreferences("PREFERENCE", Activity.MODE_PRIVATE).getBoolean(LocalConstants.CacheName.NOTIFICATION.toString(), false);
                 AlertDialog.Builder b = new AlertDialog.Builder(this)
                         .setTitle(getString(R.string.notif))
                         .setCancelable(true)
                         .setSingleChoiceItems(choices, initialValue ? 0 : 1, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface d, int choice) {
                         toggleNotifications(choice == 0);
                         getSharedPreferences("PREFERENCE", Activity.MODE_PRIVATE)
                                 .edit()
                                 .putBoolean(LocalConstants.CacheName.NOTIFICATION.toString(), (choice == 0))
                                 .commit();
                         d.dismiss();
                     }
                 });
                 b.create().show();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
