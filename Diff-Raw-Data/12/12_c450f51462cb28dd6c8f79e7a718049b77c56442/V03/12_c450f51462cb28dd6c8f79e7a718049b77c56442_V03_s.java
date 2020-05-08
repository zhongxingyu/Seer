 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 import javax.swing.JPanel;
 
 
 public class V03 extends JPanel
 {
 	WholeLane lane1;
 	Part p;
 	GUILane gl;
 	GUIPart gp1;
 	GUIPart gp2;
 	GUIPart gp3;
 	GUIPart gp4;
 	GUIFeeder gf;
 	GUIDiverter gd;
 	GUIDiverterArm gda;
 	
 	int paintCount = 0;
 	
 	public V03()
 	{
 		//panel size
 		this.setPreferredSize(new Dimension(800,400));	
 		
 		//load images
 		Painter.loadImages();
 		
 		//initialize
 		lane1 = new WholeLane();
 		gl = new GUILane( lane1.getComboLane(), 5, 190,10 );
 		gf = new GUIFeeder( lane1.getFeeder(), 570, 10 );
 		gd = new GUIDiverter( 490, 10 );
		gda = new GUIDiverterArm( 490, 50 );
 		p = new Part("p", "a random part", 5);
 	}
 	
 	public void paint(Graphics gfx)
 	{
 		Graphics2D g = (Graphics2D)gfx;
 		
 		
 		if( lane1.getLane() == 1 ){ //if top lane
 			//top lane moving parts
 			if (paintCount % 100 == 0) {
 				gp1 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 20);
 				gp1.movement = new Movement(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,20), 10, System.currentTimeMillis()+5000);
 			}
 			
 			if (paintCount % 100 == 25) {
 				gp2 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 20);
 				gp2.movement = new Movement(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,20), 10, System.currentTimeMillis()+5000);
 			}
 			
 			if (paintCount % 100 == 50) {
 				gp3 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 20);
 				gp3.movement = new Movement(gp3.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,20), 10, System.currentTimeMillis()+5000);
 			}
 			
 			if (paintCount % 100 == 75) {
 				gp4 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 20);
 				gp4.movement = new Movement(gp4.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,20), 10, System.currentTimeMillis()+5000);
 			}
 		} else { //bottom lane
 			//bot lane moving parts
 			if (paintCount % 100 == 0) {
 				gp1 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 70);
 				gp1.movement = new Movement(gp1.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,70), 10, System.currentTimeMillis()+5000);
 			}
 			
 			if (paintCount % 100 == 25) {
 				gp2 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 70);
 				gp2.movement = new Movement(gp2.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,70), 10, System.currentTimeMillis()+5000);
 			}
 			
 			if (paintCount % 100 == 50) {
 				gp3 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 70);
 				gp3.movement = new Movement(gp3.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,70), 10, System.currentTimeMillis()+5000);
 			}
 			
 			if (paintCount % 100 == 75) {
 				gp4 = new GUIPart(p, Painter.ImageEnum.CORNFLAKE, 490, 70);
 				gp4.movement = new Movement(gp4.movement.calcPos(System.currentTimeMillis()), 0, System.currentTimeMillis(), new Point2D.Double(190,70), 10, System.currentTimeMillis()+5000);
 			}
 		}
 		
 		//change lane every 40 and 90 (time for diverter to move later?)
		if( paintCount % 100 == 40 || paintCount % 100 == 90){
 			lane1.divert();
 		}
 		
 		gl.draw(g, System.currentTimeMillis());
 		
 		gf.draw(g, System.currentTimeMillis());
 		
 		gd.draw(g, System.currentTimeMillis());
 		
 		gda.draw(g, System.currentTimeMillis());
 		
 		if( gp1 != null )
 			gp1.draw(g, System.currentTimeMillis());
 		
 		if(gp2 != null)
 			gp2.draw(g, System.currentTimeMillis());
 		
 		if( gp3 != null )
 			gp3.draw(g, System.currentTimeMillis());
 		
 		if(gp4 != null)
 			gp4.draw(g, System.currentTimeMillis());
 		
 		
 //		if (paintCount % 100 == 75)
 //			if ( lane1.areLanesOn() )
 //				lane1.turnOffLane();
 //			else
 //				lane1.turnOnLane();
 		
 		
 		paintCount++;
 	}
 	
 }
