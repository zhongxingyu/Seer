 package device;
 
 import java.util.Iterator;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Logger;
 
 import data.Position;
 import javaclient3.structures.PlayerPose;
 import javaclient3.structures.localize.PlayerLocalizeHypoth;
 import javaclient3.structures.localize.PlayerLocalizeSetPose;
 
 public class Localize extends RobotDevice
 {
 	/** Logging support */
 	Logger logger = Logger.getLogger (Localize.class.getName ());
 
 	/** Callback listeners */
 	CopyOnWriteArrayList<ILocalizeListener> localizeListeners;
 
 	/** initial values for the covariance matrix (c&p example from playernav) */
 	protected double cov[] = { 0.5*0.5, 0.5*0.5, (Math.PI/6.0)*(Math.PI/6.0), 0, 0, 0 };
 
 	Position getPosition;
 	Position oldPosition;
 	PlayerLocalizeSetPose setPosition;
 
 	public Localize(DeviceNode roboClient, Device device) {
 		super(roboClient, device);
 
 		getPosition = new Position();
 		oldPosition = new Position();
 		setPosition = new PlayerLocalizeSetPose ();
 
 		/** Set the initial covariance matrix */
 //		setPosition.setCov (cov);
 
 		localizeListeners = new CopyOnWriteArrayList<ILocalizeListener>();
 	}
 	// Only to be called @~10Hz
 	@Override
 	protected void update ()
 	{
 		if (((javaclient3.LocalizeInterface) device).isDataReady()) {
 			/** Get current position belief */
 			PlayerLocalizeHypoth[] hypList = ((javaclient3.LocalizeInterface) device).getData().getHypoths();
 			if (hypList.length > 0) {
 				/** Only first hypothesis is interessting (for now) */
				PlayerLocalizeHypoth hyp1 = hypList[0];
				if (hyp1 != null) {
					PlayerPose curPos = hyp1.getMean();
					if (curPos != null) {
						getPosition.setX(curPos.getPx());
						getPosition.setY(curPos.getPy());
						getPosition.setYaw(curPos.getPa());
					}
 				}
 			}
 			
 			if(oldPosition.equals(getPosition) == false) {
 				notifyNewPosition(getPosition);
 				oldPosition.setPosition(getPosition);
 			}
 		}
 	}
 
 	private void notifyNewPosition(Position newPose) {
 
 		Iterator<ILocalizeListener> it = localizeListeners.iterator();
 		while (it.hasNext()) { it.next().newPositionAvailable(new Position(newPose)); }
 
 	}
 	public void addListener(ILocalizeListener cb){
 		localizeListeners.addIfAbsent(cb);
 	}
 	public void removeListener(ILocalizeListener cb){
 		localizeListeners.remove(cb);
 	}
 	public synchronized boolean setPosition(Position position) {
 		if (position != null) {
 			/** Set position belief */
 			setPosition.setMean(new PlayerPose(position.getX(),position.getY(),position.getYaw()));
 			((javaclient3.LocalizeInterface) device).setPose(setPosition);
 		}
 		return true;
 	}
 	public synchronized Position getPosition() {
 		return new Position(getPosition);
 	}
 	@Override synchronized public void shutdown() {
 		super.shutdown();
 		localizeListeners.clear();
 	}
 }
