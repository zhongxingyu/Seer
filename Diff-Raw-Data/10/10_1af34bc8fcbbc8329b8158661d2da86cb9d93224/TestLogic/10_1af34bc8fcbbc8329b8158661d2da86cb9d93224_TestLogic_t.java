 package stijgmachine.jti1a1.nl.model;
 
 import java.util.ArrayList;
 
 import stijgmachine.jti1a1.nl.objects.GameObject;
 import stijgmachine.jti1a1.nl.objects.GameObjectContainer;
 import wiiusej.Wiimote;
 import wiiusej.wiiusejevents.physicalevents.ExpansionEvent;
 import wiiusej.wiiusejevents.physicalevents.IREvent;
 import wiiusej.wiiusejevents.physicalevents.MotionSensingEvent;
 import wiiusej.wiiusejevents.physicalevents.WiimoteButtonsEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.DisconnectionEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.NunchukInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.NunchukRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.StatusEvent;
 
 public class TestLogic extends MiniGameLogic {
 	
 	private ArrayList<GameObject> items;
 
 	@Override
 	public ArrayList<GameObject> getObjects() {
 		System.out.println("given!");
 		return items;
 	}
 
 	@Override
 	public void giveMotes(Wiimote[] wiimotes) {
 		wiimotes[0].addWiiMoteEventListeners(this);
		//wiimotes[0].set
 		System.out.println("ee");
 		// TODO Auto-generated method stub
 	}
 	
 	public TestLogic()
 	{
 		items = new ArrayList<GameObject>();
 		items.add(new GameObjectContainer(20, 20, 500, 500, GameObject.RELATIVE_FROM_TOPLEFT, new GameObject[]{
 				new GameObjectContainer(5, 5, 246, 246, GameObject.RELATIVE_FROM_TOPLEFT),
 				new GameObjectContainer(5, 5, 246, 246, GameObject.RELATIVE_FROM_TOPRIGHT),
 				new GameObjectContainer(5, 5, 246, 246, GameObject.RELATIVE_FROM_BOTTOMLEFT),
 				new GameObjectContainer(5, 5, 246, 246, GameObject.RELATIVE_FROM_BOTTOMRIGHT)
 		}));
 	}
 
 	@Override
 	public boolean isDone() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void tick() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onButtonsEvent(WiimoteButtonsEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onClassicControllerInsertedEvent(
 			ClassicControllerInsertedEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onClassicControllerRemovedEvent(
 			ClassicControllerRemovedEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onDisconnectionEvent(DisconnectionEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onExpansionEvent(ExpansionEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onGuitarHeroInsertedEvent(GuitarHeroInsertedEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onGuitarHeroRemovedEvent(GuitarHeroRemovedEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onIrEvent(IREvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMotionSensingEvent(MotionSensingEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onNunchukInsertedEvent(NunchukInsertedEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onNunchukRemovedEvent(NunchukRemovedEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusEvent(StatusEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
