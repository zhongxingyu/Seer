 package org.openstreetmap.OSMZmiany;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.openstreetmap.OSMZmiany.DataContainer.Changeset;
 import org.openstreetmap.OSMZmiany.DataContainer.Node;
 import org.openstreetmap.gui.jmapviewer.Coordinate;
 import org.openstreetmap.gui.jmapviewer.DefaultMapController;
 import org.openstreetmap.gui.jmapviewer.JMapViewer;
 import org.openstreetmap.gui.jmapviewer.interfaces.MapViewChangeListener;
 import org.openstreetmap.gui.jmapviewer.interfaces.OverlayPainter;
 
 public class ZMapWidget extends JMapViewer implements MapViewChangeListener, MouseListener, OverlayPainter,DataContainerListener {
 	private static final long serialVersionUID = 1L;
 	
 	private Image overlayI;
 	
 	private Coordinate c1;
 	private Coordinate c2;
 	public DataContainer dc;
 	public DrawStyle drawStyle=new DrawStyle();
 	
 	private ArrayList<ZMapWidgetListener> zMapWidgetListeners=new ArrayList<ZMapWidgetListener>();
 	
 	public ZMapWidget(DataContainer dc){
 		super();
 		this.dc=dc;
 		dc.addDataContainerListener(this);
 		addChangeListener(this);
 		addOverlayPainter(this);
 		addMouseListener(this);
 		
 		DefaultMapController mapController = new DefaultMapController(this);		
 		mapController.setMovementMouseButton(MouseEvent.BUTTON2);
 	}
 
 	public void paintOverlay(Graphics g) {
 		if (overlayI != null) {
 			g.drawImage(overlayI, 0, 0, null);
 		}		
 	}
 
 	public void mouseClicked(MouseEvent arg0) {
 		Iterator<Long> iterator = dc.nodes.keySet().iterator();
 	    while (iterator.hasNext()) {
 	    	Node node=dc.nodes.get(iterator.next());	    	
 	    	Point p = getMapPosition(node.lat,node.lon);
 	    	if(p!=null){	    		
 	    		if(p.x<arg0.getX()+3&&p.x>arg0.getX()-3&&
 	    		   p.y<arg0.getY()+3&&p.y>arg0.getY()-3){
 	    			for(int i=0;i<zMapWidgetListeners.size();i++){
 	    				zMapWidgetListeners.get(i).nodeClicked(node);
 	    			}	    			
 	    			return;	    			
 	    		}
 	    	}
 	    }	    
 	}
 
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mousePressed(MouseEvent arg0) {
 		if(arg0.getButton()==1)
 			c1=getPosition(arg0.getX(), arg0.getY());		
 	}
 
 	public void mouseReleased(MouseEvent arg0) {
 		if(arg0.getButton()==1){
 			c2=getPosition(arg0.getX(), arg0.getY());
 			
 			//Corners
 			Point p = getMapPosition(c1.getLat(),c1.getLon());
 	    	Point p2 = getMapPosition(c2.getLat(),c2.getLon());
 	    	if(p2.x<p.x&&p2.y<p.y){
 	    		Coordinate c3=c1;
 	    		c1=c2;
 	    		c2=c3;	    		
 	    	}else if(p2.x>p.x&&p2.y<p.y){
 	    		Coordinate c1A=c1;
 	    		Coordinate c2A=c2;	    		
 	    		c1=new Coordinate(c2A.getLat(),c1A.getLon());
 	    		c2=new Coordinate(c1A.getLat(),c2A.getLon());
 	    	}else if(p2.x<p.x&&p2.y>p.y){
 	    		Coordinate c1A=c1;
 	    		Coordinate c2A=c2;	    		
 	    		c1=new Coordinate(c1A.getLat(),c2A.getLon());
 	    		c2=new Coordinate(c2A.getLat(),c1A.getLon());
 	    	}
 			
 			for(int i=0;i<zMapWidgetListeners.size();i++){
 				zMapWidgetListeners.get(i).boxDrawed(c1, c2);
 			}	    				
 		}		
 	}
 
 	public void mapViewChanged() {
 		refrashOverlay();		
 	}
 	
 	
 	public void refrashOverlay(){
 		overlayI = new BufferedImage(this.getWidth()+1, this.getHeight()+1, BufferedImage.TYPE_INT_ARGB);		
 		Graphics g = overlayI.getGraphics();
 		
 		
 		Changeset ch=drawStyle.getSelectedChangeset();
 		long changesetid=-1;
 		if(ch!=null){
 			changesetid=ch.id;
 		}else{
 			Node node=drawStyle.getSelectedNode();
 			if(node!=null)
 				changesetid=node.changesetId;					
 		}
 		if(changesetid!=-1){	
 			System.out.println("CHANESET:"+changesetid);
 			Iterator<Long> iterator = dc.nodes.keySet().iterator();
 			if(iterator.hasNext()){
 				Node n=dc.nodes.get(iterator.next());
 				double left=-360;
 				double right=-360;
 				double top=-360;
 				double bottom=-360;
 				while (iterator.hasNext()) {
 					Node node=dc.nodes.get(iterator.next());
 					if(node.changesetId==changesetid){
 						if(left>node.lat||left==-360)left=node.lat;
     					if(right<node.lat||right==-360)right=node.lat;
     					if(top<node.lon||top==-360)top=node.lon;
     					if(bottom>node.lon||bottom==-360)bottom=node.lon;
 					}
 				}
 				//Draw changeset box
 				Point p1=this.getMapPosition(left, top,false);
 				Point p2=this.getMapPosition(right, bottom,false);
 				if(p1!=null&&p2!=null){
 					System.out.println(p1.x+":"+p1.y+";"+(p2.x-p1.x)+";"+(p2.y-p1.y));
 					g.setColor(new Color(255,0,0,30));
 			    	g.fillRect(p2.x, p2.y,p1.x-p2.x, p1.y-p2.y);
 				}					
 			}
 			
 		}
 		g.setColor(Color.BLACK);
 		
 		if(drawStyle!=null){
 			Iterator<Long> iterator = dc.nodes.keySet().iterator();
 	    	while (iterator.hasNext()) {
 	    		Node node=dc.nodes.get(iterator.next());
 	    		drawStyle.drawNode(g,this, node);	    	
 	    	}
 		}
		MapFilter mf=Configuration.instance.getSelectedProfile().getMapFilter();
 		if(mf instanceof DrawerOverlay){
 			DrawerOverlay dov=(DrawerOverlay)mf;
 			dov.draw(g, this);
		}
 	    repaint();
 	}
 
 
 	public void dataChanged() {
 		refrashOverlay();		
 	}
 	
 		
 	public void addZMapWidgetListener(ZMapWidgetListener z){
 		zMapWidgetListeners.add(z);
 	}
 
 
 }
