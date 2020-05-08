 /*
  * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
  *     eugen
  */
 package org.nuxeo.ide.studio.data.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.nuxeo.ide.studio.StudioContentProvider;
 import org.nuxeo.ide.studio.StudioPlugin;
 import org.nuxeo.ide.studio.data.Tree;
 import org.nuxeo.ide.studio.data.TreeImpl;
 
 
 /**
  * @author eugen
  *
  */
 public class FeatureHelper {
 
 
     public static Tree buildFeatureTree(String projectName){
         StudioContentProvider provider = StudioPlugin.getDefault().getProvider();
 
         Group[] groups = provider.getProject(projectName).getFeatureTypes();
         Feature[] features = provider.getFeatures(projectName);
 
 
         Tree root = new TreeImpl("root", "root", "Root", null);
         Map<String, Group> groupMap = new HashMap<String, Group>();
         Map<String, VirtualGroup> vgroupMap = new HashMap<String, VirtualGroup>();
 
         for ( Group group : groups) {
             if ( group.isGlobal() ){
                 VirtualGroup vg =(VirtualGroup) vgroupMap.get(group.getLabel());
                 if (vg == null) {
                     vg = new VirtualGroup(group.getLabel());
                     root.getChildren().add(vg);
                     vgroupMap.put(group.getLabel(), vg);
                 }
                 vg.getSubgroups().add(group.getId());
             } else {
                 root.getChildren().add(group);
             }
             groupMap.put(group.getId(), group);
         }
 
         for ( Feature feature : features) {
             Group group = groupMap.get(feature.getType());
             if ( group  != null ){
                 if ( group.isGlobal() ) {
                    feature.setLabel(group.getName());
                    VirtualGroup vg = vgroupMap.get(group.getLabel());
                    vg.getChildren().add(feature);
                 } else {
                     group.getChildren().add(feature);
                 }
             } else {
                 root.getChildren().add(feature);
             }
         }
         return root;
     }
 
     protected static Map<String, String> group2category = new HashMap<String, String>();
     static {
         group2category.put("Life Cycles",           "Content Model");
         group2category.put("Structure Templates",   "Content Model");
         group2category.put("Documents",             "Content Model");
         group2category.put("Schemas",               "Content Model");
 
         group2category.put("Content Views",         "Search And Listings");
         group2category.put("Virtual Navigations",   "Search And Listings");
         group2category.put("Advanced Search",       "Search And Listings");
 
         group2category.put("Event Handlers",        "Automation");
         group2category.put("User Actions",          "Automation");
         group2category.put("Automation Chains",     "Automation");
 
         group2category.put("Mail Templates",        "Templates");
         group2category.put("Document Templates",    "Templates");
 
         group2category.put("Users and Groups",      "Roles and Permisions");
         group2category.put("Permissions",           "Roles and Permisions");
 
         group2category.put("XML Extension",           "Advanced Settings");
         group2category.put("Web Services Filtering",  "Advanced Settings");
         group2category.put("Deployment Extensions",   "Advanced Settings");
     }
 
     private static String getGroupCategory(String groupName) {
         String cat = group2category.get(groupName);
         if ( cat != null ) {
             return cat;
         }
         return "default";
     }
 
 
 
 
 }
