 
 package ualberta.g12.adventurecreator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ImageView.ScaleType;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 
 // Right now we are making Fragments here and also choice
 // What i was thinking was maybe just make the general fragment here
 // and have another screen to add choices then we could have list a listview type thing so can have a lot of choices
 // and long clicking each one would let you edit/delete them 
 // TODO discuss with the team
 //
 public class EditFragmentActivity extends Activity implements FView<Fragment> {
 
     private int storyId, position, type, picturePosition;
     private TextView fragmentTitleTextView;
     private ListView fragmentPartListView;
     private FragmentPartAdapter adapter;
     public static final int EDIT = 0;
     public static final int ADD = 1;
     private EditText editTitleText, idPageNumText;
     private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
     private static final String TAG = "EditFragmentActivity";
     private OfflineIOHelper offlineHelper = new OfflineIOHelper(EditFragmentActivity.this);
     private String mode, pictureMode;
     private StoryList storyList;
     private Story story;
     private int storyPos, fragPos;
     private Fragment fragment;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_fragment_editor);
 
         //obtain the intent
         Intent editActIntent = getIntent();
         mode = (String) editActIntent.getSerializableExtra("Mode");
         storyPos  = (Integer)editActIntent.getSerializableExtra("StoryPos");
         fragPos = (Integer)editActIntent.getSerializableExtra("FragmentPos"); //not reliable when in view mode
 
         //get widget references
         fragmentPartListView = (ListView) findViewById(R.id.FragmentPartList);
         fragmentTitleTextView = (TextView) findViewById(R.id.fragmentTitleText);
         editTitleText = (EditText) findViewById(R.id.fragmentTitle);
         idPageNumText = (EditText) findViewById(R.id.idPageNum);
         
         setListClickListener();
     }
         
     /** Updates all of the Ui Elements for this Activity */
     @Override
     protected void onStart() {
         super.onStart();
         
         //load save file
         storyList = offlineHelper.loadOfflineStories();
         story = storyList.getAllStories().get(storyPos);
         /*
         if (fragPos == 0 && mode.equals("View")){
         	fragment = story.getFragments().get(fragPos);
         }else if (fragPos != 0 && mode.equals("View")){
         	//load the intent bro that has a fragment
         	Intent editActIntent = getIntent();
         	fragment = (Fragment) editActIntent.getSerializableExtra("nextFragment");
         	//System.out.println(fragment.getTitle());
         }else{
         	fragment = story.getFragments().get(fragPos);
         }
         	*/
         fragment = story.getFragments().get(fragPos);
         //Make sure we have at least one part if in edit mode
         if (mode.equals("Edit") == true && fragment.getDisplayOrder().size()==0){
             FragmentController.addEmptyPart(fragment);
         }
         
         //First load fragment parts as that is the same for both modes
         //Loads fragment parts (text, images, videos, sounds, etc)
         adapter = new FragmentPartAdapter(this, R.layout.activity_fragment_editor, fragment);
         fragmentPartListView.setAdapter(adapter);
      
         if (mode.equals("Edit")){
             //don't want textview
             fragmentTitleTextView.setVisibility(View.GONE);
             
             //want the context menu for list parts
             registerForContextMenu(fragmentPartListView);
             
             //Loads title
             String title = fragment.getTitle();
             if (editTitleText != null){
                 if (title != null)
                     editTitleText.setText(title);
                 else
                     editTitleText.setText("");
             }
             
         } else if (mode.equals("View")){
             //don't want edit text views
             editTitleText.setVisibility(View.GONE);
             idPageNumText.setVisibility(View.GONE);
             
             //Loads title
             String title = fragment.getTitle();
             if (fragmentTitleTextView != null){
                 if (title != null)
                     fragmentTitleTextView.setText(title);
                 else
                     fragmentTitleTextView.setText("Error - No title found");
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         
         //we only want the menu when in edit mode
         if (mode.equals("Edit"))
             getMenuInflater().inflate(R.menu.fragment_editor, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // TODO Auto-generated method stub
         switch (item.getItemId()) {
             case R.id.add_option:
                 return true;
                 
             case R.id.save_fragment:
                 // Save values
                 if (mode.equals("Edit"))
                     saveFragment();
                 
                 // Leave activity
                 finish();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void update(Fragment model) {
         // TODO reload all fields based on new info from model
 
     }
 
     private void setListClickListener(){
         fragmentPartListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
     
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 if (fragment.getDisplayOrder().get(position).equals("c")){
                     if (mode.equals("Edit")){
                         // TODO open edit choice activity
                         
                         
                     } else if (mode.equals("View")){
                         //go to next fragment
                         
                         //get the occurence number of the textSegment
                         int occurence = 0;
                         for (int i = 0; i < position; i++){
                             if (fragment.getDisplayOrder().get(position).equals("c"))
                                 occurence++;  
                         }
                         Fragment goToFrag = fragment.getChoices().get(occurence).getLinkedToFragment();
                         
                         Intent intent = new Intent(EditFragmentActivity.this, EditFragmentActivity.class);
                         
                         for (int i = 0 ; i < storyList.getAllStories().get(storyPos).getFragments().size();i++){
                         	if (storyList.getAllStories().get(storyPos).getFragments().get(i).equals(goToFrag))
                         		fragPos = i;
                         }
                         
                         
                         //Bundle theExtras = new Bundle();
                         intent.putExtra("Mode", "View");
                         intent.putExtra("StoryList", storyList);
                         intent.putExtra("StoryPos",storyPos);
                         intent.putExtra("Story", story);
                         intent.putExtra("FragmentPos",fragPos);
                         intent.putExtra("Fragment", goToFrag);
 
                         //theExtras.putSerializable("nextFragment", goToFrag);
                        // System.out.println(goToFrag.getDisplayOrder().toString());
                         //System.out.println(goToFrag.getTitle());
                         startActivity(intent);
                     }
                 }
             }});
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.fragment_part_menu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         position = (int)info.id;
 
         CharSequence itemTitle = item.getTitle();
         if (itemTitle.equals("Insert Text")){
             FragmentController.addTextSegment(fragment, "New text", position);
 
         } else if (itemTitle.equals("Insert Illustration")){
             System.out.println("insert ill start");
             saveFragment();
             pictureMode = "Add";
             picturePosition = position;
             AddImage();
 
         } else if (itemTitle.equals("Edit")){
             String type = fragment.getDisplayOrder().get(position);
             if (type.equals("t") || type.equals("e")){
                 RelativeLayout curLayout = new RelativeLayout(this);
 
                 LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 final PopupWindow editTextWindow = new PopupWindow(inflater.inflate(R.layout.edit_text_seg_popup, null, true), fragmentPartListView.getWidth(), 400, true);
 
                 final EditText editTextSegView = (EditText) editTextWindow.getContentView().findViewById(R.id.editTextSeg);
                 if (type.equals("t")){
                     int occurrence = 0;
                     for (int i = 0; i < position; i++){
                         if (fragment.getDisplayOrder().get(i).equals("t"))
                             occurrence++;  
                     }
                     editTextSegView.setText(fragment.getTextSegments().get(occurrence));
                 }
                     
 
                 Button editTextSave = (Button) editTextWindow.getContentView().findViewById(R.id.editTextSave);
                 Button editTextCancel = (Button) editTextWindow.getContentView().findViewById(R.id.editTextCancel);
 
                 editTextSave.setOnClickListener(new EditTextSegOnClickListener(position) {
                     @Override
                     public void onClick(View v) {
                         String newText = editTextSegView.getText().toString();
                         FragmentController.deleteFragmentPart(fragment,this.position);
                         FragmentController.addTextSegment(fragment, newText, this.position);
                         fragmentPartListView.invalidateViews();
                         editTextWindow.dismiss();
                     }
                 });
 
                 editTextCancel.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         editTextWindow.dismiss();
                     }
                 });
 
 
                 editTextWindow.showAtLocation(curLayout, Gravity.CENTER, 0, 0); 
                 editTextWindow.update(0,0,fragmentPartListView.getWidth(),400);
 
             } else if (type.equals("i")){
                 saveFragment();
                 pictureMode = "Edit";
                 picturePosition = position;
                 AddImage();
             }
 
         } else if(itemTitle.equals("Add Choice")){ 
             // create, add and save new choice
             int choicePos = fragment.getChoices().size();
             Choice choice = new Choice();
             //fragment.addChoice(choice);
             FragmentController.addChoiceStatic(fragment, choice);
             saveFragment();
             
             //go to edit choice activity
             Intent intent = new Intent(this, EditChoiceActivity.class);
             intent.putExtra("StoryPos",storyPos);
             intent.putExtra("FragmentPos", fragPos);
             intent.putExtra("ChoicePos", choicePos);
             startActivity(intent);
             
         }else if (itemTitle.equals("Delete")){
             FragmentController.deleteFragmentPart(fragment, position);
         }
         //Make sure the fragment isn't completely empty
         if(fragment.getDisplayOrder().size()==0)
             FragmentController.addEmptyPart(fragment);
 
         //reset listview to display any changes
         //adapter.notifyDataSetChanged();
         fragmentPartListView.invalidateViews();
         return true;
     }
 
     public void AddImage() {
         final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
         
         AlertDialog.Builder builder = new AlertDialog.Builder(EditFragmentActivity.this);
         builder.setTitle("Add Photo!");
         builder.setItems(options, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int item) {
                 if (options[item].equals("Take Photo"))
                 {
                     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                     File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                     intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                     startActivityForResult(intent, 1);
                 }
                 else if (options[item].equals("Choose from Gallery"))
                 {
                     Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                     startActivityForResult(intent, 2);
  
                 }
                 else if (options[item].equals("Cancel")) {
                     dialog.dismiss();
                 }
             }
         });
         builder.show();
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK) {
             Bitmap bitmap=null;
             String picturePath=null;
             if (requestCode == 1) {
                 File f = new File(Environment.getExternalStorageDirectory().toString());
                 for (File temp : f.listFiles()) {
                     if (temp.getName().equals("temp.jpg")) {
                         f = temp;
                         break;
                     }
                 }
                 try {
                     BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
  
                     bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                             bitmapOptions); 
                     picturePath = android.os.Environment
                             .getExternalStorageDirectory()
                             + File.separator
                             + "Phoenix" + File.separator + "default";
                     f.delete();
                     OutputStream outFile = null;
                     File file = new File(picturePath, String.valueOf(System.currentTimeMillis()) + ".jpg");
                     try {
                         outFile = new FileOutputStream(file);
                         bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                         outFile.flush();
                         outFile.close();
                     } catch (FileNotFoundException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             } else if (requestCode == 2) {
  
                 Uri selectedImage = data.getData();
                 String[] filePath = { MediaStore.Images.Media.DATA };
                 Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                 c.moveToFirst();
                 int columnIndex = c.getColumnIndex(filePath[0]);
                 picturePath = c.getString(columnIndex);
                 c.close();
                 //bitmap = (BitmapFactory.decodeFile(picturePath));
                 Log.w("path of image from gallery......******************.........", picturePath+"");                
             }
             if(picturePath != null){
                 if (pictureMode.equals("Add")){
                     System.out.println("add ill start");
                     FragmentController.addIllustration(fragment, picturePath, position);
                 }else if (pictureMode.equals("Edit")){
                     FragmentController.deleteFragmentPart(fragment, position);
                     FragmentController.addIllustration(fragment, picturePath, position);
                 }
                 saveFragment();
                 //fragmentPartListView.invalidateViews();
             } else
                 Log.d(TAG,"bitmap returned is null");
         }
     }
     
     private void setTitleAndPageId() {
 
         String title = editTitleText.getText().toString();
         String idPageNum = idPageNumText.getText().toString();
         int idPage = -9;
         try{
             idPage = Integer.parseInt(idPageNum);
         }catch(NumberFormatException e){
             Log.d("Msg","There was a number format exception!");
         }
 
         fragment.setTitle(title);
         fragment.setId(idPage);
     } 
     
     private void saveFragment(){
         //make sure fragment does not have any empty parts
         FragmentController.removeEmptyPart(fragment);
 
         setTitleAndPageId();
         storyList.getAllStories().get(storyPos).getFragments().set(fragPos, fragment);
         offlineHelper.saveOfflineStories(storyList);
     }
 }
