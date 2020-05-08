 package models;
 
 import java.util.List;
 import org.joda.time.DateTime;
 import java.util.*;
 import javax.persistence.*;
 import play.db.jpa.*;
 import play.data.validation.*;
 
 /** 
  * Video Class
  * 
  * @author Cella Sum, Arudra Venkat, Dustin Overmiller, and Justin Kambic
  * 
  * This defines all requirements needed to implement the video model
  *
  */
  @Entity
 public class Video extends Model {
 	@Required
	private String vidId;
 	@Required
 	private Date uploadDate;
 	@Required
 	private String owner;
 	@Required
 	private String title;
 	@Required
 	private String description;
 	@Required
 	private String category;
 	@Required
 	@ManyToMany(cascade=CascadeType.PERSIST)
 	private List<VideoTag> tags;
 	@Required
 	private int likes;
 	@Required
 	private int views;
 	
 	//initial Video constructor for testing
 	public Video(Date upDate, String own, String vTitle, String desc)
 	{
 		uploadDate = upDate;
 		owner = own;
 		title = vTitle;
 		description = desc;
 		likes = 0;
 		views = 0;
 	}
 	
 	/**
 	 * Video(String ident, Date upDate, String own, String vTitle, String desc, String cat, List<VideoTag> tag, int numLike, int numView)
 	 * 
 	 * Constructor for the Video Class
 	 * 
 	 * @param ident
 	 * @param upDate
 	 * @param vidType
 	 * @param own
 	 * @param vTitle
 	 * @param desc
 	 * @param cat
 	 * @param tag
 	 * @param numLike
 	 * @param numView
 	 */
 	public Video(String ident, Date upDate, String own, String vTitle, String desc, String cat, List<VideoTag> tag, int numLike, int numView)
 	{
		vidId = ident;
 		uploadDate = upDate;
 		owner = own;
 		title = vTitle;
 		description = desc;
 		category = cat;
 		tags = tag;
 		likes = numLike;
 		views = numView;		
 	}
 	
 	/**
 	 * getID()
 	 * 
	 * Return the vidId associated with the video
 	 * 
 	 * @return
 	 */
 	public String getID()
 	{
		return vidId;
 	}
 	
 	/**
 	 * getUploadDate()
 	 * 
 	 * Return the upload date for the video
 	 * 
 	 * @return
 	 */
 	public Date getUploadDate()
 	{
 		return uploadDate;
 	}
 	
 	/**
 	 * setUploadDate(Date upDate)
 	 * 
 	 * Set the upload date for the video
 	 * 
 	 * @param upDate
 	 */
 	public void setUploadDate(Date upDate)
 	{
 		uploadDate = upDate;
 	}
 	
 	/**
 	 * getOwner()
 	 * 
 	 * Return the owner of the video
 	 * 
 	 * @return
 	 */
 	public String getOwner()
 	{
 		return owner;
 	}
 	
 	/**
 	 * setOwner(String Owner)
 	 * 
 	 * Set the Owner of the video
 	 * 
 	 * @param Owner
 	 */
 	public void setOwner(String Owner)
 	{
 		owner = Owner;
 	}
 	
 	/**
 	 * getTitle()
 	 * 
 	 * Get the Title of the video
 	 * 
 	 * @return
 	 */
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	/**
 	 * setTitle(String Title)
 	 * 
 	 * Set the title of the video
 	 * 
 	 * @param Title
 	 */
 	public void setTitle(String Title)
 	{
 		title = Title;
 	}
 	
 	/**
 	 * getCategory()
 	 * 
 	 * Get the Category of the video
 	 * 
 	 * @return
 	 */
 	public String getCategory()
 	{
 		return category;
 	}
 	
 	/**
 	 * setCateogry(String Cat)
 	 * 
 	 * Set the category of the video
 	 * 
 	 * @param Cat
 	 */
 	public void setCategory(String Cat)
 	{
 		category = Cat;
 	}
 	
 	/**
 	 * getDesc()
 	 * 
 	 * Get the Description for the video
 	 * 
 	 * @return
 	 */
 	public String getDesc()
 	{
 		return description;
 	}
 	
 	/**
 	 * setDesc(String desc)
 	 * 
 	 * Set the Description for the video
 	 * 
 	 * @param desc
 	 */
 	public void setDesc(String desc)
 	{
 		description = desc;
 	}
 	
 	/**
 	 * getTags()
 	 * 
 	 * Obtain the list of tags associated with the video
 	 * 
 	 * @return
 	 */
 	public List<VideoTag> getTags()
 	{
 		return tags;
 	}
 	
 	/**
 	 * setTags(List<VideoTag> tagList)
 	 * 
 	 * Set the list of tags associated with the video
 	 * 
 	 * @param tagList
 	 */
 	public void setTags(List<VideoTag> tagList)
 	{
 		tags = tagList;
 	}
 	
 	/**
 	 * getLikes()
 	 * 
 	 * Get the number of likes for the video
 	 * 
 	 * @return
 	 */
 	public int getLikes()
 	{
 		return likes;
 	}
 	
 	/**
 	 * setLikes(int like)
 	 * 
 	 * Set the number of likes for the video
 	 * 
 	 * @param like
 	 */
 	public void setLikes(int like)
 	{
 		likes = like;
 	}
 	
 	/**
 	 * getViews()
 	 * 
 	 * Get the number of views for the video
 	 * 
 	 * @return
 	 */
 	public int getViews()
 	{
 		return views;
 	}
 	
 	/**
 	 * setViews(int view)
 	 * 
 	 * Set the number of views for the video
 	 * 
 	 * @param view
 	 */
 	public void setViews(int view)
 	{
 		views = view;
 	}
 	
 }
