 package org.omships.omships;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 /**
  * Simple case for an item in an "RSS" feed.
  * @author jimtahu
  * Based on http://www.itcuties.com/android/how-to-write-android-rss-parser/
  */
 public class FeedItem implements Parcelable,Comparable<FeedItem>{
 	private String title;
 	private String link;
 	private Date pubDate;
 	private String description;
 	
 	final static SimpleDateFormat format =
 			new SimpleDateFormat("EEE, dd MMM yyyy k:m:s",Locale.US);
 	
 	public FeedItem(){
 		this.title = "None";
 		this.link = "http://www.google.com";
 		this.pubDate = new Date();
 		this.description = "No news is good news";
 	}
 
 	public FeedItem(Parcel in){
 		this.title=in.readString();
 		this.link=in.readString();
 		try {
 			this.pubDate=format.parse(in.readString());
 		} catch (ParseException e) {
 			this.pubDate = new Date();
 			e.printStackTrace();
 		}
 		this.description=in.readString();
 	}
 	
 	/**
 	 * Gets the resource to draw with.
 	 * Shoud be overiden with 
 	 * @return the drawable resource id to draw this item
 	 */
 	public int getImageResource(){
 		if(isImage()) return R.drawable.photos;
 		return R.drawable.rss_xml;
 	}
 	
 	/**
 	 * Checks if the linked item appears to be an image.
 	 * @return
 	 */
 	public boolean isImage(){
 		if(this.getLink().endsWith(".jpg"))return true;
 		else if(this.getLink().endsWith(".jpeg"))return true;
 		else if(this.getLink().endsWith(".png"))return true;
 		else return false;
 	}
 	
 	public boolean isVideo(){
 		if(this.getLink().contains("vimeo"))return true;
 		else return false;
 	}
 	
 	public boolean isWebPage(){
 		if(!isImage() && !isVideo()) return true;
 		else return false;
 	}
 	
 	
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getLink() {
 		return link;
 	}
 	public void setLink(String link) {
 		this.link = link;
 	}
 	public Date getPubDate() {
 		return pubDate;
 	}
 	public void setPubDate(Date pubDate) {
 		this.pubDate = pubDate;
 	}
 	public void setPubDate(String date){
 		try {
 			this.pubDate = format.parse(date);
 		} catch (ParseException ex) {
 			Log.e("RSS", "Date parsing failed on "+date,ex);
 		}
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	public String toString(){
 		if(this.isImage()) return "PHOTO: "+this.getDescription();
 		else return this.getDescription();
 	}
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeString(getTitle());
 		dest.writeString(getLink());
 		dest.writeString(format.format(getPubDate()));
 		dest.writeString(getDescription());
 	}
 	
 	public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
 		@Override
 		public FeedItem createFromParcel(Parcel source) {
 			return new FeedItem(source);
 		}
 		@Override
 		public FeedItem[] newArray(int size) {
 			return new FeedItem[size];
 		}
 	};
 
 	@Override
 	public int compareTo(FeedItem arg0) {
 		//Log.e("SORT", "Comparing "+this.getPubDate()+" to "+arg0.getPubDate());
		return -this.getPubDate().compareTo(arg0.getPubDate());
 	}
 
 }//end class RSSItem
