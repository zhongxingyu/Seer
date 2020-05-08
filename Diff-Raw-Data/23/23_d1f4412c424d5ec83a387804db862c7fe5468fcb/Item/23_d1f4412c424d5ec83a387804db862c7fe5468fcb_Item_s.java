 package de.congrace.model;
 
 import java.net.URI;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import de.congrace.jaxb.ItemAdapter;
 
 /**
  * Immutable Data transfer object
  * @author fasseg
  *
  */
 @XmlRootElement
 public class Item {
 	@XmlElement
 	private final Identifier id;
 	@XmlElement
 	private final String name;
 	@XmlElement
 	private final URI location;
 	@XmlElement(name="context-id")
 	private final Identifier contextId;
 
 	//JAXB needs this private constructor on root elements
 	private Item() {
 		super();
 		this.id = null;
 		this.name = null;
 		this.location = null;
 		this.contextId = null;
 	}
 
 	private Item(Builder b) {
 		super();
 		this.id = new Identifier(new String(b.id.getValue()));
 		this.name = new String(b.name);
 		this.location = URI.create(b.location.toASCIIString());
 		this.contextId = new Identifier(new String(b.contextId.getValue()));
 	}
 
 	public Identifier getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public URI getLocation() {
 		return location;
 	}
 
 	public Identifier getContextId() {
 		return contextId;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((location == null) ? 0 : location.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
 		Item other = (Item) obj;
 		if (contextId == null) {
 			if (other.contextId != null)
 				return false;
 		} else if (!contextId.equals(other.contextId))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (location == null) {
 			if (other.location != null)
 				return false;
 		} else if (!location.equals(other.location))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 
 
 
 
 
 	public static class Builder {
 		private String name;
 		private Identifier contextId;
 
 		private Identifier id;
 		private URI location;
 
 		public Builder(String name, Identifier contextId) {
 			this.contextId = contextId;
 			this.name = name;
 		}
 
		public static Builder fromItem(Item item) {
			Builder b = new Builder(item.getName(), item.getContextId());
			b.id = item.getId();
			b.location = item.getLocation();
			b.name = item.getName();
			return b;
 		}
 
 		public Builder id(Identifier id) {
 			this.id = id;
 			return this;
 		}
 
 		public Builder location(URI location) {
 			this.location = location;
 			return this;
 		}
 
 		public Item build() {
 			return new Item(this);
 		}
 	}
 }
