 package UserInterface;
 
 import java.util.ArrayList;
 
 public class DBTable
 {
     private ArrayList<DBTableColumn> columns;
     private String name;
     private TableType tableType;
 
     public DBTable(String name)
     {
         this.name = name;
         columns = new ArrayList<DBTableColumn>();
     }
 
     public ArrayList<DBTableColumn> getColumns()
     {
         return columns;
     }
 
     public void setColumns(ArrayList<DBTableColumn> columns)
     {
         this.columns = columns;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public TableType getTableType()
     {
         return tableType;
     }
 
     public void setTableType(TableType type)
     {
         this.tableType = type;
     }
 
     public boolean equals(Object other)
     {
         if(other == null)
             throw new NullPointerException();
         if(!(other instanceof DBTable))
             throw new RuntimeException("Type Mismatch!");
 
         DBTable that = (DBTable) other;
 
         return this.name.equals(that.getName());
     }
 
     public static TableType getTableType(String type)
     {
         TableType tType = TableType.UNKNOWN;
 
        if(type.equalsIgnoreCase("system view") || type.equalsIgnoreCase("view"))
             tType = TableType.VIEW;
         else if(type.equalsIgnoreCase("base table"))
             tType = TableType.TABLE;
 
         return tType;
     }
     enum TableType
     {
         UNKNOWN,
         TABLE,
         VIEW;
     }
 }
