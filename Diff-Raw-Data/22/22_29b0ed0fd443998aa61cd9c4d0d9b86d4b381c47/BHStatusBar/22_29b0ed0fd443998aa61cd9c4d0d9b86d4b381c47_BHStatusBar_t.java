 package org.bh.gui.swing;
 
 import java.awt.Color;
 import java.awt.Cursor;
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
 import org.bh.platform.i18n.ITranslator;

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
 
 @SuppressWarnings("serial")
 public class BHStatusBar extends JPanel {
 
 	private static BHStatusBar instance = null;
 	JLabel bh;
 	JLabel hintLabel;
 	JLabel errorHintLabel;
 	CellConstraints cons;
 
 	PopupFactory factory;
 	public Popup popup;
 
 	JScrollPane popupPane;
 	boolean open;
 
 	ITranslator translator = BHTranslator.getInstance();
 
 	JOptionPane optionPane;
 
 	private BHStatusBar() {
 		// setLayout to the status bar
 		String rowDef = "p";
 		String colDef = "6px,fill:1px:grow,6px,min,6px,fill:1px:grow,6px";
 		FormLayout layout = new FormLayout(colDef, rowDef);
 		layout.setColumnGroups(new int[][] { { 2, 6 } });
 		setLayout(layout);
 		cons = new CellConstraints();
 		
 		hintLabel = new JLabel("");
 		hintLabel.setIcon(ValidationResultViewFactory.getLargeInfoIcon());
 		removeHint();
 		
 		errorHintLabel = new JLabel(translator.translate("errorHint"));
 		errorHintLabel.setIcon(ValidationResultViewFactory.getLargeErrorIcon());
 		errorHintLabel.addMouseListener(new BHLabelListener());
		errorHintLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		removeErrorHint();
 
 		// create BH logo label
 		bh = new JLabel(new ImageIcon(BHStatusBar.class
 				.getResource("/org/bh/images/bh-logo-2.png")));
 
 		// add components to panel
 		add(hintLabel, new CellConstraints().xywh(2, 1, 1, 1));
 		add(bh, cons.xywh(4, 1, 1, 1));
 		add(errorHintLabel, new CellConstraints().xywh(6, 1, 1, 1,"right,center"));
 	}
 
 	public static BHStatusBar getInstance() {
 		if (instance == null) {
 			instance = new BHStatusBar();
 		}
 		return instance;
 	}
 
 	public void setHint(String hintText) {
 		setHint(hintText, false);
 	}
 
 	public void setHint(String hintText, boolean alert) {
 		hintLabel.setText(hintText);
 		if (alert) {
 			hintLabel.setForeground(Color.RED);
 		} else {
 			hintLabel.setForeground(Color.BLACK);
 		}
 		hintLabel.setVisible(true);
 
 	}
 
 	public void setErrorHint(JScrollPane pane) {
 		errorHintLabel.setVisible(true);
 		
 		popupPane = pane;
 
 		// creates the popup for the error information
 		optionPane = new JOptionPane(pane, JOptionPane.PLAIN_MESSAGE);
 	}
 
 	public void removeHint() {
 		hintLabel.setVisible(false);
 	}
 
 	public void removeErrorHint() {
 		errorHintLabel.setVisible(false);
 	}
 
 	public void openToolTipPopup() {
 		factory = PopupFactory.getSharedInstance();
 		popup = factory.getPopup(this, popupPane, 500, 500);
 		popup.show();
 	}
 
 	/**
 	 * 
 	 * This MouseListener provides the lErrorTip - Label the ability to show a
 	 * popup with validation information
 	 * 
 	 * @author Tietze.Patrick
 	 * @version 1.0, 2009/12/30
 	 * @version 1.1, 2010/01/25
 	 * 
 	 */
 
 	class BHLabelListener extends MouseInputAdapter {
 
 		public BHLabelListener() {
 			open = false;
 		}
 
 		public void mouseClicked(MouseEvent e) {
			optionPane.createDialog(null, translator.translate("errorsDialogTitle")).setVisible(true);
 		}
 	}
 }
