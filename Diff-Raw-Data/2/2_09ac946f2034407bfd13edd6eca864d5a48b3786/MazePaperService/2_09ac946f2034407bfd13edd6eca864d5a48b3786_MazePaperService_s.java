 package jiongye.app.livewallpaper.mazepaper;
 
 import java.util.Random;
 
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.os.Handler;
 import android.service.wallpaper.WallpaperService;
 import android.view.SurfaceHolder;
 
 public class MazePaperService extends WallpaperService {
 	private final Handler mHandler = new Handler();
 	public static final String SHARED_PREFS_NAME = "mazepaper_settings";
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 //		android.os.Debug.waitForDebugger();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	@Override
 	public Engine onCreateEngine() {
 		return new MazePaperEngine();
 	}
 
 	private class MazePaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
 
 		private SharedPreferences preferences;
 
 		private int mazeRows = 5;
 		private int mazeCols = 5;
 		private Maze maze;
 
 		private String solveSpeed;
 
 		private boolean progressiveDraw;
 		private boolean progressiveDrawDone;
 		private int progressiveDrawStep;
 		private int progressiveFullDrawCount;
 		
 		private boolean info;
 		
 		private boolean debug;
 		private Paint debugPaint;
 		
 		private int mazeSolved = 0;
 		private int mazeMoves = 0;
 		private int mazeTotalMoves = 0;
 		private int mazeAverageMoves = 0;
 		
 		private final Runnable mdrawMaze = new Runnable() {
 			public void run() {
 				drawFrame();
 			}
 		};
 		private boolean mVisible;
 
 		MazePaperEngine() {
 			preferences = MazePaperService.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
 			preferences.registerOnSharedPreferenceChangeListener(this);
 			onSharedPreferenceChanged(preferences, null);
 		}
 
 		public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
 			// grab wallpaper preference
 			try {
 				this.mazeRows = Integer.parseInt(pref.getString("maze_rows", "10"));
 				this.mazeCols = Integer.parseInt(pref.getString("maze_cols", "8"));
 				this.solveSpeed = pref.getString("maze_solve_speed", "slow");
 				this.progressiveDraw = pref.getBoolean("maze_draw_progressive", true);
 				this.info = pref.getBoolean("maze_info", false);
 			} catch (Exception exp) {
 				this.mazeRows = 10;
 				this.mazeCols = 10;
 				this.solveSpeed = "slow";
 				this.progressiveDraw = true;
 				this.progressiveDrawDone = false;
 				this.info = false;
 			}
 									
 			this.progressiveDrawDone = false;
 			this.progressiveDrawStep = (int) (this.mazeRows * this.mazeCols * 0.05);
 			this.progressiveDrawStep  = this.progressiveDrawStep < 1 ? 1 : this.progressiveDrawStep;
 			this.progressiveFullDrawCount = 0;
 						
 			this.debug = false;
 			
 			this.debugPaint = new Paint();
 			this.debugPaint.setColor(0x88ffffcc);
 			this.debugPaint.setTextSize(15f);
 			
 			this.mazeSolved = 0;
 			this.mazeTotalMoves = 0;
 			this.mazeAverageMoves = 0;
 			
 			generateMaze();
 		}
 
 		void generateMaze() {
 			maze = new Maze(mazeRows, mazeCols);
 			maze.createMaze();
 									
 			maze.cpu = new CPU();
 			
 			progressiveDrawDone = false;
 			progressiveFullDrawCount = 0;
 		}
 		
 		@Override
 		public void onCreate(SurfaceHolder surfaceHolder) {
 			super.onCreate(surfaceHolder);
 		}
 
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			mHandler.removeCallbacks(mdrawMaze);
 		}
 
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			mVisible = visible;
 			if (visible) {
 				drawFrame();
 			} else {
 				mHandler.removeCallbacks(mdrawMaze);
 			}
 		}
 
 		void drawFrame() {
 			final SurfaceHolder holder = getSurfaceHolder();
 			int speed = 750;
 
 			Canvas c = null;
 			try {
 				c = holder.lockCanvas();
 				if (c != null) {
 					drawMaze(c);
 				}
 			} finally {
 				if (c != null)
 					holder.unlockCanvasAndPost(c);
 			}
 
 			// Reschedule the next redraw based on speed selected
 			mHandler.removeCallbacks(mdrawMaze);
 			if (mVisible) {
 				if (this.solveSpeed.equals("crazy_fast"))
 					speed = 5;
 				else if (this.solveSpeed.equals("really_fast"))
 					speed = 50;
 				else if (this.solveSpeed.equals("fast"))
 					speed = 150;
 				else if (this.solveSpeed.equals("medium"))
 					speed = 500;
 
 				mHandler.postDelayed(mdrawMaze, speed);
 			}
 		}
 
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			super.onSurfaceChanged(holder, format, width, height);
 
 		}
 
 		@Override
 		public void onSurfaceCreated(SurfaceHolder holder) {
 			super.onSurfaceCreated(holder);
 		}
 
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			mVisible = false;
 			mHandler.removeCallbacks(mdrawMaze);
 		}
 		
 		void drawMaze(Canvas c) {
 			maze.cpu.setRadius(getCellSize(c));
 
 			c.save();
 			c.drawColor(0xff000000);
 
 			if(!this.progressiveDraw){
 				drawMazeNormal(c);
 			} else {
 				drawMazeProgress(c);
 			}
 			
 			if(this.info) {
 				c.drawText("Solved: " + mazeSolved, 20f, 60f, this.debugPaint);
 				c.drawText("Moves: " + mazeMoves, 20f, 75f, this.debugPaint);
 				c.drawText("Average Moves: " + mazeAverageMoves, 20f, 90f, this.debugPaint);
 			}
 			
 			if(this.debug){
 				c.drawText("Track size: " + maze.cpu.track.size(), 20f, 105f, this.debugPaint);
 			}
 						
 			c.restore();
 		}
 
 		void drawMazeNormal(Canvas c) {						
 			// draw maze cells
 			for (int i = 0; i < mazeRows; i++) {
 				for (int j = 0; j < mazeCols; j++) {
 					drawCell(c, maze.getCell(i, j), false);
 				}
 			}
 			
 			drawCpuMove(c);
 		}
 		
 		void drawMazeProgress(Canvas c){
 			Random random = new Random();
 			Cell cell;
 
 			//draw all cells with all walls first
 			for (int i = 0; i < mazeRows; i++) {
 				for (int j = 0; j < mazeCols; j++) {		
 					cell = maze.getCell(i,j);
 					
 					if(cell!=null)
 						drawCell(c, cell, cell.fullDraw);
 				}
 			}
 			
 			//change some cell to not get fully draw
 			if(!progressiveDrawDone){
 				if(progressiveFullDrawCount < mazeRows * mazeCols - progressiveDrawStep){
 					for(int i=0;i<progressiveDrawStep;){
 						cell = maze.getCell(random.nextInt(mazeRows), random.nextInt(mazeCols));
 						
 						if(cell!=null){
 							if(cell.fullDraw){
 								cell.fullDraw = false;
 								i++;
 								progressiveFullDrawCount++;
 							}
 						}
 					}
 				} else {
 					for (int i = 0; i < mazeRows; i++) {
 						for (int j = 0; j < mazeCols; j++) { 					
 							maze.cells[i][j].fullDraw = false;
 						}
 					}
 					
 					progressiveDrawDone = true;
 				}
 			} else {
 				drawCpuMove(c);
 			}
 		}
 		
 		void drawCpuMove(Canvas c){
 			int cellSize = getCellSize(c);
 			int horizontalOffset = getHorizontalOffet(c, cellSize);
 			int verticalOffset = getVerticalOffet(c, cellSize);
 			
 			Cell cpuCell = maze.getCell(maze.cpu.pos);
 			
 			if (cpuCell != null) {
 				maze.cpu.pos = new Point(cpuCell.pos.x, cpuCell.pos.y);
 
 				// draw cpu
 				c.drawCircle(cpuCell.pos.y * cellSize + horizontalOffset + maze.cpu.offset.x + 1, cpuCell.pos.x * cellSize + verticalOffset
 						+ maze.cpu.offset.y + 1, maze.cpu.radius, maze.cpu.paint);
 
 				// draw cpus track;
 				if (maze.cpu.track.size() > 1) {
 					for (int i = 0; i < maze.cpu.track.size() - 1; i++) {
 						Point pathFrom = new Point(maze.cpu.track.get(i).y * cellSize + horizontalOffset + cellSize / 2, maze.cpu.track.get(i).x
 								* cellSize + verticalOffset + cellSize / 2);
 						Point pathTo = new Point(maze.cpu.track.get(i + 1).y * cellSize + horizontalOffset + cellSize / 2,
 								maze.cpu.track.get(i + 1).x * cellSize + verticalOffset + cellSize / 2);
 
 						drawLine(c, pathFrom, pathTo, maze.cpu.pathPaint);
 					}
 				}
 				
 				if (!maze.solved) {
 					maze.cpuNextMove();
 					mazeMoves++;
 				} else {
 					mazeSolved++;
 					mazeTotalMoves += mazeMoves;
 					mazeMoves = 0;
 					mazeAverageMoves = mazeSolved > 0 ? mazeTotalMoves / mazeSolved : 0;
 					generateMaze();
 				}
 			}
 		}
 
 		void drawCell(Canvas c, Cell cell, boolean drawFull){
 			Point topLeft, topRight, bottomLeft, bottomRight;
 			
 			if (cell != null) {
 				int i = cell.pos.x;
 				int j = cell.pos.y;
 												
 				// size of cell
 				int cellSize = getCellSize(c);
 
 				// how many pixels to move from left and top
 				int horizontalOffset = getHorizontalOffet(c, cellSize);
 				int verticalOffset = getVerticalOffet(c, cellSize);
 				
 				topLeft = new Point(j * cellSize + horizontalOffset, i * cellSize + verticalOffset);
 				topRight = new Point((j + 1) * cellSize + horizontalOffset, i * cellSize + verticalOffset);
 				bottomLeft = new Point(j * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);
 				bottomRight = new Point((j + 1) * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);
 
 				// determine which wall gets drawn
 				if (cell.walls.get(CellNeighbor.TOP) || drawFull) {
 					drawLine(c, topLeft, topRight, maze.cellPaint);
 				}
 
 				if (cell.walls.get(CellNeighbor.LEFT) || drawFull) {
 					drawLine(c, topLeft, bottomLeft, maze.cellPaint);
 				}
 
 				if ((cell.walls.get(CellNeighbor.RIGHT) && j == maze.columns - 1) || drawFull) {
 					drawLine(c, topRight, bottomRight, maze.cellPaint);
 				}
 
 				if ((cell.walls.get(CellNeighbor.BOTTOM) && i == maze.rows - 1) || drawFull) {
 					drawLine(c, bottomLeft, bottomRight, maze.cellPaint);
 				}
 
 				// this cell is the destination
 				if (cell.isEnd) {
 					c.drawRect(topLeft.x + maze.cellStrokeWidth, topLeft.y + maze.cellStrokeWidth, bottomRight.x - maze.cellStrokeWidth,
 							bottomRight.y - maze.cellStrokeWidth, maze.endCellPaint);
 				}
 				// a cell visited by the play but not on his track to destination
 				else if (cell.cpuVisited && !maze.cpu.track.contains(cell.pos)) {
 					c.drawRect(topLeft.x + maze.cellStrokeWidth + 3, topLeft.y + maze.cellStrokeWidth + 3, bottomRight.x
 							- maze.cellStrokeWidth - 3, bottomRight.y - maze.cellStrokeWidth - 3, maze.cpuVisitedCellPaint);
 					// set cpu at the starting cell
 				} else if (cell.isStart && maze.cpu != null && maze.cpu.pos == null) {
 					cell.cpuVisited = true;
 					maze.cpu.pos = new Point(cell.pos.x, cell.pos.y);
 					maze.cpu.track.push(new Point(cell.pos.x, cell.pos.y));
 				} 
 				
 				if(this.debug) {
 					c.drawText(cell.possibleNeighbor + "", topLeft.x + 6f, topLeft.y + 12f, this.debugPaint);
 					
 					if(cell.pos.x == 0)
 						c.drawText(cell.pos.y+"", topLeft.x+6f, topLeft.y-12f, this.debugPaint);
 					
 					if(cell.pos.y == 0)
 						c.drawText(cell.pos.x+"", topLeft.x-17f, topLeft.y+12f, this.debugPaint);
 				}
 			}
 		}
 		
 		int getCellSize(Canvas c){
 			int cWidth = c.getWidth();
 			int cHeight = c.getHeight();
 			
			return cWidth < cHeight ? cWidth / mazeRows : cHeight / mazeRows;
 		}
 		
 		int getHorizontalOffet(Canvas c, int cellSize){
 			int cWidth = c.getWidth();
 
 			return cWidth > cellSize * mazeCols ? (cWidth - (cellSize * mazeCols)) / 2 : 0;
 		}
 		
 		int getVerticalOffet(Canvas c, int cellSize){
 			int cHeight = c.getHeight();
 			
 			return cHeight > cellSize * mazeRows ? (cHeight - (cellSize * mazeRows)) / 2 : 40;
 		}
 		
 		void drawLine(Canvas c, Point from, Point to, Paint p) {
 			c.drawLine(from.x, from.y, to.x, to.y, p);
 		}
 	}
 }
