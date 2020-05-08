 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2010  Trejkaz, Hex Project
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
 
 package org.trypticon.hex.anno.swing;
 
 import org.jdesktop.swingx.JXTreeTable;
 import org.trypticon.hex.anno.AbstractAnnotationCollection;
 import org.trypticon.hex.anno.Annotation;
 import org.trypticon.hex.anno.AnnotationCollection;
 import org.trypticon.hex.anno.AnnotationCollectionEvent;
 import org.trypticon.hex.anno.AnnotationCollectionListener;
 import org.trypticon.hex.anno.GroupAnnotation;
 import org.trypticon.hex.anno.OverlappingAnnotationException;
 
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.tree.TreePath;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A wrapper annotation model which tracks expansion status of a tree.
  */
 public class ExpansionTrackingAnnotationCollection extends AbstractAnnotationCollection {
     private final JXTreeTable treeTable;
     private final AnnotationCollection delegate;
 
     public ExpansionTrackingAnnotationCollection(JXTreeTable treeTable, AnnotationCollection delegate) {
         this.treeTable = treeTable;
         this.delegate = delegate;
 
         treeTable.addTreeExpansionListener(new TreeExpansionListener() {
             public void treeExpanded(TreeExpansionEvent treeExpansionEvent) {
                 fireAnnotationsChanged();
             }
 
             public void treeCollapsed(TreeExpansionEvent treeExpansionEvent) {
                 fireAnnotationsChanged();
             }
         });
 
         delegate.addAnnotationCollectionListener(new AnnotationCollectionListener() {
             public void annotationsChanged(AnnotationCollectionEvent event) {
                 fireAnnotationsChanged();
             }
         });
     }
 
     public GroupAnnotation getRootGroup() {
         return delegate.getRootGroup();
     }
 
     public List<Annotation> getTopLevel() {
         return delegate.getTopLevel();
     }
 
     // TODO: Methods to get the children of a node, so that it can be intercepted.
     
     public List<Annotation> getAnnotationPathAt(long position) {
         List<Annotation> fullPath = delegate.getAnnotationPathAt(position);
        if (fullPath == null) {
            return null;
        }
 
         List<Annotation> path = new ArrayList<Annotation>(fullPath.size());
         TreePath treePath = new TreePath(delegate.getRootGroup());
 
         for (Annotation node : fullPath) {
             path.add(node);
             treePath = treePath.pathByAddingChild(node);
 
             if (treeTable.isCollapsed(treePath)) {
                 break;
             }
         }
 
         return path;
     }
 
     public void add(Annotation annotation) throws OverlappingAnnotationException {
         delegate.add(annotation);
     }
 
     public void remove(Annotation annotation) {
         delegate.remove(annotation);
     }
 }
