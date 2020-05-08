 package bombi;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class Tutorials extends JFrame {
 	public static void main(String[] args){
 	new Tutorials();	
 	}
 	
 	FlowLayout layout = new FlowLayout();
 	
 	public Tutorials(){
 		super("Bomberman");
 		setLayout(layout);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		setContentPane(new BackGroundPane("img/Bomberman.png"));
 		
		setLocation
		(250,500);
 		setSize(900,700);
 		setVisible(true);
 		setLayout(null);
 		
 		JButton button1 = new JButton("Tutorial1");
 	    button1.addActionListener(new ActionListener() {
 	        @Override
 	        public void actionPerformed(ActionEvent e) {
 	        	JFrame frame = new JFrame();
                 
                 frame.setTitle("Bombi");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 BombiGui bGui = new BombiGui(false,1);
                 
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
 	    
 		JButton button2 = new JButton("Tutorial2");
 	    button2.addActionListener(new ActionListener() {
 	        @Override
 	        public void actionPerformed(ActionEvent e) {
 	        	JFrame frame = new JFrame();
                 
                 frame.setTitle("Bombi");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 BombiGui bGui = new BombiGui(false,2);
                 
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
 	    
 		JButton button3 = new JButton("Tutorial3");
 	    button3.addActionListener(new ActionListener() {
 	        @Override
 	        public void actionPerformed(ActionEvent e) {
 	        	JFrame frame = new JFrame();
                 
                 frame.setTitle("Bombi");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 BombiGui bGui = new BombiGui(false,3);
                 
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
 	    
 		JButton button4 = new JButton("Tutorial4");
 	    button4.addActionListener(new ActionListener() {
 	        @Override
 	        public void actionPerformed(ActionEvent e) {
 	        	JFrame frame = new JFrame();
                 
                 frame.setTitle("Bombi");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 BombiGui bGui = new BombiGui(false,4);
                 
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
 	    
 		JButton button5 = new JButton("Tutorial5");
 	    button5.addActionListener(new ActionListener() {
 	        @Override
 	        public void actionPerformed(ActionEvent e) {
 	        	JFrame frame = new JFrame();
                 
                 frame.setTitle("Bombi");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 BombiGui bGui = new BombiGui(false,5);
                 
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
 	    
 		JButton button6 = new JButton("Tutorial6");
 	    button6.addActionListener(new ActionListener() {
 	        @Override
 	        public void actionPerformed(ActionEvent e) {
 	        	JFrame frame = new JFrame();
                 
                 frame.setTitle("Bombi");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 BombiGui bGui = new BombiGui(false,6);
                 
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
 	    
 		JButton button7 = new JButton("Zurück");
 	    button7.addActionListener(new ActionListener() {
 	        @Override
 	        public void actionPerformed(ActionEvent e) {
             	JFrame menu= new JFrame();
             	
 
             	menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
             	
             	new Menu();
                 dispose();
 	                
 	            }
 	        });
 	    
 	    add(button1);
 	    add(button2);
 	    add(button3);
 	    add(button4);
 	    add(button5);
 	    add(button6);
 	    add(button7);
 	    setVisible(true);
 	    
         Insets insets = getInsets();
         Dimension size = button1.getPreferredSize();
         button1.setBounds(380+ insets.left, 280 + insets.top,
         		size.width+30,size.height+5);
         button2.setBounds(380+ insets.left, 315 + insets.top,
         		size.width+30,size.height+5);
         button3.setBounds(380+ insets.left, 350 + insets.top,
         		size.width+30,size.height+5);
         button4.setBounds(380+ insets.left, 385 + insets.top,
         		size.width+30,size.height+5);
         button5.setBounds(380+ insets.left, 420 + insets.top,
         		size.width+30,size.height+5);
         button6.setBounds(380+ insets.left, 455 + insets.top,
         		size.width+30,size.height+5);
         button7.setBounds(380+ insets.left, 490 + insets.top,
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
 
