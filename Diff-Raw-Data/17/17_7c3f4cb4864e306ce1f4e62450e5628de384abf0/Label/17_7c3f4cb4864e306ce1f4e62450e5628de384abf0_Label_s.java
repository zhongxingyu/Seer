 package window;
 
 import engine.Engine;
 import exceptions.RealPlayerException;
 import game.Game;
 import game.base.Base;
 import game.player.RealPlayer;
 import game.tower.Tower;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.accessibility.Accessible;
 import javax.swing.Icon;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import commands.market.SellTower;
 import commands.market.UpgradeTower;
 
 import window.graphic.GraphicElement;
 import window.panel.PanelTmpTower;
 
 public class Label extends JLabel implements Accessible, MouseListener, KeyListener, MouseMotionListener{
 
 
 	private static final long serialVersionUID = 2250314127870259478L;
 
 	protected ActionListener action;
 	protected GraphicElement graph;
 
 	/**
 	 * @return the graph
 	 */
 	public GraphicElement getGraph() {
 		return graph;
 	}
 
 	/**
 	 * @param graph the graph to set
 	 */
 	public void setGraph(GraphicElement graph) {
 		this.graph = graph;
 	}
 
 	// ----------------------------------------- MouseListener ----------------------
 
 	public void addMouseListener(ActionListener actionListener) {
 		action = actionListener;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 		
 		Game game = Game.getInstance();
 		
 		//Try to deselect the player
 		RealPlayer realPlayer = null;
 		try {
 			realPlayer = (RealPlayer) Game.getInstance().getPlayerManager().getRealPlayer();
 		} catch (RealPlayerException e) {
 
 			e.printStackTrace();
 			return;
 		}
 		
 		int x = event.getX();
 		int y = event.getY();
 		
 		// This click is it a click on an existing tower ?
 		for(final Tower tower: Game.getInstance().getTowerManager().getTowers()) {
 			float tx = tower.getPosition().x;
 			float ty = tower.getPosition().y;
 			if ( tower.getOwner().equals(realPlayer) && (x>= tx && x < tx + 40) && (y>= ty && y < ty + 40) ) {
 				
 				final JPopupMenu menu = new JPopupMenu();
 				
 				// item to upgrade the tower
 				String price = (tower.getUpgradePrice() < Integer.MAX_VALUE) ? "-$"+tower.getUpgradePrice() : "impossible!";
 			    JMenuItem item = new JMenuItem("Upgrade tower ("+price+")");
 			    item.addActionListener(new ActionListener() {
 					
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						// need to implement UpgradeTower
 						UpgradeTower command = new UpgradeTower(tower);
 						Engine.getInstance().getCommands().add(command);
 					}
 				});
 			    menu.add(item);
 			    
 			    // item to sell the tower
 			    JMenuItem item2 = new JMenuItem("Sell tower (+$"+tower.getSellPrice()+")");
 			    item2.addActionListener(new ActionListener() {
 					
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						SellTower command = new SellTower(tower);
 						Engine.getInstance().getCommands().add(command);
 					}
 				});
 			    menu.add(item2);
 			    
 			    menu.show(event.getComponent(), x, y);
 			    
 
 			    
 				return;
 			}
 		}
 
 		
 		if(realPlayer.getSelectedBases() != null) {
 			realPlayer.getSelectedBases().setBackground(realPlayer.getColor().brighter());
 		}
 		realPlayer.setSelectedBases(null);
 		
 		// Click on the map make disappear the line
 		AppliWindow.getInstance().getLine().setDisplayed(false);
 		AppliWindow.getInstance().getLine().displayLine(0, 0, 0, 0);
 		
 		
 		// This clic is it in order to build a tower ?
 		if (realPlayer.isBuildingTower()) {
 			int numArea = game.getMapManager().getNumAreaAtPosition(event.getX(), event.getY());
 			
 			if (numArea == -1) {
 				//System.out.println("C'est une zone de plaine !");
 			}
 			else if(numArea >= 0 && numArea < game.getBaseManager().getBases().size()) {
 				Base baseArea = game.getBaseManager().getBases().get(numArea);
 				if (realPlayer.equals(baseArea.getPlayer())) {
 					realPlayer.buyTower(realPlayer, "GunTower", x, y);
 				}
 				else {
 					//System.out.println("C'est pas ta zone !");
 				}
 			}
 			else {
 				System.out.println("Problem with the index in map");
 			}
 		
 		}
 		
 		realPlayer.setBuildingTower(false);
 		AppliWindow.getInstance().getPanelTmpTower().setVisible(false);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent event) {
 	}
 
 	/**
 	 * Not used, but it can show in where the Mouse leaves the Label, i.e when it enters
 	 * the Bases.
 	 * 
 	 * @deprecated
 	 */
 	@Override
 	@Deprecated
 	public void mouseExited(MouseEvent event) {
 		
 		int radiusBase = AppliWindow.getInstance().getTilesSize();
 		int xM = event.getX(), yM = event.getY();
 		int distX  = 0, distY = 0;
 
 		for(Base b : Game.getInstance().getBaseManager().getBases()){
 			distX = (int) Math.abs(xM - b.getPosition().getX());
 			distY = (int) Math.abs(yM - b.getPosition().getY());
 			if(distX > radiusBase && distY > radiusBase) {
 				break;
 			}
 			else{
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 	}
 	
 	// ----------------------------------------- MouseMotionListener -----------------
 	@Override
 	public void mouseDragged(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent event) {
 		
 		try {
 			Game game = Game.getInstance();
 			RealPlayer realPlayer = (RealPlayer)game.getPlayerManager().getRealPlayer();
 
 			if (realPlayer.isBuildingTower()) {
 				boolean acceptBuildTower = false;
 				
				int numArea = game.getMapManager().getNumAreaAtPosition(event.getX(), event.getY());
 				
 				if(numArea >= 0 && numArea < game.getBaseManager().getBases().size()) {
 					Base baseArea = game.getBaseManager().getBases().get(numArea);
 					if (realPlayer.equals(baseArea.getPlayer())) {
 						acceptBuildTower = true;
 					}
 				}
 				
 				if (acceptBuildTower) {
 					PanelTmpTower panelTmpTower = AppliWindow.getInstance().getPanelTmpTower();
 					panelTmpTower.setBounds(event.getX(), event.getY(), 30, 30);
 					panelTmpTower.setVisible(true);
 				}
 				else {
 					PanelTmpTower panelTmpTower = AppliWindow.getInstance().getPanelTmpTower();
 					panelTmpTower.setVisible(false);
 				}
 			}
 		} catch (RealPlayerException e1) {
 			e1.printStackTrace();
 		}
 		
 		try {
 			if(Game.getInstance().getPlayerManager().getRealPlayer().getSelectedBases() != null) {	
 				int x = (int) Game.getInstance().getPlayerManager().getRealPlayer().getSelectedBases().getPosition().getX() + AppliWindow.getInstance().getTilesSize()/2;
 				int y = (int) Game.getInstance().getPlayerManager().getRealPlayer().getSelectedBases().getPosition().getY() + AppliWindow.getInstance().getTilesSize()/2;
 				AppliWindow.getInstance().getLineCursor().displayLine(x, y, event.getX(), event.getY());
 			}
 			else {
 				AppliWindow.getInstance().getLineCursor().displayLine(0,0,0,0);
 			}
 		} catch (RealPlayerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	// ----------------------------------------- KeyListener--------------------------
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 	// ----------------------------------------- Accessor -----------------------------
 
 	// ------------------------------------------ constructor -------------------------
 	public Label() {
 		super();
 		this.graph = new GraphicElement();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	public Label(Icon image, int horizontalAlignment) {
 		super(image, horizontalAlignment);
 		this.graph = new GraphicElement();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	public Label(Icon image) {
 		super(image);
 		this.graph = new GraphicElement();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	public Label(String text, Icon icon, int horizontalAlignment) {
 		super(text, icon, horizontalAlignment);
 		this.graph = new GraphicElement();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	public Label(String text, int horizontalAlignment) {
 		super(text, horizontalAlignment);
 		this.graph = new GraphicElement();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	public Label(String text) {
 		super(text);
 		this.graph = new GraphicElement();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 
 
 }
