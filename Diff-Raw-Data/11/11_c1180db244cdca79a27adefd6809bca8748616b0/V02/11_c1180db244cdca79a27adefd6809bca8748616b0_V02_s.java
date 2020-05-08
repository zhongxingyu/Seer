 import java.awt.*;
 import java.util.ArrayList;
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class V02 extends JPanel {
 	ArrayList<GUINest> nests;
 	GUIKitStand guiKitStand;
 	GUIPartRobot guiPartRobot;
 	int paintCount = 0;
 	
 	private enum Status {
 		GETPARTFROMNEST, PUTPARTINKIT, TAKINGPICTURE, REMOVEPARTFROMKIT, IDLE
 	}
 	
 	private Status status = Status.IDLE;
 	
 	public V02() {
 
		this.setPreferredSize(new Dimension(800,600));
 		
 		Painter.loadImages();
 		
 		nests = new ArrayList<GUINest> ();
 		guiKitStand = new GUIKitStand( new KitStand() );
 		guiPartRobot = new GUIPartRobot( new PartRobot() );
 		
 		guiKitStand.addKit( new GUIKit( new Kit(), guiKitStand.getCameraStationLocation().x, guiKitStand.getCameraStationLocation().y ), GUIKitStand.StationNumber.THREE );
 		
 		nests.add( new GUINest( new Nest(), 722, 275 ) );
 		nests.get(0).addPart( new GUIPart( new Part(), Painter.ImageEnum.CORNFLAKE,  nests.get(0).movement.getStartPos().x + 25, nests.get(0).movement.getStartPos().y + 25, Math.PI/-2 ) );
 		
 	}
 	
 	public void paint(Graphics gfx)
 	{
 		long currentTime = System.currentTimeMillis();
 
 		checkStatus(currentTime);
 		
 		Graphics2D g = (Graphics2D)gfx;
 		
 		guiKitStand.draw(g, currentTime);
		guiPartRobot.draw(g, currentTime);
 		nests.get(0).draw( g, currentTime );
 		
 		paintCount++;
 	}
 	
 	private void checkStatus(long currentTime)
 	{/*
 		if (status == Status.IDLE && guiKitDeliveryStation.inConveyor.hasFullPalletAtEnd(currentTime))
 		{
 			guiKitRobot.movement = new Movement(guiKitDeliveryStation.inConveyor.getLocationOfEndPallet(currentTime), 0);
 			status = Status.GETKITFROMINCONVEYOR;
 			return;
 		}
 		
 		if (status == Status.GETKITFROMINCONVEYOR && guiKitRobot.arrived(currentTime))
 		{
 			guiKitRobot.setKit(guiKitDeliveryStation.inConveyor.removeEndPalletKit());
 			guiKitRobot.movement = new Movement(guiKitStand.getCameraStationLocation(), 0);
 			status = Status.PUTKITONSTAND;
 			return;
 		}
 		
 		if (status == Status.PUTKITONSTAND && guiKitRobot.arrived(currentTime))
 		{
 			guiKitStand.addKit(guiKitRobot.removeKit(), GUIKitStand.StationNumber.THREE);
 			guiKitRobot.park();
 			status = Status.TAKINGPICTURE;
 			return;
 		}
 		
 		if (status == Status.TAKINGPICTURE && guiKitRobot.arrived(currentTime))
 		{
 			guiKitRobot.movement = new Movement(guiKitStand.getCameraStationLocation(), 0);
 			status = Status.GETKITFROMSTAND;
 			return;
 		}
 		
 		if (status == Status.GETKITFROMSTAND && guiKitRobot.arrived(currentTime))
 		{
 			guiKitRobot.setKit(guiKitStand.removeKit(GUIKitStand.StationNumber.THREE));
 			guiKitRobot.movement = new Movement(guiKitDeliveryStation.getOutConveyorLocation(), 0);
 			status = Status.PUTKITONOUTCONVEYOR;
 			return;
 		}
 		
 		if (status == Status.PUTKITONOUTCONVEYOR && guiKitRobot.arrived(currentTime))
 		{
 			guiKitDeliveryStation.outConveyor.addPallet(new GUIPallet(new Pallet(), guiKitRobot.removeKit(), 
 					guiKitDeliveryStation.outConveyor.movement.getStartPos().x-50+60*guiKitDeliveryStation.outConveyor.getLaneLength(), guiKitDeliveryStation.outConveyor.movement.getStartPos().y-12));
 			guiKitRobot.park();
 			status = Status.IDLE;
 			return;
 		}
 		
 		
 		if (guiKitDeliveryStation.outConveyor.hasFullPalletAtEnd(currentTime))
 		{
 			guiKitDeliveryStation.outConveyor.removeEndPallet();
 			guiKitDeliveryStation.inConveyor.addPallet();
 		}*/
 	}
 }
