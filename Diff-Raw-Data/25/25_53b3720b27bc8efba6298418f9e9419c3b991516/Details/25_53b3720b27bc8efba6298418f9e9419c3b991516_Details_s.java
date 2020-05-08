 /*******************************************************************************
  * Copyright (c) 2011 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package podsalinan;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * @author bugman
  *
  */
 public class Details {
 
 	private URL     url;
 	private String 	size;
 	private boolean added=false,		  // Has this podcast been added to the database
 	   			    remove=false, // Does this podcast need to be deleted from the system
 					updated=false;
 	private int	    status;  	  // This is used to track if the system has already downloaded the file.
 								  // 0 - not started
 								  // 1 - was previously started, but not yet handled by a downloader
 								  // 2 - currently downloading
 								  // 3 - finished
 	
 	public static final int NOT_STARTED=0,
 			 		 		PREVIOUSLY_STARTED=1,
 			 		 		CURRENTLY_DOWNLOADING=2,
 			 		 		FINISHED=3,
 			 		 		DO_NOT_DOWNLOAD=4;
 	
 	public Details (){
 		
 	}
 	
 	public Details (String url){
 		try {
 			this.url= new URL(url);
 		} catch (MalformedURLException e) {
 		}
 		size="0";
 	}
 	
 	public Details (URL url){
 		this.url = url;
 	}
 	
 	public Details(String url, String length){
 		this(url);
 		this.size=length;
 	}
 	
 	public Details(URL url, String length){
 		this(url);
 		this.size=length;
 	}
 	
 	public Details(String url, boolean added){
 		this(url);
 		this.added=added;
 	}
 	
 	public Details(URL url, boolean added){
 		this(url);
 		this.added=added;
 	}
 	
 	public URL getURL(){
 		return url;
 	}
 	
 	public void setURL(String url){
 		try {
 			this.url= new URL(url);
 		} catch (MalformedURLException e) {
 		}
 	}
 	
 	public void setURL(URL url){
 		this.url = url;
 	}
 	
 	public String getSize(){
 		return size;
 	}
 	
 	public void setSize(String size){
 		this.size=size;
 	}
 	
 	public boolean isAdded(){
 		return added;
 	}
 	
 	public void setAdded(boolean added){
 		this.added = added;
 	}
 	
 	public boolean isRemoved(){
 		return remove;
 	}
 	
 	public void setRemoved(boolean removed){
 		remove=removed;
 	}
 	
 	public int getStatus(){
 		return status;
 	}
 	
 	public void setStatus(int newStatus){
 		status=newStatus;
 		updated=true;
 		if (updated){
 			System.out.println("Updated URL: "+getURL().toString());
 		    System.out.println("Status: "+getStatus());
 		}
 	}
 
 	/**
 	 * @return the updated
 	 */
 	public boolean isUpdated() {
 		return updated;
 	}
 
 	/**
 	 * @param updated the updated to set
 	 */
 	public void setUpdated(boolean updated) {
 		this.updated = updated;
 	}
 }
