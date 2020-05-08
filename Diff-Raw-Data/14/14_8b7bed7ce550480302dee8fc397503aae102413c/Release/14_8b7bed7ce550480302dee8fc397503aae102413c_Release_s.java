 package alma.common.vo;
 
 import java.io.Serializable;
 
 
 /**
  * Class Release
  *
  */
 public class Release implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	public static int id;
 	public static String categorie = "SPORT";
 	
 	//TODO List of news
 	
 	/**
 	 * Constructor for Release
 	 */
 	public Release(int id){
 		this.id = id;
 	}
 	
 	/**
 	 * Add a news in the list
 	 * @param news
 	 */
	public void addNews(News news){
 	}
 }
