 package bookshelf;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import net.htmlparser.jericho.Source;
 
 public abstract class AbstractBook implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private Source source;
 	protected ISBN isbn;
 	protected String publisher;
 	protected int publishingYear;
 	protected String title;
 	private Image cover;
 	private boolean cachedCover;
 	protected ArrayList<String> authors = new ArrayList<String>();
 	
 	public ISBN getISBN() {
 		return this.isbn;
 	}
 
 	public ArrayList<String> getAuthors() {
 		return this.authors;
 	}
 
 	public String getTitle() {
 		return this.title;
 	}
 
 	public String getPublisher() {
 		return this.publisher;
 	}
 
 	public int getPublishingYear() {
 		return this.publishingYear;
 	}
 
 	protected void setTitle(String title) {
 		this.title = title;
 	}
 
 	protected void setISBN(ISBN isbn) {
 		this.isbn = isbn;
 	}
 
 	protected void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 
 	protected void setPublishingYear(int year) {
 		this.publishingYear = year;
 	}
 
 	public boolean hasTitle() {
 		return (getTitle() != null);
 	}
 
 	public boolean hasISBN() {
 		return (getISBN() != null);
 	}
 
 	public boolean hasAuthors() {
 		return (getAuthors().size() > 0);
 	}
 
 	public boolean hasPublisher() {
 		return (getPublisher() != null);
 	}
 	
 	public boolean hasPublishingYear() {
 		return (getPublishingYear() > 0);
 	}
 
 	protected void addAuthor(String author) {
 		this.authors.add(author);
 	}
 	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hasISBN()) ? 0 : getISBN().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		AbstractBook other = (AbstractBook) obj;
		
		if (!hasISBN() || !other.hasISBN())
			return false;
		else if (!getISBN().equals(other.getISBN()))
 			return false;
 		
		return true;
 	}
 	
 	protected Source getSource() {
 		return this.source;
 	}
 	
 	public Image getCover() throws IOException {
 		if(!hasCachedCover()) {
 			HttpURLConnection coverCon = (HttpURLConnection)
 				getCoverUrl().openConnection();
 			coverCon.addRequestProperty("User-Agent", "Mozilla/5.0");
 			coverCon.connect();
 			
 			InputStream urlStream = coverCon.getInputStream();
             BufferedImage cover = ImageIO.read(urlStream);
 
 		    setCover(Toolkit.getDefaultToolkit().createImage(cover.getSource()));
 		    urlStream.close();
 		    coverCon.disconnect();
 			setCachedCover(true);
 		}
 
 		return this.cover;
 	}
 	
 	protected void setCachedCover(boolean b) {
 		this.cachedCover = b;
 	}
 	
 	private void setCover(Image cover) {
 		this.cover = cover;
 	}
 	
 	public boolean hasCover() throws IOException {
 		return (getCover() != null);
 	}
 	
 	private boolean hasCachedCover() {
 		return cachedCover;
 	}
 	
 	public abstract URL getCoverUrl() throws IOException;
 
 	protected void setSource(Source source) {
 		this.source = source;
 	}
 	
 	public void combineWith(AbstractBook book) {
 		if (!this.equals(book))
 			return;
 		
 		// Combine information with priority this
 	}
 }
