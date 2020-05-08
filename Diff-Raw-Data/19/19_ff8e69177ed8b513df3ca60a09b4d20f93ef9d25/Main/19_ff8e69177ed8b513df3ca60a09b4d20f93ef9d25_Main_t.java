 package javachallenge.graphics;
 
 import java.awt.Dimension;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 
 import javachallenge.graphics.components.Label;
 import javachallenge.graphics.components.MapPanel;
 import javachallenge.graphics.components.Screen;
 import javachallenge.graphics.util.ColorMaker;
 import javachallenge.graphics.util.HTMLMaker;
 import javachallenge.graphics.util.ImageHolder;
 import javachallenge.graphics.util.Position;
 
 public class Main {
 	public void run() {
 
 		@SuppressWarnings("serial")
		Screen screen = new Screen("Java Challenge") {
 			{
 				getContentPane().setBackground(ColorMaker.black);
 			}
 		};
 
 		Label[] loadings = { 
 			new Label(380, 300, 36, 36, ImageHolder.waiting),
 			new Label(360, 270, 80, 20, new HTMLMaker("Loading...", ColorMaker.white, 10).toString()),
 		};
 		for (Label loading : loadings)
 			screen.add(loading);
 
 		screen.setPreferredSize(new Dimension(800, 600));
 		screen.setVisible(true);
 		Client client = new Client(40, 20, new Position[] { new Position(4, 2) });
 		final MapPanel map = client.getPanel();
 		screen.add(map);
 
 		for (Label loading : loadings)
 			screen.remove(loading);
 		screen.addComponentListener(new ComponentListener() {
 			public void componentShown(ComponentEvent arg0) {
 			}
 
 			public void componentMoved(ComponentEvent arg0) {
 			}
 
 			public void componentHidden(ComponentEvent arg0) {
 			}
 
 			public void componentResized(ComponentEvent arg0) {
 				map.setLocation(10, 10);
 				Dimension size = arg0.getComponent().getSize();
 				map.setSize(size.width * 3 / 4, size.height - 60);
 			}
 		});
 
 		map.setLocation(10, 10);
 		Dimension size = screen.getSize();
 		map.setSize(size.width * 3 / 4, size.height - 60);
 	}
 }
