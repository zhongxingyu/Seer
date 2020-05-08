 /*
  * ChibiPaintMod
  *     Copyright (c) 2012-2014 Sergey Semushin
  *     Copyright (c) 2006-2008 Marc Schefer
  *
  *     This file is part of ChibiPaintMod (previously ChibiPaint).
  *
  *     ChibiPaintMod is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     ChibiPaintMod is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ChibiPaintMod. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package chibipaint.gui;
 
 import chibipaint.controller.CPCommandId;
 import chibipaint.controller.CPCommandSettings;
 import chibipaint.controller.CPCommonController;
 import chibipaint.file.CPFile;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 public class CPMainGUI
 {
 
 private final CPCommonController controller;
 
 class MenuItemHashMap extends HashMap<CPCommandId, ArrayList<JMenuItem>>
 {
   public void put (CPCommandId key, JMenuItem value)
   {
     ArrayList<JMenuItem> list = this.get (key);
     if (list == null)
       {
         list = new ArrayList<JMenuItem> ();
         list.add (value);
         super.put (key, list);
       }
     else
       list.add (value);
   }
 }
 
 private final MenuItemHashMap menuItems = new MenuItemHashMap ();
 private CPPaletteManager paletteManager;
 
 private JMenuBar menuBar;
 private JMenuBar menuBarTemporary; // Temporary menu bar for safe replacement with actual one
 private JMenu lastMenu, lastSubMenu;
 private JMenu recentFilesMenuItem;
 private JMenuItem lastItem;
 private JPanel mainPanel;
 private JDesktopPane jdp;
 private JPanel bg;
 
 // FIXME: replace this hack by something better
 private final Map<String, JCheckBoxMenuItem> paletteItems = new HashMap<String, JCheckBoxMenuItem> ();
 
 public CPMainGUI (CPCommonController controller)
 {
   this.controller = controller;
   controller.setMainGUI (this);
 
   // try {
   // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
   // } catch (Exception ex2) {} */
 
   createMainMenu ();
   menuBar = menuBarTemporary;
   menuBarTemporary = null;
   createGUI ();
 }
 
 public JComponent getGUI ()
 {
   return mainPanel;
 }
 
 public JMenuBar getMenuBar ()
 {
   return menuBar;
 }
 
 public void recreateMenuBar ()
 {
   createMainMenu ();
   menuBar = menuBarTemporary;
   menuBarTemporary = null;
 }
 
 private void createGUI ()
 {
   mainPanel = new JPanel (new BorderLayout ());
 
   jdp = new CPDesktop ();
   setPaletteManager (new CPPaletteManager (controller, jdp));
 
   createCanvasGUI (jdp);
   mainPanel.add (jdp, BorderLayout.CENTER);
 
   JPanel statusBar = new CPStatusBar (controller);
   mainPanel.add (statusBar, BorderLayout.PAGE_END);
   // jdp.addContainerListener(this);
 }
 
 void createCanvasGUI (JComponent c)
 {
   CPCanvas canvas = new CPCanvas (controller);
   setBg (canvas.getCanvasContainer ());
 
   c.add (getBg ());
   canvas.grabFocus ();
 }
 
 // it's internal so boolean argument is ok
 private void addMenuItemInternal (String title, int mnemonic, final CPCommandId commandId, String description, final boolean isCheckable, boolean checked, final CPCommandSettings commandSettings)
 {
   JMenuItem menuItem = null;
   if (controller.isRunningAsApplet () && commandId.isForAppOnly ())
     return;
 
   if (controller.isRunningAsApplication () && commandId.isForAppletOnly ())
     return;
 
   if (isCheckable)
     {
       menuItem = new JCheckBoxMenuItem (title, checked);
       menuItem.setMnemonic (mnemonic);
     }
   else
     {
       menuItem = new JMenuItem (title, mnemonic);
     }
   menuItem.getAccessibleContext ().setAccessibleDescription (description);
   menuItem.addActionListener (new ActionListener ()
   {
     @Override
     public void actionPerformed (ActionEvent e)
     {
       controller.performCommand (commandId, isCheckable ? new CPCommandSettings.CheckBoxState (((JCheckBoxMenuItem) e.getSource ()).isSelected ()) : commandSettings);
     }
   });
   ArrayList<JMenuItem> list = menuItems.get (commandId);
   menuItems.put (commandId, menuItem);
   if (lastSubMenu != null)
     lastSubMenu.add (menuItem);
   else
     lastMenu.add (menuItem);
   lastItem = menuItem;
 }
 
 ArrayList<JMenuItem> getMenuItemListByCmdId (CPCommandId cmdId)
 {
   return menuItems.get (cmdId);
 }
 
 void setEnabledByCmdId (CPCommandId cmdId, boolean value)
 {
   for (JMenuItem item : controller.getMainGUI ().getMenuItemListByCmdId (cmdId))
     item.setEnabled (value);
 }
 
 void setSelectedByCmdId (CPCommandId cmdId, boolean value)
 {
   for (JMenuItem item : controller.getMainGUI ().getMenuItemListByCmdId (cmdId))
     item.setSelected (value);
 }
 
 void setEnabledForTransform (boolean enabled)
 {
   Iterator it = menuItems.entrySet ().iterator ();
   while (it.hasNext ())
     {
       Map.Entry pairs = (Map.Entry) it.next ();
       ArrayList<JMenuItem> item = (ArrayList<JMenuItem>) pairs.getValue ();
       for (JMenuItem menuItem : item)
         menuItem.setEnabled (enabled);
     }
   ((CPLayersPalette) getPaletteManager ().getPalettes ().get ("layers")).setEnabledForTransform (enabled);
   ((CPMiscPalette) getPaletteManager ().getPalettes ().get ("misc")).setEnabledForTransform (enabled);
 }
 
 void addMenuItem (String title, int mnemonic, CPCommandId commandId)
 {
   addMenuItemInternal (title, mnemonic, commandId, "", false, false, null);
 }
 
 void addMenuItem (String title, int mnemonic, CPCommandId commandId, String description)
 {
   addMenuItemInternal (title, mnemonic, commandId, description, false, false, null);
 }
 
 void addMenuItem (String title, int mnemonic, CPCommandId commandId, String description, CPCommandSettings settings)
 {
   addMenuItemInternal (title, mnemonic, commandId, description, false, false, settings);
 }
 
 void addMenuItem (String title, int mnemonic, CPCommandId commandId, String description, KeyStroke accelerator)
 {
   addMenuItem (title, mnemonic, commandId, description);
   lastItem.setAccelerator (accelerator);
 }
 
 void addMenuItem (String title, int mnemonic, CPCommandId commandId, String description, KeyStroke accelerator, CPCommandSettings settings)
 {
   addMenuItemInternal (title, mnemonic, commandId, description, false, false, settings);
   lastItem.setAccelerator (accelerator);
 }
 
 void addCheckBoxMenuItem (String title, int mnemonic, CPCommandId commandId, String description, boolean checked)
 {
   addMenuItemInternal (title, mnemonic, commandId, description, true, checked, null);
 }
 
 void addCheckBoxMenuItem (String title, int mnemonic, CPCommandId commandId, boolean checked)
 {
   addMenuItemInternal (title, mnemonic, commandId, "", true, checked, null);
 }
 
 void addCheckBoxMenuItem (String title, int mnemonic, CPCommandId commandId, String description, KeyStroke accelerator, boolean checked)
 {
   addMenuItemInternal (title, mnemonic, commandId, description, true, checked, null);
   lastItem.setAccelerator (accelerator);
 }
 
 void addMenu (String Title, int mnemonic)
 {
   JMenu menu = new JMenu (Title);
   menu.setMnemonic (mnemonic);
   menuBarTemporary.add (menu);
   lastSubMenu = null;
   lastMenu = menu;
 }
 
 void addSubMenu (String Title, int mnemonic)
 {
   JMenu menu = new JMenu (Title);
   menu.setMnemonic (KeyEvent.VK_F);
   lastMenu.add (menu);
   lastSubMenu = menu;
 }
 
 void endSubMenu ()
 {
   lastSubMenu = null;
 }
 
 void addSeparator ()
 {
   if (lastSubMenu != null)
     lastSubMenu.add (new JSeparator ());
   else
     lastMenu.add (new JSeparator ());
 }
 
 void createMainMenu ()
 {
   menuBarTemporary = new JMenuBar ();
   lastMenu = null;
   lastSubMenu = null;
   menuItems.clear ();
 
   // File
   addMenu ("File", KeyEvent.VK_F);
 
   addMenuItem ("Send Oekaki", KeyEvent.VK_S, CPCommandId.Send, "Sends the oekaki to the server and exits ChibiPaintMod");
 
   if (controller.isRunningAsApplication ())
     {
       addMenuItem ("New File", KeyEvent.VK_N, CPCommandId.New, "Create new file");
 
       addSeparator ();
 
       addMenuItem ("Save", KeyEvent.VK_S, CPCommandId.Save, "Save existing file", KeyStroke.getKeyStroke (KeyEvent.VK_S, InputEvent.CTRL_MASK));
       addMenuItem ("Save as...", KeyEvent.VK_A, CPCommandId.Export, "Save .chi File", KeyStroke.getKeyStroke (KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), new CPCommandSettings.FileExtension ("chi"));
       addMenuItem ("Open...", KeyEvent.VK_O, CPCommandId.Import, "Open .chi File", KeyStroke.getKeyStroke (KeyEvent.VK_O, InputEvent.CTRL_MASK), new CPCommandSettings.FileExtension ("chi"));
 
       addSubMenu ("Open Recent", KeyEvent.VK_R);
       recentFilesMenuItem = lastSubMenu;
       endSubMenu ();
 
       updateRecentFiles ();
 
       addSeparator ();
 
       String[] importExport = {"Import", "Export"};
       CPCommandId[] loadSave = {CPCommandId.Import, CPCommandId.Export};
       int[] mnemonics = {KeyEvent.VK_I, KeyEvent.VK_X};
       String[] supportedExtensions = CPFile.getSupportedExtensions ();
 
       for (int i = 0; i < 2; i++)
         {
           addSubMenu (importExport[i], mnemonics[i]);
           CPFile file;
           for (String supportedExt : supportedExtensions)
             {
               file = CPFile.fromExtension (supportedExt);
               if (file.isNative ())
                 continue;
 
               addMenuItem (supportedExt.toUpperCase () + " File...", 0, loadSave[i], importExport[i] + " " + supportedExt.toUpperCase () + "Files", new CPCommandSettings.FileExtension (supportedExt));
             }
           endSubMenu ();
         }
 
       addSeparator ();
 
       addMenuItem ("Exit", KeyEvent.VK_E, CPCommandId.Exit, "Exit the application", KeyStroke.getKeyStroke (KeyEvent.VK_Q, InputEvent.CTRL_MASK));
     }
 
   // Edit
   addMenu ("Edit", KeyEvent.VK_E);
   addMenuItem ("Undo", KeyEvent.VK_U, CPCommandId.Undo, "Undo the most recent action", KeyStroke.getKeyStroke (KeyEvent.VK_Z, InputEvent.CTRL_MASK));
   addMenuItem ("Redo", KeyEvent.VK_U, CPCommandId.Redo, "Redo a previously undone action", KeyStroke.getKeyStroke (KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
   addMenuItem ("Clear History", KeyEvent.VK_H, CPCommandId.ClearHistory, "Remove all undo/redo information to regain memory");
   addSeparator ();
 
   addMenuItem ("Cut", KeyEvent.VK_T, CPCommandId.Cut, "Cuts the selected part of the layer", KeyStroke.getKeyStroke (KeyEvent.VK_X, InputEvent.CTRL_MASK));
   addMenuItem ("Copy", KeyEvent.VK_C, CPCommandId.Copy, "Copy the selected part of the layer", KeyStroke.getKeyStroke (KeyEvent.VK_C, InputEvent.CTRL_MASK));
   addMenuItem ("Copy Merged", KeyEvent.VK_Y, CPCommandId.CopyMerged, "Copy the selected part of all the layers merged", KeyStroke.getKeyStroke (KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
   addMenuItem ("Paste", KeyEvent.VK_P, CPCommandId.Paste, "Paste the stuff that have been copied", KeyStroke.getKeyStroke (KeyEvent.VK_V, InputEvent.CTRL_MASK));
   addSeparator ();
 
   addMenuItem ("Select All", KeyEvent.VK_A, CPCommandId.SelectAll, "Selects the whole canvas", KeyStroke.getKeyStroke (KeyEvent.VK_A, InputEvent.CTRL_MASK));
   addMenuItem ("Invert Selection", KeyEvent.VK_I, CPCommandId.InvertSelection, "Invert selection", KeyStroke.getKeyStroke (KeyEvent.VK_I, InputEvent.CTRL_MASK));
   addMenuItem ("Deselect", KeyEvent.VK_D, CPCommandId.DeselectAll, "Deselects the whole canvas", KeyStroke.getKeyStroke (KeyEvent.VK_D, InputEvent.CTRL_MASK));
 
   // Layers
   addMenu ("Layers", KeyEvent.VK_L);
   addMenuItem ("Show / Hide All Layers", KeyEvent.VK_A, CPCommandId.LayerToggleAll, "Toggle All Layers Visibility", KeyStroke.getKeyStroke (KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
   addSeparator ();
   addMenuItem ("Duplicate", KeyEvent.VK_D, CPCommandId.LayerDuplicate, "Creates a copySelected of the currently selected layer", KeyStroke.getKeyStroke (KeyEvent.VK_D, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
   addSeparator ();
   addMenuItem ("Merge Down", KeyEvent.VK_E, CPCommandId.LayerMergeDown, "Merges the currently selected layer with the one directly below it", KeyStroke.getKeyStroke (KeyEvent.VK_E, InputEvent.CTRL_MASK));
   addMenuItem ("Merge All Layers", KeyEvent.VK_A, CPCommandId.LayerMergeAll, "Merges all the layers");
 
   // Effects
   addMenu ("Effects", KeyEvent.VK_E);
   addMenuItem ("Free Transform Selected", KeyEvent.VK_T, CPCommandId.FreeTransform, "Transform selected part of the current layer", KeyStroke.getKeyStroke (KeyEvent.VK_T, InputEvent.CTRL_MASK));
   addSeparator ();
   addMenuItem ("Clear", KeyEvent.VK_C, CPCommandId.Clear, "Clears the selected area", KeyStroke.getKeyStroke (KeyEvent.VK_DELETE, 0));
   addMenuItem ("Fill", KeyEvent.VK_F, CPCommandId.Fill, "Fills the selected area with the current color", KeyStroke.getKeyStroke (KeyEvent.VK_F, InputEvent.CTRL_MASK));
   addMenuItem ("Invert", KeyEvent.VK_F, CPCommandId.FXInvert, "Invert the image colors");
 
   addMenuItem ("Make Grayscale", KeyEvent.VK_M, CPCommandId.FXMakeGrayscaleByLuma, "Make image grayscale by Luma Formula");
 
   addSubMenu ("Blur", KeyEvent.VK_B);
   addMenuItem ("Box Blur...", KeyEvent.VK_B, CPCommandId.FXBoxBlur, "Box Blur Effect");
   addMenuItem ("Gaussian Blur...", KeyEvent.VK_G, CPCommandId.FXGaussianBlur, "Gaussian Blur Effect");
   endSubMenu ();
 
   addSubMenu ("Noise", KeyEvent.VK_N);
   addMenuItem ("Render Monochromatic", KeyEvent.VK_M, CPCommandId.MNoise, "Fills the selection with noise");
   addMenuItem ("Render Color", KeyEvent.VK_C, CPCommandId.CNoise, "Fills the selection with colored noise");
   endSubMenu ();
   addSeparator ();
   addCheckBoxMenuItem ("Apply to All Layers", KeyEvent.VK_T, CPCommandId.ApplyToAllLayers, "Apply all listed above effects to all layers instead of just current", false);
 
   // View
 
   addMenu ("View", KeyEvent.VK_V);
 
   if (controller.isRunningAsApplet ())
     {
       addMenuItem ("Floating mode", KeyEvent.VK_F, CPCommandId.Float, "Opens ChibiPaintMod in an independent window");
       addSeparator ();
     }
 
   addMenuItem ("Zoom In", KeyEvent.VK_I, CPCommandId.ZoomIn, "Zooms In", KeyStroke.getKeyStroke (KeyEvent.VK_ADD, InputEvent.CTRL_MASK));
   addMenuItem ("Zoom Out", KeyEvent.VK_O, CPCommandId.ZoomOut, "Zooms Out", KeyStroke.getKeyStroke (KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK));
   addMenuItem ("Zoom 100%", KeyEvent.VK_O, CPCommandId.Zoom100, "Resets the zoom factor to 100%", KeyStroke.getKeyStroke (KeyEvent.VK_NUMPAD0, InputEvent.CTRL_MASK));
   addMenuItem ("Zoom...", KeyEvent.VK_Z, CPCommandId.ZoomSpecific, "Zoom to specified scale", KeyStroke.getKeyStroke (KeyEvent.VK_Z, InputEvent.ALT_MASK));
   addSeparator ();
   addCheckBoxMenuItem ("Use Linear Interpolation", KeyEvent.VK_L, CPCommandId.LinearInterpolation, "Linear interpolation is used to give a smoothed looked to the picture when zoomed in", false);
   addCheckBoxMenuItem ("Show Selection", KeyEvent.VK_L, CPCommandId.ShowSelection, "Show animated selection borders to better see it and operate with it", true);
   addSeparator ();
   addCheckBoxMenuItem ("Show Grid", KeyEvent.VK_G, CPCommandId.ShowGrid, "Displays a grid over the image", KeyStroke.getKeyStroke (KeyEvent.VK_G, InputEvent.CTRL_MASK), false);
   addMenuItem ("Grid options...", KeyEvent.VK_D, CPCommandId.GridOptions, "Shows the grid options dialog box");
   addSeparator ();
 
   addSubMenu ("Palettes", KeyEvent.VK_P);
   addMenuItem ("Toggle Palettes", KeyEvent.VK_P, CPCommandId.TogglePalettes, "Hides or shows all palettes", KeyStroke.getKeyStroke (KeyEvent.VK_TAB, 0));
   addSeparator ();
   addCheckBoxMenuItem ("Show Tool Preferences", KeyEvent.VK_T, CPCommandId.PalBrush, true);
   paletteItems.put ("ToolPreferences", (JCheckBoxMenuItem) lastItem);
   addCheckBoxMenuItem ("Show Color", KeyEvent.VK_C, CPCommandId.PalColor, true);
   paletteItems.put ("Color", (JCheckBoxMenuItem) lastItem);
   addCheckBoxMenuItem ("Show Layers", KeyEvent.VK_Y, CPCommandId.PalLayers, true);
   paletteItems.put ("Layers", (JCheckBoxMenuItem) lastItem);
   addCheckBoxMenuItem ("Show Misc", KeyEvent.VK_M, CPCommandId.PalMisc, true);
   paletteItems.put ("Misc", (JCheckBoxMenuItem) lastItem);
   addCheckBoxMenuItem ("Show Stroke", KeyEvent.VK_R, CPCommandId.PalStroke, true);
   paletteItems.put ("Stroke", (JCheckBoxMenuItem) lastItem);
   addCheckBoxMenuItem ("Show Swatches", KeyEvent.VK_S, CPCommandId.PalSwatches, true);
   paletteItems.put ("Color Swatches", (JCheckBoxMenuItem) lastItem);
   addCheckBoxMenuItem ("Show Textures", KeyEvent.VK_T, CPCommandId.PalTextures, true);
   paletteItems.put ("Textures", (JCheckBoxMenuItem) lastItem);
   addCheckBoxMenuItem ("Show Tools", KeyEvent.VK_O, CPCommandId.PalTool, true);
   paletteItems.put ("Tools", (JCheckBoxMenuItem) lastItem);
   endSubMenu ();
 
   // Help
   addMenu ("Help", KeyEvent.VK_H);
   addMenuItem ("About...", KeyEvent.VK_A, CPCommandId.About, "Displays some information about ChibiPaintMod");
 }
 
 public void showPalette (String palette, boolean show)
 {
   getPaletteManager ().showPalette (palette, show);
 }
 
 public void togglePaletteVisibility (String palette)
 {
   getPaletteManager ().togglePaletteVisibility (palette);
 }
 
 public void setPaletteMenuItem (String title, boolean selected)
 {
   JCheckBoxMenuItem item = paletteItems.get (title);
   if (item != null)
     {
       item.setSelected (selected);
     }
 }
 
 public boolean togglePalettes ()
 {
   return getPaletteManager ().togglePalettes ();
 }
 
 public CPPaletteManager getPaletteManager ()
 {
   return paletteManager;
 }
 
 void setPaletteManager (CPPaletteManager paletteManager)
 {
   this.paletteManager = paletteManager;
 }
 
 JPanel getBg ()
 {
   return bg;
 }
 
 void setBg (JPanel bg)
 {
   this.bg = bg;
 }
 
static public final int RECENT_FILES_COUNT = 9;

 public void updateRecentFiles ()
 {
   recentFilesMenuItem.removeAll ();
   lastSubMenu = recentFilesMenuItem;
   Preferences userRoot = Preferences.userRoot ();
   Preferences preferences = userRoot.node ("chibipaintmod");
  for (int i = 0; i < RECENT_FILES_COUNT; i++)
     {
       String recentFileName = preferences.get ("Recent File[" + i + "]", "");
       if (recentFileName.length () != 0)
         {
           File recentFile = new File (recentFileName);
           addMenuItem (recentFile.getName (), 0, CPCommandId.OpenRecent, "Open Recent File " + i, KeyStroke.getKeyStroke (KeyEvent.VK_0 + (i + 1) % 10, InputEvent.CTRL_MASK), new CPCommandSettings.RecentFileNumber (i));
         }
     }
   lastSubMenu = null;
 }
 
 class CPDesktop extends JDesktopPane
 {
 
   public CPDesktop ()
   {
     addComponentListener (new ComponentAdapter ()
     {
 
       @Override
       public void componentResized (ComponentEvent e)
       {
         getBg ().setSize (getSize ());
         getBg ().validate ();
       }
     });
   }
 }
 }
 
