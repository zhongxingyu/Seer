 package sec02.ex03;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Calendar;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JPopupMenu;
 import javax.swing.JWindow;
 
 public class ClockMainWindow extends JWindow implements Runnable, ItemListener, MouseListener, MouseMotionListener, Observer {
 	private static final long serialVersionUID = 1L;
 	private static int h;           //時を入れる変数を宣言
 	private static int m;           //分を入れる変数を宣言
 	private static int s;           //秒を入れる変数を宣言
 	private static int height = 200;
 	private static int width = 400;
     
 	private static final int BLANK_SPACE_SIZE = 50;
 
 	private Graphics buffer;        //オフスクリーンバッファのグラフィックコンテキスト
 	private Image back = null;
 	private Thread timerThread;
 
 	PropertyData data = new PropertyData(); //プロパティデータ
 	JPopupMenu pop = new ClockMenuPopup(data); //ポップアップメニュー
 	
     /*
      * コンストラクタ
      */
     public ClockMainWindow() {
     	super(new Frame());
     	timerThread = new Thread(this);
     	timerThread.start();
     	
         setLayout(new FlowLayout());
         setSize(width, height);             
 
         add(pop);
         addMouseListener(this);
         addMouseMotionListener(this);
         
         this.show();
         this.toFront();
     }
     
 	public void run() {
 	    while(true) {
 	          h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //時を代入
 	          m = Calendar.getInstance().get(Calendar.MINUTE);      //分を代入
 	          s = Calendar.getInstance().get(Calendar.SECOND);      //秒を代入
 	          repaint();
 	          pause(100);
 	    }
 	}
 
 	public void update(Graphics g) {
 		paint(g);
 	}
 
 	public void paint(Graphics g) {    		
 		back = this.createImage(width, height);  
 		buffer = back.getGraphics();
 		buffer.setFont(new Font(data.font, data.fontStyle, data.fontSize));
 		buffer.setColor(data.color);
 		setBackground(data.backgroundColor);
 
 		drawTimeCenter();
 		setSize(width, height);
 
 		g.drawImage(back, 0, 0, this);      //バッファを画面に描画
 	}
 
     private void drawTimeCenter() {
     	FontMetrics fo = buffer.getFontMetrics();
         int strWidth = fo.stringWidth(h+":"+m+":"+s);
         int strHeight = fo.getAscent() + fo.getDescent();
         
         width = strWidth + BLANK_SPACE_SIZE;
         height = strHeight + BLANK_SPACE_SIZE;
         
         buffer.drawString(h+":"+m+":"+s, (width - strWidth) / 2, (height - strHeight) / 2 + fo.getMaxAscent());
     }
 
 	private void pause(int time) {
         try {
         	Thread.sleep(time);
         } catch (InterruptedException e) {}
     }
 
     public void itemStateChanged(ItemEvent e) {
         CheckboxMenuItem menu = (CheckboxMenuItem)e.getSource();
         if (menu.getState()) {
             System.out.println(menu.getLabel() + " SELECTED");
         } else {
             System.out.println(menu.getLabel() + " DESELECTED");
         }
     }
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private MouseEvent start;
 	@Override
 	public void mousePressed(MouseEvent e) {
 		start = e;
 		//System.out.println("mousePressed");
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		int btn = e.getButton();
     	if (btn == MouseEvent.BUTTON3) {
     		//System.out.println("right click");
     		pop.show(this , e.getX() , e.getY());
     	}	
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		int btn = e.getButton();
    	if (btn == MouseEvent.BUTTON1) {
     		// System.out.println("drag");
     		Point eventLocationOnScreen = e.getLocationOnScreen();
     	    setLocation(eventLocationOnScreen.x - start.getX(),
                     eventLocationOnScreen.y - start.getY());
     	}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 		// TODO Auto-generated method stub	
 	}
 	
 	@Override
 	public void update(Observable event, Object obj) {
 		repaint();
 	}
 
 	/**
 	 * メイン関数
 	 */
 	public static void main(String args[]) {	
 		ClockMainWindow clock = new ClockMainWindow();
 	    clock.addWindowListener(new windowListener());
 	}
 }
 
 class windowListener extends WindowAdapter
 {
 	public void windowClosing(WindowEvent e) {   //×を押されたときの処理
 	   System.exit(0);
 	}
 }
