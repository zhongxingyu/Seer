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
 package org.nuxeo.ide.studio.data.mock;
 
 
 import org.nuxeo.ide.studio.data.Tree;
 import org.nuxeo.ide.studio.data.TreeImpl;
 import org.nuxeo.ide.studio.data.model.Category;
 import org.nuxeo.ide.studio.data.model.Feature;
 import org.nuxeo.ide.studio.data.model.Group;
 
 /**
  * @author eugen
  *
  */
 public class TreeFactory {
 
     private static final TreeFactory instance = new TreeFactory();
     private TreeFactory(){ }
 
     public static TreeFactory getInstance() {
         return instance;
     }
 
     public Tree buildTestTree() {
         Tree root = new TreeImpl("root", "root", "Root", null);
         addCategories(root);
         return root;
     }
 
     /**
      * @param root
      */
     private void addCategories(Tree root) {
         String rootId = root.getId();
         for ( int i = 0 ; i < 5 ; i++ ) {
             Category category = new Category(rootId + "/cat" + i  , "Category " + i);
             addGroups(category);
             root.getChildren().add(category);
         }
     }
 
     /**
      * @param category
      */
     private void addGroups(Tree category) {
         String id = category.getId();
         for ( int i = 0 ; i < 3 ; i++ ) {
             Group group = new Group(id + "/group" + i  , "Group " + i);
             addFeatures(group);
             category.getChildren().add(group);
         }
 
     }
 
     /**
      * @param group
      */
     private void addFeatures(Group group) {
         String id = group.getId();
         for ( int i = 0 ; i < 4 ; i++ ) {
            Feature feature = new Feature(id + "/feature" + i , "feature", "http://www.google.com");
             group.getChildren().add(feature);
         }
     }
 
 }
