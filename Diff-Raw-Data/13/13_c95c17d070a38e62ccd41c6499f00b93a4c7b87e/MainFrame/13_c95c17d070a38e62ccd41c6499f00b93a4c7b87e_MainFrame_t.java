 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * MainFrame.java
  *
  * Created on 2011-12-9, 17:18:25
  */
 package com.lorent.lvmc.ui;
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPopupMenu;
 
 import net.infonode.docking.RootWindow;
 
 import org.apache.log4j.Logger;
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.painter.ImagePainter;
 import org.jdesktop.swingx.painter.ImagePainter.ScaleType;
 
 import com.lorent.lvmc.controller.ControllerFacade;
 import com.lorent.lvmc.controller.ViewManager;
 import com.lorent.lvmc.util.ConfigUtil;
 import com.lorent.lvmc.util.Constants;
import com.lorent.lvmc.util.LvmcUtil;
 import com.lorent.lvmc.util.StringUtil;
 
 /**
  *
  * @author Administrator
  */
 public class MainFrame extends javax.swing.JFrame implements MainWindow {
 
 	private DockingLayoutMeetingPanel dockingLayoutMeetingPanel;
 	private Logger log = Logger.getLogger(MainFrame.class);
 
 	public DockingLayoutMeetingPanel getDockingLayoutMeetingPanel() {
 		return dockingLayoutMeetingPanel;
 	}
 
 	private static MainFrame instance;
 	private boolean shareDesktopFlag = false;
 	private int isMaximized = 0;
 	private Point point = null;
 	private java.awt.Dimension dimension = null;
 
 	public int getIsMaximized() {
 		return isMaximized;
 	}
 
 	public void setPoint(Point p) {
 		this.point = p;
 	}
 
 	public void setDimension(java.awt.Dimension d) {
 		this.dimension = d;
 	}
 
 	//	private BackgroundPanel backgroundPanel = new BackgroundPanel();
 
 	/** Creates new form MainFrame */
 	public MainFrame() {
 		//		this.getLayeredPane().add(backgroundPanel,
 		//				new Integer(Integer.MIN_VALUE));
 		//		((JPanel) this.getContentPane()).setOpaque(false);
 		//		MainFrameResizeAdapter resizeAdapter = new MainFrameResizeAdapter(this,
 		//				this);
 		//		addMouseMotionListener(resizeAdapter);
 		//		this.addMouseListener(resizeAdapter);
 		initComponents();
 		setIconImage(Toolkit.getDefaultToolkit().createImage(
 				getClass().getResource(ConfigUtil.getProperty("logo_path"))));
 		try {
 			dockingLayoutMeetingPanel = ViewManager
 					.getComponent(DockingLayoutMeetingPanel.class);//new DockingLayoutMeetingPanel();
 		} catch (Exception ex) {
 			log.error("MainFrame()", ex);
 		}
 
 		RootWindow rootWindow = dockingLayoutMeetingPanel.getRootWindow();
 		//		DockingResizeAdapter dockingResizeAdapter = new DockingResizeAdapter(
 		//				rootWindow, this);
 		//		rootWindow.addMouseMotionListener(dockingResizeAdapter);
 		//		rootWindow.addMouseListener(dockingResizeAdapter);
 		//		rootWindow.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 5,
 		//				5, 5, ((NimRODLookAndFeel) UIManager.getLookAndFeel())
 		//						.getControl()));
 		//.createLineBorder(((NimRODLookAndFeel) UIManager.getLookAndFeel()).getControl(),5)
 		this.add(rootWindow, BorderLayout.CENTER);
 		this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
 		//		setMaximized();
 		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
 
 		//		JFlashPlayer flashPlayer = new JFlashPlayer();
 		//		flashPlayer.load(Constants.AppPath + "/logo.swf");
 		//		flashPlayer.setPreferredSize(new Dimension(500, 60));
 		try {
 			File logoFile = new File(Constants.AppPath + "/logo.png");
 			BufferedImage img = ImageIO.read(logoFile);
 			ImagePainter ip = new ImagePainter(img);
 			ip.setScaleToFit(true);
 			ip.setScaleType(ScaleType.Distort);
 			JXPanel panel = new JXPanel();
 			panel.setBackgroundPainter(ip);
 			panel.setPreferredSize(new Dimension(500, 60));
 			this.logoPanel.add(panel);
 		} catch (Exception e) {
 			log.error("MainFrame()", e);
 		}
		if(LvmcUtil.isUCSAPP()){
			themeButton.setVisible(false);
		}
 
 		instance = this;
 	}
 
 	public void setMaximized() {
 		isMaximized = 1;
 		//		this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);//最大化
 		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
 				this.getGraphicsConfiguration());
 		this.setLocation(0, 0);
 		this.setSize(this.getToolkit().getScreenSize().width, this.getToolkit()
 				.getScreenSize().height
 				- screenInsets.bottom);
 	}
 
 	//GEN-BEGIN:initComponents
 	// <editor-fold defaultstate="collapsed" desc="Generated Code">
 	private void initComponents() {
 		java.awt.GridBagConstraints gridBagConstraints;
 
 		jPanel2 = new javax.swing.JPanel();
 		jPanel3 = new javax.swing.JPanel();
 		jPanel4 = new javax.swing.JPanel();
 		logoLabel = new javax.swing.JLabel();
 		titleLabel = new javax.swing.JLabel();
 		jPanel5 = new javax.swing.JPanel();
 		minizableButton = new javax.swing.JButton();
 		maxizableButton = new javax.swing.JButton();
 		jButton3 = new javax.swing.JButton();
 		jPanel1 = new javax.swing.JPanel();
 		jToolBar1 = new javax.swing.JToolBar();
 		themeButton = new javax.swing.JButton();
 		shareDesktopButton = new javax.swing.JButton();
 		saveLayouttButton = new javax.swing.JButton();
 		selectLayoutButton = new javax.swing.JButton();
 		windowButton = new javax.swing.JButton();
 		screenShotButton = new javax.swing.JButton();
 		setupButton = new javax.swing.JButton();
 		logoPanel = new javax.swing.JPanel();
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
 		setMinimumSize(new java.awt.Dimension(800, 600));
 		setUndecorated(true);
 		addWindowListener(new java.awt.event.WindowAdapter() {
 			public void windowClosing(java.awt.event.WindowEvent evt) {
 				formWindowClosing(evt);
 			}
 		});
 		addWindowStateListener(new java.awt.event.WindowStateListener() {
 			public void windowStateChanged(java.awt.event.WindowEvent evt) {
 				formWindowStateChanged(evt);
 			}
 		});
 
 		jPanel2.setLayout(new java.awt.GridBagLayout());
 
 		jPanel3.setMaximumSize(new java.awt.Dimension(2147483647, 0));
 		jPanel3.setPreferredSize(new java.awt.Dimension(101, 0));
 		jPanel3.setLayout(new java.awt.GridBagLayout());
 
 		jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5,
 				2));
 
 		logoLabel.setIcon(new ImageIcon(getClass().getResource(
 				ConfigUtil.getProperty("logo_path"))));
 		jPanel4.add(logoLabel);
 
 		titleLabel.setFont(new java.awt.Font("宋体", 1, 14));
 		jPanel4.add(titleLabel);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 0.9;
 		jPanel3.add(jPanel4, gridBagConstraints);
 
 		jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,
 				0, 2));
 
 		minizableButton.setIcon(new javax.swing.ImageIcon(getClass()
 				.getResource(
 						"/com/lorent/lvmc/resource/images/frameminimiza.png"))); // NOI18N
 		minizableButton.setMaximumSize(new java.awt.Dimension(22, 22));
 		minizableButton.setPreferredSize(new java.awt.Dimension(22, 22));
 		minizableButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				minizableButtonActionPerformed(evt);
 			}
 		});
 		jPanel5.add(minizableButton);
 
 		maxizableButton.setIcon(new javax.swing.ImageIcon(
 				getClass().getResource(
 						"/com/lorent/lvmc/resource/images/frameresize.png"))); // NOI18N
 		maxizableButton.setMaximumSize(new java.awt.Dimension(22, 22));
 		maxizableButton.setPreferredSize(new java.awt.Dimension(22, 22));
 		maxizableButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				maxizableButtonActionPerformed(evt);
 			}
 		});
 		jPanel5.add(maxizableButton);
 
 		jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/lvmc/resource/images/frameclose.png"))); // NOI18N
 		jButton3.setMaximumSize(new java.awt.Dimension(22, 22));
 		jButton3.setPreferredSize(new java.awt.Dimension(22, 22));
 		jButton3.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				jButton3ActionPerformed(evt);
 			}
 		});
 		jPanel5.add(jButton3);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 0;
 		jPanel3.add(jPanel5, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 1.0;
 		jPanel2.add(jPanel3, gridBagConstraints);
 
 		jPanel1.setMaximumSize(new java.awt.Dimension(32767, 60));
 		jPanel1.setPreferredSize(new java.awt.Dimension(361, 60));
 		jPanel1.setLayout(new java.awt.BorderLayout());
 
 		jToolBar1.setFloatable(false);
 		jToolBar1.setMargin(new java.awt.Insets(0, 4, 0, 0));
 		jToolBar1.setOpaque(false);
 
 		themeButton.setFont(new java.awt.Font("新宋体", 0, 12));
 		themeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/lvmc/resource/images/changetheme.png"))); // NOI18N
 		themeButton
 				.setText(StringUtil
 						.getUIString("com.lorent.lvmc.ui.DefaultLayoutMeetingPanel.windowStyleMenu.themeMenu"));
 		themeButton.setFocusable(false);
 		themeButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		themeButton.setIconTextGap(3);
 		themeButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
 		themeButton.setMaximumSize(new java.awt.Dimension(58, 57));
 		themeButton.setPreferredSize(new java.awt.Dimension(58, 57));
 		themeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		themeButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				themeButtonActionPerformed(evt);
 			}
 		});
 		jToolBar1.add(themeButton);
 
 		shareDesktopButton
 				.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 						"/com/lorent/lvmc/resource/images/sharedesktop_32.png"))); // NOI18N
 		shareDesktopButton.setText(StringUtil
 				.getUIString("DockingLayoutMeetingPanel.screenShareMenu.text"));
 		shareDesktopButton.setFocusable(false);
 		shareDesktopButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		shareDesktopButton.setIconTextGap(2);
 		shareDesktopButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
 		shareDesktopButton.setMaximumSize(new java.awt.Dimension(58, 57));
 		shareDesktopButton.setPreferredSize(new java.awt.Dimension(58, 57));
 		shareDesktopButton
 				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		shareDesktopButton
 				.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						shareDesktopButtonActionPerformed(evt);
 					}
 				});
 		jToolBar1.add(shareDesktopButton);
 
 		saveLayouttButton
 				.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 						"/com/lorent/lvmc/resource/images/savelayout.png"))); // NOI18N
 		saveLayouttButton
 				.setText(StringUtil
 						.getUIString("com.lorent.lvmc.ui.DefaultLayoutMeetingPanel.layoutMenuItemSaveLayout"));
 		saveLayouttButton.setFocusable(false);
 		saveLayouttButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		saveLayouttButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
 		saveLayouttButton.setMaximumSize(new java.awt.Dimension(58, 57));
 		saveLayouttButton.setPreferredSize(new java.awt.Dimension(58, 55));
 		saveLayouttButton
 				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		saveLayouttButton
 				.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						saveLayouttButtonActionPerformed(evt);
 					}
 				});
 		jToolBar1.add(saveLayouttButton);
 
 		selectLayoutButton.setIcon(new javax.swing.ImageIcon(getClass()
 				.getResource("/com/lorent/lvmc/resource/images/window.png"))); // NOI18N
 		selectLayoutButton
 				.setText(StringUtil
 						.getUIString("com.lorent.lvmc.ui.DefaultLayoutMeetingPanel.changeLayout"));
 		selectLayoutButton.setFocusable(false);
 		selectLayoutButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		selectLayoutButton.setIconTextGap(2);
 		selectLayoutButton.setMaximumSize(new java.awt.Dimension(58, 57));
 		selectLayoutButton.setPreferredSize(new java.awt.Dimension(58, 57));
 		selectLayoutButton
 				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		selectLayoutButton
 				.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						selectLayoutButtonActionPerformed(evt);
 					}
 				});
 		jToolBar1.add(selectLayoutButton);
 
 		windowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/lvmc/resource/images/views.png"))); // NOI18N
 		windowButton
 				.setText(StringUtil
 						.getUIString("com.lorent.lvmc.ui.DefaultLayoutMeetingPanel.viewsMenu"));
 		windowButton.setFocusable(false);
 		windowButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		windowButton.setIconTextGap(2);
 		windowButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
 		windowButton.setMaximumSize(new java.awt.Dimension(58, 57));
 		windowButton.setMinimumSize(new java.awt.Dimension(53, 56));
 		windowButton.setPreferredSize(new java.awt.Dimension(58, 57));
 		windowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		windowButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				windowButtonActionPerformed(evt);
 			}
 		});
 		jToolBar1.add(windowButton);
 
 		screenShotButton
 				.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 						"/com/lorent/lvmc/resource/images/screenshot.png"))); // NOI18N
 		screenShotButton
 				.setText(StringUtil
 						.getUIString("com.lorent.lvmc.ui.DefaultLayoutMeetingPanel.screenshot.txt"));
 		screenShotButton.setFocusable(false);
 		screenShotButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		screenShotButton.setMaximumSize(new java.awt.Dimension(58, 57));
 		screenShotButton.setPreferredSize(new java.awt.Dimension(58, 57));
 		screenShotButton
 				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		screenShotButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				screenShotButtonActionPerformed(evt);
 			}
 		});
 		jToolBar1.add(screenShotButton);
 
 		setupButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/lvmc/resource/images/setup.png"))); // NOI18N
 		setupButton
 				.setText(StringUtil
 						.getUIString("com.lorent.lvmc.ui.DefaultLayoutMeetingPanel.systemsetup.txt"));
 		setupButton.setFocusable(false);
 		setupButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		setupButton.setMaximumSize(new java.awt.Dimension(58, 57));
 		setupButton.setPreferredSize(new java.awt.Dimension(58, 57));
 		setupButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		setupButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				setupButtonActionPerformed(evt);
 			}
 		});
 		jToolBar1.add(setupButton);
 
 		logoPanel.setOpaque(false);
 		logoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,
 				5, 0));
 		jToolBar1.add(logoPanel);
 
 		jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 1.0;
 		jPanel2.add(jPanel1, gridBagConstraints);
 
 		getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);
 
 		pack();
 	}// </editor-fold>
 	//GEN-END:initComponents
 
 	private void setupButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		SystemSetupPopupMenu systemSetupPopupMenu = new SystemSetupPopupMenu(this.setupButton);
 		systemSetupPopupMenu.showPopup();
 	}
 
 	private void screenShotButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		ControllerFacade.execute("screenShotController", "capture");
 	}
 
 	private void windowButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		WindowPopupMenu windowPopupMenu = new WindowPopupMenu(windowButton);
 		windowPopupMenu.showPopup();
 	}
 
 	private void selectLayoutButtonActionPerformed(
 			java.awt.event.ActionEvent evt) {
 		//		showPopup();
 		//		selectLayoutPopupMenu.show();
 		LayoutPopupMenu layoutPopupMenu = new LayoutPopupMenu(
 				selectLayoutButton);
 		layoutPopupMenu.showPopup();
 	}
 
 	private void formWindowStateChanged(java.awt.event.WindowEvent evt) {
 		if (evt.getNewState() == javax.swing.JFrame.MAXIMIZED_BOTH) {
 			setMaximized();
 			maxizableButton
 					.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 							"/com/lorent/lvmc/resource/images/frameresize.png")));
 		}
 	}
 
 	//	private void jPanel3MouseDragged(java.awt.event.MouseEvent evt) {
 	//		if (isDragged) {
 	//			loc = new Point(
 	//					MainFrame.this.getLocation().x + evt.getX() - tmp.x,
 	//					MainFrame.this.getLocation().y + evt.getY() - tmp.y);
 	//			MainFrame.this.setLocation(loc);
 	//		}
 	//	}
 	//
 	//	private void jPanel3MouseReleased(java.awt.event.MouseEvent evt) {
 	//		isDragged = false;
 	//		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 	//	}
 	//
 	//	private void jPanel3MousePressed(java.awt.event.MouseEvent evt) {
 	//		tmp = new Point(evt.getX(), evt.getY());
 	//		if (this.isMaximized != 1) {
 	//			isDragged = true;
 	//			MainFrame.this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
 	//		}
 	//
 	//	}
 
 	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
 		ControllerFacade.execute("mainController", "exitApplication");
 	}
 
 	private void maxizableButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		JButton bt = (JButton) evt.getSource();
 		if (isMaximized == 1) {
 			if (this.dimension == null) {
 				this.setSize(new java.awt.Dimension(this.getMinimumSize()));
 			} else {
 				this.setSize(dimension);
 			}
 			if (this.point != null) {
 				this.setLocation(point);
 			}
 			bt.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 					"/com/lorent/lvmc/resource/images/framemaximiza.png")));
 			this.repaint();
 			isMaximized = 0;
 		} else {
 			this.setPoint(this.getLocation());
 			this.setDimension(this.getSize());
 			setMaximized();
 			bt.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 					"/com/lorent/lvmc/resource/images/frameresize.png")));
 		}
 	}
 
 	private void minizableButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		this.setExtendedState(ICONIFIED);
 	}
 
 	private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
 		ControllerFacade.execute("mainController", "exitApplication");
 	}//GEN-LAST:event_formWindowClosing
 
 	private void shareDesktopButtonActionPerformed(
 			java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shareDesktopButtonActionPerformed
 		//		if (!shareDesktopFlag) {
 		//			ControllerFacade.execute("shareDesktopController",
 		//					"startScreenShare");
 		//			shareDesktopButton
 		//					.setText(StringUtil
 		//							.getUIString("DockingLayoutMeetingPanel.stopScreenShareMI.text"));
 		//			shareDesktopFlag = true;
 		//		} else {
 		//			ControllerFacade.execute("shareDesktopController",
 		//					"stopScreenShare");
 		//			shareDesktopButton
 		//					.setText(StringUtil
 		//							.getUIString("DockingLayoutMeetingPanel.screenShareMenu.text"));
 		//			shareDesktopFlag = false;
 		//		}
 		ControllerFacade.execute("shareDesktopController",
 				"screenShareButtonClick");
 	}//GEN-LAST:event_shareDesktopButtonActionPerformed
 
 	private void saveLayouttButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveLayouttButtonActionPerformed
 		ControllerFacade.execute("layoutController", "saveLayoutByButton",
 				getDockingLayoutMeetingPanel().getRootWindow());
 	}//GEN-LAST:event_saveLayouttButtonActionPerformed
 
 	private void themeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_themeButtonActionPerformed
 		BasicPopupMenu popupMenu = new LookAndFeelPopupMenu(themeButton);
 		popupMenu.showPopup();
 	}//GEN-LAST:event_themeButtonActionPerformed
 
 	private void showThemeDialog() {
 		try {
 			ThemeDialog themeDialog = (ThemeDialog) ViewManager.getComponent(
 					ThemeDialog.class, new Class[] { java.awt.Frame.class,
 							boolean.class }, new Object[] { this, false });
 			themeButton.getX();
 			themeButton.getY();
 			themeDialog.setLocation(themeButton.getX(), themeButton.getY() + 2
 					* themeButton.getHeight() + 3);
 			themeDialog.setVisible(true);
 		} catch (Exception ex) {
 			log.error("showThemeDialog", ex);
 		}
 	}
 
 	private void showPopup(BasicPopupMenu popupMenu) {
 		popupMenu.showPopup();
 	}
 
 	private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
 		//		backgroundPanel.setBounds(0, 0, this.getWidth(), this.getHeight());
 	}//GEN-LAST:event_formComponentResized
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String args[]) {
 
 		/* Create and display the form */
 		java.awt.EventQueue.invokeLater(new Runnable() {
 
 			public void run() {
 				new MainFrame().setVisible(true);
 			}
 		});
 	}
 
 	//GEN-BEGIN:variables
 	// Variables declaration - do not modify
 	private javax.swing.JButton jButton3;
 	private javax.swing.JPanel jPanel1;
 	private javax.swing.JPanel jPanel2;
 	private javax.swing.JPanel jPanel3;
 	private javax.swing.JPanel jPanel4;
 	private javax.swing.JPanel jPanel5;
 	private javax.swing.JToolBar jToolBar1;
 	private javax.swing.JLabel logoLabel;
 	private javax.swing.JPanel logoPanel;
 	private javax.swing.JButton maxizableButton;
 	private javax.swing.JButton minizableButton;
 	private javax.swing.JButton saveLayouttButton;
 	private javax.swing.JButton screenShotButton;
 	private javax.swing.JButton selectLayoutButton;
 	private javax.swing.JButton setupButton;
 	private javax.swing.JButton shareDesktopButton;
 	private javax.swing.JButton themeButton;
 	private javax.swing.JLabel titleLabel;
 	private javax.swing.JButton windowButton;
 
 	// End of variables declaration//GEN-END:variables
 
 	public javax.swing.JPanel getTitlePanel() {
 		return jPanel3;
 	}
 
 	public javax.swing.JLabel getTitleLabel() {
 		return titleLabel;
 	}
 
 	public static MainFrame getInstance() {
 		return instance;
 	}
 
 	//	public javax.swing.JButton getMetalStyleButton() {
 	//		return metalStyleButton;
 	//	}
 	//
 	//	public void setMetalStyleButton(javax.swing.JButton metalStyleButton) {
 	//		this.metalStyleButton = metalStyleButton;
 	//	}
 	//
 	//	public javax.swing.JButton getWindowStyleButton() {
 	//		return windowStyleButton;
 	//	}
 
 	public boolean isShareDesktopFlag() {
 		return shareDesktopFlag;
 	}
 
 	public void setShareDesktopFlag(boolean shareDesktopFlag) {
 		this.shareDesktopFlag = shareDesktopFlag;
 	}
 
 	//	public javax.swing.JButton getCDEMotifButton() {
 	//		return CDEMotifButton;
 	//	}
 	//
 	//	public void setCDEMotifButton(javax.swing.JButton cDEMotifButton) {
 	//		CDEMotifButton = cDEMotifButton;
 	//	}
 
 	public javax.swing.JPanel getjPanel1() {
 		return jPanel1;
 	}
 
 	public void setjPanel1(javax.swing.JPanel jPanel1) {
 		this.jPanel1 = jPanel1;
 	}
 
 	public javax.swing.JButton getSaveLayouttButton() {
 		return saveLayouttButton;
 	}
 
 	public void setSaveLayouttButton(javax.swing.JButton saveLayouttButton) {
 		this.saveLayouttButton = saveLayouttButton;
 	}
 
 	public javax.swing.JButton getShareDesktopButton() {
 		return shareDesktopButton;
 	}
 
 	public void setShareDesktopButton(javax.swing.JButton shareDesktopButton) {
 		this.shareDesktopButton = shareDesktopButton;
 	}
 
 	public javax.swing.JButton getThemeButton() {
 		return themeButton;
 	}
 
 	public void setThemeButton(javax.swing.JButton themeButton) {
 		this.themeButton = themeButton;
 	}
 
 	public void setDockingLayoutMeetingPanel(
 			DockingLayoutMeetingPanel dockingLayoutMeetingPanel) {
 		this.dockingLayoutMeetingPanel = dockingLayoutMeetingPanel;
 	}
 
 	public static void setInstance(MainFrame instance) {
 		MainFrame.instance = instance;
 	}
 
 	//	public void setWindowStyleButton(javax.swing.JButton windowStyleButton) {
 	//		this.windowStyleButton = windowStyleButton;
 	//	}
 
 	Point loc = null;
 	Point tmp = null;
 	boolean isDragged = false;
 
 	//	private void setDragable() {
 	//		this.addMouseListener(new java.awt.event.MouseAdapter() {
 	//			public void mouseReleased(java.awt.event.MouseEvent e) {
 	//				isDragged = false;
 	//				MainFrame.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 	//			}
 	//
 	//			public void mousePressed(java.awt.event.MouseEvent e) {
 	//				tmp = new Point(e.getX(), e.getY());
 	//				isDragged = true;
 	//				MainFrame.this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
 	//			}
 	//		});
 	//		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
 	//			public void mouseDragged(java.awt.event.MouseEvent e) {
 	//				if (isDragged) {
 	//					loc = new Point(MainFrame.this.getLocation().x + e.getX()
 	//							- tmp.x, MainFrame.this.getLocation().y + e.getY()
 	//							- tmp.y);
 	//					MainFrame.this.setLocation(loc);
 	//				}
 	//			}
 	//		});
 	//	}
 
 }
