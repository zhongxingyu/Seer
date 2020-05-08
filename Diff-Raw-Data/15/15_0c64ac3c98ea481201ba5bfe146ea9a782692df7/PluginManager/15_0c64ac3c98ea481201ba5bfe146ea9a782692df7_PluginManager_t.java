 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.pluginsystem;
 
 import com.objectgraph.core.Constraint;
 import com.objectgraph.core.Node;
 import com.objectgraph.pluginsystem.exceptions.PluginManagerAlreadyInitializedException;
 import com.objectgraph.pluginsystem.exceptions.PluginManagerNotInitializedException;
 import com.objectgraph.utils.ClassUtils;
import javafx.fxml.FXMLLoader;
 import org.reflections.Reflections;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.*;
 
 
 public class PluginManager {
 
     private static PluginConfiguration configuration = null;
    private static ClassLoader classLoader = FXMLLoader.getDefaultClassLoader();
     private static Reflections internal = null;
 
     private PluginManager() {
         // you should not instantiate a PluginManager
     }
 
     public static void initialise(PluginConfiguration conf) {
         if (configuration != null)
             throw new PluginManagerAlreadyInitializedException();
 
         configuration = Node.getKryo().copy(conf);
         prepare();
     }
 
     public static PluginConfiguration getConfiguration() {
         if (configuration == null)
             throw new PluginManagerNotInitializedException();
 
         return Node.getKryo().copy(configuration);
     }
 
     public static ClassLoader getClassLoader() {
         return classLoader;
     }
 
     private static void prepare() {
         Set<URL> urls = new HashSet<>();
         for (File file : configuration.libraries) {
             if (file == null || !file.exists())
                 continue;
             try {
                 URL url = file.toURI().toURL();
                 urls.add(url);
             } catch (MalformedURLException e) {
                 // Do not do anything
             }
         }
        classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), classLoader);
         Set<String> packages = new HashSet<>();
         for (String p : configuration.packages) {
             if (p != null)
                 packages.add(p);
         }
         internal = new Reflections(classLoader, packages.toArray(new String[packages.size()]));
     }
 
     public static <T> List<T> getImplementations(Class<T> baseType, List<Constraint<?, ?>> constraints) {
         List<T> ret = new ArrayList<>();
 
         Set<Class<? extends T>> types = internal.getSubTypesOf(baseType);
         types.add(baseType);
 
         for (Class<? extends T> type : types) {
             if (ClassUtils.isImplementation(type)) {
                 try {
                     T obj = type.newInstance();
                     ret.add(obj);
                 } catch (InstantiationException | IllegalAccessException e) {
                     // Do not do anything: the class does not have an empty constructor
                 }
             }
         }
 
         for (Constraint<?, ?> c : constraints)
             c.filter((List) ret);
 
         Collections.sort(ret, new Comparator<T>() {
             @Override
             public int compare(T o1, T o2) {
                 return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
             }
         });
 
         return ret;
     }
 
 
 }
