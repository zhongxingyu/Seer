 package com.jpii.navalbattle.util;
 
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 public class WindowLib {
 	JFrame wnd;
 	int sizew, sizeh,ox,oy;
 	boolean ready = false;
 	boolean fullscreen = false;
 	Timer evilHackTimer;
 	int h = 0;
 	public WindowLib(JFrame wnd) {
 		this.wnd = wnd;
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				update();
 			}
 		};
 		if (wnd != null) {
 			FocusListener focus = new FocusListener() {
 				public void focusGained(FocusEvent arg0) {
 				}
 				public void focusLost(FocusEvent arg0) {
 					if (isFullscreen() && ++h >= 2)
 						hideFullscreen();
 				}
 			};
 			wnd.addFocusListener(focus);
 		}
 		evilHackTimer = new Timer(75,al);
 		//evilHackTimer.start();
 	}
 	public boolean isFullscreen() {
 		return fullscreen;
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
 			fullscreen = true;
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
 		fullscreen = false;
 		evilHackTimer.stop();
 	}
 }
