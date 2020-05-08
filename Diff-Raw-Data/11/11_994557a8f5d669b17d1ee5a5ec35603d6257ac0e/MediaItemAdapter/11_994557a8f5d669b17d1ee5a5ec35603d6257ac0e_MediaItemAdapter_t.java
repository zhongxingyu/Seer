 package dimitri.suls.allshare.media.frontend.listadapters;
 
 import java.io.FileInputStream;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.sec.android.allshare.Item;
 import com.sec.android.allshare.Item.MediaType;
 
 import dimitri.suls.allshare.R;
 
 public class MediaItemAdapter extends ArrayAdapter<Item> {
 	private LayoutInflater layoutInflater;
 
 	public MediaItemAdapter(Context context, List<Item> objects) {
 		super(context, 0, objects);
 
 		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	// TODO: Add thumbnail for images/video/audio
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Item mediaItem = getItem(position);
 		View view = layoutInflater.inflate(R.layout.mediaitem_view, null);
 
 		TextView textViewTitle = (TextView) view.findViewById(R.id.textViewTitle);
		// TODO: Add artist, album, .. for audio files. Not possible at the
		// moment, due to AllShare restrictions on Item-class.
 		textViewTitle.setText(mediaItem.getTitle());
 
 		final ImageView imageViewThumbnail = (ImageView) view.findViewById(R.id.imageViewThumbnail);
 
 		Uri thumbnailUri = mediaItem.getThumbnail();
 
 		if (thumbnailUri != null) {
 			imageViewThumbnail.setImageURI(thumbnailUri);
 		} else if (mediaItem.getType() == MediaType.ITEM_IMAGE) {
 			AsyncTask<Item, Void, Bitmap> bitmapLoader = new AsyncTask<Item, Void, Bitmap>() {
 				@Override
 				protected Bitmap doInBackground(Item... mediaItems) {
 					Item mediaItem = mediaItems[0];
 					String filePath = mediaItem.getURI().getPath();
 
 					Bitmap bitmap = null;
 
 					try {
 						bitmap = getBitmap(filePath);
 					} catch (Exception exception) {
 					}
 
 					return bitmap;
 				}
 
 				@Override
 				protected void onPostExecute(Bitmap result) {
 					imageViewThumbnail.setImageBitmap(result);
 
 					super.onPostExecute(result);
 				}
 			};
 
 			bitmapLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediaItem);
 		}
 
 		return view;
 	}
 
 	private Bitmap getBitmap(String path) throws Exception {
 		int IMAGE_MAX_SIZE = 3000000;
 		FileInputStream fileInputStream = new FileInputStream(path);
 		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
 
 		// Decode image size
 		bitmapFactoryOptions.inJustDecodeBounds = true;
 		BitmapFactory.decodeStream(fileInputStream, null, bitmapFactoryOptions);
 
 		fileInputStream.close();
 
 		int scale = 1;
 		while ((bitmapFactoryOptions.outWidth * bitmapFactoryOptions.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
 			scale++;
 		}
 
 		Bitmap bitmap = null;
 		fileInputStream = new FileInputStream(path);
 
 		if (scale > 1) {
 			scale--;
 			// Scale to max possible inSampleSize that still yields an image
 			// larger than target
 			bitmapFactoryOptions = new BitmapFactory.Options();
 			bitmapFactoryOptions.inSampleSize = scale;
 			bitmap = BitmapFactory.decodeStream(fileInputStream, null, bitmapFactoryOptions);
 
 			// resize to desired dimensions
 			int height = bitmap.getHeight();
 			int width = bitmap.getWidth();
 
 			double dstHeight = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
 			double dstWidth = (dstHeight / height) * width;
 
 			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) dstWidth, (int) dstHeight, true);
 
 			bitmap.recycle();
 			bitmap = scaledBitmap;
 		} else {
 			bitmap = BitmapFactory.decodeStream(fileInputStream);
 		}
 
 		fileInputStream.close();
 
 		return bitmap;
 	}
 }
