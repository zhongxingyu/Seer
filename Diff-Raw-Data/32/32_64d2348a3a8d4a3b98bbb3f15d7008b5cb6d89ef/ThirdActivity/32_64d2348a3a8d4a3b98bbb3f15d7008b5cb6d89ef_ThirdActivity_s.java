 package au.id.swords.third;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.Context;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.TableRow;
 import android.widget.ImageButton;
 import android.widget.RadioButton;
 import android.widget.AdapterView;
 import android.widget.TableLayout;
 import android.widget.ProgressBar;
 import android.widget.ViewFlipper;
 import android.widget.ArrayAdapter;
 import android.widget.SimpleCursorAdapter;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.database.Cursor;
 import java.util.Random;
 import java.util.Vector;
 
 public class ThirdActivity extends Activity
 {
     ThirdDb mDb;
     ThirdConfig mConfig;
     boolean mConfigImmutable = false;
     DiceCounter[] mDice;
     DxCounter mDx;
     ButtonCounter mMul;
     ButtonCounter mMod;
     TableLayout mLog;
     ViewFlipper mFlip;
     RadioButton mFlipPresets;
     RadioButton mFlipResults;
     SimpleCursorAdapter mProfiles;
     ArrayAdapter<ThirdConfig> mPresets;
     Cursor mProfileCursor;
     Cursor mPresetCursor;
     Spinner mProfileView;
     ListView mPresetView;
     TextView mResult;
     Vector<TextView> mResultLog;
     Integer mProfile;
     Random mRNG;
 
     private static final int ACT_NAME_PRESET = 0;
     private static final int ACT_NAME_PROFILE = 1;
     private static final int ACT_DEL_PROFILE = 2;
 
     private static final int ADD_PRESET = Menu.FIRST;
     private static final int ADD_PROFILE = Menu.FIRST + 1;
     private static final int RENAME_PRESET = Menu.FIRST + 2;
     private static final int UPDATE_PRESET = Menu.FIRST + 3;
     private static final int DEL_PRESET = Menu.FIRST + 4;
     private static final int RENAME_PROFILE = Menu.FIRST + 5;
     private static final int DEL_PROFILE = Menu.FIRST + 6;
 
     @Override
    public void onCreate(Bundle savedInstanceState)
     {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mDice = new DiceCounter[8];
         mDice[0] = new DiceCounter(this,   2, R.drawable.d2);
         mDice[1] = new DiceCounter(this,   4, R.drawable.d4);
         mDice[2] = new DiceCounter(this,   6, R.drawable.d6);
         mDice[3] = new DiceCounter(this,   8, R.drawable.d8);
         mDice[4] = new DiceCounter(this,  10, R.drawable.d10);
         mDice[5] = new DiceCounter(this,  12, R.drawable.d12);
         mDice[6] = new DiceCounter(this,  20, R.drawable.d20);
         mDice[7] = new DiceCounter(this, 100, R.drawable.d100);
         mMul = new ButtonCounter(this, "mul", R.drawable.mul);
         mMod = new ButtonCounter(this, "mod", R.drawable.mod);
         mDx = new DxCounter(this);
 
         TableLayout t = (TableLayout)findViewById(R.id.counters);
         for(DiceCounter c: mDice)
             t.addView(c);
         t.addView(mMod);
         t.addView(mMul);
         t.addView(mDx);
 
         setConfig(new ThirdConfig());
 
         Button reset = (Button)findViewById(R.id.reset);
         reset.setOnClickListener(new Button.OnClickListener()
         {
             public void onClick(View v)
             {
                 resetCounters();
             }
         });
 
         Button roll = (Button)findViewById(R.id.roll);
         roll.setOnClickListener(new Button.OnClickListener()
         {
             public void onClick(View v)
             {
                 roll();
             }
         });
 
         mFlip = (ViewFlipper)findViewById(R.id.preset_flipper);
         mFlip.setDisplayedChild(0);
         mFlipPresets = (RadioButton)findViewById(R.id.show_presets);
         mFlipResults = (RadioButton)findViewById(R.id.show_results);
         mFlipPresets.setChecked(true);
         mFlipPresets.setOnClickListener(new Button.OnClickListener()
         {
             public void onClick(View v)
             {
                 mFlip.setDisplayedChild(0);
             }
         });
         mFlipResults.setOnClickListener(new Button.OnClickListener()
         {
             public void onClick(View v)
             {
                 mFlip.setDisplayedChild(1);
             }
         });
 
         mDb = new ThirdDb(this);
 
         mProfileView = (Spinner)findViewById(R.id.profiles);
         mProfileView.setOnItemSelectedListener(
             new AdapterView.OnItemSelectedListener()
         {
             public void onItemSelected(AdapterView parent, View v,
                                        int pos, long id)
             {
                 setProfile(new Integer((int)id));
             }
 
             public void onNothingSelected(AdapterView parent)
             {
             }
         });
 
         mPresetView = (ListView)findViewById(R.id.presets);
         mPresetView.setOnItemClickListener(new ListView.OnItemClickListener()
         {
             public void onItemClick(AdapterView parent, View v,
                                     int pos, long id)
             {
                 setConfig((ThirdConfig)parent.getItemAtPosition(pos));
                 roll();
             }
         });
         registerForContextMenu(mPresetView);
         loadProfiles();
         mLog = (TableLayout)findViewById(R.id.log);
         mResult = (TextView)findViewById(R.id.result);
 
         mResultLog = new Vector<TextView>();
         mResultLog.add((TextView)findViewById(R.id.result_log1));
         mResultLog.add((TextView)findViewById(R.id.result_log2));
         mResultLog.add((TextView)findViewById(R.id.result_log3));
         mResultLog.add((TextView)findViewById(R.id.result_log4));
 
         mRNG = new Random();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         boolean result = super.onCreateOptionsMenu(menu);
         menu.add(Menu.NONE, ADD_PRESET, Menu.NONE, R.string.add_preset);
         menu.add(Menu.NONE, ADD_PROFILE, Menu.NONE, R.string.add_profile);
         menu.add(Menu.NONE, RENAME_PROFILE, Menu.NONE, R.string.rename_profile);
         if(mProfileView.getCount() > 1)
         {
             menu.add(Menu.NONE, DEL_PROFILE, Menu.NONE, R.string.del_profile);
         }
         return result;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         Intent i;
         switch(item.getItemId())
         {
             case ADD_PRESET:
                 i = new Intent(this, ThirdNamePreset.class);
                 i.putExtra("config", describeConfig());
                 startActivityForResult(i, ACT_NAME_PRESET);
                 return true;
             case ADD_PROFILE:
                 i = new Intent(this, ThirdNameProfile.class);
                 startActivityForResult(i, ACT_NAME_PROFILE);
                 return true;
             case RENAME_PROFILE:
                 i = new Intent(this, ThirdNameProfile.class);
                 i.putExtra("id", mProfile);
                 i.putExtra("name", getProfileName());
                 startActivityForResult(i, ACT_NAME_PROFILE);
                 return true;
             case DEL_PROFILE:
                 i = new Intent(this, ThirdDelProfile.class);
                 i.putExtra("id", mProfile);
                 i.putExtra("name", getProfileName());
                 startActivityForResult(i, ACT_DEL_PROFILE);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
                                     ContextMenuInfo info)
     {
         super.onCreateContextMenu(menu, v, info);
         menu.add(Menu.NONE, RENAME_PRESET, Menu.NONE, R.string.rename_preset);
         menu.add(Menu.NONE, UPDATE_PRESET, Menu.NONE, R.string.update_preset);
         menu.add(Menu.NONE, DEL_PRESET, Menu.NONE, R.string.del_preset);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
         AdapterContextMenuInfo info;
         info = (AdapterContextMenuInfo)item.getMenuInfo();
         ThirdConfig conf = mPresets.getItem(info.position);
         switch(item.getItemId())
         {
             case RENAME_PRESET:
                 Intent i = new Intent(this, ThirdNamePreset.class);
                 i.putExtra("id", conf.getId());
                 i.putExtra("name", conf.getName());
                 i.putExtra("config", conf.describe());
                 startActivityForResult(i, ACT_NAME_PRESET);
                 return true;
             case UPDATE_PRESET:
                 mDb.updatePreset(conf.getId(), mConfig);
                 loadPresets();
                 return true;
             case DEL_PRESET:
                 mDb.deletePreset(conf.getId());
                 loadPresets();
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 
     @Override
     protected void onActivityResult(int reqCode, int resCode, Intent intent)
     {
         super.onActivityResult(reqCode, resCode, intent);
         if(resCode == RESULT_CANCELED)
             return;
 
         switch(reqCode)
         {
             case ACT_NAME_PRESET:
                 {
                     String name = intent.getStringExtra("name");
                     Integer id = intent.getIntExtra("id", 0);
                     if(id != 0)
                     {
                         mDb.renamePreset(id, name);
                     }
                     else
                     {
                         mConfig.setName(name);
                         mDb.addPreset(mProfile, mConfig);
                     }
                     loadPresets();
                 }
                 break;
             case ACT_NAME_PROFILE:
                 {
                     String name = intent.getStringExtra("name");
                     Integer id = intent.getIntExtra("id", 0);
                     if(id != 0)
                     {
                         mDb.renameProfile(id, name);
                     }
                     else
                     {
                         mDb.addProfile(name);
                     }
                     loadProfiles();
                 }
                 break;
             case ACT_DEL_PROFILE:
                 {
                     Integer id = intent.getIntExtra("id", 0);
                     if(id != 0)
                     {
                         mDb.deleteProfile(id);
                         if(mProfile == id)
                             unsetProfile();
                         loadProfiles();
                     }
                 }
                 break;
 
         }
     }
 
     private void loadProfiles()
     {
         mProfileCursor = mDb.getAllProfiles();
         startManagingCursor(mProfileCursor);
         String[] cols = new String[] {"name"};
         int[] views = new int[] {android.R.id.text1};
         mProfiles = new SimpleCursorAdapter(this,
             android.R.layout.simple_spinner_item,
             mProfileCursor, cols, views);
         mProfiles.setDropDownViewResource(
             android.R.layout.simple_spinner_dropdown_item);
 
         mProfileView.setAdapter(mProfiles);
         if(mProfile == null)
         {
             int index = mProfileCursor.getColumnIndex("_id");
             mProfileCursor.moveToFirst();
             setProfile(new Integer(mProfileCursor.getInt(index)));
         }
     }
 
     private void loadPresets()
     {
         mPresetCursor = mDb.getPresets(mProfile);
         startManagingCursor(mPresetCursor);
         mPresetCursor.moveToFirst();
         mPresets = new ArrayAdapter(this, R.layout.preset);
         while(!mPresetCursor.isAfterLast())
         {
             mPresets.add(new ThirdConfig(mPresetCursor));
             mPresetCursor.moveToNext();
         }
         mPresetView.setAdapter(mPresets);
     }
 
     private void setProfile(Integer profile)
     {
         mProfile = profile;
         loadPresets();
     }
 
     private void unsetProfile()
     {
         mProfile = null;
     }
 
     private void updateConfig()
     {
         if(mConfigImmutable)
             return;
 
         for(DiceCounter c: mDice)
             mConfig.setDie(c.getSides(), c.getValue());
         mConfig.setDx(mDx.getValue());
         mConfig.setDxSides(mDx.getSides());
         mConfig.setMultiplier(mMul.getValue());
         mConfig.setModifier(mMod.getValue());
     }
 
     private void setConfig(ThirdConfig conf)
     {
         mConfig = conf;
         updateFromConfig();
     }
 
     private void updateCounters()
     {
         for(DiceCounter c: mDice)
             c.setValue(mConfig.getDie(c.getSides()));
         mDx.setValue(mConfig.getDx());
         mDx.setSides(mConfig.getDxSides());
         mMul.setValue(mConfig.getMultiplier());
         mMod.setValue(mConfig.getModifier());
     }
 
     private void updateDescription()
     {
         TextView label = (TextView)findViewById(R.id.config);
         label.setText(describeConfig());
 
         TextView range = (TextView)findViewById(R.id.range);
         range.setText(mConfig.describeRange());
 
         ProgressBar bar = (ProgressBar)findViewById(R.id.result_bar);
         bar.setMax(mConfig.getRange());
         bar.setProgress(0);
     }
 
     private void updateFromConfig()
     {
         mConfigImmutable = true;
         updateCounters();
         updateDescription();
         mConfigImmutable = false;
     }
 
     private String describeConfig()
     {
         return mConfig.describe();
     }
 
     private String getProfileName()
     {
         int pos = mProfileView.getSelectedItemPosition();
         int index = mProfileCursor.getColumnIndex("name");
         return mProfileCursor.getString(index);
     }
 
     private void clearLog()
     {
         mLog.removeAllViews();
     }
 
     private void addLog(String label, String outcome)
     {
         TableRow row = new TableRow(this);
         mLog.addView(row);
 
         TextView tv1 = new TextView(this);
         tv1.setText(String.valueOf(mLog.getChildCount()));
         tv1.setGravity(Gravity.CENTER_HORIZONTAL);
         row.addView(tv1);
 
         TextView tv2 = new TextView(this);
         tv2.setText(label);
         row.addView(tv2);
 
         TextView tv3 = new TextView(this);
         tv3.setText(outcome);
         tv3.setGravity(Gravity.RIGHT);
         row.addView(tv3);
     }
 
     private Integer rollDie(Integer sides)
     {
         Integer result = mRNG.nextInt(Math.abs(sides)) + 1;
         if(sides < 0)
             result = -result;
         return result;
     }
 
     private void roll()
     {
         Integer result = new Integer(0);
         Integer outcome;
 
         clearLog();
 
         Vector<Integer> v = mConfig.getDice();
         for(Integer sides: v)
         {
             outcome = rollDie(sides);
             String label = String.format("d%d", Math.abs(sides));
             addLog(label, outcome.toString());
             result += outcome;
         }
 
         Integer mul = mConfig.getMultiplier();
         if(mul != 1)
         {
             addLog("*", mul.toString());
             result *= mul;
         }
 
         Integer mod = mConfig.getModifier();
         if(mod != 0)
         {
             String sign = (mod < 0) ? "-" : "+";
             addLog(sign, String.valueOf(Math.abs(mod)));
             result += mod;
         }
 
         ProgressBar bar = (ProgressBar)findViewById(R.id.result_bar);
         bar.setProgress(result - mConfig.getMin());
 
         shiftResults();
         mResult.setText(result.toString());
     }
 
     private void shiftResults()
     {
         String value = mResult.getText().toString();
         String next;
         for(TextView v: mResultLog)
         {
             if(value == "")
                 break;
 
             next = v.getText().toString();
             v.setText(value);
             value = next;
         }
     }
 
     private void resetCounters()
     {
         mConfig.reset();
         updateFromConfig();
     }
 
     abstract private class Counter extends TableRow
     {
         String mName;
         EditText mCounter;
 
         public Counter(Context ctx, String name)
         {
             super(ctx);
             mName = name;
         }
 
         protected void initCounter()
         {
             mCounter.addTextChangedListener(new TextWatcher()
             {
                 public void afterTextChanged(Editable s)
                 {
                     updateConfig();
                     updateDescription();
                 }
                 public void beforeTextChanged(CharSequence s, int start,
                                               int count, int after)
                 {
                 }
                 public void onTextChanged(CharSequence s, int start,
                                           int before, int count)
                 {
                 }
             });
         }
 
         public Integer getValue()
         {
             try
             {
                 return new Integer(mCounter.getText().toString());
             }
             catch(NumberFormatException e)
             {
                 return new Integer(0);
             }
         }
 
         public Integer setValue(Integer value)
         {
             mCounter.setText(value.toString());
             return getValue();
         }
 
         public void resetValue()
         {
             setValue(0);
         }
 
         public Integer modValue(Integer mod)
         {
             Integer value = getValue();
             return setValue(value + mod);
         }
     }
 
     private class ButtonCounter extends Counter
     {
         ImageButton mButton;
 
         public ButtonCounter(Context ctx, String name, int image)
         {
             super(ctx, name);
 
             LayoutInflater li;
             li = (LayoutInflater)ctx.getSystemService(
                 ctx.LAYOUT_INFLATER_SERVICE);
             li.inflate(R.layout.counter, this);
 
             mButton = (ImageButton)findViewById(R.id.button);
             mButton.setImageResource(image);
             mButton.setOnClickListener(new ImageButton.OnClickListener()
             {
                 public void onClick(View v)
                 {
                     modValue(1);
                 }
             });
             mCounter = (EditText)findViewById(R.id.counter);
             initCounter();
         }
     }
 
     private class DiceCounter extends ButtonCounter
     {
         int mSides;
 
         public DiceCounter(Context ctx, int sides, int image)
         {
             super(ctx, String.format("d%d", sides), image);
             mSides = sides;
         }
 
         public int getSides()
         {
             return mSides;
         }
     }
 
     private class DxCounter extends Counter
     {
         EditText mSides;
 
         public DxCounter(Context ctx)
         {
             super(ctx, "dx");
 
             LayoutInflater li;
             li = (LayoutInflater)ctx.getSystemService(
                 ctx.LAYOUT_INFLATER_SERVICE);
             li.inflate(R.layout.dx, this);
 
             mSides = (EditText)findViewById(R.id.dx_sides);
             mSides.addTextChangedListener(new TextWatcher()
             {
                 public void afterTextChanged(Editable s)
                 {
                     updateConfig();
                     updateDescription();
                 }
                 public void beforeTextChanged(CharSequence s, int start,
                                               int count, int after)
                 {
                 }
                 public void onTextChanged(CharSequence s, int start,
                                           int before, int count)
                 {
                 }
             });
 
             mCounter = (EditText)findViewById(R.id.dx);
             initCounter();
         }
 
         public Integer getSides()
         {
             try
             {
                 return new Integer(mSides.getText().toString());
             }
             catch(NumberFormatException e)
             {
                 return new Integer(0);
             }
         }
 
         public void setSides(Integer sides)
         {
             mSides.setText(sides.toString());
         }
     }
 }
