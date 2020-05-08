 package org.onehippo.forge.weblogdemo.components.overview;
 
 import org.apache.commons.lang.StringUtils;
 import org.hippoecm.hst.content.beans.query.HstQuery;
 import org.hippoecm.hst.content.beans.query.HstQueryResult;
 import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
 import org.hippoecm.hst.content.beans.standard.HippoBean;
 import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
 import org.hippoecm.hst.content.beans.standard.HippoFolderBean;
 import org.hippoecm.hst.core.component.HstComponentException;
 import org.hippoecm.hst.core.component.HstRequest;
 import org.hippoecm.hst.core.component.HstResponse;
 import org.onehippo.forge.weblogdemo.beans.BaseDocument;
 import org.onehippo.forge.weblogdemo.beans.BeanConstants;
 import org.onehippo.forge.weblogdemo.components.BaseSiteComponent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Abstract component for an overview of documents in a folder.
  * Queries a folder and puts a paged resultset on the request.
  * Implementing classes should define which document type is used as filter.
  *
  * @author Jasha Joachimsthal
  */
 public abstract class AbstractListing extends BaseSiteComponent {
     public static final int PAGESIZE = 10;
     public static final Logger log = LoggerFactory.getLogger(AbstractListing.class);
 
     @Override
     public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {
        super.doBeforeRender(request,
                response);
         HippoBean contentBean = getContentBean(request);
         if (!(contentBean instanceof HippoFolderBean)) {
             log.warn("Parameter hst:relativecontentpath does not reference a folder");
             response.setStatus(HstResponse.SC_NOT_FOUND);
             return;
         }
 
         int page = getPageNumber(request);
         request.setAttribute("page", page);
 
        setPageTitle(request);
 
         request.setAttribute("documents", getDocumentsForOverview(request, page));
 
     }
 
     /**
      * Gets a {@link List} of {@link org.hippoecm.hst.content.beans.standard.HippoBean} documents.
      * Can call {@link #getDocumentList(org.hippoecm.hst.core.component.HstRequest, int, Class)} with the specific class to get a List of
      * documents with a specific class under a folder.
      *
      * @param request current {@link org.hippoecm.hst.core.component.HstRequest}
      * @param page    page number within the listing
      * @return {@link java.util.List} of {@link org.hippoecm.hst.content.beans.standard.HippoBean} documents
      */
     protected abstract List<HippoBean> getDocumentsForOverview(HstRequest request, int page);
 
     protected void setPageTitle(HstRequest request) {
        request.setAttribute("pageTitle", getParameter("pageTitle", request));
     }
 
     /**
      * Gets the current page number for the listing as int with offset 0.
      *
      * @param request current {@link org.hippoecm.hst.core.component.HstRequest}
      * @return int current page number withing a list, offset 0
      */
     private int getPageNumber(HstRequest request) {
         String pageStr = request.getParameter("page");
 
         int page = 0;
         if (StringUtils.isNotBlank(pageStr)) {
             try {
                 page = Integer.parseInt(pageStr);
             } catch (NumberFormatException e) {
                 log.debug("Value '{}' cannot be converted to an Integer", pageStr);
             }
         }
         return page;
     }
 
 
     private HippoBeanIterator getQueryResultAsIterator(HippoBean contentBean, Class<? extends BaseDocument> beanClass) {
         try {
             HstQuery hstQuery = getQueryManager().createQuery(contentBean, beanClass);
             hstQuery.addOrderByDescending(BeanConstants.PROP_DATE);
             HstQueryResult queryResult = hstQuery.execute();
             return queryResult.getHippoBeans();
         } catch (QueryException e) {
             log.warn("Error in querying blogposts", e);
         }
         return null;
     }
 
 
     /**
      * Iterates over query result for the requested "page" and returns a List of documents.
      *
      * @param request   {@link org.hippoecm.hst.core.component.HstRequest}   
      * @param page      current page number
      * @param beanClass the Class of documents that is used as query filter
      * @return {@link java.util.List} of documents or empty List if no documents are found
      */
     protected List<HippoBean> getDocumentList(HstRequest request, int page,
                                               Class<? extends BaseDocument> beanClass) {
         List<HippoBean> documents = new ArrayList<HippoBean>();
 
         HippoBeanIterator beans = getQueryResultAsIterator(getContentBean(request), beanClass);
         if (beans == null) {
             return documents;
         }
 
         long beansSize = beans.getSize();
         // calculates the total number of pages
         long pages = beans.getSize() % PAGESIZE > 0L ? beansSize / PAGESIZE + 1L : beansSize / PAGESIZE;
 
         request.setAttribute("pages", pages);
         if (beansSize > page * ((long) PAGESIZE)) {
             beans.skip(page * PAGESIZE);
         }
 
         int results = 0;
         while (beans.hasNext() && results < PAGESIZE) {
             HippoBean bean = beans.next();
             if (bean != null) { // document may have been deleted after searching
                 documents.add(bean);
                 results++;
             }
         }
         return documents;
     }
 
 }
