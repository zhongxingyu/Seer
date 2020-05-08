 package GUI;
 import GUI.Utils;
 import processing.core.PApplet;
 
 class Button extends PApplet {
 	 
 	  CircleButton circle1, circle2, circle3;
 	  float x, y, w, h;
 	  boolean locked = false;
	  int currentcolor;
	  
 	  int size;
 	  int basecolor, highlightcolor;
 	  boolean over = false;
 	  boolean pressed = false;   
 
 	  void update() 
 	  {
 	    if(over()) {
 	      currentcolor = highlightcolor;
 	    } 
 	    else {
 	      currentcolor = basecolor;
 	    }
 	  }
 
 	  boolean pressed() 
 	  {
 	    if(over) {
 	      locked = true;
 	      return true;
 	    } 
 	    else {
 	      locked = false;
 	      return false;
 	    }    
 	  }
 
 	  boolean over() 
 	  { 
 	    return true; 
 	  }
 
 	  boolean overCircle(float x, float y, int diameter) 
 	  {
 	    float disX = x - mouseX;
 	    float disY = y - mouseY;
 	    if(sqrt(sq(disX) + sq(disY)) < diameter/2 ) {
 	      return true;
 	    } 
 	    else {
 	      return false;
 	    }
 	  }
 	  
 	  Button(float x, float y, float w, float h) {
 	    this.x = x;
 	    this.y = y;
 	    this.w = w;
 	    this.h = h;
 	  } 
 	  
 	  boolean mouseOver() {
 	    return (Utils.globalProcessing.mouseX > x && Utils.globalProcessing.mouseX < x + w && Utils.globalProcessing.mouseY > y && Utils.globalProcessing.mouseY < y + h);
 	  }
 	  
 	  public void draw() {
 	    Utils.globalProcessing.stroke(80);
 	    Utils.globalProcessing.fill(mouseOver() ? 255 : 220);
 	    Utils.globalProcessing.rect(x,y,w,h); 
 	  }
 	  
 	}
 
 	class ZoomButton extends Button {
 	  
 	  boolean in = false;
 	  
 	  ZoomButton(float x, float y, float w, float h, boolean in) {
 	    super(x, y, w, h);
 	    this.in = in;
 	  }
 	  
 	  public void draw() {
 	    super.draw();
 	    Utils.globalProcessing.stroke(0);
 	    Utils.globalProcessing.line(x+3,y+h/2,x+w-3,y+h/2);
 	    if (in) {
 	    	Utils.globalProcessing.line(x+w/2,y+3,x+w/2,y+h-3);
 	    }
 	  }
 	  
 	}
 
 	class PanButton extends Button {
 	  
 	  int dir = UP;
 	  
 	  PanButton(float x, float y, float w, float h, int dir) {
 	    super(x, y, w, h);
 	    this.dir = dir;
 	  }
 	  
 	  public void draw() {
 	    super.draw();
 	    Utils.globalProcessing.stroke(0);
 	    switch(dir) {
 	      case UP:
 	        Utils.globalProcessing.line(x+w/2,y+3,x+w/2,y+h-3);
 	        Utils.globalProcessing.line(x-3+w/2,y+6,x+w/2,y+3);
 	        Utils.globalProcessing.line(x+3+w/2,y+6,x+w/2,y+3);
 	        break;
 	      case DOWN:
 	    	Utils.globalProcessing.line(x+w/2,y+3,x+w/2,y+h-3);
 	    	Utils.globalProcessing.line(x-3+w/2,y+h-6,x+w/2,y+h-3);
 	    	Utils.globalProcessing.line(x+3+w/2,y+h-6,x+w/2,y+h-3);
 	        break;
 	      case LEFT:
 	    	Utils.globalProcessing.line(x+3,y+h/2,x+w-3,y+h/2);
 	    	Utils.globalProcessing.line(x+3,y+h/2,x+6,y-3+h/2);
 	    	Utils.globalProcessing.line(x+3,y+h/2,x+6,y+3+h/2);
 	        break;
 	      case RIGHT:
 	    	Utils.globalProcessing.line(x+3,y+h/2,x+w-3,y+h/2);
 	    	Utils.globalProcessing.line(x+w-3,y+h/2,x+w-6,y-3+h/2);
 	    	Utils.globalProcessing.line(x+w-3,y+h/2,x+w-6,y+3+h/2);
 	        break;
 	    }
 	  }
 	
 	}
 	  
 	  class CircleButton extends Button
 	  { 
 		  int ihighlight = Utils.globalProcessing.color(128, 128, 0);
 	    CircleButton(float ix, float iy, int isize, int icolor, int ihighlight) 
 	    {
 	      super(ix, iy,isize,icolor);
 	      this.ihighlight = ihighlight;
 	    }
 
 	    boolean over() 
 	    {
 	      if( overCircle(x, y, size) ) {
 	        over = true;
 	        return true;
 	      } 
 	      else {
 	        over = false;
 	        return false;
 	      }
 	    }
 
 	    void display() 
 	    {
 	      stroke(255);
 	      fill(currentcolor);
 	      ellipse(x, y, size, size);
 	    }
 	  }
 	
