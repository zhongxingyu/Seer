 // Start of user code fr.eyal.datalib.sample.netflix.data.model.top100.ItemTop100. DO NOT MODIFY THE GENERATED COMMENTS
 package fr.eyal.datalib.sample.netflix.data.model.top100;
 
 import java.io.File;
 import java.lang.ref.SoftReference;
 import java.util.Calendar;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Parcel;
 import android.os.Parcelable;
 import fr.eyal.datalib.sample.netflix.data.model.movieimage.MovieImage;
 import fr.eyal.datalib.sample.netflix.fragment.model.MovieItem;
 import fr.eyal.lib.util.FileManager;
 
 public class ItemTop100 extends ItemTop100Base implements MovieItem {
 
 	private static final String TAG = ItemTop100.class.getSimpleName();
 
     /**
      * Image's reference
      */
     public MovieImage image = null;
 
     /**
      * Image's url of the Top100 item
      */
     public String imageUrl = null;
 
     /**
      * Image's name of the Top100 item
      */
     public String imageName = null;
 
 
     public ItemTop100() {
         super();
         
     }
 
     public ItemTop100(final long id) {
         super(id);
     }
 
     @Override
     public Bitmap getPoster(boolean forceCache){
     	if(image == null || image.image == null)
     		return null;
     	
     	Bitmap result = image.image.get();
     	
     	if(result == null && forceCache){
     		
     		BitmapFactory.Options options = new BitmapFactory.Options();
     		
     		image.image = new SoftReference<Bitmap>(FileManager.getPictureFromFile(image.imagePath, options));
     		result = image.image.get();
     	}
     	
     	return result;
     }
 
     @Override
     public String getPosterPath(){
     	if(image == null)
     		return null;
 
     	return image.imagePath;
     }
     
     @Override
     public String getPosterName(){
     	if(image == null)
     		return null;
     	
     	//we assume the name is not supposed to change
     	if(imageName != null)
     		return imageName;
 
    	if(image.imagePath == null)
    		return null;
    	
     	String[] elements = image.imagePath.split(File.separator);
     	imageName = elements[elements.length-1];
     	return imageName;
     }
     
     
     @Override
     public String getImageUrl(){
     	
     	if(imageUrl == null){
     		String[] content = description.split("<img src=\"");
     		content = content[1].split("\"");
     		
     		String result = content[0];
     		result = result.replace("/small/", "/ghd/");
     		imageUrl = result;
     	}
     	return imageUrl;
     }
 
 	@Override
 	public String getLabel(int position) {
 		if(position >= 0)
 			return (position+1) + ". " + title;
 		else 
 			return title;
 	}
     
 	@Override
 	public void setImage(MovieImage newImage) {
 		image = newImage;
 	}
 
 	@Override
 	public MovieImage getImage() {
 		return image;
 	}
 	
 	@Override
 	public String getId() {
 		
 		String[] elements = link.split("/");
 		
 		if(elements != null && elements.length > 0)
 			return elements[elements.length-1]; //we return the last element on the link
 		
 		return null;
 	}    
 	
 	
 	
     /**
      * PARCELABLE MANAGMENT
      */
 
 	public static final Parcelable.Creator<ItemTop100> CREATOR = new Parcelable.Creator<ItemTop100>() {
 	    @Override
 	    public ItemTop100 createFromParcel(final Parcel in) {
 	        return new ItemTop100(in);
 	    }
 	
 	    @Override
 	    public ItemTop100[] newArray(final int size) {
 	        return new ItemTop100[size];
 	    }
 	};
 
 	@Override
 	public int describeContents() {
 	    return 0;
 	}
 
 	@Override
 	public void writeToParcel(final Parcel dest, final int flags) {
 		super.writeToParcel(dest, flags);
 
 		dest.writeParcelable(image, 0);
 		dest.writeString(imageUrl);
 		dest.writeString(imageName);		
 	}
 
 	public ItemTop100(final Parcel in) {
 		super(in);
 		
 		image = in.readParcelable(MovieImage.class.getClassLoader());
 		imageUrl = in.readString();
 		imageName = in.readString();
 		
 	}
 
 }
 // End of user code
