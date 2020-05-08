 package cn.ingenic.updater;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class UpdateInfo implements Parcelable {
 	public String index;
 	public String version_from;
 	public String version_to;
 	public String description;
 	public String url;
 	public String size;
 	public String md5;
 	
 	private MyLog klilog = new MyLog(UpdateInfo.class);
 	
 	public UpdateInfo(){
 		
 	}
 	
 	private UpdateInfo(Parcel in){
 		index = in.readString();
 		version_from = in.readString();
 		version_to = in.readString();
 		description = in.readString();
 		url = in.readString();
 		size = in.readString();
 		md5 = in.readString();
 	}
 	
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 	
 	@Override
 	public void writeToParcel(Parcel out, int arg1) {
 		out.writeString(index);
 		out.writeString(version_from);
 		out.writeString(version_to);
 		out.writeString(description);
 		out.writeString(url);
 		out.writeString(size);
 		out.writeString(md5);
 	}
 	
 	public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>(){
 
 		@Override
 		public UpdateInfo createFromParcel(Parcel in) {
 			return new UpdateInfo(in);
 		}
 
 		@Override
 		public UpdateInfo[] newArray(int size) {
 			return new UpdateInfo[size];
 		}
 		
 	};
 	
 	public String toString(){
 		StringBuilder builder = new StringBuilder();
 		builder.append(index+";");
 		builder.append(version_from+";");
 		builder.append(version_to+";");
 		builder.append(description+";");
 		builder.append(url+";");
 		builder.append(size+";");
 		builder.append(md5+";");
 		
 		return builder.toString();
 	}
 	
	public static UpdateInfo createFromString(String s){
 		UpdateInfo info = new UpdateInfo();
		String [] values = s.split(";");
 		int i = 0;
 			info.index = values[i++];
 			info.version_from = values[i++];
 			info.version_to = values[i++];
 			info.description = values[i++];
 			info.url = values[i++];
 			info.size = values[i++];
 			info.md5 = values[i++];
 		return info;
 	}
 
 	public void dump(){
 		klilog.i("=========UpdateInfo===========");
 		klilog.i("index			:"+index);
 		klilog.i("version_from	:"+version_from);
 		klilog.i("version_to	:"+version_to);
 		klilog.i("description	:"+description);
 		klilog.i("url			:"+url);
 		klilog.i("size			:"+size);
 		klilog.i("md5			:"+md5);
 	}
 }
