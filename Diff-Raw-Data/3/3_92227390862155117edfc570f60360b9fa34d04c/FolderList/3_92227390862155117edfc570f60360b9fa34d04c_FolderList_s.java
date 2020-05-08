 package com.example.t_gallery;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.provider.MediaStore.Images.Media;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsoluteLayout;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class FolderList extends Activity {
 	private ArrayList<GalleryLayout> mGalleryLayouts = new ArrayList<GalleryLayout>();
 	private ArrayList<String> mAlbumName = new ArrayList<String>();
 	private ArrayList<Integer> mAlbumCount = new ArrayList<Integer>();
 	private int mAlbumIndexArray[];
 	private CacheAndAsyncWork mCacheAndAsyncWork;
 	
 	//Screen Info
 	private int mContentTop = 0;
 	private Config mConfig = null;
 	
 	private void fetchFolderList(){
 		
 		Cursor galleryList = getContentResolver().query(
 				Config.MEDIA_URI, 
 				Config.ALBUM_PROJECTION, 
 				Config.ALBUM_WHERE_CLAUSE, 
 				null, null);
 		
 		mAlbumIndexArray = new int[galleryList.getCount()];
 		for(int i = 0; i<mAlbumIndexArray.length; i++) {
 			mAlbumIndexArray[i] = -1;
 		}
 		
 		int badFolderNum = 0;
 		
 		for (int i=0; i<galleryList.getCount(); i++){
 
 			galleryList.moveToPosition(i);
 			
 			String album = galleryList.getString(galleryList.getColumnIndex(Media.BUCKET_DISPLAY_NAME));
 			
 			Cursor ImageList = getContentResolver().query(
 					Config.MEDIA_URI, 
 					Config.IMAGE_PROJECTION, 
 					Config.IMAGE_WHERE_CLAUSE, 
 					new String[]{galleryList.getString(galleryList.getColumnIndex(Media.BUCKET_ID))}, 
 					null);
 			
 			GalleryLayout galleryLayout = null;
 			
 			int maxWidth = mConfig.currentOrientation == Configuration.ORIENTATION_LANDSCAPE?
 					mConfig.screenHeight : mConfig.screenWidth;
 			int albumWidth = 0;
 			
 			if(0 == album.compareTo("Camera")) {
 				albumWidth = maxWidth - 2* Config.ALBUM_PADDING;
 			}
 			else {
 				albumWidth = (maxWidth - 3* Config.ALBUM_PADDING)/2;
 			}
 			
 			galleryLayout = new GalleryLayout(albumWidth,
 					0,
 					Config.FOLDER_THUMBNAIL_PADDING,
 					Configuration.ORIENTATION_PORTRAIT,
 					Config.COMMON_FOLDER);
 			
 			ImageList.moveToLast();
 
 			while (false == ImageList.isBeforeFirst() && galleryLayout.getLineNum() < 3) {
 				long id = ImageList.getLong(ImageList.getColumnIndex(Media._ID));
 				int width = ImageList.getInt(ImageList.getColumnIndex(Media.WIDTH));
 				int height = ImageList.getInt(ImageList.getColumnIndex(Media.HEIGHT));
 
 				if (width != 0 && height != 0) {
 					galleryLayout.addImage(id, width, height, i);
 				}
 
 				ImageList.moveToPrevious();
 			}
 			galleryLayout.addImageFinish();
 			
 			if(galleryLayout.getLineNum() == 0) {
 				badFolderNum++;
 			}
 			else {
 				if (0 == album.compareTo("Camera")) {
 					mGalleryLayouts.add(0, galleryLayout);
 					mAlbumName.add(0, album);
 					mAlbumCount.add(0, ImageList.getCount());
 
 					storeAlbumIndex(0, i);
 				} else {
 					mGalleryLayouts.add(galleryLayout);
 					mAlbumName.add(album);
 					mAlbumCount.add(ImageList.getCount());
 
 					storeAlbumIndex(i - badFolderNum, i);
 				}
 			}
 		}
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //Screen Info
         mConfig = new Config(this);
         mContentTop = Config.STATUS_BAR_HEIGHT + Config.TITLE_BAR_HEIGHT;
         
 		if (mConfig.currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
 			setContentView(R.layout.horizontal_list_view);
 			HorizontalListView mListView = (HorizontalListView) this.findViewById(R.id.horizontalListView);
 	        mListView.setAdapter(new FolderListAdapter(this));
 		}
 		else {
 	        setContentView(R.layout.vertical_list_view);
 	        ListView mListView = (ListView) this.findViewById(R.id.verticalListView);
 	        mListView.setAdapter(new FolderListAdapter(this));
 		}
 
         fetchFolderList();
         mCacheAndAsyncWork = new CacheAndAsyncWork();
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	Utils.putPrefScrollImageSum(this, 0);
     }
     
     private void storeAlbumIndex(int index, int value) {
     	int end = 0;
     	
 		for(int i = 0; i<mAlbumIndexArray.length; i++) {
 			if(mAlbumIndexArray[i] == -1) {
 				end = i;
 				break;
 			}
 		}
 		
 		if(end == index) {
 			mAlbumIndexArray[index] = value;
 		}
 		else if(index < end) {
 			for(int i = end -1; i >= index; i--) {
 				mAlbumIndexArray[i+1] = mAlbumIndexArray[i];
 			}
 			mAlbumIndexArray[index] = value;
 		}
 		else {
 			//Have not case, maybe add assert
 		}
     }
     
 	class FolderListAdapter extends BaseAdapter {
 		private Context context;
 		private ViewHolder holder;
 
 		public FolderListAdapter(Context context) {
 			super();
 			this.context = context;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			
 			if(mGalleryLayouts.size() == 0) {
 				count = 0;
 			}
 			else if(0 == mAlbumName.get(0).compareTo("Camera")) {
 				count = (mGalleryLayouts.size()/2)+1;
 			}
 			else {
 				count = (mGalleryLayouts.size()+1)/2;
 			}
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			
 			LinearLayout line;
 			holder = null;
 
 			if (convertView == null) {
 				line = new LinearLayout(context);
 			} else {
 				line = (LinearLayout) convertView;
 			}
 
 			if (position == 0) {
 				AbsoluteLayout albumLayout = new AbsoluteLayout(context);
 				
 				int height = 0;
 				if (mConfig.currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
 					
 					height = mConfig.screenHeight - mContentTop - 2* Config.ALBUM_PADDING;
 					line.setPadding(Config.ALBUM_PADDING, Config.ALBUM_PADDING, Config.ALBUM_PADDING / 2, Config.ALBUM_PADDING);
 				}
 				else {
 					ImageLineGroup currentLine = mGalleryLayouts.get(0).lines.get(0);
 					int hPad = (mConfig.screenWidth - currentLine.width)/2;
 					
 					height = Config.CAMARA_ALBUM_HEIGHT;
 					line.setPadding(hPad, Config.ALBUM_PADDING, hPad, Config.ALBUM_PADDING / 2);
 				}
 				
 				setView(line, albumLayout, convertView, 0, 0, height);
 				line.addView(albumLayout);
 			} else {
 				AbsoluteLayout fristAlbum = new AbsoluteLayout(context);
 				int height = 0;
 				
 				if (mConfig.currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
 					line.setPadding(0, Config.ALBUM_PADDING,
 							Config.ALBUM_PADDING / 2, Config.ALBUM_PADDING);
 					height = (mConfig.screenHeight - mContentTop - 3*Config.ALBUM_PADDING)/2;
 				}
 				else {
 					line.setPadding(Config.ALBUM_PADDING, Config.ALBUM_PADDING / 2,
 							Config.ALBUM_PADDING, Config.ALBUM_PADDING / 2);
 					height = Config.COMMON_ALBUM_HEIGHT;
 				}
 				
 				// First album
 				int albumPosition = (position - 1) * 2 + 1;
 				
 				setView(line, fristAlbum, convertView, albumPosition, 0, height);
 				line.addView(fristAlbum);
 
 				// Second album
 				if (albumPosition < (mGalleryLayouts.size() - 1)) {
 					albumPosition += 1;
 					AbsoluteLayout secondAlbum = new AbsoluteLayout(context);
 
 					setView(line, secondAlbum, convertView, albumPosition, 1, height);
 					line.addView(secondAlbum);
 				}
 			}
 			
 			return line;
 		}
 
 		private void setView(LinearLayout parent, AbsoluteLayout line,
 				View convertView, int albumPosition, int locate, int album_max_height) {
 			
 			int shiftWidth = 0;
 			int shiftHeight = 0;
 			
 			initView(parent, line, convertView);
 			
 			if (mConfig.currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
 				shiftWidth = 0;
 				shiftHeight = locate * (mConfig.screenHeight - mContentTop -Config.ALBUM_PADDING)/2;
 			}
 			else {
 				shiftWidth = locate * (mConfig.screenWidth-Config.ALBUM_PADDING)/2;
 				shiftHeight = 0;
 			}
 			
 			fillView(albumPosition, locate, album_max_height, shiftWidth, shiftHeight);
 		}
 		
 		private void initView(LinearLayout parent, AbsoluteLayout line, View convertView) {
 			/* Prepare the view (no content yet) */
 			if (holder == null) {
 				if (convertView == null) {
 					holder = new ViewHolder();
 
 					for (int i = 0; i < Config.THUMBNAILS_PER_LINE * 4; i++) {
 						// Image
 						holder.icons[i] = new GestureImageView(context, 1);
 						holder.icons[i].setBackgroundColor(0xFF000000);
 						holder.icons[i].setVisibility(View.GONE);
 						holder.icons[i].setPadding(Config.FOLDER_THUMBNAIL_PADDING,
 								Config.FOLDER_THUMBNAIL_PADDING,
 								Config.FOLDER_THUMBNAIL_PADDING,
 								Config.FOLDER_THUMBNAIL_PADDING);
 
 						line.addView(holder.icons[i]);
 					}
 
 					// Album Description
 					for (int i = 0; i < 2; i++) {
 						holder.albumDescBg[i] = new ImageView(context);
 						holder.albumDescBg[i].setBackgroundColor(0xFF000000);
 						holder.albumDescBg[i].setVisibility(View.GONE);
 						holder.albumDescBg[i].setAlpha(0.6f);
 						line.addView(holder.albumDescBg[i]);
 						
 						holder.albumDescName[i] = new TextView(context);
 						holder.albumDescName[i].setTextColor(Color.WHITE);
 						holder.albumDescName[i].setTextSize(16);
 						holder.albumDescName[i].setVisibility(View.GONE);
 						holder.albumDescName[i].setAlpha(0.6f);
 						holder.albumDescName[i].setSingleLine(true);
 						holder.albumDescName[i].setEllipsize(TextUtils.TruncateAt.MARQUEE);
 						holder.albumDescName[i].setMarqueeRepeatLimit(-1);
 						holder.albumDescName[i].setHorizontallyScrolling(true); 
 						holder.albumDescName[i].setSelected(true);
 						line.addView(holder.albumDescName[i]);
 						
 						holder.albumDescCount[i] = new TextView(context);
 						holder.albumDescCount[i].setTextColor(Color.WHITE);
 						holder.albumDescCount[i].setTextSize(14);
 						holder.albumDescCount[i].setVisibility(View.GONE);
 						holder.albumDescCount[i].setAlpha(0.6f);
 						line.addView(holder.albumDescCount[i]);
 					}
 
 					parent.setTag(R.id.data_holder, holder);
 				} else {
 					holder = (ViewHolder) (parent.getTag(R.id.data_holder));
 
 					for (int i = 0; i < Config.THUMBNAILS_PER_LINE * 4; i++) {
 						if (holder.task[i] != null) {
 							holder.task[i].cancel(true);
 							holder.task[i] = null;
 						}
 						holder.icons[i].setVisibility(View.GONE);
 					}
 
 					for (int i = 0; i < 2; i++) {
 						holder.albumDescBg[i].setVisibility(View.GONE);
 						holder.albumDescName[i].setVisibility(View.GONE);
 						holder.albumDescCount[i].setVisibility(View.GONE);
 					}
 				}
 			}
 		}
 		
 		private void fillView(int albumPosition, int locate,
 				int album_max_height, int shiftWidth, int shiftHeight) {
 
 			int shiftIconIndex = 2*locate * Config.THUMBNAILS_PER_LINE;
 			
 			ImageLineGroup firstLine = mGalleryLayouts.get(albumPosition).lines.get(0);
 			
 			// Fill in the content
 			for (int i = 0; i < firstLine.getImageNum(); i++) {
 				ImageCell image = firstLine.getImage(i);
 				
 				if (image.outY < album_max_height) {
 					holder.icons[i + shiftIconIndex].setVisibility(View.VISIBLE);
 					holder.icons[i + shiftIconIndex].setScaleType(ImageView.ScaleType.FIT_XY);
 
 					int height = image.outY + image.outHeight + 2* Config.FOLDER_THUMBNAIL_PADDING;
 
 					if (height <= album_max_height) {
 						holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 										image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 										image.outHeight + 2*Config.FOLDER_THUMBNAIL_PADDING,
 										image.outX + shiftWidth,
 										image.outY + shiftHeight));
 						holder.icons[i + shiftIconIndex].setScaleType(ImageView.ScaleType.FIT_XY);
 					} else {
 						holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 										image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 										album_max_height - image.outY,
 										image.outX + shiftWidth,
 										image.outY + shiftHeight));
 					}
 
 					Bitmap bmp = mCacheAndAsyncWork.getBitmapFromRamCache(image.id);
 
 					if (bmp != null) {
 						holder.icons[i + shiftIconIndex].setImageBitmap(bmp);
 					} else {
 						CacheAndAsyncWork.BitmapWorkerTask task = mCacheAndAsyncWork.new BitmapWorkerTask(
 								holder.icons[i + shiftIconIndex], context.getContentResolver(), null);
 						task.execute(image.id);
 						holder.task[i + shiftIconIndex] = task;
 						holder.icons[i + shiftIconIndex].setImageResource(R.drawable.grey);
 					}
 					
 					holder.icons[i + shiftIconIndex].setId(mAlbumIndexArray[albumPosition]);
 				}
 			}
 
 			if (firstLine.getHeight() < album_max_height) {
 				
 				shiftIconIndex += Config.THUMBNAILS_PER_LINE;
 				
 				if(mGalleryLayouts.get(albumPosition).getLineNum() > 1) {
 						
 					ImageLineGroup secondLine = mGalleryLayouts.get(albumPosition).lines.get(1);
 					
 					for (int i = 0; i < secondLine.getImageNum(); i++) {
 						ImageCell image = secondLine.getImage(i);
 						int adjustY = image.outY + firstLine.getHeight();
 						
 						if (adjustY < album_max_height) {
 							holder.icons[i + shiftIconIndex].setVisibility(View.VISIBLE);
							holder.icons[i + shiftIconIndex].setScaleType(ImageView.ScaleType.FIT_XY);
 
 							int height = adjustY + image.outHeight + 2* Config.FOLDER_THUMBNAIL_PADDING;
 
 							if (height <= album_max_height) {
 								holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 												image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 												image.outHeight + 2*Config.FOLDER_THUMBNAIL_PADDING,
 												image.outX + shiftWidth,
 												adjustY + shiftHeight));
 								holder.icons[i + shiftIconIndex].setScaleType(ImageView.ScaleType.FIT_XY);
 							} else {
 								holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 												image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 												album_max_height - adjustY,
 												image.outX + shiftWidth,
 												adjustY + shiftHeight));
 							}
 
 							Bitmap bmp = mCacheAndAsyncWork.getBitmapFromRamCache(image.id);
 
 							if (bmp != null) {
 								holder.icons[i + shiftIconIndex].setImageBitmap(bmp);
 							} else {
 								CacheAndAsyncWork.BitmapWorkerTask task = mCacheAndAsyncWork.new BitmapWorkerTask(
 										holder.icons[i + shiftIconIndex], context.getContentResolver(), null);
 								task.execute(image.id);
 								holder.task[i + shiftIconIndex] = task;
 								holder.icons[i + shiftIconIndex].setImageResource(R.drawable.grey);
 							}
 							
 							holder.icons[i + shiftIconIndex].setId(mAlbumIndexArray[albumPosition]);
 						}
 					}
 				}
 				else {
 					holder.icons[shiftIconIndex].setVisibility(View.VISIBLE);
 					holder.icons[shiftIconIndex].setScaleType(ImageView.ScaleType.FIT_XY);
 					holder.icons[shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 							firstLine.getWidth(),
 							album_max_height - firstLine.getHeight(),
 							shiftWidth,
 							firstLine.getHeight() + shiftHeight));
 					holder.icons[shiftIconIndex].setImageResource(R.drawable.placehold);
 					
 					holder.icons[shiftIconIndex].setId(mAlbumIndexArray[albumPosition]);
 				}
 			}
 
 			int	despY = album_max_height - Config.ALBUM_DESCRIPTION_HEIGHT;
 
 			// Album Description
 			holder.albumDescBg[locate].setVisibility(View.VISIBLE);
 			holder.albumDescBg[locate].setLayoutParams(new AbsoluteLayout.LayoutParams(
 					firstLine.getWidth() - 2*Config.FOLDER_THUMBNAIL_PADDING,
 					Config.ALBUM_DESCRIPTION_HEIGHT,
 					Config.FOLDER_THUMBNAIL_PADDING + shiftWidth,
 					despY + shiftHeight));
 			holder.albumDescBg[locate].setBackgroundResource(R.drawable.grey);
 			
 			holder.albumDescName[locate].setVisibility(View.VISIBLE);
 			holder.albumDescName[locate].setLayoutParams(new AbsoluteLayout.LayoutParams(
 					firstLine.getWidth() - 2*Config.FOLDER_THUMBNAIL_PADDING,
 					Config.ALBUM_DESCRIPTION_HEIGHT/2,
 					Config.FOLDER_THUMBNAIL_PADDING + shiftWidth,
 					despY + shiftHeight));
 			holder.albumDescName[locate].setText(mAlbumName.get(albumPosition));
 			
 			holder.albumDescCount[locate].setVisibility(View.VISIBLE);
 			holder.albumDescCount[locate].setLayoutParams(new AbsoluteLayout.LayoutParams(
 					firstLine.getWidth() - 2*Config.FOLDER_THUMBNAIL_PADDING,
 					Config.ALBUM_DESCRIPTION_HEIGHT/2,
 					Config.FOLDER_THUMBNAIL_PADDING + shiftWidth,
 					despY + shiftHeight + Config.ALBUM_DESCRIPTION_HEIGHT/2));
 			holder.albumDescCount[locate].setText(mAlbumCount.get(albumPosition).toString());
 		}
 
 	    class ViewHolder{
 			ImageView icons[] = new GestureImageView[Config.THUMBNAILS_PER_LINE*4];
 			CacheAndAsyncWork.BitmapWorkerTask task[] = new CacheAndAsyncWork.BitmapWorkerTask[Config.THUMBNAILS_PER_LINE*4];
 			
 			TextView albumDescName[] = new TextView[2];
 			TextView albumDescCount[] = new TextView[2];
 			ImageView albumDescBg[] = new ImageView[2];
 		}
 	}
 	
     public void entryImageList(View view) {
     	
         final Intent i = new Intent(FolderList.this, ImageList.class);
         
 		int clickItemInfo[] = new int[2];
 		view.getLocationOnScreen(clickItemInfo);
 		
 		clickItemInfo[1] -= mContentTop;
 		
         i.putExtra(Config.CLICK_ITEM_INFO, clickItemInfo);
         i.putExtra(Config.ALBUM_INDEX, view.getId());
         
         Utils.putPrefAnimFlag(this, true);
         
         startActivity(i);
         
         overridePendingTransition(R.anim.out, R.anim.in);
     }
 }
