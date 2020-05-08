 
 package com.mili.xiaominglui.app.vello.data.model;
 
 import android.content.ContentValues;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
 
 public class WordCard implements Parcelable {
     public String idCard;
     public String name;
     public String desc;
     public String due;
     public String idList;
     public String closed;
     public String dateLastActivity;
 
     public WordCard() {
 
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeString(idCard);
         dest.writeString(name);
         dest.writeString(desc);
         dest.writeString(due);
         dest.writeString(idList);
         dest.writeString(closed);
         dest.writeString(dateLastActivity);
     }
 
     public ContentValues toContentVaalues() {
         ContentValues cv = new ContentValues();
         cv.put(DbWordCard.Columns.ID_CARD.getName(), idCard);
         cv.put(DbWordCard.Columns.NAME.getName(), name);
         cv.put(DbWordCard.Columns.DESC.getName(), desc);
         cv.put(DbWordCard.Columns.DUE.getName(), due);
         cv.put(DbWordCard.Columns.CLOSED.getName(), closed);
         cv.put(DbWordCard.Columns.ID_LIST.getName(), idList);
         cv.put(DbWordCard.Columns.DATE_LAST_ACTIVITY.getName(), dateLastActivity);
        cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "false");
         return cv;
     }
 }
