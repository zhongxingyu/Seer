 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.newdawn.slick.*;
 import org.w3c.dom.DOMException;
 import org.xml.sax.SAXException;
 
 public class SumoBotSimulator2013 extends BasicGame{
 
 	Color Background = new Color(0xfff4f4f4);
 	static double PSVolt, PSAmp = 0;									// Variables for the power supply
 	static double MVolt, MAmp, MSpeed, MTorque, MDriveShaft = 0;		// Variables for the motor
 	static double WiLen, WiOhm, WiArea, WiRes = 0;						// Variables for the wire
 	static double WhDiam, WhMu = 0;										// Variables for the wheel
 	static double RWeight =  0;											// General variables for the robot
 	static String WiCircuit = " ";										// Type of circuit
 
 	
     public SumoBotSimulator2013(){
         super("Sumo Bot Simulator 2013");
     }
   
     @Override
     public void init(GameContainer gc) throws SlickException{	
     	gc.setShowFPS(false);
     	GUI.InitGUI(gc);
    	File robot = new File("robot.xml");
     	try {
 			XMLReadWrite.read(robot);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (DOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
     }
   
     @Override
     public void update(GameContainer gc, int delta) throws SlickException{
     	GUI.UpdateGUI(gc, delta);
     }
   
     public void render(GameContainer gc, Graphics g) throws SlickException{	
     	g.setColor(Background);
     	g.fillRect(0, 0, 1366, 768);
     	GUI.RenderGUI(gc, g, "MenuBar");
     }
   
     public static void main(String[] args)throws SlickException{
          AppGameContainer app = new AppGameContainer(new SumoBotSimulator2013());
          app.setDisplayMode(800, 600, false);
          app.start();
     }
 }
