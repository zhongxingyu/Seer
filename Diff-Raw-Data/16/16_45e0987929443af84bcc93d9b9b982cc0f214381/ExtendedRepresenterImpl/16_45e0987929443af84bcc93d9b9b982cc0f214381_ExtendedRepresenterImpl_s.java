 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.gui.notebook;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.LinkedHashMap;
 import java.util.HashMap;
 import java.util.ArrayList;
 
 import org.jvyaml.Representer;
 import org.jvyaml.RepresenterImpl;
 import org.jvyaml.Serializer;
 import org.jvyaml.YAMLConfig;
 import org.jvyaml.YAMLNodeCreator;
 import org.jvyaml.nodes.Node;
 
 import org.trypticon.hex.anno.Interpretor;
 import org.trypticon.hex.anno.InterpretorStorage;
 import org.trypticon.hex.anno.Annotation;
 
 /**
  * Extension of the default representer to dodge some issues in the default one.
  *
  * @author trejkaz
  */
 class ExtendedRepresenterImpl extends RepresenterImpl {
     private final InterpretorStorage interpretorStorage;
 
     public ExtendedRepresenterImpl(Serializer serializer, YAMLConfig yamlConfig,
                                    InterpretorStorage interpretorStorage) {
         super(serializer, yamlConfig);
         this.interpretorStorage = interpretorStorage;
     }
 
     @Override
     protected YAMLNodeCreator getNodeCreatorFor(Object o) {
         if (o instanceof Notebook) {
             return new NotebookYAMLNodeCreator((Notebook) o);
         } else if (o instanceof Annotation) {
             return new AnnotationYAMLNodeCreator((Annotation) o);
         } else if (o instanceof Interpretor) {
             return new InterpretorYAMLNodeCreator((Interpretor) o);
         } else {
             return super.getNodeCreatorFor(o);
         }
     }
 
     private class NotebookYAMLNodeCreator implements YAMLNodeCreator {
         private final Notebook notebook;
 
         private NotebookYAMLNodeCreator(Notebook notebook) {
             this.notebook = notebook;
         }
 
         public String taguri() {
             return YamlTags.NOTEBOOK_TAG;
         }
 
         public Node toYamlNode(Representer representer) throws IOException {
             // TODO: Fix JvYAML, it is reordering my LinkedHashMap for no good reason!
             Map<String, Object> fields = new LinkedHashMap<String, Object>(2);
             fields.put("binary_location", notebook.getBinaryLocation().toExternalForm());
             fields.put("annotations", new ArrayList<Annotation>(notebook.getAnnotations().getAll()));
             return representer.map(taguri(), fields, false);
         }
     }
 
     private class AnnotationYAMLNodeCreator implements YAMLNodeCreator {
         private final Annotation annotation;
 
         private AnnotationYAMLNodeCreator(Annotation annotation) {
             this.annotation = annotation;
         }
 
         public String taguri() {
             return YamlTags.ANNOTATION_TAG;
         }
 
         public Node toYamlNode(Representer representer) throws IOException {
             Map<String, Object> fields = new LinkedHashMap<String, Object>(3);
             fields.put("position", annotation.getPosition());
             fields.put("interpretor", annotation.getInterpretor());
             fields.put("note", annotation.getNote());
             return representer.map(taguri(), fields, false);
         }
     }
 
     private class InterpretorYAMLNodeCreator implements YAMLNodeCreator {
         private String name;
         private Map<String, Object> options;
 
         private InterpretorYAMLNodeCreator(Interpretor interpretor) {
             Map<String, Object> map = interpretorStorage.toMap(interpretor);
             options = new LinkedHashMap<String, Object>(map);
             name = (String) options.remove("name");
         }
 
         public String taguri() {
             return YamlTags.INTERPRETOR_TAG_PREFIX + name;
         }
 
         public Node toYamlNode(Representer representer) throws IOException {
             return representer.map(taguri(), options, true);
         }
     }
 }
