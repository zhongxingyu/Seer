 package org.ocha.hdx.persistence.entity.i18n;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
import org.hibernate.annotations.Type;

 @Entity
 @Table(name = "text")
 @SequenceGenerator(name = "text_seq", sequenceName = "text_seq")
 public class Text {
 
 	public Text() {
 		super();
 	}
 
 	public Text(final String defaultValue) {
 		super();
 		this.defaultValue = defaultValue;
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO, generator = "text_seq")
 	@Column(name = "id", nullable = false)
 	private long id;
 
	/**
	 * Lob is not enough do to a small weirdness in Hibernate (that seems to persiste in version 4 too )
	 * That's why the type annotation is also needed.
	 * see http://www.shredzone.de/cilla/page/299/string-lobs-on-postgresql-with-hibernate-36.html
	 * 
	 */
 	@Lob
	@Type(type="org.hibernate.type.StringClobType")
 	@Column(name = "default_value", nullable = false, updatable = true)
 	private String defaultValue;
 
 	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, mappedBy = "id.text")
 	private List<Translation> translations;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(final long id) {
 		this.id = id;
 	}
 
 	public String getDefaultValue() {
 		return defaultValue;
 	}
 
 	public void setDefaultValue(final String defaultValue) {
 		this.defaultValue = defaultValue;
 	}
 
 	public List<Translation> getTranslations() {
 		return translations;
 	}
 
 	public void setTranslations(final List<Translation> translations) {
 		this.translations = translations;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
 		result = prime * result + (int) (id ^ (id >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		final Text other = (Text) obj;
 		if (defaultValue == null) {
 			if (other.defaultValue != null)
 				return false;
 		} else if (!defaultValue.equals(other.defaultValue))
 			return false;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 }
