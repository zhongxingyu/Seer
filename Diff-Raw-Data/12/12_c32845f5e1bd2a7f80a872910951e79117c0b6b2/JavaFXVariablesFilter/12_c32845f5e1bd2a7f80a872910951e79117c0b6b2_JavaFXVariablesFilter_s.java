 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  * Other names may be trademarks of their respective owners.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 
 package org.netbeans.modules.javafx.debugger.variablesfiltering;
 
 import com.sun.javafx.jdi.FXObjectReference;
 import com.sun.javafx.jdi.FXPrimitiveValue;
 import com.sun.javafx.jdi.FXSequenceReference;
 import com.sun.javafx.jdi.FXValue;
 import com.sun.jdi.ObjectReference;
 import com.sun.jdi.ReferenceType;
 import com.sun.jdi.Value;
 import java.util.ArrayList;
 import java.util.List;
 import org.netbeans.api.debugger.jpda.ClassVariable;
 import org.netbeans.api.debugger.jpda.Field;
 import org.netbeans.api.debugger.jpda.InvalidExpressionException;
 import org.netbeans.api.debugger.jpda.JPDAClassType;
 import org.netbeans.api.debugger.jpda.LocalVariable;
 import org.netbeans.api.debugger.jpda.ObjectVariable;
 import org.netbeans.api.debugger.jpda.This;
 import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
 import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
 import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
 import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
 import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
 import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
 import org.netbeans.modules.debugger.jpda.models.AbstractObjectVariable;
 import org.netbeans.modules.debugger.jpda.models.AbstractVariable;
 import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
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
 //        System.out.println(" - " + parent );
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
 //                        System.out.println(" - " + obj );
                         if( obj instanceof ClassVariable ) {
                             ClassVariable cv = (ClassVariable)obj;
 //                            vc.add( obj );
                         } else if( obj instanceof Field ) {
                             Field f = (Field)obj;
                             vc.add( obj );
                         } else {
                             vc.add( obj );
                         }
                     }
                 } else if( child instanceof LocalVariable ) {
                     LocalVariable local = (LocalVariable)child;
                     vc.add( child );
                 }
             }
 //            List<Object> vvv = vc.subList( from, to > vc.size() ? vc.size() : to );
 //            for( Object o : vvv ) {
 //                System.out.println("    - " + o );
 //            }
             return vc.subList( from, to > vc.size() ? vc.size() : to ).toArray();
         } else {
             // Root static class
             boolean seqType = false;
             List vc = new ArrayList();
             if( parent instanceof ObjectVariable ) {
                 ObjectVariable ov = (ObjectVariable)parent;
 
                 JDIVariable jdiv = (JDIVariable)ov;
                 Value v = jdiv.getJDIValue();
                 if( v instanceof FXSequenceReference ) {
                     FXSequenceReference seq = (FXSequenceReference)v;
 
                     AbstractVariable av = (AbstractVariable)ov;
                     
                     for( int i = 0; i < seq.length(); i++ ) {
                         Value iv = seq.getValue( i );
                         
                         if( iv instanceof FXPrimitiveValue ) {
                             vc.add( new SequenceField( av.getDebugger(), 
                                    (FXPrimitiveValue) seq.getValue( i ), ov, i,
                                    v.type().toString()));
                         } else if( iv instanceof FXObjectReference ) {
                             String t = iv.type().name();
                             vc.add( new SequenceObject( av.getDebugger(),
                                     (FXObjectReference) iv, t,
                                      ov, i, 1000, v.toString()));
                         }
                     }
                     seqType = true;
                 } 
             }
             if( !seqType ) {
                 Object[] children = original.getChildren( parent, from, to );
                 for( int i = 0; i < children.length; i++ ) {
                     Object child = children[i];
                     System.out.println(" - " + child.toString());
                     if( child instanceof Field ) {
                         Field f = (Field)child;
                         vc.add( child );
                     } else {
                         vc.add( child );
 //                        System.out.println(" - " + child.toString());
                     }
                 }
             }
             visibleChildren = vc.subList( from, to > vc.size() ? vc.size() : to ).toArray();
         }
         return visibleChildren;
     }
 
     public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
         return Integer.MAX_VALUE;
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
 
     /**
      * Helper representation of Sequence
      */
     private class SequenceField extends AbstractVariable implements Field {
 
         private FXSequenceReference sequence;
         private ObjectVariable parent;
         private String name;
         private FXValue value;
 
         private int index;
 
         public SequenceField( JPDADebuggerImpl debugger, FXPrimitiveValue value,
                 ObjectVariable parent, int index, String parentID )
         {
             super( debugger, value, parentID + "." + index );
             this.parent = parent;
             this.value = value;
             this.sequence = (FXSequenceReference)((JDIVariable)parent).getJDIValue();
         }
 
         public String getName() {
             return "[" + index + "]";
         }
 
         public String getClassName() {
             return getType();
         }
 
         public JPDAClassType getDeclaringClass() {
             try {
                 return new JPDAClassTypeImpl( getDebugger(), (ReferenceType)ValueWrapper.type( sequence ));
             } catch (InternalExceptionWrapper ex) {
                 // re-throw, we should not return null and can not throw anything checked.
                 throw ex.getCause();
             } catch (ObjectCollectedExceptionWrapper ex) {
                 // re-throw, we should not return null and can not throw anything checked.
                 throw ex.getCause();
             } catch (VMDisconnectedExceptionWrapper ex) {
                 // re-throw, we should not return null and can not throw anything checked.
                 throw ex.getCause();
             }
         }
 
         public String getDeclaredType() {
             return value.type().name();
         }
 
         public boolean isStatic() {
             return false;
         }
 
         @Override
         public String toString () {
             return "SequenceField " + getName ();
         }
     }
 
     public static final class SequenceObject extends AbstractObjectVariable implements Field {
 
         private final FXSequenceReference array;
         private final ObjectVariable parent;
         private int index;
         private int maxIndexLog;
         private String declaredType;
 
         SequenceObject( JPDADebuggerImpl debugger, ObjectReference value,
                 String declaredType, ObjectVariable array, int index,
                 int maxIndex, String parentID )
         {
             super( debugger, value, parentID + '.' + index + "^" );
             this.index = index;
 //            this.maxIndexLog = ArrayFieldVariable.log10( maxIndex );
             this.declaredType = declaredType;
             this.parent = array;
             this.array = (FXSequenceReference) ((JDIVariable) array).getJDIValue();
         }
 
          public String getName() {
              return "[" + index + "]";
 //            return ArrayFieldVariable.getName( maxIndexLog, index );
         }
 
         public String getClassName() {
             return getType ();
         }
 
         public JPDAClassType getDeclaringClass() {
             try {
                 return new JPDAClassTypeImpl( getDebugger(), (ReferenceType) ValueWrapper.type( array ));
             } catch (InternalExceptionWrapper ex) {
                 throw ex.getCause();
             } catch (VMDisconnectedExceptionWrapper ex) {
                 throw ex.getCause();
             } catch (ObjectCollectedExceptionWrapper ex) {
                 throw ex.getCause();
             }
         }
 
         public ObjectVariable getParentVariable() {
             return parent;
         }
 
         public boolean isStatic() {
             return false;
         }
 
         public String getDeclaredType() {
             return declaredType.replace( '$', '.' );
         }
 
         protected void setValue( Value value ) throws InvalidExpressionException {
 //            try {
 //                ArrayReferenceWrapper.setValue(array, index, value);
 //            } catch (InvalidTypeException ex) {
 //                throw new InvalidExpressionException (ex);
 //            } catch (ClassNotLoadedException ex) {
 //                throw new InvalidExpressionException (ex);
 //            } catch (InternalExceptionWrapper ex) {
 //                throw new InvalidExpressionException (ex.getCause());
 //            } catch (VMDisconnectedExceptionWrapper ex) {
 //                // Ignore
 //            } catch (ObjectCollectedExceptionWrapper ex) {
 //                throw new InvalidExpressionException (ex.getCause());
 //            }
         }
 
         public SequenceObject clone() {
             SequenceObject clon = new SequenceObject(
                     getDebugger(),
                     (ObjectReference) getJDIValue(),
                     getDeclaredType(),
                     parent,
                     index,
                     0,
                     getID());
             clon.maxIndexLog = this.maxIndexLog;
             return clon;
         }
 
         @Override
         public String toString () {
             return "SequenceObject " + getName ();
         }
     }
 }
