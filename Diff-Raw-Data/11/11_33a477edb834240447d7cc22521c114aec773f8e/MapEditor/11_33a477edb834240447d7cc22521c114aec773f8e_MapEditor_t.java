 package tools.mapeditor;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.io.*;
 
 import common.gui.*;
 
 /*
  * ' ', '.'		Empty spaces
  * '+'			Walls
  * 's', 'S'		Spawn points
  * 'f', 'F'		Flags
  * Notes:
  *   Spawn points:
  *     First team spawns on 's' and vice versa, unless there's
  *     only one case in use, in which case anybody can spawn on any spawn point
  */
 
 /**
  * This is an interactive, graphical map editor.
  * Maybe this could be integrated into the game itself?
  * @author dvanhumb
  */
 public class MapEditor implements ActionListener, WindowListener
 {
 	private JFrame window;
 	private EditableMap map;
 	private MapView mapView;
 	// Map-menu items:
 	private JMenuItem menuNew, menuOpen, menuSave, menuSaveAs, menuQuit;
 	// Edit-menu items:
 	private JMenuItem menuCopy, menuPaste;
 	
 	/**
 	 * Create the map editor window.
 	 * Load a map with loadMap(String), loadMap(File), setMap(GameMap), or setMap(String)
 	 * Show it with show()
 	 */
 	public MapEditor()
 	{
 		map = null;
 		
 		window = new JFrame("Sphereority 2 - Map editor");
 		window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		window.addWindowListener(this);
 		
 		// Editing part of the window
 		mapView = new MapView();
 		JScrollPane scroller = new JScrollPane(mapView);
 		scroller.setPreferredSize(new Dimension(320, 240));
 		window.getContentPane().add(scroller, BorderLayout.CENTER);
 		
 		// Menus:
 		JMenuBar menuBar = new JMenuBar();
 		window.setJMenuBar(menuBar);
 		
 		// Map menu:
 		JMenu mapMenu = new JMenu("Map");
 		mapMenu.setMnemonic(KeyEvent.VK_M);
 		menuBar.add(mapMenu);
 		
 		menuNew = createMenuItem("New", KeyEvent.VK_N);
 		menuOpen = createMenuItem("Open...", KeyEvent.VK_O);
 		menuSave = createMenuItem("Save", KeyEvent.VK_S);
 		menuSaveAs = createMenuItem("Save as...", KeyEvent.VK_A);
 		menuSaveAs.setDisplayedMnemonicIndex(5);
 		menuQuit = createMenuItem("Quit", KeyEvent.VK_Q);
 		
 		mapMenu.add(menuNew);
 		mapMenu.addSeparator();
 		mapMenu.add(menuOpen);
 		mapMenu.add(menuSave);
 		mapMenu.add(menuSaveAs);
 		mapMenu.addSeparator();
 		mapMenu.add(menuQuit);
 		
 		menuCopy = createMenuItem("Copy", KeyEvent.VK_C);
 		menuPaste = createMenuItem("Paste", KeyEvent.VK_P);
 		menuPaste.setEnabled(false);
 		
 		JMenu editMenu = new JMenu("Edit");
 		editMenu.setMnemonic(KeyEvent.VK_E);
 		menuBar.add(editMenu);
 		
 		editMenu.add(menuCopy);
 		editMenu.add(menuPaste);
 		
 		window.pack();
 		window.setLocationByPlatform(true);
 		mapView.requestFocusInWindow();
 	}
 	
 	private JMenuItem createMenuItem(String label, int mnemonic)
 	{
 		JMenuItem item = new JMenuItem(label, mnemonic);
 		item.addActionListener(this);
 		
 		return item;
 	}
 	
 	public void show()
 	{
 		window.setVisible(true);
 	}
 	
 	public void hide()
 	{
 		window.setVisible(false);
 	}
 	
 	public void setVisible(boolean visible)
 	{
 		window.setVisible(visible);
 	}
 	
 	public boolean isVisible()
 	{
 		return window.isVisible();
 	}
 	
 	/**
 	 * Load a map based on the map name
 	 * @param name  The name of the map to load
 	 */
 	public void loadMap(String name)
 	{
 		try
 		{
 			map = new EditableMap(name);
 			mapView.setMap(map);
 		}
 		catch (FileNotFoundException er)
 		{
 			er.printStackTrace();
 		}
 		catch (IOException er)
 		{
 			er.printStackTrace();
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{
 		Object source = e.getSource();
 		
 		if (source.equals(menuNew))
 		{
 			EditableMap m = NewMapDialog.createMap(window);
 			if (m != null)
 			{
 				map = m;
 				mapView.setMap(map);
 			}
 		}
 		else if (source.equals(menuOpen))
 		{
 			String mapName = MapChooser.chooseMap(window);
 			if (mapName != null)
 			{
 				try
 				{
 					map = new EditableMap(mapName);
 					mapView.setMap(map);
 				}
 				catch (FileNotFoundException er)
 				{
 					er.printStackTrace();
 				}
 				catch (IOException er)
 				{
 					er.printStackTrace();
 				}
 			}
 		}
 		else if (source.equals(menuSave))
 		{
 			if (map == null)
 				return;
 			try
 			{
 				map.save();
 			}
 			catch (IOException er)
 			{
 				JOptionPane.showMessageDialog(window, String.format("Error saving map named %s.\nError was:\n%s", map.getName(), er.toString()), "Sphereority 2 Map Editor", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		else if (source.equals(menuSaveAs))
 		{
 			if (map == null)
 				return;
 		}
 		else if (source.equals(menuQuit))
 		{
 			tryQuit();
 		}
 		else if (source.equals(menuCopy))
 		{
 			mapView.copy();
 		}
 		else if (source.equals(menuPaste))
 		{
 			mapView.paste();
 		}
 	}
 	
 	public void tryQuit()
 	{
 		if (map != null && map.isDirty())
 		{
 			int result = JOptionPane.showOptionDialog(window, "You're about to close the window with an unsaved map,\nwould you like it save it before you quit?", "Sphereority 2 - Map Editor", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
 			if (result == JOptionPane.YES_OPTION)
 			{
 				try
 				{
 					map.save();
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 			else if (result == JOptionPane.CANCEL_OPTION)
 				return;
 		}
 		System.exit(0);
 	}
 	
 	// Called when the user wants to close the window
 	public void windowClosing(WindowEvent e)
 	{
 		tryQuit();
 	}
 	
 	public void windowClosed(WindowEvent e) { }
 	public void windowActivated(WindowEvent e) { }
 	public void windowDeactivated(WindowEvent e) { }
 	public void windowDeiconified(WindowEvent e) { }
 	public void windowIconified(WindowEvent e) { }
 	public void windowOpened(WindowEvent e) { }
 	
 	public static void main(String[] args)
 	{
		if (args.length == 0)
 		{
 			MapEditor me = new MapEditor();
 			me.show();
 		}
		else if (args.length == 1)
 		{
 			MapEditor me = new MapEditor();
			me.loadMap(args[0]);
 			me.show();
 		}
		else // If we have more than one command-line parameter
 		{
 			// TODO: Figure out how to show multiple windows without having them use the same painting thread
 			System.out.println("Loading multiple map files from command-line not yet possible.");
 		}
 	}
 }
