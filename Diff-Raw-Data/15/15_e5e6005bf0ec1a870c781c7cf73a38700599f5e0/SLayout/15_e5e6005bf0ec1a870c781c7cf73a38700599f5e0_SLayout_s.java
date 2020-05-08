 package com.room.scene;
 import java.util.ArrayList;
 
 import com.room.Global;
 
 public class SLayout
 {
 	static class Box
 	{
 		public String name=null;
 		public float left,right,top,bottom;
 		public Box(){}
 	}
 		
 	public void addBox(Box b)
 	{
 		boxes.add(b);
 	}		
 	
 	public String getBoxAtPixel(float x, float y)
 	{
 		float fx = x/Global.SCREEN_WIDTH;
 		float fy = y/Global.SCREEN_HEIGHT;
 		return getBoxAt(fx,fy);
 	}
 	
 	private String getBoxAt(float x, float y)
 	{
 		for(int i=0; i<boxes.size(); ++i)
 		{
 			Box box = boxes.get(i);
 			
 			if(box.left <= x && x <= box.right
 				&& box.top <= y && y <= box.bottom)
 			{
 				return box.name;
 			}
 		}
 		
 		return null;
 	}	
 	
	private static ArrayList<Box> boxes = new ArrayList<Box>();
 }
