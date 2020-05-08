 package nl.giantit.minecraft.database.drivers.h2;
 
 import nl.giantit.minecraft.database.query.Query;
 import nl.giantit.minecraft.database.QueryResult;
 import nl.giantit.minecraft.database.query.SelectQuery;
 import nl.giantit.minecraft.database.Driver;
 import nl.giantit.minecraft.database.query.Group;
 import nl.giantit.minecraft.database.query.Join;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Giant
  */
 public class H2SelectQuery implements SelectQuery {
 	
 	private final Driver db;
 	
 	private final Map<String, String> s = new HashMap<String, String>();
 	private final List<Group> where = new ArrayList<Group>();
 	private final List<Join> join = new ArrayList<Join>();
 	private final Map<String, Order> order = new LinkedHashMap<String, Order>();
 	
 	private Map<String, String> f = new LinkedHashMap<String, String>();
 	
 	private boolean hasLimit = false;
 	private Integer start = null;
 	private int limit;
 	
 	private boolean prepared = false;
 	private String query;
 	
 	public H2SelectQuery(Driver db) {
 		this.db = db;
 	}
 
 	@Override
 	public SelectQuery select(String f) {
 		this.s.put(f, f);
 		return this;
 	}
 
 	@Override
 	public SelectQuery select(String... fields) {
 		return this.select(Arrays.asList(fields));
 	}
 
 	@Override
 	public SelectQuery select(List<String> fields) {
 		for(String field : fields) {
 			this.s.put(field, field);
 		}
 		
 		return this;
 	}
 
 	@Override
 	public SelectQuery select(Map<String, String> fields) {
 		this.s.putAll(fields);
 		return this;
 	}
 	
 	@Override
 	public SelectQuery from(String table) {
 		return this.from(table, table);
 	}
 	
 	@Override
 	public SelectQuery from(String table, String as) {
 		this.f.put(table, as);
 		return this;
 	}
 	
 	@Override
 	public Join join(String table) {
 		return this.join(new H2Join(this.db, table));
 	}
 	
 	@Override
 	public Join join(Join j) {
 		this.join.add(j);
 		
 		return j;
 	}
 	
 	@Override
 	public Group where(String field, Group.ValueType vT){
 		return this.where(Group.Type.AND, field, null, vT);
 	}
 	
 	@Override
 	public Group where(Group.Type t, String field, Group.ValueType vT){
 		return this.where(t, field, null, vT);
 	}
 	
 	@Override
 	public Group where(String field, String value) {
 		return this.where(Group.Type.AND, field, value);
 	}
 	
 	@Override
 	public Group where(String field, String value, Group.ValueType vT) {
 		return this.where(Group.Type.AND, field, value, vT);
 	}
 	
 	@Override
 	public Group where(Group.Type t, String field, String value) {
 		return this.where(t, field, value, Group.ValueType.EQUALS);
 	}
 	
 	@Override
 	public Group where(Group.Type t, String field, String value, Group.ValueType vT) {
 		Group g = new H2Group(this.db);
 		g.add(t, field, value, vT);
 		
 		return this.where(g);
 	}
 	
 	@Override
 	public Group where(Group g) {
 		if(where.isEmpty()) {
 			g.setType(Group.Type.PRIMARY);
 		}else if(g.getType() == Group.Type.PRIMARY) {
 			g.setType(Group.Type.AND);
 		}
 		
 		where.add(g);
 		return g;
 	}
 	
 	@Override
 	public SelectQuery orderBy(String field, Order order) {
 		this.order.put(field, order);
 		return this;
 	}
 	
 	@Override
 	public SelectQuery orderBy(Map<String, Order> fields) {
 		this.order.putAll(fields);
 		return this;
 	}
 	
 	@Override
 	public SelectQuery limit(int limit) {
 		return this.limit(limit, null);
 	}
 	
 	@Override
 	public SelectQuery limit(int limit, Integer start) {
 		this.hasLimit = true;
 		this.limit = limit;
 		this.start = start;
 		return this;
 	}
 
 	@Override
 	public Query parse() {
 		if(!this.prepared) {
 			this.prepared = true;
 			
 			StringBuilder sB = new StringBuilder();
 			sB.append("SELECT ");
 			if(this.s.size() > 0) {
 				int i = 0;
 				for(Map.Entry<String, String> field : this.s.entrySet()) {
 					if(i > 0) {
 						sB.append(", ");
 					}else{
 						++i;
 					}
 
 					sB.append(field.getKey().replace("#__", this.db.getPrefix()));
 					sB.append(" AS ");
 					sB.append(field.getValue().replace("#__", this.db.getPrefix()));
 					
 				}
 
 			}else{
 				sB.append("*");
 			}
 			
 			sB.append("\n");
 			
 			sB.append(" FROM ");
 			int i = 0;
 			for(Map.Entry<String, String> entry : this.f.entrySet()) {
 				if(i > 0) {
 					sB.append(", ");
 				}else{
 					++i;
 				}
 				
 				sB.append(entry.getKey().replace("#__", this.db.getPrefix()));
 				sB.append(" AS ");
 				sB.append(entry.getValue().replace("#__", this.db.getPrefix()));
 			}
 			
 			sB.append("\n");
 			
 			if(this.join.size() > 0) {
 				Iterator<Join> joinIterator = this.join.iterator();
 				while(joinIterator.hasNext()) {
 					Join j = joinIterator.next();
 					if(!j.isParsed()) {
 						j.parse();
 					}
 					
 					sB.append(j.getParsedJoin());
 				}
 			
 				sB.append("\n");
 			}
 			
 			if(this.where.size() > 0) {
 				sB.append(" WHERE ");
 				Iterator<Group> whereIterator = this.where.iterator();
 				while(whereIterator.hasNext()) {
 					Group g = whereIterator.next();
 					if(!g.isParsed()) {
 						g.parse();
 					}
 					
 					sB.append(g.getType().getTextual());
 					sB.append(g.getParsedGroup());
 				}
 			
 				sB.append("\n");
 			}
 			
 			if(this.order.size() > 0) {
 				sB.append(" ORDER BY ");
 				
 				int a = 0;
 				for(Map.Entry<String, Order> entry : this.order.entrySet()) {
 					if(a > 0) {
 						sB.append(", ");
 					}else{
 						++a;
 					}
 					sB.append(entry.getKey().replace("#__", this.db.getPrefix()));
 					sB.append(entry.getValue().getValue());
 				}
 			
 				sB.append("\n");
 			}
 			
 			if(this.hasLimit) {
 				sB.append(" LIMIT ");
 				if(null != this.start) {
 					sB.append(this.start);
 					sB.append(", ");
 				}
 				
 				sB.append(this.limit);
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
 			// Send SQLite Query syntax to console for debugging purposes!
 			this.db.getPlugin().getLogger().info(this.query);
 		}
 		
 		return this.db.execQuery(this);
 	}
 	
 	@Override
 	public Group createGroup() {
 		return new H2Group(this.db);
 	}
 }
