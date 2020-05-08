 package spacegamecraft;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.Random;
 
 import javax.swing.JFrame;
 
 import spacegamecraft.data.Galaxy;
 import spacegamecraft.data.gen.LevelGen;
 import spacegamecraft.geo.Point;
 import spacegamecraft.gfx.Buffer;
 import spacegamecraft.gfx.Color;
 import spacegamecraft.gfx.Font;
 import spacegamecraft.player.Player;
 
 
 public class Game extends Canvas implements Runnable {
 	/**
 	 * This is really just to placate Eclipse.
 	 */
 	private static final long serialVersionUID = 1L;
 	public boolean running;
 	public int ticks = 0;
 	public static int SCALE = 2;
 	public static int WIDTH = 320;
 	public static int HEIGHT = 480;
 	public BufferedImage buffer = new BufferedImage(HEIGHT, WIDTH, BufferedImage.TYPE_INT_RGB);
 	private Buffer buf = new Buffer(HEIGHT, WIDTH);
 	public InputHandler input = new InputHandler(this);
 	public Player player = new Player(new Point(50, 50), 0x00ff00);//0x00ff00);
 	public int frames = 0;
 	public Galaxy galaxy = LevelGen.generateGalaxy(new Random(10));
 	public spacegamecraft.data.System nearest_system;
 
 	
 	public Game() {
 		running = false;
 		
 	}
 
 	private static final String NAME = "Space Game";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		System.out.println(Integer.toString(Color.fromRGB(0xff, 0, 0xff), 16));
 		System.out.println(Integer.toString(Color.red(0xabcdef), 16));
 		System.out.println(Integer.toString(Color.green(0xabcdef), 16));
 		System.out.println(Integer.toString(Color.blue(0xabcdef), 16));
 
 		Game game = new Game();
 		game.setMinimumSize(new Dimension(HEIGHT*SCALE, WIDTH*SCALE));
 		game.setMaximumSize(new Dimension(HEIGHT*SCALE, WIDTH*SCALE));
 		game.setPreferredSize(new Dimension(HEIGHT*SCALE, WIDTH*SCALE));
 		
 		JFrame frame = new JFrame(Game.NAME);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLayout(new BorderLayout());
 		frame.add(game, BorderLayout.CENTER);
 		frame.pack();
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 
 		game.start();
 
 	}
 
 	private void start() {
 		running = true;
 		new Thread(this).start();
 	}
 
 	@Override
 	public void run() {
 		System.out.println(galaxy.toString());
 		long lastTime = System.nanoTime();
 		double unprocessed = 0;
 		double nsPerTick = 1000000000.0 / 60;
 		int frames = 0;
 		int ticks = 0;
 		long lastTimer1 = System.currentTimeMillis();
 
 		init();
 
 		while (running) {
 			long now = System.nanoTime();
 			unprocessed += (now - lastTime) / nsPerTick;
 			lastTime = now;
 			boolean shouldRender = true;
 			while (unprocessed >= 1) {
 				ticks++;
 				tick();
 				unprocessed -= 1;
 				shouldRender = true;
 			}
 
 			try {
 				Thread.sleep(2);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			if (shouldRender) {
 				frames++;
 				render();
 			}
 
 			if (System.currentTimeMillis() - lastTimer1 > 1000) {
 				lastTimer1 += 1000;
 				System.out.println(ticks + " ticks, " + frames + " fps");
 				this.frames = frames;
 				frames = 0;
 				ticks = 0;
 			}
 		}	}
 
 	private void init() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void tick() {
 		ticks++;
 		nearest_system = galaxy.nearestSystem(player.loc);
 		player.handleKey(input);
 	}
 	
 	private void render() {
 		BufferStrategy bs = getBufferStrategy();
 		if (bs == null) {
 			createBufferStrategy(3);
 			requestFocus();
 			return;
 		}
 		
 		
 		buf.clear();
 		/*buf.replaceBuffer(Background.generateBackground(HEIGHT, WIDTH));
 
 		
 		for(int x = 0; x < buf.pixels.length; x++) {
 			for(int y = 0; y < buf.pixels[x].length; y++) {
 				if(Math.sqrt(x*x+y*y) > 255) {
 					buf.pixels[x][y] = Color.fromRGB(0, (int) Math.sqrt(x*x+y*y), 0);
 				}
 			}
 		}
 		
 		for(int x = 0; x < buf.pixels.length; x++) {
 			buf.pixels[x][(int) (100+50*Math.abs(Math.sin(x/2)))] = Color.fromRGB(0xff, 0, 0xff);
 		}
 		
 		buf.pixels[50][100+(int) (100*Math.sin(ticks))] = 0x0000ff;
 		
 		buf = Font.drawMessage(Integer.toString(frames), new Point(0, 0), 0xff0000, buf);
 		buf = Font.drawMessage("aThis is a test tasdf", new Point(50, 50), 0xffffff, buf);
 		buf = Font.drawMessage("a b c d e f g h i j k l m n o p q r s t u v w x y  z 1 2 3 4 5 6 7 8 9 0 . , !", new Point(0, 100), 0xffffff, buf);
 		buf = Font.drawMessage("The font is done!", new Point(0, 106), 0xffffff, buf);
 		buf = Font.drawMessage("abcdefghijklmnopqrstuvwxyz1234567890.,!", new Point(0, 112), 0xffffff, buf);
 		buf = Font.drawMessage("The quick brown fox jumps over the lazy dog.", new Point(0, 118), 0xffffff, buf);
 		
 		buf = Font.drawMessage(Integer.toString(ticks), new Point(150, 20), 0xffffff, buf);
 		
 		buf = Font.drawMessage("This is a test\nThis is awesome\nI wonder how this works\n" +
 									"The quick brown fox jumps over the lazy dog.\nThis is pretty cool."+
 				"I wonder how many have died in defence of the ancients.\n"+
 									"How many have died when they might have lived.",
 									new Point(0, 200), 0xffffff, buf);*/
 
 		buf = galaxy.draw(new Point(0, 0), buf);
 		
 		
 		buf.pixels[1][1] = Color.fromRGB(0, 0, 0xff);
 		
 		Buffer grey_buf = new Buffer(HEIGHT, WIDTH);
 		grey_buf.clear(Color.INVISIBLE);
 		grey_buf = player.draw(grey_buf);
 		grey_buf.pixels[100][100] = Color.fromRGB(100, 200, 50);
 		buf.replaceBuffer(grey_buf);
 		buf = Font.drawMessage(Integer.toString(frames), new Point(0, 0), 0xff0000, buf);
 
 		buf.pixels[nearest_system.loc.x][nearest_system.loc.y] = Color.fromRGB(0xff, 0, 0);
 		buf = Font.drawMessage("Nearest System "+ nearest_system.loc.toString() + "\nName " + nearest_system.name, new Point(320, 10), 0xffffff, buf);
 		if(nearest_system.owned()) {
 			buf = Font.drawMessage("[" + nearest_system.owner.name + "]\n" + nearest_system.owner.size, new Point(320, 22), nearest_system.owner.color, buf);
 		}
 		StringBuilder objects_string = new StringBuilder("Planets in nearest system: \n");
 		for(int i=0; i<nearest_system.objects.size(); i++) {
 			objects_string.append(nearest_system.objects.get(i).name + "\n");
 		}
 		
 		buf = Font.drawMessage(objects_string.toString(), new Point(320, 34), 0xffffff, buf);
 		
 		
 		buf = Font.drawMessage("Arrow keys to move the green square. The nearest\nsystem is highlighted in\nred. FPS is displayed in\nred at the top left.",
 				new Point(320, 34+66), 0xffffff, buf);
 		
 		// Flicker:
 		//buf = Blur.multiblur(buf, ticks%3);
 		
 		for(int x = 0; x < buf.pixels.length; x++) {
 			for(int y = 0; y < buf.pixels[x].length; y++) {
 				// We go through this rigamarole to double pixel and therefore image size.
 				/*buffer.setRGB(x*2, y*2, buf.pixels[x][y]);
 				buffer.setRGB(x*2+1, y*2, buf.pixels[x][y]);
 				buffer.setRGB(x*2, y*2+1, buf.pixels[x][y]);
 				buffer.setRGB(x*2+1, y*2+1, buf.pixels[x][y]);*/
 				buffer.setRGB(x, y, buf.pixels[x][y]);
 			}
 		}
 
 		Graphics g = bs.getDrawGraphics();
 		g.drawImage(buffer.getScaledInstance(HEIGHT*SCALE, WIDTH*SCALE, Image.SCALE_AREA_AVERAGING), 0, 0, null);
 		g.dispose();
 		bs.show();
 	}
 
 }
