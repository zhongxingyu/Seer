 package gameController;
 
 import gameData.ActivePlayer;
 import gameData.Player;
 import gameView.GameWindow;
 import gameView.ingame.Block;
 import gameView.ingame.datatypes.Direction;
 import gameView.ingame.datatypes.Entity;
 import gameView.ingame.datatypes.RelativeBoxPosition;
 import gameView.ingame.datatypes.Texture;
 import gameView.ingame.menu.DrawableInventory;
 import gameView.ingame.menu.MainMenu;
 import gameView.ingame.menu.SkillMenu;
 import gameView.ingame.menu.UIItem;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.KeyEventDispatcher;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 
 import recources.ImageCache;
 import singleton.GameData;
 import singleton.GameProperties;
 import singleton.SingletonWorker;
 
 import utilities.ImageUtil;
 
 public class GameControllerThread implements Runnable, MouseListener, MouseMotionListener,
 KeyEventDispatcher {
 
 	private static final int REFRESHS_PER_SECOND = 60;
 	private boolean forwardPressed, backwardPressed, leftPressed, rightPressed;
 	public static boolean rendering;
 	private static final int GRAVITY_Y = 2;
 
 	private File bg = new File(GameProperties.getGamePath()
 			+ "/rec/" + GameProperties.SPLASH_PIC_BACKGROUND);
 	private BufferedImage background = null;
 	private BufferedImage basic = null;
 	public Font f;
 	public int frames = 0;
 	public int framecount = 0;
 	public long frametime = 0;
 	private long timeout;
 	private long time;
 
 	private void fillItemsAndInventoryTest() {
 		int[] equipedItems = new int[9];
 		for (int i = 0; i < 9; i++) {
 			equipedItems[i] = Integer.MAX_VALUE;
 		}
 		SingletonWorker.gameData().activePlayer().setEquiped(equipedItems);
 	}
 
 	@Override
 	public void run() {
 		GameData gamedata = SingletonWorker.gameData();
 		f = SingletonWorker.gameProperties().gameFont();
 		Graphics2D g = (Graphics2D) gamedata.bufferstrategy().getDrawGraphics();
 		BufferedImage splashimage = ImageCache.getRecource(SingletonWorker.gameProperties().splashPath());
 		g.drawImage(ImageUtil.resizeImage(splashimage,GameWindow.getWindowWidth(),GameWindow.getWindowHeight()), 0, 0, null);
 		g.dispose();
 		gamedata.bufferstrategy().show();
 		this.initGameValues();
 
 		gamedata.gameLoaded = true;
 		ActivePlayer player = SingletonWorker.gameData().activePlayer();
 		long duration;
 		long cycleStartTime = System.currentTimeMillis();
 		frametime = System.currentTimeMillis();
 		while(true){ //game heartbeat thread running indefinitely, until explicit break
 			framecount++;
 			if((System.currentTimeMillis() - frametime) >= 1000){
 				frames = framecount;
 				framecount = 0;
 				frametime += 1000;
 				gamedata.mine().checkChunks();
 			}
 
 			duration = System.currentTimeMillis() - cycleStartTime;
 			cycleStartTime = System.currentTimeMillis();
 			//			Unwanted behaviour!
 			//			this.calculateMovements();
 			this.updateMenuActiveStates();
 
 			if(leftPressed){
 				gamedata.activePlayer().setDirection(Direction.left);
 			}else if (rightPressed){
 				gamedata.activePlayer().setDirection(Direction.right);
 			}else if (forwardPressed){
 				gamedata.activePlayer().setDirection(Direction.up);
 			}else if (backwardPressed){
 				gamedata.activePlayer().setDirection(Direction.down);
 			}
 			gamedata.activePlayer().updateImageDirection(duration);
 			this.updateMenus(duration);
 
 
 
 			/*
 			 * Start New Movement calculation Code!
 			 * */
 			//defining some variables around the player
 			int playerx = SingletonWorker.gameProperties().getPlayerBlockX();
 			int playery = SingletonWorker.gameProperties().getPlayerBlockY();
 			Block topleft = gamedata.mine().getBlock(playerx-1, playery-1);
 			Block top = gamedata.mine().getBlock(playerx, playery-1);
 			Block topright = gamedata.mine().getBlock(playerx+1, playery-1);
 
 			Block left = gamedata.mine().getBlock(playerx-1, playery);
 			Block current = gamedata.mine().getBlock(playerx, playery);
 			Block right = gamedata.mine().getBlock(playerx+1, playery);
 
 			Block bottomleft = gamedata.mine().getBlock(playerx-1, playery+1);
 			Block bottom = gamedata.mine().getBlock(playerx, playery+1);
 			Block bottomright = gamedata.mine().getBlock(playerx+1, playery+1);
 			String debug = "";
 			boolean loaded = true;
 			if(	topleft == null &&
 					top == null &&
 					topright == null &&
 					left == null &&
 					current == null &&
 					right == null &&
 					bottomleft == null &&
 					bottom == null &&
 					bottomright == null){
 				loaded = false;
 			}
 			if(loaded){
 				debug += " loaded!";
 			}
 			/*Movement code:
 			 * at the moment deactivated because of to less progress
 			 * and needed critical updates!
 			
 			double movementspeed = player.getCalculatedMovement(duration);
 			//			debug += " movementspeed:" + movementspeed;
 			boolean onLadder = false;
 			//no movement without correct chunks!
 			debug += " loaded:" + loaded;
 			if(loaded){
 
 				//start up/down movement
 				//Ignore vertical movement without ladder
 				debug += " onLadder:";
 				if(current != null && BlockWorker.isLadder(current.getID())){
 					debug += "n ";
 					onLadder = true;
 				}else{
 					if(top != null && BlockWorker.isLadder(top.getID()) && player.intercectsOffset(top, 0, -5)){
 						debug += "t ";
 						onLadder = true;
 					}
 					if(bottom != null && BlockWorker.isLadder(bottom.getID()) && player.intercectsOffset(bottom, 0, 5)){
 						debug += "b ";
 						onLadder = true;
 					}
 				}
 				debug +=onLadder;
 				int prefix = 0;
 				int screenYMovement = 0;
 				boolean verticalMovement = false;
 				if(onLadder){
 					//if-statement uses ^ as an XOR-comparison!
 					if(forwardPressed ^ backwardPressed){
 						verticalMovement = true;
 						if(forwardPressed){
 							//UP-key pressed
 							debug += " up  ";
 //							if(top != null && top.isMassive() && player.intercectsOffset(top, 0, -2)){
 //								verticalMovement = false;
 //							}
 							prefix = -1;
 						}else{
 							debug += " down";
 							//DOWN-key pressed
 //							if(bottom != null && bottom.isMassive() && player.intercectsOffset(bottom, 0, -2)){
 //								verticalMovement = false;
 //							}
 							prefix = 1;
 						}
 					}
 					if(verticalMovement){
 						screenYMovement  = (int) (prefix * movementspeed);
 					}
 				}else{
 					debug += " grav";
 					boolean gravitation = true;
 					if(bottom != null && bottom.isMassive() && player.intercectsOffset(bottom, 0, 5)){
 						gravitation = false;
 					}
 					if(bottomleft != null && bottomleft.isMassive() && player.intercectsOffset(bottomleft, 0, 5)){
 						gravitation = false;
 					}
 					if(bottomright != null && bottomright.isMassive() && player.intercectsOffset(bottomright, 0, 5)){
 						gravitation = false;
 					}
 					if(gravitation){
 						screenYMovement = (int) (movementspeed*GRAVITY_Y);
 						prefix = 1;
 					}
 				}
 				//interpolate running against walls!
 				if(screenYMovement != 0){
 //					Block runagainst = null;
 //					boolean tops = true;
 //					if(screenYMovement < 0){
 //						runagainst = top;
 //						tops = true;
 //					}else if(screenYMovement > 0){
 //						runagainst = bottom;
 //						tops = false;
 //					}
 //					if(runagainst != null){
 //						if(player.intercectsOffset(runagainst, 0, prefix*3)){
 //						}else if(player.intercectsOffset(runagainst, 0, screenYMovement)){
 //							if(!player.intercectsOffset(runagainst, 0, prefix*2)){
 //								GameProperties.playery += prefix;
 //							}
 //							//							int i = 0;
 //							//							while(!player.intercectsOffset(runagainst, 0, prefix*2)){
 //							//								GameProperties.playery += prefix;
 //							//								i++;
 //							//								player;
 //							//								if(i == 100 || i == 1){
 //							//									System.out.println(i);
 //							//									System.out.println(player.intercects(runagainst) + " " + player.intercects(bottom) + " " + player.intercects(current));
 //							//									System.out.println(onLadder + " " + tops + " " + prefix + " vertical " + screenYMovement + "  " + player.getYPos() + "|" + runagainst.getYPos());
 //							//								}
 //							//							}
 //						}else if(player.intercectsOffset(runagainst, 0, 2*screenYMovement)){
 //							screenYMovement = screenYMovement/2;
 //						}
 //					}
 					debug += " move " + prefix + " " + screenYMovement;
 					GameProperties.playery += screenYMovement;
 				}
 				//start left/right movement
 				prefix = 0;
 				int screenXMovement = 0;
 				boolean horizontalMovement = false;
 				//if-statement uses ^ as an XOR-comparison!
 				if(leftPressed ^ rightPressed){
 
 					horizontalMovement = true;
 					if(leftPressed){
 						//LEFT-key pressed
 						debug += " left ";
 //						if(left != null && left.isMassive() && player.intercectsOffset(left, -2, 0)){
 //							horizontalMovement = false;
 //						}
 						prefix = -1;
 
 					}else{
 						//RIGHT-key pressed
 						debug += " right";
 //						if(right != null && right.isMassive() && player.intercectsOffset(right, 2, 0)){
 //							horizontalMovement = false;
 //						}
 						prefix = 1;
 					}
 				}
 				if(horizontalMovement){
 					screenXMovement  = (int) (prefix * movementspeed);
 				}
 				//interpolate running against walls!
 				if(screenXMovement != 0){
 //					Block runagainst = null;
 //					if(screenXMovement < 0){
 //						runagainst = left;
 //					}else if(screenXMovement > 0){
 //						runagainst = right;
 //					}
 //					if(runagainst != null){
 //						if(player.intercectsOffset(runagainst, screenXMovement, 0)){
 //							//							int i = 0;
 //							//							while(!player.intercectsOffset(runagainst, prefix*2, 0)){
 //							//								GameProperties.playerx += prefix;
 //							//								i++;
 //							//								if(i > 100){
 //							//									System.out.println(prefix + " horizontal " + runagainst.getXPos() + "|" + runagainst.getYPos());
 //							//								}
 //							//							}
 //						}else if(player.intercectsOffset(runagainst, 2*screenXMovement, 0)){
 //							screenXMovement = screenXMovement/2;
 //						}
 //					}
 					debug += " vert " + prefix + " " + screenXMovement;
 					GameProperties.playerx += screenXMovement;
 				}
 			}
 			//			SingletonWorker.gameProperties().playerx += screenXMovement;
 			//			SingletonWorker.gameProperties().playery += screenYMovement;
 
 			
 			
 			/*
 			 * End New Movement calculation Code!*/
 			 /* 
 			 * omiting old code:
 			 * */
 
 			int screenXMovement = 0;
 			int screenYMovement = (int) player.getCalculatedMovement(duration)*GRAVITY_Y;
 
 			//			boolean onLadder = false;
 			boolean movementPossible = true;
 			LinkedList<Texture> textures = gamedata.mine().getTextures();
 			boolean digging = false;
 			//move y direction
 			boolean outofbounds = true;
			if(GameProperties.playery < 15){
 				outofbounds = false;
 			}
 
 			for(Texture curTexture : textures){
 				if(curTexture instanceof Block){
 					if(gamedata.activePlayer().intercectsOffset(curTexture,0,+5)){
 						Block block = ((Block) curTexture);
 						if(block.getID() == 11 || block.getID() == 12){
 							if(forwardPressed ^ backwardPressed){
 								if(forwardPressed){
 									screenYMovement = (int) player.getCalculatedMovement(duration)*-1;
 								}else{
 									screenYMovement = (int) player.getCalculatedMovement(duration);
 								}
 							}else{
 								screenYMovement = 0;
 							}
 							
 						}
 					}else if(gamedata.activePlayer().intercectsOffset(curTexture,0,+10)){
 						Block block = ((Block) curTexture);
 						Block ontop = gamedata.mine().getBlock(block.xPos, block.yPos-1);
 						if((block.getID() == 11 || block.getID() == 12) && !backwardPressed){
 							if(ontop != null){
 								if(ontop == null || (ontop.getID() != 11 && ontop.getID() != 12)){
 									screenYMovement = 0;
 								}
 							}
 						}
 					}
 				}
 			}
 			for (Texture currentTexture : textures) {
 				if(movementPossible){
 
 					if (currentTexture instanceof Block && gamedata.activePlayer().intercectsOffset(currentTexture,0,screenYMovement*5)) {
 						outofbounds = false;
 						Block block = ((Block) currentTexture);
 						if (block.isMassive()){
 							movementPossible = false;
 							if(!gamedata.activePlayer().intercectsOffset(currentTexture,0,5)){
 								GameProperties.playery += 1;
 							}
 							if((block.yPos > SingletonWorker.gameProperties().getPlayerBlockY() && backwardPressed) || (block.yPos < SingletonWorker.gameProperties().getPlayerBlockY() && forwardPressed)){
 								block.hit(gamedata.activePlayer());
 								digging = true;
 							}
 						}
 
 					} else if (gamedata.activePlayer().hits(currentTexture)) {
 
 						gamedata.activePlayer().collide(currentTexture);
 
 					}
 				}
 
 			}
 			if(leftPressed ^ rightPressed){
 				if(leftPressed){
 					screenXMovement = (int) player.getCalculatedMovement(duration)*-1;
 				}else{
 					screenXMovement = (int) player.getCalculatedMovement(duration);
 				}
 			}
 			if (movementPossible && !outofbounds) {
 				GameProperties.playery += screenYMovement;
 			}
 			movementPossible = true;
 			//move x direction
 			for (Texture currentTexture : textures) {
 				if(movementPossible){
 					if (currentTexture instanceof Block &&
 							gamedata.activePlayer().intercectsOffset(currentTexture,screenXMovement*3,0)) {
 						Block block = ((Block) currentTexture);
 						if (block.isMassive()){
 							movementPossible = false;
 
 							if(((block.xPos < SingletonWorker.gameProperties().getPlayerBlockX() && leftPressed) || (block.xPos > SingletonWorker.gameProperties().getPlayerBlockX() && rightPressed)) && !digging){
 								block.hit(gamedata.activePlayer());
 							}
 						}
 
 					} else if (gamedata.activePlayer().hits(currentTexture)) {
 
 						gamedata.activePlayer().collide(currentTexture);
 
 					}
 				}
 
 			}
 		
 			if (movementPossible) {
 				GameProperties.playerx += screenXMovement;
 			}
 			/*
 			 * omiting old code
 			 * */
 
 			gamedata.getNetworkHandlerThread().sendMovement(GameProperties.playerx,GameProperties.playery);
 			g = (Graphics2D) gamedata.bufferstrategy().getDrawGraphics();
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			g.setFont(f);
 			if(basic == null){
 				basic = ImageUtil.resizeImage(ImageCache.getIcon(0),GameWindow.getWindowWidth(),GameWindow.getWindowHeight());
 			}
 			g.drawImage(basic, 0, 0, null);
 
 			if(background == null){
 				try {
 					background = ImageUtil.resizeImage(
 							ImageIO.read(bg),
 							GameWindow.getWindowWidth()*2,
 							GameWindow.getWindowHeight()*2);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 			g.drawImage(background, -(GameWindow.getWindowWidth()/2)-(GameProperties.playerx/2), -200-(GameProperties.playery/2) , null);
 
 			gamedata.mine().draw(g);
 
 			gamedata.activePlayer().draw(g);
 			for(Entity e : gamedata.entityMap().values()){
 				e.draw(g);
 			}
 			if (gamedata.getActiveMenu() != Integer.MAX_VALUE) {
 				gamedata.uiItemMap()
 				.get(gamedata.getActiveMenu()).draw(g);
 			}
 			g.drawString("Frames: " + frames + " Miliseconds:" + time + "/" + timeout, 50, 50);
 			String moneystring = "Money: " + SingletonWorker.gameData().money();
 			int moneyfontsize = g.getFontMetrics().charsWidth(moneystring.toCharArray(), 0, moneystring.length());
 			g.drawString(moneystring, SingletonWorker.gameData().width()-moneyfontsize-50, 100);
 			g.drawString("Debug: " + debug, 50, 900);
 			
 			boolean debugcollision = false;
 			if(debugcollision){
 				g.setColor(Color.red);
 				g.drawRect((int) player.getXPos(), (int)player.getYPos(), GameProperties.GRAPHICS_SIZE_CHAR_WIDTH, GameProperties.GRAPHICS_SIZE_CHAR_HEIGHT);
 
 				g.setColor(Color.yellow);
 				if(top != null){
 					g.drawRect((int) top.getXPos(), (int) top.getYPos(), top.getImage().getWidth(), top.getImage().getHeight());
 				}
 				if(topleft != null){
 					g.drawRect((int) topleft.getXPos(), (int) topleft.getYPos(), topleft.getImage().getWidth(), topleft.getImage().getHeight());
 				}
 				if(topright != null){
 					g.drawRect((int) topright.getXPos(), (int) topright.getYPos(), topright.getImage().getWidth(), topright.getImage().getHeight());
 				}
 				
 				g.setColor(Color.blue);
 				if(current != null){
 					g.drawRect((int) current.getXPos(), (int) current.getYPos(), current.getImage().getWidth(), current.getImage().getHeight());
 				}
 				if(left != null){
 					g.drawRect((int) left.getXPos(), (int) left.getYPos(), left.getImage().getWidth(), left.getImage().getHeight());
 				}
 				if(right != null){
 					g.drawRect((int) right.getXPos(), (int) right.getYPos(), right.getImage().getWidth(), right.getImage().getHeight());
 				}
 				
 				g.setColor(Color.magenta);
 				if(bottom != null){
 					g.drawRect((int) bottom.getXPos(), (int) bottom.getYPos(), bottom.getImage().getWidth(), bottom.getImage().getHeight());
 				}
 				if(bottomleft != null){
 					g.drawRect((int) bottomleft.getXPos(), (int) bottomleft.getYPos(), bottomleft.getImage().getWidth(), bottomleft.getImage().getHeight());
 				}
 				if(bottomright != null){
 					g.drawRect((int) bottomright.getXPos(), (int) bottomright.getYPos(), bottomright.getImage().getWidth(), bottomright.getImage().getHeight());
 				}
 				
 			}
 			g.dispose();
 			gamedata.bufferstrategy().show();
 			time = System.currentTimeMillis()-cycleStartTime; 
 			
 			try {
 				timeout = (1000 / REFRESHS_PER_SECOND)-time;
 				Thread.sleep((timeout > 0) ? timeout : 5);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 
 		}
 	}
 
 	private void initGameValues() {
 		SingletonWorker.gameData().setActivePlayer(new ActivePlayer(20, 20, 20,20, 20, 20, 20,
 				10, Player.CLASS_BRUTE, 200));
 
 		// this.gsd.getTextureList().add(gamedata.activePlayer());
 
 		this.fillItemsAndInventoryTest();
 
 
 		// gsd.setStructureList(MapBuilder.buildMap(new
 		// File("Current Mapname")));
 
 		this.addMenus();
 	}
 
 	private void updateMenus(long duration) {
 		for (UIItem uiitem : SingletonWorker.gameData().uiItemMap().values()){
 			uiitem.process(duration);
 		}
 	}
 
 	private void addMenus() {
 		SingletonWorker.gameData().uiItemMap().put(
 				GameProperties.MENU_ID_MAIN,
 				new MainMenu(0, 0, GameWindow.getWindowWidth(), GameWindow
 						.getWindowHeight(), GameProperties.MENU_PIC_MAIN));
 		SingletonWorker.gameData().uiItemMap().put(GameProperties.MENU_ID_INVENTORY,
 				this.createInventory());
 		SingletonWorker.gameData().uiItemMap().put(
 				GameProperties.MENU_ID_SKILL,
 				new SkillMenu(0, 0, GameWindow.getWindowWidth(), GameWindow
 						.getWindowHeight(), GameProperties.MENU_PIC_SKILL));
 		SingletonWorker.gameData().uiItemMap().put(
 				GameProperties.MENU_ID_VIDEO,
 				new SkillMenu(0, 0, GameWindow.getWindowWidth(), GameWindow
 						.getWindowHeight(), GameProperties.MENU_PIC_VIDEO));
 		SingletonWorker.gameData().uiItemMap().put(
 				GameProperties.MENU_ID_AUDIO,
 				new SkillMenu(0, 0, GameWindow.getWindowWidth(), GameWindow
 						.getWindowHeight(), GameProperties.MENU_PIC_AUDIO));
 	}
 
 	private void updateMenuActiveStates() {
 		for (int i = 0; i <= 4; i++) {
 			if (SingletonWorker.gameData().getActiveMenu() == i) {
 				SingletonWorker.gameData().uiItemMap().get(i).setActive(true);
 			} else {
 				SingletonWorker.gameData().uiItemMap().get(i).setActive(false);
 			}
 		}
 	}
 
 	private void hideOrShowMenu(int menu) {
 		if (SingletonWorker.gameData().getActiveMenu() == menu) {
 			SingletonWorker.gameData().setActiveMenu(Integer.MAX_VALUE);
 		} else {
 			SingletonWorker.gameData().setActiveMenu(menu);
 		}
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return true;
 
 		if (e.getKeyCode() == KeyEvent.VK_I
 				|| e.getKeyCode() == KeyEvent.VK_TAB) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED)
 				hideOrShowMenu(GameProperties.MENU_ID_INVENTORY);
 
 		} else if (e.getKeyCode() == KeyEvent.VK_K) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED)
 				hideOrShowMenu(GameProperties.MENU_ID_SKILL);
 
 		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED)
 				hideOrShowMenu(GameProperties.MENU_ID_MAIN);
 
 		} else if (e.getKeyCode() == KeyEvent.VK_E) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED){
 				int x = SingletonWorker.gameProperties().getPlayerBlockX();
 				int y = SingletonWorker.gameProperties().getPlayerBlockY();
 				SingletonWorker.gameData().getNetworkHandlerThread().placeLadder(x,y,11);
 				SingletonWorker.gameData().getNetworkHandlerThread().placeLadder(x,y,12);
 			}
 
 		} else if (e.getKeyCode() == KeyEvent.VK_W) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED) {
 				this.forwardPressed = true;
 			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
 				this.forwardPressed = false;
 			}
 
 		} else if (e.getKeyCode() == KeyEvent.VK_S) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED) {
 				this.backwardPressed = true;
 			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
 				this.backwardPressed = false;
 			}
 
 		} else if (e.getKeyCode() == KeyEvent.VK_A) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED) {
 				this.leftPressed = true;
 			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
 				this.leftPressed = false;
 			}
 
 		} else if (e.getKeyCode() == KeyEvent.VK_D) {
 
 			if (e.getID() == KeyEvent.KEY_PRESSED) {
 				this.rightPressed = true;
 			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
 				this.rightPressed = false;
 			}
 
 		}
 
 		// Allow the event to be redispatched
 		return false;
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return;
 
 		if (SingletonWorker.gameData().getActiveMenu() != Integer.MAX_VALUE) {
 			SingletonWorker.gameData().uiItemMap().get(SingletonWorker.gameData().getActiveMenu()).mouseKlicked(e);
 		} else {
 			SingletonWorker.gameData().activePlayer().attack(e.getX(), e.getY());
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return;
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return;
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return;
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		if (!SingletonWorker.gameData().gameLoaded)
 			return;
 
 		SingletonWorker.gameData().activePlayer().setMouseX(e.getX());
 		SingletonWorker.gameData().activePlayer().setMouseY(e.getY());
 	}
 
 	private DrawableInventory createInventory() {
 		RelativeBoxPosition[] equipmentBoxes = new RelativeBoxPosition[9];
 		equipmentBoxes[0] = GameProperties.INV_RELATIVE_HAT_BOX;
 		equipmentBoxes[1] = GameProperties.INV_RELATIVE_EYE_PATCH_BOX;
 		equipmentBoxes[2] = GameProperties.INV_RELATIVE_PARROT_BOX;
 		equipmentBoxes[3] = GameProperties.INV_RELATIVE_CHEST_BOX;
 		equipmentBoxes[4] = GameProperties.INV_RELATIVE_WAEPON_BOX;
 		equipmentBoxes[5] = GameProperties.INV_RELATIVE_SECOND_HAND_BOX;
 		equipmentBoxes[6] = GameProperties.INV_RELATIVE_PANTS_BOX;
 		equipmentBoxes[7] = GameProperties.INV_RELATIVE_SHOE_BOX;
 		equipmentBoxes[8] = GameProperties.INV_RELATIVE_WOODEN_LEG_BOX;
 
 		return new DrawableInventory(0, 0, GameWindow.getWindowWidth(),
 				GameWindow.getWindowHeight(), GameProperties.MENU_PIC_INV,
 				GameProperties.INV_ROWS, GameProperties.INV_CELLS,
 				GameProperties.INV_RELATIVE_FIRST_BOX,
 				GameProperties.INV_RELATIVE_DISTANCE_X,
 				GameProperties.INV_RELATIVE_DISTANCE_Y,
 				GameProperties.INV_RELATIVE_BACK_BTN,
 				GameProperties.INV_RELATIVE_FWD_BTN, equipmentBoxes);
 	}
 
 }
