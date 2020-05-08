 package chalmers.dax021308.ecosystem.view.populationsettings;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JTabbedPane;
 
 import chalmers.dax021308.ecosystem.view.IView;
 
 /**
  * 
  * Dialog for changing population settings.
  * 
  * @author Erik Ramqvist
  *
  */
 public class PopulationSettingsDialog extends JDialog implements IView {
 	private static final long serialVersionUID = -4258143526404863551L;
 	private JTabbedPane tabPane = new JTabbedPane();
 	
 	public PopulationSettingsDialog(Frame superFrame) {
 		super(superFrame);
 		init();
 	}
 	
 	@Override
 	public void propertyChange(PropertyChangeEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void init() {
 		tabPane.addTab("Deer agent", new PopulationPanel());
 		tabPane.addTab("Grass agent", new PopulationPanel());
 		tabPane.addTab("Wolf agent", new PopulationPanel());
 		add(tabPane);
		setVisible(true);
 		pack();
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		setTitle("Population settings");
 		setIconImage(new ImageIcon("res/Simulated ecosystem icon.png").getImage());
 		centerOnScreen(this, true);
 	}
 
 	@Override
 	public void addController(ActionListener controller) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTick() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 		
 	}
 	public void centerOnScreen(final Component c, final boolean absolute) {
 	    final int width = c.getWidth();
 	    final int height = c.getHeight();
 	    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	    int x = (screenSize.width / 2) - (width / 2);
 	    int y = (screenSize.height / 2) - (height / 2);
 	    if (!absolute) {
 	        x /= 2;
 	        y /= 2;
 	    }
 	    c.setLocation(x, y);
 	}
 }
