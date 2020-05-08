 package emcshop.gui;
 
 import java.awt.Component;
 
 import javax.swing.JScrollPane;
 
 /**
  * Adds some stuff onto a {@link JScrollPane}.
  * @author Michael Angstadt
  */
 @SuppressWarnings("serial")
 public class MyJScrollPane extends JScrollPane {
 	private final Component component;
 
 	public MyJScrollPane(Component component) {
 		super(component);
 		this.component = component;
 		getVerticalScrollBar().setUnitIncrement(100);
 	}
 
 	/**
 	 * Scrolls to the top.
 	 */
 	public void scrollToTop() {
 		getVerticalScrollBar().setValue(0); //scroll to top
 		component.repaint(); //without this, the panel's background will turn white
 	}
 }
