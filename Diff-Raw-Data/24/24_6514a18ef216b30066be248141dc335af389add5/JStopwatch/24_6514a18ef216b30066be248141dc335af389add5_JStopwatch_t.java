 package jku.se.tetris.ui.swing;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JComponent;
 
 import jku.se.tetris.model.GameDataChangedListener;
 
 public class JStopwatch extends JComponent implements GameDataChangedListener {
 	private static final long serialVersionUID = -5112749912250732996L;
 
 	// ---------------------------------------------------------------------
 
 	private int width;
 	private int height;
 	private int borderInset;
 
 	// ---------------------------------------------------------------------
 
 	private Color colorBackground;
 	private Color colorForeground;
 	private Color colorText;
 
 	// ---------------------------------------------------------------------
 
 	private long gameStarted = 0;
 	private long gameOver = -1;
 
 	// ---------------------------------------------------------------------
 
 	public JStopwatch(int width, int height, int borderInset, Color bg, Color fg, Color txt) {
 		this.width = width;
 		this.height = height;
 		this.borderInset = borderInset;
 		// --
 		colorBackground = bg;
 		colorForeground = fg;
 		colorText = txt;
 		// --
 		setPreferredSize(new Dimension(width, height));
 		// --
 		Timer t = new Timer("Stopwatch");
 		t.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				repaint();
 			}
 		}, 0, 50);
 	}
 
 	// ---------------------------------------------------------------------
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		SwingGraphicsAdaptor.configureGraphicsContext(g);
 		// --
 		g.setColor(colorBackground);
 		g.fillRect(0, 0, width, height);
 		g.setColor(colorForeground);
 		g.drawRect(borderInset, borderInset, width - borderInset * 2, height - borderInset * 2);
 		// --
 		g.setColor(colorText);
 		// --
 		setFontStyle(g, Font.BOLD, 18);
 		// --
 		SwingGraphicsAdaptor.drawStringCenter(g, formatTime(), -5);
 	}
 
 	// ---------------------------------------------------------------------
 
 	private String formatTime() {
 		long duration = 0;
 		if (gameStarted > 0) {
 			if (gameOver == -1) {
 				duration = System.currentTimeMillis() - gameStarted;
 			} else {
 				duration = gameOver - gameStarted;
 			}
 		}
 		// --
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(duration);
 		// --
 		int h = c.get(Calendar.HOUR_OF_DAY) - 1;
 		int m = c.get(Calendar.MINUTE);
 		int s = c.get(Calendar.SECOND);
 		// --
 		return String.format("%02d:%02d:%02d", h, m, s);
 	}
 
 	// ---------------------------------------------------------------------
 
 	private void setFontStyle(Graphics g, int style, int size) {
 		Font f = g.getFont();
 		g.setFont(f.deriveFont(style, size));
 	}
 
 	// ---------------------------------------------------------------------
 
 	@Override
 	public void scoreChanged(long newScore) {
 	}
 
 	@Override
 	public void levelChanged(int newLevel) {
 	}
 
 	// ---------------------------------------------------------------------
 
 	@Override
 	public void gameStarted() {
 		this.gameStarted = System.currentTimeMillis();
		this.gameOver = -1;
 		repaint();
 	}
 
 	@Override
 	public void gameOver(long score, int level, long duration) {
 		gameOver = System.currentTimeMillis();
 		repaint();
 	}
 }
