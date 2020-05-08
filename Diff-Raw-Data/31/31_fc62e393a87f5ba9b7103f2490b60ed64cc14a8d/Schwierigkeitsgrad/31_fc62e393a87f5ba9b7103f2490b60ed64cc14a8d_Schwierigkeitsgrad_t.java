 package bombi;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 public class Schwierigkeitsgrad extends JFrame {
 	
 	public static void main(String[] args){
 		new Schwierigkeitsgrad();
 	}
 	
 	JRadioButton b1 = new JRadioButton("Leicht");
 	JRadioButton b2 = new JRadioButton("Mittel");
 	JRadioButton b3 = new JRadioButton("Schwer");
 	ButtonGroup bg = new ButtonGroup();
 	
 	FlowLayout layout = new FlowLayout();
 	
 	public Schwierigkeitsgrad(){
 		super("Bomberman");
 		setLayout(layout);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		setContentPane(new BackGroundPane("img/Bomberman.png"));
 		
		setLocation(250,500);
 		setSize(900,700);
 		setVisible(true);
 		setLayout(null);
 		
         JButton button1 = new JButton("Zurück");
         button1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFrame einst = new JFrame();
                 
                 einst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 new Einstellungen();
                 dispose();
             	
             }
         });
 		
 		bg.add(b1);
 		bg.add(b2);
 		bg.add(b3);
 		b1.addItemListener(new itemHandler());
 		b2.addItemListener(new itemHandler());
 		b3.addItemListener(new itemHandler());
 		add(button1);
 		add(b1);
 		add(b2);
 		add(b3);
 		setVisible(true);
 		
         Insets insets = getInsets();
         Dimension size = b1.getPreferredSize();
         b1.setBounds(360+ insets.left, 260 + insets.top,
         		size.width+30,size.height+5);
         b2.setBounds(360+ insets.left, 300 + insets.top,
         		size.width+30,size.height+5);
 		b3.setBounds(360+ insets.left, 340 + insets.top,
 				size.width+30,size.height+5);
 		button1.setBounds(360+ insets.left, 380 + insets.top,
 				size.width+30,size.height+5);
 		
 	}
 	private class itemHandler implements ItemListener{
 
 		public void itemStateChanged(ItemEvent e) {
 			//Hier kommen die Funktionen
 			
 		}
 		
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
