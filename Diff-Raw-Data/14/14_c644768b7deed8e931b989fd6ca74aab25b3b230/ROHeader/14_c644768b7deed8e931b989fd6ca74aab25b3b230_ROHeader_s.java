 package pl.psnc.dl.wf4ever.portal.model;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 public class ROHeader
 	implements Serializable
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8588940512280331265L;
 
 	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd");
 
 	private final URI uri;
 
 	private final String author;
 
 	private final Calendar created;
 
 
 	/**
 	 * @param uri
 	 * @param author
 	 * @param created
 	 */
 	public ROHeader(URI uri, String author, Calendar created)
 	{
 		this.uri = uri;
 		this.author = author;
 		this.created = created;
 	}
 
 
 	/**
 	 * @return the uri
 	 */
 	public URI getUri()
 	{
 		return uri;
 	}
 
 
 	/**
 	 * @return the author
 	 */
 	public String getAuthor()
 	{
 		return author;
 	}
 
 
 	/**
 	 * @return the created
 	 */
 	public Calendar getCreated()
 	{
 		return created;
 	}
 
 
 	/**
 	 * @return the created
 	 */
 	public String getCreatedFormatted()
 	{
		return SDF.format(getCreated().getTime());
 	}
 
 
 	public String getName()
 	{
 		return getUri().resolve("..").relativize(getUri()).toString();
 	}
 
 }
