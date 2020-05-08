 package com.jpii.navalbattle.util;
 
 import java.awt.DisplayMode;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 
 public class WindowLib {
 	JFrame wnd;
 	int sizew, sizeh;
 	boolean ready = false;
 	public WindowLib(JFrame wnd) {
 		this.wnd = wnd;
 	}
 	public boolean showFullscreen() {
 		if (FileUtils.getPlatform() == OS.windows) {
 			ready = true;
 			if (wnd == null)
 				return false;
 			int clientWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
 			int clientHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
 			sizew = wnd.getWidth();
 			sizeh = wnd.getHeight();
 			wnd.dispose();
 			wnd.setUndecorated(true);
 			wnd.setSize(clientWidth, clientHeight);
 			wnd.setVisible(true);
 			try
 			{
 				wnd.toFront();
 				wnd.setAlwaysOnTop(true);
 			}
 			catch (Exception ex) {
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 	public void hideFullscreen() {
 		wnd.dispose();
 		wnd.setUndecorated(false);
 		wnd.setVisible(true);
 		wnd.setSize(sizew, sizeh);
 		wnd.setAlwaysOnTop(false);
 		wnd.toFront();
 	}
 }
