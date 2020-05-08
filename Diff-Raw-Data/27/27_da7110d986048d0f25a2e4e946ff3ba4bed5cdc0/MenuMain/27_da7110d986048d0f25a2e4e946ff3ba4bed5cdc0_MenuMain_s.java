 /*===> MainMenu mit 3 Buttons: Start, Options, Quit
  *Ausgangspunkt frs Men ist Board.ingame = false; Board.menu = true;
  *
  *"Start" 				Setzt Board.ingame = true und Board.menu = false
  *"Options" 		 	Sound Volume, Music Volume, Difficulty, Save, Load, Back
  *					 	Diese mssen erstmal nur als Regler/Buttons ohne Funktion 
  *						implementiert werden, die folgende static Varibles setzen:
  *						Sound/Music als Schieberegler mit Zuweisung von int Werten
  *						Board.musicVolume/Board.soundVolume zwischen 0 und 100.
  *  
  *"Quit"				System.exit(0);
  * 
  */
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 
 public class MenuMain implements Runnable{
 
 	Graphics g2d;
 	BufferedImage menuMain;
 	
 	
 	public MenuMain(){
 		System.err.println("->MainMenu");
 	}
 	
 	
 	public void paintComponents(Graphics g){
 		g2d = (Graphics2D) g;
 
 	}
 	
 	public void run(){
		if (Board.printMsg)
 			System.out.println("MainMenu.run");
 	}
 	
 	//Noch nicht eingebundener Code vom Main Menu
 	
 	public class frame extends JFrame implements ActionListener {
 		
 
 		private JButton start;
 		private JButton einstellung;
 		private JButton info;
 		private JButton ende;

			public static void main (String[]args) throws Exception{ 
 					 
 			
 				
 			frame frame = new frame("Programmierpraktikum");
 			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			frame.setSize(400,400);
 			frame.setLocationRelativeTo(null);
 			frame.setResizable(false);
 			
 			frame.setLayout(null);
 			frame.setVisible(true);
 			
 		}
			
 			public frame(String title) {
 				
 				super(title);
 				
 				start = new JButton("Spiel starten");
 				start.setBounds(120, 40, 160, 40);
 				start.addActionListener(this);
 				add(start);
 				
 				einstellung = new JButton("Einstellungen");
 				einstellung.setBounds(120, 120, 160, 40);
 				einstellung.addActionListener(this);
 				add(einstellung);
 			
 				
 				info = new JButton("Credits");
 				info.setBounds(120, 200, 160, 40);
 				info.addActionListener(this);
 				add(info);
 			
 				
 			    ende = new JButton("Beenden");
 			    ende.setBounds(120, 280, 160, 40);
 			    ende.addActionListener(this);
 			    add(ende);
 			   
 			    
 				
 				JLabel label = new JLabel("Gruppe 11");
 				label.setBounds(315,340,150,20);
 				add(label);
 				
 			}
 				
 		public void actionPerformed(ActionEvent e){
 				
 				if (e.getSource() == start){
 					fenster();	
 				}
 				
 				if (e.getSource() == info){
 					Object[] options = { "OK"};
 					JOptionPane.showOptionDialog(null, "Programmiert von\nQuang Thi, Meiqian, Markus, Amine", "Information",
 
 					        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
 
 					        null, options, options[0]);
 				}
 				
 				if (e.getSource() == einstellung){
 					Object[] options = { "OK"};
 					JOptionPane.showOptionDialog(null, "Hier kannst du den Schwierigkeitsgrad auswhlen", "Einstellung",
 
 					        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
 
 					        null, options, options[0]);
 				}
 				
 				
 				if (e.getSource() == ende){
 					System.exit(0);
 					
 					
 				}
 			}
 
 			
			public static void fenster() {
 				
 				JFrame fenster = new JFrame("Game");
 				fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				fenster.setSize(650,350);
 				fenster.setVisible(true);
 				fenster.setLocationRelativeTo(null);
 			
 			}
 
 		}
 	
 	//Makierung
 	
 	public BufferedImage getImage(){
 		return menuMain;
 	}
 }
