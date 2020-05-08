 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 import model.*;
 import javax.swing.JPanel;
 
 import shape.Shape;
 import shape.lines.AbstractLine;
 
 public class GridView extends JPanel{
 
 	private static final long serialVersionUID = 1L;
 	// ,    
 	private ArrayList<Excel> steplyArray;
 	//  
 	private ArrayList<Excel> coloredEx;
 	//  
 	private ClickedExController ClickedController;
 	// 
 	private int step;	
 	// 
 	double w;
 	// 
 	double h;
 	// 
 	private ArrayList<Shape> figures;
 
 	public GridView()
 	{
 		step = 1;
 		setOpaque(false);
 		setBackground(Color.WHITE);
 		Toolkit t = Toolkit.getDefaultToolkit();
 		Dimension d = new Dimension();
 		d = t.getScreenSize();
 		w = d.getWidth();
 	    h = d.getWidth();
 	    coloredEx = new ArrayList<Excel>();
 	    steplyArray = new ArrayList<Excel>();
 	    ClickedController = new ClickedExController();
 	    figures = new ArrayList<Shape>();
 	}
 		
 	  protected void paintComponent(Graphics g) {
 	        Graphics2D g2d = (Graphics2D)g;
 	        g2d.setColor(Color.white);
 	        
 	        Dimension d = new Dimension();
 	        d.height= (int) (h * step);	
 	        d.width= (int) (w* step);	
 	        this.setPreferredSize(d);				
 	        this.revalidate();
 	      // 	        
 	        int i = 0;
 	        int x1 = 0;
 	        int y1 = 0;
 	        int x2 = 0;
 	        int y2 = d.height;
 	        	
 	        while(i < w)
 	        {
 	        	g2d.drawLine(x1, y1, x2, y2);
 	        	x1 += step;
 	        	x2 +=step;
 	        	i++;
 	        	
 	        }
 	        // 
 	         x1 = 0;
 	         y1 = 0;
 	         x2 = d.width;
 	         y2 = 0;
 	         i = 0;
 	         while(i < h)
 	         {
 		        	g2d.drawLine(x1, y1, x2, y2);
 		        	y1 += step;
 		        	y2 +=step;
 		        	i++;
 	         }
 	         //  
 	         for(Excel ex: ClickedController.getClickedExes())
 	         {
 	        	 if (ex.isColored())
 	        	 {    		 
 	        		 int x[] = {ex.getX() * step + step,ex.getX() * step ,ex.getX() * step,ex.getX()* step + step};
 	        		 int y[] = {ex.getY()* step,ex.getY()* step,ex.getY()* step + step,ex.getY()* step + step};
 	        		 g2d.setColor(ex.getColor());
 	        		 g2d.fillPolygon(x,y,4);
 	        	 }
 	         }
 	         //   
 	         for(Excel ex: coloredEx)
 	         {
 	        	 if (ex.isColored())
 	        	 {    		 
 	        		 int x[] = {ex.getX() * step + step,ex.getX() * step ,ex.getX() * step,ex.getX()* step + step};
 	        		 int y[] = {ex.getY()* step,ex.getY()* step,ex.getY()* step + step,ex.getY()* step + step};
 	        		 g2d.setColor(ex.getColor());
 	        		 g2d.fillPolygon(x,y,4);
 	        	 }
 	         }
 	        //  
 	         for (Shape figure: figures)
 	         {
 	        	 ArrayList<Excel>colored = figure.getColoredExes();
 	        	 
 	        	 for(Excel ex: colored)
 		         {
 		        	 if (ex.isColored())
 		        	 {    		 
 		        		 int x[] = {ex.getX() * step + step,ex.getX() * step ,ex.getX() * step,ex.getX()* step + step};
 		        		 int y[] = {ex.getY()* step,ex.getY()* step,ex.getY()* step + step,ex.getY()* step + step};
 		        		 g2d.setColor(ex.getColor());
 		        		 g2d.fillPolygon(x,y,4);
 		        	 }
 		         }
 	         }
 
 	
 	    }
 
 	  //   
 	  public void setSteplyArray(AbstractLine line)
 	  {
 		  ArrayList<Excel> arr = line.getColoredExes();
 		  for (Excel ex: arr)
 		  {
 			  if (ex.getX() < 0 && ex.getY() < 0)
 				  arr.remove(ex);
 		  }
 		  steplyArray = arr;
 	  }
 	  //   
 	  public void clearStepArray()
 	  {
 		  steplyArray.clear();
 	  }
 	  
 	  //        
 	  public void drawDotSteply()
 	  {
 		  if (!steplyArray.isEmpty())
 		  {
 			 Excel ex = steplyArray.get(0);
 			 coloredEx.add(ex);
 			 steplyArray.remove(0);
 		  }
 	  }
 	  //  
 	  public void setStep(int s)
 	  {
 		  step = s;
 		  if (s < 1)
 			  step = 1;
 	  }
 	  // 
 	  public int getStep()
 	  {
 		  return step;
 	  }
 	  // true,       
 	  public boolean contains(Excel ex)
 	  {
 		  for (Excel e: coloredEx)
 		  {
 			  if (e.getX() == ex.getX() && e.getY() == ex.getY())
 				  return true;
 		  }
 		  
 		  for (Excel e: ClickedController.getClickedExes())
 		  {
 			  if (e.getX() == ex.getX() && e.getY() == ex.getY())
 				  return true;
 		  }
 		  
 		  return false;
 	  }
 	  //     ,     
 	  public void removeEx(Excel ex)
 	  {
 		  for(Excel e: coloredEx)
 		  {
 			  if (ex.getX() == e.getX() && ex.getY() == e.getY())
 				  {
 				  coloredEx.remove(e);
 				  break;
 				  }
 		  }
 		  
 		  ClickedController.removeExcel(ex);
 	  }
 	  //
 	  public void addEx(Excel ex)
 	  {
 		  if (ex.getX() >= 0 && ex.getY() >= 0)
 		  {
 			  ClickedController.addExcel(ex);
 		  }
 		  		 
 	  }
 	  
 	  public void clear()
 	  {
 		  coloredEx.clear();
 		  steplyArray.clear();
 		  ClickedController.clear();
 		  figures.clear();
 	  } 
 	  
 	  public Excel getClickedEx()
 	  {
 		 return ClickedController.getExcel();
 	  }
 	  
 	  public int getH()
 	  {
 		  return (int) h;
 	  }
 	  public int getW()
 	  {
 		  return (int) w;
 	  }
 	  
 	  public void moveLine(int x1, int y1, int x2, int y2)
 	  {
 		  Excel begin = new Excel(x1,y1);
 		  Excel end = new Excel(x2, y2);
 		  
 		  for(Shape figure: figures)
 		  {
 			  figure.move(begin, end);
 		  }
 	  }
 	  
 	  public void addFugure(Shape figure)
 	  {
 		  figures.add(figure);
 	  }
 	  //   
 	  public ArrayList<Excel> getAllClicked()
 	  {
 		  return ClickedController.getClickedExes();
 	  }
 	  
	  public ArrayList<Excel> popAllClicked()
	  {
		  return ClickedController.popAll();
	  }
	  
 
 }
