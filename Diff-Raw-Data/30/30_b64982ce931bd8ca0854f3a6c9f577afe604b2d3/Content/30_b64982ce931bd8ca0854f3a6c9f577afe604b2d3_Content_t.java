 package me.guillsowns.docgym.domain;
 
import java.util.List;

 import javax.persistence.Column;
 
 import org.springframework.roo.addon.equals.RooEquals;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.serializable.RooSerializable;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooSerializable
 @RooEquals
@RooJpaActiveRecord(finders = {"findContentsByKeyAndLocale"})
public class Content
{

	@Column(name = "keyCode")
	private String	key;

	private String	locale;

	@Column(length = 10240)
	private String	htmlContent;

	public static List<Content> findAllContents()
	{
		return entityManager().createQuery("SELECT o FROM Content o order by o.key", Content.class).getResultList();
	}

 }
