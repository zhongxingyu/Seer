 package fr.frozentux.craftguard2.list;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 import fr.frozentux.craftguard2.config.ListLoader;
 
 public class ListManager {
 	
 	private CraftGuardPlugin plugin;
 	
 	private HashMap<String, List> groupsLists;
 	
 	private ArrayList<Integer> checkList;
 	
 	private ListLoader loader;
 	
 	public ListManager(CraftGuardPlugin plugin, ListLoader loader){
 		this.plugin = plugin;
 		this.loader = loader;
 	}
 	
 	public void init(){
 		checkList = new ArrayList<Integer>();
 		groupsLists = loader.load();
 	}
 	
 	public List getList(String name){
 		return groupsLists.get(name);
 	}
 	
 	public void addList(List list, boolean replaceIfExisting){
 		if(!groupsLists.containsKey(list.getName()) || replaceIfExisting){
 			Iterator<Integer> it = list.getIds(false).keySet().iterator();
 			while(it.hasNext()){
 				int id = it.next();
 				if(!checkList.contains(id))checkList.add(id);
 			}
 			groupsLists.put(list.getName(), list);
 		}
 	}
 	
 	public void addIdToCheckList(int id){
 		if(!checkList.contains(id))checkList.add(id);
 	}
 	
 	public boolean inCheckList(int id){
 		return checkList.contains(id);
 	}
 	
 	public Set<String> getListsNames(){
 		return groupsLists.keySet();
 	}
 	
 	public void saveList(List list){
 		loader.writeList(list, true);
 	}
 	
 	public void saveLists(){
 		loader.writeAllLists(this);
 	}
 	
 	public void removeList(String list){
 		groupsLists.remove(list);
 	}
 	
 }
