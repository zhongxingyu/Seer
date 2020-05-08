 package com.jonas.colorhex;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.ActionMode;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.jonas.colorhex.color.ColorPicker;
 import com.jonas.colorhex.color.OpacityBar;
 import com.jonas.colorhex.color.SVBar;
 import com.jonas.colorhex.stickylistview.StickyListHeadersAdapter;
 import com.jonas.colorhex.stickylistview.StickyListHeadersListView;
 
 import java.util.List;
 
 public class ActivityMain extends Activity implements FragmentManager.OnBackStackChangedListener {
 
 
     private Handler mHandler = new Handler();
     private static DatabaseHandler db;
 
     private boolean mShowingBack = false;
 
     private static SharedPreferences sp;
 
     private GestureDetector gestureDetector;
     View.OnTouchListener gestureListener;
     private static final int SWIPE_MIN_DISTANCE = 120;
     private static final int SWIPE_MAX_OFF_PATH = 250;
     private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 
     public static Resources res;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         sp = PreferenceManager.getDefaultSharedPreferences(this);
         db = new DatabaseHandler(this);
         res = getResources();
 
         if (savedInstanceState == null)
             getFragmentManager().beginTransaction().add(R.id.container, new CardFrontFragment()).commit();
         else
             mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
         getFragmentManager().addOnBackStackChangedListener(this);
 
         gestureDetector = new GestureDetector(this, new MyGestureDetector());
         gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 return gestureDetector.onTouchEvent(event);
             }
         };
         findViewById(R.id.container).setOnTouchListener(gestureListener);
         findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
             }
         });
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.action_flip) {
             flipCard();
         } else if (item.getItemId() == R.id.action_license) {
             DialogFragment newFragment = new LicenseDialogFragment();
             newFragment.show(getFragmentManager(), "license");
         } else if (item.getItemId() == R.id.action_settings) {
             startActivity(new Intent(this, SettingsActivity.class));
         } else if (item.getItemId() == R.id.action_favorite) {
             getFragmentManager().beginTransaction().replace(R.id.container, new FavoriteFragment()).addToBackStack(null).commit();
         }
         return super.onOptionsItemSelected(item);
 
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (mShowingBack)
             menu.findItem(R.id.action_flip).setIcon(R.drawable.ic_action_flip_front);
         else
             menu.findItem(R.id.action_flip).setIcon(R.drawable.ic_action_flip_back);
         if (findViewById(R.id.lvSticky) != null) {
             menu.findItem(R.id.action_flip).setVisible(false);
             menu.findItem(R.id.action_favorite).setVisible(false);
         } else {
             menu.findItem(R.id.action_flip).setVisible(true);
             menu.findItem(R.id.action_favorite).setVisible(true);
         }
         return super.onPrepareOptionsMenu(menu);
     }
 
     private void flipCard() {
         if (mShowingBack) {
             getFragmentManager().popBackStack();
             InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             im.hideSoftInputFromWindow(findViewById(R.id.container).getApplicationWindowToken(), 0);
             return;
         }
         mShowingBack = true;
         getFragmentManager()
                 .beginTransaction().setCustomAnimations(
                 R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                 R.animator.card_flip_left_in, R.animator.card_flip_left_out).replace(R.id.container, new CardBackFragment())
                 .addToBackStack(null).commit();
 
         mHandler.post(new Runnable() {
             @Override
             public void run() {
                 invalidateOptionsMenu();
             }
         });
     }
 
     @Override
     public void onBackStackChanged() {
         mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
         invalidateOptionsMenu();
     }
 
     public static class FavoriteFragment extends Fragment implements AdapterView.OnItemClickListener {
 
         ActionMode.Callback mCallback;
         ActionMode mMode;
         private StickyListHeadersListView mStickyListView;
         private int mSelected = 0;
         View customActionBarView;
 
         public FavoriteFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             ViewGroup rootView = (ViewGroup) inflater
                     .inflate(R.layout.fragment_favorite, container, false);
 
             inflater = (LayoutInflater) getActivity().getActionBar().getThemedContext()
                     .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
             customActionBarView = inflater.inflate(R.layout.ab_contextual_view, null);
             customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                     new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             // "Done"
                             editModeSelected(false);
                         }
                     });
 
             mCallback = new ActionMode.Callback() {
 
 
                 @Override
                 public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                     mode.setTitle(mSelected + res.getString(R.string.contextual_menu));
                     return true;
                 }
 
                 @Override
                 public void onDestroyActionMode(ActionMode mode) {
                     mMode = null;
                     mSelected = 0;
                 }
 
                 @Override
                 public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                     getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
                     return true;
                 }
 
                 @Override
                 public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch(item.getItemId()){
                         case R.id.con_action_delete:
                             Toast.makeText(getActivity(), "WIP", Toast.LENGTH_SHORT).show();
                             mode.finish();
                             break;
                         case R.id.con_action_share:
                             Toast.makeText(getActivity(), "WIP", Toast.LENGTH_SHORT).show();
                             break;
                         case R.id.con_action_select_all:
                             Toast.makeText(getActivity(), "WIP", Toast.LENGTH_SHORT).show();
                             break;
                     }
                     return false;
                 }
             };
 
             mStickyListView = (StickyListHeadersListView) rootView.findViewById(R.id.lvSticky);
             final CustomListViewAdapter adapter = new CustomListViewAdapter(getActivity(), db.getAllFavColors(), db.getAllRecColors(), new String[]{res.getString(R.string.list_header_favorite), res.getString(R.string.list_header_recent)}, db.getAllFavValues(1), db.getAllRecValues(1));
             mStickyListView.setAdapter(adapter);
             mStickyListView.setOnItemClickListener(this);
             /*SwipeDismissListViewTouchListener touchListener =
                     new SwipeDismissListViewTouchListener(
                             mStickyListView,
                             new SwipeDismissListViewTouchListener.OnDismissCallback() {
                                 @Override
                                 public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                     for (int position : reverseSortedPositions) {
                                         adapter.remove(position);
                                     }
                                     adapter.notifyDataSetChanged();
                                 }
                             });
             mStickyListView.setOnTouchListener(touchListener);
             mStickyListView.setOnScrollListener(touchListener.makeScrollListener());*/
             return rootView;
         }
 
 
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             /*if(mMode!=null)
                 return;
             else
                 mMode = getActivity().startActionMode(mCallback);
             mSelected++;
             mMode.setTitle(mSelected + res.getString(R.string.contextual_menu));*/
             editModeSelected(true);
         }
 
         @Override
         public void onPause() {
             super.onPause();
            Toast.makeText(getActivity(), "Paused", Toast.LENGTH_SHORT).show();
             editModeSelected(false);
         }
 
         public void editModeSelected(boolean selected) {
             final ActionBar actionBar = this.getActivity().getActionBar();
             if (selected) {
                 actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                         ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                                 | ActionBar.DISPLAY_SHOW_TITLE);
                 actionBar.setCustomView(customActionBarView);
             } else {
                 actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE,
                         ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
                                 | ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
 
             }
         }
     }
 
     public static class CardFrontFragment extends Fragment implements ColorPicker.OnColorChangedListener, CompoundButton.OnCheckedChangeListener {
 
         private TextView tvOutputHex, tvOutputDec;
         private static ColorPicker colorPicker;
         private SVBar svBar;
         private OpacityBar opacityBar;
         private CompoundButton star;
         private static Context ctx;
         private static ClipboardManager clipboard;
         private static String hexValue;
         private static int opacityValue;
         private boolean alphaShown;
 
         public CardFrontFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             return inflater.inflate(R.layout.fragment_picker, container, false);
         }
 
         @Override
         public void onViewCreated(View view, Bundle savedInstanceState) {
             super.onViewCreated(view, savedInstanceState);
             ctx = getActivity();
             alphaShown = sp.getBoolean("pref_alpha", false);
             clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
             colorPicker = (ColorPicker) view.findViewById(R.id.picker);
             svBar = (SVBar) view.findViewById(R.id.svbar);
             opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
             tvOutputHex = (TextView) view.findViewById(R.id.tvOutputHex);
             tvOutputDec = (TextView) view.findViewById(R.id.tvOutputDec);
             star = (CompoundButton) view.findViewById(R.id.star);
             star.setOnCheckedChangeListener(this);
             colorPicker.addSVBar(svBar);
             if (alphaShown) {
                 opacityBar.setVisibility(View.VISIBLE);
                 colorPicker.addOpacityBar(opacityBar);
             }
             colorPicker.setOnColorChangedListener(this);
             int color = sp.getInt("color", getResources().getColor(R.color.ab_red));
             colorPicker.setColor(color);
             colorPicker.setNewCenterColor(color);
             setColor(color);
         }
 
         @Override
         public void onSaveInstanceState(Bundle savedInstanceState) {
             super.onSaveInstanceState(savedInstanceState);
             sp.edit().putInt("color", colorPicker.getColor()).commit();
         }
 
         private void setColor(int color) {
             colorPicker.setOldCenterColor(color);
             hexValue = String.format("#%06X", (0xFFFFFF & colorPicker.getColor()));
             opacityValue = opacityBar.getOpacity();
             if (alphaShown) {
                 tvOutputDec.setText("A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")");
                 hexValue = "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue;
                 tvOutputHex.setText(hexValue);
             } else {
                 tvOutputHex.setText(hexValue);
                 tvOutputDec.setText("(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")");
             }
 
         }
 
         @Override
         public void onColorChanged(int color) {
             setColor(color);
         }
 
         public static void onCenterPressed() {
             String text = "";
             switch (Integer.parseInt(sp.getString(SettingsActivity.pref_clipboard, "0"))) {
                 case 0:
                     text = hexValue;
                     break;
                 case 1:
                     text = "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue;
                     break;
                 case 2:
                     text = "(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")";
                     break;
                 case 3:
                     text = "A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")";
                     break;
 
             }
             ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
             clipboard.setPrimaryClip(clip);
             float[] mHSV = new float[3];
             Color.colorToHSV(colorPicker.getColor(), mHSV);
             String mString = String.valueOf(mHSV[0]) + String.valueOf(mHSV[1]) + String.valueOf(mHSV[2]);
             Toast.makeText(ctx, res.getString(R.string.clipboard), Toast.LENGTH_SHORT).show();
             db.addRecent(colorPicker.getColor(), mString, hexValue, "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue, "(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")", "A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")");
         }
 
         @Override
         public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
             if (compoundButton.getId() == R.id.star) {
                 if (b) {
                     float[] mHSV = new float[3];
                     Color.colorToHSV(colorPicker.getColor(), mHSV);
                     String mString = String.valueOf(mHSV[0]) + String.valueOf(mHSV[1]) + String.valueOf(mHSV[2]);
                     db.addFavorite(colorPicker.getColor(), mString, hexValue, "A:" + Integer.toHexString(opacityValue).toUpperCase() + " " + hexValue, "(" + Integer.parseInt("" + hexValue.charAt(1) + hexValue.charAt(2), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt("" + hexValue.charAt(5) + hexValue.charAt(6), 16) + ")", "A:" + opacityValue + " (" + Integer.parseInt(String.valueOf(hexValue.charAt(1) + hexValue.charAt(2)), 16) + "," + Integer.parseInt("" + hexValue.charAt(3) + hexValue.charAt(4), 16) + "," + Integer.parseInt(String.valueOf(hexValue.charAt(5) + hexValue.charAt(6)), 16) + ")");
                 }
             }
         }
     }
 
     public static class CardBackFragment extends Fragment implements View.OnClickListener {
 
         private ImageView color1, color2, color3;
         public static EditText etColor1, etColor2, etColor3;
         private Button btClear1, btClear2, btClear3;
 
         public CardBackFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             return inflater.inflate(R.layout.fragment_setter, container, false);
         }
 
         @Override
         public void onViewCreated(View view, Bundle savedInstanceState) {
             super.onViewCreated(view, savedInstanceState);
 
             color1 = (ImageView) view.findViewById(R.id.color1);
             color2 = (ImageView) view.findViewById(R.id.color2);
             color3 = (ImageView) view.findViewById(R.id.color3);
             etColor1 = (EditText) view.findViewById(R.id.etColor1);
             etColor2 = (EditText) view.findViewById(R.id.etColor2);
             etColor3 = (EditText) view.findViewById(R.id.etColor3);
             btClear1 = (Button) view.findViewById(R.id.btClear1);
             btClear2 = (Button) view.findViewById(R.id.btClear2);
             btClear3 = (Button) view.findViewById(R.id.btClear3);
             etColor1.setText(sp.getString("etColor1", "#"));
             etColor2.setText(sp.getString("etColor2", "#"));
             etColor3.setText(sp.getString("etColor3", "#"));
             btClear1.setOnClickListener(this);
             btClear2.setOnClickListener(this);
             btClear3.setOnClickListener(this);
             saveText();
             etColor1.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
                 }
 
                 @Override
                 public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
                 }
 
                 @Override
                 public void afterTextChanged(Editable editable) {
                     saveText();
                 }
             });
             etColor2.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
                 }
 
                 @Override
                 public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
                 }
 
                 @Override
                 public void afterTextChanged(Editable editable) {
                     saveText();
                 }
             });
             etColor3.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
                 }
 
                 @Override
                 public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
                 }
 
                 @Override
                 public void afterTextChanged(Editable editable) {
                     saveText();
                 }
             });
         }
 
         private void saveText() {
             try {
                 String s = etColor1.getText().toString();
                 if (!s.contains("#"))
                     s = "#" + s;
                 int color = Color.parseColor(s);
                 color1.setBackgroundColor(color);
             } catch (Exception e) {
                 color1.setBackgroundColor(Color.WHITE);
             }
             try {
                 String s = etColor2.getText().toString();
                 if (!s.contains("#"))
                     s = "#" + s;
                 int color = Color.parseColor(s);
                 color2.setBackgroundColor(color);
             } catch (Exception e) {
                 color2.setBackgroundColor(Color.WHITE);
             }
             try {
                 String s = etColor3.getText().toString();
                 if (!s.contains("#"))
                     s = "#" + s;
                 int color = Color.parseColor(s);
                 color3.setBackgroundColor(color);
             } catch (Exception e) {
                 color3.setBackgroundColor(Color.WHITE);
             }
             sp.edit().putString("etColor1", etColor1.getText().toString()).commit();
             sp.edit().putString("etColor2", etColor2.getText().toString()).commit();
             sp.edit().putString("etColor3", etColor3.getText().toString()).commit();
         }
 
 
         @Override
         public void onClick(View view) {
             switch (view.getId()) {
                 case R.id.btClear1:
                     etColor1.setText("#");
                     saveText();
                     break;
                 case R.id.btClear2:
                     etColor2.setText("#");
                     saveText();
                     break;
                 case R.id.btClear3:
                     etColor3.setText("#");
                     saveText();
                     break;
             }
         }
     }
 
     public class LicenseDialogFragment extends DialogFragment {
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             LayoutInflater inflater = getActivity().getLayoutInflater();
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             builder.setView(inflater.inflate(R.layout.dialog_license, null))
                     .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             getDialog().dismiss();
                         }
                     });
             return builder.create();
         }
     }
 
     class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             try {
                 if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                     return false;
                 if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && !mShowingBack && findViewById(R.id.lvSticky) == null)
                     flipCard();
                 else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && mShowingBack && findViewById(R.id.lvSticky) == null)
                     flipCard();
             } catch (Exception e) {
 
             }
             return false;
         }
 
         @Override
         public boolean onDown(MotionEvent e) {
             return super.onDown(e);
         }
     }
 
     public static class CustomListViewAdapter extends BaseAdapter implements StickyListHeadersAdapter {
 
         private LayoutInflater inflater;
         private List<String> codeFav, codeRec;
         private List<Integer> colorsRec, colorsFav;
         private String[] headers;
 
         public CustomListViewAdapter(Context context, List<Integer> colorsFav, List<Integer> colorsRec, String[] headers, List<String> codeFav, List<String> codeRec) {
             inflater = LayoutInflater.from(context);
             this.colorsFav = colorsFav;
             this.colorsRec = colorsRec;
             this.headers = headers;
             this.codeFav = codeFav;
             this.codeRec = codeRec;
         }
 
         @Override
         public int getCount() {
             return (colorsFav.size() + colorsRec.size());
         }
 
         @Override
         public Object getItem(int position) {
             if (position > colorsFav.size())
                 return colorsRec.get(position - colorsFav.size());
             else
                 return colorsFav.get(position);
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder;
 
             if (convertView == null) {
                 holder = new ViewHolder();
                 convertView = inflater.inflate(R.layout.list_item, parent, false);
                 holder.text = (TextView) convertView.findViewById(R.id.tvItem);
                 holder.colorView = (ImageView) convertView.findViewById(R.id.item_color);
                 convertView.setTag(holder);
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
             int index;
             if (colorsFav == null)
                 index = 0;
             else index = colorsFav.size();
             if (position >= index) {
                 holder.colorView.setBackgroundColor(colorsRec.get(position - index));
                 holder.text.setText(codeRec.get(position - index));
 
             } else {
                 holder.colorView.setBackgroundColor(colorsFav.get(position));
                 holder.text.setText(codeFav.get(position));
             }
 
             return convertView;
         }
 
         @Override
         public View getHeaderView(int position, View convertView, ViewGroup parent) {
             HeaderViewHolder holder;
             if (convertView == null) {
                 holder = new HeaderViewHolder();
                 convertView = inflater.inflate(R.layout.list_header, parent, false);
                 holder.text1 = (TextView) convertView.findViewById(R.id.header);
                 convertView.setTag(holder);
             } else {
                 holder = (HeaderViewHolder) convertView.getTag();
             }
             if (colorsFav.size() == 0 || position >= colorsFav.size())
                 holder.text1.setText(headers[1]);
             else holder.text1.setText(headers[0]);
             return convertView;
         }
 
         @Override
         public long getHeaderId(int position) {
            if (position > colorsFav.size())
                 return 2L;
             else return 1L;
         }
 
         /*public void remove(int position) {
             int index;
             if (colorsFav == null)
                 index = 0;
             else index = colorsFav.size();
             if (position >= index) {
                 colorsRec.remove(position - index);
                 codeRec.remove(position - index);
             } else {
                 colorsFav.remove(position);
                 codeFav.remove(position);
             }
         }*/
 
         class HeaderViewHolder {
             TextView text1;
         }
 
         class ViewHolder {
             TextView text;
             ImageView colorView;
         }
 
     }
 
 }
