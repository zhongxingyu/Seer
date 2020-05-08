 package org.eclipse.iee.sample.formula.pad.hover;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 public class HoverShell {
 	public Shell fShell;
 	public Label fLabel;
 
 	public HoverShell(Composite parent, Image image) {
 		
		fShell = new Shell(SWT.TOOL | SWT.NO_FOCUS);
 		fShell.setVisible(false);
 		Point pt = parent.toDisplay(10, 30);
 		fShell.setLocation(pt.x, pt.y);
 		
 		fShell.setSize(0, 0);
 		fShell.setLayout(new FillLayout());
 		fLabel = new Label(fShell, SWT.NONE);
 		setImage(image);
 		pack();
 		
 	}
 
 	public void dispose() {
 		fShell.dispose();
 		fLabel.dispose();
 	}
 
 	public void setImage(Image image) {
 		fLabel.setImage(image);
 	}
 
 	public void pack() {
 		fShell.pack();
 		fShell.setVisible(true);
 	}
 	
 	public void setVisible(boolean visibility) {
 		fShell.setVisible(visibility);
 	}
 
 }
