 package com.normalexception.forum.rx8club.bitmap;
 
 /************************************************************************
  * NormalException.net Software, and other contributors
  * http://www.normalexception.net
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ************************************************************************/
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.graphics.Bitmap;
 
 import com.normalexception.forum.rx8club.Log;
 import com.normalexception.forum.rx8club.utils.MemoryManagement;
 
 public class RegisteredBitmap {
 	
 	private int id = 0;
 	private Bitmap bmp = null;
 	private static String TAG = "RegisteredBitmap";
 	
 	private static Map<String,RegisteredBitmap> archive = 
 			new HashMap<String,RegisteredBitmap>(); 
 	
 	private static final double HEAP_THRESHOLD = 0.1;
 	
 	/**
 	 * Create a registered bitmap.  This is a container for a bitmap that 
 	 * links a bitmap to an ID.  The id is a random number that is associated
 	 * to a thread.  This creates a bond between a thread and its bitmaps.
 	 * @param id		The id that represents a thread
 	 * @param source	The source where the image exists
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public RegisteredBitmap(int id, String source) 
 			throws MalformedURLException, IOException {
 		this.id = id;
 		
 		RegisteredBitmap temp = archive.get(source);
 		double currentHeap = MemoryManagement.getFreeHeapSize();
 		
 		if(temp == null) {
 			Log.d(TAG, String.format("Creating New Bitmap (%d)", id));
 			Log.d(TAG, String.format("Current Free Heap (%f)", currentHeap));
 			
 			if(currentHeap < HEAP_THRESHOLD)
 				recycleAll();
 			
             bmp = BitmapDecoder.decodeSource(source);
 			archive.put(source, this);
 		} else {
 			Log.d(TAG, "Reporting Cached Bitmap");
 			bmp = temp.getBitmap();
 		}
 	}
 	
 	/**
 	 * Report the bitmap
 	 * @return	The bitmap object
 	 */
 	public Bitmap getBitmap() {
 		return this.bmp;
 	}
 	
 	/**
 	 * Recycle all bitmaps in the archive
 	 */
 	public static void recycleAll() {
 		Log.d(TAG, "Recycling All Bitmaps");
 		for(RegisteredBitmap rBmp : archive.values())
 			rBmp.getBitmap().recycle();
 		archive.clear();
 	}
 	
 	/**
 	 * Recycle bitmaps based on their id
 	 * @param id	The thread id that the bitmaps are going to be recycled from
 	 */
 	public static void recycleById(int id) {
 		Log.d(TAG, String.format("Cleaning Out Bitmaps (%d)", id));
 		MemoryManagement.printCurrentMemoryInformation();
 		
 		ArrayList<String> toRemove = new ArrayList<String>();
 		
 		for(Map.Entry<String,RegisteredBitmap> ent : archive.entrySet()) {
			if(!ent.getValue().getBitmap().isRecycled()) {
 				if(ent.getValue().id == id) {
 					ent.getValue().getBitmap().recycle();
 					toRemove.add(ent.getKey());
 				}
 			}
 		}
 		
 		// Remove all keys
 		for(String key : toRemove)
 			archive.remove(key);
 		
 		MemoryManagement.printCurrentMemoryInformation();
 	}
 }
