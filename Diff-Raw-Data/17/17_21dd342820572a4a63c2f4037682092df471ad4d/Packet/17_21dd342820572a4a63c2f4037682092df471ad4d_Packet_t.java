 package rtkaczyk.eris.api;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public final class Packet implements Parcelable {
    public final String device;
     public final Long time;
     public final byte[] data;
 
     public static final Parcelable.Creator<Packet> CREATOR = 
             new Parcelable.Creator<Packet>() {
 
                 public Packet createFromParcel(Parcel in) {
                     return new Packet(in);
                 }
         
                 public Packet[] newArray(int size) {
                     return new Packet[size];
                 }
             };
 
    public Packet(String device, Long time, byte[] data) {
        this.device = device;
         this.time = time;
         this.data = data;
     }
 
     public Packet(Parcel in) {
        device = in.readString();
         time = in.readLong();
         data = in.createByteArray();
     }
 
     @Override
     public void writeToParcel(Parcel out, int arg1) {
        out.writeString(device);
         out.writeLong(time);
         out.writeByteArray(data);
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
     
     @Override
     public String toString() {
         return new String(data);
     }
 
 }
