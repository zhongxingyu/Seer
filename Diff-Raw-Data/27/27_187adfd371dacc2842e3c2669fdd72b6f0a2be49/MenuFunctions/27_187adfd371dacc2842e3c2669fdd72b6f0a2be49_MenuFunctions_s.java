 package pl.tabhero.core;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import pl.tabhero.HelpActivity;
 import pl.tabhero.R;
 import pl.tabhero.db.DBUtils;
 import pl.tabhero.local.EditFavPerfs;
 import pl.tabhero.local.EditFavTitles;
 import pl.tabhero.local.FavoritesActivity;
 import pl.tabhero.local.FavoritesTitleActivity;
 import pl.tabhero.utils.FileUtils;
 import pl.tabhero.utils.MyFilter;
 import pl.tabhero.utils.selector.SelectArralAdapter;
 import pl.tabhero.utils.selector.ItemsOnCheckboxList;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.InputFilter;
import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MenuFunctions {
 
     private Context context;
     private Activity activity;
     private DBUtils dbUtils;
     private final String config;
     private final String maximize;
     private final String minimize;
     private String className;
     private static final String FAV_PERF_VIEW = FavoritesActivity.class.getSimpleName();
     private static final String FAV_TITLE_VIEW = FavoritesTitleActivity.class.getSimpleName();
     private final String googleSearch;
     private final String spaceCode;
     private final String lyricsString;
     private final String isNotChecked;
     private final String isChecked;
     private final String defaultSize;
 
     public MenuFunctions(Context context) {
         this.context = context;
         this.activity = (Activity) context;
         this.dbUtils = new DBUtils(this.context);
         this.className = this.activity.getClass().getSimpleName();
         googleSearch = this.context.getString(R.string.googleUrl);
         spaceCode = this.context.getString(R.string.spaceUrl);
         lyricsString = this.context.getString(R.string.lyricsUrl);
         config = this.context.getString(R.string.configNameFile);
         maximize = this.context.getString(R.string.configMAX);
         minimize = this.context.getString(R.string.configMIN);
         isNotChecked = this.context.getString(R.string.isNotChecked);
         isChecked = this.context.getString(R.string.isChecked);
         defaultSize = this.context.getString(R.string.defaultSize);
     }
 
     public void addToFav(String performer, String title, String tab, String songUrl) {
         if (!this.dbUtils.isExistRecordByUrl(songUrl)) {
             if (this.dbUtils.isIdExistByUrl(songUrl)) {
                 buildAlertDialogToAddTabWhithSameId(performer, title, tab, songUrl);
             } else {
                 this.dbUtils.addTab(performer, title, tab, songUrl);
             }
         } else {
             Toast.makeText(this.context.getApplicationContext(),
                     R.string.recordExist, Toast.LENGTH_LONG).show();
         }
     }
 
     public void delFromFav(final String songUrl) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
         //builder.setTitle("Confirm");
         builder.setMessage(R.string.areYouSure);
         builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dbUtils.deleteTab(songUrl);
                 dialog.dismiss();
                 Intent i = new Intent(context, FavoritesTitleActivity.class);
                 context.startActivity(i);
                 context.startActivity(i);
                 activity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
             }
         });
         builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 Toast.makeText(context.getApplicationContext(),
                         R.string.notDelFromBase, Toast.LENGTH_LONG).show();
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void editTab(String tablature, String songUrl) {
         Writer writer;
         FileUtils fileUtils = new FileUtils(this.context);
         fileUtils.makePaths(songUrl);
         fileUtils.makeFiles();
         if (!fileUtils.getOutDir().isDirectory()) {
             fileUtils.getOutDir().mkdir();
         }
         try {
             if (!fileUtils.getOutDir().isDirectory()) {
                 throw new IOException(this.context.getString(R.string.createDirectoryError)
                         + this.context.getPackageName() + "." + this.context.getString(R.string.sdcardMountError));
             }
             File outputFile = new File(fileUtils.getOutDir(), fileUtils.getFileName());
             writer = new BufferedWriter(new FileWriter(outputFile));
             writer.write(tablature);
             Toast.makeText(this.context.getApplicationContext(), this.context.getString(R.string.sdcardWriteOK)
                     + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
             writer.close();
         } catch (IOException e) {
             Toast.makeText(this.context.getApplicationContext(),
                     e.getMessage() + this.context.getString(R.string.sdcardWriteError),
                     Toast.LENGTH_LONG).show();
         }
     }
 
     public void openWebBrowser(String performer, String title) {
         String perf = performer.replaceAll(" ", spaceCode);
         String tit = title.replaceAll(" ", spaceCode);
         String question = googleSearch + perf + spaceCode + tit + spaceCode + lyricsString;
         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(question));
         this.context.startActivity(browserIntent);
         this.activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
     }
 
     private void buildAlertDialogToAddTabWhithSameId(final String performer, final String title, final String tab, final String songUrl) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
         builder.setMessage(R.string.tabExist);
         builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 dbUtils.addTab(performer, title, tab, songUrl);
                 dialog.dismiss();
             }
         });
 
         builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 Toast.makeText(context.getApplicationContext(),
                         R.string.notAddTab, Toast.LENGTH_LONG).show();
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void minMax() throws IOException {
         String maxForConfig;
         boolean fullScreen = (this.activity.getWindow().getAttributes().flags
                 & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
         if (fullScreen) {
             this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                     WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
             maxForConfig = minimize;
         } else {
             this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
             this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                     WindowManager.LayoutParams.FLAG_FULLSCREEN);
             maxForConfig = maximize;
         }
         FileUtils fileUtils = new FileUtils(this.context);
         String configPath = fileUtils.getDir() + File.separator + config;
         File file = new File(configPath);
         if (file.isFile()) {
             String text = fileUtils.readConfig(file);
             String textSize = text.split(",")[1];
             String checkBoxResult = text.split(",")[2];
             fileUtils.writeForConfig(file, maxForConfig, textSize, checkBoxResult);
         } else {
             Toast.makeText(this.context.getApplicationContext(),
                     this.context.getString(R.string.configReadError), Toast.LENGTH_LONG).show();
         }
     }
 
     private InputFilter filter = new MyFilter();
 
     public void buildAlertDialogToAddOwnTab() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
         final EditText inputPerf = new EditText(this.context);
         inputPerf.setFilters(new InputFilter[]{filter});
         builder.setMessage(R.string.addOwnPerf);
         builder.setView(inputPerf);
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 String newPerfName = inputPerf.getText().toString();
                 buildAlertDialogNewTitle(newPerfName);
                 dialog.dismiss();
             }
         });
 
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void buildAlertDialogNewTitle(final String newPerfName) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
         final EditText inputTitle = new EditText(this.context);
         inputTitle.setFilters(new InputFilter[]{filter});
         builder.setMessage(this.context.getString(R.string.addOwnTitle) + " " + newPerfName);
         builder.setView(inputTitle);
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 String newSongTitle = inputTitle.getText().toString();
                 dbUtils.addToBaseNewRecord(newPerfName, newSongTitle);
                 Intent refresh = null;
                 if (className.equals(FAV_PERF_VIEW)) {
                     refresh = new Intent(activity, FavoritesActivity.class);
                 } else if (className.equals(FAV_TITLE_VIEW)) {
                     refresh = new Intent(activity, FavoritesTitleActivity.class);
                 }
                 activity.startActivity(refresh);
                 dialog.dismiss();
             }
         });
 
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void startEditPerfActivity(ArrayList<String> listToEdit) {
         Intent i = new Intent(this.activity, EditFavPerfs.class);
         Bundle bun = new Bundle();
         bun.putStringArrayList("listOfPerformers", listToEdit);
         i.putExtras(bun);
         this.context.startActivity(i);
         this.activity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
     }
 
     public void startEditTitleActivity(ArrayList<String> listToEditTitles, ArrayList<String> listToEditUrl) {
         Intent i = new Intent(this.activity, EditFavTitles.class);
         Bundle bun = new Bundle();
         bun.putStringArrayList("listOfTitles", listToEditTitles);
         bun.putStringArrayList("listOfUrl", listToEditUrl);
         i.putExtras(bun);
         this.context.startActivity(i);
         this.activity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
     }
 
     public void buildAlertDialogToChangeRecordName(final String oldRecordName, final String url) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
         final EditText input = new EditText(this.context);
         input.setFilters(new InputFilter[]{filter});
         if (className.equals(FAV_PERF_VIEW)) {
             builder.setMessage(R.string.changePerf);
         } else if (className.equals(FAV_TITLE_VIEW)) {
             builder.setMessage(R.string.changeTitle);
         }
         input.setText(oldRecordName);
         builder.setView(input);
 
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 String newRecordName = input.getText().toString();
                 if (className.equals(FAV_PERF_VIEW)) {
                     dbUtils.changePerfName(newRecordName, oldRecordName);
                     refresh();
                 } else if (className.equals(FAV_TITLE_VIEW)) {
                     dbUtils.changeSongTitle(newRecordName, url);
                     refresh();
                 }
                 dialog.dismiss();
             }
         });
 
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void refresh() {
         Intent refresh = null;
         if (className.equals(FAV_PERF_VIEW)) {
             refresh = new Intent(this.context, FavoritesActivity.class);
         } else if (className.equals(FAV_TITLE_VIEW)) {
             refresh = new Intent(this.context, FavoritesTitleActivity.class);
         }
         this.activity.startActivity(refresh);
     }
 
     public void checkReverse(ArrayList<ItemsOnCheckboxList> recordCheckList, ListView delListView) {
         for (ItemsOnCheckboxList perf : recordCheckList) {
             if (perf.isChecked()) {
                 perf.setChecked(false);
             } else {
                 perf.setChecked(true);
             }
         }
         ArrayAdapter<ItemsOnCheckboxList> listAdapter = new SelectArralAdapter(this.context, recordCheckList);
         delListView.setAdapter(listAdapter);
     }
 
     public void showInfo() {
         String versionCode = "";
         String appName = "";
         try {
             appName = this.context.getString(R.string.app_name);
             versionCode = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionName;
         } catch (NameNotFoundException e) {
             Toast.makeText(this.context.getApplicationContext(),
                     R.string.versionReadError, Toast.LENGTH_LONG).show();
         }
         final Dialog builder = new Dialog(this.context);
         builder.setContentView(R.layout.builderlayout);
         builder.setTitle(R.string.info);
         TextView text = (TextView) builder.findViewById(R.id.textDialog);
         text.setText(this.context.getString(R.string.message_info) + "\n\n"
                 + appName + " v" + versionCode);
         Button dialogButtonHelp = (Button) builder.findViewById(R.id.dialogButtonHelp);
         Button dialogButtonOK = (Button) builder.findViewById(R.id.dialogButtonOK);
         dialogButtonHelp.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(context, HelpActivity.class);
                 context.startActivity(intent);
                 activity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                 builder.dismiss();
             }
         });
         dialogButtonOK.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 builder.dismiss();
             }
         });
         builder.show();
     }
 
     public void firstRun() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
         builder.setTitle(R.string.hello);
         builder.setMessage(this.context.getString(R.string.firstRun));
         builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
         builder.setPositiveButton(R.string.help, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 Intent intent = new Intent(context, HelpActivity.class);
                 context.startActivity(intent);
                 activity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
     
     public void searchTitleRun() throws IOException {
         final FileUtils fileUtils = new FileUtils(this.context);
         String configPath = fileUtils.getDir() + File.separator + config;
         final File file = new File(configPath);
         String mMaxForConfig = minimize;
         String mTextSize = defaultSize;
         String mCheckBoxResult = isNotChecked;
         if (file.isFile()) {
             String text = fileUtils.readConfig(file);
             mMaxForConfig = text.split(",")[0];
             mTextSize = text.split(",")[1];
             mCheckBoxResult = text.split(",")[2];
         } else {
             Toast.makeText(this.context.getApplicationContext(),
                     this.context.getString(R.string.configReadError), Toast.LENGTH_LONG).show();
         }
         if (mCheckBoxResult.contains(isNotChecked)) {
             final String maxForConfig = mMaxForConfig;
             final String textSize = mTextSize;
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            LayoutInflater builderInflater = LayoutInflater.from(this.context);
            View searchTitleCheckboxLayout = builderInflater.inflate(R.layout.checkbox_in_search_title, null);
            final CheckBox dontShowAgain = (CheckBox) searchTitleCheckboxLayout.findViewById(R.id.skip);
            builder.setView(searchTitleCheckboxLayout);
             builder.setTitle(R.string.hint);
            builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                     String checkBoxResult = isNotChecked;
                     if (dontShowAgain.isChecked()) {
                     checkBoxResult = isChecked;
                     }
                     try {
                         fileUtils.writeForConfig(file, maxForConfig, textSize, checkBoxResult);
                     } catch (IOException e) {
                         Toast.makeText(context.getApplicationContext(),
                                 context.getString(R.string.configWriteError), Toast.LENGTH_LONG).show();
                     }
                    dialog.dismiss();
                 }
             });
            AlertDialog alert = builder.create();
            alert.show();
         }
     }
 }
