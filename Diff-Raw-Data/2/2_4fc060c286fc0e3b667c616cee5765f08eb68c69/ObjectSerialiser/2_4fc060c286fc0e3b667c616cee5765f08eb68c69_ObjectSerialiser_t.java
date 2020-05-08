 package tk.manf.serialisation;
 
 import java.io.File;
 import java.io.IOException;
 import tk.manf.serialisation.annotations.Unit;
 import tk.manf.serialisation.annotations.Property;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.plugin.java.JavaPlugin;
 import tk.manf.serialisation.annotations.Identification;
 import tk.manf.serialisation.handler.SerialisationHandler;
 
 public final class ObjectSerialiser {
     private File dataFolder;
     //Still don't like SuppressWarnings comparable with Warnings
     @SuppressWarnings("NonConstantLogger")
     private final Logger logger;
 
     /**
      * Initialises Object Serialiser
      * @param plugin 
      */
     public ObjectSerialiser(JavaPlugin plugin) {
         this.dataFolder = plugin.getDataFolder();
         this.logger = plugin.getLogger();
     }
 
     /**
      * 
      * @param o
      * @throws IllegalArgumentException
      * @throws IllegalAccessException
      * @throws IOException
      * @throws SerialisationException 
      */
     public void save(Object o) throws IllegalArgumentException, IllegalAccessException, IOException, SerialisationException {
         save(o, false);
     }
 
     /**
      * 
      * @param o
      * @param warn
      * @throws IllegalArgumentException
      * @throws IllegalAccessException
      * @throws IOException
      * @throws SerialisationException 
      */
     public void save(Object o, boolean warn) throws IllegalArgumentException, IllegalAccessException, IOException, SerialisationException {
         Unit unit = o.getClass().getAnnotation(Unit.class);
         if (unit == null) {
             return;
         }
         SerialisationHandler handler = unit.handler().getHandler();
         String id = null;
         for (Field f : o.getClass().getDeclaredFields()) {
             f.setAccessible(true);
             if (!unit.isStatic() && id == null) {
                 if (f.getAnnotation(Identification.class) != null) {
                     id = f.get(o).toString();
                     if (!Modifier.isFinal(f.getModifiers())) {
                        logger.log(Level.INFO, "Identification {0} is not final! NAG Developers", f.getName());
                     }
                 } else {
                     throw new SerialisationException("Identification not found! Found " + f.getName());
                 }
             }
             Property prop = f.getAnnotation(Property.class);
             /*
              * NASTY BLUB CODE - Gonna rewrite this!
              */
             if (prop == null) {
                 if (warn) {
                     logger.log(Level.INFO, "Field {0} is no Propertie({1})", new Object[]{f.getName(), f.getClass().getName()});
                 }
                 continue;
             }
 
             if (warn) {
                 if (prop.name().length() == 0) {
                     logger.log(Level.INFO, "Field {0} has no name. Using field name!", f.getName());
                 }
             }
             /*
              * END NASTY CODE
              */
             handler.save(unit, dataFolder, id, prop.name().length() == 0 ? f.getName() : prop.name(), f.get(o));
             f.setAccessible(false);
         }
         handler.save(unit, dataFolder, id);
     }
 }
