 package nl.giantit.minecraft.database.drivers.h2;
 
 import nl.giantit.minecraft.database.Driver;
 import nl.giantit.minecraft.database.QueryResult;
 import nl.giantit.minecraft.database.query.CreateQuery;
 import nl.giantit.minecraft.database.query.Column;
 import nl.giantit.minecraft.database.query.ForeignKey;
 import nl.giantit.minecraft.database.query.Group;
 import nl.giantit.minecraft.database.query.Query;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  *
  * @author Giant
  */
 public class H2CreateQuery implements CreateQuery {
 	
 	private final Driver db;
 	
 	private String t;
 	private final List<Column> fL = new ArrayList<Column>();
 	//private final List<IndexQuery> ind = new ArrayList<IndexQuery>();
 	private final List<ForeignKey> fkL = new ArrayList<ForeignKey>();
 	
 	private boolean prepared = false;
 	private String query;
 	
 	public H2CreateQuery(Driver db) {
 		this.db = db;
 	}
 
 	@Override
 	public CreateQuery create(String table) {
 		this.t = table;
 		return this;
 	}
 	
 	@Override
 	public Column addColumn(String column) {
 		return this.addColumn(new H2Column().setName(column));
 	}
 	
 	@Override
 	public Column addColumn(Column column) {
 		this.fL.add(column);
 		return column;
 	}
 	
 	@Override
 	public ForeignKey addForeignKey(String name) {
 		return this.addForeignKey(new H2ForeignKey(this.db).setName(name));
 	}
 	
 	@Override
 	public ForeignKey addForeignKey(ForeignKey fk) {
 		this.fkL.add(fk);
 		return fk;
 	}
 	
 	/*@Override
 	public IndexQuery addIndex(String index) {
 		return this.addIndex(new H2IndexQuery(this.db).setName(index).setTable(this.t));
 	}
 	
 	@Override
 	public IndexQuery addIndex(IndexQuery index) {
 		this.ind.add(index);
 		return index;
 	}*/
 	
 
 	@Override
 	public Query parse() {
 		if(!this.prepared) {
 			this.prepared = true;
 			
 			String P_KEY = null;
 			
 			StringBuilder sB = new StringBuilder();
 			sB.append("CREATE TABLE ");
 			sB.append(this.t.replace("#__", this.db.getPrefix()));
 			sB.append("\n");
 			
 			sB.append(" (\n");
 			
 			int i = 0;
 			for(Column f : this.fL) {
 				if(i > 0) {
 					sB.append(", \n");
 				}else{
 					++i;
 				}
 				
 				if(!f.isParsed()) {
 					f.parse();
 				}
 
 				sB.append(f.getParsedColumn());
 				if(f.hasPrimaryKey()) {
 					if(null == P_KEY) {
 						P_KEY = f.getName();
 					}else{
 						P_KEY = P_KEY + ", " + f.getName();
 					}
 				}
 			}
 			
 			if(null != P_KEY) {
				sB.append(", \nPRIMARY KEY(");
 				sB.append(P_KEY);
 				sB.append(")");
 			}
 			
 			for(ForeignKey fk : this.fkL) {
 				if(!fk.isParsed()) {
 					fk.parse();
 				}
 				
				sB.append(", \nCONSTRAINT ");
 				sB.append(fk.getName());
 				sB.append(fk.getParsedFK());
 			}
 			
 			sB.append("\n);");
 			
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
 			// Send H2 Query syntax to console for debugging purposes!
 			this.db.getPlugin().getLogger().info(this.query);
 		}
 		
 		/*QueryResult QR = this.db.updateQuery(this);
 		
 		for(IndexQuery i : this.ind) {
 			i.exec(debug);
 		}
 		
 		return QR;*/
 		return this.db.updateQuery(this);
 	}
 	
 	@Override
 	public Group createGroup() {
 		return new H2Group(this.db);
 	}
 }
