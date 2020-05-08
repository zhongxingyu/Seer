 package de.furryhome.e621api;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import de.furryhome.e621api.Exceptions.e621apiObjectException;
 
public final class List extends ApiMain {
 	private static final String LIST_URL = "http://www.e621.net/post/index.xml";
 	private static int count = 0;
 	private int limit = 10, page = 1, offset = 0;
 	private String atags[] = {};
 	private String stags = "";
 	
 	/*
 	 * Konstruktoren
 	 */
 	public List(int Value) {
 		super();
 		this.page = Value;
 	}
 	public List(int Page, int Limit, String Tags[]) {
 		super();
 		this.page = Page;
 		this.limit = Limit;
 		this.atags = Tags;
 		this.tagArraytoString();
 	}
 	public List(int Page, int Limit, String Tags, String TagLimiter) throws e621apiObjectException {
 		super();
 		this.page = Page;
 		this.limit = Limit;
 		this.stags = Tags;
 		this.tagStringtoArray(TagLimiter);
 	}
 	public List(int Page, int Limit) {
 		super();
 		this.page = Page;
 		this.limit = Limit;
 	}
 	public List(int Page, String Tags[]) {
 		super();
 		this.page = Page;
 		this.atags = Tags;
 		this.tagArraytoString();
 	}
 	public List(int Page, String Tags, String TagLimiter) throws e621apiObjectException {
 		super();
 		this.page = Page;
 		this.stags = Tags;
 		this.tagStringtoArray(TagLimiter);
 	}
 	public List(String Tags[]) {
 		super();
 		this.atags = Tags;
 		this.tagArraytoString();
 	}
 	public List(String Tags, String TagLimiter) throws e621apiObjectException {
 		super();
 		this.stags = Tags;
 		this.tagStringtoArray(TagLimiter);
 	}
 	
 	/*
 	 * Getter
 	 */
 	public int getListPage() { return this.page; }
 	public int getListLimit() { return this.limit; }
 	public int getItemCount() { return count; }
 	public int getItemOffset() { return offset; }
 	public String[] getTagsAsArray() { return this.atags; }
 	public String getTagsAsString() { return this.stags; }
 	
 	/*
 	 * Setter
 	 */
 	public void setTags(String Tags[]) {
 		this.atags = Tags;
 		this.tagArraytoString();
 	}
 	public void setTags(String Tags, String TagLimiter) throws e621apiObjectException {
 		this.stags = Tags;
 		this.tagStringtoArray(TagLimiter);
 	}
 	
 	/*
 	 * ffnetliche Funktionen
 	 */
 	void test() throws IOException, SAXException {
 		XMLReader xr = new com.bluecast.xml.Piccolo();
 		ListParser handler = new ListParser();
 		xr.setContentHandler(handler);
 		xr.setErrorHandler(handler);
 		FileReader r = new FileReader("C:\\SAS\\Users\\WinnME\\Documents\\Eclipse Workspace\\e621api\\ref\\index.xml");
 	    xr.parse(new InputSource(r));
 	}
 	
 	/*
 	 * Helferfunktionen
 	 */
 	private void tagStringtoArray(String Delimiter) throws e621apiObjectException {
 		if (Delimiter.length() == 0) { throw new e621apiObjectException("The tag delimiter may not be an empty string."); }
 		atags =  Pattern.compile(Delimiter).split(stags);
 	}
 	private void tagArraytoString() {
 		String out = "";
 		for (int i = 0; i < atags.length; i++) {	out = out + atags[i] + " "; }
 		if (out.length() > 0) {
 			stags = out.substring(0, out.length() - 1);
 		} else {
 			stags = "";
 		}
 	}
 }
