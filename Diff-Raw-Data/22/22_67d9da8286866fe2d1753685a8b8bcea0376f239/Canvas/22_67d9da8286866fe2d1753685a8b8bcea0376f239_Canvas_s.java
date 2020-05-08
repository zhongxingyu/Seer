 package frontend;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JLabel;
 
 import backend.environment.Element;
 
 import math.Vec;
 
 class Canvas extends JLabel implements MouseListener, MouseMotionListener, MouseWheelListener	{
 	UserInterface ui;
 	Vec origin = new Vec();	//the origin relative to the center of the Canvas
 	Vec mPoint = new Vec();	//the position of the mouse in labelSpace
 	
 	int dotDiff = 10;
 	HeightMapCache hmc = null;
 	double zoom = 1;
 	
 	public boolean renderGrid = true, renderAxes = true, renderHeightMap = true;
 	
 	public Canvas(UserInterface ui)	{
 		this.ui = ui;
 		origin = new Vec();
 		hmc = new HeightMapCache(this, ui.sim.hm);
 		
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addMouseWheelListener(this);
 	}
 	
 	//public Vec toLabelSpace(Vec v)	{	return v.invertY().plus(origin).plus(new Vec(getSize().width/2, getSize().height/2));	}
 	public Vec toLabelSpace(Vec v)	{	return v.mult(zoom).invertY().plus(origin.mult(zoom)).plus(new Vec(getSize().width/2, getSize().height/2));	}
 	//public Vec toWorldSpace(Vec v)	{	return v.minus(originInLabelSpace()).invertY();	}
 	public Vec toWorldSpace(Vec v)	{	return v.minus(originInLabelSpace()).mult(1/zoom).invertY();	}
 	public Vec originInLabelSpace()	{	return toLabelSpace(Vec.ZERO);	}
 	public Vec mouseInWorldSpace()	{	return toWorldSpace(mPoint);	}
 	
 	public void paint(Graphics g)	{
 		Graphics2D g2 = (Graphics2D)g;
 		//System.out.println(hmc.completion);
 		if(hmc.completion >= 1)	{
 			Point o = originInLabelSpace().getPoint();
 			
 			g2.setColor(Color.black);
 			g2.fillRect(0, 0, getSize().width, getSize().height);
 			
 			//calculate the drawing range
 			double dotDiffZoomed = Math.max(dotDiff*zoom, 2);
 			double dotXStart = (o.x - dotDiffZoomed*Math.floor(o.x/dotDiffZoomed) - dotDiffZoomed),
 				dotYStart = (o.y - dotDiffZoomed*Math.floor(o.y/dotDiffZoomed) - dotDiffZoomed),
 				dotXEnd   = (o.x + dotDiffZoomed*Math.floor((getSize().width-o.x)/dotDiffZoomed) + 2*dotDiffZoomed),
 				dotYEnd   = (o.y + dotDiffZoomed*Math.floor((getSize().height-o.y)/dotDiffZoomed) + 2*dotDiffZoomed);
 			
 			//render the heightMap
 			if (renderHeightMap)
 			{
 				BufferedImage img = hmc.getImage();
 				Rectangle2D.Double bounds = hmc.hm.getRenderBounds();
 				Point tl = toLabelSpace(new Vec(bounds.x, bounds.y)).getPoint(),
 					  br = toLabelSpace(new Vec(bounds.x+bounds.width, bounds.y-bounds.height)).getPoint();
 				g2.drawImage(hmc.getImage(), (int)tl.x, (int)tl.y, (int)br.x, (int)br.y, 0, 0, (int)(hmc.width), (int)(hmc.height), null);
 				/*Point clampedTl = new Point(Math.max(tl.x, 0), Math.max(tl.y, 0)),
 					  clampedBr = new Point(Math.min(br.x, getSize().width), Math.min(br.y, getSize().height));
 				g2.drawImage(img, clampedTl.x, clampedTl.y, clampedBr.x, clampedBr.y,
 								  clampedTl.x-tl.x, clampedTl.y-tl.y, (int)hmc.width-(br.x-clampedBr.x), (int)hmc.height-(br.y-clampedBr.y), null);*/
 			}	
 			//draw elements
 			g2.setColor(Color.blue);
 			for(Element e: ui.sim.elements)	{
 				if (ui.selection.contains(e))
 					g2.setColor(Color.green);	
 				int size = (int)(e.getSize()*zoom);
 				Point pos = toLabelSpace(e.getPosition()).getPoint();
				g2.fillArc(pos.x-size/2, pos.y-size/2, size, size, 0, 360);
 				if (ui.selection.contains(e))
 					g2.setColor(Color.blue);	
 				/*size = (int)(e.getSightRadius()*zoom);
 				g2.setColor(Color.red);
 				g2.drawArc(pos.x-size/2, pos.y-size/2, size, size, 0, 360);*/
 			}
 			
 			//draw the grid
 			if(renderGrid)	{
 				if(dotDiffZoomed < 5)	dotDiffZoomed *= 2;
 				g2.setColor(Color.white);
 				for(double y = dotYStart; y <= dotYEnd; y += dotDiffZoomed)
 					for(double x = dotXStart; x <= dotXEnd; x += dotDiffZoomed)
 						g2.fillRect((int)x, (int)y, 1, 1);
 			}
 			
 			//draw the axes
 			if(renderAxes)	{
 				g2.setColor(Color.white);
 				g2.fillRect(0, o.y, getSize().width, 1);
 				g2.fillRect(o.x, 0, 1, getSize().height);
 			}
 		}
 		else	{
 			int width = getSize().width/3,
 				height = 50;
 			int x = (getSize().width-width)/2,
 				y = (getSize().height-height)/2;
 			
 			g2.setColor(Color.green);
 			g2.fillRect(x,y,(int)(width*hmc.completion),height);
 			
 			g2.setColor(Color.black);
 			g2.drawRect(x,y,width,height);
 		}
 	}
 	
 	public void mousePressed(MouseEvent me)	{
 		if(hmc.completion >= 1)
 		{
 			mPoint = new Vec(me.getPoint());
 			
 		}
 	}
 	
 	public void mouseDragged(MouseEvent me)	{
 		if(hmc.completion >= 1)	{
 			Vec mp = new Vec(me.getPoint());
 			origin = origin.minus(mPoint.minus(mp).mult(1/zoom));
 			mPoint = mp;
 			repaint();
 		}
 	}
 	
 	public void mouseReleased(MouseEvent me) {}
 	public void mouseClicked(MouseEvent me) {
 		switch(me.getModifiers()) {
 	      case InputEvent.BUTTON1_MASK: {
 	    	  System.out.println("That's the LEFT button");
 		        //select prey
 	    	  	if (me.isControlDown())
 	    	  		ui.addToSelection(toWorldSpace(mPoint));
 	    	  	else
 	    	  		ui.selectPrey(toWorldSpace(mPoint));
 		        break;
 		  }
 	      case InputEvent.BUTTON2_MASK: {
 	    	  System.out.println("That's the MIDDLE button");     
 		      break;
 		  }
 	      case InputEvent.BUTTON3_MASK: {
 	    	  System.out.println("That's the RIGHT button");     
 	    	  //set direction for hashSet
 	    	  if (ui.selection.isEmpty())
 	    		  ui.placePrey(toWorldSpace(mPoint));
 	    	  else	
 	    		  ui.setPreyDirection(toWorldSpace(mPoint));
 	    	  break;
 	      }
 	   }
 	}
 	public void mouseEntered(MouseEvent me) {}
 	public void mouseExited(MouseEvent me) {}
 	public void mouseMoved(MouseEvent me)	{
 		if(hmc.completion >= 1)
 			ui.status.setMousePoint(toWorldSpace(new Vec(me.getPoint())));
 	}
 	public void mouseWheelMoved(MouseWheelEvent mwe)	{
 		if(hmc.completion >= 1) {
 			Vec m1 = toWorldSpace(new Vec(mwe.getPoint())); //mouse point in worldSpace before zoom
 			zoom -= mwe.getWheelRotation()*(0.01+ (zoom-0.1)/9.99);
 			zoom = Math.min(Math.max(0.01, zoom), 10);
 			Vec m2 = toWorldSpace(new Vec(mwe.getPoint())); //mouse point in worldSpace after zoom
 			origin = origin.plus(m2.minus(m1).invertY());
 			repaint();
 		}
 	}
 }
