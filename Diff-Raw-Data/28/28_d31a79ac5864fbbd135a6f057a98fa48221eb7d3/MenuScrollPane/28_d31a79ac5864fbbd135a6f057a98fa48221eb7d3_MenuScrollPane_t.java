 package menu.components;
 
 import javax.swing.JScrollPane;
 
 public class MenuScrollPane extends JScrollPane
 {
 	public MenuScrollPane()
 	{
 		this.viewport.setOpaque(false);
		setOpaque(false);
		setVerticalScrollBar(new CustomScrollBar());
		setBorder(null);
 	}
 }
