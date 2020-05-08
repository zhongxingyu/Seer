 import java.util.ArrayList;
 
 public class Author {
 	
 	/**
 	 * name of the author
 	 */
 	private String name;
 	
 	/**
 	 * publications published by this author
 	 */
 	private ArrayList<String> publishedPapers = new ArrayList<String>();
	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
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
 }
