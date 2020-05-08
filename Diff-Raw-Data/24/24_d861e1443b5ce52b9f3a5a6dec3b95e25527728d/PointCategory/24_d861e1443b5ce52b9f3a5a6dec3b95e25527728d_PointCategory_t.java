 public abstract class PointCategory extends PointCollection
 {
 	protected static int lastIndex = 0;
 	protected int index = 0;
 	
 	/**
 	 * @pre index >= 0
 	 * @param index
 	 */
 	public PointCategory()
 	{
		this(true);
 	}
 
 	public PointCategory(boolean inc)
 	{
 		if(inc)
 			this.index = ++lastIndex;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return Integer.toString(this.index);
 	}
 	
 	@Override
 	public boolean add(Point point)
 	{
 		assert point.getPointCategory() == this;
 		return super.add(point);
 	}
 }
