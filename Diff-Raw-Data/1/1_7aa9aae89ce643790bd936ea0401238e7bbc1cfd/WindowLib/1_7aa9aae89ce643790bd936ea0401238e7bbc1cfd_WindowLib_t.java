 package com.jpii.navalbattle.util;
 
 import java.awt.DisplayMode;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 public class WindowLib {
 	JFrame wnd;
 	int sizew, sizeh,ox,oy;
 	boolean ready = false;
 	Timer evilHackTimer;
 	public WindowLib(JFrame wnd) {
 		this.wnd = wnd;
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				update();
 			}
 		};
 		evilHackTimer = new Timer(75,al);
 		evilHackTimer.start();
 	}
 	private void update() {
 		if (wnd != null) {
 			if (ready) {
 				wnd.toFront();
 			}
 			else {
 				evilHackTimer.stop();
 			}
 		}
 		else {
 			evilHackTimer.stop();
 		}
 	}
 	public boolean showFullscreen() {
 		//evilHackTimer.start();
 		if (FileUtils.getPlatform() == OS.windows) {
 			ready = true;
 			if (wnd == null)
 				return false;
 			int clientWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
 			int clientHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
 			ox = wnd.getLocation().x;
 			oy = wnd.getLocation().y;
 			sizew = wnd.getWidth();
 			sizeh = wnd.getHeight();
 			wnd.dispose();
 			wnd.setUndecorated(true);
 			wnd.setSize(clientWidth, clientHeight);
 			wnd.setVisible(true);
 			wnd.setLocation(new Point(0,0));
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
 		wnd.setLocation(ox,oy);
 		wnd.toFront();
 		ready = false;
 		evilHackTimer.stop();
 	}
 }
