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
		frame = new JFrame();
		Person p = new Person();
		frame.add(p);
 		p.validate();
 		frame.validate();
 		frame.pack();
 		frame.setVisible(true);
 	}
 }
