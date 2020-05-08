 package lazygames.trainyoureye;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 
 import android.view.MotionEvent;
 import android.view.View;
 
 public class TouchShapesHandler extends Activity implements View.OnTouchListener{
 	protected GameOneData game_data = null;
 	protected Context context = null;
 	private Resources res;
 	
 	public TouchShapesHandler(View view, Resources res) {
 		view.setOnTouchListener(this);
 		context = view.getContext();
 		this.res = res;
 	}
 	
 	public boolean onTouch(View v, MotionEvent event) {
 		touchCorrectShape(event);	
 		return false;
 	}
 	
 	public void touchCorrectShape(MotionEvent event) {
 		RandomShape[] shapes = game_data.getShapes();
 		int x = Math.round(event.getX());
 		int y = Math.round(event.getY());
 
 		int correctShape = game_data.getCorrectShape();
 		for(int i = 0; i < shapes.length;i++) {
 		
 			if(x >= shapes[i].getX()
 				&& x <= shapes[i].getX() + shapes[i].getWidth()
 				&& y >= shapes[i].getY()
 				&& y <= shapes[i].getY() + shapes[i].getHeight()) {
 			
 				if(i == correctShape) {
 					game_data.levelUp();
 					game_data.gameLoop();
 					game_data.reDrawGameCanvas();
 				} else {
 					createDialog();
 				}
 			}
 		}
 	}
 	public void setGameData(GameOneData data) {
 		this.game_data = data;
 	}
 	public void createDialog() {
 		AlertDialog.Builder restartDialog = new AlertDialog.Builder(context);
 		 
 		// Setting Dialog Title
		restartDialog.setTitle(res.getString(R.string.game_over) + " " + Integer.toString(game_data.getScore()));
 		 
 		// Setting Dialog Message
 		restartDialog.setMessage(res.getString(R.string.retry));
 		 		 
 		// Setting Positive "Yes" Btn
 		restartDialog.setPositiveButton(res.getString(R.string.yes),
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		            	game_data.reset();
 		            	game_data.gameLoop();
 		            	game_data.reDrawGameCanvas();
 		            }
 		        });
 		// Setting Negative "NO" Btn
 		restartDialog.setNegativeButton(res.getString(R.string.no),
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		                exitGame();
 		            }
 		        });
 		 
 		// Showing Alert Dialog
 		restartDialog.show();		
 	}
 	public void exitGame() {
 		((Activity) context).finish();
 	}
 	
 }
