 package eecs.berkeley.edu.cs294;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 
 
 public class ToDo_Replica extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		final boolean customTitle = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
 		setContentView(R.layout.main);
 
 		if (customTitle)
 			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
 
 		/*
 		final TextView tv_custom_title = (TextView) findViewById(R.id.tv_custom_title);
 		if (tv_custom_title != null)
 
 		final ImageButton ib_custom_search = (ImageButton) findViewById(R.id.ib_custom_search);
 		if (ib_custom_search != null)
 		 */
		// Create a CalenderService and authenticate
 
 		final ImageButton ib_custom_add = (ImageButton) findViewById(R.id.ib_custom_add);
 		if (ib_custom_add != null) {
 			ib_custom_add.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), Add.class);
 					startActivityForResult(intent, 0);
 				}
 			});
 		}
 
 		/*
 		Button b_maps = (Button) findViewById(R.id.b_maps);
 		b_maps.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), Maps.class);
 				startActivityForResult(intent, 0);
 			}
 		});
 		Button b_todo_lists = (Button) findViewById(R.id.b_todo_lists);
 		b_todo_lists.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), ToDo_Lists.class);
 				startActivityForResult(intent, 1);
 			}
 		});
 		Button b_groups = (Button) findViewById(R.id.b_groups);
 		b_groups.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), Groups.class);
 				startActivityForResult(intent, 2);
 			}
 		});
 		 */
 
 		ImageView iv_background = (ImageView) findViewById(R.id.iv_background);
 		Drawable drawable = LoadImageFromWebOperations("http://i570.photobucket.com/albums/ss142/Vexond/PulseCocoon.jpg");
 		iv_background.setImageDrawable(drawable);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, Menu.FIRST, Menu.FIRST, "Maps");
 		menu.add(0, Menu.FIRST + 1, Menu.FIRST + 1, "ToDo Lists");
 		menu.add(0, Menu.FIRST + 2, Menu.FIRST + 2, "Groups");
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 
 		switch(item.getItemId()){
 		case Menu.FIRST:
 			intent = new Intent(ToDo_Replica.this, Maps.class);
 			startActivityForResult(intent, 1);
 
 			return true;
 		case Menu.FIRST + 1:
 			intent = new Intent(ToDo_Replica.this, ToDo_Lists.class);
 			startActivityForResult(intent, 2);
 
 			return true;
 		case Menu.FIRST + 2:
 			intent = new Intent(ToDo_Replica.this, Groups.class);
 			startActivityForResult(intent, 3);
 			return true;
 		}
 		return false;
 	}
 
 	private Drawable LoadImageFromWebOperations(String url) {
 		try {
 			InputStream is = (InputStream) new URL(url).getContent();
 			Drawable d = Drawable.createFromStream(is, "srcName");
 			return d;
 		}
 		catch (Exception e) {
 			Log.w("debug", "LoadImageFromWebOperations=" + e);
 			return null;
 		}
 	}
}
