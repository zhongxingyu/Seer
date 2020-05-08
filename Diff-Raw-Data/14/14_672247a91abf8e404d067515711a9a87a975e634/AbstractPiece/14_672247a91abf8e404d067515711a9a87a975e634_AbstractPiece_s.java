 package abstraction;
 
 public abstract class AbstractPiece<T>
 {
 	private int horizontal;
 	private int vertical;
	private T piece;
 	
	public AbstractPiece(T t, int i, int j)
 	{
		piece = (T) new Object();
 		this.horizontal = i;
 		this.vertical = i;
 	}
 		
 	/**
 	* @return the horizontal
 	*/
 	public int getHorizontal()
 	{
 		return this.horizontal;
 	}
 	
 	/**
 	 * 
 	 * @return the vertical 
 	 */
 	public int getVertical()
 	{
 		return this.vertical;
 	}
 }
