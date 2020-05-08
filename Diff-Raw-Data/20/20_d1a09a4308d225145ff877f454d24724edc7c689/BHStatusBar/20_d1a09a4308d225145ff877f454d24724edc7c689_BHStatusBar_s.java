 package org.bh.gui.swing;
 
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
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
	static BHLabel lToolTip;
	static BHLabel lErrorTip;
 	CellConstraints cons;
 	
 	PopupFactory factory;
 	public Popup popup;
 	
 	JScrollPane popupPane;
 	boolean open;
 	
 	BHTranslator translator = BHTranslator.getInstance(); 
 
 	private BHStatusBar() {
 				
 		//setLayout to the status bar
 		String rowDef = "p";
 		String colDef = "0:grow(0.4),0:grow(0.2),0:grow(0.4)";
 		setLayout(new FormLayout(colDef, rowDef));
 		cons = new CellConstraints();
 
 		// create tool tip label
		lToolTip = new BHLabel("");
 		lToolTip.setText("");
 		
 		//create a second label for the errors
		lErrorTip = new BHLabel("");
 		lErrorTip.addMouseListener(new BHLabelListener());
 		lErrorTip.setVisible(false);
 		
 		//Test the Popup		
 //		popupPane = new JScrollPane(new JLabel("TEST"));
 //		factory = PopupFactory.getSharedInstance();
 //	    popup = factory.getPopup(this, popupPane, 500, 500);
 
 
 		// create BH logo label
 		bh = new JLabel(new ImageIcon(BHSplashScreen.class
 				.getResource("/org/bh/images/bh-logo.jpg")));
 
 		// add components to panel
 		add(lToolTip, cons.xywh(1, 1, 1, 1));
 		add(lErrorTip, cons.xywh(1, 1, 1, 1));
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
	public void setToolTipLabel(BHLabel toolTip) {
 		//TODO Ausprogrammieren: Entweder "" oder Fehler anzeigen, falls vorhanden (evtl. mit "Link" zu 
 		//Popup mit allen Fehlermeldungen. Beispiel:
 		//Fremdkapital darf keine Buchstaben enthalten (hier klicken für alle Meldungen...)
 		//this.remove(lToolTip);
 		lToolTip=toolTip;
 		add(lToolTip, cons.xywh(1, 1, 1, 1));
 		this.revalidate();
 	}
 	
 	public void setToolTip(String toolTip){
 		setToolTip(toolTip, false);
 	}
 	
 	public void setToolTip(String toolTip, boolean alert){
 		lToolTip.setText(toolTip);
 		if(alert)lToolTip.setForeground(Color.red);
 		add(lToolTip, cons.xywh(1, 1, 1, 1));
 		this.revalidate();
 		//setToolTip(new JScrollPane(new JLabel("TEST")));
 	}
 	
 	public void setToolTip(JScrollPane pane) {
 		//TODO InfoText festlegen: ob erster Fehler aus Liste oder allgemeiner Hinweis!?
 		
 		lErrorTip.setText(translator.translate("LtoolTip"));
 		popupPane = pane;
 		
 		factory = PopupFactory.getSharedInstance();
 		//TODO genaue Koordniaten für das Popup bestimmen -> abhängig von MainFrame-Größe und Bidlschirmauflösung
 	    popup = factory.getPopup(this, pane, 500, 500);
 	    
 		add(lErrorTip, cons.xywh(1, 1, 1, 1));
 		this.revalidate();
 	}
 	
 	public void removeToolTip(){
 		lToolTip.setText(" ");
 		add(lToolTip, cons.xywh(1, 1, 1, 1));
 		this.revalidate();
 	}
 	
 	public void removeErrorTip(){
 		lErrorTip.setText(" ");
 		lErrorTip.setVisible(false);
 //		add(lToolTip, cons.xywh(1, 1, 1, 1));
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
 		public void mouseClicked(MouseEvent e){
 			if(!open){
     			popup.show();
     			open = true;
 			}else if(open){
     			popup.hide();
     			open = false;
     		}
 		}
 	
     }
 
 }
