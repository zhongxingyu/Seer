 package org.bh.gui.swing;
 
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.Popup;
 import javax.swing.PopupFactory;
 import javax.swing.event.MouseInputAdapter;
 import org.bh.platform.i18n.BHTranslator;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * 
  * BHStatusBar to display a status bar on the bottom of the screen
  * 
  * @author Tietze.Patrick
  * @version 0.1, 2009/12/05
  * @version 0.2, 2009/12/29
  * 
  **/
 
 public class BHStatusBar extends JPanel{
 
 	private static BHStatusBar instance = null;
 	JLabel bh;
 	JLabel hint;
 	JLabel errorHint;
 	CellConstraints cons;
 	
 	PopupFactory factory;
 	public Popup popup;
 	
 	JScrollPane popupPane;
 	boolean open;
 	
 	BHTranslator translator = BHTranslator.getInstance(); 
 	
 	JOptionPane optionPane;
 
 	private BHStatusBar() {
 				
 		//setLayout to the status bar
 		String rowDef = "p";
		String colDef = "0:grow(0.01),0:grow(0.39),0:grow(0.2),0:grow(0.4)";
 		setLayout(new FormLayout(colDef, rowDef));
 		cons = new CellConstraints();
 
 		// create tool tip label
 		hint = new JLabel("");
 		hint.setText("");
 		
 		//create a second label for the errors
 		errorHint = new JLabel("");
 		errorHint.addMouseListener(new BHLabelListener());
 		errorHint.setVisible(false);
 
 		
 		
 		//Test the Popup		
 //		popupPane = new JScrollPane(new JLabel("TEST"));
 //		factory = PopupFactory.getSharedInstance();
 //	    popup = factory.getPopup(this, popupPane, 500, 500);
 
 
 		// create BH logo label
 		bh = new JLabel(new ImageIcon(BHSplashScreen.class
 				.getResource("/org/bh/images/bh-logo-2.png")));
 
 		// add components to panel
		add(hint, cons.xywh(2, 1, 1, 1));
		add(errorHint, cons.xywh(2, 1, 1, 1));
		add(bh, cons.xywh(3, 1, 1, 1));
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
 	public void setHint(JLabel hintLabel) {
 		if(hint != null)
 			this.removeHint();
 		hint=hintLabel;
		add(hint, cons.xywh(2, 1, 1, 1));
 		this.revalidate();
 		
 		//popup test
 //		optionPane = new JOptionPane(new JLabel("TEST"), JOptionPane.PLAIN_MESSAGE);
 //		optionPane.createDialog(null, "Errors").setVisible(true);
 		
 	}
 	
 	public void setHint(String toolTip){
 		setHint(toolTip, false);
 	}
 	
 	public void setHint(String hintText, boolean alert){
 		hint.setText(hintText);
 		if(alert){
 			hint.setForeground(Color.red);
 		}else{
 			hint.setForeground(Color.black);
 		}
		add(hint, cons.xywh(2, 1, 1, 1));
 		this.revalidate();
 		//TEST
 		//setToolTip(new JScrollPane(new JLabel("TEST")));
 	}
 	
 	public void setErrorHint(JScrollPane pane) {
 		//TODO InfoText festlegen: ob erster Fehler aus Liste oder allgemeiner Hinweis!?
 		
 		errorHint.setText("   "+translator.translate("LtoolTip"));
 		popupPane = pane;
 		
 //		factory = PopupFactory.getSharedInstance();
 //		//TODO genaue Koordniaten für das Popup bestimmen -> abhängig von MainFrame-Größe und Bidlschirmauflösung
 //	    popup = factory.getPopup(this, pane, 500, 500);
 
 		//creates the popup for the error information
 		optionPane = new JOptionPane(pane, JOptionPane.PLAIN_MESSAGE);
     
		add(errorHint, cons.xywh(2, 1, 1, 1));
 		this.revalidate();
 	}
 	
 	public void removeHint(){
 		hint.setText(" ");
		add(hint, cons.xywh(2, 1, 1, 1));
 		this.revalidate();
 	}
 	
 	public void removeErrorHint(){
 		errorHint.setText(" ");
 		errorHint.setVisible(false);
//		add(lToolTip, cons.xywh(2, 1, 1, 1));
 //		this.revalidate();
 	}
 
 
 	public void openToolTipPopup(){
 		factory = PopupFactory.getSharedInstance();
 	    popup = factory.getPopup(this, popupPane, 500, 500);
 	    popup.show();
 	}
 
 	/**
 	 * 
 	 * This MouseListener provides the lErrorTip - Label the ability to show a popup with validation information
 	 * 
 	 * @author Tietze.Patrick
 	 * @version 1.0, 2009/12/30
 	 *
 	 */
 	
 	class BHLabelListener extends MouseInputAdapter{
     	
 		public BHLabelListener(){
 			open = false;
 		}
 		@Override
 		public void mouseClicked(MouseEvent e){
 //			if(!open){
 //    			popup.show();
 //    			open = true;
 //			}else if(open){
 //    			popup.hide();
 //    			open = false;
 //    		}
 			optionPane.createDialog(null, "Errors").setVisible(true);
 		}
 	
     }
 
 }
