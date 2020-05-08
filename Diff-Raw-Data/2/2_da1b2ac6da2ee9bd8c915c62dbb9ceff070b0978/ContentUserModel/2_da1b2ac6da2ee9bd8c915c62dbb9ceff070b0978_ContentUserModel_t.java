 package recommender.model;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import recommender.beans.IRStory;
 import recommender.beans.IRStoryUserStatistics;
 import recommender.beans.IRUser;
 import recommender.dataaccess.EventDAO;
 import recommender.model.bag.FeatureBag;
 
 public class ContentUserModel extends UserModel {
 
 	/**
 	 * Default Constructor
 	 */
 	public ContentUserModel() {
 		super();
 	}
 	
 	
 	/**
 	 * Constructor with a known user
 	 * @param current_user
 	 */
 	public ContentUserModel(IRUser current_user) {
 		super(current_user);
 	}
 
 
 	@Override
 	protected FeatureBag getCurrentFeatureBag() {
 		FeatureBag bag = new FeatureBag();
 		Map<IRStory, IRStoryUserStatistics> story_log;
 		
 		if(this.current_user != null) {
 			story_log = new LinkedHashMap<IRStory, IRStoryUserStatistics>();
 			EventDAO eventDAO = new EventDAO();
 			for(IRStoryUserStatistics stats : eventDAO.listUserStoryViews(this.current_user)) {
 				story_log.put(stats.getStory(), stats);
 				System.out.println(stats.getStory().getId() + "  /views:" + stats.getViews() + " /score:" + stats.getScore());
 			}
 		} else {
 			story_log = this.story_session;
 		}
 		
		for(IRStoryUserStatistics stats : story_log.values()) {
 			bag.addStoryData(stats);
 		}
 		
 		return bag;
 	}
 }
