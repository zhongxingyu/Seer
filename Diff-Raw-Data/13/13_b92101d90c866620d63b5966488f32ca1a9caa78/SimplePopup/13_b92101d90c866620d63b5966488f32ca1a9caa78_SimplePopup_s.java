 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.gui;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.apache.log4j.Logger;
 import org.gnome.gtk.Gtk;
 import org.gnome.notify.Notification;
 import org.gnome.notify.Notify;
 import org.gnome.notify.Urgency;
 
 public class SimplePopup {
 
 	private static Logger log = Logger.getLogger(SimplePopup.class);
 
 	private String message;
 	private int messageType = JOptionPane.INFORMATION_MESSAGE;
 
 	/**
 	 * SimplePopup
 	 * 
 	 * @param message
 	 */
 	public SimplePopup(String message) {
 
 		// Cut down message so the screen does get full :)
 		if (message.length() > 700) {
 			message = message.substring(0, 700);
 			message += "\n  (... and more...)";
 		}
 
 		this.message = message;
 	}
 
 	/**
 	 * setMessageTypeError
 	 */
 	public void setMessageTypeError() {
 		messageType = JOptionPane.ERROR_MESSAGE;
 	}
 
 	/**
 	 * setMessageTypeWarn
 	 */
 	public void setMessageTypeWarn() {
 		messageType = JOptionPane.WARNING_MESSAGE;
 	}
 
 	/**
 	 * Show a simple popup dialog
 	 * 
 	 * @param message
 	 */
 	public final void show() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			log.warn("Cannot change GUI to SystemLookAndFeel", e);
 		} catch (InstantiationException e) {
 			log.warn("Cannot Instantiate GUI to SystemLookAndFeel", e);
 		} catch (IllegalAccessException e) {
 			log.warn("Cannot access SystemLookAndFeel GUI", e);
 		} catch (UnsupportedLookAndFeelException e) {
 			log.warn("SystemLookAndFeel GUI is not supported", e);
 		}
 		JOptionPane.showMessageDialog(new JFrame(), message, "Meerkat Monitor",
 				messageType);
 	}
 
 	/**
 	 * Show a simple popup dialog
 	 * 
 	 * @param message
 	 */
 	public final void showMsg() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			log.warn("Cannot change GUI to SystemLookAndFeel", e);
 		} catch (InstantiationException e) {
 			log.warn("Cannot Instantiate GUI to SystemLookAndFeel", e);
 		} catch (IllegalAccessException e) {
 			log.warn("Cannot access WindowsLookAndFeel GUI", e);
 		} catch (UnsupportedLookAndFeelException e) {
 			log.warn("SystemLookAndFeel GUI is not supported", e);
 		}
 		JOptionPane.showMessageDialog(new JFrame(), message, "Meerkat Monitor",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public final void showGnomeNotification() {
 		Gtk.init(null);
 
 		Notification notification;
 
 		Notify.init("low-battery-example");
 		notification = new Notification("Low Battery Example",
 				"Your battery is low!", "messagebox_warning");
 
 		// Quit the application after notification disappears.
 		notification.connect(new org.gnome.notify.Notification.Closed() {
 			public void onClosed(Notification source) {
 				Notify.uninit();
 				Gtk.mainQuit();
 			}
 		});
 
 		notification.setUrgency(Urgency.NORMAL);
 
 		notification.show();
 
 	}
 
 }
