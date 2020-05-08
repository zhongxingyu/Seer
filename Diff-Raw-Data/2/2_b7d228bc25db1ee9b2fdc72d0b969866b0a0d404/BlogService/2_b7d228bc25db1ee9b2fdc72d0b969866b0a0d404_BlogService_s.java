 package com.madalla.service.blog;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.jcr.RepositoryException;
 import javax.swing.tree.TreeModel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.madalla.dao.blog.BlogDao;
 import com.madalla.service.cms.IContentService;
 import com.madalla.util.ui.CalendarUtils;
 import com.madalla.webapp.cms.Content;
 
 public class BlogService implements IBlogService, Serializable{
     
 	private static final long serialVersionUID = 1L;
 	private static final String BLOG_PREFIX = "blog";
 	//TODO refactor to allow multiple blogs per site
 	private static final String BLOG_NAME = "mainBlog";
 	private BlogDao dao;
 	private IContentService contentService;
 	private Log log = LogFactory.getLog(this.getClass());
 
     public List getBlogCategories(){
         return dao.getBlogCategories();
     }
 
     public int saveBlogEntry(BlogEntry blogEntry) {
     	int blogId;
         if (blogEntry.getId() == 0){
         	log.debug("saveBlogEntry - inserting "+blogEntry);
         	blogId = dao.insertBlogEntry(blogEntry);
         } else {
         	log.debug("saveBlogEntry - updating "+blogEntry);
         	dao.saveBlogEntry(blogEntry);
         	blogId = blogEntry.getId();
         }
         //save content to CMS
         Content content = new Content();
        content.setClassName(dao.getSite());
         content.setContentId(getBlogId(blogId));
         content.setText(blogEntry.getText());
         try {
         	log.debug("saveBlogEntry - saving Content "+content);
 			contentService.setContent(content);
 		} catch (RepositoryException e) {
 			log.error("saveBlogEntry - ", e);
 		}
         
         return blogId;
     }
     
     public BlogEntry getBlogEntry(int id) {
         BlogEntry blogEntry = dao.getBlogEntry(id);
         populateContentFromCMS(blogEntry);
         return blogEntry;
     }
     
     public List getBlogEntriesForCategory(int categoryId) {
         List list = dao.getBlogEntriesForCategory(categoryId);
         return getBlogEntries(list);
     }
     
     public List getBlogEntries() {
     	//get Metadata from database
         List list = dao.getBlogEntriesForSite();
         return getBlogEntries(list);
     }
 
     public TreeModel getBlogEntriesAsTree(){
     	List list = getBlogEntries();
     	return CalendarUtils.createMonthlyTree("Blog Archive", list);
     }
     
     public void deleteBlogEntry(int id){
         dao.deleteBlogEntry(id);
     }
     
     private void populateContentFromCMS(BlogEntry blogEntry){
     	log.debug("populateContent - before Content :"+blogEntry);
 		String id = getBlogId(blogEntry.getId());
 		
 		log.debug("populateContent - retrieve content from CMS for node="+BLOG_NAME+" id="+id);
 		String content = contentService.getContentData(BLOG_NAME, id);
 		log.debug("populateContent - content="+content);
 		blogEntry.setText(content);
     }
     
     private String getBlogId(int id){
     	return BLOG_PREFIX + Integer.toString(id);
     }
 
     /**
      * Content from CMS repository and sorts it
      * @param list
      * @return
      */
     private List getBlogEntries(List list){
         for (Iterator iter = list.iterator(); iter.hasNext();) {
 			BlogEntry blogEntry = (BlogEntry) iter.next();
 			populateContentFromCMS(blogEntry);
 		}
         Collections.sort(list);
         return list;
     }
 
     public void setDao(BlogDao dao) {
         this.dao = dao;
     }
 	public void setContentService(IContentService contentService) {
 		this.contentService = contentService;
 	}
 
         
 
 }
