 package com.zoeetrope.lineupcamera;
 
 import android.content.ComponentName;
 import android.content.Intent;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.zoeetrope.lineupcamera.model.Album;
 import com.zoeetrope.lineupcamera.model.Image;
 
 public class ImageListActivity extends SherlockActivity {
 
 	private Album mAlbum;
 	private GridView mGridview;
 	private ImageListAdapter mAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.image_list);
 		mGridview = (GridView) findViewById(R.id.gridview);
 
 		ActionBar bar = getSupportActionBar();
 		Bundle extras = getIntent().getExtras();
 
 		if (extras != null) {
 			mAlbum = new Album(extras.getString("ALBUM"));
 			mAdapter = new ImageListAdapter(this, R.layout.image_list_item,
 					mAlbum);
 
 			bar.setTitle((CharSequence) mAlbum.getName());
 			bar.setSubtitle(mAlbum.getImages().size() + " "
 					+ getResources().getString(R.string.picture_count));
 			bar.setDisplayHomeAsUpEnabled(true);
 			bar.setHomeButtonEnabled(true);
 
 			Image latestImage = mAlbum.getLatestImage();
 
 			if (latestImage != null) {
 				bar.setIcon(new BitmapDrawable(getResources(), latestImage
 						.getThumbnail()));
 			}
 
 			mGridview.setAdapter(mAdapter);
 			mGridview.setOnItemClickListener(new OnItemClickListener() {
 				public void onItemClick(AdapterView<?> parent, View v,
 						int position, long id) {
 					Intent imageIntent = new Intent();
 					imageIntent.setComponent(new ComponentName(
 							ImageListActivity.this, ImageActivity.class));
 
 					Bundle bundle = new Bundle();
 					bundle.putString("ALBUM", mAlbum.getName());
 					bundle.putInt("POSITION", position);
 					imageIntent.putExtras(bundle);
 
 					ImageListActivity.this.startActivity(imageIntent);
 				}
 			});
 
 			registerForContextMenu(mGridview);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		android.view.MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.image_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 
 		if (item.getItemId() == R.id.remove) {
 			mAlbum.remove(info.position);
 			mAdapter.notifyDataSetChanged();
 			mGridview.invalidateViews();
 			return true;
 		} else {
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.action_bar_images, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			Intent intent = new Intent(this, AlbumListActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		} else if (item.getItemId() == R.id.newPicture) {
 			Intent cameraIntent = new Intent();
 			cameraIntent.setComponent(new ComponentName(this,
 					LineUpCameraActivity.class));
 
 			Bundle bundle = new Bundle();
 			bundle.putString("ALBUM", mAlbum.getName());
 			cameraIntent.putExtras(bundle);
 
 			this.startActivity(cameraIntent);
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 }
