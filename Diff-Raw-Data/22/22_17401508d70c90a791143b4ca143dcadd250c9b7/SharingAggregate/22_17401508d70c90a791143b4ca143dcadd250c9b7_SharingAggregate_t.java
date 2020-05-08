 // Copyright 2009 Shun'ichi Shinohara
 
 // This file is part of ShotQuote.
 //
 // ShotQuote is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // ShotQuote is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with ShotQuote.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.tumblr.shino.shotquote;
 
 import java.io.FileNotFoundException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.tumblr.shino.shotquote.tumblr.TumblrImagePost;
 
 /**
  * Aggregate information to post a quote.
  * @author shino
  */
 public class SharingAggregate extends Activity {
 	private static final int INTENT_SELECT_BOOK = 1;
 	private static final int INTENT_PREFERENCES = 2;	
 	private static final int INTENT_SELECT_IMAGE = 3;
 	private static final int INTENT_VIEW_IMAGE = 4;
 	private static final int INTENT_EDIT_IMAGE = 5;
 
 	private ImageView imageView;
 	private Uri imageUri;
 	private Book selectedBook;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 
 		setTitle(R.string.sharing_aggregate_title);
 		setContentView(R.layout.sharing_aggregate);
 		
 		imageView = (ImageView) findViewById(R.id.captured_image_view);
 		Intent intent = getIntent();
 		Bundle bundle = intent.getExtras();
 		// The URI here is something like "content://media/external/images/media/114"
 		if(bundle != null) {
 			imageUri = (Uri) bundle.get(Intent.EXTRA_STREAM);
 			U.debugLog(this, "URI from the intent", imageUri);
 			imageView.setImageURI(imageUri);
 		} else {
 			imageView.setImageResource(R.drawable.no_image);
 		}
 		
 		Button selectImage = (Button) findViewById(R.id.select_image_button);
 		selectImage.setFocusable(true);
 		selectImage.setFocusableInTouchMode(true);
 		selectImage.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent selectImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
 				selectImageIntent.setType("image/*");
				try {
					startActivityForResult(selectImageIntent, INTENT_SELECT_IMAGE);
				} catch (ActivityNotFoundException notfound) {
					U.showDialog(SharingAggregate.this, "ERROR", "No suitable application to select an image.");
				}
 			}
 		});
 		
 		Button editImage = (Button) findViewById(R.id.edit_image_button);
 		editImage.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(imageUri == null){
 					U.showToast(SharingAggregate.this, R.string.message_no_image);
 					return;
 				}
 				
 				Intent selectImageIntent = new Intent("com.android.camera.action.CROP");
 				selectImageIntent.putExtra("aspectX", 1);
 				selectImageIntent.setData(imageUri);
				try {
					startActivityForResult(selectImageIntent, INTENT_EDIT_IMAGE);
				} catch (ActivityNotFoundException notfound) {
					U.showDialog(SharingAggregate.this, "ERROR", "No suitable application to crop an image.");
				}
 			}
 		});
 
 		Button viewImage = (Button) findViewById(R.id.view_image_button);
 		viewImage.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(imageUri == null){
 					U.showToast(SharingAggregate.this, R.string.message_no_image);
 					return;
 				}
 				
 				Intent selectImageIntent = new Intent(Intent.ACTION_VIEW);
 				selectImageIntent.setData(imageUri);
				try {
					startActivityForResult(selectImageIntent, INTENT_VIEW_IMAGE);
				} catch (ActivityNotFoundException notfound) {
					U.showDialog(SharingAggregate.this, "ERROR", "No suitable application to view an image.");
				}
 			}
 		});
 
 		Button selectLocation = (Button) findViewById(R.id.select_location_button);
 		selectLocation.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Context context = SharingAggregate.this;
 				AlertDialog.Builder builder = new AlertDialog.Builder(context);
 				builder.setTitle(R.string.location_in_page_text);
 				final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
 						R.array.locations_in_page, R.layout.list_item);
 				final ListView locationsListView = new ListView(SharingAggregate.this);
 				locationsListView.setAdapter(adapter);
 				builder.setView(locationsListView);
 				final AlertDialog dialog = builder.create();
 				locationsListView.setOnItemClickListener(new OnItemClickListener() {
 					public void onItemClick(AdapterView<?> parentView, View view,
 							int position, long id) {
 						CharSequence item = adapter.getItem(position);
 						SharingAggregate.this.setEditTextContent(R.id.location_in_page_edit_text, item);
 						dialog.dismiss();
 					}
 					
 				});
 				dialog.show();
 			}
 		});
 
 		Button selectBook = (Button) findViewById(R.id.select_book_button);
 		selectBook.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent selectBookIntent = new Intent(SharingAggregate.this, Bookshelf.class);
 				startActivityForResult(selectBookIntent, INTENT_SELECT_BOOK);
 			}
 		});
 		
 	}
 
 	private String getEditTextContent(int viewId){
 		EditText editText = (EditText) SharingAggregate.this.findViewById(viewId);
 		return editText.getText().toString();
 	}
 	
 	private void setEditTextContent(int viewId, CharSequence text){
 		EditText editText = (EditText) SharingAggregate.this.findViewById(viewId);
 		editText.setText(text);
 	}
 	
 	public class PostingImageTask extends AsyncTask<String, Integer, TumblrImagePost[]>{
 
 		private SharingAggregate activity;
 		private ProgressDialog progressDialog;
 		
 		public PostingImageTask(SharingAggregate activity) {
 			this.activity = activity;
 		}
 
 		@Override
 		protected void onPreExecute() {
 		    progressDialog = new ProgressDialog(SharingAggregate.this);
 		    progressDialog.setTitle(R.string.message_processing);
 		    progressDialog.setMessage(getString(R.string.message_posting));
 		    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		    progressDialog.setIndeterminate(false);
 		    progressDialog.setMax(100);
 		    progressDialog.show();
 		    this.publishProgressAndSecondary(10, 10);
 		}
 
 		@Override
 		protected TumblrImagePost[] doInBackground(String... args) {
 			U.debugLog(this, "ShotQuote button was pressed.", "Begin to post to Tumblr.");
 
 			try {
 		    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
 		    	TumblrImagePost post1 = createPost(preferences, "tumblr1");
 		    	TumblrImagePost post2 = createPost(preferences, "tumblr2");
 		    	if(post1 != null) {
 					postImage(post1, preferences, args);
 					if(post2 != null) {
 					    this.publishProgressAndSecondary(50, 50);
 					}
 		    	}
 		    	if(post2 != null) {
 					postImage(post2, preferences, args);		    		
 		    	}
 			    this.publishProgressAndSecondary(90, 90);
 				return new TumblrImagePost[]{post1, post2};
 			} catch (Exception e) {
 				U.errorLog(this, "Error in posting to www.tumblr.com", e);
 			}
         	return null;
 		}
 
 		private String postImage(TumblrImagePost post,
 				SharedPreferences preferences, String... args)
 				throws FileNotFoundException, Exception {
 			try {
 				post.setTitle(args[0]);
 				post.setCaption(args[1]);
 				post.setFileUri(imageUri.toString());
 				String mimeType = getContentResolver().getType(imageUri);
 				post.setMimeType(mimeType);
 				String generator = preferences.getString("post_generator", "ShotQuote Android");
 				post.setGenerator(generator);
 				post.setFileInputStream(getContentResolver().openInputStream(imageUri));
 
 				String postId = post.post();
 				return postId;
 			} catch (Exception e) {
 				U.errorLog(this, "postImage", e);
 				post.setError(e);
 				return null;
 			}
 		}
 
 		private TumblrImagePost createPost(SharedPreferences preferences, String accountPrefix) {
 	    	boolean enabled = preferences.getBoolean(accountPrefix + "_enabled", false);
 	    	if(!enabled) return null;
 	    	String email = preferences.getString(accountPrefix + "_email", "");
 	    	String password = preferences.getString(accountPrefix + "_password", "");
 	    	String tumblrHost = preferences.getString(accountPrefix + "_host", "www.tumblr.com");
 			TumblrImagePost post = new TumblrImagePost(email, password, tumblrHost);
 			return post;
 		}
 
 		@Override
 		protected void onPostExecute(TumblrImagePost[] posts) {
 		    progressDialog.dismiss();
 
 		    for (int i = 0; i < posts.length; i++) {
 				TumblrImagePost post = posts[i];
 				if(post == null) continue;
 			    if (post.getError() == null) {
 			    	U.showToast(activity, getString(R.string.message_successfully_posted) + post.getPostId());
 			    } else {
 			    	U.showDialog(activity, "ERROR occuered", "Confirm username/password or network connection.");
 			    	U.errorLog(this, "Error occuered in posting to Tumblr.", post.getError());
 			    }
 				
 			}
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progresses) {
 			progressDialog.setProgress(progresses[0]);
 			progressDialog.setSecondaryProgress(progresses[1]);
 		}
 		
 		public void publishProgressAndSecondary(Integer progress, Integer secondary){
 			super.publishProgress(progress, secondary);
 		}
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, final Intent result) {
 	    if (requestCode == INTENT_SELECT_BOOK) {
 	        if (resultCode == RESULT_OK) {
 	            U.debugLog(this, "Result of select book", result);
 	            selectedBook = result.getParcelableExtra(Bookshelf.DATA_BOOK_INFORMATION);
 	            setBookInformation();
 	        } else if (resultCode == RESULT_CANCELED) {
 	            U.debugLog(this, "Barcode scan is canceled.", Integer.toString(resultCode));
 	        } else {
 	        	U.debugLog(this, "Barcode scan result.", result.toString());
 	        }
 	    } else if (requestCode == INTENT_PREFERENCES) {
 	    	// no operations
 	    } else if (requestCode == INTENT_SELECT_IMAGE) {
 	        if (resultCode == RESULT_OK) {
 		    	U.debugLog(this, "Result of select image request", result);
 	            imageUri = result.getData();
 	            U.debugLog(this, "Selected image URI", imageUri);
 	            imageView.setImageURI(imageUri);
 	        } else if (resultCode == RESULT_CANCELED) {
 	            U.debugLog(this, "Select image request is canceled.", Integer.toString(resultCode));
 	        } else {
 	        	U.debugLog(this, "Select image requst returned with result code", result.toString());
 	        }
 	    } else if (requestCode == INTENT_EDIT_IMAGE) {
 	        if (resultCode == RESULT_OK) {
 		    	U.debugLog(this, "Result of edit image request", result);
             	U.debugLog(this, "action", result.getAction());
 	            imageUri = Uri.parse(result.getAction());
 	            U.debugLog(this, "New image uri after edit", imageUri);
 	            imageView.setImageURI(imageUri);
 	        } else if (resultCode == RESULT_CANCELED) {
 	            U.debugLog(this, "Edit image request is canceled.", Integer.toString(resultCode));
 	        } else {
 	        	U.debugLog(this, "Edit image requst returned with result code", result.toString());
 	        }
 	    	U.debugLog(this, "ResultCode of edit image request", resultCode);
 	    } else if (requestCode == INTENT_VIEW_IMAGE) {
 	    	U.debugLog(this, "ResultCode of view image request", resultCode);
 	    	U.debugLog(this, "Result of view image request", result);
 	    } else {
 	    	U.showDialog(this, "Unknow activity request code", Integer.toString(requestCode));
 	    }
 	}
 
 	private void setBookInformation() {
 		U.debugLog(this, "Selected book", selectedBook);
 		if(selectedBook == null) return;
 
 		TextView titleTextView = (TextView) findViewById(R.id.book_item_title);
 		titleTextView.setText(selectedBook.getTitle());
 		TextView authersTextView = (TextView) findViewById(R.id.book_item_authors);
 		authersTextView.setText(selectedBook.getAuthors());
 		TextView isbnTextView = (TextView) findViewById(R.id.book_item_isbn);
 		isbnTextView.setText(selectedBook.getIsbn());
 	}
 
 //	private static final int MENU_ITEM_NEW = 0;
 	private static final int MENU_ITEM_PREFERENCES = 1;
 	private static final int MENU_ITEM_BOOKSHELF = 2;
 	private static final int MENU_ITEM_POST_QUOTE = 3;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuItem postMenu = menu.add(0, MENU_ITEM_POST_QUOTE, 0, R.string.post_quote);
 		postMenu.setIcon(android.R.drawable.ic_menu_upload);
 		MenuItem prefMenu = menu.add(0, MENU_ITEM_PREFERENCES, 0, R.string.label_preferences);
 		prefMenu.setIcon(android.R.drawable.ic_menu_preferences);
 		MenuItem bookshelfMenu = menu.add(0, MENU_ITEM_BOOKSHELF, 0, R.string.label_bookshelf);
 		bookshelfMenu.setIcon(android.R.drawable.ic_menu_agenda);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()) {
 		case MENU_ITEM_POST_QUOTE:
 			postQuote();
 			return true;
 		case MENU_ITEM_PREFERENCES:
 			Intent preferencesIntent = new Intent(this, SqPreferenceActivity.class);
 			startActivityForResult(preferencesIntent, INTENT_PREFERENCES);
 			return true;
 		case MENU_ITEM_BOOKSHELF:
 			Intent bookshelfIntent = new Intent(this, Bookshelf.class);
 			startActivityForResult(bookshelfIntent, INTENT_SELECT_BOOK);
 			return true;
 		default:
 			U.showDialog(this, "Menu button " + item.getTitle() + " is pressed.",
 					"Not yet implemented");
 			return true;
 		}
 	}
 
 	private void postQuote(){
 		U.debugLog(this, "ShotQuote button was pressed.", "Begin to post to Tumblr.");
 		if(SharingAggregate.this.imageUri == null) {
 			U.showDialog(this, "Image not selected", "Select image by \"pick\" button");
 			return;
 		}
 		PostingImageTask postingImageTask = new PostingImageTask(SharingAggregate.this);
 		SharingAggregate activity = SharingAggregate.this;
 		Book book = activity.selectedBook;
 		String title = "";
 		StringBuilder caption = new StringBuilder();
 
 		if(book != null) {
 			title = book.getTitle();
 			String authors = book.getAuthors();
 			String url = book.getUrl();
 
 			caption.append("<a href=").append(url).append(">").append(title).append("</a>").append("<br />");
 			caption.append("by ").append(authors);
 			String pageNo = activity.getEditTextContent(R.id.page_no_edit_text);
 			if(pageNo !=null && pageNo.length() > 0){
 				caption.append(",<br />");
 				caption.append("at page ").append(pageNo);
 			}
 			String locationInPage = activity.getEditTextContent(R.id.location_in_page_edit_text);
 			if(locationInPage !=null && locationInPage.length() > 0){
 				caption.append(", ").append(locationInPage);
 			}
 			caption.append(".<br />");
 		}
 		postingImageTask.execute(new String[]{title, caption.toString()});
 	}
 	
 	protected void onRestoreInstanceState(Bundle savedState) {
 		imageUri = savedState.getParcelable("imageUri");
 		if(imageUri != null) imageView.setImageURI(imageUri);
 		setEditTextContent(R.id.page_no_edit_text, savedState.getCharSequence("pageNo"));
 		setEditTextContent(R.id.location_in_page_edit_text, savedState.getCharSequence("locationInPage"));
 		selectedBook = savedState.getParcelable("selectedBook");
 		setBookInformation();
 	};
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putParcelable("imageUri", imageUri);
 		outState.putCharSequence("pageNo", getEditTextContent(R.id.page_no_edit_text));
 		outState.putCharSequence("locationInPage", getEditTextContent(R.id.location_in_page_edit_text));
 		outState.putParcelable("selectedBook", selectedBook);
 	}
 }
