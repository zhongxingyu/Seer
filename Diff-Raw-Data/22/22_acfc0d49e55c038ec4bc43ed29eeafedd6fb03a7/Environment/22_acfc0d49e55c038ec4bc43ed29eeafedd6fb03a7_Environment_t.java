 package chalmers.dax021308.ecosystem.model;
 
 import java.util.Random;
 
 import chalmers.dax021308.ecosystem.model.EcoWorld.OnFinishListener;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.Position;
 
 
 /**
 * Toy environment class.
 * <p>
 * Only for testing the tick-function.
  * @author Erik
  *
  */
 public class Environment implements IEnvironment {
 	private OnFinishListener mListener;
 	private Random mRandom = new Random();
 	
 	public Environment(OnFinishListener listener) {
 		this.mListener = listener;
 
 	}
 
 	@Override
 	public void run() {
 		//Do work here, will run on separate thread.
 		
 		//Sover bara för att simulera arbete. (Ta bort)
 		long sleep = 10 + mRandom.nextInt(20);
 		Log.v("Environment sleeping " + sleep + " ms");
 		try {
 			Thread.sleep(sleep);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		Log.v("Environment onFinish().");
 		//Callback metod när arbetet är färdigt, till Ecosystem.
 		mListener.onFinish(null, null);
 	}
 
 	@Override
 	public boolean isFree(Position p) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
