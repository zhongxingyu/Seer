 package com.kukung.app.model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.util.Log;
 
 public class TOC {
 	private static List<TOCItem> itemList;
 	private static String sdCardLocation = "/sdcard/pk111/";
 	private static String appFolderPath = "pk111";
 	private static HashMap<String, Bitmap> cachePageImageMap = new HashMap<String, Bitmap>();
 	
 	public static List<TOCItem> buildTOC(Context context) {
 		if (itemList == null) {
 			try {
 				itemList = loadTOCFromXml(context);
 			} catch (Exception e) {
 				itemList = new ArrayList<TOCItem>();
 			}
 		}
 		return itemList;
 	}
 
 	private static List<TOCItem> loadTOCFromXml(Context context) throws Exception {
 		List<TOCItem> tocItemList = new ArrayList<TOCItem>();
 		AssetManager assetManager = context.getResources().getAssets();
 		InputStream istream = assetManager.open("toc.xml");
 		
 		Document document = XmlDocumentBuilder.XMLfromString(istream);
 		
 		NodeList tocItemNodeList = document.getElementsByTagName("item");
 		for (int i = 0; i < tocItemNodeList.getLength(); i++) {
 			Node itemNode = tocItemNodeList.item(i);
 			if(itemNode != null) {
 				TOCItem tocItem = new TOCItem();
 				tocItem.build(itemNode.getChildNodes());
 				tocItemList.add(tocItem);
 			}
 		}
 
 		return tocItemList;
 	}
 
 	public static boolean loadImage(String fileId, URL imageUrl) {
 		if (isThereCachedImage(fileId)) {
 			return true;
 		}
 		
 		Bitmap bitmap;
 		initializaeSDCardPath();
 		createFolder(sdCardLocation);
 		
 		if (isThereImageAtSDCard(fileId)) {
 			bitmap = loadBitmapImageFromSDCard(fileId);
 		} else {
 			bitmap = downloadBitmap(imageUrl.toString());
 			if (bitmap != null) {
 				savePngFile(bitmap, getImageFilePathAtSDCard(fileId));
 			}
 		}
 		
 		if (bitmap == null) {
 			return false;
 		} else {
 			cachePageImageMap.put(fileId, bitmap);
 			return true;
 		}
 	}
 	
 	private static void initializaeSDCardPath() {
 		File sdCardFile = Environment.getExternalStorageDirectory();
 		if (sdCardFile.exists()) {
 			sdCardLocation = sdCardFile.getAbsolutePath()+File.separator+appFolderPath+File.separator;
 		}
 	}
 
 	public static String getImageFilePathAtSDCard(String fileId) {
 		return sdCardLocation+fileId+".png";
 	}
 	
 	private static Bitmap loadBitmapImageFromSDCard(String fileId) {
 		FileInputStream fileInputStream = null;
 		try {
 			fileInputStream = new FileInputStream(new File(TOC.getImageFilePathAtSDCard(fileId)));
 			Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream); 
 			return bitmap;
 		} catch (FileNotFoundException e) {
 			return null;
 		} finally {
 			try {
 				if (fileInputStream != null) {
 					fileInputStream.close();
 					fileInputStream = null;
 				}
 			} catch (IOException e) {}
 		}
 	}
 	
 	private static boolean isThereImageAtSDCard(String fileId) {
 		File imageFile = new File(getImageFilePathAtSDCard(fileId));
 		if (imageFile.exists()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private static boolean isThereCachedImage(String fileId) {
 		Bitmap bitmap = cachePageImageMap.get(fileId);
 		if (bitmap == null) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private static void createFolder(String strFolderPath) 
 	{
 		File file = new File(strFolderPath);
 		if(!file.exists()) {
 			file.mkdirs();
 		}
 	} 
 	
 	private static void savePngFile(Bitmap bmp, String strFilePath) {
 	    File file = new File(strFilePath);
 	    OutputStream out = null;
 	    
 	    try {
 	      file.createNewFile();
 	      out = new FileOutputStream(file);
 	      
	      // Bitmap.CompressFormat.JPEG  
	      //  ǰ   ( 100%)
 	      bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
 	    } catch(Exception e) {
 	      e.printStackTrace();
 	    } finally {
 	      try {
 	        out.close();
 	      } catch(IOException e) {
 	        e.printStackTrace();
 	      }
 	    }    
 	} 
 	
 	static Bitmap downloadBitmap(String url) 
 	  { 
 	    final HttpClient client = new DefaultHttpClient(); 
 	    final HttpGet getRequest = new HttpGet(url); 
 	 
 	    try 
 	    { 
 	      HttpResponse response = client.execute(getRequest); 
 	      final int statusCode = response.getStatusLine().getStatusCode(); 
 	      if(statusCode != HttpStatus.SC_OK) { 
 	        Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
 	        return null; 
 	      } 
 	      
 	      final HttpEntity entity = response.getEntity(); 
 	      if(entity != null) 
 	      { 
 	        InputStream inputStream = null; 
 	        //BitmapFactory.Options options = new BitmapFactory.Options(); 
 	        //options.inSampleSize = 2; 
 	         
 	        try 
 	        { 
 	          inputStream = entity.getContent(); 
 	          final Bitmap bitmap = BitmapFactory.decodeStream(inputStream); 
 	          return bitmap; 
 	        } 
 	        finally 
 	        { 
 	          if(inputStream != null) 
 	          { 
 	            inputStream.close(); 
 	          } 
 	          entity.consumeContent(); 
 	        } 
 	      } 
 	    } 
 	    catch(Exception e) 
 	    { 
 	    	Log.e("pk111", "fail at downloading", e);
 	      getRequest.abort(); 
 	    } 
 	    return null; 
 	  } 
 
 }
 
 
 
