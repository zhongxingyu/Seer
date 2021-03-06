 package com.uservoice.uservoicesdk.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.TextView;
 
 import com.uservoice.uservoicesdk.Config;
 import com.uservoice.uservoicesdk.R;
 import com.uservoice.uservoicesdk.Session;
 import com.uservoice.uservoicesdk.activity.ArticleActivity;
 import com.uservoice.uservoicesdk.activity.ContactActivity;
 import com.uservoice.uservoicesdk.activity.ForumActivity;
 import com.uservoice.uservoicesdk.activity.SuggestionActivity;
 import com.uservoice.uservoicesdk.activity.TopicActivity;
 import com.uservoice.uservoicesdk.babayaga.Babayaga;
 import com.uservoice.uservoicesdk.flow.InitManager;
 import com.uservoice.uservoicesdk.model.Article;
 import com.uservoice.uservoicesdk.model.BaseModel;
 import com.uservoice.uservoicesdk.model.Forum;
 import com.uservoice.uservoicesdk.model.Suggestion;
 import com.uservoice.uservoicesdk.model.Topic;
 import com.uservoice.uservoicesdk.rest.Callback;
 
 public class PortalAdapter extends SearchAdapter<BaseModel> implements AdapterView.OnItemClickListener {
 	
 	private static int KB_HEADER = 0;
 	private static int FORUM = 1;
 	private static int TOPIC = 2;
 	private static int LOADING = 3;
 	private static int CONTACT = 4;
 	private static int ARTICLE = 5;
 	private static int SUGGESTION = 6;
 	
 	private LayoutInflater inflater;
 	private final Context context;
 	private List<Integer> staticRows;
 	
 	public PortalAdapter(Context context) {
 		this.context = context;
 		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		new InitManager(context, new Runnable() {
 			@Override
 			public void run() {
 				notifyDataSetChanged();
 				// this has to be deferred only because we fall back to clientconfig forum_id 
 				loadForum();
 			}
 		}).init();
 		loadTopics();
 	}
 	
 	private List<Topic> getTopics() {
 		return Session.getInstance().getTopics();
 	}
 	
 	private List<Article> getArticles() {
 		return Session.getInstance().getArticles();
 	}
 	
 	private boolean shouldShowArticles() {
 		return Session.getInstance().getConfig().getTopicId() != -1 || (getTopics() != null && getTopics().isEmpty());
 	}
 	
 	private void loadForum() {
 		Forum.loadForum(Session.getInstance().getConfig().getForumId(), new DefaultCallback<Forum>(context) {
 			@Override
 			public void onModel(Forum model) {
 				Session.getInstance().setForum(model);
 				notifyDataSetChanged();
 			}
 		});
 	}
 	
 	private void loadTopics() {
 		final DefaultCallback<List<Article>> articlesCallback = new DefaultCallback<List<Article>>(context) {
 			@Override
 			public void onModel(List<Article> model) {
 				Session.getInstance().setTopics(new ArrayList<Topic>());
 				Session.getInstance().setArticles(model);
 				notifyDataSetChanged();
 			}
 		};
 		
 		if (Session.getInstance().getConfig().getTopicId() != -1) {
 			Article.loadForTopic(Session.getInstance().getConfig().getTopicId(), articlesCallback);
 		} else {
 			Topic.loadTopics(new DefaultCallback<List<Topic>>(context) {
 				@Override
 				public void onModel(List<Topic> model) {
 					Session.getInstance().setTopics(model);
 					if (getTopics().isEmpty()) {
 						Article.loadAll(articlesCallback);
 					} else {
 						notifyDataSetChanged();
 					}
 				}
 			});
 		}
 	}
 	
 	private void computeStaticRows() {
 		if (staticRows == null) {
 			staticRows = new ArrayList<Integer>();
 			Config config = Session.getInstance().getConfig();
 			if (config.shouldShowContactUs())
 				staticRows.add(CONTACT);
 			if (config.shouldShowForum())
 				staticRows.add(FORUM);
 			if (config.shouldShowKnowledgeBase())
 				staticRows.add(KB_HEADER);
 		}
 	}
 
 	@Override
 	public int getCount() {
		if (Session.getInstance().getClientConfig() == null) {
 			return 1;
 		} else if (shouldShowSearchResults()) {
 			return loading ? 1 : searchResults.size();
 		} else {
 			computeStaticRows();
 			return staticRows.size() + (Session.getInstance().getConfig().shouldShowKnowledgeBase() ? (getTopics() == null ? 1 : (shouldShowArticles() ? getArticles().size() : getTopics().size() + 1)) : 0);
 		}
 	}
 
 	@Override
 	public Object getItem(int position) {
 		if (shouldShowSearchResults())
 			return loading ? null : searchResults.get(position);
 		computeStaticRows();
 		if (position < staticRows.size() && staticRows.get(position) == FORUM)
 			return Session.getInstance().getForum();
 		else if (getTopics() != null && !shouldShowArticles() && position >= staticRows.size() && position - staticRows.size() < getTopics().size()) 
 			return getTopics().get(position - staticRows.size());
 		else if (getArticles() != null && shouldShowArticles() && position >= staticRows.size() && position - staticRows.size() < getArticles().size())
 			return getArticles().get(position - staticRows.size());
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 	
 	@Override
 	public boolean isEnabled(int position) {
 		if (shouldShowSearchResults())
 			return !loading;
		if (Session.getInstance().getClientConfig() == null)
 			return false;
 		computeStaticRows();
 		if (position < staticRows.size()) {
 			int type = staticRows.get(position);
 			if (type == KB_HEADER || type == LOADING)
 				return false;
 		}
 		return true;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		int type = getItemViewType(position);
 		if (view == null) {
 			if (type == LOADING)
 				view = inflater.inflate(R.layout.loading_item, null);
 			else if (type == FORUM)
 				view = inflater.inflate(android.R.layout.simple_list_item_1, null);
 			else if (type == KB_HEADER)
 				view = inflater.inflate(android.R.layout.preference_category, null);
 			else if (type == TOPIC)
 				view = inflater.inflate(R.layout.topic_item, null);
 			else if (type == CONTACT)
 				view = inflater.inflate(android.R.layout.simple_list_item_1, null);
 			else if (type == ARTICLE)
 				view = inflater.inflate(R.layout.article_item, null);
 			else if (type == SUGGESTION)
 				view = inflater.inflate(R.layout.suggestion_result_item, null);
 		}
 		
 		if (type == FORUM) {
 			TextView textView = (TextView) view.findViewById(android.R.id.text1);
 			textView.setText(R.string.feedback_forum);
 		} else if (type == KB_HEADER) {
 			TextView textView = (TextView) view.findViewById(android.R.id.title);
 			textView.setText(R.string.knowledge_base);
 		} else if (type == TOPIC) {
 			Topic topic = (Topic) getItem(position);
 			TextView textView = (TextView) view.findViewById(R.id.topic_name);
 			textView.setText(topic == null ? context.getString(R.string.all_articles) : topic.getName());
 			textView = (TextView) view.findViewById(R.id.article_count);
 			if (topic == null) {
 				textView.setVisibility(View.GONE);
 			} else {
 				textView.setVisibility(View.VISIBLE);
 				textView.setText(String.format("%d %s", topic.getNumberOfArticles(), context.getResources().getQuantityString(R.plurals.articles, topic.getNumberOfArticles())));
 			}
 		} else if (type == CONTACT) {
 			TextView textView = (TextView) view.findViewById(android.R.id.text1);
 			textView.setText("Contact Us");
 		} else if (type == ARTICLE) {
 			TextView textView = (TextView) view.findViewById(R.id.article_name);
 			Article article = (Article) getItem(position);
 			textView.setText(shouldShowSearchResults() ? highlightResult(article.getTitle()) : article.getTitle());
 		} else if (type == SUGGESTION) {
 			TextView textView = (TextView) view.findViewById(R.id.suggestion_title);
 			Suggestion suggestion = (Suggestion) getItem(position);
 			textView.setText(highlightResult(suggestion.getTitle()));
 		}
 		return view;
 	}
 	
 	@Override
 	public int getViewTypeCount() {
 		return 7;
 	}
 	
 	@Override
 	public int getItemViewType(int position) {
		if (Session.getInstance().getClientConfig() == null)
 			return LOADING;
 		if (shouldShowSearchResults()) {
 			if (loading)
 				return LOADING;
 			BaseModel model = searchResults.get(position);
 			if (model instanceof Article)
 				return ARTICLE;
 			else if (model instanceof Suggestion)
 				return SUGGESTION;
 			else
 				return LOADING;
 		}
 		computeStaticRows();
 		if (position < staticRows.size()) {
 			int type = staticRows.get(position);
 			if (type == FORUM && Session.getInstance().getForum() == null)
 				return LOADING;
 			return type;
 		}
 		return getTopics() == null ? LOADING : (shouldShowArticles() ? ARTICLE : TOPIC);
 	}
 
 	@Override
 	protected void search(String query, Callback<List<BaseModel>> callback) {
 		currentQuery = query;
 		Babayaga.track(Babayaga.Event.SEARCH_ARTICLES);
 		Babayaga.track(Babayaga.Event.SEARCH_IDEAS);
 		Article.loadInstantAnswers(query, callback);
 	}
 	
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		int type = getItemViewType(position);
 		if (type == CONTACT) {
 			context.startActivity(new Intent(context, ContactActivity.class));
 		} else if (type == FORUM) {
 			context.startActivity(new Intent(context, ForumActivity.class));
 		} else if (type == TOPIC) {
 			Session.getInstance().setTopic((Topic) getItem(position));
 			context.startActivity(new Intent(context, TopicActivity.class));
 		} else if (type == ARTICLE) {
 			Session.getInstance().setArticle((Article) getItem(position));
 			context.startActivity(new Intent(context, ArticleActivity.class));
 		} else if (type == SUGGESTION) {
 			Session.getInstance().setSuggestion((Suggestion) getItem(position));
 			context.startActivity(new Intent(context, SuggestionActivity.class));
 		}
 	}
 
 }
