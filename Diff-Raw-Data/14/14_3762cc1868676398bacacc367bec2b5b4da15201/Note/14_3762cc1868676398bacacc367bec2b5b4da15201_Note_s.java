 package edu.bgsu.notebook;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Note class.
  * @author Josh LaRoe
  * @since 04/03/2012
  */
 
 public class Note
 {
 	private static int numberOfNotes = 0;
 	private int id;
 	private String title;
 	private String comments;
 	private NotebookColors color;
 	private List<Category> categories;
 
 	/**
 	 *  Constructor for new note with categories.
 	 */
 	public Note(String title, String comments, NotebookColors color, List<Category> categories) 
 	{
 		this.id = ++numberOfNotes;
 		this.title = title != null ? title : "";
 		this.comments = comments != null ? comments : "";
 		this.categories = categories != null ? categories : new ArrayList<Category>();
 		this.color = color != null ? color : NotebookColors.BLACK;
 	}
 
 	public void removeCategory(Category category) 
 	{
 		categories.remove(category);   
 	}
 
	public void addCategory(Category category) 
 	{     
 		categories.add(category);
 	}
 
 	public void addCategories(List<Category> categories) 
 	{
 		this.categories.addAll(categories);
 	}
 
	public List<Category> getCategory() 
 	{
 		return categories;
 	}
 
 	public void setTitle(String _title) 
 	{
 		title = _title;
 	}
 
 	public void setComments(String _comments) 
 	{
 		comments = _comments;
 	}
 
 	public String getTitle() 
 	{
 		return title;
 	}
 
 	public String getComments() 
 	{
 		return comments;
 	}
 
	public void setNotebookColors(NotebookColors _NotebookColors) 
 	{
		color = _NotebookColors;
 	}
 
	public NotebookColors getNotebookColors() 
 	{
 		return color;
 	}
 	
 	public int getID()
 	{
 		return id;
 	}
 }
