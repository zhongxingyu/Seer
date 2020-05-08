 package cmput301.f13t01.createyourownadventure;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.Uri;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class StoryFragmentViewFactory {
 
 	@SuppressWarnings("rawtypes")
 	public static void ConstructView(LinearLayout layout,
 			ArrayList<Media> content, Context context, Boolean forEdit) {
 
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 
 		// Display the fragment
 		for (Media media : content) {
 			if (media.getClass().equals(Text.class)) {
 				if (forEdit) {
 					EditText edit = new EditText(context);
 					edit.setTextColor(Color.BLACK);
 					edit.setText(media.getContent().toString());
 					layout.addView(edit, params);
 				} else {
 					TextView text = new TextView(context);
 					text.setTextColor(Color.BLACK);
 					text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
 					text.setText((CharSequence) media.getContent());
 					layout.addView(text, params);
 				}
 			} else if (media.getClass().equals(Image.class)) {
 				Image image = (Image) media;
 				Uri imageUri = Uri.parse(context.getFilesDir()
 						.getAbsolutePath()
 						+ "/"
						+ image.type.toString()
 						+ "/"
 						+ image.getContent());
 				addImage(imageUri, layout, context);
 			} else if (media.getClass().equals(ImageUri.class)) {
 				ImageUri image = (ImageUri) media;
 				addImage(image.getContent(), layout, context);
 			}
 		}
 	}
 
 	public static void addImage(Uri image, LinearLayout layout, Context context) {
 		try {
 
 			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 					LinearLayout.LayoutParams.MATCH_PARENT,
 					LinearLayout.LayoutParams.WRAP_CONTENT);
 
 			ImageView imageView = new ImageView(context);
 			imageView.setVisibility(View.VISIBLE);
 			imageView.setAdjustViewBounds(true);
 
 			Log.d("oops", "Layout width: " + layout.getWidth() + " height: "
 					+ layout.getHeight());
 
 			Bitmap bitmap = decodeUri(image, 256, 256, context);
 
 			imageView.setImageBitmap(bitmap);
 
 			layout.addView(imageView, params);
 
 		} catch (FileNotFoundException e) {
 			Log.d("oops", "Couldn't find the file...");
 			e.printStackTrace();
 		}
 	}
 
 	public static Bitmap decodeUri(Uri selectedImage, int reqWidth,
 			int reqHeight, Context context) throws FileNotFoundException {
 
 		// First decode image size
 		final BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeStream(context.getContentResolver()
 				.openInputStream(selectedImage), null, options);
 
 		// Calculate inSampleSize
 		options.inSampleSize = calculateInSampleSize(options, reqWidth,
 				reqHeight);
 
 		// Decode bitmap with inSampleSize set
 		options.inJustDecodeBounds = false;
 		return BitmapFactory.decodeStream(context.getContentResolver()
 				.openInputStream(selectedImage), null, options);
 
 	}
 
 	public static int calculateInSampleSize(BitmapFactory.Options options,
 			int reqWidth, int reqHeight) {
 		// Raw height and width of image
 		final int height = options.outHeight;
 		final int width = options.outWidth;
 		int inSampleSize = 1;
 
 		if (height > reqHeight || width > reqWidth) {
 
 			final int halfHeight = height / 2;
 			final int halfWidth = width / 2;
 
 			// Calculate the largest inSampleSize value that is a power of 2 and
 			// keeps both
 			// height and width larger than the requested height and width.
 			while ((halfHeight / inSampleSize) > reqHeight
 					&& (halfWidth / inSampleSize) > reqWidth) {
 				inSampleSize *= 2;
 			}
 		}
 
 		return inSampleSize;
 	}
 
 }
