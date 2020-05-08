 /**
  * <p>
  * <u><b>Copyright Notice</b></u>
  * </p><p>
  * The copyright in this document is the property of 
  * Bath Institute of Medical Engineering.
  * </p><p>
  * Without the written consent of Bath Institute of Medical Engineering
  * given by Contract or otherwise the document must not be copied, reprinted or
  * reproduced in any material form, either wholly or in part, and the contents
  * of the document or any method or technique available there from, must not be
  * disclosed to any other person whomsoever.
  *  </p><p>
  *  <b><i>Copyright 2013-2014 Bath Institute of Medical Engineering.</i></b>
  * --------------------------------------------------------------------------
  *
  */
 package com.projectnocturne.datamodel;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 /**
  * {"response": {"request":"/users/register","status":"success","message": "User registered"}}
  * <p/>
  * if ignoreUnknown is false, Jackson would throw an exception if we don't parse all fields
  */
 @JsonIgnoreProperties(ignoreUnknown = true)
 public final class RESTResponseMsg implements Parcelable {
 
     public static final Parcelable.Creator<RESTResponseMsg> CREATOR = new Parcelable.Creator<RESTResponseMsg>() {
         @Override
         public RESTResponseMsg createFromParcel(final Parcel in) {
             return new RESTResponseMsg(in);
         }
 
         @Override
         public RESTResponseMsg[] newArray(final int size) {
             return new RESTResponseMsg[size];
         }
     };
 
     @JsonProperty("id")
     protected String id = "";
 
     @JsonProperty("content")
     protected String content = "";
 
     @JsonProperty("message")
     protected String message = "";
 
     @JsonProperty("request")
     protected String request = "";
 
     @JsonProperty("status")
     protected String status = "";
 
     public RESTResponseMsg() {
         super();
     }
 
     private RESTResponseMsg(final Parcel in) {
         id = in.readString();
         content = in.readString();
         request = in.readString();
         status = in.readString();
         message = in.readString();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see android.os.Parcelable#describeContents()
      */
     @Override
     public int describeContents() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     /**
      * @return the message
      */
     public String getMessage() {
         return message;
     }
    /**
     * @return the message
     */
    public String getContent() {
        return content;
    }
 
     /**
      * @param message the message to set
      */
     public void setMessage(final String message) {
         this.message = message;
     }
 
     /**
      * @return the status
      */
     public String getStatus() {
         return status;
     }
 
     /**
      * @param status the status to set
      */
     public void setStatus(final String status) {
         this.status = status;
     }
 
     @Override
     public String toString() {
 
         String tmpStr = "[id=\"" + id + "\"]";
         tmpStr += " [content=\"" + content + "\"]";
         tmpStr += " [message=\"" + message + "\"]";
         tmpStr += " [request=\"" + request + "\"]";
         tmpStr += " [status=\"" + status + "\"]";
         return tmpStr;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
      */
     @Override
     public void writeToParcel(final Parcel dest, final int flags) {
         dest.writeString(id);
         dest.writeString(content);
         dest.writeString(request);
         dest.writeString(status);
         dest.writeString(message);
     }
 }
