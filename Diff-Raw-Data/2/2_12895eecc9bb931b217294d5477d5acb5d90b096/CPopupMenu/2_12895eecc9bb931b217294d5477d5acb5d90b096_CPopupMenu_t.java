 /*
  * Part of Fresco software under GPL licence
  * http://www.gnu.org/licenses/gpl-3.0.txt
  */
 package fresco.swing;
 
 import fresco.CData;
 import fresco.action.IAction.RegID;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JComponent;
 import javax.swing.JPopupMenu;
 
 /**
  *
  * @author gimli
  */
 public class CPopupMenu extends MouseAdapter {
 
 	public CPopupMenu(final JComponent main, final int id) {
 		super();
 		// create popup menu for previewBar
 		previewPopUp = new JPopupMenu();
 
 		CPopupMenuItem open = new CPopupMenuItem("Open as 2nd  CTRL+click", RegID.load2ndInput, id);
 		open.addActionListener(CData.userActionListener);
 		previewPopUp.add(open);
 
		CPopupMenuItem saveAs = new CPopupMenuItem("Save As ...", RegID.saveAs, id);
 		saveAs.addActionListener(CData.userActionListener);
 		previewPopUp.add(saveAs);
 
 		CPopupMenuItem rename = new CPopupMenuItem("Rename...", RegID.rename, id);
 		rename.addActionListener(CData.userActionListener);
 		previewPopUp.add(rename);
 
 		CPopupMenuItem close = new CPopupMenuItem("Close...", RegID.close, id);
 		close.addActionListener(CData.userActionListener);
 		previewPopUp.add(close);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		showPopup(e);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		showPopup(e);
 	}
 
 	private void showPopup(MouseEvent e) {
 		if (e.isPopupTrigger()) {
 			previewPopUp.show(e.getComponent(), e.getX(), e.getY());
 		}
 	}
 	private JPopupMenu previewPopUp;
 }
