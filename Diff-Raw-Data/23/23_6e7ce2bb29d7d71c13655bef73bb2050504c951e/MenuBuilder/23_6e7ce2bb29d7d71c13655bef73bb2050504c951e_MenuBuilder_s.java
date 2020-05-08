 package org.corewall.ui.app;
 
 import java.util.List;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 import org.corewall.Platform;
 
 import com.google.inject.internal.Lists;
 
 /**
  * Builds a JMenuBar structures from the {@link MenuRegistry} contributions for
  * an application.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class MenuBuilder {
 
 	/**
 	 * Create a menu identifier.
 	 * 
 	 * @param menu
 	 *            the menu name.
 	 * @param section
 	 *            the section.
 	 * @return the identifier.
 	 */
 	public static String menu(final String menu, final String section) {
 		return "menu:" + menu.toLowerCase() + "#" + section.toLowerCase();
 	}
 
 	private final String application;
 	private JMenu currentMenu = null;
 	private String currentMenuName = null;
	private boolean lastHadEntries = false;
 	private final List<JMenu> menus;
 	private final MenuRegistry registry;
 
 	/**
 	 * Create a new MenuBuilder.
 	 * 
 	 * @param application
 	 *            the application.
 	 */
 	public MenuBuilder(final String application) {
 		this.application = application;
 		registry = Platform.getService(MenuRegistry.class);
 		menus = Lists.newArrayList();
 	}
 
 	/**
 	 * Populate the specified JMenuBar with the built menu.
 	 * 
 	 * @param menubar
 	 *            the menu bar.
 	 * @return the menu bar.
 	 */
 	public JMenuBar build(final JMenuBar menubar) {
 		for (JMenu m : menus) {
 			menubar.add(m);
 		}
 		return menubar;
 	}
 
 	/**
 	 * Create a new top-level menu.
 	 * 
 	 * @param name
 	 *            the name.
 	 * @return the menu builder instance as a convenience.
 	 */
 	public MenuBuilder menu(final String name) {
 		currentMenuName = name;
 		currentMenu = new JMenu(name);
 		menus.add(currentMenu);
 		return this;
 	}
 
 	/**
 	 * Create a new menu section.
 	 * 
 	 * @param name
 	 *            the name.
 	 * @return the menu builder instance as a convenience.
 	 */
 	public MenuBuilder section(final String name) {
 		String id = menu(currentMenuName, name);
 		for (MenuContribution c : registry.getMenuContributions(application)) {
 			if (c.getMenu().equalsIgnoreCase(id)) {
 				currentMenu.add(new JMenuItem(c.getAction()));
				lastHadEntries = true;
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Create a new menu separator.
 	 * 
 	 * @return the menu builder instance as a convenience.
 	 */
 	public MenuBuilder separator() {
		if (lastHadEntries) {
			currentMenu.addSeparator();
		}
		lastHadEntries = false;
 		return this;
 	}
 }
