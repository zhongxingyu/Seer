 package Part1;
 
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 
 public class Map extends JPanel {
 	/**
 	 * Map is a JPanel with the lines drawn
 	 */
 	private static final long serialVersionUID = 1L;
 	private ArrayList<RoadSegment> segments;
 	private TomsTempClass test;
 	
 	private int pressedX;
 	private int pressedY;
 	private int releasedX;
 	private int releasedY;	
 		
 	public Map(){
 		test = new TomsTempClass();
 		segments = test.mapTestMethod();
 		zoomWithBox();
 	}
 
 	public void zoomWithBox(){
 		addMouseListener(new MouseAdapter(){
 			
 			public void mousePressed(MouseEvent e){				
 				pressedX = e.getX();
 				pressedY = e.getY();
 				
 				System.out.println("Pressed X : "+ pressedX);
 				System.out.println("Pressed Y : "+ pressedY);
 			}
 			
 			public void mouseReleased(MouseEvent e){
 				releasedX = e.getX();
 				releasedY =  e.getY();
 				System.out.println("Released X : "+ releasedX);
 				System.out.println("Released Y : "+ releasedY);
 				
 				double selectedXpixels = releasedX-pressedX;
 				
 				double windowXpixels = Window.getInstance().getWidth();
 				
 				double ratio = selectedXpixels/windowXpixels;
 				
 				double zoomFactor = Window.getInstance().getZoomFactor() * ratio;
 				
 				System.out.println("zoomFactor : "+ zoomFactor);
 				
 				Window.getInstance().setZoomFactor(1.5);
 				
 				getMapTestMethod();
				//Window.getInstance().repaint();
 			}
 		});		
 	}
 	
 //	public void createSegmentArray(){
 //		RoadSegment segment1 = new RoadSegment(10,10,100,100,Color.red);
 //		RoadSegment segment2 = new RoadSegment(200,300,500,500,Color.black);
 //		
 //		segments = new ArrayList<RoadSegment>();
 //		segments.add(segment1);
 //		segments.add(segment2);		
 //	}
 	
     public void paintComponent(Graphics g) {
         for(RoadSegment r : segments) {
             r.paintComponent(g);
         }
     }
     
     public void getMapTestMethod() {
     	segments = WindowHandler.calculatePixels();
     	//segments = test.mapTestMethod();
     }
     
     public void addRoadSegment(RoadSegment roadSegment) {
     	segments.add(roadSegment);
     }
     
 }
