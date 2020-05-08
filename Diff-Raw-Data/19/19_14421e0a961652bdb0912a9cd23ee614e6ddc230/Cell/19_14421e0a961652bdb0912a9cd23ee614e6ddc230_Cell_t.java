 package com.shval.jnpgame;
 
 import static com.shval.jnpgame.Globals.*;
 
 import com.shval.jnpgame.PhysicalCell;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.utils.Disposable;
 
 
 public class Cell implements Disposable  {
 	
 	private PhysicalCell physicalCell;
 	private static final String TAG = Cell.class.getSimpleName();
 	
 	private Texture rawTexture;	// the actual bitmap
 	private TextureRegion textureRegions[][]; // each cell composed of 2 x 2 texture regions
 	private TextureRegion anchorTextureRegions[]; // each cell can anchor 4 adjacent cells
 	private int x;			// the X coordinate (in the board cells matrix)
 	private int y;			// the Y coordinate 	"  "
 	private float dxFromMilestone;
 	private float dyFromMilestone;
 	private Speed speed;	// the speed with its directions
 	private Jelly jelly;
 	public int anchoredTo;
 	private int type;
 	private boolean scanFlag; // was this cell encountered in current board scanning
 	private static final float CELL_SIZE = 100; // 
 	private static final float SPEED = 500;
 	private static final float GRAVITY = -4000;
 	static private float graphicWidth;
 	static private float graphicHeight;
 	
 	Cell emerging;
 	int emergingTo; // valid only for emerging cell
 	Sprite emergingSprite;
 	
 	public Cell(Cell other) {
 		this.type = other.type;
 		this.rawTexture = other.rawTexture;
 		this.anchoredTo = other.anchoredTo;
 		this.x = other.x;
 		this.y = other.y;
 		this.jelly = other.jelly; // we assume that jellify() will rewrite this
 		
 		textureRegions = other.textureRegions;
 		anchorTextureRegions = new TextureRegion[4];
		for (int i=0; i < 4; i++)
 			anchorTextureRegions[i] = other.anchorTextureRegions[i];
		this.emergingSprite = other.emergingSprite;
 		speed = new Speed(other.getSpeed()); // creates stale cells only
 		
 		this.emergingTo = other.emergingTo;
 		if (other.emerging == null)
 			this.emerging = null;
 		else
 			this.emerging = new Cell(other.emerging);
 	}
 	
 	private Cell(int x, int y) {
 		this.type = WALL; // this is very comfortable for drawing board borders
 		this.rawTexture = null;
 		this.anchoredTo = NONE; 
 		this.x = x;
 		this.y = y;
 		this.jelly = null;
 		
 		textureRegions = null;
 		anchorTextureRegions = null;
 		speed = null;
 		emergingTo = NONE;
 	}
 	
 	private Cell(int x, int y, BoardConfig config) {
 		Gdx.app.debug(TAG, "Creating cell " + x + ", " + y);
 		this.type = config.getType(x, y);
 		this.rawTexture = config.getTexture(x, y);
 		this.anchoredTo = config.getAncoredTo(x, y);
 		this.x = x;
 		this.y = y;
 		this.jelly = null;
 		this.emergingTo = NONE;
 		
 		textureRegions = new TextureRegion[2][2];
 		anchorTextureRegions = new TextureRegion[4]; // TODO: all nulls?
 		
 		if (rawTexture != null) {
 			textureRegions[0][0] = new TextureRegion(rawTexture, 8 + 1 * 48, 8 + 4 * 48 + 48 / 2, 48 / 2, 48 / 2);
 			textureRegions[0][1] = new TextureRegion(rawTexture, 8 + 1 * 48, 8 + 0 * 48, 48 / 2, 48 / 2);
 			textureRegions[1][0] = new TextureRegion(rawTexture, 8 + 3 * 48 + 48 / 2, 8 + 4 * 48 + 48 / 2, 48 / 2, 48 / 2);
 			textureRegions[1][1] = new TextureRegion(rawTexture, 8 + 3 * 48 + 48 / 2, 8 + 0 * 48, 48 / 2, 48 / 2);
 		}
 
 		speed = new Speed(0, 0);
 
 		if (x < 0 || y < 0) { // emerging cell
 			this.emerging = null;
			this.anchoredTo = config.getAncoredTo(x, y);
 			this.emergingTo = config.getEmergingTo(x, y);
			
			Gdx.app.debug(TAG, "emerging cell emerging to " + emergingTo + " anchored to " + anchoredTo);
 		}
 		else if (x > 0 || y > 0) { // 0, 0 cannot hold emerging cell
 			this.emerging = createCell(-x, -y, config);
 			this.emergingSprite = config.getEmergingSprite(-x, -y);
 		}
 		
 	}
 	
 	
 	// factory of the cell,
 	// if no config is given, then a dummy cell is created
 	public static Cell createCell(int x, int y, BoardConfig config) {
 		
 		if (config == null) {
 			return new Cell(x, y);
 		}
 		
 		if (config.getType(x, y) == NONE)
 			return null;
 
 		Cell cell = new Cell(x, y, config);
 		return cell;
 	}
 	
 	public Speed getSpeed() {
 		return speed;
 	}
 	
 	public boolean isBlack() {
 		return isBlack(type);
 	}
 
 	public static boolean isWall(int cellType) {
 		return cellType == WALL;
 	}
 	
 	public static boolean isBlack(int cellType) {
 		return cellType >= JELLY_BLACK_MIN;
 	}
 	public Texture getRawTexture() {
 		return rawTexture;
 	}
 	
 	private int[] getTextureLocation(int topology) {
 		// target cell - lower right cell
 		int i, j;
 		switch (topology) {
 	
 			// 00	
 			// 0x		
 		case (0):
 	
 			// x0	
 			// 0x		
 		case (2):
 		
 			i = 1;
 			j = 0;
 			break;
 	
 			// 0x	
 			// 0x		
 		case (1):
 			i = 0;
 			j = 3;
 			break;
 	
 			// xx	
 			// 0x		
 		case (3):
 			i = 1;
 			j = 4;
 			break;
 			
 			// 00	
 			// xx		
 		case (4):
 			i = 2;
 			j = 0;
 			break;
 			
 			// 0x	
 			// xx		
 		case (5):
 			i = 1;
 			j = 1;
 			break;
 			
 			// x0	
 			// xx		
 		case (6):
 			i = 4;
 			j = 1;
 			break;
 			
 			// xx	
 			// xx		
 		case (7):
 			i = 2;
 			j = 2;
 			break;
 		default:
 			Gdx.app.error(TAG, "Invalid topology");
 			return null;
 		}
 		
 		int ret[] = new int[2];
 		ret[0] = i;
 		ret[1] = j;
 		return ret;			
 	}
 	
 	public void setNeighbours() {
 
 		int i, j;
 		int location[];
 		Cell c;
 		int t0, t1, t2;
 		int topology;
 
 		Gdx.app.debug(TAG, "Setting neighbors for cell: " + x + ", " + y );
 		Board board = jelly.getBoard();
 					
 		// region:
 		// x 0
 		// 0 0
 		
 		Vector2 bottomLeft[][] = new Vector2[2][2];
 		
 		c = board.getCell(x - 1, y);
 		t0 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x - 1, y + 1);
 		t1 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x, y + 1);
 		t2 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		topology = 4 * t0 + 2 * t1 + 1 * t2;	
 		location = getTextureLocation(topology);
 		i = location[0];
 		j = location[1];
 		
 		
 		//Gdx.app.debug(TAG, "Setting neighbors: " + x + ", " + y + " : topology = " + topology
 //						+ ", i = " + i + ", j= " + j);
 		textureRegions[0][1] = new TextureRegion(rawTexture, 8 + i * 48, 8 + j * 48, 48 / 2, 48 / 2);		
 		bottomLeft[0][1] = new Vector2((8 + i * 48)/256f, (24 + 8 + j * 48)/256f);
 				
 		// region:
 		// 0 x
 		// 0 0
 		
 		c = board.getCell(x + 1, y);
 		t0 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x + 1, y + 1);
 		t1 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x, y + 1);
 		t2 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		topology = 4 * t0 + 2 * t1 + 1 * t2;	
 		location = getTextureLocation(topology);
 		i = 4 - location[0];
 		j = location[1];
 		
 		textureRegions[1][1] = new TextureRegion(rawTexture, 8 + i * 48 + 48 / 2, 8 + j * 48, 48 / 2, 48 / 2);		
 		bottomLeft[1][1] = new Vector2((8 + i * 48 + 48 / 2)/256f, (24 + 8 + j * 48)/256f);
 		
 		// region:
 		// 0 0
 		// x 0
 		
 		c = board.getCell(x - 1, y);
 		t0 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x - 1, y - 1);
 		t1 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x, y - 1);
 		t2 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		topology = 4 * t0 + 2 * t1 + 1 * t2;	
 		location = getTextureLocation(topology);
 		i = location[0];
 		j = 4 - location[1];
 		
 		//Gdx.app.debug(TAG, "Setting neighbours: " + x + ", " + y + " : topology = " + topology
 			//	+ ", i = " + i + ", j= " + j);
 
 		textureRegions[0][0] = new TextureRegion(rawTexture, 8 + i * 48, 8 + j * 48 + 48 / 2, 48 / 2, 48 / 2);				
 		bottomLeft[0][0] = new Vector2((8 + i * 48)/256f, (24 + 8 + j * 48 + 48 / 2)/256f);
 		
 		// region:
 		// 0 0
 		// 0 x
 		
 		c = board.getCell(x + 1, y);
 		t0 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x + 1, y - 1);
 		t1 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		c = board.getCell(x, y - 1);
 		t2 = (c != null && c.getType() == type && c.getJelly() == jelly) ? 1 : 0;
 		
 		topology = 4 * t0 + 2 * t1 + 1 * t2;	
 		location = getTextureLocation(topology);
 		i = 4 - location[0];
 		j = 4 - location[1];
 		
 		textureRegions[1][0] = new TextureRegion(rawTexture, 8 + i * 48 + 48 / 2, 8 + j * 48 + 48 / 2, 48 / 2, 48 / 2);
 		bottomLeft[1][0] = new Vector2((8 + i * 48 + 48 / 2) / 256f, (24 + 8 + j * 48 + 48 / 2) / 256f);
 
 		Gdx.app.debug(TAG, "Setting texture bottom left to " + x + ", " + y);
 		if (PHYSICS_SUPPORTED)
 			physicalCell.setTextureBL(bottomLeft);
 		
 		
 		// anchoring
 		
 		Texture anchors[] = new Texture[4];
 		
 		c = board.getCell(x - 1, y);
 		if (c != null && c.anchoredTo == RIGHT) {
 			anchorTextureRegions[LEFT] = 
 					new TextureRegion(c.getRawTexture(), 27, 30, 24, 18);
 			anchors[LEFT] = c.getRawTexture();
 		}
 
 		c = board.getCell(x + 1, y);
 		if (c != null && c.anchoredTo == LEFT) {
 			anchorTextureRegions[RIGHT] = 
 					new TextureRegion(c.getRawTexture(), 1, 5, 24, 18);
 			anchors[RIGHT] = c.getRawTexture();
 		}
 		
 		c = board.getCell(x, y + 1);
 		if (c != null && c.anchoredTo == DOWN) {
 			anchorTextureRegions[UP] = 
 					new TextureRegion(c.getRawTexture(), 4, 27, 18, 24);
 			anchors[UP] = c.getRawTexture();
 		}
 
 		c = board.getCell(x, y - 1);
 		if (c != null && c.anchoredTo == UP) {
 			anchorTextureRegions[DOWN] = 
 					new TextureRegion(c.getRawTexture(), 30, 1, 18, 24);
 			anchors[DOWN] = c.getRawTexture();
 		}
 
 		if (PHYSICS_SUPPORTED)
 			physicalCell.setAnchors(anchors);
 	}
 	
 	public static void setResolution(float spriteWidth, float spriteHeight) {
 		Gdx.app.debug(TAG, "Setting resolution to " + spriteWidth + ", " + spriteHeight);
 		// set width only on first call
 		Cell.graphicWidth = spriteWidth;
 		Cell.graphicHeight = spriteHeight;
 
 	}
 	
 	void createPhysicalCell() {
 		if (!PHYSICS_SUPPORTED)
 			return;
 		//Gdx.app.debug(TAG, "Creating phy-cell");
 		float rows = jelly.getBoard().getRows();
 		float cols = jelly.getBoard().getCols();
 		Vector2 textureSize = new Vector2(24f/256f, 24f/256f);
 		Vector2 PhySize = new Vector2(PlayScreen.worldWidth / cols,
 									  PlayScreen.worldHeight / rows);
 		PhysicalCell.SetSize(textureSize, PhySize);
 		Vector2 pos = new Vector2(PlayScreen.worldWidth * x / cols,
 								  PlayScreen.worldHeight * y / rows);
 		
 		Gdx.app.debug(TAG, "Creating phy-cell " + x + ", " + y + ": pos = " + pos.x + ", " + pos.y +
 							" size = " + PhySize.x + ", " + PhySize.y);
 
 		// create it static
 		BodyType type;
 		if (this.type == WALL)
 			type = BodyType.StaticBody;
 		else
 			type = BodyType.DynamicBody;
 
 		destroyPhysical();
 		physicalCell = new PhysicalCell(pos, rawTexture, jelly.getBoard().getWorld(), type);
 	}
 	
 	public void resetScanFlag() {
 		scanFlag = false;
 	}
 	
 	public Jelly getJelly() {
 		return jelly;
 	}
 
 	void setJelly(Jelly newJelly) {
 		this.jelly = newJelly;
 	}
 	
 	public int getX() {
 		return x;
 	}
 	public void setX(int x) {
 		this.x = x;
 	}
 	public int getY() {
 		return y;
 	}
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	public void render(SpriteBatch spriteBatch, int layer) {
 		//Gdx.app.debug(TAG, "rendering cell");
 
 		if (layer == 0) { // emerging cells
 			if (Board.renderMode == 2) // for now
 				if (this.emerging != null)
 					this.emerging.render(spriteBatch, 1);
 		}
 		
 		if (layer == 1) {
 			if (Board.renderMode == 0)
 				physicalCell.render();
 			else
 				renderCell(spriteBatch);
 		}
 			
 		if (layer == 2) {
 			if (Board.renderMode == 0)
 				physicalCell.renderAnchors();
 			else
 				renderAnchors(spriteBatch);
 		}
 			
 	}
 	
 	private void renderCell(SpriteBatch spriteBatch) {
 		//canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
 		float graphicDx, graphicDy;
 		if (speed.getXv() != 0)
 			graphicDx = (graphicWidth * dxFromMilestone) / CELL_SIZE;
 		else
 			graphicDx = 0;
 		
 		if (speed.getYv() != 0)
 			graphicDy = (graphicHeight * (int) dyFromMilestone) / CELL_SIZE;
 		else
 			graphicDy = 0;
 		
 		float graphicX = graphicWidth * x + graphicDx;
 		float graphicY = graphicHeight * y + graphicDy;
 		/*
 		if(false) {
 			Gdx.app.debug(TAG, "(" + x + ", " + y + "): dx, dy = " + dxFromMilestone + ", " + dyFromMilestone);
 			Gdx.app.debug(TAG, "(" + x + ", " + y + "): dx, dy = " + dxFromMilestone + ", " + dyFromMilestone);
 			Gdx.app.debug(TAG, "(" + x + ", " + y + "): Gdx, Gdy = " + graphicDx + ", " + graphicDy);
 			Gdx.app.debug(TAG, "(" + x + ", " + y + "): rendering at (" + graphicX + ", " + graphicY + ")");
 		}
 		*/
 		spriteBatch.draw(textureRegions[0][0], graphicX, graphicY,
 				graphicWidth / 2, graphicHeight / 2);
 		
 		spriteBatch.draw(textureRegions[0][1], graphicX, graphicY + graphicHeight / 2,
 				graphicWidth / 2, graphicHeight / 2);
 		
 		spriteBatch.draw(textureRegions[1][0], graphicX + graphicWidth / 2, graphicY,
 				graphicWidth / 2, graphicHeight / 2);
 		
 		spriteBatch.draw(textureRegions[1][1], graphicX + graphicWidth / 2, graphicY + graphicHeight / 2,
 				graphicWidth / 2, graphicHeight / 2);
 		
 	}
 	
 	public void renderAnchors(SpriteBatch spriteBatch) {
 		float graphicDx, graphicDy;
 		if (speed.getXv() != 0)
 			graphicDx = (graphicWidth * dxFromMilestone) / CELL_SIZE;
 		else
 			graphicDx = 0;
 		
 		if (speed.getYv() != 0)
 			graphicDy = (graphicHeight * dyFromMilestone) / CELL_SIZE;
 		else
 			graphicDy = 0;
 		
 		float graphicX = graphicWidth * x + graphicDx;
 		float graphicY = graphicHeight * y + graphicDy;
 
 		// render anchors
 		// add 1 to create anchor continuity 
 		
 		float anchorWidth = graphicWidth * 3 / 8;
 		float anchorHeight = graphicHeight / 2;
 		
 		if (anchorTextureRegions[DOWN] != null)
 			spriteBatch.draw(anchorTextureRegions[DOWN], 
 					graphicX + graphicWidth / 2 - anchorWidth / 2, graphicY - 1,
 					anchorWidth, anchorHeight);
 
 		if (anchorTextureRegions[UP] != null)
 			spriteBatch.draw(anchorTextureRegions[UP], 
 					graphicX + graphicWidth / 2 - anchorWidth / 2, graphicY + graphicHeight - anchorHeight + 1,
 					anchorWidth, anchorHeight);
 
 		anchorWidth = graphicWidth / 2;
 		anchorHeight = graphicHeight * 3 / 8;
 
 		if (anchorTextureRegions[LEFT] != null)
 			spriteBatch.draw(anchorTextureRegions[LEFT], 
 					graphicX - 1, graphicY + graphicHeight / 2 - anchorHeight / 2,
 					anchorWidth, anchorHeight);
 
 		if (anchorTextureRegions[RIGHT] != null)
 			spriteBatch.draw(anchorTextureRegions[RIGHT],
 					graphicX + graphicWidth - anchorWidth + 1, graphicY + graphicHeight / 2 - anchorHeight / 2,
 					anchorWidth, anchorHeight);
 		
 		
 		if (emerging != null) {
 			//emergingSprite.setPosition(graphicX, graphicY);
 			float angle = 0;
 			float gX = graphicX;
 			float gY = graphicY;
 			float gW = 0;
 			float gH = 0;			
 			
 			if(this.emerging.emergingTo == LEFT) {
 				angle = 0;
 				gW = graphicWidth * 1 / 6;
 				gH = graphicHeight;
 				if(this.emerging.anchoredTo != NONE) {
 					gW = gW * 3f;
 				}
 			}
 			
 			if(this.emerging.emergingTo == RIGHT) {
 				angle = 0;
 				gW = graphicWidth * 1 / 6;
 				gH = graphicHeight;
 				gX += graphicWidth * 5 / 6;
 				if(this.emerging.anchoredTo != NONE) {
 					gX += gW;
 					gW = gW * 3f;
 					gX -= gW;
 				}
 			}
 			if(this.emerging.emergingTo == UP) {
 				angle = -90;
 				gY += graphicHeight;
 				gW = graphicHeight * 1 / 6;
 				if(this.emerging.anchoredTo != NONE) {
 					//gY += gW;
 					gW = gW * 3f;
 					//gY -= gW;
 				}
 				gH = graphicWidth;
 			}
 			
 
 			
 			
 			spriteBatch.draw(emergingSprite, gX, gY, 0, 0, gW, gH, 1f, 1f, angle);
 			
 		}
 	}
 
 	/**
 	 * Method which update the internal state every tick
 	 */
 	// on every tick, update returns true if reached new milestone (x, y)
 
 	
 	boolean update(float delta) {
 		boolean isMilestone = false;
 		
 		if (this.emerging != null) {
 			isMilestone |= this.emerging.update(delta);
 		}
 			
 		if(!isMoving())
 			return false; // no milestone and nothing to update
 		
 		// update speed & location
 		float newDx = dxFromMilestone + (speed.getXv() * delta);
 		float newDy = dyFromMilestone + (speed.getYv() * delta);
 		
 		// gravity
 		float Yv = speed.getYv();
 		if (Yv < 0)
 			speed.setYv(Yv + GRAVITY * delta);
 		
 		//Gdx.app.debug(TAG, "Yv = " + Yv);
 		//Gdx.app.debug(TAG, "(" + x + ", " + y + "): new (dx, dy) = (" + newDx + ", " + newDy + ")");
 		
 		// milestone reached?
 		
 		if (Math.abs(newDx) >= CELL_SIZE) {
 			isMilestone = true;
 			if (newDx > 0)
 				x = x + 1;
 			else
 				x = x - 1;
 		}
 		
 		if (Math.abs(newDy) >= CELL_SIZE) {
 			isMilestone = true;
 			if (newDy > 0)
 				y = y + 1;
 			else
 				y = y - 1;
 		}	 
 		
 		if (isMilestone) {
 			Gdx.app.debug(TAG, "Milestone reached new: (" + x + "," + y + ")");			
 			dxFromMilestone = 0;
 			dyFromMilestone = 0;
 		} else {
 			dxFromMilestone  = newDx;
 			dyFromMilestone  = newDy;
 		}
 		
 		if (emergingTo != NONE) // this is an emerging cell
 			if (isMilestone) {
 				this.stopVertical();
 				emergingTo = NONE; // this will tell the board that the cell emerged
 			}
 		return isMilestone;
 	}
 
 	/**
 
 	 * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the 
 	 * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
 	 * @param eventX - the event's X coordinate
 	 * @param eventY - the event's Y coordinate
 	 */
 	public void handleActionDown(int eventX, int eventY) {};
 	
 	public boolean canMove(int dir) {
 		if (type == WALL)
 			return false;
 		if (scanFlag)
 			return true;
 		scanFlag = true;
 		return jelly.canMove(dir);
 	}
 	
 	/*
 	public boolean getIsFixed() {
 		return isFixed;
 	}
 	*/
 	
 	public void move(int dir) {
 		if (scanFlag)
 			return;
 		scanFlag = true;
 		
 		
 		if(type == WALL) {
 			Gdx.app.error(TAG, "Trying to move a wall");
 		}
 		
 		
 		float vx = 0, vy = 0;
 		float oldVy = speed.getYv();
 		switch (dir) {
 		case LEFT:
 			vx = -SPEED;
 			if (PHYSICS_SUPPORTED)
 				physicalCell.move(LEFT);
 			break;
 		case RIGHT:		
 			vx = SPEED;
 			if (PHYSICS_SUPPORTED)
 				physicalCell.move(RIGHT);
 			break;
 		case DOWN:
 			if (oldVy == 0)
 				vy = -SPEED / 100; // gravity will take it from here
 			else
 				vy = oldVy; // again, gravity ...
 			if (PHYSICS_SUPPORTED)
 				physicalCell.move(DOWN);
 			break;
 		case UP:
 			vy = SPEED;
 			if (PHYSICS_SUPPORTED)
 				physicalCell.move(UP);
 			break;
 		default:
 			// should never be here
 			return;
 		}
 		speed.set(vx, vy);
 		
 		//Gdx.app.debug(TAG, "x, y = " + x + ", " + y + " vx, vy = " + vx + ", " + vy);
 		// move the whole parent jelly
 		if (jelly != null) // this is done for emerging cells which dont have jelly
 			jelly.move(dir);
 	}
 
 	void stop() {
 		speed.setXv(0);
 		speed.setYv(0);
 	}
 	
 	void stopHorizontal() {
 		speed.setXv(0);
 	}
 
 	void stopVertical() {
 		//Gdx.app.debug(TAG, "x, y = " + x + ", " + y + ". Stopping vertically");
 		speed.setYv(0);
 	}
 	
 	
 	boolean isMoving() {
 		return (speed.getXv() != 0 || speed.getYv() != 0);
 	}
 	
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public void destroyPhysical() {
 		if (physicalCell != null)
 			physicalCell.destroy();
 	}
 
 	public void fixPeer(Cell peer, int dir) {
 		if (physicalCell != null)
 			physicalCell.fixPeer(peer.physicalCell, dir);
 	}
 
 	public void anchorPeer(Cell peer, int dir) {
 		if (physicalCell != null)
 			physicalCell.anchorPeer(peer.physicalCell, dir);
 	}
 	
 	public void dispose() {
 		//rawTexture.dispose();
 		
 		if (PHYSICS_SUPPORTED)
 			physicalCell.dispose();
 		// TODO: is that it ? how about the regions?
 	}
 	
 	public void emerge() {
 		if (emerging == null) {
 			Gdx.app.error(TAG, "Trying to emerge. No emerge defined!!\n");
 			return;
 		}
 		
 		// some things before emerging 
 		Cell emerging = this.emerging;
 
 		Jelly jelly = new Jelly(this.jelly.getBoard());
 		emerging.setJelly(jelly);
 		jelly.join(emerging);
 		emerging.createPhysicalCell();
 		emerging.setX(x);
 		emerging.setY(y);
 		emerging.move(this.emerging.emergingTo);
 		
 	}
 }
