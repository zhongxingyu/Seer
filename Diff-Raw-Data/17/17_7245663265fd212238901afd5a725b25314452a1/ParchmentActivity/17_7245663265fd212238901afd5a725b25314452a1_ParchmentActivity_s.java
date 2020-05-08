 
 package com.n00bware.parchment;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.Editable;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /* 
  * TODO move .close() statements to finally blocks
  * nomenclature object prefixes are changing; to claify
  *p ParchmentActivity objects; Content from layout/main.xml
  *s Content from layout/save.xml
  *o Content from layout/open.xml
  */
 
 public class ParchmentActivity extends Activity {
 
     private final String TAG = "Parchment";
     private final String FILENAME = "filename";
     private final String DIRECTORY = "directory";
     private final String DEFAULT_OPEN_PATH = "/sdcard/parchment/parchment_test";
     private final String BLANK = "";
     private final String SAVE_MARKER = "saved_text";
     private final String LAST_INDEX = "last_index";
     private final String SAVE_ALERT_TITLE = "Save as %s";
     private String pFilename = new String();
     private String textContainer;
     private String pDir;
     private String pContents;
 
     private final int NEW = 1;
     private final int SAVE = 2;
     private final int SAVE_AS = 3;
     private final int OPEN = 4;
 
     private boolean mIsNewFile = false;
 
     private Dialog saveDialog;
     private Dialog openDialog;
     private File pFile;
     private File root = Environment.getExternalStorageDirectory();
     private SharedPreferences pSharedPrefs;
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setTitle(R.string.default_title);
         setContentView(R.layout.main);
 
         final EditText old_text = (EditText)findViewById(R.id.pDoc);
         final String pdoc_txt = old_text.getText().toString();
 
         try {
             String titleHolder = pFile.getAbsolutePath();
             if (titleHolder != null) {
                 setTitle(titleHolder);
             }
         } catch (NullPointerException npe) {
             npe.printStackTrace();
         }
 
         File parchment_path = new File(DEFAULT_OPEN_PATH);
         if (!parchment_path.isDirectory()) {
             parchment_path.getParentFile().mkdir();
         }
         if (parchment_path == null) {
             Log.d(TAG, "shit nulled return from parchment_path");
         } else {
             Log.d(TAG, "parchment_path returned " + parchment_path.getParentFile().getName());
         }
 
         Bundle args = getIntent().getExtras();
         if (args != null) {
             pFilename = args.getString(FILENAME);
             setTitle(pFilename);
             Log.d(TAG, "args: " + pFilename);
         } else {
             setTitle(R.string.default_title);
             Log.d(TAG, "args: null");
         }
 
         if (!root.canWrite()) {Log.d(TAG, "sdcard is not writable");} else {Log.d(TAG, "sdcard is writable");}
 
         pSharedPrefs = getPreferences(MODE_PRIVATE);
         //TODO: best to start @ /# or sdcard/parchment/#
         pDir = pSharedPrefs.getString(DIRECTORY, "/");
         /*
          * TODO: read only is important but we should look into
          * allowing su save abilities
          */
         isReadOnly();
     }
 
     /* onStart should be called when orentation changes hopefully now we won't lose data from TextEdits this way */
     @Override
     public void onStart(){
         super.onStart();
         setTitle(R.string.default_title);
         setContentView(R.layout.main);
 
         pContents = pSharedPrefs.getString(SAVE_MARKER, BLANK);
         if (!pContents.equals(BLANK)) {
             EditText contents = (EditText)findViewById(R.id.pDoc);
             contents.setText(pContents);
         }
     }
 
     protected void onPause() {
         super.onPause();
 
         if (pFilename.equals(BLANK)) {
             EditText container = (EditText)findViewById(R.id.pDoc);
             String text = container.getText().toString();
             if (text != null) {
                 SharedPreferences.Editor prefEditor = pSharedPrefs.edit();
                 prefEditor.putString(SAVE_MARKER, text);
                 prefEditor.commit();
             }
         }
     }
 
     protected void onResume() {
         super.onResume();
 
         if (pFilename.equals(BLANK)) {
             pContents = pSharedPrefs.getString(SAVE_MARKER, BLANK);
             EditText contents = (EditText)findViewById(R.id.pDoc);
             contents.setText(pContents);
         }
     }
 
     private void isReadOnly() {
         Log.d(TAG, "isReadOnly");
         pFile = new File(pFilename);
 
         if (pFile.canWrite()) {Log.d(TAG, "canWrite() true");}
         if (pFile.exists()) {Log.d(TAG, "exists() true");}
         if (pFile.canRead()) {Log.d(TAG, "canRead() true");}
         String file_path = pFile.getAbsolutePath();
         Log.d(TAG, String.format("file path { %s }", file_path));
         Log.d(TAG, pFile.toString());
 
         boolean mExternalStorageAvailable = false;
         boolean mExternalStorageWriteable = false;
         String state = Environment.getExternalStorageState();
 
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             mExternalStorageAvailable = mExternalStorageWriteable = true;
             Log.d(TAG, "sdcard mounted");
         } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
             mExternalStorageAvailable = true;
             mExternalStorageWriteable = false;
             Log.d(TAG, "sdcard readonly");
         } else {
             mExternalStorageAvailable = mExternalStorageWriteable = false;
             Log.d(TAG, "epic failure");
         }
 
         loadText();
     }
 
     private void loadText() {
         try {
             Log.d(TAG, "loadText");
             byte[] buffer = new byte[(int)pFile.length()];
             FileInputStream reader = new FileInputStream(pFile);
             reader.read(buffer);
 
             textContainer = new String(buffer);
             EditText eText = (EditText)findViewById(R.id.pDoc);
             eText.setText(textContainer);
             setTitle(pFilename);
             Log.d(TAG, eText.getText().toString());
             reader.close();
         } catch(IOException e) {
             Log.d(TAG, "Parsing error while loading " + pFilename);
         }
     }
 
     private void writeFile() {
         try {
             EditText eText = (EditText)findViewById(R.id.pDoc);
             String text = eText.getText().toString();
             FileWriter fWrite = new FileWriter(pFile);
             Log.d(TAG, String.format("text we are attempting to write { %s }", text));
             try {
                 fWrite.write(text);
                 fWrite.flush();
                 fWrite.close();
             } catch (IOException IOe) {
                 Log.d(TAG, "IOException while FileWriter was writing file");
                 IOe.printStackTrace();
             }
 
             String dir = LAST_INDEX;
             Log.d(TAG, String.format("LAST_INDEX { %s } dir { %s }", LAST_INDEX, dir));
             SharedPreferences.Editor sEdit = pSharedPrefs.edit();
             sEdit.putString(LAST_INDEX, dir);
             sEdit.commit();
         } catch(IOException e) {
             Log.d(TAG, "Failed to write " + pFilename);
         } catch (Exception e) {
             e.printStackTrace();
         }
         trustButVerify();
     }
 
     private void newFile() {
         final EditText old_text = (EditText)findViewById(R.id.pDoc);
         String pdoc_txt = old_text.getText().toString();
 
         if (!pdoc_txt.equals(BLANK)) {
             new AlertDialog.Builder(this)
             .setTitle(R.string.new_file_title)
             .setPositiveButton(R.string.save_button_text, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whatButton) {
                     mIsNewFile = true;
                     savePrompt();
                 }
             })
             .setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whatButton) {
                     old_text.setText(BLANK);
                 }
             })
             .show();
         }
     }
 
     private void open() {
         openDialog = new Dialog(this);
         openDialog.setContentView(R.layout.open);
         openDialog.setTitle("Open file... { full path }");
         final EditText open_filename = (EditText) openDialog.findViewById(R.id.oNewFile);
         open_filename.setSingleLine();
 
         if (pFilename != null) {
             open_filename.setText(pFilename);
         } else {
             open_filename.setText(DEFAULT_OPEN_PATH);
         }
 
         final Button oButton = (Button) openDialog.findViewById(R.id.oButton);
         oButton.setOnClickListener(new Button.OnClickListener() {
             public void onClick(View v) {
                 pFilename = open_filename.getText().toString();
                 File valid_path = new File(pFilename);
                 if (valid_path.isFile()) {
                     Log.d(TAG, pFilename);
                     isReadOnly();
                     openDialog.dismiss();
                 } else {
                     Log.d(TAG, String.format("%s does not exist", pFilename));
                     Toast.makeText(getApplicationContext(), String.format("%s is not a valid path", pFilename), Toast.LENGTH_SHORT).show();
                 }
             }
         });
         openDialog.show();
     }
 
     private void savePrompt() {
         saveDialog = new Dialog(this);
         saveDialog.setContentView(R.layout.save);
         saveDialog.setTitle("Save as... { full path }");
         EditText filename = (EditText) saveDialog.findViewById(R.id.sNewFile);
         filename.setSingleLine();
         filename.setText(pDir);
         final String entered_path = filename.getText().toString();
 
         final Button sButton = (Button) saveDialog.findViewById(R.id.sButton);
         sButton.setOnClickListener(new Button.OnClickListener() {
             public void onClick(View v) {
                 if (entered_path.equals(BLANK)) {
                     Toast.makeText(getApplicationContext(), "please enter full path", Toast.LENGTH_SHORT).show();
                 } else {
                     saveAs();
                 }
             }
         });
 
         saveDialog.show();
     }
 
     private void saveAs() {
         EditText fname = (EditText) saveDialog.findViewById(R.id.sNewFile);
         final EditText old_text = (EditText)findViewById(R.id.pDoc);
         final String pdoc_txt = old_text.getText().toString();
         String filename = fname.getText().toString();
         File newFile = new File(Environment.getExternalStorageDirectory() + "/" + filename);
         String debug_filename = newFile.getAbsolutePath();
         if (!newFile.exists()) {Log.d(TAG, "saveAs newFile !exists");}
         if (!newFile.canWrite()) {Log.d(TAG, "saveAs newFile canWrite=false");}
         if (!newFile.canRead()) {Log.d(TAG, "saveAs newFile canRead=false");}
         Log.d(TAG, "the file we are attempting to save is " + debug_filename);
 
         pFile = newFile;
         Log.d(TAG, pFile.getAbsolutePath());
 
         if (!newFile.isFile()) {
             new AlertDialog.Builder(this)
             .setTitle(String.format(SAVE_ALERT_TITLE, pFile.getAbsolutePath()))
             .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whatButton) {
                     saveDialog.dismiss();
                    if (mIsNewFile) {
                        old_text.setText(BLANK);
                    }
                     mIsNewFile = false;
                     writeFile();
                 }
             })
             .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whatButton) {
                     saveDialog.dismiss();
                     mIsNewFile = false;
                 }
             })
             .show();
         } else {
             Log.d(TAG, "if not a file already");
             saveDialog.dismiss();
             mIsNewFile = false;
             writeFile();
         }
     }
 
     private void trustButVerify() {
         String pFile_path = pFile.getAbsolutePath();
         File trust = new File(pFile_path);
         Log.d(TAG, String.format("pFile {%s} pFilename {%s}", pFile_path, pFilename));
         if (trust.exists() && trust.canWrite() && trust.canRead()) {
             Log.d(TAG, "Trust but verify ... all good here");
             Toast.makeText(getApplicationContext(), String.format("%s has been saved", pFile_path), Toast.LENGTH_SHORT).show();
         } else {
             Log.d(TAG, "Trust but verify ...failed combined checks");
             Toast.makeText(getApplicationContext(), String.format("%s has not been save", pFile_path), Toast.LENGTH_SHORT).show();
         }
 
         if (trust.exists()) {
             Log.d(TAG, String.format("Trust but verify ... '%s' does in fact exist", pFile_path));
         } else {Log.d(TAG, "Trust but verify ... failed exists()");}
 
         if (trust.canRead()) {
             Log.d(TAG, "Trust but verify ... we can read " + pFile_path);
         } else {Log.d(TAG, "Trust but verify ...failed canRead()");}
 
         if (trust.canWrite()) {
             Log.d(TAG, "Trust but verify ... we can write " + pFile_path);
         } else {Log.d(TAG, "Trust but verify ... failed canWrite()");}
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean pick = super.onCreateOptionsMenu(menu);
         menu.add(0, NEW, 0, "New").setIcon(R.drawable.new_file);
         menu.add(0, SAVE, 0, "Save").setIcon(R.drawable.save);
         menu.add(0, SAVE_AS, 0, "Save as").setIcon(R.drawable.save_as);
         menu.add(0, OPEN, 0, "Open").setIcon(R.drawable.open);
         return pick;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case NEW:
                 newFile();
                 break;
             case SAVE:
                 writeFile();
                 break;
             case SAVE_AS:
                 savePrompt();
                 break;
             case OPEN:
                 open();
         }
     return false;
     }
 }
