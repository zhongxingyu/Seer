 package ex02_03;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JLabel;
 
import javax.swing.JWindow;

 class PropertyPopupMenu extends JPopupMenu implements ActionListener {
   private static final long serialVersionUID = -5886033453588110137L;
   private static final int DISPLAY_FONT_NUM = 50;
 
   private DigitalClock parent;
   private JMenu fontMenu;
   private JMenu fontSizeMenu;
   private JMenu fontColorMenu;
   private JMenu backColorMenu;
   private JMenuItem quitMenu;
 
   private JMenuItem[] fontSubMenus;
   private JMenuItem[] fontSizeSubMenus;
   private JMenuItem[] fontColorSubMenus;
   private JMenuItem[] backColorSubMenus;
 
   private StopWatchDialog stopWatch;
   private JMenuItem stopWatchMenu;
 
   PropertyPopupMenu(DigitalClock parent) {
     this.parent = parent;
     fontMenu = new JMenu("Font");
     fontSizeMenu = new JMenu("Font Size");
     fontColorMenu = new JMenu("Font Color");
     backColorMenu = new JMenu("Background Color");
     stopWatchMenu = new JMenuItem("Stop watch");
     quitMenu = new JMenuItem("Quit");
 
     add(fontMenu);
     add(fontSizeMenu);
     add(fontColorMenu);
     add(backColorMenu);
     add(stopWatchMenu);
     add(quitMenu);
 
     fontSubMenus = new JMenuItem[ClockProperty.fontFamily.length];
     /* すべてのフォントを表示できないので間引く間隔を決める */
     int fontNumInterval = (ClockProperty.fontFamily.length / DISPLAY_FONT_NUM) + 1;
     for (int i = 0; i < fontSubMenus.length; ++i) {
       fontSubMenus[i] = new JMenuItem(ClockProperty.fontFamily[i]);
       if (i % fontNumInterval == 0) {  // 間引く
         fontMenu.add(fontSubMenus[i]);
         fontSubMenus[i].addActionListener(this);
       }
     }
 
     fontSizeSubMenus = new JMenuItem[ClockProperty.fontSizes.length];
     for (int i = 0; i < fontSizeSubMenus.length; ++i) {
       fontSizeSubMenus[i] = new JMenuItem(ClockProperty.fontSizes[i]);
       fontSizeMenu.add(fontSizeSubMenus[i]);
       fontSizeSubMenus[i].addActionListener(this);
     }
 
     fontColorSubMenus = new JMenuItem[ClockProperty.colorFamily.length];
     for (int i = 0; i < fontColorSubMenus.length; ++i) {
       fontColorSubMenus[i] = new JMenuItem(ClockProperty.colorFamily[i]);
       ImageIcon icon = new ImageIcon("./image/" + ClockProperty.colorFamily[i] + ".png");
       fontColorSubMenus[i].setIcon(icon);
       fontColorMenu.add(fontColorSubMenus[i]);
       fontColorSubMenus[i].addActionListener(this);
     }
 
     backColorSubMenus = new JMenuItem[ClockProperty.colorFamily.length];
     for (int i = 0; i < backColorSubMenus.length; ++i) {
       backColorSubMenus[i] = new JMenuItem(ClockProperty.colorFamily[i]);
       ImageIcon icon = new ImageIcon("./image/" + ClockProperty.colorFamily[i] + ".png");
       backColorSubMenus[i].setIcon(icon);
       backColorMenu.add(backColorSubMenus[i]);
       backColorSubMenus[i].addActionListener(this);
     }
 
     fontSizeMenu.addActionListener(this);
     quitMenu.addActionListener(this);
 
     stopWatch = new StopWatchDialog();
     stopWatchMenu.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         stopWatch.setVisible(true);
       }
     });
   }
 
   @Override
   public void actionPerformed(ActionEvent e) {
     Object source = e.getSource();
     if (source == quitMenu)
       System.exit(0);
 
     /* Change font */
     for (JMenuItem fontItem : fontSubMenus) {
       if (source == fontItem) {
         parent.changeFont(fontItem.getText());
       }
     }
     /* Change font size */
     for (JMenuItem fontSizeItem : fontSizeSubMenus) {
       if (source == fontSizeItem) {
         int fontSize = Integer.valueOf(fontSizeItem.getText());
         parent.changeFontSize(fontSize);
       }
     }
     /* Change font color */
     for (JMenuItem fontColorItem : fontColorSubMenus) {
       if (source == fontColorItem) {
         parent.changeFontColor(fontColorItem.getText());
       }
     }
     /* Change background color */
     for (JMenuItem backColorItem : backColorSubMenus) {
       if (source == backColorItem) {
         parent.changeBackColor(backColorItem.getText());
       }
     }
   }
 
   private class StopWatchDialog extends JDialog {
     private static final long serialVersionUID = -4747443104088237407L;
     private final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss SS");
 
     private long startTime;
     private long totalTime;
     private boolean isRunning;
     private Timer stopWatchTimer;
     private JLabel timeLabel;
 
     StopWatchDialog() {
       setModal(true);
       setSize(230, 100);
       setResizable(false);
       setLocationRelativeTo(null);
       setForeground(Color.BLACK);
       setBackground(Color.WHITE);
 
       startTime = 0;
       totalTime = 0;
       isRunning = false;
 
       timeLabel = new JLabel(getTimeStr());
       timeLabel.setFont(new Font("Consolas", Font.BOLD, 50));
       getContentPane().add(timeLabel);
 
       addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
           if (e.getButton() == MouseEvent.BUTTON1) {  // 左クリック
             if (e.getClickCount() == 1) {
               System.out.println("1");
               if (isRunning) { // 既に計測中
                 isRunning = false;
                 stopWatchTimer.cancel();
                 totalTime += System.currentTimeMillis() - startTime;
               } else {
                 isRunning = true;
                 stopWatchTimer = new Timer(true);
                 /* 20 ミリ秒間隔で再描画 */
                 stopWatchTimer.schedule(new StopWatchTimer(), 0, 20);
                 startTime = System.currentTimeMillis();
               }
             }
           } else if (e.getButton() == MouseEvent.BUTTON3) { // 右クリック
             if (!isRunning && stopWatchTimer != null) {
               totalTime = 0;
               startTime = System.currentTimeMillis();
               paint();
             }
           }
         }
       });
     }
 
     @Override
     public void setVisible(boolean b) {
       if (b) {
         totalTime = 0;
         startTime = System.currentTimeMillis();
         paint();
       } else {  //終了時
         isRunning = false;
         if (stopWatchTimer != null)
           stopWatchTimer.cancel();
       }
       super.setVisible(b);
     }
 
     private void paint() {
       String time_str = getTimeStr();
       timeLabel.setText(time_str);
     }
 
     private String getTimeStr() {
       long diffTime = System.currentTimeMillis() - startTime - 9 * 3600 * 1000;
       String time = sdf.format(new Date(totalTime + diffTime));
       if (time.length() > 8)
         time = time.substring(0, 8);
       return time;
     }
 
     private class StopWatchTimer extends TimerTask {
       @Override
       public void run() {
         paint();
       }
     }
   }
 }
