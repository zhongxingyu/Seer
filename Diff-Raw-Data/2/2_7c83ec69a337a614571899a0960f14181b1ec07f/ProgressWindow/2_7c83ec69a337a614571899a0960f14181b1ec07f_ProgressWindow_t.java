 package edu.uah.its.tag;
 
 import java.awt.BorderLayout;
 import java.beans.PropertyChangeEvent;
 import java.util.logging.Logger;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingUtilities;
 
 public class ProgressWindow extends JFrame {
 	
	private final static Logger logger = Logger.getLogger(ProgressWindow.class .getName());
 		
 	private JProgressBar progressBar;
 	private int progress; private String description; private boolean visible;
 	
 	public ProgressWindow() {
 		
 		setLayout(new BorderLayout());
 		
 		setTitle("Simple example");
 		setSize(400, 100);
 		setLocationRelativeTo(null);
 		
 		progressBar = new JProgressBar(0, 100);
         progressBar.setValue(0);
         progressBar.setStringPainted(true);
         progressBar.setString("Loading");
         
         add(progressBar,BorderLayout.CENTER);
 		
 	}
 	
 	public void propertyChange(PropertyChangeEvent evt) {
         if ("progress" == evt.getPropertyName()) {
             int progress = (Integer) evt.getNewValue();
             this.setProgress(progress);
         } else if ( "description" == evt.getPropertyName() ) {
         	this.setDescription((String) evt.getNewValue());
         }
         
     	//now show us in case we are hidden
 		if ( !this.isVisible() ) this.setVisible(true);
 	
     }
 	
 	private void updateBar() {
 		SwingUtilities.invokeLater(new Runnable() {
             public void run() {
             	progressBar.setValue(progress);
 				progressBar.setString(description);
 				ProgressWindow.super.setVisible(visible);
             }
 		});
 	}
 	
 	public void setProgress(final int p) {
 		progress = p;
 		visible = true;
 		this.updateBar();
 	}
 	public void setDescription(final String d) {
 		description = d;
 		this.updateBar();
 	}
 	public void setProgress(int p, String d) {
 		this.setProgress(p); this.setDescription(d);
 	}
 	public void setVisible(final boolean b) {
 		logger.finer("setVisible " + b);
 		visible = b;
 		this.updateBar();
 	}
 //	public void setVisible(final boolean b) {
 //		logger.finer("setVisible " + b);
 //		SwingUtilities.invokeLater(new Runnable() {
 //            public void run() {
 //				if (b) {
 //					ProgressWindow.super.setVisible(b);
 //					ProgressWindow.this.setAlwaysOnTop(b);
 //				} else {
 //					ProgressWindow.this.setAlwaysOnTop(b);
 //					ProgressWindow.super.setVisible(b);
 //				}
 //            }
 //		});
 //	}
 
 }
