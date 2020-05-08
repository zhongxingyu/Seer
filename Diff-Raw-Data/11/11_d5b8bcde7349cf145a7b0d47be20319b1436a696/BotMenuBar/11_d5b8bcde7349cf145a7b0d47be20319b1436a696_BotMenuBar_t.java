 package org.rsbot.gui;
 
 import org.rsbot.Configuration;
 import org.rsbot.Configuration.OperatingSystem;
 import org.rsbot.bot.Bot;
 import org.rsbot.event.impl.*;
 import org.rsbot.event.listeners.PaintListener;
 import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.UpdateChecker;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.util.Map.Entry;
 
 public class BotMenuBar extends JMenuBar {
 	private static final long serialVersionUID = 971579975301998332L;
 	public static final Map<String, Class<?>> DEBUG_MAP = new LinkedHashMap<String, Class<?>>();
 	public static final String[] TITLES;
 	public static final String[][] ELEMENTS;
 
 	private static final String[] DEVELOPER_CHECK_FEATURES = {"Game State", "Current Tab", "Camera", "Floor Height",
 			"Mouse Position", "User Input Allowed", "Menu", "Menu Actions", "Cache", "Models", "Calc Test", "Settings"};
 
 	static {
 		// Text
 		DEBUG_MAP.put("Game State", TLoginIndex.class);
 		DEBUG_MAP.put("Current Tab", TTab.class);
 		DEBUG_MAP.put("Camera", TCamera.class);
 		DEBUG_MAP.put("Animation", TAnimation.class);
 		DEBUG_MAP.put("Floor Height", TFloorHeight.class);
 		DEBUG_MAP.put("Player Position", TPlayerPosition.class);
 		DEBUG_MAP.put("Mouse Position", TMousePosition.class);
 		DEBUG_MAP.put("User Input Allowed", TUserInputAllowed.class);
 		DEBUG_MAP.put("Menu Actions", TMenuActions.class);
 		DEBUG_MAP.put("Menu", TMenu.class);
 		DEBUG_MAP.put("FPS", TFPS.class);
 		DEBUG_MAP.put("Cache", TWebStatus.class);
 
 		// Paint
 		DEBUG_MAP.put("Players", DrawPlayers.class);
 		DEBUG_MAP.put("NPCs", DrawNPCs.class);
 		DEBUG_MAP.put("Objects", DrawObjects.class);
 		DEBUG_MAP.put("Models", DrawModel.class);
 		DEBUG_MAP.put("Mouse", DrawMouse.class);
 		DEBUG_MAP.put("Inventory", DrawInventory.class);
 		DEBUG_MAP.put("Ground Items", DrawItems.class);
 		DEBUG_MAP.put("Calc Test", DrawBoundaries.class);
 		DEBUG_MAP.put("Settings", DrawSettings.class);
 		DEBUG_MAP.put("Web", DrawWeb.class);
 
 		// Other
 		DEBUG_MAP.put("Log Messages", MessageLogger.class);
 
 		TITLES = new String[]{Messages.FILE, Messages.EDIT, Messages.VIEW, Messages.HELP};
 		ELEMENTS = new String[][]{
 				{Messages.NEWBOT, Messages.CLOSEBOT, Messages.MENUSEPERATOR,
 						Messages.SERVICEKEY, Messages.ADDSCRIPT, Messages.RUNSCRIPT, Messages.STOPSCRIPT, Messages.PAUSESCRIPT, Messages.MENUSEPERATOR,
 						Messages.SAVESCREENSHOT, Messages.MENUSEPERATOR,
 						Messages.HIDEBOT, Messages.EXIT},
 				{"Accounts", Messages.MENUSEPERATOR,
 						"ToggleF Force Input", Messages.TOGGLEFALSE + " " + Messages.LESSCPU, Messages.MENUSEPERATOR,
 						"ToggleF Disable Anti-Randoms", "ToggleF Disable Auto Login", Messages.MENUSEPERATOR,
 						"ToggleF Disable Advertisements", "ToggleF Disable Monitoring", "ToggleF Disable Confirmations", Messages.MENUSEPERATOR, Messages.TOGGLEFALSE + " " + Messages.AUTOSHUTDOWN}, constructDebugs(),
 				{"Site", "Project", "About", Messages.DEVUPDATE}};
 	}
 
 	private static String[] constructDebugs() {
 		final List<String> debugItems = new ArrayList<String>();
 		debugItems.add("Hide Toolbar");
 		debugItems.add("Hide Log Window");
 		debugItems.add("All Debugging");
 		debugItems.add(Messages.MENUSEPERATOR);
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (PaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		debugItems.add(Messages.MENUSEPERATOR);
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (TextPaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		debugItems.add(Messages.MENUSEPERATOR);
 		for (final String key : DEBUG_MAP.keySet()) {
 			final Class<?> el = DEBUG_MAP.get(key);
 			if (!TextPaintListener.class.isAssignableFrom(el) && !PaintListener.class.isAssignableFrom(el)) {
 				debugItems.add(key);
 			}
 		}
 		for (final ListIterator<String> it = debugItems.listIterator(); it.hasNext();) {
 			final String s = it.next();
 			if (!s.equals(Messages.MENUSEPERATOR)) {
 				it.set("ToggleF " + s);
 			}
 		}
 		return debugItems.toArray(new String[debugItems.size()]);
 	}
 
 	private void constructItemIcons() {
 		final HashMap<String, String> map = new HashMap<String, String>(16);
 		map.put(Messages.NEWBOT, Configuration.Paths.Resources.ICON_APPADD);
 		map.put(Messages.CLOSEBOT, Configuration.Paths.Resources.ICON_APPDELETE);
 		map.put(Messages.SERVICEKEY, Configuration.Paths.Resources.ICON_KEY);
 		map.put(Messages.ADDSCRIPT, Configuration.Paths.Resources.ICON_SCRIPT_ADD);
 		map.put(Messages.RUNSCRIPT, Configuration.Paths.Resources.ICON_PLAY);
 		map.put(Messages.STOPSCRIPT, Configuration.Paths.Resources.ICON_DELETE);
 		map.put(Messages.PAUSESCRIPT, Configuration.Paths.Resources.ICON_PAUSE);
 		map.put(Messages.SAVESCREENSHOT, Configuration.Paths.Resources.ICON_PHOTO);
 		map.put(Messages.HIDEBOT, Configuration.Paths.Resources.ICON_ARROWIN);
 		map.put(Messages.EXIT, Configuration.Paths.Resources.ICON_CLOSE);
 		map.put("Accounts", Configuration.Paths.Resources.ICON_REPORTKEY);
 		map.put("Site", Configuration.Paths.Resources.ICON_WEBLINK);
 		map.put("Project", Configuration.Paths.Resources.ICON_USEREDIT);
 		map.put("About", Configuration.Paths.Resources.ICON_INFO);
 		map.put(Messages.DEVUPDATE, Configuration.Paths.Resources.ICON_APPADD);
 		for (final Entry<String, String> item : map.entrySet()) {
 			final JMenuItem menu = commandMenuItem.get(item.getKey());
 			menu.setIcon(new ImageIcon(Configuration.getImage(item.getValue())));
 		}
 	}
 
 	private final Map<String, JCheckBoxMenuItem> eventCheckMap = new HashMap<String, JCheckBoxMenuItem>();
 	private final Map<String, JCheckBoxMenuItem> commandCheckMap = new HashMap<String, JCheckBoxMenuItem>();
 	private final Map<String, JMenuItem> commandMenuItem = new HashMap<String, JMenuItem>();
 	private final ActionListener listener;
 
 	public BotMenuBar(final ActionListener listener) {
 		this.listener = listener;
 		for (int i = 0; i < TITLES.length; i++) {
 			final String title = TITLES[i];
 			final String[] elems = ELEMENTS[i];
 			add(constructMenu(title, elems));
 		}
 		constructItemIcons();
 		commandMenuItem.get(Messages.SERVICEKEY).setVisible(false);
 		commandCheckMap.get("Disable Monitoring").setVisible(false);
 		commandMenuItem.get(Messages.HIDEBOT).setVisible(SystemTray.isSupported());
		commandMenuItem.get(Messages.DEVUPDATE).setVisible(new File(Configuration.Paths.ROOT, ".git").exists() && UpdateChecker.findGit() != null);
 		commandCheckMap.get(Messages.AUTOSHUTDOWN).setVisible(Configuration.getCurrentOperatingSystem() == OperatingSystem.WINDOWS);
 		if (Configuration.RUNNING_FROM_JAR) {
 			for (String disableFeature : DEVELOPER_CHECK_FEATURES) {
 				if (commandCheckMap.containsKey(disableFeature)) {
 					commandCheckMap.get(disableFeature).setVisible(false);
 				}
 			}
 			// hide auto-shutdown for release builds
 			commandCheckMap.get(Messages.AUTOSHUTDOWN).setVisible(false);
 		}
 	}
 
 	public void setOverrideInput(final boolean force) {
 		commandCheckMap.get("Force Input").setSelected(force);
 	}
 
 	public void setPauseScript(final boolean pause) {
 		final JMenuItem item = commandMenuItem.get(Messages.PAUSESCRIPT);
 		item.setText(pause ? "Resume Script" : Messages.PAUSESCRIPT);
 		final Image image = Configuration.getImage(pause ? Configuration.Paths.Resources.ICON_START : Configuration.Paths.Resources.ICON_PAUSE);
 		if (image != null) {
 			item.setIcon(new ImageIcon(image));
 		}
 	}
 
 	public JMenuItem getMenuItem(final String name) {
 		return commandMenuItem.get(name);
 	}
 
 	public void setBot(final Bot bot) {
 		if (bot == null) {
 			commandMenuItem.get(Messages.CLOSEBOT).setEnabled(false);
 			commandMenuItem.get(Messages.RUNSCRIPT).setEnabled(false);
 			commandMenuItem.get(Messages.STOPSCRIPT).setEnabled(false);
 			commandMenuItem.get(Messages.PAUSESCRIPT).setEnabled(false);
 			commandMenuItem.get(Messages.SAVESCREENSHOT).setEnabled(false);
 			for (final JCheckBoxMenuItem item : eventCheckMap.values()) {
 				item.setSelected(false);
 				item.setEnabled(false);
 			}
 			disable("All Debugging", "Force Input", Messages.LESSCPU, "Disable Anti-Randoms", "Disable Auto Login");
 		} else {
 			commandMenuItem.get(Messages.CLOSEBOT).setEnabled(true);
 			commandMenuItem.get(Messages.RUNSCRIPT).setEnabled(true);
 			commandMenuItem.get(Messages.STOPSCRIPT).setEnabled(true);
 			commandMenuItem.get(Messages.PAUSESCRIPT).setEnabled(true);
 			commandMenuItem.get(Messages.SAVESCREENSHOT).setEnabled(true);
 			int selections = 0;
 			for (final Map.Entry<String, JCheckBoxMenuItem> entry : eventCheckMap.entrySet()) {
 				entry.getValue().setEnabled(true);
 				final boolean selected = bot.hasListener(DEBUG_MAP.get(entry.getKey()));
 				entry.getValue().setSelected(selected);
 				if (selected) {
 					++selections;
 				}
 			}
 			enable("All Debugging", selections == eventCheckMap.size());
 			enable("Force Input", bot.overrideInput);
 			enable(Messages.LESSCPU, bot.disableRendering);
 			enable("Disable Anti-Randoms", bot.disableRandoms);
 			enable("Disable Auto Login", bot.disableAutoLogin);
 		}
 	}
 
 	public JCheckBoxMenuItem getCheckBox(final String key) {
 		return commandCheckMap.get(key);
 	}
 
 	private void disable(final String... items) {
 		for (final String item : items) {
 			commandCheckMap.get(item).setSelected(false);
 			commandCheckMap.get(item).setEnabled(false);
 		}
 	}
 
 	public void enable(final String item, final boolean selected) {
 		commandCheckMap.get(item).setSelected(selected);
 		commandCheckMap.get(item).setEnabled(true);
 	}
 
 	public void doClick(final String item) {
 		commandMenuItem.get(item).doClick();
 	}
 
 	public void doTick(final String item) {
 		commandCheckMap.get(item).doClick();
 	}
 
 	public boolean isTicked(final String item) {
 		return commandCheckMap.get(item).isSelected();
 	}
 
 	public void loadPrefs() {
 		final String path = Configuration.Paths.getMenuBarPrefs();
 		if (!new File(path).exists()) {
 			return;
 		}
 		FileReader freader = null;
 		BufferedReader in = null;
 		try {
 			freader = new FileReader(path);
 			in = new BufferedReader(freader);
 			String line;
 			while ((line = in.readLine()) != null) {
 				line = line.trim();
 				if (commandCheckMap.containsKey(line)) {
 					commandCheckMap.get(line).doClick();
 				}
 			}
 		} catch (final IOException ioe) {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				if (freader != null) {
 					freader.close();
 				}
 			} catch (final IOException ioe1) {
 			}
 		}
 	}
 
 	public void savePrefs() {
 		final String path = Configuration.Paths.getMenuBarPrefs();
 		FileWriter fstream = null;
 		BufferedWriter out = null;
 		try {
 			final File f = new File(path);
 			if (f.exists()) {
 				f.delete();
 			}
 			fstream = new FileWriter(path);
 			out = new BufferedWriter(fstream);
 			for (final Entry<String, JCheckBoxMenuItem> item : commandCheckMap.entrySet()) {
 				final boolean checked = item.getValue().isSelected();
 				if (!checked) {
 					continue;
 				}
 				out.write(item.getKey());
 				out.newLine();
 			}
 		} catch (final IOException ioe) {
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 				if (fstream != null) {
 					fstream.close();
 				}
 			} catch (final IOException ioe1) {
 			}
 		}
 	}
 
 	private JMenu constructMenu(final String title, final String[] elems) {
 		final JMenu menu = new JMenu(title);
 		for (String e : elems) {
 			if (e.equals(Messages.MENUSEPERATOR)) {
 				menu.add(new JSeparator());
 			} else {
 				JMenuItem jmi;
 				if (e.startsWith(Messages.TOGGLE)) {
 					e = e.substring(Messages.TOGGLE.length());
 					final char state = e.charAt(0);
 					e = e.substring(2);
 					jmi = new JCheckBoxMenuItem(e);
 					if (state == 't' || state == 'T') {
 						jmi.setSelected(true);
 					}
 					if (DEBUG_MAP.containsKey(e)) {
 						final JCheckBoxMenuItem ji = (JCheckBoxMenuItem) jmi;
 						eventCheckMap.put(e, ji);
 					}
 					final JCheckBoxMenuItem ji = (JCheckBoxMenuItem) jmi;
 					commandCheckMap.put(e, ji);
 				} else {
 					jmi = new JMenuItem(e);
 					commandMenuItem.put(e, jmi);
 				}
 				jmi.addActionListener(listener);
 				jmi.setActionCommand(title + "." + e);
 				menu.add(jmi);
 			}
 		}
 		return menu;
 	}
 }
