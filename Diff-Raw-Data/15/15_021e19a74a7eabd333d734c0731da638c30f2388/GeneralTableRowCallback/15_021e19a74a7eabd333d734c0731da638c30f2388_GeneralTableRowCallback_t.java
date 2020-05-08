 package com.seitenbau.testing.dbunit.dsl;
 
 import groovy.lang.Closure;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.google.common.base.Optional;
 
 public class GeneralTableRowCallback<R, F, D extends DatabaseReference> implements IParsedTableRowCallback
 {
   private TableRowModel _head;
 
   private int _columns;
 
   private int _lineNr;
 
   private int _colRef;
 
   private int _colId;
 
   private final ITableAdapter<R, F, D> _tableAdapter;
 
   public GeneralTableRowCallback(ITableAdapter<R, F, D> tableAdapter)
   {
     _tableAdapter = tableAdapter;
     _head = null;
     _columns = 0;
     _lineNr = 0;
     _colRef = -1;
     _colId = -1;
   }
 
   R getRowBuilder(TableRowModel row)
   {
     R result = null;
     boolean isNewRef = false;
    boolean updateId = false;
     Object id = null;
 
     // get builder by reference (if available)
     if (_colRef != -1)
     {
       Object refValue = row.getValue(_colRef);
       if (!(refValue instanceof NoValue))
       {
         @SuppressWarnings("unchecked")
         D ref = (D) row.getValue(_colRef);
         result = _tableAdapter.getRowByReference(ref);
         isNewRef = (result == null);
       }
     }
 
     if (_colId != -1)
     {
       @SuppressWarnings("unchecked")
       ColumnBinding<R, F> column = (ColumnBinding<R, F>) _head.getValue(_colId);
       id = row.getValue(_colId);
 
       if (!(id instanceof NoValue))
       {
        updateId = true;
        System.out.println("No builder id is present: " + id);
        if (id instanceof Integer && (Integer)id == 4) {
          System.out.println("here wo go");
        }
         Optional<R> builderById = column.getWhere(_tableAdapter.getWhere(), CastUtil.cast(id, column.getDataType()));
         if (builderById.isPresent())
         {
           if (result != null && builderById.get() != result)
           {
             throw new RuntimeException("Table structure failure [Possibly trial of ID redefinition]");
           }
 
           result = builderById.get();
         }
       }
     }
 
     if (result == null)
     {
       result = _tableAdapter.insertRow();
     }
 
    if (isNewRef || updateId)
     {
       @SuppressWarnings("unchecked")
       D ref = (D) row.getValue(_colRef);
       if (id != null)
       {
         // Set manual given ID
         @SuppressWarnings("unchecked")
         ColumnBinding<R, F> column = (ColumnBinding<R, F>) _head.getValue(_colId);
         column.set(result, id);
       }
      if (isNewRef) {
        _tableAdapter.handleReferences(ref, result);
      }
     }
     return result;
   }
 
   public void parsedRow(TableRowModel row)
   {
     _lineNr++;
     if (row.isHeadRow())
     {
       _head = row;
       _colRef = _head.getRefColumn();
       _colId = _head.getIdentifierColumn();
       _columns = _head.getColumnCount();
       return;
     }
 
     if (row.getColumnCount() != _columns)
     {
       throwColumnsDoNotMatchException(_lineNr, row);
     }
     R rowbuilder = getRowBuilder(row);
 
     Map<ColumnBinding<R, F>, Closure<?>> closures = new HashMap<ColumnBinding<R, F>, Closure<?>>();
     for (int columnIndex = 0; columnIndex < _columns; columnIndex++)
     {
       if (columnIndex == _colRef || columnIndex == _colId)
       {
         continue;
       }
 
       @SuppressWarnings("unchecked")
       ColumnBinding<R, F> column = (ColumnBinding<R, F>) _head.getValue(columnIndex);
       Object value = row.getValue(columnIndex);
       if (value instanceof NoValue)
       {
         continue;
       }
 
       if (value instanceof DatabaseReference)
       {
         // TODO
         DatabaseReference ref = (DatabaseReference) value;
         column.setReference(rowbuilder, ref);
       } else if (value instanceof Closure) {
         // call closure values after row has been built and registered
         closures.put(column, (Closure<?>)value);
       }
       else
       {
         column.set(rowbuilder, value);
       }
     }
 
     if (_colRef != -1 && row.getValue(_colRef) != null)
     {
       Object refValue = row.getValue(_colRef);
       if (!(refValue instanceof NoValue))
       {
         @SuppressWarnings("unchecked")
         D ref = (D) row.getValue(_colRef);
         _tableAdapter.bindToScope(ref, rowbuilder);
       }
     }
     
     for (Entry<ColumnBinding<R, F>, Closure<?>> entry : closures.entrySet()) 
     {
       ColumnBinding<R,F> column = entry.getKey();
       Closure<?> closure = entry.getValue();
       Object value = closure.call();
       column.set(rowbuilder, value);
     }
   }
 
   private void throwColumnsDoNotMatchException(int lineNr, TableRowModel row)
   {
     throwException("column count does not match", lineNr, row);
   }
 
   private void throwException(String message, int lineNr, TableRowModel row)
   {
     StringBuilder builder = new StringBuilder();
     builder.append("Error in " + _tableAdapter.getTableName() + ", line " + lineNr + ": " + message);
     if (row.getValues().size() > 0)
     {
       builder.append(" [TableRowModel: ");
       for (Object value : row.getValues())
       {
         builder.append(value);
         builder.append(" | ");
       }
       builder.setLength(builder.length() - 3);
       builder.append("]");
     }
     throw new IllegalArgumentException(builder.toString());
   }
 }
