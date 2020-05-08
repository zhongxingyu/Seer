 /*
  * This file is part of jHaushalt.
  * jHaushalt is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * jHaushalt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with jHaushalt; if not, see <http://www.gnu.org/licenses/>.
  * (C)opyright 2012 Benjamin Marstaller
  */
 
 package haushalt.gui.mac;
 
 import haushalt.gui.Haushalt;
 import haushalt.gui.dialoge.DlgInfo;
 
 import java.awt.Image;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import javax.swing.ImageIcon;
 
 import com.apple.eawt.AboutHandler;
 import com.apple.eawt.AppEvent.AboutEvent;
 import com.apple.eawt.AppEvent.PreferencesEvent;
 import com.apple.eawt.Application;
 import com.apple.eawt.PreferencesHandler;
 
 /**
  * An adapter for mac-stylish look'n-feel on macs
  * 
  * 
  * @author Benjamin Marstaller
  * 
  */
 public final class MacAdapter {
 
 	private MacAdapter() {}
 
 	public static void macStyle(final Haushalt haushalt) {
 
 		final Application application = Application.getApplication();
 
 		final URLClassLoader urlLoader = (URLClassLoader) MacAdapter.class.getClassLoader();
 
 		// Apple dock
 		final URL iconLoc = urlLoader.findResource("res/jhh-icon.gif");
 		final Image dockIcon = new ImageIcon(iconLoc).getImage();
 		application.setDockIconImage(dockIcon);
 
 		// about dialog
 		application.setAboutHandler(new AboutHandler() {
 
			//@ Override
 			public void handleAbout(final AboutEvent e) {
 				final DlgInfo dlg = new DlgInfo(null);
 				dlg.pack();
 				dlg.setLocationRelativeTo(null);
 				dlg.setVisible(true);
 			}
 		});
 
 		// preferences
 		application.setPreferencesHandler(new PreferencesHandler() {
 
			//@ Override
 			public void handlePreferences(final PreferencesEvent arg0) {
 				haushalt.optionen();
 			}
 		});
 	}
 }
