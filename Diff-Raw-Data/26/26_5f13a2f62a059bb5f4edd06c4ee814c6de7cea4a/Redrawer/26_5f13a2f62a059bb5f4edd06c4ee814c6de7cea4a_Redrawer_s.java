 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JPanel;
 
 
 /**
  * Listens to changes from the notebook and updates the panel.
  */
 public class Redrawer implements ActionListener {
 
 	private JPanel panel;
 
 	public Redrawer(JPanel panel) {
 		this.panel = panel;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		panel.repaint();
 	}
 
 }
