 package edu.grinnell.sandb.img;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import edu.grinnell.sandb.data.Article;
 
 public class BodyImageGetter {
 
 	private static ImageTable mImageTable;
 
 	private static int numTasks = 0;
 
 	public BodyImageGetter(Context context) {
 		if (mImageTable == null)
 			mImageTable = new ImageTable(context);
 	}
 
 	public void buildImageCache(Article article) {
 		new ImageHelper().execute(article);
 	}
 
 	public class ImageHelper extends AsyncTask<Article, Integer, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			if (numTasks++ == 0) {
 				mImageTable.open();
 				mImageTable.clearTable();
 			}
 		}
 
 		@Override
 		protected Integer doInBackground(Article... article) {
 
 			String body = article[0].getBody();
 			int articleId = article[0].getId();
 
 			readImage(body, articleId);
 			return null;
 		}
 	}
 
 	protected void onPostExecute(Integer i) {
 		if (--numTasks == 0)
 			mImageTable.close();
 	}
 
 	// Read an image from the body as a byte array
 	public void readImage(String body, int articleID) {
 
 		addImage(body, articleID, "<div");
 //		addImage(body, articleID, "<a");
 		addImage(body, articleID, "<img");
 	}
 
 	private void addImage(String body, int articleId, String tag) {
 		int tagStart = 0;
 		String url = "";
 		byte[] image = null;
 		String title = "";
 
 		while ((tagStart = body.indexOf(tag, tagStart + 1)) >= 0) {
 			url = getSubstring("src=\"", body, tagStart);
 			
 			//check to make sure image has not yet been added
 			if ((Image) mImageTable.findByUrl(url) != null)
 				return;
 
 			//image = getImage(url, tagStart);
 			title = getSubstring("title=\"", body, tagStart);
 			
 			mImageTable.createImage(articleId, url, image, title);
 		}
 
 	}
 
 	private static byte[] getImage(String imgSource, int start) {
 		// download image as byte array
 
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 
 		int nRead;
 		byte[] data = new byte[16384];
 
 		InputStream is;
 		try {
 			is = fetch(imgSource);
 			while ((nRead = is.read(data, 0, data.length)) != -1) {
 				buffer.write(data, 0, nRead);
 			}
 			buffer.flush();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		return buffer.toByteArray();
 
 	}
 
 	private static InputStream fetch(String urlString)
 			throws IllegalArgumentException, MalformedURLException, IOException {
 
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		HttpGet request = new HttpGet(urlString);
 		HttpResponse response = httpClient.execute(request);
 
 		return response.getEntity().getContent();
 	}
 
 	// return a string starting immediately after the key, and ending at the
 	// first quotation mark
 	private static String getSubstring(String key, String body, int start) {
 		int subStart = 0;
 		int subEnd = 0;
 		String substring = "";
 
 		// start at beginning of link
 		subStart = body.indexOf(key, start) + key.length();
 
		if (subStart >= 0) {
			subEnd = body.indexOf("\"", subStart);
		}
		
		if (subEnd >= subStart) {
			substring = body.substring(subStart, subEnd);
		}
 
 		return substring;
 	}
 }
