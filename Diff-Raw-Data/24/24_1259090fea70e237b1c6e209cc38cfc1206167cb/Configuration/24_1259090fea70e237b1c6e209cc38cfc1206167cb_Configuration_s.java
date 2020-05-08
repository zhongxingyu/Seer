 package de.engineapp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.*;
 import java.util.Map.Entry;
 
 import de.engineapp.PresentationModel.*;
 
 import static de.engineapp.Constants.*;
 
 public class Configuration implements Serializable, Cloneable, StorageListener, ViewBoxListener
 {
     private static final long serialVersionUID = 6847314270730904825L;
     
     
     private static Configuration instance = null;
     
     private double zoom;
     private Map<String, Boolean> states;
     private Map<String, String> properties;
     
     
     private Configuration()
     {
         states = new HashMap<>();
         properties = new HashMap<>();
         
         // set default values
         zoom = 1.0;
         states.put(GRID, false);
         states.put(MAXIMIZED, false);
         states.put(DBLCLICK_SHOW_PROPERTIES, false);
         states.put(DEBUG, false);
         states.put(SHOW_ARROWS_ALWAYS, false);
         properties.put(LANGUAGE_CODE, null);
         properties.put(LOOK_AND_FEEL, null);
     }
     
     
     public static Configuration getInstance()
     {
         if (instance == null)
         {
             instance = new Configuration();
         }
         
         return instance;
     }
     
     
     // will be propably replaced by some xml file
     public static void load()
     {
         File configFile = new File("config.dat");
         
         if (configFile.exists())
         {
             try (FileInputStream   fis = new FileInputStream(configFile);
                  ObjectInputStream ois = new ObjectInputStream(fis))
             {
                 instance = (Configuration) ois.readObject();
             }
             catch (IOException | ClassNotFoundException ex)
             {
                 ex.printStackTrace();
             }
             
         }
     }
     
     
     public static void save()
     {
         try (FileOutputStream   fos = new FileOutputStream("config.dat");
              ObjectOutputStream oos = new ObjectOutputStream(fos))
         {
             oos.writeObject(getInstance());
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
         }
     }
     
     
     @Override
     public Configuration clone()
     {
         try
         {
             return (Configuration) super.clone();
         }
         catch (CloneNotSupportedException e)
         {
             e.printStackTrace();
         }
         return null;
     }
     
     
     public static void overrideInstance(Configuration newInstance)
     {
         instance = newInstance;
     }
     
     
     public void attackPresentationModel(PresentationModel model)
     {
         model.addViewBoxListener(this);
         model.addStorageListener(this);
         
         model.setZoom(zoom);
         
         for (Entry<String, Boolean> entry : states.entrySet())
         {
             model.setState(entry.getKey(), entry.getValue());
         }
         for (Entry<String, String> entry : properties.entrySet())
         {
             model.setProperty(entry.getKey(), entry.getValue());
         }
     }
     
     
     public boolean isState(String id)
     {
        return Boolean.FALSE.equals(states.get(id));
     }
     
     public void setState(String id, boolean value)
     {
         states.put(id, value);
     }
     
     
     public String getProperty(String id)
     {
         return properties.get(id);
     }
     
     public void setProperty(String id, String value)
     {
         properties.put(id, value);
     }
     
     
     public double getZoom()
     {
         return zoom;
     }
     
     public void setZoom(double zoom)
     {
         this.zoom = zoom;
     }
     
     
     @Override
     public void offsetChanged(int offsetX, int offsetY) { }
     
     @Override
     public void sizeChanged(int width, int height) { }
     
     @Override
     public void zoomChanged(double zoom)
     {
         this.zoom = zoom;
     }
     
     
     @Override
     public void stateChanged(String id, boolean value)
     {
         if (states.containsKey(id))
         {
             states.put(id, value);
         }
     }
     
     @Override
     public void propertyChanged(String id, String value)
     {
         if (properties.containsKey(id))
         {
             properties.put(id, value);
         }
     }
 }
