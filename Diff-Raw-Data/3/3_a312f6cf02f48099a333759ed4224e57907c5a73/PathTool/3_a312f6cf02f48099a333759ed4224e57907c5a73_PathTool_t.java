 package net.fhtagn.zoobeditor.editor.tools;
 
 import java.util.ArrayList;
 
 import net.fhtagn.zoobeditor.editor.cell.GridCell;
 import net.fhtagn.zoobeditor.editor.utils.Coords;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 
 public class PathTool extends EditorTool {
 	static final String TAG = "PathTool";
 	/**
 	 * This tool has 2 states. 
 	 * The user first has to select the tank for which he will set a path (by clicking on it)
 	 * Then, he can draw the path.
 	 */
 	static final int STATE_NO_TANK = 0;
 	static final int STATE_DRAWING_PATH = 1;
 	
 	private int state;
 	
 	private GridCell selectedCell = null;
 	
 	private final ArrayList<Coords> waypoints = new ArrayList<Coords>();
 	
 	public PathTool () {
 		state = STATE_NO_TANK;
 		resetPath();
 	}
 	
 	public void resetPath () {
 		waypoints.clear();
 	}
 	
 	@Override
 	public void draw (Canvas canvas) {
 		if (state == STATE_DRAWING_PATH) {
 			final int length = waypoints.size();
 			for (int i=0; i<length; i++) {
 				Coords wp = waypoints.get(i);
 				canvas.save();
 				canvas.translate(wp.getX(), wp.getY());
 				Paint paint = new Paint();
 				paint.setTextSize(0.5f);
 				paint.setColor(Color.BLACK);
 				paint.setTextAlign(Paint.Align.CENTER);
 				canvas.drawText("" + i, 0.5f, 0.9f, paint);
 				canvas.restore();
 			}
 			
 			//Highlight selected tank
 			canvas.save();
 			canvas.translate(selectedCell.getCoords().getX(), 
 											 selectedCell.getCoords().getY());
 			Paint paint = new Paint();
 			paint.setColor(Color.argb(120, 0,0,255));
 			canvas.drawRect(0, 0, 1, 1, paint);
 			canvas.restore();
 		} else {
 			Paint paint = new Paint();
 			paint.setTextSize(2);
 			paint.setColor(Color.argb(120, 120, 120, 120));
 			canvas.drawRect(0, 0, 5, 1, paint);
 			canvas.drawText("Select a tank",0, 1, paint);
 		}
 	}
 	
 	public void savePath () {
		if (selectedCell != null)
			selectedCell.setPath(waypoints);
 	}
 	
 	@Override
   public GridCell apply(GridCell cell) {
 		if (state == STATE_NO_TANK && cell.canHavePath()) {
 			state = STATE_DRAWING_PATH;
 			selectedCell = cell;
 			ArrayList<Coords> path = cell.getPath();
 			if (path != null) {
 				waypoints.clear();
 				waypoints.addAll(cell.getPath());
 			}
 			return cell;
 		} else if (state == STATE_DRAWING_PATH){ //STATE_DRAWING_PATH
 			if (cell.isValidWaypoint()) {
 				waypoints.add(cell.getCoords());
 			} 
 			return cell;
 		} else
 			return cell;
   }
 
 }
