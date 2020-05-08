 package nz.co.searchwellington.repositories;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import nz.co.searchwellington.model.ArchiveLink;
 import nz.co.searchwellington.model.CalendarFeed;
 import nz.co.searchwellington.model.CommentFeed;
 import nz.co.searchwellington.model.DiscoveredFeed;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.ResourceImpl;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.Watchlist;
 import nz.co.searchwellington.model.Website;
 import nz.co.searchwellington.model.WebsiteImpl;
 
 import org.apache.lucene.queryParser.ParseException;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.joda.time.DateTime;
 
 
 public abstract class HibernateResourceDAO extends AbsractResourceDAO implements ResourceRepository {
 
     SessionFactory sessionFactory;
     
     public HibernateResourceDAO() {
     }
     
     
     public HibernateResourceDAO(SessionFactory sessionFactory) {     
         this.sessionFactory = sessionFactory;
     }
     
     
     @SuppressWarnings("unchecked")
     public Set<Integer> getAllResourceIds() {
         Set<Integer> resourceIds = new HashSet<Integer>();        
         Session session = sessionFactory.getCurrentSession();
         List<Integer> rows = session.createQuery("select id from nz.co.searchwellington.model.ResourceImpl").setFetchSize(100).list();        
         for (Integer row : rows) {
             resourceIds.add(row);
         }               
         return resourceIds;
     }
     
     
     
 
     @SuppressWarnings("unchecked")
     final public List<Feed> getAllFeeds() {
         return sessionFactory.getCurrentSession().createCriteria(Feed.class).
         addOrder(Order.desc("latestItemDate")).
         addOrder(Order.asc("name")).
         setCacheable(true).
         list();    
     }
     
     
     
     @SuppressWarnings("unchecked")
     final public List<Feed> getFeedsToRead() {
         return sessionFactory.getCurrentSession().createCriteria(Feed.class).
         add(Restrictions.ne("acceptancePolicy", "ignore")).
         addOrder(Order.asc("lastRead")).
         setCacheable(false).
         list();
     }
     
     
     
     @SuppressWarnings("unchecked")
     final public List<Resource> getAllCalendarFeeds() {
         return sessionFactory.getCurrentSession().createCriteria(CalendarFeed.class).       
         addOrder(Order.asc("name")). 
         setCacheable(true).
         list();    
     }
     
     
     
     
     
     
     
     
         
     @SuppressWarnings("unchecked")
     // TODO add discovered timestamp and order by that.
     final public List<DiscoveredFeed> getAllDiscoveredFeeds() {
         return sessionFactory.getCurrentSession().createCriteria(DiscoveredFeed.class).
         setCacheable(true).
         addOrder(Order.desc("id")).
         list();                
     }
     
     
     
     
     @SuppressWarnings("unchecked")
     final public List<Website> getPublisherFeeds(Website publisher) {             
         return sessionFactory.getCurrentSession().createCriteria(Feed.class).
         add(Restrictions.eq("publisher", publisher)).
         addOrder(Order.asc("name")).
         list();
     }
   
     @SuppressWarnings("unchecked")  
     final public List<Website> getPublisherWatchlist(Website publisher) {    
         return sessionFactory.getCurrentSession().createCriteria(Watchlist.class).
                 add(Restrictions.eq("publisher", publisher)).
                 addOrder(Order.asc("name")).
                 list();
     }
     
     
     
     
     
    
     
     
     @SuppressWarnings("unchecked")
     public List<Newsitem> getRecentUntaggedNewsitems() {
         return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
                 add(Restrictions.isEmpty("tags")).
                 add(Expression.eq("httpStatus", 200)).
                 addOrder(Order.desc("date")).
                 setMaxResults(12).
                 setCacheable(true).list();        
     }
 
 
     
     @SuppressWarnings("unchecked")
     public List<Object[]> getAllPublishers(boolean showBroken, boolean mustHaveNewsitems) throws IOException {                 
         // TODO implement show broken option.
         if (mustHaveNewsitems) {
         return sessionFactory.getCurrentSession().createSQLQuery(
                 "select resource.id as id, resource.title as title, count(newsitems.id) as number " + 
                 "from resource, resource newsitems where newsitems.publisher = resource.id and newsitems.http_status =200 " + 
                 "group by newsitems.publisher order by resource.title").list();
         } else {
             return sessionFactory.getCurrentSession().createSQLQuery(
                     // TODO implement count for 0's then can probably merge with above.
                     "select resource.id as id, resource.title as title, 0 as number " +
                     "from resource where type='W' and http_status = 200 order by resource.title").list();
         }
     }
     
  
     @SuppressWarnings("unchecked")
     public List<Resource> getAllPublishersMatchingStem(String stem, boolean showBroken) {
         List<Resource> allPublishers = new ArrayList<Resource>();
         if (showBroken) {
             allPublishers = sessionFactory.getCurrentSession().createCriteria(Website.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list();
         } else { 
             allPublishers = sessionFactory.getCurrentSession().createCriteria(Website.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).add(Expression.eq("httpStatus", 200)).addOrder(Order.asc("name")).list();            
         }               
         return allPublishers;
     }
     
     
    
     @SuppressWarnings("unchecked")
     public List<Resource> getNotCheckedSince(Date oneMonthAgo, int maxItems) {     
         return sessionFactory.getCurrentSession().createCriteria(Resource.class).
         add(Restrictions.lt("lastScanned", oneMonthAgo)).addOrder(Order.asc("lastScanned")).
         setMaxResults(maxItems).list();       
     }
     
         
     public List<CommentFeed> getCurrentCommentFeeds(int maxItems) {
         // TODO Auto-generated method stub
         //select date, comment_feed.url, last_scanned from resource, comment_feed where comment_feed.id = resource.comment_feed and type ='N' and comment_feed is not null order by date asc;
         return null;
     }
  
     
 
     @SuppressWarnings("unchecked")
     public List<CommentFeed> getCommentFeedsToCheck(int maxItems) {
         return sessionFactory.getCurrentSession().createCriteria(CommentFeed.class).
         addOrder(Order.desc("lastRead")).
         setCacheable(false).
         setMaxResults(maxItems).
         list();
     }
 
 
     public List<Resource> getNewsitemsMatchingKeywords(String keywords, boolean showBroken) throws IOException, ParseException {
         throw(new UnsupportedOperationException());
     }
 
     public List<Resource> getWebsitesMatchingKeywords(String keywords, boolean showBroken) throws IOException, ParseException {
         throw(new UnsupportedOperationException());
     }
 
     
     @SuppressWarnings("unchecked")
     // TODO depricate this; where is it used? Used by luceneindexbuilder
     final public List<Resource> getAllNewsitems() {
         return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).list();        
     }
     
     @SuppressWarnings("unchecked")
     public List<Resource> getAllValidGeocoded(int maxItems, boolean showBroken) {      
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Resource.class).
         createAlias("geocode", "geocode").
         add(Restrictions.isNotNull("geocode")).
         add(Restrictions.ne("geocode.latitude", new Double(0))).
         add(Restrictions.ne("geocode.longitude", new Double(0))).
         addOrder(Order.desc("date")).
         addOrder(Order.desc("id")).
         setMaxResults(maxItems);     
         if (!showBroken) {
             criteria.add(Expression.eq("httpStatus", 200));
         }
         return criteria.list();
     }
     
   
     @SuppressWarnings("unchecked")
     public List<Resource> getAllResources() {
         return sessionFactory.getCurrentSession().createCriteria(Resource.class).list();   
     }
 
     @SuppressWarnings("unchecked")
     public List<Tag> getAllTags() {        
         return sessionFactory.getCurrentSession().createCriteria(Tag.class).
         addOrder(Order.asc("name")).
         setCacheable(true).
         list();        
     }
     
     
     @Deprecated
     // Doesn't recurse.
     public List<Tag> getCommentedTags(boolean showBroken) throws IOException {
         // TODO performance! Don't iterate!
         List<Tag> commentedTags = new ArrayList<Tag>();
         for(Tag tag : getAllTags()) {
             final int commentedCount = getCommentedNewsitemsForTag(tag, showBroken, 500).size();
             log.debug("Tag " + tag.getDisplayName() + " has " + commentedCount + " commented newsitems.");
             if (commentedCount > 0) {
                 commentedTags.add(tag);
             }
         }
         return commentedTags;
         
     }
     
     
     public List<Tag> getGeotaggedTags(boolean showBroken) throws IOException {
         // TODO performance! Don't iterate!
         List<Tag> geotaggedTags = new ArrayList<Tag>();
         for(Tag tag : getAllTags()) {
             final int geotaggedCount = getAllValidGeocodedForTag(tag, 5, showBroken).size();
             log.debug("Tag " + tag.getDisplayName() + " has " + geotaggedCount + " geotagged newsitems.");
             if (geotaggedCount > 0) {
                 geotaggedTags.add(tag);
             }
         }
         return geotaggedTags;        
     }
     
     
     
     @SuppressWarnings("unchecked")
     public List<Tag> getTopLevelTags() {
         return sessionFactory.getCurrentSession().createCriteria(Tag.class).
         add(Restrictions.isNull("parent")).
         addOrder(Order.asc("name")).
         setCacheable(true).
         list();
     }
 
 
     @SuppressWarnings("unchecked")
     public List<Resource> getAllWatchlists() {        
         return sessionFactory.getCurrentSession().createCriteria(Watchlist.class).
         addOrder(Order.desc("lastChanged")).
         setCacheable(true).
         list();  
     }
     
     
    
     
     
     
     @SuppressWarnings("unchecked")
     public List<Resource> getRecentlyChangedWatchlistItems() {       
         return sessionFactory.getCurrentSession().createCriteria(Watchlist.class)
             .add(Restrictions.sqlRestriction(" last_changed > DATE_SUB(now(), INTERVAL 7 DAY) "))
             .addOrder(Order.desc("lastChanged"))
             .setCacheable(true).
             list();        
     }
 
 
     @SuppressWarnings("unchecked")
     public List<Resource> getTagWatchlist(Tag tag, boolean showBroken) throws IOException {
         Criteria criteria =  sessionFactory.getCurrentSession().createCriteria(Watchlist.class).
             addOrder(Order.asc("name"));            
         if (!showBroken) {
             criteria.add(Expression.eq("httpStatus", 200));
         }            
         return criteria.createCriteria("tags").add(Restrictions.eq("id", tag.getId())).
         setCacheable(true).
         list();  
     }
     
     
     
    
     
 
 	@SuppressWarnings("unchecked")
 	public List<ArchiveLink> getArchiveMonths() {
         // TODO migrate this to map onto an object; then you might be able to cache it.
         List<ArchiveLink> archiveMonths = new ArrayList<ArchiveLink>();        
         // TODO implement show broken option.
         List<Object[]> rows = sessionFactory.getCurrentSession().createSQLQuery(
                 "select date_format(resource.date, '%Y-%m') as month, count(id) " +
                 "       from resource where http_status = 200 and type='N' " +
                 "       group by month " +
                 "       order by month desc"
                 ).             
                 list();
         
         for (Object[] objects : rows) {
             final BigInteger count = (BigInteger) objects[1];           
             SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM");
             try {
                 Date month = sdfInput.parse((String) objects[0]);
                 archiveMonths.add(new ArchiveLink(month, count.intValue()));                
             } catch (java.text.ParseException e) {
                 // TODO can hibernate do this date casting for us?
             }                
         }
         return archiveMonths;
     }
 
 
     
     @SuppressWarnings("deprecation")
 	public int getWebsiteCount(boolean showBroken) {       
         if (showBroken) {
             return ((Long) sessionFactory.getCurrentSession().
                 iterate("select count(*) from WebsiteImpl as website").
                 next()).intValue();
         } else {
             return ((Long) sessionFactory.getCurrentSession().
                     iterate("select count(*) from WebsiteImpl as website where website.httpStatus = 200").
                     next()).intValue();
         }
     }
 
     // TODO map onto a cachable object.
     @SuppressWarnings("deprecation")
 	public int getNewsitemCount(boolean showBroken) {
         if (showBroken) {
             return ((Long) sessionFactory.getCurrentSession().
                 iterate("select count(*) from NewsitemImpl as newsitem").
                 next()).intValue();
         } else {
             return ((Long) sessionFactory.getCurrentSession().
                     iterate("select count(*) from NewsitemImpl as newsitem where newsitem.httpStatus = 200").
                     next()).intValue();
         }
     }
        
     
     @SuppressWarnings("deprecation")
 	public int getCommentCount() {
         // TODO implement show broken logic.
         return ((Long) sessionFactory.getCurrentSession().
                 iterate("select count(*) from CommentImpl").
                 next()).intValue();
     }
     
 
     
     
 
     public boolean isResourceWithUrl(String url) {
         Resource existingResource = loadResourceByUrl(url);               
         return existingResource != null;
     }
 
 
     public Resource loadResourceById(int resourceID) {
    	return (Resource) sessionFactory.getCurrentSession().load(ResourceImpl.class, resourceID);        
     }
 
     
     public Resource loadResourceByUrl(String url) {
         return (Resource) sessionFactory.getCurrentSession().createCriteria(Resource.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();        
     }
     
     
     
     public CommentFeed loadCommentFeedByUrl(String url) {
         return (CommentFeed) sessionFactory.getCurrentSession().createCriteria(CommentFeed.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();  
     }
     
     
     
     public DiscoveredFeed loadDiscoveredFeedByUrl(String url) {
         return (DiscoveredFeed) sessionFactory.getCurrentSession().createCriteria(DiscoveredFeed.class).
         add(Expression.eq("url", url)).
         setMaxResults(1).
         setCacheable(true).
         uniqueResult();  
     }
 
 
     public Tag loadTagById(int tagID) {
         return (Tag) sessionFactory.getCurrentSession().load(Tag.class, tagID);
     }
     
     
     
     
     public Tag loadTagByName(String tagName) {
         return (Tag) sessionFactory.getCurrentSession().
         createCriteria(Tag.class).
         add(Restrictions.eq("name", tagName)).
         uniqueResult();    
     }
     
     
     public void saveResource(Resource resource) {     
         sessionFactory.getCurrentSession().saveOrUpdate(resource);
         sessionFactory.getCurrentSession().flush();
         //if (resource.getType().equals("F")) {
             // TODO can this be done for just the publisher only?
          //   sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds");
        // }
      
         // TODO for related tags, can we be abit more subtle than this?
         // Clear related tags query.
        // sessionFactory.evictQueries();
     }
 
     
     
     public void saveDiscoveredFeed(DiscoveredFeed discoveredFeed) {
         sessionFactory.getCurrentSession().saveOrUpdate(discoveredFeed);
         sessionFactory.getCurrentSession().flush();
     }
     
     
     public void saveCommentFeed(CommentFeed commentFeed) {
         sessionFactory.getCurrentSession().saveOrUpdate(commentFeed);
         sessionFactory.getCurrentSession().flush();
     }
 
 
 
     public void saveTag(Tag editTag) {
         sessionFactory.getCurrentSession().saveOrUpdate(editTag);
         sessionFactory.getCurrentSession().flush();
         //sessionFactory.evictCollection("nz.co.searchwellington.model.TagImpl.children");
     }
 
     
     @SuppressWarnings("unchecked")
     public List<Newsitem> getLatestNewsitems(int number, boolean showBroken) throws IOException {        
         return criteriaForLatestNewsitems(number, showBroken).setCacheable(true).list();      
     }
 
     @SuppressWarnings("unchecked")
     public List<Newsitem> getLatestTwitteredNewsitems(int number, boolean showBroken) throws IOException {        
         return criteriaForLatestNewsitems(number, showBroken).
         add(Expression.isNotNull("twitterSubmitter")).
         setCacheable(true).list();      
     }
     
     private Criteria criteriaForLatestNewsitems(int number, boolean showBroken) {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
             addOrder(Order.desc("date")).
             addOrder(Order.desc("id")).
             setMaxResults(number);
         if (!showBroken) {
             criteria.add(Expression.eq("httpStatus", 200));
         }
         return criteria;
     }
     
     
     @SuppressWarnings("unchecked")
     public List<Website> getLatestWebsites(int maxNumberOfItems, boolean showBroken) throws IOException {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Website.class).addOrder( Order.desc("date"));
         if (!showBroken) {
             criteria.add(Expression.eq("httpStatus", 200));
         }
         return criteria.setCacheable(true).
         setMaxResults(maxNumberOfItems).
         list();
     }
 
    
     
     
     
     @SuppressWarnings("unchecked")
     public List<Newsitem> getPublisherNewsitems(Website publisher, int MaxNumberOfItems, boolean showBroken) {        
         Criteria criteria = makePublisherNewsitemsCriteria(publisher, showBroken);
         return criteria.setMaxResults(MaxNumberOfItems).
         setCacheable(true).
         list();
     }
     
     @SuppressWarnings("unchecked")
     public List<Newsitem> getAllPublisherNewsitems(Website publisher, boolean showBroken) {
         return makePublisherNewsitemsCriteria(publisher, showBroken).
         setCacheable(true).
         list();
     }
 
     
     private Criteria makePublisherNewsitemsCriteria(Website publisher, boolean showBroken) {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
             add(Expression.eq("publisher", publisher)).        
             addOrder(Order.desc("date")).
             setCacheable(true);
         
         if (!showBroken) {
             criteria.add(Expression.eq("httpStatus", 200));               
         }
         return criteria;
     }
 
     
     
     @SuppressWarnings("unchecked")
     public List<Resource> getTaggedResources(Tag tag, int max_newsitems) {
         return sessionFactory.getCurrentSession().createCriteria(Resource.class).createCriteria("tags").add(Restrictions.eq("id", tag.getId())).list();
     }
     
    
         
     @SuppressWarnings("unchecked")
     public List<Website> getTaggedWebsites(Tag tag, boolean showBroken, int max_newsitems) throws IOException {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Website.class);        
         if (!showBroken) {
         criteria.add(Expression.eq("httpStatus", 200)); 
         }
         return criteria.addOrder( Order.asc("name")).
             createCriteria("tags").add(Restrictions.eq("id", tag.getId())). 
             setCacheable(true).
             list();        
     }
     
     
      
     @SuppressWarnings("unchecked")  
     public List<Website> getTaggedWebsites(Set<Tag> tags, boolean showBroken, int max_websites) throws IOException {
         if (tags.size() == 2) {            
             Iterator<Tag> i = tags.iterator();
             Tag tag1 = i.next();
             Tag tag2 = i.next();
          
             String showBrokenClause = "";
             if (!showBroken) {
                 showBrokenClause = " and http_status = 200 ";
             }
             
             List<Website> rows = sessionFactory.getCurrentSession().createSQLQuery(
                     "  select {resource.*} from resource {resource}, " + 
                     "           resource_tags as first_tag, resource_tags as second_tag where first_tag.tag_id = ? " +
                     "           and first_tag.resource_id = resource.id and second_tag.tag_id = ? " +
                     "           and second_tag.resource_id = resource.id" +
                     "           and type='W' " + showBrokenClause, "resource", WebsiteImpl.class).
                     setInteger(0, tag1.getId()).
                     setInteger(1, tag2.getId()).
                     setCacheable(true).                    
                     list();                 
             return rows;    
         }        
         throw new UnsupportedOperationException();
     }
 
 
     
     @SuppressWarnings("unchecked")
     public List<Newsitem> getNewsitemsForMonth(Date date) {
         // TODO deprication
         int year = date.getYear() + 1900;
         int month = date.getMonth();
 
         Calendar startDate = Calendar.getInstance();
         startDate.set(year, month, 1, 0, 0, 0);
 
         Calendar endDate = Calendar.getInstance();
         endDate.set(year, month + 1, 1, 0, 0, 0);
 
         return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
             add(Restrictions.ge("date", startDate.getTime())).
             add(Restrictions.lt("date", endDate.getTime())).
             addOrder(Order.desc("date")).
             addOrder(Order.desc("id")).
             setCacheable(true).
             list();
     }
 
 
         
     public void deleteResource(Resource resource) throws IOException {
         sessionFactory.getCurrentSession().delete(resource);       
         // flush collection caches.  
         sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.newsitems");
         sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.watchlist");
         sessionFactory.evictCollection("nz.co.searchwellington.model.DiscoveredFeedImpl.references");
     }
 
 
     public void deleteTag(Tag tag) throws IOException {
         sessionFactory.getCurrentSession().delete(tag);
         sessionFactory.getCurrentSession().flush();
     }
 
 
     public List<Tag> getTagsMatchingKeywords(String keywords) throws IOException, ParseException {
         throw(new UnsupportedOperationException());
     }
 
 
     @SuppressWarnings("unchecked")
     public List<Newsitem> getAllCommentedNewsitems(int maxItems, boolean hasComments) {        
         Criteria commentedNewsitems = sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
                 add(Restrictions.isNotNull("commentFeed")).
                 addOrder( Order.desc("date")).              
                 setMaxResults(maxItems).
                 setCacheable(true);
        
         if (hasComments) {
             commentedNewsitems.createCriteria("commentFeed").add(Restrictions.isNotEmpty("comments"));
         }        
         return commentedNewsitems.list();              
     }
 
     
        
     @SuppressWarnings("unchecked")
     public Date getNewslogLastChanged() {        
         Criteria latestNewsitemsCriteria = criteriaForLatestNewsitems(20, false);
         latestNewsitemsCriteria.addOrder( Order.desc("liveTime"));
         List<Resource> currentNewsitems = latestNewsitemsCriteria.setCacheable(true).list();        
         DateTime latestChange = null;
         for (Resource resource : currentNewsitems) {           
             if (latestChange == null) {
                 latestChange = new DateTime(resource.getLastChanged());
                 log.debug("Setting last changed to: " + latestChange);
             }
             if (resource.getLastChanged() != null && new DateTime(resource.getLastChanged()).isAfter(latestChange)) {
                 latestChange = new DateTime(resource.getLastChanged());                
             }
         }
         return latestChange.toDate();
     }
 
 
     
     public List<Resource> getCommentedNewsitemsForTag(Tag tag, boolean showBroken, int maxItems) throws IOException {
         throw(new UnsupportedOperationException());  
     }
     
     
     
     
      
     @SuppressWarnings("unchecked")
 	public List<Resource> getResourcesWithTag(Tag tag) {
     	log.info(tag.getName());
     	Criteria taggedResources = sessionFactory.getCurrentSession().createCriteria(Resource.class).
     		addOrder(Order.desc("date")).
     		addOrder(Order.desc("id")).
     		createAlias("tags", "rt").
     		add(Restrictions.eq("rt.id", tag.getId()));
     	
     	return taggedResources.list();
 	}
 
 
 	@SuppressWarnings("unchecked")
     public List<Tag> getRelatedLinksForTag(Tag tag, boolean showBroken) {
 		// TODO 3 operator
 		String showBrokenClause = "";
         if (!showBroken) {
             showBrokenClause = " and http_status = 200 ";
         }
 		
 		List<Tag> relatedTags = sessionFactory.getCurrentSession().createSQLQuery(
                   "  select {tag.*} from tag {tag} where id IN (" +
                   "			select distinct(trts.tag_id) from resource_tags AS rt, resource_tags AS trts, resource " +
                   "					where rt.tag_id = ? " +
                   "					AND trts.resource_id = rt.resource_id " +
                   "					AND resource.id = trts.resource_id " +
                   "					and trts.tag_id != ? " + showBrokenClause + " )",
                   "tag", Tag.class).
                   setInteger(0, tag.getId()).
                   setInteger(1, tag.getId()).
                   setCacheable(true).                    
                   list();
           return relatedTags;
     }
 	
 }
