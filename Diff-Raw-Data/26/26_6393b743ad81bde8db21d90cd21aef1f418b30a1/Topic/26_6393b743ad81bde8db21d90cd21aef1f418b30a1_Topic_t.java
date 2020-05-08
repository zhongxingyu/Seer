 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 // A Topic is the large concept of a story,
 // it is a collection of relevant keywords,
 // links to the relevant articles and the
 // dates these articles were picked up
 public class Topic implements Comparable<Topic> {
 	
 	private HashMap<String, WordInfo> words;
 	private LinkedList<Article> articles;
 	private int numWords;
 	private Date timestamp;
 	private String recentTitle;
 	private int uid;
 	double sentiment;
 	ArrayList<CompanyLink> companies;
 	Article first;
 	int count;
 
 	public Topic(Article article, int uid, double sentiment2, ArrayList<CompanyLink> companies) {
 		first = article;
 		this.articles = new LinkedList<Article>();
 		this.articles.push(article);
 		this.numWords = 0;
 		this.timestamp = new Date();
 		this.recentTitle = article.getTitle();
 		this.uid = uid;
 		this.sentiment = sentiment2;
 		this.companies = companies;
 		count = 1;
 	}
 	
 	private void mergeCompanies(ArrayList<CompanyLink> comps) {
 		ArrayList<CompanyLink> tempList = new ArrayList<CompanyLink>();
 		for(CompanyLink comp1 : companies)
 		{
 			for(CompanyLink comp2 : comps)
 			{
 				if(comp1.getCompany().equals(comp2.getCompany())){
 					comp1.merge(comp2);
 				}
 				else
 				{
 					tempList.add(comp2);
 				}
 			}
 		}
 		companies.addAll(tempList);
 	}
 
 	public int compareTo(Topic temp) {
 		return temp.artsLastHour() - this.artsLastHour();
 	}
 	
 	public double getRel(String str) {
 		return words.get(str).getRel();
 	}
 	
 	public void addSentiment(double sentiment2)
 	{
 		this.sentiment = (this.sentiment + sentiment2) / 2;
 	}
 
 	public double getSentiment() {
 		return sentiment;
 	}
 
 	// Add article only if new
 	public void addArticle(Article article, double sentiment2, ArrayList<CompanyLink> comps) {
 	    Iterator<Article> iterator = articles.iterator();
 	    boolean repeat = false;
 	    while (iterator.hasNext()) {
 	    	Article nextArticle = iterator.next();
 	       if (article.getURL().compareTo(nextArticle.getURL()) == 0 || article.getTitle().compareTo(nextArticle.getTitle()) == 0) {
 	    	   repeat = true;
 	    	   System.out.println("Repeat article");
 	    	   break;
 	       }
 	    }
 	    if (!repeat){
 	    	articles.push(article);
 	    	this.sentiment = (this.sentiment*(articles.size()-1) + sentiment2)/(articles.size()-1);
 			mergeCompanies(comps);
 			timestamp = new Date();
 			count++;
 	    }
 	    recentTitle = article.getTitle();
 	}
 
 	public void addWord(String s, Double d, Double sent) {
 		if (numWords == 0)
 			words = new HashMap<String, WordInfo>();
 		if (words.containsKey(s)) {
 			WordInfo currentInfo = words.get(s);
 			currentInfo.setRel(currentInfo.getRel() + d);
 			currentInfo.setSent((currentInfo.getSent()*(articles.size()-1) + d) / articles.size());
 			words.remove(s);
 			words.put(s, currentInfo);
 		}
 		else
 			words.put(s, new WordInfo(d/articles.size(), sent));
 		numWords++;
 	}
 
 	public boolean containsWord(String s) {
 		if (words.containsKey(s))
 			return true;
 		else
 			return false;
 	}
 	
 	// Displays all the keyword information
 	public void printWordData() {
 	    Iterator<String> iterator = words.keySet().iterator();
 	    while (iterator.hasNext()) {  
 	       String key = iterator.next().toString();  
 	       String value = words.get(key).getRel().toString(); 
 	       String sent = words.get(key).getSent().toString(); 
 	       System.out.println(key + " " + value + " " + sent);  
 	    }  
 	}
 	
 	public void printTopWords() {
 		System.out.print(this.topWords());
 	}
 	
 	// Displays top 10 words
 	public String topWords() {
 		final class WordAndVal implements Comparable<WordAndVal> {
 			private final String _word;
 			private final Double _count;
 			
 			public WordAndVal(String word, double count) {
 				this._word = word;
 				this._count = count;
 			}
 			
 			public Double getCount() {
 				return _count;
 			}
 			
 			public String getWord() {
 				return _word;
 			}
 			
 			public int compareTo(WordAndVal wav) {
 				return this._count.compareTo(wav.getCount());
 			}
 		};
 	    Iterator<String> iterator = words.keySet().iterator();
 	    ArrayList<WordAndVal> wavl = new ArrayList<WordAndVal>();
 	    while (iterator.hasNext()) {  
 	       String key = iterator.next();  
 	       Double val = words.get(key).getRel();
 	       wavl.add(new WordAndVal(key, val));
 	    }  
 	    Collections.sort(wavl);
 	    int j = 10;
 	    if (wavl.size() < 10)
 	    	j = wavl.size();
 	    String rString = "";
 	    for (int i = 1; i <= j; i++) {
 	    	WordAndVal nextWord = wavl.get(wavl.size() - i);
 	    	rString = rString + nextWord.getWord() + "@" + words.get(nextWord.getWord()).getSent() + ";\n";
 	    }
 	    return rString;
 	}
 	
 	public void printTopLinks() {
 		System.out.println(this.topArticles());
 	}
 	
 	// Displays last 5 links
 	public String topArticles() {
 		String rString = "";
 		int j = 5;
 		if (articles.size() < 5)
 			j = articles.size();
 		for (int i = 1; i <= j; i++)
 			rString = rString + articles.get(articles.size() - i).getURL() + 
 			"@" + articles.get(articles.size() - i).getTitle() +
 			"@" + articles.get(articles.size() - i).getSource() +
 			"@" + articles.get(articles.size() - i).getDescription() +
 			";\n";
 		return rString;
 	}
 	
 	// Return the number of articles received in the last hour
 	public int artsLastHour() {
 		int result = 0;
 		Date hourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
 		for (int sizeof = articles.size() - 1; sizeof >= 0; sizeof--) {
 			if (articles.get(sizeof).getDate().compareTo(hourAgo) >= 0) {
 				result++;
 			}
 		}
 		return result;
 	}
 
 	public LinkedList<Article> getArticles() {
 		return articles;
 	}
 
 	public String getDate() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		return dateFormat.format(timestamp);
 	}
 	
 	public String getRecentTitle() {
 		return recentTitle;
 	}
 	
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	public int getUid() {
 		return uid;
 	}
 
 	public HashMap<String, WordInfo> getWords() {
 		return words;
 	}
 	
 	public void mergeTopic(Topic toMerge){
 		// Ideally, have a merge topic function
 		// complex, ugly code needed
 	}
 
 	public Article getFirst() {
 		return first;
 	}
 
 	public int getCount() {
 		return count;
 	}
 
 	public String sendCompanyList() {
 		String ret = "";
 		for(CompanyLink link : companies)
 		{
 			ret += link.getCompany() + "@" + link.getRelevance() + "@" + link.getSentiment() + ";\n";
 		}
 		return ret;
 	}
 	
	public double totalRel() {
		double total = 0.0;
		Iterator<WordInfo> iterator = words.values().iterator();
	    while (iterator.hasNext()) {  
	       WordInfo wi = iterator.next();
	       total += wi.getRel();
	    }
	    return total;
	}
	
 }
 
 class CompanyLink {
 	String name;
 	Double sentiment;
 	Double relevance;
 	int num;
 
 	CompanyLink (String n, Double s, Double r)
 	{
 		name = n;
 		sentiment = s;
 		relevance = r;
 		num = 1;
 	}
 
 	public String getCompany() {
 		return name;
 	}
 
 	public Double getSentiment() {
 		return sentiment;
 	}
 
 	public Double getRelevance() {
 		return relevance;
 	}
 
 	public void merge(CompanyLink comp2) {
 		num++;
 		relevance += comp2.getRelevance();
 		sentiment = ((sentiment * (num-1)) + comp2.getSentiment())/num;
 	}
 
 }
