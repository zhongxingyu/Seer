 package bombi;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.MediaTracker;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Toolkit;
  
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class Menu extends JFrame{
     
     public static void main(String[] args){
         new Menu();
     }
  
     FlowLayout layout = new FlowLayout();  
 /*
  * Der Konstruktor.
  */
     public Menu(){
         super("Bomberman");
         setLayout(layout);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
 
         setContentPane(new BackGroundPane("img/Bomberman.png"));
         
        setLocation(250,500);
         setVisible(true);
         setSize(900,700);
         setLayout(null);
         
  /*
   * Ab hier fangen die Buttons an.
   * Die Buttons werden ausgeführt von der funktion ActionListener.
   */
         JButton start = new JButton("Einzelspieler");
         start.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                     JFrame frame = new JFrame();
                     
                     frame.setTitle("Bombi");
                     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                     
                     BombiGui bGui = new BombiGui();
                     
                     frame.add(bGui);
                     frame.pack();
                     // passt die Gr��e dem Inhalt an
 
                     // zentriert das Fenster
                     frame.setLocationRelativeTo(null);
 
                     frame.setVisible(true);
                     
                     new Thread(bGui).start();
                     
                     dispose();
             }
         });
         
         JButton button1 = new JButton("Mehrspieler");
         button1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFrame f = new JFrame();
                 
                 f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 new Multiauswahl();
                 dispose();
                 
             }
         });
         
         JButton button2 = new JButton("Tutorial");
         button2.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
             	JFrame tut= new JFrame();
             	
             	tut.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             	
             	new Tutorials();
                 dispose();
                 
             }
         });
         
         JButton button3 = new JButton("Einstellungen");
         button3.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFrame f = new JFrame();
                 
                 f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 new Einstellungen();
                 dispose();
             	
             }
         });
         
         JButton close = new JButton("Schließen");
         close.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
  /*
   * Hier werden die Buttons auf dem fenster eingefügt.
   */
         add(start);
         add(button1);
         add(button2);
         add(button3);
         add(close);
         setVisible(true);
         
         Insets insets = getInsets();
         Dimension size = start.getPreferredSize();
         start.setBounds(360+ insets.left, 280 + insets.top,
         		size.width+30,size.height+5);
         button1.setBounds(360+ insets.left, 315 + insets.top,
         		size.width+30,size.height+5);
         button2.setBounds(360+ insets.left, 350 + insets.top,
         		size.width+30,size.height+5);
         button3.setBounds(360+ insets.left, 385 + insets.top,
         		size.width+30,size.height+5);
         close.setBounds(360+ insets.left,420 + insets.top,
         		size.width+30,size.height+5);
         
     }
 
     class BackGroundPane extends JPanel{
     	Image img = null;
     	
 		
 		BackGroundPane(String imagefile) {
     		if (imagefile != null) {
     			MediaTracker mt = new MediaTracker(this);
     			img = Toolkit.getDefaultToolkit().getImage(imagefile);
     			mt.addImage(img, 0);
     			try{
     				mt.waitForAll();
     			} catch (InterruptedException e) {
     				e.printStackTrace();
     			}
     		}
     	}
     	protected void paintComponent(Graphics g){
     		super.paintComponent(g);
     		g.drawImage(img,0,0,this.getWidth(),this.getHeight(),this);
     	}
         
     }
 }
