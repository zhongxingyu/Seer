 package org.amanzi.awe.afp.filters;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 
 public class AfpColumnFilter{
 	
 	static final private String col_delimiter = "--";
 	
 	private String colName;
 	private ArrayList<String> values;
 	
 	
 	public AfpColumnFilter(String colName) {
 		this.colName = colName;
 		this.values = new ArrayList<String>();
 	}
 	
 	public AfpColumnFilter(String colName, ArrayList<String> values) {
 		this.colName = colName;
 		this.values = values;
 	}
 	
 	
 	public String getColName() {
 		return colName;
 	}
 
 	public void setColName(String colName) {
 		this.colName = colName;
 	}
 
 	public ArrayList<String> getValues() {
 		return values;
 	}
 
 	public void setValues(ArrayList<String> values) {
 		this.values = values;
 	}
 
 	public void addValue(String value) {
 		values.add(value);
 	}
 	
 	public void removeValue(String value) {
 		values.remove(value);
 	}
 	
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		sb.append(colName);
 		for (String val : values){
 			sb.append(col_delimiter);
 			sb.append(val);
 		}
 		
 		return sb.toString();
 	}
 	
 	public static AfpColumnFilter getFilter(String filterstring) {
 		String[] tokens = filterstring.split(col_delimiter);
 		String colName = tokens[0];
 		AfpColumnFilter ret = new AfpColumnFilter(colName);
 		
 		if(tokens.length > 0) {
 			for(int i=1; i < tokens.length; i++)
 				ret.addValue(tokens[i]);
 			return ret;
 		}
 		return null;
 	}
 	
 	/**
 	 * OR the different values. If any condition is met, return true.
 	 * @param n
 	 * @return
 	 */
 	public boolean equal(Node n){
 		Object obj = null;
 		if (colName.equals("Site") || colName.equals("site")){
 			Node sectorNode = n.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 			Node siteNode = sectorNode.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 			if (siteNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.SITE.getId()))
 				obj = siteNode.getProperty(INeoConstants.PROPERTY_NAME_NAME);
 		}
 		
 		else if (colName.equals("Sector") || colName.equals("sector")){
 			Node sectorNode = n.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 			if (sectorNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.SECTOR.getId()))
 				obj = sectorNode.getProperty(INeoConstants.PROPERTY_NAME_NAME);
 		}
 		
 		else{
 			obj = n.getProperty(colName);
 		}
 		String val = null;
 		int intVal = -1;
		
		if (obj instanceof String)
			val = (String)obj;
		else if (obj instanceof Integer){
			intVal = (Integer)obj;
			val = Integer.toString(intVal);
		}
 		
 		if (val != null){
 			for (String value : values){
 				if (val.equals(value))
 					return true;
 			}
 		}
 			
 		
 		return false;
 	}
 
 }
