 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 public class gui extends JPanel implements ActionListener {
 /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 static Timer time;
 
 //Geschwindigkeit des scrollens des Bildes
 static int speed; 
 
 //Geschwindigkeit von KeySchleife
 long KeyfirstFrame;
 long KeycurrentFrame;
 
 //fps
 static long firstFrame;
 int frames;
 static long currentFrame;
 int fps;
 static String fpsvisible = "false";
 
 //Seconds
 int Seconds;
 
 static Rectangle rect2 = new Rectangle();
 
 //Hhe fr offscreenimage
 int Bildhhe = frame.fenster.getHeight();
 
 //Geschwindigkeit der Perfomance
 int Geschwindigkeit = 1;
 
 //Auf diesen Bild wird dann gezeichnet
 Image offscreen;
 
 //Offscreen Image
 static Graphics bg; 
 
 static int schleife = 1;  
 
 static int KeySchleifeAnAus = 3;
 
 static int ySpace = 0;
 
 	public gui() throws Exception { 								
 		
 		setFocusable(true);					        		       							
 		
 		LoadTutorial.Tutorial();
 		
 		//Fgt einen Keylistener hinzu
 		addKeyListener(new AL());	
 		
 		//Ldt Character		
 		SchlumpfSpriteLaufen.main();
 		
 		//Berechnet Bilder
 		KeySchleife.main();
 		
 		time = new Timer(Geschwindigkeit, this); 
 		time.start();
 	}
 
  
 	public void actionPerformed(ActionEvent e){
 		repaint();
 	}		
 
 	public void paint(Graphics g){																
 		offscreen = createImage(LoadTutorial.BildbreiteMal1,Bildhhe);
 		bg = offscreen.getGraphics();
 
 		//bg.clearRect(0,0,LoadTutorial.BildbreiteMal1,Bildhhe);									
 
 		super.paint(g);
 		Graphics2D g2d = (Graphics2D) g;	
 		
 		//Zeichnen des Tutorials
 		Tutorial_draw.Tutorial_draw();											
 		
 		//Ende vom Zeichnen des Characters	
 		//Kollisionsabfrage
 		Tutorial_draw.BlockUpdater();
 		
 		//Anzeige fr Climp Power
 		bg.setFont(new Font("Sans", Font.PLAIN, 30)); 			
 		bg.setColor(Color.WHITE); 
 		if(Strings.ClimpPower <= 0) {	
 			bg.drawString("Climb Power: 0",50, 90);
 		}else{
 			bg.drawString("Climb Power: " + Strings.ClimpPower,50, 90);
 		}
 		
 		//Anezige fr Sprint Power		
 		if(Strings.SprintPower <= 0) {	
 			bg.drawString("Sprint Power: 0",50, 130);
 		}else{
 			bg.drawString("Sprint Power: " + Strings.SprintPower,50, 130);
 		}
 		
 		//Aufladen der Sprint Power
 		if(KeySchleife.KeyPressedShift == "false") {
 			if(Strings.SprintPower >= 200) {
 				Strings.SprintPower = 200;
 			}else{
 				if(Strings.realPressedShift == false) {
 					Strings.SprintPower += 0.5;
 				}
 			}							
 		}
 		
 		//Anzeige fr Fps
 		if(fpsvisible.equalsIgnoreCase("true")){						
 			frames++;
 			currentFrame = System.currentTimeMillis();
 			if(currentFrame > firstFrame + 1000){				
 				firstFrame = currentFrame;
 				fps = frames;
 				frames = 0;
 			}			
 			//int FpsGlobal = fps + Strings.Keyfps + Strings.Chafps;
 			//int FpsDurchschnitt2 = FpsGlobal / 3;
 			//int FpsDurchschnitt = FpsDurchschnitt2 - 17;
 			String Fps = String.valueOf(fps);			
 			if(fps == 1) {
 				bg.drawString("Berechne fps..." ,50, 50);
 			}else{
 				bg.drawString("fps: " + Fps, 50, 50);
 			}			
 		}
         
 		//Doublebuffer
 		g2d.drawImage(offscreen,0,0,null);									
 	}
 
 	public void update(Graphics g)
 	{ 
 		//paint(g); 
 	}
 	
 	//Keyevent
 	private class AL extends KeyAdapter{
 
 		 public AL() {
 			 
 		 }
 		 
 		 //Anfang vom berprfen ob eine Taste losgelassen wurde
 		 public void keyReleased(KeyEvent e){
 
 			 Strings.key = e.getKeyCode();
 			 
 			 if(Strings.key == KeyEvent.VK_LEFT){					 
 				 KeySchleife.ChaLeftUpdateSchleife = 1;
 				 KeySchleife.KeyPressed = "aus";	
 				 KeySchleife.KeyPressedLeft = "false";
 				 SchlumpfSpriteLaufen.LaufenLinks = "false";
 				 SchlumpfSpriteLaufen.Standbild = "aus";
 				 SchlumpfSpriteLaufen.aa = 1;				
 				 speed = 0;				 
 			 }
 
 			 if(Strings.key == KeyEvent.VK_RIGHT){					 
 				schleife = 1;
 				KeySchleifeAnAus = 1;
 				KeySchleife.KeyPressed = "aus";	
 				KeySchleife.KeyPressedRight = "aus";
 				SchlumpfSpriteLaufen.Standbild = "aus";
 				SchlumpfSpriteLaufen.aa = 1;				
 				speed = 0;						 
 			 }
 			 if(Strings.key == KeyEvent.VK_SHIFT) {
 				 Strings.realPressedShift = false;
 				 KeySchleife.run = 0;
 				 KeySchleife.KeyPressedShift = "false";
 			 }
 			 if(Strings.key == KeyEvent.VK_CONTROL) {
 				 KeySchleife.KeyPressedControl = false;
 			 }
 		 }
 		 //Ende vom berprfen ob eine Taste losgelassen wurde
 		 		 
 		
 		//Anfang vom berprfen ob eine Taste gedrckt wurde
 		 public void keyPressed(KeyEvent e){
 			 Strings.key = e.getKeyCode();  
 			 if(Strings.key == KeyEvent.VK_F3) {
 					if(fpsvisible.equalsIgnoreCase("true")) {
 						fpsvisible = "false";
 						fps = 1;
 						frames = 0;
 					}else{
 						fpsvisible = "true";
 					}
 				}
 						
 				if(Strings.key == KeyEvent.VK_ESCAPE) {
 					Strings.ChaJump = "false";
 					Strings.MenMusik = "true";
 					frame.visible();
 				}
 				if(Strings.key == KeyEvent.VK_SHIFT) {
 					Strings.realPressedShift = true;
					if(Strings.SprintPower > 0) {						
 						KeySchleife.KeyPressedShift = "true";
					}else{
						KeySchleife.KeyPressedShift = "false";
						KeySchleife.run = 0;
					}
					
 				}
 				if(Strings.key == KeyEvent.VK_SPACE) {						
 					if(!(Strings.AnimationY == true)) {							
 						if(Strings.Climb == false) {								
 							if(KeySchleife.KeyPressedControl == false) {								
 								KeySchleife.KeyPressedSpace = "true";
 							}
 						}
 					}								
 				}
 	
 				if (Strings.key == KeyEvent.VK_LEFT ){
 					if(KeySchleife.ChaLeftUpdateSchleife == 1) {
 						KeySchleife.ChaLeftUpdate = "true"; 
 						KeySchleife.ChaLeftUpdateSchleife = 2;
 						KeySchleife.KeyPressed = "an";	
 					}
 					KeySchleife.KeyPressedLeft = "true";
 					SchlumpfSpriteLaufen.RightLeft = "left";
 					
 				} 
 				
 				//Event fr die Taste Pfeil Rechts
 				if(Strings.key == KeyEvent.VK_RIGHT) {	
 					KeySchleife.KeyPressedRight = "an";
 					KeySchleife.KeyPressed = "an";	
 					SchlumpfSpriteLaufen.RightLeft = "right";									
 					
 					if(KeySchleifeAnAus == 3) {					
 						KeySchleifeAnAus = 1;										
 					}
 					
 				}
 				
 				//Event fr die Taste STRG
 				if(Strings.key == KeyEvent.VK_CONTROL) {					
 					if(Strings.ClimpPower <= 0) {
 						KeySchleife.KeyPressedControl = false;							
 					}else{
 						KeySchleife.KeyPressedControl = true;
					}	
				}	
		 }
 	}
 }
