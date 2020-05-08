 package com.alexecollins.releasemanager.web;
 
 import com.alexecollins.releasemanager.model.Release;
 import com.alexecollins.releasemanager.model.ReleaseRepository;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author alexec (alex.e.c@gmail.com)
  */
 @Controller
 @Slf4j
 public class ReleaseCalendarController {
 
 	@Autowired
 	ReleaseRepository releaseRepository;
 
 	@RequestMapping("/releases_calendar")
 	public
 	@ResponseBody
 	CalendarResponse calendar(long from, long to, HttpServletRequest request) {
 		ReleaseCalendarController.log.info("args={}..{}", from, to);
 		final List<Event> events = new ArrayList<>();
 		for (Release release : releaseRepository.findAll(new Sort(new Sort.Order("begin")))) {
 			if (release.getWhen() == null) continue;
 			final long when = release.getWhen().getTime();
 			final long duration = release.getDuration();
			//log.info(when.getEnd()*1000 +">" +from +"&&" +when.getBegin() *1000+"<"+ to);
			if (when * 1000 > from && when * 1000 < to)
				events.add(new Event(release.getId(), release.getName(), request.getContextPath() + "/releases/" + release.getId() + ".html", when * 1000, (when + duration) * 1000));
 		}
 
 		Collections.sort(events);
 
 		ReleaseCalendarController.log.info("events={}", events);
 
 		return new CalendarResponse(events);
 	}
 
 }
