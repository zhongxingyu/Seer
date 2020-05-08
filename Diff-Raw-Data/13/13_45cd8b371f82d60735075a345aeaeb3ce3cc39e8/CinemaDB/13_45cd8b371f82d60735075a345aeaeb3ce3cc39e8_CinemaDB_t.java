 package ua.in.leopard.androidCoocooAfisha;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class CinemaDB {
 	private int id;
 	private String title;
 	private String orig_title;
 	private String year;
 	private String poster = null;
 	private String description;
 	
 	private String zal_title = null;
 	private String times = null;
 	private String prices = null;
 	private byte[] cached_poster = null;
 	
 	
 	// Constructor for the TheaterDB class
 	public CinemaDB(int id, String title, String orig_title, String year, String poster, String description) {
 		super();
 		this.id = id;
 		this.title = title;
 		this.orig_title = orig_title;
 		this.year = year;
 		this.poster = poster;
 		this.description = description;
 	}
 	
 	
 	public Boolean equal(CinemaDB eq_object){
 		if (
 			(eq_object.getId() == this.getId()) && 
 			(eq_object.getTitle().equals(this.getTitle()))
 			){
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	// Getter and setter methods for all the fields.
 	// Though you would not be using the setters for this example,
 	// it might be useful later.
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getOrigTitle() {
 		return orig_title;
 	}
 	public void setOrigTitle(String orig_title) {
 		this.orig_title = orig_title;
 	}
 	public String getYear() {
 		return year;
 	}
 	public void setYear(String year) {
 		this.year = year;
 	}
 	public String getPoster() {
 		return poster;
 	}
 	public void setPoster(String poster) {
 		this.poster = poster;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	public void setCachedPoster(byte[] cached_poster){
 		this.cached_poster = cached_poster;
 	}
 	
 	public byte[] setFromInetPoster(){
 		HttpEntity http_entity = this.getPosterHttpEntity();
 		try {
 			if (http_entity != null){
 				this.cached_poster = EntityUtils.toByteArray(http_entity);
 			}
 		} catch (IOException e) {
 			//error
			this.cached_poster = null;
		} catch (Exception e) { 
			//error
			this.cached_poster = null;
 		}
 		return this.cached_poster;
 	}
 	
 	public byte[] getCachedPoster(){
 		return this.cached_poster;
 	}
 	
 	public Bitmap getPosterImg(){
 		Bitmap bitmap = null;
 		if (this.getPoster() != ""){
 			byte[] img_bytes = this.getCachedPoster();
 			BitmapFactory.Options opts = new BitmapFactory.Options();
 			
 			if (img_bytes != null){
 				bitmap = BitmapFactory.decodeByteArray(img_bytes, 0, img_bytes.length, opts);
 			} else {
 			
 				try{
 					URL newurl = new URL(this.getPosterUrl()); 
 					bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream(), null, opts);
 				} catch (IOException e) {
					bitmap = null;
				} catch (Exception e) { 
					bitmap = null;
 				}
 			}
 		}
 		return bitmap;
 	}
 	
 	private String getPosterUrl(){
 		return "http://coocoorooza.com/uploads/afisha_films/" + this.getPoster();
 	}
 	
 	private HttpEntity getPosterHttpEntity(){
 		HttpEntity http_entry = null;
 		if (this.getPoster() != ""){
 			DefaultHttpClient mHttpClient = new DefaultHttpClient();
 			HttpGet mHttpGet = new HttpGet(this.getPosterUrl());
 			HttpResponse mHttpResponse;
 			try {
 				mHttpResponse = mHttpClient.execute(mHttpGet);
 				if (mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 					http_entry = mHttpResponse.getEntity();
 				}
 			} catch (IOException e) {
 				//e.printStackTrace();
				http_entry = null;
			} catch (Exception e) { 
				http_entry = null;
 			}
 		}
 		return http_entry;
 	}
 	
 	/* additional fields */
 	public String getZalTitle() {
 		return zal_title;
 	}
 	public void setZalTitle(String zal_title) {
 		this.zal_title = zal_title;
 	}
 	public String getTimes() {
 		return times;
 	}
 	public void setTimes(String times) {
 		this.times = times;
 	}
 	public String getPrices() {
 		return prices;
 	}
 	public void setPrices(String prices) {
 		this.prices = prices;
 	}
 }
