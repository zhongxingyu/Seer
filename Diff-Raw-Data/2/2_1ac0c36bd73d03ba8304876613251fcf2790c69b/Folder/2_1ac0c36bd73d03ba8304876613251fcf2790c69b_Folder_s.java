 package cabinet;
 
 import java.io.File;
 
 public class Folder
	extends Storage
 {
 
 	public Folder(File loc, String lbl)
 	{
 		super(loc, lbl);
 	}
 
 	@Override
 	public int compareTo(Storage other)
 	{
 		return this.getLabel().compareTo(other.getLabel());
 	}
 }
