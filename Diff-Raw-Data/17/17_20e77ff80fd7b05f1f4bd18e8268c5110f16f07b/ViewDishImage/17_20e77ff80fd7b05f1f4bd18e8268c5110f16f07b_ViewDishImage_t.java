 package be.uclouvain.sinf1225.gourmet;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import be.uclouvain.sinf1225.gourmet.models.Image;
 import be.uclouvain.sinf1225.gourmet.models.Dish;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 
 /**
  * 
  * @author qeggerickx
  * 
  */
 public class ViewDishImage extends Activity
 {
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
 		setContentView(R.layout.activity_image_dish_view);
		int dishId = Integer.valueOf(getIntent().getExtras().getInt("dishId"));
 		final Dish dish = Dish.getDish(dishId);
		final Image img = Dish.getImage(dishId);
 
 		final Button deleteButton = (Button) findViewById(R.id.delete_image_dish);
 		final Button changeButton = (Button) findViewById(R.id.change_image_dish);
 		final ImageView myImage = (ImageView) findViewById(R.id.dish_image_icon);
 
		File imgFile = new File(img.getPath());
 		if (imgFile.exists())
 		{
 
 			Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
 			myImage.setImageBitmap(myBitmap);
 		}
 		deleteButton.setOnClickListener(new View.OnClickListener()
 		{
 			/**
 			 * perform action on click -> delete image
 			 */
 			public void onClick(View v)
 			{
				File imgFile = new File(img.getPath());
 				if (imgFile.exists())
 				{
 					imgFile.delete(); // delete the file
 				}
				Image.deleteImage(img.getPath()); // delete the image in the database
				finish();
 			}
 		});
 		changeButton.setOnClickListener(new View.OnClickListener()
 		{
 			/**
 			 * perform action on click
 			 */
 			public void onClick(View v)
 			{
 				Bundle args = new Bundle();
 				args.putString("objectType", "dish");
 				args.putString("id", "" + dish.getDishId());
 				Intent intent = new Intent(ViewDishImage.this, ImageGalleryActivity.class);
 				intent.putExtras(args);
 				startActivityForResult(intent, R.layout.activity_gallery_view);
 			}
 		});
 	}
 }
