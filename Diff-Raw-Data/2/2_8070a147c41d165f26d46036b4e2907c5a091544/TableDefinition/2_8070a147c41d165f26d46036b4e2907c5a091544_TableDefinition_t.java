 package uk.org.smithfamily.utils.normaliser.tableeditor;
 
 public class TableDefinition extends TableItem
 {
     private String name;
     private String map3DName;
     private String label;
     private String page;
 
     public TableDefinition(TableTracker parent,String name, String map3DName, String label, String page)
     {
         this.name = name;
         this.map3DName = map3DName;
         this.label = label;
         this.page = page;
         parent.setName(name);
     }
 
     public String getName()
     {
         return name;
     }
 
     public String getMap3DName()
     {
         return map3DName;
     }
 
     public String getLabel()
     {
         return label;
     }
 
     public String getPage()
     {
         return page;
     }
     
     @Override
     public String toString()
     {
        return String.format("t = new TableEditor(\"%s\",\"%s\",\"%s\",%s); tableEditors.put(\"%s\",t);",name,map3DName,label,page,name);
     }
 
 }
