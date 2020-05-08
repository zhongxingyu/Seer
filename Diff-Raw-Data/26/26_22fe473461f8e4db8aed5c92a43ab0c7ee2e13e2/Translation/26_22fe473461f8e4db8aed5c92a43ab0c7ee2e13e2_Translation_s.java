 package org.ocha.hdx.persistence.entity.i18n;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.ForeignKey;
 
 @Entity
 @Table(name = "hdx_translation")
 public class Translation {
 
 	@Embeddable
 	public static class Id implements Serializable {
 
 		private static final long serialVersionUID = -5077835132873268056L;
 
 		@ManyToOne
 		@ForeignKey(name = "fk_translation_to_text")
 		@JoinColumn(name = "text", nullable = false)
 		private Text text;
 
 		@ManyToOne
 		@ForeignKey(name = "fk_translation_to_language")
 		@JoinColumn(name = "language", nullable = false)
 		private Language language;
 
 		public Id() {
 			super();
 		}
 
 		public Id(final Text text, final Language language) {
 			super();
 			this.text = text;
 			this.language = language;
 		}
 
 		public Text getText() {
 			return text;
 		}
 
 		public void setText(final Text text) {
 			this.text = text;
 		}
 
 		public Language getLanguage() {
 			return language;
 		}
 
 		public void setLanguage(final Language language) {
 			this.language = language;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = (prime * result) + ((language == null) ? 0 : language.hashCode());
 			result = (prime * result) + ((text == null) ? 0 : text.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (getClass() != obj.getClass()) {
 				return false;
 			}
 			final Id other = (Id) obj;
 			if (language == null) {
 				if (other.language != null) {
 					return false;
 				}
 			} else if (!language.equals(other.language)) {
 				return false;
 			}
 			if (text == null) {
 				if (other.text != null) {
 					return false;
 				}
 			} else if (!text.equals(other.text)) {
 				return false;
 			}
 			return true;
 		}
 
 	}
 
 	@EmbeddedId
 	private Id id = new Id();
 
 	@Lob
 	@Column(name = "value", nullable = false, updatable = true)
 	private String value;
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(final String value) {
 		this.value = value;
 	}
 
 	public Id getId() {
 		return id;
 	}
 
 	public void setId(final Id id) {
 		this.id = id;
 	}
 
 }
