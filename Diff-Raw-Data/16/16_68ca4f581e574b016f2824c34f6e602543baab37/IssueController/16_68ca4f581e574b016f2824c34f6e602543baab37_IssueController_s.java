 package controllers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 
 import play.data.DynamicForm;
 import play.data.Form;
 import play.mvc.Result;
 import service.GoogleService;
 import db.models.Issue;
 import db.models.IssueType;
 import db.repositories.IssueTypeRepository;
 import db.service.IssueService;
 
 @Controller
 public class IssueController extends FixItBaseController {
 
 	private static Logger _logger = LoggerFactory.getLogger(IssueController.class);
 
 	@Inject
 	IssueService _issueService;
 
 	@Inject
 	GoogleService _googleService;
 
 	@Inject
 	IssueTypeRepository _issueTypeRepository;
 
 	public Result issueById(String id) {
 		Issue issue = _issueService.getIssueById(id);
 		return okAsJSON(issue);
 	}
 
 	public Result saveIssue() {
 
 		DynamicForm form = Form.form().bindFromRequest();
 		System.out.println(form.get("lat"));
 		System.out.println(form.get("long"));
 		System.out.println(form.get("picture"));
 		System.out.println(form.get("type"));
 		Double coordLat = Double.parseDouble(form.get("lat"));
 		Double coordLong = Double.parseDouble(form.get("long"));
 		String picture = form.get("picture");
 		String type = form.get("type");
 
 		Issue issue = new Issue();
 		issue.setLatitude(coordLat);
 		issue.setLongitude(coordLong);
 		issue.setImage(picture);
 		issue.setIssueType(type);
 		issue.setTownName(_googleService.getTownNameByLatLong(coordLat, coordLong));
 		_issueService.save(issue);
 
 		return ok();
 	}
 
 	public Result issueTypeMap() {
 		Map<String, IssueType> issueTypeMap = new HashMap<String, IssueType>();
 		for (IssueType type : _issueTypeRepository.findAll()) {
 			issueTypeMap.put(type.getId(), type);
 		}
 
 		return okAsJSON(issueTypeMap);
 	}
 
 }
