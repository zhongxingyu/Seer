 package com.ionmarkgames.ys.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.ionmarkgames.ys.client.objects.Enemy;
 import com.ionmarkgames.ys.client.objects.GameDir;
 import com.ionmarkgames.ys.client.objects.Sprite;
 import com.ionmarkgames.ys.client.objects.Wall;
 import com.ionmarkgames.ys.client.objects.You;
 import com.ionmarkgames.ys.client.ui.IncreaseStatInquery;
 import com.ionmarkgames.ys.client.ui.LifePanel;
 import com.ionmarkgames.ys.client.ui.MessagePanel;
 import com.ionmarkgames.ys.client.ui.UICallback;
 
 public class YSPanel extends AbsolutePanel {
 	
     public static final int TILE_HEIGHT = 20;
     public static final int TILE_WIDTH = 20;
     private static final int TICKER_TIME = 50;
     
     private List<Sprite> sprites = new ArrayList<Sprite>();
     private List<Sprite> toAdd = new ArrayList<Sprite>();
     private List<Sprite> toRemove = new ArrayList<Sprite>();
     private List<Enemy> enemies = new ArrayList<Enemy>();
     private Sprite[][] grid;
     private TextArea keyHandler;
     private You player;
     
     private GameController control;
     private String intro;
     private String albertText;
     private String wallImageUrl;
     private int maxEnemies = 10;
     
     private LifePanel lifePanel = new LifePanel();
     
     private int mapHeight; // Max = 51 
     private int mapWidth; // Max = 36
     
     private Timer ticker = new Timer() {
         @Override
         public void run() {
             tick();
         }
     };
     
     public YSPanel(GameController controller) {
     	control = controller;
     	this.addStyleName("level_" + control.getLevel());
         this.keyHandler = new TextArea();
         keyHandler.addKeyDownHandler(new KeyDownHandler() {
             public void onKeyDown(KeyDownEvent event) {
                 switch (event.getNativeKeyCode()) {
                     case KeyCodes.KEY_UP:
                         player.moveUp();
                         break;
                     case KeyCodes.KEY_DOWN:
                         player.moveDown();
                         break;
                     case KeyCodes.KEY_RIGHT:
                         player.moveRight();
                         break;
                     case KeyCodes.KEY_LEFT:
                         player.moveLeft();
                         break;
                 }
             }
         });
         keyHandler.addKeyPressHandler(new KeyPressHandler() {
             public void onKeyPress(KeyPressEvent event) {
                 switch(event.getCharCode()) {
                     case 'w':
                     case 'W':
                         player.shoot(GameDir.UP);
                         break;
                     case 'a':
                     case 'A':
                         player.shoot(GameDir.LEFT);
                         break;
                     case 's':
                     case 'S':
                         player.shoot(GameDir.DOWN);
                         break;
                     case 'd':
                     case 'D':
                         player.shoot(GameDir.RIGHT);
                         break;
                 }
             }
         });
         RootPanel.get("StatsArea").clear();
         RootPanel.get("StatsArea").add(this.lifePanel);
         RootPanel.get("StatsArea").setVisible(true);
     }
     
     public void start() {
     	
     	control.loading();
     	
         this.grid = new Sprite[this.mapWidth][this.mapHeight];
         this.generateMap();
         
         this.player = new You(this);
         this.visit(player, player.getX() / TILE_WIDTH, player.getY() / TILE_WIDTH);
         control.updatePlayer(player);
         this.updateLifePanel(player.getHealth());
         
         this.addEnemies();
         
         control.hidLoading();
         
         this.showIntro();
     }
     
     private void showIntro() {
     	MessagePanel msg = new MessagePanel(this.intro, new UICallback<Boolean>() {
 			public void failed() {
 			}
 			
 			public void done(Boolean obj) {
 				add(keyHandler);
 				keyHandler.getElement().focus();
 				ticker.scheduleRepeating(TICKER_TIME);
 			}
 		});
         
         msg.animate();
     }
     
     private void addEnemies() {
     	for (int i = 0; i < this.maxEnemies; i++) {
    		Enemy enemy = control.getLevelEnemy();
    		this.visit(enemy, enemy.gridX(), enemy.gridY());
        	this.addSprite(enemy);
         }
     }
     
     public void tick() {
     	try {
     		keyHandler.getElement().focus();
     		
     		player.act();
     		
     		for (Sprite sprite : toAdd) {
                 this.sprites.add(sprite);
                 if (sprite.isEnemy()) {
                     Enemy enemy = (Enemy) sprite;
                     this.enemies.add(enemy);
                     this.visit(sprite, sprite.gridX(), sprite.gridY());
                 }
             }
             this.toAdd.clear();
     		
     		for (Sprite sprite : this.toRemove) {
     			if (this.sprites.remove(sprite)) {
     				this.leave(sprite, sprite.gridX(), sprite.gridY());
     				sprite.remove();
     				if (sprite.isEnemy()) {
     					this.enemies.remove((Enemy) sprite);
     					if (this.enemies.size() < 1) {
     						this.endLevel();
     					}
     				}
     			}
     		}
     		this.toRemove.clear();
     	
     		for(Sprite sprite : this.sprites) {
     		    if (sprite.isWall()) {
     		        continue;
     		    }
     			if (sprite.isEnemy()) {
     				this.leave(sprite, sprite.gridX(), sprite.gridY());
     				sprite.act();
     				this.visit(sprite, sprite.gridX(), sprite.gridY());
     			} else {
     				sprite.act();
     			}
     		}
     	} catch (RestartException e) {
     		this.resetLevel();
     	}
     }
     
     public boolean visit(Sprite sprite, int x, int y) {
         if (this.grid[x][y] != null) {
             this.grid[x][y].visit(sprite);
         } else {
             this.grid[x][y] = sprite;
         }
         return true;
     }
     
     public void leave(Sprite sprite, int x, int y) {
         this.grid[x][y] = null;
     }
     
     private void generateMap() {
         MapBuilder builder = new MapBuilder();
         boolean[][] map = builder.buildRandomMap(this.mapWidth, this.mapHeight);
         for (int i = 0; i < map.length; i++) {
         	int len = map[i].length;
             for (int j = 0; j < len; j++) {
                 if (!map[i][j]) {
                     Sprite wall = new Wall(this, this.wallImageUrl, i * TILE_WIDTH, j * TILE_HEIGHT);
                     this.sprites.add(wall);
                     this.visit(wall, i, j);
                 }
             }
         }
     }
     
     public boolean passable(int x, int y) {
         if (x < 0 || y < 0 || x >= this.mapWidth || y >= this.mapHeight) {
             return false;
         }
         return this.grid[x][y] == null || this.grid[x][y].passable();
     }
     
     public boolean hasEnemy(int x, int y) {
     	if (this.passable(x, y) && this.grid[x][y] != null) {
     		return this.grid[x][y].isEnemy();
     	}
     	return false;
     }
     
     public void removeSprite(Sprite sprite) {
     	this.toRemove.add(sprite);
     }
     
     public void addSprite(Sprite sprite) {
         this.toAdd.add(sprite);
     }
     
     private void albertMessage() {
     	RootPanel messageSpace = RootPanel.get("MessageArea");
     	messageSpace.clear();
     	HTML message = new HTML(this.albertText);
     	message.addStyleName("albert_message");
     	
     	Timer delay = new Timer() {
 			@Override
 			public void run() {
 				control.nextLevel();
 			}
     	};
     	messageSpace.add(message);
     	delay.schedule(1000);
     }
     
     private void endLevel() {
     	this.ticker.cancel();
 		this.remove(this.keyHandler);
 		
 		IncreaseStatInquery inquery = new IncreaseStatInquery(player, new UICallback<Integer>() {
 
 			public void done(Integer obj) {
 				control.persistPlayer(player);
 				albertMessage();
 			}
 
 			public void failed() {
 			}
 			
 		});
 		inquery.show();
     }
     
     private void resetLevel() {
     	// Clear up
     	this.ticker.cancel();
 		this.remove(this.keyHandler);
 		
 		for (Sprite sprite : this.sprites) {
 			if (sprite.isEnemy() || sprite.isBullet()) {
 				this.leave(sprite, sprite.getX() / TILE_WIDTH, sprite.getY() / TILE_HEIGHT);
 				sprite.remove();
 				this.toRemove.add(sprite);
 			}
 		}
 		this.enemies.clear();
 		
 		this.leave(player, player.gridX(), player.gridY());
 		player.remove();
 		
 		// Rebuild
 		player = new You(this);
 		control.updatePlayer(player);
 		this.updateLifePanel(player.getHealth());
 		
 		this.addEnemies();
 		
 		this.showIntro();
     }
     
     public void updateLifePanel(int life) {
     	this.lifePanel.update(life);
     }
 
 	public You getPlayer() {
 		return this.player;
 	}
 
 	public void setIntro(String intro) {
 		this.intro = intro;
 	}
 
 	public void setAlbertText(String albertText) {
 		this.albertText = albertText;
 	}
 
 	public void setMapHeight(int mapHeight) {
 		this.mapHeight = mapHeight;
 	}
 
 	public int getMapHeight() {
 		return mapHeight;
 	}
 
 	public void setMapWidth(int mapWidth) {
 		this.mapWidth = mapWidth;
 	}
 
 	public int getMapWidth() {
 		return mapWidth;
 	}
 
 	public void setWallImageUrl(String wallImageUrl) {
 		this.wallImageUrl = wallImageUrl;
 	}
 
     public void setMaxEnemies(int maxEnemies) {
         this.maxEnemies = maxEnemies;
     }
 
     public int getMaxEnemies() {
         return maxEnemies;
     }
 }
