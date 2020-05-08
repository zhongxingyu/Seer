 package se.enbohms.hhcib.facade;
 
 import java.io.Serializable;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.faces.view.ViewScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 
 import se.enbohms.hhcib.common.Constants;
 import se.enbohms.hhcib.entity.Subject;
 import se.enbohms.hhcib.entity.User;
 import se.enbohms.hhcib.entity.Vote;
 import se.enbohms.hhcib.service.api.CrudService;
 
 /**
  * Facade which is responsible for loading/fetching existing subject and
  * displaying the subject on a page
  * 
  * <p>
  * Note: All facades (incl this) should only act as a facade and delegate all
  * business logic to the existing services. Validation and coordination between
  * service is the facade main responsibility
  */
 @Named
 @ViewScoped
 public class ShowSubjectFacade implements Serializable {
 
 	private static final long serialVersionUID = -1712331748877385330L;
 
 	private String subjectId;
 	private Subject subject;
 	private String category;
 	private Double currentScore;
 
 	@Inject
 	private CrudService service;
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public String getSubjectId() {
 		return subjectId;
 	}
 
 	public void setSubjectId(String subjectId) {
 		this.subjectId = subjectId;
 	}
 
 	public Subject getSubject() {
 		return subject;
 	}
 
 	public Double getCurrentScore() {
 		return currentScore;
 	}
 
 	public void setCurrentScore(Double currentScore) {
 		this.currentScore = currentScore;
 	}
 
 	public void rate(AjaxBehaviorEvent actionEvent) {
 		User voter = getLoggedInUser();
 		if (getCurrentScore() > 0) {
 			getSubject().addVote(
 					Vote.of(voter.getUserName(), getCurrentScore()));
 			service.update(getSubject());
 		}
 	}
 
 	private User getLoggedInUser() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		HttpServletRequest request = (HttpServletRequest) context
 				.getExternalContext().getRequest();
 
 		User voter = (User) request.getSession().getAttribute(Constants.USER);
 		return voter;
 	}
 
 	/**
 	 * Fetches the subject from the database using the supplied subjectId and
 	 * category (parameters from the Http Request)
 	 */
 	public void fetchSubject() {
 		this.subject = service.find(subjectId);
 
		if (userHasVoted()) {
 			this.currentScore = subject.getVoters().get(
 					getLoggedInUser().getUserName());
 		}
 	}
 
 	private boolean userHasVoted() {
 		return subject.getVoters().containsKey(getLoggedInUser().getUserName());
 	}
 }
