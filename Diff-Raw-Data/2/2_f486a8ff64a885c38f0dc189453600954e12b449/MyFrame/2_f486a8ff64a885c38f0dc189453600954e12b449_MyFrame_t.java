 import java.awt.BorderLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class MyFrame extends JFrame
 {
 	MyFrame()
 	{
 		// creates a Frame that contains 8 rows and 8 columns of randomly picked, colored and sized shapes
 		super();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(800, 800);
 		
 		final ImageHolder image = new ImageHolder();
 		
 		JPanel buttonContainer = new JPanel();
 		//Navigation buttonContainer = new Navigation();
 		
 		JButton nextButton = new JButton();
         ImageIcon nextButtonImage = new ImageIcon("/Users/Thomas/Dropbox/Programming/OOP/Labore/src/labor5/icons/right.png");
         nextButton.setIcon(nextButtonImage);
         
         JButton prevButton = new JButton();
         ImageIcon prevButtonImage = new ImageIcon("/Users/Thomas/Dropbox/Programming/OOP/Labore/src/labor5/icons/left.png");
         prevButton.setIcon(prevButtonImage);
         
         // zoom buttons
         JButton zoomOut = new JButton();
         ImageIcon zoomOutImage = new ImageIcon("/Users/Thomas/Dropbox/Programming/OOP/Labore/src/labor5/icons/zoomOut.png");
         zoomOut.setIcon(zoomOutImage);
         
         JButton zoomReal = new JButton();
        ImageIcon zoomRealImage = new ImageIcon("/Users/Thomas/Dropbox/Programming/OOP/Labore/src/labor5/icons/defaultSize.png");
         zoomReal.setIcon(zoomRealImage);
         
         JButton zoomIn = new JButton();
         ImageIcon zoomInImage = new ImageIcon("/Users/Thomas/Dropbox/Programming/OOP/Labore/src/labor5/icons/zoomIn.png");
         zoomIn.setIcon(zoomInImage);
         
         
         // action listeners
         nextButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		System.out.println("next Picture");
         		int thisPicture = image.getWhichPicture();
         		image.setWhichPicture(++thisPicture);
         		image.repaint();
         	}
 		});
         
         prevButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		System.out.println("previous Picture");
         		int thisPicture = image.getWhichPicture();
         		image.setWhichPicture(--thisPicture);
         		image.repaint();
         	}
 		});
         
         zoomOut.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		System.out.println("zoom out");
         	}
 		});
         
         zoomReal.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		System.out.println("default zoom");
         	}
 		});
         
         zoomIn.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		System.out.println("zoom in");
         	}
 		});
         
         
         
         // add zoom and navigation buttons to JPanel
         buttonContainer.add(zoomOut);
         buttonContainer.add(zoomReal);
         buttonContainer.add(zoomIn);
         buttonContainer.add(prevButton);
         buttonContainer.add(nextButton);
         
         // add image to center of frame
         add(image, BorderLayout.CENTER);
         
         // add panel containing buttons to south of frame
         add(buttonContainer, BorderLayout.SOUTH);
 		
 		setVisible(true);
 	}
 }
