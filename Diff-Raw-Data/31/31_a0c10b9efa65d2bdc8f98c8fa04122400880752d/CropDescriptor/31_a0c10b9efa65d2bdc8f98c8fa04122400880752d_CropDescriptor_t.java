 package it.geosolutions.nrl.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name = "CropDescriptor")
 @Table(name = "cropdescriptor")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "cropdescriptor")
 @XmlRootElement(name = "CropDescriptor")
 public class CropDescriptor {
     @Id
 	String id;
     
     @Column(updatable=true,nullable=false)
 	String label;
     
     @Column(updatable=true,nullable=false)
 	String seasons; // ???
 	
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	public String getLabel() {
 		return label;
 	}
 	public void setLabel(String label) {
 		this.label = label;
 	}
 	public String[] getSeasons() {
	    if(seasons==null) return new String[0];
 	    if(seasons=="") return new String[0];
 		return seasons.split(",");
 	}
 	public void setSeasons(String seasons) {
	    if(seasons != null){
           String[] arr =seasons.split(",");
           setSeasons(arr);
           return;
	    }
	    this.seasons="";
 	}
 	
 	public void setSeasons(String[] seasons) {
 	    String[] arr = seasons;
         String res = "";
         for(int i = 0; i<arr.length;i++){
            if(Season.RABI.toString().equals(arr[i]) ||Season.KHARIF.toString().equals(arr[i])){
                res += arr[i];
                if(i!=arr.length-1){
                    res+=",";
                }
             }
         }
         this.seasons = res;
     }
 	
 	public void setSeasons(Season seasons) {
 		 String[] arr = seasons.toArray();
 		 String res = "";
 		 for(int i = 0; i<arr.length;i++){
 		     res += arr[i];
 		     if(i!=arr.length-1){
 		         res+=",";
 		     }
 		     
 		 }
 		 this.seasons = res;
 	}
 	
 }
