 package be.uclouvain.sinf1225.gourmet;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import be.uclouvain.sinf1225.gourmet.models.Image;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 
 /**
  * 
  * @author http://viralpatel.net/blogs/pick-image-from-galary-android-app/
  * 
  */
 public class ImageGalleryActivity extends Activity
 {
 	private static int RESULT_LOAD_IMAGE = 1;
 	private String filePath;
 
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 	    GourmetUtils.createMenu(menu, this, R.id.search);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		return GourmetUtils.onMenuItemSelected(item, this);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gallery_view);
 
 		Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
 		buttonLoadImage.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View arg0)
 			{
 				Bundle args = new Bundle(); // un conteneur pour ses arguments
 				args.putString("objectType",
 						getIntent().getStringExtra("objectType"));// type
 				args.putString("id", getIntent().getStringExtra("id"));// id
 				Intent i = new Intent(
 						Intent.ACTION_PICK,
 						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				i.putExtras(args);
 				startActivityForResult(i, RESULT_LOAD_IMAGE);
 			}
 		});
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
 				&& null != data)
 		{
 			Uri selectedImage = data.getData();
 			String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
 			Cursor cursor = getContentResolver().query(selectedImage,
 					filePathColumn, null, null, null);
 			cursor.moveToFirst();
 
 			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			filePath =cursor.getString(columnIndex);
 			cursor.close();
 
 			final String objectType = getIntent().getStringExtra("objectType");
 			final int objectId = Integer.parseInt(getIntent().getStringExtra( "id"));
 			Image img = new Image(null, filePath, objectType, objectId); // create a new image
			
 			if (objectType == "dish")
 			{
 				AlertDialog.Builder dialog = new AlertDialog.Builder(this);// .create();
 				// TODO Application speaks french, multiple images for dishes
 				dialog.setTitle("Warning");
 				dialog.setMessage("Add this image remove the old one, dish can only have one image");
 				dialog.setIcon(android.R.drawable.ic_dialog_alert);
 				dialog.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int which)
 							{
 								dialog.cancel();
 							}
 						});
 				dialog.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int which)
 							{
 								Image.deleteImage(objectType, objectId); // delete
 							}
 						});
 				dialog.show();
 			}
 			Image.addImage(img); // ajoute l'image dans la DB
 			ImageView imageView = (ImageView) findViewById(R.id.imgViewGalleryLoad);
 			imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
 
 			ImageGalleryActivity.this.finish(); // end the activity
 		}
 	}
 }
