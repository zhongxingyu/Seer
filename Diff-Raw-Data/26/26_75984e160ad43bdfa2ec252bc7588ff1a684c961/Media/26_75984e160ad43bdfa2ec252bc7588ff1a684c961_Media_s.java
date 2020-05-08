 package net.eyelock.productlocator.model;
 
 import javax.persistence.Column;
 import javax.persistence.Lob;
 import javax.persistence.Table;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
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
	public static String ADMIN_URL_PART = "/showmedia";
	public static String API_URL_PART = "/api/showmedia";

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
		return request.getContextPath() + ADMIN_URL_PART + "/" + getId();
 	}
 
 	public String getMediaAPIURL(HttpServletRequest request) {
		return request.getContextPath() + API_URL_PART + "/" + getId();
 	}
 }
