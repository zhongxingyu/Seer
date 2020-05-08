 package com.mrfeelings.db.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityResult;
 import javax.persistence.FieldResult;
 import javax.persistence.Id;
 import javax.persistence.NamedNativeQueries;
 import javax.persistence.NamedNativeQuery;
 import javax.persistence.SqlResultSetMapping;
 
 import com.mrfeelings.db.enums.Page;
 
 @Entity
 @SqlResultSetMapping(name="PageStatResults", entities={
   @EntityResult(entityClass=PageStat.class, fields={
     @FieldResult(name="page", column="page"),
     @FieldResult(name="numViews", column="numViews")
   })
 })
 @NamedNativeQueries(value = {
   @NamedNativeQuery(
     name=PageStat.GET_PAGE_STATS,
     resultSetMapping="PageStatResults",
     query="select page, count(page) as numViews from page_views group by page order by numViews desc"),
   @NamedNativeQuery(
     name=PageStat.GET_PAGE_STATS_BY_USERID,
     resultSetMapping="PageStatResults",
     query="select page, count(page) as numViews from page_views where user_id = :userId group by page order by numViews desc")
 })
 public class PageStat {
 
   public static final String GET_PAGE_STATS = "PageStat.getPageStats";
   public static final String GET_PAGE_STATS_BY_USERID = "PageStat.getPageStatsByUser";
 
   private Page _page;
   private Integer _numViews;
   
   private PageStat() { }
 
   @Id
   @Column
   public Page getPage() {
     return _page;
   }
   public void setPage(Page page) {
     _page = page;
   }
   
   @Column
   public Integer getNumViews() {
     return _numViews;
   }
   public void setNumViews(Integer numViews) {
     _numViews = numViews;
   }
 }
