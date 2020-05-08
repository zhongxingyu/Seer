 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.netbeans.modules.javafx.debugger.tablerendering;
 
 import com.sun.javafx.jdi.FXClassType;
 import java.util.WeakHashMap;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import org.netbeans.api.debugger.jpda.Field;
 import org.netbeans.api.debugger.jpda.JPDAClassType;
 import org.netbeans.api.debugger.jpda.ObjectVariable;
 import org.netbeans.modules.debugger.jpda.models.FieldVariable;
 import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
 import org.netbeans.modules.javafx.debugger.models.ScriptClass;
 import org.netbeans.modules.javafx.debugger.models.ScriptFieldVariable;
 import org.netbeans.modules.javafx.debugger.models.ScriptObjectVariable;
 import org.netbeans.modules.javafx.debugger.models.SequenceField;
 import org.netbeans.modules.javafx.debugger.models.SequenceObject;
 import org.netbeans.spi.debugger.DebuggerServiceRegistration;
 import org.netbeans.spi.debugger.ui.Constants;
 import org.netbeans.spi.viewmodel.ModelListener;
 import org.netbeans.spi.viewmodel.TableRendererModel;
 import org.netbeans.spi.viewmodel.UnknownTypeException;
 
 /**
  *
  * @author Michal Skvor <michal.skvor at sun.com>
  */
 @DebuggerServiceRegistration( path="netbeans-JPDASession/FX/LocalsView",types={ org.netbeans.spi.viewmodel.TableRendererModel.class } )
 public class JavaFXTableRendererModel implements TableRendererModel {
 
     private WeakHashMap<Object, VariableCellRenderer> valueRenderers = new WeakHashMap<Object,VariableCellRenderer>();
 
     public JavaFXTableRendererModel() {}
 
     public boolean canRenderCell( Object o, String columnName ) throws UnknownTypeException {
         if( Constants.LOCALS_TYPE_COLUMN_ID.equals( columnName )) {
             if( o instanceof Field ) {
                 Field f = (Field)o;
                System.out.println(" f  - " + f );
                 if( f instanceof FieldVariable ) {
                     FieldVariable fv = (FieldVariable)f;
                     JPDAClassType ct = fv.getDeclaringClass();
                     if( ct instanceof JPDAClassTypeImpl ) {
                         JPDAClassTypeImpl cti = (JPDAClassTypeImpl)ct;
                         if( cti.getType() instanceof FXClassType ) return true;
                     }
                 } else if (f instanceof ScriptClass) {
                     return true;
                 } else if( f instanceof ScriptObjectVariable ) {
                     return true;
                 } else if( f instanceof ScriptFieldVariable ) {
                     return true;
                 } else if( f instanceof SequenceObject ) {
                     return true;
                 } else if( f instanceof SequenceField ) {
                     return true;
                 } else if( f instanceof ObjectVariable ) {
                     ObjectVariable ov = (ObjectVariable)f;
 //                    System.out.println("");
                     return true;
                 }
             }
         } else if( Constants.LOCALS_VALUE_COLUMN_ID.equals( columnName )) {
             if( o instanceof Field ) {
                 Field f = (Field)o;
                 if( f instanceof FieldVariable ) {
                     FieldVariable fv = (FieldVariable)f;
                     JPDAClassType ct = fv.getDeclaringClass();
                     if( ct instanceof JPDAClassTypeImpl ) {
                         JPDAClassTypeImpl cti = (JPDAClassTypeImpl)ct;
                         if( cti.getType() instanceof FXClassType ) return true;
                     }
                 } else if (f instanceof ScriptClass) {
                     return true;
                 } else if( f instanceof ScriptObjectVariable ) {
                     return true;
                 } else if( f instanceof ScriptFieldVariable ) {
                     return true;
                 } else if( f instanceof SequenceObject ) {
                     return true;
                 } else if( f instanceof SequenceField ) {
                     return true;
                 } else if( f instanceof ObjectVariable ) {
                     ObjectVariable ov = (ObjectVariable)f;
                    System.out.println("");
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public TableCellRenderer getCellRenderer( Object o, String columnName ) throws UnknownTypeException {
         if( Constants.LOCALS_TYPE_COLUMN_ID.equals( columnName ))
             return new VariableTypeRenderer( o );
         else if( Constants.LOCALS_VALUE_COLUMN_ID.equals( columnName )) {
             if( !valueRenderers.containsKey( o )) {
                 valueRenderers.put( o, new VariableCellRenderer( o, columnName ));
             }
             VariableCellRenderer renderer = valueRenderers.get( o );
             renderer.setObject( o );
             return renderer;
         }
         return null;
     }
 
     public boolean canEditCell( Object o, String columnName ) throws UnknownTypeException {
         if( Constants.LOCALS_VALUE_COLUMN_ID.equals( columnName )) {
             return true;
         }
         return false;
     }
 
     public TableCellEditor getCellEditor( Object o, String columnName ) throws UnknownTypeException {
         if( Constants.LOCALS_VALUE_COLUMN_ID.equals( columnName )) {
             if( !valueRenderers.containsKey( o )) {
                 valueRenderers.put( o, new VariableCellRenderer( o, columnName ));
             }
             return valueRenderers.get( o );
         }
         return null;
     };
 
     public void addModelListener( ModelListener ml ) {
     }
 
     public void removeModelListener( ModelListener ml ) {
     }
 }
