 package prefwork.rating.datasource;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.XMLConfiguration;
 
 import com.planetj.taste.impl.model.GenericItem;
 import com.planetj.taste.impl.model.GenericPreference;
 import com.planetj.taste.model.Preference;
 
 import prefwork.core.DataSource;
 import prefwork.core.Method;
 import prefwork.core.PrefWork;
 import prefwork.core.UserEval;
 import prefwork.core.Utils;
 import prefwork.rating.Rating;
 import prefwork.rating.method.ContentBased;
 import weka.core.Attribute;
 import weka.core.Instances;
 
 public class BehaviourAndContentData implements DataSource{
 	ContentDataSource behaviour;
 	ContentDataSource content;
 	String name;
 	boolean useBehaviour = true;
 	Map<Integer,Map<Integer, Double>> usersRatings = new HashMap<Integer, Map<Integer, Double>>();
 	public boolean isUseBehaviour() {
 		return useBehaviour;
 	}
 
 	public void useBehaviour() {
 		this.useBehaviour = true;
 	}
 	public void useContent() {
 		this.useBehaviour = false;
 	}
 
 	public ContentDataSource getBehaviour() {
 		return behaviour;
 	}
 
 	public void setBehaviour(ContentDataSource behaviour) {
 		this.behaviour = behaviour;
 	}
 
 	public ContentDataSource getContent() {
 		return content;
 	}
 
 	public void setContent(ContentDataSource content) {
 		this.content = content;
 	}
 
 	public void usePredictedRatingsForContent(Method m){
 		//Store the original ratings in the map.
 		Integer userIdDataset;
		int current = content.getCurrentUser();
		usersRatings = new HashMap<Integer, Map<Integer, Double>>();
 		content.restartUserId();
 		while( (userIdDataset= content.userId())!=null){
 			content.setFixedUserId(userIdDataset);
 			content.restart();
 			if (!content.hasNext())
 				continue;			
 			Rating rec;
 			while ((rec = (Rating)content.next()) != null) {
 				int userId = rec.getUserId();
 				int objectId = rec.getObjectId();
 				double rating = rec.getRating();
 
 				if(!usersRatings.containsKey(userId))
 					usersRatings.put(userId, new HashMap<Integer, Double>());
 				
 				usersRatings.get(userId).put(objectId, rating);
 			}
 		}
 		//Use the ratings from the behaviour method
 		content.restartUserId();
 		while( (userIdDataset= content.userId())!=null){
 			content.setFixedUserId(userIdDataset);
 			content.restart();
 			if (!content.hasNext())
 				continue;			
 			Rating rec;
 			while ((rec = (Rating)content.next()) != null) {
 				Double r = (Double)m.classifyRecord(rec);
 				if(r != null)
 					rec.setRating(r);
 			}
 		}
		content.setFixedUserId(current);
 	}
 	public void useUserRatingsForContent(){
 		//Use the user's ratings
 		Integer userIdDataset;
		int current = content.getCurrentUser();
 		content.restartUserId();
 		while( (userIdDataset= content.userId())!=null){
 			content.setFixedUserId(userIdDataset);
 			content.restart();
 			if (!content.hasNext())
 				continue;			
 			Rating rec;
 			while ((rec = (Rating)content.next()) != null) {
 				int userId = rec.getUserId();
 				int objectId = rec.getObjectId();
 				rec.setRating(usersRatings.get(userId).get(objectId));
 			}
		}
		content.setFixedUserId(current);
 	}
 	
 	@Override
 	public void shuffleInstances() {
 		if(useBehaviour)
 			behaviour.shuffleInstances();
 		else
 			content.shuffleInstances();
 	}
 
 	@Override
 	public int size() {
 		if(useBehaviour)
 			return behaviour.size();
 		else
 			return content.size();
 	}
 
 	@Override
 	public void setLimit(int from, int to, boolean recordsFromRange) {
 		if(useBehaviour)
 			behaviour.setLimit(from, to, recordsFromRange);
 		else
 			content.setLimit(from, to, recordsFromRange);		
 	}
 
 	@Override
 	public boolean hasNext() {
 		if(useBehaviour)
 			return behaviour.hasNext();
 		else
 			return content.hasNext();
 	}
 
 	@Override
 	public UserEval next() {
 		if(useBehaviour)
 			return behaviour.next();
 		else
 			return content.next();
 	}
 
 	@Override
 	public void restart() {
 		if(useBehaviour)
 			behaviour.restart();
 		else
 			content.restart();
 	}
 
 	@Override
 	public void remove() {
 		if(useBehaviour)
 			behaviour.remove();
 		else
 			content.remove();
 	}
 
 	@Override
 	public void setFixedUserId(Integer userId) {
 		if(useBehaviour)
 			behaviour.setFixedUserId(userId);
 		else
 			content.setFixedUserId(userId);
 	}
 
 	@Override
 	public Integer userId() {
 		if(useBehaviour)
 			return behaviour.userId();
 		else
 			return content.userId();
 	}
 
 	@Override
 	public boolean hasNextUserId() {
 		if(useBehaviour)
 			return behaviour.hasNextUserId();
 		else
 			return content.hasNextUserId();
 	}
 
 	@Override
 	public void restartUserId() {
 		if(useBehaviour)
 			behaviour.restartUserId();
 		else
 			content.restartUserId();
 	}
 
 	@Override
 	public void configDataSource(XMLConfiguration config, String section, String dataSourceName) {
 
 		try {
 			Configuration methodConf = config.configurationAt(section);
 
 			List<String> datasourcesContent = methodConf.getList("content.datasources");
 			List<String> datasourcesBehaviour = methodConf.getList("behaviour.datasources");
 			behaviour = (ContentDataSource)PrefWork.getDataSource(datasourcesBehaviour.get(0),methodConf.getString("behaviour.name"));		
 			content = (ContentDataSource)PrefWork.getDataSource(datasourcesContent.get(0),methodConf.getString("content.name"));
			behaviour.configDataSource(config, section+".behaviour", datasourcesBehaviour.get(0));
			content.configDataSource(config, section+".content", datasourcesContent.get(0));
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		
 	}
 
 	@Override
 	public String getName() {
 		//if(name != null)
 		//	return name;
 		return behaviour.getName()+content.getName();
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 }
