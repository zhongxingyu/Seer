 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 
 @SuppressWarnings("serial")
 public class RecognitionWin extends JFrame {
 	
 	private ImagesPanel imagesPanel;
 	private JButton btnReset;
 	private BufferedImage image;
 	
 	public RecognitionWin() {
 		setTitle("Handwritten Digit Recognition");
 		setSize(710, 480);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setResizable(false);
 		setLayout(null);
 		
 		ImagesPanel imagesPanel = new ImagesPanel();
		JButton btnReset = new JButton("Reset");
 		btnReset.setBounds(565, 400, 100, 25);
 		btnReset.setFocusPainted(false);
 		btnReset.addActionListener(new ResetButtonListener());
 		getContentPane().add(btnReset);
 		getContentPane().add(imagesPanel);
 	}
 	
 	public void loadImage() {
 		boolean[][] data = Shared.drawPanel.getData();
 		image = new BufferedImage(280, 280, BufferedImage.TYPE_BYTE_BINARY);
 		for (int i = 0; i < data.length; i++)
 			for (int j = 0; j < data[i].length; j++) {
 				int color = (data[i][j])? -1 : -16777216;
 				image.setRGB(i, j, color);
 			}
 		if (imagesPanel != null) imagesPanel.repaint();
 	}
 	
 	public BufferedImage getImage() {
 		return image;
 	}
 	
 	private class ResetButtonListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			dispose();
 			Shared.drawWin = new DrawWin();
 		}
 		
 	}
 	
 }
