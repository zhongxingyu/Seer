 package nl.giantit.minecraft.database.drivers.sqlite;
 
 import nl.giantit.minecraft.database.Driver;
 import nl.giantit.minecraft.database.QueryResult;
 import nl.giantit.minecraft.database.query.Group;
 import nl.giantit.minecraft.database.query.InsertQuery;
 import nl.giantit.minecraft.database.query.Query;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Giant
  */
 public class SQLiteInsertQuery implements InsertQuery {
 
 	private final Driver db;
 	
 	private String into;
 	private final List<String> fields = new ArrayList<String>();
 	private final List<Map<String, Elem>> values = new ArrayList<Map<String, Elem>>();
 	
 	private boolean prepared = false;
 	private String query;
 	
 	public SQLiteInsertQuery(Driver db) {
 		this.db = db;
 	}
 	
 	@Override
 	public InsertQuery into(String table) {
 		if(!this.isParsed()) {
 			this.into = table;	
 		}
 		
 		return this;
 	}
 	
 	@Override
 	public InsertQuery addField(String field) {
 		return this.addFields(field);
 	}
 	
 	@Override
 	public InsertQuery addFields(String... fields) {
 		return this.addFields(Arrays.asList(fields));
 	}
 	
 	@Override
 	public InsertQuery addFields(List<String> fields) {
 		if(!this.isParsed()) {
 			this.fields.addAll(fields);
 		}
 		
 		return this;
 	}
 	
 	@Override
 	public InsertQuery addRow() {
 		if(!this.isParsed()) {
 			Map<String, Elem> row = new HashMap<String, Elem>();
 			this.values.add(row);
 		}
 		
 		return this;
 	}
 	
 	@Override
 	public InsertQuery assignValue(String field, String value) {
 		return this.assignValue(field, value, ValueType.DEFAULT);
 	}
 	
 	@Override
 	public InsertQuery assignValue(String field, String value, ValueType vT) {
 		if(!this.isParsed()) {
 			if(this.values.isEmpty()) {
 				this.addRow();
 			}
 			
 			Map<String, Elem> row = this.values.get(this.values.size() - 1);
 			Elem e = new Elem(value, vT);
 			
 			row.put(field, e);
 		}
 		
 		return this;
 	}
 	
 	@Override
 	public Query parse() {
 		if(!this.prepared) {
 			this.prepared = true;
 			
 			StringBuilder sB = new StringBuilder();
 			sB.append("INSERT INTO ");
 			sB.append(this.into.replace("#__", this.db.getPrefix()));
 			sB.append("\n");
 			
 			sB.append("(");
 			for(int i = 0; i < this.fields.size(); ++i) {
 				if(i > 0) {
 					sB.append(", ");
 				}
 				
 				sB.append(this.fields.get(i));
 			}
 			sB.append(")");
 			sB.append("\n");
 			
 			sB.append(" SELECT ");
 			
 			for(int i = 0; i < this.values.size(); ++i) {
 				if(i > 0) {
					sB.append(" UNION ALL SELECT ");
 				}
 				
 				Map<String, Elem> vM = this.values.get(i);
 				for(int a = 0; a < this.fields.size(); ++a) {
 					String f = this.fields.get(a);
 					
 					if(a > 0) {
 						sB.append(", ");
 					}
 					
 					if(vM.containsKey(f)) {
 						Elem e = vM.get(f);
 						sB.append(e.getValueType().getTextual().replace("%1", e.getValue()));
 					}else{
 						sB.append("''");
 					}
 				}
 				
 				sB.append("\n");
 			}
 			
 			sB.append(";");
 			
 			this.query = sB.toString();
 		}
 		
 		return this;
 	}
 	
 	@Override
 	public boolean isParsed() {
 		return this.prepared;
 	}
 	
 	@Override
 	public String getParsedQuery() {
 		if(!this.prepared) {
 			return "";
 		}
 		
 		return this.query;
 	}
 
 	@Override
 	public QueryResult exec() {
 		return this.exec(false);
 	}
 
 	@Override
 	public QueryResult exec(boolean debug) {
 		if(debug) {
 			// Send MySQL Query syntax to console for debugging purposes!
 			this.db.getPlugin().getLogger().info(this.query);
 		}
 		
 		return this.db.updateQuery(this);
 	}
 	
 	@Override
 	public Group createGroup() {
 		return new SQLiteGroup(this.db);
 	}
 }
