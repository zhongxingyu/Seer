 package cbvgl;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 
 import org.lwjgl.input.Cursor;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.AngelCodeFont;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Game;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 public class Core implements Game {
 	public static final String version = "0.1";
 	private GameContainer gc;
 	private String comic;
 	private ImageList il;
 	private boolean justChanged = true;
 	AngelCodeFont acf;
 
 	public static void main(String[] args) {
 		System.out.println("cbvgl version: " + version);
 		JFileChooser fileFinder = new JFileChooser();
 		File comicdir = new File(System.getProperty("user.home") + "/comic");
 		if (comicdir.exists()) {
 			fileFinder.setCurrentDirectory(comicdir);
 		} else {
 			fileFinder.setCurrentDirectory(new File(System.getProperty("user.home")));
 		}
 		fileFinder.setMultiSelectionEnabled(false);
 		fileFinder.setDialogTitle("cbvgl version " + version);
 		fileFinder.setFileFilter(new ComicArchiveFilter());
 		fileFinder.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		int result = fileFinder.showOpenDialog(null);
 		if (result == JFileChooser.APPROVE_OPTION) {
 			System.out.println(fileFinder.getSelectedFile().getPath());
 			System.out.println(fileFinder.getSelectedFile().getParent());
 			Core c = new Core(fileFinder.getSelectedFile().getAbsolutePath());
 			try {
 				AppGameContainer gc = new AppGameContainer(c, 1280, 800, false);
 				gc.setFullscreen(true);
 				gc.start();
 			} catch (SlickException e) {
 				e.printStackTrace();
 			}
 		} else {
 			System.exit(0);
 		}
 	}
 
 	public Core(String comic) {
 		this.comic = comic;
 	}
 
 	public boolean closeRequested() {
 		return true;
 	}
 
 	public String getTitle() {
 		return "cbvgl";
 	}
 
 	public void init(GameContainer arg0) throws SlickException {
 		acf = new AngelCodeFont("/jar/font/BitstreamVeraSansMono14B.fnt", "/jar/font/BitstreamVeraSansMono14B.png");
 		gc = arg0;
 		// create the image list
 		il = new ImageList(comic);
 	}
 
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		// render the current image from the image list
 		GL11.glRotatef(90f, 0.0f, 0.0f, 1.0f);
 		if (il.isCurrentSplit()) {
 			if (il.splitStatus == 0) {
 				il.getCurrent().getScaledCopy(0.5f).draw(0, -1280);
 			} else if (il.splitStatus == 1) {
 				il.getCurrent().draw(0, -1280);
 			} else if (il.splitStatus == 2) {
 				il.getCurrent().draw(-800, -1280);
 			}
 		} else {
 			il.getCurrent().draw(0, -1280);
 		}
 		String pageCount = il.getPageNumber()+1 + "/" + il.size();
 		int acfx = 800-acf.getWidth(pageCount);
 		//int acfy = -acf.getHeight(pageCount);
 		int acfy = -acf.getLineHeight();
 		g.setColor(Color.lightGray);
 		g.fill(new Rectangle(acfx-2, acfy, acf.getWidth(pageCount)+2, acf.getHeight(pageCount)));
 		g.setColor(new Color(128, 0, 0));
 		g.fill(new Rectangle(0, acfy, acfx-2, acf.getHeight(pageCount)));
 		g.setColor(new Color(255, 0, 0));
 		g.fill(new Rectangle(0, acfy, ((float)(il.getPageNumber()+1)/(float)il.size())*((float)acfx-2f), acf.getHeight(pageCount)));
 		acf.drawString(acfx-2, acfy, pageCount, Color.black);
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
		int hour = cal.get(Calendar.HOUR);
		String hourString = "";
		if (hour == 0) {
			hourString = "12";
		} else {
			hourString = hour + "";
		}
		int minute = cal.get(Calendar.MINUTE);
		String minuteString = "";
		if (minute < 10) {
			minuteString = "0" + minute;
		} else {
			minuteString = minute + "";
		}
		String time = hourString + ":" + minuteString;
 		acf.drawString(acfx-(acf.getWidth(time)+6), acfy, time, Color.black);
 		acf.drawString(2, acfy, il.getFileName(), Color.black);
 	}
 
 	public void update(GameContainer arg0, int arg1) throws SlickException {
 		try {
 			Thread.sleep(100);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if (justChanged) {
 			justChanged = false;
 		} else {
 			il.preload();
 		}
 	}
 
 	public boolean isAcceptingInput() {
 		return true;
 	}
 
 	public void keyPressed(int arg0, char arg1) {
 		if (arg0 == Keyboard.KEY_ESCAPE
 				|| arg0 == Keyboard.KEY_Q) {
 			gc.exit();
 		} else if (arg0 == Keyboard.KEY_RETURN
 				|| arg0 == Keyboard.KEY_F
 				|| arg0 == Keyboard.KEY_F1
 				|| arg0 == Keyboard.KEY_F11) {
 			if (gc instanceof AppGameContainer) {
 				try {
 					AppGameContainer agc = (AppGameContainer) gc;
 					agc.setFullscreen(!agc.isFullscreen());
 				} catch (SlickException e) {
 					e.printStackTrace();
 				}
 			}
 		} else if (arg0 == Keyboard.KEY_DOWN
 				|| arg0 == Keyboard.KEY_NEXT
 				|| arg0 == Keyboard.KEY_SPACE) {
 			il.next();
 			justChanged = true;
 		} else if (arg0 == Keyboard.KEY_UP
 				|| arg0 == Keyboard.KEY_PRIOR
 				|| arg0 == Keyboard.KEY_BACK) {
 			il.previous();
 		} else if (arg0 == Keyboard.KEY_N) {
 			changeComic(il.getFileName(), 1);
 		} else if (arg0 == Keyboard.KEY_P) {
 			changeComic(il.getFileName(), -1);
 		}
 	}
 	
 	private void changeComic(String curComic, int delta) {
 		File cur = new File(curComic);
 		File dir = cur.getParentFile();
 		boolean found = false;
 		int i = 0;
 		String[] opts = dir.list();
 		List<String> optl = new ArrayList<String>();
 		for (String s : opts) {
 			optl.add(s);
 		}
 		Collections.sort(optl);
 		System.out.println("Current comic book: " + cur.getName());
 		System.out.println("Loading next comic book in " + dir.getAbsolutePath());
 		while (!found) {
 			System.out.println("   " + optl.get(i));
 			if (cur.getName().equalsIgnoreCase(optl.get(i))) {
 				found = true;
 			}
 			i++;
 		}
 		int nextNumber = i+delta-1;
 		if (nextNumber < 0 || nextNumber >= optl.size()) {
 			System.out.println("Proposal: " + nextNumber + "/" + optl.size() + ", not changing");
 		} else {
 			System.out.println("   " + optl.get(nextNumber));
 			il = new ImageList(dir.getAbsolutePath() + "/" + optl.get(nextNumber));
 			justChanged = true;
 		}
 	}
 
 	public void mousePressed(int button, int x, int y) {
 		if (button == 0) {
 			il.next();
 			justChanged = true;
 		} else if (button == 1) {
 			il.previous();
 		} else if (button == 2) {
 			if (il.getPageNumber() == 0) {
 				changeComic(il.getFileName(), -1);
 			} else if (il.getPageNumber() == il.size()-1) {
 				changeComic(il.getFileName(), 1);
 			}
 		}
 	}
 
 	public void controllerButtonPressed(int arg0, int arg1) {}
 	public void controllerButtonReleased(int arg0, int arg1) {}
 	public void controllerDownPressed(int arg0) {}
 	public void controllerDownReleased(int arg0) {}
 	public void controllerLeftPressed(int arg0) {}
 	public void controllerLeftReleased(int arg0) {}
 	public void controllerRightPressed(int arg0) {}
 	public void controllerRightReleased(int arg0) {}
 	public void controllerUpPressed(int arg0) {}
 	public void controllerUpReleased(int arg0) {}
 	public void inputEnded() {}
 	public void keyReleased(int arg0, char arg1) {}
 	public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {}
 	public void mouseReleased(int arg0, int arg1, int arg2) {}
 	public void mouseWheelMoved(int arg0) {}
 	public void setInput(Input arg0) {}
 }
