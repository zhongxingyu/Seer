 package net.eyelock.productlocator.model;
 
 import javax.persistence.Column;
 import javax.persistence.Lob;
 import javax.persistence.Table;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
import net.eyelock.productlocator.controllers.api.MediaAPI;
import net.eyelock.productlocator.controllers.web.MediaController;

 import org.springframework.context.annotation.Scope;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.plural.RooPlural;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.stereotype.Component;
 
 @Component
 @Scope(value = "prototype")
 @Table(name = "media")
 @RooPlural("Media")
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord(finders = { "findMediaByNameEquals", "findMediaByNameLike",
 		"findMediaByMimeTypeEquals", "findMediaByFileNameEquals",
 		"findMediaByFileNameLike", "findMediaByUrlLike",
 		"findMediaByUrlEquals", "findMediaBySizeBetween" })
 public class Media {
 	@NotNull
 	@Size(min = 2)
 	@Column(name = "name")
 	private String name;
 
 	@Size(max = 4000)
 	@Column(name = "description")
 	private String description;
 
 	@Size(min = 2, max = 255)
 	@Column(name = "file_name")
 	private String fileName;
 
 	@Column(name = "mime_type")
 	private String mimeType;
 
 	@Column(name = "file_size")
 	private Long size;
 
 	@Column(unique = true, nullable = true, name = "url")
 	@Size(min = 2, max = 255)
 	private String url;
 
 	@NotNull
 	@Lob
 	@Column(name = "content")
 	private byte[] content;
 
 	public String getMediaURL(HttpServletRequest request) {
		return request.getContextPath() + MediaController.SHOW_MEDIA_PATH + "/" + getId();
 	}
 
 	public String getMediaAPIURL(HttpServletRequest request) {
		return request.getContextPath() + MediaAPI.SHOW_MEDIA_PATH + "/" + getId();
 	}
 }
