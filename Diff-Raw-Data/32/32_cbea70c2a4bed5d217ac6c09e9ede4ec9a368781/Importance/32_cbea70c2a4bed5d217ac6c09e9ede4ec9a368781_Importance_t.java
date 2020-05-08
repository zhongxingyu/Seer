 package dataStructures;
 
 import java.util.LinkedList;
 
 public class Importance {
 
 	private AttributeList[] lists;
 	private Integer key;
 	
 	public Importance(Integer key){
 		this.key=key;
 		lists = new AttributeList[4];
 	}
 	
 	public AttributeList getList(int index){
 		return lists[index];
 	}
 	
 	public void setList(int index, LinkedList<Attribute> newList){
 		lists[index]=new AttributeList(newList);
 	}
 	
 	public Integer getKey(){
 		return key;
 	}
 	
 	public String toXML(){
		String importance = "\t<IMPORTANCE ID = '"+key.toString()+"'>\n";
		importance += "\t\t<LISTS>\n";
 		for(int i=0;i<lists.length;i++){
			importance += "\t\t\t<LIST>\n";
			importance += "\t\t\t\t<INDEX>"+i+"</INDEX>\n";
 			AttributeList list = lists[i];
 			if(list!=null)
 			for(Attribute a : list){
				importance += "\t\t\t\t<ATTRIBUTEKEY>"+a.getAttributeKey().getKey().toString()+"</ATTRIBUTEKEY>\n";
 			}
			importance += "\t\t\t</LIST>\n";
 		}
		importance += "\t\t</LISTS>\n";		
		importance += "\t</IMPORTANCE>\n";
 		return importance;
 	}
 	
 }
