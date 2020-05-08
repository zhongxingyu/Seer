 package org.d1sturbed.ww;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.util.Log;
 
 public abstract class WWBaseHandler extends DefaultHandler implements Serializable {
 	/**
 	 * handler for parsing xml
 	 */
 	private static final long serialVersionUID = 4793473735545011541L;
 
  
 	private boolean interested;
 	private String data;
 	private char tempUnit='C';
 	private int temperature;
 	protected String pic;
 	protected ArrayList<WWForecast> wwf=new ArrayList<WWForecast>();
 	
 
 	private int low_temp=0;
 	private int high_temp=0;
 	public static final boolean DEBUG = WW.DEBUG;
 	public static final String TAG = "WWHandler";
 	
 	public abstract String getShortString();
 
 	
 	public void Cache(Context context, String icon) throws Exception {
 		File tmp=new File(icon);
		final float scale = context.getResources().getDisplayMetrics().density;
         File f = new File(context.getCacheDir(), tmp.getName());
         Bitmap b=null;
         if(f.exists()) {
         	return;
         }
         FileOutputStream out = new FileOutputStream(f);
 		String img=getPicUrl(icon);
 		URL u2 = new URL(img);
 
 		HttpURLConnection c2 = (HttpURLConnection) u2
 				.openConnection();
 		c2.setUseCaches(true);
 		c2.setDoInput(true);
 		c2.connect();
 		InputStream i2 = c2.getInputStream();
		b=BitmapFactory.decodeStream(i2);
		b=Bitmap.createScaledBitmap(b,  (int)(b.getWidth()*scale+.5f), (int)(b.getHeight()*scale+.5f),false);
 		b.compress(Bitmap.CompressFormat.PNG, 1, out);
 		c2.disconnect();
 		i2.close();
 	}
 
 
 	public Bitmap fromCache(Context context, String name) throws Exception {
 		File tmp=null;
 		File f =null;
 		tmp = new File(name);
 		File cacheDir = context.getCacheDir();
 		if(tmp!=null) {
 			f = new File(cacheDir, tmp.getName());
 		} 
 		debug("fromCache("+tmp.getName()+")");
 		return BitmapFactory.decodeStream(new FileInputStream(f));
 	}
 	
 	
 	protected void debug(String msg) {
 		if (DEBUG) {
 			Log.d(TAG, msg);
 		}
 	}
  
  
 	@Override
 	public void startDocument() throws SAXException {
 		interested = false;
 	}
  
 	@Override
 	public void endDocument() throws SAXException {
 
 	}
 	//make times comparable
 	public long time2minutes(String time)
 	{
 	    long minuts = 0;
 	    debug(time);
 	    String[] atime = time.split(" ");
 	    if (atime[1].toLowerCase().equals("pm")) {
 	    	debug("pm");
 	        minuts = 12 * 60;
 	    }
 	    String[] ttime = atime[0].split(":");
 	    minuts = minuts + Long.parseLong(ttime[0]) * 60 + Long.parseLong(ttime[1]);
 	    debug(""+minuts);
 	    return minuts;
 	}
 	@Override
 	public void startElement(String namespaceURI, String localName,String qName, Attributes atts) throws SAXException {
 
 	}
  
 	@Override
 	public void endElement(String namespaceURI, String localName, String qName)throws SAXException {
 
 	}
 
 	
 	public abstract String getDesc();
 
 	public boolean isDay() {
 		return false;
 	}
 
 	@Override
     public void characters(char ch[], int start, int length)throws SAXException {
 		String s=new String(ch, start, length);
 		if(interested) {
 			if(s!=null) {
 				setData(s);
 			}
 		}
 	}
 
 	public String getData() {
 		return data;
 	}
 
 	public void setData(String data) {
 		this.data += data;
 	}
 
 	public int getTemperature() {
 		return temperature;
 	}
 
 	public void setTemperature(int temperature) {
 		this.temperature = temperature;
 	}
 
 	public int getLow_temp() {
 		return low_temp;
 	}
 
 	public void setLow_temp(int low_temp) {
 		this.low_temp = low_temp;
 	}
 
 	public int getHigh_temp() {
 		return high_temp;
 	}
 
 	public void setHigh_temp(int high_temp) {
 		this.high_temp = high_temp;
 	}
 	
 	public String getPic() {
 		return this.pic;
 	}
 
 	public void setPic(String pic) {
 		this.pic = pic;
 	}
 
 	
 	public String toString() {
 		return getDesc()+":"+getPic()+"";
 		
 	}
 
 
 	public char getTempUnit() {
 		return tempUnit;
 	}
 
 
 	public void setTempUnit(char tempunit) {
 		this.tempUnit = tempunit;
 	}
 
 
 
 
 
 	abstract public URL getUrl(Location lo);
 
 
 	abstract public ArrayList<String> getForecastPics();
 	
 	public ArrayList<WWForecast> getWwf() {
 		return wwf;
 	}
 
 
 	public abstract String getPicUrl(String pic);
 
 
 	public void getImages(Context context) throws Exception {
 		/*URL u=new URL(getPicUrl(getPic()));
 		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
 		connection.setUseCaches(true);
 		connection.setDoInput(true);
 		connection.connect();
 		InputStream input = connection.getInputStream();
 		Bitmap b=BitmapFactory.decodeStream(input);
 		connection.disconnect();
 		input.close();*/
 		Cache(context, getPic());
 		debug(getPicUrl(getPic()));
 		for(int i=0;i<getWwf().size();i++) {
 			try {
 				 Cache(context, getWwf().get(i).getIcon());
 			} catch(Exception e) {
 				debug("Could not cache image"+getWwf().get(i).getIcon());
 			}
 		}
 		
 	}
 }
