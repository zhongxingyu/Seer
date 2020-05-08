 
 package horizon.perscon.model;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class PrivacyMask implements Parcelable
 {
 	public static final int PRIV_DEVICE 		= 0;
 	public static final int PRIV_PERSON 		= 1;
 	public static final int PRIV_PLACE 			= 2;
 	public static final int PRIV_THING 			= 3;
 	public static final int PRIV_TIMESTAMP		= 4;
 	public static final int PRIV_USER 			= 5;
 
 	protected boolean mask [] = new boolean[6];
 	
 	public PrivacyMask()
 	{
 		
 	}
 
 	public boolean[] getMask()
 	{
 		return mask;
 	}
 
 	public void setMask(boolean[] mask)
 	{
 		this.mask = mask;
 	}
 	
 	public void setField(int index, boolean priv)
 	{
 		mask[index] = priv;
 	}
 	
 	public boolean getField(int index)
 	{
 		return mask[index];
 	}
 	
 	public void setAllPrivate()
 	{
 		for(int i=0; i<mask.length;i++)
 		{
 			mask[i]=false;
 		}
 	}
 	
 	public void setAllPublic()
 	{
 		for(int i=0; i<mask.length;i++)
 		{
 			mask[i]=true;
 		}		
 	}
 	
 	public boolean isAllPrivate()
 	{
 		for(int i=0; i<mask.length;i++)
 		{
 			if(mask[i])
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	
     private PrivacyMask(Parcel in)
     {
         readFromParcel(in);
     }
     
     public static final Parcelable.Creator<PrivacyMask> CREATOR = new Parcelable.Creator<PrivacyMask>()
     {
         public PrivacyMask createFromParcel(Parcel in) {
             return new PrivacyMask(in);
         }
 
         public PrivacyMask[] newArray(int size) {
             return new PrivacyMask[size];
         }
     };
     
 	public int describeContents()
 	{
 		return 0;
 	}
 	
 	public void writeToParcel(Parcel out, int flags)
 	{
 		out.writeBooleanArray(this.mask);
 	}
 
     public void readFromParcel(Parcel in)
     {
    	in.readBooleanArray(this.mask);
     }		
 }
