 package vs.piratenpartei.ch.app.helpers;
 
 import java.io.IOException;
 import java.net.URL;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.xml.sax.XMLReader;
 
 import vs.piratenpartei.ch.app.forum.ForumLink;
 import vs.piratenpartei.ch.app.forum.ThreadItem;
 import vs.piratenpartei.ch.app.forum.ThreadItemCollection;
 import vs.piratenpartei.ch.app.forum.TopicItem;
 import vs.piratenpartei.ch.app.forum.TopicItemCollection;
 
 import android.graphics.drawable.Drawable;
 import android.text.Editable;
 import android.text.Html;
 import android.util.Log;
 
 public class ForumParser 
 {
 	private static final String TAG = "vs.piratenpartei.ch.app.forum.ForumParser";
 	
 	private URL _forumUrl;
 	private Document _dom;
 	private boolean _docLoaded = false;
 	
 	private String _selectorSubject = "div.topic_table td.subject span a";
 	private String _selectorStarter = "div.topic_table td.subject p>a";
 	private String _selectorViews = "div.topic_table td.stats";
 	private String _selectorPosts = "div.topic_table td.stats";
 	private String _selectorLastUpdateDate = "div.topic_table td.lastpost";
 	private String _selectorLastMessageLink = "div.topic_table td.lastpost a:first-child";
 	private String _selectorLastUpdateAuthor = "div.topic_table td.lastpost a:last-child";
 	private String _selectorCompletePost = "#forumposts div.post_wrapper";
 	private String _selectorPostAuthor = "div.poster h4>a";
 	private String _selectorPostAuthorAvatar = "div.poster img.avatar";
 	private String _selectorPostContent = "div.postarea div.post div.inner";
 	private String _selectorPostDate = "div.postarea div.keyinfo div.smalltext";
 	private String _selectorLastBoardLink = "div.pagesection a.navPages:last-child";
 	
 	public ForumParser(URL pForumUrl)
 	{
 		Log.d(TAG, "new ForumParser(" + pForumUrl.toString() + ")");
 		this._forumUrl = pForumUrl;
 	}
 	
 	public String getSelectorPostAuthor()
 	{
 		return this._selectorPostAuthor;
 	}
 	
 	public String getSelectorPostAvatar()
 	{
 		return this._selectorPostAuthorAvatar;
 	}
 	
 	public String getSelectorPostContent()
 	{
 		return this._selectorPostContent;
 	}
 	
 	public String getSelectorPostDate()
 	{
 		return this._selectorPostDate;
 	}
 	
 	public void setSelectorPostAuthor(String pSelector)
 	{
 		this._selectorPostAuthor = pSelector;
 	}
 	
 	public void setSelectorPostAuthorAvatar(String pSelector)
 	{
 		this._selectorPostAuthorAvatar = pSelector;
 	}
 	
 	public void setSelectorPostContent(String pSelector)
 	{
 		this._selectorPostContent = pSelector;
 	}
 	
 	public void setSelectorPostDate(String pSelector)
 	{
 		this._selectorPostDate = pSelector;
 	}
 	
 	public void setSelectorSubject(String pSelector)
 	{
 		this._selectorSubject = pSelector;
 	}
 	
 	public void setSelectorStarter(String pSelector)
 	{
 		this._selectorStarter = pSelector;
 	}
 	
 	public void setSelectorViews(String pSelector)
 	{
 		this._selectorViews = pSelector;
 	}
 	
 	public void setSelectorPosts(String pSelector)
 	{
 		this._selectorPosts = pSelector;
 	}
 	
 	public void setSelectorLastUpdateDate(String pSelector)
 	{
 		this._selectorLastUpdateDate = pSelector;
 	}
 	
 	public void setSelectorLastUpdateAuthor(String pSelector)
 	{
 		this._selectorLastUpdateAuthor = pSelector;
 	}
 	
 	public void setSelectorLastMessageLink(String pSelector)
 	{
 		this._selectorLastMessageLink = pSelector;
 	}
 	
 	public void setSelectorPostTotal(String pSelector)
 	{
 		this._selectorCompletePost = pSelector;
 	}
 	
 	public void setSelectorBoardLastPageLink(String pSelector)
 	{
 		this._selectorLastBoardLink = pSelector;
 	}
 	
 	public boolean isDocumentLoaded()
 	{
 		return this._docLoaded;
 	}
 	
 	public void parseDocument() throws IOException
 	{
 		Log.d(TAG, "parseDocument()");
 		this._dom = Jsoup.parse(_forumUrl, 10000);
 		this._docLoaded = true;
 	}	
 
 	public Elements getSubjects()
 	{
 		return this.getElementsBySelector(this._selectorSubject);
 	}
 	
 	public Elements getStarters()
 	{
 		return this.getElementsBySelector(this._selectorStarter);
 	}
 	
 	public Elements getViews()
 	{
 		return this.getElementsBySelector(this._selectorViews);
 	}
 	
 	public Elements getPosts()
 	{
 		return this.getElementsBySelector(this._selectorPosts);
 	}
 	
 	public Elements getLastUpdateDates()
 	{
 		return this.getElementsBySelector(this._selectorLastUpdateDate);
 	}
 	
 	public Elements getLastMessageLink()
 	{
 		return this.getElementsBySelector(this._selectorLastMessageLink);
 	}
 	
 	public Elements getLastUpdateAuthors()
 	{
 		return this.getElementsBySelector(this._selectorLastUpdateAuthor);
 	}
 	
 	public Elements getPostAuthors()
 	{
 		return this.getElementsBySelector(this._selectorPostAuthor);
 	}
 	
 	public Elements getPostAvatars()
 	{
 		return this.getElementsBySelector(this._selectorPostAuthorAvatar);
 	}
 	
 	public Elements getPostContents()
 	{
 		return this.getElementsBySelector(this._selectorPostContent);
 	}
 	
 	public Elements getPostDates()
 	{
 		return this.getElementsBySelector(this._selectorPostDate);
 	}
 	
 	public Elements getCompletePost()
 	{
 		return this.getElementsBySelector(this._selectorCompletePost);
 	}
 	
 	public Element getLastBoardPageLink()
 	{
 		Elements result = this.getElementsBySelector(this._selectorLastBoardLink);
 		Log.i(TAG, "result.size() = " + result.size());
 		if(result.size() > 0)
 		{
 			return result.get(0);
 		}
 		return null;
 	}
 	
 	public Elements getElementsBySelector(String pSelector)
 	{
 		Log.d(TAG, "getElementsBySelector(" + pSelector + ")");
 		if(!this._docLoaded)
 		{
 			throw new IllegalStateException("Document is not loaded yet");
 		}
 		return this._dom.select(pSelector);		
 	}
 	
 
 	
 	public static ThreadItemCollection getBoard(int pBoardId) throws IOException
 	{
 		return getBoard(pBoardId, 0);
 	}
 	
 	public static ThreadItemCollection getBoard(int pBoardId, int pThreadOffset) throws IOException
 	{
 		Log.d(TAG, "getBoard(" + pBoardId + ", " + pThreadOffset + ")");
 		URL boardUrl = new URL("http://forum.piratenpartei.ch/index.php/board," + pBoardId + "." + pThreadOffset + ".html");
 		ThreadItemCollection result = new ThreadItemCollection();
 		ForumParser boardParser = new ForumParser(boardUrl);
 		boardParser.parseDocument();
 		Elements subjects = boardParser.getSubjects();
 		Elements starters = boardParser.getStarters();
 		Elements lastMessageLink = boardParser.getLastMessageLink();
 		Elements updateDates = boardParser.getLastUpdateDates();
 		Elements updateAuthors = boardParser.getLastUpdateAuthors();
 		Element boardPage = boardParser.getLastBoardPageLink();
 		if(boardPage != null)
 		{
 			String boardLink = boardPage.attr("href");
 			ForumLink lastBoardLink = ForumLink.parse(boardLink);
 			ThreadItem.LastBoardOffset = lastBoardLink.getOffset();
 		}
 		for(int index = 0; index < subjects.size(); index++)
 		{
 			ThreadItem current = new ThreadItem();
 			current.setTitle(subjects.get(index).text());
 			current.setTopicLink(subjects.get(index).attr("href"));
 			current.setStarter(starters.get(index).text());
 			String lastLink = lastMessageLink.get(index).attr("href");
 			ForumLink lnk = ForumLink.parse(lastLink);
 			current.setLastPossibleOffset(lnk.getOffset());
 			current.setLastUpdateDate(detectString(updateDates.get(index).text()));
 			current.setLastUpdateAuthor(updateAuthors.get(index).text());
 			result.add(current);
 		}
 		return result;
 	}
 	
 	public static TopicItemCollection loadTopic(String pTopicUrl) throws IOException
 	{
 		Log.d(TAG, "loadTopic(" + pTopicUrl + ")");
 		TopicItemCollection result = new TopicItemCollection();
 		ForumParser parser = new ForumParser(new URL(pTopicUrl));
 		parser.parseDocument();
 		Elements posts = parser.getCompletePost();
 		for(int index = 0; index < posts.size(); index++)
 		{
 			Element post = posts.get(index);
 			TopicItem current = new TopicItem();
 			Elements authors = post.select(parser.getSelectorPostAuthor());
 			if(authors.size() > 0)
 			{
 				Element author = authors.get(0);
 				current.setAuthor(author.text());
 			}
 			Elements avatars = post.select(parser.getSelectorPostAvatar());
 			if(avatars.size() > 0)
 			{
 				Element avatar = avatars.get(0);
 				String avatarUrl = avatar.attr("src");
 				URL avatarRef = new URL(avatarUrl);
 				Drawable avatarDrawable = Drawable.createFromStream(avatarRef.openStream(), current.getAuthor());
 				current.setAvatar(avatarDrawable);
 			}
 			Elements contents = post.select(parser.getSelectorPostContent());
 			if(contents.size() > 0)
 			{
 				Element content = contents.get(0);
 				current.setContent(Html.fromHtml(content.html(), new Html.ImageGetter() {
 					
 					@Override
 					public Drawable getDrawable(String pSource) {
 						Log.i(TAG, "Getting Image from: " + pSource);
 						URL src;
 						try {
 							src = new URL(pSource);
 							Drawable image = Drawable.createFromStream(src.openStream(), pSource);
							image.setBounds(0, 0, 0 + Math.max(image.getIntrinsicWidth(), 32), 0 + Math.max(image.getIntrinsicHeight(), 32));
 							return image;
 						} catch (IOException e) {
 							Log.w(TAG, e.getMessage());
 							e.printStackTrace();
 						}
 						return null;
 					}
 				}, new Html.TagHandler() {
 					
 					@Override
 					public void handleTag(boolean pOpening, String pTag, Editable pOutput,
 							XMLReader pXmlReader) {
 						//do nothing
 					}
 				}));
 			}
 			Elements dates = post.select(parser.getSelectorPostDate());
 			if(dates.size() > 0)
 			{
 				Element date = dates.get(0);
 				current.setDate(Html.fromHtml(date.html()).toString());
 			}
 			result.add(current);
 		}
 		return result;
 	}
 	
 	private static String detectString(String pInput)
 	{
 		String[] tempSplitted = pInput.split("\\sby\\s");
 		String date = tempSplitted[0];
 		return date;
 	}
 }
