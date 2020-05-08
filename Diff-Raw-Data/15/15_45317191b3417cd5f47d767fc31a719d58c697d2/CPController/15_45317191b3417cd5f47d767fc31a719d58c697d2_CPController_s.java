 /*
 	ChibiPaintMod
 
    Copyright (c) 2012-2013 Sergey Semushin
    Copyright (c) 2006-2008 Marc Schefer
 
     This file is part of ChibiPaintMod (previously ChibiPaint).
 
     ChibiPaintMod is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ChibiPaintMod is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ChibiPaintMod. If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package chibipaint;
 
 import cello.jtablet.installer.BrowserLauncher;
 import chibipaint.engine.CPArtwork;
 import chibipaint.engine.CPBrushInfo;
 import chibipaint.gui.CPCanvas;
 import chibipaint.gui.CPMainGUI;
 import chibipaint.util.CPColor;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public abstract class CPController implements ActionListener
 {
 
 private final static String VERSION_STRING = "0.0.5 (alpha)";
 
 private final CPColor curColor = new CPColor ();
 // int curAlpha = 255;
 // int brushSize = 16;
 
 // some important object references
 public CPArtwork artwork;
 public CPCanvas canvas;
 
 final CPBrushInfo[] tools;
 private int curBrush = T_PENCIL;
 private int curMode = M_DRAW;
 
 private final ArrayList<ICPColorListener> colorListeners = new ArrayList<ICPColorListener> ();
 private final ArrayList<ICPToolListener> toolListeners = new ArrayList<ICPToolListener> ();
 private final ArrayList<ICPModeListener> modeListeners = new ArrayList<ICPModeListener> ();
 private final ArrayList<ICPViewListener> viewListeners = new ArrayList<ICPViewListener> ();
 private final ArrayList<ICPEventListener> cpEventListeners = new ArrayList<ICPEventListener> ();
 private final HashMap<String, Image> imageCache = new HashMap<String, Image> ();
 
 //
 // Definition of all the standard tools available
 //
 // TODO: Make enum
 public static final int T_INVALID = -1;
 public static final int T_PENCIL = 0;
 public static final int T_ERASER = 1;
 public static final int T_PEN = 2;
 public static final int T_SOFTERASER = 3;
 public static final int T_AIRBRUSH = 4;
 public static final int T_DODGE = 5;
 public static final int T_BURN = 6;
 public static final int T_WATER = 7;
 public static final int T_BLUR = 8;
 public static final int T_SMUDGE = 9;
 public static final int T_BLENDER = 10;
 static final int T_MAX = 11;
 
 //
 // Definition of all the modes available
 //
 
 // TODO: Make enum
 public static final int M_INVALID = -1;
 public static final int M_DRAW = 0;
 public static final int M_FLOODFILL = 1;
 public static final int M_RECT_SELECTION = 3;
 public static final int M_ROTATE_CANVAS = 4;
 public static final int M_FREE_SELECTION = 5;
 public static final int M_FREE_TRANSFORM = 6;
 public static final int M_MAX = 7;
 
 // Setting for other modes than draw (probably should do different class for them)
 private int colorDistance;
 
 private CPMainGUI mainGUI;
 
 public interface ICPColorListener
 {
 
   public void newColor (CPColor color);
 }
 
 public interface ICPToolListener
 {
 
   public void newTool (int tool, CPBrushInfo toolInfo);
 }
 
 public interface ICPModeListener
 {
 
   public void modeChange (int mode);
 }
 
 public interface ICPViewListener
 {
 
   public void viewChange (CPViewInfo viewInfo);
 }
 
 public interface ICPEventListener
 {
 
   public void cpEvent ();
 }
 
 public static class CPViewInfo
 {
 
   public float zoom;
   public int offsetX, offsetY;
 }
 
 CPController ()
 {
   tools = new CPBrushInfo[T_MAX];
   tools[T_PENCIL] = new CPBrushInfo (T_PENCIL, 16, 255, true, false, .5f, .05f, false, true,
                                      CPBrushInfo.B_ROUND_AA, CPBrushInfo.M_PAINT, 1f, 0f);
   tools[T_ERASER] = new CPBrushInfo (T_ERASER, 16, 255, true, false, .5f, .05f, false, false,
                                      CPBrushInfo.B_ROUND_AA, CPBrushInfo.M_ERASE, 1f, 0f);
   tools[T_PEN] = new CPBrushInfo (T_PEN, 2, 128, true, false, .5f, .05f, true, false, CPBrushInfo.B_ROUND_AA,
                                   CPBrushInfo.M_PAINT, 1f, 0f);
   tools[T_SOFTERASER] = new CPBrushInfo (T_SOFTERASER, 16, 64, false, true, .5f, .05f, false, true,
                                          CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_ERASE, 1f, 0f);
   tools[T_AIRBRUSH] = new CPBrushInfo (T_AIRBRUSH, 50, 32, false, true, .5f, .05f, false, true,
                                        CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_PAINT, 1f, 0f);
   tools[T_DODGE] = new CPBrushInfo (T_DODGE, 30, 32, false, true, .5f, .05f, false, true,
                                     CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_DODGE, 1f, 0f);
   tools[T_BURN] = new CPBrushInfo (T_BURN, 30, 32, false, true, .5f, .05f, false, true,
                                    CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_BURN, 1f, 0f);
   tools[T_WATER] = new CPBrushInfo (T_WATER, 30, 70, false, true, .5f, .02f, false, true, CPBrushInfo.B_ROUND_AA,
                                     CPBrushInfo.M_WATER, .3f, .6f);
   tools[T_BLUR] = new CPBrushInfo (T_BLUR, 20, 255, false, true, .5f, .05f, false, true,
                                    CPBrushInfo.B_ROUND_PIXEL, CPBrushInfo.M_BLUR, 1f, 0f);
   tools[T_SMUDGE] = new CPBrushInfo (T_SMUDGE, 20, 128, false, true, .5f, .01f, false, true,
                                      CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_SMUDGE, 0f, 1f);
   tools[T_BLENDER] = new CPBrushInfo (T_SMUDGE, 20, 60, false, true, .5f, .1f, false, true,
                                       CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_OIL, 0f, .07f);
 }
 
 public void setArtwork (CPArtwork artwork)
 {
   this.artwork = artwork;
   if (isRunningAsApplication ())
     this.artwork.getUndoManager ().setMaxUndo (50);
 }
 
 public void setCanvas (CPCanvas canvas)
 {
   this.canvas = canvas;
 }
 
 public void setCurColor (CPColor color)
 {
   if (!curColor.isEqual (color))
     {
       artwork.setForegroundColor (color.getRgb ());
 
       curColor.copyFrom (color);
       for (Object l : colorListeners)
         {
           ((ICPColorListener) l).newColor (color);
         }
     }
 }
 
 public CPColor getCurColor ()
 {
   return (CPColor) curColor.clone ();
 }
 
 public int getCurColorRgb ()
 {
   return curColor.getRgb ();
 }
 
 public void setCurColorRgb (int color)
 {
   CPColor col = new CPColor (color);
   setCurColor (col);
 }
 
 public void setBrushSize (int size)
 {
   tools[getCurBrush ()].size = Math.max (1, Math.min (200, size));
   callToolListeners ();
 }
 
 public int getBrushSize ()
 {
   return tools[getCurBrush ()].size;
 }
 
 public void setAlpha (int alpha)
 {
   tools[getCurBrush ()].alpha = alpha;
   callToolListeners ();
 }
 
 public int getAlpha ()
 {
   return tools[getCurBrush ()].alpha;
 }
 
 public void setTool (int tool)
 {
   setMode (M_DRAW);
   setCurBrush (tool);
   artwork.setBrush (tools[tool]);
   callToolListeners ();
 }
 
 public CPBrushInfo getBrushInfo ()
 {
   return tools[getCurBrush ()];
 }
 
 void setMode (int mode)
 {
   setCurMode (mode);
   callModeListeners ();
   callToolListeners (); // For updating mode settings if they exist
 }
 
 	/*
    * public CPToolInfo getModeInfo() { return modes[curMode]; }
 	 */
 
 @Override
 public void actionPerformed (ActionEvent e)
 {
   if (artwork == null || canvas == null)
     {
       return; // this shouldn't happen but just in case
     }
 
   String command = e.getActionCommand ();
 
   if (e.getActionCommand ().equals ("CPZoomIn"))
     {
       canvas.zoomIn ();
     }
   else if (e.getActionCommand ().equals ("CPZoomOut"))
     {
       canvas.zoomOut ();
     }
   else if (e.getActionCommand ().equals ("CPZoom100"))
     {
       canvas.zoom100 ();
     }
   else if (e.getActionCommand ().equals ("CPUndo"))
     {
       artwork.undo ();
     }
   else if (e.getActionCommand ().equals ("CPRedo"))
     {
       artwork.redo ();
     }
   else if (e.getActionCommand ().equals ("CPClearHistory"))
     {
       int choice = JOptionPane
               .showConfirmDialog (
                       getDialogParent (),
                       "You're about to clear the current Undo/Redo history.\nThis operation cannot be undone, are you sure you want to do that?",
                       "Clear History", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 
       if (choice == JOptionPane.OK_OPTION)
         {
           artwork.clearHistory ();
         }
     }
   else if (e.getActionCommand ().equals ("CPPencil"))
     {
       setTool (T_PENCIL);
     }
   else if (e.getActionCommand ().equals ("CPPen"))
     {
       setTool (T_PEN);
     }
   else if (e.getActionCommand ().equals ("CPEraser"))
     {
       setTool (T_ERASER);
     }
   else if (e.getActionCommand ().equals ("CPSoftEraser"))
     {
       setTool (T_SOFTERASER);
     }
   else if (e.getActionCommand ().equals ("CPAirbrush"))
     {
       setTool (T_AIRBRUSH);
     }
   else if (e.getActionCommand ().equals ("CPDodge"))
     {
       setTool (T_DODGE);
     }
   else if (e.getActionCommand ().equals ("CPBurn"))
     {
       setTool (T_BURN);
     }
   else if (e.getActionCommand ().equals ("CPWater"))
     {
       setTool (T_WATER);
     }
   else if (e.getActionCommand ().equals ("CPBlur"))
     {
       setTool (T_BLUR);
     }
   else if (e.getActionCommand ().equals ("CPSmudge"))
     {
       setTool (T_SMUDGE);
     }
   else if (e.getActionCommand ().equals ("CPBlender"))
     {
       setTool (T_BLENDER);
     }
 
   // Modes
 
   else if (e.getActionCommand ().equals ("CPFloodFill"))
     {
       setMode (M_FLOODFILL);
     }
 
   else if (e.getActionCommand ().equals ("CPFreeSelection"))
     {
       setMode (M_FREE_SELECTION);
     }
 
   else if (e.getActionCommand ().equals ("CPFreeTransform"))
     {
       setMode (M_FREE_TRANSFORM);
     }
 
   else if (e.getActionCommand ().equals ("CPRectSelection"))
     {
       setMode (M_RECT_SELECTION);
     }
 
   else if (e.getActionCommand ().equals ("CPRotateCanvas"))
     {
       setMode (M_ROTATE_CANVAS);
     }
 
   // Stroke modes
 
   else if (e.getActionCommand ().equals ("CPFreeHand"))
     {
       tools[getCurBrush ()].strokeMode = CPBrushInfo.SM_FREEHAND;
       callToolListeners ();
     }
   else if (e.getActionCommand ().equals ("CPLine"))
     {
       tools[getCurBrush ()].strokeMode = CPBrushInfo.SM_LINE;
       callToolListeners ();
     }
   else if (e.getActionCommand ().equals ("CPBezier"))
     {
       tools[getCurBrush ()].strokeMode = CPBrushInfo.SM_BEZIER;
       callToolListeners ();
     }
 
   else if (e.getActionCommand ().equals ("CPAbout"))
     {
 
       // for copying style
       JLabel label = new JLabel ();
       Font font = label.getFont ();
 
       // create some css from the label's font
       StringBuffer style = new StringBuffer ("font-family:" + font.getFamily () + ";");
       style.append ("font-weight:" + (font.isBold () ? "bold" : "normal") + ";");
       style.append ("font-size:" + font.getSize () + "pt;");
 
 
       JEditorPane ep = new JEditorPane ("text/html", "<html><body style=\"" + style + "\"><pre>ChibiPaintMod\n" + "Version "
               + VERSION_STRING + "\n\n" + "Copyright (c) 2012-2013 Sergey Semushin.\n"
               + "Copyright (c) 2006-2008 Marc Schefer. All Rights Reserved.\n\n"
               + "ChibiPaintMod is free software: you can redistribute it and/or modify\n"
               + "it under the terms of the GNU General Public License as published by\n"
               + "the Free Software Foundation, either version 3 of the License, or\n"
               + "(at your option) any later version.\n\n"
               + "ChibiPaintMod is distributed in the hope that it will be useful,\n"
               + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
               + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
               + "GNU General Public License for more details.\n\n"
 
               + "You should have received a copySelected of the GNU General Public License\n"
               + "along with ChibiPaintMod. If not, see <a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses/</a>.\n" +
               "</pre></html>");
       ep.setEditable (false);
       ep.setBackground (label.getBackground ());
       // handle link events
       ep.addHyperlinkListener (new HyperlinkListener ()
       {
         @Override
         public void hyperlinkUpdate (HyperlinkEvent e)
         {
           if (e.getEventType ().equals (HyperlinkEvent.EventType.ACTIVATED))
             try
               {
                 BrowserLauncher.browse (e.getURL ().toURI ()); // roll your own link launcher or use Desktop if J6+
               }
             catch (URISyntaxException e1)
               {
                 return;
               }
         }
       });
       JOptionPane.showMessageDialog (getDialogParent (), ep, "About ChibiPaint...", JOptionPane.PLAIN_MESSAGE);
     }
 
   else if (e.getActionCommand ().equals ("CPTest"))
     {
       // CPTest is disabled for now
     }
 
   // Layers actions
 
 
   else if (e.getActionCommand ().equals ("CPLayerToggleAll"))
     {
       artwork.toggleLayers ();
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPLayerDuplicate"))
     {
       artwork.duplicateLayer ();
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPLayerMergeDown"))
     {
       artwork.mergeDown ();
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPLayerMergeAll"))
     {
       artwork.mergeAllLayers ();
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFill"))
     {
       artwork.fill (getCurColorRgb () | 0xff000000, canvas.getApplyToAllLayers ());
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPClear"))
     {
       artwork.clear (canvas.getApplyToAllLayers ());
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPSelectAll"))
     {
       artwork.getCurSelection ().selectAll ();
       canvas.repaint ();
     }
 
   else if (e.getActionCommand ().equals ("CPDeselectAll"))
     {
      artwork.getCurSelection ().makeEmpty ();
       canvas.repaint ();
     }
 
   else if (e.getActionCommand ().equals ("CPHFlip"))
     {
       artwork.hFlip (canvas.getApplyToAllLayers ());
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPVFlip"))
     {
       artwork.vFlip (canvas.getApplyToAllLayers ());
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPMNoise"))
     {
       artwork.monochromaticNoise (canvas.getApplyToAllLayers ());
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPCNoise"))
     {
       artwork.colorNoise (canvas.getApplyToAllLayers ());
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFXBoxBlur"))
     {
       showBoxBlurDialog ();
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFXInvert"))
     {
       artwork.invert (canvas.getApplyToAllLayers ());
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFXMakeMonochromeByIntensity"))
     {
       artwork.makeMonochrome (canvas.getApplyToAllLayers (), 0);
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFXMakeMonochromeByValue"))
     {
       artwork.makeMonochrome (canvas.getApplyToAllLayers (), 1);
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFXMakeMonochromeByLightness"))
     {
       artwork.makeMonochrome (canvas.getApplyToAllLayers (), 2);
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFXMakeMonochromeByLuma"))
     {
       artwork.makeMonochrome (canvas.getApplyToAllLayers (), 3);
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPFXMakeMonochromeBySelColor"))
     {
       artwork.makeMonochrome (canvas.getApplyToAllLayers (), 4);
       artwork.finalizeUndo ();
     }
 
   else if (e.getActionCommand ().equals ("CPApplyToAllLayers"))
     {
       canvas.setApplyToAllLayers (((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPLinearInterpolation"))
     {
       canvas.setInterpolation (((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPToggleGrid"))
     {
       canvas.showGrid (((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPGridOptions"))
     {
       showGridOptionsDialog ();
     }
 
   else if (e.getActionCommand ().equals ("CPResetCanvasRotation"))
     {
       canvas.resetRotation ();
     }
 
   else if (e.getActionCommand ().equals ("CPPalColor"))
     {
       mainGUI.showPalette ("color", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPPalBrush"))
     {
       mainGUI.showPalette ("brush", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPPalLayers"))
     {
       mainGUI.showPalette ("layers", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPPalStroke"))
     {
       mainGUI.showPalette ("stroke", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPPalSwatches"))
     {
       mainGUI.showPalette ("swatches", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPPalTool"))
     {
       mainGUI.showPalette ("tool", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPPalMisc"))
     {
       mainGUI.showPalette ("misc", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPPalTextures"))
     {
       mainGUI.showPalette ("textures", ((JCheckBoxMenuItem) e.getSource ()).isSelected ());
     }
 
   else if (e.getActionCommand ().equals ("CPTogglePalettes"))
     {
       mainGUI.togglePalettes ();
     }
 
   else if (e.getActionCommand ().equals ("CPCopy"))
     {
       canvas.copy ();
     }
   else if (e.getActionCommand ().equals ("CPPaste"))
     {
       canvas.paste ();
     }
   else if (e.getActionCommand ().equals ("CPCut"))
     {
       canvas.cut ();
     }
   callCPEventListeners ();
 }
 
 public void addColorListener (ICPColorListener listener)
 {
   colorListeners.add (listener);
 }
 
 public void addToolListener (ICPToolListener listener)
 {
   toolListeners.add (listener);
 }
 
 public void removeToolListener (ICPToolListener listener)
 {
   toolListeners.remove (listener);
 }
 
 public void callToolListeners ()
 {
   for (ICPToolListener l : toolListeners)
     {
       l.newTool (getCurBrush (), tools[getCurBrush ()]);
     }
 }
 
 public void addModeListener (ICPModeListener listener)
 {
   modeListeners.add (listener);
 }
 
 public void removeModeListener (ICPModeListener listener)
 {
   modeListeners.remove (listener);
 }
 
 void callModeListeners ()
 {
   for (ICPModeListener l : modeListeners)
     {
       l.modeChange (getCurMode ());
     }
 }
 
 public void addViewListener (ICPViewListener listener)
 {
   viewListeners.add (listener);
 }
 
 public void callViewListeners (CPViewInfo info)
 {
   for (ICPViewListener l : viewListeners)
     {
       l.viewChange (info);
     }
 }
 
 void callCPEventListeners ()
 {
   for (ICPEventListener l : cpEventListeners)
     {
       l.cpEvent ();
     }
 }
 
 byte[] getPngData (Image img)
 {
   int imageType = artwork.hasAlpha () ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
 
   // FIXME: Wouldn't it be better to use a BufferedImage and avoid this anyway?
   BufferedImage bi = new BufferedImage (img.getWidth (null), img.getHeight (null), imageType);
   Graphics bg = bi.getGraphics ();
   bg.drawImage (img, 0, 0, null);
   bg.dispose ();
 
   ByteArrayOutputStream pngFileStream = new ByteArrayOutputStream (1024);
   try
     {
       ImageIO.write (bi, "png", pngFileStream);
     }
   catch (IOException e)
     {
       return null;
     }
   byte[] pngData = pngFileStream.toByteArray ();
 
   return pngData;
 }
 
 public Image loadImage (String imageName)
 {
   Image img = imageCache.get (imageName);
   if (img == null)
     {
       try
         {
           ClassLoader loader = getClass ().getClassLoader ();
           Class[] classes = {Image.class};
 
           URL url = loader.getResource ("resource/" + imageName);
           img = (Image) url.openConnection ().getContent (classes);
         }
       catch (Throwable t)
         {
         }
       imageCache.put (imageName, img);
     }
   return img;
 }
 
 public CPArtwork getArtwork ()
 {
   return artwork;
 }
 
 public void setMainGUI (CPMainGUI mainGUI)
 {
   this.mainGUI = mainGUI;
 }
 
 public CPMainGUI getMainGUI ()
 {
   return mainGUI;
 }
 
 // returns the Component to be used as parent to display dialogs
 protected abstract Component getDialogParent ();
 
 //
 // misc dialog boxes that shouldn't be here v___v
 
 void showBoxBlurDialog ()
 {
   JPanel panel = new JPanel ();
 
   panel.add (new JLabel ("Blur amount:"));
   SpinnerModel blurXSM = new SpinnerNumberModel (3, 1, 100, 1);
   JSpinner blurX = new JSpinner (blurXSM);
   panel.add (blurX);
 
   panel.add (new JLabel ("Iterations:"));
   SpinnerModel iterSM = new SpinnerNumberModel (1, 1, 8, 1);
   JSpinner iter = new JSpinner (iterSM);
   panel.add (iter);
 
   Object[] array = {"Box blur\n\n", panel};
   int choice = JOptionPane.showConfirmDialog (getDialogParent (), array, "Box Blur", JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.PLAIN_MESSAGE);
 
   if (choice == JOptionPane.OK_OPTION)
     {
       int blur = ((Integer) blurX.getValue ()).intValue ();
       int iterations = ((Integer) iter.getValue ()).intValue ();
 
       artwork.boxBlur (blur, blur, iterations, canvas.getApplyToAllLayers ());
       canvas.repaint ();
     }
 }
 
 void showGridOptionsDialog ()
 {
   JPanel panel = new JPanel ();
 
   panel.add (new JLabel ("Grid Size:"));
   SpinnerModel sizeSM = new SpinnerNumberModel (canvas.gridSize, 1, 1000, 1);
   JSpinner sizeSpinner = new JSpinner (sizeSM);
   panel.add (sizeSpinner);
 
   Object[] array = {"Grid Options\n\n", panel};
   int choice = JOptionPane.showConfirmDialog (getDialogParent (), array, "Grid Options",
                                               JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
   if (choice == JOptionPane.OK_OPTION)
     {
       int size = ((Integer) sizeSpinner.getValue ()).intValue ();
 
       canvas.gridSize = size;
       canvas.repaint ();
     }
 
 }
 
 public boolean isRunningAsApplet ()
 {
   return this instanceof CPControllerApplet;
 }
 
 public boolean isRunningAsApplication ()
 {
   return this instanceof CPControllerApplication;
 }
 
 public int getCurMode ()
 {
   return curMode;
 }
 
 void setCurMode (int curMode)
 {
   this.curMode = curMode;
 }
 
 public int getColorDistance ()
 {
   return colorDistance;
 }
 
 public void setColorDistance (int colorDistance)
 {
   this.colorDistance = colorDistance;
 }
 
 public int getCurBrush ()
 {
   return curBrush;
 }
 
 void setCurBrush (int curBrush)
 {
   this.curBrush = curBrush;
 }
 }
