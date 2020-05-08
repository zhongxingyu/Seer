 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.smartdashboard.gui.elements;
 
 import edu.wpi.first.smartdashboard.state.Record;
 import edu.wpi.first.smartdashboard.types.Types;
 import edu.wpi.first.smartdashboard.util.StatefulDisplayElement;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingConstants;
 
 /**
  *
  * @author Alex Henning
  */
 public class VerticalProgressBar extends StatefulDisplayElement {
 
     private JProgressBar progressBar;
     private final String foregroundProperty = "Foreground";
     private final String backgroundProperty = "Background";
     private final String maximumProperty = "Maximum";
     private final String minimumProperty = "Minimum";
     private final String widthProperty = "Width";
     private final String heightProperty = "Height";
 
     @Override
     public void init() {
 	progressBar = new JProgressBar();
 	progressBar.setOrientation( SwingConstants.VERTICAL );
 	progressBar.setMaximum(0 * 100);
 	progressBar.setMaximum(100 * 100);
 	progressBar.setBorderPainted(false);
 	progressBar.setBounds(progressBar.getX(), progressBar.getY(),
 			      progressBar.getX() + 200, progressBar.getY() + 40);
 
 	setLayout(new BorderLayout());
 	add(new JLabel(m_name), BorderLayout.PAGE_START);
 	add(progressBar, BorderLayout.CENTER);
 	revalidate();
 	repaint();
 	
 	setProperty(foregroundProperty, progressBar.getForeground());
 	setProperty(backgroundProperty, progressBar.getBackground());
 	setProperty(maximumProperty, progressBar.getMaximum());
 	setProperty(minimumProperty, progressBar.getMinimum());
     	setProperty(widthProperty, progressBar.getWidth());
 	setProperty(heightProperty, progressBar.getHeight());
     }
 
     @Override
     public boolean propertyChange(String key, Object value) {
 	if (key == foregroundProperty) {
 	    progressBar.setForeground((Color) value);
 	} if (key == backgroundProperty) {
 	    progressBar.setBackground((Color) value);
 	} else if (key == maximumProperty) {
 	    progressBar.setMaximum(Integer.parseInt((String) value) * 100);
 	} else if (key == minimumProperty) {
 	    progressBar.setMinimum(Integer.parseInt((String) value) * 100);
 	} else if (key == widthProperty) {
 	    setSize(new Dimension(Integer.parseInt((String) value),
					  progressBar.getHeight()));
	    // setSize(new Dimension(Integer.parseInt((String) value),
	    // 				  progressBar.getHeight()));
 	} else if (key == heightProperty) {
	    setSize(progressBar.getWidth(),
				Integer.parseInt((String) value));
	    // setSize(progressBar.getWidth(),
	    // 			Integer.parseInt((String) value));
 	}
 	return true;
     }
 
     public static Types.Type[] getSupportedTypes() {
 	return new Types.Type[]{
 	    Types.Type.DOUBLE,
 	    Types.Type.BYTE,
 	    Types.Type.INT,
 	    Types.Type.SHORT
 	};
     }
 
     @Override
     public Object getPropertyValue(String key) {
 	if (key == foregroundProperty) {
 	    return progressBar.getForeground();
 	}
 	if (key == backgroundProperty) {
 	    return progressBar.getBackground();
 	}
 	if (key == maximumProperty) {
 	    return progressBar.getMaximum() / 100;
 	}
 	if (key == minimumProperty) {
 	    return progressBar.getMinimum() / 100;
 	} else if (key == widthProperty) {
 	    return getWidth();
 	} else if (key == heightProperty) {
 	    return getHeight();
 	} else {
 	    return 0;
 	}
     }
 
     public void update(final Record r) {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
 		Number value = (Number) (((Number) r.getValue()).floatValue() * 100);
                 progressBar.setValue(value.intValue());
                 VerticalProgressBar.this.revalidate();
                 VerticalProgressBar.this.repaint();
             }
         });
     }
 }
