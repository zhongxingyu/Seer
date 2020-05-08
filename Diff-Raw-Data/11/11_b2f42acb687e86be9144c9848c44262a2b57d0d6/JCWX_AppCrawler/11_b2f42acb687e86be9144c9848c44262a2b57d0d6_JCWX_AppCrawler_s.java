 package com.candou.ic.navigation.jcwx.crawler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.candou.conf.Configure;
 import com.candou.ic.navigation.jcwx.bean.App;
 import com.candou.ic.navigation.jcwx.bean.Article;
 import com.candou.ic.navigation.jcwx.bean.Job;
 import com.candou.ic.navigation.jcwx.bean.Like;
 import com.candou.ic.navigation.jcwx.crawler.parser.JCWX_AppParser;
 import com.candou.ic.navigation.jcwx.dao.ArticleDao;
 import com.candou.ic.navigation.jcwx.dao.JobDao;
 import com.candou.ic.navigation.jcwx.dao.LikeDao;
 import com.candou.ic.navigation.jcwx.downloader.ImageDownloader;
 
 public class JCWX_AppCrawler {
 
     private static Logger log = Logger.getLogger(JCWX_AppCrawler.class);
     private static int batchAddLimit = 5;
     private static int batchAddLikeLimit = 5;
 
     public void start() {
         List<Job> jobs = JobDao.findJobs();
         List<Article> articles = new ArrayList<Article>();
         List<Like> likes = new ArrayList<Like>();
         List<Job> fetchedJobs = new ArrayList<Job>();
         List<Job> failedJobs = new ArrayList<Job>();
 
         int appcounter = 0;
         int all = jobs.size();
         for (Job job : jobs) {
             log.info("[" + ++appcounter + "/" + all + "] " + job.getTitle());
             App app = JCWX_AppParser.parse(job);
 
             if (app == null) {
                 failedJobs.add(job);
                 continue;
             }
 
             Article article = app.getArticles().get(0);
             //
             List<Like> localLikes = likeDownloader(app.getLikes());
             app.setLikes(localLikes);
 
             String localThumbnail = ImageDownloader.downloader(article.getThumbnail(), Configure.getProperty("db_path"), Configure.getProperty("save_path"));
             article.setThumbnail(localThumbnail);
 
             articles.add(app.getArticles().get(0));
             fetchedJobs.add(job);
 
             if (articles.size() >= batchAddLimit || appcounter >= all) {
                 ArticleDao.addBatchArticles(articles);
                 articles.clear();
                 JobDao.batchUpdateMatchedStatus(fetchedJobs);
                 fetchedJobs.clear();
                 log.info("batch add apps");
             }
 
             likes.addAll(app.getLikes());
 
             if (likes.size() >= batchAddLikeLimit || appcounter >= all) {
                 LikeDao.addBatchLikes(likes);
                 likes.clear();
                 log.info("batch add likes");
             }
 
             // update failed jobs
             JobDao.batchUpdateFailedStatus(failedJobs);
             log.info("faild fetch job:" + failedJobs);
         }
     }
 
     public static List<Like> likeDownloader(List<Like> remoteLikes) {
         List<Like> localLikes = new ArrayList<Like>();
 
         for (Like like : remoteLikes) {
             String localThumbnail = ImageDownloader.downloader(like.getThumbnail(), Configure.getProperty("db_path"), Configure.getProperty("save_path"));
 
             Like newLike = new Like();
             newLike.setArticleId(like.getArticleId());
            newLike.setTitle(newLike.getTitle());
             newLike.setLikeId(like.getLikeId());
             newLike.setThumbnail(localThumbnail);
             newLike.setCreatedAt(like.getCreatedAt());
             newLike.setUpdatedAt(like.getUpdatedAt());
 
             localLikes.add(newLike);
         }
 
         return localLikes;
     }
 
 }
