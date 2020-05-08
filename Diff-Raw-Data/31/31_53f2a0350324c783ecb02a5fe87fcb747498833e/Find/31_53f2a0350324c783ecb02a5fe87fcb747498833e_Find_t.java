 package answers.test;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import answers.model.Hashtags;
 import answers.model.Tweets;
 
 import com.mysql.clusterj.ClusterJHelper;
 import com.mysql.clusterj.Constants;
 import com.mysql.clusterj.Query;
 import com.mysql.clusterj.Session;
 import com.mysql.clusterj.SessionFactory;
 import com.mysql.clusterj.query.Predicate;
 import com.mysql.clusterj.query.QueryDomainType;
 
 public class Find {
     static String usageMessage = "Find --author <author> --hashtag <hashtag>"
         + " --recent <minutes_ago> --range <minutes_ago_low> <minutes_ago_high>"
         + " --skip <skip> --limit <limit>";
     static SessionFactory sessionFactory;
     static Session session;
     public static void main(String[] args) {
         String author = null;
         String hashtag = null;
         String recent = null;
         String range_low = null;
         String range_high = null;
         String skip = null;
         String limit = null;
         long skipValue = 0;
         long limitValue = Long.MAX_VALUE;
         for (int i = 0; i < args.length; ++i) {
             String arg = args[i];
             if ("--author".equals(arg)) {
                 author = args[++i];
             } else if ("--hashtag".equals(arg)) {
                 hashtag = args[++i];
             } else if ("--recent".equals(arg)) {
                 recent = args[++i];
             } else if ("--range".equals(arg)) {
                 range_low = args[++i];
                 range_high = args[++i];
             } else if ("--skip".equals(arg)) {
                 skip = args[++i];
                 skipValue = Long.parseLong(skip);
             } else if ("--limit".equals(arg)) {
                 limit = args[++i];
                 limitValue = Long.parseLong(limit);
             } else {
                 System.out.println(usageMessage);
                 System.exit(1);
             }
         }
         System.out.println("Find: "
                 + " author: " + author
                 + " hashtag: " + hashtag
                 + " recent: " + recent
                 + " range_low: " + range_low
                 + " range_high: " + range_high
                 + " skip: " + skip
                 + " limit: " + limit
                 );
         session = getSession();
         QueryDomainType<Hashtags> qdtHashtags = session.getQueryBuilder().createQueryDefinition(Hashtags.class);
         QueryDomainType<Tweets> qdtTweets = session.getQueryBuilder().createQueryDefinition(Tweets.class);
         Predicate predicateHashtagHashtag = qdtHashtags.get("hashtag").equal(qdtHashtags.param("hashtag"));
         Predicate predicateHashtagAuthor = qdtHashtags.get("author").equal(qdtHashtags.param("author"));
         Predicate predicateHashtagRecent = qdtHashtags.get("time_stamp").greaterEqual(qdtHashtags.param("recent"));
         Predicate predicateTweetRecent = qdtTweets.get("time_stamp").greaterEqual(qdtTweets.param("recent"));
         Predicate predicateTweetRange = qdtTweets.get("time_stamp").between(qdtTweets.param("range_low"), qdtTweets.param("range_high"));
         Predicate predicateTweetAuthor = qdtTweets.get("author").equal(qdtTweets.param("author"));
         Query<Tweets> queryTweets = null;
         Query<Hashtags> queryHashtags = null;
         if (hashtag != null) {
             Date recentDate = null;
             // scan index by hashtag
             Predicate predicateHashtag = predicateHashtagHashtag;
             if (author != null) {
                 // filter by author
                 predicateHashtag = predicateHashtag.and(predicateHashtagAuthor);
             }
             if (recent != null) {
                 long millis = new Date().getTime() - (Long.parseLong(recent) * 1000);
                 recentDate = new Date(millis);
                 predicateHashtag = predicateHashtag.and(predicateHashtagRecent);
             }
             qdtHashtags.where(predicateHashtag);
             queryHashtags = session.createQuery(qdtHashtags);
             queryHashtags.setParameter("hashtag", hashtag);
             if (author != null) {
                 queryHashtags.setParameter("author", author);
             }
             if (recent != null) {
                 queryHashtags.setParameter("recent", recentDate);
             }
             
         } else if (author != null) {
             Date recentDate = null;
             // scan by author
             Predicate predicateTweets = predicateTweetAuthor;
             if (recent != null) {
                 long millis = new Date().getTime() - (Long.parseLong(recent) * 60000);
                 recentDate = new Date(millis);
                 predicateTweets = predicateTweets.and(predicateTweetRecent);
             }
             qdtTweets.where(predicateTweets);
             queryTweets = session.createQuery(qdtTweets);
             queryTweets.setParameter("author", author);
             if (recent != null) {
                 queryTweets.setParameter("recent", recentDate);
             }
 
         } else if (recent != null) {
             Date recentDate = null;
             // scan by time_stamp
             Predicate predicateTweets = predicateTweetRecent;
             long millis = new Date().getTime() - (Long.parseLong(recent) * 60000);
             recentDate = new Date(millis);
             predicateTweets = predicateTweets.and(predicateTweetRecent);
             qdtTweets.where(predicateTweets);
             queryTweets = session.createQuery(qdtTweets);
             queryTweets.setParameter("recent", recentDate);
 
         } else if (range_low != null) {
             Date lowDate = null;
             Date highDate = null;
             // scan by time_stamp
             Predicate predicateTweets = predicateTweetRange;
             long lowMillis = new Date().getTime() - (Long.parseLong(range_low) * 60000);
             lowDate = new Date(lowMillis);
             long highMillis = new Date().getTime() - (Long.parseLong(range_high) * 60000);
             highDate = new Date(highMillis);
             qdtTweets.where(predicateTweets);
             queryTweets = session.createQuery(qdtTweets);
             queryTweets.setParameter("range_low", lowDate);
             queryTweets.setParameter("range_high", highDate);
         }
 
        if (queryHashtags != null) {
//            if (skip != null || limit != null) {
//                queryHashtags.setLimits(skipValue, limitValue);
//            }
            List<Hashtags> results = queryHashtags.getResultList();
            Map<String, Object> explain = queryHashtags.explain();
            System.out.println("Hashtags scan type: " + explain.get(Query.SCAN_TYPE) + " Index: " + explain.get(Query.INDEX_USED));
            for (Hashtags result: results) {
                String tweet_id = result.getTweet_id();
                Tweets tweet = session.find(Tweets.class, tweet_id);
                System.out.println("Hashtag: " + result.getHashtag()
                        + " author: " + result.getAuthor()
                        + " tweet_id: " + result.getTweet_id()
                        + " tweet: " + tweet.getTweet()
                        );
            }
        } else if (queryTweets != null) {
//            if (skip != null || limit != null) {
//                queryTweets.setLimits(skipValue, limitValue);
//            }
            List<Tweets> results = queryTweets.getResultList();
            Map<String, Object> explain = queryTweets.explain();
            System.out.println("Tweets scan type: " + explain.get(Query.SCAN_TYPE) + " Index: " + explain.get(Query.INDEX_USED));
            for (Tweets result: results) {
                System.out.println("Tweet id: " + result.getId()
                        + " author: " + result.getAuthor()
                        + " time_stamp: " + result.getTime_stamp()
                        + " tweet: " + result.getTweet());
            }
        }
         session.close();
         sessionFactory.close();
     }
     private static Session getSession() {
         Properties props = new Properties();
         props.put(Constants.PROPERTY_CLUSTER_CONNECTSTRING, "localhost:1186");
         sessionFactory = ClusterJHelper.getSessionFactory(props);
         return sessionFactory.getSession();
     }
 }
