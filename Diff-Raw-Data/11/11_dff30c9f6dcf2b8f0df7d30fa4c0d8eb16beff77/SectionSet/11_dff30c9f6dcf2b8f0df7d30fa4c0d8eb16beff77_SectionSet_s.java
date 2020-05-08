 package ru.xrm.app.misc;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import ru.xrm.app.domain.Section;
 
 public class SectionSet {
 	static SectionSet instance;
 	List<Section> sections;
 
 	public static synchronized SectionSet getInstance(){
 		if(instance==null){
 			instance=new SectionSet();
 		}
 		return instance;
 	}
 	
 	private SectionSet(){
 		sections=new LinkedList<Section>();
 	}
 	
 	public synchronized boolean exists(String name){
 		for (Section s:sections){
			if (s.equals(name)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public synchronized void add(Section section){
 		sections.add(section);
 	}
 	
	public synchronized Section getByIndex(int num){
		return (Section)sections.toArray()[num];
	}
 	
 	public synchronized Section getByName(String name){
 		for(Section s:sections){
			if (s.equals(name)){
 				return s; 
 			}
 		}
 		return null;
 	}
 	public synchronized List<Section> getSections(){
 		return sections;
 	}
 }
