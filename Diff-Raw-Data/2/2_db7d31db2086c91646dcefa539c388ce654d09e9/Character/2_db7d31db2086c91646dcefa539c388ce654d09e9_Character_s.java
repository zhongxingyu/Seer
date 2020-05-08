 package com.atlan1.mctpo.mobile;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.LightingColorFilter;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.Log;
 
 import com.atlan1.mctpo.mobile.MCTPO;
 import com.atlan1.mctpo.mobile.Inventory.Inventory;
 import com.atlan1.mctpo.mobile.Inventory.Slot;
 import com.atlan1.mctpo.mobile.Texture.TextureLoader;
 import com.atlan1.mctpo.mobile.Graphics.FlipHelper;
 import com.atlan1.mctpo.mobile.Graphics.Line2d;
 import com.atlan1.mctpo.mobile.Graphics.Point;
 
 public class Character extends DoubleRectangle implements LivingThing{
 	private static Bitmap animationTexture;
 	private static Bitmap animationTextureFlipped;
 	static Bitmap buildOnlyButton;
 	static Bitmap destroyOnlyButton;
 	static Bitmap buildDestroyButton;
 	
 	public static int modeButtonSize = 50;
 	
 	public static enum BuildMode {
 		BUILD_DESTROY, BUILD_ONLY, DESTROY_ONLY;
 		public BuildMode getNext() {
 			return values()[(ordinal()+1) % values().length];
 		}
 	}
 
 	static public Paint redFilter;
 	
 	private static int[]  character = {0, 0};
 	static{
 		animationTexture = TextureLoader.loadImage("images/animation.png");
 		animationTextureFlipped = FlipHelper.flipAnimation(animationTexture, MCTPO.tileSize, MCTPO.tileSize * 2);
 		//Red filter
 		redFilter = new Paint(Color.RED);
 		ColorFilter filter = new LightingColorFilter(Color.RED, 1);
 		redFilter.setColorFilter(filter);
 		
 
 		buildOnlyButton = TextureLoader.loadImage("images/buildonlybutton.png");
 		destroyOnlyButton = TextureLoader.loadImage("images/destroyonlybutton.png");
 		buildDestroyButton = TextureLoader.loadImage("images/builddestroybutton.png");
 	}
 	
 	private List<Thing> collisions = new ArrayList<Thing>();
 	
 	public double fallingSpeed = 4d;
 	public double jumpingSpeed = 2d;
 	public double movementSpeed = 1.5d;
 	public double sprintSpeed = 0.5d;
 	public boolean isMoving = false;
 	public boolean wouldJump = false;
 	public boolean isJumping = false;
 	public boolean setBlockBelow = false;
 	public boolean isSprinting = false;
 	public int jumpHeight = 12, jumpCount = 0; 
 	public double dir = 1;
 	public int animation, animationFrame, animationTime = 15;
 	public int sprintAnimationTime = 8;
 	public double buildRange = 60; //in pixels
 	public boolean isFalling = false;
 	public double startFalling = 0;
 	public Inventory inventory = new Inventory(this);
 	public HealthBar healthBar;
 	public int maxHealth = 100;
 	public int health = 100;
 	public boolean damaged = false;
 	public int damageTime=10, damageFrame=0;
 	public int destroyTime=0;
 	public boolean buildOn = true;
 	private boolean building = false;
 	public BuildMode buildMode = BuildMode.BUILD_DESTROY;
 	public Block currentBlock;
 	public Block lastBlock;
 	public final int bUP = 0, bDOWN = 1, bRIGHT = 2, bLEFT = 3;
 	public com.atlan1.mctpo.mobile.Graphics.Line.Double[] bounds = new com.atlan1.mctpo.mobile.Graphics.Line.Double[4];
 	
 	public Character(double width, double height) {
 		
 		/*damageAnimationTexture = Bitmap.createBitmap(animationTexture.getWidth(), animationTexture.getHeight(), Bitmap.Config.RGB_565);
 		Canvas damageCanvas = new Canvas(damageAnimationTexture);
 		damageCanvas.drawBitmap(animationTexture, 0, 0, p);*/
 		
 		healthBar = new HealthBar(this);
 		
 		setBounds(width, height, (MCTPO.pixel.width / 2) - (width / 2), (MCTPO.pixel.height / 2) - (height / 2));
 		calcBounds();
 	}
 	
 	//TODO: Nur gerenderte Blocks durchlaufen!!!
 	
 	public boolean isCollidingWithAnyBlock(Line2d line) {
 		for(int x=(int)(this.x/MCTPO.tileSize);x<(int)(this.x/MCTPO.tileSize+3);x++)
 			for(int y=(int)(this.y/MCTPO.tileSize);y<(int)(this.y/MCTPO.tileSize+3);y++)
 				if(x >= 0 && y >= 0 && x < World.worldW && y < World.worldH){
 					boolean collide = World.blocks[x][y].contains(line.getP1())|| World.blocks[x][y].contains(line.getP2());
 					if(collide)
 						collisions.add(World.blocks[x][y].addCollision(this));
 					if(!World.blocks[x][y].material.nonSolid&&collide)
 							return true;
 				}
 		return false;
 	}
 	
 	public boolean isCollidingWithBlock(Block t) {
 		RectF blockRect = t.toRectF();
 		for(com.atlan1.mctpo.mobile.Graphics.Line.Double line : bounds){
 			if (blockRect.contains((float) line.getP1().x, (float) line.getP1().y) || blockRect.contains((float) line.getP2().x, (float) line.getP2().y)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void render(Canvas c){
 		/*Log.d("Block below x", String.valueOf((int) Math.round(x) - (int) MCTPO.sX));
 		Log.d("Block below y", String.valueOf((int) y - 1 - (int) MCTPO.sY));
 		Log.d("Block below x2", String.valueOf((int)Math.round((x + width)) - (int) MCTPO.sX));
 		Log.d("Block below y2", String.valueOf((int)(y + height) - 1 - (int) MCTPO.sY));
 		c.drawBitmap(Material.terrain.getSubImageById(1), new Rect(0, 0, MCTPO.tileSize, MCTPO.tileSize), new Rect((int)x - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, (int)(x + width) - (int) MCTPO.sX, (int)(y + height) - (int) MCTPO.sY), null);*/
 		if(dir>=0) 
 			c.drawBitmap(animationTexture, new Rect((character[0] * MCTPO.tileSize)+(MCTPO.tileSize * animation), (character[1] * MCTPO.tileSize), (character[0] * MCTPO.tileSize)+((animation + 1) * MCTPO.tileSize), ((character[1] + 2) * MCTPO.tileSize)), new Rect((int)((x -  MCTPO.sX) * MCTPO.pixelSize), (int) ((y - MCTPO.sY + MCTPO.tileSize) * MCTPO.pixelSize), (int) (((x + width) - MCTPO.sX) * MCTPO.pixelSize), (int) ((int)((y + height) - (int) MCTPO.sY + MCTPO.tileSize) * MCTPO.pixelSize)), damaged?redFilter:null);
 			//c.drawBitmap(animationTexture, (int)(x + width) - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, damaged?redFilter:null);
 			/*Rect src = new Rect((character[0] * MCTPO.tileSize)+(MCTPO.tileSize * animation), (character[1] * MCTPO.tileSize), (character[0] * MCTPO.tileSize)+(animation * MCTPO.tileSize)+ (int) width, (character[1] * MCTPO.tileSize) + (int) height);
 			Rect dst = new Rect((int)x - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, (int)(x + width) - (int) MCTPO.sX, (int)(y + height) - (int) MCTPO.sY);
 			c.drawBitmap(animationTexture, src, dst, damaged?redFilter:null);*/
 		else
 			c.drawBitmap(animationTextureFlipped, new Rect((character[0] * MCTPO.tileSize)+(MCTPO.tileSize * animation), (character[1] * MCTPO.tileSize), (character[0] * MCTPO.tileSize)+((animation + 1) * MCTPO.tileSize), ((character[1] + 2) * MCTPO.tileSize)), new Rect((int)((x -  MCTPO.sX) * MCTPO.pixelSize), (int) ((y - MCTPO.sY + MCTPO.tileSize) * MCTPO.pixelSize), (int) (((x + width) - MCTPO.sX) * MCTPO.pixelSize), (int) ((int)((y + height) - (int) MCTPO.sY + MCTPO.tileSize) * MCTPO.pixelSize)), damaged?redFilter:null);
 			//c.drawBitmap(animationTextureFlipped, (int)(x + width) - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, damaged?redFilter:null);
 			/*Rect src = new Rect((character[0] * MCTPO.tileSize)+(MCTPO.tileSize * animation), (character[1] * MCTPO.tileSize), (character[0] * MCTPO.tileSize)+(animation * MCTPO.tileSize)+ (int) width, (character[1] * MCTPO.tileSize) + (int) height);
 			Rect dst = new Rect((int)(x + width) - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, (int)x - (int) MCTPO.sX, (int)(y + height) - (int) MCTPO.sY);
 			c.drawBitmap(animationTextureFlipped, src, dst, damaged?redFilter:null);*/
 		
 	}
 	
 	public void tick(){
 		calcBounds();
 		clearCollisions();
 		//isSprinting = MCTPO.mctpo.controlDown;
 		
 		calcMovement();
 		
 		int firstHealth = health;
 		boolean noGroundCollision = !isCollidingWithAnyBlock(bounds[bDOWN]);
 		if(!isJumping && noGroundCollision){
 			y+=fallingSpeed;
 			MCTPO.sY+=fallingSpeed;
 			if(!isFalling && noGroundCollision){
 				startFalling=this.y;
 				isFalling = true;
 			}
 		}else{
 			if(wouldJump)
 				isJumping = true;
 		}
 		
 		if(!noGroundCollision && isFalling){
 			int deltaFallBlocks = (int) ((this.y-startFalling)/MCTPO.tileSize);
 			if(deltaFallBlocks>3)
 				health-=deltaFallBlocks;
 			startFalling=0;
 			isFalling = false;
 		}
 		if(isJumping){
 			boolean canJump = !isCollidingWithAnyBlock(bounds[bUP]);
 			if(canJump){
 				if(jumpHeight<=jumpCount){
 					isJumping = false;
 					jumpCount = 0;
 					if (setBlockBelow && !inventory.slots[inventory.selected].material.nonSolid) {
 						if(inventory.slots[inventory.selected].stackSize>0)
 							inventory.slots[inventory.selected].stackSize--;
 						else{
 							inventory.slots[inventory.selected].stackSize = 0;
 							inventory.slots[inventory.selected].material = Material.AIR;
 						}
 						World.blocks[(int) Math.round(x / MCTPO.tileSize)][(int) Math.round(y / 20 + 2)].material = inventory.slots[inventory.selected].material;
 						setBlockBelow = false;
 					} else if (setBlockBelow) {
 						setBlockBelow = false;
 					}
 				}else{
 					y-=jumpingSpeed;
 					MCTPO.sY-=jumpingSpeed;
 					jumpCount++;
 				}
 			}else{
 				isJumping = false;
 				jumpCount = 0;
 				setBlockBelow = false;
 			}
 		}		
 		if(isMoving){
 			boolean canMove = false;
 			
 			if(dir == movementSpeed){
 				canMove = !isCollidingWithAnyBlock(bounds[bRIGHT]);
 			}else if (dir == -movementSpeed){
 				canMove = !isCollidingWithAnyBlock(bounds[bLEFT]);
 			}
 			
 			if(animationFrame >= (isSprinting?sprintAnimationTime:animationTime)) {
 				if(animation<3){
 					animation++;
 					animationFrame=0;
 				}else{
 					animation=0;
 					animationFrame=0;
 				}
 			}else{
 				animationFrame+=1;
 			}
 			
 			if(canMove){
 				x+=isSprinting?dir<0?dir-sprintSpeed:dir+sprintSpeed:dir;
 				MCTPO.sX+=isSprinting?dir<0?dir-sprintSpeed:dir+sprintSpeed:dir;
 			} else if (!isJumping && !noGroundCollision && MCTPO.fingerDown) {
 				isJumping = true;
 			}
 		}else{
 			animation = 1;
 		}
 		if(firstHealth-health>0){
 			damaged = true;
 		}
 		if(damaged){
 			if(damageTime<=damageFrame){
 				damageFrame=0;
 				damaged= false;
 			}else{
 				damageFrame++;
 			}
 		}
 		/*if(currentBlock!=null&&currentBlock.material.nonSolid&&isBlockInBuildRange(currentBlock))
 			MCTPO.mctpo.setCursor(MCTPO.buildCursor);
 		else if(currentBlock!=null&&!currentBlock.material.nonSolid&&isBlockInBuildRange(currentBlock)){
 			MCTPO.mctpo.setCursor(MCTPO.destroyCursor);
 		}else if(currentBlock!=null&&!isBlockInBuildRange(currentBlock)){
 			MCTPO.mctpo.setCursor(MCTPO.crossHair);
 		}*/
 		if(currentBlock!=null&&lastBlock!=null&&MCTPO.fingerBuildDown&&lastBlock.equals(currentBlock)&&isBlockInBuildRange(currentBlock)){
 			destroyTime++;
 		}else{
 			destroyTime=0;
 		}
 		if (!MCTPO.fingerBuildDown) {
 			if (!buildOn) {
 				buildOn = true;
 			}
 			if (building) {
 				building = false;
 			}
 				
 		}
 		lastBlock = currentBlock;
 		currentBlock = getCurrentBlock();
 		if(currentBlock!=null)
 			build();
 		if(health<=0){
 			inventory.clear();
 			this.respawn();
 			health = maxHealth;
 		}
 		if(this.y/MCTPO.tileSize>World.worldH){
 			this.teleport((int) this.x, 0);
 		}
 	}
 	
 	private void calcMovement() {
 		if (MCTPO.fingerDown) {
 			if (MCTPO.fingerP.x <= (MCTPO.size.width) / 2 - 30) {
 				isMoving = true;
 				dir = -movementSpeed;
 			} else if (MCTPO.fingerP.x >= (MCTPO.size.width) / 2 + 30) {
 				isMoving = true;
 				dir = movementSpeed;
 			} else if (!isJumping && (MCTPO.fingerP.y <= (MCTPO.size.height) / 2 - 50) && isCollidingWithAnyBlock(bounds[bDOWN])) {
 				isJumping = true;
 			} else if (!isJumping && (MCTPO.fingerP.y >= (MCTPO.size.height) / 2 + 50) && isCollidingWithAnyBlock(bounds[bDOWN])) {
 				isJumping = true;
 				setBlockBelow = true;
 			}
 		} else if (this.isMoving) {
 			this.isMoving = false;
 		}
 		Log.d("fingerDownY", String.valueOf(MCTPO.fingerDownP.y));
 		Log.d("fingerY", String.valueOf(MCTPO.fingerP.y));
 		if (MCTPO.fingerDownP.y - MCTPO.fingerP.y  > 70 && MCTPO.fingerDown && !isJumping && isCollidingWithAnyBlock(bounds[bDOWN])) {
 			isJumping = true;
 		} /*else if (isJumping) {
 			isJumping = false;
 		}*/
 		
 	}
 
 	public void calcBounds(){
 		bounds[bUP] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)(x+2), (int) (y+1)), new Point((int)(x + width -2), (int)(y+1)));
 		bounds[bDOWN] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)(x+2), (int) (y+height)), new Point((int)(x+width-2), (int)(y+height)));
 		bounds[bRIGHT] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)(x + width -1), (int) y), new Point((int)(x + width), (int) (y + (height-2))));
 		bounds[bLEFT] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)x-1, (int) y), new Point((int)x-1, (int) (y + (height-2))));
 		
 	}
 	
 	public void clearCollisions() {
 		for(Thing t: new ArrayList<Thing>(collisions)) {
 			if(!isCollidingWithBlock((Block) t)){
 				this.removeCollision(t);
 				t.removeCollision(this);
 			}
 		}
 	}
 	
 	public Block getCurrentBlock(){
 		if (MCTPO.fingerBuildDown) {
 			//Block[][] blocks = World.blocks;
 			/*int camX=(int)MCTPO.sX;
 			int camY=(int)MCTPO.sY;
 			int renW=(MCTPO.pixel.width / MCTPO.tileSize) + 2;
 			int renH=(MCTPO.pixel.height / MCTPO.tileSize) + 2;*/
 			/*for(int x=(camX/MCTPO.tileSize);x<(camX/MCTPO.tileSize) + renW;x++){
 				for(int y=(camY/MCTPO.tileSize);y<(camY/MCTPO.tileSize) + renH;y++){
 					if(x>=0 && y>=0 && x<World.worldW && y<World.worldH){
 						if(blocks[x][y].contains(new Point(MCTPO.fingerBuildP.x + (int)MCTPO.sX, MCTPO.fingerBuildP.y + (int)MCTPO.sY))){
 							return blocks[x][y];
 						}
 					}
 				}
 			}*/
 			try {
 				Block b = getBlockIncluding(MCTPO.fingerBuildP.x / MCTPO.pixelSize, MCTPO.fingerBuildP.y / MCTPO.pixelSize);
 				/*Log.d("bx", String.valueOf(b.x));
 				Log.d("by", String.valueOf(b.y));
 				Log.d("px", String.valueOf(x));
 				Log.d("py", String.valueOf(y));*/
 				return b;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		/*else {
 				double m = (MCTPO.fingerBuildDownP.y - y)/(MCTPO.fingerBuildDownP.x - x);
 				double xIterate = MCTPO.size.width/2;
 				double yIterate = MCTPO.size.height/2;
 				double halfScreenWidth = MCTPO.size.width / 2;
 				double halfScreenHeight = MCTPO.size.height / 2;
 				int xdirection = (MCTPO.fingerBuildP.x > 0) ? 1 : -1;
 				Block b = null;
 				do {
 					xIterate = xdirection * Math.sqrt(1 / (1 + m * m));
 					yIterate = m * xIterate;
 					b =  getBlockIncluding(xIterate - halfScreenWidth, yIterate - halfScreenHeight);
 				} while (b != null?isBlockInBuildRange(b) && b.material == Material.AIR:true);
 				if (isBlockInBuildRange(b)) {
 					return b;
 				}
 			}*/
 			
 		return null;
 	}
 	
 	public Block getBlockIncluding(double x, double y) {
 		return World.blocks[(int) ((x + MCTPO.sX) / MCTPO.tileSize)][(int) ((y + MCTPO.sY) / MCTPO.tileSize) - 1]; // -1 only in mobile version
 		//return World.blocks[(int) ((x + MCTPO.sX - (MCTPO.size.width - MCTPO.pixel.width) / 2) / (MCTPO.tileSize))][(int) ((y + MCTPO.sY - (MCTPO.size.height - MCTPO.pixel.height) / 2) / (MCTPO.tileSize) - 1)];
 	}
 	
 	public boolean isBlockInBuildRange(Block block) {
 		//Log.d("range", String.valueOf(Math.sqrt((Math.pow(((MCTPO.fingerBuildP.x / MCTPO.tileSize + MCTPO.sX + (MCTPO.size.width - MCTPO.pixel.width) / 2 - (int)(this.x+width/2))), 2) + Math.pow(((MCTPO.fingerBuildP.y / MCTPO.tileSize + MCTPO.sY + (MCTPO.size.height - MCTPO.pixel.height) / 2) - (int)(this.y+height/2)) , 2)))));
 		//Log.d("rangeValue", String.valueOf(buildRange * MCTPO.pixelSize));
		return Math.sqrt((Math.pow(((MCTPO.fingerBuildP.x / MCTPO.pixelSize + (int)MCTPO.sX) - (int)(this.x+width/2)), 2) + Math.pow(((MCTPO.fingerBuildP.y / MCTPO.pixelSize + (int)MCTPO.sY) - (int)(this.y+height/2)) , 2))) <= buildRange;
 		//return Math.sqrt((Math.pow(((MCTPO.fingerBuildP.x + MCTPO.sX - (MCTPO.size.width - MCTPO.pixel.width) / 2 - (int)(this.x+width/2))), 2) + Math.pow(((MCTPO.fingerBuildP.y + MCTPO.sY - (MCTPO.size.height - MCTPO.pixel.height) / 2) - (int)(this.y+height/2)) , 2))) <= buildRange * MCTPO.pixelSize;
 	}
 	
 	public void build(){
 		if(isBlockInBuildRange(currentBlock) /*&& currentBlock != lastBlock*/){
 			//Log.d("build", "inRange");
 			Material m = currentBlock.material;
 			if(MCTPO.fingerBuildDown && m.nonSolid && buildOn && buildMode != BuildMode.DESTROY_ONLY && MCTPO.fingerBuildP.x != -1 && MCTPO.fingerBuildP.y != -1/* && !this.asRectF().contains((float) MCTPO.fingerBuildP.x, (float) MCTPO.fingerBuildP.y)*/){
 				if (!building) {
 					building = true;
 				}
 				if(inventory.slots[inventory.selected].material != Material.AIR){
 					if(inventory.slots[inventory.selected].stackSize>0)
 						inventory.slots[inventory.selected].stackSize--;
 					else{
 						inventory.slots[inventory.selected].stackSize = 0;
 						inventory.slots[inventory.selected].material = Material.AIR;
 					}
 					currentBlock.material = inventory.slots[inventory.selected].material;
 					//buildOn = false;
 					return;
 				}
 			} else if(MCTPO.fingerBuildDown && !building && buildMode != BuildMode.BUILD_ONLY && MCTPO.fingerBuildP.x != -1 && MCTPO.fingerBuildP.y != -1){
 				if (buildOn) {
 					buildOn = false;
 				}
 				if(destroyTime>=m.hardness&&!(m.hardness<0)){
 					if(inventory.containsMaterial(m)){
 						boolean check = false;
 						Slot[] slots = inventory.getSlotsContaining(m);
 						for(Slot s : slots){
 							if(s.stackSize<inventory.maxStackSize){
 								s.stackSize++;
 								check = true;
 								break;
 							}
 						}
 						if(!check && inventory.containsMaterial(Material.AIR)){
 							Slot s2 = inventory.getSlot(Material.AIR);
 							s2.material = m;
 							s2.stackSize++;
 						}
 					}else if(inventory.containsMaterial(Material.AIR)){
 						Slot s3 = inventory.getSlot(Material.AIR);
 						s3.material = m;
 						s3.stackSize++;
 					}
 					currentBlock.material = Material.AIR;
 					destroyTime = 0;
 				}
 				return;
 			}
 		}
 	}
 	
 	public void respawn(){
 		teleport(MCTPO.world.spawnPoint.x, MCTPO.world.spawnPoint.y);
 	}
 	
 	public void teleport(double x, double y){
 		this.y = y;
 		this.x = x;
 		MCTPO.sY = y - (MCTPO.pixel.height / 2) + (height / 2);
 		MCTPO.sX = x - (MCTPO.pixel.width / 2) + (width / 2);
 	}
 
 	@Override
 	public int getHealth() {
 		return health;
 	}
 
 	@Override
 	public void setHealth(int i) {
 		health = i;
 	}
 
 	@Override
 	public Thing addCollision(Thing t) {
 		collisions.add(t);
 		return this;
 	}
 
 	@Override
 	public Thing removeCollision(Thing t) {
 		collisions.remove(t);
 		return this;
 	}
 }
