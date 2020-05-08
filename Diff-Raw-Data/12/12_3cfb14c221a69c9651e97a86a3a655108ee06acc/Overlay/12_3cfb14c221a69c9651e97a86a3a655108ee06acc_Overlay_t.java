 package com.punk.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.PointerInfo;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 
 import com.punk.model.Border;
 import com.punk.model.Capturepoint;
 import com.punk.model.CapturepointsUtil;
 
 /**
  * @author Sander Schulenburg aka "Much"(schulenburgsander@gmail.com)
  */
 public class Overlay extends Thread {
 
 	private JFrame overlayFrame = null;
 	private int width = 350;
 	private int height = 400;
 
 	private CapturepointsUtil capUtil = null;
 	private Border border = null;
 
 	Timer timer = null;
 
 	public Overlay(CapturepointsUtil capUtil, Border border) {
 		this.capUtil = capUtil;
 		this.border = border;
 
 		GridLayout gridLayout = new GridLayout(0, 2);
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
 		overlayFrame = new JFrame();
 		overlayFrame.setLayout(gridLayout);
 		overlayFrame.setUndecorated(true);
 		overlayFrame.setSize(0, 0);
 		overlayFrame.setLocationRelativeTo(null);
 		overlayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		overlayFrame.setAlwaysOnTop(true);
 		overlayFrame.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
 		overlayFrame.setLocation((int) screenSize.getWidth() - width,
 				(int) screenSize.getHeight() - height - 20);
 
 		overlayFrame.setVisible(true);
 
 		updateBorder();
 
 		timer = new Timer();
 		timer.schedule(new updateTimers(), 0, 1000);
 	}
 
 	private void updateBorder() {
 		for (int index = 0; index < capUtil.getCapturepoints(border).size(); index++) {
 			Capturepoint cap = capUtil.getCapturepoints(border).get(index);
 			if (cap.getServer() != Color.GRAY && cap.getRiTime() > 0) {
 				overlayFrame.add(cap.getOverlay());
 			} else {
 				cap.setRiTime(10);
 			}
 		}
 	}
 
 	public void setBorder(Border border) {
 		ArrayList<Capturepoint> capturepoints = capUtil
 				.getCapturepoints(this.border);
 		for (int index = 0; index < capturepoints.size(); index++) {
 			overlayFrame.remove(capturepoints.get(index).getOverlay());
 		}
 		if (this.border != border) {
 			this.border = border;
 			updateBorder();
 		}
 	}
 
 	private class updateTimers extends TimerTask {
 		public void run() {
 
 			for (Capturepoint cap : capUtil.getCapturepoints(Border.RED)) {
 				cap.tickRit();
 			}
 
 			for (Capturepoint cap : capUtil.getCapturepoints(Border.GREEN)) {
 				cap.tickRit();
 			}
 
 			for (Capturepoint cap : capUtil.getCapturepoints(Border.BLUE)) {
 				cap.tickRit();
 			}
 
 			for (Capturepoint cap : capUtil.getCapturepoints(Border.EB)) {
 				cap.tickRit();
 			}
 
 			for (Capturepoint cap : capUtil.getCapturepoints(border)) {
 				if (cap.getServer() != Color.GRAY && cap.getRiTime() > 0) {
 					overlayFrame.add(cap.getOverlay());
 				} else {
 					overlayFrame.remove(cap.getOverlay());
 				}
 			}
 			overlayFrame.repaint();
 		}
 	}
 
 	public void run() {
 		while (overlayFrame.isVisible()) {
 
 			PointerInfo a = MouseInfo.getPointerInfo();
 			Point b = a.getLocation();
 			int x = (int) b.getX();
 			int y = (int) b.getY();
 
 			boolean isMouseOnOverlay = x > overlayFrame.getLocation().x
 					&& x < overlayFrame.getLocation().x + width
 					&& y > overlayFrame.getLocation().y
 					&& y < overlayFrame.getLocation().y + height;
 
 			if (isMouseOnOverlay) {
 				overlayFrame.setSize(0, 0);
 			} else {
 				overlayFrame.setSize(width, height);
 			}
 		}
 	}
 
	public void toggleOverlay() {
		overlayFrame.setVisible(!overlayFrame.isVisible());
 	}
 }
