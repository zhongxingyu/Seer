 // Start of user code fr.eyal.datalib.sample.netflix.data.model.movieimage.MovieImage. DO NOT MODIFY THE GENERATED COMMENTS
 package fr.eyal.datalib.sample.netflix.data.model.movieimage;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import fr.eyal.lib.data.service.model.ComplexOptions;
 
 public class MovieImage extends MovieImageBase {
 
     private static final String TAG = MovieImage.class.getSimpleName();
 
     public MovieImage() {
         super();
     }
 
     public MovieImage(String fingerprint, ComplexOptions complexOptions) {
         super(fingerprint, complexOptions);
     }
     
     
     /**
      * PARCELABLE MANAGMENT
      */
 
 	public static final Parcelable.Creator<MovieImage> CREATOR = new Parcelable.Creator<MovieImage>() {
 	    @Override
 	    public MovieImage createFromParcel(final Parcel in) {
 	        return new MovieImage(in);
 	    }
 	
 	    @Override
 	    public MovieImage[] newArray(final int size) {
 	        return new MovieImage[size];
 	    }
 	};
 
 	@Override
 	public int describeContents() {
 	    return 0;
 	}
 
 	@Override
 	public void writeToParcel(final Parcel dest, final int flags) {
 		super.writeToParcel(dest, flags);
 	}
 
 	public MovieImage(final Parcel in) {
 		super(in);
	}    
 
    
 }
 // End of user code
