 package com.patrykkwiatkowski.simplenotepad;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class Note implements Parcelable {
 	private static String dateFormat = "yyyy-MM-dd kk:mm:ss";
 
 	private Date creationDate;
 	private String textContent;
 
 	public static final Parcelable.Creator<Note> CREATOR = new Creator<Note>() {
 		@Override
 		public Note createFromParcel(Parcel source) {
 			return new Note(source);
 		}
 
 		@Override
 		public Note[] newArray(int size) {
 			return new Note[size];
 		}
 	};
 
	private Date getCurrentDate() {
		try {
			/*
			 * Round down the time so we dismiss microsecons and stuff we dont
			 * care about. Otherwise we'll end up comparing same and yet
			 * different Notes.
			 */
			return new SimpleDateFormat(dateFormat).parse(new SimpleDateFormat(dateFormat)
					.format(new Date()));
		}
		catch (Exception e) {
			return null;
		}
	}

 	public Note() {
		creationDate = getCurrentDate();
 		textContent = "";
 	}
 
 	public Note(String text) {
		creationDate = getCurrentDate();
 		textContent = text;
 	}
 
 	public Note(String text, Date date) {
 		creationDate = date;
 		textContent = text;
 	}
 
 	protected Note(Parcel in) {
 		String date_str = in.readString();
 		try {
 			creationDate = new SimpleDateFormat(dateFormat).parse(date_str);
 		}
 		catch (Exception e) {
 		}
 
 		textContent = in.readString();
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public String getTextContent() {
 		return textContent;
 	}
 
 	public void setTextContent(String textContent) {
 		this.textContent = textContent;
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel out, int flags) {
 		out.writeString(new SimpleDateFormat(dateFormat).format(creationDate));
 		out.writeString(textContent);
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		return (o instanceof Note) && ((Note) o).creationDate.equals(this.creationDate);
 	}
 
 	public static boolean equals(Note a, Note b) {
 		return a != null && b != null && a.creationDate.equals(b.creationDate);
 	}
 }
