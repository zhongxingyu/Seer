 package viz;
 import java.util.*;
 
 public class Variable implements Drawable
 {
 	protected String name;
 	private int value;
 	private String color = "black";
 	private boolean isReference = false;
 	private boolean isParam = false;
 	protected ArrayList<String> ids;
 	private Queue<Integer> copiesToMake;
 	
 	private LinkedList<String> copiesOwned;
 	
 	private boolean hidden = false;
 	
 	private int xPos;
 	private int yPos;
 
 	protected int length = 0;
 	
 	private int copies = 1;
 	
 	private Variable ref =  null;
 	
 	public Variable(String name, int value, boolean isParam)
 	{
 		ids = new ArrayList<String>();
 		this.name = name;
 		this.value = value;
 		this.isReference = false;
 		this.isParam = isParam;
 		this.length = (name.length() * 10) + 80;
 		copiesToMake = new LinkedList<Integer>();
		copiesOwned = new LinkedList<String>();
 	}
 	
 	public Variable(String name, Variable ref, boolean isParam)
 	{
 		ids = new ArrayList<String>();
 		this.name = name;
 		this.isReference = true;
 
 		this.isParam = isParam;
 
 		setReference(ref);
 	}
 	
 	public boolean getHidden()
 	{
 		return hidden;
 	}
 	
 	public void setReference(Variable ref)
 	{
 		this.ref = ref;
 	}
 	
 	public void setPosition(int xPos, int yPos)
 	{
 		System.out.println("Setting position of " + name + " to " + xPos + "," + yPos);
 		this.xPos = xPos;
 		this.yPos = yPos;
 	}
 	
 	public void setHidden(boolean isHidden)
 	{
 		hidden = isHidden;
 	}
 
 	public void setIsParam(boolean isParam)
 	{
 		this.isParam = isParam;
 	}
 	public void setColor(String color)
 	{
 		this.color = color;
 	}
 	
 	public String getColor()
 	{
 		return color;
 	}
 	
 	public int getValue()
 	{
 		return value;
 	}
 	
 	public void setValue(int value)
 	{
 		this.value = value;
 	}
 	
 	public void addCopy()
 	{
 		copiesToMake.offer(new Integer(value));
 		//TODO: don't think this should be here
 		copies++;
 	}
 	
 	public String popCopyId()
 	{
 		return copiesOwned.pop();
 	}
 	
 	/**
 	 * Allows this variable to own the copy designated by id
 	 * @param id
 	 */
 	public void receiveCopyOwnership(String id)
 	{
 		copiesOwned.addFirst(id);
 	}
 	
 	public int getLength()
 	{
 		return this.length;
 	}
 	
 	public int getXPos()
 	{
 		return this.xPos;
 	}
 	
 	public int getYPos()
 	{
 		return this.yPos;
 	}
 
 	public boolean getIsParam()
 	{
 		return this.isParam;
 	}
 	
 	public ArrayList<String> getIds()
 	{
 		System.out.println("I have " + ids.size() + " ids");
 		return this.ids;
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 
 	public void draw(XAALScripter scripter)
 	{
 		
 		if (this.isReference)
 		{
 			String id1 = scripter.addTriangle(xPos, yPos, 40, color, hidden);
 			ids.add(id1);
 			if (ref != null)
 			{
 				String id2 = scripter.addArrow(id1, ref.getIds().get(0), 200, false, hidden);
 				ids.add(id2);
 				String id3 = scripter.addText(xPos, yPos-5, name, "black",  hidden);
 				ids.add(id3);
 			}
 
 		}
 		else
 		{
 			String id1 = scripter.addRectangle(xPos, yPos, length, 40, color,  hidden);
 			String id2 = scripter.addText(xPos, yPos-5, name, "black", hidden);
 			//String id3 = scripter.addText(xPos+15, yPos+25, value + "", "black",  hidden);
 			
 			ids.add(id1);
 			ids.add(id2);
 			//ids.add(id3);
 			
 			do 
 			{
 				Integer temp = copiesToMake.poll();
 				if (temp == null)
 					break;
 				
 				String newId = scripter.addText(xPos+15, yPos+25, temp.toString(), "black", hidden);
 				copiesOwned.offer(newId);
 				
 			} while(true);
 			/*
 			for (int i = 0; i < copies; i++)
 			{
 				ids.add(scripter.addText(xPos+15, yPos+25, value + "", "black", hidden));
 			}*/
 		}
 		
 	}
 	
 }
 
 
