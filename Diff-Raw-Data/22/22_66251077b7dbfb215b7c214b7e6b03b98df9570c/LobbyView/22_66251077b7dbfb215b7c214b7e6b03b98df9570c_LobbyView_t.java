 package riskyspace.view.lobby;
 
 import java.awt.GraphicsEnvironment;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JFrame;
 
 import riskyspace.Main;
 import riskyspace.sound.PlayList;
 
 public class LobbyView implements Observer {
 	
 	private JFrame frame = null;
 	private StartScreen startScreen = null;
 	private Thread renderThread;
 	private PlayList playList;
 	private boolean interrupt = false;
 	
 	public LobbyView () {
 		setFrame();
 		playList = new PlayList(PlayList.STANDARD_LOBBY_LOOP);
 		startScreen = new StartScreen(playList);
 		startScreen.setObserver(this);
 		frame.addKeyListener(startScreen.getKeyListener());
 		frame.add(startScreen);
 		frame.setVisible(true);
 		renderThread = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				while (!interrupt) {
 					frame.repaint();
 					try {
 						Thread.sleep(1000/30);	
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 		}});
 		renderThread.start();
 	}
 	
 	private void setFrame() {
 		frame = Main.getFrame();
 		frame.setIgnoreRepaint(true);
 		frame.setUndecorated(true);
 		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()) {
 			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
 		} else {
 			System.err.println("Fullscreen not supported");
 		}
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if ("dispose".equals(arg)) {
 			if (renderThread != null && renderThread.isAlive()) {
 				interrupt = true;
 			}
 			playList.pause();
 			if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()) {
 				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
 			} else {
 				System.err.println("Fullscreen not supported");
 			}
 			frame.dispose();
		}else if("disconnect".equals(arg)){
			startScreen.goBack();
 		}
 	}
 }
