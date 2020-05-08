 import java.util.ArrayList;
 
 public class Author {
 	
 	/**
 	 * first name of the author
 	 */
 	private String nameFirst;
 	
 	/**
 	 * last name of the author
 	 */
 	private String nameLast;
 	
 	/**
 	 * publications published by this author
 	 */
 	private ArrayList<String> publishedPapers = new ArrayList<String>();
 	
 	public Author(String nameWhole) {
 		setName(nameWhole);
 	}
 	
 	public String toString() {
 		return getNameFirst() + " " + getNameLast();
 	}
 	
 	public void setName(String nameWhole) {
		setNameLast(nameWhole.split("\\, ")[1]);
		setNameFirst(nameWhole.split("\\, ")[0]);
 	}
 	
 	public ArrayList<String> getPublishedPapers()
 	{
 		return publishedPapers;
 	}
 	
 	public boolean addPublishedPaper(String paper)
 	{
 		publishedPapers.add(paper);
 		return true;
 	}
 
 	public String getNameFirst() {
 		return nameFirst;
 	}
 
 	public void setNameFirst(String nameFirst) {
 		this.nameFirst = nameFirst;
 	}
 
 	public String getNameLast() {
 		return nameLast;
 	}
 
 	public void setNameLast(String nameLast) {
 		this.nameLast = nameLast;
 	}
 }
