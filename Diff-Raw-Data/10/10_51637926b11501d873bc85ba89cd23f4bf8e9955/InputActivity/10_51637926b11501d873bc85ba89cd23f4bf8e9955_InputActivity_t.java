 package ru.elifantiev.rallyresults;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.*;
 import android.graphics.PorterDuff;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.util.Pair;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.*;
 import ru.elifantiev.rallyresults.infrastructure.RallySection;
 import ru.elifantiev.rallyresults.infrastructure.StatRecord;
 import ru.elifantiev.rallyresults.service.StatPoolService;
 
 import java.util.*;
 
 public class InputActivity extends Activity implements
         ServiceConnection,
         StatPoolService.OnStatRefreshListener,
         View.OnKeyListener {
 
     private int competitionId, sectionId;
     private Spinner spnStats, spnNumber;
     private ProgressDialog progress;
     private RallyWebService svc;
     private TextView txtStartHour, txtStartMinute,
             txtFinishHour, txtFinishMinute, txtFinishSecond, txtFinishMSecond,
             txtIndicator;
     private LinkedHashMap<String, StatRecord> statHash = new LinkedHashMap<String, StatRecord>();
     private StatPoolService boundService = null;
     private HashMap<Integer, Pair<Integer, View>> viewBounds = new HashMap<Integer, Pair<Integer, View>>();
 
     public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
         boundService = ((StatPoolService.StatPoolBinder) iBinder).getService();
         boundService.setOnStatRefreshListener(this);
     }
 
     public void onServiceDisconnected(ComponentName componentName) {
         boundService = null;
     }
 
     @Override
     protected void onStart() {
         startService(new Intent(this, StatPoolService.class));
         bindService(
                 new Intent(InputActivity.this, StatPoolService.class),
                 InputActivity.this,
                 Context.BIND_AUTO_CREATE);
         super.onStart();    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.inputscreen);
 
         Intent callingIntent = getIntent();
         spnStats = (Spinner) findViewById(R.id.spnItemToEdit);
 
         competitionId = callingIntent.getExtras().getInt("competitionId");
         sectionId = callingIntent.getExtras().getInt("sectionId");
 
         SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
 
         spnNumber = (Spinner) findViewById(R.id.spnNumber);
         txtStartHour = ((TextView) findViewById(R.id.startHour));
         txtStartMinute = ((TextView) findViewById(R.id.startMinute));
         txtFinishHour = ((TextView) findViewById(R.id.finishHour));
         txtFinishMinute = ((TextView) findViewById(R.id.finishMinute));
         txtFinishSecond = ((TextView) findViewById(R.id.finishSecond));
         txtFinishMSecond = ((TextView) findViewById(R.id.finishMillisecond));
         txtIndicator = (TextView)findViewById(R.id.txtIndicator);
         
         viewBounds.put(R.id.startHour, new Pair<Integer, View>(23, txtStartMinute));
         viewBounds.put(R.id.startMinute, new Pair<Integer, View>(59, txtFinishHour));
         viewBounds.put(R.id.finishHour, new Pair<Integer, View>(23, txtFinishMinute));
         viewBounds.put(R.id.finishMinute, new Pair<Integer, View>(59, txtFinishSecond));
         viewBounds.put(R.id.finishSecond, new Pair<Integer, View>(59, txtFinishMSecond));
 
 
         svc = new RallyWebService(getString(R.string.wsRootUrl),
                 prefs.getString("login", ""),
                 prefs.getString("password", ""));
 
         txtStartHour.setOnKeyListener(this);
         txtStartMinute.setOnKeyListener(this);
         txtFinishHour.setOnKeyListener(this);
         txtFinishMinute.setOnKeyListener(this);
         txtFinishSecond.setOnKeyListener(this);
         txtFinishMSecond.setOnKeyListener(this);
 
         findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 HashMap<String, String> statItem = (HashMap<String, String>) spnStats.getSelectedItem();
                 StatRecord selected = statHash.get(statItem.get("number"));
 
                 spnNumber.setSelection(spnStats.getSelectedItemPosition());
 
                 txtStartHour.setText(selected.getStartHour());
                 txtStartMinute.setText(selected.getStartMinute());
 
                 txtFinishHour.setText(selected.getFinishHour());
                 txtFinishMinute.setText(selected.getFinishMinute());
                 txtFinishSecond.setText(selected.getFinishSecond());
                 txtFinishMSecond.setText(selected.getFinishMSecond());
 
                 recalcIndicator();
             }
         });
 
         findViewById(R.id.sendResult).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 boolean isError;
                 StatRecord rec = new StatRecord(
                         (String) spnNumber.getSelectedItem(),
                         competitionId,
                         sectionId);
 
                 isError = isIncorrectInput(true);
 
                 if (!isError) {
                     try {
                         rec.setStart(
                                 txtStartHour.getText().toString(),
                                 txtStartMinute.getText().toString(),
                                 "0");
                         rec.setFinish(
                                 txtFinishHour.getText().toString(),
                                 txtFinishMinute.getText().toString(),
                                 txtFinishSecond.getText().toString(),
                                 txtFinishMSecond.getText().toString());
 
                         boundService.uploadStatRecord(rec);
                     } catch (StatRecord.ParseException e) {
                         Log.d("InputActivity", "Format parse: " + e.getMessage());
                     }
                     clearInputs();
                 }
             }
         });
 
         new AsyncLoadSection().execute();
     }
 
     private boolean isIncorrectInput(boolean doMark) {
         boolean isError;
         isError = checkField(txtStartHour, 23, doMark);
         isError |= checkField(txtStartMinute, 59, doMark);
 
         isError |= checkField(txtFinishHour, 23, doMark);
         isError |= checkField(txtFinishMinute, 59, doMark);
         isError |= checkField(txtFinishSecond, 59, doMark);
         isError |= checkField(txtFinishMSecond, 9, doMark);
         return isError;
     }
 
     @Override
     protected void onDestroy() {
        super.onDestroy();
         viewBounds.clear();
         viewBounds = null;
     }
 
     private boolean checkField(TextView control, int maxVal, boolean doMark) {
         int value;
         boolean isError = true;
         try {
             value = new Integer(control.getText().toString());
             isError = value > maxVal;
         } catch (NumberFormatException e) {
             // ignore
         }
         markError(control, isError && doMark);
         return isError;
     }
 
     private void markError(View control, boolean error) {
         if (error)
             control.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
         else
             control.getBackground().setColorFilter(null);
         control.invalidate();
     }
 
     private void clearInputs() {
         String h = String.valueOf(new Date().getHours());
         spnNumber.setSelection(0);
         txtStartHour.setText(h);
         txtStartMinute.setText("");
         txtFinishHour.setText(h);
         txtFinishMinute.setText("");
         txtFinishSecond.setText("");
         txtFinishMSecond.setText("");
         txtIndicator.setText("--:--.-");
     }
 
     private void fillNumbers(RallySection rallySection) {
         List<StatRecord> nStats = rallySection.getStats();
         List<String> nValues = new ArrayList<String>(nStats.size());
 
         for (StatRecord record : nStats) {
             nValues.add(record.getNumber());
             statHash.put(record.getNumber(), null);
         }
 
         ArrayAdapter<String> na = new ArrayAdapter<String>(
                 InputActivity.this,
                 android.R.layout.simple_spinner_item,
                 nValues);
         na.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spnNumber.setAdapter(na);
     }
 
     public void onStatRefresh(RallySection rallySection) {
 
         if(rallySection.getCompetitionId() != competitionId || rallySection.getSectionId() != sectionId)
             return;
 
         String[] keys = new String[]{"number", "start", "finish"};
         int[] views = new int[]{R.id.statNumber, R.id.statStart, R.id.statFinish};
         List<StatRecord> stats = rallySection.getStats();
         List<HashMap<String, String>> values = new ArrayList<HashMap<String, String>>(stats.size() + 1);
 
         for (StatRecord record : stats) {
             HashMap<String, String> recItem = new HashMap<String, String>(3);
 
             recItem.put("number", record.getNumber());
             recItem.put("start", record.getStart());
             recItem.put("finish", record.getFinish());
             values.add(recItem);
             statHash.put(record.getNumber(), record);
         }
 
         setTitle(rallySection.getCompetitionName() + ", " + rallySection.getSectionName());
         spnStats.setAdapter(
                 new SimpleAdapter(InputActivity.this, values, R.layout.statlistitem, keys, views)
         );
     }
 
     @Override
     public boolean onKey(View view, int i, KeyEvent keyEvent) {
         if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
 
             if(!keyEvent.isPrintingKey()) {
                 if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                     ((EditText)view).setText("");
                     recalcIndicator();
                     return true;
                 }
                 return false;
             }
 
             if(view == txtFinishMSecond)
                 return false; // do not monitor down event for tneth-of-seconds
 
             EditText txt = (EditText) view;
             String s = txt.getText().toString();
             s += keyEvent.getNumber();
             Integer val = Integer.valueOf(s);
             Pair<Integer, View> p = viewBounds.get(view.getId());
             if(p != null) {
                 Integer bound = p.first;
 
                 if(val > bound)
                     return true;
             }
         }
         
         if(keyEvent.getAction() == KeyEvent.ACTION_UP) {
             EditText txt = (EditText) view;
             String s = txt.getText().toString();
             if((keyEvent.isPrintingKey() && s.length() == 2) || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                 Pair<Integer, View> p = viewBounds.get(view.getId());
                 if(p != null) {
                     View v = p.second;
                     if(v != null) {
                         v.requestFocus();
                         recalcIndicator();
                         return true;
                     }
                 }
             }
             
             if(keyEvent.isPrintingKey())
                 recalcIndicator();
 
         }
         
         return false;
     }
 
     private void recalcIndicator() {
         if(!isIncorrectInput(false)) {
             Date s = new Date(), f = new Date(), d;
             s.setHours(intVal(txtStartHour));
             s.setMinutes(intVal(txtStartMinute));
             s.setSeconds(0);
             f.setHours(intVal(txtFinishHour));
             f.setMinutes(intVal(txtFinishMinute));
             f.setSeconds(intVal(txtFinishSecond));
             f.setTime(f.getTime() +intVal(txtFinishMSecond) * 10);
             
             if(f.before(s)) {
                 txtIndicator.setText("--:--.-");
                 return;
             }
            
             d = new Date(f.getTime() - s.getTime());
             long ms = (d.getTime() - d.getMinutes() * 60 * 1000 - d.getSeconds() * 1000) / 10;
             txtIndicator.setText(String.format("%02d:%02d.%01d", d.getMinutes(), d.getSeconds(), ms));
         } else
             txtIndicator.setText("--:--.-");
     }
 
     private Integer intVal(TextView view) {
         try {
             return Integer.valueOf(view.getText().toString());
         } catch (NumberFormatException e) {
             return 0;
         }
     }
 
 
     abstract public class AsyncGetStat<T> extends AsyncTask<T, Void, RallySection> {
 
         @Override
         protected void onPreExecute() {
             progress = ProgressDialog.show(InputActivity.this, "", getString(R.string.loadingSection), true, false);
         }
 
         @Override
         protected void onPostExecute(RallySection rallySection) {
             progress.dismiss();
             fillNumbers(rallySection);
             onStatRefresh(rallySection);
         }
     }
 
     public class AsyncLoadSection extends AsyncGetStat<Void> {
 
         @Override
         protected RallySection doInBackground(Void... strings) {
             return svc.getSectionStats(competitionId, sectionId);
         }
     }
 }
