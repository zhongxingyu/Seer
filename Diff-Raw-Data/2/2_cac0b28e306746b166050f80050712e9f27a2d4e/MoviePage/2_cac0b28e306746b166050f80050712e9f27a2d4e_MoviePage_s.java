 package no.koteng.paxTube;
 
 import com.google.api.services.youtube.model.ResourceId;
 import com.google.api.services.youtube.model.SearchResult;
 import com.google.api.services.youtube.model.Thumbnail;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.attributes.AjaxCallListener;
 import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
 import org.apache.wicket.ajax.attributes.IAjaxCallListener;
 import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 // AIzaSyDTBkXhZeazdO7pAwusHrefHlLcm0TZ0BY
 public class MoviePage extends WebPage implements Serializable {
     private List<YouTubeMovie> movieList = new ArrayList<YouTubeMovie>();
 
     public MoviePage(PageParameters parameters) {
         super(parameters);
 
 
         final WebMarkupContainer searchResultWrapper = new WebMarkupContainer("searchResultWrapper");
         searchResultWrapper.setOutputMarkupId(true);
 
         final ListView<YouTubeMovie> youTubeMovieListView = new ListView<YouTubeMovie>("youTubeMovieListView", movieList) {
 
             @Override
             protected void populateItem(ListItem<YouTubeMovie> item) {
                 final YouTubeMovie movie = item.getModelObject();
                 final WebMarkupContainer youTubeMovie = new WebMarkupContainer("youTubeMovie");
                 youTubeMovie.setOutputMarkupId(true);
                 youTubeMovie.add(new AjaxEventBehavior("onClick") {
 
                     @Override
                     protected void onEvent(AjaxRequestTarget target) {
                         String javaScriptInject = "document.getElementById('ytplayer').src = 'http://www.youtube.com/v/"
                                 + movie.getId()
                                + "?version=3&autoplay=1&vq=hd720&loop=1;'";
                         target.appendJavaScript(javaScriptInject);
                     }
                 });
 
                 youTubeMovie.add(new Label("title", movie.getTitle()));
                 youTubeMovie.add(new Image("thumbnail", movie.getThumbnail()));
 
                 item.add(youTubeMovie);
             }
         };
 
         youTubeMovieListView.setOutputMarkupId(true);
 
         final TextField<String> searchBox = new TextField<String>("searchBox", new Model<String>());
         searchBox.setOutputMarkupId(true);
 
         searchBox.add(new OnChangeAjaxBehavior() {
             @Override
             protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                 ((TextField<String>) getComponent()).getModelObject();
             }
         });
 
         searchBox.add(new AjaxEventBehavior("keydown") {
             @Override
             protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                 super.updateAjaxAttributes(attributes);
 
                 IAjaxCallListener listener = new AjaxCallListener() {
                     @Override
                     public CharSequence getPrecondition(Component component) {
                         //this javascript code evaluates wether an ajaxcall is necessary.
                         //Here only by keyocdes for enter (13)
                         return "var keycode = Wicket.Event.keyCode(attrs.event);" +
                                 "if (keycode == 13)" +
                                 "    return true;" +
                                 "else" +
                                 "    return false;";
                     }
                 };
                 attributes.getAjaxCallListeners().add(listener);
                 attributes.getDynamicExtraParameters()
                         .add("var eventKeycode = Wicket.Event.keyCode(attrs.event);" +
                                 "return {keycode: eventKeycode};");
 
                 //whithout setting, no keyboard events will reach any inputfield
                 attributes.setAllowDefault(true);
             }
 
             @Override
             protected void onEvent(AjaxRequestTarget target) {
                 final String queryTerm = searchBox.getDefaultModelObjectAsString();
                 searchForMovies(queryTerm);
                 youTubeMovieListView.setList(movieList);
                 target.add(searchResultWrapper);
             }
         });
 
         searchResultWrapper.add(youTubeMovieListView);
         add(searchBox);
         add(searchResultWrapper);
     }
 
     private void searchForMovies(String queryTerm) {
         final List<SearchResult> searchResults = Search.doSearch(queryTerm);
         movieList = new ArrayList<YouTubeMovie>();
 
         if (searchResults == null) {
             return;
         }
 
         for (SearchResult movie : searchResults) {
             ResourceId rId = movie.getId();
 
             if (rId.getKind().equals("youtube#video")) {
                 Thumbnail thumbnail = movie.getSnippet().getThumbnails().get("default");
                 movieList.add(new YouTubeMovie(rId.getVideoId(), movie.getSnippet().getTitle(), thumbnail.getUrl()));
             }
         }
     }
 }
