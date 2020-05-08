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
 package haushalt.gui.action;
 
 import haushalt.gui.Haushalt;
 
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.logging.Logger;
 
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
 
 public class StandardAction extends AbstractAction {
 
 	private static final long serialVersionUID = -7046231489315578362L;
 	private static final boolean DEBUG = false;
 
 	private final Logger logger = Logger.getLogger(StandardAction.class.getName());
 
 	private final String name;
 	private final ImageIcon bigIcon;
 	private final Haushalt haushalt;
 
 	public StandardAction(
 		final Haushalt haushalt,
 		final String name,
 		String localizedName,
 		final String bigIcon,
 		final String shortDescription,
 		final Integer mnemonicKey) {
 		this.name = name;
 		localizedName = localizedName == null || "".equals(localizedName.trim()) ? name : localizedName;
 		putValue(NAME, localizedName);
 		this.bigIcon = createBigIcon(bigIcon);
 		putValue(SMALL_ICON, createSmallIcon(bigIcon));
 		this.haushalt = haushalt;
 
 		putValue(SHORT_DESCRIPTION, shortDescription);
 		if (mnemonicKey != null) {
 			putValue(MNEMONIC_KEY, mnemonicKey);
 			final int code = mnemonicKey.intValue();
 			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(code, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		}
 
 	}
 
 	private ImageIcon createSmallIcon(Object iconname) {
 		if (iconname == null) {
 			iconname = "Leer";
 		}
 		final URLClassLoader urlLoader = (URLClassLoader) getClass().getClassLoader();
 		final URL imageURL = urlLoader.findResource("res/" + iconname + "16.png");
 		if (DEBUG) {
 			logger.info("ActionHandler: Erzeuge Image " + iconname + "@" + imageURL);
 		}
 		return new ImageIcon(imageURL);
 	}
 
 	private ImageIcon createBigIcon(final Object iconname) {
 		if (iconname == null) {
 			return null;
 		}
 		final URLClassLoader urlLoader = (URLClassLoader) getClass().getClassLoader();
 		final URL imageURL = urlLoader.findResource("res/" + iconname + "24.png");
 		if (DEBUG) {
 			logger.info("ActionHandler: Erzeuge Image " + iconname + "@" + imageURL);
 		}
 		return new ImageIcon(imageURL);
 	}
 
 	public ImageIcon getBigIcon() {
 		return bigIcon;
 	}
 
	@Override
 	public void actionPerformed(final ActionEvent arg0) {
 		Method call;
 
 		try {
 			logger.warning("Doing a reflection call on method " + name + " of 'Haushalt'");
 			call = Haushalt.class.getMethod(name, (Class[]) null);
 			call.invoke(haushalt, (Object[]) null);
 		} catch (final SecurityException e) {
 			logger.warning("SecurityException wenn calling actionPerformed of " + name);
 			logger.warning(e.getMessage());
 		} catch (final NoSuchMethodException e) {
 			logger.warning("NoSuchMethodException wenn calling actionPerformed of " + name);
 			logger.warning(e.getMessage());
 		} catch (final IllegalArgumentException e) {
 			logger.warning("IllegalArgumentException wenn calling actionPerformed of " + name);
 			logger.warning(e.getMessage());
 		} catch (final IllegalAccessException e) {
 			logger.warning("IllegalAccessException wenn calling actionPerformed of " + name);
 			logger.warning(e.getMessage());
 		} catch (final InvocationTargetException e) {
 			logger.warning("InvocationTargetException wenn calling actionPerformed of " + name);
 			logger.warning(e.getMessage());
 		}
 
 	}
 
 }
