 package db.service;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import db.models.Issue;
 import db.models.IssueType;
 import db.repositories.IssueRepository;
 import db.repositories.IssueTypeRepository;
 
 /**
  * @author Keith Buel
  * 
  */
 public class IssueService {
 
 	@Inject
 	private IssueRepository _issueRepository;
 
 	@Inject
 	private IssueTypeRepository issueTypeRepository;
 
 	public Issue save(Issue user) {
 		return _issueRepository.save(user);
 	}
 
 	public IssueType saveIssueType(IssueType issueType) {
 		return issueTypeRepository.save(issueType);
 	}
 
 	public List<Issue> getAllIssuesWithoutPictures() {
 		return _issueRepository.findAllWithoutPictures();
 	}
 
 	public Issue getIssueById(String id) {
 		return _issueRepository.findOne(id);
 	}
 
 	public List<Issue> findIssuesByTownName(String townName) {
 		return _issueRepository.findByTownNameWithoutPictures(townName);
 	}

	public void delete(String id) {
		_issueRepository.delete(id);
	}
 }
