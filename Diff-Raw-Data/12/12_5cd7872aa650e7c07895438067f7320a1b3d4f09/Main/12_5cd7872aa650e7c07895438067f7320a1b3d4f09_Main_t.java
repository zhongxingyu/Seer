 package GUIProject;
 import javax.swing.JFrame;
 
 /**
  * 
  * @author MJ Chen, Alvin Janes, Stephen Wen
  *
  */
 public class Main {
 	public static JFrame frame;
 	public static Map baseMap;
 	/**
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args){
		frame = new JFrame();//this is the frame to hold everything
		Person p = new Person();//this is a test person to make sure people are working
		frame.getContentPane().add(p);//adding the panel to the frame
 		p.validate();
 		frame.validate();
 		frame.pack();
 		frame.setVisible(true);
 	}
 }
