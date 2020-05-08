 package com.planetarypvp.bulka.loader;
 
 import com.planetarypvp.bulka.loader.Configurable;
 import com.planetarypvp.bulka.loader.ConfigurableClass;
 import com.planetarypvp.bulka.loader.LoadClassException;
 import com.planetarypvp.bulka.loader.LoadSettingsException;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class BukkitLoader
 {
     private static Map<String, YamlConfiguration> configs = new HashMap<>();
 
     public static void load(Plugin plugin, String settings) throws LoadSettingsException
     {
         File settingsDirectory = getSettingsDirectory(plugin, settings);
         load(settingsDirectory);
     }
 
     private static File getSettingsDirectory(Plugin plugin, String settings)
     {
         return new File(plugin.getDataFolder(), settings);
     }
 
     private static Map<String, YamlConfiguration> getConfigs(File settingsDirectory) throws IOException, InvalidConfigurationException
     {
         Map<String, YamlConfiguration> configs = new HashMap<>();
 
         for(File f : settingsDirectory.listFiles())
         {
             System.out.println("Config file: " + f.getName());
             String fileName = f.getName();
 
            if(!fileName.endsWith(".yml"))
                 continue;
 
             if(f.isDirectory())
                 getConfigs(f);
             else
             {
                 YamlConfiguration config = new YamlConfiguration();
                 config.load(f);
 
                 if(config.get("class") != null)
                     configs.put((String) config.get("class"), config);
             }
         }
 
         return configs;
     }
 
     public static void load(File settingsDirectory) throws LoadSettingsException
     {
         if(!settingsDirectory.exists())
             throw new LoadSettingsException("Specified settings directory does not exist.");
 
 
         try {
             configs = getConfigs(settingsDirectory);
         } catch (IOException e) {
             e.printStackTrace();
             throw new LoadSettingsException("Could not access a file in the settings directory");
         } catch (InvalidConfigurationException e) {
             e.printStackTrace();
             throw new LoadSettingsException("Invalid configuration found. " + e.getMessage());
         }
     }
 
     public static void load(String settings) throws LoadSettingsException
     {
         load(new File(settings));
     }
 
     public static void load(ConfigurableClass configurableObject)
     {
         Class<? extends ConfigurableClass> confClass = configurableObject.getClass();
         String error = "Class " + configurableObject.getClass().getName() + " was not previously loaded into memory. Load method must first be called with a specified settings directory.";
 
         if(!configs.containsKey(confClass.getName()))
         {
             configurableObject.loadDefaults(error);
             return;
         }
 
         YamlConfiguration config = configs.get(confClass.getName());
         ArrayList<Method> annotatedMethods = getAnnotatedMethods(confClass);
 
         try {
             loadMethods(configurableObject, config, annotatedMethods);
         } catch (LoadClassException e) {
             e.printStackTrace();
             configurableObject.loadDefaults(error);
         }
     }
 
     private static void loadMethods(ConfigurableClass configurableObject, YamlConfiguration config, ArrayList<Method> annotatedMethods) throws LoadClassException
     {
         for(Method m : annotatedMethods)
         {
             loadMethod(configurableObject, config, m);
         }
     }
 
     private static void loadMethod(ConfigurableClass configurableObject, YamlConfiguration config, Method method) throws LoadClassException
     {
         String key = getKeyFromMethod(method.getName());
         Object value = config.get(key);
         Class<?> param = method.getParameterTypes()[0];
         String error = "Could not load method " + method.getName();
 
         if(value instanceof ArrayList && ConfigurationSerializable.class.isAssignableFrom(param.getComponentType()))
         {
             try {
                 applyMapList(configurableObject, config, method, key);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new LoadClassException(error);
             }
         }
         else if(value instanceof ArrayList)
         {
             try {
                 applyList(configurableObject, config, method, key);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new LoadClassException(error);
             }
         }
         else if(value instanceof MemorySection)
         {
             try {
                 applyMap(configurableObject, config, method, key);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new LoadClassException(error);
             }
         }
         else
         {
             try {
                 apply(configurableObject, config, method, key);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new LoadClassException(error);
             }
         }
     }
 
     private static void apply(ConfigurableClass configurableObject, YamlConfiguration config, Method method, String key) throws InvocationTargetException, IllegalAccessException
     {
         method.invoke(configurableObject, (Object) config.get(key));
     }
 
     private static void applyMap(ConfigurableClass configurableObject, YamlConfiguration config, Method method, String key) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
     {
         MemorySection mem = (MemorySection) config.get(key);
         Class<?> paramType = method.getParameterTypes()[0];
         Constructor constructor = paramType.getConstructor(Map.class);
         constructor.setAccessible(true);
         Map map = (Map) mem.getValues(true);
         ConfigurationSerializable serializable = (ConfigurationSerializable) constructor.newInstance(map);
         method.invoke(configurableObject, paramType.cast(serializable));
     }
 
     private static void applyList(ConfigurableClass configurableObject, YamlConfiguration config, Method method, String key) throws InvocationTargetException, IllegalAccessException
     {
         Class<?> paramType = method.getParameterTypes()[0];
         method.invoke(configurableObject, paramType.cast(listToArray(config.getList(key), paramType)));
     }
 
     private static void applyMapList(ConfigurableClass configurableObject, YamlConfiguration config, Method method, String key) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
     {
         List<?> list = config.getList(key);
         List<Map<String, Object>> maps = new ArrayList<>();
         Class<?> paramType = method.getParameterTypes()[0];
         List<ConfigurationSerializable> deserializeds = new ArrayList<>();
         Class<?> configSerializableClass = paramType.getComponentType();
         Constructor constructor = null;
         constructor = configSerializableClass.getConstructor(Map.class);
 
         for(Object o : list)
         {
             Map map = (Map) o;
             maps.add(map);
         }
 
         for(Map map : maps)
         {
             deserializeds.add((ConfigurationSerializable) constructor.newInstance(map));
         }
 
         method.invoke(configurableObject, paramType.cast(listToArray(deserializeds, paramType)));
     }
 
     private static String getKeyFromMethod(String methodName)
     {
         return methodName.substring(3);
     }
 
     private static ArrayList<Method> getAnnotatedMethods(Class<? extends ConfigurableClass> configurableClass)
     {
         ArrayList<Method> annotated = new ArrayList<>();
 
         for(Method m : configurableClass.getMethods())
         {
             if(m.isAnnotationPresent(Configurable.class))
                 annotated.add(m);
         }
 
         return annotated;
     }
 
     private static Object listToArray(List<?> list, Class<?> arrayType)
     {
         Object array = Array.newInstance(arrayType.getComponentType(), list.size());
         System.arraycopy(list.toArray(), 0, array, 0, list.size());
         return array;
     }
 }
