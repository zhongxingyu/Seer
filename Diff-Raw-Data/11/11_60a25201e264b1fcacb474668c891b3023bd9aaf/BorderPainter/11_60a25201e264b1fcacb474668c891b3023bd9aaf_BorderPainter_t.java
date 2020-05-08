 package html2windows.css;
 
 import html2windows.css.Style;
 import html2windows.dom.Element;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.util.HashMap;
 
 import javax.swing.JPanel;
 
 /**
  * Draw border by border painter according to some 
  * user defined property in style.
  *  
  * @author Jason Kuo
  */
 
 @SuppressWarnings("serial")
 public class BorderPainter extends JPanel implements CSSPainter{
 	
 	/**
 	 * graphic to draw on
 	 */
 	private Graphics2D g2d;
 	
 	/**
 	 * HashMap that contains properties.
 	 */
 	private HashMap<String, String> property = new HashMap<String, String>();
 	
 	/**
 	 * height, width, top, left values of border.
 	 */
 	private int height=10;
 	private int width=10;
 	private int top=10;
 	private int left=10;
 	
 	/**
 	 * Width of border
 	 */
 	private int borderWidth=2;
 
     public BorderPainter() { 
         initial();
     }
 
     /**
      * Initial property to prevent user from not defining
      * those property.
      */
     private void initial() {
     	property.put("border-width","2");
 		property.put("width","30");
 		property.put("height","30");
 		property.put("top","10");
 		property.put("left","5");
 		property.put("bottom","10");
 		property.put("border-style","solid");
 		property.put("border-color","black");
     }
 
     /**
      * Function to paint. First, get property from user defined style.
      * Then, set color, stroke and draw on graphic.
      *
      * @param style     user defined style property
      * @param element   element to be drawn
      * @param g         graphic to draw on
      */
     public void paint(Style style, Element element, Graphics g) {
         this.g2d = (Graphics2D) g;
         
         //get property we want from user defined property
         getBorderStyle(style,"width");
 		getBorderStyle(style,"height");
 		getBorderStyle(style,"top");
 		getBorderStyle(style,"left");
 		getBorderStyle(style,"border-width");
 		getBorderStyle(style,"border-style");
 		getBorderStyle(style,"border-color");
 		
 		//set basic property from user defined property
 		setBorderWidth();
 		setHeight();
 		setWidth();
 		setTop();
 		setLeft();
 		setColor();
 		
 		//set stroke from user defined property
 		if(setStroke()!=null)
 			g2d.setStroke(setStroke());
 		
 		//draw different type due to user defined border-style
 		if ("double".equals(property.get("border-style"))){
 			g2d.drawRect(top,left,width,height);
 			g2d.drawRect(top-borderWidth,left-borderWidth,width+2*borderWidth,height+2*borderWidth);
 		}
 		else{
 			g2d.drawRect(top,left,width,height);
 		}
     }
 
 
     /**
      * Get property we want from user defined style property  
      *
      * @param style     user defined style
      * @param name      property name to get
      */
     public void getBorderStyle(Style style,String name) {
         String type = style.getProperty(name);
         if( type != null ) {
             this.property.put(name, type) ;    
         }
     }
 
     /**
      * Set color by user defined property "border-color"     
      */
 	public void setColor(){
 
 		String color=property.get("border-color");
 
 		if(color.equals("maroon")){
 			g2d.setColor(new Color(128,0,0));
 		}
 		else if(color.equals("red")){
 			g2d.setColor(new Color(255,0,0));
 		}
 		else if(color.equals("orange")){
 			g2d.setColor(new Color(255,165,0));
 		}
 		else if(color.equals("yellow")){
 			g2d.setColor(new Color(255,255,0));
 		}
 		else if(color.equals("olive")){
 			g2d.setColor(new Color(128,128,0));
 		}
 		else if(color.equals("purple")){
 			g2d.setColor(new Color(128,0,128));
 		}
 		else if(color.equals("fuchsia")){
 			g2d.setColor(new Color(255,0,255));
 		}
 		else if(color.equals("white")){
 			g2d.setColor(new Color(255,255,255));
 		}
 		else if(color.equals("lime")){
 			g2d.setColor(new Color(0,255,255));
 		}
 		else if(color.equals("green")){
 			g2d.setColor(new Color(0,255,0));
 		}
 		else if(color.equals("navy")){
 			g2d.setColor(new Color(0,0,128));
 		}
 		else if(color.equals("blue")){
 			g2d.setColor(new Color(0,0,255));
 		}
 		else if(color.equals("aqua")){
 			g2d.setColor(new Color(0,255,255));
 		}
 		else if(color.equals("teal")){
 			g2d.setColor(new Color(0,128,128));
 		}
 		else if(color.equals("black")||color.equals("default")){
 			g2d.setColor(new Color(0,0,0));
 		}
 		else if(color.equals("silver")){
 			g2d.setColor(new Color(192,192,192));
 		}
 		else if(color.equals("gray")){
 			g2d.setColor(new Color(128,128,128));
 		}
 		else{
 			String firstColor=color.substring(1, 3);
 			int firstColorNum=Integer.parseInt(firstColor, 16);
 
 			String secondColor=color.substring(3,5);
 			int secondColorNum=Integer.parseInt(secondColor, 16);
 
 			String thirdColor=color.substring(5,7);
 			int thirdColorNum=Integer.parseInt(thirdColor, 16);
 
 			g2d.setColor(new Color(firstColorNum,secondColorNum,thirdColorNum));
 		}
 	}
 	
 	/**
 	 * Set border width according to user defined border-width
 	 * 
 	 * @throws NumberFormatException
 	 */
 	private void setBorderWidth(){
 		try{
 			borderWidth=Integer.parseInt(property.get("border-width"));
 	    }
 	    catch (NumberFormatException e){
 	    }
 	}
 	
 	/**
 	 * Set width according to user defined width
 	 * 
 	 * @throws NumberFormatException
 	 */
 	private void setWidth() throws NumberFormatException{
 		try{
 			width=Integer.parseInt(property.get("width"));
 	    }
 	    catch (NumberFormatException e){
 	    }
 	}
 	
 	/**
 	 * Set height according to user defined height
 	 * 
 	 * @throws NumberFormatException
 	 */
 	private void setHeight() throws NumberFormatException{
 		try{
 			height=Integer.parseInt(property.get("height"));
 	    }
 	    catch (NumberFormatException e){
 	    }
 	}
 	
 	/**
 	 * Set top according to user defined top
 	 * 
 	 * @throws NumberFormatException
 	 */
 	private void setTop() throws NumberFormatException{
 		try{
 			top=Integer.parseInt(property.get("top"));
 	    }
 	    catch (NumberFormatException e){
 	    }
 	}
 	
 	/**
 	 * Set left according to user defined left
 	 * 
 	 * @throws NumberFormatException
 	 */
 	private void setLeft() throws NumberFormatException{
 		try{
 			left=Integer.parseInt(property.get("left"));
 	    }
 	    catch (NumberFormatException e){
 	    }
 	}
     
 	/**
 	 * Set stroke by user defined property "border-style"
 	 * 
 	 * @return 		stroke style, return null once no matched pattern
 	 */
 	private Stroke setStroke(){
 		Stroke s;
		if ("dotted".equals(property.get("border-style"))) {
 			s = new BasicStroke(10, BasicStroke.CAP_ROUND,
 					BasicStroke.JOIN_ROUND, 5,
 					new float[] { 5 }, 10);
 			return s;
		} else if ("dashed".equals(property.get("border-style"))) {
 			s = new BasicStroke(borderWidth, BasicStroke.CAP_BUTT,
 					BasicStroke.JOIN_MITER, borderWidth,
 					new float[] { borderWidth*2,borderWidth }, 0f);
 			return s;
		} else if ("solid".equals(property.get("border-style"))) {
 			s = new BasicStroke(borderWidth);
 			return s;
 
		} else if ("double".equals(property.get("border-style"))) {
 			s = new BasicStroke(2);
 			return s;
 		}
 		return null;
 	}
 
 }
