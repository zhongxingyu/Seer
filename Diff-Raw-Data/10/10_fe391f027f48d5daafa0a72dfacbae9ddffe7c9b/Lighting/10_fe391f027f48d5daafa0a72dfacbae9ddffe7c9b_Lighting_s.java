 package edu.hawaii.systemh.android.lighting;
 
 import java.util.Locale;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.ToggleButton;
 import edu.hawaii.systemh.android.R;
 import edu.hawaii.systemh.android.menu.Menu;
 import edu.hawaii.systemh.android.systemdata.SystemData;
 import android.os.Handler;
 
 /**
  * The activity that starts the lighting page.
  * 
  * @author Group H
  * 
  */
 public class Lighting extends Activity implements ColorPickerDialog.OnColorChangedListener {
 
   // log for debugging
   private static final String TAG = Lighting.class.getSimpleName();
 
   private static final String LIVING = "Living Room";
   private static final String DINING = "Dining Room";
   private static final String KITCHEN = "Kitchen";
   private static final String BATHROOM = "Bathroom";
 
   private static final String LIVING_URI = "LIVING";
   private static final String DINING_URI = "DINING";
   private static final String KITCHEN_URI = "KITCHEN";
   private static final String BATHROOM_URI = "BATHROOM";
 
   // identifiers for another thread to communicate with it.
   private static final int ENABLED_IDENTIFIER = 1;
   private static final int COLOR_IDENTIFIER = 2;
   private static final int BRIGHTNESS_IDENTIFIER = 3;
 
   private boolean running = false;
 
   private Spinner spinner;
   private View color;
   private SeekBar brightness;
   private ToggleButton lightSwitch;
 
   private SystemData bathroom;
   private SystemData kitchen;
   private SystemData livingRoom;
   private SystemData diningRoom;
 
   // handler to handle messages from another running thread
   private Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {
 
       switch (msg.what) {
       case ENABLED_IDENTIFIER:
         lightSwitch.setChecked((Boolean) msg.obj);
         break;
 
       case COLOR_IDENTIFIER:
 
         // need this if statement before the backend XML for external devices is fixed.
         if (!"true".equalsIgnoreCase(String.valueOf(msg.obj))
             && !"false".equalsIgnoreCase(String.valueOf(msg.obj))) {
           color.setBackgroundColor(Color.parseColor(String.valueOf(msg.obj)));
         }
         break;
 
       case BRIGHTNESS_IDENTIFIER:
         brightness.setProgress((Integer) msg.obj);
         break;
 
       default:
         // do nothing
       }
     }
   };
 
   /**
    * Called when the activity is first created.
    * 
    * @param savedInstanceState - A mapping from String values to various Parcelable types.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // requesting to turn the title OFF
     requestWindowFeature(Window.FEATURE_NO_TITLE);
 
     // making it full screen
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
     setContentView(R.layout.lighting);
 
     lightSwitch = (ToggleButton) this.findViewById(R.id.ToggleButton);
 
     lightSwitch.setOnClickListener(new View.OnClickListener() {
       public void onClick(View v) {
         sendEnabledCommand();
       }
     });
 
     color = (View) findViewById(R.id.color);
 
     spinner = (Spinner) findViewById(R.id.spinner);
     ArrayAdapter<CharSequence> adapter =
         ArrayAdapter.createFromResource(this, R.array.rooms, R.layout.spinner);
     adapter.setDropDownViewResource(R.layout.spinner_dropdown);
     spinner.setAdapter(adapter);
     spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
 
     brightness = (SeekBar) this.findViewById(R.id.brightnessSeekbar);
 
     brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
       @Override
       public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         // do nothing
       }
 
       @Override
       public void onStartTrackingTouch(SeekBar seekBar) {
         // do nothing
       }
 
       @Override
       public void onStopTrackingTouch(SeekBar seekBar) {
         sendBrightnessCommand();
       }
     });
 
     livingRoom = new SystemData("lighting-livingroom");
     diningRoom = new SystemData("lighting-diningroom");
     bathroom = new SystemData("lighting-bathroom");
     kitchen = new SystemData("lighting-kitchen");
 
     // set the page components to match the living room state since it's the default choice
     lightSwitch.setChecked(livingRoom.getEnabled());
     brightness.setProgress(livingRoom.getLevel());
     Log.d(TAG, "color is" + livingRoom.getColor());
     color.setBackgroundColor(Color.parseColor(livingRoom.getColor()));
 
     Runnable runnable = new SystemDataRunnable();
     running = true;
     Thread dataGatherThread = new Thread(runnable);
     dataGatherThread.start();
   }
 
   /**
    * Called when the brightness was changed and sends a do command to the backend.
    */
   public void sendBrightnessCommand() {
     if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(LIVING)) {
       livingRoom.setLightingLevel(brightness.getProgress(), LIVING_URI);
 
       // test why color isn't working
       livingRoom.setLightingColor("#FFFFFF", LIVING_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(DINING)) {
       diningRoom.setLightingLevel(brightness.getProgress(), DINING_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase("Kitchen")) {
       kitchen.setLightingLevel(brightness.getProgress(), KITCHEN_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(BATHROOM)) {
       bathroom.setLightingLevel(brightness.getProgress(), BATHROOM_URI);
     }
   }
 
   /**
    * Called when the toggle button is clicked and sends a do command to the backend.
    */
   public void sendEnabledCommand() {
 
     if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(LIVING)) {
       livingRoom.setLightingEnabled(lightSwitch.isChecked(), LIVING_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(DINING)) {
       diningRoom.setLightingEnabled(lightSwitch.isChecked(), DINING_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(KITCHEN)) {
       kitchen.setLightingEnabled(lightSwitch.isChecked(), KITCHEN_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(BATHROOM)) {
       bathroom.setLightingEnabled(lightSwitch.isChecked(), BATHROOM_URI);
     }
   }
 
   /**
    * Called when the the color is changed and sends a do command to the backend.
    * 
    * @param colorCode The color code.
    */
   public void sendColorCommand(String colorCode) {
 
     Log.d(TAG, "new color is " + colorCode);
 
     if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(LIVING)) {
       livingRoom.setLightingColor(colorCode, LIVING_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(DINING)) {
       diningRoom.setLightingColor(colorCode, DINING_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(KITCHEN)) {
       kitchen.setLightingColor(colorCode, KITCHEN_URI);
     }
     else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(BATHROOM)) {
       bathroom.setLightingColor(colorCode, BATHROOM_URI);
     }
   }
 
   /**
    * Take the user to the menu.
    * 
    * @param view The view.
    */
   public void showMenu(View view) {
     running = false;
     Intent intent = new Intent(Intent.ACTION_VIEW);
     intent.setClassName(this, Menu.class.getName());
     startActivity(intent);
   }
 
   /**
    * Called when the user clicks 'Change color'. The program will show color picker dialog.
    * 
    * @param view The view.
    */
   public void changeColor(View view) {
     ColorPickerDialog colorPicker = new ColorPickerDialog(this, this, "Key", 0, 0);
     colorPicker.show();
   }
 
   /**
    * Destroys this activity when onStop is called.
    */
   @Override
   protected void onStop() {    
     finish();
     super.onDestroy();
   }
   
   
   @Override
   protected void onPause() {
     running = false;
   }
 
   /**
    * Called after the user clicks 'confirm'.
    * 
    * @param key The key.
    * @param color The color.
    */
   @Override
   public void colorChanged(String key, int color) {
     this.color.setBackgroundColor(color);
 
     // calculate for the HEX String representation of color.
     int red = Color.red(color);
     int green = Color.green(color);
     int blue = Color.blue(color);
     String newColor =
         Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
     sendColorCommand(newColor.toUpperCase(Locale.US));
   }
 
   /**
    * The listener for the room choices.
    * 
    * @author Group H
    * 
    */
   public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 
       // update the components according to the selected room.
       if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(LIVING)) {
         livingRoom = new SystemData("lighting-livingroom");
         updateViews(livingRoom);
       }
       else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(DINING)) {
         diningRoom = new SystemData("lighting-diningroom");
         updateViews(diningRoom);
       }
       else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(KITCHEN)) {
         kitchen = new SystemData("lighting-kitchen");
         updateViews(kitchen);
       }
       else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(BATHROOM)) {
         bathroom = new SystemData("lighting-bathroom");
         updateViews(bathroom);
       }
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> parent) {
       // Do nothing.
     }
   }
 
   /**
    * Updates the lighting page according to the drop down choice.
    * 
    * @param selectedRoom The current room.
    */
   public void updateViews(SystemData selectedRoom) {
     lightSwitch.setChecked(selectedRoom.getEnabled());
     brightness.setProgress(selectedRoom.getLevel());
 
     // need this if statement until the backend XML bug is fixed
     if ("true".equalsIgnoreCase(selectedRoom.getColor())
         || "false".equalsIgnoreCase(selectedRoom.getColor())) {
       color.setBackgroundColor(Color.WHITE);
     }
     else {
       color.setBackgroundColor(Color.parseColor(selectedRoom.getColor()));
     }
   }
 
   /**
    * A runnable that gathers System data and updates the views on the current page.
    * 
    * @author Group H
    * 
    */
   public class SystemDataRunnable implements Runnable {
 
     private Message msg;
 
     @Override
     public void run() {
 
       while (running) {
         
         // update the components according to the selected room.
         if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(LIVING)) {
           livingRoom = new SystemData("lighting-livingroom");
           updateViews(livingRoom);
         }
         else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(DINING)) {
           diningRoom = new SystemData("lighting-diningroom");
           updateViews(diningRoom);
         }
         else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(KITCHEN)) {
           kitchen = new SystemData("lighting-kitchen");
           updateViews(kitchen);
         }
         else if (String.valueOf(spinner.getSelectedItem()).equalsIgnoreCase(BATHROOM)) {
           bathroom = new SystemData("lighting-bathroom");
           updateViews(bathroom);
         }
       }
     }
 
     /**
      * Creates messages and send them to the activity handler.
      * 
      * @param selectedRoom The current room.
      */
     public void updateViews(SystemData selectedRoom) {
       // send message for enabled
       msg = new Message();
       msg.what = ENABLED_IDENTIFIER;
       msg.obj = selectedRoom.getEnabled();
       handler.sendMessage(msg);
 
       // send message for color
       msg = new Message();
       msg.what = COLOR_IDENTIFIER;
       msg.obj = selectedRoom.getColor();
       handler.sendMessage(msg);
 
       // send message for brightness
       msg = new Message();
       msg.what = BRIGHTNESS_IDENTIFIER;
       msg.obj = selectedRoom.getLevel();
       handler.sendMessage(msg);
 
     }
   }
 }
