 package de.engineapp.controls;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.*;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import de.engine.environment.Scene;
 import de.engine.math.Vector;
 import de.engine.objects.*;
 import de.engine.objects.ObjectProperties.Material;
 import de.engineapp.*;
 import de.engineapp.PresentationModel.StorageListener;
 import de.engineapp.PresentationModel.ViewBoxListener;
 import de.engineapp.visual.*;
 import de.engineapp.visual.Circle;
 import de.engineapp.visual.Square;
 import de.engineapp.visual.Ground;
 import de.engineapp.windows.*;
 import de.engineapp.xml.*;
 
 import static de.engineapp.Constants.*;
 
 
 public class MainToolBar extends JToolBar implements ActionListener, ChangeListener, StorageListener, ViewBoxListener
 {
     private static final long serialVersionUID = 4164673212238397915L;
     
     private final static Localizer LOCALIZER = Localizer.getInstance();
     
     private static final FileFilter SCENE_FILTER = new FileFilter()
     {
 
         @Override
         public boolean accept(File f)
         {
             return f.getName().endsWith(".scnx");
         }
         
         @Override
         public String getDescription()
         {
             return "Scene Files (*.scnx)";
         }
     };
     
     
     private PresentationModel pModel;
     
     private EasyButton new_;
     private EasyButton open;
     private EasyButton save;
     private EasyButton play;
     private EasyButton pause;
     private EasyButton reset;
     private EasyButton grid;
     private EasyButton showArrows;
     private EasyButton focus;
     private EasyButton settings;
     
     private ZoomSlider slider;
     
     
     public MainToolBar(PresentationModel model)
     {
         pModel = model;
         
         this.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ) );
         this.setFloatable(false);
         
         pModel.addViewBoxListener(this);
         pModel.addStorageListener(this);
         
         new_       = new EasyButton(Util.getIcon("new"),           CMD_NEW,         this);
         open       = new EasyButton(Util.getIcon("open"),          CMD_OPEN,        this);
         save       = new EasyButton(Util.getIcon("save"),          CMD_SAVE,        this);
         play       = new EasyButton(Util.getIcon("play"),          CMD_PLAY,        this);
         pause      = new EasyButton(Util.getIcon("pause"),         CMD_PAUSE,       this);
         reset      = new EasyButton(Util.getIcon("reset"),         CMD_RESET,       this);
         grid       = new EasyButton(Util.getIcon("grid"),          CMD_GRID,        this);
         showArrows = new EasyButton(Util.getIcon("object_arrows"), CMD_SHOW_ARROWS, this);
         focus      = new EasyButton(Util.getIcon("focus"),         CMD_FOCUS,       this);
         settings   = new EasyButton(Util.getIcon("settings2"),     CMD_SETTINGS,    this);
         
         pause.setEnabled(false);
         reset.setEnabled(false);
         
         slider = new ZoomSlider(pModel.getZoom());
         slider.addChangeListener(this);
         
         setToolTips();
         
         this.add(new_);
         this.add(open);
         this.add(save);
         this.addSeparator();
         this.add(play);
         this.add(pause);
         this.add(reset);
         this.addSeparator();
         this.add(grid);
         this.add(showArrows);
         this.addSeparator();
         this.add(focus);
         this.addSeparator();
         this.add(slider);
         this.add(settings);
         
         slider.setValue(pModel.getZoom());
         grid.setSelected(pModel.isState(GRID));
         showArrows.setSelected(pModel.isState(SHOW_ARROWS_ALWAYS));
     }
     
     
     private void setToolTips()
     {
         new_.setToolTipText(LOCALIZER.getString("TT_NEW"));
         open.setToolTipText(LOCALIZER.getString("TT_OPEN"));
         save.setToolTipText(LOCALIZER.getString("TT_SAVE"));
         play.setToolTipText(LOCALIZER.getString("TT_PLAY"));
         pause.setToolTipText(LOCALIZER.getString("TT_PAUSE"));
         reset.setToolTipText(LOCALIZER.getString("TT_RESET"));
         grid.setToolTipText(LOCALIZER.getString("TT_GRID"));
         showArrows.setToolTipText(LOCALIZER.getString("TT_SHOW_ARROWS"));
         focus.setToolTipText(LOCALIZER.getString("TT_FOCUS"));
         slider.setToolTipText(LOCALIZER.getString("TT_ZOOM"));
         settings.setToolTipText(LOCALIZER.getString("TT_SETTINGS"));
     }
     
     
     @Override
     public void actionPerformed(ActionEvent e)
     {
         switch (e.getActionCommand())
         {
             case CMD_NEW:
                 pModel.setScene(new Scene());
                 pModel.setZoom(1.0);
                 pModel.setViewOffset(0, 0);
                 pModel.fireRepaintEvents();
                 break;
                 
             case CMD_OPEN:
                 loadScene();
                 break;
                 
             case CMD_SAVE:
                 saveScene();
                 break;
                 
             case CMD_PLAY:
                 pModel.setState(RUN_PHYSICS, true);
                 break;
                 
             case CMD_PAUSE:
                 pModel.setState(RUN_PHYSICS, false);
                 break;
                 
             case CMD_RESET:
                 pModel.restoreScene();
                 reset.setEnabled(false);
                 pModel.fireRepaintEvents();
                 break;
                 
             case CMD_GRID:
                 pModel.toggleState(GRID);
                 grid.setSelected(pModel.isState(GRID));
                 pModel.fireRepaintEvents();
                 break;
                 
             case CMD_SHOW_ARROWS:
                 pModel.toggleState(SHOW_ARROWS_ALWAYS);
                 showArrows.setSelected(pModel.isState(SHOW_ARROWS_ALWAYS));
                 break;
                 
             case CMD_FOCUS:
                 pModel.setViewOffset(0, 0);
                 pModel.fireRepaintEvents();
                 break;
                 
             case CMD_SETTINGS:
                 if (SettingsDialog.showDialog((Window) this.getTopLevelAncestor()))
                 {
                     pModel.setProperty(LANGUAGE_CODE, Configuration.getInstance().getProperty(LANGUAGE_CODE));
                     pModel.setState(DBLCLICK_SHOW_PROPERTIES, Configuration.getInstance().isState(DBLCLICK_SHOW_PROPERTIES));
                 }
                 break;
         }
     }
     
     
     @Override
     public void stateChanged(ChangeEvent e)
     {
         pModel.setZoom(slider.getValue(), new Point(pModel.getCanvasWidth() / 2, pModel.getCanvasHeight() / 2));
         pModel.fireRepaintEvents();
     }
     
     
     @Override
     public void stateChanged(String id, boolean value)
     {
         switch (id)
         {
             case RUN_PHYSICS:
                 if (value)
                 {
                     new_.setEnabled(false);
                     open.setEnabled(false);
                     save.setEnabled(false);
                     
                     play.setEnabled( false );
                     pause.setEnabled( true );
                     
                     reset.setEnabled(false);
                     
                     // store scene copy, to enable reset function
                     // TODO - disabled, because it does not work
                     pModel.storeScene();
                     
                     pModel.getPhysicsState().start();
                 }
                 else
                 {
                     new_.setEnabled(true);
                     open.setEnabled(true);
                     save.setEnabled(true);
                     
                     pause.setEnabled( false );
                     play.setEnabled(   true );
                     pModel.getPhysicsState().pause();
                     
                     if (pModel.hasStoredScene())
                     {
                         reset.setEnabled(true);
                     }
                 }
                 
                 break;
                 
             case GRID:
                 grid.setSelected(value);
                 break;
         }
     }
     
     @Override
     public void propertyChanged(String id, String value)
     {
         if (id.equals(LANGUAGE_CODE))
         {
             setToolTips();
         }
     }
     
     
     @Override
     public void offsetChanged(int offsetX, int offsetY) { }
     
     @Override
     public void sizeChanged(int width, int height) { }
     
     @Override
     public void zoomChanged(double zoom)
     {
         slider.setValue(pModel.getZoom());
     }
     
     
     private void saveScene()
     {
         File stdSceneDir = new File("scenes");
         
         if (!stdSceneDir.exists())
         {
             stdSceneDir.mkdir();
         }
         
         UIManager.put("FileChooser.cancelButtonText", LOCALIZER.getString("CANCEL"));
         UIManager.put("FileChooser.cancelButtonToolTipText", LOCALIZER.getString("CANCEL"));
         UIManager.put("FileChooser.saveButtonText", LOCALIZER.getString("SAVE"));
         UIManager.put("FileChooser.saveButtonToolTipText", LOCALIZER.getString("SAVE"));
         
         JFileChooser dlgSave = new JFileChooser("scene");
         dlgSave.setCurrentDirectory(stdSceneDir);
         dlgSave.setSelectedFile(new File("scene.scnx"));
         dlgSave.setFileFilter(SCENE_FILTER);
         dlgSave.setDialogTitle(LOCALIZER.getString("TITLE_SAVE"));
         
         if (dlgSave.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
         {
             XMLWriter writer = new XMLWriter(dlgSave.getSelectedFile());
             
             Scene scene = pModel.getScene();
             
             writer.writeDeclaration();
             writer.writeStartElement("Scene");
             writer.writeAttribute("gravitation", "9.81"); // missing property
             
             if (scene.getGround() != null)
             {
                 writer.writeStartElement("Ground");
                 writer.writeAttribute("watermark", "" + scene.getGround().watermark);
                 writer.writeEndElement();
             }
             
             for (ObjectProperties object : scene.getObjects())
             {
                 if (object instanceof Circle)
                 {
                     writer.writeStartElement("Circle");
                 }
                 else if (object instanceof Square)
                 {
                     writer.writeStartElement("Square");
                 }
                 
 //            writer.writeAttribute("id", "" + object.getId()); // not possible to set, if a file is loaded
                 writer.writeAttribute("name", ((ISelectable) object).getName());
                 writer.writeAttribute("material", "" + object.surface);
                 writer.writeAttribute("x", "" + object.getPosition().getX());
                 writer.writeAttribute("y", "" + object.getPosition().getY());
                 writer.writeAttribute("vx", "" + object.velocity.getX());
                 writer.writeAttribute("vy", "" + object.velocity.getY());
                 writer.writeAttribute("mass", "" + object.getMass());
                 writer.writeAttribute("radius", "" + object.getRadius());
                 writer.writeAttribute("pinned", object.isPinned ? "true" : "false");
                 
                 writer.writeEndElement();
             }
             
             writer.writeEndElement();
             writer.close();
         }
     }
     
     
     private void loadScene()
     {
         File stdSceneDir = new File("scenes");
         
         if (!stdSceneDir.exists())
         {
             stdSceneDir.mkdir();
         }
         
         UIManager.put("FileChooser.cancelButtonText", LOCALIZER.getString("CANCEL"));
         UIManager.put("FileChooser.cancelButtonToolTipText", LOCALIZER.getString("CANCEL"));
         UIManager.put("FileChooser.openButtonText", LOCALIZER.getString("OPEN"));
         UIManager.put("FileChooser.openButtonToolTipText", LOCALIZER.getString("OPEN"));
         
         JFileChooser dlgOpen = new JFileChooser("scene");
         dlgOpen.setCurrentDirectory(stdSceneDir);
         dlgOpen.setSelectedFile(new File("scene.scnx"));
         dlgOpen.setFileFilter(SCENE_FILTER);
         dlgOpen.setDialogTitle(LOCALIZER.getString("TITLE_OPEN"));
         
         if (dlgOpen.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
         {
             XMLReader reader = new XMLReader(dlgOpen.getSelectedFile());
             
             if (reader.getNode("Scene") != null)
             {
                 Scene scene = new Scene();
                 
                 ObjectProperties object = null;
                 Element node = reader.getNode("Scene/Ground");
                 
                 if (node != null)
                 {
                     scene.setGround(new Ground(pModel, getInt(node.getAttribute("watermark"))));
                 }
                 
                 for (Element obj : reader.getNodes("Scene/Circle | Scene/Square"))
                 {
                     switch (obj.getName())
                     {
                         case "Circle":
                             object = new Circle(pModel, new Vector(getDouble(obj.getAttribute("x")), getDouble(obj.getAttribute("y"))), 
                                     getDouble(obj.getAttribute("radius")));
                             break;
                             
                         case "Square":
                             object = new Square(pModel, new Vector(getDouble(obj.getAttribute("x")), getDouble(obj.getAttribute("y"))), 
                                     getDouble(obj.getAttribute("radius")));
                             break;
                             
                         default:
                             continue;
                     }
                     
                     ((ISelectable) object).setName(obj.getAttribute("name"));
                     object.setMass(getDouble(obj.getAttribute("mass")));
                     object.velocity = new Vector(getDouble(obj.getAttribute("vx")), getDouble(obj.getAttribute("vy")));
                     
                     String strMat = obj.getAttribute("material");
                     for (Material mat : Material.values())
                     {
                         if (mat.toString().equals(strMat))
                         {
                             object.surface = mat;
                             break;
                         }
                     }
                     
                     object.isPinned = "true".equals(obj.getAttribute("pinned"));
                     
                     scene.add(object);
                 }
                 
                 pModel.setScene(scene);
                 pModel.fireRepaintEvents();
             }
         }
     }
     
     
     private double getDouble(String value)
     {
         if (value == null)
         {
             return 0.0;
         }
         
         return Double.parseDouble(value);
     }
     
     
     private int getInt(String value)
     {
         if (value == null)
         {
             return 0;
         }
         
         return Integer.parseInt(value);
     }
 }
