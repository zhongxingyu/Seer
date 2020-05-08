 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 import java.util.ArrayList;
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class PartRobotDemo extends JPanel implements ActionListener {
 	ArrayList<GUINest> nests;
 	GUIKitStand guiKitStand;
 	GUIPartRobot guiPartRobot;
 	int paintCount = 0, timerFireCount = 0;
 	Timer moveTimer;
 	GUIFlash camFlash;
 	
 	public PartRobotDemo() {
 
 		this.setPreferredSize(new Dimension(800,600));
 		
 		Painter.loadImages();
 		
 		nests = new ArrayList<GUINest> ();
 		guiKitStand = new GUIKitStand( new KitStand() );
 		guiPartRobot = new GUIPartRobot( new PartRobot() );
 		moveTimer = new Timer( 5000, this );
 		
 		guiKitStand.addKit( new GUIKit( new Kit(), guiKitStand.getCameraStationLocation().x, guiKitStand.getCameraStationLocation().y ), GUIKitStand.StationNumber.THREE );
 		
 		nests.add( new GUINest( new Nest(), 580, 285 ) );
 		nests.get(0).addPart( new GUIPart( new Part(), Painter.ImageEnum.CORNFLAKE,  nests.get(0).movement.getStartPos().x + 25, nests.get(0).movement.getStartPos().y + 25, Math.PI/-2 ) );
 		
 		camFlash = null;
 		
 		moveTimer.start();	
 	}
 	
 	public void paint(Graphics gfx)
 	{
 		long currentTime = System.currentTimeMillis();
 		
 		Graphics2D g = (Graphics2D)gfx;
 		
 		guiKitStand.draw(g, currentTime);
 		nests.get(0).draw( g, currentTime );
 		guiPartRobot.draw(g, currentTime);
 		
 		if( camFlash != null ){
 			camFlash.draw(g, currentTime);
 		}
 		
 		paintCount++;
 	}
 	
 	public void actionPerformed( ActionEvent ae ) {
 		if ( ae.getSource() == moveTimer ) {
 			if ( timerFireCount % 5 == 0 ) {
 				guiPartRobot.movement = new Movement(new Point2D.Double( nests.get(0).movement.getStartPos().x + 50, nests.get(0).movement.getStartPos().y + 50 ), 0 );
 			}
 			
 			else if ( timerFireCount % 5 == 1 ) {
 				guiPartRobot.partRobot.addPartToGripper( 2, nests.get(0).removePart( 0 ) );
 				guiPartRobot.movement = new Movement( new Point2D.Double( guiKitStand.getCameraStationLocation().x, guiKitStand.getCameraStationLocation().y - 130 ), 0);
 			}
 			
 			else if ( timerFireCount % 5 == 2 ) {
 				guiKitStand.getKit( GUIKitStand.StationNumber.THREE ).addPart( 3, guiPartRobot.partRobot.removePartFromGripper( 2 ) );
 				guiPartRobot.movement = new Movement(new Point2D.Double( guiPartRobot.baseStartX, guiPartRobot.baseStartY ), 0 );
 			}
 			
 			else if ( timerFireCount % 5 == 3 ) {
 				//takePicture();
 				camFlash = new GUIFlash( -10, 280); //make flash appear
 			}
 			
 			else if ( timerFireCount % 5 == 4 ) {
 				camFlash = null; //make flash disappear
				nests.get(0).addPart( guiKitStand.getKit(GUIKitStand.StationNumber.THREE ).removePart( 3 ) );
 			}
 			
 			timerFireCount++;
 		}
 		
 	}
 }
