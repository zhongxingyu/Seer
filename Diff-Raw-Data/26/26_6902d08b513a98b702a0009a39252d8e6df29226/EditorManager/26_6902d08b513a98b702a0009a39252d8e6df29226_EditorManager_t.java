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
 
 package com.objectgraph.gui;
 
 import com.objectgraph.core.Constraint;
 import com.objectgraph.core.EventRecipient;
 import com.objectgraph.core.Node;
 import com.objectgraph.core.RootedProperty;
 import com.objectgraph.pluginsystem.PluginManager;
 import javafx.event.EventHandler;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 import java.util.*;
 
 public final class EditorManager {
 
     private static final List<PropertyEditor> editors = PluginManager.getImplementations(PropertyEditor.class, Collections.<Constraint<?, ?>>emptyList());
     private static final Map<Class<?>, Class<PropertyEditor>> cachedEditors = new HashMap<>();
     private static final Map<Class<?>, Map<Class<?>, Double>> distances = new HashMap<>();
 
     private EditorManager() {
         // Utility class, no instances
     }
 
     public static void openBestEditorStage(RootedProperty model, boolean runtime, boolean wait) {
         PropertyEditor editor = getBestEditor(model, runtime);
         openEditorStage(editor, model, wait);
     }
 
     public static void openEditorStage(final PropertyEditor editor, RootedProperty model, boolean wait) {
         Stage stage = new Stage();
 
         stage.setScene(new Scene(editor));
         editor.attach(model);
         if (wait) {
             stage.showAndWait();
             editor.detach();
         } else {
             stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                 @Override
                 public void handle(WindowEvent windowEvent) {
                     editor.detach();
                 }
             });
            stage.show();
         }
     }
 
     public static PropertyEditor getBestEditor(RootedProperty model, boolean runtime) {
         return getBestEditor(model, runtime, false);
     }
 
     public static PropertyEditor getBestEditor(RootedProperty model, boolean runtime, boolean attach) {
         Class<?> valueType = model.getValueType(runtime);
 
         if (cachedEditors.containsKey(valueType)) {
             PropertyEditor cached = instantiateEditor(cachedEditors.get(valueType));
             if (cached.canEdit(model)) {
                 return attach ? cached.attach(model) : cached;
             }
         }
 
         List<PropertyEditor> editors = getEditors();
         Iterator<PropertyEditor> it = editors.iterator();
         PropertyEditor best = null;
         while (it.hasNext()) {
             PropertyEditor e = it.next();
             if (e.canEdit(model)) {
                 best = updateBestEditor(valueType, best, e);
             }
         }
 
         if (best != null) {
             cachedEditors.put(valueType, (Class<PropertyEditor>) best.getClass());
             best = instantiateEditor((Class<PropertyEditor>) best.getClass());
             if (attach) {
                 best.attach(model);
             }
         }
 
         return best;
     }
 
     private static PropertyEditor instantiateEditor(Class<PropertyEditor> type) {
         try {
             return type.newInstance();
         } catch (InstantiationException | IllegalAccessException e) {
             e.printStackTrace();
             System.exit(1);
             return null;
         }
     }
 
     private static List<PropertyEditor> getEditors() {
         return new ArrayList<>(editors);
     }
 
     private static PropertyEditor updateBestEditor(Class<?> valueType, PropertyEditor current, PropertyEditor candidate) {
         double candidateDistance = distance(valueType, candidate.getBaseEditableTypes());
         double currentDistance = current == null ? Double.POSITIVE_INFINITY : distance(valueType, current.getBaseEditableTypes());
         if (currentDistance > candidateDistance) {
             return candidate;
         } else {
             return current;
         }
     }
 
     private static double distance(Class<?> dst, Set<Class<?>> srcSet) {
         double distance = Double.POSITIVE_INFINITY;
         for (Class<?> src : srcSet) {
             double current = distance(dst, src);
             if (current < distance) {
                 distance = current;
             }
         }
         return distance;
     }
 
     private static double distance(Class<?> dst, Class<?> src) {
         if (!distances.containsKey(src)) {
             distances.put(src, new HashMap<Class<?>, Double>());
             distances.get(src).put(src, 0.0);
             distances.get(src).put(null, Double.POSITIVE_INFINITY);
         }
         if (distances.get(src).containsKey(dst)) {
             return distances.get(src).get(dst);
         } else {
             double distance = 1 + distance(dst.getSuperclass(), src);
             for (Class<?> i : dst.getInterfaces()) {
                 double current = 1.02 + distance(i, src);
                 if (current < distance) {
                     distance = current;
                 }
             }
             distances.get(src).put(dst, distance);
             return distance;
         }
     }
 
     public static void detachAllEditors(Node node) {
         Map<EventRecipient, Set<String>> parents = new HashMap<>(node.getParentPaths());
         for (EventRecipient m: parents.keySet()) {
             if (m instanceof PropertyEditor) {
                 ((PropertyEditor) m).detach();
             }
         }
     }
 }
