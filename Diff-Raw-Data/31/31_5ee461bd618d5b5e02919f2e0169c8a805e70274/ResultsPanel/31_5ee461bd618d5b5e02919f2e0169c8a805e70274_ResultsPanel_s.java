 /**
  * Copyright (C) 2010 Peter Karich <jetwick_@_pannous_._info>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.jetwick.ui;
 
 import com.google.api.translate.Language;
 import de.jetwick.ui.util.LabeledLink;
 import de.jetwick.solr.SolrTweet;
 import de.jetwick.solr.SolrTweetSearch;
 import de.jetwick.solr.SolrUser;
 import de.jetwick.util.Helper;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
 import org.apache.wicket.markup.html.WebResource;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.link.ResourceLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.protocol.http.WebResponse;
 import org.apache.wicket.util.resource.IResourceStream;
 import org.apache.wicket.util.resource.StringResourceStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
  */
 public class ResultsPanel extends Panel {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private ListView userView;
     private List<SolrUser> users = new ArrayList<SolrUser>();
     private String queryMessage;
     private String queryMessageWarn;
     private String query;
     private String user;
     private int tweetsPerUser;
     private String sort;
     private LabeledLink findOriginLink;
     private LabeledLink translateAllLink;
     private Map<Long, String> translateMap = new LinkedHashMap<Long, String>();
     private boolean translateAll = false;
     private int hitsPerPage;
     private OneLineAdLazyLoadPanel lazyLoadAdPanel;
     private Map<Long, SolrTweet> allTweets = new LinkedHashMap<Long, SolrTweet>();
     // for test only
 
     public ResultsPanel(String id) {
         this(id, "en");
     }
 
     public ResultsPanel(String id, final String toLanguage) {
         super(id);
 
         lazyLoadAdPanel = new OneLineAdLazyLoadPanel("onelinead") {
 
             @Override
             public OneLineAdPanel createAdPanel(String id) {
                 return new OneLineAdPanel(id) {
 
                     @Override
                     public OneTweet createOneTweetPanel(String id) {
                         return createOneTweet(id, toLanguage);
                     }
                 };
             }
         };
         add(lazyLoadAdPanel.setOutputMarkupId(true));
         add(new Label("qm", new PropertyModel(this, "queryMessage")));
         add(new Label("qmWarn", new PropertyModel(this, "queryMessageWarn")) {
 
             @Override
             public boolean isVisible() {
                 return queryMessageWarn != null && queryMessageWarn.length() > 0;
             }
         });
 
         add(createHitLink(15));
         add(createHitLink(30));
         add(createHitLink(60));
 
         Model qModel = new Model() {
 
             @Override
             public Serializable getObject() {
                 if (query == null)
                     return "";
                 String str = query;
                 if (str.length() > 20)
                     str = str.substring(0, 20) + "..";
                 return "Find origin of '" + str + "'";
             }
         };
         findOriginLink = new LabeledLink("findOriginLink", null, qModel, false) {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 PageParameters pp = new PageParameters();
                 pp.add("findOrigin", query);
                 setResponsePage(getApplication().getHomePage(), pp);
             }
         };
 
         add(findOriginLink);
         translateAllLink = new LabeledLink("translateAllLink", null, new Model<String>() {
 
             @Override
             public String getObject() {
                 if (translateAll)
                     return "Show original language";
                 else
                     // get english name of iso language chars
                     return "Translate tweets into " + new Locale(toLanguage).getDisplayLanguage(new Locale("en"));
             }
         }) {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 if (target == null)
                     return;
 
                 translateAll = !translateAll;
                 if (!translateAll)
                     translateMap.clear();
                 target.addComponent(ResultsPanel.this);
             }
         };
 
         add(translateAllLink);
         add(createSortLink("sortRelevance", ""));
         add(createSortLink("sortRetweets", SolrTweetSearch.RT_COUNT + " desc"));
         add(createSortLink("sortLatest", SolrTweetSearch.DATE + " desc"));
         add(createSortLink("sortOldest", SolrTweetSearch.DATE + " asc"));
 
         userView = new ListView("users", users) {
 
             @Override
             public void populateItem(final ListItem item) {
                 final SolrUser user = (SolrUser) item.getModelObject();
                 String twitterUrl = Helper.TURL + "/" + user.getScreenName();
 
                 String name = user.getScreenName();
                 if (user.getRealName() != null)
                     name = user.getRealName() + "  (" + name + ")";
 
                 LabeledLink userNameLink = new LabeledLink("userNameLink", name, false) {
 
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         onUserClick(user.getScreenName(), null);
                     }
                 };
                 item.add(userNameLink);
 
                 Link showLatestTweets = new Link("profileUrl") {
 
                     @Override
                     public void onClick() {
                         onUserClick(user.getScreenName(), null);
                     }
                 };
                 item.add(new ExternalLink("latestTw", twitterUrl, "twitter.com/" + name));
                 item.add(showLatestTweets.add(new ContextImage("profileImg", user.getProfileImageUrl())));
 
                 final List<SolrTweet> tweets = new ArrayList<SolrTweet>();
                 int counter = 0;
 
                 for (SolrTweet tw : user.getOwnTweets()) {
                     if (tweetsPerUser > 0 && counter >= tweetsPerUser)
                         break;
 
                     tweets.add(tw);
                     allTweets.put(tw.getTwitterId(), tw);
                     counter++;
                 }
                 ListView tweetView = new ListView("tweets", tweets) {
 
                     @Override
                     public void populateItem(final ListItem item) {
                         item.add(createOneTweet("oneTweet", toLanguage).init(item.getModel(), false));
                     }
                 };
                 item.add(tweetView);
             }
         };
 
         add(userView);
         WebResource export = new WebResource() {
 
             @Override
             public IResourceStream getResourceStream() {
                 return new StringResourceStream(getTweetsAsString(), "text/plain");
             }
 
             @Override
             protected void setHeaders(WebResponse response) {
                 super.setHeaders(response);
                 response.setAttachmentHeader("tweets.csv");
             }
         };
 
         export.setCacheable(false);
         add(new ResourceLink("exportCsvLink", export));
         add(new Link("exportHtmlLink") {
 
             @Override
             public void onClick() {
                 onHtmlExport();
             }
         });
     }
 
     public OneTweet createOneTweet(String id, final String lang) {
         return new OneTweet("oneTweet") {
 
             @Override
             public String getTextFromTranslateAllAction(long id) {
                 if (translateAll && translateMap.size() == 0)
                     fillTranslateMap(allTweets.values(), lang);
 
                 return translateMap.get(id);
             }
 
             @Override
             public Collection<SolrTweet> onReplyClick(long id, boolean retweet) {
                 return onTweetClick(id, retweet);
             }
 
             @Override
             public void onUserClick(String screenName) {
                 ResultsPanel.this.onUserClick(screenName, null);
             }
 
             @Override
             public void onFindSimilarClick(SolrTweet tweet) {
                 ResultsPanel.this.onFindSimilar(tweet);
             }
 
             @Override
             public Collection<SolrTweet> onInReplyOfClick(long id) {
                 return ResultsPanel.this.onInReplyOfClick(id);
             }
         }.setLanguage(lang);
     }
 
     public void fillTranslateMap(Collection<SolrTweet> tweets, String toLang) {
         Map<Integer, Long> index2Id = new LinkedHashMap<Integer, Long>();
         String[] texts = new String[tweets.size()];
         Language[] froms = new Language[tweets.size()];
         Language[] tos = new Language[tweets.size()];
 
         try {
             Language toLanguage = Language.fromString(toLang);
             Iterator<SolrTweet> iter = tweets.iterator();
             for (int i = 0; i < texts.length; i++) {
                 SolrTweet tweet = iter.next();
                 index2Id.put(i, tweet.getTwitterId());
                 texts[i] = tweet.getText();
                 froms[i] = Language.AUTO_DETECT;
                 tos[i] = toLanguage;
             }
 
             String newTxts[] = Helper.translateAll(texts, froms, tos);
             for (int i = 0; i < newTxts.length; i++) {
                 Long twitterId = index2Id.get(i);
                 if (twitterId != null)
                     translateMap.put(twitterId, newTxts[i]);
             }
 //            System.out.println(translateMap);
         } catch (Exception ex) {
             logger.error("Couldn't translate all tweets", ex);
         }
     }
 
     Map<Long, String> getTranslateMap() {
         return translateMap;
     }
 
     public void onUserClick(String userName, String query) {
     }
 
     public void onFindSimilar(SolrTweet tweet) {
     }
 
     public void onSortClicked(AjaxRequestTarget target, String sortStr) {
     }
 
     public String getTweetsAsString() {
         return "";
     }
 
     public void onHtmlExport() {
     }
 
     public Collection<SolrTweet> onTweetClick(long id, boolean retweet) {
         return Collections.EMPTY_LIST;
     }
 
     public Collection<SolrTweet> onInReplyOfClick(long id) {
         return Collections.EMPTY_LIST;
     }
 
     public void clear() {
         allTweets.clear();
         translateAll = false;
         translateMap.clear();
         users.clear();
         queryMessage = "";
         queryMessageWarn = "";
     }
 
     public void setQueryMessage(String queryMessage) {
         this.queryMessage = queryMessage;
     }
 
     public void setQueryMessageWarn(String queryMessageWarn) {
         this.queryMessageWarn = queryMessageWarn;
     }
 
     public void setTweetsPerUser(int twPerUser) {
         tweetsPerUser = twPerUser;
     }
 
     public void add(SolrUser u) {
         users.add(u);
     }
 
     public void setQuery(String visibleString) {
         query = visibleString;
         if (query == null || query.isEmpty())
             findOriginLink.setVisible(false);
     }
 
     public void setUser(String u) {
         user = u;
     }
 
     public void setHitsPerPage(int hits) {
         hitsPerPage = hits;
     }
 
     public void setSort(String sortString) {
         if (sortString == null)
             sort = "";
         else
             sort = sortString;
     }
 
     public Link createHitLink(final int hits) {
         Link link = new AjaxFallbackLink("hits" + hits) {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 PageParameters pp = new PageParameters();
                 pp.add("h", "" + hits);
                 pp.add("u", user);
                 pp.add("q", query);
                 setResponsePage(getApplication().getHomePage(), pp);
             }
         };
 
         link.add(new AttributeAppender("class", new Model() {
 
             @Override
             public Serializable getObject() {
                 return hits == hitsPerPage ? "selected" : "";
             }
         }, " "));
 
         return link;
     }
 
     public AjaxFallbackLink createSortLink(String id, final String sorting) {
         AjaxFallbackLink link = new AjaxFallbackLink(id) {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 if (target != null)
                     onSortClicked(target, sorting);
             }
         };
         link.add(new AttributeAppender("class", new Model() {
 
             @Override
             public Serializable getObject() {
                 return sorting.equals(sort) ? "selected" : "";
             }
         }, " "));
 
         return link;
     }
 
     public void setAdQuery(String queryString) {
         lazyLoadAdPanel.setSearchQuery(queryString);
     }
 }
