 package rtkaczyk.eris.api;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public final class Packet implements Parcelable {
    public final String name;
    public final String address;
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
 
    public Packet(String name, String address, Long time, byte[] data) {
        this.name = name;
        this.address = address;
         this.time = time;
         this.data = data;
     }
 
     public Packet(Parcel in) {
        name = in.readString();
        address = in.readString();
         time = in.readLong();
         data = in.createByteArray();
     }
 
     @Override
     public void writeToParcel(Parcel out, int arg1) {
        out.writeString(name);
        out.writeString(address);
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
