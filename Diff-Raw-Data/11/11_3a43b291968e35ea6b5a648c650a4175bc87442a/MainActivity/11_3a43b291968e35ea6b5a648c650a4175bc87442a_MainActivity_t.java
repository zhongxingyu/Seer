 package net.reikooters.Jotter;
 
import android.animation.LayoutTransition;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.InputType;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.*;
 import android.content.Intent;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 
 public class MainActivity extends Activity
 {
     public final static String EXTRA_MESSAGE = "net.reikooters.Jotter.MESSAGE";
     public final static String LOG_TAG = "Jotter";
     ViewAnimator viewAnimator;
     ListView categoryListView;
     ListView noteListView;
     ArrayList<String> categoryList = new ArrayList<String>();
     StableArrayAdapter categoryAdapter;
     int menuLevel = 0;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         viewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);
 
         final Animation inAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
         final Animation outAnim = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
 
         viewAnimator.setInAnimation(inAnim);
         viewAnimator.setOutAnimation(outAnim);
 
         categoryListView = (ListView) findViewById(R.id.categoryListView);
         noteListView = (ListView) findViewById(R.id.noteListView);
 
         registerForContextMenu(categoryListView);
 
         /*
         String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                 "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                 "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                 "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                 "Android", "iPhone", "WindowsMobile" };
 
         for (int i = 0; i < values.length; ++i) {
             categoryList.add(values[i]);
         }
         */
 
         loadData();
 
         categoryAdapter = new StableArrayAdapter(this,
                 android.R.layout.simple_list_item_1, categoryList);
         categoryListView.setAdapter(categoryAdapter);
         //noteListView.setAdapter(adapter);
 
         categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
         {
             @Override
             public void onItemClick(AdapterView<?> parent, final View view,
                                     final int position, long id) {
                 //final long item = parent.getItemIdAtPosition(position);
                 /*
                 view.animate().setDuration(1000).alpha(0)
                         .withEndAction(new Runnable()
                         {
                             @Override
                             public void run()
                             {
                                 categoryList.remove(position);
                                 categoryAdapter.notifyDataSetChanged();
                                 view.setAlpha(1);
                             }
                         }
                 );
                 */
                 openCategory(position);
             }
 
         });

        LayoutTransition l = new LayoutTransition();
        l.enableTransitionType(LayoutTransition.APPEARING);
        l.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        l.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        l.enableTransitionType(LayoutTransition.CHANGING);
        l.enableTransitionType(LayoutTransition.DISAPPEARING);
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.container);
        viewGroup.setLayoutTransition(l);
     }
 
     private class StableArrayAdapter extends ArrayAdapter<String>
     {
         HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
 
         public StableArrayAdapter(Context context, int textViewResourceId,
                                   List<String> objects) {
             super(context, textViewResourceId, objects);
             for (int i = 0; i < objects.size(); ++i) {
                 mIdMap.put(objects.get(i), i);
             }
         }
 
         @Override
         public long getItemId(int position) {
             String item = getItem(position);
             return mIdMap.get(item);
         }
 
         @Override
         public boolean hasStableIds() {
             return true;
         }
 
         public void addItem(String text) {
             mIdMap.put(text, mIdMap.size());
         }
     }
 
     /** Called when the user clicks the Send button */
     public void sendMessage(View view)
     {
         /*
         // Do something in response to button
         Intent intent = new Intent(this, DisplayMessageActivity.class);
         EditText editText = (EditText) findViewById(R.id.edit_message);
         String message = editText.getText().toString();
         intent.putExtra(EXTRA_MESSAGE, message);
         startActivity(intent);
         */
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu items for use in the action bar
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_activity_actions, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
         switch (item.getItemId()) {
             //case R.id.action_search:
             //    openSearch();
             //    return true;
             case R.id.action_new_category:
                 addItem();
                 return true;
             case R.id.action_settings:
                 //openSettings();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onBackPressed()
     {
         if (menuLevel > 0)
         {
             viewAnimator.showPrevious();
             --menuLevel;
 
             if (menuLevel == 0)
                 setTitle(R.string.app_name);
         }
         else
         {
             // Quit program
             super.onBackPressed();
         }
         /*
         Intent startMain = new Intent(Intent.ACTION_MAIN);
         startMain.addCategory(Intent.CATEGORY_HOME);
         startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(startMain);
         */
     }
 
     void addItem()
     {
         //DialogFragment newFragment = new SearchDialogFragment();
         //newFragment.show(null, "search");
 
         // Use the Builder class for convenient dialog construction
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         //builder.setMessage(R.string.dialog_search_message);
         builder.setTitle(R.string.create_new);
         //builder.setMessage(" ");
 
         final EditText searchText = new EditText(this);
         searchText.setEllipsize(TextUtils.TruncateAt.END);
         searchText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
         builder.setView(searchText);
 
         // Add the buttons
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int id)
             {
                 // User clicked OK button
                 addCategory(searchText.getText().toString());
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int id)
             {
                 // User cancelled the dialog
             }
         });
 
         // Create the AlertDialog
         AlertDialog dialog = builder.create();
 
         dialog.show();
 
         searchText.requestFocus();
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.showSoftInput(searchText, InputMethodManager.SHOW_FORCED);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu)
     {
         MenuItem searchViewMenuItem = menu.findItem(R.id.action_search);
         SearchView mSearchView = (SearchView) searchViewMenuItem.getActionView();
         int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
         ImageView v = (ImageView) mSearchView.findViewById(searchImgId);
         v.setImageResource(R.drawable.search_force);
         //mSearchView.setOnQueryTextListener(this);
         return super.onPrepareOptionsMenu(menu);
     }
 
     public void addCategory(String text)
     {
         categoryList.add(text);
         saveData();
         categoryAdapter.addItem(text);
         categoryAdapter.notifyDataSetChanged();
     }
 
     public void delCategory(int index)
     {
         categoryList.remove(index);
         saveData();
         categoryAdapter.notifyDataSetChanged();
     }
 
     /* Checks if external storage is available for read and write */
     public boolean isExternalStorageWritable() {
         String state = Environment.getExternalStorageState();
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             return true;
         }
         return false;
     }
 
     /* Checks if external storage is available to at least read */
     public boolean isExternalStorageReadable() {
         String state = Environment.getExternalStorageState();
         if (Environment.MEDIA_MOUNTED.equals(state) ||
                 Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
             return true;
         }
         return false;
     }
 
     public boolean saveData() {
         if (isExternalStorageReadable())
         {
             /*
             // Get the directory for the user's public pictures directory.
             File dir = new File(Environment.getExternalStorageDirectory() + "/Jotter/");
             if (!dir.mkdirs()) {
                 Log.e(LOG_TAG, "Directory not created");
             }
             File file = new File(dir, "jotter_data.txt");
             if (file.exists ()) file.delete ();
             try {
                 FileOutputStream out = new FileOutputStream(file);
                 out.write();
                 out.flush();
                 out.close();
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
             */
 
             File dir = new File(Environment.getExternalStorageDirectory() + "/Jotter/");
             if (!dir.mkdirs())
             {
                 Log.e(LOG_TAG, "Directory not created");
             }
 
             String ser = SerializeObject.objectToString(categoryList);
             if (ser != null && !ser.equalsIgnoreCase(""))
                 SerializeObject.WriteSettings(this, ser, "jotter_categories.dat");
             else
                 SerializeObject.WriteSettings(this, "", "jotter_categories.dat");
 
             return true;
         }
 
         return false;
     }
 
     public boolean loadData()
     {
         String ser = SerializeObject.ReadSettings(this, "jotter_categories.dat");
         if (ser != null && !ser.equalsIgnoreCase(""))
         {
             Object obj = SerializeObject.stringToObject(ser);
             // Then cast it to your object and
             if (obj instanceof ArrayList) {
                 // Do something
                 categoryList = (ArrayList<String>)obj;
             }
             else
                 return false;
         }
 
         return true;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
                                     ContextMenu.ContextMenuInfo menuInfo)
     {
         if (v.getId() == R.id.categoryListView)
         {
             AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
             menu.setHeaderTitle(categoryList.get(info.position));
             String[] menuItems = getResources().getStringArray(R.array.contextMenuArray);
 
             for (int i = 0; i<menuItems.length; i++)
                 menu.add(Menu.NONE, i, i, menuItems[i]);
         }
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
         final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         int menuItemIndex = item.getItemId();
         //String[] menuItems = getResources().getStringArray(R.array.contextMenuArray);
         //String menuItemName = menuItems[menuItemIndex];
         //String listItemName = categoryList.get(info.position);
 
         switch (menuItemIndex)
         {
             // Open
             case 0:
                 openCategory(info.position);
                 break;
 
             // Delete
             case 1:
                 /*
                 categoryListView.animate().setDuration(1000).alpha(0)
                         .withEndAction(new Runnable()
                         {
                             @Override
                             public void run()
                             {
                                 categoryList.remove(info.position);
                                 categoryAdapter.notifyDataSetChanged();
                                 categoryListView.setAlpha(1);
                             }
                         }
                         );
                         */
                 delCategory(info.position);
                 break;
         }
 
         //TextView text = (TextView)findViewById(R.id.footer);
         //text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));
         return true;
     }
 
     void openCategory(int index)
     {
         viewAnimator.showNext();
         setTitle(categoryList.get(index));
         ++menuLevel;
     }
 }
