 package com.dunnkers.pathmaker.ui.menu;
 
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JMenu;
 import javax.swing.JRadioButtonMenuItem;
 
import com.dunnkers.pathmaker.Configuration;
 import com.dunnkers.pathmaker.util.WorldMap;
 
 public class MapMenu extends JMenu {
 
 	private static final long serialVersionUID = 1L;
 	private final ButtonGroup buttonGroup;
 	private final ArrayList<JRadioButtonMenuItem> maps;
 
 	public MapMenu(final String text) {
 		this.setText(text);
 		this.buttonGroup = new ButtonGroup();
 		maps = new ArrayList<JRadioButtonMenuItem>();
 		for (final WorldMap worldMap : WorldMap.values()) {
 			final JRadioButtonMenuItem map = new JRadioButtonMenuItem(worldMap.getName());
 			map.setEnabled(map.isEnabled());
 			map.setActionCommand(worldMap.getName());
			if (worldMap.equals(Configuration.INITIAL_WORLD_MAP)) {
				map.setSelected(true);
			}
 			this.buttonGroup.add(map);
 			maps.add(map);
 		}
 		init();
 	}
 
 	public void init() {
 		for (final JRadioButtonMenuItem map : maps) {
 			add(map);
 		}
 	}
 
 	public void addMapActionListener(final ActionListener actionListener) {
 		for (final JRadioButtonMenuItem map : maps) {
 			map.addActionListener(actionListener);
 		}
 	}
 }
