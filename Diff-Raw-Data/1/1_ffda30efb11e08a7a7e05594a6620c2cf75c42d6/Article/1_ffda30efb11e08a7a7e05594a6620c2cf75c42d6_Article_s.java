 /**
 * @Author Chris Card
 * This contains the interface for the message class
 */
 
 package Compute;
 
 import java.io.Serializable;
 
 public class Article implements Serializable
 {
 	private static final long serialVersionUID = 227L;
 
 	public final String content;
 	public final int id;
 	public final int parent;
 
 	/**
 	* Constructor
 	* @param msg message to store
 	* @param type the type of action to take when sent to the server, either send or receive
 	*/
 	public Article(String body, int parent)
 	{
 		this(body,-1,parent);
 	}
 
 	private Article(String body, int id, int parent)
 	{
 		content = body;
 		this.id = id;
 		this.parent = parent;
 	}
 
 	public Article setId(int id)	
 	{
 		return new Article(this.content,id,this.parent);
 	}
 }
