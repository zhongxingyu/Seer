 package week3.stem;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JTextArea;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import java.awt.*;
 import java.awt.event.*;
 
 public class UitslagJFrame extends JFrame implements Observer {
 
 	JTextArea u; // ugh
 	
 	public UitslagJFrame(Uitslag uitslag) {
 		super("Uitslag");		
 
 		uitslag.addObserver(this);
 
 		u = new JTextArea();		
 		this.add(u);
 		
 		this.setSize(200, 150);
 		this.setVisible(true);		
 	}
 	
 	public void update(Observable obj, Object arg) {
 	  // Cast als Uitslag-object
 	  Uitslag uitslag = (Uitslag)obj;

 		// TextArea leegmaken
 		u.setText("");
 	
 		// Doorloop partijen
 		for (String partij : uitslag.getPartijen()) {
 		  Integer stemmen = uitslag.getStemmenOp(partij);
 		  u.append(partij + ": " + stemmen + " stem(men) \n");
 		}
 	}
 	
 }
