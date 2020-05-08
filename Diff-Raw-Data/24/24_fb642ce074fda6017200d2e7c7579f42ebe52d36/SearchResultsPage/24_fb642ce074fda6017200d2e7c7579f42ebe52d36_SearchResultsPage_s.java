 package pl.psnc.dl.wf4ever.portal.pages.search;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.navigation.paging.IPageable;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.purl.wf4ever.rosrs.client.exception.SearchException;
 import org.purl.wf4ever.rosrs.client.search.SearchServer;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.FacetValue;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.FoundRO;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.SearchResult;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.solr.FacetEntry;
 
 import pl.psnc.dl.wf4ever.portal.PortalApplication;
 import pl.psnc.dl.wf4ever.portal.pages.base.Base;
 import pl.psnc.dl.wf4ever.portal.pages.base.components.BootstrapPagingNavigator;
 import pl.psnc.dl.wf4ever.portal.pages.ro.RoPage;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.behaviours.IAjaxLinkListener;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyFeedbackPanel;
 
 /**
  * The home page.
  * 
  * @author piotrekhol
  * 
  */
 public class SearchResultsPage extends Base implements IAjaxLinkListener {
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(SearchResultsPage.class);
 
    public static final int RESULTS_PER_PAGE = 10;
 
     private List<FacetEntry> facetsList = null;
     private List<FoundRO> ROsList = null;
     private String keywords = null;
     private List<FacetValue> selected = null;
     private String originalKeywords = null;
 
 
     public SearchResultsPage() {
        this("");
     }
 
 
    public SearchResultsPage(final String searchKeywords) {
        this(searchKeywords, null, null);
 
     }
 
 
     /**
      * Constructor.
      * 
      * @param parameters
      *            page params
      * @throws IOException
      *             can't connect to RODL
      */
     public SearchResultsPage(final String searchKeywords, List<FacetValue> selected, String originalKeywords) {
         super(new PageParameters());
         if (selected == null) {
             this.selected = new ArrayList<>();
         } else {
             this.selected = selected;
         }
         if (originalKeywords == null) {
             this.originalKeywords = searchKeywords;
         } else {
             this.originalKeywords = originalKeywords;
         }
         keywords = searchKeywords;
         setDefaultModel(new CompoundPropertyModel<SearchResultsPage>(this));
 
         SearchServer searchServer = ((PortalApplication) getApplication()).getSearchServer();
 
         final MyFeedbackPanel feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         add(feedbackPanel);
 
         final WebMarkupContainer searchResultsDiv = new WebMarkupContainer("searchResultsDiv");
         searchResultsDiv.setOutputMarkupId(true);
         add(searchResultsDiv);
 
        searchResultsDiv.add(new Label("searchKeywords", originalKeywords));
 
         IPageable searchResultsList = null;
         /*
         if (searchServer.supportsPagination()) {
             searchResultsList = new LazySearchResultsView("searchResultsListView", searchServer, searchKeywords,
                     RESULTS_PER_PAGE);
         } else {
             */
         SearchResult searchResult = null;
         try {
             searchResult = searchServer.search(searchKeywords);
 
         } catch (SearchException e) {
             error(e.getMessage());
             LOGGER.error("Can't do the search for " + searchKeywords, e);
         }
         facetsList = searchResult.getFactesList();
         ROsList = searchResult.getROsList();
         searchResultsList = new SimpleSearchResultsView("searchResultsListView", ROsList, RESULTS_PER_PAGE);
         //}
         searchResultsDiv.add((Component) searchResultsList);
 
         //TODO to something as below
         //        FacetsView facetsView = new FacetsView("filters", new PropertyModel<List<Facet>>(searchResults, "facets"));
        FacetsView facetsView = new FacetsView("filters", this.selected, new PropertyModel<List<FacetEntry>>(this, "facets"));
         facetsView.getListeners().add(this);
         add(facetsView);
 
         final WebMarkupContainer noResults = new WebMarkupContainer("noResults");
         searchResultsDiv.add(noResults);
         //        noResults.setVisible(searchResults == null || searchResults.isEmpty());
         noResults.setVisible(false);
 
         add(new BootstrapPagingNavigator("pagination", searchResultsList));
     }
 
 
     public List<FacetEntry> getFacets() {
         return facetsList;
     }
 
 
     public static void populateItem(ListItem<FoundRO> item) {
         final FoundRO result = item.getModelObject();
         BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", RoPage.class);
         link.getPageParameters().add("ro", result.getResearchObject().getUri().toString());
         link.add(new Label("researchObject.name", new PropertyModel<String>(result, "researchObject.name")));
         item.add(link);
         item.add(new Label("researchObject.title", new PropertyModel<String>(result, "researchObject.title")));
         //            item.add(new CreatorsPanel("researchObject.creator", new PropertyModel<List<Creator>>(result,
         //                    "researchObject.creators")));
         item.add(new Label("researchObject.createdFormatted", new PropertyModel<String>(result,
                 "researchObject.createdFormatted")));
         WebMarkupContainer score = new WebMarkupContainer("searchScore");
         item.add(score);
         score.setVisible(result.getScore() >= 0);
         score.add(new Label("scoreInPercent", new PropertyModel<String>(result, "scoreInPercent")));
         Label bar = new Label("percentBar", "");
         bar.add(new Behavior() {
 
             /** id. */
             private static final long serialVersionUID = -5409800651205755103L;
 
 
             @Override
             public void onComponentTag(final Component component, final ComponentTag tag) {
                 super.onComponentTag(component, tag);
                 tag.put("style", "width: " + Math.min(100, result.getScoreInPercent()) + "%");
             }
         });
         score.add(bar);
     }
 
 
     @Override
     public void onAjaxLinkClicked(Object source, AjaxRequestTarget target) {
         for (FacetValue val : selected) {
             if (val.getLabel().equals(((FacetValue) (source)).getLabel())
                     && val.getParamName().equals(((FacetValue) (source)).getParamName())) {
                 selected.remove(val);
                 keywords = originalKeywords;
                 for (FacetValue value : selected) {
                     keywords += " AND " + value.getQuery();
                 }
                 setResponsePage(new SearchResultsPage(keywords, selected, originalKeywords));
                 return;
             }
         }
         selected.add((FacetValue) (source));
         keywords = originalKeywords;
         for (FacetValue value : selected) {
             keywords += " AND " + value.getQuery();
         }
         setResponsePage(new SearchResultsPage(keywords, selected, originalKeywords));
     }
 }
