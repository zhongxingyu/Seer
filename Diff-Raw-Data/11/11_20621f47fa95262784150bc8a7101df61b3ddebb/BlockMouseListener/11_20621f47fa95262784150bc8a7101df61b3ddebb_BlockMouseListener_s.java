 package element_actions;
 
 import gui.GPopup;
 
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.LineBorder;
 
 /**
  * Mouse listener pentru un block.
  * 
  * @author Unknown-Revengers
  */
 public class BlockMouseListener extends MouseAdapter {
 
 	GPopup popup;
 
 	public BlockMouseListener(GPopup popup) {
 		this.popup = popup;
 	}
 
 	/**
 	 * Mouse over. Schimba culoare border in albastru.
 	 * 
 	 * @param e
 	 *            MouseEvent
 	 * 
 	 * @return void
 	 */
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		((JPanel) e.getSource()).setBorder(new LineBorder(Color.BLUE));
 	}
 
 	/**
 	 * Mouse click. Selecteaza / deselecteaza element. Selectia este tinuta in
 	 * tooltip.
 	 * 
 	 * @param e
 	 *            MouseEvent
 	 * 
 	 * @return void
 	 */
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			// Elementul nu este selectat => selecteaza.
 			if (((JPanel) e.getSource()).getToolTipText() == null
 					|| ((JPanel) e.getSource()).getToolTipText().compareTo(
 							"selected") != 0) {
 				((JPanel) e.getSource())
 						.setBorder(new LineBorder(Color.YELLOW));
 				((JPanel) e.getSource()).setToolTipText("selected");
 			}
 			// Elementul este selectat => deselecteaza.
 			else {
 				((JPanel) e.getSource()).setBorder(new LineBorder(Color.GREEN));
 				((JPanel) e.getSource()).setToolTipText("");
 			}
 		}
 	}
 
 	/**
 	 * Mouse exit. Schimba culoare border in verde daca nu e selectat si in
 	 * galben daca este selectat.
 	 * 
 	 * @param e
 	 *            MouseEvent
 	 * 
 	 * @return void
 	 */
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// Elementul nu este selectat.
 		if (((JPanel) e.getSource()).getToolTipText() == null
 				|| ((JPanel) e.getSource()).getToolTipText().compareTo(
 						"selected") != 0) {
 			((JPanel) e.getSource()).setBorder(new LineBorder(Color.GREEN));
 		}
 		// Elementul este selectat.
 		else {
 			((JPanel) e.getSource()).setBorder(new LineBorder(Color.YELLOW));
 		}
 	}
 
 	/**
 	 * Mouse pressed.
 	 * 
 	 * @param e
 	 *            MouseEvent
 	 * 
 	 * @return void
 	 */
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.isPopupTrigger()) {
 			this.showPopup(e);
 		}
 	}
 
 	/**
 	 * Mouse released.
 	 * 
 	 * @param e
 	 *            MouseEvent
 	 * 
 	 * @return void
 	 */
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.isPopupTrigger()) {
 			this.showPopup(e);
 		}
 	}
 
 	/**
 	 * Show popup where mouse is clicked.
 	 * 
 	 * @param e
 	 *            MouseEvent
 	 * 
 	 * @return void
 	 */
 	private void showPopup(MouseEvent e) {
 		this.popup.setGX(((JPanel) e.getSource()).getX() + e.getX());
 		this.popup.setGY(((JPanel) e.getSource()).getY() + e.getY());
 		this.popup.show((JPanel) e.getSource(), e.getX() + 1, e.getY() + 1);
 	}
 }
