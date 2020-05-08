 package net.analogyc.wordiary;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import net.analogyc.wordiary.dialogs.ConfirmDialogFragment;
 import net.analogyc.wordiary.dialogs.ConfirmDialogFragment.ConfirmDialogListener;
 import net.analogyc.wordiary.dialogs.EditEntryDialogFragment;
 import net.analogyc.wordiary.dialogs.EditEntryDialogFragment.EditEntryDialogListener;
 import net.analogyc.wordiary.models.DateFormats;
 import net.analogyc.wordiary.models.EntryFont;
 
 import java.io.File;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 public class EntryActivity extends BaseActivity implements EditEntryDialogListener, ConfirmDialogListener {
 
     private final int MOOD_RESULT_CODE = 101;
     private int mEntryId;
     private int mDayId;
     private TextView mMessageText, mDateText;
     private ImageView mPhotoButton, mMoodImage;
     private Button mSetNewMoodButton, mEditEntryButton, mPhotoDeleteButton;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent intent = getIntent();
         //get the id of the selected entry (normally mEntryId can't be -1)
         mEntryId = intent.getIntExtra("entryId", -1);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("entryId", mEntryId);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         if (savedInstanceState.containsKey("entryId")) {
             mEntryId = savedInstanceState.getInt("entryId");
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         setView();
     }
 
     /**
      * Catch mood result code (101) or send it over to BaseActivity for other codes (currently, photo -> 100)
      *
      * @param requestCode
      * @param resultCode
      * @param data
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == MOOD_RESULT_CODE) {
             if (resultCode == RESULT_OK) {
                 String moodId = data.getStringExtra("moodId");
                 mDataBase.updateMood(mEntryId, moodId);
                 setView();
             }
         } else {
             super.onActivityResult(requestCode, resultCode, data);
 
             if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                 if (resultCode == RESULT_OK) {
                     //If everything ok, update view
                     setView();
                 }
             }
         }
     }
 
     /**
      * Sets up the view content
      */
     protected void setView() {
         //set a new content view
         setContentView(R.layout.activity_entry);
 
         //get views references
         mMessageText = (TextView) findViewById(R.id.messageText);
         mDateText = (TextView) findViewById(R.id.dateText);
         mPhotoButton = (ImageView) findViewById(R.id.photoButton);
         mMoodImage = (ImageView) findViewById(R.id.moodImage);
 
         mSetNewMoodButton = (Button) findViewById(R.id.setNewMoodButton);
         mEditEntryButton = (Button) findViewById(R.id.editEntryButton);
         mPhotoDeleteButton = (Button) findViewById(R.id.photoDeleteButton);
 
         //if grace period is ended change button color
         if (!mDataBase.isEditableEntry(mEntryId)) {
             mSetNewMoodButton.setTextColor(0xFFBBBBBB);
             mEditEntryButton.setTextColor(0xFFBBBBBB);
         }
 
         // we keep this in onStart because the user might have changed the font in Preferences and come back to Entry
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         int typefaceInt = Integer.parseInt(preferences.getString("typeface", "1"));
         switch (typefaceInt) {
             case 2:
                 Typeface typeface = Typeface.createFromAsset(getAssets(), EntryFont.TYPEFACE_ANIMEACE_DIR);
                 mMessageText.setTypeface(typeface);
                 break;
             case 3:
                 Typeface typeface3 = Typeface.createFromAsset(getAssets(), EntryFont.TYPEFACE_STANHAND_DIR);
                 mMessageText.setTypeface(typeface3);
                 break;
             default:
                 mMessageText.setTypeface(Typeface.SANS_SERIF);
         }
 
         int fontSize = Integer.parseInt(preferences.getString("font_size", "2"));
         switch (fontSize) {
             case 1:
                 mMessageText.setTextSize(EntryFont.SIZE_SMALL);
                 break;
             case 3:
                 mMessageText.setTextSize(EntryFont.SIZE_BIG);
                 break;
             default:
                 mMessageText.setTextSize(EntryFont.SIZE_MEDIUM);
         }
 
         //get entry's informations from db
         Cursor c_entry = mDataBase.getEntryById(mEntryId);
         if (!c_entry.moveToFirst()) {
             //won't happen if MainActivity uses correct entryIds
             throw new RuntimeException("Wrong entry id");
         }
         //set message
         String message = c_entry.getString(2);
         mMessageText.setText(message);
         //set mood
         String mood = c_entry.getString(3);
         if (mood != null) {
             int identifier = getResources().getIdentifier(mood, "drawable", R.class.getPackage().getName());
             mMoodImage.setImageResource(identifier);
         }
         //set date
         String d_tmp = c_entry.getString(4);
         SimpleDateFormat format_in = new SimpleDateFormat(DateFormats.DATABASE, Locale.ITALY);
         SimpleDateFormat format_out = new SimpleDateFormat(DateFormats.ENTRY, Locale.ITALY);
         try {
             Date date = format_in.parse(d_tmp);
             mDateText.setText(format_out.format(date)); //probably a better method to do this exists
         } catch (ParseException e) {
             //won't happen if we use only dataBase.addEntry(...)
             mDateText.setText("??.??.????");
         }
 
         mDayId = c_entry.getInt(1);
 
         // make the image about square
         Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         DisplayMetrics dm = new DisplayMetrics();
         display.getMetrics(dm);
         mPhotoButton.setMaxWidth(dm.widthPixels);
         mPhotoButton.setMaxHeight(dm.widthPixels);
 
         Cursor c_photo = mDataBase.getDayById(mDayId);
         c_photo.moveToFirst();
         String path = null;
         if (!c_photo.getString(1).equals("")) {
             path = c_photo.getString(1);
         } else {
             mPhotoButton.setClickable(false);
             mPhotoDeleteButton.setVisibility(View.INVISIBLE);
         }
 
         mBitmapWorker.createTask(mPhotoButton, path)
                 .setShowDefault(mDayId)
                 .setTargetHeight(dm.widthPixels)
                 .setTargetWidth(dm.widthPixels)
                 .setHighQuality(true)
                 .setCenterCrop(true)
                 .execute();
         c_photo.close();
 
 
         c_entry.close();
 
 
         //check if this entry has a previous and a next
         if (!mDataBase.hasNextEntry(mEntryId, false)) {
             Button nextB = (Button) this.findViewById(R.id.nextEntryButton);
             nextB.setClickable(false);
             nextB.setTextColor(0xFFBBBBBB);
         }
         if (!mDataBase.hasNextEntry(mEntryId, true)) {
             Button prevB = (Button) this.findViewById(R.id.prevEntryButton);
             prevB.setClickable(false);
             prevB.setTextColor(0xFFBBBBBB);
         }
     }
 
     /**
      * Displays the full image
      *
      * @param view
      */
     public void onPhotoButtonClicked(View view) {
         Intent intent = new Intent(this, ImageActivity.class);
         intent.putExtra("dayId", mDayId);
         startActivity(intent);
     }
 
     /**
      * Displays the mood activity, only if within the grace period
      *
      * @param view
      */
     public void onMoodButtonClicked(View view) {
         if (!mDataBase.isEditableEntry(mEntryId)) {
             Toast toast = Toast.makeText(getBaseContext(), getString(R.string.grace_period_ended), TOAST_DURATION_S);
             toast.show();
             return;
         }
 
         Intent intent = new Intent(this, MoodsActivity.class);
         intent.putExtra("entryId", mEntryId);
         startActivityForResult(intent, MOOD_RESULT_CODE);
     }
 
     /**
      * Displays the panel for sharing the text of the entry
      *
      * @param view
      */
     public void onShareButtonClicked(View view) {
         Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
         sharingIntent.setType("text/plain");
         String shareBody = (String) mMessageText.getText();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
         startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
     }
 
     /**
      * Deletes the entry
      *
      * @param view
      */
     public void onDeleteButtonClicked(View view) {
         ConfirmDialogFragment newFragment = new ConfirmDialogFragment();
         newFragment.setId(0);
         newFragment.show(getSupportFragmentManager(), "Confirm");
 
     }
 
     /**
      * Called after a confirmDialog, this method could delete a photo or  delete an entry
      *
      * @param id the dialog id
      */
     public void onConfirmedClick(int id) {
 
         // 0 = deleteEntry, 1 = DeletePhoto
         switch (id) {
             case 0:
                 mDataBase.deleteEntryById(mEntryId);
                 Toast toast = Toast.makeText(getBaseContext(), getString(R.string.message_deleted), TOAST_DURATION_S);
                 toast.show();
                 if (mDataBase.hasNextEntry(mEntryId, true)) {
                     //move to the previous entry
                     moveNext(true);
                 } else {
                     //Now EntryActivity has no reason to be visible
                     finish();
                 }
                 break;
             case 1:
                 Cursor day = mDataBase.getDayById(mDayId);
                 day.moveToFirst();
                 String filename = day.getString(1);
                 day.close();
                 File photo = new File(filename);
                 if (photo.delete()) {
                     mDataBase.deletePhoto(mDayId);
                     Toast toast1 = Toast.makeText(getBaseContext(), R.string.photo_deleted, TOAST_DURATION_S);
                     toast1.show();
                 }
                 setView();
         }
     }
 
     /**
      * Deletes the entry
      *
      * @param view
      */
     public void onPhotoDelete(View view) {
         if (mDataBase.isEditableDay(mDayId)) {
             ConfirmDialogFragment newFragment = new ConfirmDialogFragment();
             newFragment.setId(1);
             newFragment.show(getSupportFragmentManager(), "Confirm");
         } else {
             Toast toast = Toast.makeText(getBaseContext(), R.string.grace_period_ended, TOAST_DURATION_S);
             toast.show();
         }
     }
 
     /**
      * Displays the dialog for editing the entry, only if within the grace period
      *
      * @param view
      */
     public void onEditButtonClicked(View view) {
         if (!mDataBase.isEditableEntry(mEntryId)) {
             Toast toast = Toast.makeText(getBaseContext(), getString(R.string.grace_period_ended), TOAST_DURATION_S);
             toast.show();
             return;
         }
 
         EditEntryDialogFragment editFragment = new EditEntryDialogFragment();
         Bundle args = new Bundle();
         args.putString("message", (String) mMessageText.getText());
         editFragment.setArguments(args);
         editFragment.show(getSupportFragmentManager(), "modifyEntry");
     }
 
     /**
      * Allows editing the content of the entry
      *
      * @param message The message that the user inputted
      */
     public void onDialogModifyClick(String message) {
         Context context = getApplicationContext();
         CharSequence text;
 
         String newMessage = message.trim();
 
         if (!newMessage.equals("")) {
             text = getString(R.string.message_saved);
             mDataBase.updateMessage(mEntryId, newMessage);
             setView();
         } else {
             text = getString(R.string.message_not_saved);
         }
 
         Toast toast = Toast.makeText(context, text, TOAST_DURATION_S);
         toast.show();
     }
 
     /**
      * Shows next entry
      *
      * @param view
      */
     public void nextEntryButtonClicked(View view) {
         moveNext(false);
     }
 
     /**
      * Shows previous entry
      *
      * @param view
      */
     public void prevEntryButtonClicked(View view) {
         moveNext(true);
     }
 
     /**
      * Provides and shows the information of the previous/next entry
      *
      * @param backwards true to go back, false otherwise
      */
     public void moveNext(boolean backwards) {
         Cursor nextEntry = mDataBase.getNextEntry(mEntryId, backwards);
         nextEntry.moveToFirst();
         int next = nextEntry.getInt(0);
         mEntryId = next;
         nextEntry.close();
         setView();
     }
 }
