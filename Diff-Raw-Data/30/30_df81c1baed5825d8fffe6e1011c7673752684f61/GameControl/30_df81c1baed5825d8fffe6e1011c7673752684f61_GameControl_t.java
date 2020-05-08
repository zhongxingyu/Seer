 package game;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import sound.BeatListener;
 import sound.BeatTrack;
 import sound.Jukebox;
 
 public class GameControl implements Runnable, BeatListener{
 	long milliseconds = new Date().getTime();
 	private JPanel frame;
 	private LinkedList<Wave> waves;
 	private Player player;
 	private Color lineColor = Color.GREEN;
 	private Jukebox jukebox;
 
 	private float xspeed = 15.55f;
 	private int intensity = 1;
 	private long prevTime;
 	private long currTime;
 
 	private int bpm = 140;
 
 
 	public GameControl() {
 		JFrame mainframe = new JFrame("HeArT bEaT");
 		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainframe.setResizable(false);
 		mainframe.setSize(800, 480);
 		frame = new GameFrame(this);
 		frame.setVisible(true);
 		
 		jukebox = new Jukebox();
 		mainframe.add(frame);
 		mainframe.setVisible(true);
 	}
 
 	public float getXSpeed() {
 		return xspeed;
 	}
 
 	public Color getLineColor() {
 		return lineColor;
 	}
 
 	public int getBPM() {
 		return bpm;
 	}
 	
 	public void startGame() {
 		player = new Player();
 		player.setPosition(frame.getWidth()/4,0);
 		frame.addKeyListener(player);
 		prevTime = Calendar.getInstance().getTimeInMillis();
 		waves = new LinkedList<Wave>();
 		intensity = 1;
 		endOfTrack();
 		Thread t = new Thread(this);
 		t.start();
 	}
 
 	public void paint(Graphics g) {
 		g.setColor(Color.GREEN);
 		//Call the render() method of all GameObjects and draw the line of the EKG
 		player.render((Graphics2D)g);
 		float posX = 0;
 		for (Wave w : waves){
 			w.render((Graphics2D) g);
 			g.drawLine((int)posX, 0, (int)w.x, 0);
 			posX = (int) w.x + w.getWidth();
 		}
 		g.drawLine((int)posX, 0, frame.getWidth(), 0);
 		
 	}
 	
 
 	@Override
 	public void run() {
 		while (true) {
 			//Calculate time since last step
 			//Call update() method of all GameObjects
 			float tpf;
 	
 			currTime = Calendar.getInstance().getTimeInMillis();
 			tpf = (float)(currTime - prevTime)/30000;
 			
			
 			player.update(tpf);
 			for (Wave w : waves) {
 				w.update(tpf);
 				if (CollisionDetector.collision(player.getCollisionLines(), 
 						w.getCollisionLines())) {
 					System.out.println("Collision detected: player must die!");
 				}
 			}
 			if (waves.size() > 0 && waves.get(0).x < -waves.get(0).getWidth()) {
 				waves.removeFirst();
 			}
 			prevTime = currTime;
 			frame.repaint();
 			try {
 				Thread.sleep(1000/60);
 			} catch (InterruptedException e) {
 				System.out.println("Thread interrupted");
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 	}
 
 	@Override
 	public void endOfTrack() {
 		//Schedule a new track!
 		new Timer().schedule(new TimerTask() {
 			public void run() {
 				jukebox.playMotif((int)Math.ceil(Math.random()*4), intensity);
 			}
 		}, BeatTrack.DELAY);
 	}
 
 	@Override
 	public void beat() {
 		//Create a wave
 		waves.addLast(new Wave(8,48,4));
 		//Schedule a heartbeat
 		new Timer().schedule(new TimerTask() {
 			public void run() {
				jukebox.playHeartBeat();
 			}
 		}, BeatTrack.DELAY);
 	}	
 
 }
