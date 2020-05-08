 package de.dhbw.muehle.gui.menus;
 
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.HierarchyBoundsAdapter;
 import java.awt.event.HierarchyEvent;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import de.dhbw.muehle.gui.View;
 
 public abstract class Menu extends JPanel{
 	
 	public final Dimension Size = new Dimension(705, 605);
 	
 	private View view;
 	
 	double proportion = (double) (Size.height) / (double) (Size.width);
 	
 	
 	public Menu(View view) {
 		this.view = view;
 		
 		// Panelgröße festlegen
 		setPreferredSize(Size);
 		
 		
 		addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
 	         @Override
 	         public void ancestorResized(HierarchyEvent e) {
 	            JComponent parent = (JComponent) getParent();
 	            Insets insets = parent.getInsets();
 	            int width = parent.getWidth() - insets.left - insets.right;
 	            int height = parent.getHeight() - insets.top - insets.bottom;
 	            width = (int) Math.min(width, height / proportion);
 	            height = (int) Math.min(width * proportion, height);
 	            setPreferredSize(new Dimension(width, height));
 	            
 	            validate();
 	            repaint();
 	         }
 	    });
 	}
 	
 	
 	public Dimension getOriginalSize(){
 		return Size;
 	}
 }
