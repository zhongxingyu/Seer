 package com.playlist.playlist_generator;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class MainActivity extends ListActivity implements OnClickListener {
     final String LOG_TAG = "myLogs";
     final int REQUEST_CODE_OPTION_FM = 1;
     final int REQUEST_CODE_OPTION_PL_FM = 2;
     final int SDK_VERSION = Integer.valueOf(android.os.Build.VERSION.SDK);
     public ArrayList<OptionsList> MusicOptionsList=new ArrayList<OptionsList>();
     public OtherBoxAdapter OptionBoxAdapter;
     private static final String[] EXTENSIONS = { ".mp3", ".mid", ".wav", ".ogg", ".mp4" }; //Playable Extensions
     private String PathToPL;
     private String PathToMusicFolder = "";
     File file;
     MediaScannerConnection msConn;
 
     private Intent IntentVar;
 
     List<String> trackNames; //Playable Track Titles
     Button btnSelectFolder; //Button Select Folder
     Button btnGeneratePL; //Button Generate Play List
     Button btnExitApp; //Exit application
     Button btnPathToPL;
 
     TextView tvPathToPL;
     EditText etPLName;
     TableLayout tlPLName;
     int ListSize;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)  {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         //Search button by ID
         btnSelectFolder = (Button) findViewById(R.id.button_select_folder);
         btnGeneratePL = (Button) findViewById(R.id.generate);
         btnExitApp = (Button) findViewById(R.id.ExitApp);
         btnPathToPL=(Button) findViewById(R.id.btnPathToPL);
 
         //Button click
         btnSelectFolder.setOnClickListener(this);
         btnGeneratePL.setOnClickListener(this);
         btnExitApp.setOnClickListener(this);
         btnPathToPL.setOnClickListener(this);
 
         tlPLName=(TableLayout) findViewById(R.id.tlPLName);
         tvPathToPL = (TextView) findViewById(R.id.tvPathToPL);
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()){
             case R.id.button_select_folder:
                 //Start Music File Manager activity
                 IntentVar = new Intent(this, FileManager_Activity.class);
                 if(PathToMusicFolder.equals("")){
                     IntentVar.putExtra("FirstChoice",true);
                     IntentVar.putExtra("PathToMusicFolder",PathToMusicFolder);
                 }
                 else
                 {
                     IntentVar.putExtra("FirstChoice",false);
                     IntentVar.putExtra("PathToMusicFolder",PathToMusicFolder);
                 }
                 startActivityForResult(IntentVar, REQUEST_CODE_OPTION_FM);
                 break;
             case R.id.generate:
                 PLGenerator_Button();
                 break;
             case R.id.ExitApp:
                 ExitApp();
                 break;
             case R.id.btnPathToPL:
                 //Start Path File Manager activity
                 IntentVar = new Intent(this, PL_FileManager_Activity.class);
                 String PathToPL = btnPathToPL.getText().toString();
                 if (!PathToPL.equals(getString(R.string.String_PathToPL))){
                     //Last choice
                     IntentVar.putExtra("PathToPL",PathToPL);
                 }
                 else {
                     //First choice
                     IntentVar.putExtra("PathToPL","False");
                 }
                 startActivityForResult(IntentVar, REQUEST_CODE_OPTION_PL_FM);
                 break;
             default:
                 break;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         // запишем в лог значения requestCode и resultCode
         Log.d("myLogs", "requestCode = " + requestCode + ", resultCode = " + resultCode);
         //If result is positive
         if (resultCode == RESULT_OK) {
             switch (requestCode) {
                 case REQUEST_CODE_OPTION_FM:
                     //TODO Auto-generated method stub
                     ArrayList<String> PathList = data.getStringArrayListExtra("ArrayMusicDirList");
                     PathToMusicFolder = data.getStringExtra("MainFolderPath");
 
                     String Option = AddOption(PathList);
                     ListSize=MusicOptionsList.size()+1;
                     //MusicOptionsList.add(new OptionsList("Option " + ListSize,PathList,R.drawable.ic_launcher,""));
                     MusicOptionsList.add(new OptionsList(Option,PathList,R.drawable.ic_launcher,""));
                     OptionBoxAdapter = new OtherBoxAdapter(this, MusicOptionsList);
 
                     ListView lvMain = (ListView) findViewById(android.R.id.list);
                     lvMain.setAdapter(OptionBoxAdapter);
                     lvMain.setItemsCanFocus(true);
                     checkList();
                     break;
                 case REQUEST_CODE_OPTION_PL_FM:
                     PathToPL = data.getStringExtra("PathToPL");
                     btnPathToPL.setText(PathToPL);
                     break;
             }
         }
     }
 
     //Check if list exists. If so - unhide button Generate
     private void checkList(){
         if(MusicOptionsList.size()!=0){
             btnGeneratePL.setVisibility(View.VISIBLE);
             tlPLName.setVisibility(View.VISIBLE);
             btnPathToPL.setVisibility(View.VISIBLE);
             tvPathToPL.setVisibility(View.VISIBLE);
         }
         else{
             btnGeneratePL.setVisibility(View.GONE);
             tlPLName.setVisibility(View.GONE);
             btnPathToPL.setVisibility(View.GONE);
             tvPathToPL.setVisibility(View.GONE);
         }
     }
 
     //Generate playlist
     private void PLGenerator_Button(){
         String ItemPath;
         Intent UpdateMediaIntent;
         UpdateMediaIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
 
         trackNames = new ArrayList<String>();
         ArrayList<ArrayList<String>> dirFiles = new ArrayList<ArrayList<String>>();
         //Looking for directories with songs from Folder Lists
         for(int i=0;i < MusicOptionsList.size();i++){
             dirFiles.add(new ArrayList<String>());
             for(int j=0; j < MusicOptionsList.get(i).getOptionPathSize(); j++){
                 ItemPath = MusicOptionsList.get(i).getPath(j);
                 addTracks(getTracks(ItemPath, dirFiles.get(i)));
             }
             Collections.shuffle(dirFiles.get(i));
         }
         CreatePList(dirFiles);
 
         //Updates Media Files indexes in memory
         if (SDK_VERSION < 19){
             sendBroadcast(UpdateMediaIntent);
         }
 
         Toast.makeText(getBaseContext(), getResources().getString(R.string.Done), Toast.LENGTH_SHORT).show();
     }
 
     private void CreatePList(ArrayList<ArrayList<String>> OptionsFilesList){
         //Try code and catch exceptions
         String PLName;
         ArrayList<Integer> SongCounterList = new ArrayList<Integer>();
         long NumOfSongs = 0;
         long NumOfIterations;
         long index=0;
         int arr_of_indexes[][]; //[0][1]=3 - SongCounterList.get(0) - start position will be 3
         //[0][0]=2 - SongCounterList.get(0) = 2, num song per option. The same as SongCounterList
         int counter;
 
         arr_of_indexes = new int [OptionsFilesList.size()][2];
         try {
             PLName=PLName();
             //new file for Playlist
             file = new File(Environment.getExternalStorageDirectory() + "/Music",PLName);
             PrintWriter writer = new PrintWriter(file, "utf-8");
 
             // Save song counters values
             for (int i=0;i<OptionsFilesList.size();i++){
                 SongCounterList.add(SongCounter(i));
                 if (SongCounterList.get(i)!=0 && NumOfSongs==0){
                     NumOfSongs=OptionsFilesList.get(i).size();
                 }
                 arr_of_indexes[i][0]=SongCounterList.get(i);
                 arr_of_indexes[i][1]=0;
             }
 
             NumOfIterations = NumOfSongs/SongCounterList.get(0);
             while (index!=NumOfIterations){
                 index=index+1;
                 for (int i=0; i<OptionsFilesList.size(); i++){
                     counter=0;
                     if (SongCounterList.get(i)!=0){
                         for(int j=arr_of_indexes[i][1]; j<OptionsFilesList.get(i).size(); j++){
                             // Write path to song to the file
                             writer.println(OptionsFilesList.get(i).get(j)+"\r");
                             counter=counter+1;
                             if(counter==arr_of_indexes[i][0]){ //num of songs per option equals counter
                                 arr_of_indexes[i][1]=j+1;
                                 break;
                             }
                         }
                     }
                 }
             }
            /* ensure that everything is
             * really written out and close */
             writer.flush();
             writer.close();
 
         }
         catch (IOException ioe)
         {ioe.printStackTrace();}
     }
 
     private int SongCounter(int index){
         ListView lvMain = (ListView) findViewById(android.R.id.list);
         View view;
         EditText etSongCounter;
         Integer SongCounter=0;
 
         view = lvMain.getChildAt(index);
         etSongCounter=(EditText) view.findViewById(R.id.OptionSongCounter);
         try {
             SongCounter = Integer.parseInt(etSongCounter.getText().toString());
             if (SongCounter==0){
                 Log.d(LOG_TAG, "Счетчик песен равен 0. Ошибка ");
             }
         }
         catch (NumberFormatException ioe)
         {
             Log.d(LOG_TAG, "Не удалось конвертировать счестчик песен в тип Long ");
             SongCounter= 1;
         }
         catch (NullPointerException npe){
             SongCounter= 1;
         }
         return SongCounter;
     }
 
     public void DelListElemButton(View v){
         //TODO Auto-generated method stub
         int itemToRemove  = getListView().getPositionForView(v);
         MusicOptionsList.remove(itemToRemove);
         OptionBoxAdapter = new OtherBoxAdapter(this, MusicOptionsList);
 
         ListView lvMain = (ListView) findViewById(android.R.id.list);
         lvMain.setAdapter(OptionBoxAdapter);
         lvMain.setItemsCanFocus(true);
         checkList();
     }
 
     //Generate a String Array that represents all of the files found
     private ArrayList<String> getTracks(String directoryName, ArrayList<String> files) {
         File directory = new File(directoryName);
 
         // get all the files from a directory
         File[] fList = directory.listFiles();
         for (File file : fList) {
             if (file.isFile()) {
                 if(trackChecker(file.getName())){
                     files.add(file.getAbsolutePath());
                 }
             } else if (file.isDirectory()) {
                 getTracks(file.getAbsolutePath(), files);
             }
         }
         return files;
     }
 
     //Adds the playable files to the trackNames List
     private void addTracks(ArrayList<String>  dirFiles){
         if(dirFiles != null){
             for(int i = 0; i < dirFiles.size(); i++){
                 //Only accept files that have one of the extensions in the EXTENSIONS array
                 if(trackChecker(dirFiles.get(i))){
                     trackNames.add(dirFiles.get(i));
                 }
             }
         }
     }
 
     //Checks to make sure that the track to be loaded has a correct extenson
     public boolean trackChecker(String trackToTest){
         for (String EXTENSION : EXTENSIONS) {
             if (trackToTest.contains(EXTENSION)) {
                 return true;
             }
         }
         return false;
     }
 
     private void ExitApp(){
         finish();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //TODO Auto-generated method stub
         // Inflate the menu; this adds items to the action bar if it is present.
         menu.add("menu1");
         menu.add("menu2");
         menu.add("menu3");
         menu.add("menu4");
 
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     private void CreatePListWoutOptions(ArrayList<ArrayList<String>> OptionsFilesList){
         //TODO Auto-generated method stub
         //Try code and catch exceptions
         String PLName;
         try {
             //Check for mounted SD and create new file for Playlist
             PLName = PLName();
 
             File file = new File(Environment.getExternalStorageDirectory() + "/Music",PLName);
             PrintWriter writer = new PrintWriter(file, "utf-8");
 
             // Write path to song to the file
             for (int i=0;i<OptionsFilesList.size();i++){
                 for(int j=0;j<OptionsFilesList.get(i).size();j++){
                     writer.println(OptionsFilesList.get(i).get(j)+"\r");
                 }
             }
            /* ensure that everything is
             * really written out and close */
             writer.flush();
             writer.close();
         }
         catch (IOException ioe)
         {ioe.printStackTrace();}
     }
 
     private String PLName(){
         String PLName;
         //Check for mounted SD and create name for PL
         if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
             Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
         }
         etPLName=(EditText) findViewById(R.id.etPLName);
         if(etPLName.getText().toString().length() == 0){
             PLName = getString(R.string.etPLName);
         }
         else{
             PLName = etPLName.getText().toString();
         }
         PLName = PLName + ".m3u";
         return PLName;
     }

     private String AddOption(ArrayList<String> PathList){
         String Option = getResources().getString(R.string.Option);
         for(String OptionPath : PathList){
             String SubOption = "";
             for(int i = OptionPath.length(); i > -1; i--){
                 if(OptionPath.indexOf("/", i)>=0){
                     if(SubOption.equals("")){
                         SubOption = OptionPath.substring(i);
                     }
                     else{
                         SubOption = ".." + SubOption;
                         break;
                     }
                 }
             }
             Option = Option + " " + SubOption + ";";
         }
         return Option;
     }
 
 }
