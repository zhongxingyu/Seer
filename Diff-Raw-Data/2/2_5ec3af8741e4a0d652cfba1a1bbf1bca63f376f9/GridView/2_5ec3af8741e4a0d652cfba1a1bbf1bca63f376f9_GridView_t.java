 package no.ntnu.tdt4240.views;
 
 import no.ntnu.tdt4240.models.Cell;
 import no.ntnu.tdt4240.models.GameBoard;
 import no.ntnu.tdt4240.R;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.view.View;
 
 public class GridView extends View {
 
 	private static final int CELL_SIZE = 50;
     private static final int WIDTH = 1000 / CELL_SIZE;
     private static final int HEIGHT = 480 / CELL_SIZE;
     private static Cell[][] gameboard;
     
 	public GridView(Context context) {
 		super(context);
//		gameboard = GameBoard.getGameBoard();
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		Paint background = new Paint();
 		background.setColor(R.color.background);
 		
 		Paint gold = new Paint();
 		gold.setColor(no.ntnu.tdt4240.R.color.gold);
 		
 		Paint mine = new Paint();
 		mine.setColor(no.ntnu.tdt4240.R.color.mine);
 		
 		Paint blank = new Paint();
 		blank.setColor(no.ntnu.tdt4240.R.color.blank);
 		
 		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
 		
 		for (int i = 0; i < HEIGHT; i++) {
 			for (int j = 0; j < WIDTH; j++) {
 				if (gameboard[i][j].getType()=="gold"){
 					//Draw gold
 					canvas.drawRect(
 							i * CELL_SIZE, 
 							j * CELL_SIZE, 
 							(i * CELL_SIZE) + CELL_SIZE, 
 							(j * CELL_SIZE) + CELL_SIZE, 
 							gold);
 				}else if (gameboard[i][j].getType()=="mine"){
 					//Draw mine
 					canvas.drawRect(
 							i * CELL_SIZE, 
 							j * CELL_SIZE, 
 							(i * CELL_SIZE) + CELL_SIZE, 
 							(j * CELL_SIZE) + CELL_SIZE, 
 							mine);
 				}else{
 					//Draw blank
 					canvas.drawRect(
 							i * CELL_SIZE, 
 							j * CELL_SIZE, 
 							(i * CELL_SIZE) + CELL_SIZE, 
 							(j * CELL_SIZE) + CELL_SIZE, 
 							blank);
 				}
 			}
 		}
 	}
 
 }
