 
 
 public class Array extends Variable implements Drawable {
 	
 	int[] values;
 	
 	public Array(String name, int[] values, boolean isParam)
 	{
 		super(name, 0, isParam);
 		this.values = values;
 		this.length = values.length*40;
 	}
 	
 	/**
 	 * 
 	 * @return a clone of the values.
 	 */
 	public int[] getValues()
 	{
 		return values.clone();
 	}
 	
 	public void setElem(int index, int value)
 	{
 		values[index] = value;
 	}
 	
 	public int arrayLength()
 	{
 		return values.length;
 	}
 	
 	@Override
 	public void draw(XAALScripter scripter) {
 		String label = scripter.addText(getXPos(), getYPos()-5, name, "black", getHidden());
 		ids.add(label);
 		
 		int arrayXPos = getXPos();
 		for (int i = 0; i < values.length; i++)
 		{
 			String rectangle = 
				scripter.addRectangle(arrayXPos + (i * 40), getYPos(), 40, 40, "black", getHidden());
 			String id = 
 				scripter.addText(arrayXPos + (i * 40) + 15, getYPos() + 25, values[i] + "", "black", getHidden());
 			
 			ids.add(rectangle);
 			ids.add(id);
 		}
 		
 	}
 
 }
