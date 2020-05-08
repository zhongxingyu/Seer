 package data;
 
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.node.ObjectNode;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 
 import data.Database.Rels;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.libs.Json;
 
 // you never wirte the datas, they are only for display!
 public final class CategoryData implements Comparable<CategoryData> {
 
 	public static CategoryData newCatData = new CategoryData(true);
 	
 	// id
     @JsonIgnore
 	public long id = -1; // use -1 as new
     
     // form data
     @Constraints.Required
     @Formats.NonEmpty
 	public String name;
 
 	@Constraints.Required
     @Formats.NonEmpty
 	public int order;
 
 
 	// form data 2 
 	public String structure;
 
 	public String itemOrder;
 
 	public boolean showIndex;
 
 	public boolean showStructure = true;
 
 	public boolean showDetail = true;
 	
 	public boolean sameType(CategoryData c) {
 		return structure.equals(c.structure) && (showDetail == c.showDetail) && (showStructure == c.showStructure);
 	}
     // object - database interface
 	private class Pro {
 		public static final String NAME = "name";
 		public static final String ORDER = "order";
 		
 		public static final String STRUCTURE = "structure";
 		public static final String ITEMORDER = "itemorder";
 		public static final String SHOWINDEX = "frontpage";
 		public static final String SHOWSTRUCTURE = "listpanel";
 		public static final String SHOWDETAIL = "detailpanel";
 	}
 	
 	public static class Structure {
 		public static final String LIST = "list";
 		public static final String MATH_GRAPH = "mathGraph";
 	}
 
 	// http interface
     public String json() {
     	if (id < 0) {
         	return Json.stringify(Json.toJson(this));
     	} else {
     		ObjectNode o = Json.newObject();
     		o.put(Pro.NAME, name);
     		o.put(Pro.ORDER, order);
     		o.put("showIndex", showIndex);		// due to data name changed... not using Pro
     		if (showStructure) {
     			o.put("showStructure", showStructure);
     		}
     		return Json.stringify(o);
     	}
     }
 
 	public static CategoryData get(long id) {
 		Node n = Database.get(id);
 		if (n != null && n.hasRelationship(Rels.SUPER_CAT_OF, Direction.INCOMING)) {
 			return new CategoryData(n);
 		}
 		return null;
 	}
 	
     // a category node -> cateogory object
 	public CategoryData(Node node) {
 		id = node.getId();
 		
 		name = (String) node.getProperty(Pro.NAME);
 		order = (int) node.getProperty(Pro.ORDER);
 		
 		structure = (String) node.getProperty(Pro.STRUCTURE);
 		itemOrder = (String) node.getProperty(Pro.ITEMORDER);
 		itemOrder = itemOrder.toLowerCase();
 		showIndex = (boolean) node.getProperty(Pro.SHOWINDEX);
 		showStructure = (boolean) node.getProperty(Pro.SHOWSTRUCTURE);
 		showDetail = (boolean) node.getProperty(Pro.SHOWDETAIL);
 	}
 	
 	public CategoryData() {
 		
 	}
 	public CategoryData(boolean hack) {
 		name = "newcat";
 		structure = "list";
 		itemOrder = "addtime desc";
 	}
 
 	// valid object -> valid node
 	public void write(Node node) {
 		node.setProperty(Pro.NAME, name);
 		node.setProperty(Pro.ORDER, order);
 		if (!showStructure)
 			node.setProperty(Pro.SHOWSTRUCTURE, showStructure);
 		if (!(id > 0)) {
 			node.setProperty(Pro.STRUCTURE, structure);
 			node.setProperty(Pro.ITEMORDER, itemOrder);
 			node.setProperty(Pro.SHOWINDEX, showIndex);
 			node.setProperty(Pro.SHOWDETAIL, showDetail);
 		}
 	}
 	
 	@Override
 	public int compareTo(CategoryData o) {
 		int diff = order - o.order;
 		if (diff != 0)
 			return diff;
 		else
 			return name.compareTo(o.name);
 	}
 	
 	
 	// bug?? why need setters??
 	
 
     public void setName(String name) {
 		this.name = name;
 	}
     public void setOrder(int order) {
 		this.order = order;
 	}
 
 	public void setStructure(String structure) {
 		this.structure = structure;
 	}
 
 	public void setItemOrder(String itemOrder) {
 		this.itemOrder = itemOrder;
 	}
 
 	public void setShowIndex(boolean showIndex) {
 		this.showIndex = showIndex;
 	}
 
 	public void setShowStructure(boolean showStructure) {
 		this.showStructure = showStructure;
 	}
 
 	public void setShowDetail(boolean showDetail) {
 		this.showDetail = showDetail;
 	}
 	
 }
