 package no.niths.application.rest;
 
 import java.util.ArrayList;
 
 import no.niths.application.rest.exception.DuplicateEntryCollectionException;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.FeedController;
 import no.niths.application.rest.lists.FeedList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Feed;
 import no.niths.domain.Student;
 import no.niths.domain.location.Location;
 import no.niths.services.interfaces.FeedService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.LocationService;
 import no.niths.services.interfaces.StudentService;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 @RequestMapping(AppConstants.FEEDS)
 public class FeedControllerImpl extends AbstractRESTControllerImpl<Feed>
 		implements FeedController {
 
 	private static final String STUDENT_REMOVED = "Student removed";
 	private static final String LOCATION_ADDED = "Location Added";
 	private static final String LOCATION_EXIST = "Location exist";
 	private static final String LOCATION_REMOVED = "Location removed";
 	private static final String LOCATION_NOT_FOUND = "Location not Found";
 	private static final String STUDENT_DOES_NOT_EXIST = "Student does not exist";
 	private static final String STUDENT_EXIST = "Student exist";
 	private static final String STUDENT_ADDED_TO_FEED = "Student added to feed";
 	private static final String STUDENT_NOT_FOUND = "Student not Found";
 	private static final String FEED_DOSE_NOT_EXIST = "Feed dose not exist";
 
 	private Logger logger = org.slf4j.LoggerFactory
 			.getLogger(FeedController.class);
 
 	@Autowired
 	private FeedService service;
 
 	@Autowired
 	private LocationService locService;
 	
 	@Autowired
 	private StudentService studentService;
 	
 	private FeedList list = new FeedList();
 
 	@Override
 	public ArrayList<Feed> getAll(Feed domain) {
 		list = (FeedList) super.getAll(domain);
 		clearRelations();
 		return list;
 	}
 
 	@Override
 	public ArrayList<Feed> getAll(Feed domain, @PathVariable int firstResult,
 			@PathVariable int maxResults) {
 		list = (FeedList) super.getAll(domain, firstResult, maxResults);
 		clearRelations();
 		return list;
 	}
 
 	private void clearRelations() {
 		for (Feed l : list) {
 			l.setStudent(null);
 			l.setLocation(null);
 		}
 	}
 
 	@Override
 	public Feed getById(@PathVariable Long id) {
		logger.debug("get by id in controller so good so far " + id);
 		Feed feed = super.getById(id);
 		if (feed.getStudent() != null) {
 			feed.getStudent().setCommittees(null);
 			feed.getStudent().setCommitteesLeader(null);
 			feed.getStudent().setCourses(null);
 			feed.getStudent().setFadderGroup(null);
 			feed.getStudent().setGroupLeaders(null);
 			feed.getStudent().setFeeds(null);
 			feed.getStudent().setRoles(null);
 		}
 		
 		if (feed.getLocation() != null) {
 			feed.getLocation().setFeeds(null);
 			feed.getLocation().setEvents(null);
 		}
 		return feed;
 	}
 
 	@Override
 	public GenericService<Feed> getService() {
 		return service;
 	}
 
 	@Override
 	public ListAdapter<Feed> getList() {
 		return list;
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@RequestMapping(value = "add/location/{feedId}/{locId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = LOCATION_ADDED)
 	public void addLocation(@PathVariable Long feedId,@PathVariable Long locId) {
 		Feed feed = service.getById(feedId);
 		ValidationHelper.isObjectNull(feed, FEED_DOSE_NOT_EXIST);
 		
 		if (feed.getLocation() != null && feed.getLocation().getId() == locId) {
 			logger.debug(LOCATION_EXIST);
 			throw new DuplicateEntryCollectionException(LOCATION_EXIST);
 		}
 		
 		Location location = locService.getById(locId);
 		ValidationHelper.isObjectNull(location, "Location does not exist");
 		
 		feed.setLocation(location);
 		service.update(feed);
 		logger.debug("Location added to feed");
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@RequestMapping(value = "remove/location/{feedId}/{locId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = LOCATION_REMOVED)
 	public void removeLocation(@PathVariable Long feedId) {
 		Feed feed = service.getById(feedId);
 		ValidationHelper.isObjectNull(feed, FEED_DOSE_NOT_EXIST);
 		
 		boolean isRemoved = false;
 		if (feed.getLocation() != null) {
 			isRemoved = true;
 			feed.setLocation(null);
 		}
 
 		if (isRemoved) {
 			service.update(feed);
 		} else {
 			logger.debug(LOCATION_NOT_FOUND);
 			throw new ObjectNotFoundException(LOCATION_NOT_FOUND);
 		}
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@RequestMapping(value = "add/student/{feedId}/{studentId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "STUDENT_ADDED_TO_FEED")
 	public void addStudent(@PathVariable Long feedId,@PathVariable Long studentId) {
 		Feed feed = service.getById(feedId);
 		ValidationHelper.isObjectNull(feed, FEED_DOSE_NOT_EXIST);
 		if (feed.getStudent() != null && feed.getStudent().getId() == studentId) {
 			logger.debug(STUDENT_EXIST);
 			throw new DuplicateEntryCollectionException(STUDENT_EXIST);
 		}
 		
 		Student student = studentService.getById(studentId);
 		ValidationHelper.isObjectNull(student, STUDENT_DOES_NOT_EXIST);
 		
 		feed.setStudent(student);
 		service.update(feed);
 		logger.debug(STUDENT_ADDED_TO_FEED);
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@RequestMapping(value = "remove/student/{feedId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = STUDENT_REMOVED)
 	public void removeStudent(@PathVariable Long feedId) {
 		Feed feed = service.getById(feedId);
 		ValidationHelper.isObjectNull(feed, FEED_DOSE_NOT_EXIST);
 	
 		boolean isRemoved = false;
 		if (feed.getStudent() != null) {
 			isRemoved = true;
 			feed.setStudent(null);
 		}
 
 		if (isRemoved) {
 			service.update(feed);
 		} else {
 			logger.debug(STUDENT_NOT_FOUND);
 			throw new ObjectNotFoundException(STUDENT_NOT_FOUND);
 		}
 	}
 }
