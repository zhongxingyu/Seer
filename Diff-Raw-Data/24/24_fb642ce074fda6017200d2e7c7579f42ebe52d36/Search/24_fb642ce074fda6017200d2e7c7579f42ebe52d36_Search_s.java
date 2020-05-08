 package pl.psnc.dl.wf4ever.portal.pages.base.components;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.search.utils.SolrQueryBuilder;
 
 import pl.psnc.dl.wf4ever.portal.pages.search.SearchResultsPage;
 
 public class Search extends Panel {
 
     /** id. */
     private static final long serialVersionUID = 6045458615921445179L;
 
     /** Logger. */
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(Search.class);
 
     /** the string that user inputs. */
     private String searchKeywords;
 
 
     public Search(String id) {
         super(id);
         Form<?> searchForm = new Form<Void>("searchFormBar") {
 
             /** id. */
             private static final long serialVersionUID = -9140218085617734421L;
 
 
             @Override
             protected void onSubmit() {
                 super.onSubmit();
                setResponsePage(new SearchResultsPage(SolrQueryBuilder.escapeString(searchKeywords)));
             }
         };
         searchForm.add(new Button("searchButtonBar"));
         RequiredTextField<String> searchFieldBar = new RequiredTextField<String>("keywords", new PropertyModel<String>(
                 this, "searchKeywords"));
 
         searchForm.add(searchFieldBar);
         add(searchForm);
     }
 
 
     public String getSearchKeywords() {
         return searchKeywords;
     }
 
 
     public void setSearchKeywords(String searchKeywords) {
         this.searchKeywords = searchKeywords;
     }
 
 }
