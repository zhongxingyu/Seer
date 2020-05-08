 package fr.youchuzz;
 
 import java.io.File;
 
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 
 import fr.youchuzz.core.API;
 
 /**
  * Create a new chuzz.
  * 
  * Ask the user to choose a title and to pick two contents from his phone.
  * 
  * Typical workflow :
  * - onCreate, then you click on a content picker :
  * - onPickContent, then you choose content type from the dialog leading you to
  * - onContextItemSelected, then you pick content using whatever method you asked, returning to
  * - onActivityResult, which will start upload and then call
  * - onContentUploaded. Once you've done that twice, you may call
  * - onSave, which will display friend list before saving.
  * @author neamar
  *
  */
 public class CreateActivity extends BaseActivity {
 	
 	private int CONTENT_1 = 1;
 	private int CONTENT_2 = 2;
 	
 	private int PICKING = 0;
 	private int UPLOADING = 1;
 	private int PREVIEWING = 2;
 	
 	private int REQUEST_IMAGE = 100;
 	private int REQUEST_PHOTO = 101;
 	
 	private int currentlyPicking = 0;
 	
 	private Intent nextActivityIntent;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_create);
 		
 		nextActivityIntent = new Intent(this, FriendsActivity.class);
 		
 		aq = new AQuery(this);
 		
 		//Initialize ui, allow user to pick content
 		updateUi(CONTENT_1, PICKING);
 		updateUi(CONTENT_2, PICKING);
 		
 		aq.id(R.id.create_add_1).clicked(this, "onPickContent");
 		aq.id(R.id.create_add_2).clicked(this, "onPickContent");
 		aq.id(R.id.create_save).clicked(this, "onSave");
 		
 		
 		registerForContextMenu(aq.id(R.id.create_add_1).getView());
 		registerForContextMenu(aq.id(R.id.create_add_2).getView());
 	}
 	
 	/**
 	 * Called when user press pick button
 	 * @param v
 	 */
 	public void onPickContent(View v)
 	{
 		if(v.getId() == R.id.create_add_1)
 			currentlyPicking = CONTENT_1;
 		else if(v.getId() == R.id.create_add_2)
 			currentlyPicking = CONTENT_2;
 		
 		v.showContextMenu();
 	}
 	
 	/**
 	 * Display "select pick menu"
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_create_source_picker, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_create_source_pick_gallery:
 				//Pick from gallery (or other file browser)
 				Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
 				galleryIntent.setType("image/*");
 				startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.create_pick_image)), REQUEST_IMAGE);
 
 				return true;
 			case R.id.menu_create_source_pick_photo:
 				//Pick by taking new picture
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 		}
 	}
 	
 
 	/**
 	 * Called when user has picked some content from some source
 	 */
 	public void onActivityResult (int requestCode, int resultCode, Intent data)
 	{
 		Log.i("yc", "Getting back result for request " + Integer.toString(requestCode));
 		if (resultCode != RESULT_OK)
 		{
 			error("Unable to retrieve file :(");
 		}
 		else
 		{
 			File content = null;
 			Uri selectedUri = null;
 			if (requestCode == REQUEST_IMAGE) {
 				//GET IMAGE FROM GALLERY / FILE BROWSER
 				selectedUri = data.getData();
 				content = new File(getPath(selectedUri));
 			}
 			
 			if(content == null)
 			{
 				error("Unable to load this content. Please use another source.");
 			}
 			{
 				API.getInstance().uploadContent(this, "onContentUploaded", content);
 				
 				updateUi(currentlyPicking, UPLOADING);
 			}
 		}
 	}
 	
 	/**
 	 * Called when content has finished uploading
 	 */
 	public void onContentUploaded(String url, JSONObject json, AjaxStatus status)
 	{
 		//Check for errors
 		if(json == null)
 		{
 			error("Error while sending content : err. " + status.getCode());
 			updateUi(currentlyPicking, PICKING);
 		}
 		else
 		{
 			Log.i("yc", "Content saved, content_id=" + getString(json, "id_content"));
 			updateUi(currentlyPicking, PREVIEWING);
 			int id = 0;
 			if(currentlyPicking == CONTENT_1)
 			{
 				id = R.id.create_image_1;
 				nextActivityIntent.putExtra("content1", getString(json, "id_content"));
 			}
 			else
 			{
 				id = R.id.create_image_2;
 				nextActivityIntent.putExtra("content2", getString(json, "id_content"));
 			}
 			
 			aq.id(id).image(getString(json, "content_preview"), true, true, 0, R.drawable.ic_menu_attachment);
 			
 			
 		}
 	}
 	
 	public void onSave(View v)
 	{
 		String title = aq.id(R.id.create_title).getText().toString();
 		if(title.equals(""))
 		{
 			error("You need to add a title for your chuzz.");
 			aq.id(R.id.create_title).getView().requestFocus();
 		}
 		else if(!nextActivityIntent.hasExtra("content1") || !nextActivityIntent.hasExtra("content2"))
 		{
 			error("You need to select two contents before saving this chuzz.");
 		}
 		else
 		{
 			Log.i("yc", "Creating chuzz « " + title + " » with contents {" + nextActivityIntent.getStringExtra("content1") + "," + nextActivityIntent.getStringExtra("content2") + "}");
 			nextActivityIntent.putExtra("title", title);
 			startActivity(nextActivityIntent);
 		}
 	}
 	/**
 	 * Update the UI to match current state
 	 * @param step
 	 */
 	private void updateUi(int content, int step)
 	{
 		int[] ids = null;
 		
 		if(content == 1)
 		{
 			ids = new int[]{R.id.create_add_1, R.id.create_uploading_1, R.id.create_image_1};
 		}
 		else if(content == 2)
 		{
 			ids = new int[]{R.id.create_add_2, R.id.create_uploading_2, R.id.create_image_2};
 		}
 		
 		//Hide all
 		for(int i = 0; i < ids.length; i++)
 		{
 			aq.id(ids[i]).gone();
 		}
 		
 		//Display current ui-item
 		aq.id(ids[step]).visible();
 	}
 	
 	/**
 	 * Get absolute path from Uri
 	 * @param uri
 	 * @return path
 	 */
 	private String getPath(Uri uri)
 	{
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		startManagingCursor(cursor);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 }
