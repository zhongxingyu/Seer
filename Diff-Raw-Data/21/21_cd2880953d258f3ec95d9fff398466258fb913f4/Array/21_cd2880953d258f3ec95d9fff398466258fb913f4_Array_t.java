 package viz;
 
 import java.util.*;
 
 public class Array extends Variable implements Drawable {
 	
 	private ArrayList<Integer> values  = new ArrayList<Integer>();
 	private ArrayList<Queue<Integer>> arrayCopiesToMake;
 	
 	private ArrayList<LinkedList<String>> arrayCopiesOwned;
 	private ArrayList<Integer> xPositions;
 	private ArrayList<String> indexRects;
 	
 	public Array(String name, ArrayList<Integer> values, boolean isParam)
 	{
 		super(name, 0, isParam);
 		this.values = values;
 		this.length = values.size()*40;
 		
 		arrayCopiesToMake = new ArrayList<Queue<Integer>>();
 		arrayCopiesOwned = new ArrayList<LinkedList<String>>();
 		
 		for (int i = 0; i < values.size(); i++)
 		{
 			arrayCopiesToMake.add(new LinkedList<Integer>());
 			arrayCopiesOwned.add(new LinkedList<String>());
 		}
 		
 		xPositions = new ArrayList<Integer>();
 		
 		indexRects = new ArrayList<String>();
 	}
 	
 	/**
 	 * 
 	 * @return a clone of the values.
 	 */
 	public ArrayList<Integer> getValues()
 	{
 		return values;
 	}
 	
 	public int getValue (int index)
 	{
 		return values.get(index).intValue();
 	}
 	 
 	public void setElem(int index, int value)
 	{
 		values.set(index, value);
 	}
 	
 	public int arrayLength()
 	{
 		return values.size();
 	}
 	
 	public void addCopy(int index)
 	{
 		arrayCopiesToMake.get(index).offer(new Integer(values.get(index)));
 	}
 	
 	public String popCopyId(int index)
 	{
 		//System.out.println("SSSSS");
 		for (String s : arrayCopiesOwned.get(index))
 		{
 		//	System.out.println(s);
 		}
 		return arrayCopiesOwned.get(index).pop();
 	}
 	
 	public String peekCopyId(int index)
 	{
 		return arrayCopiesOwned.get(index).peek();
 	}
 	
 	public String getCopyId(int index)
 	{
 		return arrayCopiesOwned.get(index).getFirst();
 	}
 	
 	/**
 	 * Allows this variable to own the copy designated by id
 	 * @param id
 	 */
 	public void receiveCopyOwnership(String id, int index)
 	{
 		arrayCopiesOwned.get(index).addFirst(id);
 	}
 	
 	public int getXPos(int index)
 	{
 		return xPositions.get(index).intValue();
 	}
 	
 	public String getRect(int index)
 	{
 		return indexRects.get(index);
 	}
 	
 	@Override
 	public void draw(XAALScripter scripter) {
 		String label = scripter.addText(getXPos(), getYPos()-5, name, "black", getHidden());
 		ids.add(label);
 		//System.out.println(label + " .....");
 		//System.out.println("Draw!!!!!!!!!!!!");
 		for (int i = 0; i < arrayCopiesToMake.size(); i++)
 		{
 		//	System.out.println(arrayCopiesToMake.get(i).peek());
 		}
 		
 		int arrayXPos = getXPos();
 		for (int i = 0; i < values.size(); i++)
 		{
 			int indexXPos = arrayXPos + (i * 40);
 			xPositions.add(new Integer(indexXPos));
 			
 			String rectangle = 
 				scripter.addRectangle(indexXPos, getYPos(), 40, 40, getColor(), getHidden());
 			indexRects.add(rectangle);
 		
 			String id = 
 				scripter.addText(indexXPos + 15, getYPos() + 25, values.get(i) + "", "black", true);
 			//System.out.println(rectangle);
 			//System.out.println(id);
 			ids.add(rectangle);
			
 			
 			do 
 			{
 				Integer temp = arrayCopiesToMake.get(i).poll();
 				if (temp == null)
 					break;
 				//test it always hidden
 				String newId = scripter.addText(indexXPos+15, yPos+25, temp.toString(), "black", true);			//System.out.println("New value: " + temp);
 				arrayCopiesOwned.get(i).offer(newId);
 				
 			} while(true);
 		}
 		
 	}
 
 }
