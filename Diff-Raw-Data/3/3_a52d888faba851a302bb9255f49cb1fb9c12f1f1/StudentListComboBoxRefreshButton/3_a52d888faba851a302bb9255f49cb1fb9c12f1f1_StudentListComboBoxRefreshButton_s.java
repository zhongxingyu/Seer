 package org.studentbase.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 
 public class StudentListComboBoxRefreshButton extends JButton 
 implements ActionListener{
 
 	private StudentInputPanel studentInputPanel = null;
 	private StudentStatisticsPanel studentStatisticsPanel = null;
 	private StudentListComboBox comboBox = null;
 	private int dim = 25;
 	private int width = dim, height = dim;
 
 	public StudentListComboBoxRefreshButton(StudentListComboBox comboBox) {
 		super();
 
 		this.comboBox = comboBox;
 		this.addActionListener(this);
 		this.setToolTipText("Ανανέωση της λίστας με τους μαθητές");
 
		ImageIcon bg_img = new ImageIcon("images/Button-Refresh-icon.jpg");
 		ImageIcon thumb = new ImageIcon(getScaledImage(bg_img.getImage(), width, height));
 
 		this.setIcon(thumb);
 		this.setBackground(Color.white);
 		this.setSize(new Dimension(width, height));
 		this.setPreferredSize(new Dimension(width, height));
 	}
 
 	public void setStudentInputPanel(StudentInputPanel studentInputPanel) {
 		this.studentInputPanel = studentInputPanel;
 	}
 
 	public void setStudentStatisticsPanel(
 			StudentStatisticsPanel studentStatisticsPanel) {
 		this.studentStatisticsPanel = studentStatisticsPanel;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (this.studentInputPanel != null)
 			this.studentInputPanel.reset();
 		if (this.comboBox != null)
 			this.comboBox.refresh();
 		if (this.studentStatisticsPanel != null)
 			this.studentStatisticsPanel.refresh();
 	}
 
 	/**
 	 * Resizes an image using a Graphics2D object backed by a BufferedImage.
 	 * @param srcImg - source image to scale
 	 * @param w - desired width
 	 * @param h - desired height
 	 * @return - the new resized image
 	 */
 	private Image getScaledImage(Image srcImg, int w, int h){
 		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = resizedImg.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		g2.drawImage(srcImg, 0, 0, w, h, null);
 		g2.dispose();
 		return resizedImg;
 	}
 
 }
