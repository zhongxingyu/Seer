 package com.example.drivequickstart.view;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.ThumbnailUtils;
 import android.os.AsyncTask;
 import android.provider.MediaStore.Video.Thumbnails;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckedTextView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.example.drivequickstart.Logger;
 import com.example.drivequickstart.R;
 import com.example.drivequickstart.model.DataModel;
 import com.haarman.listviewanimations.ArrayAdapter;
 
 public class MyListAdapter extends ArrayAdapter<String> {
 	private BaseActivity mActivity;
 	private int dataMode;
 	
 	public MyListAdapter(BaseActivity activity, ArrayList<String> items, int dataMode) {
 		super(items);
 		mActivity = activity;
 		this.dataMode = dataMode;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Logger.debug("Priyanka", "Getting view at position: " + position);
 		ViewHolder viewHolder;
 		String filePath = getItem(position);
 		String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
 		LinearLayout lview = (LinearLayout)convertView;
 
 		if (lview == null) {
 			lview = (LinearLayout) LayoutInflater.
 					from(mActivity).
 					inflate(R.layout.activity_animateremoval_row, parent, false);
 			
 			viewHolder = new ViewHolder();
			viewHolder.ctv = (CheckedTextView)lview.getChildAt(1);
			viewHolder.image = (ImageView)lview.getChildAt(0);//(ImageView) mActivity.findViewById(R.id.thumbnail_microkind);
//			viewHolder.ctv = (CheckedTextView)mActivity.findViewById(R.id.CheckedTextView);
//			viewHolder.image = (ImageView) mActivity.findViewById(R.id.thumbnail_microkind);

 			lview.setTag(viewHolder);
 		}
 		
		viewHolder = (ViewHolder) lview.getTag();
		
 		viewHolder.position = position;
 		List<Integer>mSelectedPositions = mActivity.getSelectedPositions();
 		
 		if (viewHolder.ctv != null) viewHolder.ctv.setText(fileName);
 		if (viewHolder.ctv != null) viewHolder.ctv.setChecked(mSelectedPositions.contains(position));
         
 		new ThumbnailTask(position, viewHolder, this.dataMode).execute(filePath);
 		//new ThumbnailTask(position, viewHolder, this.dataMode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePath);
 		
 		return lview;
 	}
 	
 	public static Bitmap createPictureThumbnail(String fileName) {
 		Bitmap imageBitmap = null;
 		
         try     
         {
             final int THUMBNAIL_SIZE = 96;
 
             FileInputStream fis = new FileInputStream(fileName);
             imageBitmap = BitmapFactory.decodeStream(fis);
 
             imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
 
             ByteArrayOutputStream baos = new ByteArrayOutputStream();  
             imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
             
         } catch (OutOfMemoryError e) {
         	System.out.println("Priyanka-Failed to create picture thumbnail: " + e.getMessage());
         	e.printStackTrace();
         } catch(Exception ex) {
         	ex.printStackTrace();
         }
         
         return imageBitmap;
 	}
 	
 	private static class ThumbnailTask extends AsyncTask<String, Void, Bitmap> {
 	    private int mPosition;
 	    private ViewHolder mHolder;
 	    private int mediaType;
 
 	    public ThumbnailTask(int position, ViewHolder holder, int mediaType) {
 	        mPosition = position;
 	        mHolder = holder;
 	        this.mediaType = mediaType;
 	    }
 
 	    @Override
 	    protected void onPostExecute(Bitmap bitmap) {
 	        if (mHolder != null && mHolder.position == mPosition) {
 	            if(mHolder.image != null) {
 	            	Logger.debug("Priyanka", "Setting image at position: " + 
 		        			mPosition);
 	            	mHolder.image.setImageBitmap(bitmap);
 	            	//if (this.mediaType == DataModel.MODE_PICTURES) System.out.println("Priyanka-posted image at:  " + mPosition);
 	            } else {
 	            	//if (this.mediaType == DataModel.MODE_PICTURES)  System.out.println("Priyanka:[Image is null]missed image at:"+mPosition);
 	            	Logger.debug("Priyanka", "No Image found for position: " + 
 		        			mPosition);
 	            }
 	        } else {
 	        	//if (this.mediaType == DataModel.MODE_PICTURES) System.out.println("Priyanka:[holder null]missed image at:"+mPosition);
 	        	Logger.debug("Priyanka", "Holder is Null for position: " + 
 	        			mPosition);
 	        }
 	    }
 
 		@Override
 		protected Bitmap doInBackground(String... params) {
 			String fileName = params[0];
 			Bitmap image = null;
 			
 			try {
 				if (this.mediaType == DataModel.MODE_PICTURES) {
 					image = createPictureThumbnail(fileName);
 				} else if (this.mediaType == DataModel.MODE_VIDEOS) {
 					image = ThumbnailUtils.createVideoThumbnail(fileName, 
 				    		   Thumbnails.MICRO_KIND);
 				}
 			} catch (OutOfMemoryError e) {
 				e.printStackTrace();
 			}
 			return image;
 		}
 	}
 	private static class ViewHolder {
 		public ImageView image;
 		public CheckedTextView ctv;
 		public int position;
 	}
 }
