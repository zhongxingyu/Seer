 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 import com.avaje.ebean.*;
 
 /**
  * Version entity managed by Ebean
  */
 @Entity 
 @Table(name="version")
 public class Version extends Model {
 
 	@Column(name="Version_ID")
     @Id
 	@GeneratedValue
     public Long id;
     
 	@Column(name="Version_Name")
     @Constraints.Required
     public String name;
     
 	@Column(name="Platform_ID")
     @OneToOne
 	@Constraints.Required
 	@JoinColumn(name="Platform_ID")
     public Platform platform;
 	
     /**
      * Generic query helper for entity Computer with id Long
      */
     public static Finder<Long,Version> find = new Finder<Long,Version>(Long.class, Version.class); 
     
     /**
      * Return a page of version
      *
      * @param page Page to display
      * @param pageSize Number of computers per page
      * @param sortBy Computer property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the name column
      */
     public static Page<Version> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .fetch("platform")
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 
 	//Used for creating a list of all versions. Implemented in createForm.scala.html in @select
     public static Map<String,String> options() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(Version ver: Version.find.orderBy("name").findList()) {
			if(!options.containsValue(ver.name)){//if doesn't already exist...
				options.put(ver.id.toString(), ver.name);
			}
         }
         return options;
     }
 	
     /**
      * @return all versions.
      */
     public static List<Version> getList() {
     	return find.where().orderBy("name").findList();
     }
     /**
      * 
      * @return name of version.
      */
 	public String getVersionName() {
 		return name;
 	}
 	public Platform getThePlatform() {
 		return platform;
 	}
 	
 	/**
 	 * Returns the versionID of given version.name
 	 * @param versionName Which version name to find ID for
 	 */
 	public static Long getVersionID(String versionName, Platform platform){
 		Version version=find.where()
 			.eq("name",versionName)
 			.eq("platform",platform)
 			.findUnique();
 		if( version == null ){ //If no version was found... add it and return that id
 			Version newVersion = new Version();
 			newVersion.name=versionName;
 			newVersion.platform=platform;
 			newVersion.save();
 			return newVersion.id;
 		}
		System.out.println(version.name);
 		return version.id;
 
 	}
 	
     
 }
