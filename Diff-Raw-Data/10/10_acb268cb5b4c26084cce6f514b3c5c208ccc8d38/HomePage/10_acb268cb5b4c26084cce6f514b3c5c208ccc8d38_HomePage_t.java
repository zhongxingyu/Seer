 package org.codefromhell.wicket;
 
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.CSSPackageResource;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.codefromhell.wicket.aggregator.twitter.TwitterAggregator;
 import twitter4j.Tweet;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The applications start page.
  *
  * @author Marco Grunert (magomi@gmail.com)
  *
  */
 public class HomePage extends WebPage {
 
 	private static final long serialVersionUID = 1L;
 
     private List<Tweet> tweets = new ArrayList<Tweet>();
 
     private String query;
 
     private WebMarkupContainer tweetsWmc;
 
     /**
      * Constructor.
      *
      * @param parameters Additional url parameters that has been provided to the page.
      */
     public HomePage(final PageParameters parameters) {
 
         // add the style definions
         this.add(CSSPackageResource.getHeaderContribution(new ResourceReference(HomePage.class, "960.css")));
 
         Form form = new Form("form", new CompoundPropertyModel<HomePage>(this));
 
         form.add(new TextField("query"));
 
         form.add(new AjaxButton("search") {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 TwitterAggregator ta = new TwitterAggregator();

                 tweets = ta.searchTweets(query);
 
                 target.addComponent(tweetsWmc);
             }
         });
 
         tweetsWmc = new WebMarkupContainer("tweetsWmc");
         RefreshingView<Tweet> tweetsRV = new RefreshingView<Tweet>("tweets") {
             @Override
             protected Iterator<IModel<Tweet>> getItemModels() {
                 return new ModelIteratorAdapter<Tweet>(tweets.iterator()) {
                     @Override
                     protected IModel<Tweet> model(Tweet object) {
                         return new CompoundPropertyModel<Tweet>(object);
                     }
                 };
             }
 
             @Override
             protected void populateItem(final Item<Tweet> tweetItem) {
                 tweetItem.add(new Label("fromUser"));
                 tweetItem.add(new Label("text"));
             }
         };
         tweetsWmc.add(tweetsRV);
         tweetsWmc.setOutputMarkupId(true);
         form.add(tweetsWmc);
 
 
         add(form);
     }
 
 }
