 package dungeonGame;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Resource;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 
 public class DungeonView implements Observer {
 	private ArrayList<Resource> cleanUp;
 	private Color dGray, gray, lGray;
 	private Image rock, goo, hero, stairsUp, stairsDown/*, floorTexture*/;
 	private DungeonGame newGame;
 	
 	private Shell shell;
 	private Canvas floor;
 	private Composite menu;
 	
 	public DungeonView(Shell s) {
 		newGame = new DungeonGame();
 		newGame.addObserver(this);
 		this.shell = s;
 		
 		cleanUp = new ArrayList<Resource>(10);
 		makeColors();
 		createImages();
 		
 		GridLayout layout;
 		GridData data;
 		layout = new GridLayout();
 		layout.numColumns = 2;
 		shell.setBackground(dGray);
 		shell.setLayout(layout);
 		
 		// Floor Initialization
 		floor = new Canvas(shell, SWT.NONE);
 		floor.setBackground(gray);
 		//floorTexture = new Image(shell.getDisplay(), DungeonView.class.getResourceAsStream("images/premade_tile_single.png"));
 		//floor.setBackgroundImage(floorTexture);
 		
 		
 		data = new GridData();
 		data.horizontalAlignment = SWT.BEGINNING;
 		data.heightHint = 500;
 		data.widthHint = 500;
 		floor.setLayoutData(data);
 		
 		// Menu Initialization
 		menu = new Composite(shell, SWT.NONE);
 		menu.setBackground(lGray);
 		data = new GridData();
 		data.horizontalAlignment = SWT.END;
 		data.grabExcessVerticalSpace = true;
 		data.grabExcessHorizontalSpace = true;
 		data.minimumHeight = 400;
 		data.widthHint = 200;
 		menu.setLayoutData(data);
 		
 		// Paint Listener ///////////
 		floor.addListener(SWT.Paint, new Listener () {
 			public void handleEvent(Event event) {
 				ArrayList<GameObject> objects = newGame.getFloor().getObjects();
 				int unit = newGame.getFloor().UNIT_SIZE;
 				for(GameObject obj : objects) {
 					String life = "";
 					if(obj instanceof Mob) {
 						event.gc.drawImage(goo, obj.getPos().x, obj.getPos().y);
 						life = ((Mob) obj).getCurrHealth() + " / " + ((Mob) obj).getMaxHealth();
 						event.gc.setForeground(event.display.getSystemColor(SWT.COLOR_WHITE));
 						event.gc.drawText(life, obj.getPos().x + 5, obj.getPos().y + 5, true);
 					}
 					else if(obj instanceof Obstacle) {
 						event.gc.drawImage(rock, obj.getPos().x, obj.getPos().y);
 						life = "";
 					} 
 					else if(obj instanceof Stair) {
 						if(((Stair) obj).getDescent() == true) {
 							event.gc.drawImage(stairsDown, obj.getPos().x, obj.getPos().y);
 						} else { event.gc.drawImage(stairsUp, obj.getPos().x, obj.getPos().y); }
 					}
 					else if(!(obj instanceof Hero)) {
 						event.gc.setBackground(event.display.getSystemColor(SWT.COLOR_YELLOW));
 						event.gc.fillRectangle(obj.getPos().x, obj.getPos().y, unit, unit);
 					}
 				}
 				
 				// Drawing Hero
 				Hero heroObj = newGame.getHero();
 				event.gc.drawImage(hero, heroObj.getPos().x, heroObj.getPos().y);
 				
 				// Drawing Hero range indicator
 				int width = unit / 4;
 				event.gc.setBackground(event.display.getSystemColor(SWT.COLOR_YELLOW));
 				event.gc.fillOval(heroObj.getCrosshair().x - (width / 2), heroObj.getCrosshair().y  - (width / 2), width, width);
 				
 			}
 		});
 		
 		menu.addListener(SWT.Paint, new Listener () {
 			public void handleEvent (Event event) {
 				Hero hero = newGame.getHero();
 				int firstRow = 10;
 				int rowHeight = 20;
 				event.gc.drawText(hero.getPos().x + "," + hero.getPos().y, 10, firstRow);
 				event.gc.drawText("Speed: " + hero.getSpeed(), 10, firstRow += rowHeight);
 				event.gc.drawText("keyFlags: " + newGame.keyFlags.values() , 10, firstRow += rowHeight);
 				event.gc.drawText("Experience: " + hero.getExperience(), 10, firstRow += rowHeight);
 				event.gc.drawText("Money: " + hero.getBooty(), 10, firstRow += rowHeight);
 			}
 		});
 		
 		// Dispose Listener
 		shell.addDisposeListener(new DisposeListener () {
 			public void widgetDisposed(DisposeEvent e) {
 				for(Resource c : cleanUp) {
 					c.dispose();
 				}
 			}
 		});
 		
 	}
 	
 	public void makeColors() {
 		addResource(dGray = new Color(shell.getDisplay(), 30, 30, 30));
 		addResource(gray = new Color(shell.getDisplay(), 150, 150, 150));
 		addResource(lGray = new Color(shell.getDisplay(), 200, 200, 200));
 	}
 	
 	public void createImages() {
 		rock = new Image(shell.getDisplay(), getClass().getResourceAsStream("images/rock_base_2.png"));
 		ImageData rockData = rock.getImageData();
 		RGB rockRgb = rockData.palette.getRGB(rockData.getPixel(0, 0));
 		rockData.transparentPixel = rockData.palette.getPixel(rockRgb);
 		addResource(rock = new Image(shell.getDisplay(), rockData.scaledTo(50,50)));
 		
 		goo = new Image(shell.getDisplay(), getClass().getResourceAsStream("images/goo_1_1.png"));
 		ImageData gooData = goo.getImageData();
 		RGB gooRgb = gooData.palette.getRGB(gooData.getPixel(0, 0));
 		gooData.transparentPixel = gooData.palette.getPixel(gooRgb);
 		addResource(goo = new Image(shell.getDisplay(), gooData.scaledTo(50,50)));
 		
 		hero = new Image(shell.getDisplay(), getClass().getResourceAsStream("images/hero_1.png"));
 		ImageData heroData = hero.getImageData();
 		RGB heroRgb = rockData.palette.getRGB(heroData.getPixel(0, 0));
 		heroData.transparentPixel = rockData.palette.getPixel(heroRgb);
 		addResource(hero = new Image(shell.getDisplay(), heroData.scaledTo(50,50)));
 		
 		stairsUp = new Image(shell.getDisplay(), getClass().getResourceAsStream("images/stair_up.png"));
 		ImageData stairsUpData = stairsUp.getImageData();
 		RGB stairsUpRgb = stairsUpData.palette.getRGB(stairsUpData.getPixel(0, 0));
 		stairsUpData.transparentPixel = stairsUpData.palette.getPixel(stairsUpRgb);
 		addResource(stairsUp = new Image(shell.getDisplay(), stairsUpData.scaledTo(50,50)));
 		
 		stairsDown = new Image(shell.getDisplay(), getClass().getResourceAsStream("images/stairs_down.png"));
 		ImageData stairsDownData = stairsDown.getImageData();
 		RGB stairsDownRgb = stairsDownData.palette.getRGB(stairsDownData.getPixel(0, 0));
 		stairsDownData.transparentPixel = stairsDownData.palette.getPixel(stairsDownRgb);
 		addResource(stairsDown = new Image(shell.getDisplay(), stairsDownData.scaledTo(50,50)));
 	}
 	
 	public void addResource(Resource r) {
 		cleanUp.add(r);
 	}
 	
 	@Override
 	public void update(Observable obs, Object obj) {
 		if(shell.isDisposed()) {
 			System.out.println("Runnable: shell is disposed!");
 			return;
 		}
 		floor.redraw();
 		menu.redraw();
 	}
 	
 	public void addController(DungeonController controller) {
 		shell.addKeyListener(controller);
 	}
 	
 	public DungeonGame getGame() { return newGame; }
 
 }
