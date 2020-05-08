 package com.matthewtole.androidrise.game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.RectF;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 import com.matthewtole.androidrise.game.enums.GamePlayer;
 import com.matthewtole.androidrise.game.pieces.Tile;
 import com.matthewtole.androidrise.game.pieces.Tower;
 import com.matthewtole.androidrise.game.pieces.Worker;
 import com.matthewtole.androidrise.lib.GameUpdate;
 import com.matthewtole.androidrise.lib.GridLocation;
 import com.matthewtole.androidrise.lib.RiseGame;
 import com.matthewtole.androidrise.lib.ScreenLocation;
 import com.matthewtole.androidrise.lib.Utils;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 
 	private static final String TAG = GameView.class.getSimpleName();
 
 	private final float DRAG_START_AMOUNT = 30;
 
 	private GameThread thread;
 	private int surfaceWidth = 0;
 	private int surfaceHeight = 0;
 	private int sidebarWidth = 250;
 
 	private RiseGame game;
 
 	private SpriteManager spriteManager;
 	private HashMap<String, Paint> paints;
 
 	private float offsetX = 0;
 	private float offsetY = 0;
 
 	private ArrayList<Tile> tiles;
 	private ArrayList<Tower> towers;
 	private ArrayList<Worker> workers;
 	private boolean listLockout = false;
 
 	private float dragStartX = 0;
 	private float dragStartY = 0;
 	private boolean isDragging = false;
 
 	private int sleepCounter = 50;
 
 	private char[][] layout;
 
 	private ScreenLocation centerLocation;
 
 	private RectF sidebarRectangle;
 
 	public GameView(Context context) {
 		super(context);
 		this.getHolder().addCallback(this);
 		this.setFocusable(true);
 
 		SoundManager.getInstance();
 		SoundManager.initSounds(context);
 		SoundManager.loadSounds();
 
 		this.spriteManager = new SpriteManager(context);
 
 		this.tiles = new ArrayList<Tile>();
 		this.towers = new ArrayList<Tower>();
 		this.workers = new ArrayList<Worker>();
 
 		this.loadLayout("the_pit");
 
 		this.paints = new HashMap<String, Paint>();
 		this.makePaints();
 	}
 
 	private void makePaints() {
 		this.paints.put("sidebarBackground", new Paint());
 		this.paints.get("sidebarBackground").setColor(
 				Color.parseColor("#333333"));
 		this.paints.get("sidebarBackground").setStyle(Style.FILL);
 		this.paints.get("sidebarBackground").setAntiAlias(true);
 
 		this.paints.put("finishBackground", new Paint());
 		this.paints.get("finishBackground").setColor(
 				Color.parseColor("#333333"));
 		this.paints.get("finishBackground").setStyle(Style.FILL);
 		this.paints.get("finishBackground").setAntiAlias(true);
 		this.paints.get("finishBackground").setAlpha(200);
 
 		this.paints.put("text", new Paint());
 		this.paints.get("text").setColor(Color.WHITE);
 		this.paints.get("text").setTextSize(48);
 
 		Paint lineMaster = new Paint();
 		lineMaster.setColor(Color.WHITE);
 		lineMaster.setAntiAlias(true);
 		lineMaster.setStyle(Style.FILL);
 
 		this.paints.put("sidebarLineMiddle", new Paint(lineMaster));
 		this.paints.put("sidebarLineRed", new Paint(lineMaster));
 		this.paints.get("sidebarLineRed").setColor(Color.parseColor("#E72D18"));
 		this.paints.put("sidebarLineBlue", new Paint(lineMaster));
 		this.paints.get("sidebarLineBlue")
 				.setColor(Color.parseColor("#4B56CE"));
 		this.paints.put("sidebarLineDisabled", new Paint(lineMaster));
 		this.paints.get("sidebarLineDisabled").setColor(
 				Color.parseColor("#222222"));
 	}
 
 	private void buildInitialLayout() {
 
 		int layoutOffsetX = 30 - this.layout.length / 2;
 		int layoutOffsetY = 30 - this.layout[1].length / 2;
 		if (layoutOffsetX % 2 == 1) {
 			layoutOffsetX -= 1;
 		}
 		if (layoutOffsetY % 2 == 1) {
 			layoutOffsetY -= 1;
 		}
 
 		ScreenLocation redPos = null, bluePos = null;
 
 		for (int x = 0; x < this.layout.length; x += 1) {
 			for (int y = 0; y < this.layout[x].length; y += 1) {
 				char c = this.layout[x][y];
 				GridLocation loc = new GridLocation(layoutOffsetX + x,
 						layoutOffsetY + y);
 
 				if (c == 'R' || c == 'O' || c == 'B') {
 					Tile t = new Tile(this.spriteManager);
 					t.setLocation(loc);
 					this.tiles.add(t);
 
 					if (c == 'R') {
 						Worker w = new Worker(this.spriteManager,
 								GamePlayer.RED);
 						w.setLocation(loc);
 						while (listLockout) {
 						}
 						listLockout = true;
 						this.workers.add(w);
 						listLockout = false;
 						redPos = new ScreenLocation(loc);
 					} else if (c == 'B') {
 						Worker w = new Worker(this.spriteManager,
 								GamePlayer.BLUE);
 						w.setLocation(loc);
 						while (listLockout) {
 						}
 						listLockout = true;
 						this.workers.add(w);
 						listLockout = false;
 						bluePos = new ScreenLocation(loc);
 					}
 				}
 			}
 		}
 
 		if (redPos != null && bluePos != null) {
 			this.centerLocation = new ScreenLocation(
 					(redPos.getScreenX() + bluePos.getScreenX()) / 2
 							+ Common.TILE_WIDTH_HALF,
 					(redPos.getScreenY() + bluePos.getScreenY()) / 2
 							+ Common.TILE_HEIGHT_HALF);
 		}
 	}
 
 	private boolean loadLayout(String name) {
 
 		try {
 			String layoutString = Utils.readTextAsset(this.getContext(), name);
 			if (layoutString == "") {
 				return false;
 			}
 			layoutString = layoutString.replace(",", "");
 			layoutString = layoutString.replace(".", "");
 
 			String[] layoutRows = layoutString.split("\n");
 			this.layout = new char[layoutRows[0].length()][layoutRows.length];
 			for (int r = 0; r < layoutRows.length; r += 1) {
 				String row = layoutRows[r].trim();
 				for (int c = 0; c < row.length(); c += 1) {
 					this.layout[c][r] = row.charAt(c);
 				}
 			}
 			return true;
 
 		} catch (Exception ex) {
 			Log.e("MainView", ex.getMessage());
 		}
 		return false;
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		this.surfaceWidth = width;
 		this.surfaceHeight = height;
 		this.offsetX = -1 * this.centerLocation.getScreenX()
 				+ this.surfaceWidth / 2;
 		this.offsetY = -1 * this.centerLocation.getScreenY()
 				+ this.surfaceHeight / 2;
 
 		this.sidebarRectangle = new RectF(10, 10, sidebarWidth - 10,
 				surfaceHeight - 10);
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 
 		this.game = new RiseGame();
 		this.game.setup(this.layout);
 
 		this.buildInitialLayout();
 
 		this.thread = new GameThread(this.getHolder(), this);
 		if (this.thread == null || !thread.isRunning()) {
 			thread.setRunning(true);
 			thread.start();
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		thread.setRunning(false);
 		boolean retry = true;
 		while (retry) {
 			try {
 				this.thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	public void render(Canvas canvas) {
 		try {
 			canvas.drawColor(Color.BLACK);
 			this.drawBackground(canvas);
 			canvas.save();
 			canvas.translate(this.offsetX, this.offsetY);
 			this.drawTiles(canvas);
 			this.drawPieces(canvas);
 			canvas.restore();
 			this.drawInterface(canvas);
 		} catch (Exception ex) {
 			Log.e(TAG,
 					ex.getMessage() == null ? ex.toString() : ex.getMessage());
 			thread.setRunning(false);
 		}
 	}
 
 	private void drawBackground(Canvas canvas) {
 		for (int x = 0; x < surfaceWidth; x += 256) {
 			for (int y = 0; y < surfaceHeight; y += 256) {
 				canvas.drawBitmap(this.spriteManager.getBitmap("background"),
 						x, y, null);
 			}
 		}
 	}
 
 	private void drawTiles(Canvas canvas) {
 		while (listLockout) {
 		}
 		listLockout = true;
 		for (Tile tile : this.tiles) {
 			tile.draw(canvas);
 		}
 		listLockout = false;
 	}
 
 	private void drawPieces(Canvas canvas) {
 
 		while (listLockout) {
 		}
 		listLockout = true;
 		for (Worker worker : this.workers) {
 			worker.draw(canvas);
 		}
 		for (Tower tower : this.towers) {
 			tower.draw(canvas);
 		}
 		listLockout = false;
 	}
 
 	private void drawInterface(Canvas canvas) {
 		canvas.drawBitmap(this.spriteManager.getBitmap("target"),
 				this.surfaceWidth
 						- this.spriteManager.getBitmap("target").getWidth()
 						- 10, 10, null);
 
 		canvas.drawRoundRect(this.sidebarRectangle, 20.0f, 20.0f,
 				this.paints.get("sidebarBackground"));
 	}
 
 	public void update() {
 
 		if (this.sleepCounter > 0) {
 			this.sleepCounter -= 1;
 			return;
 		}
 
 		while (listLockout) {
 		}
 		listLockout = true;
 		for (Worker worker : this.workers) {
 			worker.update();
 		}
 		for (Tower tower : this.towers) {
 			tower.update();
 		}
 		listLockout = false;
 	}
 
 	private void updateDrawOffset(float x, float y) {
 		this.offsetX += (x - this.dragStartX);
 		this.offsetY += (y - this.dragStartY);
 		this.dragStartX = x;
 		this.dragStartY = y;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		if (this.sidebarRectangle.contains(event.getX(), event.getY())) {
 			this.isDragging = false;
 			return true;
 		}
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			dragStartX = event.getX();
 			dragStartY = event.getY();
 			break;
 		case MotionEvent.ACTION_UP:
 			if (this.isDragging) {
 				updateDrawOffset(event.getX(), event.getY());
 				this.isDragging = false;
 			} else {
 				if (event.getPointerCount() == 1) {
 					this.onGameClick(event.getX(), event.getY());
 				} else {
 					//Log.d(TAG, Utils.coordString(event.getX(), event.getY()));
 				}
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (!this.isDragging
 					&& (Math.abs(event.getX() - dragStartX) > this.DRAG_START_AMOUNT || Math
 							.abs(event.getY() - dragStartY) > this.DRAG_START_AMOUNT)) {
 				this.isDragging = true;
 			}
 			if (this.isDragging) {
 				updateDrawOffset(event.getX(), event.getY());
 			}
 			break;
 		}
 
 		return true;
 	}
 
 	private void onGameClick(float x, float y) {
 
 		int touchX = (int) (x - this.offsetX);
 		int touchY = (int) (y - this.offsetY);
 
 		GridLocation loc = new ScreenLocation(touchX, touchY).toGridLocation();
 		GameUpdate update = this.game.doAction(loc.getGridX(), loc.getGridY(),
 				this.game.getCurrentPlayer());
 
 		if (update.success) {
 			Worker w = findWorkerByLocation(update.location);
 			Worker w2 = findWorkerByLocation(update.locationSecondary);
 			Worker w3 = findWorkerByLocation(update.locationTertiary);
 
 			switch (update.type) {
 			case GameUpdate.WORKER_SELECTED:
 				if (w != null) {
 					w.setSelected(true);
 				}
 				break;
 			case GameUpdate.WORKER_UNSELECTED:
 				if (w != null) {
 					w.setSelected(false);
 				}
 				break;
 			case GameUpdate.WORKER_MOVED:
 				if (w != null) {
 					w.setLocation(update.locationSecondary, false);
 					w.setSelected(false);
 				}
 				break;
 			case GameUpdate.WORKER_ADDED: {
 				Worker newWorker = new Worker(spriteManager,
 						update.extraInt == RiseGame.BLUE ? GamePlayer.BLUE
 								: GamePlayer.RED);
 				newWorker.setLocation(update.location);
 				while (listLockout) {
 				}
 				listLockout = true;
 				this.workers.add(newWorker);
 				listLockout = false;
 			}
 				break;
 			case GameUpdate.WORKER_JUMP:
 				w.setLocation(update.locationSecondary, false);
 				w.setSelected(false);
 				while (listLockout) {
 				}
 				listLockout = true;
 				this.workers.remove(w3);
 				listLockout = false;
 				break;
 			case GameUpdate.TILE_ADDED: {
 				Tile t = new Tile(spriteManager);
 				t.setLocation(update.location);
 				this.tiles.add(t);
 			}
 				break;
 			case GameUpdate.TOWER_REDUCED: {
 				Tower t = findTowerByLocation(update.location);
 				t.removeLevel();
 			}
 				break;
 			case GameUpdate.TOWER_DEMOLISHED: {
 				Tower t = findTowerByLocation(update.location);
 				while (listLockout) {
 				}
 				listLockout = true;
 				this.towers.remove(t);
 				listLockout = false;
 			}
 				break;
 			case GameUpdate.SACRIFICE_ADD: {
 				if (w == null) {
 					Worker newWorker = new Worker(
 							spriteManager,
 							(w2.getPlayer() == GamePlayer.BLUE) ? GamePlayer.BLUE
 									: GamePlayer.RED);
 					newWorker.setLocation(update.location);
 					while (listLockout) {
 					}
 					listLockout = true;
 					this.workers.add(newWorker);
 					this.workers.remove(w2);
 					this.workers.remove(w3);
 					listLockout = false;
 				}
 			}
 				break;
 			case GameUpdate.SACRIFICE_REMOVE: {
 				while (listLockout) {
 				}
 				listLockout = true;
 				this.workers.remove(w);
 				this.workers.remove(w2);
 				this.workers.remove(w3);
 				listLockout = false;
 			}
 				break;
 			}
 		} else {
 			Context context = this.getContext();
 			CharSequence text = update.failureReason;
 			int duration = Toast.LENGTH_SHORT;
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 		}
 	}
 
 	private Tower findTowerByLocation(GridLocation location) {
 		if (location == null) {
 			return null;
 		}
 
 		while (listLockout) {
 		}
 		listLockout = true;
 		for (Tower t : this.towers) {
 			if (t.getLocation().equals(location)) {
 				listLockout = false;
 				return t;
 			}
 		}
 		listLockout = true;
 		return null;
 	}
 
 	private Worker findWorkerByLocation(GridLocation location) {
 		if (location == null) {
 			return null;
 		}
 
 		while (listLockout) {
 		}
 		listLockout = true;
 		for (Worker w : this.workers) {
 			if (w.getLocation().equals(location)) {
 				listLockout = false;
 				return w;
 			}
 		}
 		listLockout = false;
 		return null;
 	}
 
 }
