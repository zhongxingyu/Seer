 package views.listeners;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 
 import views.MapPanel;
 import controllers.MapController;
 import controllers.OperationController;
 
 public class SubMenuMapToggleButtonListener implements ActionListener {
 	
 	private OperationController _operationController;
 	private MapController _mapController;
 	private MapPanel _mapPanel;
 	
 	public SubMenuMapToggleButtonListener(MapController mapController, MapPanel mapPanel, OperationController operationController) {
 		_operationController = operationController;
 		_mapController = mapController;
 		_mapPanel = mapPanel;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		ImageIcon image = _mapController.getImage();
 		
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		double width = screenSize.getWidth();
 		double height = screenSize.getHeight();
 		double ratio = 1;
 		
 		if(image.getIconHeight() > height && image.getIconWidth() > width) {
 			if(image.getIconHeight() > image.getIconWidth()) {
 				ratio = width/image.getIconWidth();
 			} else {
 				ratio = height/image.getIconHeight();
 			}
 		} else if(image.getIconHeight() > height) {
 			ratio = height/image.getIconHeight();
 		} else if(image.getIconWidth() > width) {
 			ratio = width/image.getIconWidth();
 		}
 		
 		BufferedImage newMap = new BufferedImage((int)(image.getIconWidth() * ratio), (int)(image.getIconHeight()*ratio), BufferedImage.TYPE_INT_RGB);
 		newMap.getGraphics().drawImage(image.getImage(), 0, 0, (int)(image.getIconWidth()*ratio), (int)(image.getIconHeight()*ratio), null);
 		_mapPanel.setMap(newMap);
 		_operationController.setCurrentMap(_mapController);
 		_mapPanel.resetPosition();
 		_mapPanel.repaint();
 		_mapPanel.revalidate();
 	}
 
 }
