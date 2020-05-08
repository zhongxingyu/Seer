 package edu.cornell.cordovaocr;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.util.Log;
 
 import com.googlecode.leptonica.android.Pixa;
 import com.googlecode.tesseract.android.ResultIterator;
 import com.googlecode.tesseract.android.TessBaseAPI;
 
 public class Ocr extends CordovaPlugin {
 	private static final String TAG = "Tesseract";
 	
     private static final String DEFAULT_LANG = "eng";
     
     private static final String TESSDATA_PATH = "tessdata";
 
     private static final String INIT                         = "init";
     private static final String GET_INIT_LANGUAGES_AS_STRING = "getInitLanguagesAsString";
     private static final String CLEAR                        = "clear";
     private static final String SET_VARIABLE                 = "setVariable";
     private static final String SET_PAGE_SEG_MODE            = "setPageSegMode";
     private static final String SET_RECTANGLE                = "setRectangle";
     private static final String SET_IMAGE                    = "setImage";
     private static final String GET_UTF8_TEXT                = "getUTF8Text";
     private static final String MEAN_CONFIDENCE              = "meanConfidence";
     private static final String WORD_CONFIDENCES             = "wordConfidences";
     private static final String GET_REGIONS                  = "getRegions";
     private static final String GET_TEXTLINES                = "getTextlines";
     private static final String GET_STRIPS                   = "getStrips";
     private static final String GET_WORDS                    = "getWords";
     private static final String GET_CHARACTERS               = "getCharacters";
     private static final String GET_RESULTS                  = "getResults";
     
     private static final List<String> ACTIONS = Arrays.asList(
     	INIT, GET_INIT_LANGUAGES_AS_STRING, CLEAR, SET_VARIABLE, SET_PAGE_SEG_MODE, SET_RECTANGLE,
     	SET_IMAGE, GET_UTF8_TEXT, MEAN_CONFIDENCE, WORD_CONFIDENCES, GET_REGIONS, GET_TEXTLINES,
     	GET_STRIPS, GET_WORDS, GET_CHARACTERS, GET_RESULTS
     );
     
     private static final int BYTES_PER_PIXEL = 4;
     private static final int BITMAP_MEM_LIMIT = 5000000;
     
 	TessBaseAPI tess;
 	ResultIterator resultIterator;
 	Bitmap image;
 	Uri imageUri;
 	int imageScale;
 	
 	public Ocr() {
 		tess = new TessBaseAPI();
 	}
 	
 	@Override
 	public boolean execute(final String action, final JSONArray args,
 			final CallbackContext callbackContext) {
 		if (!ACTIONS.contains(action)) {
 			return false;
 		}
 		
 		cordova.getThreadPool().execute(new Runnable() {
 			public void run() {
 				try {
 					executeAsync(action, args, callbackContext);
 				} catch (Exception e) {
 					e.printStackTrace();
 					callbackContext.error(e.toString());
 				}
 			}
 		});
 		
 		return true;
 	}
 	
	public synchronized void executeAsync(String action, JSONArray args,
			CallbackContext callbackContext)
 			throws JSONException, FileNotFoundException, IOException {
 		Log.d(TAG, "Executing action: " + action);
 		
 		if (INIT.equals(action)) {
 			tess.init(getTessPath(), args.optString(0, DEFAULT_LANG));
 			callbackContext.success();
 		}
 		else if (GET_INIT_LANGUAGES_AS_STRING.equals(action)) {
 			callbackContext.success(tess.getInitLanguagesAsString());
 		}
 		else if (CLEAR.equals(action)) {
 			tess.clear();
 			callbackContext.success();
 		}
 		else if (SET_VARIABLE.equals(action)) {
 			String key = args.getString(0);
 			String val = args.getString(1);
 			boolean success = tess.setVariable(key, val);
 			if (success) {
 				callbackContext.success();
 			} else {
 				callbackContext.error("Lookup failed for key: " + key);
 			}
 		}
 		else if (SET_PAGE_SEG_MODE.equals(action)) {
 			tess.setPageSegMode(args.getInt(0));
 			callbackContext.success();
 		}
 		else if (SET_RECTANGLE.equals(action)) {
 			JSONObject r = args.getJSONObject(0);
 			tess.setRectangle(r.getInt("x"), r.getInt("y"), r.getInt("width"), r.getInt("height"));
 			callbackContext.success();
 		}
 		else if (SET_IMAGE.equals(action)) {
 			setImage(args.getString(0));
 			callbackContext.success();
 		}
 		else if (GET_UTF8_TEXT.equals(action)) {
 			callbackContext.success(tess.getUTF8Text());
 		}
 		else if (MEAN_CONFIDENCE.equals(action)) {
 			callbackContext.success(tess.meanConfidence());
 		}
 		else if (WORD_CONFIDENCES.equals(action)) {
 			callbackContext.success(new JSONArray(Arrays.asList(tess.wordConfidences())));
 		}
 		else if (GET_REGIONS.equals(action)) {
 			callbackContext.success(getBoxes(tess.getRegions()));
 		}
 		else if (GET_TEXTLINES.equals(action)) {
 			callbackContext.success(getBoxes(tess.getTextlines()));
 		}
 		else if (GET_STRIPS.equals(action)) {
 			callbackContext.success(getBoxes(tess.getStrips()));
 		}
 		else if (GET_WORDS.equals(action)) {
 			callbackContext.success(getBoxes(tess.getWords()));
 		}
 		else if (GET_CHARACTERS.equals(action)) {
 			callbackContext.success(getBoxes(tess.getWords()));
 		}
 		else if (GET_RESULTS.equals(action)) {
 			// We need to force recognition before we can use the ResultIterator
 			tess.getUTF8Text();
 			
 			int level = args.getInt(0);
 			JSONArray results = new JSONArray();
 			ResultIterator it = tess.getResultIterator();
 			it.begin();
 			do {
 				JSONObject obj = new JSONObject();
 				obj.put("text", it.getUTF8Text(level));
 				obj.put("confidence", it.confidence(level));
 				results.put(obj);
 			} while (it.next(level));
 			
 			callbackContext.success(results);
 		}
 	}
 	
 	// Attempts to set the image for Tesseract.
 	private void setImage(String uriString) throws FileNotFoundException {
 		// Get image URI
 		Uri uri = Uri.parse(uriString);
 		
 		// If Tesseract not already using this image, we set it now
 		if (!uri.equals(imageUri)) {
 			Log.d(TAG, "Setting new image in Tesseract");
 			// Remove old bitmap to make space for the new
 			if (image != null) {
 				tess.clear();
 				image.recycle();
 				// Set nulls so we aren't in undefined state if tess.setImage fails
 				image = null;
 				imageUri = null;
 			}
 			// Get bitmap
 			Bitmap bmp = getBitmap(uri);
 			if (bmp == null) {
 				throw new IllegalArgumentException("Invalid image format");
 			}
 			// Set bitmap
 	        tess.setImage(bmp);
 	        image = bmp;
 			imageUri = uri;
 		}
 	}
 	
 	private Bitmap getBitmap(Uri imageUri) throws FileNotFoundException {
 		// Get bitmap size without loading bitmap
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 		opts.inJustDecodeBounds = true;
 		ContentResolver cr = this.webView.getContext().getContentResolver();
 		BitmapFactory.decodeStream(cr.openInputStream(imageUri), null, opts);
 		double bitmapMem = (double) opts.outWidth * opts.outHeight * BYTES_PER_PIXEL;
 		
 		// Determine scaling factor; need to round up to nearest power of 2
 		// using formula 2^ceil(log_2(scale))
 		double scale = Math.sqrt(Math.max(1, bitmapMem / BITMAP_MEM_LIMIT));
 		imageScale = 1 << (int) Math.ceil(Math.log(scale)/Math.log(2));
 		Log.d(TAG, String.format("bitmapMem = %d; scale = %d\n", (int) bitmapMem, imageScale));
 		
 		opts.inSampleSize = imageScale;
 		opts.inJustDecodeBounds = false;
 		opts.inPreferredConfig = Config.ARGB_8888;
 		return BitmapFactory.decodeStream(cr.openInputStream(imageUri), null, opts);
 	}
 	
 	private String getTessPath() throws IOException {
 		
 		Context ctx = this.webView.getContext();
 		String[] files = ctx.getAssets().list(TESSDATA_PATH);
 		File dir = ctx.getFilesDir();
 		File tessdata = new File(dir, TESSDATA_PATH);
 		tessdata.mkdirs();
 
 		// Move data files from assets to filesystem
 		for (String fileName : files) {
 			File file = new File(tessdata, fileName);
 			if (file.exists()) { continue; }
 			
 			InputStream in = ctx.getAssets().open(TESSDATA_PATH + File.separator + fileName);
 			OutputStream out = new FileOutputStream(file);
 			
 			byte[] buffer = new byte[1024];
 			int len = 0;
 			while ((len = in.read(buffer)) != -1) {
 			    out.write(buffer, 0, len);
 			}
 			in.close();
 			out.close();
 		}
 		
 		return ctx.getFilesDir().getAbsolutePath();
 	}
 	
 	private JSONArray getBoxes(Pixa pixa) throws JSONException {
 		List<Rect> rects = pixa.getBoxRects();
 		JSONArray result = new JSONArray();
 		for (int i = 0; i < rects.size(); i++) {
 			Rect r = rects.get(i);
 			JSONObject box = new JSONObject();
 			box.put("x", imageScale * r.left);
 			box.put("y", imageScale * r.top);
 			box.put("w", imageScale * r.width());
 			box.put("h", imageScale * r.height());
 			result.put(box);
 		}
 		return result;
 	}
 }
