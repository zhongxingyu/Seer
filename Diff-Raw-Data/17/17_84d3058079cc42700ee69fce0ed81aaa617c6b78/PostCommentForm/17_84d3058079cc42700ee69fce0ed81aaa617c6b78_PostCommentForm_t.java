 package de.metacoder.blog.components;
 
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.corelib.components.TextArea;
 import org.apache.tapestry5.corelib.components.TextField;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import de.metacoder.blog.persistence.entities.BlogEntry;
 import de.metacoder.blog.persistence.entities.BlogEntryComment;
 import de.metacoder.blog.persistence.repositories.BlogEntryRepository;
 
 public class PostCommentForm {
 
 	@Inject
 	private BlogEntryRepository blogEntryRepository;
 	
 	@Component
 	private Form commentForm;
 
 	@Component
 	private TextArea contentArea;
 	
 	@Component
 	private TextField nameField;
 	
 	@Component
 	private TextField emailField;
 	
	@Property
 	BlogEntryComment blogEntryComment;
 	
 	@Parameter(required=true)
 	private BlogEntry entry;
 	
	public void pageAttached(){
		blogEntryComment = new BlogEntryComment();
	}
	
 	public void onSuccessFromCommentForm(){
 		entry.getComments().add(blogEntryComment);
 		blogEntryRepository.save(entry);
 	}
 }
