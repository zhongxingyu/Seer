 package bookshelf.apis.google;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Source;
 import bookshelf.AbstractBook;
 import bookshelf.ISBN;
 import bookshelf.Keyword;
 
 
 public class GoogleBook extends AbstractBook {
 	private static final long serialVersionUID = 1L;
 	private String summary;
 	private float rating = -1f;
 	private ArrayList<String> subtitles = new ArrayList<String>();
 	private ArrayList<Keyword> keywords = new ArrayList<Keyword>();
 	private boolean cachedSummary;
 	private boolean cachedRating;
 	private boolean cachedTitle;
 	private boolean cachedSubtitles;
 	private boolean cachedKeywords;
 	
 	public GoogleBook(String url) throws IOException {
 		HttpURLConnection bookCon = (HttpURLConnection)
 				(new URL(url)).openConnection();
 		bookCon.addRequestProperty("User-Agent", "Mozilla/5.0");
 		setSource(new Source(bookCon));
 		this.cacheMetadata();
 	}
 	
 	public GoogleBook(ISBN isbn) throws IOException {
 		this("http://books.google.com/books?isbn=" + isbn);
 	}
 	
 	private void addSubtitle(String subtitle) {
 		this.subtitles.add(subtitle);
 	}
 	
 	private void addKeyword(Keyword keyword) {
 		this.keywords.add(keyword);
 	}
 	
 	public URL getCoverUrl() throws IOException {
 		return new URL("http://books.google.com/books?vid=ISBN"+getISBN()+"&printsec=frontcover&img=1&zoom=1");
 	}
 	
 	public String getSummary() {
 		if (!hasCachedSummary()) {
 			Element element = getSource().getElementById("synopsistext");
 			
 			if (element != null) {
 				setSummary(element.getTextExtractor().setIncludeAttributes(false).toString());
 			}
 			
 			setCachedSummary(true);
 		}
 		
 		return this.summary;
 	}
 
 	public String getTitle() {
 		if (!hasCachedTitle()) {
 			List<Element> elements = getSource().getAllElementsByClass("booktitle");
 			for (Element e : elements) {
 				e = e.getFirstElement();
 				setTitle(e.getTextExtractor().setIncludeAttributes(false).toString());
 			}
 			
 			setCachedTitle(true);
 		}
 		
 		return this.title;
 	}
 
 	public ArrayList<String> getSubtitles() {
 		if (!hasCachedSubtitles()) {
 			List<Element> elements = getSource().getAllElementsByClass("subtitle");
 			for (Element e : elements)
 				addSubtitle(e.getTextExtractor().setIncludeAttributes(false).toString());
 			
 			setCachedSubtitles(true);
 		}
 		
 		return new ArrayList<String>(this.subtitles);
 	}
 
 	public ArrayList<Keyword> getKeywords() {
 		if (!hasCachedWords()) {
 		
 			Element element = getSource().getFirstElementByClass("cloud");
 			if (element != null) {
 				List<Element> elements = element.getAllElements(HTMLElementName.A);
 				for (Element e : elements) {
 					String classValue = e.getAttributeValue("class");
 					int length = classValue.length();
					int nbr = Integer.parseInt(classValue.substring(length-1, length));
 					String value = e.getTextExtractor().setIncludeAttributes(false).toString();
					addKeyword(new Keyword(value, 1/(nbr+1)));
 				}
 			}
 			
 			setCachedKeywords(true);
 		}
 		
 		return this.keywords;
 	}
 	
 	public float getRating() {
 		if (!hasCachedRating()) {
 			List<Element> starOn = getSource().getAllElementsByClass("gb-star-on");
 			List<Element> starHalf = getSource().getAllElementsByClass("gb-star-half");
 			
 			if (starOn.size() > 0 || starHalf.size() > 0)
 				setRating(1f*starOn.size() + 0.5f*starHalf.size());
 			
 			setCachedRating(true);
 		}
 		
 		return this.rating;
 	}
 	
 	protected void cacheMetadata() {
 		List<Element> metalabels = getSource().getAllElementsByClass("metadata_label");
 		List<Element> metavalues = getSource().getAllElementsByClass("metadata_value");
 		
 		for (int i=0; i<metalabels.size(); i++) {
 			String label = metalabels.get(i).getTextExtractor().setIncludeAttributes(false).toString();
 			
 			if (label.equals("ISBN")) {
 				String[] isbns = metavalues.get(i).getTextExtractor().setIncludeAttributes(false).toString().split(", ");
 				setISBN(new ISBN(isbns[0]));
 			} else if (label.equals("Author") || label.equals("Authors")) {
 				List<Element> elements = metavalues.get(i).getAllElements(HTMLElementName.A);
 				for (Element e : elements)
 					addAuthor(e.getTextExtractor().setIncludeAttributes(false).toString());
 			} else if (label.equals("Publisher")) {
 				String[] values = metavalues.get(i).getTextExtractor().setIncludeAttributes(false).toString().split(", ");
 				
 				if (values.length >= 1)
 					setPublisher(values[0]);
 				
 				if (values.length >= 2)
 					setPublishingYear(Integer.parseInt(values[values.length-1]));
 			}
 		}
 	}
 	
 	private void setCachedTitle(boolean b) {
 		this.cachedTitle = b;
 	}
 	
 	private void setCachedSubtitles(boolean b) {
 		this.cachedSubtitles = b;
 	}
 	
 	private void setCachedSummary(boolean b) {
 		this.cachedSummary = b;
 	}
 	
 	private void setCachedRating(boolean b) {
 		this.cachedRating = b;
 	}
 	
 	private void setCachedKeywords(boolean b) {
 		this.cachedKeywords = b;
 	}
 	
 	private void setSummary(String summary) {
 		this.summary = summary;
 	}
 	
 	private void setRating(float rating) {
 		this.rating = rating;
 	}
 	
 	public boolean hasSubtitles() {
 		return (getSubtitles().size() > 0);
 	}
 	
 	public boolean hasRating() {
 		return (getRating() != -1f);
 	}
 	
 	public boolean hasSummary() {
 		return (getSummary() != null);
 	}
 	
 	public boolean hasKeywords() {
 		return (getKeywords().size() > 0);
 	}
 	
 	private boolean hasCachedTitle() {
 		return cachedTitle;
 	}
 	
 	private boolean hasCachedSubtitles() {
 		return cachedSubtitles;
 	}
 	
 	private boolean hasCachedRating() {
 		return cachedRating;
 	}
 	
 	private boolean hasCachedSummary() {
 		return cachedSummary;
 	}
 	
 	private boolean hasCachedWords() {
 		return cachedKeywords;
 	}
 	
 	public void resetCache() {
 		setCachedCover(false);
 		setCachedRating(false);
 		setCachedSubtitles(false);
 		setCachedSummary(false);
 		setCachedTitle(false);
 		setCachedKeywords(false);
 	}
 }
