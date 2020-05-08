 package models;
 
import org.hibernate.annotations.Type;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import java.util.Date;
 
 @Entity
 public class Paste extends Model {
     @Required
 	public String title;
 
 	@Lob // Play makes this a CLOB
	@Type(type="org.hibernate.type.TextType")
 	public String code;
 	public String codeMimeType;
 
 	@Lob // Play makes this a BLOB
 	public byte[] attachment;
 	public String attachmentMimeType;
 	public String attachmentFilename;
 
 	public Date pastedAt;
 	public String pastedByNick;
 	public String pastedForNick;
 
 	public Paste() {
 		this.pastedAt = new Date();
 	}
 
 	public Paste(final String title, final String code,
 				 final String codeMimeType,
 				 final byte[] attachment,
 				 final String attachmentMimeType,
 				 final String pastedByNick,
 				 final String pastedForNick) {
 		this();
 		this.title = title;
 		this.code = code;
 		this.codeMimeType = codeMimeType;
 		this.attachment = attachment;
 		this.attachmentMimeType = attachmentMimeType;
 		this.pastedByNick = pastedByNick;
 		this.pastedForNick = pastedForNick;
 	}
 
 	public String getLongDescription() {
 		final StringBuilder description = new StringBuilder();
 
 		if (this.pastedForNick != null && !this.pastedForNick.equals(""))
 			description.append(this.pastedForNick + ": ");
 
 		description.append("new paste \"" + this.title + "\" ");
 
 		if (this.pastedByNick != null && !this.pastedByNick.equals(""))
 			description.append("from " + this.pastedByNick);
 
 		return description.toString();
 	}
 
 	public String attachmentAsString() {
 		return new String(this.attachment);
 	}
 }
