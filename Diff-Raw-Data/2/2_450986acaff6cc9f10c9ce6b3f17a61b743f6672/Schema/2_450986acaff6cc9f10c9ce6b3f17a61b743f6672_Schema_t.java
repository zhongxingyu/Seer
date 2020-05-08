 package gr.uoi.cs.daintiness.hecate.sql;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 public class Schema implements SqlItem{
 	
 	private String name;
 	private TreeMap<String, Table> tables;
 	
 	public Schema(TreeMap<String, Table> t) {
 		this.tables = t;
 	}	
 	
 	public Schema() {
 		this.tables = new TreeMap<String, Table>();
 	}
 	
 	public Schema(String name) {
 		this.tables = new TreeMap<String, Table>();
 		this.name = name;
 	}
 	
 	public void addTable(Table table) {
 		this.tables.put(table.getName(), table);
 	}
 	
 	public TreeMap<String, Table> getTables() {
 		return this.tables;
 	}
 	
 	public String toString() {
 		return name;
 	}
 	
 	public String print() {
 		String buff = new String();
		buff = "Schema: \n\n";
 		for (Map.Entry<String, Table> entry : this.tables.entrySet()) {
 			Table a = entry.getValue();
 			buff += "  " + a.print() + "\n";
 		}
 		return buff;
 	}
 	
 	public int[] getSize() {
 		int attr = 0;
 		for (Table t : this.tables.values()) {
 			attr += t.getSize();
 		}
 		int[] res = {this.tables.size(), attr};
 		return res;
 	}
 	
 	public void setTitle(String title) {
 		this.name = title;
 	}
 	
 	public Table getTableAt(int i) {
 		int c = 0;
 		if (i >= 0 && i < tables.size()){
 			for (Map.Entry<String, Table> t : tables.entrySet()) {
 				if (c == i) {
 					return t.getValue();
 				}
 				c++;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public int getMode() {
 		return 0;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public void setMode(int mode) {
 	
 	}
 }
