 package sk.opendatanode.ui;
 
 import java.io.IOException;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sk.opendatanode.ui.search.SearchQueryPage;
 import sk.opendatanode.ui.search.SearchResultPage;
 import sk.opendatanode.utils.SolrQueryHelper;
 
 
 /**
  * Homepage
  */
 public class HomePage extends WebPage {
     private static final long serialVersionUID = -3362496726021637053L;
     private Logger logger = LoggerFactory.getLogger(HomePage.class);
     
     /**
 	 * Constructor that is invoked when page is invoked without a session.
 	 * 
 	 * @param params
 	 *            Page parameters
 	 */
    public HomePage(PageParameters parameters) {
 
         SearchQueryPage sp = new SearchQueryPage("searchPage", parameters);
         add(sp);
         
         QueryResponse response = null;
         try {
             response = SolrQueryHelper.search(parameters);
         } catch (IOException e) {
             logger.error("IOException error", e);
         } catch (SolrServerException e) {
             logger.error("SolrServerException",e);
         }
         
         SearchResultPage srp = new SearchResultPage("searchResultPage", parameters, response);
         add(srp);       
     }
 }
