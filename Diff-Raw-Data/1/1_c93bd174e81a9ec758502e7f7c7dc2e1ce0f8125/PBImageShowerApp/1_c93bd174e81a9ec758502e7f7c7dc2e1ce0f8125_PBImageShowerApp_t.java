 /**
  * 
  */
 package de.fumanoids;
 
 import java.awt.BorderLayout;
 import java.io.File;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import de.fumanoids.gui.ImagePanel;
 import de.fumanoids.message.MsgImage;
 
 /**
  * @author naja
  * 
  */
 public class PBImageShowerApp {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		// if(args.length != 1) {
 		// System.out.println("Give path to pbi file as param!");
 		// System.exit(1);
 		// }
 
 		JFrame frame = new JFrame("PBI Image Viewer");
 
 		frame.setSize(640, 620);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel mainPanel = new JPanel(new BorderLayout());
 
 		frame.add(mainPanel);
 
 		ImagePanel imgPanel = new ImagePanel(640, 480);
 		JTextArea infoTextAres = new JTextArea();
 
 		mainPanel.add(imgPanel, BorderLayout.CENTER);
 		mainPanel.add(infoTextAres, BorderLayout.SOUTH);
 
 		frame.setVisible(true);
 
 		// load pbi image and show it
 		String pbiFilePath = "";
         File pbiFile = null;
 		if (args.length != 1) {
 			pbiFilePath = "img/1.pbi";
             try {
             java.net.URL file = PBImageShowerApp.class
                                 .getResource("/img/1.pbi");
             pbiFile = new File(file.toURI());
             } catch (Exception ex) {
                 ex.printStackTrace();
                 // TODO: hanle exception
             }
         } else {
 			pbiFilePath = args[0];
             pbiFile = new File(pbiFilePath);
         }
 
 		MsgImage.Image pbImage = ImageManager.openImage(pbiFile);
 		pbImage.getCenter();
 		imgPanel.setImage(pbImage);
 
 		// show infos of image
 		infoTextAres.setText("" + "Loaded image: " + pbiFilePath + "\n"
 				+ "Pitch: " + imgPanel.getPitch() + "\n" + "Roll: "
 				+ imgPanel.getRoll() + "\n" + "Image center x: "
 				+ imgPanel.getCenterX() + "\n" + "Image center y: "
 				+ imgPanel.getCenterY() + "\n" + "Image focal length: "
 				+ imgPanel.getFocalLength() + "\n");
 
 	}
 
 }
