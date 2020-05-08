 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.netbeans.modules.javafx.debugger.variablesfiltering;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.netbeans.api.debugger.jpda.ClassVariable;
 import org.netbeans.api.debugger.jpda.Field;
 import org.netbeans.api.debugger.jpda.JPDAClassType;
 import org.netbeans.api.debugger.jpda.LocalVariable;
 import org.netbeans.api.debugger.jpda.This;
 import org.netbeans.spi.debugger.DebuggerServiceRegistration;
 import org.netbeans.spi.viewmodel.ModelListener;
 import org.netbeans.spi.viewmodel.TreeModel;
 import org.netbeans.spi.viewmodel.TreeModelFilter;
 import org.netbeans.spi.viewmodel.UnknownTypeException;
 
 /**
  *
  * @author Michal Skvor
  */
 @DebuggerServiceRegistration( path="netbeans-JPDASession/FX/LocalsView",types={ org.netbeans.spi.viewmodel.TreeModelFilter.class } )
 public class JavaFXVariablesFilter implements TreeModelFilter {
 
     /** Creates new JavaFXVariablesFilter instance */
     public JavaFXVariablesFilter() {}
 
     /**
      *
      * Returns filtered root of hierarchy.
      *
      * @param   original the original tree model
      * @return  filtered root of hierarchy
      */
     public Object getRoot( TreeModel original ) {
         return original.getRoot();
     }
 
     public Object[] getChildren( TreeModel original, Object parent, int from, int to ) throws UnknownTypeException {
         Object[] visibleChildren = null;
 
         if( parent.equals( original.getRoot())) {
             // Retrieve children
             int parentChildrenCount = original.getChildrenCount( parent );
             Object[] children = original.getChildren( parent, 0, parentChildrenCount );
             parentChildrenCount = children.length;
             List vc = new ArrayList();
             for( int j = 0; j < parentChildrenCount; j++ ) {
                 Object child = children[j];
                 if( child instanceof JPDAClassType || child instanceof This ) {
                     Object[] ch = original.getChildren( child, from, to );
                     for( int i = 0; i < ch.length; i++ ) {
                         Object obj = ch[i];
     //                    System.out.println(" - " + obj );
                         if( obj instanceof ClassVariable ) {
                             ClassVariable cv = (ClassVariable)obj;
     //                        vc.add( obj );
                         } else if( obj instanceof Field ) {
                             Field f = (Field)obj;
                             if( f.getName().startsWith( "$" )) {
                                 vc.add( obj );
                             }
                         }
 
                     }
                 } else if( child instanceof LocalVariable ) {
                     LocalVariable local = (LocalVariable)child;
                     // Helper Sequences out
                     if( local.getDeclaredType().startsWith( "com.sun.javafx.runtime." )) continue;
                     if( local.getName().endsWith( "$ind" )) continue;
                     if( local.getName().endsWith( "$limit" )) continue;
                     // Skip all internal jfx$ variables
                     if( local.getName().startsWith( "jfx$" )) continue;
                     vc.add( child );
                 }
             }
             List<Object> vvv = vc.subList( from, to > vc.size() ? vc.size() : to );
             for( Object o : vvv ) {
                 System.out.println(" - " + o );
             }
             return vc.subList( from, to > vc.size() ? vc.size() : to ).toArray();
         } else {
             // Root static class
             Object[] children = original.getChildren( parent, from, to );
             List vc = new ArrayList();
             for( int i = 0; i < children.length; i++ ) {
                 Object child = children[i];
                 if( child instanceof Field ) {
                     Field f = (Field)child;
                     if( f.getName().startsWith( "$" )) {
                         vc.add( child );
                     }
                 } else {
                     System.out.println(" - " + child.toString());
                 }
             }
             visibleChildren = vc.subList( from, to > vc.size() ? vc.size() : to ).toArray();
         }
         return visibleChildren;
     }
 
     public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        int countVisible = 0;

        // For root
        if (node.equals( original.getRoot())) {
            countVisible = original.getChildrenCount( node );
            Object[] children = original.getChildren( node, 0, countVisible );
            countVisible = children.length;
            if( countVisible == 1 && children[0] instanceof JPDAClassType ) {
                return original.getChildrenCount( children[0] );
            }
        } else {
            countVisible = original.getChildrenCount( node );
        }

        return countVisible;
     }
 
     public boolean isLeaf( TreeModel original, Object node ) throws UnknownTypeException {
         boolean il;
 
         if( node instanceof Field ) {
             Field f = (Field)node;
             if( "java.lang.String".equals( f.getType())) {
                 return true;
             }
         }
         il = original.isLeaf( node );
 
         return il;
     }
 
     public void addModelListener( ModelListener l ) {
     }
 
     public void removeModelListener( ModelListener l ) {
     }
 
 }
