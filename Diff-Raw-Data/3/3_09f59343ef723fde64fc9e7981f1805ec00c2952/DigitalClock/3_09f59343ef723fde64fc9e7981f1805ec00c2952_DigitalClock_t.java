 package ex1_2;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Menu;
 import java.awt.MenuBar;
 import java.awt.MenuItem;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Calendar;
 
 public class DigitalClock extends Frame implements Runnable, ActionListener {
 	private static int hour, min, sec;
 	private Thread thread = new Thread(this);
 	private PropertyDialog dlg;
 	private boolean propFlag = false;
 	private Font oldfont = new Font("TimesRoman", Font.BOLD, 48);
 	private Color oldTextcolor;
 	private Color oldBackground;
 	private Dimension size;
 	private Image imgBuffer;
 	private Graphics buffer;
 
 
 	/**
 	 * デジタル時計に次の機能追加を行ってください。
 	 * ・メニューをつけてプロパティダイアログを開ける
 	 * ・プロパティダイアログでは、以下の項目を設定できる
 	 *    1. フォントの指定
 	 *    2. フォントサイズの指定
 	 *    3. 文字色の指定
 	 *    4. 時計の背景色の指定
 	 * ・描画に際して、ちらつきをなくすようにダブルバッファリングする
 	 * ・フォントとフォントサイズを変更すると、時計を表示すべきフレームの大きさを適切に自動変更して、正しく表示されるようにする
 	 */
 	public static void main(String[] args) {
 		DigitalClock clock = new DigitalClock("DigitalClock");
 		clock.setSize(600, 200);
 		clock.setVisible(true);
 	}
 
 	DigitalClock(String title) {
 		this.setTitle(title);
 		this.setLayout(new FlowLayout());
 		this.setResizable(false);
 		MenuBar menuBar = new MenuBar();
 		this.setMenuBar(menuBar);
 
 		// [File]
 		Menu menuFile = new Menu("File");
 		menuFile.addActionListener(this);
 		menuBar.add(menuFile);
 
 		// [File] - [プロパティ]
 		MenuItem menuProperty = new MenuItem("プロパティ");
 		menuFile.add(menuProperty);
 
 		// [File] - [終了]
 		MenuItem menuExit = new MenuItem("終了");
 		menuFile.add(menuExit);
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent close) {
 				System.exit(0);
 			}
 		});
 
 		thread.start();
 	}
 
 	public void run() {
 		while (true) {
 			hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 			min = Calendar.getInstance().get(Calendar.MINUTE);
 			sec = Calendar.getInstance().get(Calendar.SECOND);
 			repaint();
 
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				;
 			}
 		}
 	}
 
 	public void paint(Graphics g) {
 		Font font;
 		size = this.getSize();
 		imgBuffer = createImage(size.width, size.height);
 		buffer = imgBuffer.getGraphics();
 
 		if (propFlag) {
 			if (dlg.isFlag()) {
 				font = dlg.getFont();
 				buffer.setColor(dlg.getTextcolor());
 				this.setBackground(dlg.getBackground());
 
 				oldfont = dlg.getFont();
 				oldTextcolor = dlg.getTextcolor();
 				oldBackground = dlg.getBackground();
 			} else {
 				font = oldfont;
 				g.setColor(oldTextcolor);
 				this.setBackground(oldBackground);
 			}
 		} else {
 			font = oldfont;
 		}
 		this.setSize(this.getWindowWidth(font), this.getWindowHeight(font));
 		buffer.setFont(font);
 		buffer.drawString(hour + ":" + min + ":" + sec, (size.width/2) - font.getSize() -20, size.height/2 + (font.getSize()/2) );
 		g.drawImage(imgBuffer, 0, 0, this);
 	}
 
	@SuppressWarnings("deprecation")
 	public void actionPerformed(ActionEvent e) {
 		switch (e.getActionCommand()) {
 		case "プロパティ":
 			dlg = new PropertyDialog(this);
 			dlg.show();
 			propFlag = true;
 			break;
 		case "終了":
 			System.exit(0);
 			break;
 		default:
 			break;
 		}
 	}
 
 	public int getWindowWidth(Font font) {
 		int width = 0;
 		if (font.getSize() < 40) {
 			width = 300;
 		} else if (font.getSize() < 50) {
 			width = 350;
 		} else if (font.getSize() < 60) {
 			width = 400;
 		} else if (font.getSize() < 70) {
 			width = 450;
 		} else if (font.getSize() < 80) {
 			width = 500;
 		} else if (font.getSize() < 90) {
 			width = 550;
 		} else {
 			width = 600;
 		}
 		return width;
 	}
 
 	public int getWindowHeight(Font font) {
 		int height = 0;
 		if (font.getSize() < 40) {
 			height = 200;
 		} else if (font.getSize() < 50) {
 			height = 220;
 		} else if (font.getSize() < 60) {
 			height = 240;
 		} else if (font.getSize() < 70) {
 			height = 260;
 		} else if (font.getSize() < 80) {
 			height = 280;
 		} else if (font.getSize() < 90) {
 			height = 300;
 		} else {
 			height = 320;
 		}
 		return height;
 	}
 }
