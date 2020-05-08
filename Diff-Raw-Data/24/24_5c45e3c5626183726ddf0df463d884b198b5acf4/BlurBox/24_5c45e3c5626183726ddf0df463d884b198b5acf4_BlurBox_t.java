 package mmb.foss.aueb.icong.boxes;
 
 import mmb.foss.aueb.icong.R;
 import android.content.Context;
import android.graphics.Bitmap;
 import android.util.Log;
 
 public class BlurBox extends Box {
 
 	public BlurBox(Context context) {
 		// TODO Auto-generated constructor stub
 		super(context, R.drawable.blur);
 		buttonX = new int[3][2];
 		buttonY = new int[3][2];
 		buttonPressed = new boolean[3];
 		buttonX[0][0] = 5;
 		buttonX[0][1] = 27;
 		buttonX[1][0] = 5;
 		buttonX[1][1] = 27;
 		buttonX[2][0] = 95;
 		buttonX[2][1] = 117;
 		buttonY[0][0] = 5;
 		buttonY[0][1] = 27;
 		buttonY[1][0] = 39;
 		buttonY[1][1] = 61;
 		buttonY[2][0] = 23;
 		buttonY[2][1] = 43;
 		this.setNoOfInputs(2);
 		this.setNoOfOutpus(1);
 
 	}
 
 	@Override
 	public void function() {
 		
 		if(this.getOutput(0) != null)
 			return;
 		
 		Bitmap src = null;
		Float size;
 		
 		if (this.getInput(0) == null || this.getInput(1) == null) {
 			return;
 		} else {
			
			src = (Bitmap) this.getInput(0);
			
 		}
 
 	}
 
 	@Override
 	public void showDialog(Context context) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
