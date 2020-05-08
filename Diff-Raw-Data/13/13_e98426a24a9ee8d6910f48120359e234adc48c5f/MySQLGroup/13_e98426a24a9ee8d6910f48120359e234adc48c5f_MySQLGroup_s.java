 package nl.giantit.minecraft.database.drivers.mysql;
 
 import nl.giantit.minecraft.database.Driver;
 import nl.giantit.minecraft.database.query.Group;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Giant
  */
 public class MySQLGroup implements Group {
 
 	private final Driver db;
 	private Type t;
 	
 	private final List<Object> elems = new ArrayList<Object>();
 	
 	private boolean prepared = false;
 	private String group;
 	
 	MySQLGroup(Driver db) {
 		this.db = db;
 	}
 	
 	@Override
 	public Group setType(Type t) {
 		this.t = t;
 		
 		return this;
 	}
 	
 	@Override
 	public Group add(String field, ValueType vT){
 		return this.add(Type.AND, field, null, vT);
 	}
 	
 	@Override
 	public Group add(Type t, String field, ValueType vT){
 		return this.add(t, field, null, vT);
 	}
 	
 	@Override
 	public Group add(String field, String value) {
 		return this.add(Type.AND, field, value);
 	}
 	
 	@Override
 	public Group add(String field, String value, ValueType vT) {
 		return this.add(Type.AND, field, value, vT);
 	}
 	
 	@Override
 	public Group add(Type t, String field, String value) {
 		return this.add(t, field, value, ValueType.EQUALS);
 	}
 	
 	@Override
 	public Group add(Type t, String field, String value, ValueType vT) {
 		Elem e = new Elem(t, field, value, vT);
 		elems.add(e);
 		
 		return this;
 	}
 	
 	@Override
 	public Group add(Group g) {
 		this.elems.add(g);
 		
 		return this;
 	}
 	
 	@Override
 	public Group parse() {
 		if(!this.prepared) {
 			this.prepared = true;
 			
 			StringBuilder sB = new StringBuilder();
 			sB.append("(");
 			Iterator<Object> elemIterator = this.elems.iterator();
 			while(elemIterator.hasNext()) {
 				Object eRaw = elemIterator.next();
 				
 				if(eRaw instanceof Elem) {
 					Elem e = (Elem)eRaw;
 					sB.append(e.getType().getTextual());
 
 					String v = e.getValue() == null ? "" : e.getValue();
 					ValueType vT = e.getValueType();
 					String vTS = vT.getTextual().replace("%1", e.getField().replace("#__", this.db.getPrefix()));
 					vTS = vTS.replace("%2", v);
 					sB.append(vTS);
 					
 					/*sB.append(e.getField().replace("#__", this.db.getPrefix()));
 					sB.append(" = ");
 					sB.append(e.getValue());*/
 				}else if(eRaw instanceof Group) {
 					Group e = (Group)eRaw;
 					sB.append(e.getType().getTextual());
 					if(!e.isParsed()) {
 						e.parse();
 					}
 					
 					sB.append(e.getParsedGroup());
 				}
 			}
 			
 			sB.append(")");
 			
 			this.group = sB.toString();
 		}
 		
 		return this;
 	}
 	
 	@Override
 	public boolean isParsed() {
 		return this.prepared;
 	}
 	
 	@Override
 	public String getParsedGroup() {
 		if(!this.prepared) {
 			return "";
 		}
 		
 		return this.group;
 	}
 	
 	@Override
 	public Type getType() {
 		return this.t;
 	}
 	
 	@Override
 	public Group merge(Group g) {
 		return this;
 	}
 }
