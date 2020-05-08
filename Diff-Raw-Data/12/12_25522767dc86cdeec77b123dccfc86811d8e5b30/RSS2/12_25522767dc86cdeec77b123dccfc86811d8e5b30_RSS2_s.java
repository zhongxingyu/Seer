 package de.metacoder.blog.pages.rss;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.tapestry5.annotations.ContentType;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import de.metacoder.blog.persistence.entities.BlogEntry;
 import de.metacoder.blog.persistence.repositories.BlogEntryRepository;
 
 @ContentType("application/rss+xml")
 public class RSS2 {
 	
 	@Inject
 	@Property
 	private BlogEntryRepository blogEntryRepositroy;
 
 	@Property
 	BlogEntry currentEntry;
 
 	public String getAuthorNames(){
		return StringUtils.join(currentEntry.getAuthors(), ",");
 	}
 }
