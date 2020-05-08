 package com.schlock.website.services.database.blog.impl;
 
 import com.schlock.website.entities.blog.Post;
 import com.schlock.website.services.database.BaseDAOImpl;
 import com.schlock.website.services.database.blog.PostDAO;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import java.util.*;
 
 public class PostDAOImpl extends BaseDAOImpl<Post> implements PostDAO
 {
     public static final int TOP_RECENT = 5;
 
     public PostDAOImpl(Session session)
     {
         super(Post.class, session);
     }
 
     public Post getByUuid(String uuid)
     {
         String text = "from Post p " +
                         " where p.uuid = :uuid ";
 
         Query query = session.createQuery(text);
         query.setParameter("uuid", uuid);
 
         List list = query.list();
         if (list.size() > 1)
         {
             //log problem
         }
 
         if (list.isEmpty())
         {
             return null;
         }
         return (Post) list.get(0);
     }
 
     public Post getByWpid(String wpid)
     {
         String text = "from Post p where p.wpid = :wpid";
         Query query = session.createQuery(text);
         query.setParameter("wpid", wpid);
 
         return (Post) singleResult(query);
     }
 
     public Post getByMtid(String mtid)
     {
         String text = "from Post p where p.mtid = :mtid";
         Query query = session.createQuery(text);
         query.setParameter("mtid", mtid);
 
         return (Post) singleResult(query);
     }
 
     public Set<String> getAllUuids()
     {
         String text = "select p.uuid " +
                         " from Post p ";
 
         Query query = session.createQuery(text);
 
         List<String> list = query.list();
 
         Set<String> uuids = new HashSet<String>();
         uuids.addAll(list);
 
         return uuids;
     }
 
     public Post getMostRecentPost(boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select p from Post p ";
         String orderByClause = " order by p.createdGMT desc";
 
         Query query = createQuery(TOP_RECENT,
                 null,
                 null,
                 withUnpublished,
                 categoryId,
                 false,
                 selectClause,
                 null,
                 orderByClause);
 
         return (Post) singleResult(query);
     }
 
     public List<Post> getNextPosts(Post currentPost, boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select p from Post p ";
         String extraWhereClause = " p.createdGMT > :currentCreated ";
         String orderByClause = " order by p.createdGMT asc";
 
         Query query = createQuery(TOP_RECENT,
                                     null,
                                     null,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     Arrays.asList(extraWhereClause),
                                     orderByClause);
 
         query.setTimestamp("currentCreated", currentPost.getCreatedGMT());
 
         return query.list();
     }
 
     public List<Post> getPreviousPosts(Post currentPost, boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select p from Post p ";
         String extraWhereClause = " p.createdGMT < :currentCreated ";
         String orderByClause = " order by p.createdGMT desc";
 
         Query query = createQuery(TOP_RECENT,
                                     null,
                                     null,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     Arrays.asList(extraWhereClause),
                                     orderByClause);
 
         query.setTimestamp("currentCreated", currentPost.getCreatedGMT());
 
         return query.list();
     }
 
 
 
     public List<Integer> getPostYears(boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select distinct(year(p.created)) from Post p ";
         String orderByClause = " order by year(p.created) desc";
 
         Query query = createQuery(null,
                                     null,
                                     null,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     null,
                                     orderByClause);
 
         return query.list();
     }
 
     public List<Integer> getPostMonths(int year, boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select distinct(month(p.created)) from Post p ";
         String orderByClause = " order by month(p.created) desc";
 
         Query query = createQuery(null,
                                     year,
                                     null,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     null,
                                     orderByClause);
 
         return query.list();
     }
 
     public List<Post> getPostsByYearMonth(int year, int month, boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select p from Post p ";
         String orderByClause = " order by month(p.created) desc ";
 
         Query query = createQuery(null,
                                     year,
                                     month,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     null,
                                     orderByClause);
         return query.list();
     }
 
     public List<Object[]> getRecentYearsMonths(Integer postCount, Integer year, Integer month, boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select year(p.created), month(p.created) from Post p ";
         String orderByClause = " order by year(p.created) desc, month(p.created) desc ";
 
         Query query = createQuery(postCount,
                                     year,
                                     month,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     null,
                                     orderByClause);
 
         return query.list();
     }
 
     public List<Post> getRecentPostsByYearMonth(Integer postCount, Integer year, Integer month, boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select p from Post p ";
         String orderByClause = " order by p.created desc ";
 
         Query query = createQuery(postCount,
                                     year,
                                     month,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     null,
                                     orderByClause);
 
         return query.list();
     }
 
     public List<Post> getRecentPinnedPostsByYearMonth(Integer postCount, Integer year, Integer month, boolean withUnpublished, Long categoryId)
     {
         String selectClause = "select p from Post p ";
         String extraWhereClause = " p.pinned is true ";
         String orderByClause = " order by p.created desc ";
 
         Query query = createQuery(postCount,
                                     year,
                                     month,
                                     withUnpublished,
                                     categoryId,
                                     false,
                                     selectClause,
                                     Arrays.asList(extraWhereClause),
                                     orderByClause);
 
         return query.list();
     }
 
     public List<Post> getAllPages(boolean withUnpublished)
     {
         String selectClause = "select p from Post p ";
         String orderByClause = " order by p.created desc ";
 
         Query query = createQuery(null, null, null,
                                     withUnpublished,
                                     null,
                                     true,
                                     selectClause,
                                     null,
                                     orderByClause);
         return query.list();
     }
 
     private Query createQuery(Integer postCount,
                               Integer year,
                               Integer month,
                               boolean withUnpublished,
                               Long categoryId,
 
                               boolean isPage,
 
                               String selectClause,
                               List<String> whereClauses,
                               String orderByClause)
     {
         List<String> phrases = new ArrayList<String>();
         if (categoryId != null)
         {
             phrases.add(" c.id = :categoryId ");
         }
         if (!withUnpublished)
         {
             phrases.add(" p.published is true ");
         }
         if (year != null)
         {
             phrases.add(" year(p.created) = :year ");
         }
         if (month != null)
         {
             phrases.add(" month(p.created) = :month ");
         }
         if (whereClauses != null && !whereClauses.isEmpty())
         {
             phrases.addAll(whereClauses);
         }
 
         if (isPage)
         {
             phrases.add(" p.page is true ");
         }
         else
         {
             phrases.add(" p.page is false ");
         }
 
         String text = selectClause;
        if (categoryId != null)
        {
            text += " join p.categories c ";
        }
         boolean first = true;
         for(String phrase : phrases)
         {
             if (first)
                 text += " where ";
             else
                 text += " and ";
 
             text += phrase;
 
             first = false;
         }
         text += orderByClause;
 
         Query query = session.createQuery(text);
         if (categoryId != null)
         {
             query.setParameter("categoryId", categoryId);
         }
         if (year != null)
         {
             query.setParameter("year", year);
         }
         if (month != null)
         {
             query.setParameter("month", month);
         }
         if (postCount != null)
         {
             query.setMaxResults(postCount);
         }
 
         return query;
     }
 }
