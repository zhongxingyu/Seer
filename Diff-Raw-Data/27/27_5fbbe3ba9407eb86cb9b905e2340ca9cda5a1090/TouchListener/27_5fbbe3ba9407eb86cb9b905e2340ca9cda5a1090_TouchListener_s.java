 package dimitri.suls.allshare.tv;
 
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 import com.sec.android.allshare.control.TVController;
 
 import dimitri.suls.allshare.managers.DeviceInteractionManager;
 
 public class TouchListener implements OnTouchListener {
 	private DeviceInteractionManager deviceInteractionManager = null;
 	private int previousPositionX = 0;
 	private int previousPositionY = 0;
 
 	public TouchListener(DeviceInteractionManager deviceInteractionManager) {
 		this.deviceInteractionManager = deviceInteractionManager;
 	}
 
 	@Override
 	public boolean onTouch(View view, MotionEvent motionEvent) {
 		boolean hasConsumedEvent = false;
 
 		int currentPositionX = (int) motionEvent.getX();
 		int currentPositionY = (int) motionEvent.getY();
 
 		switch (motionEvent.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			deviceInteractionManager.execute(new TVCommand() {
 				@Override
 				public void execute(TVController tvController) {
 					tvController.sendTouchDown();
 				}
 			});
 
 			break;
 		case MotionEvent.ACTION_UP:
			hasConsumedEvent = true;

 			deviceInteractionManager.execute(new TVCommand() {
 				@Override
 				public void execute(TVController tvController) {
 					tvController.sendTouchUp();
 				}
 			});
 
 			break;
 		case MotionEvent.ACTION_MOVE:
 			hasConsumedEvent = true;
 
 			final int differenceBetweenCurrentAndPreviousPositionX = (currentPositionX - previousPositionX) * 2;
 			final int differenceBetweenCurrentAndPreviousPositionY = (currentPositionY - previousPositionY) * 2;
 
 			deviceInteractionManager.execute(new TVCommand() {
 				@Override
 				public void execute(TVController tvController) {
 					tvController.sendTouchMove(differenceBetweenCurrentAndPreviousPositionX, differenceBetweenCurrentAndPreviousPositionY);
 				}
 			});
 
 			break;
 		}
 
 		previousPositionX = currentPositionX;
 		previousPositionY = currentPositionY;
 
 		return hasConsumedEvent;
 	}
 }
