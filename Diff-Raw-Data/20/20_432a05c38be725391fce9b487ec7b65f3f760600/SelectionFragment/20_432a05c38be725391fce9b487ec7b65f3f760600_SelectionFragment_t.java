 
 package squeak.app;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.SystemClock;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.text.SpannableStringBuilder;
 import android.text.style.AbsoluteSizeSpan;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 import android.widget.ViewSwitcher;
 
 import com.example.mh.AssetsLookup;
 import com.example.mh.JsonCallbackCommand;
 import com.facebook.Session;
 
 import example.squeak.R;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import squeak.content.ItemAssetLoader;
 import squeak.model.Assets;
 import squeak.model.Item;
 import squeak.os.HttpPostTask;
 
 import java.net.HttpURLConnection;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 public class SelectionFragment extends Fragment implements
         LoaderManager.LoaderCallbacks<SparseArray<Item>>, ItemSelectFragment.Listener {
     private static final String BAIT_SELECT_TAG = "BAIT_SELECT";
     private static final String TRINKET_SELECT_TAG = "TRINKET_SELECT";
 
     private static final String TAG = "SelectionFragment";
 
     private CountDownTimer timer;
     private long millisUntilHorn;
 
     private ViewFlipper viewFlipper;
 
     private int lastKnownBaitId;
     private int lastKnownBaitQuantity;
     
     private int lastKnownTrinketId;
     private int lastKnownTrinketQuantity;
 
     private int lastKnownLocationId;
 
     private int lastKnownTitleId;
 
     private long pauseTime;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         getActivity().getSupportLoaderManager().initLoader(ItemAssetLoader.LOADER_ID, null, this)
                 .forceLoad();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         Log.d(TAG, "on create view");
 
         View view = inflater.inflate(R.layout.selection, container, false);
         debugToScreen(view, getClass().getName() + " createview");
         ConnectivityManager connMgr = (ConnectivityManager) getActivity()
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             // fetch data
             viewFlipper = ((ViewFlipper) view
                     .findViewById(R.id.viewFlipperHorn));
             viewFlipper.setDisplayedChild(0);
             ProgressBar progressBar = (ProgressBar) view
                     .findViewById(R.id.progressBar1);
             progressBar.setIndeterminate(false);
             progressBar.setProgress(0);
             makeGameVersionRequest();
             debugToScreen(view, "requesting game info");
         } else {
             // display error
             TextView timer = (TextView) view.findViewById(R.id.textViewTimer);
             if (!timer.isShown()) {
                 viewFlipper = ((ViewFlipper) view
                         .findViewById(R.id.viewFlipperHorn));
                 viewFlipper.setDisplayedChild(1);
             }
             timer.setText("No network connection");
         }
 
         Button buttonHorn = (Button) view.findViewById(R.id.buttonHorn);
         buttonHorn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d("foo", "horn button clicked");
                 makeSoundHornRequest();
                 viewFlipper = ((ViewFlipper) getActivity().findViewById(
                         R.id.viewFlipperHorn));
                 if (viewFlipper != null) {
                     ProgressBar progressBar = (ProgressBar) getActivity()
                             .findViewById(R.id.progressBar1);
                     progressBar.setIndeterminate(true);
                     // show progress bar
                     viewFlipper.setDisplayedChild(0);
                 }
             }
         });
 
         ViewSwitcher cheeseViewSwitcher = (ViewSwitcher) view
                 .findViewById(R.id.viewSwitcherCheese);
         cheeseViewSwitcher.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 ItemSelectFragment dialog = new ItemSelectFragment();
                 dialog.setTargetFragment(SelectionFragment.this, 0);
                 dialog.show(getFragmentManager(), BAIT_SELECT_TAG);
             }
         });
 
         return view;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Log.d(TAG, "on resume view");
         debugToScreen(getView(), getClass().getName() + " resumed");
 
         if (timer != null) {
             this.millisUntilHorn -= System.currentTimeMillis() - pauseTime;
             timer = new HornCountDownTimer(this.millisUntilHorn, 800).start();
         }
     }
 
     @Override
     public void onPause() {
         this.pauseTime = System.currentTimeMillis();
         if (timer != null) {
             timer.cancel();
         }
         debugToScreen(getView(), getClass().getName() + " paused");
 
         super.onPause();
     }
 
     @Override
     public void onStop() {
         debugToScreen(getView(), getClass().getName() + " stopped");
         super.onStop();
     }
 
     @Override
     public void onStart() {
         super.onStart();
         debugToScreen(getView(), getClass().getName() + " started");
     }
 
     public void onTileSelected(View v) {
         switch (v.getId()) {
             case R.id.viewSwitcherCheese:
                 DialogFragment dialog = new ItemSelectFragment();
                 dialog.show(getFragmentManager(), "ItemSelectFragment");
                 break;
 
             default:
                 break;
         }
     }
 
     private void makeGameVersionRequest() {
         Map<String, String> params = new HashMap<String, String>();
         params.put("url", "https://www.mousehuntgame.com/api/info");
         params.put("game_version", "null");
         final HttpPostTask task = new HttpPostTask(new JsonCallbackCommand() {
             @Override
             public void run(JSONObject o) {
                 try {
                     Log.i(TAG, o.toString());
                     int responseCode = o.getInt("http-response");
                     if (responseCode == HttpURLConnection.HTTP_OK) {
                         MainActivity.gameVersion = o.getString("game_version");
                         MainActivity.v = o.getString("v");
                         MainActivity.minV = o.getString("min_v");
 
                         ProgressBar progressBar = (ProgressBar) getView()
                                 .findViewById(R.id.progressBar1);
                         progressBar.incrementProgressBy(50);
                         makeUserDataRequest();
                     } else {
                         TextView timer = (TextView) getView().findViewById(
                                 R.id.textViewTimer);
                         if (!timer.isShown()) {
                             viewFlipper = ((ViewFlipper) getView()
                                     .findViewById(R.id.viewFlipperHorn));
                             viewFlipper.setDisplayedChild(1);
                         }
                         timer.setText("HTTP " + responseCode);
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });
         task.execute(params);
     }
 
     private void makeUserDataRequest() {
         Map<String, String> params = new HashMap<String, String>();
         Session s = Session.getActiveSession();
         if (s != null) {
             params.clear();
             params.put("url", "https://www.mousehuntgame.com/api/get/user/me");
             params.put("v", MainActivity.v);
             params.put("client_id", "Cordova:Android");
             params.put("client_version", getString(R.string.client_version));
             params.put("game_version", MainActivity.gameVersion);
             params.put("access_token", s.getAccessToken());
             final HttpPostTask task = new HttpPostTask(
                     new JsonCallbackCommand() {
                         @Override
                         public void run(JSONObject o) {
                             try {
                                 ProgressBar progressBar = (ProgressBar) getView()
                                         .findViewById(R.id.progressBar1);
                                 progressBar.incrementProgressBy(50);
                                 viewFlipper = ((ViewFlipper) getView()
                                         .findViewById(R.id.viewFlipperHorn));
                                 viewFlipper.setDisplayedChild(1);
                                 Log.d(TAG, o.toString());
 
                                 int responseCode = o.getInt("http-response");
                                 if (responseCode == HttpURLConnection.HTTP_OK) {
                                     millisUntilHorn = o
                                             .getLong("next_activeturn_seconds") * 1000;
                                     long millisecsElapsed = o
                                             .getLong("http-elapsed");
                                     millisUntilHorn -= millisecsElapsed;
                                     timer = new HornCountDownTimer(millisUntilHorn,
                                             800).start();
 
                                     // scheduleNotification(millisUntilHorn);
                                     debugToScreen(getView(),
                                             o.getString("progress-log"));
                                     debugToScreen(getView(),
                                             "user data request took "
                                                     + millisecsElapsed + " ms");
                                     Assets.INSTANCE.updateCachedItems(o.optJSONArray("inventory"));
                                     updateTiles(o);
                                 } else {
                                     TextView timer = (TextView) getView().findViewById(
                                             R.id.textViewTimer);
                                     if (!timer.isShown()) {
                                         viewFlipper = ((ViewFlipper) getView()
                                                 .findViewById(R.id.viewFlipperHorn));
                                         viewFlipper.setDisplayedChild(1);
                                     }
                                     timer.setText("HTTP " + responseCode);
                                 }
                             } catch (JSONException e) {
                                 e.printStackTrace();
                             }
                         }
                     });
             debugToScreen(getView(), "requesting user data");
             task.execute(params);
         }
     }
 
     private void makeSoundHornRequest() {
         Map<String, String> params = new HashMap<String, String>();
         Session s = Session.getActiveSession();
         if (s != null) {
             params.clear();
             params.put("url",
                     "https://www.mousehuntgame.com/api/action/turn/me");
             params.put("v", MainActivity.v);
             params.put("client_id", "Cordova:Android");
             params.put("client_version", getString(R.string.client_version));
             params.put("game_version", MainActivity.gameVersion);
             params.put("access_token", s.getAccessToken());
             final HttpPostTask task = new HttpPostTask(
                     new JsonCallbackCommand() {
                         @Override
                         public void run(JSONObject o) {
                             try {
                                 Log.i(TAG, o.toString());
                                 debugToScreen(getView(),
                                         o.getString("progress-log"));
 
                                 JSONObject user = o.getJSONObject("user");
                                 millisUntilHorn = user
                                         .getLong("next_activeturn_seconds") * 1000;
                                 long millisecsElapsed = o
                                         .getLong("http-elapsed");
                                 millisUntilHorn -= millisecsElapsed;
                                 timer = new HornCountDownTimer(millisUntilHorn,
                                         800).start();
 
                                 // scheduleNotification(millisUntilHorn);
                                 debugToScreen(getView(),
                                         "user data request took "
                                                 + millisecsElapsed + " ms");
                                 Assets.INSTANCE.updateCachedItems(o.optJSONArray("inventory"));
                                 updateTiles(user);
                             } catch (JSONException e) {
                                 e.printStackTrace();
                                 // refresh. sounding horn when timer not up
                                 // returns an empty json object from server
                                 debugToScreen(getView(),
                                         "horn sounded elsewhere...refreshing");
                                 makeUserDataRequest();
                             }
                         }
                     });
             debugToScreen(getView(), "making horn request");
             task.execute(params);
         }
     }
 
     private void scheduleNotification(long milliseconds) {
         AlarmManager am = (AlarmManager) getActivity().getSystemService(
                 Context.ALARM_SERVICE);
         am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
                 + milliseconds, PendingIntent.getActivity(getActivity(), -1,
                 new Intent(getActivity(), MainActivity.class),
                 PendingIntent.FLAG_UPDATE_CURRENT));
     }
 
     private void updateTiles(JSONObject o) {
         JSONObject trap = o.optJSONObject("trap");
         if (trap == null) {
             return;
         }
         int baitId = trap.optInt("bait_id", -1);
         if (baitId != -1) {
             Item bait = Assets.INSTANCE.getItems().get(baitId);
             int baitQuantity = bait.getQuantity();
             // only update tile if bait type or quantity has changed
             if (baitId != this.lastKnownBaitId || baitQuantity != this.lastKnownBaitQuantity) {
                 this.lastKnownBaitId = baitId;
                 this.lastKnownBaitQuantity = baitQuantity;
                 String baitName = bait.getName();
 
                 ViewSwitcher cheeseViewSwitcher = (ViewSwitcher) getView()
                         .findViewById(R.id.viewSwitcherCheese);
                 LinearLayout l = (LinearLayout) cheeseViewSwitcher
                         .getNextView();
                 TextView quantity = (TextView) l.getChildAt(0);
                 quantity.setText(Integer.toString(baitQuantity));
                 TextView name = (TextView) l.getChildAt(1);
                 name.setText(baitName);
                 
                 cheeseViewSwitcher.showNext();
             }
         }
         int weaponId = trap.optInt("weapon_id", -1);
         if (weaponId != -1) {
             Button trapButton = (Button) getView()
                     .findViewById(R.id.buttonTrap);
             String weaponName = Assets.INSTANCE.getItems().get(weaponId).getName();
             trapButton.setText(weaponName == null ? Integer.toString(weaponId)
                     : weaponName);
         }
         int baseId = trap.optInt("base_id", -1);
         if (baseId != -1) {
             Button baseButton = (Button) getView()
                     .findViewById(R.id.buttonBase);
             String baseName = Assets.INSTANCE.getItems().get(baseId).getName();
             baseButton.setText(baseName == null ? Integer.toString(baseId)
                     : baseName);
         }
         int trinketId = trap.optInt("trinket_id", -1);
         if (trinketId != -1) {
             Item trinket = Assets.INSTANCE.getItems().get(trinketId);
             int trinketQuantity = trinket.getQuantity();
             if (trinketId != this.lastKnownTrinketId) {
                 String trinketName = Assets.INSTANCE.getItems().get(trinketId).getName();
                 
                 ViewSwitcher trinketViewSwitcher = (ViewSwitcher) getView().findViewById(
                         R.id.viewSwitcherCharm);
                 LinearLayout l = (LinearLayout) trinketViewSwitcher
                         .getNextView();
                 TextView quantity = (TextView) l.getChildAt(0);
                 quantity.setText(Integer.toString(trinketQuantity));
                 TextView name = (TextView) l.getChildAt(1);
                 name.setText(trinketName);
                 
                 trinketViewSwitcher.showNext();
             }
         }
         // journal
         JSONArray journals = o.optJSONArray("journals");
         if (journals != null) {
             JSONObject entry = journals.optJSONObject(0);
             if (entry != null) {
                 String entryDisplayText = formatJournalEntry(entry);
                 TextView journalTextView = (TextView) getView().findViewById(
                         R.id.textViewJournal);
                 journalTextView.setText(entryDisplayText);
             }
         }
 
         updateStatus(o);
     }
     
     private String formatJournalEntry(JSONObject entry) {
         SpannableStringBuilder sb = new SpannableStringBuilder();
         int index = 0;
         String entryClassname = entry.optString("class_name");
         Log.d(TAG, entryClassname);
         String[] type = entryClassname.split("\\s");
         if (entryClassname.contains("catchuccessloot")) {
             String displayName = "Caught";
             sb.append(displayName);
             sb.setSpan(new AbsoluteSizeSpan(24, true), index, index
                     + displayName.length(), 0);
             index += displayName.length();
             sb.append("\n");
             index++;
             JSONObject tokens = entry.optJSONObject("tokens");
             String mouseName = tokens.optString("mouse");
             sb.append(mouseName);
             index += mouseName.length();
             sb.append("\n");
             index++;
             String loot = tokens.optString("loot");
             sb.append(loot);
        } else if (entryClassname.contains("catchsuccess")) {
            String displayName = "Caught";
             sb.append(displayName);
             sb.setSpan(new AbsoluteSizeSpan(24, true), index, index
                     + displayName.length(), 0);
             index += displayName.length();
             sb.append("\n");
             index++;
            JSONObject tokens = entry.optJSONObject("tokens");
            String mouseName = tokens.optString("mouse");
            sb.append(mouseName);
            index += mouseName.length();
        } else if (entryClassname.contains("attractionfailurestale")) {
            String displayName = "Failed to attract - Stale";
             sb.append(displayName);
             sb.setSpan(new AbsoluteSizeSpan(24, true), index, index
                     + displayName.length(), 0);
             index += displayName.length();
             sb.append("\n");
             index++;
        } else if (entryClassname.contains("catchfailure")) {
             String displayName = "Failed to catch";
             sb.append(displayName);
             sb.setSpan(new AbsoluteSizeSpan(24, true), index, index
                     + displayName.length(), 0);
             index += displayName.length();
             sb.append("\n");
             index++;
             JSONObject tokens = entry.optJSONObject("tokens");
             String mouseName = tokens.optString("mouse");
             sb.append(mouseName);
             index += mouseName.length();
         } else {
             // class_name = potionuse
             // dailyreward gift captchasolved
             sb.append(type[0]);
             sb.setSpan(new AbsoluteSizeSpan(24, true), index, index
                     + type[0].length(), 0);
             sb.append("\n");
             index++;
         }
         return sb.toString();
     }
 
     private void updateStatus(JSONObject o) {
         SpannableStringBuilder sb = new SpannableStringBuilder();
         // location
         try {
             int locationId = o.getInt("environment_id");
             String locationName = Integer.toString(locationId);
             if (locationId != this.lastKnownLocationId) {
                 this.lastKnownLocationId = locationId;
             }
             long elapsedTime = System.currentTimeMillis();
             locationName = AssetsLookup.getEnvironmentName(getResources()
                     .getAssets(), locationId);
             elapsedTime = System.currentTimeMillis() - elapsedTime;
             debugToScreen(getView(), "read env asset in " + elapsedTime
                     + " ms");
             sb.append(locationName);
             sb.append("\n");
         } catch (JSONException e) {
             e.printStackTrace();
         }
         NumberFormat formatter = NumberFormat.getIntegerInstance();
         try {
             int gold = o.getInt("gold");
             sb.append("Gold: " + formatter.format(gold));
             sb.append("\n");
         } catch (JSONException e) {
             e.printStackTrace();
         }
         try {
             int points = o.getInt("points");
             sb.append("Points: " + formatter.format(points));
             sb.append("\n");
         } catch (JSONException e) {
             e.printStackTrace();
         }
         try {
             int titleId = o.getInt("title_id");
             String titleName = Integer.toString(titleId);
             if (titleId != this.lastKnownTitleId) {
                 this.lastKnownTitleId = titleId;
             }
             String gender = o.getString("gender");
             long elapsedTime = System.currentTimeMillis();
             titleName = AssetsLookup.getTitleName(getResources()
                     .getAssets(), titleId, gender);
             elapsedTime = System.currentTimeMillis() - elapsedTime;
             debugToScreen(getView(), "read title asset in " + elapsedTime
                     + " ms");
             sb.append(titleName);
             double titlePercent = o.getDouble("title_percentage");
             sb.append(" (" + titlePercent + "%)");
             sb.append("\n");
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         TextView journalTextView = (TextView) getView().findViewById(
                 R.id.textViewStatus);
         journalTextView.setText(sb);
     }
 
     private void debugToScreen(View view, String s) {
         if (view != null) {
             TextView v = ((TextView) view.findViewById(R.id.textViewDebug));
             v.append(s + "\n");
         }
     }
 
     @Override
     public Loader<SparseArray<Item>> onCreateLoader(int id, Bundle args) {
         Loader<SparseArray<Item>> loader = null;
         switch (id) {
             case ItemAssetLoader.LOADER_ID:
                 loader = new ItemAssetLoader(getActivity());
                 break;
 
             default:
                 break;
         }
         return loader;
     }
 
     @Override
     public void onLoadFinished(Loader<SparseArray<Item>> loader,
             SparseArray<Item> data) {
         SparseArray<Item> inMemoryItems = Assets.INSTANCE.getItems();
         int dataSize = data.size();
         for (int i = 0; i < dataSize; i++) {
             int key = data.keyAt(i);
             Item inMemoryItem = inMemoryItems.get(key);
             if (inMemoryItem == null) {
                 // item doesn't exist in memory yet
                 inMemoryItems.append(key, data.valueAt(i));
             }
         }
         debugToScreen(getView(), "loaded items assets");
     }
 
     @Override
     public void onLoaderReset(Loader<SparseArray<Item>> loader) {
 
     }
 
     private class HornCountDownTimer extends CountDownTimer {
         private final String timerDisplayFormat = getResources().getString(
                 R.string.text_horn_timer);
 
         public HornCountDownTimer(long millisInFuture, long countDownInterval) {
             super(millisInFuture, countDownInterval);
         }
 
         @Override
         public void onTick(long millisUntilFinished) {
             millisUntilHorn = millisUntilFinished;
             View v = getView();
             if (v != null) {
                 long seconds = millisUntilHorn / 1000 % 60;
                 long minutes = millisUntilHorn / 1000 / 60;
                 String text = String.format(timerDisplayFormat, minutes,
                         seconds);
 
                 TextView timer = (TextView) v.findViewById(R.id.textViewTimer);
                 if (!timer.isShown()) {
                     viewFlipper = ((ViewFlipper) getView().findViewById(
                             R.id.viewFlipperHorn));
                     viewFlipper.setDisplayedChild(1);
                 }
                 timer.setText(text);
             }
         }
 
         @Override
         public void onFinish() {
             Log.d("foo", "on finish");
             View v = getView();
             if (v != null) {
                 viewFlipper = ((ViewFlipper) getView().findViewById(
                         R.id.viewFlipperHorn));
                 viewFlipper.setDisplayedChild(2);
             }
         }
     }
 
     @Override
     public void onPositiveClick(DialogFragment dialog, long itemId) {
         String tag = dialog.getTag();
         if (BAIT_SELECT_TAG.equals(tag)) {
             if (itemId != -1) {
                 // make http request to arm bait
                 makeArmBaitRequest((int) itemId);
             }
         }
     }
 
     @Override
     public void onNegativeClick(DialogFragment dialog) {
         // TODO Auto-generated method stub
 
     }
 
     private void makeArmBaitRequest(int baitId) {
         Map<String, String> params = new HashMap<String, String>();
         Session s = Session.getActiveSession();
         if (s != null) {
             params.clear();
             params.put("url",
                     "https://www.mousehuntgame.com/api/action/arm/" + baitId);
             params.put("v", MainActivity.v);
             params.put("client_id", "Cordova:Android");
             params.put("client_version", getString(R.string.client_version));
             params.put("game_version", MainActivity.gameVersion);
             params.put("access_token", s.getAccessToken());
             final HttpPostTask task = new HttpPostTask(
                     new JsonCallbackCommand() {
                         @Override
                         public void run(JSONObject o) {
                             try {
                                 Log.i(TAG, o.toString());
                                 debugToScreen(getView(),
                                         o.getString("progress-log"));
 
                                 JSONObject user = o.getJSONObject("user");
                                 millisUntilHorn = user
                                         .getLong("next_activeturn_seconds") * 1000;
                                 long millisecsElapsed = o
                                         .getLong("http-elapsed");
                                 millisUntilHorn -= millisecsElapsed;
                                 timer = new HornCountDownTimer(millisUntilHorn,
                                         800).start();
 
                                 // scheduleNotification(millisUntilHorn);
                                 debugToScreen(getView(),
                                         "arm bait request took "
                                                 + millisecsElapsed + " ms");
                                 updateTiles(user);
                                 Assets.INSTANCE.updateCachedItems(user.optJSONArray("inventory"));
                             } catch (JSONException e) {
                                 e.printStackTrace();
                                 // refresh. sounding horn when timer not up
                                 // returns an empty json object from server
                                 debugToScreen(getView(),
                                         "horn sounded elsewhere...refreshing");
                                 makeUserDataRequest();
                             }
                         }
                     });
             debugToScreen(getView(), "making horn request");
             task.execute(params);
         }
     }
 }
