 package org.bh.gui.swing;
 
 import java.awt.Color;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * 
  * BHStatusBar to display a status bar on the bottom of the screen
  * 
  * @author Tietze.Patrick
  * @version 0.1, 2009/12/05
  * 
  **/
 
 public class BHStatusBar extends JPanel {
 
 	private static BHStatusBar instance = null;
 	JLabel bh;
 	static JLabel lToolTip;
 	CellConstraints cons;
 
 	private BHStatusBar() {
		setBackground(Color.white);
 
 		// setLayout to the status bar
 		String rowDef = "p";
 		String colDef = "0:grow(0.4),0:grow(0.2),0:grow(0.4)";
 		setLayout(new FormLayout(colDef, rowDef));
 		cons = new CellConstraints();
 
 		// create tool tip label
 		lToolTip = new JLabel();
 		lToolTip.setToolTipText("");
 
 		// create BH logo label
 		bh = new JLabel(new ImageIcon(BHSplashScreen.class
 				.getResource("/org/bh/images/bh-logo.jpg")));
 
 		// add components to panel
 		add(lToolTip, cons.xywh(1, 1, 1, 1));
 		add(bh, cons.xywh(2, 1, 1, 1));
 	}
 
 	public static BHStatusBar getInstance() {
 		if (instance == null) {
 			instance = new BHStatusBar();
 		}
 		return instance;
 	}
 
 	/**
 	 * create the status bar with the tooltip included
 	 * 
 	 * @param toolTip
 	 */
 	public void setToolTip(String toolTip) {
 		lToolTip.setText(toolTip);
 		// test.repaint();
 		lToolTip.revalidate();
 
 	}
 
 	public void setValidationToolTip(JLabel label) {
 		lToolTip.setText(label.getText());
 		lToolTip.revalidate();
 	}
 
 }
