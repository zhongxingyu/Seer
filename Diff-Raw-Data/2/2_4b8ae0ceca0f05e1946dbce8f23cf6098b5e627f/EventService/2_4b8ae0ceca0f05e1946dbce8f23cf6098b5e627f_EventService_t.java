 package gossip.server.service;
 
 import gossip.mapper.EventMapper;
 import gossip.mapper.Page;
 import gossip.model.Event;
 import gossip.model.News;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class EventService {
 	@Autowired
 	private EventMapper eventMapper;
 	
 	@Autowired
 	private NewsService newsService;
 	
 //	private DLDELogger logger = new DLDELogger();
 
 	
 	public void update(Event event){
 		eventMapper.update(event);
 	}
 	
 	
 	public Event getEventById(int id) {
 		Event event = eventMapper.getEventById(id);
 		
 		String pages = event.getPages();
 		String[] ids = pages.split(",");
 		List<News> newsList = new ArrayList<News>();
 		for(String tmp : ids){
 			News news = newsService.getNewsById(Integer.parseInt(tmp));
 			newsList.add(news);
 		}
 		event.setNewsList(newsList);
 		return event;
 	}
 
 	private List<Event> getEventListTimeConstrain(Page page, int year, int month, int day) {
 		String time = year+"-"+"-"+month+"-"+day;
		List<Event> events = getEventListSelectWhat(page, time,null, null);
 		return events;
 	}
 	
 	public List<Event> getEventListSelectWhat(Page page, String createAt, String updateAt, String keyword){
 		return eventMapper.getEventListSelectWhat(page, createAt, updateAt, keyword);
 	}
 
 	public List<Event> getEventListSimply(Page page) {
 		return getEventListByRanking(page);
 	}
 	
 	public List<Event> getEventListByTimeDesc(Page page){
 		List<Event> events = eventMapper.getEventListOrderByWhat(page, Event.Time);
 		return events;
 	}
 	
 
 	/**
 	 * 从数据库里面读出按照recommeded排序的event，并且将id和排序号放入jsonObj
 	 * 
 	 * @return <ranking, event-id>...
 	 */
 	public List<Event> getEventListByRanking(Page page) {
 		List<Event> events = eventMapper.getEventListOrderByWhat(page, Event.Importance);
 		return events;
 	}
 	
 	
 
 	public EventMapper getEventMapper() {
 		return eventMapper;
 	}
 
 	public void setEventMapper(EventMapper eventMapper) {
 		this.eventMapper = eventMapper;
 	}
 
 	public static void main(String[] args) {
 		EventService eventService = new EventService();
 		Event event = eventService.getEventById(7);
 		System.out.println(event.getTitle() + "   " + event.getKeyWords());
 	}
 }
