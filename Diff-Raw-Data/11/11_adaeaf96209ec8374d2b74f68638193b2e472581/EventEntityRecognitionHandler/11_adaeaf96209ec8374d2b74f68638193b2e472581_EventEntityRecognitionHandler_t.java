 package gossip.gossip.event;
 
 import gossip.model.Event;
 import gossip.model.News;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.ansj.domain.Term;
 import org.ansj.recognition.NatureRecognition;
 import org.ansj.splitWord.analysis.ToAnalysis;
 /**
  * 事件内实体识别
  * Task1: 新闻发生地识别 Location
 	 * step1. 找所有新闻的地点词
 	 * step2. 判断地点词的可能性,数量最多的就是
  * Task2: 事件发生人识别 People
 	 * step1. 找到新闻中人名
 	 * step2. 找到最重要的人名
  * @author ChenJie
  *
  * Nov 13, 2013
  */
 public class EventEntityRecognitionHandler implements Handler{
 	
 	private Map<String, HashMap<String, Integer>> titleEntityStore;
 	private Map<String, HashMap<String, Integer>> bodyEntityStore;
 	
 	@Override
 	public void handle(Event event) {
 		titleEntityStore = new HashMap<String, HashMap<String, Integer>>();
 		bodyEntityStore = new HashMap<String, HashMap<String, Integer>>();
 		List<News> newsList = event.getNewsList();
 		for(News news : newsList){
 			String title = news.getTitle();
 			String body = news.getBody();
 			entityRecognition(title, titleEntityStore);
 			entityRecognition(body, bodyEntityStore);
 		}
 		/**
 		 * 1.从标题中计算实体
 		 */
 		Entity peopleInTitle = findMost(People, titleEntityStore);
 		Entity locationInTitle = findMost(Location, titleEntityStore);
 		
 		/**
 		 * 2.从正文中计算实体
 		 */
 		Entity peopleInBody = findMost(People, bodyEntityStore);
 		Entity locationInBody = findMost(Location, bodyEntityStore);
 		
 		/**
 		 * 选择逻辑
 		 */
 		
 		String people = compare(peopleInTitle, peopleInBody);
 		String location = compare(locationInTitle, locationInBody);;
 		
 		event.setStartedLocation(location);
 		event.setImportantPeople(people);
 	}
 
 	/**
 	 * 从两个实体中选择一个
 	 * 如果正文中的数量大于等于标题中的2呗
 	 * @param entity1 标题中的实体
 	 * @param entity2 正文中的实体
 	 * @return
 	 */
 	public String compare(Entity entity1, Entity entity2){
		String value = null;
 		if(entity1 == null || entity2 == null){
 			if(entity1 == null){
				value = entity2 == null ? null : entity2.getName();
 			}else{
 				value = entity1.getName();
 			}
 		}else{
 			int count1 = entity1.getCount();
 			int count2 = entity2.getCount();
 			
 			if(count2 >= 2 * count1){
 				value = entity2.getName();
 			}else{
 				value = entity1.getName();
 			}
 		}
		if(value!=null && value.length()>1){
 			return value;
 		}else{
			return null;
 		}
 		
 	}
 	
 	/**
 	 * 没有区分标题和正文，仅从数量判断
 	 * @param entityType
 	 * @return
 	 */
 	private Entity findMost(String entityType, Map<String, HashMap<String, Integer>> entityStore){
 		String most = "";
 		HashMap<String, Integer> entities = entityStore.get(entityType);
 		if(entities == null){
 			return null;
 		}
 		int max = 0;
 		for(Entry<String, Integer> entry : entities.entrySet()){
 			int tmpCount = entry.getValue();
 			if(tmpCount > max){
 				most = entry.getKey();
 				max = tmpCount;
 			}
 		}
 		return new Entity(most, max);
 	}
 	
 	
 	private final static String Location = "location";
 	private final static String People = "people";
 	
 	private void entityRecognition(String content, Map<String, HashMap<String, Integer>> entityStore){
 		HashMap<String, Integer> locations = entityStore.get(Location);
 		HashMap<String, Integer> people = entityStore.get(People);
 		List<Term> terms = ToAnalysis.parse(content);
 		new NatureRecognition(terms).recognition() ;
         for(Term term : terms){
         	String name = term.getName();
         	if(term.getNatrue().natureStr.equalsIgnoreCase("ns")){//地点名词
         		if(locations == null){
         			locations = new HashMap<String, Integer>();
         		}
         		Integer count = locations.get(name);
         		if(count == null){
         			count = 1;
         		}
         		count = count+1;
         		locations.put(name, count);
         	}else if(term.getNatrue().natureStr.equalsIgnoreCase("nr")){//人名名词
         		if(people == null){
         			people = new HashMap<String, Integer>();
         		}
         		Integer count = people.get(name);
         		if(count == null){
         			count = 1;
         		}
         		count = count+1;
         		people.put(name, count);
         	}
         }
         entityStore.put(Location, locations);
         entityStore.put(People, people);
 	}
 }
 class Entity{
 	private String name;
 	private int count;
 	public Entity(String name, int count){
 		this.name = name;
 		this.count = count;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public int getCount() {
 		return count;
 	}
 	public void setCount(int count) {
 		this.count = count;
 	}
 	
 }
