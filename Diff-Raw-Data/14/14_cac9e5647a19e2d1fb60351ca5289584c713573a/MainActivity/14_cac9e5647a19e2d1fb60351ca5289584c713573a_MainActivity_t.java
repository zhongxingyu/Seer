 package com.svenkapudija.imagewall;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.svenkapudija.imagechooser.AlertDialogImageChooser;
 import com.svenkapudija.imagechooser.ImageChooser;
 import com.svenkapudija.imagechooser.ImageChooserListener;
 import com.svenkapudija.imagechooser.StorageOption;
 import com.svenkapudija.imagechooser.settings.AlertDialogImageChooserSettings;
 import com.svenkapudija.imagewall.adapters.TimelineAdapter;
 import com.svenkapudija.imagewall.api.ImageWallApi.ImagesListener;
 import com.svenkapudija.imagewall.base.ImageWallActivity;
 import com.svenkapudija.imagewall.models.Image;
 import com.svenkapudija.imagewall.models.Location;
 import com.svenkapudija.imagewall.models.Tag;
 
 public class MainActivity extends ImageWallActivity {
 	
 	private static final int CHOOSER_IMAGE_REQUEST_CODE = 1000;
 	private static final int NEW_IMAGE_REQUEST_CODE = 1001;
 	
 	private ImageChooser chooser;
 	private List<Image> listItems = new ArrayList<Image>();
 	private ArrayAdapter<Image> adapter;
 	
 	private TextView header;
 	private ImageButton searchActionBar;
 	
 	private ImageButton newImage;
 	private PullToRefreshListView listView;
 	
 	@Override
 	public void initUI() {
 		newImage = (ImageButton) findViewById(R.id.imageButton_newImage);
 		header = (TextView) findViewById(R.id.textView_header);
 		listView = (PullToRefreshListView) findViewById(R.id.listView);
 		
 		searchActionBar = (ImageButton) findViewById(R.id.imageButton_search);
 		searchActionBar.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				onSearchRequested();
 			}
 		});
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		initImageChooser();
 		
 		Intent intent = getIntent();
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			performSearch(query);
 		} else {
 			initListItemsFromDb();
 		}
 		
 		adapter = new TimelineAdapter(this, listItems);
 		initListView();
 		
 		newImage.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				chooser.show();
 			}
 		});
 		
 		// Fetch new images if necessary
 		if(listItems.size() == 0) {
 			final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.loading_images), true, false);
 			fetchImagesFromNetwork(new FetchImagesListener() {
 				
 				@Override
 				public void onSuccess() {
 					dialog.cancel();
 				}
 				
 				@Override
 				public void onFailure() {
 					dialog.cancel();
 				}
 			});
 		}
 	}
 	
 	private void initListItemsFromDb() {
 		List<Image> images = new ArrayList<Image>();
 		try {
 			images = getHelper().getImagesDao().queryBuilder().orderBy("dateCreated", false).query();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		listItems = images;
 	}
 
 	private void initListView() {
 		listView.setAdapter(adapter);
 		
 		listView.setReleaseLabel(getString(R.string.pulltorefresh_release_label_let_go));
 		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
 			@Override
 			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
 				fetchImagesFromNetwork();
 			}
 		});
 		
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 				position--; // Possible bug due PullToRefresh? [header counts as first row]
 				
 				Intent i = new Intent(MainActivity.this, ImageActivity.class);
 				i.putExtra(ImageActivity.EXTRA_IMAGE, listItems.get(position));
 				startActivity(i);
 			}
 		});
 	}
 
 	private void initImageChooser() {
 		chooser = new AlertDialogImageChooser(this, CHOOSER_IMAGE_REQUEST_CODE, new AlertDialogImageChooserSettings(true));
 		chooser.saveImageTo(StorageOption.SD_CARD_APP_ROOT, "imageToUpload", "myImage");
 	}
 	
 	private void performSearch(String searchText) {
 		header.setText("#" + searchText);
 		
 		final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.searching), true, false);
 		fetchImagesFromNetworkByTag(searchText, new FetchImagesListener() {
 			
 			@Override
 			public void onSuccess() {
 				dialog.cancel();
 			}
 			
 			@Override
 			public void onFailure() {
 				dialog.cancel();
 			}
 		});
 	}
 
 	private void fetchImagesFromNetworkByTag(final String tagValue, final FetchImagesListener ... listeners) {
 		getApi().getImages(new Tag(tagValue), new ImagesListener() {
 			
 			@Override
 			public void onSuccess(Collection<Image> images) {
 				List<Image> imagesList = new ArrayList<Image>(images);
 				Collections.reverse(imagesList);
 				
 				RuntimeExceptionDao<Image, Integer> imagesDao = getHelper().getImagesDao();
 				RuntimeExceptionDao<Tag, Integer> tagsDao = getHelper().getTagsDao();
 				RuntimeExceptionDao<Location, Integer> locationsDao = getHelper().getLocationsDao();
 				
 				listItems.clear();
 				
 				for(Image image : imagesList) {
 					Tag tag = image.getTag();
 					if(tag != null) {
 						tagsDao.createOrUpdate(tag);
 					}
 					
 					Location location = image.getLocation();
 					if(location != null) {
 						locationsDao.createOrUpdate(location);
 					}
 					
 					imagesDao.createOrUpdate(image);
 				}
 				
 				if(images.size() == 0) {
 					showErrorDialog();
 				} else {
 					addToAdapterList(imagesList);
 				}
 				
 				for(FetchImagesListener listener : listeners) {
 					listener.onSuccess();
 				}
 			}
 
 			private void addToAdapterList(List<Image> imagesList) {
 				for(Image image : imagesList) {
					listItems.add(0, image);
 				}
 				adapter.notifyDataSetChanged();
 			}
 			
 			@Override
 			public void onFailure() {
 				for(FetchImagesListener listener : listeners) {
 					listener.onFailure();
 				}
 				
 				showErrorDialog();
 			}
 			
 			private void showErrorDialog() {
 				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 				builder.setMessage(String.format(getString(R.string.there_is_no_images_with_tag), tagValue));
 				builder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						finish();
 					}
 				});
 				
 				builder.create().show();
 			}
 		});
 	}
 	
 	private void fetchImagesFromNetwork(final FetchImagesListener ... listeners) {
 		getApi().getImages(new ImagesListener() {
 			
 			@Override
 			public void onSuccess(Collection<Image> images) {
 				List<Image> imagesListCreated = new ArrayList<Image>();
 				
 				List<Image> imagesList = new ArrayList<Image>(images);
 				Collections.reverse(imagesList);
 				
 				RuntimeExceptionDao<Image, Integer> imagesDao = getHelper().getImagesDao();
 				RuntimeExceptionDao<Tag, Integer> tagsDao = getHelper().getTagsDao();
 				RuntimeExceptionDao<Location, Integer> locationsDao = getHelper().getLocationsDao();
 				
 				for(Image image : imagesList) {
 					Tag tag = image.getTag();
 					if(tag != null) {
 						tagsDao.createOrUpdate(tag);
 					}
 					
 					Location location = image.getLocation();
 					if(location != null) {
 						locationsDao.createOrUpdate(location);
 					}
 					
 					boolean newImage = imagesDao.createOrUpdate(image).isCreated();
 					if(newImage) {
 						imagesListCreated.add(image);
 					}
 				}
 				
 				for(Image image : imagesListCreated) {
					listItems.add(0, image);
 				}
 				
				adapter.notifyDataSetChanged();
 				listView.onRefreshComplete();
 				
 				for(FetchImagesListener listener : listeners) {
 					listener.onSuccess();
 				}
 			}
 			
 			@Override
 			public void onFailure() {
 				listView.onRefreshComplete();
 				
 				for(FetchImagesListener listener : listeners) {
 					listener.onFailure();
 				}
 			}
 		});
 	}
 	
 	private interface FetchImagesListener {
 		public void onSuccess();
 		public void onFailure();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    if (resultCode != Activity.RESULT_OK) {
 	    	return;
 	    }
 	    	
 	    if(requestCode == CHOOSER_IMAGE_REQUEST_CODE) {
 	    	chooser.onActivityResult(data, new ImageChooserListener() {
 
 	    		@Override
 	    		public void onResult(final Bitmap image, final File ... savedImages) {
 	    			image.recycle();
 	 	            
 	    			Intent i = new Intent(MainActivity.this, NewImageActivity.class);
 	    			startActivityForResult(i, NEW_IMAGE_REQUEST_CODE);
 	    		}
 
 	    		@Override
 	    		public void onError(String message) {
 	    			Toast.makeText(MainActivity.this, getString(R.string.couldnt_load_the_image_please_try_again), Toast.LENGTH_LONG).show();
 	    		}
 	    	});
 	    } else if (requestCode == NEW_IMAGE_REQUEST_CODE) {
 	    	final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.loading_images), true, false);
 			fetchImagesFromNetwork(new FetchImagesListener() {
 				
 				@Override
 				public void onSuccess() {
 					dialog.cancel();
 				}
 				
 				@Override
 				public void onFailure() {
 					dialog.cancel();
 				}
 			});
 	    }
 	}
 
 }
