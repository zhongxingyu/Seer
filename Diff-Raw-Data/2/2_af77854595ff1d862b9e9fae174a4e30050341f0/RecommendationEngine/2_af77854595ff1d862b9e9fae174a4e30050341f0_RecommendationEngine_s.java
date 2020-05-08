 package controllers;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.jsoup.Jsoup;
 
 import com.aliasi.chunk.Chunk;
 import com.aliasi.cluster.KMeansClusterer;
 import com.aliasi.util.FeatureExtractor;
 import com.google.gson.JsonObject;
 import com.sun.cnpi.rss.elements.Item;
 import com.sun.cnpi.rss.elements.Rss;
 import com.sun.cnpi.rss.parser.RssParser;
 import com.sun.cnpi.rss.parser.RssParserException;
 import com.sun.cnpi.rss.parser.RssParserFactory;
 
 import models.Choice;
 import models.Feed;
 import models.LikeFrequency;
 import models.LikeFrequencyComparator;
 import models.LikeGroup;
 import models.Likes;
 import models.Reason;
 import models.Recommendation;
 import models.Topic;
 import models.User;
 
 import play.modules.facebook.FbGraph;
 import play.modules.facebook.FbGraphException;
 import play.mvc.Controller;
 import play.mvc.Scope.Session;
 import pt.voiceinteraction.keyphraseextraction.KeyPhrase;
 
 public class RecommendationEngine extends Controller{
 
 	public static void index() {
 		//Topic topic = Topic.findById((long)22334);
 		//System.out.println(topic.description);
 		List<Topic> topics = Topic.all().fetch();
 		for (int i = 0; i < topics.size(); i++) {
 			addTagsToTopic(topics.get(i));
 		}
 		//runKMeans();
 		//runKMeans();
 		/* Set up stuff */
 		//RSSEngine.fetchNews();
 		/*
 		LikeGroup.generateLikeGroupsFromStaticArray();
 		Application.getUserLikes();
 		Application.generateFeeds();
 		RSSEngine.fetchNews();*/
 		/*
 		Session.current().put("user", 1243350056);
 		Date date = new Date();
 		Random random = new Random(date.getTime());
 		int i = random.nextInt(2);
 		if (i == 0) {
 			renderArgs.put("choice", likeRankingChoice());
 		} else {
 			renderArgs.put("choice", genericVsCalculatedChoice());
 		}
     	renderTemplate("Recommendation/index.html");
     	*/
 	}
 	
 	public static Recommendation recommendationForFriendsWithUserIds(List<Long> userIds) {
 		List<User> people = new ArrayList<User>();
 		
 		for (int i = 0; i < userIds.size(); i++) {
 			User user = User.find("byUserId", userIds.get(i)).first();
 			people.add(user);
 		}
 		
 		return recommendationForFriends(people);
 	}
 	
 	public static Recommendation recommendationForFriends(List<User> people) {
 		String tag = LikeGroup.getLikeGroupFromCategory(findLikeIntersection(people));
 		
     	List<Topic> topics = Topic.find("select t from Topic t join t.tags as tag where tag = ?", tag).fetch();
     	
     	/* If no topic is found, return a generic result */
     	if (topics == null || topics.size() == 0) {
     		System.out.println("ERROR: We found no topics! Going to generic search results");
     		topics =  Topic.find("select t from Topic t join t.tags as tag where tag = ?", "Generic").fetch();
     	}
 
     	Topic topic = getRandomTopicFrom(topics);
     	
     	Recommendation rec = new Recommendation(topic);
     	rec.reasons.add(Reason.getReasonWithType(Reason.LIKE | Reason.MUTUAL));
     	rec.save();
     	return rec;
 	}
 	
 	public static String findLikeIntersection(List<User> people) {
 		Map<String, Integer> likeFrequencyMap = new HashMap<String, Integer>();
 		for (int i = 0; i < people.size(); i++) {
 			User user = people.get(i);
 			if (user.allUserLikes == null || user.allUserLikes.size() == 0 ||
 					user.frequencyOfLikes == null || user.frequencyOfLikes.size() == 0) {
 				user.getLikes();
 			}
 			List<LikeFrequency> likeFrequencies = user.frequencyOfLikes;
 			Collections.sort(likeFrequencies, new LikeFrequencyComparator());
 			for (int j = 0; j < likeFrequencies.size(); j++) {
 				LikeFrequency lf = likeFrequencies.get(j);
 				if (likeFrequencyMap.containsKey(lf.likeCategory)) {
 					likeFrequencyMap.put(lf.likeCategory, likeFrequencyMap.get(lf.likeCategory)+lf.frequency);
 				} else {
 					likeFrequencyMap.put(lf.likeCategory, lf.frequency);
 				}
 			}
 		}
 		Map.Entry<String, Integer> maxEntry= null;
 		Iterator iter = likeFrequencyMap.entrySet().iterator();
 		
 		while (iter.hasNext()) {
 			Map.Entry<String, Integer> lfmEntry = (Entry<String, Integer>) iter.next();
 			
 			if (maxEntry == null) {
 				maxEntry = lfmEntry;
 			} else {
 				if (lfmEntry.getValue() > maxEntry.getValue()) {
 					maxEntry = lfmEntry;
 				}
 			}
 		}
 			
 		return maxEntry.getKey();
 	}
 	
 	/* Choice Generation */
 	public static Choice genericVsCalculatedChoice() {
 		List<Topic> topics = Topic.find("select t from Topic t join t.tags as tag where tag = ?", "Generic").fetch();
 		Topic topic1 = getRandomTopicFrom(topics);
 		
 		User user = User.find("byUserId", Session.current().get("user")).first();
     	String tag = "Generic";
 		if (user != null) {
 			if (user.frequencyOfLikes == null || user.frequencyOfLikes.size() == 0) {
 				Application.getUserLikes();
 			}
 			List<LikeFrequency> likeFrequencies = user.frequencyOfLikes;
 			Collections.sort(likeFrequencies, new LikeFrequencyComparator());
 			LikeFrequency lf;
 			if (likeFrequencies.size() > 0) {
 				lf = likeFrequencies.get(0);
 				tag = LikeGroup.getLikeGroupFromCategory(lf.likeCategory);
 			}
 		} else {
 			System.out.println("ERROR: Could not find user in session.");
 			return null;
 		}
 		
 		List<Topic> topics2 = Topic.find("select t from Topic t join t.tags as tag where tag = ?", tag).fetch();
 		Topic topic2 = getRandomTopicFrom(topics2);
 		
 		Recommendation rec1 = new Recommendation(topic1);
     	Reason genericReason = Reason.getCategoryReason(Reason.GENERIC);
     	rec1.addReason(genericReason);
     	rec1.save();
     	
     	Recommendation rec2 = new Recommendation(topic2);
     	Reason likeCategoryReason = Reason.getCategoryReason(Reason.LIKE);
     	rec2.addReason(likeCategoryReason);
     	rec2.save();
     	
     	Choice choice = new Choice();
     	choice.addRecommendation(rec1);
     	choice.addRecommendation(rec2);
     	choice.save();
 		
 		return choice;
 	}
 	
 	public static Choice likeRankingChoice() {
 		Topic topic1 = fetchTopic(0);
     	Topic topic2 = fetchTopic(1);
  
     	Recommendation rec1 = new Recommendation(topic1);
     	Reason likeCategoryReason = Reason.getLikeCategoryReason();
     	rec1.addReason(likeCategoryReason);
     	rec1.save();
     	
     	Recommendation rec2 = new Recommendation(topic2);
     	rec2.addReason(likeCategoryReason);
     	rec2.save();
     	
     	Choice choice = new Choice();
     	choice.addRecommendation(rec1);
     	choice.addRecommendation(rec2);
     	choice.save();
     	
     	return choice;
 	}
     
     public static Topic fetchTopic(int seed) {
     	User user = User.find("byUserId", Session.current().get("user")).first();
     	
     	return fetchTopicForUser(user, seed);
     }
     
     public static Topic fetchTopicForUser(User user, int seed) {
     	JsonObject profile;
     
     	String tag;
     	if (seed == 0) {
     		tag = "Technology";
     	} else {
     		tag = "Fashion";
     	}
     	
     	if (LikeGroup.count() == 0) {
     		LikeGroup.generateLikeGroupsFromStaticArray();
     	}
     	
     	if (Feed.count() == 0) {
     		Application.generateFeeds();
     		RSSEngine.fetchNews();
     	}
     	
 		if (user != null) {
 			System.out.println("Got a user :"+user);
 			if (user.frequencyOfLikes == null || user.frequencyOfLikes.size() == 0) {
 				System.out.println("ERROR: This should not happen");
 			}
 			List<LikeFrequency> likeFrequencies = user.frequencyOfLikes;
 			Collections.sort(likeFrequencies, new LikeFrequencyComparator());
 			LikeFrequency lf;
 			System.out.println("Found "+likeFrequencies.size()+" Likes");
 			if (likeFrequencies.size() > seed) {
 				lf = likeFrequencies.get(seed);
 				tag = LikeGroup.getLikeGroupFromCategory(lf.likeCategory);
 				System.out.println("Setting tag to "+tag);
 			}
 		} else {
 			System.out.println("ERROR: Could not find user in session.");
 			return null;
 		}
 
     	List<Topic> topics = Topic.find("select t from Topic t join t.tags as tag where tag = ?", tag).fetch();
     	
     	/* If no topic is found, return a generic result */
     	if (topics == null || topics.size() == 0) {
     		System.out.println("ERROR: We found no topics! Going to generic search results");
     		topics =  Topic.find("select t from Topic t join t.tags as tag where tag = ?", "Generic").fetch();
     	}
 
     	return getRandomTopicFrom(topics);
     }
     
     /* Response */
     public static void processChoice(long choiceId, int selection) {
     	Choice choice = Choice.findById(choiceId);
     	System.out.println(choice+" "+selection);
     	if (choice == null) {
     		System.out.println("ERROR: Could not find choice with id "+choiceId);
     		index();
     	}
     	choice.selection = selection;
     	choice.save();
     	index();
     }
     
     /* Helpers */
     public static Topic getRandomTopicFrom(List<Topic> topics) {
     	Date date = new Date();
     	Random random = new Random(date.getTime());
     	
     	int i = random.nextInt(topics.size());
     	return topics.get(i);
     }
     
     public static int addTagsToTopic(Topic topic) {
     	while(topic.tags.size() > 1) {
     		topic.tags.remove(1);
     		topic.save();
     	}
     	
         try {
             int nrKeyphrases = 0;
             StringTokenizer st = new StringTokenizer(topic.description);
             int numberOfWords = st.countTokens();
             nrKeyphrases = Math.max(5, Math.min(30, numberOfWords / 30));
 
            EnglishKeyPhraseExtractor extractor = new EnglishKeyPhraseExtractor("/Users/sophiez/tmp/English_KEModel_manualData",
                     "data/models/en_US/hub4_all.np.4g.hub97.1e-9.clm",
                     "data/models/en_US/left3words-wsj-0-18.tagger",
                     "data/stopwords/stopwords_en.txt");
            
             String[] texts = new String[]{
             		topic.description
             };
             for (KeyPhrase keyPhrase : extractor.getKeyphrases(nrKeyphrases, Arrays.asList(texts))) {
                 System.out.println("Got keyphrase: "+keyPhrase.getKeyPhrase());
             	topic.tags.add(keyPhrase.getKeyPhrase());
             	topic.save();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return 0;
     }
     
     public static void runKMeans() {
     	FeatureExtractor<Topic> featureExtractor = new FeatureExtractor<Topic>() {
 				@Override
 				public Map<String, ? extends Number> features(Topic topic) {
 					HashSet hs = new HashSet();
 					for (int i = 0; i < StaticData.feedCategories.length; i++) {
 						hs.add(StaticData.feedCategories[i]);
 					}
 
 					HashMap<String, Double> hm = new HashMap<String, Double>();
 					List<String> tags = topic.tags;
 
 					for (int i = 0; i < tags.size(); i++) {
 						String tag = tags.get(i);
 						/*
 						if (hs.contains(tag)) {
 							continue;
 						}*/
 						tag = tag.toLowerCase();
 					    StringTokenizer st = new StringTokenizer(tags.get(i));
 					    if (st.countTokens() > 1) {
 					    	while (st.hasMoreTokens()) {
 					    		String token = st.nextToken();
 					    		hm.put(token, (double) token.hashCode());
 					    	}
 					    }
 						hm.put(tag, (double) tag.hashCode());
 					}
 					return hm;
 				}
     	};
     	KMeansClusterer<Topic> kmc = new KMeansClusterer<Topic>(featureExtractor, 30, 1, true, 10.0);
     	
     	List<Topic> topics = Topic.all().fetch();
     	HashSet<Topic> hs = new HashSet<Topic>(topics);
     	
     	Set<Set<Topic> > topicClusters = kmc.cluster(hs);
     	Iterator<Set<Topic> > topicClusterIter = topicClusters.iterator();
     	
     	while(topicClusterIter.hasNext()) {
     		Set<Topic> topicCluster = topicClusterIter.next();
     		Iterator<Topic> topicIter = topicCluster.iterator();
     		System.out.println();
     		System.out.println("NEW CLUSTER: ");
     		while(topicIter.hasNext()) {
     			Topic nextTopic = topicIter.next();
     			System.out.println(nextTopic.tags);
     		}
     		
     		System.out.println();
     	}
     }
 }
