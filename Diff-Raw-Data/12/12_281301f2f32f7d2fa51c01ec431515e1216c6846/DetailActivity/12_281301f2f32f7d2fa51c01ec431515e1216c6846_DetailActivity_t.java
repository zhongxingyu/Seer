 package de.hsbremen.android.dribl;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.webimageloader.ImageLoader;
 import com.webimageloader.ext.ImageHelper;
 
 import de.hsbremen.android.dribl.adapter.IconTextArrayAdapter;
 import de.hsbremen.android.dribl.provider.DribbbleContract;
 
 public class DetailActivity extends Activity {
 
 	public static final String EXTRA_BASE_URI = "content_uri";
 	public static final String EXTRA_ID = "id";
 
 	private Uri mContentUri;
 	private Uri mCollectionUri;
 	
 	// Outlets
 	private ListAdapter mListAdapter;
 	private ActionBar mActionBar;
 	private ImageLoader mImageLoader;
 	private ImageView mImageView;
 	private TextView mTitleText;
 	private TextView mAuthorText;
 	private MenuItem mAddToCollectionItem;
 	private MenuItem mRemoveFromCollectionItem;
 	private ListView mDetailList;
 	
 	// Data
 	private Shot mShot;
 	private class Shot {
 		long id;
 		String url;
 		String imageUrl;
 		String title;
 		String author;
 		int likesCount;
 		int reboundsCount;
 		int commentsCount;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_detail);
 		
 		mActionBar = getActionBar();
 		mImageLoader = ((DriblApplication) getApplicationContext()).getImageLoader();
 		mImageView = (ImageView) findViewById(R.id.image);
 		mTitleText = (TextView) findViewById(R.id.title);
 		mAuthorText = (TextView) findViewById(R.id.author);
 		mDetailList = (ListView) findViewById(R.id.detailList);
 		
 		// Setup action bar
 		mActionBar.setDisplayHomeAsUpEnabled(true);
 		
 		// Get stat resources
 		String[] texts = getResources().getStringArray(R.array.detail_list);
 		int[] icons = {
 				R.drawable.icon_likes,
 				R.drawable.icon_buckets,
 				R.drawable.icon_views
 		};
 		
 		// Get intent data
 		Intent intent = getIntent();
 		Uri baseUri = intent.getParcelableExtra(EXTRA_BASE_URI);
 		long id = intent.getLongExtra(EXTRA_ID, -1);
 		if (baseUri == null || id == -1) {
 			throw new IllegalArgumentException("Missing arguments");
 		}
 		
 		// Build the content URIs
 		mContentUri = ContentUris.withAppendedId(baseUri, id);
 		mCollectionUri = ContentUris.withAppendedId(DribbbleContract.Image.COLLECTION_URI, id);
 		
 		// Get a cursor that's position is set to the current image
 		// TODO: This should be done in an AsyncTask
 		Cursor cursor = getContentResolver().query(mContentUri,
 				null,  // No projection
 				null,  // No selection
 				null,  // No selectionArgs
 				null   // No sortOrder
 			);
 		if (cursor != null) {
 			mShot = new Shot();
 			
 			// Get the data from the cursor
 			mShot.id = cursor.getLong(cursor.getColumnIndex(DribbbleContract.Image._ID));
 			mShot.url = cursor.getString(cursor.getColumnIndex(DribbbleContract.Image.URL));
 			mShot.imageUrl = cursor.getString(cursor.getColumnIndex(DribbbleContract.Image.IMAGE_URL));
 			mShot.title = cursor.getString(cursor.getColumnIndex(DribbbleContract.Image.TITLE));
 			mShot.author = cursor.getString(cursor.getColumnIndex(DribbbleContract.Image.AUTHOR));
 			mShot.likesCount = cursor.getInt(cursor.getColumnIndex(DribbbleContract.Image.LIKES_COUNT));
 			mShot.reboundsCount = cursor.getInt(cursor.getColumnIndex(DribbbleContract.Image.REBOUNDS_COUNT));
 			mShot.commentsCount = cursor.getInt(cursor.getColumnIndex(DribbbleContract.Image.COMMENTS_COUNT));
 			
 			// Load image
 			new ImageHelper(this, mImageLoader)
 				.setLoadingResource(R.drawable.placeholder)
 				.setFadeIn(true)
 				.load(mImageView, mShot.imageUrl);
 			
 			// Set text
 			mActionBar.setTitle(mShot.title);
 			mTitleText.setText(mShot.title);
 			mAuthorText.setText(mShot.author);
 			
 			// Prepare stats
 			texts[0] = mShot.likesCount + " " + texts[0];
			texts[1] = mShot.reboundsCount + " " + texts[1];
			texts[2] = mShot.commentsCount + " " + texts[2];
 			
 			// Create the ListAdapter
 			mListAdapter = new IconTextArrayAdapter(this, icons, texts, R.layout.row_icontext) {
 				@Override
 				public boolean isEnabled(int position) {
 					// Make all items in this list non-clickable
 					return false;
 				}
 			};
 			
 			// Show the stats
 			mDetailList.setAdapter(mListAdapter);
 		}
 		
 
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.detail, menu);
 		
 		// Fill outlets
 		mAddToCollectionItem = menu.findItem(R.id.action_add_to_collection);
 		mRemoveFromCollectionItem = menu.findItem(R.id.action_remove_from_collection);
 		
 		// Show remove button instead, if the image is in the user's collection
 		if (mShot != null && isInCollection()) {
 			updateButtons(true);
 		}
 		
 		return true;
 	}
 	
 	private void updateButtons(boolean isInCollection) {
 		if (isInCollection) {
 			// Show remove button
 			mAddToCollectionItem.setVisible(false);
 			mRemoveFromCollectionItem.setVisible(true);
 		} else {
 			// Show add button
 			mAddToCollectionItem.setVisible(true);
 			mRemoveFromCollectionItem.setVisible(false);
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			// This call is only needed for compatibility with older API levels
 			case android.R.id.home:
 				NavUtils.navigateUpFromSameTask(this);
 				return true;
 			
 			case R.id.action_add_to_collection:
 				// Add the current image to the collection
 		        ContentValues values = new ContentValues();   
 		        values.put(DribbbleContract.Image._ID, mShot.id);
 		        values.put(DribbbleContract.Image.URL, mShot.url);
 		        values.put(DribbbleContract.Image.IMAGE_URL, mShot.imageUrl);
 		        values.put(DribbbleContract.Image.TITLE, mShot.title);
 		        values.put(DribbbleContract.Image.AUTHOR, mShot.author);
 		        values.put(DribbbleContract.Image.LIKES_COUNT, mShot.likesCount);
 		        values.put(DribbbleContract.Image.REBOUNDS_COUNT, mShot.reboundsCount);
 		        values.put(DribbbleContract.Image.COMMENTS_COUNT, mShot.commentsCount);
 		        
 		        // TODO: This should be done in an AsyncTask
 				getContentResolver().insert(mCollectionUri, values);
 				
 				// Update the UI
 				updateButtons(true);
 				
 				// Tell the user what happened
 				Toast.makeText(
 						getApplicationContext(),
 						getResources().getString(R.string.toast_collection_added),
 						Toast.LENGTH_SHORT
 					).show();
 				
 				return true;
 			
 			case R.id.action_remove_from_collection:
 		        // TODO: This should be done in an AsyncTask
 				getContentResolver().delete(mCollectionUri,
 						null, // No where clause, because the ID is already specified as part of the URI
 						null  // No selectionArgs
 					);
 				
 				// Update the UI
 				updateButtons(false);
 				
 				// Tell the user what happened
 				Toast.makeText(
 						getApplicationContext(),
 						getResources().getString(R.string.toast_collection_removed),
 						Toast.LENGTH_SHORT
 					).show();				
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Tests if the current image is in the user's collection
 	 * 
 	 * @return boolean
 	 */
 	private boolean isInCollection() {
 		// Checks for the existance of the image. This behavior of the ContentProvider
 		// is documented in the DribbbleContentProvider.query() method
 		// TODO: This should be done in an AsyncTask
 		Cursor cursor = getContentResolver().query(mCollectionUri,
 				new String[] { "1" },
 				null,  // No selection
 				null,  // No selectionArgs
 				null   // No sortOrder
 			);
 		boolean exists = (cursor.getCount() > 0);
 		cursor.close();
 		
 		return exists;
 	}
 	
 }
