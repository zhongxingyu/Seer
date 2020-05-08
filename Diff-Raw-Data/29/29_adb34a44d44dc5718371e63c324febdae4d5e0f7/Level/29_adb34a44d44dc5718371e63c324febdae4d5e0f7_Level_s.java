 package com.quizz.core.models;
 
 import java.util.ArrayList;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.google.gson.annotations.SerializedName;
 import com.quizz.core.db.QuizzDAO;
 
 public class Level implements Parcelable {
 
 	public static int STATUS_LEVEL_CLEAR = 1;
 	public static int STATUS_LEVEL_UNCLEAR = 0;
 
 	public static String LEVEL_EASY = "easy";
 	public static String LEVEL_MEDIUM = "medium";
 	public static String LEVEL_HARD = "hard";
 
 	public static int HINT_USED = 1;
 	public static int HINT_UNUSED = 0;
 
 	public static final Parcelable.Creator<Level> CREATOR = new Parcelable.Creator<Level>() {
 
 		public Level createFromParcel(Parcel parcel) {
 			return new Level(parcel);
 		}
 
 		public Level[] newArray(int size) {
 			return new Level[size];
 		}
 	};
 
 	@SerializedName("id")
 	public int id;
 
 	public String ref;
 	
 	@SerializedName("response")
 	public String response;
 
 	@SerializedName("partial_response")
 	public String partialResponse;
 
 	@SerializedName("indication")
 	public String indication;
 
 	@SerializedName("link")
 	public String moreInfosLink;
 
 	@SerializedName("image")
 	public String imageName;
 
 	public int status = STATUS_LEVEL_UNCLEAR;
 	public int sectionId = 0;
 	public float rotation = 0;
 	
 	public Level() {
 
 	}
 
 	/**
 	 * @param parcel
 	 */
 	public Level(Parcel parcel) {
 		this.id = parcel.readInt();
 		this.ref = parcel.readString();
 		this.response = parcel.readString();
 		this.partialResponse = parcel.readString();
 		this.indication = parcel.readString();
 		this.moreInfosLink = parcel.readString();
 		this.imageName = parcel.readString();
 		this.status = parcel.readInt();
 		this.sectionId = parcel.readInt();
 		this.rotation = parcel.readFloat();
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(this.id);
 		dest.writeString(this.ref);
 		dest.writeString(this.response);
 		dest.writeString(this.partialResponse);
 		dest.writeString(this.indication);
 		dest.writeString(this.moreInfosLink);
 		dest.writeString(this.imageName);
 		dest.writeInt(this.status);
 		dest.writeInt(this.sectionId);
 		dest.writeFloat(this.rotation);
 	}
 
 	public void update() {
 		QuizzDAO.INSTANCE.updateLevel(this);
 	}
 }
