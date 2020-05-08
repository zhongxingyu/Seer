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
 
 package chibipaint.controller;
 
 import cello.jtablet.installer.BrowserLauncher;
 import chibipaint.effects.*;
 import chibipaint.engine.CPArtwork;
 import chibipaint.engine.CPBrushInfo;
 import chibipaint.gui.CPCanvas;
 import chibipaint.gui.CPMainGUI;
 import chibipaint.util.CPColor;
 import chibipaint.util.CPTablet;
 import chibipaint.util.CPTablet2;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public abstract class CPCommonController implements ICPController
 {
 
 private int selectionFillAlpha = 255;
 
 public void setSelectionAction (selectionAction selectionActionArg)
 {
   this.curSelectionAction = selectionActionArg;
 }
 
 public selectionAction getCurSelectionAction ()
 {
   return curSelectionAction;
 }
 
 public void setCurSelectionAction (selectionAction curSelectionAction)
 {
   this.curSelectionAction = curSelectionAction;
 }
 
 public void setSelectionFillAlpha (int selectionFillAlphaArg)
 {
   this.selectionFillAlpha = selectionFillAlphaArg;
 }
 
 public int getSelectionFillAlpha ()
 {
   return selectionFillAlpha;
 }
 
 public enum selectionAction
 {
   FILL_AND_DESELECT,
   SELECT,
 }
 
 private final static String VERSION_STRING = "0.0.5 (alpha)";
 
 private final CPColor curColor = new CPColor ();
 private boolean oldJTabletUsed;
 // int curAlpha = 255;
 // int brushSize = 16;
 
 // some important object references
 public CPArtwork artwork;
 public CPCanvas canvas = null;
 
 final CPBrushInfo[] tools;
 private int curBrush = T_PENCIL;
 private int curMode = M_DRAW;
 private selectionAction curSelectionAction = selectionAction.SELECT;
 
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
 public static final int M_MAX = 6;
 
 // Setting for other modes than draw (probably should do different class for them)
 private int colorDistance = 0;
 private boolean transformIsOn;
 
 private CPMainGUI mainGUI;
 private int activeMode;
 private Cursor rotateCursor;
 
 public boolean getTransformIsOn ()
 {
   return transformIsOn;
 }
 
 public CPBrushInfo[] getTools ()
 {
   return tools;
 }
 
 public Cursor getRotateCursor ()
 {
   return rotateCursor;
 }
 
 public void performCommand (CPCommandId commandId, CPCommandSettings commandSettings)
 {
   if (commandId.isSpecificToType ())
     return;
 
   switch (commandId)
     {
     case ZoomIn:
       canvas.zoomIn ();
       break;
     case ZoomOut:
       canvas.zoomOut ();
       break;
     case Zoom100:
       canvas.zoom100 ();
       break;
     case Undo:
       artwork.undo ();
       break;
     case Redo:
       artwork.redo ();
       break;
     case ClearHistory:
       clearHistory ();
       break;
     case Pencil:
       setTool (T_PENCIL);
       break;
     case Pen:
       setTool (T_PEN);
       break;
     case Eraser:
       setTool (T_ERASER);
       break;
     case SoftEraser:
       setTool (T_SOFTERASER);
       break;
     case AirBrush:
       setTool (T_AIRBRUSH);
       break;
     case Dodge:
       setTool (T_DODGE);
       break;
     case Burn:
       setTool (T_BURN);
       break;
     case Water:
       setTool (T_WATER);
       break;
     case Blur:
       setTool (T_BLUR);
       break;
     case Smudge:
       setTool (T_SMUDGE);
       break;
     case Blender:
       setTool (T_BLENDER);
       break;
     case FloodFill:
       setMode (M_FLOODFILL);
       break;
     case FreeSelection:
       setMode (M_FREE_SELECTION);
       break;
     case FreeTransform:
       canvas.initTransform ();
       callToolListeners ();
       break;
     case RectSelection:
       setMode (M_RECT_SELECTION);
       break;
     case RotateCanvas:
       setMode (M_ROTATE_CANVAS);
       break;
     case FreeHand:
       tools[getCurBrush ()].strokeMode = CPBrushInfo.SM_FREEHAND;
       callToolListeners ();
       break;
     case Line:
       tools[getCurBrush ()].strokeMode = CPBrushInfo.SM_LINE;
       callToolListeners ();
       break;
     case Bezier:
       tools[getCurBrush ()].strokeMode = CPBrushInfo.SM_BEZIER;
       callToolListeners ();
       break;
     case About:
       launchAboutDialog ();
       break;
     case Test:
       // CPTest is disabled for now
       break;
     case LayerToggleAll:
       artwork.toggleLayers ();
       artwork.finalizeUndo ();
       break;
     case LayerDuplicate:
       artwork.duplicateLayer ();
       artwork.finalizeUndo ();
       break;
     case LayerMergeDown:
       artwork.mergeDown ();
       artwork.finalizeUndo ();
       break;
     case LayerMergeAll:
       artwork.mergeAllLayers ();
       artwork.finalizeUndo ();
       break;
     case Fill:
       artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPFillEffect (getCurColorRgb () | 0xff000000));
       artwork.finalizeUndo ();
       break;
     case Clear:
       artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPClearEffect ());
       artwork.finalizeUndo ();
       break;
     case SelectAll:
       artwork.selectAll ();
       canvas.repaint ();
       break;
     case DeselectAll:
       artwork.deselectAll ();
       canvas.repaint ();
       break;
     case MNoise:
       artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPGrayscaleNoiseEffect ());
       artwork.finalizeUndo ();
       break;
     case CNoise:
       artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPColorNoiseEffect ());
       artwork.finalizeUndo ();
       break;
     case FXBoxBlur:
       showBlurDialog (BlurType.BOX_BLUR);
       break;
     case FXGaussianBlur:
       showBlurDialog (BlurType.GAUSSIAN_BLUR);
       break;
     case FXInvert:
       artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPInverseEffect ());
       artwork.finalizeUndo ();
       break;
     case FXMakeGrayscaleByLuma:
       artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPMakeGrayscaleEffect ());
       artwork.finalizeUndo ();
       break;
     case ApplyToAllLayers:
       canvas.setApplyToAllLayers (((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case LinearInterpolation:
       canvas.setInterpolation (((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case ShowSelection:
       canvas.setShowSelection (((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case ShowGrid:
       canvas.showGrid (((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case GridOptions:
       showGridOptionsDialog ();
       break;
     case ResetCanvasRotation:
       canvas.resetRotation ();
       break;
     case PalColor:
       mainGUI.showPalette ("color", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case PalBrush:
       mainGUI.showPalette ("tool_preferences", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case PalLayers:
       mainGUI.showPalette ("layers", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case PalStroke:
       mainGUI.showPalette ("stroke", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case PalSwatches:
       mainGUI.showPalette ("swatches", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case PalTool:
       mainGUI.showPalette ("tool", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case PalMisc:
       mainGUI.showPalette ("misc", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case PalTextures:
       mainGUI.showPalette ("textures", ((CPCommandSettings.checkBoxState) commandSettings).checked);
       break;
     case TogglePalettes:
       mainGUI.togglePalettes ();
       break;
     case Copy:
       canvas.copy ();
       break;
     case CopyMerged:
       canvas.copyMerged ();
       break;
     case Paste:
       canvas.paste ();
       break;
     case Cut:
       canvas.cut ();
       break;
     case ApplyTransform:
       canvas.applyTransform ();
       break;
     case CancelTransform:
       canvas.cancelTransform ();
       break;
     case FlipHorizontally:
       artwork.doTransformAction (CPArtwork.transformType.FLIP_H);
       canvas.repaint ();
       break;
     case FlipVertically:
       artwork.doTransformAction (CPArtwork.transformType.FLIP_V);
       canvas.repaint ();
       break;
     case Rotate90CCW:
       artwork.doTransformAction (CPArtwork.transformType.ROTATE_90_CCW);
       canvas.repaint ();
       break;
     case Rotate90CW:
       artwork.doTransformAction (CPArtwork.transformType.ROTATE_90_CW);
       canvas.repaint ();
       break;
     }
   callCPEventListeners ();
 }
 
 private void launchAboutDialog ()
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
 
 private void clearHistory ()
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
   public int width;
   public int height;
 }
 
 CPCommonController ()
 {
   Image img = loadImage ("cursor/rotate.png");
   Point hotSpot = new Point (11, 11);
   rotateCursor = Toolkit.getDefaultToolkit ().createCustomCursor (img, hotSpot, "Rotate");
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
  artwork.setForegroundColor (curColor.getRgb ());
   if (isRunningAsApplication ())
     this.artwork.getUndoManager ().setMaxUndo (50);
 }
 
 public void setCanvas (CPCanvas canvasArg)
 {
   if (this.canvas == null)
     initTablet (canvasArg);
   this.canvas = canvasArg;
 }
 
 private void initTablet (final CPCanvas canvasArg)
 {
   Class<?> JTabletExtensionClass;
   try
     {
       JTabletExtensionClass = Class.forName ("cello.jtablet.installer.JTabletExtension");
       try
         {
           Class<?>[] params = new Class[2];
           params[0] = Component.class;
           params[1] = String.class;
           Method checkCompatibility = JTabletExtensionClass.getMethod ("checkCompatibility", params);
           oldJTabletUsed = !(((Boolean) checkCompatibility.invoke (JTabletExtensionClass, canvasArg, "1.2.0")).booleanValue ());
         }
       catch (Exception e)
         {
           System.out.format ("Error happened during checking compatility with JTablet 1.2\n");
           System.exit (1);
         }
     }
   catch (ClassNotFoundException e)
     {
       oldJTabletUsed = true;
     }
 
   if (!oldJTabletUsed)
     {
       CPTablet2.connectToCanvas (canvasArg);
 
       // This stuff is to fix bug with not disappearing brush preview while moving cursor on widgets while
       // using tablet
       // It's bug of nature unknown to me, that's why I fixed it in a little confusing kind of way.
       // TODO: Maybe fix it a better way.
       canvasArg.addMouseListener (new MouseAdapter ()
       {
         @Override
         public void mouseExited (MouseEvent me)
         {
           canvasArg.setCursorIn (false);
           canvasArg.repaint ();
         }
 
         @Override
         public void mouseEntered (MouseEvent me)
         {
           canvasArg.setCursorIn (true);
         }
       });
     }
   else
     {
       canvasArg.ShowLoadingTabletListenerMessage ();
       CPTablet.getRef ();
       canvasArg.HideLoadingTabletListenerMessage ();
       canvasArg.initMouseListeners ();
     }
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
 
 protected void initTransform ()
 {
 
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
 
 enum BlurType
 {
   BOX_BLUR,
   GAUSSIAN_BLUR,
 }
 
 void showBlurDialog (BlurType type)
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
   String title = "";
 
   switch (type)
     {
     case BOX_BLUR:
       title = "Box Blur";
       break;
     case GAUSSIAN_BLUR:
       title = "Gaussian Blur";
       break;
     }
 
   Object[] array = {title + "\n\n", panel};
   int choice = JOptionPane.showConfirmDialog (getDialogParent (), array, title, JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.PLAIN_MESSAGE);
 
   if (choice == JOptionPane.OK_OPTION)
     {
 
       int blur = ((Integer) blurX.getValue ()).intValue ();
       int iterations = ((Integer) iter.getValue ()).intValue ();
       switch (type)
         {
         case BOX_BLUR:
           artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPBoxBlurEffect (blur, iterations));
           break;
         case GAUSSIAN_BLUR:
           artwork.doEffectAction (canvas.getApplyToAllLayers (), new CPGaussianBlurEffect (blur, iterations));
           break;
         }
 
       canvas.repaint ();
       artwork.finalizeUndo ();
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
 
 public void setTransformStateImpl (boolean transformIsOnArg)
 {
 }
 
 public void setTransformState (boolean transformIsOnArg)
 {
   transformIsOn = transformIsOnArg;
   setTransformStateImpl (transformIsOnArg);
 }
 
 
 }
