 package fr.utc.assos.uvweb.data;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import fr.utc.assos.uvweb.util.DateUtils;
 import org.joda.time.DateTime;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Helper class for providing UV content for user interfaces
  */
 public class UVwebContent {
 	/**
	 * UV title format pattern, consistent accross the application.
 	 */
 	public static final String UV_TITLE_FORMAT = "<font color='#000000'>%1$s</font>%2$s";
 	public static final String UV_TITLE_FORMAT_LIGHT = "<font color='#ffffff'>%1$s</font>%2$s";
 	public static final String NEWSFEED_ACTION_FORMAT = "<b>%1$s</b><font color='#000000'>%2$s</font>";
 	public static final String UV_SUCCESS_RATE_FORMAT = "%1$s<b>%2$s</b>";
 	/**
 	 * Used to format grades.
 	 */
 	private static final DecimalFormat sDecimalFormat = new DecimalFormat("0");
 	/**
 	 * A map of UVs, by ID.
 	 */
 	public static Map<String, UV> UV_MAP = new HashMap<String, UV>();
 
 	public static void addItem(UV item) {
 		UV_MAP.put(item.getName(), item);
 	}
 
 	/**
 	 * A UV representing a piece of content.
 	 */
 	public static class UV implements Comparable<UV>, Parcelable {
 		public static final Parcelable.Creator<UV> CREATOR = new Parcelable.Creator<UV>() {
 			public UV createFromParcel(Parcel in) {
 				return new UV(in);
 			}
 
 			public UV[] newArray(int size) {
 				return new UV[size];
 			}
 		};
 		private String mName;
 		private String mDescription;
 		private double mRate;
 		private double mSuccessRate;
 
 		public UV(String name, String description, double rate, double successRate) {
 			mName = name;
 			mDescription = description;
 			mRate = rate;
 			mSuccessRate = successRate;
 		}
 
 		protected UV(Parcel in) {
 			mName = in.readString();
 			mDescription = in.readString();
 			mRate = in.readDouble();
 			mSuccessRate = in.readDouble();
 		}
 
 		@Override
 		public String toString() {
 			return mName;
 		}
 
 		public String getName() {
 			return mName;
 		}
 
 		public void setName(String name) {
 			mName = name;
 		}
 
 		public String getDescription() {
 			return mDescription;
 		}
 
 		public void setDescription(String description) {
 			mDescription = description;
 		}
 
 		public double getRate() {
 			return mRate;
 		}
 
 		public void setRate(double rate) {
 			mRate = rate;
 		}
 
 		public double getSuccessRate() {
 			return mSuccessRate;
 		}
 
 		public void setSuccessRate(double successRate) {
 			mSuccessRate = successRate;
 		}
 
 		public String getLetterCode() {
 			return mName.substring(0, 2);
 		}
 
 		public String getNumberCode() {
 			return mName.substring(2, 4);
 		}
 
 		public String getFormattedSuccessRate() {
 			return sDecimalFormat.format(mSuccessRate) + "%";
 		}
 
 		public String getFormattedRate() {
 			return sDecimalFormat.format(mRate) + "/10";
 		}
 
 		@Override
 		public int compareTo(UV uv) {
 			return mName.compareTo(uv.getName());
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel parcel, int flags) {
 			parcel.writeString(mName);
 			parcel.writeString(mDescription);
 			parcel.writeDouble(mRate);
 			parcel.writeDouble(mSuccessRate);
 		}
 	}
 
 	public static class UVComment implements Parcelable {
 		public static final Parcelable.Creator<UVComment> CREATOR = new Parcelable.Creator<UVComment>() {
 			public UVComment createFromParcel(Parcel in) {
 				return new UVComment(in);
 			}
 
 			public UVComment[] newArray(int size) {
 				return new UVComment[size];
 			}
 		};
 		private String mAuthor;
 		private DateTime mDate;
 		private String mComment;
 		private int mGlobalRate;
 		private String mSemester;
 
 		public UVComment(String author, DateTime date, String comment, int globalRate, String semester) {
 			mAuthor = author;
 			mDate = date;
 			mComment = comment;
 			mGlobalRate = globalRate;
 			mSemester = semester;
 		}
 
 		public UVComment(String author, String date, String comment, int globalRate, String semester) {
 			this(author, DateUtils.getDateFromString(date), comment, globalRate, semester);
 		}
 
 
 		protected UVComment(Parcel in) {
 			mAuthor = in.readString();
 			mDate = DateUtils.getDateFromString(in.readString());
 			mComment = in.readString();
 			mGlobalRate = in.readInt();
 			mSemester = in.readString();
 		}
 
 		public String getAuthor() {
 			return mAuthor;
 		}
 
 		public void setAuthor(String author) {
 			mAuthor = author;
 		}
 
 		public DateTime getDate() {
 			return mDate;
 		}
 
 		public void setDate(DateTime date) {
 			mDate = date;
 		}
 
 		public String getComment() {
 			return mComment;
 		}
 
 		public void setComment(String comment) {
 			mComment = comment;
 		}
 
 		public int getGlobalRate() {
 			return mGlobalRate;
 		}
 
 		public void setGlobalRate(int globalRate) {
 			mGlobalRate = globalRate;
 		}
 
 		public String getSemester() {
 			return mSemester;
 		}
 
 		public void setSemester(String semester) {
 			mSemester = semester;
 		}
 
 		public String getFormattedRate() {
 			return sDecimalFormat.format(mGlobalRate) + "/10";
 		}
 
 		public String getFormattedDate() {
 			return DateUtils.getFormattedDate(mDate);
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel parcel, int flags) {
 			parcel.writeString(mAuthor);
 			parcel.writeString(DateUtils.getFormattedDate(mDate));
 			parcel.writeString(mComment);
 			parcel.writeInt(mGlobalRate);
 			parcel.writeString(mSemester);
 		}
 	}
 
 	public static class NewsFeedEntry implements Parcelable {
 		public static final Parcelable.Creator<NewsFeedEntry> CREATOR = new Parcelable.Creator<NewsFeedEntry>() {
 			public NewsFeedEntry createFromParcel(Parcel in) {
 				return new NewsFeedEntry(in);
 			}
 
 			public NewsFeedEntry[] newArray(int size) {
 				return new NewsFeedEntry[size];
 			}
 		};
 		private String mAuthor;
 		private DateTime mDate;
 		private String mComment;
 		private String mAction;
 
 		public NewsFeedEntry(String author, DateTime date, String comment, String action) {
 			mAuthor = author;
 			mDate = date;
 			mComment = comment;
 			mAction = action;
 		}
 
 		public NewsFeedEntry(String author, String date, String comment, String action) {
 			this(author, DateUtils.getDateFromString(date), comment, action);
 		}
 
 		protected NewsFeedEntry(Parcel in) {
 			mAuthor = in.readString();
 			mDate = DateUtils.getDateFromString(in.readString());
 			mComment = in.readString();
 			mAction = in.readString();
 		}
 
 		public String getAuthor() {
 			return mAuthor;
 		}
 
 		public void setAuthor(String author) {
 			mAuthor = author;
 		}
 
 		public String getAction() {
 			return mAction;
 		}
 
 		public DateTime getDate() {
 			return mDate;
 		}
 
 		public void setDate(DateTime date) {
 			mDate = date;
 		}
 
 		public String getComment() {
 			return mComment;
 		}
 
 		public void setComment(String comment) {
 			mComment = comment;
 		}
 
 		public String getTimeDifference() {
 			return DateUtils.getFormattedTimeDifference(mDate, new DateTime());
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel parcel, int flags) {
 			parcel.writeString(mAuthor);
 			parcel.writeString(DateUtils.getFormattedDate(mDate));
 			parcel.writeString(mComment);
 			parcel.writeString(mAction);
 		}
 	}
 }
