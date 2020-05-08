 package database;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import main.Logger;
 import relationenalgebra.AndExpression;
 import relationenalgebra.EqualityExpression;
 import relationenalgebra.OrExpression;
 import relationenalgebra.PrimaryExpression;
 
 public class Table implements Serializable {
 
     private static class ValueLookup {
         private Map<String, Integer> indexMap;
         private Table[] tables;
         private int[] currentRowIndices;
         private boolean anonymous;
 
         ValueLookup(Table... tables) {
             indexMap = new HashMap<String, Integer>();
             this.tables = tables;
             currentRowIndices = new int[tables.length];
             anonymous = false;
 
             for (int i = 0; i < tables.length; i++) {
                 indexMap.put(tables[i].getOfficialName(), Integer.valueOf(i));
                 if (tables[i].getOfficialName().equals("")) {
                     anonymous = true;
                 }
             }
         }
 
         void setCurrentRowIndex(Table table, int rowIndex) {
             currentRowIndices[indexMap.get(table.getOfficialName())] = rowIndex;
         }
 
         void setCurrentRowIndex(int tableIndex, int rowIndex) {
             currentRowIndices[tableIndex] = rowIndex;
         }
 
         String lookupValue(String name) {
             Integer tableIndex = Integer.valueOf(0);
             String columnName = name;
             String tableName = "";
             
             if (!anonymous) {
                 int i = name.indexOf(".");
                 tableName = name.substring(0, i);
                 columnName = name.substring(i + 1, name.length());
                 tableIndex = indexMap.get(tableName);
 
                 if (tableIndex == null) {
                     throw new IllegalArgumentException("table '" + tableName
                             + "' not found");
                 }
                 Table table = tables[tableIndex];
 
                 int rowIndex = currentRowIndices[tableIndex];
                 int columnIndex = table.getColumnIndex(columnName);
 
                 if (columnIndex < 0) {
                     throw new IllegalArgumentException("no column named '"
                             + columnName + "' found in current table");
                 }
 
                 return table.rows.get(rowIndex).get(columnIndex);
             } else {
             	Table table = null;
             	
             	for (int i = 0; i < tables.length; ++i ) {
             		Table t = tables[i];
             		for (String colName : t.columnNames){
             			String alias = t.alias + "." + colName;
            			String splitted = "";
            			int k = -100;
            			k = colName.indexOf(".");
            			if (k > 0) {
            				splitted = colName.substring(i + 1, colName.length());
            			}
                        
            			if (colName.equals(name) || alias.equals(name) || splitted.equals(name)) {
             				table = t;
             				tableIndex = i;
             				break;
             			}
             		}
             		if (table != null)
             			break;
             	}
             	
             	if (table == null)
             		throw new IllegalArgumentException("no column named '"
                             + columnName + "' found in current table");
             	
             	int rowIndex = currentRowIndices[tableIndex];
                 int columnIndex = table.getColumnIndex(columnName);
 
                 if (columnIndex < 0) {
                     throw new IllegalArgumentException("no column named '"
                             + columnName + "' found in current table");
                 }
             	
                 return table.rows.get(rowIndex).get(columnIndex);
             }
 
             
         }
     }
 
     static final long serialVersionUID = 1234;
     protected String name;
     protected String alias;
     protected boolean drop;
     protected List<String> columnNames;
     protected List<List<String>> rows;
     protected int cost;
 
     public Table(String name) {
         super();
         this.name = name;
         this.setUp();
     }
 
     public Table(String name, List<String> columns) {
         super();
         this.name = name;
         this.columnNames = columns;
         this.setUp();
     }
 
     private void setUp() {
         this.rows = newRowsList();
         this.drop = false;
     }
 
     /**
      * Writes the actual instance to the filesystem.
      */
     public void write() {
         FileSystemDatabase.getInstance().write(this);
     }
 
     /**
      * Returns one row from privat row repository.
      */
     public List<String> getRow(int number) {
         return Collections.unmodifiableList(rows.get(number));
     }
 
     public void addRow(List<String> names) {
         if (names.size() != columnNames.size()) {
             throw new IllegalArgumentException("bad row size");
         }
         rows.add(new ArrayList<String>(names));
     }
 
     public void deleteRow(int number) {
         rows.remove(number);
     }
 
     private int getColumnIndex(String name) {
         for (int i = 0; i < columnNames.size(); i++) {
             if (columnNames.get(i).equals(name)) {
                 return i;
             }
             if (name.equals(getOfficialName() + "." + columnNames.get(i))) {
                 return i;
             }
         }
         return -1;
     }
 
     private static List<List<String>> newRowsList() {
         return new LinkedList<List<String>>();
     }
 
     public Table projectTo(List<String> param) {
         // compute indices for projection
         int[] projection = new int[param.size()];
         for (int i = 0; i < projection.length; i++) {
             int index = getColumnIndex(param.get(i));
             if (index < 0) {
                 throw new IllegalArgumentException("no column with name '"
                         + param.get(i) + "'");
             }
             projection[i] = index;
         }
 
         //Logger.debug("projection: "+columnNames+" to "+param);
 
         // generate column names list
         List<String> newColumnNames = new ArrayList<String>(projection.length);
         for (int i = 0; i < projection.length; i++) {
             newColumnNames.add(columnNames.get(projection[i]));
         }
 
         // generate new rows list
         List<List<String>> newRows = newRowsList();
         for (List<String> row : rows) {
             List<String> projectedRow = new ArrayList<String>(projection.length);
             for (int i = 0; i < projection.length; i++) {
                 projectedRow.add(row.get(projection[i]));
             }
             newRows.add(projectedRow);
         }
 
         int newCost = this.cost + columnNames.size() * rows.size();
 
         return createAlteredClone(newColumnNames, newRows, newCost);
     }
 
     private Table createAlteredClone(List<String> newColumnNames,
             List<List<String>> newRows, int newCost) {
         Table result = new Table(this.name);
         result.alias = this.alias;
 
         if (newColumnNames == null) {
             newColumnNames = new ArrayList<String>(this.columnNames);
         }
 
         result.columnNames = newColumnNames;
         result.drop = false;
         result.rows = newRows;
         result.cost = newCost;
         return result;
     }
 
     public Table select(AndExpression exp) {
         List<List<String>> newRows = newRowsList();
         ValueLookup vl = new ValueLookup(this);
 
         //Logger.debug("selection: "+exp);
         //Logger.debug("name: "+getOfficialName()+", columns: "+columnNames);
 
         for (int i = 0; i < rows.size(); i++) {
             vl.setCurrentRowIndex(this, i);
 
             //Logger.debug("next row: "+rows.get(i));
 
             if (evaluate(exp, vl)) {
                 newRows.add(new ArrayList<String>(rows.get(i)));
 
             //	Logger.debug("ADDED");
             }
             //else Logger.debug("DISCARDED");
         }
 
         int newCost = cost + newRows.size() * columnNames.size();
 
         return createAlteredClone(null, newRows, newCost);
     }
 
     private boolean evaluate(AndExpression expr, ValueLookup vl) {
         /*
         if (expr.getExprs() == null) {
             return evaluate(expr.getExpr(), vl);
         }
         */
         for (OrExpression or : expr.getExprs()) {
             if (!evaluate(or, vl)) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean evaluate(OrExpression expr, ValueLookup vl) {
         /*
         if (expr.getExprs() == null) {
             return evaluate(expr.getExpr(), vl);
         }
         */
         for (EqualityExpression eq : expr.getExprs()) {
             if (evaluate(eq, vl)) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean evaluate(EqualityExpression expr, ValueLookup vl) {
         String valueA = retrieveValue(expr.getExpr1(), vl);
         String valueB = retrieveValue(expr.getExpr2(), vl);
         int cmp = valueA.compareTo(valueB);
 
         //Logger.debug("comparing "+valueA+" to "+valueB+": "+cmp);
 
         switch (expr.getOperator()) {
         case Equal:
             return cmp == 0;
         case Greater:
             return cmp > 0;
         case GreaterEqual:
             return cmp >= 0;
         case Lower:
             return cmp < 0;
         case LowerEqual:
             return cmp <= 0;
         case NotEqual:
             return cmp != 0;
         default:
             throw new RuntimeException("unknown operator: "
                     + expr.getOperator());
         }
     }
 
     private String getOfficialName() {
         if (alias != null) {
             return alias;
         }
         return name;
     }
 
     private String retrieveValue(PrimaryExpression expr, ValueLookup vl) {
         if (expr.isConstant()) {
             return expr.getValue();
         }
         return vl.lookupValue(expr.getValue());
     }
 
     private static List<String> toQualifiedColumnNames(
             List<String> columnNames, String tableName) {
         List<String> result = new ArrayList<String>(columnNames.size());
         for (String name : columnNames) {
             if (tableName.length() == 0) {
                 result.add(name);
             } else {
                 result.add(tableName + "." + name);
             }
         }
         return result;
     }
 
     public Table join(Table table, AndExpression exp) {
         List<List<String>> newRows = newRowsList();
         ValueLookup vl = new ValueLookup(this, table);
         int newRowSize = columnNames.size() + table.columnNames.size();
 
         List<String> newColumnNames = new ArrayList<String>(newRowSize);
         newColumnNames.addAll(toQualifiedColumnNames(columnNames,
                 getOfficialName()));
         newColumnNames.addAll(toQualifiedColumnNames(table.columnNames, table
                 .getOfficialName()));
         
         //if (exp != null) 
         //	System.out.println("new column names: "+newColumnNames);
 
         for (int i = 0; i < rows.size(); i++) {
             vl.setCurrentRowIndex(0, i);
             for (int j = 0; j < table.rows.size(); j++) {
                 vl.setCurrentRowIndex(1, j);
                 List<String> newRow = new ArrayList<String>(newRowSize);
                 newRow.addAll(rows.get(i));
                 newRow.addAll(table.rows.get(j));
 
 
                 //System.out.print("next row "+i+", "+j+": "+newRow);
 
                 if (exp == null || evaluate(exp, vl)) {
                     newRows.add(newRow);
                     //if (exp != null) System.out.println(" ACCEPTED");
                 } else {
                 	//if (exp != null) System.out.println(" REJECTED");
                 }
             }
         }
 
         Table result = new Table("", newColumnNames);
         result.rows = newRows;
         result.cost = this.cost + table.cost + newRows.size() * newColumnNames.size();
         return result;
     }
 
     public Table cross(Table table) {
         return join(table, null);
     }
 
     public String toString() {
         String s = "table '" + this.name + "'\n\tcolumns: '";
         for (String c : this.columnNames) {
             s += c + ", ";
         }
         s += "'\n\trows:\n";
 
         if (this.rows != null) {
             for (List<String> row : this.rows) {
                 s += "\t\t";
                 for (String value : row) {
                     s += value + "\t";
                 }
                 s += "\n";
             }
         }
 
         s += "cost: " + cost + "\n";
         return s;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getAlias() {
         return alias;
     }
 
     public void setAlias(String alias) {
         this.alias = alias;
     }
 
     public int getCost() {
         return cost;
     }
 
     public String[] getColumnnames() {
         return this.columnNames.toArray(new String[this.columnNames.size()]);
     }
 
 }
