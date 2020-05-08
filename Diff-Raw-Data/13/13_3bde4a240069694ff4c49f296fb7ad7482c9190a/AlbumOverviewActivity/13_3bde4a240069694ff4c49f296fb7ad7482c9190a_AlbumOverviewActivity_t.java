 package ch.bergturbenthal.raoa.client.album;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import ch.bergturbenthal.raoa.R;
 import ch.bergturbenthal.raoa.client.binding.AbstractViewHandler;
 import ch.bergturbenthal.raoa.client.binding.ComplexCursorAdapter;
 import ch.bergturbenthal.raoa.client.binding.PhotoViewHandler;
 import ch.bergturbenthal.raoa.client.binding.SetTagViewHandler;
 import ch.bergturbenthal.raoa.client.binding.TextViewHandler;
 import ch.bergturbenthal.raoa.client.binding.ViewHandler;
 import ch.bergturbenthal.raoa.client.photo.PhotoOverviewActivity;
 import ch.bergturbenthal.raoa.provider.Client;
 
 public class AlbumOverviewActivity extends Activity implements LoaderCallbacks<Cursor> {
 
 	private static String TAG = AlbumOverviewActivity.class.getSimpleName();
 	private CursorAdapter cursorAdapter;
 
 	@Override
 	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
 		return new CursorLoader(this, Client.ALBUM_URI, null, null, null, null);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu) {
 		final MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.album_overview_menu, menu);
 		return true;
 	}
 
 	@Override
 	public void onLoaderReset(final Loader<Cursor> loader) {
 		cursorAdapter.swapCursor(null);
 	}
 
 	@Override
 	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
 		cursorAdapter.swapCursor(data);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.createAlbumMenuItem:
 			startActivity(new Intent(this, CreateAlbumActivity.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.album_overview);
 
 		cursorAdapter = ComplexCursorAdapter.registerLoaderManager(getLoaderManager(), this, Client.ALBUM_URI, R.layout.album_overview_item, makeViewHandlers());
 		final GridView gridview = (GridView) findViewById(R.id.album_overview);
 		gridview.setAdapter(cursorAdapter);
 
 		// Handle clicks on album image
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(final AdapterView<?> parent, final View v, final int position, final long id) {
 				final Intent intent = new Intent(AlbumOverviewActivity.this, PhotoOverviewActivity.class);
 				intent.putExtra("album_uri", (String) (v.getTag()));
 				startActivity(intent);
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<ViewHandler<? extends View>> makeViewHandlers() {
 		return Arrays.<ViewHandler<? extends View>> asList(	new PhotoViewHandler(R.id.album_item_image, Client.Album.THUMBNAIL, Client.Album.ALBUM_ENTRIES_URI),
 																												new SetTagViewHandler(R.id.album_overview_grid_item, Client.Album.ALBUM_ENTRIES_URI),
 																												new TextViewHandler(R.id.album_item_name, Client.Album.TITLE),
 																												new TextViewHandler(R.id.album_item_size, Client.Album.ENTRY_COUNT),
 																												new AbstractViewHandler<ImageView>(R.id.album_item_icon_offline) {
 
 																													@Override
 																													public void bindView(final ImageView view, final Context context, final Map<String, Object> values) {
 
 																														final boolean shouldSync = ((Number) values.get(Client.Album.SHOULD_SYNC)).intValue() != 0;
 																														final boolean synced = ((Number) values.get(Client.Album.SYNCED)).intValue() != 0;
 																														if (shouldSync) {
 																															view.setImageResource(R.drawable.ic_icon_offline_online);
 																															if (synced) {
																																view.clearAnimation();
																															} else {
 																																final Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotate_infinitely);
 																																view.startAnimation(animation);
 																															}
 																														} else {
 																															view.clearAnimation();
 																															view.setImageResource(R.drawable.ic_icon_offline_offline);
 																														}
 																														final String entryUri = (String) values.get(Client.Album.ENTRY_URI);
 																														view.setOnClickListener(new OnClickListener() {
 
 																															@Override
 																															public void onClick(final View v) {
 																																final ContentValues values = new ContentValues();
 																																values.put(Client.Album.SHOULD_SYNC, Boolean.valueOf(!shouldSync));
 																																context.getContentResolver().update(Uri.parse(entryUri), values, null, null);
 
 																															}
 																														});
 																													}
 
 																													@Override
 																													public String[] usedFields() {
 																														return new String[] { Client.Album.SHOULD_SYNC, Client.Album.SYNCED, Client.Album.ENTRY_URI };
 																													}
 																												});
 	}
 }
