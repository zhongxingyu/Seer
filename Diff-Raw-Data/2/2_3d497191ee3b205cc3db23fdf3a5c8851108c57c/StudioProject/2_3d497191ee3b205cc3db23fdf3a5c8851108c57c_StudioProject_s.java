 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.connect.studio;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonParser.Feature;
 import org.codehaus.jackson.JsonToken;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.nuxeo.ide.connect.ConnectPreferences;
 import org.nuxeo.ide.connect.studio.tree.ProjectTree;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class StudioProject implements Comparable<StudioProject> {
 
     public static final StudioFeature[] EMPTY_FEATURES = new StudioFeature[0];
 
     public static final DocumentSchema[] EMPTY_SCHEMAS = new DocumentSchema[0];
 
     public static final DocumentType[] EMPTY_DOCTYPES = new DocumentType[0];
 
     protected String id;
 
     protected String name;
 
     protected String targetVersion;
 
     protected Map<String, Map<String, StudioFeature>> features;
 
     protected Map<String, TypeDescriptor> types;
 
     protected Map<String, String> categories;
 
     protected TargetPlatform platform;
 
     private StudioProject() {
         this.features = new HashMap<String, Map<String, StudioFeature>>();
         this.types = new HashMap<String, StudioProject.TypeDescriptor>();
         this.categories = new HashMap<String, String>();
     }
 
     public StudioProject(String id) {
         this();
         this.id = id;
     }
 
     protected void setId(String id) {
         this.id = id;
     }
 
     public String getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public TargetPlatform getPlatform() {
         return platform;
     }
 
     public void setPlatform(TargetPlatform platform) {
         this.platform = platform;
     }
 
     public void setTargetVersion(String targetVersion) {
         this.targetVersion = targetVersion;
     }
 
     public String getTargetVersion() {
         return targetVersion;
     }
 
     public Map<String, Map<String, StudioFeature>> getFeaturesMap() {
         return features;
     }
 
     public StudioFeature[] getFeatures() {
         ArrayList<StudioFeature> result = new ArrayList<StudioFeature>();
         for (Map<String, StudioFeature> map : features.values()) {
             result.addAll(map.values());
         }
         return result.toArray(new StudioFeature[result.size()]);
     }
 
     public void addFeature(StudioFeature feature) {
         Map<String, StudioFeature> map = features.get(feature.getType());
         if (map == null) {
             map = new HashMap<String, StudioFeature>();
             features.put(feature.getType(), map);
         }
         map.put(feature.getId(), feature);
     }
 
     public StudioFeature[] getFeatures(String type) {
         Map<String, StudioFeature> map = features.get(type);
         if (map == null) {
             return EMPTY_FEATURES;
         }
         return map.values().toArray(new StudioFeature[map.size()]);
     }
 
     /**
      * Get project defined document schemas. Platform schemas are not included.
      * 
      * @return
      */
     public DocumentSchema[] getDocumentSchemas() {
         Map<String, StudioFeature> map = features.get("ds");
         if (map == null) {
             return EMPTY_SCHEMAS;
         }
         return map.values().toArray(new DocumentSchema[map.size()]);
     }
 
     /**
      * Get project defined document types. Platform document types are not
      * included
      * 
      * @return
      */
     public DocumentType[] getDocumentTypes() {
         Map<String, StudioFeature> map = features.get("doc");
         if (map == null) {
             return EMPTY_DOCTYPES;
         }
         return map.values().toArray(new DocumentType[map.size()]);
     }
 
     public StudioFeature getFeature(String type, String id) {
         Map<String, StudioFeature> map = features.get(type);
         if (map == null) {
             return null;
         }
         return map.get(id);
     }
 
     public Map<String, TypeDescriptor> getTypeDescriptors() {
         return types;
     }
 
     public void addTypeDescriptor(TypeDescriptor td) {
         types.put(td.id, td);
     }
 
     public Map<String, String> getCategories() {
         return categories;
     }
 
     public void putCategory(String id, String label) {
         categories.put(id, label);
     }
 
     public TypeDescriptor getTypeDescriptor(String id) {
         return types.get(id);
     }
 
     public String getCategoryLabel(String id) {
         String label = categories.get(id);
         return label != null ? label : id;
     }
 
     public String getUrl() {
         try {
             String host = ConnectPreferences.load().getHost();
             int i = host.lastIndexOf("/studio");
             if (i > -1) {
                 return host.substring(0, i + "/studio".length())
                        + "/ide?projectId=" + getId();
             }
         } catch (Exception e) {
             e.printStackTrace(); // TODO
         }
         return null;
     }
 
     public ProjectTree getTree() {
         return new ProjectTree(this);
     }
 
     @Override
     public String toString() {
         return id;
     }
 
     @Override
     public int hashCode() {
         return id.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (obj instanceof StudioProject) {
             return id.equals(((StudioProject) obj).id);
         }
         return false;
     }
 
     @Override
     public int compareTo(StudioProject o) {
         return name.compareTo(o.name);
     }
 
     public static StudioProject getProject(IProject owner) throws Exception {
         IFile file = owner.getFile("studio.project");
         if (file.exists()) {
             InputStream in = file.getContents(true);
             try {
                 return readProject(in);
             } finally {
                 in.close();
             }
         }
         return null;
     }
 
     public static List<StudioProject> readProjects(InputStream in)
             throws IOException {
         JsonFactory jsonFactory = new JsonFactory();
         jsonFactory.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
         JsonParser jp = jsonFactory.createJsonParser(in);
         return readProjects(jp);
     }
 
     public static List<StudioProject> readProjects(JsonParser jp)
             throws IOException {
         if (jp.nextToken() != JsonToken.START_ARRAY) {
             throw new IOException(
                     "Invalid JSON project list format. Expecting array start.");
         }
 
         ArrayList<StudioProject> projects = new ArrayList<StudioProject>();
         while (jp.nextToken() != JsonToken.END_ARRAY) {
             projects.add(readProjectEntry(jp));
         }
         return projects;
     }
 
     public static StudioProject readProject(InputStream in) throws IOException {
         JsonFactory jsonFactory = new JsonFactory();
         jsonFactory.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
         JsonParser jp = jsonFactory.createJsonParser(in);
         return readProject(jp);
     }
 
     public static StudioProject readProject(JsonParser jp) throws IOException {
         if (jp.nextToken() != JsonToken.START_OBJECT) {
             throw new IOException(
                     "Invalid JSON project format. Expecting object start.");
         }
         return readProjectEntry(jp);
     }
 
     private static StudioProject readProjectEntry(JsonParser jp)
             throws IOException {
         StudioProject project = new StudioProject();
         while (jp.nextToken() != JsonToken.END_OBJECT) {
             String key = jp.getCurrentName();
             jp.nextToken();
             if (key.equals("id")) {
                 project.id = jp.getText();
             } else if (key.equals("name")) {
                 project.name = jp.getText();
             } else if (key.equals("targetVersion")) {
                 project.targetVersion = jp.getText();
             } else if (key.equals("features")) {
                 readFeatures(jp, project);
             } else if (key.equals("types")) {
                 readTypes(jp, project);
             } else if (key.equals("categories")) {
                 readCategories(jp, project);
             } else if (key.equals("platform")) {
                 readPlatform(jp, project);
             }
         }
 
         if (project.id == null) {
             throw new IOException(
                     "Invalid JSON definition of a studio project. Id not found");
         }
         if (project.targetVersion == null) {
             throw new IOException(
                     "Invalid JSON definition of a studio project. targetVersion not found");
         }
 
         return project;
     }
 
     private static void readFeatures(JsonParser jp, StudioProject project)
             throws IOException {
         if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
             throw new IOException(
                     "Invalid JSON content. Expecting feature groups object");
         }
 
         while (jp.nextToken() != JsonToken.END_ARRAY) {
             // we are positioned on start object
             project.addFeature(StudioFeatureType.readFeature(jp));
         }
     }
 
     private static void readTypes(JsonParser jp, StudioProject project)
             throws IOException {
         if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
             throw new IOException(
                     "Invalid JSON content. Expecting feature type list");
         }
 
         while (jp.nextToken() != JsonToken.END_ARRAY) {
             readType(jp, project);
         }
 
     }
 
     private static void readType(JsonParser jp, StudioProject project)
             throws IOException {
         if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
             throw new IOException(
                     "Invalid JSON content. Expecting type descriptor object");
         }
 
         TypeDescriptor td = new TypeDescriptor();
         while (jp.nextToken() != JsonToken.END_OBJECT) {
             String key = jp.getCurrentName();
             jp.nextToken();
             if (key.equals("id")) {
                 td.id = jp.getText();
             } else if (key.equals("category")) {
                 td.category = jp.getText();
                 if (td.category != null && td.category.length() == 0) {
                     td.category = null;
                 }
             } else if (key.equals("label")) {
                 td.label = jp.getText();
             } else if (key.equals("global")) {
                 td.global = jp.getBooleanValue();
             }
         }
 
         if (td.id == null) {
             throw new IOException(
                     "Invalid JSON project content: a type descriptor must have an id.");
         }
         if (td.label == null) {
             td.label = td.id;
         }
         project.addTypeDescriptor(td);
 
     }
 
     private static void readCategories(JsonParser jp, StudioProject project)
             throws IOException {
         if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
             throw new IOException(
                     "Invalid JSON content. Expecting category map");
         }
 
         while (jp.nextToken() != JsonToken.END_OBJECT) {
             String key = jp.getCurrentName();
             jp.nextToken();
             project.putCategory(key, jp.getText());
         }
 
     }
 
     private static void readPlatform(JsonParser jp, StudioProject project)
             throws IOException {
         TargetPlatform platform = new TargetPlatform();
         platform.read(jp);
         project.setPlatform(platform);
     }
 
     public static class TypeDescriptor {
         public String id;
 
         public String label;
 
         public String category;
 
         public boolean global;
 
     }
 }
