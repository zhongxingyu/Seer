 package de.splashfish.EvenLinux;
 
 import java.io.File;
 
 import de.splashfish.util.FileShift;
 
 public class ItemDefinition {
 
 	public static final int TRUE 	= 0;
 	public static final int FALSE 	= 1;
 	public static final int WAIT 	= 2;
 	
 	private FileShift 	shift;
 	private int			state;
 	private long		lastupdate;
 	
 	private String		error;
 	
 	public ItemDefinition(File from, File to) {
 		shift = new FileShift(from, to);
 		state = 0;
 		lastupdate = from.lastModified();
 	}
 	
 	public void setState(int i) {
 		this.state = i;
 	}
 	
 	public void setError(String error) {
 		this.error = error;
 		this.setState(FALSE);
 	}
 	
 	public String getError() {
 		return error;
 	}
 	
 	public void setUpdate(long update) {
 		lastupdate = update;
 	}
 	
 	public FileShift getShift() {
 		return shift;
 	}
 	
 	public int getState() {
 		return state;
 	}
 	
 	public long getUpdate() {
 		return lastupdate;
 	}
 	
 	public String getLessName() {
 		return shift.get(FileShift.FROM_FILE).getName();
 	}
 	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ItemDefinition) {
			ItemDefinition def = (ItemDefinition) obj;
			return this.state == def.state && this.shift.equals(def.shift);
		}
		return false;
	}
	
 }
