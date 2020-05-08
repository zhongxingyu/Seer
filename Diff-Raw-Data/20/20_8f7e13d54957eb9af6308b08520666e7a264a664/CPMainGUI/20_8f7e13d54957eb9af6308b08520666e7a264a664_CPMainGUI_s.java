 /*
 	ChibiPaint
     Copyright (c) 2006-2008 Marc Schefer
 
     This file is part of ChibiPaint.
 
     ChibiPaint is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ChibiPaint is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ChibiPaint. If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package chibipaint.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.util.*;
 import java.util.prefs.Preferences;
 
 import javax.swing.*;
 
 import chibipaint.*;
 
 public class CPMainGUI {
 
 	CPController controller;
 	private CPPaletteManager paletteManager;
 
 	JMenuBar menuBar;
 	JPanel mainPanel;
 	JDesktopPane jdp;
 	JPanel bg;
 
 	// FIXME: replace this hack by something better
 	Map<String, JCheckBoxMenuItem> paletteItems = new HashMap();
 
 	public CPMainGUI(CPController controller) {
 		this.controller = controller;
 		controller.setMainGUI(this);
 
 		// try {
 		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		// } catch (Exception ex2) {} */
 
 		menuBar = createMainMenu(controller);
 		createGUI();
 	}
 
 	public JComponent getGUI() {
 		return mainPanel;
 	}
 
 	public JMenuBar getMenuBar() {
 		return menuBar;
 	}
 
 	private void createGUI() {
 		mainPanel = new JPanel(new BorderLayout());
 
 		jdp = new CPDesktop();
 		setPaletteManager(new CPPaletteManager(controller, jdp));
 
 		createCanvasGUI(jdp);
 		mainPanel.add(jdp, BorderLayout.CENTER);
 
 		JPanel statusBar = new CPStatusBar(controller);
 		mainPanel.add(statusBar, BorderLayout.PAGE_END);
 
 		// jdp.addContainerListener(this);
 	}
 
 	void createCanvasGUI(JComponent c) {
 		CPCanvas canvas = new CPCanvas(controller);
 		bg = canvas.getContainer();
 
 		c.add(bg);
 		canvas.grabFocus();
 	}
 
 	public JMenuBar createMainMenu(ActionListener listener) {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu, submenu;
 		JMenuItem menuItem;
 
 		//
 		// File Menu
 		//
 		menu = new JMenu("File");
 		menu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(menu);
 
 
 		if (controller.isRunningAsApplet ()) {
 			menuItem = new JMenuItem("Send Oekaki", KeyEvent.VK_S);
 			menuItem.getAccessibleContext().setAccessibleDescription(
 					"Sends the oekaki to the server and exits ChibiPaint");
 			menuItem.setActionCommand("CPSend");
 			menuItem.addActionListener(listener);
 			menu.add(menuItem);
 		}
 
 		if (controller.isRunningAsApplication ())
 		{
 			menuItem = new JMenuItem("New File", KeyEvent.VK_N);
 			menuItem.getAccessibleContext().setAccessibleDescription(
 					"New File");
 			menuItem.setActionCommand("CPNew");
 			menuItem.addActionListener(listener);
 			menu.add(menuItem);
 
 			menu.add(new JSeparator());
 
			menuItem = new JMenuItem("Export as .png File...", KeyEvent.VK_C);
 			menuItem.getAccessibleContext().setAccessibleDescription(
 					"Save .png File");
 			menuItem.setActionCommand("CPSavePng");
 			menuItem.addActionListener(listener);
 			menu.add(menuItem);
 
 			menu.add(new JSeparator());
 
 			menuItem = new JMenuItem("Save", KeyEvent.VK_S);
 			menuItem.getAccessibleContext().setAccessibleDescription(
 					"Save File");
 			menuItem.setActionCommand("CPSave");
 			menuItem.addActionListener(listener);
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem("Save as...", KeyEvent.VK_A);
 			menuItem.getAccessibleContext().setAccessibleDescription(
 					"Save .chi File");
 			menuItem.setActionCommand("CPSaveChi");
 			menuItem.addActionListener(listener);
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem("Open...", KeyEvent.VK_L);
 			menuItem.getAccessibleContext().setAccessibleDescription(
 					"Open .chi File");
 			menuItem.setActionCommand("CPLoadChi");
 			menuItem.addActionListener(listener);
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 			menu.add(menuItem);
 
 			submenu = new JMenu("Open Recent");
 
 
 			Preferences userRoot = Preferences.userRoot();
 			Preferences preferences = userRoot.node( "chibipaintmod" );
 			int recent_file_num = 0;
 			for (int i = 0; i < 10; i++)
 			{
 				String recentFileName = preferences.get("Recent File["+ i + "]", "");
 				if (recentFileName.length() != 0)
 				{
 					File recentFile = new File (recentFileName);
 					menuItem = new JMenuItem(recentFile.getName ());
 					menuItem.getAccessibleContext().setAccessibleDescription("Open Recent File");
 					menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + (i + 1) % 10, ActionEvent.CTRL_MASK));
 					menuItem.setActionCommand("CPOpenRecent " + i);
 					menuItem.addActionListener(listener);
 					submenu.add(menuItem);
 					recent_file_num++;
 				}
 			}
 
 			if (recent_file_num > 0)
 				menu.add (submenu);
 		}
 
 		//
 		// Edit Menu
 		//
 
 		menu = new JMenu("Edit");
 		menu.setMnemonic(KeyEvent.VK_E);
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("Undo", KeyEvent.VK_U);
 		menuItem.getAccessibleContext().setAccessibleDescription("Undoes the most recent action");
 		menuItem.setActionCommand("CPUndo");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Redo", KeyEvent.VK_R);
 		menuItem.getAccessibleContext().setAccessibleDescription("Redoes a previously undone action");
 		menuItem.setActionCommand("CPRedo");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Clear History", KeyEvent.VK_H);
 		menuItem.getAccessibleContext().setAccessibleDescription("Removes all undo/redo information to regain memory");
 		menuItem.setActionCommand("CPClearHistory");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		menu.add(new JSeparator());
 
 		menuItem = new JMenuItem("Cut", KeyEvent.VK_T);
 		menuItem.getAccessibleContext().setAccessibleDescription("");
 		menuItem.setActionCommand("CPCut");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Copy", KeyEvent.VK_C);
 		menuItem.getAccessibleContext().setAccessibleDescription("");
 		menuItem.setActionCommand("CPCopy");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Copy Merged", KeyEvent.VK_Y);
 		menuItem.getAccessibleContext().setAccessibleDescription("");
 		menuItem.setActionCommand("CPCopyMerged");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Paste", KeyEvent.VK_P);
 		menuItem.getAccessibleContext().setAccessibleDescription("");
 		menuItem.setActionCommand("CPPaste");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		menu.add(new JSeparator());
 
 		menuItem = new JMenuItem("Select All", KeyEvent.VK_A);
 		menuItem.getAccessibleContext().setAccessibleDescription("Selects the whole canvas");
 		menuItem.setActionCommand("CPSelectAll");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Deselect", KeyEvent.VK_D);
 		menuItem.getAccessibleContext().setAccessibleDescription("Deselects the whole canvas");
 		menuItem.setActionCommand("CPDeselectAll");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		menu = new JMenu("Layers");
 		menu.setMnemonic(KeyEvent.VK_L);
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("Duplicate", KeyEvent.VK_D);
 		menuItem.getAccessibleContext().setAccessibleDescription("Creates a copy of the currently selected layer");
 		menuItem.setActionCommand("CPLayerDuplicate");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
 		menu.add(menuItem);
 
 		menu.add(new JSeparator());
 
 		menuItem = new JMenuItem("Merge Down", KeyEvent.VK_E);
 		menuItem.getAccessibleContext().setAccessibleDescription(
 				"Merges the currently selected layer with the one directly below it");
 		menuItem.setActionCommand("CPLayerMergeDown");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		/*
 		 * menuItem = new JMenuItem("Merge Visible", KeyEvent.VK_V);
 		 * menuItem.getAccessibleContext().setAccessibleDescription("Merges all the visible layers");
 		 * menuItem.setActionCommand("CPLayerMergeVisible"); menuItem.addActionListener(listener);
 		 * menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK |
 		 * ActionEvent.SHIFT_MASK)); menu.add(menuItem);
 		 */
 
 		menuItem = new JMenuItem("Merge All Layers", KeyEvent.VK_A);
 		menuItem.getAccessibleContext().setAccessibleDescription("Merges all the layers");
 		menuItem.setActionCommand("CPLayerMergeAll");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		//
 		// Effects Menu
 		//
 		menu = new JMenu("Effects");
 		menu.setMnemonic(KeyEvent.VK_E);
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("Clear", KeyEvent.VK_C);
 		menuItem.getAccessibleContext().setAccessibleDescription("Clears the selected area");
 		menuItem.setActionCommand("CPClear");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Fill", KeyEvent.VK_F);
 		menuItem.getAccessibleContext().setAccessibleDescription("Fills the selected area with the current color");
 		menuItem.setActionCommand("CPFill");
 		menuItem.addActionListener(listener);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Flip Horizontal", KeyEvent.VK_H);
 		menuItem.getAccessibleContext().setAccessibleDescription("Flips the current selected area horizontally");
 		menuItem.setActionCommand("CPHFlip");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Flip Vertical", KeyEvent.VK_V);
 		menuItem.getAccessibleContext().setAccessibleDescription("Flips the current selected area vertically");
 		menuItem.setActionCommand("CPVFlip");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Invert", KeyEvent.VK_I);
 		menuItem.getAccessibleContext().setAccessibleDescription("Invert the image colors");
 		menuItem.setActionCommand("CPFXInvert");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		submenu = new JMenu("Make Grayscale");
 		submenu.setMnemonic(KeyEvent.VK_G);
 
 		menuItem = new JMenuItem("By Intensity", KeyEvent.VK_I);
 		menuItem.getAccessibleContext().setAccessibleDescription("Make image grayscale by Intensity Formula");
 		menuItem.setActionCommand("CPFXMakeMonochromeByIntensity");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menuItem = new JMenuItem("By Value", KeyEvent.VK_V);
 		menuItem.getAccessibleContext().setAccessibleDescription("Make image grayscale by Value Formula");
 		menuItem.setActionCommand("CPFXMakeMonochromeByValue");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menuItem = new JMenuItem("By Lightness", KeyEvent.VK_L);
 		menuItem.getAccessibleContext().setAccessibleDescription("Make image grayscale by Lightness Formula");
 		menuItem.setActionCommand("CPFXMakeMonochromeByLightness");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menuItem = new JMenuItem("By Luma", KeyEvent.VK_U);
 		menuItem.getAccessibleContext().setAccessibleDescription("Make image grayscale by Luma Formula");
 		menuItem.setActionCommand("CPFXMakeMonochromeByLuma");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menuItem = new JMenuItem("By Selected Color", KeyEvent.VK_C);
 		menuItem.getAccessibleContext().setAccessibleDescription("Make image grayscale by Selected Color");
 		menuItem.setActionCommand("CPFXMakeMonochromeBySelColor");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menu.add(submenu);
 
 		submenu = new JMenu("Blur");
 		submenu.setMnemonic(KeyEvent.VK_B);
 
 		menuItem = new JMenuItem("Box Blur...", KeyEvent.VK_B);
 		menuItem.getAccessibleContext().setAccessibleDescription("Blur effect");
 		menuItem.setActionCommand("CPFXBoxBlur");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menu.add(submenu);
 
 		submenu = new JMenu("Noise");
 		submenu.setMnemonic(KeyEvent.VK_N);
 
 		menuItem = new JMenuItem("Render Monochromatic", KeyEvent.VK_M);
 		menuItem.getAccessibleContext().setAccessibleDescription("Fills the selection with noise");
 		menuItem.setActionCommand("CPMNoise");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menuItem = new JMenuItem("Render Color", KeyEvent.VK_C);
 		menuItem.getAccessibleContext().setAccessibleDescription("Fills the selection with colored noise");
 		menuItem.setActionCommand("CPCNoise");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		menu.add(submenu);
 
 		menu.add(new JSeparator());
 
 		menuItem = new JCheckBoxMenuItem("Apply to All Layers", false);
 		menuItem.getAccessibleContext().setAccessibleDescription(
 				"Apply all listed above effects to all layers instead of just current");
 		menuItem.setActionCommand("CPApplyToAllLayers");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 		paletteItems.put("Apply to All Layers", (JCheckBoxMenuItem) menuItem);
 
 		//
 		// View Menu
 		//
 
 		menu = new JMenu("View");
 		menu.setMnemonic(KeyEvent.VK_V);
 		menuBar.add(menu);
 
 		if (controller.isRunningAsApplet()) {
 			menuItem = new JMenuItem("Floating mode", KeyEvent.VK_F);
 			menuItem.getAccessibleContext().setAccessibleDescription("Opens ChibiPaint in an independent window");
 			menuItem.setActionCommand("CPFloat");
 			menuItem.addActionListener(listener);
 			menu.add(menuItem);
 			menu.add(new JSeparator());
 		}
 
 		menuItem = new JMenuItem("Zoom In", KeyEvent.VK_I);
 		menuItem.getAccessibleContext().setAccessibleDescription("Zooms In");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, ActionEvent.CTRL_MASK));
 		menuItem.setActionCommand("CPZoomIn");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Zoom Out", KeyEvent.VK_O);
 		menuItem.getAccessibleContext().setAccessibleDescription("Zooms Out");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, ActionEvent.CTRL_MASK));
 		menuItem.setActionCommand("CPZoomOut");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Zoom 100%", KeyEvent.VK_1);
 		menuItem.getAccessibleContext().setAccessibleDescription("Resets the zoom factor to 100%");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, ActionEvent.CTRL_MASK));
 		menuItem.setActionCommand("CPZoom100");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		menu.add(new JSeparator());
 
 		menuItem = new JCheckBoxMenuItem("Use Linear Interpolation", false);
 		menuItem.setMnemonic(KeyEvent.VK_L);
 		menuItem.getAccessibleContext().setAccessibleDescription(
 				"Linear interpolation is used to give a smoothed looked to the picture when zoomed in");
 		menuItem.setActionCommand("CPLinearInterpolation");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 		paletteItems.put("Use Linear Interpolation", (JCheckBoxMenuItem) menuItem);
 
 		menu.add(new JSeparator());
 
 		menuItem = new JCheckBoxMenuItem("Show Grid", false);
 		menuItem.setMnemonic(KeyEvent.VK_G);
 		menuItem.getAccessibleContext().setAccessibleDescription("Displays a grid over the image");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
 		menuItem.setActionCommand("CPToggleGrid");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 		paletteItems.put("Show Grid", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JMenuItem("Grid options...", KeyEvent.VK_D);
 		menuItem.getAccessibleContext().setAccessibleDescription("Shows the grid options dialog box");
 		menuItem.setActionCommand("CPGridOptions");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		menu.add(new JSeparator());
 
 		submenu = new JMenu("Palettes");
 		submenu.setMnemonic(KeyEvent.VK_P);
 
 		menuItem = new JMenuItem("Toggle Palettes", KeyEvent.VK_P);
 		menuItem.getAccessibleContext().setAccessibleDescription("Hides or shows all palettes");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
 		menuItem.setActionCommand("CPTogglePalettes");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 
 		submenu.add(new JSeparator());
 
 		menuItem = new JCheckBoxMenuItem("Show Brush", true);
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setActionCommand("CPPalBrush");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Brush", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JCheckBoxMenuItem("Show Color", true);
 		menuItem.setMnemonic(KeyEvent.VK_C);
 		menuItem.setActionCommand("CPPalColor");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Color", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JCheckBoxMenuItem("Show Layers", true);
 		menuItem.setMnemonic(KeyEvent.VK_Y);
 		menuItem.setActionCommand("CPPalLayers");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Layers", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JCheckBoxMenuItem("Show Misc", true);
 		menuItem.setMnemonic(KeyEvent.VK_M);
 		menuItem.setActionCommand("CPPalMisc");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Misc", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JCheckBoxMenuItem("Show Stroke", true);
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menuItem.setActionCommand("CPPalStroke");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Stroke", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JCheckBoxMenuItem("Show Swatches", true);
 		menuItem.setMnemonic(KeyEvent.VK_W);
 		menuItem.setActionCommand("CPPalSwatches");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Color Swatches", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JCheckBoxMenuItem("Show Textures", true);
 		menuItem.setMnemonic(KeyEvent.VK_X);
 		menuItem.setActionCommand("CPPalTextures");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Textures", (JCheckBoxMenuItem) menuItem);
 
 		menuItem = new JCheckBoxMenuItem("Show Tools", true);
 		menuItem.setMnemonic(KeyEvent.VK_T);
 		menuItem.setActionCommand("CPPalTool");
 		menuItem.addActionListener(listener);
 		submenu.add(menuItem);
 		paletteItems.put("Tools", (JCheckBoxMenuItem) menuItem);
 
 		menu.add(submenu);
 
 		//
 		// Help Menu
 		//
 
 		menu = new JMenu("Help");
 		menu.setMnemonic(KeyEvent.VK_H);
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("About...", KeyEvent.VK_A);
 		menuItem.getAccessibleContext().setAccessibleDescription("Displays some information about ChibiPaint");
 		menuItem.setActionCommand("CPAbout");
 		menuItem.addActionListener(listener);
 		menu.add(menuItem);
 
 		return menuBar;
 	}
 
 	public void showPalette(String palette, boolean show) {
 		getPaletteManager().showPalette(palette, show);
 	}
 
 	public void setPaletteMenuItem(String title, boolean selected) {
 		JCheckBoxMenuItem item = paletteItems.get(title);
 		if (item != null) {
 			item.setSelected(selected);
 		}
 	}
 
 	public void togglePalettes() {
 		getPaletteManager().togglePalettes();
 	}
 
 	public CPPaletteManager getPaletteManager() {
 		return paletteManager;
 	}
 
 	public void setPaletteManager(CPPaletteManager paletteManager) {
 		this.paletteManager = paletteManager;
 	}
 
 	class CPDesktop extends JDesktopPane {
 
 		public CPDesktop() {
 			addComponentListener(new ComponentAdapter() {
 
 				public void componentResized(ComponentEvent e) {
 					bg.setSize(getSize());
 					bg.validate();
 				}
 			});
 		}
 	}
 }
