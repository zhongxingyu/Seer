 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 
 public class Data
 {
 	static class Box
 	{
 		public String name="noname";
 		public String desc="nodesc";
 		public float left,right,top,bottom;
 		private float x1,y1,x2,y2;
 		public Box(){}
 			
 		public boolean isValid()
 		{
 			if(left!=right && top!=bottom)
 				return true;
 			return false;
 		}
 		
 		public void movePixels(int dx, int dy)
 		{
 			if(ScreenLayoutTool.sceneView==null || ScreenLayoutTool.sceneView.img==null )
 				return;
 			
 			float fx = (float)dx/ScreenLayoutTool.sceneView.img.getWidth();
 			float fy = (float)dy/ScreenLayoutTool.sceneView.img.getHeight();
 			move(fx,fy);
 		}
 		
 		public void move(float dx, float dy)
 		{
 			left += dx;
 			right += dx;
 			top += dy;
 			bottom += dy;
 		}
 		
 		public void setFirstPixelPoint(int x, int y)
 		{
 			if(ScreenLayoutTool.sceneView==null || ScreenLayoutTool.sceneView.img==null )
 				return;
 			
 			float fx = (float)x/ScreenLayoutTool.sceneView.img.getWidth();
 			float fy = (float)y/ScreenLayoutTool.sceneView.img.getHeight();			
 			setFirstPoint(fx,fy);
 			sync();
 		}		
 		
 		public void setSecondPixelPoint(int x, int y)
 		{
 			if(ScreenLayoutTool.sceneView==null || ScreenLayoutTool.sceneView.img==null )
 				return;
 			
 			float fx = (float)x/ScreenLayoutTool.sceneView.img.getWidth();
 			float fy = (float)y/ScreenLayoutTool.sceneView.img.getHeight();			
 			setSecondPoint(fx,fy);
 			sync();	
 		}
 		
 		public void setFirstPoint(float x, float y)
 		{
 			x1 = x;
 			y1 = y;
 			x2 = x;
 			y2 = y;
 			sync();
 		}
 		
 		public void setSecondPoint(float x, float y)
 		{
 			x2 = x;
 			y2 = y;
 			sync();
 		}
 		
 		private void sync()
 		{
 			left = Math.min(x1, x2);
 			right = Math.max(x1, x2);
 			top = Math.min(y1, y2);
 			bottom = Math.max(y1, y2);			
 		}
 		
 	}
 	
 	public static ArrayList<Box> boxes = new ArrayList<Box>();
 	
 
 	
 	public static Box getBoxAtPixel(int x, int y)
 	{
 		if(ScreenLayoutTool.sceneView==null || ScreenLayoutTool.sceneView.img==null )
 			return null;
 		
 		float fx = (float)x/ScreenLayoutTool.sceneView.img.getWidth();
 		float fy = (float)y/ScreenLayoutTool.sceneView.img.getHeight();
 		return getBoxAt(fx,fy);
 	}
 	
 	public static Box getBoxAt(float x, float y)
 	{
 		for(int i=0; i<boxes.size(); ++i)
 		{
 			Box box = boxes.get(i);
 			
 			if(box.left <= x && x <= box.right
 				&& box.top <= y && y <= box.bottom)
 			{
 				return box;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	public static void removeBox(Box b)
 	{
 		boxes.remove(b);
 	}	
 	
 	public static void addBox(Box b)
 	{
 		boxes.add(b);
 	}	
 	
 	public static void clearAll()
 	{
 		boxes.clear();		
 	}
 	
 	public static void draw(Graphics g)
 	{
 		if(ScreenLayoutTool.sceneView.img == null)
 			return;
 		
 		g.setColor(boxColor);
 		
 		for(int i=0; i<boxes.size(); ++i)
 		{
 			Box box = boxes.get(i);
 			
 			int ptop = (int)(box.top * ScreenLayoutTool.sceneView.img.getHeight());
 			int pbottom = (int)(box.bottom * ScreenLayoutTool.sceneView.img.getHeight());
 			int pleft = (int)(box.left * ScreenLayoutTool.sceneView.img.getWidth());
 			int pright = (int)(box.right * ScreenLayoutTool.sceneView.img.getWidth());
 			
 			g.drawRect(pleft, ptop, pright-pleft, pbottom-ptop);
 		}
 		
 		g.setColor(selectedBoxColor);
 		
 		if(ScreenLayoutTool.sceneView==null || ScreenLayoutTool.sceneView.img==null )
 			return;
 		
 		Box box = ScreenLayoutTool.sceneView.selectedBox;
 		if(box!=null)
 		{
 			int ptop = (int)(box.top * ScreenLayoutTool.sceneView.img.getHeight());
 			int pbottom = (int)(box.bottom * ScreenLayoutTool.sceneView.img.getHeight());
 			int pleft = (int)(box.left * ScreenLayoutTool.sceneView.img.getWidth());
 			int pright = (int)(box.right * ScreenLayoutTool.sceneView.img.getWidth());
 			g.drawRect(pleft, ptop, pright-pleft, pbottom-ptop);
 		}
 	}
 	
 	
 	public static void loadFromFile(String filename)
 	{
 		clearAll();
 		
 		try
 		{
 			BufferedReader in = new BufferedReader(new FileReader(filename));
 			
 			while(true)
 			{
 				String line = in.readLine();
 				if(line==null)break;
 				StringTokenizer st = new StringTokenizer(line);
 				
 				Data.Box box = new Data.Box();
 				box.name = st.nextToken();
 				box.left = Float.parseFloat(st.nextToken());
 				box.right = Float.parseFloat(st.nextToken());
 				box.top = Float.parseFloat(st.nextToken());
 				box.bottom = Float.parseFloat(st.nextToken());
 				
 				if(st.hasMoreTokens())
					box.desc = st.nextToken("");
 				
 				Data.addBox(box);
 				
 			}
 			
 			in.close();
 		}
 		catch(Exception e)
 		{
 			System.out.println("LOAD ERROR!");
 			System.out.println(e);
 		}
 		
 		if(ScreenLayoutTool.sceneView!=null)
 			ScreenLayoutTool.sceneView.repaint();
 	}
 	
 	public static void saveToFile(String filename)
 	{
 		try
 		{
 			PrintWriter out = new PrintWriter(new FileWriter(filename));
 			
 			for(int i=0; i<boxes.size(); ++i)
 			{
 				Box box = boxes.get(i);
 				out.println(box.name+" "+box.left+" "+box.right+" "+box.top+" "+box.bottom+" "+box.desc);
 			}
 			
 			out.close();
 		}
 		catch(Exception e)
 		{
 			System.out.println("SAVE ERROR!");
 			System.out.println(e);
 		}
 	}
 	
 	static Color selectedBoxColor = new Color(0,255,0);
 	static Color boxColor = new Color(255,0,255);	
 }
