 package moontime.droid;
 
 import java.util.List;
 
 import moontime.MoonEvent;
 import moontime.droid.store.GlobalPreferences;
 import roboguice.activity.RoboListActivity;
 import roboguice.inject.InjectView;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.google.inject.Inject;
 import com.google.inject.internal.Lists;
import com.google.inject.internal.Nullable;
 
 public class ReminderActivity extends RoboListActivity {
 
   @Inject
   protected MoontimeService _moontimeService;
   @Inject
   protected GlobalPreferences _globalPreferences;
  // TODO why inject fails
  @Nullable
   @InjectView(R.id.switchLists)
   protected Button _switchListsButton;
   private MoonEvent _nextMoonEvent;
   private List<Reminder> _reminders = Lists.newArrayList();
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
    setListAdapter(new ArrayAdapter<Reminder>(this, android.R.layout.simple_list_item_multiple_choice, _reminders));
     setContentView(R.layout.reminder_layout);
     getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
     _nextMoonEvent = _moontimeService.getNextMoonEvent();
 
    _switchListsButton = (Button) findViewById(R.id.switchLists);
     updateListAdapter();
     registerForContextMenu(getListView());
   }
 
   private void updateListAdapter() {
     // TODO +/- hours to event ? in title
     setTitle(_nextMoonEvent.getType().getDisplayName() + " - Reminders");
     _switchListsButton.setText("Switch to " + _nextMoonEvent.getType().opposite().getDisplayName());
     _reminders.clear();
     _reminders.addAll(_globalPreferences.getReminders(_nextMoonEvent, true));
     for (int i = 0; i < _reminders.size(); i++) {
       getListView().setItemChecked(i, _reminders.get(i).isChecked());
     }
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.reminders_menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
     switch (item.getItemId()) {
     case R.id.clearChecks:
       for (int i = 0; i < getListAdapter().getCount(); i++) {
         getListView().setItemChecked(i, false);
       }
     default:
       return super.onOptionsItemSelected(item);
     }
   }
 
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
     menu.setHeaderTitle(getListAdapter().getItem(info.position).getText());
     for (ReminderContextMenu reminderMenu : ReminderContextMenu.values()) {
       menu.add(Menu.NONE, reminderMenu.ordinal(), reminderMenu.ordinal(), reminderMenu.getDisplayName());
     }
   }
 
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
     int menuItemIndex = item.getItemId();
     ReminderContextMenu reminderMenu = ReminderContextMenu.values()[menuItemIndex];
     reminderMenu.execute(this, info.position);
     return true;
   }
 
   public void switchLists(View view) {
     saveChecks();
     _nextMoonEvent = new MoonEvent(_nextMoonEvent.getType().opposite(), _nextMoonEvent.getDate());
     updateListAdapter();
   }
 
   public void newReminder(View view) {
     newReminder(view, null, -1);
   }
 
   protected void newReminder(View view, final Reminder existingReminder, final int existingReminderIndex) {
     final Dialog dialog = new Dialog(this);
     dialog.setContentView(R.layout.new_reminder_dialog);
     if (existingReminder == null) {
       dialog.setTitle("Create Reminder");
     } else {
       dialog.setTitle("Edit Reminder");
     }
     dialog.setOwnerActivity(this);
 
     Button submitButton = (Button) dialog.findViewById(R.id.Submit);
     Button cancelButton = (Button) dialog.findViewById(R.id.Cancel);
     final EditText reminderText = (EditText) dialog.findViewById(R.id.newReminderText);
     if (existingReminder != null) {
       reminderText.setText(existingReminder.getText());
     }
     submitButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         String text = reminderText.getText().toString();
         if (existingReminder != null) {
           _reminders.get(existingReminderIndex).setText(text);
           _globalPreferences.saveReminders(_nextMoonEvent, _reminders);
           getListAdapter().notifyDataSetChanged();
         } else {
           Reminder reminder = new Reminder(text);
           _reminders.add(reminder);
           _globalPreferences.saveReminders(_nextMoonEvent, _reminders);
           ReminderActivity.this.getListAdapter().notifyDataSetChanged();
         }
         dialog.dismiss();
       }
     });
     cancelButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         dialog.dismiss();
       }
     });
     dialog.show();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public ArrayAdapter<Reminder> getListAdapter() {
     return (ArrayAdapter<Reminder>) super.getListAdapter();
   }
 
   @Override
   protected void onPause() {
     saveChecks();
     super.onPause();
   }
 
   private void saveChecks() {
     for (int i = 0; i < _reminders.size(); i++) {
       _reminders.get(i).setChecked(getListView().isItemChecked(i));
     }
     _globalPreferences.saveReminders(_nextMoonEvent, _reminders);
   }
 
   private static enum ReminderContextMenu {
     EDIT("Edit") {
       @Override
       public void execute(ReminderActivity activity, int reminderIndex) {
         activity.newReminder(null, activity._reminders.get(reminderIndex), reminderIndex);
       }
     },
     DELETE("Delete") {
       @Override
       public void execute(ReminderActivity activity, int reminderIndex) {
         activity._reminders.remove(reminderIndex);
         activity._globalPreferences.saveReminders(activity._nextMoonEvent, activity._reminders);
         activity.getListAdapter().notifyDataSetChanged();
       }
     },
     MOVE_UP("Move Up") {
       @Override
       public void execute(ReminderActivity activity, int reminderIndex) {
         if (reminderIndex > 0) {
           moveReminderPostion(activity, reminderIndex, reminderIndex - 1);
         }
       }
     },
     MOVE_DOWN("Move Down") {
       @Override
       public void execute(ReminderActivity activity, int reminderIndex) {
         if (reminderIndex < activity.getListAdapter().getCount() - 1) {
           moveReminderPostion(activity, reminderIndex, reminderIndex + 1);
         }
       }
     };
 
     private final String _displayName;
 
     private ReminderContextMenu(String displayName) {
       _displayName = displayName;
     }
 
     public String getDisplayName() {
       return _displayName;
     }
 
     public abstract void execute(ReminderActivity activity, int reminderIndex);
 
     private static void moveReminderPostion(ReminderActivity activity, int reminderIndex, int toIndex) {
       Reminder reminder = activity._reminders.remove(reminderIndex);
       activity._reminders.add(toIndex, reminder);
       activity._globalPreferences.saveReminders(activity._nextMoonEvent, activity._reminders);
       activity.getListAdapter().notifyDataSetChanged();
     }
   }
 }
