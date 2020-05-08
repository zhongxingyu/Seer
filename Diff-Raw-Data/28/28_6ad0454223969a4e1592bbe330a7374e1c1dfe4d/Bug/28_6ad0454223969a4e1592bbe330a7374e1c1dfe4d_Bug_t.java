 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 import com.avaje.ebean.*;
 
 /**
  * Bug entity managed by Ebean
  */
 @Entity
 @Table(name="bug") 
 public class Bug extends Model {
 
 	@Column(name="Bug_ID")
     @Id
 	@GeneratedValue
     public Long id;
     
 	@Column(name="Bug_Number")
     @Constraints.Required
     public Long number;
 	
 	@Column(name="Bug_Status_ID")
 	@OneToOne
 	@JoinColumn(name="Bug_Status_ID")
 	public BugStatus bugstatus;
 	
 	/**
      * Get file format of bug by going to bug->page->run->Format
      */
 	public FileFormat fileformat;
 	@Column
 	public static FileFormat getFileFormat( Bug bug ){
 		PageOut pageout = PageOut.pageFromBug(bug);
 		return pageout.run.format;
 	}
 	
 	/**
      * Get version of bug by going to bug->page->run->Version
      */
 	public static Version getVersion( Bug bug ){
 		PageOut pageout = PageOut.pageFromBug(bug);
 		return pageout.run.version;
 	}
 	
 	/**
      * Get Date of bug by going to bug->page->run->Date
      */
 	public static Date getDate( Bug bug ){
 		PageOut pageout = PageOut.pageFromBug(bug);
 		return pageout.run.date;
 	}
 	
 	//PageToBug
 	@ManyToMany
 	@JoinTable(
 		name="pagetobug",
 		joinColumns=@JoinColumn(name="Bug_ID", referencedColumnName="Bug_ID"),
 		inverseJoinColumns=@JoinColumn(name="Page_ID", referencedColumnName="Page_ID")
 	)
 	public Map<Bug,PageOut> pagesoutbug = new HashMap<Bug,PageOut>();
     
     /**
      * Generic query helper for entity Bug with id Long
      */
     public static Finder<Long,Bug> find = new Finder<Long,Bug>(Long.class, Bug.class); 
     
     /**
      * Return a page of Bug
      *
      * @param page Page to display
      * @param pageSize Number of Bugs per page
      * @param sortBy Bug property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the number column
      */
     public static Page<Bug> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("number", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .fetch("bugstatus")
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 	
     /**
 	 * Returns the bugerneceID of given bug and bugtype
 	 * @param bug Which bug name to find ID for
 	 * @return id of bug. Creates new one if no bug previously existed
 	 */
 	public static Long getBugID(Long bugNum){ //Used in AddToDB
 		Bug bug=find.where()
 			.eq("number",bugNum)
 			.findUnique();
 		if( bug == null ){ //If no bug was found... add it and return that id
 			Bug newBug = new Bug();
 			newBug.number=bugNum;
 			newBug.save();
 			return newBug.id;
 		}
 		return bug.id;
 	}
	
	/**
	 * This method calculates how many Bugs occured in given run
	 * @param run Which run to calculate
	 * @return Number of bugs in run
	 */
	public static int calculateBugs(Run run){
		Set<Bug> bugSet = find.where() //create a list of pagetobug with only pages from this run
			.eq("pagesoutbug.run.id", run.id) //make sure page is from run
			.findSet();
		return bugSet.size();
	}
 }
 
