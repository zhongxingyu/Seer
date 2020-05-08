 package ca.cmput301f13t03.adventure_datetime.model;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.UUID;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Base64;
 import android.util.Log;
 
 public class Image {
 	
 	private UUID _id;
 	private String encodedBitmap;
 	private transient Bitmap bitmap;
 	private transient boolean dirty;
 
     public static String compressBitmap(Bitmap bit, int qual) {
         Bitmap bitmapex = bit;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapex.compress(Bitmap.CompressFormat.JPEG, qual, baos);
         return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
     }
 	
 	public Image(String bitmap) {
 		this._id = UUID.randomUUID();
 		this.encodedBitmap = bitmap;
 	}
 	
 	public Image(Bitmap bitmap) {
 		this._id = UUID.randomUUID();
 		encodeBitmap(bitmap);
 	}
 	
 	public Image(UUID id, String bitmap) {
 		this(bitmap);
 		this._id = id;
 	}
 	
 	public Image(UUID id, Bitmap bitmap) {
 		this(bitmap);
 		this._id = id;
 	}
 
 	public UUID getId() {
 		return _id;
 	}
 	
 	public void setId(UUID id) {
 		this._id = id;
 	}
 	
 	public String getEncodedBitmap() {
 		return encodedBitmap;
 	}
 	
 	public void setBitmap(String bitmap) {
 		this.dirty = true;
 		this.encodedBitmap = bitmap;
 	}
 	
 	public void setBitmap(Bitmap bitmap) {
 		this.dirty = true;
 		encodeBitmap(bitmap);
 	}
 	
 	public Bitmap decodeBitmap() {
 		if (dirty || bitmap == null) {
 			byte[] decodedThumbnail = Base64.decode(this.encodedBitmap, 0);
 			this.bitmap = BitmapFactory.decodeByteArray(decodedThumbnail, 0, decodedThumbnail.length);
 			this.dirty = false;
 		}
 		
 		return bitmap;
 	}
 	
 	private void encodeBitmap(Bitmap bitmap) {
 		Bitmap bitmapex = bitmap;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		bitmapex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
 		this.encodedBitmap = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
 		
 		try {
 			baos.close();
 		} catch (IOException e) {
 			Log.e("Story", "Error closing stream", e);
 		}
 	}
 }
