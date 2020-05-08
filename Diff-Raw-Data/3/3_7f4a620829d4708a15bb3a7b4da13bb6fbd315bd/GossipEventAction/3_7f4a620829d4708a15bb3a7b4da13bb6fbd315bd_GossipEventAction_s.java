 package gossip.gossip.action;
 
 import gossip.gossip.service.GossipEventService;
 import gossip.gossip.service.GossipNewsService;
 import gossip.model.Event;
 import gossip.model.News;
 
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 @RequestMapping("/gossip/event")
 @Controller
 public class GossipEventAction {
 
 	@Autowired
 	private GossipEventService eventService;
 	@Autowired
 	private GossipNewsService newsService;
 	//测试用
 	@RequestMapping("/detect")
 	public void detectEvent(){
 		Date now = new Date(System.currentTimeMillis());
 		//step0. 取出今天的新闻包括本周的未被识别为事件的新闻
 		List<News> newsToday = newsService.getNewsLast7Days(now);
 		System.out.println("newsToday need to compute is : "+newsToday.size());
 		//step1. 找出今天的events，标记没有存为事件的那些新闻
 		List<Event> eventsTody = eventService.computeEventFromNews(newsToday);
 		//step2. 找出前面7天的events
 		List<Event> eventsLast7Days = eventService.getEventsLast7Days(now);
 		//step3. 合并今天的与本周的，并找出新的
 		List<Event> newOrUpdatedEvents = eventService.mergeEvents(eventsLast7Days, eventsTody);
 		//step4. 插入or更新这些events
 		eventService.updateOrInsert(newOrUpdatedEvents);
 	}
 
 }
