 package riskyspace.view.swingImpl;
 
 import java.awt.GraphicsEnvironment;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFrame;
 
 import riskyspace.logic.FleetMove;
 import riskyspace.logic.SpriteMapData;
 import riskyspace.model.BuildAble;
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Planet;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.model.Territory;
 import riskyspace.model.World;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.view.View;
 
 public class SwingView implements View {
 
 	private JFrame frame = null;
 	private RenderArea renderArea = null;
 	
 	public SwingView(int rows, int cols) {
 		setFrame();
 		renderArea = new RenderArea(rows, cols);
 		frame.add(renderArea);
		frame.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent event) {
 				if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					System.exit(0);
 				} else if (event.getKeyCode() == KeyEvent.VK_SPACE) {
 					Event evt = new Event(Event.EventTag.MOVE, null);
 					EventBus.CLIENT.publish(evt);
 				} else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
 					Event evt = new Event(Event.EventTag.NEXT_TURN, null);
 					EventBus.CLIENT.publish(evt);
 				}
 			}
 			@Override public void keyReleased(KeyEvent arg0) {}
 			@Override public void keyTyped(KeyEvent arg0) {}
 		});
 	}
 	
 	private void setFrame() {
 		frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setIgnoreRepaint(true);
 		frame.setUndecorated(true);
 		System.out.println("Mem: " + GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getAvailableAcceleratedMemory());
 		/*
 		 * If Mem is small set non transparent textures!
 		 */
 		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()) {
 			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
 		} else {
 			System.err.println("Fullscreen not supported");
 		}
 	}
 
 	@Override
 	public void draw() {
 		renderArea.repaint();
 	}
 
 	@Override
 	public void setViewer(Player player) {
 		renderArea.setViewer(player);
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		frame.setVisible(visible);
 	}
 
 	@Override
 	public boolean isVisible() {
 		return frame.isVisible();
 	}
 
 	@Override
 	public void updateData(SpriteMapData smd) {
 		renderArea.updateData(smd);
 	}
 
 	@Override
 	public void setPlayerStats(PlayerStats stats) {
 		renderArea.setStats(stats);
 	}
 	
 	@Override
 	public void setQueue(Map<Colony, List<BuildAble>> colonyQueues) {
 		renderArea.setQueue(colonyQueues);
 	}
 
 	@Override
 	public void showPlanet(Territory selection) {
 		renderArea.showTerritory(selection);
 	}
 
 	@Override
 	public void showColony(Colony selection) {
 		renderArea.showColony(selection);
 	}
 
 	@Override
 	public void showFleet(Fleet selection) {
 		renderArea.showFleet(selection);
 	}
 
 	@Override
 	public void hideMenus() {
 		renderArea.hideSideMenus();
 	}
 
 	@Override
 	public void setActivePlayer(Player player) {
 		renderArea.setActivePlayer(player);
 	}
 }
