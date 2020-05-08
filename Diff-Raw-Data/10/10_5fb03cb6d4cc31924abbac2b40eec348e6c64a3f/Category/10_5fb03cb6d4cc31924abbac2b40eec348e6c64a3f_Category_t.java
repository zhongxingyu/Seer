 package edu.nus.tp.engine.utils;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 
 public enum Category {
 
 	UNCLASSIFIED(0),
 	POSITIVE(1),
 	NEGATIVE(2),
 	NEUTRAL(3),
 	;
 
 	private static Map<Integer, Category> valueToKeyMap=new HashMap<Integer,Category>(5);

	static {
		for (Category cat: Category.values())
			valueToKeyMap.put(cat.getId(), cat);
	}

 	private Integer id;
 
 	private Category (int id){
 		this.id = id;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public static Category getCategoryForId(int id){
 		return valueToKeyMap.get(id);
 	}

 	public static List<Category> getClassificationClasses(){
 		return Lists.newArrayList(Category.POSITIVE, Category.NEGATIVE, Category.NEUTRAL);
 	}
 }
