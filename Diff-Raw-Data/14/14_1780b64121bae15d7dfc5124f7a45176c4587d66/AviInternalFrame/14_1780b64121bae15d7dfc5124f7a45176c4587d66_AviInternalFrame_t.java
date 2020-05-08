 package org.peie.avicultura.helper;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyVetoException;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JInternalFrame;
 import javax.swing.JPanel;
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 
 import org.apache.log4j.Logger;
 import org.peie.avicultura.viewer.Application;
 import org.peie.avicultura.viewer.ZuchtPaareFrame;
 
 public class AviInternalFrame extends JInternalFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2585102774686646569L;
 	private String name;
 	private JPanel taskBar;
 	private JButton button = new JButton();
 	private URL url;
 	private Logger LOG = Logger.getLogger(AviInternalFrame.class);
 
 	public AviInternalFrame(JPanel taskBar) {
 		createFrame(taskBar, "N/A");
 	}
 
 	public AviInternalFrame(JPanel taskBar, String name) {
 
 		createFrame(taskBar, name);
 
 	}
 
 	private void createFrame(JPanel taskBar, String name) {
 
 		url = ClassLoader.getSystemResource(Application.ICONS_LOGO_LITTLE_PNG);
 
 		try {
 			BufferedImage icon = ImageIO.read(url);
 			super.setFrameIcon(new ImageIcon(icon));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		this.name = name;
 		this.taskBar = taskBar;
 		this.addInternalFrameListener(new InternalFrameAdapter() {
 			@Override
 			public void internalFrameClosing(InternalFrameEvent arg0) {
 
 				button.setVisible(false);
 				super.internalFrameClosing(arg0);
 			}
 
 		});
 
 		newTask();
 	}
 
 	private void newTask() {
 
 		super.setTitle(name);
 		button.setText(name);
 		button.setToolTipText(name);
 		button.setPreferredSize(new Dimension(110, 25));
 		button.setFont(new Font("Arial", Font.PLAIN, 10));
 		button.setMargin(new Insets(0, 0, 0, 0));
 		button.setBackground(Color.darkGray);
 		button.setForeground(Color.WHITE);
 		//button.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
 		button.setHorizontalAlignment(JButton.LEADING);
 
 		try {
 			BufferedImage icon = ImageIO.read(url);
 			button.setIcon(new ImageIcon(icon));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		button.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				LOG.info(isShowing());
 
 				if (isVisible()) {
 					
 					if(isSelected){
 					setVisible(false);
 					button.setBackground(Color.lightGray);
 					button.setForeground(Color.darkGray);
 					}else{
 						try {
 							setSelected(true);
 						} catch (PropertyVetoException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 					}
 
 				} else {
 					setVisible(true);
 					button.setBackground(Color.darkGray);
 					button.setForeground(Color.WHITE);
 				}
 
 			}
 		});
 
 		
 		button.setVisible(true);
 
 		taskBar.add(button);
 
 	}
 	
 	public void setButtonBild(Icon bild){
 		LOG.info("button => "+button);
 		
 		button.setIcon(bild);
 		super.setFrameIcon(bild);
 		
 	}
 	
 	@Deprecated
 	public void setFrameIcon(Icon arg0) {
 		
 		
 		super.setFrameIcon(arg0);
 	}
 	
 	
 
 	@Override
 	public void setTitle(String arg0) {
 		
 		LOG.info(arg0 + " " + button);
 		button.setText(arg0);
 		button.setToolTipText(arg0);
 		super.setTitle(arg0);
 	}
 	

	public void setVisibleAuswahl(boolean aFlag) {
 		
 		if (button != null){
 			button.setVisible(aFlag);
 		}
 		
 		
 		
 		super.setVisible(aFlag);
 	}
 	
 	
 
 }
